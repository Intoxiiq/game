package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.entity.boss.Bandit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GangsterData extends AbstractDAO<Bandit>
{
  public GangsterData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(Bandit obj)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `bandits` SET `time` = ?");
      p.setLong(1,obj.getTime());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("BanditData update",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public void load()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM bandits");
      ResultSet RS=result.resultSet;
      if(RS.next())
      {
        new Bandit(RS.getString("mobs"),RS.getString("maps"),RS.getLong("time"));
      }
    }
    catch(SQLException e)
    {
      super.sendError("BanditData load",e);
    } finally
    {
      close(result);
    }
  }
}
