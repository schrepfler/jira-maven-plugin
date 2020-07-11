package net.sigmalab.maven.plugin.jira;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.VersionInput;

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
    private String developmentVersion;

    /**
     * @parameter default-value="${project.build.finalName}"
     */
    private String finalName;

    /**
     * Whether the final name is to be used for the version; defaults to false.
     * 
     * @parameter
     */
    private boolean finalNameUsedForVersion;

    /**
     * @parameter default-value="${project.name}"
     */
    private String versionDescription;

    @Override
    public void doExecute(JiraRestClient restClient) throws MojoFailureException {
        Log log = getLog();

        String newVersionName = computeVersionName();
        log.debug(String.format("Name of version to be created == [%s]", newVersionName));

        ProjectRestClient projectRestClient = restClient.getProjectClient();
        VersionRestClient versionRestClient = restClient.getVersionRestClient();
        
        Project project = projectRestClient.getProject(getJiraProjectKey()).claim();

        Iterable<Version> projectVersions = project.getVersions();

        if ( versionAlreadyExists(projectVersions, newVersionName) ) {
            log.warn(String.format("Version %s already exists in %s. Nothing to do.", newVersionName, getSettingsKey()));
            return;
        }

        VersionInput newVersion = VersionInput.create(getJiraProjectKey(), newVersionName, versionDescription, null, false, false);
        log.debug(String.format("New version description: [%s]", newVersion.getDescription()));

        Version created = versionRestClient.createVersion(newVersion).claim();
        log.info(String.format("Version created in %s for project key [%s] : %s", getSettingsKey(), getJiraProjectKey(), created.getName()));
    }

    /**
     * Check if version is already present on array
     * 
     * @param remoteVersions
     * @param newDevVersion
     * @return
     */
    private boolean versionAlreadyExists(Iterable<Version> remoteVersions, String newDevVersion) {
        if ( remoteVersions != null ) {
            for ( Version remoteVersion : remoteVersions ) {
                if ( remoteVersion.getName().equalsIgnoreCase(newDevVersion) ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compute the name of the version to be created based upon the settings
     * provided.
     * 
     * @return
     */
    private String computeVersionName() {
        String name = ( isFinalNameUsedForVersion() ? finalName : developmentVersion );

        // Remove the -SNAPSHOT suffix from the version name
        name = name.replace("-SNAPSHOT", "");

        return name;
    }

    public String getDevelopmentVersion() {
        return developmentVersion;
    }

    public void setDevelopmentVersion(String developmentVersion) {
        this.developmentVersion = developmentVersion;
    }

    public String getFinalName() {
        return finalName;
    }

    public void setFinalName(String finalName) {
        this.finalName = finalName;
    }

    public String getVersionDescription() {
        return versionDescription;
    }

    public void setVersionDescription(String versionDescription) {
        this.versionDescription = versionDescription;
    }

    /**
     * @return the finalNameUsedForVersion
     */
    public boolean isFinalNameUsedForVersion() {
        return finalNameUsedForVersion;
    }

    /**
     * @param finalNameUsedForVersion the finalNameUsedForVersion to set
     */
    public void setFinalNameUsedForVersion(boolean finalNameUsedForVersion) {
        this.finalNameUsedForVersion = finalNameUsedForVersion;
    }
}
