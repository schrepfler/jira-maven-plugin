package net.sigmalab.maven.plugin.jira.formats;

import static java.text.MessageFormat.format;

import java.util.List;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;

public class PlainTextGenerator extends Generator {
    static final String ISSUETEMPLATE = "[{0}]\t{1}";
    static final String HORIZONTAL_RULE = "==============================";

    
    public PlainTextGenerator(JiraRestClient r, Iterable<Issue> i, String u, String b, String a, List<String> f) {
        super(r, i, u, b, a, f);
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
    public String addRow(Issue issue) {
        StringBuilder str = new StringBuilder();
        
        // str.append(format(ISSUETEMPLATE, i.getKey(), i.getSummary()));
        
        if ( getFields() != null && getFields().size() > 0 ) {
            // Can't just use an iterator because we don't want to print too many tabs.
        	for ( int i = 0; i < getFields().size(); i++ ) {
        	    String field = getFields().get(i);
        	    System.err.println("Adding value for " + field + " == " + issue.getKey().toString());
        		str.append(issue.getFieldByName(field).getValue());
        		
        		if ( i < getFields().size() )
        		    str.append("\t");
        	}
        }
        
        return str.toString();
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
        // If we haven't got any custom fields then just return a new line.
    	if ( getFields() == null || getFields().size() == 0 )
    		return System.getProperty("line.separator");
    	
    	// Otherwise let's find the custom fields and append these to the table headings.
    	StringBuilder header = new StringBuilder();
    	for ( int i = 0; i < getFields().size(); i++ ) {
    	    String field = getFields().get(i);
    	    
    		header.append(field);
    		
    		if ( i < getFields().size() )
    		    header.append("\t");
    	}

    	// Finally add a new line.
    	header.append(System.getProperty("line.separator"));
    	
    	return header.toString();
	}

	@Override
    public String addTableFooter() {
        return "";
    }

}
