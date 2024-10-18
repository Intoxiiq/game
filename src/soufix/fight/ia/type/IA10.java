package soufix.fight.ia.type;

import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.ia.AbstractIA;
import soufix.fight.ia.util.Function;
import soufix.fight.spells.Spell.SortStats;
import soufix.main.Constant;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA10 extends AbstractIA
{

  public IA10(Fight fight, Fighter fighter, byte count)
  {
    super(fight,fighter,count);
  }

  @Override
  public void apply()
  {
    if(count>0&&this.fighter.canPlay()&&!this.stop)
    {
      Fighter target=Function.getInstance().getNearestEnnemy(fight,this.fighter);
      if(target==null)
        return;

      int value=Function.getInstance().moveToAttackIfPossible(fight,this.fighter),cellId=value-(value/1000)*1000;
      SortStats spellStats=this.fighter.getMob().getSpells().get(value/1000);

      if(cellId!=-1)
      {
        if(fight.canCastSpell1(this.fighter,spellStats,this.fighter.getCell(),cellId))
          fight.tryCastSpell(this.fighter,spellStats,cellId);
      } else if(this.fighter.haveState(Constant.ETAT_PORTE))
      {
        if(!Function.getInstance().HealIfPossible(fight,this.fighter,true))
          if(!Function.getInstance().HealIfPossible(fight,this.fighter,false))
            this.stop=true;
      } else
      {
        this.stop=true;
      }

      addNext(this::decrementCount,800);
    } else
    {
      this.stop=true;
    }
  }
}