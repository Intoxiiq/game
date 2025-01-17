package soufix.common;

import soufix.Hdv.Hdv;
import soufix.Hdv.HdvEntry;
import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.area.map.entity.InteractiveObject;
import soufix.area.map.entity.MountPark;
import soufix.area.map.entity.Trunk;
import soufix.client.Player;
import soufix.client.other.Party;
import soufix.client.other.Shortcuts;
import soufix.client.other.Stats;
import soufix.client.other.IStats;
import soufix.entity.Collector;
import soufix.entity.Npc;
import soufix.entity.Prism;
import soufix.entity.monster.MobGroup;
import soufix.entity.mount.Mount;
import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.game.GameClient;
import soufix.guild.Guild;
import soufix.guild.GuildMember;
import soufix.job.JobStat;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.object.ObjectSet;
import soufix.object.ObjectTemplate;
import soufix.quest.Quest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

public class SocketManager
{

  public static void send(Player player, String packet)
  {
    if(player!=null&&player.getAccount()!=null&&player.getGameClient()!=null)
    {
      send(player.getGameClient(),packet);
    }
  }

  public static void send(GameClient client, String packet)
  {
    if(client!=null)
      client.send(packet);
  }

  public static void GAME_SEND_UPDATE_ITEM(Player P, GameObject obj) // Utilisé pour tours bonbon
  {
	  if(obj == null)
		  return;
    String packet="OC|"+obj.parseItem();
    send(P,packet);
  }

