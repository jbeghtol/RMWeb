package com.ilsian.rmweb;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ilsian.rmweb.CombatEngineSQLite.CriticalResults;
import com.ilsian.rmweb.EntityEngineSQLite.ActiveEntity;
import com.ilsian.rmweb.EntityEngineSQLite.Skill;
import com.ilsian.tomcat.ActionHandler;
import com.ilsian.tomcat.UserInfo;
import com.ilsian.tomcat.WebLib;

public class ActiveEntities extends HashMap<String, ActiveEntity> implements ActionHandler
{
	long changedTime = System.currentTimeMillis();
	int currentRound = 1;
	int currentStage = 0;
	int activeUid = 0;
	String lastSkillCheck="Result";
	
	static String [] mMasterWeaponList = null;
	static ActiveEntities instance_ = null;
	
	public static void init(String [] mlist) {
		mMasterWeaponList = mlist;
		instance_ = new ActiveEntities();
	}
	
	public static ActiveEntities instance() {
		return instance_;
	}
	
	public ActiveEntities() {
		// when loading, we start with all 'public' entities, which are 'players'
		try {
			EntityEngineSQLite.getInstance().queryToMap(null, this, mMasterWeaponList, null, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void refreshActiveFromDB() {
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			try {
				return EntityEngineSQLite.getInstance().queryUpdateMap(this, mMasterWeaponList);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			return false;
		});
	}

	public void setGroupActive(int entuid, boolean active, boolean hidden) {
		// Activate all players in this name's group
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			try {
				String tag = EntityEngineSQLite.getInstance().queryGroup(entuid);
				if (tag == null)
					return false;
				
				if (!active) {
					return (this.values().removeIf( entry -> (entry.mTag.equals(tag))));
				} else {
					return EntityEngineSQLite.getInstance().queryToMap(null, this, mMasterWeaponList, tag, hidden);
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			return false;
		});
	}
	
	public void setActive(String name, boolean active, boolean hidden) {
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			if (!active) {
				ActiveEntity ent = this.get(name);
				if (ent != null) {
					this.remove(name);
					return true;
				}
			} else {
				ActiveEntity ent = this.get(name);
				if (ent == null) {
					try {
						return EntityEngineSQLite.getInstance().queryToMap(name, this, mMasterWeaponList, null, hidden);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					if (ent.mVisible == hidden) {
						ent.mVisible = !hidden;
						return true;
					}
				}
			}
			return false;
		});
	}
	
	public void deleteEntity(int uid) {
		try {
			String name = EntityEngineSQLite.getInstance().queryName(uid);
			if (name != null) {
				// first, remove them from active just in case
				setActive(name, false, false);
				// now delete the bugger
				EntityEngineSQLite.getInstance().delete(uid);
			}
		} catch (Exception sqe) {
			sqe.printStackTrace();
		}
	}
	
	public void deleteEntityGroup(int uid) {
		try {
			// first, remove them all from active just in case
			setGroupActive(uid, false, false);
			// now delete the buggers
			EntityEngineSQLite.getInstance().deleteGroup(uid);
		} catch (Exception sqe) {
			sqe.printStackTrace();
		}
	}
	
	public void rollInitiative() {
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			currentStage = 1;
			for (ActiveEntity entry:this.values()) {
				int d1 = Dice.roll(10);
				int d2 = Dice.roll(10);
				entry.mLastInit = entry.mFirstStrike + d1 + d2;
				entry.mLastInitExplain = entry.mFirstStrike + " + (" + d1 + " + " + d2 + ") = " + entry.mLastInit;
				// for sort, snap phases before normal before delib
				entry.mLastInitSort = (entry.mLastInitPhase - 1) * -100 + entry.mLastInit;
			}
			return true;
		});
	}

	class SkillResult {
		public Dice.Open roll_;
		public int total_;
		public String explain_;
		public String player_;
		public String skillName_;
		public String detail_;
		public boolean valid_;
	}
	
	/*
	protected void doSkillCheckOBS(Skill lookup, String skillName, UserInfo user) {
		if (lookup.mTotal > 0) {
			int roll = Dice.rollOpenPercent();
			int total = roll + lookup.mTotal;
			String explain = lookup.mTotal + " + (" + roll + ") = " + total;
			String header = String.format("[%s] Skill Check '%s' : %s", 
					lookup.mOwner, lookup.mDisplayName, explain);				
			SimpleEventList.getInstance().postEvent(new SimpleEvent("Total = " + total, 
					header,	"rmskill", user.mUsername));
		}
	}
	*/
	
	static final int [] STUN_MOD_TABLE = { 0, 0, -10, -20, -20, -30, -30, -30, -50, -50, -70  };
	static final HashMap<SkillResolve.General, Integer> STUN_REDUCE;
	static {
		STUN_REDUCE = new HashMap();
		STUN_REDUCE.put(SkillResolve.General.NEAR_SUCCESS, -1);
		STUN_REDUCE.put(SkillResolve.General.SUCCESS, -2);
		STUN_REDUCE.put(SkillResolve.General.ABSOLUTE_SUCCESS, -3);
		STUN_REDUCE.put(SkillResolve.General.UNUSUAL_SUCCESS, -5);
	}
	
	protected boolean resolveSkill(ActiveEntity entity, Skill skill, SkillResult res, String skillName) {
		boolean changedEntity = false;
		res.roll_ = Dice.rollOpen();
		res.total_ = res.roll_.total_ + skill.mTotal;
		res.player_ = entity.mName;
		res.skillName_ = skill.mDisplayName;
		res.explain_ = skill.mTotal + " + " + res.roll_ + " = " + res.total_;
		
		if (Global.USE_COMBAT_TRACKER && skillName.equals("breakstun")) {
			// lookup the stun mod
			final int stunMod = STUN_MOD_TABLE[Math.min(Math.max(0, entity.mEffects.stun_), STUN_MOD_TABLE.length-1)];
			if (stunMod < 0) {
				res.total_ += stunMod;
				res.explain_ = skill.mTotal + " - " + -stunMod + " + " + res.roll_ + " = " + res.total_;
			}
			
			// check if it worked
			SkillResolve.General checkResult = SkillResolve.General.resolve( new Dice.Open(res.roll_, res.total_) );
			Integer stunReduce = STUN_REDUCE.get(checkResult);
			int reduce = stunReduce==null?0:stunReduce;
			res.detail_ = "Total = " + res.total_ + " " + checkResult + " (" + (stunReduce==null?"No Effect":String.format("%d rounds", stunReduce)) + ")";
			
			// perform actual stun reduction
			int finalStun = Math.max(0, entity.mEffects.stun_ + reduce);
			if (finalStun != entity.mEffects.stun_) {
				entity.mEffects.stun_ = finalStun;
				changedEntity = true;
			}
			// and we also remove noparry, thx!
			int finalNoparry = Math.max(0, entity.mEffects.noParry_ + reduce);
			if (finalNoparry != entity.mEffects.noParry_) {
				entity.mEffects.noParry_ = finalNoparry;
				changedEntity = true;
			}
		} else {
			// Normal flow, any skill, maybe figure out success level
			//SkillResolve.General checkResult = SkillResolve.General.resolve( new Dice.Open(res.roll_, res.total_) );
			String multiResult = SkillResolve.General.multiResolve( new Dice.Open(res.roll_, res.total_) );
			res.detail_ = "Total = " + res.total_ + " " + multiResult;
		}
		res.valid_ = true;
		return changedEntity;
	}
	
	public void singleSkillCheck(UserInfo user, String skillName, int entityUID) {
		final SkillResult skillRes = new SkillResult();
		
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			for (ActiveEntity entry:this.values()) {
				if (entry.mUid != entityUID)
					continue;
				
				Skill sk = entry.mSkills.get(skillName);
				if (sk != null) {
					return resolveSkill(entry, sk, skillRes, skillName);
				}
				break;
			}
			return false;
		});

		if (skillRes.valid_) {
			String header = String.format("[%s] Skill Check '%s' : %s", 
					skillRes.player_, skillRes.skillName_, skillRes.explain_);				
			SimpleEventList.getInstance().postEvent(new SimpleEvent(skillRes.detail_, 
					header,	"rmskill", user.mUsername));
		}
	}
	
	public void customSkillCheck(UserInfo user, String skillName, int entityUID, int skillBase) {
		final Skill lookup = new Skill();
		lookup.mTotal = skillBase;
		
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			for (ActiveEntity entry:this.values()) {
				if (entry.mUid != entityUID)
					continue;
				
				lookup.mDisplayName = skillName;
				lookup.mOwner = entry.mName;
				break;
			}
			return false;
		});

		if (skillBase >= 0) {
			Dice.Open roll = Dice.rollOpen();
			int total = roll.total_ + lookup.mTotal;
			String explain = lookup.mTotal + " + " + roll + " = " + total;
			String header = String.format("[%s] %s %s : %s", 
					lookup.mOwner, lookup.mDisplayName==null?"Quick Roll":"Skill Check '", lookup.mDisplayName==null?"":(lookup.mDisplayName + "'"), explain);				
			SimpleEventList.getInstance().postEvent(new SimpleEvent("Total = " + total, 
					header,	"rmskill", user.mUsername));
		}
	}
	
	public void groupSkillCheck(String skillName) {
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			for (ActiveEntity entry:this.values()) {
				int baseSkill = 0;
				Skill sk = entry.mSkills.get(skillName);
				if (sk != null) {
					lastSkillCheck = sk.mDisplayName;
					baseSkill = sk.mTotal;
				}
				if (baseSkill == 0) {
					entry.mLastResult = 0;
					entry.mLastResultExplain = "No skill";
				} else {
					Dice.Open roll = Dice.rollOpen();
					entry.mLastResult = baseSkill + roll.total_;
					entry.mLastResultExplain = baseSkill + " + " + roll + " = " + entry.mLastResult;
				}
			}
			return true;
		});
	}
	
	public String applyDamage(String attacker, String defender, int baseDamage, CriticalResults effect) {
		final StringBuilder summary = new StringBuilder();
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			boolean anyApplied = false;
			ActiveEntity att = this.get(attacker);
			ActiveEntity def = this.get(defender);
			if (effect != null && effect.details_ != null) {
				for (EffectRecord r:effect.details_) {
					if (att != null) {
						att.mEffects.merge(r, false);
						anyApplied = true;
					}
					if (def != null) {
						def.mEffects.merge(r, true);
						anyApplied = true;
					}
				}
			}
			if (def != null && baseDamage > 0) {
				def.mEffects.damage_ += baseDamage;
				anyApplied = true;
			}
			if (def != null) {
				summary.append(def.mEffects.toString());
			}
			return anyApplied;
		});
		return summary.toString();
	}
	
	public String advanceRound() {
		final StringBuilder summary = new StringBuilder();
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			currentStage = 0;
			currentRound++;
			for (ActiveEntity entry:this.values()) {
				entry.mLastInit = -1;
				entry.mLastInitExplain = "";
				if (entry.mEffects.applyRound()) {
					if (Global.USE_COMBAT_TRACKER) {
						// We could add this to the status - all entities who change from a round inc
						if (summary.length() > 0)
							summary.append("<br>");
						summary.append("[");
						summary.append(entry.mName);
						summary.append("]: ");
						summary.append(entry.mEffects.getDetail());
					}
				}
			}
			return true;
		});
		return summary.toString();
	}
	
	public void setInitPhase(int uid, int phase) {
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			for (ActiveEntity entry:this.values()) {
				if (entry.mUid == uid) {
					entry.mLastInitPhase = phase;
					return true;
				}
			}
			return false;
		});
	}
	
	public void resetRounds() {
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			currentStage = 0;
			currentRound = 1;
			lastSkillCheck = "Result";
			for (ActiveEntity entry:this.values()) {
				entry.mLastInit = -1;
				entry.mLastInitExplain = "";
				entry.mLastResult = 0;
				entry.mLastResultExplain = "Reset";
				entry.mEffects = new EffectRecord();
			}
			return true;
		});
	}
	
	public void toggleVisible(int uid) {
		ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
			for (ActiveEntity entry:this.values()) {
				if (entry.mUid == uid) {
					entry.mVisible = !entry.mVisible;
					return true;
				}
			}
			return false;
		});
	}
	
	public JSONObject reportIfNeeded(UserInfo user, long lastKnown) throws JSONException {
		return ModelSync.extractModel(ModelSync.Model.ENTITIES, new ModelSync.DataModel() {
			@Override
			public void extractModelData(JSONObject outData, long modelTime) throws JSONException {
				if (modelTime != lastKnown) {
					JSONArray list = new JSONArray();
					for (String val:keySet()) {
						ActiveEntity actor = get(val);
						if (actor.mVisible || user.mLevel >= RMUserSecurity.kLoginGM)
							list.put(actor.toJSON());
					}
					outData.put("records", list);
					outData.put("round", currentRound);
					outData.put("stage", currentStage);
					outData.put("lastskill", lastSkillCheck);
				}
			}
		});
	}
	
	@Override
	public void handleAction(String action, UserInfo user,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (action.equals("rollinit") && user.mLevel >= RMUserSecurity.kLoginLeader) {
			rollInitiative();
			SimpleEventList.getInstance().postEvent(new SimpleEvent("Today is a good day to die.", "[GM] Round " + currentRound + " begins!", "rmgmaction", user.mUsername));
		}
		else if (action.equals("nextround") && user.mLevel >= RMUserSecurity.kLoginLeader) {
			String summary = advanceRound();
			groupSkillCheck("combatawareness");
			SimpleEventList.getInstance().postEvent(new SimpleEvent(summary.length() == 0?"Choose a phase and declare your actions.":summary, "[GM] Round " + currentRound + " declarations/updates", "rmgmaction", user.mUsername));
		} else if (action.equals("setphase")) {
			// has params: uid - entitiy uid, phase - next init phase in -1,0,1
			// TODO: Security! Client can lie, so we should check controllers for the uid, but skip isnt that good of a hacker so LATER
			setInitPhase(Integer.parseInt(request.getParameter("uid")), Integer.parseInt(request.getParameter("phase")));
		} else if (action.equals("skillcheck") && user.mLevel >= RMUserSecurity.kLoginLeader) {
			groupSkillCheck(request.getParameter("skill"));
		} else if (action.equals("skillsingle")) {
			singleSkillCheck(user, request.getParameter("skill"), WebLib.getIntParam(request, "uid", -1));
		} else if (action.equals("skillcustom")) {
			customSkillCheck(user, request.getParameter("skill"), WebLib.getIntParam(request, "uid", -1), WebLib.getIntParam(request, "base", 0));
		} else if ((action.equals("activate") || action.equals("activatepeer")) && user.mLevel >= RMUserSecurity.kLoginGM) {
			try {
				if (action.equals("activate")) {
					String name = EntityEngineSQLite.getInstance().queryName(WebLib.getIntParam(request, "uid", -1));
					setActive(name, true, WebLib.getBoolParam(request, "hidden", false));
				}
				else {
					setGroupActive(WebLib.getIntParam(request, "uid", -1), true, WebLib.getBoolParam(request, "hidden", false));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ((action.equals("deactivate") || action.equals("deactivatepeer")) && user.mLevel >= RMUserSecurity.kLoginGM) {
			try {
				if (action.equals("deactivate")) {
					String name = EntityEngineSQLite.getInstance().queryName(WebLib.getIntParam(request, "uid", -1));
					setActive(name, false, false);
				}
				else {
					setGroupActive(WebLib.getIntParam(request, "uid", -1), false, WebLib.getBoolParam(request, "hidden", false));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (action.equals("toggleVisible") && user.mLevel >= RMUserSecurity.kLoginGM) {
			toggleVisible(WebLib.getIntParam(request, "uid", -1));
		} else if (action.equals("delete") && user.mLevel >= RMUserSecurity.kLoginGM) {
			deleteEntity(WebLib.getIntParam(request, "uid", -1));
		} else if (action.equals("deletegroup") && user.mLevel >= RMUserSecurity.kLoginGM) {
			deleteEntityGroup(WebLib.getIntParam(request, "uid", -1));
		}
	}
	
}
