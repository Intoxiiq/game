package soufix.database.active;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import soufix.database.Database;
import soufix.database.active.data.AnimationData;
import soufix.database.active.data.AreaData;
import soufix.database.active.data.BankData;
import soufix.database.active.data.Bourse_kamasdata;
import soufix.database.active.data.ChallengeData;
import soufix.database.active.data.CollectorData;
import soufix.database.active.data.CraftData;
import soufix.database.active.data.DropData;
import soufix.database.active.data.DungeonData;
import soufix.database.active.data.EndFightActionData;
import soufix.database.active.data.ExperienceData;
import soufix.database.active.data.ExtraMonsterData;
import soufix.database.active.data.FullMorphData;
import soufix.database.active.data.GangsterData;
import soufix.database.active.data.GiftData;
import soufix.database.active.data.GuildData;
import soufix.database.active.data.GuildMemberData;
import soufix.database.active.data.HdvData;
import soufix.database.active.data.HdvObjectData;
import soufix.database.active.data.HeroicMobsGroups;
import soufix.database.active.data.HouseData;
import soufix.database.active.data.InteractiveDoorData;
import soufix.database.active.data.InteractiveObjectData;
import soufix.database.active.data.JobData;
import soufix.database.active.data.MapData;
import soufix.database.active.data.MonsterData;
import soufix.database.active.data.MountData;
import soufix.database.active.data.MountParkData;
import soufix.database.active.data.NpcAnswerData;
import soufix.database.active.data.NpcData;
import soufix.database.active.data.NpcQuestionData;
import soufix.database.active.data.NpcTemplateData;
import soufix.database.active.data.ObjectActionData;
import soufix.database.active.data.ObjectData;
import soufix.database.active.data.ObjectSetData;
import soufix.database.active.data.ObjectTemplateData;
import soufix.database.active.data.ObvejivanData;
import soufix.database.active.data.OrnementsData;
import soufix.database.active.data.PetData;
import soufix.database.active.data.PetTemplateData;
import soufix.database.active.data.PrismData;
import soufix.database.active.data.QuestData;
import soufix.database.active.data.QuestObjectiveData;
import soufix.database.active.data.QuestPlayerData;
import soufix.database.active.data.QuestStepData;
import soufix.database.active.data.ScriptedCellData;
import soufix.database.active.data.ShortcutsData;
import soufix.database.active.data.SpellData;
import soufix.database.active.data.SubAreaData;
import soufix.database.active.data.TitreData;
import soufix.database.active.data.TrunkData;
import soufix.database.active.data.TutorialData;
import soufix.database.active.data.ZaapData;
import soufix.database.active.data.ZaapiData;
import soufix.main.Config;
import soufix.main.Main;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.LoggerFactory;

public class DynamicsDatabase
{
  private static Logger logger=(Logger)LoggerFactory.getLogger(DynamicsDatabase.class);
  //connection
  private HikariDataSource dataSource;
  //data
  private AreaData areaData;
  private GangsterData gangsterData;
  private BankData bankData;
  private TrunkData trunkData;
  private GuildMemberData guildMemberData;

