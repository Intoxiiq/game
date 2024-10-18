package soufix.client.other;

import soufix.area.map.GameCase;
import soufix.client.Player;
import soufix.common.PathFinding;
import soufix.common.SocketManager;
import soufix.utility.TimerWaiterPlus;

import java.util.ArrayList;
public class Party
{

  private final Player chief;
  private Player master;
  private Player One_windows = null;
private final  ArrayList<Player> players=new ArrayList<>();

  public Party(Player p1, Player p2)
  {
    this.chief=p1;
    this.players.add(p1);
    this.players.add(p2);
  }

  public ArrayList<Player> getPlayers()
  {
    return this.players;
  }

  public Player getChief()
  {
    return this.chief;
  }

  public boolean isChief(int id)
  {
    return this.chief.getId()==id;
  }

  public Player getMaster()
  {
    return master;
  }

  public void setMaster(Player master)
  {
    this.master=master;
  }

  public void addPlayer(Player player)
  {
    this.players.add(player);
  }

  public void leave(Player player)
  {
    if(!this.players.contains(player))
      return;

    player.follow=null;
    player.follower.clear();
    player.setParty(null);
    TimerWaiterPlus.addNext(() -> {
    this.players.remove(player);
    },130);
    TimerWaiterPlus.addNext(() -> {
    for(Player member : this.players)
    {
    	if(member == null)
    		continue;
      if(member.follow==player)
        member.follow=null;
      if(member.follower.containsKey(player.getId()))
        member.follower.remove(player.getId());
    }

    if(this.players.size()==1)
    {
      this.players.get(0).setParty(null);
      if(this.players.get(0).getAccount()==null||this.players.get(0).getGameClient()==null)
        return;
      SocketManager.GAME_SEND_PV_PACKET(this.players.get(0).getGameClient(),"");
    } else
    {
      SocketManager.GAME_SEND_PM_DEL_PACKET_TO_GROUP(this,player.getId());
    }
    },180);
  }
  public void clear_groupe()
  {
    for (Player player : this.players) {
    player.follow=null;
    player.follower.clear();
    player.setParty(null);
    player.setOne_windows(false);
     SocketManager.GAME_SEND_PM_DEL_PACKET_TO_GROUP(this,player.getId());
     SocketManager.GAME_SEND_PV_PACKET(player.getGameClient(),"");
  }
  this.players.clear();  
  }

  public void moveAllPlayersToMaster(final GameCase cell)
  {
    if(this.master!=null)
    {
      this.players.stream().filter((follower1) -> isWithTheMaster(follower1,false)).forEach(follower -> follower.setBlockMovement(true));
      this.players.stream().filter((follower1) -> isWithTheMaster(follower1,false)).forEach(follower -> {
        try
        {
          final GameCase newCell=cell!=null ? cell : this.master.getCurCell();
          String path=PathFinding.getShortestStringPathBetween(this.master.getCurMap(),follower.getCurCell().getId(),newCell.getId(),0);
          if(path!=null)
          {
            follower.getCurCell().removePlayer(follower);
            follower.setCurCell(newCell);
            follower.setOldCell(newCell.getId());
            follower.getCurCell().addPlayer(follower);

            SocketManager.GAME_SEND_GA_PACKET_TO_MAP(follower.getCurMap(),"0",1,String.valueOf(follower.getId()),path);
          }
        }
        catch(Exception ignored)
        {
        }
      });
      this.players.stream().filter((follower1) -> isWithTheMaster(follower1,false)).forEach(follower -> follower.setBlockMovement(false));
    }
  }
  public Player getOne_windows() {
	return One_windows;
}

public void setOne_windows(Player one_windows) {
	One_windows = one_windows;
}
  public boolean isWithTheMaster(Player follower, boolean inFight)
  {
    return follower!=null&&!follower.getName().equals(this.master.getName())&&this.players.contains(follower)&&follower.getGameClient()!=null&&this.master.getCurMap().getId()==follower.getCurMap().getId()&&(inFight ? follower.getFight()==this.master.getFight() : follower.getFight()==null);
  }
  public boolean isWithTheMaster2(Player follower, boolean inFight)
  {
    return follower!=null&&!follower.getName().equals(this.master.getName())&&this.players.contains(follower)&&follower.getGameClient()!=null&&(inFight ? follower.getFight()==this.master.getFight() : follower.getFight()==null);
  }
}
