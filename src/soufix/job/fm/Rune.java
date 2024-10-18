package soufix.job.fm;

import java.util.*;

import soufix.main.Constant;

public class Rune
{
  public final static List<Rune> runes=new ArrayList<>();

  private int templateId;
  private String statId; //hex string of stat
  private String nStatId; //hex string of stat
  private byte statsAdd; //flat stat added
  private float power; //flat power when negative
  private float nPower; //flat power when negative

  public Rune(int templateId, String statId, String nStatId, byte statsAdd, float power, float nPower)
  {
    this.setTemplateId(templateId);
    this.setStatId(statId);
    this.setnStatId(nStatId);
    this.setStatsAdd(statsAdd);
    this.setPower(power);
    this.setnPower(nPower);
    Rune.runes.add(this);
  }

  public static Rune getRuneById(int id)
  {
    for(Rune rune : runes)
      if(rune.getTemplateId()==id)
        return rune;
    return null;
  }

  public static Rune getRuneByStatId(String statId)
  {
    for(Rune rune : runes)
      if(rune.getStatId().compareTo(statId)==0)
        return rune;
    return null;
  }

  public static void addRunes()
  {
	  new Rune(1523,Integer.toHexString(Constant.STATS_ADD_VITA),Integer.toHexString(Constant.STATS_REM_VITA),(byte)5,1f,0.5f); //Vi
	    new Rune(1548,Integer.toHexString(Constant.STATS_ADD_VITA),Integer.toHexString(Constant.STATS_REM_VITA),(byte)15,2f,1.5f); //Pa Vi
	    new Rune(1554,Integer.toHexString(Constant.STATS_ADD_VITA),Integer.toHexString(Constant.STATS_REM_VITA),(byte)50,5f,3f); //Ra Vi
	    new Rune(1521,Integer.toHexString(Constant.STATS_ADD_SAGE),Integer.toHexString(Constant.STATS_REM_SAGE),(byte)1,1f,1f); //Sa
	    new Rune(1546,Integer.toHexString(Constant.STATS_ADD_SAGE),Integer.toHexString(Constant.STATS_REM_SAGE),(byte)3,2f,2f); //Pa Sa
	    new Rune(1552,Integer.toHexString(Constant.STATS_ADD_SAGE),Integer.toHexString(Constant.STATS_REM_SAGE),(byte)10,5f,5f); //Ra Sa
	    new Rune(1519,Integer.toHexString(Constant.STATS_ADD_FORC),Integer.toHexString(Constant.STATS_REM_FORC),(byte)1,1f,1f); //Fo
	    new Rune(1545,Integer.toHexString(Constant.STATS_ADD_FORC),Integer.toHexString(Constant.STATS_REM_FORC),(byte)3,2f,2f); //Pa Fo
	    new Rune(1551,Integer.toHexString(Constant.STATS_ADD_FORC),Integer.toHexString(Constant.STATS_REM_FORC),(byte)10,5f,5f); //Ra Fo
	    new Rune(1522,Integer.toHexString(Constant.STATS_ADD_INTE),Integer.toHexString(Constant.STATS_REM_INTE),(byte)1,1f,1f); //Ine
	    new Rune(1547,Integer.toHexString(Constant.STATS_ADD_INTE),Integer.toHexString(Constant.STATS_REM_INTE),(byte)3,2f,2f); //Pa Ine
	    new Rune(1553,Integer.toHexString(Constant.STATS_ADD_INTE),Integer.toHexString(Constant.STATS_REM_INTE),(byte)10,5f,5f); //Ra Ine
	    new Rune(1525,Integer.toHexString(Constant.STATS_ADD_CHAN),Integer.toHexString(Constant.STATS_REM_CHAN),(byte)1,1f,1f); //Cha
	    new Rune(1550,Integer.toHexString(Constant.STATS_ADD_CHAN),Integer.toHexString(Constant.STATS_REM_CHAN),(byte)3,2f,2f); //Pa Cha
	    new Rune(1556,Integer.toHexString(Constant.STATS_ADD_CHAN),Integer.toHexString(Constant.STATS_REM_CHAN),(byte)10,5f,5f); //Ra Cha
	    new Rune(1524,Integer.toHexString(Constant.STATS_ADD_AGIL),Integer.toHexString(Constant.STATS_REM_AGIL),(byte)1,1f,1f); //Age
	    new Rune(1549,Integer.toHexString(Constant.STATS_ADD_AGIL),Integer.toHexString(Constant.STATS_REM_AGIL),(byte)3,1f,1f); //Pa Age
	    new Rune(1555,Integer.toHexString(Constant.STATS_ADD_AGIL),Integer.toHexString(Constant.STATS_REM_AGIL),(byte)10,5f,5f); //Ra Age

	    new Rune(1557,Integer.toHexString(Constant.STATS_ADD_PA),Integer.toHexString(Constant.STATS_REM_PA),(byte)1,100f,50f); //Ga Pa
	    new Rune(1558,Integer.toHexString(Constant.STATS_ADD_PM),Integer.toHexString(Constant.STATS_REM_PM),(byte)1,90f,45f); //Ga Pme
	    new Rune(7438,Integer.toHexString(Constant.STATS_ADD_PO),Integer.toHexString(Constant.STATS_REM_PO),(byte)1,5f,3f); //Po
	    new Rune(7442,Integer.toHexString(Constant.STATS_ADD_SUM),Integer.toHexString(Constant.STATS_REM_SUM),(byte)1,10f,5f); //Sum
	    new Rune(7433,Integer.toHexString(Constant.STATS_ADD_CC),Integer.toHexString(Constant.STATS_REM_CC),(byte)1,5f,3f); //Cri
	    new Rune(7434,Integer.toHexString(Constant.STATS_ADD_SOIN),Integer.toHexString(Constant.STATS_REM_SOIN),(byte)1,5f,2f); //So

	    new Rune(7460,Integer.toHexString(Constant.STATS_ADD_RP_NEU),Integer.toHexString(Constant.STATS_REM_RP_NEU),(byte)1,2f,1f); //Neutral Re Per
	    new Rune(7459,Integer.toHexString(Constant.STATS_ADD_RP_TER),Integer.toHexString(Constant.STATS_REM_RP_TER),(byte)1,2f,1f); //Earth Re Per
	    new Rune(7457,Integer.toHexString(Constant.STATS_ADD_RP_FEU),Integer.toHexString(Constant.STATS_REM_RP_FEU),(byte)1,2f,1f); //Fire Re Per
	    new Rune(7560,Integer.toHexString(Constant.STATS_ADD_RP_EAU),Integer.toHexString(Constant.STATS_REM_RP_EAU),(byte)1,2f,1f); //Water Re Per
	    new Rune(7458,Integer.toHexString(Constant.STATS_ADD_RP_AIR),Integer.toHexString(Constant.STATS_ADD_RP_AIR),(byte)1,2f,1f); //Air Re Per
	    new Rune(7456,Integer.toHexString(Constant.STATS_ADD_R_NEU),Integer.toHexString(Constant.STATS_REM_R_NEU),(byte)1,2f,1f); //Neutral Re
	    new Rune(7455,Integer.toHexString(Constant.STATS_ADD_R_TER),Integer.toHexString(Constant.STATS_REM_R_TER),(byte)1,2f,1f); //Earth Re
	    new Rune(7452,Integer.toHexString(Constant.STATS_ADD_R_FEU),Integer.toHexString(Constant.STATS_REM_R_FEU),(byte)1,2f,1f); //Fire Re
	    new Rune(7454,Integer.toHexString(Constant.STATS_ADD_R_EAU),Integer.toHexString(Constant.STATS_REM_R_EAU),(byte)1,2f,1f); //Water Re
	    new Rune(7453,Integer.toHexString(Constant.STATS_ADD_R_AIR),Integer.toHexString(Constant.STATS_REM_R_AIR),(byte)1,2f,1f); //Air Re

	    new Rune(7435,Integer.toHexString(Constant.STATS_ADD_DOMA),Integer.toHexString(Constant.STATS_REM_DOMA),(byte)1,3f,2f); //Do
	    new Rune(7436,Integer.toHexString(Constant.STATS_ADD_PERDOM),Integer.toHexString(Constant.STATS_REM_PERDOM),(byte)1,2f,1f); //Do Per
	    new Rune(10618,Integer.toHexString(Constant.STATS_ADD_PERDOM),Integer.toHexString(Constant.STATS_REM_PERDOM),(byte)3,3f,1f); //Pa Do Per
	    new Rune(10619,Integer.toHexString(Constant.STATS_ADD_PERDOM),Integer.toHexString(Constant.STATS_REM_PERDOM),(byte)10,4f,2f); //Ra Do Per
	    new Rune(7437,Integer.toHexString(Constant.STATS_RETDOM),"",(byte)1,4f,2f); //Do Ren
	    new Rune(7446,Integer.toHexString(Constant.STATS_TRAPDOM),"",(byte)1,2f,1.5f); //Pi
	    new Rune(10613,Integer.toHexString(Constant.STATS_TRAPDOM),"",(byte)3,5f,2.5f); //Pa Pi
	    new Rune(7447,Integer.toHexString(Constant.STATS_TRAPPER),"",(byte)1,2f,1f); //Pi Per
	    new Rune(10615,Integer.toHexString(Constant.STATS_TRAPPER),"",(byte)3,3f,1f); //Pa Pi Per
	    new Rune(10616,Integer.toHexString(Constant.STATS_TRAPPER),"",(byte)10,5f,3f); //Ra Pi Per

	    new Rune(7443,Integer.toHexString(Constant.STATS_ADD_PODS),Integer.toHexString(Constant.STATS_REM_PODS),(byte)10,1.5f,0.5f); //Pod
	    new Rune(7444,Integer.toHexString(Constant.STATS_ADD_PODS),Integer.toHexString(Constant.STATS_REM_PODS),(byte)30,3.5f,2.5f); //Pa Pod
	    new Rune(7445,Integer.toHexString(Constant.STATS_ADD_PODS),Integer.toHexString(Constant.STATS_REM_PODS),(byte)100,4f,2f); //Ra Pod
	    new Rune(7448,Integer.toHexString(Constant.STATS_ADD_INIT),Integer.toHexString(Constant.STATS_REM_INIT),(byte)10,1f,0.5f); //Ini
	    new Rune(7449,Integer.toHexString(Constant.STATS_ADD_INIT),Integer.toHexString(Constant.STATS_REM_INIT),(byte)30,2f,1.5f); //Pa Ini
	    new Rune(7450,Integer.toHexString(Constant.STATS_ADD_INIT),Integer.toHexString(Constant.STATS_REM_INIT),(byte)100,3f,1f); //Ra Ini
	    new Rune(7451,Integer.toHexString(Constant.STATS_ADD_PROS),Integer.toHexString(Constant.STATS_REM_PROS),(byte)1,1f,1f); //Prospe
	    new Rune(10662,Integer.toHexString(Constant.STATS_ADD_PROS),Integer.toHexString(Constant.STATS_REM_PROS),(byte)3,3f,2f); //Pa Prospe
	    new Rune(10057,Integer.toHexString(Constant.STATS_HUNTING),"",(byte)1,5f,2.5f); //Hunting}
  }

