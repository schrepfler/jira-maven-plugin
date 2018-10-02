package net.sigmalab.maven.plugin.jira.formats;

import java.io.PrintWriter;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

public abstract class Generator {
    private JiraRestClient restClient;
    protected IssueRestClient issueClient;
    private Iterable<Issue> issues;
    private String issueTemplate;
    private String beforeText;
    private String afterText;
    
    public Generator(JiraRestClient r, Iterable<Issue> i, String t, String b, String a) {
        this.restClient = r;
        this.issues = i;
        this.issueClient = restClient.getIssueClient();
        this.setIssueTemplate(t);
        this.setBeforeText(b);
        this.setAfterText(a);
    }
    
    public abstract String addHeader();
    public abstract String addRow(Issue i);
    public abstract String addBeforeText();
    public abstract String addAfterText();
    public abstract String addFooter();
    
    public void output(PrintWriter ps) {
        ps.print(this.addHeader());
        ps.println(this.addBeforeText());
        
        for ( Issue issue : issues ) {
            ps.println(addRow(issue));
        }
        
        ps.println(this.addAfterText());
        ps.print(this.addFooter());
    }

    public String getIssueTemplate() {
        return issueTemplate;
    }

    public void setIssueTemplate(String issueTemplate) {
        this.issueTemplate = issueTemplate;
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
