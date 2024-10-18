package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.main.Main;
import soufix.other.Dopeul;
import soufix.utility.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DungeonData extends AbstractDAO<Object>
{
  public DungeonData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(Object obj)
  {
    return false;
  }

  public void load()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM donjons");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        Dopeul.getDonjons().put(RS.getInt("map"),new Pair<>(RS.getInt("npc"),RS.getInt("key")));
      }
    }
    catch(SQLException e)
    {
      super.sendError("DonjonData load",e);
      Main.stop("unknown");
    } finally
    {
      close(result);
    }
  }

  //v2.7 - Replaced String += with StringBuilder
  public String get_all_keys()
  {
    Result result=null;
    try
    {
      result=getData("SELECT key FROM donjons");
      ResultSet RS=result.resultSet;
      StringBuilder keys=new StringBuilder();
      while(RS.next())
      {
        String key=Integer.toHexString(RS.getInt("key"));
        keys.append(keys.toString().isEmpty() ? key : ","+key);
      }
      return keys.toString();
    }
    catch(SQLException e)
    {
      super.sendError("DonjonData get_all_keys",e);
    } finally
    {
      close(result);
    }
    return "";
  }
}
