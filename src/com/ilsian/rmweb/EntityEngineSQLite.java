package com.ilsian.rmweb;

import java.io.File;
import java.io.IOException;
import java.sql.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class EntityEngineSQLite {

	static EntityEngineSQLite instance_;
	
	public static EntityEngineSQLite getInstance() throws Exception {
		if (instance_ == null) {
			instance_ = new EntityEngineSQLite();
			instance_.open();
		}
		return instance_;
	}
	
	public static class Weapon {
		public int mWeaponId;
		public String mWeaponName;
		public int mSkill;
	}
	
	public static class Skill {
		public int mTotal;
		public String mDisplayName;
		public String mOwner; // not always set
	}
	
	public static class ActiveEntity {
		public int mUid;
		public String mName;
		public String mTag;
		public String mControllers;
		public int mFirstStrike;
		public int mLastInitPhase;
		public int mLastInit;
		public int mLastInitSort;
		public String mLastInitExplain;
		public int mLastResult;
		public String mLastResultExplain;
		public int mAt;
		public int mDb;
		public boolean mVisible = true;
		public Weapon [] mWeapons = new Weapon[4];
		public HashMap<String, Skill> mSkills = new HashMap();
		
		JSONObject toJSON() throws JSONException {
			JSONObject jact = new JSONObject();
			jact.put("name", mName);
			jact.put("controllers", mControllers);
			jact.put("tag", mTag);
			jact.put("fs", mFirstStrike);
			jact.put("phase", mLastInitPhase);
			jact.put("result", mLastResult);
			jact.put("explain", mLastResultExplain);
			jact.put("initiative", mLastInit);
			jact.put("initexplain", mLastInitExplain);
			jact.put("sort", mLastInitSort);
			jact.put("uid", mUid);
			jact.put("at", mAt);
			jact.put("db", mDb);
			jact.put("visibility", mVisible);
			JSONArray wlist = new JSONArray();
			for (int i=0;i<mWeapons.length; i++) {
				if (mWeapons[i] != null) {
					JSONObject jw = new JSONObject();
					jw.put("name", mWeapons[i].mWeaponName);
					jw.put("uid", mWeapons[i].mWeaponId);
					jw.put("ob", mWeapons[i].mSkill);
					wlist.put(jw);
				}
			}
			jact.put("weapons", wlist);
			return jact;
		}
	};
	
	// These ACTUALLY define the schema
	static final String [] STRING_KEYS = { "name", "tag", "controllers", "weapon1", "weapon2", "weapon3", "weapon4" };
	static final String [] BOOL_KEYS = { "public" };
	static final String [] INT_KEYS = { "at", "db", "fs", "ob1", "ob2", "ob3", "ob4", "observation", "combatawareness", "alertness" };
	
	static final HashMap<String,String> SKILL_MAP;
	static {
		SKILL_MAP = new HashMap();
		SKILL_MAP.put("observation", "Observation");
		SKILL_MAP.put("combatawareness", "Combat Aware");
		SKILL_MAP.put("alertness", "Alertness");
	}
	Connection iConnection=null;
	
	public static void main(String [] args) throws Exception
	{
		EntityEngineSQLite cdb = new EntityEngineSQLite();
		cdb.open();
		cdb.createSchema();
		cdb.updateDataFromImport(new File("c:\\users\\justin\\rmweb.csv"), new StringBuilder());
		cdb.close();
	}

	public void open() throws Exception
	{
		Class.forName("org.sqlite.JDBC");
		iConnection = DriverManager.getConnection("jdbc:sqlite:entities.db"); 
	}
	
	public void close()
	{
		if (iConnection != null)
		{
			try {
				iConnection.close();
			} catch (SQLException ignore) {
			}
		}
	}
	
	public String createSchemeStatement() {
		StringBuilder sbuild = new StringBuilder();
		sbuild.append("create table entities ( _id integer primary key ");
		for (String skey:STRING_KEYS) {
			sbuild.append(", " + skey + " text ");
		}
		for (String skey:BOOL_KEYS) {
			sbuild.append(", " + skey + " bool ");
		}
		for (String skey:INT_KEYS) {
			sbuild.append(", " + skey + " int default 0 ");
		}
		sbuild.append(")");
		return sbuild.toString();
	}
	
	public String createInsertStatement() {
		StringBuilder sbuild = new StringBuilder();
		sbuild.append("INSERT INTO entities ( ");
		for (String skey:STRING_KEYS) {
			sbuild.append(skey + ", ");
		}
		for (String skey:BOOL_KEYS) {
			sbuild.append(skey);
		}
		for (String skey:INT_KEYS) {
			sbuild.append(", " + skey);
		}
		sbuild.append(") values (?");
		int total = STRING_KEYS.length + BOOL_KEYS.length + INT_KEYS.length;
		for (int i=1;i<total;i++) {
			sbuild.append(",?");
		}
		sbuild.append(")");
		return sbuild.toString();
	}
	
	public String createUpateStatement() {
		StringBuilder sbuild = new StringBuilder();
		sbuild.append("UPDATE entities set ");
		for (String skey:STRING_KEYS) {
			sbuild.append(skey + "=? ,");
		}
		for (String skey:BOOL_KEYS) {
			sbuild.append(skey + "=?");
		}
		for (String skey:INT_KEYS) {
			sbuild.append(", " + skey + "=?");
		}
		sbuild.append("where _id=?");
		return sbuild.toString();
	}
	
	public String createQueryStatement(boolean pubOnly, String name, String tag) {
		StringBuilder sbuild = new StringBuilder();
		sbuild.append("SELECT _id");
		for (String skey:STRING_KEYS) {
			sbuild.append(", " + skey);
		}
		for (String skey:BOOL_KEYS) {
			sbuild.append(", " + skey);
		}
		for (String skey:INT_KEYS) {
			sbuild.append(", " + skey);
		}
		sbuild.append(" from entities");
		if (pubOnly) {
			sbuild.append(" where public=1");
			if (name != null) {
				sbuild.append(" and name=?");
			}
		} else if (name != null) {
			sbuild.append(" where name=?");
		} else if (tag != null) {
			sbuild.append(" where tag=?");
		}
		sbuild.append(" order by tag asc, name asc");
		System.err.println("QUERY -> " + sbuild.toString());
		return sbuild.toString();
	}
	
	protected void createSchema()
	{
		try {
			Statement st = iConnection.createStatement();
			st.executeUpdate(createSchemeStatement());
			System.err.println("Created table!");
			st.close();
		
		} catch (Exception sqe) {
			System.err.println("Did not create table!");
			if (sqe.toString().indexOf("already exists")<0)
				System.err.println("Couldnt populate weapon table: " + sqe);
		}
	}

	protected void updateDataFromImport(File csvFile, StringBuilder info) throws Exception {
		int updated = 0;
		int inserted = 0;
		
		PreparedStatement ps_lookup = iConnection.prepareStatement("SELECT _id FROM entities where name=?");
		PreparedStatement ps_add = iConnection.prepareStatement(createInsertStatement());
		PreparedStatement ps_update = iConnection.prepareStatement(createUpateStatement());
		PreparedStatement ps_insert;
		CSVParser parser = CSVParser.parse(csvFile, java.nio.charset.Charset.forName("UTF-8"), CSVFormat.RFC4180);
		HashMap<String, Integer> headerMap = new HashMap();
		for (CSVRecord csvRecord : parser) {
			if (csvRecord.getRecordNumber()==1) {
				int index=0;
				for (String col:csvRecord) {
					headerMap.put(col.toLowerCase(),  index);
					index++;
				}
				continue;
			}
			System.err.println("Parsing row=" + csvRecord.getRecordNumber());
			
			final String name = csvRecord.get(headerMap.get("name")).trim();
			ps_lookup.setString(1, name);
			final ResultSet rs = ps_lookup.executeQuery();
			int uid = -1;
			if (rs.next()) {
				uid = rs.getInt(1);
				ps_insert = ps_update;
				System.err.println("Entity is OLD=" + name + ", _id=" + uid);
				updated++;
			} else {
				System.err.println("Entity is NEW=" + name);
				ps_insert = ps_add;
				inserted++;
			}
			rs.close();
			
			int valIndex = 1;
			for (String strkey: STRING_KEYS) {
				Integer colIndex = headerMap.get(strkey);
				if (colIndex != null)
					ps_insert.setString(valIndex, csvRecord.get(headerMap.get(strkey)));
				else
					ps_insert.setString(valIndex, "");
				valIndex++;
			}
			for (String boolkey: BOOL_KEYS) {
				Integer colIndex = headerMap.get(boolkey);
				if (colIndex != null) {
					ps_insert.setBoolean(valIndex, csvRecord.get(headerMap.get(boolkey)).trim().equalsIgnoreCase("Y"));
				}
				else {
					ps_insert.setBoolean(valIndex, false);
				}
				valIndex++;
			}
			for (String intkey:INT_KEYS) {
				Integer colIndex = headerMap.get(intkey);
				if (colIndex != null) {
					final String istring = csvRecord.get(headerMap.get(intkey)).trim();
					if (istring.isEmpty())
						ps_insert.setInt(valIndex, 0);
					else
						ps_insert.setInt(valIndex, Integer.parseInt(istring));
				}
				else {
					ps_insert.setInt(valIndex, 0);
				}
				valIndex++;
			}
			if (uid >= 0) {
				ps_insert.setInt(valIndex, uid);
			}
			ps_insert.executeUpdate();
		}
		ps_add.close();
		ps_update.close();
		info.append("New: " + inserted + ", Updated: " + updated);
	}

	String queryName(int uid) throws SQLException {
		PreparedStatement p = iConnection.prepareStatement("SELECT name FROM entities where _id=?");
		p.setInt(1, uid);
		ResultSet rs = p.executeQuery();
		if (rs.next()) {
			return rs.getString(1);
		}
		return null;
	}
	
	String queryGroup(int uid) throws SQLException {
		PreparedStatement p = iConnection.prepareStatement("SELECT tag FROM entities where _id=?");
		p.setInt(1, uid);
		ResultSet rs = p.executeQuery();
		if (rs.next()) {
			return rs.getString(1);
		}
		return null;
	}
	
	JSONObject queryEntities(boolean publicOnly) throws SQLException, JSONException {
		JSONObject robj = new JSONObject();
		JSONArray ents = new JSONArray();
		robj.put("entities", ents);
		
		PreparedStatement p = iConnection.prepareStatement(createQueryStatement(publicOnly, null, null));
		ResultSet rs = p.executeQuery();
		while (rs.next()) {
			JSONObject ent = new JSONObject();
			int valIndex = 1;
			ent.put("_id", rs.getInt(valIndex++));
			for (String strkey: STRING_KEYS) {
				ent.put(strkey, rs.getString(valIndex++));
			}
			for (String boolkey: BOOL_KEYS) {
				ent.put(boolkey, rs.getBoolean(valIndex++));
			}
			for (String intkey:INT_KEYS) {
				ent.put(intkey, rs.getInt(valIndex++));
			}
			ents.put(ent);
		}
		return robj;
	}
	
	static String weaponLookupName(String name) {
		return name.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
	}
	
	void decodeEntity(ActiveEntity actor, ResultSet rs, HashMap<String, Integer> weaponMap) throws SQLException {
		actor.mUid = rs.getInt(1);
		int valIndex = 2;
		for (String strkey: STRING_KEYS) {
			if (strkey.equals("name")) {
				actor.mName = rs.getString(valIndex++);
			} else if (strkey.equals("tag"))  {
				actor.mTag = rs.getString(valIndex++);
			} else if (strkey.equals("controllers")) {
				actor.mControllers = rs.getString(valIndex++);
			} else if (strkey.startsWith("weapon")) {
				int index = Integer.parseInt(strkey.substring(6));
				actor.mWeapons[index-1] = new Weapon();
				actor.mWeapons[index-1].mWeaponName = rs.getString(valIndex++);
			} else {
				valIndex++;
			}
		}
		for (String boolkey: BOOL_KEYS) {
			valIndex++;
		}
		for (String intkey:INT_KEYS) {
			if (intkey.equals("fs")) {
				actor.mFirstStrike = rs.getInt(valIndex++);
			} else if (intkey.equals("at")) {
				actor.mAt = rs.getInt(valIndex++);
			} else if (intkey.equals("db")) {
				actor.mDb = rs.getInt(valIndex++);
			} else if (intkey.startsWith("ob") && intkey.length() == 3) {
				int index = Integer.parseInt(intkey.substring(2));
				actor.mWeapons[index-1].mSkill = rs.getInt(valIndex++);
			} else if (SKILL_MAP.containsKey(intkey)) {
				Skill sk = new Skill();
				sk.mDisplayName = SKILL_MAP.get(intkey);
				sk.mTotal = rs.getInt(valIndex++);
				actor.mSkills.put(intkey, sk);
			} else {
				valIndex++;
			}
		}
		
		// finally, perform a weapon lookup to validate the weapons and fill in the uid
		for (int i=0; i<actor.mWeapons.length; i++) {
			// support lookupname|display name
			String [] nameSplit = actor.mWeapons[i].mWeaponName.split("\\|");
			Integer uid = weaponMap.get(weaponLookupName(nameSplit[0]));
			if (uid != null) {
				actor.mWeapons[i].mWeaponId = uid;
				if (nameSplit.length>1) {
					actor.mWeapons[i].mWeaponName = nameSplit[1];
				}
			} else {
				if (!actor.mWeapons[i].mWeaponName.trim().isEmpty()) {
					System.err.println("Failed to lookup: " + actor.mWeapons[i].mWeaponName);
				}
				actor.mWeapons[i] = null;
			}
		}	
	}
	
	boolean queryUpdateMap(Map<String, ActiveEntity> map, String [] weaponNames) {
		final HashMap<String, Integer> weaponMap = new HashMap();
		for (int i=0;i<weaponNames.length; i++) {
			weaponMap.put(weaponLookupName(weaponNames[i]), i);
		}
		// basically, we updated our data store underneath our loaded models - but
		// these models may have some useful transient data we want to keep.
		return map.entrySet().removeIf(actor -> {
			// clever by 1/2, update or remove the bad apples
			PreparedStatement p = null;
			ResultSet rs = null;
			try {
				p = iConnection.prepareStatement(createQueryStatement(false, actor.getValue().mName, null));
				p.setString(1, actor.getValue().mName);
				rs = p.executeQuery();
				if (rs.next()) {
					decodeEntity(actor.getValue(), rs, weaponMap);
					return false; // dont remove
				} else {
					return true; // remove
				}
			} catch (SQLException sqe) {
				return false; // ?? better not to remove 
			} finally {
				Util.safeClose(p);
				Util.safeClose(rs);
			}
		});
	}

	boolean queryToMap(String name, Map<String, ActiveEntity> map, String [] weaponNames, String tag, boolean hidden) throws SQLException {
		HashMap<String, Integer> weaponMap = new HashMap();
		for (int i=0;i<weaponNames.length; i++) {
			weaponMap.put(weaponLookupName(weaponNames[i]), i);
		}
		PreparedStatement p = iConnection.prepareStatement(createQueryStatement((name==null&&tag==null)?true:false, name, tag));
		if (name != null)
			p.setString(1, name);
		else if (tag != null)
			p.setString(1, tag);
		
		boolean changed = false;
		
		ResultSet rs = p.executeQuery();
		while (rs.next()) {
			ActiveEntity actor = new ActiveEntity();
			decodeEntity(actor, rs, weaponMap);
			actor.mVisible = !hidden;
			map.put(actor.mName, actor);
			changed = true;
		}
		return changed;
	}
	
	public boolean importLive(File csvFile, String userRefName, StringBuilder resultLog)
	{
		try {
			resultLog.append(userRefName);
			resultLog.append(": ");
			updateDataFromImport(csvFile, resultLog);
			return true;
		} catch (Exception exc) {
			resultLog.append("Error: " + exc.getMessage());
			return false;
		}
	}
}
