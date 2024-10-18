package soufix.entity.monster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.common.Formulas;
import soufix.common.PathFinding;
import soufix.common.SocketManager;

import soufix.fight.Fight;
import soufix.game.World;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.utility.Pair;

public class MobGroup
{

  private int id;
  private int cellId;
  private int spawnCellId;
  private int orientation=2;
  private int align=-1;
  private int starBonus=0;
  private int aggroDistance=0;
  private int subarea=-1;
  private boolean changeAgro=false;
  private boolean isFix=false;
  private boolean isExtraGroup=false;
  private Map<Integer, MobGrade> mobs=new HashMap<Integer, MobGrade>();
  private String condition="";
  private long spawnTime=System.currentTimeMillis();
  private ArrayList<GameObject> objects;
  private boolean isDynamic=false;
  private Fight fight=null;
  private int ID_PLAYER = 0;
  private String ip;

public MobGroup(int Aid, int Aalign, ArrayList<MobGrade> possibles, GameMap Map, int cell, int fixSize, int minSize, int maxSize, MobGrade extra, boolean dynamic)
  {
    id=Aid;
    align=Aalign;
    //Détermination du nombre de mob du groupe
    int rand=0;
    int nbr=0;
    if(fixSize>0&&fixSize<9)
      nbr=fixSize;
    else if(minSize!=-1&&maxSize!=-1&&maxSize!=0&&(minSize<maxSize))
    {
      if(minSize==3&&maxSize==8)
      {
        rand=Formulas.getRandomValue(0,99);
        if(rand<25) //3: 25%
          nbr=3;
        else if(rand<48) //4:23%
          nbr=4;
        else if(rand<51) //5:20%
          nbr=5;
        else if(rand<85) //6:17%
          nbr=6;
        else if(rand<95) //7:10%
          nbr=7;
        else
          //8:5%
          nbr=8;
      }
      else if(minSize==1&&maxSize==3)
      { // 21 - normalement tout astrub
        rand=Formulas.getRandomValue(0,99);
        if(rand<40) //1: 40%
          nbr=1;
        else if(rand<75)//2: 35%
          nbr=2;
        else //3: 25%
          nbr=3;
      }
      else if(minSize==1&&maxSize==5)
      {
        rand=Formulas.getRandomValue(0,99);
        if(rand<30) //3: 30%
          nbr=1;
        else if(rand<53) //4:23%
          nbr=2;
        else if(rand<73) //5:20%
          nbr=3;
        else if(rand<90) //6:17%
          nbr=4;
        else //8:10%
          nbr=5;
      }
      else if(minSize==1&&maxSize==4)
      {
        rand=Formulas.getRandomValue(0,99);
        if(rand<35) //3: 35%
          nbr=1;
        else if(rand<61) //4:26%
          nbr=2;
        else if(rand<82) //5:21%
          nbr=3;
        else //8:18%
          nbr=4;
      }
      else
        nbr=Formulas.getRandomValue(minSize,maxSize);
    }
    else if(minSize==-1)
    {
      switch(maxSize)
      {
        case 0:
          return;
        case 1:
          nbr=1;
          break;
        case 2:
          nbr=Formulas.getRandomValue(1,2); //1:50%   2:50%
          break;
        case 3:
          nbr=Formulas.getRandomValue(1,3); //1:33.3334%  2:33.3334%  3:33.3334%
          break;
        case 4:
          rand=Formulas.getRandomValue(0,99);
          if(rand<22) //1:22%
            nbr=1;
          else if(rand<48) //2:26%
            nbr=2;
          else if(rand<74) //3:26%
            nbr=3;
          else //4:26%
            nbr=4;
          break;
        case 5:
          rand=Formulas.getRandomValue(0,99);
          if(rand<15) //1:15%
            nbr=1;
          else if(rand<35) //2:20%
            nbr=2;
          else if(rand<60) //3:25%
            nbr=3;
          else if(rand<85) //4:25%
            nbr=4;
          else //5:15%
            nbr=5;
          break;
        case 6:
          rand=Formulas.getRandomValue(0,99);
          if(rand<10) //1:10%
            nbr=1;
          else if(rand<25) //2:15%
            nbr=2;
          else if(rand<45) //3:20%
            nbr=3;
          else if(rand<65) //4:20%
            nbr=4;
          else if(rand<85) //5:20%
            nbr=5;
          else //6:15%
            nbr=6;
          break;
        case 7:
          rand=Formulas.getRandomValue(0,99);
          if(rand<9) //1:9%
            nbr=1;
          else if(rand<20) //2:11%
            nbr=2;
          else if(rand<35) //3:15%
            nbr=3;
          else if(rand<55) //4:20%
            nbr=4;
          else if(rand<75) //5:20%
            nbr=5;
          else if(rand<91) //6:16%
            nbr=6;
          else //7:9%
            nbr=7;
          break;
        default:
          rand=Formulas.getRandomValue(0,99);
          if(rand<9) //1:9%
            nbr=1;
          else if(rand<20) //2:11%
            nbr=2;
          else if(rand<33) //3:13%
            nbr=3;
          else if(rand<50) //4:17%
            nbr=4;
          else if(rand<67) //5:17%
            nbr=5;
          else if(rand<80) //6:13%
            nbr=6;
          else if(rand<91) //7:11%
            nbr=7;
          else //8:9%
            nbr=8;
          break;
      }
    }
    else
    {
      switch(minSize)
      {
        case 1:
          rand=Formulas.getRandomValue(1,8);
          switch(rand)
          {
            case 1:
              nbr=1;
              break;
            case 2:
              nbr=2;
              break;
            case 3:
              nbr=3;
              break;
            case 4:
              nbr=4;
              break;
            case 5:
              nbr=5;
              break;
            case 6:
              nbr=6;
              break;
            case 7:
              nbr=7;
              break;
            case 8:
              nbr=8;
              break;
          }
          break;
        case 2:
          rand=Formulas.getRandomValue(2,8);
          switch(rand)
          {
            case 2:
              nbr=2;
              break;
            case 3:
              nbr=3;
              break;
            case 4:
              nbr=4;
              break;
            case 5:
              nbr=5;
              break;
            case 6:
              nbr=6;
              break;
            case 7:
              nbr=7;
              break;
            case 8:
              nbr=8;
              break;
          }
          break;
        case 3:
          rand=Formulas.getRandomValue(3,8);
          switch(rand)
          {
            case 3:
              nbr=3;
              break;
            case 4:
              nbr=4;
              break;
            case 5:
              nbr=5;
              break;
            case 6:
              nbr=6;
              break;
            case 7:
              nbr=7;
              break;
            case 8:
              nbr=8;
              break;
          }
          break;
        case 4:
          rand=Formulas.getRandomValue(4,8);
          switch(rand)
          {
            case 4:
              nbr=4;
              break;
            case 5:
              nbr=5;
              break;
            case 6:
              nbr=6;
              break;
            case 7:
              nbr=7;
              break;
            case 8:
              nbr=8;
              break;
          }
          break;
        case 5:
          rand=Formulas.getRandomValue(5,8);
          switch(rand)
          {
            case 5:
              nbr=5;
              break;
            case 6:
              nbr=6;
              break;
            case 7:
              nbr=7;
              break;
            case 8:
              nbr=8;
              break;
          }
          break;
        case 6:
          rand=Formulas.getRandomValue(6,8);
          switch(rand)
          {
            case 6:
              nbr=6;
              break;
            case 7:
              nbr=7;
              break;
            case 8:
              nbr=8;
              break;
          }
          break;
        case 7:
          rand=Formulas.getRandomValue(7,8);
          switch(rand)
          {
            case 7:
              nbr=7;
              break;
            case 8:
              nbr=8;
              break;
          }
          break;
        case 8:
          nbr=8;
          break;
        default:
          rand=Formulas.getRandomValue(1,8);
          switch(rand)
          {
            case 1:
              nbr=1;
              break;
            case 2:
              nbr=2;
              break;
            case 3:
              nbr=3;
              break;
            case 4:
              nbr=4;
              break;
            case 5:
              nbr=5;
              break;
            case 6:
              nbr=6;
              break;
            case 7:
              nbr=7;
              break;
            case 8:
              nbr=8;
              break;
          }
          break;
      }
    }
    int guid=-1;
    boolean haveSameAlign = false;

    if(extra!=null)
    {
      setIsExtraGroup(true);
      nbr--;
      this.mobs.put(guid,extra);
      guid--;
    }
    for (MobGrade mob : possibles)
    {
    	if (mob.getTemplate().getAlign() == this.align)
    	{
    		haveSameAlign = true;	
    	}
    }

    if (!haveSameAlign)
        return;//S'il n'y en a pas
    for(int a=0;a<nbr;a++)
    {
      MobGrade Mob=null;
      do
      {
        int random=Formulas.getRandomValue(0,possibles.size()-1); //on prend un mob au hasard dans le tableau
        Mob=possibles.get(random).getCopy();

      } while(Mob.getTemplate().getAlign()!=this.align);

      this.mobs.put(guid,Mob);
      if(Mob.getTemplate().getAggroDistance()>this.aggroDistance)
        this.aggroDistance=Mob.getTemplate().getAggroDistance();
      guid--;
    }

    if(this.aggroDistance<=0&&this.align!=Constant.ALIGNEMENT_NEUTRE)
      this.aggroDistance=3;
    this.cellId=(cell==-1 ? Map.getRandomFreeCellId() : cell);
    while(Map.containsForbiddenCellSpawn(this.cellId))
      this.cellId=Map.getRandomFreeCellId();
    if(this.cellId==0)
      return;
    this.spawnCellId=this.getCellId();
    this.orientation=(Formulas.getRandomValue(0,3)*2)+1;
    this.isFix=false;
    this.spawnTime=10000;

    this.setIsDynamic(dynamic);
  }

