package soufix.entity.boss;

import soufix.area.map.GameMap;
import soufix.common.Formulas;
import soufix.database.Database;
import soufix.entity.monster.Monster;
import soufix.main.Main;

import java.util.ArrayList;

public class Bandit
{
  private static Bandit bandits;
  private final ArrayList<Monster> monsters=new ArrayList<>();
  private final ArrayList<GameMap> maps=new ArrayList<>();
  private long time;
  private boolean isPop=false;

  public Bandit(String mobs, String maps, long time)
  {
    if(!mobs.equalsIgnoreCase(""))
    {
      for(String mob : mobs.split(","))
      {
        Integer _mob=null;
        try
        {
          _mob=Integer.parseInt(mob);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        if(_mob==null)
          continue;

        Monster monstre=Main.world.getMonstre(_mob);
        if(monstre==null)
        {
          continue;
        }
        this.monsters.add(monstre);
      }
    }

    if(!maps.equalsIgnoreCase(""))
    {
      for(String map : maps.split(","))
      {
        Short _map=null;
        try
        {
          _map=Short.parseShort(map);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        if(_map==null)
          continue;

        GameMap _Map=Main.world.getMap(_map);
        if(_Map==null)
          continue;

        this.maps.add(_Map);
      }
    }

    this.time=time;
    bandits=this;

    run();
  }

  public static Bandit getBandits()
  {
    return bandits;
  }

  public static void run()
  {
    Bandit bandit=getBandits();
    if(bandit.isPop)
    {
     return;
    } else
    {
      long time=bandit.getTime();
      long actuel=System.currentTimeMillis();
      if(time<=0)
      {
        pop(bandit,actuel);
      } else
      {
        int random=Formulas.getRandomValue(6,18);
        long timeRandom=1000*60*60*random; // Temps en MS d'heures entre les repops des bandits
        if(time+timeRandom<=actuel)
        {
          pop(bandit,actuel);
        } else
        {
        	 return;
        }
      }
    }
  }

  //v2.7 - replaced String += with StringBuilder
  public static void pop(final Bandit bandit, final long actuel)
  {
    try
    {
      bandit.setTime(actuel);
      int nbMap=bandit.getMaps().size();
      int random=Formulas.getRandomValue(0,nbMap-1);
      GameMap map=bandit.getMaps().get(random);
      StringBuilder groupData=new StringBuilder();
      for(Monster monstre : bandit.getMonsters())
      {
        Integer id=monstre.getId();
        Integer lvl=monstre.getRandomGrade().getLevel();
        while(lvl==null)
        {
          lvl=monstre.getRandomGrade().getLevel();
        }
        if(groupData.toString().equalsIgnoreCase(""))
          groupData.append(id+","+lvl+","+lvl);
        else
          groupData.append(";"+id+","+lvl+","+lvl);
      }
      map.nextObjectId++;
      map.spawnNewGroup(false,map.getRandomFreeCellId(),groupData.toString(),"" , 0,"");
      Database.getDynamics().getGangsterData().update(bandit);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public ArrayList<Monster> getMonsters()
  {
    return this.monsters;
  }

  public ArrayList<GameMap> getMaps()
  {
    return this.maps;
  }

  public long getTime()
  {
    return this.time;
  }

  public void setTime(long time)
  {
    this.time=time;
  }

  public void setPop(boolean isPop)
  {
    this.isPop=isPop;
  }
}