  public static void MULTI_SEND_Af_PACKET(GameClient out, int position, int totalAbo, int totalNonAbo, int button)
  {
	  try {
    send(out,"Af"+position+"|"+totalAbo+"|"+totalNonAbo+"|"+button+"|"+Config.getInstance().serverId);
	  }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	    }
  }
  public static void ENVIAR_bl_RANKING_DATA(final Player ss, String param, String data) {
      final String packet = "be" + param + "|" + data;
      send(ss, packet);
  }
  public static void PACKET_ALL_CLASSE(GameClient out)
  {
    send(out,"éG1,2,3,4,5,6,7,8,9,10,11,12");
    send(out,"érR");
  }

  public static void GAME_SEND_ATTRIBUTE_FAILED(GameClient out)
  {
    String packet="ATE";
    send(out,packet);
  }

  public static void GAME_SEND_AV0(GameClient out)
  {
    String packet="AV0";
    send(out,packet);
  }

  //TODO: Abonement
  public static void GAME_SEND_PERSO_LIST(GameClient out, java.util.Map<Integer, Player> persos, long subscriber)
  {
    StringBuilder packet=new StringBuilder();

    packet.append("ALK");
      packet.append(subscriber);
    packet.append("|").append(persos.size());
    for(Entry<Integer, Player> entry : persos.entrySet())
      packet.append(entry.getValue().parseALK());
    send(out,packet.toString());
  }

  public static void GAME_SEND_NAME_ALREADY_EXIST(GameClient out)
  {
    String packet="AAEa";
    send(out,packet);
  }

  public static void GAME_SEND_CREATE_PERSO_FULL(GameClient out)
  {
    String packet="AAEf";
    send(out,packet);
  }

  public static void GAME_SEND_CREATE_OK(GameClient out)
  {
    String packet="AAK";
    send(out,packet);
  }

  public static void GAME_SEND_DELETE_PERSO_FAILED(GameClient out)
  {
    String packet="ADE";
    send(out,packet);
  }

  public static void GAME_SEND_CREATE_FAILED(GameClient out)
  {
    String packet="AAEF";
    send(out,packet);
  }

  public static void GAME_SEND_PERSO_SELECTION_FAILED(GameClient out)
  {
    String packet="ASE";
    send(out,packet);
  }
  public static void ENVIAR_Os_SETS_RAPIDOS(GameClient out)
  {
	  if(out.getPlayer() == null)
		  return;
	  final String packet = "ks" + out.getPlayer().getSetsRapidos();
    send(out,packet);
  }
  public static void GAME_SEND_STATS_PACKET(Player perso)
  {
    String packet=perso.getAsPacket(false,false);
    SocketManager.GAME_SEND_Ow_PACKET(perso);
    send(perso,packet);
  }
  public static void GAME_SEND_STATS_PACKET_ONE_WINDOWS(Player perso1 , Player perso2)
  {
    String packet=perso1.getAsPacket(false,true);
    send(perso2,packet);
  }

  public static void GAME_SEND_Rx_PACKET(Player out)
  {
    String packet="Rx"+out.getMountXpGive();
    send(out,packet);
  }

  public static void GAME_SEND_Rn_PACKET(Player out, String name)
  {
    String packet="Rn"+name;
    send(out,packet);
  }
  public static void GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(Player perso, GameObject item)
  {
    send(perso,"OCO"+item.parseItem());
  }

  public static void GAME_SEND_Re_PACKET(Player out, String sign, Mount DD)
  {
    String packet="Re"+sign;
    if(sign.equals("+"))
      packet+=DD.parse();
    send(out,packet);
  }

  public static void GAME_SEND_ASK(GameClient out, Player perso)
  {
    try
    {
      StringBuilder packet=new StringBuilder();
      int color1=perso.getColor1(),color2=perso.getColor2(),color3=perso.getColor3();
      if(perso.getObjetByPos(Constant.ITEM_POS_MALEDICTION)!=null)
      {
        if(perso.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId()==10838)
        {
          color1=16342021;
          color2=16342021;
          color3=16342021;
        }
      }
      packet.append("ASK|").append(perso.getId()).append("|").append(perso.getName()).append("|");
      packet.append(perso.getLevel()).append("|").append(perso.getMorphMode() ? -1 : perso.getClasse()).append("|").append(perso.getSexe());
      packet.append("|").append(perso.getGfxId()).append("|").append((color1==-1 ? "-1" : Integer.toHexString(color1)));
      packet.append("|").append((color2==-1 ? "-1" : Integer.toHexString(color2))).append("|");
      packet.append((color3==-1 ? "-1" : Integer.toHexString(color3))).append("|");
      packet.append(perso.parseItemToASK());
      send(out,packet.toString());
    }
    catch(Exception e)
    {
      e.printStackTrace();
      System.out.println("Error occured : "+e.getMessage());
    }
  }
  public static void GAME_SEND_ASK_WINDOW(GameClient out, Player perso)
  {
    try
    {
      StringBuilder packet=new StringBuilder();
      int color1=perso.getColor1(),color2=perso.getColor2(),color3=perso.getColor3();
      if(perso.getObjetByPos(Constant.ITEM_POS_MALEDICTION)!=null)
      {
        if(perso.getObjetByPos(Constant.ITEM_POS_MALEDICTION).getTemplate().getId()==10838)
        {
          color1=16342021;
          color2=16342021;
          color3=16342021;
        }
      }
      packet.append("kO").append(perso.getId()).append("|").append(perso.getName()).append("|");
      packet.append(perso.getLevel()).append("|").append(perso.getMorphMode() ? -1 : perso.getClasse()).append("|").append(perso.getSexe());
      packet.append("|").append(perso.getGfxId()).append("|").append((color1==-1 ? "-1" : Integer.toHexString(color1)));
      packet.append("|").append((color2==-1 ? "-1" : Integer.toHexString(color2))).append("|");
      packet.append((color3==-1 ? "-1" : Integer.toHexString(color3))).append("|");
      packet.append(perso.parseItemToASK());
      send(out,packet.toString());
    }
    catch(Exception e)
    {
      e.printStackTrace();
      System.out.println("Error occured : "+e.getMessage());
    }
  }

  public static void GAME_SEND_ALIGNEMENT(GameClient out, int alliID)
  {
    String packet="ZS"+alliID;
    send(out,packet);
  }

  public static void GAME_SEND_ADD_CANAL(GameClient out, String chans)
  {
    String packet="cC+"+chans;
    send(out,packet);
  }

  public static void GAME_SEND_ZONE_ALLIGN_STATUT(GameClient out)
  {
    String packet="al|"+Main.world.getSousZoneStateString();
    send(out,packet);
  }

  public static void GAME_SEND_RESTRICTIONS(GameClient out)
  {
    String packet="AR6bk";
    send(out,packet);
  }

  public static void GAME_SEND_Ow_PACKET(Player perso)
  {
    String packet="Ow"+perso.getPodUsed()+"|"+perso.getMaxPod();
    send(perso,packet);
  }

  public static void GAME_SEND_OT_PACKET(GameClient out, int id)
  {
    String packet="OT";
    if(id>0)
      packet+=id;
    send(out,packet);
  }

  public static void GAME_SEND_SEE_FRIEND_CONNEXION(GameClient out, boolean see)
  {
    String packet="FO"+(see ? "+" : "-");
    send(out,packet);

  }

  public static void GAME_SEND_GAME_CREATE(GameClient out, String _name)
  {
    String packet="GCK|1|"+_name;
    send(out,packet);

  }
  public static void PACKET_POPUP_DEPART(final Player perso, final String str) {
		final String packet = "BAIO" + str;
		send(perso, packet);
	}

  public static void GAME_SEND_SERVER_HOUR(GameClient out)
  {
    String packet=Main.gameServer.getServerTime();
    send(out,packet);

  }

  public static void GAME_SEND_MAPDATA(GameClient out, int id, String date, String key)
  {
    String packet="GDM|"+id+"|"+date+"|"+key;
    send(out,packet);

  }

  public static void GAME_SEND_GDK_PACKET(GameClient out)
  {
    String packet="GDK";
    send(out,packet);

  }
  /*public static void ENVIAR_GDM_MAPDATA_COMPLETO(final Player perso) {
		GameMap mapa = perso.getCurMap();
		final String packet = "GDM|" + mapa.getId() + "|" + mapa.getDate() + "||" + mapa.getW() + "|" + mapa.getH()
		+ "|" + mapa.get_bgI() + "|" + mapa.get_musicID() + "|" + mapa.get_ambienteID() + "|" + mapa.get_outDoor() + "|" + mapa.getCapabilities() + "|" + mapa.getDdata() + "|1";
		send(perso.getGameClient(),packet);
	}*/

  public static void GAME_SEND_MAP_MOBS_GMS_PACKETS(GameClient out, GameMap Map)
  {
	  if(Map == null ||out == null || out.getPlayer() == null)
		  return;
    String packet=Map.getMobGroupGMsPackets(out.getPlayer());
    if(packet.equals(""))
      return;
    send(out,packet);

  }

  public static void GAME_SEND_MAP_OBJECTS_GDS_PACKETS(GameClient out, GameMap Map)
  {
    String packet=Map.getObjectsGDsPackets();
    if(packet.equals(""))
      return;
    send(out,packet);
  }

  public static void GAME_SEND_MAP_NPCS_GMS_PACKETS(GameClient out, GameMap Map)
  {
    String packet=Map.getNpcsGMsPackets(out.getPlayer());
    if(packet.equals("")&&packet.length()<4)
      return;
    send(out,packet);
  }

  public static void GAME_SEND_MAP_PERCO_GMS_PACKETS(GameClient out, GameMap Map)
  {
    String packet=Collector.parseGM(Map);
    if(packet.length()<5)
      return;
    send(out,packet);
  }

  public static void GAME_SEND_ERASE_ON_MAP_TO_MAP(GameMap map, int guid)
  {
    if(map==null)
      return;
    String packet="GM|-"+guid;
    if(Main.world.getPlayer(guid) != null && Main.world.getPlayer(guid).isInvisible())
    {
    	send(Main.world.getPlayer(guid).getGameClient(),packet);
    	return;
    }
    for(Player z : map.getPlayers())
    {
    	if(z== null)
    	continue;
      if(z.getGameClient()==null)
        continue;

      send(z.getGameClient(),packet);
    }
  }
  public static void ENVIAR_Nf_BESTIARIO_DROPS(final GameClient ss, final String str) {
      String packet = "wf" + str;
      send(ss, packet);
      // if (MainServidor.MOSTRAR_ENVIOS) {
      // imprimir("BESTIARIO DROPS: PERSO", packet);
  }
  public static void ENVIAR_NE_DETALLE_MOB(final GameClient ss, final String str) {
      String packet = "wE" + str;
      send(ss, packet);
  }

  public static void ENVIAR_NF_BESTIARIO_MOBS(final GameClient ss, final String str) {
      String packet = "wF" + str;
      send(ss, packet);
  }

  public static void GAME_SEND_ON_FIGHTER_KICK(Fight f, int guid, int team)
  {
    String packet="GM|-"+guid;
    for(Fighter F : f.getFighters(team))
    {
      if(F.getPersonnage()==null||F.getPersonnage().getGameClient()==null||F.getPersonnage().getId()==guid)
        continue;
      send(F.getPersonnage().getGameClient(),packet);
    }
  }

  public static void GAME_SEND_ALTER_FIGHTER_MOUNT(Fight fight, Fighter fighter, int guid, int team, int otherteam)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("GM|-").append(guid).append((char)0x00).append(fighter.getGmPacket('+',true));
    for(Fighter F : fight.getFighters(team))
    {
      if(F.getPersonnage()==null||F.getPersonnage().getGameClient()==null||!F.getPersonnage().isOnline())
        continue;
      send(F.getPersonnage().getGameClient(),packet.toString());
    }
    if(otherteam>-1)
    {
      for(Fighter F : fight.getFighters(otherteam))
      {
        if(F.getPersonnage()==null||F.getPersonnage().getGameClient()==null||!F.getPersonnage().isOnline())
          continue;
        send(F.getPersonnage().getGameClient(),packet.toString());
      }
    }
  }

  public static void GAME_SEND_ADD_PLAYER_TO_MAP(GameMap map, Player perso)
  {
    String packet="GM|+"+perso.parseToGM();
	  if(perso.isInvisible()) {
		  send(perso,packet);
		  return;
	  }
    for(Player z : map.getPlayers())
    {
    	if(z == null)
    		continue;
      if(perso.get_size()>0) {
    	  if(z != null)
        send(z,packet);
      }
      else if(z.getGroupe()!=null) {
    	  if(z != null)
        send(z,packet);
      }
    }
  }

  public static void GAME_SEND_DUEL_Y_AWAY(GameClient out, int guid)
  {
    String packet="GA;903;"+guid+";o";
    send(out,packet);

  }

  public static void GAME_SEND_DUEL_E_AWAY(GameClient out, int guid)
  {
    String packet="GA;903;"+guid+";z";
    send(out,packet);

  }

  public static void GAME_SEND_MAP_NEW_DUEL_TO_MAP(GameMap map, int guid, int guid2)
  {
    String packet="GA;900;"+guid+";"+guid2;
    for(Player z : map.getPlayers())
      send(z,packet);
  }

  public static void GAME_SEND_CANCEL_DUEL_TO_MAP(GameMap map, int guid, int guid2)
  {
    String packet="GA;902;"+guid+";"+guid2;
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_MAP_START_DUEL_TO_MAP(GameMap map, int guid, int guid2)
  {
    String packet="GA;901;"+guid+";"+guid2;
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_MAP_FIGHT_COUNT(GameClient out, GameMap map)
  {
    String packet="fC"+map.getNbrFight();
    send(out,packet);

  }

  public static void GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(Fight fight, int teams, int state, int cancelBtn, int duel, int spec, int time, int type)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("GJK").append(state).append("|").append(cancelBtn).append("|").append(duel).append("|").append(spec).append("|").append(time).append("|").append(type);
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      send(f.getPersonnage(),packet.toString());
    }

  }

  public static void GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(Fight fight, int teams, String places, int team)
  {
    String packet="GP"+places+"|"+team;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }
  public static void GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT2(Player perso, String places, int team)
  {
    String packet="GDZ"+places+"|"+team;

      send(perso,packet);

  }

  public static void GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(GameMap map)
  {
    String packet="fC"+map.getNbrFight();
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(GameMap map, int arg1, int guid1, int guid2, int cell1, String str1, int cell2, String str2)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("Gc+").append(guid1).append(";").append(arg1).append("|").append(guid1).append(";").append(cell1).append(";").append(str1).append("|").append(guid2).append(";").append(cell2).append(";").append(str2);
    for(Player z : map.getPlayers())
      send(z,packet.toString());

  }

  public static void GAME_SEND_GAME_ADDFLAG_PACKET_TO_PLAYER(Player p, GameMap map, int arg1, int guid1, int guid2, int cell1, String str1, int cell2, String str2)
  {
    send(p,"Gc+"+guid1+";"+arg1+"|"+guid1+";"+cell1+";"+str1+"|"+guid2+";"+cell2+";"+str2);

  }

  public static void GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(GameMap map, int guid)
  {
    String packet="Gc-"+guid;
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(GameMap map, int teamID, Fighter perso)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("Gt").append(teamID).append("|+").append(perso.getId()).append(";").append(perso.getPacketsName()).append(";").append(perso.getLvl());
    for(Player z : map.getPlayers())
    {
      send(z,packet.toString());
    }

  }

  public static void GAME_SEND_ADD_IN_TEAM_PACKET_TO_PLAYER(Player p, GameMap map, int teamID, Fighter perso)
  {
    send(p,"Gt"+teamID+"|+"+perso.getId()+";"+perso.getPacketsName()+";"+perso.getLvl());

  }

  public static void GAME_SEND_REMOVE_IN_TEAM_PACKET_TO_MAP(GameMap map, int teamID, Fighter perso)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("Gt").append(teamID).append("|-").append(perso.getId()).append(";").append(perso.getPacketsName()).append(";").append(perso.getLvl());
    for(Player z : map.getPlayers())
      send(z,packet.toString());

  }

  public static void GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(GameMap map)
  {
    for(Player z : map.getPlayers())
      send(z,map.getMobGroupGMsPackets(z));

  }

  public static void GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(GameMap map, Player perso)
  {
    String packet=map.getMobGroupGMsPackets(perso); // Un par un comme sa lors du respawn :)
    send(perso,packet);

  }

  public static void GAME_SEND_MAP_MOBS_GM_PACKET(GameMap map, MobGroup current_Mobs)
  {
    for(Player z : map.getPlayers())
      send(z,"GM|"+current_Mobs.parseGM(z));

  }

  public static void GAME_SEND_MAP_GMS_PACKETS(GameMap map, Player _perso)
  {
    String packet=(_perso.getFight()!=null ? map.getFightersGMsPackets(_perso.getFight()) : map.getGMsPackets());
    send(_perso,packet);

  }

  public static void GAME_SEND_ON_EQUIP_ITEM(GameMap map, Player _perso)
  {
    String packet=_perso.parseToOa();
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_ON_EQUIP_ITEM_FIGHT(Player _perso, Fighter f, Fight F)
  {
    String packet=_perso.parseToOa();
    for(Fighter z : F.getFighters(f.getTeam2()))
    {
      if(z.getPersonnage()==null)
        continue;
      send(z.getPersonnage(),packet);
    }
    for(Fighter z : F.getFighters(f.getOtherTeam()))
    {
      if(z.getPersonnage()==null)
        continue;
      send(z.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(Fight fight, int teams, GameMap map, int guid, int cell)
  {
    String packet="GIC|"+guid+";"+cell+";1";
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_Ew_PACKET(Player perso, int pods, int podsMax)
  { //Pods de la dinde
    String packet="Ew"+pods+";"+podsMax+"";
    send(perso,packet);

  }

  public static void GAME_SEND_EL_MOUNT_PACKET(Player out, Mount drago)
  { // Inventaire dinde : Liste des objets
    String packet="EL"+drago.parseToMountObjects();
    send(out,packet);

  }

  public static void GAME_SEND_GM_MOUNT_TO_MAP(GameMap map, Mount dd)
  {
    String packet=dd.parseToGM();
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_GDO_OBJECT_TO_MAP(GameClient out, GameMap map)
  {// Actualisation d'une cellule
    String packet=map.getObjects();
    if(packet.equals(""))
      return;
    send(out,packet);
  }

  public static void GAME_SEND_GM_MOUNT(GameClient out, GameMap map, boolean ok)
  {
    String packet=map.getGMOfMount(ok,out.getPlayer());
    if(packet=="")
      return;
    send(out,packet);

  }

  public static void GAME_SEND_Ef_MOUNT_TO_ETABLE(Player perso, char c, String s)
  {
    String packet="Ef"+c+s;
    send(perso,packet);

  }

  public static void GAME_SEND_GA_ACTION_TO_MAP(GameMap mapa, String idUnique, int idAction, String s1, String s2)
  {
    String packet="GA"+idUnique+";"+idAction+";"+s1;
    if(!s2.equals(""))
      packet+=";"+s2;
    for(Player z : mapa.getPlayers())
      send(z,packet);

  }

  public static void SEND_GDO_PUT_OBJECT_MOUNT(GameMap map, String str)
  {
    String packet="GDO+"+str;
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void SEND_GDE_FRAME_OBJECT_EXTERNAL(GameMap map, String str)
  {
    String packet="GDE|"+str;
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void SEND_GDE_FRAME_OBJECT_EXTERNAL(Player perso, String str)
  {
    String packet="GDE|"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(GameMap map, char s, char option, int guid)
  {
    String packet="Go"+s+option+guid;
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(Fight fight, int teams, int guid, boolean b)
  {
	  if(fight == null)
		  return;
    String packet="GR"+(b ? "1" : "0")+guid;
    if(fight.getState()!=2)
      return;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      if(f.hasLeft())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_GJK_PACKET(Player out, int state, int cancelBtn, int duel, int spec, int time, int unknown)
  {

    send(out,"GJK"+state+"|"+cancelBtn+"|"+duel+"|"+spec+"|"+time+"|"+unknown);

  }

  public static void GAME_SEND_FIGHT_PLACES_PACKET(GameClient out, String places, int team)
  {
    String packet="GP"+places+"|"+team;

    send(out,packet);

  }

  public static void GAME_SEND_Im_PACKET_TO_ALL(String str)
  {
    String packet="Im"+str;
    for(Player perso : Main.world.getOnlinePlayers())
      send(perso,packet);

  }

  public static void GAME_SEND_Im_PACKET(Player out, String str)
  {
    String packet="Im"+str;
    send(out,packet);

  }

  public static void GAME_SEND_ILS_PACKET(Player out, int i)
  {
    String packet="ILS"+i;
    send(out,packet);
  }

  public static void GAME_SEND_ILF_PACKET(Player P, int i)
  {
    String packet="ILF"+i;
    send(P,packet);
  }

  public static void GAME_SEND_Im_PACKET_TO_MAP(GameMap map, String id)
  {
    String packet="Im"+id;
    for(Player z : map.getPlayers())
      send(z,packet);
  }

  public static void GAME_SEND_Im_PACKET_TO_PLAYER(Player p, String id)
  {
    String packet="Im"+id;
    send(p,packet);
  }

  public static void GAME_SEND_eUK_PACKET_TO_MAP(GameMap map, int guid, int emote)
  {
    String packet="eUK"+guid+"|"+emote;
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_Im_PACKET_TO_FIGHT(Fight fight, int teams, String id)
  {
    String packet="Im"+id;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_MESSAGE(Player out, String mess, String color)
  {
    String packet="cs<font color='#"+color+"'>"+mess+"</font>";
    send(out,packet);
  }

  public static void GAME_SEND_MESSAGE(Player out, String mess)
  {
    String packet="cs<font color='#"+Config.getInstance().colorMessage+"'>"+mess+"</font>";
    send(out,packet);
  }

  public static void GAME_SEND_MESSAGE_TO_MAP(GameMap map, String mess, String color)
  {
    String packet="cs<font color='#"+color+"'>"+mess+"</font>";
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_GA903_ERROR_PACKET(GameClient out, char c, int guid)
  {
    String packet="GA;903;"+guid+";"+c;
    send(out,packet);

  }

  public static void GAME_SEND_GIC_PACKETS_TO_FIGHT(Fight fight, int teams)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("GIC|");
    for(Fighter p : fight.getFighters(3))
    {
      if(p.getCell()==null)
        continue;
      packet.append(p.getId()).append(";").append(p.getCell().getId()).append(";1|");
    }
    for(Fighter perso : fight.getFighters(teams))
    {
      if(perso.hasLeft())
        continue;
      if(perso.getPersonnage()==null||!perso.getPersonnage().isOnline())
        continue;
      send(perso.getPersonnage(),packet.toString());
    }

  }

  public static void GAME_SEND_GIC_PACKET_TO_FIGHT(Fight fight, int teams, Fighter f)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("GIC|").append(f.getId()).append(";").append(f.getCell().getId()).append(";1|");

    for(Fighter perso : fight.getFighters(teams))
    {
      if(perso.hasLeft())
        continue;
      if(perso.getPersonnage()==null||!perso.getPersonnage().isOnline())
        continue;
      send(perso.getPersonnage(),packet.toString());
    }

  }

  public static void GAME_SEND_GIC_PACKETS(Fight fight, Player out)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("GIC|");
    for(Fighter p : fight.getFighters(3))
    {
      if(p.getCell()==null)
        continue;
      packet.append(p.getId()).append(";").append(p.getCell().getId()).append(";1|");
    }
    send(out,packet.toString());

  }
  public static void GAME_SEND_STATS_BOUTIQUE_PACKET(final Player perso) {
		final String packet = perso.getAsPacket(true,false);
		GAME_SEND_Ow_PACKET(perso);
		send(perso, packet);
	}

  public static void GAME_SEND_GS_PACKET_TO_FIGHT(Fight fight, int teams)
  {
    String packet="GS";
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      f.initBuffStats();
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_GS_PACKET(Player out)
  {
    String packet="GS";
    send(out,packet);

  }

  public static void GAME_SEND_GTL_PACKET_TO_FIGHT(Fight fight, int teams)
  {
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),fight.getGTL());
    }
  }

  public static void GAME_SEND_GTL_PACKET(Player out, Fight fight)
  {
    String packet=fight.getGTL();
    send(out,packet);

  }

  public static void GAME_SEND_GTM_PACKET_TO_FIGHT(Fight fight, int teams)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("GTM");
    for(Fighter f : fight.getFighters(3))
    {
      packet.append("|").append(f.getId()).append(";");
      if(f.isDead())
      {
        packet.append("1");
        continue;
      }
      packet.append("0;").append(f.getPdv()).append(";").append(f.getPa()).append(";").append(f.getPm()).append(";");
      if(f.getCell() != null)
      packet.append((f.isHide() ? "-1" : f.getCell().getId())).append(";");//On envoie pas la cell d'un invisible :p
      packet.append(";");//??
      packet.append(f.getPdvMax());
    }
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet.toString());
    }

  }

  public static void GAME_SEND_GTM_PACKET(Player out, Fight fight)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("GTM");
    for(Fighter f : fight.getFighters(3))
    {
      packet.append("|").append(f.getId()).append(";");
      if(f.isDead())
      {
        packet.append("1");
        continue;
      }
      else
        packet.append("0;").append(f.getPdv()).append(";").append(f.getPa()).append(";").append(f.getPm()).append(";");
      packet.append((f.isHide() ? "-1" : f.getCell().getId())).append(";");//On envoie pas la cell d'un invisible :p
      packet.append(";");//??
      packet.append(f.getPdvMax());
    }
    send(out,packet.toString());
  }

  public static void GAME_SEND_GAMETURNSTART_PACKET_TO_FIGHT(Fight fight, int teams, int guid, int time, int turns)
  {
    String packet="GTS"+guid+"|"+time+"|"+turns;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;

      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_GAMETURNSTART_PACKET(Player P, int guid, int time, int turns)
  {
    String packet="GTS"+guid+"|"+time+"|"+turns;
    send(P,packet);

  }

  public static void GAME_SEND_GV_PACKET(Player P)
  {
    String packet="GV";
    send(P,packet);

  }

  public static void GAME_SEND_PONG(GameClient out)
  {
    String packet="pong";
    send(out,packet);
  }

  public static void GAME_SEND_QPONG(GameClient out)
  {
    String packet="qpong";
    send(out,packet);
  }

  public static void GAME_SEND_GAS_PACKET_TO_FIGHT(Fight fight, int teams, int guid)
  {
    String packet="GAS"+guid;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_GA_PACKET_TO_FIGHT(Fight fight, int teams, int actionID, String s1, String s2)
  {
    String packet="GA;"+actionID+";"+s1;

    if(!s2.isEmpty())
      packet+=";"+s2;

    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft()||f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

    try
    {
      Thread.sleep(Config.getInstance().gameActionDelay);
    }
    catch(Exception e)
    {
     // e.printStackTrace();
    }
  }

  public static void GAME_SEND_GA_PACKET(Fight fight, Player perso, int actionID, String s1, String s2)
  {
    String packet="GA;"+actionID+";"+s1;
    if(!s2.equals(""))
      packet+=";"+s2;
    send(perso,packet);
  }

  public static void SEND_SB_SPELL_BOOST(Player perso, String modif)
  {
    String packet="SB"+modif;
    send(perso,packet);
  }

  public static void GAME_SEND_GA_PACKET(GameClient out, String actionID, String s0, String s1, String s2)
  {
    String packet="GA"+actionID+";"+s0;
    if(!s1.equals(""))
      packet+=";"+s1;
    if(!s2.equals(""))
      packet+=";"+s2;

    send(out,packet);

  }

  public static void GAME_SEND_GA_PACKET_TO_FIGHT(Fight fight, int teams, int gameActionID, String s1, String s2, String s3)
  {
    String packet="GA"+gameActionID+";"+s1+";"+s2+";"+s3;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_GAMEACTION_TO_FIGHT(Fight fight, int teams, String packet)
  {
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_GAF_PACKET_TO_FIGHT(Fight fight, int teams, int i1, int guid)
  {
    String packet="GAF"+i1+"|"+guid;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_BN(Player out)
  {
    String packet="BN";
    send(out,packet);

  }

  public static void GAME_SEND_BN(GameClient out)
  {
    String packet="BN";
    send(out,packet);

  }

  public static void GAME_SEND_GAMETURNSTOP_PACKET_TO_FIGHT(Fight fight, int teams, int guid)
  {
    String packet="GTF"+guid;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;

      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_GTR_PACKET_TO_FIGHT(Fight fight, int teams, int guid)
  {
    String packet="GTR"+guid;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_EMOTICONE_TO_MAP(GameMap map, int guid, int id)
  {
    String packet="cS"+guid+"|"+id;
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_SPELL_UPGRADE_FAILED(GameClient _out)
  {
    String packet="SUE";
    send(_out,packet);

  }

  public static void GAME_SEND_SPELL_UPGRADE_SUCCED(GameClient _out, int spellID, int level)
  {
    String packet="SUK"+spellID+"~"+level;
    send(_out,packet);

  }

  public static void GAME_SEND_SPELL_LIST(Player perso)
  {
    String packet=perso.parseSpellList();
    send(perso,packet);

  }
  public static void GAME_SEND_SPELL_LIST_ONE_WINDOWS(Player perso1 , Player perso2)
  {
    String packet=perso1.parseSpellList();
    send(perso2,packet);

  }
  public static void GAME_SEND_SPELL_LIST_CONTROL(Player perso1 , String sort)
  {
    send(perso1,sort);

  }

  public static void GAME_SEND_FIGHT_PLAYER_DIE_TO_FIGHT(Fight fight, int teams, int guid)
  {
    String packet="GA;103;"+guid+";"+guid;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft()||f.getPersonnage()==null)
        continue;
      if(f.getPersonnage().isOnline())
        send(f.getPersonnage(),packet);
    }
  }

  public static void GAME_SEND_FIGHT_GIE_TO_FIGHT(Fight fight, int teams, int mType, int cible, int value, String mParam2, String mParam3, String mParam4, int turn, int spellID)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("GIE").append(mType).append(";").append(cible).append(";").append(value).append(";").append(mParam2).append(";").append(mParam3).append(";").append(mParam4).append(";").append(turn).append(";").append(spellID);
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft()||f.getPersonnage()==null)
        continue;
      if(f.getPersonnage().isOnline())
      {
        send(f.getPersonnage(),packet.toString());
      }
    }
  }

  public static void GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(Fight fight, int teams, GameMap map)
  {
    String packet=map.getFightersGMsPackets(fight);
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }
  }

  public static void GAME_SEND_MAP_FIGHT_GMS_PACKETS(Fight fight, GameMap map, Player _perso)
  {
    String packet=map.getFightersGMsPackets(fight);
    send(_perso,packet);

  }

  public static void GAME_SEND_FIGHT_PLAYER_JOIN(Fight fight, int teams, Fighter _fighter)
  {
    String packet=_fighter.getGmPacket('+',true);

    for(Fighter f : fight.getFighters(teams))
    {
      if(f!=_fighter)
      {
        if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
          continue;
        if(f.getPersonnage()!=null&&f.getPersonnage().getGameClient()!=null)
        {
          send(f.getPersonnage(),packet);
        }
      }
    }

  }

  public static void GAME_SEND_cMK_PACKET(Player perso, String suffix, int guid, String name, String msg)
  {
    String packet="cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
    send(perso,packet);

  }

  public static void GAME_SEND_FIGHT_LIST_PACKET(GameClient out, GameMap map)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("fL");
    for(Fight entry : map.getFights())
    {
      if(packet.length()>2)
        packet.append("|");
      packet.append(entry.parseFightInfos());
    }
    send(out,packet.toString());

  }

  public static void GAME_SEND_cMK_PACKET_TO_MAP(GameMap map, String suffix, int guid, String name, String msg)
  {
    String packet="cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
    for(Player z : map.getPlayers())
      send(z,packet);
  }

  public static void GAME_SEND_cMK_PACKET_TO_GUILD(Guild g, String suffix, int guid, String name, String msg)
  {
    String packet="cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
    for(Player perso : g.getOnlineMembers())
    {
      if(!perso.isOnline())
        perso.setOnline(true);
      send(perso,packet);
    }
  }

  public static void GAME_SEND_cMK_PACKET_TO_ALL(Player perso, String suffix, int guid, String name, String msg)
  {
    String packet="cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
    if(perso.getLevel()<6)
    {
      SocketManager.GAME_SEND_MESSAGE(perso,"Cette chaéne ne peut étre utilisée qu'é partir du niveau <b> 6 </b> et plus.");
      GAME_SEND_BN(perso);
      return;
    }
    for(Player perso1 : Main.world.getOnlinePlayers())
      send(perso1,packet);
  }
  public static void GAME_SEND_cMK_PACKET_TO_ALL_commande_all(Player perso, String suffix, int guid, String name, String msg)
  {
    String packet="cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
    if(perso.getLevel()<6)
    {
      SocketManager.GAME_SEND_MESSAGE(perso,"Cette chaéne ne peut étre utilisée qu'é partir du niveau <b> 6 </b> et plus.");
      GAME_SEND_BN(perso);
      return;
    }
    for(Player perso1 : Main.world.getOnlinePlayers()) {
    	if(perso1.noall)
    		continue;
      send(perso1,packet);
    }
  }

  public static void GAME_SEND_cMK_PACKET_TO_ALIGN(String suffix, int guid, String name, String msg, Player _perso)
  {
    String packet="cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
    for(Player perso : Main.world.getOnlinePlayers())
    {
      if(perso.get_align()==_perso.get_align())
      {
        send(perso,packet);
      }
    }
  }

  public static void GAME_SEND_cMK_PACKET_TO_ADMIN(String suffix, int guid, String name, String msg)
  {
    String packet="cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
    for(Player perso : Main.world.getOnlinePlayers())
      if(perso.isOnline())
        if(perso.getAccount()!=null)
          if(perso.getGroupe()!=null)
            send(perso,packet);
  }
  public static void GAME_SEND_cMK_PACKET_TO_ADMINV2(String suffix, int guid, String name, String msg)
  {
    String packet="cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
    for(Player perso : Main.world.getOnlinePlayers())
      if(perso.isOnline())
        if(perso.getAccount()!=null)
          if(perso.getGroupe()!=null)
        	  if(perso.getGroupe().getId() >= 5)
            send(perso,packet);
  }
  public static void GAME_SEND_cMK_PACKET_TO_ADMINV3( String msg)
  {
    for(Player perso : Main.world.getOnlinePlayers())
      if(perso.isOnline())
        if(perso.getAccount()!=null)
          if(perso.getGroupe()!=null)
        	  if(perso.getGroupe().getId() >= 2)
        		  perso.sendMessage(msg);
  }

  public static void GAME_SEND_cMK_PACKET_TO_FIGHT(Fight fight, int teams, String suffix, int guid, String name, String msg)
  {
    String packet="cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_GDZ_PACKET_TO_FIGHT(Fight fight, int teams, String suffix, int cell, int size, int unk)
  {
    String packet="GDZ"+suffix+cell+";"+size+";"+unk;

    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_GDC_PACKET_TO_FIGHT(Fight fight, int teams, int cell)
  {
    String packet="GDC"+cell;

    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_GA2_PACKET(GameClient out, int guid)
  {
    String packet="GA;2;"+guid+";";
    send(out,packet);

  }

  public static void GAME_SEND_CHAT_ERROR_PACKET(GameClient out, String name)
  {
    String packet="cMEf"+name;
    send(out,packet);

  }

  public static void GAME_SEND_eD_PACKET_TO_MAP(GameMap map, int guid, int dir)
  {
    String packet="eD"+guid+"|"+dir;
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_ECK_PACKET(Player out, int type, String str)
  {
    String packet="ECK"+type;
    if(!str.isEmpty())
      packet+="|"+str;
    send(out,packet);
  }

  public static void GAME_SEND_ECK_PACKET(GameClient out, int type, String str)
  {
    String packet="ECK"+type;
    if(!str.equals(""))
      packet+="|"+str;
    send(out,packet);

  }

  public static void GAME_SEND_ITEM_VENDOR_LIST_PACKET(GameClient out, Npc npc)
  {
    String packet="EL"+npc.getTemplate().getItemVendorList(out.getPlayer());
    send(out,packet);

  }

  public static void GAME_SEND_ITEM_LIST_PACKET_PERCEPTEUR(GameClient out, Collector perco)
  {
    String packet="EL"+perco.getItemCollectorList();
    send(out,packet);

  }

  public static void GAME_SEND_ITEM_LIST_PACKET_SELLER(Player p, Player out)
  {
    String packet="EL"+p.parseStoreItemsList();
    send(out,packet);

  }

  public static void GAME_SEND_EV_PACKET(GameClient out)
  {
    String packet="EV";
    send(out,packet);
  }

  public static void GAME_SEND_DCK_PACKET(GameClient out, int id)
  {
    String packet="DCK"+id;
    send(out,packet);

  }

  public static void GAME_SEND_QUESTION_PACKET(GameClient out, String str)
  {
    String packet="DQ"+str;
    send(out,packet);

  }

  public static void GAME_SEND_END_DIALOG_PACKET(GameClient out)
  {
    String packet="DV";
    send(out,packet);

  }

  public static void GAME_SEND_CONSOLE_MESSAGE_PACKET(GameClient out, String mess)
  {
    String packet="BAT2"+mess;
    send(out,packet);

  }

  public static void GAME_SEND_BUY_ERROR_PACKET(GameClient out)
  {
    String packet="EBE";
    send(out,packet);

  }

  public static void GAME_SEND_SELL_ERROR_PACKET(GameClient out)
  {
    String packet="ESE";
    send(out,packet);

  }

  public static void GAME_SEND_BUY_OK_PACKET(GameClient out)
  {
    String packet="EBK";
    send(out,packet);

  }

  public static void GAME_SEND_OBJECT_QUANTITY_PACKET(Player out, GameObject obj)
  {
    String packet="OQ"+obj.getGuid()+"|"+obj.getQuantity();
    send(out,packet);
  }

  public static void GAME_SEND_OAKO_PACKET(Player out, GameObject obj)
  {
    String packet="OAKO"+obj.parseItem();
    send(out,packet);
  }


  public static void SEND_OAKO_PACKET(GameClient out, GameObject obj)
  {
    String packet="OAKO"+obj.parseItem();
    send(out,packet);

  }

  public static void GAME_SEND_ESK_PACKEt(Player out)
  {
    String packet="ESK";
    send(out,packet);

  }

  public static void GAME_SEND_REMOVE_ITEM_PACKET(Player out, int guid)
  {
    String packet="OR"+guid;
    send(out,packet);

  }

  public static void GAME_SEND_DELETE_OBJECT_FAILED_PACKET(GameClient out)
  {
    String packet="OdE";
    send(out,packet);

  }

  public static void GAME_SEND_OBJET_MOVE_PACKET(Player out, GameObject obj)
  {
    String packet="OM"+obj.getGuid()+"|";
    if(obj.getPosition()!=Constant.ITEM_POS_NO_EQUIPED)
      packet+=obj.getPosition();

    send(out,packet);

  }

  public static void GAME_SEND_DELETE_STATS_ITEM_FM(Player perso, int id)
  {
    String packet="OR"+id;
    send(perso,packet);

  }

  public static void GAME_SEND_EMOTICONE_TO_FIGHT(Fight fight, int teams, int guid, int id)
  {
    String packet="cS"+guid+"|"+id;
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_OAEL_PACKET(GameClient out)
  {
    String packet="OAEL";
    send(out,packet);

  }

  public static void GAME_SEND_NEW_LVL_PACKET(GameClient out, int lvl)
  {
    String packet="AN"+lvl;
    send(out,packet);

  }

  public static void GAME_SEND_MESSAGE_TO_ALL(String msg, String color)
  {
    String packet="cs<font color='#"+color+"'>"+msg+"</font>";
    for(Player P : Main.world.getOnlinePlayers())
      send(P,packet);
  }

  public static void GAME_SEND_EXCHANGE_REQUEST_OK(GameClient out, int guid, int guidT, int msgID)
  {
    String packet="ERK"+guid+"|"+guidT+"|"+msgID;
    send(out,packet);

  }

  public static void GAME_SEND_EXCHANGE_REQUEST_ERROR(GameClient out, char c)
  {
    String packet="ERE"+c;
    send(out,packet);

  }

  public static void GAME_SEND_EXCHANGE_CONFIRM_OK(GameClient out, int type)
  {
    String packet="ECK"+type;
    send(out,packet);

  }

  public static void GAME_SEND_EXCHANGE_MOVE_OK(Player out, char type, String signe, String s1)
  {
    String packet="EMK"+type+signe;
    if(!s1.equals(""))
      packet+=s1;
    send(out,packet);

  }

  public static void GAME_SEND_EXCHANGE_MOVE_OK_FM(Player out, char type, String signe, String s1)
  {
    String packet="EmK"+type+signe;
    if(!s1.equals(""))
      packet+=s1;
    send(out,packet);

  }

  public static void GAME_SEND_EXCHANGE_OTHER_MOVE_OK(GameClient out, char type, String signe, String s1)
  {
    String packet="EmK"+type+signe;
    if(!s1.equals(""))
      packet+=s1;
    send(out,packet);

  }

  public static void GAME_SEND_EXCHANGE_OTHER_MOVE_OK_FM(GameClient out, char type, String signe, String s1)
  {
    String packet="EMK"+type+signe;
    if(!s1.equals(""))
      packet+=s1;
    send(out,packet);

  }

  public static void GAME_SEND_EXCHANGE_OK(GameClient out, boolean ok, int guid)
  {
    String packet="EK"+(ok ? "1" : "0")+guid;
    send(out,packet);
  }

  public static void GAME_SEND_EXCHANGE_OK(GameClient out, boolean ok)
  {
    String str="EK"+(ok ? "1" : "0");
    send(out,str);
  }

  public static void GAME_SEND_EXCHANGE_VALID(GameClient out, char c)
  {
    String packet="EV"+c;
    send(out,packet);
  }

  public static void GAME_SEND_STAKE_VALID(GameClient out, char c)
  {
    String packet="EV"+c;
    send(out,packet);
  }

  public static void GAME_SEND_GROUP_INVITATION_ERROR(GameClient out, String s)
  {
    String packet="PIE"+s;
    send(out,packet);

  }

  public static void GAME_SEND_GROUP_INVITATION(GameClient out, String n1, String n2)
  {
    String packet="PIK"+n1+"|"+n2;
    send(out,packet);

  }

  public static void GAME_SEND_GROUP_CREATE(GameClient out, Party g)
  {
    String packet="PCK"+g.getChief().getName();
    send(out,packet);

  }

  public static void GAME_SEND_PL_PACKET(GameClient out, Party g)
  {
    String packet="PL"+g.getChief().getId();
    send(out,packet);

  }

  public static void GAME_SEND_PR_PACKET(Player out)
  {
    String packet="PR";
    send(out,packet);

  }

  public static void GAME_SEND_PV_PACKET(GameClient out, String s)
  {
    String packet="PV"+s;
    send(out,packet);

  }

  public static void GAME_SEND_ALL_PM_ADD_PACKET(GameClient out, Party g)
  {
	  try
	  {
    StringBuilder packet=new StringBuilder();
    packet.append("PM+");
    boolean first=true;
    for(Player p : g.getPlayers())
    {
      if(!first)
        packet.append("|");
      packet.append(p.parseToPM());
      first=false;
    }
    send(out,packet.toString());
  }
  catch(Exception e)
  {
  }
  }

  public static void GAME_SEND_PM_ADD_PACKET_TO_GROUP(Party g, Player p)
  {
	  try
	  {
 
    String packet="PM+"+p.parseToPM();
    for(Player P : g.getPlayers())
      send(P,packet);
  }
  catch(Exception e)
  {
  }
 
  }

  public static void GAME_SEND_PM_MOD_PACKET_TO_GROUP(Party g, Player p)
  {
	  try
	  {
    String packet="PM~"+p.parseToPM();
    for(Player P : g.getPlayers())
      send(P,packet);
	  }
	    catch(Exception e)
	    {
	    	
	    }
  }

  public static void GAME_SEND_PM_DEL_PACKET_TO_GROUP(Party party, int guid)
  {
	  try
	  {
	 
    String packet="PM-"+guid;
    Iterator<Player> iterator=party.getPlayers().iterator();
    while(iterator.hasNext())
    	 send(iterator.next(),packet);
	  }
    catch(Exception e)
    {
    }
  }

  public static void GAME_SEND_cMK_PACKET_TO_GROUP(Party g, String s, int guid, String name, String msg)
  {
    String packet="cMK"+s+"|"+guid+"|"+name+"|"+msg+"|";
    for(Player P : g.getPlayers())
      send(P,packet);

  }

  public static void GAME_SEND_FIGHT_DETAILS(GameClient out, Fight fight)
  {
    if(fight==null)
      return;
    StringBuilder packet=new StringBuilder();
    packet.append("fD").append(fight.getId()).append("|");
    fight.getFighters(1).stream().filter(f -> !f.isInvocation()).forEach(f -> packet.append(f.getPacketsName()).append("~").append(f.getLvl()).append(";"));
    packet.append("|");
    fight.getFighters(2).stream().filter(f -> !f.isInvocation()).forEach(f -> packet.append(f.getPacketsName()).append("~").append(f.getLvl()).append(";"));
    send(out,packet.toString());

  }

  public static void GAME_SEND_IQ_PACKET(Player perso, int guid, int qua)
  {
    String packet="IQ"+guid+"|"+qua;
    send(perso,packet);

  }

  public static void GAME_SEND_JN_PACKET(Player perso, int jobID, int lvl)
  {
    String packet="JN"+jobID+"|"+lvl;
    send(perso,packet);

  }

  public static void GAME_SEND_GDF_PACKET_TO_MAP(GameMap map, GameCase cell)
  {
    int cellID=cell.getId();
    InteractiveObject object=cell.getObject();
    String packet="GDF|"+cellID+";"+object.getState()+";"+(object.isInteractive() ? "1" : "0");
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_GDF_PACKET_TO_FIGHT(Player player, Collection<GameCase> collection)
  {
    String packet="GDF|";
    for(GameCase cell : collection)
    {
      if(cell.getObject()==null)
        continue;
      if(cell.getObject().getTemplate()==null)
        continue;

      switch(cell.getObject().getTemplate().getId())
      {
        case 7515:
        case 7511:
        case 7517:
        case 7512:
        case 7513:
        case 7516:
        case 7550:
        case 7518:
        case 7534:
        case 7535:
        case 7533:
        case 7551:
        case 7500:
        case 7536:
        case 7501:
        case 7502:
        case 7503:
        case 7542:
        case 7541:
        case 7504:
        case 7553:
        case 7505:
        case 7506:
        case 7507:
        case 7557:
        case 7554:
        case 7508:
        case 7509:
        case 7552:
          packet+=cell.getId()+";1;0|";
          break;
      }
    }
    send(player,packet);
  }

  public static void GAME_SEND_GA_PACKET_TO_MAP(GameMap map, String gameActionID, int actionID, String s1, String s2)
  {
    String packet="GA"+gameActionID+";"+actionID+";"+s1;
    if(!s2.equals(""))
      packet+=";"+s2;

    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_EL_BANK_PACKET(Player perso)
  {
    String packet="EL"+perso.parseBankPacket();
    send(perso,packet);

  }

  public static void GAME_SEND_EL_TRUNK_PACKET(Player perso, Trunk t)
  {
    String packet="EL"+t.parseToTrunkPacket();
    send(perso,packet);

  }

  public static void GAME_SEND_JX_PACKET(Player perso, ArrayList<JobStat> SMs)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("JX");
    for(JobStat sm : SMs)
      packet.append("|").append(sm.getTemplate().getId()).append(";").append(sm.get_lvl()).append(";").append(sm.getXpString(";")).append(";");
    send(perso,packet.toString());
  }

  public static void GAME_SEND_JO_PACKET(Player perso, ArrayList<JobStat> JobStats)
  {
    for(JobStat SM : JobStats)
    {
      String packet="JO"+SM.getPosition()+"|"+SM.getOptBinValue()+"|"+SM.getSlotsPublic();
      send(perso,packet);
    }
  }

  public static void GAME_SEND_JO_PACKET(Player perso, JobStat SM)
  {
    String packet="JO"+SM.getPosition()+"|"+SM.getOptBinValue()+"|"+SM.getSlotsPublic();
    send(perso,packet);
  }

  public static void GAME_SEND_JS_PACKET(Player perso, ArrayList<JobStat> SMs)
  {
    String packet="JS";
    for(JobStat sm : SMs)
    {
      packet+=sm.parseJS();
    }
    send(perso,packet);

  }

  public static void GAME_SEND_EsK_PACKET(Player perso, String str)
  {
    String packet="EsK"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_FIGHT_SHOW_CASE(ArrayList<GameClient> PWs, int guid, int cellID)
  {
    String packet="Gf"+guid+"|"+cellID;
    for(GameClient PW : PWs)
    {
      send(PW,packet);
    }

  }

  public static void GAME_SEND_Ea_PACKET(Player perso, String str)
  {
    String packet="Ea"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_EA_PACKET(Player perso, String str)
  {
    String packet="EA"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_Ec_PACKET(Player perso, String str)
  {
    String packet="Ec"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_Em_PACKET(Player perso, String str)
  {
    String packet="Em"+str;
    send(perso,packet);
  }

  public static void GAME_SEND_IO_PACKET_TO_MAP(GameMap map, int guid, String str)
  {
    String packet="IO"+guid+"|"+str;
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_FRIENDLIST_PACKET(Player perso)
  {
    String packet="FL"+perso.getAccount().parseFriendList();
    send(perso,packet);
    if(perso.getWife()!=0)
    {
      String packet2="FS"+perso.get_wife_friendlist();
      send(perso,packet2);
    }
  }

  public static void GAME_SEND_FRIEND_ONLINE(Player friend, Player perso)
  {
    String packet="Im0143;"+friend.getAccount().getPseudo()+" (<b><a href='asfunction:onHref,ShowPlayerPopupMenu,"+friend.getName()+"'>"+friend.getName()+"</a></b>)";
    send(perso,packet);

  }

  public static void GAME_SEND_FA_PACKET(Player perso, String str)
  {
    String packet="FA"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_FD_PACKET(Player perso, String str)
  {
    String packet="FD"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_Rp_PACKET(Player perso, MountPark MP)
  {
    StringBuilder packet=new StringBuilder();
    if(MP==null)
      return;

    packet.append("Rp").append(MP.getOwner()).append(";").append(MP.getPrice()).append(";").append(MP.getSize()).append(";").append(MP.getMaxObject()).append(";");

    Guild G=MP.getGuild();
    //Si une guilde est definie
    if(G!=null)
    {
      packet.append(G.getName()).append(";").append(G.getEmblem());
    }
    else
    {
      packet.append(";");
    }

    send(perso,packet.toString());

  }

  public static void GAME_SEND_OS_PACKET(Player perso, int pano)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("OS");
    int num=perso.getNumbEquipedItemOfPanoplie(pano);
    if(num<=0)
      packet.append("-").append(pano);
    else
    {
      packet.append("+").append(pano).append("|");
      ObjectSet IS=Main.world.getItemSet(pano);
      if(IS!=null)
      {
        StringBuilder items=new StringBuilder();
        //Pour chaque objet de la pano
        for(ObjectTemplate OT : IS.getItemTemplates())
        {
          //Si le joueur l'a équipé
          if(perso.hasEquiped(OT.getId()))
          {
            //On l'ajoute au packet
            if(items.length()>0)
              items.append(";");
            items.append(OT.getId());
          }
        }
        packet.append(items.toString()).append("|").append(IS.getBonusStatByItemNumb(num).parseToItemSetStats());
      }
    }
    send(perso,packet.toString());

  }

  public static void GAME_SEND_MOUNT_DESCRIPTION_PACKET(Player perso, Mount DD)
  {
    String packet="Rd"+DD.parse();

    send(perso,packet);

  }

  public static void GAME_SEND_Rr_PACKET(Player perso, String str)
  {
    String packet="Rr"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_ALTER_GM_PACKET(GameMap map, Player perso)
  {
    String packet="GM|~"+perso.parseToGM();
    for(Player z : map.getPlayers())
    {
      if(perso.get_size()>0)
        send(z,packet);
      else if(z.getGroupe()!=null)
        send(z,packet);
    }
  }

  public static void GAME_SEND_Ee_PACKET(Player perso, char c, String s)
  {
    String packet="Ee"+c+s;
    send(perso,packet);

  }
  public static void GAME_SEND_Ee_PACKET_WAIT(Player perso, char c, String s)
  {
    String packet="Ee"+c+s;
    send(perso,packet);

  }
  public static void GAME_SEND_cC_PACKET(Player perso, char c, String s)
  {
    String packet="cC"+c+s;
    send(perso,packet);

  }

  public static void GAME_SEND_ADD_NPC_TO_MAP(GameMap map, Npc npc)
  {
    for(Player z : map.getPlayers())
      send(z,"GM|"+npc.parse(false,z));
  }

  public static void GAME_SEND_ADD_NPC(Player player, Npc npc)
  {
    send(player,"GM|"+npc.parse(false,player));
  }

  public static void GAME_SEND_ADD_PERCO_TO_MAP(GameMap map)
  {
    String packet="GM|"+Collector.parseGM(map);
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_GDO_PACKET_TO_MAP(GameMap map, char c, int cell, int itm, int i)
  {
    String packet="GDO"+c+cell+";"+itm+";"+i;
    for(Player z : map.getPlayers())
      send(z,packet);

  }

  public static void GAME_SEND_GDO_PACKET(Player p, char c, int cell, int itm, int i)
  {
    String packet="GDO"+c+cell+";"+itm+";"+i;
    send(p,packet);

  }

  public static void GAME_SEND_ZC_PACKET(Player p, int a)
  {
    String packet="ZC"+a;
    send(p,packet);

  }

  public static void GAME_SEND_GIP_PACKET(Player p, int a)
  {
    String packet="GIP"+a;
    send(p,packet);

  }

  public static void GAME_SEND_gn_PACKET(Player p)
  {
    String packet="gn";
    send(p,packet);

  }

  public static void GAME_SEND_gC_PACKET(Player p, String s)
  {
    String packet="gC"+s;
    send(p,packet);

  }

  public static void GAME_SEND_gV_PACKET(Player p)
  {
    String packet="gV";
    send(p,packet);

  }

  public static void GAME_SEND_gIM_PACKET(Player p, Guild g, char c)
  {
	  if(g == null)
		  return;
    String packet="gIM"+c;
    switch(c)
    {
      case '+':
        packet+=g.parseMembersToGM();
        break;
    }
    send(p,packet);

  }

  public static void GAME_SEND_gIB_PACKET(Player p, String infos)
  {
    String packet="gIB"+infos;
    send(p,packet);

  }

  public static void GAME_SEND_gIH_PACKET(Player p, String infos)
  {
    String packet="gIH"+infos;
    send(p,packet);

  }

  public static void GAME_SEND_gS_PACKET(Player p, GuildMember gm)
  {
    send(p,"gS"+gm.getGuild().getName()+"|"+gm.getGuild().getEmblem().replace(',','|')+"|"+gm.parseRights());
  }

  public static void GAME_SEND_gJ_PACKET(Player p, String str)
  {
    String packet="gJ"+str;
    send(p,packet);

  }

  public static void GAME_SEND_gK_PACKET(Player p, String str)
  {
    String packet="gK"+str;
    send(p,packet);

  }

  public static void GAME_SEND_gIG_PACKET(Player p, Guild g)
  {
	  if(g == null)
		  return;
    long xpMin=Main.world.getExpLevel(g.getLvl()).guilde;
    long xpMax;
    if(Main.world.getExpLevel(g.getLvl()+1)==null)
    {
      xpMax=-1;
    }
    else
    {
      xpMax=Main.world.getExpLevel(g.getLvl()+1).guilde;
    }
    send(p,"gIG"+(g.haveTenMembers() ? 1 : 0)+"|"+g.getLvl()+"|"+xpMin+"|"+g.getXp()+"|"+xpMax);

  }

  public static void REALM_SEND_MESSAGE(GameClient out, String args)
  {
    String packet="M"+args;
    send(out,packet);

  }

  public static void GAME_SEND_WC_PACKET(Player perso)
  {
    String packet="WC"+perso.parseZaapList();
    send(perso.getGameClient(),packet);

  }

  public static void GAME_SEND_WV_PACKET(Player out)
  {
    String packet="WV";
    send(out,packet);

  }

  public static void GAME_SEND_ZAAPI_PACKET(Player perso, String list)
  {
    String packet="Wc"+perso.getCurMap().getId()+"|"+list;
    send(perso,packet);

  }

  public static void GAME_SEND_CLOSE_ZAAPI_PACKET(Player out)
  {
    String packet="Wv";
    send(out,packet);

  }

  public static void GAME_SEND_WUE_PACKET(Player out)
  {
    String packet="WUE";
    send(out,packet);

  }

  public static void GAME_SEND_EMOTE_LIST(Player perso, String s)
  {
    send(perso,"eL"+s);
  }

  public static void GAME_SEND_NO_EMOTE(Player out)
  {
    String packet="eUE";
    send(out,packet);

  }

  public static void REALM_SEND_TOO_MANY_PLAYER_ERROR(GameClient out)
  {
    String packet="AlEw";
    send(out,packet);

  }

  public static void REALM_SEND_REQUIRED_APK(GameClient out) //FIXME:Générateur de nom
  {
    String pass="";
    String noms="fantasy;mr;beau;fort;dark;knight;sword;big;boss;chuck;norris;wood;rick;roll;food;play;volt;rick;ven;bana;sam;ron;fou;pui;to;fu;lo;rien;bank;cap;chap;fort;dou;soleil;gentil;mechant;bad;killer;fight;gra;evil;dark;jerry;fatal;haut;bas;arc;epe;cac;ec;mai;invo;tro;com;koi;bou;let;top;fun;fai;sony;kani;meulou;faur;asus;choa;chau;cho;miel;beur;pain;cry;big;sma;to;day;bi;cih;geni;bou;che;scania;dave;swi;cas;que;chi;er;de;nul;do;a;b;c;d;e;f;g;h;i;j;k;l;m;n;o;p;q;r;s;t;u;v;w;x;y;z;a;e;i;o;u;y";
    String str[]=noms.split(";");
    String rep="";
    int tiree=0;
    int maxi=(int)Math.floor(Math.random()*4D)+2;
    for(int x=0;x<maxi;x++)
    {
      rep=(new StringBuilder(String.valueOf(rep))).append(str[(int)Math.floor(Math.random()*str.length)]).toString();
      if(maxi>=3&&x==0&&tiree==0&&(int)Math.floor(Math.random()*2D)==1)
      {
        rep=(new StringBuilder(String.valueOf(rep))).append("-").toString();
        tiree=1;
      }
    }

    rep=(new StringBuilder(String.valueOf(rep.substring(0,1).toUpperCase()))).append(rep.substring(1)).toString();
    pass=rep;
    String packet=(new StringBuilder("APK")).append(pass).toString();
    send(out,packet);
  }

  public static void GAME_SEND_ADD_ENEMY(Player out, Player pr)
  {

    String packet="iAK"+pr.getAccount().getName()+";2;"+pr.getName()+";36;10;0;100.FL.";
    send(out,packet);

  }

  public static void GAME_SEND_iAEA_PACKET(Player out)
  {

    String packet="iAEA.";
    send(out,packet);

  }

  public static void GAME_SEND_ENEMY_LIST(Player perso)
  {

    String packet="iL"+perso.getAccount().parseEnemyList();
    send(perso,packet);

  }

  public static void GAME_SEND_iD_COMMANDE(Player perso, String str)
  {
    String packet="iD"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_BWK(Player perso, String str)
  {
    String packet="BWK"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_KODE(Player perso, String str)
  {
    String packet="K"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_hOUSE(Player perso, String str)
  {
    String packet="h"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_FORGETSPELL_INTERFACE(char sign, Player perso)
  {
    String packet="SF"+sign;
    send(perso,packet);
  }

  public static void GAME_SEND_R_PACKET(Player perso, String str)
  {
    String packet="R"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_gIF_PACKET(Player perso, String str)
  {
    String packet="gIF"+str;
    send(perso,packet);
  }

  public static void GAME_SEND_gITM_PACKET(Player perso, String str)
  {
    String packet="gITM"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_gITp_PACKET(Player perso, String str)
  {
    String packet="gITp"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_gITP_PACKET(Player perso, String str)
  {
    String packet="gITP"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_IH_PACKET(Player perso, String str)
  {
    String packet="IH"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_FLAG_PACKET(Player perso, Player cible)
  {
    String packet="IC"+cible.getCurMap().getX()+"|"+cible.getCurMap().getY();
    send(perso,packet);

  }

  public static void GAME_SEND_FLAG_PACKET(Player perso, GameMap CurMap)
  {
    String packet="IC"+CurMap.getX()+"|"+CurMap.getY();
    send(perso,packet);

  }

  public static void GAME_SEND_DELETE_FLAG_PACKET(Player perso)
  {
    String packet="IC|";
    send(perso,packet);

  }

  public static void GAME_SEND_gT_PACKET(Player perso, String str)
  {
    String packet="gT"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_GUILDHOUSE_PACKET(Player perso)
  {
    String packet="gUT";
    send(perso,packet);

  }

  public static void GAME_SEND_GUILDENCLO_PACKET(Player perso)
  {
    String packet="gUF";
    send(perso,packet);

  }

  /**
   * HDV *
   */
  public static void GAME_SEND_EHm_PACKET(Player out, String sign, String str)
  {
    String packet="EHm"+sign+str;

    send(out,packet);

  }

  public static void GAME_SEND_EHM_PACKET(Player out, String sign, String str)
  {
    String packet="EHM"+sign+str;

    send(out,packet);

  }

  public static void GAME_SEND_EHP_PACKET(Player out, int templateID) //Packet d'envoie du prix moyen du template (En réponse a un packet EHP)
  {
   if(Main.world.getObjTemplate(templateID) == null)
	   return;
    String packet="EHP"+templateID+"|"+Main.world.getObjTemplate(templateID).getAvgPrice();

    send(out,packet);

  }

  public static void GAME_SEND_EHl(Player out, Hdv seller, int templateID)
  {
    if(seller==null)
      return;
    String packet="EHl"+seller.parseToEHl(templateID);

    send(out,packet);

  }

  public static void GAME_SEND_EHL_PACKET(Player out, int categ, String templates) //Packet de listage des templates dans une catégorie (En réponse au packet EHT)
  {
    String packet="EHL"+categ+"|"+templates;

    send(out,packet);

  }

  public static void GAME_SEND_EHL_PACKET(Player out, String items) //Packet de listage des objets en vente
  {
    String packet="EHL"+items;

    send(out,packet);

  }

  //v2.7 - Replaced String += with StringBuilder
  //v2.8 - World Market
  public static void GAME_SEND_HDVITEM_SELLING(Player perso)
  {
    StringBuilder packet=new StringBuilder("EL");
    HdvEntry[] entries;
   // if(perso.getWorldMarket())
      entries=perso.getAccount().getHdvEntries(Config.getInstance().worldMarket); //Récupére un tableau de tout les items que le personnage é en vente dans l'HDV oé il est
   // else
   //   entries=perso.getAccount().getHdvEntries(Math.abs(((Integer)perso.getExchangeAction().getValue()))); //Récupére un tableau de tout les items que le personnage é en vente dans l'HDV oé il est
    boolean isFirst=true;
    for(HdvEntry curEntry : entries)
    {
    	// new code
      if(curEntry==null)
      continue;	  
      if(curEntry.gameObject == null)
      continue;	  
      if(!isFirst)
        packet.append("|");
      packet.append(curEntry.parseToEL());
      isFirst=false;
    }
    send(perso,packet.toString());
  }

  public static void GAME_SEND_WEDDING(GameMap c, int action, int homme, int femme, int parlant)
  {
    String packet="GA;"+action+";"+homme+";"+homme+","+femme+","+parlant;
    Player Homme=Main.world.getPlayer(homme);
    send(Homme,packet);

  }

  public static void GAME_SEND_PF(Player perso, String str)
  {
    String packet="PF"+str;
    send(perso,packet);

  }

  public static void GAME_SEND_MERCHANT_LIST(Player P, short mapID)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("GM|");
    if(Main.world.getSeller(P.getCurMap().getId())==null)
      return;
    for(Integer pID : Main.world.getSeller(P.getCurMap().getId()))
    {
    	if(Main.world.getPlayer(pID) == null)
    		continue;
      if(!Main.world.getPlayer(pID).isOnline()&&Main.world.getPlayer(pID).isShowSeller())
      {
        packet.append("~").append(Main.world.getPlayer(pID).parseToMerchant()).append("|");
      }
    }
    if(packet.length()<5)
      return;
    send(P,packet.toString());

  }

  public static void GAME_SEND_PACKET_TO_FIGHT(Fight fight, int i, String packet)
  {
    for(Fighter f : fight.getFighters(i))
    {
      if(f.hasLeft())
        continue;
      if(f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(Fight fight, int teams, int state, int cancelBtn, int duel, int spec, long time, int type)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("GJK").append(state).append("|");
    packet.append(cancelBtn).append("|").append(duel).append("|");
    packet.append(spec).append("|").append(time).append("|").append(type);
    for(Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft())
        continue;
      send(f.getPersonnage(),packet.toString());
    }

  }

  public static void GAME_SEND_GJK_PACKET(Player out, int state, int cancelBtn, int duel, int spec, long time, int unknown)
  {
    send(out,"GJK"+state+"|"+cancelBtn+"|"+duel+"|"+spec+"|"+time+"|"+unknown);

  }

  public static void GAME_SEND_cMK_PACKET_INCARNAM_CHAT(Player perso, String suffix, int guid, String name, String msg)
  {
    String packet="cMK"+suffix+"|"+guid+"|"+name+"|"+msg;
    /*if(perso.getLevel()>15&&perso.getGroupe()==null)
    {
      GAME_SEND_BN(perso);
      return;
    }*/
    for(Player player : Main.world.getOnlinePlayers())

      //if(player.getCurMap()!=null&&player.getCurMap().getSubArea()!=null&&player.getCurMap().getSubArea().getArea()!=null&&player.getCurMap().getSubArea().getArea().getId()==45)
      send(player,packet);
  }

  public static void GAME_SEND_Ag_PACKET(GameClient out, int idObjet, String codObjet)
  {
    String packet="Ag1|"+idObjet+"|Gift| You've just received a nice gift! "+"Hopefully it will help a young adventurer such as yourself along your way! "+"Good luck with the rest of your journey! |DOFUS|"+codObjet;
    send(out,packet);

  }

  public static void SEND_Ej_LIVRE(Player pj, String str)
  {
    String packet="Ej"+str;
    send(pj,packet);

  }

  public static void SEND_EW_METIER_PUBLIC(Player pj, String str)
  {
    String packet="EW"+str;
    send(pj,packet);

  }

  public static void SEND_EJ_LIVRE(Player pj, String str)
  {
    String packet="EJ"+str;
    send(pj,packet);

  }

  public static void SEND_GDF_PERSO(Player perso, int celda, int frame, int esInteractivo)
  {
    String packet="GDF|"+celda+";"+frame+";"+esInteractivo;
    send(perso,packet);

  }

  public static void SEND_EMK_MOVE_ITEM(GameClient out, char tipoOG, String signo, String s1)
  {
    String packet="EMK"+tipoOG+signo;
    if(!s1.equals(""))
      packet+=s1;
    send(out,packet);

  }

  public static void SEND_OR_DELETE_ITEM(GameClient out, int id)
  {
    String packet="OR"+id;
    send(out,packet);
  }

  public static void GAME_SEND_CHALLENGE_FIGHT(Fight fight, int team, String str)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("Gd").append(str);

    for(Fighter fighter : fight.getFighters(team))
    {
      if(fighter.hasLeft())
        continue;
      if(fighter.getPersonnage()==null||!fighter.getPersonnage().isOnline())
        continue;
      send(fighter.getPersonnage(),packet.toString());
    }

  }

  public static void GAME_SEND_CHALLENGE_PERSO(Player p, String str)
  {
    send(p,"Gd"+str);

  }

  public static void GAME_SEND_Im_PACKET_TO_CHALLENGE(Fight fight, int challenge, String str)
  {
    StringBuilder packet=new StringBuilder();
    packet.append("Im").append(str);
    for(Fighter fighter : fight.getFighters(challenge))
    {
      if(fighter.hasLeft())
        continue;
      if(fighter.getPersonnage()==null||!fighter.getPersonnage().isOnline())
        continue;
      send(fighter.getPersonnage(),packet.toString());
    }

  }

  public static void GAME_SEND_Im_PACKET_TO_CHALLENGE_PERSO(Player player, String str)
  {

    send(player,"Im"+str);

  }

  public static void GAME_SEND_MESSAGE_SERVER(Player out, String args)
  {
    String packet="M1"+args;
    send(out,packet);

  }

  public static void GAME_SEND_WELCOME(Player perso)
  {
    send(perso,"TB");

  }

  public static void GAME_SEND_Eq_PACKET(Player Personnage, long Prix)
  {
    send(Personnage,"Eq1|1|"+Prix);
  }

  public static void GAME_SEND_INFO_HIGHLIGHT_PACKET(Player perso, String args)
  {
    String packet="IH"+args;
    send(perso,packet);

  }

  public static void GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(final Fight fight, final int teams)
  {
    String packet="GA;0";
    for(final Fighter f : fight.getFighters(teams))
    {
      if(f.hasLeft()||f.getPersonnage()==null||!f.getPersonnage().isOnline())
        continue;
      send(f.getPersonnage(),packet);
    }

  }

  public static void SEND_MESSAGE_DECO(Player P, int MSG_ID, String args)
  {
    String packet="M0"+MSG_ID+"|"+args;
    send(P,packet);
  }

  public static void SEND_MESSAGE_DECO_ALL(int MSG_ID, String args)
  {
    String packet="M0"+MSG_ID+"|"+args;
    for(Player perso : Main.world.getOnlinePlayers())
      send(perso,packet);
  }

  public static void SEND_gA_PERCEPTEUR(Player perso, String str)
  {
    String packet="gA"+str;
    send(perso,packet);
  }

  public static void SEND_Im1223_ALL(String str)
  {
    String packet="Im116;<b>Server</b>~"+str;
    for(Player perso : Main.world.getOnlinePlayers())
      send(perso,packet);
  }

  public static void GAME_SEND_PERCO_INFOS_PACKET(Player perso, Collector perco, String car)
  {
    send(perso,"gA"+car+perco.getN1()+","+perco.getN2()+"|"+"-1"+"|"+Main.world.getMap(perco.getMap()).getX()+"|"+Main.world.getMap(perco.getMap()).getY());
  }

  public static void SEND_Wp_MENU_Prisme(Player perso)
  {
    String packet="Wp"+perso.parsePrismesList();
    send(perso.getGameClient(),packet);
  }

  public static void SEND_Ww_CLOSE_Prisme(Player out)
  {
    String packet="Ww";
    send(out,packet);
  }

  public static void GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(Player p, String str)
  {
    String packet="am"+str;
    //Evite les putins de flood en console.
    if(p==null||p.getAccount()==null)
      return;
    send(p,packet);
  }

  public static void SEND_CB_BONUS_CONQUETE(Player pj, String str)
  {
    String packet="CB"+str;
    send(pj,packet);
  }

  public static void SEND_Cb_BALANCE_CONQUETE(Player pj, String str)
  {
    String packet="Cb"+str;
    send(pj,packet);
  }

  public static void SEND_GM_PRISME_TO_MAP(GameClient out, GameMap Map)
  {// envia informacion de todos mercantes en 1 Map
    String packet=Map.getPrismeGMPacket();
    if(packet==""||packet.isEmpty())
      return;
    send(out,packet);
  }

  public static void GAME_SEND_PRISME_TO_MAP(GameMap Map, Prism Prisme)
  {
    String packet=Prisme.getGMPrisme();
    for(Player z : Map.getPlayers())
      send(z,packet);
  }

  public static void SEND_CP_INFO_DEFENSEURS_PRISME(Player perso, String str)
  {
    String packet="CP"+str;
    send(perso,packet);
  }

  public static void SEND_Cp_INFO_ATTAQUANT_PRISME(Player perso, String str)
  {
    String packet="Cp"+str;
    send(perso,packet);
  }

  public static void SEND_CIV_CLOSE_INFO_CONQUETE(Player pj)
  {
    String packet="CIV";
    send(pj,packet);

  }

  public static void SEND_CW_INFO_WORLD_CONQUETE(Player pj, String str)
  {
    String packet="CW"+str;
    send(pj,packet);

  }

  public static void SEND_CIJ_INFO_JOIN_PRISME(Player pj, String str)
  {
    String packet="CIJ"+str;
    send(pj,packet);
  }

  public static void GAME_SEND_aM_ALIGN_PACKET_TO_AREA(Player perso, String str)
  {
    String packet="aM"+str;
    send(perso,packet);
  }

  public static void SEND_GA_ACTION_TO_Map(GameMap Map, String gameActionID, int actionID, String s1, String s2)
  {
    String packet="GA"+gameActionID+";"+actionID+";"+s1;
    if(!s2.equals(""))
      packet+=";"+s2;
    for(Player z : Map.getPlayers())
      send(z,packet);
  }

  public static void SEND_CS_SURVIVRE_MESSAGE_PRISME(Player perso, String str)
  {
    String packet="CS"+str;
    send(perso,packet);
  }

  public static void SEND_CD_MORT_MESSAGE_PRISME(Player perso, String str)
  {
    String packet="CD"+str;
    send(perso,packet);
  }

  public static void SEND_CA_ATTAQUE_MESSAGE_PRISME(Player perso, String str)
  {
    String packet="CA"+str;
    send(perso,packet);
  }

  public static void GAME_SEND_ACTION_TO_DOOR(GameMap map, int args, boolean open)
  {
    String packet="";
    if(open)
      packet="GDF|"+args+";2";
    else
      packet="GDF|"+args+";4";
    for(Player z : map.getPlayers())
      send(z,packet);
  }

  public static void GAME_SEND_ACTION_TO_DOOR(Player p, int args, boolean open)
  {
    String packet="";
    if(open)
      packet="GDF|"+args+";2";
    else if(!open)
      packet="GDF|"+args+";4";

    send(p,packet);
  }

  public static void GAME_UPDATE_CELL(GameMap map, String args)
  {
    String packet="GDC"+args;
    for(Player z : map.getPlayers())
      send(z,packet);
  }

  public static void GAME_UPDATE_CELL(Player p, String args)
  {
    send(p,"GDC"+args);
  }

  public static void GAME_SEND_ALE_PACKET(GameClient out, String caract)
  {
    String packet=(new StringBuilder("AlE")).append(caract).toString();
    send(out,packet);
  }

  public static void QuestList(GameClient out, Player perso)
  {
    /*
     * Explication packet : QL + QuestID ; Finish ? 1 : 0 ;
     */
    String packet="QL"+perso.getQuestGmPacket();
    send(out,packet);
  }

  public static void QuestGep(GameClient out, Quest quest, Player perso)
  {
    /*
     * Explication packet : aQuestId | aObjectifCurrent |
     * aEtapeId,aFinish;aEtapeId,aFinish... | aPreviousObjectif |
     * aNextObjectif | aDialogId | aDialogParams
     */
    // String packet = "QS"+"3|6|289,0;421,0|";//TODO suite ...
    String packet="QS"+quest.getGmQuestDataPacket(perso);
    //String packet = "QS181|343|745,0|342|344|3646|";
    send(out,packet);
  }

  public static void sendPacketToMap(GameMap map, String packet)
  {
    for(Player perso : map.getPlayers())
      send(perso,packet);
  }

  public static void sendPacketToMapGM(GameMap map, Npc npc)
  {
    for(Player perso : map.getPlayers())
      send(perso,"GM|"+npc.parse(true,perso));
  }

  //v2.7 - Mimisymbics
  public static void sendMimisymbicPacket(GameClient out)
  {
    send(out,"bM");
  }

  //v2.8 - Average ping system
  public static void sendAveragePingPacket(GameClient out)
  {
    String packet="Bp";
    send(out,packet);
  }

  public static void ENVIAR_GA_MOVER_SPRITE_MAPA(final GameMap mapa, final int idUnica, final int idAccionModelo, final String s1, final String s2)
  {
    String packet="GA"+(idUnica<=-1 ? "" : idUnica)+";"+idAccionModelo+";"+s1;
    if(!s2.isEmpty())
      packet+=";"+s2;
    for(final Player p : mapa.getPlayers())
      send(p,packet);
  }

  //v2.8 - Average ping system
  public static void sendClearChallengePacket(GameClient out)
  {
    System.out.println("sent");
    String packet="zC";
    send(out,packet);
  }
  public static void ENVIAR_AK_KEY_ENCRIPTACION_PACKETS(final GameClient out,int currentKey, String key) {
	  final String packet = "AK" + Integer.toHexString(currentKey).toUpperCase() + key;
	  send(out, packet);
	 }
  
	public static void ENVIAR_bT_TIENDA(final Player out, StringBuilder tienda, int ogrinas) {
		final String packet = "bT" + tienda + "^" + ogrinas;
		send(out, packet);
	}

	  public static void PACKET_POPUP(final Player perso, final String str) {
			final String packet = "BAIO" + str;
			send(perso, packet);
		}

	  public static void ENVIAR_BAT2_CONSOLA(Player out, String mess)
	  {
	    String packet="BAT2"+mess;
	    send(out,packet);

	  }

	    public static void GAME_SEND_ADD_SHORTCUT(Player player, Shortcuts shortcut)
	    {
	        if(shortcut.getObject() != null) {
	            String packet = "OrA" + shortcut.getPosition() + ";" + shortcut.getObject().getTemplate().getId() + ";" + shortcut.getObject().parseToSave();
	            send(player, packet);
	        }
	    }
	    public static void GAME_SEND_REMOVE_SHORTCUT(Player player, int pos)
	    {
	        String packet = "OrR" + pos;
	        send(player, packet);
	    }

	    public static void ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_TODOS(final Fight pelea, final int equipos) {
	        ArrayList<Fighter> aEnviar = pelea.getFighters(equipos);
	        for (final Fighter luchador : pelea.getFighters(3)) {
	            final Stats totalStats = luchador.getTotalStats();
	            final StringBuilder packet1 = new StringBuilder();
	            final StringBuilder packet2 = new StringBuilder();
	            packet1.append("|" + luchador.getId() + ";");
	            if (luchador.isDead()) {
	                packet1.append(1 + ";");
	            } else {
	                packet1.append(0 + ";");
	                packet1.append(luchador.getPdv() + ";");
	                packet1.append(luchador.getPa() + ";");
	                packet1.append(luchador.getPm() + ";");
	                packet2.append(";");
	                packet2.append(luchador.getPdvMax() + ";");
	                packet2.append(";");
	                packet2.append(";");
	                int[] resist = new int[7];
	                resist[0] = Constant.STATS_ADD_RP_NEU;
	                resist[1] = Constant.STATS_ADD_RP_TER;
	                resist[2] = Constant.STATS_ADD_RP_FEU;
	                resist[3] = Constant.STATS_ADD_RP_EAU;
	                resist[4] = Constant.STATS_ADD_RP_AIR;
	                resist[5] = Constant.STATS_ADD_AFLEE;
	                resist[6] = Constant.STATS_ADD_MFLEE;
	                for (int statID : resist) {
	                    int total = totalStats.getEffectSummed(statID);
	                    packet2.append(total + ",");
	                }
	            }
	            for (final Fighter enviar : aEnviar) {
	                if (enviar.hasLeft() || enviar.getPersonnage() == null) {
	                    continue;
	                }
	                enviar.getStringBuilderGTM().append(packet1.toString());
	                if (!luchador.isDead()) {
	                    enviar.getStringBuilderGTM()
	                            .append((luchador.getCell() == null || luchador.esInvisible(enviar.getId()) ? "-1"
	                                    : luchador.getCell().getId()) + ";" + packet2.toString());
	                }
	            }
	        }
	        for (final Fighter enviar : aEnviar) {
	            if (enviar.hasLeft() || enviar.getPersonnage() == null) {
	                continue;
	            }
	            if (enviar.getStringBuilderGTM().toString().isEmpty()) {
	                continue;
	            }
	            String packet = "GTU";
	            packet += enviar.getStringBuilderGTM().toString();
	            enviar.resetStringBuilderGTM();
	            send(enviar.getPersonnage(), packet);

	        }
	    }

}
