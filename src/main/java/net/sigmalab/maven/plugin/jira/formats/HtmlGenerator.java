package net.sigmalab.maven.plugin.jira.formats;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

public class HtmlGenerator extends Generator {

    public HtmlGenerator(JiraRestClient r, Iterable<Issue> i, String b, String a) {
        super(r, i, b, a);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String addHeader() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String addRow(Issue i) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String addBeforeText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String addAfterText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String addFooter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String addHorizontalRule() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String addTableHeader() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String addTableFooter() {
        // TODO Auto-generated method stub
        return null;
    }

}