  private HdvObjectData hdvObjectData;
  private HouseData houseData;
  private MountParkData mountParkData;
  private CollectorData collectorData;
  private PrismData prismData;
  private SubAreaData subAreaData;
  private AnimationData animationData;
  private ChallengeData challengeData;
  private CraftData craftData;
  private DungeonData dungeonData;
  private DropData dropData;
  private EndFightActionData endFightActionData;
  private ExperienceData experienceData;
  private ExtraMonsterData extraMonsterData;
  private FullMorphData fullMorphData;
  private GiftData giftData;
  private HdvData hdvData;
  private InteractiveDoorData interactiveDoorData;
  private InteractiveObjectData interactiveObjectData;
  private ObjectTemplateData objectTemplateData;
  private ObjectSetData objectSetData;
  private JobData jobData;
  private MapData mapData;
  private MonsterData monsterData;
  private NpcQuestionData npcQuestionData;
  private NpcAnswerData npcAnswerData;
  private NpcTemplateData npcTemplateData;
  private NpcData npcData;
  private ObjectActionData objectActionData;
  private PetTemplateData petTemplateData;
  private QuestData questData;
  private QuestStepData questStepData;
  private QuestObjectiveData questObjectiveData;
  private ScriptedCellData scriptedCellData;
  private SpellData spellData;
  private TutorialData tutorialData;
  private ZaapData zaapData;
  private ZaapiData zaapiData;
  private HeroicMobsGroups heroicMobsGroups;
  private TitreData titreData;
  private QuestPlayerData questPlayerData;
  private PetData petData;
  private ObjectData objectData;
  private ObvejivanData obvejivanData;
  private MountData mountData;
  private GuildData guildData;
  private Bourse_kamasdata boursekamas;
  private OrnementsData ornementsData;
  private ShortcutsData shortcutsData;
  public void initializeData()
  {
    this.areaData=new AreaData(dataSource);
    this.gangsterData=new GangsterData(this.dataSource);
    this.bankData=new BankData(this.dataSource);
    this.trunkData=new TrunkData(this.dataSource);
    this.guildMemberData=new GuildMemberData(this.dataSource);
    this.hdvObjectData=new HdvObjectData(this.dataSource);
    this.houseData=new HouseData(this.dataSource);
    this.mountParkData=new MountParkData(this.dataSource);
    this.collectorData=new CollectorData(this.dataSource);
    this.prismData=new PrismData(this.dataSource);
    this.subAreaData=new SubAreaData(this.dataSource);
    this.animationData=new AnimationData(this.dataSource);
    this.areaData=new AreaData(this.dataSource);
    this.challengeData=new ChallengeData(this.dataSource);
    this.trunkData=new TrunkData(this.dataSource);
    this.craftData=new CraftData(this.dataSource);
    this.dungeonData=new DungeonData(this.dataSource);
    this.dropData=new DropData(this.dataSource);
    this.endFightActionData=new EndFightActionData(this.dataSource);
    this.experienceData=new ExperienceData(this.dataSource);
    this.extraMonsterData=new ExtraMonsterData(this.dataSource);
    this.fullMorphData=new FullMorphData(this.dataSource);
    this.giftData=new GiftData(this.dataSource);
    this.hdvData=new HdvData(this.dataSource);
    this.houseData=new HouseData(this.dataSource);
    this.interactiveDoorData=new InteractiveDoorData(this.dataSource);
    this.interactiveObjectData=new InteractiveObjectData(this.dataSource);
    this.objectTemplateData=new ObjectTemplateData(this.dataSource);
    this.objectSetData=new ObjectSetData(this.dataSource);
    this.jobData=new JobData(this.dataSource);
    this.mapData=new MapData(this.dataSource);
    this.monsterData=new MonsterData(this.dataSource);
    this.mountParkData=new MountParkData(this.dataSource);
    this.npcQuestionData=new NpcQuestionData(this.dataSource);
    this.npcAnswerData=new NpcAnswerData(this.dataSource);
    this.npcTemplateData=new NpcTemplateData(this.dataSource);
    this.npcData=new NpcData(this.dataSource);
    this.objectActionData=new ObjectActionData(this.dataSource);
    this.petTemplateData=new PetTemplateData(this.dataSource);
    this.questData=new QuestData(this.dataSource);
    this.questStepData=new QuestStepData(this.dataSource);
    this.questObjectiveData=new QuestObjectiveData(this.dataSource);
    this.scriptedCellData=new ScriptedCellData(this.dataSource);
    this.subAreaData=new SubAreaData(this.dataSource);
    this.spellData=new SpellData(this.dataSource);
    this.tutorialData=new TutorialData(this.dataSource);
    this.zaapData=new ZaapData(this.dataSource);
    this.zaapiData=new ZaapiData(this.dataSource);
    this.heroicMobsGroups=new HeroicMobsGroups(dataSource);
    this.titreData=new TitreData(dataSource);
    this.petData=new PetData(this.dataSource);
    this.objectData=new ObjectData(this.dataSource);
    this.obvejivanData=new ObvejivanData(this.dataSource);
    this.mountData=new MountData(this.dataSource);
    this.guildData=new GuildData(this.dataSource);
    this.questPlayerData = new QuestPlayerData(this.dataSource);
    this.boursekamas = new Bourse_kamasdata(this.dataSource);
    this.ornementsData = new OrnementsData(dataSource);
    this.shortcutsData = new ShortcutsData(this.dataSource);
  }

