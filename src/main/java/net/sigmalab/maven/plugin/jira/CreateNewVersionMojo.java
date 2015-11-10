package net.sigmalab.maven.plugin.jira;

import org.apache.maven.plugin.logging.Log;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.VersionRestClient;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.input.VersionInput;

/**
 * Goal that creates a version in a JIRA project . NOTE: REST API access must be
 * enabled in your JIRA installation. Check JIRA docs for more info.
 * 
 * @goal create-new-jira-version
 * @phase deploy
 * 
 * @author George Gastaldi
 * @author Srdan Srepfler
 * @author dgrierso
 */
public class CreateNewVersionMojo extends AbstractJiraMojo {

	/**
	 * Next Development Version
	 * 
	 * @parameter default-value="${project.version}"
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
	 * @parameter
	 */
	boolean finalNameUsedForVersion;

	/**
     * @parameter default-value="${project.name}"
     */
    private String versionDescription;
    
    
    @Override
    public void doExecute(JiraRestClient restClient){
        Log log = getLog();
        
        String newVersionName = computeVersionName();
        log.debug(String.format("Name of version to be created == [%s]", newVersionName));

        // Get a list of the existing versions in the project. 
        Project project = restClient.getProjectClient().getProject(jiraProjectKey).claim();
        VersionRestClient versionRestClient = restClient.getVersionRestClient();

        Iterable<Version> projectVersions = project.getVersions();
        
        if ( versionAlreadyExists(projectVersions, newVersionName) ) {
            log.warn(String.format("Version %s already exists. Nothing to do.", newVersionName));
            return;
        }
        
        VersionInput newVersion = VersionInput.create(jiraProjectKey, newVersionName, versionDescription, null, false, false);
        log.debug(String.format("New version description: [%s]", newVersion.getDescription()));
    
        versionRestClient.createVersion(newVersion);
        log.info(String.format("Version created in JIRA for project key [%s] : %s", jiraProjectKey, newVersion.getName()));
    }

	/**
	 * Check if version is already present on array
	 * 
	 * @param remoteVersions
	 * @param newDevVersion
	 * @return
	 */
	boolean versionAlreadyExists(Iterable<Version> remoteVersions, String newDevVersion) {
		if (remoteVersions != null) {
			for (Version remoteVersion : remoteVersions) {
				if (remoteVersion.getName().equalsIgnoreCase(newDevVersion)) {
                    return true;
				}
			}
		}
		return false;
	}
	
	/**
     * Compute the name of the version to be created based upon the settings provided.
     * 
     * @return
     */
    private String computeVersionName() {
        String name = ( finalNameUsedForVersion == false ? developmentVersion : finalName );
                
        // Remove the -SNAPSHOT suffix from the version name
        name = name.replace("-SNAPSHOT", "");
        
        return name;
    }
}
