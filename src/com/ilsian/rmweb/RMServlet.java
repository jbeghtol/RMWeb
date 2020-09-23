package com.ilsian.rmweb;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ilsian.rmweb.EntityEngineSQLite.ActiveEntity;
import com.ilsian.rmweb.EntityEngineSQLite.Skill;
import com.ilsian.tomcat.ActionHandler;
import com.ilsian.tomcat.AppServlet;
import com.ilsian.tomcat.UserInfo;
import com.ilsian.tomcat.WebLib;

public class RMServlet extends AppServlet {

	static final int MAX_HISTORY = 50;
	static Logger logger = Logger.getLogger("com.ilsian.rmweb.RMServlet");
	
	public RMServlet(RMUserSecurity userModel, String routeParam) {
		super(userModel, routeParam);
	}

	@Override
	public HashMap createDataMap(UserInfo user, HttpServletRequest request) {
		HashMap hmap = new HashMap();
		HashMap rm = new HashMap();
		// this message is rendered inside the HelloWorld.ftl template
		hmap.put("message", "RM Web");
		hmap.put("rm", rm);
		if (user.mLevel > 0)
			rm.put("user", user.mUsername);
		rm.put("permit", user.mLevel);
		rm.put("hideback", true);
		
		// add any other rm data members
		return hmap;
	}

