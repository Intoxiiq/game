package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.job.Job;
import soufix.main.Main;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JobData extends AbstractDAO<Job>
{
  public JobData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(Job obj)
  {
    return false;
  }

  public void load()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * from jobs_data");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {

        String skills="";
        if(RS.getString("skills")!=null)
          skills=RS.getString("skills");
        Main.world.addJob(new Job(RS.getInt("id"),RS.getString("tools"),RS.getString("crafts"),skills,RS.getString("name")));
      }
    }
    catch(SQLException e)
    {
      super.sendError("Jobs_dataData load",e);
    } finally
    {
      close(result);
    }
  }
}
