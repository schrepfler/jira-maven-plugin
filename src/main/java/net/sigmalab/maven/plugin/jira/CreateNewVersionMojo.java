package net.sigmalab.maven.plugin.jira;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.VersionInput;

/**
 * Goal that creates a version in a JIRA project. NOTE: REST API access must be
 * enabled in your JIRA installation. Check JIRA docs for more info.
 * 
 * @author George Gastaldi
 * @author Srdan Srepfler
 * @author dgrierso
 */
@Mojo(name = "create-new-jira-version", defaultPhase = LifecyclePhase.DEPLOY)
public class CreateNewVersionMojo extends AbstractJiraMojo {

    /**
     * Next Development Version
     */
    @Parameter(defaultValue = "${project.version}", required = true)
    private String developmentVersion;

    /**
     * Final name of the build artifact
     */
    @Parameter(defaultValue = "${project.build.finalName}")
    private String finalName;

    /**
     * Whether the final name is to be used for the version; defaults to false.
     */
    @Parameter
    private boolean finalNameUsedForVersion;

    /**
     * Description for the version
     */
    @Parameter(defaultValue = "${project.name}")
    private String versionDescription;

    /**
     * Validates the specific parameters for this mojo
     * 
     * @throws MojoFailureException if validation fails
     */
    private void validateMojoParameters() throws MojoFailureException {
        // Validate developmentVersion
        if (developmentVersion == null || developmentVersion.trim().isEmpty()) {
            throw new MojoFailureException("Development version is required. Please set developmentVersion parameter.");
        }
        
        // If finalNameUsedForVersion is true, validate finalName
        if (finalNameUsedForVersion && (finalName == null || finalName.trim().isEmpty())) {
            throw new MojoFailureException("Final name is required when finalNameUsedForVersion is true. Please set finalName parameter.");
        }
    }

    @Override
    public void doExecute(JiraRestClient restClient) throws MojoFailureException {
        // Validate mojo-specific parameters
        validateMojoParameters();
        
        String newVersionName = computeVersionName();
        getLog().debug(String.format("Name of version to be created == [%s]", newVersionName));

        ProjectRestClient projectRestClient = restClient.getProjectClient();
        VersionRestClient versionRestClient = restClient.getVersionRestClient();
        
        Project project = projectRestClient.getProject(getJiraProjectKey()).claim();

        Iterable<Version> projectVersions = project.getVersions();

        if ( versionAlreadyExists(projectVersions, newVersionName) ) {
            getLog().warn(String.format("Version %s already exists in %s. Nothing to do.", newVersionName, getSettingsKey()));
            return;
        }

        VersionInput newVersion = VersionInput.create(getJiraProjectKey(), newVersionName, versionDescription, null, false, false);
        getLog().debug(String.format("New version description: [%s]", newVersion.getDescription()));

        Version created = versionRestClient.createVersion(newVersion).claim();
        getLog().info(String.format("Version created in %s for project key [%s] : %s", getSettingsKey(), getJiraProjectKey(), created.getName()));
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
