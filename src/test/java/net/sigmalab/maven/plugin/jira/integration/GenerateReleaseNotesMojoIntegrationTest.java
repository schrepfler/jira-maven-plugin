package net.sigmalab.maven.plugin.jira.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.atlassian.jira.rest.client.api.domain.Version;

import net.sigmalab.maven.plugin.jira.CreateNewVersionMojo;
import net.sigmalab.maven.plugin.jira.GenerateReleaseNotesMojo;
import net.sigmalab.maven.plugin.jira.ReleaseVersionMojo;

@RunWith(JUnit4.class)
public class GenerateReleaseNotesMojoIntegrationTest extends AbstractJiraIntegrationTest {
    
    private static final String TEST_VERSION_PREFIX = "IntegrationTest-Notes-";
    private String versionName;
    private File targetFile;
    
    @Before
    public void createTestVersion() throws Exception {
        // Create a unique version name for this test
        versionName = TEST_VERSION_PREFIX + System.currentTimeMillis();
        
        // Create a version for release notes
        CreateNewVersionMojo createMojo = new CreateNewVersionMojo();
        createMojo.setJiraURL(JIRA_URL);
        createMojo.setJiraUser(JIRA_USERNAME);
        
        if (JIRA_PAT != null) {
            createMojo.setJiraPersonalAccessToken(JIRA_PAT);
        } else {
            createMojo.setJiraPassword(JIRA_PASSWORD);
        }
        
        createMojo.setJiraProjectKey(JIRA_PROJECT_KEY);
        createMojo.setDevelopmentVersion(versionName);
        
        // Execute the mojo to create the version
        createMojo.execute();
        
        // Create a temporary file for the release notes
        targetFile = File.createTempFile("release-notes-", ".txt");
        targetFile.deleteOnExit();
    }
    
    @After
    public void cleanupTestData() {
        cleanupTestVersions(TEST_VERSION_PREFIX);
        
        // Delete the target file if it exists
        if (targetFile != null && targetFile.exists()) {
            targetFile.delete();
        }
    }
    
    @Test
    public void testGenerateReleaseNotesPlainText() throws Exception {
        // Setup the release notes mojo
        GenerateReleaseNotesMojo mojo = new GenerateReleaseNotesMojo();
        mojo.setJiraURL(JIRA_URL);
        mojo.setJiraUser(JIRA_USERNAME);
        
        if (JIRA_PAT != null) {
            mojo.setJiraPersonalAccessToken(JIRA_PAT);
        } else {
            mojo.setJiraPassword(JIRA_PASSWORD);
        }
        
        mojo.setJiraProjectKey(JIRA_PROJECT_KEY);
        mojo.setReleaseVersion(versionName);
        mojo.setTargetFile(targetFile);
        mojo.setFormat("PlainTextGenerator");
        mojo.setJqlTemplate("project = ''{0}'' AND fixVersion = ''{1}''");
        mojo.setBeforeText("Integration Test Release Notes");
        mojo.setAfterText("End of Release Notes");
        
        // Execute the mojo to generate release notes
        mojo.execute();
        
        // Verify the release notes file was created
        assertThat("Release notes file should exist", targetFile.exists(), is(true));
        
        // Verify the file contains our before and after text
        String content = new String(Files.readAllBytes(targetFile.toPath()));
        assertThat("Release notes should contain before text", 
                   content.contains("Integration Test Release Notes"), is(true));
        assertThat("Release notes should contain after text", 
                   content.contains("End of Release Notes"), is(true));
    }
    
    @Test
    public void testGenerateReleaseNotesMarkdown() throws Exception {
        // Setup the release notes mojo
        GenerateReleaseNotesMojo mojo = new GenerateReleaseNotesMojo();
        mojo.setJiraURL(JIRA_URL);
        mojo.setJiraUser(JIRA_USERNAME);
        
        if (JIRA_PAT != null) {
            mojo.setJiraPersonalAccessToken(JIRA_PAT);
        } else {
            mojo.setJiraPassword(JIRA_PASSWORD);
        }
        
        mojo.setJiraProjectKey(JIRA_PROJECT_KEY);
        mojo.setReleaseVersion(versionName);
        mojo.setTargetFile(targetFile);
        mojo.setFormat("MarkDownGenerator");
        mojo.setJqlTemplate("project = ''{0}'' AND fixVersion = ''{1}''");
        mojo.setBeforeText("# Integration Test Release Notes");
        mojo.setAfterText("*End of Release Notes*");
        
        // Execute the mojo to generate release notes
        mojo.execute();
        
        // Verify the release notes file was created
        assertThat("Release notes file should exist", targetFile.exists(), is(true));
        
        // Verify the file contains our before and after text
        String content = new String(Files.readAllBytes(targetFile.toPath()));
        assertThat("Release notes should contain before text", 
                   content.contains("# Integration Test Release Notes"), is(true));
        assertThat("Release notes should contain after text", 
                   content.contains("*End of Release Notes*"), is(true));
    }
    
    @Test
    public void testGenerateReleaseNotesHtml() throws Exception {
        // Setup the release notes mojo
        GenerateReleaseNotesMojo mojo = new GenerateReleaseNotesMojo();
        mojo.setJiraURL(JIRA_URL);
        mojo.setJiraUser(JIRA_USERNAME);
        
        if (JIRA_PAT != null) {
            mojo.setJiraPersonalAccessToken(JIRA_PAT);
        } else {
            mojo.setJiraPassword(JIRA_PASSWORD);
        }
        
        mojo.setJiraProjectKey(JIRA_PROJECT_KEY);
        mojo.setReleaseVersion(versionName);
        mojo.setTargetFile(targetFile);
        mojo.setFormat("HtmlGenerator");
        mojo.setJqlTemplate("project = ''{0}'' AND fixVersion = ''{1}''");
        mojo.setBeforeText("<h1>Integration Test Release Notes</h1>");
        mojo.setAfterText("<p><em>End of Release Notes</em></p>");
        
        // Execute the mojo to generate release notes
        mojo.execute();
        
        // Verify the release notes file was created
        assertThat("Release notes file should exist", targetFile.exists(), is(true));
        
        // Verify the file contains our before and after text
        String content = new String(Files.readAllBytes(targetFile.toPath()));
        assertThat("Release notes should contain before text", 
                   content.contains("<h1>Integration Test Release Notes</h1>"), is(true));
        assertThat("Release notes should contain after text", 
                   content.contains("<p><em>End of Release Notes</em></p>"), is(true));
    }
}
