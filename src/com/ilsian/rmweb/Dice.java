package com.ilsian.rmweb;

import java.util.Random;

public class Dice {
	static Random _commonRand = new Random();
	
	public static int roll(int die) {
		// TODO: do a modulo bias safe rand
		return _commonRand.nextInt(die) + 1; 
	}
	
	public static int rollClosed() {
		return roll(100);
	}
	
	public static int rollOpenPercent() {
		int baseRoll = rollClosed();
	
		if (baseRoll > 95)
		{
			/* open end up! */
			int nextRoll = rollClosed();
			while (nextRoll > 95)
			{
				baseRoll += nextRoll;
				nextRoll = rollClosed();
			}
			baseRoll += nextRoll;
		}
		else if (baseRoll < 6)
		{
			/* open end down! */
			int nextRoll = rollClosed();
			while (nextRoll < 6)
			{
				baseRoll -= nextRoll;
				nextRoll = rollClosed();
			}
			baseRoll -= nextRoll;
		}		
		return baseRoll;
	}
}
