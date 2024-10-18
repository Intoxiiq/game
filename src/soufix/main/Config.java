package soufix.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.typesafe.config.ConfigFactory;

import soufix.database.Database;
import soufix.utility.Points;

public class Config
{
  private static final com.typesafe.config.Config configFile;
  public final long startTime=System.currentTimeMillis();
  public static final Config singleton = new Config();
  static {
      configFile = ConfigFactory.parseFile(new File("config.conf"));
  }

	public static Config getInstance() {
		return singleton;
	}

 public int numberOfThread()
	{
		if(Config.singleton.serverId == 1) {
			return 250;
		}
		else {
			return 100;	
		}
	}
 public int mysqlconnection()
	{
		if(Config.singleton.serverId == 1) {
			return 30;
		}
		else {
			return 25;	
		}
	}
  /** Text */
  public String name="Casper";
  public String discord="";
  public String startMessage="";
  public String colorMessage="ff0000";

  /** Server info */
  public boolean HALLOWEEN=false, NOEL=true; //events
  public boolean debugMode=false, useSubscribe=false;
  public boolean spion = false;
  public boolean Auto_event = true;
  public int    timePerEvent = 5;

  //maxconnections
  public int maxconnections()
	{
		if(Config.singleton.serverId == 6 ) {
			return 2;
		}
		else {
			return 16;	
		}
	}
  /** Starting info */
  public boolean allZaap=true, allEmote=true; //start options
  public int startLevel=1, startKamas=0;
  public int startMap=0, startCell=0; //if 0 choose basic class starting locations
  public int rateKamas=1, rateDrop=3, rateHonor=1, rateJob=5, rateFm=1, rateMount=60;
  public float rateXp=2;

