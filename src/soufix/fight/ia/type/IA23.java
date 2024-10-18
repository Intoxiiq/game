package soufix.fight.ia.type;

import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.ia.AbstractIA;
import soufix.fight.ia.util.Function;

public class IA23 extends AbstractIA
{

  public IA23(Fight fight, Fighter fighter, byte count)
  {
    super(fight,fighter,count);
  }

  @Override
  public void apply()
  {
    if(!this.stop&&this.fighter.canPlay()&&this.count>0)
    {
      Fighter ennemy=Function.getInstance().getNearestFriendNoInvok(this.fight,this.fighter);

      if(!Function.getInstance().moveNearIfPossible(this.fight,this.fighter,ennemy))
        Function.getInstance().HealIfPossible(this.fight,this.fighter,false,90);

      addNext(this::decrementCount,500);
    } else
    {
      this.stop=true;
    }
  }
}