  //v2.8 - Added support for more negative stats on items
  public static String getNegativeStatByRuneStat(String runeStat)
  {
    if(runeStat.compareTo("6f")==0) //AP
      runeStat="65";
    else if(runeStat.compareTo("80")==0) //MP
      runeStat="7f";
    else if(runeStat.compareTo("75")==0) //Range
      runeStat="74";
    else if(runeStat.compareTo("70")==0) //Damage
      runeStat="91";
    else if(runeStat.compareTo("b6")==0) //Summons
      runeStat="91";
    else if(runeStat.compareTo("73")==0) //Critical Hits
      runeStat="ab";
    else if(runeStat.compareTo("b2")==0) //Heal
      runeStat="b3";
    else if(runeStat.compareTo("d2")==0) //% Earth Res
      runeStat="d7";
    else if(runeStat.compareTo("d3")==0) //% Water Res
      runeStat="d8";
    else if(runeStat.compareTo("d4")==0) //% Air Res
      runeStat="d9";
    else if(runeStat.compareTo("d5")==0) //% Fire Res
      runeStat="da";
    else if(runeStat.compareTo("d6")==0) //% Neutral Res
      runeStat="db";
    else if(runeStat.compareTo("b0")==0) //Prospecting
      runeStat="b1";
    else if(runeStat.compareTo("7c")==0) //Wisdom
      runeStat="9c";
    else if(runeStat.compareTo("f3")==0) //Fire Res
      runeStat="f8";
    else if(runeStat.compareTo("f4")==0) //Neutral Res
      runeStat="f9";
    else if(runeStat.compareTo("f0")==0) //Earth Res
      runeStat="f5";
    else if(runeStat.compareTo("f1")==0) //Water Res
      runeStat="f6";
    else if(runeStat.compareTo("f2")==0) //Air Res
      runeStat="f7";
    else if(runeStat.compareTo("8a")==0) //% Damage
      runeStat="ba";
    else if(runeStat.compareTo("76")==0) //Strength
      runeStat="9d";
    else if(runeStat.compareTo("7e")==0) //Intelligence
      runeStat="9b";
    else if(runeStat.compareTo("7b")==0) //Chance
      runeStat="98";
    else if(runeStat.compareTo("77")==0) //Agility
      runeStat="9a";
    else if(runeStat.compareTo("9e")==0) //Pods
      runeStat="9f";
    else if(runeStat.compareTo("7d")==0) //Vitality
      runeStat="99";
    else if(runeStat.compareTo("ae")==0) //Initiative
      runeStat="af";
    return runeStat;
  }

