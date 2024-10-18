package soufix.area.map.entity;

import soufix.area.map.GameMap;
import soufix.common.Formulas;
import soufix.entity.mount.Mount;
import soufix.guild.Guild;
import soufix.main.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class MountPark
{
  private int owner;
  private int size;
  private Guild guild;
  private GameMap map;
  private int cell=-1;
  private int price;
  private int placeOfSpawn;
  private int maxObject;
  private int door;
  private ArrayList<Integer> cellOfObject=new ArrayList<Integer>();
  private Map<Integer, Integer> cellAndObject=new HashMap<Integer, Integer>();
  private Map<Integer, Map<Integer, Integer>> objDurab=new HashMap<Integer, Map<Integer, Integer>>();
  private Map<Integer, Map<Integer, Integer>> breedingObject=new HashMap<Integer, Map<Integer, Integer>>();
  private CopyOnWriteArrayList<Integer> raising=new CopyOnWriteArrayList<Integer>();
  private ArrayList<Mount> etable=new ArrayList<Mount>();

  public MountPark(GameMap map, int cellid, int size, int placeOfSpawn, int door, String cellOfObject, int maxObject)
  {
    this.map=map;
    this.cell=cellid;
    this.size=size;
    this.placeOfSpawn=placeOfSpawn;
    this.door=door;
    this.maxObject=maxObject;

    if(!cellOfObject.isEmpty())
    {
      for(String cases : cellOfObject.split(";"))
      {
        int cellId=Integer.parseInt(cases);
        if(cellId>0)
          this.cellOfObject.add(cellId);
      }
    }
  }

  public void setData(int owner, int guild, int price, String raising, String objects, String objDurab, String etable)
  {
    this.owner=owner;
    this.guild=Main.world.getGuild(guild);
    this.price=price;
    if(!objects.isEmpty())
    {
      for(String object : objects.split("\\|"))
      {
        String[] infos=object.split(";");
        int cellId=Integer.parseInt(infos[0]);
        int objectId=Integer.parseInt(infos[1]);
        int proprietor=Integer.parseInt(infos[2]);
        java.util.Map<Integer, Integer> other=new HashMap<Integer, Integer>();
        other.put(objectId,proprietor);
        this.cellAndObject.put(cellId,objectId);
        this.breedingObject.put(cellId,other);
      }
    }
    //chargement de la liste des dragodinde dans l'Ã©table
    for(String i : raising.split(";"))
    {
      try
      {
        Mount DD=Main.world.getMountById(Integer.parseInt(i));
        if(DD!=null)
          this.etable.add(DD);
      }
      catch(Exception e)
      {
      }
    }
    if(!objDurab.isEmpty())
    {
      for(String object : objDurab.split("\\|"))
      {
        String[] infos=object.split(";");
        int cellId=Integer.parseInt(infos[0]);
        int durability=Integer.parseInt(infos[1]);
        int durabilityMax=Integer.parseInt(infos[2]);
        Map<Integer, Integer> inDurab=new HashMap<Integer, Integer>();
        inDurab.put(durability,durabilityMax);
        this.objDurab.put(cellId,inDurab);
      }
    }
    if(!etable.isEmpty())
      for(String dd : etable.split(";"))
      {
        try
        {
          this.raising.add(Integer.parseInt(dd));
          Mount mount=Main.world.getMountById(Integer.parseInt(dd));
          mount.setMapId(this.map.getId());
          mount.setCellId(mount.getCellId());
        }
        catch(Exception ignored)
        {
        }
      }
    if(this.map!=null)
      this.map.setMountPark(this);
    for(String firstCut : etable.split(";"))//PosseseurID,DragoID;PosseseurID2,DragoID2;PosseseurID,DragoID3
    {
      try
      {
        String[] secondCut=firstCut.split(",");
        Mount DD=Main.world.getMountById(Integer.parseInt(secondCut[1]));
        if(DD==null)
          continue;
        this.raising.add(Integer.parseInt(secondCut[1]),Integer.parseInt(secondCut[0]));
      }
      catch(Exception ignored)
      {
      }
    }
  }

  public void setInfos(GameMap map, int cellid, int size, int placeOfSpawn, int door, String cellOfObject, int maxObject)
  {
    this.map=map;
    this.cell=cellid;
    this.size=size;
    this.placeOfSpawn=placeOfSpawn;
    this.door=door;
    this.maxObject=maxObject;

    if(!cellOfObject.isEmpty())
    {
      for(String cases : cellOfObject.split(";"))
      {
        int cellId=Integer.parseInt(cases);
        if(cellId>0)
          this.cellOfObject.add(cellId);
      }
    }
  }

  public void setDoor(int id)
  {
    this.door=id;
  }

  public int getMountcell()
  {
    return placeOfSpawn;
  }

  public void setMountCell(int id)
  {
    this.placeOfSpawn=id;
  }

  @SuppressWarnings("unchecked")
  public void setCellObject(ArrayList<Integer> array)
  {
    this.cellOfObject=(ArrayList<Integer>)array.clone();
  }

  public int getOwner()
  {
    return this.owner;
  }

  public void setOwner(int owner)
  {
    this.owner=owner;
  }

  public int getSize()
  {
    return this.size;
  }

  public Guild getGuild()
  {
    return this.guild;
  }

  public void setGuild(Guild guild)
  {
    this.guild=guild;
  }

  public GameMap getMap()
  {
    return this.map;
  }

  public int getCell()
  {
    return this.cell;
  }

  public int getPrice()
  {
    return this.price;
  }

  public void setPrice(int price)
  {
    this.price=price;
  }

  public int getPlaceOfSpawn()
  {
    return this.placeOfSpawn;
  }

  public int getMaxObject()
  {
    return this.maxObject;
  }

  public int getDoor()
  {
    return this.door;
  }

  public ArrayList<Integer> getCellOfObject()
  {
    return this.cellOfObject;
  }

  public boolean hasEtableFull(int id)
  {
    if(this.getOwner()==-1)
    {
      int i=0;
      for(Mount mount : this.getEtable())
        if(mount.getOwner()==id)
          i++;
      return i>=100;
    } else
    {
      return this.getEtable().size()>=100;
    }
  }

  public boolean hasEnclosFull(int id)
  {
    if(this.getOwner()==-1)
    {
      int i=0;
      for(int mountId : this.getListOfRaising())
        if(mountId==id)
          i++;
      return i>=this.getSize();
    } else
    {
      return this.getListOfRaising().size()>=this.getSize();
    }
  }

  public void addCellObject(int cell)
  {
    if(this.cellOfObject.contains(cell))
      return;
    if(cell<=0)
      return;
    this.cellOfObject.add(cell);
  }

  public String parseStringCellObject()
  {
    StringBuilder cell=new StringBuilder();
    boolean first=true;
    for(Integer i : this.cellOfObject)
    {
      if(first)
        cell.append(i);
      else
        cell.append(";"+i);
      first=false;
    }
    return cell.toString();
  }

  public Map<Integer, Integer> getCellAndObject()
  {
    return this.cellAndObject;
  }

  public void addObject(int cell, int object, int owner, int durability, int durabilityMax)
  {
    if(this.breedingObject.containsKey(cell))
    {
      this.breedingObject.remove(cell);
      this.cellAndObject.remove(cell);
    }
    Map<Integer, Integer> other=new HashMap<Integer, Integer>();
    other.put(object,owner);

    Map<Integer, Integer> inDurab=new HashMap<Integer, Integer>();
    inDurab.put(durability,durabilityMax);

    this.cellAndObject.put(cell,object);
    this.breedingObject.put(cell,other);
    this.objDurab.put(cell,inDurab);
  }

  public boolean delObject(int cell)
  {
    if(!this.breedingObject.containsKey(cell)&&!this.objDurab.containsKey(cell))
      return false;
    this.objDurab.remove(cell);
    this.breedingObject.remove(cell);
    this.cellAndObject.remove(cell);
    return true;
  }

  public Map<Integer, Map<Integer, Integer>> getObjDurab()
  {
    return this.objDurab;
  }

  public Map<Integer, Map<Integer, Integer>> getObject()
  {
    return this.breedingObject;
  }

  public void addRaising(int id)
  {
    this.raising.add(id);
  }

  public void delRaising(int id)
  {
    if(this.raising.contains(id))
      this.raising.remove(this.raising.indexOf(id));
  }

  public CopyOnWriteArrayList<Integer> getListOfRaising()
  {
    return this.raising;
  }

  public ArrayList<Mount> getEtable()
  {
    return this.etable;
  }

  public  void startMoveMounts()
  {
    if(this.raising.size()>0)
    {
      char[] directions= { 'b', 'd', 'f', 'h' };
      for(Integer id : this.raising)
      {
        Mount mount=Main.world.getMountById(id);
        if(mount!=null)
        {
          mount.moveMountsAuto(directions[Formulas.getRandomValue(0,3)],3,false);
        }
      }
    }
  }

  //v2.7 - Replaced String += with StringBuilder
  public String getStringObject()
  {
    StringBuilder str=new StringBuilder();
    boolean first=false;

    if(this.breedingObject.size()==0)
      return str.toString();

    for(Map.Entry<Integer, Map<Integer, Integer>> entry : this.breedingObject.entrySet())
    {
      if(first)
        str.append("|");
      str.append(entry.getKey());

      for(Map.Entry<Integer, Integer> entry2 : entry.getValue().entrySet())
        str.append(";"+entry2.getKey()+";"+entry2.getValue());
      first=true;
    }
    return str.toString();
  }

  //v2.7 - Replaced String += with StringBuilder
  public String getStringObjDurab()
  {
    StringBuilder str=new StringBuilder();
    boolean first=false;
    if(this.objDurab.size()==0)
      return str.toString();
    for(Map.Entry<Integer, java.util.Map<Integer, Integer>> entry : this.objDurab.entrySet())
    {
      if(first)
        str.append("|");
      str.append(entry.getKey());
      for(Map.Entry<Integer, Integer> entry2 : entry.getValue().entrySet())
      {
        str.append(";"+entry2.getKey()+";"+entry2.getValue());
      }
      first=true;
    }
    return str.toString();
  }

  //v2.7 - Replaced String += with StringBuilder
  public String parseRaisingToString()
  {
    StringBuilder str=new StringBuilder();
    boolean first=true;
    if(this.raising.size()==0)
      return "";
    for(Integer id : this.raising)
    {
      if(!first)
        str.append(";");
      str.append(id);
      first=false;
    }
    return str.toString();
  }

  //v2.7 - Replaced String += with StringBuilder
  public String parseEtableToString()
  {
    StringBuilder str=new StringBuilder();
    for(Mount mount : this.etable)
    {
      if(!str.toString().equalsIgnoreCase(""))
        str.append(";");
      str.append(mount.getId());
    }
    return str.toString();
  }
}
