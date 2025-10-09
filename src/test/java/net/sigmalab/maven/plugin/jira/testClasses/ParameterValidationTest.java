package net.sigmalab.maven.plugin.jira.testClasses;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import io.atlassian.util.concurrent.Promise;

import net.sigmalab.maven.plugin.jira.CreateNewVersionMojo;
import net.sigmalab.maven.plugin.jira.GenerateReleaseNotesMojo;
import net.sigmalab.maven.plugin.jira.ReleaseVersionMojo;
import net.sigmalab.maven.plugin.jira.testClasses.TestAbstractJiraMojo;

import java.io.File;

/**
 * Tests for parameter validation in the Jira Maven Plugin.
 * 
 * Note on Exception Types:
 * - MojoExecutionException: Thrown during parameter validation in execute() method
 * - MojoFailureException: Thrown during mojo-specific validation in doExecute() method
 */
@RunWith(JUnit4.class)
public class ParameterValidationTest {

    private Log mockLog;
    private JiraRestClient mockJiraRestClient;
    private ProjectRestClient mockProjectClient;
    private VersionRestClient mockVersionClient;
    private Promise<Project> mockProjectPromise;
    private Project mockProject;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        mockLog = mock(Log.class);
        
        // Create mock objects for JiraRestClient
        mockJiraRestClient = mock(JiraRestClient.class);
        mockProjectClient = mock(ProjectRestClient.class);
        mockVersionClient = mock(VersionRestClient.class);
        mockProjectPromise = (Promise<Project>) mock(Promise.class);
        mockProject = mock(Project.class);
        
