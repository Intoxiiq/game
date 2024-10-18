package soufix.fight.spells;

import java.util.*;
import java.util.Map.Entry;
import soufix.area.map.GameCase;
import soufix.client.Player;
import soufix.common.Formulas;
import soufix.common.PathFinding;
import soufix.common.SocketManager;
import soufix.entity.monster.MobGrade;
import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.spells.Spell.SortStats;
import soufix.fight.traps.Glyph;
import soufix.fight.traps.Trap;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;

public class SpellEffect
{
  private final int durationFixed;
  private int effectID;
  private int turns=0;
  private String jet="0d0+0";
  private int chance=100;
  private String args;
  private int value=0;
  private Fighter caster=null;
  private int spell=0;
  private int spellLvl=1;
  private boolean debuffable=true;
  private int duration=0;
  private GameCase cell=null;
  private boolean aoe=false;
  private boolean isTrap=false;

  public SpellEffect(int aID, String aArgs, int aSpell, int aSpellLevel)
  {
    effectID=aID;
    args=aArgs;
    spell=aSpell;
    spellLvl=aSpellLevel;
    durationFixed=0;
    try
    {
      value=Integer.parseInt(args.split(";")[0]);
      turns=Integer.parseInt(args.split(";")[3]);
      chance=Integer.parseInt(args.split(";")[4]);
      jet=args.split(";")[5];
    }
    catch(Exception ignored)
    {
    }
  }

  public SpellEffect(int id, int value2, int aduration, int turns2, boolean debuff, Fighter aCaster, String args2, int aspell)
  {
    effectID=id;
    value=value2;
    turns=turns2;
    debuffable=debuff;
    caster=aCaster;
    duration=aduration;
    this.durationFixed=duration;
    args=args2;
    spell=aspell;
    try
    {
      jet=args.split(";")[5];
    }
    catch(Exception ignored)
    {
    }
  }

  public static ArrayList<Fighter> getTargets(SpellEffect SE, Fight fight, ArrayList<GameCase> cells)
  {
    ArrayList<Fighter> cibles=new ArrayList<Fighter>();
    for(GameCase aCell : cells)
    {
      if(aCell==null)
        continue;
      Fighter f=aCell.getFirstFighter();
      if(f==null)
        continue;
      cibles.add(f);
    }
    return cibles;
  }

