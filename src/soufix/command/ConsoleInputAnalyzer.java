package soufix.command;
import java.io.Console;

import org.fusesource.jansi.AnsiConsole;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.fight.Fight;
import soufix.game.GameClient;
import soufix.game.scheduler.entity.WorldSave;
import soufix.main.Config;
import soufix.main.Main;


public class ConsoleInputAnalyzer implements Runnable{
	private Thread _t;

	public ConsoleInputAnalyzer()
	{
		this._t = new Thread(this,"Console");
		_t.setDaemon(true);
		_t.start();
	}
	public void run() {
		while (Main.isRunning){
			try{
			Console console = System.console();
		    String command = console.readLine();
		    evalCommand(command);
		    }catch(Exception e){}
		    finally
		    {
		    	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
		    }
		}
	}
	public void evalCommand(String command)
	{
		String[] args = command.split(" ");
		String fct =args[0].toUpperCase();
		if(fct.equals("SAVE"))
		{
			WorldSave.cast(0);
		}else if(fct.equals("BDD"))
		{
			Database.launchDatabase();
		}else if(fct.equals("perso"))
		{
			Main.world.getPlayers().stream().filter(player -> player!=null).filter(Player::isOnline).forEach(player -> {
		        Database.getStatics().getPlayerData().update(player);
		        if(player.getGuildMember()!=null)
		          Database.getDynamics().getGuildMemberData().update(player);
		      });
		}else
		if(fct.equals("EXIT"))
		{
            Main.stop("Exit by administrator");
	
			}else
				if(fct.equalsIgnoreCase("DDOS"))
				{	
					if(Main.anti_ddos){
						Main.anti_ddos=false;
						sendEcho("<Anti DDOS Off>");
						return;
					}
					if(!Main.anti_ddos){
						Main.anti_ddos=true;
						sendEcho("<Anti DDOS On>");
						return;
					}
						
				}else
			if(fct.equals("SETON"))
			{
				 Main.gameServer.setState(1);
			}else
		if(fct.equalsIgnoreCase("ANNOUNCE"))
		{	
				String announce = command.substring(9);
				String PrefixConsole = "<b>Serveur</b> : ";
				SocketManager.GAME_SEND_MESSAGE_TO_ALL(PrefixConsole+announce, "ff0000");
				sendEcho("<Announce:> "+announce);
		}else if(fct.equalsIgnoreCase("ENDFIGHT"))
		{	
			 Player P=Main.world.getPlayerByName(command.substring(9));
			if(P == null) {
				sendEcho("Personnage Null");	
			}
			if(P.getFight() == null) {
				sendEcho("Personnage pas en combat");		
			}
			P.getFight().endFight(true,true);
			sendEcho("ENdfight good");
	}else
				if(fct.equalsIgnoreCase("ENDFIGHTALL"))
				{	
				     try
				      {
				        for(GameClient client : Main.gameServer.getClients())
				        {
				          Player player=client.getPlayer();
				          if(player==null)
				            continue;
				          Fight f=player.getFight();
				          if(f==null)
				            continue;
				          try
				          {
				            if(f.getLaunchTime()>1)
				              continue;
				            f.endFight(true,true);
				            sendEcho("Le combat de "+player.getName()+" a ete termine.");
				          }
				          catch(Exception e)
				          {
				            // ok
				        	  sendEcho("Le combat de "+player.getName()+" a deje ete termine.");
				          }
				        }
				      }
				      catch(Exception e)
				      {
				        e.printStackTrace();
				        sendEcho("Erreur lors de la commande endfightall : "+e.getMessage()+".");
				      } finally
				      {
				    	  sendEcho("Tous les combats ont ete termines.");
				      }
				}else
		if(fct.equals("INFOS"))
		{
			long uptime = System.currentTimeMillis()
					- Config.getInstance().startTime;
			final int jour = (int) (uptime / 86400000L);
			uptime %= 86400000L;
			final int hour = (int) (uptime / 3600000L);
			uptime %= 3600000L;
			final int min = (int) (uptime / 60000L);
			uptime %= 60000L;
			final int sec = (int) (uptime / 1000L);
			final int nbPlayer = Main.world.getOnlinePlayers().size();
			final int nbPlayerIp = Main.gameServer.getPlayersNumberByIp();
			final int maxPlayer = Main.gameServer.getMaxPlayer();
			String mess6 = "===========\nUptime : " + jour + "j " + hour + "h "
					+ min + "m " + sec + "s.\n";
			if (nbPlayer > 0) {
				mess6 = String.valueOf(mess6) + "Joueurs en ligne : " + nbPlayer
						+ "\n";
			}
			if (nbPlayerIp > 0) {
				mess6 = String.valueOf(mess6) + "Joueurs uniques en ligne : "
						+ nbPlayerIp + "\n";
			}
			if (maxPlayer > 0) {
				mess6 = String.valueOf(mess6) + "Record de connexion : "
						+ maxPlayer + "\n";
			}
			mess6 = String.valueOf(mess6) + "===========";
			sendEcho(mess6);
		}
		else
		{
			sendError("Commande non reconnue ou incomplete.");
		}
	}

	public static void sendInfo(String msg)
	{
		AnsiConsole.out.println(msg);
	}
	public static void sendError(String msg)
	{
		AnsiConsole.out.println(msg);
	}
	public static void send(String msg)
	{
		AnsiConsole.out.println(msg);
	}
	public static void sendDebug(String msg)
	{
		//if(Ancestra.CONFIG_DEBUG)common.Console.println(msg, common.Console.ConsoleColorEnum.YELLOW);
	}
	public static void sendEcho(String msg)
	{
		AnsiConsole.out.println(msg);
	}
	
}
