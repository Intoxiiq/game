package soufix.job.fm;

import java.util.ArrayList;
import java.util.List;

public class Potion
{
  public final static List<Potion> potions=new ArrayList<>();

  private int templateId;
  private byte level;
  private byte strength; //strength of potion
  private byte statId; //id of stat (not hex)
  private byte power;

  public Potion(int templateId, byte level, byte strength, byte statId, byte power)
  {
    this.setTemplateId(templateId);
    this.setLevel(level);
    this.setStrength(strength);
    this.setStatId(statId);
    this.setPower(power);
    Potion.potions.add(this);
  }

  public static Potion getPotionById(int id)
  {
    for(Potion potion : potions)
      if(potion.templateId==id)
        return potion;
    return null;
  }

  public byte getStrength()
  {
    return strength;
  }

  public void setStrength(byte strength)
  {
    this.strength=strength;
  }

  public byte getStatId()
  {
    return statId;
  }

  public void setStatId(byte statId)
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
  
  public int getPower()
  {
    return power;
  }

  public void setPower(byte power)
  {
    this.power=power;
  }

  public static void addPotions()
  {
    //fire
    new Potion(1333,(byte)1,(byte)50,(byte)99, (byte)3);
    new Potion(1343,(byte)25,(byte)65,(byte)99, (byte)10);
    new Potion(1345,(byte)50,(byte)80,(byte)99, (byte)20);
    //water
    new Potion(1335,(byte)1,(byte)50,(byte)96, (byte)3);
    new Potion(1341,(byte)25,(byte)65,(byte)96, (byte)10);
    new Potion(1346,(byte)50,(byte)80,(byte)96, (byte)20);
    //air
    new Potion(1337,(byte)1,(byte)50,(byte)98, (byte)3);
    new Potion(1342,(byte)25,(byte)65,(byte)98, (byte)10);
    new Potion(1347,(byte)50,(byte)80,(byte)98, (byte)20);
    //earth
    new Potion(1338,(byte)1,(byte)50,(byte)97, (byte)3);
    new Potion(1340,(byte)25,(byte)65,(byte)97, (byte)10);
    new Potion(1348,(byte)50,(byte)80,(byte)97, (byte)20);
  }

  public byte getLevel()
  {
    return level;
  }

  public void setLevel(byte level)
  {
    this.level = level;
  }
}
