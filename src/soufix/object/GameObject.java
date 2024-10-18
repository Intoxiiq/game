package soufix.object;

import soufix.client.Player;
import soufix.client.other.Stats;
import soufix.common.Formulas;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.mount.Mount;
import soufix.entity.pet.PetEntry;
import soufix.fight.spells.SpellEffect;
import soufix.job.JobAction;
import soufix.job.fm.Rune;
import soufix.main.Constant;
import soufix.main.Logging;
import soufix.main.Main;
import soufix.object.entity.Fragment;
import soufix.utility.Pair;
import soufix.utility.TimerWaiterPlus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class GameObject
{
  protected ObjectTemplate template;
  protected int quantity=1;
  protected int position=Constant.ITEM_POS_NO_EQUIPED;
  protected int guid;
  protected int obvijevanPos;
  protected int obvijevanLook;
  protected float puit;
  protected int anti_hack=1;
  private Stats Stats=new Stats();
  private ArrayList<SpellEffect> Effects=new ArrayList<>();
  private ArrayList<String> SortStats=new ArrayList<>();
  private Map<Integer, String> txtStats=new HashMap<>();
  private Map<Integer, Integer> SoulStats=new HashMap<>();

  public byte modification=-1;

  public GameObject(int Guid, int template, int qua, int pos, String strStats, float puit)
  {
    this.guid=Guid;
    this.template=Main.world.getObjTemplate(template);
    this.quantity=qua;
    this.position=pos;
    this.puit=puit;

    Stats=new Stats();
    this.parseStringToStats(strStats,false);
   // restat();
  }

 /* public void restat () {
	    boolean modif = false;
	    if((this.getTemplate().getType() >= 1 && this.getTemplate().getType() <= 11) || (this.getTemplate().getType() >= 16 && this.getTemplate().getType() <= 23)
				|| (this.getTemplate().getType() >= 81 && this.getTemplate().getType() <= 82) || this.getTemplate().getType() == 85
				|| this.getTemplate().getType() == 113)
	    for(Entry<Integer, Integer> entry : Stats.getMap().entrySet())
	    {

	      String statID=Integer.toHexString(entry.getKey());
	      final int currentStat=Job.getActualJet(this,statID);
	      final int statMax=JobAction.getStatBaseMaxs(this.template,statID);
	      if(currentStat > statMax) {
	    	  modif = true;
	    	  System.out.println(currentStat);
	    	  System.out.println(statMax);
	      }
	    }
	    if(modif) {
	    	System.out.println(this.guid);
	    this.clearStats();
	    Stats = this.template.generateNewStatsFromTemplate(this.template.getStrTemplate(),false);
	    Database.getStatics().getObjectData().update(this);
	    }
  }*/
  public GameObject(int Guid)
  {
    this.guid=Guid;
    this.template=Main.world.getObjTemplate(8378);
    this.quantity=1;
    this.position=-1;
    this.puit=0;
  }

  public GameObject(int Guid, int template, int qua, int pos, Stats stats, ArrayList<SpellEffect> effects, Map<Integer, Integer> _SoulStat, Map<Integer, String> _txtStats, float puit)
  {
    this.guid=Guid;
    this.template=Main.world.getObjTemplate(template);
    this.quantity=qua;
    this.position=pos;
    this.Stats=stats;
    this.Effects=effects;
    this.SoulStats=_SoulStat;
    this.txtStats=_txtStats;
    this.obvijevanPos=0;
    this.obvijevanLook=0;
    this.puit=puit;
    //restat();
  }

  public static GameObject getCloneObjet(GameObject obj, int qua)
  {
    Map<Integer, Integer> maps=new HashMap<>();
    maps.putAll(obj.getStats().getMap());
    Stats newStats=new Stats(maps);

    GameObject ob=new GameObject(Database.getDynamics().getObjectData().getNextId(),obj.getTemplate().getId(),qua,Constant.ITEM_POS_NO_EQUIPED,newStats,obj.getEffects(),obj.getSoulStat(),obj.getTxtStat(),obj.getPuit());
    ob.modification=0;
    return ob;
  }

  public int setId()
  {
    this.guid=Database.getDynamics().getObjectData().getNextId();
    return this.getGuid();
  }

  public float getPuit()
  {
    return this.puit;
  }

  public void setPuit(float puit)
  {
    this.puit=puit;
  }

  public int getObvijevanPos()
  {
    return obvijevanPos;
  }

  public void setObvijevanPos(int pos)
  {
    obvijevanPos=pos;
    this.setModification();
  }

  public int getObvijevanLook()
  {
    return obvijevanLook;
  }

  public void setObvijevanLook(int look)
  {
    obvijevanLook=look;
    this.setModification();
  }

  public void setModification()
  {
    if(this.modification==-1)
      this.modification=1;
  }

  public void parseStringToStats(String strStats, boolean save)
  {
    if(this.template!=null&&this.template.getId()==7010)
      return;
    String dj1="";
    if(!strStats.equalsIgnoreCase(""))
    {
      for(String split : strStats.split(","))
      {
        try
        {
          if(split.equalsIgnoreCase(""))
            continue;
          if(split.substring(0,3).equalsIgnoreCase("325")&&(this.getTemplate().getId()==10207||this.getTemplate().getId()==10601))
          {
            txtStats.put(Constant.STATS_DATE,split.substring(3)+"");
            continue;
          }
          if(split.substring(0,3).equalsIgnoreCase("3dc"))
          {// Si c'est une rune de signature crée
            txtStats.put(Constant.STATS_SIGNATURE,split.split("#")[4]);
            continue;
          }
          if(split.substring(0,3).equalsIgnoreCase("3cf"))
          {// Si c'est une rune de signature crée
            txtStats.put(Constant.STATS_MIMI_NOM,split.split("#")[4]);
            continue;
          }
          if(split.substring(0,3).equalsIgnoreCase("3d9"))
          {// Si c'est une rune de signature modifié
            txtStats.put(Constant.STATS_CHANGE_BY,split.split("#")[4]);
            continue;
          }

          String[] stats=split.split("#");
          int id=Integer.parseInt(stats[0],16);

          if(id>=281&&id<=294)
          {
            this.getSpellStats().add(split);
            continue;
          }
          if(id==Constant.STATS_PETS_DATE&&this.getTemplate().getType()==Constant.ITEM_TYPE_CERTIFICAT_CHANIL)
          {
            txtStats.put(id,split.substring(3));
            continue;
          }
          if(id==Constant.STATS_CHANGE_BY||id==Constant.STATS_NAME_TRAQUE||id==Constant.STATS_OWNER_1)
          {
            txtStats.put(id,stats[4]);
            continue;
          }
          if(id==Constant.STATS_GRADE_TRAQUE||id==Constant.STATS_ALIGNEMENT_TRAQUE||id==Constant.STATS_NIVEAU_TRAQUE)
          {
            txtStats.put(id,stats[3]);
            continue;
          }
          if(id==Constant.STATS_PETS_SOUL)
          {
            SoulStats.put(Integer.parseInt(stats[1],16),Integer.parseInt(stats[3],16)); // put(id_monstre, nombre_tué)
            continue;
          }
          if(id==Constant.STATS_NAME_DJ)
          {
            dj1+=(!dj1.isEmpty() ? "," : "")+stats[3];
            txtStats.put(Constant.STATS_NAME_DJ,dj1);
            continue;
          }
          if(id==997||id==996)
          {
            txtStats.put(id,stats[4]);
            continue;
          }
          if(this.template!=null&&this.template.getId()==77&&id==Constant.STATS_PETS_DATE)
          {
            txtStats.put(id,split.substring(3));
            continue;
          }
          if(id==Constant.STATS_DATE || id==Constant.STATS_MIMI_ID )
          {
            txtStats.put(id,stats[3]);
            continue;
          }
          //Si stats avec Texte (Signature, apartenance, etc)//FIXME
          if(id!=Constant.STATS_RESIST&&(!stats[3].equals("")&&(!stats[3].equals("0")||id==Constant.STATS_PETS_DATE||id==Constant.STATS_PETS_PDV||id==Constant.STATS_PETS_POIDS||id==Constant.STATS_PETS_EPO||id==Constant.STATS_PETS_REPAS)))
          {//Si le stats n'est pas vide et (n'est pas égale é 0 ou est de type familier)
            if(!(this.getTemplate().getType()==Constant.ITEM_TYPE_CERTIFICAT_CHANIL&&id==Constant.STATS_PETS_DATE))
            {
              txtStats.put(id,stats[3]);
              continue;
            }
          }
          if(id==Constant.STATS_RESIST&&this.getTemplate()!=null&&this.getTemplate().getType()==93)
          {
            txtStats.put(id,stats[4]);
            continue;
          }
          if(id==Constant.STATS_RESIST)
          {
            txtStats.put(id,stats[4]);
            continue;
          }

          boolean follow1=true;
          switch(id)
          {
            case 110:
            case 139:
            case 605:
            case 614:
              String min=stats[1];
              String max=stats[2];
              String jet=stats[4];
              String args=min+";"+max+";-1;-1;0;"+jet;
              Effects.add(new SpellEffect(id,args,0,-1));
              follow1=false;
              break;
          }
          if(!follow1)
          {
            continue;
          }

          boolean follow2=true;
          for(int a : Constant.ARMES_EFFECT_IDS)
          {
            if(a==id)
            {
              Effects.add(new SpellEffect(id,stats[1]+";"+stats[2]+";-1;-1;0;"+stats[4],0,-1));
              follow2=false;
            }
          }
          if(!follow2)
            continue;//Si c'était un effet Actif d'arme ou une signature
            
          if(id == 995)
          Stats.addOneStat(id,Integer.parseInt(stats[4].substring(4)));
          else
          Stats.addOneStat(id,Integer.parseInt(stats[1],16));
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
    }
    if(save)
      this.setModification();
  }
  public boolean containtTxtStats(int id){
		return txtStats.containsKey(id);
	}
  public void addTxtStat(int i, String s)
  {

    txtStats.put(i,s);
    this.setModification();
  }

  public String getTraquedName()
  {
    for(Entry<Integer, String> entry : txtStats.entrySet())
    {
      if(Integer.toHexString(entry.getKey()).compareTo("3dd")==0)
      {
        return entry.getValue();
      }
    }
    return null;
  }

  public Stats getStats()
  {
    return Stats;
  }

  public void setStats(Stats SS)
  {
    Stats=SS;
    this.setModification();
  }

  public int getQuantity()
  {
    return quantity;
  }

  public void setQuantity(int quantity, Player player)
  {
    if(quantity<=0)
      quantity=0;
    
   /* else if(quantity>=100000) {
      if(Logging.USE_LOG)
        Logging.getInstance().write("Object","Faille : Objet guid : "+guid+" a depasse 100 000 qua ("+quantity+") avec comme template : "+template.getName()+" ("+template.getId()+")");
      bot.Send_text("Faille : Objet guid : "+guid+" a depasse 100 000 qua ("+quantity+") avec comme template : "+template.getName()+" ("+template.getId()+")");
      if(player!=null)
      bot.Send_text("Perso "+player.getName());
    }*/
    if(quantity>=200) {
    	if(this.template.boutique == 1) {
    		if(player != null) {
    	    player.getAccount().setBanned(true);
              //Database.getStatics().getAccountData().update(player.getAccount()); 
    	        SocketManager.send(player,"Im1201;AntiHack");
    	        TimerWaiterPlus.addNext(() -> {
    	        	if(player.getGameClient()!= null)
    	        	player.getGameClient().kick(); 	
   			 }, 2000);
    		}
    	        if(Logging.USE_LOG)
    	          Logging.getInstance().write("Object","Faille : Objet boutique guid : "+guid+" a depasse 200 qua ("+quantity+") avec comme template : "+template.getName()+" ("+template.getId()+")");
    	       
    	}
      }
    if(quantity>=100) {
    	if(this.template.getType() == 23) {
    		if(player != null) {
    	    player.getAccount().setBanned(true);
              //Database.getStatics().getAccountData().update(player.getAccount()); 
    	        SocketManager.send(player,"Im1201;AntiHack");
    	        TimerWaiterPlus.addNext(() -> {
    	        	if(player.getGameClient()!= null)
    	        	player.getGameClient().kick(); 	
   			 },2000); 
    		}
    	        if(Logging.USE_LOG)
    	          Logging.getInstance().write("Object","Faille : Objet dofus guid : "+guid+" a depasse 100 qua ("+quantity+") avec comme template : "+template.getName()+" ("+template.getId()+")");
    	        
    	}
      }
    if(quantity>=30) {
    	if(this.template.getId() == 25540 || this.template.getId() == 25541 || this.template.getId() == 25542 ||
    			this.template.getId() == 25543) {
    		if(player != null) {
    	    player.getAccount().setBanned(true);
              //Database.getStatics().getAccountData().update(player.getAccount()); 
    	        SocketManager.send(player,"Im1201;AntiHack");
    	        TimerWaiterPlus.addNext(() -> {
    	        	if(player.getGameClient()!= null)
    	        	player.getGameClient().kick(); 	
   			 },2000);
    		}
    	        if(Logging.USE_LOG)
    	          Logging.getInstance().write("Object","Faille : Objet Parcho boutique guid : "+guid+" a depasse 30 qua ("+quantity+") avec comme template : "+template.getName()+" ("+template.getId()+")");
    	      
    	}
      }
    if(quantity != 0)
    if(this.quantity*2 == quantity) {
 	   this.anti_hack+=1;
 	  }else {
 	  this.anti_hack = 0;	  
 	  }
    if(this.anti_hack >= 5) {
    	 if(Logging.USE_LOG)
	          Logging.getInstance().write("Object","Duplication a tcheck  : Objet  guid : "+guid+" ("+quantity+") avec comme template : "+template.getName()+" ("+template.getId()+")");
	       
	        this.anti_hack = 0;
    }
    
    
    this.quantity=quantity;
    this.setModification();
  }

  public int getPosition()
  {
    return position;
  }

  public void setPosition(int position)
  {
    this.setModification();
    this.position=position;
  }

  public ObjectTemplate getTemplate()
  {
    return template;
  }

  public void setTemplate(int Tid)
  {
    this.setModification();
    this.template=Main.world.getObjTemplate(Tid);
  }

  public int getGuid()
  {
    return guid;
  }

  public Map<Integer, Integer> getSoulStat()
  {
    return SoulStats;
  }

  public Map<Integer, String> getTxtStat()
  {
    return txtStats;
  }

  public void setExchangeIn(Player player)
  {

    this.setModification();
  }

  public void setMountStats(Player player, Mount mount)
  {
    if(mount==null)
      mount=new Mount(Constant.getMountColorByParchoTemplate(this.getTemplate().getId()),player.getId(),false);
    this.clearStats();
    this.getStats().addOneStat(995,-(mount.getId()));
    this.getTxtStat().put(996,player.getName());
    this.getTxtStat().put(997,mount.getName());
    this.setModification();
  }

  public void attachToPlayer(Player player)
  {
    this.getTxtStat().put(Constant.STATS_OWNER_1,player.getName());
    SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player,this);
    this.setModification();
  }

  public boolean isAttach()
  {
    boolean ok=this.getTxtStat().containsKey(Constant.STATS_OWNER_1);

    if(ok)
    {
      Player player=Main.world.getPlayerByName(this.getTxtStat().get(Constant.STATS_OWNER_1));
      if(player!=null)
        player.send("BN");
    }

    return ok;
  }

  public String parseItem()
  {
    String posi=position==Constant.ITEM_POS_NO_EQUIPED ? "" : Integer.toHexString(position);
    if(this.template == null)
    return "";
    return Integer.toHexString(guid)+"~"+Integer.toHexString(template.getId())+"~"+Integer.toHexString(quantity)+"~"+posi+"~"+parseStatsString()+";";
  }
  public String parseItemWINDOW()
  {
    String posi="";
    return Integer.toHexString(guid)+"~"+Integer.toHexString(template.getId())+"~"+Integer.toHexString(quantity)+"~"+posi+"~"+parseStatsString()+";";
  }

  public String parseStatsString()
  {
    if(getTemplate().getType()==83) //Si c'est une pierre d'éme vide
      return getTemplate().getStrTemplate();
    StringBuilder stats=new StringBuilder();
    boolean isFirst=true;

    if((getTemplate().getPanoId()>=81&&getTemplate().getPanoId()<=92)||(getTemplate().getPanoId()>=201&&getTemplate().getPanoId()<=212))
    {
      for(String spell : SortStats)
      {
        if(!isFirst)
        {
          stats.append(",");
        }
        String[] sort=spell.split("#");
        int spellid=Integer.parseInt(sort[1],16);
        stats.append(sort[0]).append("#").append(sort[1]).append("#0#").append(sort[3]).append("#").append(Main.world.getSort(spellid));
        isFirst=false;
      }
    }
    for(SpellEffect SE : Effects)
    {
      if(!isFirst)
        stats.append(",");
      String[] infos=SE.getArgs().split(";");

      try
      {
        switch(SE.getEffectID())
        {
          case 614:
            stats.append(Integer.toHexString(SE.getEffectID())).append("#0#0#").append(infos[0]).append("#").append(infos[5]);
            break;

          default:
            stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#").append(infos[1]).append("#").append(infos[5]);
            break;
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
        continue;
      }
      isFirst=false;
    }

    for(Entry<Integer, String> entry : txtStats.entrySet())
    {
      if(!isFirst)
        stats.append(",");
      if(template.getType()==77||template.getType()==90)
      {
        if(entry.getKey()==Constant.STATS_PETS_PDV)
          stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
        if(entry.getKey()==Constant.STATS_PETS_EPO)
          stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
        if(entry.getKey()==Constant.STATS_PETS_REPAS)
          stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
        if(entry.getKey()==Constant.STATS_PETS_POIDS)
        {
          int corpu=0;
          int corpulence=0;
          String c=entry.getValue();
          if(c!=null&&!c.equalsIgnoreCase(""))
          {
            try
            {
              corpulence=Integer.parseInt(c);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
          }
          if(corpulence>0||corpulence<0)
            corpu=7;
          stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(corpu)).append("#").append(corpulence>0 ? corpu : 0).append("#").append(Integer.toHexString(corpu));
        }
        if(entry.getKey()==Constant.STATS_PETS_DATE&&template.getType()==77)
        {
          if(entry.getValue().contains("#"))
            stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
          else
            stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
        }
      }
      else if(entry.getKey()==Constant.STATS_CHANGE_BY||entry.getKey()==Constant.STATS_NAME_TRAQUE||entry.getKey()==Constant.STATS_OWNER_1)
      {
        stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
      }
      else if(entry.getKey()==Constant.STATS_GRADE_TRAQUE||entry.getKey()==Constant.STATS_ALIGNEMENT_TRAQUE||entry.getKey()==Constant.STATS_NIVEAU_TRAQUE)
      {
        stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue()).append("#0");
      }
      else if(entry.getKey()==Constant.STATS_NAME_DJ)
      {
        if(entry.getValue().equals("0d0+0"))
          continue;
        for(String i : entry.getValue().split(","))
        {
          stats.append(",").append(Integer.toHexString(entry.getKey())).append("#0#0#").append(i);
        }
        continue;
      }
      else if(entry.getKey()==Constant.STATS_DATE || entry.getKey()==Constant.STATS_MIMI_ID )
      {
        String item=entry.getValue();
        if(item.contains("#"))
        {
          String date=item.split("#")[3];
          if(date!=null&&!date.equalsIgnoreCase(""))
            stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(date)));
        }
        else
          stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(item)));
      }
      else if(entry.getKey()==Constant.CAPTURE_MONSTRE)
      {
        stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
      }
      else if(entry.getKey()==Constant.STATS_PETS_PDV||entry.getKey()==Constant.STATS_PETS_POIDS||entry.getKey()==Constant.STATS_PETS_DATE||entry.getKey()==Constant.STATS_PETS_REPAS)
      {
        PetEntry p=Main.world.getPetsEntry(this.getGuid());
        if(p==null)
        {
          if(entry.getKey()==Constant.STATS_PETS_PDV)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append("a").append("#0#a");
          if(entry.getKey()==Constant.STATS_PETS_POIDS)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
          if(entry.getKey()==Constant.STATS_PETS_DATE)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
          if(entry.getKey()==Constant.STATS_PETS_REPAS)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
        }
        else
        {
          if(entry.getKey()==Constant.STATS_PETS_PDV)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getPdv())).append("#0#").append(Integer.toHexString(p.getPdv()));
          if(entry.getKey()==Constant.STATS_PETS_POIDS)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toString(p.parseCorpulence())).append("#").append(p.getCorpulence()>0 ? p.parseCorpulence() : 0).append("#").append(Integer.toString(p.parseCorpulence()));
          if(entry.getKey()==Constant.STATS_PETS_DATE)
            stats.append(Integer.toHexString(entry.getKey())).append(p.parseLastEatDate());
          if(entry.getKey()==Constant.STATS_PETS_REPAS)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
          if(p.getIsEupeoh()&&entry.getKey()==Constant.STATS_PETS_EPO)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0)).append("#0#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0));
        }
      }
      else if(entry.getKey()==Constant.STATS_RESIST&&getTemplate().getType()==93)
      {
        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate()))).append("#").append(entry.getValue()).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate())));
      }
      else if(entry.getKey()==Constant.STATS_RESIST)
      {
        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate()))).append("#").append(entry.getValue()).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate())));
      }
      else
      {
        stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
      }
      isFirst=false;
    }

    for(Entry<Integer, Integer> entry : SoulStats.entrySet())
    {
      if(!isFirst)
        stats.append(",");

      if(this.getTemplate().getType()==18)
        stats.append(Integer.toHexString(Constant.STATS_PETS_SOUL)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
      if(entry.getKey()==Constant.STATS_NIVEAU)
        stats.append(Integer.toHexString(Constant.STATS_NIVEAU)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
      isFirst=false;
    }

    for(Entry<Integer, Integer> entry : Stats.getMap().entrySet())
    {

      int statID=entry.getKey();
      if((getTemplate().getPanoId()>=81&&getTemplate().getPanoId()<=92)||(getTemplate().getPanoId()>=201&&getTemplate().getPanoId()<=212))
      {
        String[] modificable=template.getStrTemplate().split(",");
        int cantMod=modificable.length;
        for(int j=0;j<cantMod;j++)
        {
          String[] mod=modificable[j].split("#");
          if(Integer.parseInt(mod[0],16)==statID)
          {
            String jet="0d0+"+Integer.parseInt(mod[1],16);
            if(!isFirst)
              stats.append(",");
            stats.append(mod[0]).append("#").append(mod[1]).append("#0#").append(mod[3]).append("#").append(jet);
            isFirst=false;
          }
        }
        continue;
      }

      if(!isFirst)
        stats.append(",");
      if(statID==615)
      {
        stats.append(Integer.toHexString(statID)).append("#0#0#").append(Integer.toHexString(entry.getValue()));
      }
      else if((statID==970)||(statID==971)||(statID==972)||(statID==973)||(statID==974))
      {
        int jet=entry.getValue().intValue();
        if((statID==974)||(statID==972)||(statID==970))
          stats.append(Integer.toHexString(statID)).append("#0#0#").append(Integer.toHexString(jet));
        else
          stats.append(Integer.toHexString(statID)).append("#0#0#").append(jet);
        if(statID==973)
          setObvijevanPos(jet);
        if(statID==972)
          setObvijevanLook(jet);
      }
      else if(statID==Constant.STATS_TURN)
      {
        String jet="0d0+"+entry.getValue();
        stats.append(Integer.toHexString(statID)).append("#");
        stats.append("0#0#").append(Integer.toHexString(entry.getValue())).append("#").append(jet);
      }
      else if(statID==995)
      {
        stats.append(Integer.toHexString(statID)).append("#");
        stats.append(Integer.toHexString(Integer.parseInt(entry.getValue().toString().replace("-", ""))));
        stats.append("#0#").append(Integer.toHexString(entry.getValue())).append("#").append(entry.getValue());
      }
      else
      {
        String jet="0d0+"+entry.getValue();
        stats.append(Integer.toHexString(statID)).append("#");
        stats.append(Integer.toHexString(entry.getValue().intValue())).append("#0#0#").append(jet);
      }
      isFirst=false;
    }
    return stats.toString();
  }

  public String parseStatsStringSansUserObvi()
  {
    if(getTemplate().getType()==83) //Si c'est une pierre d'éme vide
      return getTemplate().getStrTemplate();

    StringBuilder stats=new StringBuilder();
    boolean isFirst=true;

    if(this instanceof Fragment)
    {
      Fragment fragment=(Fragment)this;
      for(Pair<Integer, Integer> couple : fragment.getRunes())
      {
        stats.append((stats.toString().isEmpty() ? couple.getLeft() : ";"+couple.getLeft())).append(":").append(couple.getRight());
      }
      return stats.toString();
    }
    for(Entry<Integer, String> entry : txtStats.entrySet())
    {
      if(!isFirst)
        stats.append(",");
      if(template.getType()==77)
      {
        if(entry.getKey()==Constant.STATS_PETS_PDV)
          stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
        if(entry.getKey()==Constant.STATS_PETS_POIDS)
          stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#").append(entry.getValue()).append("#").append(entry.getValue());
        if(entry.getKey()==Constant.STATS_PETS_DATE)
        {
          if(entry.getValue().contains("#"))
            stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
          else
            stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
        }
        //stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
      }
      else if(entry.getKey()==Constant.STATS_DATE || entry.getKey()==Constant.STATS_MIMI_ID)
      {
        if(entry.getValue().contains("#"))
          stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
        else
          stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(Long.parseLong(entry.getValue()));
      }
      else if(entry.getKey()==Constant.STATS_CHANGE_BY||entry.getKey()==Constant.STATS_NAME_TRAQUE||entry.getKey()==Constant.STATS_OWNER_1)
      {
        stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
      }
      else if(entry.getKey()==Constant.STATS_GRADE_TRAQUE||entry.getKey()==Constant.STATS_ALIGNEMENT_TRAQUE||entry.getKey()==Constant.STATS_NIVEAU_TRAQUE)
      {
        stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue()).append("#0");
      }
      else if(entry.getKey()==Constant.STATS_NAME_DJ)
      {
        for(String i : entry.getValue().split(","))
          stats.append(",").append(Integer.toHexString(entry.getKey())).append("#0#0#").append(i);
      }
      else if(entry.getKey()==Constant.CAPTURE_MONSTRE)
      {
        stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
      }
      else if(entry.getKey()==Constant.STATS_PETS_PDV||entry.getKey()==Constant.STATS_PETS_POIDS||entry.getKey()==Constant.STATS_PETS_DATE)
      {
        PetEntry p=Main.world.getPetsEntry(this.getGuid());
        if(p==null)
        {
          if(entry.getKey()==Constant.STATS_PETS_PDV)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append("a").append("#0#a");
          if(entry.getKey()==Constant.STATS_PETS_POIDS)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
          if(entry.getKey()==Constant.STATS_PETS_DATE)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
        }
        else
        {
          if(entry.getKey()==Constant.STATS_PETS_PDV)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getPdv())).append("#0#").append(Integer.toHexString(p.getPdv()));
          if(entry.getKey()==Constant.STATS_PETS_POIDS)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toString(p.parseCorpulence())).append("#").append(p.getCorpulence()>0 ? p.parseCorpulence() : 0).append("#").append(Integer.toString(p.parseCorpulence()));
          if(entry.getKey()==Constant.STATS_PETS_DATE)
            stats.append(Integer.toHexString(entry.getKey())).append(p.parseLastEatDate());
          if(p.getIsEupeoh()&&entry.getKey()==Constant.STATS_PETS_EPO)
            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0)).append("#0#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0));
        }
      }
      else
      {
        stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
      }
      isFirst=false;
    }
    /*
     * if(isCertif && !db) return stats.toString();
     */

    for(SpellEffect SE : Effects)
    {
      if(!isFirst)
        stats.append(",");

      String[] infos=SE.getArgs().split(";");
      try
      {
        stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#0#").append(infos[5]);
      }
      catch(Exception e)
      {
        e.printStackTrace();
        continue;
      }
      isFirst=false;
    }
    for(Entry<Integer, Integer> entry : SoulStats.entrySet())
    {
      if(!isFirst)
        stats.append(",");
      stats.append(Integer.toHexString(Constant.STATS_PETS_SOUL)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
      isFirst=false;
    }
    for(Entry<Integer, Integer> entry : Stats.getMap().entrySet())
    {
      if(!isFirst)
        stats.append(",");

      if(entry.getKey()==615)
      {
        stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(Integer.toHexString(entry.getValue()));
      }
      else
      {
        String jet="0d0+"+entry.getValue();
        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue()));
        stats.append("#0#0#").append(jet);
      }
      isFirst=false;
    }
    return stats.toString();
  }

  public String parseToSave()
  {
    return parseStatsStringSansUserObvi();
  }
