
package net.sigmalab.maven.plugin.java;

import java.util.Arrays;

import net.sigmalab.maven.plugin.jira.CreateNewVersionMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.input.VersionInput;

/**
 * JUnit test case for CreateNewVersionMojo
 * 
 * @author george
 * @author dgrierso
 * 
 */
public class CreateNewVersionMojoTest  {

	private static final Version[] versionArray = new Version[] { new Version(null, null, "3.1", "Release 3.1 (Gamma)", false, false, new DateTime()),
	                                                              new Version(null, null, "3.0", "Release 3.0 (Delta)", false, false, new DateTime()),
                                                                  new Version(null, null, "2.0", "Release 2.0 (Beta)",  false, false, new DateTime()),
                                                                  new Version(null, null, "1.0", "Release 1.0 (Alpha)", false, false, new DateTime()) };
	
	private static final Iterable<Version> VERSIONS = Arrays.asList(versionArray);

	private CreateNewVersionMojo jiraVersionMojo;
	
	@Mock
	private JiraRestClient jiraStub;

	@Before
	public void setUp() {
		this.jiraVersionMojo = new CreateNewVersionMojo();
		jiraVersionMojo.setJiraUser("user");
		jiraVersionMojo.setJiraPassword("password");

		// This removes the locator coupling
		jiraStub = EasyMock.createStrictMock(JiraRestClient.class);
		jiraVersionMojo.setJiraRestClient(jiraStub);
	}

	/**
	 * Test method for {@link CreateNewVersionMojo#execute()}
	 * @throws MojoFailureException 
	 * @throws MojoExecutionException 
	 */
	@Test
	public void testExecuteWithNewDevVersion() throws MojoExecutionException, MojoFailureException {
		jiraVersionMojo.setDevelopmentVersion("v4.0");

		EasyMock.expect(
		        jiraStub.getProjectClient().getProject(jiraVersionMojo.getJiraProjectKey()).claim().getVersions()
		     ).andReturn(VERSIONS).once();

		// Add a new version
		EasyMock.expect(
		        jiraStub.getVersionRestClient().createVersion(new VersionInput(jiraVersionMojo.getJiraProjectKey(),
								jiraVersionMojo.getDevelopmentVersion(), null,
								null, false, false)).claim()).andReturn(versionArray[0]);

		// Habilita o controle para inicio dos testes
		EasyMock.replay(jiraStub);

		jiraVersionMojo.execute();
	}

	@Test
	public void testExecuteWithNewDevVersionIncludingQualifierAndSnapshot() throws MojoExecutionException, MojoFailureException {
		jiraVersionMojo.setDevelopmentVersion("5.0-beta-2-SNAPSHOT");

		// Chama o getVersions
		EasyMock.expect(
				jiraStub.getProjectClient().getProject(jiraVersionMojo.getJiraProjectKey()).claim().getVersions()).andReturn(VERSIONS)
				.once();

		// Adiciona a nova versao
		EasyMock.expect(
				jiraStub.getVersionRestClient().createVersion(new VersionInput(jiraVersionMojo.getJiraProjectKey(),
								"5.0 Beta 2", null, null, false, false)).claim())
				.andReturn(versionArray[0]);

		// Habilita o controle para inicio dos testes
		EasyMock.replay(jiraStub);

		jiraVersionMojo.execute();
	}

	@Test
	public void testExecuteWithNewDevVersionAndUseFinalNameForVersionSetToTrue() throws MojoExecutionException, MojoFailureException {
		jiraVersionMojo.setDevelopmentVersion("5.0-beta-2-SNAPSHOT");
		jiraVersionMojo.setFinalNameUsedForVersion(true);
		jiraVersionMojo.setFinalName("my-component-5.0-beta-2-SNAPSHOT");

		// Chama o getVersions
		EasyMock.expect(
				jiraStub.getProjectClient().getProject(jiraVersionMojo.getJiraProjectKey()).claim().getVersions()).andReturn(VERSIONS)
				.once();

		// Adiciona a nova versao
		EasyMock.expect(
				jiraStub.getVersionRestClient().createVersion(new VersionInput(jiraVersionMojo.getJiraProjectKey(),
                        "My Component 5.0 Beta 2", null, null, false,
								false)).claim()).andReturn(versionArray[0]);
		// Habilita o controle para inicio dos testes
		EasyMock.replay(jiraStub);

		jiraVersionMojo.execute();
	}

	/**
	 * Test method for {@link ReleaseVersionMojo#execute()}
	 * @throws MojoFailureException 
	 * @throws MojoExecutionException 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecuteWithExistentDevVersion() throws MojoExecutionException, MojoFailureException {
		jiraVersionMojo.setDevelopmentVersion("2.0");
		// Chama o getVersions
		EasyMock.expect(
				jiraStub.getProjectClient().getProject(jiraVersionMojo.getJiraProjectKey()).claim().getVersions()).andReturn(VERSIONS)
				.once();

		// Habilita o controle para inicio dos testes
		EasyMock.replay(jiraStub);

		jiraVersionMojo.execute();
	}

	@After
	public void tearDown() {
		this.jiraVersionMojo = null;
	}
}
