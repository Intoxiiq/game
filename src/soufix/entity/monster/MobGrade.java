package soufix.entity.monster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import soufix.area.map.GameCase;
import soufix.client.other.Stats;
import soufix.common.Formulas;
import soufix.fight.Fighter;
import soufix.fight.spells.Spell;
import soufix.fight.spells.SpellEffect;
import soufix.fight.spells.Spell.SortStats;
import soufix.main.Constant;
import soufix.main.Main;

public class MobGrade
{
  private static int pSize=2;
  private Monster template;
  private int grade;
  private int level;
  private int pdv;
  private int pdvMax;
  private int inFightId;
  private int init;
  private int pa;
  private int pm;
  private int size;
  private int baseXp=10;
  private GameCase fightCell;
  private String _resistencias, _spells;
  private ArrayList<SpellEffect> fightBuffs=new ArrayList<SpellEffect>();
  private Map<Integer, Integer> stats=new HashMap<Integer, Integer>();
  private Map<Integer, SortStats> spells=new HashMap<Integer, SortStats>();
  private ArrayList<Integer> statsInfos=new ArrayList<Integer>();

  public MobGrade(Monster template, int grade, int level, int pa, int pm, String resists, String stats, String statsInfos, String allSpells, int pdvMax, int aInit, int xp, int n)
  {
    this.size=100+n*pSize;
    this.template=template;
    this.grade=grade;
    this.level=level;
    this.pdvMax=pdvMax;
    this.pdv=pdvMax;
    this.pa=pa;
    this.pm=pm;
    this.baseXp=xp;
    this.init=aInit;
    this.stats.clear();
    this.spells.clear();
    if (!allSpells.equals("-1")) {
        _spells = allSpells;
    }
    String[] resist=resists.split(";"),stat=stats.split(","),statInfos=statsInfos.split(";");

    for(String str : statInfos)
      this.statsInfos.add(Integer.parseInt(str));

    try
    {
      if(resist.length>3)
      {
        this.stats.put(Constant.STATS_ADD_RP_NEU,Integer.parseInt(resist[0]));
        this.stats.put(Constant.STATS_ADD_RP_TER,Integer.parseInt(resist[1]));
        this.stats.put(Constant.STATS_ADD_RP_FEU,Integer.parseInt(resist[2]));
        this.stats.put(Constant.STATS_ADD_RP_EAU,Integer.parseInt(resist[3]));
        this.stats.put(Constant.STATS_ADD_RP_AIR,Integer.parseInt(resist[4]));
        this.stats.put(Constant.STATS_ADD_AFLEE,Integer.parseInt(resist[5]));
        this.stats.put(Constant.STATS_ADD_MFLEE,Integer.parseInt(resist[6]));
      }
      else
      {
        String[] split=resist[0].split(",");
        this.stats.put(-1,Integer.parseInt(split[0]));
        this.stats.put(-100,Integer.parseInt(split[1]));
        this.stats.put(Constant.STATS_ADD_AFLEE,Integer.parseInt(resist[1]));
        this.stats.put(Constant.STATS_ADD_MFLEE,Integer.parseInt(resist[2]));
      }

      this.stats.put(Constant.STATS_ADD_FORC,Integer.parseInt(stat[0]));
      this.stats.put(Constant.STATS_ADD_SAGE,Integer.parseInt(stat[1]));
      this.stats.put(Constant.STATS_ADD_INTE,Integer.parseInt(stat[2]));
      this.stats.put(Constant.STATS_ADD_CHAN,Integer.parseInt(stat[3]));
      this.stats.put(Constant.STATS_ADD_AGIL,Integer.parseInt(stat[4]));
      this.stats.put(Constant.STATS_ADD_DOMA,Integer.parseInt(statInfos[0]));
      this.stats.put(Constant.STATS_ADD_PERDOM,Integer.parseInt(statInfos[1]));
      this.stats.put(Constant.STATS_ADD_SOIN,Integer.parseInt(statInfos[2]));
      this.stats.put(Constant.STATS_ADD_SUM,Integer.parseInt(statInfos[3]));
    }
    catch(Exception e)
    {
      Main.world.logger.error("#1# Erreur lors du chargement du grade du monstre (template) : "+template.getId());
      e.printStackTrace();
    }
    _resistencias = resists;
    if(!allSpells.equalsIgnoreCase(""))
    {
      String[] spells=allSpells.split(";");

      for(String str : spells)
      {
        if(str.equals(""))
          continue;
        String[] spellInfo=str.split("@");
        int id,lvl;

        try
        {
          id=Integer.parseInt(spellInfo[0]);
          lvl=Integer.parseInt(spellInfo[1]);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          continue;
        }

        if(id==0||lvl==0)
          continue;
        Spell spell=Main.world.getSort(id);
        if(spell==null)
          continue;
        SortStats spellStats=spell.getStatsByLevel(lvl);
        if(spellStats==null)
          continue;
        this.spells.put(id,spellStats);
      }
    }
  }

