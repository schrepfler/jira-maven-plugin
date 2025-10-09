package net.sigmalab.maven.plugin.jira.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.atlassian.jira.rest.client.api.domain.Version;

import net.sigmalab.maven.plugin.jira.CreateNewVersionMojo;
import net.sigmalab.maven.plugin.jira.ReleaseVersionMojo;

@RunWith(JUnit4.class)
public class ReleaseVersionMojoIntegrationTest extends AbstractJiraIntegrationTest {
    
    private static final String TEST_VERSION_PREFIX = "IntegrationTest-Release-";
    private String versionName;
    
    @Before
    public void createTestVersion() throws Exception {
        // Create a unique version name for this test
        versionName = TEST_VERSION_PREFIX + System.currentTimeMillis();
        
        // Create a version to release
        CreateNewVersionMojo createMojo = new CreateNewVersionMojo();
        createMojo.setJiraURL(JIRA_URL);
        createMojo.setJiraUser(JIRA_USERNAME);
        
        if (JIRA_PAT != null) {
            createMojo.setJiraPersonalAccessToken(JIRA_PAT);
        } else {
            createMojo.setJiraPassword(JIRA_PASSWORD);
        }
        
        createMojo.setJiraProjectKey(JIRA_PROJECT_KEY);
        createMojo.setDevelopmentVersion(versionName);
        
        // Execute the mojo to create the version
        createMojo.execute();
    }
    
    @After
    public void cleanupTestData() {
        cleanupTestVersions(TEST_VERSION_PREFIX);
    }
    
    @Test
    public void testReleaseVersion() throws Exception {
        // Setup the release mojo
        ReleaseVersionMojo mojo = new ReleaseVersionMojo();
        mojo.setJiraURL(JIRA_URL);
        mojo.setJiraUser(JIRA_USERNAME);
        
        if (JIRA_PAT != null) {
            mojo.setJiraPersonalAccessToken(JIRA_PAT);
        } else {
            mojo.setJiraPassword(JIRA_PASSWORD);
        }
        
        mojo.setJiraProjectKey(JIRA_PROJECT_KEY);
        mojo.setReleaseVersion(versionName);
        
        // Execute the mojo to release the version
        mojo.execute();
        
        // Refresh project data to get updated versions
        jiraProject = jiraRestClient.getProjectClient().getProject(JIRA_PROJECT_KEY).claim();
        
        // Verify the version was released
        boolean versionReleased = false;
        for (Version version : jiraProject.getVersions()) {
            if (version.getName().equals(versionName) && version.isReleased()) {
                versionReleased = true;
                break;
            }
        }
        
        assertThat("Version should have been released", versionReleased, is(true));
    }
    
    @Test
    public void testReleaseLatestVersion() throws Exception {
        // Setup the release mojo without specifying a version
        // It should release the latest version (which is our test version)
        ReleaseVersionMojo mojo = new ReleaseVersionMojo();
        mojo.setJiraURL(JIRA_URL);
        mojo.setJiraUser(JIRA_USERNAME);
        
        if (JIRA_PAT != null) {
            mojo.setJiraPersonalAccessToken(JIRA_PAT);
        } else {
            mojo.setJiraPassword(JIRA_PASSWORD);
        }
        
        mojo.setJiraProjectKey(JIRA_PROJECT_KEY);
        // Enable auto-discovery of latest version
        mojo.setAutoDiscoverLatestRelease(true);
        
        // Execute the mojo to release the version
        mojo.execute();
        
        // Refresh project data to get updated versions
        jiraProject = jiraRestClient.getProjectClient().getProject(JIRA_PROJECT_KEY).claim();
        
        // Verify the version was released
        boolean versionReleased = false;
        for (Version version : jiraProject.getVersions()) {
            if (version.getName().equals(versionName) && version.isReleased()) {
                versionReleased = true;
                break;
            }
        }
        
        assertThat("Latest version should have been released", versionReleased, is(true));
    }
}
