package soufix.fight;

import soufix.area.map.GameCase;
import soufix.client.Player;
import soufix.client.other.Stats;
import soufix.common.Formulas;
import soufix.common.SocketManager;
import soufix.entity.Collector;
import soufix.entity.Prism;
import soufix.entity.monster.MobGrade;
import soufix.fight.spells.LaunchedSpell;
import soufix.fight.spells.Spell;
import soufix.fight.spells.SpellEffect;
import soufix.guild.Guild;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.utility.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

//v2.6 - invisible prediction AI
public class Fighter implements Comparable<Fighter>
{
  public int nbrInvoc;
  public boolean inLancer=false;
  public boolean isStatique=false;
  private int id=0;
  private boolean canPlay=false;
  private Fight fight;
  private int type=0; // 1 : Personnage, 2 : Mob, 5 : Perco
  private MobGrade mob=null;
  private Player perso=null;
  private Player _double=null;
  private Collector collector=null;
  private Prism prism=null;
  private int team=-2;
  private GameCase cell;
  private int pdvMax;
  private int pdv;
  private boolean isDead;
  private boolean hasLeft;
  private int gfxId;
  private Fighter isHolding;
  private Fighter holdedBy;
  private Fighter oldCible=null;
  private Fighter invocator;
  private boolean levelUp=false;
  private boolean isDeconnected=false;
  private int turnRemaining=0;
  private int nbrDisconnection=0;
  private boolean isTraqued=false;
  private Stats stats;
  private Map<Integer, Integer> state=new HashMap<Integer, Integer>();
  private CopyOnWriteArrayList<SpellEffect> fightBuffs=new CopyOnWriteArrayList<SpellEffect>();
  private Map<Integer, Integer> chatiValue=new HashMap<Integer, Integer>();
  private ArrayList<LaunchedSpell> launchedSpell=new ArrayList<LaunchedSpell>();
  public Pair<Byte, Long> killedBy;
  public GameCase lastInvisCell=null;
  public int lastInvisMP=-1;
  private boolean hadSober=false;
  private boolean justTrapped=false;
  public long chamkar = 0;
  public int tour = 0;
  public long start_turn = 0L;
  public int invocado = 0;
  private StringBuilder _stringBuilderGTM = new StringBuilder();

public Fighter(Fight f, MobGrade mob)
  {
    this.fight=f;
    this.type=2;
    this.mob=mob;
    setId(mob.getInFightID());
    this.pdvMax=mob.getPdvMax();
    this.pdv=mob.getPdv();
    this.setGfxId(getDefaultGfx());
  }

  public Fighter(Fight f, Player player)
  {
    this.fight=f;
    if(player._isClone)
    {
      this.type=10;
      setDouble(player);
    }
    else
    {
      this.type=1;
      this.perso=player;
    }
    setId(player.getId());
    this.pdvMax=player.getMaxPdv();
    this.pdv=player.getCurPdv();
    this.setGfxId(getDefaultGfx());
  }

  public Fighter(Fight f, Collector Perco)
  {
    this.fight=f;
    this.type=5;
    setCollector(Perco);
    setId(-1);
    this.pdvMax=(Main.world.getGuild(Perco.getGuildId()).getLvl()*100);
    this.pdv=(Main.world.getGuild(Perco.getGuildId()).getLvl()*100);
    this.setGfxId(6000);
  }

  public Fighter(Fight Fight, Prism Prisme)
  {
    this.fight=Fight;
    this.type=7;
    setPrism(Prisme);
    setId(-1);
    this.pdvMax=Prisme.getLevel()*10000;
    this.pdv=Prisme.getLevel()*10000;
    this.setGfxId(Prisme.getAlignement()==1 ? 8101 : 8100);
    Prisme.refreshStats();
  }

  public int getId()
  {
    return this.id;
  }

  public void setId(int id)
  {
    this.id=id;
  }

  public boolean canPlay()
  {
    return this.canPlay;
  }

  public void setCanPlay(boolean canPlay)
  {
    this.canPlay=canPlay;
  }

  public Fight getFight()
  {
    return this.fight;
  }

  public int getType()
  {
    return this.type;
  }


  public int getTour() {
	return tour;
}

public void setTourplus() {
	this.tour = tour+1;
}
  public MobGrade getMob()
  {
    if(this.type==2)
      return this.mob;
    return null;
  }

  public boolean isMob()
  {
    return (this.mob!=null);
  }

  public Player getPersonnage()
  {
    if(this.type==1)
      return this.perso;
    return null;
  }

  public Player getDouble()
  {
    return _double;
  }

  public boolean isDouble()
  {
    return (this._double!=null);
  }

  public void setDouble(Player _double)
  {
    this._double=_double;
  }

  public Collector getCollector()
  {
    if(this.type==5)
      return this.collector;
    return null;
  }

  public boolean isCollector()
  {
    return (this.collector!=null);
  }

  public void setCollector(Collector collector)
  {
    this.collector=collector;
  }

  public Prism getPrism()
  {
    if(this.type==7)
      return this.prism;
    return null;
  }

  public void setPrism(Prism prism)
  {
    this.prism=prism;
  }

  public boolean isPrisme()
  {
    return (this.prism!=null);
  }

  public int getTeam()
  {
    return this.team;
  }

