package soufix.other;

import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.game.World;
import soufix.game.action.ExchangeAction;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.object.ObjectTemplate;
import soufix.utility.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Dopeul
{
  private static Map<Integer, Pair<Integer, Integer>> donjons=new HashMap<>();

  public static Map<Integer, Pair<Integer, Integer>> getDonjons()
  {
    return donjons;
  }

  public static void getReward(Player player, int type)
  {
    GameMap curMap=player.getCurMap();
    int idMap=Main.world.getTempleByClasse(player.getClasse());
    switch(type)
    {
      case 1: //Sort spécial
        if(!player.hasItemTemplate(getDoplonByClasse(player.getClasse()),1))
        { //Si on a pas le doplon de classe
          SocketManager.GAME_SEND_Im_PACKET(player,"14");
          return;
        } else if(curMap.getId()!=(short)idMap) //Si on est pas dans le temple de notre classe
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Vous n'étes pas dans le temple de votre classe.");
          return;
        } else if(player.hasSpell(Constant.getSpecialSpellByClasse(player.getClasse()))) // Si on a déjé le sort
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Vous avez déjé appris le sort.");
          return;
        }

        player.learnSpell(Constant.getSpecialSpellByClasse(player.getClasse()),1,true,true,true);
        removeObject(player,getDoplonByClasse(player.getClasse()),1);
        break;

      case 2://Trousseau de clés
        if(player.hasItemTemplate(10207,1))
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Vous avez déjé le trousseau de clés.");
          return;
        }
        int doplon=hasOneDoplon(player);
        if(doplon==-1)
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14");
          return;
        }
        GameObject obj=Main.world.getObjTemplate(10207).createNewItem(1,true);
        if(player.addObjet(obj,false))
          World.addGameObject(obj,true);
        removeObject(player,doplon,1);
        break;

      case 3://Reset spell
        ArrayList<Integer> doplons=hasQuaDoplon(player,7);

        if(doplons.contains(Dopeul.getDoplonByClasse(player.getClasse())))
        {
          removeObject(player,Dopeul.getDoplonByClasse(player.getClasse()),7);
        } else
        {
          doplons=Dopeul.hasQuaDoplon(player,1);
          if(doplons.size()==12)
          {
            for(int id : doplons)
              removeObject(player,id,1);
          } else
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"14");
            return;
          }
        }

        player.setExchangeAction(new ExchangeAction<>(ExchangeAction.FORGETTING_SPELL,0));
        SocketManager.GAME_SEND_FORGETSPELL_INTERFACE('+',player);
        break;

      case 4://Reset caractéristiques
        if(!player.hasItemTemplate(getDoplonByClasse(player.getClasse()),1))
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14");
          return;
        } else if(curMap.getId()!=(short)idMap) // Si on est pas dans le temple de notre classe
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Vous n'étes pas dans le temple de votre classe!");
          return;
        } else if(player.hasItemTemplate(10601,1))
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Vous ne pouvez pas réinitialiser vos statistiques plusieurs fois.");
          return;
        }
        player.getStats().addOneStat(125,-player.getStats().getEffect(125));
        player.getStats().addOneStat(124,-player.getStats().getEffect(124));
        player.getStats().addOneStat(118,-player.getStats().getEffect(118));
        player.getStats().addOneStat(123,-player.getStats().getEffect(123));
        player.getStats().addOneStat(119,-player.getStats().getEffect(119));
        player.getStats().addOneStat(126,-player.getStats().getEffect(126));
        player.addCapital((player.getLevel()-1)*5-player.get_capital());

        ObjectTemplate OT=Main.world.getObjTemplate(10601); // On lui donne un certificat de restat
        GameObject obj2=OT.createNewItem(1,false);
        if(player.addObjet(obj2,true)) //Si le joueur n'avait pas d'item similaire
          World.addGameObject(obj2,true);
        obj2.refreshStatsObjet("325"+System.currentTimeMillis());
        SocketManager.GAME_SEND_STATS_PACKET(player);
        removeObject(player,getDoplonByClasse(player.getClasse()),1);
        break;

      case 5://Guildalogemme
        doplons=hasQuaDoplon(player,1);
        if(doplons==null)
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14");
          return;
        }
        obj=Main.world.getObjTemplate(1575).createNewItem(1,true);
        if(player.addObjet(obj,false))
          World.addGameObject(obj,true);
        for(int id : doplons)
          removeObject(player,id,1);
        break;

      case 6://Parchemin de caractéristique
        SocketManager.GAME_SEND_MESSAGE(player,"Coming soon..");
        break;
    }
    SocketManager.GAME_SEND_Ow_PACKET(player);
    //Database.getStatics().getPlayerData().update(player);
  }

  public static Integer getDoplonByClasse(int classe)
  {
    switch(classe)
    {
      case Constant.CLASS_FECA:
        return 10306;
      case Constant.CLASS_OSAMODAS:
        return 10308;
      case Constant.CLASS_ENUTROF:
        return 10305;
      case Constant.CLASS_SRAM:
        return 10312;
      case Constant.CLASS_XELOR:
        return 10313;
      case Constant.CLASS_ECAFLIP:
        return 10303;
      case Constant.CLASS_ENIRIPSA:
        return 10304;
      case Constant.CLASS_IOP:
        return 10307;
      case Constant.CLASS_CRA:
        return 10302;
      case Constant.CLASS_SADIDA:
        return 10311;
      case Constant.CLASS_SACRIEUR:
        return 10310;
      case Constant.CLASS_PANDAWA:
        return 10309;
    }
    return -1;
  }

  public static int hasOneDoplon(Player perso)
  {
    if(perso.hasItemTemplate(10306,1))
      return 10306;
    else if(perso.hasItemTemplate(10308,1))
      return 10308;
    else if(perso.hasItemTemplate(10305,1))
      return 10305;
    else if(perso.hasItemTemplate(10312,1))
      return 10312;
    else if(perso.hasItemTemplate(10313,1))
      return 10313;
    else if(perso.hasItemTemplate(10303,1))
      return 10303;
    else if(perso.hasItemTemplate(10304,1))
      return 10304;
    else if(perso.hasItemTemplate(10307,1))
      return 10307;
    else if(perso.hasItemTemplate(10302,1))
      return 10302;
    else if(perso.hasItemTemplate(10311,1))
      return 10311;
    else if(perso.hasItemTemplate(10310,1))
      return 10310;
    else if(perso.hasItemTemplate(10309,1))
      return 10309;
    else
      return -1;
  }

  private static ArrayList<Integer> hasQuaDoplon(Player perso, int qua)
  {
    ArrayList<Integer> doplons=new ArrayList<>();

    if(perso.hasItemTemplate(10306,qua))
      doplons.add(10306);
    if(perso.hasItemTemplate(10308,qua))
      doplons.add(10308);
    if(perso.hasItemTemplate(10305,qua))
      doplons.add(10305);
    if(perso.hasItemTemplate(10312,qua))
      doplons.add(10312);
    if(perso.hasItemTemplate(10313,qua))
      doplons.add(10313);
    if(perso.hasItemTemplate(10303,qua))
      doplons.add(10303);
    if(perso.hasItemTemplate(10304,qua))
      doplons.add(10304);
    if(perso.hasItemTemplate(10307,qua))
      doplons.add(10307);
    if(perso.hasItemTemplate(10302,qua))
      doplons.add(10302);
    if(perso.hasItemTemplate(10311,qua))
      doplons.add(10311);
    if(perso.hasItemTemplate(10310,qua))
      doplons.add(10310);
    if(perso.hasItemTemplate(10309,qua))
      doplons.add(10309);
    return doplons;
  }

  private static void removeObject(Player perso, int id, int qua)
  {
    perso.removeByTemplateID(id,qua);
    SocketManager.GAME_SEND_Ow_PACKET(perso);
    SocketManager.GAME_SEND_Im_PACKET(perso,"022;"+qua+"~"+id);
  }

  /**
   * Trousseau de clef *
   */
  public static boolean parseConditionTrousseau(String stats, int npc, int map)
  {
    Pair<Integer, Integer> couple=donjons.get(map);
    if(stats.contains("]"))
    stats = stats.replace("]", "");
    if(stats.contains("["))
        stats = stats.replace("[", "");
    if(couple!=null)
      if(couple.getLeft()==npc&&Integer.toHexString(couple.getRight()).startsWith(stats))
        return true;
    return false;
  }

  public static String generateStats()
  {
    StringBuilder stats=new StringBuilder();

    for(Pair<Integer, Integer> couple : donjons.values())
    {
      if(!stats.toString().isEmpty())
        stats.append(",");
      stats.append(Integer.toHexString(couple.getRight()));
    }
    return stats.toString();
  }

  public static Map<Integer, String> generateStatsTrousseau()
  {
    Map<Integer, String> txtStat=new HashMap<>();
    txtStat.put(Constant.STATS_NAME_DJ,generateStats());
    txtStat.put(Constant.STATS_DATE,String.valueOf(System.currentTimeMillis()));
    return txtStat;
  }
  
  public static Map<Integer, Pair<Integer, Integer>> getDopeul()
  {
    java.util.Map<Integer, Pair<Integer, Integer>> changeDopeul=new HashMap<Integer, Pair<Integer, Integer>>();
    changeDopeul.put(1549,new Pair<Integer, Integer>(167,460)); // Dopeul iop
    changeDopeul.put(1466,new Pair<Integer, Integer>(169,465)); // Dopeul sadida
    changeDopeul.put(1558,new Pair<Integer, Integer>(168,458)); // Dopeul cra
    changeDopeul.put(1470,new Pair<Integer, Integer>(162,464)); // Dopeul enu
    changeDopeul.put(1469,new Pair<Integer, Integer>(164,468)); // Dopeul xelor
    changeDopeul.put(1546,new Pair<Integer, Integer>(161,461)); // Dopeul osa
    changeDopeul.put(1554,new Pair<Integer, Integer>(160,469)); // Dopeul feca
    changeDopeul.put(6928,new Pair<Integer, Integer>(166,462)); // Dopeul eni
    changeDopeul.put(8490,new Pair<Integer, Integer>(2691,466)); // Dopeul panda
    changeDopeul.put(6926,new Pair<Integer, Integer>(163,467)); // Dopeul sram
    changeDopeul.put(1544,new Pair<Integer, Integer>(165,459)); // Dopeul eca
    changeDopeul.put(6949,new Pair<Integer, Integer>(455,463)); // Dopeul sacri
    return changeDopeul;
  }
}