  /** Config data */
  public boolean HEROIC=false;
  public int starHour=1; //how many hours for a mob to gain one star
  public int AIDelay=50, AIMovementCellDelay=50, AIMovementFlatDelay=50, craftDelay=10, gameActionDelay=5; //delay in ms
  public int weaponBonusBase=80, primaryWeaponBonus=30, secondaryWeaponBonus=20, daggerSwordNerf=5; //class weapon bonus
  public String lessDurationSpells=",16,20,95,98,148,149,577,612,741,760,811,813,962,1004,1092,1122,1129,1143,2079,2081,53,170,1164,1169,1172,2118,"; //spells with one turn less duration when selfcast, e.g. amplification, mutilation, clock
  public int erosion=10;
  public int basePods=5000;
  public int idleTime=1000*60*30; //20 minutes
  public int idleCheck=1000*60*5; //30 minutes
  public int starUpdate=1000*60*15; //updates stars of mobs in database every 2 hours
  public int averagePingUpdate=1000*60*10; //10 minutes
  public int moveEntityUpdate=1000*60*5; //3 minutes
  public int mountUpdate=1000*35; //35s minutes
  public int worldSaveUpdate=1000*60*60*3; //3 hours
  public int Worldvote = 1000*60*7; //7 minutes
  public int World_kick_threads = 1000*60*1; //30 seconds
  public int World_kick_Hikaricp = 1000*60*5; //30 seconds
  public int Fight_anti_bug = 1000*5*1; //5 seconds
  public int Session_kick = 1000*10*1; //4 seconds
  public int Reboot= 1000*60*50; //50 minutes
  public int Bandit= 1000*60*61; //60 minutes
  public int worldMarket=10111; //mapID of world market
  public Map<Integer, Integer> teleports=new HashMap<Integer, Integer>();
  public boolean mapAsBlocked=false, fightAsBlocked=false, tradeAsBlocked=false;
  public int doppleTime=1000*60*60*4; //24 hours between dopple fights
  public ArrayList<String> mountMaps=new ArrayList<String>(Arrays.asList("9358,4605,4606,4607,4609,4611,4613,4614,4615,4616,4617,4618,4620,4621,4622,4624,4625,4626,4627,4628,4629,4630,4631,4633,4634,4637,4639,4640,4644,4646,4647,4649,4666,4690,4705,4711,4720,4723,4729,4757,4805,4806,4809,4810,4851,4852,4855,4856,4930,4931,4932,4934,4935,4936,4937,4941,4966,4967,5036,5108,5111,5112,5113,5127,5133,5136,5139,5150,5277,5278,5279,5280,5304,5311,5317,5324,5326,5331,5333,5334,8479,8480,8564,8567,8570,8582,8598,8604,8607,8610,8743,8744,8745,8746,8747,8748,8749,8750,8751,8752,8753,8754,8755,8756,8757,8758,8759,8760,8770,8773,8774,8776,8778,8779,8780,8781,8783,8786,8788,8789,8791,8793,8794,8798,8799,8803,8804,8805,8806,8813,8814,8817,8818,8821,8822,8825,8826,8829,8830,8832,8833,8834,8837,8838,8848,8851,9152,9153,9154,9156,9157,9158,9159,9160,9162,9163,9164,9165,9166,9167,9168,9169,9204,9205,9207,9208,9209,9210,9211,9213,9216,9218,9219,9220,9222,9223,9225,9268,9270,9273,9274,9277,9278,9280,9281,9283,9285,9286,9287,9288,9289,9291,9292,9342,9345,9346,9349,9350,9352,9353,9354,9355,9356,9357,4603,4601,2209,2210,2215,2216,2218,2220,2221,2579,2679,2687,2688,2694,2708,2723,2813,2814,2842,2843,2879,2880,2924,2925,3072,3073,3079,3112,3113,3119,3142,3187,3242,3243,3273,3312,3328,3367,3440,3441,3479,3480,3488,3493,3529,3534,3643,3644,3663,3664,3672,3673,3684,3685,3704,3705,3713,3714,3736,3737,3771,3772,3781,3782,3786,3816,3817,3821,4070,4072,4077,4079,4082,4090,4093,4094,4096,4097,4104,4121,4122,4169,4170,4172,4173,4177,4178,4180,4181,4182,4204,4206,4207,4208,4209,4210,4211,4212,4213,4214,4215,4216,4217,4218,4219,4225,4229,4231,4232,4233,4236,4238,4240,4241,4242,4243,4245,4246,4248,4249,4250,4252,4258,4260,4261,4262,4264,4265,4269,4270,4271,4272,4273,4275,4278,4280,4282,4284,4285,4287,4289,4290,4291,4299,4300,4301,4302,4303,4304,4305,4308,4309,4336,4342,4347,4432,4436,4468,4471,4485,4488,4489,4491,4544,4545,4546,4547,4549,4562,4581,4582,4583,4584,4585,4586,4588,4589,4590,4591,4592,4593,4595,4596,4597,4598,4599,4600,9449,9450,9451,9453,9455,9456,9457,9458,9459,9460,9461,9462,9463,9464,9465,9466,9467,9725,9726,9727,9728,9729,9730,9731,9732,9733,9734,9735,9736,9737,9738,9739,9740,9741,9742,9743,9744,9745,9746,9747,9748,10249,10554,10557,10559,10561,10599,10600,10601,10602,10606,10607,10609,10611,10616,10618,10622,10630".split(",")));
  public int prix_color = 200;
  public int prix_name = 300;
  public int prix_titre = 150;
  public int prix_size = 120;
  public boolean bot_ok = false;
  ///
  public boolean cryptage = true;
  public boolean logs_by_data = true;
  
  /** Connection data */
  public String key;
  public int serverId, exchangePort;
  public String exchangeIp="";
  public String loginHostDB, loginNameDB, loginUserDB, loginPassDB, loginPortDB="3306";
  public int gamePort;
  public String hostDB, nameDB, userDB, passDB, portDB="3306";
  public String Ip; //set ip of server here
  public String boutique = ",12324,101620,11474,11475,25540,25541,25542,25543,25593,25594,25595,22006,22007,22008,4,22009,22005,10860,10861,10862,24470,10859,25475,25476,25477,25478,25479,25480,25481,25482,25483,25484,25485,25486,9636,9637,9638,9639,9640,9643,8948,9642,17918,17917,24464,684,1575,8012,24467,25012,25013,25014,6106660,6106661,6106662,6106663,6106664,6106665,7414,101193,101046,100913,100911,7520,11464,11465,11466,11467,6716,7518,7519,7522,7524,7704,7705,7706,7707,7708,7709,7710,7711,7712,7713,7714,7892,7911,8000,6106666,8151,8153,8155,8211,8561,8677,8693,9594,9617,9619,9620,9623,9624,9785,10106,10107,"
  		+ "6978,10865,10866,10802,8071,9582,10229,10228,10231,10234,10233,10237,10238,10239,10240,10241,10247,10252,10248,10257,10259,10242,10606,10244,10246,10253,10255,10260,10249,10230,10236,10262,10250,10251,10254,10261,10235,10245,10256,10266,10258,10267,10264,10263,10278,10265,10268,10270,10269,10271,10272,47031,101657,47023,47022,101287,10556,10169,22004,22028,22029,22030,10506,10507,10508,10509,10510,10511,10512,10513,9916,6966,6664,731,";
  
  
  