	ActionHandler shutdownHandler = new ActionHandler() {

		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			if (action.equals("shutdown")) {
				logger.info("Shuting down by user request");
				new Thread() {
					public void run() {
						try {
							Main.shutDown();
							Thread.sleep(500);
						} catch (Exception ignored) {
							
						}
						System.exit(0);
					}
			
				}.start();
			}
			
		}
		
	};
	
	final ActionHandler mLogoutHandler = new ActionHandler() {
		@Override
		public void handleAction(String action, UserInfo user,
				HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			((RMUserSecurity)_userModel).logout(request);
			response.sendRedirect("gui");
		}
	};

	class ActiveEntities extends HashMap<String, ActiveEntity> implements ActionHandler
	{
		long changedTime = System.currentTimeMillis();
		int currentRound = 1;
		int currentStage = 0;
		int activeUid = 0;
		String lastSkillCheck="Result";
		
		public ActiveEntities() {
			// when loading, we start with all 'public' entities, which are 'players'
			try {
				EntityEngineSQLite.getInstance().queryToMap(null, this, mMasterWeaponList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void setActive(String name, boolean active) {
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
							EntityEngineSQLite.getInstance().queryToMap(name, this, mMasterWeaponList);
							return true;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				return false;
			});
		}
		
		public void rollInitiative() {
			ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
				currentStage = 1;
				for (ActiveEntity entry:this.values()) {
					entry.mLastInit = entry.mFirstStrike + Dice.roll(10) + Dice.roll(10);
					// for sort, snap phases before normal before delib
					entry.mLastInitSort = (entry.mLastInitPhase - 1) * -100 + entry.mLastInit;
				}
				return true;
			});
		}

		
		public void singleSkillCheck(UserInfo user, String skillName, int entityUID) {
			final Skill lookup = new Skill();
			lookup.mTotal = 0;
			ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
				for (ActiveEntity entry:this.values()) {
					if (entry.mUid != entityUID)
						continue;
					
					Skill sk = entry.mSkills.get(skillName);
					if (sk != null) {
						lookup.mTotal = sk.mTotal;
						lookup.mDisplayName = sk.mDisplayName;
						lookup.mOwner = entry.mName;
					}
					break;
				}
				return false;
			});

			if (lookup.mTotal > 0) {
				int roll = Dice.rollOpenPercent();
				int total = roll + lookup.mTotal;
				String explain = lookup.mTotal + " + (" + roll + ") = " + total;
				String header = String.format("[%s] SkillCheck '%s' : %s", 
						lookup.mOwner, lookup.mDisplayName, explain);				
				SimpleEventList.getInstance().postEvent(new SimpleEvent("Total = " + total, 
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

			if (skillBase > 0) {
				int roll = Dice.rollOpenPercent();
				int total = roll + lookup.mTotal;
				String explain = lookup.mTotal + " + (" + roll + ") = " + total;
				String header = String.format("[%s] SkillCheck '%s' : %s", 
						lookup.mOwner, lookup.mDisplayName, explain);				
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
						int roll = Dice.rollOpenPercent();
						entry.mLastResult = baseSkill + roll;
						entry.mLastResultExplain = baseSkill + " + (" + roll + ") = " + entry.mLastResult;
					}
				}
				return true;
			});
		}
		
		public void advanceRound() {
			ModelSync.modelUpdate(ModelSync.Model.ENTITIES, () -> {
				currentStage = 0;
				currentRound++;
				for (ActiveEntity entry:this.values()) {
					entry.mLastInit = -1;
				}
				return true;
			});
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
		public JSONObject reportIfNeeded(long lastKnown) throws JSONException {
			return ModelSync.extractModel(ModelSync.Model.ENTITIES, new ModelSync.DataModel() {
				@Override
				public void extractModelData(JSONObject outData, long modelTime) throws JSONException {
					if (modelTime != lastKnown) {
						JSONArray list = new JSONArray();
						for (String val:keySet()) {
							ActiveEntity actor = get(val);
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
			if (action.equals("rollinit") && user.mLevel >= RMUserSecurity.kLoginGM) {
				rollInitiative();
				SimpleEventList.getInstance().postEvent(new SimpleEvent("Today is a good day to die.", "[GM] Round " + currentRound + " begins!", "rmgmaction", user.mUsername));
			}
			else if (action.equals("nextround") && user.mLevel >= RMUserSecurity.kLoginGM) {
				advanceRound();
				SimpleEventList.getInstance().postEvent(new SimpleEvent("Choose a phase and declare your actions.", "[GM] Round " + currentRound + " declarations", "rmgmaction", user.mUsername));
			} else if (action.equals("setphase")) {
				// has params: uid - entitiy uid, phase - next init phase in -1,0,1
				// TODO: Security! Client can lie, so we should check controllers for the uid, but skip isnt that good of a hacker so LATER
				setInitPhase(Integer.parseInt(request.getParameter("uid")), Integer.parseInt(request.getParameter("phase")));
			} else if (action.equals("skillcheck") && user.mLevel >= RMUserSecurity.kLoginGM) {
				groupSkillCheck(request.getParameter("skill"));
			} else if (action.equals("skillsingle")) {
				singleSkillCheck(user, request.getParameter("skill"), WebLib.getIntParam(request, "uid", -1));
			} else if (action.equals("skillcustom")) {
				customSkillCheck(user, request.getParameter("skill"), WebLib.getIntParam(request, "uid", -1), WebLib.getIntParam(request, "base", 0));
			} else if (action.equals("activate") && user.mLevel >= RMUserSecurity.kLoginGM) {
				try {
					String name = EntityEngineSQLite.getInstance().queryName(WebLib.getIntParam(request, "uid", -1));
					setActive(name, true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (action.equals("deactivate") && user.mLevel >= RMUserSecurity.kLoginGM) {
				try {
					String name = EntityEngineSQLite.getInstance().queryName(WebLib.getIntParam(request, "uid", -1));
					setActive(name, false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	ActiveEntities mActiveList = null;
	
	class PlayerList extends HashMap<String, Long>
	{
		long changedTime = System.currentTimeMillis();
		
		public void ping(final String username) {
			ModelSync.modelUpdate(ModelSync.Model.PLAYERS, () -> {
				boolean changed = false;
				final long now = System.currentTimeMillis();
				final long expires = now - 30000; // offline if no ping in 30s
				if (!this.containsKey(username))
					changed = true;
				this.put(username, now);
				// remove any oldies
				if (this.entrySet().removeIf( entry -> (entry.getValue() < expires))) {
					changed = true;
				}				
			    return changed;
			});
		}

		public JSONObject reportIfNeeded(long lastKnown) throws JSONException {
			return ModelSync.extractModel(ModelSync.Model.PLAYERS, new ModelSync.DataModel() {
				@Override
				public void extractModelData(JSONObject outData, long modelTime) throws JSONException {
					if (modelTime != lastKnown) {
						JSONArray list = new JSONArray();
						for (String val:keySet()) {
							list.put(val);
						}
						outData.put("online", list);
					}					
				}
			});
		}
		
	};
	PlayerList mPlayerList = new PlayerList();
	
	Object modelSyncSignal = new Object();
	
	ActionHandler modelSyncHandler = new ActionHandler() {
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
				//logger.info("Got Model Sync Request");
				
				// Any data updates
				mPlayerList.ping(user.mUsername);
				
				long player_ts = Long.parseLong(WebLib.getStringParam(request, "player_ts", "0"));
				long log_ts = Long.parseLong(WebLib.getStringParam(request, "log_ts", "0"));
				long ent_ts = Long.parseLong(WebLib.getStringParam(request, "ent_ts", "0"));

				// new mode - wait on ANY model change here
				try {
					if (!ModelSync.waitChange(player_ts, log_ts, ent_ts)) {
						// no changes,
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						return;
					}
				} catch (InterruptedException ie) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return;
				}

				// Serialize output based on request
				try {
					JSONObject outData = new JSONObject();

					// sync online players
					outData.put("players", mPlayerList.reportIfNeeded(player_ts));

					// sync log 
					outData.put("log", SimpleEventList.getInstance().reportIfNeeded(log_ts));
					
					// sync active entity model
					outData.put("active", mActiveList.reportIfNeeded(ent_ts));
					
		            response.setContentType("application/json");
		            response.setStatus(HttpServletResponse.SC_OK);
		            ServletOutputStream p = response.getOutputStream();
		            p.print(outData.toString());
		            p.flush();	
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
		}
	};
	
	ActionHandler entityHandler = new ActionHandler() {
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response) {
			try {
				JSONObject outData = EntityEngineSQLite.getInstance().queryEntities(user.mLevel <= RMUserSecurity.kLoginPlayer);
	            response.setContentType("application/json");
	            response.setStatus(HttpServletResponse.SC_OK);
	            ServletOutputStream p = response.getOutputStream();
	            p.print(outData.toString());
	            p.flush();	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	};
	
	CombatHandler mCombatHandler = new CombatHandler();
	String [] mMasterWeaponList = null;
	
	public void init() throws ServletException {
		super.init();
	
		try {
			mMasterWeaponList = mCombatHandler.getEngine().getWeaponList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mActiveList = new ActiveEntities();
		
		SimpleEventList.getInstance().postEvent(new SimpleEvent("System startup", "Notice", "rmsystem", "system"));
		
		// setup HTTP-GET handlers
		addGetHandler(DEFAULT_HANDLER, new TemplateResourceHandler("HomePage.ftl"));
		// setup HTTP-GET handlers
		addFtlHandler("LoginPage.ftl", new TemplateResourceHandler("LoginPage.ftl").setPostHandler(_userModel).setMinSecurity(UserInfo.kLoginInvalid));
		addGetHandler("logout", mLogoutHandler);
		addPostHandler("shutdown", shutdownHandler);
		addPostHandler("modelsync", modelSyncHandler);
		addPostHandler("entities", entityHandler);
		
		addPostHandler("rollinit", mActiveList);
		addPostHandler("nextround", mActiveList);
		addPostHandler("setphase", mActiveList);
		addPostHandler("skillcheck", mActiveList);
		addPostHandler("skillsingle", mActiveList);
		addPostHandler("skillcustom", mActiveList);
		addPostHandler("activate", mActiveList);
		addPostHandler("deactivate", mActiveList);
		
		addPostHandler("lookupTables", mCombatHandler.makeHandlerTable());
		addPostHandler("lookupAttack", mCombatHandler.makeHandlerAttack());
		addPostHandler("lookupCritical", mCombatHandler.makeHandlerCritical());
	}
}