        // Set up the mock chain
        when(mockJiraRestClient.getProjectClient()).thenReturn(mockProjectClient);
        when(mockJiraRestClient.getVersionRestClient()).thenReturn(mockVersionClient);
        when(mockProjectClient.getProject(any(String.class))).thenReturn(mockProjectPromise);
        when(mockProjectPromise.claim()).thenReturn(mockProject);
    }
    
    /**
     * Helper method to assert MojoExecutionException with specific message fragment.
     * Provides better error messages when assertions fail.
     */
    private void assertMojoExecutionExceptionWithMessage(Exception e, String expectedMessageFragment) {
        assertTrue("Exception should be MojoExecutionException but was: " + e.getClass().getName(),
                   e instanceof MojoExecutionException);
        assertTrue("Expected message to contain '" + expectedMessageFragment + "' but was: " + e.getMessage(),
                   e.getMessage().contains(expectedMessageFragment));
    }
    
    /**
     * Test that validation fails when JIRA URL is missing
     */
    @Test
    public void testMissingJiraUrl() {
        TestAbstractJiraMojo mojo = new TestAbstractJiraMojo();
        mojo.setJiraUser("user");
        mojo.setJiraPassword("password");
        mojo.setJiraProjectKey("TEST");
        
        // Don't set JIRA URL
        
        // Set the mock JiraRestClient to avoid actual REST calls
        mojo.setJiraRestClient(mockJiraRestClient);
        
        try {
            mojo.testValidateParameters();
            fail("Expected MojoExecutionException was not thrown");
        } catch (Exception e) {
            assertMojoExecutionExceptionWithMessage(e, "JIRA URL is required");
        }
    }
    
    /**
     * Test that validation fails when JIRA project key is missing.
     * Note: Explicitly sets project key to null to test validation directly,
     * rather than relying on URL parsing side effects.
     */
    @Test
    public void testMissingProjectKey() {
        TestAbstractJiraMojo mojo = new TestAbstractJiraMojo();
        mojo.setJiraUser("user");
        mojo.setJiraPassword("password");
        mojo.setJiraURL("https://jira.example.com");
        
        // Explicitly set project key to null to test validation
        mojo.setJiraProjectKey(null);
        
        // Set the mock JiraRestClient to avoid actual REST calls
        mojo.setJiraRestClient(mockJiraRestClient);
        
        try {
            mojo.testValidateParameters();
            fail("Expected MojoExecutionException was not thrown");
        } catch (Exception e) {
            assertMojoExecutionExceptionWithMessage(e, "JIRA Project Key is required");
        }
    }
    
    /**
     * Test that validation fails when JIRA username is missing
     */
    @Test
    public void testMissingUsername() {
        TestAbstractJiraMojo mojo = new TestAbstractJiraMojo();
        mojo.setJiraPassword("password");
        mojo.setJiraURL("https://jira.example.com");
        mojo.setJiraProjectKey("TEST");
        
        // Don't set username
        
        // Set the mock JiraRestClient to avoid actual REST calls
        mojo.setJiraRestClient(mockJiraRestClient);
        
        try {
            mojo.testValidateParameters();
            fail("Expected MojoExecutionException was not thrown");
        } catch (Exception e) {
            assertMojoExecutionExceptionWithMessage(e, "JIRA Username is required");
        }
    }
    
    /**
     * Test that validation fails when JIRA password is missing and no PAT is provided
     */
    @Test
    public void testMissingPassword() {
        TestAbstractJiraMojo mojo = new TestAbstractJiraMojo();
        mojo.setJiraUser("user");
        mojo.setJiraURL("https://jira.example.com");
        mojo.setJiraProjectKey("TEST");
        
        // Don't set password or PAT
        
        // Set the mock JiraRestClient to avoid actual REST calls
        mojo.setJiraRestClient(mockJiraRestClient);
        
        try {
            mojo.testValidateParameters();
            fail("Expected MojoExecutionException was not thrown");
        } catch (Exception e) {
            assertMojoExecutionExceptionWithMessage(e, "JIRA Password is required");
        }
    }
    
    /**
     * Test that validation fails when both password and PAT are provided
     */
    @Test
    public void testBothPasswordAndPat() {
        TestAbstractJiraMojo mojo = new TestAbstractJiraMojo();
        mojo.setJiraUser("user");
        mojo.setJiraPassword("password");
        mojo.setJiraPersonalAccessToken("token");
        mojo.setJiraURL("https://jira.example.com");
        mojo.setJiraProjectKey("TEST");
        
        // Set the mock JiraRestClient to avoid actual REST calls
        mojo.setJiraRestClient(mockJiraRestClient);
        
        try {
            mojo.testValidateParameters();
            fail("Expected MojoExecutionException was not thrown");
        } catch (Exception e) {
            assertMojoExecutionExceptionWithMessage(e, "Both password and Personal Access Token provided");
        }
    }
    
    /**
     * Test that validation fails when development version is missing in CreateNewVersionMojo
     */
    @Test(expected = MojoFailureException.class)
    public void testMissingDevelopmentVersion() throws MojoExecutionException, MojoFailureException {
        CreateNewVersionMojo mojo = new CreateNewVersionMojo();
        mojo.setJiraUser("user");
        mojo.setJiraPassword("password");
        mojo.setJiraURL("https://jira.example.com");
        mojo.setJiraProjectKey("TEST");
        
        // Don't set development version
        
        // Mock the JiraRestClient to avoid actual REST calls
        JiraRestClient mockClient = mock(JiraRestClient.class);
        mojo.setJiraRestClient(mockClient);
        
        // This should throw MojoFailureException
        mojo.doExecute(mockClient);
    }
    
    /**
     * Test that validation fails when release version is missing in ReleaseVersionMojo
     */
    @Test(expected = MojoFailureException.class)
    public void testMissingReleaseVersion() throws MojoExecutionException, MojoFailureException {
        ReleaseVersionMojo mojo = new ReleaseVersionMojo();
        mojo.setJiraUser("user");
        mojo.setJiraPassword("password");
        mojo.setJiraURL("https://jira.example.com");
        mojo.setJiraProjectKey("TEST");
        
        // Don't set release version and don't enable auto-discover
        
        // Mock the JiraRestClient to avoid actual REST calls
        JiraRestClient mockClient = mock(JiraRestClient.class);
        mojo.setJiraRestClient(mockClient);
        
        // This should throw MojoFailureException
        mojo.doExecute(mockClient);
    }
    
    /**
     * Test that validation fails when required parameters are missing in GenerateReleaseNotesMojo
     */
    @Test(expected = MojoFailureException.class)
    public void testMissingGenerateReleaseNotesParameters() throws MojoExecutionException, MojoFailureException {
        GenerateReleaseNotesMojo mojo = new GenerateReleaseNotesMojo();
        mojo.setJiraUser("user");
        mojo.setJiraPassword("password");
        mojo.setJiraURL("https://jira.example.com");
        mojo.setJiraProjectKey("TEST");
        
        // Don't set release version
        
        // Mock the JiraRestClient to avoid actual REST calls
        JiraRestClient mockClient = mock(JiraRestClient.class);
        mojo.setJiraRestClient(mockClient);
        
        // This should throw MojoFailureException
        mojo.doExecute(mockClient);
    }
    
    // Edge Case Tests
    
    /**
     * Test that validation fails when JIRA URL is empty string (not just null)
     */
    @Test
    public void testEmptyJiraUrl() {
        TestAbstractJiraMojo mojo = new TestAbstractJiraMojo();
        mojo.setJiraUser("user");
        mojo.setJiraPassword("password");
        mojo.setJiraURL(""); // Empty string
        mojo.setJiraProjectKey("TEST");
        
        mojo.setJiraRestClient(mockJiraRestClient);
        
        try {
            mojo.testValidateParameters();
            fail("Expected MojoExecutionException for empty URL");
        } catch (Exception e) {
            assertMojoExecutionExceptionWithMessage(e, "JIRA URL is required");
        }
    }
    
    /**
     * Test that validation fails when JIRA project key is empty string (not just null)
     */
    @Test
    public void testEmptyProjectKey() {
        TestAbstractJiraMojo mojo = new TestAbstractJiraMojo();
        mojo.setJiraUser("user");
        mojo.setJiraPassword("password");
        mojo.setJiraURL("https://jira.example.com");
        mojo.setJiraProjectKey(""); // Empty string
        
        mojo.setJiraRestClient(mockJiraRestClient);
        
        try {
            mojo.testValidateParameters();
            fail("Expected MojoExecutionException for empty project key");
        } catch (Exception e) {
            assertMojoExecutionExceptionWithMessage(e, "JIRA Project Key is required");
        }
    }
    
    /**
     * Test that validation fails when JIRA username is empty string (not just null)
     */
    @Test
    public void testEmptyUsername() {
        TestAbstractJiraMojo mojo = new TestAbstractJiraMojo();
        mojo.setJiraUser(""); // Empty string
        mojo.setJiraPassword("password");
        mojo.setJiraURL("https://jira.example.com");
        mojo.setJiraProjectKey("TEST");
        
        mojo.setJiraRestClient(mockJiraRestClient);
        
        try {
            mojo.testValidateParameters();
            fail("Expected MojoExecutionException for empty username");
        } catch (Exception e) {
            assertMojoExecutionExceptionWithMessage(e, "JIRA Username is required");
        }
    }
    
    /**
     * Test that validation fails when JIRA password is empty string and no PAT is provided
     */
    @Test
    public void testEmptyPassword() {
        TestAbstractJiraMojo mojo = new TestAbstractJiraMojo();
        mojo.setJiraUser("user");
        mojo.setJiraPassword(""); // Empty string
        mojo.setJiraURL("https://jira.example.com");
        mojo.setJiraProjectKey("TEST");
        
        mojo.setJiraRestClient(mockJiraRestClient);
        
        try {
            mojo.testValidateParameters();
            fail("Expected MojoExecutionException for empty password");
        } catch (Exception e) {
            assertMojoExecutionExceptionWithMessage(e, "JIRA Password is required");
        }
    }
    
    /**
     * Test that validation succeeds when using PAT authentication (password not required)
     */
    @Test
    public void testPatAuthenticationWithoutPassword() {
        TestAbstractJiraMojo mojo = new TestAbstractJiraMojo();
        mojo.setJiraUser("user");
        mojo.setJiraPersonalAccessToken("valid-pat-token");
        mojo.setJiraURL("https://jira.example.com");
        mojo.setJiraProjectKey("TEST");
        
        mojo.setJiraRestClient(mockJiraRestClient);
        
        try {
            mojo.testValidateParameters();
            // Should succeed without exception
        } catch (Exception e) {
            fail("Should not throw exception for valid PAT authentication: " + e.getMessage());
        }
    }
    
    /**
     * Test that validation fails when finalName is required but not provided in CreateNewVersionMojo
     */
    @Test(expected = MojoFailureException.class)
    public void testFinalNameRequiredWhenFlagEnabled() throws MojoExecutionException, MojoFailureException {
        CreateNewVersionMojo mojo = new CreateNewVersionMojo();
        mojo.setJiraUser("user");
        mojo.setJiraPassword("password");
        mojo.setJiraURL("https://jira.example.com");
        mojo.setJiraProjectKey("TEST");
        mojo.setDevelopmentVersion("1.0.0-SNAPSHOT");
        mojo.setFinalNameUsedForVersion(true);
        // Don't set finalName - should fail validation
        
        JiraRestClient mockClient = mock(JiraRestClient.class);
        mojo.setJiraRestClient(mockClient);
        
        mojo.doExecute(mockClient);
    }
}
