package soufix.entity.exchange;

import java.util.ArrayList;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.entity.npc.NpcTemplate;
import soufix.entity.pet.PetEntry;
import soufix.game.World;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.utility.Pair;

public class NpcRessurectPets
{
  private Player perso;
  private NpcTemplate npc;
  private long kamas1=0;
  private long kamas2=0;
  private ArrayList<Pair<Integer, Integer>> items1=new ArrayList<Pair<Integer, Integer>>();
  private ArrayList<Pair<Integer, Integer>> items2=new ArrayList<Pair<Integer, Integer>>();
  private boolean ok1;
  private boolean ok2;

  public NpcRessurectPets(Player p, NpcTemplate n)
  {
    this.perso=p;
    this.npc=n;
  }

  public  long getKamas(boolean b)
  {
    if(b)
      return this.kamas2;
    return this.kamas1;
  }

  public  void toogleOK(boolean paramBoolean)
  {
    if(paramBoolean)
    {
      this.ok2=(!this.ok2);
      SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok2);
    }
    else
    {
      this.ok1=(!this.ok1);
      SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok1,this.perso.getId());
    }
    if((this.ok2)&&(this.ok1))
      apply();
  }

  public  void setKamas(boolean paramBoolean, long paramLong)
  {
    if(paramLong<0L)
      return;
    this.ok2=(this.ok1=false);
    SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok1,this.perso.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok2);
    if(paramBoolean)
    {
      this.kamas2=paramLong;
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.perso.getGameClient(),'G',"",paramLong+"");
      return;
    }
    if(paramLong>this.perso.getKamas())
      return;
    this.kamas1=paramLong;
    SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.perso,'G',"",paramLong+"");
  }

  public  void cancel()
  {
    if((this.perso.getAccount()!=null)&&(this.perso.getGameClient()!=null))
      SocketManager.GAME_SEND_EV_PACKET(this.perso.getGameClient());
    this.perso.setExchangeAction(null);
  }

  public  void apply()
  {
    for(Pair<Integer, Integer> item : items1)
    {
      GameObject object=World.getGameObject(item.getLeft());
      if(object.getTemplate().getId()==8012)
      {
        if((object.getQuantity()-item.getRight())<1)
        {
          perso.removeItem(item.getLeft());
          item.right=object.getQuantity();
          SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(perso,item.getLeft());
        }
        else
        {
          object.setQuantity(object.getQuantity()-item.getRight(),perso);
          SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(perso,object);
        }
      }
      else
      {
        PetEntry pet=Main.world.getPetsEntry(item.getLeft());
        if(pet!=null)
        {
          pet.resurrection();
          SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(this.perso,object);
        }
      }
    }
    this.perso.setExchangeAction(null);
    SocketManager.GAME_SEND_EXCHANGE_VALID(this.perso.getGameClient(),'a');
    SocketManager.GAME_SEND_Ow_PACKET(this.perso);
    //Database.getStatics().getPlayerData().update(this.perso);
  }

  public  void addItem(int obj, int qua)
  {
    if(qua<=0)
      return;
    if(World.getGameObject(obj)==null)
      return;
    this.ok1=(this.ok2=false);
    SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok1,this.perso.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok2);
    String str=obj+"|"+qua;
    Pair<Integer, Integer> Pair=getPairInList(items1,obj);
    if(Pair!=null)
    {
      Pair.right+=qua;
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso,'O',"+",""+obj+"|"+Pair.getRight());
      return;
    }
    SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso,'O',"+",str);
    items1.add(new Pair<Integer, Integer>(obj,qua));
    if(verification())
    {
      if(items1.size()==2)
      {
        int id=-1;
        GameObject objet=null;

        for(Pair<Integer, Integer> i : items1)
        {
          objet=World.getGameObject(i.getLeft());

          if(objet==null)
            continue;
          if(objet.getTemplate().getType()==90)
          {
            id=Main.world.getPetsEntry(i.getLeft()).getTemplate();
            break;
          }
        }

        if(id==-1||objet==null)
          return;
        String str1=id+"|"+1+"|"+id+"|"+objet.parseStatsString();
        this.items2.add(new Pair<Integer, Integer>(id,1));
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.perso.getGameClient(),'O',"+",str1);
        this.ok2=true;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok2);
      }
      else
      {
        clearNpcItems();
        this.ok2=false;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok2);
      }
    }
    else
    {
      clearNpcItems();
      this.ok2=false;
      SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok2);
    }
  }

  public  void removeItem(int guid, int qua)
  {
    if(qua<0)
      return;
    this.ok1=(this.ok2=false);
    SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok1,this.perso.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok2);
    if(World.getGameObject(guid)==null)
      return;
    Pair<Integer, Integer> Pair=getPairInList(items1,guid);
    int newQua=Pair.getRight()-qua;
    if(newQua<1)//Si il n'y a pu d'item
    {
      items1.remove(Pair);
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.perso,'O',"-",""+guid);
    }
    else
    {
      Pair.right=newQua;
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.perso,'O',"+",""+guid+"|"+newQua);
    }
    if(verification())
    {
      if(items1.size()==2)
      {
        int id=-1;
        GameObject objet=null;

        for(Pair<Integer, Integer> i : items1)
        {
          objet=World.getGameObject(i.getLeft());

          if(objet==null)
            continue;
          if(objet.getTemplate().getType()==90)
          {
            id=Main.world.getPetsEntry(i.getLeft()).getTemplate();
            break;
          }
        }

        if(id==-1||objet==null)
          return;

        String str=id+"|"+1+"|"+id+"|"+objet.parseStatsString();
        this.items2.add(new Pair<Integer, Integer>(id,1));
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.perso.getGameClient(),'O',"+",str);
        this.ok2=true;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok2);
      }
      else
      {
        clearNpcItems();
        this.ok2=false;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok2);
      }
    }
    else
    {
      clearNpcItems();
      this.ok2=false;
      SocketManager.GAME_SEND_EXCHANGE_OK(this.perso.getGameClient(),this.ok2);
    }
  }

  public boolean verification()
  {
    boolean verif=true;
    for(Pair<Integer, Integer> item : items1)
    {
      GameObject object=World.getGameObject(item.getLeft());
      if((object.getTemplate().getId()!=8012&&object.getTemplate().getType()!=90)||item.getRight()>1)
        verif=false;
    }
    return verif;
  }

  public  void clearNpcItems()
  {
    for(Pair<Integer, Integer> i : items2)
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.perso.getGameClient(),'O',"-",i.getLeft()+"");
    this.items2.clear();
  }

  private  Pair<Integer, Integer> getPairInList(ArrayList<Pair<Integer, Integer>> items, int guid)
  {
    for(Pair<Integer, Integer> Pair : items)
      if(Pair.getLeft()==guid)
        return Pair;
    return null;
  }

  public  int getQuaItem(int obj, boolean b)
  {
    ArrayList<Pair<Integer, Integer>> list;
    if(b)
      list=this.items2;
    else
      list=this.items1;

    for(Pair<Integer, Integer> item : list)
      if(item.getLeft()==obj)
        return item.getRight();
    return 0;
  }

  public NpcTemplate getNpc()
  {
    return npc;
  }

  public void setNpc(NpcTemplate npc)
  {
    this.npc=npc;
  }
}