// modif code bouclier objetvivant by dark
  public String obvijevanOCO_Packet(int pos)
  {
    String strPos=String.valueOf(Integer.toHexString(pos));
    if(pos==-1)
      strPos="";
    String upPacket="OCO";
    upPacket=upPacket+Integer.toHexString(getGuid())+"~";
    upPacket=upPacket+Integer.toHexString(getTemplate().getId())+"~";
    upPacket=upPacket+Integer.toHexString(getQuantity())+"~";
    upPacket=upPacket+strPos+"~";
    upPacket=upPacket+parseStatsString();
    this.setModification();
    return upPacket;
  }

  public void obvijevanNourir(GameObject obj)
  {
    if(obj==null)
      return;
    for(Entry<Integer, Integer> entry : Stats.getMap().entrySet())
    {
      if(entry.getKey().intValue()!=974) // on ne boost que la stat de l'expérience de l'obvi
        continue;
      if(entry.getValue().intValue()>500) // si le boost a une valeur supérieure é 500 (irréaliste)
        return;
      entry.setValue(Integer.valueOf(entry.getValue().intValue()+obj.getTemplate().getLevel()/3));
    }
    this.setModification();
  }

  public void obvijevanChangeStat(int statID, int val)
  {
    for(Entry<Integer, Integer> entry : Stats.getMap().entrySet())
    {
      if(entry.getKey().intValue()!=statID)
        continue;
      entry.setValue(Integer.valueOf(val));
    }
    this.setModification();
  }

  public void removeAllObvijevanStats()
  {
    setObvijevanPos(0);
    soufix.client.other.Stats StatsSansObvi=new Stats();
    for(Entry<Integer, Integer> entry : Stats.getMap().entrySet())
    {
      int statID=entry.getKey().intValue();
      if((statID==970)||(statID==971)||(statID==972)||(statID==973)||(statID==974))
        continue;
      StatsSansObvi.addOneStat(statID,entry.getValue().intValue());
    }
    Stats=StatsSansObvi;
    this.setModification();
  }

  public void removeAll_ExepteObvijevanStats()
  {
    setObvijevanPos(0);
    soufix.client.other.Stats StatsSansObvi=new Stats();
    for(Entry<Integer, Integer> entry : Stats.getMap().entrySet())
    {
      int statID=entry.getKey().intValue();
      if((statID!=971)&&(statID!=972)&&(statID!=973)&&(statID!=974))
        continue;
      StatsSansObvi.addOneStat(statID,entry.getValue().intValue());
    }
    Stats=StatsSansObvi;
    this.setModification();
  }

  public String getObvijevanStatsOnly()
  {
    GameObject obj=getCloneObjet(this,1);
    obj.removeAll_ExepteObvijevanStats();
    this.setModification();
    return obj.parseStatsStringSansUserObvi();
  }

  /* *********FM SYSTEM********* */

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
      if(!follow)
        continue;//Si c'était un effet Actif d'arme

      String jet="";
      int value=1;
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
      itemStats.addOneStat(statID,value);
    }
    return itemStats;
  }

  public ArrayList<SpellEffect> getEffects()
  {
    return Effects;
  }

  public ArrayList<SpellEffect> getCritEffects()
  {
    ArrayList<SpellEffect> effets=new ArrayList<SpellEffect>();
    for(SpellEffect SE : Effects)
    {
      try
      {
        boolean boost=true;
        for(int i : Constant.NO_BOOST_CC_IDS)
          if(i==SE.getEffectID())
            boost=false;
        String[] infos=SE.getArgs().split(";");
        if(!boost)
        {
          effets.add(SE);
          continue;
        }
        int min=Integer.parseInt(infos[0],16)+(boost ? template.getBonusCC() : 0);
        int max=Integer.parseInt(infos[1],16)+(boost ? template.getBonusCC() : 0);
        String jet="1d"+(max-min+1)+"+"+(min-1);
        //exCode: String newArgs = Integer.toHexString(min)+";"+Integer.toHexString(max)+";-1;-1;0;"+jet;
        //osef du minMax, vu qu'on se sert du jet pour calculer les dégats
        String newArgs="0;0;0;-1;0;"+jet;
        effets.add(new SpellEffect(SE.getEffectID(),newArgs,0,-1));
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    return effets;
  }

  public void clearStats()
  {
    Stats=new Stats();
    Effects.clear();
    txtStats.clear();
    SortStats.clear();
    SoulStats.clear();
    this.setModification();
  }

  public void refreshStatsObjet(String newsStats)
  {
    parseStringToStats(newsStats,true);
    this.setModification();
  }

  public int getResistance(String statsTemplate)
  {
    int Resistance=0;

    String[] splitted=statsTemplate.split(",");
    for(String s : splitted)
    {
      String[] stats=s.split("#");
      if(Integer.parseInt(stats[0],16)==Constant.STATS_RESIST)
      {
        Resistance=Integer.parseInt(stats[2],16);
      }
    }
    return Resistance;
  }

  public int getResistanceMax(String statsTemplate)
  {
    int ResistanceMax=0;

    String[] splitted=statsTemplate.split(",");
    for(String s : splitted)
    {
      String[] stats=s.split("#");
      if(Integer.parseInt(stats[0],16)==Constant.STATS_RESIST)
      {
        ResistanceMax=Integer.parseInt(stats[1],16);
      }
    }
    return ResistanceMax;
  }

  public int getRandomValue(String statsTemplate, int statsId)
  {
    if(statsTemplate.equals(""))
      return 0;

    String[] splitted=statsTemplate.split(",");
    int value=0;
    for(String s : splitted)
    {
      String[] stats=s.split("#");
      int statID=Integer.parseInt(stats[0],16);
      if(statID!=statsId)
        continue;
      String jet;
      try
      {
        jet=stats[4];
        value=Formulas.getRandomJet(jet);
      }
      catch(Exception e)
      {
        e.printStackTrace();
        return 0;
      }
    }
    return value;
  }

  //v2.7 - Replaced String += with StringBuilder
  public String parseFMStatsString(String runeStat, GameObject obj, int add, boolean negatif)
  {
    StringBuilder stats=new StringBuilder();
    boolean isFirst=true;
    boolean found=false;
    for(SpellEffect SE : obj.Effects)
    {
      if(!isFirst)
        stats.append(",");

      String[] infos=SE.getArgs().split(";");
      try
      {
        stats.append(Integer.toHexString(SE.getEffectID())+"#"+infos[0]+"#"+infos[1]+"#0#"+infos[5]);
      }
      catch(Exception e)
      {
        e.printStackTrace();
        continue;
      }
      isFirst=false;
    }

    for(Entry<Integer, Integer> entry : obj.Stats.getMap().entrySet())
    {
      boolean statRemoved=false;
      if(!isFirst)
        stats.append(",");
      if(Integer.toHexString(entry.getKey()).compareTo(runeStat)==0) //stat you are maging
      {
        int newstats=0;
        if(negatif)
        {
          newstats=entry.getValue()-add;
          if(newstats<0) //stat > 0, add positive stat
            stats.append(Rune.getPositiveStatByRuneStat(Integer.toHexString(entry.getKey()))+"#"+Integer.toHexString(-newstats)+"#0#0#0d0+"+-newstats);
          else if(newstats>0) //stat < 0, keep
            stats.append(Integer.toHexString(entry.getKey())+"#"+Integer.toHexString(newstats)+"#0#0#0d0+"+newstats);
          else //else if newstats = 0, remove last character of string (",") and continue
          {
        	  try {
            stats.setLength(stats.length()-1);
            statRemoved=true;
        	  }
          catch(Exception e)
          {
            e.printStackTrace();
          }
          }
        }
        else
        {
          newstats=entry.getValue()+add;
          stats.append(Integer.toHexString(entry.getKey())+"#"+Integer.toHexString(newstats)+"#0#0#0d0+"+newstats);
        }
        found=true;
      }
      else
        stats.append(Integer.toHexString(entry.getKey())+"#"+Integer.toHexString(entry.getValue())+"#0#0#0d0+"+entry.getValue());
      if(!statRemoved)
        isFirst=false;
    }
    if(!found)
    {
      if(!isFirst)
        stats.append(",");
      stats.append(runeStat+"#"+Integer.toHexString(add)+"#0#0#0d0+"+add);
    }

    for(Entry<Integer, String> entry : obj.txtStats.entrySet())
    {
      if(!isFirst)
        stats.append(",");
      stats.append(Integer.toHexString(entry.getKey())+"#0#0#0#"+entry.getValue());
      isFirst=false;
    }
    return stats.toString();
  }

  //v2.0 - Redid magus fail stat choice
  //v2.7 - Replaced String += with StringBuilder
  public String parseStringStatsEC_FM(GameObject obj, double power, int stat)
  {
    String stats="";
    double lost=0.0;
    boolean stop=false;
    do
    {
      stats="";
      boolean first=false;
      for(SpellEffect EH : obj.Effects)
      {
        if(first)
          stats+=",";
        String[] infos=EH.getArgs().split(";");
        try
        {
          stats+=Integer.toHexString(EH.getEffectID())+"#"+infos[0]+"#"+infos[1]+"#0#"+infos[5];
        }
        catch(Exception e)
        {
          e.printStackTrace();
          continue;
        }
        first=true;
      }

      Map<Integer, Integer> statsObj=new HashMap<Integer, Integer>(obj.Stats.getMap());
      ArrayList<Integer> keys=new ArrayList<Integer>(obj.Stats.getMap().keySet());
      ArrayList<Integer> noNegativeMaxKeys=new ArrayList<Integer>();
      ArrayList<Integer> filteredKeys=new ArrayList<Integer>();
      ArrayList<Integer> removedStats=new ArrayList<Integer>();
      ArrayList<Integer> overmageKeys=new ArrayList<Integer>();
      Collections.shuffle(keys);

      for(Integer i : keys)
      {
        if(Rune.isNegativeStat(Integer.toHexString(i)))
        {
          if(statsObj.get(i)!=JobAction.getBaseMaxJet(obj.getTemplate().getId(),Rune.getNegativeStatByRuneStat(Integer.toHexString(i)))) //if current stat
            noNegativeMaxKeys.add(i);
          else
            removedStats.add(i); //remove stat if maxStat and negative
        }
        else
          noNegativeMaxKeys.add(i);
      }

      if(noNegativeMaxKeys.size()>1) //more than one stat to take sink from
      {
        for(Integer i : noNegativeMaxKeys)
        {
          if(i!=stat&&i!=Integer.parseInt(Rune.getNegativeStatByRuneStat(Integer.toHexString(stat)),16))
            filteredKeys.add(i);
          else
            removedStats.add(i); //remove key if runestat or negative runestat
        }
      }
      else
        filteredKeys=noNegativeMaxKeys; //if only one stat to take sink from

      if(filteredKeys.size()>1)
      {
        for(Integer i : filteredKeys)
          if(isOverFm(i,statsObj.get(i))) //stat, valueOfStat
            overmageKeys.add(i);
        for(Integer i : overmageKeys) //move overmaged stats to front of stats not being maged
        {
          filteredKeys.remove(i);
          filteredKeys.add(0,i);
        }
      }

      if(filteredKeys.size()==0)
        stop=true;
      //END OF FILTERING+SORTING REGION

      for(Integer i : filteredKeys)
      {
        int newstats=0;
        int statID=i;
        int statAmount=statsObj.get(i);
        float statPower=0;
        if(Rune.getRuneByStatId(Integer.toHexString(statID))!=null)
        {
          Rune rune=Rune.getRuneByStatId(Integer.toHexString(statID));
          statPower=rune.getPower()/rune.getStatsAdd();
        }
        if((statID==stat&&filteredKeys.size()!=1)||lost>=power) //stop removing if stat being targeted is runestat while more options are available or if more power than runepower has been lost
          newstats=statAmount;
        else if(statID==stat&&filteredKeys.size()==1&&statAmount*statPower<=power-lost) //only stat available is runeStat and less than power limit
        {
          newstats=0;
          stop=true;
        }
        else if(Rune.isNegativeStat(Integer.toHexString(statID)))
        {
          if(isOverFm(i,statsObj.get(i))) //TODO: add overmaged negative stat handler
          {

          }

          float chute=(float)(statAmount+statAmount*(power-(int)Math.floor(lost))/100.0D);
          newstats=(int)Math.ceil(chute); //ceil to prevent infinite loops (stat always goes up at least one)
          if(newstats>JobAction.getBaseMaxJet(obj.getTemplate().getId(),Integer.toHexString(i)))
            newstats=JobAction.getBaseMaxJet(obj.getTemplate().getId(),Integer.toHexString(i));
          double chutePwr=(newstats-statAmount)*-statPower;
          lost+=chutePwr;
        }
        else
        {
          double totalPower=statAmount*statPower;
          if(isOverFm(i,statsObj.get(i))&&power-lost<=totalPower) //if overmaged and has enough power
          {
            newstats=(int)Math.floor((totalPower-(power-lost))/statPower);
            double chutePwr=(statAmount-newstats)*statPower;
            lost+=chutePwr;
          }
          else
          {
            float chute=(float)(statAmount-statAmount*(power-(int)Math.floor(lost))/100.0D);
            newstats=(int)Math.floor(chute);
            double chutePwr=(statAmount-newstats)*statPower;
            lost+=chutePwr;
            newstats=(int)Math.floor(chute);
          }
        }
        if(newstats<1)
          continue;
        if(first)
          stats+=",";
        stats+=Integer.toHexString(statID)+"#"+Integer.toHexString(newstats)+"#0#0#0d0+"+newstats;
        first=true;
      }
      for(Integer i : removedStats) //add back filtered stats (stat currently being maged)
      {
        if(first)
          stats+=",";
        stats+=Integer.toHexString(i)+"#"+Integer.toHexString(statsObj.get(i))+"#0#0#0d0+"+statsObj.get(i);
        first=true;
      }
      for(Entry<Integer, String> entry : obj.txtStats.entrySet()) //add back textstats
      {
        if(first)
          stats+=",";
        stats+=Integer.toHexString((entry.getKey()))+"#0#0#0#"+entry.getValue();
        first=true;
      }
      obj.clearStats();
      obj.parseStringToStats(stats,true);
      if(stop)
        return stats;
    } while(lost<power&&!stats.isEmpty()); //stop looping when power limit is reached or item doesnt have stats
    return stats;
  }

  public boolean isOverFm(int stat, int val)
  {
    boolean trouve=false;
    String statsTemplate="";
    statsTemplate=this.template.getStrTemplate();
    if(statsTemplate==null||statsTemplate.isEmpty())
      return false;
    String[] split=statsTemplate.split(",");
    for(String s : split)
    {
      String[] stats=s.split("#");
      int statID=Integer.parseInt(stats[0],16);
      if(statID!=stat)
        continue;

      trouve=true;
      boolean sig=true;
      for(int a : Constant.ARMES_EFFECT_IDS)
        if(a==statID)
          sig=false;
      if(!sig)
        continue;
      String jet="";
      int value=1;
      try
      {
        jet=stats[4];
        value=Formulas.getRandomJet(jet);
        try
        {
          int min=Integer.parseInt(stats[1],16);
          int max=Integer.parseInt(stats[2],16);
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
        e.printStackTrace();
      }
      if(val>value)
        return true;
    }
    return !trouve;
  }

  //v2.6 - Soulstone fix
  public String findOverExo(final GameObject obj, int currentStat)
  {
    String stats="";
    final Map<Integer, Integer> statsObj=new HashMap<Integer, Integer>(obj.Stats.getMap());
    final ArrayList<Integer> keys=new ArrayList<Integer>(obj.Stats.getMap().keySet());
    Collections.shuffle(keys);
    final ArrayList<Integer> key=new ArrayList<Integer>();
    if(keys.size()>0)
    {
      Iterator<Integer> iter=keys.iterator();
      while(iter.hasNext())
      {
        int i=iter.next();
        final int value=statsObj.get(i);
        if(obj.getTemplate().getType()!=83)
          if(this.isOverFm(i,value)&&i!=currentStat)
            key.add(i);
      }
    }
    for(Integer i : key)
    {
      final int statID=i;
      final int value=statsObj.get(i);
      stats=String.valueOf(stats)+statID+","+String.valueOf(value)+";";
    }
    return stats;
  }

  //v2.6 - Soulstone fix
  public String findAllExo(final GameObject obj)
  {
    String stats="";
    final Map<Integer, Integer> statsObj=new HashMap<Integer, Integer>(obj.Stats.getMap());
    final ArrayList<Integer> keys=new ArrayList<Integer>(obj.Stats.getMap().keySet());
    Collections.shuffle(keys);
    final ArrayList<Integer> key=new ArrayList<Integer>();
    if(keys.size()>0)
    {
      Iterator<Integer> iter=keys.iterator();
      while(iter.hasNext())
      {
        int i=iter.next();
        final int value=statsObj.get(i);
        if(obj.getTemplate().getType()!=83)
          if(this.isOverFm(i,value))
            key.add(i);
      }
    }
    for(Integer i : key)
    {
      final int statID=i;
      final int value=statsObj.get(i);
      stats=String.valueOf(stats)+statID+","+String.valueOf(value)+";";
    }
    return stats;
  }

  public boolean isOverFm2(int stat, int val)
  {
    boolean trouve=false;
    String statsTemplate="";
    statsTemplate=this.template.getStrTemplate();
    if(statsTemplate==null||statsTemplate.isEmpty())
      return false;
    String[] split=statsTemplate.split(",");
    for(String s : split)
    {
      String[] stats=s.split("#");
      int statID=Integer.parseInt(stats[0],16);
      if(statID!=stat)
        continue;

      trouve=true;
      boolean sig=true;
      for(int a : Constant.ARMES_EFFECT_IDS)
        if(a==statID)
          sig=false;
      if(!sig)
        continue;
      String jet="";
      int value=1;
      try
      {
        jet=stats[4];
        value=Formulas.getRandomJet(jet);
        try
        {
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
      catch(Exception e)
      {
        e.printStackTrace();
      }
      if(val==value)
        return true;
    }
    if(!trouve)
      return true;
    return false;
  }

  //v2.8 - Better healing weapon code
  public boolean isHealWeapon()
  {
    for(SpellEffect effect : this.getEffects())
      if(effect.getEffectID()==108)
        return true;
    return false;
  }

  public boolean onlyStat(String runeStat)
  {
    Map<Integer, Integer> statsObj=new HashMap<Integer, Integer>(this.Stats.getMap());
    ArrayList<Integer> keys=new ArrayList<Integer>(this.Stats.getMap().keySet());
    for(Integer i : keys)
    {
      if(!Integer.toHexString(i).equalsIgnoreCase(runeStat)) //if statid not equal to rune's statid
      {
        if(Rune.isNegativeStat(Integer.toHexString(i))) //if current stat is negative
        {
          if(Integer.parseInt(Rune.getNegativeStatByRuneStat(runeStat),16)!=i) //if negative stat isnt equal to negative rune stat
            if(statsObj.get(i)!=JobAction.getBaseMaxJet(this.getTemplate().getId(),Integer.toHexString(i))) //if negative version of stat is not at max stat
              return false;
        }
        else //current stat is positive and not equal to rune's statid
          return false;
      }
    }
    return true;
  }

  public ArrayList<String> getSpellStats()
  {
    return SortStats;
  }

    public void resetEffects()
    {
        Effects = new ArrayList<SpellEffect>();
        Stats = new Stats();
    }

    public void parseStringToStats(final String strStats, final boolean save, final boolean isForFm) {
        if(this.template != null & this.template.getId() == 7010) return;
        
        final StringBuilder statsToOrder = new StringBuilder(); // for fm
        boolean isFirst = true; // for fm
        
        String dj1 = "";
        if (!strStats.equalsIgnoreCase("")) {
            for (String split : strStats.split(",")) {
                try {
                    if (split.equalsIgnoreCase(""))
                        continue;
                    if (split.substring(0, 3).equalsIgnoreCase("325") && (this.getTemplate().getId() == 10207 || this.getTemplate().getId() == 10601)) {
                        txtStats.put(Constant.STATS_DATE, split.substring(3) + "");
                        continue;
                    }
                    if (split.substring(0, 3).equalsIgnoreCase("3dc")) {// Si c'est une rune de signature crée
                        txtStats.put(Constant.STATS_SIGNATURE, split.split("#")[4]);
                        continue;
                    }
                    if (split.substring(0, 3).equalsIgnoreCase("3d9")) {// Si c'est une rune de signature modifié
                        txtStats.put(Constant.STATS_CHANGE_BY, split.split("#")[4]);
                        continue;
                    }
                    if (split.substring(0, 3).equalsIgnoreCase("3d7")) {// Si c'est une rune de signature modifié
                        txtStats.put(Constant.STATS_EXCHANGE_IN, split.split("#")[4]);
                        continue;
                    }
                    if (split.substring(0, 3).equalsIgnoreCase("29d")) {// Si c'est une rune de signature modifié
                        txtStats.put(Constant.STATS_NIVEAU2, split.split("#")[4]);
                        continue;
                    }
                    if (split.substring(0, 3).equalsIgnoreCase("844")) {// Si c'est une rune de signature modifié
                        txtStats.put(Constant.STATS_BONUSADD, split.split("#")[4]);
                        continue;
                    }

                    String[] stats = split.split("#");
                    int id = Integer.parseInt(stats[0], 16);
                    
                    if(id == Constant.STATS_MIMI) {
                    	final String[] datas = split.split("#");
                    	// 2 == Apparat
                    	// 3 == Id Template
                    	txtStats.put(id, datas[2]+";"+datas[3]);
                    	continue;
                    }
                    if (id == Constant.STATS_PETS_DATE
                            && this.getTemplate().getType() == Constant.ITEM_TYPE_CERTIFICAT_CHANIL) {
                        txtStats.put(id, split.substring(3));
                        continue;
                    }
                    if (id == Constant.STATS_CHANGE_BY || id == Constant.STATS_NAME_TRAQUE || id == Constant.STATS_OWNER_1) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (id == Constant.STATS_GRADE_TRAQUE || id == Constant.STATS_ALIGNEMENT_TRAQUE || id == Constant.STATS_NIVEAU_TRAQUE) {
                        txtStats.put(id, stats[3]);
                        continue;
                    }
                    if (id == Constant.STATS_PETS_SOUL) {
                        SoulStats.put(Integer.parseInt(stats[1], 16), Integer.parseInt(stats[3], 16)); // put(id_monstre, nombre_tué)
                        continue;
                    }
                    if (id == Constant.STATS_NAME_DJ) {
                        dj1 += (!dj1.isEmpty() ? "," : "") + stats[3];
                        txtStats.put(Constant.STATS_NAME_DJ, dj1);
                        continue;
                    }
                    if (id == 997 || id == 996) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (this.template != null && this.template.getId() == 77 && id == Constant.STATS_PETS_DATE) {
                        txtStats.put(id, split.substring(3));
                        continue;
                    }
                    if (id == Constant.STATS_DATE) {
                        txtStats.put(id, stats[3]);
                        continue;
                    }
                    
                    if (id >= 281 && id <= 294) {
                    	SortStats.add(split);
                    	continue; 
                    }
                    
                    //Si stats avec Texte (Signature, apartenance, etc)//FIXME
                    if (id != Constant.STATS_RESIST && (!stats[3].equals("") && (!stats[3].equals("0") || id == Constant.STATS_PETS_DATE || id == Constant.STATS_PETS_PDV || id == Constant.STATS_PETS_POIDS || id == Constant.STATS_PETS_EPO || id == Constant.STATS_PETS_REPAS))) {//Si le stats n'est pas vide et (n'est pas égale à 0 ou est de type familier)
                        if (!(this.getTemplate().getType() == Constant.ITEM_TYPE_CERTIFICAT_CHANIL && id == Constant.STATS_PETS_DATE)) {
                            txtStats.put(id, stats[3]);
                            continue;
                        }
                    }
                    if (id == Constant.STATS_RESIST && this.getTemplate() != null && this.getTemplate().getType() == 93) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (id == Constant.STATS_RESIST) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }

                    boolean follow1 = true;
                    switch (id) {
                        case 110:
                        case 139:
                        case 605:
                        case 614:
                            String min = stats[1];
                            String max = stats[2];
                            String jet = stats[4];
                            String args = min + ";" + max + ";-1;-1;0;" + jet;
                            Effects.add(new SpellEffect(id, args, 0, -1));
                            follow1 = false;
                            break;
                    }
                    if (!follow1) {
                        continue;
                    }

                    boolean follow2 = true;
                    for (int a : Constant.ARMES_EFFECT_IDS) {
                        if (a == id) {
                            Effects.add(new SpellEffect(id, stats[1] + ";" + stats[2] + ";-1;-1;0;" + stats[4], 0, -1));
                            follow2 = false;
                        }
                    }
                    if (!follow2)
                        continue;//Si c'était un effet Actif d'arme ou une signature

                    if(!isForFm)
                    	Stats.addOneStat(id, Integer.parseInt(stats[1], 16));
                    else {
                    	if(!isFirst)
                    		statsToOrder.append(",");
                    	isFirst = false;
                    	statsToOrder.append(split);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        final String stringOrder = statsToOrder.toString();
        
        if(isForFm && !stringOrder.isEmpty())
        {
        	for(final String stat : Formulas.sortStatsByOrder(stringOrder).split(",")) {
        		final String[] split = stat.split("#");
        		final int id = Integer.parseInt(split[0], 16);
        		final int value = Integer.parseInt(split[1], 16);
        		Stats.addOneStat(id, value);
        	}
        }
        
        if(save)
            this.setModification();
    }
}
