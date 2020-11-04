package com.ilsian.rmweb;

import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ilsian.rmweb.CombatEngineSQLite.CriticalResults;
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

	WoundDB mWoundDB = new WoundDB();
	
	public ActionHandler makeHandlerTable() {
		return new TableLookup();
	}
	
	public ActionHandler makeHandlerAttack() {
		return new AttackLookupServer();
	}
	
	public ActionHandler makeHandlerCritical() {
		return new CritLookup();
	}
	
	public ActionHandler makeHandlerBAR() {
		return new BARLookup();
	}

	public ActionHandler makeHandlerRR() {
		return new RRLookup();
	}
	
	public WoundDB getWoundDB() {
		return mWoundDB;
	}
	
	public static class PendingWound {
		public String attacker_;
		public String defender_;
		public int damage_;
		public CriticalResults crits_;
		public PendingWound(String attacker, String defender, int damage, CriticalResults res) {
			attacker_ = attacker; defender_ = defender; damage_ = damage; crits_ = res;
		}
		
		public void apply(boolean notify, String user, int uid) {
			String update = ActiveEntities.instance().applyDamage(attacker_, defender_, damage_, crits_);
			if (notify)
				SimpleEventList.getInstance().postEvent(new SimpleEvent(
						String.format("<i>[%s]:%s</i>", defender_, update), 
						String.format("[%s] Damage applied", defender_),
						"rmgmaction", user).setDbInvalidate(uid));
		}
	
		public void applyNext(boolean notify, String user, int uid) {
			// bump up any durations by 1
			if (crits_.details_ != null)
				for (EffectRecord e:crits_.details_)
					e.bump();
			String update = ActiveEntities.instance().applyDamage(attacker_, defender_, damage_, crits_);
			if (notify)
				SimpleEventList.getInstance().postEvent(new SimpleEvent(
						String.format("<i>[%s]:%s</i>", defender_, update), 
						String.format("[%s] Damage applied (next round)", defender_),
						"rmgmaction", user).setDbInvalidate(uid));
		}
		
		public void reportCleared(String user, int uid) {
			SimpleEventList.getInstance().postEvent(new SimpleEvent(
					"", 
					String.format("[%s] Damage canceled", defender_),
					"rmhidden", user).setDbInvalidate(uid));
		}
	}
	
	public class WoundDB implements ActionHandler {
		HashMap<Integer,PendingWound> pendingWounds_ = new HashMap();
		int nextWound_ = 0;
		
		public int submit(PendingWound p) {
			if (Global.USE_AFFIRMATIVE_TRACKER) {
				synchronized(this) {
					nextWound_++;
					pendingWounds_.put(nextWound_, p);
					return nextWound_;
				}
			} else {
				p.apply(false, null, -1);
				return -1;
			}
		}
		
		public void reset() {
			synchronized(this) {
				pendingWounds_.clear();
			}
		}
		
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response)
		{
			if (user.mLevel < RMUserSecurity.kLoginLeader)
				return;
			
			//now, next, cancel, edit
			final String op = WebLib.getStringParam(request, "dispatch", null);
			if (op == null) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			
			final int uid = WebLib.getIntParam(request, "uid", -1); 
			PendingWound p = null;
			synchronized(this) {
				p = pendingWounds_.remove(uid);
			}
			if (p == null) {
				// wound already dispatched, just ignore
				return;
			}
			
			if (op.equals("now")) {
				p.apply(true, user.mUsername, uid);
			} else if (op.equals("next")) {
				p.applyNext(true, user.mUsername, uid);
			} else if (op.equals("cancel") || op.equals("edit")) {
				// do nothing, we already removed it, yay.. edit is client side
				p.reportCleared(user.mUsername, uid);
			} else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		}
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
			final int att_cond = WebLib.getIntParam(request, "att_cond", 0);
			final int def_cond = WebLib.getIntParam(request, "def_cond", 0);
			
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
			
			int summation = ob + att_cond + mods - db - parry + def_cond + roll.total_;
			String explain = String.format("%d + %d + %d - %d - %d + %d + %s", ob, att_cond, mods, db, parry, def_cond, roll, summation);
			
			try {
				final CombatEngineSQLite engine = getEngine();
				DamageResult dam = engine.getDamage(roll.base_, summation, windex, at, rankLimit);
				
				String msg;
				String resclass;
				
				if (dam.iDamage < 0) {
					msg = "FUMBLE";
					dam.iDamage = 0;
					resclass = "fumble";
				} else if (dam.iDamage > 0) {
					if (dam.checkReduceCriticals(reduceCrits, sizeClass, critVsLarge)) {
						msg = "HIT " + dam.iDamage + " " + dam.iCriticals + " (Downgraded from " + dam.iOGCrits + ")";
					} else {
						msg = "HIT " + dam.iDamage + " " + dam.iCriticals;
					}
					resclass = "hit";
				} else {
					msg = "MISS";
					resclass = "miss";
				}
				
				if (validity < VALIDITY_PRACTICE) {
					String rankExplain = "";
					if (dam.iRankLimit > 0) {
						rankExplain = String.format(" [R%d]", rankLimit + 1);
					}
					int woundOpts = -1;
					if (Global.USE_COMBAT_TRACKER && !defender.isEmpty()) {
						if (dam.iDamage > 0) {
							if (Global.USE_AFFIRMATIVE_TRACKER)
								woundOpts = mWoundDB.submit(new PendingWound(attacker, defender, dam.iDamage, null));
							else
								ActiveEntities.instance().applyDamage(attacker, defender, dam.iDamage, null);
						}
					}
					
					String header = String.format("[%s] Attack%s [%s] : %s = %s%s", attacker==null?"???":attacker,
							validity==VALIDITY_COMPUTER?"":"*",
							defender==null?"???":defender,
							explain, summation, rankExplain);
					SimpleEventList.getInstance().postEvent(new SimpleEvent(msg, 
							header,	"rmattack", user.mUsername).setDbOpt(woundOpts).setTargetName(defender));
				}
				
				JSONObject p = new JSONObject();
				p.put("hits", dam.iDamage);
				p.put("criticals", dam.iCriticals);
				p.put("roll", roll.total_);
				p.put("summation", summation);
				p.put("explain", explain);
				p.put("result", resclass);
				
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
			String attacker = request.getParameter("attacker");
			String defender = request.getParameter("defender");
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
				String critMod = "";
				if (getEngine().hasReverseCritical(crits)) {
					// special handling for fumbles, switch the attacker and defender
					String tmp = attacker;
					attacker = defender;
					defender = tmp;
					critMod = "Reverse-";
				}
				
				final CriticalResults cres = getEngine().getCriticalResults(dice, crits);
				String innerHtml = cres.html_;
				String resHtml = innerHtml;
				if (validity < VALIDITY_PRACTICE) {
					// apply the effects
					int woundOpts = -1;
					if (Global.USE_COMBAT_TRACKER && !defender.isEmpty()) {
						if (Global.USE_AFFIRMATIVE_TRACKER) {
							woundOpts = mWoundDB.submit(new PendingWound(attacker, defender, 0, cres));
						} else {
							String defStatus = ActiveEntities.instance().applyDamage(attacker, defender, 0, cres);
							if (!defStatus.isEmpty()) {
								innerHtml += "<br><i>[" + defender + "]:" + defStatus + "</i>";
							}
						}
					}
					String header = String.format("[%s] %sCritical%s [%s] : %s : (%d)", attacker==null?"???":attacker,
							critMod,validity==VALIDITY_COMPUTER?"":"*",
							defender==null?"???":defender,
							crits, dice.expressedRoll());
					SimpleEvent eve = new SimpleEvent(innerHtml, 
							header,
							"rmattack", user.mUsername).setDbOpt(woundOpts).setTargetName(defender);
					SimpleEventList.getInstance().postEvent(eve);
				}
				
				JSONObject p = new JSONObject();
				p.put("effects", resHtml);
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
	
	class BARLookup implements ActionHandler
	{
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response)
		{
			// In this version, we are provided all the details to create explain
			final int userRoll = WebLib.getIntParam(request, "roll", -1000);
			final int mods = WebLib.getIntParam(request, "mods", 0);
			final int base = WebLib.getIntParam(request, "base", 0);
			String attacker = request.getParameter("attacker");
			String defender = request.getParameter("defender");
			final String validityStr = request.getParameter("validity");
			final int validity = validityStr==null?3:Integer.parseInt(validityStr);
			
			// DICE!
			Dice.Open dice = Dice.rollOpen(false);
			if (userRoll > -1000) {
				dice.manual(userRoll);
			}

			// use the dice total to lookup the result
			dice.total_ = base + mods + dice.base_;
			String expl = base + " + " + mods + " + (" + dice.base_ + ")";
			
			int resultMod = SkillResolve.BAR(dice);
		
			try {
				String innerHtml;
				JSONObject p = new JSONObject();
				p.put("roll", dice.base_);
				p.put("explain", expl);
				p.put("summation", dice.total_);
				if (resultMod == Integer.MAX_VALUE) {
					// fumble
					p.put("error", "FAIL");
					innerHtml = "SPELL FAIL";
				} else {
					// normal modifier
					p.put("modifier", resultMod);
					innerHtml = "RR Modifier: " + resultMod;
				}
				
				if (validity < VALIDITY_PRACTICE) {
					String header = String.format("[%s] BAR%s vs [%s] : %s = %d", 
							attacker==null?"???":attacker,
							validity==VALIDITY_COMPUTER?"":"*",
							defender==null?"???":defender,
							expl, dice.total_);
					SimpleEventList.getInstance().postEvent(new SimpleEvent(innerHtml, 
							header,
							"rmattack", user.mUsername));
				}
				
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
	
	private int divUp(int numberOfObjects, int pageSize) {
	    return numberOfObjects / pageSize + (numberOfObjects % pageSize == 0 ? 0 : 1);
	}
	
	class RRLookup implements ActionHandler
	{
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response)
		{
			// In this version, we are provided all the details to create explain
			final int userRoll = WebLib.getIntParam(request, "roll", -1000);
			final int mods = WebLib.getIntParam(request, "mods", 0);
			final int rrbonus = WebLib.getIntParam(request, "rr_bonus", 0);
			final int lvlAttacker = WebLib.getIntParam(request, "level_att", 1);
			final int lvlDefender = WebLib.getIntParam(request, "level_def", 1);
			String attacker = request.getParameter("attacker");
			String defender = request.getParameter("defender");
			// TODO: get rr_effect
			
			final String validityStr = request.getParameter("validity");
			final int validity = validityStr==null?3:Integer.parseInt(validityStr);
			
			// DICE!
			Dice.Open dice = Dice.rollOpen(false);
			if (userRoll > -1000) {
				dice.manual(userRoll);
			}

			// Determine TARGET
			int target = SkillResolve.RRTarget(lvlAttacker, lvlDefender);
			
			// use the dice total to lookup the result
			int total = mods + rrbonus + dice.total_;
			int delta = total - target;
			String expl = mods + " + " + rrbonus + " + (" + dice.base_ + ")";

			try {
				String innerHtml;
				JSONObject p = new JSONObject();
				p.put("roll", dice.base_);
				p.put("explain", expl);
				p.put("target", target);
				p.put("summation", total);
				p.put("delta", delta);
				
				if (delta < 0) {
					// defender fails!
					int perFive = divUp(-delta, 5);
					int perTen = divUp(-delta, 10);
					innerHtml = String.format("FAIL by %d (Fives: %d, Tens: %d)", delta, perFive, perTen);
				} else {
					// defender saves!
					innerHtml = "RESISTS by " + delta;
				}
				p.put("result", innerHtml);
				
				if (validity < VALIDITY_PRACTICE) {
					String header = String.format("[%s] RR%s for [%s] : %s = %d", 
							attacker==null?"???":attacker,
							validity==VALIDITY_COMPUTER?"":"*",
							defender==null?"???":defender,
							expl, total);
					SimpleEventList.getInstance().postEvent(new SimpleEvent(innerHtml, 
							header,
							"rmattack", user.mUsername));
				}
				
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
}
