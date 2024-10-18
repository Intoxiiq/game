package soufix.other;


public class Bourse_kamas {

    public int id;
    public int id_perso;
    public long kamas;
    public int points;
    public int taux;
    public int statu ;
    
    public Bourse_kamas(int id, int idperso, long kamas, int points , int taux , int statu) {
        this.id = id;
        this.id_perso  = idperso;
        this.kamas = kamas;
        this.points = points;
        this.taux = taux;
        this.statu = statu;
        
           }
}
