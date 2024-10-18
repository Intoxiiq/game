package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.quest.QuestStep;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestStepData extends AbstractDAO<QuestStep>
{
  public QuestStepData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(QuestStep obj)
  {
    return false;
  }

  public void load()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM quest_etapes");
      ResultSet RST=result.resultSet;
      QuestStep.getQuestStepList().clear();
      while(RST.next())
      {
        QuestStep QE=new QuestStep(RST.getInt("id"),RST.getInt("type"),RST.getInt("objectif"),RST.getString("item"),RST.getInt("npc"),RST.getString("monster"),RST.getString("conditions"),RST.getInt("validationType"));
        QuestStep.addQuestStep(QE);
      }
    }
    catch(SQLException e)
    {
      super.sendError("Quest_etapeData load",e);
    } finally
    {
      close(result);
    }
  }
}
