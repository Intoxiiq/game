package soufix.client;

import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.area.map.entity.House;
import soufix.area.map.entity.InteractiveObject;
import soufix.area.map.entity.MountPark;
import soufix.client.other.Party;
import soufix.client.other.Shortcuts;
import soufix.client.other.Stalk;
import soufix.client.other.Stats;
import soufix.command.administration.Group;
import soufix.common.ConditionParser;
import soufix.common.Formulas;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.Collector;
import soufix.entity.Prism;
import soufix.entity.monster.MobGroup;
import soufix.entity.mount.Mount;
import soufix.entity.pet.Pet;
import soufix.entity.pet.PetEntry;
import soufix.events.Start;
import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.spells.Spell;
import soufix.fight.spells.SpellEffect;
import soufix.game.GameClient;
import soufix.game.World;
import soufix.game.action.ExchangeAction;
import soufix.game.action.GameAction;
import soufix.guild.Guild;
import soufix.guild.GuildMember;
import soufix.job.Job;
import soufix.job.JobAction;
import soufix.job.JobConstant;
import soufix.job.JobStat;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.object.ObjectSet;
import soufix.object.ObjectTemplate;
import soufix.other.Action;
import soufix.other.Dopeul;
import soufix.quest.Quest;
import soufix.quest.QuestPlayer;
import soufix.utility.Pair;
import soufix.utility.TimerWaiterPlus;
import soufix.other.SetRapido;
import soufix.other.Succes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;



public class Player
{

  public Stats stats;
  public boolean isNew=false;
  //Job
  //Disponibilité
  public boolean _isAbsent=false;
  public boolean _isInvisible=false;
  //Double
  public boolean _isClone=false;
  //Suiveur - Suivi
  public Map<Integer, Player> follower=new HashMap<>();
  public Player follow=null;
  //Prison Alignement :
  public boolean isInEnnemyFaction;
  public long enteredOnEnnemyFaction;
  public boolean donjon;
  //Commande héhé
  public int thatMap=-1;
  public int thatCell=-1;
  public boolean walkFast=false;
  public boolean getCases=false;
  public ArrayList<Integer> thisCases=new ArrayList<>();
  public boolean mpToTp=false;
  public boolean noall=false;
  private int id;
  private String name;
  private int sexe;
  private int classe;
  private int color2;
  private int color1;
  private int color3;
  private int level;
  private int energy;
  private long exp;
  private int curPdv;
  private int maxPdv;
  private Stats statsParcho=new Stats(true);
  private long kamas;
  private int _spellPts;
  private int _capital;
  private int _size;
  private int gfxId;
  private int _orientation=1;
  private Account account;
  private short oldMap;
  private int oldCell;
  //PDV
  private int _accID;
  private boolean canAggro=true;
  //Emote
  private List<Integer> emotes=new ArrayList<>();
  //Variables d'ali
  private byte _align=0;
  private int _deshonor=0;
  private int _honor=0;
  private boolean _showWings=false;
  private int _aLvl=0;
  private GuildMember _guildMember;
  private boolean _showFriendConnection;
  private String _canaux;
  private Fight fight;
  private boolean away;
  private GameMap curMap;
  private GameCase curCell;
  private boolean ready=false;
  private boolean isOnline=false;
  private Party party;
  private int duelId=-1;
  private Map<Integer, SpellEffect> buffs=new HashMap<Integer, SpellEffect>();
  private Map<Integer, GameObject> objects=new ConcurrentHashMap<Integer, GameObject>();
private String _savePos;
  private int _emoteActive=0;
  private int savestat;
  private House _curHouse;
  //Invitation
  private int _inviting=0;
  private ArrayList<Integer> craftingType=new ArrayList<>();
  private Map<Integer, JobStat> _metiers=new HashMap<Integer, JobStat>();

  //Monture
  private Mount _mount;
  private int _mountXpGive=0;
  private boolean _onMount=false;
  //Zaap
  private ArrayList<Short> _zaaps=new ArrayList<Short>();
  //Sort
  private Map<Integer, Spell.SortStats> _sorts=new HashMap<Integer, Spell.SortStats>();
  private Map<Integer, Character> _sortsPlaces=new HashMap<Integer, Character>();
  //Titre
  private int _title=0;
  //Mariage
  private int wife=0;
  private int _isOK=0;
  //Fantome
  private boolean isGhost=false;
  private int _Speed=0;
  //Marchand
  private boolean _seeSeller=false;
  private Map<Integer, Integer> _storeItems=new HashMap<>(); //<ObjID, Prix>
  //Metier
  private boolean _metierPublic=false;
  private boolean _livreArti=false;

  //Fight end
  private int hasEndFight=-1;
  private Action endFightAction;
  private MobGroup hasMobGroup=null;
  //Item classe
  private ArrayList<Integer> _itemClasse=new ArrayList<Integer>();
  private Map<Integer, Pair<Integer, Integer>> _itemClasseSpell=new HashMap<>();
  private int _bendHechizo=0;
  private int _bendEfecto=0;
  private int _bendModif=0;
  // Taverne
  private long timeTaverne=0;
  //GA
  private GameAction _gameAction=null;
  private GameAction _gameAction_rapide=null;
//Name
  //Fight :
  private boolean _spec;
  //Traque
  private Stalk _traqued;
  private boolean doAction;
  //FullMorph Stats
  private boolean _morphMode=false;
  private int _morphId;
  private Map<Integer, Spell.SortStats> _saveSorts=new HashMap<Integer, Spell.SortStats>();
  private Map<Integer, Character> _saveSortsPlaces=new HashMap<Integer, Character>();
  private int _saveSpellPts;
  private int pa=0, pm=0, vitalite=0, sagesse=0, terre=0, feu=0, eau=0, air=0, initiative=0;
  private boolean useStats=false;
  private boolean useCac=true;
  // Other ?
  private String _allTitle="";
  private boolean isBlocked=false;
  private int action=-1;
  //Regen hp
  private boolean sitted;
  private int regenRate=2000;
  private long regenTime=-1; //-1 veut dire que la personne ne c'est jamais connecte
  private boolean isInPrivateArea=false;
  public Start start;
  private Group groupe;
  private boolean isInvisible=false;
  private boolean _isForgetingSpell;

  private Map<Integer, QuestPlayer> questList=new HashMap<>();
  private boolean changeName;
  public boolean afterFight=false;
  public boolean boutique=false;
  public boolean tokenShop=false;
  private int tokens;
  //private int apExo;
  //private int mpExo;
  //private int raExo;
  private JobAction _curJobAction;
  //Follow system
  public boolean isLeader=false;
  public Player leader=null;
  public boolean isFollowing=false;
  public ArrayList<Player> followers=new ArrayList<Player>();
  private GameMap followerMap;
  private int followerCell;
  //Tactical mode memory
  private boolean worldMarket=false;
  //IPDrop system
  public boolean ipDrop=false;
  public boolean ipKamas=false;
  private Pair<InteractiveObject, GameCase> inInteractiveObject=null;
  private boolean canDrop=true;
  private boolean canDrop_items=true;
  private boolean canDrop_ressources=true;
  private boolean autoSkip=false;
  private Long time_last_connecte;
  private Player Spioned_by;
  private final Map<Integer, SetRapido> _setsRapidos = new ConcurrentHashMap<Integer, SetRapido>();
  private boolean One_windows=false;
  public boolean relique_paase=false;
  public boolean couleur=false;
  public int Song;
  private long time_co;
  private long time_total;
  private int total_combat ;
  private int total_reculte ;
  private int send_notif_bot;
  private int joindelay = 1;
  public boolean songe_reset =false;
	private Player player;
	private int id1;
//public boolean bot=false;
public ArrayList<Integer> getIsCraftingType()
  {
    return craftingType;
  }

  public Player(int id, String name, int groupe, int sexe, int classe, int color1, int color2, int color3, long kamas, int pts, int _capital, int energy, int level, long exp, int _size, int _gfxid, byte alignement, int account, Map<Integer, Integer> stats, byte seeFriend, byte seeAlign, byte seeSeller, String canaux, short map, int cell, String stuff, String storeObjets, String pdvPer, String spells, String savePos, String jobs, int mountXp, int mount, int honor, int deshonor, int alvl, String z, int title, int wifeGuid, String morphMode, String allTitle, String emotes, long prison, boolean isNew, String parcho, long timeDeblo, boolean noall, String deadInformation, byte deathCount, long totalKills, final int tokens, final int apExo, final int mpExo, final int raExo, String rapid,int song, boolean Reload_item, int ornement, String ListOrnements)
  {
    this.id=id;
    this.noall=noall;
    this.name=name;
    this.groupe=Group.getGroupeById(groupe);
	  if(Main.world.getPlayer(id) != null)
			return;
    this.sexe=sexe;
    this.classe=classe;
    this.Song=song;
    this.color1=color1;
    this.color2=color2;
    this.color3=color3;
    this.kamas=kamas;
    this._capital=_capital;
    this._align=alignement;
    this._honor=honor;
    this._deshonor=deshonor;
    this._aLvl=alvl;
    this.energy=energy;
    this.level=level;
    this.exp=exp;
    if(mount!=-1)
      this._mount=Main.world.getMountById(mount);
    this._size=_size;
    this.gfxId=_gfxid;
    this._mountXpGive=mountXp;
    this.stats=new Stats(stats,true,this);
    this._accID=account;
    this.account=Main.world.getAccount(account);
    this._showFriendConnection=seeFriend==1;
    this.wife=wifeGuid;
    this._metierPublic=false;
    this._title=title;
    this.changeName=false;
    this._allTitle=allTitle;
    this._seeSeller=seeSeller==1;
    savestat=0;
    this._canaux=canaux;
    this.curMap=Main.world.getMap(map);
    this._savePos=savePos;
    this.isNew=isNew;
    this.regenTime=System.currentTimeMillis();
    //Database.getDynamics().getQuestPlayerData().loadPerso(this);
    this.timeTaverne=timeDeblo;
    this._isForgetingSpell=false;
    try
    {
      String[] split=deadInformation.split(",");
      this.dead=Byte.parseByte(split[0]);
      this.deadTime=Long.parseLong(split[1]);
      this.deadType=Byte.parseByte(split[2]);
      this.killByTypeId=Long.parseLong(split[3]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    this.totalKills=totalKills;
    this.deathCount=deathCount;
    this.tokens=tokens;
   // this.apExo=apExo;
   // this.mpExo=mpExo;
    //this.raExo=raExo;
    if(World.get_Succes(id) == null) {
    	World.Succes.put(id, new Succes(id, 0, 0 , 0 , 0 , 0,0,0,"","",0,0,0,0,0,0,0,0,0));	
    	Database.getStatics().getSuccesData().add_succes(id,0, 0, 0, 0, 0,0,0,"","",0,0,0,0,0,0,0,0,0);
    }
    try
    {
      if(!emotes.isEmpty())
        for(String i : emotes.split(";"))
          this.addStaticEmote(Integer.parseInt(i));
      if(!morphMode.equals(""))
      {
        if(morphMode.equals("0"))
          morphMode="0;0";
        String[] i=morphMode.split(";");
        _morphMode=i[0].equals("1");
        if(!i[1].equals(""))
          _morphId=Integer.parseInt(i[1]);
      }
      if(_morphMode)
        this._saveSpellPts=pts;
      else
        this._spellPts=pts;
      if(prison!=0)
      {
        this.isInEnnemyFaction=true;
        this.enteredOnEnnemyFaction=prison;
      }
      this._showWings=this.get_align()!=0&&seeAlign==1;
      if(curMap==null&&Main.world.getMap((short)7411)!=null)
      {
        this.curMap=Main.world.getMap((short)7411);
        this.curCell=curMap.getCase(311);
      }
      else if(curMap==null&&Main.world.getMap((short)7411)==null)
      {
        Main.stop("Player1");
        return;
      }
      else if(curMap!=null)
      {
        this.curCell=curMap.getCase(cell);
        if(curCell==null)
        {
          this.curMap=Main.world.getMap((short)7411);
          this.curCell=curMap.getCase(311);
        }
      }
      if(!z.equalsIgnoreCase(""))
      {
        for(String str : z.split(","))
        {
          try
          {
            _zaaps.add(Short.parseShort(str));
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        }
      }
      if(!isNew&&(curMap==null||curCell==null))
      {
        Main.stop("Player2");
        return;
      }
      if(Reload_item)
      if(!stuff.equals(""))
      {
        if(stuff.charAt(stuff.length()-1)=='|')
          stuff=stuff.substring(0,stuff.length()-1);
        Database.getDynamics().getObjectData().load(stuff.replace("|",","));
      }
      for(String item : stuff.split("\\|"))
      {
        if(item.equals(""))
          continue;
        String[] infos=item.split(":");

        int guid=0;
        try
        {
          guid=Integer.parseInt(infos[0]);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          continue;
        }

        GameObject obj=World.getGameObject(guid);

        if(obj==null)
          continue;
        objects.put(obj.getGuid(),obj);
      }
      try
      {
        if(parcho!=null&&!parcho.equalsIgnoreCase(""))
          for(String stat : parcho.split(";"))
            if(!stat.equalsIgnoreCase(""))
              this.statsParcho.addOneStat(Integer.parseInt(stat.split(",")[0]),Integer.parseInt(stat.split(",")[1]));
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      try {
    	  if(rapid != null)
			for (String s : rapid.split(Pattern.quote("*"))) {
				if (s.isEmpty()) {
					continue;
				}
				String[] split = s.split(Pattern.quote("|"));
				int idSet = Integer.parseInt(split[0]);
				String nombreSet = split[1];
				int iconoSet = Integer.parseInt(split[2]);
				String dataSet = split[3];
				addSetRapido(idSet, nombreSet, iconoSet, dataSet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}			 this.ornement = ornement;
	      try {
	          if (!ListOrnements.isEmpty() || !ListOrnements.equalsIgnoreCase(""))
	              for (String i : ListOrnements.split(","))
	                  this.addOrnements(Integer.parseInt(i));


	      } catch (NullPointerException e) {
	      }
      if(!storeObjets.equals(""))
      {
        for(String _storeObjets : storeObjets.split("\\|"))
        {
          String[] infos=_storeObjets.split(",");
          int guid=0;
          int price=0;
          try
          {
            guid=Integer.parseInt(infos[0]);
            price=Integer.parseInt(infos[1]);
          }
          catch(Exception e)
          {
            e.printStackTrace();
            continue;
          }

          GameObject obj=World.getGameObject(guid);
          if(obj==null)
            continue;

          _storeItems.put(obj.getGuid(),price);
        }
      }
      this.maxPdv=(this.level-1)*5+55+getTotalStats().getEffect(Constant.STATS_ADD_VITA)+getTotalStats().getEffect(Constant.STATS_ADD_VIE);
      if(this.curPdv<=0)
        this.curPdv=1;
      String[] pdvv2 = null;
		if(pdvPer.contains(";")){
			pdvv2 = pdvPer.split(";");	
		}else{
			final String pdv1 = pdvPer+";0";
			pdvv2 = pdv1.split(";");
		}
		this.time_last_connecte = (long) Integer.parseInt(pdvv2[1]);
      if(Integer.parseInt(pdvv2[0])>100)
        this.curPdv=(this.maxPdv*100/100);
      else
        this.curPdv=(this.maxPdv*Integer.parseInt(pdvv2[0])/100);
      if(this.curPdv<=0)
        this.curPdv=1;
      parseSpells(spells);
      //Chargement des métiers
      if(!jobs.equals(""))
      {
        for(String aJobData : jobs.split(";"))
        {
          String[] infos=aJobData.split(",");
          try
          {
            int jobID=Integer.parseInt(infos[0]);
            long xp=Long.parseLong(infos[1]);
            Job m=Main.world.getMetier(jobID);
            JobStat SM=_metiers.get(learnJob(m));
            SM.addXp(this,xp);
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        }
      }
      if(this.energy==0)
        setGhost();
      else if(this.energy==-1)
        setFuneral();
      
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  //Clone double
  public Player(int id, String name, int groupe, int sexe, int classe, int color1, int color2, int color3, int level, int _size, int _gfxid, Map<Integer, Integer> stats, String stuff, int pdvPer, byte seeAlign, int mount, int alvl, byte alignement)
  {
	  
    this.id=id;
    this.name=name;
    this.groupe=Group.getGroupeById(groupe);
    this.sexe=sexe;
    this.classe=classe;
    this.color1=color1;
    this.color2=color2;
    this.color3=color3;
    this.level=level;
    this._aLvl=alvl;
    this._size=_size;
    this.gfxId=_gfxid;
    this.stats=new Stats(stats,true,this);
    this.changeName=false;
    this.set_isClone(true);
    /*if(!stuff.equals(""))
    {
      if(stuff.charAt(stuff.length()-1)=='|')
        stuff=stuff.substring(0,stuff.length()-1);
      Database.getDynamics().getObjectData().load(stuff.replace("|",","));
    }*/
    for(String item : stuff.split("\\|"))
    {
      if(item.equals(""))
        continue;
      String[] infos=item.split(":");
      int guid=Integer.parseInt(infos[0]);
      GameObject obj=World.getGameObject(guid);
      if(obj==null)
        continue;
      objects.put(obj.getGuid(),obj);
    }
    this.maxPdv=(this.level-1)*5+50+getStats().getEffect(Constant.STATS_ADD_VITA);
    this.curPdv=(this.maxPdv*pdvPer)/100;
    this._align=alignement;
    this._showWings=this.get_align()!=0&&seeAlign==1;
    if(mount!=-1)
      this._mount=Main.world.getMountById(mount);
    this._isForgetingSpell=false;
  }

  //v2.7 - Replaced String += with StringBuilder
  public static Player CREATE_PERSONNAGE(String name, int sexe, int classe, int color1, int color2, int color3, Account compte, int ornement, String ListOrnements)
  {
    StringBuilder z=new StringBuilder();
    if(Config.getInstance().allZaap)
    {
      for(Entry<Integer, Integer> i : Constant.ZAAPS.entrySet())
      {
        if(z.length()!=0)
          z.append(",");
        z.append(i.getKey());
      }
    }
    if(classe>12||classe<1)
      return null;
    if(sexe<0||sexe>1)
      return null;
    Player perso=new Player(Database.getStatics().getPlayerData().getNextId(),name,-1,sexe,classe,color1,color2,color3,(Config.getInstance().serverId==6 ? 10000000 : 0),((Config.getInstance().startLevel-1)),((Config.getInstance().startLevel-1)*5),10000,Config.getInstance().startLevel,Main.world.getPersoXpMin(Config.getInstance().startLevel),100,Integer.parseInt(classe+""+sexe),(byte)0,compte.getId(),new HashMap<Integer, Integer>(),(byte)1,(byte)0,(byte)0,"*#%!pi$:?",(Config.getInstance().startMap!=0 ? (short)Config.getInstance().startMap : Constant.getStartMap(classe)),(Config.getInstance().startCell!=0 ? (short)Config.getInstance().startCell : Constant.getStartCell(classe)),"","","100;0","",(Config.getInstance().startMap!=0 ? (short)Config.getInstance().startMap : Constant.getStartMap(classe))+","+(Config.getInstance().startCell!=0 ? (short)Config.getInstance().startCell : Constant.getStartCell(classe)),"",0,-1,0,0,0,z.toString(),(byte)0,0,"0;0","",(Config.getInstance().allEmote ? "0;1;2;3;4;5;6;7;8;9;10;11;12;13;14;15;16;17;18;19;20;21" : "0"),0,true,"118,0;119,0;123,0;124,0;125,0;126,0",0,false,"0,0,0,0",(byte)0,0,0,0,0,0,"",0,false,ornement, ListOrnements);
    perso.emotes.add(1);
    perso._sorts=Constant.getStartSorts(classe);
    for(int a=1;a<=perso.getLevel();a++)
      Constant.onLevelUpSpells(perso,a);
    perso._sortsPlaces=Constant.getStartSortsPlaces(classe);

    SocketManager.GAME_SEND_WELCOME(perso);

    if(!Database.getStatics().getPlayerData().add(perso))
      return null;
    Main.world.addPlayer(perso);
    if(Config.singleton.serverId != 6)
    for (final ObjectTemplate t : Main.world.getItemSet(5).getItemTemplates()) {
		final GameObject obj = t.createNewItem(1, true);
		if (perso.addObjet(obj, true)) {
			World.addGameObject(obj, true);
		}
	}
    if(Config.singleton.serverId == 8) {
    	GameObject obj2=Main.world.getObjTemplate(1711).createNewItem(1,true);
        if(perso.addObjet(obj2,true))
          World.addGameObject(obj2,true);
        obj2=Main.world.getObjTemplate(10922).createNewItem(1,true);
        if(perso.addObjet(obj2,true))
          World.addGameObject(obj2,true);
    }
    	
    if(Config.singleton.serverId == 6) {
         
    	int val = 101;
    	 for(int i=0;i<val;i++)
         {
    		perso.boostStat(11,false);
    		perso.getStatsParcho().addOneStat(Constant.STATS_ADD_VITA,1);
         }
    	 for(int i=0;i<val;i++)
         {
           perso.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE,1);
           perso.boostStat(12,false);
         }
    	 for(int i=0;i<val;i++)
         {
           perso.boostStat(10,false);
           perso.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC,1);
         }
    	for(int i=0;i<val;i++)
        {
          perso.boostStat(15,false);
          perso.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE,1);
        }
    	for(int i=0;i<val;i++)
        {
          perso.boostStat(13,false);
          perso.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN,1);
        }
    	for(int i=0;i<val;i++)
        {
          perso.boostStat(14,false);
          perso.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL,1);
        }	
    }
    int xp = 0;
    int i=0;
    int th = Constant.jobs.length;
    for(i=0;i<34;i++) { // 34 metiers
        //apprendre le metier
        perso.learnJob(Main.world.getMetier(Constant.jobs[i]));
        //Ajout outil du metier
        /*  ObjectTemplate t=Main.world.getObjTemplate(Constant.tools[i]);
          GameObject obj=t.createNewItem(1,false);
          if(perso.addObjet(obj,true))//Si le joueur n'avait pas d'item similaire
              World.addGameObject(obj,true);*/
           // String str="Creation de l'item "+jobs[i]+" reussie"; 
           }
    Database.getStatics().getPlayerData().update(perso);
    return perso;
  }

  public static String getCompiledEmote(List<Integer> i)
  {
    int i2=0;
    for(Integer b : i)
      i2+=(2<<(b-2));
    return i2+"|0";
  }

  //CLONAGE
  public static Player ClonePerso(Player P, int id, int pdv)
  {
    HashMap<Integer, Integer> stats=new HashMap<Integer, Integer>();
    stats.put(Constant.STATS_ADD_VITA,pdv);
    stats.put(Constant.STATS_ADD_FORC,P.getStats().getEffect(Constant.STATS_ADD_FORC));
    stats.put(Constant.STATS_ADD_SAGE,P.getStats().getEffect(Constant.STATS_ADD_SAGE));
    stats.put(Constant.STATS_ADD_INTE,P.getStats().getEffect(Constant.STATS_ADD_INTE));
    stats.put(Constant.STATS_ADD_CHAN,P.getStats().getEffect(Constant.STATS_ADD_CHAN));
    stats.put(Constant.STATS_ADD_AGIL,P.getStats().getEffect(Constant.STATS_ADD_AGIL));
    stats.put(Constant.STATS_ADD_PA,P.getStats().getEffect(Constant.STATS_ADD_PA));
    stats.put(Constant.STATS_ADD_PM,P.getStats().getEffect(Constant.STATS_ADD_PM));
    stats.put(Constant.STATS_ADD_RP_NEU,P.getStats().getEffect(Constant.STATS_ADD_RP_NEU) <= 50  ?P.getStats().getEffect(Constant.STATS_ADD_RP_NEU) : 50);
    stats.put(Constant.STATS_ADD_RP_TER,P.getStats().getEffect(Constant.STATS_ADD_RP_TER) <= 50  ?P.getStats().getEffect(Constant.STATS_ADD_RP_TER): 50);
    stats.put(Constant.STATS_ADD_RP_FEU,P.getStats().getEffect(Constant.STATS_ADD_RP_FEU) <= 50  ?P.getStats().getEffect(Constant.STATS_ADD_RP_FEU): 50);
    stats.put(Constant.STATS_ADD_RP_EAU,P.getStats().getEffect(Constant.STATS_ADD_RP_EAU) <= 50  ?P.getStats().getEffect(Constant.STATS_ADD_RP_EAU): 50);
    stats.put(Constant.STATS_ADD_RP_AIR,P.getStats().getEffect(Constant.STATS_ADD_RP_AIR) <= 50  ?P.getStats().getEffect(Constant.STATS_ADD_RP_AIR): 50);
    stats.put(Constant.STATS_ADD_AFLEE,P.getStats().getEffect(Constant.STATS_ADD_AFLEE));
    stats.put(Constant.STATS_ADD_MFLEE,P.getStats().getEffect(Constant.STATS_ADD_MFLEE));

    byte showWings=0;
    int alvl=0;
    if(P.get_align()!=0&&P._showWings)
    {
      showWings=1;
      alvl=P.getGrade();
    }
    int mountID=-1;
    if(P.getMount()!=null)
      mountID=P.getMount().getId();

    Player Clone=new Player(id,P.getName(),(P.getGroupe()!=null) ? P.getGroupe().getId() : -1,P.getSexe(),P.getClasse(),P.getColor1(),P.getColor2(),P.getColor3(),P.getLevel(),100,P.getGfxId(),stats,"",100,showWings,mountID,alvl,P.get_align());
    Clone.objects=new HashMap<>();
    Clone.objects.putAll(P.objects);
    Clone.set_isClone(true);
    if(P._onMount)
      Clone._onMount=true;
    return Clone;
  }

  public int getId()
  {
    return this.id;
  }

  public String getName()
  {
    return this.name;
  }

  public void setName(String name)
  {
    this.name=name;
    this.changeName=false;
    Database.getStatics().getPlayerData().updateInfos(this);
    if(this.getGuildMember()!=null)
      Database.getDynamics().getGuildMemberData().update(this);
  }

  public Group getGroupe()
  {
    return this.groupe;
  }
  public void setColor1(int color1) {
	this.color1 = color1;
}
  public void setColor2(int color2) {
	this.color2 = color2;
}
  public void setColor3(int color3) {
	this.color3 = color3;
}
  public Map<Integer, GameObject> getObjects() {
	return objects;
}
  public void setGroupe(Group groupe, boolean reload)
  {
    this.groupe=groupe;
    if(reload)
      Database.getStatics().getPlayerData().updateGroupe(this);
  }

  public boolean isInvisible()
  {
    return this.isInvisible;
  }

  public void setInvisible(boolean b)
  {
    this.isInvisible=b;
  }

  public int getSexe()
  {
    return this.sexe;
  }

  public int getJoindelay() {
		return joindelay;
	}

	public void setJoindelay(int joindelay) {
		this.joindelay = joindelay;
	}
  public void setSexe(int sexe)
  {
    this.sexe=sexe;
    this.setGfxId(10*this.getClasse()+this.sexe);
  }

  public int getClasse()
  {
    return this.classe;
  }

  public int getColor1()
  {
    return this.color1;
  }

  public int getColor2()
  {
    return this.color2;
  }

  public int getColor3()
  {
    return this.color3;
  }
  public int getSend_notif_bot() {
		return send_notif_bot;
	}

	public void setSend_notif_bot(int send_notif_bot) {
		this.send_notif_bot = send_notif_bot;
	}


  public Player getSpioned_by() {
	  if(Spioned_by != null && !Spioned_by.isOnline)
		  Spioned_by = null;
	return Spioned_by;
  }

  public void setSpioned_by(Player spioned_by) {
	Spioned_by = spioned_by;
  }
  public int getLevel()
  {
    return this.level;
  }

  public void setLevel(int level)
  {
    this.level=level;
  }
  public GameAction get_gameAction_rapide() {
	return _gameAction_rapide;
}

public void set_gameAction_rapide(GameAction _gameAction_rapide) {
	this._gameAction_rapide = _gameAction_rapide;
}

  public boolean isCanDrop_ressources() {
		return canDrop_ressources;
	}

	public void setCanDrop_ressources(boolean canDrop_ressources) {
		this.canDrop_ressources = canDrop_ressources;
	}

	public boolean isCanDrop_items() {
		return canDrop_items;
	}

	public void setCanDrop_items(boolean canDrop_items) {
		this.canDrop_items = canDrop_items;
	}

  public int getEnergy()
  {
    return this.energy;
  }

  public void setEnergy(int energy)
  {
	  if(Config.singleton.HEROIC)
    this.energy=energy;
  }

  public long getExp()
  {
    return this.exp;
  }

  public void setExp(long exp)
  {
    this.exp=exp;
  }

  public int getCurPdv()
  {
    refreshLife(false);
    return this.curPdv;
  }

  public void setPdv(int pdv)
  {
    this.curPdv=pdv;
    if(this.curPdv>this.maxPdv)
      this.curPdv=this.maxPdv;
    if(party!=null)
      SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(party,this);
  }
  public int getTotal_combat() {
	return total_combat;
}

public void setTotal_combat() {
	this.total_combat += 1;
}
public int getTotal_reculte() {
	return total_reculte;
}

public void setTotal_reculte() {
	this.total_reculte += 1;
}
  public int getMaxPdv()
  {
    return this.maxPdv;
  }

  public void setMaxPdv(int maxPdv)
  {
    this.maxPdv=maxPdv;
    SocketManager.GAME_SEND_STATS_PACKET(this);
    if(party!=null)
      SocketManager.GAME_SEND_PM_MOD_PACKET_TO_GROUP(party,this);
  }

  public Stats getStats()
  {
    if(useStats)
      return newStatsMorph();
    else
      return this.stats;
  }

  public Stats getStatsParcho()
  {
    return statsParcho;
  }

  //v2.7 - Replaced String += with StringBuilder
  public String parseStatsParcho()
  {
    StringBuilder parcho=new StringBuilder();
    for(Entry<Integer, Integer> i : statsParcho.getMap().entrySet())
      parcho.append(parcho.toString().isEmpty() ? i.getKey()+","+i.getValue() : ";"+i.getKey()+","+i.getValue());
    return parcho.toString();
  }

  public boolean getDoAction()
  {
    return doAction;
  }

  public void setDoAction(boolean b)
  {
    doAction=b;
  }

  public void setRoleplayBuff(int id)
  {
    int objTemplate=0;
    switch(id)
    {
      case 10673:
        objTemplate=10844;
        break;
      case 10669:
        objTemplate=10681;
        break;
    }
    if(objTemplate==0)
      return;
    if(getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
    {
      int guid=getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getGuid();
      SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,guid);
      this.deleteItem(guid);
    }

    GameObject obj=Main.world.getObjTemplate(objTemplate).createNewRoleplayBuff();
    this.addObjet(obj,false);
    World.addGameObject(obj,true);
    SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(),this);
    SocketManager.GAME_SEND_Ow_PACKET(this);
    SocketManager.GAME_SEND_STATS_PACKET(this);
   // Database.getStatics().getPlayerData().update(this);
  }

  public void setBenediction(int id)
  {
    if(getObjetByPos(Constant.ITEM_POS_BENEDICTION)!=null)
    {
      int guid=getObjetByPos(Constant.ITEM_POS_BENEDICTION).getGuid();
      SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,guid);
      this.deleteItem(guid);
    }
    if(id==0)
    {
      SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(),this);
      return;
    }
    int turn=0;
    switch(id)
    {
      case 10682:
        turn=20;
        break;
      default:
        turn=1;
        break;
    }

    GameObject obj=Main.world.getObjTemplate(id).createNewBenediction(turn);
    this.addObjet(obj,false);
    World.addGameObject(obj,true);
    SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(),this);
    SocketManager.GAME_SEND_Ow_PACKET(this);
    SocketManager.GAME_SEND_STATS_PACKET(this);
    //Database.getStatics().getPlayerData().update(this);
  }

