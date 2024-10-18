package soufix.game.scheduler.entity;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.Prism;
import soufix.game.scheduler.Updatable;
import soufix.main.Config;
import soufix.main.Main;
import soufix.object.GameObject;

import java.util.ArrayList;

public class WorldSave extends Updatable
{
  public final static Updatable updatable=new WorldSave(Config.getInstance().worldSaveUpdate);
  private static Thread thread;

  private WorldSave(int wait)
  {
    super(wait);
  }

  @Override
  public void update()
  {
    if(this.verify())
      if(!Main.isSaving)
      {
        thread=new Thread(() -> WorldSave.cast(1));
        thread.setName(WorldSave.class.getName());
        thread.setDaemon(true);
        thread.start();
      }
  }

  //v2.8 - heroic mobgroup save fix
  public static void cast(int trys)
  {
   // if(trys!=0)
    //  Main.gameServer.setState(2);

    try
    {
      Main.world.logger.debug("Starting the save of the world..");
      Main.gameServer.setState(2);
      SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1164;");
      Main.isSaving=true;

      /** Save of data **/
      Main.world.logger.info("-> of accounts bank.");
      Main.world.getAccounts().stream().filter(account -> account!=null).forEach(account -> Database.getDynamics().getBankData().update(account));

      Main.world.logger.info("-> of players.");
      Main.world.getPlayers().stream().filter(player -> player!=null).forEach(player -> {
    	  Database.getStatics().getPlayerData().update(player);
      });
      Main.world.logger.info("-> of prisms.");
      for(Prism prism : Main.world.getPrisms().values())
        if(Main.world.getMap(prism.getMap()).getSubArea().getPrismId()!=prism.getId())
          Database.getDynamics().getPrismData().delete(prism.getId());
        else
          Database.getDynamics().getPrismData().update(prism);

      Main.world.logger.info("-> of guilds.");
      Main.world.getGuilds().values().stream().forEach(guild -> Database.getDynamics().getGuildData().update(guild));

      Main.world.logger.info("-> of collectors.");
      Main.world.getCollectors().values().stream().filter(collector -> collector.getInFight()<=0).forEach(collector -> Database.getDynamics().getCollectorData().update(collector));

      Main.world.logger.info("-> of houses.");
      Main.world.getHouses().values().stream().filter(house -> house.getOwnerId()>0).forEach(house -> Database.getDynamics().getHouseData().update(house));

      Main.world.logger.info("-> of trunks.");
      Main.world.getTrunks().values().stream().forEach(trunk -> Database.getDynamics().getTrunkData().update(trunk));

      Main.world.logger.info("-> of parks.");
      Main.world.getMountparks().values().stream().filter(mp -> mp.getOwner()>0||mp.getOwner()==-1).forEach(mp -> Database.getDynamics().getMountParkData().update(mp));

      Main.world.logger.info("-> of mounts.");
      Main.world.getMounts().values().stream().forEach(mount -> Database.getDynamics().getMountData().update(mount));

      Main.world.logger.info("-> of areas.");
      Main.world.getAreas().values().stream().forEach(area -> Database.getDynamics().getAreaData().update(area));
      Main.world.getSubAreas().values().stream().forEach(subArea -> Database.getDynamics().getSubAreaData().update(subArea));

      Main.world.logger.info("-> of objects.");
      try
      {
        for(GameObject object : new ArrayList<>(Main.world.getGameObjects()))
        {
          if(object==null)
            continue;
          if(object.modification==0)
            Database.getDynamics().getObjectData().insert(object);
          else if(object.modification==1)
            Database.getDynamics().getObjectData().update(object);
          object.modification=-1;
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      Main.world.logger.info("-> of Shortcuts.");
      Main.world.getAllShortcuts().values().forEach(shortcuts -> Database.getDynamics().getShortcutsData().update(shortcuts));


      //Main.world.logger.info("-> of group-monsters.");

     /* List<Pair<Short, MobGroup>> groups=new ArrayList<Pair<Short, MobGroup>>();
      List<Pair<Short, MobGroup>> groupsFix=new ArrayList<Pair<Short, MobGroup>>();

      for(GameMap map : Main.world.getMaps())
      {
        for(MobGroup group : map.getMobGroups().values())
        {
          if(group.getIsDynamic())
            groups.add(new Pair<Short, MobGroup>(map.getId(),group));
          else if(group.isFix())
            groupsFix.add(new Pair<Short, MobGroup>(map.getId(),group));
        }
      }*/
      try
      {
        //Database.getDynamics().getHeroicMobsGroups().batchResetFix(groupsFix);
        //Database.getDynamics().getHeroicMobsGroups().batchReset(groups);
    	 // Main.world.logger.info("-> of Hdv Offline msg.");
         // Main.world.getAccounts().stream().filter(account -> account!=null).forEach(account ->  Database.getDynamics().getBankData().update_offline_HDV(account));
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }

      Main.world.logger.debug("The save has been done successfully !");
      SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1165;");
      Main.gameServer.setState(1);
    }
    catch(Exception exception)
    {
      exception.printStackTrace();
      Main.world.logger.error("Error when trying save of the world : "+exception.getMessage());
      if(trys<10)
      {
        Main.world.logger.error("Fail of the save, num of try : "+(trys+1)+".");
        WorldSave.cast(trys+1);
        return;
      }
      Main.isSaving=false;
    } finally
    {
      Main.isSaving=false;
    }

   // if(trys!=0)
   //   Main.gameServer.setState(1);

    if(thread!=null)
    {
      Thread copy=thread;
      thread=null;
      copy.interrupt();
    }
  }

  @Override
  public GameObject get()
  {
    return null;
  }
}