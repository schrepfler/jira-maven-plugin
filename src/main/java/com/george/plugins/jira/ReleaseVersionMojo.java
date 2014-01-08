package com.george.plugins.jira;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

import com.atlassian.jira.rest.client.ProjectRestClient;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.Version;
import com.google.common.collect.Lists;

/**
 * Goal that creates a version in a JIRA project . NOTE: SOAP access must be
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
	 * @parameter expression="${releaseVersion}"
	 *            default-value="${project.version}"
	 */
	String releaseVersion;

	/**
	 * Auto Discover latest release and release it.
	 * 
	 * @parameter expression="${autoDiscoverLatestRelease}" default-value="true"
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
	public void doExecute(ProjectRestClient projectRestClient, String loginToken)
			throws Exception {
		Log log = getLog();
		log.debug("Login Token returned: " + loginToken);
		Project project = projectRestClient.getProject(jiraProjectKey).claim();
		Iterable<Version> versions = project.getVersions();
		String thisReleaseVersion = (autoDiscoverLatestRelease) ? calculateLatestReleaseVersion(versions)
				: releaseVersion;
		if (thisReleaseVersion != null) {
			log.info("Releasing Version " + this.releaseVersion);
			markVersionAsReleased(projectRestClient, loginToken, versions,
					thisReleaseVersion);
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
	 * Release Version
	 * 
	 * @param log
	 * @param jiraService
	 * @param loginToken
	 * @throws RemoteException
	 * @throws RemotePermissionException
	 * @throws RemoteAuthenticationException
	 * @throws com.atlassian.jira.rpc.soap.client.RemoteException
	 */
	Version markVersionAsReleased(JiraSoapService jiraService,
			String loginToken, RemoteVersion[] versions, String releaseVersion)
			throws RemoteException, RemotePermissionException,
			RemoteAuthenticationException,
			com.atlassian.jira.rpc.soap.client.RemoteException {
		Version ret = null;
		if (versions != null) {
			for (Version remoteReleasedVersion : versions) {
				if (releaseVersion.equalsIgnoreCase(remoteReleasedVersion
						.getName()) && !remoteReleasedVersion.isReleased()) {
					// Mark as released
					remoteReleasedVersion.setReleased(true);
					remoteReleasedVersion
							.setReleaseDate(Calendar.getInstance());
					jiraService.releaseVersion(loginToken, jiraProjectKey,
							remoteReleasedVersion);
					getLog().info(
							"Version " + remoteReleasedVersion.getName()
									+ " was released in JIRA.");
					ret = remoteReleasedVersion;
					break;
				}
			}
		}
		return ret;
	}
}
