package soufix.main;

import java.util.ArrayList;
import java.util.List;

import soufix.client.Player;
import soufix.client.other.Stats;
import soufix.common.SocketManager;
import soufix.object.ObjectTemplate;

public class Tokenshop
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
      player.tokenShop=true;
      SocketManager.send(player,"ECK0|1");
      SocketManager.send(player,"EL"+packet);
    } else
    {
      SocketManager.GAME_SEND_MESSAGE(player,"You can not open the tokenshop while in combat.");
    }
  }

  private static String getObjectList()
  {
    StringBuilder items=new StringBuilder();
    for(ObjectTemplate obj : Tokenshop.items)
    {
      Stats stats=obj.generateNewStatsFromTemplate(obj.getStrTemplate(),true);
      items.append(obj.getId()+";"+stats.parseToItemSetStats()).append("|");
    }
    return items.toString();
  }
}
