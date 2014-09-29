package net.sigmalab.maven.plugin.jira;

import java.util.Comparator;

import com.atlassian.jira.rest.client.VersionRestClient;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.input.VersionInput;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import org.joda.time.DateTime;

/**
 * Goal that creates a version in a JIRA project . NOTE: REST API access must be
 * enabled in your JIRA installation. Check JIRA docs for more info.
 * 
 * @goal create-new-jira-version
 * @phase deploy
 * 
 * @author George Gastaldi
 * @author Srdan Srepfler
 */
public class CreateNewVersionMojo extends AbstractJiraMojo {

	/**
	 * Next Development Version
	 * 
	 * @parameter expression="${developmentVersion}"
	 *            default-value="${project.version}"
	 * @required
	 */
	String developmentVersion;

	/**
	 * @parameter default-value="${project.build.finalName}"
	 */
	String finalName;

	/**
	 * Whether the final name is to be used for the version; defaults to false.
	 * 
	 * @parameter expression="${finalNameUsedForVersion}"
	 */
	boolean finalNameUsedForVersion;


    @Override
    public void doExecute(){
        Log log = getLog();
        Project project = jiraRestClient.getProjectClient().getProject(jiraProjectKey).claim();
        VersionRestClient versionRestClient = jiraRestClient.getVersionRestClient();

        Iterable<Version> projectVersions = project.getVersions();

        String newDevVersion;
        if (finalNameUsedForVersion) {
            newDevVersion = finalName;
        } else {
            newDevVersion = developmentVersion;
        }

        newDevVersion = StringUtils.capitaliseAllWords(newDevVersion.replace("-SNAPSHOT", "").replace("-", " "));
        boolean versionExists = isVersionAlreadyPresent(projectVersions, newDevVersion);
        if (!versionExists){
            log.debug("New Development version in JIRA is: " + newDevVersion);
            VersionInput newVersionInput = VersionInput.create(jiraProjectKey, newDevVersion, null, new DateTime(), false, false);
            Version createdNewVersion = versionRestClient.createVersion(newVersionInput).claim();
            log.info("Version created in JIRA for project key "
					+ jiraProjectKey + " : " + createdNewVersion);

            // TODO handle version already created in JIRA, nothing to do.
            // Example of usage https://bitbucket.org/atlassian/jira-rest-java-client/src/454723e7ffb54c1a10a1be5f64e9d052566d8496/test/src/test/java/it/AsynchronousVersionRestClientTest.java?at=master
        }

    }

	/**
	 * Check if version is already present on array
	 * 
	 * @param remoteVersions
	 * @param newDevVersion
	 * @return
	 */
	boolean isVersionAlreadyPresent(Iterable<Version> remoteVersions, String newDevVersion) {
		boolean versionExists = false;
		if (remoteVersions != null) {
			// Creating new Version (if not already created)
			for (Version remoteVersion : remoteVersions) {
				if (remoteVersion.getName().equalsIgnoreCase(newDevVersion)) {
					versionExists = true;
					break;
				}
			}
		}
		// existant
		return versionExists;
	}
}
