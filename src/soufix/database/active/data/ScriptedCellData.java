package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.database.active.AbstractDAO;
import soufix.main.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ScriptedCellData extends AbstractDAO<Object>
{
  public ScriptedCellData(HikariDataSource dataSource)
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

  public int load()
  {
    Result result=null;
    int nbr=0;
    try
    {
      result=getData("SELECT * FROM `scripted_cells`");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        short mapId=RS.getShort("MapID");
        int cellId=RS.getInt("CellID");
        GameMap map=Main.world.getMap(mapId);
        if(map==null)
          continue;

        GameCase cell=map.getCase(cellId);
        if(cell==null)
          continue;

        switch(RS.getInt("EventID"))
        {
          case 1: // Stop sur la case (triggers)
            cell.addOnCellStopAction(RS.getInt("ActionID"),RS.getString("ActionsArgs"),RS.getString("Conditions"),null);
            break;

          default:
            break;
        }
        nbr++;
      }
    }
    catch(SQLException e)
    {
      super.sendError("Scripted_cellData load",e);
      Main.stop("unknown");
    } finally
    {
      close(result);
    }
    return nbr;
  }
 
  public boolean update(int mapID1, int cellID1, int action, int event, String args, String cond)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("REPLACE INTO `scripted_cells` VALUES (?,?,?,?,?,?)");
      p.setInt(1,mapID1);
      p.setInt(2,cellID1);
      p.setInt(3,action);
      p.setInt(4,event);
      p.setString(5,args);
      p.setString(6,cond);
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("Scripted_cellData update",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public boolean delete(int mapID, int cellID)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("DELETE FROM `scripted_cells` WHERE `MapID` = ? AND `CellID` = ?");
      p.setInt(1,mapID);
      p.setInt(2,cellID);
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("Scripted_cellData delete",e);
    } finally
    {
      close(p);
    }
    return false;
  }
}
