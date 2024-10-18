package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.quest.QuestObjectif;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestObjectiveData extends AbstractDAO<QuestObjectif> {
    public QuestObjectiveData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(QuestObjectif obj) {
        return false;
    }

    public void load() {
        Result result = null;
        try {
            result = getData("SELECT * FROM quest_objectifs");
            ResultSet loc1 = result.resultSet;
            QuestObjectif.getQuestObjectifList().clear();
            while (loc1.next()) {
              QuestObjectif qObjectif = new QuestObjectif(loc1.getInt("id"), loc1.getInt("xp"), loc1.getInt("kamas"), loc1.getString("item"), loc1.getString("action"));
              QuestObjectif.addQuestObjectif(qObjectif);
            }
            close(result);
        } catch (SQLException e) {
            super.sendError("Quest_objectifData load", e);
        } finally {
            close(result);
        }
    }
}