  public MobGroup(int id, short mapId, int cellId, String groupData, String objects, long time)
  {
    this.id=id;
    this.align=Constant.ALIGNEMENT_NEUTRE;
    this.cellId=cellId;
    this.spawnCellId=this.getCellId();
    this.isFix=false;
    this.orientation=(Formulas.getRandomValue(0,3)*2)+1;
    this.spawnTime=time;

    int guid=-1;

    for(String data : groupData.split(";"))
    {
      if(data.equalsIgnoreCase(""))
        continue;
      String[] infos=data.split(",");

      try
      {
        int idMonster=Integer.parseInt(infos[0]);
        int min=Integer.parseInt(infos[1]);
        int max=Integer.parseInt(infos[2]);
        Monster m=Main.world.getMonstre(idMonster);
        List<MobGrade> mgs=new ArrayList<MobGrade>();
        //on ajoute a la liste les grades possibles

        for(MobGrade MG : m.getGrades().values())
          if(MG.getLevel()>=min&&MG.getLevel()<=max)
            mgs.add(MG);
       
        if(mgs.isEmpty())
          continue;
        MobGrade chosen = mgs.get(Formulas.getRandomValue(0,mgs.size()-1));
        if(this.align==Constant.ALIGNEMENT_NEUTRE&&chosen.getTemplate().getAlign()!=Constant.ALIGNEMENT_NEUTRE)
        {
          if(chosen.getTemplate().getId()==372||chosen.getTemplate().getId()==415)
            this.align=Constant.ALIGNEMENT_BRAKMARIEN;
          else if(chosen.getTemplate().getId()==296)
            this.align=Constant.ALIGNEMENT_BONTARIEN;
          else
            this.align=Main.world.getMap(mapId).getSubArea().getAlignement();
        }
        
        //On prend un grade au hasard entre 0 et size -1 parmis les mobs possibles
        this.mobs.put(guid,chosen);
        if(m.getAggroDistance()>this.aggroDistance)
          this.aggroDistance=m.getAggroDistance();
        guid--;
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }

    for(Entry<Integer, MobGrade> mob : this.mobs.entrySet())// kralamour
      if(mob.getValue().getTemplate().getId()==423)
        this.orientation=3;
    if(this.aggroDistance<=0&&this.align!=Constant.ALIGNEMENT_NEUTRE)
      this.aggroDistance=3;
    if(Config.getInstance().HEROIC)
    if(!objects.isEmpty())
    {
      for(String value : objects.split(","))
      {
    	  if(value == null)
    		  continue;
        final GameObject gameObject=World.getGameObject(Integer.parseInt(value));
        if(this.objects==null&&Config.getInstance().HEROIC)
            this.objects=new ArrayList<>();
         if(gameObject != null)
         this.objects.add(gameObject);	 
        
      }
    }
  }

  public MobGroup(int id, int cellId, String groupData)
  {
    this.id=id;
    this.cellId=cellId;
    this.spawnCellId=this.getCellId();
    this.isFix=true;
    int guid=-1;
    @SuppressWarnings("unused")
    boolean star=false;
    int tempAlign=Constant.ALIGNEMENT_NEUTRE;
    for(String data : groupData.split(";"))
    {
      if(data.equalsIgnoreCase(""))
        continue;
      String[] infos=data.split(",");
      try
      {
        int idMonster=Integer.parseInt(infos[0]);
        int min=Integer.parseInt(infos[1]);
        int max=Integer.parseInt(infos[2]);
        Monster m=Main.world.getMonstre(idMonster);
        List<MobGrade> mgs=new ArrayList<MobGrade>();
        for(MobGrade MG : m.getGrades().values())
        {
          if(MG.getBaseXp()!=0)
            star=true;
          if(MG.getLevel()>=min&&MG.getLevel()<=max)
          {
            mgs.add(MG);
          }
        }
        if(mgs.isEmpty())
          continue;
        this.mobs.put(guid,mgs.get(Formulas.getRandomValue(0,mgs.size()-1))); //On prend un grade au hasard entre 0 et size -1 parmis les mobs possibles
        if(m.getAggroDistance()>this.aggroDistance)
          this.aggroDistance=m.getAggroDistance();
        if(m.getAlign()!=Constant.ALIGNEMENT_NEUTRE)
          tempAlign=m.getAlign();
        guid--;
      }
      catch(Exception e)
      {
        e.printStackTrace();
        System.out.println(groupData);
      }
    }

    this.align=tempAlign;
    if(this.aggroDistance<=0&&this.align!=Constant.ALIGNEMENT_NEUTRE)
      this.aggroDistance=3;

    this.orientation=(Formulas.getRandomValue(0,3)*2)+1;
  }

  public void setSubArea(int sa)
  {
    this.subarea=sa;
  }

  public void changeAgro()
  {
    if(!changeAgro)
    {
      if(this.haveMineur())
      {
        // 29 : sous-terrain
        // 96 : exploitation miniére d'astrub
        // 31 : passage vers brakmar
        if(this.subarea!=29&&this.subarea!=96&&this.subarea!=31)
        {
          this.removeAgro(118);
        }
      }
    }
    changeAgro=true;
  }

  public void removeAgro(int id)
  {
    this.aggroDistance=0;
    for(Entry<Integer, MobGrade> e : this.mobs.entrySet())
    {
      MobGrade mb=e.getValue();
      if(mb.getTemplate().getId()!=id)
      {
        if(mb.getTemplate().getAggroDistance()>this.aggroDistance)
        {
          this.aggroDistance=mb.getTemplate().getAggroDistance();
        }
      }
    }
  }
  public String Groupe_to_string()
  {
	  String groupe = null;
    for(Entry<Integer, MobGrade> e : this.mobs.entrySet())
    {
    	if(e == null)
    		continue;
      MobGrade mb=e.getValue();
      groupe += mb.getTemplate().getId();
      groupe += ",";
      groupe += mb.getLevel();
      groupe += ",";
      groupe += mb.getLevel();
      groupe += ";";
      groupe = groupe.replace("null", "");
    }
    return groupe;
  }

  public boolean haveMineur()
  {
    for(Entry<Integer, MobGrade> e : this.mobs.entrySet())
    {
      MobGrade mb=e.getValue();
      if(mb.getTemplate().getId()==118)
      {
        return true;
      }
    }
    return false;
  }

  public int getId()
  {
    return this.id;
  }

  //v2.8 - fixed mobgroup stars
  public int getSpawnCellId()
  {
    return this.spawnCellId;
  }

  //v2.8 - fixed mobgroup stars
  public void setSpawnCellId(int spawnCellId)
  {
    this.spawnCellId=spawnCellId;
  }

  public int getCellId()
  {
    return this.cellId;
  }

  public void setCellId(int cellId)
  {
    this.cellId=cellId;
  }

  public int getOrientation()
  {
    return this.orientation;
  }

  public void setOrientation(int orientation)
  {
    this.orientation=orientation;
  }

  public int getAlignement()
  {
    return this.align;
  }

  public int getStarBonus(int currentStars)
  {
    int stars=0;
    stars= currentStars;
    long currTime=System.currentTimeMillis();
    long diffTime=currTime-this.spawnTime;
    if(diffTime<0)
      diffTime=0;
    if(diffTime>Config.getInstance().starHour*36000000) //10 hour old group
      stars+=+200;
    else if(diffTime>Config.getInstance().starHour*32400000) //9 hour old group
      stars+=180;
    else if(diffTime>Config.getInstance().starHour*28800000) //8 hour old group
      stars+=160;
    else if(diffTime>Config.getInstance().starHour*25200000) //7 hour old group
      stars+=140;
    else if(diffTime>Config.getInstance().starHour*21600000) //6 hour old group
      stars+=120;
    else if(diffTime>Config.getInstance().starHour*18000000) //5 hour old group
      stars+=100;
    else if(diffTime>Config.getInstance().starHour*14400000) //4 hour old group
      stars+=80;
    else if(diffTime>Config.getInstance().starHour*10800000) //3 hour old group
      stars+=60;
    else if(diffTime>Config.getInstance().starHour*7200000) //2 hour old group
      stars+=40;
    else if(diffTime>Config.getInstance().starHour*3600000) //1 hour old group
      stars+=20;
    if(stars>200)
      stars=200;
    return stars;
  }

  public int getAggroDistance()
  {
    return this.aggroDistance;
  }

  public boolean isFix()
  {
    return this.isFix;
  }

  public void setIsFix(boolean isFix)
  {
    this.isFix=isFix;
  }

  public boolean getIsExtraGroup()
  {
    return this.isExtraGroup;
  }


  public String getIp() {
	return ip;
}

public void setIp(String ip) {
	this.ip = ip;
}

  public Map<Integer, MobGrade> getMobs()
  {
    return this.mobs;
  }

  public MobGrade getMobGradeById(int id)
  {
    return this.mobs.get(id);
  }

  public String getCondition()
  {
    return this.condition;
  }

  public void setCondition(String condition)
  {
    this.condition=condition;
  }
  public void set_ID_PLAYER(int idd)
  {
   this.ID_PLAYER = idd;
  }
  public int get_id_player()
  {
    return this.ID_PLAYER;
  }

  //public void startCondTimer()
  //{
	//  TimerWaiterPlus.addNext(() -> {
	//	  this.set_condi_zero();
	//  },60000*10);

  //}
  public void set_condi_zero()
  {
	  condition="";  
  }

  public ArrayList<GameObject> getObjects()
  {
    if(this.objects==null&&Config.getInstance().HEROIC)
      this.objects=new ArrayList<>();
    else if(!Config.getInstance().HEROIC)
      return new ArrayList<>();
    return objects;
  }

  public String parseGM(Player perso)
  {
    StringBuilder mobIDs=new StringBuilder();
    StringBuilder mobGFX=new StringBuilder();
    StringBuilder mobLevels=new StringBuilder();
    StringBuilder colors=new StringBuilder();
    StringBuilder toreturn=new StringBuilder();
    boolean archi = false;
    boolean mort = false;
    boolean isFirst=true;
    if(this.mobs.isEmpty())
      return "";
    long xp = 0;
    boolean krala = false;
    if(this.objects != null)
    	mort = true;
    for(Entry<Integer, MobGrade> entry : this.mobs.entrySet())
    	xp = xp+entry.getValue().getBaseXp();
    for(Entry<Integer, MobGrade> entry : this.mobs.entrySet())
    {
    if(Main.world.archi.contains("."+entry.getValue().getTemplate().getId()+"."))
    	archi = true;
      if(!isFirst)
      {
        mobIDs.append(",");
        mobGFX.append(",");
        mobLevels.append(",");
      }
      if(entry.getValue().getTemplate().getId() == 423)
    	  krala = true;
      mobIDs.append(entry.getValue().getTemplate().getId());
      mobGFX.append(entry.getValue().getTemplate().getGfxId()).append("^").append(entry.getValue().getSize());
      mobLevels.append(entry.getValue().getLevel());
      colors.append(entry.getValue().getTemplate().getColors()).append(","+ (long) (xp * ((getStarBonus(this.getInternalStarBonus()) / 100f) + Config.getInstance().rateXp))+";0,0,0,0;");
      isFirst=false;
    }
    toreturn.append("+").append(this.cellId).append(";").append(krala ? 1 : this.orientation).append(";");
    toreturn.append(getStarBonus(this.getInternalStarBonus()));
    toreturn.append(";").append(this.id).append(";").append(mobIDs).append(";-3;").append(mobGFX).append(";").append(mobLevels).append(";").append(colors);
    if(archi)
    SocketManager.GAME_SEND_MESSAGE(perso,"Archimonstre présent sur la map", "E83F51");
    if(mort)
        SocketManager.GAME_SEND_MESSAGE(perso,"Quelqu'un est mort ici", "E83F51");
    return toreturn.toString();
  }

  public int getInternalStarBonus()
  {
    return starBonus;
  }

  public void setInternalStarBonus(int starBonus)
  {
    this.starBonus=starBonus;
  }

  public void setIsExtraGroup(boolean isExtraGroup)
  {
    this.isExtraGroup=isExtraGroup;
  }

  public boolean getIsDynamic()
  {
    return isDynamic;
  }

  public void setIsDynamic(boolean isDynamic)
  {
    this.isDynamic=isDynamic;
  }

  public void moveMobGroup(final GameMap map)
  {
    if(this.fight!=null)
      return;
    final short destionationCell=PathFinding.cellMoveSprite(map,this.cellId);
    if(destionationCell==-1)
      return;
    final Pair<Integer, ArrayList<GameCase>> pathCeldas=PathFinding.getPath(map,(short)this.cellId,destionationCell,-1);
    if(pathCeldas==null)
      return;
    final ArrayList<GameCase> celdas=pathCeldas.getRight();
    String pathStr=PathFinding.getPathToString(map,celdas,(short)this.cellId,false);
    if(pathStr.isEmpty())
    {
      return;
    }
    try
    {
      Thread.sleep(100);
    }
    catch(final Exception e)
    {
    }
    SocketManager.ENVIAR_GA_MOVER_SPRITE_MAPA(map,0,1,this.id+"",Main.world.getCryptManager().getValorHashPorNumero(this.orientation)+Main.world.getCryptManager().cellID_To_Code(this.cellId)+pathStr);
    this.orientation=PathFinding.getIndexDireccion(pathStr.charAt(pathStr.length()-3));
    this.cellId=destionationCell;
    
  }

  public Fight getFight()
  {
    return fight;
  }

  public void setFight(Fight fight)
  {
    this.fight=fight;
  }
  public long getSpawnTime() {
	return spawnTime;
}

public void setSpawnTime(long spawnTime) {
	long calcul=System.currentTimeMillis()-Config.getInstance().startTime;
    if(calcul<120000)
    {
    	this.spawnTime = 10000;
      return;
    }
	this.spawnTime = spawnTime;
}
}