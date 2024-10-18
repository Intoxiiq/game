package soufix.game.scheduler.entity;

import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.game.GameClient;
import soufix.game.scheduler.Updatable;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.utility.TimerWaiterPlus;
public class Fight_anti_bug extends Updatable
{
  public final static Updatable updatable=new Fight_anti_bug(Config.getInstance().Fight_anti_bug);

  public Fight_anti_bug(int wait)
  {
    super(wait);
  }

public void update()
  {
      if(this.verify())
      {
    	  try{
    		  String liste_perso = "";
    		  for(GameMap map : Main.world.getMaps()) {
    			  if(map == null)
    				  continue;
    		  for(Fight fight : map.getFights()) {
    			  if(fight == null)
    				  continue;
    		  for(Fighter fighter : fight.getFighters3()) {
    			  if(fighter == null)
    				  continue;	
    			  if(fighter.getPersonnage() == null)
    				  continue;
    			  if(liste_perso.contains(";"+fighter.getPersonnage().getId()+";"))
    			  {
    				  
    				  fighter.getPersonnage().getAccount().setBanned(true);
    	              //Database.getStatics().getAccountData().update(fighter.getPersonnage().getAccount()); 
    	    	        SocketManager.send(fighter.getPersonnage(),"Im1201;AntiHack");
    	    	        TimerWaiterPlus.addNext(() -> {
    	    	        	fight.endFight(false,true);
    	    	        	if(fighter.getPersonnage().getGameClient()!= null)
    	    	        		fighter.getPersonnage().getGameClient().kick(); 	
    	   			 }, 2000);	  
    			  }
    			  liste_perso += ";"+fighter.getPersonnage().getId()+";";
    		  }
    			  
    		  }
    			  
    		  }
    		  for(GameClient client : Main.gameServer.getClients())
    	      {
    			  if(client == null)
    				  continue;
    			  if(client.send_packet == false) {
    				  TimerWaiterPlus.addNext(() -> {
    				client.kick();
    				  },400);
    				continue;
    			  }
    			  if(client.getPlayer() == null)
    				  continue;
    	        Player player=client.getPlayer();
    	        if(player==null || player.getFight()==null)
    	          continue;
    	        if(player.getFight().getState() != Constant.FIGHT_STATE_ACTIVE) {
    	        	if(player.getFight().getType() == Constant.FIGHT_TYPE_PVM)
    	        	if(player.getFight().getLaunchTime()+50000 < System.currentTimeMillis()) {
    	        		player.getFight().Anti_bug();
    	        	}
    	        	continue;	
    	        }
    	        if(player.getFight().getState() != Constant.FIGHT_STATE_ACTIVE)
    	        	continue;
    	        if(player.getFight().getFighterByOrdreJeu() == null) {
    	        	player.getFight().endTurn(false);
    	        continue;	
    	        }
    	        if(player.getFight().getFighterByOrdreJeu().start_turn+45000 < System.currentTimeMillis()) {
    	         player.getFight().endTurn(false);
    	        }
    	      }
    	
          	} catch (Exception e) {
          		e.printStackTrace();
      		}
      }
  }

  @Override
  public Object get()
  {
    return null;
  }
}