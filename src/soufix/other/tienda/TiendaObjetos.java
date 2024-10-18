package soufix.other.tienda;

public class TiendaObjetos {

	private int id;
	private int idObjeto;
	private int tipo;
	private int precio;
	private String contenido;

	public TiendaObjetos(int id, int idObjeto, int tipo, int precio, String contenido) {
		this.id = id;
		this.idObjeto = idObjeto;
		this.tipo = tipo;
		this.precio = precio;
		this.contenido = contenido;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdObjeto() {
		return idObjeto;
	}

	public void setIdObjeto(int idObjeto) {
		this.idObjeto = idObjeto;
	}

	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public int getPrice() {
		return precio;
	}

	public void setPrice(int price) {
		this.precio = price;
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

}
