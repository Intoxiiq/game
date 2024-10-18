package soufix.database.passive.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.passive.AbstractDAO;
import soufix.game.scheduler.entity.WorldPub;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PubData extends AbstractDAO<Object> {

    public PubData(HikariDataSource dataSource)
	{
		super(dataSource);
	}

	@Override
	public void load(Object obj) {
        Result result = null;
        try {
            result = getData("SELECT * FROM `pubs`;");
            ResultSet RS = result.resultSet;
            while (RS.next())
                WorldPub.pubs.add(RS.getString("data"));
        } catch (SQLException e) {
            super.sendError("PubData load", e);
        } finally {
            close(result);
        }
    }

	@Override
	public boolean update(Object t)	{
		return false;
	}
}
