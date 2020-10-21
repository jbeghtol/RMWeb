package com.ilsian.rmweb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CritParser {

	interface SimpleEffect {
		void addToCrit(int val, boolean dupe);
	}
	interface DurationEffect {
		void addToCrit(int val, int dur, boolean dupe);
	}
	
	enum Effect {
		EXTRA_DAMAGE("\\+([0-9]+)H"),
		BLEEDING("([0-9]+)\\!"),
		STUN("([0-9]+)\\*"),
		NO_PARRY("([0-9]+)\\@"),
		STUN_NO_PARRY("([0-9]+)\\*\\@"),
		MUST_PARRY("([0-9]+)x"),
		MUST_PARRY_PENALTY("([0-9]+)x\\((-[0-9]+)\\)"),
		
		PENALTY_SINGLE("\\((-[0-9]+)\\)"),
		PENALTY_DURATION("([0-9]+)\\((-[0-9]+)\\)"),
		BONUS_SINGLE("\\((\\+[0-9]+)\\)"),
		BONUS_DURATION("([0-9]+)\\((\\+[0-9]+)\\)")
		;
		Pattern p_;
		private Effect(String pString) {
			p_ = Pattern.compile(pString);
		}
		public void check(String crit, SimpleEffect effect) {
			Matcher m = p_.matcher(crit);
			if (m.find()) {
				effect.addToCrit( Integer.parseInt(m.group(1)), m.find() );
			}
		}
		public void checkDur(String crit, DurationEffect effect) {
			Matcher m = p_.matcher(crit);
			if (m.find()) {
				effect.addToCrit( Integer.parseInt(m.group(2)), Integer.parseInt(m.group(1)), m.find() );
			}
		}
	}

	
	public EffectRecord extractDetail(String crit) {
		final EffectRecord cd = new EffectRecord();
		
		Effect.EXTRA_DAMAGE.check(crit, (r, d) -> {
			cd.damage_ = r;
			cd.duplicate_ = d;
		});
		Effect.BLEEDING.check(crit, (r, d) -> {
			cd.bleeding_ = r;
			cd.duplicate_ = d;
		});
		Effect.STUN.check(crit, (r, d) -> {
			cd.stun_ = r;
		});
		Effect.NO_PARRY.check(crit, (r, d) -> {
			cd.noParry_ = r;
		});
		Effect.STUN_NO_PARRY.check(crit, (r, d) -> {
			cd.stun_ = r;
			cd.noParry_ = r;
		});
		Effect.MUST_PARRY.check(crit, (r, d) -> {
			cd.mustParry_ = r;
		});
		Effect.MUST_PARRY_PENALTY.checkDur(crit, (r, dur, d) -> {
			// in this, duration is for both mp and penalty
			cd.mustParry_ = dur;
			cd.addBonus(r, dur);
		});
		Effect.PENALTY_SINGLE.check(crit, (r, d) -> {
			cd.addBonus(r, 0);
		});
		Effect.PENALTY_DURATION.checkDur(crit, (r, dur, d) -> {
			cd.addBonus(r, dur);
		});
		Effect.BONUS_SINGLE.check(crit, (r, d) -> {
			cd.addBonus(r, 0);
		});
		Effect.BONUS_DURATION.checkDur(crit, (r, dur, d) -> {
			cd.addBonus(r, dur);
		});
	
		return cd;
	}
	
	
	public static void main(String[] args) {
		String crit = "Hammer foe in shoulder. He falls 10 feet and spuins around. He stumbles another 5 ft before regaining control. w/stuff without stuff +15H,2*@,2(-15),89!,6(+6)";
		//String crit = "Strike brings foe down. His spine is broken with liitle effort. Foe is still. Blood pours from his mouth heralding his death. He dies in 3 rounds. (+20)";
		//String crit = "Something 3x(-30),(+10)";
		
		CritParser cp = new CritParser();
		EffectRecord cd = cp.extractDetail(crit);
		
		if (cd.duplicate_)
			System.err.print("DUPE! ");
		System.err.println(cd);
		
	}
}
