package soufix.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import soufix.other.Action;

public class QuestObjectif
{
  private static Map<Integer, QuestObjectif> questObjectifList=new HashMap<>();

  public static Map<Integer, QuestObjectif> getQuestObjectifList()
  {
    return questObjectifList;
  }

  static QuestObjectif getQuestObjectifById(int id)
  {
    return questObjectifList.get(id);
  }

  public static void addQuestObjectif(QuestObjectif questObjectif)
  {
    questObjectifList.put(questObjectif.getId(),questObjectif);
  }
  //endregion

  private int id;
  private int xp;
  private int kamas;
  private Map<Integer, Integer> objects=new HashMap<>();
  private ArrayList<Action> actions=new ArrayList<>();
  private ArrayList<QuestStep> questSteps=new ArrayList<>();

  public QuestObjectif(int id, int xp, int kamas, String objects, String actions)
  {
    this.id=id;
    this.xp=xp;
    this.kamas=kamas;
    try
    {
      if(!objects.equalsIgnoreCase(""))
      {
        String[] split=objects.split(";");
        if(split.length>0)
        {
          for(String loc1 : split)
          {
            if(!loc1.equalsIgnoreCase(""))
            {
              if(loc1.contains(","))
              {
                String[] loc2=loc1.split(",");
                this.objects.put(Integer.parseInt(loc2[0]),Integer.parseInt(loc2[1]));
              }
              else
              {
                this.objects.put(Integer.parseInt(loc1),1);
              }
            }
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
      if(actions!=null&&!actions.equalsIgnoreCase(""))
      {
        String[] split=actions.split(";");
        if(split.length>0)
        {
          for(String loc1 : split)
          {
            String[] loc2=loc1.split("\\|");
            this.actions.add(new Action(Integer.parseInt(loc2[0]),loc2[1],"-1",null));
          }
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public int getId()
  {
    return id;
  }

  public int getXp()
  {
    return xp;
  }

  public int getKamas()
  {
    return kamas;
  }

  public Map<Integer, Integer> getObjects()
  {
    return objects;
  }

  ArrayList<Action> getActions()
  {
    return actions;
  }

  int getSizeUnique()
  {
    int cpt=0;
    ArrayList<Integer> id=new ArrayList<>();
    for(QuestStep step : this.questSteps)
    {
      if(!id.contains(step.getId()))
      {
        id.add(step.getId());
        cpt++;
      }
    }
    return cpt;
  }

  void addQuestStep(QuestStep step)
  {
    if(!this.questSteps.contains(step))
    {
      this.questSteps.add(step);
    }
  }
}