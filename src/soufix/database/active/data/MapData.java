package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.area.map.GameMap;
import soufix.database.active.AbstractDAO;
import soufix.main.Constant;
import soufix.main.Main;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MapData extends AbstractDAO<GameMap>
{
  public MapData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(GameMap obj)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `maps` SET `places` = ?, `numgroup` = ? WHERE id = ?");
      p.setString(1,obj.getPlaces());
      p.setInt(2,obj.getMaxGroupNumb());
      p.setInt(3,obj.getId());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("MapData update",e);
    } finally
    {
      close(p);
    }
    return false;
  }
  public boolean updatemapmpster(int map_id , String mobs)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `maps` SET  `monsters` = ? WHERE id = ?");
      p.setString(1,mobs);
      p.setInt(2,map_id);
 
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("MapData update",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public boolean updateGs(GameMap map)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `maps` SET `numgroup` = ?, `minSize` = ?, `fixSize` = ?, `maxSize` = ? WHERE id = ?");
      p.setInt(1,map.getMaxGroupNumb());
      p.setInt(2,map.getMinSize());
      p.setInt(3,map.getFixSize());
      p.setInt(4,map.getMaxSize());
      p.setInt(5,map.getId());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("MapData updateGs",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public boolean updateMonster(GameMap map, String monsters)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `maps` SET `monsters` = ? WHERE id = ?");
      p.setString(1,monsters);
      p.setInt(2,map.getId());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("MapData updateMonster",e);
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
      result=getData("SELECT * from maps LIMIT "+Constant.DEBUG_MAP_LIMIT);
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        Main.world.addMap(new GameMap(RS.getShort("id"),RS.getString("date"),RS.getByte("width"),RS.getByte("heigth"),RS.getString("key"),RS.getString("places"),RS.getString("mapData"),RS.getString("monsters"),RS.getString("mappos"),RS.getByte("numgroup"),RS.getByte("fixSize"),RS.getByte("minSize"),RS.getByte("maxSize"),RS.getString("forbidden"),RS.getByte("sniffed"),RS.getInt("minRespawnTime"),RS.getInt("maxRespawnTime"),RS.getInt("song")
        		));
      }
      close(result);

      result=getData("SELECT * from mobgroups_fix");
      RS=result.resultSet;
      int timer=0;
      while(RS.next())
      {
        timer=RS.getInt("Timer");
      }
      result=getData("SELECT * from mobgroups_fix_dynamic");
      RS=result.resultSet;
      while(RS.next())
      {
        GameMap c=Main.world.getMap(RS.getShort("map"));
        if(c==null)
          continue;
        if(c.getCase(RS.getInt("cell"))==null)
          continue;
        c.addStaticGroupv2(RS.getInt("cell"),RS.getString("group"),false,RS.getLong("stars"));
        Main.world.maps_dj.add(""+RS.getShort("map"));
        Main.world.addGroupFix(RS.getInt("map")+";"+RS.getInt("cell"),RS.getString("group"),timer,RS.getLong("stars"));
      }

      result=getData("SELECT * from mobgroups_fix_random");
      RS=result.resultSet;
      while(RS.next())
      {
        GameMap c=Main.world.getMap(RS.getShort("mapid"));
        if(c==null)
          continue;
        if(c.getCase(RS.getInt("cellid"))==null)
          continue;
        if(RS.getString("bossData")!=null)
        {
          c.addRandomStaticGroup(RS.getInt("cellid"),RS.getString("groupSize")+":"+RS.getString("groupData")+":"+RS.getString("bossData"),false);
          Main.world.addRandomGroupFix(RS.getInt("mapid")+";"+RS.getInt("cellid"),RS.getString("groupSize")+":"+RS.getString("groupData")+":"+RS.getString("bossData"),RS.getInt("Timer"));
        }
        else
        {
          c.addRandomStaticGroup(RS.getInt("cellid"),RS.getString("groupSize")+":"+RS.getString("groupData"),false);
          Main.world.addRandomGroupFix(RS.getInt("mapid")+";"+RS.getInt("cellid"),RS.getString("groupSize")+":"+RS.getString("groupData"),RS.getInt("Timer"));
        }
      }

      result=getData("SELECT  * from mobgroups_fix_exception");
      RS=result.resultSet;
      while(RS.next())
      {
        GameMap c=Main.world.getMap(RS.getShort("mapid"));
        if(c==null)
          continue;
        if(c.getCase(RS.getInt("cellid"))==null)
          continue;
        c.addStaticGroup(RS.getInt("cellid"),RS.getString("groupData"),false);
        Main.world.addGroupFix(RS.getInt("mapid")+";"+RS.getInt("cellid"),RS.getString("groupData"),RS.getInt("Timer"),0L);
      }

    }
    catch(SQLException e)
    {
      super.sendError("MapData load",e);
    } finally
    {
      close(result);
    }
  }
