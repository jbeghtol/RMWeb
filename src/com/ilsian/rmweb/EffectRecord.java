package com.ilsian.rmweb;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ilsian.tomcat.WebLib;


public class EffectRecord {
	
	static final String DISPLAY_STUN = "<img class=\"mod\" src=\"/res/stunned.png\" />";
	static final String DISPLAY_NO_PARRY = "<img class=\"mod\" src=\"/res/noparry.png\" />";
	static final String DISPLAY_BLOOD = "<img class=\"mod\" src=\"/res/blood.png\" />";
		
	public int damage_ = 0;
	public int stun_ = 0;
	public int noParry_ = 0;
	public int mustParry_ = 0;
	public int bleeding_ = 0;
	
	public boolean duplicate_ = false;
	
	public String getHighlights() {
		StringBuilder sb = new StringBuilder(" ");
		if (stun_>0)
			sb.append(DISPLAY_STUN);
		if (noParry_>0)
			sb.append(DISPLAY_NO_PARRY);
		if (bleeding_>0)
			sb.append(DISPLAY_BLOOD);
		
		if (sb.length() == 1) {
			return "";
		}
		return sb.toString();
	}
	
	public String getDetail() {
		return toString();
	}
	
	public void updateFromForm(HttpServletRequest req) {
		damage_ = WebLib.getIntParam(req, "hits", 0);
		bleeding_ = WebLib.getIntParam(req, "bleed", 0);
		stun_ = WebLib.getIntParam(req, "stun", 0);
		noParry_ = WebLib.getIntParam(req, "noparry", 0);
		mustParry_ = WebLib.getIntParam(req, "mustparry", 0);
		modifiers_.clear();
		int bn = WebLib.getIntParam(req, "bonus", 0);
		if (bn > 0) {
			modifiers_.add(new TimedModifier(bn, WebLib.getIntParam(req, "bonusdur", 0)));
		}
		int pe = WebLib.getIntParam(req, "penalty", 0);
		if (pe < 0) {
			modifiers_.add(new TimedModifier(pe, WebLib.getIntParam(req, "penaltydur", 0)));
		}
	}
	
	public JSONObject asJSON() throws JSONException {
		JSONObject w = new JSONObject();
		w.put("hits", damage_);
		w.put("bleed", bleeding_);
		w.put("stun", stun_);
		w.put("noparry", noParry_);
		w.put("mustparry", mustParry_);
		
		int bonus = 0;
		int penalty = 0;
		int bonusDur = 0;
		int penaltyDur = 0;
		for (TimedModifier tm:modifiers_) {
			if (tm.value_ > 0) {
				bonus += tm.value_;
				bonusDur = Math.max(bonusDur, tm.duration_);
			}
			else {
				penalty += tm.value_;
				penaltyDur = Math.max(penaltyDur, tm.duration_);
			}
		}
		w.put("bonus", bonus);
		w.put("bonusdur", bonusDur);
		w.put("penalty", penalty);
		w.put("penaltydur", penaltyDur);
		return w;
	}
	
	public String toString() {
		int bonus = 0;
		int penalty = 0;
		int bonusDur = 0;
		int penaltyDur = 0;
		for (TimedModifier tm:modifiers_) {
			if (tm.value_ > 0) {
				bonus += tm.value_;
				bonusDur = Math.max(bonusDur, tm.duration_);
			}
			else {
				penalty += tm.value_;
				penaltyDur = Math.max(penaltyDur, tm.duration_);
			}
		}

		return String.format("Damage: %d, Bleed: %d, Stun: %d, NoParry: %d, MustParry: %d, Bonus: %s Penalty: %s",
				damage_,bleeding_,stun_,noParry_,mustParry_, bonus==0?"0":String.format("+%d (%d)", bonus, bonusDur),  penalty==0?"0":String.format("%d (%d)", penalty, penaltyDur));
	}
	
	static class TimedModifier {
		public int duration_ = 0;
		public int value_ = 0;
		TimedModifier(int v, int d) {
			duration_ = d;
			value_ = v;
		}
	}
	
	public ArrayList<TimedModifier> modifiers_ = new ArrayList<TimedModifier>();
	
	public int netBonus() {
		int total = 0;
		for (TimedModifier tm:modifiers_) {
			total += tm.value_;
		}
		return total;
	}
	
	public void addBonus(int val, int dur) {
		for (TimedModifier tm:modifiers_) {
			if (val == tm.value_) {
				tm.duration_ = Math.max(tm.duration_, dur);
				return;
			} 
		}
		modifiers_.add(new TimedModifier(val, dur));
	}
	
	public void merge(EffectRecord eff, boolean defender) {
		if (defender) {
			// add all penalties
			for (TimedModifier tm:eff.modifiers_) {
				if (tm.value_ < 0) {
					modifiers_.add(tm);
				}
			}
			damage_ += eff.damage_;
			stun_ += eff.stun_;
			noParry_ += eff.noParry_;
			mustParry_ += eff.mustParry_;
			bleeding_ += eff.bleeding_;
		} else {
			// add all bonuses
			for (TimedModifier tm:eff.modifiers_) {
				if (tm.value_ > 0) {
					modifiers_.add(tm);
				}
			}
		}
	}
	
	public boolean applyRound() {
		boolean anyChange = false;
		
		if (bleeding_ > 0) {
			damage_ += bleeding_;
			anyChange = true;
		}
		if (stun_ > 0) {
			stun_--;
			anyChange = true;
		}
		if (noParry_ > 0) {
			noParry_--;
			anyChange = true;
		}
		if (mustParry_ > 0) {
			mustParry_--;
			anyChange = true;
		}
		// remove anything w/ 1 round left
		if (modifiers_.removeIf(x -> {
			return x.duration_ == 1;
			})) {
			anyChange = true;
		}
		// next decrement what remains
		for (TimedModifier it:modifiers_) {
			if (it.duration_ > 0) {
				it.duration_--;
				anyChange = true;
			}
		}
		return anyChange;
	}
	
}
