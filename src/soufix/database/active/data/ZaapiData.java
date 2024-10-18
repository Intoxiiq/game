package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.main.Constant;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ZaapiData extends AbstractDAO<Object>
{
  public ZaapiData(HikariDataSource dataSource)
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

  //v2.7 - Replaced String += with StringBuilder
  public int load()
  {
    Result result=null;
    int i=0;
    StringBuilder Bonta=new StringBuilder();
    StringBuilder Brak=new StringBuilder();
    StringBuilder Neutre=new StringBuilder();
    try
    {
      result=getData("SELECT mapid, align from zaapi");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        if(RS.getInt("align")==Constant.ALIGNEMENT_BONTARIEN)
        {
          Bonta.append(RS.getString("mapid"));
          if(!RS.isLast())
            Bonta.append(",");
        } else if(RS.getInt("align")==Constant.ALIGNEMENT_BRAKMARIEN)
        {
          Brak.append(RS.getString("mapid"));
          if(!RS.isLast())
            Brak.append(",");
        } else
        {
          Neutre.append(RS.getString("mapid"));
          if(!RS.isLast())
            Neutre.append(",");
        }
        i++;
      }
      Constant.ZAAPI.put(Constant.ALIGNEMENT_BONTARIEN,Bonta.toString());
      Constant.ZAAPI.put(Constant.ALIGNEMENT_BRAKMARIEN,Brak.toString());
      Constant.ZAAPI.put(Constant.ALIGNEMENT_NEUTRE,Neutre.toString());
    }
    catch(SQLException e)
    {
      super.sendError("ZaapiData load",e);
    } finally
    {
      close(result);
    }
    return i;
  }
}
