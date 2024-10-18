package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.entity.monster.Monster;
import soufix.main.Main;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MonsterData extends AbstractDAO<Monster>
{
  public MonsterData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(Monster obj)
  {
    return false;
  }

  public void load()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM monsters");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        int id=RS.getInt("id");
        int gfxID=RS.getInt("gfxID");
        int align=RS.getInt("align");
        String colors=RS.getString("colors");
        String grades=RS.getString("grades");
        String spells=RS.getString("spells");
        String stats=RS.getString("stats");
        String statsInfos=RS.getString("statsInfos");
        String pdvs=RS.getString("pdvs");
        String pts=RS.getString("points");
        String inits=RS.getString("inits");
        String name=RS.getString("name");
        int mK=RS.getInt("minKamas");
        int MK=RS.getInt("maxKamas");
        int IAType=RS.getInt("AI_Type");
        String xp=RS.getString("exps");
        int aggroDistance=RS.getInt("aggroDistance");
        boolean capturable=RS.getInt("capturable")==1;
        Monster monster=new Monster(id,gfxID,align,colors,grades,spells,stats,statsInfos,pdvs,pts,inits,mK,MK,xp,IAType,capturable,aggroDistance,name);
        Main.world.addMobTemplate(id,monster);
      }
    }
    catch(SQLException e)
    {
      super.sendError("MonsterData load",e);
      Main.stop("unknown");
    } finally
    {
      close(result);
    }
  }

  public void reload()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM monsters");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        int id=RS.getInt("id");
        int gfxID=RS.getInt("gfxID");
        int align=RS.getInt("align");
        String colors=RS.getString("colors");
        String grades=RS.getString("grades");
        String spells=RS.getString("spells");
        String stats=RS.getString("stats");
        String statsInfos=RS.getString("statsInfos");
        String pdvs=RS.getString("pdvs");
        String pts=RS.getString("points");
        String inits=RS.getString("inits");
        String name=RS.getString("name");
        int mK=RS.getInt("minKamas");
        int MK=RS.getInt("maxKamas");
        int IAType=RS.getInt("AI_Type");
        String xp=RS.getString("exps");
        int aggroDistance=RS.getInt("aggroDistance");
        boolean capturable=(RS.getInt("capturable")==1);
        if(Main.world.getMonstre(id)==null)
        {
          Main.world.addMobTemplate(id,new Monster(id,gfxID,align,colors,grades,spells,stats,statsInfos,pdvs,pts,inits,mK,MK,xp,IAType,capturable,aggroDistance,name));
        }
        else
        {
          Main.world.getMonstre(id).setInfos(gfxID,align,colors,grades,spells,stats,statsInfos,pdvs,pts,inits,mK,MK,xp,IAType,capturable,aggroDistance);
        }
      }
    }
    catch(SQLException e)
    {
      super.sendError("MonsterData reload",e);
    } finally
    {
      close(result);
    }
  }
}