  public void setMalediction(int id)
  {
    int objTemplate=0;
    switch(id)
    {
      case 10827:
        objTemplate=10838;
        break;
      default:
        objTemplate=id;
    }
    if(objTemplate==0)
    {
      SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(),this);
      return;
    }
    if(getObjetByPos(Constant.ITEM_POS_MALEDICTION)!=null)
    {
      int guid=getObjetByPos(Constant.ITEM_POS_MALEDICTION).getGuid();
      SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,guid);
      this.deleteItem(guid);
    }

    GameObject obj=Main.world.getObjTemplate(objTemplate).createNewMalediction();
    this.addObjet(obj,false);
    World.addGameObject(obj,true);
    if(this.getFight()!=null)
    {
      SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(),this);
      SocketManager.GAME_SEND_Ow_PACKET(this);
      SocketManager.GAME_SEND_STATS_PACKET(this);
     // Database.getStatics().getPlayerData().update(this);
    }
  }

  public void setMascotte(int id)
  {
    if(getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR)!=null)
    {
      int guid=getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR).getGuid();
      SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,guid);
      this.deleteItem(guid);
    }
    if(id==0)
    {
      SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(),this);
      return;
    }

    GameObject obj=Main.world.getObjTemplate(id).createNewFollowPnj(1);
    if(obj!=null)
      if(this.addObjet(obj,false))
        World.addGameObject(obj,true);

    SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(),this);
    SocketManager.GAME_SEND_Ow_PACKET(this);
    SocketManager.GAME_SEND_STATS_PACKET(this);
   // Database.getStatics().getPlayerData().update(this);
  }

  public void setCandy(int id)
  {
    if(getObjetByPos(Constant.ITEM_POS_BONBON)!=null)
    {
      int guid=getObjetByPos(Constant.ITEM_POS_BONBON).getGuid();
      SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,guid);
      this.deleteItem(guid);
    }
    int turn=30;
    switch(id)
    {
      case 8948:
      case 8949:
      case 8950:
      case 8951:
      case 8952:
      case 8953:
      case 8954:
      case 8955:
        turn=5;
        break;
      case 7042:
         turn=10;
         break;
      case 10665:
        turn=20;
        break;
      default:
        turn=30;
        break;
    }

    GameObject obj=Main.world.getObjTemplate(id).createNewCandy(turn);
    this.addObjet(obj,false);
    World.addGameObject(obj,true);
    SocketManager.GAME_SEND_Ow_PACKET(this);
    SocketManager.GAME_SEND_STATS_PACKET(this);
    //Database.getStatics().getPlayerData().update(this);
  }

  public void calculTurnCandy()
  {
    GameObject obj=getObjetByPos(Constant.ITEM_POS_BONBON);
    if(obj != null)
    obj.setModification();	
    if(obj!=null)
    {
      obj.getStats().addOneStat(Constant.STATS_TURN,-1);
      if(obj.getStats().getEffect(Constant.STATS_TURN)<=0)
      {
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,obj.getGuid());
        this.deleteItem(obj.getGuid());
      }
      else
      {
        SocketManager.GAME_SEND_UPDATE_ITEM(this,obj);
      }
      //Database.getDynamics().getObjectData().update(obj);
    }
    obj=getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR);
    if(obj!=null)
    {
      obj.getStats().addOneStat(Constant.STATS_TURN,-1);
      if(obj.getStats().getEffect(Constant.STATS_TURN)<=0)
      {
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,obj.getGuid());
        this.deleteItem(obj.getGuid());
      }
      else
      {
        SocketManager.GAME_SEND_UPDATE_ITEM(this,obj);
      }
      //Database.getDynamics().getObjectData().update(obj);
    }
    obj=getObjetByPos(Constant.ITEM_POS_BENEDICTION);
    if(obj!=null)
    {
      obj.getStats().addOneStat(Constant.STATS_TURN,-1);
      if(obj.getStats().getEffect(Constant.STATS_TURN)<=0)
      {
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,obj.getGuid());
        this.deleteItem(obj.getGuid());
      }
      else
      {
        SocketManager.GAME_SEND_UPDATE_ITEM(this,obj);
      }
      //Database.getDynamics().getObjectData().update(obj);
    }
    obj=getObjetByPos(Constant.ITEM_POS_MALEDICTION);
    if(obj!=null)
    {
      obj.getStats().addOneStat(Constant.STATS_TURN,-1);
      if(obj.getStats().getEffect(Constant.STATS_TURN)<=0)
      {
        gfxId=getClasse()*10+getSexe();
        if(this.getFight()==null)
          SocketManager.GAME_SEND_ALTER_GM_PACKET(getCurMap(),this);
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,obj.getGuid());
        switch(obj.getTemplate().getId())
        {
          case 8169:
          case 8170:
            unsetFullMorph();
            break;
        }

        this.deleteItem(obj.getGuid());
      }
      else
      {
        SocketManager.GAME_SEND_UPDATE_ITEM(this,obj);
      }
     // Database.getDynamics().getObjectData().update(obj);
    }
    obj=getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF);
    if(obj!=null)
    {
      obj.getStats().addOneStat(Constant.STATS_TURN,-1);
      if(obj.getStats().getEffect(Constant.STATS_TURN)<=0)
      {
        gfxId=getClasse()*10+getSexe();
        SocketManager.GAME_SEND_ALTER_GM_PACKET(getCurMap(),this);
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,obj.getGuid());
        this.deleteItem(obj.getGuid());
      }
      else
      {
        SocketManager.GAME_SEND_UPDATE_ITEM(this,obj);
      }
     // Database.getDynamics().getObjectData().update(obj);
    }
  }

  public boolean isSpec()
  {
    return _spec;
  }

  public void setSpec(boolean s)
  {
    this._spec=s;
  }

  //v2.1 - alltitle nullpointerexception fix
  public String getAllTitle()
  {
  //  _allTitle=Database.getStatics().getPlayerData().loadTitles(this.getId());
   // if(_allTitle!=null)
   //   return _allTitle;
   // else
     return "";
  }

  public void setAllTitle(String title)
  {
    getAllTitle();
    boolean erreur=false;
    if(title.equals(""))
      title="0";
    if(_allTitle!=null&&!_allTitle.isEmpty())
      for(String i : _allTitle.split(","))
        if(i.equals(title))
          erreur=true;
    if((_allTitle==null&&!erreur)||_allTitle.isEmpty())
      _allTitle=title;
    else if(!erreur)
      _allTitle+=","+title;
    Database.getStatics().getPlayerData().updateTitles(this.getId(),_allTitle);
  }

  public void setSpells(Map<Integer, Spell.SortStats> spells)
  {
    _sorts.clear();
    _sortsPlaces.clear();
    _sorts=spells;
    _sortsPlaces=Constant.getStartSortsPlaces(this.getClasse());
  }

  public void teleportOld()
  {
    this.teleport(this.getOldMap(),this.getOldCell());
  }

  public short getOldMap()
  {
    return oldMap;
  }
  public void setOldPosition() {
      this.oldMap = this.getCurMap().getId();
      this.oldCell = this.getCurCell().getId();
  }

  public void setOldMap(short i)
  {
    oldMap=i;
  }

  public int getOldCell()
  {
    return oldCell;
  }

  public void setOldCell(int i)
  {
    oldCell=i;
  }

  public void setOnline(boolean isOnline)
  {
    this.isOnline=isOnline;
  }

  public boolean isOnline()
  {
    return isOnline;
  }

  public Party getParty()
  {
    return party;
  }

  public void setParty(Party party)
  {
    this.party=party;
  }

  public String parseSpellToDB()
  {
    StringBuilder sorts=new StringBuilder();

    if(_morphMode)
    {
      if(_saveSorts.isEmpty())
        return "";
      for(int key : _saveSorts.keySet())
      {
        //3;1;a,4;3;b
        Spell.SortStats SS=_saveSorts.get(key);
        if(SS==null)
          continue;
        sorts.append(SS.getSpellID()).append(";").append(SS.getLevel()).append(";");
        if(_saveSortsPlaces.get(key)!=null)
          sorts.append(_saveSortsPlaces.get(key));
        else
          sorts.append("_");
        sorts.append(",");
      }
    }
    else
    {
      if(_sorts.isEmpty())
        return "";
      for(int key : _sorts.keySet())
      {
        //3;1;a,4;3;b
        Spell.SortStats SS=_sorts.get(key);
        if(SS==null)
          continue;
        sorts.append(SS.getSpellID()).append(";").append(SS.getLevel()).append(";");
        if(_sortsPlaces.get(key)!=null)
          sorts.append(_sortsPlaces.get(key));
        else
          sorts.append("_");
        sorts.append(",");
      }
    }
    return sorts.substring(0,sorts.length()-1);
  }

  private void parseSpells(String str)
  {
    if(!str.equalsIgnoreCase(""))
    {
      if(_morphMode)
      {
        String[] spells=str.split(",");
        _saveSorts.clear();
        _saveSortsPlaces.clear();
        for(String e : spells)
        {
          try
          {
            int id=Integer.parseInt(e.split(";")[0]);
            int lvl=Integer.parseInt(e.split(";")[1]);
            char place=e.split(";")[2].charAt(0);
            learnSpell(id,lvl);
            this._saveSortsPlaces.put(id,place);
          }
          catch(NumberFormatException e1)
          {
            e1.printStackTrace();
          }
        }
      }
      else
      {
        String[] spells=str.split(",");
        _sorts.clear();
        _sortsPlaces.clear();
        for(String e : spells)
        {
          try
          {
            int id=Integer.parseInt(e.split(";")[0]);
            int lvl=Integer.parseInt(e.split(";")[1]);
            char place=e.split(";")[2].charAt(0);
            if(!_morphMode)
              learnSpell(id,lvl,false,false,false);
            else
              learnSpell(id,lvl,false,true,false);
            _sortsPlaces.put(id,place);
          }
          catch(NumberFormatException e1)
          {
            e1.printStackTrace();
          }
        }
      }
    }
  }

  private void parseSpellsFullMorph(String str)
  {
    String[] spells=str.split(",");
    _sorts.clear();
    _sortsPlaces.clear();
    for(String e : spells)
    {
      try
      {
        int id=Integer.parseInt(e.split(";")[0]);
        int lvl=Integer.parseInt(e.split(";")[1]);
        char place=e.split(";")[2].charAt(0);
        if(!_morphMode)
          learnSpell(id,lvl,false,false,false);
        else
          learnSpell(id,lvl,false,true,false);
        _sortsPlaces.put(id,place);
      }
      catch(NumberFormatException e1)
      {
        e1.printStackTrace();
      }
    }
  }

  public String getSavePosition()
  {
    return _savePos;
  }

  public void set_savePos(String savePos)
  {
    _savePos=savePos;
  }

  public long getKamas()
  {
    return kamas;
  }

  public void setKamas(long l)
  {
    this.kamas=l;
  }

  public void addKamas(long l, boolean ipkamas)
  {
	  if(ipkamas)
	  if(this.party != null && this.party.getMaster() != null && this.party.getMaster().id != this.id &&this.party.getMaster().ipKamas == true) {
		  this.party.getMaster().kamas+=l;
		  return;
	  }

    kamas+=l;

  }

  public Map<Integer, SpellEffect> get_buff()
  {
    return buffs;
  }

  public Account getAccount()
  {
    return account;
  }

  public void setAccount(Account c)
  {
    account=c;
  }

  public int get_spellPts()
  {
    if(_morphMode)
      return _saveSpellPts;
    else
      return _spellPts;
  }

  public void set_spellPts(int pts)
  {
    if(_morphMode)
      _saveSpellPts=pts;
    else
      _spellPts=pts;
  }

  public Guild get_guild()
  {
    if(_guildMember==null)
      return null;
    return _guildMember.getGuild();
  }

  public void setChangeName(boolean changeName)
  {
    this.changeName=changeName;
    if(changeName)
      this.send("AlEr");
  }

  public boolean isChangeName()
  {
    return changeName;
  }

  public boolean isReady()
  {
    return ready;
  }

  public void setReady(boolean ready)
  {
    this.ready=ready;
  }

  public int getDuelId()
  {
    return duelId;
  }

  public void setDuelId(int _duelid)
  {
    duelId=_duelid;
  }

  public Fight getFight()
  {
    return fight;
  }

  public void setFight(Fight fight)
  {
    refreshLife(false);
    if(fight==null)
      SocketManager.send(this,"ILS2000");
    else
      SocketManager.send(this,"ILF0");
    this.sitted=false;
    this.fight=fight;
  }

  public boolean is_showFriendConnection()
  {
    return _showFriendConnection;
  }

  public boolean is_showWings()
  {
    return _showWings;
  }
  public boolean isShowSeller()
  {
    return _seeSeller;
  }

  public void setShowSeller(boolean is)
  {
    _seeSeller=is;
  }

  public String get_canaux()
  {
    return _canaux;
  }

  public GameCase getCurCell()
  {
    return curCell;
  }

  public void setCurCell(GameCase cell)
  {
    curCell=cell;
  }

  public int get_size()
  {
    return _size;
  }

  public void set_size(int _size)
  {
    this._size=_size;
  }

  public int getGfxId()
  {
    return gfxId;
  }

  public void setGfxId(int _gfxid)
  {
    if(this.getClasse()*10+this.getSexe()!=_gfxid)
    {
      if(this.isOnMount())
        this.toogleOnMount();
      this.send("AR3K");
    }
    else
    {
      this.send("AR6bK");
    }
    gfxId=_gfxid;
  }

  public boolean isMorphMercenaire()
  {
    return (this.gfxId==8009||this.gfxId==8006);
  }

  public GameMap getCurMap()
  {
    return curMap;
  }

  public void setCurMap(GameMap curMap)
  {
    this.curMap=curMap;
  }

  public boolean isAway()
  {
    return away;
  }

  public void setAway(boolean away)
  {
    if(this.away!=away)
    {
      this.away=away;
    }
  }

  public boolean isSitted()
  {
    return sitted;
  }

  public void setSitted(boolean sitted)
  {
    if(this.sitted==sitted)
    {
      return;
    }
    this.sitted=sitted;
    refreshLife(false);
    regenRate=(sitted ? 1000 : 2000);
    SocketManager.send(this,"ILS"+regenRate);
  }

  public int get_capital()
  {
    return _capital;
  }

  public void setSpellsPlace(boolean ok)
  {
    if(ok)
      _sortsPlaces=Constant.getStartSortsPlaces(this.getClasse());
    else
      _sortsPlaces.clear();
    SocketManager.GAME_SEND_SPELL_LIST(this);
  }

  public void learnSpell(int spell, int level, char pos)
  {
    if(Main.world.getSort(spell).getStatsByLevel(level)==null)
    {
      return;
    }

    if(!_sorts.containsKey(Integer.valueOf(spell)))
    {
      _sorts.put(Integer.valueOf(spell),Main.world.getSort(spell).getStatsByLevel(level));
      replace_SpellInBook(pos);
      _sortsPlaces.remove(spell);
      _sortsPlaces.put(spell,pos);
      SocketManager.GAME_SEND_SPELL_LIST(this);
      SocketManager.GAME_SEND_Im_PACKET(this,"03;"+spell);
    }
  }

  public boolean learnSpell(int spellID, int level, boolean save, boolean send, boolean learn)
  {
    if(Main.world.getSort(spellID).getStatsByLevel(level)==null)
    {
      return false;
    }

    if(_sorts.containsKey(Integer.valueOf(spellID))&&learn)
    {
      SocketManager.GAME_SEND_MESSAGE(this,"Vous Avez déjé ce sort.");
      return false;
    }
    else
    {
      _sorts.put(Integer.valueOf(spellID),Main.world.getSort(spellID).getStatsByLevel(level));
      if(send)
      {
        SocketManager.GAME_SEND_SPELL_LIST(this);
        SocketManager.GAME_SEND_Im_PACKET(this,"03;"+spellID);
      }
     // if(save)
     //   Database.getStatics().getPlayerData().update(this);
      return true;
    }
  }

  public boolean learnSpell(int spellID, int level)
  {
    if(Main.world.getSort(spellID).getStatsByLevel(level)==null)
    {
      return false;
    }

    if(_saveSorts.containsKey(Integer.valueOf(spellID)))
    {
      return false;
    }
    else
    {
      _saveSorts.put(Integer.valueOf(spellID),Main.world.getSort(spellID).getStatsByLevel(level));
      return true;
    }
  }

  public boolean unlearnSpell(int spell)
  {
    if(Main.world.getSort(spell)==null)
    {
      return false;
    }

    _sorts.remove(spell);
    this._sortsPlaces.remove(spell);
    SocketManager.GAME_SEND_SPELL_LIST(this);
    SocketManager.GAME_SEND_STATS_PACKET(this);
   // Database.getStatics().getPlayerData().update(this);
    return true;
  }

  public boolean unlearnSpell(Player perso, int spellID, int level, int ancLevel, boolean save, boolean send)
  {
    int spellPoint=1;
    if(ancLevel==2)
      spellPoint=1;
    if(ancLevel==3)
      spellPoint=2+1;
    if(ancLevel==4)
      spellPoint=3+3;
    if(ancLevel==5)
      spellPoint=4+6;
    if(ancLevel==6)
      spellPoint=5+10;

    if(Main.world.getSort(spellID).getStatsByLevel(level)==null)
    {
      return false;
    }

    _sorts.put(Integer.valueOf(spellID),Main.world.getSort(spellID).getStatsByLevel(level));
    if(send)
    {
      SocketManager.GAME_SEND_SPELL_LIST(this);
      SocketManager.GAME_SEND_Im_PACKET(this,"0154;"+"<b>"+ancLevel+"</b>"+"~"+"<b>"+spellPoint+"</b>");
      addSpellPoint(spellPoint);
      SocketManager.GAME_SEND_STATS_PACKET(perso);
    }
    //if(save)
     // Database.getStatics().getPlayerData().update(this);
    return true;
  }

  public boolean boostSpell(int spellID)
  {
    if(getSortStatBySortIfHas(spellID)==null)
      return false;
    int AncLevel=getSortStatBySortIfHas(spellID).getLevel();
    if(AncLevel==6)
      return false;
    if(_spellPts>=AncLevel&&Main.world.getSort(spellID).getStatsByLevel(AncLevel+1).getReqLevel()<=this.getLevel())
    {
      if(learnSpell(spellID,AncLevel+1,true,false,false))
      {
        _spellPts-=AncLevel;
        //Database.getStatics().getPlayerData().update(this);
        return true;
      }
      else
      {
        return false;
      }
    }
    else
    //Pas le niveau ou pas les Points
    {
      if(_spellPts<AncLevel)
        if(Main.world.getSort(spellID).getStatsByLevel(AncLevel+1).getReqLevel()>this.getLevel())
          return false;
    }
    return away;
  }
  public boolean boostSpellpvp(int spellID)
  {
    if(getSortStatBySortIfHas(spellID)==null)
      return false;
    int AncLevel=getSortStatBySortIfHas(spellID).getLevel();
    if(AncLevel==6)
      return false;
    if(Main.world.getSort(spellID).getStatsByLevel(AncLevel+1).getReqLevel()<=this.getLevel())
    {
      if(learnSpell(spellID,AncLevel+1,true,false,false))
      {
        //Database.getStatics().getPlayerData().update(this);
        return true;
      }
      else
      {
        return false;
      }
    }
    else
    //Pas le niveau ou pas les Points
    {
      if(_spellPts<AncLevel)
        if(Main.world.getSort(spellID).getStatsByLevel(AncLevel+1).getReqLevel()>this.getLevel())
          return false;
    }
    return away;
  }

  public void boostSpellIncarnation()
  {
    for(Entry<Integer, Spell.SortStats> i : _sorts.entrySet())
    {
      if(getSortStatBySortIfHas(i.getValue().getSpell().getSpellID())==null)
        continue;
      learnSpell(i.getValue().getSpell().getSpellID(),i.getValue().getLevel()+1,true,false,false);
       // Database.getStatics().getPlayerData().update(this);
    }
  }

  public boolean forgetSpell(int spellID)
  {
    if(getSortStatBySortIfHas(spellID)==null)
    {
      return false;
    }
    int AncLevel=getSortStatBySortIfHas(spellID).getLevel();
    if(AncLevel<=1)
      return false;

    if(learnSpell(spellID,1,true,false,false))
    {
      _spellPts+=Formulas.spellCost(AncLevel);
      //Database.getStatics().getPlayerData().update(this);
      return true;
    }
    else
    {
      return false;
    }
  }

  public void demorph()
  {
    if(this.getMorphMode())
    {
      int morphID=this.getClasse()*10+this.getSexe();
      this.setGfxId(morphID);
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getCurMap(),this.getId());
      SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.getCurMap(),this);
    }
  }

  public boolean getMorphMode()
  {
    return _morphMode;
  }

  public int getMorphId()
  {
    return _morphId;
  }

  public void setMorphId(int id)
  {
    this._morphId=id;
  }

  public void setFullMorph(int morphid, boolean isLoad, boolean join)
  {
    if(this.isOnMount())
      this.toogleOnMount();
    if(_morphMode&&!join)
      unsetFullMorph();
    if(this.isGhost)
    {
      SocketManager.send(this,"Im1185");
      return;
    }

    Map<String, String> fullMorph=Main.world.getFullMorph(morphid);

    if(fullMorph==null)
      return;

    if(!join)
    {
      if(!_morphMode)
      {
        _saveSpellPts=_spellPts;
        _saveSorts.putAll(_sorts);
        _saveSortsPlaces.putAll(_sortsPlaces);
      }
      if(isLoad)
      {
        _saveSpellPts=_spellPts;
        _saveSorts.putAll(_sorts);
        _saveSortsPlaces.putAll(_sortsPlaces);
      }
    }

    _morphMode=true;
    _sorts.clear();
    _sortsPlaces.clear();
    _spellPts=0;

    setGfxId(Integer.parseInt(fullMorph.get("gfxid")));
    if(this.fight==null)
      SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(),this);
    parseSpellsFullMorph(fullMorph.get("spells"));
    setMorphId(morphid);

    if(this.getObjetByPos(Constant.ITEM_POS_ARME)!=null)
      if(Constant.isIncarnationWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId()))
        for(int i=0;i<=this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);i++)
          if(i==10||i==20||i==30||i==40||i==50)
            boostSpellIncarnation();
    if(this.fight==null)
    {
      SocketManager.GAME_SEND_ASK(this.getGameClient(),this);
      SocketManager.GAME_SEND_SPELL_LIST(this);
    }

    if(fullMorph.get("vie")!=null)
    {
      try
      {
        this.maxPdv=Integer.parseInt(fullMorph.get("vie"));
        this.setPdv(this.getMaxPdv());
        this.pa=Integer.parseInt(fullMorph.get("pa"));
        this.pm=Integer.parseInt(fullMorph.get("pm"));
        this.vitalite=Integer.parseInt(fullMorph.get("vitalite"));
        this.sagesse=Integer.parseInt(fullMorph.get("sagesse"));
        this.terre=Integer.parseInt(fullMorph.get("terre"));
        this.feu=Integer.parseInt(fullMorph.get("feu"));
        this.eau=Integer.parseInt(fullMorph.get("eau"));
        this.air=Integer.parseInt(fullMorph.get("air"));
        this.initiative=Integer.parseInt(fullMorph.get("initiative")+this.sagesse+this.terre+this.feu+this.eau+this.air);
        this.useStats=fullMorph.get("stats").equals("1");
        this.donjon=fullMorph.get("donjon").equals("1");
        this.useCac=false;
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }

    if(this.fight==null)
      SocketManager.GAME_SEND_STATS_PACKET(this);
    //if(!join)
    //  Database.getStatics().getPlayerData().update(this);
  }

  public boolean isMorph()
  {
    return (this.gfxId!=(this.getClasse()*10+this.getSexe()));
  }

  public boolean canCac()
  {
    return this.useCac;
  }

  public void unsetMorph()
  {
    this.setGfxId(this.getClasse()*10+this.getSexe());
    SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap,this);
    //Database.getStatics().getPlayerData().update(this);
  }

  public void unsetFullMorph()
  {
    if(!_morphMode)
      return;

    int morphID=this.getClasse()*10+this.getSexe();
    setGfxId(morphID);

    useStats=false;
    donjon=false;
    _morphMode=false;
    this.useCac=true;
    _sorts.clear();
    _sortsPlaces.clear();
    _spellPts=_saveSpellPts;
    _sorts.putAll(_saveSorts);
    _sortsPlaces.putAll(_saveSortsPlaces);
    parseSpells(parseSpellToDB());

    setMorphId(0);
    if(this.getFight()==null)
    {
      SocketManager.GAME_SEND_SPELL_LIST(this);
      SocketManager.GAME_SEND_STATS_PACKET(this);
      SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap,this);
    }
    //Database.getStatics().getPlayerData().update(this);
  }

  public String parseSpellList()
  {
    StringBuilder packet=new StringBuilder();
    packet.append("SL");
    for(Iterator<Spell.SortStats> i=_sorts.values().iterator();i.hasNext();)
    {
      Spell.SortStats SS=i.next();
      packet.append(SS.getSpellID()).append("~").append(SS.getLevel()).append("~").append(_sortsPlaces.get(SS.getSpellID())).append(";");
    }
    return packet.toString();
  }

  public void set_SpellPlace(int SpellID, char Place)
  {
    replace_SpellInBook(Place);
    _sortsPlaces.remove(SpellID);
    _sortsPlaces.put(SpellID,Place);
   // Database.getStatics().getPlayerData().update(this);
  }

  private void replace_SpellInBook(char Place)
  {
    for(int key : _sorts.keySet())
      if(_sortsPlaces.get(key)!=null)
        if(_sortsPlaces.get(key).equals(Place))
          _sortsPlaces.remove(key);
  }

  public Spell.SortStats getSortStatBySortIfHas(int spellID)
  {
    return _sorts.get(spellID);
  }

  public String parseALK()
  {
    StringBuilder perso=new StringBuilder();
    perso.append("|");
    perso.append(this.getId()).append(";");
    perso.append(this.getName()).append(";");
    perso.append(this.getLevel()).append(";");
    int gfx=this.gfxId;
    if(this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
      if(this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId()==10681)
        gfx=8037;
    perso.append(gfx).append(";");
    int color1=this.getColor1(),color2=this.getColor2(),color3=this.getColor3();
    if(this.getObjetByPos(Constant.ITEM_POS_MALEDICTION)!=null)
      if(this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId()==10838)
      {
        color1=16342021;
        color2=16342021;
        color3=16342021;
      }
    perso.append((color1!=-1 ? Integer.toHexString(color1) : "-1")).append(";");
    perso.append((color2!=-1 ? Integer.toHexString(color2) : "-1")).append(";");
    perso.append((color3!=-1 ? Integer.toHexString(color3) : "-1")).append(";");
    perso.append(getGMStuffString()).append(";");
    perso.append((this.isShowSeller() ? 1 : 0)).append(";");
    perso.append(Config.getInstance().serverId).append(";");

    if(this.dead==1&&Config.getInstance().HEROIC)
    {
      perso.append(this.dead).append(";").append(this.deathCount);
    }
    else
    {
      perso.append(0);
    }
    return perso.toString();
  }

  public void remove()
  {
    Database.getStatics().getPlayerData().delete(this);
  }

  //v2.0 - Removed voting message
  public void OnJoinGame()
  {
	  try
	  {
				//if(getUrlContents("https://proxy.mind-media.com/block/proxycheck.php?ip="+this.account.getCurrentIp()).contains("Y")){
				//	this.getGameClient().kick();
				//	return;
				//}
                     

    this.account.setCurrentPlayer(this);
    this.setOnline(true);
    if(this.account.getGameClient()==null)
      return;
    GameClient client=this.account.getGameClient();
    client.connecte = true;
    Map<Integer, GameObject> objectsdebug=new ConcurrentHashMap<Integer, GameObject>();
    for(GameObject object2 : this.objects.values())
    {
    	if(object2 == null)
    		continue;
    	if(object2.getTemplate() == null)
    		continue;
    	objectsdebug.put(object2.getGuid(),object2);
    }
    this.objects.clear();
    this.objects = objectsdebug;
    Database.getStatics().getPlayerData().updateLogged(this.id,1);
    if(this.isShowSeller())
    {
      this.setShowSeller(false);
      Main.world.removeSeller(this.getId(),this.getCurMap().getId());
      SocketManager.GAME_SEND_ALTER_GM_PACKET(this.getCurMap(),this);
      try
      {
      for(Player z : this.curMap.getPlayers())
      {
      	if(z== null)
      	continue;
        if(z.getGameClient()==null)
          continue;
        if(z.getExchangeAction() == null)
        	continue;
        if(z.getExchangeAction().getType()==ExchangeAction.TRADING_WITH_OFFLINE_PLAYER) {
        	if(Integer.parseInt(z.getExchangeAction().getValue().toString()) == this.id)
        		GameClient.leaveExchange(z);
        }
      }
      }
      catch(Exception e)
      {
      }
    }
    if(this._mount!=null)
      SocketManager.GAME_SEND_Re_PACKET(this,"+",this._mount);
    if(this.getClasse()*10+this.getSexe()!=this.getGfxId())
      this.send("AR3K");
    SocketManager.GAME_SEND_Rx_PACKET(this);
    SocketManager.GAME_SEND_ASK(client,this);
   // for(int a=1;a<Main.world.getItemSetNumber();a++)
     for(int a=1;a<190;a++)
      if(this.getNumbEquipedItemOfPanoplie(a)!=0)
        SocketManager.GAME_SEND_OS_PACKET(this,a);
    
    if(this.fight!=null)
      SocketManager.send(this,"ILF0");
    else
      SocketManager.send(this,"ILS2000");
    if(this._metiers.size()>0)
    {
      ArrayList<JobStat> list=new ArrayList<JobStat>();
      list.addAll(this._metiers.values());
      //packet JS
      SocketManager.GAME_SEND_JS_PACKET(this,list);
      //packet JX
      SocketManager.GAME_SEND_JX_PACKET(this,list);
      //Packet JO (Job Option)
      SocketManager.GAME_SEND_JO_PACKET(this,list);
      GameObject obj=getObjetByPos(Constant.ITEM_POS_ARME);
      if(obj!=null)
        for(JobStat sm : list)
        	if (sm.getTemplate().isValidTool(obj.getTemplate().getId()))
            SocketManager.GAME_SEND_OT_PACKET(account.getGameClient(),sm.getTemplate().getId());
    }

    SocketManager.GAME_SEND_ALIGNEMENT(client,_align);
    SocketManager.GAME_SEND_ADD_CANAL(client,"*#%!p$:?i~"+"^"+(this.getGroupe()!=null ? "@" : ""));
    this._canaux = "*#%!pi$:?";
    if(_guildMember!=null)
      SocketManager.GAME_SEND_gS_PACKET(this,_guildMember);
    SocketManager.GAME_SEND_ZONE_ALLIGN_STATUT(client);
    SocketManager.GAME_SEND_EMOTE_LIST(this,getCompiledEmote(this.emotes));
    SocketManager.GAME_SEND_RESTRICTIONS(client);
    SocketManager.GAME_SEND_Ow_PACKET(this);
    SocketManager.GAME_SEND_SEE_FRIEND_CONNEXION(client,_showFriendConnection);
    SocketManager.GAME_SEND_SPELL_LIST(this);
    this.account.sendOnline();

    try {
    //Messages de bienvenue
    SocketManager.GAME_SEND_Im_PACKET(this,"189");
    if(!this.account.getLastConnectionDate().equals("")&&!account.getLastIP().equals(""))
      SocketManager.GAME_SEND_Im_PACKET(this,"0152;"+account.getLastConnectionDate()+"~"+account.getLastIP().substring(0, 3)+".XXX.XXX");
    SocketManager.GAME_SEND_Im_PACKET(this,"0153;"+account.getLastIP().substring(0, 3)+".XXX.XXX");
    this.account.setLastIP(this.account.getCurrentIp());
    }
    catch(Exception e)
    {
  
    }
    //Mise a jour du lastConnectionDate
    Date actDate=new Date();
    DateFormat dateFormat=new SimpleDateFormat("dd");
    String jour=dateFormat.format(actDate);
    dateFormat=new SimpleDateFormat("MM");
    String mois=dateFormat.format(actDate);
    dateFormat=new SimpleDateFormat("yyyy");
    String annee=dateFormat.format(actDate);
    dateFormat=new SimpleDateFormat("HH");
    String heure=dateFormat.format(actDate);
    dateFormat=new SimpleDateFormat("mm");
    String min=dateFormat.format(actDate);
    account.setLastConnectionDate(annee+"~"+mois+"~"+jour+"~"+heure+"~"+min);
    if(_guildMember!=null)
      _guildMember.setLastCo(annee+"~"+mois+"~"+jour+"~"+heure+"~"+min);
    //Affichage des prismes
    Main.world.showPrismes(this);
    //Actualisation dans la DB
    Database.getStatics().getAccountData().updateLastConnection(account);

    for(GameObject object : this.objects.values())
    {
    	if(object.getTemplate() == null)
    		continue;
      if(object.getTemplate().getType()==Constant.ITEM_TYPE_FAMILIER)
      {
        PetEntry p=Main.world.getPetsEntry(object.getGuid());
        Pet pets=Main.world.getPets(object.getTemplate().getId());

        if(p==null||pets==null)
        {
         // if(p!=null&&p.getPdv()>0)
           // SocketManager.GAME_SEND_Im_PACKET(this,"025");
          continue;
        }
        if(pets.getType()==0||pets.getType()==1)
          continue;
        p.updatePets(this,Integer.parseInt(pets.getGap().split(",")[1]));
      }
      else if(object.getTemplate().getId()==10207)
      {
        String date=object.getTxtStat().get(Constant.STATS_DATE);
        if(date!=null)
        {
          if(date.contains("#"))
          {
            date=date.split("#")[3];
          }
          if(System.currentTimeMillis()-Long.parseLong(date)>345600000)
          {
            object.getTxtStat().clear();
            object.getTxtStat().putAll(Dopeul.generateStatsTrousseau());
            object.setModification();
            SocketManager.GAME_SEND_UPDATE_ITEM(this,object);
            this.sendMessage("Votre trousseau a été réinitialisé");
          }
        }
      }
    }
    if(!Config.getInstance().startMessage.equals("")) //Si le motd est notifié
      SocketManager.GAME_SEND_MESSAGE(this,Config.getInstance().startMessage);
    if(_morphMode)
      setFullMorph(_morphId,true,true);

    if(Config.getInstance().fightAsBlocked)
      this.sendServerMessage("Vous ne pouvez démarrer aucun combat tant que le serveur n'a pas redémarré.");

    Main.world.logger.info("The player "+this.getName()+" come to connect.");
    if(this.getEnergy()==0)
      this.setGhost();
    if(this.time_last_connecte < (System.currentTimeMillis()/1000)){
		if(this.time_last_connecte != 0){
		int val = (int) ((System.currentTimeMillis()/1000)-this.time_last_connecte);
		if (this.getCurPdv() != this.getMaxPdv()) {
			
		if (this.getCurPdv() + val > this.getMaxPdv()) {
			val = this.getMaxPdv() - this.getCurPdv();
		}
		this.setPdv(this.getCurPdv() + val);	
	SocketManager.GAME_SEND_STATS_PACKET(this);
	SocketManager.GAME_SEND_Im_PACKET(this, "01;" + val);
         }
		int val2 = (int) ((System.currentTimeMillis()/1000)-this.time_last_connecte)/60;
		if(val2 != 0){
			if(this.getEnergy() + val2 < 10000){
			if (this.getEnergy() + val2 > 10000) {
				val2 = 10000 - this.getEnergy();
			}
			this.setEnergy(this.getEnergy() + val2);
			SocketManager.GAME_SEND_STATS_PACKET(this);	
			SocketManager.GAME_SEND_Im_PACKET(this, "07;" + val2);
			
		}
		}
		 }
	     }
    SocketManager.sendAveragePingPacket(this.getGameClient());
    refreshItemClasse(null);
	int fake = 0;
	 long calcul=System.currentTimeMillis()-Config.getInstance().startTime;
	 if(calcul>1200000) {
		 if(Config.getInstance().serverId == 1 )
      	fake = 0;
		 if(Config.getInstance().serverId == 22 || Config.getInstance().serverId == 8 || Config.getInstance().serverId == 7)
	        	fake = 0;
	 }
       
       if(Config.getInstance().HEROIC) {
    	   this.sendMessage("Bienvenue sur <b>Casper Heroic</b>, il y a actuellement <b>"+(Main.world.getOnlinePlayers().size()+fake)+"</b> joueurs en ligne.");   
       } 
       if(Config.singleton.serverId == 1) 
       {
      this.sendMessage("Bienvenue sur <b>Casper Semi-Like</b>, il y a actuellement <b>"+(Main.world.getOnlinePlayers().size()+fake)+"</b> joueurs en ligne.");
       }
       if(Config.singleton.serverId == 6) 
       {
      this.sendMessage("Bienvenue sur <b>Finishim PvP</b>, il y a actuellement <b>"+(Main.world.getOnlinePlayers().size()+fake)+"</b> joueurs en ligne.");
       }
       if(Config.singleton.serverId == 7) 
       {
      this.sendMessage("Bienvenue sur <b>Casper Semi-Like II</b>, il y a actuellement <b>"+(Main.world.getOnlinePlayers().size()+fake)+"</b> joueurs en ligne.");
       }
       if(Config.singleton.serverId == 8) 
       {
      this.sendMessage("Bienvenue sur <b>Zoldik</b>, il y a actuellement <b>"+(Main.world.getOnlinePlayers().size()+fake)+"</b> joueurs en ligne.");
       }
       if(Config.singleton.serverId != 6)
       SocketManager.GAME_SEND_MESSAGE(this,"La relique du jour est <b>["+Main.relique_donjon+"]</b> pour <b>Antre du kralamour </b>.","257C38");
       
    //this.verifEquiped();
    SocketManager.ENVIAR_Os_SETS_RAPIDOS(client);
    // Succes
    for (int  idd = 1 ; 18 >= idd ; idd++) {
    World.get_Succes(id).Check(this, idd);
    }
    // Succes
    
    // passe dj
    if(this.account.getTime_dj() != 0L) 
    {
    if(this.getItemTemplate(10207)!=null) {
		  this.removeByTemplateID(10207,1);
	       // SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+10207);  
	  }
		  GameObject obj2=Main.world.getObjTemplate(10207).createNewItem(1,false);
	        if(this.addObjet(obj2,true))
	          World.addGameObject(obj2,true);
	  }
	  //passe dj
	  }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	      SocketManager.GAME_SEND_ATTRIBUTE_FAILED(this.getGameClient());
	      if(this.getGameClient() != null)
	      this.getGameClient().disconnect();
	    }
	  if (Main.world.getOnlinePlayers().size() > Main.Max_players) {
		  Main.Max_players = Main.world.getOnlinePlayers().size();
       }
	    if(Config.singleton.serverId == 6) {
	    	TimerWaiterPlus.addNext(() -> {
			this.send("kB0");
		 },300);
	    }
	    if(this.curMap.getSong() == 1) {
	    	SocketManager.GAME_SEND_MESSAGE(this,"Casper Songe "+this.Song, "008000");
	    	if(Config.getInstance().HEROIC) {
	    		this.sendMessage("tu ne mourras pas ici ");
	    	}
	    }
	    if(this.account.hdv_offline != null) {
	    	for(String im : this.account.hdv_offline.split("X")) {
	    		  SocketManager.GAME_SEND_Im_PACKET(this,im);
	    	}
	    	this.account.hdv_offline = null;
	    }
	    List<Shortcuts> shortcuts = Main.world.getShortcutsFromPlayer(this);
		if (shortcuts != null) {
			for (Shortcuts shortcut : shortcuts) {
				SocketManager.GAME_SEND_ADD_SHORTCUT(this, shortcut);
			}
		}
	    this.setTime_co(System.currentTimeMillis());
	    if(this.curMap.getFights().size() == 0)
	    this.fight = null;	
  }

  public void SetSeeFriendOnline(boolean bool)
  {
    _showFriendConnection=bool;
  }

  public void sendGameCreate()
  {
	  try {
    this.setOnline(true);
    this.account.setCurrentPlayer(this);

    if(this.account.getGameClient()==null)
      return;

    GameClient client=this.account.getGameClient();
    SocketManager.GAME_SEND_GAME_CREATE(client,this.getName());
    SocketManager.GAME_SEND_STATS_PACKET(this);
    //Database.getStatics().getPlayerData().updateLogged(this.id,1);

    if(this.needEndFight()==-1)
    {
      SocketManager.GAME_SEND_MAPDATA(client,this.curMap.getId(),this.curMap.getDate(),this.curMap.getKey());
      SocketManager.GAME_SEND_MAP_FIGHT_COUNT(client,this.getCurMap());
      if(this.getFight()==null)
        this.curMap.addPlayer(this);
    }
    else
    {
      try
      {
        client.parsePacket("GI");
      }
      catch(InterruptedException e)
      {
        e.printStackTrace();
      }
    }
	  }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	      this.getGameClient().disconnect();
	    }
  }

  public String parseToOa()
  {
    return "Oa"+this.getId()+"|"+getGMStuffString();
  }

  public String parseToGM()
  {
    StringBuilder str=new StringBuilder();
    if(fight==null&&curCell!=null)// Hors combat
    {
      str.append(curCell.getId()).append(";").append(_orientation).append(";");
	  str.append(this.getOrnement()).append(";");
      //str.append("0^"+(this.couleur ? 1 : 0)).append(";");//FIXME:?
      str.append(this.getId()).append(";").append(this.getName()).append(";").append(this.getClasse());
      str.append((this.get_title()>0 ? (","+this.getStringTitle(get_title())+";") : (";")));
      int gfx=gfxId;
      if(this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF)!=null)
        if(this.getObjetByPos(Constant.ITEM_POS_ROLEPLAY_BUFF).getTemplate().getId()==10681)
          gfx=8037;
      str.append(gfx).append("^").append(_size);//gfxID^size
      if(this.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR)!=null)
        str.append(",").append(Constant.getItemIdByMascotteId(this.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR).getTemplate().getId())).append("^100");
      str.append(";").append(this.getSexe()).append(";");
      str.append(_align).append(",");
      str.append("0").append(",");//FIXME:?
      str.append((_showWings ? getGrade() : "0")).append(",");
      str.append(this.getLevel()+this.getId());
      if(_showWings&&_deshonor>0)
      {
        str.append(",");
        str.append(_deshonor>0 ? 1 : 0).append(';');
      }
      else
      {
        str.append(";");
      }
      int color1=this.getColor1(),color2=this.getColor2(),color3=this.getColor3();
      if(this.getObjetByPos(Constant.ITEM_POS_MALEDICTION)!=null)
        if(this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId()==10838)
        {
          color1=16342021;
          color2=16342021;
          color3=16342021;
        }

      str.append((color1==-1 ? "-1" : Integer.toHexString(color1))).append(";");
      str.append((color2==-1 ? "-1" : Integer.toHexString(color2))).append(";");
      str.append((color3==-1 ? "-1" : Integer.toHexString(color3))).append(";");
      str.append(getGMStuffString()).append(";");
      if(this.hasEquiped(10054)||this.hasEquiped(10055)||this.hasEquiped(10056)||this.hasEquiped(10058)||this.hasEquiped(10061)||this.hasEquiped(10102))
      {
        str.append(3).append(";");
      }
      else
      {
        Group g=this.getGroupe();
        int level=this.getLevel();
        if(g!=null)
          if(!g.isPlayer()||this.get_size()<=0) // Si c'est un groupe non joueur ou que l'on est invisible on cache l'aura
            level=1;
        str.append((level>99 ? (level>199 ? (2) : (1)) : (0))).append(";");
      }
      str.append(";");//Emote
      str.append(";");//Emote timer
      if(this._guildMember!=null&&this._guildMember.getGuild().haveTenMembers())
        str.append(this._guildMember.getGuild().getName()).append(";").append(this._guildMember.getGuild().getEmblem()).append(";");
      else
        str.append(";;");
      if(this.dead==1&&!this.isGhost)
        str.append("-1");
      str.append(getSpeed()).append(";");//Restriction
      str.append((_onMount&&_mount!=null ? _mount.getStringColor(parsecolortomount()) : "")).append(";");
      str.append(this.isDead()).append(";");
    }
    return str.toString();
  }

  public String parseToMerchant()
  {
    StringBuilder str=new StringBuilder();
    str.append(curCell.getId()).append(";");
    str.append(_orientation).append(";");
    str.append("0").append(";");
    str.append(this.getId()).append(";");
    str.append(this.getName()).append(";");
    str.append("-5").append(";");//Merchant identifier
    str.append(gfxId).append("^").append(_size).append(";");
    int color1=this.getColor1(),color2=this.getColor2(),color3=this.getColor3();
    if(this.getObjetByPos(Constant.ITEM_POS_MALEDICTION)!=null)
      if(this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId()==10838)
      {
        color1=16342021;
        color2=16342021;
        color3=16342021;
      }
    str.append((color1==-1 ? "-1" : Integer.toHexString(color1))).append(";");
    str.append((color2==-1 ? "-1" : Integer.toHexString(color2))).append(";");
    str.append((color3==-1 ? "-1" : Integer.toHexString(color3))).append(";");
    str.append(getGMStuffString()).append(";");//acessories
    str.append((_guildMember!=null ? _guildMember.getGuild().getName() : "")).append(";");//guildName
    str.append((_guildMember!=null ? _guildMember.getGuild().getEmblem() : "")).append(";");//emblem
    str.append("0;");//offlineType
    return str.toString();
  }

  public String getGMStuffString()
  {
    StringBuilder str=new StringBuilder();
 
    GameObject object = getObjetByPos(Constant.ITEM_POS_ARME);

	if (object != null)
		if (getObjetByPos(Constant.ITEM_POS_APARIENCIA_ARMA) != null) {
			str.append(Integer.toHexString(getObjetByPos(Constant.ITEM_POS_APARIENCIA_ARMA).getTemplate().getId()));
		} else {
			str.append(Integer.toHexString(object.getTemplate().getId()));
		}
	str.append(",");

	object = getObjetByPos(Constant.ITEM_POS_COIFFE);

	if (object != null) {
		object.parseStatsString();

		Integer obvi = object.getStats().getMap().get(970);
		if (obvi == null) {
			if (getObjetByPos(Constant.ITEM_POS_APARIENCIA_SOMBRERO) != null) {
				str.append(Integer
						.toHexString(getObjetByPos(Constant.ITEM_POS_APARIENCIA_SOMBRERO).getTemplate().getId()));
			} else {
				str.append(Integer.toHexString(object.getTemplate().getId()));
			}
		} else {
			str.append(Integer.toHexString(obvi)).append("~16~").append(object.getObvijevanLook());
		}
	}

	str.append(",");

	object = getObjetByPos(Constant.ITEM_POS_CAPE);

	if (object != null) {
		object.parseStatsString();

		Integer obvi = object.getStats().getMap().get(970);
		if (obvi == null) {
			if (getObjetByPos(Constant.ITEM_POS_APARIENCIA_CAPA) != null) {
				str.append(Integer
						.toHexString(getObjetByPos(Constant.ITEM_POS_APARIENCIA_CAPA).getTemplate().getId()));
			} else {
				str.append(Integer.toHexString(object.getTemplate().getId()));
			}
		} else {
			str.append(Integer.toHexString(obvi)).append("~17~").append(object.getObvijevanLook());
		}
	}

	str.append(",");

	object = getObjetByPos(Constant.ITEM_POS_FAMILIER);

	if (object != null)
		if (getObjetByPos(Constant.ITEM_POS_APARIENCIA_MASCOTA) != null) {
			str.append(
					Integer.toHexString(getObjetByPos(Constant.ITEM_POS_APARIENCIA_MASCOTA).getTemplate().getId()));
		} else {
			str.append(Integer.toHexString(object.getTemplate().getId()));
		}

	str.append(",");

    object=getObjetByPos(Constant.ITEM_POS_BOUCLIER);

    if(object!=null)
    {
      object.parseStatsString();

      Integer obvi=object.getStats().getMap().get(970);
      if(obvi==null)
      {
    	  if (getObjetByPos(Constant.ITEM_POS_APARIENCIA_ESCUDO) != null) {
				str.append(Integer
						.toHexString(getObjetByPos(Constant.ITEM_POS_APARIENCIA_ESCUDO).getTemplate().getId()));
			} 
          else
        str.append(Integer.toHexString(object.getTemplate().getId()));
      }
      else
      {
        str.append(Integer.toHexString(obvi)).append("~82~").append(object.getObvijevanLook());
      }
    }
       return str.toString();
  }

  public String getAsPacket(boolean boutique ,boolean window)
  {
    refreshStats();
    refreshLife(true);
    StringBuilder ASData=new StringBuilder();
    ASData.append("As").append(xpString(",")).append("|");
    ASData.append(boutique ? kamas+5000 : kamas).append("#").append(this.getAccount().getPoints()).append("|").append(_capital).append("|").append(_spellPts).append("|");
    ASData.append(_align).append("~").append(_align).append(",").append(_aLvl).append(",").append(getGrade()).append(",").append(_honor).append(",").append(_deshonor).append(",").append((_showWings ? "1" : "0")).append("|");
    int pdv=this.curPdv;
    int pdvMax=this.maxPdv;
    if(fight!=null&&!fight.isFinish())
    {
      Fighter f=fight.getFighterByPerso(this);
      if(f!=null)
      {
        pdv=f.getPdv();
        pdvMax=f.getPdvMax();
      }
    }
    Stats stats=this.getStats(),sutffStats=this.getStuffStats(),donStats=this.getDonsStats(),buffStats=this.getBuffsStats(),totalStats=this.getTotalStats();

    ASData.append(pdv).append(",").append(pdvMax).append("|");
    ASData.append(this.getEnergy()).append(",10000|");
    ASData.append(getInitiative()).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_PROS)+sutffStats.getEffect(Constant.STATS_ADD_PROS)+((int)Math.ceil(totalStats.getEffect(Constant.STATS_ADD_CHAN)/10))+buffStats.getEffect(Constant.STATS_ADD_PROS)+((int)Math.ceil(buffStats.getEffect(Constant.STATS_ADD_CHAN)/10))).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_PA)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_PA)).append(",").append(donStats.getEffect(Constant.STATS_ADD_PA)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_PA)).append(",").append(totalStats.getEffect(Constant.STATS_ADD_PA)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_PM)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_PM)).append(",").append(donStats.getEffect(Constant.STATS_ADD_PM)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_PM)).append(",").append(totalStats.getEffect(Constant.STATS_ADD_PM)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_FORC)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_FORC)).append(",").append(donStats.getEffect(Constant.STATS_ADD_FORC)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_FORC)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_VITA)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_VITA)).append(",").append(donStats.getEffect(Constant.STATS_ADD_VITA)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_VITA)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_SAGE)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_SAGE)).append(",").append(donStats.getEffect(Constant.STATS_ADD_SAGE)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_SAGE)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_CHAN)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_CHAN)).append(",").append(donStats.getEffect(Constant.STATS_ADD_CHAN)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_CHAN)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_AGIL)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_AGIL)).append(",").append(donStats.getEffect(Constant.STATS_ADD_AGIL)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_AGIL)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_INTE)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_INTE)).append(",").append(donStats.getEffect(Constant.STATS_ADD_INTE)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_INTE)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_PO)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_PO)).append(",").append(donStats.getEffect(Constant.STATS_ADD_PO)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_PO)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_SUM)+(window ? 40 : 0)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_SUM)).append(",").append(donStats.getEffect(Constant.STATS_ADD_SUM)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_SUM)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_DOMA)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_DOMA)).append(",").append(donStats.getEffect(Constant.STATS_ADD_DOMA)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_DOMA)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_PDOM)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_PDOM)).append(",").append(donStats.getEffect(Constant.STATS_ADD_PDOM)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_PDOM)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_MAITRISE)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_MAITRISE)).append(",").append(donStats.getEffect(Constant.STATS_ADD_MAITRISE)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_MAITRISE)).append("|");//ASData.append("0,0,0,0|");//Maitrise ?
    ASData.append(stats.getEffect(Constant.STATS_ADD_PERDOM)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_PERDOM)).append(",").append(donStats.getEffect(Constant.STATS_ADD_PERDOM)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_PERDOM)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_SOIN)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_SOIN)).append(",").append(donStats.getEffect(Constant.STATS_ADD_SOIN)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_SOIN)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_TRAPDOM)).append(",").append(sutffStats.getEffect(Constant.STATS_TRAPDOM)).append(",").append(donStats.getEffect(Constant.STATS_TRAPDOM)).append(",").append(buffStats.getEffect(Constant.STATS_TRAPDOM)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_TRAPPER)).append(",").append(sutffStats.getEffect(Constant.STATS_TRAPPER)).append(",").append(donStats.getEffect(Constant.STATS_TRAPPER)).append(",").append(buffStats.getEffect(Constant.STATS_TRAPPER)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_RETDOM)).append(",").append(sutffStats.getEffect(Constant.STATS_RETDOM)).append(",").append(donStats.getEffect(Constant.STATS_RETDOM)).append(",").append(buffStats.getEffect(Constant.STATS_RETDOM)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_CC)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_CC)).append(",").append(donStats.getEffect(Constant.STATS_ADD_CC)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_CC)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_EC)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_EC)).append(",").append(donStats.getEffect(Constant.STATS_ADD_EC)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_EC)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_AFLEE)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_AFLEE)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_AFLEE)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_AFLEE)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_MFLEE)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_MFLEE)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_MFLEE)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_MFLEE)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_R_NEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_NEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_NEU)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_RP_NEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_NEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_NEU)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_R_PVP_NEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_PVP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_NEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_NEU)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_RP_PVP_NEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_PVP_NEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_NEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_NEU)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_R_TER)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_TER)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_TER)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_RP_TER)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_TER)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_TER)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_R_PVP_TER)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_PVP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_TER)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_TER)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_RP_PVP_TER)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_PVP_TER)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_TER)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_TER)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_R_EAU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_EAU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_EAU)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_RP_EAU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_EAU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_EAU)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_R_PVP_EAU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_PVP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_EAU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_EAU)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_RP_PVP_EAU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_PVP_EAU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_EAU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_EAU)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_R_AIR)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_AIR)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_AIR)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_RP_AIR)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_AIR)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_AIR)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_R_PVP_AIR)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_PVP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_AIR)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_AIR)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_RP_PVP_AIR)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_PVP_AIR)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_AIR)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_AIR)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_R_FEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_FEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_FEU)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_RP_FEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_FEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_FEU)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_R_PVP_FEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_R_PVP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_FEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_R_PVP_FEU)).append("|");
    ASData.append(stats.getEffect(Constant.STATS_ADD_RP_PVP_FEU)).append(",").append(sutffStats.getEffect(Constant.STATS_ADD_RP_PVP_FEU)).append(",").append(0).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_FEU)).append(",").append(buffStats.getEffect(Constant.STATS_ADD_RP_PVP_FEU)).append("|");
    return ASData.toString();
  }

  public int getGrade()
  {
    if(_align==Constant.ALIGNEMENT_NEUTRE)
      return 0;
    if(_honor>=17500)
      return 10;
    for(int n=1;n<=10;n++)
      if(_honor<Main.world.getExpLevel(n).pvp)
        return n-1;
    return 0;
  }

  public String xpString(String c)
  {
    if(!_morphMode)
    {
      return this.getExp()+c+Main.world.getPersoXpMin(this.getLevel())+c+Main.world.getPersoXpMax(this.getLevel());
    }
    else
    {
      if(this.getObjetByPos(Constant.ITEM_POS_ARME)!=null)
        if(Constant.isIncarnationWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId()))
          if(this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.ERR_STATS_XP)!=null)
            return this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.ERR_STATS_XP)+c+Main.world.getBanditsXpMin(this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU))+c+Main.world.getBanditsXpMax(this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU));
    }
    return 1+c+1+c+1;
  }

  public int emoteActive()
  {
    return _emoteActive;
  }

  public void setEmoteActive(int emoteActive)
  {
    this._emoteActive=emoteActive;
  }

  public Stats getStuffStats()
  {
    if(this.useStats)
      return new Stats();

    Stats stats=new Stats(false,null);
    ArrayList<Integer> itemSetApplied=new ArrayList<>();
    for(GameObject gameObject : this.objects.values())
    {
      byte position=(byte)gameObject.getPosition();
      if(position!=Constant.ITEM_POS_NO_EQUIPED)
      {
        if(position>=35&&position<=48)
          continue;
        stats=Stats.cumulStat(stats,gameObject.getStats(),this);
        int id=gameObject.getTemplate().getPanoId();

        if(id>0&&!itemSetApplied.contains(id))
        {
          itemSetApplied.add(id);
          ObjectSet objectSet=Main.world.getItemSet(id);
          if(objectSet!=null)
            stats=Stats.cumulStat(stats,objectSet.getBonusStatByItemNumb(this.getNumbEquipedItemOfPanoplie(id)),this);
        }
      }

    }

    if(this._mount!=null&&this._onMount)
      stats=Stats.cumulStat(stats,this._mount.getStats(),this);

    return stats;
  }

  public Stats getBuffsStats()
  {
    Stats stats=new Stats(false,null);
    if(this.fight!=null)
      if(this.fight.getFighterByPerso(this)!=null)
        for(SpellEffect entry : this.fight.getFighterByPerso(this).getFightBuff())
          stats.addOneStat(entry.getEffectID(),entry.getValue());

    for(Entry<Integer, SpellEffect> entry : buffs.entrySet())
      stats.addOneStat(entry.getValue().getEffectID(),entry.getValue().getValue());
    return stats;
  }

  public int get_orientation()
  {
    return _orientation;
  }

  public void set_orientation(int _orientation)
  {
    this._orientation=_orientation;
  }

  public int getInitiative()
  {
    if(!useStats)
    {
      int fact=4;
      int maxPdv=this.maxPdv-55;
      int curPdv=this.curPdv-55;
      if(this.getClasse()==Constant.CLASS_SACRIEUR)
        fact=8;
      double coef=maxPdv/fact;

      coef+=getStuffStats().getEffect(Constant.STATS_ADD_INIT);
      coef+=getTotalStats().getEffect(Constant.STATS_ADD_AGIL);
      coef+=getTotalStats().getEffect(Constant.STATS_ADD_CHAN);
      coef+=getTotalStats().getEffect(Constant.STATS_ADD_INTE);
      coef+=getTotalStats().getEffect(Constant.STATS_ADD_FORC);

      int init=1;
      if(maxPdv!=0)
        init=(int)(coef*((double)curPdv/(double)maxPdv));
      if(init<0)
        init=0;
      return init;
    }
    else
    {
      return this.initiative;
    }
  }

  public Stats getTotalStats()
  {
    Stats total=new Stats(false,null);
    if(!useStats)
    {
      total=Stats.cumulStat(total,this.getStats(),this);
      total=Stats.cumulStat(total,this.getStuffStats(),this);
      total=Stats.cumulStat(total,this.getDonsStats(),this);
      if(fight!=null)
        total=Stats.cumulStatfight2(total,this.getBuffsStats(),this);
    }
    else
    {
      return newStatsMorph();
    }
    return total;
  }

  //v2.8 - buffless stats
  public Stats getBaseStats()
  {
    Stats total=new Stats(false,null);
    if(!useStats)
    {
      total=Stats.cumulStat(total,this.getStats(),this);
      total=Stats.cumulStat(total,this.getStuffStats(),this);
      total=Stats.cumulStat(total,this.getDonsStats(),this);
    }
    else
    {
      return newStatsMorph();
    }
    return total;
  }

  public Stats getDonsStats()
  {
    Stats stats=new Stats(false,null);
    return stats;
  }

  public Stats newStatsMorph()
  {
    Stats stats=new Stats();
    stats.addOneStat(Constant.STATS_ADD_PA,this.pa);
    stats.addOneStat(Constant.STATS_ADD_PM,this.pm);
    stats.addOneStat(Constant.STATS_ADD_VITA,this.vitalite);
    stats.addOneStat(Constant.STATS_ADD_SAGE,this.sagesse);
    stats.addOneStat(Constant.STATS_ADD_FORC,this.terre);
    stats.addOneStat(Constant.STATS_ADD_INTE,this.feu);
    stats.addOneStat(Constant.STATS_ADD_CHAN,this.eau);
    stats.addOneStat(Constant.STATS_ADD_AGIL,this.air);
    stats.addOneStat(Constant.STATS_ADD_INIT,this.initiative);
    stats.addOneStat(Constant.STATS_ADD_PROS,100);
    stats.addOneStat(Constant.STATS_ADD_SUM,1);
    this.useCac=false;
    return stats;
  }

  public int getPodUsed()
  {
    int pod=0;
    for(Entry<Integer, GameObject> entry : objects.entrySet()) {
    	if(entry.getValue().getTemplate() == null)
    		continue;
      pod+=entry.getValue().getTemplate().getPod()*entry.getValue().getQuantity();
    }
    pod+=parseStoreItemsListPods();
    return pod;
  }

  //v2.8 - different base pods
  public int getMaxPod()
  {
    Stats total=new Stats(false,null);
    total=Stats.cumulStat(total,this.getStats(),this);
    total=Stats.cumulStat(total,this.getStuffStats(),this);
    total=Stats.cumulStat(total,this.getDonsStats(),this);
    int pods=Config.getInstance().basePods;
    pods+=this.getLevel()*5;
    pods+=total.getEffect(Constant.STATS_ADD_PODS);
    pods+=total.getEffect(Constant.STATS_ADD_FORC)*5;
    for(JobStat SM : _metiers.values())
    {
      pods+=SM.get_lvl()*5;
      if(SM.get_lvl()==100)
        pods+=1000;
    }
    if(this.getAccount() != null )
    if(this.getAccount().getSubscribeRemaining() != 0L)
    	pods+=10000;
    
    if(pods<Config.getInstance().basePods)
      pods=Config.getInstance().basePods;
    return pods;
  }

  //v2.3 - Regen message bugfix
  public void refreshLife(boolean refresh)
  {
    if(get_isClone())
      return;
    long time=(System.currentTimeMillis()-regenTime);
    regenTime=System.currentTimeMillis();
    if(fight!=null)
      return;
    if(regenRate==0)
      return;
    if(this.curPdv>this.maxPdv)
    {
      this.curPdv=this.maxPdv-1;
      if(!refresh)
        SocketManager.GAME_SEND_STATS_PACKET(this);
      return;
    }
    int diff=(int)time/regenRate;
    if(diff+this.curPdv>this.maxPdv)
      diff=this.maxPdv-this.curPdv;
    if(diff>=10&&this.regenRate==2000)
      SocketManager.send(this,"ILF"+diff);
    setPdv(this.curPdv+diff);
  }

  public byte get_align()
  {
    return _align;
  }

  public int get_pdvper()
  {
    refreshLife(false);
    int pdvper=100;
    pdvper=(100*this.curPdv)/this.maxPdv;
    if(pdvper>100)
      return 100;
    return pdvper;
  }

  public void useSmiley(String str)
  {
    try
    {
      int id=Integer.parseInt(str);
      GameMap map=curMap;
      if(fight==null)
        SocketManager.GAME_SEND_EMOTICONE_TO_MAP(map,this.getId(),id);
      else
        SocketManager.GAME_SEND_EMOTICONE_TO_FIGHT(fight,7,this.getId(),id);
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
    }
  }

  public void boostStat(int stat, boolean capital)
  {
    int value=0;
    switch(stat)
    {
      case 10://Force
        value=this.getStats().getEffect(Constant.STATS_ADD_FORC);
        break;
      case 13://Chance
        value=this.getStats().getEffect(Constant.STATS_ADD_CHAN);
        break;
      case 14://Agilité
        value=this.getStats().getEffect(Constant.STATS_ADD_AGIL);
        break;
      case 15://Intelligence
        value=this.getStats().getEffect(Constant.STATS_ADD_INTE);
        break;
    }
    int cout=Constant.getReqPtsToBoostStatsByClass(this.getClasse(),stat,value);
    if(!capital)
      cout=0;
    if(cout<=_capital)
    {
      switch(stat)
      {
        case 11://Vita
          if(this.getClasse()!=Constant.CLASS_SACRIEUR)
            this.getStats().addOneStat(Constant.STATS_ADD_VITA,1);
          else
            this.getStats().addOneStat(Constant.STATS_ADD_VITA,capital ? 2 : 1);
          break;
        case 12://Sage
          this.getStats().addOneStat(Constant.STATS_ADD_SAGE,1);
          break;
        case 10://Force
          this.getStats().addOneStat(Constant.STATS_ADD_FORC,1);
          break;
        case 13://Chance
          this.getStats().addOneStat(Constant.STATS_ADD_CHAN,1);
          break;
        case 14://Agilité
          this.getStats().addOneStat(Constant.STATS_ADD_AGIL,1);
          break;
        case 15://Intelligence
          this.getStats().addOneStat(Constant.STATS_ADD_INTE,1);
          break;
        default:
          return;
      }
      _capital-=cout;
    }
  }

  public void boostStatFixedCount(int stat, int countVal)
  {
    for(int i=0;i<countVal;i++)
    {
      int value=0;
      switch(stat)
      {
        case 10://Force
          value=this.getStats().getEffect(Constant.STATS_ADD_FORC);
          break;
        case 13://Chance
          value=this.getStats().getEffect(Constant.STATS_ADD_CHAN);
          break;
        case 14://Agilité
          value=this.getStats().getEffect(Constant.STATS_ADD_AGIL);
          break;
        case 15://Intelligence
          value=this.getStats().getEffect(Constant.STATS_ADD_INTE);
          break;
      }
      int cout=Constant.getReqPtsToBoostStatsByClass(this.getClasse(),stat,value);
      if(cout<=_capital)
      {
        switch(stat)
        {
          case 11://Vita
            if(this.getClasse()!=Constant.CLASS_SACRIEUR)
              this.getStats().addOneStat(Constant.STATS_ADD_VITA,1);
            else
              this.getStats().addOneStat(Constant.STATS_ADD_VITA,2);
            break;
          case 12://Sage
            this.getStats().addOneStat(Constant.STATS_ADD_SAGE,1);
            break;
          case 10://Force
            this.getStats().addOneStat(Constant.STATS_ADD_FORC,1);
            break;
          case 13://Chance
            this.getStats().addOneStat(Constant.STATS_ADD_CHAN,1);
            break;
          case 14://Agilité
            this.getStats().addOneStat(Constant.STATS_ADD_AGIL,1);
            break;
          case 15://Intelligence
            this.getStats().addOneStat(Constant.STATS_ADD_INTE,1);
            break;
          default:
            return;
        }
        _capital-=cout;
      }
    }
    SocketManager.GAME_SEND_STATS_PACKET(this);
    //Database.getStatics().getPlayerData().update(this);
  }

  public boolean isMuted()
  {
    return account.isMuted();
  }

  public String parseObjetsToDB()
  {
    StringBuilder str=new StringBuilder();
    if(objects.isEmpty())
      return "";
    for(Entry<Integer, GameObject> entry : objects.entrySet())
    {
      GameObject obj=entry.getValue();
      if(obj==null)
        continue;
      str.append(obj.getGuid()).append("|");
    }

    return str.toString();
  }

  public boolean addObjet(GameObject newObj, boolean stackIfSimilar)
  {
    for(Entry<Integer, GameObject> entry : objects.entrySet())
    {
      GameObject obj=entry.getValue();
      if(ConditionParser.stackIfSimilar(obj,newObj,stackIfSimilar))
      {
        obj.setQuantity(obj.getQuantity()+newObj.getQuantity(), this);//On ajoute QUA item a la quantité de l'objet existant
        if(isOnline)
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,obj);
        return false;
      }
    }
    objects.put(newObj.getGuid(),newObj);
    SocketManager.GAME_SEND_OAKO_PACKET(this,newObj);
    return true;
  }

  public void addObjet(GameObject newObj)
  {
	  if(newObj == null)
		  return;
    objects.put(newObj.getGuid(),newObj);
    SocketManager.GAME_SEND_OAKO_PACKET(this,newObj);
    //Database.getStatics().getPlayerData().update(this);
    /*if(newObj!=null)
    {
      if(newObj.modification==0)
        Database.getDynamics().getObjectData().insert(newObj);
      else if(newObj.modification==1)
        Database.getDynamics().getObjectData().update(newObj);
    }
    */
  }

  public Map<Integer, GameObject> getItems()
  {
    return objects;
  }

  public String parseItemToASK()
  {
    StringBuilder str=new StringBuilder();
    if(objects.isEmpty())
      return "";
    ArrayList<GameObject> objet2 = new ArrayList<>();
    objet2.addAll(objects.values());
    Collections.sort(objet2, new order_items());
    for(GameObject obj : objet2)
    {
      str.append(obj.parseItem());
    }
    return str.toString();
  }
  private static class order_items implements Comparator<GameObject> {
      public int compare(GameObject p1, GameObject p2) {
    	  try {
          return Long.valueOf(p1.getTemplate().getLevel()).compareTo((long) (p2.getTemplate().getLevel()));
    	    } catch (final Exception e) {
    			e.printStackTrace();
    		}
		return 0;
      }

  }

  public String getItemsIDSplitByChar(String splitter)
  {
    StringBuilder str=new StringBuilder();
    if(objects.isEmpty())
      return "";
    for(int entry : objects.keySet())
    {
      if(str.length()!=0)
        str.append(splitter);
      str.append(entry);
    }

    return str.toString();
  }

  public String getStoreItemsIDSplitByChar(String splitter)
  {
    StringBuilder str=new StringBuilder();
    if(_storeItems.isEmpty())
      return "";
    for(int entry : _storeItems.keySet())
    {
      if(str.length()!=0)
        str.append(splitter);
      str.append(entry);
    }
    return str.toString();
  }

  public boolean hasItemGuid(int guid)
  {
    return objects.get(guid)!=null&&objects.get(guid).getQuantity()>0;
  }

  public void sellItem(int guid, int qua)
  {
    if(qua<=0)
      return;

    if(objects.get(guid).getQuantity()<qua)//Si il a moins d'item que ce qu'on veut Del
      qua=objects.get(guid).getQuantity();
  int prix = 0;
    if(objects.get(guid).getTemplate().getStrTemplate().contains("32c#")) {
        prix=qua*(objects.get(guid).getTemplate().getPrice()/10);//Calcul du prix de vente (prix d'achat/10)
        	
    }else {
      prix=qua*((objects.get(guid).getTemplate().getPrice()/10) < 200 ? (objects.get(guid).getTemplate().getPrice()/10) : 200);//Calcul du prix de vente (prix d'achat/10)
        	
    }
    int newQua=objects.get(guid).getQuantity()-qua;
     if(objects.get(guid).getTemplate().getType() == 78)
    	 prix = 0;
    if(newQua<=0)//Ne devrait pas etre <0, S'il n'y a plus d'item apres la vente
    {
      objects.remove(guid);
      Main.world.removeGameObject(guid);
      Database.getDynamics().getObjectData().delete(guid);
      SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,guid);
    }
    else
    //S'il reste des items apres la vente
    {
      objects.get(guid).setQuantity(newQua, this);
      SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,objects.get(guid));
    }
    this.setKamas(kamas+prix);
    Main.world.kamas_total -= prix;
    SocketManager.GAME_SEND_STATS_PACKET(this);
    SocketManager.GAME_SEND_Ow_PACKET(this);
    SocketManager.GAME_SEND_ESK_PACKEt(this);
  }

  public void removeItem(int guid)
  {
    objects.remove(guid);
  }

  public void removeItem(int guid, int nombre, boolean send, boolean deleteFromWorld)
  {
    GameObject obj=objects.get(guid);

    if(nombre>obj.getQuantity())
      nombre=obj.getQuantity();

    if(obj.getQuantity()>=nombre)
    {
      int newQua=obj.getQuantity()-nombre;
      if(newQua>0)
      {
        obj.setQuantity(newQua, this);
        if(send&&isOnline)
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,obj);
      }
      else
      {
        //on supprime de l'inventaire et du Monde
        objects.remove(obj.getGuid());
        if(deleteFromWorld)
          Main.world.removeGameObject(obj.getGuid());
        //on envoie le packet si connecté
        if(send&&isOnline)
          SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,obj.getGuid());
      }
    }

    SocketManager.GAME_SEND_Ow_PACKET(this);
  }

  public void deleteItem(int guid)
  {
    objects.remove(guid);
    Main.world.removeGameObject(guid);
  }

  public GameObject getObjetByPos(int pos)
  {
    if(pos==Constant.ITEM_POS_NO_EQUIPED)
      return null;

    for(GameObject gameObject : this.objects.values())
    {
      if(gameObject.getPosition()==pos&&pos==Constant.ITEM_POS_FAMILIER)
      {
        if(gameObject.getTxtStat().isEmpty())
          return null;
        else if(Main.world.getPetsEntry(gameObject.getGuid())==null)
          return null;
      }
      if(gameObject.getPosition()==pos)
        return gameObject;
    }

    return null;
  }

  //TODO: Delete s'te fonction.
  public GameObject getObjetByPos2(int pos)
  {
    if(pos==Constant.ITEM_POS_NO_EQUIPED)
      return null;

    for(Entry<Integer, GameObject> entry : objects.entrySet())
    {
      GameObject obj=entry.getValue();

      if(obj.getPosition()==pos)
        return obj;
    }
    return null;
  }

  public void refreshStats()
  {
    double actPdvPer=(100*(double)this.curPdv)/(double)this.maxPdv;
    if(!useStats)
      this.maxPdv=(this.getLevel()-1)*5+50+getTotalStats().getEffect(Constant.STATS_ADD_VITA);
    this.curPdv=(int)Math.round(maxPdv*actPdvPer/100);
  }

  public boolean levelUp(boolean send, boolean addXp)
  {
    if(this.getLevel()==Main.world.getExpLevelSize())
      return false;
    this.level++;
    _capital+=5;
    _spellPts++;
    this.maxPdv+=5;
    this.setPdv(this.getMaxPdv());
    SocketManager.GAME_SEND_STATS_PACKET(this);
    if(this.getLevel()==100)
      this.getStats().addOneStat(Constant.STATS_ADD_PA,1);
    Constant.onLevelUpSpells(this,this.getLevel());
    if(addXp)
      this.exp=Main.world.getExpLevel(this.getLevel()).perso;
    if(send&&isOnline)
    {
      SocketManager.GAME_SEND_NEW_LVL_PACKET(account.getGameClient(),this.getLevel());
      SocketManager.GAME_SEND_STATS_PACKET(this);
      SocketManager.GAME_SEND_SPELL_LIST(this);
    }
    if(Config.singleton.serverId != 6)
    if(this.groupe == null)
    if(this.getLevel()==200) {
    SocketManager.GAME_SEND_Im_PACKET_TO_ALL("116;"+"<b>Server</b>"+"~Le joueur ["+this.name+"] a atteint le niveau 200 !!");
    }
    return true;
  }

  public boolean addXp(long winxp)
  {
    boolean up=false;
    this.exp+=winxp;
    int exLevel=this.getLevel();
    while(this.getExp()>=Main.world.getPersoXpMax(this.getLevel())&&this.getLevel()<Main.world.getExpLevelSize())
      up=levelUp(true,false);
    if(isOnline)
    {
      if(exLevel<this.getLevel())
        SocketManager.GAME_SEND_NEW_LVL_PACKET(account.getGameClient(),this.getLevel());
      SocketManager.GAME_SEND_STATS_PACKET(this);
    }
    return up;
  }

  public boolean levelUpIncarnations(boolean send, boolean addXp)
  {
    int level=this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);

    if(level==50)
      return false;

    level++;
    this.setPdv(this.getMaxPdv());
    SocketManager.GAME_SEND_STATS_PACKET(this);

    switch(level)
    {
      case 10:
      case 20:
      case 30:
      case 40:
      case 50:
        boostSpellIncarnation();
        break;
    }

    if(send&&isOnline)
    {
      SocketManager.GAME_SEND_STATS_PACKET(this);
      SocketManager.GAME_SEND_SPELL_LIST(this);
    }

    this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().clear();
    this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().put(Constant.STATS_NIVEAU,level);
    this.getObjetByPos(Constant.ITEM_POS_ARME);
    SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(this,this.getObjetByPos(Constant.ITEM_POS_ARME));
    return true;
  }

  public boolean addXpIncarnations(long winxp)
  {
    boolean up=false;
    int level=this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
    long exp=this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.ERR_STATS_XP);
    exp+=winxp;
    if(Constant.isBanditsWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId()))
    {
      while(exp>=Main.world.getBanditsXpMax(level)&&level<50)
      {
        up=levelUpIncarnations(true,false);
        level=this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
      }
    }
    else if(Constant.isTourmenteurWeapon(this.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getId()))
    {
      while(exp>=Main.world.getTourmenteursXpMax(level)&&level<50)
      {
        up=levelUpIncarnations(true,false);
        level=this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
      }
    }
    if(isOnline)
      SocketManager.GAME_SEND_STATS_PACKET(this);
    level=this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().get(Constant.STATS_NIVEAU);
    this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().clear();
    this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().put(Constant.STATS_NIVEAU,level);
    this.getObjetByPos(Constant.ITEM_POS_ARME).getSoulStat().put(Constant.ERR_STATS_XP,(int)exp);
    return up;
  }


  public GameObject getSimilarItem(GameObject exGameObject)
  {
	  if(exGameObject == null || exGameObject.getTemplate() == null)
		  return null;
    if(exGameObject.getTemplate().getId()==8378)
      return null;
    if(exGameObject.getTxtStat().get(Constant.STATS_MIMI_ID)!=null)
    	 return null;
    for(GameObject gameObject : this.objects.values()) {
    	if(gameObject.getTemplate() == null)
    		continue;
      if(gameObject.getTemplate().getId()==exGameObject.getTemplate().getId()&&gameObject.getStats().isSameStats(exGameObject.getStats())&&gameObject.getGuid()!=exGameObject.getGuid()&&!Constant.isIncarnationWeapon(exGameObject.getTemplate().getId())&&exGameObject.getTemplate().getType()!=Constant.ITEM_TYPE_CERTIFICAT_CHANIL&&exGameObject.getTemplate().getType()!=Constant.ITEM_TYPE_PIERRE_AME_PLEINE&&gameObject.getTemplate().getType()!=Constant.ITEM_TYPE_OBJET_ELEVAGE&&gameObject.getTemplate().getType()!=Constant.ITEM_TYPE_CERTIF_MONTURE&&(exGameObject.getTemplate().getType()!=Constant.ITEM_TYPE_QUETES||Constant.isFlacGelee(gameObject.getTemplate().getId()))&&!Constant.isCertificatDopeuls(gameObject.getTemplate().getId())&&gameObject.getTemplate().getType()!=Constant.ITEM_TYPE_FAMILIER&&gameObject.getTemplate().getType()!=Constant.ITEM_TYPE_OBJET_VIVANT&&gameObject.getPosition()==Constant.ITEM_POS_NO_EQUIPED&&gameObject.getTxtStat().get(Constant.STATS_MIMI_ID)==null)
        return gameObject;
    }

    return null;
  }

  public int learnJob(Job m)
  {
    for(Entry<Integer, JobStat> entry : _metiers.entrySet())
    {
      if(entry.getValue().getTemplate().getId()==m.getId())//Si le joueur a déjé le métier
        return -1;
    }
    int Msize=_metiers.size();
    if(Msize==35)//Si le joueur a déjé 6 métiers
    {
        System.out.println("JOB Limit !!!!!!!");
        return -1;
    }
    int pos=0;
    int i =0;
    if(JobConstant.isMageJob(m.getId())) 
    {

      for(i=1;i<36;i++) {
          if(_metiers.get(i)==null)
                pos=i; 
      }
    }
      else 
      {
          for(i=1;i<36;i++) {
              if(_metiers.get(i)==null)
                    pos=i;
          }
      }


    if(JobConstant.isMageJob(m.getId()))
    {
      if(_metiers.get(5)==null)
        pos=5;
      if(_metiers.get(4)==null)
        pos=4;
      if(_metiers.get(3)==null)
        pos=3;
    }
    else
    {
      if(_metiers.get(2)==null)
        pos=2;
      if(_metiers.get(1)==null)
        pos=1;
      if(_metiers.get(0)==null)
        pos=0;
    }

    JobStat sm=new JobStat(pos,m,1,0);
    _metiers.put(pos,sm);//On apprend le métier lvl 1 avec 0 xp
    if(isOnline)
    {
      //on créer la listes des JobStats a envoyer (Seulement celle ci)
      ArrayList<JobStat> list=new ArrayList<JobStat>();
      list.add(sm);

      SocketManager.GAME_SEND_Im_PACKET(this,"02;"+m.getId());
      //packet JS
      SocketManager.GAME_SEND_JS_PACKET(this,list);
      //packet JX
      SocketManager.GAME_SEND_JX_PACKET(this,list);
      //Packet JO (Job Option)
      SocketManager.GAME_SEND_JO_PACKET(this,list);

      GameObject obj=getObjetByPos(Constant.ITEM_POS_ARME);
      if(obj!=null)
        if(sm.getTemplate().isValidTool(obj.getTemplate().getId()))
          SocketManager.GAME_SEND_OT_PACKET(account.getGameClient(),m.getId());
    }
    return pos;
  }

  public void unlearnJob(int m)
  {
    _metiers.remove(Integer.valueOf(m));
  }

  public void unequipedObjet(GameObject o)
  {
	  if(o == null)
		  return;
    o.setPosition(Constant.ITEM_POS_NO_EQUIPED);
    ObjectTemplate oTpl=o.getTemplate();
    int idSetExObj=oTpl.getPanoId();
    if((idSetExObj>=81&&idSetExObj<=92)||(idSetExObj>=201&&idSetExObj<=212))
    {
      String[] stats=oTpl.getStrTemplate().split(",");
      for(String stat : stats)
      {
        String[] val=stat.split("#");
        String modifi=Integer.parseInt(val[0],16)+";"+Integer.parseInt(val[1],16)+";0";
        SocketManager.SEND_SB_SPELL_BOOST(this,modifi);
        this.removeItemClasseSpell(Integer.parseInt(val[1],16));
      }
      this.removeItemClasse(oTpl.getId());
    }
    SocketManager.GAME_SEND_OBJET_MOVE_PACKET(this,o);
    if(oTpl.getPanoId()>0)
      SocketManager.GAME_SEND_OS_PACKET(this,oTpl.getPanoId());
    this.refreshStats();
    SocketManager.GAME_SEND_STATS_PACKET(this);
    //Database.getStatics().getObjectData().update(o);
  }

  public void verifEquiped()
  {
    if(this.getMorphMode())
      return;
    GameObject arme=this.getObjetByPos(Constant.ITEM_POS_ARME);
    GameObject bouclier=this.getObjetByPos(Constant.ITEM_POS_BOUCLIER);
    if(arme!=null)
    {
    	if(!arme.getTemplate().getConditions().contains("PJ"))
      if(arme.getTemplate().isTwoHanded()&&bouclier!=null)
      {
        this.unequipedObjet(arme);
        this.sendMessage("Vous ne pouve pas équipée une arme é 2 mains avec votre bouclier");
      }
      else if(!arme.getTemplate().getConditions().equalsIgnoreCase("")&&!ConditionParser.validConditions(this,arme.getTemplate().getConditions()))
      {
       this.unequipedObjet(arme);
       this.sendMessage("Vous ne pouve pas équipée une arme é 2 mains avec votre bouclier");
      }
    }
    if(bouclier!=null)
    {
      if(!bouclier.getTemplate().getConditions().equalsIgnoreCase("")&&!ConditionParser.validConditions(this,bouclier.getTemplate().getConditions()))
      {
        this.unequipedObjet(bouclier);
        this.sendMessage("Vous ne pouve pas équipée une arme é 2 mains avec votre bouclier");
      }
    }
  }

  public boolean hasEquiped(int id)
  {
    for(Entry<Integer, GameObject> entry : objects.entrySet()) {
    if(entry.getValue().getTemplate() == null)	
    	continue;
      if(entry.getValue().getTemplate().getId()==id&&entry.getValue().getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
        return true;
    }

    return false;
  }

  public int getInvitation()
  {
    return _inviting;
  }

  public void setInvitation(int target)
  {
    _inviting=target;
  }

  public String parseToPM()
  {
    StringBuilder str=new StringBuilder();
    str.append(this.getId()).append(";");
    str.append(this.getName()).append(";");
    str.append(gfxId).append(";");
    int color1=this.getColor1(),color2=this.getColor2(),color3=this.getColor3();
    if(this.getObjetByPos(Constant.ITEM_POS_MALEDICTION)!=null)
      if(this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId()==10838)
      {
        color1=16342021;
        color2=16342021;
        color3=16342021;
      }
    str.append(color1).append(";");
    str.append(color2).append(";");
    str.append(color3).append(";");
    str.append(getGMStuffString()).append(";");
    str.append(this.curPdv).append(",").append(this.maxPdv).append(";");
    str.append(this.getLevel()).append(";");
    str.append(getInitiative()).append(";");
    str.append(getTotalStats().getEffect(Constant.STATS_ADD_PROS)+((int)Math.ceil(getTotalStats().getEffect(Constant.STATS_ADD_CHAN)/10))).append(";");
    str.append("0");//Side = ?
    return str.toString();
  }

  public int getNumbEquipedItemOfPanoplie(int panID)
  {
    int nb=0;

    for(Entry<Integer, GameObject> i : objects.entrySet())
    {
      //On ignore les objets non équipés
      if(i.getValue().getPosition()==Constant.ITEM_POS_NO_EQUIPED)
        continue;
      //On prend que les items de la pano demandée, puis on augmente le nombre si besoin
      if(i.getValue().getTemplate().getPanoId()==panID)
        nb++;
    }
    return nb;
  }

  public void startActionOnCell(GameAction GA)
  {
    int cellID=-1;
    int action=-1;
    try
    {
      cellID=Integer.parseInt(GA.args.split(";")[0]);
      action=Integer.parseInt(GA.args.split(";")[1]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if(cellID==-1||action==-1)
      return;
    //Si case invalide

    if(!this.curMap.getCase(cellID).canDoAction(action))
      return;
    this.curMap.getCase(cellID).startAction(this,GA);
    this._gameAction_rapide = GA;
  }

  public void finishActionOnCell(GameAction GA)
  {
    int cellID=-1;
    try
    {
      cellID=Integer.parseInt(GA.args.split(";")[0]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if(cellID==-1)
      return;
    this.curMap.getCase(cellID).finishAction(this,GA);
  }

  public void teleportD(short newMapID, int newCellID)
  {
    if(this.getFight()!=null)
      return;
    this.curMap=Main.world.getMap(newMapID);
    this.curCell=Main.world.getMap(newMapID).getCase(newCellID);
    //Database.getStatics().getPlayerData().update(this);
  }


  //v2.0 - 0 Energy mount fix
  public void teleport(short newMapID, int newCellID)
  {
    if(this.getFight()!=null)
      return;
    if(this.isDead()==1)
    	return;
    GameClient client=this.getGameClient();
    if(client==null)
      return;
    GameClient.leaveExchange(this);
    GameMap map=Main.world.getMap(newMapID);
    if(map==null)
    {
      return;
    }

    if(map.getCase(newCellID)==null)
    {
      return;
    }
    
    if(newMapID==this.curMap.getId())
    {
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.curMap,this.getId());
      if(this.curCell != null)
      this.curCell.removePlayer(this);
      this.curCell=curMap.getCase(newCellID);
      this.curMap.addPlayer(this);
      SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this.curMap,this);
      return;
    }
    if(this.getSpioned_by() != null)
    	this.getSpioned_by().teleport(newMapID, newCellID);
    this.setAway(false);
    boolean fullmorph=false;
    if(Constant.isInMorphDonjon(this.curMap.getId()))
      if(!Constant.isInMorphDonjon(newMapID))
        fullmorph=true;

    SocketManager.GAME_SEND_GA2_PACKET(client,this.getId());
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.curMap,this.getId());
    if(Config.singleton.serverId != 6)
    if(this.getMount()!=null)
    {
      if(this.getMount().getFatigue()>=220)
        this.getMount().setEnergy(this.getMount().getEnergy()-1);
      if(this.getMount().getEnergy()<=0&&this.isOnMount())
      {
        this.toogleOnMount();
        SocketManager.GAME_SEND_MESSAGE(this,"Vous ne pouvez plus monter votre monture, elle est épuisée ... Nourrissez-la ou laissez-la reposer dans un enclos");
      }
    }
    if(this.curCell != null)
    if(this.curCell.getPlayers().contains(this))
      this.curCell.removePlayer(this);
    this.curMap=map;
    this.curCell=this.curMap.getCase(newCellID);
    // Verification de la Map
    // Verifier la validité du mountpark

    // Verifier la validité du Collector
    Collector col=Collector.getCollectorByMapId(this.curMap.getId());
    if(col!=null)
    {
      if(Main.world.getGuild(col.getGuildId())==null)// Ne devrait pas arriver
      {
        Collector.removeCollector(col.getGuildId());
      }
    }

    if(this.isInAreaNotSubscribe())
    {
      if(!this.isInPrivateArea)
        SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(this.getGameClient(),'S');
      this.isInPrivateArea=true;
    }
    else
    {
      this.isInPrivateArea=false;
    }
    SocketManager.GAME_SEND_MAPDATA(client,newMapID,this.curMap.getDate(),this.curMap.getKey());
    this.curMap.addPlayer(this);
    if(fullmorph)
      this.unsetFullMorph();

    if(this.follower!=null&&!this.follower.isEmpty()) // On met a jour la Map des personnages qui nous suivent
    {
      for(Player t : this.follower.values())
      {
        if(t.isOnline())
          SocketManager.GAME_SEND_FLAG_PACKET(t,this);
        else
          this.follower.remove(t.getId());
      }
    }

    if(this.getInHouse()!=null)
      if(this.getInHouse().getMapId()==this.curMap.getId())
        this.setInHouse(null);
    
    if(this.curMap.getSong() == 1) {
    	SocketManager.GAME_SEND_MESSAGE(this,"Casper Songe "+this.Song, "008000");
    	if(Config.getInstance().HEROIC) {
    		this.sendMessage("tu ne mourras pas ici ");
    	}
    }
    if(this.curMap.getFights().size() == 0)
	    this.fight = null;	
  }

  //v2.0 - 0 Energy mount fix
  public void teleport(GameMap map, int cell)
  {
    if(this.getFight()!=null)
      return;
    GameClient PW=null;
    if(account.getGameClient()!=null)
    {
      PW=account.getGameClient();
    }
    if(map==null)
    {
      return;
    }
    if(map.getCase(cell)==null)
    {
      return;
    }
    if(!isInPrison()&&!cantTP())
    {
      if(this.getCurMap().getSubArea()!=null&&map.getSubArea()!=null)
      {
        if(this.getCurMap().getSubArea().getId()==165&&map.getSubArea().getId()==165)
        {
          if(this.hasItemTemplate(997,1))
          {
            this.removeByTemplateID(997,1);
          }
          else
          {
            SocketManager.GAME_SEND_Im_PACKET(this,"14");
            return;
          }
        }
      }
    }

    boolean fullmorph=false;
    if(Constant.isInMorphDonjon(curMap.getId()))
      if(!Constant.isInMorphDonjon(map.getId()))
        fullmorph=true;

    if(map.getId()==curMap.getId())
    {
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap,this.getId());
      if(this.curCell != null)
      curCell.removePlayer(this);
      curCell=curMap.getCase(cell);
      curMap.addPlayer(this);
      SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(curMap,this);
      if(fullmorph)
        this.unsetFullMorph();
      return;
    }
    if(PW!=null)
    {
      SocketManager.GAME_SEND_GA2_PACKET(PW,this.getId());
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap,this.getId());
    }
    if(this.getMount()!=null)
    {
      if(this.getMount().getFatigue()>=220)
        this.getMount().setEnergy(this.getMount().getEnergy()-1);
      if(this.getMount().getEnergy()<=0&&this.isOnMount())
      {
        this.toogleOnMount();
        SocketManager.GAME_SEND_MESSAGE(this,"Vous ne pouvez plus monter votre monture, elle est épuisée ... Nourrissez-la ou laissez-la reposer dans un enclos");
      }
    }
    if(this.curCell != null)
    curCell.removePlayer(this);
    curMap=map;
    curCell=curMap.getCase(cell);
    // Verification de la Map
    // Verifier la validité du mountpark
    if(curMap.getMountPark()!=null&&curMap.getMountPark().getOwner()>0&&curMap.getMountPark().getGuild().getId()!=-1)
    {
      if(Main.world.getGuild(curMap.getMountPark().getGuild().getId())==null)// Ne devrait  pas  arriver
      {
        //FIXME : Map.MountPark.removeMountPark(curMap.getMountPark().get_guild().getId());
      }
    }
    // Verifier la validité du Collector
    if(Collector.getCollectorByMapId(curMap.getId())!=null)
    {
      if(Main.world.getGuild(Collector.getCollectorByMapId(curMap.getId()).getGuildId())==null)// Ne devrait pas arriver
      {
        Collector.removeCollector(Collector.getCollectorByMapId(curMap.getId()).getGuildId());
      }
    }

    if(PW!=null)
    {
      SocketManager.GAME_SEND_MAPDATA(PW,map.getId(),curMap.getDate(),curMap.getKey());
      curMap.addPlayer(this);
      if(fullmorph)
        this.unsetFullMorph();
    }

    if(!follower.isEmpty())// On met a jour la Map des personnages qui nous suivent
    {
      for(Player t : follower.values())
      {
        if(t.isOnline())
          SocketManager.GAME_SEND_FLAG_PACKET(t,this);
        else
          follower.remove(t.getId());
      }
    }
  }
  public String getStringTitle(int title) { 
	  if(World.gettitre(title) != null) {
		return ""+World.gettitre(title).Content+"*"+World.gettitre(title).Color+"";
	  }else {
		  return ""+title;  
	  }

  }
  public void disconnectInFight()
  {
    //Si en groupe
    if(getParty()!=null)
      getParty().leave(this);
    resetVars();
    //Database.getStatics().getPlayerData().update(this);
    set_isClone(true);
    Main.world.unloadPerso(this.getId());
  }

  public int getBankCost()
  {
    return account.getBank().size();
  }

  public String getStringVar(String str)
  {
    switch(str)
    {
      case "name":
        return this.getName();
      case "bankCost":
        return getBankCost()+"";
      case "points":
        return this.getAccount().getPoints()+"";
      case "align":
        return Main.world.getStatOfAlign();
    }
    return "";
  }

  public void refreshMapAfterFight()
  {
    if(this.account.getGameClient()!=null)
    {
      SocketManager.send(this,"ILS"+2000);
      this.regenRate=2000;
      this.curMap.addPlayer(this);
      SocketManager.GAME_SEND_STATS_PACKET(this);
      this.fight=null;
      this.away=false;
    }
  }

  public long getBankKamas()
  {
    return account.getBankKamas();
  }

  public void setBankKamas(long i)
  {
    account.setBankKamas(i);
    //Database.getDynamics().getBankData().update(account);
  }

  public String parseBankPacket()
  {
    StringBuilder packet=new StringBuilder();
    for(GameObject entry : account.getBank())
      packet.append("O").append(entry.parseItem()).append(";");
    if(getBankKamas()!=0)
      packet.append("G").append(getBankKamas());
    return packet.toString();
  }

  public void addCapital(int pts)
  {
    _capital+=pts;
  }

  public void setCapital(final int capital)
  {
    this._capital=capital;
  }

  public void addSpellPoint(int pts)
  {
    if(_morphMode)
      _saveSpellPts+=pts;
    else
      _spellPts+=pts;
  }

  public void addInBank(int guid, int qua)
  {
    if(qua<=0)
      return;
    GameObject PersoObj=World.getGameObject(guid);

    if(this.objects==null)
      return;

    if(objects.get(guid)==null || PersoObj == null) // Si le joueur n'a pas l'item dans son sac ...
      return;
    if(PersoObj.getTemplate().getType() == 24)
 	   return;
    if(PersoObj.getPosition()!=Constant.ITEM_POS_NO_EQUIPED) // Si c'est un item équipé ...
      return;

    GameObject BankObj=getSimilarBankItem(PersoObj);
    int newQua=PersoObj.getQuantity()-qua;
    if(BankObj==null) // Ajout d'un nouvel objet dans la banque
    {
      if(newQua<=0) // Ajout de toute la quantité disponible
      {
        removeItem(PersoObj.getGuid()); // On enleve l'objet du sac du joueur
        account.getBank().add(PersoObj); // On met l'objet du sac dans la banque, avec la meme quantité
        String str="O+"+PersoObj.getGuid()+"|"+PersoObj.getQuantity()+"|"+PersoObj.getTemplate().getId()+"|"+PersoObj.parseStatsString();
        SocketManager.GAME_SEND_EsK_PACKET(this,str);
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,guid);
      }
      else
      //S'il reste des objets au joueur
      {
        PersoObj.setQuantity(newQua, this); //on modifie la quantité d'item du sac
        BankObj=GameObject.getCloneObjet(PersoObj,qua); //On ajoute l'objet a la banque et au monde
        World.addGameObject(BankObj,true);
        account.getBank().add(BankObj);

        String str="O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getId()+"|"+BankObj.parseStatsString();
        SocketManager.GAME_SEND_EsK_PACKET(this,str); //Envoie des packets
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,PersoObj);
      }
    }
    else
    // S'il y avait un item du meme template
    {
      if(newQua<=0) //S'il ne reste pas d'item dans le sac
      {
        removeItem(PersoObj.getGuid()); //On enleve l'objet du sac du joueur
        Main.world.removeGameObject(PersoObj.getGuid()); //On enleve l'objet du monde
        BankObj.setQuantity(BankObj.getQuantity()+PersoObj.getQuantity(), this); //On ajoute la quantité a l'objet en banque
        String str="O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getId()+"|"+BankObj.parseStatsString(); //on envoie l'ajout a la banque de l'objet
        SocketManager.GAME_SEND_EsK_PACKET(this,str);
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,guid); //on envoie la supression de l'objet du sac au joueur
      }
      else
      //S'il restait des objets
      {
        PersoObj.setQuantity(newQua, this); //on modifie la quantité d'item du sac
        BankObj.setQuantity(BankObj.getQuantity()+qua, this);
        String str="O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getId()+"|"+BankObj.parseStatsString();
        SocketManager.GAME_SEND_EsK_PACKET(this,str);
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,PersoObj);
      }
    }
    SocketManager.GAME_SEND_Ow_PACKET(this);

    //Database.getStatics().getPlayerData().update(this);
    //Database.getDynamics().getBankData().update(account);
  }

  private GameObject getSimilarBankItem(GameObject exGameObject)
  {
    for(GameObject gameObject : this.account.getBank())
      if(ConditionParser.stackIfSimilar(gameObject,exGameObject,true))
        return gameObject;
    return null;
  }

  public void removeFromBank(int guid, int qua)
  {
    if(qua<=0)
      return;
    GameObject BankObj=World.getGameObject(guid);

    //Si le joueur n'a pas l'item dans sa banque ...
    int index=account.getBank().indexOf(BankObj);
    if(index==-1)
      return;

    GameObject PersoObj=getSimilarItem(BankObj);
    int newQua=BankObj.getQuantity()-qua;

    if(PersoObj==null)//Si le joueur n'avait aucun item similaire
    {
      //S'il ne reste rien en banque
      if(newQua<=0)
      {
        //On retire l'item de la banque
        account.getBank().remove(index);
        //On l'ajoute au joueur

        objects.put(guid,BankObj);

        //On envoie les packets
        SocketManager.GAME_SEND_OAKO_PACKET(this,BankObj);
        String str="O-"+guid;
        SocketManager.GAME_SEND_EsK_PACKET(this,str);
      }
      else
      //S'il reste des objets en banque
      {
        //On crée une copy de l'item en banque
        PersoObj=GameObject.getCloneObjet(BankObj,qua);
        //On l'ajoute au monde
        World.addGameObject(PersoObj,true);
        //On retire X objet de la banque
        BankObj.setQuantity(newQua, this);
        //On l'ajoute au joueur

        objects.put(PersoObj.getGuid(),PersoObj);

        //On envoie les packets
        SocketManager.GAME_SEND_OAKO_PACKET(this,PersoObj);
        String str="O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getId()+"|"+BankObj.parseStatsString();
        SocketManager.GAME_SEND_EsK_PACKET(this,str);
      }
    }
    else
    {
      //S'il ne reste rien en banque
      if(newQua<=0)
      {
        //On retire l'item de la banque
        account.getBank().remove(index);
        Main.world.removeGameObject(BankObj.getGuid());
        //On Modifie la quantité de l'item du sac du joueur
        PersoObj.setQuantity(PersoObj.getQuantity()+BankObj.getQuantity(), this);

        //On envoie les packets
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,PersoObj);
        String str="O-"+guid;
        SocketManager.GAME_SEND_EsK_PACKET(this,str);
      }
      else
      //S'il reste des objets en banque
      {
        //On retire X objet de la banque
        BankObj.setQuantity(newQua, this);
        //On ajoute X objets au joueurs
        PersoObj.setQuantity(PersoObj.getQuantity()+qua, this);

        //On envoie les packets
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,PersoObj);
        String str="O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getId()+"|"+BankObj.parseStatsString();
        SocketManager.GAME_SEND_EsK_PACKET(this,str);
      }
    }

    SocketManager.GAME_SEND_Ow_PACKET(this);

    //Database.getStatics().getPlayerData().update(this);
   // Database.getDynamics().getBankData().update(account);
  }

  /**
   * MountPark *
   */
  //v2.8 - disappearing mounts fixed
  public void openMountPark()
  {
    if(this.getDeshonor()>=5)
    {
      SocketManager.GAME_SEND_Im_PACKET(this,"183");
      return;
    }
    if(this.getGuildMember()!=null&&this.curMap.getMountPark().getGuild()!=null)
    {
      if(this.curMap.getMountPark().getGuild().getId()==this.getGuildMember().getGuild().getId())
      {
        if(!this.getGuildMember().canDo(Constant.G_USEENCLOS))
        {
          SocketManager.GAME_SEND_Im_PACKET(this,"1101");
          return;
        }
      }
    }

    MountPark mountPark=this.curMap.getMountPark();
    this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_MOUNTPARK,mountPark));
    this.away=true;

    StringBuilder packet=new StringBuilder();

    if(mountPark.getEtable().size()>0) //in shed
      for(Mount mount : mountPark.getEtable())
        if(mount.getOwner()==this.getId())
        {
          packet.append(";");
          packet.append(mount.parse());
        }
    packet.append("~");

    if(mountPark.getListOfRaising().size()>0) //in field
      for(Integer id : mountPark.getListOfRaising())
      {
        Mount mount=Main.world.getMountById(id);
        if(mount==null)
          continue;

        if(mount.getOwner()==this.getId())
        {
          packet.append(";");
          packet.append(mount.parse());
        }
        else if(getGuildMember()!=null)
          if(getGuildMember().canDo(Constant.G_OTHDINDE)&&mountPark.getOwner()!=-1&&mountPark.getGuild()!=null)
            if(mountPark.getGuild().getId()==this.get_guild().getId())
            {
              packet.append(";");
              packet.append(mount.parse());
            }
      }

    SocketManager.GAME_SEND_ECK_PACKET(this,16,packet.toString());

    //TimerWaiterPlus.addNext(() -> mountPark.getEtable().stream().filter(mount -> mount!=null&&mount.getSize()==50&&mount.getOwner()==this.getId()).forEach(mount -> SocketManager.GAME_SEND_Ee_PACKET_WAIT(this,'~',mount.parse())),500);
  }

  public void fullPDV()
  {
    this.setPdv(this.getMaxPdv());
    SocketManager.GAME_SEND_STATS_PACKET(this);
  }

  public void warpToSavePos()
  {
    try
    {
      String[] infos=_savePos.split(",");
      this.teleport(Short.parseShort(infos[0]),Integer.parseInt(infos[1]));
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public void removeByTemplateID(int tID, int count)
  {
    //Copie de la liste pour eviter les modif concurrentes
    ArrayList<GameObject> list=new ArrayList<GameObject>();

    list.addAll(objects.values());

    ArrayList<GameObject> remove=new ArrayList<GameObject>();
    int tempCount=count;

    //on verifie pour chaque objet
    for(GameObject obj : list)
    {
      //Si mauvais TemplateID, on passe
      if(obj.getTemplate().getId()!=tID)
        continue;

      if(obj.getQuantity()>=count)
      {
        int newQua=obj.getQuantity()-count;
        if(newQua>0)
        {
          obj.setQuantity(newQua, this);
          if(isOnline)
            SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,obj);
        }
        else
        {
          //on supprime de l'inventaire et du Monde
          objects.remove(obj.getGuid());
          Main.world.removeGameObject(obj.getGuid());
          //on envoie le packet si connecté
          if(isOnline)
            SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,obj.getGuid());
        }
        return;
      }
      else
      //Si pas assez d'objet
      {
        if(obj.getQuantity()>=tempCount)
        {
          int newQua=obj.getQuantity()-tempCount;
          if(newQua>0)
          {
            obj.setQuantity(newQua, this);
            if(isOnline)
              SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,obj);
          }
          else
            remove.add(obj);

          for(GameObject o : remove)
          {
            //on supprime de l'inventaire et du Monde

            objects.remove(o.getGuid());

            Main.world.removeGameObject(o.getGuid());
            //on envoie le packet si connecté
            if(isOnline)
              SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,o.getGuid());
          }
        }
        else
        {
          // on réduit le compteur
          tempCount-=obj.getQuantity();
          remove.add(obj);
        }
      }
    }
  }

  public ArrayList<Job> getJobs()
  {
    ArrayList<Job> list=new ArrayList<>();
    for(JobStat js : _metiers.values())
      if(js.getTemplate()!=null)
        list.add(js.getTemplate());
    return (list.isEmpty() ? null : list);
  }

  public Map<Integer, JobStat> getMetiers()
  {
    return _metiers;
  }

  public void doJobAction(int actionID, InteractiveObject object, GameAction GA, GameCase cell)
  {
    JobStat SM=getMetierBySkill(actionID);
    if(SM==null)
    {
      switch(actionID)
      {
        case 151:
          new JobAction(151,4,0,true,100,0).startAction(this,object,GA,cell);
          return;
        case 121:
          new JobAction(121,8,0,true,100,0).startAction(this,object,GA,cell);
          return;
        case 110:
          new JobAction(110,2,0,true,100,0).startAction(this,object,GA,cell);
          return;
        case 22:
          new JobAction(22,1,0,true,100,0).startAction(this,object,GA,cell);
          return;

      }
      SocketManager.GAME_SEND_MESSAGE(this,"Error: jobstats null.");
      return;
    }
    SM.startAction(actionID,this,object,GA,cell);
  }

  public void finishJobAction(int actionID, InteractiveObject object, GameAction GA, GameCase cell)
  {
    JobStat SM=getMetierBySkill(actionID);
    if(SM==null)
      return;
    SM.endAction(this,object,GA,cell);
  }

  public String parseJobData()
  {
    StringBuilder str=new StringBuilder();
    if(_metiers.isEmpty())
      return "";
    for(JobStat SM : _metiers.values())
    {
      if(SM==null)
        continue;
      if(str.length()>0)
        str.append(";");
      str.append(SM.getTemplate().getId()).append(",").append(SM.getXp());
    }
    return str.toString();
  }

  public int totalJobBasic()
  {
    int i=0;

    for(JobStat SM : _metiers.values())
    {
      // Si c'est un métier 'basic' :
      if(SM.getTemplate().getId()==2||SM.getTemplate().getId()==11||SM.getTemplate().getId()==13||SM.getTemplate().getId()==14||SM.getTemplate().getId()==15||SM.getTemplate().getId()==16||SM.getTemplate().getId()==17||SM.getTemplate().getId()==18||SM.getTemplate().getId()==19||SM.getTemplate().getId()==20||SM.getTemplate().getId()==24||SM.getTemplate().getId()==25||SM.getTemplate().getId()==26||SM.getTemplate().getId()==27||SM.getTemplate().getId()==28||SM.getTemplate().getId()==31||SM.getTemplate().getId()==36||SM.getTemplate().getId()==41||SM.getTemplate().getId()==56||SM.getTemplate().getId()==58||SM.getTemplate().getId()==60||SM.getTemplate().getId()==65)
      {
        i++;
      }
    }
    return i;
  }

  public int totalJobFM()
  {
    int i=0;

    for(JobStat SM : _metiers.values())
    {
      // Si c'est une spécialisation 'FM' :
      if(SM.getTemplate().getId()==43||SM.getTemplate().getId()==44||SM.getTemplate().getId()==45||SM.getTemplate().getId()==46||SM.getTemplate().getId()==47||SM.getTemplate().getId()==48||SM.getTemplate().getId()==49||SM.getTemplate().getId()==50||SM.getTemplate().getId()==62||SM.getTemplate().getId()==63||SM.getTemplate().getId()==64)
      {
        i++;
      }
    }
    return i;
  }

  public boolean canAggro()
  {
    return canAggro;
  }

  public void setCanAggro(boolean canAggro)
  {
    this.canAggro=canAggro;
  }

  public JobStat getMetierBySkill(int skID)
  {
    for(JobStat SM : _metiers.values())
      if(SM.isValidMapAction(skID))
        return SM;
    return null;
  }

  public String parseToFriendList(int guid)
  {
    StringBuilder str=new StringBuilder();
    str.append(";");
    str.append("?;");
    str.append(this.getName()).append(";");
    if(account.isFriendWith(guid))
    {
      str.append(this.getLevel()).append(";");
      str.append(_align).append(";");
    }
    else
    {
      str.append("?;");
      str.append("-1;");
    }
    str.append(this.getClasse()).append(";");
    str.append(this.getSexe()).append(";");
    str.append(gfxId);
    return str.toString();
  }

  public String parseToEnemyList(int guid)
  {
    StringBuilder str=new StringBuilder();
    str.append(";");
    str.append("?;");
    str.append(this.getName()).append(";");
    if(account.isFriendWith(guid))
    {
      str.append(this.getLevel()).append(";");
      str.append(_align).append(";");
    }
    else
    {
      str.append("?;");
      str.append("-1;");
    }
    str.append(this.getClasse()).append(";");
    str.append(this.getSexe()).append(";");
    str.append(gfxId);
    return str.toString();
  }

  public JobStat getMetierByID(int job)
  {
    for(JobStat SM : _metiers.values()) {
      if(SM.getTemplate().getId()==job)
        return SM;
}
    return null;
  }

  public boolean isOnMount()
  {
    return _onMount;
  }

  public void toogleOnMount()
  {
    if(_mount==null||this.isMorph()||this.getLevel()<60)
      return;
    if(this.getClasse()*10+this.getSexe()!=this.getGfxId())
      return;
    if(this.getInHouse()!=null)
    {
      SocketManager.GAME_SEND_Im_PACKET(this,"1117");
      return;
    }
    if(Config.singleton.serverId != 6) {
    if(!_onMount&&_mount.isMontable()==0 && _mount.getColor() != 88)
    {
      SocketManager.GAME_SEND_Re_PACKET(this,"Er",null);
      return;
    }

    if(!_onMount)
    {
      if(_mount.getEnergy()<Formulas.calculEnergieLooseForToogleMount(_mount.getFatigue()))
      {
        SocketManager.GAME_SEND_Im_PACKET(this,"1113");
        return;
      }
    }

    if(!_onMount)
    {
      int EnergyoLose=_mount.getEnergy()-Formulas.calculEnergieLooseForToogleMount(_mount.getFatigue());
      _mount.setEnergy(EnergyoLose);
    }
    }

    _onMount=!_onMount;
    GameObject obj=getObjetByPos(Constant.ITEM_POS_FAMILIER);

    if(_onMount&&obj!=null)
    {
      obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
      SocketManager.GAME_SEND_OBJET_MOVE_PACKET(this,obj);
    }

    if(Config.singleton.serverId != 6) {
    if(_mount.getEnergy()<=0)
    {
      _mount.setEnergy(0);
      SocketManager.GAME_SEND_Im_PACKET(this,"1114");
      return;
    }
    }
    //on envoie les packets
    if(getFight()!=null&&getFight().getState()==2)
    {
      SocketManager.GAME_SEND_ALTER_FIGHTER_MOUNT(getFight(),getFight().getFighterByPerso(this),getId(),getFight().getTeamId(getId()),getFight().getOtherTeamId(getId()));
    }
    else
    {
      SocketManager.GAME_SEND_ALTER_GM_PACKET(curMap,this);
    }
    SocketManager.GAME_SEND_Re_PACKET(this,"+",_mount);
    SocketManager.GAME_SEND_Rr_PACKET(this,_onMount ? "+" : "-");
    SocketManager.GAME_SEND_STATS_PACKET(this);

  }

  public int getMountXpGive()
  {
    return _mountXpGive;
  }

  public Mount getMount()
  {
    return _mount;
  }

  public void setMount(Mount DD)
  {
    _mount=DD;
  }

  public void setMountGiveXp(int parseInt)
  {
    _mountXpGive=parseInt;
  }

  public void resetVars()
  {
    if(this.getExchangeAction()!=null)
    {
      if(this.getExchangeAction().getValue() instanceof JobAction&&((JobAction)this.getExchangeAction().getValue()).getJobCraft()!=null)
        ((JobAction)this.getExchangeAction().getValue()).getJobCraft().getJobAction().broken=true;
      this.setExchangeAction(null);
    }

    doAction=false;
    this.setGameAction(null);
    away=false;
    _emoteActive=0;
    fight=null;
    duelId=0;
    ready=false;
    party=null;
    _inviting=0;
    sitted=false;
    _onMount=false;
    _isClone=false;
    _isForgetingSpell=false;
    _isAbsent=false;
    _isInvisible=false;
    follower.clear();
    follow=null;
    _curHouse=null;
    isGhost=false;
    _livreArti=false;
    _spec=false;
    afterFight=false;
    ipKamas = false;
    this.time_last_connecte = (long) 0;
  }

  public void addChanel(String chan)
  {
    if(_canaux.indexOf(chan)>=0)
      return;
    _canaux+=chan;
    SocketManager.GAME_SEND_cC_PACKET(this,'+',chan);
  }

  public void removeChanel(String chan)
  {
    _canaux=_canaux.replace(chan,"");
    SocketManager.GAME_SEND_cC_PACKET(this,'-',chan);
  }

  public void modifAlignement(int i)
  {
    _honor=0;
    //_deshonor=0;
    _align=(byte)i;
    _aLvl=1;
    SocketManager.GAME_SEND_ZC_PACKET(this,i);
    SocketManager.GAME_SEND_STATS_PACKET(this);
    if(get_guild()!=null)
      Database.getDynamics().getGuildMemberData().update(this);
    if(!this._showWings&&Config.getInstance().HEROIC)
      _showWings=true;
  }

  public int getDeshonor()
  {
    return _deshonor;
  }

  public void setDeshonor(int deshonor)
  {
    _deshonor=deshonor;
  }

  public void setShowWings(boolean showWings)
  {
    _showWings=showWings;
  }

  public int get_honor()
  {
    return _honor;
  }

  public void set_honor(int honor)
  {
    _honor=honor;
  }

  public int getALvl()
  {
    return _aLvl;
  }

  public void setALvl(int a)
  {
    _aLvl=a;
  }

  public void toggleWings(char c)
  {
    if(_align==Constant.ALIGNEMENT_NEUTRE)
      return;
    int hloose=_honor*5/100;
    switch(c)
    {
      case '*':
        SocketManager.GAME_SEND_GIP_PACKET(this,hloose);
        return;
      case '+':
        setShowWings(true);
        SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap,this);
       // Database.getStatics().getPlayerData().update(this);
        break;
      case '-':
        if(Config.getInstance().HEROIC)
        {
          SocketManager.GAME_SEND_MESSAGE(this,"Vous ne pouvez pas désactiver vos ailes sur un serveur héroéque.");
          break;
        }
        else
        {
          setShowWings(false);
          _honor-=hloose;
          SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap,this);
          //Database.getStatics().getPlayerData().update(this);
          break;
        }
    }
    SocketManager.GAME_SEND_STATS_PACKET(this);
  }

  public void addHonor(int winH)
  {
    if(_align==0)
      return;
    int curGrade=getGrade();
    _honor+=winH;
    if(_honor>18000)
      _honor=18000;
    SocketManager.GAME_SEND_Im_PACKET(this,"080;"+winH);
    //Changement de grade
    if(getGrade()!=curGrade)
    {
      SocketManager.GAME_SEND_Im_PACKET(this,"082;"+getGrade());
    }
    World.get_Succes(this.id).Grade(this);
  }

  public void remHonor(int losePH)
  {
    if(_align==0)
      return;
    int curGrade=getGrade();
    _honor-=losePH;
    SocketManager.GAME_SEND_Im_PACKET(this,"081;"+losePH);
    //Changement de grade
    if(getGrade()!=curGrade)
    {
      SocketManager.GAME_SEND_Im_PACKET(this,"083;"+getGrade());
    }
  }

  public void setTime_co(long time_co) {
	this.time_co = time_co;
}
  public long getTime_co() {
	return time_co;
}
  public long getTime_total() {
  	return time_total;
  }

  public void setTime_total(long time_total) {
  	this.time_total += time_total;
  }
  public GuildMember getGuildMember()
  {
	  try {
    return _guildMember;
	  }
      catch(Exception e)
      {
        e.printStackTrace();
        return null;
      }
  }

  public void setGuildMember(GuildMember _guild)
  {
    this._guildMember=_guild;
  }

  public int getAccID()
  {
    return _accID;
  }

  public String parseZaapList()//Pour le packet WC
  {
    String map=curMap.getId()+"";
    try
    {
      map=_savePos.split(",")[0];
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    StringBuilder str=new StringBuilder();
    str.append(map);

    int SubAreaID=curMap.getSubArea().getArea().getSuperArea();

    for(short i : _zaaps)
    {
      if(Main.world.getMap(i)==null)
        continue;
      if(Main.world.getMap(i).getSubArea().getArea().getSuperArea()!=SubAreaID)
        continue;
      //int cost=Formulas.calculZaapCost(curMap,Main.world.getMap(i));
      //if(Config.singleton.serverId == 12)
    	int  cost = 0;
      if(i==curMap.getId())
        cost=0;
      str.append("|").append(i).append(";").append(cost);
    }
    return str.toString();
  }

  //v2.7 - Replaced String += with StringBuilder
  public String parsePrismesList()
  {
    String map=curMap.getId()+"";
    StringBuilder str=new StringBuilder(map+"");
    int SubAreaID=curMap.getSubArea().getArea().getSuperArea();
    for(Prism Prisme : Main.world.AllPrisme())
    {
      if(Prisme.getAlignement()!=_align)
        continue;
      short MapID=Prisme.getMap();
      if(Main.world.getMap(MapID)==null)
        continue;
      if(Main.world.getMap(MapID).getSubArea().getArea().getSuperArea()!=SubAreaID)
        continue;
      if(Prisme.getInFight()==0||Prisme.getInFight()==-2)
      {
        str.append("|"+MapID+";*");
      }
      else
      {
        int costo=Formulas.calculZaapCost(curMap,Main.world.getMap(MapID));
        if(MapID==curMap.getId())
          costo=0;
        str.append("|"+MapID+";"+costo);
      }
    }
    return str.toString();
  }

  public void openZaapMenu()
  {
    if(this.fight==null)
    {
      if(getDeshonor()>=3)
      {
        SocketManager.GAME_SEND_Im_PACKET(this,"183");
        return;
      }

      this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_ZAAPING,0));
      verifAndAddZaap(curMap.getId());
      SocketManager.GAME_SEND_WC_PACKET(this);
    }
  }

  public void verifAndAddZaap(short mapId)
  {
    if(!_zaaps.contains(mapId))
    {
      _zaaps.add(mapId);
      SocketManager.GAME_SEND_Im_PACKET(this,"024");
      //Database.getStatics().getPlayerData().update(this);
    }
  }

  /*public boolean verifOtomaiZaap()
  {
    return !(this.getCurMap().getId()==10643||this.getCurMap().getId()==11210)||ConditionParser.validConditions(this,"QT=231")&&ConditionParser.validConditions(this,"QT=232");
  }*/

  public void openPrismeMenu()
  {
    if(this.fight==null)
    {
      if(getDeshonor()>=3)
      {
        SocketManager.GAME_SEND_Im_PACKET(this,"183");
        return;
      }

      this.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_PRISM,0));
      SocketManager.SEND_Wp_MENU_Prisme(this);
    }
  }

  public void useZaap(short id)
  {
    if(this.getExchangeAction()==null||this.getExchangeAction().getType()!=ExchangeAction.IN_ZAAPING)
      return; //S'il n'a pas ouvert l'interface Zaap(hack?)
    if(fight!=null)
      return; //Si il combat
    if(!_zaaps.contains(id))
      return; //S'il n'a pas le zaap demandé(ne devrais pas arriver)
    
    int cost = 0;
   // int cost=Formulas.calculZaapCost(curMap,Main.world.getMap(id));
    //if(Config.singleton.serverId != 12)
    //if(kamas<cost)
    //  return; //S'il n'a pas les kamas (verif coté client)
    short mapID=id;
    int SubAreaID=curMap.getSubArea().getArea().getSuperArea();
    int cellID=Main.world.getZaapCellIdByMapId(id);
    if(Main.world.getMap(mapID)==null)
    {
      SocketManager.GAME_SEND_WUE_PACKET(this);
      return;
    }
    if(Main.world.getMap(mapID).getCase(cellID)==null)
    {
      SocketManager.GAME_SEND_WUE_PACKET(this);
      return;
    }
    if(!Main.world.getMap(mapID).getCase(cellID).isWalkable(true))
    {
      SocketManager.GAME_SEND_WUE_PACKET(this);
      return;
    }
    if(Main.world.getMap(mapID).getSubArea().getArea().getSuperArea()!=SubAreaID)
    {
      SocketManager.GAME_SEND_WUE_PACKET(this);
      return;
    }
    if(id==4263&&this.get_align()==2)
      return;
    if(id==5295&&this.get_align()==1)
      return;
    if(cost<0)
    	return;
    //if(Config.singleton.serverId != 12)
    //this.setKamas(kamas-cost);
    teleport(mapID,cellID);
    SocketManager.GAME_SEND_STATS_PACKET(this);//On envoie la perte de kamas
    SocketManager.GAME_SEND_WV_PACKET(this);//On ferme l'interface Zaap
    this.setExchangeAction(null);
  }

  public void usePrisme(String packet)
  {
    if(this.getExchangeAction()==null||this.getExchangeAction().getType()!=ExchangeAction.IN_PRISM)
      return;
    int celdaID=340;
    //short MapID=7411;
    for(Prism Prisme : Main.world.AllPrisme())
    {
      if(Prisme.getMap()==Short.valueOf(packet.substring(2)))
      {
        if(Prisme.getFight()!=null)
        {
          SocketManager.GAME_SEND_MESSAGE(this,"Ce prisme est actuellement en combat et ne peut pas étre téléporté vers.");
          return;
        }

        celdaID=Prisme.getCell();
       // MapID=Prisme.getMap();
        break;
      }
    }
    //int costo=Formulas.calculZaapCost(curMap,Main.world.getMap(MapID));
    /*int costo = 0;
    if(MapID==curMap.getId())
      costo=0;
    if(kamas<costo)
    {
      SocketManager.GAME_SEND_MESSAGE(this,"Vous n'avez pas assez de Kamas pour effectuer cette action.");
      return;
    }
    if(costo<0)
    	return;
    //this.setKamas(kamas-costo);
     * 
     */
    SocketManager.GAME_SEND_STATS_PACKET(this);
    this.teleport(Short.valueOf(packet.substring(2)),celdaID);
    SocketManager.SEND_Ww_CLOSE_Prisme(this);
    this.setExchangeAction(null);
  }

  public String parseZaaps()
  {
    StringBuilder str=new StringBuilder();
    boolean first=true;

    if(_zaaps.isEmpty())
      return "";
    for(int i : _zaaps)
    {
      if(!first)
        str.append(",");
      first=false;
      str.append(i);
    }
    return str.toString();
  }

  public String parsePrisme()
  {
    String str="";
    Prism Prisme=Main.world.getPrisme(curMap.getSubArea().getPrismId());
    if(Prisme==null)
      str="-3";
    else if(Prisme.getInFight()==0)
    {
      str="0;"+Prisme.getTurnTime()+";45000;7";
    }
    else
    {
      str=Prisme.getInFight()+"";
    }
    return str;
  }

  public void stopZaaping()
  {
    if(this.getExchangeAction()==null||this.getExchangeAction().getType()!=ExchangeAction.IN_ZAAPING)
      return;

    this.setExchangeAction(null);
    SocketManager.GAME_SEND_WV_PACKET(this);
  }

  public void Zaapi_close()
  {
    if(this.getExchangeAction()==null||this.getExchangeAction().getType()!=ExchangeAction.IN_ZAPPI)
      return;
    this.setExchangeAction(null);
    SocketManager.GAME_SEND_CLOSE_ZAAPI_PACKET(this);
  }

  public void Prisme_close()
  {
    if(this.getExchangeAction()==null||this.getExchangeAction().getType()!=ExchangeAction.IN_PRISM)
      return;
    this.setExchangeAction(null);
    SocketManager.SEND_Ww_CLOSE_Prisme(this);
  }

  public void Zaapi_use(String packet)
  {
    if(this.getExchangeAction()==null||this.getExchangeAction().getType()!=ExchangeAction.IN_ZAPPI)
      return;
    GameMap map=Main.world.getMap(Short.valueOf(packet.substring(2)));
    short cell=100;
    if(map!=null)
    {
      for(GameCase entry : map.getCases())
      {
        InteractiveObject obj=entry.getObject();
        if(obj!=null)
        {
          if(obj.getId()==7031||obj.getId()==7030)
          {
            if(map.getId()==6159) //small map down-right
              cell=(short)(entry.getId()+15);
            else if(map.getId()==4272||map.getId()==2214||map.getId()==4300||map.getId()==4074||map.getId()==4172||map.getId()==4308||map.getId()==4238||map.getId()==2214|| //facing down-left
                map.getId()==8758||map.getId()==4229||map.getId()==2221||map.getId()==4172||map.getId()==4623||map.getId()==5116||map.getId()==4612||map.getId()==4551||map.getId()==4639||map.getId()==4620||map.getId()==5304||map.getId()==8756||map.getId()==5277||map.getId()==4549||map.getId()==4618||map.getId()==4622||map.getId()==5112||map.getId()==5311||map.getId()==5317)
              cell=(short)(entry.getId()+18);
            else //facing down-right
              cell=(short)(entry.getId()+19);
          }
        }
      }
      if(map.getSubArea()!=null&&(map.getSubArea().getArea().getId()==7||map.getSubArea().getArea().getId()==11))
      {
        //int price=20;
        //if(this.get_align()==1||this.get_align()==2)
         // price=10;
        //this.setKamas(kamas-price);
        SocketManager.GAME_SEND_STATS_PACKET(this);
        if((map.getSubArea().getArea().getId()==7&&this.getCurMap().getSubArea().getArea().getId()==7)||(map.getSubArea().getArea().getId()==11&&this.getCurMap().getSubArea().getArea().getId()==11))
        {
          this.teleport(Short.valueOf(packet.substring(2)),cell);
        }
        SocketManager.GAME_SEND_CLOSE_ZAAPI_PACKET(this);
        this.setExchangeAction(null);
      }
    }
  }

  public boolean hasItemTemplate(int i, int q)
  {
    for(GameObject obj : objects.values())
    {
      if(obj.getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
        continue;
      if(obj.getTemplate().getId()!=i)
        continue;
      if(obj.getQuantity()>=q)
        return true;
    }
    return false;
  }
  public boolean hasItemTemplatev2(final int i, final int q) {
			for (GameObject obj :  objects.values()) {
				if(obj.getPosition() == Constant.ITEM_POS_NO_EQUIPED)
		        continue;
				if(obj.getTemplate().getId()!=i)
			        continue;
				 if(obj.getQuantity()>=q)
				        return false;
			}
	
		return true;
	}
  public boolean hasItemTemplatev3(final int i, final int q) {
		for (GameObject obj :  objects.values()) {
			if(obj.getTemplate().getId()!=i)
		        continue;
			 if(obj.getQuantity()>=q)
				 return true;
		}

		  return false;
}

  public boolean hasItemType(int type)
  {
    for(GameObject obj : objects.values())
    {
      if(obj.getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
        continue;
      if(obj.getTemplate().getType()==type)
        return true;
    }

    return false;
  }

  public GameObject getItemTemplate(int i, int q)
  {
    for(GameObject obj : objects.values())
    {
      if(obj.getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
        continue;
      if(obj.getTemplate().getId()!=i)
        continue;
      if(obj.getQuantity()>=q)
        return obj;
    }
    return null;
  }

  public GameObject getItemTemplate(int i)
  {

    for(GameObject obj : objects.values())
    {
      if(obj.getTemplate().getId()!=i)
        continue;
      return obj;
    }

    return null;
  }

  public int getNbItemTemplate(int i)
  {
    for(GameObject obj : objects.values())
    {
      if(obj.getTemplate().getId()!=i)
        continue;
      return obj.getQuantity();
    }
    return -1;
  }

  public boolean isDispo(Player sender)
  {
    return !_isAbsent&&(!_isInvisible||account.isFriendWith(sender.getAccount().getId()));

  }

  public boolean get_isClone()
  {
    return _isClone;
  }

  public void set_isClone(boolean isClone)
  {
    _isClone=isClone;
  }

  public int get_title()
  {
    return _title;
  }

  public String get_titleladder()
  {
	  if(World.gettitre(_title) != null) {
		return World.gettitre(_title).Content;  
	  }else {
		  return "";  
	  }
  }
  public void set_title(int i)
  {
    _title=i;
  }

  //FIN CLONAGE
  public void VerifAndChangeItemPlace()
  {
    boolean isFirstAM=true;
    boolean isFirstAN=true;
    boolean isFirstANb=true;
    boolean isFirstAR=true;
    boolean isFirstBO=true;
    boolean isFirstBOb=true;
    boolean isFirstCA=true;
    boolean isFirstCE=true;
    boolean isFirstCO=true;
    boolean isFirstDa=true;
    boolean isFirstDb=true;
    boolean isFirstDc=true;
    boolean isFirstDd=true;
    boolean isFirstDe=true;
    boolean isFirstDf=true;
    boolean isFirstFA=true;

    for(GameObject obj : objects.values())
    {
      if(obj.getPosition()==Constant.ITEM_POS_NO_EQUIPED)
        continue;
      if(obj.getPosition()==Constant.ITEM_POS_AMULETTE)
      {
        if(isFirstAM)
        {
          isFirstAM=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_ANNEAU1)
      {
        if(isFirstAN)
        {
          isFirstAN=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_ANNEAU2)
      {
        if(isFirstANb)
        {
          isFirstANb=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_ARME)
      {
        if(isFirstAR)
        {
          isFirstAR=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_BOTTES)
      {
        if(isFirstBO)
        {
          isFirstBO=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_BOUCLIER)
      {
        if(isFirstBOb)
        {
          isFirstBOb=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_CAPE)
      {
        if(isFirstCA)
        {
          isFirstCA=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_CEINTURE)
      {
        if(isFirstCE)
        {
          isFirstCE=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_COIFFE)
      {
        if(isFirstCO)
        {
          isFirstCO=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_DOFUS1)
      {
        if(isFirstDa)
        {
          isFirstDa=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_DOFUS2)
      {
        if(isFirstDb)
        {
          isFirstDb=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_DOFUS3)
      {
        if(isFirstDc)
        {
          isFirstDc=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_DOFUS4)
      {
        if(isFirstDd)
        {
          isFirstDd=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_DOFUS5)
      {
        if(isFirstDe)
        {
          isFirstDe=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_DOFUS6)
      {
        if(isFirstDf)
        {
          isFirstDf=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
      else if(obj.getPosition()==Constant.ITEM_POS_FAMILIER)
      {
        if(isFirstFA)
        {
          isFirstFA=false;
        }
        else
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
        }
      }
    }
  }

  //Mariage

  public Stalk get_traque()
  {
    return _traqued;
  }

  public void set_traque(Stalk traq)
  {
    _traqued=traq;
  }

  public void setWife(int id)
  {
    this.wife=id;
    //Database.getStatics().getPlayerData().update(this);
  }

  public String get_wife_friendlist()
  {
    Player wife=Main.world.getPlayer(this.wife);
    StringBuilder str=new StringBuilder();
    if(wife!=null)
    {
      int color1=wife.getColor1(),color2=wife.getColor2(),color3=wife.getColor3();
      if(wife.getObjetByPos(Constant.ITEM_POS_MALEDICTION)!=null)
        if(wife.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId()==10838)
        {
          color1=16342021;
          color2=16342021;
          color3=16342021;
        }
      str.append(wife.getName()).append("|").append(wife.getClasse()+wife.getSexe()).append("|").append(color1).append("|").append(color2).append("|").append(color3).append("|");
      if(!wife.isOnline())
      {
        str.append("|");
      }
      else
      {
        str.append(wife.parse_towife()).append("|");
      }
    }
    else
    {
      str.append("|");
    }
    return str.toString();
  }

  public String parse_towife()
  {
    int f=0;
    if(fight!=null)
    {
      f=1;
    }
    return curMap.getId()+"|"+this.getLevel()+"|"+f;
  }

  public void meetWife(Player p)// Se teleporter selon les sacro-saintes autorisations du mariage.
  {
    if(p==null)
      return; // Ne devrait theoriquement jamais se produire.

    if(this.getPodUsed()>=this.getMaxPod()) // Refuser la téléportation si on est full pods.
    {
      SocketManager.GAME_SEND_Im_PACKET(this,"170");
      return;
    }

    int dist=(curMap.getX()-p.getCurMap().getX())*(curMap.getX()-p.getCurMap().getX())+(curMap.getY()-p.getCurMap().getY())*(curMap.getY()-p.getCurMap().getY());
    if(dist>100||p.getCurMap().getId()==this.getCurMap().getId())// La distance est trop grande...
    {
      if(p.getSexe()==0)
        SocketManager.GAME_SEND_Im_PACKET(this,"178");
      else
        SocketManager.GAME_SEND_Im_PACKET(this,"179");
      return;
    }

    int cellPositiontoadd=Constant.getNearCellidUnused(p);
    if(cellPositiontoadd==-1)
    {
      if(p.getSexe()==0)
        SocketManager.GAME_SEND_Im_PACKET(this,"141");
      else
        SocketManager.GAME_SEND_Im_PACKET(this,"142");
      return;
    }

    //teleport(p.getCurMap().getId(),cellPositiontoadd);
  }

  public void Divorce()
  {
    if(isOnline())
      SocketManager.GAME_SEND_Im_PACKET(this,"047;"+Main.world.getPlayer(wife).getName());

    wife=0;
   // Database.getStatics().getPlayerData().update(this);
  }

  public int getWife()
  {
    return wife;
  }

  public int setisOK(int ok)
  {
    return _isOK=ok;
  }

  public int getisOK()
  {
    return _isOK;
  }

  public void changeOrientation(int toOrientation)
  {
    if(this.get_orientation()==0||this.get_orientation()==2||this.get_orientation()==4||this.get_orientation()==6)
    {
      this.set_orientation(toOrientation);
      SocketManager.GAME_SEND_eD_PACKET_TO_MAP(getCurMap(),this.getId(),toOrientation);
    }
  }

  /** Heroic **/
  private byte dead=0, deathCount=0, deadType=0;
  private long deadTime=0, killByTypeId=0, totalKills=0;

  public byte isDead()
  {
    return dead;
  }

  public byte getDeathCount()
  {
    return deathCount;
  }

  public void increaseTotalKills()
  {
    this.totalKills++;
  }

  public long getTotalKills()
  {
    return totalKills;
  }

  public String getDeathInformation()
  {
    return dead+","+deadTime+","+deadType+","+killByTypeId;
  }

  public void die(byte type, long id)
  {
    new ArrayList<>(this.getItems().values()).stream().filter(object -> object!=null).forEach(object -> this.removeItem(object.getGuid(),object.getQuantity(),true,false));
    this.setFuneral();
    this.deathCount++;
    this.deadType=type;
    this.killByTypeId=id;
    SocketManager.GAME_SEND_Im_PACKET_TO_ALL("116;"+"<b>Server</b>"+"~Le joueur ["+this.getName()+"] vient de mourir");
    
  }

  public void revive()
  {
    byte revive=Database.getStatics().getPlayerData().canRevive(this);

    if(revive==1)
    {
      this.curMap=Main.world.getMap((short)7411);
      this.curCell=Main.world.getMap((short)7411).getCase(311);
    }
    else
    {
        this.getStats().addOneStat(125,-this.getStats().getEffect(125));
        this.getStats().addOneStat(124,-this.getStats().getEffect(124));
        this.getStats().addOneStat(118,-this.getStats().getEffect(118));
        this.getStats().addOneStat(123,-this.getStats().getEffect(123));
        this.getStats().addOneStat(119,-this.getStats().getEffect(119));
        this.getStats().addOneStat(126,-this.getStats().getEffect(126));
        int val = 0;
        if(this.getStatsParcho().getEffect(Constant.STATS_ADD_VITA) != 0) {
        	val = this.getStatsParcho().getEffect(Constant.STATS_ADD_VITA);
        	this.getStatsParcho().addOneStat(Constant.STATS_ADD_VITA,-this.getStatsParcho().getEffect(Constant.STATS_ADD_VITA));
        	for(int i=0;i<val;i++)
            {
        		this.boostStat(11,false);
              this.getStatsParcho().addOneStat(125,1);
              
            }
        }
        if(this.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE) != 0) {
        	val = this.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE);
        	this.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE,-this.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE));
        	for(int i=0;i<val;i++)
            {
        		this.boostStat(12,false);
              this.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE,1);
              
            }	
        }
        	
        if(this.getStatsParcho().getEffect(Constant.STATS_ADD_FORC) != 0)
        {
        	val = this.getStatsParcho().getEffect(Constant.STATS_ADD_FORC) ;
        	this.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC,-this.getStatsParcho().getEffect(Constant.STATS_ADD_FORC));
        	
        	for(int i=0;i<val;i++)
            {
        		this.boostStat(10,false);
              this.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC,1);
              
            }	
        }
        	
        
      if(this.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN) != 0)
        {
        	val = this.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN);
        	this.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN,-this.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN));
        	
        	for(int i=0;i<val;i++)
            {
        		this.boostStat(13,false);
              this.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN,1);
              
            }
        }
       if(this.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL) != 0)
        {
        	val = this.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL);
        	this.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL,-this.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL));
        	
        	for(int i=0;i<val;i++)
            {
        		this.boostStat(14,false);	
              this.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL,1);
              
            }
        }
        if(this.getStatsParcho().getEffect(Constant.STATS_ADD_INTE) != 0)
        {
        	val = this.getStatsParcho().getEffect(Constant.STATS_ADD_INTE) ;
        	this.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE,-this.getStatsParcho().getEffect(Constant.STATS_ADD_INTE));
 
        	for(int i=0;i<val;i++)
            {
        	  this.boostStat(15,false);
              this.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE,1);
             
            }
        }
      if(this.getLevel()>=100)
        this.getStats().addOneStat(Constant.STATS_ADD_PA,-1);
      this.setCapital(0);
      //this.getStatsParcho().getMap().clear();
      this._sorts=Constant.getStartSorts(classe);
      this._sortsPlaces=Constant.getStartSortsPlaces(classe);
      this._spellPts=0;
      this.level=1;
      this.exp=0;
      this.curMap=Main.world.getMap(Constant.getStartMap(this.classe));
      this.curCell=this.curMap.getCase(Constant.getStartCell(this.classe));
    }
    this._honor=0;
    this._deshonor=0;
    this._align=0;
    Main.world.kamas_total -= this.kamas;
    this.kamas=0;
    //this._metiers.clear();
    if(this._mount!=null)
    {
      for(GameObject gameObject : this._mount.getObjects().values())
        Main.world.removeGameObject(gameObject.getGuid());
      this._mount.getObjects().clear();

      this.setMount(null);
      this.setMountGiveXp(0);
    }
    this.isGhost=false;
    this.dead=0;
    this.setEnergy(10000);
    this.setGfxId(Integer.parseInt(this.getClasse()+""+this.getSexe()));
    this.setCanAggro(true);
    this.setAway(false);
    this.setSpeed(0);
    for (final ObjectTemplate t : Main.world.getItemSet(5).getItemTemplates()) {
		final GameObject obj = t.createNewItem(1, true);
		if (this.addObjet(obj, true)) {
			World.addGameObject(obj, true);
		}
    }
    Database.getStatics().getPlayerData().setRevive(this);
  }
  /** End heroic **/

  public boolean isGhost()
  {
    return this.isGhost;
  }

  public void setFuneral()
  {
    this.dead=1;
    this.deadTime=System.currentTimeMillis();
    this.setEnergy(-1);
    if(this.isOnMount())
      this.toogleOnMount();
    if(this.get_orientation()==2)
    {
      this.set_orientation(1);
      SocketManager.GAME_SEND_eD_PACKET_TO_MAP(this.getCurMap(),this.getId(),1);
    }
    this.setGfxId(Integer.parseInt(this.getClasse()+"3"));
    SocketManager.send(this,"AR3K"); //Block l'orientation
    SocketManager.send(this,"M112"); //T'es mort!!! t'es mort!!! Mouhhahahahahaaaarg
    SocketManager.GAME_SEND_ALTER_GM_PACKET(getCurMap(),this);
  }

  public void setGhost()
  {
    if(isOnMount())
      toogleOnMount();
    if(Config.getInstance().HEROIC)
    {
      this.setGfxId(Integer.parseInt(this.getClasse()+""+this.getSexe()));
      this.send("GO");
      return;
    }
    if(this.getEnergy()!=0)
      Constant.tpCim(this.getCurMap().getSubArea().getArea().getId(),this);
    this.dead=0;
    this.isGhost=true;
    this.setEnergy(0);
    setGfxId(8004);
    setCanAggro(false);
    setAway(true);
    setSpeed(-40);
    this.regenRate=0;
    SocketManager.send(this,"IH"+Constant.ALL_PHOENIX);
  }

  public void setAlive()
  {
    if(!this.isGhost)
      return;
    this.isGhost=false;
    this.dead=0;
    this.setEnergy(1000);
    this.setPdv(1);
    this.setGfxId(Integer.parseInt(this.getClasse()+""+this.getSexe()));
    this.setCanAggro(true);
    this.setAway(false);
    this.setSpeed(0);
    SocketManager.GAME_SEND_MESSAGE(this,"You have gained <b>1000</b> points of energy.","009900");
    SocketManager.GAME_SEND_STATS_PACKET(this);
    SocketManager.GAME_SEND_ALTER_GM_PACKET(this.curMap,this);
    SocketManager.send(this,"IH");
    SocketManager.send(this,"AR6bk"); //Block l'orientation
  }

  public Map<Integer, Integer> getStoreItems()
  {
    return _storeItems;
  }

  public int needEndFight()
  {
    return hasEndFight;
  }

  public MobGroup hasMobGroup()
  {
    return hasMobGroup;
  }

  public void setNeededEndFight(int hasEndFight, MobGroup group)
  {
    this.endFightAction=null;
    this.hasEndFight=hasEndFight;
    this.hasMobGroup=group;
  }

  public void setNeededEndFightAction(Action endFightAction)
  {
    this.hasEndFight=-2;
    this.endFightAction=endFightAction;
  }

  public boolean castEndFightAction()
  {
    if(this.endFightAction!=null)
    {
      this.endFightAction.apply(this,null,-1,-1);
      this.endFightAction=null;
    }
    else
      return true;
    return false;
  }

  public String parseStoreItemsList()
  {
    StringBuilder list=new StringBuilder();
    if(_storeItems.isEmpty())
      return "";
    for(Entry<Integer, Integer> obj : _storeItems.entrySet())
    {
      GameObject O=World.getGameObject(obj.getKey());
      if(O==null)
        continue;
      list.append(O.getGuid()).append(";").append(O.getQuantity()).append(";").append(O.getTemplate().getId()).append(";").append(O.parseStatsString()).append(";").append(obj.getValue()).append("|");
    }

    return (list.length()>0 ? list.toString().substring(0,list.length()-1) : list.toString());
  }

  public int parseStoreItemsListPods()
  {
    if(_storeItems.isEmpty())
      return 0;
    int total=0;
    for(Entry<Integer, Integer> obj : _storeItems.entrySet())
    {
      GameObject O=World.getGameObject(obj.getKey());
      if(O!=null)
      {
        int qua=O.getQuantity();
        int poidBase1=O.getTemplate().getPod()*qua;
        total+=poidBase1;
      }
    }
    return total;
  }

  public String parseStoreItemstoBD()
  {
    StringBuilder str=new StringBuilder();
    for(Entry<Integer, Integer> _storeObjets : _storeItems.entrySet())
      str.append(_storeObjets.getKey()).append(",").append(_storeObjets.getValue()).append("|");
    return str.toString();
  }

  public void addInStore(int ObjID, int price, int qua)
  {
    GameObject PersoObj=World.getGameObject(ObjID);
    //Si le joueur n'a pas l'item dans son sac ...
    if(PersoObj.isAttach())
      return;
    if(_storeItems.get(ObjID)!=null)
    {
      _storeItems.remove(ObjID);
      _storeItems.put(ObjID,price);
      SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this,this);
      return;
    }

    if(objects.get(ObjID)==null)
    {
      return;
    }

    //Si c'est un item équipé ...
    if(PersoObj.getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
      return;

    GameObject SimilarObj=getSimilarStoreItem(PersoObj);
    int newQua=PersoObj.getQuantity()-qua;
    if(SimilarObj==null)//S'il n'y pas d'item du meme Template
    {
      //S'il ne reste pas d'item dans le sac
      if(newQua<=0)
      {
        //On enleve l'objet du sac du joueur
        removeItem(PersoObj.getGuid());
        //On met l'objet du sac dans le store, avec la meme quantité
        _storeItems.put(PersoObj.getGuid(),price);
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,PersoObj.getGuid());
        SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this,this);
      }
      else
      //S'il reste des objets au joueur
      {
        //on modifie la quantité d'item du sac
        PersoObj.setQuantity(newQua, this);
        //On ajoute l'objet a la banque et au monde
        SimilarObj=GameObject.getCloneObjet(PersoObj,qua);
        World.addGameObject(SimilarObj,true);
        _storeItems.put(SimilarObj.getGuid(),price);

        //Envoie des packets
        SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this,this);
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,PersoObj);

      }
    }
    else
    // S'il y avait un item du meme template
    {
      //S'il ne reste pas d'item dans le sac
      if(newQua<=0)
      {
        //On enleve l'objet du sac du joueur
        removeItem(PersoObj.getGuid());
        //On enleve l'objet du monde
        Main.world.removeGameObject(PersoObj.getGuid());
        //On ajoute la quantité a l'objet en banque
        SimilarObj.setQuantity(SimilarObj.getQuantity()+PersoObj.getQuantity(), this);

        _storeItems.remove(SimilarObj.getGuid());
        _storeItems.put(SimilarObj.getGuid(),price);

        //on envoie l'ajout a la banque de l'objet
        SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this,this);
        //on envoie la supression de l'objet du sac au joueur
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,PersoObj.getGuid());
      }
      else
      //S'il restait des objets
      {
        //on modifie la quantité d'item du sac
        PersoObj.setQuantity(newQua, this);
        SimilarObj.setQuantity(SimilarObj.getQuantity()+qua, this);

        _storeItems.remove(SimilarObj.getGuid());
        _storeItems.put(SimilarObj.getGuid(),price);

        SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this,this);
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,PersoObj);

      }
    }
    SocketManager.GAME_SEND_Ow_PACKET(this);
    //Database.getStatics().getPlayerData().update(this);
  }

  private GameObject getSimilarStoreItem(GameObject exGameObject)
  {
    for(Integer id : _storeItems.keySet())
    {
      GameObject gameObject=World.getGameObject(id);
      if(ConditionParser.stackIfSimilar(gameObject,exGameObject,true))
        return gameObject;
    }

    return null;
  }

  public void removeFromStore(int guid, int qua)
  {
    GameObject SimilarObj=World.getGameObject(guid);
    //Si le joueur n'a pas l'item dans son store ...
    if(_storeItems.get(guid)==null)
    {
      return;
    }

    GameObject PersoObj=getSimilarItem(SimilarObj);
    int newQua=SimilarObj.getQuantity()-qua;
    if(PersoObj==null)//Si le joueur n'avait aucun item similaire
    {
      //S'il ne reste rien en store
      if(newQua<=0)
      {
        //On retire l'item du store
        _storeItems.remove(guid);
        //On l'ajoute au joueur
        objects.put(guid,SimilarObj);

        //On envoie les packets
        SocketManager.GAME_SEND_OAKO_PACKET(this,SimilarObj);
        SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this,this);
      }
    }
    else
    {
      //S'il ne reste rien en store
      if(newQua<=0)
      {
        //On retire l'item de la banque
        _storeItems.remove(SimilarObj.getGuid());
        Main.world.removeGameObject(SimilarObj.getGuid());
        //On Modifie la quantité de l'item du sac du joueur
        PersoObj.setQuantity(PersoObj.getQuantity()+SimilarObj.getQuantity(), this);
        //On envoie les packets
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,PersoObj);
        SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this,this);
      }
    }
    SocketManager.GAME_SEND_Ow_PACKET(this);
    //Database.getStatics().getPlayerData().update(this);
  }

  public void removeStoreItem(int guid)
  {
    _storeItems.remove(guid);
  }

  public void addStoreItem(int guid, int price)
  {
    _storeItems.put(guid,price);
  }

  public int getSpeed()
  {
    return _Speed;
  }

  public void setSpeed(int _Speed)
  {
    this._Speed=_Speed;
  }

  public int get_savestat()
  {
    return this.savestat;
  }

  public void set_savestat(int stat)
  {
    this.savestat=stat;
  }

  public boolean getMetierPublic()
  {
    return _metierPublic;
  }

  public void setMetierPublic(boolean b)
  {
    _metierPublic=b;
  }

  public boolean getLivreArtisant()
  {
    return _livreArti;
  }

  public void setLivreArtisant(boolean b)
  {
    _livreArti=b;
  }

  public boolean hasSpell(int spellID)
  {
    return (getSortStatBySortIfHas(spellID)!=null);
  }

  public void leaveEnnemyFaction()
  {
    if(!isInEnnemyFaction)
      return;//pas en prison on fait pas la commande
    int pGrade=this.getGrade();
    long compar=System.currentTimeMillis()-(enteredOnEnnemyFaction+60000*pGrade);

    switch(pGrade)
    {
      case 1:
        if(compar>=0)
        {
          leaveFaction();
          this.sendMessage("You have been released from prison after one minute of waiting.");
        }
        else
        {
          long restant=-compar;
          if(restant<=1000)
            restant=1000;
          this.sendMessage("You have to wait "+restant/1000+" more seconds before being released.");
        }
        break;
      case 2:
        if(compar>=0)
        {
          leaveFaction();
          this.sendMessage("You have been released from prison after two minutes of waiting.");
        }
        else
        {
          long restant=-compar;
          if(restant<=1000)
            restant=1000;
          this.sendMessage("You have to wait "+restant/1000+" more seconds before being released.");
        }
        break;
      case 3:
        if(compar>=0)
        {
          leaveFaction();
          this.sendMessage("You have been released from prison after three minutes of waiting.");
        }
        else
        {
          long restant=-compar;
          if(restant<=1000)
            restant=1000;
          this.sendMessage("You have to wait "+restant/1000+" more seconds before being released.");
        }
        break;
      case 4:
        if(compar>=0)
        {
          leaveFaction();
          this.sendMessage("You have been released from prison after four minutes of waiting.");
        }
        else
        {
          long restant=-compar;
          if(restant<=1000)
            restant=1000;
          this.sendMessage("You have to wait "+restant/1000+" more seconds before being released.");
        }
        break;
      case 5:
        if(compar>=0)
        {
          leaveFaction();
          this.sendMessage("You have been released from prison after five minutes of waiting.");
        }
        else
        {
          long restant=-compar;
          if(restant<=1000)
            restant=1000;
          this.sendMessage("You have to wait "+restant/1000+" more seconds before being released.");
        }
        break;
      case 6:
        if(compar>=0)
        {
          leaveFaction();
          this.sendMessage("You have been released from prison after six minutes of waiting.");
        }
        else
        {
          long restant=-compar;
          if(restant<=1000)
            restant=1000;
          this.sendMessage("You have to wait "+restant/1000+" more seconds before being released.");
        }
        break;
      case 7:
        if(compar>=0)
        {
          leaveFaction();
          this.sendMessage("You have been released from prison after seven minutes of waiting.");
        }
        else
        {
          long restant=-compar;
          if(restant<=1000)
            restant=1000;
          this.sendMessage("You have to wait "+restant/1000+" more seconds before being released.");
        }
        break;
      case 8:
        if(compar>=0)
        {
          leaveFaction();
          this.sendMessage("You have been released from prison after eight minutes of waiting.");
        }
        else
        {
          long restant=-compar;
          if(restant<=1000)
            restant=1000;
          this.sendMessage("You have to wait "+restant/1000+" more seconds before being released.");
        }
        break;
      case 9:
        if(compar>=0)
        {
          leaveFaction();
          this.sendMessage("You have been released from prison after nine minutes of waiting.");
        }
        else
        {
          long restant=-compar;
          if(restant<=1000)
            restant=1000;
          this.sendMessage("You have to wait "+restant/1000+" more seconds before being released.");
        }
        break;
      case 10:
        if(compar>=0)
        {
          leaveFaction();
          this.sendMessage("You have been released from prison after ten minutes of waiting.");
        }
        else
        {
          long restant=-compar;
          if(restant<=1000)
            restant=1000;
          this.sendMessage("You have to wait "+restant/1000+" more seconds before being released.");
        }
        break;
    }
    //Database.getStatics().getPlayerData().update(this);
  }

  public void leaveEnnemyFactionAndPay(Player perso)
  {
    if(!isInEnnemyFaction)
      return;//pas en prison on fait pas la commande
    int pGrade=perso.getGrade();
    long curKamas=perso.getKamas();
    switch(pGrade)
    {
      case 1:
        if(curKamas<1000)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"You only have "+curKamas+" Kamas. You do not have enough to leave the jail.","009900");
        }
        else
        {
          int countKamas=1000;
          long newKamas=curKamas-countKamas;
          if(newKamas<0)
            newKamas=0;
          perso.setKamas(newKamas);
          leaveFaction();
          SocketManager.GAME_SEND_MESSAGE(perso,"You have paid "+countKamas+" Kamas in order to leave. You have "+newKamas+" Kamas left.","009900");
        }
        break;
      case 2:
        if(curKamas<2000)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"You only have "+curKamas+" Kamas. You do not have enough to leave the jail.","009900");
        }
        else
        {
          int countKamas=2000;
          long newKamas=curKamas-countKamas;
          if(newKamas<0)
            newKamas=0;
          perso.setKamas(newKamas);
          leaveFaction();
          SocketManager.GAME_SEND_MESSAGE(perso,"You have paid "+countKamas+" Kamas in order to leave. You have "+newKamas+" Kamas left.","009900");
        }
        break;
      case 3:
        if(curKamas<3000)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"You only have "+curKamas+" Kamas. You do not have enough to leave the jail.","009900");
        }
        else
        {
          int countKamas=3000;
          long newKamas=curKamas-countKamas;
          if(newKamas<0)
            newKamas=0;
          perso.setKamas(newKamas);
          leaveFaction();
          SocketManager.GAME_SEND_MESSAGE(perso,"You have paid "+countKamas+" Kamas in order to leave. You have "+newKamas+" Kamas left.","009900");
        }
        break;
      case 4:
        if(curKamas<4000)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"You only have "+curKamas+" Kamas. You do not have enough to leave the jail.","009900");
        }
        else
        {
          int countKamas=4000;
          long newKamas=curKamas-countKamas;
          if(newKamas<0)
            newKamas=0;
          perso.setKamas(newKamas);
          leaveFaction();
          SocketManager.GAME_SEND_MESSAGE(perso,"You have paid "+countKamas+" Kamas in order to leave. You have "+newKamas+" Kamas left.","009900");
        }
        break;
      case 5:
        if(curKamas<5000)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"You only have "+curKamas+" Kamas. You do not have enough to leave the jail.","009900");
        }
        else
        {
          int countKamas=5000;
          long newKamas=curKamas-countKamas;
          if(newKamas<0)
            newKamas=0;
          perso.setKamas(newKamas);
          leaveFaction();
          SocketManager.GAME_SEND_MESSAGE(perso,"You have paid "+countKamas+" Kamas in order to leave. You have "+newKamas+" Kamas left.","009900");
        }
        break;
      case 6:
        if(curKamas<7000)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"You only have "+curKamas+" Kamas. You do not have enough to leave the jail.","009900");
        }
        else
        {
          int countKamas=7000;
          long newKamas=curKamas-countKamas;
          if(newKamas<0)
            newKamas=0;
          perso.setKamas(newKamas);
          leaveFaction();
          SocketManager.GAME_SEND_MESSAGE(perso,"You have paid "+countKamas+" Kamas in order to leave. You have "+newKamas+" Kamas left.","009900");
        }
        break;
      case 7:
        if(curKamas<9000)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"You only have "+curKamas+" Kamas. You do not have enough to leave the jail.","009900");
        }
        else
        {
          int countKamas=9000;
          long newKamas=curKamas-countKamas;
          if(newKamas<0)
            newKamas=0;
          perso.setKamas(newKamas);
          leaveFaction();
          SocketManager.GAME_SEND_MESSAGE(perso,"You have paid "+countKamas+" Kamas in order to leave. You have "+newKamas+" Kamas left.","009900");
        }
        break;
      case 8:
        if(curKamas<12000)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"You only have "+curKamas+" Kamas. You do not have enough to leave the jail.","009900");
        }
        else
        {
          int countKamas=12000;
          long newKamas=curKamas-countKamas;
          if(newKamas<0)
            newKamas=0;
          perso.setKamas(newKamas);
          leaveFaction();
          SocketManager.GAME_SEND_MESSAGE(perso,"You have paid "+countKamas+" Kamas in order to leave. You have "+newKamas+" Kamas left.","009900");
        }
        break;
      case 9:
        if(curKamas<16000)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"You only have "+curKamas+" Kamas. You do not have enough to leave the jail.","009900");
        }
        else
        {
          int countKamas=16000;
          long newKamas=curKamas-countKamas;
          if(newKamas<0)
            newKamas=0;
          perso.setKamas(newKamas);
          leaveFaction();
          SocketManager.GAME_SEND_MESSAGE(perso,"You have paid "+countKamas+" Kamas in order to leave. You have "+newKamas+" Kamas left.","009900");
        }
        break;
      case 10:
        if(curKamas<25000)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"You only have "+curKamas+" Kamas. You do not have enough to leave the jail.","009900");
        }
        else
        {
          int countKamas=25000;
          long newKamas=curKamas-countKamas;
          if(newKamas<0)
            newKamas=0;
          perso.setKamas(newKamas);
          leaveFaction();
          SocketManager.GAME_SEND_MESSAGE(perso,"You have paid "+countKamas+" Kamas in order to leave. You have "+newKamas+" Kamas left.","009900");
        }
        break;
    }
    //Database.getStatics().getPlayerData().update(this);
    SocketManager.GAME_SEND_STATS_PACKET(perso);
  }

  public void leaveFaction()
  {
    try
    {
      isInEnnemyFaction=false;
      enteredOnEnnemyFaction=0;
      warpToSavePos();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public void teleportWithoutBlocked(short newMapID, int newCellID)//Aucune condition genre <<en_prison>> etc
  {
    GameClient PW=null;
    if(account.getGameClient()!=null)
    {
      PW=account.getGameClient();
    }
    if(Main.world.getMap(newMapID)==null)
    {
      return;
    }
    if(Main.world.getMap(newMapID).getCase(newCellID)==null)
    {
      return;
    }
    if(PW!=null)
    {
      SocketManager.GAME_SEND_GA2_PACKET(PW,this.getId());
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(curMap,this.getId());
    }
    if(this.getSpioned_by() != null)
    	this.getSpioned_by().teleport(newMapID, newCellID);
    curCell.removePlayer(this);
    curMap=Main.world.getMap(newMapID);
    curCell=curMap.getCase(newCellID);

    //Verification de la Map
    //Verifier la validité du mountpark
    if(curMap.getMountPark()!=null&&curMap.getMountPark().getOwner()>0&&curMap.getMountPark().getGuild().getId()!=-1)
    {
      if(Main.world.getGuild(curMap.getMountPark().getGuild().getId())==null)//Ne devrait pas arriver
      {
        GameMap.removeMountPark(curMap.getMountPark().getGuild().getId());
      }
    }
    //Verifier la validité du Collector
    if(Collector.getCollectorByMapId(curMap.getId())!=null)
    {
      if(Main.world.getGuild(Collector.getCollectorByMapId(curMap.getId()).getGuildId())==null)//Ne devrait pas arriver
      {
        Collector.removeCollector(Collector.getCollectorByMapId(curMap.getId()).getGuildId());
      }
    }

    if(PW!=null)
    {
      SocketManager.GAME_SEND_MAPDATA(PW,newMapID,curMap.getDate(),curMap.getKey());
      curMap.addPlayer(this);
    }

    if(!follower.isEmpty())//On met a jour la Map des personnages qui nous suivent
    {
      for(Player t : follower.values())
      {
        if(t.isOnline())
          SocketManager.GAME_SEND_FLAG_PACKET(t,this);
        else
          follower.remove(t.getId());
      }
    }
  }

  public void teleportFaction(int factionEnnemy)
  {
    short mapID=0;
    int cellID=0;
    enteredOnEnnemyFaction=System.currentTimeMillis();
    isInEnnemyFaction=true;

    switch(factionEnnemy)
    {
      case 1://bonta
        mapID=(short)6164;
        cellID=236;
        break;

      case 2://brakmar
        mapID=(short)6171;
        cellID=397;
        break;

      case 3://Seriane
        mapID=(short)1002;
        cellID=326;
        break;

      default://neutre(WTF? XD)
        mapID=(short)8534;
        cellID=297;
        break;
    }
    this.sendMessage("You have been imprisoned!<br />\nYou have to wait a few minutes before you can go out.<br/>\nSpeak with the prison guard for more information.");
    if(this.getEnergy()<=0)
    {
      if(isOnMount())
        toogleOnMount();
      this.isGhost=true;
      setGfxId(8004);
      setCanAggro(false);
      setAway(true);
      setSpeed(-40);
    }
    teleportWithoutBlocked(mapID,cellID);
   // Database.getStatics().getPlayerData().update(this);
  }

  public GameObject getItemGuid(int IDtemplate)
  {

    for(Entry<Integer, GameObject> value : objects.entrySet())
    {
    	if(World.getGameObject(value.getKey()) == null)
    			continue;
      GameObject obj2=World.getGameObject(value.getKey());
      if(obj2.getTemplate() == null)
    	  continue;
      if(obj2.getTemplate().getId()==IDtemplate)
        return obj2;
    }

    return null;
  }

  public String parsecolortomount()
  {
    int color1=this.getColor1(),color2=this.getColor2(),color3=this.getColor3();
    if(this.getObjetByPos(Constant.ITEM_POS_MALEDICTION)!=null)
      if(this.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId()==10838)
      {
        color1=16342021;
        color2=16342021;
        color3=16342021;
      }
    return (color1==-1 ? "" : Integer.toHexString(color1))+","+(color2==-1 ? "" : Integer.toHexString(color2))+","+(color3==-1 ? "" : Integer.toHexString(color3));
  }

  public boolean addObjetWithOAKO(GameObject objet, boolean Similer)
  {
    for(Entry<Integer, GameObject> entry : objects.entrySet())
    {
      GameObject obj=entry.getValue();
      if(obj.getTemplate().getId()==objet.getTemplate().getId()&&obj.getStats().isSameStats(objet.getStats())&&Similer&&objet.getTemplate().getType()!=85&&obj.getPosition()==-1)
      {
        obj.setQuantity(obj.getQuantity()+objet.getQuantity(), this);
        if(isOnline)
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,obj);
        return false;
      }

      objects.put(objet.getGuid(),objet);
      SocketManager.GAME_SEND_OAKO_PACKET(this,objet);
    }
    return true;
  }

  public boolean addObjetSimiler(GameObject objet, boolean hasSimiler, int oldID)
  {
    ObjectTemplate objModelo=objet.getTemplate();
    if(objModelo.getType()==85||objModelo.getType()==18)
      return false;
    if(hasSimiler)
    {
      for(Entry<Integer, GameObject> entry : objects.entrySet())
      {
        GameObject obj=entry.getValue();
        if(obj.getPosition()==-1&&obj.getGuid()!=oldID&&obj.getTemplate().getId()==objModelo.getId()&&obj.getStats().isSameStats(objet.getStats())&&ConditionParser.stackIfSimilar(obj,objet,hasSimiler))
        {
          obj.setQuantity(obj.getQuantity()+objet.getQuantity(), this);
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,obj);
          return true;
        }
      }
    }
    return false;
  }

  public Map<Integer, Pair<Integer, Integer>> getItemClasseSpell()
  {
    return _itemClasseSpell;
  }

  public void removeItemClasseSpell(int spell)
  {
    if(_itemClasseSpell.containsKey(spell))
    {
      _itemClasseSpell.remove(spell);
    }
  }

  public void addItemClasseSpell(int spell, int effect, int modif)
  {
    if(!_itemClasseSpell.containsKey(spell))
    {
      _itemClasseSpell.put(spell,new Pair<Integer, Integer>(effect,modif));
    }
  }

  public ArrayList<Integer> getItemClasse()
  {
    return _itemClasse;
  }

  public void setItemClasse(ArrayList<Integer> ItemClasse)
  {
    _itemClasse=ItemClasse;
  }

  public void addItemClasse(int item)
  {
    if(!_itemClasse.contains(item))
      _itemClasse.add(item);
  }

  public void removeItemClasse(int item)
  {
    if(_itemClasse.contains(item))
    {
      int index=_itemClasse.indexOf(item);
      _itemClasse.remove(index);
    }
  }

  public void refreshItemClasse(Player perso2)
  {
    for(int j=2;j<8;j++)
    {
      if(getObjetByPos(j)==null)
        continue;
      GameObject obj=getObjetByPos(j);
      int template=obj.getTemplate().getId();
      int pano=obj.getTemplate().getPanoId();
      if((pano>=81&&pano<=92)||(pano>=201&&pano<=212))
      {
        String[] stats=obj.getTemplate().getStrTemplate().split(",");
        for(String stat : stats)
        {
          String[] val=stat.split("#");
          int effect=Integer.parseInt(val[0],16);
          int spell=Integer.parseInt(val[1],16);
          int modif=Integer.parseInt(val[3],16);
          if(effect == 282)
        	  modif = 1;
          String modifi=effect+";"+spell+";"+modif;
          if(perso2 != null) {
        	  SocketManager.SEND_SB_SPELL_BOOST(perso2,modifi);  
          }
          SocketManager.SEND_SB_SPELL_BOOST(this,modifi);
          addItemClasseSpell(spell,effect,modif);
        }
        if(!_itemClasse.contains(template))
          _itemClasse.add(template);
      }
    }
  }

  public int getItemClasseModif(int spell, int effect)
  {
    int modif=0;
    if(_bendHechizo==spell&&_bendEfecto==effect)
    {
      modif+=_bendModif;
    }
    if(_itemClasseSpell.containsKey(spell))
    {
      if(_itemClasseSpell.get(spell).getLeft()==effect)
      {
        modif+=_itemClasseSpell.get(spell).getRight();
        return modif;
      }
    }
    return modif;
  }

  public int storeAllBuy()
  {
    int total=0;
    for(Entry<Integer, Integer> value : _storeItems.entrySet())
    {
      GameObject O=World.getGameObject(value.getKey());
      int multiple=O.getQuantity();
      int add=value.getValue()*multiple;
      total+=add;
    }

    return total;
  }

  public void DialogTimer()
  {
	  TimerWaiterPlus.addNext(() -> {
      if(this.getExchangeAction()==null||this.getExchangeAction().getType()!=ExchangeAction.TRADING_WITH_COLLECTOR)
        return;
      if((Integer)this.getExchangeAction().getValue()!=0)
      {
        Collector Collector=Main.world.getCollector((Integer)this.getExchangeAction().getValue());
        if(Collector==null)
          return;
        Collector.reloadTimer();
        for(Player z : Main.world.getGuild(Collector.getGuildId()).getOnlineMembers())
        {
          if(!z.isOnline)
            z.setOnline(true);
          if(z.get_guild() == null)
        	  continue;
          SocketManager.GAME_SEND_gITM_PACKET(z,soufix.entity.Collector.parseToGuild(z.get_guild().getId()));
          String str="G"+Collector.getN1()+","+Collector.getN2()+"|.|"+Main.world.getMap(Collector.getMap()).getX()+"|"+Main.world.getMap(Collector.getMap()).getY()+"|"+getName()+"|"+Collector.getXp()+";";
          if(!Collector.getLogObjects().equals(""))
            str+=Collector.getLogObjects();
          Player.this.getGuildMember().giveXpToGuild(Collector.getXp());
          SocketManager.GAME_SEND_gT_PACKET(z,str);
        }
        getCurMap().RemoveNpc(Collector.getId());
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getCurMap(),Collector.getId());
        Collector.delCollector(Collector.getId());
        Database.getDynamics().getCollectorData().delete(Collector.getId());
      }
      //Database.getStatics().getPlayerData().update(getAccount().getCurrentPlayer());
      SocketManager.GAME_SEND_EV_PACKET(getGameClient());
      setAway(false);
    },5*60*1000);
  }

  public long getTimeTaverne()
  {
    return timeTaverne;
  }

  public void setTimeTaverne(long timeTaverne)
  {
    this.timeTaverne=timeTaverne;
    //Database.getStatics().getPlayerData().updateTimeTaverne(this);
  }

  public GameAction getGameAction()
  {
    return _gameAction;
  }

  public void setGameAction(GameAction Action)
  {
    _gameAction=Action;
  }

  public int getAlignMap()
  {
    if(this.getCurMap().getSubArea()==null)
      return -1;
    if(this.getCurMap().getSubArea().getAlignement()==0)
      return 1;
    if(this.getCurMap().getSubArea().getAlignement()==this.get_align())
      return 1;
    return -1;
  }

  public List<Integer> getEmotes()
  {
    return emotes;
  }

  public void addStaticEmote(int emote)
  {
    if(this.emotes.contains(emote))
      return;
    this.emotes.add(emote);
    if(!isOnline())
      return;
    SocketManager.GAME_SEND_EMOTE_LIST(this,getCompiledEmote(getEmotes()));
    SocketManager.GAME_SEND_STATS_PACKET(this);
    SocketManager.send(this,"eA"+emote);
  }

  public String parseEmoteToDB()
  {
    StringBuilder str=new StringBuilder();
    boolean isFirst=true;
    for(int i : emotes)
    {
      if(isFirst)
        str.append(i).append("");
      else
        str.append(";").append(i);
      isFirst=false;
    }
    return str.toString();
  }

  public boolean getBlockMovement()
  {
    return this.isBlocked;
  }

  public void setBlockMovement(boolean b)
  {
    this.isBlocked=b;
  }

  public GameClient getGameClient()
  {
    return this.getAccount().getGameClient();
  }

  public void send(String packet)
  {
    SocketManager.send(this,packet);
  }

  public void sendMessage(String msg)
  {
    SocketManager.GAME_SEND_MESSAGE(this,msg);
  }

  public void sendServerMessage(String msg)
  {
    this.send("Im116;<b>Server</b>~"+msg);
  }

  public boolean isSubscribe()
  {
    return !Config.getInstance().useSubscribe||this.getAccount().isSubscribe();
  }

  public boolean isInAreaNotSubscribe()
  {
    boolean ok=Config.getInstance().useSubscribe;

    if(this.curMap==null)
      return false;
    switch(this.curMap.getId())
    {
      case 6824:
      case 6825:
      case 6826:
        return false;
    }
    if(this.curMap.getSubArea()==null)
      return false;
    if(this.curMap.getSubArea().getArea()==null)
      return false;
    if(this.curMap.getSubArea().getArea().getSuperArea()==3||this.curMap.getSubArea().getArea().getSuperArea()==4||this.curMap.getSubArea().getArea().getId()==18)
      ok=false;

    return ok;
  }

  public boolean cantDefie()
  {
    return getCurMap().noDefie;
  }

  public boolean cantAgro()
  {
    return getCurMap().noAgro;
  }

  public boolean cantCanal()
  {
    return getCurMap().noCanal;
  }

  public boolean cantTP()
  {
    return !this.isInPrison()&&getCurMap().noTP;
  }

  public boolean isInPrison()
  {
    if(this.curMap==null)
      return false;

    switch(this.curMap.getId())
    {
      case 666:
      case 8726:
        return true;
    }
    return false;
  }

  public void addQuestPerso(QuestPlayer qPerso)
  {
    questList.put(qPerso.getId(),qPerso);
  }

  public void delQuestPerso(int key)
  {
    this.questList.remove(key);
  }

  public Map<Integer, QuestPlayer> getQuestPerso()
  {
    return questList;
  }

  public QuestPlayer getQuestPersoByQuest(Quest quest)
  {
    for(QuestPlayer questPlayer : this.questList.values())
      if(questPlayer!=null&&questPlayer.getQuest().getId()==quest.getId())
        return questPlayer;
    return null;
  }

  public QuestPlayer getQuestPersoByQuestId(int id)
  {
    for(QuestPlayer qPerso : questList.values())
      if(qPerso.getQuest().getId()==id)
        return qPerso;
    return null;
  }

  public String getQuestGmPacket()
  {
    StringBuilder packet=new StringBuilder();
    int nb=0;
    packet.append("+");
    for(QuestPlayer qPerso : questList.values())
    {
      packet.append(qPerso.getQuest().getId()).append(";");
      packet.append(qPerso.isFinish() ? 1 : 0);
      if(nb<questList.size()-1)
        packet.append("|");
      nb++;
    }
    return packet.toString();
  }

  public House getInHouse()
  {
    return _curHouse;
  }

  public void setInHouse(House h)
  {
    _curHouse=h;
  }

  public int getIsOnDialogAction()
  {
    return this.action;
  }

  public void setIsOnDialogAction(int action)
  {
    this.action=action;
  }

  private ExchangeAction<?> exchangeAction;

  public ExchangeAction<?> getExchangeAction()
  {
    return exchangeAction;
  }

  public void setExchangeAction(ExchangeAction<?> exchangeAction)
  {
    if(exchangeAction==null)
      this.setAway(false);
    this.exchangeAction=exchangeAction;
  }

  public void setisForgetingSpell(final boolean isForgetingSpell)
  {
    this._isForgetingSpell=isForgetingSpell;
  }

  public boolean isForgetingSpell()
  {
    return this._isForgetingSpell;
  }

  public void setTokens(final int i)
  {
    this.tokens=i;
    Database.getStatics().getPlayerData().updateTokens(this.name,this.tokens);
  }

  public int getTokens()
  {
    return this.tokens=Database.getStatics().getPlayerData().loadTokens(this.name);
  }

  public void setCurJobAction(final JobAction JA)
  {
    this._curJobAction=JA;
  }

  public JobAction getCurJobAction()
  {
    return this._curJobAction;
  }
 /*
  //2.0 - Exo Limit
  public void setApExo(final int i)
  {
    this.apExo=i;
    Database.getStatics().getPlayerData().updateApExo(this.id,this.apExo);
  }

  //2.0 - Exo Limit
  public void setMpExo(final int i)
  {
    this.mpExo=i;
    Database.getStatics().getPlayerData().updateMpExo(this.id,this.mpExo);
  }

  //2.0 - Exo Limit
  public void setRaExo(final int i)
  {
    this.raExo=i;
    Database.getStatics().getPlayerData().updateRaExo(this.id,this.raExo);
  }

  //2.0 - Exo Limit
  public int getApExo()
  {
    return this.apExo=Database.getStatics().getPlayerData().loadApExo(this.name);
  }

  //2.0 - Exo Limit
  public int getMpExo()
  {
    return this.mpExo=Database.getStatics().getPlayerData().loadMpExo(this.name);
  }

  //2.0 - Exo Limit
  public int getRaExo()
  {
    return this.raExo=Database.getStatics().getPlayerData().loadRaExo(this.name);
  }*/

  public boolean getWorldMarket()
  {
    return worldMarket;
  }

  public void setWorldMarket(boolean worldMarket)
  {
    this.worldMarket=worldMarket;
  }

  public GameMap getFollowerMap()
  {
    return followerMap;
  }

  public void setFollowerMap(GameMap followerMap)
  {
    this.followerMap=followerMap;
  }

  public int getFollowerCell()
  {
    return followerCell;
  }

  public void setFollowerCell(int followerCell)
  {
    this.followerCell=followerCell;
  }

  public Pair<InteractiveObject, GameCase> getInInteractiveObject()
  {
    return inInteractiveObject;
  }

  public void setInInteractiveObject(Pair<InteractiveObject, GameCase> inInteractiveObject)
  {
    this.inInteractiveObject=inInteractiveObject;
  }

  public boolean getCanDrop()
  {
    return canDrop;
  }

  public void setCanDrop(boolean canDrop)
  {
    this.canDrop=canDrop;
  }

  public Map<Integer, Spell.SortStats> getSorts()
  {
    return _sorts;
  }
  public boolean getAutoSkip()
  {
    return autoSkip;
  }