  public void setTeam(int i)
  {
    this.team=i;
  }

  public int getTeam2()
  {
    return this.fight.getTeamId(getId());
  }

  public int getOtherTeam()
  {
    return this.fight.getOtherTeamId(getId());
  }

  public GameCase getCell()
  {
    return this.cell;
  }

  public void setCell(GameCase cell)
  {
    this.cell=cell;
  }

  public int getPdvMax()
  {
    return this.pdvMax+getBuffValue(Constant.STATS_ADD_VITA);
  }

  public void removePdvMax(int pdv)
  {
    this.pdvMax=this.pdvMax-pdv;
    if(this.pdv>this.pdvMax)
      this.pdv=this.pdvMax;
  }

  public int getPdv()
  {
    return (this.pdv+getBuffValue(Constant.STATS_ADD_VITA));
  }

  public void setPdvMax(int pdvMax)
  {
    this.pdvMax=pdvMax;
  }

  public void setPdv(int pdv)
  {
    this.pdv=pdv;
    if(this.pdv>this.pdvMax)
      this.pdv=this.pdvMax;
  }

  public void removePdv(Fighter caster, int pdv)
  {
    if(pdv>0)
      this.getFight().getAllChallenges().values().stream().filter(challenge -> challenge!=null).forEach(challenge -> challenge.onFighterAttacked(caster,this));
    this.pdv-=pdv;
  }

  public void fullPdv()
  {
    this.pdv=this.pdvMax;
  }

  public boolean isFullPdv()
  {
    return this.pdv==this.pdvMax;
  }

  public boolean isDead()
  {
    return this.isDead;
  }

  public void setIsDead(boolean isDead)
  {
    this.isDead=isDead;
  }

  public boolean hasLeft()
  {
    return this.hasLeft;
  }

  public void setLeft(boolean hasLeft)
  {
    this.hasLeft=hasLeft;
  }

  public Fighter getIsHolding()
  {
    return this.isHolding;
  }

  public void setIsHolding(Fighter isHolding)
  {
    this.isHolding=isHolding;
  }

  public Fighter getHoldedBy()
  {
    return this.holdedBy;
  }

  public void setHoldedBy(Fighter holdedBy)
  {
    this.holdedBy=holdedBy;
  }

  public Fighter getOldCible()
  {
    return this.oldCible;
  }

  public void setOldCible(Fighter cible)
  {
    this.oldCible=cible;
  }

  public Fighter getInvocator()
  {
    return this.invocator;
  }

  public void setInvocator(Fighter invocator)
  {
    this.invocator=invocator;
  }

  public boolean isInvocation()
  {
    return (this.invocator!=null);
  }

  public boolean getLevelUp()
  {
    return this.levelUp;
  }

  public void setLevelUp(boolean levelUp)
  {
    this.levelUp=levelUp;
  }

  public void Disconnect()
  {
    if(this.isDeconnected)
      return;
    this.isDeconnected=true;
    this.turnRemaining=20;
    this.nbrDisconnection++;
  }

  public void Reconnect()
  {
    this.isDeconnected=false;
    this.turnRemaining=0;
  }

  public boolean isDeconnected()
  {
    return !this.hasLeft&&this.isDeconnected;
  }

  public int getTurnRemaining()
  {
    return this.turnRemaining;
  }

  public void setTurnRemaining()
  {
    this.turnRemaining--;
  }

  public int getNbrDisconnection()
  {
    return this.nbrDisconnection;
  }

  public boolean getTraqued()
  {
    return this.isTraqued;
  }

  public void setTraqued(boolean isTraqued)
  {
    this.isTraqued=isTraqued;
  }

