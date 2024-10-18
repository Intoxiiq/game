package soufix.other;


public class Succes_data {

    private int ID;
	private String Name;
	private int Type;
	private int Args;
	private int Recompense;
	private String Recompense_args;
	private int Points;
	private int Art;
	private int Categorie;
	
    public Succes_data(int id , String name , int type , int args , int recompense , String recompense_args , int points , int art , int categorie) {

    this.ID = id;
    this.Name = name;
    this.Type = type;
    this.Args = args;
    this.Recompense = recompense;
    this.Recompense_args = recompense_args;
    this.Points = points;
    this.Art = art;
    this.Categorie = categorie;
    	
    }
	public int getID() {
		return ID;
	}

	public String getName() {
		return Name;
	}

	public int getType() {
		return Type;
	}

	public int getArgs() {
		return Args;
	}

	public int getRecompense() {
		return Recompense;
	}

	public String getRecompense_args() {
		return Recompense_args;
	}

	public int getPoints() {
		return Points;
	}

	public int getArt() {
		return Art;
	}

	public int getCategorie() {
		return Categorie;
	}


}


