package soufix.entity.exchange;

import java.util.ArrayList;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.entity.npc.NpcTemplate;
import soufix.game.World;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.game.World.Couple;

public class NpcExchange {
    private Player player;
    private NpcTemplate npc;
    private long kamas1 = 0;
    private long kamas2 = 0;
    private ArrayList<Couple<Integer,Integer>> items1 = new ArrayList<Couple<Integer,Integer>>();
    private ArrayList<Couple<Integer,Integer>> items2 = new ArrayList<Couple<Integer,Integer>>();
    private boolean ok1;
    private boolean ok2;

    public NpcExchange(Player p, NpcTemplate n) {
        this.player = p;
        this.setNpc(n);
    }

    public long getKamas(boolean b) {
        if(b)return this.kamas2;
        return this.kamas1;
    }

    public  void toogleOK(boolean paramBoolean) {
        if(paramBoolean) {
            this.ok2 = (!this.ok2);
            SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
        } else {
            this.ok1 = (!this.ok1);
            SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
        }
        if((this.ok2) && (this.ok1))
            apply();
    }

    public  void setKamas(boolean ok, long kamas) {
        if(kamas < 0L)
            return;
        this.ok2 = (this.ok1 = false);
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
        if(ok) {
            this.kamas2 = kamas;
            SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'G', "", String.valueOf(kamas));
            putAllGiveItem();
            return;
        }
        if(kamas > this.player.getKamas())
            return;
        this.kamas1 = kamas;
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'G', "", String.valueOf(kamas));
        putAllGiveItem();
    }

    public void cancel() {
        if((this.player.getAccount() != null) && (this.player.getGameClient() != null))
            SocketManager.GAME_SEND_EV_PACKET(this.player.getGameClient());
        this.player.setExchangeAction(null);
    }

    public  void apply() {
        for(Couple<Integer, Integer> couple : items1) {
            if(couple.second == 0)continue;
            if(World.getGameObject(couple.first).getPosition() != Constant.ITEM_POS_NO_EQUIPED)continue;
            if(!this.player.hasItemGuid(couple.first)) {
                couple.second = 0;//On met la quantité a 0 pour éviter les problemes
                continue;
            }
            GameObject obj = World.getGameObject(couple.first);
            if((obj.getQuantity() - couple.second) < 1) {
                this.player.removeItem(couple.first);
                Main.world.removeGameObject(World.getGameObject(couple.first).getGuid());
                couple.second = obj.getQuantity();
                SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this.player, couple.first);
            } else {
                obj.setQuantity(obj.getQuantity()-couple.second,this.player);
                SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this.player, obj);
            }
        }

        for(Couple<Integer, Integer> couple1 : items2) {
            if(couple1.second == 0)continue;
            if(Main.world.getObjTemplate(couple1.first) == null)continue;
            GameObject obj1 = Main.world.getObjTemplate(couple1.first).createNewItem(couple1.second, false);
            if(this.player.addObjet(obj1, true))
            	World.addGameObject(obj1, true);
            SocketManager.GAME_SEND_Im_PACKET(this.player, "021;" + couple1.second + "~" + couple1.first);
        }
        player.addKamas(kamas2,false);
        Main.world.kamas_total += kamas2;
        SocketManager.GAME_SEND_Im_PACKET(player,"045;"+kamas2);
        SocketManager.GAME_SEND_STATS_PACKET(player);
        this.player.setExchangeAction(null);
        SocketManager.GAME_SEND_EXCHANGE_VALID(this.player.getGameClient(), 'a');
        //Database.getStatics().getPlayerData().update(this.player);
    }

    public void addItem(int obj, int qua) {
        if(qua <= 0)return;
        if(World.getGameObject(obj) == null)return;
        this.ok1 = (this.ok2 = false);
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
        String str = obj + "|" + qua;
        Couple<Integer,Integer> couple = getCoupleInList(items1, obj);
        if(npc.getId() == 874){
        int prix = 0;
        if(World.getGameObject(obj).getTemplate().getStrTemplate().contains("32c#")) {
        	 prix=qua*(World.getGameObject(obj).getTemplate().getPrice()/10);//Calcul du prix de vente (prix d'achat/10)
            	
        }else {
          prix=qua*((World.getGameObject(obj).getTemplate().getPrice()/10) < 200 ? (World.getGameObject(obj).getTemplate().getPrice()/10) : 200);//Calcul du prix de vente (prix d'achat/10)
            	
        }
         if(World.getGameObject(obj).getTemplate().getType() == 78)
        	 prix = 0;
         kamas2 +=prix;
         if(kamas2 != 0)
        	 this.ok2 = true;
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'G', "", String.valueOf(kamas2));
        }
        if(couple != null) {
            couple.second += qua;
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "+", ""+obj+"|"+couple.second);
            putAllGiveItem();
            return;
        }
        SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "+", str);
        items1.add(new Couple<Integer,Integer>(obj,qua));
        putAllGiveItem();
    }

    public void removeItem(int guid, int qua) {
        if(qua < 0)return;
        this.ok1 = (this.ok2 = false);
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok1, this.player.getId());
        SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
        if(World.getGameObject(guid) == null)return;
        Couple<Integer,Integer> couple = getCoupleInList(items1,guid);
        if(npc.getId() == 874){
        int prix = 0;
        if(World.getGameObject(guid).getTemplate().getStrTemplate().contains("32c#")) {
        	 prix=qua*(World.getGameObject(guid).getTemplate().getPrice()/10);//Calcul du prix de vente (prix d'achat/10)
            	
        }else {
          prix=qua*((World.getGameObject(guid).getTemplate().getPrice()/10) < 200 ? (World.getGameObject(guid).getTemplate().getPrice()/10) : 200);//Calcul du prix de vente (prix d'achat/10)
            	
        }
         if(World.getGameObject(guid).getTemplate().getType() == 78)
        	 prix = 0;
         kamas2 -=prix;
         if(kamas2 != 0)
        	 this.ok2 = true;
        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'G', "", String.valueOf(kamas2));
        }
        int newQua = couple.second - qua;
        if(newQua <1) {
            items1.remove(couple);
            putAllGiveItem();
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "-", ""+guid);
        } else {
            couple.second = newQua;
            SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(this.player, 'O', "+", ""+guid+"|"+newQua);
            putAllGiveItem();
        }
    }

    public int getQuaItem(int obj, boolean b) {
        ArrayList<Couple<Integer, Integer>> list;
        if(b)
            list = this.items2;
        else
            list = this.items1;
        for(Couple<Integer, Integer> item: list)
            if(item.first == obj)
                return item.second;
        return 0;
    }

    public void clearItems() {
        if(this.items2.isEmpty()) return;
        for(Couple<Integer, Integer> i: items2)
            SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'O', "-", i.first+"");
        this.kamas2 = 0;
        this.items2.clear();
        if(this.ok2) {
            this.ok2 = false;
            SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
        }
    }

    private  Couple<Integer, Integer> getCoupleInList(ArrayList<Couple<Integer, Integer>> items,int guid) {
        for(Couple<Integer, Integer> couple : items)
            if(couple.first == guid)
                return couple;
        return null;
    }

    public  void putAllGiveItem() {
        ArrayList<Couple<Integer,Integer>> objects = this.npc.checkGetObjects(this.items1);

        if(objects != null) {
            this.clearItems();
            for(Couple<Integer, Integer> object : objects) {
                if(object.second == -1) {
                    int kamas = object.first;

                    if(kamas == -1) {
                        for(Couple<Integer, Integer> pepite : this.items1)
                            if(World.getGameObject(pepite.first).getTemplate().getId() == 1)
                                this.kamas2 += Integer.parseInt(World.getGameObject(pepite.first).getTxtStat().get(990).substring(9, 13)) * pepite.second;

                        SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'G', "", String.valueOf(this.kamas2));
                        continue;
                    }

                    this.kamas2 += kamas;
                    SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'G', "", String.valueOf(this.kamas2));
                    continue;
                }
                String str = object.first + "|" + object.second + "|" + object.first + "|" + Main.world.getObjTemplate(object.first).getStrTemplate();
                this.items2.add(new Couple<Integer, Integer>(object.first, object.second));
                SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(this.player.getGameClient(), 'O', "+", str);
            }
            if(!this.ok2) {
                this.ok2 = true;
                SocketManager.GAME_SEND_EXCHANGE_OK(this.player.getGameClient(), this.ok2);
            }
        } else {
            this.clearItems();
        }
    }

    public NpcTemplate getNpc() {
        return npc;
    }

    public void setNpc(NpcTemplate npc) {
        this.npc = npc;
    }
}