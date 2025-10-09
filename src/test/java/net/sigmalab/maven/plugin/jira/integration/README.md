# Jira Maven Plugin Integration Tests

This package contains integration tests for the Jira Maven Plugin that execute against a real Jira Cloud instance.

## Test Structure

- `AbstractJiraIntegrationTest`: Base class for all integration tests with common setup and cleanup methods
- `CreateNewVersionMojoIntegrationTest`: Tests for creating new versions in Jira
- `ReleaseVersionMojoIntegrationTest`: Tests for releasing versions in Jira
- `GenerateReleaseNotesMojoIntegrationTest`: Tests for generating release notes from Jira issues
- `PasswordAuthenticationIT`: Tests specifically for password-based authentication
- `PersonalAccessTokenAuthenticationIT`: Tests specifically for PAT-based authentication

## Running Integration Tests

To run the integration tests, you need to provide connection details for a Jira Cloud instance:

```bash
mvn clean verify -Pintegration-tests \
  -Djira.url=https://your-instance.atlassian.net \
  -Djira.username=your-username \
  -Djira.pat=your-personal-access-token \
  -Djira.projectKey=TEST
```

Alternatively, you can set these as environment variables:

```bash
export JIRA_URL=https://your-instance.atlassian.net
export JIRA_USERNAME=your-username
export JIRA_PAT=your-personal-access-token
export JIRA_PROJECT_KEY=TEST
mvn clean verify -Pintegration-tests
```

## Authentication Options

The integration tests support two authentication methods, which are mutually exclusive (you must use one or the other, not both):

1. Username and Personal Access Token (recommended for Jira Cloud):
   ```
   -Djira.username=your-username -Djira.pat=your-personal-access-token
   ```

2. Username and Password (for Jira Server):
   ```
   -Djira.username=your-username -Djira.password=your-password
   ```

If both authentication methods are provided, the tests will fail with an error message indicating that only one method should be used.

## Best Practices

1. Use a dedicated test project in your Jira instance
2. The tests automatically clean up after themselves, but manual cleanup may be needed if tests fail
3. All test data is prefixed with "IntegrationTest-" for easy identification
4. For CI/CD environments, use secrets management to store credentials