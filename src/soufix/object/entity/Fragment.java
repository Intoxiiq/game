package soufix.object.entity;

import soufix.object.GameObject;
import soufix.utility.Pair;

import java.util.ArrayList;

public class Fragment extends GameObject
{
  private ArrayList<Pair<Integer, Integer>> runes;

  public Fragment(int Guid, String runes)
  {
    super(Guid);
    this.runes=new ArrayList<>();

    if(!runes.isEmpty())
    {
      for(String rune : runes.split(";"))
      {
        String[] split=rune.split(":");
        this.runes.add(new Pair<>(Integer.parseInt(split[0]),Integer.parseInt(split[1])));
      }
    }
  }

  public ArrayList<Pair<Integer, Integer>> getRunes()
  {
    return runes;
  }

  public void addRune(int id)
  {
    Pair<Integer, Integer> rune=this.search(id);

    if(rune==null)
      this.runes.add(new Pair<>(id,1));
    else
      rune.right+=1;
  }

  public Pair<Integer, Integer> search(int id)
  {
    for(Pair<Integer, Integer> couple : this.runes)
      if(couple.getLeft()==id)
        return couple;
    return null;
  }
}