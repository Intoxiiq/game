package soufix.game.scheduler.entity;

import soufix.entity.boss.Bandit;
import soufix.game.scheduler.Updatable;
import soufix.main.Config;
public class BanditRun extends Updatable
{
  public final static Updatable updatable=new Reboot(Config.getInstance().Bandit);
  public BanditRun(int wait)
  {
    super(wait);
  }

  public void update()
  {
      if(this.verify())
      {
    	  try{
    	
    			Bandit.run();
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