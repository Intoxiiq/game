package soufix.main;

import org.fusesource.jansi.AnsiConsole;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import soufix.area.map.entity.InteractiveObject;
import soufix.database.Database;
import soufix.exchanger.ExchangeClient;
import soufix.game.GameServer;
import soufix.game.World;
import soufix.game.scheduler.entity.AveragePing;
import soufix.game.scheduler.entity.BanditRun;
import soufix.game.scheduler.entity.Fight_anti_bug;
//import soufix.game.scheduler.entity.Fille_att;
import soufix.game.scheduler.entity.MountUpdate;
import soufix.game.scheduler.entity.Reboot;
import soufix.game.scheduler.entity.Threads_kick_IAPLAY;
import soufix.game.scheduler.entity.WorldKickIdle;
import soufix.game.scheduler.entity.WorldPub;
import soufix.game.scheduler.entity.WorldSave;
import soufix.game.scheduler.entity.kolizeum;
import soufix.utility.LoggerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main
{
  public static Logger logger=(Logger)LoggerFactory.getLogger(Main.class);
  public static final List<Runnable> runnables=Collections.synchronizedList(new LinkedList<Runnable>());
  public static ExchangeClient exchangeClient;
  public static GameServer gameServer;
  public static boolean isRunning=false, isSaving=false , com_Running = false;
  public static World world;
  public static Scanner scanner=new Scanner(System.in);
  public static String FolderLogName = LoggerManager.getDate();
  public static boolean anti_ddos = true;
  public static boolean anti_ddos_ip = true;
  public static boolean reboot_lanced = false;
  public static long Out = 0;
  public static long In = 0;
  public static String relique_donjon  ;
  public static int  id_mob_relique_dj;
  public static int  anti_bug = 0;
  public static int Max_players	= 0;
  public static int nextId_items = -1;

  public static void main(String[] args) throws SQLException
  {
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        if(Main.isRunning)
        {
         Config.getInstance().set();
          Main.isRunning=false;
          //gameServer.setState(0);
          gameServer.setState(0);
          if(Main.gameServer!=null)
              Main.gameServer.kickAll();
          WorldSave.cast(0);
          try
          {
         
         }
          catch(Exception e)
          {
            e.printStackTrace();
          }
          Logging.getInstance().stop();
          Database.getStatics().getServerData().loggedZero();
          Database.getStatics().getServerData().loggedZero_accounts();
        }
        Main.logger.info("The server is now closed.");
      }
    });

    try
    {
      System.setOut(new PrintStream(System.out,true,"IBM850"));
      if (!new File("Logs").exists())
          new File("Logs").mkdir();
      if(!new File("Logs/Error").exists())
        new File("Logs/Error").mkdir();
      if (!new File("Logs/Ip_logs").exists())
          new File("Logs/Ip_logs").mkdir();
      if (!new File("Logs/EUUREBDD").exists())
          new File("Logs/EUUREBDD").mkdir();
      if (!new File("Logs/DDOS").exists())
          new File("Logs/DDOS").mkdir();
      if (!new File("Logs/AccountIpConnect").exists())
          new File("Logs/AccountIpConnect").mkdir();
      LoggerManager.checkFolder("Logs/Ip_logs/" + FolderLogName);
      System.setErr(new PrintStream(new FileOutputStream("Logs/Error/"+new SimpleDateFormat("dd-MM-yyyy - HH-mm-ss",Locale.FRANCE).format(new Date())+".log")));
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    Main.start();
  }

  public static void start()
  {
	  Config.getInstance().set();
    Main.setTitle("Casper - Loading data..");
    Main.logger.info("You use "+System.getProperty("java.vendor")+" with the version "+System.getProperty("java.version"));
    Main.logger.debug("Starting of the server : "+new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss",Locale.FRANCE).format(new Date()));
    Main.logger.debug("Current timestamp : "+System.currentTimeMillis());
    Logging.getInstance().initialize();

    if(Database.launchDatabase())
    {
      Main.isRunning=true;
      world=new World();
      Main.world.createWorld();

      gameServer=new GameServer();
      gameServer.start();
      exchangeClient = new ExchangeClient();
      Main.refreshTitle();
      //gameServer.setState(1);
      Main.logger.info("The server is ready ! Waiting for connection..\n");
      if(!Config.getInstance().debugMode)
      {
        ch.qos.logback.classic.Logger root=(ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.OFF);
      }
      Config.getInstance().teleports.put(1868,316);
		
  	new soufix.command.ConsoleInputAnalyzer();
  	Star_load();
    }
    else
    {
      Main.logger.error("An error occurred when the server have try a connection on the Mysql server. Please check your identification.");
    }
  }
  
  public static void Star_load () {
      while(Main.isRunning)
      {
        try
        {
          WorldSave.updatable.update();
          WorldPub.updatable.update();
          WorldKickIdle.updatable.update();
          AveragePing.updatable.update();
          //MoveMobs.updatable.update();
          MountUpdate.updatable.update();
          InteractiveObject.updatable.update();
          Threads_kick_IAPLAY.updatable.update();
          // Krala.updatable.update();
          Reboot.updatable.update();
          BanditRun.updatable.update();
          Fight_anti_bug.updatable.update();
          kolizeum.updatable.update();
          //Fille_att.updatable.update();
          //Worldvotesp.updatable.update();
          //Worldvoterpg.updatable.update();
          refreshTitle();
          if(!runnables.isEmpty())
          {
            List<Runnable> copyRunnables=new LinkedList<Runnable>(runnables);
            List<Runnable> toRemove=new LinkedList<Runnable>(runnables);
            for(Runnable runnable : copyRunnables)
            {
              try
              {
                if(runnable!=null)
                {
                  new Thread(runnable).start();
                  toRemove.add(runnable);
                }
              }
              catch(Exception e)
              {
                e.printStackTrace();
              }
            }
            runnables.removeAll(toRemove);
          }
          Thread.sleep(1000);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          Star_load();
          
        }

      }  
	  
  }
  
  public static void getRandomRelique_donjon(){
      int r = (int) (Math.random()*10);
      String name = new String [] {"Donjon Bouftou:147","Donjon Squelettes:396","Donjon Craqueleur:669","Donjon Blop Multicolore:1188"
    		  ,"Donjon Chene Mou:257","Donjon Bulbes:519" ,"Donjon Rat Noir:939","Donjon Canidés:232","Laboratoire du Tynril:1086","Goulet du Rasboul:1071"}[r];
      String[] split=name.split(":");
      relique_donjon = split[0] ;
      id_mob_relique_dj = Integer.parseInt(split[1]);
  }


  public static void stop(String reason)
  {
    Logging.getInstance().write("Error",reason);
    System.exit(0);
  }

  private static void setTitle(String title)
  {
    AnsiConsole.out.printf("%c]0;%s%c",'\033',title,'\007');
  }

  public static void refreshTitle()
  {
    if(Main.isRunning)
      Main.setTitle(gameServer.getClients().size()+" Session(s) | "+Main.world.getOnlinePlayers().size()+" Player(s) | "+Thread.getAllStackTraces().keySet().size()+" Thread(s) | Free-Ram: "+Runtime.getRuntime().freeMemory()/(1024*1024)+" Mo. Out "+ Out + " In " + In);
  }

  public static void clear()
  {
    AnsiConsole.out.print("\033[H\033[2J");
  }

}
