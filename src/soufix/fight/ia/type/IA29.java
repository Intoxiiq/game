package soufix.fight.ia.type;

import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.ia.AbstractIA;
import soufix.fight.ia.util.Function;

public class IA29 extends AbstractIA
{

  public IA29(Fight fight, Fighter fighter, byte count)
  {
    super(fight,fighter,count);
  }

  @Override
  public void apply()
  {
    if(!this.stop&&this.fighter.canPlay()&&this.count>0)
    {
      Fighter ennemy=Function.getInstance().getNearestEnnemy(this.fight,this.fighter);

      if(!Function.getInstance().buffIfPossibleTortu(this.fight,this.fighter,this.fighter))
        Function.getInstance().moveNearIfPossible(this.fight,this.fighter,ennemy);
      Function.getInstance().moveNearIfPossible(this.fight,this.fighter,ennemy);

      addNext(this::decrementCount,1000);
    } else
    {
      this.stop=true;
    }
  }
}