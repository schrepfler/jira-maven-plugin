package net.sigmalab.maven.plugin.jira.formats;

import static java.text.MessageFormat.format;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

import net.steppschuh.markdowngenerator.rule.HorizontalRule;

public class HtmlGenerator extends Generator {
    static final String ISSUETEMPLATE = "<td>[<a href=\"{0}\">{1}</a>]</td><td>{2}</td>";
    static final String HORIZONTAL_RULE = "<hr/>";
    
    public HtmlGenerator(JiraRestClient r, Iterable<Issue> i, String b, String a) {
        super(r, i, b, a);
    }

    @Override
    public String addHeader() {
        return "<html><body>";
    }

    @Override
    public String addRow(Issue i) {
        return "<tr>" + 
               format(ISSUETEMPLATE, i.getSelf(), i.getKey(), i.getSummary()) +
               "</tr>";       
    }

    @Override
    public String addBeforeText() {
        return "<p>" + getBeforeText() + "</p>";
    }

    @Override
    public String addAfterText() {
        return "<p>" + getAfterText() + "</p>";
    }

    @Override
    public String addFooter() {
        return "</body></html>";
    }

    @Override
    public String addHorizontalRule() {
        return HORIZONTAL_RULE;
    }

    @Override
    public String addTableHeader() {
        return "<table><thead>\n" + 
               "<tr><th>Key</th><th>Summary</th></tr>\n" + 
               "</thead>\n" +
               "<tbody>\n";
    }

    @Override
    public String addTableFooter() {
        return "</tbody>\n" +
               "</table>\n";
    }

}