public boolean isOne_windows() {
	return One_windows;
}

public void setOne_windows(boolean one_windows) {
	One_windows = one_windows;
}


  public void setAutoSkip(boolean autoSkip)
  {
    this.autoSkip=autoSkip;
  }
  public void setClasse(int classe) {
	this.classe = classe;
}
  public void setsorts(Map<Integer, Spell.SortStats> _sorts) {
		this._sorts = _sorts;
	}
  public GameObject chekhackitem(final int pos) {
		if (pos == Constant.ITEM_POS_NO_EQUIPED)
			return null;
		int chek = 0;
		for (Entry<Integer, GameObject> entry : objects.entrySet()) {
			final GameObject obj = entry.getValue();
			if (obj.getPosition() == pos){
				if (pos != -1 && (obj.getTemplate().getPanoId() != -1 || obj.getTemplate().getType() == 23)
						&& this.hasEquiped_chek(obj.getTemplate().getId(),obj.getGuid())) {
					this.unequipedObjet(this.getObjetByPos(pos));
				}	
			chek++;	
			}
			if(chek >= 2){
				this.unequipedObjet(this.getObjetByPos(pos));		
			}
			if(pos == Constant.ITEM_POS_FAMILIER ) {
			if(_onMount && chek >= 1) {
			this.unequipedObjet(this.getObjetByPos(pos));	
			}
			}
		}
		return null;
	}
  public boolean hasEquiped_chek(final int id,final int guid) {
			for (final Map.Entry<Integer, GameObject> entry : this.objects.entrySet()) {
				if(entry.getValue() == null)continue;
				if(entry.getValue().getTemplate() == null)continue;
				if(entry.getValue().getGuid() == guid)continue;
				if (entry.getValue().getTemplate().getId() == id && entry.getValue().getPosition() != -1) {
					return true;
				}
			}
		return false;
  }
  public void itemchek (){
		this.chekhackitem(Constant.ITEM_POS_AMULETTE);	
		this.chekhackitem(Constant.ITEM_POS_ARME);
		this.chekhackitem(Constant.ITEM_POS_ANNEAU1);
		this.chekhackitem(Constant.ITEM_POS_CEINTURE);
		this.chekhackitem(Constant.ITEM_POS_ANNEAU2);
		this.chekhackitem(Constant.ITEM_POS_BOTTES);
		this.chekhackitem(Constant.ITEM_POS_COIFFE);
		this.chekhackitem(Constant.ITEM_POS_CAPE);
		this.chekhackitem(Constant.ITEM_POS_FAMILIER);
		this.chekhackitem(Constant.ITEM_POS_DOFUS1);
		this.chekhackitem(Constant.ITEM_POS_DOFUS2);
		this.chekhackitem(Constant.ITEM_POS_DOFUS3);
		this.chekhackitem(Constant.ITEM_POS_DOFUS4);
		this.chekhackitem(Constant.ITEM_POS_DOFUS5);
		this.chekhackitem(Constant.ITEM_POS_DOFUS6);
		this.chekhackitem(Constant.ITEM_POS_BOUCLIER);	
	}
  
	public void addSetRapido(int id, String nombre, int icono, String data) {
		SetRapido set = new SetRapido(id, nombre, icono, data);
		_setsRapidos.put(set.getID(), set);
	}
	
	public void borrarSetRapido(int id) {
		_setsRapidos.remove(id);
	}
	
	public SetRapido getSetRapido(int id) {
		return _setsRapidos.get(id);
	}
	
	public String getSetsRapidos() {
		StringBuilder str = new StringBuilder();
		for (SetRapido s : _setsRapidos.values()) {
			if (str.length() > 0) {
				str.append("*");
			}
			str.append(s.getString());
		}
		return str.toString();
	}
	
	public void actualizarSetsRapido(int oldID, int newID, byte oldPos, byte newPos) {
		boolean b = false;
		for (SetRapido set : _setsRapidos.values()) {
			b |= set.actualizarObjetos(oldID, newID, oldPos, newPos);
		}
		if (b) {
			SocketManager.ENVIAR_Os_SETS_RAPIDOS(this.getGameClient());
		}
	}

	public int getPuntos() {
		return getAccount().getOgrinas();
	}

	public void setPuntos(int puntos) {
		getAccount().setPoints(getPuntos() + puntos);
	}

	public void crearItemTienda(int itemid, int cantidad, boolean max) {
		GameObject newObjAdded = Main.world.getObjTemplate(itemid).createNewItem(cantidad, max);
		if (!addObjetSimiler(newObjAdded, false, -1)) {
			World.addGameObject(newObjAdded, true);
			addObjet(newObjAdded);
			// newObjAdded.getStats().addOneStat(428, 1);
			newObjAdded.getStats().addOneStat(900, 1);
			SocketManager.GAME_SEND_UPDATE_ITEM(this, newObjAdded);
			SocketManager.GAME_SEND_Im_PACKET(this, "021;" + cantidad + "~" + newObjAdded.getTemplate().getId());
			SocketManager.GAME_SEND_Ow_PACKET(this);//427
			SocketManager.GAME_SEND_STATS_PACKET(this);
		}
	}

	public boolean DeboostSpell(int spellID) {
	      if (getSortStatBySortIfHas(spellID) == null)
	          return false;
	      int AncLevel = getSortStatBySortIfHas(spellID).getLevel();
	      if (AncLevel == 1)
	          return false;
	      if (Main.world.getSort(spellID).getStatsByLevel(AncLevel - 1).getReqLevel() <= this.getLevel()) {
	          if (learnSpell(spellID, AncLevel - 1, true, false, false)) {
	              _spellPts += AncLevel-1;
	              Database.getStatics().getPlayerData().update(this);
	              return true;
	          } else {
	              return false;
	          }
	      }
	      return away;
	  }

  	public void refresh() {
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getCurMap(), getId());
		SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(getCurMap(), this);
	}
  	
	  /** ORNEMENT **/
	  private List<Integer> ornementsList = new ArrayList<>();
	  private int ornement = 0;

	  public int getOrnement() {
	      return ornement;
	  }
	  public void setOrnement(int ornement) {
	      this.ornement = ornement;
	  }
	  /** ORNEMENT **/
	  
