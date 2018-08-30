
package net.sigmalab.maven.plugin.jira.testClasses;

import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.VersionInput;
import com.atlassian.util.concurrent.Promise;

import net.sigmalab.maven.plugin.jira.CreateNewVersionMojo;

/**
 * JUnit test case for CreateNewVersionMojo
 * 
 * @author george
 * @author dgrierso
 * 
 */
@RunWith(JUnit4.class)
public class CreateNewVersionMojoTest  {

    private static final Version[] VERSION_ARRAY = new Version[] { new Version(null, null, "3.1", "Release 3.1 (Delta)", false, false, new DateTime()),
                                                                   new Version(null, null, "3.0", "Release 3.0 (Gamma)", false, false, new DateTime()),
                                                                   new Version(null, null, "2.0", "Release 2.0 (Beta)",  false, false, new DateTime()),
                                                                   new Version(null, null, "1.0", "Release 1.0 (Alpha)", false, false, new DateTime()) };
    private static final Iterable<Version> VERSIONS = Arrays.asList(VERSION_ARRAY);

	private CreateNewVersionMojo jiraVersionMojo;
	
	private JiraRestClient mockJiraRestClient;

	@Before
	public void setUp() {
		jiraVersionMojo = new CreateNewVersionMojo();
		
		jiraVersionMojo.setJiraUser("user");
		jiraVersionMojo.setJiraPassword("password");
		jiraVersionMojo.setJiraProjectKey("KEY");
        jiraVersionMojo.setJiraURL("http://localhost/jira/browse/" + jiraVersionMojo.getJiraProjectKey());
        
		mockJiraRestClient = Mockito.mock(JiraRestClient.class);        
        
        // Mocking the Project REST handling
		// First the ProjectRestClient
        ProjectRestClient mockProjectClient = Mockito.mock(ProjectRestClient.class);
        Mockito.when(mockJiraRestClient.getProjectClient()).thenReturn(mockProjectClient);
        
        // Next the retrieval of project information from that REST client.
        @SuppressWarnings("unchecked")
        Promise<Project> mockProjectPromise = (Promise<Project>) Mockito.mock(Promise.class);
        Mockito.when(mockProjectClient.getProject(jiraVersionMojo.getJiraProjectKey())).thenReturn(mockProjectPromise);
        
        Project mockProject = Mockito.mock(Project.class);
        Mockito.when(mockProjectPromise.claim()).thenReturn(mockProject);
        Mockito.when(mockProject.getVersions()).thenReturn(VERSIONS);
        
        // Mocking the Version REST handling
        VersionRestClient mockVersionClient = Mockito.mock(VersionRestClient.class);
        Mockito.when(mockJiraRestClient.getVersionRestClient()).thenReturn(mockVersionClient);
        
        @SuppressWarnings("unchecked")
        Promise<Version> mockVersionPromise = (Promise<Version>) Mockito.mock(Promise.class);
        Mockito.when(mockVersionClient.createVersion(any(VersionInput.class))).thenReturn(mockVersionPromise);
        
        Version mockVersion = Mockito.mock(Version.class);
        Mockito.when(mockVersionPromise.claim()).thenReturn(mockVersion);
                
        jiraVersionMojo.setJiraRestClient(mockJiraRestClient);
	}

	/**
	 * Test method for {@link CreateNewVersionMojo#execute()}
	 * @throws MojoFailureException 
	 * @throws MojoExecutionException 
	 */
	@Test
	public void testExecuteWithNewDevVersion() throws MojoExecutionException, MojoFailureException {
		jiraVersionMojo.setDevelopmentVersion("v4.0");

		jiraVersionMojo.execute();
	}

	@Test
	public void testExecuteWithNewDevVersionIncludingQualifierAndSnapshot() throws MojoExecutionException, MojoFailureException {
		jiraVersionMojo.setDevelopmentVersion("5.0-beta-2-SNAPSHOT");

		jiraVersionMojo.execute();
	}

	@Test
	public void testExecuteWithNewDevVersionAndUseFinalNameForVersionSetToTrue() throws MojoExecutionException, MojoFailureException {
		jiraVersionMojo.setDevelopmentVersion("5.0-beta-2-SNAPSHOT");
		jiraVersionMojo.setFinalNameUsedForVersion(true);
		jiraVersionMojo.setFinalName("my-component-5.0-beta-2-SNAPSHOT");

		jiraVersionMojo.execute();
	}

	@Test
	public void testExecuteWithExistentDevVersion() throws MojoExecutionException, MojoFailureException  {
		jiraVersionMojo.setDevelopmentVersion("2.0");
		
		jiraVersionMojo.execute();
	}

	@After
	public void tearDown() {
		this.jiraVersionMojo = null;
	}
}
