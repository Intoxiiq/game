package soufix.entity.pet;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.game.World;
import soufix.job.fm.Rune;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

public class PetEntry
{
  private int objectId;
  private int template;
  private long lastEatDate;
  private int quaEat;
  private int pdv;
  private int corpulence;
  private boolean isEupeoh;

  public PetEntry(int Oid, int template, long lastEatDate, int quaEat, int pdv, int corpulence, boolean isEPO)
  {
    this.objectId=Oid;
    this.template=template;
    this.lastEatDate=lastEatDate;
    this.quaEat=quaEat;
    this.pdv=pdv;
    this.corpulence=corpulence;
    getCurrentStatsPoids();
    this.isEupeoh=isEPO;
  }

  public int getObjectId()
  {
    return this.objectId;
  }

  public int getTemplate()
  {
    return template;
  }

  public long getLastEatDate()
  {
    return this.lastEatDate;
  }

  public int getQuaEat()
  {
    return this.quaEat;
  }

  public int getPdv()
  {
    return this.pdv;
  }

  public int getCorpulence()
  {
    return this.corpulence;
  }

  public boolean getIsEupeoh()
  {
    return this.isEupeoh;
  }

  public String parseLastEatDate()
  {
    String hexDate="#";
    DateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date=formatter.format(this.lastEatDate);

    String[] split=date.split("\\s");

    String[] split0=split[0].split("-");
    hexDate+=Integer.toHexString(Integer.parseInt(split0[0]))+"#";
    int mois=Integer.parseInt(split0[1])-1;
    int jour=Integer.parseInt(split0[2]);
    hexDate+=Integer.toHexString(Integer.parseInt((mois<10 ? "0"+mois : mois)+""+(jour<10 ? "0"+jour : jour)))+"#";

    String[] split1=split[1].split(":");
    String heure=split1[0]+split1[1];
    hexDate+=Integer.toHexString(Integer.parseInt(heure));

    return hexDate;
  }

  public int parseCorpulence()
  {
    if(corpulence>0||corpulence<0)
      return 7;
    return 0;
  }

  public float getCurrentStatsPoids()
  {
    GameObject obj=World.getGameObject(this.objectId);
    if(obj==null)
      return 0;
    float cumul=0;
    for(Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet())
      if(entry.getKey()!=Integer.parseInt("320",16)&&entry.getKey()!=Integer.parseInt("326",16)&&entry.getKey()!=Integer.parseInt("328",16)) //textstats of pet
        if(Rune.getRuneByStatId(Integer.toHexString(entry.getKey()))!=null)
        {
          Rune rune=Rune.getRuneByStatId(Integer.toHexString(entry.getKey()));
          cumul+=rune.getPower()/rune.getStatsAdd()*entry.getValue();
        }
    return cumul;
  }

  public int getMaxStat()
  {
    return Main.world.getPets(this.template).getMaxStat();
  }

