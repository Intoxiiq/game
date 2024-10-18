package soufix.area;

import soufix.area.map.GameMap;
import soufix.main.Main;

import java.util.ArrayList;
import java.util.List;

public class SubArea
{
  public static int bontarians=0, brakmarians=0;

  private final int id;
  private final Area area;
  private int alignement, prismId;
  private boolean conquistable;
  private final ArrayList<GameMap> maps=new ArrayList<>();
  private final String name;
  public long time = 0;
  public final List<String> perco = new ArrayList<>();

  public SubArea(int id, int area, String name)
  {
    this.id=id;
    this.area=Main.world.getArea(area);
    this.name=name;
  }

  public int getId()
  {
    return id;
  }

  public Area getArea()
  {
    return area;
  }

  public int getAlignement()
  {
    return alignement;
  }

  public void setAlignement(int alignement)
  {
    if(this.alignement==1&&alignement==-1)
      bontarians--;
    else if(this.alignement==2&&alignement==-1)
      brakmarians--;
    else if(this.alignement==-1&&alignement==1)
      bontarians++;
    else if(this.alignement==-1&&alignement==2)
      brakmarians++;
    this.alignement=alignement;
  }

  public int getPrismId()
  {
    return prismId;
  }

  public void setPrismId(int prism)
  {
    this.prismId=prism;
  }

  public boolean getConquistable()
  {
    return conquistable;
  }

  public void setConquistable(int conquistable)
  {
    this.conquistable=conquistable==0;
  }

  public ArrayList<GameMap> getMaps()
  {
    return maps;
  }

  public void addMap(GameMap Map)
  {
    this.maps.add(Map);
  }

  public String getName()
  {
    return name;
  }
}