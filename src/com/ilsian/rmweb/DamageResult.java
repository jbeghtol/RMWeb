package com.ilsian.rmweb;

public class DamageResult {

	public int iDamage;
	public String iCriticals="";
	
	public DamageResult(int dam, String crit) {
		iDamage = dam;
		iCriticals = crit;
	}
	
	public DamageResult(int dam)
	{
		iDamage = dam;
	}
	
	public void addDamage(int dam, String crit) {
		iDamage += dam;
		if (crit != null && crit.length() > 0)
		{
			if (iCriticals.length() > 0)
				iCriticals += "," + crit;
			else
				iCriticals = crit;
		}
	}
	
	public String toString() {
		if (iDamage <= 0)
			return "No Damage";
		return String.format("Damage: %d %s", iDamage, iCriticals);
	}
}
