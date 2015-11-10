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
	String releaseVersion;

	/**
	 * Auto Discover latest release and release it.
	 * 
     * @parameter default-value="true"
	 */
	boolean autoDiscoverLatestRelease;

	/**
	 * Comparator for discovering the latest release
	 * 
	 * @parameter implementation="com.github.gastaldi.jira.VersionComparator"
	 */
	Comparator<Version> versionComparator = new VersionComparator();

	@Override
	public void doExecute(JiraRestClient jiraRestClient) throws Exception {
	    Iterable<Version> versions = getProjectVersions(jiraRestClient);
        Version thisReleaseVersion = ( autoDiscoverLatestRelease ) ?
                                        calculateLatestReleaseVersion(versions) : getVersion(jiraRestClient, releaseVersion);
        
        if ( thisReleaseVersion != null ) {
            log.info("Releasing Version " + this.releaseVersion);
            
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
        return restClient.getProjectClient().getProject(jiraProjectKey).claim().getVersions();
    }
    
    /**
     * Returns the latest unreleased version
     * 
     * @param versions
     * @return
     */
    Version calculateLatestReleaseVersion(Iterable<Version> versions) {
        for (Version version : versions) {
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
	boolean isVersionAlreadyPresent(Version[] versions, String newDevVersion) {
		boolean versionExists = false;
		if (versions != null) {
			// Creating new Version (if not already created)
			for (Version remoteVersion : versions) {
				if (remoteVersion.getName().equalsIgnoreCase(newDevVersion)) {
					versionExists = true;
					break;
				}
			}
		}
		// existant
		return versionExists;
	}

	/**
     * 
     * @param restClient
     * @param releaseVersion
     */
    private void markVersionAsReleased(JiraRestClient restClient, Version releaseVersion) {
        VersionInputBuilder vib = new VersionInputBuilder(jiraProjectKey, releaseVersion);
        
        vib.setReleased(true);
        vib.setReleaseDate(new DateTime());
        
        restClient.getVersionRestClient().updateVersion(releaseVersion.getSelf(), vib.build());
        
        getLog().info("Version " + releaseVersion.getName() + " was released in JIRA.");
    }
}
