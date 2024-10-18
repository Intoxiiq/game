package soufix.fight.ia.type;

import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.ia.AbstractNeedSpell;
import soufix.fight.ia.util.Function;
import soufix.fight.spells.Spell;
import soufix.fight.spells.Spell.SortStats;

public class IA200 extends AbstractNeedSpell {

    Spell.SortStats soin = this.fighter.getMob().getSpells().get(140);
    Spell.SortStats sacrifice = this.fighter.getMob().getSpells().get(440);
    Spell.SortStats transpo = this.fighter.getMob().getSpells().get(438);
    Spell.SortStats attaque = this.fighter.getMob().getSpells().get(527);
    public IA200(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
	  public void apply()
	  {
	    if(!this.stop&&this.fighter.canPlay()&&this.count>0)
	    {
	      int time=100,maxPo=1,maxPoBuff=1;
	      boolean action=false;
	      Fighter ennemy=Function.getInstance().getNearestEnnemy(this.fight,this.fighter);
	      //Fighter A=Function.getInstance().getNearestAminbrcasemax(this.fight,this.fighter,1,63);// pomax +1;

	      for(SortStats S : this.highests)
	        if(S!=null&&S.getMaxPO()>maxPo)
	          maxPo=S.getMaxPO();

	      for(SortStats S : this.buffs)
		        if(S!=null&&S.getMaxPO()>maxPoBuff)
		        	maxPoBuff=S.getMaxPO();
	      
	      Fighter longestEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,1,maxPo+1);// pomax +1;
	      Fighter nearestEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2);//2 = po min 1 + 1;
	      Fighter ami=Function.getInstance().getNearestAminbrcasemax(this.fight,this.fighter,0,maxPoBuff+1);// condition ami pour buff	      
	    /*  if(maxPo==1)
	        longestEnnemy=null;
	      if(nearestEnnemy!=null)
	        if(nearestEnnemy.isHide())
	          nearestEnnemy=null;
	      if(longestEnnemy!=null)
	        if(longestEnnemy.isHide())
	          longestEnnemy=null;*/
//buff ami
	      if(this.fighter.getCurPa(this.fight)>0&&!action&&ami!=null)
	      {
	    	  if(this.fight.tryCastSpell(this.fighter,sacrifice,ami.getCell().getId())==0)
	        {
	        
	          time=2000;
	          action=true;
	        }
	      }
	      // pm vers enemie le plus proche
	      if(this.fighter.getCurPm(this.fight)>0/*&&longestEnnemy==null&&nearestEnnemy==null*/)
	      {
	        int value=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,ennemy);
	        if(value!=0)
	        {
	          time=value;
	          action=true;
	          longestEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,1,maxPo+1);
	          nearestEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2);
	          ami=Function.getInstance().getNearestAminbrcasemax(this.fight,this.fighter,0,64);
	        }
	      }
        // on peut laisser meme si le monstre a pas d invocation
	      if(this.fighter.getCurPa(this.fight)>0&&this.fighter.nbInvocation()<2)
	      {
	        if(Function.getInstance().invocIfPossible(this.fight,this.fighter,this.invocations))
	        {
	          time=600;
	          action=true;
	        }
	      }
	      
	      //soin prioritaire sur lui meme 
	      if(this.fighter.getCurPa(this.fight)>0 && fighter.getPdv()< fighter.getPdvMax()/2)
	      {
	        //if(Function.getInstance().HealIfPossible(this.fight,this.fighter,false,50)!=0)
	        if(this.fight.tryCastSpell(this.fighter,soin,fighter.getCell().getId())==0)
	        {
	          time=1000;
	          action=true;
	        }
	      }
	      
	      // si non soigne ami le plus proche
	      if(this.fighter.getCurPa(this.fight)>0 && ami!=null && ami.getPdv()< ami.getPdvMax()/2)
	      {
	        //if(Function.getInstance().HealIfPossible(this.fight,this.fighter,false,50)!=0)
	        if(this.fight.tryCastSpell(this.fighter,soin,ami.getCell().getId())==0)
	        {
	          time=1000;
	          action=true;
	        }
	      }
	      


	      if(this.fighter.getCurPa(this.fight)>0&&longestEnnemy!=null&&nearestEnnemy==null&&!action)
	      {
	        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.highests);
	        if(value!=-1)
	        {
	          time=value;
	          action=true;
	        }
	      } else if(this.fighter.getCurPa(this.fight)>0&&nearestEnnemy!=null&&!action)
	      {
	        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.cacs);
	        if(value!=-1)
	        {
	          time=value;
	          action=true;
	        }
	      }

	      if(this.fighter.getCurPa(this.fight)>0&&nearestEnnemy!=null&&!action)
	      {
	        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.highests);
	        if(value!=-1)
	        {
	          time=value;
	          action=true;
	        }
	      }
	      if(this.fighter.getCurPa(this.fight)>0&&nearestEnnemy!=null&&!action)
	      {
	        String value = Function.getInstance().moveToAttackIfPossible2(this.fight,this.fighter);
	        if(!value.isEmpty())
	        {
	          int cellId = Integer.parseInt(value.split(";")[0]);
	          SortStats spellStats = fighter.getMob().getSpells().get(Integer.parseInt(value.split(";")[1]));

	          if(fight.canCastSpell1(fighter, spellStats, fight.getMap().getCase(cellId), cellId)){
	            int val = fight.tryCastSpell(fighter, spellStats, cellId);
	            if(val == 0) {
	              time = spellStats.getSpell().getDuration();
	              action = true;
	            }
	          }
	        }
	      }
	      
	   // transpo si possible
	      if (this.fighter.getCurPa(this.fight) > 0 && !action && ami != null) {
	          // Vérifier si aucun joueur n'est à portée
	          boolean noPlayerInRange = Function.getInstance().getNearestPlayer(this.fight, this.fighter, 0, maxPoBuff + 1) == null;

	          // Si aucun joueur n'est à portée, lancez le sort de téléportation sur l'ami le plus proche d'un autre ennemi
	          if (noPlayerInRange) {
	              Fighter otherEnemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
	              if (otherEnemy != null) {
	                  Fighter amiProche = Function.getInstance().getNearestAminbrcasemax(this.fight, this.fighter, 0, maxPoBuff + 1);
	                  if (amiProche != null) {
	                      if (this.fight.tryCastSpell(this.fighter, transpo, amiProche.getCell().getId()) == 0) {
	                          time = 2000;
	                          action = true;
	                      }
	                  }
	              }
	          } else {
	              // Sinon, si des joueurs sont à portée, lancez le sort de téléportation sur l'ami standard
	              if (this.fight.tryCastSpell(this.fighter, transpo, ami.getCell().getId()) == 0) {
	                  time = 2000;
	                  action = true;
	              }
	          }
	      }

	      if(this.fighter.getCurPm(this.fight)>0&&!action)
	      {
	        int value=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,ennemy);
	        if(value!=0)
	          time=value;
	      }

	      if(this.fighter.getCurPa(this.fight)==0&&this.fighter.getCurPm(this.fight)==0)
	        this.stop=true;
	      addNext(this::decrementCount,time);
	    } else
	    {
	      this.stop=true;
	    }
	  }
	}