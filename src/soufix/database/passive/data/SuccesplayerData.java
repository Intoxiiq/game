package soufix.database.passive.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.passive.AbstractDAO;
import soufix.game.World;
import soufix.other.Succes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SuccesplayerData extends AbstractDAO<Object>
{

  public SuccesplayerData(HikariDataSource dataSource)
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


  public int load_succes() {
		int nbr;
		Result result=null;
		nbr = 0;
		try {
			try {
					 result =getData("SELECT * from player_infos");
					 ResultSet RS=result.resultSet;
				while (RS.next()) {
					World.Succes.put(RS.getInt("id"), new Succes(RS.getInt("id"), RS.getInt("combat"), RS.getInt("pvp"), RS.getInt("quete") , RS.getInt("donjon") , RS.getInt("archi"), RS.getInt("boutique"), RS.getInt("points"), RS.getString("passed"), RS.getString("ravens"),RS.getInt("koli_lose"),RS.getInt("koli_wine"),RS.getInt("recolte"),RS.getInt("craft"),RS.getInt("fm"),RS.getInt("brisage"),RS.getInt("chall"),RS.getInt("msg"),RS.getInt("pvp_lose")));
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

	public void load_succes(final int id) {
		Result result=null;
		try {
			try {
				result =getData("SELECT * from player_infos where id = '"+id+"'");
				ResultSet RS=result.resultSet;
				if(!RS.first())return;
				World.Succes.put(RS.getInt("id"), new Succes(RS.getInt("id"), RS.getInt("combat"), RS.getInt("pvp"), RS.getInt("quete") , RS.getInt("donjon") , RS.getInt("archi"), RS.getInt("boutique"), RS.getInt("points"), RS.getString("passed"), RS.getString("ravens"),RS.getInt("koli_lose"),RS.getInt("koli_wine"),RS.getInt("recolte"),RS.getInt("craft"),RS.getInt("fm"),RS.getInt("brisage"),RS.getInt("chall"),RS.getInt("msg"),RS.getInt("pvp_lose")));
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} finally {
			super.close(result);
		}
		return;
	}
	public boolean add_succes(int id , int combat ,int pvp , int quete, int donjon , int archi ,int boutique, int points, String passed , String ravens, int koli_lose , int koli_wine , int recolte, int craft, int fm, int brisage, int chall, int msg, int pvp_lose) {
		PreparedStatement p = null;
		try {
			p = this.getPreparedStatement("INSERT INTO `player_infos`(`id`,`combat`,`pvp`,`quete`,`donjon`,`archi`,`boutique`,`points`,`passed`,`ravens`,`koli_lose`,`koli_wine`,`recolte`,`craft`,`fm`,`brisage`,`chall`,`msg`,`pvp_lose`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			p.setInt(1, id);
			p.setInt(2, combat);
			p.setInt(3, pvp);
			p.setInt(4, quete);
			p.setInt(5, donjon);
			p.setInt(6, archi);
			p.setInt(7, boutique);
			p.setInt(8, points);
			p.setString(9, passed);
			p.setString(10, ravens);
			p.setInt(11, koli_lose);
			p.setInt(12, koli_wine);
			p.setInt(13, recolte);
			p.setInt(14, craft);
			p.setInt(15, fm);
			p.setInt(16, brisage);
			p.setInt(17, chall);
			p.setInt(18, msg);
			p.setInt(19, pvp_lose);
			this.execute(p);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close(p);
		}
		return false;
	}
	public boolean update_succes(int id , int combat , int quete, int donjon , int archi ,
			String passed , int pvp,int boutique ,int points, String ravens,int koli_lose , int koli_wine ,int recolte,int craft, int fm, int brisage , int chall, int msg, int pvp_lose) {
		PreparedStatement p = null;
		try {
			p = this.getPreparedStatement("UPDATE `player_infos` SET `combat` = ?,`quete` = ?,`donjon` = ?,`archi` = ?,`passed` = ? ,`pvp`= ?,`boutique` = ?,`points` = ? ,`ravens` = ?,`koli_lose` = ?,`koli_wine` = ?,`recolte` = ?,`craft` = ?,`fm` = ?,`brisage` = ? ,`chall` = ? ,`msg` = ? ,`pvp_lose` = ? WHERE `id` = ?;");
			if(p == null)
				return false;
			p.setInt(1, combat);
			p.setInt(2, quete);
			p.setInt(3, donjon);
			p.setInt(4, archi);
			p.setString(5, passed);
			p.setInt(6, pvp);
			p.setInt(7, boutique);
			p.setInt(8, points);
			p.setString(9, ravens);
			p.setInt(10, koli_lose);
			p.setInt(11, koli_wine);
			p.setInt(12, recolte);
			p.setInt(13, craft);
			p.setInt(14, fm);
			p.setInt(15, brisage);
			p.setInt(16, chall);
			p.setInt(17, msg);
			p.setInt(18, pvp_lose);
			p.setInt(19, id);
			this.execute(p);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close(p);
		}
		return false;
	}

}
