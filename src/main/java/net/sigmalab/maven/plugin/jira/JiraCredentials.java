package net.sigmalab.maven.plugin.jira;

import java.util.Objects;
import java.util.Optional;

public final class JiraCredentials {
    private final String username;
    private final Optional<String> password;
    private final Optional<String> personalAccessToken;

    private JiraCredentials(String username, Optional<String> password, Optional<String> personalAccessToken) {
        this.username = Objects.requireNonNull(username, "JIRA Username is required");
        this.password = password;
        this.personalAccessToken = personalAccessToken;
        validate();
    }

    public static JiraCredentials of(String username, String password, String personalAccessToken) {
        return new JiraCredentials(
            username,
            Optional.ofNullable(password).filter(s -> !s.trim().isEmpty()),
            Optional.ofNullable(personalAccessToken).filter(s -> !s.trim().isEmpty())
        );
    }

    private void validate() {
        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("JIRA Username is required");
        }
        boolean hasPassword = password.isPresent();
        boolean hasPAT = personalAccessToken.isPresent();
        if (!hasPassword && !hasPAT) {
            throw new IllegalArgumentException("JIRA Password is required when no Personal Access Token is provided");
        }
        if (hasPassword && hasPAT) {
            throw new IllegalArgumentException("Both password and Personal Access Token provided. Please use only one authentication method");
        }
    }

    public String getUsername() { return username; }
    public Optional<String> getPassword() { return password; }
    public Optional<String> getPersonalAccessToken() { return personalAccessToken; }
}

