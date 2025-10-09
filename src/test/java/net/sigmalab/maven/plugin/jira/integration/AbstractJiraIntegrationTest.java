package net.sigmalab.maven.plugin.jira.integration;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.rest.client.auth.PersonalAccessTokenAuthenticationHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;

public abstract class AbstractJiraIntegrationTest {
    
    protected static String JIRA_URL;
    protected static String JIRA_USERNAME;
    protected static String JIRA_PASSWORD;
    protected static String JIRA_PAT;
    protected static String JIRA_PROJECT_KEY;
    
    protected JiraRestClient jiraRestClient;
    protected Project jiraProject;
    
    @BeforeClass
    public static void setupEnvironment() {
        // Load from system properties or environment variables
        JIRA_URL = getProperty("jira.url", getenv("JIRA_URL"));
        JIRA_USERNAME = getProperty("jira.username", getenv("JIRA_USERNAME"));
        JIRA_PASSWORD = getProperty("jira.password", getenv("JIRA_PASSWORD"));
        JIRA_PAT = getProperty("jira.pat", getenv("JIRA_PAT"));
        JIRA_PROJECT_KEY = getProperty("jira.projectKey", getenv("JIRA_PROJECT_KEY"));
        
        // Debug output
        System.out.println("=== Integration Test Configuration ===");
        System.out.println("JIRA_URL: " + (JIRA_URL != null ? JIRA_URL : "NOT SET"));
        System.out.println("JIRA_USERNAME: " + (JIRA_USERNAME != null ? JIRA_USERNAME : "NOT SET"));
        System.out.println("JIRA_PASSWORD: " + (JIRA_PASSWORD != null ? "***" : "NOT SET"));
        System.out.println("JIRA_PAT: " + (JIRA_PAT != null ? "***" : "NOT SET"));
        System.out.println("JIRA_PROJECT_KEY: " + (JIRA_PROJECT_KEY != null ? JIRA_PROJECT_KEY : "NOT SET"));
        System.out.println("=====================================");
        
        // Check basic required configuration
        Assume.assumeNotNull(
            "Integration tests require JIRA URL. Set -Djira.url=... or JIRA_URL environment variable",
            JIRA_URL
        );
        Assume.assumeNotNull(
            "Integration tests require JIRA Project Key. Set -Djira.projectKey=... or JIRA_PROJECT_KEY environment variable",
            JIRA_PROJECT_KEY
        );
        
        // Check authentication configuration (related params bundled together)
        // Check for both null and empty string
        boolean hasPassword = JIRA_PASSWORD != null && !JIRA_PASSWORD.trim().isEmpty();
        boolean hasPAT = JIRA_PAT != null && !JIRA_PAT.trim().isEmpty();
        boolean hasBothAuth = hasPassword && hasPAT;
        boolean hasEitherAuth = hasPassword || hasPAT;
        
        Assume.assumeFalse(
            "Integration tests require only ONE authentication method. Provide either password OR PAT, not both",
            hasBothAuth
        );
        
        Assume.assumeTrue(
            "Integration tests require authentication. Provide either -Djira.password=... or -Djira.pat=...",
            hasEitherAuth
        );
        
        // Username is always required (for both password and PAT authentication)
        Assume.assumeNotNull(
            "Integration tests require JIRA username. Set -Djira.username=... or JIRA_USERNAME environment variable",
            JIRA_USERNAME
        );
    }
    
    @Before
    public void setUp() throws Exception {
        // Create Jira client
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        
        if (JIRA_PAT != null) {
            jiraRestClient = factory.createWithAuthenticationHandler(
                new URI(JIRA_URL),
                new PersonalAccessTokenAuthenticationHandler(JIRA_USERNAME, JIRA_PAT)
            );
        } else {
            jiraRestClient = factory.createWithBasicHttpAuthentication(
                new URI(JIRA_URL),
                JIRA_USERNAME,
                JIRA_PASSWORD
            );
        }
        
        // Get project information
        jiraProject = jiraRestClient.getProjectClient().getProject(JIRA_PROJECT_KEY).claim();
    }
    
    @After
    public void tearDown() {
        try {
            // Perform cleanup operations first
            // Individual cleanup methods handle their own exceptions
        } finally {
            // Always close client, even if cleanup fails
            if (jiraRestClient != null) {
                try {
                    jiraRestClient.close();
                } catch (IOException e) {
                    System.err.println("Error closing Jira REST client: " + e.getMessage());
                }
            }
        }
    }
    
    // Helper methods for test cleanup
    protected void cleanupTestVersions(String versionPrefix) {
        if (jiraProject == null) {
            System.err.println("Cannot cleanup versions: jiraProject is null");
            return;
        }
        
        try {
            for (Version version : jiraProject.getVersions()) {
                if (version.getName().startsWith(versionPrefix)) {
                    try {
                        jiraRestClient.getVersionRestClient()
                            .removeVersion(version.getSelf(), null, null).claim();
                        System.out.println("Cleaned up test version: " + version.getName());
                    } catch (Exception e) {
                        System.err.println("Failed to cleanup version " + version.getName() + ": " + e.getMessage());
                        // Continue with other versions
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during version cleanup: " + e.getMessage());
        }
    }
    
    protected void cleanupTestIssues(String summaryPrefix) {
        try {
            String jql = "project = " + JIRA_PROJECT_KEY + " AND summary ~ \"" + summaryPrefix + "\"";
            SearchRestClient searchClient = jiraRestClient.getSearchClient();
            Iterable<Issue> issues = searchClient.searchJql(jql).claim().getIssues();
            
            for (Issue issue : issues) {
                try {
                    jiraRestClient.getIssueClient().deleteIssue(issue.getKey(), true).claim();
                    System.out.println("Cleaned up test issue: " + issue.getKey());
                } catch (Exception e) {
                    System.err.println("Failed to cleanup issue " + issue.getKey() + ": " + e.getMessage());
                    // Continue with other issues
                }
            }
        } catch (Exception e) {
            System.err.println("Error during issue cleanup: " + e.getMessage());
        }
    }
}
