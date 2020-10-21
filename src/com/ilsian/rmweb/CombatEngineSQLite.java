package com.ilsian.rmweb;

import java.sql.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CombatEngineSQLite implements CombatLookup {

	Connection iConnection=null;
	
	// these are the max table limits for rank-limited skills
	private static final int[] RANK_LIMITS = {105, 120, 135, 150};
	
	public static void main(String [] args) throws Exception
	{
		CombatEngineSQLite cdb = new CombatEngineSQLite();
		cdb.open();
		for (String s:cdb.getWeaponList())
			System.err.println("WEAPON: " + s);
		for (String s:cdb.getCriticalList())
			System.err.println("CRITICAL: " + s);
		System.err.println("DAMAGE: " + cdb.getDamage(99, 140, 2, 4, 3));
		System.err.println("CRIT ROLL:");
		for (String s:cdb.getCriticals(99, "ES,AS"))
			System.err.println("-> " + s);
		System.err.println("CRIT AS HTML:");
		System.err.println("-> " + cdb.getCriticalsAsHtml(99, "ES,AS"));
		cdb.close();
	}

	public void open() throws Exception
	{
		Class.forName("org.sqlite.JDBC");
		iConnection = DriverManager.getConnection("jdbc:sqlite::resource:com/ilsian/rmweb/res/combat.db");
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
	
	protected class TableHandler extends DefaultHandler
	{
		PreparedStatement iPsList;
		PreparedStatement iPsData;
		int iCurrWeapon=0;
		int iCurrData=0;
		int iLowRange=0;
		int iHighRange=0;
		
		public TableHandler(PreparedStatement ps_list, PreparedStatement ps_data)
		{
			iPsList = ps_list;
			iPsData = ps_data;
		}
		
		public void startElement(String uri, String qName, String tag, Attributes h) throws SAXException
		{
			try {
				if (tag.equals("weapon"))
				{
					//"INSERT INTO weaponlisttbl (id, name, fumble) values (?,?,?)"
					iPsList.setInt(1,++iCurrWeapon);
					iPsList.setString(2,h.getValue("name"));
					iPsList.setInt(3, Integer.parseInt(h.getValue("fumblehigh")));
					iPsList.executeUpdate();
					System.err.println("Added weapon[" + iCurrWeapon + "]=" + h.getValue("name"));
				}
				else if (tag.equals("range"))
				{
					// just buffer to use with damage lines
					iLowRange = Integer.parseInt(h.getValue("low"));
					iHighRange = Integer.parseInt(h.getValue("high"));
					System.err.println("-- Range: " + iLowRange + "-" + iHighRange);
				}
				else if (tag.equals("damage"))
				{
					//"INSERT INTO weapondatatbl (id, weapon_id, low_range, high_range, at, damage, critical) values (?,?,?,?,?,?,?)"
					iPsData.setInt(1,iCurrData++);
					iPsData.setInt(2, iCurrWeapon);
					iPsData.setInt(3, iLowRange);
					iPsData.setInt(4, iHighRange);
					iPsData.setInt(5, Integer.parseInt(h.getValue("at")));
					iPsData.setInt(6, Integer.parseInt(h.getValue("damage")));
					String crit = h.getValue("critical");
					if (crit != null)
						iPsData.setString(7, crit);
					else
						iPsData.setString(7, "");
					iPsData.addBatch();
					//iPsData.executeUpdate();
				}
			} catch (SQLException sqe) {
				System.err.println("SQE EXC on '" + tag + "': " + sqe);
			}
		}
		
		public void endElement(String uri, String qName, String tag) throws SAXException
		{
			try {
				if (tag.equals("range"))
				{
					System.err.print("Exec Batch...");
					iPsData.executeBatch();
					System.err.println("Done.");
				}
			} catch (SQLException sqe){
				sqe.printStackTrace();
			}
		}
	}
	
	protected class CriticalHandler extends DefaultHandler
	{
		PreparedStatement iPsList;
		PreparedStatement iPsData;
		int iCurrWeapon=0;
		int iCurrData=0;
		int iLowRange=0;
		int iHighRange=0;
		
		public CriticalHandler(PreparedStatement ps_list, PreparedStatement ps_data)
		{
			iPsList = ps_list;
			iPsData = ps_data;
		}
		
		public void startElement(String uri, String qName, String tag, Attributes h) throws SAXException
		{
			try {
				if (tag.equals("table"))
				{
					//INSERT INTO criticallisttable (id, name, code) values (?,?,?)
					iPsList.setInt(1,++iCurrWeapon);
					iPsList.setString(2,h.getValue("name"));
					iPsList.setString(3, h.getValue("code"));
					iPsList.executeUpdate();
					System.err.println("Added crittbl[" + iCurrWeapon + "]=" + h.getValue("name"));
				}
				else if (tag.equals("critical"))
				{
					//INSERT INTO criticaldatatable (id, crit_id, low_range, high_range, severity, critical) values (?,?,?,?,?,?)
					iLowRange = Integer.parseInt(h.getValue("low_range"));
					iHighRange = Integer.parseInt(h.getValue("high_range"));

					iPsData.setInt(1,iCurrData++);
					iPsData.setInt(2, iCurrWeapon);
					iPsData.setInt(3, iLowRange);
					iPsData.setInt(4, iHighRange);
					iPsData.setString(5, h.getValue("severity"));
					iPsData.setString(6, h.getValue("result"));
					iPsData.executeUpdate();
				}
			} catch (SQLException sqe) {
				System.err.println("SQE EXC on '" + tag + "': " + sqe);
			}
		}
	}
	
	protected void populateWeaponTable()
	{
		try {
		Statement st = iConnection.createStatement();
		st.executeUpdate("create table weaponlisttable ( _id int primary key, name text, fumble int, ranked bool default false, rank1 int, rank2 int, rank3 int, rank4 int )");
		st.executeUpdate("create table weapondatatable ( _id int primary key, weapon_id int, low_range int, high_range int, at int, damage int, critical text)");		
		PreparedStatement ps_list = iConnection.prepareStatement("INSERT INTO weaponlisttable (_id, name, fumble) values (?,?,?)");
		PreparedStatement ps_dat = iConnection.prepareStatement("INSERT INTO weapondatatable (_id, weapon_id, low_range, high_range, at, damage, critical) values (?,?,?,?,?,?,?)");

		TableHandler handler = new TableHandler(ps_list, ps_dat);
    	SAXParserFactory factory = SAXParserFactory.newInstance();
    	SAXParser saxParser = factory.newSAXParser();
    	saxParser.parse(getClass().getResourceAsStream("/com/ilsian/battlemat/db/weapons.xml"), handler);
    	
    	ps_list.close();
    	ps_dat.close();
    	st.close();
		} catch (Exception sqe) {
			if (sqe.toString().indexOf("already exists")<0)
				System.err.println("Couldnt populate weapon table: " + sqe);
		}
	}
	
	protected void populateCriticalTable()
	{
		try {
		Statement st = iConnection.createStatement();
		st.executeUpdate("create table criticallisttable ( _id int primary key, name text, code text )");
		st.executeUpdate("create table criticaldatatable ( _id int primary key, crit_id int, low_range int, high_range int, severity text, critical text)");		
		PreparedStatement ps_list = iConnection.prepareStatement("INSERT INTO criticallisttable (_id, name, code) values (?,?,?)");
		PreparedStatement ps_dat = iConnection.prepareStatement("INSERT INTO criticaldatatable (_id, crit_id, low_range, high_range, severity, critical) values (?,?,?,?,?,?)");

		CriticalHandler handler = new CriticalHandler(ps_list, ps_dat);
    	SAXParserFactory factory = SAXParserFactory.newInstance();
    	SAXParser saxParser = factory.newSAXParser();
    	saxParser.parse(getClass().getResourceAsStream("/com/ilsian/battlemat/db/crittables.xml"), handler);

    	ps_list.close();
    	ps_dat.close();
    	st.close();
		} catch (Exception sqe) {
			if (sqe.toString().indexOf("already exists")<0)
				System.err.println("Couldnt populate critical table: " + sqe);
		}
	}
	
	public String [] getWeaponList() {
		Vector weaps = new Vector();
		try {
			
			Statement st = iConnection.createStatement();
			ResultSet rs = st.executeQuery("select _id, name from weaponlisttable group by name order by _id asc");
			while (rs.next())
			{
				weaps.add(rs.getString(2));
			}
			rs.close();
			st.close();
		} catch (SQLException sqe) {
			System.err.println("Error getting weapon list: " + sqe);
		}
		
		String [] weapons = { "Pick weapon..." };
		if (weaps.size() > 0)
		{
			weapons = new String[weaps.size()];
			weaps.copyInto(weapons);
		}
		return weapons;
	}
	
	public String [] getCriticalList() {
		String [] crits = { "A","B","C","D","E" };
		return crits;
	}
	
	public boolean isFumble(int roll, int weaponIndex)
	{
		weaponIndex++;
		try {
			Statement st = iConnection.createStatement();
			ResultSet rs = st.executeQuery("select fumble from weaponlisttable where _id=" + weaponIndex);
			if (rs.next())
			{
				int fbelow = rs.getInt(1);
				if (roll <= fbelow)
					return true;
			}
		} catch (SQLException sqe) {
			System.err.println("Error checking fumble: " + sqe);
		}
		
		return false;
	}
	
	public boolean isFumble(int roll, int weaponIndex, StringBuilder sb)
	{
		weaponIndex++;
		try {
			Statement st = iConnection.createStatement();
			ResultSet rs = st.executeQuery("select fumble,onfumble from weaponlisttable where _id=" + weaponIndex);
			if (rs.next())
			{
				int fbelow = rs.getInt(1);
				if (roll <= fbelow) {
					String fm = rs.getString(2);
					if (fm != null && !fm.isEmpty()) 
						sb.append(fm);
					return true;
				}
			}
		} catch (SQLException sqe) {
			System.err.println("Error checking fumble: " + sqe);
		}
		
		return false;
	}
	
	public DamageResult getDamage(int um_roll, int roll, int weaponIndex, int at, int rankSelect) {
		// check basic fumbles first
		StringBuilder fumbCrit = new StringBuilder();
		if (isFumble(um_roll, weaponIndex, fumbCrit)) {
			DamageResult dr = new DamageResult(-1);
			if (fumbCrit.length() > 0) dr.iCriticals = fumbCrit.toString();
			return dr;
		}

		weaponIndex++;
		
		int rankLimit = 150;
		// Lookup rankLimit using rankSelect and weaponIndex
		Statement s = null;
		ResultSet rrs = null;
		try
		{
			s = iConnection.createStatement();
			rrs = s.executeQuery("select ranked from weaponlisttable where _id=" + weaponIndex);
			if (rrs.next())
			{
				if (rrs.getBoolean(1)) {
					if (rankSelect >= 0 && rankSelect < RANK_LIMITS.length) {
						rankLimit = RANK_LIMITS[rankSelect];
					} else {
						System.err.println("Invalid Rank Selection!");
					}
				}
			}
		} catch (SQLException sqe) {
			sqe.printStackTrace();
		} finally {
			Util.safeClose(rrs);
			Util.safeClose(s);
		}
		
		DamageResult d = new DamageResult(0);
		if (rankLimit < 150 && roll > rankLimit) {
			// only set if it limits the effect
			d.iRankLimit = rankLimit;
		}
		try {
			PreparedStatement ps = iConnection.prepareStatement("select damage,critical from weapondatatable where weapon_id=? and at=? and low_range<=? and high_range>=?");
			while (roll > 0)
			{
				int effectiveRoll = Math.min(rankLimit, (roll>150?150:roll));
				ps.setInt(1,weaponIndex);
				ps.setInt(2, at);
				ps.setInt(3, effectiveRoll);
				ps.setInt(4, effectiveRoll);
				ResultSet rs = ps.executeQuery();
				if (rs.next())
				{
					d.addDamage(rs.getInt(1), rs.getString(2));
				}
				rs.close();
				roll -= 150;
			}
			ps.close();
		} catch (SQLException sqe) {
			System.err.println("Error checking damage: " + sqe);
		}
		
		if (d.iDamage < 0) {
			// Fumble detected in lookup portion - kind of a kludge but lookup the fumble table by reporting an UM1
			if (isFumble(1, weaponIndex, fumbCrit)) {
				// this *should* be true
				if (fumbCrit.length() > 0) d.iCriticals = fumbCrit.toString();
			}			
		}
		return d;
	}	
	
	public String [] getCriticals(int roll, String tables)
	{
		Vector results = new Vector();
		
		try {
			// tables holds a comma separated list of severity+critical
			String [] crits = tables.split(",");
			PreparedStatement ps = iConnection.prepareStatement("select critical from criticaldatatable where crit_id in (select _id from criticallisttable where code=?) and severity=? and low_range<=? and high_range>=?");
			for (int i=0;i<crits.length;i++)
			{
				String ct = crits[i].trim();
				if (ct.length()==0)
					continue;
				
				char severity = ct.charAt(0);
				char table = ct.charAt(1);
				ps.setString(1, Character.toString(table));
				ps.setString(2, Character.toString(severity));
				ps.setInt(3,roll);
				ps.setInt(4, roll);
				ResultSet rs = ps.executeQuery();
				if (rs.next())
				{
					results.add(rs.getString(1));
				}
				rs.close();
			}
			ps.close();
			
			// return results
			if (results.size() == 0)
				return null;
		} catch (SQLException sqe) {
			System.err.println("FAILED LOOKING UP CRITICAL: " + sqe);
		}
		String [] sr = new String[results.size()];
		results.copyInto(sr);
		return sr;
	}
	
	public String [] getCriticals(Dice.Open roll, String tables)
	{
		Vector results = new Vector();
		
		try {
			// tables holds a comma separated list of severity+critical
			String [] crits = tables.split(",");
			PreparedStatement ps = iConnection.prepareStatement("select critical from criticaldatatable where crit_id in (select _id from criticallisttable where code=?) and severity=? and low_range<=? and high_range>=?");
			PreparedStatement checkps = iConnection.prepareStatement("select max(high_range) from criticaldatatable where crit_id in (select _id from criticallisttable where code=?)");
			for (int i=0;i<crits.length;i++)
			{
				String ct = crits[i].trim();
				if (ct.length()==0)
					continue;
				
				char severity = ct.charAt(0);
				char table = ct.charAt(1);
				
				// check if we use open ended or d100 only
				checkps.setString(1, Character.toString(table));
				ResultSet checkrs = checkps.executeQuery();
				if (!checkrs.next()) {
					// can't find it, skip it
					continue;
				}
				
				int tableMax = checkrs.getInt(1);
				checkrs.close();
				
				if (tableMax > 100)
					roll.used_open_ = true;
				
				int rollForCrit = Math.min(tableMax, roll.total_);				
				
				ps.setString(1, Character.toString(table));
				ps.setString(2, Character.toString(severity));
				ps.setInt(3, rollForCrit);
				ps.setInt(4, rollForCrit);
				ResultSet rs = ps.executeQuery();
				if (rs.next())
				{
					results.add(rs.getString(1));
				}
				rs.close();
			}
			ps.close();
			
			// return results
			if (results.size() == 0)
				return null;
		} catch (SQLException sqe) {
			System.err.println("FAILED LOOKING UP CRITICAL: " + sqe);
		}
		String [] sr = new String[results.size()];
		results.copyInto(sr);
		return sr;
	}
	
	CritParser critParser_ = new CritParser();
	
	public static class CriticalResults
	{
		public String html_;
		public String [] results_;
		public EffectRecord [] details_;
	}
	
	public boolean hasReverseCritical(String tables) {
		String [] crits = tables.split(",");
		for (int i=0;i<crits.length;i++)
		{
			String ct = crits[i].trim();
			if (ct.length()==0)
				continue;
			
			char table = ct.charAt(1);
			if (table == 'f' || table == 'F')
				return true;
		}
		return false;
	}
	
	public CriticalResults getCriticalResults(Dice.Open roll, String tables)
	{
		CriticalResults cres = new CriticalResults();
		cres.results_ = getCriticals(roll, tables);
		cres.html_ = formatCriticalResults(cres.results_);
		if (cres.results_ != null) {
			cres.details_ = new EffectRecord[cres.results_.length];
			for (int i=0;i<cres.results_.length;i++) {
				cres.details_[i] = critParser_.extractDetail(cres.results_[i]);
			}
		}
		return cres;
	}
	
	public String getCriticalsAsHtml(Dice.Open roll, String tables)
	{
		return formatCriticalResults(getCriticals(roll, tables));
	}
	
	public String getCriticalsAsHtml(int roll, String tables)
	{
		return formatCriticalResults(getCriticals(roll, tables));
	}
	
	Pattern mBloodPattern = Pattern.compile("(.*[0-9]+)!(.*)");
    protected String formatCriticalResults(String [] results)
    {
    	StringBuffer sb = new StringBuffer();
    	if (results == null)
    	{
    		sb.append("No results");
    	}
    	else
    	{
    		for (String res:results)
    		{
    			if (sb.length() > 0)
    				sb.append("<br>");
    			// first replace the easy ones
    			res = res.replaceAll("\\*", "<img class=\"mod\" src=\"/res/stunned.png\" />").
    					replaceAll("@", "<img class=\"mod\" src=\"/res/noparry.png\" />");
    			
        		final Matcher m = mBloodPattern.matcher(res);
    			if (m.matches())
    			{
    				res = m.group(1) + "<img class=\"mod\" src=\"/res/blood.png\" />" + m.group(2);
    			}
    			sb.append(res);
    		}
    	}
    		
    	return sb.toString();
    }
}
