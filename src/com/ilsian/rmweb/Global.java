package com.ilsian.rmweb;

import java.util.logging.Logger;

public class Global {
	
	static Logger logger = Logger.getLogger("com.ilsian.rmweb.Global");
	
	public static final boolean EMOJI_CRITS = true;

	public static boolean USE_COMBAT_TRACKER = true;
	public static boolean USE_AFFIRMATIVE_TRACKER = true;
	public static boolean CONDITION_MODS = true;
	
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
