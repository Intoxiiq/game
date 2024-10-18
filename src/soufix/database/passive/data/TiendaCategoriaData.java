package soufix.database.passive.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.passive.AbstractDAO;
import soufix.game.World;
import soufix.other.tienda.TiendaCategoria;

public class TiendaCategoriaData extends AbstractDAO<TiendaCategoria>
{
	public TiendaCategoriaData(HikariDataSource dataSource)
	{
		super(dataSource);
	}

	@Override
	public void load(Object obj) {}

	@Override
	public boolean update(TiendaCategoria invasion)
	{
		return false;
	}

	 public ArrayList<TiendaCategoria> loadCategorias() {
	   	 Result result = null;
	   	 final ArrayList<TiendaCategoria> titulos = new ArrayList<TiendaCategoria>();
	        try {
	            result = getData("SELECT `id` FROM `tiendacategoria` ORDER BY `id` ASC LIMIT 200;");
	            ResultSet RS = result.resultSet;
	            while (RS.next()) {
	           	 try {
	           		titulos.add(World.getTiendaCategoria2(Integer.parseInt(RS.getString("id"))));
	           	 }catch (NullPointerException localNullPointerException) {}
	            }
	        } catch (SQLException e) {
	            super.sendError("tiendaCategoriaData loadTiendaC", e);
	        } finally {
	            close(result);
	        }
	        return titulos;
		}
	
	public int loadCategoria() {
		int nbr;
		Result result=null;
		nbr = 0;
		try {
			try {
				
					 result =getData("SELECT * from tiendacategoria");
					 ResultSet RS=result.resultSet;
				while (RS.next()) {
					World.TiendaCategoria.put(RS.getInt("id"), new TiendaCategoria(RS.getInt("id"), RS.getInt("icono"), RS.getString("nombre")));
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