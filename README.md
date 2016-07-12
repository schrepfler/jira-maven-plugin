[![Build Status](https://travis-ci.org/schrepfler/jira-maven-plugin.svg?branch=develop)](https://travis-ci.org/schrepfler/jira-maven-plugin) [![Stories in Ready](https://badge.waffle.io/schrepfler/jira-maven-plugin.png?label=ready&title=Ready)](http://waffle.io/schrepfler/jira-maven-plugin)

Maven JIRA Plugin
=================

This plugins is a fork of George Gastaldi's jira-maven-plugin available here: https://github.com/gastaldi/jira-maven-plugin

The internals of it were changed so that it uses the JIRA REST API rather the SOAP one which is going to be deprecated.

To do so, we are going to use the Atlassian jira-rest-client library. 

This Maven plugin allows performing of JIRA common actions, like releasing a version, create a new version and generate the release notes:


Usage
=====================

Before you start using this plugin, you must have two configurations already set on your pom.xml:

issueManagement tag
=====================

        <issueManagement>
           <system>JIRA</system>
           <url>http://www.myjira.com/jira/browse/PROJECTKEY</url>
        </issueManagement>

Note: This is extremely important, as will use this information to connect on JIRA.

<server> entry in settings.xml with the authentication information
=====================

Put the following in the settings.xml file: 

    <servers>
        <server>
            <id>jira</id>
            <username>your_user</username>
            <password>your_password</password>
        </server>
    </servers>


Also, make sure your JIRA has REST API access enabled.


release-jira-version goal
=====================

Add the following profile to be executed when released:

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
				    <version>0.4</version>
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

create-new-version
=====================

Creates a new JIRA version of this project (without the -SNAPSHOT suffix)

Place it on your pom.xml:

    <plugin>
	    <groupId>net.sigmalab.maven.plugins</groupId>
	    <artifactId>jira-maven-plugin</artifactId>
	    <version>0.4</version>
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


Development
===================

To build and install locally:

    mvn clean install
    
Release to Sonatype OSS maven repository
===================

Setup your ~/.m2/settings.xml to contain the credentials for deployment to Sonatype OSS

    mvn jgitflow:release-start
    pray
    mvn jgitflow:release-finish
    pray some more

TODO
===================

* Block release if all issues with fixVersion of the release version are not Closed/Resolved.
* Generate release reports mojo
* Capability to add a prefix/postfix to the JIRA version (as JIRA struggles supporting multiple components per project).