  //v2.8 - Added support for more negative stats on items
  public static String getPositiveStatByRuneStat(String runeStat)
  {
    if(runeStat.compareTo("65")==0) //AP
      runeStat="6f";
    else if(runeStat.compareTo("7f")==0) //MP
      runeStat="80";
    else if(runeStat.compareTo("74")==0) //Range
      runeStat="75";
    else if(runeStat.compareTo("91")==0) //Damage
      runeStat="70";
    else if(runeStat.compareTo("91")==0) //Summons
      runeStat="b6";
    else if(runeStat.compareTo("ab")==0) //Critical Hits
      runeStat="73";
    else if(runeStat.compareTo("b3")==0) //Heal
      runeStat="b2";
    else if(runeStat.compareTo("d7")==0) //% Earth Res
      runeStat="d2";
    else if(runeStat.compareTo("d8")==0) //% Water Res
      runeStat="d3";
    else if(runeStat.compareTo("d9")==0) //% Air Res
      runeStat="d4";
    else if(runeStat.compareTo("da")==0) //% Fire Res
      runeStat="d5";
    else if(runeStat.compareTo("db")==0) //% Neutral Res
      runeStat="d6";
    else if(runeStat.compareTo("b1")==0) //Prospecting
      runeStat="b0";
    else if(runeStat.compareTo("9c")==0) //Wisdom
      runeStat="7c";
    else if(runeStat.compareTo("f8")==0) //Fire Res
      runeStat="f3";
    else if(runeStat.compareTo("f9")==0) //Neutral Res
      runeStat="f4";
    else if(runeStat.compareTo("f5")==0) //Earth Res
      runeStat="f0";
    else if(runeStat.compareTo("f6")==0) //Water Res
      runeStat="f1";
    else if(runeStat.compareTo("f7")==0) //Air Res
      runeStat="f2";
    else if(runeStat.compareTo("ba")==0) //% Damage
      runeStat="8a";
    else if(runeStat.compareTo("9d")==0) //Strength
      runeStat="76";
    else if(runeStat.compareTo("9b")==0) //Intelligence
      runeStat="7e";
    else if(runeStat.compareTo("98")==0) //Chance
      runeStat="7b";
    else if(runeStat.compareTo("9a")==0) //Agility
      runeStat="77";
    else if(runeStat.compareTo("9f")==0) //Pods
      runeStat="9e";
    else if(runeStat.compareTo("99")==0) //Vitality
      runeStat="7d";
    else if(runeStat.compareTo("af")==0) //Initiative
      runeStat="ae";
    return runeStat;
  }

