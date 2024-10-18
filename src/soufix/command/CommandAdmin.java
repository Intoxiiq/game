package soufix.command;

import soufix.area.SubArea;
import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.area.map.entity.MountPark;
import soufix.client.Account;
import soufix.client.Player;
import soufix.command.administration.AdminUser;
import soufix.command.administration.Command;
import soufix.command.administration.Group;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.Collector;
import soufix.entity.Npc;
import soufix.entity.monster.MobGrade;
import soufix.entity.monster.MobGroup;
import soufix.entity.monster.Monster;
import soufix.entity.mount.Mount;
import soufix.entity.npc.NpcTemplate;
import soufix.entity.pet.PetEntry;
import soufix.fight.Challenge;
import soufix.fight.Fight;
import soufix.game.GameClient;
import soufix.game.World;
import soufix.game.action.ExchangeAction;
import soufix.game.scheduler.entity.WorldSave;
import soufix.job.JobStat;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.object.ObjectSet;
import soufix.object.ObjectTemplate;
import soufix.quest.Quest;
import soufix.quest.QuestPlayer;
import soufix.quest.QuestStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class CommandAdmin extends AdminUser
{

  public CommandAdmin(Player player)
  {
    super(player);
  }

  public void apply(String packet,boolean serveur)
  {
    String msg=packet.substring(2);
    String[] infos=msg.split(" ");

    if(infos.length==0)
      return;
    String command=infos[0];

    try
    {
    	 if(!serveur) {
      Group groupe=this.getPlayer().getGroupe();
     
      if(groupe==null)
      {
        this.getClient().disconnect();
        return;
      }
	  if(command.equalsIgnoreCase("JOIN"))
		  command = "gon";
	  if(command.equalsIgnoreCase("GONAME"))
		  command = "gon";
	  if(command.equalsIgnoreCase("TELEPORT"))
		  command = "tp";
	  if(command.equalsIgnoreCase("NAMEANNOUNCE"))
		  command = "aname";
	  if(command.equalsIgnoreCase("ANNOUNCE"))
		  command = "a";
      if(!groupe.haveCommand(command))
      {
        this.sendMessage("Commande invalide !");
        return;
      }}
      this.command(command,infos,msg);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public void command(String command, String[] infos, String msg)
  {
     if(command.equalsIgnoreCase("CHALL"))
    {
      Challenge challenge=new Challenge(this.getPlayer().getFight(),Integer.parseInt(infos[1]),0,0);
      this.getPlayer().getFight().getAllChallenges().put(Integer.parseInt(infos[1]),challenge);
      challenge.fightStart();
      SocketManager.GAME_SEND_CHALLENGE_FIGHT(this.getPlayer().getFight(),1,challenge.parseToPacket());
      return;
    }
    else if(command.equalsIgnoreCase("HELP"))
    {
      String cmd="";
      try
      {
        cmd=infos[1];
      }
      catch(Exception e)
      {
      }
      if(cmd.equalsIgnoreCase(""))
      {
        this.sendMessage("\nVous avez actuellement le groupe GM "+this.getPlayer().getGroupe().getName()+".\nCommandes disponibles :\n");
        for(Command commande : this.getPlayer().getGroupe().getCommands())
        {
          String args=(commande.getArguments()[1]!=null&&!commande.getArguments()[1].equalsIgnoreCase("")) ? (" + "+commande.getArguments()[1]) : ("");
          String desc=(commande.getArguments()[2]!=null&&!commande.getArguments()[2].equalsIgnoreCase("")) ? (commande.getArguments()[2]) : ("");
          this.sendMessage("<u>"+commande.getArguments()[0]+args+"</u> - "+desc);
        }
        return;
      }
      else
      {
        this.sendMessage("\nVous avez actuellement le groupe GM "+this.getPlayer().getGroupe().getName()+".\nCommandes recherches :\n");
        for(Command commande : this.getPlayer().getGroupe().getCommands())
        {
          if(commande.getArguments()[0].contains(cmd.toUpperCase()))
          {
            String args=(commande.getArguments()[1]!=null&&!commande.getArguments()[1].equalsIgnoreCase("")) ? (" + "+commande.getArguments()[1]) : ("");
            String desc=(commande.getArguments()[2]!=null&&!commande.getArguments()[2].equalsIgnoreCase("")) ? (commande.getArguments()[2]) : ("");
            this.sendMessage("<u>"+commande.getArguments()[0]+args+"</u> - "+desc);
          }
        }
        return;
      }
    }
    else 
    	if(command.equalsIgnoreCase("EVENTGG"))
        {
          Player perso=this.getPlayer();
          if(infos.length>1)
          {
            try
            {
              perso=Main.world.getPlayerByName(infos[1]);
            }
            catch(Exception e)
            {
              // ok
            }
            if(perso==null)
            {
              this.sendMessage("Le personnage n'a pas ete trouve");
              return;
            }
          }
          if(this.getPlayer().getAccount().getEvent() == 3) {
        	  String str="3 event max par jour.";
              this.sendMessage(str);  
              return;
          }
          perso.getAccount().setPoints( perso.getAccount().getPoints() + 300);
          Database.getStatics().getPlayerData().logs_event(this.getPlayer().getName(), perso.getName(), 300);
          this.getPlayer().getAccount().setEvent( this.getPlayer().getAccount().getEvent()+1);
           String suffix="Le joueur "+perso.getName()+" vient de gagner 300 points boutique suite é l'event fait par "+this.getPlayer().getName()+" .";
            String prefix="<b>Server</b>";
            SocketManager.GAME_SEND_Im_PACKET_TO_ALL("116;"+prefix+"~"+suffix);
          String str="Le joueur a eu ses points.";
          this.sendMessage(str);
          return;
        }
        else
        	if(command.equalsIgnoreCase("LOADPERSO"))
            {
               String name = null;
              if(infos.length>1)
              {
                try
                {
                 name = infos[1];
                }
                catch(Exception e)
                {
                  // ok
                }
                if(name==null)
                {
                  this.sendMessage("Le personnage n'a pas ete trouve");
                  return;
                }
              }
              
              Database.getStatics().getPlayerData().loadby_name(name);
             // Main.world.ReassignAccountToChar(perso.getAccount().getId());
              String str="Le joueur "+name+" a ete load.";
              this.sendMessage(str);
              return;
            }
            else
        	if(command.equalsIgnoreCase("UNLOADPERSO"))
            {
              Player perso=this.getPlayer();
              if(infos.length>1)
              {
                try
                {
                  perso=Main.world.getPlayerByName(infos[1]);
                }
                catch(Exception e)
                {
                  // ok
                }
                if(perso==null)
                {
                  this.sendMessage("Le personnage n'a pas ete trouve");
                  return;
                }
              }
              Database.getStatics().getPlayerData().update(perso);
              perso.getCurCell().removePlayer(perso);
              if(perso.getGameClient()!=null)
                perso.getGameClient().disconnect();
              perso.setOnline(false);
              perso.resetVars();
              SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(),perso.getId());
             Main.world.unloadPerso(perso);
              String str="Le joueur "+perso.getName()+" a ete un load.";
              this.sendMessage(str);
              return;
            }
            else
        	if(command.equalsIgnoreCase("ONLINE"))
    {
      Player perso=this.getPlayer();
      if(infos.length>1)
      {
        try
        {
          perso=Main.world.getPlayerByName(infos[1]);
        }
        catch(Exception e)
        {
          // ok
        }
        if(perso==null)
        {
          this.sendMessage("Le personnage n'a pas ete trouve");
          return;
        }
      }
      try
      {
        for(GameObject object : new ArrayList<>(perso.getObjects().values()))
        {
          if(object==null)
            continue;
          if(object.modification==0)
            Database.getDynamics().getObjectData().insert(object);
          else if(object.modification==1)
            Database.getDynamics().getObjectData().update(object);
          object.modification=-1;
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      Database.getStatics().getPlayerData().update(perso);
      perso.getCurCell().removePlayer(perso);
      if(perso.getGameClient()!=null)
        perso.getGameClient().disconnect();
      perso.setOnline(false);
      perso.resetVars();
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(),perso.getId());
     Main.world.unloadPerso(perso);
      Database.getStatics().getPlayerData().load(perso.getId());
     // Main.world.ReassignAccountToChar(perso.getAccount().getId());
      if(perso.get_guild() != null)
	   perso.get_guild().getMember(perso.getId()).setPlayer(Main.world.getPlayer(perso.getId()));
      String str="Le joueur "+perso.getName()+" a ete reinitialise de ces variables.";
      this.sendMessage(str);
      return;
    }
    else
    	if(command.equalsIgnoreCase("ANAME") || command.equalsIgnoreCase("NAMEANNOUNCE"))
    {
      infos=msg.split(" ",2);
      String prefix="<b><a href='asfunction:onHref,ShowPlayerPopupMenu,"+this.getPlayer().getName()+"'>["+this.getPlayer().getGroupe().getName()+"] "+this.getPlayer().getName()+"</a></b>";
      if(infos.length>1)
      {
        String suffix=infos[1];
        if(suffix.contains("<")&&(!suffix.contains(">")||!suffix.contains("</"))) // S'il n'y a pas de balise fermante
          suffix=suffix.replace("<","").replace(">","");
        if(suffix.contains("<")&&suffix.contains(">")&&!suffix.contains("</")) // S'il n'y a pas de balise fermante
          suffix=suffix.replace("<","").replace(">","");
        SocketManager.GAME_SEND_Im_PACKET_TO_ALL("116;"+prefix+"~"+suffix);
      }
      return;
    }
    else if(command.equalsIgnoreCase("GONAME")||command.equalsIgnoreCase("JOIN")||command.equalsIgnoreCase("GON"))
    {
    	if(Config.singleton.serverId == 22)
    	if(this.getPlayer().getGroupe().getId() >= 1 && this.getPlayer().getGroupe().getId() < 6)
    		return;
      Player P=Main.world.getPlayerByName(infos[1]);
      if(P==null)
      {
        String str="Le personnage de destination n'existe pas.";
        this.sendMessage(str);
        return;
      }
      if(P.getGroupe() != null && P.getGroupe().getId() == 7)
          return;	  
      short mapID=P.getCurMap().getId();
      int cellID=P.getCurCell().getId();

      Player perso=this.getPlayer();
      if(infos.length>2)//Si un nom de perso est specifie
      {
        perso=Main.world.getPlayerByName(infos[2]);
        if(perso==null)
        {
          String str="Le personnage e teleporter n'a pas ete trouve.";
          this.sendMessage(str);
          return;
        }
        if(perso.getFight()!=null)
        {
          String str="La cible e teleporter est en combat.";
          this.sendMessage(str);
          return;
        }
      }
      perso.teleport(mapID,cellID);
      String str="Le joueur "+perso.getName()+" a ete teleporte vers "+P.getName()+".";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("KICKFIGHT"))
    {
      Player P=Main.world.getPlayerByName(infos[1]);
      if(P==null||P.getFight()==null)
      {
        this.sendMessage("Le personnage n'a pas ete trouve ou il n'est pas en combat.");
        return;
      }
      SocketManager.GAME_SEND_GV_PACKET(P);
      if(P.getFight()!=null)
      {
        P.getFight().leftFight(P,null);
        P.setFight(null);
      }
      SocketManager.GAME_SEND_GV_PACKET(P);
      this.sendMessage("Le personnage "+P.getName()+" a ete expulse de son combat.");
      return;
    }
    else if(command.equalsIgnoreCase("KICKIP"))
    {
    	if(this.getPlayer().getCurMap().getId() != 3336)
    		return;
    	List<String> ip = new ArrayList<>();
        for(Player perso : this.getPlayer().getCurMap().getPlayers()) {
          
        	  if(!ip.contains(";"+perso.getAccount().getCurrentIp() +";")) {
        		  ip.add(";"+perso.getAccount().getCurrentIp() +";");
              }else {
            	  for(Player perso1 : this.getPlayer().getCurMap().getPlayers())
            	  SocketManager.GAME_SEND_MESSAGE(perso1,"Le personnage "+perso.getName()+" a ete expulse multi ip.");
            	  perso.teleport((short) 6954, 300);
              }
        }
      return;
    }
    else if(command.equalsIgnoreCase("DEBUG"))
    {
      Player perso=this.getPlayer();
      if(infos.length>1)//Si un nom de perso est specifie
      {
        perso=Main.world.getPlayerByName(infos[1]);
        if(perso==null)
        {
          String str="Le personnage n'a pas ete trouve.";
          this.sendMessage(str);
          return;
        }
        if(perso.getFight()!=null)
        {
          String str="La cible est en combat.";
          this.sendMessage(str);
          return;
        }
      }
      else
      {
        return;
      }
      perso.warpToSavePos();
      String str="Le joueur "+perso.getName()+" a ete teleporte e son point de sauvegarde.";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("JOBLEFT"))
    {
      Player perso=this.getPlayer();
      try
      {
        perso=Main.world.getPlayerByName(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }
      if(perso==null)
        perso=this.getPlayer();
      perso.setDoAction(false);
      perso.setExchangeAction(null);
      this.sendMessage("L'action de metier e ete annule.");
      return;
    }
    else if(command.equalsIgnoreCase("WHO"))
    {
    	if(Config.singleton.serverId == 22)
        	if(this.getPlayer().getGroupe().getId() >= 1 && this.getPlayer().getGroupe().getId() < 6)
        		return;
      String mess="\n<u>Liste des joueurs en ligne :</u>";
      this.sendMessage(mess);
      int i=0;

      for(Player player : Main.world.getOnlinePlayers())
      {
        if(i==30)
          break;
        if(player==null)
          continue;
        i++;
        mess=player.getName()+" ("+player.getId()+") ";
        mess+=returnClasse(player.getClasse());
        mess+=" ";
        mess+=(player.getSexe()==0 ? "M" : "F")+" ";
        mess+=player.getLevel()+" ";
        mess+=player.getCurMap().getId()+"("+player.getCurMap().getX()+"/"+player.getCurMap().getY()+") ";
        mess+=player.getFight()==null ? "" : "Combat ";

        this.sendMessage(mess);
      }

      if(Main.gameServer.getClients().size()-30>0)
      {
        mess="Et "+(Main.gameServer.getClients().size()-30)+" autres personnages";
        this.sendMessage(mess);
      }
      mess="\n";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("WHOALL"))
    {
    	if(Config.singleton.serverId == 22)
        	if(this.getPlayer().getGroupe().getId() >= 1 && this.getPlayer().getGroupe().getId() < 6)
        		return;
      String mess="\n<u>Liste des joueurs en ligne :</u>";
      this.sendMessage(mess);
      for(GameClient client : Main.gameServer.getClients())
      {
        Player player=client.getPlayer();

        if(player==null)
          continue;

        mess=player.getName()+" ("+player.getId()+") ";
        mess+=returnClasse(player.getClasse());
        mess+=" ";
        mess+=(player.getSexe()==0 ? "M" : "F")+" ";
        mess+=player.getLevel()+" ";
        mess+=player.getCurMap().getId()+"("+player.getCurMap().getX()+"/"+player.getCurMap().getY()+") ";
        mess+=player.getFight()==null ? "" : "Combat ";

        this.sendMessage(mess);
      }
      mess="\n";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("WHOFIGHT"))
    {
      String mess="";
      this.sendMessage("\n<u>Liste des joueurs en ligne et en combat :</u>");
      for(GameClient client : Main.gameServer.getClients())
      {
        Player player=client.getPlayer();

        if(player==null)
          continue;

        if(player.getFight()==null)
          continue;

        mess=player.getName()+" ("+player.getId()+") ";
        mess+=returnClasse(player.getClasse());
        mess+=" ";
        mess+=(player.getSexe()==0 ? "M" : "F")+" ";
        mess+=player.getLevel()+" ";
        mess+=player.getCurMap().getId()+"("+player.getCurMap().getX()+"/"+player.getCurMap().getY()+") ";
        mess+=player.getFight()==null ? "" : "Combat ";

        this.sendMessage(mess);
      }
      if(mess.equalsIgnoreCase(""))
      {
        this.sendMessage("Aucun joueur en combat.");
      }
      else
      {
        mess="\n";
        this.sendMessage(mess);
      }
      return;
    }
    else if(command.equalsIgnoreCase("NAMEGO")||command.equalsIgnoreCase("NGO"))
    {
      Player perso=Main.world.getPlayerByName(infos[1]);
      if(perso==null)
      {
        String str="Le personnage e teleporter n'existe pas.";
        this.sendMessage(str);
        return;
      }
      if(perso.getFight()!=null)
      {
        String str="Le personnage e teleporter est en combat.";
        this.sendMessage(str);
        return;
      }
      Player P=this.getPlayer();
      if(infos.length>2)//Si un nom de perso est specifie
      {
        P=Main.world.getPlayerByName(infos[2]);
        if(P==null)
        {
          String str="Le personnage de destination n'a pas ete trouve.";
          this.sendMessage(str);
          return;
        }
      }
      if(P.isOnline())
      {
        short mapID=P.getCurMap().getId();
        int cellID=P.getCurCell().getId();
        perso.teleport(mapID,cellID);
        String str="Le joueur "+perso.getName()+" a ete teleporte vers "+P.getName()+".";
        this.sendMessage(str);
      }
      else
      {
        String str="Le joueur "+P.getName()+" n'est pas en ligne.";
        this.sendMessage(str);
      }
      return;
    }
    else if(command.equalsIgnoreCase("TP") || command.equalsIgnoreCase("TELEPORT"))
    {
    	if(Config.singleton.serverId == 22)
        	if(this.getPlayer().getGroupe().getId() >= 1 && this.getPlayer().getGroupe().getId() < 6)
        		return;
      short mapID=-1;
      int cellID=-1;
      try
      {
        mapID=Short.parseShort(infos[1]);
        cellID=Integer.parseInt(infos[2]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(mapID==-1||cellID==-1||Main.world.getMap(mapID)==null)
      {
        String str="";
        if(mapID==-1||Main.world.getMap(mapID)==null)
          str="MapID invalide.";
        else
          str="cellID invalide.";
        this.sendMessage(str);
        return;
      }
      if(Main.world.getMap(mapID).getCase(cellID)==null)
      {
        String str="cellID invalide.";
        this.sendMessage(str);
        return;
      }
      Player perso=this.getPlayer();
      if(infos.length>3)//Si un nom de perso est specifie
      {
        perso=Main.world.getPlayerByName(infos[3]);
        if(perso==null||perso.getFight()!=null)
        {
          String str="Le personnage n'a pas ete trouve ou est en combat";
          this.sendMessage(str);
          return;
        }
        if(!perso.isOnline())
        {
          perso.setCurMap(Main.world.getMap(mapID));
          perso.setCurCell(Main.world.getMap(mapID).getCase(cellID));
        }
      }
      perso.teleport(mapID,cellID);
      String str="Le joueur "+perso.getName()+" a ete teleporte.";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("SIZE"))
    {
      int size=-1;
      try
      {
        size=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }
      if(size==-1)
      {
        String str="Taille invalide.";
        this.sendMessage(str);
        return;
      }
      Player perso=this.getPlayer();
      if(infos.length>2)//Si un nom de perso est specifie
      {
        perso=Main.world.getPlayerByName(infos[2]);
        if(perso==null)
        {
          String str="Le personnage n'a pas ete trouve.";
          this.sendMessage(str);
          return;
        }
      }
      perso.set_size(size);
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(),perso.getId());
      SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(),perso);
      String str="La taille du joueur "+perso.getName()+" a ete modifiee.";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("SPIONED"))
    {
        Player perso=null;

        perso=Main.world.getPlayerByName(infos[1]);
          if(perso==null)
          {
            String str="Le personnage n'a pas ete trouve.";
            this.sendMessage(str);
            return;
          }
        String str;
        if(perso.getSpioned_by() == null) {
         perso.setSpioned_by(this.getPlayer());
         this.getPlayer().teleport(perso.getCurMap().getId(),perso.getCurCell().getId());
        str="Spioned On";
        }else
        {
        	 perso.setSpioned_by(null);
             str="Spioned Off";	
        }
        this.sendMessage(str);
        return;
      }
      else 
    	if(command.equalsIgnoreCase("FREEZE"))
    {
      Player perso=this.getPlayer();
      if(infos.length>1)
      {
        perso=Main.world.getPlayerByName(infos[1]);
      }
      if(perso==null)
      {
        String mess="Le personnage n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      if(perso.getBlockMovement())
        this.sendMessage("Le joueur n'est plus bloque.");
      else
        this.sendMessage("Le joueur est bloque.");
      perso.setBlockMovement(!perso.getBlockMovement());
      return;
    }
    else if(command.equalsIgnoreCase("BLOCKMAP"))
    {
      int i=-1;
      try
      {
        i=Short.parseShort(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }
      if(i==0)
      {
        Config.getInstance().mapAsBlocked=false;
        this.sendMessage("Map deblocke.");
      }
      else if(i==1)
      {
        Config.getInstance().mapAsBlocked=true;
        this.sendMessage("Map blocke.");
      }
      else
      {
        this.sendMessage("Aucune information.");
      }
      return;
    }
    else if(command.equalsIgnoreCase("BLOCKFIGHT"))
    {
      int i=-1;
      try
      {
        i=Short.parseShort(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }
      if(i==0)
      {
        Config.getInstance().fightAsBlocked=false;
        for(Player player : Main.world.getOnlinePlayers())
          player.sendServerMessage("Les combats ont etes debloques !");
        this.sendMessage("Les combats ont etes debloques.");
      }
      else if(i==1)
      {
        for(Player player : Main.world.getOnlinePlayers())
          player.sendServerMessage("Les combats ont etes bloques.");
        this.sendMessage("Les combats ont etes bloques.");
      }
      else
      {
        this.sendMessage("Aucune information.");
      }
      return;
    }
    else if(command.equalsIgnoreCase("MUTE"))
    {
      Player player;
      String name;
      short time=0;

      try
      {
        name=infos[1];
        time=Short.parseShort(infos[2]);
      }
      catch(Exception e)
      {
        this.sendErrorMessage("The name/time you've enter is/are invalid ! (max time : "+Short.MAX_VALUE+" in minutes)");
        return;
      }

      player=Main.world.getPlayerByName(name);

      if(player==null||time<=0)
      {
        this.sendErrorMessage("The player wasn't found or the time is negative. (max time : "+Short.MAX_VALUE+" in minutes)");
        return;
      }
      if(player.getAccount()==null || player.getGroupe() != null)
      {
        this.sendErrorMessage("The account of the player wasn't found, please check the name.");
        return;
      }

      player.getAccount().mute(time,this.getPlayer().getName());
      this.sendSuccessMessage("You've mute the player "+player.getName()+" for "+time+"minute(s) effective for all players of this account !");

      if(!player.isOnline())
        this.sendErrorMessage("The player is not online, are you sure it is the correct player ?");
      return;
    }
    else if(command.equalsIgnoreCase("MUTEIP"))
    {
      Player player;
      String name;
      short time;

      try
      {
        name=infos[1];
        time=Short.parseShort(infos[2]);
      }
      catch(Exception e)
      {
        this.sendErrorMessage("The name/time you've enter is/are invalid ! (max time : "+Short.MAX_VALUE+" in minutes)");
        return;
      }

      player=Main.world.getPlayerByName(name);

      if(player==null||time<=0)
      {
        this.sendErrorMessage("The player wasn't found or the time is negative. (max time : "+Short.MAX_VALUE+" in minutes)");
        return;
      }

      if(player.getAccount()==null)
      {
        this.sendErrorMessage("The account of the player wasn't found, please check the name.");
        return;
      }

      String ip=player.getAccount().getLastIP();

      if(ip.equalsIgnoreCase(""))
      {
        this.sendErrorMessage("Sorry but the server don't have any IP of this account, check another account please.");
        return;
      }

      Main.world.getAccountsByIp(ip).values().stream().filter(account -> account!=null&&account.getLastIP().equalsIgnoreCase(ip)).forEach(account -> {
        account.mute(time,this.getPlayer().getName());
        if(account.getCurrentPlayer()!=null)
          this.sendMessage("You've mute the account "+account.getName()+".");
      });

      this.sendSuccessMessage("All the accounts of the IP ("+ip+") have been mute for "+time+" minute(s) successfully !");
      return;
    }
    else if(command.equalsIgnoreCase("UNMUTEIP"))
    {
      Player player;
      String name;

      try
      {
        name=infos[1];
      }
      catch(Exception e)
      {
        this.sendErrorMessage("The name/time you've enter is/are invalid ! (max time : "+Short.MAX_VALUE+" in minutes)");
        return;
      }

      player=Main.world.getPlayerByName(name);

      if(player==null)
      {
        this.sendErrorMessage("The player wasn't found or the time is negative. (max time : "+Short.MAX_VALUE+" in minutes)");
        return;
      }

      if(player.getAccount()==null)
      {
        this.sendErrorMessage("The account of the player wasn't found, please check the name.");
        return;
      }

      String ip=player.getAccount().getLastIP();

      if(ip.equalsIgnoreCase(""))
      {
        this.sendErrorMessage("Sorry but the server don't have any IP of this account, check another account please.");
        return;
      }

      Main.world.getAccountsByIp(ip).values().stream().filter(account -> account!=null&&account.getLastIP().equalsIgnoreCase(ip)).forEach(account -> {
        account.unMute();
        if(account.getCurrentPlayer()!=null)
          this.sendMessage("The account "+account.getName()+" is free to talk.");
      });

      this.sendSuccessMessage("All the accounts of the IP ("+ip+") are free to talk successfully !");
      return;
    }
    else if(command.equalsIgnoreCase("UNMUTE"))
    {
      Player player;
      String name;

      try
      {
        name=infos[1];
      }
      catch(Exception e)
      {
        this.sendErrorMessage("The name/time you've enter is/are invalid ! (max time : "+Short.MAX_VALUE+" in minutes)");
        return;
      }

      player=Main.world.getPlayerByName(name);

      if(player==null)
      {
        this.sendErrorMessage("The player wasn't found or the time is negative. (max time : "+Short.MAX_VALUE+" in minutes)");
        return;
      }

      if(player.getAccount()==null)
      {
        this.sendErrorMessage("The account of the player wasn't found, please check the name.");
        return;
      }

      player.getAccount().unMute();
      this.sendSuccessMessage("You've unmute the player "+player.getName()+" effective for all players of this account !");

      if(!player.isOnline())
        this.sendErrorMessage("The player is not online, are you sure it is the correct player ?");
      return;
    }
    else if(command.equalsIgnoreCase("MUTEMAP"))
    {
      if(this.getPlayer().getCurMap()==null)
        return;
      this.getPlayer().getCurMap().mute();
      String mess="";
      if(this.getPlayer().getCurMap().isMute())
        mess="Vous venez de muter la MAP.";
      else
        mess="Vous venez de demuter la MAP.";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("KICK"))
    {
      /*
      1 : Tu es resté trop longtemps inactif.
      2 : Ton personnage a atteint le niveau maximum autorisé
      3 : Pour des raisons de maintenance, le serveur va être coupé d'ici quelques minutes.
      4 : Votre connexion a été coupée pour des raisons de maintenance.
      5 : Retry connection (Oui ou Non)
      6 : Le nombre d'objets pour cet inventaire est déjà atteint.
      7 : Cette opération n'est pas autorisée ici.
      8 : Cet objetn 'est plus disponible.
       */

      Player player;
      String name,reason="";

      try
      {
        name=infos[1];
      }
      catch(Exception ignored)
      {
        this.sendErrorMessage("You need to give the name of the player !");
        return;
      }

      try
      {
        reason=msg.substring(infos[0].length()+infos[1].length()+1);
      }
      catch(Exception ignored)
      {
      }

      player=Main.world.getPlayerByName(name);

      if(player==null)
      {
        this.sendErrorMessage("The name of the player is invalid or non-existent !");
        return;
      }

      if(player.isOnline())
      {
        if(reason.isEmpty())
        {
          player.send("M018|"+this.getPlayer().getName()+";");
        }
        else
        {
          player.send("M018|"+this.getPlayer().getName()+";<br>"+reason);
        }
        if(player.getGameClient() != null)
        player.getGameClient().disconnect();
        this.sendSuccessMessage("The player have been kicked successfully.");
      }
      else
      {
        this.sendErrorMessage("The player isn't connected, verify the name please.");
      }
      return;
    }
    else if(command.equalsIgnoreCase("JAIL"))
    {
      short mapID=666;
      int cellID=getCellJail();
      if(mapID==-1||cellID==-1||Main.world.getMap(mapID)==null)
      {
        String str="MapID ou cellID invalide.";
        if(cellID==-1)
          str="cellID invalide.";
        else
          str="MapID invalide.";
        this.sendMessage(str);
        return;
      }
      if(Main.world.getMap(mapID).getCase(cellID)==null)
      {
        String str="cellID invalide.";
        this.sendMessage(str);
        return;
      }
      try
      {
        if(infos.length>1)//Si un nom de perso est specifie
        {
          Player perso=Main.world.getPlayerByName(infos[1]);
          if(perso.getGroupe()!=null)
          {
            String str="Il est interdit d'emprisonner un personnage ayant des droits.";
            this.sendMessage(str);
            return;
          }
          if(perso==null||perso.getFight()!=null)
          {
            String str="Le personnage n'a pas ete trouve ou est en combat.";
            this.sendMessage(str);
            return;
          }
          if(perso.isOnline())
            perso.teleport(mapID,cellID);
          else
            perso.teleportD(mapID,cellID);
          String str="Le joueur "+perso.getName()+" a ete teleporte emprisonne.";
          this.sendMessage(str);
        }
      }
      catch(Exception e)
      {
        this.sendMessage("Introuvable.");
        // ok
        return;
      }
      return;
    }
    else if(command.equalsIgnoreCase("UNJAIL"))
    {
      Player perso=Main.world.getPlayerByName(infos[1]);
      if(perso==null||perso.getFight()!=null)
      {
        String str="Le personnage n'a pas ete trouve ou est en combat.";
        this.sendMessage(str);
        return;
      }
      if(infos.length>1&&perso.isInPrison())//Si un nom de perso est specifie
      {
        perso.warpToSavePos();
        String str="Le joueur "+perso.getName()+" a ete teleporte e son point de sauvegarde.";
        this.sendMessage(str);
        return;
      }
      return;
    }
    else if(command.equalsIgnoreCase("BAN"))
    {
      Player player=Main.world.getPlayerByName(infos[1]);
      short days=0;

      try
      {
        days=Short.parseShort(infos[2]);
      }
      catch(Exception ignored)
      {
        this.sendMessage("You've not enter a day value (the time while the account is banned), the default value is unlimited.");
      }

      if(player==null)
      {
        this.sendErrorMessage("The player was not found, check the name please.");
        return;
      }
      if(player.getAccount().isBanned()) {
    	  this.sendErrorMessage("Deja ban.");
    	  return;  
      }
    	 
      if(player.getAccount()==null)
        Database.getStatics().getAccountData().load(player.getAccID());
      if(player.getAccount()==null)
      {
        this.sendErrorMessage("The account of the player was not found, contact a supervisor.");
        return;
      }
      if(player.getGroupe() != null) {
          if(player.getGroupe().getId() >= this.getPlayer().getGroupe().getId())
          return;
          }
      if(days != 0) {

      player.getAccount().setBanned(true);
      Database.getStatics().getAccountData().updateBannedTime(player.getAccount(),0);
      }
      else {
    	  player.getAccount().setBanned(true);
    	  Database.getStatics().getAccountData().updateBannedTime(player.getAccount(),0); 
      }
      if(player.getFight()==null)
      {
        if(player.getGameClient()!=null)
          player.getGameClient().disconnect();
      }
      else
      {
        SocketManager.send(player,"Im1201;"+this.getPlayer().getName());
      }
     
      this.sendSuccessMessage("You've kick and ban the player "+player.getName()+"(Acc: "+player.getAccount().getName()+") for "+(days==0 ? "unlimited" : days)+" day(s).");
      return;
    }
    else
    	 if(command.equalsIgnoreCase("ipco"))
    	    {
    		 String IP = null;
    		 try
    	      {
    	      IP = infos[1];
    	      }
    	      catch(Exception ignored)
    	      {
    	        this.sendMessage("You've not enter a value .");
    	      }
              if(!Main.world.IP_ALLOW.contains(IP)) {
            	  Main.world.IP_ALLOW += ";"+IP+";";
              }
              this.sendSuccessMessage(Main.world.IP_ALLOW.toString());
    	      this.sendSuccessMessage("Ip allowed");
    	      return;
    	    }
    	    else 
    	    	if(command.equalsIgnoreCase("BANACCOUNT"))
    {
      String mess="Le compte est introuvable";
      String A="";
      try
      {
        A=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }
      if(A.equalsIgnoreCase(""))
      {
        this.sendMessage("Il faut le nom de compte.");
        return;
      }
      for(Account account : Main.world.getAccounts())
      {
        if(account==null)
          continue;
        if(!account.getName().equalsIgnoreCase(A))
          continue;
        account.setBanned(true);
        Database.getStatics().getAccountData().update(account);
        mess="Vous avez banni le compte "+A;
        Player p=account.getCurrentPlayer();
        if(p!=null)
        {
          if(p.isOnline())
          {
            mess+=" dont le joueur est "+p.getName();
            if(p.getFight()==null)
            {
              if(p.getGameClient()!=null)
                p.getGameClient().disconnect();
            }
            else
            {
              SocketManager.send(p,"Im1201;"+this.getPlayer().getName());
            }
          }
        }
      }
      this.sendMessage(mess+".");
      return;
    }
    else if(command.equalsIgnoreCase("BANBYID"))
    {
      int ID=-1;
      String mess="Aucun personnage n'a ete trouve.";
      try
      {
        ID=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(ID<=0)
      {
        this.sendMessage("Une IP est necessaire.");
        return;
      }
      for(Player player : Main.world.getPlayers())
      {
        if(player==null)
          continue;
        if(player.getId()==ID)
        {
          if(player.getAccount()==null)
            Database.getStatics().getAccountData().load(player.getAccID());
          if(player.getAccount()==null)
          {
            this.sendMessage("Le personnage n'a pas de compte.");
            if(player.getGameClient()!=null)
              player.getGameClient().disconnect();
            return;
          }
          player.getAccount().setBanned(true);
          Database.getStatics().getAccountData().update(player.getAccount());
          if(player.getFight()==null)
          {
            if(player.getGameClient()!=null)
              player.getGameClient().disconnect();
          }
          else
          {
            SocketManager.send(player,"Im1201;"+this.getPlayer().getName());
          }
          mess="Vous avez banni "+player.getName()+".";
        }
      }
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("BANBYIP"))
    {
      String IP="";
      try
      {
        IP=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }

      if(IP.equalsIgnoreCase(""))
      {
        this.sendMessage("Une IP est necessaire.");
        return;
      }
      for(Entry<Integer, Account> entry : Main.world.getAccountsByIp(IP).entrySet())
      {
        Account a=entry.getValue();
        if(a==null)
          continue;
        if(!a.getLastIP().equalsIgnoreCase(IP))
          continue;

        a.setBanned(true);
        Database.getStatics().getAccountData().update(a);
        this.sendMessage("Le compte "+a.getName()+" a ete banni.");
        if(a.isOnline())
        {
          GameClient gc=a.getGameClient();
          if(gc==null)
            continue;
          this.sendMessage("Le joueur "+gc.getPlayer().getName()+" a ete kick.");
          gc.disconnect();
        }
      }
      Main.exchangeClient.send("SB"+IP);
      if(Database.getStatics().getBanIpData().add(IP))
        this.sendMessage("L'IP "+IP+" a ete banni.");
      return;
    }
    else if(command.equalsIgnoreCase("BANIP"))
    {
      Player P=null;
      try
      {
        P=Main.world.getPlayerByName(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }
      if(P==null)
      {
        this.sendMessage("Le personnage n'a pas ete trouve.");
        return;
      }
      if(P.getGroupe() != null) {
          if(P.getGroupe().getId() >= this.getPlayer().getGroupe().getId())
          return;
          }
      String IP=P.getAccount().getLastIP();
      if(IP.equalsIgnoreCase(""))
      {
        this.sendMessage("L'IP est invalide.");
        return;
      }
      Database.getStatics().getBanIpData().delete(IP);
      Database.getStatics().getBanIpData().add(IP);
      for(Entry<Integer, Account> entry : Main.world.getAccountsByIp(IP).entrySet())
      {
        Account a=entry.getValue();
        if(a==null)
          continue;
        if(!a.getLastIP().equalsIgnoreCase(IP))
          continue;

        a.setBanned(true);
        Database.getStatics().getAccountData().updateBannedTime(a,0);
        this.sendMessage("Le compte "+a.getName()+" a ete banni.");
        if(a.isOnline())
        {
          GameClient gc=a.getGameClient();
          if(gc==null)
            continue;
          if(gc.getPlayer() != null)
          this.sendMessage("Le joueur "+gc.getPlayer().getName()+" a ete kick.");
          gc.disconnect();
        }
      }
      Main.exchangeClient.send("SB"+IP);
      if(!Main.world.IP_BANNI.contains(IP)) {
    	  Main.world.IP_BANNI += ";"+IP+";";
      }
      if(Database.getStatics().getBanIpData().add(IP))
        this.sendMessage("L'IP "+IP+" a ete banni.");
      return;
    }
    else if(command.equalsIgnoreCase("SHOWITEM"))
    {
      Player perso=this.getPlayer();
      String name=null;
      try
      {
        name=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }

      perso=Main.world.getPlayerByName(name);
      if(perso==null)
      {
        String mess="Le personnage n'a pas ete trouve.";
        this.sendMessage(mess);
        return;
      }
      String mess="==========\n"+"Liste d'items sur le personnage :\n";
      this.sendMessage(mess);
      for(Entry<Integer, GameObject> entry : perso.getItems().entrySet())
      {
        mess=entry.getValue().getGuid()+" || "+entry.getValue().getTemplate().getName()+" || "+entry.getValue().getQuantity();
        this.sendMessage(mess);
      }

      this.sendMessage("Le personnage possede : "+perso.getKamas()+" Kamas.\n");
      mess="==========";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("SHOWBANK"))
    {
      Player perso=this.getPlayer();
      String name=null;
      try
      {
        name=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }

      perso=Main.world.getPlayerByName(name);
      if(perso==null)
      {
        String mess="Le personnage n'a pas ete trouve.";
        this.sendMessage(mess);
        return;
      }
      Account cBank=perso.getAccount();
      String mess="==========\n"+"Liste d'items dans la banque :";
      this.sendMessage(mess);
      for(GameObject entry : cBank.getBank())
      {
        mess=entry.getGuid()+" || "+entry.getTemplate().getName()+" || "+entry.getQuantity();
        this.sendMessage(mess);
      }
      this.sendMessage("Le personnage possede : "+cBank.getBankKamas()+" Kamas en banque.");
      mess="==========";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("SHOWSTORE"))
    {
      Player perso=this.getPlayer();
      String name=null;
      try
      {
        name=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }

      perso=Main.world.getPlayerByName(name);
      if(perso==null)
      {
        String mess="Le personnage n'a pas ete trouve.";
        this.sendMessage(mess);
        return;
      }
      String mess="==========\n"+"Liste d'items dans le Store :";
      this.sendMessage(mess);
      for(Entry<Integer, Integer> obj : perso.getStoreItems().entrySet())
      {
        GameObject entry=World.getGameObject(obj.getKey());
        mess=entry.getGuid()+" || "+entry.getTemplate().getName()+" || "+entry.getQuantity();
        this.sendMessage(mess);
      }

      mess="==========";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("SHOWMOUNT"))
    {
      Player perso=this.getPlayer();
      String name=null;
      try
      {
        name=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }

      perso=Main.world.getPlayerByName(name);
      if(perso==null)
      {
        String mess="Le personnage n'a pas ete trouve.";
        this.sendMessage(mess);
        return;
      }
      String mess="==========\n"+"Liste d'items dans la banque :";
      this.sendMessage(mess);
      if(perso.getMount()!=null)
      {
        for(Entry<Integer, GameObject> entry : perso.getMount().getObjects().entrySet())
        {
          mess=entry.getValue().getGuid()+" || "+entry.getValue().getTemplate().getName()+" || "+entry.getValue().getQuantity();
          this.sendMessage(mess);
        }
      }
      mess="==========";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("BLOCKTRADE"))
    {
      int i=-1;
      try
      {
        i=Short.parseShort(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(i==0)
      {
        Config.getInstance().tradeAsBlocked=false;
        this.sendMessage("Les échanges ont été débloqués.");
      }
      else if(i==1)
      {
        Config.getInstance().tradeAsBlocked=true;
        this.sendMessage("Tous les échanges sont bloqués.");
      }
      else
      {
        this.sendMessage("Aucune information.");
      }
      return;
    }
    else if(command.equalsIgnoreCase("ERASEALLMAP"))
    {
      for(GameMap map : Main.world.getMaps())
        map.delAllDropItem();
      this.sendMessage("Tous les objets sur toutes les maps ont été supprimés.");
      return;
    }
    else if(command.equalsIgnoreCase("ERASEMAP"))
    {
      this.getPlayer().getCurMap().delAllDropItem();
      this.sendMessage("Les objets de la map ont été supprimés.");
      return;
    }
    else if(command.equalsIgnoreCase("MORPH"))
    {
      int morphID=-9;
      try
      {
        morphID=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(morphID==-9)
      {
        String str="MorphID invalide.";
        this.sendMessage(str);
        return;
      }
      Player target=this.getPlayer();
      if(infos.length>2)//Si un nom de perso est specifie
      {
        target=Main.world.getPlayerByName(infos[2]);
        if(target==null)
        {
          String str="Le personnage n'a pas ete trouve.";
          this.sendMessage(str);
          return;
        }
      }
      if(morphID==-1)
      {
        morphID=target.getClasse()*10+target.getSexe();
        target.setGfxId(morphID);
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getCurMap(),target.getId());
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.getCurMap(),target);
        String str="Le joueur "+target.getName()+" a son apparence originale.";
        this.sendMessage(str);
        return;
      }
      else
      {
        target.setGfxId(morphID);
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getCurMap(),target.getId());
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.getCurMap(),target);
        String str="Le joueur "+target.getName()+" a ete transforme.";
        this.sendMessage(str);
        return;
      }
    }
    else if(command.equalsIgnoreCase("DEMORPHALL"))
    {
      for(Player player : Main.world.getOnlinePlayers())
      {
        player.setGfxId(player.getClasse()*10+player.getSexe());
      }
      this.sendMessage("Tous les joueurs connectes ont leur apparence originale.");
      return;
    }
    else if(command.equalsIgnoreCase("DEMORPH"))
        {
    	this.getPlayer().setGfxId(this.getPlayer().getClasse()*10+this.getPlayer().getSexe());
          this.sendMessage("ont leur apparence originale.");
          return;
        }
        else
      if(command.equalsIgnoreCase("ADDHONOR"))
    {
      int honor=0;
      try
      {
        honor=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      Player perso=this.getPlayer();
      String str="Vous avez ajoute "+honor+" points d'honneur e "+perso.getName()+".";
      if(perso.get_align()!=Constant.ALIGNEMENT_MERCENAIRE)
      {
        str="Le joueur n'est pas mercenaire ... l'action a ete annulee.";
        this.sendMessage(str);
        return;
      }
      perso.addHonor(honor);
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("HONOR"))
    {
      int honor=0;
      try
      {
        honor=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      Player perso=this.getPlayer();
      String str="Vous avez ajoute "+honor+" points d'honneur e "+perso.getName()+".";
      if(perso.get_align()==Constant.ALIGNEMENT_NEUTRE)
      {
        str="Le joueur est neutre ... l'action a ete annulee.";
        this.sendMessage(str);
        return;
      }
      perso.addHonor(honor);
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("NOAGRO"))
    {
      Player perso=this.getPlayer();
      String name=null;
      try
      {
        name=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }

      perso=Main.world.getPlayerByName(name);
      if(perso==null)
      {
        String mess="Le personnage n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      perso.setCanAggro(!perso.canAggro());
      String mess=perso.getName();
      if(perso.canAggro())
        mess+=" peut maintenant etre aggresse.";
      else
        mess+=" ne peut plus etre agresse.";
      this.sendMessage(mess);
      if(!perso.isOnline())
      {
        mess="Le personnage "+perso.getName()+" n'etait pas connecte.";
        this.sendMessage(mess);
      }
      return;
    }
    else if(command.equalsIgnoreCase("WHOIS"))
    {
    	if(Config.singleton.serverId == 22)
        	if(this.getPlayer().getGroupe().getId() >= 1 && this.getPlayer().getGroupe().getId() < 6)
        		return;
      String name="";
      Player perso=null;
      try
      {
        name=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }

      if(name=="")
        return;

      perso=Main.world.getPlayerByName(name);
      if(perso==null)
      {
        String mess="Le personnage n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      else if(perso.getAccount().getLastIP().equalsIgnoreCase(""))
      {
        String mess="Aucune IP.";
        this.sendMessage(mess);
        return;
      }
      java.util.Map<Integer, Account> accounts=Main.world.getAccountsByIp(perso.getAccount().getLastIP());
      String mess="Whois sur le joueur : "+name+"\n";
      mess+="Derniere IP : "+perso.getAccount().getLastIP()+"\n";
      int i=1;
      for(Entry<Integer, Account> entry : accounts.entrySet())
      {
        String persos="";
        Account a=entry.getValue();
        if(a==null)
          continue;
        for(Entry<Integer, Player> entry2 : a.getPlayers().entrySet())
        {
          perso=entry2.getValue();
          if(perso!=null)
          {
            if(persos.equalsIgnoreCase(""))
              persos+=perso.getName()+((perso.getGroupe()!=null) ? ":"+perso.getGroupe().getName() : "");
            else
              persos+=", "+perso.getName()+((perso.getGroupe()!=null) ? ":"+perso.getGroupe().getName() : "");
          }
        }
        if(!persos.equalsIgnoreCase(""))
        {
          mess+="["+i+"] "+a.getName()+" - "+persos+((a.isBanned()) ? " : banni" : "")+"\n";
          i++;
        }
      }
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("CLEANFIGHT"))
    {
      this.getPlayer().getCurMap().getFights().clear();
      this.sendMessage("Tous les combats de la map ont etes supprimes.");
      return;
    }
    else if(command.equalsIgnoreCase("ETATSERVER"))
    {
      int etat=1;
      try
      {
        etat=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }
      Main.gameServer.setState(etat);
      this.sendMessage("Vous avez change l'etat du serveur en "+etat+".");
      return;
    }
    else if(command.equalsIgnoreCase("MPTOTP"))
    {
      this.getPlayer().mpToTp=!this.getPlayer().mpToTp;
      String mess="";
      if(this.getPlayer().mpToTp)
        mess="Vous venez d'activer le MP to TP.";
      else
        mess="Vous venez de desactiver le MP to TP.";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("RETURNTP"))
    {
      for(Player perso : Main.world.getOnlinePlayers())
      {
        if(perso.thatMap==-1||perso.getFight()!=null)
          continue;
        perso.teleport((short)perso.thatMap,perso.thatCell);
        perso.thatMap=-1;
        perso.thatCell=-1;
      }
      this.sendMessage("Vous venez de renvoyer tous les joueurs e leur ancienne position.");
      return;
    }
    else if(command.equalsIgnoreCase("GETCASES"))
    {
      if(this.getPlayer().getCases)
      {
        this.sendMessage("Le getCases viens d'etre desactive :");
        String i="";
        for(Integer c : this.getPlayer().thisCases)
          i+=";"+c;
        this.sendMessage(i.substring(1));
        this.getPlayer().thisCases.clear();
      }
      else
        this.sendMessage("Le getCases viens d'etre active. Deplacez-vous sur la map pour capturer les cellules.");
      this.getPlayer().getCases=!this.getPlayer().getCases;
      return;
    }
    else if(command.equalsIgnoreCase("WALKFAST"))
    {
      if(this.getPlayer().walkFast)
        this.sendMessage("La marche instantanne viens d'etre desactive.");
      else
        this.sendMessage("La marche instantanne viens d'etre active.");
      this.getPlayer().walkFast=!this.getPlayer().walkFast;
      return;
    }
    else if(command.equalsIgnoreCase("LISTMAP"))
    {
      String data="";
      ArrayList<GameMap> i=Main.world.getMapByPosInArray(this.getPlayer().getCurMap().getX(),this.getPlayer().getCurMap().getY());
      for(GameMap map : i)
        data+=map.getId()+" | ";
      this.sendMessage(data);
      return;
    }
    else if(command.equalsIgnoreCase("DELINVENTORY"))
    {
      Player perso=null;
      infos=msg.split(" ",3);
      try
      {
        perso=Main.world.getPlayerByName(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(perso==null)
      {
        this.sendMessage("Le nom du personnage est incorrect.");
        return;
      }
      int i=0;
      ArrayList<GameObject> list=new ArrayList<GameObject>();

      list.addAll(perso.getItems().values());
      for(GameObject obj : list)
      {
        int guid=obj.getGuid();
        SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(perso,guid);
        perso.deleteItem(guid);
        i++;
      }

      this.sendMessage("Vous venez de supprimer "+i+" objets au joueur "+perso.getName()+".");
      return;
    }
    else if(command.equalsIgnoreCase("RMOBS"))
    {
      this.getPlayer().getCurMap().refreshSpawns();
      String mess="Les spawns de monstres sur la map ont etes rafraichit.";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("DELJOB"))
    {
      Player perso=this.getPlayer();
      infos=msg.split(" ",3);
      int job=-1;
      try
      {
        job=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      try
      {
        perso=Main.world.getPlayerByName(infos[2]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(perso==null)
      {
        this.sendMessage("Le nom du personnage est incorrect.");
        return;
      }
      if(job<1)
        return;
      JobStat jobStats=perso.getMetierByID(job);
      if(jobStats==null)
        return;
      perso.unlearnJob(jobStats.getId());
      SocketManager.GAME_SEND_STATS_PACKET(perso);
      Database.getStatics().getPlayerData().update(perso);
      SocketManager.GAME_SEND_MESSAGE(perso,"Vous venez d'apprendre un métier, veuillez vous reconnecter pour finaliser les changements.");
      this.sendMessage("You have remove profession "+job+" from character "+perso.getName()+".");
      return;
    }
    else if(command.equalsIgnoreCase("ADDTRIGGER"))
    {
      String args="";
      try
      {
        args=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }

      if(args.equals(""))
      {
        String str="Valeur invalide.";
        this.sendMessage(str);
        return;
      }

      this.getPlayer().getCurCell().addOnCellStopAction(0,args,"-1",null);
      boolean success=Database.getDynamics().getScriptedCellData().update(this.getPlayer().getCurMap().getId(),this.getPlayer().getCurCell().getId(),0,1,args,"-1");
      String str="";
      if(success)
        str="Le trigger a ete ajoute.";
      else
        str="Le trigger n'a pas ete ajoute.";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("DELTRIGGER"))
    {
      int cellID=-1;
      try
      {
        cellID=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(cellID==-1||this.getPlayer().getCurMap().getCase(cellID)==null)
      {
        String str="CellID invalide.";
        this.sendMessage(str);
        return;
      }
      this.getPlayer().getCurMap().getCase(cellID).clearOnCellAction();
      boolean success=Database.getDynamics().getScriptedCellData().delete(this.getPlayer().getCurMap().getId(),cellID);
      String str="";
      if(success)
        str="Le trigger a ete retire.";
      else
        str="Le trigger n'a pas ete retire.";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("SAVETHAT"))
    {
      this.getPlayer().thatMap=this.getPlayer().getCurMap().getId();
      this.getPlayer().thatCell=this.getPlayer().getCurCell().getId();
      this.sendMessage("Vous avez sauvegarde la map "+this.getPlayer().thatMap+" et la cellule "+this.getPlayer().thatCell+".");
      return;
    }
    else if(command.equalsIgnoreCase("APPLYTHAT"))
    {
      if(this.getPlayer().thatMap==-1||this.getPlayer().thatCell==-1)
      {
        this.sendMessage("Impossible d'ajouter le trigger, veuillez utiliser la commande SAVETHAT avant.");
        return;
      }
      this.getPlayer().getCurCell().addOnCellStopAction(0,this.getPlayer().thatMap+","+this.getPlayer().thatCell,"-1",null);
      Database.getDynamics().getScriptedCellData().update(this.getPlayer().getCurMap().getId(),this.getPlayer().getCurCell().getId(),0,1,this.getPlayer().thatMap+","+this.getPlayer().thatCell,"-1");
      this.sendMessage("REPLACE INTO `scripted_cells` VALUES ('"+this.getPlayer().getCurMap().getId()+"', '"+this.getPlayer().getCurCell().getId()+"','0','1','"+this.getPlayer().thatMap+","+this.getPlayer().thatCell+"','-1');"+"\nVous avez applique le trigger.");
      this.getPlayer().thatMap=-1;
      this.getPlayer().thatCell=-1;

      return;
    }
    else if(command.equalsIgnoreCase("STRIGGER"))
    {
      this.getPlayer().thatMap=this.getPlayer().getCurMap().getId();
      this.getPlayer().thatCell=this.getPlayer().getCurCell().getId();
      this.sendMessage("Vous avez sauvegarde la map "+this.getPlayer().thatMap+" et la cellule "+this.getPlayer().thatCell+".");
      return;
    }
    else if(command.equalsIgnoreCase("APTRIGGER"))
    {
      if(this.getPlayer().thatMap==-1||this.getPlayer().thatCell==-1)
      {
        this.sendMessage("Impossible d'ajouter le trigger, veuillez utiliser la commande STRIGGER avant.");
        return;
      }
      Main.world.getMap((short)this.getPlayer().thatMap).getCase(this.getPlayer().thatCell).addOnCellStopAction(0,this.getPlayer().getCurMap().getId()+","+this.getPlayer().getCurCell().getId(),"-1",null);
      Database.getDynamics().getScriptedCellData().update(this.getPlayer().thatMap,this.getPlayer().thatCell,0,1,this.getPlayer().getCurMap().getId()+","+this.getPlayer().getCurCell().getId(),"-1");
      this.getPlayer().thatMap=-1;
      this.getPlayer().thatCell=-1;
      this.sendMessage("Vous avez applique le trigger.");
      return;
    }
    else if(command.equalsIgnoreCase("INFOS"))
    {
      long uptime=System.currentTimeMillis()-Config.getInstance().startTime;
      int day=(int)(uptime/(1000*3600*24));
      uptime%=(1000*3600*24);
      int hour=(int)(uptime/(1000*3600));
      uptime%=(1000*3600);
      int min=(int)(uptime/(1000*60));
      uptime%=(1000*60);
      int sec=(int)(uptime/(1000));
  	int fake = 0;
	 long calcul=System.currentTimeMillis()-Config.getInstance().startTime;
	 if(calcul>1200000) {
		 if(Config.getInstance().serverId == 1 || Config.getInstance().serverId == 7)
       	fake = 100;
		 if(Config.getInstance().serverId == 22)
	        	fake = 20;
	 }
      String message="\n<u><b>Global informations system of emulator :</b></u>\n\n<u>Uptime :</u> "+day+"j "+hour+"h "+min+"m "+sec+"s.\n";
      message+="Online players         : "+(Main.gameServer.getClients().size()+fake)+"\n";
      message+="Unique online players  : "+Main.gameServer.getPlayersNumberByIp()+"\n";
      message+="Online clients         : "+(Main.gameServer.getClients().size()+fake)+"\n";

      int mb=1024*1024;
      Runtime instance=Runtime.getRuntime();
      message+="\n<u>Heap utilization statistics :</u>";
      message+="\nTotal Memory : "+instance.totalMemory()/mb+" Mo.";
      message+="\nFree Memory  : "+instance.freeMemory()/mb+" Mo.";
      message+="\nUsed Memory  : "+(instance.totalMemory()-instance.freeMemory())/mb+" Mo.";
      message+="\nMax Memory   : "+instance.maxMemory()/mb+" Mo.";
      message+="\n\n<u>Available processor :</u> "+instance.availableProcessors();
      Set<Thread> list=Thread.getAllStackTraces().keySet();
      int news=0,running=0,blocked=0,waiting=0,sleeping=0,terminated=0;
      for(Thread thread : list)
      {
        switch(thread.getState())
        {
          case NEW:
            news++;
            break;
          case RUNNABLE:
            running++;
            break;
          case BLOCKED:
            blocked++;
            break;
          case WAITING:
            waiting++;
            break;
          case TIMED_WAITING:
            sleeping++;
            break;
          case TERMINATED:
            news++;
            break;
        }
      }

      message+="\n\n<u>Informations of "+list.size()+" threads :</u> ";
      message+="\nNEW           : "+news;
      message+="\nRUNNABLE      : "+running;
      message+="\nBLOCKED       : "+blocked;
      message+="\nWAITING       : "+waiting;
      message+="\nTIMED_WAITING : "+sleeping;
      message+="\nTERMINATED    : "+terminated;

      this.sendMessage(message+"\n");

      if(infos.length>1)
      {
        message="List of all threads :\n";
        for(Thread thread : list)
          message+="- "+thread.getId()+" -> "+thread.getName()+" -> "+thread.getState().name().toUpperCase()+""+(thread.isDaemon() ? " (Daemon)" : "")+".\n";
        this.sendMessage(message);
      }
      return;
    }
    else if(command.equalsIgnoreCase("STARTFIGHT"))
    {
      if(this.getPlayer().getFight()==null)
      {
        this.sendMessage("Vous devez etre dans un combat.");
        return;
      }
      this.getPlayer().getFight().startFight();
      this.sendMessage("Le combat a ete demarre.");
      return;
    }
    else if(command.equalsIgnoreCase("ENDFIGHT"))
    {
      if(this.getPlayer().getFight()==null)
      {
        this.sendMessage("Le combat n'existe pas.");
        return;
      }
      int i=-1;
      try
      {
        i=Short.parseShort(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }
      if(i==0)
      {
        this.getPlayer().getFight().endFight(false,true);
        this.sendMessage("L'equipe des joueurs meurent !");
      }
      else if(i==1)
      {
        this.getPlayer().getFight().endFight(true,true);
        this.sendMessage("L'equipe des monstres meurent !");
      }
      else if(i == 3){
    	this.getPlayer().getFight().endTurn(false); 
      }
      else
      {
        this.sendMessage("Aucune information.");
      }
      return;
    }
    else if(command.equalsIgnoreCase("ENDFIGHTALL"))
    {
      try
      {
        for(GameClient client : Main.gameServer.getClients())
        {
        	if(client == null || client.getPlayer() == null)
        		continue;
          Player player=client.getPlayer();
          if(player==null)
            continue;
          Fight f=player.getFight();
          if(f==null)
            continue;
          try
          {
            if(f.getLaunchTime()>1)
              continue;
            f.endFight(true,true);
            this.sendMessage("Le combat de "+player.getName()+" a ete termine.");
          }
          catch(Exception e)
          {
            // ok
            this.sendMessage("Le combat de "+player.getName()+" a deje ete termine.");
          }
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
        this.sendMessage("Erreur lors de la commande endfightall : "+e.getMessage()+".");
      } finally
      {
        this.sendMessage("Tous les combats ont ete termines.");
      }
      return;
    }
    else 
    	if(command.equalsIgnoreCase("ENDFIGHTMAP"))
        {
          try
          {
            for(Player client : this.getPlayer().getCurMap().getPlayers())
            {
              Player player=client;
              if(player==null)
                continue;
              Fight f=player.getFight();
              if(f==null)
                continue;
              try
              {
                if(f.getLaunchTime()>1)
                  continue;
                f.endFight(true,true);
                this.sendMessage("Le combat de "+player.getName()+" a ete termine.");
              }
              catch(Exception e)
              {
                // ok
                this.sendMessage("Le combat de "+player.getName()+" a deje ete termine.");
              }
            }
          }
          catch(Exception e)
          {
            e.printStackTrace();
            this.sendMessage("Erreur lors de la commande endfightall : "+e.getMessage()+".");
          } finally
          {
            this.sendMessage("Tous les combats ont ete termines.");
          }
          return;
        }
        else if(command.equalsIgnoreCase("MAPINFO"))
    {
      String mess="==========\n"+"Liste des PNJs de la Map :";
      this.sendMessage(mess);
      GameMap map=this.getPlayer().getCurMap();
      for(Entry<Integer, Npc> entry : map.getNpcs().entrySet())
      {
        mess=entry.getKey()+" | "+entry.getValue().getTemplate().getId()+" | "+entry.getValue().getCellid()+" | "+entry.getValue().getTemplate().getInitQuestionId(this.getPlayer().getCurMap().getId());
        this.sendMessage(mess);
      }
      mess="Liste des groupes de monstres :";
      this.sendMessage(mess);
      for(Entry<Integer, MobGroup> entry : map.getMobGroups().entrySet())
      {
        mess=entry.getKey()+" | "+entry.getValue().getCellId()+" | "+entry.getValue().getAlignement()+" | "+entry.getValue().getMobs().size();
        this.sendMessage(mess);
      }
      mess="==========";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("UNBANIP"))
    {
      Player perso=null;
      try
      {
        perso=Main.world.getPlayerByName(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }
      if(perso==null)
      {
        this.sendMessage("Le nom du personnage n'est pas bon.");
        return;
      }
      if(Database.getStatics().getBanIpData().delete(perso.getAccount().getCurrentIp()))
      {
        this.sendMessage("L'IP a ete debanni.");
        return;
      }
      return;
    }
    else if(command.equalsIgnoreCase("UNBAN"))
    {
      Player P=Main.world.getPlayerByName(infos[1]);
      if(P==null)
      {
        this.sendMessage("Personnage non trouve.");
        return;
      }
      if(P.getAccount()==null)
        Database.getStatics().getAccountData().load(P.getAccID());
      if(P.getAccount()==null)
      {
        this.sendMessage("Le personnage n'a pas de compte.");
        return;
      }
      P.getAccount().setBanned(false);
      Database.getStatics().getAccountData().updateBannedTime(P.getAccount(),0);
      this.sendMessage("Vous avez debanni "+P.getName()+".");
      return;
    }
    else if(command.equalsIgnoreCase("EXIT"))
    {
    	 this.sendMessage("Lancement du reboot.");
         Main.stop("Exit by administrator");
      return;
    }
  
    else if(command.equalsIgnoreCase("SAVE")&&!Main.isSaving)
    {
      Thread thread=new Thread(() -> WorldSave.cast(1));
      thread.setName(WorldSave.class.getName());
      thread.setDaemon(true);
      thread.start();
      String mess="Sauvegarde lancee!";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("LEVEL"))
    {
      int count=0;
      try
      {
        count=Integer.parseInt(infos[1]);
        if(count<1)
          count=1;
        if(count>Main.world.getExpLevelSize())
          count=Main.world.getExpLevelSize();
        Player perso=this.getPlayer();
        if(infos.length==3)//Si le nom du perso est specifie
        {
          String name=infos[2];
          perso=Main.world.getPlayerByName(name);
          if(perso==null)
            perso=this.getPlayer();
        }
        if(perso.getLevel()<count)
        {
          while(perso.getLevel()<count)
            perso.levelUp(false,true);
          if(perso.isOnline())
          {
            SocketManager.GAME_SEND_SPELL_LIST(perso);
            SocketManager.GAME_SEND_NEW_LVL_PACKET(perso.getGameClient(),perso.getLevel());
            SocketManager.GAME_SEND_STATS_PACKET(perso);
          }
        }
        String mess="Vous avez fixe le niveau de "+perso.getName()+" e "+count+".";
        this.sendMessage(mess);
      }
      catch(Exception e)
      {
        // ok
        this.sendMessage("Valeur incorecte.");
        return;
      }
      return;
    }
    else if(command.equalsIgnoreCase("KAMAS"))
    {
      int count=0;
      try
      {
        count=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
        this.sendMessage("Valeur incorecte.");
        return;
      }
      if(count==0)
      {
        this.sendMessage("Valeur inutile.");
        return;
      }
      Player perso=this.getPlayer();
      long curKamas=perso.getKamas();
      long newKamas=curKamas+count;
      if(newKamas<0)
        newKamas=0;
      perso.setKamas(newKamas);
      Main.world.kamas_total += count;
      if(perso.isOnline())
        SocketManager.GAME_SEND_STATS_PACKET(perso);
      String mess="Vous avez ";
      mess+=(count<0 ? "retire" : "ajoute")+" ";
      mess+=Math.abs(count)+" kamas e "+perso.getName()+".";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("ITEMSET"))
    {
      int tID=0;
      try
      {
        tID=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      ObjectSet IS=Main.world.getItemSet(tID);
      if(tID==0||IS==null || tID == 165)
      {
        String mess="La panoplie "+tID+" n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      boolean useMax=false;
      for(ObjectTemplate t : IS.getItemTemplates())
      {
        GameObject obj=t.createNewItem(1,useMax);
        if(this.getPlayer().addObjet(obj,true))//Si le joueur n'avait pas d'item similaire
          World.addGameObject(obj,true);
      }
      String str="Creation de la panoplie "+tID+" reussie";
      if(useMax)
        str+=" avec des stats maximums";
      str+=".";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("ITEM")||command.equalsIgnoreCase("!getitem"))
    {
      int tID=0;
      try
      {
        tID=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(tID==0)
      {
        String mess="Le template "+tID+" n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      int qua=1;
      if(infos.length==3)//Si une quantite est specifiee
      {
        try
        {
          qua=Integer.parseInt(infos[2]);
        }
        catch(Exception e)
        {
          // ok
        }
      }
      Player target = this.getPlayer();
      if(infos.length >= 4)
      {
          Player findHim = Main.world.getPlayerByName(infos[3]);
          if(findHim != null)
          {
              target = findHim;
          }
      }
      boolean useMax=false;
      ObjectTemplate t=Main.world.getObjTemplate(tID);
      if(t==null)
      {
        String mess="Le template "+tID+" n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      if(tID==25540 || tID==25541 || tID==25542 || tID==25543 || tID == 12324 || tID == 101620)
      {
        String mess="Le template "+tID+" n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      if(t.boutique == 1 || t.getLevel() > 150 || t.getPoints() != 0)
    	  qua=1;
      if(this.getPlayer().getGroupe().getId() != 7)
      if(t.getLevel() < 80 && qua > 50)
    	  qua=50; 
      if(t.getType()==Constant.ITEM_TYPE_OBJET_ELEVAGE&&(t.getStrTemplate().isEmpty()||t.getStrTemplate().equalsIgnoreCase("")))
      {
        this.sendMessage("Impossible de creer l'item d'elevage. Le StrTemplate ("+tID+") est vide.");
        return;
      }
      if(qua<1)
        qua=1;
      GameObject obj=t.createNewItem(qua,useMax);

      if(t.getType() == Constant.ITEM_TYPE_CERTIF_MONTURE) {
          //obj.setMountStats(this.getPlayer(), null);
          Mount mount = new Mount(Constant.getMountColorByParchoTemplate(obj.getTemplate().getId()), target.getId(), false);
          obj.clearStats();
          obj.getStats().addOneStat(995, - (mount.getId()));
          obj.getTxtStat().put(996, target.getName());
          obj.getTxtStat().put(997, mount.getName());
          mount.setToMax();
          // Pour générer une monture niveau 100 :
          mount.level = 100;
          mount.energy = 2000;
          mount.stats = Constant.getMountStats(mount.color, mount.level);
      }
      if(this.getPlayer().addObjet(obj,true))//Si le joueur n'avait pas d'item similaire
        World.addGameObject(obj,true);
      String str="Creation de l'item "+tID+" reussie";
      if(useMax)
        str+=" avec des stats maximums";
      str+=".";
      this.sendMessage(str);
      SocketManager.GAME_SEND_Ow_PACKET(this.getPlayer());
      Database.getStatics().getPlayerData().logs_gm(this.getPlayer().getName(),t.getName() , this.getAccount().getCurrentIp() , this.getAccount().getName(),this.getPlayer().getCurMap().getId());
      
      return;
    }
    else if(command.equalsIgnoreCase("SPELLPOINT"))
    {
      int pts=-1;
      try
      {
        pts=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(pts==-1)
      {
        String str="Valeur invalide.";
        this.sendMessage(str);
        return;
      }
      Player perso=this.getPlayer();

      perso.addSpellPoint(pts);
      SocketManager.GAME_SEND_STATS_PACKET(perso);
      String str="Vous avez ajoute "+pts+" points de sorts e "+perso.getName()+".";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("UTILITY"))
    {
      switch(infos[1].toUpperCase())
      {
        case "COLLECTOR":
          switch(infos[2].toUpperCase())
          {
            case "GET":
              Collector collector=Main.world.getCollector(Integer.parseInt(infos[3]));
              if(collector==null||collector.getInFight()>0||collector.getExchange()||collector.getMap()!=this.getPlayer().getCurMap().getId())
                return;
              collector.setExchange(true);
              SocketManager.GAME_SEND_ECK_PACKET(this.getClient(),8,collector.getId()+"");
              SocketManager.GAME_SEND_ITEM_LIST_PACKET_PERCEPTEUR(this.getClient(),collector);
              this.getPlayer().setExchangeAction(new ExchangeAction<>(ExchangeAction.TRADING_WITH_COLLECTOR,collector.getId()));
              this.getPlayer().DialogTimer();
              break;
            default:
              final StringBuilder message=new StringBuilder("All id of collectors present on the map "+this.getPlayer().getCurMap().getId()+" :<br>");
              Main.world.getCollectors().values().stream().filter(collector1 -> collector1.getMap()==this.getPlayer().getCurMap().getId()).forEach(collector1 -> message.append("> ").append(collector1.getId()).append(" | ").append(collector1.getDate()).append(" | ").append(Main.world.getGuild(collector1.getGuildId())!=null ? Main.world.getGuild(collector1.getGuildId()).getName() : "Unknow").append("<br>"));
              this.sendMessage(message.toString());
              break;
          }
          break;
        case "DEBUG":

          break;
      }
    }
    else if(command.equalsIgnoreCase("LSPELL"))
    {
      int spell=-1;
      try
      {
        spell=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(spell==-1)
      {
        String str="Valeur invalide.";
        this.sendMessage(str);
        return;
      }
      Player perso=this.getPlayer();
      perso.learnSpell(spell,1,true,true,true);
      String str="Le sort "+spell+" a ete appris e "+perso.getName()+".";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("CAPITAL"))
    {
      int pts=-1;
      try
      {
        pts=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(pts==-1)
      {
        String str="Valeur invalide.";
        this.sendMessage(str);
        return;
      }
      Player perso=this.getPlayer();

      perso.addCapital(pts);
      SocketManager.GAME_SEND_STATS_PACKET(perso);
      String str="Vous avez ajoute "+pts+" points de capital e "+perso.getName()+".";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("ALIGN"))
    {
      byte align=-1;
      try
      {
        align=Byte.parseByte(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(align<Constant.ALIGNEMENT_NEUTRE||align>Constant.ALIGNEMENT_MERCENAIRE)
      {
        String str="Valeur invalide.";
        this.sendMessage(str);
        return;
      }
      Player perso=this.getPlayer();
      if(infos.length>2)//Si un nom de perso est specifie
      {
        perso=Main.world.getPlayerByName(infos[2]);
        if(perso==null)
        {
          String str="Le personnage n'a pas ete trouve.";
          this.sendMessage(str);
          return;
        }
      }
      perso.modifAlignement(align);
      String a="";
      if(align==0)
        a="neutre";
      else if(align==1)
        a="bontarien";
      else if(align==2)
        a="brakmarien";
      else if(align==3)
        a="serianne";
      String str="L'alignement du joueur a ete modifie en "+a+".";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("LIFE"))
    {
      int count=0;
      try
      {
        count=Integer.parseInt(infos[1]);
        if(count<0)
          count=0;
        if(count>100)
          count=100;
        Player perso=this.getPlayer();
        if(infos.length==3)//Si le nom du perso est specifie
        {
          String name=infos[2];
          perso=Main.world.getPlayerByName(name);
          if(perso==null)
            perso=this.getPlayer();
        }
        int newPDV=perso.getMaxPdv()*count/100;
        perso.setPdv(newPDV);
        if(perso.isOnline())
          SocketManager.GAME_SEND_STATS_PACKET(perso);
        String mess="Vous avez fixe le pourcentage de vitalite de "+perso.getName()+" e "+count+"%.";
        this.sendMessage(mess);
      }
      catch(Exception e)
      {
        // ok
        this.sendMessage("Valeur incorecte.");
        return;
      }
      return;
    }
    else if(command.equalsIgnoreCase("XPJOB"))
    {
      int job=-1;
      int xp=-1;
      try
      {
        job=Integer.parseInt(infos[1]);
        xp=Integer.parseInt(infos[2]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(job==-1||xp<0)
      {
        String str="Valeurs invalides.";
        this.sendMessage(str);
        return;
      }
      Player perso=this.getPlayer();
      if(infos.length>3)//Si un nom de perso est specifie
      {
        perso=Main.world.getPlayerByName(infos[3]);
        if(perso==null)
        {
          String str="Le personnage n'a pas ete trouv.e";
          this.sendMessage(str);
          return;
        }
      }
      JobStat SM=perso.getMetierByID(job);
      if(SM==null)
      {
        String str="Le joueur ne possede pas le metier demande.";
        this.sendMessage(str);
        return;
      }
      SM.addXp(perso,xp);
      ArrayList<JobStat> SMs=new ArrayList<JobStat>();
      SMs.add(SM);
      SocketManager.GAME_SEND_JX_PACKET(perso,SMs);
      String str="Vous avez ajoute "+xp+" points d'experience au metier "+job+" de "+perso.getName()+".";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("LJOB"))
    {
      int job=-1;
      try
      {
        job=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(job==-1||Main.world.getMetier(job)==null)
      {
        String str="Valeur invalide.";
        this.sendMessage(str);
        return;
      }
      Player perso=this.getPlayer();
      if(infos.length>2)//Si un nom de perso est specifie
      {
        perso=Main.world.getPlayerByName(infos[2]);
        if(perso==null)
        {
          String str="Le personnage n'a pas ete trouve.";
          this.sendMessage(str);
          return;
        }
      }
      perso.learnJob(Main.world.getMetier(job));
      String str="Le metier "+job+" a ete appris e "+perso.getName()+".";
      this.sendMessage(str);
      return;
    }
    else
    	if(command.equalsIgnoreCase("PVPWINE"))
        {
          int numbre=-1;
          try
          {
            numbre=Integer.parseInt(infos[1]);
          }
          catch(Exception e)
          {
        	  numbre = 0;
          }

          Player perso=this.getPlayer();
          if(infos.length>2)//Si un nom de perso est specifie
          {
            perso=Main.world.getPlayerByName(infos[2]);
            if(perso==null)
            {
              String str="Le personnage n'a pas ete trouve.";
              this.sendMessage(str);
              return;
            }
          }
          perso.Song = numbre;
          World.get_Succes(perso.getId()).pvp_add_set(perso, numbre);
          String str="change modifier "+numbre+" a "+perso.getName()+".";
          this.sendMessage(str);
          return;
        }
        else
        	if(command.equalsIgnoreCase("PVPLOSE"))
            {
              int numbre=-1;
              try
              {
                numbre=Integer.parseInt(infos[1]);
              }
              catch(Exception e)
              {
            	  numbre = 0;
              }

              Player perso=this.getPlayer();
              if(infos.length>2)//Si un nom de perso est specifie
              {
                perso=Main.world.getPlayerByName(infos[2]);
                if(perso==null)
                {
                  String str="Le personnage n'a pas ete trouve.";
                  this.sendMessage(str);
                  return;
                }
              }
              perso.Song = numbre;
              World.get_Succes(perso.getId()).pvp_lose_set(perso, numbre);
              String str="change modifier "+numbre+" a "+perso.getName()+".";
              this.sendMessage(str);
              return;
            }
            else
            	if(command.equalsIgnoreCase("KOLIWINE"))
                {
                  int numbre=-1;
                  try
                  {
                    numbre=Integer.parseInt(infos[1]);
                  }
                  catch(Exception e)
                  {
                	  numbre = 0;
                  }

                  Player perso=this.getPlayer();
                  if(infos.length>2)//Si un nom de perso est specifie
                  {
                    perso=Main.world.getPlayerByName(infos[2]);
                    if(perso==null)
                    {
                      String str="Le personnage n'a pas ete trouve.";
                      this.sendMessage(str);
                      return;
                    }
                  }
                  perso.Song = numbre;
                  World.get_Succes(perso.getId()).koli_wine_set(perso, numbre);
                  String str="change modifier "+numbre+" a "+perso.getName()+".";
                  this.sendMessage(str);
                  return;
                }
                else
            	if(command.equalsIgnoreCase("KOLILOSE"))
                {
                  int numbre=-1;
                  try
                  {
                    numbre=Integer.parseInt(infos[1]);
                  }
                  catch(Exception e)
                  {
                	  numbre = 0;
                  }

                  Player perso=this.getPlayer();
                  if(infos.length>2)//Si un nom de perso est specifie
                  {
                    perso=Main.world.getPlayerByName(infos[2]);
                    if(perso==null)
                    {
                      String str="Le personnage n'a pas ete trouve.";
                      this.sendMessage(str);
                      return;
                    }
                  }
                  perso.Song = numbre;
                  World.get_Succes(perso.getId()).koli_lose_set(perso, numbre);
                  String str="change modifier "+numbre+" a "+perso.getName()+".";
                  this.sendMessage(str);
                  return;
                }
                else
    	if(command.equalsIgnoreCase("SONGE"))
        {
          int idsonge=-1;
          try
          {
            idsonge=Integer.parseInt(infos[1]);
          }
          catch(Exception e)
          {
        	  idsonge = 0;
          }

          Player perso=this.getPlayer();
          if(infos.length>2)//Si un nom de perso est specifie
          {
            perso=Main.world.getPlayerByName(infos[2]);
            if(perso==null)
            {
              String str="Le personnage n'a pas ete trouve.";
              this.sendMessage(str);
              return;
            }
          }
          perso.Song = idsonge;
          String str="change modifier "+idsonge+" a "+perso.getName()+".";
          this.sendMessage(str);
          return;
        }
        else
        	if(command.equalsIgnoreCase("UNLSPELL"))
    {
      Player perso=this.getPlayer();
      if(infos.length>2)//Si un nom de perso est specifie
      {
        perso=Main.world.getPlayerByName(infos[2]);
        if(perso==null)
        {
          String str="Le personnage n'a pas ete trouve.";
          this.sendMessage(str);
          return;
        }
      }
      perso.setExchangeAction(new ExchangeAction<>(ExchangeAction.FORGETTING_SPELL,0));
      SocketManager.GAME_SEND_FORGETSPELL_INTERFACE('+',perso);
      return;
    }
    else if(command.equalsIgnoreCase("SPAWN"))
    {
      String Mob=null;
      try
      {
        Mob=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }

      if(Mob==null)
      {
        this.sendMessage("Les parametres sont invalides.");
        return;
      }
      this.getPlayer().getCurMap().spawnGroupOnCommand(this.getPlayer().getCurCell().getId(),Mob,true);
      this.sendMessage("Vous avez ajoute un groupe de monstres.");
      return;
    }
     // modif item template
    else if(command.equalsIgnoreCase("SET_STATS_MODELO")){
        int idInt = 0;
        Player perso=this.getPlayer();
       String str = "";
        try {
          idInt = Integer.parseInt(infos[1]);
        } catch (final Exception e) {
          SocketManager.ENVIAR_BAT2_CONSOLA(perso, "Wrong Arguments");
          return;
        }
        try {
          str = infos[2];
        } catch (final Exception e) {}
        ObjectTemplate objMod = Main.world.getObjTemplate(idInt);
        if (objMod == null) {
          SocketManager.ENVIAR_BAT2_CONSOLA(perso, "Item Null");
          return;
        }
        SocketManager.ENVIAR_BAT2_CONSOLA(perso, "\n"
        		+ "Les stats on été modifié dans la bdd Template" + objMod.getName()
                + ": \nAncien Stats - " + objMod.getStrTemplate() + "\nNouveau Stats - " + str);
        objMod.setStrTemplate(str);
        Database.getDynamics().getObjectTemplateData().UPDATE_STATS_OBJETO_MODELO(idInt, str);
        try {
          for (final NpcTemplate npcMod : Main.world.npcsTemplate.values()) {
            if (npcMod.getAllItem().contains(objMod)) {
              npcMod.actualizarObjetosAVender();
            }
          }
        } catch (final Exception e) {
          SocketManager.ENVIAR_BAT2_CONSOLA(perso, "Exception happend");
        }
  }
     // modif stats item
    else if(command.equalsIgnoreCase("SET_STATS_OBJETO")){
        int idInt = 0;
        Player perso=this.getPlayer();
        String str = "";
        try {
          try {
            idInt = Integer.parseInt(infos[1]);
          } catch (final Exception e) {
            SocketManager.ENVIAR_BAT2_CONSOLA(perso, "Arguments incorrects");
            return;
          }
          final GameObject obj = Main.world.getGameObject(idInt);
          if (obj == null) {
            SocketManager.ENVIAR_BAT2_CONSOLA(perso, "Objeto nulo");
            return;
          }
          try {
            str = infos[2];
          } catch (final Exception e) {}
          SocketManager.ENVIAR_BAT2_CONSOLA(perso, "\n"
          		+ "Modifier les stats d'un objet " + idInt + ": \n\n"
          				+ "Anciennes stats - " + obj
                  .parseStatsString() + "\nNouvelles Stats - " + str);
          obj.resetEffects();
          obj.parseStringToStats(str, true, false);
          if (perso.getObjects().get(idInt) != null) {
            if (perso.isOnline()) {
              perso.refreshStats();
              SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(perso, obj);
              SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(perso, obj);
              SocketManager.GAME_SEND_ON_EQUIP_ITEM(perso.getCurMap(), perso);
              SocketManager.GAME_SEND_STATS_PACKET(perso);
              Database.getDynamics().getObjectData().update(obj);
            }
          }
        } catch (Exception e) {
          SocketManager.ENVIAR_BAT2_CONSOLA(perso, "Argumentos invalidos");
        }
      }
    else if(command.equalsIgnoreCase("SPAWNFIX"))
    {
      String Mob=null;
      try
      {
        Mob=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }

      if(Mob==null)
      {
        this.sendMessage("Les parametres sont invalides.");
        return;
      }
      this.getPlayer().getCurMap().spawnGroupOnCommandfix(this.getPlayer().getCurCell().getId(),Mob,true);
      this.sendMessage("Vous avez ajoute un groupe de monstres.");
      return;
    }
    else if(command.equalsIgnoreCase("SHUTDOWN"))
    {
      int time=30,OffOn=0;
      try
      {
        OffOn=Integer.parseInt(infos[1]);
        time=Integer.parseInt(infos[2]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(OffOn==1&&this.isTimerStart())// demande de demarer le reboot
      {
        this.sendMessage("Un reboot est déjà programmé.");
        return;
      }
      else if(OffOn==1&&!this.isTimerStart())
      {
        if(time<=15)
        {
          for(Player player : Main.world.getOnlinePlayers())
          {
        	  SocketManager.PACKET_POPUP_DEPART(player, "Vous ne pouvez pas lancer de combats car le serveur redémarrera bientét.");
            //player.sendServerMessage("Vous ne pouvez pas lancer de combats car le serveur redémarrera bientét.");
            //player.send("M13");
          }
          Config.getInstance().fightAsBlocked=true;
        }
        this.setTimer(createTimer(time));
        this.getTimer().start();
        this.setTimerStart(true);
        String timeMSG="minutes";
        if(time<=1)
          timeMSG="minute";
        SocketManager.GAME_SEND_Im_PACKET_TO_ALL("115;"+time+" "+timeMSG);
        if(this.getPlayer() != null)
        this.sendMessage("Reboot programmé.");
       
      }
      else if(OffOn==0&&this.isTimerStart())
      {
        this.getTimer().stop();
        this.setTimerStart(false);
        for(Player player : Main.world.getOnlinePlayers())
          player.sendServerMessage("Le redémarrage a été annulé. Vous pouvez recommencer les combats.");
        Config.getInstance().fightAsBlocked=false;
        this.sendMessage("Reboot arrêté.");
      }
      else if(OffOn==0&&!this.isTimerStart())
      {
        this.sendMessage("Aucun reboot n'est lancé.");
      }
      return;
    }
    else if(command.equalsIgnoreCase("LINEM"))
    {
      String line="|";
      for(String split : infos[1].split(","))
      {
        int id=Integer.parseInt(split);
        Monster monster=Main.world.getMonstre(id);

        for(MobGrade mobGrade : monster.getGrades().values())
          line+=monster.getId()+","+mobGrade.getLevel()+"|";
      }
      this.sendMessage(line);
      return;
    }
    else if(command.equalsIgnoreCase("ENERGIE"))
    {
      try
      {
        Player perso=this.getPlayer();
        String name=null;
        name=infos[2];
        perso=Main.world.getPlayerByName(name);
        int jet=Integer.parseInt(infos[1]);
        int EnergyTotal=perso.getEnergy()+jet;
        if(EnergyTotal>10000)
          EnergyTotal=10000;
        perso.setEnergy(EnergyTotal);
        SocketManager.GAME_SEND_STATS_PACKET(perso);
        this.sendMessage("Vous avez fixe l'energie de "+perso.getName()+" e "+EnergyTotal+".");
        return;
      }
      catch(Exception e)
      {

      }
      return;
    }
    else if(command.equalsIgnoreCase("RES"))
    {
      Player perso=this.getPlayer();
      perso=Main.world.getPlayerByName(infos[1]);
      if(perso==null)
      {
        String mess="Le personnage n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      if(perso.getFight()!=null)
      {
        this.sendMessage("Le personnage est en combat.");
        return;
      }
      if(perso.isOnline())
      {
        this.sendMessage("Vous avez ramene e la vie "+perso.getName()+".");
        perso.setAlive();
      }
      else
        this.sendMessage("Le personnage n'est pas connecte.");
      return;
    }
    else if(command.equalsIgnoreCase("KICKALL"))
    {
      this.sendMessage("Tout le monde va etre kicke.");
      Main.gameServer.kickAll();
      return;
    }
    else if(command.equalsIgnoreCase("RESET"))
    {
      Player perso=this.getPlayer();
      if(infos.length>1)
      {
        perso=Main.world.getPlayerByName(infos[1]);
      }
      if(perso==null)
      {
        String mess="Le personnage n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      perso.getStats().addOneStat(125,-perso.getStats().getEffect(125));
      perso.getStats().addOneStat(124,-perso.getStats().getEffect(124));
      perso.getStats().addOneStat(118,-perso.getStats().getEffect(118));
      perso.getStats().addOneStat(123,-perso.getStats().getEffect(123));
      perso.getStats().addOneStat(119,-perso.getStats().getEffect(119));
      perso.getStats().addOneStat(126,-perso.getStats().getEffect(126));
      perso.getStatsParcho().getMap().clear();
      perso.addCapital((perso.getLevel()-1)*5-perso.get_capital());
      SocketManager.GAME_SEND_STATS_PACKET(perso);
      this.sendMessage("Vous avez restat "+perso.getName()+".");
      return;
    }
    else if(command.equalsIgnoreCase("RENAMEPERSO"))
    {
      Player perso=this.getPlayer();
      if(infos.length>1)
        perso=Main.world.getPlayerByName(infos[1]);
      if(perso==null)
      {
        String mess="Le personnage n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      if(Main.world.getPlayerByName(infos[2])!=null)
      {
        String mess="Le personnage "+infos[2]+" existe déjà.";
        this.sendMessage(mess);
        
        return;
      }
      String name=perso.getName();
      perso.setName(infos[2]);
      Database.getStatics().getPlayerData().update(perso);
      SocketManager.GAME_SEND_STATS_PACKET(perso);
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(),perso.getId());
      SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(),perso);
      this.sendMessage("Vous avez renomme "+name+" en "+perso.getName()+".");
      return;
    }
    else if(command.equalsIgnoreCase("RENAMEGUILDE"))
    {
      String ancName="";
      String newName="";
      int idGuild=-1;
      if(infos.length>1)
        ancName=infos[1];
      newName=infos[2];
      idGuild=Main.world.getGuildByName(ancName);
      if(idGuild==-1)
      {
        String mess="La guilde n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      Main.world.getGuild(idGuild).setName(newName);
      this.sendMessage("Vous avez renomme la guilde en "+newName+".");
      return;
    }
    else if(command.equalsIgnoreCase("A"))
    {
      infos=msg.split(" ",2);
      String prefix="<b>Server</b>";
      SocketManager.GAME_SEND_Im_PACKET_TO_ALL("116;"+prefix+"~"+infos[1]);
      this.sendMessage("Vous avez envoye un message e tout le serveur.");
      return;
    }
    else if(command.equalsIgnoreCase("MOVEMOB"))
    {
      this.getPlayer().getCurMap().onMapMonsterDeplacement();
      this.sendMessage("Vous avez deplace un groupe de monstres.");
      return;
    }
    else if(command.equalsIgnoreCase("SHOWPOINTS"))
    {
      Player perso=this.getPlayer();
      if(infos.length>1)
        perso=Main.world.getPlayerByName(infos[1]);
      if(perso==null)
      {
        String mess="Le personnage n'existe pas.";
        this.sendMessage(mess);
        return;
      }
      this.sendMessage(perso.getName()+" possede "+perso.getAccount().getPoints()+" points boutique.");
      return;
    }
    else if(command.equalsIgnoreCase("ADDNPC"))
    {
      int id=0;
      try
      {
        id=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(id==0||Main.world.getNPCTemplate(id)==null)
      {
        String str="NpcID invalide.";
        this.sendMessage(str);
        return;
      }
      Npc npc=this.getPlayer().getCurMap().addNpc(id,this.getPlayer().getCurCell().getId(),this.getPlayer().get_orientation());
      SocketManager.GAME_SEND_ADD_NPC_TO_MAP(this.getPlayer().getCurMap(),npc);
      String str="Le PNJ a ete ajoute";
      if(this.getPlayer().get_orientation()==0||this.getPlayer().get_orientation()==2||this.getPlayer().get_orientation()==4||this.getPlayer().get_orientation()==6)
        str+=" mais est invisible (orientation diagonale invalide)";
      str+=".";
      if(Database.getDynamics().getNpcData().addOnMap(this.getPlayer().getCurMap().getId(),id,this.getPlayer().getCurCell().getId(),this.getPlayer().get_orientation(),false))
        this.sendMessage(str);
      else
        this.sendMessage("Erreur lors de la sauvegarde de la position.");
      return;
    }
    else if(command.equalsIgnoreCase("DELNPC"))
    {
      int id=0;
      try
      {
        id=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      Npc npc=this.getPlayer().getCurMap().getNpc(id);
      if(id==0||npc==null)
      {
        String str="Npc GUID invalide.";
        this.sendMessage(str);
        return;
      }
      int exC=npc.getCellid();
      //on l'efface de la map
      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this.getPlayer().getCurMap(),id);
      this.getPlayer().getCurMap().removeNpcOrMobGroup(id);

      String str="Le PNJ a ete supprime.";
      if(Database.getDynamics().getNpcData().delete(this.getPlayer().getCurMap().getId(),exC))
        this.sendMessage(str);
      else
        this.sendMessage("Erreur lors de la sauvegarde de la position.");
      return;
    }
    else if(command.equalsIgnoreCase("SETSTATS"))
    {
      int obj=-1;
      String stats="";
      try
      {
        obj=Integer.parseInt(infos[1]);
        stats=infos[2];
      }
      catch(Exception e)
      {
        // ok
      }
      if(obj==-1||stats.equals(""))
      {
        this.sendMessage("Les parametres sont invalides.");
        return;
      }
      GameObject object=World.getGameObject(obj);
      if(object==null)
      {
        this.sendMessage("L'objet n'existe pas.");
        return;
      }
      if(stats.equals("-1"))
      {
        object.clearStats();
        SocketManager.GAME_SEND_UPDATE_ITEM(this.getPlayer(),object);
      }
      else
      {
        object.refreshStatsObjet(stats);
        SocketManager.GAME_SEND_UPDATE_ITEM(this.getPlayer(),object);
      }
      this.sendMessage("L'objet a ete modifie avec succes.");
      return;
    }
    else if(command.equalsIgnoreCase("ADDCELLPARK"))
    {
      if(this.getPlayer().getCurMap().getMountPark()==null)
      {
        this.sendMessage("Pas d'enclos sur votre map.");
        return;
      }
      this.getPlayer().getCurMap().getMountPark().addCellObject(this.getPlayer().getCurCell().getId());
      Database.getStatics().getMountParkData().update(this.getPlayer().getCurMap().getMountPark());
      this.sendMessage("Vous avez ajoute la cellule e l'enclos.");
      return;
    }
    else if(command.equalsIgnoreCase("O"))
    {
      MountPark mp=this.getPlayer().getCurMap().getMountPark();

      for(GameCase c : this.getPlayer().getCurMap().getCases())
      {
        if(c.getObject()!=null)
        {
          switch(c.getObject().getTemplate().getId())
          {
            case 6766:
            case 6767:
            case 6763:
            case 6772:
              mp.setDoor(c.getId());
              this.sendMessage("Vous avez ajoute une porte e l'enclos.");
              return;
          }
        }
      }
      this.sendMessage("Vous ne vous situez pas sur la porte.");
    }
    else if(command.equalsIgnoreCase("A1"))
    {
      this.getPlayer().getCurMap().getMountPark().setMountCell(this.getPlayer().getCurCell().getId());
      this.sendMessage("Vous avez modifie la cellule de spawn de l'enclos.");
    }
    else if(command.equalsIgnoreCase("B1"))
    {
      this.getPlayer().getCases=true;
      this.sendMessage("Vous avez active le getCases.");
    }
    else if(command.equalsIgnoreCase("C1"))
    {
      this.getPlayer().getCases=false;
      this.getPlayer().getCurMap().getMountPark().setCellObject(this.getPlayer().thisCases);
      this.getPlayer().thisCases.clear();
      Database.getStatics().getMountParkData().update(this.getPlayer().getCurMap().getMountPark());
      this.sendMessage("Vous avez applique les nouvelles cases e l'enclos.");
    }
    else if(command.equalsIgnoreCase("RELOADDROP"))
    {
      Main.world.reloadDrops();
      this.sendMessage("Le rechargement des drops a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADBOURSE"))
    {
      Main.world.reloadBourse();
      this.sendMessage("Le rechargement de bourse aux kamas a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADENDFIGHT"))
    {
      Main.world.reloadEndFightActions();
      this.sendMessage("Le rechargement des endfights a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADHOUSE"))
    {
      Main.world.reloadHouses();
      this.sendMessage("Le rechargement des maisons a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADCOFFRE"))
    {
      Main.world.reloadTrunks();
      this.sendMessage("Le rechargement des coffres a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADACTION"))
    {
      Main.world.reloadObjectsActions();
      this.sendMessage("Le rechargement des actions a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADMAP"))
    {
      Main.world.reloadMaps();
      this.sendMessage("Le rechargement des maps a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADMOUNTPARK"))
    {
      int i=Integer.parseInt(infos[1]);
      Main.world.reloadMountParks(i);
      this.sendMessage("Le rechargement de l'enclos "+i+" a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADNPC"))
    {
      try
      {
        Main.world.reloadNpcs();
      }
      catch(Exception e)
      {
        e.printStackTrace();
        this.sendMessage(e.getMessage());
      }
      this.sendMessage("Le rechargement des Npcs a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADSPELL"))
    {
      Main.world.reloadSpells();
      this.sendMessage("Le rechargement des sorts a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADITEM"))
    {
      Main.world.reloadItems();
      this.sendMessage("Le rechargement des items a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADMONSTER"))
    {
      Main.world.reloadMonsters();
      this.sendMessage("Le rechargement des monstres a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("RELOADQUEST"))
    {
      Main.world.reloadQuests();
      this.sendMessage("Le rechargement des quetes a ete effectue.");
      return;
    }
    else if(command.equalsIgnoreCase("CONVERT"))
    {
      try
      {
        this.sendMessage(Integer.toHexString(Integer.parseInt(infos[1])));
        this.sendMessage(Integer.parseInt(infos[1],16)+"");
      }
      catch(Exception e)
      {
        this.sendMessage(Integer.parseInt(infos[1],16)+"");
      }
      return;
    }
    else if(command.equalsIgnoreCase("LISTTYPE"))
    {
      String s="";
      for(ObjectTemplate obj : Main.world.getObjTemplates())
        if(obj.getType()==Integer.parseInt(infos[1]))
          s+=obj.getId()+",";
      this.sendMessage(s);
      return;
    }
    else if(command.equalsIgnoreCase("EMOTE"))
    {
      Player perso=this.getPlayer();
      byte emoteId=0;
      try
      {
        emoteId=Byte.parseByte(infos[1]);
        perso=Main.world.getPlayerByName(infos[2]);
      }
      catch(Exception e)
      {
        // ok
      }
      if(perso==null)
        perso=this.getPlayer();
      this.getPlayer().addStaticEmote(emoteId);
      this.sendMessage("L'emote "+emoteId+" a ete ajoute au joueur "+perso.getName()+".");
      return;
    }
    else if(command.equalsIgnoreCase("DELNPCITEM"))
    {
      int npcGUID=0;
      int itmID=-1;
      try
      {
        npcGUID=Integer.parseInt(infos[1]);
        itmID=Integer.parseInt(infos[2]);
      }
      catch(Exception e)
      {
        // ok
      }

      GameMap map=this.getPlayer().getCurMap();
      Npc npc=map.getNpc(npcGUID);
      NpcTemplate npcTemplate=null;
      if(npc==null)
        npcTemplate=Main.world.getNPCTemplate(npcGUID);
      else
        npcTemplate=npc.getTemplate();
      if(npcGUID==0||itmID==-1||npcTemplate==null)
      {
        String str="NpcGUID ou itemID invalide.";
        this.sendMessage(str);
        return;
      }
      String str="";
      if(npcTemplate.removeItemVendor(itmID))
        str="L'objet a ete retire.";
      else
        str="L'objet n'a pas ete retire.";
      this.sendMessage(str);
      Database.getDynamics().getNpcTemplateData().update(npcTemplate);
      return;
    }
    else  if(command.equalsIgnoreCase("LISTEXTRA"))
    {
      String mess="Liste des Extra Monstres :";
      for(Entry<Integer, GameMap> i : Main.world.getExtraMonsterOnMap().entrySet())
        mess+="\n- "+i.getKey()+" est sur la map : "+i.getValue().getId();
      if(Main.world.getExtraMonsterOnMap().size()<=0)
        mess="Aucun Extra Monstres existe.";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("CREATEGUILD"))
    {
      Player perso=this.getPlayer();
      if(infos.length>1)
      {
        perso=Main.world.getPlayerByName(infos[1]);
      }
      if(perso==null)
      {
        String mess="Le personnage n'existe pas.";
        this.sendMessage(mess);
        return;
      }

      if(!perso.isOnline())
      {
        String mess="Le personnage "+perso.getName()+" n'est pas connecte.";
        this.sendMessage(mess);
        return;
      }
      if(perso.get_guild()!=null||perso.getGuildMember()!=null)
      {
        String mess="Le personnage "+perso.getName()+" possede deje une guilde.";
        this.sendMessage(mess);
        return;
      }
      SocketManager.GAME_SEND_gn_PACKET(perso);
      String mess=perso.getName()+": Panneau de creation de guilde ouvert.";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("SEND"))
    {
      SocketManager.send(this.getClient(),msg.substring(5));
      this.sendMessage("Le paquet a ete envoye : "+msg.substring(5));
      return;
   
    }
    else if(command.equalsIgnoreCase("SENDTOMAP"))
    {
      SocketManager.sendPacketToMap(this.getPlayer().getCurMap(),infos[1]);
      this.sendMessage("Le paquet a ete envoye : "+msg.substring(10));
      return;
    }
    else if(command.equalsIgnoreCase("SENDTO"))
    {
      Player perso=null;
      try
      {
        perso=Main.world.getPlayerByName(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }
      if(perso==null)
      {
        this.sendMessage("Le nom du personnage est incorrect.");
        return;
      }
      SocketManager.send(Main.world.getPlayerByName(infos[1]),msg.substring(8+infos[1].length()));
      this.sendMessage("Le paquet a ete envoye : "+msg.substring(8+infos[1].length())+" e "+infos[1]+".");
      return;
    }
    else if(command.equalsIgnoreCase("TITRE"))
    {
      Player perso=this.getPlayer();
      byte TitleID=0;
      try
      {
        TitleID=Byte.parseByte(infos[1]);
        perso=Main.world.getPlayerByName(infos[2]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(perso==null)
      {
        perso=this.getPlayer();
      }

      perso.set_title(TitleID);
      perso.setAllTitle(String.valueOf(TitleID));
      this.sendMessage("Vous avez modifie le titre de "+perso.getName()+".");
      Database.getStatics().getPlayerData().update(perso);
      if(perso.getFight()==null)
        SocketManager.GAME_SEND_ALTER_GM_PACKET(perso.getCurMap(),perso);
      return;
    }
    else if(command.equalsIgnoreCase("POINTS"))
    {
      int count=0;
      try
      {
        count=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
        this.sendMessage("Valeur incorrecte.");
        return;
      }
      if(count==0)
      {
        this.sendMessage("Valeur inutile.");
        return;
      }
      Player perso=this.getPlayer();
      int pointtotal=perso.getAccount().getPoints()+count;
      if(pointtotal<0)
        pointtotal=0;
      perso.getAccount().setPoints(pointtotal+perso.getAccount().getPoints());
      if(perso.isOnline())
        SocketManager.GAME_SEND_STATS_PACKET(perso);
      String mess="Vous venez de donner "+count+" points boutique e "+perso.getName()+".";
      this.sendMessage(mess);
      return;
    }
    else if(command.equalsIgnoreCase("ITEMTYPE"))
    {
      int type=0;
      try
      {
        type=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      for(ObjectTemplate obj : Main.world.getObjTemplates())
      {
        if(obj.getType()==type)
        {
          GameObject addObj=obj.createNewItem(1,true);
          if(this.getPlayer().addObjet(addObj,true))//Si le joueur n'avait pas d'item similaire
            World.addGameObject(addObj,true);
        }
      }
      this.sendMessage("Vous avez tous les objets de type "+type+" dans votre inventaire.");
      return;
    }
    else if(command.equalsIgnoreCase("FULLMORPH"))
    {
      this.getPlayer().setFullMorph(10,false,false);
      this.sendMessage("Vous avez ete transforme en crocoburio.");
      return;
    }
    else if(command.equalsIgnoreCase("UNFULLMORPH"))
    {
      String pseudo="";
      try
      {
        pseudo=infos[1];
      }
      catch(Exception e)
      {
        // ok
      }
      Player p=Main.world.getPlayerByName(pseudo);
      if(p==null)
        p=this.getPlayer();
      p.unsetFullMorph();
      this.sendMessage("Vous avez transforme dans la forme originale "+p.getName()+".");
      return;
    }
    else if(command.equalsIgnoreCase("PETSRES"))
    {
      int objID=1;
      try
      {
        objID=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      PetEntry p=Main.world.getPetsEntry(objID);
      if(p==null)
      {
        this.sendMessage("Le familier n'existe pas.");
        return;
      }
      p.resurrection();
      this.sendMessage("Vous avez ressuscite le familier.");
      SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(this.getPlayer(),World.getGameObject(objID));
      return;
    }
    else if(command.equalsIgnoreCase("SETGROUPE"))
    {
      int groupe=-1;
      try
      {
        groupe=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      Group g=null;
     
        g=Group.getGroupeById(groupe);

      Player perso=null;
      if(infos.length>2)//Si un nom de perso est specifie
      {
        perso=Main.world.getPlayerByName(infos[2]);
        if(perso == null)
        	return;
        
        if(perso.getGroupe() != null) {
        if(perso.getGroupe().getId() >= this.getPlayer().getGroupe().getId())
        return;
        }
        
        if(perso.getGroupe() != null && groupe == 1)
        if(perso.getGroupe().getId() > 1)
        groupe = 0;	
        if(groupe >= 6)
            groupe = 6;
        if(this.getPlayer().getGroupe().getId()  == 5) {
        	if(groupe >= 5)
                groupe = 4;	
        }
        if(this.getPlayer().getGroupe().getId()  == 6) {
        	if(groupe >= 6)
                groupe = 5;	
        }
        g=Group.getGroupeById(groupe);
        if(perso!=null&&g!=null)
          perso.setGroupe(g,true);
      }
      Database.getStatics().getPlayerData().updateGroupe(groupe,infos[2]);
      if(groupe == 0 || groupe == -1  || groupe == 1 && perso.getGroupe().getId() > 1) {
      perso.getAccount().setBanned(true);
      Database.getStatics().getAccountData().update(perso.getAccount());
        if(perso.getGameClient()!=null)
          perso.getGameClient().disconnect();
      }else {
    	  Database.getStatics().getAccountData().admin(perso.getAccount().getId());	  
      }

      String str="Le groupe du joueur "+infos[2]+" a ete modifie.";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("SETFREEPLACE"))
    {
      String str="Le groupe du joueur "+infos[2]+" a ete modifie.";
      this.sendMessage(str);
      return;
    }
    else if(command.equalsIgnoreCase("SHOWRIGHTGROUPE"))
    {
      int groupe=-1;
      String cmd="";
      try
      {
        groupe=Integer.parseInt(infos[1]);
        cmd=infos[2];
      }
      catch(Exception e)
      {
        // ok
      }

      Group g=null;
      
        g=Group.getGroupeById(groupe);

      if(g==null)
      {
        String str="Le groupe est invalide.";
        this.sendMessage(str);
        return;
      }

      List<Command> c=g.getCommands();

      if(cmd.equalsIgnoreCase(""))
      {
        this.sendMessage("\nCommandes disponibles pour le groupe "+g.getName()+" :\n");
        for(Command co : c)
        {
          String args=(co.getArguments()[1]!=null&&!co.getArguments()[1].equalsIgnoreCase("")) ? (" + "+co.getArguments()[1]) : ("");
          String desc=(co.getArguments()[2]!=null&&!co.getArguments()[2].equalsIgnoreCase("")) ? (co.getArguments()[2]) : ("");
          this.sendMessage("<u>"+co.getArguments()[0]+args+"</u> - "+desc);
        }
      }
      else
      {
        this.sendMessage("\nCommandes recherches pour le groupe "+g.getName()+" :\n");
        for(Command co : c)
        {
          if(co.getArguments()[0].contains(cmd.toUpperCase()))
          {
            String args=(co.getArguments()[1]!=null&&!co.getArguments()[1].equalsIgnoreCase("")) ? (" + "+co.getArguments()[1]) : ("");
            String desc=(co.getArguments()[2]!=null&&!co.getArguments()[2].equalsIgnoreCase("")) ? (co.getArguments()[2]) : ("");
            this.sendMessage("<u>"+co.getArguments()[0]+args+"</u> - "+desc);
          }
        }
      }
      return;
    }
    else if(command.equalsIgnoreCase("INV"))
    {
      int size=this.getPlayer().get_size();
      Player perso=this.getPlayer();
      if(size==0)
      {
        if(perso.getGfxId()==8008)
          perso.set_size(150);
        else
          perso.set_size(100);
        perso.setInvisible(false);
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(),perso.getId());
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(),perso);
        this.sendMessage("Vous etes visible.");
      }
      else
      {
          SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(),perso.getId());
        perso.setInvisible(true);
        perso.set_size(0);
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(),perso);
        this.sendMessage("Vous etes invisible.");
      }
      return;
    }
    else if(command.equalsIgnoreCase("INCARNAM"))
    {
      Player perso=this.getPlayer();
      perso.teleport((short)10292,284);
      this.sendMessage("Vous avez ete teleporte e Incarnam.");
      return;
    }
    else if(command.equalsIgnoreCase("ASTRUB"))
    {
      Player perso=this.getPlayer();
      perso.teleport((short)7411,311);
      this.sendMessage("Vous avez ete teleporte e Astrub.");
      return;
    }
    else if(command.equalsIgnoreCase("DELQUEST"))
    {
      int id=-1;
      String perso="";
      try
      {
        id=Integer.parseInt(infos[1]);
        perso=infos[2];
      }
      catch(Exception e)
      {
        // ok
      }

      if(id==-1||perso.equalsIgnoreCase(""))
      {
        this.sendMessage("Un des parametres est invalide.");
        return;
      }
      Player p=Main.world.getPlayerByName(perso);
      Quest q=Quest.getQuestById(id);
      if(p==null||q==null)
      {
        this.sendMessage("La quete ou le joueur est introuvable.");
        return;
      }
      QuestPlayer qp=p.getQuestPersoByQuest(q);
      if(qp==null)
      {
        this.sendMessage("Le personnage n'a pas la quete.");
        return;
      }
      p.delQuestPerso(qp.getId());
      if(qp.removeQuestPlayer())
      {
        Database.getStatics().getPlayerData().update(p);
        this.sendMessage("La quete a ete supprime sur le personnage "+perso+".");
      }
      else
        this.sendMessage("Un probleme est survenu.");
      return;
    }
    else if(command.equalsIgnoreCase("ADDQUEST"))
    {
      int id=-1;
      String perso="";
      try
      {
        id=Integer.parseInt(infos[1]);
        perso=infos[2];
      }
      catch(Exception e)
      {
        // ok
      }

      if(id==-1||perso.equalsIgnoreCase(""))
      {
        this.sendMessage("Un des parametres est invalide.");
        return;
      }
      Player p=Main.world.getPlayerByName(perso);
      Quest q=Quest.getQuestById(id);
      if(p==null||q==null)
      {
        this.sendMessage("La quete ou le joueur est introuvable.");
        return;
      }
      QuestPlayer qp=p.getQuestPersoByQuest(q);
      if(qp!=null)
      {
        this.sendMessage("Le personnage a deje la quete.");
        return;
      }
      q.applyQuest(p);
      qp=p.getQuestPersoByQuest(q);
      if(qp==null)
      {
        this.sendMessage("Une erreur est survenue.");
        return;
      }
      this.sendMessage("La quete a ete ajoute sur le personnage "+perso+".");
      return;
    }
    else if(command.equalsIgnoreCase("FINISHQUEST"))
    {
      int id=-1;
      String perso="";
      try
      {
        id=Integer.parseInt(infos[1]);
        perso=infos[2];
      }
      catch(Exception e)
      {
        // ok
      }

      if(id==-1||perso.equalsIgnoreCase(""))
      {
        this.sendMessage("Un des parametres est invalide.");
        return;
      }
      Player p=Main.world.getPlayerByName(perso);
      Quest q=Quest.getQuestById(id);
      if(p==null||q==null)
      {
        this.sendMessage("La quete ou le joueur est introuvable.");
        return;
      }
      QuestPlayer qp=p.getQuestPersoByQuest(q);
      if(qp==null)
      {
        this.sendMessage("Le personnage n'a pas la quete.");
        return;
      }
      for(QuestStep e : q.getQuestSteps())
      {
        q.updateQuestData(p,true,e.getValidationType());
      }
      Database.getStatics().getPlayerData().update(p);
      this.sendMessage("La quete a ete termine sur le personnage "+perso+".");
      return;
    }
    else if(command.equalsIgnoreCase("SKIPQUEST"))
    {
      int id=-1;
      String perso="";
      try
      {
        id=Integer.parseInt(infos[1]);
        perso=infos[2];
      }
      catch(Exception e)
      {
        // ok
      }

      if(id==-1||perso.equalsIgnoreCase(""))
      {
        this.sendMessage("Un des parametres est invalide.");
        return;
      }
      Player p=Main.world.getPlayerByName(perso);
      Quest q=Quest.getQuestById(id);
      if(p==null||q==null)
      {
        this.sendMessage("La quete ou le joueur est introuvable.");
        return;
      }
      QuestPlayer qp=p.getQuestPersoByQuest(q);
      if(qp==null)
      {
        this.sendMessage("Le personnage n'a pas la quete.");
        return;
      }
      for(QuestStep e : q.getQuestSteps())
      {
        if(qp.isQuestStepIsValidate(e))
          continue;

        q.updateQuestData(p,true,e.getValidationType());
        break;
      }
      Database.getStatics().getPlayerData().update(p);
      this.sendMessage("La quete est passe e l'etape suivante sur le personnage "+perso+".");
      return;
    }
    else if(command.equalsIgnoreCase("ITEMQUEST"))
    {
      int id=-1;
      try
      {
        id=Integer.parseInt(infos[1]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(id==-1)
      {
        this.sendMessage("Le parametre est invalide.");
        return;
      }
      Quest q=Quest.getQuestById(id);
      if(q==null)
      {
        this.sendMessage("La quete est introuvable.");
        return;
      }

      for(QuestStep e : q.getQuestSteps())
      {
        for(Entry<Integer, Integer> entry : e.getItemNecessaryList().entrySet())
        {
          ObjectTemplate objT=Main.world.getObjTemplate(entry.getKey());
          int qua=entry.getValue();
          GameObject obj=objT.createNewItem(qua,false);
          if(this.getPlayer().addObjet(obj,true))
            World.addGameObject(obj,true);
          SocketManager.GAME_SEND_Im_PACKET(this.getPlayer(),"021;"+qua+"~"+objT.getId());
          if(objT.getType()==32) // Si le drop est une mascotte, on l'ajoute ! :)
          {
            this.getPlayer().setMascotte(entry.getKey());
          }
        }
      }
      this.sendMessage("Vous avez reeu tous les items necessaire e la quete.");
      return;
    }
    else if(command.equalsIgnoreCase("SHOWFIGHTPOS"))
    {
      String mess="Liste des StartCell [teamID][cellID]:";
      this.sendMessage(mess);
      String places=this.getPlayer().getCurMap().getPlaces();
      if(places.indexOf('|')==-1||places.length()<2)
      {
        mess="Les places n'ont pas ete definies";
        this.sendMessage(mess);
        return;
      }
      String team0="",team1="";
      String[] p=places.split("\\|");
      try
      {
        team0=p[0];
      }
      catch(Exception e)
      {
        // ok
      }

      try
      {
        team1=p[1];
      }
      catch(Exception e)
      {
        // ok
      }

      mess="Team 0 : ";
      for(int a=0;a<=team0.length()-2;a+=2)
      {
        String code=team0.substring(a,a+2);
        mess+=Main.world.getCryptManager().cellCode_To_ID(code)+",";
      }
      this.sendMessage(mess);
      mess="Team 1 : ";
      for(int a=0;a<=team1.length()-2;a+=2)
      {
        String code=team1.substring(a,a+2);
        mess+=Main.world.getCryptManager().cellCode_To_ID(code)+",";
      }
      this.sendMessage(mess);
    }
    else if(command.equalsIgnoreCase("ADDFIGHTPOS"))
    {
      int team=-1;
      int cell=-1;
      try
      {
        team=Integer.parseInt(infos[1]);
        cell=Integer.parseInt(infos[2]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(team<0||team>1)
      {
        String str="Team ou cellID incorects";
        this.sendMessage(str);
        return;
      }
      if(cell<0||this.getPlayer().getCurMap().getCase(cell)==null||!this.getPlayer().getCurMap().getCase(cell).isWalkable(true))
      {
        cell=this.getPlayer().getCurCell().getId();
      }
      String places=this.getPlayer().getCurMap().getPlaces();
      String[] p=places.split("\\|");
      boolean already=false;
      String team0="",team1="";
      try
      {
        team0=p[0];
      }
      catch(Exception e)
      {
        // ok
      }

      try
      {
        team1=p[1];
      }
      catch(Exception e)
      {
        // ok
      }

      for(int a=0;a<=team0.length()-2;a+=2)
        if(cell==Main.world.getCryptManager().cellCode_To_ID(team0.substring(a,a+2)))
          already=true;
      for(int a=0;a<=team1.length()-2;a+=2)
        if(cell==Main.world.getCryptManager().cellCode_To_ID(team1.substring(a,a+2)))
          already=true;
      if(already)
      {
        this.sendMessage("La case est deja dans la liste");
        return;
      }
      if(team==0)
        team0+=Main.world.getCryptManager().cellID_To_Code(cell);
      else if(team==1)
        team1+=Main.world.getCryptManager().cellID_To_Code(cell);
      String newPlaces=team0+"|"+team1;
      this.getPlayer().getCurMap().setPlaces(newPlaces);
      if(!Database.getDynamics().getMapData().update(this.getPlayer().getCurMap()))
        return;
      this.sendMessage("Les places ont ete modifiees ("+newPlaces+")");
    }
    else if(command.equalsIgnoreCase("DELFIGHTPOS"))
    {
      int cell=-1;
      try
      {
        cell=Integer.parseInt(infos[2]);
      }
      catch(Exception e)
      {
        // ok
      }

      if(cell<0||this.getPlayer().getCurMap().getCase(cell)==null)
      {
        cell=this.getPlayer().getCurCell().getId();
      }
      String places=this.getPlayer().getCurMap().getPlaces();
      String[] p=places.split("\\|");
      String newPlaces="";
      String team0="",team1="";
      try
      {
        team0=p[0];
      }
      catch(Exception e)
      {
        // ok
      }

      try
      {
        team1=p[1];
      }
      catch(Exception e)
      {
        // ok
      }

      for(int a=0;a<=team0.length()-2;a+=2)
      {
        String c=p[0].substring(a,a+2);
        if(cell==Main.world.getCryptManager().cellCode_To_ID(c))
          continue;
        newPlaces+=c;
      }
      newPlaces+="|";
      for(int a=0;a<=team1.length()-2;a+=2)
      {
        String c=p[1].substring(a,a+2);
        if(cell==Main.world.getCryptManager().cellCode_To_ID(c))
          continue;
        newPlaces+=c;
      }
      this.getPlayer().getCurMap().setPlaces(newPlaces);
      if(!Database.getDynamics().getMapData().update(this.getPlayer().getCurMap()))
        return;
      this.sendMessage("Les places ont ete modifiees ("+newPlaces+")");
    }
    else if(command.equalsIgnoreCase("DELALLFIGHTPOS"))
    {
      this.getPlayer().getCurMap().setPlaces("");
      if(!Database.getDynamics().getMapData().update(this.getPlayer().getCurMap()))
        return;
      this.sendMessage("Les places ont ete mis a zero !");
    }
    else if(command.equalsIgnoreCase("ADDMOBSUBAREA"))
    {
      String monsters="";
      String mess="";
      if(infos.length>1)
        monsters=infos[1];
      else
      {
        mess="Il manque le premier argument.";
        this.sendMessage(mess);
        return;
      }

      Player perso=this.getPlayer();
      GameMap map=perso.getCurMap();

      SubArea subArea=map.getSubArea();
      ArrayList<GameMap> maps=subArea.getMaps();
      int i=0;
      int y=0;
      for(GameMap m : maps)
      {
        if(m.getPlaces().equalsIgnoreCase("")||m.getPlaces().equalsIgnoreCase("|"))
        {

          m.setMobPossibles("");
          Database.getDynamics().getMapData().updateMonster(m,"");
          y++;
        }
        else
        {
          m.setMobPossibles(monsters);
          Database.getDynamics().getMapData().updateMonster(m,monsters);
          i++;
        }
        m.refreshSpawns();
      }

      mess=i+" maps ont etes modifies et refresh. "+y+"maps ont etes modifies sans monstres et refresh.";
      this.sendMessage(mess);
    }
    else if(command.equalsIgnoreCase("GSMOBSUBAREA"))
    {
      byte maxGroup=0;
      byte minSize=0;
      byte fixSize=0;
      byte maxSize=0;
      byte def=-1;
      String mess="";
      if(infos.length>4)
      {
        maxGroup=Byte.parseByte(infos[1]);
        minSize=Byte.parseByte(infos[2]);
        fixSize=Byte.parseByte(infos[3]);
        maxSize=Byte.parseByte(infos[4]);
      }
      else
      {
        mess="Il manque les arguments.";
        this.sendMessage(mess);
        return;
      }

      Player perso=this.getPlayer();
      GameMap map=perso.getCurMap();

      SubArea subArea=map.getSubArea();
      ArrayList<GameMap> maps=subArea.getMaps();
      int i=0;
      int y=0;
      for(GameMap m : maps)
      {
        if(m.getPlaces().equalsIgnoreCase("")||m.getPlaces().equalsIgnoreCase("|"))
        {
          m.setGs(def,def,def,def);
          Database.getDynamics().getMapData().updateGs(m);
          y++;
        }
        else
        {
          m.setGs(maxGroup,minSize,fixSize,maxSize);
          Database.getDynamics().getMapData().updateGs(m);
          i++;
        }
        m.refreshSpawns();
      }

      mess=i+" maps ont etes modifies et refresh. "+y+" maps ont etes modifies e -1 partout et refresh.";
      this.sendMessage(mess);
    }
    else if(command.equalsIgnoreCase("FINDEXTRAMONSTER"))
    {
      java.util.Map<Integer, java.util.Map<String, java.util.Map<String, Integer>>> extras=Main.world.getExtraMonsters();

      for(Entry<Integer, java.util.Map<String, java.util.Map<String, Integer>>> entry : extras.entrySet())
      {
        Integer idMob=entry.getKey();
        for(GameMap map : Main.world.getMaps())
          map.getMobPossibles().stream().filter(mob -> mob.getTemplate().getId()==idMob).forEach(mob -> this.sendMessage("Map avec extraMonster : "+map.getId()+" -> "+idMob+"."));
      }
      this.sendMessage("Recherche termine et affiche en console.");
    }
    else if(command.equalsIgnoreCase("GETAREA"))
    {
      int subArea=-1,area=-1,superArea=-1;
      try
      {
        subArea=this.getPlayer().getCurMap().getSubArea().getId();
        area=this.getPlayer().getCurMap().getSubArea().getArea().getId();
        superArea=this.getPlayer().getCurMap().getSubArea().getArea().getSuperArea();
      }
      catch(Exception e)
      {
        // ok
      }
      this.sendMessage("subArea : "+subArea+"\nArea : "+area+"\nsuperArea : "+superArea);
    }
    else if(command.equalsIgnoreCase("RMOBSWORLD"))
    {
      for(GameMap map : Main.world.getMaps())
      {
        try
        {
          if(map.getMobPossibles()!=null&&!map.getMobPossibles().isEmpty()&&map.getMinSize()>0)
          {
            map.refreshSpawns();
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
      this.sendMessage("Finished reloading maps.");
    }
    else  if(command.equalsIgnoreCase("SPION"))
    {
      if(Config.singleton.spion) {
    	Config.getInstance().spion = false;
    	this.sendMessage("Mode spion off.");
      }else {
    	  Config.getInstance().spion = true;
  	this.sendMessage("Mode spion ON.");  
      }
    }
    else
    {
      this.sendMessage("Commande invalide !");
    }
  }

  private static int getCellJail()
  {
    Random rand=new Random();
    switch(rand.nextInt(4)+1)
    {
      case 1:
        return 148;
      case 2:
        return 156;
      case 3:
        return 380;
      case 4:
        return 388;
      default:
        return 148;
    }
  }

  private static String returnClasse(int id)
  {
    switch(id)
    {
      case Constant.CLASS_FECA:
        return "Fec";
      case Constant.CLASS_OSAMODAS:
        return "Osa";
      case Constant.CLASS_ENUTROF:
        return "Enu";
      case Constant.CLASS_SRAM:
        return "Sra";
      case Constant.CLASS_XELOR:
        return "Xel";
      case Constant.CLASS_ECAFLIP:
        return "Eca";
      case Constant.CLASS_ENIRIPSA:
        return "Eni";
      case Constant.CLASS_IOP:
        return "Iop";
      case Constant.CLASS_CRA:
        return "Cra";
      case Constant.CLASS_SADIDA:
        return "Sad";
      case Constant.CLASS_SACRIEUR:
        return "Sac";
      case Constant.CLASS_PANDAWA:
        return "Pan";
      default:
        return "Unk";
    }
  }
}