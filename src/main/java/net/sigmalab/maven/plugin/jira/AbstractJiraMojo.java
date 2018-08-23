package net.sigmalab.maven.plugin.jira;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

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

    /**
     * @parameter default-value = "${settings}", readonly = true
     */
    private Settings settings;

    /**
     * @component role-hint="mng-4384"
     * @required
     */
    private SecDispatcher securityDispatcher;

    /**
     * @parameter default-value = "${session}", readonly = true
     * @required
     */
    private MavenSession mavenSession;

    /**
     * The current Maven project.
     * @parameter default-value = "${project}", readonly = true
     * @required
     */
    protected MavenProject project;

    /**
     * Server's id in settings.xml to look up username and password.
     * 
     * @parameter
     */
    private String settingsKey;

    /**
     * JIRA Installation URL. If not informed, it will use the
     * project.issueManagement.url info.
     * 
     * @parameter default-value="${project.issueManagement.url}"
     * @required
     */
    protected String jiraURL;

    /**
     * JIRA Authentication User.
     * 
     * @parameter
     */
    protected String jiraUsername;

    /**
     * JIRA Authentication Password.
     * 
     * @parameter
     */
    protected String jiraPassword;

    /**
     * JIRA Project Key.
     * 
     * @parameter
     */
    private String jiraProjectKey;

    /**
     * Returns if this plugin is enabled for this context
     * 
     * @parameter property="skip"
     */
    protected boolean skip;

    /**
     * Indicate when to actually execute the goal.
     * <ul>
     * <li>project: always (the default)</li>
     * <li>session: only for the last project of the reactor</li>
     * </ul>
     * 
     * @parameter default-value="project"
     */
    protected String scope;

    private JiraRestClient jiraRestClient;

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
            setJiraProjectKey(jiraURL.substring(jiraURL.lastIndexOf(JIRA_ISSUE_URL_PREFIX) + JIRA_ISSUE_URL_PREFIX.length()));
            setJiraProjectKey(getJiraProjectKey().replaceAll("/", ""));
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
            }
        }
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
            log.debug("Project key == [" + getJiraProjectKey() + "]");

            if ( jiraRestClient == null ) {
                jiraRestClient = jiraRestClientFactory.createWithBasicHttpAuthentication(computeRootURI(jiraURL), jiraUsername, jiraPassword);
            }

            try {
                log.debug("Starting execution ...");

                doExecute(jiraRestClient);
            }
            finally {
                log.debug("All done!");
            }
        }
        catch ( Exception e ) {
            log.error("Error when executing mojo", e);
            // Nothing further to do - perhaps print some more useful error message?
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
        catch ( SecDispatcherException e ) {
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