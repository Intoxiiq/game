package soufix.common;

import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.spells.Spell;
import soufix.fight.traps.Glyph;
import soufix.fight.traps.Trap;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.utility.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class PathFinding
{
  public static char[] DIRECTIONS= { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };
  private static Short _nroMovimientos=0;

@SuppressWarnings("deprecation")
private static Integer nSteps=new Integer(0);
  private static class CeldaCamino
  {
    private short num, f, h, v, g, d, m, l;
    private CeldaCamino parent;

    private CeldaCamino()
    {
    }
  }

  //v2.7 - String += replaced with StringBuilder
  public static int isValidPath(GameMap map, int cellID, AtomicReference<String> pathRef, Fight fight, Player perso, int targetCell)
  {
      nSteps=0;
      int newPos=cellID;
      int Steps=0;
      String path=pathRef.get();
      StringBuilder newPath=new StringBuilder("");
      for(int i=0;i<path.length();i+=3)
      {
        String SmallPath=path.substring(i,i+3);
        char dir=SmallPath.charAt(0);
        int dirCaseID=Main.world.getCryptManager().cellCode_To_ID(SmallPath.substring(1));
        nSteps=0;
        //Si en combat et Si Pas début du path, on vérifie tacle
        if(fight!=null&&i!=0&&getEnemyFighterArround(newPos,map,fight)!=null)
        {
          pathRef.set(newPath.toString());
          return Steps;
        }
        //Si en combat, et pas au début du path
        if(fight!=null&&i!=0)
        {
          for(Trap p : fight.getAllTraps())
          {
            int dist=getDistanceBetween(map,p.getCell().getId(),newPos);
            if(dist<=p.getSize())
            {
              //on arrete le déplacement sur la 1ere case du piege
              pathRef.set(newPath.toString());
              return Steps;
            }
          }
        }

        String[] aPathInfos=ValidSinglePath(newPos,SmallPath,map,fight,perso,targetCell).split(":");
        if(aPathInfos[0].equalsIgnoreCase("stop"))
        {
          newPos=Integer.parseInt(aPathInfos[1]);
          Steps+=nSteps;
          newPath.append(dir+Main.world.getCryptManager().cellID_To_Code(newPos));
          pathRef.set(newPath.toString());
          return -Steps;
        }
        else if(aPathInfos[0].equalsIgnoreCase("ok"))
        {
          newPos=dirCaseID;
          Steps+=nSteps;
        }
        else if(aPathInfos[0].equalsIgnoreCase("stoptp"))
        {
          newPos=Integer.parseInt(aPathInfos[1]);
          Steps+=nSteps;
          newPath.append(dir+Main.world.getCryptManager().cellID_To_Code(newPos));
          pathRef.set(newPath.toString());
          return -Steps-10000;
        }
        else
        {
          pathRef.set(newPath.toString());
          return -1000;
        }
        newPath.append(dir+Main.world.getCryptManager().cellID_To_Code(newPos));
      }
      pathRef.set(newPath.toString());
      return Steps;
    
  }

  public static boolean getcasebetwenenemie(int cellId, GameMap map, Fight fight, Fighter F)
  {
    char[] dirs= { 'b', 'd', 'f', 'h' };
    for(char dir : dirs)
    {
      GameCase cell=map.getCase(GetCaseIDFromDirrection(cellId,dir,map,false));
      if(cell==null)
        continue;
      Fighter f=cell.getFirstFighter();

      if(f!=null&&f.getTeam2()!=F.getTeam2())
        return true;
    }
    return false;
  }

  public static boolean isCACwithEnnemy(Fighter fighter, ArrayList<Fighter> Ennemys)
  {
    for(Fighter f : Ennemys)
      if(isNextTo(fighter.getFight().getMap(),fighter.getCell().getId(),f.getCell().getId()))
        return true;
    return false;
  }

  //v2.2 - invisible enemy interrupt fix
  public static ArrayList<Fighter> getEnemyFighterArround(int cellID, GameMap map, Fight fight)
  {
    char[] dirs= { 'b', 'd', 'f', 'h' };
    ArrayList<Fighter> enemy=new ArrayList<Fighter>();

    for(char dir : dirs)
    {
      GameCase cell=map.getCase((short)GetCaseIDFromDirrection(cellID,dir,map,false));
      if(cell!=null)
      {
        Fighter f=cell.getFirstFighter();
        if(f!=null)
        {
          if(f.getFight()!=fight)
            continue;
          if(f.getTeam()!=fight.getFighterByOrdreJeu().getTeam()&&!f.isHide())
            enemy.add(f);
        }
      }
    }
    if(enemy.size()==0||enemy.size()==4)
      return null;

    return enemy;
  }

  public static boolean isNextTo(GameMap map, int cell1, int cell2)
  {
    boolean result=false;
    if(cell1+14==cell2)
      result=true;
    else if(cell1+15==cell2)
      result=true;
    else
      result=cell1-14==cell2||cell1-15==cell2;
    return result;
  }

  public static String ValidSinglePath(int CurrentPos, String Path, GameMap map, Fight fight, Player perso, int targetCell)
  {
    nSteps=0;
    char dir=Path.charAt(0);
    int dirCaseID=Main.world.getCryptManager().cellCode_To_ID(Path.substring(1)),
        check=("353;339;325;311;297;283;269;255;241;227;213;228;368;354;340;326;312;298;284;270;256;242;243;257;271;285;299;313;327;341;355;369;383".contains(String.valueOf(targetCell)) ? 1 : 0);

    if(fight!=null&&fight.isOccuped(dirCaseID))
      return "no:";

    if(perso!=null)
    {
      if(perso.getCases)
        if(!perso.thisCases.contains(CurrentPos))
          perso.thisCases.add(CurrentPos);
    }
    // int oldPos = CurrentPos;
    int lastPos=CurrentPos,oldPos=CurrentPos;

    for(nSteps=1;nSteps<=64;nSteps++)
    {
      if(GetCaseIDFromDirrection(lastPos,dir,map,fight!=null)==dirCaseID)
      {
        if(fight!=null&&fight.isOccuped(dirCaseID))
          return "stop:"+lastPos;
        GameCase cell=map.getCase(dirCaseID);
        if(map.getId()==2019)
        {
          if(cell.getId()==297&&((cell.getPlayers()!=null&&cell.getPlayers().size()>0)||perso.getSexe()==0))
            return "stop:"+oldPos;
          if(cell.getId()==282&&((cell.getPlayers()!=null&&cell.getPlayers().size()>0)||perso.getSexe()==1))
            return "stop:"+oldPos;
        }
        if(cell.isWalkable(true,fight!=null,targetCell))
        {
          return "ok:";
        }
        else
        {
          nSteps--;
          return ("stop:"+lastPos);
        }
      }
      else
      {
        lastPos=GetCaseIDFromDirrection(lastPos,dir,map,fight!=null);
      }

      if(fight==null)
      {
        if(perso.getCurMap().getId()==9588)
        {
          String cell="353;339;325;311;297;283;269;255;241;227;213;228;368;354;340;326;312;298;284;270;256;242;243;257;271;285;299;313;327;341;355;369;383";
          if(cell.contains(String.valueOf(lastPos)))
            check++;
          if(check>1)
            return "stoptp:"+lastPos;
        }
        try
        {
          if(perso.getCases)
            if(!perso.thisCases.contains(lastPos))
              perso.thisCases.add(lastPos);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }

        if(lastPos<0)
          continue;
        GameCase _case=map.getCase(lastPos);
        if(_case==null)
          continue;
        if(map.getId()==2019)
        {
          if(_case.getId()==297&&((_case.getPlayers()!=null&&_case.getPlayers().size()>0)||perso.getSexe()==0))
            return "stop:"+oldPos;
          if(_case.getId()==282&&((_case.getPlayers()!=null&&_case.getPlayers().size()>0)||perso.getSexe()==1))
            return "stop:"+oldPos;
        }
        if(_case.getOnCellStopAction())
          return "stop:"+lastPos;
        if(map.isAggroByMob(perso,lastPos))
          return "stop:"+lastPos;
        if(!map.getCase(lastPos).isWalkable(true,false,targetCell))
          return "stop:"+oldPos;
        oldPos=lastPos;
      }
      else
      {
        if(fight.isOccuped(lastPos))
          return "no:";
        if(getEnemyFighterArround(lastPos,map,fight)!=null)//Si ennemie proche
          return "stop:"+lastPos;
        for(Trap p : fight.getAllTraps())
        {
          if(getDistanceBetween(map,p.getCell().getId(),lastPos)<=p.getSize())
          {//on arrete le déplacement sur la 1ere case du piege
            return "stop:"+lastPos;
          }
        }
      }
    }
    return "no:";
  }

  public static ArrayList<Integer> getListCaseFromFighter(Fight fight, Fighter fighter, int cellStart, ArrayList<Spell.SortStats> SS)
  {
    int bestPo=0;
    if(SS!=null)
    {
      for(Spell.SortStats sort : SS)
      {
        if(sort.getMaxPO()>bestPo)
          bestPo=sort.getMaxPO();
      }
    }
    int pmNumber=fighter.getCurPm(fight);
    /*
     * if(fighter != fight.getCurFighter()) pmNumber = fighter.getPm();
     */
    int cellNumber=Formulas.countCell(pmNumber+1);
    int _loc1_=0;
    int _loc3_=0;
    char[] dirs= { 'b', 'd', 'f', 'h' };
    ArrayList<Integer> cellT=new ArrayList<Integer>();
    ArrayList<Integer> cellY=new ArrayList<Integer>();
    cellT.add(cellStart);
    if(fighter.getCurPm(fight)<=0)
      return cellT;
    ArrayList<Integer> cell=new ArrayList<Integer>();
    //int distanceMin = bestPo + 4;
    while(_loc1_++<cellNumber)
    {
      int _loc2_=0;
      if(cellT.size()<=_loc3_||cellT.isEmpty())
      {
        //Fini de tout bouclé
        cell.addAll(cellT);
        cellT.clear();
        cellT.addAll(cellY);
        cellY.clear();
        _loc3_=0;
      }

      if(cellT.isEmpty()&&cellY.isEmpty())
        return cell;

      _loc2_=cellT.get(_loc3_);
      for(char dir : dirs)
      {
        int _loc4_=PathFinding.getCaseIDFromDirrection(_loc2_,dir,fight.getMapOld());
        if(fight.getMap()==null)
          continue;
        if(_loc4_<0||fight.getMap().getCase(_loc4_)==null||cell.contains(_loc4_)||cellT.contains(_loc4_)||cellY.contains(_loc4_))
          continue;
        if(haveFighterOnThisCell(_loc4_,fight)||!fight.getMapOld().getCase(_loc4_).isWalkable(true,true,-1))
          continue;
        cellY.add(_loc4_);
      }
      _loc3_++;
    }
    return cell;
  }

  public static ArrayList<Integer> getListCaseFromFighter(Fight fight, Fighter fighter, ArrayList<Spell.SortStats> SS, Fighter nearest)
  {
    int bestPo=0;
    for(Spell.SortStats sort : SS)
    {
      if(sort.getMaxPO()>bestPo)
        bestPo=sort.getMaxPO();
    }
    int cellNumber=Formulas.countCell(fighter.getCurPm(fight)+1);
    int _loc1_=0;
    int _loc3_=0;
    char[] dirs= { 'b', 'd', 'f', 'h' };
    ArrayList<Integer> cellT=new ArrayList<>();
    ArrayList<Integer> cellY=new ArrayList<>();
    cellT.add(fighter.getCell().getId());
    ArrayList<Integer> cell=new ArrayList<>();
    while(_loc1_++<cellNumber)
    {
      int _loc2_=0;
      if(cellT.size()<=_loc3_||cellT.isEmpty())
      {
        //Fini de tout bouclé
        cell.addAll(cellT);
        cellT.clear();
        cellT.addAll(cellY);
        cellY.clear();
        _loc3_=0;
      }
      if(cellT.isEmpty()&&cellY.isEmpty())
        return cell;
      _loc2_=cellT.get(_loc3_);
      for(char dir : dirs)
      {
        int _loc4_=(short)PathFinding.getCaseIDFromDirrection(_loc2_,dir,fight.getMapOld());
        if(_loc4_<0||fight.getMap().getCase(_loc4_)==null||cell.contains(_loc4_)||cellT.contains(_loc4_)||cellY.contains(_loc4_))
        {
          continue;
        }
        if(haveFighterOnThisCell(_loc4_,fight)||!fight.getMapOld().getCase(_loc4_).isWalkable(true,true,-1))
          continue;

        cellY.add(_loc4_);
      }
      _loc3_++;
    }
    return cell;
  }

  public static ArrayList<Integer> getAllCaseIdAllDirrection(int caseId, GameMap map)
  {
    ArrayList<Integer> list=new ArrayList<Integer>();
    char[] dir= { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };
    int _c=-1;
    for(char d : dir)
    {
      _c=GetCaseIDFromDirrection(caseId,d,map,false);
      if(_c>0)
        list.add(_c);
    }
    return list;
  }

  public static int GetCaseIDFromDirrection(int CaseID, char Direction, GameMap map, boolean Combat)
  {
    if(map==null)
      return -1;
    switch(Direction)
    {
      case 'a':
        return Combat ? -1 : CaseID+1;
      case 'b': //right down
        return CaseID+map.getW();
      case 'c':
        return Combat ? -1 : CaseID+(map.getW()*2-1);
      case 'd': //left down
        return CaseID+map.getW()-1;
      case 'e':
        return Combat ? -1 : CaseID-1;
      case 'f': //left up
        return CaseID-map.getW();
      case 'g':
        return Combat ? -1 : CaseID-(map.getW()*2-1);
      case 'h': //right up
        return CaseID-map.getW()+1;
    }
    return -1;
  }

  public static int getDistanceBetween(GameMap map, int id1, int id2)
  {
    if(id1==id2)
      return 0;
    if(map==null)
      return 0;

    int diffX=Math.abs(getCellXCoord(map,id1)-getCellXCoord(map,id2));
    int diffY=Math.abs(getCellYCoord(map,id1)-getCellYCoord(map,id2));
    return (diffX+diffY);
  }

  public static Fighter getEnemyAround(int cellId, GameMap map, Fight fight)
  {
    char[] dirs= { 'b', 'd', 'f', 'h' };
    for(char dir : dirs)
    {
      GameCase cell=map.getCase(GetCaseIDFromDirrection(cellId,dir,map,false));
      if(cell==null)
        continue;
      Fighter f=cell.getFirstFighter();

      if(f!=null)
        if(f.getFight()==fight)
          if(f.getTeam()!=fight.getFighterByOrdreJeu().getTeam())
            if(!f.isHide())
              return f;
    }
    return null;
  }

  public static int newCaseAfterPush(Fight fight, GameCase CCase, GameCase TCase, int value)
  {
    GameMap map=fight.getMap();
    if(CCase.getId()==TCase.getId())
      return 0;

    char c=getDirBetweenTwoCase(CCase.getId(),TCase.getId(),map,true);
    int id=TCase.getId();

    if(value<0)
    {
      c=getOpositeDirection(c);
      value=-value;
    }
    boolean b=false;
    for(int a=0;a<value;a++)
    {
      int nextCase=GetCaseIDFromDirrection(id,c,map,true);
      for(Trap p : fight.getAllTraps())
      {
        if(distBetweenTwoCase(map,p.getCell(),map.getCase(nextCase))<=p.getSize())
        {
          id=nextCase;
          b=true;
        }
      }
      if(b)
        break;
      if(map.getCase(nextCase)!=null&&map.getCase(nextCase).isWalkable(true)&&map.getCase(nextCase).getFirstFighter()==null)
        id=nextCase;
      else
        return -(value-a);
    }

    if(id==TCase.getId())
      id=0;
    return id;
  }

  //v2.3 - center-cell AoE knockback fix
  public static int newCaseAfterPush(Fight fight, GameCase currentCell, GameCase targetCell, int value, boolean piege)
  {
    GameMap map=fight.getMap();

    if(currentCell.getId()==targetCell.getId())
      return 0;
    char dir=getDirBetweenTwoCase(currentCell.getId(),targetCell.getId(),map,true);
    int id=targetCell.getId();

    if(value<0)
    {
      dir=getOpositeDirection(dir);
      value=-value;
    }

    if(dir==0x00)
      return 0;
    boolean b=false;
    for(int a=0;a<value;a++)
    {
      int nextCase=GetCaseIDFromDirrection(id,dir,map,true);

      for(Trap trap : fight.getAllTraps())
      {
        if(distBetweenTwoCase(map,trap.getCell(),map.getCase(nextCase))<=trap.getSize())
        {
          id=nextCase;
          b=true;
        }
      }

      if(b)
        break;

      if(map.getCase(nextCase)!=null&&map.getCase(nextCase).isWalkable(false)&&map.getCase(nextCase).getFighters().isEmpty())
        id=nextCase;
      else
        return -(value-a);
    }

    if(id==targetCell.getId())
      return 0;
    return id;
  }

  public static int distBetweenTwoCase(GameMap map, GameCase c1, GameCase c2)
  {
    int dist=0;
    if(c1==null||c2==null)
    {
      return dist;
    }
    if(c1.getId()==c2.getId())
      return dist;
    int id=c1.getId();
    char c=getDirBetweenTwoCase(c1.getId(),c2.getId(),map,true);

    while(c2!=map.getCase(id))
    {
      id=GetCaseIDFromDirrection(id,c,map,true);
      if(map.getCase(id)==null)
      {
        return dist;
      }
      dist++;
    }
    return dist;
  }

  public static char getOpositeDirection(char c)
  {
    switch(c)
    {
      case 'a':
        return 'e';
      case 'b':
        return 'f';
      case 'c':
        return 'g';
      case 'd':
        return 'h';
      case 'e':
        return 'a';
      case 'f':
        return 'b';
      case 'g':
        return 'c';
      case 'h':
        return 'd';
    }
    return 0x00;
  }

  public static int getNearenemycontremur(GameMap map, int startCell, int endCell, ArrayList<GameCase> forbidens)
  {
    //On prend la cellule autour de la cible, la plus proche
    int dist=1000;
    int cellID=startCell;
    if(forbidens==null)
      forbidens=new ArrayList<GameCase>();
    char[] dirs= { 'b', 'd', 'f', 'h' };
    GameCase hd=null,bg=null,hg=null,bd=null;
    for(char d : dirs)
    {
      if(d=='b')//En Haut à Droite.
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        hd=map.getCase(c);
      }
      else if(d=='f')//En Bas à Gauche.
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        bg=map.getCase(c);
      }
      else if(d=='d')//En Haut à Gauche.
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        hg=map.getCase(c);
      }
      else if(d=='h')//En Bas à Droite.
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        bd=map.getCase(c);
      }
    }

    GameCase[] tab= { hd, bg, hg, bd };
    for(GameCase c : tab)
    {
      if(c==null)
        continue;
      if(c==hd)
      {
        if(!c.isWalkable(false)&&bg!=null||c.getFirstFighter()!=null&&bg!=null)
        {
          // On cherche la distance entre
          int dis=PathFinding.getDistanceBetween(map,endCell,bg.getId());
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&!forbidens.contains(bg))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=bg.getId();
          }
        }
      }
      else if(c==bg)
      {
        if(!c.isWalkable(false)&&hd!=null||c.getFirstFighter()!=null&&hd!=null)
        {
          // On cherche la distance entre
          int dis=PathFinding.getDistanceBetween(map,endCell,hd.getId());
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&!forbidens.contains(hd))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=hd.getId();
          }
        }
      }
      else if(c==bd)
      {
        if(!c.isWalkable(false)&&hg!=null||c.getFirstFighter()!=null&&hg!=null)
        {
          // On cherche la distance entre
          int dis=PathFinding.getDistanceBetween(map,endCell,hg.getId());
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&!forbidens.contains(hg))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=hg.getId();
          }
        }
      }
      else if(c==hg)
      {
        if(!c.isWalkable(false)&&bd!=null||c.getFirstFighter()!=null&&bd!=null)
        {
          // On cherche la distance entre
          int dis=PathFinding.getDistanceBetween(map,endCell,bd.getId());
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&!forbidens.contains(bd))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=bd.getId();
          }
        }
      }
    }

    //On renvoie -1 si pas trouvé
    return cellID==startCell ? -1 : cellID;
  }

  public static int getcasebetwenEnemy(int cellId, GameMap map, Fight fight)
  {
    if(map==null)
      return 0;
    char[] dirs= { 'f', 'd', 'b', 'h' };
    for(char dir : dirs)
    {
      int id=GetCaseIDFromDirrection(cellId,dir,map,false);
      GameCase cell=map.getCase(id);
      if(cell==null)
        continue;
      Fighter f=cell.getFirstFighter();

      if(f==null&&cell.isWalkable(false))
        return cell.getId();
    }
    return 0;
  }

  public static int getNearestligneGA(Fight fight, int startCell, int endCell, ArrayList<GameCase> forbidens, int distmin)
  {
    GameMap map=fight.getMap();
    ArrayList<Glyph> glyphs=new ArrayList<Glyph>();//Copie du tableau
    glyphs.addAll(fight.getAllGlyphs());
    int dist=1000;
    //On prend la cellule autour de la cible, la plus proche
    int cellID=startCell;
    if(forbidens==null)
      forbidens=new ArrayList<>();
    char[] dirs= { 'b', 'd', 'f', 'h' };
    
    int check = 0;
    for(char d : dirs)
    {
    	check++;
    	if(check > 60)
    		break;
      int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
      if(map.getCase(c)==null)
        continue;
      int dis=PathFinding.getDistanceBetween(map,endCell,c);
      int dis2=PathFinding.getDistanceBetween(map,startCell,c);
      if(dis<dist&&dis2<=distmin&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
      {
        boolean ok1=true;
        int check2 = 0;
        for(Glyph g : glyphs)
        {
        	check2++;
        	if(check2 > 60)
        		break;
          if(PathFinding.getDistanceBetween(map,c,g.getCell().getId())<=g.getSize()&&g.getSpell()!=476)
            ok1=false;
        }

        if(!ok1)
          continue;
        // On crée la distance
        dist=dis;
        // On modifie la cellule
        cellID=c;
      }
      else if(dis<dist&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
      {
        boolean ok1=true;
        int check3 = 0;
        for(Glyph g : glyphs)
        {
        	check3++;
        	if(check3 > 60)
        		break;
          if(PathFinding.getDistanceBetween(map,c,g.getCell().getId())<=g.getSize()&&g.getSpell()!=476)
            ok1=false;
        }

        if(!ok1)
          continue;
        // On crée la distance
        dist=dis;
        // On modifie la cellule
        cellID=c;
      }
      boolean ok=false;
      int check4 = 0;
      while(!ok)
      {
    	  check4++;
      	if(check4 > 60) {
      		ok=true;
      		break;
      	}
        int h=PathFinding.GetCaseIDFromDirrection(c,d,map,true);
        if(map.getCase(h)==null)
          ok=true;
        dis=PathFinding.getDistanceBetween(map,endCell,c);
        dis2=PathFinding.getDistanceBetween(map,startCell,c);
        if(dis<dist&&dis2<=distmin&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
        {
          boolean ok1=true;
          int check5 = 0;
          for(Glyph g : glyphs)
          {
        	  check5++;
            	if(check5 > 60)
            		break;
            if(PathFinding.getDistanceBetween(map,c,g.getCell().getId())<=g.getSize()&&g.getSpell()!=476)
              ok1=false;
          }

          if(!ok1)
            continue;
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=c;
        }
        else if(dis<dist&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
        {
          boolean ok1=true;
          int check6 = 0;
          for(Glyph g : glyphs)
          {
        	  check6++;
          	if(check6 > 60)
          		break;
            if(PathFinding.getDistanceBetween(map,c,g.getCell().getId())<=g.getSize()&&g.getSpell()!=476)
              ok1=false;
          }

          if(!ok1)
            continue;
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=c;
        }
        c=h;
      }
    }

    return cellID==startCell ? -1 : cellID;
  }

  public static Fighter getNearestligneenemy(GameMap map, int startCell, Fighter f, int dist)
  {
    //On prend la cellule autour de la cible, la plus proche
    Fighter E=null;
    char[] dirs= { 'b', 'd', 'f', 'h' };
    int endCell=f.getCell().getId();
    for(char d : dirs)
    {
      int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
      if(map.getCase(c)==null)
        continue;
      int dis=PathFinding.getDistanceBetween(map,endCell,c);
      // Si la distance est strictement inférieur à 1000 et que la case
      // est marchable et que personne ne
      // se trouve dessus et que la case n'est pas interdite
      if(dis<dist&&map.getCase(c).getFirstFighter()!=null)
      {
        if(map.getCase(c).getFirstFighter().getTeam2()!=f.getTeam2())
          E=map.getCase(c).getFirstFighter();
      }
      boolean ok=false;
      while(!ok)
      {
        int h=PathFinding.GetCaseIDFromDirrection(c,d,map,true);
        if(map.getCase(h)!=null)
        {
          dis=PathFinding.getDistanceBetween(map,endCell,h);
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&map.getCase(c).getFirstFighter()!=null)
          {
            if(map.getCase(c).getFirstFighter().getTeam2()!=f.getTeam2())
              E=map.getCase(c).getFirstFighter();
          }
        }
        else
          ok=true;
        c=h;
      }

    }
    //On renvoie null si pas trouvé
    return E;
  }

  public static int getNearenemycontremur2(GameMap map, int startCell, int endCell, ArrayList<GameCase> forbidens, Fighter F)
  {
    //On prend la cellule autour de la cible, la plus proche
    int dist=1000;
    int cellID=startCell;
    if(forbidens==null)
      forbidens=new ArrayList<>();
    char[] dirs= { 'b', 'd', 'f', 'h' };
    char perso=' ';
    for(char d : dirs)
    {
      int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
      if(map.getCase(c)==null)
        continue;
      if(map.getCase(c)==F.getCell())
        perso=d;
    }

    for(char d : dirs)
    {
      if(getOpositeDirection(perso)==d)
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        if(!map.getCase(c).isWalkable(false)||map.getCase(c).getFirstFighter()!=null)
        {
          int dis=PathFinding.getDistanceBetween(map,endCell,map.getCase(c).getId());
          if(dis<dist&&!forbidens.contains(map.getCase(c))&&F.getCell()!=map.getCase(c))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=map.getCase(c).getId();
          }
        }
      }
    }

    //On renvoie -1 si pas trouvé
    return cellID==startCell ? -1 : cellID;
  }

  public static int getRandomcelllignepomax(GameMap map, int startCell, int endCell, ArrayList<GameCase> forbidens, int distmin)
  {
    int dist=1000;
    //On prend la cellule autour de la cible, la plus proche
    int cellID=startCell;
    if(forbidens==null)
      forbidens=new ArrayList<>();
    char[] dirs= { 'b', 'd', 'f', 'h' };
    for(char d : dirs)
    {
      if(d=='b')//En Haut à Droite.
      {

        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        int dis=PathFinding.getDistanceBetween(map,endCell,c);
        int dis2=PathFinding.getDistanceBetween(map,startCell,c);
        // Si la distance est strictement inférieur à 1000 et que la case
        // est marchable et que personne ne
        // se trouve dessus et que la case n'est pas interdite
        if(dis<dist&&dis2<=distmin&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=c;
        }
        else if(dis<dist&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=c;
        }
        boolean ok=false;
        while(ok==false)
        {
          int h=PathFinding.GetCaseIDFromDirrection(c,d,map,true);
          if(map.getCase(h)==null)
            ok=true;
          dis=PathFinding.getDistanceBetween(map,endCell,c);
          dis2=PathFinding.getDistanceBetween(map,startCell,c);
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&dis2<=distmin&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=c;
          }
          else if(dis<dist&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=c;
          }
          c=h;
        }

      }
      else if(d=='f')//En Bas à Gauche.
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        int dis=PathFinding.getDistanceBetween(map,endCell,c);
        int dis2=PathFinding.getDistanceBetween(map,startCell,c);
        // Si la distance est strictement inférieur à 1000 et que la case
        // est marchable et que personne ne
        // se trouve dessus et que la case n'est pas interdite
        if(dis<dist&&dis2<=distmin&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=c;
        }
        else if(dis<dist&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=c;
        }
        boolean ok=false;
        while(ok==false)
        {
          int h=PathFinding.GetCaseIDFromDirrection(c,d,map,true);
          if(map.getCase(h)==null)
            ok=true;
          dis=PathFinding.getDistanceBetween(map,endCell,c);
          dis2=PathFinding.getDistanceBetween(map,startCell,c);
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&dis2<=distmin&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=c;
          }
          else if(dis<dist&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=c;
          }
          c=h;
        }
      }
      else if(d=='d')//En Haut à Gauche.
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        int dis=PathFinding.getDistanceBetween(map,endCell,c);
        int dis2=PathFinding.getDistanceBetween(map,startCell,c);
        // Si la distance est strictement inférieur à 1000 et que la case
        // est marchable et que personne ne
        // se trouve dessus et que la case n'est pas interdite
        if(dis<dist&&dis2<=distmin&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=c;
        }
        else if(dis<dist&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=c;
        }
        boolean ok=false;
        while(!ok)
        {
          int h=PathFinding.GetCaseIDFromDirrection(c,d,map,true);
          if(map.getCase(h)==null)
            ok=true;
          dis=PathFinding.getDistanceBetween(map,endCell,c);
          dis2=PathFinding.getDistanceBetween(map,startCell,c);
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&dis2<=distmin&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=c;
          }
          else if(dis<dist&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=c;
          }
          c=h;
        }
      }
      else if(d=='h')//En Bas à Droite.
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        int dis=PathFinding.getDistanceBetween(map,endCell,c);
        int dis2=PathFinding.getDistanceBetween(map,startCell,c);
        // Si la distance est strictement inférieur à 1000 et que la case
        // est marchable et que personne ne
        // se trouve dessus et que la case n'est pas interdite
        if(dis<dist&&dis2<=distmin&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=c;
        }
        else if(dis<dist&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=c;
        }
        boolean ok=false;
        while(!ok)
        {
          int h=PathFinding.GetCaseIDFromDirrection(c,d,map,true);
          if(map.getCase(h)==null)
            ok=true;
          dis=PathFinding.getDistanceBetween(map,endCell,c);
          dis2=PathFinding.getDistanceBetween(map,startCell,c);
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&dis2<=distmin&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=c;
          }
          else if(dis<dist&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=c;
          }
          c=h;
        }
      }
    }

    return cellID==startCell ? -1 : cellID;
  }

  public static int getNearestCellDiagGA(GameMap map, int startCell, int endCell, ArrayList<GameCase> forbidens)
  {
    //On prend la cellule autour de la cible, la plus proche
    int dist=1000;
    int cellID=startCell;
    if(forbidens==null)
      forbidens=new ArrayList<GameCase>();
    char[] dirs= { 'b', 'd', 'f', 'h' };
    GameCase hd=null;
    GameCase bg=null;
    GameCase hg=null;
    GameCase bd=null;
    for(char d : dirs)
    {
      if(d=='b')//En Haut à Droite.
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        hd=map.getCase(c);
      }
      else if(d=='f')//En Bas à Gauche.
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        bg=map.getCase(c);
      }
      else if(d=='d')//En Haut à Gauche.
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        hg=map.getCase(c);
      }
      else if(d=='h')//En Bas à Droite.
      {
        int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
        if(map.getCase(c)==null)
          continue;
        bd=map.getCase(c);
      }
    }

    GameCase[] tab= { hd, bg, hg, bd };
    for(GameCase c : tab)
    {
      if(c==null)
        continue;
      if(c==hd)//En Haut à Droite.
      {
        if(hd.getFirstFighter()==null&&hd.blockLoS()==true)
        {
          int p=PathFinding.GetCaseIDFromDirrection(c.getId(),'b',map,true);
          if(map.getCase(p)==null)
            continue;
          // On cherche la distance entre
          int dis=PathFinding.getDistanceBetween(map,endCell,p);
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&map.getCase(p).isWalkable(true,true,-1)&&map.getCase(p).getFirstFighter()==null&&!forbidens.contains(map.getCase(p)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=p;
          }
        }
        int p=PathFinding.GetCaseIDFromDirrection(c.getId(),'h',map,true);
        if(map.getCase(p)==null)
          continue;
        // On cherche la distance entre
        int dis=PathFinding.getDistanceBetween(map,endCell,p);
        // Si la distance est strictement inférieur à 1000 et que la case
        // est marchable et que personne ne
        // se trouve dessus et que la case n'est pas interdite
        if(dis<dist&&map.getCase(p).isWalkable(true,true,-1)&&map.getCase(p).getFirstFighter()==null&&!forbidens.contains(map.getCase(p)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=p;
        }

        int m=PathFinding.GetCaseIDFromDirrection(c.getId(),'d',map,true);
        if(map.getCase(m)==null)
          continue;
        // On cherche la distance entre
        dis=PathFinding.getDistanceBetween(map,endCell,m);
        // Si la distance est strictement inférieur à 1000 et que la case
        // est marchable et que personne ne
        // se trouve dessus et que la case n'est pas interdite
        if(dis<dist&&map.getCase(m).isWalkable(true,true,-1)&&map.getCase(m).getFirstFighter()==null&&!forbidens.contains(map.getCase(m)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=m;
        }
      }
      else if(c==bg)//En Bas à Gauche.
      {
        if(bg.getFirstFighter()==null&&bg.blockLoS()==true)
        {
          int p=PathFinding.GetCaseIDFromDirrection(c.getId(),'f',map,true);
          if(map.getCase(p)==null)
            continue;
          // On cherche la distance entre
          int dis=PathFinding.getDistanceBetween(map,endCell,p);
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&map.getCase(p).isWalkable(true,true,-1)&&map.getCase(p).getFirstFighter()==null&&!forbidens.contains(map.getCase(p)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=p;
          }
        }
        int p=PathFinding.GetCaseIDFromDirrection(c.getId(),'h',map,true);
        if(map.getCase(p)==null)
          continue;
        // On cherche la distance entre
        int dis=PathFinding.getDistanceBetween(map,endCell,p);
        // Si la distance est strictement inférieur à 1000 et que la case
        // est marchable et que personne ne
        // se trouve dessus et que la case n'est pas interdite
        if(dis<dist&&map.getCase(p).isWalkable(true,true,-1)&&map.getCase(p).getFirstFighter()==null&&!forbidens.contains(map.getCase(p)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=p;
        }

        int m=PathFinding.GetCaseIDFromDirrection(c.getId(),'d',map,true);
        if(map.getCase(m)==null)
          continue;
        // On cherche la distance entre
        dis=PathFinding.getDistanceBetween(map,endCell,m);
        // Si la distance est strictement inférieur à 1000 et que la case
        // est marchable et que personne ne
        // se trouve dessus et que la case n'est pas interdite
        if(dis<dist&&map.getCase(m).isWalkable(true,true,-1)&&map.getCase(m).getFirstFighter()==null&&!forbidens.contains(map.getCase(m)))
        {
          // On crée la distance
          dist=dis;
          // On modifie la cellule
          cellID=m;
        }
      }
      else if(c==hg)//En Haut à Gauche.
      {
        if(hg.getFirstFighter()==null&&hg.blockLoS()==true)
        {
          int p=PathFinding.GetCaseIDFromDirrection(c.getId(),'d',map,true);
          if(map.getCase(p)==null)
            continue;
          // On cherche la distance entre
          int dis=PathFinding.getDistanceBetween(map,endCell,p);
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&map.getCase(p).isWalkable(true,true,-1)&&map.getCase(p).getFirstFighter()==null&&!forbidens.contains(map.getCase(p)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=p;
          }
        }
      }
      else if(c==bd)//En Haut à Gauche.
      {
        if(bd.getFirstFighter()==null&&bd.blockLoS()==true)
        {
          int p=PathFinding.GetCaseIDFromDirrection(c.getId(),'h',map,true);
          if(map.getCase(p)==null)
            continue;
          // On cherche la distance entre
          int dis=PathFinding.getDistanceBetween(map,endCell,p);
          // Si la distance est strictement inférieur à 1000 et que la case
          // est marchable et que personne ne
          // se trouve dessus et que la case n'est pas interdite
          if(dis<dist&&map.getCase(p).isWalkable(true,true,-1)&&map.getCase(p).getFirstFighter()==null&&!forbidens.contains(map.getCase(p)))
          {
            // On crée la distance
            dist=dis;
            // On modifie la cellule
            cellID=p;
          }
        }
      }
    }
    return cellID==startCell ? -1 : cellID;
  }

  public static boolean casesAreInSameLine(GameMap map, int c1, int c2, char dir, int max)
  {
    if(c1==c2)
      return true;

    if(dir!='z')//Si la direction est définie
    {
      for(int a=0;a<max;a++)
      {
        if(GetCaseIDFromDirrection(c1,dir,map,true)==c2)
          return true;
        if(GetCaseIDFromDirrection(c1,dir,map,true)==-1)
          break;
        c1=GetCaseIDFromDirrection(c1,dir,map,true);
      }
    }
    else
    //Si on doit chercher dans toutes les directions
    {
      char[] dirs= { 'b', 'd', 'f', 'h' };
      for(char d : dirs)
      {
        int c=c1;
        for(int a=0;a<max;a++)
        {
          if(GetCaseIDFromDirrection(c,d,map,true)==c2)
            return true;
          c=GetCaseIDFromDirrection(c,d,map,true);
        }
      }
    }
    return false;
  }

  //v2.9 - new axe/shovel target zones
  public static ArrayList<Fighter> getCiblesByZoneByWeapon(Fight fight, int type, GameCase cell, int castCellID)
  {
    ArrayList<Fighter> cibles=new ArrayList<>();
    char c=getDirBetweenTwoCase(castCellID,cell.getId(),fight.getMap(),true);
    if(c==0)
    {
      //On cible quand meme le fighter sur la case
      if(cell.getFirstFighter()!=null)
        cibles.add(cell.getFirstFighter());
      return cibles;
    }

    switch(type)
    {
      //Cases devant celle ou l'on vise
      case Constant.ITEM_TYPE_MARTEAU:
        Fighter f=getFighter2CellBefore(castCellID,c,fight.getMap());
        if(f!=null)
          cibles.add(f);
        Fighter g=get1StFighterOnCellFromDirection(fight.getMap(),castCellID,(char)(c-1));
        if(g!=null)
          cibles.add(g);//Ajoute case a gauche
        Fighter h=get1StFighterOnCellFromDirection(fight.getMap(),castCellID,(char)(c+1));
        if(h!=null)
          cibles.add(h);//Ajoute case a droite
        Fighter i=cell.getFirstFighter();
        if(i!=null)
          cibles.add(i);
        break;
      case Constant.ITEM_TYPE_BATON:
        int dist=PathFinding.getDistanceBetween(fight.getMap(),cell.getId(),castCellID);
        int newCell=PathFinding.getCaseIDFromDirrection(castCellID,c,fight.getMap());

        Fighter j=get1StFighterOnCellFromDirection(fight.getMap(),(dist>1 ? newCell : castCellID),(char)(c-1));
        if(j!=null)
          cibles.add(j);//Ajoute case a gauche
        Fighter k=get1StFighterOnCellFromDirection(fight.getMap(),(dist>1 ? newCell : castCellID),(char)(c+1));
        if(k!=null)
          cibles.add(k);//Ajoute case a droite
        Fighter l=cell.getFirstFighter();
        if(l!=null)
          cibles.add(l);//Ajoute case cible
        break;
      case Constant.ITEM_TYPE_HACHE:
      case Constant.ITEM_TYPE_FAUX:
      case Constant.ITEM_TYPE_PELLE:
      case Constant.ITEM_TYPE_PIOCHE:
      case Constant.ITEM_TYPE_EPEE:
      case Constant.ITEM_TYPE_DAGUES:
      case Constant.ITEM_TYPE_BAGUETTE:
      case Constant.ITEM_TYPE_ARC:
      case Constant.ITEM_TYPE_OUTIL:
      case Constant.ITEM_TYPE_FILET_CAPTURE: //capture net
        Fighter t=cell.getFirstFighter();
        if(t!=null)
          cibles.add(t);
        break;
    }
    return cibles;
  }

  private static Fighter get1StFighterOnCellFromDirection(GameMap map, int id, char c)
  {
    if(c==(char)('a'-1))
      c='h';
    if(c==(char)('h'+1))
      c='a';
    return map.getCase(GetCaseIDFromDirrection(id,c,map,false)).getFirstFighter();
  }

  private static Fighter getFighter2CellBefore(int CellID, char c, GameMap map)
  {
    int new2CellID=GetCaseIDFromDirrection(GetCaseIDFromDirrection(CellID,c,map,false),c,map,false);
    return map.getCase(new2CellID).getFirstFighter();
  }

 /* private static Fighter getFighterScythe(int CellID, char c, GameMap map)
  {
    char newChar=warpScytheChar(c);
    if(newChar!=0)
    {
      int new2CellID=GetCaseIDFromDirrection(GetCaseIDFromDirrection(CellID,newChar,map,false),newChar,map,false);
      return map.getCase(new2CellID).getFirstFighter();
    }
    return null;
  }

  private static char warpScytheChar(char c)
  {
    switch(c)
    {
      case 'b': //right down
        return 'd';
      case 'd': //left down
        return 'f';
      case 'f': //left up
        return 'h';
      case 'h': //right up
        return 'b';
    }
    return 0;
  }*/

  public static char getDirBetweenTwoCase(int cell1ID, int cell2ID, GameMap map, boolean Combat)
  {
    ArrayList<Character> dirs=new ArrayList<Character>();
    dirs.add('b');
    dirs.add('d');
    dirs.add('f');
    dirs.add('h');
    if(!Combat)
    {
      dirs.add('a');
      dirs.add('b');
      dirs.add('c');
      dirs.add('d');
    }
    for(char c : dirs)
    {
      int cell=cell1ID;
      for(int i=0;i<=64;i++)
      {
        if(GetCaseIDFromDirrection(cell,c,map,Combat)==cell2ID)
          return c;
        cell=GetCaseIDFromDirrection(cell,c,map,Combat);
      }
    }
    return 0;
  }

  //2.0 - Dynamic AoEs
  public static ArrayList<GameCase> getCellListFromAreaString(GameMap map, int cellID, int castCellID, String zoneStr, int PONum, boolean isCC, int chChance)
  {
    if(zoneStr.contains("::"))
    {
      ArrayList<Integer> aoeSizes=new ArrayList<Integer>();
      if(chChance!=0)
      {
        if(!isCC)
          zoneStr=zoneStr.substring(2,2+(zoneStr.length()-2)/2);
        else
          zoneStr=zoneStr.substring(2+(zoneStr.length()-2)/2,zoneStr.length());
      }

      for(int i=0;i<zoneStr.length()/2;i++)
        aoeSizes.add(Main.world.getCryptManager().getIntByHashedValue(zoneStr.charAt(2*i+1)));
      ArrayList<GameCase> cases=new ArrayList<GameCase>();
      if(map.getCase(cellID)==null)
        return cases;
      cases.add(map.getCase(cellID));

      for(int i=0;i<zoneStr.length()/2;i++)
      {
        switch(zoneStr.charAt(2*i))
        {
          case 'C':// Cercle
          {
            for(int a=0;a<aoeSizes.get(i);a++)
            {
              char[] dirs= { 'b', 'd', 'f', 'h' };
              ArrayList<GameCase> cases2=new ArrayList<GameCase>();
              cases2.addAll(cases);
              for(GameCase aCell : cases2)
              {
                for(char d : dirs)
                {
                  GameCase cell=map.getCase(GetCaseIDFromDirrection(aCell.getId(),d,map,true));
                  if(cell==null)
                    continue;
                  if(!cases.contains(cell))
                    cases.add(cell);
                }
              }
            }
            break;
          }
          case 'X':// Croix
          {
            char[] dirs= { 'b', 'd', 'f', 'h' };
            for(char d : dirs)
            {
              int cID=cellID;
              for(int a=0;a<aoeSizes.get(i);a++)
              {
                cases.add(map.getCase(GetCaseIDFromDirrection(cID,d,map,true)));
                cID=GetCaseIDFromDirrection(cID,d,map,true);
              }
            }
            break;
          }
          case 'L':// Ligne
          {
            char dir=PathFinding.getDirBetweenTwoCase(castCellID,cellID,map,true);
            for(int a=0;a<aoeSizes.get(i)-1;a++)
            {
              cases.add(map.getCase(GetCaseIDFromDirrection(cellID,dir,map,true)));
              cellID=GetCaseIDFromDirrection(cellID,dir,map,true);
            }
            break;
          }
          case 'D': //Alternating rings
          {
            break;
          }
          case 'O': //Hollow Ring
          {
            break;
          }
          case 'T': //Horizontal Line
          {
            break;
          }
          case 'R': //Supposed to be Rectangle, is broken
          {
            char[] dirs3= { 'h', 'd' };
            for(char d : dirs3)
            {
              int cID=cellID;
              for(int a=0;a<aoeSizes.get(i);a++)
              {
                if(!cases.contains(map.getCase(GetCaseIDFromDirrection(cID,d,map,true))))
                  cases.add(map.getCase(GetCaseIDFromDirrection(cID,d,map,true)));
                cID=GetCaseIDFromDirrection(cID,d,map,true);
              }
            }

            char[] dirs= { 'b', 'f' };
            for(char d : dirs)
            {
              int cID=cellID;
              for(int a=0;a<aoeSizes.get(i);a++)
              {
                if(!cases.contains(map.getCase(GetCaseIDFromDirrection(cID,d,map,true))))
                  cases.add(map.getCase(GetCaseIDFromDirrection(cID,d,map,true)));
                int tempcID=map.getCase(GetCaseIDFromDirrection(cID,d,map,true)).getId();
                cID=map.getCase(GetCaseIDFromDirrection(cID,d,map,true)).getId();
                char[] dirs2= { 'h', 'd' };
                for(char ch : dirs2)
                {
                  int cID2=cID;
                  for(int a2=0;a2<aoeSizes.get(i);a2++)
                  {
                    if(!cases.contains(map.getCase(GetCaseIDFromDirrection(cID2,d,map,true))))
                      cases.add(map.getCase(GetCaseIDFromDirrection(cID2,ch,map,true)));
                    cID2=map.getCase(GetCaseIDFromDirrection(cID2,ch,map,true)).getId();
                  }
                }
                cID=tempcID;
              }
            }
            break;
          }
          default:
            break;
        }
      }
      return cases;
    }
    else
    {
      ArrayList<GameCase> cases=new ArrayList<GameCase>();
      int c=PONum;
      if(map.getCase(cellID)==null)
        return cases;
      cases.add(map.getCase(cellID));
      int taille = 0;
      try {
      taille=Main.world.getCryptManager().getIntByHashedValue(zoneStr.charAt(c+1));
    }
    catch(final Exception e)
    {
    	taille = 0;
    }
      try {
      switch(zoneStr.charAt(c))
      {
        case 'C':// Cercle
        {
          for(int a=0;a<taille;a++)
          {
            char[] dirs= { 'b', 'd', 'f', 'h' };
            ArrayList<GameCase> cases2=new ArrayList<GameCase>();
            cases2.addAll(cases);
            for(GameCase aCell : cases2)
            {
              for(char d : dirs)
              {
                GameCase cell=map.getCase(PathFinding.GetCaseIDFromDirrection(aCell.getId(),d,map,true));
                if(cell==null)
                  continue;
                if(!cases.contains(cell))
                  cases.add(cell);
              }
            }
          }
          break;
        }
        case 'X':// Croix
        {
          char[] dirs= { 'b', 'd', 'f', 'h' };
          for(char d : dirs)
          {
            int cID=cellID;
            for(int a=0;a<taille;a++)
            {
              cases.add(map.getCase(GetCaseIDFromDirrection(cID,d,map,true)));
              cID=GetCaseIDFromDirrection(cID,d,map,true);
            }
          }
          break;
        }

        case 'L'://Vertical Line
        {
          char dir=PathFinding.getDirBetweenTwoCase(castCellID,cellID,map,true);
          for(int a=0;a<taille;a++)
          {
            cases.add(map.getCase(GetCaseIDFromDirrection(cellID,dir,map,true)));
            cellID=GetCaseIDFromDirrection(cellID,dir,map,true);
          }
          break;
        }
        

        case 'D': //Alternating rings
        {
          break;
        }

        case 'O': //Hollow Ring
        {
          break;
        }
        case 'T': //Horizontal Line
        {
          break;
        }
        case 'R': //Supposed to be Rectangle, is broken
        {
          char[] dirs3= { 'h', 'd' };
          for(char d : dirs3)
          {
            int cID=cellID;
            for(int a=0;a<taille;a++)
            {
              if(!cases.contains(map.getCase(GetCaseIDFromDirrection(cID,d,map,true))))
                cases.add(map.getCase(GetCaseIDFromDirrection(cID,d,map,true)));
              cID=GetCaseIDFromDirrection(cID,d,map,true);
            }
          }

          char[] dirs= { 'b', 'f' };
          for(char d : dirs)
          {
            int cID=cellID;
            for(int a=0;a<taille;a++)
            {
              if(!cases.contains(map.getCase(GetCaseIDFromDirrection(cID,d,map,true))))
                cases.add(map.getCase(GetCaseIDFromDirrection(cID,d,map,true)));
              int tempcID=map.getCase(GetCaseIDFromDirrection(cID,d,map,true)).getId();
              cID=map.getCase(GetCaseIDFromDirrection(cID,d,map,true)).getId();
              char[] dirs2= { 'h', 'd' };
              for(char ch : dirs2)
              {
                int cID2=cID;
                for(int a2=0;a2<taille;a2++)
                {
                  if(!cases.contains(map.getCase(GetCaseIDFromDirrection(cID2,d,map,true))))
                    cases.add(map.getCase(GetCaseIDFromDirrection(cID2,ch,map,true)));
                  cID2=map.getCase(GetCaseIDFromDirrection(cID2,ch,map,true)).getId();
                }
              }
              cID=tempcID;
            }
          }
          break;
        }

        default:
          break;
      }
      }
      catch(final Exception e)
      {
      	System.out.println(" bug pousse "+zoneStr);
      	System.out.println(" bug pousse "+c);
      }
      return cases;
    }
  }

  public static int getCellXCoord(GameMap map, int cellID)
  {
    if(map==null)
      return 0;
    int w=map.getW();
    return ((cellID-(w-1)*getCellYCoord(map,cellID))/w);
  }

  public static int getCellYCoord(GameMap map, int cellID)
  {
    int w=map.getW();
    int loc5=cellID/((w*2)-1);
    int loc6=cellID-loc5*((w*2)-1);
    int loc7=loc6%w;
    return (loc5-loc7);
  }

  public static boolean checkLoS(GameMap map, int cell1, int cell2, Fighter fighter)
  {
    {
      if(fighter!=null&&fighter.getPersonnage()!=null) // on ne revérifie pas (en plus du client) pour les joueurs
        return true;
      ArrayList<Integer> CellsToConsider=new ArrayList<Integer>();
      CellsToConsider=getLoSBotheringIDCases(map,cell1,cell2,true);
      if(CellsToConsider==null)
      {
        return true;
      }
      for(Integer cellID : CellsToConsider)
      {
        if(map.getCase(cellID)!=null)
          if(!map.getCase(cellID).blockLoS()||(!map.getCase(cellID).isWalkable(false)))
          {
            return false;
          }
      }
      return true;
    }
    /*if(fighter.getPersonnage()!=null)
      return true;
    int dist=getDistanceBetween(map,cell1,cell2);
    ArrayList<Integer> los=new ArrayList<Integer>();
    if(dist>2)
      los=getLoS(cell1,cell2);
    if(los!=null&&dist>2)
    {
      for(int i : los)
      {
        if(i!=cell1&&i!=cell2&&!map.getCase(i).blockLoS())
          return false;
      }
    }
    if(dist>2)
    {
      int cell=getNearestCellAround(map,cell2,cell1,null);
      if(cell!=-1&&!map.getCase(cell).blockLoS())
        return false;
    }
    return true;*/
  }

  public static int getNearestCellAround(GameMap map, int startCell, int endCell, ArrayList<GameCase> forbidens)
  {
    if(map==null)
      return -1;
    // On prend la cellule autour de la cible, la plus proche
    int dist=1000;
    int cellID=startCell;
    if(forbidens==null)
      forbidens=new ArrayList<GameCase>();
    char[] dirs= { 'b', 'd', 'f', 'h' };
    for(char d : dirs)
    {

      // On cherche la celluleID correspondant é la direction associé
      int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
      if(map.getCase(c)==null)
        continue;
      // On cherche la distance entre
      int dis=PathFinding.getDistanceBetween(map,endCell,c);
      // Si la distance est strictement inférieur é 1000 et que la case
      // est marchable et que personne ne
      // se trouve dessus et que la case n'est pas interdite
      if(dis<dist&&map.getCase(c).isWalkable(true,true,-1)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
      {
        // On crée la distance
        dist=dis;
        // On modifie la cellule
        cellID=c;
      }
    }
    // On renvoie -1 si pas trouvé
    return cellID==startCell ? -1 : cellID;
  }

  public static int getNearestCellAroundGA(GameMap map, int startCell, int endCell, ArrayList<GameCase> forbidens)
  {
    //On prend la cellule autour de la cible, la plus proche
    int dist=1000;
    int cellID=startCell;
    if(forbidens==null)
      forbidens=new ArrayList<GameCase>();
    char[] dirs= { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };
    for(char d : dirs)
    {
      int c=PathFinding.GetCaseIDFromDirrection(startCell,d,map,true);
      int dis=PathFinding.getDistanceBetween(map,endCell,c);
      if(map.getCase(c)==null)
        continue;
      if(dis<dist&&map.getCase(c).isWalkable(true)&&map.getCase(c).getFirstFighter()==null&&!forbidens.contains(map.getCase(c)))
      {
        dist=dis;
        cellID=c;
      }
    }

    //On renvoie -1 si pas trouvé
    return cellID==startCell ? -1 : cellID;
  }

  public static ArrayList<GameCase> getShortestPathBetween(GameMap map, int start, int dest, int distMax)
  {
    ArrayList<GameCase> curPath=new ArrayList<GameCase>();
    ArrayList<GameCase> curPath2=new ArrayList<GameCase>();
    ArrayList<GameCase> closeCells=new ArrayList<GameCase>();
    int limit=1000;
    GameCase curCase=map.getCase(start);
    int stepNum=0;
    boolean stop=false;

    while(!stop&&stepNum++<=limit)
    {
      int nearestCell=getNearestCellAround(map,curCase.getId(),dest,closeCells);
      if(nearestCell==-1)
      {
        closeCells.add(curCase);
        if(curPath.size()>0)
        {
          curPath.remove(curPath.size()-1);
          if(curPath.size()>0)
            curCase=curPath.get(curPath.size()-1);
          else
            curCase=map.getCase(start);
        }
        else
          curCase=map.getCase(start);
      }
      else if(distMax==0&&nearestCell==dest)
      {
        curPath.add(map.getCase(dest));
        break;
      }
      else if(distMax>PathFinding.getDistanceBetween(map,nearestCell,dest))
      {
        curPath.add(map.getCase(dest));
        break;
      }
      else
      //on continue
      {
        curCase=map.getCase(nearestCell);
        closeCells.add(curCase);
        curPath.add(curCase);
      }
    }

    curCase=map.getCase(start);
    closeCells.clear();
    if(!curPath.isEmpty())
      closeCells.add(curPath.get(0));

    while(!stop&&stepNum++<=limit)
    {
      int nearestCell=getNearestCellAround(map,curCase.getId(),dest,closeCells);
      if(nearestCell==-1)
      {
        closeCells.add(curCase);
        if(curPath2.size()>0)
        {
          curPath2.remove(curPath2.size()-1);
          if(curPath2.size()>0)
            curCase=curPath2.get(curPath2.size()-1);
          else
            curCase=map.getCase(start);
        }
        else
        //Si retour a zero
        {
          curCase=map.getCase(start);
        }
      }
      else if(distMax==0&&nearestCell==dest)
      {
        curPath2.add(map.getCase(dest));
        break;
      }
      else if(distMax>PathFinding.getDistanceBetween(map,nearestCell,dest))
      {
        curPath2.add(map.getCase(dest));
        break;
      }
      else
      //on continue
      {
        curCase=map.getCase(nearestCell);
        closeCells.add(curCase);
        curPath2.add(curCase);
      }
    }

    if((curPath2.size()<curPath.size()&&curPath2.size()>0)||curPath.isEmpty())
      curPath=curPath2;
    return curPath;
  }

  public static String getShortestStringPathBetween(GameMap map, int start, int dest, int distMax)
  {
    if(start==dest)
      return null;
    Pair<Integer, ArrayList<GameCase>> prePath=PathFinding.getPath(map,(short)start,(short)dest,-1);
    ArrayList<GameCase> path=null;
    if(prePath!=null)
      path=prePath.getRight();
    //ArrayList<GameCase> path=getShortestPathBetween(map,start,dest,distMax);
    if(path==null)
      return null;
    String pathstr="";
    int curCaseID=start;
    char curDir='\000';
    for(GameCase c : path)
    {
      char d=getDirBetweenTwoCase(curCaseID,c.getId(),map,true);
      if(d==0)
        return null;
      if(curDir!=d)
      {
        if(path.indexOf(c)!=0)
          pathstr=pathstr+Main.world.getCryptManager().cellID_To_Code(curCaseID);
        pathstr=pathstr+d;
        curDir=d;
      }
      curCaseID=c.getId();
    }
    if(curCaseID!=start)
    {
      pathstr=pathstr+Main.world.getCryptManager().cellID_To_Code(curCaseID);
    }
    if(pathstr=="")
      return null;
    return "a"+Main.world.getCryptManager().cellID_To_Code(start)+pathstr;
  }

  public static boolean isBord1(int id)
  {
    int[] bords= { 1, 30, 59, 88, 117, 146, 175, 204, 233, 262, 291, 320, 349, 378, 407, 436, 465, 15, 44, 73, 102, 131, 160, 189, 218, 247, 276, 305, 334, 363, 392, 421, 450, 479 };
    ArrayList<Integer> test=new ArrayList<Integer>();
    for(int i : bords)
    {
      test.add(i);
    }

    return test.contains(id);
  }

  public static boolean isBord2(int id)
  {
    int[] bords= { 16, 45, 74, 103, 132, 161, 190, 219, 248, 277, 306, 335, 364, 393, 422, 451, 29, 58, 87, 116, 145, 174, 203, 232, 261, 290, 319, 348, 377, 406, 435, 464 };
    ArrayList<Integer> test=new ArrayList<Integer>();
    for(int i : bords)
    {
      test.add(i);
    }

    return test.contains(id);
  }

  public static ArrayList<Integer> getLoS(int cell1, int cell2)
  {
    ArrayList<Integer> Los=new ArrayList<Integer>();
    int cell=cell1;
    boolean next=false;
    int[] dir1= { 1, -1, 29, -29, 15, 14, -15, -14 };

    for(int i : dir1)
    {
      Los.clear();
      cell=cell1;
      Los.add(cell);
      next=false;
      while(!next)
      {
        cell+=i;
        Los.add(cell);
        if(isBord2(cell)||isBord1(cell)||cell<=0||cell>=480)
          next=true;
        if(cell==cell2)
        {
          return Los;
        }
      }
    }
    return null;
  }

  public static boolean checkLoS(GameMap map, int cell1, int cell2, Fighter fighter, boolean needsWalkable)
  {
    if(fighter!=null&&fighter.getPersonnage()!=null) //on ne revérifie pas (en plus du client) pour les joueurs
      return true;
    ArrayList<Integer> CellsToConsider=new ArrayList<Integer>();
    CellsToConsider=getLoSBotheringIDCases(map,cell1,cell2,true);
    if(CellsToConsider==null)
    {
      return true;
    }
    for(Integer cellID : CellsToConsider)
    {
      if(map.getCase(cellID)!=null)
        if(!map.getCase(cellID).blockLoS()||(!map.getCase(cellID).isWalkable(false)&&needsWalkable))
          return false;
    }
    return true;
  }

  private static ArrayList<Integer> getLoSBotheringIDCases(GameMap map, int cellID1, int cellID2, boolean Combat)
  {
    ArrayList<Integer> toReturn=new ArrayList<Integer>();
    int consideredCell1=cellID1;
    int consideredCell2=cellID2;
    char dir='b';
    int diffX=0;
    int diffY=0;
    int compteur=0;
    ArrayList<Character> dirs=new ArrayList<Character>();
    dirs.add('b');
    dirs.add('d');
    dirs.add('f');
    dirs.add('h');

    while(getDistanceBetween(map,consideredCell1,consideredCell2)>2&&compteur<300)
    {
      diffX=getCellXCoord(map,consideredCell1)-getCellXCoord(map,consideredCell2);
      diffY=getCellYCoord(map,consideredCell1)-getCellYCoord(map,consideredCell2);
      if(Math.abs(diffX)>Math.abs(diffY))
      { // si il ya une plus grande différence pour la premiére coordonnée
        if(diffX>0)
          dir='f';
        else
          dir='b';
        consideredCell1=GetCaseIDFromDirrection(consideredCell1,dir,map,Combat); // on avance le chemin d'obstacles possibles
        consideredCell2=GetCaseIDFromDirrection(consideredCell2,getOpositeDirection(dir),map,Combat); // des deux cétés
        toReturn.add(consideredCell1); // la liste des cases potentiellement obstacles
        toReturn.add(consideredCell2); // la liste des cases potentiellement obstacles
      }
      else if(Math.abs(diffX)<Math.abs(diffY))
      { // si il y a une plus grand différence pour la seconde
        if(diffY>0) // détermine dans quel sens
          dir='h';
        else
          dir='d';
        consideredCell1=GetCaseIDFromDirrection(consideredCell1,dir,map,Combat); // on avance le chemin d'obstacles possibles
        consideredCell2=GetCaseIDFromDirrection(consideredCell2,getOpositeDirection(dir),map,Combat); // des deux cétés
        toReturn.add(consideredCell1); // la liste des cases potentiellement obstacles
        toReturn.add(consideredCell2); // la liste des cases potentiellement obstacles
      }
      else
      {
        if(compteur==0) // si on est en diagonale parfaite
          return getLoSBotheringCasesInDiagonal(map,cellID1,cellID2,diffX,diffY);
        if(dir=='f'||dir=='b') // on change la direction dans le cas oé on se retrouve en diagonale
          if(diffY>0)
            dir='h';
          else
            dir='d';
        else if(dir=='h'||dir=='d')
          if(diffX>0)
            dir='f';
          else
            dir='b';
        consideredCell1=GetCaseIDFromDirrection(consideredCell1,dir,map,Combat); // on avance le chemin d'obstacles possibles
        consideredCell2=GetCaseIDFromDirrection(consideredCell2,getOpositeDirection(dir),map,Combat); // des deux cétés
        toReturn.add(consideredCell1); // la liste des cases potentiellement obstacles
        toReturn.add(consideredCell2); // la liste des cases potentiellement obstacles
      }
      compteur++;
    }
    if(getDistanceBetween(map,consideredCell1,consideredCell2)==2)
    {
      dir=0;
      diffX=getCellXCoord(map,consideredCell1)-getCellXCoord(map,consideredCell2);
      diffY=getCellYCoord(map,consideredCell1)-getCellYCoord(map,consideredCell2);
      if(diffX==0)
        if(diffY>0)
          dir='h';
        else
          dir='d';
      if(diffY==0)
        if(diffX>0)
          dir='f';
        else
          dir='b';
      if(dir!=0)
        toReturn.add(GetCaseIDFromDirrection(consideredCell1,dir,map,Combat));
    }
    return toReturn;
  }

  private static ArrayList<Integer> getLoSBotheringCasesInDiagonal(GameMap map, int cellID1, int cellID2, int diffX, int diffY)
  {
    ArrayList<Integer> toReturn=new ArrayList<Integer>();
    char dir='a';
    if(diffX>0&&diffY>0)
      dir='g';
    if(diffX>0&&diffY<0)
      dir='e';
    if(diffX<0&&diffY>0)
      dir='a';
    if(diffX<0&&diffY<0)
      dir='c';
    int consideredCell=cellID1,compteur=0;
    while(consideredCell!=-1&&compteur<100)
    {
      consideredCell=GetCaseIDFromDirrection(consideredCell,dir,map,true);
      if(consideredCell==cellID2)
        return toReturn;
      toReturn.add(consideredCell);
      compteur++;
    }
    return toReturn;
  }

  public static ArrayList<Fighter> getFightersAround(int cellID, GameMap map, Fight fight)
  {
    char[] dirs= { 'b', 'd', 'f', 'h' };
    ArrayList<Fighter> fighters=new ArrayList<>();

    for(char dir : dirs)
    {
      GameCase gameCase=map.getCase(GetCaseIDFromDirrection(cellID,dir,map,false));
      if(gameCase==null)
        continue;
      Fighter f=gameCase.getFirstFighter();
      if(f!=null)
        fighters.add(f);
    }
    return fighters;
  }

  public static char getDirEntreDosCeldas(GameMap map, int id1, int id2)
  {
    if(id1==id2)
      return 0;
    if(map==null)
      return 0;
    int difX=(getCellXCoord(map,id1)-getCellXCoord(map,id2));
    int difY=(getCellYCoord(map,id1)-getCellYCoord(map,id2));
    int difXabs=Math.abs(difX);
    int difYabs=Math.abs(difY);
    if(difXabs>difYabs)
    {
      if(difX>0)
        return 'f';
      else
        return 'b';
    }
    else
    {
      if(difY>0)
        return 'h';
      else
        return 'd';
    }
  }

  public static int getCellArroundByDir(int cellId, char dir, GameMap map)
  {
    if(map==null)
      return -1;

    switch(dir)
    {
      case 'b':
        return cellId+map.getW();//En Haut é Droite.
      case 'd':
        return cellId+(map.getW()-1);//En Haut é Gauche.
      case 'f':
        return cellId-map.getW();//En Bas é Gauche.
      case 'h':
        return cellId-map.getW()+1;//En Bas é Droite.
    }
    return -1;
  }

  public static GameCase checkIfCanPushEntity(Fight fight, int startCell, int endCell, char direction)
  {
    GameMap map=fight.getMap();
    GameCase cell=map.getCase(getCellArroundByDir(startCell,direction,map));
    GameCase oldCell=cell;
    GameCase actualCell=cell;

    while(actualCell.getId()!=endCell)
    {
      actualCell=map.getCase(getCellArroundByDir(actualCell.getId(),direction,map));
      if(!actualCell.getFighters().isEmpty()||!actualCell.isWalkable(true))
        return oldCell;

      for(Trap trap : fight.getAllTraps())
      {
        if(PathFinding.getDistanceBetween(fight.getMap(),trap.getCell().getId(),actualCell.getId())<=trap.getSize())
          return actualCell;
      }

      oldCell=actualCell;
    }

    return null;
  }

  public static boolean haveFighterOnThisCell(int cell, Fight fight)
  {
    for(Fighter f : fight.getFighters(3))
    {
      if(f.getCell().getId()==cell&&!f.isDead())
        return true;
    }
    return false;
  }

  public static int getCaseIDFromDirrection(int CaseID, char Direccion, GameMap map)
  {
    switch(Direccion)
    {// mag.get_w() = te da el ancho del mapa
      case 'b':
        return CaseID+map.getW(); // diagonal derecha abajo
      case 'd':
        return CaseID+(map.getW()-1); // diagonal izquierda abajo
      case 'f':
        return CaseID-map.getW(); // diagonal izquierda arriba
      case 'h':
        return CaseID-map.getW()+1;// diagonal derecha arriba
    }
    return -1;
  }

  public static boolean cellArroundCaseIDisOccuped(Fight fight, int cell)
  {
    char[] dirs= { 'b', 'd', 'f', 'h' };
    ArrayList<Integer> Cases=new ArrayList<Integer>();

    for(char dir : dirs)
    {
      int caseID=PathFinding.GetCaseIDFromDirrection(cell,dir,fight.getMap(),true);
      Cases.add(caseID);
    }
    int ha=0;
    for(int o=0;o<Cases.size();o++)
    {
      if(fight.getMap().getCase(Cases.get(o)).getFirstFighter()!=null)
        ha++;
    }
    return ha!=4;
  }

  public static int getAvailableCellArround(Fight fight, int cellId, List<Integer> cellsUnavailable)
  {
    if(fight==null)
      return 0;
    char[] dirs= { 'f', 'd', 'b', 'h' };

    for(char dir : dirs)
    {
      int id=GetCaseIDFromDirrection(cellId,dir,fight.getMap(),false);
      GameCase cell=fight.getMap().getCase(id);

      if(cell!=null)
      {
        Fighter fighter=cell.getFirstFighter();
        if(fighter==null&&cell.isWalkable(false))
        {
          if(cellsUnavailable!=null&&cellsUnavailable.contains(cell.getId()))
            continue;
          return cell.getId();
        }
      }
    }
    return 0;
  }

  public static int getCaseBetweenEnemy(int cellId, GameMap map, Fight fight)
  {
    if(map==null)
      return 0;
    char[] dirs= { 'f', 'd', 'b', 'h' };
    for(char dir : dirs)
    {
      int id=GetCaseIDFromDirrection(cellId,dir,map,false);
      GameCase cell=map.getCase(id);
      if(cell==null)
        continue;
      Fighter f=cell.getFirstFighter();

      if(f==null&&cell.isWalkable(false))
        return cell.getId();
    }
    return 0;
  }

  public static ArrayList<GameCase> getCellListFromAreaString(GameMap map, int cellID, int castCellID, String zoneStr, int PONum, boolean isCC)
  {
    ArrayList<GameCase> cases=new ArrayList<GameCase>();
    int c=PONum;
    if(map.getCase(cellID)==null)
      return cases;
    cases.add(map.getCase(cellID));

    int taille=Main.world.getCryptManager().getIntByHashedValue(zoneStr.charAt(c+1));
    switch(zoneStr.charAt(c))
    {
      case 'C':// Cercle
        for(int a=0;a<taille;a++)
        {
          char[] dirs= { 'b', 'd', 'f', 'h' };
          ArrayList<GameCase> cases2=new ArrayList<GameCase>();// on évite les
          // modifications
          // concurrentes
          cases2.addAll(cases);
          for(GameCase aCell : cases2)
          {
            for(char d : dirs)
            {
              GameCase cell=map.getCase(PathFinding.GetCaseIDFromDirrection(aCell.getId(),d,map,true));
              if(cell==null)
                continue;
              if(!cases.contains(cell))
                cases.add(cell);
            }
          }
        }
        break;

      case 'X':// Croix
        char[] dirs= { 'b', 'd', 'f', 'h' };
        for(char d : dirs)
        {
          int cID=cellID;
          for(int a=0;a<taille;a++)
          {
            cases.add(map.getCase(GetCaseIDFromDirrection(cID,d,map,true)));
            cID=GetCaseIDFromDirrection(cID,d,map,true);
          }
        }
        break;

      case 'L':// Ligne
        char dir=PathFinding.getDirBetweenTwoCase(castCellID,cellID,map,true);
        for(int a=0;a<taille;a++)
        {
          cases.add(map.getCase(GetCaseIDFromDirrection(cellID,dir,map,true)));
          cellID=GetCaseIDFromDirrection(cellID,dir,map,true);
        }
        break;

      case 'P':// Player?

        break;

      default:
        break;
    }
    return cases;
  }

  public static int getDistanceBetweenTwoCase(GameMap map, GameCase c1, GameCase c2)
  {
    int dist=0;
    if(c1==null||c2==null)
    {
      return dist;
    }
    if(c1.getId()==c2.getId())
      return dist;
    int id=c1.getId();
    char c=getDirBetweenTwoCase(c1.getId(),c2.getId(),map,true);

    while(c2!=map.getCase(id))
    {
      id=GetCaseIDFromDirrection(id,c,map,true);
      if(map.getCase(id)==null)
      {
        return dist;
      }
      dist++;
    }
    return dist;
  }

  public static Pair<Integer, ArrayList<GameCase>> getPath(final GameMap mapa, final short celdaInicio, short celdaDestino, int PM)
  {
    int i=0;
    if(mapa == null)
    	return null;
    while(i<5)
    {
      try
      {
        if(celdaInicio==celdaDestino)
          return null;
        if(PM==-1)
          PM=500;
        final int _PM=PM;
        final byte _ancho=mapa.getW();
        final int _nroLados=4;
        final byte[] _diagonales= { _ancho, (byte)(_ancho-1), (byte)-_ancho, (byte)-(_ancho-1) };
        final byte[] _unos= { 1, 1, 1, 1 };
        final Map<Short, GameCase> _celdas=new TreeMap<Short, GameCase>();
        for(GameCase cell : mapa.getCases())
          _celdas.put((short)cell.getId(),cell);
        final Map<Short, CeldaCamino> _celdasCamino1=new TreeMap<Short, CeldaCamino>();
        final Map<Short, CeldaCamino> _celdasCamino2=new TreeMap<Short, CeldaCamino>();
        boolean _loc18=false;
        final CeldaCamino _newCeldaCamino=new CeldaCamino();
        _newCeldaCamino.num=celdaInicio;
        _newCeldaCamino.g=0;
        _newCeldaCamino.v=0;
        _newCeldaCamino.h=distanciaEstimada(mapa,celdaInicio,celdaDestino);
        _newCeldaCamino.f=_newCeldaCamino.h;
        _newCeldaCamino.l=_celdas.get(celdaInicio).getLevel();
        _newCeldaCamino.m=_celdas.get(celdaInicio).getMovimiento();
        _newCeldaCamino.parent=null;
        _celdasCamino1.put(_newCeldaCamino.num,_newCeldaCamino);
        while(!_loc18)
        {
          short _loc20=-1;
          int _loc21=500000;
          for(final CeldaCamino c : _celdasCamino1.values())
          {
            if(c.f<_loc21)
            {
              _loc21=c.f;
              _loc20=c.num;
            }
          }
          CeldaCamino _celdaCamino=_celdasCamino1.get(_loc20);
          _celdasCamino1.remove(_loc20);
          if(_celdaCamino.num==celdaDestino) //end
          {
            final ArrayList<GameCase> _tempCeldas=new ArrayList<GameCase>();
            while(_celdaCamino.num!=celdaInicio)
            {
              if(_celdaCamino.m==0)
              {
                _tempCeldas.clear();
              }
              else
              {
                _tempCeldas.add(0,_celdas.get(_celdaCamino.num));
              }
              _celdaCamino=_celdaCamino.parent;
            }
            return new Pair<Integer, ArrayList<GameCase>>(i,_tempCeldas);
          }
          boolean _loc24=false;
          for(byte _nDiagonal=0;_nDiagonal<_nroLados;_nDiagonal++)
          {
            final short _tempCeldaID=(short)(_celdaCamino.num+_diagonales[_nDiagonal]);
            if(_celdas.get(_tempCeldaID) != null && Math.abs(_celdas.get(_tempCeldaID).getX()-_celdas.get(_celdaCamino.num).getX())<=53)
            {
              final GameCase _tempCelda=_celdas.get(_tempCeldaID);
              final byte _levelTempCelda=_tempCelda.getLevel();
              final boolean _sinLuchador=_tempCeldaID==celdaDestino ? true : (_tempCelda.getFirstFighter()!=null ? false : true);
              _loc24=_tempCeldaID==celdaDestino&&_tempCelda.getMovimiento()==1 ? true : false;
              final boolean _loc30=_celdaCamino.l==-1||Math.abs(_levelTempCelda-_celdaCamino.l)<2;
              if(_loc30&&_tempCelda.getActivo()&&_sinLuchador)
              {
                final short _loc31=_tempCeldaID;
                final short _loc32=(short)(_celdaCamino.v+_unos[_nDiagonal]+(_tempCelda.getMovimiento()==0||_tempCelda.getMovimiento()==1 ? 1000+(_nDiagonal%2==0 ? 3 : 0) : 0)+(_tempCelda.getMovimiento()==1&&_loc24 ? -1000 : (_nDiagonal!=_celdaCamino.d ? 0.500000 : 0)+(5-_tempCelda.getMovimiento())/3));
                final short _loc33=(short)(_celdaCamino.g+_unos[_nDiagonal]);
                short _loc34=-1;
                if(_celdasCamino1.get(_loc31)!=null)
                {
                  _loc34=_celdasCamino1.get(_loc31).v;
                }
                else if(_celdasCamino2.get(_loc31)!=null)
                {
                  _loc34=_celdasCamino2.get(_loc31).v;
                } // end else if
                if((_loc34==-1||_loc34>_loc32)&&_loc33<=_PM)
                {
                  if(_celdasCamino2.get(_loc31)!=null)
                  {
                    _celdasCamino2.remove(_loc31);
                  } // end if
                  final CeldaCamino _tempCeldaCamino=new CeldaCamino();
                  _tempCeldaCamino.num=_tempCeldaID;
                  _tempCeldaCamino.g=_loc33;
                  _tempCeldaCamino.v=_loc32;
                  _tempCeldaCamino.h=distanciaEstimada(mapa,_tempCeldaID,celdaDestino);
                  _tempCeldaCamino.f=(short)(_tempCeldaCamino.v+_tempCeldaCamino.h);
                  _tempCeldaCamino.d=_nDiagonal;
                  _tempCeldaCamino.l=_levelTempCelda;
                  _tempCeldaCamino.m=_tempCelda.getMovimiento();
                  _tempCeldaCamino.parent=_celdaCamino;
                  _celdasCamino1.put(_loc31,_tempCeldaCamino);
                }
              }
            }
          }
          _celdasCamino2.put(_celdaCamino.num,new CeldaCamino());
          _celdasCamino2.get(_celdaCamino.num).v=_celdaCamino.v;
          _loc18=true;
          for(final CeldaCamino c : _celdasCamino1.values())
          {
            if(c==null)
            {
              continue;
            }
            _loc18=false;
            break;
          }
        }
        return null;
      }
      catch(final Exception e)
      {
    	  e.printStackTrace();
        celdaDestino=celdaMasCercanaACeldaObjetivo(mapa,celdaDestino,celdaInicio,null,false);
        i++;
      }
    }
    return null;
  }

  public static short distanciaEstimada(final GameMap mapa, final short celdaInicio, final short celdaDestino)
  {
    if(celdaInicio==celdaDestino)
    {
      return 0;
    }
    GameCase cInicio=mapa.getCase(celdaInicio);
    GameCase cDestino=mapa.getCase(celdaDestino);
    final int difX=Math.abs(cInicio.getX()-cDestino.getX());
    final int difY=Math.abs(cInicio.getY()-cDestino.getY());
    return (short)Math.sqrt(Math.pow(difX,2)+Math.pow(difY,2));
  }

  private static short celdaMasCercanaACeldaObjetivo(final GameMap mapa, final short celdaInicio, final short celdaDestino, ArrayList<GameCase> celdasProhibidas, final boolean ocupada)
  {
    if(mapa==null)
      return -1;
    if(mapa.getCase(celdaInicio)==null||mapa.getCase(celdaDestino)==null)
      return -1;
    int dist=1000;
    short celdaID=celdaInicio;
    if(celdasProhibidas==null)
    {
      celdasProhibidas=new ArrayList<GameCase>();
    }
    final char[] dirs=listaDirEntreDosCeldas(mapa,celdaInicio,celdaDestino);
    for(final char d : dirs)
    {
      final short sigCelda=getSigIDCeldaMismaDir(celdaInicio,d,mapa,true);
      final GameCase celda=mapa.getCase(sigCelda);
      if(celda==null)
      {
        continue;
      }
      final int distancia=getDistanceBetween(mapa,celdaInicio,celdaDestino);
      if(distancia<dist&&celda.isWalkable(true)&&(!ocupada||celda.getFirstFighter()==null)&&!celdasProhibidas.contains(celda))
      {
        dist=distancia;
        celdaID=sigCelda;
      }
    }
    return celdaID==celdaInicio ? -1 : celdaID;
  }

  public static char[] listaDirEntreDosCeldas(final GameMap mapa, final short celdaInicio, final short celdaDestino)
  {
    char[] abc;
    if(celdaInicio==celdaDestino||mapa==null)
    {
      abc=new char[] {};
      return abc;
    }
    GameCase cInicio=mapa.getCase(celdaInicio);
    GameCase cDestino=mapa.getCase(celdaDestino);
    final int difX=cInicio.getX()-cDestino.getX();
    final int difY=cInicio.getY()-cDestino.getY();
    final int difXabs=Math.abs(difX);
    final int difYabs=Math.abs(difY);
    if(difXabs>difYabs)
    {
      if(difX>0)
      {
        if(difY>0)
        {
          abc=new char[] { 'f', 'h', 'b', 'd' };
        }
        else
        {
          abc=new char[] { 'f', 'd', 'b', 'h' };
        }
      }
      else
      {
        if(difY>0)
        {
          abc=new char[] { 'b', 'h', 'f', 'd' };
        }
        else
        {
          abc=new char[] { 'b', 'd', 'f', 'h' };
        }
      }
    }
    else
    {
      if(difY>0)
      {
        if(difX>0)
        {
          abc=new char[] { 'h', 'f', 'd', 'b' };
        }
        else
        {
          abc=new char[] { 'h', 'b', 'd', 'f' };
        }
      }
      else
      {
        if(difX>0)
        {
          abc=new char[] { 'd', 'f', 'h', 'b' };
        }
        else
        {
          abc=new char[] { 'd', 'b', 'h', 'f' };
        }
      }
    }
    return abc;
  }

  public static short getSigIDCeldaMismaDir(final short celdaID, final char direccion, final GameMap mapa, final boolean combate)
  {
    switch(direccion)
    {
      case 'a':
        return (short)(combate ? -1 : celdaID+1);// derecha
      case 'b':
        return (short)(celdaID+mapa.getW()); // diagonal derecha abajo
      case 'c':
        return (short)(combate ? -1 : celdaID+(mapa.getW()*2-1));// abajo
      case 'd':
        return (short)(celdaID+(mapa.getW()-1)); // diagonal izquierda abajo
      case 'e':
        return (short)(combate ? -1 : celdaID-1);// izquierda
      case 'f':
        return (short)(celdaID-mapa.getW()); // diagonal izquierda arriba
      case 'g':
        return (short)(combate ? -1 : celdaID-(mapa.getW()*2-1));// arriba
      case 'h':
        return (short)(celdaID-mapa.getW()+1);// diagonal derecha arriba
    }
    return -1;
  }

  public static byte getIndexDireccion(char c)
  {
    byte b=0;
    for(char a : DIRECTIONS)
    {
      if(a==c)
        return b;
      b++;
    }
    return 0;
  }

  public static short cellMoveSprite(final GameMap map, final int cell)
  {
    final ArrayList<Short> celdasPosibles=new ArrayList<Short>();
    final short ancho=map.getW();
    final short[] dir= { (short)-ancho, (short)-(ancho-1), (short)(ancho-1), ancho };
    for(final short element : dir)
    {
      try
      {
        if(cell+element>14||cell+element<464)
        {
        	if(map.getCase((short)(cell+element)) == null)
        		 return -1;
          if(map.getCase((short)(cell+element)).isWalkable(false))
          {
            celdasPosibles.add((short)(cell+element));
          }
        }
      }
      catch(Exception e)
      {
    	  e.printStackTrace();
      }
    }
    if(celdasPosibles.size()<=0)
    {
      return -1;
    }
    return celdasPosibles.get(Formulas.getRandomValue(0,celdasPosibles.size()-1));
  }

  public static char direccionEntreDosCeldas(final GameMap mapa, final short celdaInicio, final short celdaDestino, final boolean esPelea)
  {
    if(celdaInicio==celdaDestino||mapa==null)
    {
      return 0;
    }
    if(!esPelea)
    {
      final byte ancho=mapa.getW();
      final byte[] _loc6= { 1, ancho, (byte)(ancho*2-1), (byte)(ancho-1), -1, (byte)-ancho, (byte)(-ancho*2+1), (byte)-(ancho-1) };
      final int _loc7=celdaDestino-celdaInicio;
      for(int _loc8=7;_loc8>=0;_loc8--)
      {
        if(_loc6[_loc8]==_loc7)
        {
          return DIRECTIONS[_loc8];
        }
      }
    }
    GameCase cInicio=mapa.getCase(celdaInicio);
    GameCase cDestino=mapa.getCase(celdaDestino);
    final int difX=cDestino.getX()-cInicio.getX();
    final int difY=cDestino.getY()-cInicio.getY();
    if(difX==0)
    {
      if(difY>0)
      {
        return DIRECTIONS[3];
      }
      else
      {
        return DIRECTIONS[7];
      }
    }
    else if(difX>0)
    {
      return DIRECTIONS[1];
    }
    else
    {
      return DIRECTIONS[5];
    }
  }

  public static String getPathToString(GameMap mapa, ArrayList<GameCase> celdas, short celdaInicio, boolean esPelea)
  {
    StringBuilder pathStr=new StringBuilder();
    short tempCeldaID=celdaInicio;
    for(final GameCase celda : celdas)
    {
      final char dir=direccionEntreDosCeldas(mapa,tempCeldaID,(short)celda.getId(),esPelea);
      if(dir==0)
      {
        return "";
      }
      pathStr.append(dir);
      pathStr.append(Main.world.getCryptManager().cellID_To_Code(celda.getId()));
      tempCeldaID=(short)celda.getId();
    }
    return pathStr.toString();
  }

  public static short numeroMovimientos(final GameMap mapa, final Fight pelea, final AtomicReference<String> pathRef, final short celdaInicio, final short celdaFinal, Player perso)
  {
      _nroMovimientos=0;
      short nuevaCelda=celdaInicio;
      short movimientos=0;
      final String path=pathRef.get();
      final StringBuilder nuevoPath=new StringBuilder();
      for(int i=0;i<path.length();i+=3)
      {
        if(path.length()<i+3)
        {
          return movimientos;
        }
        final String miniPath=path.substring(i,i+3);
        final char dir=miniPath.charAt(0);
        final short celdaTemp=(short)Main.world.getCryptManager().cellCode_To_ID(miniPath.substring(1));
        _nroMovimientos=0;

        final String[] aPathInfos=pathSimpleValido(nuevaCelda,celdaTemp,dir,mapa,pelea,celdaFinal,perso).split(Pattern.quote(";"));
        if(aPathInfos[0].equalsIgnoreCase("invisible"))
        {
          nuevaCelda=Short.parseShort(aPathInfos[1]);
          movimientos+=_nroMovimientos;
          nuevoPath.append(dir+Main.world.getCryptManager().cellID_To_Code(nuevaCelda));
          pathRef.set(nuevoPath.toString());
          return (short)(movimientos+20000);
        }
        else if(aPathInfos[0].equalsIgnoreCase("stop")||aPathInfos[0].equalsIgnoreCase("trampa"))
        {
          nuevaCelda=Short.parseShort(aPathInfos[1]);
          movimientos+=_nroMovimientos;
          nuevoPath.append(dir+Main.world.getCryptManager().cellID_To_Code(nuevaCelda));
          pathRef.set(nuevoPath.toString());
          return (short)(movimientos+10000);
        }
        else if(aPathInfos[0].equalsIgnoreCase("ok"))
        {
          nuevaCelda=celdaTemp;
          movimientos+=_nroMovimientos;
        }
        else if(aPathInfos[0].equalsIgnoreCase("no"))
        {
          pathRef.set(nuevoPath.toString());
          return -1000;
        }
        nuevoPath.append(dir+Main.world.getCryptManager().cellID_To_Code(nuevaCelda));
      }
      pathRef.set(nuevoPath.toString());
      return movimientos;
   
  }

  private static String pathSimpleValido(final short celdaID, final short celdaSemiFinal, final char dir, final GameMap mapa, final Fight pelea, final short celdaFinalDest, Player perso)
  {
    _nroMovimientos=0;
    // if (pelea != null && pelea.celdaOcupada(celdaFinal)) {
    // return "stop:" + celdaFinal;
    // }
    short ultimaCelda=celdaID;
    for(_nroMovimientos=1;_nroMovimientos<=64;_nroMovimientos++)
    {
      final short celdaTempID=getSigIDCeldaMismaDir(ultimaCelda,dir,mapa,pelea!=null);
      GameCase celdaTemp=mapa.getCase(celdaTempID);
      if(celdaTemp==null||!celdaTemp.isWalkable(true))
      {
        return "stop;"+ultimaCelda;
      }
      if(pelea!=null)
      {
        Fighter ocupado=mapa.getCase(celdaTempID).getFirstFighter();
        Fighter luchTurno=pelea.getFighterByOrdreJeu();
        if(ocupado!=null)
        {
          _nroMovimientos--;
          if(ocupado.isHide()&&ocupado.getTeam()!=luchTurno.getTeam())
          {
            return ("invisible;"+ultimaCelda);
          }
          else
          {
            return ("stop;"+ultimaCelda);
          }
        }
        if(celdaTempID!=celdaFinalDest)
        {
          // si algun luchador esta alrededor por donde va a pasar
          //Fighter alrededor=this.ge //getEnemigoAlrededor(celdaTempID,mapa,null,-2);
          Fighter alrededor=getEnemyAround(celdaTempID,mapa,pelea);
          if(alrededor!=null&&alrededor.getId()!=luchTurno.getId())
          {
            if(alrededor.isHide()&&alrededor.getTeam()!=luchTurno.getTeam())
            {
              return "invisible;"+celdaTempID;
            }
            else
            {
              return "stop;"+celdaTempID;
            }
          }
          // si se topa con una trampa
          if(pelea.getAllTraps()!=null&&pelea.getAllTraps().size()!=0)
          {
            for(final Trap trampa : pelea.getAllTraps())
            {
              final int dist=getDistanceBetween(mapa,trampa.getCell().getId(),celdaTempID);
              if(dist<=trampa.getSize())
              {
                return "trampa;"+celdaTempID;
              }
            }
          }
        }
      }
      if(celdaTempID==celdaSemiFinal)
      {
        return "ok";
      }
      ultimaCelda=celdaTempID;
    }
    return "no";
  }
}
