package soufix.database.passive.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.passive.AbstractDAO;
import soufix.game.World;
import soufix.other.tienda.TiendaObjetos;


public class TiendaObjetosData extends AbstractDAO<TiendaObjetos>
{
	public TiendaObjetosData(HikariDataSource dataSource)
	{
		super(dataSource);
	}

	@Override
	public void load(Object obj) {}

	@Override
	public boolean update(TiendaObjetos invasion)
	{
		return false;
	}

	 public ArrayList<TiendaObjetos> loadObjetosTienda() {
	   	 Result result = null;
	   	 final ArrayList<TiendaObjetos> titulos = new ArrayList<TiendaObjetos>();
	        try {
	            result = getData("SELECT `id` FROM `tiendaobjetos` ORDER BY `id` ASC LIMIT 200;");
	            ResultSet RS = result.resultSet;
	            while (RS.next()) {
	           	 try {
	           		titulos.add(World.getTiendaObjetos2(Integer.parseInt(RS.getString("id"))));
	           	 }catch (NullPointerException localNullPointerException) {}
	            }
	        } catch (SQLException e) {
	            super.sendError("titreData loadTItulos", e);
	        } finally {
	            close(result);
	        }
	        return titulos;
		}
	
	public int loadObjetos() {
		int nbr;
		Result result=null;
		nbr = 0;
		try {
			try {
				
					 result =getData("SELECT * from tiendaobjetos");
					 ResultSet RS=result.resultSet;
				while (RS.next()) {
					World.tiendaObjetos.put(RS.getInt("id"), new TiendaObjetos(RS.getInt("id"), RS.getInt("idObjeto"), RS.getInt("tipo"), RS.getInt("ogrinas"), RS.getString("contenidoCaja")));
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
}