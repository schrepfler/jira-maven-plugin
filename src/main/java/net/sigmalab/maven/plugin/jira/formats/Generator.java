package net.sigmalab.maven.plugin.jira.formats;

import java.io.PrintWriter;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

public abstract class Generator {
    private JiraRestClient restClient;
    protected IssueRestClient issueClient;
    private Iterable<Issue> issues;
    private String jiraUrl;
    private String beforeText;
    private String afterText;
    
    public Generator(JiraRestClient r, Iterable<Issue> i, String u, String b, String a) {
        this.restClient = r;
        this.issues = i;
        this.issueClient = restClient.getIssueClient();
        this.jiraUrl = u;
        this.beforeText = b;
        this.afterText = a;
    }
    
    public abstract String addHeader();
    public abstract String addHorizontalRule();
    public abstract String addTableHeader();
    public abstract String addRow(Issue i);
    public abstract String addTableFooter();
    public abstract String addBeforeText();
    public abstract String addAfterText();
    public abstract String addFooter();
    
    public void output(PrintWriter ps) {
        ps.print(this.addHeader());
        
        // Only print the beforeText if it's been specified.
        if ( this.getBeforeText() != null ) {
            ps.println(this.addBeforeText());
            ps.println(this.addHorizontalRule());
        }
        
        // Deliberately *not* using println() on the table header and footer as
        // this may sometimes be empty.
        ps.print(this.addTableHeader());
        for ( Issue issue : issues ) {
            ps.println(addRow(issue));
        }
        ps.print(this.addTableFooter());
        
        // Only print the afterText if it's been specified.
        if ( this.getAfterText() != null ) {
            ps.println(this.addHorizontalRule());
            ps.println(this.addAfterText());
        }
        
        ps.print(this.addFooter());
    }

    public String getBeforeText() {
        return beforeText;
    }

    public String getAfterText() {
        return afterText;
    }
    
    protected String computeIssueUrl(Issue i) {
        // Use the jiraUrl as the basis for the URL of the issue passed to the method.
        // Do this by stripping back the URL to just .../browse/ ending and then append
        // the issue key.
        int position = jiraUrl.lastIndexOf("/browse/");
        
        // If we haven't found /browse/ in the Jira URL then just return the issue key.
        if ( position < 0 ) {
            return i.getKey();
        }
        
        // Otherwise return the issue key appended to the substring of the Jira URL.
        return jiraUrl.substring(0, position) + "/browse/" + i.getKey();
    }
}