  public void setState(int id, int t, int casterId)
  {
    if(t!=0)
    {
      if(state.get(id)!=null) //fighter already has same state
      {
        if(state.get(id)==-1||state.get(id)>t) //infite duration state or current state lasts longer than parameter state
          return;
        else //current state lasts shorter than parameter state, refresh state
        {
          state.remove(id);
          state.put(id,t);
        }
      }
      else //fighter does not have parameter state
      {
        state.put(id,t);
      }
    }
    else //t=0 removes state
    {
      this.state.remove(id);
      if(id == 300 || id == 20)
    	  return;
      SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,new StringBuilder(String.valueOf(casterId)).toString(),String.valueOf(this.getId())+","+id+",0");
    }
  }

  public int getState(int id)
  {
    return this.state.get(id)!=null ? this.state.get(id) : -1;
  }

  public boolean haveState(int id)
  {
    for(Map.Entry<Integer, Integer> state : this.state.entrySet())
      if(state.getKey()==id)
        return true;
    return false;
  }

  public void sendState(Player p)
  {
    if(p.getAccount()!=null&&p.getGameClient()!=null)
      for(Entry<Integer, Integer> state : this.state.entrySet())
        SocketManager.GAME_SEND_GA_PACKET(p.getGameClient(),7+"",950+"",getId()+"",getId()+","+state.getKey()+",1");
  }

  public boolean haveInvocation()
  {
    for(Entry<Integer, Fighter> entry : this.getFight().getTeam(this.getTeam2()).entrySet())
    {
      Fighter f=entry.getValue();
      if(f.isInvocation())
        if(f.getInvocator()==this)
          return true;
    }
    return false;
  }

  public int nbInvocation()
  {
    int i=0;
    for(Entry<Integer, Fighter> entry : this.getFight().getTeam(this.getTeam2()).entrySet())
    {
      Fighter f=entry.getValue();
      if(f.isInvocation()&&!f.isStatique)
        if(f.getInvocator()==this)
          i++;
    }
    return i;
  }

  public CopyOnWriteArrayList<SpellEffect> getFightBuff()
  {
    return this.fightBuffs;
  }

  private Stats getFightBuffStats()
  {
    Stats stats=new Stats();
    for(SpellEffect entry : this.fightBuffs)
      stats.addOneStat(entry.getEffectID(),entry.getValue());
    return stats;
  }

  public int getBuffValue(int id)
  {
    int value=0;
    for(SpellEffect entry : this.fightBuffs)
      if(entry.getEffectID()==id)
        value+=entry.getValue();
    return value;
  }

  public SpellEffect getBuff(int id)
  {
    for(SpellEffect entry : this.fightBuffs)
      if(entry.getEffectID()==id&&entry.getDuration()>0)
        return entry;
    return null;
  }

  public ArrayList<SpellEffect> getBuffsByEffectID(int effectID)
  {
    ArrayList<SpellEffect> buffs=new ArrayList<SpellEffect>();
    buffs.addAll(this.fightBuffs.stream().filter(buff -> buff.getEffectID()==effectID).collect(Collectors.toList()));
    return buffs;
  }

  public Stats getTotalStatsLessBuff()
  {
    Stats stats=new Stats(new HashMap<>());
    if(this.type==1)
      stats=this.perso.getTotalStats();
    if(this.type==2)
      if(this.stats==null)
        this.stats=this.mob.getStats();
    if(this.type==5)
      stats=Main.world.getGuild(getCollector().getGuildId()).getStatsFight();
    if(this.type==7)
      stats=getPrism().getStats();
    if(this.type==10)
      stats=getDouble().getTotalStats();
    return stats;
  }

  public boolean hasBuff(int id)
  {
    for(SpellEffect entry : this.fightBuffs) {
    	if(entry == null)
    		continue;
      if(entry.getEffectID()==id&&entry.getDuration()>0)
        return true;
    }
    return false;
  }

  //v2.4 - selfbuff duration list fix
  public void addBuff(int id, int val, int duration, int turns, boolean debuff, int spellID, String args, Fighter caster, boolean isStart)
  {
    if(this.mob!=null)
      for(int id1 : Constant.STATIC_INVOCATIONS)
        if(id1==this.mob.getTemplate().getId())
          return;
    //v2.0 - infinite duration spells fix, damage reflect spell fix
    if(id==106) //reflect
    {
      if(Config.getInstance().lessDurationSpells.contains(","+Integer.toString(spellID)+",")) //selfbuff less duration
        this.fightBuffs.add(new SpellEffect(id,val,duration,turns,debuff,caster,val+";;",spellID));
      else if(duration==-1) //infinite duration
        this.fightBuffs.add(new SpellEffect(id,val,9999999,turns,debuff,caster,val+";;",spellID));
      else
        this.fightBuffs.add(new SpellEffect(id,val,(this.canPlay ? duration+1 : duration),turns,debuff,caster,val+";;",spellID));
    }
    else if(id==128&&(spellID==3500||spellID==3501)) //reflect
    {
      this.fightBuffs.add(new SpellEffect(id,val,duration,turns,debuff,caster,val+";;",spellID));
    }
    else //standard
    {
      if(Config.getInstance().lessDurationSpells.contains(","+Integer.toString(spellID)+",")) //selfbuff less duration
        this.fightBuffs.add(new SpellEffect(id,val,duration,turns,debuff,caster,args,spellID));
      else if(duration==-1) //infinite duration
        this.fightBuffs.add(new SpellEffect(id,val,9999999,turns,debuff,caster,args,spellID));
      else if(spellID==89&&id==101) //devotement fix
        this.fightBuffs.add(new SpellEffect(id,val,duration,turns,debuff,caster,args,spellID));
      else if(spellID==908&&id==111) //devotement fix
        this.fightBuffs.add(new SpellEffect(id,val,duration,turns,debuff,caster,args,spellID));
      else //normal
        this.fightBuffs.add(new SpellEffect(id,val,(this.canPlay ? duration+1 : duration),turns,debuff,caster,args,spellID));
    }

    switch(id)
    {
      case 6://Renvoie de sort
        SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),-1,val+"","10","",duration,spellID);
        break;
      case 79://Chance éca
        val=Integer.parseInt(args.split(";")[0]);
        String valMax=args.split(";")[1];
        String chance=args.split(";")[2];
        SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,valMax,chance,"",duration,spellID);
        break;
      case 85:
      case 86:
      case 87:
      case 88:
      case 89:
        val=Integer.parseInt(args.split(";")[0]);
        String valMax1=args.split(";")[1];
        if(valMax1.compareTo("-1")==0)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
        else if(valMax1.compareTo("-1")!=0)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,valMax1,"","",duration,spellID);
        break;
      case 96:
      case 97:
      case 98://Poison insidieux
      case 99:
      case 100:
      case 106:
        val=Integer.parseInt(args.split(";")[0]);
        String valMax4=args.split(";")[1];
        if(valMax4.compareTo("-1")==0)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
        else if(valMax4.compareTo("-1")!=0)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,valMax4,"","",duration,spellID);
        break;
      case 131: //AP Poisons
        if(duration==-1)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,args.split(";")[1],"","",duration-1,spellID);
        else
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,args.split(";")[1],"","",duration,spellID);
        break;
      case 107://Mot d'épine (2à3), Contre(3)
      case 108://Mot de Régénération, Tout ou rien
      case 165://Maîtris
      case 781://MAX
      case 782://MIN
        val=Integer.parseInt(args.split(";")[0]);
        String valMax2=args.split(";")[1];
        if(valMax2.compareTo("-1")==0||spellID==82||spellID==94)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
        else if(valMax2.compareTo("-1")!=0)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,valMax2,"","",duration,spellID);
        break;
      case 606:
      case 607:
      case 608:
      case 609:
      case 611:
        // de X sur Y tours
        String jet=args.split(";")[5];
        int min=Formulas.getMinJet(jet);
        int max=Formulas.getMaxJet(jet);
        SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),min,""+max,""+max,"",duration,spellID);
        break;
      case 788://Fait apparaitre message le temps de buff sacri Chatiment de X sur Y tours
        val=Integer.parseInt(args.split(";")[1]);
        String valMax3=args.split(";")[2];
        if(Integer.parseInt(args.split(";")[0])==108)
          return;
        SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,""+val,""+valMax3,"",duration,spellID);
        break;
      case 950:
    	  if(spellID == 413) {
    		  SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
    	  }
    	  if(spellID == 167 || spellID == 425 || spellID == 1709 || spellID == 2000) {
    		  SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
    	  }
    	  if(val != -1) 
        if(spellID==16)
        {
          if(getId()!=caster.getId())
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
          else
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration-1,spellID);
        }
        else if(spellID==20)
        {
          if(this!=caster)
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration+1,spellID);
          else
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
        }
        else
        {
          if(duration==-1)
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration-1,spellID);
          else if(duration!=1||spellID==101||spellID==2083) //roulette and doplesque roulette two turns
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
          else if(duration==1&&spellID==83&&id==120) //Hand self-buff duration
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
          else
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration-1,spellID);
          break;
        }
        break;
      default:
        if(spellID==20)
        {
          if(this!=caster)
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
          else
            SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration-1,spellID);
        }
        else if(spellID==908)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration-1,spellID);
        else if(duration==-1)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration-1,spellID);
        else if(duration!=1||spellID==101||spellID==2083) //roulette and doplesque roulette two turns
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
        else if(duration==1&&spellID==83&&id==120) //Hand self-buff duration
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
        else
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration-1==0 ? duration : duration-1,spellID);
        break;
    }
    SocketManager.ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_TODOS(this.getFight(), 7);//test refresh with mob
  }

  //v2.8 - only add tooltip not effect
  public void addBuffTooltip(int id, int val, int duration, int turns, boolean debuff, int spellID, String args, Fighter caster, boolean isStart)
  {
    if(this.mob!=null)
      for(int id1 : Constant.STATIC_INVOCATIONS)
        if(id1==this.mob.getTemplate().getId())
          return;

    switch(id)
    {
      case 6://Renvoie de sort
        SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),-1,val+"","10","",duration,spellID);
        break;
      case 79://Chance éca
        val=Integer.parseInt(args.split(";")[0]);
        String valMax=args.split(";")[1];
        String chance=args.split(";")[2];
        SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,valMax,chance,"",duration,spellID);
        break;
      case 85:
      case 86:
      case 87:
      case 88:
      case 89:
        val=Integer.parseInt(args.split(";")[0]);
        String valMax1=args.split(";")[1];
        if(valMax1.compareTo("-1")==0)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
        else if(valMax1.compareTo("-1")!=0)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,valMax1,"","",duration,spellID);
        break;
      case 96:
      case 97:
      case 98://Poison insidieux
      case 99:
      case 100:
      case 106:
        if(duration!=-1)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,Integer.toString(val),"","",duration,spellID);
        else
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,Integer.toString(val),"","",duration-1,spellID);
        break;
      case 131: //AP Poisons
        if(duration==-1)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,args.split(";")[1],"","",duration-1,spellID);
        else
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,args.split(";")[1],"","",duration,spellID);
        break;
      case 107://Mot d'épine (2à3), Contre(3)
      case 108://Mot de Régénération, Tout ou rien
      case 165://Maîtris
      case 781://MAX
      case 782://MIN
        val=Integer.parseInt(args.split(";")[0]);
        String valMax2=args.split(";")[1];
        if(valMax2.compareTo("-1")==0||spellID==82||spellID==94)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
        else if(valMax2.compareTo("-1")!=0)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,valMax2,"","",duration,spellID);
        break;
      case 606:
      case 607:
      case 608:
      case 609:
      case 611:
        // de X sur Y tours
        String jet=args.split(";")[5];
        int min=Formulas.getMinJet(jet);
        int max=Formulas.getMaxJet(jet);
        SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),min,""+max,""+max,"",duration,spellID);
        break;
      case 788://Fait apparaitre message le temps de buff sacri Chatiment de X sur Y tours
        val=Integer.parseInt(args.split(";")[1]);
        String valMax3=args.split(";")[2];
        if(Integer.parseInt(args.split(";")[0])==108)
          return;
        SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,""+val,""+valMax3,"",duration,spellID);
        break;
      default:
        if(duration==-1)
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration-1,spellID);
        else if(duration!=1||spellID==101||spellID==2083) //roulette and doplesque roulette two turns
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
        else if(duration==1&&spellID==83&&id==120) //Hand self-buff duration
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration,spellID);
        else
          SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(this.fight,7,id,getId(),val,"","","",duration-1,spellID);
        break;
    }
  }

  public void debuff()
  {
    if(!fightBuffs.isEmpty())
    {
      ArrayList<SpellEffect> newBuffs=new ArrayList<SpellEffect>();
      for(SpellEffect buff : fightBuffs)
      {
        if(!buff.isDebuffable()&&buff.getSpell()!=686) //not boozer
        {
          newBuffs.add(buff);
          continue;
        }
        switch(buff.getEffectID())
        {
          case Constant.STATS_ADD_PA:
          case Constant.STATS_ADD_PA2:
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight,7,101,getId()+"",getId()+",-"+buff.getValue());
            break;
          case Constant.STATS_ADD_PM:
          case Constant.STATS_ADD_PM2:
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight,7,127,getId()+"",getId()+",-"+buff.getValue());
            break;
          case Constant.STATS_REM_PA:
          //case Constant.STATS_REM_PA2:
        	  
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight,7,111,getId()+"",getId()+","+buff.getValue());
            this.setCurPa(this.fight,this.getCurPa(fight)+buff.getValue());
            break;
          case Constant.STATS_REM_PM:
          //case Constant.STATS_REM_PM2:
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight,7,128,getId()+"",getId()+",+"+buff.getValue());
            this.setCurPm(this.fight,this.getCurPm(fight)+buff.getValue());
            break;
          case Constant.STATS_REM_PM2:
        	  if(buff.getSpell()!=686)
        		break;
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight,7,128,getId()+"",getId()+",+"+buff.getValue());
            this.setCurPm(this.fight,this.getCurPm(fight)+buff.getValue());
            break;
        }
        if(this.getPersonnage()!=null)
          SocketManager.GAME_SEND_STATS_PACKET(this.getPersonnage());
      }
      fightBuffs.clear();
      if(!newBuffs.isEmpty())
      {
        newBuffs.stream().filter(spellEffect -> spellEffect!=null).forEach(spellEffect -> this.addBuff(spellEffect.getEffectID(),spellEffect.getValue(),spellEffect.getDuration(),spellEffect.getTurn(),spellEffect.isDebuffable(),spellEffect.getSpell(),spellEffect.getArgs(),this,true));
        this.fight.buffsToAdd.add(new Pair<Fighter, ArrayList<SpellEffect>>(this,newBuffs));
      }
    }
  }


  public void refreshEndTurnBuff()
  {
	  for(SpellEffect entry2 : this.fightBuffs)
    {
      SpellEffect entry=entry2;
      if(entry==null||entry.getCaster().isDead)
        continue;
      if(entry.decrementDuration()==0)
      {
    	  this.fightBuffs.remove(entry);
        switch(entry.getEffectID())
        {
          case 108:
            if(entry.getSpell()==441)
            {
              //Baisse des pdvs max
              this.pdvMax=(this.pdvMax-entry.getValue());

              //Baisse des pdvs actuel
              int pdv=0;
              if(this.pdv-entry.getValue()<=0)
              {
                pdv=0;
                this.fight.onFighterDie(this,this.holdedBy);
                this.fight.verifIfTeamAllDead();
              }
              else
                pdv=(this.pdv-entry.getValue());
              this.pdv=pdv;
            }
            break;

          case 150://Invisibilité
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight,7,150,entry.getCaster().getId()+"",getId()+",0");
            break;

          case 950:
            String args=entry.getArgs();
            int id=-1;
            try
            {
              id=Integer.parseInt(args.split(";")[2]);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }
            if(id==-1)
              return;
            if(id==Constant.STATE_DRUNK)
            {
              entry.getCaster().setState(Constant.STATE_SOBER,-1,this.getId()); //infinite duration
              SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,950,entry.getCaster().getId()+"",entry.getCaster().getId()+","+Constant.STATE_SOBER+",1");
            }
            setState(id,0,this.getId());
            break;
        }
      }
    }
  }

  public void initBuffStats()
  {
    if(this.type==1)
      this.fightBuffs.addAll(this.perso.get_buff().values().stream().collect(Collectors.toList()));
  }

  public void applyBeginningTurnBuff(Fight fight)
  {
    for(int effectID : Constant.BEGIN_TURN_BUFF)
    {
      ArrayList<SpellEffect> buffs=new ArrayList<>(this.fightBuffs);
      buffs.stream().filter(entry -> entry.getEffectID()==effectID).forEach(entry -> entry.applyBeginingBuff(fight,this));
    }
  }

  public ArrayList<LaunchedSpell> getLaunchedSorts()
  {
    return this.launchedSpell;
  }

  public void refreshLaunchedSort()
  {
    ArrayList<LaunchedSpell> copie=new ArrayList<>(this.launchedSpell);

    int i=0;
    for(LaunchedSpell S : copie)
    {
      S.actuCooldown();
      if(S.getCooldown()<=0)
      {
        this.launchedSpell.remove(i);
        i--;
      }
      i++;
    }
  }

  public void addLaunchedSort(Fighter target, Spell.SortStats sort, Fighter fighter)
  {
    LaunchedSpell launched=new LaunchedSpell(target,sort,fighter);
    this.launchedSpell.add(launched);
  }

  public Stats getTotalStats()
  {
    Stats stats=new Stats(new HashMap<>());
    if(this.type==1)
      stats=this.perso.getTotalStats();
    if(this.type==2)
      stats=this.mob.getStats();
    if(this.type==5)
      stats=Main.world.getGuild(getCollector().getGuildId()).getStatsFight();
    if(this.type==7)
      stats=this.getPrism().getStats();
    if(this.type==10)
      stats=this.getDouble().getTotalStats();

    if(this.type!=1)
      stats=Stats.cumulStatFight(stats,getFightBuffStats());

    return stats;
  }

  public int getMaitriseDmg(int id)
  {
    int value=0;
    for(SpellEffect entry : this.fightBuffs)
      if(entry.getSpell()==id)
        value+=entry.getValue();
    return value;
  }

  public boolean getSpellValueBool(int id)
  {
    for(SpellEffect entry : this.fightBuffs)
      if(entry.getSpell()==id)
        return true;
    return false;
  }

  public boolean testIfCC(int baseCrit)
  {
    if(baseCrit<2)
      return false;
    int agi=getTotalStats().getEffect(Constant.STATS_ADD_AGIL);
    if(agi<0)
      agi=0;
    int equipCrit=getTotalStats().getEffect(Constant.STATS_ADD_CC);
    int negativeCrit=getTotalStats().getEffect(Constant.STATS_REM_CC);
    return Formulas.isCriticalHit(baseCrit,equipCrit,negativeCrit,agi);
  }

  public boolean testIfCC(int baseCrit, Spell.SortStats sSort, Fighter fighter)
  {
    Player perso=fighter.getPersonnage();
    if(baseCrit<2)
      return false;
    int agi=getTotalStats().getEffect(Constant.STATS_ADD_AGIL);
    if(agi<0)
      agi=0;
    int equipCrit=getTotalStats().getEffect(Constant.STATS_ADD_CC);
    int negativeCrit=getTotalStats().getEffect(Constant.STATS_REM_CC);
    if(fighter.getType()==1&&perso.getItemClasseSpell().containsKey(sSort.getSpellID()))
    {
      int modi=perso.getItemClasseModif(sSort.getSpellID(),287);
      baseCrit-=modi;
    }
    return Formulas.isCriticalHit(baseCrit,equipCrit,negativeCrit,agi);
  }

  public int getInitiative()
  {
    if(this.type==1)
      return this.perso.getInitiative();
    if(this.type==2)
      return this.mob.getInit();
    if(this.type==5)
      return Main.world.getGuild(getCollector().getGuildId()).getLvl();
    if(this.type==7)
      return 0;
    if(this.type==10)
      return getDouble().getInitiative();
    return 0;
  }

  public int getPa()
  {
    switch(this.type)
    {
      case 1:
        return getTotalStats().getEffect(Constant.STATS_ADD_PA);
      case 2:
        return getTotalStats().getEffect(Constant.STATS_ADD_PA)+this.mob.getPa();
      case 5:
        return getTotalStats().getEffect(Constant.STATS_ADD_PM)+6;
      case 7:
        return getTotalStats().getEffect(Constant.STATS_ADD_PM)+6;
      case 10:
        return getTotalStats().getEffect(Constant.STATS_ADD_PA);
    }
    return 0;
  }

  public int getPm()
  {
    switch(this.type)
    {
      case 1: // personnage
        return getTotalStats().getEffect(Constant.STATS_ADD_PM);
      case 2: // mob
        return getTotalStats().getEffect(Constant.STATS_ADD_PM)+this.mob.getPm();
      case 5: // perco
        return getTotalStats().getEffect(Constant.STATS_ADD_PM)+4;
      case 7: // prisme
        return getTotalStats().getEffect(Constant.STATS_ADD_PM);
      case 10: // clone
        return getTotalStats().getEffect(Constant.STATS_ADD_PM);
    }
    return 0;
  }

  public int getPros()
  {
    switch(this.type)
    {
      case 1: // personnage
        return (getTotalStats().getEffect(Constant.STATS_ADD_PROS)+Math.round(getTotalStats().getEffect(Constant.STATS_ADD_CHAN)/10)+Math.round(getBuffValue(Constant.STATS_ADD_CHAN)/10));
      case 2: // mob
        if(this.isInvocation()) // Si c'est un coffre animé, la chance est égale é 1000*(1+lvlinvocateur/100)
        {
          int tempPros=0;
          for(SpellEffect prospecting : this.getBuffsByEffectID(Constant.STATS_ADD_PROS))
          {
            tempPros+=prospecting.getValue();
          }
          return tempPros+this.getInvocator().getPros();
        }
        else
          return (getTotalStats().getEffect(Constant.STATS_ADD_PROS)+Math.round(getBuffValue(Constant.STATS_ADD_CHAN)/10));
    }
    return 0;
  }

  public int getCurPa(Fight fight)
  {
    return fight.getCurFighterPa();
  }

  public void setCurPa(Fight fight, int pa)
  {
    fight.setCurFighterPa(fight.getCurFighterPa()+pa);
  }

  public int getCurPm(Fight fight)
  {
    return fight.getCurFighterPm();
  }

  public void setCurPm(Fight fight, int pm)
  {
    fight.setCurFighterPm(fight.getCurFighterPm()+pm);
  }

  public boolean canLaunchSpell(int spellID)
  {
    return this.getPersonnage().hasSpell(spellID)&&LaunchedSpell.cooldownGood(this,spellID);
  }

  public void unHide(int spellid)
  {
    //on retire le buff invi
    if(spellid!=-1)// -1 : CAC
    {
      switch(spellid)
      {
        case 66: //Poison
        case 181: //Earthquake
        case 196: //Poisoned Wind
        case 200: //Paralyzing Poison
        case 219: //Plissken's Poisoning
          return;
      }
    }
    ArrayList<SpellEffect> buffs=new ArrayList<SpellEffect>();
    buffs.addAll(getFightBuff());
    for(SpellEffect SE : buffs)
    {
      if(SE.getEffectID()==150)
        getFightBuff().remove(SE);
    }
    SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this.fight,7,150,getId()+"",getId()+",0");
    //On actualise la position
    SocketManager.GAME_SEND_GIC_PACKET_TO_FIGHT(this.fight,7,this);
  }

  public boolean isHide()
  {
    return hasBuff(150);
  }

  public int getPdvMaxOutFight()
  {
    if(this.perso!=null)
      return this.perso.getMaxPdv();
    if(this.mob!=null)
      return this.mob.getPdvMax();
    return 0;
  }

  public Map<Integer, Integer> getChatiValue()
  {
    return this.chatiValue;
  }

  public int getDefaultGfx()
  {
    if(this.perso!=null)
      return this.perso.getGfxId();
    if(this.mob!=null)
      return this.mob.getTemplate().getGfxId();
    return 0;
  }

  public int getLvl()
  {
    if(this.type==1)
      return this.perso.getLevel();
    if(this.type==2)
      return this.mob.getLevel();
    if(this.type==5)
      return Main.world.getGuild(getCollector().getGuildId()).getLvl();
    if(this.type==7)
      return getPrism().getLevel();
    if(this.type==10)
      return getDouble().getLevel();
    return 0;
  }

  public String xpString(String str)
  {
    if(this.perso!=null)
    {
      int max=this.perso.getLevel()+1;
      if(max>Main.world.getExpLevelSize())
        max=Main.world.getExpLevelSize();
      return Main.world.getExpLevel(this.perso.getLevel()).perso+str+this.perso.getExp()+str+Main.world.getExpLevel(max).perso;
    }
    return "0"+str+"0"+str+"0";
  }

  public String getPacketsName()
  {
    if(this.type==1)
      return this.perso.getName();
    if(this.type==2)
      return this.mob.getTemplate().getId()+"";
    if(this.type==5)
      return (Integer.parseInt(Integer.toString(getCollector().getN1()),36)+","+Integer.parseInt(Integer.toString(getCollector().getN2()),36));
    if(this.type==7)
      return (getPrism().getAlignement()==1 ? 1111 : 1112)+"";
    if(this.type==10)
      return getDouble().getName();

    return "";
  }

  public String getGmPacket(char c, boolean withGm)
  {
    StringBuilder str=new StringBuilder();
    str.append(withGm ? "GM|" : "").append(c);
    str.append(getCell().getId()).append(";");
    str.append("1;0;");//1; = Orientation
    str.append(getId()).append(";");
    str.append(getPacketsName()).append(";");

    switch(this.type)
    {
      case 1://Perso
        str.append(this.perso.getClasse()).append(";");
        str.append(this.perso.getGfxId()).append("^").append(this.perso.get_size()).append(";");
        str.append(this.perso.getSexe()).append(";");
        str.append(this.perso.getLevel()).append(";");
        str.append(this.perso.get_align()).append(",");
        str.append("0").append(",");
        str.append((this.perso.is_showWings() ? this.perso.getGrade() : "0")).append(",");
        str.append(this.perso.getLevel()+this.perso.getId());
        if(this.perso.is_showWings()&&this.perso.getDeshonor()>0)
        {
          str.append(",");
          str.append(this.perso.getDeshonor()>0 ? 1 : 0).append(';');
        }
        else
        {
          str.append(";");
        }
        int color1=this.perso.getColor1(),color2=this.perso.getColor2(),color3=this.perso.getColor3();
        if(this.perso.getObjetByPos(Constant.ITEM_POS_MALEDICTION)!=null)
          if(this.perso.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId()==10838)
          {
            color1=16342021;
            color2=16342021;
            color3=16342021;
          }
        str.append((color1==-1 ? "-1" : Integer.toHexString(color1))).append(";");
        str.append((color2==-1 ? "-1" : Integer.toHexString(color2))).append(";");
        str.append((color3==-1 ? "-1" : Integer.toHexString(color3))).append(";");
        str.append(this.perso.getGMStuffString()).append(";");
        str.append(getPdv()).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_PA)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_PM)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_RP_NEU)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_RP_TER)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_RP_FEU)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_RP_EAU)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_RP_AIR)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_AFLEE)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_MFLEE)).append(";");
        str.append(this.team).append(";");
        if(this.perso.isOnMount()&&this.perso.getMount()!=null)
          str.append(this.perso.getMount().getStringColor(this.perso.parsecolortomount()));
        str.append(";");
        break;
      case 2://Mob
        str.append("-2;");
        str.append(this.mob.getTemplate().getGfxId()).append("^").append(this.mob.getSize()).append(";");
        str.append(this.mob.getGrade()).append(";");
        str.append(this.mob.getTemplate().getColors().replace(",",";")).append(";");
        str.append("0,0,0,0;");
        str.append(this.getPdvMax()).append(";");
        str.append(this.mob.getPa()).append(";");
        str.append(this.mob.getPm()).append(";");
        str.append(this.team);
        break;
      case 5://Perco
        str.append("-6;");//Perco
        str.append("6000^100;");//GFXID^Size
        Guild G=Main.world.getGuild(this.collector.getGuildId());
        str.append(G.getLvl()).append(";");
        str.append("1;");
        str.append("2;4;");
        str.append((int)Math.floor(G.getLvl()/4)).append(";").append((int)Math.floor(G.getLvl()/4)).append(";").append((int)Math.floor(G.getLvl()/4)).append(";").append((int)Math.floor(G.getLvl()/4)).append(";").append((int)Math.floor(G.getLvl()/4)).append(";").append((int)Math.floor(G.getLvl()/4)).append(";").append((int)Math.floor(G.getLvl()/4)).append(";");//Résistances
        str.append(this.team);
        break;
      case 7://Prisme
        str.append("-2;");
        str.append(getPrism().getAlignement()==1 ? 8101 : 8100).append("^100;");
        str.append(getPrism().getLevel()).append(";");
        str.append("-1;-1;-1;");
        str.append("0,0,0,0;");
        str.append(this.getPdvMax()).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_PA)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_PM)).append(";");
        str.append(getTotalStats().getEffect(214)).append(";");
        str.append(getTotalStats().getEffect(210)).append(";");
        str.append(getTotalStats().getEffect(213)).append(";");
        str.append(getTotalStats().getEffect(211)).append(";");
        str.append(getTotalStats().getEffect(212)).append(";");
        str.append(getTotalStats().getEffect(160)).append(";");
        str.append(getTotalStats().getEffect(161)).append(";");
        str.append(this.team);
        break;
      case 10://Double
        str.append(getDouble().getClasse()).append(";");
        str.append(getDouble().getGfxId()).append("^").append(getDouble().get_size()).append(";");
        str.append(getDouble().getSexe()).append(";");
        str.append(getDouble().getLevel()).append(";");
        str.append(getDouble().get_align()).append(",");
        str.append("1,");//TODO
        str.append((getDouble().is_showWings() ? getDouble().getALvl() : "0")).append(",");
        str.append(getDouble().getId()).append(";");

        str.append((getDouble().getColor1()==-1 ? "-1" : Integer.toHexString(getDouble().getColor1()))).append(";");
        str.append((getDouble().getColor2()==-1 ? "-1" : Integer.toHexString(getDouble().getColor2()))).append(";");
        str.append((getDouble().getColor3()==-1 ? "-1" : Integer.toHexString(getDouble().getColor3()))).append(";");
        str.append(getDouble().getGMStuffString()).append(";");
        str.append(getPdv()).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_PA)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_PM)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_RP_NEU)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_RP_TER)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_RP_FEU)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_RP_EAU)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_RP_AIR)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_AFLEE)).append(";");
        str.append(getTotalStats().getEffect(Constant.STATS_ADD_MFLEE)).append(";");
        str.append(this.team).append(";");
        if(getDouble().isOnMount()&&getDouble().getMount()!=null)
          str.append(getDouble().getMount().getStringColor(getDouble().parsecolortomount()));
        str.append(";");
        break;
    }

    return str.toString();
  }

  @Override
  public int compareTo(Fighter t)
  {
    return ((this.getPros()>t.getPros()&&!this.isInvocation()) ? 1 : 0);
  }

  public int getGfxId()
  {
    return gfxId;
  }

  public void setGfxId(int gfxId)
  {
    this.gfxId=gfxId;
  }

  public boolean getHadSober()
  {
    return hadSober;
  }

  public void setHadSober(boolean hadSober)
  {
    this.hadSober=hadSober;
  }

  public boolean getJustTrapped()
  {
    return justTrapped;
  }

  public void setJustTrapped(boolean justTrapped)
  {
    this.justTrapped=justTrapped;
  }

  public StringBuilder getStringBuilderGTM() {
      return _stringBuilderGTM;
  }
  public void resetStringBuilderGTM() {
      _stringBuilderGTM = new StringBuilder();
  }

  public boolean esInvisible(final int idMirador) {
      if (idMirador != 0) {
          if (idMirador == this.id) {
              return false;
          }
      }
      if (hasBuff(150)) {
          return true;
      }
      return false;
  }
}