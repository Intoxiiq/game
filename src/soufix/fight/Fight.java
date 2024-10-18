package soufix.fight;

import soufix.area.SubArea;
import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.client.other.Party;
import soufix.client.other.Stalk;
import soufix.common.ConditionParser;
import soufix.common.Formulas;
import soufix.common.PathFinding;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.Collector;
import soufix.entity.Prism;
import soufix.entity.boss.Bandit;
import soufix.entity.monster.MobGrade;
import soufix.entity.monster.MobGroup;
import soufix.entity.monster.Monster;
import soufix.entity.mount.Mount;
import soufix.entity.pet.PetEntry;
import soufix.fight.ia.IAHandler;
import soufix.fight.spells.LaunchedSpell;
import soufix.fight.spells.Spell;
import soufix.fight.spells.SpellEffect;
import soufix.fight.spells.Spell.SortStats;
import soufix.fight.traps.Glyph;
import soufix.fight.traps.Trap;
import soufix.fight.turn.Turn;
import soufix.game.GameClient;
import soufix.game.World;
import soufix.game.World.Drop;
import soufix.game.action.GameAction;
import soufix.game.scheduler.entity.RespawnGroup;
import soufix.guild.Guild;
import soufix.job.JobStat;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.object.ObjectTemplate;
import soufix.object.entity.Capture;
import soufix.other.Action;
import soufix.quest.Quest;
import soufix.quest.QuestPlayer;
import soufix.utility.Pair;
import soufix.utility.TimerWaiterPlus;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Fight
{
  private int id, state=0, guildId=-1, type=-1;
  private int st1, st2;
  private int curPlayer, captWinner=-1;
  private int curFighterPa, curFighterPm;
  private int curFighterUsedPa, curFighterUsedPm;
  private final Map<Integer, Fighter> team0=new HashMap<>();
  private final Map<Integer, Fighter> team1=new HashMap<>();
  private final ArrayList<Pair<Integer, Fighter>> deadList=new ArrayList<Pair<Integer, Fighter>>();
  private final ConcurrentHashMap<Integer, Player> viewer=new ConcurrentHashMap<>();
  private ArrayList<GameCase> start0=new ArrayList<>();
  private ArrayList<GameCase> start1=new ArrayList<>();
  private final Map<Integer, Challenge> allChallenges=new HashMap<>();
  private final Map<Integer, GameCase> rholBack=new HashMap<>();
  private final List<Glyph> allGlyphs=new ArrayList<>();
  private final List<Trap> allTraps=new ArrayList<>();
  private List<Fighter> orderPlaying=new ArrayList<>();
  private final ArrayList<Fighter> capturer=new ArrayList<>(8);
  private final ArrayList<Fighter> trainer=new ArrayList<>(8);
  private long launchTime=0, startTime=0;
  private boolean locked0=false, locked1=false;
  private boolean onlyGroup0=false, onlyGroup1=false;
  private boolean help0=false, help1=false;
  private boolean viewerOk=true;
  private boolean haveKnight=false;
  private boolean isBegin=false;
  private boolean checkTimer=false;
  private boolean finish=false;
  private boolean collectorProtect=false;
  private boolean STAFF=false;
  private boolean MONO=false;
  private boolean DUO=false;
  private boolean bLock_join=false;
  private String curAction="";
  private MobGroup mobGroup;
  private Collector collector;
  private Prism prism;
  private GameMap map, mapOld;
  private Fighter init0, init1;
  private Capture fullSoul;
  private Turn turn;
  private String defenders="";
  private int trainerWinner=-1;
  private int nextId=-100;
  private boolean donjon;
  private boolean archi;
  private boolean DROP_ONE_TIME;
  //v2.3 - Spectator join PvM
  public int startGuid=-1;
  public ArrayList<Pair<Fighter, ArrayList<SpellEffect>>> buffsToAdd=new ArrayList<>();
	private boolean invocationAlreadySummoned = false;

  public Fight(int type, int id, GameMap map, Player perso, Player init2)
  {
	  if(perso == null || init2 == null)
		  return;
    launchTime=System.currentTimeMillis();
    setType(type); // 0: Dï¿½fie (4: Pvm) 1:PVP (5:Perco)
    setId(id);
    setMap(map.getMapCopy());
    setMapOld(map);
    setInit0(new Fighter(this,perso));
    setInit1(new Fighter(this,init2));
    getTeam0().put(perso.getId(),getInit0());
    getTeam1().put(init2.getId(),getInit1());
    if(perso.getGroupe() != null)
    if(perso.getGroupe().getId() > 1 && perso.getGroupe().getId() < 7) {
        STAFF = true;	
        }
    if(getInit0().getPersonnage() == null || getInit1().getPersonnage() == null)
    	return;
    SocketManager.GAME_SEND_GDF_PACKET_TO_FIGHT(perso,this.getMap().getCases());
    // on desactive le timer de regen cotï¿½ client
    if(getType()!=Constant.FIGHT_TYPE_CHALLENGE)
      scheduleTimer(45);
    int cancelBtn=getType()==Constant.FIGHT_TYPE_CHALLENGE ? 1 : 0;
    long time=getType()==Constant.FIGHT_TYPE_CHALLENGE ? 0 : Constant.TIME_START_FIGHT;
    SocketManager.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this,7,2,cancelBtn,1,0,time,getType());
    if(init2.get_align()==0&&(map.getSubArea()!=null&&map.getSubArea().getAlignement() == 0))
      setHaveKnight();

    int morph=perso.getGfxId();
    if(morph==1109||morph==1046||morph==9001)
    {
      perso.unsetFullMorph();
      SocketManager.GAME_SEND_ALTER_GM_PACKET(perso.getCurMap(),perso);
    }

    this.start0=Main.world.getCryptManager().parseStartCell(getMap(),0);
    this.start1=Main.world.getCryptManager().parseStartCell(getMap(),1);
    SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlaces(),0);
    SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,2,getMap().getPlaces(),1);
    setSt1(0);
    setSt2(1);

    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTE+",0");
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTEUR+",0");
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,init2.getId()+"",init2.getId()+","+Constant.ETAT_PORTE+",0");
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,init2.getId()+"",init2.getId()+","+Constant.ETAT_PORTEUR+",0");

    getInit0().setCell(getRandomCell(this.start0));
    getInit1().setCell(getRandomCell(this.start1));

    if(getInit0().getPersonnage().getCurCell() != null)
    getInit0().getPersonnage().getCurCell().removePlayer(getInit0().getPersonnage());
    if(getInit1().getPersonnage().getCurCell() != null)
    getInit1().getPersonnage().getCurCell().removePlayer(getInit1().getPersonnage());

    getInit0().getCell().addFighter(getInit0());
    getInit1().getCell().addFighter(getInit1());
    getInit0().getPersonnage().setFight(this);
    getInit0().setTeam(0);
    getInit1().getPersonnage().setFight(this);
    getInit1().setTeam(1);
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit0().getId());
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit1().getPersonnage().getCurMap(),getInit1().getId());
    if(getType()==1)
    {
      SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),0,getInit0().getId(),getInit1().getId(),getInit0().getPersonnage().getCurCell().getId(),"0;"+getInit0().getPersonnage().get_align(),getInit1().getPersonnage().getCurCell().getId(),"0;"+getInit1().getPersonnage().get_align());
    }
    else
    {
      SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),0,getInit0().getId(),getInit1().getId(),getInit0().getPersonnage().getCurCell().getId(),"0;-1",getInit1().getPersonnage().getCurCell().getId(),"0;-1");
    }
    SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit0().getId(),getInit0());
    SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit1().getId(),getInit1());

    SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,getMap());

    setState(Constant.FIGHT_STATE_PLACE);
  }

  public Fight(int id, GameMap map, Player perso, MobGroup group)
  {
	  if(perso == null)
		return;  
    launchTime=System.currentTimeMillis();
    setCheckTimer(true);
    setMobGroup(group);
    demorph(perso);
    if(perso.getGroupe() != null)
    if(perso.getGroupe().getId() > 1 && perso.getGroupe().getId() < 7) {
    STAFF = true;	
    }
    setType(Constant.FIGHT_TYPE_PVM); // (0: Dï¿½fie) 4: Pvm (1:PVP) (5:Perco)
    setId(id);
    setMap(map.getMapCopy());
    setMapOld(map);
    setInit0(new Fighter(this,perso));
    setStartGuid(perso.getId());
    getTeam0().put(perso.getId(),getInit0());
    for(Entry<Integer, MobGrade> entry : group.getMobs().entrySet())
    {
      entry.getValue().setInFightID(entry.getKey());
      Fighter mob=new Fighter(this,entry.getValue());
      getTeam1().put(entry.getKey(),mob);
      if(entry.getValue().getTemplate().getId()==599) //Zilla
        map.zillaTimer();
    }
    if(getInit0().getPersonnage() == null)
    	return;

    SocketManager.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this,1,2,0,1,0,45000,getType());
    SocketManager.GAME_SEND_GDF_PACKET_TO_FIGHT(perso,this.getMap().getCases());
    // on desactive le timer de regen cotï¿½ client

    scheduleTimer(45);
    this.start0=Main.world.getCryptManager().parseStartCell(getMap(),0);
    this.start1=Main.world.getCryptManager().parseStartCell(getMap(),1);
    SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlaces(),0);
    setSt1(0);
    setSt2(1);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTE+",0");
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTEUR+",0");
    List<Entry<Integer, Fighter>> e=new ArrayList<>();
    e.addAll(getTeam1().entrySet());
    for(Entry<Integer, Fighter> entry : e)
    {
      Fighter f=entry.getValue();
      GameCase cell=getRandomCell(getStart1());
      if(cell==null)
      {
        getTeam1().remove(f.getId());
        continue;
      }
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,f.getId()+"",f.getId()+","+Constant.ETAT_PORTE+",0");
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,f.getId()+"",f.getId()+","+Constant.ETAT_PORTEUR+",0");
      f.setCell(cell);
      f.getCell().addFighter(f);
      f.setTeam(1);
      f.fullPdv();
    }
    getInit0().setCell(getRandomCell(getStart0()));

    if(getInit0().getPersonnage().getCurCell()!=null)
      getInit0().getPersonnage().getCurCell().removePlayer(getInit0().getPersonnage());

    getInit0().getCell().addFighter(getInit0());

    getInit0().getPersonnage().setFight(this);
    getInit0().setTeam(0);
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit0().getId());
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPersonnage().getCurMap(),group.getId());

    int c=PathFinding.getNearestCellAround(getInit0().getPersonnage().getCurMap(),getInit0().getPersonnage().getCurCell().getId(),group.getCellId(),new ArrayList<>());
    if(c<0)
      c=getInit0().getPersonnage().getCurCell().getId();

    SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),4,getInit0().getId(),group.getId(),c,"0;-1",group.getCellId(),"1;-1");
    SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit0().getId(),getInit0());
    for(Fighter f : getTeam1().values())
      SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),group.getId(),f);
    SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,getMap());
    setState(Constant.FIGHT_STATE_PLACE);
    if(group.isFix()) //players won against fixed mobgroup
        this.getMapOld().spawnAfterTimeGroupFix(mobGroup.getSpawnCellId(),this.getMapOld().getMinRespawnTime(),this.getMapOld().getMaxRespawnTime());
      
  }

  public Fight(int id, GameMap map, Player perso, MobGroup group, int type,boolean protecteur)
  {
	  if(perso == null)
		  return;
    launchTime=System.currentTimeMillis();
    setMobGroup(group);
    setType(type); // (0: Dï¿½fie) 4: Pvm (1:PVP) (5:Perco)
    setId(id);
    setMap(map.getMapCopy());
    setMapOld(map);
    demorph(perso);
    setInit0(new Fighter(this,perso));
    getTeam0().put(perso.getId(),getInit0());
    for(Entry<Integer, MobGrade> entry : group.getMobs().entrySet())
    {
      entry.getValue().setInFightID(entry.getKey());
      Fighter mob=new Fighter(this,entry.getValue());
      getTeam1().put(entry.getKey(),mob);
      if(entry.getValue().getTemplate().getId()==599) //Zilla
        map.zillaTimer();
    }

    if(perso.getCurPdv()>=perso.getMaxPdv())
    {
      int pdvMax=perso.getMaxPdv();
      perso.setPdv(pdvMax);
    }
    if(protecteur)
    this.bLock_join = true;
    SocketManager.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this,1,2,0,1,0,45000,getType());
    SocketManager.GAME_SEND_GDF_PACKET_TO_FIGHT(perso,this.getMap().getCases());
    // on desactive le timer de regen cotï¿½ client

    scheduleTimer(45);

    this.start0=Main.world.getCryptManager().parseStartCell(getMap(),0);
    this.start1=Main.world.getCryptManager().parseStartCell(getMap(),1);
    SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlaces(),0);
    setSt1(0);
    setSt2(1);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTE+",0");
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTEUR+",0");

    List<Entry<Integer, Fighter>> e=new ArrayList<>();
    e.addAll(getTeam1().entrySet());
    for(Entry<Integer, Fighter> entry : e)
    {
      Fighter f=entry.getValue();
      GameCase cell=getRandomCell(getStart1());
      if(cell==null)
      {
        getTeam1().remove(f.getId());
        continue;
      }

      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,f.getId()+"",f.getId()+","+Constant.ETAT_PORTE+",0");
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,f.getId()+"",f.getId()+","+Constant.ETAT_PORTEUR+",0");
      f.setCell(cell);
      f.getCell().addFighter(f);
      f.setTeam(1);
      f.fullPdv();
    }
    getInit0().setCell(getRandomCell(getStart0()));

    getInit0().getPersonnage().getCurCell().removePlayer(getInit0().getPersonnage());

    getInit0().getCell().addFighter(getInit0());

    getInit0().getPersonnage().setFight(this);
    getInit0().setTeam(0);
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit0().getId());
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPersonnage().getCurMap(),group.getId());
    SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit0().getId(),getInit0());
    for(Fighter f : getTeam1().values())
      SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),group.getId(),f);
    SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,getMap());
    setState(Constant.FIGHT_STATE_PLACE);
  }

  public Fight(int id, GameMap map, Player perso, Collector perco)
  {
    if(perso.getFight()!=null)
      return;
    launchTime=System.currentTimeMillis();
    setGuildId(perco.getGuildId());
    perco.setInFight((byte)1);
    perco.set_inFightID((byte)id);

    demorph(perso);

    setType(Constant.FIGHT_TYPE_PVT); // (0: Dï¿½fie) (4: Pvm) (1:PVP) 5:Perco
    setId(id);
    setMap(map.getMapCopy());
    setMapOld(map);
    setInit0(new Fighter(this,perso));
    setCollector(perco);
    // on desactive le timer de regen cotï¿½ client

    getTeam0().put(perso.getId(),getInit0());

    Fighter percoF=new Fighter(this,perco);
    getTeam1().put(-1,percoF);

    SocketManager.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this,1,2,0,1,0,60000,getType()); // timer de combat
    SocketManager.GAME_SEND_GDF_PACKET_TO_FIGHT(perso,this.getMap().getCases());
    scheduleTimer(60);

    Random teams=new Random();
    if(teams.nextBoolean())
    {
      this.start0=Main.world.getCryptManager().parseStartCell(getMap(),0);
      this.start1=Main.world.getCryptManager().parseStartCell(getMap(),1);
      SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlaces(),0);
      setSt1(0);
      setSt2(1);
    }
    else
    {
      this.start0=Main.world.getCryptManager().parseStartCell(getMap(),1);
      this.start1=Main.world.getCryptManager().parseStartCell(getMap(),0);
      setSt1(1);
      setSt2(0);
      SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlaces(),1);
    }
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTE+",0");
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTEUR+",0");

    List<Entry<Integer, Fighter>> e=new ArrayList<>();
    e.addAll(getTeam1().entrySet());
    for(Entry<Integer, Fighter> entry : e)
    {
      Fighter f=entry.getValue();
      GameCase cell=getRandomCell(this.start1);
      if(cell==null)
      {
        getTeam1().remove(f.getId());
        continue;
      }

      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,f.getId()+"",f.getId()+","+Constant.ETAT_PORTE+",0");
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,f.getId()+"",f.getId()+","+Constant.ETAT_PORTEUR+",0");
      f.setCell(cell);
      f.getCell().addFighter(f);
      f.setTeam(1);
      f.fullPdv();

    }
    getInit0().setCell(getRandomCell(this.start0));

    getInit0().getPersonnage().getCurCell().removePlayer(getInit0().getPersonnage());

    getInit0().getCell().addFighter(getInit0());

    getInit0().getPersonnage().setFight(this);
    getInit0().setTeam(0);

    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit0().getId());
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPersonnage().getCurMap(),perco.getId());

    int c=PathFinding.getNearestCellAround(getInit0().getPersonnage().getCurMap(),getInit0().getPersonnage().getCurCell().getId(),perco.getCell(),new ArrayList<>());
    if(c<0)
      c=getInit0().getPersonnage().getCurCell().getId();

    SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),5,getInit0().getId(),perco.getId(),c,"0;-1",perco.getCell(),"3;-1");
    SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit0().getId(),getInit0());

    for(Fighter f : getTeam1().values())
      SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),perco.getId(),f);

    SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,getMap());
    setState(Constant.FIGHT_STATE_PLACE);

    String str="";
    if(getCollector()!=null)
      str="A"+getCollector().getN1()+","+getCollector().getN2()+"|.|"+Main.world.getMap(getCollector().getMap()).getX()+"|"+Main.world.getMap(getCollector().getMap()).getY();

    for(Player z : Main.world.getGuild(getGuildId()).getOnlineMembers())
    {
      if(!z.isOnline())
        z.setOnline(true);
      if(z.get_guild() == null)
    	  continue;
      SocketManager.GAME_SEND_gITM_PACKET(z,Collector.parseToGuild(z.get_guild().getId()));
      Collector.parseAttaque(z,getGuildId());
      Collector.parseDefense(z,getGuildId());
      SocketManager.SEND_gA_PERCEPTEUR(z,str);
    }
  }

  public Fight(int id, GameMap Map, Player perso, Prism Prisme)
  {
    launchTime=System.currentTimeMillis();
    Prisme.setInFight((byte)0);
    Prisme.setFight(this);
    Prisme.setFightId(id);
    demorph(perso);
    setType(Constant.FIGHT_TYPE_CONQUETE); // (0: Desafio) (4: Pvm) (1:PVP)
    // 5:Perco
    setId(id);
    setMap(Map.getMapCopy());
    setMapOld(Map);
    setInit0(new Fighter(this,perso));
    setPrism(Prisme);

    getTeam0().put(perso.getId(),getInit0());
    Fighter lPrisme=new Fighter(this,Prisme);
    setInit1(lPrisme);
    getTeam1().put(-1,lPrisme);
    SocketManager.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this,1,2,0,1,0,60000,getType());
    SocketManager.GAME_SEND_GDF_PACKET_TO_FIGHT(perso,this.getMap().getCases());
    scheduleTimer(60);

    Random teams=new Random();
    if(teams.nextBoolean())
    {
      this.start0=Main.world.getCryptManager().parseStartCell(getMap(),0);
      this.start1=Main.world.getCryptManager().parseStartCell(getMap(),1);
      SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlaces(),0);
      setSt1(0);
      setSt2(1);
    }
    else
    {
      this.start0=Main.world.getCryptManager().parseStartCell(getMap(),1);
      this.start1=Main.world.getCryptManager().parseStartCell(getMap(),0);
      setSt1(1);
      setSt2(0);
      SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlaces(),1);
    }
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTE+",0");
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTEUR+",0");

    List<Entry<Integer, Fighter>> e=new ArrayList<>();
    e.addAll(getTeam1().entrySet());
    for(Entry<Integer, Fighter> entry : e)
    {
      Fighter f=entry.getValue();
      GameCase cell=getRandomCell(getStart1());
      if(cell==null)
      {
        getTeam1().remove(f.getId());
        continue;
      }
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,f.getId()+"",f.getId()+","+Constant.ETAT_PORTE+",0");
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,f.getId()+"",f.getId()+","+Constant.ETAT_PORTEUR+",0");
      f.setCell(cell);
      f.getCell().addFighter(f);
      f.setTeam(1);
      f.fullPdv();
    }
    getInit0().setCell(getRandomCell(getStart0()));
    getInit0().getPersonnage().getCurCell().removePlayer(getInit0().getPersonnage());
    getInit0().getCell().addFighter(getInit0());
    getInit0().getPersonnage().setFight(this);
    getInit0().setTeam(0);
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit0().getId());
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPersonnage().getCurMap(),Prisme.getId());

    int c=PathFinding.getNearestCellAround(getInit0().getPersonnage().getCurMap(),getInit0().getPersonnage().getCurCell().getId(),Prisme.getCell(),new ArrayList<>());
    if(c<0)
      c=getInit0().getPersonnage().getCurCell().getId();

    SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),0,getInit0().getId(),Prisme.getId(),c,"0;"+getInit0().getPersonnage().get_align(),Prisme.getCell(),"0;"+Prisme.getAlignement());
    SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit0().getId(),getInit0());
    for(Fighter f : getTeam1().values())
      SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),Prisme.getId(),f);
    SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,getMap());
    setState(Constant.FIGHT_STATE_PLACE);
    String str="";
    if(getPrism()!=null)
      str=Prisme.getMap()+"|"+Prisme.getX()+"|"+Prisme.getY();
    for(Player z : Main.world.getOnlinePlayers())
    {
      if(z==null)
        continue;
      if(z.get_align()!=Prisme.getAlignement())
        continue;
      SocketManager.SEND_CA_ATTAQUE_MESSAGE_PRISME(z,str);
    }
  }

  public static void FightStateAddFlag(GameMap map, Player player)
  {
    map.getFights().stream().filter(fight -> fight.state==Constant.FIGHT_STATE_PLACE).forEach(fight -> {
      if(fight.type==Constant.FIGHT_TYPE_CHALLENGE)
      {
        SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(player,fight.init0.getPersonnage().getCurMap(),0,fight.init0.getId(),fight.init1.getId(),fight.init0.getPersonnage().getCurCell().getId(),"0;-1",fight.init1.getPersonnage().getCurCell().getId(),"0;-1");
        SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player,fight.init0.getPersonnage().getCurMap(),fight.init0.getId(),fight.init0);
        SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player,fight.init1.getPersonnage().getCurMap(),fight.init1.getId(),fight.init1);
      }
      else if(fight.type==Constant.FIGHT_TYPE_AGRESSION)
      {
        SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(player,fight.init0.getPersonnage().getCurMap(),0,fight.init0.getId(),fight.init1.getId(),fight.init0.getPersonnage().getCurCell().getId(),"0;"+fight.init0.getPersonnage().get_align(),fight.init1.getPersonnage().getCurCell().getId(),"0;"+fight.init1.getPersonnage().get_align());
        SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player,fight.init0.getPersonnage().getCurMap(),fight.init0.getId(),fight.init0);
        SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player,fight.init1.getPersonnage().getCurMap(),fight.init1.getId(),fight.init1);
      }
      else if(fight.type==Constant.FIGHT_TYPE_PVM)
      {
        SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(player,fight.init0.getPersonnage().getCurMap(),4,fight.init0.getId(),fight.mobGroup.getId(),(fight.init0.getPersonnage().getCurCell().getId()+1),"0;-1",fight.mobGroup.getCellId(),"1;-1");
        SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player,fight.init0.getPersonnage().getCurMap(),fight.init0.getId(),fight.init0);
        for(Entry<Integer, Fighter> F : fight.team1.entrySet())
          SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player,fight.map,fight.getMobGroup().getId(),F.getValue());
      }
      else if(fight.type==Constant.FIGHT_TYPE_PVT)
      {
        SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(player,fight.init0.getPersonnage().getCurMap(),5,fight.init0.getId(),fight.collector.getId(),(fight.init0.getPersonnage().getCurCell().getId()+1),"0;-1",fight.collector.getCell(),"3;-1");
        SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player,fight.init0.getPersonnage().getCurMap(),fight.init0.getId(),fight.init0);
        for(Entry<Integer, Fighter> F : fight.team1.entrySet())
          SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player,fight.map,fight.getCollector().getId(),F.getValue());
      }
      else if(fight.type==Constant.FIGHT_TYPE_CONQUETE)
      {
        SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(player,fight.init0.getPersonnage().getCurMap(),0,fight.init0.getId(),fight.prism.getId(),fight.init0.getPersonnage().getCurCell().getId(),"0;"+fight.init0.getPersonnage().get_align(),fight.prism.getCell(),"0;"+fight.prism.getAlignement());
        SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player,fight.init0.getPersonnage().getCurMap(),fight.init0.getId(),fight.init0);
        for(Entry<Integer, Fighter> F : fight.team1.entrySet())
          SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(player,fight.map,fight.getPrism().getId(),F.getValue());
      }
    });
  }

  public int getId()
  {
    return id;
  }

  void setId(int id)
  {
    this.id=id;
  }

  //v2.3 - Spectator join PvM
  void setStartGuid(int guid)
  {
    this.startGuid=guid;
  }

  //v2.3 - Spectator join PvM
  public int getStartGuid()
  {
    return this.startGuid;
  }

  public int getState()
  {
    return state;
  }

  void setState(int state)
  {
    this.state=state;
  }

  int getGuildId()
  {
    return guildId;
  }

  void setGuildId(int guildId)
  {
    this.guildId=guildId;
  }

  public int getType()
  {

    return type;
  }

  void setType(int type)
  {
    this.type=type;
  }

  int getSt1()
  {
    return st1;
  }

  void setSt1(int st1)
  {
    this.st1=st1;
  }

  int getSt2()
  {
    return st2;
  }

  void setSt2(int st2)
  {
    this.st2=st2;
  }

  public int getCurPlayer()
  {
    return curPlayer;
  }

  void setCurPlayer(int curPlayer)
  {
    this.curPlayer=curPlayer;
  }

  int getCaptWinner()
  {
    return captWinner;
  }

  void setCaptWinner(int captWinner)
  {
    this.captWinner=captWinner;
  }

  public int getCurFighterPa()
  {
    return curFighterPa;
  }

  public void setCurFighterPa(int curFighterPa)
  {
    this.curFighterPa=curFighterPa;
  }

  public int getCurFighterPm()
  {
    return curFighterPm;
  }

  public void setCurFighterPm(int curFighterPm)
  {
    this.curFighterPm=curFighterPm;
  }

  private int getCurFighterUsedPa()
  {
    return curFighterUsedPa;
  }

  private void setCurFighterUsedPa(int ap)
  {
    this.curFighterUsedPa=ap;
  }

  public int getCurFighterUsedPm()
  {
    return curFighterUsedPm;
  }

  private void setCurFighterUsedPm(int mp)
  {
    this.curFighterUsedPm=mp;
  }

  public Map<Integer, Fighter> getTeam(int team)
  {
    switch(team)
    {
      case 1:
        return team0;
      case 2:
        return team1;
    }
    return team0;
  }

  public Map<Integer, Fighter> getTeam0()
  {
    return team0;
  }

  public Map<Integer, Fighter> getTeam1()
  {
    return team1;
  }

  public ArrayList<Pair<Integer, Fighter>> getDeadList()
  {
    return deadList;
  }

  public boolean removeDead(Fighter target)
  {
    return deadList.remove(new Pair<Integer, Fighter>(target.getId(),target));
  }

  ConcurrentHashMap<Integer, Player> getViewer()
  {
    return viewer;
  }

  ArrayList<GameCase> getStart0()
  {
    return start0;
  }

  ArrayList<GameCase> getStart1()
  {
    return start1;
  }

  public Map<Integer, Challenge> getAllChallenges()
  {
    return allChallenges;
  }

  public Map<Integer, GameCase> getRholBack()
  {
    return rholBack;
  }

  public List<Glyph> getAllGlyphs()
  {
    return allGlyphs;
  }

  public List<Trap> getAllTraps()
  {
    return allTraps;
  }

  ArrayList<Fighter> getCapturer()
  {
    return capturer;
  }

  ArrayList<Fighter> getTrainer()
  {
    return trainer;
  }

  public long getStartTime()
  {
    return startTime;
  }

  void setStartTime(long startTime)
  {
    this.startTime=startTime;
  }

  public long getLaunchTime()
  {
    return launchTime;
  }

  boolean isLocked0()
  {
    return locked0;
  }

  void setLocked0(boolean locked0)
  {
    this.locked0=locked0;
  }

  boolean isLocked1()
  {
    return locked1;
  }

  void setLocked1(boolean locked1)
  {
    this.locked1=locked1;
  }

  boolean isOnlyGroup0()
  {
    return onlyGroup0;
  }

  void setOnlyGroup0(boolean onlyGroup0)
  {
    this.onlyGroup0=onlyGroup0;
  }

  boolean isOnlyGroup1()
  {
    return onlyGroup1;
  }

  void setOnlyGroup1(boolean onlyGroup1)
  {
    this.onlyGroup1=onlyGroup1;
  }

  boolean isHelp0()
  {
    return help0;
  }

  void setHelp0(boolean help0)
  {
    this.help0=help0;
  }

  boolean isHelp1()
  {
    return help1;
  }

  void setHelp1(boolean help1)
  {
    this.help1=help1;
  }

  boolean isViewerOk()
  {
    return viewerOk;
  }

  void setViewerOk(boolean viewerOk)
  {
    this.viewerOk=viewerOk;
  }

  boolean isHaveKnight()
  {
    return haveKnight;
  }

  void setHaveKnight()
  {
    this.haveKnight=true;
  }

  public boolean isBegin()
  {
    return isBegin;
  }

  void setBegin()
  {
    this.isBegin=true;
  }

  boolean isCheckTimer()
  {
    return checkTimer;
  }

  void setCheckTimer(boolean checkTimer)
  {
    this.checkTimer=checkTimer;
  }

  public String getCurAction()
  {
    return curAction;
  }

  public void setCurAction(String curAction)
  {
    this.curAction=curAction;
  }

  public MobGroup getMobGroup()
  {
    return mobGroup;
  }

  void setMobGroup(MobGroup mobGroup)
  {
    mobGroup.setFight(this);
    this.mobGroup=mobGroup;
  }

  Collector getCollector()
  {
    return collector;
  }

  void setCollector(Collector collector)
  {
    this.collector=collector;
  }

  public Prism getPrism()
  {
    return prism;
  }

  void setPrism(Prism prism)
  {
    this.prism=prism;
  }

  public GameMap getMap()
  {
    return map;
  }

  void setMap(GameMap map)
  {
    this.map=map;
  }

  public GameMap getMapOld()
  {
    return mapOld;
  }

  void setMapOld(GameMap mapOld)
  {
    this.mapOld=mapOld;
  }

  public Fighter getInit0()
  {
    return init0;
  }

  void setInit0(Fighter init0)
  {
    this.init0=init0;
  }

  public Fighter getInit1()
  {
    return init1;
  }

  void setInit1(Fighter init1)
  {
    this.init1=init1;
  }

  Capture getFullSoul()
  {
    return fullSoul;
  }

  void setFullSoul(Capture fullSoul)
  {
    this.fullSoul=fullSoul;
  }

  String getDefenders()
  {
    return defenders;
  }

  public void setDefenders(String defenders)
  {
    this.defenders=defenders;
  }

  int getTrainerWinner()
  {
    return trainerWinner;
  }

  void setTrainerWinner(int trainerWinner)
  {
    this.trainerWinner=trainerWinner;
  }

  public boolean isFinish()
  {
    return finish;
  }

  public int getTeamId(int guid)
  {
    if(getTeam0().containsKey(guid))
      return 1;
    if(getTeam1().containsKey(guid))
      return 2;
    if(getViewer().containsKey(guid))
      return 4;
    return -1;
  }

  public int getOtherTeamId(int guid)
  {
    if(getTeam0().containsKey(guid))
      return 2;
    if(getTeam1().containsKey(guid))
      return 1;
    return -1;
  }

  void scheduleTimer(int time)
  {
	  TimerWaiterPlus.addNext(() -> {
      if(!this.isBegin)
      {
        if(this.collector!=null&&!this.collectorProtect)
          this.collector.removeTimeTurn(1000);

        if(this.getState()!=Constant.FIGHT_STATE_ACTIVE)
          this.startFight();
        else if(this.collector!=null&&!this.collectorProtect)
          this.collector.setTimeTurn(60000);
      }
    },time*1000);
  }

  //v2.4 - nullpointerexception fix
  void demorph(Player p)
  {
    if(p!=null)
      if(!p.getMorphMode()&&p.isMorph()&&(p.getGroupe()==null)&&(p.getMorphId()!=8006&&p.getMorphId()!=8007&&p.getMorphId()!=8009))
        p.unsetMorph();
  }

  //v2.8 - oldMap now in joinFight
  public void startFight()
  {
    this.launchTime=-1;
    this.startTime=System.currentTimeMillis();
    if(this.collector!=null&&!this.collectorProtect)
    {
      ArrayList<Player> protectors=new ArrayList<>(collector.getDefenseFight().values());
      int time = 1000;
      for(Player player : protectors)
      {
        if(player.getFight()==null&&!player.isAway())
        {
        	time+= 100;
          player.setOldMap(player.getCurMap().getId());
          player.setOldCell(player.getCurCell().getId());
          if(player.getCurMap().getId()!=this.getMapOld().getId())
            player.teleport(this.getMapOld().getId(),this.collector.getCell());
          TimerWaiterPlus.addNext(() -> this.joinCollectorFight(player,collector.getId()),time);
        }
        else
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Vous ne pouviez pas rejoindre le combat des percepteurs parce que vous n'étiez pas disponible.");
          collector.delDefenseFight(player);
        }
      }

      this.collectorProtect=true;
      this.scheduleTimer(10);
      return;
    }

    if(getState()>=Constant.FIGHT_STATE_ACTIVE)
      return;

    if(getType()==Constant.FIGHT_TYPE_CONQUETE)
    {
      getPrism().setInFight(-2);
      for(Player z : Main.world.getOnlinePlayers())
      {
        if(z==null)
          continue;
        if(z.get_align()==getPrism().getAlignement())
        {
          Prism.parseAttack(z);
          Prism.parseDefense(z);
        }
      }
    }

    if(getType()==Constant.FIGHT_TYPE_PVT&&getCollector()!=null)
      getCollector().setInFight((byte)2);

    setState(Constant.FIGHT_STATE_ACTIVE);
    setStartTime(System.currentTimeMillis());
    SocketManager.GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),getInit0().getId());

    if(isHaveKnight()&&getType()==Constant.FIGHT_TYPE_AGRESSION)
      addChevalier();

    setCheckTimer(false);
    SocketManager.GAME_SEND_GIC_PACKETS_TO_FIGHT(this,7);
    SocketManager.GAME_SEND_GS_PACKET_TO_FIGHT(this,7);
    initOrderPlaying();
    setCurPlayer(-1);
    SocketManager.GAME_SEND_GTL_PACKET_TO_FIGHT(this,7);
    SocketManager.GAME_SEND_GTM_PACKET_TO_FIGHT(this,7);

    /** Challenges **/
    if(getType()==Constant.FIGHT_TYPE_PVM||getType()==Constant.FIGHT_TYPE_DOPEUL)
    {
      boolean hasMale=false,hasFemale=false,hasDisciple=false;
      boolean hasCawotte=false,hasChafer=false,hasRoulette=false,hasArakne=false,hasArround=false;
      boolean severalEnnemies,severalAllies,bothSexes,EvenEnnemies,MoreEnnemies,ecartLvlPlayer=false;
      int hasBoss=-1;

      if(this.getTeam0().size()>1)
      {
        int lowLvl1=201,lowLvl2=201;

        for(Fighter fighter : getTeam0().values())
          if(fighter.getLvl()<lowLvl1)
            lowLvl1=fighter.getLvl();
        for(Fighter fighter : getTeam0().values())
          if(fighter.getLvl()<lowLvl2&&fighter.getLvl()>lowLvl1)
            lowLvl2=fighter.getLvl();
        if(lowLvl2-lowLvl1>10)
          ecartLvlPlayer=true;
      }

      for(Fighter f : getTeam0().values())
      {
        Player player=f.getPersonnage();
        if(f.getPersonnage()!=null)
        {
          switch(player.getClasse())
          {
            case Constant.CLASS_OSAMODAS:
            case Constant.CLASS_FECA:
            case Constant.CLASS_SADIDA:
            case Constant.CLASS_XELOR:
            case Constant.CLASS_SRAM:
              hasDisciple=true;
              break;
          }

          player.setOldMap(player.getCurMap().getId());
          player.setOldCell(player.getCurCell().getId());

          if(player.hasSpell(367))
            hasCawotte=true;
          if(player.hasSpell(373))
            hasChafer=true;
          if(player.hasSpell(101))
            hasRoulette=true;
          if(player.hasSpell(370))
            hasArakne=true;
          if(player.getSexe()==0)
            hasMale=true;
          if(player.getSexe()==1)
            hasFemale=true;
        }
      }

      String boss=".58.85.86.107.113.121.147.173.180.225.226.230.232.251.252.257.289.295.374.375.377.382.404.423.430.457.478.568.605.612.669.670.673.675.677.681.780.792.797.799.800.827.854.926.939.940.943.1015.1027.1045.1051.1071.1072.1085.1086.1087.1159.1184.1185.1186.1187.1188.519.";
try {
      for(Fighter fighter : getTeam1().values())
      {
        if(fighter.getMob()!=null)
        {
          if(fighter.getMob().getTemplate()!=null)
          {
            if(boss.contains("."+fighter.getMob().getTemplate().getId()+"."))
              hasBoss=fighter.getMob().getTemplate().getId();
            
            if(Main.world.archi.contains("."+fighter.getMob().getTemplate().getId()+"."))
        		this.archi = true;
            
            for(Fighter fighter2 : getTeam0().values()) {
            	if(fighter2.getCell() == null)
            		continue;
              if(PathFinding.getDistanceBetween(this.getMap(),fighter2.getCell().getId(),fighter.getCell().getId())>=5)
                hasArround=true;
            }
          }
        }
      }
      if(hasBoss != -1)
    	  this.donjon = true;
           }catch (Exception e) {
	       e.printStackTrace();
           }

      for(Fighter fighter : getTeam1().values())
      {
        if(fighter.getMob()!=null)
        {
          if(fighter.getMob().getTemplate()!=null)
          {
            switch(fighter.getMob().getTemplate().getId())
            {
              case 98:// Tofu all
              case 111:
              case 120:
              case 382:
              case 473:
              case 794:
              case 796:
              case 800:
              case 801:
              case 803:
              case 805:
              case 806:
              case 807:
              case 808:
              case 841:
              case 847:
              case 868:
              case 970:
              case 171:// Dragodinde all
              case 200:
              case 666:
              case 582:// TouchParak
                hasArround=false;
                break;
            }
          }
        }
      }

      severalEnnemies=(getTeam1().size()>=2);
      severalAllies=(getTeam0().size()>=2);
      bothSexes=(!(!hasMale||!hasFemale));
      EvenEnnemies=(getTeam1().size()%2==0);
      MoreEnnemies=(getTeam1().size()>=getTeam0().size());

      String challenges=Main.world.getChallengeFromConditions(severalEnnemies,severalAllies,bothSexes,EvenEnnemies,MoreEnnemies,hasCawotte,hasChafer,hasRoulette,hasArakne,hasBoss,ecartLvlPlayer,hasArround,hasDisciple,(this.getTeam0().size()!=1));
      String[] chalInfo;

      int challengeID,challengeXP,challengeDP,bonusGroupe;
      int challengeNumber=((this.getMapOld().hasEndFightAction(this.getType())||Capture.isInArenaMap(this.getMapOld().getId())) ? 2 : 1);

      for(String chalInfos : Main.world.getRandomChallenge(challengeNumber,challenges))
      {
        chalInfo=chalInfos.split(",");
        challengeID=Integer.parseInt(chalInfo[0]);
        challengeXP=Integer.parseInt(chalInfo[1]);
        challengeDP=Integer.parseInt(chalInfo[2]);
        bonusGroupe=Integer.parseInt(chalInfo[3]);
        bonusGroupe*=getTeam1().size();
        getAllChallenges().put(challengeID,new Challenge(this,challengeID,challengeXP+bonusGroupe,challengeDP+bonusGroupe));
      }
      for(Entry<Integer, Challenge> c : getAllChallenges().entrySet())
      {
        if(c.getValue()==null)
          continue;
        c.getValue().fightStart();
        SocketManager.GAME_SEND_CHALLENGE_FIGHT(this,1,c.getValue().parseToPacket());
      }
    }
    /** Challenges **/
     int total = 0 ;
    for(Fighter F : getFighters(3))
    {
      Player player=F.getPersonnage();
      if(player!=null)
      {
    	  total++;
    	  
    	  if(player.getSpioned_by() != null) {
    	 if(player.getSpioned_by().getCurMap().getId() == player.getCurMap().getId()) {
    		 if(player.getSpioned_by().getGameClient() != null) {
    			 TimerWaiterPlus.addNext(() ->   player.getSpioned_by().getGameClient().gameJoinFight("GA903"+this.id),900);
    			
    		 }	
    	 }else
    	 {
    		 player.getSpioned_by().teleport(player.getCurMap().getId(), player.getCurCell().getId());
    		 if(player.getSpioned_by().getGameClient() != null) {
    			 TimerWaiterPlus.addNext(() ->   player.getSpioned_by().getGameClient().gameJoinFight("GA903"+this.id),900);
    		 }
    	 }
    	  }
          // if(player.isOnMount())
         // SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,player.getId()+"",player.getId()+","+Constant.ETAT_CHEVAUCHANT+",1");
        if(player.getClasse()==Constant.CLASS_PANDAWA)
        {
          F.setState(Constant.STATE_SOBER,-1,F.getId()); //infinite duration
         // SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,950,F.getId()+"",F.getId()+","+Constant.STATE_SOBER+",1");
        }
      }
    }
    
    for(Fighter F : getFighters(3))
    {
      Player player=F.getPersonnage();
      if(this.type == Constant.FIGHT_TYPE_PVM)
      {
      if(total == 1) {
    	 MONO = true; 
      }
      if(total == 2) {
    	  MONO = false; 
     	 DUO = true; 
       }
      if(Config.singleton.serverId != 6)
      if(player!=null)
      {
    	 if(player.getAccount().getSubscribeRemaining() != 0L)
       	SocketManager.GAME_SEND_MESSAGE(player,"Bonus VIP 25% activé.","008000");
    	if(DUO)
        SocketManager.GAME_SEND_MESSAGE(player,"Bonus DUO Compte 15% activé.","008000");
    	if(MONO)
        SocketManager.GAME_SEND_MESSAGE(player,"Bonus MONO Compte 15% activé.","008000");

      }
      }
      if(player!=null)
    	  player.itemchek();
    }

    this.startTurn();
    this.getFighters(3).stream().filter(F -> F!=null).forEach(F -> getRholBack().put(F.getId(),F.getCell()));
    this.setBegin();
  }

  public void leftFight(Player playerCaster, Player playerTarget)
  {
    if(playerCaster==null)
      return;
    
    final Fighter caster=this.getFighterByPerso(playerCaster),target=(playerTarget!=null ? this.getFighterByPerso(playerTarget) : null);
     
    if(caster!=null)
    {
        if(getType() == Constant.FIGHT_TYPE_KOLI)
        	return;
        if(Config.singleton.serverId == 6)
        if(getType() == Constant.FIGHT_TYPE_AGRESSION )
        	return;
        
        if(getType() == Constant.FIGHT_TYPE_AGRESSION  && this.state == Constant.FIGHT_STATE_PLACE)
        	return;
        
      switch(getType())
      {
      case Constant.FIGHT_TYPE_KOLI:
    	  return;
        case Constant.FIGHT_TYPE_CHALLENGE:
        case Constant.FIGHT_TYPE_AGRESSION:
        case Constant.FIGHT_TYPE_PVM:
        case Constant.FIGHT_TYPE_PVT:
        case Constant.FIGHT_TYPE_CONQUETE:
        case Constant.FIGHT_TYPE_DOPEUL:
          if(this.getState()>=Constant.FIGHT_STATE_ACTIVE)
          {
            if(!this.isBegin&&target==null)
              return;
            this.onFighterDie(caster,caster);
            caster.setLeft(true);

            if(this.getFighterByOrdreJeu()!=null&&this.getFighterByOrdreJeu().getId()==caster.getId())
              endTurn(false,caster);

            final Player player=caster.getPersonnage();
            player.setDuelId(-1);
            player.setReady(false);
            player.setFight(null);
            player.setAway(false);
            this.verifIfTeamAllDead();

            if(!this.finish)
            {
              this.onPlayerLoose(caster);
              SocketManager.GAME_SEND_GV_PACKET(caster.getPersonnage());
            }
          }
          else if(getState()==Constant.FIGHT_STATE_PLACE)
          {
            boolean isValid=false;
            if(target!=null)
            {
              if(getInit0()!=null&&getInit0().getPersonnage()!=null&&caster.getPersonnage().getId()==getInit0().getPersonnage().getId())
                isValid=true;
              if(getInit1()!=null&&getInit1().getPersonnage()!=null&&caster.getPersonnage().getId()==getInit1().getPersonnage().getId())
                isValid=true;
            }

            if(isValid)
            {// Celui qui fait l'action a lancer le combat et leave un autre personnage
              if((target.getTeam()==caster.getTeam())&&(target.getId()!=caster.getId()))
              {
                SocketManager.GAME_SEND_ON_FIGHTER_KICK(this,target.getPersonnage().getId(),getTeamId(target.getId()));

                if(getType()==Constant.FIGHT_TYPE_AGRESSION||getType()==Constant.FIGHT_TYPE_CHALLENGE||getType()==Constant.FIGHT_TYPE_PVT||getType()==Constant.FIGHT_TYPE_CONQUETE||getType()==Constant.FIGHT_TYPE_DOPEUL)
                  SocketManager.GAME_SEND_ON_FIGHTER_KICK(this,target.getPersonnage().getId(),getOtherTeamId(target.getId()));

                final Player player=target.getPersonnage();
                player.setDuelId(-1);
                player.setReady(false);
                player.setFight(null);
                player.setAway(false);

                if(player.isOnline())
                  SocketManager.GAME_SEND_GV_PACKET(player);

                // On le supprime de la team
                if(this.getTeam0().containsKey(target.getId()))
                {
                  target.getCell().removeFighter(target);
                  this.getTeam0().remove(target.getId());
                }
                else if(this.getTeam1().containsKey(target.getId()))
                {
                  target.getCell().removeFighter(target);
                  this.getTeam1().remove(target.getId());
                }

                for(Player player1 : this.getMapOld().getPlayers())
                  FightStateAddFlag(getMapOld(),player1);
              }
            }
            else if(target==null)
            {// Il leave de son plein grï¿½ donc (target = null)
              boolean isValid2=false;
              if(this.getInit0()!=null&&this.getInit0().getPersonnage()!=null&&caster.getPersonnage().getId()==this.getInit0().getPersonnage().getId())
                isValid2=true;
              if(this.getInit1()!=null&&this.getInit1().getPersonnage()!=null&&caster.getPersonnage().getId()==this.getInit1().getPersonnage().getId())
                isValid2=true;

              if(isValid2)
              {// Soit il a lancer le combat => annulation du combat
                for(Fighter fighter : this.getFighters(caster.getTeam2()))
                {
                  final Player player=fighter.getPersonnage();
                  player.setDuelId(-1);
                  player.setReady(false);
                  player.setFight(null);
                  player.setAway(false);
                  fighter.setLeft(true);

                  if(caster.getPersonnage().getId()!=fighter.getPersonnage().getId())
                  {// Celui qui a join le fight revient sur la map
                    if(player.isOnline())
                      SocketManager.GAME_SEND_GV_PACKET(player);
                  }
                  else
                  {// Celui qui a fait le fight meurt + perte honor
                    if(this.getType()==Constant.FIGHT_TYPE_AGRESSION||getType()==Constant.FIGHT_TYPE_PVM||getType()==Constant.FIGHT_TYPE_PVT||getType()==Constant.FIGHT_TYPE_CONQUETE)
                    {
                      int looseEnergy=Formulas.getLoosEnergy(player.getLevel(),getType()==1,getType()==5),totalEnergy=player.getEnergy()-looseEnergy;
                      if(totalEnergy<0)
                        totalEnergy=0;

                      player.setEnergy(totalEnergy);
                      player.setMascotte(0);

                      if(player.isOnline())
                        SocketManager.GAME_SEND_Im_PACKET(player,"034;"+looseEnergy);

                      if(caster.getPersonnage().getObjetByPos(Constant.ITEM_POS_FAMILIER)!=null)
                      {
                        GameObject obj=caster.getPersonnage().getObjetByPos(Constant.ITEM_POS_FAMILIER);
                        if(obj!=null)
                        {
                          PetEntry pets=Main.world.getPetsEntry(obj.getGuid());
                          if(pets!=null)
                            pets.looseFight(caster.getPersonnage());
                        }
                      }

                      if(this.getType()==Constant.FIGHT_TYPE_AGRESSION||getType()==Constant.FIGHT_TYPE_CONQUETE)
                      {
                        int honor=player.get_honor()-150;
                        if(honor<0)
                          honor=0;
                        player.set_honor(honor);
                        if(player.isOnline())
                          SocketManager.GAME_SEND_Im_PACKET(player,"076;"+honor);
                      }

                      final int energy=totalEnergy;

                      if(energy==0)
                      {
                        if(getType()==Constant.FIGHT_TYPE_AGRESSION)
                        {
                          for(Fighter enemy : (this.getTeam1().containsValue(caster) ? this.getTeam0() : this.getTeam1()).values())
                          {
                            if(enemy.getPersonnage()!=null)
                            {
                              if(enemy.getPersonnage().get_traque().getTraque()==caster.getPersonnage())
                              {
                            	  TimerWaiterPlus.addNext(() ->  player.teleportFaction(enemy.getPersonnage().get_align()),1500);
                                break;
                              }
                            }
                          }
                          player.setFuneral();
                        }
                        else
                        {
                          player.setFuneral();
                        }
                      }
                      else
                      {
                        if(getType()==Constant.FIGHT_TYPE_AGRESSION)
                        {
                          for(Fighter enemy : (this.getTeam1().containsValue(caster) ? this.getTeam0() : this.getTeam1()).values())
                          {
                            if(enemy.getPersonnage()!=null)
                            {
                              if(enemy.getPersonnage().get_traque().getTraque()==caster.getPersonnage())
                              {
                            	  TimerWaiterPlus.addNext(() ->  player.teleportFaction(enemy.getPersonnage().get_align()),1500);
                                break;
                              }
                            }
                          }
                        }
                        else
                        {
                          if(player.isOnline())
                          {
                            String[] split=player.getSavePosition().split(",");
                            TimerWaiterPlus.addNext(() ->  player.teleport(Short.parseShort(split[0]),Integer.parseInt(split[1])),1500);
                          }
                          else
                          {
                            player.setNeededEndFightAction(new Action(1001,player.getSavePosition(),"",null));
                          }
                        }
                        player.setPdv(1);
                      }
                    }

                    if(player.isOnline())
                      SocketManager.GAME_SEND_GV_PACKET(player);
                  }
                }

                if(getType()==Constant.FIGHT_TYPE_AGRESSION||getType()==Constant.FIGHT_TYPE_CHALLENGE||getType()==Constant.FIGHT_TYPE_PVT||getType()==Constant.FIGHT_TYPE_CONQUETE)
                {
                  for(Fighter f : this.getFighters(caster.getOtherTeam()))
                  {
                    if(f.getPersonnage()==null)
                      continue;
                    final Player player=f.getPersonnage();

                    player.setDuelId(-1);
                    player.setReady(false);
                    player.setFight(null);
                    player.setAway(false);

                    if(player.isOnline())
                      SocketManager.GAME_SEND_GV_PACKET(player);
                  }
                }

                this.setState(4);// Nous assure de ne pas dï¿½marrer le combat
                Main.world.getMap(this.getMap().getId()).removeFight(this.getId());
                SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(Main.world.getMap(this.getMap().getId()));
                SocketManager.GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(this.getMapOld(),this.getInit0().getId());

                if(getType()==Constant.FIGHT_TYPE_PVT)
                {
                  for(Player player : Main.world.getGuild(getGuildId()).getOnlineMembers())
                  {
                    if(!player.isOnline())
                      player.setOnline(true);
                    SocketManager.GAME_SEND_gITM_PACKET(player,Collector.parseToGuild(player.get_guild().getId()));
                    SocketManager.GAME_SEND_MESSAGE(player,"Votre percepteur a gagné le combat.");
                  }

                  this.getCollector().setInFight((byte)0);
                  this.getCollector().set_inFightID((byte)-1);

                  Main.world.getMap(this.getCollector().getMap()).getPlayers().stream().filter(player -> player!=null).forEach(player -> SocketManager.GAME_SEND_MAP_PERCO_GMS_PACKETS(player.getGameClient(),player.getCurMap()));
                }
                setMap(null);
                this.orderPlaying=null;
              }
              else
              {// Soit il a rejoin le combat => Left de lui seul
                SocketManager.GAME_SEND_ON_FIGHTER_KICK(this,caster.getPersonnage().getId(),getTeamId(caster.getId()));

                if(getType()==Constant.FIGHT_TYPE_AGRESSION||getType()==Constant.FIGHT_TYPE_CHALLENGE||getType()==Constant.FIGHT_TYPE_PVT||getType()==Constant.FIGHT_TYPE_CONQUETE)
                  SocketManager.GAME_SEND_ON_FIGHTER_KICK(this,caster.getPersonnage().getId(),getOtherTeamId(caster.getId()));

                final Player player=caster.getPersonnage();
                player.setDuelId(-1);
                player.setReady(false);
                player.setFight(null);
                player.setAway(false);
                caster.setLeft(true);
                caster.hasLeft();

                if(getType()==Constant.FIGHT_TYPE_AGRESSION||getType()==Constant.FIGHT_TYPE_PVM||getType()==Constant.FIGHT_TYPE_PVT||getType()==Constant.FIGHT_TYPE_CONQUETE||getType()==Constant.FIGHT_TYPE_DOPEUL)
                {
                  int loosEnergy=Formulas.getLoosEnergy(player.getLevel(),getType()==1,getType()==5),totalEnergy=player.getEnergy()-loosEnergy;
                  if(totalEnergy<0)
                    totalEnergy=0;

                  player.setEnergy(totalEnergy);
                  player.setMascotte(0);

                  if(player.isOnline())
                    SocketManager.GAME_SEND_Im_PACKET(player,"034;"+loosEnergy);
                  if(caster.getPersonnage().getObjetByPos(Constant.ITEM_POS_FAMILIER)!=null)
                  {
                    GameObject obj=caster.getPersonnage().getObjetByPos(Constant.ITEM_POS_FAMILIER);
                    if(obj!=null)
                    {
                      PetEntry pets=Main.world.getPetsEntry(obj.getGuid());
                      if(pets!=null)
                        pets.looseFight(caster.getPersonnage());
                    }
                  }

                  if(getType()==Constant.FIGHT_TYPE_AGRESSION||getType()==Constant.FIGHT_TYPE_CONQUETE)
                  {
                    int honor=player.get_honor()-150;
                    if(honor<0)
                      honor=0;
                    player.set_honor(honor);
                    if(player.isOnline())
                      SocketManager.GAME_SEND_Im_PACKET(player,"076;"+honor);
                  }

                  final int energy=totalEnergy;

                  if(energy==0)
                  {
                    if(getType()==Constant.FIGHT_TYPE_AGRESSION)
                    {
                      for(Fighter enemy : (this.getTeam1().containsValue(caster) ? this.getTeam0() : this.getTeam1()).values())
                      {
                        if(enemy.getPersonnage()!=null)
                        {
                          if(enemy.getPersonnage().get_traque().getTraque()==caster.getPersonnage())
                          {
                        	  TimerWaiterPlus.addNext(() ->  player.teleportFaction(enemy.getPersonnage().get_align()),1500);
                            break;
                          }
                        }
                      }
                      player.setFuneral();
                    }
                    else
                    {
                      player.setFuneral();
                    }
                  }
                  else
                  {
                    if(getType()==Constant.FIGHT_TYPE_AGRESSION)
                    {
                      for(Fighter enemy : (this.getTeam1().containsValue(caster) ? this.getTeam0() : this.getTeam1()).values())
                      {
                        if(enemy.getPersonnage()!=null)
                        {
                          if(enemy.getPersonnage().get_traque().getTraque()==caster.getPersonnage())
                          {
                        	  TimerWaiterPlus.addNext(() ->  player.teleportFaction(enemy.getPersonnage().get_align()),1500);
                            break;
                          }
                        }
                      }
                    }
                    else
                    {
                      if(getType()!=Constant.FIGHT_TYPE_PVT)
                        player.setNeededEndFightAction(new Action(1001,player.getSavePosition(),"",null));
                      else if(!player.getCurMap().hasEndFightAction(0))
                        player.setNeededEndFightAction(new Action(1001,player.getSavePosition(),"",null));
                    }
                    player.setPdv(1);
                  }
                }

                if(player.isOnline())
                  SocketManager.GAME_SEND_GV_PACKET(player);

                // On le supprime de la team
                if(this.getTeam0().containsKey(caster.getId()))
                {
                  caster.getCell().removeFighter(caster);
                  this.getTeam0().remove(caster.getId());
                }
                else if(getTeam1().containsKey(caster.getId()))
                {
                  caster.getCell().removeFighter(caster);
                  this.getTeam1().remove(caster.getId());
                }
                for(Player player1 : this.getMapOld().getPlayers())
                  FightStateAddFlag(getMapOld(),player1);
              }
            }
          }
          break;
      }
      if(target==null)
      {
        if(caster.getPersonnage().getMorphMode())
          if(caster.getPersonnage().donjon)
            caster.getPersonnage().unsetFullMorph();

        if(this.getTeam0().containsKey(caster.getId()))
        {
          caster.getCell().removeFighter(caster);
          this.getTeam0().remove(caster.getId());
        }
        else if(getTeam1().containsKey(caster.getId()))
        {
          caster.getCell().removeFighter(caster);
          this.getTeam1().remove(caster.getId());
        }
      }
      if(target!=null)
      {
        if(this.getTeam0().containsKey(target.getId()))
        {
          target.getCell().removeFighter(target);
          this.getTeam0().remove(target.getId());
        }
        else if(getTeam1().containsKey(target.getId()))
        {
          target.getCell().removeFighter(target);
          this.getTeam1().remove(target.getId());
        }
      }
    }
    else
    {
      SocketManager.GAME_SEND_GV_PACKET(playerCaster);
      this.getViewer().remove(playerCaster.getId());
      playerCaster.setFight(null);
      playerCaster.setAway(false);
    }
  }
