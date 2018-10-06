package net.sigmalab.maven.plugin.jira;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.Iterables;

import net.sigmalab.maven.plugin.jira.formats.Generator;

/**
 * Goal that generates release notes based on a version in a JIRA project.
 * 
 * @goal generate-release-notes
 * @phase deploy
 * 
 * @author George Gastaldi
 * @author dgrierso
 */
public class GenerateReleaseNotesMojo extends AbstractJiraMojo {
    final Log log = getLog();

    /**
     * JQL Template to find issues associated with this version.
     * 
     * Parameter 0 = Project Key
     * Parameter 1 = Fix version
     * 
     * @parameter default-value="project = ''{0}'' AND fixVersion = ''{1}''"
     * @required
     */
    String jqlTemplate;

    /**
     * Max number of issues to return
     * 
     * @parameter default-value="500"
     * @required
     */
    int maxIssues;

    /**
     * Released Version
     * 
     * @parameter default-value="${project.version}"
     */
    String releaseVersion;

    /**
     * Target file
     * 
     * @parameter default-value="${project.build.directory}/releaseNotes.txt"
     */
    File targetFile;

    /**
     * Text to be appended BEFORE all issues details.
     * 
     * @parameter
     */
    String beforeText;

    /**
     * Text to be appended AFTER all issues details.
     * 
     * @parameter
     */
    String afterText;

    /**
     * Format of the generated release note.
     * 
     * Options are: PlainTextGenerator | MarkDownGenerator | HtmlGenerator
     * 
     * @parameter default-value="PlainTextGenerator"
     */
    String format;

    @Override
    public void doExecute(JiraRestClient jiraRestClient) throws MojoFailureException {
        log.info("Generating release note ...");
        
        Iterable<Issue> issues = getIssues(jiraRestClient);
        log.debug("Found " + Iterables.size(issues) + " issues.");

        try {
            output(jiraRestClient, issues);
        }
        catch ( IOException e ) {
            throw new MojoFailureException("Unable to generate release notes", e);
        }
    }

    /**
     * Recover issues from JIRA based on JQL Filter
     * 
     * @param restClient
     * @return
     */
    private Iterable<Issue> getIssues(JiraRestClient restClient) {
        String jql = format(jqlTemplate, getJiraProjectKey(), releaseVersion);
        log.info("Searching for ");
        log.debug("JQL Query: " + jql);

        return restClient.getSearchClient().searchJql(jql, maxIssues, 0, null).claim().getIssues();
    }

    /**
     * Writes issues to output
     * 
     * @param restClient
     * @param issues
     * @throws IOException
     */
    private void output(JiraRestClient restClient, Iterable<Issue> issues) throws IOException {
        log.debug("Target file == [" + targetFile + "]");

        if ( issues == null ) {
            log.warn("No Jira issues found.");
        }

        validateOutputFile(targetFile);
        
        try ( OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(targetFile, false), "UTF8");
                PrintWriter ps = new PrintWriter(writer) ) {

            Generator generator = null;
            try {
                // If the format has been specified using a custom class (i.e. has dots from the package name) 
                // then don't prepend our default package location onto the name of the class to be loaded.
                String formatPackage = ( format.contains(".") ? "" : "net.sigmalab.maven.plugin.jira.formats" );
                
                Class<?> clazz = Class.forName(formatPackage + "." + format);
                Constructor<?> constructor = clazz.getConstructor(JiraRestClient.class, Iterable.class, String.class,
                                                                  String.class);
                generator = (Generator) constructor.newInstance(restClient, issues, beforeText, afterText);
            }
            catch ( ClassNotFoundException | NoSuchMethodException e ) {
                String msg = "Could not find class [" + format + "] to generate the release note.";
                log.error(msg);
                throw new IOException(msg, e);
            }
            catch ( InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e ) {
                String msg = "Could not instantiate an instance of [" + format + "].";
                log.error(msg);
                throw new IOException(msg, e);
            }

            // We must have a generator object by this point otherwise we can go
            // no further, so double check and throw an exception if necessary.
            if ( generator == null ) {
                String msg = "No release note generator object created - exiting!";
                log.error(msg);
                throw new IOException(msg);
            }

            generator.output(ps);
        }
    }

    private void validateOutputFile(File f) throws IOException {
        // Creates a new file - DOES NOT APPEND - so warn if the file already exists.
        if ( f.exists() && ! f.isDirectory() ) { 
            log.warn("Target release notes file already exists - this will be overwritten!");
        }
        else if ( f.isDirectory() ) {
            String errorString = "Target release note filename already exists and is a directory";
            log.error(errorString + " - exiting!");
            
            throw new IOException(errorString);
        }
    }

    public String getJqlTemplate() {
        return jqlTemplate;
    }

    public void setJqlTemplate(String jqlTemplate) {
        this.jqlTemplate = jqlTemplate;
    }

    public int getMaxIssues() {
        return maxIssues;
    }

    public void setMaxIssues(int maxIssues) {
        this.maxIssues = maxIssues;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
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

    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
        
    }
}
