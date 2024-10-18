package soufix.game;

import ch.qos.logback.classic.Level;
import java.util.concurrent.ConcurrentHashMap;
import ch.qos.logback.classic.Logger;
import soufix.Hdv.Hdv;
import soufix.Hdv.HdvEntry;
import soufix.area.Area;
import soufix.area.SubArea;
import soufix.area.map.GameMap;
import soufix.area.map.entity.Animation;
import soufix.area.map.entity.House;
import soufix.area.map.entity.MountPark;
import soufix.area.map.entity.Trunk;
import soufix.area.map.entity.Tutorial;
import soufix.area.map.entity.InteractiveObject.InteractiveObjectTemplate;
import soufix.client.Account;
import soufix.client.Player;
import soufix.client.other.Stats;
import soufix.common.CryptManager;
import soufix.common.Formulas;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.Collector;
import soufix.entity.Prism;
import soufix.entity.monster.Monster;
import soufix.entity.mount.Mount;
import soufix.entity.npc.NpcAnswer;
import soufix.entity.npc.NpcQuestion;
import soufix.entity.npc.NpcTemplate;
import soufix.entity.pet.Pet;
import soufix.entity.pet.PetEntry;
import soufix.fight.spells.Spell;
import soufix.guild.Guild;
import soufix.job.Job;
import soufix.job.fm.Potion;
import soufix.job.fm.Rune;
import soufix.main.Boutique;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.main.Tokenshop;
import soufix.object.GameObject;
import soufix.object.ObjectSet;
import soufix.object.ObjectTemplate;
import soufix.object.entity.Capture;
import soufix.object.entity.Fragment;
import soufix.other.tienda.TiendaCategoria;
import soufix.other.tienda.TiendaObjetos;
import soufix.quest.QuestPlayer;
import soufix.utility.Pair;
import soufix.utility.Sort;
import soufix.other.tienda.*;
import org.slf4j.LoggerFactory;
import soufix.client.other.Ornements;
import soufix.client.other.Shortcuts;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class World
{
  public Logger logger=(Logger)LoggerFactory.getLogger(World.class);

  private Map<Integer, Account> accounts=new HashMap<>();
  private Map<Integer, Player> players=new ConcurrentHashMap<>();
  private Map<Short, GameMap> maps=new HashMap<>();
  private static Map<Integer, GameObject> objects=new ConcurrentHashMap<>();
  private Map<Integer, ExpLevel> experiences=new HashMap<>();
  private Map<Integer, Spell> spells=new HashMap<>();
  private static Map<Integer, ObjectTemplate> ObjTemplates=new HashMap<>();
  private Map<Integer, Monster> MobTemplates=new HashMap<>();
  public Map<Integer, NpcTemplate> npcsTemplate=new HashMap<>();
  private Map<Integer, NpcQuestion> questions=new HashMap<>();
  private Map<Integer, NpcAnswer> answers=new HashMap<>();
  private Map<Integer, InteractiveObjectTemplate> IOTemplate=new HashMap<>();
  private Map<Integer, Mount> Dragodindes=new HashMap<>();
  private Map<Integer, Area> areas=new HashMap<>();
  private Map<Integer, SubArea> subAreas=new HashMap<>();
  private Map<Integer, Job> Jobs=new HashMap<>();
  private Map<Integer, ArrayList<Pair<Integer, Integer>>> Crafts=new HashMap<>();
  private Map<Integer, ObjectSet> ItemSets=new HashMap<>();
  private Map<Integer, Guild> Guildes=new HashMap<>();
  private Map<Integer, Hdv> Hdvs=new HashMap<>();
  private Map<Integer, Map<Integer, CopyOnWriteArrayList<HdvEntry>>> hdvsItems=new ConcurrentHashMap<>();
  private Map<Integer, Animation> Animations=new HashMap<>();
  private Map<Short, MountPark> MountPark=new HashMap<>();
  private Map<Integer, Trunk> Trunks=new HashMap<>();
  private Map<Integer, Collector> collectors=new HashMap<>();
  private Map<Integer, House> Houses=new HashMap<>();
  private Map<Short, Collection<Integer>> Seller=new HashMap<>();
  private StringBuilder Challenges=new StringBuilder();
  private Map<Integer, Prism> Prismes=new HashMap<>();
  private Map<Integer, Map<String, String>> fullmorphs=new HashMap<>();
  private Map<Integer, Pet> Pets=new HashMap<>();
  private Map<Integer, PetEntry> PetsEntry=new HashMap<>();
  private Map<String, Map<String, String>> mobsGroupsFix=new HashMap<>();
  private Map<String, Map<String, String>> randomMobsGroupsFix=new HashMap<>();
  public Map<Integer, Map<String, Map<String, Integer>>> extraMonstre=new HashMap<>();
  public Map<Integer, GameMap> extraMonstreOnMap=new HashMap<>();
  private Map<Integer, Tutorial> Tutorial=new HashMap<>();
  private int nextObjectHdvId, nextLineHdvId;
  private CryptManager cryptManager=new CryptManager();
  public ArrayList<String> maps_dj = new ArrayList<String>();
  public CopyOnWriteArrayList<String> fille_att = new CopyOnWriteArrayList<String>();
  public static Map<Integer, soufix.other.Bourse_kamas> Bourse_kamas = Collections.synchronizedMap(new HashMap<Integer, soufix.other.Bourse_kamas>());
  public static Map<Integer, soufix.other.Titre> Titre = Collections.synchronizedMap(new HashMap<Integer, soufix.other.Titre>());
  public static Map<Integer, soufix.other.Succes> Succes = Collections.synchronizedMap(new HashMap<Integer, soufix.other.Succes>());
  public static Map<Integer, soufix.other.Succes_data> Succes_data = Collections.synchronizedMap(new HashMap<Integer, soufix.other.Succes_data>());
  public String IP_ALLOW = "";
  public String IP_BANNI = "";
  public String archi = "";
  public String Succes_packet = "";
  public List<Player> Koli_players;
  private static CopyOnWriteArrayList<Player> _LADDER_NIVEL = new CopyOnWriteArrayList<>();
  private static CopyOnWriteArrayList<Player> _LADDER_Succes = new CopyOnWriteArrayList<>();
  private static CopyOnWriteArrayList<Guild> _LADDER_GREMIO = new CopyOnWriteArrayList<>();
  private static CopyOnWriteArrayList<Player> _LADDER_PVP = new CopyOnWriteArrayList<>();
  private static CopyOnWriteArrayList<Player> _LADDER_KOLI = new CopyOnWriteArrayList<>();
  private static Map<Integer, Ornements> Ornements = new TreeMap<Integer, Ornements>();
  private Map<Player, List<Shortcuts>> shortcuts = new HashMap<>();
  public long kamas_total = 0;
  public long kamas_total_start = 0;
	private Map<Integer, TiendaCategoria> shopCategoria = new HashMap<>();
	public static Map<Integer, TiendaCategoria> TiendaCategoria = Collections
			.synchronizedMap(new HashMap<Integer, TiendaCategoria>());

	private Map<Integer, TiendaObjetos> shopObjetos = new HashMap<>();
	public static Map<Integer, TiendaObjetos> tiendaObjetos = Collections
			.synchronizedMap(new HashMap<Integer, TiendaObjetos>());

	public void addTiendaCategoria(TiendaCategoria shopCategoria) {
		this.shopCategoria.put(shopCategoria.getId(), shopCategoria);
	}

	public TiendaCategoria getTiendaCategoria(int shopCategoria) {
		return this.shopCategoria.get(shopCategoria);
	}

	public void addTiendaObjetos(TiendaObjetos shopCategoria) {
		this.shopObjetos.put(shopCategoria.getId(), shopCategoria);
	}

	public TiendaObjetos getTiendaObjetos(int shopCategoria) {
		return this.shopObjetos.get(shopCategoria);
	}
  public CryptManager getCryptManager()
  {
    return cryptManager;
  }


  //region Accounts data
  public void addAccount(Account account)
  {
    accounts.put(account.getId(),account);
  }
 

  public Account getAccount(int id)
  {
	//if(accounts.get(id) == null)Database.getStatics().getAccountData().loadv2(id); 
    return accounts.get(id);
  }
  public  boolean addKoli_players(Player player)
  {
    if(this.Koli_players==null)
      this.Koli_players=new ArrayList<>();
    if(!this.Koli_players.contains(player)) {
      this.Koli_players.add(player);
      return true;
    }
    return false;
  }

  public  boolean removeKoli_players(Player player)
  {
    if(this.players!=null)
    {
    	if(this.Koli_players != null)
    	 if(!this.Koli_players.isEmpty())
      if(this.Koli_players.contains(player))
      {
        this.Koli_players.remove(player);
        return true;
      }
    	if(this.Koli_players != null)
      if(this.Koli_players.isEmpty())
        this.Koli_players=null;
    	return false;
    }
    return false;
  }

  public List<Player> getKoli_players()
  {
    if(this.Koli_players==null)
      return new ArrayList<>();
    return Koli_players;
  }

  public Collection<Account> getAccounts()
  {
    return accounts.values();
  }

  public Map<Integer, Account> getAccountsByIp(String ip)
  {
    Map<Integer, Account> newAccounts=new HashMap<>();
    accounts.values().stream().filter(account -> account.getLastIP().equalsIgnoreCase(ip)).forEach(account -> newAccounts.put(newAccounts.size(),account));
    return newAccounts;
  }

  public Account getAccountByPseudo(String pseudo)
  {
    for(Account account : accounts.values())
      if(account.getPseudo().equals(pseudo))
        return account;
    return null;
  }
  //endregion

  //region Players data
  public Collection<Player> getPlayers()
  {
	  try {
    return players.values();
	  }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	      return null;
	    }
	
  }

  public void addPlayer(Player player)
  {
	  if(this.getPlayer(player.getId()) != null)
		  return;
    players.put(player.getId(),player);
  }

  public Player getPlayerByName(String name)
  {
    for(Player player : players.values())
      if(player.getName().
    		  equalsIgnoreCase(name))
        return player;
    return null;
  }
    public Player getPlayer(int id)
  {
    return players.get(id);
  }


  public  List<Player> getOnlinePlayers()
  {
    final List<Player> online=new ArrayList<Player>();
    for(final Map.Entry<Integer, Player> perso : Main.world.players.entrySet())
    {
      if(perso.getValue()==null)
        continue;
      if(!perso.getValue().isOnline()||perso.getValue().getGameClient()==null)
        continue;
      online.add(perso.getValue());
    }
    return online;
  }

  //v2.6 - max connection bugfix
  public int getOnlinePlayerCountSameIP(GameClient source)
  {
    int i=0;

    final List<Player> online=new ArrayList<Player>();
    for(final Map.Entry<Integer, Player> perso : Main.world.players.entrySet())
    {
      if(perso.getValue()==null)
      {
        continue;
      }
      if(!perso.getValue().isOnline()||perso.getValue().getGameClient()==null)
      {
        continue;
      }
      if(perso.getValue().getGameClient()==null)
      {
        continue;
      }
      online.add(perso.getValue());
    }
    for(Player player : online)
    {
      if(player.getAccount()!=null&&player.getAccount().getCurrentIp()!=null)
        if(player.getAccount().getCurrentIp().compareTo(source.getAccount().getCurrentIp())==0)
          i++;
    }

    return i;
  }

  //v2.8 - same IP player list for .ipdrop command
  public ArrayList<Player> getOnlinePlayersSameIP(GameClient source)
  {
    ArrayList<Player> sameIpPlayers=new ArrayList<>();

    final List<Player> online=new ArrayList<Player>();
    for(final Map.Entry<Integer, Player> perso : Main.world.players.entrySet())
    {
      if(perso.getValue()==null)
      {
        continue;
      }
      if(!perso.getValue().isOnline()||perso.getValue().getGameClient()==null)
      {
        continue;
      }
      online.add(perso.getValue());
    }
    for(Player player : online)
    {
      if(player.getAccount()!=null&&player.getAccount().getCurrentIp()!=null)
        if(player.getAccount().getCurrentIp().compareTo(source.getAccount().getCurrentIp())==0)
          sameIpPlayers.add(player);
    }
    return sameIpPlayers;
  }

  //endregion

  //region Maps data
  public Collection<GameMap> getMaps()
  {
    return maps.values();
  }

  public GameMap getMap(short id)
  {

    return maps.get(id);
  }

  public void addMap(GameMap map)
  {
    if(map.getSubArea()!=null&&map.getSubArea().getArea().getId()==42&&!Config.getInstance().NOEL)
      return;
    maps.put(map.getId(),map);
  }

  public CopyOnWriteArrayList<GameObject> getGameObjects()
  {
    return new CopyOnWriteArrayList<>(objects.values());
  }

  public static void addGameObject(GameObject gameObject, boolean saveSQL)
  {
    if(gameObject!=null)
    {
      objects.put(gameObject.getGuid(),gameObject);
      if(saveSQL)
        gameObject.modification=0;
    }
  }

  public static GameObject getGameObject(int guid)
  {
   return objects.get(guid);
  }

  public void removeGameObject(int id)
  {
    if(objects.containsKey(id))
      objects.remove(id);
    Database.getDynamics().getObjectData().delete(id);
  }
  //endregion

  public Map<Integer, Spell> getSpells()
  {
    return spells;
  }

  public Map<Integer, ObjectTemplate> getObjectsTemplates()
  {
    return ObjTemplates;
  }

  public Map<Integer, NpcAnswer> getAnswers()
  {
    return answers;
  }

  public Map<Integer, Mount> getMounts()
  {
    return Dragodindes;
  }

  public Map<Integer, Area> getAreas()
  {
    return areas;
  }

  public Map<Integer, SubArea> getSubAreas()
  {
    return subAreas;
  }

  public Map<Integer, Guild> getGuilds()
  {
    return Guildes;
  }

  public Map<Short, MountPark> getMountparks()
  {
    return MountPark;
  }

  public Map<Integer, Trunk> getTrunks()
  {
    return Trunks;
  }

  public Map<Integer, Collector> getCollectors()
  {
    return collectors;
  }

  public Map<Integer, House> getHouses()
  {
    return Houses;
  }

  public Map<Integer, Prism> getPrisms()
  {
    return Prismes;
  }

  public void setPrisms(Map<Integer, Prism> prisms)
  {
    this.Prismes=prisms;
  }

  public Map<Integer, Map<String, Map<String, Integer>>> getExtraMonsters()
  {
    return extraMonstre;
  }
  /**
   * end region *
   */

  //v2.8 - Correct gameServer state
  public void createWorld()
  {
    logger.info("Loading of data..");
    long time=System.currentTimeMillis();

    Database.getStatics().getServerData().loggedZero();
    logger.debug("The reset of the logged players were done successfully.");
    
    logger.debug("Chargement des Succes "+Database.getStatics().getSuccesData().load_succes()+".");
    logger.debug("Chargement des Succes Data "+Database.getDynamics().getTitreData().load_succes_data()+".");
    
    Database.getStatics().getCommandData().load(null);
    logger.debug("The administration commands were loaded successfully.");

    Database.getStatics().getGroupData().load(null);
    logger.debug("The administration groups were loaded successfully.");

    Database.getStatics().getPubData().load(null);
    logger.debug("The pubs were loaded successfully.");

    Database.getDynamics().getFullMorphData().load();
    logger.debug("The incarnations were loaded successfully.");

    Database.getDynamics().getExtraMonsterData().load();
    logger.debug("The extra-monsters were loaded successfully.");

    Database.getDynamics().getExperienceData().load();
    logger.debug("The experiences were loaded successfully.");

    Database.getDynamics().getSpellData().load();
    logger.debug("The spells were loaded successfully.");

    Database.getDynamics().getMonsterData().load();
    logger.debug("The monsters were loaded successfully.");

    Database.getDynamics().getObjectTemplateData().load();
    logger.debug("The template objects were loaded successfully.");

    Database.getDynamics().getObjectData().load();
    logger.debug("The objects were loaded successfully.");

    Database.getDynamics().getNpcTemplateData().load();
    logger.debug("The non-player characters were loaded successfully.");

    Database.getDynamics().getNpcQuestionData().load();
    logger.debug("The n-p-c questions were loaded successfully.");

    Database.getDynamics().getNpcAnswerData().load();
    logger.debug("The n-p-c answers were loaded successfully.");

    Database.getDynamics().getQuestObjectiveData().load();
    logger.debug("The quest goals were loaded successfully.");

    Database.getDynamics().getGuildData().loadALL();;
    logger.debug("The Guilde  were loaded successfully.");

    
    Database.getDynamics().getQuestStepData().load();
    logger.debug("The quest steps were loaded successfully.");

    Database.getDynamics().getQuestData().load();
    logger.debug("The quests data were loaded successfully.");

    Database.getStatics().getAreaData().load();
    logger.debug("The statics areas data were loaded successfully.");
    Database.getDynamics().getAreaData().load();
    logger.debug("The dynamics areas data were loaded successfully.");

    Database.getDynamics().getSubAreaData().load();
    logger.debug("The sub-area data were loaded successfully.");
    
    Database.getDynamics().getPrismData().load();
    logger.debug("The prisms were loaded successfully.");

    Database.getDynamics().getInteractiveDoorData().load();
    logger.debug("The templates of interactive doors were loaded successfully.");

    Database.getDynamics().getInteractiveObjectData().load();
    logger.debug("The templates of interactive objects were loaded successfully.");

    Database.getDynamics().getCraftData().load();
    logger.debug("The crafts were loaded successfully.");

    Database.getDynamics().getJobData().load();
    logger.debug("The jobs were loaded successfully.");

    Database.getDynamics().getObjectSetData().load();
    logger.debug("The panoplies were loaded successfully.");

    Database.getDynamics().getMapData().load();
    logger.debug("The maps were loaded successfully.");

    Database.getDynamics().getScriptedCellData().load();
    logger.debug("The scripted cells were loaded successfully.");

    Database.getDynamics().getEndFightActionData().load();
    logger.debug("The end fight actions were loaded successfully.");

    Database.getDynamics().getNpcData().load();
    logger.debug("The placement of non-player character were done successfully.");

    Database.getDynamics().getObjectActionData().load();
    logger.debug("The action of objects were loaded successfully.");

    Database.getDynamics().getMountData().loadALL(ObjTemplates);;
    logger.debug("The All Mount data were loaded successfully.");
    
    Database.getDynamics().getDropData().load();
    logger.debug("The drops were loaded successfully.");

    logger.debug("The mounts were loaded successfully.");

    Database.getDynamics().getAnimationData().load();
    logger.debug("The animations were loaded successfully.");

    Database.getStatics().getAccountData().load();
    logger.debug("The accounts were loaded successfully.");
    
    Database.getDynamics().getBankData().LoadALL();
    logger.debug("The All bank accounts were loaded successfully.");


    Database.getStatics().getPlayerData().load();
    logger.debug("The players were loaded successfully.");
    
    Database.getDynamics().getQuestPlayerData().loadPersoALL();
    logger.debug("The All Quetes players were loaded successfully.");

    Database.getDynamics().getGuildMemberData().load();
    logger.debug("The guilds and guild members were loaded successfully.");

    Database.getDynamics().getPetData().load();
    logger.debug("The pets were loaded successfully.");

    Database.getDynamics().getPetTemplateData().load();
    logger.debug("The templates of pets were loaded successfully.");

    Database.getDynamics().getTutorialData().load();
    logger.debug("The tutorials were loaded successfully.");

    Database.getStatics().getMountParkData().load();
    logger.debug("The statics parks of the mounts were loaded successfully.");
    Database.getDynamics().getMountParkData().load();
    logger.debug("The dynamics parks of the mounts were loaded successfully.");

    Database.getDynamics().getCollectorData().load();
    logger.debug("The collectors were loaded successfully.");

    Database.getStatics().getHouseData().load();
    logger.debug("The statics houses were loaded successfully.");
    Database.getDynamics().getHouseData().load();
    logger.debug("The dynamics houses were loaded successfully.");

    Database.getStatics().getTrunkData().load();
    logger.debug("The statics trunks were loaded successfully.");
    Database.getDynamics().getTrunkData().load();
    logger.debug("The dynamics trunks were loaded successfully.");

    Database.getDynamics().getZaapData().load();
    logger.debug("The zaaps were loaded successfully.");

    Database.getDynamics().getZaapiData().load();
    logger.debug("The zappys were loaded successfully.");

    Database.getDynamics().getChallengeData().load();
    logger.debug("The challenges were loaded successfully.");

    Database.getDynamics().getHdvData().load();
    logger.debug("The hotels of sales were loaded successfully.");

    Database.getDynamics().getHdvObjectData().load();
    logger.debug("The objects of hotels were loaded successfully.");

    Database.getDynamics().getDungeonData().load();
    logger.debug("The dungeons were loaded successfully.");
    logger.debug("Chargement des Titre "+Database.getDynamics().getTitreData().load_titre()+".");
    logger.debug("Chargement des Bourse Kamas "+Database.getDynamics().getboursekamasData().load_bourse()+".");
    Database.getDynamics().getHeroicMobsGroups().load();
    logger.debug("The dynamic mob groups were loaded successfully.");

    Database.getDynamics().getHeroicMobsGroups().loadFix();
    logger.debug("The fixed dynamic mob groups were loaded successfully.");

    loadExtraMonster();
    logger.debug("The adding of extra-monsters on the maps were done successfully.");

    loadMonsterOnMap();
    logger.debug("The adding of mobs groups on the maps were done successfully.");

    this.initializeFirefoux();
    logger.debug("The adding of mobs groups to firefoux dungeon was done succesfully.");

    Database.getDynamics().getGangsterData().load();
    logger.debug("The adding of gangsters on the maps were done successfully.");

    logger.debug("Initialization of the voteshop.");
    Boutique.initPacket();
    logger.debug("Loading "+Tokenshop.items.size()+" tokenshop items.");
    logger.debug("Initialization of the tokenshop.");
    Tokenshop.initPacket();
    Database.getStatics().getCategoriaData().loadCategoria();
    logger.debug("Loading tienda categoria.");
    
    Database.getStatics().getObjetosData().loadObjetos();
    logger.debug("Loading tienda objetos.");
    
    Database.getDynamics().getOrnementsData().load();
    logger.debug("The ornements were loaded successfully.");
    
    Database.getDynamics().getShortcutsData().load();
    logger.debug("The Shortcuts of playes were loaded successfully.");
    
    Rune.addRunes();
    logger.debug("Runes have been added succesfully");
    Potion.addPotions();
    logger.debug("Runes have been added succesfully");

    this.setPrisms(Sort.sortPrismsAlphabetically(Main.world.getPrisms()));
    actualizarRankings();
    Database.getStatics().getServerData().updateTime(time);
    logger.info("All data was loaded successfully at "+new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss",Locale.FRANCE).format(new Date())+" in "+new SimpleDateFormat("mm",Locale.FRANCE).format((System.currentTimeMillis()-time))+" min "+new SimpleDateFormat("ss",Locale.FRANCE).format((System.currentTimeMillis()-time))+" s.");
    logger.setLevel(Level.ALL);
    Main.getRandomRelique_donjon();
    Config.getInstance().bot_ok = true;
    this.kamas_total_start = this.kamas_total;
  }

  public void addExtraMonster(int idMob, String superArea, String subArea, int chances)
  {
    Map<String, Map<String, Integer>> map=new HashMap<>();
    Map<String, Integer> _map=new HashMap<>();
    _map.put(subArea,chances);
    map.put(superArea,_map);
    extraMonstre.put(idMob,map);
  }

  public Map<Integer, GameMap> getExtraMonsterOnMap()
  {
    return extraMonstreOnMap;
  }

  public void loadExtraMonster()
  {
    ArrayList<GameMap> mapPossible=new ArrayList<>();
    for(Entry<Integer, Map<String, Map<String, Integer>>> i : extraMonstre.entrySet())
    {
      try
      {
        Map<String, Map<String, Integer>> map=i.getValue();

        for(Entry<String, Map<String, Integer>> areaChances : map.entrySet())
        {
          Integer chances=null;
          for(Entry<String, Integer> _e : areaChances.getValue().entrySet())
          {
            Integer _c=_e.getValue();
            if(_c!=null&&_c!=-1)
              chances=_c;
          }
          if(!areaChances.getKey().equals(""))
          {// Si la superArea n'est pas null
            for(String ar : areaChances.getKey().split(","))
            {
              Area Area=areas.get(Integer.parseInt(ar));
              for(GameMap Map : Area.getMaps())
              {
                if(Map==null)
                  continue;
                if(Map.haveMobFix())
                  continue;
                if(!Map.isPossibleToPutMonster())
                  continue;

                if(chances!=null)
                  Map.addMobExtra(i.getKey(),chances);
                else if(!mapPossible.contains(Map))
                  mapPossible.add(Map);
              }
            }
          }
          if(areaChances.getValue()!=null) // Si l'area n'est pas null
          {
            for(Entry<String, Integer> area : areaChances.getValue().entrySet())
            {
              String areas=area.getKey();
              for(String sub : areas.split(","))
              {
                SubArea subArea=null;
                try
                {
                  subArea=subAreas.get(Integer.parseInt(sub));
                }
                catch(Exception e)
                {
                  e.printStackTrace();
                }
                if(subArea==null)
                  continue;
                for(GameMap Map : subArea.getMaps())
                {
                  if(Map==null)
                    continue;
                  if(Map.haveMobFix())
                    continue;
                  if(!Map.isPossibleToPutMonster())
                    continue;

                  if(chances!=null)
                    Map.addMobExtra(i.getKey(),chances);
                  if(!mapPossible.contains(Map))
                    mapPossible.add(Map);
                }
              }
            }
          }
        }
        if(mapPossible.size()<=0)
        {
          throw new Exception(" no maps was found for the extra monster "+i.getKey()+".");
        }
        else
        {
          GameMap randomMap;
          if(mapPossible.size()==1)
            randomMap=mapPossible.get(0);
          else
            randomMap=mapPossible.get(Formulas.getRandomValue(0,mapPossible.size()-1));
          if(randomMap==null)
            throw new Exception("the random map is null.");
          if(getMonstre(i.getKey())==null)
            throw new Exception("the monster template of the extra monster is invalid (id : "+i.getKey()+").");
          if(randomMap.loadExtraMonsterOnMap(i.getKey()))
            extraMonstreOnMap.put(i.getKey(),randomMap);
          else
            throw new Exception("a empty mobs group or invalid monster.");
        }

        mapPossible.clear();
      }
      catch(Exception e)
      {
        e.printStackTrace();
        mapPossible.clear();
        logger.error("An error occurred when the server try to put extra-monster caused by : "+e.getMessage());
      }
    }
  }

  public Map<String, String> getGroupFix(int map, int cell)
  {
    return mobsGroupsFix.get(map+";"+cell);
  }

  public void addGroupFix(String str, String mob, int Time, Long stars)
  {
    mobsGroupsFix.put(str,new HashMap<>());
    mobsGroupsFix.get(str).put("groupData",mob);
    mobsGroupsFix.get(str).put("timer",Time+"");
    mobsGroupsFix.get(str).put("stars",stars+"");
  }

  //v2.0 - Random dungeons
  public Map<String, String> getRandomGroupFix(int map, int cell)
  {
    return randomMobsGroupsFix.get(map+";"+cell);
  }

  //v2.0 - Random dungeons
  public void addRandomGroupFix(String str, String mob, int Time)
  {

    randomMobsGroupsFix.put(str,new HashMap<>());
    randomMobsGroupsFix.get(str).put("groupData",mob);
    randomMobsGroupsFix.get(str).put("timer",Time+"");
  }

  public void loadMonsterOnMap()
  {
    long time=System.currentTimeMillis();
    maps.values().stream().filter(map -> map!=null).forEach(map -> {
      if(map.getId()!=8338&&map.getId()!=8340&&map.getId()!=8342&&map.getId()!=8344&&map.getId()!=8345&&map.getId()!=8347)
      {//dont spawn on firefoux maps
        try
        {
          map.loadMonsterOnMap();
        }
        catch(Exception e)
        {
          logger.error("An error occurred when the server try to put monster on the map id "+map.getId()+".");
        }
      }
    });
    long time2=System.currentTimeMillis();
    System.out.println("Loaded monsters on all maps. ("+(time2-time)+"ms)");
  }

  public Area getArea(int areaID)
  {
    return areas.get(areaID);
  }

  public SubArea getSubArea(int areaID)
  {
    return subAreas.get(areaID);
  }

  public void addArea(Area area)
  {
    areas.put(area.getId(),area);
  }

  public void addSubArea(SubArea SA)
  {
    subAreas.put(SA.getId(),SA);
  }

  //v2.7 - Replaced String += with StringBuilder
  public String getSousZoneStateString()
  {
    StringBuilder str=new StringBuilder();
    boolean first=false;
    for(SubArea subarea : subAreas.values())
    {
      if(!subarea.getConquistable())
        continue;
      if(first)
        str.append("|");
      str.append(subarea.getId()+";"+subarea.getAlignement());
      first=true;
    }
    return str.toString();
  }

  public void addNpcAnswer(NpcAnswer rep)
  {
    answers.put(rep.getId(),rep);
  }

  public NpcAnswer getNpcAnswer(int guid)
  {
    return answers.get(guid);
  }

  public double getBalanceArea(Area area, int alignement)
  {
    int cant=0;
    for(SubArea subarea : subAreas.values())
    {
      if(subarea.getArea()==area&&subarea.getAlignement()==alignement)
        cant++;
    }
    if(cant==0||area.getSubAreas().size()==0)
      return 0;
    return Math.rint((1000*cant/(area.getSubAreas().size()))/10);
  }

  public double getBalanceWorld(int alignement)
  {
    int cant=0;
    for(SubArea subarea : subAreas.values())
    {
      if(subarea.getAlignement()==alignement)
        cant++;
    }
    if(cant==0)
      return 0;
    return Math.rint((10*cant/4)/10);
  }

  //v2.0 - Conquest bonus edit
  public double getConquestBonus(Player player)
  {
    if(player==null)
      return 0;
    if(player.get_align()==0)
      return 0;
    final double factor=getBalanceWorld(player.get_align())*Math.rint((player.getGrade()/2.5)+1);
    if(factor<0)
      return 0;
    return factor;
  }
  public static soufix.other.Titre gettitre(final int id) {
		return World.Titre.get(id);
	}
  
  public static soufix.other.Succes get_Succes(final int id) {
		return World.Succes.get(id);
	}
  public static soufix.other.Bourse_kamas get_bourse(final int id) {
		return World.Bourse_kamas.get(id);
	}

  public int getExpLevelSize()
  {
    return experiences.size();
  }

  public void addExpLevel(int lvl, ExpLevel exp)
  {
    experiences.put(lvl,exp);
  }

  public void addNPCQuestion(NpcQuestion quest)
  {
    questions.put(quest.getId(),quest);
  }

  public NpcQuestion getNPCQuestion(int guid)
  {
    return questions.get(guid);
  }

  public NpcTemplate getNPCTemplate(int guid)
  {
    return npcsTemplate.get(guid);
  }

  public void addNpcTemplate(NpcTemplate temp)
  {
    npcsTemplate.put(temp.getId(),temp);
  }

  public void removePlayer(Player player)
  {
    if(player.get_guild()!=null)
    {
      if(player.get_guild().getMembers().size()<=1)
        removeGuild(player.get_guild().getId());
      else if(player.getGuildMember().getRank()==1)
      {
        int curMaxRight=0;
        Player leader=null;

        for(Player newLeader : player.get_guild().getMembers())
          if(newLeader!=player&&newLeader.getGuildMember().getRights()<curMaxRight)
            leader=newLeader;

        player.get_guild().removeMember(player);
        if(leader!=null)
          leader.getGuildMember().setRank(1);
      }
      else
      {
        player.get_guild().removeMember(player);
      }
    }
    if(player.getWife()!=0)
    {
      Player wife=getPlayer(player.getWife());

      if(wife!=null)
      {
        wife.setWife(0);
      }
    }
    if(player.getQuestPerso()!=null&&!player.getQuestPerso().isEmpty())
    {
      for(Entry<Integer, QuestPlayer> entry : new HashMap<>(player.getQuestPerso()).entrySet())
      {
        QuestPlayer qa=entry.getValue();
        player.delQuestPerso(entry.getKey());
        if(qa.removeQuestPlayer())
        {

        }
      }
    }
    player.remove();
    unloadPerso(player.getId());
    players.remove(player.getId());
  }

  public void unloadPerso(Player perso)
  {
    unloadPerso(perso.getId());//UnLoad du perso+item
    players.remove(perso.getId());
  }

  public long getPersoXpMin(int _lvl)
  {
    if(_lvl>getExpLevelSize())
      _lvl=getExpLevelSize();
    if(_lvl<1)
      _lvl=1;
    return experiences.get(_lvl).perso;
  }

  public long getPersoXpMax(int _lvl)
  {
    if(_lvl>=getExpLevelSize())
      _lvl=(getExpLevelSize()-1);
    if(_lvl<=1)
      _lvl=1;
    return experiences.get(_lvl+1).perso;
  }

  public long getTourmenteursXpMax(int _lvl)
  {
    if(_lvl>=getExpLevelSize())
      _lvl=(getExpLevelSize()-1);
    if(_lvl<=1)
      _lvl=1;
    return experiences.get(_lvl+1).tourmenteurs;
  }

  public long getBanditsXpMin(int _lvl)
  {
    if(_lvl>getExpLevelSize())
      _lvl=getExpLevelSize();
    if(_lvl<1)
      _lvl=1;
    return experiences.get(_lvl).bandits;
  }

  public long getBanditsXpMax(int _lvl)
  {
    if(_lvl>=getExpLevelSize())
      _lvl=(getExpLevelSize()-1);
    if(_lvl<=1)
      _lvl=1;
    return experiences.get(_lvl+1).bandits;
  }

  public void addSort(Spell sort)
  {
    spells.put(sort.getSpellID(),sort);
  }

  public Spell getSort(int id)
  {
    return spells.get(id);
  }

  public void addObjTemplate(ObjectTemplate obj)
  {
    ObjTemplates.put(obj.getId(),obj);
  }

  public ObjectTemplate getObjTemplate(int id)
  {
    return ObjTemplates.get(id);
  }

  public ArrayList<ObjectTemplate> getEtherealWeapons(int level)
  {
    ArrayList<ObjectTemplate> array=new ArrayList<>();
    final int levelMin=(level-5<0 ? 0 : level-5),levelMax=level+5;
    getObjectsTemplates().values().stream().filter(objectTemplate -> objectTemplate!=null&&objectTemplate.getStrTemplate().contains("32c#")&&(levelMin<objectTemplate.getLevel()&&objectTemplate.getLevel()<levelMax)&&objectTemplate.getType()!=93).forEach(array::add);
    return array;
  }

  public void addMobTemplate(int id, Monster mob)
  {
    MobTemplates.put(id,mob);
  }

  public Monster getMonstre(int id)
  {
    return MobTemplates.get(id);
  }

  public Collection<Monster> getMonstres()
  {
    return MobTemplates.values();
  }

  public String getStatOfAlign()
  {
    int ange=0;
    int demon=0;
    int total=0;
    for(Player i : getPlayers())
    {
      if(i==null)
        continue;
      if(i.get_align()==1)
        ange++;
      if(i.get_align()==2)
        demon++;
      total++;
    }
    ange=ange/total;
    demon=demon/total;
    if(ange>demon)
      return "Les Brâkmarien sont actuellement en minorité, je peux donc te proposer de rejoindre les rangs Brâkmarien ?";
    else if(demon>ange)
      return "Les Bontarien sont actuellement en minorité, je peux donc te proposer de rejoindre les rangs Bontarien ?";
    else if(demon==ange)
      return " Aucune milice est actuellement en minorité, je peux donc te proposer de rejoindre aléatoirement une milice ?";
    return "Undefined";
  }

  public void addIOTemplate(InteractiveObjectTemplate IOT)
  {
    IOTemplate.put(IOT.getId(),IOT);
  }

  public Mount getMountById(int id)
  {

    Mount mount=Dragodindes.get(id);
    /*if(mount==null)
    {
      Database.getDynamics().getMountData().load(id);
      mount=Dragodindes.get(id);
    }
    */
    return mount;
  }

  public void addMount(Mount mount)
  {
    Dragodindes.put(mount.getId(),mount);
  }

  public void removeMount(int id)
  {
    Dragodindes.remove(id);
  }

  public void addTutorial(Tutorial tutorial)
  {
    Tutorial.put(tutorial.getId(),tutorial);
  }

  public Tutorial getTutorial(int id)
  {
    return Tutorial.get(id);
  }

  public ExpLevel getExpLevel(int lvl)
  {
    return experiences.get(lvl);
  }

  public InteractiveObjectTemplate getIOTemplate(int id)
  {
    return IOTemplate.get(id);
  }

  public Job getMetier(int id)
  {
    return Jobs.get(id);
  }

  public void addJob(Job metier)
  {
    Jobs.put(metier.getId(),metier);
  }

  public void addCraft(int id, ArrayList<Pair<Integer, Integer>> m)
  {
    Crafts.put(id,m);
  }

  public ArrayList<Pair<Integer, Integer>> getCraft(int i)
  {
    return Crafts.get(i);
  }

  public void addFullMorph(int morphID, String name, int gfxID, String spells, String[] args)
  {
    if(fullmorphs.get(morphID)!=null)
      return;

    fullmorphs.put(morphID,new HashMap<>());

    fullmorphs.get(morphID).put("name",name);
    fullmorphs.get(morphID).put("gfxid",gfxID+"");
    fullmorphs.get(morphID).put("spells",spells);
    if(args!=null)
    {
      fullmorphs.get(morphID).put("vie",args[0]);
      fullmorphs.get(morphID).put("pa",args[1]);
      fullmorphs.get(morphID).put("pm",args[2]);
      fullmorphs.get(morphID).put("vitalite",args[3]);
      fullmorphs.get(morphID).put("sagesse",args[4]);
      fullmorphs.get(morphID).put("terre",args[5]);
      fullmorphs.get(morphID).put("feu",args[6]);
      fullmorphs.get(morphID).put("eau",args[7]);
      fullmorphs.get(morphID).put("air",args[8]);
      fullmorphs.get(morphID).put("initiative",args[9]);
      fullmorphs.get(morphID).put("stats",args[10]);
      fullmorphs.get(morphID).put("donjon",args[11]);
    }
  }

  public Map<String, String> getFullMorph(int morphID)
  {
    return fullmorphs.get(morphID);
  }

  public int getObjectByIngredientForJob(ArrayList<Integer> list, Map<Integer, Integer> ingredients)
  {
    if(list==null)
      return -1;
    for(int tID : list)
    {
      ArrayList<Pair<Integer, Integer>> craft=getCraft(tID);
      if(craft==null)
        continue;
      if(craft.size()!=ingredients.size())
        continue;
      boolean ok=true;
      for(Pair<Integer, Integer> c : craft)
      {
        if(!((ingredients.get(c.getLeft())+" ").equals(c.getRight()+" "))) //si ingredient non présent ou mauvaise quantité
          ok=false;
      }
      if(ok)
        return tID;
    }
    return -1;
  }

  public void addItemSet(ObjectSet itemSet)
  {
    ItemSets.put(itemSet.getId(),itemSet);
  }

  public ObjectSet getItemSet(int tID)
  {
    return ItemSets.get(tID);
  }

  public int getItemSetNumber()
  {
    return ItemSets.size();
  }

  public ArrayList<GameMap> getMapByPosInArray(int mapX, int mapY)
  {
    ArrayList<GameMap> i=new ArrayList<>();
    for(GameMap map : maps.values())
      if(map.getX()==mapX&&map.getY()==mapY)
        i.add(map);
    return i;
  }

  public ArrayList<GameMap> getMapByPosInArrayPlayer(int mapX, int mapY, Player player)
  {
    return maps.values().stream().filter(map -> map!=null&&map.getSubArea()!=null&&player.getCurMap().getSubArea()!=null).filter(map -> map.getX()==mapX&&map.getY()==mapY&&map.getSubArea().getArea().getSuperArea()==player.getCurMap().getSubArea().getArea().getSuperArea()).collect(Collectors.toCollection(ArrayList::new));
  }

  public void addGuild(Guild g, boolean save)
  {
    Guildes.put(g.getId(),g);
    if(save)
      Database.getDynamics().getGuildData().add(g);
  }

  public boolean guildNameIsUsed(String name)
  {
    for(Guild g : Guildes.values())
      if(g.getName().equalsIgnoreCase(name))
        return true;
    return false;
  }

  public boolean guildEmblemIsUsed(String emb)
  {
    for(Guild g : Guildes.values())
    {
      if(g.getEmblem().equals(emb))
        return true;
    }
    return false;
  }

  public Guild getGuild(int i)
  {
	  if(i == -1)
		  return null;
    Guild guild=Guildes.get(i);
    if(guild==null)
    {
      Database.getDynamics().getGuildData().load(i);
      guild=Guildes.get(i);
    }
    return guild;
  }

  public int getGuildByName(String name)
  {
    for(Guild g : Guildes.values())
    {
      if(g.getName().equalsIgnoreCase(name))
        return g.getId();
    }
    return -1;
  }

  public long getGuildXpMax(int _lvl)
  {
    if(_lvl>=200)
      _lvl=199;
    if(_lvl<=1)
      _lvl=1;
    return experiences.get(_lvl+1).guilde;
  }

  public void ReassignAccountToChar(int id)
  {
	  Account account=Main.world.getAccount(id);
    Database.getStatics().getPlayerData().loadByAccountId(id);
    players.values().stream().filter(player -> player.getAccID()==id).forEach(player -> player.setAccount(account));
  }

  public int getZaapCellIdByMapId(short i)
  {
    for(Entry<Integer, Integer> zaap : Constant.ZAAPS.entrySet())
    {
      if(zaap.getKey()==i)
        return zaap.getValue();
    }
    return -1;
  }

  public int getEncloCellIdByMapId(short i)
  {
    GameMap map=getMap(i);
    if(map!=null&&map.getMountPark()!=null&&map.getMountPark().getCell()>0)
      return map.getMountPark().getCell();
    return -1;
  }

  public void delDragoByID(int getId)
  {
    Dragodindes.remove(getId);
  }

  public void removeGuild(int id)
  {
    House.removeHouseGuild(id);
    GameMap.removeMountPark(id);
    Collector.removeCollector(id);
    Guildes.remove(id);
    Database.getDynamics().getGuildMemberData().deleteAll(id);
    Database.getDynamics().getGuildData().delete(id);
  }

  public void unloadPerso(int g)
  {
    Player toRem=players.get(g);
    if(!toRem.getItems().isEmpty())
      for(Entry<Integer, GameObject> curObj : toRem.getItems().entrySet())
        objects.remove(curObj.getKey());
  }

  public GameObject newObjet(int Guid, int template, int qua, int pos, String strStats, float puit)
  {
    if(getObjTemplate(template)==null)
    {
      return null;
    }

    if(template==8378)
    {
      return new Fragment(Guid,strStats);
    }
    else if(getObjTemplate(template).getType()==85)
    {
      return new Capture(Guid,qua,template,pos,strStats);
    }
    else if(getObjTemplate(template).getType()==24&&(Constant.isCertificatDopeuls(getObjTemplate(template).getId())||getObjTemplate(template).getId()==6653))
    {
      try
      {
        Map<Integer, String> txtStat=new HashMap<>();
        txtStat.put(Constant.STATS_DATE,strStats.substring(3)+"");
        return new GameObject(Guid,template,qua,Constant.ITEM_POS_NO_EQUIPED,new Stats(false,null),new ArrayList<>(),new HashMap<>(),txtStat,puit);
      }
      catch(Exception e)
      {
        e.printStackTrace();
        return new GameObject(Guid,template,qua,pos,strStats,0);
      }
    }
    else
    {
      return new GameObject(Guid,template,qua,pos,strStats,0);
    }
  }

  public Map<Integer, Integer> getChangeHdv()
  {
    Map<Integer, Integer> changeHdv=new HashMap<>();
    changeHdv.put(8753,8759); // HDV Annimaux
    changeHdv.put(4607,4271); // HDV Alchimistes
    changeHdv.put(4622,4216); // HDV Bijoutiers
    changeHdv.put(4627,4232); // HDV Bricoleurs
    changeHdv.put(5112,4178); // HDV Bécherons
    changeHdv.put(4562,4183); // HDV Cordonniers
    changeHdv.put(8754,8760); // HDV Bibliothéque
    changeHdv.put(5317,4098); // HDV Forgerons
    changeHdv.put(4615,4247); // HDV Pécheurs
    changeHdv.put(4646,4262); // HDV Ressources
    changeHdv.put(8756,8757); // HDV Forgemagie
    changeHdv.put(4618,4174); // HDV Sculpteurs
    changeHdv.put(4588,4172); // HDV Tailleurs
    changeHdv.put(8482,10129); // HDV émes
    changeHdv.put(4595,4287); // HDV Bouchers
    changeHdv.put(4630,2221); // HDV Boulangers
    changeHdv.put(5311,4179); // HDV Mineurs
    changeHdv.put(4629,4299); // HDV Paysans
    return changeHdv;
  }

  // Utilisé deux fois. Pour tous les modes HDV dans la fonction getHdv ci-dessous et dans le mode Vente de GameClient.java
  public int changeHdv(int map)
  {
    Map<Integer, Integer> changeHdv=getChangeHdv();
    if(changeHdv.containsKey(map))
    {
      map=changeHdv.get(map);
    }
    return map;
  }

  public Hdv getHdv(int map)
  {
    return Hdvs.get(changeHdv(map));
  }

  //v2.8 - Global Marketplace
  public Hdv getWorldMarket()
  {
    return Hdvs.get(Config.getInstance().worldMarket);
  }

  public  int getNextObjectHdvId()
  {
    nextObjectHdvId++;
    return nextObjectHdvId;
  }

  public  void setNextObjectHdvId(int id)
  {
    nextObjectHdvId=id;
  }

  public int getNextLineHdvId()
  {
    nextLineHdvId++;
    return nextLineHdvId;
  }

  public void addHdvItem(int compteID, int hdvID, HdvEntry toAdd)
  {
    if(hdvsItems.get(compteID)==null) //Si le compte n'est pas dans la memoire
      hdvsItems.put(compteID,new HashMap<>()); //Ajout du compte clé:compteID et un nouveau Map<hdvID,items<>>
    if(hdvsItems.get(compteID).get(hdvID)==null)
      hdvsItems.get(compteID).put(hdvID,new CopyOnWriteArrayList<>());
    hdvsItems.get(compteID).get(hdvID).add(toAdd);
  }

  public void removeHdvItem(int compteID, int hdvID, HdvEntry toDel)
  {
    hdvsItems.get(compteID).get(hdvID).remove(toDel);
  }

  public void addHdv(Hdv toAdd)
  {
    Hdvs.put(toAdd.getHdvId(),toAdd);
  }

  public Map<Integer, CopyOnWriteArrayList<HdvEntry>> getMyItems(int compteID)
  {
    if(hdvsItems.get(compteID)==null) //Si le compte n'est pas dans la memoire
      hdvsItems.put(compteID,new HashMap<>());//Ajout du compte clé:compteID et un nouveau Map<hdvID,items
    return hdvsItems.get(compteID);
  }

  public Collection<ObjectTemplate> getObjTemplates()
  {
    return ObjTemplates.values();
  }

  //v2.8 - Same sex marriage
  public void priestRequest(Player player1, Player player2, Player asked)
  {
    final GameMap map=player1.getCurMap();
    if(player1.getWife()!=0)
    {
      SocketManager.GAME_SEND_MESSAGE_TO_MAP(map,player1.getName()+" is already married.",Config.getInstance().colorMessage);
      return;
    }
    if(player2.getWife()!=0)
    {
      SocketManager.GAME_SEND_MESSAGE_TO_MAP(map,player2.getName()+" is already married.",Config.getInstance().colorMessage);
      return;
    }
    SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(map,"",-1,"Priest",asked.getName()+", will your marry "+(asked.getSexe()==1 ? player2 : player1).getName()+"?");
    SocketManager.GAME_SEND_WEDDING(map,617,(player1==asked ? player1.getId() : player2.getId()),(player1==asked ? player2.getId() : player1.getId()),-1);
  }

  //v2.8 - Same sex marriage
  public void wedding(Player player1, Player player2, int isOK)
  {
    if(isOK>0)
    {
      SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(player1.getCurMap(),"",-1,"Priest","I declare "+player1.getName()+" and "+player2.getName()+" united by the sacred bonds of marriage.");
      player1.setWife(player2.getId());
      player2.setWife(player1.getId());
    }
    else
    {
      SocketManager.GAME_SEND_Im_PACKET_TO_MAP(player1.getCurMap(),"048;"+player1.getName()+"~"+player2.getName());
    }
    player1.setisOK(0);
    player1.setBlockMovement(false);
    player2.setisOK(0);
    player2.setBlockMovement(false);
  }

  public Animation getAnimation(int AnimationId)
  {
    return Animations.get(AnimationId);
  }

  public void addAnimation(Animation animation)
  {
    Animations.put(animation.getId(),animation);
  }

  public void addHouse(House house)
  {
    Houses.put(house.getId(),house);
  }

  public House getHouse(int id)
  {
    return Houses.get(id);
  }

  public void addCollector(Collector Collector)
  {
    collectors.put(Collector.getId(),Collector);
  }

  public Collector getCollector(int CollectorID)
  {
    return collectors.get(CollectorID);
  }

  public void addTrunk(Trunk trunk)
  {
    Trunks.put(trunk.getId(),trunk);
  }

  public Trunk getTrunk(int id)
  {
    return Trunks.get(id);
  }

  public void addMountPark(MountPark mp)
  {
    MountPark.put(mp.getMap().getId(),mp);
  }

  public Map<Short, MountPark> getMountPark()
  {
    return MountPark;
  }

  public String parseMPtoGuild(int GuildID)
  {
    Guild G=getGuild(GuildID);
    byte enclosMax=(byte)Math.floor(G.getLvl()/10);
    StringBuilder packet=new StringBuilder();
    packet.append(enclosMax);

    for(Entry<Short, MountPark> mp : MountPark.entrySet())
    {
      if(mp.getValue().getGuild()!=null&&mp.getValue().getGuild().getId()==GuildID)
      {
        packet.append("|").append(mp.getValue().getMap().getId()).append(";").append(mp.getValue().getSize()).append(";").append(mp.getValue().getMaxObject()); // Nombre d'objets pour le dernier
        if(mp.getValue().getListOfRaising().size()>0)
        {
          packet.append(";");
          boolean primero=false;
          for(Integer id : mp.getValue().getListOfRaising())
          {
            Mount dd=getMountById(id);
            if(dd!=null)
            {
              if(primero)
                packet.append(",");
              packet.append(dd.getColor()).append(",").append(dd.getName()).append(",");
              if(getPlayer(dd.getOwner())==null)
                packet.append("Sans maitre");
              else
                packet.append(getPlayer(dd.getOwner()).getName());
              primero=true;
            }
          }
        }
      }
    }
    return packet.toString();
  }

  public int totalMPGuild(int GuildID)
  {
    int i=0;
    for(Entry<Short, MountPark> mp : MountPark.entrySet())
      if(mp.getValue().getGuild()!=null&&mp.getValue().getGuild().getId()==GuildID)
        i++;
    return i;
  }

  public void addChallenge(String chal)
  {
    if(!Challenges.toString().isEmpty())
      Challenges.append(";");
    Challenges.append(chal);
  }

  public void addPrisme(Prism Prisme)
  {
    Prismes.put(Prisme.getId(),Prisme);
  }

  public Prism getPrisme(int id)
  {
    return Prismes.get(id);
  }

  public void removePrisme(int id)
  {
    Prismes.remove(id);
  }

  public Collection<Prism> AllPrisme()
  {
    if(Prismes.size()>0)
      return Prismes.values();
    return null;
  }

  //v2.7 - Replaced String += with StringBuilder
  public String PrismesGeoposition(int alignement)
  {
    StringBuilder str=new StringBuilder();
    boolean first=false;
    int subareas=0;
    for(SubArea subarea : subAreas.values())
    {
      if(!subarea.getConquistable())
        continue;
      if(first)
        str.append(";");
      str.append(subarea.getId()+","+(subarea.getAlignement()==0 ? -1 : subarea.getAlignement())+",0,");
      if(getPrisme(subarea.getPrismId())==null)
        str.append(0+",1");
      else
        str.append((subarea.getPrismId()==0 ? 0 : getPrisme(subarea.getPrismId()).getMap())+",1");
      first=true;
      subareas++;
    }
    if(alignement==1)
      str.append("|"+Area.bontarians);
    else if(alignement==2)
      str.append("|"+Area.brakmarians);
    str.append("|"+areas.size()+"|");
    first=false;
    for(Area area : areas.values())
    {
      if(area.getAlignement()==0)
        continue;
      if(first)
        str.append(";");
      str.append(area.getId()+","+area.getAlignement()+",1,"+(area.getPrismId()==0 ? 0 : 1));
      first=true;
    }
    String str2="";
    if(alignement==1)
      str2=Area.bontarians+"|"+subareas+"|"+(subareas-(SubArea.bontarians+SubArea.brakmarians))+"|"+str.toString();
    else if(alignement==2)
      str2=Area.brakmarians+"|"+subareas+"|"+(subareas-(SubArea.bontarians+SubArea.brakmarians))+"|"+str.toString();
    return str2;
  }

  public void showPrismes(Player perso)
  {
    for(SubArea subarea : subAreas.values())
    {
      if(subarea.getAlignement()==0)
        continue;
      SocketManager.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(perso,subarea.getId()+"|"+subarea.getAlignement()+"|1");
    }
  }

  public  int getNextIDPrisme()
  {
    int max=-102;
    for(int a : Prismes.keySet())
      if(a<max)
        max=a;
    return max-3;
  }

  public void addPets(Pet pets)
  {
    Pets.put(pets.getTemplateId(),pets);
  }

  public Pet getPets(int Tid)
  {
    return Pets.get(Tid);
  }

  public Collection<Pet> getPets()
  {
    return Pets.values();
  }

  public void addPetsEntry(PetEntry pets)
  {
    PetsEntry.put(pets.getObjectId(),pets);
  }

  public PetEntry getPetsEntry(int guid)
  {
    return PetsEntry.get(guid);
  }

  public PetEntry removePetsEntry(int guid)
  {
    return PetsEntry.remove(guid);
  }

  public String getChallengeFromConditions(boolean sevEnn, boolean sevAll, boolean bothSex, boolean EvenEnn, boolean MoreEnn, boolean hasCaw, boolean hasChaf, boolean hasRoul, boolean hasArak, int isBoss, boolean ecartLvlPlayer, boolean hasArround, boolean hasDisciple, boolean isSolo)
  {
    StringBuilder toReturn=new StringBuilder();
    boolean isFirst=true,isGood=false;
    int cond;

    for(String chal : Challenges.toString().split(";"))
    {
      if(!isFirst&&isGood)
        toReturn.append(";");
      isGood=true;
      int id=Integer.parseInt(chal.split(",")[0]);
      cond=Integer.parseInt(chal.split(",")[4]);
      //Necessite plusieurs ennemis
      if(((cond&1)==1)&&!sevEnn)
        isGood=false;
      //Necessite plusieurs allies
      if((((cond>>1)&1)==1)&&!sevAll)
        isGood=false;
      //Necessite les deux sexes
      if((((cond>>2)&1)==1)&&!bothSex)
        isGood=false;
      //Necessite un nombre pair d'ennemis
      if((((cond>>3)&1)==1)&&!EvenEnn)
        isGood=false;
      //Necessite plus d'ennemis que d'allies
      if((((cond>>4)&1)==1)&&!MoreEnn)
        isGood=false;
      //Jardinier
      if(!hasCaw&&(id==7))
        isGood=false;
      //Fossoyeur
      if(!hasChaf&&(id==12))
        isGood=false;
      //Casino Royal
      if(!hasRoul&&(id==14))
        isGood=false;
      //Araknophile
      if(!hasArak&&(id==15))
        isGood=false;
      //Les mules d'abord
      if(!ecartLvlPlayer&&(id==48))
        isGood=false;
      //Contre un boss de donjon
      if(isBoss!=-1&&id==5)
        isGood=false;
      //Hardi
      if(!hasArround&&id==36)
        isGood=false;
      //Mains propre
      if(!hasDisciple&&id==19)
        isGood=false;

      switch(id)
      {
        case 47:
        case 46:
        case 45:
        case 44:
          if(isSolo)
            isGood=false;
          break;
      }

      switch(isBoss)
      {
        case 1045://Kimbo
          switch(id)
          {
            case 37:
            case 8:
            case 1:
            case 2:
              isGood=false;
              break;
          }
          break;
        case 1072://Tynril
        case 1085://Tynril
        case 1086://Tynril
        case 1087://Tynril
          switch(id)
          {
            case 36:
            case 20:
              isGood=false;
              break;
          }
          break;
        case 1071://Rasboul Majeur
          switch(id)
          {
            case 9:
            case 22:
            case 17:
            case 47:
              isGood=false;
              break;
          }
          break;
        case 780://Skeunk
          switch(id)
          {
            case 35:
            case 25:
            case 4:
            case 32:
            case 3:
            case 31:
            case 34:
              isGood=false;
              break;
          }
          break;
        case 113://DC
          switch(id)
          {
            case 12:
            case 15:
            case 7:
            case 41:
              isGood=false;
              break;
          }
          break;
        case 612://Maitre pandore
          switch(id)
          {
            case 20:
            case 37:
              isGood=false;
              break;
          }
          break;
        case 478://Bworker
        case 568://Tanukoui san
        case 940://Rat blanc
          switch(id)
          {
            case 20:
              isGood=false;
              break;
          }
          break;
        case 1188://Blop multi
          switch(id)
          {
            case 20:
            case 46:
            case 44:
              isGood=false;
              break;
          }
          break;

        case 865://Grozila
        case 866://Grasmera
          switch(id)
          {
            case 31:
            case 32:
              isGood=false;
              break;
          }
          break;

      }
      if(isGood)
        toReturn.append(chal);
      isFirst=false;
    }
    return toReturn.toString();
  }

  public void verifyClone(Player p)
  {
    if(p.getCurCell()!=null&&p.getFight()==null)
    {
      if(p.getCurCell().getPlayers().contains(p))
      {
        p.getCurCell().removePlayer(p);
        //Database.getStatics().getPlayerData().update(p);
      }
    }
    //if(p.isOnline())
    //  Database.getStatics().getPlayerData().update(p);
  }

  public ArrayList<String> getRandomChallenge(int nombreChal, String challenges)
  {
    String MovingChals=";1;2;8;36;37;39;40;";// Challenges de déplacements incompatibles
    boolean hasMovingChal=false;
    String TargetChals=";3;4;10;25;31;32;34;35;38;42;";// ceux qui ciblent
    boolean hasTargetChal=false;
    String SpellChals=";5;6;9;11;19;20;24;41;";// ceux qui obligent é caster spécialement
    boolean hasSpellChal=false;
    String KillerChals=";28;29;30;44;45;46;48;";// ceux qui disent qui doit tuer
    boolean hasKillerChal=false;
    String HealChals=";18;43;";// ceux qui empéchent de soigner
    boolean hasHealChal=false;

    int compteur=0,i;
    ArrayList<String> toReturn=new ArrayList<>();
    String chal;
    while(compteur<100&&toReturn.size()<nombreChal)
    {
      compteur++;
      i=Formulas.getRandomValue(1,challenges.split(";").length);
      chal=challenges.split(";")[i-1];// challenge au hasard dans la liste

      if(!toReturn.contains(chal))// si le challenge n'y etait pas encore
      {
        if(MovingChals.contains(";"+chal.split(",")[0]+";"))// s'il appartient a une liste
          if(!hasMovingChal)// et qu'aucun de la liste n'a ete choisi deja
          {
            hasMovingChal=true;
            toReturn.add(chal);
            continue;
          }
          else
            continue;
        if(TargetChals.contains(";"+chal.split(",")[0]+";"))
          if(!hasTargetChal)
          {
            hasTargetChal=true;
            toReturn.add(chal);
            continue;
          }
          else
            continue;
        if(SpellChals.contains(";"+chal.split(",")[0]+";"))
          if(!hasSpellChal)
          {
            hasSpellChal=true;
            toReturn.add(chal);
            continue;
          }
          else
            continue;
        if(KillerChals.contains(";"+chal.split(",")[0]+";"))
          if(!hasKillerChal)
          {
            hasKillerChal=true;
            toReturn.add(chal);
            continue;
          }
          else
            continue;
        if(HealChals.contains(";"+chal.split(",")[0]+";"))
          if(!hasHealChal)
          {
            hasHealChal=true;
            toReturn.add(chal);
            continue;
          }
          else
            continue;
        toReturn.add(chal);
      }
      compteur++;
    }
    return toReturn;
  }

  public Collector getCollectorByMap(int id)
  {

    for(Entry<Integer, Collector> Collector : getCollectors().entrySet())
    {
      GameMap map=getMap(Collector.getValue().getMap());
      if(map.getId()==id)
      {
        return Collector.getValue();
      }
    }
    return null;
  }

  //public void reloadPlayerGroup()
 // {
 //   Main.gameServer.getClients().stream().filter(client -> client!=null&&client.getPlayer()!=null).forEach(client -> Database.getStatics().getPlayerData().reloadGroup(client.getPlayer()));
 // }

  public void reloadDrops()
  {
    Database.getDynamics().getDropData().reload();
  }
  public void reloadBourse()
  {
	  Bourse_kamas.clear();
    Database.getDynamics().getboursekamasData().load_bourse();
  }
  public void reloadEndFightActions()
  {
    Database.getDynamics().getEndFightActionData().reload();
  }

  public void reloadNpcs()
  {
    Database.getDynamics().getNpcTemplateData().reload();
    questions.clear();
    Database.getDynamics().getNpcQuestionData().load();
    answers.clear();
    Database.getDynamics().getNpcAnswerData().load();
  }

  public void reloadHouses()
  {
    Houses.clear();
    Database.getStatics().getHouseData().load();
    Database.getDynamics().getHouseData().load();
  }

  public void reloadTrunks()
  {
    Trunks.clear();
    Database.getStatics().getTrunkData().load();
    Database.getDynamics().getTrunkData().load();
  }

  public void reloadMaps()
  {
    Database.getDynamics().getMapData().reload();
  }

  public void reloadMountParks(int i)
  {
    Database.getStatics().getMountParkData().reload(i);
    Database.getDynamics().getMountParkData().reload(i);
  }

  public void reloadMonsters()
  {
    Database.getDynamics().getMonsterData().reload();
  }

  public void reloadQuests()
  {
    Database.getDynamics().getQuestData().load();
  }

  public void reloadObjectsActions()
  {
    Database.getDynamics().getObjectActionData().reload();
  }

  public void reloadSpells()
  {
    Database.getDynamics().getSpellData().load();
  }

  public void reloadItems()
  {
    Database.getDynamics().getObjectTemplateData().load();
  }

  public void addSeller(Player player)
  {
    if(player.getStoreItems().isEmpty())
      return;

    short map=player.getCurMap().getId();

    if(Seller.get(map)==null)
    {
      ArrayList<Integer> players=new ArrayList<>();
      players.add(player.getId());
      Seller.put(map,players);
    }
    else
    {
      ArrayList<Integer> players=new ArrayList<>();
      players.add(player.getId());
      players.addAll(Seller.get(map));
      Seller.remove(map);
      Seller.put(map,players);
    }
  }

  public Collection<Integer> getSeller(short map)
  {
    return Seller.get(map);
  }

  public void removeSeller(int player, short map)
  {
    if(getSeller(map)!=null)
      Seller.get(map).remove(player);
  }

  public double getTauxObtentionIntermediaire(double bonus, boolean b1, boolean b2)
  {
    double taux=bonus;
    if(b1)
    {
      if(bonus==100.0)
        taux+=2.0*getTauxObtentionIntermediaire(30.0,true,b2);
      if(bonus==30.0)
        taux+=2.0*getTauxObtentionIntermediaire(10.0,(!b2),b2); // Si b2 est false alors on calculera 2*3.0 dans 10.0
      if(bonus==10.0)
        taux+=2.0*getTauxObtentionIntermediaire(3.0,(b2),b2); // Si b2 est true alors on calculera aprés
      else if(bonus==3.0)
        taux+=2.0*getTauxObtentionIntermediaire(1.0,false,b2);
    }

    return taux;
  }

  public int getMetierByMaging(int idMaging)
  {
    int mId=-1;
    switch(idMaging)
    {
      case 43: // FM Dagues
        mId=17;
        break;
      case 44: // FM Epées
        mId=11;
        break;
      case 45: // FM Marteaux
        mId=14;
        break;
      case 46: // FM Pelles
        mId=20;
        break;
      case 47: // FM Haches
        mId=31;
        break;
      case 48: // FM Arcs
        mId=13;
        break;
      case 49: // FM Baguettes
        mId=19;
        break;
      case 50: // FM Bétons
        mId=18;
        break;
      case 62: // Cordo
        mId=15;
        break;
      case 63: // Jaillo
        mId=16;
        break;
      case 64: // Costu
        mId=27;
        break;
    }
    return mId;
  }

  public int getTempleByClasse(int classe)
  {
    int temple=-1;
    switch(classe)
    {
      case Constant.CLASS_FECA: // féca
        temple=1554;
        break;
      case Constant.CLASS_OSAMODAS: // osa
        temple=1546;
        break;
      case Constant.CLASS_ENUTROF: // énu
        temple=1470;
        break;
      case Constant.CLASS_SRAM: // sram
        temple=6926;
        break;
      case Constant.CLASS_XELOR: // xelor
        temple=1469;
        break;
      case Constant.CLASS_ECAFLIP: // éca
        temple=1544;
        break;
      case Constant.CLASS_ENIRIPSA: // éni
        temple=6928;
        break;
      case Constant.CLASS_IOP: // iop
        temple=1549;
        break;
      case Constant.CLASS_CRA: // cra
        temple=1558;
        break;
      case Constant.CLASS_SADIDA: // sadi
        temple=1466;
        break;
      case Constant.CLASS_SACRIEUR: // sacri
        temple=6949;
        break;
      case Constant.CLASS_PANDAWA: // panda
        temple=8490;
        break;
    }
    return temple;
  }

  public void initializeFirefoux()
  {
    Main.world.getMap((short)8338).spawnGroupWith(Main.world.getMonstre(599)); //room 1
    Main.world.getMap((short)8340).spawnGroupWith(Main.world.getMonstre(599)); //room 2
    Main.world.getMap((short)8342).spawnGroupWith(Main.world.getMonstre(599)); //room 3
    Main.world.getMap((short)8344).spawnGroupWith(Main.world.getMonstre(599)); //room 4
    Main.world.getMap((short)8345).spawnGroupWith(Main.world.getMonstre(599)); //room 5
    Main.world.getMap((short)8347).spawnGroupWith(Main.world.getMonstre(599)); //room 6
  }
  public static class Couple<L, R> {
      public L first;
      public R second;

      public Couple(L s, R i) {
          this.first = s;
          this.second = i;
      }
  }

  //v2.8 - min/maxdrop
  public static class Drop
  {
    private int objectId, ceil, action, level, minDrop, maxDrop;
    private String condition;
    private ArrayList<Double> percents;
    private double localPercent;
    private double bestiaers;

    //v2.8 - min/maxdrop
    public Drop(int objectId, ArrayList<Double> percents, int ceil, int action, int level, String condition, int minDrop, int maxDrop,double bestiaers)
    {
      this.objectId=objectId;
      this.percents=percents;
      this.ceil=ceil;
      this.action=action;
      this.level=level;
      this.condition=condition;
      this.minDrop=minDrop;
      this.maxDrop=maxDrop;
      this.bestiaers = bestiaers;
    }

    //v2.8 - min/maxdrop
    public Drop(int objectId, double percent, int ceil)
    {
      this.objectId=objectId;
      this.localPercent=percent;
      this.ceil=ceil;
      this.action=-1;
      this.level=-1;
      this.condition="";
      this.minDrop=1;
      this.maxDrop=1;
    }

    public int getObjectId()
    {
      return objectId;
    }

    public int getCeil()
    {
      return ceil;
    }

    public int getAction()
    {
      return action;
    }

    public int getLevel()
    {
      return level;
    }

    public String getCondition()
    {
      return condition;
    }

    public double getLocalPercent()
    {
      return localPercent;
    }
    public double bestiaers()
    {
      return bestiaers;
    }

    //v2.8 - min/maxdrop
    public int getMinDrop()
    {
      return minDrop;
    }

    //v2.8 - min/maxdrop
    public int getMaxDrop()
    {
      return maxDrop;
    }

    //v.28 - min/maxdrop
    public Drop copy(int grade)
    {
      Drop drop=new Drop(this.objectId,null,this.ceil,this.action,this.level,this.condition,this.minDrop,this.maxDrop,this.bestiaers);
      if(this.percents==null)
        return null;
      if(this.percents.isEmpty())
        return null;
      try
      {
        if(this.percents.get(grade-1)==null)
          return null;
        drop.localPercent=this.percents.get(grade-1);
      }
      catch(IndexOutOfBoundsException ignored)
      {
        return null;
      }
      return drop;
    }
  }

  public static class ExpLevel
  {
    public long perso;
    public int metier;
    public int mount;
    public int pvp;
    public long guilde;
    public long tourmenteurs;
    public long bandits;

    public ExpLevel(long c, int m, int d, int p, long t, long b)
    {
      perso=c;
      metier=m;
      this.mount=d;
      pvp=p;
      guilde=perso*10;
      tourmenteurs=t;
      bandits=b;
    }
  }
  //Ranking

  private static class CompNivelMasMenos implements Comparator<Player> {
      public int compare(Player p1, Player p2) {
          return Long.valueOf(p2.getExp()).compareTo((p1.getExp()));
      }
  }
 
  private static class CompPVPMasMenos implements Comparator<Player> {
	  public int compare(Player p1, Player p2) {
          int v = Long.valueOf(World.get_Succes(p2.getId()).getPvp()).compareTo((long) World.get_Succes(p1.getId()).getPvp());
          if (v == 0) {
              return Long.valueOf(0).compareTo((long) 0);
          }
          return v;
      }
  }
  private static class CompKOLIMasMenos implements Comparator<Player> {
	  public int compare(Player p1, Player p2) {
          int v = Long.valueOf(World.get_Succes(p2.getId()).getKoli_wine()).compareTo((long) World.get_Succes(p1.getId()).getKoli_wine());
          if (v == 0) {
              return Long.valueOf(World.get_Succes(p2.getId()).getKoli_lose()).compareTo((long) World.get_Succes(p2.getId()).getKoli_lose());
          }
          return v;
      }
  }
  private static class CompSuccesMasMenos implements Comparator<Player> {
	  public int compare(Player p1, Player p2) {
          return Long.valueOf(World.get_Succes(p2.getId()).getPoints()).compareTo((long)(World.get_Succes(p1.getId()).getPoints()));
      }
  }
  private static class CompGremioMasMenos implements Comparator<Guild> {
      public int compare(Guild p1, Guild p2) {
          return Long.valueOf(p2.getXp()).compareTo(p1.getXp());
      }
  }


  private static void rankingNivel() {
      ArrayList<Player> persos = new ArrayList<>();
      persos.addAll(Main.world.players.values());
      Collections.sort(persos, new CompNivelMasMenos());
      _LADDER_NIVEL.clear();

      _LADDER_NIVEL.addAll(persos);
  }
  private static void rankingPVP() {
      ArrayList<Player> persos = new ArrayList<>();
      persos.addAll(Main.world.players.values());
      Collections.sort(persos, new CompPVPMasMenos());
      _LADDER_PVP.clear();

      _LADDER_PVP.addAll(persos);
  }
  private static void rankingKOLI() {
      ArrayList<Player> persos = new ArrayList<>();
      persos.addAll(Main.world.players.values());
      Collections.sort(persos, new CompKOLIMasMenos());
      _LADDER_KOLI.clear();

      _LADDER_KOLI.addAll(persos);
  }
  private static void rankingSucces() {
      ArrayList<Player> persos = new ArrayList<>();
      persos.addAll(Main.world.players.values());
      Collections.sort(persos, new CompSuccesMasMenos());
      _LADDER_Succes.clear();

      _LADDER_Succes.addAll(persos);
  }
  private static void rankingGremio() {
      ArrayList<Guild> persos = new ArrayList<>();
      persos.addAll(Main.world.Guildes.values());
      Collections.sort(persos, new CompGremioMasMenos());
      _LADDER_GREMIO.clear();
      _LADDER_GREMIO.addAll(persos);
  }


  private static void addPaginas(StringBuilder temp, int inicio, int add) {
      temp.append("|" + (inicio == -1 ? 0 : 1) + "|" + (add == 20 + 1 ? 1 : 0));
  }

  private static void addStringParaLadder(StringBuilder temp, Player perso, int pos) {

      if (temp.length() > 0) {
          temp.append("#");
      }
      temp.append(getStringParaLadder(perso, pos));
  }
  private static void addStringParaSucces(StringBuilder temp, Player perso, int pos) {

      if (temp.length() > 0) {
          temp.append("#");
      }
      temp.append(getStringParaSucces(perso, pos));
  }
  private static void addStringParaLadderkoli(StringBuilder temp, Player perso, int pos) {

      if (temp.length() > 0) {
          temp.append("#");
      }
      temp.append(getStringParaLadderkoli(perso, pos));
  }
  private static void addStringParaLadderpvp(StringBuilder temp, Player perso, int pos) {

      if (temp.length() > 0) {
          temp.append("#");
      }
      temp.append(getStringParaLadderpvp(perso, pos));
  }
  private static String getStringParaLadderkoli(Player perso, int pos) {
      int victorias = World.get_Succes(perso.getId()).getKoli_wine();
      int derrotas = World.get_Succes(perso.getId()).getKoli_lose();
      return pos + ";" + perso.getGfxId() + ";" + perso.getName() + ";" + perso.get_titleladder() + ";" + perso
              .getLevel() + ";" + victorias + "	"+ derrotas+ ";" + (perso.isOnline() ? (perso.getFight() != null ? 2 : 1) : 1) + ";"
              + perso.get_align();
  }

  private static String getStringParaLadderpvp(Player perso, int pos) {
      int victorias = World.get_Succes(perso.getId()).getPvp();
      int derrotas = World.get_Succes(perso.getId()).getPvp_lose();
      return pos + ";" + perso.getGfxId() + ";" + perso.getName() + ";" + perso.get_titleladder() + ";" + perso
              .getLevel() + ";" + victorias + "	"+ derrotas+ ";" + (perso.isOnline() ? (perso.getFight() != null ? 2 : 1) : 1) + ";"
              + perso.get_align();
  }
  private static String getStringParaLadder(Player perso, int pos) {
      if(perso.getGroupe() != null)
          return "";
      return pos + ";" + perso.getGfxId() + ";" + perso.getName() + ";" + perso.get_titleladder() + ";" + perso.getLevel() + ";" + perso.getExp() + ";" + (perso.isOnline() ? (perso.getFight() != null ? 2 : 1) : 1) + ";"
              + perso.get_align();
  }
  private static String getStringParaSucces(Player perso, int pos) {
      if(perso.getGroupe() != null)
          return "";
      return pos + ";" + perso.getGfxId() + ";" + perso.getName() + ";" + perso.get_titleladder() + ";" + perso.getLevel() + ";" + World.get_Succes(perso.getId()).getPoints() + ";" + (perso.isOnline() ? (perso.getFight() != null ? 2 : 1) : 1) + ";"
              + perso.get_align();
  }


  private static void strRankingNivel(final Player out, String buscar, int iniciarEn) {
      int pos = 0, add = 0;
      int inicio = 0;
      final StringBuilder temp = new StringBuilder();
      if(_LADDER_NIVEL.isEmpty()){
          SocketManager.send(out, "bD");
          return;
      }
      for (final Player perso : _LADDER_NIVEL) {
          try {
              if(perso.getGroupe() != null)
                  continue;
              if (add > 50) {
                  break;
              }
              pos++;
              if (!buscar.isEmpty()) {
                  if (!perso.getName().toUpperCase().contains(buscar)) {
                      continue;
                  }
              }
              if (inicio == 0) {
                  inicio = pos;
              }
              if (pos < iniciarEn) {
                  continue;
              }
              if (pos == inicio) {
                  inicio = -1;
              }
              if (add < 50) {
                  addStringParaLadder(temp, perso, pos);
              }
              add++;
          } catch (final Exception e) {}
      }
      addPaginas(temp, inicio, add);//this
      SocketManager.ENVIAR_bl_RANKING_DATA(out, "NIVEL", temp.toString());
  }
  private static void strRankingSucces(final Player out, String buscar, int iniciarEn) {
      int pos = 0, add = 0;
      int inicio = 0;
      final StringBuilder temp = new StringBuilder();
      if(_LADDER_Succes.isEmpty()){
          SocketManager.send(out, "bD");
          return;
      }
      for (final Player perso : _LADDER_Succes) {
          try {
              if(perso.getGroupe() != null)
                  continue;
              if (add > 50) {
                  break;
              }
              pos++;
              if (!buscar.isEmpty()) {
                  if (!perso.getName().toUpperCase().contains(buscar)) {
                      continue;
                  }
              }
              if (inicio == 0) {
                  inicio = pos;
              }
              if (pos < iniciarEn) {
                  continue;
              }
              if (pos == inicio) {
                  inicio = -1;
              }
              if (add < 50) {
                  addStringParaSucces(temp, perso, pos);
              }
              add++;
          } catch (final Exception e) {}
      }
      addPaginas(temp, inicio, add);//this
      SocketManager.ENVIAR_bl_RANKING_DATA(out, "SUCCESS", temp.toString());
  }
    public static void strRankingPVP(final Player out, String buscar, int iniciarEn) {
      int pos = 0, add = 0;
      int inicio = 0;
      final StringBuilder temp = new StringBuilder();
      if(_LADDER_PVP.isEmpty()){
          SocketManager.send(out, "bD");
          return;
      }
      for (final Player perso : _LADDER_PVP) {
          try {
              if(perso.getGroupe() != null)
                  continue;
              if (add > 50) {
                  break;
              }
              pos++;
              if (!buscar.isEmpty()) {
                  if (!perso.getName().toUpperCase().contains(buscar)) {
                      continue;
                  }
              }
              if (inicio == 0) {
                  inicio = pos;
              }
              if (pos < iniciarEn) {
                  continue;
              }
              if (pos == inicio) {
                  inicio = -1;
              }
              if (add < 50) {
                  addStringParaLadderpvp(temp, perso, pos);
              }
              add++;
          } catch (final Exception e) {}
      }
      addPaginas(temp, inicio, add);//this
      SocketManager.ENVIAR_bl_RANKING_DATA(out, "PVP", temp.toString());
  }



  private static void strRankingKOLI(final Player out, String buscar, int iniciarEn) {
      int pos = 0, add = 0;
      int inicio = 0;
      final StringBuilder temp = new StringBuilder();
      if(_LADDER_KOLI.isEmpty()){
          SocketManager.send(out, "bD");
          return;
      }
      for (final Player perso : _LADDER_KOLI) {
          try {
              if(perso.getGroupe() != null)
                  continue;
              if (add > 50) {
                  break;
              }
              pos++;
              if (!buscar.isEmpty()) {
                  if (!perso.getName().toUpperCase().contains(buscar)) {
                      continue;
                  }
              }
              if (inicio == 0) {
                  inicio = pos;
              }
              if (pos < iniciarEn) {
                  continue;
              }
              if (pos == inicio) {
                  inicio = -1;
              }
              if (add < 50) {
            	  addStringParaLadderkoli(temp, perso, pos);
              }
              add++;
          } catch (final Exception e) {}
      }
      addPaginas(temp, inicio, add);//this
      SocketManager.ENVIAR_bl_RANKING_DATA(out, "KOLISEO", temp.toString());
  }
  private static void strRankingGremio(final Player out, String buscar, int iniciarEn) {
      int pos = 0, add = 0;
      int inicio = 0;
      final StringBuilder temp = new StringBuilder();
      try {
          for (final Guild gremio : _LADDER_GREMIO) {
              try {
                  if (add > 50) {
                      break;
                  }
                  pos++;
                  if (!buscar.isEmpty()) {
                      if (!gremio.getName().toUpperCase().contains(buscar)) {
                          continue;
                      }
                  }
                  if(gremio.getId() == 1)
                      continue;
                  if (inicio == 0) {
                      inicio = pos;
                  }
                  if (pos < iniciarEn) {
                      continue;
                  }
                  if (pos == inicio) {
                      inicio = -1;
                  }
                  if (add < 50) {
                      if (temp.length() > 0) {
                          temp.append("#");
                      }
                      temp.append(pos + ";" + gremio.getEmblem() + ";" + gremio.getName() + ";" + gremio.getMembers().size()
                              + ";" + gremio.getLvl() + ";" + gremio.getXp() + ";;;");
                  }
                  add++;
              } catch (final Exception e) {}
          }
      }catch (Exception e) {
          e.printStackTrace();
      }
      addPaginas(temp, inicio, add);//this
      SocketManager.ENVIAR_bl_RANKING_DATA(out, "GREMIO", temp.toString());
  }

  public int getNumberOfThread() {
      int fight = getNumberOfFight();
      int player = getOnlinePlayers().size();
      return (fight + player) / 30;
  }
  public int getNumberOfFight() {
      final int[] fights = {0};
      this.maps.values().forEach(map -> fights[0] += map.getFights().size());
      return fights[0];
  }

  public static String rankingsPermitidos() {
      final StringBuilder temp = new StringBuilder();
      if (temp.length() > 0) {
          temp.append("|");
      }
      if(Config.singleton.serverId != 6) {
      temp.append("Nivel");
      if (temp.length() > 0) {
          temp.append("|");
      }
      }
      temp.append("PVP");
      if (temp.length() > 0) {
          temp.append("|");
      }
      temp.append("Gremio");
      if (temp.length() > 0) {
          temp.append("|");
      }
      temp.append("Succes");
      if (temp.length() > 0) {
          temp.append("|");
      }
      
      temp.append("Koliseo");
      return temp.toString();
  }
  public static void enviarRanking(Player perso, String param, String buscar, int iniciarEn) {
      switch (param) {
          case "NIVEL" :
              strRankingNivel(perso, buscar, iniciarEn);
              break;
          case "PVP" :
              strRankingPVP(perso, buscar, iniciarEn);
              break;
          case "KOLISEO" :
        	  strRankingKOLI(perso, buscar, iniciarEn);
              break;
          case "GREMIO" :
              strRankingGremio(perso, buscar, iniciarEn);
              break;
          case "SUCCESS" :
        	  strRankingSucces(perso, buscar, iniciarEn);
              break;
              
          default :
              SocketManager.GAME_SEND_BN(perso);
              return;
      }
  }

  public static void actualizarRankings() {
      rankingNivel();
      rankingPVP();
      rankingGremio();
      rankingKOLI();
      rankingSucces();
  }
	public static TiendaCategoria getTiendaCategoria2(final int id) {
		return World.TiendaCategoria.get(id);
	}

	public static TiendaObjetos getTiendaObjetos2(final int id) {
		return World.tiendaObjetos.get(id);
	}
	
	 public static void addOrnements(Ornements orn) {
	        getOrnements().put(orn.getId(), orn);

	    }

	    public static void setOrnements(Map<Integer, Ornements> orn) {
	        Ornements = orn;
	    }
	    public static Map<Integer, Ornements> getOrnements() {
	        return Ornements;
	    }

	    public Map<Player, List<Shortcuts>> getAllShortcuts()
	    {
	        return shortcuts;
	    }
	    public List<Shortcuts> getShortcutsFromPlayer(Player player)
	    {
	        return shortcuts.get(player);
	    }
	    public Shortcuts getShortcutsFromPlayerByPosition(Player player, int position)
	    {
	        List<Shortcuts> shortcutsList = shortcuts.get(player);
	        if(shortcutsList != null) {
	            for(Shortcuts shortcut : shortcutsList)
	            {
	                if(shortcut.getPosition() == position){
	                    return  shortcut;
	                }
	            }
	        }
	        return null;
	    }
	    public void addShortcut(Player player, Shortcuts shortcut)
	    {
	        if(shortcuts.containsKey(player))
	        {
	            List<Shortcuts> shortcutsList = shortcuts.get(player);
	            if(shortcutsList != null){
	                if(!shortcuts.get(player).contains(shortcut)) {
	                    shortcutsList.add(shortcut);
	                }
	            }
	        }
	        else
	        {
	            List<Shortcuts> shortcutsList = new ArrayList<>();
	            shortcutsList.add(shortcut);
	            shortcuts.put(player, shortcutsList);
	        }

	    }
	    public void removeShortcut(Player player, Shortcuts shortcut)
	    {
	        if(shortcuts.containsKey(player))
	        {
	            shortcuts.get(player).remove(shortcut);
	        }
	    }

}
