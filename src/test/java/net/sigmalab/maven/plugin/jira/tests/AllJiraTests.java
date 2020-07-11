package net.sigmalab.maven.plugin.jira.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import net.sigmalab.maven.plugin.jira.testClasses.CreateNewVersionMojoTest;
import net.sigmalab.maven.plugin.jira.testClasses.GenerateReleaseNotesMojoTest;
import net.sigmalab.maven.plugin.jira.testClasses.ReleaseVersionMojoTest;

@SuiteClasses({ CreateNewVersionMojoTest.class, ReleaseVersionMojoTest.class, GenerateReleaseNotesMojoTest.class })
@RunWith(Suite.class)
public class AllJiraTests {

}
