package com.ilsian.rmweb;
import java.io.Serializable;

public class SimpleEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public long mTimestamp;
	public String mHeader;
	public String mEventData;
	public String mType;
	public String mUser;
	
	// optionals
	public int mDbIndex;
	public int mDbInvalidate;
	public String mTargetName;
	
	public SimpleEvent(String data, String header, String type, String user) {
		mTimestamp = System.currentTimeMillis();
		mEventData = data;
		mType = type;
		mHeader = header;
		mUser = user;
		mDbIndex = -1;
		mDbInvalidate = -1;
	}
	
	public SimpleEvent setDbOpt(int opt) {
		mDbIndex = opt;
		return this;
	}
	
	public SimpleEvent setDbInvalidate(int opt) {
		mDbInvalidate = opt;
		return this;
	}
	
	public SimpleEvent setTargetName(String opt) {
		mTargetName = opt;
		return this;
	}
}
