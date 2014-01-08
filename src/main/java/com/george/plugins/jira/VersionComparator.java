package com.george.plugins.jira;

import java.util.Comparator;

import com.atlassian.jira.rest.client.domain.Version;

public class VersionComparator implements Comparator<Version> {

	@Override
	public int compare(Version o1, Version o2) {
		return doComparison(o1, o2);
	}

	public static int doComparison(Version o1, Version o2) {
		return -1 * o1.getName().compareToIgnoreCase(o2.getName());
	}

}
