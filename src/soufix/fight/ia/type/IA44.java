package soufix.fight.ia.type;

import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.ia.AbstractNeedSpell;
import soufix.fight.ia.util.Function;

public class IA44 extends AbstractNeedSpell
{

  public IA44(Fight fight, Fighter fighter, byte count)
  {
    super(fight,fighter,count);
  }

  @Override
  public void apply()
  {
    if(!this.stop&&this.fighter.canPlay()&&this.count>0)
    {
      int time=100;
      boolean action=false;
      Fighter E=Function.getInstance().getNearestEnnemy(this.fight,this.fighter);
      Fighter C=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2);//2 = po min 1 + 1;
      Fighter A=Function.getInstance().getNearestAminoinvocnbrcasemax(this.fight,this.fighter,0,5);//2 = po min 1 + 1;
      Fighter A1=Function.getInstance().getNearestAminoinvocnbrcasemax(this.fight,this.fighter,0,2);//2 = po min 1 + 1;

      if(A!=null&&(A.getPdv()*100)/A.getPdvMax()>90)
        A=null;
      if(A1!=null&&(A1.getPdv()*100)/A1.getPdvMax()>90)
        A1=null;
      if(C!=null&&C.isHide())
        C=null;

      if(this.fighter.getCurPm(this.fight)>0&&A!=null&&A1==null)
      {
        int value=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,A);
        if(value!=0)
        {
          time=value;
          action=true;
          C=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2);//2 = po min 1 + 1;
          A1=Function.getInstance().getNearestAminoinvocnbrcasemax(this.fight,this.fighter,0,2);//2 = po min 1 + 1;
        }
      } else if(this.fighter.getCurPm(this.fight)>0&&C==null)
      {
        int value=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,E);
        if(value!=0)
        {
          time=value;
          action=true;
          C=Function.getInstance().getNearestEnnemynbrcasemax(this.fight,this.fighter,0,2);//2 = po min 1 + 1;
        }
      }
      if(this.fighter.getCurPa(this.fight)>0&&C!=null&&!action)
      {
        int value=Function.getInstance().attackIfPossible(this.fight,this.fighter,this.cacs);
        if(value!=-1)
        {
          time=value;
          action=true;
        }
      } else if(this.fighter.getCurPa(this.fight)>0&&A1!=null&&!action)
      {
        int value=Function.getInstance().attackIfPossiblevisee(this.fight,this.fighter,A1,this.cacs);
        if(value!=-1)
        {
          time=value;
          action=true;
        }
      }
      if(this.fighter.getCurPm(this.fight)>0&&!action)
      {
        int value=Function.getInstance().moveautourIfPossible(this.fight,this.fighter,E);
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