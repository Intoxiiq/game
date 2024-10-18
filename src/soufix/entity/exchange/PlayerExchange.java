package soufix.entity.exchange;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.game.World;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.object.ObjectTemplate;
import soufix.utility.Pair;
import java.util.ArrayList;

public class PlayerExchange extends Exchange
{

  public PlayerExchange(Player player1, Player player2)
  {
    super(player1,player2);
  }

  private boolean isPodsOK(byte i)
  {
    if(this instanceof CraftSecure)
      return true;

    int newpods=0;
    int oldpods=0;
    if(i==1)
    {
      int podsmax=this.player1.getMaxPod();
      int pods=this.player1.getPodUsed();
      for(Pair<Integer, Integer> couple : items2)
      {
        if(couple.getRight()==0)
          continue;
        GameObject obj=World.getGameObject(couple.getLeft());
        if(obj == null)
        	 continue;
        newpods+=obj.getTemplate().getPod()*couple.getRight();
      }
      if(newpods==0)
      {
        return true;
      }
      for(Pair<Integer, Integer> couple : items1)
      {
        if(couple.getRight()==0)
          continue;
        GameObject obj=World.getGameObject(couple.getLeft());
        if(obj == null)
       	 continue;
        oldpods+=obj.getTemplate().getPod()*couple.getRight();
      }
      if((newpods+pods-oldpods)>podsmax)
      {
        // Erreur 70
        // 1 + 70 => 170
        SocketManager.GAME_SEND_Im_PACKET(this.player1,"170");
        return false;
      }
    }
    else
    {
      int podsmax=this.player2.getMaxPod();
      int pods=this.player2.getPodUsed();
      for(Pair<Integer, Integer> couple : items1)
      {
        if(couple.getRight()==0)
          continue;
        GameObject obj=World.getGameObject(couple.getLeft());
        if(obj == null)
          	 continue;
        newpods+=obj.getTemplate().getPod()*couple.getRight();
      }
      if(newpods==0)
      {
        return true;
      }
      for(Pair<Integer, Integer> couple : items2)
      {
        if(couple.getRight()==0)
          continue;
        GameObject obj=World.getGameObject(couple.getLeft());
        if(obj == null)
          	 continue;
        oldpods+=obj.getTemplate().getPod()*couple.getRight();
      }
      if((newpods+pods-oldpods)>podsmax)
      {
        SocketManager.GAME_SEND_Im_PACKET(this.player2,"170");
        return false;
      }
    }
    return true;
  }

  public  long getKamas(int guid)
  {
    int i=0;
    if(this.player1.getId()==guid)
      i=1;
    else if(this.player2.getId()==guid)
      i=2;

    if(i==1)
      return kamas1;
    else if(i==2)
      return kamas2;
    return 0;
  }

