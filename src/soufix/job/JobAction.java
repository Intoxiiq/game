package soufix.job;

import soufix.area.map.GameCase;
import soufix.area.map.entity.InteractiveObject;
import soufix.client.Player;
import soufix.common.Formulas;
import soufix.common.PathFinding;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.monster.MobGroup;
import soufix.fight.spells.SpellEffect;
import soufix.game.GameClient;
import soufix.game.World;
import soufix.game.action.ExchangeAction;
import soufix.game.action.GameAction;
import soufix.job.fm.Potion;
import soufix.job.fm.Rune;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.object.ObjectTemplate;
import soufix.utility.Pair;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class JobAction
{
  public Map<Integer, Integer> ingredients=new TreeMap<>(), lastCraft=new TreeMap<>();
  public Player player;
  public String data="";
  public boolean broken=false;
  public boolean isRepeat=false;
  private int id;
  private int min=1;
  private int max=1;
  private boolean isCraft;
  private int chan=100;
  private int time=0;
  private int xpWin=0;
  private JobStat SM;
  private JobCraft jobCraft;
  public JobCraft oldJobCraft;
  private JobCraft oldJobCraft2;
  private int reConfigingRunes=-1;

  public JobAction(int sk, int min, int max, boolean craft, int arg, int xpWin)
  {
    this.id=sk;
    this.min=min;
    this.max=max;
    this.isCraft=craft;
    this.xpWin=xpWin;
    if(craft)
      this.chan=arg;
    else
      this.time=arg;
  }

  public int getId()
  {
    return this.id;
  }

  public int getMin()
  {
    return this.min;
  }

  public int getMax()
  {
    return this.max;
  }

  public boolean isCraft()
  {
    return this.isCraft;
  }

  public int getChance()
  {
    return this.chan;
  }

  public int getTime()
  {
    return this.time;
  }

  public int getXpWin()
  {
    return this.xpWin;
  }

  public JobStat getJobStat()
  {
    return this.SM;
  }

  public JobCraft getJobCraft()
  {
    return this.jobCraft;
  }

  public void setJobCraft(JobCraft jobCraft)
  {
    this.jobCraft=jobCraft;
  }

  public void startCraft(Player P)
  {
    /*if(P.getInInteractiveObject()!=null)
    {
      P.getInInteractiveObject().getLeft().setState(JobConstant.IOBJECT_STATE_EMPTYING);
      SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(),P.getInInteractiveObject().getRight());
    }*/
    this.craft(false, P);
  }
  public void startCraft_rapide(Player P)
  {
    /*if(P.getInInteractiveObject()!=null)
    {
      P.getInInteractiveObject().getLeft().setState(JobConstant.IOBJECT_STATE_EMPTYING);
      SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(),P.getInInteractiveObject().getRight());
    }*/
	  this.isRepeat = true;
    this.craft(true, P);
  }
  public void startCraft_repete(Player P)
  {
    this.jobCraft = new JobCraft(this, P);
  }

  public void startAction(Player P, InteractiveObject IO, GameAction GA, GameCase cell, JobStat SM) {
      this.SM = SM;
      this.player = P;

      if (P.getObjetByPos(Constant.ITEM_POS_ARME) != null && SM.getTemplate().getId() == 36) {
          if (Main.world.getMetier(36).isValidTool(P.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId())) {
              int dist = PathFinding.getDistanceBetween(P.getCurMap(), P.getCurCell().getId(), cell.getId());
              int distItem = JobConstant.getDistCanne(P.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId());
              if (distItem < dist) {
                  SocketManager.GAME_SEND_MESSAGE(P, "Vous étes trop loin pour pouvoir pécher ce poisson !");
                  SocketManager.GAME_SEND_GA_PACKET(P.getGameClient(), "", "0", "", "");
                  P.setExchangeAction(null);
                  P.setDoAction(false);
                  return;
              }
          }
      }
      if (!this.isCraft) {
          P.getGameClient().action = System.currentTimeMillis();
          IO.setInteractive(false);
          IO.setState(JobConstant.IOBJECT_STATE_EMPTYING);
          SocketManager.GAME_SEND_GA_PACKET_TO_MAP(P.getCurMap(), "" + GA.id, 501, P.getId() + "", cell.getId() + "," + this.time);
          SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
      } else {
          P.setAway(true);
          IO.setState(JobConstant.IOBJECT_STATE_EMPTYING);
          P.setExchangeAction(new ExchangeAction<>(ExchangeAction.CRAFTING, this));
          SocketManager.GAME_SEND_ECK_PACKET(P, 3, this.min + ";" + this.id);
          SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
      }
  }

  public void startAction(Player P, InteractiveObject IO, GameAction GA, GameCase cell)
  {
	  if(this.id == 121) {
		  
	  }
    this.player=P;
    P.setAway(true);
    IO.setState(JobConstant.IOBJECT_STATE_EMPTYING); //FIXME trouver la bonne valeur
    P.setExchangeAction(new ExchangeAction<>(ExchangeAction.CRAFTING,this));
    SocketManager.GAME_SEND_ECK_PACKET(P,3,this.min+";"+this.id); //this.min => Nbr de Case de l'interface
    SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(),cell);
  }

  public void endAction(Player player, InteractiveObject IO, GameAction GA, GameCase cell)
  {
	  if(player == null || player.getGameClient() == null)
		  return;
	 player.setDoAction(false);
    if(!this.isCraft&&player.getGameClient().action!=0)
    {
      if(System.currentTimeMillis()-player.getGameClient().action<this.time-200)
      {
    	 player.send("BMSpeed Hack ?");
    	 player.setAway(false);
        return;
      }
    }
    
    if(IO==null) {
    	player.setAway(false);
      return;
    }
    if(!this.isCraft)
    {
      IO.setState(JobConstant.IOBJECT_STATE_EMPTY);
      IO.desactive();
      SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(player.getCurMap(),cell);
      int qua=(this.max>this.min ? Formulas.getRandomValue(this.min,this.max) : this.min);
      World.get_Succes(player.getId()).recolte_add(player,qua);
      this.player.setTotal_reculte();
      if(SM.getTemplate().getId()==36)
      {
          SM.addXp(player,(long)(this.getXpWin()*50));
      }
      else {
    	  if(SM.getTemplate().getId()==2)
    	  SM.addXp(player,(long)(this.getXpWin()*20));
    	  else
        SM.addXp(player,(long)(this.getXpWin()*Config.getInstance().rateJob));
      }
      int tID=JobConstant.getObjectByJobSkill(this.id);
      int chance = SM.getTemplate().getId()==JobConstant.JOB_PECHEUR ? 10 : 40;
      if(Formulas.getRandomValue(1,50)==1)
      {
        int _tID=-1;
        int rareQua=1;
        if(SM.getTemplate().getId()==JobConstant.JOB_PECHEUR&&qua>0)
          _tID=JobConstant.getRareFish(tID);
        else if(SM.getTemplate().getId()==JobConstant.JOB_MINEUR&&qua>0)
          _tID=JobConstant.getRareStones();
        else if(SM.getTemplate().getId()==JobConstant.JOB_PAYSAN&&qua>0)
          _tID=JobConstant.getRareCereals(tID);

        if(_tID!=-1)
        {
          ObjectTemplate _T=Main.world.getObjTemplate(_tID);
          if(_T!=null)
          {
            GameObject _O=_T.createNewItem(rareQua,true);
            if(player.addObjet(_O,true))
              World.addGameObject(_O,true);
            if(SM.getTemplate().getId()==JobConstant.JOB_PECHEUR)
              SocketManager.GAME_SEND_MESSAGE(player,"Vous venez de pécher un poisson rare.","009900");
            else if(SM.getTemplate().getId()==JobConstant.JOB_MINEUR)
              SocketManager.GAME_SEND_MESSAGE(player,"Lors de l'exploitation miniére, vous remarquez quelque chose qui scintille dans la roche.","009900");
            else if(SM.getTemplate().getId()==JobConstant.JOB_PAYSAN)
              SocketManager.GAME_SEND_MESSAGE(player,"Vous venez de récolter une céréale rare.","009900");
          }
        }
      }

      ObjectTemplate T=Main.world.getObjTemplate(tID);
      if(T==null) {
    	  player.setAway(false);
        return;
      }
      GameObject O=T.createNewItem(qua,true);

      if(player.addObjet(O,true))
        World.addGameObject(O,true);
      SocketManager.GAME_SEND_IQ_PACKET(player,player.getId(),qua);
      SocketManager.GAME_SEND_Ow_PACKET(player);
      IO.setState(JobConstant.IOBJECT_STATE_EMPTY2);
      if(player.getMetierBySkill(this.id).get_lvl()>=30&&Formulas.getRandomValue(1,chance)==1)
      {
        for(int[] protector : JobConstant.JOB_PROTECTORS)
        {
          if(tID==protector[1])
          {
            int monsterLvl=JobConstant.getProtectorLvl(player.getLevel());
            player.getCurMap().startFightVersusProtectors(player,new MobGroup(player.getCurMap().nextObjectId,cell.getId(),protector[0]+","+monsterLvl+","+monsterLvl));
            break;
          }
        }
      }
    }
    player.setAway(false);
  }

  private int addCraftObject(Player player, GameObject newObj)
  {
    for(Entry<Integer, GameObject> entry : player.getItems().entrySet())
    {
      GameObject obj=entry.getValue();
      if(obj.getTemplate().getId()==newObj.getTemplate().getId()&&obj.getTxtStat().equals(newObj.getTxtStat())&&obj.getStats().isSameStats(newObj.getStats())&&obj.getPosition()==Constant.ITEM_POS_NO_EQUIPED)
      {
        obj.setQuantity(obj.getQuantity()+newObj.getQuantity(),player); //On ajoute QUA item a la quantité de l'objet existant
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(player,obj);
        return obj.getGuid();
      }
    }

    player.getItems().put(newObj.getGuid(),newObj);
    SocketManager.GAME_SEND_OAKO_PACKET(player,newObj);
    World.addGameObject(newObj,true);
    return -1;
  }

  //v2.9 - 100% crafting success
  public void craft(boolean repeat ,Player perso)
  {
	  if(Config.singleton.serverId == 6)
		  return;
	  this.player = perso;
    if(!this.isCraft)
      return;
    if(this.player == null)
    	return;
    
    if(this.id != 110)
    if(this.id != 121)
    if(this.SM == null)
    return;	
    
    if(this.id==1||this.id==113||this.id==115||this.id==116||this.id==117||this.id==118||this.id==119||this.id==120||(this.id>=163&&this.id<=169))
    {
      doMage(repeat,null,null);
      return;
    }
    if((this.id>=140&&this.id<=150))
    {
    Reparation();
      return;
    }
    Map<Integer, Integer> items=new HashMap<>();
    for(final Map.Entry<Integer, Integer> e : this.ingredients.entrySet())
    {
      if(!this.player.hasItemGuid(e.getKey()))
      {
        SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
        return;
      }

      GameObject obj=World.getGameObject(e.getKey());

      if(obj==null)
      {
        SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
        return;
      }
      if(obj.getQuantity()<e.getValue())
      {
        SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
        return;
      }

      items.put(obj.getTemplate().getId(),e.getValue());
    }
    boolean signed=false;
    if(items.containsKey(7508))
    {
      signed=true;
      items.remove(7508);
    }
    if(!isRepeat)
    SocketManager.GAME_SEND_Ow_PACKET(this.player);

    boolean isUnjobSkill=this.getJobStat()==null;

    if(!isUnjobSkill)
    {
      final JobStat SM=this.player.getMetierBySkill(this.id);
      final int tID=Main.world.getObjectByIngredientForJob(SM.getTemplate().getListBySkill(this.id),items);
      if(tID==-1||!SM.getTemplate().canCraft(this.id,tID) || Main.world.getObjTemplate(tID) == null)
      {
    	  if(!isRepeat) {
        SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
        SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-");
    	  }
        this.ingredients.clear();
        return;
      }

      boolean success=true;
      World.get_Succes(player.getId()).craft_add(player);
      // System anti lost echec
      Map<Integer, Integer> ingredients2=new ConcurrentHashMap<>();
      ingredients2 = ingredients;
      for(final Map.Entry<Integer, Integer> e : ingredients2.entrySet())
      {
        GameObject obj=World.getGameObject(e.getKey());

        final int newQua=obj.getQuantity()-e.getValue();
        if(newQua<0)
          return;
        if(newQua==0)
        {
          this.player.removeItem(e.getKey());
          Main.world.removeGameObject(e.getKey());
          if(!isRepeat)
          SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player,e.getKey());
        }
        else
        {
          final int change=obj.getQuantity()-newQua;
          obj.setQuantity(newQua,player);
          //v2.8 - ingredient quantity updater
          if(!isRepeat) {
          SocketManager.GAME_SEND_Em_PACKET(this.player,"KO+"+e.getKey()+"|-"+change+"|"+obj.getTemplate().getId()+"|"+obj.parseStatsString().replace(";","#"));
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,obj);
          }
        }
      }
     // System anti lost echec
      if(Main.world.getObjTemplate(tID).getLevel() > 80 )
     Database.getStatics().getPlayerData().logs_craft(this.player.getName(), this.player.getName()+" a crafter avec "+(success ? "SUCCES" : "ECHEC")+" l'item "+tID+" ("+Main.world.getObjTemplate(tID).getName()+")");
      
      if(!success)
      {
    	  if(!isRepeat) {
        SocketManager.GAME_SEND_Ec_PACKET(this.player,"EF");
        SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-"+tID);
        SocketManager.GAME_SEND_Im_PACKET(this.player,"0118");
    	  }
      }
      else
      {
        GameObject newObj=Main.world.getObjTemplate(tID).createNewItemWithoutDuplication(this.player.getItems().values(),1,false);

        int guid=newObj.getGuid();
        if(guid==-1)
        { // Don't exist
          guid=newObj.setId();
          this.player.getItems().put(guid,newObj);
          if(!isRepeat)
          SocketManager.GAME_SEND_OAKO_PACKET(this.player,newObj);
          World.addGameObject(newObj,true);
        }
        else
        {
          newObj.setQuantity(newObj.getQuantity()+1,player);
          if(!isRepeat)
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,newObj);
        }
        if(!isRepeat)
        SocketManager.GAME_SEND_Ow_PACKET(this.player);
        if(signed)
          newObj.addTxtStat(988,this.player.getName());
        if(!isRepeat) {
        SocketManager.GAME_SEND_Em_PACKET(this.player,"KO+"+guid+"|1|"+tID+"|"+newObj.parseStatsString().replace(";","#"));
        SocketManager.GAME_SEND_Ec_PACKET(this.player,"K;"+tID);
        SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"+"+tID);
        }
      }
      int winXP=0;
      winXP=Formulas.calculXpWinCraft(SM.get_lvl(),this.ingredients.size())*Config.getInstance().rateJob;
     
      if(winXP>0)
      {
        SM.addXp(this.player,winXP);
        final ArrayList<JobStat> SMs=new ArrayList<JobStat>();
        SMs.add(SM);
        if(!isRepeat)
        SocketManager.GAME_SEND_JX_PACKET(this.player,SMs);
      }
    }
    else
    {
      final int templateId=Main.world.getObjectByIngredientForJob(Main.world.getMetier(this.id).getListBySkill(this.id),items);
      if(templateId==-1||!Main.world.getMetier(this.id).canCraft(this.id,templateId) || Main.world.getObjTemplate(templateId) == null)
      {
    	  if(!isRepeat) {
        SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
        SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-");
    	  }
        this.ingredients.clear();
        return;
      }
      // System anti lost echec
      for(final Map.Entry<Integer, Integer> e : this.ingredients.entrySet())
      {
        GameObject obj=World.getGameObject(e.getKey());

        final int newQua=obj.getQuantity()-e.getValue();
        if(newQua<0)
          return;
        if(newQua==0)
        {
          this.player.removeItem(e.getKey());
          Main.world.removeGameObject(e.getKey());
          if(!isRepeat)
          SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player,e.getKey());
        }
        else
        {
          final int change=obj.getQuantity()-newQua;
          obj.setQuantity(newQua,player);
          //v2.8 - ingredient quantity updater
          if(!isRepeat) {
          SocketManager.GAME_SEND_Em_PACKET(this.player,"KO+"+e.getKey()+"|-"+change+"|"+obj.getTemplate().getId()+"|"+obj.parseStatsString().replace(";","#"));
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,obj);
          }
        }
      }
     // System anti lost echec
      GameObject newObj=Main.world.getObjTemplate(templateId).createNewItemWithoutDuplication(this.player.getItems().values(),1,false);

      int guid=newObj.getGuid();

      if(guid==-1)
      { // Don't exist
        guid=newObj.setId();
        this.player.getItems().put(guid,newObj);
        if(!isRepeat)
        SocketManager.GAME_SEND_OAKO_PACKET(this.player,newObj);
        World.addGameObject(newObj,true);
      }
      else
      {
        newObj.setQuantity(newObj.getQuantity()+1,player);
        if(!isRepeat)
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player,newObj);
      }

      if(signed)
        newObj.addTxtStat(988,this.player.getName());
      {
    	  if(!isRepeat) {
      SocketManager.GAME_SEND_Ow_PACKET(this.player);
      SocketManager.GAME_SEND_Em_PACKET(this.player,"KO+"+guid+"|1|"+templateId+"|"+newObj.parseStatsString().replace(";","#"));
      SocketManager.GAME_SEND_Ec_PACKET(this.player,"K;"+templateId);
      SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"+"+templateId);
      }
      }
    }
    this.lastCraft.clear();
    this.lastCraft.putAll(this.ingredients);
    //this.ingredients.clear();

    if(!repeat)
    {
      if(this.player.getInInteractiveObject()!=null)
      {
        this.player.getInInteractiveObject().getLeft().setState(JobConstant.IOBJECT_STATE_FULL);
       // SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(this.player.getCurMap(),this.player.getInInteractiveObject().getRight());
      }
      this.oldJobCraft=this.jobCraft;
      this.jobCraft=null;
    }
  }
  

  //v2.8 - no more max stats items
  public boolean craftPublicMode(Player crafter, Player receiver, Map<Player, ArrayList<Pair<Integer, Integer>>> list)
  {
	  if(Config.singleton.serverId == 6)
		  return false;
    if(!this.isCraft)
      return false;

    this.player=crafter;
    JobStat SM=this.player.getMetierBySkill(this.id);
    boolean signed=false;

    if(this.id==1||this.id==113||this.id==115||this.id==116||this.id==117||this.id==118||this.id==119||this.id==120||(this.id>=163&&this.id<=169))
    {
      this.SM=SM;
      doMage(isRepeat,receiver,list);
      return true;
    }

    Map<Integer, Integer> items=new HashMap<>();

    for(Entry<Player, ArrayList<Pair<Integer, Integer>>> entry : list.entrySet())
    {
      Player player=entry.getKey();
      Map<Integer, Integer> playerItems=new HashMap<>();

      for(Pair<Integer, Integer> couple : entry.getValue())
        playerItems.put(couple.getLeft(),couple.getRight());

      for(Entry<Integer, Integer> e : playerItems.entrySet())
      {
        if(!player.hasItemGuid(e.getKey()))
        {
          SocketManager.GAME_SEND_Ec_PACKET(player,"EI");
          SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
          return false;
        }

        GameObject gameObject=World.getGameObject(e.getKey());
        if(gameObject==null)
        {
          SocketManager.GAME_SEND_Ec_PACKET(player,"EI");
          SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
          return false;
        }
        if(gameObject.getQuantity()<e.getValue())
        {
          SocketManager.GAME_SEND_Ec_PACKET(player,"EI");
          SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
          return false;
        }

        int newQua=gameObject.getQuantity()-e.getValue();

        if(newQua<0)
          return false;

        if(newQua==0)
        {
          player.removeItem(e.getKey());
          Main.world.removeGameObject(e.getKey());
          SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player,e.getKey());
        }
        else
        {
          gameObject.setQuantity(newQua,player);
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(player,gameObject);
        }

        items.put(gameObject.getTemplate().getId(),e.getValue());
      }
    }

    SocketManager.GAME_SEND_Ow_PACKET(this.player);

    //Rune de signature
    if(items.containsKey(7508))
      if(SM.get_lvl()==100)
        signed=true;
    items.remove(7508);

    int template=Main.world.getObjectByIngredientForJob(SM.getTemplate().getListBySkill(this.id),items);

    if(template==-1||!SM.getTemplate().canCraft(this.id,template))
    {
      SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
      receiver.send("EcEI");
      SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-");
      items.clear();
      return false;
    }

    boolean success=true;
    if(Main.world.getObjTemplate(template).getLevel() > 80 )
    Database.getStatics().getPlayerData().logs_craft(this.player.getName(), this.player.getName()+" a crafter avec "+(success ? "SUCCES" : "ECHEC")+" l'item "+template+" ("+Main.world.getObjTemplate(template).getName()+") pour "+receiver.getName());
    if(!success)
    {
      SocketManager.GAME_SEND_Ec_PACKET(this.player,"EF");
      SocketManager.GAME_SEND_Ec_PACKET(receiver,"EF");
      SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-"+template);
      SocketManager.GAME_SEND_Im_PACKET(this.player,"0118");
    }
    else
    {
      GameObject newObj=Main.world.getObjTemplate(template).createNewItem(1,false);
      if(signed)
        newObj.addTxtStat(988,this.player.getName());
      int guid=this.addCraftObject(receiver,newObj);
      if(guid==-1)
        guid=newObj.getGuid();
     // String stats=newObj.parseStatsString();

      this.player.send("EcK;"+template);
      receiver.send("EcK;"+template);

      SocketManager.GAME_SEND_Ow_PACKET(this.player);
      SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"+"+template);
    }

    int winXP=Formulas.calculXpWinCraft(SM.get_lvl(),this.ingredients.size())*Config.getInstance().rateJob;
    SM.addXp(this.player,winXP);
    if(SM.getTemplate().getId()==28&&winXP==1)
      winXP=10;
    if(success)
    {
      ArrayList<JobStat> SMs=new ArrayList<>();
      SMs.add(SM);
      SocketManager.GAME_SEND_JX_PACKET(this.player,SMs);
    }

    this.ingredients.clear();
    return success;
  }

  public void addIngredient(Player player, int id, int quantity)
  {
	  try {
		  
    int oldQuantity=this.ingredients.get(id)==null ? 0 : this.ingredients.get(id);
    if(World.getGameObject(id) == null)
    	return;
    if((quantity+oldQuantity) > World.getGameObject(id).getQuantity())
    	return;
    if(quantity<0)
      if(-quantity>oldQuantity)
        return;
    if (World.getGameObject(id).getObvijevanLook() != 0) {
		SocketManager.GAME_SEND_MESSAGE(player,"Action impossible sur objivivant.");	
	return;	
	}
    if (World.getGameObject(id).getTemplate().getType() == 23) {
		SocketManager.GAME_SEND_MESSAGE(player,"Action impossible.");	
	return;	
	}
    if (World.getGameObject(id).containtTxtStats(975)) {
    	SocketManager.GAME_SEND_MESSAGE(player,"Action impossible sur Mimibiote.");	
    	return;	
	}
    this.ingredients.remove(id);
    oldQuantity+=quantity;

    if(oldQuantity>0)
    {
      this.ingredients.put(id,oldQuantity);
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(player,'O',"+",id+"|"+oldQuantity);
    }
    else
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(player,'O',"-",id+"");
	  }  catch(Exception e)
      {
		  e.printStackTrace();  
      }
  }

  public byte sizeList(Map<Player, ArrayList<Pair<Integer, Integer>>> list)
  {
    byte size=0;

    for(ArrayList<Pair<Integer, Integer>> entry : list.values())
    {
      for(Pair<Integer, Integer> couple : entry)
      {
        GameObject object=World.getGameObject(couple.getLeft());
        if(object!=null)
        {
          ObjectTemplate objectTemplate=object.getTemplate();
          if(objectTemplate!=null&&objectTemplate.getId()!=7508)
            size++;
        }
      }
    }
    return size;
  }

  public void putLastCraftIngredients()
  {
    if(this.player==null||this.lastCraft==null||!this.ingredients.isEmpty())
      return;

    this.ingredients.clear();
    this.ingredients.putAll(this.lastCraft);
    if(!this.isRepeat)
    this.ingredients.entrySet().stream().filter(e -> World.getGameObject(e.getKey())!=null).filter(e -> !(World.getGameObject(e.getKey()).getQuantity()<e.getValue())).forEach(e -> SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player,'O',"+",e.getKey()+"|"+e.getValue()));
  }

  public void resetCraft()
  {
    this.ingredients.clear();
    this.lastCraft.clear();
    this.oldJobCraft=null;
    this.jobCraft=null;
  }

  //v2.0 - Sinks, exomaging redone
