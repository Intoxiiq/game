package soufix.utility;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import soufix.entity.Prism;

public class Sort
{
  public static Map<Integer, Prism> sortPrismsAlphabetically(Map<Integer, Prism> map)
  {
    List<Map.Entry<Integer, Prism>> entries=new LinkedList<Map.Entry<Integer, Prism>>(map.entrySet());
    Collections.sort(entries,new Comparator<Map.Entry<Integer, Prism>>()
    {
      @Override
      public int compare(Entry<Integer, Prism> o1, Entry<Integer, Prism> o2)
      {
        return o1.getValue().getSubArea().getName().compareToIgnoreCase(o2.getValue().getSubArea().getName());
      }
    });

    Map<Integer, Prism> sortedMap=new LinkedHashMap<Integer, Prism>();

    for(Map.Entry<Integer, Prism> entry : entries)
    {
      sortedMap.put(entry.getKey(),entry.getValue());
    }

    return sortedMap;
  }
}
