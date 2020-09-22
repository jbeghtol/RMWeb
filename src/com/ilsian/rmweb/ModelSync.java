package com.ilsian.rmweb;

import java.util.function.Supplier;

import org.json.JSONException;
import org.json.JSONObject;

public class ModelSync {
	public static final long CLIENT_SLEEP_TIME = 10000;
	
	public static Object lock_ = new Object();
		
	public enum Model {
		PLAYERS,
		LOG,
		ENTITIES
	};

	public static interface DataModel {
		public void extractModelData(JSONObject jobj, long modts) throws JSONException;
	};
	
	public static long [] update_times_;
	static {
		update_times_ = new long[Model.values().length];
		final long now = System.currentTimeMillis();
		for (int i=0;i<update_times_.length; i++) {
			update_times_[i] = now;
		}
	}

	public static Object getLock() {
		return lock_;
	}
	
	public static void modelUpdate(Model mdl, Supplier<Boolean> modelUpdater) {
		synchronized(lock_) {
			if (modelUpdater.get()) {
				update_times_[mdl.ordinal()] = System.currentTimeMillis();
				System.err.println("Model updated: " + mdl);
				lock_.notifyAll();
			}
		}
	}
	
	public static JSONObject extractModel(Model mdl, DataModel dataExtractor) throws JSONException {
		synchronized(lock_) {
			long lastTime = update_times_[mdl.ordinal()];
			JSONObject outData = new JSONObject();
			outData.put("mod_ts", lastTime);
			dataExtractor.extractModelData(outData, lastTime);
			return outData;
		}
	}
	
	public static boolean waitChange(long player_ts, long log_ts, long ent_ts) throws InterruptedException {
		synchronized(lock_) {
			if (player_ts != update_times_[Model.PLAYERS.ordinal()] || 
				log_ts != update_times_[Model.LOG.ordinal()] || 
				ent_ts != update_times_[Model.ENTITIES.ordinal()] ) {
				System.err.println("Sync wait returning immediately, new data exists!");
				return true;
			}
			//System.err.println("CLIENT WAITING FOR MODEL DATA");
			lock_.wait(CLIENT_SLEEP_TIME);
			//System.err.println("CLIENT WOKE FOR MODEL DATA");
			return (player_ts != update_times_[Model.PLAYERS.ordinal()] || 
				log_ts != update_times_[Model.LOG.ordinal()] || 
				ent_ts != update_times_[Model.ENTITIES.ordinal()] );
		}
	}
}
