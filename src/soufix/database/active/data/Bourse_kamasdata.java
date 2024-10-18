package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.game.World;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Bourse_kamasdata extends AbstractDAO<Object>
{

  public Bourse_kamasdata(HikariDataSource dataSource)
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


  public int load_bourse() {
		int nbr;
		Result result=null;
		nbr = 0;
		try {
			try {
				
					 result =getData("SELECT * from bourse_kamas");
					 ResultSet RS=result.resultSet;
				while (RS.next()) {
					World.Bourse_kamas.put(RS.getInt("id"), new soufix.other.Bourse_kamas(RS.getInt("id"), RS.getInt("id_perso"), RS.getLong("kamas") ,RS.getInt("points"), RS.getInt("taux"), RS.getInt("statu")));
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
	public boolean delete_bourse(int id) {
		PreparedStatement p = null;
		try {
			p = this.getPreparedStatement("DELETE FROM `bourse_kamas` where id = ?");
			p.setInt(1, id);;
			this.execute(p);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close(p);
		}
		return false;
	}
	public boolean add_bourse(int id ,int id_perso , long kamas , int points ,int taux ,int statu) {
		PreparedStatement p = null;
		try {
			p = this.getPreparedStatement("INSERT INTO `bourse_kamas`(`id`,`id_perso`,`kamas`,`points`,`taux`,`statu`) VALUES (?,?,?,?,?,?)");
			p.setInt(1, id);
			p.setInt(2, id_perso);
			p.setLong(3, kamas);
			p.setLong(4, points);
			p.setLong(5, taux);
			p.setLong(6, statu);
			this.execute(p);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close(p);
		}
		return false;
	}
	public boolean add_bourse_logs(int id_perso_buy , int id_perso_sell,String note) {
		PreparedStatement p = null;
		try {
			p = this.getPreparedStatement("INSERT INTO `bourse_kamas_logs`(`id_perso_buy`,`id_perso_sell`,`note`) VALUES (?,?,?)");
			p.setInt(1, id_perso_buy);
			p.setInt(2, id_perso_sell);
			p.setString(3, note);
			this.execute(p);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close(p);
		}
		return false;
	}
	 public int getNextIdbourse()
	  {
	    Result result=null;
	    int guid=0;
	    try
	    {
	      result=getData("SELECT id FROM bourse_kamas ORDER BY id DESC LIMIT 1");
	      ResultSet RS=result.resultSet;

	      if(!RS.first())
	        guid=1;
	      else
	        guid=RS.getInt("id")+1;
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
