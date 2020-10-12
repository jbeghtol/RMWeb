package com.ilsian.rmweb;

public class DamageResult {

	public int iDamage;
	public String iCriticals="";
	public int iRankLimit;
	public String iOGCrits="";
	
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
	
	public boolean checkReduceCriticals(int reduceCrits, int largeClass, String critColumnLg) {
		if (reduceCrits == 0 && largeClass == 0) {
			// crits good as-is
			return false;
		}
				
		// first perform any critical reduction
		String [] multi = iCriticals.split(",");
		for (int i=0; i<multi.length; i++)
		{
			char rval = (char) (multi[i].charAt(0) - reduceCrits);
			if (rval < 'A') {
				multi[i]=null; //nerf'd
			}
			else
			{
				switch (largeClass) {
					case 0:
					default:
						multi[i] = Character.toString( (char) rval) + multi[i].substring(1);
						break;
					case 1: // large
						if (rval >= 'B') {
							// Convert to large crit using the weapon class prefix
							multi[i] = critColumnLg + "L";
						}
						else {
							multi[i]=null; //nerf'd
						}
						break;
					case 2: // super large
						if (rval >= 'D') {
							// Convert to superlarge crit using the weapon class prefix
							multi[i] = critColumnLg + "V";
						}
						else {
							multi[i]=null; //nerf'd
						}
						break;
				}
			}
		}
		
		final StringBuffer newcrit = new StringBuffer();
		for (String m:multi)
		{
			if (m != null)
			{
				if (newcrit.length()>0)
					newcrit.append(",");
				newcrit.append(m);
			}
		}
		iOGCrits = iCriticals;
		iCriticals = newcrit.toString();
		return true;
	}
	
}
