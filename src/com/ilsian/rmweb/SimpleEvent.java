package com.ilsian.rmweb;

public class SimpleEvent {
	public long mTimestamp;
	public String mHeader;
	public String mEventData;
	public String mType;
	public String mUser;
	
	public SimpleEvent(String data, String header, String type, String user) {
		mTimestamp = System.currentTimeMillis();
		mEventData = data;
		mType = type;
		mHeader = header;
		mUser = user;
	}
}
