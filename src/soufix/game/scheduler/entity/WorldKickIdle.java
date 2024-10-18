package soufix.game.scheduler.entity;

import soufix.client.Player;
import soufix.game.GameClient;
import soufix.game.scheduler.Updatable;
import soufix.main.Config;
import soufix.main.Main;

import java.util.ArrayList;

public class WorldKickIdle extends Updatable
{
  public final static ArrayList<String> pubs=new ArrayList<>();
  public final static Updatable updatable=new WorldKickIdle(Config.getInstance().idleCheck);
  public WorldKickIdle(int wait)
  {
    super(wait);
  }

  //v2.8 - fixed idle gamethreads kicker
  public void update()
  {
	  
    if(this.verify())
    {
      for(Player player : Main.world.getOnlinePlayers())
      {
        if(player.getGameClient()==null)
        {
          player.resetVars();
          player.getAccount().resetAllChars();
          //Database.getStatics().getAccountData().update(player.getAccount());
        }
      }

      for(GameClient client : Main.gameServer.getClients())
      {
    	  if(client == null)
    		  continue;
        boolean found=false;
        for(Player player : Main.world.getOnlinePlayers())
        {
        	if(client.send_packet == false) {
        		client.kick();
        		continue;
        	}
        	if(client.getPlayer() != null)
          if(client.getPlayer()==player)
            if(!client.getCharacterSelect()==true)
            {
              found=true;
              break;
            }
        }
        if(!found)
        {
          if(!client.getCharacterSelect()==true)
            if(client!=null)
            {
              client.kick();
            }
        }
        else if(System.currentTimeMillis()-client.timeLastAct>Config.getInstance().idleTime)
          if(!client.getCharacterSelect()==true)
          {
            client.disconnect();
          }
         
      }
      //Logging.getInstance().write("Error","Purged "+idleClient+" idle client(s), "+idleSession+" idle session(s) and "+idlePlayer+" idle player(s).");
    }
     
  }

  @Override
  public Object get()
  {
    return null;
  }
}