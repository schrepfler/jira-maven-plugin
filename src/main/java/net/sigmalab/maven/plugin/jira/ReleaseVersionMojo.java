package net.sigmalab.maven.plugin.jira;

import java.util.Comparator;

import org.apache.maven.plugin.logging.Log;
import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.input.VersionInputBuilder;

/**
 * Goal that creates a version in a JIRA project . NOTE: API access must be
 * enabled in your JIRA installation. Check JIRA docs for more info.
 * 
 * @goal release-jira-version
 * @phase deploy
 * 
 * @author George Gastaldi
 * @author dgrierso
 */
public class ReleaseVersionMojo extends AbstractJiraMojo {
    private final Log log = getLog();

    /**
     * Released Version
     * 
     * @parameter default-value="${project.version}"
     */
    private String releaseVersion;

    /**
     * Auto Discover latest release and release it.
     * 
     * @parameter default-value="false"
     */
    boolean autoDiscoverLatestRelease;

    /**
     * Comparator for discovering the latest release
     * 
     * @parameter implementation="com.github.gastaldi.jira.VersionComparator"
     */
    Comparator<Version> versionComparator = new VersionComparator();

    @Override
    public void doExecute(JiraRestClient jiraRestClient) {
        Iterable<Version> versions = getProjectVersions(jiraRestClient);
        Version thisReleaseVersion = (autoDiscoverLatestRelease) ? calculateLatestReleaseVersion(versions)
                                                                 : getVersion(jiraRestClient, getReleaseVersion());

        if ( thisReleaseVersion != null ) {
            log.debug("Releasing Version " + thisReleaseVersion.getName());

            markVersionAsReleased(jiraRestClient, thisReleaseVersion);
        }
    }

    /**
     * 
     * @param restClient
     * @param versionString
     * @return
     */
    private Version getVersion(JiraRestClient restClient, String versionString) {
        Iterable<Version> versions = getProjectVersions(restClient);

        if ( versions != null ) {
            for ( Version version : versions ) {
                if ( version.getName().equalsIgnoreCase(versionString) ) {
                    return version;
                }
            }
        }

        return null;
    }

    /**
     * 
     * @param restClient
     * @return
     */
    private Iterable<Version> getProjectVersions(JiraRestClient restClient) {
        return restClient.getProjectClient().getProject(getJiraProjectKey()).claim().getVersions();
    }

    /**
     * Returns the latest unreleased version
     * 
     * @param versions
     * @return
     */
    public Version calculateLatestReleaseVersion(Iterable<Version> versions) {
        for ( Version version : versions ) {
            if ( version.isReleased() != true )
                return version;
        }

        return null;
    }

    /**
     * Check if version is already present on array
     * 
     * @param versions
     * @param newDevVersion
     * @return
     */
    public boolean isVersionAlreadyPresent(Version[] versions, String newDevVersion) {
        boolean versionExists = false;

        if ( versions != null ) {
            for ( Version remoteVersion : versions ) {
                if ( remoteVersion.getName().equalsIgnoreCase(newDevVersion) ) {
                    versionExists = true;
                    break;
                }
            }
        }

        return versionExists;
    }

    /**
     * 
     * @param restClient
     * @param releaseVersion
     * @return 
     */
    private Version markVersionAsReleased(JiraRestClient restClient, Version releaseVersion) {
        VersionInputBuilder vib = new VersionInputBuilder(getJiraProjectKey(), releaseVersion);
        
        vib.setReleased(true);
        vib.setReleaseDate(new DateTime());

        Version updatedVersion = restClient.getVersionRestClient().updateVersion(releaseVersion.getSelf(), vib.build()).claim();

        getLog().info("Version " + updatedVersion.getName() + " was released in JIRA.");
        
        return updatedVersion;
    }

    /**
     * @return the releaseVersion
     */
    public String getReleaseVersion() {
        return releaseVersion;
    }

    /**
     * @param releaseVersion the releaseVersion to set
     */
    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }
}
