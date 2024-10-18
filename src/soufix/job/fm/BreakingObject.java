package soufix.job.fm;

import java.util.ArrayList;

import soufix.utility.Pair;

public class BreakingObject
{

  private ArrayList<Pair<Integer, Integer>> objects=new ArrayList<>();
  private int count=0;
  private boolean stop=false;
  private boolean ok=false;


public void setCount(int count)
  {
    this.count=count;
  }

  public int getCount()
  {
    return count;
  }

  public void setStop(boolean stop)
  {
    this.stop=stop;
  }

  public boolean isStop()
  {
    return stop;
  }
  public boolean isOk() {
	return ok;
}

public void setOk(boolean ok) {
	this.ok = ok;
}

  public void setObjects(ArrayList<Pair<Integer, Integer>> objects)
  {
    this.objects=objects;
  }

  public ArrayList<Pair<Integer, Integer>> getObjects()
  {
    return objects;
  }

  public int addObject(int id, int quantity)
  {
    Pair<Integer, Integer> couple=this.search(id);

    if(couple==null)
    {
      this.objects.add(new Pair<>(id,quantity));
      return quantity;
    } else
    {
      couple.right+=quantity;
      return couple.right;
    }
  }

  public int removeObject(int id, int quantity)
  {
    Pair<Integer, Integer> couple=this.search(id);

    if(couple!=null)
    {
      if(quantity>couple.getRight())
      {
        this.objects.remove(couple);
        return quantity;
      } else
      {
        couple.right-=quantity;
        if(couple.getRight()<=0)
        {
          this.objects.remove(couple);
          return 0;
        }
        return couple.getRight();
      }
    }
    return 0;
  }

  private Pair<Integer, Integer> search(int id)
  {
    for(Pair<Integer, Integer> couple : this.objects)
      if(couple.getLeft()==id)
        return couple;
    return null;
  }
}