package soufix.game.scheduler.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import soufix.game.scheduler.Updatable;
import soufix.main.Config;
import soufix.main.Main;
public class Krala extends Updatable
{
  public final static Updatable updatable=new Krala(Config.getInstance().World_kick_threads);

  public Krala(int wait)
  {
    super(wait);
  }

  public void update()
  {
      if(this.verify())
      {
    	  try{
    		  Date actDate = new Date();
    			DateFormat dateFormat = new SimpleDateFormat("dd");
    			dateFormat = new SimpleDateFormat("HH");
    			int heure = Integer.parseInt(dateFormat.format(actDate));
    			dateFormat = new SimpleDateFormat("mm");
    			int min = Integer.parseInt(dateFormat.format(actDate));
    			if (heure == 1 && min == 0)
    			{
    				Main.world.getMap((short)10700).closeKrala();
    			}
    			if (heure == 4 && min == 0)
    			{
    				Main.world.getMap((short)10700).openKrala();
    			}
    			if (heure == 5 && min == 0)
    			{
    				Main.world.getMap((short)10700).closeKrala();
    			}
    			if (heure == 8 && min == 8)
    			{
    				Main.world.getMap((short)10700).openKrala();
    			}
    			if (heure == 9 && min == 0)
    			{
    				Main.world.getMap((short)10700).closeKrala();
    			}
    			if (heure == 12 && min == 0)
    			{
    				Main.world.getMap((short)10700).openKrala();
    			}
    			if (heure == 13 && min == 0)
    			{
    				Main.world.getMap((short)10700).closeKrala();
    			}
    			if (heure == 16 && min == 0)
    			{
    				Main.world.getMap((short)10700).openKrala();
    			}
    			if (heure == 17 && min == 0)
    			{
    				Main.world.getMap((short)10700).closeKrala();
    			}
    			if (heure == 20 && min == 0)
    			{
    				Main.world.getMap((short)10700).openKrala();
    			}
    			if (heure == 21 && min == 0)
    			{
    				Main.world.getMap((short)10700).closeKrala();
    			}
    			if (heure == 00 && min == 0)
    			{
    				Main.world.getMap((short)10700).openKrala();
    			}
    		
          	} catch (Exception e2) {
      		}
      }
  }

  @Override
  public Object get()
  {
    return null;
  }
}