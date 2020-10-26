package com.ilsian.rmweb;

public class SkillResolve {

	public static boolean ALLOW_UNUSUALS = false;
	
	private static String prettyStringEnum(String enumName) {
		String [] words = enumName.replace('_', ' ').split(" ");
		StringBuilder sb = new StringBuilder();
		for (String w:words) {
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(w.substring(0, 1).toUpperCase());
			sb.append(w.substring(1).toLowerCase());
		}
		return sb.toString();
	}
	
	private static String wrapCSS(String str, String cssClass) {
		return String.format("<span class=\"%s\">%s</span>", cssClass, str);
	}
	
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
			
			return String.format("Succeed[%s] Fail[%s]", 
					highestSuccess==null?"--":wrapCSS(prettyStringEnum(highestSuccess.toString()), "good bold"), 
					lowestFailure==null?"--" :wrapCSS(prettyStringEnum(lowestFailure.toString()), "bad bold"));
		}
	};
	
	// these are reverse, since we subtract from the top of the lower
	static final int [] BAR_LOWER = {5,15,20,30,35,45,50,60,65,70};
	public static int BAR(Dice.Open roll) {
		// Handle all the UM rolls
		if (roll.base_ < 5 || roll.total_ < 4) {
			// Fumble!
			return Integer.MAX_VALUE;
		}
		if (roll.base_ == 100) {
			return -175;
		} else if (roll.base_ >= 98) {
			return -100;
		} else if (roll.base_ >= 96) {
			return -75;
		}
		
		// low value handled, now prevent the total from exceeding the max UM score  
		int input = Math.min(95, roll.total_);
		
		// now for the magic!
		if (input < 45) {
			// lower bracket is annoying
			int interval = (44 - input) / 4;
			return BAR_LOWER[interval];
		} else if (input > 52) {
			// upper brackets easy, -5 for every 4%
			int interval = 1 + (input - 53) / 4;
			return interval * -5;
		}
			
		// the mids, 45-52
		return 0;
	}

	static final int [] [] RR_ROWS = {
		// rows are DEFENDER level increasing, Columns ATTACKER level increasing
		{ 50,55,60,65,70,73,76,79,82,85,87,89,91,93,95 },
		{ 45,50,55,60,65,68,71,74,77,80,82,84,86,88,90 },
		{ 40,45,50,55,60,63,66,69,72,75,77,79,81,83,85 },
		{ 35,40,45,50,55,58,61,64,67,70,72,74,76,78,80 },
		{ 30,35,40,45,50,53,56,59,62,65,67,69,71,73,75 },
		{ 27,32,37,42,47,50,53,56,59,62,64,66,68,70,72 },
		{ 24,29,34,39,44,47,50,53,56,59,61,63,65,67,69 },
		{ 21,26,31,36,41,44,47,50,53,56,58,60,62,64,66 },
		{ 18,23,28,33,38,41,44,47,50,53,55,57,59,61,63 },
		{ 15,20,25,30,35,38,41,44,47,50,52,54,56,58,60 },
		{ 13,18,23,28,33,36,39,42,45,48,50,52,54,56,58 },
		{ 11,16,21,26,31,34,37,40,43,46,48,50,52,54,56 },
		{ 9, 14,19,24,29,32,35,38,41,44,46,48,50,52,54 },
		{ 7, 12,17,22,27,30,33,36,39,42,44,46,48,50,52 },
		{ 5, 10,15,20,25,28,31,34,37,40,42,44,46,48,50 },
	};
	
	public static int RRTarget(int attackLevel, int defLevel) {
		if (attackLevel < 1)
			attackLevel = 1;
		if (defLevel < 1)
			defLevel = 1;
		
		if (attackLevel > 15 && defLevel > 15) {
			// this is easy
			return 50 + attackLevel - defLevel;
		} else if (attackLevel > 15) {
			// attacker only exceeding table
			return RR_ROWS[defLevel-1][14] + attackLevel - 15;
		} else if (defLevel > 15) {
			// defender only exceeding table
			return RR_ROWS[14][attackLevel-1] - defLevel + 15;
		} else {
			// straight table lookup
			return RR_ROWS[defLevel-1][attackLevel-1];
		}
	}
	
	public static void main(String [] args) {
//		for (int i=1;i<=100;i++) {
//			Dice.Open roll = Dice.rollOpen();
//			roll.base_ = i; roll.total_ = i;
//			System.err.printf("BAR[%d] = %d\n", i, BAR(roll));
//		}
		
		System.err.printf("RR ATT: %d, DEF: %d TARGET: %d\n", 15, 1, RRTarget(15, 1));
		System.err.printf("RR ATT: %d, DEF: %d TARGET: %d\n", 1, 15, RRTarget(1, 15));
		System.err.printf("RR ATT: %d, DEF: %d TARGET: %d\n", 15, 15, RRTarget(15, 15));
		System.err.printf("RR ATT: %d, DEF: %d TARGET: %d\n", 25, 20, RRTarget(25, 20));
	}
}
