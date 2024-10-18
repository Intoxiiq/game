package soufix.Hdv;

import soufix.client.Account;
import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.ObjectTemplate;
import soufix.utility.Pair;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Hdv
{
  private int hdvId;
  private float taxe;
  private short sellTime;
  private short maxAccountItem;
  private String strCategory;
  private short lvlMax;

  private Map<Integer, HdvCategory> categorys=new HashMap<Integer, HdvCategory>();
  private Map<Integer, Pair<Integer, Integer>> path=new HashMap<Integer, Pair<Integer, Integer>>(); //<LigneID,<CategID,TemplateID>>

  private DecimalFormat pattern=new DecimalFormat("0.0");

  public Hdv(int hdvID, float taxe, short sellTime, short maxItemCompte, short lvlMax, String strCategory)
  {
    this.hdvId=hdvID;
    this.taxe=taxe;
    this.maxAccountItem=maxItemCompte;
    this.strCategory=strCategory;
    this.lvlMax=lvlMax;
    int categId;
    for(String strCategID : strCategory.split(","))
    {
      categId=Integer.parseInt(strCategID);
      this.categorys.put(categId,new HdvCategory(categId));
    }
  }

  public int getHdvId()
  {
    return hdvId;
  }

  public float getTaxe()
  {
    return taxe;
  }

  public short getSellTime()
  {
    return sellTime;
  }

  public short getMaxAccountItem()
  {
    return maxAccountItem;
  }

  public String getStrCategory()
  {
    return strCategory;
  }

  public short getLvlMax()
  {
    return lvlMax;
  }

  public Map<Integer, HdvCategory> getCategorys()
  {
    return categorys;
  }

  public boolean haveCategory(int categ)
  {
    return categorys.containsKey(categ);
  }

  //v2.8 - nullPointerException fix
  public HdvLine getLine(int lineId)
  {
    if(this.path==null||this.path.get(lineId)==null||this.getCategorys()==null)
      return null;

    int categoryId=this.path.get(lineId).getLeft();
    int templateId=this.path.get(lineId).getRight();

    HdvCategory category=this.getCategorys().get(categoryId);

    if(category==null)
      return null;

    HdvTemplate template=category.getTemplate(templateId);

    if(template==null)
      return null;

    return template.getLine(lineId);
  }

  public void addEntry(HdvEntry toAdd, boolean load)
  {
    toAdd.setHdvId(this.getHdvId());
    int categoryId=toAdd.getGameObject().getTemplate().getType();
    int templateId=toAdd.getGameObject().getTemplate().getId();
    if(this.getCategorys().get(categoryId)==null)
      return;
    this.getCategorys().get(categoryId).addEntry(toAdd);
    this.path.put(toAdd.getLineId(),new Pair<Integer, Integer>(categoryId,templateId));
    if(!load)
    {
     Database.getDynamics().getHdvObjectData().add(toAdd);
    }
    Main.world.addHdvItem(toAdd.getOwner(),this.getHdvId(),toAdd);
  }

  public boolean delEntry(HdvEntry toDel)
  {
    boolean toReturn=this.getCategorys().get(toDel.getGameObject().getTemplate().getType()).delEntry(toDel);
    if(toReturn)
    {
      this.path.remove(toDel.getLineId());
      Main.world.removeHdvItem(toDel.getOwner(),toDel.getHdvId(),toDel);
    }
    return toReturn;
  }

  public ArrayList<HdvEntry> getAllEntry()
  {
    ArrayList<HdvEntry> toReturn=new ArrayList<HdvEntry>();
    for(HdvCategory curCat : this.getCategorys().values())
      toReturn.addAll(curCat.getAllEntry());
    return toReturn;
  }

  public  boolean buyItem(int ligneID, byte amount, int price, Player newOwner)
  {
    boolean toReturn=true;
    try
    {
      if(newOwner.getKamas()<price)
        return false;

      HdvLine ligne=this.getLine(ligneID);
      HdvEntry toBuy=ligne.doYouHave(amount,price);
      Main.world.kamas_total -= price;
      newOwner.addKamas(price*-1,false);//Retire l'argent é l'acheteur (prix et taxe de vente)

      if(toBuy.getOwner()!=-1)
      {
    	  Account C=Main.world.getAccount(toBuy.getOwner());
        if(C!=null)
          C.setBankKamas(C.getBankKamas()+toBuy.getPrice());//Ajoute l'argent au vendeur
      }
      SocketManager.GAME_SEND_STATS_PACKET(newOwner);//Met a jour les kamas de l'acheteur

      toBuy.getGameObject().setPosition(Constant.ITEM_POS_NO_EQUIPED);
      newOwner.addObjet(toBuy.getGameObject(),true);//Ajoute l'objet au nouveau propriétaire
      toBuy.getGameObject().getTemplate().newSold(toBuy.getAmount(true),price);//Ajoute la ventes au statistiques
      delEntry(toBuy);//Retire l'item de l'HDV ainsi que de la liste du vendeur
     Database.getDynamics().getHdvObjectData().delete(toBuy.getGameObject().getGuid());
      if(Main.world.getAccount(toBuy.getOwner())!=null&&Main.world.getAccount(toBuy.getOwner()).getCurrentPlayer()!=null) {
        SocketManager.GAME_SEND_Im_PACKET(Main.world.getAccount(toBuy.getOwner()).getCurrentPlayer(),"065;"+price+"~"+toBuy.getGameObject().getTemplate().getId()+"~"+toBuy.getGameObject().getTemplate().getId()+"~1");
      }else
      {
    	  if(Main.world.getAccount(toBuy.getOwner()) != null)
    	  Main.world.getAccount(toBuy.getOwner()).hdv_offline += "X065;"+price+"~"+toBuy.getGameObject().getTemplate().getId()+"~"+toBuy.getGameObject().getTemplate().getId()+"~1";
      }
      // new code
      toBuy.id =  -398;
      toBuy.gameObject = null;
      toBuy.owner = -398;
      //Si le vendeur est connecter, envoie du packet qui lui annonce la vente de son objet
      //Database.getStatics().getPlayerData().update(newOwner);
    }
    catch(NullPointerException e)
    {
      e.printStackTrace();
      toReturn=false;
    }

    return toReturn;
  }

  public String parseToEHl(int templateID)
  {
    try
    {
      ObjectTemplate OT=Main.world.getObjTemplate(templateID);
      if(OT == null)
      return "|;;;;";  
      HdvCategory Hdv=this.getCategorys().get(OT.getType());
      HdvTemplate HdvT=Hdv.getTemplate(templateID);
      if(HdvT==null) // Il a pu étre acheté avant et supprimé de l'HDV. getTemplate devient null.
        return "|;;;;";
      return HdvT.parseToEHl();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return "|;;;;";
    //return this.getCategorys().getWaitingAccount(Main.world.getObjTemplate(templateID).getType()).getTemplate(templateID).parseToEHl();
  }

  public String parseTemplate(int categID)
  {
	  try {
    return this.getCategorys().get(categID).parseTemplate();
	  }catch(Exception e) {
		  return "";  
	  }
  }

  public String parseTaxe()
  {
    return pattern.format(this.getTaxe()).replace(",",".");
  }
}
