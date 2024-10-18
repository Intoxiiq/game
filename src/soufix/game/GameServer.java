package soufix.game;

import soufix.client.Player;
import soufix.main.Config;
import soufix.main.Main;
import soufix.utility.TimerWaiterPlus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer implements Runnable
{

  public final List<String> waitingaccount = new ArrayList<>();
  private CopyOnWriteArrayList<GameClient> _clients = new CopyOnWriteArrayList<GameClient>();
  public ServerSocket _SS;
  public Thread _t;
  private int id = 0;
  private int maxConnections	= 0;



  public void start()
  {
	 
      try {
    	  try {
			_SS = new ServerSocket(Config.getInstance().gamePort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.start();
		}
		_SS.setReceiveBufferSize(2048);
	      _t = new Thread(this,"GameServeur");
	      _t.start();
	} catch (SocketException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		this.start();
	}
  }


  //v2.8 - correct number
  public int getPlayersNumberByIp()
  {
	  try {
    ArrayList<String> IPs=new ArrayList<String>();
    for(Player player : Main.world.getOnlinePlayers())
      if(player.getGameClient()!=null)
      {
        boolean same=false;
        for(String IP : IPs)
          if(player.getGameClient().getAccount().getCurrentIp().equals(IP))
            same=true;
        if(same==false)
          IPs.add(player.getGameClient().getAccount().getCurrentIp());
      }
    return IPs.size();
	  }
 	   catch(Exception e)
 	    {
 	      e.printStackTrace();
 	      return 0;
 	    }
  }

  public void setState(int state)
  {
    Main.exchangeClient.send("SS"+state);
  }



  public void kickAll()
  {
    for(Player player : Main.world.getOnlinePlayers())
    {
      if(player!=null&&player.getGameClient()!=null)
      {
        player.send("M04");
        if(player.getGameClient()==null)
        	continue;
        //Database.getStatics().getAccountData().update(player.getAccount());
        //Database.getStatics().getPlayerData().update(player);
        player.getGameClient().kickv2();
      }
    }
  }
  public void delClient(GameClient gameThread) {
	  TimerWaiterPlus.addNext(() -> {
  	_clients.remove(gameThread);
	  },300);
      
      //Ancestra.refreshTitle();
  }
  public CopyOnWriteArrayList<GameClient> getClients() {
      return _clients;
  }

  public String getServerTime()
  {
    return "BT"+(new Date().getTime()+3600000*2);
  }


  public int getMaxPlayer() {
		return this.maxConnections;
	}

  public void run()
 	{	
 		while(Main.isRunning)//bloque sur _SS.accept()
 		{
 			try
 			{
 				Socket SAccepted = _SS.accept();
 				String ip = SAccepted.getInetAddress().getHostAddress();
 				
 			    SAccepted.setTcpNoDelay(true);
                 GameClient Gt = new GameClient(ip, SAccepted);
                
                 _clients.add(Gt);
                 id++;
                 Main.world.logger.info("Session "+id+" created");
                 if (_clients.size() > this.maxConnections) {
         			this.maxConnections = _clients.size();
                 }
         		

 			}catch(IOException e)
 			{
 				this.run();
 				 e.printStackTrace();
 			}
 		}
 	}
}
