package soufix.quest;

import soufix.client.Player;
import soufix.database.Database;
import soufix.game.World;
import soufix.main.Main;

import java.util.HashMap;
import java.util.Map;

public class QuestPlayer
{

  private int id;
  private Quest quest=null;
  private boolean finish;
  private Player player;
  private Map<Integer, QuestStep> stepsValidate=new HashMap<>();
  private Map<Integer, Short> monsterKill=new HashMap<>();

  public QuestPlayer(int id, int quest, boolean finish, int player, String steps)
  {
    this.id=id;
    this.quest=Quest.getQuestById(quest);
    this.finish=finish;
    this.player=Main.world.getPlayer(player);

    try
    {
      String[] split=steps.split(";");
      if(split.length>0)
      {
        for(String data : split)
        {
          if(!data.equalsIgnoreCase(""))
          {
            QuestStep step=QuestStep.getQuestStepById(Integer.parseInt(data));
            this.stepsValidate.put(step.getId(),step);
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

  public Quest getQuest()
  {
    return quest;
  }

  public boolean isFinish()
  {
    return finish;
  }

  public void setFinish(boolean finish)
  {
    this.finish=finish;
    
    if(this.player != null)
    World.get_Succes(player.getId()).quete_add(this.player);
    
    if(this.getQuest()!=null&&this.getQuest().isDelete())
    {
      if(this.player!=null&&this.player.getQuestPerso()!=null&&this.player.getQuestPerso().containsKey(this.getId()))
      {
        this.player.delQuestPerso(this.getId());
        this.removeQuestPlayer();
      }
    }
    else if(this.getQuest()==null)
    {
      if(this.player.getQuestPerso().containsKey(this.getId()))
      {
        this.player.delQuestPerso(this.getId());
        this.removeQuestPlayer();
      }
    }
  }

  public Player getPlayer()
  {
    return player;
  }

  public boolean isQuestStepIsValidate(QuestStep step)
  {
    return stepsValidate.containsKey(step.getId());
  }

  public void setQuestStepValidate(QuestStep step)
  {
    if(!stepsValidate.containsKey(step.getId()))
      stepsValidate.put(step.getId(),step);
  }

  public String getQuestStepString()
  {
    StringBuilder str=new StringBuilder();
    int nb=0;
    for(QuestStep step : this.stepsValidate.values())
    {
      nb++;
      str.append(step.getId());
      if(nb<this.stepsValidate.size())
        str.append(";");
    }
    return str.toString();
  }

  public Map<Integer, Short> getMonsterKill()
  {
    return monsterKill;
  }

  public boolean overQuestStep(QuestObjectif qObjectif)
  {
    int nbrQuest=0;
    for(QuestStep step : this.stepsValidate.values())
    {
      if(step.getObjectif()==qObjectif.getId())
        nbrQuest++;
    }
    return qObjectif.getSizeUnique()==nbrQuest;
  }

  public boolean removeQuestPlayer()
  {
    return Database.getDynamics().getQuestPlayerData().delete(this.id);
  }
}