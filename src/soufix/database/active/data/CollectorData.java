package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.database.active.AbstractDAO;
import soufix.entity.Collector;
import soufix.main.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CollectorData extends AbstractDAO<Collector>
{
  public CollectorData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(Collector P)
  {
	  	if(P == null)
			return false;
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `percepteurs` SET `objets` = ?,`kamas` = ?,`xp` = ? WHERE guid = ?");
      if(p == null) {
      p=getPreparedStatement("UPDATE `percepteurs` SET `objets` = ?,`kamas` = ?,`xp` = ? WHERE guid = ?");	  
      }
      if(p == null)
      return false;	  
      p.setString(1,P.parseItemCollector());
      p.setLong(2,P.getKamas());
      p.setLong(3,P.getXp());
      p.setInt(4,P.getId());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("PercepteurData update",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public int load()
  {
    Result result=null;
    int nbr=0;
    try
    {
      result=getData("SELECT * from percepteurs");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        GameMap map=Main.world.getMap(RS.getShort("mapid"));
        if(map==null)
          continue;

        Player perso=null;
        Integer poseur_id=RS.getInt("poseur_id");
        if(poseur_id!=null&&poseur_id>0)
        {
          perso=Main.world.getPlayer(poseur_id);
        }

        String date=RS.getString("date");
        long time=0;
        if(date!=null&&!date.equals(""))
        {
          time=Long.parseLong(date);
        }
        Main.world.kamas_total += RS.getLong("kamas");
        Main.world.addCollector(new Collector(RS.getInt("guid"),RS.getShort("mapid"),RS.getInt("cellid"),RS.getByte("orientation"),RS.getInt("guild_id"),RS.getShort("N1"),RS.getShort("N2"),perso,time,RS.getString("objets"),RS.getLong("kamas"),RS.getLong("xp")));
        nbr++;
      }
    }
    catch(SQLException e)
    {
      super.sendError("PercepteurData load",e);
    } finally
    {
      close(result);
    }
    return nbr;
  }

  public boolean delete(int id)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("DELETE FROM percepteurs WHERE guid = ?");
      p.setInt(1,id);
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("PercepteurData delete",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public boolean add(int guid, int mapid, int guildID, int poseur_id, long date, int cellid, int o, short N1, short N2)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("INSERT INTO `percepteurs` VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
      p.setInt(1,guid);
      p.setInt(2,mapid);
      p.setInt(3,cellid);
      p.setInt(4,o);
      p.setInt(5,guildID);
      p.setInt(6,poseur_id);
      p.setString(7,Long.toString(date));
      p.setShort(8,N1);
      p.setShort(9,N2);
      p.setString(10,"");
      p.setLong(11,0);
      p.setLong(12,0);
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("PercepteurData add",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public int getId()
  {
    Result result=null;
    int i=-50;//Pour éviter les conflits avec touts autre NPC
    try
    {
      result=getData("SELECT `guid` FROM `percepteurs` ORDER BY `guid` ASC LIMIT 0 , 1");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        i=RS.getInt("guid")-1;
      }
    }
    catch(SQLException e)
    {
      super.sendError("PercepteurData getId",e);
      close(result);
    } finally
    {
      close(result);
    }
    if(i>=-9999)
      i=-10000;

    return i;
  }
}