  public boolean initializeConnection()
  {
    try
    {
      logger.setLevel(Level.ALL);
      logger.trace("Reading database config");

      HikariConfig config=new HikariConfig();
      config.setJdbcUrl("jdbc:mysql://"+Config.getInstance().hostDB+":"+Config.getInstance().portDB+"/"+Config.getInstance().nameDB);
      config.addDataSourceProperty("databaseName",Config.getInstance().nameDB);
      config.addDataSourceProperty("user",Config.getInstance().userDB);
      config.addDataSourceProperty("password",Config.getInstance().passDB);
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      //config.setLeakDetectionThreshold(2000);
      config.setConnectionInitSql("SHOW TABLES;");
      config.setAllowPoolSuspension(true);
      config.setConnectionTestQuery("SHOW TABLES;");
      config.setMaxLifetime(60000);
      config.setIdleTimeout(60000);
      config.setKeepaliveTime(50000);
      config.setConnectionTimeout(2000);
      config.setMinimumIdle(5);
      config.setMaximumPoolSize(Config.getInstance().mysqlconnection());
      this.dataSource=new HikariDataSource(config);

      if(!Database.tryConnection(this.dataSource))
      {
        logger.error("Please check your username and password and database connection");
        Main.stop("try database connection failed");
        return false;
      }

      logger.info("Database connection established");
      this.initializeData();
      logger.info("Database data loaded");
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public HikariDataSource getDataSource()
  {
    return dataSource;
  }

  public GangsterData getGangsterData()
  {
    return gangsterData;
  }

  public BankData getBankData()
  {
    return bankData;
  }

  public GuildMemberData getGuildMemberData()
  {
    return guildMemberData;
  }

  public HdvObjectData getHdvObjectData()
  {
    return hdvObjectData;
  }

  public CollectorData getCollectorData()
  {
    return collectorData;
  }

  public PrismData getPrismData()
  {
    return prismData;
  }

  public AreaData getAreaData()
  {
    return areaData;
  }

  public AnimationData getAnimationData()
  {
    return animationData;
  }

  public ChallengeData getChallengeData()
  {
    return challengeData;
  }

  public TrunkData getTrunkData()
  {
    return trunkData;
  }

  public CraftData getCraftData()
  {
    return craftData;
  }

  public DungeonData getDungeonData()
  {
    return dungeonData;
  }

  public DropData getDropData()
  {
    return dropData;
  }

  public EndFightActionData getEndFightActionData()
  {
    return endFightActionData;
  }

  public ExperienceData getExperienceData()
  {
    return experienceData;
  }

  public ExtraMonsterData getExtraMonsterData()
  {
    return extraMonsterData;
  }

  public FullMorphData getFullMorphData()
  {
    return fullMorphData;
  }

  public GiftData getGiftData()
  {
    return giftData;
  }

  public HdvData getHdvData()
  {
    return hdvData;
  }

  public HouseData getHouseData()
  {
    return houseData;
  }

  public InteractiveDoorData getInteractiveDoorData()
  {
    return interactiveDoorData;
  }

  public InteractiveObjectData getInteractiveObjectData()
  {
    return interactiveObjectData;
  }

  public ObjectTemplateData getObjectTemplateData()
  {
    return objectTemplateData;
  }

  public ObjectSetData getObjectSetData()
  {
    return objectSetData;
  }

  public JobData getJobData()
  {
    return jobData;
  }

  public MapData getMapData()
  {
    return mapData;
  }

  public MonsterData getMonsterData()
  {
    return monsterData;
  }

  public MountParkData getMountParkData()
  {
    return mountParkData;
  }

  public NpcQuestionData getNpcQuestionData()
  {
    return npcQuestionData;
  }

  public NpcAnswerData getNpcAnswerData()
  {
    return npcAnswerData;
  }

  public NpcTemplateData getNpcTemplateData()
  {
    return npcTemplateData;
  }

  public NpcData getNpcData()
  {
    return npcData;
  }

  public ObjectActionData getObjectActionData()
  {
    return objectActionData;
  }

  public PetTemplateData getPetTemplateData()
  {
    return petTemplateData;
  }

  public QuestData getQuestData()
  {
    return questData;
  }

  public QuestStepData getQuestStepData()
  {
    return questStepData;
  }

  public QuestObjectiveData getQuestObjectiveData()
  {
    return questObjectiveData;
  }

  public ScriptedCellData getScriptedCellData()
  {
    return scriptedCellData;
  }
  public GuildData getGuildData()
  {
    return guildData;
  }
  public MountData getMountData()
  {
    return mountData;
  }

  public ObjectData getObjectData()
  {
    return objectData;
  }

  public ObvejivanData getObvejivanData()
  {
    return obvejivanData;
  }
  public PetData getPetData()
  {
    return petData;
  }

  public QuestPlayerData getQuestPlayerData()
  {
    return questPlayerData;
  }

  public SubAreaData getSubAreaData()
  {
    return subAreaData;
  }

  public SpellData getSpellData()
  {
    return spellData;
  }

  public TutorialData getTutorialData()
  {
    return tutorialData;
  }

  public ZaapData getZaapData()
  {
    return zaapData;
  }

  public ZaapiData getZaapiData()
  {
    return zaapiData;
  }

  public HeroicMobsGroups getHeroicMobsGroups()
  {
    return heroicMobsGroups;
  }
  public TitreData getTitreData()
  {
    return titreData;
  }
  public Bourse_kamasdata getboursekamasData()
  {
    return boursekamas;
  }
	public OrnementsData getOrnementsData() {
		return ornementsData;
	}
    public ShortcutsData getShortcutsData() {return shortcutsData;}

}
