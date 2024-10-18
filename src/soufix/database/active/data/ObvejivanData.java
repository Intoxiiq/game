package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.object.GameObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ObvejivanData extends AbstractDAO<GameObject>
{

  public ObvejivanData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(GameObject obj)
  {
    return false;
  }
  
  //2.0 - Parasymbic/Alyverol item dissociation bugfix
  public void add(GameObject obvi, GameObject victime)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("INSERT INTO `world.entity.obvijevans`(`id`, `template`) VALUES(?, ?);");
      p.setInt(1,obvi.getTemplate().getId());
      p.setInt(2,victime.getGuid());
      execute(p);
    }
    catch(Exception e)
    {
      super.sendError("ObvejivanData add",e);
    } finally
    {
      close(p);
    }
  }

  //2.0 - Parasymbic/Alyverol item dissociation bugfix
  public int getId(GameObject object)
  {
	  if(object == null)
		  return 0;
    Result result=null;
    int id=-1;
    try
    {
      result=getData("SELECT * FROM `world.entity.obvijevans` WHERE `template` = '"+object.getGuid()+"';");
      ResultSet resultSet=result.resultSet;
      if(resultSet.next())
        id=resultSet.getInt("id");
    }
    catch(SQLException e)
    {
      super.sendError("ObvejivanData getId",e);
    } finally
    {
      close(result);
    }
    return id;
  }
  
  public void delete(GameObject object)
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM `world.entity.obvijevans` WHERE `template` = '"+object.getGuid()+"';");
      ResultSet resultSet=result.resultSet;
      if(resultSet.next())
      {
          PreparedStatement ps=getPreparedStatement("DELETE FROM `world.entity.obvijevans` WHERE `template` = '"+object.getGuid()+"';");
          execute(ps);
      }
    }
    catch(SQLException e)
    {
      super.sendError("ObvejivanData getAndDelete",e);
    } finally
    {
      close(result);
    }
  }
}
