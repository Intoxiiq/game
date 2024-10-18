package soufix.game.scheduler.entity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import soufix.command.CommandAdmin;
import soufix.command.administration.AdminUser;
import soufix.game.scheduler.Updatable;
import soufix.main.Config;
import soufix.main.Main;
public class Reboot extends Updatable
{
  public final static Updatable updatable=new Reboot(Config.getInstance().Reboot);
  private AdminUser adminUser;
  public Reboot(int wait)
  {
    super(wait);
  }

  public void update()
  {
      if(this.verify())
      {
    	  try{
    		  this.adminUser=new CommandAdmin(null);
    		  Date actDate = new Date();
    			DateFormat dateFormat = new SimpleDateFormat("dd");
    			dateFormat = new SimpleDateFormat("HH");
    			int heure = Integer.parseInt(dateFormat.format(actDate));
    			dateFormat = new SimpleDateFormat("mm");
    			if (heure == 5)
    			{
    				if(!Main.reboot_lanced) {
    				this.adminUser.apply("BAshutdown 1 20",true);
    				Main.reboot_lanced = true;
    				}
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