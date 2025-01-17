package com.ilsian.rmweb;

import java.util.HashMap;
import java.util.logging.Logger;

public class Global {
	
	static Logger logger = Logger.getLogger("com.ilsian.rmweb.Global");
	
	public static final boolean EMOJI_CRITS = true;
	public static boolean ALLOW_UNKNOWN_PLAYERS = true;
	public static boolean USE_COMBAT_TRACKER = true;
	public static boolean USE_AFFIRMATIVE_TRACKER = true;
	public static boolean CONDITION_MODS = true;
	public static String ENTITY_LINKS = "";
	//"https://docs.google.com/spreadsheets/d/e/2PACX-1vQMUUrtRyyntU-585PUGYkXJikM5M5yP2pGspSnP7sb9nJauGsnNuMN4FGleyNsQy3xCf8lUrNGFwBf/pub?gid=0&single=true&output=csv";
	
	static {
		load();
	}
	
	private static boolean getAsBool(HashMap<String,String> setMap, String key, boolean def) {
		return setMap.getOrDefault(key, def?"1":"0").equals("1");
	}
	
	private static void putAsBool(HashMap<String,String> setMap, String key, boolean val) {
		setMap.put(key, val?"1":"0");
	}
	
	public static void load()
	{
		// All Settings in DB are String, by convention bools are 1 or 0 strings
		logger.info("Loading global settings.");
		HashMap<String,String> setMap = EntityEngineSQLite.getInstance().getSettingMap();
		ALLOW_UNKNOWN_PLAYERS = getAsBool(setMap, "allow_unknown", true);
		USE_COMBAT_TRACKER = getAsBool(setMap, "combat_tracker", true);
		USE_AFFIRMATIVE_TRACKER = getAsBool(setMap, "affirmative_tracker", true);
		CONDITION_MODS = getAsBool(setMap, "condition_mods", true);
		ENTITY_LINKS = setMap.getOrDefault("entity_links", "");
	}
	
	public static void save()
	{
		logger.info("Saving global settings.");
		HashMap<String,String> setMap = new HashMap<String,String>();
		putAsBool(setMap, "allow_unknown", ALLOW_UNKNOWN_PLAYERS);
		putAsBool(setMap, "combat_tracker", USE_COMBAT_TRACKER);
		putAsBool(setMap, "affirmative_tracker", USE_AFFIRMATIVE_TRACKER);
		putAsBool(setMap, "condition_mods", CONDITION_MODS);
		setMap.put("entity_links", ENTITY_LINKS);
		EntityEngineSQLite.getInstance().putSettingMap(setMap);
	}
	
}
