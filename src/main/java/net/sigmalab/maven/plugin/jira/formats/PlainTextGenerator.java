package net.sigmalab.maven.plugin.jira.formats;

import static java.text.MessageFormat.format;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

public class PlainTextGenerator extends Generator {
    static final String ISSUETEMPLATE = "[{0}]\t{1}";
    static final String HORIZONTAL_RULE = "==============================";

    
    public PlainTextGenerator(JiraRestClient r, Iterable<Issue> i, String b, String a) {
        super(r, i, b, a);
    }

    @Override
    public String addHeader() {
        return "";
    }

    @Override
    public String addHorizontalRule() {
        return HORIZONTAL_RULE;
    }

    @Override
    public String addRow(Issue i) {
        return format(ISSUETEMPLATE, i.getKey(), i.getSummary());
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
    public String addTableHeader() {
        // Explicitly placing a newline at the end of the string.
        return "ISSUE KEY\tSUMMARY" + System.getProperty("line.separator");
    }

    @Override
    public String addTableFooter() {
        return "";
    }

}
