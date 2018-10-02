package net.sigmalab.maven.plugin.jira.formats;

import static java.text.MessageFormat.format;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

public class PlainTextGenerator extends Generator {

    public PlainTextGenerator(JiraRestClient r, Iterable<Issue> i, String t, String b, String a) {
        super(r, i, t, b, a);
    }

    @Override
    public String addHeader() {
        return "";
    }

    @Override
    public String addRow(Issue i) {
        return format(getIssueTemplate(), i.getKey(), i.getSummary());
    }

    @Override
    public String addBeforeText() {
        return getBeforeText();
    }

    @Override
    public String addAfterText() {
        return getAfterText();
    }

    @Override
    public String addFooter() {
        return "";
    }

}
