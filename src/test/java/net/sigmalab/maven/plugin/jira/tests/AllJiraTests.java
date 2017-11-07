package net.sigmalab.maven.plugin.jira.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.sigmalab.maven.plugin.jira.GenerateReleaseNotesMojo;

@SuiteClasses({ CreateNewVersionMojoTest.class, ReleaseVersionMojoTest.class, GenerateReleaseNotesMojo.class })
@RunWith(Suite.class)
public class AllJiraTests {

}
