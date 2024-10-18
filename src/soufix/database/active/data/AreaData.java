package soufix.database.active.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.zaxxer.hikari.HikariDataSource;

import soufix.area.Area;
import soufix.database.active.AbstractDAO;
import soufix.main.Main;

public class AreaData extends AbstractDAO<Area>
{
  public AreaData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(Area area)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `area_data` SET `alignement` = ?, `Prisme` = ? WHERE id = ?");
      p.setInt(1,area.getAlignement());
      p.setInt(2,area.getPrismId());
      p.setInt(3,area.getId());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("Area_dataData update",e);
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
      result=getData("SELECT * from area_data");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        int id=RS.getInt("id");
        int alignement=RS.getInt("alignement");
        int prisme=RS.getInt("Prisme");
        Area A=Main.world.getArea(id);

        if(A!=null)
        {
          A.setAlignement(alignement);
          A.setPrismId(prisme);
        }
      }
    }
    catch(SQLException e)
    {
      super.sendError("Area_dataData load",e);
    } finally
    {
      close(result);
    }
  }
}
