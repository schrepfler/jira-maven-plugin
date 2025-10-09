package net.sigmalab.maven.plugin.jira.testClasses;

import org.apache.maven.plugin.MojoFailureException;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import net.sigmalab.maven.plugin.jira.AbstractJiraMojo;

/**
 * Simple implementation of AbstractJiraMojo for testing purposes
 */
public class TestAbstractJiraMojo extends AbstractJiraMojo {

    @Override
    public void doExecute(JiraRestClient restClient) throws MojoFailureException {
        // Do nothing - this is just for testing the validation in AbstractJiraMojo
    }
    
    /**
     * Expose the validateParameters method for testing
     */
    public void testValidateParameters() throws Exception {
        // This will call the execute method which contains the validation
        execute();
    }
}