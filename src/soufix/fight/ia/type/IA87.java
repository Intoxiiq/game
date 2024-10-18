package soufix.fight.ia.type;

import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.ia.AbstractNeedSpell;
import soufix.fight.ia.util.Function;
import soufix.fight.spells.Spell;

public class IA87 extends AbstractNeedSpell
{
  boolean hasMoved=false;

  public IA87(Fight fight, Fighter fighter, byte count)
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

      Fighter ennemy1=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,1,maxPo+1); // pomax +1;
      Fighter ennemy2=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2); //2 = po min 1 + 1;

      if(maxPo==1)
        ennemy1=null;
      if(ennemy2!=null)
        if(ennemy2.isHide())
          ennemy2=null;
      if(ennemy1!=null)
        if(ennemy1.isHide())
          ennemy1=null;

      if(this.fighter.getCurPa(this.fight)>0&&!action) //heal self
      {
        if(Function.getInstance().HealIfPossible(this.fight,this.fighter,true,50)!=0)
        {
          time=1000;
          action=true;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&hasMoved&&!action) //summon if moved before high priority
      {
        if(Function.getInstance().invocIfPossible(this.fight,this.fighter,this.invocations))
        {
          time=600;
          action=true;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&ennemy2!=null&&!action) //sylvan bite
      {
        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.cacs);
        if(value!=-1)
        {
          time=value;
          action=true;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&ennemy1!=null&&!action) //paralyzing branch longrange
      {
        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.highests);
        if(value!=-1)
        {
          time=value;
          action=true;
        }
      }
      
      if(this.fighter.getCurPa(this.fight)>0&&ennemy2!=null&&!action) //paralyzing branch shortrange
      {
        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.highests);
        if(value!=-1)
        {
          time=value;
          action=true;
        }
      }

      if(this.fighter.getCurPm(this.fight)>0&&!action)
      {
        if(Function.getInstance().moveNearIfPossible(this.fight,this.fighter,nearestEnnemy))
        {
          hasMoved=true;
          time=1000;
          action=true;
          ennemy1=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,1,maxPo+1);// pomax +1;
          ennemy2=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2);//2 = po min 1 + 1;

          if(maxPo==1)
            ennemy1=null;
        }
      }
      if(this.fighter.getCurPm(this.fight)>0&&!action)
      {
        int value=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,nearestEnnemy);
        if(value!=0)
        {
          time=value;
          hasMoved=true;
          action=true;
          ennemy1=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,1,maxPo+1);// pomax +1;
          ennemy2=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2);//2 = po min 1 + 1;

          if(maxPo==1)
            ennemy1=null;
        }
      }

      if(this.fighter.getCurPa(this.fight)>0&&!action) //summon if cant move low priority
      {
        if(Function.getInstance().invocIfPossible(this.fight,this.fighter,this.invocations))
        {
          time=600;
          action=true;
        }
      }

      if(this.fighter.getCurPa(this.fight)==0&&this.fighter.getCurPm(this.fight)==0)
        this.stop=true;
      addNext(this::decrementCount,time);
    }
    else
    {
      this.stop=true;
    }
  }
}