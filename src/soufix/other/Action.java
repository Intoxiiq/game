package soufix.other;

import soufix.area.map.GameMap;
import soufix.area.map.entity.Animation;
import soufix.area.map.entity.House;
import soufix.area.map.entity.Tutorial;
import soufix.client.Player;
import soufix.client.other.Stalk;
import soufix.client.other.Stats;
import soufix.common.ConditionParser;
import soufix.common.Formulas;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.Npc;
import soufix.entity.monster.MobGrade;
import soufix.entity.monster.MobGroup;
import soufix.entity.monster.Monster;
import soufix.entity.mount.Mount;
import soufix.entity.npc.NpcQuestion;
import soufix.entity.pet.PetEntry;
import soufix.game.GameClient;
import soufix.game.World;
import soufix.game.action.ExchangeAction;
import soufix.job.Job;
import soufix.job.JobStat;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.object.ObjectTemplate;
import soufix.object.entity.Capture;
import soufix.other.Action;
import soufix.quest.Quest;
import soufix.quest.QuestPlayer;
import soufix.utility.Pair;
import soufix.utility.TimerWaiterPlus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class Action
{
  private int id;
  private String args;
  private String cond;
  private GameMap map;

  public Action(int id, String args, String cond, GameMap map)
  {
    this.setId(id);
    this.setArgs(args);
    this.setCond(cond);
    this.setMap(map);
  }

  public int getId()
  {
    return id;
  }

  public void setId(int id)
  {
    this.id=id;
  }

  public String getArgs()
  {
    return args;
  }

  public void setArgs(String args)
  {
    this.args=args;
  }

  public void setCond(String cond)
  {
    this.cond=cond;
  }

  public GameMap getMap()
  {
    return map;
  }

  public void setMap(GameMap map)
  {
    this.map=map;
  }

  public boolean apply(final Player player, Player target, int itemID, int cellid)
  {
    if(player==null)
      return true;
    if(player.getFight()!=null)
    {
      SocketManager.GAME_SEND_MESSAGE(player,"<b>Action impossible,</b> you are in combat.","000000");
      return true;
    }
    if(!cond.equalsIgnoreCase("")&&!cond.equalsIgnoreCase("-1")&&!ConditionParser.validConditions(player,cond))
    {
      SocketManager.GAME_SEND_Im_PACKET(player,"119");
      return true;
    }

    GameClient client=player.getGameClient();
    switch(id)
    {
      case -22: //Remettre prisonnier
        if(player.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR)!=null)
        {
          int skinFollower=player.getObjetByPos(Constant.ITEM_POS_PNJ_SUIVEUR).getTemplate().getId();
          int questId=Constant.getQuestByMobSkin(skinFollower);
          if(questId!=-1)
          {
            //perso.upgradeQuest(questId);
            player.setMascotte(1);
            int itemFollow=Constant.getItemByMobSkin(skinFollower);
            player.removeByTemplateID(itemFollow,1);
          }
        }
        break;
      case -11: //Tï¿½lï¿½portation map dï¿½part ï¿½ la crï¿½ation d'un personnage (?)
        player.teleport(Constant.getStartMap(player.getClasse()),Constant.getStartCell(player.getClasse()));
        SocketManager.GAME_SEND_WELCOME(player);
        break;
      case -10: //Alignement ange si plus de demon et vice-versa sinon random
        if(player.get_align()==1||player.get_align()==2||player.get_align()==3)
          return true;
        int ange=0;
        int demon=0;
        int total=0;
        for(Player i : Main.world.getPlayers())
        {
          if(i==null)
            continue;
          if(i.get_align()==1)
            ange++;
          if(i.get_align()==2)
            demon++;
          total++;
        }
        ange=ange/total;
        demon=demon/total;
        if(ange>demon)
          player.modifAlignement(2);
        else if(demon>ange)
          player.modifAlignement(1);
        else if(demon==ange)
          player.modifAlignement(Formulas.getRandomValue(1,2));
        break;
      case -9: //Mettre un titre
        player.setAllTitle(args);
        break;

      case -8: //Ajouter un zaap
        player.verifAndAddZaap(Short.parseShort(args));
        break;

      case -7://Echange doplon		
        Dopeul.getReward(player,Integer.parseInt(args));
        break;

      case -6://Dopeuls
        GameMap mapActuel=player.getCurMap();
        Map<Integer, Pair<Integer, Integer>> dopeuls=Dopeul.getDopeul();
        Integer IDmob=null;
        if(dopeuls.containsKey((int)mapActuel.getId()))
        {
          IDmob=dopeuls.get((int)mapActuel.getId()).getLeft();
        }
        else
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Dopple error, please report this on the bug report channel on discord.");
          return true;
        }

        int LVLmob=Formulas.getLvlDopeuls(player.getLevel());
        if(player.getLevel()<11)
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Vous devez avoir au moins le niveau 11 pour combattre un dopple.");
          return true;
        }
        int certificat=Constant.getCertificatByDopeuls(IDmob);
        if(certificat==-1)
          return true;
        if(player.hasItemTemplate(certificat,1))
        {
          String date=player.getItemTemplate(certificat,1).getTxtStat().get(Constant.STATS_DATE);
          if(date.contains("#"))
            date=date.split("#")[3];
          long timeStamp=Long.parseLong(date);
          if(System.currentTimeMillis()-timeStamp<=Config.getInstance().doppleTime)
          {
            SocketManager.GAME_SEND_MESSAGE(player,"Vous devez attendre au moins 4 heures pour combattre ce dopple.");
            return true;
          }
          else
            player.removeByTemplateID(certificat,1);
        }
        boolean b=true;
        if(player.getQuestPerso()!=null&&!player.getQuestPerso().isEmpty())
        {
          for(Entry<Integer, QuestPlayer> entry : new HashMap<>(player.getQuestPerso()).entrySet())
          {
            QuestPlayer qa=entry.getValue();
            if(qa.getQuest().getId()==dopeuls.get((int)mapActuel.getId()).getRight())
            {
              b=false;
              if(qa.isFinish())
              {
                player.delQuestPerso(entry.getKey());
                if(qa.removeQuestPlayer())
                {
                  Quest q=Quest.getQuestById(dopeuls.get((int)mapActuel.getId()).getRight());
                  q.applyQuest(player);
                }
              }
            }
          }
        }
        if(b)
        {
          Quest q=Quest.getQuestById(dopeuls.get((int)mapActuel.getId()).getRight());
          q.applyQuest(player);
        }
        String grp=IDmob+","+LVLmob+","+LVLmob+";";
        MobGroup MG=new MobGroup(player.getCurMap().nextObjectId,player.getCurCell().getId(),grp);
        player.getCurMap().startFigthVersusDopeuls(player,MG);
        break;
      case -5://Apprendre un sort
        try
        {
          int sID=Integer.parseInt(args);
          if(Main.world.getSort(sID)==null)
            return true;
          player.learnSpell(sID,1,true,true,true);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;
      case -4://Prison
        switch(Short.parseShort(args))
        {
          case 1://Payer
            player.leaveEnnemyFactionAndPay(player);
            break;
          case 2: //Attendre 10minutes
            player.leaveEnnemyFaction();
            break;
        }
        break;
      case -3://Mascotte
        int idMascotte=Integer.parseInt(args);

        if(player.hasItemTemplate(itemID,1))
        {
          player.removeByTemplateID(itemID,1);
          player.setMascotte(idMascotte);
          //Database.getStatics().getPlayerData().update(player);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+itemID);
          SocketManager.GAME_SEND_ALTER_GM_PACKET(player.getCurMap(),player);
        }
        break;
      case -2://crï¿½er guilde
        if(player.isAway())
          return true;
        if(player.get_guild()!=null||player.getGuildMember()!=null)
        {
          SocketManager.GAME_SEND_gC_PACKET(player,"Ea");
          return true;
        }
        if(player.hasItemTemplate(1575,1))
        {
          SocketManager.GAME_SEND_gn_PACKET(player);
          player.removeByTemplateID(1575,-1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+-1+"~"+1575);
        }
        else
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Vous devez avoir un Guildalogem pour créer une guilde.");
        }
        break;

      case -1://Ouvrir banque
        //Sauvagarde du perso et des item avant.  
        boolean ok=false;
        for(Npc npc : player.getCurMap().getNpcs().values())
          if(npc.getTemplate().getGfxId()==9048)
            ok=true;

        if(ok)
        {
          //Database.getStatics().getPlayerData().update(player);
          if(player.getDeshonor()>=1)
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"183");
            return true;
          }
          final int cost=player.getBankCost();
          if(cost>0)
          {
            final long playerKamas=player.getKamas();
            final long kamasRemaining=playerKamas-cost;
            final long bankKamas=player.getAccount().getBankKamas();
            final long totalKamas=bankKamas+playerKamas;
            if(kamasRemaining<0)//Si le joueur n'a pas assez de kamas SUR LUI pour ouvrir la banque
            {
              if(bankKamas>=cost)
              {
            	  Main.world.kamas_total -= cost;
                player.setBankKamas(bankKamas-cost); //On modifie les kamas de la banque
              }
              else if(totalKamas>=cost)
              {
            	  Main.world.kamas_total -= cost;
                player.setKamas(0); //On puise l'entiï¿½reter des kamas du joueurs. Ankalike ?
                player.setBankKamas(totalKamas-cost); //On modifie les kamas de la banque
                SocketManager.GAME_SEND_STATS_PACKET(player);
                SocketManager.GAME_SEND_Im_PACKET(player,"020;"+playerKamas);
              }
              else
              {
                SocketManager.GAME_SEND_MESSAGE_SERVER(player,"10|"+cost);
                return true;
              }
            }
            else
            //Si le joueur a les kamas sur lui on lui retire directement
            {
              player.setKamas(kamasRemaining);
              Main.world.kamas_total -= cost;
              SocketManager.GAME_SEND_STATS_PACKET(player);
              SocketManager.GAME_SEND_Im_PACKET(player,"020;"+cost);
            }
          }
          SocketManager.GAME_SEND_ECK_PACKET(player.getGameClient(),5,"");
          SocketManager.GAME_SEND_EL_BANK_PACKET(player);
          player.setAway(true);
          player.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_BANK,0));
        }
        break;

      case 0://Tï¿½lï¿½portation
        try
        {
          short newMapID=Short.parseShort(args.split(",",2)[0]);
          int newCellID=Integer.parseInt(args.split(",",2)[1]);
          if(newMapID==10754) //v2.0 - excluded maps
          {
            player.sendMessage("La carte que vous avez essayé d'atteindre est manquante et a été désactivée pour empêcher les joueurs de rester coincés.");
            break;
          }
          if(!player.isInPrison())
          {
            player.teleport(newMapID,newCellID);
          }
          else
          {
            if(player.getCurCell().getId()==268)
            {
              player.teleport(newMapID,newCellID);
            }
          }
        }
        catch(Exception e)
        {
          // Pas ok, mais il y a trop de dialogue de PNJ buggï¿½ pour laisser cette erreur flood.
          // e.printStackTrace();
          return true;
        }
        break;

      case 1://Discours NPC
        if(client==null)
          return true;
        if(args.equalsIgnoreCase("DV"))
        {
          SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
          player.setExchangeAction(null);
        }
        else
        {
          int qID=-1;
          try
          {
            qID=Integer.parseInt(args);
          }
          catch(NumberFormatException e)
          {
            e.printStackTrace();
          }
          NpcQuestion quest=Main.world.getNPCQuestion(qID);
          if(quest==null)
          {
            SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
            player.setExchangeAction(null);
            return true;
          }
          try
          {
            SocketManager.GAME_SEND_QUESTION_PACKET(client,quest.parse(player));
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        }
        break;

      case 2://Tï¿½lï¿½portation
        try
        {
          short newMapID=Short.parseShort(args.split(",")[0]);
          int newCellID=Integer.parseInt(args.split(",")[1]);
          int verifMapID=Integer.parseInt(args.split(",")[2]);
          if(player.getCurMap().getId()==verifMapID)
            player.teleport(newMapID,newCellID);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          return true;
        }
        break;

      case 4://Kamas
        try
        {
        	/*
          int count=Integer.parseInt(args);
          long curKamas=player.getKamas();
          long newKamas=curKamas+count;
          if(newKamas<0)
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"084;1");
            return true;
          }
          else
          {
            player.setKamas(newKamas);
            SocketManager.GAME_SEND_Im_PACKET(player,"046;"+count);
            if(player.isOnline())
              SocketManager.GAME_SEND_STATS_PACKET(player);
          }*/
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;
      case 5://objet
        try
        {
          int tID=Integer.parseInt(args.split(",")[0]);
          int count=Integer.parseInt(args.split(",")[1]);
          boolean send=true;
          if(args.split(",").length>2)
            send=args.split(",")[2].equals("1");

          //Si on ajoute
          if(count>0)
          {
            ObjectTemplate T=Main.world.getObjTemplate(tID);
            if(T==null)
              return true;
            GameObject O=T.createNewItem(count,false);
            //Si retourne true, on l'ajoute au monde
            if(player.addObjet(O,true))
              World.addGameObject(O,true);
          }
          else
          {
            player.removeByTemplateID(tID,-count);
          }
          //Si en ligne (normalement oui)
          if(player.isOnline())//on envoie le packet qui indique l'ajout//retrait d'un item
          {
            SocketManager.GAME_SEND_Ow_PACKET(player);
            if(send)
            {
              if(count>=0)
              {
                SocketManager.GAME_SEND_Im_PACKET(player,"021;"+count+"~"+tID);
              }
              else if(count<0)
              {
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+-count+"~"+tID);
              }
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;
      case 6://Apprendre un métier
          try
          {
            if(client==null)
              return true;
            if(player.getParty()!=null)
              if(player.getParty().getMaster()!=null) {
            	  player.sendMessage("Vous ne pouve pas en mode maitre.");
                  
                return true;
              }
            player.setIsOnDialogAction(1);
            int mID=Integer.parseInt(args.split(",")[0]);
            //int mapId=Integer.parseInt(args.split(",")[1]);
            int sucess=-1;
            int fail=-1;
            if(Main.world.getMetier(mID)==null)
              return true;
            // Si c'est un métier 'basic' :
            if(mID==2||mID==11||mID==13||mID==14||mID==15||mID==16||mID==17||mID==18||mID==19||mID==20||mID==24||mID==25||mID==26||mID==27||mID==28||mID==31||mID==36||mID==41||mID==56||mID==58||mID==60||mID==65)
            {
              if(player.getMetierByID(mID)!=null)//Métier déjé appris
              {
                SocketManager.GAME_SEND_Im_PACKET(player,"111");
                SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
                player.setExchangeAction(null);
                player.setIsOnDialogAction(-1);
                return true;
              }
              if(player.getMetierByID(2)!=null&&player.getMetierByID(2).get_lvl()<30||player.getMetierByID(11)!=null&&player.getMetierByID(11).get_lvl()<30||player.getMetierByID(13)!=null&&player.getMetierByID(13).get_lvl()<30||player.getMetierByID(14)!=null&&player.getMetierByID(14).get_lvl()<30||player.getMetierByID(15)!=null&&player.getMetierByID(15).get_lvl()<30||player.getMetierByID(16)!=null&&player.getMetierByID(16).get_lvl()<30||player.getMetierByID(17)!=null&&player.getMetierByID(17).get_lvl()<30||player.getMetierByID(18)!=null&&player.getMetierByID(18).get_lvl()<30||player.getMetierByID(19)!=null&&player.getMetierByID(19).get_lvl()<30||player.getMetierByID(20)!=null&&player.getMetierByID(20).get_lvl()<30||player.getMetierByID(24)!=null&&player.getMetierByID(24).get_lvl()<30||player.getMetierByID(25)!=null&&player.getMetierByID(25).get_lvl()<30||player.getMetierByID(26)!=null&&player.getMetierByID(26).get_lvl()<30||player.getMetierByID(27)!=null&&player.getMetierByID(27).get_lvl()<30||player.getMetierByID(28)!=null&&player.getMetierByID(28).get_lvl()<30||player.getMetierByID(31)!=null&&player.getMetierByID(31).get_lvl()<30||player.getMetierByID(36)!=null&&player.getMetierByID(36).get_lvl()<30||player.getMetierByID(41)!=null&&player.getMetierByID(41).get_lvl()<30||player.getMetierByID(56)!=null&&player.getMetierByID(56).get_lvl()<30||player.getMetierByID(58)!=null&&player.getMetierByID(58).get_lvl()<30||player.getMetierByID(60)!=null&&player.getMetierByID(60).get_lvl()<30||player.getMetierByID(65)!=null&&player.getMetierByID(65).get_lvl()<30)
              {
                if(sucess==-1||fail==-1)
                {
                  SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
                  player.setExchangeAction(null);
                  player.setIsOnDialogAction(-1);
                  SocketManager.GAME_SEND_Im_PACKET(player,"18;30");
                }
                else
                  SocketManager.send(client,"DQ"+fail+"|4840");
                return true;
              }
              if(player.totalJobBasic()>2)//On compte les métiers déja acquis si c'est supérieur a 2 on ignore
              {
                SocketManager.GAME_SEND_Im_PACKET(player,"19");
                SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
                player.setExchangeAction(null);
                player.setIsOnDialogAction(-1);
                return true;
              }
              else
              //Si c'est < ou = é 2 on apprend
              {
                if(mID==27)
                {
                  player.learnJob(Main.world.getMetier(mID));
                }
                else
                {
                  //if(player.getCurMap().getId()!=mapId)
                  //  return true;
                  player.learnJob(Main.world.getMetier(mID));
                  if(sucess==-1||fail==-1)
                  {
                    SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
                    player.setExchangeAction(null);
                    player.setIsOnDialogAction(-1);
                  }
                  else
                    SocketManager.send(client,"DQ"+sucess+"|4840");
                }
              }
            }
           // Si c'est une specialisations 'FM' :
             if(mID == 43 || mID == 44 || mID == 45 || mID == 46 ||
             mID == 47 || mID == 48 || mID == 49 || mID == 50 || mID
              == 62 || mID == 63 || mID == 64) {
            	 //Si necessaire lvl 65, enlevé les hide si ankalike
            	 
            	 boolean good = false;
              if(player.getMetierByID(17) != null && player.getMetierByID(17).get_lvl() >= 65 && mID == 43)
            	  good = true;
              if(player.getMetierByID(11) != null && player.getMetierByID(11).get_lvl() >= 65 && mID == 44)
            	  good = true;
              if(player.getMetierByID(14) != null &&  player.getMetierByID(14).get_lvl() >= 65 && mID == 45)
            	  good = true;
              if(player.getMetierByID(20) != null &&  player.getMetierByID(20).get_lvl() >= 65 && mID == 46)
            	  good = true;
              if(player.getMetierByID(31) != null && player.getMetierByID(31).get_lvl() >= 65 && mID == 47)
            	  good = true;
              if(player.getMetierByID(13) != null && player.getMetierByID(13).get_lvl() >= 65 && mID == 48)
            	  good = true;
              if(player.getMetierByID(19) != null && player.getMetierByID(19).get_lvl() >= 65 && mID == 49)
            	  good = true;
              if( player.getMetierByID(18) != null && player.getMetierByID(18).get_lvl() >= 65 && mID == 50)
            	  good = true;
              if( player.getMetierByID(15) != null &&  player.getMetierByID(15).get_lvl() >= 65 && mID == 62)
            	  good = true;
              if(player.getMetierByID(16) != null && player.getMetierByID(16).get_lvl() >= 65 && mID == 63)
            	  good = true;
              if(player.getMetierByID(27) != null && player.getMetierByID(27).get_lvl() >= 65 && mID == 64)
            	  good = true;
            	  
              if(good)
              {
             //On compte les specialisations déja acquis si c'est
             // supérieur a 2 on ignore 
             if(player.getMetierByID(mID) !=null)//Métier déjé appris
              SocketManager.GAME_SEND_Im_PACKET(player, "111");
              if(player.totalJobFM() > 2)//On compte les métiers déja acquis si c'est supérieur a 2 on ignore
              SocketManager.GAME_SEND_Im_PACKET(player, "19"); else//Si c'est < ou = é 2 on apprend
              player.learnJob(Main.world.getMetier(mID)); 
              }else
              {
             SocketManager.GAME_SEND_Im_PACKET(player, "12"); 
             SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
             player.setExchangeAction(null);
             player.setIsOnDialogAction(-1);
             return true;
             } 
              }
             
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
          break;

      case 7://retour au point de sauvegarde
        if(!player.isInPrison())
          player.warpToSavePos();
        break;

      case 8://Ajouter une Stat
        try
        {
          int statID=Integer.parseInt(args.split(",",2)[0]);
          int number=Integer.parseInt(args.split(",",2)[1]);
          player.getStats().addOneStat(statID,number);
          SocketManager.GAME_SEND_STATS_PACKET(player);
          int messID=0;
          switch(statID)
          {
            case Constant.STATS_ADD_INTE:
              messID=14;
              break;
          }
          if(messID>0)
            SocketManager.GAME_SEND_Im_PACKET(player,"0"+messID+";"+number);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          return true;
        }
        break;

      case 9://Apprendre un sort
        try
        {
          int sID=Integer.parseInt(args.split(",",2)[0]);
          int mapId=Integer.parseInt(args.split(",",2)[1]);
          if(Main.world.getSort(sID)==null)
            return true;
          if(player.getCurMap().getId()!=mapId)
            return true;
          player.learnSpell(sID,1,true,true,true);
        }
        catch(Exception e)
        {
        }
        break;

      case 10://Pain/potion/viande/poisson
        try
        {
          int min=Integer.parseInt(args.split(",",2)[0]);
          int max=Integer.parseInt(args.split(",",2)[1]);
          if(max==0)
            max=min;
          int val=Formulas.getRandomValue(min,max);
          if(target!=null)
          {
            if(target.getCurPdv()+val>target.getMaxPdv())
              val=target.getMaxPdv()-target.getCurPdv();
            target.setPdv(target.getCurPdv()+val);
            SocketManager.GAME_SEND_STATS_PACKET(target);
          }
          else
          {
            if(player.getCurPdv()+val>player.getMaxPdv())
              val=player.getMaxPdv()-player.getCurPdv();
            player.setPdv(player.getCurPdv()+val);
            SocketManager.GAME_SEND_STATS_PACKET(player);
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 11://Definir l'alignement
        try
        {
        	final byte newAlign = Byte.parseByte(this.args.split(",", 2)[0]);
          player.modifAlignement(newAlign);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 12://Spawn d'un groupe de monstre
        try
        {
          boolean delObj=args.split(",")[0].equals("true");
          boolean inArena=args.split(",")[1].equals("true");

          if(inArena&&Capture.isInArenaMap(player.getCurMap().getId()))
            return true;

          Capture pierrePleine=(Capture)World.getGameObject(itemID);

          String groupData=pierrePleine.parseGroupData();
          String condition="MiS = "+player.getId(); //Condition pour que le groupe ne soit lanï¿½able que par le personnage qui ï¿½ utiliser l'objet
          player.getCurMap().spawnNewGroup(true,player.getCurCell().getId(),groupData,condition,player.getId(),player.getAccount().getCurrentIp());

          if(delObj)
            player.removeItem(itemID,1,true,true);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 13: //Reset Carac incarnam
          try
          {
            player.getStats().addOneStat(125,-player.getStats().getEffect(125));
            player.getStats().addOneStat(124,-player.getStats().getEffect(124));
            player.getStats().addOneStat(118,-player.getStats().getEffect(118));
            player.getStats().addOneStat(123,-player.getStats().getEffect(123));
            player.getStats().addOneStat(119,-player.getStats().getEffect(119));
            player.getStats().addOneStat(126,-player.getStats().getEffect(126));
            int val = 0;
            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_VITA) != 0) {
            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_VITA);
            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_VITA,-player.getStatsParcho().getEffect(Constant.STATS_ADD_VITA));
            	for(int i=0;i<val;i++)
                {
            		player.boostStat(11,false);
                  player.getStatsParcho().addOneStat(125,1);
                  
                }
            }
            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE) != 0) {
            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE);
            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE,-player.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE));
            	for(int i=0;i<val;i++)
                {
            		player.boostStat(12,false);
                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE,1);
                  
                }	
            }
            	
            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_FORC) != 0)
            {
            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_FORC) ;
            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC,-player.getStatsParcho().getEffect(Constant.STATS_ADD_FORC));
            	
            	for(int i=0;i<val;i++)
                {
            		player.boostStat(10,false);
                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC,1);
                  
                }	
            }
            	
            
          if(player.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN) != 0)
            {
            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN);
            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN,-player.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN));
            	
            	for(int i=0;i<val;i++)
                {
            		player.boostStat(13,false);
                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN,1);
                  
                }
            }
           if(player.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL) != 0)
            {
            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL);
            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL,-player.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL));
            	
            	for(int i=0;i<val;i++)
                {
            		player.boostStat(14,false);	
                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL,1);
                  
                }
            }
            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_INTE) != 0)
            {
            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_INTE) ;
            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE,-player.getStatsParcho().getEffect(Constant.STATS_ADD_INTE));
     
            	for(int i=0;i<val;i++)
                {
            	  player.boostStat(15,false);
                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE,1);
                 
                }
            }
            player.addCapital((player.getLevel()-1)*5-player.get_capital());
           //player.getStatsParcho().getMap().clear();
            //Database.getStatics().getPlayerData().update(player);
            SocketManager.GAME_SEND_STATS_PACKET(player);
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        break;

      case 14://Ouvrir l'interface d'oublie de sort incarnam
          player.setisForgetingSpell(true);
          SocketManager.GAME_SEND_FORGETSPELL_INTERFACE('+',player);
        break;

      case 15://Tï¿½lï¿½portation donjon
        try
        {
          short newMapID=Short.parseShort(args.split(",")[0]);
          int newCellID=Integer.parseInt(args.split(",")[1]);
          int ObjetNeed=Integer.parseInt(args.split(",")[2]);
          int MapNeed=Integer.parseInt(args.split(",")[3]);
          if(ObjetNeed==0)
          {
            //Tï¿½lï¿½portation sans objets
            player.teleport(newMapID,newCellID);
          }
          else if(ObjetNeed>0)
          {
            if(MapNeed==0)
            {
              //Tï¿½lï¿½portation sans map
              player.teleport(newMapID,newCellID);
            }
            else if(MapNeed>0)
            {
              if(player.hasItemTemplate(ObjetNeed,1)&&player.getCurMap().getId()==MapNeed)
              {
                //Le perso a l'item
                //Le perso est sur la bonne map
                //On tï¿½lï¿½porte, on supprime aprï¿½s
                player.teleport(newMapID,newCellID);
                player.removeByTemplateID(ObjetNeed,1);
                SocketManager.GAME_SEND_Ow_PACKET(player);
              }
              else if(player.getCurMap().getId()!=MapNeed)
              {
                //Le perso n'est pas sur la bonne map
                SocketManager.GAME_SEND_MESSAGE(player,"You are not on the correct map to be teleported to the dungeon.","009900");
              }
              else
              {
            	  if(player.getParty() != null) {
                  	if(player.getParty().getMaster() != null)
                  	{
                  		if(player.getParty().getMaster().hasItemTemplate(ObjetNeed,1))
                  		{
                            //Le perso a l'item
                            //Le perso est sur la bonne map
                            //On tï¿½lï¿½porte, on supprime aprï¿½s
                            player.teleport(newMapID,newCellID);
                            player.getParty().getMaster().removeByTemplateID(ObjetNeed,1);
                            SocketManager.GAME_SEND_Ow_PACKET(player.getParty().getMaster());
                          }
                          else if(player.getCurMap().getId()!=MapNeed)
                          {
                            //Le perso n'est pas sur la bonne map
                            SocketManager.GAME_SEND_MESSAGE(player,"You are not on the correct map to be teleported to the dungeon.","009900");
                          }
                          else
                          {
                        	  SocketManager.GAME_SEND_MESSAGE(player,"You do not have the necessary key.","009900");  
                          }
                  	}
            	  }
            	  else
                SocketManager.GAME_SEND_MESSAGE(player,"You do not have the necessary key.","009900");
              }
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 16://Tï¿½lï¿½portation donjon sans perte de clef
        try
        {
          short newMapID=Short.parseShort(args.split(",")[0]);
          int newCellID=Integer.parseInt(args.split(",")[1]);
          int ObjetNeed=Integer.parseInt(args.split(",")[2]);
          int MapNeed=Integer.parseInt(args.split(",")[3]);
          if(ObjetNeed==0)
          {
            //Tï¿½lï¿½portation sans objets
            player.teleport(newMapID,newCellID);
          }
          else if(ObjetNeed>0)
          {
            if(MapNeed==0)
            {
              //Tï¿½lï¿½portation sans map
              player.teleport(newMapID,newCellID);
            }
            else if(MapNeed>0)
            {
              if(player.hasItemTemplate(ObjetNeed,1)&&player.getCurMap().getId()==MapNeed)
              {
                //Le perso a l'item
                //Le perso est sur la bonne map
                //On tï¿½lï¿½porte
                player.teleport(newMapID,newCellID);
                SocketManager.GAME_SEND_Ow_PACKET(player);
              }
              else if(player.getCurMap().getId()!=MapNeed)
              {
                //Le perso n'est pas sur la bonne map
                SocketManager.GAME_SEND_MESSAGE(player,"You are not on the correct map to be teleported to the dungeon.","009900");
              }
              else
              {
                //Le perso ne possï¿½de pas l'item
                SocketManager.GAME_SEND_MESSAGE(player,"You do not have the necessary key.","009900");
              }
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 17://Xp mï¿½tier JobID,XpValue
        try
        {
          int JobID=Integer.parseInt(args.split(",")[0]);
          int XpValue=Integer.parseInt(args.split(",")[1]);
          if(player.getMetierByID(JobID)!=null)
          {
            player.getMetierByID(JobID).addXp(player,XpValue);
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 18://Tï¿½lï¿½portation chez soi
        if(House.alreadyHaveHouse(player))//Si il a une maison
        {
          GameObject obj2=World.getGameObject(itemID);
          if(player.hasItemTemplate(obj2.getTemplate().getId(),1))
          {
            player.removeByTemplateID(obj2.getTemplate().getId(),1);
            House h=House.getHouseByPerso(player);
            if(h==null)
              return true;
            player.teleport((short)h.getHouseMapId(),h.getHouseCellId());
          }
        }
        break;

      case 19://Tï¿½lï¿½portation maison de guilde (ouverture du panneau de guilde)
        SocketManager.GAME_SEND_GUILDHOUSE_PACKET(player);
        break;

      case 20://+Points de sorts
        try
        {
          int pts=Integer.parseInt(args);
          if(pts<1)
            return true;
          player.addSpellPoint(pts);
          SocketManager.GAME_SEND_STATS_PACKET(player);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 21://+Energie
        try
        {
          int energyMin=Integer.parseInt(args.split(",",2)[0]);
          int energyMax=Integer.parseInt(args.split(",",2)[1]);
          if(energyMax==0)
            energyMax=energyMin;
          int val=Formulas.getRandomValue(energyMin,energyMax);
          int EnergyTotal=player.getEnergy()+val;
          if(EnergyTotal>10000)
            EnergyTotal=10000;
          player.setEnergy(EnergyTotal);
          SocketManager.GAME_SEND_STATS_PACKET(player);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 22://+Xp
        try
        {
          long XpAdd=Integer.parseInt(args);
          if(XpAdd<1)
            return true;

          long TotalXp=player.getExp()+XpAdd;
          player.setExp(TotalXp);
          SocketManager.GAME_SEND_STATS_PACKET(player);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 23://UnlearnJob
        int Job=Integer.parseInt(args.split(",",2)[0]);
        int mapId=Integer.parseInt(args.split(",",2)[1]);
        if(player.getCurMap().getId()!=mapId)
          return true;
        if(Job<1)
          return true;
        JobStat m2=player.getMetierByID(Job);
        if(m2==null)
          return true;
        player.unlearnJob(m2.getId());
        SocketManager.GAME_SEND_STATS_PACKET(player);
        //Database.getStatics().getPlayerData().update(player);
        break;

      case 24://Morph
        try
        {
          int morphID=Integer.parseInt(args);
          if(morphID<0)
            return true;
          player.setGfxId(morphID);
          SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(),player.getId());
          SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(player.getCurMap(),player);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 25://SimpleUnMorph
        int UnMorphID=player.getClasse()*10+player.getSexe();
        player.setGfxId(UnMorphID);
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(),player.getId());
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(player.getCurMap(),player);
        break;

      case 26://Tï¿½lï¿½portation enclos de guilde (ouverture du panneau de guilde)
        SocketManager.GAME_SEND_GUILDENCLO_PACKET(player);
        break;

      //v2.4 - Startfight placement fix
      //v2.7 - Replaced String += with StringBuilder
      case 27://Lancement de combat : startFigthVersusMonstres args : monsterID,monsterLevel| ... // id,lvl|id,lvl:mapid
        StringBuilder ValidMobGroup=new StringBuilder();
        if(player.getFight()!=null)
          return true;
        try
        {
          int mapId1=Integer.parseInt(args.split(":",2)[1]);
          if(player.getCurMap().getId()!=mapId1)
            return true;
          for(String MobAndLevel : args.split(":",2)[0].split("\\|"))
          {
            int monsterID=-1;
            int monsterLevel=-1;
            String[] MobOrLevel=MobAndLevel.split(",");
            monsterID=Integer.parseInt(MobOrLevel[0]);
            monsterLevel=Integer.parseInt(MobOrLevel[1]);
            if(Main.world.getMonstre(monsterID)==null||Main.world.getMonstre(monsterID).getGradeByLevel(monsterLevel)==null)
              continue;
            ValidMobGroup.append(monsterID+","+monsterLevel+","+monsterLevel+";");
          }
          if(ValidMobGroup.toString().isEmpty())
            return true;
          MobGroup group=new MobGroup(player.getCurMap().nextObjectId,player.getCurCell().getId(),ValidMobGroup.toString());
          player.getCurMap().startFightVersusMonstres(player,group);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          
        }
        break;

      case 28://Desapprendre un sort
        try
        {
          int sID=Integer.parseInt(args);
          int AncLevel=player.getSortStatBySortIfHas(sID).getLevel();
          if(player.getSortStatBySortIfHas(sID)==null)
            return true;
          if(AncLevel<=1)
            return true;
          player.unlearnSpell(player,sID,1,AncLevel,true,true);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 29://Desapprendre un sort avec kamas
        long pKamas3=player.getKamas();
        int payKamas=player.getLevel()*player.getLevel()*25;

        if(pKamas3>=payKamas)
        {
          long pNewKamas3=pKamas3-payKamas;
          if(pNewKamas3<0)
            pNewKamas3=0;
          int sID=Integer.parseInt(args);
          int AncLevel=player.getSortStatBySortIfHas(sID).getLevel();
          if(player.getSortStatBySortIfHas(sID)==null)
            return true;
          if(AncLevel<=1)
            return true;
          player.unlearnSpell(player,sID,1,AncLevel,true,true);
          SocketManager.GAME_SEND_MESSAGE(player,"You have lost "+payKamas+" Kamas.");
        }
        else
        {
          SocketManager.GAME_SEND_MESSAGE(player,"You do not have enough kamas to perform this action.");
          return true;
        }
        break;

      case 30: //Change la taille d'un personnage size
        int size=Integer.parseInt(args);
        player.set_size(size);
        SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(),player.getId());
        SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(player.getCurMap(),player);
        break;

      case 33:// Stat max a un obj pos
        int posItem=Integer.parseInt(args);
        GameObject itemPos=player.getObjetByPos(posItem);
        if(itemPos!=null)
        {
          itemPos.clearStats();
          Stats maxStats=itemPos.generateNewStatsFromTemplate(itemPos.getTemplate().getStrTemplate(),true);
          itemPos.setStats(maxStats);
          int idObjPos=itemPos.getGuid();
          player.removeItem(itemID,1,true,true);
          SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player,idObjPos);
          SocketManager.GAME_SEND_OAKO_PACKET(player,itemPos);
          SocketManager.GAME_SEND_STATS_PACKET(player);
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"There are no items in the chosen item position.");
        }
        break;

      case 34://Loterie
        int idLot=Integer.parseInt(args.split(",",2)[0]);
        //int mapId1 = Integer.parseInt(args.split(",", 2)[1]);
        Loterie.startLoterie(player,idLot);
        break;

      case 35: //Reset Carac condition : Map xï¿½lor 741 et l'obre de recons 10563
        try
        {
          if(player.getCurMap().getId()!=741||!player.hasItemTemplate(10563,1))
            return true;
          player.removeByTemplateID(10563,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+10563+"~"+1);
          player.getStats().addOneStat(125,-player.getStats().getEffect(125));
          player.getStats().addOneStat(124,-player.getStats().getEffect(124));
          player.getStats().addOneStat(118,-player.getStats().getEffect(118));
          player.getStats().addOneStat(123,-player.getStats().getEffect(123));
          player.getStats().addOneStat(119,-player.getStats().getEffect(119));
          player.getStats().addOneStat(126,-player.getStats().getEffect(126));
          player.addCapital((player.getLevel()-1)*5-player.get_capital());
          SocketManager.GAME_SEND_STATS_PACKET(player);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          
        }
        break;

      case 36: //Cout d'un jeu
        try
        {
          long price=Integer.parseInt(args.split(";")[0]);
          int tutorial=Integer.parseInt(args.split(";")[1]);
          if(tutorial==30)
          {
            int random=Formulas.getRandomValue(1,200);
            if(random==100)
              tutorial=31;
            else
              Database.getDynamics().getNpcQuestionData().updateLot();
          }
          final Tutorial tuto=Main.world.getTutorial(tutorial);
          if(tuto==null || tuto.getStart() == null)
            return true;
          if(player.getKamas()>=price)
          {
            if(price!=0L)
            {
            	 Main.world.kamas_total -= price;
              player.setKamas(player.getKamas()-price);
              if(player.isOnline())
                SocketManager.GAME_SEND_STATS_PACKET(player);
              SocketManager.GAME_SEND_Im_PACKET(player,"046;"+price);
            }
            try
            {
              tuto.getStart().apply(player,null,-1,(short)-1);
            }
            catch(Exception e)
            {
              e.printStackTrace();
            }

            TimerWaiterPlus.addNext(() -> {
              SocketManager.send(player,"TC"+tuto.getId()+"|7001010000");
              player.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_TUTORIAL,tuto));
              player.setAway(true);

            }, 1500);
            return true;
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"182");
        }
        catch(Exception e23)
        {
          e23.printStackTrace();
        }
        break;

      case 37://Loterie pioute
        Loterie.startLoteriePioute(player);
        break;

      case 38://Apprendre une ï¿½mote
        player.addStaticEmote(Integer.parseInt(args));
        break;

      case 40: //Donner une quï¿½te
        int QuestID=Integer.parseInt(args);
        boolean problem=false;
        Quest quest0=Quest.getQuestById(QuestID);
        if(quest0==null)
        {
          SocketManager.GAME_SEND_MESSAGE(player,"This quest could not be found.");
          problem=true;
          break;
        }
        for(QuestPlayer qPerso : player.getQuestPerso().values())
        {
          if(qPerso.getQuest().getId()==QuestID)
          {
            SocketManager.GAME_SEND_MESSAGE(player,"Vous avez déjà commencé cette quête.");
            problem=true;
            break;
          }
        }

        if(!problem)
          quest0.applyQuest(player);
        break;

      case 41: //Confirm objective
        break;

      case 42: //Monte prochaine Ã©tape quete ou termine
        break;

      case 43: //Tï¿½lï¿½portation de quï¿½te
        String[] split=args.split(";");
        int mapid=Integer.parseInt(split[0].split(",")[0]);
        cellid=Integer.parseInt(split[0].split(",")[1]);
        int mapsecu=Integer.parseInt(split[1]);
        int questId=Integer.parseInt(split[2]);

        if(player.getCurMap().getId()!=mapsecu)
          return true;
        QuestPlayer questt=player.getQuestPersoByQuestId(questId);
        if(questt==null||!questt.isFinish())
          return true;
        player.teleport((short)mapid,cellid);
        break;

      case 44: //Commande admin level up \ niveau
        int count=Integer.parseInt(args);
        if(player.getLevel()<count)
        {
          while(player.getLevel()<count)
            player.levelUp(false,true);
          if(player.isOnline())
          {
            SocketManager.GAME_SEND_SPELL_LIST(player);
            SocketManager.GAME_SEND_NEW_LVL_PACKET(player.getGameClient(),player.getLevel());
            SocketManager.GAME_SEND_STATS_PACKET(player);
          }
        }
        break;

      case 50: //Traque
        if(player.get_align()==0)
          return true;

        if(player.get_traque()!=null&&player.get_traque().getTime()==-2)
        {
          long xp=Formulas.getXpStalk(player.getLevel());
          player.addXp(xp);
          player.set_traque(null);//On supprime la traque
          SocketManager.GAME_SEND_STATS_PACKET(player);
          SocketManager.GAME_SEND_MESSAGE(player,"vous venez de recevoir "+xp+" points d'expérience.","000000");
          return true;
        }

        if(player.get_traque()==null)
        {
          Stalk t=new Stalk(0,null);
          player.set_traque(t);
        }
        if(player.get_traque().getTime()<System.currentTimeMillis()-600000||player.get_traque().getTime()==0)
        {
          Player tempP=null;
          ArrayList<Player> victimes=new ArrayList<Player>();
          for(Player victime : Main.world.getOnlinePlayers())
          {
            if(victime==null||victime==player)
              continue;
           if(victime.getAccount().getCurrentIp().compareTo(player.getAccount().getCurrentIp())==0)
           continue;
  
            if(player.getAccount().restriction.aggros.containsKey(victime.getAccount().getCurrentIp())) {
            	  if((System.currentTimeMillis()-player.getAccount().restriction.aggros.get(victime.getAccount().getCurrentIp()))<1000*25*60)
                  {
            		  continue;
                  }
            }
            if(victime.get_align()==player.get_align()||victime.get_align()==0||!victime.is_showWings()||victime.getGroupe() != null)
              continue;
            if(((player.getLevel()+20)>=victime.getLevel())&&((player.getLevel()-20)<=victime.getLevel()))
              victimes.add(victime);
          }
          if(victimes.size()==0)
          {
            SocketManager.GAME_SEND_MESSAGE(player,"Nous n'avons pas trouvé de cible é votre niveau, merci de revenir plus tard.","000000");
            player.set_traque(null);
            return true;
          }
          if(victimes.size()==1)
            tempP=victimes.get(0);
          else
            tempP=victimes.get(Formulas.getRandomValue(0,victimes.size()-1));
          SocketManager.GAME_SEND_MESSAGE(player,"Vous chassez maintenant "+tempP.getName(),"000000");
          player.get_traque().setTraque(tempP);
          player.get_traque().setTime(System.currentTimeMillis());
          GameObject object=player.getItemTemplate(10085);
          if(object!=null)
            player.removeItem(object.getGuid(),player.getNbItemTemplate(10085),true,true);
          ObjectTemplate T=Main.world.getObjTemplate(10085);
          GameObject newObj=T.createNewItem(20,false);
          newObj.addTxtStat(Constant.STATS_NAME_TRAQUE,tempP.getName());
          newObj.addTxtStat(Constant.STATS_ALIGNEMENT_TRAQUE,Integer.toHexString(tempP.get_align())+"");
          newObj.addTxtStat(Constant.STATS_GRADE_TRAQUE,Integer.toHexString(tempP.getALvl())+"");
          newObj.addTxtStat(Constant.STATS_NIVEAU_TRAQUE,Integer.toHexString(tempP.getLevel())+"");

          if(player.addObjet(newObj,true))
            World.addGameObject(newObj,true);
        }
        else
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Vous venez de signer un contrat, vous devez attendre avant d'en signer un autre.","000000");
        }
        break;

      case 53: //Suivre le dï¿½placement pour une map prï¿½sice
        if(args==null)
          break;
        if(Main.world.getMap(Short.parseShort(args))==null)
          break;
        GameMap CurMap=Main.world.getMap(Short.parseShort(args));
        if(player.getFight()==null)
        {
          SocketManager.GAME_SEND_FLAG_PACKET(player,CurMap);
        }
        break;

      //v2.7 - Replaced String += with StringBuilder
      case 60: //Combattre un protecteur
        StringBuilder ValidMobGroup1=new StringBuilder();
        if(player.getFight()!=null)
          return true;
        try
        {
          for(String MobAndLevel : args.split("\\|"))
          {
            int monsterID=-1;
            int lvlMin=-1;
            int lvlMax=-1;
            String[] MobOrLevel=MobAndLevel.split(",");
            monsterID=Integer.parseInt(MobOrLevel[0]);
            lvlMin=Integer.parseInt(MobOrLevel[1]);
            lvlMax=Integer.parseInt(MobOrLevel[2]);

            if(Main.world.getMonstre(monsterID)==null||Main.world.getMonstre(monsterID).getGradeByLevel(lvlMin)==null||Main.world.getMonstre(monsterID).getGradeByLevel(lvlMax)==null)
            {
              continue;
            }
            ValidMobGroup1.append(monsterID+","+lvlMin+","+lvlMax+";");
          }
          if(ValidMobGroup1.toString().isEmpty())
            return true;
          MobGroup group=new MobGroup(player.getCurMap().nextObjectId,player.getCurCell().getId(),ValidMobGroup1.toString());
          player.getCurMap().startFightVersusProtectors(player,group);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          
        }
        break;

      case 100: //Donner l'abilitï¿½ 'args' ï¿½ une dragodinde
        if(player.hasItemTemplate(361,100))
        {
          player.removeByTemplateID(361,100);
          GameObject newObjAdded=Main.world.getObjTemplate(9201).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded,true,-1))
          {
            World.addGameObject(newObjAdded,true);
            player.addObjet(newObjAdded);
          }
        }
        break;

      case 102: //Marier des personnages
        GameMap map0=player.getCurMap();
        if(map0.getCase(297).getPlayers()!=null&&map0.getCase(282).getPlayers()!=null)
        {
          if(map0.getCase(297).getPlayers().size()==1&&map0.getCase(282).getPlayers().size()==1)
          {
            Player boy=(Player)map0.getCase(282).getPlayers().toArray()[0],girl=(Player)map0.getCase(297).getPlayers().toArray()[0];
            boy.setBlockMovement(true);
            girl.setBlockMovement(true);
            Main.world.priestRequest(boy,girl,player);
          }
        }
        break;

      case 103: //Divorce
        if(player.getKamas()<50000)
        {
          return true;
        }
        else
        {
        	 Main.world.kamas_total -= 50000;
          player.setKamas(player.getKamas()-50000);
          if(player.isOnline())
            SocketManager.GAME_SEND_STATS_PACKET(player);
          Player wife=Main.world.getPlayer(player.getWife());
          wife.Divorce();
          player.Divorce();
        }
        break;

      case 104: //Tï¿½lï¿½portation mine + Animation
        if(player.getCurMap().getId()!=(short)10257)
          return true;

        ArrayList<Pair<Short, Integer>> arrays=new ArrayList<>();
        for(String i : args.split("\\;"))
          arrays.add(new Pair<>(Short.parseShort(i.split("\\,")[0]),Integer.parseInt(i.split("\\,")[1])));

        Pair<Short, Integer> couple=arrays.get(new Random().nextInt(arrays.size()));
        SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"","2",player.getId()+"","6");
        player.teleport(couple.getLeft(),couple.getRight());
        break;

      case 105: //Restat carac second pouvoir
        if(!player.hasItemTemplate(10563,1))
        {
          player.sendMessage("You do not have a magical orb.");
          return true;
        }
        else
        {
          player.removeByTemplateID(10563,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+10563+"~"+1);
        }

        if(player.getStatsParcho().getEffect(125)!=0||player.getStatsParcho().getEffect(124)!=0||player.getStatsParcho().getEffect(118)!=0||player.getStatsParcho().getEffect(119)!=0||player.getStatsParcho().getEffect(126)!=0||player.getStats().getEffect(123)!=0)
        {
          player.getStats().addOneStat(125,-player.getStats().getEffect(125)+player.getStatsParcho().getEffect(125));
          player.getStats().addOneStat(124,-player.getStats().getEffect(124)+player.getStatsParcho().getEffect(124));
          player.getStats().addOneStat(118,-player.getStats().getEffect(118)+player.getStatsParcho().getEffect(118));
          player.getStats().addOneStat(123,-player.getStats().getEffect(123)+player.getStatsParcho().getEffect(123));
          player.getStats().addOneStat(119,-player.getStats().getEffect(119)+player.getStatsParcho().getEffect(119));
          player.getStats().addOneStat(126,-player.getStats().getEffect(126)+player.getStatsParcho().getEffect(126));
          player.addCapital((player.getLevel()-1)*5-player.get_capital());
        }
        else if(player.getStats().getEffect(125)==101&&player.getStats().getEffect(124)==101&&player.getStats().getEffect(118)==101&&player.getStats().getEffect(123)==101&&player.getStats().getEffect(119)==101&&player.getStats().getEffect(126)==101)
        {
          player.getStats().addOneStat(125,-player.getStats().getEffect(125)+101);
          player.getStats().addOneStat(124,-player.getStats().getEffect(124)+101);
          player.getStats().addOneStat(118,-player.getStats().getEffect(118)+101);
          player.getStats().addOneStat(123,-player.getStats().getEffect(123)+101);
          player.getStats().addOneStat(119,-player.getStats().getEffect(119)+101);
          player.getStats().addOneStat(126,-player.getStats().getEffect(126)+101);

          player.getStatsParcho().addOneStat(125,101);
          player.getStatsParcho().addOneStat(124,101);
          player.getStatsParcho().addOneStat(118,101);
          player.getStatsParcho().addOneStat(123,101);
          player.getStatsParcho().addOneStat(119,101);
          player.getStatsParcho().addOneStat(126,101);

          player.addCapital((player.getLevel()-1)*5-player.get_capital());
        }
        else
        {
          player.sendMessage("Vous ne pouvez pas réinitialiser vos caractéristiques en utilisant ce pouvoir, vous n'avez pas utilisé de défilement de caractéristiques.");
          return true;
        }

        SocketManager.GAME_SEND_STATS_PACKET(player);
        player.sendMessage("You have succesfully reset your characteristics.");
        break;

      case 106:
        switch(this.args)
        {
          case "1"://Remove spell
            if(player.hasItemTemplate(15004,1))
            {
              player.removeByTemplateID(15004,1);
              player.setExchangeAction(new ExchangeAction<>(ExchangeAction.FORGETTING_SPELL,0));
              SocketManager.GAME_SEND_FORGETSPELL_INTERFACE('+',player);
            }
            break;
          case "2"://Restat without
            if(player.hasItemTemplate(15006,1))
            {
              player.removeByTemplateID(15006,1);
              player.getStats().addOneStat(125,-player.getStats().getEffect(125));
              player.getStats().addOneStat(124,-player.getStats().getEffect(124));
              player.getStats().addOneStat(118,-player.getStats().getEffect(118));
              player.getStats().addOneStat(123,-player.getStats().getEffect(123));
              player.getStats().addOneStat(119,-player.getStats().getEffect(119));
              player.getStats().addOneStat(126,-player.getStats().getEffect(126));
              int val = 0;
	            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_VITA) != 0) {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_VITA);
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_VITA,-player.getStatsParcho().getEffect(Constant.STATS_ADD_VITA));
	            	for(int i=0;i<val;i++)
	                {
	            		player.boostStat(11,false);
	                  player.getStatsParcho().addOneStat(125,1);
	                  
	                }
	            }
	            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE) != 0) {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE);
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE,-player.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE));
	            	for(int i=0;i<val;i++)
	                {
	            		player.boostStat(12,false);
	                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE,1);
	                  
	                }	
	            }
	            	
	            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_FORC) != 0)
	            {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_FORC) ;
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC,-player.getStatsParcho().getEffect(Constant.STATS_ADD_FORC));
	            	
	            	for(int i=0;i<val;i++)
	                {
	            		player.boostStat(10,false);
	                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC,1);
	                  
	                }	
	            }
	            	
	            
	          if(player.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN) != 0)
	            {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN);
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN,-player.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN));
	            	
	            	for(int i=0;i<val;i++)
	                {
	            		player.boostStat(13,false);
	                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN,1);
	                  
	                }
	            }
	           if(player.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL) != 0)
	            {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL);
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL,-player.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL));
	            	
	            	for(int i=0;i<val;i++)
	                {
	            		player.boostStat(14,false);	
	                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL,1);
	                  
	                }
	            }
	            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_INTE) != 0)
	            {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_INTE) ;
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE,-player.getStatsParcho().getEffect(Constant.STATS_ADD_INTE));
	     
	            	for(int i=0;i<val;i++)
	                {
	            	  player.boostStat(15,false);
	                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE,1);
	                 
	                }
	            }
              player.addCapital((player.getLevel()-1)*5-player.get_capital());
              
              SocketManager.GAME_SEND_STATS_PACKET(player);
            }
            break;
          case "3"://Restat with
            if(player.hasItemTemplate(15005,1))
            {
              player.removeByTemplateID(15005,1);
                player.getStats().addOneStat(125,-player.getStats().getEffect(125)+player.getStatsParcho().getEffect(125));
                player.getStats().addOneStat(124,-player.getStats().getEffect(124)+player.getStatsParcho().getEffect(124));
                player.getStats().addOneStat(118,-player.getStats().getEffect(118)+player.getStatsParcho().getEffect(118));
                player.getStats().addOneStat(123,-player.getStats().getEffect(123)+player.getStatsParcho().getEffect(123));
                player.getStats().addOneStat(119,-player.getStats().getEffect(119)+player.getStatsParcho().getEffect(119));
                player.getStats().addOneStat(126,-player.getStats().getEffect(126)+player.getStatsParcho().getEffect(126));
                player.addCapital((player.getLevel()-1)*5-player.get_capital());
                int val = 0;
	            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_VITA) != 0) {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_VITA);
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_VITA,-player.getStatsParcho().getEffect(Constant.STATS_ADD_VITA));
	            	for(int i=0;i<val;i++)
	                {
	            		player.boostStat(11,false);
	                  player.getStatsParcho().addOneStat(125,1);
	                  
	                }
	            }
	            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE) != 0) {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE);
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE,-player.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE));
	            	for(int i=0;i<val;i++)
	                {
	            		player.boostStat(12,false);
	                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE,1);
	                  
	                }	
	            }
	            	
	            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_FORC) != 0)
	            {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_FORC) ;
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC,-player.getStatsParcho().getEffect(Constant.STATS_ADD_FORC));
	            	
	            	for(int i=0;i<val;i++)
	                {
	            		player.boostStat(10,false);
	                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC,1);
	                  
	                }	
	            }
	            	
	            
	          if(player.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN) != 0)
	            {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN);
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN,-player.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN));
	            	
	            	for(int i=0;i<val;i++)
	                {
	            		player.boostStat(13,false);
	                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN,1);
	                  
	                }
	            }
	           if(player.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL) != 0)
	            {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL);
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL,-player.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL));
	            	
	            	for(int i=0;i<val;i++)
	                {
	            		player.boostStat(14,false);	
	                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL,1);
	                  
	                }
	            }
	            if(player.getStatsParcho().getEffect(Constant.STATS_ADD_INTE) != 0)
	            {
	            	val = player.getStatsParcho().getEffect(Constant.STATS_ADD_INTE) ;
	            	player.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE,-player.getStatsParcho().getEffect(Constant.STATS_ADD_INTE));
	     
	            	for(int i=0;i<val;i++)
	                {
	            	  player.boostStat(15,false);
	                  player.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE,1);
	                 
	                }
	            }
                SocketManager.GAME_SEND_STATS_PACKET(player);
            }
            break;
        }
        break;

      case 116://EPO donner de la nourriture ï¿½ son familier
        GameObject EPO=World.getGameObject(itemID);
        if(EPO==null)
          return true;
        GameObject pets=player.getObjetByPos(Constant.ITEM_POS_FAMILIER);
        if(pets==null)
          return true;
        PetEntry MyPets=Main.world.getPetsEntry(pets.getGuid());
        if(MyPets==null)
          return true;
        if(EPO.getTemplate().getConditions().contains(pets.getTemplate().getId()+""))
          MyPets.giveEpo(player);
        break;

      case 170:// Donner titre
        try
        {
          byte title1=(byte)Integer.parseInt(args);
          target=Main.world.getPlayerByName(player.getName());
          target.set_title(title1);
          target.setAllTitle(String.valueOf(title1));
          SocketManager.GAME_SEND_MESSAGE(player,"Vous avez gagné un nouveau titre.");
          SocketManager.GAME_SEND_STATS_PACKET(player);
          //Database.getStatics().getPlayerData().update(player);
          if(target.getFight()==null)
            SocketManager.GAME_SEND_ALTER_GM_PACKET(player.getCurMap(),player);
        }
        catch(Exception e)
        {
          e.printStackTrace();
          
        }
        break;

      case 171: // Alignement avec condition
        short type2=(short)Integer.parseInt(args.split(",")[0]);
        int mapId2=Integer.parseInt(args.split(",")[1]);
        if(player.get_align()>0)
          return true;
        if(type2==1&&player.getCurMap().getId()==mapId2)
        {
          if(player.hasItemTemplate(42,10))
          {
            player.removeByTemplateID(42,10);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+10+"~"+42);
            player.modifAlignement((byte)1);
          }
        }
        if(type2==2&&player.getCurMap().getId()==mapId2)
        {
          if(player.hasItemTemplate(95,10))
          {
            player.removeByTemplateID(95,10);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+10+"~"+95);
            player.modifAlignement((byte)2);
          }
        }
        break;

      case 172: //Bricoleur avec condition
        int mapId4=Integer.parseInt(args);
        if(player.getCurMap().getId()!=mapId4)
          return true;
        if(player.totalJobBasic()>2)//On compte les mï¿½tiers dï¿½ja acquis si c'est supï¿½rieur a 2 on ignore
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"19");
          return true;
        }
        if(player.hasItemTemplate(459,20)&&player.hasItemTemplate(7657,15))
        {
          player.removeByTemplateID(459,20);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+459+"~"+20);
          player.removeByTemplateID(7657,15);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+7657+"~"+15);
          player.learnJob(Main.world.getMetier(65));
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
          return true;
        }
        break;

      case 200: //Animation + Condition voyage Ile Minotor
        long pKamas2=player.getKamas();
        if(pKamas2>=100&&player.getCurMap().getId()==9520)
        {
          long pNewKamas2=pKamas2-100;
          if(pNewKamas2<0)
            pNewKamas2=0;
          player.setKamas(pNewKamas2);
          Main.world.kamas_total -= 100;
          if(player.isOnline())
            SocketManager.GAME_SEND_STATS_PACKET(player);
          SocketManager.GAME_SEND_Im_PACKET(player,"046;"+100);
          SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"","2",player.getId()+"","2");
          player.teleport((short)9541,407);
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"182");
        }
        break;

      case 219://Sortie donjon wabbit
        if(player.getCurMap().getId()!=1780)
          return true;
        int type11=Integer.parseInt(args);
        if(type11==1)
        {
          GameObject newObjAdded=Main.world.getObjTemplate(970).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded,true,-1))
          {
            World.addGameObject(newObjAdded,true);
            player.addObjet(newObjAdded);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+970);
          player.teleport((short)844,212);
        }
        else if(type11==2)
        {
          GameObject newObjAdded=Main.world.getObjTemplate(969).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded,true,-1))
          {
            World.addGameObject(newObjAdded,true);
            player.addObjet(newObjAdded);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+969);
          player.teleport((short)844,212);
        }
        else if(type11==3)
        {
          GameObject newObjAdded=Main.world.getObjTemplate(971).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded,true,-1))
          {
            World.addGameObject(newObjAdded,true);
            player.addObjet(newObjAdded);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+971);
          player.teleport((short)844,212);
        }
        break;

      case 220:// IDitem,quantitï¿½ pour IDitem's,quantitï¿½ pour teleport
        try
        {
          String remove0=args.split(";")[0];
          String add0=args.split(";")[1];
          String add1=args.split(";")[4];
          int obj0=Integer.parseInt(remove0.split(",")[0]);
          int qua0=Integer.parseInt(remove0.split(",")[1]);
          int newObj1=Integer.parseInt(add0.split(",")[0]);
          int newQua1=Integer.parseInt(add0.split(",")[1]);
          int newObj2=Integer.parseInt(add1.split(",")[0]);
          int newQua2=Integer.parseInt(add1.split(",")[1]);
          if(player.hasItemTemplate(obj0,qua0))
          {
            player.removeByTemplateID(obj0,qua0);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua0+"~"+obj0);
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+newQua1+"~"+newObj1);
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+newQua2+"~"+newObj2);
            GameObject newObjAdded=Main.world.getObjTemplate(newObj1).createNewItem(newQua1,false);
            if(!player.addObjetSimiler(newObjAdded,true,-1))
            {
              World.addGameObject(newObjAdded,true);
              player.addObjet(newObjAdded);
            }
            GameObject newObjAdded1=Main.world.getObjTemplate(newObj2).createNewItem(newQua2,false);
            if(!player.addObjetSimiler(newObjAdded1,true,-1))
            {
              World.addGameObject(newObjAdded1,true);
              player.addObjet(newObjAdded1);
            }
          }
          else
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        return true;

      case 221:// IDitem,quantitï¿½ pour IDitem's,quantitï¿½ pour teleport
        try
        {
          String remove0=args.split(";")[0];
          String remove1=args.split(";")[1];
          String add=args.split(";")[4];
          int obj0=Integer.parseInt(remove0.split(",")[0]);
          int qua0=Integer.parseInt(remove0.split(",")[1]);
          int obj1=Integer.parseInt(remove1.split(",")[0]);
          int qua1=Integer.parseInt(remove1.split(",")[1]);
          int newObj1=Integer.parseInt(add.split(",")[0]);
          int newQua1=Integer.parseInt(add.split(",")[1]);
          if(player.hasItemTemplate(obj0,qua0)&&player.hasItemTemplate(obj1,qua1))
          {
            player.removeByTemplateID(obj0,qua0);
            player.removeByTemplateID(obj1,qua1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua0+"~"+obj0);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua1+"~"+obj1);
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+newQua1+"~"+newObj1);
            GameObject newObjAdded=Main.world.getObjTemplate(newObj1).createNewItem(newQua1,false);
            if(!player.addObjetSimiler(newObjAdded,true,-1))
            {
              World.addGameObject(newObjAdded,true);
              player.addObjet(newObjAdded);
            }
          }
          else
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
          
        }
        return true;

      case 222:// IDitem,quantitï¿½ pour IDitem's,quantitï¿½ pour teleport
        try
        {
          String remove0=args.split(";")[0];
          String remove1=args.split(";")[1];
          String remove2=args.split(";")[2];
          String remove3=args.split(";")[3];
          String add=args.split(";")[4];
          int verifMapId=Integer.parseInt(args.split(";")[5]);
          int obj0=Integer.parseInt(remove0.split(",")[0]);
          int qua0=Integer.parseInt(remove0.split(",")[1]);
          int obj1=Integer.parseInt(remove1.split(",")[0]);
          int qua1=Integer.parseInt(remove1.split(",")[1]);
          int obj2=Integer.parseInt(remove2.split(",")[0]);
          int qua2=Integer.parseInt(remove2.split(",")[1]);
          int obj3=Integer.parseInt(remove3.split(",")[0]);
          int qua3=Integer.parseInt(remove3.split(",")[1]);
          int mapID=Integer.parseInt(add.split(",")[0]);
          int cellID=Integer.parseInt(add.split(",")[1]);

          if(player.hasItemTemplate(obj0,qua0)&&player.hasItemTemplate(obj1,qua1)&&player.hasItemTemplate(obj2,qua2)&&player.hasItemTemplate(obj3,qua3))
          {
            player.removeByTemplateID(obj0,qua0);
            player.removeByTemplateID(obj1,qua1);
            player.removeByTemplateID(obj2,qua2);
            player.removeByTemplateID(obj3,qua3);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua0+"~"+obj0);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua1+"~"+obj1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua2+"~"+obj2);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua3+"~"+obj3);
            if(player.getFight()!=null||player.getCurMap().getId()!=verifMapId)
              return true;
            player.teleport((short)mapID,cellID);
          }
          else
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        return true;

      case 223:// IDitem,quantitï¿½ pour IDitem's,quantitï¿½
        try
        {
          String remove0=args.split(";")[0];
          String remove1=args.split(";")[1];
          String remove2=args.split(";")[2];
          String remove3=args.split(";")[3];
          String remove4=args.split(";")[4];
          String add=args.split(";")[5];
          int obj0=Integer.parseInt(remove0.split(",")[0]);
          int qua0=Integer.parseInt(remove0.split(",")[1]);
          int obj1=Integer.parseInt(remove1.split(",")[0]);
          int qua1=Integer.parseInt(remove1.split(",")[1]);
          int obj2=Integer.parseInt(remove2.split(",")[0]);
          int qua2=Integer.parseInt(remove2.split(",")[1]);
          int obj3=Integer.parseInt(remove3.split(",")[0]);
          int qua3=Integer.parseInt(remove3.split(",")[1]);
          int obj4=Integer.parseInt(remove4.split(",")[0]);
          int qua4=Integer.parseInt(remove4.split(",")[1]);
          int newItem=Integer.parseInt(add.split(",")[0]);
          int quaNewItem=Integer.parseInt(add.split(",")[1]);
          if(player.hasItemTemplate(obj0,qua0)&&player.hasItemTemplate(obj1,qua1)&&player.hasItemTemplate(obj2,qua2)&&player.hasItemTemplate(obj3,qua3)&&player.hasItemTemplate(obj4,qua4))
          {
            player.removeByTemplateID(obj0,qua0);
            player.removeByTemplateID(obj1,qua1);
            player.removeByTemplateID(obj2,qua2);
            player.removeByTemplateID(obj3,qua3);
            player.removeByTemplateID(obj4,qua4);
            GameObject newObjAdded=Main.world.getObjTemplate(newItem).createNewItem(quaNewItem,false);
            if(!player.addObjetSimiler(newObjAdded,true,-1))
            {
              World.addGameObject(newObjAdded,true);
              player.addObjet(newObjAdded);
            }
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua0+"~"+obj0);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua1+"~"+obj1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua2+"~"+obj2);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua3+"~"+obj3);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua4+"~"+obj4);
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+quaNewItem+"~"+newItem);
          }
          else
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        return true;

      case 224:// IDitem,quantitï¿½ pour IDitem's,quantitï¿½
        try
        {
          String remove0=args.split(";")[0];
          String remove1=args.split(";")[1];
          String remove2=args.split(";")[2];
          String remove3=args.split(";")[3];
          String add=args.split(";")[4];
          int obj0=Integer.parseInt(remove0.split(",")[0]);
          int qua0=Integer.parseInt(remove0.split(",")[1]);
          int obj1=Integer.parseInt(remove1.split(",")[0]);
          int qua1=Integer.parseInt(remove1.split(",")[1]);
          int obj2=Integer.parseInt(remove2.split(",")[0]);
          int qua2=Integer.parseInt(remove2.split(",")[1]);
          int obj3=Integer.parseInt(remove3.split(",")[0]);
          int qua3=Integer.parseInt(remove3.split(",")[1]);
          int newItem=Integer.parseInt(add.split(",")[0]);
          int quaNewItem=Integer.parseInt(add.split(",")[1]);
          if(player.hasItemTemplate(obj0,qua0)&&player.hasItemTemplate(obj1,qua1)&&player.hasItemTemplate(obj2,qua2)&&player.hasItemTemplate(obj3,qua3))
          {
            player.removeByTemplateID(obj0,qua0);
            player.removeByTemplateID(obj1,qua1);
            player.removeByTemplateID(obj2,qua2);
            player.removeByTemplateID(obj3,qua3);
            GameObject newObjAdded=Main.world.getObjTemplate(newItem).createNewItem(quaNewItem,false);
            if(!player.addObjetSimiler(newObjAdded,true,-1))
            {
              World.addGameObject(newObjAdded,true);
              player.addObjet(newObjAdded);
            }
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua0+"~"+obj0);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua1+"~"+obj1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua2+"~"+obj2);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua3+"~"+obj3);
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+quaNewItem+"~"+newItem);
          }
          else
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
          
        }
        return true;

      case 225:// IDitem,quantitï¿½ pour IDitem's,quantitï¿½
        try
        {
          String remove0=args.split(";")[0];
          String remove1=args.split(";")[1];
          String remove2=args.split(";")[2];
          String add=args.split(";")[3];
          int obj0=Integer.parseInt(remove0.split(",")[0]);
          int qua0=Integer.parseInt(remove0.split(",")[1]);
          int obj1=Integer.parseInt(remove1.split(",")[0]);
          int qua1=Integer.parseInt(remove1.split(",")[1]);
          int obj2=Integer.parseInt(remove2.split(",")[0]);
          int qua2=Integer.parseInt(remove2.split(",")[1]);
          int newItem=Integer.parseInt(add.split(",")[0]);
          int quaNewItem=Integer.parseInt(add.split(",")[1]);
          if(player.hasItemTemplate(obj0,qua0)&&player.hasItemTemplate(obj1,qua1)&&player.hasItemTemplate(obj2,qua2))
          {
            player.removeByTemplateID(obj0,qua0);
            player.removeByTemplateID(obj1,qua1);
            player.removeByTemplateID(obj2,qua2);
            GameObject newObjAdded=Main.world.getObjTemplate(newItem).createNewItem(quaNewItem,false);
            if(!player.addObjetSimiler(newObjAdded,true,-1))
            {
              World.addGameObject(newObjAdded,true);
              player.addObjet(newObjAdded);
            }
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua0+"~"+obj0);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua1+"~"+obj1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua2+"~"+obj2);
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+quaNewItem+"~"+newItem);
          }
          else
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 226://Animation + Condition voyage Ile Moon
        if(player.hasItemTemplate(1089,1)&&player.hasEquiped(1021)&&player.hasEquiped(1019)&&player.getCurMap().getId()==1014)
        {
          player.removeByTemplateID(1019,1);
          player.removeByTemplateID(1021,1);
          player.removeByTemplateID(1089,1);
          GameObject newObj1=Main.world.getObjTemplate(1020).createNewItem(1,false);
          if(!player.addObjetSimiler(newObj1,true,-1))
          {
            World.addGameObject(newObj1,true);
            player.addObjet(newObj1);
          }
          GameObject newObj2=Main.world.getObjTemplate(1022).createNewItem(1,false);
          if(!player.addObjetSimiler(newObj2,true,-1))
          {
            World.addGameObject(newObj2,true);
            player.addObjet(newObj2);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1089);
          SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"","2",player.getId()+"","1");
          player.teleport((short)437,411);
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
          return true;
        }
        break;

      case 227://Animation + Condition voyage Ile Wabbit
        long pKamas=player.getKamas();
        if(pKamas>=500&&player.getCurMap().getId()==167)
        {
          long pNewKamas=pKamas-500;
          if(pNewKamas<0)
            pNewKamas=0;
          player.setKamas(pNewKamas);
          Main.world.kamas_total -= 500;
          if(player.isOnline())
            SocketManager.GAME_SEND_STATS_PACKET(player);
          SocketManager.GAME_SEND_Im_PACKET(player,"046;"+500);
          SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"","2",player.getId()+"","2");
          player.teleport((short)833,141);
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"182");
        }
        break;

      case 228://Faire animation Hors Combat
        try
        {
          int AnimationId=Integer.parseInt(args);
          Animation animation=Main.world.getAnimation(AnimationId);
          if(player.getFight()!=null)
            return true;
          player.changeOrientation(1);
          SocketManager.GAME_SEND_GA_PACKET_TO_MAP(player.getCurMap(),"0",228,player.getId()+";"+cellid+","+Animation.PrepareToGA(animation),"");
        }
        catch(Exception e)
        {
          e.printStackTrace();
          
        }
        break;

      case 229://Animation d'incarnam ï¿½ astrub
        short map=Constant.getClassStatueMap(player.getClasse());
        int cell=Constant.getClassStatueCell(player.getClasse());
        SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"","2",player.getId()+"","7");
        player.teleport(map,cell);
        player.set_savePos(map+","+cell);
        SocketManager.GAME_SEND_Im_PACKET(player,"06");
        break;

     /* case 230://Point Boutique  
        try
        {
          int pts=Integer.parseInt(args);
          int ptsTotal=player.getAccount().getPoints()+pts;
          if(ptsTotal<0)
            ptsTotal=0;
          if(ptsTotal>50000)
            ptsTotal=50000;
          player.getAccount().setPoints(ptsTotal);
          if(player.isOnline())
            SocketManager.GAME_SEND_STATS_PACKET(player);
          SocketManager.GAME_SEND_MESSAGE(player,"You have gained "+pts+" votepoint(s). You now have "+ptsTotal+" votepoint(s).");
          return true;
        }
        catch(Exception e)
        {
          e.printStackTrace();
          Main.gameServer.a();
        }
        break;
*/
      case 231:// IDitem,quantitï¿½ pour IDitem's,quantitï¿½
        try
        {
          String remove=args.split(";")[0];
          String add=args.split(";")[1];
          int obj=Integer.parseInt(remove.split(",")[0]);
          int qua=Integer.parseInt(remove.split(",")[1]);
          int newItem=Integer.parseInt(add.split(",")[0]);
          int quaNewItem=Integer.parseInt(add.split(",")[1]);
          if(player.hasItemTemplate(obj,qua))
          {
            player.removeByTemplateID(obj,qua);
            GameObject newObjAdded=Main.world.getObjTemplate(newItem).createNewItem(quaNewItem,false);
            if(!player.addObjetSimiler(newObjAdded,true,-1))
            {
              World.addGameObject(newObjAdded,true);
              player.addObjet(newObjAdded);
            }
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+qua+"~"+obj);
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+quaNewItem+"~"+newItem);
          }
          else
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      //v2.7 - Replaced String += with StringBuilder  
      case 232://startFigthVersusMonstres args : monsterID,monsterLevel| ...
        if(player.getFight()!=null)
          return true;
        StringBuilder ValidMobGroup2=new StringBuilder();
        int pMap=player.getCurMap().getId();
        if(pMap==10131||pMap==10132||pMap==10133||pMap==10134||pMap==10135||pMap==10136||pMap==10137||pMap==10138)
        {
          try
          {
            for(String MobAndLevel : args.split("\\|"))
            {
              int monsterID=-1;
              int monsterLevel=-1;
              String[] MobOrLevel=MobAndLevel.split(",");
              monsterID=Integer.parseInt(MobOrLevel[0]);
              monsterLevel=Integer.parseInt(MobOrLevel[1]);

              if(Main.world.getMonstre(monsterID)==null||Main.world.getMonstre(monsterID).getGradeByLevel(monsterLevel)==null)
              {
                continue;
              }
              ValidMobGroup2.append(monsterID+","+monsterLevel+","+monsterLevel+";");
            }
            if(ValidMobGroup2.toString().isEmpty())
              return true;
            MobGroup group=new MobGroup(player.getCurMap().nextObjectId,player.getCurCell().getId(),ValidMobGroup2.toString());
            player.getCurMap().startFightVersusMonstres(player,group);// Si bug startfight, voir "//Respawn d'un groupe fix" dans fight.java
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        }
        else
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Vous ne pouvez pas commencer le combat ici, allez à l'arène!");
        }
        break;

      case 233: //Pierre d'ame en arï¿½ne
        try
        {
          int tID=Integer.parseInt(args.split(",")[0]);
          count=Integer.parseInt(args.split(",")[1]);
          boolean send=true;
          if(args.split(",").length>2)
            send=args.split(",")[2].equals("1");
          int pMap2=player.getCurMap().getId();
          if(pMap2==10131||pMap2==10132||pMap2==10133||pMap2==10134||pMap2==10135||pMap2==10136||pMap2==10137||pMap2==10138)
          {
            //Si on ajoute
            if(count>0)
            {
              ObjectTemplate T=Main.world.getObjTemplate(tID);
              if(T==null)
                return true;
              GameObject O=T.createNewItem(count,false);
              //Si retourne true, on l'ajoute au monde
              if(player.addObjet(O,true))
                World.addGameObject(O,true);
            }
            else
            {
              player.removeByTemplateID(tID,-count);
            }
            //Si en ligne (normalement oui)
            if(player.isOnline())//on envoie le packet qui indique l'ajout//retrait d'un item
            {
              SocketManager.GAME_SEND_Ow_PACKET(player);
              if(send)
              {
                if(count>=0)
                {
                  SocketManager.GAME_SEND_Im_PACKET(player,"021;"+count+"~"+tID);
                }
                else if(count<0)
                {
                  SocketManager.GAME_SEND_Im_PACKET(player,"022;"+-count+"~"+tID);
                }
              }
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 234: //Ajout d'un objet en fonction de la map
        int IdObj=Short.parseShort(args.split(";")[0]);
        int MapId=Integer.parseInt(args.split(";")[1]);
        if(player.getCurMap().getId()!=MapId)
          return true;
        if(!player.hasItemTemplate(IdObj,1))
        {
          GameObject newObjAdded=Main.world.getObjTemplate(IdObj).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded,true,-1))
          {
            World.addGameObject(newObjAdded,true);
            player.addObjet(newObjAdded);
          }
        }
        else
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Tu as déjà cet objet");
        }
        break;

      case 235:// IDitem,quantitï¿½ pour IDitem's,quantitï¿½
        if(player.getCurMap().getId()==713)
        {
          if(player.hasItemTemplate(757,1)&&player.hasItemTemplate(368,1)&&player.hasItemTemplate(369,1)&&!player.hasItemTemplate(960,1))
          {
            player.removeByTemplateID(757,1);
            player.removeByTemplateID(368,1);
            player.removeByTemplateID(369,1);

            GameObject newObjAdded=Main.world.getObjTemplate(960).createNewItem(1,false);
            if(!player.addObjetSimiler(newObjAdded,true,-1))
            {
              World.addGameObject(newObjAdded,true);
              player.addObjet(newObjAdded);
            }
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+757);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+368);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+369);
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+960);
          }
          else
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
          }
        }
        break;

      case 300://Sort arakne
        if(player.getCurMap().getId()==1559&&player.hasItemTemplate(973,1))
        {
          player.removeByTemplateID(973,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+973);
          player.learnSpell(370,1,true,true,true);
        }
        break;

      /*
       * case 92://bonbon try { if(perso.getCandyId() != 0)
       * SocketManager.send(perso, "OR1"); int t =
       * Main.world.getGameObject(itemID).getTemplate().getId();
       * perso.removeByTemplateID(t, 1); perso.setCandy(t);
       * SocketManager.GAME_SEND_STATS_PACKET(perso); }catch (Exception e)
       * {} break;
       */

      case 239://Ouvrir l'interface d'oublie de sort
        player.setExchangeAction(new ExchangeAction<>(ExchangeAction.FORGETTING_SPELL,0));
        SocketManager.GAME_SEND_FORGETSPELL_INTERFACE('+',player);
        break;

      case 241:
    	  try {
        if(player.getKamas()>=10&&player.getCurMap().getId()==6863)
        {
          if(player.hasItemTemplate(6653,1))
          {
            String date=player.getItemTemplate(6653,1).getTxtStat().get(Constant.STATS_DATE);
            long timeStamp=Long.parseLong(date);
            if(System.currentTimeMillis()-timeStamp<=86400000)
            {
              SocketManager.GAME_SEND_MESSAGE(player,"Votre billet est valide.");
              return true;
            }
            else
            {
              SocketManager.GAME_SEND_MESSAGE(player,"Votre ticket a expiré, vous devez en acheter un nouveau");
              player.removeByTemplateID(6653,1);
            }
          }
          long rK=player.getKamas()-10;
          if(rK<0)
            rK=0;
          player.setKamas(rK);
          Main.world.kamas_total -= 10;
          if(player.isOnline())
            SocketManager.GAME_SEND_STATS_PACKET(player);
          ObjectTemplate OT=Main.world.getObjTemplate(6653);
          GameObject obj=OT.createNewItem(1,false);
          if(player.addObjet(obj,true))//Si le joueur n'avait pas d'item similaire
            World.addGameObject(obj,true);
          obj.refreshStatsObjet("325#0#0#"+System.currentTimeMillis());
          //Database.getStatics().getPlayerData().update(player);
          SocketManager.GAME_SEND_Ow_PACKET(player);
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+6653);
        }
    	  }
          catch(Exception e)
          {
           
          }
        break;

      /** Ile Moon **/
      case 450: //Ortimus
        if(player.getCurMap().getId()==1844&&player.getKamas()>=5000&&player.hasItemTemplate(363,5))
        {
          player.removeByTemplateID(363,5);
          long rK=player.getKamas()-5000;
          if(rK<0)
            rK=0;
          player.setKamas(rK);
          Main.world.kamas_total -= 5000;
          if(player.isOnline())
            SocketManager.GAME_SEND_STATS_PACKET(player);
          GameObject newObjAdded=Main.world.getObjTemplate(998).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded,true,-1))
          {
            World.addGameObject(newObjAdded,true);
            player.addObjet(newObjAdded);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+998);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+5+"~"+363);
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
        }
        break;

      case 451: //Employï¿½ de l'agence Touriste
        if(player.getKamas()>=200&&player.getCurMap().getId()==436)
        {
          long rK=player.getKamas()-200;
          if(rK<0)
            rK=0;
          player.setKamas(rK);
          Main.world.kamas_total -= 200;
          if(player.isOnline())
            SocketManager.GAME_SEND_STATS_PACKET(player);
          GameObject newObjAdded=Main.world.getObjTemplate(1004).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded,true,-1))
          {
            World.addGameObject(newObjAdded,true);
            player.addObjet(newObjAdded);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+1004);
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
        }
        break;

      case 452: //Kib Roche
        if(player.hasItemTemplate(1000,6)&&player.hasItemTemplate(1003,1)&&player.hasItemTemplate(1018,10)&&player.hasItemTemplate(998,1)&&player.hasItemTemplate(1002,1)&&player.hasItemTemplate(999,1)&&player.hasItemTemplate(1004,4)&&player.hasItemTemplate(1001,2)&&player.getCurMap().getId()==437)
        {
          player.removeByTemplateID(1000,6);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+6+"~"+1000);
          player.removeByTemplateID(1003,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1003);
          player.removeByTemplateID(1018,10);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+10+"~"+1018);
          player.removeByTemplateID(998,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+998);
          player.removeByTemplateID(1002,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1002);
          player.removeByTemplateID(999,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+99);
          player.removeByTemplateID(1004,4);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+4+"~"+1004);
          player.removeByTemplateID(1001,2);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+2+"~"+1001);
          GameObject newObjAdded=Main.world.getObjTemplate(6716).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded,true,-1))
          {
            World.addGameObject(newObjAdded,true);
            player.addObjet(newObjAdded);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+6716);
          player.teleport((short)1701,247);
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
        }
        break;

      case 453: //Kanniboul cuisininer : fonctionnel avec les 4 carapaces
        if(player.hasItemTemplate(1010,1)&&player.hasItemTemplate(1011,1)&&player.hasItemTemplate(1012,1)&&player.hasItemTemplate(1013,1)&&player.getCurMap().getId()==1714)
        {
          player.removeByTemplateID(1010,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1010);
          player.removeByTemplateID(1011,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1011);
          player.removeByTemplateID(1012,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1012);
          player.removeByTemplateID(1013,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1013);
          player.teleport((short)1766,332);
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
        }
        break;

      case 454: //masque kanniboul : fonctionnel
        if(player.hasEquiped(1088)&&player.getCurMap().getId()==1764)
        {
          player.teleport((short)1765,226);
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
        }
        break;

      case 455://Issï¿½ Heau : peinture noire : fonctionnel
        if(player.hasItemTemplate(1006,1)&&player.hasItemTemplate(1007,1)&&player.hasItemTemplate(1008,1)&&player.hasItemTemplate(1009,1)&&player.getCurMap().getId()==1838)
        {
          player.removeByTemplateID(1006,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1006);
          player.removeByTemplateID(1007,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1007);
          player.removeByTemplateID(1008,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1008);
          player.removeByTemplateID(1009,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1009);
          GameObject newObjAdded=Main.world.getObjTemplate(1086).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded,true,-1))
          {
            World.addGameObject(newObjAdded,true);
            player.addObjet(newObjAdded);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+1086);
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
        }
        break;

      case 456: //Obtention du masque kanniboul : fonctionnel
        if(player.hasItemTemplate(1014,1)&&player.hasItemTemplate(1015,1)&&player.hasItemTemplate(1016,1)&&player.hasItemTemplate(1017,1)&&player.hasItemTemplate(1086,1)&&player.getCurMap().getId()==425)
        {
          player.removeByTemplateID(1014,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1014);
          player.removeByTemplateID(1015,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1015);
          player.removeByTemplateID(1016,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1016);
          player.removeByTemplateID(1017,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1017);
          player.removeByTemplateID(1086,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1086);
          GameObject newObjAdded=Main.world.getObjTemplate(1088).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded,true,-1))
          {
            World.addGameObject(newObjAdded,true);
            player.addObjet(newObjAdded);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+1088);

          NpcQuestion quest=Main.world.getNPCQuestion(577);
          if(quest==null)
          {
            SocketManager.GAME_SEND_END_DIALOG_PACKET(player.getGameClient());
            player.setExchangeAction(null);
            return true;
          }
          try
          {
            SocketManager.GAME_SEND_QUESTION_PACKET(player.getGameClient(),quest.parse(player));
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"14|43");
        }
        break;

      case 457://Vente ticket ï¿½le moon
        if(player.getKamas()>=1000&&player.getCurMap().getId()==1014)
        {
          player.setKamas(player.getKamas()-1000);
          Main.world.kamas_total -= 1000;
          GameObject newObjAdded11=Main.world.getObjTemplate(1089).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded11,true,-1))
          {
            World.addGameObject(newObjAdded11,true);
            player.addObjet(newObjAdded11);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+1089);
          SocketManager.GAME_SEND_STATS_PACKET(player);
        }
        break;
      /** Fin Ile Moon **/

      /** Donjon Condition **/
      case 500:// Donjon Bouftou : Rï¿½compense.
        if(player.getCurMap().getId()!=2084)
          return true;
        player.teleport((short)1856,226);
       /* GameObject newObjAdded=Main.world.getObjTemplate(1728).createNewItem(1,false);
        if(!player.addObjetSimiler(newObjAdded,true,-1))
        {
          World.addGameObject(newObjAdded,true);
          player.addObjet(newObjAdded);
        }
        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+1728);
        */
        break;

      case 501:// Donjon Bwork : Rï¿½compense.
        if(player.getCurMap().getId()!=9767)
          return true;
        player.teleport((short)9470,198);
        GameObject newObjAdded1=Main.world.getObjTemplate(8000).createNewItem(1,false);
        if(!player.addObjetSimiler(newObjAdded1,true,-1))
        {
          World.addGameObject(newObjAdded1,true);
          player.addObjet(newObjAdded1);
        }
        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+8000);
        break;

      case 502://Entrï¿½ Donjon Cawotte
        if(player.hasEquiped(969)&&player.hasEquiped(970)&&player.hasEquiped(971)&&player.getCurMap().getId()==1781)
        {
          player.teleport((short)1787,394);
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"11;");
        }
        break;

      case 503://Sortie Donjon Cawotte
        if(player.getCurMap().getId()!=1795)
          return true;
        if(player.hasItemTemplatev3(969,1)&&player.hasItemTemplatev3(970,1)&&player.hasItemTemplatev3(971,1))
        {
          player.removeByTemplateID(969,1);
          player.removeByTemplateID(970,1);
          player.removeByTemplateID(971,1);
          GameObject newObjAdded11=Main.world.getObjTemplate(972).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded11,true,-1))
          {
            World.addGameObject(newObjAdded11,true);
            player.addObjet(newObjAdded11);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+972);
        }
        player.teleport((short)1781,227);
        break;

      case 504://Sortie Donjon Koulosse
        if(player.getCurMap().getId()==9717)
        {
          int type111=0;
          try
          {
            type111=Integer.parseInt(args);
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
          if(type111==1)
          {
            GameObject newObjAdded11=Main.world.getObjTemplate(7890).createNewItem(1,false);
            if(!player.addObjetSimiler(newObjAdded11,true,-1))
            {
              World.addGameObject(newObjAdded11,true);
              player.addObjet(newObjAdded11);
            }
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+7890);
          }
          if(type111==2)
          {
            GameObject newObjAdded11=Main.world.getObjTemplate(7889).createNewItem(1,false);
            if(!player.addObjetSimiler(newObjAdded11,true,-1))
            {
              World.addGameObject(newObjAdded11,true);
              player.addObjet(newObjAdded11);
            }
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+7889);
          }
          if(type111==3)
          {
            GameObject newObjAdded11=Main.world.getObjTemplate(7888).createNewItem(1,false);
            if(!player.addObjetSimiler(newObjAdded11,true,-1))
            {
              World.addGameObject(newObjAdded11,true);
              player.addObjet(newObjAdded11);
            }
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+7888);
          }
          if(type111==4)
          {
            GameObject newObjAdded11=Main.world.getObjTemplate(7887).createNewItem(1,false);
            if(!player.addObjetSimiler(newObjAdded11,true,-1))
            {
              World.addGameObject(newObjAdded11,true);
              player.addObjet(newObjAdded11);
            }
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+7887);
          }
          player.teleport((short)8905,431);
        }
        break;

      case 505://Learn spell Apprivoisement
        if(player.getCurMap().getId()==9717)
        {
          if(player.hasItemTemplate(7904,50)&&player.hasItemTemplate(7903,50))
          {
            player.removeByTemplateID(7904,50);
            player.removeByTemplateID(7903,50);
            player.learnSpell(414,1,true,true,true);
          }
        }
        break;

      case 506://Entrï¿½ Donjon Koulosse
        if(player.getCurMap().getId()==8905&&player.getCurCell().getId()==213)
        {
          if(player.hasItemTemplate(7908,1))
          {
            player.removeByTemplateID(7908,1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+7908);
            player.teleport((short)8950,408);
          }
          else
          {
            SocketManager.GAME_SEND_MESSAGE(player,"Vous n'avez pas la clé nécessaire.");
          }
        }
        else
        {
          SocketManager.GAME_SEND_MESSAGE(player,"Vous n'êtes pas devant le PNJ.");
        }
        break;

      case 507://Donjon Gelï¿½e
        int type3=0;
        if(player.getCurMap().getId()==6823)
        {
          try
          {
            type3=Integer.parseInt(args);
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
          switch(type3)
          {
            case 1://Menthe -> 2433
             
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2433)+"~"+2433);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2432)+"~"+2432);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2431)+"~"+2431);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2430)+"~"+2430);
                player.removeByTemplateID(2430,player.getNbItemTemplate(2430));
                player.removeByTemplateID(2431,player.getNbItemTemplate(2431));
                player.removeByTemplateID(2432,player.getNbItemTemplate(2432));
                player.removeByTemplateID(2433,player.getNbItemTemplate(2433));
                player.teleport((short)6834,422);
              
            
              break;
            case 2://Fraise -> 2432
             
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2433)+"~"+2433);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2432)+"~"+2432);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2431)+"~"+2431);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2430)+"~"+2430);
                player.removeByTemplateID(2430,player.getNbItemTemplate(2430));
                player.removeByTemplateID(2431,player.getNbItemTemplate(2431));
                player.removeByTemplateID(2432,player.getNbItemTemplate(2432));
                player.removeByTemplateID(2433,player.getNbItemTemplate(2433));
                player.teleport((short)6833,422);
              
              break;
            case 3://Citron -> 2431
            
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2433)+"~"+2433);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2432)+"~"+2432);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2431)+"~"+2431);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2430)+"~"+2430);
                player.removeByTemplateID(2430,player.getNbItemTemplate(2430));
                player.removeByTemplateID(2431,player.getNbItemTemplate(2431));
                player.removeByTemplateID(2432,player.getNbItemTemplate(2432));
                player.removeByTemplateID(2433,player.getNbItemTemplate(2433));
                player.teleport((short)6832,422);
             
              break;
            case 4://Bleue -> 2430
              
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2433)+"~"+2433);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2432)+"~"+2432);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2431)+"~"+2431);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2430)+"~"+2430);
                player.removeByTemplateID(2430,player.getNbItemTemplate(2430));
                player.removeByTemplateID(2431,player.getNbItemTemplate(2431));
                player.removeByTemplateID(2432,player.getNbItemTemplate(2432));
                player.removeByTemplateID(2433,player.getNbItemTemplate(2433));
                player.teleport((short)6831,422);
              
              break;
            case 5://Gelï¿½e x10 => 2433,2432,2431,2430
             
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2433)+"~"+2433);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2432)+"~"+2432);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2431)+"~"+2431);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2430)+"~"+2430);
                player.removeByTemplateID(2430,player.getNbItemTemplate(2430));
                player.removeByTemplateID(2431,player.getNbItemTemplate(2431));
                player.removeByTemplateID(2432,player.getNbItemTemplate(2432));
                player.removeByTemplateID(2433,player.getNbItemTemplate(2433));
                player.teleport((short)6835,422);
             
              break;
            case 6://Menthe -> 2433
              
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2433)+"~"+2433);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2432)+"~"+2432);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2431)+"~"+2431);
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+player.getNbItemTemplate(2430)+"~"+2430);
                player.removeByTemplateID(2430,player.getNbItemTemplate(2430));
                player.removeByTemplateID(2431,player.getNbItemTemplate(2431));
                player.removeByTemplateID(2432,player.getNbItemTemplate(2432));
                player.removeByTemplateID(2433,player.getNbItemTemplate(2433));
                player.teleport((short)6836,422);
             
              break;
          }
        }
        break;

      case 508://Donjon Kitsoune : Rï¿½compense
        if(player.getCurMap().getId()==8317)
        {
          player.teleport((short)8236,370);
          GameObject newObjAdded11=Main.world.getObjTemplate(7415).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded11,true,-1))
          {
            World.addGameObject(newObjAdded11,true);
            player.addObjet(newObjAdded11);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+7415);
        }
        break;

      case 509://Donjon Bworker : Rï¿½compense
        player.teleport((short)4786,300);
        GameObject newObjAdded11=Main.world.getObjTemplate(6885).createNewItem(1,false);
        if(!player.addObjetSimiler(newObjAdded11,true,-1))
        {
          World.addGameObject(newObjAdded11,true);
          player.addObjet(newObjAdded11);
        }
        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+6885);
        GameObject newObjAdded12=Main.world.getObjTemplate(8388).createNewItem(1,false);
        if(!player.addObjetSimiler(newObjAdded12,true,-1))
        {
          World.addGameObject(newObjAdded12,true);
          player.addObjet(newObjAdded12);
        }
        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+8388);
        break;

      case 510://Jeton Bwroker Vs Packet
        if(player.getCurMap().getId()==3373&&player.hasItemTemplate(6885,1))
        {
          player.removeByTemplateID(6885,1);
          GameObject newObjAdded121=Main.world.getObjTemplate(6887).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded121,true,-1))
          {
            World.addGameObject(newObjAdded121,true);
            player.addObjet(newObjAdded121);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+6885);
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+6887);
        }
        break;

      case 511://Cadeau Bworker
        int cadeau=Loterie.getCadeauBworker();
        GameObject newObjAdded121=Main.world.getObjTemplate(cadeau).createNewItem(1,false);
        if(!player.addObjetSimiler(newObjAdded121,true,-1))
        {
          World.addGameObject(newObjAdded121,true);
          player.addObjet(newObjAdded121);
        }
        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+cadeau);
        break;

      case 512://Rat Blanc : Rï¿½compense
        if(player.getCurMap().getId()==10213)
        {
          player.teleport((short)6536,273);
          GameObject newObjAdded111=Main.world.getObjTemplate(8476).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded111,true,-1))
          {
            World.addGameObject(newObjAdded111,true);
            player.addObjet(newObjAdded111);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+8476);
        }
        break;

      case 513://Rat noir : Rï¿½compense
        if(player.getCurMap().getId()==10199)
        {
          player.teleport((short)6738,213);
          GameObject newObjAdded111=Main.world.getObjTemplate(8477).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded111,true,-1))
          {
            World.addGameObject(newObjAdded111,true);
            player.addObjet(newObjAdded111);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+8477);
        }
        break;

      case 514://Splinter Cell : Entrï¿½
        if(player.getCurMap().getId()==9638&&player.hasItemTemplate(8476,1)&&player.hasItemTemplate(8477,1))
        {
          player.teleport((short)10141,448);
          player.removeByTemplateID(8476,1);
          player.removeByTemplateID(8477,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+8476);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+8477);
        }
        break;

      case 515://Rï¿½compense Pandikaze
        if(player.getCurMap().getId()==8497)
        {
          player.teleport((short)8167,252);
          GameObject newObjAdded111=Main.world.getObjTemplate(7414).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded111,true,-1))
          {
            World.addGameObject(newObjAdded111,true);
            player.addObjet(newObjAdded111);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+7414);
        }
        break;

      case 516://Chenil achat poudre eniripsa
        if(player.getCurMap().getId()==1140&&player.getKamas()>=1000)
        {
          GameObject newObjAdded111=Main.world.getObjTemplate(2239).createNewItem(1,false);
          if(!player.addObjetSimiler(newObjAdded111,true,-1))
          {
            World.addGameObject(newObjAdded111,true);
            player.addObjet(newObjAdded111);
          }
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+2239);
        }
        break;

      case 517://Entrï¿½e donjon familier 9052,268 - O: 7 - Transformation donjon abra
        mapId=Integer.parseInt(args.split(";")[0].split(",")[0]);
        int cellId=Integer.parseInt(args.split(";")[0].split(",")[1]);
        short mapSecu=Short.parseShort(args.split(";")[1]);
        int id=Integer.parseInt(args.split(";")[2]);
        if(player.getCurMap().getId()!=mapSecu)
          return true;
        if(player.getCurMap().getId()==9052)
        {
          if(player.getCurCell().getId()!=268||player.get_orientation()!=7)
            return true;
          if(!player.hasItemType(90))
          {
            SocketManager.GAME_SEND_MESSAGE(player,"Vous n'avez pas de fantôme animal.");
            return true;
          }
        }
        player.teleport((short)mapId,cellId);
        player.setFullMorph(id,false,false);
        break;

      case 518://Dï¿½morph + TP : Donjon abra, familier
        mapId=Integer.parseInt(args.split(";")[0].split(",")[0]);
        cellId=Integer.parseInt(args.split(";")[0].split(",")[1]);
        mapSecu=Short.parseShort(args.split(";")[1]);
        if(player.getCurMap().getId()!=mapSecu)
          return true;
        player.unsetFullMorph();
        player.teleport((short)mapId,cellId);
        break;

      case 519://Donjon Grotte Hesque, Arche, Rasboul, Tynril
        mapId=Integer.parseInt(args.split(";")[0].split(",")[0]);
        cellId=Integer.parseInt(args.split(";")[0].split(",")[1]);
        mapSecu=Short.parseShort(args.split(";")[1]);
        if(player.getCurMap().getId()!=mapSecu)
          return true;
       // GameObject obj1=Main.world.getObjTemplate(Integer.parseInt(args.split(";")[2])).createNewItem(1,false);
       // if(obj1!=null)
        //  if(player.addObjet(obj1,true))
        //    World.addGameObject(obj1,true);
        //player.send("Im021;1~"+args.split(";")[2]);
        GameObject obj1=Main.world.getObjTemplate(Integer.parseInt(args.split(";")[3])).createNewItem(1,false);
        if(obj1!=null)
          if(player.addObjet(obj1,true))
            World.addGameObject(obj1,true);
        player.send("Im021;1~"+args.split(";")[3]);
        player.teleport((short)mapId,cellId);
        break;

      case 520://Dj pandikaze
        if(player.getCurMap().getId()!=8497)
          return true;

        obj1=Main.world.getObjTemplate(7414).createNewItem(1,false);
        if(player.addObjet(obj1,true))
          World.addGameObject(obj1,true);
        player.send("Im021;1~7414");
        if(!player.getEmotes().contains(15))
        {
          obj1=Main.world.getObjTemplate(7413).createNewItem(1,false);
          if(player.addObjet(obj1,true))
            World.addGameObject(obj1,true);
          player.send("Im021;1~7413");
        }

        player.teleport((short)8167,252);
        break;

      case 521://Echange clef skeunk
        if(player.getCurMap().getId()!=9248)
          return true;

        if(player.hasItemTemplate(7887,1)&&player.hasItemTemplate(7888,1)&&player.hasItemTemplate(7889,1)&&player.hasItemTemplate(7890,1))
        {
          player.removeByTemplateID(7887,1);
          player.removeByTemplateID(7888,1);
          player.removeByTemplateID(7889,1);
          player.removeByTemplateID(7890,1);
          player.send("Im022;1~7887");
          player.send("Im022;1~7888");
          player.send("Im022;1~7889");
          player.send("Im022;1~7890");

          obj1=Main.world.getObjTemplate(8073).createNewItem(1,false);
          if(player.addObjet(obj1,true))
            World.addGameObject(obj1,true);
          player.send("Im021;1~8073");
        }
        else
        {
          player.send("Im119|45");
        }
        break;

      case 522://Pï¿½ki pï¿½ki
        if(player.getCurMap().getId()!=8349)
          return true;

        //obj1=Main.world.getObjTemplate(6978).createNewItem(1,false);
        //if(player.addObjet(obj1,true))
        //  World.addGameObject(obj1,true);
        //player.send("Im021;1~6978");
        player.teleport((short)8467,227);
        break;

      case 523://Cawotte vs spell
        if(player.getCurMap().getId()!=1779)
          return true;
        if(client==null)
          return true;

        if(player.hasItemTemplate(361,100))
        {
          player.removeByTemplateID(361,100);
          player.send("Im022;100~361");
          player.learnSpell(367,1,true,true,true);

          NpcQuestion quest=Main.world.getNPCQuestion(473);
          if(quest==null)
          {
            SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
            player.setExchangeAction(null);
            return true;
          }
          SocketManager.GAME_SEND_QUESTION_PACKET(client,quest.parse(player));
          return false;
        }
        else
        {
          player.send("Im14");
        }
        break;

      case 524://Rï¿½ponse Maitre corbac
        if(client==null)
          return true;
        int qID=-1;
        NpcQuestion quest=Main.world.getNPCQuestion(qID);
        if(quest==null)
        {
          SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
          player.setExchangeAction(null);
          return true;
        }
        SocketManager.GAME_SEND_QUESTION_PACKET(client,quest.parse(player));
        return false;

      case 525://EndFight Action Maitre Corbac
        MobGroup group=player.hasMobGroup();

        split=args.split(";");

        for(MobGrade mb : group.getMobs().values())
        {
          switch(mb.getTemplate().getId())
          {
            case 289:
              player.teleport((short)9604,403);
              return true;
            case 819:
              player.teleport(Short.parseShort(split[0].split(",")[0]),Integer.parseInt(split[0].split(",")[1]));
              return true;
            case 820:
              player.teleport(Short.parseShort(split[1].split(",")[0]),Integer.parseInt(split[1].split(",")[1]));
              return true;
          }
        }
        break;

      case 526://Fin donjon maitre corbac
        if(player.getCurMap().getId()!=9604)
          return true;

        obj1=Main.world.getObjTemplate(7703).createNewItem(1,false);
        if(player.addObjet(obj1,true))
          World.addGameObject(obj1,true);
        player.send("Im021;1~7703");
        player.teleport((short)2985,279);
        break;

      case 527://Donjon ensablï¿½ fin
        if(player.getCurMap().getId()!=10165)
          return true;

        player.addStaticEmote(19);
        player.teleport((short)10155,210);
        break;
      case 610://Jeton Bwroker Vs Packet 10
          if(player.getCurMap().getId()==3373&&player.hasItemTemplate(6885,10))
          {
            player.removeByTemplateID(6885,10);
            GameObject newObjAdded1212=Main.world.getObjTemplate(6887).createNewItem(10,false);
            if(!player.addObjetSimiler(newObjAdded1212,true,-1))
            {
              World.addGameObject(newObjAdded1212,true);
              player.addObjet(newObjAdded1212);
            }
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+6885);
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+6887);
          }
          break;

      case 964://Signer le registre
        if(client==null)
          return true;
        if(player.getCurMap().getId()!=10255)
          return true;
        if(player.get_align()!=1&&player.get_align()!=2)
          return true;
        if(player.hasItemTemplate(9487,1) && Config.getInstance().HEROIC)
        {
          String date=player.getItemTemplate(9487,1).getTxtStat().get(Constant.STATS_DATE);
          long timeStamp=Long.parseLong(date);
          if(System.currentTimeMillis()-timeStamp<=1209600000) // 14 jours
          {
            return true;
          }
          else
          {
            player.removeByTemplateID(9487,1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+9487);
          }
        }

        if(player.hasItemTemplate(9811,1)) // Formulaire de neutralitï¿½
        {
          player.removeByTemplateID(9811,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+9811);
          player.modifAlignement(0);
        }
        else if(player.hasItemTemplate(9812,1)) // Formulaire de dï¿½sertion
        {
          if(player.hasItemTemplate(9488,1))
          {
            player.removeByTemplateID(9488,1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+9488);
            player.modifAlignement(1);
          }
          else if(player.hasItemTemplate(9489,1))
          {
            player.removeByTemplateID(9489,1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+9489);
            player.modifAlignement(2);
          }
          player.removeByTemplateID(9812,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+9812);
        }

        ObjectTemplate t2=Main.world.getObjTemplate(9487);
        GameObject obj2=t2.createNewItem(1,false);
        obj2.refreshStatsObjet("325#0#0#"+System.currentTimeMillis());
        if(player.addObjet(obj2,false))
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+obj2.getTemplate().getId());
          World.addGameObject(obj2,true);
        }

        quest=Main.world.getNPCQuestion(Integer.parseInt(this.args));
        if(quest==null)
        {
          SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
          player.setExchangeAction(null);
          return true;
        }
        try
        {
          SocketManager.GAME_SEND_QUESTION_PACKET(client,quest.parse(player));
          return false;
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 965://Signer le document officiel
        if(client==null)
          return true;
        if(player.getCurMap().getId()!=10255)
          return true;
        if(player.get_align()!=1&&player.get_align()!=2)
          return true;
        if(player.hasItemTemplate(9487,1) && Config.getInstance().HEROIC)
        {
          String date=player.getItemTemplate(9487,1).getTxtStat().get(Constant.STATS_DATE);
          long timeStamp=Long.parseLong(date);
          if(System.currentTimeMillis()-timeStamp<=1209600000) // 14 jours
          {
            return true;
          }
        }

        boolean next=false;
        if(player.hasItemTemplate(9811,1)) // Formulaire de neutralitï¿½
        {
          next=true;
        }
        else if(player.hasItemTemplate(9812,1)) // Formulaire de dï¿½sertion
        {
          int idTemp=-1;
          if(player.get_align()==2) // Brak, donc passer bont
            idTemp=9488;
          else
            idTemp=9489;

          ObjectTemplate t=Main.world.getObjTemplate(idTemp);
          GameObject obj=t.createNewItem(1,false);
          obj.refreshStatsObjet("325#0#0#"+System.currentTimeMillis());
          if(player.addObjet(obj,false))
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+obj.getTemplate().getId());
            World.addGameObject(obj,true);
          }
          next=true;
        }

        if(next)
        {
          quest=Main.world.getNPCQuestion(Integer.parseInt(this.args));
          if(quest==null)
          {
            SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
            player.setExchangeAction(null);
            return true;
          }
          try
          {
            SocketManager.GAME_SEND_QUESTION_PACKET(client,quest.parse(player));
            return false;
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        }
        break;

      case 963://Formulaire de dï¿½sertion
        if(client==null)
          return true;
        if(player.getCurMap().getId()!=10255)
          return true;
        if(player.get_align()!=1&&player.get_align()!=2)
          return true;
        if(player.hasItemTemplate(9487,1) && Config.getInstance().HEROIC)
        {
          String date=player.getItemTemplate(9487,1).getTxtStat().get(Constant.STATS_DATE);
          long timeStamp=Long.parseLong(date);
          if(System.currentTimeMillis()-timeStamp<=1209600000) // 14 jours
          {
            return true;
          }
        }

        t2=Main.world.getObjTemplate(9812);
        obj2=t2.createNewItem(1,false);
        obj2.refreshStatsObjet("325#0#0#"+System.currentTimeMillis());
        if(player.addObjet(obj2,false))
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+obj2.getTemplate().getId());
          World.addGameObject(obj2,true);
        }

        quest=Main.world.getNPCQuestion(Integer.parseInt(this.args));
        if(quest==null)
        {
          SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
          player.setExchangeAction(null);
          return true;
        }
        try
        {
          SocketManager.GAME_SEND_QUESTION_PACKET(client,quest.parse(player));
          return false;
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 966://Formulaire de neutralitï¿½
        if(client==null)
          return true;
        if(player.getCurMap().getId()!=10255)
          return true;
        if(player.get_align()!=1&&player.get_align()!=2)
          return true;
        if(player.hasItemTemplate(9487,1) && Config.getInstance().HEROIC)
        {
          String date=player.getItemTemplate(9487,1).getTxtStat().get(Constant.STATS_DATE);
          long timeStamp=Long.parseLong(date);
          if(System.currentTimeMillis()-timeStamp<=1209600000) // 14 jours
          {
            return true;
          }
        }

        int kamas=256000;
        if(player.getALvl()<=10)
          kamas=500;
        else if(player.getALvl()<=20)
          kamas=1000;
        else if(player.getALvl()<=30)
          kamas=2000;
        else if(player.getALvl()<=40)
          kamas=4000;
        else if(player.getALvl()<=50)
          kamas=8000;
        else if(player.getALvl()<=60)
          kamas=16000;
        else if(player.getALvl()<=70)
          kamas=32000;
        else if(player.getALvl()<=80)
          kamas=64000;
        else if(player.getALvl()<=90)
          kamas=128000;
        else if(player.getALvl()<=100)
          kamas=256000;

        if(player.getKamas()<kamas)
        {
          SocketManager.GAME_SEND_MESSAGE_SERVER(player,"10|"+kamas);
          return true;
        }
        else
        {
        	 Main.world.kamas_total -= kamas;
          player.setKamas(player.getKamas()-kamas);
          SocketManager.GAME_SEND_STATS_PACKET(player);
          SocketManager.GAME_SEND_Im_PACKET(player,"046;"+kamas);

          if(player.hasItemTemplate(9811,1))
          {
            player.removeByTemplateID(9811,1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+9811);
          }

          ObjectTemplate t=Main.world.getObjTemplate(9811);
          GameObject obj=t.createNewItem(1,false);
          obj.refreshStatsObjet("325#0#0#"+System.currentTimeMillis());
          if(player.addObjet(obj,false))
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+obj.getTemplate().getId());
            World.addGameObject(obj,true);
          }

          quest=Main.world.getNPCQuestion(Integer.parseInt(this.args));
          if(quest==null)
          {
            SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
            player.setExchangeAction(null);
            return true;
          }
          try
          {
            SocketManager.GAME_SEND_QUESTION_PACKET(client,quest.parse(player));
            return false;
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        }
        break;

      case 967://Apprendre bricoleur
        if(client==null)
          return true;
        if(player.getCurMap().getId()!=8736&&player.getCurMap().getId()!=8737)
          return true;

        Job job=Main.world.getMetier(65);
        if(job==null)
          return true;

        if(player.getMetierByID(job.getId())!=null)
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"111");
          SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
          player.setExchangeAction(null);
          player.setIsOnDialogAction(-1);
          return true;
        }

        if(player.getMetierByID(2)!=null&&player.getMetierByID(2).get_lvl()<30||player.getMetierByID(11)!=null&&player.getMetierByID(11).get_lvl()<30||player.getMetierByID(13)!=null&&player.getMetierByID(13).get_lvl()<30||player.getMetierByID(14)!=null&&player.getMetierByID(14).get_lvl()<30||player.getMetierByID(15)!=null&&player.getMetierByID(15).get_lvl()<30||player.getMetierByID(16)!=null&&player.getMetierByID(16).get_lvl()<30||player.getMetierByID(17)!=null&&player.getMetierByID(17).get_lvl()<30||player.getMetierByID(18)!=null&&player.getMetierByID(18).get_lvl()<30||player.getMetierByID(19)!=null&&player.getMetierByID(19).get_lvl()<30||player.getMetierByID(20)!=null&&player.getMetierByID(20).get_lvl()<30||player.getMetierByID(24)!=null&&player.getMetierByID(24).get_lvl()<30||player.getMetierByID(25)!=null&&player.getMetierByID(25).get_lvl()<30||player.getMetierByID(26)!=null&&player.getMetierByID(26).get_lvl()<30||player.getMetierByID(27)!=null&&player.getMetierByID(27).get_lvl()<30||player.getMetierByID(28)!=null&&player.getMetierByID(28).get_lvl()<30||player.getMetierByID(31)!=null&&player.getMetierByID(31).get_lvl()<30||player.getMetierByID(36)!=null&&player.getMetierByID(36).get_lvl()<30||player.getMetierByID(41)!=null&&player.getMetierByID(41).get_lvl()<30||player.getMetierByID(56)!=null&&player.getMetierByID(56).get_lvl()<30||player.getMetierByID(58)!=null&&player.getMetierByID(58).get_lvl()<30||player.getMetierByID(60)!=null&&player.getMetierByID(60).get_lvl()<30)
        {
          SocketManager.send(client,"DQ336|4840");
          return false;
        }

        if(player.totalJobBasic()>2)
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"19");
          SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
          player.setExchangeAction(null);
          player.setIsOnDialogAction(-1);
        }
        else
        {
          if(player.hasItemTemplate(459,20)&&player.hasItemTemplate(7657,15))
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+20+"~"+459);
            player.removeByTemplateID(459,20);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+15+"~"+7657);
            player.removeByTemplateID(7657,15);
            player.learnJob(job);
            SocketManager.send(client,"DQ3153|4840");
            return false;
          }
          else
          {
            SocketManager.send(client,"DQ3151|4840");
            return false;
          }
        }
        return true;

      case 968://Fin de donjon toror & tot
        if(player.getCurMap().getId()==(short)9877||player.getCurMap().getId()==(short)9881)
        {
          player.teleport((short)9538,186);
        }
        break;

      case 969://Transformation donjon abra & CM
        if(player.getCurMap().getId()==(short)8715) // Abra
        {
          player.teleport((short)8716,366);
          player.setFullMorph(11,false,false);
        }
        else if(player.getCurMap().getId()==(short)9120) // CM
        {
          player.teleport((short)9121,69);
          player.setFullMorph(11,false,false);
        }
        break;

      case 970://Dï¿½morph + TP : Donjon abra & CM
        if(player.getCurMap().getId()==(short)8719) // Abra
        {
          player.unsetFullMorph();
          player.teleport((short)10154,335);
        }
        else if(player.getCurMap().getId()==(short)9123) // CM
        {
          player.unsetFullMorph();
          player.teleport((short)9125,71);
        }
        break;

      //v2.7 - Replaced String += with StringBuilder
      case 971://Entrï¿½e du donjon dragoeufs
        if(player.getCurMap().getId()==(short)9788)
        {
          boolean key0=player.hasItemTemplate(8342,1),key1=false;
          if(key0||player.hasItemTemplate(10207,1))
          {

            if(player.hasItemTemplate(10207,1))
            {
              String stats=player.getItemTemplate(10207).getTxtStat().get(Constant.STATS_NAME_DJ);
              for(String key : stats.split(","))
              {
            	 
                id=Integer.parseInt(key,16);
                if(id==8342)
                  key1=true;
              }

              if(key1)
              {
            	 
            	  if(player.getAccount().getTime_dj() == 0L) {
                String replace1=Integer.toHexString(8342);
                StringBuilder newStats=new StringBuilder();
                for(String i : stats.split(","))
                  if(!i.equals(replace1))
                    newStats.append((newStats.toString().isEmpty() ? i : ","+i));
                player.getItemTemplate(10207).getTxtStat().remove(Constant.STATS_NAME_DJ);
                player.getItemTemplate(10207).getTxtStat().put(Constant.STATS_NAME_DJ,newStats.toString());
                SocketManager.GAME_SEND_UPDATE_ITEM(player,player.getItemTemplate(10207));
            	  }
              }
            }
            if(key0&&(!key1))
            {
              player.removeByTemplateID(8342,1);
            }
            if(key0||(key1))
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+8342);
              //SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+8343);
              player.teleport((short)10102,350);
              return true;
            }
          }
        }
        SocketManager.GAME_SEND_Im_PACKET(player,"119");
        break;

      //v2.7 - Replaced String += with StringBuilder  
      case 972://Sortir du donjon skeunk avec le trousseau
        if(player.getCurMap().getId()!=(short)8978)
          return true;
        if(!player.hasItemTemplate(7935,1))
          return true;
        if(!player.hasItemTemplate(7936,1))
          return true;
        if(!player.hasItemTemplate(7937,1))
          return true;
        if(!player.hasItemTemplate(7938,1))
          return true;

        boolean key0=false;
        if(player.hasItemTemplate(10207,1))
        {
          String stats=player.getItemTemplate(10207).getTxtStat().get(Constant.STATS_NAME_DJ);
          for(String key : stats.split(","))
          {
            if(Integer.parseInt(key,16)==8073)
              key0=true;
          }

          if(key0)
          {
        	  if(player.getAccount().getTime_dj() == 0L) {
            String replace=Integer.toHexString(8073);
            StringBuilder newStats=new StringBuilder();
            for(String i : stats.split(","))
              if(!i.equals(replace))
                newStats.append((newStats.toString().isEmpty() ? i : ","+i));
            player.getItemTemplate(10207).getTxtStat().remove(Constant.STATS_NAME_DJ);
            player.getItemTemplate(10207).getTxtStat().put(Constant.STATS_NAME_DJ,newStats.toString());
            SocketManager.GAME_SEND_UPDATE_ITEM(player,player.getItemTemplate(10207));
        	  }
          }
          else
            return true;
        }
        else
          return true;

        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+7935);
        player.removeByTemplateID(7935,1);
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+7936);
        player.removeByTemplateID(7936,1);
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+7937);
        player.removeByTemplateID(7937,1);
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+7938);
        player.removeByTemplateID(7938,1);
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+8073);

        GameObject object=Main.world.getObjTemplate(8072).createNewItem(1,false);

        if(player.addObjet(object,false))
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+object.getTemplate().getId());
          World.addGameObject(object,true);
        }

        player.teleport((short)9503,357);
        break;

      case 973://Sortir du donjon skeunk avec la clef
        if(player.getCurMap().getId()!=(short)8978)
          return true;
        if(!player.hasItemTemplate(7935,1))
          return true;
        if(!player.hasItemTemplate(7936,1))
          return true;
        if(!player.hasItemTemplate(7937,1))
          return true;
        if(!player.hasItemTemplate(7938,1))
          return true;
        if(!player.hasItemTemplate(8073,1))
          return true;

        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+7935);
        player.removeByTemplateID(7935,1);
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+7936);
        player.removeByTemplateID(7936,1);
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+7937);
        player.removeByTemplateID(7937,1);
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+7938);
        player.removeByTemplateID(7938,1);
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+8073);
        player.removeByTemplateID(8073,1);

        ObjectTemplate dofus=Main.world.getObjTemplate(8072);
        GameObject obj=dofus.createNewItem(1,false);
        if(player.addObjet(obj,false))
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+obj.getTemplate().getId());
          World.addGameObject(obj,true);
        }

        player.teleport((short)9503,357);
        break;

      case 974://Sort boomerang perfide
        if(player.getCurMap().getId()!=(short)8978)
          return true;
        if(!player.hasItemTemplate(8075,10))
          return true;
        if(!player.hasItemTemplate(8076,10))
          return true;
        if(!player.hasItemTemplate(8077,10))
          return true;
        if(!player.hasItemTemplate(8064,10))
          return true;
        if(player.hasSpell(364))
          return true;

        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+10+"~"+8075);
        player.removeByTemplateID(8075,10);
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+10+"~"+8076);
        player.removeByTemplateID(8076,10);
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+10+"~"+8077);
        player.removeByTemplateID(8077,10);
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+10+"~"+8064);
        player.removeByTemplateID(8064,10);

        player.learnSpell(364,1,true,true,true);
        break;

      case 975://Entrï¿½e salle skeunk
        if(player.getCurMap().getId()!=(short)8973)
          return true;
        if(!player.hasItemTemplate(7935,1)||!player.hasItemTemplate(7936,1)||!player.hasItemTemplate(7937,1)||!player.hasItemTemplate(7938,1))
          return true;

        player.teleport((short)8977,448);
        break;
      case 976://Tï¿½lï¿½portation en Minotoror
        try
        {
          if(player.getCurMap().getId()!=(short)9557)
            return true;
          if(!player.hasItemTemplate(8305,1))
            return true;
          if(!player.hasItemTemplate(8306,1))
            return true;
          if(!player.hasItemTemplate(7924,1))
            return true;

          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+8305);
          player.removeByTemplateID(8305,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+8306);
          player.removeByTemplateID(8306,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+7924);
          player.removeByTemplateID(7924,1);

          player.teleport((short)9880,399);
        }
        catch(Exception e)
        {
          return true;
        }
        break;

      case 977://Tï¿½lï¿½portation en salle des dalles Toror
        try
        {
          switch(player.getCurMap().getId())
          {
            case 9553:
            case 9554:
            case 9555:
            case 9556:
            case 9557:
            case 9558:
            case 9559:
            case 9560:
            case 9561:
            case 9562:
            case 9563:
            case 9564:
            case 9565:
            case 9566:
            case 9567:
            case 9568:
            case 9569:
            case 9570:
            case 9571:
            case 9572:
            case 9573:
            case 9574:
            case 9575:
            case 9576:
            case 9577:
              player.teleport((short)9876,287);
              break;
          }
        }
        catch(Exception e)
        {
          return true;
        }
        break;

      case 978://Tï¿½lï¿½portation en salle des dalles DC
        try
        {
          switch(player.getCurMap().getId())
          {
            case 9372:
            case 9384:
            case 9380:
            case 9381:
            case 9382:
            case 9383:
            case 9393:
            case 9374:
            case 9394:
            case 9390:
            case 9391:
            case 9392:
            case 9373:
            case 9389:
            case 9385:
            case 9386:
            case 9387:
            case 9388:
            case 9371:
            case 9375:
            case 9376:
            case 9377:
            case 9378:
            case 9379:
              player.teleport((short)9396,387);
              break;
          }
        }
        catch(Exception e)
        {
          return true;
        }
        break;

      case 980: // tï¿½lï¿½portation avec mapsecu, et deux itemssecu supprimï¿½s : donjon dc
        try
        {
          mapId=Integer.parseInt(args.split(",")[0]);
          cellId=Integer.parseInt(args.split(",")[1]);
          int item=Integer.parseInt(args.split(",")[2]);
          int item2=Integer.parseInt(args.split(",")[3]);
          mapSecu=Short.parseShort(args.split(",")[4]);

          if(player.getCurMap().getId()!=mapSecu)
            return true;
          if(!player.hasItemTemplate(item,1)&&!player.hasItemTemplate(item2,1))
            return true;

          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+item);
          player.removeByTemplateID(item,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+item2);
          player.removeByTemplateID(item2,1);
          player.teleport((short)mapId,cellId);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 981: // tï¿½lï¿½portation avec mapsecu et itemsecu : donjon dc
        try
        {
          mapId=Integer.parseInt(args.split(",")[0]);
          cellId=Integer.parseInt(args.split(",")[1]);
          int item=Integer.parseInt(args.split(",")[2]);
          mapSecu=Short.parseShort(args.split(",")[3]);

          if(player.getCurMap().getId()!=mapSecu)
            return true;

          if(!player.hasItemTemplate(item,1))
            return true;

          player.teleport((short)mapId,cellId);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 982: // Mort
        try
        {
          player.setFuneral();
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 983:
        try
        {
          Quest q=Quest.getQuestById(193);
          if(q==null)
            return true;
          GameMap curMap=player.getCurMap();
          if(curMap.getId()!=(short)10332)
            return true;
          if(player.getQuestPersoByQuest(q)==null)
            q.applyQuest(player);
          else if(q.getCurrentQuestStep(player.getQuestPersoByQuest(q)).getId()!=793)
            return true;

          Monster petitChef=Main.world.getMonstre(984);
          if(petitChef==null)
            return true;
          MobGrade mg=petitChef.getGradeByLevel(10);
          if(mg==null)
            return true;
          MobGroup _mg=new MobGroup(player.getCurMap().nextObjectId,player.getCurCell().getId(),petitChef.getId()+","+mg.getLevel()+","+mg.getLevel()+";");
          player.getCurMap().startFightVersusMonstres(player,_mg);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 984:
        try
        {
          int xp=Integer.parseInt(args.split(",")[0]);
          int mapCurId=Integer.parseInt(args.split(",")[1]);
          int idQuest=Integer.parseInt(args.split(",")[2]);

          if(player.getCurMap().getId()!=(short)mapCurId)
            return true;

          QuestPlayer qp=player.getQuestPersoByQuestId(idQuest);
          if(qp==null)
            return true;
          if(qp.isFinish())
            return true;

          player.addXp((long)xp);
          SocketManager.GAME_SEND_Im_PACKET(player,"08;"+xp);
          qp.setFinish(true);
          SocketManager.GAME_SEND_Im_PACKET(player,"055;"+idQuest);
          SocketManager.GAME_SEND_Im_PACKET(player,"056;"+idQuest);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 985:
        if(client==null)
          return true;
        try
        {
          int item=Integer.parseInt(args.split(",")[0]);
          int item2=Integer.parseInt(args.split(",")[1]);
          int mapCurId=Integer.parseInt(args.split(",")[2]);
          int metierId=Integer.parseInt(args.split(",")[3]);

          if(player.getCurMap().getId()!=(short)mapCurId)
            return true;
          Job metierArgs=Main.world.getMetier(metierId);
          if(metierArgs==null)
            return true;

          if(player.getMetierByID(metierId)!=null)
          {
            SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
            player.setExchangeAction(null);
            player.setIsOnDialogAction(-1);
            SocketManager.GAME_SEND_Im_PACKET(player,"111");
            return true; // Si on a dï¿½jï¿½ le mï¿½tier
          }

          ObjectTemplate t=Main.world.getObjTemplate(item2);
          if(t==null)
            return true;

          if(player.hasItemTemplate(item,1))
          {

            for(Entry<Integer, JobStat> entry : player.getMetiers().entrySet())
            {
              if(entry.getValue().get_lvl()<30&&!entry.getValue().getTemplate().isMaging())
              {
                SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
                player.setExchangeAction(null);
                player.setIsOnDialogAction(-1);
                SocketManager.GAME_SEND_Im_PACKET(player,"18;30");
                return true;
              }
            }

            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+item);
            player.removeByTemplateID(item,1);
            obj=t.createNewItem(1,false);
            obj.refreshStatsObjet("325#0#0#"+System.currentTimeMillis());
            if(player.addObjet(obj,false))
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+obj.getTemplate().getId());
              World.addGameObject(obj,true);
            }

            player.learnJob(Main.world.getMetier(metierId));
            //Database.getStatics().getPlayerData().update(player);
            SocketManager.GAME_SEND_Ow_PACKET(player);
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 986:
        if(client==null)
          return true;
        try
        {
          int mapCurId=Integer.parseInt(args.split(",")[0]);
          int item=Integer.parseInt(args.split(",")[1]);
          int item2=Integer.parseInt(args.split(",")[2]);
          int metierId=Integer.parseInt(args.split(",")[3]);

          if(player.getCurMap().getId()!=(short)mapCurId)
            return true;
          Job metierArgs=Main.world.getMetier(metierId);
          if(metierArgs==null)
            return true;

          if(player.getMetierByID(metierId)!=null)
          {
            SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
            player.setExchangeAction(null);
            player.setIsOnDialogAction(-1);
            SocketManager.GAME_SEND_Im_PACKET(player,"111");
            return true; // Si on a dï¿½jï¿½ le mï¿½tier
          }

          if(player.hasItemTemplate(item,1))
          {
            player.removeByTemplateID(item,1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+item);
            ObjectTemplate t=Main.world.getObjTemplate(item2);
            if(t!=null)
            {
              obj=t.createNewItem(1,false);
              obj.refreshStatsObjet("325#0#0#"+System.currentTimeMillis());
              if(player.addObjet(obj,false))
              {
                SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+obj.getTemplate().getId());
                World.addGameObject(obj,true);
                //Database.getStatics().getPlayerData().update(player);
                SocketManager.GAME_SEND_Ow_PACKET(player);
                return false;
              }
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 987:
        if(client==null)
          return true;
        try
        {
          int item=Integer.parseInt(args.split(",")[0]);
          int item2=Integer.parseInt(args.split(",")[1]);
          int item3=Integer.parseInt(args.split(",")[2]);
          int mapCurId=Integer.parseInt(args.split(",")[3]);
          int metierId=Integer.parseInt(args.split(",")[4]);

          if(player.getCurMap().getId()!=(short)mapCurId)
            return true;
          Job metierArgs=Main.world.getMetier(metierId);
          if(metierArgs==null)
            return true;

          if(player.getMetierByID(metierId)!=null)
          {
            SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
            player.setExchangeAction(null);
            player.setIsOnDialogAction(-1);
            SocketManager.GAME_SEND_Im_PACKET(player,"111");
            return true; // Si on a dï¿½jï¿½ le mï¿½tier
          }

          if(player.hasItemTemplate(item,1)&&player.hasItemTemplate(item2,1))
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+item);
            player.removeByTemplateID(item,1);
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+item2);
            player.removeByTemplateID(item2,1);

            ObjectTemplate t=Main.world.getObjTemplate(item3);
            if(t!=null)
            {
              obj=t.createNewItem(1,false);
              if(player.addObjet(obj,false))
              {
                SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+obj.getTemplate().getId());
                World.addGameObject(obj,true);
                //Database.getStatics().getPlayerData().update(player);
                SocketManager.GAME_SEND_Ow_PACKET(player);
                return false;
              }
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 988: // devenir pï¿½cheur
        if(client==null)
          return true;
        try
        {
          if(player.hasItemTemplate(2107,1))
          {
            long timeStamp=Long.parseLong(player.getItemTemplate(2107,1).getTxtStat().get(Constant.STATS_DATE));
            boolean success=(System.currentTimeMillis()-timeStamp<=2*60*1000);
            NpcQuestion qQuest=Main.world.getNPCQuestion(success ? 1171 : 1172);

            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+2107);
            player.removeByTemplateID(2107,1);

            if(qQuest==null)
            {
              SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
              player.setExchangeAction(null);
              return true;
            }

            if(success)
            {
              Job metierArgs=Main.world.getMetier(36);
              if(metierArgs==null)
                return true; // Si le mï¿½tier n'existe pas
              if(player.getMetierByID(36)!=null)
              {
                SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
                player.setExchangeAction(null);
                player.setIsOnDialogAction(-1);
                SocketManager.GAME_SEND_Im_PACKET(player,"111");
                return true; // Si on a dï¿½jï¿½ le mï¿½tier
              }

              for(Entry<Integer, JobStat> entry : player.getMetiers().entrySet())
              {
                if(entry.getValue().get_lvl()<30&&!entry.getValue().getTemplate().isMaging())
                {
                  SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
                  player.setExchangeAction(null);
                  player.setIsOnDialogAction(-1);
                  SocketManager.GAME_SEND_Im_PACKET(player,"18;30");
                  return true;
                }
              }

              player.learnJob(Main.world.getMetier(36));
              //Database.getStatics().getPlayerData().update(player);
              SocketManager.GAME_SEND_Ow_PACKET(player);
            }

            SocketManager.GAME_SEND_QUESTION_PACKET(client,qQuest.parse(player));
            return false;
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 989:
        if(client==null)
          return true;
        try
        {
          int mapCurId=Integer.parseInt(args.split(",")[0]);
          int item=Integer.parseInt(args.split(",")[1]);
          int item2=Integer.parseInt(args.split(",")[2]);
          int metierId=Integer.parseInt(args.split(",")[3]);

          if(player.getCurMap().getId()!=(short)mapCurId)
            return true;
          Job metierArgs=Main.world.getMetier(metierId);
          if(metierArgs==null)
            return true;

          if(player.getMetierByID(metierId)!=null)
          {
            SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
            player.setExchangeAction(null);
            player.setIsOnDialogAction(-1);
            SocketManager.GAME_SEND_Im_PACKET(player,"111");
            return true; // Si on a dï¿½jï¿½ le mï¿½tier
          }

          if(player.hasItemTemplate(item,1))
          {
            ObjectTemplate t=Main.world.getObjTemplate(item2);
            if(t!=null)
            {
              obj=t.createNewItem(1,false);
              if(player.addObjet(obj,false))
              {
                SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+obj.getTemplate().getId());
                World.addGameObject(obj,true);
                //Database.getStatics().getPlayerData().update(player);
                SocketManager.GAME_SEND_Ow_PACKET(player);
                return false;
              }
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 990:
        if(client==null)
          return true;
        try
        {
          if(player.getCurMap().getId()==(short)7388)
          {
            if(player.hasItemTemplate(2039,1)&&player.hasItemTemplate(2041,1))
            {
              long timeStamp=Long.parseLong(player.getItemTemplate(2039,1).getTxtStat().get(Constant.STATS_DATE));
              boolean success=(System.currentTimeMillis()-timeStamp<=2*60*1000);
              NpcQuestion qQuest=Main.world.getNPCQuestion(success ? 2364 : 1175);

              SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+2039);
              player.removeByTemplateID(2039,1);
              SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+2041);
              player.removeByTemplateID(2041,1);

              if(qQuest==null)
              {
                SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
                player.setExchangeAction(null);
                return true;
              }

              if(success)
              {
                Job metierArgs=Main.world.getMetier(41);
                if(metierArgs==null)
                  return true; // Si le mï¿½tier n'existe pas
                if(player.getMetierByID(41)!=null)
                {
                  SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
                  player.setExchangeAction(null);
                  player.setIsOnDialogAction(-1);
                  SocketManager.GAME_SEND_Im_PACKET(player,"111");
                  return true; // Si on a dï¿½jï¿½ le mï¿½tier
                }

                for(Entry<Integer, JobStat> entry : player.getMetiers().entrySet())
                {
                  if(entry.getValue().get_lvl()<30&&!entry.getValue().getTemplate().isMaging())
                  {
                    SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
                    player.setExchangeAction(null);
                    player.setIsOnDialogAction(-1);
                    SocketManager.GAME_SEND_Im_PACKET(player,"18;30");
                    return true;
                  }
                }

                player.learnJob(Main.world.getMetier(41));
                //Database.getStatics().getPlayerData().update(player);
                SocketManager.GAME_SEND_Ow_PACKET(player);
              }

              SocketManager.GAME_SEND_QUESTION_PACKET(client,qQuest.parse(player));
              return false;
            }
            else
            {
              player.send("Im14");
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 991: // Possï¿½der un item, lancer un combat contre un monstre
        try
        {
          int mapCurId=Integer.parseInt(args.split(",")[0]);
          int item=Integer.parseInt(args.split(",")[1]);
          int monstre=Integer.parseInt(args.split(",")[2]);
          int grade=Integer.parseInt(args.split(",")[3]);
          if(player.getCurMap().getId()==(short)mapCurId)
          {
            if(player.hasItemTemplate(item,1))
            {
              String groupe=monstre+","+grade+","+grade+";";
              MobGroup Mgroupe=new MobGroup(player.getCurMap().nextObjectId,player.getCurCell().getId(),groupe);
              player.getCurMap().startFightVersusMonstres(player,Mgroupe);
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 992: // Supprime deux items & apprends un mï¿½tier
        if(client==null)
          return true;
        try
        {
          int item1=Integer.parseInt(args.split(",")[0]);
          int item2=Integer.parseInt(args.split(",")[1]);
          int mapCurId=Integer.parseInt(args.split(",")[2]);
          int mId=Integer.parseInt(args.split(",")[3]);
          if(player.getCurMap().getId()==(short)mapCurId)
          {
            if(player.hasItemTemplate(item1,1))
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+item1);
              player.removeByTemplateID(item1,1);
            }
            if(player.hasItemTemplate(item2,1))
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+item2);
              player.removeByTemplateID(item2,1);
            }

            Job metierArgs=Main.world.getMetier(mId);
            if(metierArgs==null)
              return true;
            if(player.getMetierByID(mId)!=null)
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"111");
              return true;
            }

            for(Entry<Integer, JobStat> entry : player.getMetiers().entrySet())
            {
              if(entry.getValue().get_lvl()<30&&!entry.getValue().getTemplate().isMaging())
              {
                SocketManager.GAME_SEND_END_DIALOG_PACKET(client);
                player.setExchangeAction(null);
                player.setIsOnDialogAction(-1);
                SocketManager.GAME_SEND_Im_PACKET(player,"18;30");
                return true;
              }
            }

            player.learnJob(Main.world.getMetier(mId));
            //Database.getStatics().getPlayerData().update(player);
            SocketManager.GAME_SEND_Ow_PACKET(player);
            return true;
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 993: // Supprime deux items
        try
        {
          int item1=Integer.parseInt(args.split(",")[0]);
          int item2=Integer.parseInt(args.split(",")[1]);
          if(player.hasItemTemplate(item1,1))
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+item1);
            player.removeByTemplateID(item1,1);
          }
          if(player.hasItemTemplate(item2,1))
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+item2);
            player.removeByTemplateID(item2,1);
          }
          //Database.getStatics().getPlayerData().update(player);
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 994: // donnï¿½ un item si on ne l'a pas dï¿½jï¿½
        try
        {
          int mapID=Integer.parseInt(args.split(",")[0]);
          int item=Integer.parseInt(args.split(",")[1]);
          int metierId=Integer.parseInt(args.split(",")[2]);
          Job metierArgs=Main.world.getMetier(metierId);

          if(metierArgs==null)
            return true;
          if(player.getMetierByID(metierId)!=null)
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"111");
            return true;
          }

          GameMap curMapP=player.getCurMap();
          if(curMapP.getId()==(short)mapID)
          {
            if(!player.hasItemTemplate(item,1))
            {
              if(player.getMetierByID(41)!=null)
              {
                SocketManager.GAME_SEND_Im_PACKET(player,"182");
                return true;
              }
              ObjectTemplate t=Main.world.getObjTemplate(item);
              if(t!=null)
              {
                obj=t.createNewItem(1,false);
                obj.refreshStatsObjet("325#0#0#"+System.currentTimeMillis());
                if(player.addObjet(obj,false))
                {
                  SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+obj.getTemplate().getId());
                  World.addGameObject(obj,true);
                  //Database.getStatics().getPlayerData().update(player);
                  SocketManager.GAME_SEND_Ow_PACKET(player);
                  return true;
                }
              }
            }
          }
          return false;
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;
      case 995: // tï¿½lï¿½portation passage vers brakmar
        GameMap curMap2=player.getCurMap();
        if(!player.isInPrison())
        {
          if(curMap2.getId()==(short)11866)
          {
            SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"","2",player.getId()+"","6");
            player.teleport((short)11862,253);
          }
          else if(curMap2.getId()==(short)11862)
          {
            SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"","2",player.getId()+"","6");
            player.teleport((short)11866,344);
          }
          else
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"182");
            return true;
          }
        }
        break;

      case 996: // tï¿½lï¿½portation mine chariot + Animation
        GameMap curMap=player.getCurMap();
        ArrayList<Integer> mapSecure=new ArrayList<Integer>();
        for(String i : args.split("\\,"))
          mapSecure.add(Integer.parseInt(i));

        if(!mapSecure.contains((int)curMap.getId()))
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"182");
          return true;
        }

        long pKamas4=player.getKamas();
        if(pKamas4<50)
        {
          player.teleport((short)11862,253);
          return true;
        }

        if(!player.isInPrison())
        {
          SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"","2",player.getId()+"","6");
          long pNewKamas4=pKamas4-50;
          if(pNewKamas4<0)
            pNewKamas4=0;
          Main.world.kamas_total -= 50;
          player.setKamas(pNewKamas4);
          if(player.isOnline())
            SocketManager.GAME_SEND_STATS_PACKET(player);
          SocketManager.GAME_SEND_Im_PACKET(player,"046;"+50);
          player.teleport((short)10256,211);
        }
        break;

      case 997: // Apprendre un mï¿½tier de forgemagie
        try
        {
          int metierID=Integer.parseInt(args.split(",")[0]);
          int mapIdargs=Integer.parseInt(args.split(",")[1]);
          Job metierArgs=Main.world.getMetier(metierID);

          if(metierArgs==null)
            return true; // Si le mï¿½tier n'existe pas
          if(player.getMetierByID(metierID)!=null)
          {
            SocketManager.GAME_SEND_Im_PACKET(player,"111");
            return true; // Si on a dï¿½jï¿½ le mï¿½tier
          }

          GameMap curMapPerso=player.getCurMap();
          if(curMapPerso.getId()!=(short)mapIdargs)
            return true; // Map secure

          if(metierArgs.isMaging()) // Si c'est du FM
          {
            JobStat metierBase=player.getMetierByID(Main.world.getMetierByMaging(metierID));
            if(metierBase==null)
              return true; // Si la base n'existe pas
            if(metierBase.get_lvl()<65)
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"111");
              return true; // Si la base n'est pas assez hl
            }
            else if(player.totalJobFM()>2)
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"19");
              return true; // On compte les mï¿½tiers dï¿½ja acquis si c'est supï¿½rieur a 2 on ignore
            }
            else
            {
              player.learnJob(Main.world.getMetier(metierID));
            }
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        break;

      case 998://Donjon abraknyde salle des cases
        if(player.getCurMap().getId()==10154&&player.getCurCell().getId()==142)
        {
          player.teleport((short)8721,395);
        }
        else
        {
          SocketManager.GAME_SEND_MESSAGE(player,"You are not in front of the NPC.");
        }
        break;

      /** Fin Donjon **/
      case 999:
        player.teleport(this.map,Integer.parseInt(this.args));
        break;

      case 1000:
        map=Short.parseShort(this.args.split(",")[0]);
        cell=Integer.parseInt(this.args.split(",")[1]);
        player.teleport(map,cell);
        player.set_savePos(map+","+cell);
        SocketManager.GAME_SEND_Im_PACKET(player,"06");
        break;

      case 1001:
        map=Short.parseShort(this.args.split(",")[0]);
        cell=Integer.parseInt(this.args.split(",")[1]);
        player.teleport(map,cell);
        break;

      case 460: //Feca-only item rewards
      {
        if(player.getClasse()==1)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained a Feca artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 461: //Osamodas-only item rewards
      {
        if(player.getClasse()==2)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained an Osamodas artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 462: //Enutrof-only item rewards
      {
        if(player.getClasse()==3)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained an Enutrof artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 463: //Sram-only item rewards
      {
        if(player.getClasse()==4)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained a Sram artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 464: //Xelor-only item rewards
      {
        if(player.getClasse()==5)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained a Xelor artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 465: //Ecaflip-only item rewards
      {
        if(player.getClasse()==6)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained an Ecaflip artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 466: //Eniripsa-only item rewards
      {
        if(player.getClasse()==7)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained an Eniripsa artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 467: //Iop-only item rewards
      {
        if(player.getClasse()==8)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained an Iop artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 468: //Cra-only item rewards
      {
        if(player.getClasse()==9)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained a Cra artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 469: //Sadida-only item rewards
      {
        if(player.getClasse()==10)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained a Sadida artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 470: //Sacrier-only item rewards
      {
        if(player.getClasse()==11)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained a Sacrier artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 471: //Pandawa-only item rewards
      {
        if(player.getClasse()==12)
        {
          final int tID=Integer.parseInt(this.args);
          final ObjectTemplate t=Main.world.getObjTemplate(tID);
          final GameObject obj3=t.createNewItem(1,false);
          if(player.addObjet(obj3,true))
          {
            World.addGameObject(obj3,true);
            SocketManager.GAME_SEND_MESSAGE(player,"You have gained a Pandawa artifact!");
          }
          SocketManager.GAME_SEND_Ow_PACKET(player);
          return true;
        }
      }
      case 472://Donner l'abilitï¿½ 'args' ï¿½ une dragodinde Dragodinde
        Mount mount=player.getMount();
        Main.world.addMount(new Mount(mount.getId(),mount.getColor(),mount.getSex(),mount.getAmour(),mount.getEndurance(),mount.getLevel(),mount.getExp(),mount.getName(),mount.getFatigue(),mount.getEnergy(),mount.getReproduction(),mount.getMaturity(),mount.getState(),mount.parseObjectsToString(),mount.getAncestors(),args,mount.getSize(),mount.getCellId(),mount.getMapId(),mount.getOwner(),mount.getOrientation(),mount.getFecundatedDate(),mount.getCouple(),mount.getSavage()));
        player.setMount(Main.world.getMountById(mount.getId()));
        SocketManager.GAME_SEND_Re_PACKET(player,"+",Main.world.getMountById(mount.getId()));
        Database.getDynamics().getMountData().update(mount);
        return true;

      case 473://Teleport + give item
        try
        {
          String[] args2=args.split(";");
          short newMapID=Short.parseShort(args2[0].split(",",2)[0]);
          int newCellID=Integer.parseInt(args2[0].split(",",2)[1]);
          int tID=Integer.parseInt(args2[1].split(",")[0]);
          int count2=Integer.parseInt(args2[1].split(",")[1]);
          boolean send=true;
          if(count2>0)
          {
            ObjectTemplate T=Main.world.getObjTemplate(tID);
            if(T==null)
              return true;
            GameObject O=T.createNewItem(count2,false);
            //Si retourne true, on l'ajoute au monde
            if(player.addObjet(O,true))
              World.addGameObject(O,true);
          }
          if(player.isOnline())//on envoie le packet qui indique l'ajout//retrait d'un item
          {
            SocketManager.GAME_SEND_Ow_PACKET(player);
            if(send)
            {
              if(count2>=0)
              {
                SocketManager.GAME_SEND_Im_PACKET(player,"021;"+count2+"~"+tID);
              }
            }
          }
          if(newMapID==10754) //v2.0 - excluded maps
          {
            player.sendMessage("The map you tried to reach is missing and has been disabled to prevent players from getting stuck.");
            break;
          }
          if(!player.isInPrison())
          {
            player.teleport(newMapID,newCellID);
          }
          else
          {
            if(player.getCurCell().getId()==268)
            {
              player.teleport(newMapID,newCellID);
            }
          }
        }
        catch(Exception e)
        {
          // Pas ok, mais il y a trop de dialogue de PNJ buggï¿½ pour laisser cette erreur flood.
          // e.printStackTrace();
          return true;
        }
        break;
      case 1100: //peki peki
          if(player.hasItemTemplate(7373,1) && player.hasItemTemplate(7374,1) && player.hasItemTemplate(7375,1) &&
        		  player.hasItemTemplate(7376,1) && player.hasItemTemplate(7377,1) && player.hasItemTemplate(7378,1))
          {
            player.removeByTemplateID(7373,1);
            player.removeByTemplateID(7374,1);
            player.removeByTemplateID(7375,1);
            player.removeByTemplateID(7376,1);
            player.removeByTemplateID(7377,1);
            player.removeByTemplateID(7378,1);
            player.teleport((short) 8357,53);
          }else
        	  SocketManager.GAME_SEND_MESSAGE(player,"vous devez me ramener les 6 artefacts", "008000");
          break;
          
          case 1200:
        	  if(player.Song < 0)
        		  player.Song = 0;
        	  if(player.getLevel()<179)
              {
                SocketManager.GAME_SEND_MESSAGE(player,"Vous devez avoir au moins le niveau 180 .");
                return true;
              }
        	player.teleport((short) ((31000+player.Song)),339);
        	 SocketManager.GAME_SEND_MESSAGE(player,"Casper Songe "+player.Song, "008000");
    	  break;
          case 1201:
        	  int template= 8574;
              int qua = 1;
              if(player.Song != 15)
              {
        		  SocketManager.GAME_SEND_MESSAGE(player,"vous devez terminer votre casper songe .");  
        		  return true;
              }
         GameObject objj=Main.world.getObjTemplate(template).createNewItem(qua,false);
         if(player.addObjet(objj,true))
           World.addGameObject(objj,true);
         SocketManager.GAME_SEND_Ow_PACKET(player);
         SocketManager.GAME_SEND_Im_PACKET(player,"021;"+qua+"~"+template);
        	player.teleport((short) (31016),339);
        	player.Song =player.Song+1;
    	  break;
          case 1202:
        	  int template2= 7112;
              int qua2 = 1;
              if(player.Song != 31)
              {
        		  SocketManager.GAME_SEND_MESSAGE(player,"vous devez terminer votre casper songe .");  
        		  return true;
              }
         GameObject objj2=Main.world.getObjTemplate(template2).createNewItem(qua2,false);
         if(player.addObjet(objj2,true))
           World.addGameObject(objj2,true);
         SocketManager.GAME_SEND_Ow_PACKET(player);
         SocketManager.GAME_SEND_Im_PACKET(player,"021;"+qua2+"~"+template2);
        	player.teleport((short) (6954),283);
        	player.Song =0;
    	  break;
          case 1203:
        	     if(player.songe_reset) {
        	    	 SocketManager.GAME_SEND_MESSAGE(player,"Limitation a une fois par jour."); 
        	    	 return true;
        	     }
        	    player.songe_reset = true;
        	    player.Song = 0;
        	    SocketManager.GAME_SEND_MESSAGE(player,"Songe Remis a zéro.");
        	  if(player.getLevel()<179)
              {
                SocketManager.GAME_SEND_MESSAGE(player,"Vous devez avoir au moins le niveau 180 .");
                return true;
              }
        	player.teleport((short) ((31000+player.Song)),339);
        	 SocketManager.GAME_SEND_MESSAGE(player,"Casper Songe "+player.Song, "008000");
    	  break;
    	  

      default:
        break;
    }

    return true;
  }
}
