package soufix.main;

import java.util.ArrayList;
import java.util.List;

import soufix.client.Player;
import soufix.client.other.Stats;
import soufix.common.SocketManager;
import soufix.object.ObjectTemplate;

public class Boutique
{
  public static final List<ObjectTemplate> items=new ArrayList<>();
  private static String packet;

  public static void initPacket()
  {
    packet=getObjectList();
  }

  public static void open(Player player)
  {
    if(player.getFight()==null)
    {
      player.boutique=true;
      SocketManager.send(player,"ECK0|1");
      SocketManager.send(player,"EL"+packet);
      if(player.getKamas() < 5000)
			SocketManager.GAME_SEND_STATS_BOUTIQUE_PACKET(player);
      player.sendMessage("Vous avez <b>" + player.getAccount().getPoints() + "</b> points boutique");
    } else
    {
      SocketManager.GAME_SEND_MESSAGE(player,"Vous ne pouvez pas ouvrir le Shop pendant le combat.");
    }
  }

  private static String getObjectList()
  {
    StringBuilder items=new StringBuilder();
    for(ObjectTemplate obj : Boutique.items)
    {
    	if(obj == null)continue;
      Stats stats=obj.generateNewStatsFromTemplate(obj.getStrTemplate(),true);
      items.append(obj.getId()+";"+stats.parseToItemSetStats()).append("|");

    }
    return items.toString();
  }
}
