package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.area.map.entity.Tutorial;
import soufix.database.active.AbstractDAO;
import soufix.main.Main;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TutorialData extends AbstractDAO<Tutorial>
{
  public TutorialData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(Tutorial obj)
  {
    return false;
  }

  public void load()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM tutoriel");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        int id=RS.getInt("id");
        String start=RS.getString("start");
        String reward=RS.getString("reward1")+"$"+RS.getString("reward2")+"$"+RS.getString("reward3")+"$"+RS.getString("reward4");
        String end=RS.getString("end");
        Main.world.addTutorial(new Tutorial(id,reward,start,end));
      }
    }
    catch(SQLException e)
    {
      super.sendError("TutorielData load",e);
      Main.stop("unknown");
    } finally
    {
      close(result);
    }
  }
}
