package com.ilsian.rmweb;

/**
 * Common place to store shared cookie names and session
 * attributes.
 * Webspace
 * @author justin
 *
 */
public class Webspace {

	///////////////////////////////////////////////////////
	// SESSION ATTRIBUTE NAMES
	///////////////////////////////////////////////////////
	// information about currently signed in user
	public static final String CURR_USER = "rmweb.login";
	// target user wanted to access before being redirected to login
	public static final String LOGIN_TARGET = "rmweb.logintarget";
	// last username used to login
	public static final String LAST_USER = "rmweb.lastuser";
	// a unique pw challenge token
	public static final String CHALLENGE_TOKEN = "rmweb.challenge";
}

