package com.bskyb.cbs.sdlc.maven.plugin.jira;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
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

    /**
     * @parameter default-value = "${settings}", readonly = true
     */
    Settings settings;

    /**
     * @component role-hint="mng-4384"
     * @required
     */
    private SecDispatcher securityDispatcher;

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
     * @parameter default-value="${scmUsername}"
     */
    protected String jiraUser;

    /**
     * JIRA Authentication Password.
     * 
     * @parameter default-value="${scmPassword}"
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

    transient private JiraRestClient jiraRestClient;

    /**
     * Load username password from settings if user has not set them in JVM
     * properties
     */
    void loadUserInfoFromSettings() {
        if ( settingsKey == null ) {
            settingsKey = jiraURL;
        }

        /*
         * If we haven't been supplied with a <jiraProjectKey> configuration
         * parameter then use the settingsKey parameter to figure out the key
         * for the project.
         */
        if ( getJiraProjectKey() == null ) {
            setJiraProjectKey(jiraURL.substring(jiraURL.lastIndexOf("/browse/") + 8));
            setJiraProjectKey(getJiraProjectKey().replaceAll("/", ""));
        }

        if ( (jiraUser == null || jiraPassword == null) && (settings != null) ) {
            Server server = settings.getServer(this.settingsKey);

            if ( server != null ) {
                if ( jiraUser == null ) {
                    jiraUser = server.getUsername();
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
        if ( isSkip() ) {
            log.info("Skipping Plugin execution.");
            return;
        }
        try {
            final JiraRestClientFactory jiraRestClientFactory = new AsynchronousJiraRestClientFactory();

            loadUserInfoFromSettings();
            log.debug("JIRA URL    == [" + jiraURL + "]");
            
            log.debug("JIRA user   == [" + jiraUser + "]");
            log.debug("Project key == [" + getJiraProjectKey() + "]");

            if ( jiraRestClient == null ) {
                jiraRestClient = jiraRestClientFactory.createWithBasicHttpAuthentication(computeRootURI(jiraURL), jiraUser, jiraPassword);
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
            // XXX: Por enquanto nao faz nada.
        }
    }

    private URI computeRootURI(String url) throws URISyntaxException {
        String rootURL = url.substring(0, Math.min(url.length(), url.lastIndexOf("/browse/")));
        
        return new URI(rootURL);
    }

    public abstract void doExecute(JiraRestClient restClient) throws Exception;

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

    public void setJiraRestClient(JiraRestClient jiraRestClient) {
        this.jiraRestClient = jiraRestClient;
    }

}