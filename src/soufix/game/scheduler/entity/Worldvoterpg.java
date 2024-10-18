package soufix.game.scheduler.entity;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.game.scheduler.Updatable;
import soufix.main.Config;
import soufix.main.Main;

import java.util.HashMap;
import java.util.Map;

public class Worldvoterpg extends Updatable
{
	private Map<String, Map<String, String>> ip=new HashMap<>();
  public final static Updatable updatable=new Worldvoterpg(Config.getInstance().Worldvote+80000);

  public Worldvoterpg(int wait)
  {
    super(wait);
  }

  @Override
  public void update()
  {
      if(this.verify())
      {
    	 for (final Player z : Main.world.getOnlinePlayers()) {
				if (z.getGameClient() == null) {
					continue;
				}
				if(ip.get(z.getAccount().getCurrentIp()) != null) {
					 Map<String, String> data=ip.get(z.getAccount().getCurrentIp());
					 if(data.get("vote") == "1") {
						 continue;
					 }
					 SocketManager.GAME_SEND_MESSAGE(z,"(Message Auto) Vous pouvez désormais voter sur Rpg ");	
				}else {
					ip.put(z.getAccount().getCurrentIp(),new HashMap<>());
					if (!z.getAccount().gettimevoterpg()) {
						ip.get(z.getAccount().getCurrentIp()).put("vote","1");
						continue;	
					}
				    ip.get(z.getAccount().getCurrentIp()).put("vote","0");
					SocketManager.GAME_SEND_MESSAGE(z,"(Message Auto) Vous pouvez désormais voter sur Rpg ");	
				}
				
			}
 	 ip.clear();
      }
  }

  @Override
  public Object get()
  {
    return null;
  }
}