  public  boolean toogleOk(int guid)
  {
    byte i=(byte)(this.player1.getId()==guid ? 1 : 2);
    if(this.isPodsOK(i))
    {
      if(i==1)
      {
        ok1=!ok1;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok1,guid);
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok1,guid);
      }
      else if(i==2)
      {
        ok2=!ok2;
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok2,guid);
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok2,guid);
      }
      if(player2.getParty() != null)
    	  if(player2.getParty().getMaster() != null)
    	  if(player2.getParty().getMaster().getId() == player1.getId())
    		  return true;
      if(player1.getGroupe() != null)
    		  return true;
      return (ok1&&ok2);
    }
    return false;
  }

  public  void setKamas(int guid, long k)
  {
    ok1=false;
    ok2=false;

    int i=0;
    if(this.player1.getId()==guid)
      i=1;
    else if(this.player2.getId()==guid)
      i=2;
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok2,this.player2.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok2,this.player2.getId());
    if(k<0)
      return;
    if(i==1)
    {
      kamas1=k;
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player1,'G',"",k+"");
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(),'G',"",k+"");
    }
    else if(i==2)
    {
      kamas2=k;
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(),'G',"",k+"");
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player2,'G',"",k+"");
    }
  }

  public  void cancel()
  {
    if(this.player1.getAccount()!=null)
      if(this.player1.getGameClient()!=null)
        SocketManager.GAME_SEND_EV_PACKET(this.player1.getGameClient());
    if(this.player2.getAccount()!=null)
      if(this.player2.getGameClient()!=null)
        SocketManager.GAME_SEND_EV_PACKET(this.player2.getGameClient());
    this.player1.setExchangeAction(null);
    this.player2.setExchangeAction(null);
  }

  //v2.7 - Replaced String += with StringBuilder
  public  void apply()
  {
    StringBuilder str=new StringBuilder();
    try
    {
      str.append(this.player1.getName()+" : ");
      for(Pair<Integer, Integer> couple1 : items1)
        str.append(", ["+World.getGameObject(couple1.getLeft()).getTemplate().getName()+"@"+couple1.getLeft()+";"+couple1.getRight()+"]");
      str.append(" avec "+kamas1+" K.");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    try
    {
      str.append("Avec "+this.player2.getName());
      for(Pair<Integer, Integer> couple2 : items2)
        str.append(", ["+World.getGameObject(couple2.getLeft()).getTemplate().getName()+"@"+couple2.getLeft()+";"+couple2.getRight()+"]");
      str.append(" avec "+kamas2+" K.");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    this.player1.addKamas((-kamas1+kamas2),false);
    this.player2.addKamas((-kamas2+kamas1),false);

    for(Pair<Integer, Integer> couple : items1) // Les items du player vers le player2
    {
      if(couple.getRight()==0)
        continue;
      if(World.getGameObject(couple.getLeft())==null)
        continue;
      if(World.getGameObject(couple.getLeft()).getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
        continue;
      if(!this.player1.hasItemGuid(couple.getLeft()))//Si le player n'a pas l'item (Ne devrait pas arriver : wpepro)
      {
        couple.right=0;//On met la quantité a 0 pour éviter les problemes
        continue;
      }
      GameObject obj=World.getGameObject(couple.getLeft());
      if((obj.getQuantity()-couple.getRight())<1)//S'il ne reste plus d'item apres l'échange
      {
        this.player1.removeItem(couple.getLeft());
        couple.right=obj.getQuantity();
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player1,couple.getLeft());
        if(!this.player2.addObjet(obj,true))//Si le joueur avait un item similaire
          Main.world.removeGameObject(couple.getLeft());//On supprime l'item inutile
      }
      else
      {
        obj.setQuantity(obj.getQuantity()-couple.getRight(),this.player2);
        SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player1,obj);
        GameObject newObj=GameObject.getCloneObjet(obj,couple.getRight());
        if(this.player2.addObjet(newObj,true))//Si le joueur n'avait pas d'item similaire
          World.addGameObject(newObj,true);//On ajoute l'item au World
      }
    }
    for(Pair<Integer, Integer> couple : items2)
    {
      if(couple.getRight()==0)
        continue;
      if(World.getGameObject(couple.getLeft())==null)
        continue;
      if(World.getGameObject(couple.getLeft()).getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
        continue;
      if(!this.player2.hasItemGuid(couple.getLeft()))//Si le player n'a pas l'item (Ne devrait pas arriver)
      {
        couple.right=0;//On met la quantité a 0 pour éviter les problemes
        continue;
      }
      this.giveObject(couple,World.getGameObject(couple.getLeft()));
    }
    //Fin
    this.player1.setExchangeAction(null);
    this.player2.setExchangeAction(null);
    SocketManager.GAME_SEND_Ow_PACKET(this.player1);
    SocketManager.GAME_SEND_Ow_PACKET(this.player2);
    SocketManager.GAME_SEND_STATS_PACKET(this.player1);
    SocketManager.GAME_SEND_STATS_PACKET(this.player2);
    SocketManager.GAME_SEND_EXCHANGE_VALID(this.player1.getGameClient(),'a');
    SocketManager.GAME_SEND_EXCHANGE_VALID(this.player2.getGameClient(),'a');
    //Database.getStatics().getPlayerData().update(this.player1);
    //Database.getStatics().getPlayerData().update(this.player2);
    Database.getStatics().getPlayerData().logs_echange(player1.getName(), player2.getName(),str.toString());	
  }

  protected void giveObject(Pair<Integer, Integer> couple, GameObject object)
  {
    if(object==null)
      return;
    if((object.getQuantity()-couple.getRight())<1)
    {
      this.player2.removeItem(couple.getLeft());
      couple.right=object.getQuantity();
      SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player2,couple.getLeft());
      if(!this.player1.addObjet(object,true))
        Main.world.removeGameObject(couple.getLeft());
    }
    else
    {
      object.setQuantity(object.getQuantity()-couple.getRight(),this.player2);
      SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player2,object);
      GameObject newObj=GameObject.getCloneObjet(object,couple.getRight());
      if(this.player1.addObjet(newObj,true))
        World.addGameObject(newObj,true);
    }
  }

  public  void addItem(int guid, int qua, int pguid)
  {
    ok1=false;
    ok2=false;

    GameObject obj=World.getGameObject(guid);
    int i=0;

    if(this.player1.getId()==pguid)
      i=1;
    if(this.player2.getId()==pguid)
      i=2;

    if(qua==1)
      qua=1;
    String str=guid+"|"+qua;
    if(obj==null)
      return;
    if(obj.getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
      return;

    if(this instanceof CraftSecure)
    {
      ArrayList<ObjectTemplate> tmp=new ArrayList<>();
      for(Pair<Integer, Integer> couple : this.items1)
      {
        GameObject _tmp=World.getGameObject(couple.getLeft());
        if(_tmp==null)
          continue;
        if(!tmp.contains(_tmp.getTemplate()))
          tmp.add(_tmp.getTemplate());
      }
      for(Pair<Integer, Integer> couple : this.items2)
      {
        GameObject _tmp=World.getGameObject(couple.getLeft());
        if(_tmp==null)
          continue;
        if(!tmp.contains(_tmp.getTemplate()))
          tmp.add(_tmp.getTemplate());
      }

      if(!tmp.contains(obj.getTemplate()))
      {
        if(tmp.size()+1>((CraftSecure)this).getMaxCase())
        {
          SocketManager.GAME_SEND_MESSAGE((this.player1.getId()==pguid) ? this.player1 : this.player2,"Can not add more ingredients.","B9121B");
          return;
        }
      }
    }

    String add="|"+obj.getTemplate().getId()+"|"+obj.parseStatsString();
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok2,this.player2.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok2,this.player2.getId());
    if(i==1)
    {
      Pair<Integer, Integer> couple=getPairInList(items1,guid);
      if(couple!=null)
      {
        couple.right+=qua;
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player1,'O',"+",""+guid+"|"+couple.getRight());
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(),'O',"+",""+guid+"|"+couple.getRight()+add);
        return;
      }
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player1,'O',"+",str);
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(),'O',"+",str+add);
      items1.add(new Pair<>(guid,qua));
    }
    else if(i==2)
    {
      Pair<Integer, Integer> couple=getPairInList(items2,guid);
      if(couple!=null)
      {
        couple.right+=qua;
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player2,'O',"+",""+guid+"|"+couple.getRight());
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(),'O',"+",""+guid+"|"+couple.getRight()+add);
        return;
      }
      SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player2,'O',"+",str);
      SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(),'O',"+",str+add);
      items2.add(new Pair<>(guid,qua));
    }
  }

  public  void removeItem(int guid, int qua, int pguid)
  {
    int i=0;
    if(this.player1.getId()==pguid)
      i=1;
    else if(this.player2.getId()==pguid)
      i=2;
    ok1=false;
    ok2=false;

    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok1,this.player1.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player1.getGameClient(),ok2,this.player2.getId());
    SocketManager.GAME_SEND_EXCHANGE_OK(this.player2.getGameClient(),ok2,this.player2.getId());

    GameObject object=World.getGameObject(guid);
    if(object==null)
      return;
    String add="|"+object.getTemplate().getId()+"|"+object.parseStatsString();

    if(i==1)
    {
      Pair<Integer, Integer> couple=getPairInList(items1,guid);

      if(couple==null)
        return;
      int newQua=couple.getRight()-qua;

      if(newQua<1)
      {
        items1.remove(couple);
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player1,'O',"-",""+guid);
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(),'O',"-",""+guid);
      }
      else
      {
        couple.right=newQua;
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player1,'O',"+",""+guid+"|"+newQua);
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player2.getGameClient(),'O',"+",""+guid+"|"+newQua+add);
      }
    }
    else if(i==2)
    {
      Pair<Integer, Integer> couple=getPairInList(items2,guid);

      if(couple==null)
        return;
      int newQua=couple.getRight()-qua;

      if(newQua<1)
      {
        items2.remove(couple);
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(),'O',"-",""+guid);
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player2,'O',"-",""+guid);
      }
      else
      {
        couple.right=newQua;
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player1.getGameClient(),'O',"+",""+guid+"|"+newQua+add);
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player2,'O',"+",""+guid+"|"+newQua);
      }
    }
  }

  public  int getQuaItem(int itemID, int playerGuid)
  {
    ArrayList<Pair<Integer, Integer>> items;
    if(this.player1.getId()==playerGuid)
      items=items1;
    else
      items=items2;
    for(Pair<Integer, Integer> curCoupl : items)
      if(curCoupl.getLeft()==itemID)
        return curCoupl.getRight();
    return 0;
  }
}