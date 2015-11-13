package net.sigmalab.maven.plugin.java;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import net.sigmalab.maven.plugin.jira.GenerateReleaseNotesMojo;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GenerateReleaseNotesMojoTest extends AbstractMojoTestCase {

	private GenerateReleaseNotesMojo mojo;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		mojo = (GenerateReleaseNotesMojo) lookupMojo("generate-release-notes",
				"src/test/resources/GenerateReleaseNotesMojoTest.xml");
	}

	@Ignore
	@Test
	public void testDoExecute() throws Exception {
		mojo.execute();
		assertThat(new File("target/releaseNotes.txt").exists(), is(true));
	}

}
