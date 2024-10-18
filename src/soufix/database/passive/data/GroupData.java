package soufix.database.passive.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.command.administration.Group;
import soufix.database.passive.AbstractDAO;
import soufix.main.Main;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GroupData extends AbstractDAO<Group>
{

  public GroupData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM `administration.groups`;");
      ResultSet RS=result.resultSet;

      while(RS.next())
        new Group(RS.getInt("id"),RS.getString("name"),RS.getBoolean("isPlayer"),RS.getString("commands"));
    }
    catch(SQLException e)
    {
      super.sendError("GroupData load",e);
      Main.stop("unknown");
    } finally
    {
      close(result);
    }
  }

  @Override
  public boolean update(Group obj)
  {
    return false;
  }
}