  //v2.8 - Reinforcementex
  public static int applyOnHitBuffs(int finalDommage, Fighter target, Fighter caster, Fight fight, int elementId , int idspell)
  {
    for(int id : Constant.ON_HIT_BUFFS)
    {
      for(SpellEffect buff : target.getBuffsByEffectID(id))
      {
        switch(id)
        {
          case 114: //multiply damage by x
            if(buff.getSpell()==521) //kitsou ruse
              finalDommage=finalDommage*2;
            break;
          case 138: //% damage
            if(buff.getSpell()==1039) //Hololol
            {
              int stats=0;
              if(elementId==Constant.ELEMENT_AIR)
                stats=217;
              else if(elementId==Constant.ELEMENT_EAU)
                stats=216;
              else if(elementId==Constant.ELEMENT_FEU)
                stats=218;
              else if(elementId==Constant.ELEMENT_NEUTRE)
                stats=219;
              else if(elementId==Constant.ELEMENT_TERRE)
                stats=215;
              int value=50;
              int turns=buff.getDuration();
              int duration=buff.getDuration();
              String args=buff.getArgs();
              for(int i : Constant.getOppositeStats(stats))
              {
                target.addBuff(i,value,turns,duration,true,buff.getSpell(),args,caster,true);
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,i,caster.getId()+"",target.getId()+","+value+","+turns);
              }
              target.addBuff(stats,value,turns,duration,true,buff.getSpell(),args,caster,true);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,stats,caster.getId()+"",target.getId()+","+value+","+turns);
            }
            break;
          case 9: //Derobade
            int d=PathFinding.getDistanceBetween(fight.getMap(),target.getCell().getId(),caster.getCell().getId());
            if(d>1)
              continue;
            int chan=buff.getValue();
            int c=Formulas.getRandomValue(0,99);
            if(c+1>=chan)
              continue;
            int nbrCase=0;
            try
            {
              nbrCase=Integer.parseInt(buff.getArgs().split(";")[1]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(nbrCase==0)
              continue;
            int exCase=target.getCell().getId();
            int newCellID=PathFinding.newCaseAfterPush(fight,caster.getCell(),target.getCell(),nbrCase,false);
            if(newCellID<0)
            {
              int a=-newCellID;
              a=nbrCase-a;
              newCellID=PathFinding.newCaseAfterPush(fight,caster.getCell(),target.getCell(),a,false);
              if(newCellID==0) {
            	  finalDommage=0;
                continue;
              }
              if(fight.getMap().getCase(newCellID)==null) {
            	  finalDommage=0;
                continue;
                
              }
            }
            if(newCellID == 0)
            	continue;
            target.getCell().getFighters().clear();
            target.setCell(fight.getMap().getCase(newCellID));
            target.getCell().addFighter(target);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,5,target.getId()+"",target.getId()+","+newCellID);

            /*ArrayList<Trap> P=(new ArrayList<Trap>());
            P.addAll(fight.getAllTraps());
            for(Trap p : P)
            {
              int dist=PathFinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),target.getCell().getId());
              //on active le piege
              if(dist<=p.getSize())
                p.onTraped(target);
            }*/
            verifyTraps(fight,target);
            //si le joueur a bouger
            if(exCase!=newCellID)
              finalDommage=0;
            break;

          case 79: //chance Ã©ca
            try
            {
              String[] infos=buff.getArgs().split(";");
              int coefDom=Integer.parseInt(infos[0]);
              int coefHeal=Integer.parseInt(infos[1]);
              int chance=Integer.parseInt(infos[2]);
              int jet=Formulas.getRandomValue(0,99);

              if(jet<chance) //Soin
              {
                finalDommage=-(finalDommage*coefHeal);
                if(-finalDommage>(target.getPdvMax()-target.getPdv()))
                  finalDommage=-(target.getPdvMax()-target.getPdv());
              }
              else //Dommage
                finalDommage=finalDommage*coefDom;
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            break;

          case 107: //renvoie Dom
            if(target.getId()==caster.getId())
              break;
            if(caster.hasBuff(765))//sacrifice
            {
              if(caster.getBuff(765)!=null&&!caster.getBuff(765).getCaster().isDead())
              {
                applyEffect_765B(fight,caster);
                caster=caster.getBuff(765).getCaster();
              }
            }

            String[] args=buff.getArgs().split(";");
            float coef=1+(target.getTotalStats().getEffect(Constant.STATS_ADD_SAGE)/100);
            int renvoie=0;
            try
            {
              if(Integer.parseInt(args[1])!=-1)
              {
                renvoie=(int)(coef*Formulas.getRandomValue(Integer.parseInt(args[0]),Integer.parseInt(args[1])));
              }
              else
              {
                renvoie=(int)(coef*Integer.parseInt(args[0]));
              }
            }
            catch(Exception e)
            {
            	 e.printStackTrace();
              return finalDommage;
            }
            if(renvoie>finalDommage)
              renvoie=finalDommage;
            finalDommage-=renvoie;
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,107,"-1",target.getId()+","+renvoie);
            if(renvoie>caster.getPdv())
              renvoie=caster.getPdv();
            if(finalDommage<0)
              finalDommage=0;
            
            renvoie-=Formulas.getArmorResist(caster,-1);
            if(caster.hasBuff(149))
                if(caster.getBuff(149).spell == 197)
                	renvoie = 0;
            if(renvoie<0)
            	renvoie=0;
            caster.removePdv(caster,renvoie);
            caster.removePdvMax((int)Math.floor(renvoie*(Config.getInstance().erosion+target.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-target.getTotalStats().getEffect(Constant.STATS_REM_ERO)-caster.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+caster.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",caster.getId()+",-"+renvoie);
            break;
          case 606://Chatiment (acncien)
        	  if(idspell == 435)
        		  break;
            int stat=buff.getValue();
            int jet=Formulas.getRandomJet(buff.getJet());
            target.addBuff(stat,jet,-1,-1,false,buff.getSpell(),buff.getArgs(),caster,true);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,stat,caster.getId()+"",target.getId()+","+jet+","+-1);
            break;
          case 607://Chatiment (acncien)
        	  if(idspell == 435)
        		  break;
            stat=buff.getValue();
            jet=Formulas.getRandomJet(buff.getJet());
            target.addBuff(stat,jet,-1,-1,false,buff.getSpell(),buff.getArgs(),caster,true);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,stat,caster.getId()+"",target.getId()+","+jet+","+-1);
            break;
          case 608://Chatiment (acncien)
        	  if(idspell == 435)
        		  break;
            stat=buff.getValue();
            jet=Formulas.getRandomJet(buff.getJet());
            target.addBuff(stat,jet,-1,-1,false,buff.getSpell(),buff.getArgs(),caster,true);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,stat,caster.getId()+"",target.getId()+","+jet+","+-1);
            break;
          case 609://Chatiment (acncien)
        	  if(idspell == 435)
        		  break;
            stat=buff.getValue();
            jet=Formulas.getRandomJet(buff.getJet());
            target.addBuff(stat,jet,-1,-1,false,buff.getSpell(),buff.getArgs(),caster,true);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,stat,caster.getId()+"",target.getId()+","+jet+","+-1);
            break;
          case 611://Chatiment (acncien)
        	  if(idspell == 435)
        		  break;
            stat=buff.getValue();
            jet=Formulas.getRandomJet(buff.getJet());
            target.addBuff(stat,jet,-1,-1,false,buff.getSpell(),buff.getArgs(),caster,true);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,stat,caster.getId()+"",target.getId()+","+jet+","+-1);
            break;
          case 788://Chatiments
        	  if(idspell == 435)
        		  break;
            int taux=(caster.getPersonnage()==null ? 1 : 2),gain=finalDommage/taux,max=0;
            stat=buff.getValue();

            try
            {
              max=Integer.parseInt(buff.getArgs().split(";")[1]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
              continue;
            }

            //on retire au max possible la valeur dÃ©jÃ  gagnÃ© sur le chati
            int oldValue=(target.getChatiValue().get(stat)==null ? 0 : target.getChatiValue().get(stat));
            max-=oldValue;
            //Si gain trop grand, on le reduit au max
            if(gain>max)
              gain=max;
            //On met a jour les valeurs des chatis
            int newValue=oldValue+gain;

            if(stat==125)
            {
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,stat,caster.getId()+"",target.getId()+","+gain+","+5);
              target.setPdv(target.getPdv()+gain);
              if(target.getPersonnage()!=null)
                SocketManager.GAME_SEND_STATS_PACKET(target.getPersonnage());
            }
            else
            {
              target.getChatiValue().put(stat,newValue);
              target.addBuff(stat,gain,5,1,false,buff.getSpell(),buff.getArgs(),caster,true);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,stat,caster.getId()+"",target.getId()+","+gain+","+5);
            }
            target.getChatiValue().put(stat,newValue);
            break;
          case 1017: //Reinforcementex (Yokai)
            target.addBuff(138,buff.getValue(),-1,-1,false,buff.getSpell(),buff.getArgs(),caster,false);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,138,caster.getId()+"",target.getId()+","+buff.getValue()+","+buff.getDuration());
            break;
          case 1026: //Moowolf's Rage (+x damage for every hit taken)
            target.addBuff(112,buff.getValue(),buff.getDuration(),buff.getTurn(),true,buff.getSpell(),buff.getArgs(),caster,false);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,112,caster.getId()+"",target.getId()+","+5+","+buff.getDuration());
          default:
            break;
        }
      }
    }

    return finalDommage;
  }

  private void applyOnHealBuffs(int heal, Fighter target, Fighter caster, Fight fight)
  {
    for(int id : Constant.ON_HEAL_BUFFS)
    {
      for(SpellEffect buff : target.getBuffsByEffectID(id))
      {
        switch(id)
        {
          case 1018: //earth health% damage
          {
            if(target.hasBuff(765))//sacrifice
            {
              if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
              {
                applyEffect_765B(fight,target);
                target=target.getBuff(765).getCaster();
              }
            }
            //si la cible a le buff renvoie de sort
            if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
            {
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
              //le lanceur devient donc la cible
              target=caster;
            }
            int dmg=0;
            String[] args=buff.getArgs().split(";");
            if(target.hasBuff(782)) //Brokle
            {
              dmg=Formulas.getMaxJet(args[5]);
            }
            else if(target.hasBuff(781)) //Jinx
            {
              dmg=Formulas.getMinJet(args[5]);
            }
            else
              dmg=Formulas.getRandomJet(args[5]);//%age de pdv infligï¿½

            int val=(int)Math.floor(((double)caster.getPdv()/100)*dmg);//Valeur des dï¿½gats
            int armor=Formulas.getArmorResist(target,Constant.ELEMENT_TERRE);
            if(!target.hasBuff(786))
            {
              val-=armor;
              if(val<0)
                val=0;
              if(armor>0)
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+armor);
            }
            int finalDommage=applyOnHitBuffs(val,target,caster,fight,Constant.ELEMENT_TERRE,spell);
            if(finalDommage>target.getPdv())
              finalDommage=target.getPdv();//Target va mourrir
            target.removePdv(caster,finalDommage);
            target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
            int cura=finalDommage;
            if(target.hasBuff(786))
            {
              if((cura+caster.getPdv())>caster.getPdvMax())
                cura=caster.getPdvMax()-caster.getPdv();
              caster.removePdv(caster,-cura);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
            }
            finalDommage=-(finalDommage);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
            if(target.getMob()!=null)
              verifmobs(fight,target,97,cura);
            if(target.getPdv()<=0)
            {
              fight.onFighterDie(target,caster);
              if(target.canPlay()&&target.getPersonnage()!=null)
                fight.endTurn(false);
              else if(target.canPlay())
                target.setCanPlay(false);
            }
            break;
          }
          case 1019: //fire health% damage
          {
            if(target.hasBuff(765))//sacrifice
            {
              if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
              {
                applyEffect_765B(fight,target);
                target=target.getBuff(765).getCaster();
              }
            }
            //si la cible a le buff renvoie de sort
            if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
            {
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
              //le lanceur devient donc la cible
              target=caster;
            }
            int dmg=0;
            String[] args=buff.getArgs().split(";");
            if(target.hasBuff(782)) //Brokle
            {
              dmg=Formulas.getMaxJet(args[5]);
            }
            else if(target.hasBuff(781)) //Jinx
            {
              dmg=Formulas.getMinJet(args[5]);
            }
            else
              dmg=Formulas.getRandomJet(args[5]);//%age de pdv infligï¿½

            int val=(int)Math.floor(((double)caster.getPdv()/100)*dmg);//Valeur des dï¿½gats
            int armor=Formulas.getArmorResist(target,Constant.ELEMENT_FEU);
            if(!target.hasBuff(786))
            {
              val-=armor;
              if(val<0)
                val=0;
              if(armor>0)
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+armor);
            }
            int finalDommage=applyOnHitBuffs(val,target,caster,fight,Constant.ELEMENT_FEU,spell);
            if(finalDommage>target.getPdv())
              finalDommage=target.getPdv();//Target va mourrir
            target.removePdv(caster,finalDommage);
            target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
            int cura=finalDommage;
            if(target.hasBuff(786))
            {
              if((cura+caster.getPdv())>caster.getPdvMax())
                cura=caster.getPdvMax()-caster.getPdv();
              caster.removePdv(caster,-cura);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
            }
            finalDommage=-(finalDommage);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
            if(target.getMob()!=null)
              verifmobs(fight,target,97,cura);
            if(target.getPdv()<=0)
            {
              fight.onFighterDie(target,caster);
              if(target.canPlay()&&target.getPersonnage()!=null)
                fight.endTurn(false);
              else if(target.canPlay())
                target.setCanPlay(false);
            }
            break;
          }
          case 1020: //water health% damage
          {
            if(target.hasBuff(765))//sacrifice
            {
              if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
              {
                applyEffect_765B(fight,target);
                target=target.getBuff(765).getCaster();
              }
            }
            //si la cible a le buff renvoie de sort
            if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
            {
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
              //le lanceur devient donc la cible
              target=caster;
            }
            int dmg=0;
            String[] args=buff.getArgs().split(";");
            if(target.hasBuff(782)) //Brokle
            {
              dmg=Formulas.getMaxJet(args[5]);
            }
            else if(target.hasBuff(781)) //Jinx
            {
              dmg=Formulas.getMinJet(args[5]);
            }
            else
              dmg=Formulas.getRandomJet(args[5]);//%age de pdv infligï¿½

            int val=(int)Math.floor(((double)caster.getPdv()/100)*dmg);//Valeur des dï¿½gats
            int armor=Formulas.getArmorResist(target,Constant.ELEMENT_EAU);
            if(!target.hasBuff(786))
            {
              val-=armor;
              if(val<0)
                val=0;
              if(armor>0)
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+armor);
            }
            int finalDommage=applyOnHitBuffs(val,target,caster,fight,Constant.ELEMENT_EAU,spell);
            if(finalDommage>target.getPdv())
              finalDommage=target.getPdv();//Target va mourrir
            target.removePdv(caster,finalDommage);
            target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
            int cura=finalDommage;
            if(target.hasBuff(786))
            {
              if((cura+caster.getPdv())>caster.getPdvMax())
                cura=caster.getPdvMax()-caster.getPdv();
              caster.removePdv(caster,-cura);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
            }
            finalDommage=-(finalDommage);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
            if(target.getMob()!=null)
              verifmobs(fight,target,97,cura);
            if(target.getPdv()<=0)
            {
              fight.onFighterDie(target,caster);
              if(target.canPlay()&&target.getPersonnage()!=null)
                fight.endTurn(false);
              else if(target.canPlay())
                target.setCanPlay(false);
            }
            break;
          }
          case 1021: //air health% damage
          {
            if(target.hasBuff(765))//sacrifice
            {
              if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
              {
                applyEffect_765B(fight,target);
                target=target.getBuff(765).getCaster();
              }
            }
            //si la cible a le buff renvoie de sort
            if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
            {
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
              //le lanceur devient donc la cible
              target=caster;
            }
            int dmg=0;
            String[] args=buff.getArgs().split(";");
            if(target.hasBuff(782)) //Brokle
            {
              dmg=Formulas.getMaxJet(args[5]);
            }
            else if(target.hasBuff(781)) //Jinx
            {
              dmg=Formulas.getMinJet(args[5]);
            }
            else
              dmg=Formulas.getRandomJet(args[5]);//%age de pdv infligï¿½

            int val=(int)Math.floor(((double)caster.getPdv()/100)*dmg);//Valeur des dï¿½gats
            int armor=Formulas.getArmorResist(target,Constant.ELEMENT_AIR);
            if(!target.hasBuff(786))
            {
              val-=armor;
              if(val<0)
                val=0;
              if(armor>0)
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+armor);
            }
            int finalDommage=applyOnHitBuffs(val,target,caster,fight,Constant.ELEMENT_AIR,spell);
            if(finalDommage>target.getPdv())
              finalDommage=target.getPdv();//Target va mourrir
            target.removePdv(caster,finalDommage);
            target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
            int cura=finalDommage;
            if(target.hasBuff(786))
            {
              if((cura+caster.getPdv())>caster.getPdvMax())
                cura=caster.getPdvMax()-caster.getPdv();
              caster.removePdv(caster,-cura);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
            }
            finalDommage=-(finalDommage);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
            if(target.getMob()!=null)
              verifmobs(fight,target,97,cura);
            if(target.getPdv()<=0)
            {
              fight.onFighterDie(target,caster);
              if(target.canPlay()&&target.getPersonnage()!=null)
                fight.endTurn(false);
              else if(target.canPlay())
                target.setCanPlay(false);
            }
            break;
          }
          case 1022: //earth damage
          {
            if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
              continue; // Les monstres de s'entretuent pas

            if(target.hasBuff(765))//sacrifice
            {
              if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
              {
                applyEffect_765B(fight,target);
                target=target.getBuff(765).getCaster();
              }
            }
            //si la cible a le buff renvoie de sort

            if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
            {
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
              //le lanceur devient donc la cible
              target=caster;
            }
            int dmg=0;
            String[] args=buff.getArgs().split(";");
            if(target.hasBuff(782)) //Brokle
            {
              dmg=Formulas.getMaxJet(args[5]);
            }
            else if(target.hasBuff(781)) //Jinx
            {
              dmg=Formulas.getMinJet(args[5]);
            }
            else
              dmg=Formulas.getRandomJet(args[5]);

            //Si le sort est boostï¿½ par un buff spï¿½cifique
            if(caster.hasBuff(293)||caster.haveState(300))
            {
              if(caster.haveState(300))
                caster.setState(300,0,caster.getId());
              for(SpellEffect SE : caster.getBuffsByEffectID(293))
              {
                if(SE==null)
                  continue;
                if(SE.getValue()==spell)
                {
                  int add=-1;
                  try
                  {
                    add=Integer.parseInt(SE.getArgs().split(";")[2]);
                  }
                  catch(Exception e)
                  {
                    e.printStackTrace();
                  }
                  if(add<=0)
                    continue;
                  dmg+=add;
                }
              }
            }

            int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_TERRE,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);
            finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_TERRE,spell);//S'il y a des buffs spï¿½ciaux
            if(finalDommage>target.getPdv())
              finalDommage=target.getPdv();//Target va mourrir
            target.removePdv(caster,finalDommage);
            target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
            int cura=finalDommage;

            if(target.hasBuff(786))
            {
              if((cura+caster.getPdv())>caster.getPdvMax())
                cura=caster.getPdvMax()-caster.getPdv();
              caster.removePdv(caster,-cura);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
            }
            finalDommage=-(finalDommage);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
            if(target.getMob()!=null)
              verifmobs(fight,target,97,cura);
            if(target.getPdv()<=0)
            {
              fight.onFighterDie(target,caster);
              if(target.canPlay()&&target.getPersonnage()!=null)
                fight.endTurn(false);
              else if(target.canPlay())
                target.setCanPlay(false);
            }
            break;
          }
          case 1023: //fire damage
          {
            if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
              continue; // Les monstres de s'entretuent pas

            if(target.hasBuff(765))//sacrifice
            {
              if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
              {
                applyEffect_765B(fight,target);
                target=target.getBuff(765).getCaster();
              }
            }
            //si la cible a le buff renvoie de sort

            if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
            {
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
              //le lanceur devient donc la cible
              target=caster;
            }
            int dmg=0;
            String[] args=buff.getArgs().split(";");
            if(target.hasBuff(782)) //Brokle
            {
              dmg=Formulas.getMaxJet(args[5]);
            }
            else if(target.hasBuff(781)) //Jinx
            {
              dmg=Formulas.getMinJet(args[5]);
            }
            else
              dmg=Formulas.getRandomJet(args[5]);

            //Si le sort est boostï¿½ par un buff spï¿½cifique
            if(caster.hasBuff(293)||caster.haveState(300))
            {
              if(caster.haveState(300))
                caster.setState(300,0,caster.getId());
              for(SpellEffect SE : caster.getBuffsByEffectID(293))
              {
                if(SE==null)
                  continue;
                if(SE.getValue()==spell)
                {
                  int add=-1;
                  try
                  {
                    add=Integer.parseInt(SE.getArgs().split(";")[2]);
                  }
                  catch(Exception e)
                  {
                    e.printStackTrace();
                  }
                  if(add<=0)
                    continue;
                  dmg+=add;
                }
              }
            }

            int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_FEU,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);
            finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_FEU,spell);//S'il y a des buffs spï¿½ciaux
            if(finalDommage>target.getPdv())
              finalDommage=target.getPdv();//Target va mourrir
            target.removePdv(caster,finalDommage);
            target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
            int cura=finalDommage;

            if(target.hasBuff(786))
            {
              if((cura+caster.getPdv())>caster.getPdvMax())
                cura=caster.getPdvMax()-caster.getPdv();
              caster.removePdv(caster,-cura);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
            }
            finalDommage=-(finalDommage);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
            if(target.getMob()!=null)
              verifmobs(fight,target,97,cura);
            if(target.getPdv()<=0)
            {
              fight.onFighterDie(target,caster);
              if(target.canPlay()&&target.getPersonnage()!=null)
                fight.endTurn(false);
              else if(target.canPlay())
                target.setCanPlay(false);
            }
            break;
          }
          case 1024: //water damage
          {
            if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
              continue; // Les monstres de s'entretuent pas

            if(target.hasBuff(765))//sacrifice
            {
              if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
              {
                applyEffect_765B(fight,target);
                target=target.getBuff(765).getCaster();
              }
            }
            //si la cible a le buff renvoie de sort

            if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
            {
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
              //le lanceur devient donc la cible
              target=caster;
            }
            int dmg=0;
            String[] args=buff.getArgs().split(";");
            if(target.hasBuff(782)) //Brokle
            {
              dmg=Formulas.getMaxJet(args[5]);
            }
            else if(target.hasBuff(781)) //Jinx
            {
              dmg=Formulas.getMinJet(args[5]);
            }
            else
              dmg=Formulas.getRandomJet(args[5]);

            //Si le sort est boostï¿½ par un buff spï¿½cifique
            if(caster.hasBuff(293)||caster.haveState(300))
            {
              if(caster.haveState(300))
                caster.setState(300,0,caster.getId());
              for(SpellEffect SE : caster.getBuffsByEffectID(293))
              {
                if(SE==null)
                  continue;
                if(SE.getValue()==spell)
                {
                  int add=-1;
                  try
                  {
                    add=Integer.parseInt(SE.getArgs().split(";")[2]);
                  }
                  catch(Exception e)
                  {
                    e.printStackTrace();
                  }
                  if(add<=0)
                    continue;
                  dmg+=add;
                }
              }
            }

            int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_EAU,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);
            finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_EAU,spell);//S'il y a des buffs spï¿½ciaux
            if(finalDommage>target.getPdv())
              finalDommage=target.getPdv();//Target va mourrir
            target.removePdv(caster,finalDommage);
            target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
            int cura=finalDommage;

            if(target.hasBuff(786))
            {
              if((cura+caster.getPdv())>caster.getPdvMax())
                cura=caster.getPdvMax()-caster.getPdv();
              caster.removePdv(caster,-cura);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
            }
            finalDommage=-(finalDommage);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
            if(target.getMob()!=null)
              verifmobs(fight,target,97,cura);
            if(target.getPdv()<=0)
            {
              fight.onFighterDie(target,caster);
              if(target.canPlay()&&target.getPersonnage()!=null)
                fight.endTurn(false);
              else if(target.canPlay())
                target.setCanPlay(false);
            }
            break;
          }
          case 1025: //air damage
          {
            if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
              continue; // Les monstres de s'entretuent pas

            if(target.hasBuff(765))//sacrifice
            {
              if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
              {
                applyEffect_765B(fight,target);
                target=target.getBuff(765).getCaster();
              }
            }
            //si la cible a le buff renvoie de sort

            if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
            {
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
              //le lanceur devient donc la cible
              target=caster;
            }
            int dmg=0;
            String[] args=buff.getArgs().split(";");
            if(target.hasBuff(782)) //Brokle
            {
              dmg=Formulas.getMaxJet(args[5]);
            }
            else if(target.hasBuff(781)) //Jinx
            {
              dmg=Formulas.getMinJet(args[5]);
            }
            else
              dmg=Formulas.getRandomJet(args[5]);

            //Si le sort est boostï¿½ par un buff spï¿½cifique
            if(caster.hasBuff(293)||caster.haveState(300))
            {
              if(caster.haveState(300))
                caster.setState(300,0,caster.getId());
              for(SpellEffect SE : caster.getBuffsByEffectID(293))
              {
                if(SE==null)
                  continue;
                if(SE.getValue()==spell)
                {
                  int add=-1;
                  try
                  {
                    add=Integer.parseInt(SE.getArgs().split(";")[2]);
                  }
                  catch(Exception e)
                  {
                    e.printStackTrace();
                  }
                  if(add<=0)
                    continue;
                  dmg+=add;
                }
              }
            }

            int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_AIR,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);
            finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_AIR,spell);//S'il y a des buffs spï¿½ciaux
            if(finalDommage>target.getPdv())
              finalDommage=target.getPdv();//Target va mourrir
            target.removePdv(caster,finalDommage);
            target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
            int cura=finalDommage;

            if(target.hasBuff(786))
            {
              if((cura+caster.getPdv())>caster.getPdvMax())
                cura=caster.getPdvMax()-caster.getPdv();
              caster.removePdv(caster,-cura);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
            }
            finalDommage=-(finalDommage);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
            if(target.getMob()!=null)
              verifmobs(fight,target,97,cura);
            if(target.getPdv()<=0)
            {
              fight.onFighterDie(target,caster);
              if(target.canPlay()&&target.getPersonnage()!=null)
                fight.endTurn(false);
              else if(target.canPlay())
                target.setCanPlay(false);
            }
            break;
          }
          default:
            break;
        }
      }
    }
  }

  public int getDuration()
  {
    return duration;
  }

  public int getTurn()
  {
    return turns;
  }

  public void setTurn(int turn)
  {
    this.turns=turn;
  }

  public boolean isDebuffable()
  {
    return debuffable;
  }

  public int getEffectID()
  {
    return effectID;
  }

  public void setEffectID(int id)
  {
    effectID=id;
  }

  public String getJet()
  {
    return jet;
  }

  public int getValue()
  {
    return value;
  }

  public void setValue(int i)
  {
    value=i;
  }

  public int getChance()
  {
    return chance;
  }

  public String getArgs()
  {
    return args;
  }

  public void setArgs(String newArgs)
  {
    args=newArgs;
  }

  public int getMaxMinSpell(Fighter fighter, int value)
  {
    int val=value;
    if(fighter.hasBuff(782))
    {
      int max=Integer.parseInt(args.split(";")[1]);
      if(max==-1)
        max=Integer.parseInt(args.split(";")[0]);
      val=max;
    }
    else if(fighter.hasBuff(781))
      val=Integer.parseInt(args.split(";")[0]);
    return val;
  }

  public int decrementDuration()
  {
    duration-=1;
    return duration;
  }

  //v2.2 - Mot Olof fix
  public void applyBeginingBuff(Fight fight, Fighter fighter)
  {
    ArrayList<Fighter> targets=new ArrayList<>();
    targets.add(fighter);
    this.turns=-1;
    if(this.spell==1679) //Mot Olof
    {
      int mapWidth=fight.getMap().getW();
      int casterCell=fighter.getCell().getId();
      int upLeft=casterCell-mapWidth;
      int upRight=casterCell-mapWidth-1;
      int downLeft=casterCell+mapWidth-1;
      int downRight=casterCell+mapWidth;
      if(fight.getMap().getCase(upLeft).getFirstFighter()!=null)
        targets.add(fight.getMap().getCase(upLeft).getFirstFighter());
      if(fight.getMap().getCase(upRight).getFirstFighter()!=null)
        targets.add(fight.getMap().getCase(upRight).getFirstFighter());
      if(fight.getMap().getCase(downLeft).getFirstFighter()!=null)
        targets.add(fight.getMap().getCase(downLeft).getFirstFighter());
      if(fight.getMap().getCase(downRight).getFirstFighter()!=null)
        targets.add(fight.getMap().getCase(downRight).getFirstFighter());
      this.setEffectID(1003);
      this.setArgs("33;-1;-1;0;0;0d0+33");
    }
    this.applyToFight(fight,this.caster,targets,false);
  }

  public void applyToFight(Fight fight, Fighter perso, GameCase Cell, ArrayList<Fighter> cibles, boolean aoe, boolean isTrap)
  {
    cell=Cell;
    this.aoe=aoe;
    this.isTrap=isTrap;
    applyToFight(fight,perso,cibles,false);
  }

  private int getDurationFixed()
  {
    return this.durationFixed;
  }

  public Fighter getCaster()
  {
    return caster;
  }

  public int getSpell()
  {
    return spell;
  }

  public void applyToFight(Fight fight, Fighter acaster, ArrayList<Fighter> cibles, boolean isCaC)
  {
    try
    {
      try
      {
        if(turns!=-1) //Si ce n'est pas un buff qu'on applique en dÃ©but de tour
          turns=Integer.parseInt(args.split(";")[3]);
      }
      catch(NumberFormatException ignored)
      {
    	  
      }
      caster=acaster;
      try
      {
        jet=args.split(";")[5];
      }
      catch(Exception ignored)
      {
      }
      if(caster.getPersonnage()!=null)
      {
        Player perso=caster.getPersonnage();
        if(perso.getItemClasseSpell().containsKey(spell))
        {
          int modi=0;
          if(effectID==108)
            modi=perso.getItemClasseModif(spell,284);
          else if(effectID>=91&&effectID<=100)
            modi=perso.getItemClasseModif(spell,283);
          String jeta=jet.split("\\+")[0];
          int bonus=Integer.parseInt(jet.split("\\+")[1])+modi;
          jet=jeta+"+"+bonus;
        }
      }

      switch(effectID)
      {
        case 4://Fuite/Bond du fÃ©lin/ Bond du iop / tÃ©lÃ©port
          applyEffect_4(fight,cibles);
          break;
        case 5://Repousse de X case
          applyEffect_5(cibles,fight);
          break;
        case 6://Attire de X case
          applyEffect_6(cibles,fight);
          break;
        case 8://Echange les place de 2 joueur
          applyEffect_8(cibles,fight);
          break;
        case 9://Esquive une attaque en reculant de 1 case
          applyEffect_9(cibles,fight);
          break;
        case 50://Porter
          applyEffect_50(fight);
          break;
        case 51://jeter
          applyEffect_51(fight);
          break;
        case 77://Vol de PM
          applyEffect_77(cibles,fight);
          break;
        case 78://Bonus PM
          applyEffect_78(cibles,fight);
          break;
        case 79: //+ X chance(%) dommage subis * Y sinon soignÃ© de dommage *Z
          applyEffect_79(cibles,fight);
          break;
        case 81://Cura, PDV devueltos
          applyEffect_81(cibles,fight);
          break;
        case 82://Vol de Vie fixe
          applyEffect_82(cibles,fight);
          break;
        case 84://Vol de PA
          applyEffect_84(cibles,fight);
          break;
        case 85://Dommage Eau %vie
          applyEffect_85(cibles,fight);
          break;
        case 86://Dommage Terre %vie
          applyEffect_86(cibles,fight);
          break;
        case 87://Dommage Air %vie
          applyEffect_87(cibles,fight);
          break;
        case 88://Dommage feu %vie
          applyEffect_88(cibles,fight);
          break;
        case 89://Dommage neutre %vie
          applyEffect_89(cibles,fight);
          break;
        case 90://Donne X% de sa vie
          applyEffect_90(cibles,fight);
          break;
        case 91://Vol de Vie Eau
          applyEffect_91(cibles,fight,isCaC);
          break;
        case 92://Vol de Vie Terre
          applyEffect_92(cibles,fight,isCaC);
          break;
        case 93://Vol de Vie Air
          applyEffect_93(cibles,fight,isCaC);
          break;
        case 94://Vol de Vie feu
          applyEffect_94(cibles,fight,isCaC);
          break;
        case 95://Vol de Vie neutre
          applyEffect_95(cibles,fight,isCaC);
          break;
        case 96://Dommage Eau
          applyEffect_96(cibles,fight,isCaC);
          break;
        case 97://Dommage Terre
          applyEffect_97(cibles,fight,isCaC);
          break;
        case 98://Dommage Air
          applyEffect_98(cibles,fight,isCaC);
          break;
        case 99://Dommage feu
          applyEffect_99(cibles,fight,isCaC);
          break;
        case 100://Dommage neutre
          applyEffect_100(cibles,fight,isCaC);
          break;
        case 101://Retrait PA
          applyEffect_101(cibles,fight);
          break;
        case 105://Dommages rÃ©duits de X
          applyEffect_105(cibles,fight);
          break;
        case 106://Renvoie de sort
          applyEffect_106(cibles,fight);
          break;
        case 107://Renvoie de dom
          applyEffect_107(cibles,fight);
          break;
        case 108://Soin
          applyEffect_108(cibles,fight,isCaC);
          break;
        case 109://Dommage pour le lanceur
          applyEffect_109(fight);
          break;
        case 110://+ X vie
          applyEffect_110(cibles,fight);
          break;
        case 111://+ X PA
          applyEffect_111(cibles,fight);
          break;
        case 112://+Dom
          applyEffect_112(cibles,fight);
          break;
        case 114://Multiplie les dommages par X
          applyEffect_114(cibles,fight);
          break;
        case 115://+Cc
          applyEffect_115(cibles,fight);
          break;
        case 116://Malus PO
          applyEffect_116(cibles,fight);
          break;
        case 117://Bonus PO
          applyEffect_117(cibles,fight);
          break;
        case 118://Bonus force
          applyEffect_118(cibles,fight);
          break;
        case 119://Bonus AgilitÃ©
          applyEffect_119(cibles,fight);
          break;
        case 120://Bonus PA
          applyEffect_120(cibles,fight);
          break;
        case 121://+Dom
          applyEffect_121(cibles,fight);
          break;
        case 122://+EC
          applyEffect_122(cibles,fight);
          break;
        case 123://+Chance
          applyEffect_123(cibles,fight);
          break;
        case 124://+Sagesse
          applyEffect_124(cibles,fight);
          break;
        case 125://+VitalitÃ©
          applyEffect_125(cibles,fight);
          break;
        case 126://+Intelligence
          applyEffect_126(cibles,fight);
          break;
        case 127://Retrait PM
          applyEffect_127(cibles,fight);
          break;
        case 128://+PM
          applyEffect_128(cibles,fight);
          break;
        case 130://Vol de kamas
          applyEffect_130(fight,cibles);
          break;
        case 131://Poison : X Pdv  par PA
          applyEffect_131(cibles,fight);
          break;
        case 132://Enleve les envoutements
          applyEffect_132(cibles,fight);
          break;
        case 138://%dom
          applyEffect_138(cibles,fight);
          break;
        case 140://Passer le tour
          applyEffect_140(cibles,fight);
          break;
        case 141://Tue la cible
          applyEffect_141(fight,cibles);
          break;
        case 142://Dommages physique
          applyEffect_142(fight,cibles);
          break;
        case 143:// PDV rendu
          applyEffect_143(cibles,fight);
          break;
        case 144:// - Dommages (pas bostÃ©)
          applyEffect_144(fight,cibles);
        case 145://Malus Dommage
          applyEffect_145(fight,cibles);
          break;
        case 149://Change l'apparence
          applyEffect_149(fight,cibles);
          break;
        case 150://InvisibilitÃ©
          applyEffect_150(fight,cibles);
          break;
        case 152:// - Chance
          applyEffect_152(fight,cibles);
          break;
        case 153:// - Vita
          applyEffect_153(fight,cibles);
          break;
        case 154:// - Agi
          applyEffect_154(fight,cibles);
          break;
        case 155:// - Intel
          applyEffect_155(fight,cibles);
          break;
        case 156:// - Sagesse
          applyEffect_156(fight,cibles);
          break;
        case 157:// - Force
          applyEffect_157(fight,cibles);
          break;
        case 160:// + Esquive PA
          applyEffect_160(fight,cibles);
          break;
        case 161:// + Esquive PM
          applyEffect_161(fight,cibles);
          break;
        case 162:// - Esquive PA
          applyEffect_162(fight,cibles);
          break;
        case 163:// - Esquive PM
          applyEffect_163(fight,cibles);
          break;
        case 164:// DaÃ±os reducidos en x%
          applyEffect_164(cibles,fight);
          break;
        case 165:// MaÃ®trises
          applyEffect_165(fight,cibles);
          break;
        case 168://Perte PA non esquivable
          applyEffect_168(cibles,fight);
          break;
        case 169://Perte PM non esquivable
          applyEffect_169(cibles,fight);
          break;
        case 171://Malus CC
          applyEffect_171(fight,cibles);
          break;
        case 176:// + prospection
          applyEffect_176(cibles,fight);
          break;
        case 177:// - prospection
          applyEffect_177(cibles,fight);
          break;
        case 178:// + soin
          applyEffect_178(cibles,fight);
          break;
        case 179:// - soin
          applyEffect_179(cibles,fight);
          break;
        case 180://Double du sram
          applyEffect_180(fight);
          break;
        case 181://Invoque une crÃ©ature
          applyEffect_181(fight);
          break;
        case 182://+ Crea Invoc
          applyEffect_182(fight,cibles);
          break;
        case 183://Resist Magique
          applyEffect_183(fight,cibles);
          break;
        case 184://Resist Physique
          applyEffect_184(fight,cibles);
          break;
        case 185://Invoque une creature statique
          applyEffect_185(fight);
          break;
        case 186://Diminue les dommages %
          applyEffect_186(fight,cibles);
          break;
        case 202://Perception
          applyEffect_202(fight,cibles);
          break;
        case 210://Resist % terre
          applyEffect_210(fight,cibles);
          break;
        case 211://Resist % eau
          applyEffect_211(fight,cibles);
          break;
        case 212://Resist % air
          applyEffect_212(fight,cibles);
          break;
        case 213://Resist % feu
          applyEffect_213(fight,cibles);
          break;
        case 214://Resist % neutre
          applyEffect_214(fight,cibles);
          break;
        case 215://Faiblesse % terre
          applyEffect_215(fight,cibles);
          break;
        case 216://Faiblesse % eau
          applyEffect_216(fight,cibles);
          break;
        case 217://Faiblesse % air
          applyEffect_217(fight,cibles);
          break;
        case 218://Faiblesse % feu
          applyEffect_218(fight,cibles);
          break;
        case 219://Faiblesse % neutre
          applyEffect_219(fight,cibles);
          break;
        case 220:// Renvoie dommage
          applyEffect_220(cibles,fight);
          break;
        case 240://+ earth resist
          applyEffect_240(fight,cibles);
          break;
        case 241://+ water resist
          applyEffect_241(fight,cibles);
          break;
        case 242://+ air resist
          applyEffect_242(fight,cibles);
          break;
        case 243://+ fire resist
          applyEffect_243(fight,cibles);
          break;
        case 244://+ neutral resist
          applyEffect_244(fight,cibles);
          break;
        case 265://Reduit les Dom de X
          applyEffect_265(fight,cibles);
          break;
        case 266://Vol Chance
          applyEffect_266(fight,cibles);
          break;
        case 267://Vol vitalitÃ©
          applyEffect_267(fight,cibles);
          break;
        case 268://Vol agitlitÃ©
          applyEffect_268(fight,cibles);
          break;
        case 269://Vol intell
          applyEffect_269(fight,cibles);
          break;
        case 270://Vol sagesse
          applyEffect_270(fight,cibles);
          break;
        case 271://Vol force
          applyEffect_271(fight,cibles);
          break;
        case 293://Augmente les dÃ©gÃ¢ts de base du sort X de Y
          applyEffect_293(fight);
          break;
        case 320://Vol de PO
          applyEffect_320(fight,cibles);
          break;
        case 400://CrÃ©er un  piÃ¨ge
          applyEffect_400(fight);
          break;
        case 401://CrÃ©er une glyphe
          applyEffect_401(fight);
          break;
        case 402://Glyphe des Blop
          applyEffect_402(fight);
          break;
        /*case 606://Ancien chati
        case 607:
        case 608:
        case 609:
        case 611:
        	applyEffect_606To611(cibles, fight);
        	break;*/
        case 666://Pas d'effet complÃ©mentaire
          break;
        case 671://Dommages: X% de la vie de l'attaquant (neutre)
          applyEffect_671(cibles,fight);
          break;
        case 672://Dommages: X% de la vie de l'attaquant (neutre)
          applyEffect_672(cibles,fight);
          break;
        case 765://Sacrifice
          applyEffect_765(cibles,fight);
          break;
        case 776://Enleve %vita pendant l'attaque
          applyEffect_776(cibles,fight);
          break;
        case 780://laisse spirituelle
          applyEffect_780(fight);
          break;
        case 781://Minimize les effets alÃ©atoires
          applyEffect_781(cibles,fight);
          break;
        case 782://Maximise les effets alÃ©atoires
          applyEffect_782(cibles,fight);
          break;
        case 783://Pousse jusqu'a la case visÃ©
          applyEffect_783(cibles,fight);
          break;
        case 784://Raulebaque
          applyEffect_784(cibles,fight);
          break;
        case 786://Soin pendant l'attaque
          applyEffect_786(cibles,fight);
          break;
        case 787://Change etat
          applyEffect_787(cibles,fight);
          break;
        case 788://Chatiment de X sur Y tours
          applyEffect_788(cibles,fight);
          break;
        case 950://Etat X
          applyEffect_950(fight,cibles);
          break;
        case 951://Enleve l'Etat X
          applyEffect_951(fight,cibles);
          break;
        case 1000: //Even Glyph
          applyEffect_1000(fight,cibles);
          break;
        case 1001: //Odd Glyph
          applyEffect_1001(fight,cibles);
          break;
        case 1002: //Glyph kaskargo
          applyEffect_1002(fight,cibles);
          break;
        case 1003: //Mot Olov 33% neutral damage
          applyEffect_1003(cibles,fight);
          break;
        case 1008: //% hp vitality bonus
          applyEffect_1008(cibles,fight);
          break;
        case 1011: //+% erosion resistance
          applyEffect_1011(cibles,fight);
          break;
        case 1012: //-% erosion resistance
          applyEffect_1012(cibles,fight);
          break;
        case 1013: //attracts caster to target
          applyEffect_1013(cibles,fight);
          break;
        case 1014: //water damage heals allies around target equal to damage done
          applyEffect_1014(cibles,fight);
          break;
        case 1015: //earth damage +5 base damage for every mp caster has
          applyEffect_1015(cibles,fight);
          break;
        case 1016: //new time theft
          applyEffect_1016(cibles,fight);
          break;
        case 1017://Reinforcementex
          applyEffect_1017(cibles,fight);
          break;
        case 1018://Earth damage % HP when healed
          applyEffect_1018(cibles,fight);
          break;
        case 1019://Fire damage % HP when healed
          applyEffect_1019(cibles,fight);
          break;
        case 1020://Water damage % HP when healed
          applyEffect_1020(cibles,fight);
          break;
        case 1021://Air damage % HP when healed
          applyEffect_1021(cibles,fight);
          break;
        case 1022://Earth damage when healed
          applyEffect_1022(cibles,fight);
          break;
        case 1023://Fire damage when healed
          applyEffect_1023(cibles,fight);
          break;
        case 1024://Water damage when healed
          applyEffect_1024(cibles,fight);
          break;
        case 1025://Air damage when healed
          applyEffect_1025(cibles,fight);
          break;
        case 1026: //+ Damage on hit (Moowolf)
          applyEffect_1026(cibles,fight);
          break;
        case 1027: //Earth damage 50% chance to hit self
          applyEffect_1027(cibles,fight,isCaC);
          break;
        case 1028: //AP bonus if hitting target
          applyEffect_1028(cibles,fight);
          break;
        case 1029: //Extra damage received by glyphs and traps
          applyEffect_1029(cibles,fight);
          break;
        case 1030: //Teleglyph
          applyEffect_1030(cibles,fight);
          break;
        default:
          break;
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  private void applyEffect_4(Fight fight, ArrayList<Fighter> cibles)
  {
    if(turns>1)
      return; //Olol bondir 3 tours apres ?
   if(!caster.haveState(7))
    if(cell.isWalkable(false)&&!fight.isOccuped(cell.getId()))//Si la case est prise, on va ï¿½viter que les joueurs se montent dessus *-*
    {
      caster.getCell().getFighters().clear();
      caster.setCell(cell);
      caster.getCell().addFighter(caster);

      /* ArrayList<Trap> P=(new ArrayList<Trap>());
      P.addAll(fight.getAllTraps());
      for(Trap p : P)
      {
        int dist=PathFinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),caster.getCell().getId());
        //on active le piege
        if(dist<=p.getSize())
          p.onTraped(caster);
      }*/

      verifyTraps(fight,caster);

      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,4,caster.getId()+"",caster.getId()+","+cell.getId());
    }
  }

  private void applyEffect_5(ArrayList<Fighter> cibles, Fight fight)
  {
    if(cibles.size()==1&&spell==120||spell==310)
      if(!cibles.get(0).isDead())
        caster.setOldCible(cibles.get(0));

    if(turns<=0)
    {
      switch(spell)
      {
        case 73://PiÃ©ge rÃ©pulsif
        case 418://FlÃ©che de dispersion
        case 151://Soufle
        case 165://FlÃ¨che enflammÃ©
          cibles=this.trierCibles(cibles,fight);
          break;
      }

      for(Fighter target : cibles)
      {
        boolean next=false;
        if(target.getMob()!=null)
          for(int i : Constant.STATIC_INVOCATIONS)
            if(i==target.getMob().getTemplate().getId())
              next=true;

        if(target.haveState(6)||next)
          continue;

        GameCase cell=this.cell;

        if(target.getCell().getId()==this.cell.getId()||spell==73)
          cell=caster.getCell();

        int newCellId=PathFinding.newCaseAfterPush(fight,cell,target.getCell(),value,spell==73);
        if(newCellId==0)
          return;
        if(newCellId<0)
        {

          int a=-newCellId;
          int finalDmg=Formulas.pushDamage(-newCellId,(caster.isInvocation() ? caster.getInvocator().getLvl() : caster.getLvl()),caster.getTotalStats().getEffect(Constant.STATS_ADD_PUSH),caster.getTotalStats().getEffect(Constant.STATS_REM_PUSH),target.getTotalStats().getEffect(Constant.STATS_ADD_R_PUSH),target.getTotalStats().getEffect(Constant.STATS_REM_R_PUSH));

          if(finalDmg<1)
            finalDmg=1;
          if(finalDmg>target.getPdv())
            finalDmg=target.getPdv();

          if(target.hasBuff(184))
          {
            finalDmg=finalDmg-target.getBuff(184).getValue();//RÃ©duction physique
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+target.getBuff(184).getValue());
          }
          if(target.hasBuff(105))
          {
            finalDmg=finalDmg-target.getBuff(105).getValue();//Immu
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+target.getBuff(105).getValue());
          }
          if(target.getMob() != null)
          	  if(target.getMob().getTemplate() != null)
          		  if(target.getMob().getTemplate().getId() == 423)
          			  finalDmg = 0;
        			  
          if(finalDmg>0)
          {
            if(finalDmg>200)
              finalDmg=Formulas.getRandomValue(189,211);
            target.removePdv(caster,finalDmg);
            target.removePdvMax((int)Math.floor(finalDmg*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+",-"+finalDmg);
            if(target.getPdv()<=0)
            {
              fight.onFighterDie(target,caster);
              if(target.canPlay()&&target.getPersonnage()!=null)
                fight.endTurn(false);
              else if(target.canPlay())
                target.setCanPlay(false);
              return;
            }
          }
          a=value-a;
          newCellId=PathFinding.newCaseAfterPush(fight,caster.getCell(),target.getCell(),a,spell==73);

          char dir=PathFinding.getDirBetweenTwoCase(cell.getId(),target.getCell().getId(),fight.getMap(),true);
          GameCase nextCase=fight.getMap().getCase(PathFinding.GetCaseIDFromDirrection(target.getCell().getId(),dir,fight.getMap(),true));

          if(nextCase!=null&&nextCase.getFirstFighter()!=null)
          {
            Fighter wallTarget=nextCase.getFirstFighter();
            finalDmg=finalDmg/2;
            if(finalDmg<1)
              finalDmg=1;
            if(finalDmg>0)
            {
              wallTarget.removePdv(caster,finalDmg);
              target.removePdvMax((int)Math.floor(finalDmg*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",wallTarget.getId()+",-"+finalDmg);
              if(wallTarget.getPdv()<=0)
                fight.onFighterDie(wallTarget,caster);
            }
          }

          if(newCellId==0)
            continue;
          if(fight.getMap().getCase(newCellId)==null)
            continue;
        }

        target.getCell().getFighters().clear();
        target.setCell(fight.getMap().getCase(newCellId));
        target.getCell().addFighter(target);

        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,5,caster.getId()+"",target.getId()+","+newCellId);
        verifyTraps(fight,target);
      }
    }
  }

  private void applyEffect_6(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<=0)
    {
      for(Fighter target : cibles)
      {
        if(target.getMob()!=null)
          if(282==target.getMob().getTemplate().getId()||556==target.getMob().getTemplate().getId()||2750==target.getMob().getTemplate().getId()||7000==target.getMob().getTemplate().getId())
            continue;
        if(target.haveState(6))
          continue;
        GameCase eCell=cell;
        //Si meme case
        if(target.getCell().getId()==cell.getId())
        {
          //on prend la cellule caster
          eCell=caster.getCell();
        }
        int newCellID=PathFinding.newCaseAfterPush(fight,eCell,target.getCell(),-value);
        if(newCellID==0)
          continue;

        if(newCellID<0)//S'il a ï¿½tï¿½ bloquï¿½
        {
          int a=-(value+newCellID);
          newCellID=PathFinding.newCaseAfterPush(fight,caster.getCell(),target.getCell(),a);
          if(newCellID==0)
            continue;
          if(fight.getMap().getCase(newCellID)==null)
            continue;
        }

        target.getCell().getFighters().clear();
        target.setCell(fight.getMap().getCase(newCellID));
        target.getCell().addFighter(target);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,5,caster.getId()+"",target.getId()+","+newCellID);

        /* ArrayList<Trap> P=(new ArrayList<Trap>());
        P.addAll(fight.getAllTraps());
        for(Trap p : P)
        {
          int dist=PathFinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),target.getCell().getId());
          //on active le piege
          if(dist<=p.getSize())
            p.onTraped(target);
        }*/

        verifyTraps(fight,target);
      }
    }
  }

  private void applyEffect_8(ArrayList<Fighter> cibles, Fight fight)
  {
    if(cibles.isEmpty())
      return;
    Fighter target=cibles.get(0);
    if(target==null)
      return;//ne devrait pas arriver
    if(target.haveState(6))
      return;//Stabilisation
    switch(spell)
    {
      case 438://Transpo
        //si les 2 joueurs ne sont pas dans la meme team, on ignore
        if(target.getTeam()!=caster.getTeam())
          return;
        break;

      case 445://Coop
        //si les 2 joueurs sont dans la meme team, on ignore
        if(target.getTeam()==caster.getTeam())
          return;
        break;

      case 449://Dï¿½tour
      default:
        break;
    }
    if(target.getMob()!= null)
    	if(target.getMob().getTemplate() != null)
    		if(target.getMob().getTemplate().getId() == 423)
    		return;
    //on enleve les persos des cases
    target.getCell().getFighters().clear();
    caster.getCell().getFighters().clear();
    //on retient les cases
    GameCase exTarget=target.getCell();
    GameCase exCaster=caster.getCell();
    //on ï¿½change les cases
    target.setCell(exCaster);
    caster.setCell(exTarget);
    //on ajoute les fighters aux cases
    target.getCell().addFighter(target);
    caster.getCell().addFighter(caster);
    /*ArrayList<Trap> P=(new ArrayList<Trap>());
    P.addAll(fight.getAllTraps());
    for(Trap p : P)
    {
      int dist=PathFinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),target.getCell().getId());
      int dist2=PathFinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),caster.getCell().getId());
      //on active le piege
      if(dist<=p.getSize())
        p.onTraped(target);
      else if(dist2<=p.getSize())
        p.onTraped(caster);
    }*/

    verifyTraps(fight,caster);
    verifyTraps(fight,target);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,4,caster.getId()+"",target.getId()+","+exCaster.getId());
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,4,caster.getId()+"",caster.getId()+","+exTarget.getId());

  }

  private void applyEffect_9(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
      target.addBuff(effectID,value,turns,1,true,spell,args,caster,true);
  }

  private void applyEffect_50(Fight fight) //Karcham
  {
    Fighter target=cell.getFirstFighter();
    if(target==null||target.isDead()||target.haveState(Constant.ETAT_PORTEUR)||target.haveState(Constant.ETAT_PORTE)||target.haveState(Constant.ETAT_ENRACINE))
      return;
    if(target.getMob()!=null)
      for(int i : Constant.STATIC_INVOCATIONS)
        if(i==target.getMob().getTemplate().getId())
          return;

    target.getCell().getFighters().clear();
    target.setCell(caster.getCell());
    target.setState(Constant.ETAT_PORTE,-1,caster.getId()); //infinite duration
    caster.setState(Constant.ETAT_PORTEUR,-1,caster.getId()); //infinite duration
    target.setHoldedBy(caster);
    caster.setIsHolding(target);
    if(target.haveState(Constant.STATE_SOBER))
    {
      target.setHadSober(true);
      target.setState(Constant.STATE_SOBER,0,caster.getId()); //duration 0, remove state
    }
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,target.getId()+"",target.getId()+","+Constant.ETAT_PORTE+",1");
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",caster.getId()+","+Constant.ETAT_PORTEUR+",1");
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,50,caster.getId()+"",""+target.getId());
    fight.setCurAction("");
  }

  private void applyEffect_51(final Fight fight) //Chamrak
  {
    if(!cell.isWalkable(false)||cell.getFighters().size()>0)
      return;
    Fighter target=caster.getIsHolding();
    if(target==null)
      return;

    if(!target.isDead())
    {
      caster.getCell().removeFighter(target);
      target.setCell(cell);
      target.getCell().addFighter(target);
      target.setState(Constant.ETAT_PORTE,0,caster.getId()); //duration 0, remove state
      target.setHoldedBy(null);
      if(target.getHadSober()==true)
      {
        target.setHadSober(false);
        target.setState(Constant.STATE_SOBER,-1,caster.getId()); //infinite duration
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,target.getId()+"",target.getId()+","+Constant.STATE_SOBER+",1");
      }
    }

    caster.setState(Constant.ETAT_PORTEUR,0,caster.getId()); //duration 0, remove state
    caster.setIsHolding(null);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,51,new StringBuilder(String.valueOf(this.caster.getId())).toString(),new StringBuilder(String.valueOf(this.cell.getId())).toString());
    verifyTraps(fight,target);
    fight.setCurAction("");
    caster.chamkar = ((long) (System.currentTimeMillis()/1000) + 5);
  }

  private void applyEffect_77(ArrayList<Fighter> cibles, Fight fight)
  {
    int value=1;
    try
    {
      value=Integer.parseInt(args.split(";")[0]);
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
    }
    int num=0;
    for(Fighter target : cibles)
    {
      int val=Formulas.getPointsLost('m',value,caster,target);
      //if(target.getCurPm(fight) <= 0)
      //  val = 0;
      if(val<value)
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,309,caster.getId()+"",target.getId()+","+(value-val));
      if(val<1)
        continue;
      if(turns==0)
      {
        target.addBuff(Constant.STATS_REM_PM,val,1,1,true,spell,args,caster,true);
      }
      else
      {
        target.addBuff(Constant.STATS_REM_PM,val,turns,0,true,spell,args,caster,true);
      }
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_REM_PM,caster.getId()+"",target.getId()+",-"+val+","+turns);
      num+=val;
    }
    if(num!=0)
    {
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_ADD_PM,caster.getId()+"",caster.getId()+","+num+","+turns);
      caster.addBuff(Constant.STATS_ADD_PM,num,turns,1,true,spell,args,caster,false);
      //Gain de PM pendant le tour de jeu
      if(caster.canPlay())
        caster.setCurPm(fight,num);
    }
  }

  private void applyEffect_78(ArrayList<Fighter> cibles, Fight fight)//Bonus PA
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;
    for(Fighter target : cibles)
    {
    	if(turns==0)
        {
          target.addBuff(effectID,val,1,1,true,spell,args,caster,false);
        }
        else {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
        }
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_79(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<1)
      return;//Je vois pas comment, vraiment ...
    else
    {
      for(Fighter target : cibles)
      {
    	if(spell == 103) {
    	if(fight.getTeamId(caster.getId()) != fight.getTeamId(target.getId()))
    		continue;
    	}
        target.addBuff(effectID,-1,turns,0,true,spell,args,caster,true);//on applique un buff
      }
    }
  }

  private void applyEffect_81(ArrayList<Fighter> cibles, Fight fight)
  {// healcion
    if(turns<=0)
    {
      String[] jet=args.split(";");
      int heal=0;
      if(jet.length<6)
      {
        heal=1;
      }
      else
      {
        heal=Formulas.getRandomJet(jet[5]);
      }
      int heal2=heal;
      for(Fighter cible : cibles)
      {
        if(cible.isDead())
          continue;
        if(spell==521)// ruse kistoune
          if(cible.getTeam2()!=caster.getTeam2())
            continue;
        heal=getMaxMinSpell(cible,heal);
        int pdvMax=cible.getPdvMax();
        int healFinal=Formulas.calculFinalHeal(caster,heal,false);
        if((healFinal+cible.getPdv())>pdvMax)
          healFinal=pdvMax-cible.getPdv();
        if(healFinal<1)
          healFinal=0;
        cible.removePdv(caster,-healFinal);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,108,caster.getId()+"",cible.getId()+","+healFinal);
        heal=heal2;
      }
    }
    else
    {
      for(Fighter cible : cibles)
      {
        if(cible.isDead())
          continue;
        cible.addBuff(effectID,0,turns,0,true,spell,args,caster,false);
      }
    }
  }

  private void applyEffect_82(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<=0)
    {
      for(Fighter target : cibles)
      {
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }

        int dmg=Formulas.getRandomJet(args.split(";")[5]);
        //si la cible a le buff renvoie de sort et que le sort peut etre renvoyer
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        //int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_NULL, dmg, false, false, spell);
        int finalDommage=dmg;
        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_NULL,spell);//S'il y a des buffs spï¿½ciaux
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
        //Vol de vie
        int heal=(int)(-finalDommage)/2;
        if(heal<0)
          heal=0;
        if((caster.getPdv()+heal)>caster.getPdvMax())
          heal=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-heal);
        if(heal!=0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+","+heal);

        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,target);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
    {
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,0,turns,0,true,spell,args,caster,true);//on applique un buff
      }
    }
  }

  private void applyEffect_84(ArrayList<Fighter> cibles, Fight fight)
  {
    int value=1;
    try
    {
      value=Integer.parseInt(args.split(";")[0]);
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
    }
    int num=0;
    for(Fighter target : cibles)
    {
    	if(target.getMob() != null){
			if(target.getMob().getTemplate().getId() == 1071){
				target.addBuff(120, this.value, 3, 3, true, this.spell, this.args, target, false);
			target.setCurPa(fight, this.value);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 120,new StringBuilder(String.valueOf(target.getId())).toString(),
					String.valueOf(target.getId()) + "," + this.value + "," + 3);	
			return;	
			
		}
    	}	
      int val=Formulas.getPointsLost('m',value,caster,target);
      //if(target.getCurPa(fight) <= 0)
      //  val = 0;
      if(val<value)
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,308,caster.getId()+"",target.getId()+","+(value-val));

      if(val<1)
        continue;
      if(turns==0)
      {
        target.addBuff(Constant.STATS_REM_PA,val,1,1,true,spell,args,caster,false);
      }
      else
      {
        target.addBuff(Constant.STATS_REM_PA,val,turns,0,true,spell,args,caster,false);
      }
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_REM_PA,caster.getId()+"",target.getId()+",-"+val+","+turns);
      num+=val;
    }
    if(num!=0)
    {
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_ADD_PA,caster.getId()+"",caster.getId()+","+num+","+turns);
      caster.addBuff(Constant.STATS_ADD_PA,num,1,1,true,spell,args,caster,false);
      //Gain de PA pendant le tour de jeu
      if(caster.canPlay())
        caster.setCurPa(fight,num);
    }
  }

  //v2.8 - HP% damage shield fix
  private void applyEffect_85(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<=0)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligï¿½

        int val=(int)Math.floor(((double)caster.getPdv()/100)*dmg);//Valeur des dï¿½gats
        int armor=Formulas.getArmorResist(target,Constant.ELEMENT_EAU);
        if(!target.hasBuff(786))
        {
          val-=armor;
          if(val<0)
            val=0;
          if(armor>0)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+armor);
        }

        int finalDommage=applyOnHitBuffs(val,target,caster,fight,Constant.ELEMENT_EAU,spell);
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
        if(target.getMob()!=null)
          verifmobs(fight,target,97,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligï¿½   
        target.addBuff(effectID,dmg,turns,0,true,spell,args,caster,false);
      }
  }

  //v2.8 - HP% damage shield fix
  private void applyEffect_86(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<=0)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }

        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligï¿½

        int val=(int)Math.floor(((double)caster.getPdv()/100)*dmg);//Valeur des dï¿½gats
        int armor=Formulas.getArmorResist(target,Constant.ELEMENT_TERRE);
        if(!target.hasBuff(786))
        {
          val-=armor;
          if(val<0)
            val=0;
          if(armor>0)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+armor);
        }

        int finalDommage=applyOnHitBuffs(val,target,caster,fight,Constant.ELEMENT_TERRE,spell);
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
        if(target.getMob()!=null)
          verifmobs(fight,target,97,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligï¿½   
        target.addBuff(effectID,dmg,turns,0,true,spell,args,caster,false);
      }
  }

  //v2.8 - HP% damage shield fix
  private void applyEffect_87(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<=0)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        if(spell==1009)
          if(LaunchedSpell.haveEffectTarget(fight.getTeam0(),target,108)<=0)
            continue;
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligï¿½

        int val=(int)Math.floor(((double)caster.getPdv()/100)*dmg);//Valeur des dï¿½gats
        int armor=Formulas.getArmorResist(target,Constant.ELEMENT_AIR);
        if(!target.hasBuff(786))
        {
          val-=armor;
          if(val<0)
            val=0;
          if(armor>0)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+armor);
        }

        int finalDommage=applyOnHitBuffs(val,target,caster,fight,Constant.ELEMENT_AIR,spell);
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
        if(target.getMob()!=null)
          verifmobs(fight,target,97,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligï¿½   
        target.addBuff(effectID,dmg,turns,0,true,spell,args,caster,false);
      }
  }

  //v2.8 - HP% damage shield fix
  private void applyEffect_88(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<=0)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligï¿½

        int val=(int)Math.floor(((double)caster.getPdv()/100)*dmg);//Valeur des dï¿½gats
        int armor=Formulas.getArmorResist(target,Constant.ELEMENT_FEU);
        if(!target.hasBuff(786))
        {
          val-=armor;
          if(val<0)
            val=0;
          if(armor>0)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+armor);
        }

        int finalDommage=applyOnHitBuffs(val,target,caster,fight,Constant.ELEMENT_FEU,spell);
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
        if(target.getMob()!=null)
          verifmobs(fight,target,97,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligï¿½   
        target.addBuff(effectID,dmg,turns,0,true,spell,args,caster,false);
      }
  }

  //v2.8 - HP% damage shield fix
  private void applyEffect_89(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<=0)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }

        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligï¿½

        int val=(int)Math.floor(((double)caster.getPdv()/100)*dmg);//Valeur des dï¿½gats
        int armor=Formulas.getArmorResist(target,Constant.ELEMENT_NEUTRE);
        if(!target.hasBuff(786))
        {
          val-=armor;
          if(val<0)
            val=0;
          if(armor>0)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+armor);
        }

        int finalDommage=applyOnHitBuffs(val,target,caster,fight,Constant.ELEMENT_NEUTRE,spell);

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv(); //Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }

        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
        if(target.getMob()!=null)
          verifmobs(fight,target,97,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]); //%age de pdv infligï¿½   
        target.addBuff(effectID,dmg,turns,0,true,spell,args,caster,false);
      }
  }

  private void applyEffect_90(ArrayList<Fighter> cibles, Fight fight) {
		if (turns <= 0)//Si Direct
		{
			int pAge = Formulas.getRandomJet(args.split(";")[5]);
			int val = pAge * (caster.getPdv() / 100);
			//Calcul des Doms recus par le lanceur
			int finalDommage = applyOnHitBuffs(val, caster, caster, fight, Constant.ELEMENT_NULL,spell);//S'il y a des buffs spéciaux

			if (finalDommage > caster.getPdv())
				finalDommage = caster.getPdv();//Caster va mourrir
			caster.removePdv(caster, finalDommage);
			finalDommage = -(finalDommage);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
					+ "", caster.getId() + "," + finalDommage);

			//Application du soin
			for (Fighter target : cibles) {
		    	  if(target.isDead())
		    		  continue;
				if ((val + target.getPdv()) > target.getPdvMax())
					val = target.getPdvMax() - target.getPdv();//Target va mourrir
				target.removePdv(caster, -val);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + ",+" + val);
			}
			if (caster.getPdv() <= 0)
				fight.onFighterDie(caster, caster);
		} else {
			for (Fighter target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
			}
		}
	}



  private void applyEffect_91(ArrayList<Fighter> cibles, Fight fight, boolean isCaC)//vole eau
  {
    if(isCaC)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }

        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_EAU,dmg,false,true,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_EAU,spell);//S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",3");
        int heal=(int)(-finalDommage)/2;
        if(heal<0)
          heal=0;
        if((caster.getPdv()+heal)>caster.getPdvMax())
          heal=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-heal);
        if(heal!=0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+","+heal);
        if(target.getMob()!=null)
          verifmobs(fight,target,91,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else if(turns<=0)
    {
      if(caster.isHide())
        caster.unHide(spell);
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_EAU,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_EAU,spell);//S'il y a des buffs spï¿½ciaux
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",3");
        int heal=(int)(-finalDommage)/2;
        if(heal<0)
          heal=0;
        if((caster.getPdv()+heal)>caster.getPdvMax())
          heal=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-heal);
        if(heal!=0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+","+heal);
        if(target.getMob()!=null)
          verifmobs(fight,target,91,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,target);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
    {
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,value,turns,0,true,spell,args,caster,false); //on applique un buff
      }
    }
  }

  private void applyEffect_92(ArrayList<Fighter> cibles, Fight fight, boolean isCaC)//vole terre
  {
    if(caster.isHide())
      caster.unHide(spell);
    if(isCaC)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }

        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_TERRE,dmg,false,true,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_TERRE,spell);//S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",1");
        int heal=(int)(-finalDommage)/2;
        if(heal<0)
          heal=0;
        if((caster.getPdv()+heal)>caster.getPdvMax())
          heal=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-heal);
        if(heal!=0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+","+heal);
        if(target.getMob()!=null)
          verifmobs(fight,target,92,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else if(turns<=0)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_TERRE,dmg,false,false,spell,this.cell,target.getCell(),aoe,false);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_TERRE,spell);//S'il y a des buffs spï¿½ciaux
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",1");
        int heal=(int)(-finalDommage)/2;
        if(heal<0)
          heal=0;
        if((caster.getPdv()+heal)>caster.getPdvMax())
          heal=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-heal);
        if(heal!=0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+","+heal);
        if(target.getMob()!=null)
          verifmobs(fight,target,92,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,target);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
    {
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,value,turns,0,true,spell,args,caster,false);//on applique un buff
      }
    }
  }

  private void applyEffect_93(ArrayList<Fighter> cibles, Fight fight, boolean isCaC)//vole air
  {
    if(caster.isHide())
      caster.unHide(spell);
    if(isCaC)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }

        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_AIR,dmg,false,true,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_AIR,spell);//S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",4");
        int heal=(int)(-finalDommage)/2;
        if(heal<0)
          heal=0;
        if((caster.getPdv()+heal)>caster.getPdvMax())
          heal=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-heal);
        if(heal!=0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+","+heal);
        if(target.getMob()!=null)
          verifmobs(fight,target,93,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else if(turns<=0)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_AIR,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_AIR,spell);//S'il y a des buffs spï¿½ciaux
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",4");

        int heal=(int)(-finalDommage)/2;
        if(heal<0)
          heal=0;
        if((caster.getPdv()+heal)>caster.getPdvMax())
          heal=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-heal);
        if(heal!=0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+","+heal);
        if(target.getMob()!=null)
          verifmobs(fight,target,93,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,target);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
    {
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,value,turns,0,true,spell,args,caster,false);
      }
    }
  }

  private void applyEffect_94(ArrayList<Fighter> cibles, Fight fight, boolean isCaC)
  {
    if(caster.isHide())
      caster.unHide(spell);
    if(isCaC) //CaC feu
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_FEU,dmg,false,true,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_FEU,spell);//S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",2");
        int heal=(int)(-finalDommage)/2;
        if(heal<0)
          heal=0;
        if((caster.getPdv()+heal)>caster.getPdvMax())
          heal=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-heal);
        if(heal!=0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+","+heal);
        if(target.getMob()!=null)
          verifmobs(fight,target,94,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else if(turns<=0)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_FEU,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_FEU,spell);//S'il y a des buffs spï¿½ciaux
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",2");
        int heal=(int)(-finalDommage)/2;
        if(heal<0)
          heal=0;
        if((caster.getPdv()+heal)>caster.getPdvMax())
          heal=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-heal);
        if(heal!=0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+","+heal);
        if(target.getMob()!=null)
          verifmobs(fight,target,94,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,target);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
    {
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,value,turns,0,true,spell,args,caster,false);//on applique un buff
      }
    }
  }

  private void applyEffect_95(ArrayList<Fighter> cibles, Fight fight, boolean isCaC)
  {
    if(caster.isHide())
      caster.unHide(spell);
    if(isCaC) //CaC Eau
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_NEUTRE,dmg,false,true,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_NEUTRE,spell);//S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",0");
        int heal=(int)(-finalDommage)/2;
        if(heal<0)
          heal=0;
        if((caster.getPdv()+heal)>caster.getPdvMax())
          heal=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-heal);
        if(heal!=0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+","+heal);
        if(target.getMob()!=null)
          verifmobs(fight,target,95,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else if(turns<=0)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_NEUTRE,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);
        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_NEUTRE,spell);//S'il y a des buffs spï¿½ciaux
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",0");

        int heal=(int)(-finalDommage)/2;
        if(heal<0)
          heal=0;
        if((caster.getPdv()+heal)>caster.getPdvMax())
          heal=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-heal);
        if(heal!=0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+","+heal);
        if(target.getMob()!=null)
          verifmobs(fight,target,95,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,target);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
    {
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,value,turns,0,true,spell,args,caster,false);//on applique un buff
      }
    }
  }

  private void applyEffect_96(ArrayList<Fighter> cibles, Fight fight, boolean isCaC) //dmg eau
  {
    if(isCaC)//CaC Eau
    {
      if(caster.isHide())
        caster.unHide(spell);
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
          continue; // Les monstres de s'entretuent pas
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }

        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);

        //Si le sort est boostï¿½ par un buff spï¿½cifique
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_EAU,dmg,false,true,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_EAU,spell);//S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",3");
        if(target.getMob()!=null)
          verifmobs(fight,target,96,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else if(turns<=0)
    {
      if(caster.isHide())
        caster.unHide(spell);
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
          continue; // Les monstres de s'entretuent pas

        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);

        //Si le sort est boostï¿½ par un buff spï¿½cifique
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }

        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_EAU,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_EAU,spell);//S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",3");
        if(target.getMob()!=null)
          verifmobs(fight,target,96,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
    {
      for(Fighter target : cibles)
        target.addBuff(effectID,value,turns,1,true,spell,args,caster,false); //on applique un buff
    }
  }

  private void applyEffect_97(ArrayList<Fighter> cibles, Fight fight, boolean isCaC) //dmg terre
  {
    if(isCaC)//CaC Terre
    {
      if(caster.isHide())
        caster.unHide(spell);
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(caster.isMob())
        {
          if(caster.getTeam2()==target.getTeam2()&&!caster.isInvocation())
            continue; // Les monstres de s'entretuent pas
        }

        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);

        //Si le sort est boostï¿½ par un buff spï¿½cifique
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_TERRE,dmg,false,true,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_TERRE,spell); //S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv(); //Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;

        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }

        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",1");
        if(target.getMob()!=null)
          verifmobs(fight,target,97,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else if(turns<=0)
    {
      if(caster.isHide())
        caster.unHide(spell);
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
          continue; // Les monstres de s'entretuent pas
    	if(this.spell == 108 && this.chance == 50) {
    		target = caster;
    	}
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort

        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);

        //Si le sort est boostï¿½ par un buff spï¿½cifique
        if(caster.hasBuff(293)||caster.haveState(300))
        {
          if(caster.haveState(300))
            caster.setState(300,0,caster.getId());
          for(SpellEffect SE : caster.getBuffsByEffectID(293))
          {
            if(SE==null)
              continue;
            if(SE.getValue()==spell)
            {
              int add=-1;
              try
              {
                add=Integer.parseInt(SE.getArgs().split(";")[2]);
              }
              catch(Exception e)
              {
                e.printStackTrace();
              }
              if(add<=0)
                continue;
              dmg+=add;
            }
          }
        }

        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_TERRE,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);
        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_TERRE,spell);//S'il y a des buffs spï¿½ciaux
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",1");
        if(target.getMob()!=null)
          verifmobs(fight,target,97,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }

      }
    }
    else
    {
      if(spell==470)
      {
        for(Fighter target : cibles)
        {
          if(target.getTeam()==caster.getTeam())
            continue;
          target.addBuff(effectID,0,turns,0,true,spell,args,caster,false);//on applique un buff
        }
      }
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,value,turns,0,true,spell,args,caster,false);//on applique un buff
      }
    }
  }

  private void applyEffect_98(ArrayList<Fighter> cibles, Fight fight, boolean isCaC) //dmg air
  {
	  if(spell == 1495)
	  {
	 applyEffect_97(cibles,fight,isCaC);
	 return;
	  }
    if(isCaC)//CaC Air
    {
      if(caster.isHide())
        caster.unHide(spell);
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
          continue; // Les monstres de s'entretuent pas
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        //Si le sort est boostï¿½ par un buff spï¿½cifique
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }

        // applyEffect_142

        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_AIR,dmg,false,true,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_AIR,spell);//S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",4");
        if(target.getMob()!=null)
          verifmobs(fight,target,98,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else if(turns<=0)
    {
      if(caster.isHide())
        caster.unHide(spell);
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
          continue; // Les monstres de s'entretuent pas

        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        //Si le sort est boostï¿½ par un buff spï¿½cifique
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }

        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_AIR,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_AIR,spell);//S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir

        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",4");
        if(target.getMob()!=null)
          verifmobs(fight,target,98,cura);

        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
    {
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,value,turns,0,true,spell,args,caster,false);//on applique un buff
      }
    }
  }

  private void applyEffect_99(ArrayList<Fighter> cibles, Fight fight, boolean isCaC) //dmg feu
  {
    if(caster.isHide())
      caster.unHide(spell);
    if(isCaC)//CaC Feu
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
          continue; // Les monstres de s'entretuent pas
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);

        //Si le sort est boostï¿½ par un buff spï¿½cifique
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_FEU,dmg,false,true,spell,this.cell,target.getCell(),aoe,isTrap);
        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_FEU,spell);//S'il y a des buffs spï¿½ciaux
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",2");
        if(target.getMob()!=null)
          verifmobs(fight,target,99,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else if(turns<=0)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
          continue; // Les monstres de s'entretuent pas
        if(spell==36&&target==caster)//Frappe du Craqueleur ne tape pas l'osa
        {
          continue;
        }

        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);

        //Si le sort est boostï¿½ par un buff spï¿½cifique
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_FEU,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);
        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_FEU,spell); //S'il y a des buffs spï¿½ciaux
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",2");
        if(target.getMob()!=null)
          verifmobs(fight,target,99,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
    {
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,value,turns,1,true,spell,args,caster,false);
      }
    }
  }

  private void applyEffect_100(ArrayList<Fighter> cibles, Fight fight, boolean isCaC)
  {
    if(caster.isHide())
      caster.unHide(spell);
    if(fight.getType()==7)
      return;
    if(isCaC) //CaC Neutre
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
          continue; // Les monstres de s'entretuent pas
        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);

        //Si le sort est boostï¿½ par un buff spï¿½cifique
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_NEUTRE,dmg,false,true,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_NEUTRE,spell);//S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",0");
        if(target.getMob()!=null)
          verifmobs(fight,target,100,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else if(turns<=0)
    {
      for(Fighter target : cibles)
      {
    	  if(target.isDead())
    		  continue;
        if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
          continue; // Les monstres de s'entretuent pas

        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1"); // le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);

        //Si le sort est boostï¿½ par un buff spï¿½cifique
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }

        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_NEUTRE,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_NEUTRE,spell);//S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage+",0");
        if(target.getMob()!=null)
          verifmobs(fight,target,100,(-finalDommage));
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
    {
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,value,turns,0,true,spell,args,caster,false); //on applique un buff
      }
    }
  }

  private void applyEffect_101(ArrayList<Fighter> cibles, Fight fight)
  {
    if(spell==470)
    {
      for(Fighter target : cibles)
      {
        if(target.getTeam()==caster.getTeam())
          continue;
        if(target.getMob()!=null)
        {
          if(target.getMob().getTemplate().getId()==1071)// si Rasboul
          {
        		target.addBuff(120, this.value, 3, 3, true, this.spell, this.args, target, false);
    			target.setCurPa(fight, this.value);
    			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 120,new StringBuilder(String.valueOf(target.getId())).toString(),
    					String.valueOf(target.getId()) + "," + this.value + "," + 3);	
            return;
          }
        }
        if(target.hasBuff(788))
        {
          if(target.getBuff(788)!=null)
            if(target.getBuff(788).getValue()==101)
            {
              SpellEffect SE=target.getBuff(788);
              target.addBuff(111,value,SE.getDurationFixed(),0,true,target.getBuff(788).getSpell(),args,target,false);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,101,target.getId()+"",target.getId()+",+"+value);
            }
        }
        int retrait=Formulas.getPointsLost('a',value,caster,target);
        //if(target.getCurPa(fight) <= 0)
        //	retrait = 0;
        if((value-retrait)>0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,308,caster.getId()+"",target.getId()+","+(value-retrait));
        if(retrait>0)
        {
          target.addBuff(effectID,retrait,1,1,false,spell,args,caster,false);//m
          if(turns<=1||duration<=1)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,101,target.getId()+"",target.getId()+",-"+retrait);
        }

        if(fight.getFighterByOrdreJeu()==target)
          fight.setCurFighterPa(fight.getCurFighterPa()-retrait);
      }
      return;
    }
    if(turns<=0)
    {
      for(Fighter target : cibles)
      {
        if(target.hasBuff(788))
        {
          if(target.getBuff(788)!=null)
            if(target.getBuff(788).getValue()==101)
            {
              target.addBuff(111,value,turns,1,true,target.getBuff(788).getSpell(),args,target,false);
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,101,target.getId()+"",target.getId()+",+"+value);
            }
        }
        int remove=Formulas.getPointsLost('a',value,caster,target);
        //if(target.getCurPa(fight) <= 0)
        //	remove = 0;
        if((value-remove)>0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,308,caster.getId()+"",target.getId()+","+(value-remove));
        if(remove>0)
        {
          target.addBuff(Constant.STATS_REM_PA,remove,1,1,false,spell,args,caster,false);
          if(turns<=1||duration<=1)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,101,target.getId()+"",target.getId()+",-"+remove);
        }

        if(fight.getFighterByOrdreJeu()==target)
          fight.setCurFighterPa(fight.getCurFighterPa()-remove);

        if(target.getMob()!=null)
          this.verifmobs(fight,target,101,0);
      }
    }
    else
    {
      if(cibles.size()>0)
        for(Fighter target : cibles)
        {
            if(target.getMob()!=null)
            {
              if(target.getMob().getTemplate().getId()==1071)// si Rasboul
              {
            		target.addBuff(120, this.value, 3, 3, true, this.spell, this.args, target, false);
        			target.setCurPa(fight, this.value);
        			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 120,new StringBuilder(String.valueOf(target.getId())).toString(),
        					String.valueOf(target.getId()) + "," + this.value + "," + 3);	
                return;
              }
            }
          if(target.hasBuff(788))
          {
            if(target.getBuff(788)!=null)
              if(target.getBuff(788).getValue()==101)
              {
                SpellEffect SE=target.getBuff(788);
                target.addBuff(111,value,SE.getDurationFixed(),0,true,target.getBuff(788).getSpell(),args,target,false);
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,101,target.getId()+"",target.getId()+",+"+value);
              }
          }
          int retrait=Formulas.getPointsLost('a',value,caster,target);
          //if(target.getCurPa(fight) <= 0)
          //  retrait = 0;
          if((value-retrait)>0)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,308,caster.getId()+"",target.getId()+","+(value-retrait));
          if(retrait>0)
          {
            target.addBuff(effectID,retrait,1,1,true,spell,args,caster,false);//m
            if(turns<=1||duration<=1)
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,101,target.getId()+"",target.getId()+",-"+retrait);
          }

          if(fight.getFighterByOrdreJeu()==target)
            fight.setCurFighterPa(fight.getCurFighterPa()-retrait);

        }
    }

  }

  private void applyEffect_105(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;
    for(Fighter target : cibles)
    {
    	if(spell == 20)
      target.addBuff(effectID,val,2,1,true,spell,args,caster,true);
    	else
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
    }
  }

  private void applyEffect_106(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=-1;
    try
    {
      val=Integer.parseInt(args.split(";")[1]); //Niveau de sort max
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if(val==-1)
      return;

    this.duration=turns;
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
    }
  }

  private void applyEffect_107(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<1)
      return; //Je vois pas comment, vraiment ...
    else
    {
      for(Fighter target : cibles)
        target.addBuff(effectID,0,turns,0,true,spell,args,caster,true); //on applique un buff
    }
  }

  private void applyEffect_108(ArrayList<Fighter> cibles, Fight fight, boolean isCaC)
  {
    if(spell==441)
      return;
    if(isCaC)
      return;
    if(turns<=0)
    {
      String[] jet=args.split(";");
      int heal=0;
      if(jet.length<6)
      {
        heal=1;
      }
      else
      {
        heal=Formulas.getRandomJet(jet[5]);
      }
      int heal2=heal;
      for(Fighter cible : cibles)
      {
        if(cible.isDead())
          continue;
        applyOnHealBuffs(heal,cible,caster,fight);
        if(cible.isDead())
          continue;
        if(caster.hasBuff(178))
          heal+=caster.getBuffValue(178);
        if(caster.hasBuff(179))
          heal=heal-caster.getBuffValue(179);
        heal=getMaxMinSpell(cible,heal);
        int pdvMax=cible.getPdvMax();
        int healFinal=Formulas.calculFinalHeal(caster,heal,isCaC);
        if((healFinal+cible.getPdv())>pdvMax)
          healFinal=pdvMax-cible.getPdv();
        if(healFinal<1||cible.haveState(Constant.STATE_UNHEALABLE))
          healFinal=0;
        cible.removePdv(caster,-healFinal);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,108,caster.getId()+"",cible.getId()+","+healFinal);
        heal=heal2;
      }
    }
    else
    {
      cibles.stream().filter(target -> !target.isDead()).forEach(target -> target.addBuff(effectID,0,turns,0,true,spell,args,caster,false));
    }
  }

  private void applyEffect_109(Fight fight)//Dommage pour le lanceur (fixes)
  {
    if(turns<=0)
    {
      int dmg=0;
      if(caster.hasBuff(782)) //Brokle
      {
        dmg=Formulas.getMaxJet(args.split(";")[5]);
      }
      else if(caster.hasBuff(781)) //Jinx
      {
        dmg=Formulas.getMinJet(args.split(";")[5]);
      }
      else
        dmg=Formulas.getRandomJet(args.split(";")[5]);
      int finalDommage=Formulas.calculFinalDommage(fight,caster,caster,Constant.ELEMENT_NULL,dmg,false,false,spell,this.cell,caster.getCell(),aoe,isTrap);

      finalDommage=applyOnHitBuffs(finalDommage,caster,caster,fight,Constant.ELEMENT_NULL,spell);//S'il y a des buffs spï¿½ciaux
      if(finalDommage>caster.getPdv())
        finalDommage=caster.getPdv();//Caster va mourrir
      caster.removePdv(caster,finalDommage);
      caster.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-caster.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+caster.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
      finalDommage=-(finalDommage);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",caster.getId()+","+finalDommage);

      if(caster.getPdv()<=0)
      {
        fight.onFighterDie(caster,caster);

      }
    }
    else
    {
      caster.addBuff(effectID,0,turns,0,true,spell,args,caster,false);//on applique un buff
    }
  }

  private void applyEffect_110(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_111(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    boolean repetibles=false;
    int gainedAP=0;
    for(Fighter target : cibles)
    {
      if(spell==101&&target!=caster)
        continue;
      if(spell==115)
      { // odorat
        if(!repetibles)
        {
          gainedAP=Formulas.getRandomJet(jet);
          if(gainedAP==-1)
            continue;
        }
        target.addBuff(effectID,gainedAP,turns,turns,true,spell,args,caster,false);
        if(target.canPlay())
          target.setCurPa(fight,gainedAP);
        repetibles=true;
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+",-"+target.getPa()+","+turns);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+target.getPa()+","+turns);
        //gainedAP =0;
        continue;
      }

      if(spell==101&&val==1)
        turns=0;
      if(spell==521)// ruse kistoune
        if(target.getTeam2()!=caster.getTeam2())
          continue;
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      //Gain de PA pendant le tour de jeu
      if(target.canPlay())
        target.setCurPa(fight,val);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_112(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {
      return;
    }
    if(duration<1)
      duration=1;
    if(spell==1090)
    {
      caster.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",caster.getId()+","+val+","+turns);
      return;
    }
    else if(spell==477) //Red Wyrmling's Dragofire, do not print message
    {
      for(Fighter target : cibles)
        target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_114(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    boolean modif=false;
    if(!modif)
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
      }
  }

  private void applyEffect_115(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_116(ArrayList<Fighter> cibles, Fight fight)//Malus PO
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_117(ArrayList<Fighter> cibles, Fight fight)//Bonus PO
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
      //Gain de PO pendant le tour de jeu
      if(target.canPlay()&&target==caster)
        target.getTotalStats().addOneStat(Constant.STATS_ADD_PO,val);
    }
  }

  private void applyEffect_118(ArrayList<Fighter> cibles, Fight fight)//Bonus Force
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    if(spell==52)//cupiditer
      cibles=fight.getFighters(3);

    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_119(ArrayList<Fighter> cibles, Fight fight)//Bonus Agilitï¿½
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_120(ArrayList<Fighter> cibles, Fight fight)//Bonus PA
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    if(turns==0)
    {
    	caster.addBuff(Constant.STATS_ADD_PA,val,0,1,true,spell,args,caster,false);
    }else {
    caster.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
    }
    caster.setCurPa(fight,val);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",caster.getId()+","+val+","+turns);
  }

  private void applyEffect_121(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_122(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_123(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_124(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_125(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;

    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_126(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    if(spell==52)//cupiditer
      cibles=fight.getFighters(3);

    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_127(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<=0)
    {
      for(Fighter target : cibles)
      {
        int retrait=Formulas.getPointsLost('m',value,caster,target);
        //if(target.getCurPm(fight) <= 0)
        //	retrait = 0;
        if((value-retrait)>0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,309,caster.getId()+"",target.getId()+","+(value-retrait));
        }
        if(retrait>0)
        {
          target.addBuff(Constant.STATS_REM_PM,retrait,1,1,false,spell,args,caster,false);
          if(turns<=1||duration<=1)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,127,target.getId()+"",target.getId()+",-"+retrait);
          if(target.getMob()!=null)
            this.verifmobs(fight,target,127,0);
        }

      }
    }
    else
    {
      for(Fighter target : cibles)
      {
        int retrait=Formulas.getPointsLost('m',value,caster,target);
        //if(target.getCurPm(fight) <= 0)
        //	retrait = 0;
        if((value-retrait)>0)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,309,caster.getId()+"",target.getId()+","+(value-retrait));
        if(retrait>0)
        {
          if(spell==136)//Mot d'immobilisation
          {
            target.addBuff(effectID,retrait,turns,turns,false,spell,args,caster,false);
          }
          else
          {
            target.addBuff(effectID,retrait,1,1,false,spell,args,caster,false);
          }
          if(turns<=1||duration<=1)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,127,target.getId()+"",target.getId()+",-"+retrait);
        }
        if(retrait>0)
          if(target.getMob()!=null)
            this.verifmobs(fight,target,127,0);
      }
    }
  }

  private void applyEffect_128(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    boolean repetibles=false;
    int gainedMP=0;
    for(Fighter target : cibles)
    {
      if(spell==115)
      { // odorat
        if(!repetibles)
        {
          gainedMP=Formulas.getRandomJet(jet);
          if(gainedMP==-1)
            continue;
        }
        target.addBuff(effectID,gainedMP,turns,turns,true,spell,args,caster,false);
        if(target.canPlay()&&target==caster)
          target.setCurPm(fight,gainedMP);
        repetibles=true;
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+",-"+target.getCurPm(fight)+","+turns);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+target.getCurPm(fight)+","+turns);
        continue;
      }
      else if(spell==521)// ruse kistoune
        if(target.getTeam2()!=caster.getTeam2())
          continue;

      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      //Gain de PM pendant le tour de jeu
      if(target.canPlay()&&target==caster)
        target.setCurPm(fight,val);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_130(Fight fight, ArrayList<Fighter> cibles)
  {

  }

  private void applyEffect_131(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,value,turns,1,true,spell,args,caster,false);
    }
  }

  //v2.7 - drunk dispell fix
  private void applyEffect_132(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
    {
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,132,caster.getId()+"",target.getId()+"");
      if(target.getPersonnage()!=null&&!target.hasLeft())
        SocketManager.GAME_SEND_STATS_PACKET(target.getPersonnage());
      if(target.isHide())
        target.unHide(spell);
      target.debuff();
    }
  }

  private void applyEffect_138(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
    	if(this.spell == 149)
      target.addBuff(effectID,val,1,1,true,spell,args,caster,false);
    	else
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);		
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_140(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,0,1,0,true,spell,args,caster,false);
    }
  }

  private void applyEffect_141(Fight fight, ArrayList<Fighter> cibles)
  {
    for(Fighter target : cibles)
    {
      if(target.hasBuff(765))//sacrifice
      {
        if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
        {
          applyEffect_765B(fight,target);
          target=target.getBuff(765).getCaster();
        }
      }
      fight.onFighterDie(target,target);
    }
  }

  private void applyEffect_142(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
    	if(this.spell == 149)
      target.addBuff(effectID,val,1,1,true,spell,args,caster,false);
    	else
      target.addBuff(effectID,val,1,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_143(ArrayList<Fighter> cibles, Fight fight)
  {
    if(spell==470)
    {
      String[] jet=args.split(";");
      int heal=0;
      if(jet.length<6)
      {
        heal=1;
      }
      else
      {
        heal=Formulas.getRandomJet(jet[5]);
      }
      int dmg2=heal;
      for(Fighter cible : cibles)
      {
        if(cible.getTeam()!=caster.getTeam())
          continue;
        if(cible.isDead())
          continue;
        heal=getMaxMinSpell(cible,heal);
        int healFinal=Formulas.calculFinalHeal(caster,heal,false);
        if(spell==450)
        {
          healFinal=heal;
        }
        if((healFinal+cible.getPdv())>cible.getPdvMax())
          healFinal=cible.getPdvMax()-cible.getPdv();
        if(healFinal<1)
          healFinal=0;
        cible.removePdv(caster,-healFinal);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,108,caster.getId()+"",cible.getId()+","+healFinal);
        heal=dmg2;
      }
      return;
    }
    if(turns<=0)
    {
      String[] jet=args.split(";");
      int heal=0;
      if(jet.length<6)
      {
        heal=1;
      }
      else
      {
        heal=Formulas.getRandomJet(jet[5]);
      }
      int dmg2=heal;
      for(Fighter cible : cibles)
      {
        if(cible.isDead())
          continue;
        heal=getMaxMinSpell(cible,heal);
        int healFinal=Formulas.calculFinalHeal(caster,heal,false);
        if(spell==450)
        {
          healFinal=heal;
        }
        if((healFinal+cible.getPdv())>cible.getPdvMax())
          healFinal=cible.getPdvMax()-cible.getPdv();
        if(healFinal<1)
          healFinal=0;
        cible.removePdv(caster,-healFinal);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,108,caster.getId()+"",cible.getId()+","+healFinal);
        heal=dmg2;
      }
    }
    else
    {
      for(Fighter cible : cibles)
      {
        if(cible.isDead())
          continue;
        cible.addBuff(effectID,0,turns,0,true,spell,args,caster,false);
      }
    }
  }

  private void applyEffect_144(Fight pelea, ArrayList<Fighter> objetivos)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;
    int val2=val;
    for(Fighter objetivo : objetivos)
    {
      val=getMaxMinSpell(objetivo,val);
      objetivo.addBuff(145,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea,7,145,caster.getId()+"",objetivo.getId()+","+val+","+turns);
      val=val2;
    }
  }

  private void applyEffect_145(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_149(Fight fight, ArrayList<Fighter> cibles)
  {
    int id=-1;

    try
    {
      id=Integer.parseInt(args.split(";")[2]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    for(Fighter target : cibles)
    {
      if(target.isDead())
        continue;
      if(spell==686)
        if(target.getPersonnage()!=null&&target.getPersonnage().getSexe()==1||target.getMob()!=null&&target.getMob().getTemplate().getId()==547)
          id=8011;
      if(id==-1)
        id=target.getDefaultGfx();

      target.addBuff(effectID,id,turns,1,true,spell,args,caster,true);
      int defaut=target.getDefaultGfx();
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+defaut+","+id+","+(target.canPlay() ? turns+1 : turns));
    }
  }

  private void applyEffect_150(Fight fight, ArrayList<Fighter> cibles)
  {
    if(turns==0)
      return;
   /* if(caster.getMob() != null)
    if(caster.getMob().getTemplate() != null)
    	return;*/
    if(spell==547||spell==546||spell==548||spell==525)
    {
      caster.addBuff(effectID,0,3,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",caster.getId()+","+(3-1));
      caster.lastInvisCell=caster.getCell();
      caster.lastInvisMP=caster.getCurPm(fight);
      return;
    }

    for(Fighter target : cibles)
    {
      target.lastInvisCell=target.getCell();
      if(target==fight.getFighterByOrdreJeu())
      {
        if(spell==72)
        {
          if(spellLvl==6)
            target.lastInvisMP=target.getCurPm(fight)+2;
          else
            target.lastInvisMP=target.getCurPm(fight)+1;
        }
        else
          target.lastInvisMP=target.getCurPm(fight);
      }
      else
        target.lastInvisMP=0;
      target.addBuff(effectID,0,turns,1,true,spell,args,caster,true);

      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+(turns-1));
    }
  }

  private void applyEffect_152(Fight pelea, ArrayList<Fighter> objetivos)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;
    int val2=val;
    for(Fighter objetivo : objetivos)
    {
      val=getMaxMinSpell(objetivo,val);
      objetivo.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea,7,effectID,caster.getId()+"",objetivo.getId()+","+val+","+turns);
      val=val2;
    }
  }

  private void applyEffect_153(Fight pelea, ArrayList<Fighter> objetivos)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;

    int val2=val;
    for(Fighter objetivo : objetivos)
    {
      val=getMaxMinSpell(objetivo,val);
      objetivo.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea,7,effectID,caster.getId()+"",objetivo.getId()+","+val+","+turns);
      val=val2;
    }
  }

  private void applyEffect_154(Fight pelea, ArrayList<Fighter> objetivos)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;

    int val2=val;
    for(Fighter objetivo : objetivos)
    {
      val=getMaxMinSpell(objetivo,val);
      objetivo.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea,7,effectID,caster.getId()+"",objetivo.getId()+","+val+","+turns);
      val=val2;
    }
  }

  private void applyEffect_155(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_156(Fight pelea, ArrayList<Fighter> objetivos)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;

    int val2=val;
    for(Fighter objetivo : objetivos)
    {
      val=getMaxMinSpell(objetivo,val);
      objetivo.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea,7,effectID,caster.getId()+"",objetivo.getId()+","+val+","+turns);
      val=val2;
    }
  }

  private void applyEffect_157(Fight pelea, ArrayList<Fighter> objetivos)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;

    int val2=val;
    for(Fighter objetivo : objetivos)
    {
      val=getMaxMinSpell(objetivo,val);
      objetivo.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea,7,effectID,caster.getId()+"",objetivo.getId()+","+val+","+turns);
      val=val2;
    }
  }

  private void applyEffect_160(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_161(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_162(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_163(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    if(cibles.isEmpty()&&spell==310&&caster.getOldCible()!=null)
    {
      caster.getOldCible().addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getOldCible().getId()+"",caster.getOldCible().getId()+","+turns);
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_164(ArrayList<Fighter> objetivos, Fight pelea)
  {
    int val=value;
    if(val==-1)
      return;

    for(Fighter objetivo : objetivos)
    {
      objetivo.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
    }
  }

  private void applyEffect_165(Fight fight, ArrayList<Fighter> cibles)
  {
    int value=-1;
    try
    {
      value=Integer.parseInt(args.split(";")[1]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if(value==-1)
      return;
    caster.addBuff(effectID,value,turns,1,true,spell,args,caster,true);
  }

  private void applyEffect_168(ArrayList<Fighter> cibles, Fight fight)
  {// - PA, no esquivables
    if(turns<=0)
    {
      for(Fighter cible : cibles)
      {
    	  if(cible.getMob() != null){
				if(cible.getMob().getTemplate().getId() == 1071){
					cible.addBuff(120, this.value, 3, 3, true, this.spell, this.args, cible, false);
					cible.setCurPa(fight, this.value);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 120,new StringBuilder(String.valueOf(cible.getId())).toString(),
						String.valueOf(cible.getId()) + "," + this.value + "," + 3);	
				return;	
				
			}
    	  }
        if(cible.isDead())
          continue;
        cible.addBuff(effectID,value,1,1,true,spell,args,caster,false);
        if(turns<=1||duration<=1)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,168,cible.getId()+"",cible.getId()+",-"+value);
        }
        if(fight.getFighterByOrdreJeu()==cible)
          fight.setCurFighterPa(fight.getCurFighterPa()-value);
        if(cible.getMob()!=null)
          verifmobs(fight,cible,168,0);
      }
    }
    else
    {
      boolean repetibles=false;
      int lostPA=0;

      for(Fighter cible : cibles)
      {
    		if(cible.getMob() != null){
				if(cible.getMob().getTemplate().getId() == 1071){
					cible.addBuff(120, this.value, 3, 3, true, this.spell, this.args, cible, false);
					cible.setCurPa(fight, this.value);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 120,new StringBuilder(String.valueOf(cible.getId())).toString(),
						String.valueOf(cible.getId()) + "," + this.value + "," + 3);	
				return;	
				
			}
				}
        if(cible.isDead())
          continue;
        if(spell==197||spell==112)
        { // potencia silvestre, garra - ceangal (critico)
          cible.addBuff(effectID,value,turns,turns,true,spell,args,caster,false);
        }
        else if(spell==115)
        { // Odorat
          if(!repetibles)
          {
            lostPA=Formulas.getRandomJet(jet);
            if(lostPA==-1)
              continue;
          }
          cible.addBuff(effectID,lostPA,turns,turns,true,spell,args,caster,false);

          if(cible.canPlay()&&cible==caster)
            cible.setCurPa(fight,-lostPA);
          repetibles=true;
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",cible.getId()+",-"+lostPA+","+turns);
          continue;
        }
        else
        {
          cible.addBuff(effectID,value,turns,duration,true,spell,args,caster,false);
        }
        if(turns<=1||duration<=1)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,168,cible.getId()+"",cible.getId()+",-"+value);

        if(fight.getFighterByOrdreJeu()==cible)
          fight.setCurFighterPa(fight.getCurFighterPa()-value);

        if(cible.getMob()!=null)
          verifmobs(fight,cible,168,0);

        if(cible.canPlay()&&cible==caster)
          cible.setCurPa(fight,-lostPA);
      }
    }
  }

  private void applyEffect_169(ArrayList<Fighter> cibles, Fight fight)
  {

    if(spell==686&&caster.haveState(1)) //anti bug saoul
      return;
    if(turns<=0)
    {
      for(Fighter cible : cibles)
      {
        if(cible.isDead())
          continue;
        cible.addBuff(effectID,value,1,1,true,spell,args,caster,false);
        if(turns<=1||duration<=1)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,169,cible.getId()+"",cible.getId()+",-"+value);
        if(cible.getMob()!=null)
          verifmobs(fight,cible,169,0);
      }
    }
    else
    {
      if(cibles.isEmpty()&&spell==120&&caster.getOldCible()!=null)
      {
        caster.getOldCible().addBuff(effectID,value,turns,1,false,spell,args,caster,false);
        if(turns<=1||duration<=1)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,169,caster.getOldCible().getId()+"",caster.getOldCible().getId()+",-"+value);
      }
      boolean repetibles=false;
      int lostPM=0;
      for(Fighter cible : cibles)
      {
        if(cible.isDead())
          continue;
        if(spell==192)
        {
          cible.addBuff(effectID,value,turns,0,true,spell,args,caster,false);
        }
        else if(spell==115) //smell
        {
          if(!repetibles)
          {
            lostPM=Formulas.getRandomJet(jet);
            if(lostPM==-1)
              continue;
          }
          cible.addBuff(effectID,lostPM,turns,turns,true,spell,args,caster,false);
          if(cible.canPlay()&&cible==caster)
            cible.setCurPm(fight,-lostPM);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",cible.getId()+",-"+lostPM+","+turns);
          repetibles=true;

          continue;
        }
        else if(spell==197) //portencia sivelstre
        {
          cible.addBuff(effectID,value,turns,turns,true,spell,args,caster,false);
        }
        else if(spell==686) //picole
        {
          cible.addBuff(effectID,value,turns,turns,true,spell,args,caster,false);
        }
        else
        {
          cible.addBuff(effectID,value,1,1,true,spell,args,caster,false);
        }
        if(turns<=1||duration<=1)
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,169,cible.getId()+"",cible.getId()+",-"+value);
        if(cible.getMob()!=null)
          verifmobs(fight,cible,168,0);
        if(cible.canPlay()&&cible==caster)
          cible.setCurPm(fight,-lostPM);
      }
    }
  }

  private void applyEffect_171(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_176(ArrayList<Fighter> objetivos, Fight pelea)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {
      return;
    }
    for(Fighter objetivo : objetivos)
    {
      objetivo.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea,7,Constant.STATS_ADD_PROS,caster.getId()+"",objetivo.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_177(ArrayList<Fighter> objetivos, Fight pelea)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {
      return;
    }
    for(Fighter objetivo : objetivos)
    {
      objetivo.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea,7,Constant.STATS_REM_PROS,caster.getId()+"",objetivo.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_178(ArrayList<Fighter> objetivos, Fight pelea)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {
      return;
    }
    int val2=val;
    for(Fighter objetivo : objetivos)
    {
      val=getMaxMinSpell(objetivo,val);
      objetivo.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea,7,effectID,caster.getId()+"",objetivo.getId()+","+val+","+turns);
      val=val2;
    }
  }

  private void applyEffect_179(ArrayList<Fighter> objetivos, Fight pelea)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {
      return;
    }
    int val2=val;
    for(Fighter objetivo : objetivos)
    {
      val=getMaxMinSpell(objetivo,val);
      objetivo.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea,7,effectID,caster.getId()+"",objetivo.getId()+","+val+","+turns);
      val=val2;
    }
  }

  private void applyEffect_180(Fight fight) //invocation
  {
    int cell=this.cell.getId();
    if(!this.cell.getFighters().isEmpty())
      return;
    if(fight.getTeam(caster.getTeam()).size() > 30)
    	return;
    int id=fight.getNextLowerFighterGuid();
    Player clone=Player.ClonePerso(caster.getPersonnage(),-id-10000,(caster.getPersonnage().getMaxPdv()-((caster.getLvl()-1)*5+50)));
    clone.setFight(fight);

    Fighter fighter=new Fighter(fight,clone);
    fighter.fullPdv();
    fighter.setTeam(caster.getTeam());
    fighter.setInvocator(caster);

    fight.getMap().getCase(cell).addFighter(fighter);
    fighter.setCell(fight.getMap().getCase(cell));

    fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(caster)+1),fighter);
    fight.addFighterInTeam(fighter,caster.getTeam());

    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,180,caster.getId()+"",fighter.getGmPacket('+',true).substring(3));
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"",fight.getGTL());

    verifyTraps(fight,fighter);
  }

  private void applyEffect_181(Fight fight) //invocation
  {
    int cell=this.cell.getId();

    if(!this.cell.getFighters().isEmpty())
      return;

    int id=-1,level=-1;

    try
    {
      String mobs=args.split(";")[0],levels=args.split(";")[1];

      if(mobs.contains(":"))
      {
        String[] split=mobs.split(":");
        id=Integer.parseInt(split[Formulas.getRandomValue(0,split.length-1)]);
      }
      else
      {
        id=Integer.parseInt(mobs);
      }

      if(levels.contains(":"))
      {
        String[] split=levels.split(":");
        level=Integer.parseInt(split[Formulas.getRandomValue(0,split.length-1)]);
      }
      else
      {
        level=Integer.parseInt(levels);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    MobGrade MG;
    try
    {
      MG=Main.world.getMonstre(id).getGradeByLevel(level).getCopy();
    }
    catch(Exception e1)
    {
      try
      {
        MG=Main.world.getMonstre(id).getRandomGrade().getCopy();
      }
      catch(Exception e2)
      {
        return;
      }
    }

    if(id==-1||level==-1||MG==null)
      return;
    if(fight.getTeam(caster.getTeam()).size() > 30)
    	return;
    MG.setInFightID(fight.getNextLowerFighterGuid());
    if(caster.getPersonnage()!=null)
      MG.modifStatByInvocator(caster,id);
    Fighter F=new Fighter(fight,MG);
    F.setTeam(caster.getTeam());
    F.setInvocator(caster);
    fight.getMap().getCase(cell).addFighter(F);
    F.setCell(fight.getMap().getCase(cell));
    fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(caster)+1),F);
    fight.addFighterInTeam(F,caster.getTeam());
    String gm=F.getGmPacket('+',true).substring(3);
    String gtl=fight.getGTL();
    try
    {
      if(this.caster.getMob()!=null)
        Thread.sleep(1000);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,181,caster.getId()+"",gm);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"",gtl);
    caster.nbrInvoc++;
    /*ArrayList<Trap> P=(new ArrayList<Trap>());
    P.addAll(fight.getAllTraps());
    for(Trap p : P)
    {
      int dist=PathFinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),F.getCell().getId());
      //on active le piege
      if(dist<=p.getSize())
        p.onTraped(F);
    }*/
    verifyTraps(fight,F);
  }

  private void applyEffect_182(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_183(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_184(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_185(Fight fight)
  {
    int monster=-1,level=-1;

    try
    {
      monster=Integer.parseInt(args.split(";")[0]);
      level=Integer.parseInt(args.split(";")[1]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    MobGrade mobGrade;

    try
    {
      mobGrade=Main.world.getMonstre(monster).getGradeByLevel(level).getCopy();
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return;
    }

    if(monster==-1||level==-1||mobGrade==null)
      return;
    if(monster==556&&this.caster.getPersonnage()!=null)
      mobGrade.modifStatByInvocator(this.caster,556);

    int id=fight.getNextLowerFighterGuid();
    mobGrade.setInFightID(id);

    Fighter fighter=new Fighter(fight,mobGrade);
    fighter.setTeam(this.caster.getTeam());
    fighter.setInvocator(this.caster);

    fight.getMap().getCase(this.cell.getId()).addFighter(fighter);
    fighter.setCell(fight.getMap().getCase(this.cell.getId()));
    fight.addFighterInTeam(fighter,this.caster.getTeam());
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,185,this.caster.getId()+"",fighter.getGmPacket('+',true).substring(3));
  }

  private void applyEffect_186(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    int val2=val;
    for(Fighter f : cibles)
    {
      val=getMaxMinSpell(f,val);
      f.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",f.getId()+","+val+","+turns);
      val=val2;
    }
  }

  private void applyEffect_202(Fight fight, ArrayList<Fighter> cibles)
  {
    // unhide des personnages
    for(Fighter target : cibles)
    {
      if(target.isHide())
      {
        if(target!=caster)
          target.unHide(spell);
      }
    }
    // unhide des piï¿½ges
    for(Trap p : fight.getAllTraps())
    {
      p.setIsUnHide(caster);
      p.appear(caster);
    }
  }

  private void applyEffect_210(Fight fight, ArrayList<Fighter> cibles)
  {
    if(spell==686&&caster.haveState(1))//anti bug saoul
    {
      int pa=1;
      if(this.spellLvl==5)
        pa=2;
      else if(this.spellLvl==4)
        pa=3;
      else if(this.spellLvl==3||this.spellLvl==2)
        pa=4;
      else if(this.spellLvl==1)
        pa=5;

      caster.addBuff(111,pa,-1,1,true,spell,args,caster,false);
      //Gain de PA pendant le tour de jeu
      caster.setCurPa(fight,pa);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,111,caster.getId()+"",caster.getId()+","+pa+","+-1);
      return;
    }
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;
    if(spell==2005) //Crackler's Crushing, do not print message
    {
      for(Fighter target : cibles)
        target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_211(Fight fight, ArrayList<Fighter> cibles)
  {
    if(spell==686&&caster.haveState(1))//anti bug saoul
      return;
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;
    if(spell==2005) //Crackler's Crushing, do not print message
    {
      for(Fighter target : cibles)
        target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_212(Fight fight, ArrayList<Fighter> cibles)
  {
    if(spell==686&&caster.haveState(1))//anti bug saoul
      return;
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;
    if(spell==2005) //Crackler's Crushing, do not print message
    {
      for(Fighter target : cibles)
        target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_213(Fight fight, ArrayList<Fighter> cibles)
  {
    if(spell==686&&caster.haveState(1))//anti bug saoul
      return;
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;
    if(spell==2005) //Crackler's Crushing, do not print message
    {
      for(Fighter target : cibles)
        target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_214(Fight fight, ArrayList<Fighter> cibles)
  {
    if(spell==686&&caster.haveState(1))//anti bug saoul
      return;
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
      return;
    if(spell==2005) //Crackler's Crushing, do not print message
    {
      for(Fighter target : cibles)
        target.addBuff(effectID,val,turns,1,true,spell,args,caster,false);
      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }

  }

  private void applyEffect_215(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_216(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_217(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_218(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_219(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_220(ArrayList<Fighter> objetivos, Fight pelea)
  {
    if(turns<1)
      return;
    else
    {
      for(Fighter objetivo : objetivos)
        objetivo.addBuff(effectID,0,turns,0,true,spell,args,caster,true);
    }
  }

  private void applyEffect_240(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_241(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_242(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_243(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_244(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {

      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_265(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {
      return;
    }
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_266(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    int vol=0;
    for(Fighter target : cibles)
    {
      target.addBuff(Constant.STATS_REM_CHAN,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_REM_CHAN,caster.getId()+"",target.getId()+","+val+","+turns);
      vol+=val;
    }
    if(vol==0)
      return;
    //on ajoute le buff
    caster.addBuff(Constant.STATS_ADD_CHAN,vol,turns,1,true,spell,args,caster,false);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_ADD_CHAN,caster.getId()+"",caster.getId()+","+vol+","+turns);
  }

  private void applyEffect_267(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    int vol=0;
    for(Fighter target : cibles)
    {
      target.addBuff(Constant.STATS_REM_VITA,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_REM_VITA,caster.getId()+"",target.getId()+","+val+","+turns);
      vol+=val;
    }
    if(vol==0)
      return;
    //on ajoute le buff
    caster.addBuff(Constant.STATS_ADD_VITA,vol,turns,1,true,spell,args,caster,false);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_ADD_VITA,caster.getId()+"",caster.getId()+","+vol+","+turns);
  }

  private void applyEffect_268(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    int vol=0;
    for(Fighter target : cibles)
    {
      target.addBuff(Constant.STATS_REM_AGIL,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_REM_AGIL,caster.getId()+"",target.getId()+","+val+","+turns);
      vol+=val;
    }
    if(vol==0)
      return;
    //on ajoute le buff
    caster.addBuff(Constant.STATS_ADD_AGIL,vol,turns,1,true,spell,args,caster,false);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_ADD_AGIL,caster.getId()+"",caster.getId()+","+vol+","+turns);
  }

  private void applyEffect_269(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    int vol=0;
    for(Fighter target : cibles)
    {
      target.addBuff(Constant.STATS_REM_INTE,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_REM_INTE,caster.getId()+"",target.getId()+","+val+","+turns);
      vol+=val;
    }
    if(vol==0)
      return;
    //on ajoute le buff
    caster.addBuff(Constant.STATS_ADD_INTE,vol,turns,1,true,spell,args,caster,false);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_ADD_INTE,caster.getId()+"",caster.getId()+","+vol+","+turns);
  }

  private void applyEffect_270(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    int vol=0;
    for(Fighter target : cibles)
    {
      target.addBuff(Constant.STATS_REM_SAGE,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_REM_SAGE,caster.getId()+"",target.getId()+","+val+","+turns);
      vol+=val;
    }
    if(vol==0)
      return;
    //on ajoute le buff
    caster.addBuff(Constant.STATS_ADD_SAGE,vol,turns,1,true,spell,args,caster,false);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_ADD_SAGE,caster.getId()+"",caster.getId()+","+vol+","+turns);
  }

  private void applyEffect_271(Fight fight, ArrayList<Fighter> cibles)
  {
    int val=Formulas.getRandomJet(jet);
    int vol=0;
    for(Fighter target : cibles)
    {
      target.addBuff(Constant.STATS_REM_FORC,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_REM_FORC,caster.getId()+"",target.getId()+","+val+","+turns);
      vol+=val;
    }
    if(vol==0)
      return;
    //on ajoute le buff
    caster.addBuff(Constant.STATS_ADD_FORC,vol,turns,1,true,spell,args,caster,false);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_ADD_FORC,caster.getId()+"",caster.getId()+","+vol+","+turns);
  }

  private void applyEffect_293(Fight fight)
  {
    caster.addBuff(effectID,value,turns,1,false,spell,args,caster,false);
    caster.setState(300,turns+1,caster.getId());
  }

  private void applyEffect_320(Fight fight, ArrayList<Fighter> cibles)
  {
    int value=1;
    try
    {
      value=Integer.parseInt(args.split(";")[0]);
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
    }
    int num=0;
    for(Fighter target : cibles)
    {
      target.addBuff(Constant.STATS_REM_PO,value,turns,0,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_REM_PO,caster.getId()+"",target.getId()+","+value+","+turns);
      num+=value;
    }
    if(num!=0)
    {
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,Constant.STATS_ADD_PO,caster.getId()+"",caster.getId()+","+num+","+turns);
      caster.addBuff(Constant.STATS_ADD_PO,num,1,0,true,spell,args,caster,false);
      //Gain de PO pendant le tour de jeu
      if(caster.canPlay())
        caster.getTotalStats().addOneStat(Constant.STATS_ADD_PO,num);
    }
  }

  private void applyEffect_400(Fight fight)
  {
    if(!cell.isWalkable(false))
      return; //Si case pas marchable
    if(cell.getFirstFighter()!=null)
      return; //Si la case est prise par un joueur

    //Si la case est prise par le centre d'un piege
    for(Trap p : fight.getAllTraps()){
      if(p.getCell().getId()==cell.getId()) {
    	  if(caster.getPersonnage() != null)
    		SocketManager.GAME_SEND_MESSAGE(caster.getPersonnage(),"Erreur","CD2B2E");
        return;
      }
    }
    String[] infos=args.split(";");
    int spellID=Short.parseShort(infos[0]);
    int level=Byte.parseByte(infos[1]);
    String po=Main.world.getSort(spell).getStatsByLevel(spellLvl).getPorteeType();
    byte size=(byte)Main.world.getCryptManager().getIntByHashedValue(po.charAt(1));
    SortStats TS=Main.world.getSort(spellID).getStatsByLevel(level);
    Trap g=new Trap(fight,caster,cell,size,TS,spell);
    fight.getAllTraps().add(g);
    int unk=g.getColor();
    int team=caster.getTeam()+1;
    String str="GDZ+"+cell.getId()+";"+size+";"+unk;
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,team,999,caster.getId()+"",str);
    str="GDC"+cell.getId()+";Haaaaaaaaz3005;";
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,team,999,caster.getId()+"",str);
    caster.setJustTrapped(true);
  }

  //v2.7 - glyph trap fix
  private void applyEffect_401(Fight fight)
  {
    if(!cell.isWalkable(false))
      return;//Si case pas marchable
    /*if(cell.getFirstFighter()!=null)
      return;//Si la case est prise par un joueur*/

    for(Trap p : fight.getAllTraps())
      if(p.getCell().getId()==cell.getId())
        return;

    String[] infos=args.split(";");
    int spellID=Short.parseShort(infos[0]);
    int level=Byte.parseByte(infos[1]);
    byte duration=Byte.parseByte(infos[3]);
    String po=Main.world.getSort(spell).getStatsByLevel(spellLvl).getPorteeType();
    byte size=(byte)Main.world.getCryptManager().getIntByHashedValue(po.charAt(1));
    SortStats TS=Main.world.getSort(spellID).getStatsByLevel(level);
    Glyph g=new Glyph(fight,caster,cell,size,TS,duration,spell);
    fight.getAllGlyphs().add(g);
    if(spell==17&&fight.getAllGlyphs().size()>1) //move excursion glyph forward
      Collections.swap(fight.getAllGlyphs(),0,fight.getAllGlyphs().size()-1);
    int unk=g.getColor();
    String str="GDZ+"+cell.getId()+";"+size+";"+unk;
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"",str);
    str="GDC"+cell.getId()+";Haaaaaaaaa3005;";
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"",str);
  }

  private void applyEffect_402(Fight fight)
  {
    if(!cell.isWalkable(false))
      return;//Si case pas marchable

    String[] infos=args.split(";");
    int spellID=Short.parseShort(infos[0]);
    int level=Byte.parseByte(infos[1]);
    byte duration=Byte.parseByte(infos[3]);
    String po=Main.world.getSort(spell).getStatsByLevel(spellLvl).getPorteeType();
    byte size=(byte)Main.world.getCryptManager().getIntByHashedValue(po.charAt(1));
    SortStats TS=Main.world.getSort(spellID).getStatsByLevel(level);
    Glyph g=new Glyph(fight,caster,cell,size,TS,duration,spell);
    fight.getAllGlyphs().add(g);
    int unk=g.getColor();
    String str="GDZ+"+cell.getId()+";"+size+";"+unk;
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"",str);
    str="GDC"+cell.getId()+";Haaaaaaaaa3005;";
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"",str);
  }

  @SuppressWarnings("unlikely-arg-type")
  private void applyEffect_671(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<=0)
    {
      for(Fighter target : cibles)
      {
        if(target.hasBuff(765))
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          target=caster;
        }
        int resP=target.getPersonnage() != null  ?target.getTotalStats().getEffect(Constant.STATS_ADD_RP_NEU): 50,resF=target.getPersonnage() != null  ?target.getTotalStats().getEffect(Constant.STATS_ADD_R_NEU): 50;

        if(target.getPersonnage()!=null)
        {
          resP+=target.getPersonnage() != null  ?target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_NEU): 50;
          resF+=target.getPersonnage() != null  ?target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_NEU): 50;
        }

        int dmg=Formulas.getRandomJet(args.split(";")[5]);// % de pdv
        dmg=getMaxMinSpell(target,dmg);
        int val=caster.getPdv()/100*dmg;// Valor de daï¿½os
        val-=resF;
        int reduc=(int)(((float)val)/(float)100)*resP;// Reduc
        // %resis
        val-=reduc;
        if(val<0)
          val=0;
        val=applyOnHitBuffs(val,target,caster,fight,Constant.ELEMENT_NULL,spell);
        if(val>target.getPdv())
          val=target.getPdv();
        target.removePdv(caster,val);
        target.removePdvMax((int)Math.floor(val*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=val;
        if(target.hasBuff(786)&&target.getBuff(786)!=null)
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        val=-(val);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+val);
        if(target.getPdv()<=0)
          fight.getDeadList().remove(target);
      }
    }
    else
    {
      caster.addBuff(effectID,0,turns,0,true,spell,args,caster,false);
    }
  }

  private void applyEffect_672(ArrayList<Fighter> cibles, Fight fight)
  {
    double val=((double)Formulas.getRandomJet(jet)/(double)100);
    int pdvMax=caster.getPdvMaxOutFight();
    double pVie=(double)caster.getPdv()/(double)caster.getPdvMax();
    double rad=(double)2*Math.PI*(double)(pVie-0.5);
    double cos=Math.cos(rad);
    double taux=(Math.pow((cos+1),2))/(double)4;
    double dgtMax=val*pdvMax;
    int dgt=(int)(taux*dgtMax);
    for(Fighter target : cibles)
    {
      if(target.hasBuff(765))//sacrifice
      {
        if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
        {
          applyEffect_765B(fight,target);
          target=target.getBuff(765).getCaster();
        }
      }
      //si la cible a le buff renvoie de sort
      if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl)
      {
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
        //le lanceur devient donc la cible
        target=caster;
      }
     // int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_NEUTRE,dgt,false,true,spell,this.cell,target.getCell(),aoe,isTrap);
      int finalDommage=applyOnHitBuffs(dgt,target,caster,fight,Constant.ELEMENT_NEUTRE,spell); //S'il y a des buffs spï¿½ciaux
      int resi=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_NEU);
      int retir=0;
      if(resi>2)
      {
        retir=(finalDommage*resi)/100;
        finalDommage=finalDommage-retir;
      }
      if(resi<-2)
      {
        retir=((-finalDommage)*(-resi))/100;
        finalDommage=finalDommage+retir;
      }
      if(finalDommage>target.getPdv())
        finalDommage=target.getPdv();//Target va mourrir
      target.removePdv(caster,finalDommage);
      target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
      finalDommage=-(finalDommage);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);

      if(target.getPdv()<=0)
      {
        fight.onFighterDie(target,target);
        if(target.canPlay()&&target.getPersonnage()!=null)
          fight.endTurn(false);
        else if(target.canPlay())
          target.setCanPlay(false);
      }
    }
  }

  private void applyEffect_765(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,0,turns,1,true,spell,args,caster,true);
    }
  }

  public static void applyEffect_765B(Fight fight, Fighter target)
  {
    Fighter sacrified=target.getBuff(765).getCaster();
    GameCase cell1=sacrified.getCell();
    GameCase cell2=target.getCell();

    sacrified.getCell().getFighters().clear();
    target.getCell().getFighters().clear();
    sacrified.setCell(cell2);
    sacrified.getCell().addFighter(sacrified);
    target.setCell(cell1);
    target.getCell().addFighter(target);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,4,target.getId()+"",target.getId()+","+cell1.getId());
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,4,sacrified.getId()+"",sacrified.getId()+","+cell2.getId());
  }

  private void applyEffect_776(ArrayList<Fighter> objetivos, Fight pelea)
  {
    int val=Formulas.getRandomJet(jet);
    if(val==-1)
    {
      return;
    }
    for(Fighter objetivo : objetivos)
    {
      objetivo.addBuff(effectID,val,turns,1,true,spell,args,caster,true);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea,7,effectID,caster.getId()+"",objetivo.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_780(Fight fight)
  {
    Fighter target=null;

    for(int i=fight.getDeadList().size()-1;i>=0;i--)
    {
      Fighter fighter=fight.getDeadList().get(i).getRight();
      if(!fighter.hasLeft()&&fighter.getTeam()==caster.getTeam())
      {
        target=fighter;
        break;
      }
    }

    if(target==null)
      return;

    fight.addFighterInTeam(target,target.getTeam());
    target.setIsDead(false);
    target.getFightBuff().clear();
    target.setInvocator(caster);
    if(target.isInvocation()&&target.getPersonnage()==null)
      fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(target.getInvocator())+1),target);

    target.setCell(cell);
    target.getCell().addFighter(target);

    target.fullPdv();
    int percent=(100-value)*target.getPdvMax()/100;
    target.removePdv(target,percent);
    target.removePdvMax((int)Math.floor(percent*(Config.getInstance().erosion+target.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-target.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
    String gm=target.getGmPacket('+',true).substring(3);
    String gtl=fight.getGTL();
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,181,target.getId()+"",gm);
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,target.getId()+"",gtl);
    if(!target.isInvocation())
      SocketManager.GAME_SEND_STATS_PACKET(target.getPersonnage());

    fight.removeDead(target);
    verifyTraps(fight,target);
  }

  private void applyEffect_781(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,value,turns,1,debuffable,spell,args,caster,true);
    }
  }

  private void applyEffect_782(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,value,turns,1,debuffable,spell,args,caster,true);
    }
  }

  private void applyEffect_783(ArrayList<Fighter> cibles, Fight fight)
  {
    //Pousse jusqu'a la case visÃ©e
    GameCase ccase=caster.getCell();
    //On calcule l'orientation entre les 2 cases
    char dir=PathFinding.getDirBetweenTwoCase(ccase.getId(),cell.getId(),fight.getMap(),true);
    //On calcule l'id de la case a cotÃ© du lanceur dans la direction obtenue
    int tcellID=PathFinding.GetCaseIDFromDirrection(ccase.getId(),dir,fight.getMap(),true);
    //on prend la case corespondante
    GameCase tcase=fight.getMap().getCase(tcellID);

    if(tcase==null)
      return;
    //S'il n'y a personne sur la case, on arrete
    if(tcase.getFighters().isEmpty())
      return;
    //On prend le Fighter ciblÃ©
    Fighter target=tcase.getFirstFighter();
    //On verifie qu'il peut aller sur la case ciblÃ© en ligne droite
    int c1=tcellID,limite=0;
    if(target.getMob()!=null)
      for(int i : Constant.STATIC_INVOCATIONS)
        if(i==target.getMob().getTemplate().getId())
          return;

    while(true)
    {
      if(PathFinding.GetCaseIDFromDirrection(c1,dir,fight.getMap(),true)==cell.getId())
        break;
      if(PathFinding.GetCaseIDFromDirrection(c1,dir,fight.getMap(),true)==-1)
        return;
      c1=PathFinding.GetCaseIDFromDirrection(c1,dir,fight.getMap(),true);
      limite++;
      if(limite>50)
        return;
    }
    GameCase newCell=PathFinding.checkIfCanPushEntity(fight,ccase.getId(),cell.getId(),dir);

    if(newCell!=null)
      cell=newCell;

    target.getCell().getFighters().clear();
    target.setCell(cell);
    target.getCell().addFighter(target);

    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,5,caster.getId()+"",target.getId()+","+cell.getId());
    verifyTraps(fight,target);
  }

  private void applyEffect_784(ArrayList<Fighter> cibles, Fight fight)
  {
    Map<Integer, GameCase> origPos=fight.getRholBack(); // les positions de dÃ©but de combat

    ArrayList<Fighter> list=fight.getFighters(3); // on copie la liste des fighters
    for(int i=1;i<list.size();i++) // on boucle si tout le monde est Ã  la place
      if(!list.isEmpty()) // d'un autre
        for(Fighter F : list)
        {
          if(F==null||F.isDead()||!origPos.containsKey(F.getId()))
          {
            continue;
          }
          if(F.getCell().getId()==origPos.get(F.getId()).getId())
          {
            continue;
          }
          if(origPos.get(F.getId()).getFirstFighter()==null)
          {
            F.getCell().getFighters().clear();
            F.setCell(origPos.get(F.getId()));
            F.getCell().addFighter(F);
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,4,F.getId()+"",F.getId()+","+F.getCell().getId());
          }
        }
  }

  private void applyEffect_786(ArrayList<Fighter> objetivos, Fight pelea)
  {
    for(Fighter objetivo : objetivos)
      objetivo.addBuff(effectID,value,turns,1,true,spell,args,caster,true);
  }

  private void applyEffect_787(ArrayList<Fighter> objetivos, Fight pelea)
  {
    int hechizoID=-1;
    int hechizoNivel=-1;
    try
    {
      hechizoID=Integer.parseInt(args.split(";")[0]);
      hechizoNivel=Integer.parseInt(args.split(";")[1]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    Spell hechizo=Main.world.getSort(hechizoID);
    ArrayList<SpellEffect> EH=hechizo.getStatsByLevel(hechizoNivel).getEffects();
    for(SpellEffect eh : EH)
    {
      for(Fighter objetivo : objetivos)
      {
        objetivo.addBuff(eh.effectID,eh.value,1,1,true,eh.spell,eh.args,caster,true);
      }
    }
  }

  private void applyEffect_788(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
    {
      target.addBuff(effectID,value,turns,1,false,spell,args,target,true);
    }
  }

  private void applyEffect_950(Fight fight, ArrayList<Fighter> cibles)
  {
    int id=-1;
    try
    {
      id=Integer.parseInt(args.split(";")[2]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if(id==-1)
      return;
    if(id==31||id==32||id==33||id==34)
    {
      turns=2;
      for(Entry<Integer, Fighter> entry : fight.getTeam1().entrySet())
      {
        Fighter mob=entry.getValue();
        mob.setState(id,turns,caster.getId());
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",mob.getId()+","+id+",1");
        mob.addBuff(effectID,value,turns,1,false,spell,args,mob,true);
      }

      if(id==34)
        for(Fighter target : cibles)
          target.addBuff(140,0,1,0,true,1102,"",caster,false);
    }

    for(Fighter target : cibles)
    {
      if(spell==139&&target.getTeam()!=caster.getTeam()) //Mot d'altruisme on saute les ennemis ?
      {
        continue;
      }
      
      if(turns<=0)
      {
        target.setState(id,turns,caster.getId());
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+id+",1");
      }
      else
      {
        if(id==Constant.STATE_DRUNK)
          target.setState(Constant.STATE_SOBER,0,caster.getId()); //duration 0, remove state
        target.setState(id,turns,caster.getId());
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+id+",1");
        if(spell==20&&caster==target)
          turns--;
        target.addBuff(effectID,value,turns,1,false,spell,args,caster,true);
      }
    }
  }

  private void applyEffect_951(Fight fight, ArrayList<Fighter> cibles)
  {
    int id=-1;
    try
    {
      id=Integer.parseInt(args.split(";")[2]);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    if(id==-1)
      return;

    for(Fighter target : cibles)
    {
      if(!target.haveState(id))
        continue;

      if(target.haveState(Constant.STATE_DRUNK))
      {
        target.setState(Constant.STATE_SOBER,-1,caster.getId()); //infinite duration
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,target.getId()+"",target.getId()+","+Constant.STATE_SOBER+",1");
      }

      target.setState(id,0,caster.getId());
    }
  }

  private void applyEffect_1000(Fight fight, ArrayList<Fighter> cibles)
  {
    String[] infos=args.split(";");
    int spellID=Short.parseShort(infos[0]);
    int level=Byte.parseByte(infos[1]);
    byte duration=1;
    SortStats TS=Main.world.getSort(spellID).getStatsByLevel(level);

    GameCase baseCell=this.caster.getCell();
    GameCase UnsafeBorderCell=fight.getMap().getCase(baseCell.getId()%(2*fight.getMap().getW()-1));
    int last=fight.getMap().getW();
    int currentCell=UnsafeBorderCell.getId();
    int loops=0;
    int curloop=0;
    boolean fifteen=true;
    boolean fourteen=false;
    boolean stop=false;
    while(!stop)
    {
      if(currentCell<=fight.getMap().getW())
        stop=true;
      currentCell-=last;

      if(last==fight.getMap().getW()-1)
        last=fight.getMap().getW();
      if(last==fight.getMap().getW())
        last=fight.getMap().getW()-1;
      if(!stop)
        loops++;
    }

    for(GameCase entry : fight.getMap().getCases())
    {
      curloop++;
      if(fifteen&&curloop==fight.getMap().getW())
      {
        fourteen=true;
        fifteen=false;
        curloop=0;
      }
      else if(fourteen&&curloop==fight.getMap().getW()-1)
      {
        fourteen=false;
        fifteen=true;
        curloop=0;
      }

      if(entry.isWalkable(false))
      {
        if(loops%2==0&&fifteen) //if even
        {
          Glyph g=new Glyph(fight,caster,entry,(byte)0,TS,duration,spell);
          fight.getAllGlyphs().add(g);

          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"","GDZ+"+entry.getId()+";"+0+";"+g.getColor());
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"","GDC"+entry.getId()+";Haaaaaaaaa3005;");
        }
        else if(loops%2!=0&&fourteen) //if odd
        {
          Glyph g=new Glyph(fight,caster,entry,(byte)0,TS,duration,spell);
          fight.getAllGlyphs().add(g);

          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"","GDZ+"+entry.getId()+";"+0+";"+g.getColor());
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"","GDC"+entry.getId()+";Haaaaaaaaa3005;");
        }
      }
    }
  }

  private void applyEffect_1001(Fight fight, ArrayList<Fighter> cibles)
  {
    String[] infos=args.split(";");
    int spellID=Short.parseShort(infos[0]);
    int level=Byte.parseByte(infos[1]);
    byte duration=1;
    SortStats TS=Main.world.getSort(spellID).getStatsByLevel(level);

    GameCase baseCell=this.caster.getCell();
    GameCase UnsafeBorderCell=fight.getMap().getCase(baseCell.getId()%(2*fight.getMap().getW()-1));
    int last=fight.getMap().getW();
    int currentCell=UnsafeBorderCell.getId();
    int loops=0;
    int curloop=0;
    boolean fifteen=true;
    boolean fourteen=false;
    boolean stop=false;
    while(!stop)
    {
      if(currentCell<=fight.getMap().getW())
        stop=true;
      currentCell-=last;

      if(last==fight.getMap().getW()-1)
        last=fight.getMap().getW();
      if(last==fight.getMap().getW())
        last=fight.getMap().getW()-1;
      if(!stop)
        loops++;
    }

    for(GameCase entry : fight.getMap().getCases())
    {
      curloop++;
      if(fifteen&&curloop==fight.getMap().getW())
      {
        fourteen=true;
        fifteen=false;
        curloop=0;
      }
      else if(fourteen&&curloop==fight.getMap().getW()-1)
      {
        fourteen=false;
        fifteen=true;
        curloop=0;
      }

      if(entry.isWalkable(false))
      {
        if(loops%2==0&&fourteen) //if even
        {
          Glyph g=new Glyph(fight,caster,entry,(byte)0,TS,duration,spell);
          fight.getAllGlyphs().add(g);

          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"","GDZ+"+entry.getId()+";"+0+";"+g.getColor());
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"","GDC"+entry.getId()+";Haaaaaaaaa3005;");
        }
        else if(loops%2!=0&&fifteen) //if odd
        {
          Glyph g=new Glyph(fight,caster,entry,(byte)0,TS,duration,spell);
          fight.getAllGlyphs().add(g);

          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"","GDZ+"+entry.getId()+";"+0+";"+g.getColor());
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"","GDC"+entry.getId()+";Haaaaaaaaa3005;");
        }
      }
    }
  }

  private void applyEffect_1002(Fight fight, ArrayList<Fighter> cibles)
  {
    String[] infos=args.split(";");
    int spellID=Short.parseShort(infos[0]);
    int level=Byte.parseByte(infos[1]);
    byte duration=100;
    SortStats TS=Main.world.getSort(spellID).getStatsByLevel(level);

    if(cell.isWalkable(false)&&!fight.isOccuped(cell.getId()))
    {
      caster.getCell().getFighters().clear();
      caster.setCell(cell);
      caster.getCell().addFighter(caster);
      /* new ArrayList<>(fight.getAllTraps()).stream().filter(trap -> PathFinding.getDistanceBetween(fight.getMap(),trap.getCell().getId(),caster.getCell().getId())<=trap.getSize()).forEach(trap -> trap.onTraped(caster));
      */SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,4,caster.getId()+"",caster.getId()+","+cell.getId());
      verifyTraps(fight,caster);
    }

    Glyph g=new Glyph(fight,caster,cell,(byte)0,TS,duration,spell);
    fight.getAllGlyphs().add(g);
    int unk=g.getColor();
    String str="GDZ+"+cell.getId()+";"+0+";"+unk;
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"",str);
    str="GDC"+cell.getId()+";Haaaaaaaaa3005;";
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,999,caster.getId()+"",str);
  }

  private void applyEffect_1003(ArrayList<Fighter> cibles, Fight fight)
  {
    int casterPdv=this.caster.getPdv();
    for(Fighter target : cibles)
    {
      if(target.hasBuff(765))//sacrifice
      {
        if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
        {
          applyEffect_765B(fight,target);
          target=target.getBuff(765).getCaster();
        }
      }
      //si la cible a le buff renvoie de sort
      if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
      {
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
        target=caster;
      }
      int resP=target.getPersonnage() != null  ?target.getTotalStats().getEffect(Constant.STATS_ADD_RP_NEU): 50;
      int resF=target.getPersonnage() != null  ?target.getTotalStats().getEffect(Constant.STATS_ADD_R_NEU): 50;
      if(target.getPersonnage()!=null) //Si c'est un joueur, on ajoute les resists bouclier
      {
        resP+=target.getPersonnage() != null  ?target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_NEU): 50;
        resF+=target.getPersonnage() != null  ?target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_NEU): 50;
      }
      int dmg=33; //%age de pdv infligï¿½
      int val=(int)Math.floor((casterPdv/(float)100*(float)dmg)); //Valeur des dï¿½gats
      val-=resF; //retrait de la rï¿½sist fixe
      int reduc=(int)(((float)val)/(float)100)*resP; //Reduc %resis
      val-=reduc;
      int armor=0;
      for(SpellEffect SE : target.getBuffsByEffectID(105))
      {
        int[] stats= { target.getTotalStats().getEffect(118), target.getTotalStats().getEffect(126), target.getTotalStats().getEffect(123), target.getTotalStats().getEffect(119) };
        int highest=0;
        for(int i=0;i<stats.length-1;i++)
          if(stats[i]>highest)
            highest=stats[i];
        final int value=SE.getValue();
        int carac=target.getTotalStats().getEffect(Constant.STATS_ADD_FORC);
        final int a=value*(100+highest/2+carac/2)/100;
        armor+=a;
      }
      if(armor>0)
      {
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+armor);
        val=val-armor;
      }
      if(val<0)
        val=0;
      val=applyOnHitBuffs(val,target,caster,fight,Constant.ELEMENT_NULL,spell);//S'il y a des buffs spï¿½ciaux
      if(val>target.getPdv())
        val=target.getPdv();//Target va mourrir
      target.removePdv(caster,val);
      target.removePdvMax((int)Math.floor(val*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
      int cura=val;
      if(target.hasBuff(786))
      {
        if((cura+caster.getPdv())>caster.getPdvMax())
          cura=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-cura);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
      }
      val=-(val);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+val);
      if(target.getPdv()<=0)
      {
        fight.onFighterDie(target,caster);
        if(target.canPlay()&&target.getPersonnage()!=null)
          fight.endTurn(false);
        else if(target.canPlay())
          target.setCanPlay(false);
      }
    }
  }

  //% max hp vitality buff
  private void applyEffect_1008(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    if(val<0)
      return;

    for(Fighter target : cibles)
    {
      val=target.getPdvMax()*val/100;
      if(val>0)
      {
        target.addBuff(125,val,turns,1,true,spell,args,caster,false);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,125,caster.getId()+"",target.getId()+","+val+","+turns);
      }
    }
  }

  //+% erosion resistance
  private void applyEffect_1011(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    for(Fighter target : cibles)
    {
      target.addBuff(1011,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,1011,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  //-% erosion resistance
  private void applyEffect_1012(ArrayList<Fighter> cibles, Fight fight)
  {
    int val=Formulas.getRandomJet(jet);
    for(Fighter target : cibles)
    {
      target.addBuff(1012,val,turns,1,true,spell,args,caster,false);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,1011,caster.getId()+"",target.getId()+","+val+","+turns);
    }
  }

  //attract caster to target
  private void applyEffect_1013(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<=0)
    {
      for(Fighter target : cibles)
      {
        if(caster.haveState(6))
          continue;
        GameCase eCell=cell;
        if(target.getCell().getId()==cell.getId())
        {
          eCell=target.getCell();
        }

        int newCellID=PathFinding.newCaseAfterPush(fight,eCell,caster.getCell(),-value);
        if(newCellID==0)
          continue;

        if(newCellID<0) //S'il a ï¿½tï¿½ bloquï¿½
        {
          int a=-(value+newCellID);
          newCellID=PathFinding.newCaseAfterPush(fight,target.getCell(),caster.getCell(),a);
          if(newCellID==0)
            continue;
          if(fight.getMap().getCase(newCellID)==null)
            continue;
        }

        caster.getCell().getFighters().clear();
        caster.setCell(fight.getMap().getCase(newCellID));
        caster.getCell().addFighter(caster);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,5,target.getId()+"",caster.getId()+","+newCellID);

        /* ArrayList<Trap> P=(new ArrayList<Trap>());
        P.addAll(fight.getAllTraps());
        for(Trap p : P)
        {
          int dist=PathFinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),caster.getCell().getId());
          if(dist<=p.getSize())
            p.onTraped(caster);
        }*/
        verifyTraps(fight,caster);
      }
    }
  }

  //v2.8 - Debugged limited damage by missing target hp
  //water damage heals allies around target equal to damage done
  private void applyEffect_1014(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns<=0)
    {
      if(caster.isHide())
        caster.unHide(spell);
      for(Fighter target : cibles)
      {
        if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
          continue; //Les monstres de s'entretuent pas

        if(target.hasBuff(765)) //sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort
        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }

        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_EAU,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);
        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_EAU,spell); //S'il y a des buffs spï¿½ciaux
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;
        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        if(target.getMob()!=null)
          verifmobs(fight,target,96,finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+(-finalDommage));

        ArrayList<Fighter> allies=new ArrayList<Fighter>();
        GameCase cell1=fight.getMap().getCase(PathFinding.GetCaseIDFromDirrection(target.getCell().getId(),'b',fight.getMap(),false));
        GameCase cell2=fight.getMap().getCase(PathFinding.GetCaseIDFromDirrection(target.getCell().getId(),'d',fight.getMap(),false));
        GameCase cell3=fight.getMap().getCase(PathFinding.GetCaseIDFromDirrection(target.getCell().getId(),'f',fight.getMap(),false));
        GameCase cell4=fight.getMap().getCase(PathFinding.GetCaseIDFromDirrection(target.getCell().getId(),'h',fight.getMap(),false));
        if(cell1.getFirstFighter()!=null)
          if(cell1.getFirstFighter().getTeam()==caster.getTeam()&&cell1.getFirstFighter()!=caster)
            allies.add(cell1.getFirstFighter());
        if(cell2.getFirstFighter()!=null)
          if(cell2.getFirstFighter().getTeam()==caster.getTeam()&&cell2.getFirstFighter()!=caster)
            allies.add(cell2.getFirstFighter());
        if(cell3.getFirstFighter()!=null)
          if(cell3.getFirstFighter().getTeam()==caster.getTeam()&&cell3.getFirstFighter()!=caster)
            allies.add(cell3.getFirstFighter());
        if(cell4.getFirstFighter()!=null)
          if(cell4.getFirstFighter().getTeam()==caster.getTeam()&&cell4.getFirstFighter()!=caster)
            allies.add(cell4.getFirstFighter());
        for(Fighter ally : allies)
        {
          int heal=finalDommage;
          if(ally.isDead())
            continue;
          if(caster.hasBuff(178))
            heal+=caster.getBuffValue(178);
          if(caster.hasBuff(179))
            heal=heal-caster.getBuffValue(179);
          int pdvMax=ally.getPdvMax();
          if((heal+ally.getPdv())>pdvMax)
            heal=pdvMax-ally.getPdv();
          if(heal<1)
            heal=0;
          ally.removePdv(caster,-heal);
          if(heal!=0)
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,108,caster.getId()+"",ally.getId()+","+heal);
        }
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else
    {
      for(Fighter target : cibles)
        target.addBuff(effectID,value,turns,1,true,spell,args,caster,false); //on applique un buff
    }
  }

  //earth damage +5 base damage for every mp caster has
  private void applyEffect_1015(ArrayList<Fighter> cibles, Fight fight)
  {
    if(caster.isHide())
      caster.unHide(spell);
    for(Fighter target : cibles)
    {
      if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
        continue; // Les monstres de s'entretuent pas

      if(target.hasBuff(765))//sacrifice
      {
        if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
        {
          applyEffect_765B(fight,target);
          target=target.getBuff(765).getCaster();
        }
      }
      //si la cible a le buff renvoie de sort
      if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
      {
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
        target=caster;
      }
      int dmg=0;
      if(target.hasBuff(782)) //Brokle
      {
        dmg=Formulas.getMaxJet(args.split(";")[5]);
      }
      else if(target.hasBuff(781)) //Jinx
      {
        dmg=Formulas.getMinJet(args.split(";")[5]);
      }
      else
        dmg=Formulas.getRandomJet(args.split(";")[5]);
      dmg=dmg+5*caster.getCurPm(caster.getFight());

      //Si le sort est boostï¿½ par un buff spï¿½cifique
      if(caster.hasBuff(293)||caster.haveState(300))
      {
        if(caster.haveState(300))
          caster.setState(300,0,caster.getId());
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE==null)
            continue;
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }
      }

      int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_TERRE,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);
      finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_TERRE,spell);//S'il y a des buffs spï¿½ciaux
      if(finalDommage>target.getPdv())
        finalDommage=target.getPdv();//Target va mourrir
      target.removePdv(caster,finalDommage);
      target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
      int cura=finalDommage;

      if(target.hasBuff(786))
      {
        if((cura+caster.getPdv())>caster.getPdvMax())
          cura=caster.getPdvMax()-caster.getPdv();
        caster.removePdv(caster,-cura);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
      }
      finalDommage=-(finalDommage);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
      if(target.getMob()!=null)
        verifmobs(fight,target,97,cura);
      if(target.getPdv()<=0)
      {
        fight.onFighterDie(target,caster);
        if(target.canPlay()&&target.getPersonnage()!=null)
          fight.endTurn(false);
        else if(target.canPlay())
          target.setCanPlay(false);
      }

    }
  }

  private void applyEffect_1016(ArrayList<Fighter> cibles, Fight fight)
  {
    int bonus=0;
    for(Fighter target : cibles)
    {
      if(target.hasBuff(Constant.STATS_REM_PA))
        bonus+=target.getBuff(Constant.STATS_REM_PA).getValue();
      if(target.hasBuff(Constant.STATS_ADD_PA))
        bonus+=target.getBuff(Constant.STATS_ADD_PA).getValue();
      target.debuff();
      if(target.isHide())
        target.unHide(spell);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,132,caster.getId()+"",target.getId()+"");
    }
    caster.addBuff(Constant.STATS_ADD_PA,bonus,turns,1,true,spell,args,caster,false);
    fight.endTurn(false);
  }

  private void applyEffect_1017(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
      target.addBuff(effectID,value,turns,1,true,spell,args,caster,true);
  }

  private void applyEffect_1018(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
    {
      int dmg=0;
      if(target.hasBuff(782)) //Brokle
      {
        dmg=Formulas.getMaxJet(args.split(";")[5]);
      }
      else if(target.hasBuff(781)) //Jinx
      {
        dmg=Formulas.getMinJet(args.split(";")[5]);
      }
      else
        dmg=Formulas.getRandomJet(args.split(";")[5]);
      target.addBuff(effectID,dmg,turns,0,true,spell,args,caster,false);
    }
  }

  private void applyEffect_1019(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
    {
      int dmg=0;
      if(target.hasBuff(782)) //Brokle
      {
        dmg=Formulas.getMaxJet(args.split(";")[5]);
      }
      else if(target.hasBuff(781)) //Jinx
      {
        dmg=Formulas.getMinJet(args.split(";")[5]);
      }
      else
        dmg=Formulas.getRandomJet(args.split(";")[5]);
      target.addBuff(effectID,dmg,turns,0,true,spell,args,caster,false);
    }
  }

  private void applyEffect_1020(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
    {
      int dmg=0;
      if(target.hasBuff(782)) //Brokle
      {
        dmg=Formulas.getMaxJet(args.split(";")[5]);
      }
      else if(target.hasBuff(781)) //Jinx
      {
        dmg=Formulas.getMinJet(args.split(";")[5]);
      }
      else
        dmg=Formulas.getRandomJet(args.split(";")[5]);
      target.addBuff(effectID,dmg,turns,0,true,spell,args,caster,false);
    }
  }

  private void applyEffect_1021(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
    {
      int dmg=0;
      if(target.hasBuff(782)) //Brokle
      {
        dmg=Formulas.getMaxJet(args.split(";")[5]);
      }
      else if(target.hasBuff(781)) //Jinx
      {
        dmg=Formulas.getMinJet(args.split(";")[5]);
      }
      else
        dmg=Formulas.getRandomJet(args.split(";")[5]);
      target.addBuff(effectID,dmg,turns,0,true,spell,args,caster,false);
    }
  }

  private void applyEffect_1022(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
      target.addBuff(effectID,value,turns,1,true,spell,args,caster,false);
  }

  private void applyEffect_1023(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
      target.addBuff(effectID,value,turns,1,true,spell,args,caster,false);
  }

  private void applyEffect_1024(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
      target.addBuff(effectID,value,turns,1,true,spell,args,caster,false);
  }

  private void applyEffect_1025(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
      target.addBuff(effectID,value,turns,1,true,spell,args,caster,false);
  }

  private void applyEffect_1026(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
      target.addBuff(effectID,value,turns,duration,false,spell,args,caster,true);
  }

  private void applyEffect_1027(ArrayList<Fighter> cibles, Fight fight, boolean isCaC) //dmg terre
  {
    if(isCaC)//CaC Terre
    {
      if(caster.isHide())
        caster.unHide(spell);
      for(Fighter target : cibles)
      {
        target=caster;

        if(caster.isMob())
        {
          if(caster.getTeam2()==target.getTeam2()&&!caster.isInvocation())
            continue; // Les monstres de s'entretuent pas
        }

        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);

        //Si le sort est boostï¿½ par un buff spï¿½cifique
        for(SpellEffect SE : caster.getBuffsByEffectID(293))
        {
          if(SE.getValue()==spell)
          {
            int add=-1;
            try
            {
              add=Integer.parseInt(SE.getArgs().split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(add<=0)
              continue;
            dmg+=add;
          }
        }
        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_TERRE,dmg,false,true,spell,this.cell,target.getCell(),aoe,isTrap);

        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_TERRE,spell); //S'il y a des buffs spï¿½ciaux

        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv(); //Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;

        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }

        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
        if(target.getMob()!=null)
          verifmobs(fight,target,97,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }
      }
    }
    else if(turns<=0)
    {
      if(caster.isHide())
        caster.unHide(spell);
      for(Fighter target : cibles)
      {
        if(caster.isMob()&&(caster.getTeam2()==target.getTeam2())&&!caster.isInvocation())
          continue; // Les monstres de s'entretuent pas

        if(target.hasBuff(765))//sacrifice
        {
          if(target.getBuff(765)!=null&&!target.getBuff(765).getCaster().isDead())
          {
            applyEffect_765B(fight,target);
            target=target.getBuff(765).getCaster();
          }
        }
        //si la cible a le buff renvoie de sort

        if(target.hasBuff(106)&&target.getBuffValue(106)>=spellLvl&&spell!=0)
        {
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,106,target.getId()+"",target.getId()+",1");
          //le lanceur devient donc la cible
          target=caster;
        }
        int dmg=0;
        if(target.hasBuff(782)) //Brokle
        {
          dmg=Formulas.getMaxJet(args.split(";")[5]);
        }
        else if(target.hasBuff(781)) //Jinx
        {
          dmg=Formulas.getMinJet(args.split(";")[5]);
        }
        else
          dmg=Formulas.getRandomJet(args.split(";")[5]);

        //Si le sort est boostï¿½ par un buff spï¿½cifique
        if(caster.hasBuff(293)||caster.haveState(300))
        {
          if(caster.haveState(300))
            caster.setState(300,0,caster.getId());
          for(SpellEffect SE : caster.getBuffsByEffectID(293))
          {
            if(SE==null)
              continue;
            if(SE.getValue()==spell)
            {
              int add=-1;
              try
              {
                add=Integer.parseInt(SE.getArgs().split(";")[2]);
              }
              catch(Exception e)
              {
                e.printStackTrace();
              }
              if(add<=0)
                continue;
              dmg+=add;
            }
          }
        }

        int finalDommage=Formulas.calculFinalDommage(fight,caster,target,Constant.ELEMENT_TERRE,dmg,false,false,spell,this.cell,target.getCell(),aoe,isTrap);
        finalDommage=applyOnHitBuffs(finalDommage,target,caster,fight,Constant.ELEMENT_TERRE,spell);//S'il y a des buffs spï¿½ciaux
        if(finalDommage>target.getPdv())
          finalDommage=target.getPdv();//Target va mourrir
        target.removePdv(caster,finalDommage);
        target.removePdvMax((int)Math.floor(finalDommage*(Config.getInstance().erosion+caster.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-caster.getTotalStats().getEffect(Constant.STATS_REM_ERO)-target.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+target.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        int cura=finalDommage;

        if(target.hasBuff(786))
        {
          if((cura+caster.getPdv())>caster.getPdvMax())
            cura=caster.getPdvMax()-caster.getPdv();
          caster.removePdv(caster,-cura);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,target.getId()+"",caster.getId()+",+"+cura);
        }
        finalDommage=-(finalDommage);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",target.getId()+","+finalDommage);
        if(target.getMob()!=null)
          verifmobs(fight,target,97,cura);
        if(target.getPdv()<=0)
        {
          fight.onFighterDie(target,caster);
          if(target.canPlay()&&target.getPersonnage()!=null)
            fight.endTurn(false);
          else if(target.canPlay())
            target.setCanPlay(false);
        }

      }
    }
    else
    {
      if(spell==470)
      {
        for(Fighter target : cibles)
        {
          if(target.getTeam()==caster.getTeam())
            continue;
          target.addBuff(effectID,0,turns,0,true,spell,args,caster,false);//on applique un buff
        }
      }
      for(Fighter target : cibles)
      {
        target.addBuff(effectID,value,turns,0,true,spell,args,caster,false);//on applique un buff
      }
    }
  }

  private void applyEffect_1028(ArrayList<Fighter> cibles, Fight fight) //AP bonus if has target
  {
    if(!cibles.isEmpty())
    {
      int val=Formulas.getRandomJet(jet);
      if(val==-1)
      {
        return;
      }
      caster.addBuff(120,val,turns,1,true,spell,args,caster,false);
      caster.setCurPa(fight,val);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,120,caster.getId()+"",caster.getId()+","+val+","+turns);
    }
  }

  private void applyEffect_1029(ArrayList<Fighter> cibles, Fight fight)
  {
    for(Fighter target : cibles)
      target.addBuff(effectID,value,turns,duration,false,spell,args,caster,true);
  }

  private void applyEffect_1030(ArrayList<Fighter> cibles, Fight fight)
  {
    if(turns>1)
      return;

    if(cell.isWalkable(false)&&!fight.isOccuped(cell.getId()))
    {
      caster.getCell().getFighters().clear();
      caster.setCell(cell);
      caster.getCell().addFighter(caster);

      /* ArrayList<Trap> P=(new ArrayList<Trap>());
      P.addAll(fight.getAllTraps());
      for(Trap p : P)
      {
        int dist=PathFinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),caster.getCell().getId());
        if(dist<=p.getSize())
          p.onTraped(caster);
      }*/
      verifyTraps(fight,caster);
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,4,caster.getId()+"",caster.getId()+","+cell.getId());

      ArrayList<Glyph> glyphs=new ArrayList<>(caster.getFight().getAllGlyphs());
      ArrayList<Glyph> targetGlyphs=new ArrayList<>();
      for(Glyph glyph : glyphs)
      {
        if(PathFinding.getDistanceBetween(caster.getFight().getMap(),caster.getCell().getId(),glyph.getCell().getId())<=glyph.getSize()&&glyph.getSpell()!=476)
          targetGlyphs.add(glyph);
      }
      for(Glyph glyph : targetGlyphs)
      {
        for(Fighter f : caster.getFight().getFighters(3))
        {
          if(f!=caster&&PathFinding.getDistanceBetween(f.getFight().getMap(),f.getCell().getId(),glyph.getCell().getId())<=glyph.getSize()&&Constant.isFecaGlyph(glyph.getSpell()))
          {
            glyph.onTrapped(f);
            if(f.getPdv()<=0)
            {
              if(f.canPlay()&&f.getPersonnage()!=null)
                fight.endTurn(false);
              else if(f.canPlay())
                f.setCanPlay(false);
            }
          }
        }
      }
    }
  }

  private ArrayList<Fighter> trierCibles(ArrayList<Fighter> cibles, Fight fight)
  {
    ArrayList<Fighter> array=new ArrayList<>();
    int max=-1;
    int distance;

    for(Fighter f : cibles)
    {
      distance=PathFinding.getDistanceBetween(fight.getMap(),this.cell.getId(),f.getCell().getId());
      if(distance>max)
        max=distance;
    }

    for(int i=max;i>=0;i--)
    {
      Iterator<Fighter> it=cibles.iterator();
      while(it.hasNext())
      {
        Fighter f=it.next();
        distance=PathFinding.getDistanceBetween(fight.getMap(),this.cell.getId(),f.getCell().getId());
        if(distance==i)
        {
          array.add(f);
          it.remove();
        }
      }
    }

    return array;
  }

  public void verifmobs(Fight fight, Fighter target, int effet, int cura)
  {
    switch(target.getMob().getTemplate().getId())
    {
      case 233:
        if(effet==168||effet==101)
        {
          target.addBuff(128,1,2,1,true,spell,args,target,false);
          //Gain de PM pendant le tour de jeu
          target.setCurPm(fight,1);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+1+","+2);
        }
        if(effet==169||effet==127)//rall pm don pa
        {
          target.addBuff(111,1,2,1,true,spell,args,target,false);
          //Gain de PA pendant le tour de jeu
          target.setCurPa(fight,1);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,effectID,caster.getId()+"",target.getId()+","+1+","+2);
        }
        if(effet==100||effet==97)
        {
          int healFinal=200;
          if((healFinal+caster.getPdv())>caster.getPdvMax())
            healFinal=caster.getPdvMax()-caster.getPdv();
          if(healFinal<1)
            healFinal=0;
          caster.removePdv(caster,healFinal);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,108,caster.getId()+"",target.getId()+","+healFinal);

        }
        break;
      case 2750:
        int healFinal=cura;
        if((healFinal+caster.getPdv())>caster.getPdvMax())
          healFinal=caster.getPdvMax()-caster.getPdv();
        if(healFinal<1)
          healFinal=0;
        caster.removePdv(caster,-healFinal);
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,108,caster.getId()+"",caster.getId()+","+healFinal);
        break;
      case 1045://kimbo
        if(effet==99||effet==98||effet==94||effet==93)
        {
          if(target.haveState(Constant.STATE_EVEN_GLYPH))
            target.setState(Constant.STATE_EVEN_GLYPH,0,caster.getId()); //Remove even
          target.setState(Constant.STATE_EVEN_GLYPH,1,caster.getId()); //Add even
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+Constant.STATE_EVEN_GLYPH+",1");
          if(target.haveState(Constant.STATE_ODD_GLYPH))
            target.setState(Constant.STATE_ODD_GLYPH,0,caster.getId()); //Remove odd
        }
        else if(effet==97||effet==96||effet==92||effet==91)
        {
          if(target.haveState(Constant.STATE_ODD_GLYPH))
            target.setState(Constant.STATE_ODD_GLYPH,0,caster.getId()); //Remove odd
          target.setState(Constant.STATE_ODD_GLYPH,1,caster.getId()); //Add odd
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+Constant.STATE_ODD_GLYPH+",1");
          if(target.haveState(Constant.STATE_EVEN_GLYPH))
            target.setState(Constant.STATE_EVEN_GLYPH,0,caster.getId()); //Remove even
        }
        break;
      case 423://kralamour
        if(effet==99||effet==94)
        {
          target.setState(37,1,caster.getId());//etat tersiaire feu
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+37+",1");
          if(target.haveState(38))//secondaire terre
          {
            target.setState(38,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+38+",0");
          }
          if(target.haveState(36))//quanternaire eau
          {
            target.setState(36,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+36+",0");
          }
          if(target.haveState(35))//primaire air
          {
            target.setState(35,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+35+",0");
          }
        }
        else if(effet==98||effet==93)
        {
          target.setState(35,1,caster.getId());//etat primaire air
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+35+",1");
          if(target.haveState(38))//secondaire terre
          {
            target.setState(38,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+38+",0");
          }
          if(target.haveState(36))//quanternaire eau
          {
            target.setState(36,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+36+",0");
          }
          if(target.haveState(37))//tersiaire feu
          {
            target.setState(37,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+37+",0");
          }
        }
        else if(effet==97||effet==92)
        {
          target.setState(38,1,caster.getId());//etat secondaire terre
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+38+",1");
          if(target.haveState(35))//primaire air
          {
            target.setState(35,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+35+",0");
          }
          if(target.haveState(36))//quanternaire eau
          {
            target.setState(36,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+36+",0");
          }
          if(target.haveState(37))//tersiaire feu
          {
            target.setState(37,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+37+",0");
          }
        }
        else if(effet==96||effet==91)
        {
          target.setState(36,1,caster.getId());//etat quanternaire eau
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+36+",1");
          if(target.haveState(35))//primaire air
          {
            target.setState(35,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+35+",0");
          }
          if(target.haveState(38))//secondaire terre
          {
            target.setState(38,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+38+",0");
          }
          if(target.haveState(37))//tersiaire feu
          {
            target.setState(37,0,caster.getId());
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,caster.getId()+"",target.getId()+","+37+",0");
          }
        }

        break;
      case 1071://Rasboul
        if(effet==101)
        {
          target.addBuff(111,value,1,1,true,spell,args,target,false);
          SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,111,target.getId()+"",target.getId()+",+"+value);
        }
        if(target.haveState(73)) {
        if(effet == 91) {
        	target.addBuff(216, 50, 2, 1, true, this.spell, this.args, this.caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 216,new StringBuilder(String.valueOf(this.caster.getId())).toString(),
					String.valueOf(target.getId()) + "," + 50 + "," + 2);	
        }
        if(effet == 92) {
        	target.addBuff(215, 50, 2, 1, true, this.spell, this.args, this.caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 215,new StringBuilder(String.valueOf(this.caster.getId())).toString(),
					String.valueOf(target.getId()) + "," + 50 + "," + 2);	
        }
        if(effet == 93) {
        	target.addBuff(217, 50, 2, 1, true, this.spell, this.args, this.caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 217,new StringBuilder(String.valueOf(this.caster.getId())).toString(),
					String.valueOf(target.getId()) + "," + 50 + "," + 2);	
        }
        if(effet == 94) {
        	target.addBuff(218, 50, 2, 1, true, this.spell, this.args, this.caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 218,new StringBuilder(String.valueOf(this.caster.getId())).toString(),
					String.valueOf(target.getId()) + "," + 50 + "," + 2);	
        }
        if(effet == 95) {
        	target.addBuff(218, 50, 2, 1, true, this.spell, this.args, this.caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 219,new StringBuilder(String.valueOf(this.caster.getId())).toString(),
					String.valueOf(target.getId()) + "," + 50 + "," + 2);	
        }
        if(effet == 96) {
        	target.addBuff(216, 50, 2, 1, true, this.spell, this.args, this.caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 216,new StringBuilder(String.valueOf(this.caster.getId())).toString(),
					String.valueOf(target.getId()) + "," + 50 + "," + 2);	
        }
        if(effet == 97) {
        	target.addBuff(215, 50, 2, 1, true, this.spell, this.args, this.caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 215,new StringBuilder(String.valueOf(this.caster.getId())).toString(),
					String.valueOf(target.getId()) + "," + 50 + "," + 2);	
        }
        if(effet == 98) {
        	target.addBuff(217, 50, 2, 1, true, this.spell, this.args, this.caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 217,new StringBuilder(String.valueOf(this.caster.getId())).toString(),
					String.valueOf(target.getId()) + "," + 50 + "," + 2);	
        }
        if(effet == 99) {
        	target.addBuff(218, 50, 2, 1, true, this.spell, this.args, this.caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 218,new StringBuilder(String.valueOf(this.caster.getId())).toString(),
					String.valueOf(target.getId()) + "," + 50 + "," + 2);	
        }
        if(effet == 100) {
        	target.addBuff(219, 50, 2, 1, true, this.spell, this.args, this.caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 219,new StringBuilder(String.valueOf(this.caster.getId())).toString(),
					String.valueOf(target.getId()) + "," + 50 + "," + 2);	
        }
        	
        	
        }
        break;
    }
  }

  /*public void checkTraps(Fight fight, Fighter fighter)
  {
    final short[] nbr= { 0 };
    new ArrayList<>(fight.getAllTraps()).stream().filter(trap -> PathFinding.getDistanceBetween(fight.getMap(),trap.getCell().getId(),fighter.getCell().getId())<=trap.getSize()).forEach(trap -> {
      trap.onTraped(fighter);
      try
      {
        Thread.sleep(750+nbr[0]*300);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      nbr[0]++;
    });
  }*/

  public static void verifyTraps(Fight fight, Fighter fighter)
  {
	  ArrayList<Trap> objet2 = new ArrayList<>();
	    objet2.addAll(fight.getAllTraps());
	    Collections.sort(objet2, new order_trap());  
    new ArrayList<>(objet2).stream().filter(trap -> PathFinding.getDistanceBetween(fight.getMap(),trap.getCell().getId(),fighter.getCell().getId())<=trap.getSize()).forEach(trap -> {
    	trap.onTraped(fighter);
    });
  }
  private static class order_trap implements Comparator<Trap> {
      public int compare(Trap p1, Trap p2) {
    	  try {
          return Long.valueOf(p1.getCell().getId()).compareTo((long) (p2.getCell().getId()));
    	    } catch (final Exception e) {
    			e.printStackTrace();
    		}
		return 0;
      }

  }
