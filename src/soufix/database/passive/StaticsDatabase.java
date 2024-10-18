package soufix.database.passive;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import soufix.database.Database;
import soufix.database.passive.data.AccountData;
import soufix.database.passive.data.AreaData;
import soufix.database.passive.data.BanIpData;
import soufix.database.passive.data.CommandData;
import soufix.database.passive.data.GroupData;
import soufix.database.passive.data.HouseData;
import soufix.database.passive.data.MountParkData;
import soufix.database.passive.data.PlayerData;
import soufix.database.passive.data.PubData;
import soufix.database.passive.data.ServerData;
import soufix.database.passive.data.SuccesplayerData;
import soufix.database.passive.data.TrunkData;
import soufix.main.Config;
import soufix.main.Main;
import soufix.database.passive.data.TiendaCategoriaData;
import soufix.database.passive.data.TiendaObjetosData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.LoggerFactory;

public class StaticsDatabase
{
  //connection
  private HikariDataSource dataSource;
  private Logger logger=(Logger)LoggerFactory.getLogger(StaticsDatabase.class);
  //data
  private AccountData accountData;
  private CommandData commandData;
  private PlayerData playerData;
  private ServerData serverData;
  private BanIpData banIpData;
  private AreaData areaData;
  private GroupData groupData;
  private HouseData houseData;
  private TrunkData trunkData;
  private MountParkData mountParkData;
  private PubData pubData;
  private SuccesplayerData succesdata;
  private TiendaCategoriaData tiendaCategoriaData;
  private TiendaObjetosData tiendaObjetosData;

  public void initializeData()
  {
    this.accountData=new AccountData(this.dataSource);
    this.commandData=new CommandData(this.dataSource);
    this.playerData=new PlayerData(this.dataSource);
    this.serverData=new ServerData(this.dataSource);
    this.banIpData=new BanIpData(this.dataSource);
    this.areaData=new AreaData(this.dataSource);
    this.groupData=new GroupData(this.dataSource);
    this.houseData=new HouseData(this.dataSource);
    this.trunkData=new TrunkData(this.dataSource);
    this.mountParkData=new MountParkData(this.dataSource);
    this.pubData=new PubData(this.dataSource);
    this.succesdata=new SuccesplayerData(this.dataSource);
	this.tiendaCategoriaData = new TiendaCategoriaData(this.dataSource);
	this.tiendaObjetosData = new TiendaObjetosData(this.dataSource);
  }

  public boolean initializeConnection()
  {
    try
    {
      logger.setLevel(Level.ALL);
      logger.trace("Reading database config");
      HikariConfig config=new HikariConfig();
      config.setJdbcUrl("jdbc:mysql://"+Config.getInstance().loginHostDB+":"+Config.getInstance().loginPortDB+"/"+Config.getInstance().loginNameDB);
      config.addDataSourceProperty("user",Config.getInstance().loginUserDB);
      config.addDataSourceProperty("password",Config.getInstance().loginPassDB);
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      //config.setLeakDetectionThreshold(2000);
      config.setConnectionInitSql("SHOW TABLES;");
      config.setAllowPoolSuspension(true);
      config.setConnectionTestQuery("SHOW TABLES;");
      config.setMaxLifetime(60000);
      config.setIdleTimeout(60000);
      config.setConnectionTimeout(2000);
      config.setKeepaliveTime(50000);
      config.setMinimumIdle(5);
      config.setMaximumPoolSize(Config.getInstance().mysqlconnection());
      this.dataSource=new HikariDataSource(config);

      if(!Database.tryConnection(this.dataSource))
      {
        logger.error("Please check your username and password and database connection");
        Main.stop("statics try connection failed");
        return false;
      }
      logger.info("Database connection established");
      initializeData();
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

  public AccountData getAccountData()
  {
    return accountData;
  }

  public CommandData getCommandData()
  {
    return commandData;
  }

  public PlayerData getPlayerData()
  {
    return playerData;
  }

  public ServerData getServerData()
  {
    return serverData;
  }

  public BanIpData getBanIpData()
  {
    return banIpData;
  }

  public AreaData getAreaData()
  {
    return areaData;
  }


  public GroupData getGroupData()
  {
    return groupData;
  }

  public HouseData getHouseData()
  {
    return houseData;
  }

  public TrunkData getTrunkData()
  {
    return trunkData;
  }

  public MountParkData getMountParkData()
  {
    return mountParkData;
  }


  public PubData getPubData()
  {
    return pubData;
  }
  public SuccesplayerData getSuccesData()
  {
    return succesdata;
  }
	public TiendaCategoriaData getCategoriaData() {
		return tiendaCategoriaData;
	}

	public TiendaObjetosData getObjetosData() {
		return tiendaObjetosData;
	}
}
