package soufix.client.other;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import soufix.client.Player;
import soufix.main.Constant;

public interface IStats {
	IStats getBase();
	
	Map<Integer, Integer> getMap();
	int addOneStat(int id, int val);
	boolean isSameStats(IStats other);
	
	void replace(IStats stats);

	void clear();
	
	default int get(int id) { return getMap().getOrDefault(id, 0); };
	
	default String parseToItemSetStats() {
		if(this.getMap().isEmpty()) return "";
		
	    StringBuilder str=new StringBuilder();
	    for(Entry<Integer, Integer> entry : this.getMap().entrySet())
	    {
	      if(str.length()>0)
	        str.append(",");
	      str.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue())).append("#0#0");
	    }
	    return str.toString();
	}
	
	default public int getEffectSummed(int id) {
		Map<Integer,Integer> effects = getMap();
		
	    int val;
	    if(effects.get(id)==null)
	      val =0;
	    else
	      val = effects.get(id);

	    switch(id)
	    {
	      case Constant.STATS_ADD_PA:
	        if(effects.get(Constant.STATS_ADD_PA2)!=null)
	          val += effects.get(Constant.STATS_ADD_PA2);
	        if(effects.get(Constant.STATS_REM_PA)!=null)
	          val -= effects.get(Constant.STATS_REM_PA);
	        if(effects.get(Constant.STATS_REM_PA2)!=null) //Non esquivable
	          val -= effects.get(Constant.STATS_REM_PA2);
	        break;
	      case Constant.STATS_ADD_PM:
	        if(effects.get(Constant.STATS_ADD_PM2)!=null)
	          val += effects.get(Constant.STATS_ADD_PM2);
	        if(effects.get(Constant.STATS_REM_PM)!=null)
	          val -= effects.get(Constant.STATS_REM_PM);
	        if(effects.get(Constant.STATS_REM_PM2)!=null)//Non esquivable
	          val -= effects.get(Constant.STATS_REM_PM2);
	        break;
	      case Constant.STATS_ADD_PO:
	        if(effects.get(Constant.STATS_REM_PO)!=null)
	          val -= effects.get(Constant.STATS_REM_PO);
	        break;
	      case Constant.STATS_ADD_SUM:
	        if(effects.get(Constant.STATS_REM_SUM)!=null)
	          val -= effects.get(Constant.STATS_REM_SUM);
	        break;
	      case Constant.STATS_ADD_DOMA:
	        if(effects.get(Constant.STATS_REM_DOMA)!=null)
	          val -= effects.get(Constant.STATS_REM_DOMA);
	        break;
	      case Constant.STATS_ADD_CC:
	        if(effects.get(Constant.STATS_REM_CC)!=null)
	          val -= effects.get(Constant.STATS_REM_CC);
	        break;
	      case Constant.STATS_ADD_SOIN:
	        if(effects.get(Constant.STATS_REM_SOIN)!=null)
	          val-=effects.get(Constant.STATS_REM_SOIN);
	        break;
	      case Constant.STATS_ADD_RP_TER:
	        if(effects.get(Constant.STATS_REM_RP_TER)!=null)
	          val-=effects.get(Constant.STATS_REM_RP_TER);
	        break;
	      case Constant.STATS_ADD_RP_EAU:
	        if(effects.get(Constant.STATS_REM_RP_EAU)!=null)
	          val-=effects.get(Constant.STATS_REM_RP_EAU);
	        break;
	      case Constant.STATS_ADD_RP_AIR:
	        if(effects.get(Constant.STATS_REM_RP_AIR)!=null)
	          val-=effects.get(Constant.STATS_REM_RP_AIR);
	        break;
	      case Constant.STATS_ADD_RP_FEU:
	        if(effects.get(Constant.STATS_REM_RP_FEU)!=null)
	          val-=effects.get(Constant.STATS_REM_RP_FEU);
	        break;
	      case Constant.STATS_ADD_RP_NEU:
	        if(effects.get(Constant.STATS_REM_RP_NEU)!=null)
	          val-=effects.get(Constant.STATS_REM_RP_NEU);
	        break;
	      case Constant.STATS_ADD_PROS:
	        if(effects.get(Constant.STATS_REM_PROS)!=null)
	          val-=effects.get(Constant.STATS_REM_PROS);
	        break;
	      case Constant.STATS_ADD_SAGE:
	        if(effects.get(Constant.STATS_REM_SAGE)!=null)
	          val-=effects.get(Constant.STATS_REM_SAGE);
	        break;
	      case Constant.STATS_ADD_R_FEU:
	        if(effects.get(Constant.STATS_REM_R_FEU)!=null)
	          val-=effects.get(Constant.STATS_REM_R_FEU);
	        break;
	      case Constant.STATS_ADD_R_NEU:
	        if(effects.get(Constant.STATS_REM_R_NEU)!=null)
	          val-=effects.get(Constant.STATS_REM_R_NEU);
	        break;
	      case Constant.STATS_ADD_R_TER:
	        if(effects.get(Constant.STATS_REM_R_TER)!=null)
	          val-=effects.get(Constant.STATS_REM_R_TER);
	        break;
	      case Constant.STATS_ADD_R_EAU:
	        if(effects.get(Constant.STATS_REM_R_EAU)!=null)
	          val-=effects.get(Constant.STATS_REM_R_EAU);
	        break;
	      case Constant.STATS_ADD_R_AIR:
	        if(effects.get(Constant.STATS_REM_R_AIR)!=null)
	          val-=effects.get(Constant.STATS_REM_R_AIR);
	        break;
	      case Constant.STATS_ADD_PERDOM:
	        if(effects.get(Constant.STATS_REM_PERDOM)!=null)
	          val-=effects.get(Constant.STATS_REM_PERDOM);
	        break;
	      case Constant.STATS_ADD_FORC:
	        if(effects.get(Constant.STATS_REM_FORC)!=null)
	          val-=effects.get(Constant.STATS_REM_FORC);
	        break;
	      case Constant.STATS_ADD_INTE:
	        if(effects.get(Constant.STATS_REM_INTE)!=null)
	          val-=effects.get(Constant.STATS_REM_INTE);
	        break;
	      case Constant.STATS_ADD_CHAN:
	        if(effects.get(Constant.STATS_REM_CHAN)!=null)
	          val-=effects.get(Constant.STATS_REM_CHAN);
	        break;
	      case Constant.STATS_ADD_AGIL:
	        if(effects.get(Constant.STATS_REM_AGIL)!=null)
	          val-=effects.get(Constant.STATS_REM_AGIL);
	        break;
	      case Constant.STATS_ADD_PODS:
	        if(effects.get(Constant.STATS_REM_PODS)!=null)
	          val-=effects.get(Constant.STATS_REM_PODS);
	        break;
	      case Constant.STATS_ADD_VITA:
	        if(effects.get(Constant.STATS_REM_VITA)!=null)
	          val-=effects.get(Constant.STATS_REM_VITA);
	        break;
	      case Constant.STATS_ADD_VIE:
	        val=Constant.STATS_ADD_VIE;
	        break;
	      case Constant.STATS_ADD_INIT:
	        if(effects.get(Constant.STATS_REM_INIT)!=null)
	          val-=effects.get(Constant.STATS_REM_INIT);
	        break;
	        
	      case Constant.STATS_ADD_AFLEE:
	        if(effects.get(Constant.STATS_REM_AFLEE)!=null)
	          val-=getEffectSummed(Constant.STATS_REM_AFLEE);
	        if(effects.get(Constant.STATS_ADD_SAGE)!=null)
	          val+=getEffectSummed(Constant.STATS_ADD_SAGE)/4;
	        break;
	      case Constant.STATS_ADD_MFLEE:
	        if(effects.get(Constant.STATS_REM_MFLEE)!=null)
	          val-=getEffectSummed(Constant.STATS_REM_MFLEE);
	        if(effects.get(Constant.STATS_ADD_SAGE)!=null)
	          val+=getEffectSummed(Constant.STATS_ADD_SAGE)/4;
	        break;
	      case Constant.STATS_ADD_MAITRISE:
	        if(effects.get(Constant.STATS_ADD_MAITRISE)!=null)
	          val=effects.get(Constant.STATS_ADD_MAITRISE);
	        break;
	    }
	    return val;
	  }
	
	public static Stats cumulStat(IStats s1, IStats s2, Player perso) {
	    HashMap<Integer, Integer> effets=new HashMap<Integer, Integer>();
	    for(int a=0;a<=Constant.MAX_EFFECTS_ID;a++)
	    {
	      int v1 = s1.get(a);
	      int v2 = s2.get(a);
	      
	      int sum= v1 + v2;
	      if(sum == 0) continue;
	      
	    
	      if(perso != null)
	      if(perso.getFight() != null)
	      if(perso.getFight().getType() == Constant.FIGHT_TYPE_AGRESSION || 
	      perso.getFight().getType() == Constant.FIGHT_TYPE_KOLI || 
	      perso.getFight().getType() == Constant.FIGHT_TYPE_PVT || 
	      perso.getFight().getType() == Constant.FIGHT_TYPE_CONQUETE) {
	          if(a==Constant.STATS_ADD_PA && sum>16)sum=16;
	  		if(a==Constant.STATS_ADD_PM && sum>8)sum=8;
	      }
	    	  		if(a==Constant.STATS_ADD_RP_TER && sum>50)sum=50;
	    	  		if(a==Constant.STATS_ADD_RP_EAU && sum>50)sum=50;
	    	  		if(a==Constant.STATS_ADD_RP_AIR && sum>50)sum=50;
	    	  		if(a==Constant.STATS_ADD_RP_FEU && sum>50)sum=50;
	    	  		if(a==Constant.STATS_ADD_RP_NEU && sum>50)sum=50;
	      effets.put(a,sum);
	    }
	    return new Stats(effets,false,null);
	  }

}