//v2.3 - random dungeon arrayoutofbounds fix
  /*
  public void load(int id)
  {
	  System.out.println(id);
    Result result=null;
    try
    {
      result=getData("SELECT * from maps where id = "+id);
      ResultSet RS=result.resultSet;
      GameMap map = null;
      while(RS.next())
      {
    	  map = Main.world.addMapv2(new GameMap(RS.getShort("id"),RS.getString("date"),RS.getByte("width"),RS.getByte("heigth"),RS.getString("key"),RS.getString("places"),RS.getString("mapData"),RS.getString("monsters"),RS.getString("mappos"),RS.getByte("numgroup"),RS.getByte("fixSize"),RS.getByte("minSize"),RS.getByte("maxSize"),RS.getString("forbidden"),RS.getByte("sniffed"),RS.getInt("minRespawnTime"),RS.getInt("maxRespawnTime")));
      }
      close(result);
      if(map == null)
    	  return;
      result=getData("SELECT * FROM `scripted_cells` where MapID = "+id);
      RS=result.resultSet;
      while(RS.next())
      {
        int cellId=RS.getInt("CellID");
        GameCase cell=map.getCase(cellId);
        if(cell==null)
          continue;

        switch(RS.getInt("EventID"))
        {
          case 1: // Stop sur la case (triggers)
            cell.addOnCellStopAction(RS.getInt("ActionID"),RS.getString("ActionsArgs"),RS.getString("Conditions"),null);
            break;
        }
      }
    
      close(result);
      result=getData("SELECT * from mobgroups_fix where mapid ="+id);
      RS=result.resultSet;
      int timer=0;
      while(RS.next())
      {
        timer=RS.getInt("Timer");
      }
      close(result);
      result=getData("SELECT * from mobgroups_fix_dynamic where map = "+id);
      RS=result.resultSet;
      while(RS.next())
      {
    	  if(RS != null) {
        if(map.getCase(RS.getInt("cell"))==null)
          continue;
        map.addStaticGroupv2(RS.getInt("cell"),RS.getString("group"),false,RS.getLong("stars"));
        Main.world.maps_dj.add(""+RS.getShort("map"));
        Main.world.addGroupFix(RS.getInt("map")+";"+RS.getInt("cell"),RS.getString("group"),timer,RS.getLong("stars"));
      }
      }
      close(result);
      result=getData("SELECT * from mobgroups_fix_random where mapid = "+id);
      RS=result.resultSet;
      while(RS.next())
      {
        if(map.getCase(RS.getInt("cellid"))==null)
          continue;
        if(RS.getString("bossData")!=null)
        {
          map.addRandomStaticGroup(RS.getInt("cellid"),RS.getString("groupSize")+":"+RS.getString("groupData")+":"+RS.getString("bossData"),false);
          Main.world.addRandomGroupFix(RS.getInt("mapid")+";"+RS.getInt("cellid"),RS.getString("groupSize")+":"+RS.getString("groupData")+":"+RS.getString("bossData"),RS.getInt("Timer"));
        }
        else
        {
          map.addRandomStaticGroup(RS.getInt("cellid"),RS.getString("groupSize")+":"+RS.getString("groupData"),false);
          Main.world.addRandomGroupFix(RS.getInt("mapid")+";"+RS.getInt("cellid"),RS.getString("groupSize")+":"+RS.getString("groupData"),RS.getInt("Timer"));
        }
      }
      close(result);
      result=getData("SELECT  * from mobgroups_fix_exception where mapid = "+id);
      RS=result.resultSet;
      while(RS.next())
      {
        if(map.getCase(RS.getInt("cellid"))==null)
          continue;
        map.addStaticGroup(RS.getInt("cellid"),RS.getString("groupData"),false);
        Main.world.addGroupFix(RS.getInt("mapid")+";"+RS.getInt("cellid"),RS.getString("groupData"),RS.getInt("Timer"),0L);
      }
      close(result);
      result=getData("SELECT * FROM endfight_action where map = "+id);
      RS=result.resultSet;
      while(RS.next())
      {
        map.addEndFightAction(RS.getInt("fighttype"),new Action(RS.getInt("action"),RS.getString("args"),RS.getString("cond"),null));
      }
      close(result);
      result=getData("SELECT * from npcs where mapid = "+id);
      RS=result.resultSet;
      while(RS.next())
      {
        map.addNpc(RS.getInt("npcid"),RS.getShort("cellid"),RS.getInt("orientation"));
      }
      close(result);
      result=getData("SELECT * FROM `mobgroups_dynamic` where map = "+id);
      RS=result.resultSet;
      while(RS.next())
      {
        final MobGroup group=new MobGroup(RS.getInt("id"),RS.getShort("map"),RS.getInt("cell"),RS.getString("group"),RS.getString("objects"),RS.getLong("stars"));
        if(map!=null)
          map.respawnGroup(group);
      }
      close(result);
      result=getData("SELECT * FROM `mobgroups_fix_dynamic` where map = "+id);
      RS=result.resultSet;
      while(RS.next())
      {
        RespawnGroup.fixMobGroupObjects.put(RS.getInt("map")+","+RS.getInt("cell"),new Pair<ArrayList<GameObject>, Integer>(new ArrayList<GameObject>(),0));
      }

    }
    catch(SQLException e)
    {
      super.sendError("MapData load",e);
    } finally
    {
      close(result);
    }
  }
*/
  public void reload()
  {
    Result result=null;
    try
    {
      result=getData("SELECT  * from maps LIMIT "+Constant.DEBUG_MAP_LIMIT);
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        GameMap map=Main.world.getMap(RS.getShort("id"));
        if(map==null)
        {
          Main.world.addMap(new GameMap(RS.getShort("id"),RS.getString("date"),RS.getByte("width"),RS.getByte("heigth"),RS.getString("key"),RS.getString("places"),RS.getString("mapData"),RS.getString("monsters"),RS.getString("mappos"),RS.getByte("numgroup"),RS.getByte("fixSize"),RS.getByte("minSize"),RS.getByte("maxSize"),RS.getString("forbidden"),RS.getByte("sniffed"),RS.getInt("minRespawnTime"),RS.getInt("maxRespawnTime"),RS.getInt("song")));
          continue;
        }
        map.setInfos(RS.getString("date"),RS.getString("monsters"),RS.getString("mappos"),RS.getByte("numgroup"),RS.getByte("fixSize"),RS.getByte("minSize"),RS.getByte("maxSize"),RS.getString("forbidden"));
      }
    }
    catch(SQLException e)
    {
      super.sendError("MapData reload",e);
    } finally
    {
      close(result);
    }
  }
}
