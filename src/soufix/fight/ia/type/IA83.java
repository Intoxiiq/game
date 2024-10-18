package soufix.fight.ia.type;

import soufix.common.PathFinding;
import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.ia.AbstractNeedSpell;
import soufix.fight.ia.util.Function;
import soufix.fight.spells.Spell;
import soufix.main.Config;

public class IA83 extends AbstractNeedSpell
{

  private int attack=0;
  private boolean hasMovedClose=false;

  public IA83(Fight fight, Fighter fighter, byte count)
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
      Fighter nearestEnnemy=Function.getInstance().getNearestEnnemy(this.fight,this.fighter);

      for(Spell.SortStats S : this.highests)
        if(S!=null&&S.getMaxPO()>maxPo)
          maxPo=S.getMaxPO();

      Fighter longestEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,maxPo+1); //po max+ 1;
      if(longestEnnemy!=null)
        if(longestEnnemy.isHide())
          longestEnnemy=null;

      if(this.fighter.getCurPa(this.fight)>0&&!action)
      {
        int beforeAP=this.fighter.getCurPa(this.fight);
        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.linear);
        int afterAP=this.fighter.getCurPa(this.fight);
        if(beforeAP>afterAP)
        {
          time=value+300;
          action=true;
          this.attack++;
        }
      }

      if(this.fighter.getCurPm(this.fight)>0&&this.attack==0&&!hasMovedClose)
      {
        final int val=Function.getInstance().moveToAttackIfPossible(this.fight,this.fighter);
        final Spell.SortStats SS=fighter.getMob().getSpells().get(val/1000);
        final int cellID=val-val/1000*1000;
        if(fight.canCastSpell1(fighter,SS,fighter.getCell(),cellID))
        {
          int path=PathFinding.getShortestPathBetween(fight.getMap(),fighter.getCell().getId(),cellID,fighter.getCurPm(fight)).size();
          if(path>0)
          {
            time=1000+path*100;
            action=true;
            hasMovedClose=true;
          }
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&!action&&hasMovedClose)
      {
        int beforeAP=this.fighter.getCurPa(this.fight);
        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.linear);
        int afterAP=this.fighter.getCurPa(this.fight);
        if(beforeAP>afterAP)
        {
          time=value+300;
          action=true;
          this.attack++;
        }
      }

      if(this.fighter.getCurPm(this.fight)>0&&longestEnnemy==null&&this.attack==0&&!hasMovedClose)
      {
        int value=Function.getInstance().movediagIfPossible(this.fight,this.fighter,nearestEnnemy);
        if(value!=0)
        {
          time=value+1000;
          action=true;
          longestEnnemy=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,maxPo+1);
          hasMovedClose=true;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&!action)
      {
        if(Function.getInstance().invocIfPossible(this.fight,this.fighter,this.invocations))
        {
          time=600;
          action=true;
        }
      }

      int percentPdv=(this.fighter.getPdv()*100)/this.fighter.getPdvMax();

      if(this.fighter.getCurPa(this.fight)>0&&!action&&percentPdv<50&&percentPdv<95)
      {
        if(Function.getInstance().HealIfPossible(this.fight,this.fighter,true,50)!=0)
        {
          time=400;
          action=true;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&!action)
      {
        if(Function.getInstance().HealIfPossible(this.fight,this.fighter,false,80)!=0)
        {
          time=400;
          action=true;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&!action)
      {
        if(Function.getInstance().buffIfPossible(this.fight,this.fighter,this.fighter,this.buffs))
        {
          time=400;
          action=true;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&!action)
      {
        int beforeAP=this.fighter.getCurPa(this.fight);
        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.linear);
        int afterAP=this.fighter.getCurPa(this.fight);
        if(beforeAP>afterAP)
        {
          time=value+300;
          action=true;
          this.attack++;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&!action)
      {
        int beforeAP=this.fighter.getCurPa(this.fight);
        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.highests);
        int afterAP=this.fighter.getCurPa(this.fight);
        if(beforeAP>afterAP)
        {
          time=value+300;
          action=true;
          this.attack++;
        }
        else if(this.fighter.getCurPm(this.fight)>0&&this.attack==0)
        {
          value=Function.getInstance().movediagIfPossible(this.fight,this.fighter,nearestEnnemy);
          if(value!=0)
          {
            time=value;
            action=true;
            Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,maxPo+1);
          }
        }
      }

      if(this.fighter.getCurPm(this.fight)>0&&!action&&this.attack>0)
      {
        int value=Function.getInstance().moveFarIfPossible(this.fight,this.fighter);
        if(value!=0)
          time=value;
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