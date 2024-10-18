package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.client.Account;
import soufix.database.active.AbstractDAO;
import soufix.main.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BankData extends AbstractDAO<Object>
{
  public BankData(HikariDataSource dataSource)
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

  public boolean add(int guid)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("INSERT INTO banks(`id`, `kamas`, `items`, `hdv_msg`) VALUES (?, 0, '', '')");
      p.setInt(1,guid);
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("BankData add",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public void update(Account acc)
  {
    PreparedStatement p=null;
    try
    {

      p=getPreparedStatement("UPDATE `banks` SET `kamas` = ?, `items` = ? , `hdv_msg` = ?, `friends` = ? , `enemy` = ?  WHERE `id` = ?");
      p.setLong(1,acc.getBankKamas());
      p.setString(2,acc.parseBankObjectsToDB());
      p.setString(3,acc.hdv_offline);
      p.setString(4,acc.parseFriendListToDB());
      p.setString(5,acc.parseEnemyListToDB());
      p.setInt(6,acc.getId());
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("BankData update ID "+acc.getId(),e);
    } finally
    {
      close(p);
    }
  }

  public void LoadALL()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM `banks`");	  
      if(result == null)
    	  return;
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
    	if(Main.world.getAccount(RS.getInt("id")) == null) 
    	continue;
    	Main.world.kamas_total += RS.getInt("kamas");
        Main.world.getAccount(RS.getInt("id")).Load_items_bank(RS.getInt("kamas")+"@"+RS.getString("items"));
        Main.world.getAccount(RS.getInt("id")).load_amis(RS.getString("friends"),RS.getString("enemy"));
        Main.world.getAccount(RS.getInt("id")).hdv_offline = RS.getString("hdv_msg");
      }
    }
    catch(SQLException e)
    {
      super.sendError("BankData all getWaitingAccount ",e);
    } finally
    {
      super.close(result);
    }
  }

  public String get(int guid)
  {
    String get=null;
    Result result=null;
    try
    {
      result=getData("SELECT * FROM `banks` WHERE id = '"+guid+"'");	  
      if(result == null)
    	  return null;
      ResultSet RS=result.resultSet;
      if(RS.next())
      {
    	  Main.world.kamas_total += RS.getInt("kamas");
        get=RS.getInt("kamas")+"@"+RS.getString("items");
        if(Main.world.getAccount(guid) != null)
        	Main.world.getAccount(guid).hdv_offline = RS.getString("hdv_msg");
      }
    }
    catch(SQLException e)
    {
      super.sendError("BankData getWaitingAccount id "+guid,e);
    } finally
    {
      super.close(result);
    }
    return get;
  }
}