public static void cartaDeInvocación(Fight fight,Fighter lanzador, int celda, int idMob, int gradoMob)// invocation
	{



		MobGrade MG;
		try {
			MG = Main.world.getMonstre(idMob).getGradeByLevel(gradoMob).getCopy();
		} catch (Exception e1) {
			try {
				MG = Main.world.getMonstre(idMob).getRandomGrade().getCopy();
			} catch (Exception e2) {
				return;
			}
		}
		
		if(lanzador.invocado < 1) {
		
		if (idMob == -1 || gradoMob == -1 || MG == null)
			return;
		if (fight.getTeam(lanzador.getTeam()).size() > 30)
			return;
		MG.setInFightID(fight.getNextLowerFighterGuid());
		if (lanzador.getPersonnage() != null)
			MG.modifStatByInvocator(lanzador, idMob);
		Fighter F = new Fighter(fight, MG);
		F.setTeam(lanzador.getTeam());
		F.setInvocator(lanzador);
		fight.getMap().getCase(celda).addFighter(F);
		F.setCell(fight.getMap().getCase(celda));
		fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(lanzador) + 1), F);
		fight.addFighterInTeam(F, lanzador.getTeam());
		String gm = F.getGmPacket('+', true).substring(3);
		String gtl = fight.getGTL();
		try {
			if (lanzador.getMob() != null)
				Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 181, lanzador.getId() + "", gm);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, lanzador.getId() + "", gtl);
		SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(fight, 7, "01000;<b>[Carta de Invocación]</b> lanza <b>"+ MG.getTemplate().getNombre()+"</b>");
		lanzador.invocado++;
		verifyTraps(fight, F);
		}else {
			SocketManager.send(lanzador.getPersonnage(), "BN");
		}
	}
}