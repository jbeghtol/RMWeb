package com.ilsian.rmweb;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public 	class SimpleEventList extends LinkedList<SimpleEvent> {
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
							list.put(entry);
						}
					}
					outData.put("events", list);
				}
			}
		});
	}

	
};