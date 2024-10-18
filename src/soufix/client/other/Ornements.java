package soufix.client.other;

public class Ornements {
    /**
     * @author Stik
     */
    private  int id;
    private String name;
    private int price;
    private int succes;
    private boolean canbuy;

    public Ornements(int id, String name, int price, int succes, int canbuy) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.succes = succes;
        this.canbuy = (canbuy == 1);

    }


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public boolean isCanbuy() {
        return canbuy;
    }
    public void setCanbuy(boolean canbuy) {
        this.canbuy = canbuy;
    }
    public int getSucces() {
        return succes;
    }
    public void setSucces(int succes) {
        this.succes = succes;
    }


}