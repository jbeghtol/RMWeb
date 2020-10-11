package com.ilsian.rmweb;

public interface CombatLookup {

	public String [] getWeaponList();
	public String [] getCriticalList();
	public boolean isFumble(int roll, int weaponIndex);
	public DamageResult getDamage(int um_roll, int roll, int weaponIndex, int at, int rankLimit);
	public String [] getCriticals(int roll, String tables);
}