public boolean doMage(boolean repeat, Player receiver, Map<Player, ArrayList<Pair<Integer, Integer>>> items)
  {
	try
    {
	if(Config.singleton.serverId == 6)
		  return false;
    boolean isSigningRune=false;
    GameObject objectFm=null,signingRune=null,runeObject=null,potionObject=null;
    Rune rune=null;
    Potion potion=null;
    int deleteID=-1;
    boolean negative=false;
    boolean canFM=true;
    final boolean secure=items!=null&&receiver!=null;
    boolean fm_exo = false;
    final Map<Integer, Integer> ingredients=items==null ? this.ingredients : new HashMap<>();

    if(items!=null)
      for(Entry<Player, ArrayList<Pair<Integer, Integer>>> entry : items.entrySet()) {
        for(Pair<Integer, Integer> couple : entry.getValue()) {
          ingredients.put(couple.getLeft(),couple.getRight());
        }
        }
    for(int idIngredient : ingredients.keySet())
    {
      final GameObject item=World.getGameObject(idIngredient);
      if(item==null)
        if(!this.player.hasItemGuid(idIngredient)||(secure&&!this.player.hasItemGuid(idIngredient)&&!receiver.hasItemGuid(idIngredient)))
        {
          SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
         // SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-");
          //clearMage(objectFm,runeObject,this.player);
          ingredients.clear();
          //GameClient.leaveExchange(this.player);
         this.player.sendMessage("Erreur Fm 1 merci de contacter un administrateur");
          return false;
        }
      if(item == null || item.getTemplate() == null)
          {
            SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
           // SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-");
           // clearMage(objectFm,runeObject,this.player);
            ingredients.clear();
         	//GameClient.leaveExchange(this.player);
            this.player.sendMessage("Erreur Fm 2 merci de contacter un administrateur");
            return false;
          }
     
      int templateId=item.getTemplate().getId();
      if(Rune.getRuneById(templateId)!=null)
      {
        runeObject=item;
        rune=Rune.getRuneById(templateId);
      }
      else if(Potion.getPotionById(templateId)!=null)
      {
        potionObject=item;
        potion=Potion.getPotionById(templateId);
      }
      else if(templateId==7508)
      {
        isSigningRune=true;
        signingRune=item;
      }
      else
      {
        int type=item.getTemplate().getType();
        if((type>=1&&type<=11)||(type>=16&&type<=22)||type==81||type==102||type==114||item.getTemplate().getPACost()>0)
        {
          final Player player=this.player.hasItemGuid(item.getGuid()) ? this.player : receiver;
          objectFm=item;
          if(player.getGameClient() != null)
          SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK_FM(player.getGameClient(),'O',"+",String.valueOf(objectFm.getGuid())+"|"+1);
          deleteID=idIngredient;          
        }
      }
    }
    //Invalid Input
    if(SM==null||objectFm==null||(rune==null&&potion==null))
    {
      if(objectFm!=null)
      {
        World.addGameObject(objectFm,true);
        this.player.addObjet(objectFm);
      }
      if(receiver!=null)
        SocketManager.GAME_SEND_Ec_PACKET(receiver,"EI");
      SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
      SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-");
      //clearMage(objectFm,runeObject,this.player);
      ingredients.clear();
      //GameClient.leaveExchange(this.player);
      this.player.sendMessage("Erreur Fm 3 merci de contacter un administrateur");
      return false;
    }
    
    if(deleteID!=-1) {
        GameObject newObj=GameObject.getCloneObjet(objectFm,1);

        if(objectFm.getQuantity()>1)
        {
          int newQuant=objectFm.getQuantity()-1;
          objectFm.setQuantity(newQuant,player);
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(player,objectFm);
        }
        else
        {
          Main.world.removeGameObject(deleteID);
          player.removeItem(deleteID);
          SocketManager.GAME_SEND_DELETE_STATS_ITEM_FM(player,deleteID);
        }
        objectFm=newObj;
      ingredients.remove(deleteID);
    }

    int itemType=objectFm.getTemplate().getType();

    //Hunting rune check
    if(rune!=null)
      if(rune.getTemplateId()==10057)
        if(itemType==1||itemType==9||itemType==10||itemType==11||itemType==16||itemType==17||itemType==81)
        {
          SocketManager.GAME_SEND_MESSAGE(this.player,"Vous ne pouvez utiliser cette rune que sur des armes.");
          if(receiver!=null)
          {
        	  canFM=false;
          }
          else
          {
        	  canFM=false;
          }
        }

    final ObjectTemplate objTemplate=objectFm.getTemplate();
    ArrayList<Integer> chances=new ArrayList<Integer>();
    String statStringObj=objectFm.parseStatsString();
    int chance=0,lvlJob=SM.get_lvl();
    float oldSink=objectFm.getPuit(),currentItemPower=1f;
    Pair<Integer, Float> lostPower=null;
    int objTemplateID=objTemplate.getId();
    //v2.8 - only allow maging of items <= twice your job level
    if(lvlJob<(int)Math.floor(objTemplate.getLevel()/2))
    {
      SocketManager.GAME_SEND_MESSAGE(this.player,"Vous ne pouvez fm que deux fois le niveau de votre travail.");
      if(receiver!=null)
      {
    	clearMage(objectFm,runeObject,receiver);
		GameClient.leaveExchange(receiver);
		GameClient.leaveExchange(this.player);
      }
      else
      {
    	clearMage(objectFm,runeObject,this.player);
    	GameClient.leaveExchange(this.player);
      }
      return false;
    }

    String exoStatStr=objectFm.findAllExo(objectFm);
    float exoPower=0;
    if(exoStatStr!="") //item has exo
    {
      String[] exoSplit=exoStatStr.split(";"); //calculate total exo power
      for(int i=0;i<exoSplit.length;i++)
      {
        String[] exoSplit2=exoSplit[i].split(",");
        if(Rune.getRuneByStatId(Integer.toHexString(Integer.parseInt(exoSplit2[0])))!=null)
        {
          Rune tempRune=Rune.getRuneByStatId(Integer.toHexString(Integer.parseInt(exoSplit2[0])));
          float statPwr=tempRune.getPower()/tempRune.getStatsAdd();
          float entryPower=statPwr*(Integer.valueOf(exoSplit2[1])-getStatBaseMaxs(objectFm.getTemplate(),Integer.toString(Integer.valueOf(exoSplit2[0]),16)));
          exoPower=exoPower+entryPower;
        }
      }
    }

   // System.out.println("exoPower: "+exoPower);

    //v2.8 - total power limit
    if(rune!=null)
    {
      if(Job.getActualJet(objectFm,rune.getStatId())+rune.getStatsAdd()>getStatBaseMaxs(objectFm.getTemplate(),rune.getStatId())) //current mage exceeds base stats
        if(rune.getPower()+exoPower>101f)
        {
         // SocketManager.GAME_SEND_MESSAGE(this.player,"Ce mage dépasserait la limite de puissance maximale autorisée.");
          if(receiver!=null)
          {
        	  canFM=false;
          }
          else
          {
        	  canFM=false;
          }
        }
    }

    //ALL CHECKS ARE DONE FROM THIS POINT

    //calculate potion chance
    if(potion!=null)
    {
      chance=Formulas.calculChanceByElement(lvlJob,objTemplate.getLevel(),potion.getLevel());
      if(chance>100-lvlJob/20)
        chance=100-lvlJob/20;
      if(chance<lvlJob/20)
        chance=lvlJob/20;
      chances.add(0,chance);
      chances.add(1,0);
      chances.add(2,100-chance);
    }
    //calculate rune chance
    else
    {
  
       int statMax=getStatBaseMaxs(objectFm.getTemplate(),rune.getStatId());
       int currentStat=Job.getActualJet(objectFm,rune.getStatId());
       if(statMax < 0)
    	   statMax = 0;
      int maxOvermage=getMaxStat(rune.getPower(),rune.getStatsAdd());
      int maxOvermage2=(int) (statMax+(statMax*0.30));
      if(statMax>maxOvermage) //basestat higher than theoretical max
        maxOvermage=statMax;
       if(maxOvermage2 < maxOvermage)
    	   maxOvermage=maxOvermage2;
      //v2.8 - smarter max stat calculation
       if(statMax == -1)
    	   statMax = 1;
       if(maxOvermage == -1)
    	   maxOvermage = 1;
       if(maxOvermage2 == -1)
    	   maxOvermage2 = 0;
       if(potion == null)
       if(statMax == 0) {
    	  if (currentStat+rune.getStatsAdd() >= 2)
    	  {
    		  //SocketManager.GAME_SEND_MESSAGE(this.player,"Ce mage dépasserait la limite de statistiques maximale autorisée");
    		  canFM = false; 	  
    	  }
       }else
      if(currentStat+rune.getStatsAdd()>maxOvermage)
      {
        SocketManager.GAME_SEND_MESSAGE(this.player,"Ce fm dépasserait la limite de statistiques maximale autorisée "+maxOvermage+".");
      
        	canFM = false;
       
      }

      if(canFM)
      {
        float currentStatPower=1f;
        final int statMin=getStatBaseMins(objectFm.getTemplate(),rune.getStatId());
        float maxItemPower=maxTotalPower(objTemplateID);
        float minitemPower=minTotalPower(objTemplateID);

        final int currentStats2=Job.viewActualStatsItem(objectFm,rune.getStatId());
        if(currentStats2==2) //change id if negative stat
          if(!Rune.getNegativeStatByRuneStat(rune.getStatId()).equalsIgnoreCase(rune.getStatId())) //negative rune for rune found
            negative=true;

        float coef=1f;
        if(negative) //Maging negative stat
        {
          if(currentStat+rune.getnPower()>getStatBaseMaxs(objectFm.getTemplate(),Rune.getNegativeStatByRuneStat(rune.getStatId()))) //Overmaging negative stat
            coef=0.6f;
        }
        else if(statMax==0&&getStatBaseMins(objectFm.getTemplate(),rune.getStatId())==0) //Exomaging, researched
          coef=0.5f;
        else if(currentStat+rune.getPower()>statMax) //Overmaging positive stat
          coef=0.6f;

        if(!objectFm.parseStatsString().isEmpty())
        {
          currentItemPower=currentTotalPower(statStringObj,objectFm);
          currentStatPower=currentStatPower(objectFm,rune);
        }
        if(maxItemPower==0)
          maxItemPower=0.01f;
        if(minitemPower==0)
          minitemPower=0.01f;
        if(minitemPower<0&&currentStatPower<0) //negative stats
          chances=Formulas.chanceFM(maxItemPower,minitemPower,currentItemPower,currentStatPower,rune.getnPower(),statMax,statMin,rune.getStatsAdd(),coef,negative,rune);
        else //standard
          chances=Formulas.chanceFM(maxItemPower,minitemPower,currentItemPower,currentStatPower,rune.getPower(),statMax,statMin,rune.getStatsAdd(),coef,negative,rune);
      }
      else
      {
        chances.add(0,0);
        chances.add(1,0);
      }
    }
    
    // exo 
    if(Config.singleton.serverId == 7 || Config.singleton.serverId == 8) {
        if(potion == null)
            if(rune.getStatId().compareTo("80") == 0 || rune.getStatId().compareTo("6f") == 0) {
            	if(Job.getActualJet(objectFm,rune.getStatId()) == 0) {
            		
            // exo un item avec pa ou pm deja	
          	if(Job.getActualJet(objectFm,rune.getStatId()) == 0 && getStatBaseMaxs(objectFm.getTemplate(),rune.getStatId()) == 1)  {
          		int taux_exo = Formulas.getRandomValue(1, 2);
          	if(taux_exo == 2)
          		fm_exo = true;
          	SocketManager.GAME_SEND_MESSAGE(this.player,"Exo taux "+taux_exo+"/2");	
          	}
          	 // exo un item avec pa ou pm deja	
          	
          	 // exo un item sans pa ou pm
          	if(!fm_exo) {
          	if(Job.getActualJet(objectFm,"80") == 1 || Job.getActualJet(objectFm,"6f") == 1) {
          		chances.add(0,0);
                chances.add(1,0);	
          	}
          	else
          	{	
          	int max = getStatBaseMaxs(objectFm.getTemplate(),rune.getStatId());
          	if(max < 0)
          		max = 0;
          	if(max == 0 && Job.getActualJet(objectFm,rune.getStatId()) == 0)  {
          		int taux_exo2 = Formulas.getRandomValue(1, player.getAccount().getSubscribeRemaining() == 0L ? 25 : 10);
          		int taux_haut = player.getAccount().getSubscribeRemaining() == 0L ? 25 : 10;
          		if(taux_exo2 == taux_haut)
          	  		fm_exo = true;	
          	  	SocketManager.GAME_SEND_MESSAGE(this.player,"Exo taux "+taux_exo2+"/"+taux_haut+"");	
          	}
          	}
          	}
         // exo un item sans pa ou pm
            	}else {
            		chances.add(0,0);
                    chances.add(1,0);	
            	}
            }
    }else {
        if(potion == null)
            if(rune.getStatId().compareTo("80") == 0 || rune.getStatId().compareTo("6f") == 0) {
            	if(Job.getActualJet(objectFm,rune.getStatId()) == 0) {
            		
            // exo un item avec pa ou pm deja	
          	if(Job.getActualJet(objectFm,rune.getStatId()) == 0 && getStatBaseMaxs(objectFm.getTemplate(),rune.getStatId()) == 1)  {
          		int taux_exo = Formulas.getRandomValue(1, 2);
          	if(taux_exo == 2)
          		fm_exo = true;
          	SocketManager.GAME_SEND_MESSAGE(this.player,"Exo taux "+taux_exo+"/2");	
          	}
          	 // exo un item avec pa ou pm deja	
          	
          	 // exo un item sans pa ou pm
          	//if(!fm_exo) {
          	//if(Job.getActualJet(objectFm,"80") == 1 || Job.getActualJet(objectFm,"6f") == 1) {
          	//	chances.add(0,0);
             //   chances.add(1,0);	
          	//}
          	//else
          	//{	
          	int max = getStatBaseMaxs(objectFm.getTemplate(),rune.getStatId());
          	if(max < 0)
          		max = 0;
          	if(max == 0 && Job.getActualJet(objectFm,rune.getStatId()) == 0)  {
          		int taux_exo2 = Formulas.getRandomValue(1, player.getAccount().getSubscribeRemaining() == 0L ? 10 : 4);
          		int taux_haut = player.getAccount().getSubscribeRemaining() == 0L ? 10 : 4;
          		if(taux_exo2 == taux_haut)
          	  		fm_exo = true;	
          	  	SocketManager.GAME_SEND_MESSAGE(this.player,"Exo taux "+taux_exo2+"/"+taux_haut+"");	
          	}
          	//}
          	//}
         // exo un item sans pa ou pm
            	}else {
            		chances.add(0,0);
                    chances.add(1,0);	
            	}
            }	
    }
    // exo 
    

    // Ceinture du Dodu Trembleur d zeb
    if(potion == null && objectFm.getTemplate().getId() == 1668)
    if(rune.getStatId().compareTo("70") == 0 && Job.getActualJet(objectFm,rune.getStatId()) < 8)
    	fm_exo = true;
    //////////////////////////////////
    final int aleatoryChance=Formulas.getRandomValue(1,100);
    int SC=chances.get(0);
    int SN=chances.get(1);
    if(statStringObj.isEmpty())
    {
      SC+=(float)(SN/2);
      SN=0;
    }
    if(rune!=null)
      if(objectFm.onlyStat(rune.getStatId()))
      {
        SC+=(float)(SN/6);
        SN=0;
      }

    boolean successC=aleatoryChance<=SC;
    final boolean successN=aleatoryChance<=SC+SN;
     if(fm_exo)
	  successC = true;
	  
   // if(successC||successN)
   // {
      int winXP=0;
      if(potion!=null)
        winXP=Formulas.calculXpWinFm(objectFm.getTemplate().getLevel(),potion.getPower())*Config.getInstance().rateJob;
      else
        winXP=Formulas.calculXpWinFm(objectFm.getTemplate().getLevel(),rune.getPower())*Config.getInstance().rateJob;
      if(winXP>0)
      {
        SM.addXp(this.player,winXP);
        final ArrayList<JobStat> SMs=new ArrayList<JobStat>();
        SMs.add(SM);
        SocketManager.GAME_SEND_JX_PACKET(this.player,SMs);
      }
   // }

    if(successC)
    {
      if(isSigningRune)
        objectFm.addTxtStat(985,this.player.getName());
      if(potion!=null)
        doCritPotionMage(potion,objectFm);
      else
        doCritRuneMage(rune,objectFm,statStringObj);

      String data=objectFm.getGuid()+"|1|"+objectFm.getTemplate().getId()+"|"+objectFm.parseStatsString();

      if(!repeat)
        this.reConfigingRunes=-1;
      if(this.reConfigingRunes!=0||this.broken)
        if(receiver==null)
          SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player,'O',"+",data);

      this.data=data;
      SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"+"+objTemplateID);
      if(!secure)
        SocketManager.GAME_SEND_MESSAGE(this.player,"Vous avez fm avec succés.","009900");
      SocketManager.GAME_SEND_Ec_PACKET(this.player,"EF");
      World.get_Succes(this.player.getId()).fm_add(this.player);
    }
    else if(successN) //nSuccess item stat maging
    {
      if(isSigningRune)
        objectFm.addTxtStat(985,this.player.getName());
      lostPower=doNormalRuneMage(rune,objectFm,statStringObj,oldSink);

      String data=objectFm.getGuid()+"|1|"+objectFm.getTemplate().getId()+"|"+objectFm.parseStatsString();
      if(!repeat)
        this.reConfigingRunes=-1;
      if(this.reConfigingRunes!=0||this.broken)
        if(receiver==null)
          SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player,'O',"+",data);

      this.data=data;
      SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"+"+objTemplateID);

      if(lostPower.getRight()>0)
        SocketManager.GAME_SEND_Ec_PACKET(this.player,"EF");
      else
        SocketManager.GAME_SEND_Ec_PACKET(this.player,"EF;"+objTemplateID);
      World.get_Succes(this.player.getId()).fm_add(this.player);
    }
    else //Fail item stat maging
    {
      if(potion!=null)
        lostPower=doFailPotionMage(potion,objectFm,statStringObj,oldSink);
      else
        lostPower=doFailRuneMage(rune,objectFm,statStringObj,oldSink);

      String data=objectFm.getGuid()+"|1|"+objectFm.getTemplate().getId()+"|"+objectFm.parseStatsString();
      if(!repeat)
        this.reConfigingRunes=-1;
      if(this.reConfigingRunes!=0||this.broken)
        if(receiver==null)
          SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.player,'O',"+",data);

      this.data=data;
      SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-"+objTemplateID);
      SocketManager.GAME_SEND_Ec_PACKET(this.player,"EF");
    }

    int newQuantity=0;
    if(potion!=null)
      newQuantity=ingredients.get(potionObject.getGuid())==null ? 0 : ingredients.get(potionObject.getGuid())-1;
    else
      newQuantity=ingredients.get(runeObject.getGuid())==null ? 0 : ingredients.get(runeObject.getGuid())-1;

    if(objectFm!=null)
    {
      World.addGameObject(objectFm,true);
      if(receiver==null)
        this.player.addObjet(objectFm);
      else
        receiver.addObjet(objectFm);
    }

    if(!successC)
    {
      if(lostPower.getLeft()!=-1)
      {
        float newSink=0;
        if(lostPower.getLeft()==0)
        {
          if(rune!=null)
          {
            if(negative)
              newSink=objectFm.getPuit()+lostPower.getRight()-rune.getnPower();
            else
              newSink=objectFm.getPuit()+lostPower.getRight()-rune.getPower();
          }
          else
            newSink=objectFm.getPuit()+lostPower.getRight()-potion.getPower();
        }
        if(lostPower.getLeft()==1)
        {
          if(negative)
            newSink=objectFm.getPuit()+lostPower.getRight();
          else
            newSink=objectFm.getPuit()+lostPower.getRight();
          if(newSink<0)
            newSink=0;
        }
        if(newSink<0)
          newSink=0;
        newSink=(float)Math.round(newSink*100)/100; //Rounds sink to 2 decimals (negative initiative is 0.05 sink)

        if(newSink!=objectFm.getPuit())
        {
          if(successN)
            SocketManager.GAME_SEND_MESSAGE(this.player,"Vous avez réussi é fm cet objet - Le puits actuel de cet objet est "+newSink+".","009900");
          else
            SocketManager.GAME_SEND_MESSAGE(this.player,"Vous n'avez pas réussi é fm cet objet - Le puits actuel de cet objet est "+newSink+".","009900");
          objectFm.setPuit(newSink);
        }
        else if(successN)
          SocketManager.GAME_SEND_MESSAGE(this.player,"Vous avez réussi é fm cet objet, mais vous avez perdu certaines statistiques.","009900");
        else
          SocketManager.GAME_SEND_MESSAGE(this.player,"Vous n'avez pas réussi é fm cet élément.","009900");
      }
      else if(successN)
        SocketManager.GAME_SEND_MESSAGE(this.player,"Vous avez réussi é fm cet objet, mais vous avez perdu certaines statistiques.","009900");
      else
        SocketManager.GAME_SEND_MESSAGE(this.player,"Vous n'avez pas réussi é fm cet élément.","009900");
    }

    if(receiver==null)
    {
      if(signingRune!=null)
        decrementObjectQuantity(this.player,signingRune);
      if(potion!=null)
        decrementObjectQuantity(this.player,potionObject);
      else if(rune!=null)
        decrementObjectQuantity(this.player,runeObject);

      this.player.send("EmKO-"+objectFm.getGuid()+"|1|");
      this.ingredients.clear();
      this.player.send("EMKO+"+objectFm.getGuid()+"|1");
      this.ingredients.put(objectFm.getGuid(),1);

      if(newQuantity>=1)
      {
        if(potion!=null)
        {
          this.player.send("EMKO+"+potionObject.getGuid()+"|"+newQuantity);
          this.ingredients.put(potionObject.getGuid(),newQuantity);
        }
        else
        {
          this.player.send("EMKO+"+runeObject.getGuid()+"|"+newQuantity);
          this.ingredients.put(runeObject.getGuid(),newQuantity);
        }
      }
      else if(potion!=null)
        this.player.send("EMKO-"+potionObject.getGuid());
      else
        this.player.send("EMKO-"+runeObject.getGuid());
    }
    else
    {
      if(items!=null)
      {
        for(Entry<Player, ArrayList<Pair<Integer, Integer>>> entry : items.entrySet())
        {
          final Player player=entry.getKey();
          for(Pair<Integer, Integer> couple : entry.getValue())
          {
            if(signingRune!=null&&signingRune.getGuid()==couple.getLeft())
              decrementObjectQuantity(player,signingRune);
            if(potion!=null)
            {
              if(potionObject.getGuid()==couple.getLeft())
                decrementObjectQuantity(player,potionObject);
            }
            else
            {
              if(runeObject.getGuid()==couple.getLeft())
                decrementObjectQuantity(player,runeObject);
            }
          }
        }
      }

      String stats=objectFm.parseStatsString();
      this.player.send("ErKO+"+objectFm.getGuid()+"|1|"+objTemplate+"|"+stats);
      receiver.send("ErKO+"+objectFm.getGuid()+"|1|"+objTemplate+"|"+stats);
      this.player.send("EcK;"+objTemplate);
      receiver.send("EcK;"+objTemplate);
    }

    this.lastCraft.clear();
    this.lastCraft.putAll(this.ingredients);

    SocketManager.GAME_SEND_Ow_PACKET(this.player);
    if(!repeat) {
        this.oldJobCraft2 = this.getJobCraft();
        this.setJobCraft(null);
        }
        if(repeat)
            this.setJobCraft(oldJobCraft2);
    return true;
    }
    catch(Exception e)
    {
       e.printStackTrace();
      return true;
    }
  }

  //TODO: fix so you dont have to re-open magus interface
  public void clearMage(GameObject item, GameObject rune, Player receiver)
  {
    World.addGameObject(item,true);
    receiver.addObjet(item);
  }

  public void doCritPotionMage(Potion potion, GameObject objectFm)
  {
    for(final SpellEffect effect : objectFm.getEffects())
    {
      if(effect.getEffectID()!=100)
        continue;
      final String[] infos=effect.getArgs().split(";");
      try
      {
        final int min=Integer.parseInt(infos[0],16);
        final int max=Integer.parseInt(infos[1],16);
        int newMin=min*potion.getStrength()/100;
        final int newMax=max*potion.getStrength()/100;
        if(newMin==0)
          newMin=1;
        final String newRange="1d"+(newMax-newMin+1)+"+"+(newMin-1);
        final String newArgs=String.valueOf(Integer.toHexString(newMin))+";"+Integer.toHexString(newMax)+";-1;-1;0;"+newRange;
        effect.setArgs(newArgs);
        effect.setEffectID(potion.getStatId());
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  public void doCritRuneMage(Rune rune, GameObject objectFm, String statStringObj)
  {
    String runeStat=rune.getStatId();
    boolean negative=false;
    final int currentStats2=Job.viewActualStatsItem(objectFm,rune.getStatId());
    if(currentStats2==2) //change id if negative stat
    {
      runeStat=Rune.getNegativeStatByRuneStat(runeStat);
      if(!runeStat.equalsIgnoreCase(rune.getStatId()))
        negative=true;
    }
    if(currentStats2==1||currentStats2==2)
    {
      if(statStringObj.isEmpty())
        updateItemAddRune(objectFm,rune,runeStat);
      else
      {
        final String statsStr=objectFm.parseFMStatsString(runeStat,objectFm,rune.getStatsAdd(),negative);
        objectFm.clearStats();
        objectFm.parseStringToStats(statsStr,true);
      }
    }
    else if(objectFm.parseStatsString().isEmpty())
      updateItemAddRune(objectFm,rune,runeStat);
    else
    {
      final String statsStr=String.valueOf(objectFm.parseFMStatsString(runeStat,objectFm,rune.getStatsAdd(),negative));
      objectFm.clearStats();
      objectFm.parseStringToStats(statsStr,true);
    }
  }
  // reparation cac
  public boolean Reparation()
    {
  	try
      {
  	if(Config.singleton.serverId == 6)
  		  return false;
  	final Map<Integer, Integer> ingredients= this.ingredients;
  	GameObject item_modif= null;
  	GameObject potion= null;
    for(int idIngredient : ingredients.keySet())
    {
      final GameObject item=World.getGameObject(idIngredient);
      if(item==null)
        if(!this.player.hasItemGuid(idIngredient))
        {
          SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
         // SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-");
          //clearMage(objectFm,runeObject,this.player);
          ingredients.clear();
          //GameClient.leaveExchange(this.player);
          return false;
        }
      if(item == null || item.getTemplate() == null)
          {
            SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
           // SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-");
           // clearMage(objectFm,runeObject,this.player);
            ingredients.clear();
         	//GameClient.leaveExchange(this.player);
            return false;
          }
     
      int templateId=item.getTemplate().getId();
      if(templateId==2529 || templateId==2538 || templateId==2539 ||
    		  templateId==2540 || templateId==2541 || templateId==2542 ||
    		  templateId==2543 )
      {
        potion = item;
      }
      else
      {
    	  if(item.getTemplate().getStrTemplate().contains("32c#")) {
    		item_modif = item; 
    	  }
      }
    }
    if(item_modif == null || potion == null)
    {
      SocketManager.GAME_SEND_Ec_PACKET(this.player,"EI");
     // SocketManager.GAME_SEND_IO_PACKET_TO_MAP(this.player.getCurMap(),this.player.getId(),"-");
     // clearMage(objectFm,runeObject,this.player);
      ingredients.clear();
   	//GameClient.leaveExchange(this.player);
      return false;
    }

      int statNew=Integer.parseInt(item_modif.getTxtStat().get(Constant.STATS_RESIST),16)+2;

      item_modif.getTxtStat().remove(Constant.STATS_RESIST); // on retire les stats "32c"
      item_modif.addTxtStat(Constant.STATS_RESIST,Integer.toHexString(statNew));// on ajout les bonnes stats
      SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player,item_modif);
 
          if(potion.getQuantity()>1)
          {
            int newQuant=potion.getQuantity()-1;
            potion.setQuantity(newQuant,player);
            SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(player,potion);
          }
          else
          {
            Main.world.removeGameObject(potion.getGuid());
            player.removeItem(potion.getGuid());
            SocketManager.GAME_SEND_DELETE_STATS_ITEM_FM(player,potion.getGuid());
          }
      
    
      return true;
      }
      catch(Exception e)
      {
         e.printStackTrace();
        return true;
      }
    }


  public Pair<Integer, Float> doNormalRuneMage(Rune rune, GameObject objectFm, String statStringObj, float oldSink)
  {
    String runeStat=rune.getStatId();
    boolean negative=false;
    float currentTotalPower=currentTotalPower(statStringObj,objectFm);
    float runePower=rune.getPower();
    final int currentStats3=Job.viewActualStatsItem(objectFm,rune.getStatId());
    if(currentStats3==2) //change id if negative stat
    {
      runeStat=Rune.getNegativeStatByRuneStat(runeStat);
      if(runeStat!=rune.getStatId())
      {
        runePower=rune.getnPower();
        negative=true;
      }
    }
    if(currentStats3==1||currentStats3==2) //not first mage
    {
      if(statStringObj.isEmpty()) //item does not have stats
      {
        updateItemAddRune(objectFm,rune,runeStat);
        return new Pair<Integer, Float>(0,0f);
      }
      else //nSuccess targeting normal stats
      {
        String exoStatStr=objectFm.findOverExo(objectFm,Integer.parseInt(runeStat,16));
        float exoPower=0;
        if(exoStatStr!="") //item has exo
        {
          String[] exoSplit=exoStatStr.split(";"); //calculate total exo power
          for(int i=0;i<exoSplit.length;i++)
          {
            String[] exoSplit2=exoSplit[i].split(",");
            if(Rune.getRuneByStatId(Integer.toHexString(Integer.parseInt(exoSplit2[0])))!=null)
            {
              Rune tempRune=Rune.getRuneByStatId(Integer.toHexString(Integer.parseInt(exoSplit2[0])));
              float statPwr=tempRune.getPower()/tempRune.getStatsAdd();
              float entryPower=statPwr*(Integer.valueOf(exoSplit2[1])-getStatBaseMaxs(objectFm.getTemplate(),Integer.toString(Integer.valueOf(exoSplit2[0]),16)));
              exoPower=exoPower+entryPower;
            }
          }
          exoPower=(float)Math.round(exoPower*100)/100; //rounding to two decimals

          if(exoPower>=runePower) //reduce all from exo stats, do not give sink, do not consume sink
          {
            updateItemStatsEC(objectFm,runePower,runeStat);
            updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative);
            return new Pair<Integer, Float>(-1,-1f);
          }
          else if(exoPower<runePower)
          {
            if(oldSink>0)
            {
              if(oldSink>=runePower-exoPower) //remove exo and then reduce sink
              {
                updateItemStatsEC(objectFm,exoPower,runeStat);
                updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative);
                return new Pair<Integer, Float>(0,0f);
              }
              else if(oldSink<runePower-exoPower) //reduce from exo, then from sink, then from stats
              {
                updateItemStatsEC(objectFm,exoPower,runeStat);
                objectFm.setPuit(0);
                updateItemStatsEC(objectFm,runePower-(exoPower+oldSink),runeStat);
                return new Pair<Integer, Float>(1,currentTotalPower-currentTotalPower(updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative),objectFm));
              }
            }
            else //remove exo then reduce stats
            {
              updateItemStatsEC(objectFm,exoPower,runeStat);
              updateItemStatsEC(objectFm,runePower-exoPower,runeStat);
              return new Pair<Integer, Float>(1,currentTotalPower-currentTotalPower(updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative),objectFm));
            }
          }
        } //end of exo region
        else //not-exo'd item with stats
        {
          if(oldSink>0)
          {
            if(oldSink>=runePower) //reduce all from sink
            {
              updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative);
              return new Pair<Integer, Float>(0,0f);
            }
            else if(oldSink<runePower) //reduce from sink then from stats
            {
              float restPower=runePower-oldSink;
              objectFm.setPuit(0);
              updateItemStatsEC(objectFm,restPower,runeStat);
              return new Pair<Integer, Float>(1,currentTotalPower-currentTotalPower(updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative),objectFm));
            }
          }
          else //reduce all from stats
          {
            updateItemStatsEC(objectFm,runePower,runeStat);
            return new Pair<Integer, Float>(1,currentTotalPower-currentTotalPower(updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative),objectFm));
          }
        }
      }
    }
    else //first stat mage
    {
      if(statStringObj.isEmpty()) //item does not have stats
      {
        updateItemAddRune(objectFm,rune,runeStat);
        return new Pair<Integer, Float>(0,0f);
      }
      else //nSuccess targeting normal stats
      {
        String exoStatStr=objectFm.findOverExo(objectFm,Integer.parseInt(runeStat,16));
        float exoPower=0;
        if(exoStatStr!="") //item has exo
        {
          String[] exoSplit=exoStatStr.split(";"); //calculate total exo power
          for(int i=0;i<exoSplit.length;i++)
          {
            String[] exoSplit2=exoSplit[i].split(",");
            if(Rune.getRuneByStatId(Integer.toHexString(Integer.parseInt(exoSplit2[0])))!=null)
            {
              Rune tempRune=Rune.getRuneByStatId(Integer.toHexString(Integer.parseInt(exoSplit2[0])));
              float statPwr=tempRune.getPower()/tempRune.getStatsAdd();
              float entryPower=statPwr*(Integer.valueOf(exoSplit2[1])-getStatBaseMaxs(objectFm.getTemplate(),Integer.toString(Integer.valueOf(exoSplit2[0]),16)));
              exoPower=exoPower+entryPower;
            }
          }
          exoPower=(float)Math.round(exoPower*100)/100; //rounding to two decimals

          if(exoPower>=runePower) //reduce all from exo stats, do not give sink, do not consume sink
          {
            final String statsStr=String.valueOf(updateItemStatsEC(objectFm,runePower,runeStat)+","+runeStat+"#"+Integer.toHexString(rune.getStatsAdd())+"#0#0#0d0+"+rune.getStatsAdd());
            objectFm.clearStats();
            objectFm.parseStringToStats(statsStr,true);
            return new Pair<Integer, Float>(-1,-1f);
          }
          else if(exoPower<runePower)
          {
            if(oldSink>0)
            {
              if(oldSink>=runePower-exoPower) //remove exo and then reduce sink
              {
                updateItemStatsEC(objectFm,exoPower,runeStat);
                updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative);
                return new Pair<Integer, Float>(0,0f);
              }
              else if(oldSink<runePower-exoPower) //reduce from exo, then from sink, then from stats
              {
                updateItemStatsEC(objectFm,exoPower,runeStat);
                objectFm.setPuit(0);
                updateItemStatsEC(objectFm,runePower-(exoPower+oldSink),runeStat);
                return new Pair<Integer, Float>(1,currentTotalPower-currentTotalPower(updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative),objectFm));
              }
            }
            else //remove exo then reduce stats
            {
              updateItemStatsEC(objectFm,exoPower,runeStat);
              updateItemStatsEC(objectFm,runePower-exoPower,runeStat);
              return new Pair<Integer, Float>(0,currentTotalPower-currentTotalPower(updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative),objectFm));
            }
          }
        } //end of exo region
        else //not-exo'd item with stats
        {
          if(oldSink>0)
          {
            if(oldSink>=runePower) //reduce all from sink
            {
              updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative);
              return new Pair<Integer, Float>(0,0f);
            }
            else if(oldSink<runePower) //reduce from sink then from stats
            {
              float restPower=runePower-oldSink;
              objectFm.setPuit(0);
              updateItemStatsEC(objectFm,restPower,runeStat);
              return new Pair<Integer, Float>(1,currentTotalPower-currentTotalPower(updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative),objectFm));
            }
          }
          else //reduce all from stats
          {
            updateItemStatsEC(objectFm,runePower,runeStat);
            return new Pair<Integer, Float>(1,currentTotalPower-currentTotalPower(updateItemStats(runeStat,objectFm,rune.getStatsAdd(),negative),objectFm));
          }
        }
      }
    }
    return new Pair<Integer, Float>(0,0f);
  }

  public Pair<Integer, Float> doFailRuneMage(Rune rune, GameObject objectFm, String statStringObj, float oldSink)
  {
    String runeStat=rune.getStatId();
    float currentTotalPower=currentTotalPower(statStringObj,objectFm);
    float runePower=rune.getPower();
    final int currentStats3=Job.viewActualStatsItem(objectFm,rune.getStatId());
    if(currentStats3==2) //change id if negative stat
    {
      runeStat=Rune.getNegativeStatByRuneStat(runeStat);
      if(runeStat!=rune.getStatId())
        runePower=rune.getnPower();
    }
    if(statStringObj.isEmpty()) //item does not have stats
      return new Pair<Integer, Float>(0,0f);
    else //nSuccess targeting normal stats
    {
      String exoStatStr=objectFm.findOverExo(objectFm,Integer.parseInt(runeStat,16));
      float exoPower=0f;
      if(exoStatStr!="") //item has exo
      {
        String[] exoSplit=exoStatStr.split(";"); //split string per stat
        for(int i=0;i<exoSplit.length;i++)
        {
          String[] exoSplit2=exoSplit[i].split(","); //split stat per value
          if(Rune.getRuneByStatId(Integer.toHexString(Integer.parseInt(exoSplit2[0])))!=null)
          {
            Rune tempRune=Rune.getRuneByStatId(Integer.toHexString(Integer.parseInt(exoSplit2[0])));
            float statPwr=tempRune.getPower()*tempRune.getStatsAdd();
            float entryPower=statPwr*(Integer.valueOf(exoSplit2[1])-getStatBaseMaxs(objectFm.getTemplate(),Integer.toString(Integer.valueOf(exoSplit2[0]),16)));
            exoPower=exoPower+entryPower;
          }
        }
        exoPower=(float)Math.round(exoPower*100)/100; //rounding to two decimals

        if(exoPower>=runePower) //reduce all from exo stats, do not give sink, do not consume sink
        {
          updateItemStatsEC(objectFm,runePower,runeStat);
          return new Pair<Integer, Float>(-1,0f);
        }
        else if(exoPower<runePower)
        {
          if(oldSink>0)
          {
            if(oldSink>=runePower-exoPower) //remove exo and then reduce sink
            {
              updateItemStatsEC(objectFm,exoPower,runeStat);
              return new Pair<Integer, Float>(0,0f);
            }
            else if(oldSink<runePower-exoPower) //reduce from exo, then from sink, then from stats
            {
              updateItemStatsEC(objectFm,exoPower,runeStat);
              float restPower=runePower-(exoPower+oldSink);
              return new Pair<Integer, Float>(0,currentTotalPower-currentTotalPower(updateItemStatsEC(objectFm,restPower,runeStat),objectFm));
            }
          }
          else //remove exo then reduce stats
          {
            updateItemStatsEC(objectFm,exoPower,runeStat);
            return new Pair<Integer, Float>(0,currentTotalPower-currentTotalPower(updateItemStatsEC(objectFm,runePower-exoPower,runeStat),objectFm));
          }
        }
      } //end of exo region
      else //not-exo'd item with stats
      {
        if(oldSink>0)
        {
          if(oldSink>=runePower) //reduce all from sink
            return new Pair<Integer, Float>(0,0f);
          else if(oldSink<runePower) //reduce from sink then from stats
          {
            float restPower=runePower-oldSink;
            return new Pair<Integer, Float>(0,currentTotalPower-currentTotalPower(updateItemStatsEC(objectFm,restPower,runeStat),objectFm));
          }
        }
        else //reduce all from stats
          return new Pair<Integer, Float>(0,currentTotalPower-currentTotalPower(updateItemStatsEC(objectFm,runePower,runeStat),objectFm));
      }
    }
    return new Pair<Integer, Float>(0,0f);
  }

  public Pair<Integer, Float> doFailPotionMage(Potion potion, GameObject objectFm, String statStringObj, float oldSink)
  {
    String potionStat=Integer.toHexString((int)potion.getStatId());
    float currentTotalPower=currentTotalPower(statStringObj,objectFm);
    float potionPower=potion.getPower();
    if(statStringObj.isEmpty()) //item does not have stats
      return new Pair<Integer, Float>(0,0f);
    else //nSuccess targeting normal stats
    {
      String exoStatStr=objectFm.findOverExo(objectFm,Integer.parseInt(potionStat,16));
      float exoPower=0f;
      if(exoStatStr!="") //item has exo
      {
        String[] exoSplit=exoStatStr.split(";"); //split string per stat
        for(int i=0;i<exoSplit.length;i++)
        {
          String[] exoSplit2=exoSplit[i].split(","); //split stat per value
          if(Rune.getRuneByStatId(Integer.toHexString(Integer.parseInt(exoSplit2[0])))!=null)
          {
            Rune tempRune=Rune.getRuneByStatId(Integer.toHexString(Integer.parseInt(exoSplit2[0])));
            float statPwr=tempRune.getPower()/tempRune.getStatsAdd();
            float entryPower=statPwr*(Integer.valueOf(exoSplit2[1])-getStatBaseMaxs(objectFm.getTemplate(),Integer.toString(Integer.valueOf(exoSplit2[0]),16)));
            exoPower=exoPower+entryPower;
          }
        }
        exoPower=(float)Math.round(exoPower*100)/100; //rounding to two decimals

        if(exoPower>=potionPower) //reduce all from exo stats, do not give sink, do not consume sink
        {
          updateItemStatsEC(objectFm,potionPower,potionStat);
          return new Pair<Integer, Float>(-1,0f);
        }
        else if(exoPower<potionPower)
        {
          if(oldSink>0)
          {
            if(oldSink>=potionPower-exoPower) //remove exo and then reduce sink
            {
              updateItemStatsEC(objectFm,exoPower,potionStat);
              return new Pair<Integer, Float>(0,0f);
            }
            else if(oldSink<potionPower-exoPower) //reduce from exo, then from sink, then from stats
            {
              updateItemStatsEC(objectFm,exoPower,potionStat);
              float restPower=potionPower-(exoPower+oldSink);
              return new Pair<Integer, Float>(0,currentTotalPower-currentTotalPower(updateItemStatsEC(objectFm,restPower,potionStat),objectFm));
            }
          }
          else //remove exo then reduce stats
          {
            updateItemStatsEC(objectFm,exoPower,potionStat);
            return new Pair<Integer, Float>(0,currentTotalPower-currentTotalPower(updateItemStatsEC(objectFm,potionPower-exoPower,potionStat),objectFm));
          }
        }
      } //end of exo region
      else //not-exo'd item with stats
      {
        if(oldSink>0)
        {
          if(oldSink>=potionPower) //reduce all from sink
            return new Pair<Integer, Float>(0,0f);
          else if(oldSink<potionPower) //reduce from sink then from stats
          {
            float restPower=potionPower-oldSink;
            return new Pair<Integer, Float>(0,currentTotalPower-currentTotalPower(updateItemStatsEC(objectFm,restPower,potionStat),objectFm));
          }
        }
        else //reduce all from stats
          return new Pair<Integer, Float>(0,currentTotalPower-currentTotalPower(updateItemStatsEC(objectFm,potionPower,potionStat),objectFm));
      }
    }
    return new Pair<Integer, Float>(0,0f);
  }

  private void decrementObjectQuantity(Player player, GameObject object)
  {
    if(object!=null)
    {
      int newQua=object.getQuantity()-1;
      if(newQua<=0)
      {
        player.removeItem(object.getGuid(),object.getQuantity(),true,true);
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player,object.getGuid());
      }
      else
      {
        object.setQuantity(newQua,player);
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(player,object);
      }
    }
  }

  public static float currentTotalPower(final String statsModelo, final GameObject obj)
  {
    if(statsModelo.equalsIgnoreCase(""))
      return 0;
    float Weigth=1;
    float Alto=0;
    final String[] split=statsModelo.split(",");
    String[] array;
    for(int length=(array=split).length,i=0;i<length;++i)
    {
      final String s=array[i];
      final String[] stats=s.split("#");
      final int statID=Integer.parseInt(stats[0],16);
      if(statID!=985)
        if(statID!=988)
        {
          boolean xy=false;
          int[] armes_EFFECT_IDS;
          for(int length2=(armes_EFFECT_IDS=Constant.ARMES_EFFECT_IDS).length,j=0;j<length2;++j)
          {
            final int a=armes_EFFECT_IDS[j];
            if(a==statID)
              xy=true;
          }
          if(!xy)
          {
            String jet="";
            int qua=1;
            try
            {
              jet=stats[4];
              qua=Formulas.getRandomJet(jet);
              try
              {
                final int min=Integer.parseInt(stats[1],16);
                final int max=Integer.parseInt(stats[2],16);
                qua=min;
                if(max!=0)
                  qua=max;
              }
              catch(Exception e)
              {
                e.printStackTrace();
                qua=Formulas.getRandomJet(jet);
              }
            }
            catch(Exception ex)
            {
            }
            if(Rune.getRuneByStatId(Integer.toHexString(statID))!=null)
            {
              Rune rune=Rune.getRuneByStatId(Integer.toHexString(statID));
              Weigth=qua*(rune.getPower()/rune.getStatsAdd());
              Alto+=Weigth;
            }
          }
        }
    }
    Alto=(float)Math.round(Alto*100)/100; //rounds to 2 decimals (negative ini = 0.05s)
    return Alto;
  }

  //v2.8 - negative stat support
  public static int getStatBaseMaxs(final ObjectTemplate objMod, final String statsModif)
  {
    final String[] split=objMod.getStrTemplate().split(",");
    String[] array;
    for(int length=(array=split).length,i=0;i<length;++i)
    {
      final String s=array[i];
      final String[] stats=s.split("#");
      if(stats[0].toLowerCase().compareTo(statsModif.toLowerCase())==0)
      {
        int max=Integer.parseInt(stats[2],16);
        if(max==0)
          max=Integer.parseInt(stats[1],16);
        return max;
      }
      else if(stats[0].toLowerCase().compareTo(Rune.getNegativeStatByRuneStat(statsModif.toLowerCase()))==0)
        return -Integer.parseInt(stats[1],16);
    }
    return 0;
  }

  //v2.8 - negative stat support
  public static int getStatBaseMins(final ObjectTemplate objMod, final String statsModif)
  {
    final String[] split=objMod.getStrTemplate().split(",");
    String[] array;
    for(int length=(array=split).length,i=0;i<length;++i)
    {
      final String s=array[i];
      final String[] stats=s.split("#");
      if(stats[0].toLowerCase().compareTo(statsModif.toLowerCase())==0)
        return Integer.parseInt(stats[1],16);
      if(stats[0].toLowerCase().compareTo(Rune.getNegativeStatByRuneStat(statsModif.toLowerCase()))==0)
      {
        int max=Integer.parseInt(stats[2],16);
        if(max==0)
          max=Integer.parseInt(stats[1],16);
        return -max;
      }
    }
    return 0;
  }

  public static float currentStatPower(final GameObject obj, Rune rune)
  {
    for(final Map.Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet())
    {
      final int statID=entry.getKey();
      if(Integer.toHexString(statID).toLowerCase().compareTo(rune.getStatId())==0)
      {
        float finalWeight=0; //v2.0 - Divide by 0 handler
        final float Weight=entry.getValue()*(rune.getPower()/rune.getStatsAdd());
        if(Weight==0)
          finalWeight=1;
        else
          finalWeight=Weight;
        return finalWeight;
      }
      else if(Integer.toHexString(statID).toLowerCase().compareTo(Rune.getNegativeStatByRuneStat(rune.getStatId()))==0)
      {
        float finalWeight=0; //v2.0 - Divide by 0 handler
        final float Weight=entry.getValue()*(rune.getnPower()/rune.getStatsAdd());
        if(Weight==0)
          finalWeight=1;
        else
          finalWeight=Weight;
        return finalWeight;
      }
    }
    return 0;
  }

  public static float minTotalPower(final int objTemplateID)
  {
    float weight=0;
    float alt=0;
    String statsTemplate="";
    statsTemplate=Main.world.getObjTemplate(objTemplateID).getStrTemplate();
    if(statsTemplate==null||statsTemplate.isEmpty())
      return 0;
    final String[] split=statsTemplate.split(",");
    for(String s : split)
    {
      final String[] stats=s.split("#");
      final int statID=Integer.parseInt(stats[0],16);
      boolean sig=true;
      for(int effectID : Constant.ARMES_EFFECT_IDS)
        if(effectID==statID)
          sig=false;
      if(sig)
      {
        String jet="";
        int value=1;
        try
        {
          jet=stats[4];
         
          try
          {
            value=Integer.parseInt(stats[1],16);
            if(Rune.isNegativeStat(Integer.toHexString(statID)))
              value=Integer.parseInt(stats[2],16);
          }
          catch(Exception e)
          {
            e.printStackTrace();
            jet=stats[4];
            value=Formulas.getRandomJet(jet);
          }
        }
        catch(Exception e)
        {
          //e.printStackTrace();
        }
        if(Rune.getRuneByStatId(Integer.toHexString(statID))!=null)
        {
          Rune rune=Rune.getRuneByStatId(Integer.toHexString(statID));
          weight=value*(rune.getPower()/rune.getStatsAdd());
          alt+=weight;
        }
      }
    }
    return alt;
  }

  //v2.8 - de-spaghetti'd
  public static float maxTotalPower(final int objTemplateID)
  {
    float weight=0;
    float alt=0;
    String statsTemplate="";
    statsTemplate=Main.world.getObjTemplate(objTemplateID).getStrTemplate();
    if(statsTemplate==null||statsTemplate.isEmpty())
      return 0;
    final String[] split=statsTemplate.split(",");
    for(String s : split)
    {
      final String[] stats=s.split("#");
      final int statID=Integer.parseInt(stats[0],16);
      boolean sig=true;
      for(int effectID : Constant.ARMES_EFFECT_IDS)
        if(effectID==statID)
          sig=false;
      if(sig)
      {
        String jet="";
        int value=1;
        try
        {
          jet=stats[4];
          value=Formulas.getRandomJet(jet);
          try
          {
            final int min=Integer.parseInt(stats[1],16);
            int max=Integer.parseInt(stats[2],16);
            if(Rune.isNegativeStat(Integer.toHexString(statID)))
              max=Integer.parseInt(stats[1],16);
            value=min;
            if(max!=0)
              value=max;
          }
          catch(Exception e)
          {
           // e.printStackTrace();
            value=Formulas.getRandomJet(jet);
          }
        }
        catch(Exception e)
        {
          //e.printStackTrace();
        }
        if(Rune.getRuneByStatId(Integer.toHexString(statID))!=null)
        {
          Rune rune=Rune.getRuneByStatId(Integer.toHexString(statID));
          weight=value*(rune.getStatsAdd()/rune.getPower());
          alt+=weight;
        }
      }
    }
    return alt;
  }

  public static int getMaxStat(float runePower, byte runeStat)
  {
    float basePower=runePower/runeStat; //power per 1 stat
    return (int)Math.floor(100/basePower);
  }

  public static int getBaseMaxJet(int templateID, String statsModif)
  {
    ObjectTemplate t=Main.world.getObjTemplate(templateID);
    String[] splitted=t.getStrTemplate().split(",");
    for(String s : splitted)
    {
      String[] stats=s.split("#");
      if(stats[0].compareTo(statsModif)>0)//Effets n'existe pas de base
      {
      }
      else if(stats[0].compareTo(statsModif)==0)//L'effet existe bien !
      {
        int max=Integer.parseInt(stats[2],16);
        if(max==0)
          max=Integer.parseInt(stats[1],16);//Pas de jet maximum on prend le minimum
        return max;
      }
    }
    return 0;
  }

  public static String updateItemStats(String runeStat, GameObject objectFm, byte statsAdd, boolean negative)
  {
    String newStats=objectFm.parseFMStatsString(runeStat,objectFm,statsAdd,negative);
    objectFm.clearStats();
    objectFm.parseStringToStats(newStats,true);
    return newStats;
  }

  public static String updateItemStatsEC(GameObject objectFm, float power, String runeStat)
  {
    String newStats=objectFm.parseStringStatsEC_FM(objectFm,power,Integer.parseInt(runeStat,16));
    objectFm.clearStats();
    objectFm.parseStringToStats(newStats,true);
    return newStats;
  }

  public static String updateItemAddRune(GameObject objectFm, Rune rune, String runeStat)
  {
    final String statsStr=String.valueOf(runeStat)+"#"+Integer.toHexString(rune.getStatsAdd())+"#0#0#0d0+"+rune.getStatsAdd();
    objectFm.clearStats();
    objectFm.parseStringToStats(statsStr,true);
    return statsStr;
  }
}