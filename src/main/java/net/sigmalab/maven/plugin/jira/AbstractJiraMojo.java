package net.sigmalab.maven.plugin.jira;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.atlassian.jira.rest.client.auth.PersonalAccessTokenAuthenticationHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.utils.xml.Xpp3Dom;
import org.codehaus.plexus.components.secdispatcher.SecDispatcher;
import org.codehaus.plexus.components.secdispatcher.SecDispatcherException;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import javax.inject.Inject;

/**
 * This class allows the use of {@link JiraRestClient} in JIRA Actions
 * 
 * @author george
 * @author schrepfler
 * @author dgrierso
 * 
 */
public abstract class AbstractJiraMojo extends AbstractMojo {

    private static final String JIRA_ISSUE_URL_PREFIX = "/browse/";

    private static final String SCOPE_SESSION = "session";

    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    @Inject()
    private SecDispatcher securityDispatcher;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    /**
     * The current Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     * Server's id in settings.xml to look up username and password.
     **/
    @Parameter
    private String settingsKey;

    /**
     * JIRA Installation URL. If not informed, it will use the
     * project.issueManagement.url info.
     **/
    @Parameter(defaultValue = "${project.issueManagement.url}", required = true)
    protected String jiraURL;

    /**
     * JIRA Authentication User.
     *
     */
    @Parameter
    protected String jiraUsername;

    /**
     * JIRA Authentication Password.
     * 
     */
    @Parameter
    protected String jiraPassword;

    /**
     * JIRA Personal Access Token.
     *
     */
    @Parameter
    protected String jiraPersonalAccessToken;

    /**
     * JIRA Project Key.
     * 
     */
    @Parameter
    private String jiraProjectKey;

    /**
     * Returns if this plugin is enabled for this context
     * 
     */
    @Parameter
    protected boolean skip;

    /**
     * Indicate when to actually execute the goal.
     * <ul>
     * <li>project: always (the default)</li>
     * <li>session: only for the last project of the reactor</li>
     * </ul>
     * 
     */
    @Parameter(defaultValue = "project")
    protected String scope;

    private JiraRestClient jiraRestClient;

    private JiraConnectionConfig connectionConfig;

    /**
     * Load username password from settings if user has not set them in JVM
     * properties
     */
    private void loadUserInfoFromSettings() {
        if ( settingsKey == null ) {
            settingsKey = jiraURL;
        }

        /*
         * If we haven't been supplied with a <jiraProjectKey> configuration
         * parameter then use the settingsKey parameter to figure out the key
         * for the project.
         */
        if ( getJiraProjectKey() == null ) {
            int idx = jiraURL != null ? jiraURL.lastIndexOf(JIRA_ISSUE_URL_PREFIX) : -1;
            if (idx >= 0 && jiraURL.length() > idx + JIRA_ISSUE_URL_PREFIX.length()) {
                String derivedKey = jiraURL.substring(idx + JIRA_ISSUE_URL_PREFIX.length()).replaceAll("/", "");
                if (!derivedKey.trim().isEmpty()) {
                    setJiraProjectKey(derivedKey);
                }
            }
            // If not set, leave as null so validation will fail
        }

        if ( (jiraUsername == null || jiraPassword == null) && (settings != null) ) {
            Server server = settings.getServer(this.settingsKey);

            if ( server != null ) {
                if ( jiraUsername == null ) {
                    jiraUsername = server.getUsername();
                }

                if ( jiraPassword == null ) {
                    jiraPassword = decrypt(server.getPassword(), settingsKey);
                }

                Xpp3Dom configuration = (Xpp3Dom) server.getConfiguration();
                if (configuration != null) {
                    jiraPersonalAccessToken = configuration.getChild("jiraPersonalAccessToken").getValue();
                }
            }
        }
    }

