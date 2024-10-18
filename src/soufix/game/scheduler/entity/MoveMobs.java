package soufix.game.scheduler.entity;

import soufix.entity.npc.NpcMovable;
import soufix.game.GameClient;
import soufix.game.scheduler.Updatable;
import soufix.main.Config;
import soufix.main.Main;

public class MoveMobs extends Updatable
{
  public final static Updatable updatable=new MoveMobs(Config.getInstance().moveEntityUpdate);
  public MoveMobs(int wait)
  {
    super(wait);
  }

  public void update()
  {
    if(this.verify())
    {
      for(GameClient client : Main.gameServer.getClients())
        if(client.getPlayer()!=null) {
          client.getPlayer().getCurMap().moveMobGroups(5);
      NpcMovable.moveAll();
        }
    }
  }

  @Override
  public Object get()
  {
    return null;
  }
}
