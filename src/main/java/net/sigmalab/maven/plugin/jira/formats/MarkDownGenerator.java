package net.sigmalab.maven.plugin.jira.formats;

import static java.text.MessageFormat.format;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

public class MarkDownGenerator extends Generator {
    static final String ISSUETEMPLATE = "| [{0}]({1}) | {2} |";
    static final String HORIZONTAL_RULE = "---";
    
    public MarkDownGenerator(JiraRestClient r, Iterable<Issue> i, String b, String a) {
        super(r, i, b, a);
    }

    @Override
    public String addHeader() {
        return "";
    }

    @Override
    public String addRow(Issue i) {
        return format(ISSUETEMPLATE, i.getKey(), i.getSelf(), i.getSummary());
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

    @Override
    public String addHorizontalRule() {
        return HORIZONTAL_RULE;
    }

    @Override
    public String addTableHeader() {
        return "| ISSUE KEY | SUMMARY |\n" + 
               "|-----------|---------|\n";
    }

    @Override
    public String addTableFooter() {
        return "";
    }

}
