package com.ilsian.rmweb;

public class CombatDBMaker {

	public static void main(String[] args) {
		try {
			CombatEngineSQLite.createCombatDatabase();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
