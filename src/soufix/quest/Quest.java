/*package scruffemu.quest;

import scruffemu.client.Player;
import scruffemu.common.SocketManager;
import scruffemu.database.Database;
import scruffemu.entity.npc.NpcTemplate;
import scruffemu.game.World;
import scruffemu.utility.Pair;
import scruffemu.game.action.ExchangeAction;
import scruffemu.main.Config;
import scruffemu.main.Constant;
import scruffemu.object.GameObject;
import scruffemu.object.ObjectTemplate;
import scruffemu.other.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Quest
{
  public static Map<Integer, Quest> questDataList=new HashMap<Integer, Quest>();

  private int id;
  private ArrayList<Quest_Etape> questEtapeList=new ArrayList<Quest_Etape>();
  private ArrayList<Quest_Objectif> questObjectifList=new ArrayList<Quest_Objectif>();
  private NpcTemplate npc=null;
  private ArrayList<Action> actions=new ArrayList<Action>();
  private boolean delete;
  private Pair<Integer, Integer> condition=null;

  public Quest(int aId, String questEtape, String aObjectif, int aNpc, String action, String args, boolean delete, String condition)
  {
    this.id=aId;
    this.delete=delete;
    try
    {
      if(!questEtape.equalsIgnoreCase(""))
      {
        String[] split=questEtape.split(";");

        if(split!=null&&split.length>0)
        {
          for(String qEtape : split)
          {
            Quest_Etape q_Etape=Quest_Etape.getQuestEtapeById(Integer.parseInt(qEtape));
            q_Etape.setQuestData(this);
            questEtapeList.add(q_Etape);
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
      if(!aObjectif.equalsIgnoreCase(""))
      {
        String[] split=aObjectif.split(";");

        if(split!=null&&split.length>0)
        {
          for(String qObjectif : split)
          {
            questObjectifList.add(Quest_Objectif.getQuestObjectifById(Integer.parseInt(qObjectif)));
          }
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if(!condition.equalsIgnoreCase(""))
    {
      try
      {
        String[] split=condition.split(":");
        if(split!=null&&split.length>0)
        {
          this.condition=new Pair<Integer, Integer>(Integer.parseInt(split[0]),Integer.parseInt(split[1]));
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    this.npc=Main.world.getNPCTemplate(aNpc);
    try
    {
      if(!action.equalsIgnoreCase("")&&!args.equalsIgnoreCase(""))
      {
        String[] arguments=args.split(";");
        int nbr=0;
        for(String loc0 : action.split(","))
        {
          int actionId=Integer.parseInt(loc0);
          String arg=arguments[nbr];
          actions.add(new Action(actionId,arg,-1+"",null));
          nbr++;
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Main.world.logger.error("Erreur avec l action et les args de la quete "+this.id+".");
    }
  }

  public static Map<Integer, Quest> getQuestDataList()
  {
    return questDataList;
  }

  public static Quest getQuestById(int id)
  {
    return questDataList.get(id);
  }

  public static void setQuestInList(Quest quest)
  {
    questDataList.put(quest.getId(),quest);
  }

  public boolean isDelete()
  {
    return this.delete;
  }

  public int getId()
  {
    return id;
  }

  public ArrayList<Quest_Objectif> getObjectifList()
  {
    return questObjectifList;
  }

  public NpcTemplate getNpc_Tmpl()
  {
    return npc;
  }

  public ArrayList<Quest_Etape> getQuestEtapeList()
  {
    return questEtapeList;
  }

  public boolean haveRespectCondition(QuestPlayer qPerso, Quest_Etape qEtape)
  {
    switch(qEtape.getCondition())
    {
      case "1": //Valider les etapes d'avant
        boolean loc2=true;
        for(Quest_Etape aEtape : questEtapeList)
        {
          if(aEtape==null)
            continue;
          if(aEtape.getId()==qEtape.getId())
            continue;
          if(!qPerso.isQuestEtapeIsValidate(aEtape))
            loc2=false;
        }
        return loc2;

      case "0":
        return true;
    }
    return false;
  }

  public String getGmQuestDataPacket(Player perso)
  {
    QuestPlayer qPerso=perso.getQuestPersoByQuest(this);
    int loc1=getObjectifCurrent(qPerso);
    int loc2=getObjectifPrevious(qPerso);
    int loc3=getNextObjectif(Quest_Objectif.getQuestObjectifById(getObjectifCurrent(qPerso)));
    StringBuilder str=new StringBuilder();
    str.append(id).append("|");
    str.append(loc1>0 ? loc1 : "");
    str.append("|");

    StringBuilder str_prev=new StringBuilder();
    boolean loc4=true;
    for(Quest_Etape qEtape : questEtapeList)
    {
      if(qEtape.getObjectif()!=loc1)
        continue;
      if(!haveRespectCondition(qPerso,qEtape))
        continue;
      if(!loc4)
        str_prev.append(";");
      str_prev.append(qEtape.getId());
      str_prev.append(",");
      str_prev.append(qPerso.isQuestEtapeIsValidate(qEtape) ? 1 : 0);
      loc4=false;
    }
    str.append(str_prev);
    str.append("|");
    str.append(loc2>0 ? loc2 : "").append("|");
    str.append(loc3>0 ? loc3 : "");
    if(npc!=null)
    {
      str.append("|");
      str.append(npc.getInitQuestionId(perso.getCurMap().getId())).append("|");
    }
    return str.toString();
  }

  public Quest_Etape getQuestEtapeCurrent(QuestPlayer qPerso)
  {
    for(Quest_Etape qEtape : getQuestEtapeList())
    {
      if(!qPerso.isQuestEtapeIsValidate(qEtape))
        return qEtape;
    }
    return null;
  }

  public int getObjectifCurrent(QuestPlayer qPerso)
  {
    for(Quest_Etape qEtape : questEtapeList)
    {
      if(qPerso.isQuestEtapeIsValidate(qEtape))
        continue;
      return qEtape.getObjectif();
    }
    return 0;
  }

  public int getObjectifPrevious(QuestPlayer qPerso)
  {
    if(questObjectifList.size()==1)
      return 0;
    else
    {
      int previousqObjectif=0;
      for(Quest_Objectif qObjectif : questObjectifList)
      {
        if(qObjectif.getId()==getObjectifCurrent(qPerso))
          return previousqObjectif;
        else
          previousqObjectif=qObjectif.getId();
      }
    }
    return 0;
  }

  public int getNextObjectif(Quest_Objectif qO)
  {
    if(qO==null)
      return 0;
    for(Quest_Objectif qObjectif : questObjectifList)
    {
      if(qObjectif.getId()==qO.getId())
      {
        int index=questObjectifList.indexOf(qObjectif);
        if(questObjectifList.size()<=index+1)
          return 0;
        return questObjectifList.get(index+1).getId();
      }
    }
    return 0;
  }

  //v2.4 - Map Quest fix
  public void applyQuest(Player perso)
  {
    if(this.condition!=null)
    {
      switch(this.condition.getLeft())
      {
        case 1: // Niveau
          if(perso.getLevel()<this.condition.getRight())
          {
            SocketManager.GAME_SEND_MESSAGE(perso,"Your level is too low to start this quest.");
            return;
          }
          break;
      }
    }
    QuestPlayer qPerso=new QuestPlayer(Database.getStatics().getQuestPlayerData().getNextId(),id,false,perso.getId(),"");
    perso.addQuestPerso(qPerso);
    SocketManager.GAME_SEND_Im_PACKET(perso,"054;"+id);
    Database.getStatics().getQuestPlayerData().add(qPerso);

    SocketManager.GAME_SEND_MAP_NPCS_GMS_PACKETS(perso.getGameClient(),perso.getCurMap());
    SocketManager.GAME_SEND_GA2_PACKET(perso.getGameClient(),perso.getId()); //2.0 - Update map when accepting quest
    SocketManager.GAME_SEND_MAPDATA(perso.getGameClient(),perso.getCurMap().getId(),perso.getCurMap().getDate(),perso.getCurMap().getKey());

    if(!actions.isEmpty())
      for(Action aAction : actions)
        aAction.apply(perso,perso,-1,-1);
    Database.getStatics().getPlayerData().update(perso);
  }

  public void updateQuestData(Player perso, boolean validation, int type)
  {
    QuestPlayer qPerso=perso.getQuestPersoByQuest(this);
    for(Quest_Etape qEtape : questEtapeList)
    {
      if(qEtape.getValidationType()!=type)
        continue;

      boolean refresh=false;
      if(qPerso.isQuestEtapeIsValidate(qEtape)) //On a déjé validé la questEtape on passe
        continue;

      if(qEtape.getObjectif()!=getObjectifCurrent(qPerso))
        continue;

      if(!haveRespectCondition(qPerso,qEtape))
        continue;

      if(validation)
        refresh=true;
      switch(qEtape.getType())
      {

        case 3://Donner item
          if(perso.getExchangeAction()!=null&&perso.getExchangeAction().getType()==ExchangeAction.TALKING_WITH&&perso.getCurMap().getNpc((Integer)perso.getExchangeAction().getValue()).getTemplate().getId()==qEtape.getNpc().getId())
          {
            for(Entry<Integer, Integer> entry : qEtape.getItemNecessaryList().entrySet())
            {
              if(perso.hasItemTemplate(entry.getKey(),entry.getValue()))
              { //Il a l'item et la quantité
                perso.removeByTemplateID(entry.getKey(),entry.getValue()); //On supprime donc
                refresh=true;
              }
            }
          }
          break;

        case 0:
        case 1://Aller voir %
        case 9://Retourner voir %
          if(qEtape.getCondition().equalsIgnoreCase("1"))
          { //Valider les questEtape avant
            if(perso.getExchangeAction()!=null&&perso.getExchangeAction().getType()==ExchangeAction.TALKING_WITH&&perso.getCurMap().getNpc((Integer)perso.getExchangeAction().getValue()).getTemplate().getId()==qEtape.getNpc().getId())
            {
              if(haveRespectCondition(qPerso,qEtape))
              {
                refresh=true;
              }
            }
          } else
          {
            if(perso.getExchangeAction()!=null&&perso.getExchangeAction().getType()==ExchangeAction.TALKING_WITH&&perso.getCurMap().getNpc((Integer)perso.getExchangeAction().getValue()).getTemplate().getId()==qEtape.getNpc().getId())
              refresh=true;
          }
          break;

        case 6: // monstres
          for(Entry<Integer, Short> entry : qPerso.getMonsterKill().entrySet())
            if(entry.getKey()==qEtape.getMonsterId()&&entry.getValue()>=qEtape.getQua())
              refresh=true;
          break;

        case 10://Ramener prisonnier
          if(perso.getExchangeAction()!=null&&perso.getExchangeAction().getType()==ExchangeAction.TALKING_WITH&&perso.getCurMap().getNpc((Integer)perso.getExchangeAction().getValue()).getTemplate().getId()==qEtape.getNpc().getId())
          {
            GameObject follower=perso.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR);
            if(follower!=null)
            {
              Map<Integer, Integer> itemNecessaryList=qEtape.getItemNecessaryList();
              for(Entry<Integer, Integer> entry2 : itemNecessaryList.entrySet())
              {
                if(entry2.getKey()==follower.getTemplate().getId())
                {
                  refresh=true;
                  perso.setMascotte(0);
                }
              }
            }
          }
          break;
      }

      if(refresh)
      {
        Quest_Objectif ansObjectif=Quest_Objectif.getQuestObjectifById(getObjectifCurrent(qPerso));
        qPerso.setQuestEtapeValidate(qEtape);
        SocketManager.GAME_SEND_Im_PACKET(perso,"055;"+id);
        if(haveFinish(qPerso,ansObjectif))
        {
          SocketManager.GAME_SEND_Im_PACKET(perso,"056;"+id);
          applyButinOfQuest(perso,qPerso,ansObjectif);
          qPerso.setFinish(true);
        } else
        {
          if(getNextObjectif(ansObjectif)!=0)
          {
            if(qPerso.overQuestEtape(ansObjectif))
              applyButinOfQuest(perso,qPerso,ansObjectif);
          }
        }
        Database.getStatics().getPlayerData().update(perso);
      }
    }
  }

  public boolean haveFinish(QuestPlayer qPerso, Quest_Objectif qO)
  {
    return qPerso.overQuestEtape(qO)&&getNextObjectif(qO)==0;
  }

  public void applyButinOfQuest(Player perso, QuestPlayer qPerso, Quest_Objectif ansObjectif)
  {
    long aXp=0;

    if((aXp=ansObjectif.getXp())>0)
    { //Xp a donner
      perso.addXp(aXp*((int)Config.getInstance().rateXp));
      SocketManager.GAME_SEND_Im_PACKET(perso,"08;"+(aXp*((int)Config.getInstance().rateXp)));
      SocketManager.GAME_SEND_STATS_PACKET(perso);
    }

    if(ansObjectif.getItem().size()>0)
    { //Item a donner
      for(Entry<Integer, Integer> entry : ansObjectif.getItem().entrySet())
      {
        ObjectTemplate objT=Main.world.getObjTemplate(entry.getKey());
        int qua=entry.getValue();
        GameObject obj=objT.createNewItem(qua,false);
        if(perso.addObjet(obj,true))
          World.addGameObject(obj,true);
        SocketManager.GAME_SEND_Im_PACKET(perso,"021;"+qua+"~"+objT.getId());
      }
    }

    int aKamas=0;
    if((aKamas=ansObjectif.getKamas())>0)
    { //Kams a donner
      perso.setKamas(perso.getKamas()+(long)aKamas);
      SocketManager.GAME_SEND_Im_PACKET(perso,"045;"+aKamas);
      SocketManager.GAME_SEND_STATS_PACKET(perso);
    }

    if(getNextObjectif(ansObjectif)!=ansObjectif.getId())
    { //On passe au nouveau objectif on applique les actions
      for(Action a : ansObjectif.getAction())
      {
        a.apply(perso,null,0,0);
      }
    }

  }

  public int getQuestEtapeByObjectif(Quest_Objectif qObjectif)
  {
    int nbr=0;
    for(Quest_Etape qEtape : getQuestEtapeList())
    {
      if(qEtape.getObjectif()==qObjectif.getId())
        nbr++;
    }
    return nbr;
  }

  public ArrayList<Action> getActions()
  {
    return actions;
  }

  public void setActions(ArrayList<Action> actions)
  {
    this.actions=actions;
  }

  public static class QuestPlayer
  {
    private int id;
    private Quest quest=null;
    private boolean finish;
    private Player player;
    private Map<Integer, Quest_Etape> questEtapeListValidate=new HashMap<Integer, Quest_Etape>();
    private Map<Integer, Short> monsterKill=new HashMap<Integer, Short>();

    public QuestPlayer(int aId, int qId, boolean aFinish, int pId, String qEtapeV)
    {
      this.id=aId;
      this.quest=Quest.getQuestById(qId);
      this.finish=aFinish;
      this.player=Main.world.getPlayer(pId);
      try
      {
        String[] split=qEtapeV.split(";");
        if(split!=null&&split.length>0)
        {
          for(String loc1 : split)
          {
            if(loc1.equalsIgnoreCase(""))
              continue;
            Quest_Etape qEtape=Quest_Etape.getQuestEtapeById(Integer.parseInt(loc1));
            questEtapeListValidate.put(qEtape.getId(),qEtape);
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
      if(this.getQuest()!=null&&this.getQuest().isDelete())
      {
        if(this.player!=null&&this.player.getQuestPerso()!=null&&this.player.getQuestPerso().containsKey(this.getId()))
        {
          this.player.delQuestPerso(this.getId());
          this.deleteQuestPerso();
        }
      } else if(this.getQuest()==null)
      {
        if(this.player.getQuestPerso().containsKey(this.getId()))
        {
          this.player.delQuestPerso(this.getId());
          this.deleteQuestPerso();
        }
      }
    }

    public Player getPlayer()
    {
      return player;
    }

    public boolean isQuestEtapeIsValidate(Quest_Etape qEtape)
    {
      return questEtapeListValidate.containsKey(qEtape.getId());
    }

    public void setQuestEtapeValidate(Quest_Etape qEtape)
    {
      if(!questEtapeListValidate.containsKey(qEtape.getId()))
        questEtapeListValidate.put(qEtape.getId(),qEtape);
    }

    public String getQuestEtapeString()
    {
      StringBuilder str=new StringBuilder();
      int nb=0;
      for(Quest_Etape qEtape : questEtapeListValidate.values())
      {
        nb++;
        str.append(qEtape.getId());
        if(nb<questEtapeListValidate.size())
          str.append(";");
      }
      return str.toString();
    }

    public Map<Integer, Short> getMonsterKill()
    {
      return monsterKill;
    }

    public boolean overQuestEtape(Quest_Objectif qObjectif)
    {
      int nbrQuest=0;
      for(Quest_Etape qEtape : questEtapeListValidate.values())
      {
        if(qEtape.getObjectif()==qObjectif.getId())
          nbrQuest++;
      }
      return qObjectif.getSizeUnique()==nbrQuest;
    }

    public boolean deleteQuestPerso()
    {
      return Database.getStatics().getQuestPlayerData().delete(this.id);
    }
  }

  public static class Quest_Objectif
  {

    public static Map<Integer, Quest_Objectif> questObjectifList=new HashMap<Integer, Quest_Objectif>();

    private int id;

    private int xp;
    private int kamas;
    private Map<Integer, Integer> items=new HashMap<Integer, Integer>();
    private ArrayList<Action> actionList=new ArrayList<Action>();
    private ArrayList<Quest_Etape> questEtape=new ArrayList<Quest_Etape>();

    public Quest_Objectif(int aId, int aXp, int aKamas, String aItems, String aAction)
    {
      this.id=aId;
      this.xp=aXp;
      this.kamas=aKamas;
      try
      {
        if(!aItems.equalsIgnoreCase(""))
        {
          String[] split=aItems.split(";");
          if(split!=null&&split.length>0)
          {
            for(String loc1 : split)
            {
              if(loc1.equalsIgnoreCase(""))
                continue;
              if(loc1.contains(","))
              {
                String[] loc2=loc1.split(",");
                this.items.put(Integer.parseInt(loc2[0]),Integer.parseInt(loc2[1]));
              } else
              {
                this.items.put(Integer.parseInt(loc1),1);
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
        if(aAction!=null&&!aAction.equalsIgnoreCase(""))
        {
          String[] split=aAction.split(";");
          if(split!=null&split.length>0)
          {
            for(String loc1 : split)
            {
              String[] loc2=loc1.split("\\|");
              int actionId=Integer.parseInt(loc2[0]);
              String args=loc2[1];
              Action action=new Action(actionId,args,"-1",null);
              actionList.add(action);
            }
          }
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }

    public static Quest_Objectif getQuestObjectifById(int id)
    {
      return questObjectifList.get(id);
    }

    public static Map<Integer, Quest_Objectif> getQuestObjectifList()
    {
      return questObjectifList;
    }

    public static void setQuest_Objectif(Quest_Objectif qObjectif)
    {
      if(!questObjectifList.containsKey(qObjectif.getId())&&!questObjectifList.containsValue(qObjectif))
        questObjectifList.put(qObjectif.getId(),qObjectif);
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

    public Map<Integer, Integer> getItem()
    {
      return items;
    }

    public ArrayList<Action> getAction()
    {
      return actionList;
    }

    public int getSizeUnique()
    {
      int cpt=0;
      ArrayList<Integer> id=new ArrayList<Integer>();
      for(Quest_Etape qe : questEtape)
      {
        if(!id.contains(qe.getId()))
        {
          id.add(qe.getId());
          cpt++;
        }
      }
      return cpt;
    }

    public ArrayList<Quest_Etape> getQuestEtapeList()
    {
      return questEtape;
    }

    public void setEtape(Quest_Etape qEtape)
    {
      if(!questEtape.contains(qEtape))
        questEtape.add(qEtape);
    }
  }

}*/

