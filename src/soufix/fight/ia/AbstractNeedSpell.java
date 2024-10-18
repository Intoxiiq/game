package soufix.fight.ia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.spells.SpellEffect;
import soufix.fight.spells.Spell.SortStats;
import soufix.main.Main;

public abstract class AbstractNeedSpell extends AbstractIA
{

  protected List<SortStats> buffs, glyphs, invocations, cacs, highests, heal, linear, antisummon;

  public AbstractNeedSpell(Fight fight, Fighter fighter, byte count)
  {
    super(fight,fighter,count);

    try
    {
      this.buffs=AbstractNeedSpell.getListSpellOf(fighter,"BUFF");
      this.glyphs=AbstractNeedSpell.getListSpellOf(fighter,"GLYPH");
      this.invocations=AbstractNeedSpell.getListSpellOf(fighter,"INVOCATION");
      this.cacs=AbstractNeedSpell.getListSpellOf(fighter,"CAC");
      this.highests=AbstractNeedSpell.getListSpellOf(fighter,"HIGHEST");
      this.heal=AbstractNeedSpell.getListSpellOf(fighter,"HEAL");
      this.linear=AbstractNeedSpell.getListSpellOf(fighter,"LINEAR");
      this.antisummon=AbstractNeedSpell.getListSpellOf(fighter,"ANTISUMMON");
    }
    catch(Exception e)
    {
     e.printStackTrace();
    }
  }

  private static List<SortStats> getListSpellOf(Fighter fighter, String type)
  {
    final List<SortStats> spells=new ArrayList<>();
    Map<Integer, SortStats> spells2=new HashMap<>();
    if(fighter.getMob() != null)
    {
    spells2 = fighter.getMob().getSpells();
    }
    if(fighter.isCollector())
    {
    spells2 = Main.world.getGuild(fighter.getCollector().getGuildId()).getSpells();
    }	
    for(SortStats spell : spells2.values())
    {
    	if(spell == null)
    		continue;
      switch(type)
      {
        case "CAC":
          if(spell.getSpell().getType()==0)
          {
            boolean effect=false;
            for(SpellEffect spellEffect : spell.getEffects())
              if(spellEffect.getEffectID()==4||spellEffect.getEffectID()==6)
                effect=true;
            if(!effect&&spell.getMaxPO()<3)
              spells.add(spell);
          }
          break;
        case "HIGHEST":
          if(spell.getSpell().getType()==0)
          {
            boolean effect=false;
            for(SpellEffect spellEffect : spell.getEffects())
              if(spellEffect.getEffectID()==4||spellEffect.getEffectID()==6)
                effect=true;
            if(effect&&spell.getSpellID()!=805)
              continue;
            if(spell.getMaxPO()>1)
              spells.add(spell);
          }
          break;
        case "BUFF":
          if(spell.getSpell().getType()==1)
            spells.add(spell);
          break;
        case "INVOCATION":
          if(spell.getSpell().getType()==2)
            spells.add(spell);
          break;
        case "GLYPH":
          if(spell.getSpell().getType()==4)
            spells.add(spell);
          break;
        case "HEAL":
          if(spell.getSpell().getType()==5)
          {
            spells.add(spell);
          }
          break;
        case "LINEAR":
          if(spell.getSpell().getType()==6)
          {
            spells.add(spell);
          }
          break;
        case "ANTISUMMON":
          if(spell.getSpell().getType()==7)
          {
            spells.add(spell);
          }
          break;
      }
    }
    return spells;
  }
}
