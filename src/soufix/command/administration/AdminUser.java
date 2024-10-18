package soufix.command.administration;

import soufix.client.Account;
import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.game.GameClient;
import soufix.main.Main;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class AdminUser
{
  private Account account;
  private Player player;
  private GameClient client;

  private boolean timerStart=false;
  private Timer timer;

  public AdminUser(Player player)
  {
	  if(player != null) {
    this.account=player.getAccount();
    this.player=player;
    this.client=player.getAccount().getGameClient();
	  }
  }

  public Account getAccount()
  {
    return account;
  }

  public Player getPlayer()
  {
    return player;
  }

  public GameClient getClient()
  {
    return client;
  }

  public boolean isTimerStart()
  {
    return timerStart;
  }

  public void setTimerStart(boolean timerStart)
  {
    this.timerStart=timerStart;
  }

  public Timer getTimer()
  {
    return timer;
  }

  public void setTimer(Timer timer)
  {
    this.timer=timer;
  }

  public Timer createTimer(final int timer)
  {
    ActionListener action=new ActionListener()
    {
      int time=timer;

      public void actionPerformed(ActionEvent event)
      {
        time=time-1;
        if(time==1)
          SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;"+time+" minute");
        else
          SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;"+time+" minutes");
        if(time<=0)
          Main.stop("Shutdown by an administrator");
      }
    };
    return new Timer(60000,action);
  }

  public void sendMessage(String message)
  {
    this.player.send("BAT0"+message);
  }

  public void sendErrorMessage(String message)
  {
    this.player.send("BAT1"+message);
  }

  public void sendSuccessMessage(String message)
  {
    this.player.send("BAT2"+message);
  }

  public abstract void apply(String packet , boolean serveur);
}