package net.sigmalab.maven.plugin.jira;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

import org.apache.maven.plugin.MojoExecutionException;

public final class JiraConnectionConfig {
    private final URI jiraURL;
    private final String projectKey;
    private final JiraCredentials credentials;

    private JiraConnectionConfig(URI jiraURL, String projectKey, JiraCredentials credentials) {
        this.jiraURL = Objects.requireNonNull(jiraURL, "JIRA URL is required");
        this.projectKey = Objects.requireNonNull(projectKey, "JIRA Project Key is required");
        this.credentials = Objects.requireNonNull(credentials, "JIRA Credentials are required");
        validate();
    }

    public static JiraConnectionConfig of(String jiraURL, String projectKey, JiraCredentials credentials) throws MojoExecutionException {
        try {
            // Validate URL is not null or empty
            String trimmedURL = Objects.requireNonNull(jiraURL, "JIRA URL is required").trim();
            if (trimmedURL.isEmpty()) {
                throw new IllegalArgumentException("JIRA URL is required");
            }
            URI uri = new URI(trimmedURL);
            String key = deriveProjectKey(uri, projectKey);
            return new JiraConnectionConfig(uri, key, credentials);
        } catch (URISyntaxException e) {
            throw new MojoExecutionException("Invalid JIRA URL format: " + jiraURL, e);
        }
    }

    private static String deriveProjectKey(URI jiraURL, String explicitProjectKey) {
        if (explicitProjectKey != null && !explicitProjectKey.trim().isEmpty()) {
            return explicitProjectKey.trim();
        }
        // Could add logic to extract from URL if needed
        return "";
    }

    private void validate() {
        if (projectKey.trim().isEmpty()) {
            throw new IllegalArgumentException("JIRA Project Key is required");
        }
    }

    public URI getJiraURL() { return jiraURL; }
    public String getProjectKey() { return projectKey; }
    public JiraCredentials getCredentials() { return credentials; }
}
