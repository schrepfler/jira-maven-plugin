
package net.sigmalab.maven.plugin.java;

import java.util.Arrays;

import net.sigmalab.maven.plugin.jira.CreateNewVersionMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.ProjectRestClient;
import com.atlassian.jira.rest.client.VersionRestClient;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.util.concurrent.Promise;

/**
 * JUnit test case for CreateNewVersionMojo
 * 
 * @author george
 * @author dgrierso
 * 
 */
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
        jiraVersionMojo.setJiraURL("http://cbsjira.bskyb.com/browse/" + jiraVersionMojo.getJiraProjectKey());
        
		mockJiraRestClient = Mockito.mock(JiraRestClient.class);

        VersionRestClient mockVersionClient = Mockito.mock(VersionRestClient.class);
        Mockito.when(mockJiraRestClient.getVersionRestClient()).thenReturn(mockVersionClient);
        
        ProjectRestClient mockProjectClient = Mockito.mock(ProjectRestClient.class);
        Mockito.when(mockJiraRestClient.getProjectClient()).thenReturn(mockProjectClient);
        
        @SuppressWarnings("unchecked")
        Promise<Project> mockProjectPromise = (Promise<Project>) Mockito.mock(Promise.class);
        Mockito.when(mockProjectClient.getProject(jiraVersionMojo.getJiraProjectKey())).thenReturn(mockProjectPromise);
        
        Project mockProject = Mockito.mock(Project.class);
        Mockito.when(mockProjectPromise.claim()).thenReturn(mockProject);
        
        Mockito.when(mockProject.getVersions()).thenReturn(VERSIONS);
        
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
