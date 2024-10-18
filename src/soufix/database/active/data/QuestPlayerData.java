package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.client.Player;
import soufix.database.active.AbstractDAO;
import soufix.main.Main;
import soufix.quest.QuestPlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestPlayerData extends AbstractDAO<QuestPlayer>
{
	private int nextId = -1;
  public QuestPlayerData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(QuestPlayer qp)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `world.entity.players.quests` SET `finish` = ?, `stepsValidation` = ? WHERE `id` = ?;");
      p.setInt(1,qp.isFinish() ? 1 : 0);
      p.setString(2,qp.getQuestStepString());
      p.setInt(3,qp.getId());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("QuestPlayerData update",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public void update(QuestPlayer questPlayer, Player player)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `world.entity.players.quests` SET `quest`= ?, `finish`= ?, `player` = ?, `stepsValidation` = ? WHERE `id` = ?;");
      p.setInt(1,questPlayer.getQuest().getId());
      p.setInt(2,questPlayer.isFinish() ? 1 : 0);
      p.setInt(3,player.getId());
      p.setString(4,questPlayer.getQuestStepString());
      p.setInt(5,questPlayer.getId());
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("QuestPlayerData update",e);
    } finally
    {
      close(p);
    }
  }

  public void loadPersoALL()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM `world.entity.players.quests` ;");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
    	  if(Main.world.getPlayer(RS.getInt("player")) == null)
    		  continue;
    	  Main.world.getPlayer(RS.getInt("player")).addQuestPerso(new QuestPlayer(RS.getInt("id"),RS.getInt("quest"),RS.getInt("finish")==1,RS.getInt("player"),RS.getString("stepsValidation")));
      }
    }
    catch(SQLException e)
    {
      super.sendError("QuestPlayerData loadPerso",e);
    } finally
    {
      close(result);
    }
  }

  public boolean delete(int id)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("DELETE FROM `world.entity.players.quests` WHERE `id` = ?;");
      p.setInt(1,id);
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("QuestPlayerData delete",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public boolean add(QuestPlayer questPlayer)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("INSERT INTO `world.entity.players.quests` VALUES (?, ?, ?, ?, ?);");
      p.setInt(1,questPlayer.getId());
      p.setInt(2,questPlayer.getQuest().getId());
      p.setInt(3,questPlayer.isFinish() ? 1 : 0);
      p.setInt(4,questPlayer.getPlayer().getId());
      p.setString(5,questPlayer.getQuestStepString());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("QuestPlayerData add",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public int getNextId()
  {
	  if (this.nextId != -1) {
		  this.nextId++;
		  return this.nextId;
	  }
    	Result result=null;
    	int nextObjectId = 0;
    	try {
    		result = getData("SELECT MAX(id) AS max FROM `world.entity.players.quests`;");
            ResultSet RS = result.resultSet;
            boolean found = RS.first();
            if (found) nextObjectId = RS.getInt("max");
            else nextObjectId = 1;
        } catch (SQLException e) {
            logger.error("WorldEntityData quetes", e);
        } finally {
            close(result);
        }
    	this.nextId = nextObjectId+1;
		return nextObjectId+1;

  }
}
