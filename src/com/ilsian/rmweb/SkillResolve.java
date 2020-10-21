package com.ilsian.rmweb;

public class SkillResolve {

	public static boolean ALLOW_UNUSUALS = false;
	
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
	};
	
}