package soufix.quest;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.npc.NpcTemplate;
import soufix.game.World;
import soufix.game.action.ExchangeAction;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.object.ObjectTemplate;
import soufix.other.Action;
import soufix.utility.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Quest
{
  private static Map<Integer, Quest> questList=new HashMap<>();

  public static Map<Integer, Quest> getQuestList()
  {
    return questList;
  }

  public static Quest getQuestById(int id)
  {
    return questList.get(id);
  }

  public static void addQuest(Quest quest)
  {
    questList.put(quest.getId(),quest);
  }

  private int id;
  private ArrayList<QuestStep> questSteps=new ArrayList<>();
  private ArrayList<QuestObjectif> questObjectifList=new ArrayList<>();
  private NpcTemplate npc=null;
  private ArrayList<Action> actions=new ArrayList<>();
  private boolean delete;
  private Pair<Integer, Integer> condition=null;

  public Quest(int id, String steps, String objectifs, int npc, String action, String args, boolean delete, String condition)
  {
    this.id=id;
    this.delete=delete;
    try
    {
      if(!steps.equalsIgnoreCase(""))
      {
        String[] split=steps.split(";");

        if(split.length>0)
        {
          for(String qEtape : split)
          {
            QuestStep q_Etape=QuestStep.getQuestStepById(Integer.parseInt(qEtape));
            q_Etape.setQuestData(this);
            questSteps.add(q_Etape);
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
      if(!objectifs.equalsIgnoreCase(""))
      {
        String[] split=objectifs.split(";");

        if(split.length>0)
        {
          for(String qObjectif : split)
          {
            questObjectifList.add(QuestObjectif.getQuestObjectifById(Integer.parseInt(qObjectif)));
          }
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if(!condition.equalsIgnoreCase(""))
    {
      try
      {
        String[] split=condition.split(":");
        if(split.length>0)
        {
          this.condition=new Pair<>(Integer.parseInt(split[0]),Integer.parseInt(split[1]));
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    this.npc=Main.world.getNPCTemplate(npc);
    try
    {
      if(!action.equalsIgnoreCase("")&&!args.equalsIgnoreCase(""))
      {
        String[] arguments=args.split(";");
        int nbr=0;
        for(String loc0 : action.split(","))
        {
          int actionId=Integer.parseInt(loc0);
          String arg=arguments[nbr];
          actions.add(new Action(actionId,arg,-1+"",null));
          nbr++;
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      Main.world.logger.error("Erreur avec l action et les args de la quete "+this.id+".");
    }
  }

  public int getId()
  {
    return id;
  }

  public boolean isDelete()
  {
    return this.delete;
  }

  public NpcTemplate getNpcTemplate()
  {
    return npc;
  }

  public ArrayList<QuestStep> getQuestSteps()
  {
    return questSteps;
  }

  private boolean haveRespectCondition(QuestPlayer questPlayer, QuestStep questStep)
  {
    switch(questStep.getCondition())
    {
      case "1": //Valider les etapes d'avant
        boolean loc2=true;
        for(QuestStep step : this.questSteps)
        {
          if(step!=null&&step.getId()!=questStep.getId()&&!questPlayer.isQuestStepIsValidate(step))
          {
            loc2=false;
          }
        }
        return loc2;

      case "0":
        return true;
    }
    return false;
  }

  public String getGmQuestDataPacket(Player player)
  {
    QuestPlayer questPlayer=player.getQuestPersoByQuest(this);
    int loc1=getObjectifCurrent(questPlayer);
    int loc2=getObjectifPrevious(questPlayer);
    int loc3=getNextObjectif(QuestObjectif.getQuestObjectifById(getObjectifCurrent(questPlayer)));
    StringBuilder str=new StringBuilder();
    str.append(id).append("|");
    str.append(loc1>0 ? loc1 : "");
    str.append("|");

    StringBuilder str_prev=new StringBuilder();
    boolean loc4=true;
    // Il y a une exeption dans le code ici pour la seconde étape de papotage
    for(QuestStep qEtape : questSteps)
    {
      if(qEtape.getObjectif()!=loc1)
        continue;
      if(!haveRespectCondition(questPlayer,qEtape))
        continue;
      if(!loc4)
        str_prev.append(";");
      str_prev.append(qEtape.getId());
      str_prev.append(",");
      str_prev.append(questPlayer.isQuestStepIsValidate(qEtape) ? 1 : 0);
      loc4=false;
    }
    str.append(str_prev);
    str.append("|");
    str.append(loc2>0 ? loc2 : "").append("|");
    str.append(loc3>0 ? loc3 : "");
    if(npc!=null)
    {
      str.append("|");
      str.append(npc.getInitQuestionId(player.getCurMap().getId())).append("|");
    }
    return str.toString();
  }

  public QuestStep getCurrentQuestStep(QuestPlayer questPlayer)
  {
    for(QuestStep step : getQuestSteps())
    {
      if(!questPlayer.isQuestStepIsValidate(step))
      {
        return step;
      }
    }
    return null;
  }

  private int getObjectifCurrent(QuestPlayer questPlayer)
  {
    for(QuestStep step : questSteps)
    {
      if(!questPlayer.isQuestStepIsValidate(step))
      {
        return step.getObjectif();
      }
    }
    return 0;
  }

  private int getObjectifPrevious(QuestPlayer questPlayer)
  {
    if(questObjectifList.size()==1)
      return 0;
    else
    {
      int previous=0;
      for(QuestObjectif qObjectif : questObjectifList)
      {
        if(qObjectif.getId()==getObjectifCurrent(questPlayer))
          return previous;
        else
          previous=qObjectif.getId();
      }
    }
    return 0;
  }

  private int getNextObjectif(QuestObjectif questObjectif)
  {
    if(questObjectif==null)
      return 0;
    for(QuestObjectif objectif : questObjectifList)
    {
      if(objectif.getId()==questObjectif.getId())
      {
        int index=questObjectifList.indexOf(objectif);
        if(questObjectifList.size()<=index+1)
          return 0;
        return questObjectifList.get(index+1).getId();
      }
    }
    return 0;
  }

  public void applyQuest(Player player)
  {
    if(this.condition!=null)
    {
      switch(this.condition.getLeft())
      {
        case 1: // Niveau
          if(player.getLevel()<this.condition.getRight())
          {
            SocketManager.GAME_SEND_MESSAGE(player,"Votre niveau n'est pas assez élevé pour recevoir cette quéte.");
            return;
          }
          break;
      }
    }

    QuestPlayer questPlayer=new QuestPlayer(Database.getDynamics().getQuestPlayerData().getNextId(),id,false,player.getId(),"");
    player.addQuestPerso(questPlayer);
    SocketManager.GAME_SEND_Im_PACKET(player,"054;"+this.id);
    Database.getDynamics().getQuestPlayerData().add(questPlayer);
    SocketManager.GAME_SEND_MAP_NPCS_GMS_PACKETS(player.getGameClient(),player.getCurMap());

    if(!this.actions.isEmpty())
    {
      for(Action aAction : this.actions)
      {
        aAction.apply(player,player,-1,-1);
      }
    }

    //Database.getStatics().getPlayerData().update(player);
  }

  public void updateQuestData(Player player, boolean validation, int type)
  {
    QuestPlayer questPlayer=player.getQuestPersoByQuest(this);
    for(QuestStep questStep : this.questSteps)
    {
      if(questStep.getValidationType()!=type||questPlayer.isQuestStepIsValidate(questStep)) //On a déjé validé la questEtape on passe
        continue;
      if(questStep.getObjectif()!=getObjectifCurrent(questPlayer)||!haveRespectCondition(questPlayer,questStep))
        continue;

      boolean refresh=false;

      if(validation)
        refresh=true;
      switch(questStep.getType())
      {
        case 3://Donner item
          if(player.getExchangeAction()!=null&&player.getExchangeAction().getType()==ExchangeAction.TALKING_WITH&&player.getCurMap().getNpc((Integer)player.getExchangeAction().getValue()).getTemplate().getId()==questStep.getNpc().getId())
          {
            for(Entry<Integer, Integer> entry : questStep.getItemNecessaryList().entrySet())
            {
              if(player.hasItemTemplate(entry.getKey(),entry.getValue()))
              { //Il a l'item et la quantité
                player.removeByTemplateID(entry.getKey(),entry.getValue()); //On supprime donc
                refresh=true;
              }
            }
          }
          break;

        case 0:
        case 1://Aller voir %
        case 9://Retourner voir %
          if(questStep.getCondition().equalsIgnoreCase("1"))
          { //Valider les questEtape avant
            if(player.getExchangeAction()!=null&&player.getExchangeAction().getType()==ExchangeAction.TALKING_WITH&&player.getCurMap().getNpc((Integer)player.getExchangeAction().getValue()).getTemplate().getId()==questStep.getNpc().getId())
            {
              if(haveRespectCondition(questPlayer,questStep))
              {
                refresh=true;
              }
            }
          }
          else
          {
            if(player.getExchangeAction()!=null&&player.getExchangeAction().getType()==ExchangeAction.TALKING_WITH&&player.getCurMap().getNpc((Integer)player.getExchangeAction().getValue()).getTemplate().getId()==questStep.getNpc().getId())
              refresh=true;
          }
          break;

        case 6: // monstres
          for(Entry<Integer, Short> entry : questPlayer.getMonsterKill().entrySet())
            if(entry.getKey()==questStep.getMonsterId()&&entry.getValue()>=questStep.getQua())
              refresh=true;
          break;

        case 10://Ramener prisonnier
          if(player.getExchangeAction()!=null&&player.getExchangeAction().getType()==ExchangeAction.TALKING_WITH&&player.getCurMap().getNpc((Integer)player.getExchangeAction().getValue()).getTemplate().getId()==questStep.getNpc().getId())
          {
            GameObject follower=player.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR);
            if(follower!=null)
            {
              Map<Integer, Integer> itemNecessaryList=questStep.getItemNecessaryList();
              for(Entry<Integer, Integer> entry2 : itemNecessaryList.entrySet())
              {
                if(entry2.getKey()==follower.getTemplate().getId())
                {
                  refresh=true;
                  player.setMascotte(0);
                }
              }
            }
          }
          break;
      }

      if(refresh)
      {
        QuestObjectif ansObjectif=QuestObjectif.getQuestObjectifById(getObjectifCurrent(questPlayer));
        questPlayer.setQuestStepValidate(questStep);
        SocketManager.GAME_SEND_Im_PACKET(player,"055;"+id);
        if(haveFinish(questPlayer,ansObjectif))
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"056;"+id);
          applyButinOfQuest(player,ansObjectif);
          questPlayer.setFinish(true);
        }
        else
        {
          if(getNextObjectif(ansObjectif)!=0)
          {
            if(questPlayer.overQuestStep(ansObjectif))
              applyButinOfQuest(player,ansObjectif);
          }
        }
        //Database.getStatics().getPlayerData().update(player);
      }
    }
  }

  private boolean haveFinish(QuestPlayer questPlayer, QuestObjectif questObjectif)
  {
    return questPlayer.overQuestStep(questObjectif)&&getNextObjectif(questObjectif)==0;
  }

  private void applyButinOfQuest(Player player, QuestObjectif questObjectif)
  {
    long xp;
    int kamas;

    if((xp=questObjectif.getXp())>0)
    { //Xp a donner
      player.addXp(xp*((int)Config.getInstance().rateXp));
      SocketManager.GAME_SEND_Im_PACKET(player,"08;"+(xp*((int)Config.getInstance().rateXp)));
      SocketManager.GAME_SEND_STATS_PACKET(player);
    }

    if(questObjectif.getObjects().size()>0)
    { //Item a donner
      for(Entry<Integer, Integer> entry : questObjectif.getObjects().entrySet())
      {
        ObjectTemplate template=Main.world.getObjTemplate(entry.getKey());
        int quantity=entry.getValue();
        GameObject object=template.createNewItem(quantity,false);

        if(player.addObjet(object,true))
        {
          World.addGameObject(object,true);
        }
        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+quantity+"~"+template.getId());
      }
    }

    if((kamas=questObjectif.getKamas())>0)
    { //Kams a donner
      long newKamas=player.getKamas()+(long)kamas;
      if(newKamas<0)
        return;
      player.setKamas(player.getKamas()+(long)kamas);
      Main.world.kamas_total += kamas;
      SocketManager.GAME_SEND_Im_PACKET(player,"045;"+kamas);
      SocketManager.GAME_SEND_STATS_PACKET(player);
    }

    if(getNextObjectif(questObjectif)!=questObjectif.getId())
    { //On passe au nouveau objectif on applique les actions
      for(Action action : questObjectif.getActions())
      {
        action.apply(player,null,0,0);
      }
    }
  }
}
