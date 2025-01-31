package com.ilsian.rmweb;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
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
		rm.put("cond_mods", Global.CONDITION_MODS);
		
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
	
	File makeCheckpointDir(String note) {
		long now = System.currentTimeMillis();
		File f = new File("checkpoints", String.format("checkpoint_%d", now));
		f.mkdirs();
		
	    // Write note to file
	    File noteFile = new File(f, "note.txt");
	    try (PrintWriter writer = new PrintWriter(noteFile)) {
	        writer.println(note);
	    } catch (IOException e) {
	    	logger.warning("Failed to save note to checkpoint dir: " + e);
	    }
	    
		return f;
	}
	
	static class CheckPoint {
		public String mNote;
		public long mTimestamp;
		public CheckPoint(String note, long ts) {
			mNote = note; mTimestamp = ts;
		}
	}
	
	ArrayList<CheckPoint> getCheckpoints() {
		File rootDir = new File("checkpoints");
		ArrayList<CheckPoint> cp = new ArrayList<CheckPoint>();
		
	    File[] files = rootDir.listFiles();
	    if (files != null) {
	        for (File file : files) {
	            if (file.isDirectory() && file.getName().startsWith("checkpoint_")) {
	                String timestampStr = file.getName().substring(11); // 11 is the length of "checkpoint_"
	                try {
	                    long timestamp = Long.parseLong(timestampStr);
                        File noteFile = new File(file, "note.txt");
                        Scanner scanner = new Scanner(noteFile);
                        scanner.useDelimiter("\\Z"); // Read entire file
	                    cp.add(new CheckPoint(scanner.next(), timestamp));
	                    scanner.close();
	                } catch (Exception e) {
	                    logger.warning("Ignoring checkpoint candidate " + file + ", Exception: " + e);
	                }
	            }
	        }
	    }
	    Collections.sort(cp, new Comparator<CheckPoint>() {
	        @Override
	        public int compare(CheckPoint o1, CheckPoint o2) {
	            return Long.compare(o2.mTimestamp, o1.mTimestamp);
	        }
	    });
	    return cp;
	}
	
	File findLastCheckpointDir() {
		File rootDir = new File("checkpoints");
	    File mostRecentCheckpointDir = null;
	    long mostRecentTimestamp = 0;

	    File[] files = rootDir.listFiles();
	    if (files != null) {
	        for (File file : files) {
	            if (file.isDirectory() && file.getName().startsWith("checkpoint_")) {
	                String timestampStr = file.getName().substring(11); // 11 is the length of "checkpoint_"
	                try {
	                    long timestamp = Long.parseLong(timestampStr);
	                    if (timestamp > mostRecentTimestamp) {
	                        mostRecentTimestamp = timestamp;
	                        mostRecentCheckpointDir = file;
	                    }
	                } catch (NumberFormatException e) {
	                    // ignore directories with non-numeric timestamps
	                }
	            }
	        }
	    }

	    return mostRecentCheckpointDir;
	}
	
	ActionHandler checkpointQueryHandler = new ActionHandler() {
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {

				ArrayList<CheckPoint> checkPoints = getCheckpoints();
				
				// Serialize output based on request
				try {
					JSONObject outData = new JSONObject();
					
					for (CheckPoint cp:checkPoints) {
						JSONObject cpData = new JSONObject();
						cpData.put("note", cp.mNote);
						cpData.put("time", cp.mTimestamp);
						cpData.put("date", new Date(cp.mTimestamp).toString());
						outData.append("checkpoints", cpData);
					}
					
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
	
	ActionHandler cleanSlateHandler = new ActionHandler() {
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response) {
			if (user.mLevel < RMUserSecurity.kLoginGM) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			
			File cp = makeCheckpointDir("Clean Slate");
			
			// Clears the event history log - note TODO: This doesn't archive ANYTHING yet
			SimpleEventList.getInstance().archive(cp, true);
			// Clears all active entities back to unharmed and round count back to pre-round 1
			if (mActiveList != null)
				mActiveList.archive(cp, true);
			
			// Delete all pending wounds and would tracking records
			mCombatHandler.getWoundDB().reset(cp);
			
			response.setStatus(HttpServletResponse.SC_OK);
		}
	};
	
	ActionHandler checkpointHandler = new ActionHandler() {
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response) {
			if (user.mLevel < RMUserSecurity.kLoginGM) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			String note = WebLib.getStringParam(request, "note", "User Save");
			File cp = makeCheckpointDir(note);
			SimpleEventList.getInstance().archive(cp, false);
			if (mActiveList != null)
				mActiveList.archive(cp, false);
			
			SimpleEventList.getInstance().postEvent(new SimpleEvent("Checkpoint saved: " + note, "Notice", "rmsystem", "system"));
			response.setStatus(HttpServletResponse.SC_OK);
		}
	};
		
	ActionHandler loadSlateHandler = new ActionHandler() {
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response) {
			if (user.mLevel < RMUserSecurity.kLoginGM) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			
			String ts = WebLib.getStringParam(request, "time", null);
			File cp = ts == null?findLastCheckpointDir():new File("checkpoints", "checkpoint_" + ts);
			if (cp != null) {
				logger.info("Loading checkpoint from " + cp);
				
				// Clears the event history log - note TODO: This doesn't archive ANYTHING yet
				SimpleEventList.restoreCheckpoint(cp);
				// Clears all active entities back to unharmed and round count back to pre-round 1
				if (mActiveList != null)
					mActiveList.restoreCheckpoint(cp);
				
				// Resets pending wounds, we don't track through restore
				mCombatHandler.getWoundDB().reset(cp);
			}
			
			response.setStatus(HttpServletResponse.SC_OK);
		}
	};
	
	ActionHandler terminatorHandler = new ActionHandler() {
		@Override
		public void handleAction(String action, UserInfo user, HttpServletRequest request, HttpServletResponse response) {
			if (user.mLevel < RMUserSecurity.kLoginAdmin) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			
			new Thread() {
				public void run() {
					logger.info("Terminating RMWeb in 1s...");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignore) {
					}
					logger.info("Exiting.");
					System.exit(0);
				}
			}.start();
			response.setStatus(HttpServletResponse.SC_OK);
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
		ActiveEntities.init(mMasterWeaponList);
		mActiveList = ActiveEntities.instance();
		
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
		addPostHandler("updateWounds", mActiveList);
		addPostHandler("alterphase", mActiveList);
		addPostHandler("syncentities", mActiveList);
		
		addPostHandler("cleanslate", cleanSlateHandler);
		addPostHandler("loadslate", loadSlateHandler);
		addPostHandler("checkpoint", checkpointHandler);
		addPostHandler("checkpointQuery", checkpointQueryHandler);
		addPostHandler("terminate", terminatorHandler);
		
		addPostHandler("upload", mFileUploadHandler);
		
		addPostHandler("lookupTables", mCombatHandler.makeHandlerTable());
		addPostHandler("lookupAttack", mCombatHandler.makeHandlerAttack());
		addPostHandler("lookupCritical", mCombatHandler.makeHandlerCritical());
		addPostHandler("BAR", mCombatHandler.makeHandlerBAR());
		addPostHandler("RR", mCombatHandler.makeHandlerRR());
		addPostHandler("pendingWound", mCombatHandler.getWoundDB());
		
		addFtlHandler("EditWounds.ftl", new TemplateResourceHandler("EditWounds.ftl").setMinSecurity(RMUserSecurity.kLoginGM));
		addFtlHandler("EditGlobals.ftl", new TemplateResourceHandler("EditGlobals.ftl").setInteraction(new SettingsAgent(), true).setMinSecurity(RMUserSecurity.kLoginGM));
	}
}
