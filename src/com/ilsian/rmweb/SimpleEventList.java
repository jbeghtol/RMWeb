package com.ilsian.rmweb;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public 	class SimpleEventList extends LinkedList<SimpleEvent> {
	
	static SimpleEventList mInstance = null;
	
	public static SimpleEventList getInstance() {
		if (mInstance == null) {
			mInstance = new SimpleEventList();
		}
		return mInstance;
	}
	
	long changedTime = System.currentTimeMillis();
	
	public synchronized void postEvent(SimpleEvent eve) {
		changedTime = eve.mTimestamp;
		this.add(eve);
	}
	
	public synchronized JSONObject reportIfNeeded(long lastKnown) throws JSONException {
		
		JSONObject outData = new JSONObject();
		outData.put("mod_ts", changedTime);
		if (lastKnown != changedTime) {
			JSONArray list = new JSONArray();
			for (SimpleEvent eve:this) {
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
		return outData;
	}
	
};