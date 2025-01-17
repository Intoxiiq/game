package soufix.object;

import soufix.client.Player;
import soufix.client.other.Stats;
import soufix.common.ConditionParser;
import soufix.common.Formulas;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.monster.MobGroup;
import soufix.entity.pet.PetEntry;
import soufix.fight.spells.SpellEffect;
import soufix.game.World;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Logging;
import soufix.main.Main;
import soufix.object.entity.Capture;
import soufix.other.Dopeul;

import java.util.*;

public class ObjectTemplate
{
  private int id;
  private String strTemplate;
  private String name;
  private int type;
  private int level;
  private int pod;
  private int price;
  private int panoId;
  private String conditions;
  private int PACost, POmin, POmax, tauxCC, tauxEC, bonusCC;
  private boolean isTwoHanded;
  private long sold;
  private int avgPrice;
  private int points, newPrice;
  private int tokens;
  private ArrayList<ObjectAction> onUseActions;
  public int boutique=0;
  public int tokenShop=0;

  public String toString()
  {
    return id+"";
  }

  public ObjectTemplate(int id, String strTemplate, String name, int type, int level, int pod, int price, int panoId, String conditions, String armesInfos, int sold, int avgPrice, int points, int newPrice, int boutique, int tokenShop, final int tokens)
  {
    this.id=id;
    this.strTemplate=strTemplate;
    this.name=name;
    this.type=type;
    this.level=level;
    this.pod=pod;
    this.price=price;
    this.panoId=panoId;
    this.conditions=conditions;
    this.PACost=-1;
    this.POmin=1;
    this.POmax=1;
    this.tauxCC=100;
    this.tauxEC=2;
    this.bonusCC=0;
    this.sold=sold;
    this.avgPrice=avgPrice;
    this.points=points;
    this.newPrice=newPrice;
    this.tokens=tokens;
    this.boutique=boutique;
    this.tokenShop=boutique;
    if(armesInfos.isEmpty())
      return;
    try
    {
      String[] infos=armesInfos.split(";");
      PACost=Integer.parseInt(infos[0]);
      POmin=Integer.parseInt(infos[1]);
      POmax=Integer.parseInt(infos[2]);
      tauxCC=Integer.parseInt(infos[3]);
      tauxEC=Integer.parseInt(infos[4]);
      bonusCC=Integer.parseInt(infos[5]);
      isTwoHanded=infos[6].equals("1");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public void setInfos(String strTemplate, String name, int type, int level, int pod, int price, int panoId, String conditions, String armesInfos, int sold, int avgPrice, int points, int newPrice)
  {
    this.strTemplate=strTemplate;
    this.name=name;
    this.type=type;
    this.level=level;
    this.pod=pod;
    this.price=price;
    this.panoId=panoId;
    this.conditions=conditions;
    this.PACost=-1;
    this.POmin=1;
    this.POmax=1;
    this.tauxCC=100;
    this.tauxEC=2;
    this.bonusCC=0;
    this.sold=sold;
    this.avgPrice=avgPrice;
    this.points=points;
    this.newPrice=newPrice;
    try
    {
      String[] infos=armesInfos.split(";");
      PACost=Integer.parseInt(infos[0]);
      POmin=Integer.parseInt(infos[1]);
      POmax=Integer.parseInt(infos[2]);
      tauxCC=Integer.parseInt(infos[3]);
      tauxEC=Integer.parseInt(infos[4]);
      bonusCC=Integer.parseInt(infos[5]);
      isTwoHanded=infos[6].equals("1");
    }
    catch(Exception e)
    {
      //nothing
    }
  }

  public int getId()
  {
    return id;
  }

  public void setId(int id)
  {
    this.id=id;
  }

  public String getStrTemplate()
  {
    return strTemplate;
  }

  public void setStrTemplate(String strTemplate)
  {
    this.strTemplate=strTemplate;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name=name;
  }

  public int getType()
  {
    return type;
  }

  public void setType(int type)
  {
    this.type=type;
  }

  public int getLevel()
  {
    return level;
  }

  public void setLevel(int level)
  {
    this.level=level;
  }

  public int getPod()
  {
    return pod;
  }

  public void setPod(int pod)
  {
    this.pod=pod;
  }

  public int getPrice()
  {
    return price;
  }

  public void setPrice(int price)
  {
    this.price=price;
  }

  public int getPanoId()
  {
    return panoId;
  }

  public void setPanoId(int panoId)
  {
    this.panoId=panoId;
  }

  public String getConditions()
  {
    return conditions;
  }

  public void setConditions(String conditions)
  {
    this.conditions=conditions;
  }

  public int getPACost()
  {
    return PACost;
  }

  public void setPACost(int pACost)
  {
    PACost=pACost;
  }

  public int getPOmin()
  {
    return POmin;
  }
  private ArrayList<Integer> _mobsQueDropean = new ArrayList<>(); 
  public ArrayList<Integer> getMobsQueDropean() {
       return _mobsQueDropean;
   }
   public void addMobQueDropea(int id) {
       if (!_mobsQueDropean.contains(id)) {
           _mobsQueDropean.add(id);
       }
   }
   public void delMobQueDropea(int id) {
       _mobsQueDropean.remove((Object) id);
   }
  public void setPOmin(int pOmin)
  {
    POmin=pOmin;
  }

  public int getPOmax()
  {
    return POmax;
  }

  public void setPOmax(int pOmax)
  {
    POmax=pOmax;
  }

  public int getTauxCC()
  {
    return tauxCC;
  }

  public void setTauxCC(int tauxCC)
  {
    this.tauxCC=tauxCC;
  }

  public int getTauxEC()
  {
    return tauxEC;
  }

  public void setTauxEC(int tauxEC)
  {
    this.tauxEC=tauxEC;
  }

  public int getBonusCC()
  {
    return bonusCC;
  }

  public void setBonusCC(int bonusCC)
  {
    this.bonusCC=bonusCC;
  }

  public boolean isTwoHanded()
  {
    return isTwoHanded;
  }

  public void setTwoHanded(boolean isTwoHanded)
  {
    this.isTwoHanded=isTwoHanded;
  }

  public int getAvgPrice()
  {
    return avgPrice;
  }

  public long getSold()
  {
    return this.sold;
  }

  public int getPoints()
  {
    return this.points;
  }

  //v0.01 - Token Shop
  public int getTokens()
  {
    return this.tokens;
  }

  public void addAction(ObjectAction A)
  {
    if(this.onUseActions==null)
      this.onUseActions=new ArrayList<>();
    this.onUseActions.add(A);
  }

  public ArrayList<ObjectAction> getOnUseActions()
  {
    return onUseActions==null ? new ArrayList<>() : onUseActions;
  }

  public GameObject createNewCertificat(GameObject obj)
  {
    int id=Database.getDynamics().getObjectData().getNextId();
    GameObject item=null;
    if(getType()==Constant.ITEM_TYPE_CERTIFICAT_CHANIL)
    {
      PetEntry myPets=Main.world.getPetsEntry(obj.getGuid());
      Map<Integer, String> txtStat=new HashMap<Integer, String>();
      Map<Integer, String> actualStat=new HashMap<Integer, String>();
      actualStat=obj.getTxtStat();
      if(actualStat.containsKey(Constant.STATS_PETS_PDV))
        txtStat.put(Constant.STATS_PETS_PDV,actualStat.get(Constant.STATS_PETS_PDV));
      if(actualStat.containsKey(Constant.STATS_PETS_DATE))
        txtStat.put(Constant.STATS_PETS_DATE,myPets.getLastEatDate()+"");
      if(actualStat.containsKey(Constant.STATS_PETS_POIDS))
        txtStat.put(Constant.STATS_PETS_POIDS,actualStat.get(Constant.STATS_PETS_POIDS));
      if(actualStat.containsKey(Constant.STATS_PETS_EPO))
        txtStat.put(Constant.STATS_PETS_EPO,actualStat.get(Constant.STATS_PETS_EPO));
      if(actualStat.containsKey(Constant.STATS_PETS_REPAS))
        txtStat.put(Constant.STATS_PETS_REPAS,actualStat.get(Constant.STATS_PETS_REPAS));
      item=new GameObject(id,getId(),1,Constant.ITEM_POS_NO_EQUIPED,obj.getStats(),new ArrayList<SpellEffect>(),new HashMap<Integer, Integer>(),txtStat,0);
      Main.world.removePetsEntry(obj.getGuid());
      Database.getDynamics().getPetData().delete(obj.getGuid());
    }
    return item;
  }

  public GameObject createNewFamilier(GameObject obj)
  {
    int id=Database.getDynamics().getObjectData().getNextId();
    Map<Integer, String> stats=new HashMap<>();
    stats.putAll(obj.getTxtStat());

    GameObject object=new GameObject(id,getId(),1,Constant.ITEM_POS_NO_EQUIPED,obj.getStats(),new ArrayList<>(),new HashMap<>(),stats,0);

    long time=System.currentTimeMillis();
    Main.world.addPetsEntry(new PetEntry(id,getId(),time,0,Integer.parseInt(stats.get(Constant.STATS_PETS_PDV),16),Integer.parseInt(stats.get(Constant.STATS_PETS_POIDS),16),!stats.containsKey(Constant.STATS_PETS_EPO)));
    Database.getDynamics().getPetData().add(id,time,getId());
    return object;
  }

  public GameObject createNewBenediction(int turn)
  {
    int id=Database.getDynamics().getObjectData().getNextId();
    GameObject item=null;
    Stats stats=generateNewStatsFromTemplate(getStrTemplate(),true);
    stats.addOneStat(Constant.STATS_TURN,turn);
    item=new GameObject(id,getId(),1,Constant.ITEM_POS_BENEDICTION,stats,new ArrayList<>(),new HashMap<>(),new HashMap<>(),0);
    return item;
  }

  public GameObject createNewMalediction()
  {
    int id=Database.getDynamics().getObjectData().getNextId();
    Stats stats=generateNewStatsFromTemplate(getStrTemplate(),true);
    stats.addOneStat(Constant.STATS_TURN,1);
    return new GameObject(id,getId(),1,Constant.ITEM_POS_MALEDICTION,stats,new ArrayList<>(),new HashMap<>(),new HashMap<>(),0);
  }

  public GameObject createNewRoleplayBuff()
  {
    int id=Database.getDynamics().getObjectData().getNextId();
    Stats stats=generateNewStatsFromTemplate(getStrTemplate(),true);
    stats.addOneStat(Constant.STATS_TURN,1);
    return new GameObject(id,getId(),1,Constant.ITEM_POS_ROLEPLAY_BUFF,stats,new ArrayList<>(),new HashMap<>(),new HashMap<>(),0);
  }

  public GameObject createNewCandy(int turn)
  {
    int id=Database.getDynamics().getObjectData().getNextId();
    GameObject item=null;
    Stats stats=generateNewStatsFromTemplate(getStrTemplate(),true);
    stats.addOneStat(Constant.STATS_TURN,turn);
    item=new GameObject(id,getId(),1,Constant.ITEM_POS_BONBON,stats,new ArrayList<SpellEffect>(),new HashMap<Integer, Integer>(),new HashMap<Integer, String>(),0);
    return item;
  }

  public GameObject createNewFollowPnj(int turn)
  {
    int id=Database.getDynamics().getObjectData().getNextId();
    GameObject item=null;
    Stats stats=generateNewStatsFromTemplate(getStrTemplate(),true);
    stats.addOneStat(Constant.STATS_TURN,turn);
    stats.addOneStat(148,0);
    item=new GameObject(id,getId(),1,Constant.ITEM_POS_PNJ_SUIVEUR,stats,new ArrayList<SpellEffect>(),new HashMap<Integer, Integer>(),new HashMap<Integer, String>(),0);
    return item;
  }

  public GameObject createNewItem(int qua, boolean useMax)
  {
	  if(this.id == 101620)
		  useMax = true;
	  if(Config.singleton.serverId == 8)
		  useMax = true;
    int id=Database.getDynamics().getObjectData().getNextId();
    GameObject item;
    if(getType()==Constant.ITEM_TYPE_QUETES&&(Constant.isCertificatDopeuls(getId())||getId()==6653))
    {
      Map<Integer, String> txtStat=new HashMap<Integer, String>();
      txtStat.put(Constant.STATS_DATE,System.currentTimeMillis()+"");
      item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,new Stats(false,null),new ArrayList<SpellEffect>(),new HashMap<Integer, Integer>(),txtStat,0);
    }
    else if(this.getId()==10207)
    {
      item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,new Stats(false,null),new ArrayList<SpellEffect>(),new HashMap<Integer, Integer>(),Dopeul.generateStatsTrousseau(),0);
    }
    else if(getType()==Constant.ITEM_TYPE_FAMILIER)
    {
      item=new GameObject(id,getId(),1,Constant.ITEM_POS_NO_EQUIPED,(useMax ? generateNewStatsFromTemplate(Main.world.getPets(this.getId()).getJet(),false) : new Stats(false,null)),new ArrayList<>(),new HashMap<>(),Main.world.getPets(getId()).generateNewtxtStatsForPets(),0);
      //Ajouter du Pets_data SQL et World
      long time=System.currentTimeMillis();
      Main.world.addPetsEntry(new PetEntry(id,getId(),time,0,10,0,false));
      Database.getDynamics().getPetData().add(id,time,getId());
    }
    else if(getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
    {
      item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,generateNewStatsFromTemplate(getStrTemplate(),useMax),getEffectTemplate(getStrTemplate()),new HashMap<>(),new HashMap<>(),0);
    }
    else
    {
      if(getType()==Constant.ITEM_TYPE_OBJET_ELEVAGE)
      {
        item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,new Stats(false,null),new ArrayList<>(),new HashMap<>(),getStringResistance(getStrTemplate()),0);
      }
      else if(Constant.isIncarnationWeapon(getId()))
      {
        Map<Integer, Integer> Stats=new HashMap<>();
        Stats.put(Constant.ERR_STATS_XP,0);
        Stats.put(Constant.STATS_NIVEAU,1);
        item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,generateNewStatsFromTemplate(getStrTemplate(),useMax),getEffectTemplate(getStrTemplate()),Stats,new HashMap<Integer, String>(),0);
      }
      else
      {
        Map<Integer, String> Stat=new HashMap<>();
        switch(getType())
        {
          case 1:
          case 2:
          case 3:
          case 4:
          case 5:
          case 6:
          case 7:
          case 8:
            String[] splitted=getStrTemplate().split(",");
            for(String s : splitted)
            {
            	try {
              String[] stats=s.split("#");
              int statID=Integer.parseInt(stats[0],16);
              if(statID==Constant.STATS_RESIST)
              {
                String ResistanceIni=stats[1];
                Stat.put(statID,ResistanceIni);
              }
            	}
    			catch (Exception e1) {
    				Logging.getInstance().write("ITEM","erreur id item "+this.id);
    			}
            }
            break;
        }
        item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,generateNewStatsFromTemplate(getStrTemplate(),useMax),getEffectTemplate(getStrTemplate()),new HashMap<Integer, Integer>(),Stat,0);
      }
    }
    return item;
  }
  
  public GameObject createNewItem_mimi(GameObject obj, int qua)
  {
    int id=Database.getDynamics().getObjectData().getNextId();
    GameObject item;
    Map<Integer, Integer> maps=new HashMap<>();
    maps.putAll(obj.getStats().getMap());
    Stats newStats=new Stats(maps);
    Map<Integer, String> Stat=new HashMap<>();
        item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,newStats,getEffectTemplate(getStrTemplate()),new HashMap<Integer, Integer>(),Stat,0);

    return item;
  }

  public GameObject createNewItemWithoutDuplication(Collection<GameObject> objects, int qua, boolean useMax)
  {
	  if(Config.singleton.serverId == 8)
		  useMax = true;
    int id=-1;
    GameObject item;
    if(getType()==Constant.ITEM_TYPE_QUETES&&(Constant.isCertificatDopeuls(getId())||getId()==6653))
    {
      Map<Integer, String> txtStat=new HashMap<>();
      txtStat.put(Constant.STATS_DATE,System.currentTimeMillis()+"");
      item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,new Stats(false,null),new ArrayList<>(),new HashMap<>(),txtStat,0);
    }
    else if(this.getId()==10207)
    {
      item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,new Stats(false,null),new ArrayList<>(),new HashMap<>(),Dopeul.generateStatsTrousseau(),0);
    }
    //v2.8 - changed pet id assignment
    else if(getType()==Constant.ITEM_TYPE_FAMILIER)
    {
      id=Database.getDynamics().getObjectData().getNextId();
      item=new GameObject(id,getId(),1,Constant.ITEM_POS_NO_EQUIPED,(useMax ? generateNewStatsFromTemplate(Main.world.getPets(this.getId()).getJet(),false) : new Stats(false,null)),new ArrayList<>(),new HashMap<>(),Main.world.getPets(getId()).generateNewtxtStatsForPets(),0);
      //Ajouter du Pets_data SQL et World
      long time=System.currentTimeMillis();
      Main.world.addPetsEntry(new PetEntry(id,getId(),time,0,10,0,false));
      Database.getDynamics().getPetData().add(id,time,getId());
    }
    else if(getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
    {
      item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,generateNewStatsFromTemplate(getStrTemplate(),useMax),getEffectTemplate(getStrTemplate()),new HashMap<>(),new HashMap<>(),0);
    }
    else
    {
      if(getType()==Constant.ITEM_TYPE_OBJET_ELEVAGE)
      {
        item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,new Stats(false,null),new ArrayList<>(),new HashMap<>(),getStringResistance(getStrTemplate()),0);
      }
      else if(Constant.isIncarnationWeapon(getId()))
      {
        Map<Integer, Integer> Stats=new HashMap<>();
        Stats.put(Constant.ERR_STATS_XP,0);
        Stats.put(Constant.STATS_NIVEAU,1);
        item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,generateNewStatsFromTemplate(getStrTemplate(),useMax),getEffectTemplate(getStrTemplate()),Stats,new HashMap<>(),0);
      }
      else
      {
        Map<Integer, String> Stat=new HashMap<>();
        switch(getType())
        {
          case 1:
          case 2:
          case 3:
          case 4:
          case 5:
          case 6:
          case 7:
          case 8:
            String[] splitted=getStrTemplate().split(",");
            for(String s : splitted)
            {
              String[] stats=s.split("#");
              int statID=Integer.parseInt(stats[0],16);
              if(statID==Constant.STATS_RESIST)
              {
                String ResistanceIni=stats[1];
                Stat.put(statID,ResistanceIni);
              }
            }
            break;
        }
        item=new GameObject(id,getId(),qua,Constant.ITEM_POS_NO_EQUIPED,generateNewStatsFromTemplate(getStrTemplate(),useMax),getEffectTemplate(getStrTemplate()),new HashMap<Integer, Integer>(),Stat,0);
      }
    }

    for(GameObject object : objects)
      if(ConditionParser.stackIfSimilar(object,item,true))
        return object;
    return item;
  }

  private Map<Integer, String> getStringResistance(String statsTemplate)
  {
    Map<Integer, String> Stat=new HashMap<Integer, String>();
    String[] splitted=statsTemplate.split(",");

    for(String s : splitted)
    {
      String[] stats=s.split("#");
      int statID=Integer.parseInt(stats[0],16);
      String ResistanceIni=stats[1];
      Stat.put(statID,ResistanceIni);
    }
    return Stat;
  }

  public Stats generateNewStatsFromTemplate(String statsTemplate, boolean useMax)
  {
    Stats itemStats=new Stats(false,null);
    //Si stats Vides
    if(statsTemplate.equals("")||statsTemplate==null)
      return itemStats;

    String[] splitted=statsTemplate.split(",");
    for(String s : splitted)
    {
      String[] stats=s.split("#");
      int statID=Integer.parseInt(stats[0],16);
      boolean follow=true;

      for(int a : Constant.ARMES_EFFECT_IDS)
        //Si c'est un Effet Actif
        if(a==statID)
          follow=false;
      if(!follow)//Si c'était un effet Actif d'arme
        continue;
      if(statID==Constant.STATS_RESIST)
        continue;
      boolean isStatsInvalid=false;
      switch(statID)
      {
        case 110:
        case 139:
        case 605:
        case 614:
          isStatsInvalid=true;
          break;
        case 615:
          itemStats.addOneStat(statID,Integer.parseInt(stats[3],16));
          break;
      }
      if(isStatsInvalid)
        continue;
      String jet="";
      int value=1;
      if(this.getType()!=83&&stats.length>=5) //v2.8 - stat parse fix
      {
        try
        {
          jet=stats[4];
          value=Formulas.getRandomJet(jet);
          if(useMax)
          {
            try
            {
              //on prend le jet max
              int min=Integer.parseInt(stats[1],16);
              int max=Integer.parseInt(stats[2],16);
              value=min;
              if(max!=0)
                value=max;
            }
            catch(Exception e)
            {
              e.printStackTrace();
              value=Formulas.getRandomJet(jet);
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
      itemStats.addOneStat(statID,value);
    }
    return itemStats;
  }

  private ArrayList<SpellEffect> getEffectTemplate(String statsTemplate)
  {
    ArrayList<SpellEffect> Effets=new ArrayList<SpellEffect>();
    if(statsTemplate.equals(""))
      return Effets;

    String[] splitted=statsTemplate.split(",");
    for(String s : splitted)
    {
      String[] stats=s.split("#");
      int statID=Integer.parseInt(stats[0],16);
      for(int a : Constant.ARMES_EFFECT_IDS)
      {
        if(a==statID)
        {
          int id=statID;
          String min=stats[1];
          String max=stats[2];
          String jet=stats[4];
          String args=min+";"+max+";-1;-1;0;"+jet;
          Effets.add(new SpellEffect(id,args,0,-1));
        }
      }
      switch(statID)
      {
        case 110:
        case 139:
        case 605:
        case 614:
          String min=stats[1];
          String max=stats[2];
          String jet=stats[4];
          String args=min+";"+max+";-1;-1;0;"+jet;
          Effets.add(new SpellEffect(statID,args,0,-1));
          break;
      }
    }
    return Effets;
  }

  public String parseItemTemplateStats()
  {
    return getId()+";"+getStrTemplate()+(this.newPrice>0 ? ";"+this.newPrice : "");
  }

  public void applyAction(Player player, Player target, int objectId, short cellId)
  {
    if(World.getGameObject(objectId)==null)
      return;
    if(World.getGameObject(objectId).getTemplate().getType()==85 && World.getGameObject(objectId).getTemplate().getId() == 7010)
    {
      if(!Capture.isInArenaMap(player.getCurMap().getId()))
        return;
      if(Config.getInstance().fightAsBlocked)
		  return;
      if(player.getFight()!=null)
          return;
     /* for(MobGroup group : player.getCurMap().getMobGroups().values())
      {
       if(group.getCellId() == player.getCurCell().getId()) {
      	 player.sendMessage("Casse Non libre");
          return;
       }
      }*/
      int numbres = 0;
      for(MobGroup group : player.getCurMap().getMobGroups().values())
      {
    	  if(group.getIp() == null)
          	continue;
       if(group.getIp().compareTo(player.getAccount().getCurrentIp()) == 0) {
      	numbres++;
      	if(numbres >= 9)
      	{
      	player.sendMessage("9 Captures par ip maximum");
          return;
      	}
       }
      }
      Capture capture=(Capture) World.getGameObject(objectId);

      player.getCurMap().spawnNewGroup(true,player.getCurCell().getId(),capture.parseGroupData(),"MiS="+player.getId(),player.getId(),player.getAccount().getCurrentIp());
      SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+World.getGameObject(objectId).getTemplate().getId());
      player.removeItem(objectId,1,true,true);
    }
    else
    {
      for(ObjectAction action : this.getOnUseActions())
        action.apply(player,target,objectId,cellId);
    }
  }

  public void newSold(int amount, int price)
  {
    long oldSold=getSold();
    sold+=amount;
    avgPrice=(int)((getAvgPrice()*oldSold+price)/getSold());
  }

}