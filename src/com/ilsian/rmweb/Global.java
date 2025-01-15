package com.ilsian.rmweb;

import java.util.logging.Logger;

public class Global {
	
	static Logger logger = Logger.getLogger("com.ilsian.rmweb.Global");
	
	public static final boolean EMOJI_CRITS = true;

	public static boolean USE_COMBAT_TRACKER = true;
	public static boolean USE_AFFIRMATIVE_TRACKER = true;
	public static boolean CONDITION_MODS = true;
	public static String ENTITY_LINKS = "https://docs.google.com/spreadsheets/d/e/2PACX-1vQMUUrtRyyntU-585PUGYkXJikM5M5yP2pGspSnP7sb9nJauGsnNuMN4FGleyNsQy3xCf8lUrNGFwBf/pub?gid=0&single=true&output=csv";
	
	
	static {
		load();
	}
	
	public static void load()
	{
		logger.info("Loading global settings.");
		// TODO: Load from props file
	}
	
	public static void save()
	{
		logger.info("Saving global settings.");
		// TODO: Save to props file
	}
	
	
}
