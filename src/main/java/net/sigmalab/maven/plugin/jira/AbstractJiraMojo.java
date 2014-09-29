package net.sigmalab.maven.plugin.jira;

import java.net.URI;

import com.atlassian.jira.rest.client.domain.User;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

/**
 * This class allows the use of {@link JiraRestClient} in JIRA Actions
 * 
 * @author george
 * @author schrepfler
 * 
 */
public abstract class AbstractJiraMojo extends AbstractMojo {

	/**
	 * @parameter property="settings"
	 */
	Settings settings;

	/**
	 * Server's id in settings.xml to look up username and password.
	 * 
	 * @parameter property="settingsKey"
	 */
	private String settingsKey;

	/**
	 * JIRA Installation URL. If not informed, it will use the
	 * project.issueManagement.url info.
	 * 
	 * @parameter property="jiraURL"
	 *            default-value="${project.issueManagement.url}"
	 * @required
	 */
	protected String jiraURL;

	/**
	 * JIRA Authentication User.
	 * 
	 * @parameter property="jiraUser" default-value="${scmUsername}"
	 */
	protected String jiraUser;

	/**
	 * JIRA Authentication Password.
	 * 
	 * @parameter property="jiraPassword" default-value="${scmPassword}"
	 */
	protected String jiraPassword;

	/**
	 * JIRA Project Key.
	 * 
	 * @parameter property="jiraProjectKey"
	 */
	protected String jiraProjectKey;

	transient JiraRestClient jiraRestClient;

	/**
	 * Returns if this plugin is enabled for this context
	 * 
	 * @parameter property="skip"
	 */
	protected boolean skip;
    protected User userClient;


	/**
	 * Load username password from settings if user has not set them in JVM
	 * properties
	 */
	void loadUserInfoFromSettings() {
		if (settingsKey == null) {
			settingsKey = jiraURL;
		}
		if ((jiraUser == null || jiraPassword == null) && (settings != null)) {
			Server server = settings.getServer(this.settingsKey);

			if (server != null) {
				if (jiraUser == null) {
					jiraUser = server.getUsername();
				}

				if (jiraPassword == null) {
					jiraPassword = server.getPassword();
				}
			}
		}
	}

	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		Log log = getLog();
		if (isSkip()) {
			log.info("Skipping Plugin execution.");
			return;
		}
		try {
			
			loadUserInfoFromSettings();
			
			JiraRestClientFactory jiraRestClientFactory = new AsynchronousJiraRestClientFactory();
			JiraRestClient jiraRestClient = jiraRestClientFactory.create(URI.create(jiraURL), new BasicHttpAuthenticationHandler(jiraUser, jiraPassword));

			log.debug("Logging in JIRA");
			userClient = jiraRestClient.getUserClient().getUser(jiraUser).claim();
			log.debug("Logged in JIRA");
			
			try {
				doExecute();
			} finally {
				log.debug("Logging out from JIRA");
				// TODO How to logout? Is it needed?
				log.debug("Logged out from JIRA");
			}
		} catch (Exception e) {
			log.error("Error when executing mojo", e);
			// XXX: Por enquanto nao faz nada.
		}
	}

	public abstract void doExecute() throws Exception;

	public boolean isSkip() {
		return skip;
	}

	public void setJiraProjectKey(String jiraProjectKey) {
		this.jiraProjectKey = jiraProjectKey;
	}

	public void setJiraPassword(String jiraPassword) {
		this.jiraPassword = jiraPassword;
	}

	public void setJiraURL(String jiraURL) {
		this.jiraURL = jiraURL;
	}

	public void setJiraUser(String jiraUser) {
		this.jiraUser = jiraUser;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public void setSettingsKey(String settingsKey) {
		this.settingsKey = settingsKey;
	}

}