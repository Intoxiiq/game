package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.area.map.entity.House;
import soufix.area.map.entity.Trunk;
import soufix.client.Player;
import soufix.database.active.AbstractDAO;
import soufix.main.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrunkData extends AbstractDAO<Trunk>
{
  public TrunkData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(Trunk t)
  {
	  int id = 0;
    PreparedStatement p=null;
    try
    {
    	if(t == null)
    	return true;	
    	id = t.getId();
      p=getPreparedStatement("UPDATE `coffres` SET `kamas`=?, `object`=? WHERE `id`=?");
      p.setLong(1,t.getKamas());
      p.setString(2,t.parseTrunkObjetsToDB());
      p.setInt(3,t.getId());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("CoffreData update erreur id "+id,e);
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
      result=getData("SELECT * from coffres");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        int id=RS.getInt("id");
        String objects=RS.getString("object");
        objects=(objects==null||objects.equals(" ") ? "" : objects);
        int kamas=RS.getInt("kamas");
        Main.world.kamas_total += kamas;
        int owner_id=RS.getInt("owner_id");
        String key=RS.getString("key");
        Trunk t=Main.world.getTrunk(id);
        if(t==null)
          continue;
        t.setObjects(objects);
        t.setKamas(kamas);
        t.setOwnerId(owner_id);
        t.setKey(key);
        nbr++;
      }
    }
    catch(SQLException e)
    {
      super.sendError("CoffreData load",e);
    } finally
    {
      close(result);
    }
    return nbr;
  }

  public void update(Player player, House house)
  {
    PreparedStatement p=null;

    for(Trunk trunk : Trunk.getTrunksByHouse(house))
    {
      if(trunk.getOwnerId()!=player.getAccID())
      {
        trunk.setOwnerId(player.getAccID());
        trunk.setKey("-");

        try
        {
          p=getPreparedStatement("UPDATE `coffres` SET `owner_id`=?, `key`='-' WHERE `id`=?");
          p.setInt(1,player.getAccID());
          p.setInt(2,trunk.getId());
          execute(p);
        }
        catch(SQLException e)
        {
          super.sendError("CoffreData update",e);
        } finally
        {
          close(p);
        }
      }
    }
  }

  public void insert(Trunk trunk)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("INSERT INTO `coffres` (`id`, `object`, `kamas`, `key`, `owner_id`) "+"VALUES (?, ?, ?, ?, ?)");
      p.setInt(1,trunk.getId());
      p.setString(2,"");
      p.setInt(3,0);
      p.setString(4,"-");
      p.setInt(5,trunk.getOwnerId());
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("Coffre insert",e);
    } finally
    {
      close(p);
    }
  }

  public void updateCode(Player P, Trunk t, String packet)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `coffres` SET `key`=? WHERE `id`=? AND owner_id=?");
      p.setString(1,packet);
      p.setInt(2,t.getId());
      p.setInt(3,P.getAccID());
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("CoffreData updateCode",e);
    } finally
    {
      close(p);
    }
  }
}
