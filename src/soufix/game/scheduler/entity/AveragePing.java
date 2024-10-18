package soufix.game.scheduler.entity;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.game.scheduler.Updatable;
import soufix.main.Config;
import soufix.main.Main;

public class AveragePing extends Updatable
{
  public final static Updatable updatable=new AveragePing(Config.getInstance().averagePingUpdate);
  public AveragePing(int wait)
  {
    super(wait);
  }

  //v2.8 - fixed idle gamethreads kicker
  public void update()
  {
    if(this.verify())
      for(Player player : Main.world.getOnlinePlayers())
        SocketManager.sendAveragePingPacket(player.getGameClient());
  }

  @Override
  public Object get()
  {
    return null;
  }
}
