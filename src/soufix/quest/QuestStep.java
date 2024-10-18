package soufix.quest;

import soufix.entity.npc.NpcTemplate;
import soufix.main.Main;

import java.util.HashMap;
import java.util.Map;

public class QuestStep
{
  private static Map<Integer, QuestStep> questStepList=new HashMap<>();

  public static Map<Integer, QuestStep> getQuestStepList()
  {
    return questStepList;
  }

  public static QuestStep getQuestStepById(int id)
  {
    return questStepList.get(id);
  }

  public static void addQuestStep(QuestStep step)
  {
    questStepList.put(step.getId(),step);
  }
  //endregion

  private int id;
  private short type;
  private int objectif;
  private Quest quest=null;
  private Map<Integer, Integer> itemNecessary=new HashMap<>();//ItemId,Qua
  private NpcTemplate npc=null;
  private int monsterId;
  private short qua;
  private String condition=null;
  private int validationType;

  public QuestStep(int id, int type, int objectif, String items, int npc, String monsters, String condition, int validationType)
  {
    this.id=id;
    this.type=(short)type;
    this.objectif=objectif;
    this.npc=Main.world.getNPCTemplate(npc);
    this.condition=condition;
    this.validationType=validationType;

    try
    {
      if(!items.equalsIgnoreCase(""))
      {
        String[] split=items.split(";");

        if(split.length>0)
        {
          for(String data : split)
          {
            String[] loc1=data.split(",");
            this.itemNecessary.put(Integer.parseInt(loc1[0]),Integer.parseInt(loc1[1]));
          }
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    try
    {
      if(monsters.contains(",")&&!monsters.equals("0"))
      {
        String[] loc0=monsters.split(",");
        this.setMonsterId(Integer.parseInt(loc0[0]));
        this.setQua(Short.parseShort(loc0[1])); // Des quétes avec le truc vide ! ><
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    QuestObjectif questObjectif=QuestObjectif.getQuestObjectifById(this.objectif);
    if(questObjectif!=null)
    {
      questObjectif.addQuestStep(this);
    }
  }

  public int getId()
  {
    return id;
  }

  public short getType()
  {
    return type;
  }

  int getObjectif()
  {
    return objectif;
  }

  public Quest getQuestData()
  {
    return quest;
  }

  void setQuestData(Quest aQuest)
  {
    quest=aQuest;
  }

  public Map<Integer, Integer> getItemNecessaryList()
  {
    return itemNecessary;
  }

  public NpcTemplate getNpc()
  {
    return npc;
  }

  public String getCondition()
  {
    return condition;
  }

  public int getMonsterId()
  {
    return monsterId;
  }

  private void setMonsterId(int monsterId)
  {
    this.monsterId=monsterId;
  }

  public short getQua()
  {
    return qua;
  }

  public void setQua(short qua)
  {
    this.qua=qua;
  }

  public int getValidationType()
  {
    return validationType;
  }
}
