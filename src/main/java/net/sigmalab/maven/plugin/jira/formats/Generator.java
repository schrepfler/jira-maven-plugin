package net.sigmalab.maven.plugin.jira.formats;

import java.io.PrintWriter;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

public abstract class Generator {
    private JiraRestClient restClient;
    protected IssueRestClient issueClient;
    private Iterable<Issue> issues;
    private String beforeText;
    private String afterText;
    
    public Generator(JiraRestClient r, Iterable<Issue> i, String b, String a) {
        this.restClient = r;
        this.issues = i;
        this.issueClient = restClient.getIssueClient();
        this.setBeforeText(b);
        this.setAfterText(a);
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
        ps.println(this.addBeforeText());
        ps.println(this.addHorizontalRule());

        // Deliberately *not* using prinln() on the table header and footer as
        // this may sometimes be empty.
        ps.print(this.addTableHeader());
        for ( Issue issue : issues ) {
            ps.println(addRow(issue));
        }
        ps.print(this.addTableFooter());
        
        ps.println(this.addHorizontalRule());
        ps.println(this.addAfterText());
        ps.print(this.addFooter());
    }

    public String getBeforeText() {
        return beforeText;
    }

    public void setBeforeText(String beforeText) {
        this.beforeText = beforeText;
    }

    public String getAfterText() {
        return afterText;
    }

    public void setAfterText(String afterText) {
        this.afterText = afterText;
    }
}
