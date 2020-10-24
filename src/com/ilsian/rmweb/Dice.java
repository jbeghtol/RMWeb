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
	
	public static Open rollOpen(boolean allowDown) {
		return new Open(allowDown);
	}
	
	public static Open rollOpen() {
		return new Open(true);
	}

	static class Open {
		public int base_;
		public int total_;
		public boolean manual_;
		public boolean used_open_; // set when someone uses the open ended version, for crits
		
		public Open(Open src, int newTotal) {
			base_ = src.base_;
			manual_ = src.manual_;
			used_open_ = src.used_open_;
			total_ = newTotal;
		}
		
		public Open(boolean allowDown) {
			base_ = rollClosed();
			total_ = base_;
			
			if (base_ > 95)
			{
				/* open end up! */
				int nextRoll = rollClosed();
				while (nextRoll > 95)
				{
					total_ += nextRoll;
					nextRoll = rollClosed();
				}
				total_ += nextRoll;
			}
			else if (allowDown && base_ < 6)
			{
				/* open end down! */
				int nextRoll = rollClosed();
				while (nextRoll < 6)
				{
					total_ -= nextRoll;
					nextRoll = rollClosed();
				}
				total_ -= nextRoll;
			}		
		}
		
		public void manual(int val) {
			manual_ = true;
			base_ = val;
			total_ = val;
		}
		
		public int expressedRoll() {
			return used_open_?total_:base_;
		}
		
		public String toString() {
			if (total_ != base_) {
				return String.format("(%d | %d)", base_, total_);
			} else if (manual_) {
				return String.format("(%d*)", total_);
			} else {
				return String.format("(%d)", total_);
			}
		}
	}
}
