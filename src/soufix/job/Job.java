package soufix.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import soufix.client.Player;
import soufix.job.fm.Rune;
import soufix.object.GameObject;

public class Job
{
  private int id;
  private ArrayList<Integer> tools=new ArrayList<>();
  private Map<Integer, ArrayList<Integer>> crafts=new HashMap<>();
  private Map<Integer, ArrayList<Integer>> skills=new HashMap<>();
  private String name ;

  public Job(int id, String tools, String crafts, String skills , String name)
  {
	this.name = name;  
    this.id=id;
    if(!tools.equals(""))
    {
      for(String str : tools.split(","))
      {
        try
        {
          this.tools.add(Integer.parseInt(str));
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
    }
    if(!crafts.equals(""))
    {
      for(String str : crafts.split("\\|"))
      {
        try
        {
          int skID=Integer.parseInt(str.split(";")[0]);
          ArrayList<Integer> list=new ArrayList<>();
          for(String str2 : str.split(";")[1].split(","))
            list.add(Integer.parseInt(str2));
          this.crafts.put(skID,list);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
    }
    if(!skills.isEmpty()||!skills.equals(""))
    {
      for(String arg0 : skills.split("\\|"))
      {
        String io=arg0.split(";")[0],skill=arg0.split(";")[1];
        ArrayList<Integer> list=new ArrayList<>();

        for(String arg1 : skill.split(","))
          list.add(Integer.parseInt(arg1));

        for(String arg1 : io.split(","))
          this.skills.put(Integer.parseInt(arg1),list);
      }
    }
  }

  public int getId()
  {
    return this.id;
  }

  public Map<Integer, ArrayList<Integer>> getSkills()
  {
    return skills;
  }

  public boolean isValidTool(int id1)
  {
    for(int id : this.tools)
      if(id==id1)
        return true;
    return false;
  }

  public ArrayList<Integer> getListBySkill(int skill)
  {
    return this.crafts.get(skill);
  }

  public boolean canCraft(int skill, int template)
  {
    if(this.crafts.get(skill)!=null)
      for(int id : this.crafts.get(skill))
        if(id==template)
          return true;
    return false;
  }

  public boolean isMaging()
  {
    return (this.id>42&&this.id<51)||(this.id>61&&this.id<65);
  }

  public static int getActualJet(GameObject obj, String statsModif)
  {
    for(Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet())
    {
      if(Integer.toHexString(entry.getKey()).compareTo(statsModif)>0)//Effets inutiles
      {
        continue;
      }
      else if(Integer.toHexString(entry.getKey()).compareTo(statsModif)==0)//L'effet existe bien !
      {
        int JetActual=entry.getValue();
        return JetActual;
      }
    }
    return 0;
  }

  //v2.8 - complete negative stat maging
  public static int viewActualStatsItem(GameObject obj, String runeStat)
  {
    if(!obj.parseStatsString().isEmpty())
    {
      for(Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet())
      {
        if(!Integer.toHexString(entry.getKey()).equalsIgnoreCase(runeStat)) //Rune is not on item
        {
          if(Rune.getNegativeStatByRuneStat(runeStat).equalsIgnoreCase(Integer.toHexString(entry.getKey())))
          {
            return 2;
          }
          else
            continue;
        }
        else if(Integer.toHexString(entry.getKey()).equalsIgnoreCase(runeStat)) //Rune is on item
          return 1;
      }
      return 0;
    }
    else
    {
      return 0;
    }
  }

public String getName() {
	return name;
}
}
