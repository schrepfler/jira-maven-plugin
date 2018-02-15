package net.sigmalab.maven.plugin.jira.testClasses;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;

import net.sigmalab.maven.plugin.jira.GenerateReleaseNotesMojo;

@RunWith(JUnit4.class)
public class GenerateReleaseNotesMojoTest extends AbstractMojoTestCase {
    private static final String NEWLINE = System.getProperty("line.separator");

    private static final BasicIssue[] ISSUE_ARRAY = new BasicIssue[] { new BasicIssue(null, "DUMMY-1"),
                                                                       new BasicIssue(null, "DUMMY-4"),
                                                                       new BasicIssue(null, "DUMMY-3"),
                                                                       new BasicIssue(null, "DUMMY-2") };

    private static final Iterable<BasicIssue> ISSUES = Arrays.asList(ISSUE_ARRAY);

    private static final Issue DUMMY_ISSUE = new Issue("Dummy Issue", null, "ISSUE-1", null, null, null,
                                                       "Dummy Issue Description", null, null, null, null,
                                                       null, null, null, null, null, null, null, null,
                                                       null, null, null, null, null, null, null, null,
                                                       null, null, null);

    private GenerateReleaseNotesMojo releaseNoteMojo;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        releaseNoteMojo = new GenerateReleaseNotesMojo();
        
        releaseNoteMojo.setJiraUser("user");
        releaseNoteMojo.setJiraPassword("password");
        releaseNoteMojo.setJiraProjectKey("DUMMY");
        releaseNoteMojo.setBeforeText("This is BEFORE TEXT" + NEWLINE + "==============================");
        releaseNoteMojo.setAfterText("==============================" + NEWLINE + "This is AFTER TEXT");
        releaseNoteMojo.setJiraProjectKey("KEY");
        releaseNoteMojo.setSettingsKey("jira");
        releaseNoteMojo.setReleaseVersion("3.3.2.SR1");
        releaseNoteMojo.setTargetFile(new File("target/releaseNotes.txt"));
        releaseNoteMojo.setJqlTemplate("project = ''{0}'' AND fixVersion = ''{1}''");
        releaseNoteMojo.setIssueTemplate("[{0}] {1}");
        

        JiraRestClient mockJiraRestClient = Mockito.mock(JiraRestClient.class);

        /*
         * return restClient.getSearchClient().searchJql(jql, maxIssues, 0).claim().getIssues();
         */
        SearchRestClient mockSearchClient = Mockito.mock(SearchRestClient.class);
        Mockito.when(mockJiraRestClient.getSearchClient()).thenReturn(mockSearchClient);

        @SuppressWarnings("unchecked")
        Promise<SearchResult> mockSearchPromise = (Promise<SearchResult>) Mockito.mock(Promise.class);
        Mockito.when(mockSearchClient.searchJql(anyString(), anyInt(), anyInt())).thenReturn(mockSearchPromise);

        SearchResult mockSearchResult = Mockito.mock(SearchResult.class);
        Mockito.when(mockSearchPromise.claim()).thenReturn(mockSearchResult);
        Mockito.when(mockSearchResult.getIssues()).thenReturn(ISSUES);

        /*
         * Issue fullIssue = issueClient.getIssue(basicIssue.getKey()).claim();
         */
        IssueRestClient mockIssueClient = Mockito.mock(IssueRestClient.class);
        Mockito.when(mockJiraRestClient.getIssueClient()).thenReturn(mockIssueClient);

        @SuppressWarnings("unchecked")
        Promise<Issue> mockIssuePromise = (Promise<Issue>) Mockito.mock(Promise.class);
        Mockito.when(mockIssueClient.getIssue(anyString())).thenReturn(mockIssuePromise);
        Mockito.when(mockIssuePromise.claim()).thenReturn(DUMMY_ISSUE);
        
        releaseNoteMojo.setJiraRestClient(mockJiraRestClient);
    }

    @Test
    public void testDoExecute() throws Exception {
        releaseNoteMojo.execute();

        File newFile = new File("target/releaseNotes.txt");
        File staticFile = new File("src/test/resources/expectedReleaseNotes.txt");

        assertThat(newFile.exists(), is(true));
        assertThat(FileUtils.contentEqualsIgnoreEOL(newFile, staticFile, null), is(true));
    }

}
