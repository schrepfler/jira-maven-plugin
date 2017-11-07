package net.sigmalab.maven.plugin.jira.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({ CreateNewVersionMojoTest.class, ReleaseVersionMojoTest.class, GenerateReleaseNotesMojoTest.class })
@RunWith(Suite.class)
public class AllJiraTests {

}
