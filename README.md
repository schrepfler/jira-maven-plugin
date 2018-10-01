[![Build Status](https://travis-ci.org/schrepfler/jira-maven-plugin.svg?branch=develop)](https://travis-ci.org/schrepfler/jira-maven-plugin) [![Join the chat at https://gitter.im/jira-maven-plugin/Lobby](https://img.shields.io/badge/gitter-join%20chat-blue.svg)](https://gitter.im/jira-maven-plugin/Lobby)

JIRA Maven Plugin
=

This plugins is a fork of George Gastaldi's jira-maven-plugin available here: https://github.com/gastaldi/jira-maven-plugin

The internals of it were changed so that it uses the JIRA REST API rather the SOAP one which is deprecated in JIRA 7.x.

This Maven plugin allows the manipulation of JIRA fixVersions within the project associated with the built component.

<aside class="notice">

**The latest version of Atlassian's Jira REST client only supports Java versions greater than Java 8; therefore this plugin also only supports Java 8 and beyond.**

</aside>


# Usage

Before you start using this plugin, you *must* define the URL of your JIRA server inside the `<issueManagement>` section of your project's pom.xml:

    <issueManagement>
      <system>JIRA</system>
      <url>http://www.myjira.com/jira/browse/PROJECTKEY</url>
    </issueManagement>

The URL specified here is used to identify the associated JIRA project and server URL.

To use the jira-maven-plugin you should include it in the appropriate `<plugins>` section of your POM -- for example:

    <build>
      <plugins>
        <plugin>
          <groupId>net.sigmalab.maven.plugins</groupId>
          <artifactId>jira-maven-plugin</artifactId>
          <version>0.7</version>
          <configuration>
              <!-- Particular configuration options -->
          </configuration>
          <executions>
            <execution>
              <phase>deploy</phase>
              <goals>
                <!-- Goals which you're interested in executing during the deploy phase -->
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

## Goals


### `release-jira-version`

Add the following profile to be performed when the `deploy` phase is executed:

    <profile>
      <id>release</id>
      <activation>
        <property>
          <name>performRelease</name>
          <value>true</value>
        </property>
      </activation>
      <build>
        <plugins>
          <plugin>
            <groupId>net.sigmalab.maven.plugins</groupId>
            <artifactId>jira-maven-plugin</artifactId>
            <version>0.7</version>
            <inherited>false</inherited>
            <configuration>
              <!- <server> entry in settings.xml -->
              <settingsKey>jira</settingsKey>
            </configuration>
            <executions>
              <execution>
                <phase>deploy</phase>
                <goals>
                  <goal>release-jira-version</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </profile>

### `create-new-version`

Creates a new JIRA version of this project (without the -SNAPSHOT suffix)

Place it on your pom.xml:

    <plugin>
      <groupId>net.sigmalab.maven.plugins</groupId>
      <artifactId>jira-maven-plugin</artifactId>
      <version>0.7</version>
      <inherited>false</inherited>
      <configuration>
        <!- <server> entry in settings.xml -->
        <settingsKey>jira</settingsKey>
      </configuration>
      <executions>
        <execution>
          <phase>deploy</phase>
          <goals>
            <goal>create-new-jira-version</goal>
          </goals>
        </execution>
      </executions>
    </plugin>

### `generate-release-notes`

## Configuration Options

The following options can be specified in the `<configuration> ... </configuration>` section:

| Parameter | Description |
|----------|-------------|
| `<jiraProjectKey>` | The project key associated with the JIRA project -- if not specified this is computed from the jiraURL and/or the `<issuetracking>` component of your POM. | 
| `<jiraURL>` | The root URL of the JIRA server - this can be used to override the `<issueTracking>` section of your POM. |
| `<skip>` | Controls whether the plugin is enabled for this context. |
| `<versionDescription>` | Description which will be applied to the version if it is created -- defaults to the project name. | 
| `<finalName>` | Name of the version to be created - defaults to the project's finalName -- typically `${project.artifactId}-${project.version}` |
| `<finalNameUsedForVersion>` | Boolean controlling whether the finalName or |
| `<developmentVersion>` | The version number -- defaults to `${project.version}` |
| `<autoDiscoverLatestRelease>` | Boolean controlling whether the latest version in JIRA should be the one identified for release. | 
| `<releaseVersion>` | Version to release within JIRA -- defaults to `${project.version}` |
| `<scope>` | `project` (the default) or `session` if you want the goal to be executed for each project or only the last one in the Maven session |

### Authentication

The username and password used to authenticate with JIRA can be explicitly specified using the `<jiraUsername>` and `<jiraPassword>` configuration parameters respectively.

#### `<settingsKey>` Configuration

Alternatively, the authentication configuration can be controlled using Maven's standard `<servers>` configuration by specifying the `<settingsKey>` configuration parameter to identify which `<server>` section of your `settings.xml` to be used to define the username and password.

For example - if you specify:

    ...
      <configuration>
        <settingsKey>JIRA Server</settingsKey>
      </configuration>
    ...
    
This will look for the a `<server>` entry with the same name - something like:

    <servers>
      <server>
        <id>JIRA Server</id>
        <username>bob</username>
        <password>...</password>
      </server>
      <!-- ... etc ... -->
    </servers>
    
If the password in the `<server>` section uses Maven's standard encryption mechanism this will be automatically decrypted for authentication to JIRA.

### Release Note Structure

| Paramter | Description |
|----------|-------------|
| `<jqlTemplate>` | JQL query template which will be used to identify the issues for the release note. |
| `<issueTemplate>` | |
| `<maxIssues>` | Maximum number of issues to be returned by the query to Jira (**default: 500**). |
| `<releaseVersion>` | Version to have the release notes generated for (**default: <maven-project-version>**). |
| `<targetFile>` | Name of the output file which will be generated (**default: <build-directory>/releaseNotes.txt**). |
| `<beforeText>` | Text inserted before the table of issues being released. |
| `<afterText>` | Text appended after the table of issues being released. |

### Usage with the maven-release-plugin

One of the more useful automation functions that can be achieved with the jira-maven-plugin is the automation
of the creation of new unreleased versions and the releasing of existing versions of a jira project when used
in conjunction with the maven-release-plugin.

Using the settings below, the version of a jira project will be specified by ${project.artifactId}-${project.version}
from your pom.xml.

When run in conjunction with the maven-release-plugin to automate your release processes, the version of your
jira project will be released, and a new version will be created.

NOTE: Standard jira (without possible additional jira plugins) does not support versions per component within
a project. Therefore to keep the versioning relatively easy to follow, I only ever use this plugin on one component
within a jira project. If however, you only have one component per jira project, then you will not run into this 
limitation.

    <!-- Specify the build settings.                                                -->
    <build>
      <plugins>
        <!-- Use the jira-maven-plugin to automate the releasing of existing    -->
        <!-- versions in jira and the creation of the next, unreleased version  -->
        <!-- of the project in jira when used in conjunction with the standard  -->
        <!-- maven release process of 'mvn -B release:prepare release:perform'. -->
        <plugin>
          <groupId>net.sigmalab.maven.plugins</groupId>
          <artifactId>jira-maven-plugin</artifactId>
          <version>0.7</version>
          <inherited>false</inherited>
          <configuration>
            <!-- <server> <id> entry in settings.xml                        -->
            <settingsKey>JIRA Server</settingsKey>
          </configuration>
          <!-- The sequence of these three executions is significant.         -->
          <executions>
            <!-- Firstly, we release the currently existing latest version. -->
            <!-- Here we are relying upon 'mvn deploy' being forked from    -->
            <!-- mvn release:perform.                                       -->
            <execution>
              <id>deploy-release-jira-version</id>
              <phase>deploy</phase>
              <goals>
                <goal>release-jira-version</goal>
              </goals>
              <configuration>
                <!-- Because we are using finalNameUsedForVersion=true  -->
                <releaseVersion>${project.artifactId}-${project.version}</releaseVersion>
              </configuration>
            </execution>
            <!-- Then we create the release notes.                          -->
            <!-- By default creates: ./target/releaseNotes.txt              -->
            <!--
              And contains:
                [KEY-ID] maven-test-release: Create a dummy project for maven releases.
            -->
            <!-- If additional columns/output are needed, then the plugin   -->
            <!-- will need to be modified - see issue #48.                  -->
            <execution>
              <id>deploy-generate-release-notes</id>
              <phase>deploy</phase>
              <goals>
                <goal>generate-release-notes</goal>
              </goals>
              <configuration>
                <!-- Add in Done as a completed status.                 -->
                <jqlTemplate>project = ''{0}'' AND status in (Resolved, Closed, Done) AND fixVersion = ''{1}''</jqlTemplate>
                <!-- Because we are using finalNameUsedForVersion=true  -->
                <releaseVersion>${project.artifactId}-${project.version}</releaseVersion>
              </configuration>
            </execution>
            <!-- Next we create a new version (based on ${project.version}).-->
            <!-- A bit of a hack, but recreating an existing version works. -->
            <!-- The plugin is smart enough to remove the '-SNAPSHOT' from -->
            <!-- ${project.version} so SNAPSHOT versions are not created.   -->
            <!-- If the version already exists in jira, then no harm done.  -->
            <execution>
              <id>deploy-create-new-jira-version</id>
              <phase>install</phase>
              <goals>
                <goal>create-new-jira-version</goal>
              </goals>
              <!-- Change the Version from a default of ${pom.version} to -->
              <!-- ${project.artifactId}-${project.version}               -->
              <configuration>
                <finalNameUsedForVersion>true</finalNameUsedForVersion>
              </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
