package soufix.job;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.main.Config;
import soufix.utility.TimerWaiterPlus;

public class JobCraft
{
  private JobAction jobAction;
  private int time=0;
 // private boolean itsOk=true;

  public JobCraft(JobAction jobAction, Player player)
  {
	  if(player.getGameClient() == null)
		return;  
    this.jobAction=jobAction;
    //TimerWaiterPlus.addNext(() -> {
    //	if(itsOk)
    //    jobAction.craft(false,player);
    //},1000);
    
    TimerWaiterPlus.addNext(() -> {
    	//  if(!itsOk)
        repeat(time,time,player);
    },1000);
    
  }

  public void setAction(int time)
  {
    this.time=time-1;
    this.jobAction.broken=false;
    //this.itsOk=false;
  }

  private void repeat(final int time1, final int time2, final Player player)
  {
    final int j=time1-time2;
    this.jobAction.player=player;
    this.jobAction.isRepeat=true;
    int timer = Config.getInstance().craftDelay;
    if (this.jobAction.getId() == 1 || this.jobAction.getId() == 113 || this.jobAction.getId() == 115 || this.jobAction.getId() == 116 || this.jobAction.getId() == 117 || this.jobAction.getId() == 118
			|| this.jobAction.getId() == 119 || this.jobAction.getId() == 120 || (this.jobAction.getId() >= 163 && this.jobAction.getId() <= 169)) {
    	timer =  1000;
	}
    if(!this.check(player,j,time2)||time2<=0)
    {
      this.end();
    }
    else
    {
    	TimerWaiterPlus.addNext(() ->  this.repeat(time1,(time2-1),player),timer);
    }
  }

  private boolean check(final Player player, int j, int time2)
  {
    if(this.jobAction.broken||player.getExchangeAction()==null||!player.isOnline())
    {
      if(player.getExchangeAction()==null)
        this.jobAction.broken=true;
      if(player.isOnline())
        SocketManager.GAME_SEND_Ea_PACKET(this.jobAction.player,this.jobAction.broken ? "2" : "4");
      return false;
    }
    else
    {
      SocketManager.GAME_SEND_EA_PACKET(this.jobAction.player,String.valueOf(time2));
      this.jobAction.craft(this.jobAction.isRepeat, player);
      this.jobAction.ingredients.clear();
      this.jobAction.ingredients.putAll(this.jobAction.lastCraft);
      return true;
    }
  }

  //v2.8 - disappearing items fix
  private void end()
  {
    SocketManager.GAME_SEND_Ea_PACKET(this.jobAction.player,"1");
    if(!(this.jobAction.getId()==1||this.jobAction.getId()==113||this.jobAction.getId()==115||this.jobAction.getId()==116||this.jobAction.getId()==117||this.jobAction.getId()==118||this.jobAction.getId()==119||this.jobAction.getId()==120||(this.jobAction.getId()>=163&&this.jobAction.getId()<=169)))
      this.jobAction.ingredients.clear();
    if(!this.jobAction.data.isEmpty())
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK_FM(this.jobAction.player,'O',"+",this.jobAction.data);
    this.jobAction.isRepeat=false;
    this.jobAction.setJobCraft(null);

    /*if(this.jobAction.player.getInInteractiveObject()!=null)
    {
      this.jobAction.player.getInInteractiveObject().getLeft().setState(JobConstant.IOBJECT_STATE_FULL);
      SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(this.jobAction.player.getCurMap(),this.jobAction.player.getInInteractiveObject().getRight());
    }*/
  }
  public JobAction getJobAction() {
	return jobAction;
}
}