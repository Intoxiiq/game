package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.game.World;
import soufix.main.Main;
import soufix.other.Succes_data;
import soufix.other.Titre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TitreData extends AbstractDAO<Object>
{

  public TitreData(HikariDataSource dataSource)
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


  public int load_titre() {
		int nbr;
		Result result=null;
		nbr = 0;
		try {
			try {
				
					 result =getData("SELECT * from titre");
					 ResultSet RS=result.resultSet;
				while (RS.next()) {
					World.Titre.put(RS.getInt("guid"), new Titre(RS.getInt("guid"), RS.getInt("color"), RS.getString("name")));
					++nbr;
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
				nbr = 0;
			}
		} finally {
			 super.close(result);
		}
		return nbr;
	}
  public int load_succes_data() {
		int nbr;
		Result result=null;
		nbr = 0;
		String Liste = null;
		boolean first = false;
		try {
			try {
					 result =getData("SELECT * from succes ORDER BY guid ASC");
					 ResultSet RS=result.resultSet;
				while (RS.next()) {
					World.Succes_data.put(RS.getInt("guid"), new Succes_data(RS.getInt("guid"),RS.getString("name"), RS.getInt("type"),RS.getInt("args"),RS.getInt("recompense"),RS.getString("recompenseArgs"),RS.getInt("points"),RS.getInt("art"),RS.getInt("categoria")));

			             if (first) {
			                 first = false;
			                 Liste += (RS.getInt("guid") + "~" + RS.getString("name") + "~" + RS.getInt("points")
			                         + "~" + RS.getInt("categoria") + "~" + RS.getInt("art"));
			             } else {
			                 Liste += (";" + RS.getInt("guid") + "~" + RS.getString("name") + "~"
			                         + RS.getInt("points") + "~" + RS.getInt("categoria") + "~"
			                         + RS.getInt("art"));
			             }
					
					
					++nbr;
				}
				Main.world.Succes_packet = Liste;
			} catch (SQLException e) {
				e.printStackTrace();
				nbr = 0;
			}
		} finally {
			 super.close(result);
		}
		return nbr;
	}
	public void load_titre(final int id) {
		Result result=null;
		try {
			try {
				result =getData("SELECT * from titre where guid = '"+id+"'");
				ResultSet RS=result.resultSet;
				if(!RS.first())return;
				World.Titre.put(RS.getInt("guid"), new Titre(RS.getInt("guid"), RS.getInt("color"), RS.getString("name")));
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} finally {
			super.close(result);
		}
		return;
	}
	public boolean add_titre(int id ,String titre , int color) {
		PreparedStatement p = null;
		try {
			p = this.getPreparedStatement("INSERT INTO `titre`(`guid`,`name`,`color`) VALUES (?,?,?)");
			p.setInt(1, id);
			p.setString(2, titre);
			p.setInt(3, color);
			this.execute(p);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close(p);
		}
		return false;
	}
	 public int getNextIdtitre()
	  {
	    Result result=null;
	    int guid=0;
	    try
	    {
	      result=getData("SELECT guid FROM titre ORDER BY guid DESC LIMIT 1");
	      ResultSet RS=result.resultSet;

	      if(!RS.first())
	        guid=1;
	      else
	        guid=RS.getInt("guid")+1;
	    }
	    catch(SQLException e)
	    {
	      super.sendError("titre getNextId",e);
	    } finally
	    {
	      close(result);
	    }
	    return guid;
	  }
}