  public void looseFight(Player player)
  {
    GameObject obj=World.getGameObject(this.objectId);
    if(obj==null)
      return;
    Pet pets=Main.world.getPets(obj.getTemplate().getId());
    if(pets==null)
      return;

    this.pdv--;
    obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
    obj.getTxtStat().put(Constant.STATS_PETS_PDV,Integer.toHexString((this.pdv>0 ? (this.pdv) : 0)));

    if(this.pdv<=0)
    {
      this.pdv=0;
      obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
      obj.getTxtStat().put(Constant.STATS_PETS_PDV,Integer.toHexString(0));//Mise a 0 des pdv

      if(pets.getDeadTemplate()==0)// Si Pets DeadTemplate = 0 remove de l'item et pet entry
      {
        Main.world.removeGameObject(obj.getGuid());
        player.removeItem(obj.getGuid());
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player,obj.getGuid());
        if(player.addObjet(obj,true))//Si le joueur n'avait pas d'item similaire
          World.addGameObject(obj,true);
      }
      else
      {
        obj.setTemplate(pets.getDeadTemplate());
        if(obj.getPosition()==Constant.ITEM_POS_FAMILIER)
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
          SocketManager.GAME_SEND_OBJET_MOVE_PACKET(player,obj);
        }
      }
      SocketManager.GAME_SEND_Im_PACKET(player,"154");
    }
    SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player,obj);
    Database.getDynamics().getPetData().update(this);
  }

  public void eat(Player p, int min, int max, int statsID, GameObject feed)
  {
    GameObject obj=World.getGameObject(this.objectId);
    if(obj==null)
      return;
    Pet pets=Main.world.getPets(obj.getTemplate().getId());
    if(pets==null)
      return;

    if(this.corpulence<=0)//Si il est maigrichon (X repas ratés) on peu le nourrir plusieurs fois
    {
      //Update du petsEntry
      this.lastEatDate=System.currentTimeMillis();
      this.corpulence++;
      this.quaEat++;
      //Update de l'item
      obj.getTxtStat().remove(Constant.STATS_PETS_POIDS);
      obj.getTxtStat().put(Constant.STATS_PETS_POIDS,Integer.toString(this.corpulence));
      obj.getTxtStat().remove(Constant.STATS_PETS_DATE);
      obj.getTxtStat().put(Constant.STATS_PETS_DATE,this.getLastEatDate()+"");
      SocketManager.GAME_SEND_Im_PACKET(p,"029");
      if(this.quaEat>=3)
      {
        //Update de l'item
        if((this.getIsEupeoh() ? pets.getMax()*1.1 : pets.getMax())>this.getCurrentStatsPoids()) //Si il est sous l'emprise d'EPO on augmente de +10% le jet maximum
        {
          if(obj.getStats().getMap().containsKey(statsID))
          {
            int value=obj.getStats().getMap().get(statsID)+Main.world.getPets(World.getGameObject(this.objectId).getTemplate().getId()).getGain();
            if(value>this.getMaxStat())
              value=this.getMaxStat();
            obj.getStats().getMap().remove(statsID);
            obj.getStats().addOneStat(statsID,value);
          }
          else
            obj.getStats().addOneStat(statsID,pets.getGain());
        }
        this.quaEat=0;
      }
    }
    else if(((this.lastEatDate+(min*3600000))>System.currentTimeMillis())&&this.corpulence>=0)//Si il n'est pas maigrichon, et on le nourri trop rapidement
    {
      //Update du petsEntry
      this.lastEatDate=System.currentTimeMillis();
      this.corpulence++;
      //Update de l'item
      obj.getTxtStat().remove(Constant.STATS_PETS_POIDS);
      obj.getTxtStat().put(Constant.STATS_PETS_POIDS,Integer.toString(this.corpulence));
      obj.getTxtStat().remove(Constant.STATS_PETS_DATE);
      obj.getTxtStat().put(Constant.STATS_PETS_DATE,this.getLastEatDate()+"");

      if(corpulence==1)
      {
        this.quaEat++;
        SocketManager.GAME_SEND_Im_PACKET(p,"026");
      }
      else
      {
        this.pdv--;
        obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
        obj.getTxtStat().put(Constant.STATS_PETS_PDV,Integer.toHexString((this.pdv>0 ? (this.pdv) : 0)));
        SocketManager.GAME_SEND_Im_PACKET(p,"027");
      }
      if(this.quaEat>=3)
      {
        //Update de l'item
        if((this.getIsEupeoh() ? pets.getMax()*1.1 : pets.getMax())>this.getCurrentStatsPoids())//Si il est sous l'emprise d'EPO on augmente de +10% le jet maximum
        {
          if(obj.getStats().getMap().containsKey(statsID))
          {
            int value=obj.getStats().getMap().get(statsID)+Main.world.getPets(World.getGameObject(this.objectId).getTemplate().getId()).getGain();
            if(value>this.getMaxStat())
              value=this.getMaxStat();
            obj.getStats().getMap().remove(statsID);
            obj.getStats().addOneStat(statsID,value);
          }
          else
            obj.getStats().addOneStat(statsID,pets.getGain());
        }
        this.quaEat=0;
      }
    }
    else if(((this.lastEatDate+(min*3600000))<System.currentTimeMillis())&&this.corpulence>=0)//Si il n'est pas maigrichon, et que le temps minimal est écoulé
    {
      //Update du petsEntry
      this.lastEatDate=System.currentTimeMillis();
      obj.getTxtStat().remove(Constant.STATS_PETS_DATE);
      obj.getTxtStat().put(Constant.STATS_PETS_DATE,this.getLastEatDate()+"");

      if(statsID!=0)
        this.quaEat++;
      else
        return;
      if(this.quaEat>=3)
      {
        //Update de l'item
        if((this.getIsEupeoh() ? pets.getMax()*1.1 : pets.getMax())>this.getCurrentStatsPoids())//Si il est sous l'emprise d'EPO on augmente de +10% le jet maximum
        {
          if(obj.getStats().getMap().containsKey(statsID))
          {
            int value=obj.getStats().getMap().get(statsID)+Main.world.getPets(World.getGameObject(this.objectId).getTemplate().getId()).getGain();
            if(value>this.getMaxStat())
              value=this.getMaxStat();
            obj.getStats().getMap().remove(statsID);
            obj.getStats().addOneStat(statsID,value);
          }
          else
            obj.getStats().addOneStat(statsID,pets.getGain());
        }
        this.quaEat=0;
      }
      SocketManager.GAME_SEND_Im_PACKET(p,"032");
    }

    if(this.pdv<=0)
    {
      this.pdv=0;
      obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
      obj.getTxtStat().put(Constant.STATS_PETS_PDV,Integer.toHexString((this.pdv>0 ? (this.pdv) : 0)));//Mise a 0 des pdv
      if(pets.getDeadTemplate()==0)// Si Pets DeadTemplate = 0 remove de l'item et pet entry
      {
        Main.world.removeGameObject(obj.getGuid());
        p.removeItem(obj.getGuid());
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(p,obj.getGuid());
      }
      else
      {
        obj.setTemplate(pets.getDeadTemplate());

        if(obj.getPosition()==Constant.ITEM_POS_FAMILIER)
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
          SocketManager.GAME_SEND_OBJET_MOVE_PACKET(p,obj);
        }
      }
      SocketManager.GAME_SEND_Im_PACKET(p,"154");
    }
    if(obj.getTxtStat().containsKey(Constant.STATS_PETS_REPAS))
    {
      obj.getTxtStat().remove(Constant.STATS_PETS_REPAS);
      obj.getTxtStat().put(Constant.STATS_PETS_REPAS,Integer.toHexString(feed.getTemplate().getId()));
    }
    else
    {
      obj.getTxtStat().put(Constant.STATS_PETS_REPAS,Integer.toHexString(feed.getTemplate().getId()));
    }
    SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p,obj);
    //Database.getDynamics().getObjectData().update(obj);
    //Database.getDynamics().getPetData().update(this);
  }

  public void eatSouls(Player p, Map<Integer, Integer> souls)
  {
    GameObject obj=World.getGameObject(this.objectId);
    if(obj==null)
      return;
    Pet pet=Main.world.getPets(obj.getTemplate().getId());
    if(pet==null||pet.getType()!=1)
      return;

    try
    {
      for(Entry<Integer, Integer> entry : souls.entrySet())
      {
        int soul=entry.getKey();
        int count=entry.getValue();
        if(pet.canEat(-1,-1,soul))
        {
          int statsID=pet.statsIdByEat(-1,-1,soul);
          if(statsID==0)
            return;
          int soulCount=(obj.getSoulStat().get(soul)!=null ? obj.getSoulStat().get(soul) : 0);
          if(soulCount>0)
          {
            obj.getSoulStat().remove(soul);
            obj.getSoulStat().put(soul,count+soulCount);
            this.lastEatDate=System.currentTimeMillis();
            obj.getTxtStat().remove(Constant.STATS_PETS_DATE);
            obj.getTxtStat().put(Constant.STATS_PETS_DATE,this.getLastEatDate()+"");
          }
          else
            obj.getSoulStat().put(soul,count);
        }
      }

      for(Entry<Integer, ArrayList<Map<Integer, Integer>>> ent : pet.getMonsters().entrySet())
        for(Map<Integer, Integer> entry : ent.getValue())
          for(Entry<Integer, Integer> monsterEntry : entry.entrySet())
          {
            if(pet.getNumbMonster(ent.getKey(),monsterEntry.getKey())!=0)
            {
              int pts=0;
              for(Entry<Integer, Integer> list : obj.getSoulStat().entrySet())
              {
                if(pet.getNumbMonster(ent.getKey(),list.getKey())!=0) //Do not eat monsters not in eatable list, divide-by-zero handler
                {
                  int statsAdd=(int)Math.floor(list.getValue()/pet.getNumbMonster(ent.getKey(),list.getKey()));
                  float statPower=0;
                  if(Rune.getRuneByStatId(Integer.toHexString(ent.getKey()))!=null)
                  {
                    Rune rune=Rune.getRuneByStatId(Integer.toHexString(ent.getKey()));
                    statPower=rune.getPower()/rune.getStatsAdd();
                  }
                  int max=(int)Math.floor((this.getIsEupeoh() ? pet.getMax()*1.1 : pet.getMax()));
                  while((max<(getCurrentStatsPoids()+statsAdd*statPower))&&statsAdd!=0)
                  {
                    statsAdd--;
                  }

                  pts+=statsAdd;
                }
              }

              if(pts>0)
              {
                if(obj.getStats().getMap().containsKey(ent.getKey()))
                {
                  int nbr=obj.getStats().getMap().get(ent.getKey());
                  if(pts+nbr>this.getMaxStat())
                    pts=this.getMaxStat()-nbr;
                  pts+=nbr;

                  obj.getStats().getMap().remove(ent.getKey());
                }
                obj.getStats().getMap().put(ent.getKey(),pts);
              }
            }
          }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      System.out.println("Error : "+e.getMessage());
    }
    SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p,obj);
    //Database.getDynamics().getObjectData().update(obj);
    //Database.getDynamics().getPetData().update(this);
  }

  public void updatePets(Player p, int max)
  {
    GameObject obj=World.getGameObject(this.objectId);
    if(obj==null)
      return;
    Pet pets=Main.world.getPets(obj.getTemplate().getId());
    if(pets==null)
      return;
    if(this.pdv<=0&&obj.getTemplate().getId()==pets.getDeadTemplate())
      return;//Ne le met pas a jour si deja mort

    if(this.lastEatDate+(max*3600000)<System.currentTimeMillis()) //Oublier de le nourrir
    {
      int nbrepas=(int)Math.floor((System.currentTimeMillis()-this.lastEatDate)/(max*3600000));
      //Perte corpulence
      this.corpulence=this.corpulence-nbrepas;

      if(nbrepas!=0)
      {
        obj.getTxtStat().remove(Constant.STATS_PETS_POIDS);
        obj.getTxtStat().put(Constant.STATS_PETS_POIDS,Integer.toString(this.corpulence));
      }
    }
    else
    {
      if(this.pdv>0)
        SocketManager.GAME_SEND_Im_PACKET(p,"025");
    }

    if(this.pdv<=0)
    {
      this.pdv=0;
      obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
      obj.getTxtStat().put(Constant.STATS_PETS_PDV,Integer.toHexString((this.pdv>0 ? (this.pdv) : 0)));

      if(pets.getDeadTemplate()==0)//Si Pets DeadTemplate = 0 remove de l'item et pet entry
      {
        Main.world.removeGameObject(obj.getGuid());
        p.removeItem(obj.getGuid());
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(p,obj.getGuid());
      }
      else
      {
        obj.setTemplate(pets.getDeadTemplate());
        if(obj.getPosition()==Constant.ITEM_POS_FAMILIER)
        {
          obj.setPosition(Constant.ITEM_POS_NO_EQUIPED);
          SocketManager.GAME_SEND_OBJET_MOVE_PACKET(p,obj);
        }
      }
      SocketManager.GAME_SEND_Im_PACKET(p,"154");
    }
    SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p,obj);
    //Database.getDynamics().getObjectData().update(obj);
    //Database.getDynamics().getPetData().update(this);
  }

  public void resurrection()
  {
    GameObject obj=World.getGameObject(this.objectId);
    if(obj==null)
      return;

    obj.setTemplate(this.template);

    this.pdv=1;
    this.corpulence=0;
    this.quaEat=0;
    this.lastEatDate=System.currentTimeMillis();
    obj.getTxtStat().remove(Constant.STATS_PETS_DATE);
    obj.getTxtStat().put(Constant.STATS_PETS_DATE,this.getLastEatDate()+"");

    obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
    obj.getTxtStat().put(Constant.STATS_PETS_PDV,Integer.toHexString(this.pdv));
    //Database.getDynamics().getObjectData().update(obj);
    Database.getDynamics().getPetData().update(this);
  }

  public void restoreLife(Player p)
  {
    GameObject obj=World.getGameObject(this.objectId);
    if(obj==null)
      return;
    Pet pets=Main.world.getPets(obj.getTemplate().getId());
    if(pets==null)
      return;

    if(this.pdv>=10)
    {
      //Il la mange pas de pdv en plus
      SocketManager.GAME_SEND_Im_PACKET(p,"032");
    }
    else if(this.pdv<10&&this.pdv>0)
    {
      this.pdv++;

      obj.getTxtStat().remove(Constant.STATS_PETS_PDV);
      obj.getTxtStat().put(Constant.STATS_PETS_PDV,Integer.toHexString(this.pdv));

      //this.lastEatDate = System.currentTimeMillis();
      SocketManager.GAME_SEND_Im_PACKET(p,"032");
    }
    else
    {
      return;
    }
    //Database.getDynamics().getObjectData().update(obj);
    Database.getDynamics().getPetData().update(this);
  }

  public void giveEpo(Player p)
  {
    GameObject obj=World.getGameObject(this.objectId);
    if(obj==null)
      return;
    Pet pets=Main.world.getPets(obj.getTemplate().getId());
    if(pets==null)
      return;
    if(this.isEupeoh)
      return;
    obj.getTxtStat().put(Constant.STATS_PETS_EPO,Integer.toHexString(1));
    SocketManager.GAME_SEND_Im_PACKET(p,"032");
    SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(p,obj);
    Database.getDynamics().getPetData().update(this);
  }
}