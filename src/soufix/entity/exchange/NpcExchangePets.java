package soufix.entity.exchange;

import java.util.ArrayList;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.entity.npc.NpcTemplate;
import soufix.game.World;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.utility.Pair;

public class NpcExchangePets
{
  private Player player;
  private NpcTemplate npc;
  private long kamas1=0;
  private long kamas2=0;
  private ArrayList<Pair<Integer, Integer>> items1=new ArrayList<>();
  private ArrayList<Pair<Integer, Integer>> items2=new ArrayList<>();
  private boolean ok1;
  private boolean ok2;

  public NpcExchangePets(Player p, NpcTemplate n)
  {
    this.player=p;
    this.npc=n;
  }

  public  void toogleOK(boolean paramBoolean)
  {
    if(paramBoolean)
    {
      this.ok2=(!this.ok2);
      SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
    }
    else
    {
      this.ok1=(!this.ok1);
      SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok1,this.player.getId());
    }
    if((this.ok2)&&(this.ok1))
      apply();
  }

  public  void setKamas(boolean paramBoolean, long paramLong)
  {
    if(paramLong<0L)
      return;
    this.ok2=(this.ok1=false);
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok1,this.player.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
    if(paramBoolean)
    {
      this.setKamas2(paramLong);
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(),'G',"",paramLong+"");
      return;
    }
    if(paramLong>this.player.getKamas())
      return;
    this.setKamas1(paramLong);
    SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player,'G',"",paramLong+"");
  }

  public  void cancel()
  {
    if((this.player.getAccount()!=null)&&(this.player.getGameClient()!=null))
      SocketManager.GAME_SEND_EV_PACKET(this.player.getGameClient());
    this.player.setExchangeAction(null);
  }

  public  void apply()
  {
    GameObject objetToChange=null;
    for(Pair<Integer, Integer> Pair : items1)
    {
      if(Pair.getRight()==0)
        continue;
      if(World.getGameObject(Pair.getLeft()).getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
        continue;
      if(!player.hasItemGuid(Pair.getLeft()))//Si le player n'a pas l'item (Ne devrait pas arriver)
      {
        Pair.right=0;//On met la quantité a 0 pour éviter les problemes
        continue;
      }
      GameObject obj=World.getGameObject(Pair.getLeft());
      objetToChange=obj;
      if((obj.getQuantity()-Pair.getRight())<1)//S'il ne reste plus d'item apres l'échange
      {
        player.removeItem(Pair.getLeft());
        Pair.right=obj.getQuantity();
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player,Pair.getLeft());
      }
      else
      {
        obj.setQuantity(obj.getQuantity()-Pair.getRight(),player);
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(player,obj);
      }
    }

    for(Pair<Integer, Integer> Pair1 : items2)
    {
      if(Pair1.getRight()==0)
        continue;
      if(Main.world.getObjTemplate(Pair1.getLeft())==null)
        continue;
      if(World.getGameObject(objetToChange.getGuid())==null)
        continue;
      GameObject obj1=null;
      if(Main.world.getObjTemplate(Pair1.getLeft()).getType()==18)
        obj1=Main.world.getObjTemplate(Pair1.getLeft()).createNewFamilier(objetToChange);
      if(Main.world.getObjTemplate(Pair1.getLeft()).getType()==77)
        obj1=Main.world.getObjTemplate(Pair1.getLeft()).createNewCertificat(objetToChange);

      if(obj1==null)
        continue;
      if(this.player.addObjet(obj1,true))
        World.addGameObject(obj1,true);
      SocketManager.GAME_SEND_Im_PACKET(this.player,"021;"+Pair1.getRight()+"~"+Pair1.getLeft());
    }
    Main.world.removeGameObject(objetToChange.getGuid());
    this.player.setExchangeAction(null);
    SocketManager.GAME_SEND_EXCHANGE_VALID(this.player.getGameClient(),'a');
    SocketManager.GAME_SEND_Ow_PACKET(this.player);
    //Database.getStatics().getPlayerData().update(this.player);
  }

  public  void addItem(int obj, int qua)
  {
    if(qua<=0)
      return;
    if(World.getGameObject(obj)==null)
      return;
    this.ok1=(this.ok2=false);
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok1,this.player.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
    String str=obj+"|"+qua;
    Pair<Integer, Integer> Pair=getPairInList(items1,obj);
    if(Pair!=null)
    {
      Pair.right+=qua;
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(player,'O',"+",""+obj+"|"+Pair.getRight());
      return;
    }
    SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(player,'O',"+",str);
    items1.add(new Pair<Integer, Integer>(obj,qua));
    if(verifIfAlonePets()||verifIfAloneParcho())
    {
      if(items1.size()==1)
      {
        int id=-1;
        GameObject objet=null;
        for(Pair<Integer, Integer> i : items1)
        {
          if(World.getGameObject(i.getLeft())==null)
            continue;
          objet=World.getGameObject(i.getLeft());
          if(World.getGameObject(i.getLeft()).getTemplate().getType()==18)
          {
            id=Constant.getParchoByIdPets(World.getGameObject(i.getLeft()).getTemplate().getId());
          }
          else if(World.getGameObject(i.getLeft()).getTemplate().getType()==77)
          {
            id=Constant.getPetsByIdParcho(World.getGameObject(i.getLeft()).getTemplate().getId());
          }
        }
        if(id==-1)
          return;
        String str1=id+"|"+1+"|"+id+"|"+objet.parseStatsString();
        this.items2.add(new Pair<Integer, Integer>(id,1));
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(),'O',"+",str1);
        this.ok2=true;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
      }
      else
      {
        clearNpcItems();
        this.ok2=false;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
      }
    }
    else
    {
      clearNpcItems();
      this.ok2=false;
      SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
    }
  }

  public  void removeItem(int guid, int qua)
  {
    if(qua<0)
      return;
    this.ok1=(this.ok2=false);
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok1,this.player.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
    if(World.getGameObject(guid)==null)
      return;
    Pair<Integer, Integer> Pair=getPairInList(items1,guid);
    int newQua=Pair.getRight()-qua;
    if(newQua<1)//Si il n'y a pu d'item
    {
      items1.remove(Pair);
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player,'O',"-",""+guid);
    }
    else
    {
      Pair.right=newQua;
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player,'O',"+",""+guid+"|"+newQua);
    }
    if(verifIfAlonePets())
    {
      if(items1.size()==1)
      {
        int id=-1;
        GameObject objet=null;
        for(Pair<Integer, Integer> i : items1)
        {
          if(World.getGameObject(i.getLeft())==null)
            continue;
          objet=World.getGameObject(i.getLeft());
          if(World.getGameObject(i.getLeft()).getTemplate().getType()==18)
          {
            id=Constant.getParchoByIdPets(World.getGameObject(i.getLeft()).getTemplate().getId());
          }
          else if(World.getGameObject(i.getLeft()).getTemplate().getType()==77)
          {
            id=Constant.getPetsByIdParcho(World.getGameObject(i.getLeft()).getTemplate().getId());
          }
        }
        if(id==-1)
          return;
        String str=id+"|"+1+"|"+id+"|"+objet.parseStatsString();
        this.items2.add(new Pair<Integer, Integer>(id,1));
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(),'O',"+",str);
        this.ok2=true;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
      }
      else
      {
        clearNpcItems();
        this.ok2=false;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
      }
    }
    else
    {
      clearNpcItems();
      this.ok2=false;
      SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(),this.ok2);
    }
  }

  public boolean verifIfAlonePets()
  {
    for(Pair<Integer, Integer> i : items1)
      if(World.getGameObject(i.getLeft()).getTemplate().getType()!=18)
        return false;
    return true;
  }

  public boolean verifIfAloneParcho()
  {
    for(Pair<Integer, Integer> i : items1)
      if(World.getGameObject(i.getLeft()).getTemplate().getType()!=77)
        return false;
    return true;
  }

  public  void clearNpcItems()
  {
    for(Pair<Integer, Integer> i : items2)
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(),'O',"-",i.getLeft()+"");
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

  public long getKamas2()
  {
    return kamas2;
  }

  public void setKamas2(long kamas2)
  {
    this.kamas2=kamas2;
  }

  public long getKamas1()
  {
    return kamas1;
  }

  public void setKamas1(long kamas1)
  {
    this.kamas1=kamas1;
  }
}