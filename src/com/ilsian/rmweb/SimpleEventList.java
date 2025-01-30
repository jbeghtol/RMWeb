package com.ilsian.rmweb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public 	class SimpleEventList extends LinkedList<SimpleEvent> {

	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger("com.ilsian.rmweb.SimpleEventList");
	
	static final int MAX_HISTORY = 500;
	
	static SimpleEventList mInstance = null;
	
	public static SimpleEventList getInstance() {
		if (mInstance == null) {
			mInstance = new SimpleEventList();
		}
		return mInstance;
	}
	
	long changedTime = System.currentTimeMillis();
	
	public synchronized void postEventOld(SimpleEvent eve) {
		changedTime = eve.mTimestamp;
		this.add(eve);
	}
	
    public void saveToFile(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
            logger.info("Save SimpleEventList to " + filePath);
        } catch (IOException ioe) {
        	logger.warning("Failed to save SimpleEventList to " + filePath + ":" + ioe);
        }
    }
    
    public static void restoreCheckpoint(File cp) {
		ModelSync.modelUpdate(ModelSync.Model.LOG, () -> {
			
			if (cp != null) {
				String serialPath = cp + File.separator + "events.ser";
		        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serialPath))) {
		            mInstance = (SimpleEventList) ois.readObject();
           
		            boolean restoreAdded = false;
		            if (mInstance.size() > 0) {
		            	SimpleEvent eve = mInstance.getFirst();
		            	if (eve.mUser.equals("system")) {
		            		eve.mEventData = "System restored";
		            		restoreAdded = true;
		            	}
		            }
		            if (!restoreAdded) {
			            mInstance.addFirst(new SimpleEvent("System restored", "Notice", "rmsystem", "system"));
		            }
		            
		            // pretend these all happened right now
		            long ts = System.currentTimeMillis();
		            for (SimpleEvent eve:mInstance) {
		            	eve.mTimestamp = ts;
		            }
			    } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return true;
		});
    }
    

	public synchronized void archive(File cp, boolean clear) {
		ModelSync.modelUpdate(ModelSync.Model.LOG, () -> {
			
			if (cp != null) {
				String serialPath = cp + File.separator + "events.ser";
				saveToFile(serialPath);
			}
			
			if (clear) {
				this.clear();
				this.add(new SimpleEvent("System reset", "Notice", "rmsystem", "system"));
			}
			return true;
		});
	}
	
	public synchronized void postEvent(SimpleEvent eve) {
		
		ModelSync.modelUpdate(ModelSync.Model.LOG, () -> {
			this.add(eve);
			// keep the list under control (clients should perhaps do the same)
			while (this.size() > MAX_HISTORY) {
				this.removeFirst();
			}
			return true;
		});
	}
	
	public JSONObject reportIfNeeded(long lastKnown) throws JSONException {
		return ModelSync.extractModel(ModelSync.Model.LOG, new ModelSync.DataModel() {
			@Override
			public void extractModelData(JSONObject outData, long modelTime) throws JSONException {
				if (modelTime != lastKnown) {
					JSONArray list = new JSONArray();
					for (SimpleEvent eve:SimpleEventList.this) {
						if (eve.mTimestamp>lastKnown) {
							JSONObject entry = new JSONObject();
							entry.put("ts", eve.mTimestamp);
							entry.put("event", eve.mEventData);
							entry.put("header", eve.mHeader);
							entry.put("type", eve.mType);
							entry.put("user", eve.mUser);
							if (eve.mDbIndex >= 0) {
								entry.put("dbopt", eve.mDbIndex);
							}
							if (eve.mDbInvalidate >= 0) {
								entry.put("dbclear", eve.mDbInvalidate);
							}
							if (eve.mTargetName != null) {
								entry.put("target", eve.mTargetName);
							}
							list.put(entry);
						}
					}
					outData.put("events", list);
				}
			}
		});
	}

	
};