    /**
     * Validates that all required parameters are set and valid using immutable parameter objects
     *
     * @throws MojoExecutionException if validation fails
     */
    private void validateParameters() throws MojoExecutionException {
        try {
            JiraCredentials credentials = JiraCredentials.of(jiraUsername, jiraPassword, jiraPersonalAccessToken);
            this.connectionConfig = JiraConnectionConfig.of(jiraURL, getJiraProjectKey(), credentials);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    public JiraConnectionConfig getConnectionConfig() {
        return connectionConfig;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();

        // Skip property
        if ( isSkip() ) {
            log.info("Skipping Plugin execution.");
            return;
        }

        // Scope property
        if ( SCOPE_SESSION.equals(this.scope) ) {
            List<MavenProject> projects = this.mavenSession.getProjects();

            MavenProject lastProject = projects.get(projects.size() - 1);

            if ( lastProject != this.project ) {
                log.info("Skipping waiting for the last Maven session project.");

                return;
            }
        }

        try {
            final JiraRestClientFactory jiraRestClientFactory = new AsynchronousJiraRestClientFactory();

            loadUserInfoFromSettings();
            log.debug("JIRA URL    == [" + jiraURL + "]");
            log.debug("JIRA user   == [" + jiraUsername + "]");
            log.debug("PAT         == [" + (jiraPersonalAccessToken != null ? "********" : "null") + "]");
            log.debug("Project key == [" + getJiraProjectKey() + "]");
            
            // Validate parameters after loading from settings
            validateParameters();

            if ( jiraRestClient == null ) {
                if (jiraPersonalAccessToken != null) {
                    jiraRestClient = jiraRestClientFactory.createWithAuthenticationHandler(computeRootURI(jiraURL),
                            new PersonalAccessTokenAuthenticationHandler(jiraUsername, jiraPersonalAccessToken));
                } else {
                    jiraRestClient = jiraRestClientFactory.createWithBasicHttpAuthentication(computeRootURI(jiraURL), jiraUsername, jiraPassword);
                }
            }

            try {
                log.debug("Starting execution ...");

                doExecute(jiraRestClient);
            }
            finally {
                log.debug("All done!");
            }
        } catch (MojoExecutionException | MojoFailureException e) {
            log.error("Error when executing mojo", e);
            throw e;
        } catch (Exception e) {
            log.error("Error when executing mojo", e);
            // Only log unexpected exceptions, do not rethrow
        }
    }

    private URI computeRootURI(String url) throws URISyntaxException {
        /*
         *  Test whether the JIRA_ISSUE_URL_PREFIX is missing from the specified URL
         *  in which case just return the URL we've been passed.
         */
        if ( url.lastIndexOf(JIRA_ISSUE_URL_PREFIX) < 0 ) {
            return new URI(url);
        }
        else {
            /*
             * Otherwise, compute the part of the URL in front of the prefix and return that.
             */
            String rootURL = url.substring(0, Math.min(url.length(), url.lastIndexOf(JIRA_ISSUE_URL_PREFIX)));
        
            return new URI(rootURL);
        }
    }

    public abstract void doExecute(JiraRestClient restClient) throws MojoFailureException;

    private String decrypt(String str, String server) {
        try {
            return securityDispatcher.decrypt(str);
        }
        catch ( SecDispatcherException | java.io.IOException e ) {
            getLog().warn("Failed to decrypt password/passphrase for server " + server + ", using auth token as is");
            return str;
        }
    }

    public boolean isSkip() {
        return skip;
    }

    /**
     * @return the jiraProjectKey
     */
    public String getJiraProjectKey() {
        return jiraProjectKey;
    }

    /**
     * @return the settingsKey
     */
    public String getSettingsKey() {
        return settingsKey;
    }

    public String getJiraURL() {
        return jiraURL;
    }

    public void setJiraProjectKey(String jiraProjectKey) {
        this.jiraProjectKey = jiraProjectKey;
    }

    public void setJiraPassword(String jiraPassword) {
        this.jiraPassword = jiraPassword;
    }

    public void setJiraPersonalAccessToken(String jiraPersonalAccessToken) {
        this.jiraPersonalAccessToken = jiraPersonalAccessToken;
    }

    public void setJiraURL(String jiraURL) {
        this.jiraURL = jiraURL;
    }

    public void setJiraUser(String jiraUser) {
        this.jiraUsername = jiraUser;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public void setSettingsKey(String settingsKey) {
        this.settingsKey = settingsKey;
    }

    public void setJiraRestClient(JiraRestClient jiraRestClient) {
        this.jiraRestClient = jiraRestClient;
    }
}
