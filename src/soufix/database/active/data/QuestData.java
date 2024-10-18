package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.quest.Quest;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestData extends AbstractDAO<Quest>
{
  public QuestData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(Quest obj)
  {
    return false;
  }

  public void load()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM quest_data");
      ResultSet RS=result.resultSet;
      Quest.getQuestList().clear();
      while(RS.next())
      {
        Quest quest=new Quest(RS.getInt("id"),RS.getString("etapes"),RS.getString("objectif"),RS.getInt("npc"),RS.getString("action"),RS.getString("args"),(RS.getInt("deleteFinish")==1),RS.getString("condition"));
        if(quest.getNpcTemplate()!=null)
        {
          quest.getNpcTemplate().setQuest(quest);
          quest.getNpcTemplate().setExtraClip(4);
        }
        Quest.addQuest(quest);
      }
    }
    catch(SQLException e)
    {
      super.sendError("Quest_dataData load",e);
    } finally
    {
      close(result);
    }
  }
}