  private MobGrade(Monster template, int grade, int level, int pdv, int pdvMax, int pa, int pm, Map<Integer, Integer> stats, ArrayList<Integer> statsInfos, Map<Integer, SortStats> spells, int xp, int n)
  {
    this.size=100+n*pSize;
    this.template=template;
    this.grade=grade;
    this.level=level;
    this.pdv=pdv;
    this.pdvMax=pdvMax;
    this.pa=pa;
    this.pm=pm;
    this.stats=stats;
    this.statsInfos=statsInfos;
    this.spells=spells;
    this.inFightId=-1;
    this.baseXp=xp;
  }
  public String getSpellss() {
      return _spells;
  }
  public String getResistencias() {
      return _resistencias;
  }
  public MobGrade getCopy()
  {
    Map<Integer, Integer> newStats=new HashMap<Integer, Integer>();
    newStats.putAll(this.stats);
    int n=(this.size-100)/pSize;
    return new MobGrade(this.template,this.grade,this.level,this.pdv,this.pdvMax,this.pa,this.pm,newStats,this.statsInfos,this.spells,this.baseXp,n);
  }

  //v2.7 - Replaced String += with StringBuilder
  public void refresh()
  {
    if(this.spells.isEmpty())
      return;
    StringBuilder spells=new StringBuilder();
    for(Entry<Integer, SortStats> entry : this.spells.entrySet())
      spells.append((spells.toString().isEmpty() ? entry.getKey()+","+entry.getValue().getLevel() : ";"+entry.getKey()+","+entry.getValue().getLevel()));
    this.spells.clear();
    if(!spells.toString().equalsIgnoreCase(""))
    {
      for(String split : spells.toString().split("\\;"))
      {
        int id=Integer.parseInt(split.split("\\,")[0]);
        this.spells.put(id,Main.world.getSort(id).getStatsByLevel(Integer.parseInt(split.split("\\,")[1])));
      }
    }
  }

  public int getSize()
  {
    return this.size;
  }

  public Monster getTemplate()
  {
    return this.template;
  }

  public int getGrade()
  {
    return this.grade;
  }

  public int getLevel()
  {
    return this.level;
  }

  public int getPdv()
  {
    return this.pdv;
  }

  public void setPdv(int pdv)
  {
    this.pdv=pdv;
  }

  public int getPdvMax()
  {
    return this.pdvMax;
  }

  public int getInFightID()
  {
    return this.inFightId;
  }

  public void setInFightID(int i)
  {
    this.inFightId=i;
  }

  public int getInit()
  {
    return this.init;
  }

  public int getPa()
  {
    return this.pa;
  }

  public int getPm()
  {
    return this.pm;
  }

  public int getBaseXp()
  {
    return this.baseXp;
  }

  public GameCase getFightCell()
  {
    return this.fightCell;
  }
  public String packetSpellsList() {
		char[] positions = { 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
				'k', 'l', 'm', 'n' };
		int i = 0;
		StringBuilder packet = new StringBuilder();
		for (SortStats SS : spells.values()) {

			packet.append(SS.getSpellID()).append("~")
					.append(SS.getLevel()).append("~")
					.append(positions[i++]).append(";");

		}
		return packet.toString();
	}
  public void setFightCell(GameCase cell)
  {
    this.fightCell=cell;
  }

  public ArrayList<SpellEffect> getBuffs()
  {
    return this.fightBuffs;
  }

