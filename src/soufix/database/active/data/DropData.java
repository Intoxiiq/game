package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.entity.monster.Monster;
import soufix.game.World;
import soufix.main.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DropData extends AbstractDAO<World.Drop>
{
  public DropData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(World.Drop obj)
  {
    return false;
  }

  public void load()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * from drops");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        Monster MT=Main.world.getMonstre(RS.getInt("monsterId"));
        if(Main.world.getObjTemplate(RS.getInt("objectId"))!=null&&MT!=null)
        {
          String action=RS.getString("action");
          String condition="";

          if(!action.equals("-1")&&!action.equals("1")&&action.contains(":"))
          {
            condition=action.split(":")[1];
            action=action.split(":")[0];
          }
          ArrayList<Double> percents=new ArrayList<>();
          percents.add(RS.getDouble("percentGrade1"));
          percents.add(RS.getDouble("percentGrade2"));
          percents.add(RS.getDouble("percentGrade3"));
          percents.add(RS.getDouble("percentGrade4"));
          percents.add(RS.getDouble("percentGrade5"));

          MT.addDrop(new World.Drop(RS.getInt("objectId"),percents,RS.getInt("ceil"),Integer.parseInt(action),RS.getInt("level"),condition,RS.getInt("minDrop"),RS.getInt("maxDrop"),RS.getDouble("percentGrade5")));
        }
        else
        {
          if(MT==null&&RS.getInt("monsterId")==0)
          {
            String action=RS.getString("action");
            String condition="";

            if(!action.equals("-1")&&!action.equals("1")&&action.contains(":"))
            {
              condition=action.split(":")[1];
              action=action.split(":")[0];
            }
            ArrayList<Double> percents=new ArrayList<>();
            percents.add(RS.getDouble("percentGrade1"));
            percents.add(RS.getDouble("percentGrade2"));
            percents.add(RS.getDouble("percentGrade3"));
            percents.add(RS.getDouble("percentGrade4"));
            percents.add(RS.getDouble("percentGrade5"));
            World.Drop drop=new World.Drop(RS.getInt("objectId"),percents,RS.getInt("ceil"),Integer.parseInt(action),RS.getInt("level"),condition,RS.getInt("minDrop"),RS.getInt("maxDrop"),RS.getDouble("percentGrade5"));
            Main.world.getMonstres().stream().filter(monster -> monster!=null).forEach(monster -> monster.addDrop(drop));
          }
        }
      }
    }
    catch(SQLException e)
    {
      super.sendError("DropData load",e);
    } finally
    {
      close(result);
    }
  }

  public void reload()
  {
    Main.world.getMonstres().stream().filter(m -> m!=null).filter(m -> m.getDrops()!=null).forEach(m -> m.getDrops().clear());
    load();
  }
}
