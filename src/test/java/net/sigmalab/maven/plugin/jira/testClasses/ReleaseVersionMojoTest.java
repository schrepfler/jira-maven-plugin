package net.sigmalab.maven.plugin.jira.testClasses;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.net.URI;
import java.util.Arrays;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.joda.time.DateTime;
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

import net.sigmalab.maven.plugin.jira.ReleaseVersionMojo;

/**
 * JUnit test case for ReleaseVersionMojo
 * 
 * @author george
 * @author dgrierso
 * 
 */
@RunWith(JUnit4.class)
public class ReleaseVersionMojoTest {
    
    private static String TESTKEY = "TESTKEY";

    // Latest version is released.
    private static final Version[] VERSION_ARRAY = new Version[] { new Version(null, null, "3.1", "Release 3.1 (Delta)", false, true, new DateTime()),
                                                                  new Version(null, null, "3.0", "Release 3.0 (Gamma)", false, false, new DateTime()),
                                                                  new Version(null, null, "2.0", "Release 2.0 (Beta)",  false, false, new DateTime()),
                                                                  new Version(null, null, "1.0", "Release 1.0 (Alpha)", false, false, new DateTime()) };
    
    private static final Iterable<Version> VERSIONS = Arrays.asList(VERSION_ARRAY);

    private static final DateTime RELEASE_TIME = new DateTime();
    private static final Version RELEASED_VERSION = new Version(null, null, "3.0", "Release 3.0 (Gamma)", false, true, RELEASE_TIME);

	
	private ReleaseVersionMojo jiraVersionMojo;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.jiraVersionMojo = new ReleaseVersionMojo();
		
		jiraVersionMojo.setJiraUser("user");
		jiraVersionMojo.setJiraPassword("password");
		jiraVersionMojo.setJiraProjectKey(TESTKEY);
		jiraVersionMojo.setJiraURL("http://localhost/jira/browse/" + jiraVersionMojo.getJiraProjectKey());

		JiraRestClient mockJiraRestClient = Mockito.mock(JiraRestClient.class);
		
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
		
		@SuppressWarnings("unchecked")
        Promise<Version> mockVersionPromise = (Promise<Version>) Mockito.mock(Promise.class);
		Mockito.when(mockVersionClient.updateVersion((URI) any(), any(VersionInput.class))).thenReturn(mockVersionPromise);
		Mockito.when(mockVersionPromise.claim()).thenReturn(RELEASED_VERSION);
        
		jiraVersionMojo.setJiraRestClient(mockJiraRestClient);
	}

	@Test
	public void testLatestVersionInfo() throws Exception {
		final String expected = "3.0";
		
		Version actual = jiraVersionMojo.calculateLatestReleaseVersion(VERSIONS);
		
		assertThat(actual.getName(), is(equalTo(expected)));
	}

	@Test
	public void testExecuteWithReleaseVersion() throws MojoExecutionException, MojoFailureException {
		jiraVersionMojo.setReleaseVersion("3.0");

		jiraVersionMojo.execute();
	}
}
