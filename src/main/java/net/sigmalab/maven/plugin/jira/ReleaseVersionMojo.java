package net.sigmalab.maven.plugin.jira;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.input.VersionInput;
import com.google.common.collect.Lists;

/**
 * Goal that creates a version in a JIRA project . NOTE: API access must be
 * enabled in your JIRA installation. Check JIRA docs for more info.
 * 
 * @goal release-jira-version
 * @phase deploy
 * 
 * @author George Gastaldi
 */
public class ReleaseVersionMojo extends AbstractJiraMojo {

	/**
	 * Released Version
	 * 
	 * @parameter property="releaseVersion"
	 *            default-value="${project.version}"
	 */
	String releaseVersion;

	/**
	 * Auto Discover latest release and release it.
	 * 
	 * @parameter property="autoDiscoverLatestRelease" default-value="true"
	 */
	boolean autoDiscoverLatestRelease;

	/**
	 * Comparator for discovering the latest release
	 * 
	 * @parameter 
	 *            implementation="com.george.plugins.jira.RemoteVersionComparator"
	 */
	Comparator<Version> versionComparator = new VersionComparator();

	@Override
	public void doExecute() throws Exception {
		Log log = getLog();
		log.debug("Login for: " + jiraRestClient.getSessionClient().getCurrentSession().claim().getUsername());
		Project project = jiraRestClient.getProjectClient().getProject(jiraProjectKey).claim();
		Iterable<Version> versions = project.getVersions();
		String thisReleaseVersion = (autoDiscoverLatestRelease) ? calculateLatestReleaseVersion(versions):releaseVersion;
		if (thisReleaseVersion != null) {
			log.info("Releasing Version " + this.releaseVersion);
			markVersionAsReleased(versions, thisReleaseVersion);
		}
	}

	/**
	 * Returns the latest unreleased version
	 * 
	 * @param versions
	 * @return
	 */
	String calculateLatestReleaseVersion(Iterable<Version> versions) {
		List<Version> versionsList = Lists.newArrayList(versions);
		Collections.sort(versionsList, versionComparator);

		for (Version remoteVersion : versions) {
			if (!remoteVersion.isReleased())
				return remoteVersion.getName();
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
     * Release version
     * @param versions
     * @param releaseVersion
     * @return
     */
	private Version markVersionAsReleased(Iterable<Version> versions, String releaseVersion) {
		Version ret = null;
		if (versions != null) {
			for (Version remoteReleasedVersion : versions) {
				if (releaseVersion.equalsIgnoreCase(remoteReleasedVersion.getName()) && !remoteReleasedVersion.isReleased()) {

                    VersionInput updateVersionInput = VersionInput.create(jiraProjectKey, remoteReleasedVersion.getName(), remoteReleasedVersion.getDescription(), new DateTime(), false, true);

					Version updatedVersion = jiraRestClient.getVersionRestClient().updateVersion(remoteReleasedVersion.getSelf(), updateVersionInput).claim();

					getLog().info("Version " + remoteReleasedVersion.getName() + " was released in JIRA.");
					ret = updatedVersion;
					break;
				}
			}
		}
		return ret;
	}
}
