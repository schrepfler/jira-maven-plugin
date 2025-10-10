# Unit Tests

This directory contains unit tests for the Jira Maven Plugin.

## Test Structure

### ParameterValidationTest
Validates parameter requirements and authentication logic for all Mojos.

**Test Categories:**
- **Basic Parameter Validation**: Tests for missing required parameters (URL, username, project key, password)
- **Edge Cases**: Tests for empty strings vs null values
- **Authentication**: Tests for password vs PAT authentication and mutual exclusivity
- **Mojo-Specific Validation**: Tests for mojo-specific parameter requirements

### TestAbstractJiraMojo
Test harness class that extends `AbstractJiraMojo` to expose the validation logic for testing.

## Exception Types

The tests verify two different exception types based on where validation occurs:

- **MojoExecutionException**: Thrown during parameter validation in the `execute()` method (common parameters)
- **MojoFailureException**: Thrown during mojo-specific validation in the `doExecute()` method

## Test Patterns

### Helper Methods
The tests use helper methods for better assertions:
```java
assertMojoExecutionExceptionWithMessage(Exception e, String expectedMessageFragment)
```
This provides better error messages when assertions fail.

### Mock Setup
Tests use Mockito to mock `JiraRestClient` to avoid actual REST API calls during unit tests.

## Running Unit Tests

### Run All Unit Tests
```bash
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=ParameterValidationTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=ParameterValidationTest#testMissingJiraUrl
```

## Test Coverage

Current test coverage includes:
- ✅ Missing required parameters (URL, username, project key, password)
- ✅ Empty string parameters
- ✅ Both authentication methods provided simultaneously
- ✅ PAT authentication without password
- ✅ Mojo-specific parameter validation
- ✅ Final name requirement when flag is enabled

## Best Practices

1. **Always use helper methods** for assertions to get better error messages
2. **Mock only what's needed** - don't create unnecessary mocks
3. **Test edge cases** - empty strings, null values, invalid combinations
4. **Document why** - explain why different exception types are expected
5. **Keep tests focused** - each test should verify one specific behavior

## Adding New Tests

When adding new tests:

1. Follow the existing naming convention: `test<Scenario>`
2. Add JavaDoc comments explaining what the test validates
3. Use the `assertMojoExecutionExceptionWithMessage` helper for parameter validation tests
4. Consider both null and empty string cases for string parameters
5. Update this README if adding new test categories
