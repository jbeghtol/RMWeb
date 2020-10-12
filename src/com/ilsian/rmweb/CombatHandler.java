package com.ilsian.rmweb;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;



import com.ilsian.tomcat.ActionHandler;
import com.ilsian.tomcat.UserInfo;
import com.ilsian.tomcat.WebLib;


public class CombatHandler {

	public static final int VALIDITY_MANUAL = 0;
	public static final int VALIDITY_COMPUTER = 1;
	public static final int VALIDITY_PRACTICE = 2;
	
	CombatEngineSQLite mEngine = null;

	public CombatEngineSQLite getEngine() throws Exception
	{
		if (mEngine == null)
		{
			mEngine = new CombatEngineSQLite();
			mEngine.open();
		}
		return mEngine;
	}

	public ActionHandler makeHandlerTable() {
		return new TableLookup();
	}
	
	public ActionHandler makeHandlerAttack() {
		return new AttackLookupServer();
	}
	
	public ActionHandler makeHandlerCritical() {
		return new CritLookup();
	}
	
	public class TableLookup implements ActionHandler
	{
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response)
		{
			try {
				final CombatEngineSQLite engine = getEngine();
			
				JSONArray wlist = new JSONArray();
				String [] weapons = engine.getWeaponList();
				for (String w:weapons)
				{
					wlist.put(w);
				}
				JSONArray clist = new JSONArray();
				String [] crits = engine.getCriticalList();
				for (String c:crits)
				{
					clist.put(c);
				}
				
				JSONArray atlist = new JSONArray();
				for (int i=1;i<=20;i++)
					atlist.put(i);
				
				JSONObject p = new JSONObject();
				p.put("weapons", wlist);
				p.put("criticals", clist);
				p.put("armors", atlist);
				
				response.setStatus( HttpServletResponse.SC_OK );
				response.setContentType("application/json");
				PrintWriter outs = response.getWriter();
				outs.print(p.toString());
				outs.flush();
				outs.close();
			} catch (Exception exc) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

		} 
	}
	
	class AttackLookupServer implements ActionHandler
	{
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response)
		{
			// In this version, we are provided all the details to create explain
			final int ob = WebLib.getIntParam(request, "ob", 0);
			final int parry = WebLib.getIntParam(request, "parry", 0);
			final int db = WebLib.getIntParam(request, "db", 0);
			final int mods = WebLib.getIntParam(request, "mods", 0);
			
			final int userRoll = WebLib.getIntParam(request, "roll", -1000);
			
			final int windex = WebLib.getIntParam(request, "weap", 0);
			final int at = WebLib.getIntParam(request, "at", 1);
			final String attacker = request.getParameter("attacker");
			final String defender = request.getParameter("defender");
			final String validityStr = request.getParameter("validity");
			final String critVsLarge = WebLib.getStringParam(request, "largecrit", "A");
			final int reduceCrits = WebLib.getIntParam(request, "reducecrit", 0);
			final int sizeClass = WebLib.getIntParam(request, "size", 0);
			
			final int rankLimit = WebLib.getIntParam(request, "ranklimit", 3);
			final int validity = validityStr==null?3:Integer.parseInt(validityStr);

			// DICE!
			Dice.Open roll = Dice.rollOpen();
			if (userRoll > -1000) {
				roll.manual(userRoll);
			}
			
			int summation = ob + mods - db - parry + roll.total_;
			String explain = String.format("%d + %d - %d - %d + %s", ob, mods, db, parry, roll, summation);
			
			try {
				final CombatEngineSQLite engine = getEngine();
				DamageResult dam = engine.getDamage(roll.base_, summation, windex, at, rankLimit);
				
				String msg;
				if (dam.iDamage < 0) {
					msg = "FUMBLE";
					dam.iDamage = 0;
				} else if (dam.iDamage > 0) {
					if (dam.checkReduceCriticals(reduceCrits, sizeClass, critVsLarge)) {
						msg = "HIT " + dam.iDamage + " " + dam.iCriticals + " (Downgraded from " + dam.iOGCrits + ")";
					} else {
						msg = "HIT " + dam.iDamage + " " + dam.iCriticals;
					}
				} else {
					msg = "MISS";
				}
				
				if (validity < VALIDITY_PRACTICE) {
					String rankExplain = "";
					if (dam.iRankLimit > 0) {
						rankExplain = String.format(" [R%d]", rankLimit + 1);
					}
					String header = String.format("[%s] Attack%s [%s] : %s = %s%s", attacker==null?"???":attacker,
							validity==VALIDITY_COMPUTER?"":"*",
							defender==null?"???":defender,
							explain, summation, rankExplain);
					SimpleEventList.getInstance().postEvent(new SimpleEvent(msg, 
							header,	"rmattack", user.mUsername));
				}
				
				JSONObject p = new JSONObject();
				p.put("hits", dam.iDamage);
				p.put("criticals", dam.iCriticals);
				p.put("roll", roll.total_);
				p.put("summation", summation);
				p.put("explain", explain);
				
				response.setStatus( HttpServletResponse.SC_OK );
				response.setContentType("application/json");
				PrintWriter outs = response.getWriter();
				outs.print(p.toString());
				outs.flush();
				outs.close();
			} catch (Exception exc) {
				exc.printStackTrace();
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

		} 
	}
	
	class CritLookup implements ActionHandler
	{

		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response)
		{
			final int userRoll = WebLib.getIntParam(request, "roll", -1000);
			final String crits = request.getParameter("crits");
			final String attacker = request.getParameter("attacker");
			final String defender = request.getParameter("defender");
			final String validityStr = request.getParameter("validity");
			final int validity = validityStr==null?3:Integer.parseInt(validityStr);
			
			// DICE!
			Dice.Open dice = Dice.rollOpen(false);
			if (userRoll > -1000) {
				dice.manual(userRoll);
			}
			
			if (crits == null)
			{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			try {
				final String innerHtml = getEngine().getCriticalsAsHtml(dice, crits);
				
				if (validity < VALIDITY_PRACTICE) {
					String header = String.format("[%s] Critical%s [%s] : %s : (%d)", attacker==null?"???":attacker,
							validity==VALIDITY_COMPUTER?"":"*",
							defender==null?"???":defender,
							crits, dice.expressedRoll());
					SimpleEventList.getInstance().postEvent(new SimpleEvent(innerHtml, 
							header,
							"rmattack", user.mUsername));
				}
				
				JSONObject p = new JSONObject();
				p.put("effects", innerHtml);
				p.put("roll", dice.expressedRoll());
				response.setStatus( HttpServletResponse.SC_OK );
				response.setContentType("application/json");
				PrintWriter outs = response.getWriter();
				outs.print(p.toString());
				outs.flush();
				outs.close();
			} catch (Exception exc) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}			
		}
		
	}
}
