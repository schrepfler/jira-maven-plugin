# Jira Maven Plugin Parameter Validation

This document describes the parameter validation implemented in the Jira Maven Plugin.

## Overview

The Jira Maven Plugin performs validation of all parameters to ensure they are properly set before executing operations against a Jira instance. This validation helps prevent runtime errors and provides clear error messages when required parameters are missing or invalid.

## Common Parameter Validation

The following parameters are validated in all Mojos through the `AbstractJiraMojo` class:

| Parameter | Validation | Error Message |
|-----------|------------|---------------|
| `jiraURL` | Not null or empty | "JIRA URL is required. Please set jiraURL parameter." |
| `jiraProjectKey` | Not null or empty | "JIRA Project Key is required. Please set jiraProjectKey parameter." |
| `jiraUsername` | Not null or empty | "JIRA Username is required. Please set jiraUsername parameter." |
| `jiraPassword` | Not null or empty when PAT not provided | "JIRA Password is required when not using Personal Access Token. Please set jiraPassword parameter." |
| `jiraPersonalAccessToken` | Not used together with password | "Both password and Personal Access Token provided. Use only one authentication method." |

## Authentication Validation

The plugin supports two authentication methods:

1. **Username and Password**: Traditional basic authentication
2. **Username and Personal Access Token (PAT)**: Token-based authentication

The validation ensures:
- Only one authentication method is used at a time
- Username is always provided, even when using PAT
- Either password or PAT is provided

## Mojo-Specific Validation

### CreateNewVersionMojo

| Parameter | Validation | Error Message |
|-----------|------------|---------------|
| `developmentVersion` | Not null or empty | "Development version is required. Please set developmentVersion parameter." |
| `finalName` | Not null or empty when `finalNameUsedForVersion` is true | "Final name is required when finalNameUsedForVersion is true. Please set finalName parameter." |

### ReleaseVersionMojo

| Parameter | Validation | Error Message |
|-----------|------------|---------------|
| `releaseVersion` | Not null or empty when `autoDiscoverLatestRelease` is false | "Release version is required when autoDiscoverLatestRelease is false. Please set releaseVersion parameter." |
| Version existence | Checks if the specified version exists | "Version '[version]' not found in JIRA. Please check the version exists." |
| Unreleased version | Checks if there's an unreleased version when auto-discovering | "No unreleased version found to release. Please create a version first." |

### GenerateReleaseNotesMojo

| Parameter | Validation | Error Message |
|-----------|------------|---------------|
| `releaseVersion` | Not null or empty | "Release version is required. Please set releaseVersion parameter." |
| `jqlTemplate` | Not null or empty | "JQL template is required. Please set jqlTemplate parameter." |
| `maxIssues` | Greater than zero | "Maximum number of issues must be greater than zero. Please set a valid maxIssues parameter." |
| `targetFile` | Not null | "Target file is required. Please set targetFile parameter." |
| `format` | Not null or empty | "Format is required. Please set format parameter." |
| Output file | Checks if target file is a directory | "Target release note filename already exists and is a directory" |

## Testing

Parameter validation is tested in the `ParameterValidationTest` class, which verifies that appropriate exceptions are thrown when required parameters are missing or invalid.

## Best Practices

When using the Jira Maven Plugin:

1. Always provide all required parameters
2. Use only one authentication method (either password or PAT)
3. Check error messages carefully when validation fails
4. Set parameters in your POM file or through Maven properties