  public Stats getStats()
  {
    if(this.getTemplate().getId()==42&&!stats.containsKey(Constant.STATS_ADD_SUM))
      stats.put(Constant.STATS_ADD_SUM,5);
    if(this.stats.get(-1)!=null)
    {
      Map<Integer, Integer> stats=new HashMap<>();
      stats.putAll(this.stats);
      stats.remove(-1);
      stats.remove(-100);

      int random=Formulas.getRandomValue(210,214);
      int one=this.stats.get(-1),all=this.stats.get(-100);

      stats.put(Constant.STATS_ADD_RP_NEU,(random==Constant.STATS_ADD_RP_NEU ? one : all));
      stats.put(Constant.STATS_ADD_RP_TER,(random==Constant.STATS_ADD_RP_TER ? one : all));
      stats.put(Constant.STATS_ADD_RP_FEU,(random==Constant.STATS_ADD_RP_FEU ? one : all));
      stats.put(Constant.STATS_ADD_RP_EAU,(random==Constant.STATS_ADD_RP_EAU ? one : all));
      stats.put(Constant.STATS_ADD_RP_AIR,(random==Constant.STATS_ADD_RP_AIR ? one : all));

      return new Stats(stats);
    }
    return new Stats(this.stats);
  }

  public Map<Integer, SortStats> getSpells()
  {
    return this.spells;
  }

  //2.6 - Better hp scaling
  public void modifStatByInvocator(final Fighter caster, int mobID)
  {
    if(mobID==116) //Special scaling for sacrifical doll
    {
      if(caster.getPersonnage()!=null)
      {
        double casterVit=caster.getPersonnage().getMaxPdv();
        pdv=(int)((pdvMax)+(casterVit*0.02));
        pdvMax=pdv;
      }
      double casterWis=caster.getPersonnage().getTotalStats().getEffect(Constant.STATS_ADD_SAGE);
      if(casterWis<0)
        casterWis=0;
      double casterAgi=caster.getPersonnage().getTotalStats().getEffect(Constant.STATS_ADD_AGIL);
      if(casterAgi<0)
        casterAgi=0;
      double agili=stats.get(Constant.STATS_ADD_AGIL)+(casterAgi*0.3);
      double sages=stats.get(Constant.STATS_ADD_SAGE)+(casterWis*0.2);
      stats.put(Constant.STATS_ADD_AGIL,(int)agili);
      stats.put(Constant.STATS_ADD_SAGE,(int)sages);
    }
    else
    {
      if(caster.getPersonnage()!=null)
      {
        double casterVit=caster.getPersonnage().getMaxPdv();
        double modifier=((casterVit*pdvMax*0.15)/225);
        if(modifier>800)
          modifier=800;
        pdv=(int)(pdvMax+modifier);
        pdvMax=pdv;
      }
      double casterWis=caster.getPersonnage().getTotalStats().getEffect(Constant.STATS_ADD_SAGE);
      if(casterWis<0)
        casterWis=0;
      double casterStr=caster.getPersonnage().getTotalStats().getEffect(Constant.STATS_ADD_FORC);
      if(casterStr<0)
        casterStr=0;
      double casterInt=caster.getPersonnage().getTotalStats().getEffect(Constant.STATS_ADD_INTE);
      if(casterInt<0)
        casterInt=0;
      double casterCha=caster.getPersonnage().getTotalStats().getEffect(Constant.STATS_ADD_CHAN);
      if(casterCha<0)
        casterCha=0;
      double casterAgi=caster.getPersonnage().getTotalStats().getEffect(Constant.STATS_ADD_AGIL);
      if(casterAgi<0)
        casterAgi=0;
      double casterSummons=caster.getPersonnage().getTotalStats().getEffect(Constant.STATS_ADD_SUM);
      if(casterSummons<0)
        casterSummons=0;
      double sages=stats.get(Constant.STATS_ADD_SAGE)+(casterWis*0.2);
      double force=stats.get(Constant.STATS_ADD_FORC)+(casterStr*0.5);
      double intel=stats.get(Constant.STATS_ADD_INTE)+(casterInt*0.5);
      double chance=stats.get(Constant.STATS_ADD_CHAN)+(casterCha*0.5);
      double agili=stats.get(Constant.STATS_ADD_AGIL)+(casterAgi*0.5);
      double summons=1+(casterSummons*0.5);
      stats.put(Constant.STATS_ADD_SAGE,(int)sages);
      stats.put(Constant.STATS_ADD_FORC,(int)force);
      stats.put(Constant.STATS_ADD_INTE,(int)intel);
      stats.put(Constant.STATS_ADD_CHAN,(int)chance);
      stats.put(Constant.STATS_ADD_AGIL,(int)agili);
      stats.put(Constant.STATS_ADD_SUM,(int)summons);
    }
  }
}