  //v2.8 - returns true if hexadecimal string runeStat is a negative stat
  public static boolean isNegativeStat(String stat)
  {
    if(stat.equalsIgnoreCase("65")||stat.equalsIgnoreCase("7f")||stat.equalsIgnoreCase("74")||stat.equalsIgnoreCase("91")||stat.equalsIgnoreCase("bb")||stat.equalsIgnoreCase("ab")||stat.equalsIgnoreCase("b3")||stat.equalsIgnoreCase("d7")||stat.equalsIgnoreCase("d8")||stat.equalsIgnoreCase("d9")||stat.equalsIgnoreCase("da")||stat.equalsIgnoreCase("db")||stat.equalsIgnoreCase("b1")||stat.equalsIgnoreCase("9c")||stat.equalsIgnoreCase("f5")||stat.equalsIgnoreCase("f6")||stat.equalsIgnoreCase("f7")||stat.equalsIgnoreCase("f8")||stat.equalsIgnoreCase("f9")||stat.equalsIgnoreCase("ba")||stat.equalsIgnoreCase("9d")||stat.equalsIgnoreCase("9b")||stat.equalsIgnoreCase("98")||stat.equalsIgnoreCase("9a")||stat.equalsIgnoreCase("9f")||stat.equalsIgnoreCase("99")||stat.equalsIgnoreCase("af"))
      return true;
    return false;
  }

  public float getnPower()
  {
    return nPower;
  }

  public void setnPower(float nPower)
  {
    this.nPower=nPower;
  }

  public float getPower()
  {
    return power;
  }

  public void setPower(float power)
  {
    this.power=power;
  }

  public byte getStatsAdd()
  {
    return statsAdd;
  }

  public void setStatsAdd(byte statsAdd)
  {
    this.statsAdd=statsAdd;
  }

  public String getStatId()
  {
    return statId;
  }

  public void setStatId(String statId)
  {
    this.statId=statId;
  }

  public int getTemplateId()
  {
    return templateId;
  }

  public void setTemplateId(int templateId)
  {
    this.templateId=templateId;
  }

  public String getnStatId()
  {
    return nStatId;
  }

  public void setnStatId(String nStatId)
  {
    this.nStatId=nStatId;
  }
}