public void Anti_bug () {
    for(Fighter caster : getTeam1().values())
    {
      try
      {
        if(caster==null)
          continue;
        if(caster.getPersonnage() != null && caster.getPersonnage().getFight() != null) {
        	 caster.getPersonnage().setReady(true);
             caster.getPersonnage().getFight().verifIfAllReady();
             SocketManager.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(caster.getPersonnage().getFight(),3,caster.getPersonnage().getId(),true);
             }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    for(Fighter caster : getTeam0().values())
    {
      try
      {
        if(caster==null)
          continue;
        if(caster.getPersonnage() != null && caster.getPersonnage().getFight() != null) {
            caster.getPersonnage().setReady(true);
            caster.getPersonnage().getFight().verifIfAllReady();
            SocketManager.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(caster.getPersonnage().getFight(),3,caster.getPersonnage().getId(),true);
            }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
 
}
  public void endFight(boolean b, boolean admin)
  {
    if(admin) {
    for(Fighter caster : getTeam1().values())
    {
      try
      {
        if(caster==null)
          continue;
        if(caster.getPersonnage() != null) {
        	 caster.getPersonnage().setReady(true);
             caster.getPersonnage().getFight().verifIfAllReady();
             SocketManager.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(caster.getPersonnage().getFight(),3,caster.getPersonnage().getId(),true);
             }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    for(Fighter caster : getTeam0().values())
    {
      try
      {
        if(caster==null)
          continue;
        if(caster.getPersonnage() != null) {
            caster.getPersonnage().setReady(true);
            caster.getPersonnage().getFight().verifIfAllReady();
            SocketManager.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(caster.getPersonnage().getFight(),3,caster.getPersonnage().getId(),true);
            }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    }
    if(this.launchTime>1)
        return;
    if(b)
    {
      for(Fighter caster : getTeam1().values())
      {
        try
        {
          if(caster==null)
            continue;
          caster.setIsDead(true);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
      verifIfTeamAllDead();
    }
    else
    {
      for(Fighter caster : getTeam0().values())
      {
        try
        {
          if(caster==null)
            continue;
          caster.setIsDead(true);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
      verifIfTeamAllDead();
    }
  }

  //v2.6 - invisible prediction AI
  public void startTurn()
  {
    if(verifyStillInFight())
      verifIfTeamAllDead();

    if(getState()>=Constant.FIGHT_STATE_FINISHED)
      return;

    setCurPlayer(getCurPlayer()+1);
    setCurAction("");

    if(getCurPlayer()>=this.getOrderPlayingSize())
      setCurPlayer(0);

    Fighter current=this.getFighterByOrdreJeu();
    if(current == null)
    	endTurn(false,this.getFighterByOrdreJeu());
    current.start_turn = System.currentTimeMillis();

    Fighter _luchadorDeTurno = this.getFighterByOrdreJeu();
    
    setCurFighterPa(current.getPa());
    setCurFighterPm(current.getPm());
    setCurFighterUsedPa(0);
    setCurFighterUsedPm(0);

    if(current.hasLeft()||current.isDead())
    {
      this.endTurn(false,current);
      return;
    }
    // ONE WINDOWS
    if(current.getPersonnage() != null) {
    if(current.getPersonnage().getParty() != null && current.getPersonnage().getParty().getMaster() != null)
    if(current.getPersonnage().getParty().getMaster().isOne_windows())
    {
    if(current.getPersonnage().getParty().getMaster().getId() != current.getPersonnage().getId())
    
    	{
    	current.getPersonnage().getParty().setOne_windows(current.getPersonnage()); 
    	SocketManager.GAME_SEND_ASK_WINDOW(current.getPersonnage().getParty().getMaster().getGameClient(), current.getPersonnage());
    	
    	  current.getPersonnage().getParty().getMaster().send("kI"+current.getPersonnage().getId());
    		SocketManager.GAME_SEND_STATS_PACKET_ONE_WINDOWS(current.getPersonnage(),current.getPersonnage().getParty().getMaster());
    		SocketManager.GAME_SEND_SPELL_LIST_ONE_WINDOWS(current.getPersonnage(),current.getPersonnage().getParty().getMaster());
 
    
    	}
    	else {
    		current.getPersonnage().getParty().setOne_windows(null); 
    		SocketManager.GAME_SEND_ASK_WINDOW(current.getPersonnage().getParty().getMaster().getGameClient(), current.getPersonnage());
        	
    		current.getPersonnage().send("kI"+current.getPersonnage().getId());
    		SocketManager.GAME_SEND_STATS_PACKET_ONE_WINDOWS(current.getPersonnage(),current.getPersonnage());
    		SocketManager.GAME_SEND_SPELL_LIST_ONE_WINDOWS(current.getPersonnage(),current.getPersonnage());
    		

    	}
    } 	
    }
    // ONE WINDOWS
 
    // ONE WINDOWS
    if(current.getPersonnage() != null) {
    if(current.getPersonnage().getParty() != null && current.getPersonnage().getParty().getMaster() != null)
      if(current.getPersonnage().getParty().getMaster().isOne_windows())
    {
    	if(current.getPersonnage().getParty().getMaster().getId() != current.getPersonnage().getId())
    	
    	{
    		for(int key : current.getPersonnage().getSorts().keySet())
  	      {

  	        Spell.SortStats SS=current.getPersonnage().getSorts().get(key);
  	        if(SS==null)
  	          continue;
  	        current.getPersonnage().getParty().getMaster().send("kM"+current.getId()+","+SS.getSpellID()+","+current.getCell().getId()+","+0);
  	       }
      for(LaunchedSpell S : current.getLaunchedSorts())
    	       
    		    {
    			  current.getPersonnage().getParty().getMaster().send("kM"+current.getId()+","+S.getSpellId()+","+current.getCell().getId()+","+S.getCooldown()); 
    			 
    		    }
      current.getPersonnage().refreshItemClasse(current.getPersonnage().getParty().getMaster());
                }
    	      else
    	       {
    		 for(int key : current.getPersonnage().getSorts().keySet())
    	      {
 
    	        Spell.SortStats SS=current.getPersonnage().getSorts().get(key);
    	        if(SS==null)
    	          continue;
    	        current.getPersonnage().send("kM"+current.getId()+","+SS.getSpellID()+","+current.getCell().getId()+","+0);
    	      }
            for(LaunchedSpell S : current.getLaunchedSorts())
		    {
            	current.getPersonnage().send("kM"+current.getId()+","+S.getSpellId()+","+current.getCell().getId()+","+S.getCooldown()); 

		    }
            current.getPersonnage().refreshItemClasse(null);
    	}
    } 	
    }
 // ONE WINDOWS

    current.applyBeginningTurnBuff(this);

    if(current.isDead()&&current.isInvocation())
    {
      endTurn(false,this.getFighterByOrdreJeu());
      return;
    }

    if(getState()==Constant.FIGHT_STATE_FINISHED)
      return;

    if(current.getPdv()<=0)
    {
      onFighterDie(current,getInit0());
      endTurn(false,current);
      return;
    }
    // On actualise les sorts launch
    current.refreshLaunchedSort();
    // reset des Max des Chatis
    current.getChatiValue().clear();

    if(current.isDead()&&!current.isInvocation())
    {
      endTurn(false,current);
      return;
    }
    if(current.getPersonnage()!=null)
      SocketManager.GAME_SEND_STATS_PACKET(current.getPersonnage());

    if(current.hasBuff(Constant.EFFECT_PASS_TURN))
    {
      endTurn(false,current);
      return;
    } 
    

    current.setTourplus();
    SocketManager.ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_TODOS(this, 7);// test refresh with mob
    SocketManager.GAME_SEND_GAMETURNSTART_PACKET_TO_FIGHT(this,7,current.getId(),Constant.TIME_BY_TURN,current.getTour());
    current.setCanPlay(true);
    this.turn=new Turn(this,current);

    // Gestion des glyphes
    ArrayList<Glyph> glyphs=new ArrayList<>(this.getAllGlyphs());// Copie du tableau

    for(Glyph glyph : glyphs)
    {
      if(glyph.getCaster().getId()==current.getId())
      {
        if(glyph.decrementDuration()==0)
        {
          getAllGlyphs().remove(glyph);
          glyph.disappear();
          continue;
        }
      }

      if(!verifOtherTeamIsDead(current))
      {
        if(PathFinding.getDistanceBetween(getMap(),current.getCell().getId(),glyph.getCell().getId())<=glyph.getSize()&&glyph.getSpell()!=476)
          glyph.onTrapped(current);
      }
    }

    if((getType()==Constant.FIGHT_TYPE_PVM)&&(getAllChallenges().size()>0)&&!current.isInvocation()&&!current.isDouble()&&!current.isCollector()||getType()==Constant.FIGHT_TYPE_DOPEUL&&(getAllChallenges().size()>0)&&!current.isInvocation()&&!current.isDouble()&&!current.isCollector())
      for(Challenge challenge : this.getAllChallenges().values())
        if(challenge!=null)
          challenge.onPlayerStartTurn(current);

    if(current.isDeconnected())
    {
      current.setTurnRemaining();
      if(current.getTurnRemaining()<=0)
      {
        if(current.getPersonnage()!=null)
        {
          leftFight(current.getPersonnage(),null);
          current.getPersonnage().disconnectInFight();
        }
        else
        {
          onFighterDie(current,current);
          current.setLeft(true);
        }
      }
      else
      {
        SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,7,"0162;"+current.getPacketsName()+"~"+current.getTurnRemaining());
        this.endTurn(false,current);
        return;
      }
    }
    
    if(current.lastInvisCell!=null)
      current.lastInvisCell=null;
    if(current.lastInvisMP!=-1)
      current.lastInvisMP=-1;
    if(this.getType() == Constant.FIGHT_TYPE_PVM)
    if(current.getPersonnage()!=null)
      if(current.getPersonnage().getAutoSkip()==true)
      {
    	  int size = 0;
    	  for(Fighter f : getTeam0().values()) {
    	  if(!f.isDead())
    		  if(!f.isInvocation())
    			  size++;
    	  }
    	  if(size > 1)
        this.endTurn(false,current);
        return;
      }
    // ONE WINDOWS
    if(current.getPersonnage() != null) {
    if(current.getPersonnage().getParty() != null && current.getPersonnage().getParty().getMaster() != null)
    	if(current.getPersonnage().getParty().getMaster().isOne_windows())
    {
    	if(current.getPersonnage().getParty().getMaster().getId() != current.getPersonnage().getId())
    
    	{
    		SocketManager.GAME_SEND_STATS_PACKET_ONE_WINDOWS(current.getPersonnage(),current.getPersonnage().getParty().getMaster());
    	}
    	else {
    		SocketManager.GAME_SEND_STATS_PACKET_ONE_WINDOWS(current.getPersonnage(),current.getPersonnage());
    	}
    } 	
    }

	// ONE WINDOWS
	// int inv = 0;
	if (!_luchadorDeTurno.isInvocation() && getType() == Constant.FIGHT_TYPE_PVM && !invocationAlreadySummoned) {
	    if (_luchadorDeTurno.getPersonnage() != null) { // Vérifier si un personnage est associé à _luchadorDeTurno
	        GameObject obj = _luchadorDeTurno.getPersonnage().getObjetByPos(Constant.ITEM_POS_CARTA_INVOCACION);

	        if (obj != null && _luchadorDeTurno.getPersonnage().hasEquiped(obj.getTemplate().getId())) {
	            int mobID = obj.getStats().getEffect(426);
	            List<Integer> celdas = new ArrayList<>();
	            int celdaAlrededor = PathFinding.getAvailableCellArround(this, _luchadorDeTurno.getCell().getId(), celdas);
	            
	            SpellEffect.cartaDeInvocación(this, _luchadorDeTurno, celdaAlrededor, mobID, 1);
	            // Marquer l'invocation comme déjà invoquée
	            invocationAlreadySummoned = true;
	        } else {
	            _luchadorDeTurno.getPersonnage().send("BN");
	        }
	    } else {
	        // Gérer le cas où _luchadorDeTurno.getPersonnage() est null
	        // Peut-être envoyer un message d'erreur ou une action appropriée
	    }
	}
    
    if(current.getPersonnage()==null||current.getDouble()!=null||current.getCollector()!=null)
      IAHandler.select(this,current);
  }

  public  void  endTurn(boolean onAction, Fighter f)
  {
    final Fighter current=this.getFighterByOrdreJeu();
    if(current == null) {
    	 startTurn();
    }
    if(current!=null)
      if(f==current)
        this.endTurn(onAction);
  }

  public  void endTurn(final boolean onAction)
  {
    if(!buffsToAdd.isEmpty())
    {
      for(Pair<Fighter, ArrayList<SpellEffect>> pair : buffsToAdd)
      {
        Fighter fighter=pair.getLeft();
        ArrayList<SpellEffect> buffs=pair.getRight();
        for(SpellEffect buff : buffs)
          fighter.addBuffTooltip(buff.getEffectID(),buff.getValue(),buff.getDuration(),buff.getTurn(),buff.isDebuffable(),buff.getSpell(),buff.getArgs(),fighter,true);
      }
      buffsToAdd.clear();
    }
    
    final Fighter current=this.getFighterByOrdreJeu();
    if(current==null) {
    	this.setCurAction("");
    	// startTurn();
      return;
    }
    current.start_turn = 0L;
    
    try
    {
      if(getState()>=Constant.FIGHT_STATE_FINISHED)
        return;
      if(this.turn!=null)
        this.turn.stop();
      if(current.hasLeft()||current.isDead())
      {
    	  this.setCurAction("");
        startTurn();
        return;
      }
      if(!this.getCurAction().equals(""))
      {
    	 this.setCurAction("");
    	 TimerWaiterPlus.addNext(() -> this.endTurn(onAction,current),100);
        return;
      }
     /* if(current.getPersonnage() != null){
			if (current.chamkar > (long)(System.currentTimeMillis()/1000)) {
				this.setCurAction("");
				 TimerWaiter.addNext(() -> this.endTurn(onAction,current),300);
				return;
			}
			}*/
      if(current.getState(Constant.ETAT_PORTEUR)==0)
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,950,current.getId()+"",current.getId()+","+Constant.ETAT_PORTE+",0");

      SocketManager.GAME_SEND_GAMETURNSTOP_PACKET_TO_FIGHT(this,7,current.getId());
      current.setCanPlay(false);
      setCurAction("");
      if(current.getPersonnage() != null) {
    	    if(current.getPersonnage().getParty() != null && current.getPersonnage().getParty().getMaster() != null) {
    	    	if(current.getPersonnage().getParty().getMaster().isOne_windows() && current.getPersonnage().getParty().getMaster().getId() != current.getPersonnage().getId()) {
    	    	 current.getPersonnage().getParty().setOne_windows(null);
    	    	}
 
    	    }
      }
      if(onAction) {
    	  this.setCurAction("");
    	  TimerWaiterPlus.addNext(() -> this.newTurn(current),300);
      }
      else
      {
    	  this.setCurAction("");
        this.newTurn(current);
      }
    }
    catch(NullPointerException e)
    {
    	this.setCurAction("");
      e.printStackTrace();
    // this.endTurn(false);
    }
  }

  //2.8 - AP poison damage fix
  private void doPoisons(Fighter current) //only for end-turn damage poisons
  {
    for(SpellEffect SE : current.getBuffsByEffectID(131))
    {
      int pas=SE.getValue();
      int val=-1;
      try
      {
        val=Integer.parseInt(SE.getArgs().split(";")[1]);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }

      if(val==-1)
        continue;

      int apDebuff=0;
      for(SpellEffect effect : current.getBuffsByEffectID(101)) //Ap removal deals damage
        apDebuff+=effect.getValue();

      int nbr=(int)Math.floor((double)(getCurFighterUsedPa()+apDebuff)/(double)pas);

      int dgt=val*nbr;

      if(SE.getSpell()==200) //Paralyzing poison
      {
        float inte=SE.getCaster().getTotalStats().getEffect(Constant.STATS_ADD_INTE);
        if(inte<0)
          inte=0;
        float pdom=SE.getCaster().getTotalStats().getEffect(Constant.STATS_ADD_PERDOM);
        if(pdom<0)
          pdom=0;
        dgt=(int)Math.floor(((inte+pdom)/100+1)*dgt);
      }

      if(dgt<=0)
        continue;
      if(dgt>current.getPdv())
        dgt=current.getPdv(); //va mourrir

      current.removePdv(current,dgt);
      current.removePdvMax((int)Math.floor(dgt*(Config.getInstance().erosion+SE.getCaster().getTotalStats().getEffect(Constant.STATS_ADD_ERO)-SE.getCaster().getTotalStats().getEffect(Constant.STATS_REM_ERO)-current.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+current.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
      dgt=-(dgt);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,100,SE.getCaster().getId()+"",current.getId()+","+dgt);
    }
  }

  private void newTurn(Fighter current)
  {
    doPoisons(current);
    ArrayList<Glyph> glyphs=new ArrayList<>();// Copie du tableau
    glyphs.addAll(getAllGlyphs());
    for(Glyph g : glyphs)
    {
      if(!verifOtherTeamIsDead(current))
      {
        // Si dans le glyphe
        int dist=PathFinding.getDistanceBetween(getMap(),current.getCell().getId(),g.getCell().getId());
        if(dist<=g.getSize()&&g.getSpell()==476)// 476 a effet en fin de tour, alors le joueur est dans le glyphe
          g.onTrapped(current);
      }
    }

    if(current.getPdv()<=0)
      onFighterDie(current,getInit0());

    if((getType()==Constant.FIGHT_TYPE_PVM)&&(getAllChallenges().size()>0)&&!current.isInvocation()&&!current.isDouble()&&!current.isCollector()&&current.getTeam()==0||getType()==Constant.FIGHT_TYPE_DOPEUL&&(getAllChallenges().size()>0)&&!current.isInvocation()&&!current.isDouble()&&!current.isCollector()&&current.getTeam()==0)
    {
      for(Entry<Integer, Challenge> c : getAllChallenges().entrySet())
      {
        if(c.getValue()==null)
          continue;
        c.getValue().onPlayerEndTurn(current);
      }
    }
    setCurFighterUsedPa(0);
    setCurFighterUsedPm(0);
    setCurFighterPa(current.getTotalStats().getEffect(Constant.STATS_ADD_PA));
    setCurFighterPm(current.getTotalStats().getEffect(Constant.STATS_ADD_PM));
    current.refreshEndTurnBuff();
    if(current.getPersonnage()!=null)
      if(current.getPersonnage().isOnline())
        SocketManager.GAME_SEND_STATS_PACKET(current.getPersonnage());

    SocketManager.GAME_SEND_GTM_PACKET_TO_FIGHT(this,7);
    SocketManager.GAME_SEND_GTR_PACKET_TO_FIGHT(this,7,current.getId());
    SocketManager.ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_TODOS(this, 7);
    // Timer d'une seconde ï¿½ la fin du tour
    this.startTurn();
  }

  public void playerPass(Player player)
  {
    final Fighter fighter=getFighterByPerso(player);
    if(fighter!=null)
      if(fighter.canPlay()&&this.getCurAction().isEmpty())
        this.endTurn(false,fighter);
  }

  //v2.7 - Tactical mode memory
  //v2.8 - Save old map and cell
  public void joinFight(Player perso, int guid)
  {
	  if(perso.getFight()!= null)
		  return;
    long timeRestant=Constant.TIME_START_FIGHT-(System.currentTimeMillis()-launchTime);
    Fighter currentJoin=null;
    if(this.bLock_join || this.getType() == Constant.FIGHT_TYPE_KOLI)
    {
      SocketManager.GAME_SEND_MESSAGE(perso,"You can not join this fight as it is forbidden.");
      return;
    }
    if( this.getType() == Constant.FIGHT_TYPE_PVM)
    if(perso.getGroupe() != null)
	        if(perso.getGroupe().getId() >= 1 && perso.getGroupe().getId() < 6) {
	        
	        	SocketManager.GAME_SEND_MESSAGE(perso,"You can not join this fight as it is forbidden.");
	            return;
	        	}
    if(Config.singleton.serverId == 6)
    	 if( this.getType() == Constant.FIGHT_TYPE_AGRESSION)
    	    {
    	      SocketManager.GAME_SEND_MESSAGE(perso,"You can not join this fight as it is forbidden.");
    	      return;
    	    }	
    perso.setOldMap(perso.getCurMap().getId());
    perso.setOldCell(perso.getCurCell().getId());
    if(perso.isDead()==1)
      return;
    if(isBegin())
      return;
    if(perso.getFight()!=null)
      return;
    if(perso.getGroupe() != null)
    if(perso.getGroupe().getId() > 1 && perso.getGroupe().getId() < 7) {
        STAFF = true;	
        }
    if(getTeam0().containsKey(guid))
    {
      GameCase cell=getRandomCell(getStart0());
      if(cell==null)
        return;
      if(getType()==Constant.FIGHT_TYPE_AGRESSION)
      {
        boolean multiIp=false;
        for(Fighter f : getTeam0().values())
          if(perso.getAccount().getCurrentIp().compareTo(f.getPersonnage().getAccount().getCurrentIp())==0)
            multiIp=true;
        if(multiIp)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne pouvez pas rejoindre ce combat, il y a Déjé un personnage dans ce combat avec la méme IP.");
          return;
        }
      }
      if(getType()==Constant.FIGHT_TYPE_PVT)
      {
    	  int multiIp= 0;
        for(Fighter f : getTeam0().values())
          if(perso.getAccount().getCurrentIp().compareTo(f.getPersonnage().getAccount().getCurrentIp())==0)
        	  multiIp++;
        if(multiIp >= 2)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne pouvez pas rejoindre ce combat, il y a Déjé un personnage dans ce combat avec la méme IP.");
          return;
        }
      }
      if(isOnlyGroup0())
      {
        Party g=getInit0().getPersonnage().getParty();
        if(g!=null)
        {
          if(!g.getPlayers().contains(perso))
          {
            SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'f',guid);
            return;
          }
        }
      }
      if(getType()==Constant.FIGHT_TYPE_AGRESSION)
      {
        if(perso.get_align()==Constant.ALIGNEMENT_NEUTRE)
        {
          SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'f',guid);
          return;
        }
        if(getInit0().getPersonnage().get_align()!=perso.get_align())
        {
          SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'f',guid);
          return;
        }
      }
      if(getType()==Constant.FIGHT_TYPE_CONQUETE)
      {
        if(perso.get_align()==Constant.ALIGNEMENT_NEUTRE)
        {
          SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'a',guid);
          return;
        }
        if(getInit0().getPrism().getAlignement()!=perso.get_align())
        {
          SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'a',guid);
          return;
        }
        perso.toggleWings('+');
      }
      if(getGuildId()>-1&&perso.get_guild()!=null)
      {
        if(getGuildId()==perso.get_guild().getId())
        {
          SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'f',guid);
          return;
        }
      }
      if(isLocked0())
      {
        SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'f',guid);
        return;
      }
      if(this.getTeam0().size()>=8||this.start0.size()==this.getTeam0().size())
        return;
      if(getType()==Constant.FIGHT_TYPE_CHALLENGE)
        SocketManager.GAME_SEND_GJK_PACKET(perso,2,1,1,0,timeRestant,getType());
      else
        SocketManager.GAME_SEND_GJK_PACKET(perso,2,0,1,0,timeRestant,getType());

      SocketManager.GAME_SEND_FIGHT_PLACES_PACKET(perso.getGameClient(),getMap().getPlaces(),getSt1());
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTE+",0");
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTEUR+",0");
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(),perso.getId());
      SocketManager.GAME_SEND_GDF_PACKET_TO_FIGHT(perso,this.getMap().getCases());
      Fighter f=new Fighter(this,perso);
      currentJoin=f;
      f.setTeam(0);
      getTeam0().put(perso.getId(),f);
      perso.setFight(this);
      f.setCell(cell);
      f.getCell().addFighter(f);
    }
    else if(getTeam1().containsKey(guid))
    {
      GameCase cell=getRandomCell(getStart1());
      if(cell==null)
        return;
      if(getType()==Constant.FIGHT_TYPE_AGRESSION)
      {
        boolean multiIp=false;
        for(Fighter f : getTeam1().values())
          if(perso.getAccount().getCurrentIp().compareTo(f.getPersonnage().getAccount().getCurrentIp())==0)
            multiIp=true;
        if(multiIp)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne pouvez pas rejoindre ce combat, il y a déjé un personnage dans ce combat avec la méme IP.");
          return;
        }
      }
      if(getType()==Constant.FIGHT_TYPE_PVT)
      {
    	  int multiIp= 0;
        for(Fighter f : getTeam1().values())
          if(perso.getAccount().getCurrentIp().compareTo(f.getPersonnage().getAccount().getCurrentIp())==0)
        	  multiIp++;
        if(multiIp >= 2)
        {
          SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne pouvez pas rejoindre ce combat, il y a Déjé un personnage dans ce combat avec la méme IP.");
          return;
        }
      }
      if(isOnlyGroup1())
      {
        Party g=getInit1().getPersonnage().getParty();
        if(g!=null)
        {
          if(!g.getPlayers().contains(perso))
          {
            SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'f',guid);
            return;
          }
        }
      }
      if(getType()==Constant.FIGHT_TYPE_AGRESSION)
      {
        if(perso.get_align()==Constant.ALIGNEMENT_NEUTRE)
        {
          SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'f',guid);
          return;
        }
        if(getInit1().getPersonnage().get_align()!=perso.get_align())
        {
          SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'f',guid);
          return;
        }
      }
      if(getType()==Constant.FIGHT_TYPE_CONQUETE)
      {
        if(perso.get_align()==Constant.ALIGNEMENT_NEUTRE)
        {
          SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'a',guid);
          return;
        }
        if(getInit1().getPrism().getAlignement()!=perso.get_align())
        {
          SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'a',guid);
          return;
        }
        perso.toggleWings('+');
      }
      if(getGuildId()>-1&&perso.get_guild()!=null)
      {
        if(getGuildId()==perso.get_guild().getId())
        {
          SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'f',guid);
          return;
        }
      }
      if(isLocked1())
      {
        SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getGameClient(),'f',guid);
        return;
      }
      if(this.getTeam1().size()>=8||this.start1.size()==this.getTeam0().size())
        return;
      if(getType()==Constant.FIGHT_TYPE_CHALLENGE)
        SocketManager.GAME_SEND_GJK_PACKET(perso,2,1,1,0,0,getType());
      else
        SocketManager.GAME_SEND_GJK_PACKET(perso,2,0,1,0,0,getType());

      SocketManager.GAME_SEND_FIGHT_PLACES_PACKET(perso.getGameClient(),getMap().getPlaces(),getSt2());
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTE+",0");
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTEUR+",0");
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(),perso.getId());

      Fighter f=new Fighter(this,perso);
      currentJoin=f;
      f.setTeam(1);
      getTeam1().put(perso.getId(),f);
      perso.setFight(this);
      f.setCell(cell);
      f.getCell().addFighter(f);
    }

    demorph(perso);

    if(currentJoin==null)
      return;
    perso.getCurCell().removePlayer(perso);

    SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(perso.getCurMap(),(currentJoin.getTeam()==0 ? getInit0() : getInit1()).getId(),currentJoin);
    SocketManager.GAME_SEND_FIGHT_PLAYER_JOIN(this,7,currentJoin);
    SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,getMap(),perso);
    if(getCollector()!=null)
    {
      for(Player z : Main.world.getGuild(getGuildId()).getOnlineMembers())
      {
        if(!z.isOnline())
          z.setOnline(true);
        Collector.parseAttaque(z,getGuildId());
        Collector.parseDefense(z,getGuildId());
      }
      if(getPrism()!=null)
        Main.world.getOnlinePlayers().stream().filter(z -> z!=null).filter(z -> z.get_align()==getPrism().getAlignement()).forEach(z -> Prism.parseAttack(perso));
    }
  }

  void joinCollectorFight(final Player player, final int collector)
  {
	  if(player.getFight()!= null)
		  return;
    final GameCase cell=getRandomCell(getStart1());

    if(cell==null)
      return;

    TimerWaiterPlus.addNext(() -> {
      SocketManager.GAME_SEND_GJK_PACKET(player,2,0,1,0,0,getType());
      SocketManager.GAME_SEND_FIGHT_PLACES_PACKET(player.getGameClient(),getMap().getPlaces(),getSt2());
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,player.getId()+"",player.getId()+","+Constant.ETAT_PORTE+",0");
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,player.getId()+"",player.getId()+","+Constant.ETAT_PORTEUR+",0");
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(),player.getId());

      Fighter f=new Fighter(this,player);
      f.setTeam(1);
      getTeam1().put(player.getId(),f);
      player.setFight(this);
      f.setCell(cell);
      f.getCell().addFighter(f);
      player.getCurCell().removePlayer(player);

      SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(player.getCurMap(),collector,f);
      SocketManager.GAME_SEND_FIGHT_PLAYER_JOIN(this,7,f);
      SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,getMap(),player);
      SocketManager.GAME_SEND_GDF_PACKET_TO_FIGHT(player,this.getMap().getCases());
    },700);
  }

  public void joinPrismFight(final Player player, final int team)
  {
	  if(player.getFight()!= null)
		  return;
    final GameCase cell=getRandomCell((team==1 ? this.start1 : this.start0));

    if(cell==null)
      return;

    int prismTeam=(this.getTeam0().containsKey(this.getPrism().getId()) ? 0 : 1);

    if(prismTeam==team)
    {
      if(player.get_align()!=this.getPrism().getAlignement())
        return;
    }
    else
    {
      if(player.get_align()==this.getPrism().getAlignement())
        return;
    }

    TimerWaiterPlus.addNext(() -> {
      SocketManager.GAME_SEND_GJK_PACKET(player,2,0,1,0,0,getType());
      SocketManager.GAME_SEND_FIGHT_PLACES_PACKET(player.getGameClient(),getMap().getPlaces(),getSt2());
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,player.getId()+"",player.getId()+","+Constant.ETAT_PORTE+",0");
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,player.getId()+"",player.getId()+","+Constant.ETAT_PORTEUR+",0");
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(),player.getId());

      Fighter f=new Fighter(this,player);
      f.setTeam(team);
      this.getTeam(team+1).put(player.getId(),f);
      player.setFight(this);
      f.setCell(cell);
      demorph(player);
      f.getCell().addFighter(f);
      player.getCurCell().removePlayer(player);

      SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(player.getCurMap(),((Fighter)this.getTeam(team+1).values().toArray()[0]).getId(),f);
      SocketManager.GAME_SEND_FIGHT_PLAYER_JOIN(this,7,f);
      SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,getMap(),player);
      SocketManager.GAME_SEND_GDF_PACKET_TO_FIGHT(player,this.getMap().getCases());
    },500);
  }

  public void joinAsSpect(Player p)
  {
	  if(p.getFight()!= null)
		  return;
    final Fighter current=this.getFighterByOrdreJeu();
    if(current==null)
      return;

    if(!isBegin()||p.getFight()!=null)
    {
      SocketManager.GAME_SEND_Im_PACKET(p,"157");
      return;
    }
    if(p.getGroupe()==null)
    {
      if(!isViewerOk()||getState()!=Constant.FIGHT_STATE_ACTIVE)
      {
        SocketManager.GAME_SEND_Im_PACKET(p,"157");
        return;
      }
    }
    demorph(p);
    p.getCurCell().removePlayer(p);
    SocketManager.GAME_SEND_GJK_PACKET(p,getState(),0,0,1,0,getType());
    SocketManager.GAME_SEND_GS_PACKET(p);
    SocketManager.GAME_SEND_GTL_PACKET(p,this);
    SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(p.getCurMap(),p.getId());
    SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,getMap(),p);
    SocketManager.GAME_SEND_GAMETURNSTART_PACKET(p,current.getId(),Constant.TIME_BY_TURN,current.getTour());

    getViewer().put(p.getId(),p);
    p.setSpec(true);
    p.setOldMap(p.getCurMap().getId());
    p.setOldCell(p.getCurCell().getId());
    p.setFight(this);

    ArrayList<Fighter> all=new ArrayList<>();
    all.addAll(this.getTeam0().values());
    all.addAll(this.getTeam1().values());
    all.stream().filter(Fighter::isHide).forEach(f -> SocketManager.GAME_SEND_GA_PACKET(this,p,150,f.getId()+"",f.getId()+",4"));
    if(p.getGroupe()==null)
      SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,7,"036;"+p.getName());
    if((getType()==Constant.FIGHT_TYPE_PVM)&&(getAllChallenges().size()>0)||getType()==Constant.FIGHT_TYPE_DOPEUL&&getAllChallenges().size()>0)
    {
      for(Entry<Integer, Challenge> c : getAllChallenges().entrySet())
      {
        if(c.getValue()==null)
          continue;
        SocketManager.GAME_SEND_CHALLENGE_PERSO(p,c.getValue().parseToPacket());
        if(!c.getValue().loose())
          c.getValue().challengeSpecLoose(p);
      }
    }
  }

  public void toggleLockTeam(int guid)
  {
    if(getInit0()!=null&&getInit0().getId()==guid)
    {
      setLocked0(!isLocked0());
      SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),isLocked0() ? '+' : '-','A',guid);
      SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,1,isLocked0() ? "095" : "096");
    }
    else if(getInit1()!=null&&getInit1().getId()==guid)
    {
      setLocked1(!isLocked1());
      SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit1().getPersonnage().getCurMap(),isLocked1() ? '+' : '-','A',guid);
      SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,2,isLocked1() ? "095" : "096");
    }
  }

  //v2.8 - new spectator lock system
  public  void toggleLockSpec(Player player)
  {
    //If the player is one of the initiator
    if(getInit0()!=null&&getInit0().getId()==player.getId()||getInit1()!=null&&getInit1().getId()==player.getId())
    {
      setViewerOk(!isViewerOk());
      SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),isViewerOk() ? '+' : '-','S',getInit0().getId());
      SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,7,isViewerOk() ? "039" : "040");
    }

    //Remove all guys actually spectating
    getViewer().values().forEach(spectator -> {
      if(spectator!=null&&spectator.getGroupe()==null)
      {

        SocketManager.GAME_SEND_GV_PACKET(spectator);
        getViewer().remove(spectator.getId());
        spectator.setFight(null);
        spectator.setSpec(false);
        spectator.setAway(false);
      }
    });
  }


  public void toggleOnlyGroup(int guid)
  {
    if(getInit0()!=null&&getInit0().getId()==guid)
    {
      setOnlyGroup0(!isOnlyGroup0());
      SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),isOnlyGroup0() ? '+' : '-','P',guid);
      SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,1,isOnlyGroup0() ? "093" : "094");
    }
    else if(getInit1()!=null&&getInit1().getId()==guid)
    {
      setOnlyGroup1(!isOnlyGroup1());
      SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit1().getPersonnage().getCurMap(),isOnlyGroup1() ? '+' : '-','P',guid);
      SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,2,isOnlyGroup1() ? "095" : "096");
    }
  }

  public void toggleHelp(int guid)
  {
    if(getInit0()!=null&&getInit0().getId()==guid)
    {
      setHelp0(!isHelp0());
      SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),isHelp0() ? '+' : '-','H',guid);
      SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,1,isHelp0() ? "0103" : "0104");
    }
    else if(getInit1()!=null&&getInit1().getId()==guid)
    {
      setHelp1(!isHelp1());
      SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit1().getPersonnage().getCurMap(),isHelp1() ? '+' : '-','H',guid);
      SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,2,isHelp1() ? "0103" : "0104");
    }
  }

  public void showCaseToTeam(int guid, int cellID)
  {
    int teams=getTeamId(guid)-1;
    if(teams==4)// Les spectateurs ne montrent pas.
      return;
    ArrayList<GameClient> PWs=new ArrayList<>();
    if(teams==0)
    {
      PWs.addAll(getTeam0().entrySet().stream().filter(e -> e.getValue().getPersonnage()!=null&&e.getValue().getPersonnage().getGameClient()!=null).map(e -> e.getValue().getPersonnage().getGameClient()).collect(Collectors.toList()));
    }
    else if(teams==1)
    {
      PWs.addAll(getTeam1().entrySet().stream().filter(e -> e.getValue().getPersonnage()!=null&&e.getValue().getPersonnage().getGameClient()!=null).map(e -> e.getValue().getPersonnage().getGameClient()).collect(Collectors.toList()));
    }
    SocketManager.GAME_SEND_FIGHT_SHOW_CASE(PWs,guid,cellID);
  }

  void showCaseToAll(int guid, int cellID)
  {
    ArrayList<GameClient> PWs=new ArrayList<>();
    for(Entry<Integer, Fighter> e : getTeam0().entrySet())
    {
      if(e.getValue().getPersonnage()!=null&&e.getValue().getPersonnage().getGameClient()!=null)
        PWs.add(e.getValue().getPersonnage().getGameClient());
    }
    for(Entry<Integer, Fighter> e : getTeam1().entrySet())
    {
      if(e.getValue().getPersonnage()!=null&&e.getValue().getPersonnage().getGameClient()!=null)
        PWs.add(e.getValue().getPersonnage().getGameClient());
    }
    for(Entry<Integer, Player> e : getViewer().entrySet())
    {
      PWs.add(e.getValue().getGameClient());
    }
    SocketManager.GAME_SEND_FIGHT_SHOW_CASE(PWs,guid,cellID);
  }

  private void initOrderPlaying()
  {
    int j=0;
    int k=0;
    int start0=0;
    int start1=0;
    int curMaxIni0=0;
    int curMaxIni1=0;
    Fighter curMax0=null;
    Fighter curMax1=null;
    boolean team1_ready=false;
    boolean team2_ready=false;

    do
    {
      if(!team1_ready)
      {
        team1_ready=true;
        Map<Integer, Fighter> team=getTeam0();
        for(Entry<Integer, Fighter> entry : team.entrySet())
        {
          if(this.haveFighterInOrdreJeu(entry.getValue()))
            continue;
          team1_ready=false;

          if(entry.getValue().getInitiative()>=curMaxIni0)
          {
            curMaxIni0=entry.getValue().getInitiative();
            curMax0=entry.getValue();
          }
          if(curMaxIni0>start0)
            start0=curMaxIni0;
        }
      }
      if(!team2_ready)
      {
        team2_ready=true;
        for(Entry<Integer, Fighter> entry : getTeam1().entrySet())
        {
          if(this.haveFighterInOrdreJeu(entry.getValue()))
            continue;
          team2_ready=false;
          if(entry.getValue().getInitiative()>=curMaxIni1)
          {
            curMaxIni1=entry.getValue().getInitiative();
            curMax1=entry.getValue();
          }
          if(curMaxIni1>start1)
            start1=curMaxIni1;
        }
      }
      if(curMax1==null&&curMax0==null)
      {
        return;
      }
      if(start0>start1)
      {
        if(getFighters(1).size()>j)
        {
          this.orderPlaying.add(curMax0);
          j++;
        }
        if(getFighters(2).size()>k)
        {
          this.orderPlaying.add(curMax1);
          k++;
        }
      }
      else
      {
        if(getFighters(2).size()>j)
        {
          this.orderPlaying.add(curMax1);
          j++;
        }
        if(getFighters(1).size()>k)
        {
          this.orderPlaying.add(curMax0);
          k++;
        }
      }

      curMaxIni0=0;
      curMaxIni1=0;
      curMax0=null;
      curMax1=null;
    } while(this.getOrderPlayingSize()!=getFighters(3).size());
  }

  //v2.8 - Healing weapon minHeal fix
  public void tryCaC(Player perso, int cellID)
  {
    final Fighter current=this.getFighterByOrdreJeu();
    if(current==null) {
    	SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,305,perso.getId()+"","");// Echec Critique Cac
    	setCurAction("");
      return;
    }
    Fighter caster=getFighterByPerso(perso);

    if(caster==null) {
    	SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,305,perso.getId()+"","");// Echec Critique Cac
    	setCurAction("");
      return;
    }
    if(current.getId()!=caster.getId())// Si ce n'est pas a lui de jouer
    {
    	SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,305,perso.getId()+"","");// Echec Critique Cac
    	setCurAction("");
      return;
    }
    if(!perso.canCac())
    {
    	 this.setCurAction("");
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,305,perso.getId()+"","");// Echec Critique Cac
      
      endTurn(false,current);
      return;
    }
    if((getType()==Constant.FIGHT_TYPE_PVM)&&(getAllChallenges().size()>0)||getType()==Constant.FIGHT_TYPE_DOPEUL&&getAllChallenges().size()>0)
    {
      for(Entry<Integer, Challenge> c : getAllChallenges().entrySet())
      {
        if(c.getValue()==null)
          continue;
        c.getValue().onPlayerCac(current);
      }
    }
    if(perso.getObjetByPos(Constant.ITEM_POS_ARME)==null)
    {
      tryCastSpell(caster,Main.world.getSort(0).getStatsByLevel(1),cellID);
    }
    else
    {
      GameObject arme=perso.getObjetByPos(Constant.ITEM_POS_ARME);
      // Pierre d'ï¿½mes = EC
      if(arme.getTemplate().getType()==83)
      {
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,305,perso.getId()+"","");// Echec Critique Cac
        this.setCurAction("");
        this.endTurn(false,current);
        return;
      }

      int PACost=arme.getTemplate().getPACost();

      if(getCurFighterPa()<PACost)
      {
        SocketManager.GAME_SEND_Im_PACKET(perso,"1170;"+getCurFighterPa()+"~"+PACost);
     // window
        if(current.getPersonnage() != null) {
      	    if(current.getPersonnage().getParty() != null && current.getPersonnage().getParty().getMaster() != null)
      	    if(current.getPersonnage().getParty().getMaster().isOne_windows())
      	    {
      	    if(current.getPersonnage().getParty().getMaster().getId() != current.getPersonnage().getId())
      	    
      	    	{
      	    	 SocketManager.GAME_SEND_Im_PACKET(current.getPersonnage().getParty().getMaster(),"1170;"+getCurFighterPa()+"~"+PACost);
      	    	}
      	    }
        }
        // window
        setCurAction("");
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,305,perso.getId()+"","");// Echec Critique Cac
        return;
      }

      int dist=PathFinding.getDistanceBetween(getMap(),caster.getCell().getId(),cellID);
      int MaxPO=arme.getTemplate().getPOmax();
      int MinPO=arme.getTemplate().getPOmin();

      if(dist<MinPO||dist>MaxPO)
      {
        SocketManager.GAME_SEND_Im_PACKET(perso,"1171;"+MinPO+"~"+MaxPO+"~"+dist);
     // window
        if(current.getPersonnage() != null) {
      	    if(current.getPersonnage().getParty() != null && current.getPersonnage().getParty().getMaster() != null)
      	    if(current.getPersonnage().getParty().getMaster().isOne_windows())
      	    {
      	    if(current.getPersonnage().getParty().getMaster().getId() != current.getPersonnage().getId())
      	    
      	    	{
      	    	SocketManager.GAME_SEND_Im_PACKET(current.getPersonnage().getParty().getMaster(),"1171;"+MinPO+"~"+MaxPO+"~"+dist);
      	    	}
      	    }
        }
        // window
        setCurAction("");
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,305,perso.getId()+"","");// Echec Critique Cac
        return;
      }
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,303,perso.getId()+"",cellID+"");
        boolean isCC=caster.testIfCC(arme.getTemplate().getTauxCC());
        if(isCC)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,301,perso.getId()+"","0");
        }

        // Si le joueur est invisible
        if(caster.isHide())
          caster.unHide(-1);

        ArrayList<SpellEffect> effets;

        if(isCC)
          effets=arme.getCritEffects();
        else
          effets=arme.getEffects();
        ArrayList<Fighter> cibles=PathFinding.getCiblesByZoneByWeapon(this,arme.getTemplate().getType(),getMap().getCase(cellID),caster.getCell().getId());

        for(SpellEffect SE : effets)
        {
          try
          {
            if(getState()!=Constant.FIGHT_STATE_ACTIVE)
              break;

            SE.setTurn(0);
            if((this.getType()!=Constant.FIGHT_TYPE_CHALLENGE)&&(this.getAllChallenges().size()>0))
            {
              for(Entry<Integer, Challenge> c : this.getAllChallenges().entrySet())
              {
                if(c.getValue()==null)
                  continue;
                c.getValue().onFightersAttacked(cibles,caster,SE,-1,false);
              }
            }
            SE.applyToFight(this,caster,cibles,true);
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        }

        if(arme.isHealWeapon())
        {
          Pair<Integer, Integer> heals=Constant.weaponHeal(arme);
          if(heals.getLeft()!=-1&&heals.getRight()!=-1) //no errors
          {
            int heal=Formulas.getRandomValue(heals.getLeft(),heals.getRight());
            if(isCC)
              heal+=arme.getTemplate().getBonusCC();
            heal=Formulas.calculFinalHeal(caster,heal,true);
            for(Fighter target : cibles)
            {
              if(target==null)
                continue;
              if((heal+target.getPdv())>target.getPdvMax())
                heal=target.getPdvMax()-target.getPdv(); //Target

              target.removePdv(target,-heal);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,100,target.getId()+"",target.getId()+",+"+heal);
            }
          }
        }
        setCurFighterPa(getCurFighterPa()-PACost);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,102,perso.getId()+"",perso.getId()+",-"+PACost);
        verifIfTeamAllDead();
      
    }
  }

  public  int tryCastSpell(Fighter fighter, SortStats spell, int cell) //0 = success, 5 = crit fail, 10 = error
  {
    final Fighter current=this.getFighterByOrdreJeu();

    if(current==null||spell==null||!this.getCurAction().isEmpty()||current!=fighter)
    {
    	if(current.getPersonnage() != null)
    	SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,302,fighter.getId()+"",spell.getSpellID()+""); // envoi de  l'EC
    	setCurAction("");
      return 10;
    }

    //v2.8 - Carrying lockout fix
    if(fighter.getHoldedBy()!=null&&(spell.getSpellID()==686||spell.getSpellID()==699||spell.getSpellID()==701))
    {
    	if(current.getPersonnage() != null)
        	SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,302,fighter.getId()+"",spell.getSpellID()+""); // envoi de  l'EC
        	
    	setCurAction("");
      return 10;
    }

    GameCase Cell=getMap().getCase(cell);
    Player player=fighter.getPersonnage();

    if(Cell.getFirstFighter()!=null)
      if(Cell.getFirstFighter().isHide())
        for(SpellEffect effect : spell.getEffects())
          if(effect.getEffectID()==181||effect.getEffectID()==185||effect.getEffectID()==400||effect.getEffectID()==401||effect.getEffectID()==4) //summons, static summons, traps, glyphs, teleportation
          {
            for(Fighter f : this.getFighters(3))
              if(f.getTeam()==fighter.getTeam())
                if(f.getPersonnage()!=null)
                  SocketManager.GAME_SEND_MESSAGE(f.getPersonnage(),player.getName()+" cannot cast "+spell.getSpell().getNombre()+" because of an invisible obstacle.");
            setCurAction("");
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,102,fighter.getId()+"",fighter.getId()+",-0");
            return 10;
          }

    setCurAction("casting");

    if(canCastSpell1(fighter,spell,Cell,-1))
    {
      if(fighter.getPersonnage()!=null)
        SocketManager.GAME_SEND_STATS_PACKET(fighter.getPersonnage()); // envoi des stats du lanceur
      if(fighter.getType()==1&&player.getItemClasseSpell().containsKey(spell.getSpellID()))
      {
        int modi=player.getItemClasseModif(spell.getSpellID(),285);
        setCurFighterPa(getCurFighterPa()-(spell.getPACost()-modi));
        setCurFighterUsedPa(getCurFighterUsedPa()+spell.getPACost()-modi);
      }
      else
      {
        setCurFighterPa(getCurFighterPa()-spell.getPACost());
        setCurFighterUsedPa(getCurFighterUsedPa()+spell.getPACost());
      }

      boolean isEc=Formulas.isCriticalFail(spell.getTauxEC(),fighter);
      if(isEc)
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,302,fighter.getId()+"",spell.getSpellID()+""); // envoi de  l'EC

      else
      {
        if((getType()==Constant.FIGHT_TYPE_PVM)&&(getAllChallenges().size()>0)&&!current.isInvocation()&&!current.isDouble()&&!current.isCollector())
        {
          for(Entry<Integer, Challenge> c : getAllChallenges().entrySet())
          {
            if(c.getValue()==null)
              continue;
            c.getValue().onPlayerAction(current,spell.getSpellID());
            if(spell.getSpell().getSpellID()==0)
              continue;
            c.getValue().onPlayerSpell(current,spell);
          }
        }

        boolean isCC=fighter.testIfCC(spell.getTauxCC(),spell,fighter);
        
        if(isCC) {
        	if(fighter.getPersonnage() == null)
                isCC = true;
        	if(fighter.isInvocation() && fighter.getInvocator() != null && fighter.getInvocator().getPersonnage() != null)
        		isCC = true;
        }
        
        
        String sort=spell.getSpellID()+","+cell+","+spell.getSpriteID()+","+spell.getLevel()+","+spell.getSpriteInfos();
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,300,fighter.getId()+"",sort); // xx lance le sort

        if(isCC)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,301,fighter.getId()+"",sort); // CC !
        if(fighter.isHide()) // Si le joueur est invi, on montre la case
        {
          if(spell.getSpellID()==0)// Si le coup est Coup de Poing alors on refait apparaitre le personnage
            fighter.unHide(cell);
          else
          {
            showCaseToAll(fighter.getId(),fighter.getCell().getId());
            fighter.lastInvisCell=fighter.getCell();
            fighter.lastInvisMP=fighter.getCurPm(this);
          }
        }
        spell.applySpellEffectToFight(this,fighter,Cell,isCC,false); // on applique les effets de l'arme
      }

      if(fighter.getType()==1&&player.getItemClasseSpell().containsKey(spell.getSpellID()))
      {
        int modi=player.getItemClasseModif(spell.getSpellID(),285);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,102,fighter.getId()+"",fighter.getId()+",-"+(spell.getPACost()-modi));
      }
      else
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,102,fighter.getId()+"",fighter.getId()+",-"+spell.getPACost());

      if(!isEc)
        fighter.addLaunchedSort(Cell.getFirstFighter(),spell,fighter);

      if((isEc&&spell.isEcEndTurn()))
      {
        try
        {
          Thread.sleep(300); //2.0 - Crit fail delay reduction
        }
        catch(InterruptedException e)
        {
         
        }
        setCurAction("");

        if(fighter.getMob()!=null||fighter.isInvocation())
        {
        	this.setCurAction("");
          return 5;
        }
        else
        {
        	 this.setCurAction("");
          endTurn(false,current);
          return 5;
        }
      }
    }
    else if(fighter.getMob()!=null||fighter.isInvocation())
    {
      try
      {
        Thread.sleep(100);
      }
      catch(InterruptedException e)
      {
        
      }
      setCurAction("");
      return 10;
    }

    this.verifIfTeamAllDead();
    if(fighter.getPersonnage()!=null)
    {
    	TimerWaiterPlus.addNext(() -> {
        setCurAction("");
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,102,fighter.getId()+"",fighter.getId()+",-0");
      },400);
    }
    else
    {
      try
      {
        Thread.sleep(1000);
      }
      catch(InterruptedException e)
      {
        
      }
      setCurAction("");
    }

    return 0;
  }

  public boolean canCastSpell1(Fighter caster, SortStats SS, GameCase cell, int targetCell)
  {
    final Fighter current=this.getFighterByOrdreJeu();
    if(current==null)
      return false;

    int casterCell;
    if(targetCell<=-1)
      casterCell=caster.getCell().getId();
    else
      casterCell=targetCell;

    Player perso=caster.getPersonnage();

    if(SS==null)
    {
      if(perso!=null)
      {
        /*
        SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this,7);
        SocketManager.GAME_SEND_Im_PACKET(perso,"1169");
        SocketManager.GAME_SEND_GAF_PACKET_TO_FIGHT(this,7,0,perso.getId());*/
      }
      return false;
    }

    if(current.getId()!=caster.getId())
    {
      if(perso!=null)
        SocketManager.GAME_SEND_Im_PACKET(perso,"1175");
      return false;
    }

    int usedPA;

    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
    {
      int modi=perso.getItemClasseModif(SS.getSpellID(),285);
      usedPA=SS.getPACost()-modi;
    }
    else
    {
      usedPA=SS.getPACost();
    }

    if(getCurFighterPa()<usedPA)
    {
      if(perso!=null)
        SocketManager.GAME_SEND_Im_PACKET(perso,"1170;"+getCurFighterPa()+"~"+SS.getPACost());
      return false;
    }

    if(cell==null)
    {
      if(perso!=null)
        SocketManager.GAME_SEND_Im_PACKET(perso,"1172");
      return false;
    }

    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
    {
      int modi=perso.getItemClasseModif(SS.getSpellID(),288);
      boolean modif=modi==1;
      if(SS.isLineLaunch()&&!modif&&!PathFinding.casesAreInSameLine(getMap(),casterCell,cell.getId(),'z',70))
      {
        SocketManager.GAME_SEND_Im_PACKET(perso,"1173");
        return false;
      }
    }
    else if(SS.isLineLaunch()&&!PathFinding.casesAreInSameLine(getMap(),casterCell,cell.getId(),'z',70))
    {
      if(perso!=null)
        SocketManager.GAME_SEND_Im_PACKET(perso,"1173");
      return false;
    }

    char dir=PathFinding.getDirBetweenTwoCase(casterCell,cell.getId(),getMap(),true);

    if(SS.getSpellID()==67)
    {
      if(!PathFinding.checkLoS(getMap(),PathFinding.GetCaseIDFromDirrection(casterCell,dir,getMap(),true),cell.getId(),null,true))
      {
        if(perso!=null)
          SocketManager.GAME_SEND_Im_PACKET(perso,"1174");
        return false;
      }
    }
    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
    {
      int modi=perso.getItemClasseModif(SS.getSpellID(),289);
      boolean modif=modi==1;
      if(SS.hasLDV()&&!PathFinding.checkLoS(getMap(),casterCell,cell.getId(),caster)&&!modif)
      {
        SocketManager.GAME_SEND_Im_PACKET(perso,"1174");
        return false;
      }
    }
    else if(SS.getSpell().getSpellID() != 1105 && SS.hasLDV()&&!PathFinding.checkLoS(getMap(),casterCell,cell.getId(),caster))
    {
      if(perso!=null)
        SocketManager.GAME_SEND_Im_PACKET(perso,"1174");
      return false;
    }

    int dist=PathFinding.getDistanceBetween(getMap(),casterCell,cell.getId());
    int maxAlc=SS.getMaxPO();
    int minAlc=SS.getMinPO();
    // + alcance
    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
    {
      int modi=perso.getItemClasseModif(SS.getSpellID(),281);
      maxAlc=maxAlc+modi;
    } // alcance modificable

    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
    {
      int modi=perso.getItemClasseModif(SS.getSpellID(),282);
      boolean modif=modi==1;
      if(SS.isModifPO()||modif)
      {
        maxAlc+=caster.getTotalStats().getEffect(117);
        if(maxAlc<=minAlc)
          maxAlc=minAlc+1;
      }
    }
    else if(SS.isModifPO())
    {
      maxAlc+=caster.getTotalStats().getEffect(117);
      if(maxAlc<=minAlc)
        maxAlc=minAlc+1;
    }

    if(maxAlc<minAlc)
      maxAlc=minAlc;
    
    if(current.getMob() != null && current.getMob().getTemplate().getId() == 423)
    {
    	
    }
    else
    if(dist<minAlc||dist>maxAlc)
    {
      if(perso!=null)
        SocketManager.GAME_SEND_Im_PACKET(perso,"1171;"+minAlc+"~"+maxAlc+"~"+dist);
      return false;
    }

    if(!LaunchedSpell.cooldownGood(caster,SS.getSpellID()))
    {
      return false;
    }

    int numLunch=SS.getMaxLaunchbyTurn();

    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
      numLunch+=perso.getItemClasseModif(SS.getSpellID(),290);

    if(numLunch-LaunchedSpell.getNbLaunch(caster,SS.getSpellID())<=0&&numLunch>0)
    {
      return false;
    }

    Fighter t=cell.getFirstFighter();
    int numLunchT=SS.getMaxLaunchByTarget();

    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
      numLunchT+=perso.getItemClasseModif(SS.getSpellID(),291);

    return !(numLunchT-LaunchedSpell.getNbLaunchTarget(caster,t,SS.getSpellID())<=0&&numLunchT>0);
  }

  public boolean canCastSpell2(Fighter caster, SortStats SS, GameCase casterCell, GameCase targetCell)
  {
    final Fighter current=this.getFighterByOrdreJeu();
    if(current==null)
      return false;

    Player perso=caster.getPersonnage();

    if(SS==null)
      return false;

    if(current.getId()!=caster.getId())
    {
      if(perso!=null)
        SocketManager.GAME_SEND_Im_PACKET(perso,"1175");
      return false;
    }

    int usedPA;

    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
    {
      int modi=perso.getItemClasseModif(SS.getSpellID(),285);
      usedPA=SS.getPACost()-modi;
    }
    else
    {
      usedPA=SS.getPACost();
    }

    if(getCurFighterPa()<usedPA)
    {
      if(perso!=null)
        SocketManager.GAME_SEND_Im_PACKET(perso,"1170;"+getCurFighterPa()+"~"+SS.getPACost());
      return false;
    }

    if(targetCell==null)
    {
      if(perso!=null)
        SocketManager.GAME_SEND_Im_PACKET(perso,"1172");
      return false;
    }

    if(perso!=null)
    {
      if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
      {
        int modi=perso.getItemClasseModif(SS.getSpellID(),288);
        boolean modif=modi==1;
        if(SS.isLineLaunch()&&!modif&&!PathFinding.casesAreInSameLine(getMap(),casterCell.getId(),targetCell.getId(),'z',70))
        {
          SocketManager.GAME_SEND_Im_PACKET(perso,"1173");
          return false;
        }
      }
    }
    else if(SS.isLineLaunch()&&!PathFinding.casesAreInSameLine(getMap(),casterCell.getId(),targetCell.getId(),'z',70))
      return false;

    char dir=PathFinding.getDirBetweenTwoCase(casterCell.getId(),targetCell.getId(),getMap(),true);

    if(SS.getSpellID()==67)
    {
      if(!PathFinding.checkLoS(getMap(),PathFinding.GetCaseIDFromDirrection(casterCell.getId(),dir,getMap(),true),targetCell.getId(),null,true))
      {
        if(perso!=null)
          SocketManager.GAME_SEND_Im_PACKET(perso,"1174");
        return false;
      }
    }
    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
    {
      int modi=perso.getItemClasseModif(SS.getSpellID(),289);
      boolean modif=modi==1;
      if(SS.hasLDV()&&!PathFinding.checkLoS(getMap(),casterCell.getId(),targetCell.getId(),caster)&&!modif)
      {
        SocketManager.GAME_SEND_Im_PACKET(perso,"1174");
        return false;
      }
    }
    else if(SS.hasLDV()&&!PathFinding.checkLoS(getMap(),casterCell.getId(),targetCell.getId(),caster))
    {
      if(perso!=null)
        SocketManager.GAME_SEND_Im_PACKET(perso,"1174");
      return false;
    }

    int dist=PathFinding.getDistanceBetween(getMap(),casterCell.getId(),targetCell.getId());
    int maxAlc=SS.getMaxPO();
    int minAlc=SS.getMinPO();
    // + alcance
    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
    {
      int modi=perso.getItemClasseModif(SS.getSpellID(),281);
      maxAlc=maxAlc+modi;
    } // alcance modificable

    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
    {
      int modi=perso.getItemClasseModif(SS.getSpellID(),282);
      boolean modif=modi==1;
      if(SS.isModifPO()||modif)
      {
        maxAlc+=caster.getTotalStats().getEffect(117);
        if(maxAlc<=minAlc)
          maxAlc=minAlc+1;
      }
    }
    else if(SS.isModifPO())
    {
      maxAlc+=caster.getTotalStats().getEffect(117);
      if(maxAlc<=minAlc)
        maxAlc=minAlc+1;
    }

    if(maxAlc<minAlc)
      maxAlc=minAlc;
    if(dist<minAlc||dist>maxAlc)
    {
      if(perso!=null)
        SocketManager.GAME_SEND_Im_PACKET(perso,"1171;"+minAlc+"~"+maxAlc+"~"+dist);
      return false;
    }

    if(!LaunchedSpell.cooldownGood(caster,SS.getSpellID()))
    {
      return false;
    }

    int numLunch=SS.getMaxLaunchbyTurn();

    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
      numLunch+=perso.getItemClasseModif(SS.getSpellID(),290);

    if(numLunch-LaunchedSpell.getNbLaunch(caster,SS.getSpellID())<=0&&numLunch>0)
    {
      return false;
    }

    Fighter t=targetCell.getFirstFighter();
    int numLunchT=SS.getMaxLaunchByTarget();

    if(caster.getType()==1&&perso.getItemClasseSpell().containsKey(SS.getSpellID()))
      numLunchT+=perso.getItemClasseModif(SS.getSpellID(),291);

    return !(numLunchT-LaunchedSpell.getNbLaunchTarget(caster,t,SS.getSpellID())<=0&&numLunchT>0);
  }

  public boolean onFighterDeplace(Fighter fighter, GameAction GA)
  {
    final Fighter current=this.getFighterByOrdreJeu();
    if(current==null) {
    	if(fighter.getPersonnage()!=null) {
            SocketManager.GAME_SEND_GA_PACKET(fighter.getPersonnage().getGameClient(),"","0","","");
    	}
    	this.setCurAction("");
      return false;
    }
    String path=GA.args;
    if(path.equals(""))
    {
    	if(fighter.getPersonnage()!=null) {
    		// ONE WINDOWS
    	    if(current.getPersonnage() != null) {
    	    if(current.getPersonnage().getParty() != null && current.getPersonnage().getParty().getMaster() != null)
    	    if(current.getPersonnage().getParty().getMaster().isOne_windows())
    	    {
    	    if(current.getPersonnage().getParty().getMaster().getId() != current.getPersonnage().getId())
    	    	SocketManager.GAME_SEND_GA_PACKET(fighter.getPersonnage().getGameClient(),"","0","","");
    	    	}
    	    } 	
    	    // ONE WINDOWS
            SocketManager.GAME_SEND_GA_PACKET(fighter.getPersonnage().getGameClient(),"","0","","");
    	}
    this.setCurAction("");
      return false;
    }
    if(this.getOrderPlayingSize()<=getCurPlayer()) {
    	if(fighter.getPersonnage()!=null) {
    		// ONE WINDOWS
    	    if(current.getPersonnage() != null) {
    	    if(current.getPersonnage().getParty() != null && current.getPersonnage().getParty().getMaster() != null)
    	    if(current.getPersonnage().getParty().getMaster().isOne_windows())
    	    {
    	    if(current.getPersonnage().getParty().getMaster().getId() != current.getPersonnage().getId())
    	    	SocketManager.GAME_SEND_GA_PACKET(fighter.getPersonnage().getGameClient(),"","0","","");
    	    	}
    	    } 	
    	    // ONE WINDOWS
            SocketManager.GAME_SEND_GA_PACKET(fighter.getPersonnage().getGameClient(),"","0","","");
    	}
    	this.setCurAction("");
      return false;
    }
    if(current.getId()!=fighter.getId()||this.getState()!=Constant.FIGHT_STATE_ACTIVE) {
    	if(fighter.getPersonnage()!=null) {
    		// ONE WINDOWS
    	    if(current.getPersonnage() != null) {
    	    if(current.getPersonnage().getParty() != null && current.getPersonnage().getParty().getMaster() != null)
    	    if(current.getPersonnage().getParty().getMaster().isOne_windows())
    	    {
    	    if(current.getPersonnage().getParty().getMaster().getId() != current.getPersonnage().getId())
    	    	SocketManager.GAME_SEND_GA_PACKET(fighter.getPersonnage().getGameClient(),"","0","","");
    	    	}
    	    } 	
    	    // ONE WINDOWS
            SocketManager.GAME_SEND_GA_PACKET(fighter.getPersonnage().getGameClient(),"","0","","");
    	}
    	this.setCurAction("");
      return false;
    }
    Fighter targetTacle=PathFinding.getEnemyAround(fighter.getCell().getId(),getMap(),this);
    this.setCurAction("deplace");
    if(targetTacle!=null&&!fighter.haveState(Constant.ETAT_ENRACINE)&&!fighter.haveState(Constant.ETAT_PORTE)&&!fighter.isHide())
    {
      int esquive=Formulas.getTacleChance(fighter,targetTacle);
      int rand=Formulas.getRandomValue(0,99);
      if(targetTacle.haveState(Constant.STATE_UNDODGEABLE))
        rand=100;
      if(rand>esquive)
      {
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,GA.id,"104",fighter.getId()+";","");
        int pierdePA=getCurFighterPa()*esquive/100;
        if(pierdePA<0)
          pierdePA=-pierdePA;
        if(getCurFighterPm()<0)
          setCurFighterPm(0);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,GA.id,"129",fighter.getId()+"",fighter.getId()+",-"+getCurFighterPm());
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,GA.id,"102",fighter.getId()+"",fighter.getId()+",-"+pierdePA);
        setCurFighterPm(0);
        setCurFighterPa(getCurFighterPa()-pierdePA);
        this.setCurAction("");
        return false;
      }
    }
    AtomicReference<String> pathRef=new AtomicReference<>(path);
    int nStep=PathFinding.isValidPath(getMap(),fighter.getCell().getId(),pathRef,this,null,-1);
    String newPath=pathRef.get();
    if(nStep>getCurFighterPm()||nStep==-1000)
    {
      if(fighter.getPersonnage()!=null)
        SocketManager.GAME_SEND_GA_PACKET(fighter.getPersonnage().getGameClient(),"","0","","");
      this.setCurAction("");
      return false;
    }
    current.setJustTrapped(false);
    setCurFighterPm(getCurFighterPm()-nStep);
    this.setCurFighterUsedPm(this.getCurFighterUsedPm()+nStep);

    int nextCellID=Main.world.getCryptManager().cellCode_To_ID(newPath.substring(newPath.length()-2));
    // les monstres n'ont pas de GAS//GAF
    if(current.getPersonnage()!=null)
      SocketManager.GAME_SEND_GAS_PACKET_TO_FIGHT(this,7,current.getId());

    // Si le joueur n'est pas invisible
    if(!current.isHide())
    {
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,GA.id,"1",current.getId()+"","a"+Main.world.getCryptManager().cellID_To_Code(fighter.getCell().getId())+newPath);
    }
    else
    {
      if(current.getPersonnage()!=null)
      {
        // On envoie le path qu'au joueur qui se dï¿½place
        GameClient out=current.getPersonnage().getGameClient();
        SocketManager.GAME_SEND_GA_PACKET(out,GA.id+"","1",current.getId()+"","a"+Main.world.getCryptManager().cellID_To_Code(fighter.getCell().getId())+newPath);
        if(fighter.getPersonnage()!=null) {
      		// ONE WINDOWS
      	    if(current.getPersonnage() != null) {
      	    if(current.getPersonnage().getParty() != null && current.getPersonnage().getParty().getMaster() != null)
      	    if(current.getPersonnage().getParty().getMaster().isOne_windows())
      	    {
      	    if(current.getPersonnage().getParty().getMaster().getId() != current.getPersonnage().getId())
            SocketManager.GAME_SEND_GA_PACKET(current.getPersonnage().getParty().getMaster().getGameClient(),GA.id+"","1",current.getId()+"","a"+Main.world.getCryptManager().cellID_To_Code(fighter.getCell().getId())+newPath);
            
      	    	}
      	    } 	
      	}
      }
    }
    if(current.getPersonnage()==null)
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,GA.id,"129",fighter.getId()+"",fighter.getId()+",-"+nStep);
        
    // Si portï¿½
    final Fighter po=current.getHoldedBy();

    if(po!=null)
    {
      // si le joueur va bouger
      if((short)nextCellID!=po.getCell().getId())
      {
        // on retire les ï¿½tats
        po.setState(Constant.ETAT_PORTEUR,0,po.getId());
        current.setState(Constant.ETAT_PORTE,0,po.getId());
        // on retire dï¿½ lie les 2 fighters
        po.setIsHolding(null);
        current.setHoldedBy(null);
        // La nouvelle case sera dï¿½finie plus tard dans le code. On
        // envoie les packets !
      }
    }

    current.getCell().getFighters().clear();
    current.setCell(getMap().getCase(nextCellID));
    current.getCell().addFighter(current);

    if(po!=null)// mï¿½me erreur que tantï¿½t, bug ou plus de fighter sur la
      // case
      po.getCell().addFighter(po);
    if(nStep<0)
    {
      nStep=nStep*(-1);
    }

    setCurAction("GA;129;"+current.getId()+";"+current.getId()+",-"+nStep);
    // Si porteur
    final Fighter po2=current.getIsHolding();

    if(po2!=null&&current.haveState(Constant.ETAT_PORTEUR)&&po2.haveState(Constant.ETAT_PORTE))
    {
      // on dï¿½place le portï¿½ sur la case
      po2.setCell(current.getCell());
    }

    if(fighter.getPersonnage()==null)
    {
      try
      {
        Thread.sleep((int)(400+(100*Math.sqrt(nStep))));
      }
      catch(final Exception e)
      {
    	 
      }
      this.setCurAction("");
      SpellEffect.verifyTraps(this,fighter);
      /*TimerWaiter.addNext(() -> {
        SocketManager.GAME_SEND_GAMEACTION_TO_FIGHT(this,7,this.getCurAction());
        this.setCurAction("");
        new ArrayList<>(this.getAllTraps()).stream().filter(trap -> trap!=null).filter(trap -> PathFinding.getDistanceBetween(getMap(),trap.getCell().getId(),current.getCell().getId())<=trap.getSize()).forEach(trap -> trap.onTraped(current));
      },700+150*nStep);*/
      return true;
    }

    if((getType()==Constant.FIGHT_TYPE_PVM)&&(getAllChallenges().size()>0)&&!current.isInvocation()&&!current.isDouble()&&!current.isCollector()||(getType()==Constant.FIGHT_TYPE_DOPEUL)&&(getAllChallenges().size()>0)&&!current.isInvocation()&&!current.isDouble()&&!current.isCollector())
      this.getAllChallenges().entrySet().stream().filter(c -> c.getValue()!=null).forEach(c -> c.getValue().onPlayerMove(fighter));

    if(fighter.getPersonnage()!=null)
      SpellEffect.verifyTraps(this,fighter);
    fighter.getPersonnage().getGameClient().addAction(GA);
    return true;
  }

  //v2.8 - Fixed nullpointerexception on fighter dying, fixed infinite carry when summoner dies and carrying summon
  public void onFighterDie(Fighter target, Fighter caster)
  {
    try
    {
      if(Config.getInstance().HEROIC && this.getMapOld().getSong() != 1)
      {	  
        Player player=caster.getPersonnage(),deadPlayer=target.getPersonnage();
        if(deadPlayer!=null)
        {
          byte type=caster.isMob() ? (byte)2 : player==deadPlayer ? (byte)-1 : (byte)1;
          long id=type==1 ? player.getId() : type==2 ? caster.getMob().getTemplate().getId() : 0;
          target.killedBy=new Pair<>(type,id);
        }
        if(player!=null&&target!=caster&&deadPlayer!=null)
          player.increaseTotalKills();
      }

      target.setIsDead(true);
      if(!target.hasLeft())
        this.getDeadList().add(new Pair<Integer, Fighter>(target.getId(),target));

      final Fighter current=this.getFighterByOrdreJeu();
      if(current==null)
        return;

      SocketManager.GAME_SEND_FIGHT_PLAYER_DIE_TO_FIGHT(this,7,target.getId());
      target.getCell().getFighters().clear();// Supprime tout causait bug si portï¿½/porteur
      if(target.haveState(Constant.ETAT_PORTEUR))
      {
        Fighter f=target.getIsHolding();
        f.setCell(f.getCell());
        f.getCell().addFighter(f);// Le bug venait par manque de ceci, il ni avait plus de firstFighter
        f.setState(Constant.ETAT_PORTE,0,caster.getId());// J'ajoute ceci quand mï¿½me pour signaler qu'ils ne sont plus en ï¿½tat portï¿½/porteur
        target.setState(Constant.ETAT_PORTEUR,0,caster.getId());
        f.setHoldedBy(null);
        target.setIsHolding(null);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,950,f.getId()+"",f.getId()+","+Constant.ETAT_PORTE+",0");
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,950,target.getId()+"",target.getId()+","+Constant.ETAT_PORTEUR+",0");
      }

      if(target.haveState(Constant.ETAT_PORTE))
    	  TimerWaiterPlus.addNext(() -> removeCarry(target),3000); //timer so mob only gets dropped after already despawning

      if((this.getType()==Constant.FIGHT_TYPE_PVM)&&(this.getAllChallenges().size()>0)||this.getType()==Constant.FIGHT_TYPE_DOPEUL&&this.getAllChallenges().size()>0)
        this.getAllChallenges().values().stream().filter(challenge -> challenge!=null).forEach(challenge -> challenge.onFighterDie(target));

      if(target.getTeam()==0)
      {
        HashMap<Integer, Fighter> team=new HashMap<>(this.getTeam0());

        for(Fighter entry : team.values())
        {
          if(entry.getInvocator()==null)
            continue;
          if(entry.getPdv()==0||entry.isDead())
            continue;

          if(entry.getInvocator().getId()==target.getId())
          {
            this.onFighterDie(entry,caster);

            try
            {
              if(this.getOrderPlaying()!=null)
              {
                int index=this.getOrderPlaying().indexOf(entry);
                if(index!=-1)
                  this.getOrderPlaying().remove(index);
              }
              if(this.getTeam0().containsKey(entry.getId()))
                this.getTeam0().remove(entry.getId());
              else if(this.getTeam1().containsKey(entry.getId()))
                this.getTeam1().remove(entry.getId());
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,999,target.getId()+"",this.getGTL());
          }
        }
      }
      else if(target.getTeam()==1)
      {
        HashMap<Integer, Fighter> team=new HashMap<>(this.getTeam1());

        for(Fighter fighter : team.values())
        {
          if(fighter.getInvocator()==null)
            continue;
          if(fighter.getPdv()==0||fighter.isDead())
            continue;
          if(fighter.getInvocator().getId()==target.getId())
          {// si il a ï¿½tï¿½ invoquï¿½ par le joueur mort
            fighter.setLevelUp(true);
            this.onFighterDie(fighter,caster);

            if(this.getOrderPlaying()!=null&&!this.getOrderPlaying().isEmpty())
            {
              try
              {
                int index=this.getOrderPlaying().indexOf(fighter);
                if(index!=-1)
                  this.getOrderPlaying().remove(index);
              }
              catch(Exception e)
              {
                e.printStackTrace();
              }
            }
            if(this.getTeam0().containsKey(fighter.getId()))
              this.getTeam0().remove(fighter.getId());
            else if(this.getTeam1().containsKey(fighter.getId()))
              this.getTeam1().remove(fighter.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,999,target.getId()+"",getGTL());
          }
        }
      }
      if(target.getMob()!=null)
      {
        try
        {
          if(target.isInvocation()&&!target.isStatique)
          {
            target.getInvocator().nbrInvoc--;
            // Il ne peut plus jouer, et est mort on revient au joueur
            // prï¿½cedent pour que le startTurn passe au suivant
            if(!target.canPlay()&&current.getId()==target.getId())
            {
              this.setCurPlayer(getCurPlayer()-1);
              this.endTurn(false,current);
            }
            // Il peut jouer, et est mort alors on passe son tour
            // pour que l'autre joue, puis on le supprime de l'index
            // sans problï¿½mes
            if(target.canPlay()&&current.getId()==target.getId())
            {
              this.setCurAction("");
              this.endTurn(false,current);
            }
            if(this.getOrderPlaying()!=null&&!this.getOrderPlaying().isEmpty())
            {
              int index=this.getOrderPlaying().indexOf(target);
              // Si le joueur courant a un index plus ï¿½levï¿½, on le
              // diminue pour ï¿½viter le outOfBound
              if(index!=-1)
              {
                if(getCurPlayer()>index&&getCurPlayer()>0)
                  this.setCurPlayer(getCurPlayer()-1);
                this.getOrderPlaying().remove(index);
              }

              if(this.getCurPlayer()<0)
                return;
              if(this.getTeam0().containsKey(target.getId()))
                this.getTeam0().remove(target.getId());
              else if(this.getTeam1().containsKey(target.getId()))
                this.getTeam1().remove(target.getId());
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,999,target.getId()+"",this.getGTL());
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
      if((getType()==Constant.FIGHT_TYPE_PVM||getType()==Constant.FIGHT_TYPE_DOPEUL)&&getAllChallenges().size()>0)
        this.getAllChallenges().values().stream().filter(challenge -> challenge!=null).forEach(challenge -> challenge.onMobDie(target,caster));

      new ArrayList<>(this.getAllGlyphs()).stream().filter(glyph -> glyph.getCaster().getId()==target.getId()).forEach(glyph -> {
    	  SocketManager.GAME_SEND_GDZ_PACKET_TO_FIGHT(this,7,"-",glyph.getCell().getId(),glyph.getSize(),5);
    	SocketManager.GAME_SEND_GDZ_PACKET_TO_FIGHT(this,7,"-",glyph.getCell().getId(),glyph.getSize(),4);
        SocketManager.GAME_SEND_GDZ_PACKET_TO_FIGHT(this,7,"-",glyph.getCell().getId(),glyph.getSize(),3);
        SocketManager.GAME_SEND_GDZ_PACKET_TO_FIGHT(this,7,"-",glyph.getCell().getId(),glyph.getSize(),2);
        SocketManager.GAME_SEND_GDZ_PACKET_TO_FIGHT(this,7,"-",glyph.getCell().getId(),glyph.getSize(),1);
        SocketManager.GAME_SEND_GDZ_PACKET_TO_FIGHT(this,7,"-",glyph.getCell().getId(),glyph.getSize(),0);
        SocketManager.GAME_SEND_GDC_PACKET_TO_FIGHT(this,7,glyph.getCell().getId());
        this.getAllGlyphs().remove(glyph);
      });

      new ArrayList<>(this.getAllTraps()).stream().filter(trap -> trap.getCaster().getId()==target.getId()).forEach(trap -> {
        trap.desappear();
        this.getAllTraps().remove(trap);
      });

      if(target.canPlay()&&current.getId()==target.getId()&&!current.hasLeft()) // java.lang.NullPointerException
        this.endTurn(false,current);
      if(target.isCollector())
      {// Le percepteur viens de mourrir on met fin au
        this.getFighters(target.getTeam2()).stream().filter(f -> !f.isDead()).forEach(f -> {
          this.onFighterDie(f,target);
          this.verifIfTeamAllDead();
        });
      }
      if(target.isPrisme())
      {
        this.getFighters(target.getTeam2()).stream().filter(f -> !f.isDead()).forEach(f -> {
          this.onFighterDie(f,target);
          this.verifIfTeamAllDead();
        });
      }

      for(Fighter fighter : getFighters(3))
      {
        ArrayList<SpellEffect> newBuffs=new ArrayList<>();
        for(SpellEffect entry : fighter.getFightBuff())
        {
         if(entry.getCaster().getId()==target.getId()) {
          if(entry.getEffectID() == 150) {
        	  SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,150,getId()+"",fighter.getId()+",0");
        	    //On actualise la position
        	    SocketManager.GAME_SEND_GIC_PACKET_TO_FIGHT(this,7,fighter);
        	  continue;
          }
         }
         try
         {
         int id =Integer.parseInt(entry.getArgs().split(";")[2]);
         if(id == 7) {
        	 fighter.setState(7,0,fighter.getId());
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fighter.getFight(),7,950,fighter.getId()+"",fighter.getId()+","+id+",0");
        continue;
         }
         if(id == 42) {
        	 fighter.setState(42,0,fighter.getId());
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fighter.getFight(),7,950,fighter.getId()+"",fighter.getId()+","+id+",0");
        continue;
         }
         }
         catch(Exception e)
         {
         }
          switch(entry.getSpell())
          {
            case 431:
            case 433:
            case 437:
            case 441:
            case 443:
              newBuffs.add(entry);
              continue;
          }
          if(entry.getCaster().getId()!=target.getId())
            newBuffs.add(entry);
        }
        fighter.getFightBuff().clear();
        fighter.getFightBuff().addAll(newBuffs);
      }
      SocketManager.GAME_SEND_GTL_PACKET_TO_FIGHT(this,7);
      this.verifIfTeamAllDead();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public void removeCarry(Fighter target)
  {
    Fighter carry=target.getHoldedBy();
    carry.setState(Constant.ETAT_PORTEUR,0,target.getId()); //duration 0, remove state
    carry.setIsHolding(null);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,7,51,carry.getId()+"",carry.getCell().getId()+"");
  }

  public ArrayList<Fighter> getFighters(int teams)
  {// Entre 0 et 7, binaire([spec][t2][t1]).
    ArrayList<Fighter> fighters=new ArrayList<>();

    if(teams-4>=0)
    {
      fighters.addAll(new ArrayList<>(this.getViewer().values()).stream().filter(player -> player!=null).map(player -> new Fighter(this,player)).collect(Collectors.toList()));
      teams-=4;
    }
    if(teams-2>=0)
    {
      new ArrayList<>(this.getTeam1().values()).stream().filter(fighter -> fighter!=null).forEach(fighters::add);
      teams-=2;
    }
    if(teams-1>=0)
      new ArrayList<>(this.getTeam0().values()).stream().filter(fighter -> fighter!=null).forEach(fighters::add);
    return fighters;
  }

  public ArrayList<Fighter> getFighters2(int teams)
  {
    ArrayList<Fighter> fighters=new ArrayList<>();

    if(teams==0)
      fighters.addAll(getViewer().entrySet().stream().map(entry -> new Fighter(this,entry.getValue())).collect(Collectors.toList()));
    if(teams==2)
      fighters.addAll(getTeam1().entrySet().stream().map(Entry::getValue).collect(Collectors.toList()));
    if(teams==1)
      fighters.addAll(getTeam0().entrySet().stream().map(Entry::getValue).collect(Collectors.toList()));
    return fighters;
  }
  public ArrayList<Fighter> getFighters3()
  {
    ArrayList<Fighter> fighters=new ArrayList<>();
      fighters.addAll(getTeam1().entrySet().stream().map(Entry::getValue).collect(Collectors.toList()));
      fighters.addAll(getTeam0().entrySet().stream().map(Entry::getValue).collect(Collectors.toList()));
    return fighters;
  }

  public Fighter getFighterByPerso(Player player)
  {
    Fighter fighter=null;
    if(this.getTeam0().get(player.getId())!=null)
      fighter=this.getTeam0().get(player.getId());
    if(this.getTeam1().get(player.getId())!=null)
      fighter=this.getTeam1().get(player.getId());
    return fighter;
  }

  //v2.8 - remove cell from list if taken, shuffle cells
  private GameCase getRandomCell(List<GameCase> cells)
  {
    if(cells.isEmpty())
      return null;
    Collections.shuffle(cells);
    for(GameCase possibleCell : cells)
    {
      if(!possibleCell.getFighters().isEmpty())
        continue;
      return possibleCell;
    }
    return null;
  }

  public  void exchangePlace(Player perso, int cell)
  {
    Fighter fighter=getFighterByPerso(perso);
    assert fighter!=null;
    if(fighter == null)
    	return;
    int team=fighter.getTeam();
    //if(collector!=null&&this.collectorProtect&&collector.getDefenseFight()!=null&&!collector.getDefenseFight().containsValue(perso))
     //   return;
    boolean valid1=false,valid2=false;

    for(int a=0;a<getStart0().size();a++)
      if(getStart0().get(a).getId()==cell)
        valid1=true;
    for(int a=0;a<getStart1().size();a++)
      if(getStart1().get(a).getId()==cell)
        valid2=true;
    if(getState()!=2||isOccuped(cell)||perso.isReady()||(team==0&&!valid1)||(team==1&&!valid2))
      return;
    fighter.getCell().getFighters().clear();
    fighter.setCell(getMap().getCase(cell));
    getMap().getCase(cell).addFighter(fighter);
    SocketManager.GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(this,3,getMap(),perso.getId(),cell);
  }
  public  void echange_pos(Player perso, Player perso2)
  {
    Fighter fighter=getFighterByPerso(perso);
    Fighter fighter2=getFighterByPerso(perso2);
    assert fighter!=null;
    assert fighter2!=null;
    if(getState()!=2||perso.isReady())
      return;
    int celle1 = fighter.getCell().getId();
    int celle2 = fighter2.getCell().getId();
    fighter.getCell().getFighters().clear();
    fighter2.getCell().getFighters().clear();
    fighter.setCell(getMap().getCase(celle2));
    fighter2.setCell(getMap().getCase(celle1));
    getMap().getCase(celle1).addFighter(fighter2);
    getMap().getCase(celle2).addFighter(fighter);
    SocketManager.GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(this,3,getMap(),perso.getId(),celle2);
    SocketManager.GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(this,3,getMap(),perso2.getId(),celle1);
  }

  public boolean isOccuped(int cell)
  {
    return getMap().getCase(cell)==null||getMap().getCase(cell).getFighters().size()>0;
  }

  public int getNextLowerFighterGuid()
  {
    return nextId--;
  }

  public void addFighterInTeam(Fighter f, int team)
  {
    if(team==0)
      getTeam0().put(f.getId(),f);
    else if(team==1)
      getTeam1().put(f.getId(),f);
  }

  void addChevalier()
  {
    String groupData="";
    int a=0;
    for(Fighter F : getTeam0().values())
    {
      if(F.getPersonnage()==null)
        continue;
      if(getTeam1().size()>getTeam0().size())
        continue;
      groupData=groupData+"394,"+Constant.getLevelForChevalier(F.getPersonnage())+","+Constant.getLevelForChevalier(F.getPersonnage())+";";
      if(a<getTeam0().size()-1)
        groupData=groupData+";";
      a++;
    }
    setMobGroup(new MobGroup(getMapOld().nextObjectId,getInit0().getPersonnage().getCurCell().getId(),groupData));
    for(Entry<Integer, MobGrade> entry : getMobGroup().getMobs().entrySet())
    {
      entry.getValue().setInFightID(entry.getKey());
      getTeam1().put(entry.getKey(),new Fighter(this,entry.getValue()));
    }
    List<Entry<Integer, Fighter>> e=new ArrayList<>();
    e.addAll(getTeam1().entrySet());
    for(Entry<Integer, Fighter> entry : e)
    {
      if(entry.getValue().getPersonnage()!=null)
        continue;
      Fighter f=entry.getValue();
      GameCase cell=getRandomCell(getStart1());
      if(cell==null)
      {
        getTeam1().remove(f.getId());
      }
      else
      {
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,f.getId()+"",f.getId()+","+Constant.ETAT_PORTE+",0");
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,f.getId()+"",f.getId()+","+Constant.ETAT_PORTEUR+",0");
        f.setCell(cell);
        f.getCell().addFighter(f);
        f.setTeam(1);
        SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPersonnage().getCurMap(),getMobGroup().getId(),entry.getValue());
        SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit1().getPersonnage().getCurMap(),getMobGroup().getId(),entry.getValue());
      }
    }
    SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,getMap());
  }

  public boolean playerDisconnect(Player player, boolean verif)
  {
    // True si entre en mode dï¿½connexion en combat, false sinon
    if(this.getState()==Constant.FIGHT_STATE_INIT||this.getState()==Constant.FIGHT_STATE_PLACE)
    {
      player.setReady(true);
      player.getFight().verifIfAllReady();
      SocketManager.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(player.getFight(),3,player.getId(),true);
      if(getFighterByPerso(player) != null)
    	  getFighterByPerso(player).Disconnect();
      return true;
    }
    final Fighter current=this.getFighterByOrdreJeu();

    if(current==null)
        return false;
    Fighter f=getFighterByPerso(player);
    if(f==null)
      return false;
    if(player.start!=null)
    {
      this.endFight(true,false);
      return true;
    }
    if(getState()==Constant.FIGHT_STATE_INIT||getState()==Constant.FIGHT_STATE_FINISHED)
    {
      if(!verif)
        leftFight(player,null);
      return false;
    }
    if(f.getNbrDisconnection()>=5)
    {
      if(!verif)
      {
        leftFight(player,null);
        for(Fighter e : this.getFighters(7))
        {
          if(e.getPersonnage()==null||!e.getPersonnage().isOnline())
            continue;
          SocketManager.GAME_SEND_MESSAGE(e.getPersonnage(),f.getPacketsName()+" has disconnected more than five times in the same fight and has been forced to leave automatically.","A00000");
        }
      }
      return false;
    }
    if(!verif)
    {
      if(!isBegin())
      {
        player.setReady(true);
        player.getFight().verifIfAllReady();
        SocketManager.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(player.getFight(),3,player.getId(),true);
        f.Disconnect();
      }
    }
    if(!verif)
    {
      if(!player.getFight().getFighterByPerso(player).isDeconnected())
        SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,7,"1182;"+f.getPacketsName()+"~20");
      f.Disconnect();
    }
    if(current.getId()==f.getId())
      endTurn(false,current);
    return true;
  }

  public boolean playerReconnect(final Player perso)
  {
    final Fighter current=this.getFighterByOrdreJeu();
    if(getState()!=Constant.FIGHT_STATE_PLACE)
    if(current==null)
      return false;
    final Fighter f=getFighterByPerso(perso);
    if(f==null)
      return false;
    if(getState()==Constant.FIGHT_STATE_INIT)
      return false;
    if(!f.isDeconnected())
    	return false;
    f.Reconnect();
    if(getState()==Constant.FIGHT_STATE_FINISHED)
      return false;
    // Si combat en cours on envois des im
    ArrayList<Fighter> all=new ArrayList<>();
    all.addAll(this.getTeam0().values());
    all.addAll(this.getTeam1().values());
    
    
    all.stream().filter(fighter -> fighter!=null&&fighter.isHide()).forEach(f1 -> SocketManager.GAME_SEND_GA_PACKET(this,perso,150,f1.getId()+"",f1.getId()+",4"));

    SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,7,"1184;"+f.getPacketsName());

    if(getState()==Constant.FIGHT_STATE_ACTIVE)
      SocketManager.GAME_SEND_GJK_PACKET(perso,getState(),0,0,0,0,getType());// Join Fight => getState(), pas d'anulation...
    else
    {
      if(getType()==Constant.FIGHT_TYPE_CHALLENGE)
        SocketManager.GAME_SEND_GJK_PACKET(perso,2,1,1,0,0,getType());
      else
        SocketManager.GAME_SEND_GJK_PACKET(perso,2,0,1,0,0,getType());
    }
    try {
    SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(perso,getMap(),(f.getTeam()==0 ? getInit0() : getInit1()).getId(),f);// Indication de la team
    }catch(Exception e)
    {
    	
    }
    SocketManager.GAME_SEND_STATS_PACKET(perso);

    SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,getMap(),perso);

    if(getState()==Constant.FIGHT_STATE_PLACE)
    {
      SocketManager.GAME_SEND_FIGHT_PLACES_PACKET(perso.getGameClient(),getMap().getPlaces(),getSt1());
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTE+",0");
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTEUR+",0");
    }
    else
    {
        SocketManager.GAME_SEND_FIGHT_PLACES_PACKET(perso.getGameClient(),getMap().getPlaces(),getSt1());
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTE+",0");
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this,3,950,perso.getId()+"",perso.getId()+","+Constant.ETAT_PORTEUR+",0");
      
      SocketManager.GAME_SEND_GS_PACKET(perso);// Dï¿½but du jeu
      SocketManager.GAME_SEND_GTL_PACKET(perso,this);// Liste des tours
      SocketManager.GAME_SEND_GAMETURNSTART_PACKET(perso,current.getId(),(int)(System.currentTimeMillis()-launchTime),current.getTour());
      // SocketManager.GAME_SEND_GTM_PACKET(perso, this);
      if((getType()==Constant.FIGHT_TYPE_PVM||getType()==Constant.FIGHT_TYPE_DOPEUL)&&(getAllChallenges().size()>0))
      {
        for(Entry<Integer, Challenge> c : getAllChallenges().entrySet())
        {
          if(c.getValue()==null)
            continue;
          SocketManager.GAME_SEND_CHALLENGE_PERSO(perso,c.getValue().parseToPacket());
          if(!c.getValue().loose())
            c.getValue().challengeSpecLoose(perso);
        }
      }
      for(Fighter f1 : getFighters(3))
        f1.sendState(perso);
    }
    ArrayList<Glyph> glyphs = new ArrayList<Glyph>();// Copie du
	ArrayList<Trap> trap = new ArrayList<Trap>();// Copie du
	trap.addAll(allTraps);
	glyphs.addAll(allGlyphs);
	for (Glyph g : glyphs) {
		String str = "GDZ+" + g.getCell().getId() + ";" + g.getSize() + ";"
				+ g.getColor();
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(Fight.this, 7, 999,
				g.getCaster().getId() + "", str);
	}
	for (Trap g : trap) {
		if(g.getCaster().getId() == perso.getId()){
		String str = "GDZ+" + g.getCell().getId() + ";" + g.getSize() + ";"
				+ g.getColor();
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(Fight.this, 7, 999,
				g.getCaster().getId() + "", str);
		str = "GDC" + g.getCell().getId() + ";Haaaaaaaaz3005;";
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(Fight.this, 7, 999,
				new StringBuilder(String.valueOf(g.getCaster().getId())).toString(), str);
		
	}
	}
	
	 for(LaunchedSpell S : f.getLaunchedSorts())
	    {
     	perso.send("kM"+f.getId()+","+S.getSpellId()+","+f.getCell().getId()+","+S.getCooldown()); 

	    }
	 if(perso.getParty() != null) {
		 SocketManager.GAME_SEND_GROUP_CREATE(perso.getGameClient(),perso.getParty());
	      SocketManager.GAME_SEND_PL_PACKET(perso.getGameClient(),perso.getParty());
	      SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(perso.getGameClient(),perso.getParty()); 
	 }
    return true;
  }

  public void verifIfAllReady()
  {
    boolean val=true;
    if(getType()==Constant.FIGHT_TYPE_DOPEUL)
    {
      for(Fighter f : getTeam0().values())
      {
        if(f==null||f.getPersonnage()==null)
          continue;
        Player perso=f.getPersonnage();
        if(!perso.isReady())
          val=false;
      }
      if(val)
        startFight();
      return;
    }

    for(int a=0;a<getTeam0().size();a++) {
    	if(getTeam0().get(getTeam0().keySet().toArray()[a]) != null)
    	if(getTeam0().get(getTeam0().keySet().toArray()[a]).getPersonnage() != null)
      if(!getTeam0().get(getTeam0().keySet().toArray()[a]).getPersonnage().isReady())
        val=false;
    }
    if(getType()!=4&&getType()!=5&&getType()!=7&&getType()!=Constant.FIGHT_TYPE_CONQUETE)
      for(int a=0;a<getTeam1().size();a++)
      {
    	  if(getTeam1().get(getTeam1().keySet().toArray()[a]) != null)
    	  if(getTeam1().get(getTeam1().keySet().toArray()[a]).getPersonnage() != null)
        if(!getTeam1().get(getTeam1().keySet().toArray()[a]).getPersonnage().isReady())
          val=false;
      }
    if(getType()==5||getType()==2)
      val=false;
    if(val)
      startFight();
  }

  boolean verifyStillInFight()// Return true si au moins un joueur est encore dans le combat
  {
    for(Fighter f : getTeam0().values())
    {
      if(f.isCollector())
        return false;
      if(f.isInvocation()||f.isDead()||f.getPersonnage()==null||f.getMob()!=null||f.getDouble()!=null||f.hasLeft())
        continue;
      if(f.getPersonnage()!=null&&f.getPersonnage().getFight()!=null&&f.getPersonnage().getFight().getId()==this.getId()) // Si il n'est plus dans ce combat
        return false;
    }
    for(Fighter f : getTeam1().values())
    {
      if(f.isCollector())
        return false;
      if(f.isInvocation()||f.isDead()||f.getPersonnage()==null||f.getMob()!=null||f.getDouble()!=null||f.hasLeft())
        continue;
      if(f.getPersonnage()!=null&&f.getPersonnage().getFight()!=null&&f.getPersonnage().getFight().getId()==this.getId()) // Si il n'est plus dans ce combat
        return false;
    }
    return true;
  }

  public boolean verifOtherTeamIsDead(Fighter f)
  {
    if(f.getTeam()==0)
    {
      boolean finish=true;
      for(Entry<Integer, Fighter> entry : getTeam1().entrySet())
      {
        if(entry.getValue().isInvocation())
          continue;
        if(!entry.getValue().isDead())
        {
          finish=false;
          break;
        }
      }
      return finish;
    }
    else
    {
      boolean finish=true;
      for(Entry<Integer, Fighter> entry : getTeam0().entrySet())
      {
        if(entry.getValue().isInvocation())
          continue;
        if(!entry.getValue().isDead())
        {
          finish=false;
          break;
        }
      }
      return finish;
    }
  }

  public boolean verifIfTeamIsDead()
  {
    boolean finish=true;
    for(Entry<Integer, Fighter> entry : getTeam1().entrySet())
    {
      if(entry.getValue().isInvocation())
        continue;
      if(!entry.getValue().isDead())
      {
        finish=false;
        break;
      }
    }
    return finish;
  }

  //v2.5 - follow system / endfight teleport fix
  //v2.7 - Replaced String += with StringBuilder
  public void verifIfTeamAllDead()
  {
    if(getState()>=Constant.FIGHT_STATE_FINISHED)
      return;

    boolean team0=true,team1=true;

    for(Fighter fighter : getTeam0().values())
    {
      if(fighter.isInvocation())
        continue;
      if(!fighter.isDead())
      {
        team0=false;
        break;
      }
    }

    for(Fighter fighter : getTeam1().values())
    {
      if(fighter.isInvocation())
        continue;
      if(!fighter.isDead())
      {
        team1=false;
        break;
      }
    }

    if((team0||team1||verifyStillInFight())&&!finish)
    {
      this.finish=true;

      final Map<Integer, Fighter> copyTeam0=new HashMap<>();
      final Map<Integer, Fighter> copyTeam1=new HashMap<>();
      for(Entry<Integer, Fighter> entry : this.getTeam0().entrySet())
      {
        if(entry.getValue().getMob()!=null)
          if(entry.getValue().getMob().getTemplate().getId()==375)
            Bandit.getBandits().setPop(false);
        copyTeam0.put(entry.getKey(),entry.getValue());
      }

      for(Entry<Integer, Fighter> entry : this.getTeam1().entrySet())
      {
        if(entry.getValue().getMob()!=null)
          if(entry.getValue().getMob().getTemplate().getId()==375)
            Bandit.getBandits().setPop(false);
        copyTeam1.put(entry.getKey(),entry.getValue());
      }

      final boolean winners=team0;

      final ArrayList<Fighter> fighters=new ArrayList<>();
      fighters.addAll(copyTeam0.values());
      fighters.addAll(copyTeam1.values());
      this.turn.stop();
      this.turn=null;
      try
      {
        StringBuilder challenges=new StringBuilder();
        if((getType()==Constant.FIGHT_TYPE_PVM&&getAllChallenges().size()>0)||(getType()==Constant.FIGHT_TYPE_DOPEUL&&getAllChallenges().size()>0))
        {
          for(Challenge challenge : getAllChallenges().values())
          {
            if(challenge!=null)
            {
              challenge.fightEnd();
              challenges.append((challenges.toString().isEmpty() ? challenge.getPacketEndFight() : ","+challenge.getPacketEndFight()));
            }
          }
        }

        this.setState(Constant.FIGHT_STATE_FINISHED);

        final ArrayList<Fighter> winTeam=new ArrayList<>(),looseTeam=new ArrayList<>();

        if(winners)
        {
          looseTeam.addAll(copyTeam0.values());
          winTeam.addAll(copyTeam1.values());
        }
        else
        {
          winTeam.addAll(copyTeam0.values());
          looseTeam.addAll(copyTeam1.values());
        }

        if(Constant.FIGHT_TYPE_PVM==this.getType()&&this.getMapOld().hasEndFightAction(this.getType()))
        {
          for(Fighter fighter : winTeam)
          {
            Player player=fighter.getPersonnage();
            if(player==null)
              continue;

            player.setFight(null);
            if(fighter.isDeconnected())
            {
              player.setNeededEndFight(this.getType(),this.getMobGroup());
              player.getCurMap().applyEndFightAction(player);
              player.setNeededEndFight(-1,null);
            }
            else
            {
              player.setNeededEndFight(this.getType(),this.getMobGroup());
            }
            
          }
        }

        final String packet=this.getGE(winners ? 2 : 1);

        for(Fighter fighter : fighters)
        {
          Player player=fighter.getPersonnage();
          if(player!=null)
          {
            player.setFight(null);
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getMap(),fighter.getId());
          }
        }

        for(Player player : this.getViewer().values())
        {
          player.refreshMapAfterFight();
          player.setSpec(false);
          SocketManager.send(player,packet);

          if(player.getAccount().isBanned())
            player.getGameClient().disconnect();
        }

        this.setCurPlayer(-1);

        switch(this.getType())
        {
          case Constant.FIGHT_TYPE_CHALLENGE:
          case Constant.FIGHT_TYPE_KOLI:
          case Constant.FIGHT_TYPE_AGRESSION:
          case Constant.FIGHT_TYPE_CONQUETE:
        }

        for(Fighter fighter : this.getTeam0().values())
        {
          Player player=fighter.getPersonnage();

          if(player!=null)
          {
            player.setDuelId(-1);
            player.setReady(false);
          }
        }

        for(Fighter fighter : this.getFighters(3))
          fighter.getFightBuff().clear();

        this.getMapOld().removeFight(this.getId());
        SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(Main.world.getMap(this.getMap().getId()));

        final String str=(this.getPrism()!=null ? this.getPrism().getMap()+"|"+this.getPrism().getX()+"|"+this.getPrism().getY() : "");

        this.setMap(null);
        this.orderPlaying=null;

        /** WINNER **/
        for(Fighter fighter : winTeam)
        {
          /** Collector **/
          if(fighter.getCollector()!=null)
          {
            for(Player player : Main.world.getGuild(getGuildId()).getOnlineMembers())
            {
              if(!player.isOnline())
                player.setOnline(true);
              SocketManager.GAME_SEND_gITM_PACKET(player,Collector.parseToGuild(player.get_guild().getId()));
              SocketManager.GAME_SEND_PERCO_INFOS_PACKET(player,fighter.getCollector(),"S");
            }

            fighter.getCollector().setInFight((byte)0);
            fighter.getCollector().set_inFightID((byte)-1);
            fighter.getCollector().clearDefenseFight();

            this.getMapOld().getPlayers().stream().filter(player -> player!=null).forEach(player -> SocketManager.GAME_SEND_MAP_PERCO_GMS_PACKETS(player.getGameClient(),player.getCurMap()));
          }
          /** Prism **/
          if(fighter.getPrism()!=null)
          {
            Main.world.getOnlinePlayers().stream().filter(player -> player!=null).filter(player -> player.get_align()==getPrism().getAlignement()).forEach(player -> SocketManager.SEND_CS_SURVIVRE_MESSAGE_PRISME(player,str));

            fighter.getPrism().setInFight(-1);
            fighter.getPrism().setFightId(-1);

            this.getMapOld().getPlayers().stream().filter(player -> player!=null).forEach(player -> SocketManager.SEND_GM_PRISME_TO_MAP(player.getGameClient(),player.getCurMap()));
          }
 
          if(fighter.isInvocation())
            continue;
          if(fighter.hasLeft())
            continue;

          this.onPlayerWin(fighter,looseTeam);
        }
        /** END WINNER **/

        /** LOOSER **/
        for(Fighter fighter : looseTeam)
        {
          if(fighter.getCollector()!=null)
          {
            for(Player player : Main.world.getGuild(getGuildId()).getOnlineMembers())
            {
              if(!player.isOnline())
                player.setOnline(true);
              if(player.get_guild() == null)
            	  continue;
              SocketManager.GAME_SEND_gITM_PACKET(player,Collector.parseToGuild(player.get_guild().getId()));
              SocketManager.GAME_SEND_PERCO_INFOS_PACKET(player,fighter.getCollector(),"D");
            }

            this.getMapOld().RemoveNpc(fighter.getCollector().getId());
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getMapOld(),fighter.getCollector().getId());
            fighter.getCollector().reloadTimer();
            this.getCollector().delCollector(fighter.getCollector().getId());
            Database.getDynamics().getCollectorData().delete(fighter.getCollector().getId());
          }

          if(fighter.getPrism()!=null)
          {
            SubArea subarea=this.getMapOld().getSubArea();

            for(Player player : Main.world.getOnlinePlayers())
            {
              if(player==null)
                continue;

              if(player.get_align()==0)
              {
                SocketManager.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(player,subarea.getId()+"|-1|1");
                continue;
              }

              if(player.get_align()==getPrism().getAlignement())
                SocketManager.SEND_CD_MORT_MESSAGE_PRISME(player,str);

              SocketManager.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(player,subarea.getId()+"|-1|0");

              if(getPrism().getConquestArea()!=-1)
              {
                SocketManager.GAME_SEND_aM_ALIGN_PACKET_TO_AREA(player,subarea.getArea().getId()+"|-1");
                subarea.getArea().setPrismId(0);
                subarea.getArea().setAlignement(0);
              }
              SocketManager.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(player,subarea.getId()+"|0|1");
            }
            subarea.time = System.currentTimeMillis();
            final int id=fighter.getPrism().getId();
            subarea.setPrismId(0);
            subarea.setAlignement(0);
            this.getMapOld().RemoveNpc(id);
            SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getMapOld(),id);
            Main.world.removePrisme(id);
            Database.getDynamics().getPrismData().delete(id);
          }

          if(fighter.getMob()!=null)
            continue;
          if(fighter.isInvocation())
            continue;

          this.onPlayerLoose(fighter);
        }
        /** END LOOSER **/

        for(Fighter fighter : fighters)
        {
          Player player=fighter.getPersonnage();
          if(player!=null)
          {
            if(this.isBegin())
            {
              if(player.getCurMap().getId()==8357&&player.hasItemTemplate(7373,1)&&player.hasItemTemplate(7374,1)&&player.hasItemTemplate(7375,1)&&player.hasItemTemplate(7376,1)&&player.hasItemTemplate(7377,1)&&player.hasItemTemplate(7378,1))
              {
                player.removeByTemplateID(7373,1);
                player.removeByTemplateID(7374,1);
                player.removeByTemplateID(7375,1);
                player.removeByTemplateID(7376,1);
                player.removeByTemplateID(7377,1);
                player.removeByTemplateID(7378,1);
              }
              if(player.getParty() != null && player.getParty().getMaster() != null &&  player.getParty().getMaster().getId() != player.getId())
              {
            	  
            	  player.send(packet);
            	  TimerWaiterPlus.addNext(() -> {
            		  player.send("GE");
 				 },500);
            	  
              }
              else
              player.send(packet);
            }
          }
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
        for(Fighter fighter : fighters)
        {
          Player player=fighter.getPersonnage();
          if(player!=null)
          {
            player.setDuelId(-1);
            player.setReady(false);
            player.setFight(null);
            SocketManager.GAME_SEND_GV_PACKET(player);
          }
        }
      }

      for(Fighter fighter : fighters)
      {
        Player player=fighter.getPersonnage();

        if(player==null)
          continue;

        if(player.getFight()!=null)
          player.setFight(null);

        player.refreshLife(false);

        if(!player.isFollowing)
        {
          if(Config.getInstance().teleports.containsKey((int)player.getCurMap().getId()))
          {
            int teleCell=Config.getInstance().teleports.get((int)player.getCurMap().getId());
            TimerWaiterPlus.addNext(() ->  player.teleport(player.getCurMap(),teleCell),1500);
          }
        }

        if(player.getAccount().isBanned())
          player.getGameClient().disconnect();
        if(fighter.isDeconnected())
          player.getAccount().disconnect(player);
        if(player.getMorphMode())
          SocketManager.GAME_SEND_SPELL_LIST(player);
      }
    }

  }

  void onPlayerWin(Fighter fighter, ArrayList<Fighter> looseTeam)
  {
    Player player=fighter.getPersonnage();

    if(player==null)
      return;

    player.afterFight=true;
    
  //ONE WINDOWS
    if(player.getParty() != null)
    if(player.isOne_windows()) {
    	player.getParty().setOne_windows(null);
 SocketManager.GAME_SEND_ASK_WINDOW(player.getGameClient(), player);
 SocketManager.GAME_SEND_SPELL_LIST_ONE_WINDOWS(player,player);
 if(player.getMetiers().size()>0)
 {
   ArrayList<JobStat> list=new ArrayList<JobStat>();
   list.addAll(player.getMetiers().values());
   //packet JS
   SocketManager.GAME_SEND_JS_PACKET(player,list);
   //packet JX
   SocketManager.GAME_SEND_JX_PACKET(player,list);
   //Packet JO (Job Option)
   SocketManager.GAME_SEND_JO_PACKET(player,list);
   GameObject obj=player.getObjetByPos(Constant.ITEM_POS_ARME);
   if(obj!=null)
     for(JobStat sm : list)
       if(sm.getTemplate().isValidTool(obj.getTemplate().getId()))
           SocketManager.GAME_SEND_OT_PACKET(player.getGameClient(),sm.getTemplate().getId());
       }
 
    	player.send("kI"+player.getId());
    }
    for(Fighter f : looseTeam)
    {
      if(f.getMob()==null)
        continue;

      int id=f.getMob().getTemplate().getId();
        if(id == Main.id_mob_relique_dj) {
        	player.relique_paase = true;
        SocketManager.GAME_SEND_MESSAGE(player,"La relique du jour est <b>["+Main.relique_donjon+"]</b> pour <b>Antre du kralamour </b> Active.","257C38");
        }
    }
	
    GameObject weapon=player.getObjetByPos(Constant.ITEM_POS_ARME);
    if(weapon!=null)
    {
      if(weapon.getTxtStat().containsKey(Constant.STATS_RESIST))
      {
        int statNew=Integer.parseInt(weapon.getTxtStat().get(Constant.STATS_RESIST),16)-1;
        if(statNew<=0)
        {
          SocketManager.send(player,"Im160");
          player.removeItem(weapon.getGuid(),1,true,true);
        }
        else
        {
          weapon.getTxtStat().remove(Constant.STATS_RESIST); // on retire les stats "32c"
          weapon.addTxtStat(Constant.STATS_RESIST,Integer.toHexString(statNew));// on ajout les bonnes stats
          SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player,weapon);
        }
      }
    }

    if(this.getType()!=Constant.FIGHT_TYPE_CHALLENGE && this.getType()!=Constant.FIGHT_TYPE_KOLI)
    {
      if(fighter.getPdv()<=0)
        player.setPdv(1);
      else
        player.setPdv(fighter.getPdv());

      if(fighter.getLevelUp())
        player.fullPDV();
    }

    if(this.getType()==2)
      if(this.getPrism()!=null)
        SocketManager.SEND_CP_INFO_DEFENSEURS_PRISME(player,this.getDefenders());
    
    if(this.getType()==Constant.FIGHT_TYPE_KOLI) {
    	TimerWaiterPlus.addNext(() ->  player.teleportOld(),6000);
    	 if(Main.world.addKoli_players(player))
      	  { 
      	   SocketManager.GAME_SEND_MESSAGE(player,"Vous vous étes inscrit au Kolizeum", "008000");
      	  }
    	 World.get_Succes(player.getId()).koli_wine_add(player);
    	 long xp=Formulas.getXpStalk(player.getLevel());
         player.addXp(xp);
    	 SocketManager.GAME_SEND_Im_PACKET(player,"08;"+xp);
    }
    if(this.getType()==Constant.FIGHT_TYPE_PVT)
      if(player.getGuildMember()!=null)
        if(this.getCollector().getGuildId()==player.getGuildMember().getGuild().getId())
        	TimerWaiterPlus.addNext(() ->  player.teleportOld(),1500);

    if(this.getType()==Constant.FIGHT_TYPE_AGRESSION)
    	World.get_Succes(player.getId()).pvp_add(player);
    
    if(this.getType()==Constant.FIGHT_TYPE_PVM)
    {
    	if(this.getMapOld().getSong() == 1) {
    	player.Song = player.Song+1;
    	World.get_Succes(player.getId()).Check(player, 18);
    	GameObject obj2=Main.world.getObjTemplate(11502).createNewItem(1,false);
        if(player.addObjet(obj2,true))
          World.addGameObject(obj2,true);
        SocketManager.GAME_SEND_Ow_PACKET(player);
        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+11502);
    	}
    	World.get_Succes(player.getId()).combat_add(player);
    	player.setTotal_combat();
    	if(this.donjon)
    	World.get_Succes(player.getId()).donjon_add(player);
    	
    	if(this.archi)
    	World.get_Succes(player.getId()).archi_add(player);
    	
    	World.get_Succes(player.getId()).level(player);
      GameObject obj=player.getObjetByPos(Constant.ITEM_POS_FAMILIER);
      if(obj!=null)
      {
        Map<Integer, Integer> souls=new HashMap<>();

        for(Fighter f : looseTeam)
        {
          if(f.getMob()==null)
            continue;

          int id=f.getMob().getTemplate().getId();

          if(!souls.isEmpty()&&souls.containsKey(id))
            souls.put(id,souls.get(id)+1);
          else
            souls.put(id,1);
        }
        if(!souls.isEmpty())
        {
          PetEntry pet=Main.world.getPetsEntry(obj.getGuid());
          if(pet!=null)
            pet.eatSouls(player,souls);
        }
        
      }
    }
  }

  void onPlayerLoose(Fighter fighter)
  {
    final Player player=fighter.getPersonnage();

    if(player==null)
      return;

    if(player.getMorphMode()&&player.donjon)
      player.unsetFullMorph();
    
  	//ONE WINDOWS
    if(player.isOne_windows()) {
    	if(player.getParty() != null)
    	player.getParty().setOne_windows(null);
    	SocketManager.GAME_SEND_ASK_WINDOW(player.getGameClient(), player);
    	 SocketManager.GAME_SEND_SPELL_LIST_ONE_WINDOWS(player,player);
    	 if(player.getMetiers().size()>0)
    	 {
    	   ArrayList<JobStat> list=new ArrayList<JobStat>();
    	   list.addAll(player.getMetiers().values());
    	   //packet JS
    	   SocketManager.GAME_SEND_JS_PACKET(player,list);
    	   //packet JX
    	   SocketManager.GAME_SEND_JX_PACKET(player,list);
    	   //Packet JO (Job Option)
    	   SocketManager.GAME_SEND_JO_PACKET(player,list);
    	   GameObject obj=player.getObjetByPos(Constant.ITEM_POS_ARME);
    	   if(obj!=null)
    	     for(JobStat sm : list)
    	       if(sm.getTemplate().isValidTool(obj.getTemplate().getId()))
    	           SocketManager.GAME_SEND_OT_PACKET(player.getGameClient(),sm.getTemplate().getId());
    	       }
    	 
    	player.send("kI"+player.getId());
    }
  //ONE WINDOWS
    
    GameObject arme=player.getObjetByPos(Constant.ITEM_POS_ARME);

    if(arme!=null)
    {
      if(arme.getTxtStat().containsKey(Constant.STATS_RESIST))
      {
        int statNew=Integer.parseInt(arme.getTxtStat().get(Constant.STATS_RESIST),16)-1;
        if(statNew<=0)
        {
          SocketManager.send(player,"Im160");
          player.removeItem(arme.getGuid(),1,true,true);
        }
        else
        {
          arme.getTxtStat().remove(Constant.STATS_RESIST); // on retire les stats "32c"
          arme.addTxtStat(Constant.STATS_RESIST,Integer.toHexString(statNew));// on ajout les bonnes stats
          SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player,arme);
        }
      }
    }

    if(player.getObjetByPos(Constant.ITEM_POS_FAMILIER)!=null&&this.getType()!=Constant.FIGHT_TYPE_CHALLENGE
    		&&this.getType()!=Constant.FIGHT_TYPE_KOLI)
    {
      GameObject obj=player.getObjetByPos(Constant.ITEM_POS_FAMILIER);
      if(obj!=null)
      {
        PetEntry pets=Main.world.getPetsEntry(obj.getGuid());
        if(pets!=null)
          pets.looseFight(player);
      }
    }
    if(this.getType()==Constant.FIGHT_TYPE_AGRESSION)
    	World.get_Succes(player.getId()).pvp_lose_add(player);
    
    if(player.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR)!=null)
      player.setMascotte(0);

    if(this.getType()==2)
      if(this.getPrism()!=null)
        SocketManager.SEND_CP_INFO_DEFENSEURS_PRISME(player,this.getDefenders());
    
    if(this.getType()==Constant.FIGHT_TYPE_AGRESSION||this.getType()==Constant.FIGHT_TYPE_CONQUETE)
    {
      int honor=player.get_honor()-150;
      if(honor<0)
        honor=0;
      player.set_honor(honor);
    }
    if(this.getType()==Constant.FIGHT_TYPE_KOLI) {
    	TimerWaiterPlus.addNext(() ->  player.teleportOld(),6000);
    	 SocketManager.GAME_SEND_MESSAGE(player,"Vous avez quitté le Kolizeum", "FF0000");
    	 World.get_Succes(player.getId()).koli_lose_add(player);
    }

    if(this.getType()!=Constant.FIGHT_TYPE_CHALLENGE
    		&&this.getType()!=Constant.FIGHT_TYPE_KOLI)
    {
      int loose=Formulas.getLoosEnergy(player.getLevel(),getType()==1,getType()==5);
      int energy=player.getEnergy()-loose;

      player.setEnergy((energy<0 ? 0 : energy));

      if(player.isOnline())
        SocketManager.GAME_SEND_Im_PACKET(player,"034;"+loose);
      if(Config.getInstance().HEROIC && this.getMapOld().getSong() != 1)
      {
    	  
        if(fighter.killedBy!=null)
          player.die(fighter.killedBy.getLeft(),fighter.killedBy.getRight());
    	  
      }
      else
      {
        if(energy<=0)
        {
          if(this.getType()==Constant.FIGHT_TYPE_AGRESSION&&fighter.getTraqued())
          {
            if(getTeam1().containsValue(fighter))
            	TimerWaiterPlus.addNext(() ->  player.teleportFaction(this.getAlignementOfTraquer(this.getTeam0().values(),player)),1500);
            else
            	TimerWaiterPlus.addNext(() ->  player.teleportFaction(this.getAlignementOfTraquer(this.getTeam0().values(),player)),1500);
            player.setEnergy(1);
          }
          else
          {
            player.setFuneral();
          }
        }
        else
        {
          if(this.getType()==Constant.FIGHT_TYPE_AGRESSION&&fighter.getTraqued())
          {
            if(getTeam1().containsValue(fighter))
            	TimerWaiterPlus.addNext(() ->  player.teleportFaction(this.getAlignementOfTraquer(this.getTeam0().values(),player)),1500);
            else
            	TimerWaiterPlus.addNext(() ->  player.teleportFaction(this.getAlignementOfTraquer(this.getTeam0().values(),player)),1500);
          }
          else
          {
 
              player.setNeededEndFightAction(new Action(1001,player.getSavePosition(),"",null));
              player.setPdv(1);
            
          }
        }
      }
    }
  }

  int getAlignementOfTraquer(Collection<Fighter> fighters, Player player)
  {
    for(Fighter fighter : fighters)
      if(fighter.getPersonnage()!=null)
    	  if(fighter.getPersonnage().get_traque() != null)
        if(fighter.getPersonnage().get_traque().getTraque()==player)
          return (int)fighter.getPersonnage().get_align();
    return 0;
  }

  public void onGK(Player player)
  {
    final Fighter current=this.getFighterByOrdreJeu();
    if(current==null)
      return;
    if(getCurAction().equals("")||current.getId()!=player.getId()||getState()!=Constant.FIGHT_STATE_ACTIVE)
      return;

    SocketManager.GAME_SEND_GAMEACTION_TO_FIGHT(this,7,this.getCurAction());
    SocketManager.GAME_SEND_GAF_PACKET_TO_FIGHT(this,7,2,current.getId());

    //final ArrayList<Trap> traps=new ArrayList<>(this.getAllTraps());
    // final Fighter fighter=getFighterByPerso(player);
    //final int currentCell=fighter.getCell().getId();

    if(!current.getJustTrapped()&&current.getFight()!=null)
    {
      /*  for(Trap trap : traps)
      {
        if(PathFinding.getDistanceBetween(this.getMap(),trap.getCell().getId(),currentCell)<=trap.getSize())
          trap.onTraped(fighter);
        if(this.getState()==Constant.FIGHT_STATE_FINISHED)
          break;
      }*/
      SpellEffect.verifyTraps(current.getFight(),current);
    }

    this.setCurAction("");
  }

  public String getGE(int win)
  {
    int type=Constant.FIGHT_TYPE_CHALLENGE;
    if(this.getType()==Constant.FIGHT_TYPE_AGRESSION||getType()==Constant.FIGHT_TYPE_CONQUETE)
      type=1;
    if(this.getType()==Constant.FIGHT_TYPE_PVT)
      type=Constant.FIGHT_TYPE_CHALLENGE;
    final StringBuilder packet=new StringBuilder();

    packet.append("GE").append(System.currentTimeMillis()-getStartTime());
    if(getType()==Constant.FIGHT_TYPE_PVM&&getMobGroup()!=null)
      packet.append(';').append(getMobGroup().getStarBonus(getMobGroup().getInternalStarBonus()));
    packet.append("|").append(this.getInit0().getId()).append("|").append(type).append("|");

    ArrayList<Fighter> winners=new ArrayList<>(),loosers=new ArrayList<>();

    @SuppressWarnings("rawtypes")
	Iterator iterator=this.getTeam0().entrySet().iterator();
    while(iterator.hasNext())
    {
      @SuppressWarnings("rawtypes")
	Entry entry=(Entry)iterator.next();
      Fighter fighter=(Fighter)entry.getValue();

      if(fighter.isInvocation()&&fighter.getMob()!=null&&fighter.getMob().getTemplate().getId()!=285)
        iterator.remove();
      if(fighter.isDouble())
        iterator.remove();
    }

    iterator=this.getTeam1().entrySet().iterator();
    while(iterator.hasNext())
    {
      @SuppressWarnings("rawtypes")
	Entry entry=(Entry)iterator.next();
      Fighter fighter=(Fighter)entry.getValue();

      if(fighter.isInvocation()&&fighter.getMob()!=null&&fighter.getMob().getTemplate().getId()!=285)
        iterator.remove();
      if(fighter.isDouble())
        iterator.remove();
    }

    if(win==1)
    {
      winners.addAll(this.getTeam0().values());
      loosers.addAll(this.getTeam1().values());
    }
    else
    {
      winners.addAll(this.getTeam1().values());
      loosers.addAll(this.getTeam0().values());
    }

    try
    {
      boolean team=false;

      long totalXP=0;
      for(Fighter F : loosers)
      {
        if(F.getMob()!=null)
          totalXP+=F.getMob().getBaseXp();
        if(F.getPersonnage()!=null)
          team=true;
      }

      /* Capture d'ï¿½mes **/
      boolean mobCapturable=true;
      for(Fighter fighter : loosers)
      {
        if(fighter.getMob()==null||fighter.getMob().getTemplate()==null||!fighter.getMob().getTemplate().isCapturable())
        {
          mobCapturable=false;
        }
        if(fighter.getMob()!=null&&fighter.getMob().getTemplate()!=null)
        {
          for(int[] protector : soufix.job.JobConstant.JOB_PROTECTORS)
          {
            if(protector[0]==fighter.getMob().getTemplate().getId())
            {
              mobCapturable=false;
            }
          }
        }
      }
       if(this.getMapOld().getSong() != 1)
      if(mobCapturable&&!Capture.isInArenaMap(this.getMapOld().getId()))
      {
        boolean isFirst=true;
        int maxLvl=0;
        String stats="";

        for(Fighter fighter : loosers)
        {
          if(fighter.isInvocation()||fighter.getInvocator()!=null)
            continue;
          stats+=(isFirst ? "" : "|")+fighter.getMob().getTemplate().getId()+","+fighter.getLvl();
          isFirst=false;
          if(fighter.getLvl()>maxLvl)
            maxLvl=fighter.getLvl();
        }

        this.setFullSoul(new Capture(Database.getDynamics().getObjectData().getNextId(),1,7010,Constant.ITEM_POS_NO_EQUIPED,stats)); // Crï¿½e la pierre d'ï¿½me
        winners.stream().filter(F -> !F.isInvocation()&&F.haveState(Constant.ETAT_CAPT_AME)).forEach(F -> getCapturer().add(F));

        if(this.getCapturer().size()>0&&!Capture.isInArenaMap(this.getMapOld().getId())) // S'il y a des captureurs
        {
          for(int i=0;i<this.getCapturer().size();i++)
          {
            try
            {
              Fighter f=this.getCapturer().get(Formulas.getRandomValue(0,this.getCapturer().size()-1)); //Rï¿½cupï¿½re un captureur au hasard dans la liste
              if(f!=null&&f.getPersonnage()!=null)
              {
                if(f.getPersonnage().getObjetByPos(Constant.ITEM_POS_PIERRE_AME)==null||!(f.getPersonnage().getObjetByPos(Constant.ITEM_POS_PIERRE_AME).getTemplate().getType()==Constant.ITEM_TYPE_PIERRE_AME))
                {
                  this.getCapturer().remove(f);
                  continue;
                }
                Pair<Integer, Integer> playerSoulStone=Formulas.decompPierreAme(f.getPersonnage().getObjetByPos(Constant.ITEM_POS_PIERRE_AME));// Rï¿½cupï¿½re les stats de la pierre ï¿½quippï¿½

                if(playerSoulStone.getRight()<maxLvl)
                { //Si la pierre est trop faible
                  this.getCapturer().remove(f);
                  continue;
                }
                if(Formulas.getRandomValue(1,100)<=Formulas.totalCaptChance(playerSoulStone.getLeft(),f.getPersonnage()))
                { //Si le joueur obtiens la capture Retire la pierre vide au personnage et lui envoie ce changement
                  int emptySoulStone=f.getPersonnage().getObjetByPos(Constant.ITEM_POS_PIERRE_AME).getGuid();
                  f.getPersonnage().deleteItem(emptySoulStone);
                  SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(f.getPersonnage(),emptySoulStone);
                  this.setCaptWinner(f.getId());
                  break;
                }
              }
            }
            catch(NullPointerException e)
            {
              e.printStackTrace();
            }
          }
        }
      }
      /* Capture d'ï¿½mes **/

      /* Quest **/
      if(this.getType()==Constant.FIGHT_TYPE_PVM||this.getType()==Constant.FIGHT_TYPE_DOPEUL)
      {
        for(Fighter fighter : winners)
        {
          Player player=fighter.getPersonnage();
          if(player==null)
            continue;

          if(!player.getQuestPerso().isEmpty())
          {
            for(Fighter ennemy : loosers)
            {
              if(ennemy.getMob()==null)
                continue;
              if(ennemy.getMob().getTemplate()==null)
                continue;
              try
              {
              for(QuestPlayer questP : player.getQuestPerso().values())
              {
                if(questP==null)
                  continue;
                Quest quest=questP.getQuest();
                if(quest==null)
                  continue;
                quest.getQuestSteps().stream().filter(qEtape -> !questP.isQuestStepIsValidate(qEtape)&&(qEtape.getType()==0||qEtape.getType()==6)).filter(qEtape -> qEtape.getMonsterId()==ennemy.getMob().getTemplate().getId()).forEach(qEtape -> {
                  try
                  {
                    player.getQuestPersoByQuest(qEtape.getQuestData()).getMonsterKill().put(ennemy.getMob().getTemplate().getId(),(short)1);
                    qEtape.getQuestData().updateQuestData(player,false,2);
                  }
                  catch(Exception e)
                  {
                   // e.printStackTrace();
                    //player.sendMessage("Report to an admin : "+e.getMessage());
                  }
                });
              }
              
            }
            catch(Exception e)
            {
              //e.printStackTrace();
            }
            }
          }
        }
      }
      /* Apprivoisement **/
      boolean amande=false,rousse=false,doree=false;

      for(Fighter fighter : loosers)
      {
        try
        {
          if(fighter.getMob().getTemplate().getId()==171)
            amande=true;
          if(fighter.getMob().getTemplate().getId()==200)
            rousse=true;
          if(fighter.getMob().getTemplate().getId()==666)
            doree=true;
        }
        catch(Exception e)
        {
          amande=false;
          rousse=false;
          doree=false;
          break;
        }
      }
      if(amande||rousse||doree)
      {
        winners.stream().filter(fighter -> !fighter.isInvocation()&&fighter.haveState(Constant.ETAT_APPRIVOISEMENT)).forEach(F -> getTrainer().add(F));
        if(getTrainer().size()>0)
        {
          for(int i=0;i<getTrainer().size();i++)
          {
            try
            {
              Fighter f=getTrainer().get(Formulas.getRandomValue(0,getTrainer().size()-1)); // Rï¿½cupï¿½re un captureur au hasard dans la liste
              Player player=f.getPersonnage();
              if(player.getObjetByPos(Constant.ITEM_POS_ARME)==null||!(player.getObjetByPos(Constant.ITEM_POS_ARME).getTemplate().getType()==Constant.ITEM_TYPE_FILET_CAPTURE))
              {
                getTrainer().remove(f);
                continue;
              }
              int chance=Formulas.getRandomValue(1,100),appriChance=Formulas.totalAppriChance(amande,rousse,doree,player);
              if(chance<=appriChance)
              {
                // Retire le filet au personnage et lui envoie ce changement
                int filet=player.getObjetByPos(Constant.ITEM_POS_ARME).getGuid();
                player.deleteItem(filet);
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player,filet);
                setTrainerWinner(f.getId());
                break;
              }
            }
            catch(NullPointerException e)
            {
              e.printStackTrace();
            }
          }
        }
      }
      /** Apprivoisement **/
      int memberGuild=0;

      if(this.getType()==Constant.FIGHT_TYPE_PVT&&win==1)
        for(Fighter i : winners)
          if(i.getPersonnage()!=null)
            if(i.getPersonnage().getGuildMember()!=null)
              memberGuild++;

      int lvlLoosers=0,lvlWinners=0,lvlMaxLooser=0,lvlMax,lvlMin,challXp=0,challwine=0;
      byte nbbonus=0;
      for(Challenge c : getAllChallenges().values())
        if(c!=null&&c.getWin()) {
          challXp+=c.getXp();
          challwine++;
        }

      for(Fighter entry : loosers)
        lvlLoosers+=entry.getLvl();

      for(Fighter entry : winners)
      {
        lvlWinners+=entry.getLvl();
        if(entry.getLvl()>lvlMaxLooser&&entry.getPersonnage()!=null)
          lvlMaxLooser=entry.getLvl();
      }
      if(lvlLoosers>lvlWinners)
      {
        lvlMax=lvlLoosers;
        lvlMin=lvlWinners;
      }
      else
      {
        lvlMax=lvlWinners;
        lvlMin=lvlLoosers;
      }
      for(Fighter entry : winners)
        if(entry.getLvl()>(lvlMaxLooser/3)&&entry.getPersonnage()!=null)
          nbbonus+=1;

      if(lvlWinners<=0)
        lvlWinners=1;

      Map<Integer, Integer> mobs=new HashMap<>();
      loosers.stream().filter(mob -> mob.getMob()!=null).forEach(mob -> {
        if(mobs.get(mob.getMob().getTemplate().getId())!=null)
          mobs.put(mob.getMob().getTemplate().getId(),mobs.get(mob.getMob().getTemplate().getId())+1); // Quantite
        else
          mobs.put(mob.getMob().getTemplate().getId(),1);
      });

      Collections.sort(winners);
      Map<Integer, StringBuilder> gains=new HashMap<>();

      /**********************/
      /**       Drop       **/
      /**********************/
      int totalProspecting=0;
      float challengeFactor=0;
      float starFactor=this.getMobGroup()!=null ? getMobGroup().getStarBonus(getMobGroup().getInternalStarBonus()) : 0;

      //initialize PP variables
      for(Fighter fighter : winners)
        if(fighter!=null&&!fighter.isDouble())
          if(!fighter.isInvocation()||(fighter.getMob()!=null&&fighter.getMob().getTemplate()!=null&&fighter.getMob().getTemplate().getId()==285))
            totalProspecting+=fighter.getPros();
      if(totalProspecting < 100)
    	  totalProspecting = 100;
      
      //initialize challenge variable
      if(this.getType()==Constant.FIGHT_TYPE_PVM&&this.getAllChallenges().size()>0)
        for(Challenge challenge : this.getAllChallenges().values())
          if(challenge.getWin())
            challengeFactor+=challenge.getDrop();

      ArrayList<Drop> dropsPlayers=new ArrayList<>(),dropsMeats=new ArrayList<>();
      Collection<GameObject> dropsCollector=null;
      Pair<Integer, Integer> kamas;

      if(this.getType()==Constant.FIGHT_TYPE_PVT&&win==1)
      {
        int kamasCollector=(int)Math.ceil(collector.getKamas()/winners.size());
        kamas=new Pair<>(kamasCollector,kamasCollector);
        dropsCollector=this.getCollector().getDrops();
      }
      else
      {
        int minKamas=0,maxKamas=0;
        for(Fighter fighter : loosers)
        {
          if(!fighter.isInvocation()&&fighter.getMob()!=null&&!fighter.isDouble())
          {
            minKamas+=fighter.getMob().getTemplate().getMinKamas();
            maxKamas+=fighter.getMob().getTemplate().getMaxKamas();

            for(Drop drop1 : fighter.getMob().getTemplate().getDrops())
            {
              switch(drop1.getAction())
              {
                case 1:
                  Drop drop=drop1.copy(fighter.getMob().getGrade());
                  if(drop==null)
                    break;
                  dropsMeats.add(drop);
                  break;
                default:
                  if(drop1.getCeil()<=totalProspecting&&fighter.getMob()!=null)
                  {
                    drop=drop1.copy(fighter.getMob().getGrade());
                    if(drop==null)
                      break;
                    dropsPlayers.add(drop);
                  }
                  break;
              }
            }
          }
        }

        kamas=new Pair<>(minKamas,maxKamas);
      }

      // Sort fighter by prospecting.
      ArrayList<Fighter> temporary1=new ArrayList<>();
      Fighter higherFighter=null;
      while(temporary1.size()<winners.size())
      {
        int currentProspecting=-1;
        for(Fighter fighter : winners)
        {
          if(fighter.getMob()!=null&&fighter.getMob().getTemplate()!=null&&fighter.getMob().getTemplate().getId()==285&&fighter.getInvocator()!=null)
          {
            if(fighter.getInvocator().getTotalStats().getEffect(Constant.STATS_ADD_PROS)>currentProspecting&&!temporary1.contains(fighter))
            {
              higherFighter=fighter;
              currentProspecting=fighter.getInvocator().getTotalStats().getEffect(Constant.STATS_ADD_PROS);
            }
          }
          else if(fighter.getTotalStats().getEffect(Constant.STATS_ADD_PROS)>currentProspecting&&!temporary1.contains(fighter))
          {
            higherFighter=fighter;
            currentProspecting=fighter.getTotalStats().getEffect(Constant.STATS_ADD_PROS);
          }
        }
        temporary1.add(higherFighter);
      }

      winners.clear();
      winners.addAll(temporary1);
      final NumberFormat formatter=new DecimalFormat("#0.000");
      /**********************/
      /**     Fin Drop     **/
      /**********************/

      /** Stalk **/
      if(this.getType() == Constant.FIGHT_TYPE_KOLI) {
    	  for(Fighter fighter : winners)
          {
    		  if(fighter.getPersonnage() == null)
    			  continue;
    		  GameObject object=Main.world.getObjTemplate(10275).createNewItem(2,false);
              if(fighter.getPersonnage().addObjet(object,true))
                World.addGameObject(object,true);
              kamas=new Pair<>(1000*2,1000*2);
              fighter.getPersonnage().addKamas(100,true);
              Main.world.kamas_total += 100;
          }
      }
      Player curPlayer=null;
      boolean stalk=false;
      int quantity=2;
      if(this.getType()==Constant.FIGHT_TYPE_AGRESSION)
      {
      /*  int totalLoserRank=0;
        int losingPlayers=0;
        for(Fighter fighter : loosers)
          if(fighter.getPersonnage()!=null)
          {
            losingPlayers++;
            if(fighter.getPersonnage().get_align()!=0)
              totalLoserRank+=fighter.getPersonnage().getGrade();
          }

        int totalWinnerRank=0;
        int winningPlayers=0;
        for(Fighter fighter : winners)
          if(fighter.getPersonnage()!=null)
          {
            winningPlayers++;
            if(fighter.getPersonnage().get_align()!=0)
              totalWinnerRank+=fighter.getPersonnage().getGrade();
          }

        float averageLoserRank=totalLoserRank/losingPlayers;
        if(totalWinnerRank<=totalLoserRank&&averageLoserRank>=2)
        {
          int tokenReward=(int)Math.floor(4*totalLoserRank/winningPlayers);
         // for(Fighter f : winners)
          //  if(f.getPersonnage()!=null)
            //  f.getPersonnage().setNeededEndFightAction(new Action(475,Integer.toString(tokenReward),"",null)); //token reward
        }*/

        boolean isAlone=true;

        for(Fighter fighter : winners)
          if(!fighter.isInvocation())
            curPlayer=fighter.getPersonnage();

        for(Fighter fighter : winners)
          if(fighter.getPersonnage()!=curPlayer&&!fighter.isInvocation())
            isAlone=false;

        if(isAlone)
        {
          for(Fighter fighter : loosers)
          {
            if(!fighter.isInvocation()&&curPlayer!=null&&curPlayer.get_traque()!=null&&curPlayer.get_traque().getTraque()==fighter.getPersonnage())
            {
              SocketManager.GAME_SEND_MESSAGE(curPlayer,"Thomas Sacre : Le contrat a été exécuté, venez me voir pour réclamer votre récompense.","000000");
              curPlayer.get_traque().setTime(-2);
              stalk=true;
              fighter.setTraqued(true);

              Stalk stalkTarget=fighter.getPersonnage().get_traque();

              if(stalkTarget!=null)
                if(stalkTarget.getTraque()==curPlayer)
                  quantity=4;

              GameObject object=Main.world.getObjTemplate(10275).createNewItem(quantity,false);
              if(curPlayer.addObjet(object,true))
                World.addGameObject(object,true);
              kamas=new Pair<>(1000*quantity,1000*quantity);
              curPlayer.addKamas(1000*quantity,true);
              Main.world.kamas_total += 1000*quantity;
            }
          }
        }

        if(!stalk)
        {
          Player traqued=null;
          curPlayer=null;

          for(Fighter fighter : loosers)
            if(fighter.getPersonnage()!=null)
              if(fighter.getPersonnage().get_traque()!=null)
                traqued=fighter.getPersonnage().get_traque().getTraque();

          if(traqued!=null)
            for(Fighter fighter : winners)
              if(fighter.getPersonnage()==traqued)
                curPlayer=traqued;

          if(curPlayer!=null)
          {
            kamas=new Pair<>(1000*quantity,1000*quantity);
            curPlayer.addKamas(1000*quantity,true);
            Main.world.kamas_total += 1000*quantity;
            GameObject object=Main.world.getObjTemplate(10275).createNewItem(quantity,false);
            if(curPlayer.addObjet(object,true))
              World.addGameObject(object,true);
            stalk=true;
          }
        }
      }
      /** Stalk **/

      /** Heroic **/
      Map<Player, String> list=null;
      ArrayList<GameObject> objects=null;

     /* if(this.getType()==Constant.FIGHT_TYPE_CONQUETE)
      {
        int totalLoserRank=0;
        int losingPlayers=0;
        for(Fighter fighter : loosers)
          if(fighter.getPersonnage()!=null)
          {
            losingPlayers++;
            if(fighter.getPersonnage().get_align()!=0)
              totalLoserRank+=fighter.getPersonnage().getGrade();
          }

        int totalWinnerRank=0;
        int winningPlayers=0;
        for(Fighter fighter : winners)
          if(fighter.getPersonnage()!=null)
          {
            winningPlayers++;
            if(fighter.getPersonnage().get_align()!=0)
              totalWinnerRank+=fighter.getPersonnage().getGrade();
          }

        float averageLoserRank=totalLoserRank/losingPlayers;
        if(totalWinnerRank<=totalLoserRank&&averageLoserRank>=2)
        {
          int tokenReward=(int)Math.floor(4*totalLoserRank/winningPlayers);
          for(Fighter f : winners)
            if(f.getPersonnage()!=null)
             f.getPersonnage().setNeededEndFightAction(new Action(475,Integer.toString(tokenReward),"",null)); //token reward
        }
      }*/

      if(Config.getInstance().HEROIC && this.getMapOld().getSong() != 1)
      {
        switch(this.getType())
        {
          case Constant.FIGHT_TYPE_PVT:
          case Constant.FIGHT_TYPE_AGRESSION:
            final ArrayList<GameObject> objects1=new ArrayList<>();
            long xp = 0;
            for(final Fighter fighter : loosers)
            {
            	if(fighter.getPersonnage() == null)
            	continue;	
              final Player player=fighter.getPersonnage();
              if(player!=null) {
                objects1.addAll(player.getItems().values());
                xp =+ player.getExp(); 
              }
            }
            long xp_taux = xp/winners.size();
            for(final Fighter fighter : winners)
            {
            	if(fighter.getPersonnage() == null)
                	continue;		
              final Player player=fighter.getPersonnage();
              if(player!=null) {
                player.addXp(xp_taux);
              }
            }

            list=Fight.give(objects1,winners);
            break;
          case Constant.FIGHT_TYPE_PVM:
            try
            {
              final MobGroup group=this.getMobGroup();

              if(team)
              { // Players have loose the fight, mob win the fight
                objects=new ArrayList<>();
                for(final Fighter fighter : loosers)
                {
                  final Player player=fighter.getPersonnage();
                  if(player!=null)
                    objects.addAll(player.getItems().values());
                }

                if(group.isFix())
                {
                  String infos=this.getMapOld().getId()+","+group.getCellId();
                  if(RespawnGroup.fixMobGroupObjects.get(infos)!=null)
                  {
                    objects.addAll(RespawnGroup.fixMobGroupObjects.get(infos).getLeft());
                    RespawnGroup.fixMobGroupObjects.remove(infos);
                    RespawnGroup.fixMobGroupObjects.put(infos,new Pair<ArrayList<GameObject>, Integer>(objects,0));
                  }
                  else
                  {
                    RespawnGroup.fixMobGroupObjects.put(infos,new Pair<ArrayList<GameObject>, Integer>(objects,0));
                  //  Database.getDynamics().getHeroicMobsGroups().insertFix(this.getMapOld().getId(),group,objects);
                  }
                }
                else
                {
                  group.getObjects().addAll(objects);
                  this.getMapOld().respawnGroup(group);
                 // Database.getDynamics().getHeroicMobsGroups().insert(this.getMapOld().getId(),group,objects);
                }
              }
              else
              { // mob loose..
            	  if(group.isFix()) {
            	  if(RespawnGroup.fixMobGroupObjects.get(this.getMapOld().getId()+","+group.getCellId()) != null) {
            		  list=Fight.give(RespawnGroup.fixMobGroupObjects.get(this.getMapOld().getId()+","+group.getCellId()).getLeft(),winners);
            		  RespawnGroup.fixMobGroupObjects.get(this.getMapOld().getId()+","+group.getCellId()).left = new ArrayList<GameObject>();
            	  }
            	  }
                  if(!group.isFix()) {
                	  list=Fight.give(group.getObjects(),winners);  
            	  }
                 GameMap map=this.getMapOld();
                if(map.getId()!=8338&&map.getId()!=8340&&map.getId()!=8342&&map.getId()!=8344&&map.getId()!=8345&&map.getId()!=8347) //firefoux
                  if(!group.isFix())
                    this.getMapOld().spawnAfterTimeGroup(this.getMapOld().getMinRespawnTime(),this.getMapOld().getMaxRespawnTime()); 
              }
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
        }

      }
      else
      {
        if(this.getType()==Constant.FIGHT_TYPE_PVM)
        {
          try
          {
            final MobGroup group=this.getMobGroup();

            if(team) //players lost, respawn same group
            {
              this.getMapOld().respawnGroup(group);
            }
            else //players won, spawn new group
            {
              GameMap map=this.getMapOld();
              //if(group.isFix()) //players won against fixed mobgroup
               // this.getMapOld().spawnAfterTimeGroupFix(mobGroup.getSpawnCellId(),this.getMapOld().getMinRespawnTime(),this.getMapOld().getMaxRespawnTime());
              //else 
              if(!group.isFix()) {
            	  if(map.getId()!=8338&&map.getId()!=8340&&map.getId()!=8342&&map.getId()!=8344&&map.getId()!=8345&&map.getId()!=8347) //firefoux
                this.getMapOld().spawnAfterTimeGroup(this.getMapOld().getMinRespawnTime(),this.getMapOld().getMaxRespawnTime());
              }
            }
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        }
      }
      /** End heroic **/
      /** Winners **/
      if(winners.size()>=2)
      {
        try
        {
          Collections.sort(winners,new Comparator<Fighter>()
          {
            public int compare(Fighter f1, Fighter f2)
            {
              if(f1.getPersonnage()!=null&&f2.getPersonnage()==null) //1 exists 2 doesnt
                return Boolean.valueOf(f1.getPersonnage().ipDrop).compareTo(false);
              else if(f1.getPersonnage()==null&&f2.getPersonnage()!=null) //2 exists 1 doesnt
                return Boolean.valueOf(false).compareTo(Boolean.valueOf(f2.getPersonnage().ipDrop));
              else if(f1.getPersonnage()!=null&&f2.getPersonnage()!=null) //both exist
                return Boolean.valueOf(f1.getPersonnage().ipDrop).compareTo(Boolean.valueOf(f2.getPersonnage().ipDrop));
              return Boolean.valueOf(false).compareTo(false);
            }
          });
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }

      Map<Fighter, String> ipDroppers=new HashMap<Fighter, String>();
      for(Fighter i : winners)
      {
        if(i.isInvocation()&&i.getMob()!=null&&i.getMob().getTemplate().getId()!=285)
          continue;
        if(i.isDouble())
          continue;

        final Player player=i.getPersonnage();

        boolean add=true;
        if(player!=null&&player.ipDrop)
          for(Fighter ipDropper : ipDroppers.keySet())
            if(player.getAccount().getCurrentIp().compareTo(ipDropper.getPersonnage().getAccount().getCurrentIp())==0) //same IP of two IpDroppers, do not add
              add=false;
        if(add&&player!=null)
          ipDroppers.put(i,"");

        if(player!=null&&getType()!=Constant.FIGHT_TYPE_CHALLENGE)
          player.calculTurnCandy();
        if(getType()==Constant.FIGHT_TYPE_PVT||getType()==Constant.FIGHT_TYPE_PVM||getType()==Constant.FIGHT_TYPE_CHALLENGE
        		||getType()==Constant.FIGHT_TYPE_DOPEUL ||getType()==Constant.FIGHT_TYPE_KOLI)
        {
          String drops="";
          long xpPlayer=0,xpGuild=0,xpMount=0;
          int winKamas;

          AtomicReference<Long> XP=new AtomicReference<>();
           if (!this.STAFF) {
          /** Xp,kamas **/
          if(player!=null)
          {
            xpPlayer=Formulas.getXp(i,winners,totalXP,nbbonus,this.getMobGroup()!=null ? getMobGroup().getStarBonus(getMobGroup().getInternalStarBonus()) : 0,challXp,lvlMax,lvlMin,lvlLoosers,lvlWinners,Main.world.getConquestBonus(player));
            if(this.type == Constant.FIGHT_TYPE_PVM)
            {
            if(player.getAccount().getSubscribeRemaining() != 0L)xpPlayer = (long) (xpPlayer+(xpPlayer*0.25));
            if(MONO || DUO)xpPlayer = (long) (xpPlayer+(xpPlayer*0.15));
            }
            XP.set(xpPlayer);
            World.get_Succes(player.getId()).chall_add(player, challwine);
            if(this.getType()==Constant.FIGHT_TYPE_PVT&&win==1)
            {
              if(memberGuild!=0)
                if(player.getGuildMember()!=null)
                  xpGuild=(int)Math.floor(this.getCollector().getXp()/memberGuild);
              xpGuild =  xpGuild/2;
             // if(Main.world.getGuild(this.getCollector().getGuildId()).getLvl()>=20)
             //   if(this.getCollector().getPodsTotal()>=100)
              //    player.setNeededEndFightAction(new Action(475,"20","",null)); //20 token reward
            }
            else
              xpGuild=(Formulas.getGuildXpWin(i,XP)/2);

            if(player.isOnMount())
            {
              xpMount=Formulas.getMountXpWin(i,XP);
              player.getMount().addXp(xpMount*10);
              SocketManager.GAME_SEND_Re_PACKET(player,"+",player.getMount());
            }
          }
        	   
           }else
           {
        	   XP.set((long)0); 
           }
          winKamas=(int)((this.getType()==Constant.FIGHT_TYPE_PVT&&win==1) ? Math.floor(kamas.getLeft()/winners.size()) : Formulas.getKamasWin(i,winners,kamas.getLeft(),kamas.getRight()));
          if (this.STAFF)winKamas = 0;
          /** Xp,kamas **/
          /**********************/
          /**       Drop       **/
          /**********************/
          Map<Integer, Integer> objectsWon=new HashMap<>(),itemWon2=new HashMap<>();
          if (!this.STAFF) {
          if(this.getType()==Constant.FIGHT_TYPE_PVT&&win==1&&dropsCollector!=null)
          {
            int objectPerPlayer=(int)Math.floor(dropsCollector.size()/winners.size()),counter=0;
            ArrayList<GameObject> temporary2=new ArrayList<>(dropsCollector);
            Collections.shuffle(temporary2);

            for(GameObject object : temporary2)
            {
              if(counter<=objectPerPlayer)
              {
                objectsWon.put(object.getTemplate().getId(),object.getQuantity());
                dropsCollector.remove(object);
                Main.world.removeGameObject(object.getGuid());
                counter++;
              }
            }
          }
          else
          {
            ArrayList<Drop> temporary3=new ArrayList<>(dropsPlayers);
            temporary3.addAll(Main.world.getEtherealWeapons(i.isInvocation() ? i.getInvocator().getLvl() : i.getLvl()).stream().map(objectTemplate -> new Drop(objectTemplate.getId(),0.001,0)).collect(Collectors.toList()));
            Collections.shuffle(temporary3);

            for(Drop drop : temporary3)
            {
              int charPros=i.getPros();
              if(charPros<1)
                charPros=1;
              if(this.type == Constant.FIGHT_TYPE_PVM)
              {
              if(player!=null&&player.getAccount().getSubscribeRemaining() != 0L)charPros = (int) (charPros+(charPros*0.25));
              if(MONO || DUO)charPros = (int) (charPros+(charPros*0.15));
              }
              float soloBonus=Formulas.getSoloBonus(winners);

              final double jet=Double.parseDouble(formatter.format(Math.random()*100).replace(',','.')),
                  chance=Formulas.dropChance((float)drop.getLocalPercent(),starFactor,challengeFactor,(float)Main.world.getConquestBonus(player),(float)charPros,soloBonus);
              boolean ok=false;

              switch(drop.getAction())
              {
                case 4:
                  if(player!=null&&ConditionParser.validConditions(player,"QE="+drop.getCondition()))
                    ok=true;
                  break;
              }
              if(jet<chance||ok) //item gained
              {
                ObjectTemplate objectTemplate=Main.world.getObjTemplate(drop.getObjectId());

                if(objectTemplate==null)
                  continue;
                int max_drop = drop.getMaxDrop();
                /*if((objectTemplate.getType() >= 1 && objectTemplate.getType() <= 11) || (objectTemplate.getType() >= 16 && objectTemplate.getType() <= 23)
						|| (objectTemplate.getType() >= 81 && objectTemplate.getType() <= 82) || objectTemplate.getType() == 85
						|| objectTemplate.getType() == 113 || objectTemplate.getType() == 84 || objectTemplate.getType() == 100 || objectTemplate.getType() == 73
						|| objectTemplate.getType() == 13 || objectTemplate.getType() == 41){	
				}else
					if(Config.getInstance().HEROIC) {
						max_drop = max_drop*3;	
					}
					else {
						max_drop = max_drop*Config.getInstance().rateDrop;	
					}*/
					
                quantity=drop.getMinDrop()+(int)Math.rint(Math.random()*(max_drop-drop.getMinDrop()));
                boolean itsOk=false,unique=false;
                
                if(objectTemplate.getType() == 23 || objectTemplate.getId() == 12324 || objectTemplate.getId() == 101620) {
            
                if(this.DROP_ONE_TIME)
                continue;	
                this.DROP_ONE_TIME = true;	
                }
                switch(drop.getAction())
                {
                  case -2:
                    itsOk=true;
                    break;
                  case -1:// All items without condition.
                    itsOk=true;
                    break;

                  case 1:// Is meat so..
                    break;

                  case 2:// Verification of the condition (MAP)
                    for(String id : drop.getCondition().split(","))
                      if(id.equals(String.valueOf(getMap().getId())))
                        itsOk=true;
                    break;

                  case 3:// Alignement
                    if(this.getMapOld().getSubArea()==null)
                      break;
                    switch(drop.getCondition())
                    {
                      case "0":
                        if(this.getMapOld().getSubArea().getAlignement()==2)
                          itsOk=true;
                        break;
                      case "1":
                        if(this.getMapOld().getSubArea().getAlignement()==1)
                          itsOk=true;
                        break;
                      case "2":
                        if(this.getMapOld().getSubArea().getAlignement()==2)
                          itsOk=true;
                        break;
                      case "3":
                        if(this.getMapOld().getSubArea().getAlignement()==3)
                          itsOk=true;
                        break;
                      default:
                        itsOk=true;
                        break;
                    }
                    break;

                  case 4: // Quete
                    if(ConditionParser.validConditions(player,"QE="+drop.getCondition()))
                      itsOk=true;
                    break;

                  case 5: // Dropable une seule fois
                    if(player==null)
                      break;
                    if(player.getNbItemTemplate(objectTemplate.getId())>0)
                      break;
                    itsOk=true;
                    break;

                  case 6: // Avoir l'objet
                    if(player==null)
                      break;
                    int item=Integer.parseInt(drop.getCondition());
                    if(item==2039)
                    {
                      if(this.getMap().getId()==(short)7388)
                      {
                        if(player.hasItemTemplate(item,1))
                          itsOk=true;
                      }
                      else
                        itsOk=false;
                    }
                    else if(player.hasItemTemplate(item,1))
                      itsOk=true;
                    break;

                  case 7:// Verification of the condition (MAP) mais pas plusieurs fois
                    if(player==null)
                      break;
                    if(player.hasItemTemplate(objectTemplate.getId(),1))
                      break;
                    for(String id : drop.getCondition().split(","))
                    {
                      if(id.equals(String.valueOf(this.getMap().getId())))
                      {
                        itsOk=true;
                      }
                    }
                    break;

                  case 8:// Win a specific quantity
                    String[] split=drop.getCondition().split(",");
                    quantity=Formulas.getRandomValue(Integer.parseInt(split[0]),Integer.parseInt(split[1]));
                    itsOk=true;
                    break;

 

                  case 10: //subarea requirement
                    if(ConditionParser.validConditions(player,"PB="+drop.getCondition()))
                      itsOk=true;
                    break;
                  case 11: //random
                      int random = Formulas.getRandomValue(1,3);
                      if(random == 1)
                        itsOk=true;
                      break;

                  case 999:// Drop for collector
                    itsOk=true;
                    break;

                  default:
                    itsOk=true;
                    break;
                }
                if(itsOk)
                {
                  objectsWon.put(objectTemplate.getId(),(objectsWon.get(objectTemplate.getId())==null ? quantity : (objectsWon.get(objectTemplate.getId()))+quantity));
                  if(unique)
                    dropsPlayers.remove(drop);
                }
              }
            }
            /** Drop Chasseur **/
            if(player!=null)
            {
              ArrayList<Drop> temporary=new ArrayList<>(dropsMeats);
              Collections.shuffle(temporary);

              GameObject weapon=player.getObjetByPos(Constant.ITEM_POS_ARME);
              boolean ok=weapon!=null&&weapon.getStats().getEffect(795)==1;

              if(ok)
              {
                for(Drop drop : temporary)
                {
                  final double jet=Double.parseDouble(formatter.format(Math.random()*100).replace(',','.')),
                      chance=Double.parseDouble(formatter.format(drop.getLocalPercent()*(i.getPros()/100.0)).replace(',','.'));

                  if(jet<chance)
                  {
                    ObjectTemplate objectTemplate=Main.world.getObjTemplate(drop.getObjectId());

                    if(drop.getAction()==1&&objectTemplate!=null&&player.getMetierByID(41)!=null&&player.getMetierByID(41).get_lvl()>=drop.getLevel())
                      itemWon2.put(objectTemplate.getId(),(itemWon2.get(objectTemplate.getId())==null ? 0 : itemWon2.get(objectTemplate.getId()))+5);
                  }
                }
              }
            }
          }
        
        }
          if(player!=null||(i.getMob()!=null&&i.getMob().getTemplate().getId()==285))
          {
            if(player!=null)
            {
            	if(this.getTrainerWinner()!=-1&&i.getId()==this.getTrainerWinner()&&player.getMount()==null)
              {
            	  if(amande || rousse || doree) {
            		
            		  ArrayList<Integer>  lista = new ArrayList<Integer>();
                      if(amande)
                      	 lista.add(20);
                      if(rousse)
                      	 lista.add(10);
                      if(doree)
                      	 lista.add(18);

                      Random r = new Random();
                 int  color = lista.get(r.nextInt(lista.size()));
                Mount mount=new Mount(color,i.getId(),true);
                player.setMount(mount);
                SocketManager.GAME_SEND_Re_PACKET(player,"+",mount);
                SocketManager.GAME_SEND_Rx_PACKET(player);
                SocketManager.GAME_SEND_STATS_PACKET(player);
                if(drops.length()>0)
                  drops+=",";
                switch(color)
                {
                  case 20:
                    drops+="7807~1";
                    break;
                  case 10:
                    drops+="7809~1";
                    break;
                  case 18:
                    drops+="7864~1";
                    break;
                }
              }
              }
              if(i.getId()==this.getCaptWinner()&&this.getFullSoul()!=null)
              {
                if(drops.length()>0)
                  drops+=",";
                drops+=this.getFullSoul().getTemplate().getId()+"~"+1;
                if(player.addObjet(this.getFullSoul(),false))
                  World.addGameObject(this.getFullSoul(),true);
              }
              if(list!=null)
              {
                String value=list.get(i.getPersonnage());
                if(value!=null&&!value.isEmpty())
                  drops+=(drops.isEmpty() ? "" : ",")+value;
              }
            }

            for(Entry<Integer, Integer> entry : objectsWon.entrySet())
            {
              ObjectTemplate objectTemplate=Main.world.getObjTemplate(entry.getKey());

              if(player==null&&i.getInvocator()==null)
                break;
              if(objectTemplate==null||i.isDouble())
                continue;

              Fighter target=i.getPersonnage()==null ? i.getInvocator() : i;
              Player playerTarget=target.getPersonnage();
              
              if(playerTarget != null) {
              if(objectTemplate.getType() == 23) {
              SocketManager.GAME_SEND_Im_PACKET_TO_ALL("116;"+"<b>Server</b>"+"~ON dirait que la chance sourit é ["+playerTarget.getName()+"] Il vient de drop ["+objectTemplate.getName()+"]");
              }
              if(objectTemplate.getId() == 12324 || objectTemplate.getId() == 101620) {
                  SocketManager.GAME_SEND_Im_PACKET_TO_ALL("116;"+"<b>Server</b>"+"~ON dirait que la chance sourit é ["+playerTarget.getName()+"] Il vient de drop ["+objectTemplate.getName()+"]");
              }
              }
              if(objectTemplate.getType()!=24) //not a quest item
              {
                //v2.8 - ipdrop system
                for(Player p : getPlayersSameIP(playerTarget,winners))
                {
                  if(p.ipDrop==true&&playerTarget!=p)
                  {
                    playerTarget=p;
                    break;
                  }
                }

                //v2.9 - nodrop system
                if(playerTarget.getCanDrop()==false)
                  continue;
                if(playerTarget.isCanDrop_items() == false) {
                	if((objectTemplate.getType() >= 1 && objectTemplate.getType() <= 11) || (objectTemplate.getType() >= 16 && objectTemplate.getType() <= 23)
							|| (objectTemplate.getType() >= 81 && objectTemplate.getType() <= 82) || objectTemplate.getType() == 85
							|| objectTemplate.getType() == 113){
						continue;
					}	
                }
                if(playerTarget.isCanDrop_ressources() == false) {
                    boolean okok = false;
                	if((objectTemplate.getType() >= 1 && objectTemplate.getType() <= 11) || (objectTemplate.getType() >= 16 && objectTemplate.getType() <= 23)
							|| (objectTemplate.getType() >= 81 && objectTemplate.getType() <= 82) || objectTemplate.getType() == 85
							|| objectTemplate.getType() == 113){
						okok = true;
					}						      
					       if(okok){
		                
		                  if(drops.length()>0)
		                    drops+=",";
		                  drops+=entry.getKey()+"~"+entry.getValue();
		                }else
					       continue;
                }else 
                
                {
                
                  if(drops.length()>0)
                    drops+=",";
                  drops+=entry.getKey()+"~"+entry.getValue();
                }
                
              }
              else
              {
                if(drops.length()>0)
                  drops+=",";
                drops+=entry.getKey()+"~"+entry.getValue();
              }

              if(objectTemplate.getType()==32&&player!=null)
              {
                player.setMascotte(entry.getKey());
              }
              else
              {
                GameObject newObj=null;

                if(objectTemplate.getType()==Constant.ITEM_TYPE_FAMILIER)
                {
                  newObj=objectTemplate.createNewItem(entry.getValue(),false);
                  if(playerTarget.addObjet(newObj,true))
                    World.addGameObject(newObj,true);
                }
                else
                {
                  newObj=objectTemplate.createNewItemWithoutDuplication(playerTarget.getItems().values(),entry.getValue(),false);
                  if(objectTemplate.getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
                  {
                    //obj.setMountStats(this.getPlayer(), null);
                    Mount mount=new Mount(Constant.getMountColorByParchoTemplate(newObj.getTemplate().getId()),playerTarget.getId(),false);
                    newObj.clearStats();
                    newObj.getStats().addOneStat(995,-(mount.getId()));
                    newObj.getTxtStat().put(996,playerTarget.getName());
                    newObj.getTxtStat().put(997,mount.getName());
                  }
                  int guid=newObj.getGuid();
                  if(guid==-1)
                  { // Don't exist
                    guid=newObj.setId();
                    playerTarget.getItems().put(guid,newObj);
                    SocketManager.GAME_SEND_OAKO_PACKET(playerTarget,newObj);
                    World.addGameObject(newObj,true);
                  }
                  else
                  {
                    GameObject object=playerTarget.getItems().get(guid);

                    if(object!=null)
                    {
                      object.setQuantity(object.getQuantity()+entry.getValue(),null);
                      SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(playerTarget,object);
                    }
                  }
                }
              }
            }

            for(Entry<Integer, Integer> entry : itemWon2.entrySet())
            {
              ObjectTemplate objectTemplate=Main.world.getObjTemplate(entry.getKey());

              if(player==null&&i.getInvocator().getPersonnage()==null)
                break;
              if(objectTemplate==null)
                continue;
              Player target=player!=null ? player : i.getInvocator().getPersonnage();
              if(objectTemplate.getType()!=24) //not a quest item
              {
                //v2.8 - ipdrop system
                for(Player p : getPlayersSameIP(target,winners))
                {
                  if(p.ipDrop==true&&target!=p)
                  {
                    target=p;
                    break;
                  }
                }

                //v2.9 - nodrop system
                if(target.getCanDrop()==false)
                  continue;
                if(target.isCanDrop_items() == false) {
                	if((objectTemplate.getType() >= 1 && objectTemplate.getType() <= 11) || (objectTemplate.getType() >= 16 && objectTemplate.getType() <= 23)
							|| (objectTemplate.getType() >= 81 && objectTemplate.getType() <= 82) || objectTemplate.getType() == 85
							|| objectTemplate.getType() == 113){
						continue;
					}	
                }
                if(target.isCanDrop_ressources() == false) {
                    boolean okok = false;
                	if((objectTemplate.getType() >= 1 && objectTemplate.getType() <= 11) || (objectTemplate.getType() >= 16 && objectTemplate.getType() <= 23)
							|| (objectTemplate.getType() >= 81 && objectTemplate.getType() <= 82) || objectTemplate.getType() == 85
							|| objectTemplate.getType() == 113){
						okok = true;
					}						      
					       if(okok){
		                
		                  if(drops.length()>0)
		                    drops+=",";
		                  drops+=entry.getKey()+"~"+entry.getValue();
		                }else
					       continue;
                }else 
                
                {
                
                  if(drops.length()>0)
                    drops+=",";
                  drops+=entry.getKey()+"~"+entry.getValue();
                }
                
              }
              else
              {
                if(drops.length()>0)
                  drops+=",";
                drops+=entry.getKey()+"~"+entry.getValue();
              }

              GameObject newObj=Main.world.getObjTemplate(objectTemplate.getId()).createNewItemWithoutDuplication(target.getItems().values(),entry.getValue(),false);
              int guid=newObj.getGuid(); //FIXME: Ne pas recrÃ©e un item pour l'empiler aprÃ¨s
              if(objectTemplate.getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
              {
                //obj.setMountStats(this.getPlayer(), null);
                Mount mount=new Mount(Constant.getMountColorByParchoTemplate(newObj.getTemplate().getId()),target.getId(),false);
                newObj.clearStats();
                newObj.getStats().addOneStat(995,-(mount.getId()));
                newObj.getTxtStat().put(996,target.getName());
                newObj.getTxtStat().put(997,mount.getName());
              }
              if(guid==-1)
              {
                guid=newObj.setId();
                target.getItems().put(guid,newObj);
                SocketManager.GAME_SEND_OAKO_PACKET(target,newObj);
                World.addGameObject(newObj,true);
              }
              else
              {
                GameObject object=target.getItems().get(guid);
                if(object!=null)
                {
                  object.setQuantity(object.getQuantity()+entry.getValue(),null);
                  SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(target,object);
                }
              }
            }
            if(this.getType()==Constant.FIGHT_TYPE_DOPEUL)
            {
              for(Fighter F : loosers)
              {
                MobGrade mob=F.getMob();
                Monster m=mob.getTemplate();
                if(m==null)
                  continue;
                int IDmob=m.getId();
                if(drops.length()>0)
                  drops+=",";
                drops+=Constant.getCertificatByDopeuls(IDmob)+"~1";
                // Certificat :
                if(player != null) {
                ObjectTemplate OT2=Main.world.getObjTemplate(Constant.getCertificatByDopeuls(IDmob));
                if(OT2!=null)
                {
                  GameObject obj2=OT2.createNewItem(1,false);
                  if(player.addObjet(obj2,true))// Si le joueur n'avait pas d'item similaire
                    World.addGameObject(obj2,true);
                  obj2.refreshStatsObjet("325#0#0#"+System.currentTimeMillis());
                  //Database.getStatics().getPlayerData().update(player);
                  SocketManager.GAME_SEND_Ow_PACKET(player);
                }
                }
              }
            }
            if(this.getType() == Constant.FIGHT_TYPE_KOLI) {
            	drops+="10275~2";
            }
            if(this.getType()==Constant.FIGHT_TYPE_PVM&&player!=null)
            {
            	/*
              int bouftou=0,tofu=0;

              for(MobGrade mob : getMobGroup().getMobs().values())
              {
                switch(mob.getTemplate().getId())
                {
                  case 793:
                    bouftou++;
                    break;
                  case 794:
                    tofu++;
                    break;
                  case 289:
                    //if(player.getCurMap().getSubArea().getId()==211)
                    //  MobGroup.MAITRE_CORBAC.repop(player.getCurMap().getId());
                    break;
                }
              }
             
              if(Config.getInstance().HALLOWEEN)
              {
                if((bouftou>0||tofu>0)&&!player.hasEquiped(976))
                {
                  if(bouftou>tofu)
                  {
                    drops+=(drops.length()>0 ? "," : "")+"8169~1";
                    player.setMalediction(8169);
                    player.setFullMorph(Formulas.getRandomValue(16,20),false,false);
                  }
                  else if(tofu>bouftou)
                  {
                    drops+=(drops.length()>0 ? "," : "")+"8170~1";
                    player.setMalediction(8170);
                    player.setFullMorph(Formulas.getRandomValue(21,25),false,false);
                  }
                  else
                  {
                    switch(Formulas.getRandomValue(1,2))
                    {
                      case 1:
                        drops+=(drops.length()>0 ? "," : "")+"8169~1";
                        player.setMalediction(8169);
                        player.setFullMorph(Formulas.getRandomValue(16,20),false,false);
                        break;
                      case 2:
                        drops+=(drops.length()>0 ? "," : "")+"8170~1";
                        player.setMalediction(8170);
                        player.setFullMorph(Formulas.getRandomValue(21,25),false,false);
                        break;
                    }
                  }
                }
              }*/

              switch(player.getCurMap().getId())
              {
                case 8984:
                  GameObject obj=Main.world.getObjTemplate(8012).createNewItem(1,false);
                  if(Main.world.getObjTemplate(8012).getType()==Constant.ITEM_TYPE_CERTIF_MONTURE)
                  {
                    //obj.setMountStats(this.getPlayer(), null);
                    Mount mount=new Mount(Constant.getMountColorByParchoTemplate(obj.getTemplate().getId()),player.getId(),false);
                    obj.clearStats();
                    obj.getStats().addOneStat(995,-(mount.getId()));
                    obj.getTxtStat().put(996,player.getName());
                    obj.getTxtStat().put(997,mount.getName());
                  }
                  if(player.addObjet(obj,true))
                    World.addGameObject(obj,true);
                  drops+=(drops.length()>0 ? "," : "")+"8012~1";
                  break;
              }
            }
            /**********************/
            /**     Fin Drop     **/
            /**********************/

            if(player!=null)
            {
              xpPlayer=XP.get();
              if(xpPlayer!=0)
              {
                if(player.getMorphMode())
                {
                  GameObject obj=player.getObjetByPos(Constant.ITEM_POS_ARME);
                  if(obj!=null)
                    if(Constant.isIncarnationWeapon(obj.getTemplate().getId()))
                      if(player.addXpIncarnations(xpPlayer))
                        i.setLevelUp(true);
                }
                else if(player.addXp(xpPlayer))
                  i.setLevelUp(true);
              }
              
              if(winKamas!=0) {
                player.addKamas(winKamas,true);
              }
              if(xpGuild>0&&player.getGuildMember()!=null)
                player.getGuildMember().giveXpToGuild(xpGuild);
            }
            if(winKamas!=0&&i.isInvocation()&&!i.isDouble()&&i.getInvocator().getPersonnage()!=null)
              i.getInvocator().getPersonnage().addKamas(winKamas,true);
            Main.world.kamas_total += winKamas;
          }

          //v2.8 - ipdrop system
          Fighter target=i;
          if(player!=null)
          {
            for(Fighter fighter : winners)
            {
              if(fighter.getPersonnage()!=null)
                if(fighter.getPersonnage().ipDrop&&fighter.getPersonnage()!=player&&fighter.getPersonnage().getAccount().getCurrentIp().equals(player.getAccount().getCurrentIp()))
                {
                  target=fighter;
                  break;
                }
            }
          }

          StringBuilder p=new StringBuilder();
          p.append("2;");
          p.append(i.getId()).append(";");
          p.append(i.getPacketsName()).append(";");
          p.append(i.getLvl()).append(";");
          p.append((i.isDead() ? "1" : "0")).append(";");
          p.append(i.xpString(";")).append(";");
          p.append((xpPlayer==0 ? "" : xpPlayer)).append(";");
          p.append((xpGuild==0 ? "" : xpGuild)).append(";");
          p.append((xpMount==0 ? "" : xpMount)).append(";");

          if(target.getPersonnage()!=null)
          {
            if(target.getPersonnage()==i.getPersonnage()&&i.getPersonnage().ipDrop) //take ipDrops
            {
              if(!drops.isEmpty())
              {
                if(i.getPersonnage()!=null)
                  if(i.getPersonnage().getCanDrop()==true)
                    p.append(stackDrops(drops,ipDroppers.get(i)));
                p.append(";");
              }
              else
              {
                if(i.getPersonnage()!=null)
                  if(i.getPersonnage().getCanDrop()==true)
                    p.append(ipDroppers.get(i));
                p.append(";");
              }
            }
            else if(target!=i) //give ipDrops away person
            {
              p.append(";");
              if(ipDroppers.get(target)==null)
                ipDroppers.put(target,drops);
              else
              {
                StringBuilder temp=new StringBuilder();
                temp.append(stackDrops(drops,ipDroppers.get(target)));
                ipDroppers.put(target,temp.toString());
              }
            }
            else //no ipDrop enabled
            {
              if(i.getPersonnage()!=null)
              {
                if(i.getPersonnage().getCanDrop()==true)
                  p.append(drops);
                p.append(";");
              }
              else
                p.append(drops).append(";");
            }
          }
          else if(target!=i) //give ipDrops away summon (living chest)
          {
            p.append(";");
            if(ipDroppers.get(i)==null)
              ipDroppers.put(i,drops);
            else
            {
              StringBuilder temp=new StringBuilder();
              temp.append(stackDrops(drops,ipDroppers.get(i)));
              ipDroppers.put(i,temp.toString());
            }
          }
          else
            p.append(drops).append(";");

          p.append((winKamas==0 ? "" : winKamas)).append("|");

          gains.put(i.getId(),p);
        }
        else
        {
          if(this.getType()==Constant.FIGHT_TYPE_KOLI)
          {
            if(i.isInvocation()||i.isPrisme()||i.isMob()||i.isDouble())
              continue;

            StringBuilder temporary=new StringBuilder();
            temporary.append("2;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
            temporary.append(0).append(";");
            temporary.append(0).append(";");
            temporary.append(0).append(";");
            temporary.append(0).append(";");
            temporary.append(0).append(";");
            temporary.append(0).append(";");
            temporary.append(0);
            temporary.append(";");
            temporary.append("10275~2");
            temporary.append(";").append(Formulas.getRandomValue(kamas.getLeft(),kamas.getRight())).append(";0;0;0;0|");
            temporary.append(";;0;0;0;0;0|");
            gains.put(i.getId(),temporary);
          }
          // Si c'est un neutre, on ne gagne pas de points
          int winH=0,winD=0;

          if(this.getType()==Constant.FIGHT_TYPE_AGRESSION)
          {
            if(i.isInvocation()||i.isPrisme()||i.isMob()||i.isDouble())
              continue;

            if(this.getType()==Constant.FIGHT_TYPE_AGRESSION)
            {
              if(getInit1().getPersonnage().get_align()!=0&&getInit0().getPersonnage().get_align()!=0)
              {
                if(getInit1().getPersonnage().getAccount().getCurrentIp().compareTo(getInit0().getPersonnage().getAccount().getCurrentIp())!=0)
                  winH=Formulas.calculHonorWin(winners,loosers,i);
                if(player.getDeshonor()>0)
                  winD=-1;
              }
            }
            else if(this.getType()==Constant.FIGHT_TYPE_CONQUETE)
              winH=Formulas.calculHonorWin(winners,loosers,i);

            if(player.get_align()!=0)
            {
              if(player.get_honor()+winH<0)
                winH=-player.get_honor();
              player.addHonor(winH);
              player.setDeshonor(player.getDeshonor()+winD);
            }

            int maxHonor=Main.world.getExpLevel(player.getGrade()+1).pvp;
            if(maxHonor==-1)
              maxHonor=Main.world.getExpLevel(player.getGrade()).pvp;

            StringBuilder temporary=new StringBuilder();
            temporary.append("2;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
            temporary.append(player.get_align()!=Constant.ALIGNEMENT_NEUTRE ? Main.world.getExpLevel(player.getGrade()).pvp : 0).append(";");
            temporary.append(player.get_honor()).append(";");
            temporary.append(player.get_align()!=Constant.ALIGNEMENT_NEUTRE ? maxHonor : 0).append(";");
            temporary.append(winH).append(";");
            temporary.append(player.getGrade()).append(";");
            temporary.append(player.getDeshonor()).append(";");
            temporary.append(winD);
            temporary.append(";");
            temporary.append(stalk ? "10275~"+quantity : "");
            if(Config.getInstance().HEROIC&&list!=null)
            {
              String value;
              if((value=list.get(player))!=null)
                if(!value.isEmpty())
                  temporary.append(stalk ? "," : "").append(value);
            }
            temporary.append(";").append(Formulas.getRandomValue(kamas.getLeft(),kamas.getRight())).append(";0;0;0;0|");
            temporary.append(";;0;0;0;0;0|");
            gains.put(i.getId(),temporary);
          }
          else if(this.getType()==Constant.FIGHT_TYPE_CONQUETE)
          {
            if(player!=null)
            {
              if(player.get_honor()+winH<0)
                winH=-player.get_honor();
              player.addHonor(winH);
              if(player.getDeshonor()-winD<0)
                winD=0;
              player.setDeshonor(player.getDeshonor()-winD);
              int maxHonor=Main.world.getExpLevel(player.getGrade()+1).pvp;
              if(maxHonor==-1)
                maxHonor=Main.world.getExpLevel(player.getGrade()).pvp;

              StringBuilder temporary=new StringBuilder();
              temporary.append("2;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
              temporary.append(player.get_align()!=Constant.ALIGNEMENT_NEUTRE ? Main.world.getExpLevel(player.getGrade()).pvp : 0).append(";");
              temporary.append(player.get_honor()).append(";");
              temporary.append(player.get_align()!=Constant.ALIGNEMENT_NEUTRE ? maxHonor : 0).append(";");
              temporary.append(winH).append(";");
              temporary.append(player.getGrade()).append(";");
              temporary.append(player.getDeshonor()).append(";");
              temporary.append(winD);
              temporary.append(";;0;0;0;0;0|");
              gains.put(i.getId(),temporary);
            }
            else
            {
              final Prism prism=i.getPrism();
              winH=winH*5;
              if(prism.getHonor()+winH<0)
                winH=-prism.getHonor();
              winH*=3;
              prism.addHonor(winH);

              int maxHonor=Main.world.getExpLevel(prism.getLevel()+1).pvp;
              if(maxHonor==-1)
                maxHonor=Main.world.getExpLevel(prism.getLevel()).pvp;

              StringBuilder temporary=new StringBuilder();
              temporary.append("2;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
              temporary.append(Main.world.getExpLevel(prism.getLevel()).pvp).append(";");
              temporary.append(prism.getHonor()).append(";");
              temporary.append(maxHonor).append(";");
              temporary.append(winH).append(";");
              temporary.append(prism.getLevel()).append(";");
              temporary.append("0;0;;0;0;0;0;0|");

              gains.put(i.getId(),temporary);
            }
          }
        }
      }

      Collections.shuffle(winners);
      Map<Integer, Integer> invoks=new HashMap<>();

      winners.stream().filter(i -> i.isInvocation()&&i.getMob()!=null).filter(i -> i.getMob().getTemplate().getId()==285).forEach(i -> invoks.put(i.getId(),i.getInvocator().getId()));

      if(invoks!=null&&invoks.size()>0)
        for(Entry<Integer, Integer> entry : invoks.entrySet())
          winners=this.deplace(winners,entry.getValue(),entry.getKey());

      winners.stream().filter(fighter -> !(fighter.isInvocation()&&fighter.getMob()!=null&&fighter.getMob().getTemplate().getId()!=285)).filter(fighter -> !fighter.isDouble()&&gains.get(fighter.getId())!=null).forEach(fighter -> packet.append(gains.get(fighter.getId()).toString()));

      /** End winner **/

      /** Looser **/
      for(Fighter i : loosers)
      {
        if(i.isInvocation()&&i.getMob()!=null&&i.getMob().getTemplate().getId()!=285)
          continue;
        if(i.isDouble())
          continue;

        final Player player=i.getPersonnage();

        if(player!=null&&this.getType()!=Constant.FIGHT_TYPE_CHALLENGE)
          player.calculTurnCandy();
        if(this.getType()!=Constant.FIGHT_TYPE_AGRESSION&&this.getType()!=Constant.FIGHT_TYPE_CONQUETE)
        {
          StringBuilder temporary=new StringBuilder();
          if(i.getPdv()==0||i.hasLeft()||i.isDead())
            temporary.append("0;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";1").append(";").append(i.xpString(";")).append(";;;;|");
          else
            temporary.append("0;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";0").append(";").append(i.xpString(";")).append(";;;;|");
          packet.append(temporary);
        }
        else
        {
          // Si c'est un neutre, on ne gagne pas de points
          int winH=0;
          int winD=0;
          if(this.getType()==Constant.FIGHT_TYPE_AGRESSION)
          {
            if(getInit1().getPersonnage().get_align()!=0&&getInit0().getPersonnage().get_align()!=0)
              if(getInit1().getPersonnage().getAccount().getCurrentIp().compareTo(getInit0().getPersonnage().getAccount().getCurrentIp())!=0)
                winH=Formulas.calculHonorWin(winners,loosers,i);

            if(player==null)
              continue;
            if(player.get_align()!=0)
            {
            	int max = player.get_honor()+winH<0 ? -player.get_honor() : -winH;
            	if(max > 250)
            		max = 250;
              player.remHonor(max);
              if(player.getDeshonor()-winD<0)
                winD=0;
              player.setDeshonor(player.getDeshonor()-winD);
            }

            int maxHonor=Main.world.getExpLevel(player.getGrade()+1).pvp;
            if(maxHonor==-1)
              maxHonor=Main.world.getExpLevel(player.getGrade()).pvp;

            packet.append("0;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
            packet.append(player.get_align()!=Constant.ALIGNEMENT_NEUTRE ? Main.world.getExpLevel(player.getGrade()).pvp : 0).append(";");
            packet.append(player.get_honor()).append(";");
            packet.append(player.get_align()!=Constant.ALIGNEMENT_NEUTRE ? maxHonor : 0).append(";");
            packet.append(winH).append(";");
            packet.append(player.getGrade()).append(";");
            packet.append(player.getDeshonor()).append(";");
            packet.append(winD);
            packet.append(";;0;0;0;0;0|");
          }
            if(this.getType()==Constant.FIGHT_TYPE_CONQUETE)
          {
            winH=Formulas.calculHonorWin(winners,loosers,i);

            if(player!=null)
            {
              winH=0;
              if(player.getDeshonor()-winD<0)
                winD=0;
              int maxHonor=Main.world.getExpLevel(player.getGrade()+1).pvp;
              if(maxHonor==-1)
                maxHonor=Main.world.getExpLevel(player.getGrade()).pvp;

              player.setDeshonor(player.getDeshonor()-winD);
              packet.append("0;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
              packet.append(player.get_align()!=Constant.ALIGNEMENT_NEUTRE ? Main.world.getExpLevel(player.getGrade()).pvp : 0).append(";");
              packet.append(player.get_honor()).append(";");
              packet.append(player.get_align()!=Constant.ALIGNEMENT_NEUTRE ? maxHonor : 0).append(";");
              packet.append(winH).append(";");
              packet.append(player.getGrade()).append(";");
              packet.append(player.getDeshonor()).append(";");
              packet.append(winD);
              packet.append(";;0;0;0;0;0|");
            }
            else
            {
              Prism prism=i.getPrism();

              if(prism.getHonor()+winH<0)
                winH=-prism.getHonor();
              int maxHonor=Main.world.getExpLevel(prism.getLevel()+1).pvp;
              if(maxHonor==-1)
                maxHonor=Main.world.getExpLevel(prism.getLevel()).pvp;

              prism.addHonor(winH);
              packet.append("0;").append(i.getId()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0")).append(";");
              packet.append(Main.world.getExpLevel(prism.getLevel()).pvp).append(";");
              packet.append(prism.getHonor()).append(";");
              packet.append(maxHonor).append(";");
              packet.append(winH).append(";");
              packet.append(prism.getLevel()).append(";");
              packet.append("0;0;;0;0;0;0;0|");
            }
          }
        }
      }
      /** End Looser **/
      if(!this.STAFF)
      if(Collector.getCollectorByMapId(getMap().getId())!=null&&getType()==Constant.FIGHT_TYPE_PVM)
      {
        Collector collector=Collector.getCollectorByMapId(getMap().getId());

        long winxp=Formulas.getXp(collector,winners,totalXP,nbbonus,(getMobGroup()!=null ? getMobGroup().getStarBonus(getMobGroup().getInternalStarBonus()) : 0),challXp,lvlMax,lvlMin,lvlLoosers,lvlWinners,0)/15;
        long winkamas=(int)Math.floor(Formulas.getKamasWinPerco(kamas.getLeft(),kamas.getRight()));

        collector.setXp(collector.getXp()+winxp);
        collector.setKamas(collector.getKamas()+winkamas);
        Guild guild=Main.world.getGuild(collector.getGuildId());

        packet.append("5;").append(collector.getId()).append(";").append(collector.getFullName()).append(";").append(Main.world.getGuild(collector.getGuildId()).getLvl()).append(";0;");
        packet.append(guild.getLvl()).append(";");
        packet.append(guild.getXp()).append(";");
        packet.append(Main.world.getGuildXpMax(guild.getLvl())).append(";");
        packet.append(";");// XpGagner
        packet.append(winxp).append(";");// XpGuilde
        packet.append(";");// Monture

        String drops="";
        ArrayList<Drop> temporary=new ArrayList<>(dropsPlayers);
        Collections.shuffle(temporary);
        Map<Integer, Integer> objectsWon=new HashMap<>();

        if(collector.getPodsTotal()<collector.getMaxPod())
        {
          for(Drop drop : temporary)
          {
            final double jet=Double.parseDouble(formatter.format(Math.random()*100).replace(',','.')),
                chance=(int)(drop.getLocalPercent()*(Main.world.getGuild(collector.getGuildId()).getStats(Constant.STATS_ADD_PROS)/100.0));

            if(jet<chance)
            {
              ObjectTemplate objectTemplate=Main.world.getObjTemplate(drop.getObjectId());

              if(objectTemplate==null)
                continue;

              boolean itsOk=false,unique=false;
              switch(drop.getAction())
              {
                case -2:
                  unique=true;
                  itsOk=true;
                  break;
                case -1:// All items without condition.
                  itsOk=true;
                  break;

                case 1:// Is meat so..
                  break;

                case 2:// Verification of the condition ( MAP )
                  for(String id : drop.getCondition().split(","))
                    if(id.equals(getMap().getId()+""))
                      itsOk=true;
                  break;

                case 3:// Alignement
                  if(this.getMapOld().getSubArea()==null)
                    break;
                  switch(drop.getCondition())
                  {
                    case "0":
                      if(this.getMapOld().getSubArea().getAlignement()==2)
                        itsOk=true;
                      break;
                    case "1":
                      if(this.getMapOld().getSubArea().getAlignement()==1)
                        itsOk=true;
                      break;
                    case "2":
                      if(this.getMapOld().getSubArea().getAlignement()==2)
                        itsOk=true;
                      break;
                    case "3":
                      if(this.getMapOld().getSubArea().getAlignement()==3)
                        itsOk=true;
                      break;

                    default:
                      itsOk=true;
                      break;
                  }
                  break;

                case 4:
                  if(objectTemplate.getId()==2553)//Gros boulet
                    itsOk=true;
                  break;

                case 5:
                  itsOk=false;
                  break;

                case 6: // Les percepteurs ne font pas de quï¿½tes
                case 7:
                  break;

                default:
                  itsOk=true;
                  break;
              }

              if(itsOk)
              {
                objectsWon.put(objectTemplate.getId(),(objectsWon.get(objectTemplate.getId())==null ? 0 : objectsWon.get(objectTemplate.getId()))+1);

                if(unique)
                  dropsPlayers.remove(drop);
              }
            }
          }

          for(Entry<Integer, Integer> entry : objectsWon.entrySet())
          {
            ObjectTemplate objectTemplate=Main.world.getObjTemplate(entry.getKey());

            if(objectTemplate==null||collector.getPodsTotal()+objectTemplate.getPod()*entry.getValue()>=collector.getMaxPod())
              continue;
            if(drops.length()>0)
              drops+=",";

            drops+=entry.getKey()+"~"+entry.getValue();

            GameObject newObj=Main.world.getObjTemplate(objectTemplate.getId()).createNewItemWithoutDuplication(collector.getOjects().values(),entry.getValue(),false);
            int guid=newObj.getGuid();//FIXME: Ne pas recrÃ©e un item pour l'empiler aprÃ¨s

            if(guid==-1)
            { // Don't exist
              guid=newObj.setId();
              collector.getOjects().put(guid,newObj);
              World.addGameObject(newObj,true);
            }
            else
            {
              newObj.setQuantity(newObj.getQuantity()+entry.getValue(),null);
            }
          }
        }
        packet.append(drops).append(";");// Drop
        packet.append(winkamas).append("|");

        Database.getDynamics().getCollectorData().update(collector);
      }
      return packet.toString();
    }
    catch(Exception e)
    {
      e.printStackTrace();
      System.out.println("An error occurred when server went to give the 'GE' packet : "+e.getMessage()+" "+e.getLocalizedMessage());
    }
    return "";
  }

  ArrayList<Fighter> deplace(ArrayList<Fighter> TEAM1, Integer Invocator, Integer Invocation)
  {
    int k=0;
    int p=0;
    int j=0;
    int s=TEAM1.size()-1;
    boolean b=true;
    Fighter invok=null;
    for(Fighter i : TEAM1)
    {
      if(i.getId()==Invocation)
      {
        invok=i;
        b=false;
      }
      if(!b&&invok!=i)
      {
        TEAM1.set((k-1),i);
      }
      k++;
    }
    TEAM1.set(s,invok);
    k=0;
    b=true;
    for(Fighter i : TEAM1)
    {
      if(i.getId()==Invocator)
      {
        p=k;
        b=false;
      }
      if(!b&&i.getId()!=Invocator)
      {
        j++;
        if(k<s)
          TEAM1.set((s-j+1),TEAM1.get(s-j));
      }
      k++;
    }
    TEAM1.set(p+1,invok);
    return TEAM1;
  }

  //v2.7 - Replaced String += with StringBuilder
  public String getGTL()
  {
    StringBuilder packet=new StringBuilder("GTL");
    try
    {
    if(this.orderPlaying!=null)
      for(Fighter f : this.orderPlaying)
        if(f != null&&!f.isDead())
          packet.append("|"+f.getId());
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
    }
    return packet.toString()+(char)0x00;
  }

  //v2.3 - Fight Duration Daylight Savings Time fix
  public String parseFightInfos()
  {
    StringBuilder infos=new StringBuilder();
    infos.append(getId()).append(";");
    long time=startTime+TimeZone.getDefault().getRawOffset()+TimeZone.getDefault().getDSTSavings();
    infos.append((getStartTime()==0 ? "-1" : time)).append(";");
    // Team1
    infos.append("0,");// 0 car toujours joueur :)
    switch(getType())
    {
      case Constant.FIGHT_TYPE_CHALLENGE:
        infos.append("0,");
        infos.append(this.getTeamSizeWithoutInvocation(this.getTeam0().values())).append(";");
        // Team2
        infos.append("0,");
        infos.append("0,");
        infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
        break;
      case Constant.FIGHT_TYPE_KOLI:
          infos.append("0,");
          infos.append(this.getTeamSizeWithoutInvocation(this.getTeam0().values())).append(";");
          // Team2
          infos.append("0,");
          infos.append("0,");
          infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
          break;

      case Constant.FIGHT_TYPE_AGRESSION:
        infos.append(getInit0().getPersonnage().get_align()).append(",");
        infos.append(getTeam0().size()).append(";");
        // Team2
        infos.append("0,");
        infos.append(getInit1().getPersonnage().get_align()).append(",");
        infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
        break;

      case Constant.FIGHT_TYPE_CONQUETE:
        infos.append(getInit0().getPersonnage().get_align()).append(",");
        infos.append(this.getTeamSizeWithoutInvocation(this.getTeam0().values())).append(";");
        // Team2
        infos.append("0,");
        infos.append(getPrism().getAlignement()).append(",");
        infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
        break;

      case Constant.FIGHT_TYPE_PVM:
        infos.append("0,");
        infos.append(this.getTeamSizeWithoutInvocation(this.getTeam0().values())).append(";");
        // Team2
        infos.append("1,");
        if(getTeam0().isEmpty())
          infos.append("0,");
        else
          infos.append(getTeam1().get(getTeam1().keySet().toArray()[0]).getMob().getTemplate().getAlign()).append(",");
        infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
        break;

      case Constant.FIGHT_TYPE_DOPEUL:
        infos.append("0,");
        infos.append(this.getTeamSizeWithoutInvocation(this.getTeam0().values())).append(";");
        // Team2
        infos.append("1,");
        if(getTeam0().isEmpty())
          infos.append("0,");
        else
          infos.append(getTeam1().get(getTeam1().keySet().toArray()[0]).getMob().getTemplate().getAlign()).append(",");
        infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
        break;

      case Constant.FIGHT_TYPE_PVT:
        infos.append("0,");
        infos.append(this.getTeamSizeWithoutInvocation(this.getTeam0().values())).append(";");
        // Team2
        infos.append("4,");
        infos.append("0,");
        infos.append(this.getTeamSizeWithoutInvocation(this.getTeam1().values())).append(";");
        break;
    }
    return infos.toString();
  }

  int getTeamSizeWithoutInvocation(Collection<Fighter> fighters)
  {
    int i=0;
    for(Fighter fighter : fighters)
      if(!fighter.isInvocation())
        i++;
    return i;
  }

  public Fighter getFighterByOrdreJeu()
  {
    if(this.orderPlaying==null)
      return null;
    if(this.curPlayer>=this.orderPlaying.size())
      this.curPlayer=this.orderPlaying.size()-1;
    if(this.curPlayer<0)
      this.curPlayer=0;
    if(this.orderPlaying.size()<=0)
      return null;
    Fighter current=null;
    try
    {
      current=this.orderPlaying.get(this.curPlayer);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return current;
  }

  int getOrderPlayingSize()
  {
    if(this.orderPlaying==null)
      return 0;
    if(this.orderPlaying.size()<=0)
      return 0;
    return this.orderPlaying.size();
  }

  boolean haveFighterInOrdreJeu(Fighter f)
  {
    return this.orderPlaying!=null&&f!=null&&this.orderPlaying.contains(f);
  }

  public List<Fighter> getOrderPlaying()
  {
    return this.orderPlaying;
  }

  public void cast(Fighter fighter, Runnable runnable)
  {
    if(this.turn!=null&&System.currentTimeMillis()-this.turn.getStartTime()>=40000)
    {
      return;
    }
    SocketManager.GAME_SEND_GAS_PACKET_TO_FIGHT(this,7,fighter.getId());
    try
    {
      runnable.run();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    SocketManager.GAME_SEND_GAF_PACKET_TO_FIGHT(this,7,0,fighter.getId());
  }

  public static Map<Player, String> give(ArrayList<GameObject> objects, ArrayList<Fighter> winners)
  {
    final Map<Player, String> list=new HashMap<>();

    if(Config.getInstance().HEROIC )
    {
      final ArrayList<Player> players=new ArrayList<>();

      new ArrayList<>(winners).stream().filter(fighter -> fighter!=null).forEach(fighter -> {
        final Player player=fighter.getPersonnage();

        if(player!=null)
        {
          players.add(player);
          list.put(player,"");
        }
      });

      if(players.size()>0&&objects!=null&&!objects.isEmpty())
      {
        byte count=-1;
        GameObject object;

        Iterator<GameObject> iterator=objects.iterator();
        while(iterator.hasNext())
        {
          object=iterator.next();

          if(object==null || object.getTemplate().getType() == 24 || object.getTemplate().getId() == 25540
        		  || object.getTemplate().getId() == 25541 || object.getTemplate().getId() == 25542 || object.getTemplate().getId() == 25543
        		  || object.getTemplate().getId() == 25593 || object.getTemplate().getId() == 25594 || object.getTemplate().getId() == 25595
        		  || object.getTemplate().getId() == 22007 || object.getTemplate().getId() == 22007)
          {
            continue;
          }

          count++;
          final Player player=players.get(count);

          if(player!=null)
          {
            object.setPosition(Constant.ITEM_POS_NO_EQUIPED);
            player.addObjet(object,true);
            String value=list.get(player);
            value+=(value.isEmpty() ? "" : ",")+object.getTemplate().getId()+"~"+object.getQuantity();
            list.remove(player);
            list.put(player,value);
          }
          if(count>=players.size()-1)
            count=-1;

        }
      }
    }
    return list;
  }

  //v2.8 - ipDrop System
  private String stackDrops(String a, String b)
  {
    if(a.isEmpty())
      return b;
    else if(b.isEmpty())
      return a;
    ArrayList<String> aSplit=new ArrayList<String>(Arrays.asList(a.split(",")));
    ArrayList<String> bSplit=new ArrayList<String>(Arrays.asList(b.split(",")));
    StringBuilder doneString=new StringBuilder();
    for(String aString : aSplit)
    {
      int aId=Integer.parseInt(aString.split("~")[0]);
      int aQua=Integer.parseInt(aString.split("~")[1]);
      Iterator<String> bSplitIterator=bSplit.iterator();
      while(bSplitIterator.hasNext())
      {
        String bString=bSplitIterator.next();
        int bId=Integer.parseInt(bString.split("~")[0]);
        if(aId==bId)
        {
          aQua+=Integer.parseInt(bString.split("~")[1]);
          bSplitIterator.remove();
        }
      }
      doneString.append(aId+"~"+aQua+",");
    }
    for(String bString : bSplit)
    {
      int bId=Integer.parseInt(bString.split("~")[0]);
      int bQua=Integer.parseInt(bString.split("~")[1]);
      doneString.append(bId+"~"+bQua+",");
    }
    if(doneString.charAt(doneString.length()-1)==',')
      doneString.deleteCharAt(doneString.length()-1);
    return doneString.toString();
  }

  //v2.8 - same IP player list for .ipdrop command
  public static ArrayList<Player> getPlayersSameIP(Player source, ArrayList<Fighter> fighters)
  {
    ArrayList<Player> sameIpPlayers=new ArrayList<>();
    final List<Player> online=new ArrayList<Player>();
    for(Fighter f : fighters)
    {
      if(f.getPersonnage()==null)
      {
        continue;
      }
      if(!f.getPersonnage().isOnline()||f.getPersonnage().getGameClient()==null)
      {
        continue;
      }
      online.add(f.getPersonnage());
    }
    for(Player player : online)
    {
      if(player.getAccount()!=null&&player.getAccount().getCurrentIp()!=null)
        if(player.getAccount().getCurrentIp().compareTo(source.getAccount().getCurrentIp())==0)
          sameIpPlayers.add(player);
    }
    return sameIpPlayers;
  }
}
