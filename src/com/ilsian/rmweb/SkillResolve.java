package com.ilsian.rmweb;

public class SkillResolve {

	public static boolean ALLOW_UNUSUALS = false;
	
	public enum Difficulty {
		ROUTINE(30),
		EASY(20),
		LIGHT(10),
		MEDIUM(0),
		HARD(-10),
		VERY_HARD(-20),
		EXTREMELY_HARD(-30),
		SHEER_FOLLY(-50),
		ABSURD(-70);
		int modifier_;
		private Difficulty(int mod) {
			modifier_ = mod;
		}
		public int getMod() {
			return modifier_;
		}
	}
	
	public enum General {
		SPECTACULAR_FAILURE(-26),
		ABSOLUTE_FAILURE(4),
		FAILURE(75),
		PARTIAL_SUCCESS(90),
		NEAR_SUCCESS(110),
		SUCCESS(175),
		ABSOLUTE_SUCCESS(Integer.MAX_VALUE),
		UNUSUAL_EVENT(66),
		UNUSUAL_SUCCESS(100);
		int ceiling_;
		private General(int ceiling) {
			ceiling_ = ceiling;
		}
		
		public static General resolve(Dice.Open value) {
			if (ALLOW_UNUSUALS) {
				if (value.base_ == UNUSUAL_EVENT.ceiling_)
					return UNUSUAL_EVENT;
				else if (value.base_ == UNUSUAL_SUCCESS.ceiling_)
					return UNUSUAL_SUCCESS;
			}
				
			for (General g:values()) {
				if (value.total_ <= g.ceiling_)
					return g;
			}
			// this shouldn't be possible
			return ABSOLUTE_SUCCESS;
		}
		
		public static String multiResolve(Dice.Open value) {
			if (ALLOW_UNUSUALS) {
				if (value.base_ == UNUSUAL_EVENT.ceiling_)
					return UNUSUAL_EVENT.toString();
				else if (value.base_ == UNUSUAL_SUCCESS.ceiling_)
					return UNUSUAL_SUCCESS.toString();
			}
			
			Difficulty highestSuccess = null;
			Difficulty lowestFailure = null;
			for (Difficulty d:Difficulty.values()) {
				General g = resolve(new Dice.Open(value, value.total_ + d.modifier_));
				if (g.compareTo(General.FAILURE) < 1) {
					// this is some kind of failure
					if (lowestFailure == null) {
						lowestFailure = d;
					}
				} else if (g.compareTo(General.SUCCESS) >= 0) {
					// this some kind of success
					highestSuccess = d;
				}
			}
			
			return String.format("Succeed[%s] Fail[%s]", highestSuccess==null?"None":highestSuccess.toString(), lowestFailure==null?"None":lowestFailure.toString());
		}
	};
	

}
