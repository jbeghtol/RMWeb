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
		return new AttackLookup();
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
	
	class AttackLookup implements ActionHandler
	{
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response)
		{
			final String roll = request.getParameter("roll");
			final String windex = request.getParameter("weap");
			final String at = request.getParameter("at");
			final String attacker = request.getParameter("attacker");
			final String defender = request.getParameter("defender");
			final String validityStr = request.getParameter("validity");
			final String explain = request.getParameter("explain");
			final int validity = validityStr==null?3:Integer.parseInt(validityStr);

			if (roll == null || windex == null || at == null)
			{
				response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
				return;
			}
			
			try {
				final CombatEngineSQLite engine = getEngine();
				DamageResult dam = engine.getDamage(Integer.parseInt(roll), Integer.parseInt(windex), Integer.parseInt(at));
				
				String msg;
				if (dam.iDamage < 0) {
					msg = "FUMBLE";
				} else if (dam.iDamage > 0) {
					msg = "HIT " + dam.iDamage + " " + dam.iCriticals;
				} else {
					msg = "MISS";
				}
				
				if (validity < VALIDITY_PRACTICE) {
					String header = String.format("[%s] Attack%s [%s] : %s = %s", attacker==null?"???":attacker,
							validity==VALIDITY_COMPUTER?"":"*",
							defender==null?"???":defender,
							explain, roll);
					SimpleEventList.getInstance().postEvent(new SimpleEvent(msg, 
							header,	"rmattack", user.mUsername));
				}
				
				JSONObject p = new JSONObject();
				p.put("hits", dam.iDamage);
				p.put("criticals", dam.iCriticals);
				
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
			final String stringroll = request.getParameter("roll");
			final String crits = request.getParameter("crits");
			final String attacker = request.getParameter("attacker");
			final String defender = request.getParameter("defender");
			final String validityStr = request.getParameter("validity");
			final int validity = validityStr==null?3:Integer.parseInt(validityStr);
			
			if (stringroll == null || crits == null)
			{
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			final int roll = Integer.parseInt(stringroll);
			try {
				final String innerHtml = getEngine().getCriticalsAsHtml(roll, crits);
				
				if (validity < VALIDITY_PRACTICE) {
					String header = String.format("[%s] Critical%s [%s] : %s : (%d)", attacker==null?"???":attacker,
							validity==VALIDITY_COMPUTER?"":"*",
							defender==null?"???":defender,
							crits, roll);
					SimpleEventList.getInstance().postEvent(new SimpleEvent(innerHtml, 
							header,
							"rmattack", user.mUsername));
				}
				
				JSONObject p = new JSONObject();
				p.put("effects", innerHtml);
				
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
