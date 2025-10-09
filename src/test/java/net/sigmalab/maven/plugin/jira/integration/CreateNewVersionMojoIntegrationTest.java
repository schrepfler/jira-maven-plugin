package net.sigmalab.maven.plugin.jira.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.atlassian.jira.rest.client.api.domain.Version;

import net.sigmalab.maven.plugin.jira.CreateNewVersionMojo;

@RunWith(JUnit4.class)
public class CreateNewVersionMojoIntegrationTest extends AbstractJiraIntegrationTest {
    
    private static final String TEST_VERSION_PREFIX = "IntegrationTest-";
    
    @After
    public void cleanupTestData() {
        cleanupTestVersions(TEST_VERSION_PREFIX);
    }
    
    @Test
    public void testCreateNewVersion() throws Exception {
        // Create a unique version name for this test
        String versionName = TEST_VERSION_PREFIX + System.currentTimeMillis();
        
        // Setup the mojo
        CreateNewVersionMojo mojo = new CreateNewVersionMojo();
        mojo.setJiraURL(JIRA_URL);
        mojo.setJiraUser(JIRA_USERNAME);
        
        if (JIRA_PAT != null) {
            mojo.setJiraPersonalAccessToken(JIRA_PAT);
        } else {
            mojo.setJiraPassword(JIRA_PASSWORD);
        }
        
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
        
        assertThat("Version should have been created", versionExists, is(true));
    }
    
    @Test
    public void testCreateNewVersionWithFinalName() throws Exception {
        // Create a unique version name for this test
        String versionName = TEST_VERSION_PREFIX + System.currentTimeMillis() + "-SNAPSHOT";
        String finalName = "my-component-" + versionName;
        // Note: CreateNewVersionMojo strips -SNAPSHOT from the version name
        String expectedVersionName = finalName.replace("-SNAPSHOT", "");
        
        // Setup the mojo
        CreateNewVersionMojo mojo = new CreateNewVersionMojo();
        mojo.setJiraURL(JIRA_URL);
        mojo.setJiraUser(JIRA_USERNAME);
        
        if (JIRA_PAT != null) {
            mojo.setJiraPersonalAccessToken(JIRA_PAT);
        } else {
            mojo.setJiraPassword(JIRA_PASSWORD);
        }
        
        mojo.setJiraProjectKey(JIRA_PROJECT_KEY);
        mojo.setDevelopmentVersion(versionName);
        mojo.setFinalNameUsedForVersion(true);
        mojo.setFinalName(finalName);
        
        // Execute the mojo
        mojo.execute();
        
        // Wait a bit for Jira to process the version creation
        Thread.sleep(1000);
        
        // Refresh project data to get updated versions
        jiraProject = jiraRestClient.getProjectClient().getProject(JIRA_PROJECT_KEY).claim();
        
        // Verify the version was created with the expected name (without -SNAPSHOT)
        boolean versionExists = false;
        System.out.println("Looking for version: " + expectedVersionName);
        System.out.println("Available versions:");
        for (Version version : jiraProject.getVersions()) {
            System.out.println("  - " + version.getName());
            if (version.getName().equals(expectedVersionName)) {
                versionExists = true;
                break;
            }
        }
        
        assertThat("Version with expected name should have been created", versionExists, is(true));
    }
}
