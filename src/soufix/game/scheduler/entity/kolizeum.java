package soufix.game.scheduler.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.fight.Fight;
import soufix.game.World;
import soufix.game.scheduler.Updatable;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.utility.TimerWaiterPlus;
public class kolizeum extends Updatable
{
  public final static Updatable updatable=new kolizeum(1000*15*1);

  public kolizeum(int wait)
  {
    super(wait);
  }

public void update()
  {
      if(this.verify())
      {
    	  try{
    		  World.actualizarRankings();
    		  if(Main.world.Koli_players == null || Main.world.Koli_players.size() == 1)
    			 return; 
    		  
    		  String enfightnow = "";
    		  for(Player perso : Main.world.Koli_players)
        	  {
    			  if(perso == null || !perso.isOnline() || perso.getFight() != null)
    				  continue;
    			  if(enfightnow.contains("."+perso.getId()+"."))
    				  continue;
    			 Player Target = null;
    			 for(Player target2 : Main.world.Koli_players)
           	     {
    				 if(enfightnow.contains("."+target2.getId()+"."))
       				  continue;
    				 if(target2 == null || !target2.isOnline() || target2.getFight() != null || perso.getId() == target2.getId())
       				  continue;	 
   			    if(perso.getAccount().getCurrentIp().compareTo(target2.getAccount().getCurrentIp()) == 0)
			     continue;
			      if(perso.getAccount().restriction.koli.containsKey(target2.getAccount().getCurrentIp()))
			      {
			        if((System.currentTimeMillis()-perso.getAccount().restriction.koli.get(target2.getAccount().getCurrentIp()))<1000*60*25)
			        {
			        	 continue;
			        }
			        else
			         perso.getAccount().restriction.koli.remove(target2.getAccount().getCurrentIp());
			         target2.getAccount().restriction.koli.remove(perso.getAccount().getCurrentIp());
			      }
			      int level = target2.getLevel() - perso.getLevel();
			      if(level < 0)
			    	  level = perso.getLevel() - target2.getLevel();
    			  	 if(level <= 10) {
    			  		Target = target2; 
    			  		break;
    			  	 }	 
           	     }
    			 if(Target == null)
    			 continue;
    			    List<Integer> givenList = Arrays.asList(952, 7423, 1869, 9544, 10423);
    			    Random rand = new Random();
    			    int randomElement = givenList.get(rand.nextInt(givenList.size()));
    			    int map = randomElement;
    			    
    			    perso.setOldMap( perso.getCurMap().getId());
    		          perso.setOldCell( perso.getCurCell().getId());
    		          
    		          Target.setOldMap(Target.getCurMap().getId());
    		          Target.setOldCell(Target.getCurCell().getId());

    			  perso.teleport((short) map, 200);
    			  Target.teleport((short) map, 200);
    	
    			  
    			  final Player target = Target;
    			  enfightnow += "."+target.getId()+".";
    			  enfightnow += "."+perso.getId()+".";

			      perso.getAccount().restriction.koli.put(target.getAccount().getCurrentIp(),System.currentTimeMillis());

			      target.getAccount().restriction.koli.put(target.getAccount().getCurrentIp(),System.currentTimeMillis());

			      TimerWaiterPlus.addNext(() -> {
    				  Main.world.removeKoli_players(target);
        			  Main.world.removeKoli_players(perso);
    				    SocketManager.GAME_SEND_MAP_START_DUEL_TO_MAP(perso.getCurMap(),target.getId(),perso.getId());
    				    Fight fight=perso.getCurMap().newFight(perso,target,Constant.FIGHT_TYPE_KOLI);
    				    perso.setFight(fight);
    				    perso.setAway(false);
    				    target.setFight(fight);
    				    target.setAway(false);
    			    },2000);
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