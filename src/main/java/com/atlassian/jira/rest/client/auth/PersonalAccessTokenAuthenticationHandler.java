package com.atlassian.jira.rest.client.auth;

import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import org.apache.commons.codec.binary.Base64;

/**
 * Handler for Personal Access Token (PAT) authentication.
 * Do NOT use it in with unencrypted HTTP protocol over public networks, as credentials are passed
 * effectively in free text.
 *
 * @since v0.1
 */
public class PersonalAccessTokenAuthenticationHandler extends BasicHttpAuthenticationHandler {

    public PersonalAccessTokenAuthenticationHandler(final String username, final String personalAccessToken) {
        super(username, personalAccessToken);
    }

}