  public String boutique_pvp = ",25593,25594,25595,4,22009,10860,10861,10862,24470,10859,25475,25476,25477,25478,25479,25480,25481,25482,25483,25484,25485,25486,9636,9637,9638,9639,9640,9643,8948,1575,8012,24467,6106660,6106661,6106662,6106663,6106664,6106665,9544,9545,9546,9547,9548,10125,10126,10127,10133,10506,10507,10508,10509,10510,10511,10512,10513,9916,6966,6664,731,";
	  
  public String boutique_zoldik = ",25540,25541,25542,25543,25593,25594,25595,22006,22007,22008,4,22009,22005,10860,10861,10862,24470,10859,25475,25476,25477,25478,25479,25480,25481,25482,25483,25484,25485,25486,9636,9637,9638,9639,9640,9643,8948,9642,17918,17917,24464,684,1575,8012,24467,25012,25013,25014,6106660,6106661,6106662,6106663,6106664,6106665,7414,101193,101046,100913,100911,7520,11464,11465,11466,11467,6716,7518,7519,7522,7524,7704,7705,7706,7707,7708,7709,7710,7711,7712,7713,7714,7892,7911,8000,6106666,8151,8153,8155,8211,8561,8677,8693,9594,9617,9619,9620,9623,9624,9785,10106,10107,"
	  		+ "6978,10865,10866,10802,8071,9582,47031,101657,47023,47022,101287,10556,10169,22004,22028,22029,22030,10506,10507,10508,10509,10510,10511,10512,10513,9916,6966,6664,731,";
	  

  public Points points=new Points()
  {
    @Override
    public int load(int web)
    {
      return Database.getStatics().getAccountData().loadPointsWithoutUsersDb(web);
    }

    @Override
    public void update(int id, int points)
    {
      Database.getStatics().getAccountData().updatePointsWithoutUsersDb(id,points);
    }
  };
public int parchoMax = 101;

	public void set() {
		Main.isRunning = true;
		try {
			 exchangePort = Config.configFile.getInt("network.exchangePort");
	    		exchangeIp = Config.configFile.getString("network.exchangeIp");
	    		Ip = Config.configFile.getString("network.loginIp");
	    		gamePort = Config.configFile.getInt("network.gamePort");
	    		
    		
	    		hostDB = Config.configFile.getString("database.game_host");
	    		nameDB = Config.configFile.getString("database.game_loginName");
	    		userDB = Config.configFile.getString("database.game_user");
	    		passDB = Config.configFile.getString("database.game_password");
    		
	    		loginHostDB = Config.configFile.getString("database.login_host");
	    		loginNameDB = Config.configFile.getString("database.login_loginName");
	    		loginUserDB = Config.configFile.getString("database.login_user");
	    		loginPassDB = Config.configFile.getString("database.login_password");
    		//srv
    		serverId = Config.configFile.getInt("game.serveur_id");
    		if(serverId == 22)
    		this.HEROIC = true;	
    		key = Config.configFile.getString("game.serveur_key");
            Config.getInstance().rateDrop = Config.configFile.getInt("game.rate_drop"); 
    		Config.getInstance().rateFm = Config.configFile.getInt("game.rate_fm");
    		Config.getInstance().rateHonor = Config.configFile.getInt("game.rate_horor");
    		Config.getInstance().rateJob = Config.configFile.getInt("game.rate_job");
    		Config.getInstance().rateKamas = Config.configFile.getInt("game.rate_kamas");
    		Config.getInstance().rateXp = Config.configFile.getInt("game.rate_xp");
        }
        catch (Exception e) {
            System.out.println(" <> Config illisible ou champs manquants: " + e.getMessage());
            System.exit(1);
        }
	}
}