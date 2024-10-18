package soufix.events;

import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.common.PathFinding;
import soufix.common.SocketManager;
import soufix.entity.Npc;
import soufix.main.Main;

import java.util.Map;
import java.util.HashMap;

public class Start
{

  private Player player;
  private Npc helper;
  public boolean leave=false;
  private GameMap map;
  private Map<Integer, GameMap> mapUse=new HashMap<>();
  private Thread thread;

  public Start(Player player)
  {
    this.player=player;
    this.player.start=this;
    this.thread=new Thread(new starting());
    this.thread.start();
    new Thread(new verifyIsOnline()).start();
  }

  public class verifyIsOnline implements Runnable
  {
    @SuppressWarnings("deprecation")
    @Override
    public void run()
    {
      while(Start.this.player.isOnline())
        try
        {
          Thread.sleep(250);
        }
        catch(Exception ignored)
        {
        }

      player=null;
      helper=null;
      map=null;
      mapUse.clear();
      thread.interrupt();
      thread.stop();
    }
  }

  public class starting implements Runnable
  {
    @SuppressWarnings("deprecation")
    @Override
    public void run()
    {
      /** START : Construction de l'nvironement **/
      try
      {
        mapUse.put(1,Main.world.getMap((short)6824).getMapCopyIdentic());
        mapUse.put(2,Main.world.getMap((short)6826).getMapCopyIdentic());
        mapUse.put(3,Main.world.getMap((short)6828).getMapCopyIdentic());

        mapUse.get(1).getCase(329).addOnCellStopAction(999,"192","-1",mapUse.get(2));
        mapUse.get(1).getCase(325).addOnCellStopAction(999,"224","-1",Main.world.getMap((short)1863));
        mapUse.get(3).getCase(192).addOnCellStopAction(999,"389","-1",Main.world.getMap((short)6829));

        /** MAP 1 : Talk & Walk to start Fight **/
        try
        {
          Thread.sleep(2000);
        }
        catch(InterruptedException ignored)
        {
        }
        map=mapUse.get(1);
        helper=map.addNpc(15020,(short)211,3);
        player.setSpellsPlace(false);
        Start.this.player.unlearnSpell(661);
        Start.this.player.teleport(map,224);
        Start.this.player.setBlockMovement(true);
        SocketManager.GAME_SEND_ADD_NPC(Start.this.player,Start.this.helper);
        try
        {
          Thread.sleep(5000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Amakna Guardian","Welcome "+Start.this.player.getName()+", I am the guardian of Amakna.");
        try
        {
          Thread.sleep(4000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Amakna Guardian","I will ask you to make a choice...");
        try
        {
          Thread.sleep(4000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Amakna Guardian","If you want us to help you take your first steps in this world ...");
        try
        {
          Thread.sleep(4000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Amakna Guardian","... join my friend in the next room, he must be waiting for you ...");
        try
        {
          Thread.sleep(4000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Amakna Guardian","... to do this, walk on this transfer pad.");
        Start.this.player.send("Gf-1|329");
        try
        {
          Thread.sleep(4000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Amakna Guardian","Otherwise, if you want to discover the world by yourself, walk on the other tranfer pad.");
        Start.this.player.send("Gf-1|325");
        try
        {
          Thread.sleep(4000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Amakna Guardian","... join my friend in the next room, he must be waiting for you ...");
        Start.this.player.setBlockMovement(false);
        try
        {
          Thread.sleep(3000);
        }
        catch(InterruptedException ignored)
        {
        }

        int trying=0;
        while(Start.this.player.getCurMap().getId()==6824&&!leave)
        {
          try
          {
            Thread.sleep(2000);
          }
          catch(InterruptedException ignored)
          {
          }
          if(trying%10==1)
            SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Amakna Guardian","You have to walk on one of the two transfer pads in order to move on.");
          trying++;
        }

        if(leave)
        {
          SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Amakna Guardian","If you are sure you do not want any help, click on the transfer pad again.");
          try
          {
            Thread.sleep(3000);
          }
          catch(InterruptedException ignored)
          {
          }
          SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Amakna Guardian","Good luck!");
          player=null;
          helper=null;
          map=null;
          mapUse.clear();
          thread.interrupt();
          thread.stop();
          return;
        }

        map.RemoveNpc(helper.getId());
        map=mapUse.get(2);
        helper=map.addNpc(50000,(short)210,3);
        SocketManager.GAME_SEND_ADD_NPC_TO_MAP(Start.this.map,Start.this.helper);

        trying=0;
        while(Start.this.player.getCurMap().getId()==6824)
        {
          try
          {
            Thread.sleep(250);
          }
          catch(InterruptedException ignored)
          {
          }
          if(trying%80==1)
            SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Amakna Guardian","You have to walk on one of the two transfer pads to move on.");
          trying++;
        }

        SocketManager.GAME_SEND_ADD_NPC_TO_MAP(Start.this.map,Start.this.helper);
        Start.this.player.setBlockMovement(true);

        try
        {
          Thread.sleep(3000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","Enter the light so I can see what kind of warrior you are!");
        try
        {
          Thread.sleep(2000);
        }
        catch(InterruptedException ignored)
        {
        }

        String pathstr;
        try
        {
          pathstr=PathFinding.getShortestStringPathBetween(map,player.getCurCell().getId(),238,0);
        }
        catch(Exception e)
        {
          return;
        }
        if(pathstr==null)
          return;
        SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"0","1",player.getId()+"",pathstr);

        player.getCurCell().removePlayer(player);
        player.setCurCell(map.getCase(238));
        map.addPlayer(player);
        try
        {
          Thread.sleep(3000);
        }
        catch(InterruptedException ignored)
        {
        }
        player.set_orientation(7);
        SocketManager.GAME_SEND_eD_PACKET_TO_MAP(map,player.getId(),7);

        try
        {
          Thread.sleep(1000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","Well, let's see how to cast a spell.");
        try
        {
          Thread.sleep(4000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","To practice, I have lent you my training spell.");
        try
        {
          Thread.sleep(1500);
        }
        catch(InterruptedException ignored)
        {
        }
        Start.this.player.learnSpell(661,1,'b');
        player.setBlockMovement(false);
        try
        {
          Thread.sleep(2500);
        }
        catch(InterruptedException ignored)
        {
        }

        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","Take our friend the scarecrow...");
        try
        {
          Thread.sleep(500);
        }
        catch(InterruptedException ignored)
        {
        }
        map.spawnGroupOnCommand(224,"1003,1,1;",false);
        SocketManager.GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(map,player);

        try
        {
          Thread.sleep(2500);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","... Attack the scarecrow!");
        try
        {
          Thread.sleep(3000);
        }
        catch(InterruptedException ignored)
        {
        }

        boolean say=false;
        while(map.getMobGroups().size()>0)
        {
          if(!say)
            SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(map,"",-1,"Ganymede","To enter into combat, click on the sword.");
          try
          {
            Thread.sleep(1000);
          }
          catch(InterruptedException ignored)
          {
          }
          say=true;
        }

        say=true;
        while(player.getFight()!=null&&say)
        {
          if(player.getFight().isBegin())
            say=false;
          try
          {
            Thread.sleep(2000);
          }
          catch(InterruptedException ignored)
          {
          }
        }

        try
        {
          Thread.sleep(1000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"#",helper.getId(),"Ganymede","Let's see what this spell does...");
        try
        {
          Thread.sleep(3000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"#",helper.getId(),"Ganymede","To do that click on the spell I have you.");

        while(player.getFight()!=null)
          try
          {
            Thread.sleep(1500);
          }
          catch(InterruptedException ignored)
          {
          }

        player.setBlockMovement(true);

        try
        {
          Thread.sleep(5000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","You are now ready to start your first fight.");
        try
        {
          Thread.sleep(3000);
        }
        catch(InterruptedException ignored)
        {
        }
        Start.this.player.unlearnSpell(661);
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","I will take my training spell back now.");
        try
        {
          Thread.sleep(3000);
        }
        catch(InterruptedException ignored)
        {
        }
        mapUse.get(2).getCase(177).addOnCellStopAction(999,"388","-1",mapUse.get(3));
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","Follow me to the next room, you will have 3 new spells.");
        try
        {
          Thread.sleep(2000);
        }
        catch(InterruptedException ignored)
        {
        }
        player.setBlockMovement(false);

        try
        {
          pathstr=PathFinding.getShortestStringPathBetween(map,helper.getCellid(),177,0);
        }
        catch(Exception e)
        {
          return;
        }

        if(pathstr==null)
          return;
        SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"0","1",helper.getId()+"",pathstr);
        try
        {
          Thread.sleep(2000);
        }
        catch(InterruptedException ignored)
        {
        }

        map.RemoveNpc(helper.getId());
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(map,helper.getId());

        map=mapUse.get(3);
        map.addNpc(50001,(short)299,1);
        map.spawnGroupOnCommand(311,"432,1,1;",false);

        while(!map.getPlayers().contains(player))
          try
          {
            Thread.sleep(250);
          }
          catch(InterruptedException ignored)
          {
          }

        SocketManager.GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(map,player);
        player.setBlockMovement(true);
        try
        {
          Thread.sleep(3000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","Well, now you know how to fight against an enemy.");
        try
        {
          Thread.sleep(3000);
        }
        catch(InterruptedException ignored)
        {
        }
        player.setSpellsPlace(true);
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","I have given you your first three spells.");
        try
        {
          Thread.sleep(3000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","Use them to fight the Arachnee that is in this room.");
        try
        {
          Thread.sleep(3000);
        }
        catch(InterruptedException ignored)
        {
        }
        SocketManager.GAME_SEND_cMK_PACKET(player,"",helper.getId(),"Ganymede","If you manage to defeat the Arachnee, you will gain a level. Come back to see me as soon when you're level 2.");
        player.setBlockMovement(false);
        thread.interrupt();
      }
      catch(Exception ignored)
      {
      }
    }
  }
}