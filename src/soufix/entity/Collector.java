package soufix.entity;

import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.common.ConditionParser;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.game.World;
import soufix.guild.Guild;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Collector
{
  private int id;
  private short map;
  private int cell;
  private byte orientation;
  private int guildId=0;
  private short N1=0;
  private short N2=0;
  private byte inFight=0;
  private int inFightId=-1;
  private long kamas=0;
  private long xp=0;
  private boolean inExchange=false;
  private Player poseur=null;
  private long date;
  //Timer
  private long timeTurn=45000;
  private long time_add =0;
//Les logs
  private Map<Integer, GameObject> logObjects=new HashMap<Integer, GameObject>();
  private Map<Integer, GameObject> objects=new HashMap<Integer, GameObject>();
  //La défense
  private Map<Integer, Player> defenserId=new HashMap<>();

  public Collector(int id, short map, int cell, byte orientation, int aGuildID, short N1, short N2, Player poseur, long date, String items, long kamas, long xp)
  {
    this.id=id;
    this.map=map;
    this.cell=cell;
    this.orientation=orientation;
    this.guildId=aGuildID;
    this.N1=N1;
    this.N2=N2;
    this.poseur=poseur;
    this.date=date;
    this.time_add = System.currentTimeMillis()+900000;
    for(String item : items.split("\\|"))
    {
      if(item.equals(""))
        continue;
      String[] infos=item.split(":");
      int itemId=Integer.parseInt(infos[0]);
      GameObject obj=World.getGameObject(itemId);
      if(obj==null)
        continue;
      this.objects.put(obj.getGuid(),obj);
    }
    this.xp=xp;
    this.kamas=kamas;
  }

  public String getFullName()
  {
    return Integer.toString(this.getN1(),36)+","+Integer.toString(this.getN2(),36);
  }

  public static String parseGM(GameMap map)
  {
    StringBuilder sock=new StringBuilder();
    sock.append("GM|");
    boolean isFirst=true;
    for(Map.Entry<Integer, Collector> Collector : Main.world.getCollectors().entrySet())
    {
      Collector c=Collector.getValue();
      if(c==null)
        continue;
      if(c.inFight>0)
        continue;//On affiche pas le Collector si il est en combat
      if(c.map==map.getId())
      {
        Guild G=Main.world.getGuild(c.guildId);
        if(G==null)
        {
          c.reloadTimer();
          Database.getDynamics().getCollectorData().delete(c.getId());
          Main.world.getCollectors().remove(c.getId());
          continue;
        }
        if(!isFirst)
          sock.append("|");
        sock.append("+");
        sock.append(c.cell).append(";");
        sock.append(c.orientation).append(";");
        sock.append("0").append(";");
        sock.append(c.id).append(";");
        sock.append(Integer.toString(c.N1,36)).append(",").append(Integer.toString(c.N2,36)).append(";");
        sock.append("-6").append(";");
        sock.append("6000^100;");
        sock.append(G.getLvl()).append(";");
        sock.append(G.getName()).append(";").append(G.getEmblem());
        isFirst=false;
      }
    }
    return sock.toString();
  }

  public static String parseToGuild(int GuildID)
  {
    /**
     * gITM + id;-10000 nameId1,14 nameId2,26 callerName,poney startDate,
     * date a laquel tu as mis le perco lastHName, dernier récolteur lastHD,
     * date a laquel le perco a été récolté nextHD; date a laquel le perco
     * pourra étre récolté mapid; state; 0 récolte, 1 attaque, 2 combat, é
     * la fin du timer, passe en combat automatiquement time;temps en ms
     * quand le perco a été lancé maxTimer;temps en ms quand le combat se
     * lance numbPlayer: 1-7
     *
     * les dates au dessus aucune conversion, juste un timestamp é mettre ?
     * ouaip impec, grand merci :) !
     *
     * TEST : gITM+-10000;14,26,poney,0,tagada,55,6400000000000;5q6;1;
     * 1000000000000;2400000000000;7
     */

    // id du poseur
    // date quand on pose
    StringBuilder packet=new StringBuilder();
    boolean isFirst=true;
    for(Map.Entry<Integer, Collector> Collector : Main.world.getCollectors().entrySet())
    {
      if(Collector.getValue().getGuildId()==GuildID)
      {
        GameMap map=Main.world.getMap(Collector.getValue().getMap());
        if(isFirst)
          packet.append("+");
        if(!isFirst)
          packet.append("|");

        Collector perco=Collector.getValue();
        int inFight=Collector.getValue().getInFight();
        String name="";
        if(Collector.getValue().getPoseur()!=null)
        {
          name=Collector.getValue().getPoseur().getName();
        }

        packet.append(perco.getId()); // id
        packet.append(";");
        packet.append(Integer.toString(perco.N1,36)); // nameId1
        packet.append(",");
        packet.append(Integer.toString(perco.N2,36)); // nameId2

        packet.append(",");
        packet.append(name); // callerName
        packet.append(",");
        packet.append(Long.toString(perco.date)); // startDate
        packet.append(",");
        packet.append(""); // lastHName
        packet.append(",");
        packet.append("-1"); // lastHD
        packet.append(",");
        packet.append(Long.toString(perco.date+Main.world.getGuild(GuildID).getLvl()*600000)); // nextHD
        packet.append(";");

        packet.append(Integer.toString(map.getId(),36));
        packet.append(",");
        packet.append(map.getX());
        packet.append(",");
        packet.append(map.getY());
        packet.append(";");

        packet.append(inFight);
        packet.append(";");

        if(inFight==1)
        {
          if(map.getFight(Collector.getValue().get_inFightID())==null)
          {
            packet.append("45000");//TimerActuel
            packet.append(";");
          }
          else
          {
            packet.append(Collector.getValue().getTurnTimer());//TimerActuel si combat
            packet.append(";");
          }

          packet.append("45000");//TimerInit
          packet.append(";");

          int numcase=(Main.world.getMap(Collector.getValue().getMap()).getMaxTeam()-1);
          if(numcase>7)
            numcase=7;
          packet.append(numcase);//Nombre de place maximum : En fonction de la map moins celle du Collector
          packet.append(";");
        }
        else
        {
          packet.append("0;");
          packet.append("45000;");
          packet.append("7;");
        }
        isFirst=false;
      }
    }
    if(packet.length()==0)
      packet=new StringBuilder("null");

    return packet.toString();

  }

  public static int getCollectorByGuildId(int id)
  {
    for(Map.Entry<Integer, Collector> Collector : Main.world.getCollectors().entrySet())
      if(Collector.getValue().getMap()==id)
        return Collector.getValue().getGuildId();
    return 0;
  }

  public static Collector getCollectorByMapId(short id)
  {
    for(Map.Entry<Integer, Collector> Collector : Main.world.getCollectors().entrySet())
      if(Collector.getValue().getMap()==id)
        return Main.world.getCollectors().get(Collector.getValue().getId());
    return null;
  }

  public static int countCollectorGuild(int GuildID)
  {
    int i=0;
    for(Map.Entry<Integer, Collector> Collector : Main.world.getCollectors().entrySet())
      if(Collector.getValue().getGuildId()==GuildID)
        i++;
    return i;
  }

  public static void parseAttaque(Player perso, int guildID)
  {
    for(Map.Entry<Integer, Collector> Collector : Main.world.getCollectors().entrySet())
      if(Collector.getValue().getInFight()>0&&Collector.getValue().getGuildId()==guildID)
        SocketManager.GAME_SEND_gITp_PACKET(perso,parseAttaqueToGuild(Collector.getValue().getId(),Collector.getValue().getMap(),Collector.getValue().get_inFightID()));
  }

  public static void parseDefense(Player perso, int guildID)
  {
    for(Map.Entry<Integer, Collector> Collector : Main.world.getCollectors().entrySet())
      if(Collector.getValue().getInFight()>0&&Collector.getValue().getGuildId()==guildID)
        SocketManager.GAME_SEND_gITP_PACKET(perso,parseDefenseToGuild(Collector.getValue()));
  }

  public static String parseAttaqueToGuild(int id, short map, int fightId)
  {
    StringBuilder str=new StringBuilder();
    str.append("+").append(id);
    GameMap gameMap=Main.world.getMap(map);

    if(gameMap!=null)
    {
      gameMap.getFights().stream().filter(fight -> fight.getId()==fightId).forEach(fight -> {
        fight.getFighters(1).stream().filter(f -> f.getPersonnage()!=null).forEach(f -> {
          str.append("|");
          str.append(Integer.toString(f.getPersonnage().getId(),36)).append(";");
          str.append(f.getPersonnage().getName()).append(";");
          str.append(f.getPersonnage().getLevel()).append(";");
          str.append("0;");
        });
      });
    }
    return str.toString();
  }

  public static String parseDefenseToGuild(Collector collector)
  {
    StringBuilder str=new StringBuilder();
    str.append("+").append(collector.getId());

    for(Player player : collector.getDefenseFight().values())
    {
      if(player==null)
        continue;
      str.append("|");
      str.append(Integer.toString(player.getId(),36)).append(";");
      str.append(player.getName()).append(";");
      str.append(player.getGfxId()).append(";");
      str.append(player.getLevel()).append(";");
      str.append(Integer.toString(player.getColor1(),36)).append(";");
      str.append(Integer.toString(player.getColor2(),36)).append(";");
      str.append(Integer.toString(player.getColor3(),36)).append(";");
    }
    return str.toString();
  }

  public static void removeCollector(int GuildID)
  {
    for(Map.Entry<Integer, Collector> Collector : Main.world.getCollectors().entrySet())
    {
      if(Collector.getValue().getGuildId()==GuildID)
      {
        Main.world.getCollectors().remove(Collector.getKey());
        for(Player p : Main.world.getMap(Collector.getValue().getMap()).getPlayers())
        {
          SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(p.getCurMap(),Collector.getValue().getId());//Suppression visuelle
        }
        Collector.getValue().reloadTimer();
        Database.getDynamics().getCollectorData().delete(Collector.getKey());
      }
     }
  }

  public void reloadTimer()
  {
    if(Main.world.getGuild(getGuildId())==null)
      return;
    Long time=Main.world.getGuild(getGuildId()).timePutCollector.get(getMap());
    if(time!=null)
      return;
    Main.world.getGuild(getGuildId()).timePutCollector.put(getMap(),getDate());
  }

  public long getDate()
  {
    return this.date;
  }

  public Player getPoseur()
  {
    return this.poseur;
  }

  public void setPoseur(Player poseur)
  {
    this.poseur=poseur;
  }

  public int getId()
  {
    return this.id;
  }

  public short getMap()
  {
    return this.map;
  }

  public int getCell()
  {
    return this.cell;
  }

  public void setCell(int cell)
  {
    this.cell=cell;
  }

  public int getGuildId()
  {
    return this.guildId;
  }

  public int getN1()
  {
    return this.N1;
  }

  public int getN2()
  {
    return this.N2;
  }

  public int getInFight()
  {
    return this.inFight;
  }

  public void setInFight(byte inFight)
  {
    this.inFight=inFight;
  }

  public int get_inFightID()
  {
    return this.inFightId;
  }

  public void set_inFightID(int inFightId)
  {
    this.inFightId=inFightId;
  }

  public long getKamas()
  {
    return kamas;
  }

  public void setKamas(long kamas)
  {
    this.kamas=kamas;
  }

  public long getXp()
  {
    return this.xp;
  }

  public void setXp(long xp)
  {
    this.xp=xp;
  }

  public boolean getExchange()
  {
    return this.inExchange;
  }

  public void setExchange(boolean inExchange)
  {
    this.inExchange=inExchange;
  }

  public long getTurnTimer()
  {
    return this.timeTurn;
  }

  public void setTimeTurn(long timeTurn)
  {
    this.timeTurn=timeTurn;
  }

  public void removeTimeTurn(int timeTurn)
  {
    this.timeTurn-=timeTurn;
  }

  public void addLogObjects(int id, GameObject obj)
  {
    this.logObjects.put(id,obj);
  }

  public String getLogObjects()
  {
    if(this.logObjects.isEmpty())
      return "";
    StringBuilder str=new StringBuilder();

    for(GameObject obj : this.logObjects.values())
      str.append(";").append(obj.getTemplate().getId()).append(",").append(obj.getQuantity());

    return str.toString();
  }

  public Map<Integer, GameObject> getOjects()
  {
    return this.objects;
  }

  public boolean haveObjects(int id)
  {
    return this.objects.get(id)!=null;
  }

  public int getPodsTotal()
  {
    int pod=0;
    for(GameObject object : this.objects.values())
      if(object!=null)
        pod+=object.getTemplate().getPod()*object.getQuantity();
    return pod;
  }

  public int getMaxPod()
  {
    return Main.world.getGuild(this.getGuildId()).getStats(Constant.STATS_ADD_PODS);
  }

  public boolean addObjet(GameObject newObj)
  {
    for(Map.Entry<Integer, GameObject> entry : this.objects.entrySet())
    {
      GameObject obj=entry.getValue();
      if(ConditionParser.stackIfSimilar(obj,newObj,true))
      {
        obj.setQuantity(obj.getQuantity()+newObj.getQuantity(),null);//On ajoute QUA item a la quantité de l'objet existant
        return false;
      }
    }
    this.objects.put(newObj.getGuid(),newObj);
    return true;
  }

  public void removeObjet(int id)
  {
    this.objects.remove(id);
  }

  public void delCollector(int id)
  {
    for(GameObject obj : this.objects.values())
      Main.world.removeGameObject(obj.getGuid());
    Main.world.getCollectors().remove(id);
  }

  public String getItemCollectorList()
  {
    StringBuilder items=new StringBuilder();
    if(!this.objects.isEmpty())
      for(GameObject obj : this.objects.values())
        items.append("O").append(obj.parseItem()).append(";");
    if(this.kamas!=0)
      items.append("G").append(this.kamas);
    return items.toString();
  }

  //v2.7 - Replaced String += with StringBuilder
  public String parseItemCollector()
  {
    StringBuilder items=new StringBuilder();
    for(GameObject obj : this.objects.values())
      items.append(obj.getGuid()+"|");
    return items.toString();
  }

  public void removeFromCollector(Player P, int id, int qua)
  {
    if(qua<=0)
      return;
    GameObject CollectorObj=World.getGameObject(id);
    GameObject PersoObj=P.getSimilarItem(CollectorObj);
    int newQua=CollectorObj.getQuantity()-qua;
    if(PersoObj==null)//Si le joueur n'avait aucun item similaire
    {
      //S'il ne reste rien
      if(newQua<=0)
      {
        //On retire l'item
        removeObjet(id);
        //On l'ajoute au joueur
        P.addObjet(CollectorObj);
        //On envoie les packets
        String str="O-"+id;
        SocketManager.GAME_SEND_EsK_PACKET(P,str);
      }
      else
      //S'il reste des this.objects
      {
        //On crée une copy de l'item
        PersoObj=GameObject.getCloneObjet(CollectorObj,qua);
        //On l'ajoute au monde
        World.addGameObject(PersoObj,true);
        //On retire X objet
        CollectorObj.setQuantity(newQua,P);
        //On l'ajoute au joueur
        P.addObjet(PersoObj);

        //On envoie les packets
        String str="O+"+CollectorObj.getGuid()+"|"+CollectorObj.getQuantity()+"|"+CollectorObj.getTemplate().getId()+"|"+CollectorObj.parseStatsString();
        SocketManager.GAME_SEND_EsK_PACKET(P,str);
      }
    }
    else
    {
      //S'il ne reste rien
      if(newQua<=0)
      {
        //On retire l'item
        this.removeObjet(id);
        Main.world.removeGameObject(CollectorObj.getGuid());
        //On Modifie la quantité de l'item du sac du joueur
        PersoObj.setQuantity(PersoObj.getQuantity()+CollectorObj.getQuantity(),P);

        //On envoie les packets
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(P,PersoObj);
        String str="O-"+id;
        SocketManager.GAME_SEND_EsK_PACKET(P,str);
      }
      else
      //S'il reste des this.objects
      {
        //On retire X objet
        CollectorObj.setQuantity(newQua,P);
        //On ajoute X this.objects
        PersoObj.setQuantity(PersoObj.getQuantity()+qua,P);

        //On envoie les packets
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(P,PersoObj);
        String str="O+"+CollectorObj.getGuid()+"|"+CollectorObj.getQuantity()+"|"+CollectorObj.getTemplate().getId()+"|"+CollectorObj.parseStatsString();
        SocketManager.GAME_SEND_EsK_PACKET(P,str);
      }
    }
    SocketManager.GAME_SEND_Ow_PACKET(P);
    //Database.getStatics().getPlayerData().update(P);
  }

  public boolean addDefenseFight(Player P)
  {
    if(this.defenserId.size()>=Main.world.getMap(getMap()).getMaxTeam())
    {
      return false;
    }
    else
    {
    	int multiIp= 0;
        for(Entry<Integer, Player> perso : defenserId.entrySet())
          if(perso.getValue().getAccount().getCurrentIp().compareTo(P.getAccount().getCurrentIp())==0)
            multiIp++;
        if(multiIp >= 2)
        {
          SocketManager.GAME_SEND_MESSAGE(P,"Vous ne pouvez pas rejoindre ce combat, il y a Déjé un personnage dans ce combat avec la méme IP.");
          return false;
        }
      this.defenserId.put(P.getId(),P);
      return true;
    }
  }

  public void delDefenseFight(Player P)
  {
    if(this.defenserId.containsKey(P.getId()))
      this.defenserId.remove(P.getId());
  }

  public void clearDefenseFight()
  {
    this.defenserId.clear();
  }

  public Map<Integer, Player> getDefenseFight()
  {
    return this.defenserId;
  }
  public long getTime_add() {
	return time_add;
}


  public Collection<GameObject> getDrops()
  {
    return this.objects.values();
  }
  
  public String parseQuestionTaxCollector()
  {
    return "1"+';'+Main.world.getGuild(this.getGuildId()).getName()+','+Main.world.getGuild(this.getGuildId()).getLvl()+','+this.getPodsTotal()+','+this.getXp();
  }
}