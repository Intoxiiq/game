package soufix.entity.exchange;

import soufix.client.Player;
import soufix.common.Formulas;
import soufix.common.SocketManager;
import soufix.game.World;
import soufix.job.JobAction;
import soufix.job.JobConstant;
import soufix.job.JobStat;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.object.GameObject;
import soufix.utility.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CraftSecure extends PlayerExchange
{
  private long payKamas=0;
  private long payIfSuccessKamas=0;
  private int maxCase=9;
  private boolean maging=false;

  private ArrayList<Pair<Integer, Integer>> payItems=new ArrayList<>();
  private ArrayList<Pair<Integer, Integer>> payItemsIfSuccess=new ArrayList<>();

  public CraftSecure(Player player1, Player player2)
  {
    super(player1,player2);
    JobStat job=this.player1.getMetierBySkill(this.player1.getIsCraftingType().get(1));
    this.maging=job.getTemplate().isMaging();
    int nb=this.maging ? 3 : JobConstant.getTotalCaseByJobLevel(job.get_lvl());
    this.maxCase=nb;
  }

  public Player getNeeder()
  {
    return player2;
  }

  public int getMaxCase()
  {
    return maxCase;
  }

  public  void apply()
  {
    JobStat jobStat=this.player1.getMetierBySkill(this.player1.getIsCraftingType().get(1));

    if(jobStat==null)
      return;

    JobAction jobAction=jobStat.getJobActionBySkill(this.player1.getIsCraftingType().get(1));

    if(jobAction==null)
      return;

    Map<Player, ArrayList<Pair<Integer, Integer>>> items=new HashMap<>();
    items.put(this.player1,this.items1);
    items.put(this.player2,this.items2);

    int sizeList=jobAction.sizeList(items);

    boolean success=jobAction.craftPublicMode(this.player1,this.player2,items);

    this.player1.addKamas(payKamas+(success ? payIfSuccessKamas : 0),false);
    this.player2.addKamas(-payKamas-(success ? payIfSuccessKamas : 0),false);

    if(success)
      this.giveObjects(this.payItems,this.payItemsIfSuccess);
    else
      this.giveObjects(this.payItems);

    int winXP=0;
    if(success)
      winXP=Formulas.calculXpWinCraft(jobStat.get_lvl(),sizeList)*Config.getInstance().rateJob;
    else if(!jobStat.getTemplate().isMaging())
      winXP=Formulas.calculXpWinCraft(jobStat.get_lvl(),sizeList)*Config.getInstance().rateJob;

    if(winXP>0)
    {
      jobStat.addXp(this.player1,winXP);
      ArrayList<JobStat> SMs=new ArrayList<>();
      SMs.add(jobStat);
      SocketManager.GAME_SEND_JX_PACKET(this.player1,SMs);
    }

    SocketManager.GAME_SEND_STATS_PACKET(this.player1);
    SocketManager.GAME_SEND_STATS_PACKET(this.player2);

    this.payIfSuccessKamas=0;
    this.payKamas=0;
    this.payItems.clear();
    this.payItemsIfSuccess.clear();
    this.items1.clear();
    this.items2.clear();
    this.ok1=false;
    this.ok2=false;
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok2,this.player2.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok2,this.player2.getId());

  }

  @SafeVarargs
  private final void giveObjects(ArrayList<Pair<Integer, Integer>>... arrays)
  {
    for(ArrayList<Pair<Integer, Integer>> array : arrays)
    {
      for(Pair<Integer, Integer> couple : array)
      {
        if(couple.getRight()==0)
          continue;

        GameObject object=World.getGameObject(couple.getLeft());

        if(object==null)
          continue;
        if(object.getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
          continue;
        if(!this.player2.hasItemGuid(couple.getLeft()))
        {
          couple.right=0;
          continue;
        }

        this.giveObject(couple,object);
      }
    }
  }

  public  void cancel()
  {
    this.send("EV");
    this.player1.getIsCraftingType().clear();
    this.player2.getIsCraftingType().clear();
    this.player1.setExchangeAction(null);
    this.player2.setExchangeAction(null);
  }

  public void setPayKamas(byte type, long kamas)
  {
    if(kamas<0)
      return;
    if(this.player2.getKamas()<kamas)
      kamas=this.player2.getKamas();
    this.ok1=false;
    this.ok2=false;
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok2,this.player2.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok2,this.player2.getId());

    switch(type)
    {
      case 1:// Pay
        if(this.payIfSuccessKamas>0&&((kamas+this.payIfSuccessKamas)>this.player2.getKamas()))
          kamas-=this.payIfSuccessKamas;
        if(kamas<0)
        kamas = 0;	
        this.payKamas=kamas;
        this.send("Ep1;G"+this.payKamas);
        break;
      case 2: // PayIfSuccess
        if(this.payKamas>0&&((kamas+this.payKamas)>this.player2.getKamas()))
          kamas-=this.payKamas;
        if(kamas<0)
          kamas = 0;
        this.payIfSuccessKamas=kamas;
        this.send("Ep2;G"+this.payIfSuccessKamas);
        break;
    }
  }

  public void setPayItems(byte type, boolean adding, int guid, int quantity)
  {
    GameObject object=World.getGameObject(guid);

    if(object==null)
      return;
    if(object.getPosition()!=Constant.ITEM_POS_NO_EQUIPED||object.isAttach())
      return;

    this.ok1=false;
    this.ok2=false;
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok2,this.player2.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok2,this.player2.getId());

    if(adding)
    {
      this.addItem(object,quantity,type);
    } else
    {
      this.removeItem(object,quantity,type);
    }
  }

  private void addItem(GameObject object, int quantity, byte type)
  {
    if(object.getQuantity()<quantity)
      quantity=object.getQuantity();

    ArrayList<Pair<Integer, Integer>> items=(type==1 ? this.payItems : this.payItemsIfSuccess);
    Pair<Integer, Integer> couple=getPairInList(items,object.getGuid());
    String add="|"+object.getTemplate().getId()+"|"+object.parseStatsString();

    if(couple!=null)
    {
		if(object.getQuantity() < quantity+couple.right)return;
      couple.right+=quantity;
      this.player2.send("Ep"+type+";O+"+object.getGuid()+"|"+couple.getRight());
      this.player1.send("Ep"+type+";O+"+object.getGuid()+"|"+couple.getRight()+add);
      return;
    }

    items.add(new Pair<>(object.getGuid(),quantity));
    this.player2.send("Ep"+type+";O+"+object.getGuid()+"|"+quantity);
    this.player1.send("Ep"+type+";O+"+object.getGuid()+"|"+quantity+add);
  }

  private void removeItem(GameObject object, int quantity, byte type)
  {
    ArrayList<Pair<Integer, Integer>> items=(type==1 ? this.payItems : this.payItemsIfSuccess);
    Pair<Integer, Integer> couple=getPairInList(items,object.getGuid());

    if(couple==null)
      return;
    int newQua=couple.getRight()-quantity;

    if(newQua<1)
    {
      items.remove(couple);
      this.player1.send("Ep"+type+";O-"+object.getGuid());
      this.player2.send("Ep"+type+";O-"+object.getGuid());
    } else
    {
      couple.right=newQua;
      this.player2.send("Ep"+type+";O+"+object.getGuid()+"|"+newQua);
      this.player1.send("Ep"+type+";O+"+object.getGuid()+"|"+newQua+"|"+object.getTemplate().getId()+"|"+object.parseStatsString());
    }
  }

  private void send(String packet)
  {
    this.player1.send(packet);
    this.player2.send(packet);
  }
}