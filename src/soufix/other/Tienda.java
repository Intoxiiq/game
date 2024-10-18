package soufix.other;

public class Tienda {

	private int id;
	private int idObjeto;
	private int categoria;

	
	public Tienda(int id,int idObjeto, int categoria) {
		this.id = id;
		this.idObjeto = idObjeto;
		this.categoria = categoria;

	}
	
	public int getId() {
		return id;
	}
	
	public int getIdObjeto() {
		return idObjeto;
	}
	
	public int getCategoria() {
		return categoria;
	}
	
}
