package net.sigmalab.maven.plugin.jira.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import net.sigmalab.maven.plugin.jira.CreateNewVersionMojo;

import java.io.IOException;
import java.net.URI;

/**
 * Integration test that specifically tests password-based authentication.
 * This test will be skipped if JIRA_PASSWORD is not set.
 */
@RunWith(JUnit4.class)
public class PasswordAuthenticationIT {
    
    private static final String TEST_VERSION_PREFIX = "PasswordAuth-Test-";
    
    protected static String JIRA_URL;
    protected static String JIRA_USERNAME;
    protected static String JIRA_PASSWORD;
    protected static String JIRA_PROJECT_KEY;
    
    protected JiraRestClient jiraRestClient;
    protected Project jiraProject;
    
    @BeforeClass
    public static void setupEnvironment() {
        // Load from system properties or environment variables
        JIRA_URL = System.getProperty("jira.url", System.getenv("JIRA_URL"));
        JIRA_USERNAME = System.getProperty("jira.username", System.getenv("JIRA_USERNAME"));
        JIRA_PASSWORD = System.getProperty("jira.password", System.getenv("JIRA_PASSWORD"));
        JIRA_PROJECT_KEY = System.getProperty("jira.projectKey", System.getenv("JIRA_PROJECT_KEY"));
        
        // Skip test if password authentication is not configured
        org.junit.Assume.assumeTrue("Password authentication not configured, skipping test", 
            JIRA_URL != null && JIRA_PROJECT_KEY != null && 
            JIRA_USERNAME != null && JIRA_PASSWORD != null);
    }
    
    @Before
    public void setUp() throws Exception {
        // Create Jira client with password authentication
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        jiraRestClient = factory.createWithBasicHttpAuthentication(
            new URI(JIRA_URL),
            JIRA_USERNAME,
            JIRA_PASSWORD
        );
        
        // Get project information
        jiraProject = jiraRestClient.getProjectClient().getProject(JIRA_PROJECT_KEY).claim();
    }
    
    @After
    public void tearDown() {
        // Clean up test versions
        if (jiraRestClient != null) {
            try {
                for (Version version : jiraProject.getVersions()) {
                    if (version.getName().startsWith(TEST_VERSION_PREFIX)) {
                        jiraRestClient.getVersionRestClient().removeVersion(version.getSelf(), null, null).claim();
                    }
                }
                
                jiraRestClient.close();
            } catch (IOException e) {
                System.err.println("Error closing Jira REST client: " + e.getMessage());
            }
        }
    }
    
    @Test
    public void testPasswordAuthentication() throws Exception {
        // Create a unique version name for this test
        String versionName = TEST_VERSION_PREFIX + System.currentTimeMillis();
        
        // Setup the mojo with password authentication
        CreateNewVersionMojo mojo = new CreateNewVersionMojo();
        mojo.setJiraURL(JIRA_URL);
        mojo.setJiraUser(JIRA_USERNAME);
        mojo.setJiraPassword(JIRA_PASSWORD);
        mojo.setJiraProjectKey(JIRA_PROJECT_KEY);
        mojo.setDevelopmentVersion(versionName);
        
        // Execute the mojo
        mojo.execute();
        
        // Refresh project data to get updated versions
        jiraProject = jiraRestClient.getProjectClient().getProject(JIRA_PROJECT_KEY).claim();
        
        // Verify the version was created
        boolean versionExists = false;
        for (Version version : jiraProject.getVersions()) {
            if (version.getName().equals(versionName)) {
                versionExists = true;
                break;
            }
        }
        
        assertThat("Version should have been created using password authentication", versionExists, is(true));
    }
}