//** ORNEMENT **//
	  


	  public List<Integer> getOrnementsList() {
	      return ornementsList;
	  }

	  public void setOrnementsList(List<Integer> ornementsList) {
	      this.ornementsList = ornementsList;
	  }
	  public String parseOrnementsToDB() {
	      StringBuilder str = new StringBuilder();
	      boolean isFirst = true;
	      for (int i : this.ornementsList) {
	          if (isFirst)
	              str.append(i).append("");
	          else
	              str.append(",").append(i);
	          isFirst = false;
	      }
	      return str.toString();
	  }
	      public void addOrnements(int id) {
	          if(!this.ornementsList.contains(id))
	              this.ornementsList.add(id);
	      }
	  //** ORNEMENT**//

	      public void AddItem(int id, int qua) {
	          ObjectTemplate t = Main.world.getObjTemplate(id);
	          if (t == null) {
	             this.sendMessage("Le template " + id + " n'existe pas.");
	              return;
	          }
	          GameObject obj = t.createNewItem(qua, false);

	          if(t.getType() == Constant.ITEM_TYPE_CERTIF_MONTURE) {
	              //obj.setMountStats(this.getPlayer(), null);
	              Mount mount = new Mount(Constant.getMountColorByParchoTemplate(obj.getTemplate().getId()), getId(), false);
	              obj.clearStats();
	              obj.getStats().addOneStat(995, - (mount.getId()));
	              obj.getTxtStat().put(996, getName());
	              obj.getTxtStat().put(997, mount.getName());
	              mount.setToMax();
	          }
	          if (addObjet(obj, true))//Si le joueur n'avait pas d'item similaire
	              World.addGameObject(obj, true);
	          SocketManager.GAME_SEND_Im_PACKET(this, "021;" + qua + "~" + id);
	      }

	  	public void RemoveItem(int id, int qua) {
			this.removeByTemplateID(id, qua);
			SocketManager.GAME_SEND_Im_PACKET(this, "022;" + qua + "~" + id);
		}


}