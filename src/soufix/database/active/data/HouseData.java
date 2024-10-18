package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.area.map.entity.House;
import soufix.client.Player;
import soufix.database.Database;
import soufix.database.active.AbstractDAO;
import soufix.main.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HouseData extends AbstractDAO<House>
{
  public HouseData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(House h)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `houses` SET `owner_id` = ?,`sale` = ?,`guild_id` = ?,`access` = ?,`key` = ?,`guild_rights` = ? WHERE id = ?");
      p.setInt(1,h.getOwnerId());
      p.setInt(2,h.getSale());
      p.setInt(3,h.getGuildId());
      p.setInt(4,h.getAccess());
      p.setString(5,h.getKey());
      p.setInt(6,h.getGuildRights());
      p.setInt(7,h.getId());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("HouseData update",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public boolean update(int id, long price)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `houses` SET `sale` = ? WHERE id = ?");
      p.setLong(1,price);
      p.setInt(2,id);
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("HouseData update",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public int load()
  {
    Result result=null;
    int nbr=0;
    try
    {
      result=getData("SELECT * from houses");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        int id=RS.getInt("id");
        int owner=RS.getInt("owner_id");
        int sale=RS.getInt("sale");
        int guild=RS.getInt("guild_id");
        int access=RS.getInt("access");
        String key=RS.getString("key");
        int guildRights=RS.getInt("guild_rights");
        House house=Main.world.getHouse(id);
        if(house==null)
          continue;
        if(owner!=0&&Main.world.getAccount(owner)==null)
        {
        	house.setOwnerId(0);
            house.setSale(house.getSale_base());
            house.setGuildId(0);
            house.setAccess(0);
            house.setKey("-");
            house.setGuildRightsWithParse(0);
            continue;
        }
        try {
        /*if(owner!=0&&Main.world.getAccount(owner)!=null)
        {
        	String date = Main.world.getAccount(owner).getLastConnectionDate();
        	date = date.substring(5, 7);
        	if(date.substring(0,1).contains("0"))
        	date = date.replaceAll("0", "");
        	int mois = Integer.parseInt(date);
        	int mois_plus = mois+3;
        	if(mois_plus == 13)
        	mois_plus = 1;
        	if(mois_plus == 14)
            mois_plus = 2;	
        	if(mois_plus > mois) {
            	System.out.println("maison update "+house.getId());
        	house.setOwnerId(0);
            house.setSale(house.getSale_base());
            house.setGuildId(0);
            house.setAccess(0);
            house.setKey("-");
            house.setGuildRightsWithParse(0);
            this.update(house);
            continue;
        	}
        }*/
        }
        catch(Exception e)
        {
        	 e.printStackTrace();
        }
        house.setOwnerId(owner);
        house.setSale(sale);
        house.setGuildId(guild);
        house.setAccess(access);
        house.setKey(key);
        house.setGuildRightsWithParse(guildRights);
        nbr++;
      }
    }
    catch(SQLException e)
    {
      super.sendError("HouseData load",e);
      nbr=0;
    } finally
    {
      close(result);
    }
    return nbr;
  }

  public void buy(Player P, House h)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `houses` SET `sale`='0', `owner_id`=?, `guild_id`='0', `access`='0', `key`='-', `guild_rights`='0' WHERE `id`=?");
      p.setInt(1,P.getAccID());
      p.setInt(2,h.getId());
      execute(p);

      h.setSale(0);
      h.setOwnerId(P.getAccID());
      h.setGuildId(0);
      h.setAccess(0);
      h.setKey("-");
      h.setGuildRights(0);

      Database.getDynamics().getTrunkData().update(P,h);
    }
    catch(SQLException e)
    {
      super.sendError("HouseData buy",e);
    } finally
    {
      close(p);
    }
  }

  public void sell(House h, int price)
  {
    h.setSale(price);
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `houses` SET `sale`=? WHERE `id`=?");
      p.setInt(1,price);
      p.setInt(2,h.getId());
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("HouseData sell",e);
    } finally
    {
      close(p);
    }
  }

  public void updateCode(Player P, House h, String packet)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `houses` SET `key`=? WHERE `id`=? AND owner_id=?");
      p.setString(1,packet);
      p.setInt(2,h.getId());
      p.setInt(3,P.getAccID());
      execute(p);
      h.setKey(packet);
    }
    catch(SQLException e)
    {
      super.sendError("HouseData updateCode",e);
    } finally
    {
      close(p);
    }
  }

  public void updateGuild(House h, int GuildID, int GuildRights)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `houses` SET `guild_id`=?, `guild_rights`=? WHERE `id`=?");
      p.setInt(1,GuildID);
      p.setInt(2,GuildRights);
      p.setInt(3,h.getId());
      execute(p);
      h.setGuildId(GuildID);
      h.setGuildRights(GuildRights);
    }
    catch(SQLException e)
    {
      super.sendError("HouseData updateGuild",e);
    } finally
    {
      close(p);
    }
  }

  public void removeGuild(int GuildID)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `houses` SET `guild_rights`='0', `guild_id`='0' WHERE `guild_id`=?");
      p.setInt(1,GuildID);
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("HouseData removeGuild",e);
    } finally
    {
      close(p);
    }
  }
}
