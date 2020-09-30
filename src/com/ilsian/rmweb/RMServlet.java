package com.ilsian.rmweb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

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
				String header = String.format("[%s] Skill Check '%s' : %s", 
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

			if (skillBase >= 0) {
				int roll = Dice.rollOpenPercent();
				int total = roll + lookup.mTotal;
				String explain = lookup.mTotal + " + (" + roll + ") = " + total;
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
					entry.mLastInitExplain = "";
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
				advanceRound();
				SimpleEventList.getInstance().postEvent(new SimpleEvent("Choose a phase and declare your actions.", "[GM] Round " + currentRound + " declarations", "rmgmaction", user.mUsername));
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
					outData.put("active", mActiveList.reportIfNeeded(user, ent_ts));
					
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
				JSONObject outData = EntityEngineSQLite.getInstance().queryEntities(user.mLevel < RMUserSecurity.kLoginGM);
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
	
	/**
	 * Method to handle file uploads from the user
	 */
	final ActionHandler mFileUploadHandler = new ActionHandler() {

		/**
		 * Get the local file to be used to store the uploaded file name. Attempts
		 * to get a unique file that doesn't rely on the source name being valid
		 * in our OS environment.
		 * @throws IOException 
		 */
		File getSafeTargetFile(File rootDir, String sourceName, String type, UserInfo user) throws IOException {
			final String safesuffix = "." + user.mUsername.replaceAll("[^A-Za-z0-9]", "");
			return File.createTempFile(type + "-", safesuffix, rootDir);
		}
		
		@Override
		public void handleAction(String action, UserInfo user,
				HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			
			final String type = WebLib.getStringParam(request, "type", "invalid");
			if (user.mLevel < RMUserSecurity.kLoginGM) {
				WebLib.renderArrayJSONResponse(response, new boolean[] { false }, new String[] { "User access is denied." });
				return;
			}

			if (!ServletFileUpload.isMultipartContent(request)) {
				WebLib.renderArrayJSONResponse(response, new boolean[] { false }, new String[] { "Request content invalid." });
				return;
			}

			ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
			ServletRequestContext reqContext = new ServletRequestContext(request);

			// uploads are stored in a subdir of the working directory
			File uploadPath = new File("uploads");
			if (!uploadPath.exists())
				uploadPath.mkdirs();

			HashMap<String,String> formParams = new HashMap();
			
			PrintWriter writer = response.getWriter();
			response.setContentType("application/json");
			JSONArray json = new JSONArray();
			boolean anyChanges = false;
			try {
				List<FileItem> items = uploadHandler.parseRequest(reqContext);
				for (FileItem item : items) {
					if (!item.isFormField()) {
						// save the file contents
						final File file = getSafeTargetFile(uploadPath, item.getName(), type, user);
						item.write(file);
						
						// handle the uploaded file
						final StringBuilder msg = new StringBuilder();
						final boolean hresult = EntityEngineSQLite.getInstance().importLive(file,  item.getName(), msg);
						// update the json response objects
						final JSONObject jsono = new JSONObject();
						jsono.put("name", item.getName());
						jsono.put("size", item.getSize());
						jsono.put("result", hresult);
						jsono.put("message", msg.toString());
						json.put(jsono);
						if (hresult)
							anyChanges = true;
					}
					else {
						// this presumes formData comes first - which should be guaranteed by our form
						formParams.put(item.getFieldName(), item.getString());
					}
				}
				if (anyChanges && mActiveList != null) {
					mActiveList.refreshActiveFromDB();
				}
			} catch (Exception e) {
				// this is unlikely to be useful, but in case there are
				// exceptions where client connection is still okay, add
				// the exception as a resut record
				final JSONObject jsono = new JSONObject();
				try {
					jsono.put("result", false);
					jsono.put("message", e.toString());
				} catch (JSONException ignored) {
					// for this input, we will never throw
				}
				json.put(jsono);
			} finally {
				writer.write(json.toString());
				writer.close();
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
		addPostHandler("activatepeer", mActiveList);
		addPostHandler("deactivatepeer", mActiveList);
		addPostHandler("toggleVisible", mActiveList);
		addPostHandler("delete", mActiveList);
		addPostHandler("deletegroup", mActiveList);
		
		addPostHandler("upload", mFileUploadHandler);
		
		addPostHandler("lookupTables", mCombatHandler.makeHandlerTable());
		addPostHandler("lookupAttack", mCombatHandler.makeHandlerAttack());
		addPostHandler("lookupCritical", mCombatHandler.makeHandlerCritical());
	}
}
