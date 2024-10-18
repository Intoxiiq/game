package soufix.game.scheduler.entity;

import soufix.area.map.GameMap;
import soufix.entity.mount.Mount;
import soufix.game.scheduler.Updatable;
import soufix.main.Config;
import soufix.main.Main;

public class MountUpdate extends Updatable
{
  public final static Updatable updatable=new MountUpdate(Config.getInstance().mountUpdate);
  public MountUpdate(int wait)
  {
    super(wait);
  }

  public void update()
  {
    if(this.verify())
    {
      for(Mount mount : Main.world.getMounts().values())
      {
        if(mount.getFatigue()<=0)
          continue;
        mount.setFatigue(mount.getFatigue()-3);
        if(mount.getFatigue()<0)
          mount.setFatigue(0);
      }

      for(String s : Config.getInstance().mountMaps)
      {
        short mapId=(short)Integer.parseInt(s);
        GameMap map=Main.world.getMap(mapId);
        map.getMountPark().startMoveMounts();
      }
    }
  }

  @Override
  public Object get()
  {
    return null;
  }
}
