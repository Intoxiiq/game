package soufix.entity.npc;

import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.common.PathFinding;
import soufix.common.SocketManager;
import soufix.entity.Npc;
import soufix.main.Main;
import soufix.utility.Pair;

import java.util.ArrayList;

public class NpcMovable extends Npc
{

  private final static ArrayList<NpcMovable> movables=new ArrayList<>();

  private GameMap map;
  private short position=0;
  private String[] path;

  public NpcMovable(int id, int cellid, byte orientation, short mapid, NpcTemplate template)
  {
    super(id,cellid,orientation,template);
    this.map=Main.world.getMap(mapid);
    this.path=template.getPath().split(";");
    NpcMovable.movables.add(this);
  }

  private void move()
  {
    char dir=this.path[this.position].charAt(0);
    short nbr;

    if(dir=='E')
    {
      nbr=Short.parseShort(this.path[this.position].substring(1));

      for(Player player : this.map.getPlayers())
        player.send("eUK"+this.getId()+"|"+nbr);
    } else
    {
      nbr=Short.parseShort(String.valueOf(this.path[this.position].charAt(1)));

      int oldCell=this.getCellid(),cell=this.getCellid();

      for(short i=0;i<=nbr;i++)
      {
        cell=PathFinding.getCaseIDFromDirrection(cell,NpcMovable.getDirByChar(dir),this.map);
        if(!this.map.getCase(cell).isWalkable(true))
          break;
        oldCell=cell;
      }

      final Pair<Integer, ArrayList<GameCase>> pathCells=PathFinding.getPath(this.map,(short)oldCell,(short)this.getCellid(),-1);
      if(pathCells==null)
      {
        return;
      }
      final ArrayList<GameCase> cells=pathCells.getRight();
      String path=PathFinding.getPathToString(this.map,cells,(short)oldCell,false);

      if(path==null)
        return;

      for(Player player : this.map.getPlayers())
        SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"0","1",String.valueOf(this.getId()),path);

      this.setCellid(oldCell);
    }

    this.position++;

    if(this.position==this.path.length)
    {
      this.path=(NpcMovable.getPath(this.path).equals(this.getTemplate().getPath()) ? NpcMovable.inverseOfPath(this.getTemplate().getPath()).split(";") : this.getTemplate().getPath().split(";"));
      this.position=0;
    }
  }

  //v2.7 - Replaced String += with StringBuilder
  private static String inverseOfPath(String arg)
  {
    String[] split=arg.split(";");
    StringBuilder var=new StringBuilder();

    for(int i=split.length-1;i>=0;i--)
    {
      String loc0=split[i];

      if(loc0.contains("R"))
        continue;

      switch(loc0.charAt(0))
      {
        case 'H':
          loc0=loc0.replace("H","B");
          break;
        case 'B':
          loc0=loc0.replace("B","H");
          break;
        case 'G':
          loc0=loc0.replace("G","D");
          break;
        case 'D':
          loc0=loc0.replace("D","G");
          break;
      }

      var.append((var.toString().isEmpty() ? "" : ";")+loc0);
    }
    return var.toString();
  }

  //v2.7 - Replaced String += with StringBuilder
  private static String getPath(String[] path)
  {
    StringBuilder original=new StringBuilder();
    for(String arg : path)
      original.append((original.toString().isEmpty() ? "" : ";")+arg);
    return original.toString();
  }

  private static char getDirByChar(char letter)
  {
    switch(letter)
    {
      case 'H':
        return 'f';
      case 'B':
        return 'b';
      case 'G':
        return 'd';
      case 'D':
        return 'h';
      default:
        return '?';
    }
  }

  public static void moveAll()
  {
    NpcMovable.movables.forEach(NpcMovable::move);
  }
}