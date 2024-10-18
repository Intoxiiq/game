package soufix.fight.ia.type;


import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.ia.AbstractNeedSpell;
import soufix.fight.ia.util.Function;

public class IA21 extends AbstractNeedSpell
{

  public IA21(Fight fight, Fighter fighter, byte count)
  {
    super(fight,fighter,count);
  }

  @Override
  public void apply()
  {
    if(!this.stop&&this.fighter.canPlay()&&this.count>0)
    {
    	//if((this.fight.getStartTime()+60000) > System.currentTimeMillis())
    	if(fighter.getBuff(216) == null)
    	Function.getInstance().attackIfPossible(this.fight,this.fighter,this.highests);
    	Function.getInstance().invoctantaIfPossible(this.fight,this.fighter);
      Function.getInstance().buffIfPossibleKrala(this.fight,this.fighter,this.fighter);
      addNext(this::decrementCount,200);
    } else
    {
      this.stop=true;
    }
  }
}