package soufix.Hdv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import soufix.main.Main;

public class HdvTemplate
{
  private int templateId;
  private Map<Integer, HdvLine> lines=new HashMap<Integer, HdvLine>();

  public HdvTemplate(int templateId, HdvEntry toAdd)
  {
    this.templateId=templateId;
    addEntry(toAdd);
  }

  public int getTemplateId()
  {
    return templateId;
  }

  public Map<Integer, HdvLine> getLines()
  {
    return lines;
  }

  public HdvLine getLine(int lineId)
  {
    return lines.get(lineId);
  }

  public void addEntry(HdvEntry toAdd)
  {
    for(HdvLine line : this.getLines().values())
      //Boucle dans toutes les lignes pour essayer de trouver des objets de mémes stats
      if(line.addEntry(toAdd))//Si une ligne l'accepte, arréte la méthode.
        return;
    int lineId=Main.world.getNextLineHdvId();
    this.getLines().put(lineId,new HdvLine(lineId,toAdd));
  }

  public boolean delEntry(HdvEntry toDel)
  {
    boolean toReturn=this.getLines().get(toDel.getLineId()).delEntry(toDel);
    if(this.getLines().get(toDel.getLineId()).isEmpty())//Si la ligne est devenue vide
      this.getLines().remove(toDel.getLineId());
    return toReturn;
  }

  public ArrayList<HdvEntry> getAllEntry()
  {
    ArrayList<HdvEntry> toReturn=new ArrayList<HdvEntry>();
    for(HdvLine line : this.getLines().values())
      toReturn.addAll(line.getAll());
    return toReturn;
  }

  public boolean isEmpty()
  {
    return this.getLines().size()==0;
  }

  //v2.7 - replaced String += with StringBuilder
  public String parseToEHl()
  {
    StringBuilder toReturn=new StringBuilder(this.getTemplateId()+"|");
    boolean isFirst=true;
    for(HdvLine line : this.getLines().values())
    {
      if(!isFirst)
        toReturn.append("|");
      toReturn.append(line.parseToEHl());
      isFirst=false;
    }
    return toReturn.toString();
  }
}