package soufix.database.active.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;

import soufix.client.other.Ornements;
import soufix.database.active.AbstractDAO;
import soufix.main.Main;

public class OrnementsData extends AbstractDAO<Object> {
    public OrnementsData(HikariDataSource dataSource) {
		super(dataSource);
		// TODO Auto-generated constructor stub
	}

    @Override
    public void load(Object obj) {
    }

    public void load() {
        Result result = null;
        try {
            result = getData("SELECT * FROM ornements;");
            ResultSet RS = result.resultSet;
            while (RS.next()) {
                Main.world.addOrnements(
                        new Ornements(
                                RS.getInt("id"),
                                RS.getString("name"),
                                RS.getInt("price"),
                                RS.getInt("success"),
                                RS.getInt("canbuy")
                        ));
            }
        } catch (SQLException e) {
            super.sendError("Item_templateData load", e);
            Main.stop("unknown");
        } finally {
            close(result);
        }
    }

    public boolean update(Object obj) {
        return false;
    }
}