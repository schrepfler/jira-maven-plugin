[![Build Status](https://travis-ci.org/schrepfler/jira-maven-plugin.svg?branch=develop)](https://travis-ci.org/schrepfler/jira-maven-plugin)

JIRA Maven Plugin
=

This plugins is a fork of George Gastaldi's jira-maven-plugin available here: https://github.com/gastaldi/jira-maven-plugin

The internals of it were changed so that it uses the JIRA REST API rather the SOAP one which is deprecated in JIRA 7.x.

This Maven plugin allows the manipulation of JIRA fixVersions within the project associated with the built component.


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
                <version>0.5</version>
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
				    <version>0.5</version>
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
	    <version>0.5</version>
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

| Paramter | Description |
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
| `<jqlTemplate>` | |
| `<issueTemplate>` | |
| `<maxIssues>` | |
| `<releaseVersion>` | |
| `<targetFile>` | |
| `<beforeText>` | |
| `<afterText>` | |


