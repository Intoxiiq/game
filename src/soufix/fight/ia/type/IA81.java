package soufix.fight.ia.type;

import java.util.Iterator;

import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.ia.AbstractNeedSpell;
import soufix.fight.ia.util.Function;
import soufix.fight.spells.Spell.SortStats;
import soufix.main.Config;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA81 extends AbstractNeedSpell
{
  private boolean hasSummons=false;
  private Fighter summon=null;
  private int buff2=0;

  public IA81(Fight fight, Fighter fighter, byte count)
  {
    super(fight,fighter,count);
  }

  @Override
  public void apply()
  {
    if(!this.stop&&this.fighter.canPlay()&&this.count>0)
    {
      int time=100,maxPo=1;
      boolean action=false;
      Fighter E=Function.getInstance().getNearestEnnemy(this.fight,this.fighter);

      for(SortStats spellStats : this.highests)
        if(spellStats!=null&&spellStats.getMaxPO()>maxPo)
          maxPo=spellStats.getMaxPO();

      if(!this.hasSummons)
      {
        Iterator<Fighter> fightItt=this.fight.getFighters(this.fighter.getOtherTeam()).iterator();
        {
          while(fightItt.hasNext())
          {
            Fighter nextFighter=fightItt.next();
            if(nextFighter.isInvocation())
            {
              this.hasSummons=true;
              this.summon=nextFighter;
            }
          }
        }
      }

      Fighter firstEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,1,maxPo+1);
      Fighter secondEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2);

      if(maxPo==1)
        firstEnnemy=null;
      if(secondEnnemy!=null)
        if(secondEnnemy.isHide())
          secondEnnemy=null;
      if(firstEnnemy!=null)
        if(firstEnnemy.isHide())
          firstEnnemy=null;

      if(this.fighter.getCurPm(this.fight)>0&&this.summon!=null)
      {
        int num=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,this.summon);
        if(num!=0)
        {
          time=num;
          action=true;
          firstEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,1,maxPo+1);
          secondEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2);
          if(maxPo==1)
            firstEnnemy=null;
        }
      }
      else if(this.fighter.getCurPm(this.fight)>0&&firstEnnemy==null&&secondEnnemy==null)
      {
        int num=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,E);
        if(num!=0)
        {
          time=num;
          action=true;
          firstEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,1,maxPo+1);
          secondEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2);
          if(maxPo==1)
            firstEnnemy=null;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&!action&&this.buff2==0)
      {
        if(Function.getInstance().buffIfPossible(this.fight,this.fighter,this.fighter,this.buffs))
        {
          time=400;
          action=true;
        }
        this.buff2++;
      }

      if(this.fighter.getCurPa(this.fight)>0&&secondEnnemy!=null&&!action&&this.hasSummons&&this.summon!=null)
      {
        int value=Function.getInstance().attackIfPossibleAll(this.fight,this.fighter,summon);
        if(value!=0)
        {
          time=value;
          action=true;
          this.hasSummons=false;
          this.summon=null;
        }
      }

      if(this.fighter.getCurPm(this.fight)>0&&!action)
      {
        int num=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,summon);
        if(num!=0)
          time=num;
      }

      if(this.fighter.getCurPa(this.fight)==0&&this.fighter.getCurPm(this.fight)==0)
        this.stop=true;
      addNext(this::decrementCount,time+Config.getInstance().AIDelay);
    }
    else
    {
      this.stop=true;
    }
  }
}