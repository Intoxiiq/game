package soufix.game.scheduler.entity;

import soufix.game.scheduler.Updatable;
import soufix.main.Config;
public class Threads_kick_IAPLAY extends Updatable
{
  public final static Updatable updatable=new Threads_kick_IAPLAY(Config.getInstance().World_kick_threads);

  public Threads_kick_IAPLAY(int wait)
  {
    super(wait);
  }

public void update()
  {
      if(this.verify())
      {
    	  try{
    		  
    		  ThreadGroup threadg = Thread.currentThread().getThreadGroup();
          	Thread[] threads = new Thread[threadg.activeCount()];
  			threadg.enumerate(threads);
  		
  			for(Thread t : threads)
  			{
  			if(t.getName().compareTo("kick2") == 0) {
  				try{
  				synchronized (t) {
  					t.interrupt();
  				}
  				} catch (Exception e) {
  	      		}
  				
  			}
  			if(t.getName().contains("IAPlay")) {
  				t.setName("kick2");
  			}
  			}
          	} catch (Exception e) {
      		}
      }
  }

  @Override
  public Object get()
  {
    return null;
  }
}