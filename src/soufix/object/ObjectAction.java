package soufix.object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import soufix.area.Area;
import soufix.area.SubArea;
import soufix.area.map.GameMap;
import soufix.area.map.entity.Animation;
import soufix.area.map.entity.House;
import soufix.area.map.entity.MountPark;
import soufix.client.Player;
import soufix.common.ConditionParser;
import soufix.common.SocketManager;
import soufix.database.Database;
import soufix.entity.Prism;
import soufix.entity.monster.MobGroup;
import soufix.entity.mount.Mount;
import soufix.entity.pet.PetEntry;
import soufix.events.Noel;
import soufix.fight.spells.Spell.SortStats;
import soufix.game.World;
import soufix.game.action.ExchangeAction;
import soufix.job.JobStat;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.entity.Capture;
import soufix.object.entity.Fragment;
import soufix.other.Action;
import soufix.utility.Pair;

public class ObjectAction
{
  private String type;
  private String args;
  private String cond;
  private boolean send=true;

  public ObjectAction(String type, String args, String cond)
  {
    this.type=type;
    this.args=args;
    this.cond=cond;
  }

  public void apply(Player player0, Player target, int objet, int cellid)
  {
    if(player0==null||!player0.isOnline()||player0.getDoAction()||player0.getGameClient()==null)
      return;
    if(!this.cond.equalsIgnoreCase("")&&!this.cond.equalsIgnoreCase("-1")&&!ConditionParser.validConditions(player0,this.cond))
    {
      SocketManager.GAME_SEND_Im_PACKET(player0,"119");
      return;
    }
    if(player0.getLevel()<World.getGameObject(objet).getTemplate().getLevel())
    {
      SocketManager.GAME_SEND_Im_PACKET(player0,"119");
      return;
    }
    if(player0.getExchangeAction()!=null)
        return;
    Player player=target!=null ? target : player0;

    if(World.getGameObject(objet)==null)
    {
      SocketManager.GAME_SEND_MESSAGE(player,"Error: null object. Please contact a staff member to fix this issue.");
      return;
    }

    boolean sureIsOk=false,isOk=true;
    int turn=0;
    String arg="";
    try
    {
      for(String type : this.type.split(";"))
      {
        String[] split=args.split("\\|",2);
        if(!this.args.isEmpty()&&split.length>turn)
          arg=split[turn];

        switch(Integer.parseInt(type))
        {
          case -1:
            if(player0.getFight()!=null)
              return;
            isOk=true;
            send=false;
            break;

          case -100: // mystery box
              player.send("kn"+World.getGameObject(objet).getTemplate().getId()+"|"+this.getArgs());
              isOk = false;
              send = false;
              /* try
                  RandomCollection<Integer> rc = new RandomCollection<>();
                  for (String infos : args.split(";")) {
                      String[] ids = infos.split(",");
                      int iditem = Integer.parseInt(ids[0]);
                      double taux = Double.parseDouble(ids[1]);
                      rc.add(taux, iditem);
                  }
                  int finalid = rc.next();
                  if(finalid == 66666) {
                      for (String infos : args.split(";")) {
                          String[] ids = infos.split(",");
                          int iditem = Integer.parseInt(ids[0]);
                          if(iditem == 66666)continue;
                          player.AddItem(iditem,1);
                      }
                  } else
                  player.AddItem(finalid,1);

              } catch (Exception e){
                  player.sendMessage("ERROR: merci de contacter un membre de staff");
                  isOk = false;
              } */
            
          case 0://Téléportation.
            if(player0.getFight()!=null)
              return;
            short mapId=Short.parseShort(arg.split(",",2)[0]);
            int cellId=Integer.parseInt(arg.split(",",2)[1]);
            if(!player.isInPrison()&&!player.cantTP())
              player.teleport(mapId,cellId);
            else if(player.getCurCell().getId()==268)
              player.teleport(mapId,cellId);
            break;

          case 1://Téléportation au point de sauvegarde.
            if(player0.getFight()!=null)
              return;
            if(!player.isInPrison()&&!player.cantTP())
              player.warpToSavePos();
            break;

          case 2://Don de Kamas.
        	  /*
            if(player0.getFight()!=null)
              return;
            int count=Integer.parseInt(arg);
            long curKamas=player.getKamas();
            long newKamas=curKamas+count;
            if(newKamas<0)
              newKamas=0;
            player.setKamas(newKamas);
            if(player.isOnline())
              SocketManager.GAME_SEND_STATS_PACKET(player);
              */
            break;

          case 3://Don de vie.
            if(this.type.split(";").length>1&&player.getFight()!=null)
              return;
            boolean isOk1=true,isOk2=true;
            for(String arg0 : arg.split(","))
            {
              int val,statId1;
              if(arg.contains(";"))
              {
                statId1=Integer.parseInt(arg.split(";")[0]);
                val=World.getGameObject(objet).getRandomValue(World.getGameObject(objet).parseStatsString(),Integer.parseInt(arg.split(";")[0]));
              }
              else
              {
                statId1=Integer.parseInt(arg0);
                val=World.getGameObject(objet).getRandomValue(World.getGameObject(objet).parseStatsString(),Integer.parseInt(arg0));
              }
              switch(statId1)
              {
                case 110://Vie.
                  if(player.getCurPdv()==player.getMaxPdv())
                  {
                    isOk1=false;
                    continue;
                  }
                  if(player.getCurPdv()+val>player.getMaxPdv())
                    val=player.getMaxPdv()-player.getCurPdv();
                  player.setPdv(player.getCurPdv()+val);
                  if(player.getFight()!=null)
                    player.getFight().getFighterByPerso(player).setPdv(player.getCurPdv());
                  SocketManager.GAME_SEND_STATS_PACKET(player);
                  SocketManager.GAME_SEND_Im_PACKET(player,"01;"+val);
                  sureIsOk=true;
                  break;
                case 139://Energie.
                  if(player.getEnergy()==10000)
                  {
                    isOk2=false;
                    continue;
                  }
                  if(player.getEnergy()+val>10000)
                    val=10000-player.getEnergy();
                  player.setEnergy(player.getEnergy()+val);
                  SocketManager.GAME_SEND_STATS_PACKET(player);
                  SocketManager.GAME_SEND_Im_PACKET(player,"07;"+val);
                  sureIsOk=true;
                  break;
                case 605://Expérience.
                  player.addXp(val);
                  SocketManager.GAME_SEND_STATS_PACKET(player);
                  SocketManager.GAME_SEND_Im_PACKET(player,"08;"+val);
                  break;
                case 614://Expérience métier.
                  JobStat job=player.getMetierByID(Integer.parseInt(arg0.split(";")[1]));
                  if(job==null)
                  {
                    isOk1=false;
                    isOk2=false;
                    continue;
                  }
                  job.addXp(player,val);
                  SocketManager.GAME_SEND_Im_PACKET(player,"017;"+val+"~"+Integer.parseInt(arg0.split(";")[1]));
                  final ArrayList<JobStat> SMs=new ArrayList<JobStat>();
                  SMs.add(job);
                  SocketManager.GAME_SEND_JX_PACKET(player,SMs);
                  sureIsOk=true;
                  break;
              }
            }
            if(arg.split(",").length==1)
              if(!isOk1||!isOk2)
                isOk=false;
              else if(!isOk1&&!isOk2)
                isOk=false;
            send=false;
            break;

          case 4://Don de Stats.
            if(player0.getFight()!=null)
              return;
            if(World.getGameObject(objet).getTemplate().getId() == 24464) {
            if(player0.getStats().getEffect(Constant.STATS_ADD_INTE) != 0 || player.getStatsParcho().getEffect(Constant.STATS_ADD_INTE) != 0) {
            	 SocketManager.GAME_SEND_MESSAGE(player,"Restat Avant."); 	
            return;	
            }
            if(player0.getStats().getEffect(Constant.STATS_ADD_FORC) != 0 || player.getStatsParcho().getEffect(Constant.STATS_ADD_FORC) != 0) {
            	 SocketManager.GAME_SEND_MESSAGE(player,"Restat Avant."); 	
                 return;		
            }
            if(player0.getStats().getEffect(Constant.STATS_ADD_VITA) != 0 || player.getStatsParcho().getEffect(Constant.STATS_ADD_VITA) != 0) {
            	 SocketManager.GAME_SEND_MESSAGE(player,"Restat Avant."); 	
                 return;		
            }
            if(player0.getStats().getEffect(Constant.STATS_ADD_AGIL) != 0 || player.getStatsParcho().getEffect(Constant.STATS_ADD_AGIL) != 0) {
            	 SocketManager.GAME_SEND_MESSAGE(player,"Restat Avant."); 	
                 return;		
            }
            if(player0.getStats().getEffect(Constant.STATS_ADD_SAGE) != 0 || player.getStatsParcho().getEffect(Constant.STATS_ADD_SAGE) != 0) {
            	 SocketManager.GAME_SEND_MESSAGE(player,"Restat Avant."); 	
                 return;		
            }
            if(player0.getStats().getEffect(Constant.STATS_ADD_CHAN) != 0 || player.getStatsParcho().getEffect(Constant.STATS_ADD_CHAN) != 0) {
            	 SocketManager.GAME_SEND_MESSAGE(player,"Restat Avant."); 	
                 return;		
            }
            	
            }
            for(String arg0 : arg.split(","))
            {
              int statId=Integer.parseInt(arg0.split(";")[0]);
              int val=Integer.parseInt(arg0.split(";")[1]);
              switch(statId)
              {
                case 1://Vitalité.
                  for(int i=0;i<val;i++)
                  {
                    player.boostStat(11,false);
                    player.getStatsParcho().addOneStat(Constant.STATS_ADD_VITA,1);
                  }
                  break;
                case 2://Sagesse.
                  for(int i=0;i<val;i++)
                  {
                    player.getStatsParcho().addOneStat(Constant.STATS_ADD_SAGE,1);
                    player.boostStat(12,false);
                  }
                  break;
                case 3://Force.
                  for(int i=0;i<val;i++)
                  {
                    player.boostStat(10,false);
                    player.getStatsParcho().addOneStat(Constant.STATS_ADD_FORC,1);
                  }
                  break;
                case 4://Intelligence.
                  for(int i=0;i<val;i++)
                  {
                    player.boostStat(15,false);
                    player.getStatsParcho().addOneStat(Constant.STATS_ADD_INTE,1);
                  }
                  break;
                case 5://Chance.
                  for(int i=0;i<val;i++)
                  {
                    player.boostStat(13,false);
                    player.getStatsParcho().addOneStat(Constant.STATS_ADD_CHAN,1);
                  }
                  break;
                case 6://Agilité.
                  for(int i=0;i<val;i++)
                  {
                    player.boostStat(14,false);
                    player.getStatsParcho().addOneStat(Constant.STATS_ADD_AGIL,1);
                  }
                  break;
                case 7://Point de Sort.
                  player.set_spellPts(player.get_spellPts()+val);
                  break;
              }
            }
            sureIsOk=true;
            //Database.getStatics().getPlayerData().update(player);
            SocketManager.GAME_SEND_STATS_PACKET(player);
            break;

          case 5://Fée d'artifice.
            if(player0.getFight()!=null)
              return;
            int id0=Integer.parseInt(arg);
            Animation anim=Main.world.getAnimation(id0);
            if(player.getFight()!=null)
              return;
            player.changeOrientation(1);
            SocketManager.GAME_SEND_GA_PACKET_TO_MAP(player.getCurMap(),"0",228,player.getId()+";"+cellid+","+Animation.PrepareToGA(anim),"");
            break;

          case 6://Apprendre un sort.
            if(player0.getFight()!=null)
              return;
            id0=Integer.parseInt(arg);
            if(Main.world.getSort(id0)==null)
              return;
            if(!player.learnSpell(id0,1,true,true,true))
              return;
            send=false;
            break;

          case 7://Désapprendre un sort.
            if(player0.getFight()!=null)
              return;
            id0=Integer.parseInt(arg);
            int oldLevel=player.getSortStatBySortIfHas(id0).getLevel();
            if(player.getSortStatBySortIfHas(id0)==null)
              return;
            if(oldLevel<=1)
              return;
            player.unlearnSpell(player,id0,1,oldLevel,true,true);
            break;

          case 8://Désapprendre un sort é un percepteur.
            if(player0.getFight()!=null)
              return;
            if(player.get_guild() == null)
      	  {
      		  player.sendMessage("Vous ne posséde pas de guilde"); 
      		return;  
      	  }
      	  if(player.get_guild().getMember(player.getId()) == null)
      		return;
      	  if(player.get_guild().getMember(player.getId()).getRank() != 1) {
      		  player.sendMessage("Vous devez etre le mineur de votre guilde");
        		return;
      	  }
                  player.get_guild().Reset();
      	  
            break;

          case 9://Oublié un métier.
            if(player0.getFight()!=null)
              return;
            int job=Integer.parseInt(arg);
            JobStat jobStats=player.getMetierByID(job);

            if(jobStats==null)
            {
              player.send("Im149"+job);
              return;
            }

            player.unlearnJob(jobStats.getId());
            SocketManager.GAME_SEND_STATS_PACKET(player);
            //Database.getStatics().getPlayerData().update(player);
            player.send("JR"+job);
            break;

          case 10://EPO.
            if(player0.getFight()!=null)
              return;
            GameObject obj=World.getGameObject(objet);
            if(obj==null)
              return;
            GameObject pets=player.getObjetByPos(Constant.ITEM_POS_FAMILIER);
            if(pets==null)
              return;
            PetEntry MyPets=Main.world.getPetsEntry(pets.getGuid());
            if(MyPets==null)
              return;
            if(obj.getTemplate().getConditions().contains(pets.getTemplate().getId()+""))
              MyPets.giveEpo(player);
            break;

          case 11://Changé de Sexe.
            if(player0.getFight()!=null)
              return;
            if(player.getSexe()==0)
              player.setSexe(1);
            else
              player.setSexe(0);

            SocketManager.GAME_SEND_ALTER_GM_PACKET(player.getCurMap(),player);
            Database.getStatics().getPlayerData().updateInfos(player);
            break;

          case 12://Changé de nom.
            if(player0.getFight()!=null)
              return;
            player.setChangeName(true);
            isOk=false;
            send=false;
            break;

          case 13://Apprendre une émote.
            if(player0.getFight()!=null)
              return;
            int emote=Integer.parseInt(arg);

            if(player.getEmotes().contains(emote))
            {
              SocketManager.GAME_SEND_MESSAGE(player,"Vous connaissez déjé cet emote.");
              return;
            }

            player.addStaticEmote(emote);
            break;

          case 14://Apprendre un métier.
            if(player0.getFight()!=null)
              return;
            job=Integer.parseInt(arg);
            if(Main.world.getMetier(job)==null)
              return;
            if(player.getMetierByID(job)!=null)//Métier déjé appris
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"111");
              return;
            }
            if(player.getMetierByID(2)!=null&&player.getMetierByID(2).get_lvl()<30||player.getMetierByID(11)!=null&&player.getMetierByID(11).get_lvl()<30||player.getMetierByID(13)!=null&&player.getMetierByID(13).get_lvl()<30||player.getMetierByID(14)!=null&&player.getMetierByID(14).get_lvl()<30||player.getMetierByID(15)!=null&&player.getMetierByID(15).get_lvl()<30||player.getMetierByID(16)!=null&&player.getMetierByID(16).get_lvl()<30||player.getMetierByID(17)!=null&&player.getMetierByID(17).get_lvl()<30||player.getMetierByID(18)!=null&&player.getMetierByID(18).get_lvl()<30||player.getMetierByID(19)!=null&&player.getMetierByID(19).get_lvl()<30||player.getMetierByID(20)!=null&&player.getMetierByID(20).get_lvl()<30||player.getMetierByID(24)!=null&&player.getMetierByID(24).get_lvl()<30||player.getMetierByID(25)!=null&&player.getMetierByID(25).get_lvl()<30||player.getMetierByID(26)!=null&&player.getMetierByID(26).get_lvl()<30||player.getMetierByID(27)!=null&&player.getMetierByID(27).get_lvl()<30||player.getMetierByID(28)!=null&&player.getMetierByID(28).get_lvl()<30||player.getMetierByID(31)!=null&&player.getMetierByID(31).get_lvl()<30||player.getMetierByID(36)!=null&&player.getMetierByID(36).get_lvl()<30||player.getMetierByID(41)!=null&&player.getMetierByID(41).get_lvl()<30||player.getMetierByID(56)!=null&&player.getMetierByID(56).get_lvl()<30||player.getMetierByID(58)!=null&&player.getMetierByID(58).get_lvl()<30||player.getMetierByID(60)!=null&&player.getMetierByID(60).get_lvl()<30||player.getMetierByID(65)!=null&&player.getMetierByID(65).get_lvl()<30)
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"18;30");
              return;
            }
            if(player.totalJobBasic()>2)
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"19");
              return;
            }
            else
            {
              if(job==27)
              {
                if(!player.hasItemTemplate(966,1))
                  return;
                SocketManager.GAME_SEND_Im_PACKET(player,"022;"+966+"~"+1);
                player.learnJob(Main.world.getMetier(job));
              }
              else
              {
                player.learnJob(Main.world.getMetier(job));
              }
            }
            break;

          case 15://TP au foyer.
            if(player0.getFight()!=null)
              return;
            boolean tp=false;
            for(House i : Main.world.getHouses().values())
            {
              if(i.getOwnerId()==player.getAccount().getId())
              {
                player.teleport((short)i.getHouseMapId(),i.getHouseCellId());
                tp=true;
                break;
              }
            }
            if(!tp)
            {
              player.send("Im161");
              return;
            }
            break;

          case 16://Pnj Follower.
            if(player0.getFight()!=null)
              return;
            // Petite larve dorée = 7425
            player.setMascotte(Integer.parseInt(this.args));
            break;

          case 17://Bénédiction.
            if(player0.getFight()!=null)
              return;
            player.setBenediction(World.getGameObject(objet).getTemplate().getId());
            break;

          case 18://Malédiction.
            if(player0.getFight()!=null)
              return;
            player.setMalediction(World.getGameObject(objet).getTemplate().getId());
            break;

          case 19://RolePlay Buff.
            if(player0.getFight()!=null)
              return;
            player.setRoleplayBuff(World.getGameObject(objet).getTemplate().getId());
            break;

          case 20://Bonbon.
            if(player0.getFight()!=null)
              return;
            player.setCandy(World.getGameObject(objet).getTemplate().getId());
            break;

          case 21://Poser un objet d'élevage.
            if(player0.getFight()!=null)
              return;
            GameMap map0=player.getCurMap();
            id0=World.getGameObject(objet).getTemplate().getId();

            int resist=World.getGameObject(objet).getResistance(World.getGameObject(objet).parseStatsString());
            int resistMax=World.getGameObject(objet).getResistanceMax(World.getGameObject(objet).getTemplate().getStrTemplate());
            if(map0.getMountPark()==null)
              return;
            MountPark MP=map0.getMountPark();
            if(player.get_guild()==null)
            {
              SocketManager.GAME_SEND_BN(player);
              return;
            }
            if(!player.getGuildMember().canDo(Constant.G_AMENCLOS))
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"193");
              return;
            }
            if(MP.getCellOfObject().size()==0||!MP.getCellOfObject().contains(cellid))
            {
              SocketManager.GAME_SEND_BN(player);
              return;
            }
            if(MP.getObject().size()<MP.getMaxObject())
            {
              MP.addObject(cellid,id0,player.getId(),resistMax,resist);
              SocketManager.SEND_GDO_PUT_OBJECT_MOUNT(map0,cellid+";"+id0+";1;"+resist+";"+resistMax);
            }
            else
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"1107");
              return;
            }
            break;

          case 22://Poser un prisme.
            if(player0.getFight()!=null)
              return;
            map0=player.getCurMap();
            int cellId1=player.getCurCell().getId();
            SubArea subArea=map0.getSubArea();
            Area area=subArea.getArea();
            int alignement=player.get_align();
            if(cellId1<=0)
              return;
            if(Config.getInstance().HEROIC)
            if(area.getId() == 4 || area.getId() ==17)
            {
                SocketManager.GAME_SEND_MESSAGE(player,"Vous ne pouvez pas placer un prisme sur cette carte..");
                return;
              }
            long calcul=System.currentTimeMillis()-subArea.time;
            if(calcul<600000)
            {
              player.sendMessage("Vous devez attendre "+((600000-calcul)/60000)+" minute(s) avant d'attaquer ce percepteur.");
              return;
            }
            if(alignement==0||alignement==3)
            {
              SocketManager.GAME_SEND_MESSAGE(player,"Vous n'avez pas l'alignement nécessaire pour placer un prisme.");
              return;
            }
            if(!player.is_showWings())
            {
              SocketManager.GAME_SEND_MESSAGE(player,"Vos ailes doivent étre activées avant de placer un prisme.");
              return;
            }
            if(map0.noPrism||(subArea!=null&&(subArea.getId()==9||subArea.getId()==95))||map0.haveMobFix()||map0.getMobGroups().isEmpty()||map0.getPlaces().isEmpty())
            {
              SocketManager.GAME_SEND_MESSAGE(player,"Vous ne pouvez pas placer un prisme sur cette carte.");
              return;
            }
            if(subArea.getAlignement()!=0||!subArea.getConquistable())
            {
              SocketManager.GAME_SEND_MESSAGE(player,"L'alignement de cette sous-zone n'est pas neutre.");
              return;
            }
            Prism Prisme=new Prism(Main.world.getNextIDPrisme(),alignement,1,map0.getId(),cellId1,player.get_honor(),-1);
            subArea.setAlignement(alignement);
            subArea.setPrismId(Prisme.getId());
            for(Player z : Main.world.getOnlinePlayers())
            {
              if(z==null)
                continue;
              if(z.get_align()==0)
              {
                SocketManager.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(z,subArea.getId()+"|"+alignement+"|1");
                if(area.getAlignement()==0)
                  SocketManager.GAME_SEND_aM_ALIGN_PACKET_TO_AREA(z,area.getId()+"|"+alignement);
                continue;
              }
              SocketManager.GAME_SEND_am_ALIGN_PACKET_TO_SUBAREA(z,subArea.getId()+"|"+alignement+"|0");
              if(area.getAlignement()==0)
                SocketManager.GAME_SEND_aM_ALIGN_PACKET_TO_AREA(z,area.getId()+"|"+alignement);
            }
            if(area.getAlignement()==0)
            {
              area.setPrismId(Prisme.getId());
              area.setAlignement(alignement);
              Prisme.setConquestArea(area.getId());
            }
            Main.world.addPrisme(Prisme);
            Database.getDynamics().getPrismData().add(Prisme);
            player.getCurMap().getSubArea().setAlignement(player.get_align());
            Database.getDynamics().getSubAreaData().update(player.getCurMap().getSubArea());
            SocketManager.GAME_SEND_PRISME_TO_MAP(map0,Prisme);
            break;

          case 23://Rappel Prismatique.
            if(player0.getFight()!=null)
              return;
            int dist=99999,alea;
            mapId=0;
            cellId=0;
            for(Prism i : Main.world.AllPrisme())
            {
              if(i.getAlignement()!=player.get_align())
                continue;
              alea=(Main.world.getMap(i.getMap()).getX()-player.getCurMap().getX())*(Main.world.getMap(i.getMap()).getX()-player.getCurMap().getX())+(Main.world.getMap(i.getMap()).getY()-player.getCurMap().getY())*(Main.world.getMap(i.getMap()).getY()-player.getCurMap().getY());
              if(alea<dist)
              {
                dist=alea;
                mapId=i.getMap();
                cellId=i.getCell();
              }
            }
            if(mapId!=0)
              player.teleport(mapId,cellId);
            break;

          case 24://TP Village aligné.
            if(player0.getFight()!=null)
              return;
            mapId=(short)Integer.parseInt(arg.split(",")[0]);
            cellId=Integer.parseInt(arg.split(",")[1]);
            if(Main.world.getMap(mapId).getSubArea().getAlignement()==player.get_align())
              player.teleport(mapId,cellId);
            break;

          case 25://Spawn groupe.
        	  if(Config.getInstance().fightAsBlocked)
        		  return;
            if(player0.getFight()!=null)
              return;
           /* for(MobGroup group : player.getCurMap().getMobGroups().values())
            {
             if(group.getCellId() == player.getCurCell().getId()) {
            	 player.sendMessage("Casse Non libre");
                return;
             }
            }*/
            int numbres = 0;
            for(MobGroup group : player.getCurMap().getMobGroups().values())
            {
            	if(group.getIp() == null)
            	continue;	
             if(group.getIp().compareTo(player.getAccount().getCurrentIp()) == 0) {
            	numbres++;
            	if(numbres >= 9)
            	{
            	player.sendMessage("9 Captures par ip maximum");
                return;
            	}
             }
            }
            
            boolean inArena=arg.split(";")[0].equals("true");
            String groupData="";
            if(!Capture.isInArenaMap(player.getCurMap().getId()))
              return;
            if(arg.split(";")[1].equals("1"))
            {
            	if(inArena){
					groupData =  arg.substring(7);	
				}else{
					groupData = arg.substring(8);	
				}
            }
            else
            {
              Capture capture=(Capture)World.getGameObject(objet);
              groupData=capture.parseGroupData();
            }
            String condition="MiS = "+player.getId();
            player.getCurMap().spawnNewGroup(true,player.getCurCell().getId(),groupData,condition,player.getId(),player.getAccount().getCurrentIp());
            break;

          case 26://Ajout d'objet.
            if(player0.getFight()!=null)
              return;
            for(String i : arg.split(";"))
            {
              obj=Main.world.getObjTemplate(Integer.parseInt(i.split(",")[0])).createNewItem(Integer.parseInt(i.split(",")[1]),false);
              if(player.addObjet(obj,true))
                World.addGameObject(obj,true);
            }
            SocketManager.GAME_SEND_Ow_PACKET(player);
            break;

          case 27://Ajout de titre.
            if(player0.getFight()!=null)
              return;
            player.setAllTitle(arg);
            break;

          case 28://Ajout de zaap.
            if(player0.getFight()!=null)
              return;
            player.verifAndAddZaap((short)Integer.parseInt(arg));
            break;

          case 29://Panel d'oubli de sort.
            if(player0.getFight()!=null)
              return;
            player.setExchangeAction(new ExchangeAction<>(ExchangeAction.FORGETTING_SPELL,0));
            SocketManager.GAME_SEND_FORGETSPELL_INTERFACE('+',player);
            break;

          case 31://Cadeau bworker.
            if(player0.getFight()!=null)
              return;
            new Action(511,"","",null).apply(player,null,objet,-1);
            break;

          case 32://Géoposition traque.
            if(player0.getFight()!=null)
              return;
            String traque=World.getGameObject(objet).getTraquedName();

            if(traque==null)
              break;

            Player cible=Main.world.getPlayerByName(traque);

            if(cible==null)
              break;

            if(!cible.isOnline())
            {
              SocketManager.GAME_SEND_Im_PACKET(player,"1198");
              break;
            }

            SocketManager.GAME_SEND_FLAG_PACKET(player,cible);
            break;

          case 33: {
				if(player0.getMount() == null)return;
				
				Mount mount=player.getMount();
		        Main.world.addMount(new Mount(mount.getId(),mount.getColor(),mount.getSex(),mount.getAmour(),mount.getEndurance(),mount.getLevel(),mount.getExp(),mount.getName(),mount.getFatigue(),mount.getEnergy(),mount.getReproduction(),mount.getMaturity(),mount.getState(),mount.parseObjectsToString(),mount.getAncestors(),"9",mount.getSize(),mount.getCellId(),mount.getMapId(),mount.getOwner(),mount.getOrientation(),mount.getFecundatedDate(),mount.getCouple(),mount.getSavage()));
		        player.setMount(Main.world.getMountById(mount.getId()));
		        SocketManager.GAME_SEND_Re_PACKET(player,"+",Main.world.getMountById(mount.getId()));
		        Database.getDynamics().getMountData().update(mount);
		        SocketManager.GAME_SEND_MESSAGE(player,"Votre dragodinde a eté transforme on caméléone");
				break;
			}

          case 34: {
 			 int classe = 0;
 			 try
  				{
  				classe = Integer.parseInt(args);
  			}catch(Exception e)
  			{
  				return;
  			};
  			if(classe > 12 || classe < 1){
					  break;
					  }
  			player0.setClasse(classe);
  			player0.setGfxId(classe*10 + player0.getSexe());
  			StringBuilder sort = new StringBuilder();
  			for (final Entry<Integer, SortStats> i : player0.getSorts().entrySet()) {
					if(i.getValue().getSpellID() == 350)sort.append(i.getValue().getSpellID()+";");
					if(i.getValue().getSpellID() == 366)sort.append(i.getValue().getSpellID()+";");
					if(i.getValue().getSpellID() == 370)sort.append(i.getValue().getSpellID()+";");
					if(i.getValue().getSpellID() == 367)sort.append(i.getValue().getSpellID()+";");
					if(i.getValue().getSpellID() == 413)sort.append(i.getValue().getSpellID()+";");
					if(i.getValue().getSpellID() == 414)sort.append(i.getValue().getSpellID()+";");
					if(i.getValue().getSpellID() == 369)sort.append(i.getValue().getSpellID()+";");
					if(i.getValue().getSpellID() == 364)sort.append(i.getValue().getSpellID()+";");
					if(i.getValue().getSpellID() == 481)sort.append(i.getValue().getSpellID()+";");
				}
				int parcho_sort = 0;
				for (final Entry<Integer, SortStats> i : player0.getSorts().entrySet()) {
					if(i.getValue().getLevel() == 1)continue;
					if(i.getValue().getLevel() == 2)parcho_sort +=1;
					if(i.getValue().getLevel() == 3)parcho_sort +=3;
					if(i.getValue().getLevel() == 4)parcho_sort +=6;
					if(i.getValue().getLevel() == 5)parcho_sort +=10;
					if(i.getValue().getLevel() == 6)parcho_sort +=15;
				}
				parcho_sort+= player0.get_spellPts();
  			player0.setsorts(null);
  			player0.setsorts(Constant.getStartSorts(classe));
					for (int a = 1; a <= player0.getLevel(); a++) {
						Constant.onLevelUpSpells(player0, a);
					}
					if((player0.getLevel() - 1) >= parcho_sort){
						parcho_sort = ((player0.getLevel() - 1)-parcho_sort);
					}else{
						 parcho_sort = (parcho_sort-(player0.getLevel() - 1));	
					}
					player0.set_spellPts((player0.getLevel() - 1)+parcho_sort);
				  Database.getStatics().getPlayerData().updateInfos(player0);
				  player0.getStats().addOneStat(125, -player0.getStats().getEffect(125));
				  player0.getStats().addOneStat(124, -player0.getStats().getEffect(124));
				  player0.getStats().addOneStat(118, -player0.getStats().getEffect(118));
				  player0.getStats().addOneStat(123, -player0.getStats().getEffect(123));
				  player0.getStats().addOneStat(119, -player0.getStats().getEffect(119));
				  player0.getStats().addOneStat(126, -player0.getStats().getEffect(126));
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
				  player0.addCapital((player0.getLevel() - 1) * 5 - player0.get_capital());
				  for(String sortid : sort.toString().split("\\;"))
					{
						if(sortid.equals(""))continue;
						int id = Integer.parseInt(sortid);
						player0.learnSpell(id, 1, false, true, false);
					   }	
				  //Database.getStatics().getPlayerData().update(player0);
				  //kick
		          player0.getAccount().getGameClient().kick();
			break;
		}
          case 35: {
				final int points = Integer.parseInt(args);
				 player0.getAccount().setPoints( player0.getAccount().getPoints() + points);
				 player.sendMessage("Vous venez de gagner "+ points+" points boutiques.");
				 Database.getStatics().getPlayerData().shop_parcho(player.getName(), player.getAccID(), objet, points);
				break;
			}
          case 36: { // changement de nom
        	  player.send("AEn");
        	  return;
			}
          case 37: {// changement de coleur
        	  player.send("AEc");
        	  return;
			}
          case 38: {// abonnement 7 jours
        	  long time = player.getAccount().getSubscribeRemaining()+System.currentTimeMillis()+604800000;

        	  for(Player perso1 : Main.world.getOnlinePlayers())
        	  {
        		  if(perso1.getAccount().getId_web() != player.getAccount().getId_web())
        			  continue;
        		  perso1.getAccount().setSubscriber(time);
            	  Database.getStatics().getAccountData().abonnement(perso1.getAccount().getId_web(), time);
            	  perso1.sendMessage("Vous venez d'activer le passe 7 Jours");
        	  }
        	  break;
			}
          case 39: {// abonnement 15 jours
        	  long time = player.getAccount().getSubscribeRemaining()+System.currentTimeMillis()+1296000000;
        	  for(Player perso1 : Main.world.getOnlinePlayers())
        	  {
        		  if(perso1.getAccount().getId_web() != player.getAccount().getId_web())
        			  continue;
        		  perso1.getAccount().setSubscriber(time);
            	  Database.getStatics().getAccountData().abonnement(perso1.getAccount().getId_web(), time);
            	  perso1.sendMessage("Vous venez d'activer le passe 15 Jours");
        	  }
        	  break;
			}
          case 40: {// Mimibiote
        	  player.send("AEi");
        	  return;
			}
          case 41: {// titre perso
        	  player.send("bL");
        	  return;
			}
          case 42: {// abonnement 30 jours
        	  long time = player.getAccount().getSubscribeRemaining()+System.currentTimeMillis()+2592000000L;
        	  for(Player perso1 : Main.world.getOnlinePlayers())
        	  {
        		  if(perso1.getAccount().getId_web() != player.getAccount().getId_web())
        			  continue;
        		  perso1.getAccount().setSubscriber(time);
            	  Database.getStatics().getAccountData().abonnement(perso1.getAccount().getId_web(), time);
            	  perso1.sendMessage("Vous venez d'activer le passe 30 Jours");
        	  }
        	  break;
			}
          case 43 : { // potion 5 etoilles
        	  int numbre =  0;
        	  if(Capture.isInArenaMap(player.getCurMap().getId())) {
        		  for(Entry<Integer, MobGroup> entry :player.getCurMap().getMobGroups().entrySet())
                  {
            		  if(numbre == 3)
            			  continue;
            		  if(entry.getValue() == null)
            			  continue;
            		  if(entry.getValue().get_id_player() != player.getId())
            			  continue;
            		  if(entry.getValue().getSpawnTime() == 6000)
            			  continue;
                    entry.getValue().setSpawnTime(6000);
                     SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(),entry.getValue().getId());
                     SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(player.getCurMap(),entry.getValue());
                     numbre++;
                  }  
        		  
        	  }
        	  for(Entry<Integer, MobGroup> entry :player.getCurMap().getMobGroups().entrySet())
              {
        		  if(numbre == 3)
        			  continue;
        		  if(entry.getValue() == null)
        			  continue;
        		  if(entry.getValue().getSpawnTime() == 6000)
        			  continue;
                entry.getValue().setSpawnTime(6000);
                 SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(),entry.getValue().getId());
                 SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(player.getCurMap(),entry.getValue());
                 numbre++;
              }
        	  if(numbre == 0)
        		  return;
        	break;  
          }
          case 44: {// potion dj 3 jour
        	  long time = player.getAccount().getTime_dj()+System.currentTimeMillis()+259200000;
        	 
        	  Database.getStatics().getAccountData().dj(player.getAccount().getId_web(), time);
        	  
        	  for(Player perso1 : Main.world.getOnlinePlayers())
        	  {
        		  if(perso1.getAccount().getId_web() != player.getAccount().getId_web())
        			  continue;
        		  perso1.getAccount().setTime_dj(time);
        		  perso1.sendMessage("Vous venez d'activer le passe 2 Jours Dj");
        	  if(perso1.getItemTemplate(10207)!=null) {
        		  perso1.removeByTemplateID(10207,1);
        	       // SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+10207);  
        	  }
        		  GameObject obj2=Main.world.getObjTemplate(10207).createNewItem(1,false);
        	        if(perso1.addObjet(obj2,true))
        	          World.addGameObject(obj2,true);
        	        SocketManager.GAME_SEND_Ow_PACKET(perso1);
        	        SocketManager.GAME_SEND_Im_PACKET(perso1,"021;"+1+"~"+10207);  
        	  }
        	  
        	  break;
			}
          case 45: {// potion dj 7 jour
        	  long time = player.getAccount().getTime_dj()+System.currentTimeMillis()+604800000;
        	  
        	  Database.getStatics().getAccountData().dj(player.getAccount().getId_web(), time);
        	  
        	  for(Player perso1 : Main.world.getOnlinePlayers())
        	  {
        		  if(perso1.getAccount().getId_web() != player.getAccount().getId_web())
        			  continue;
        		  perso1.getAccount().setTime_dj(time);
        		  perso1.sendMessage("Vous venez d'activer le passe 5 Jours Dj");
        	  if(perso1.getItemTemplate(10207)!=null) {
        		  perso1.removeByTemplateID(10207,1);
        	       // SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+10207);  
        	  }
        		  GameObject obj2=Main.world.getObjTemplate(10207).createNewItem(1,false);
        	        if(perso1.addObjet(obj2,true))
        	          World.addGameObject(obj2,true);
        	        SocketManager.GAME_SEND_Ow_PACKET(perso1);
        	        SocketManager.GAME_SEND_Im_PACKET(perso1,"021;"+1+"~"+10207);  
        	  }
        	  
        	  break;
			}
          case 46: {// Mystery Box : Polykrome
        	  List<Integer> givenList = Arrays.asList(11476,11477,11478,11479,11480,11481,11482,11483,11484
        			  ,11485,11486,11487,11488,11489,11490,11491,11492,11493,11494,11495,11496,11497);
        	    Random rand = new Random();
        	    int randomElement = givenList.get(rand.nextInt(givenList.size()));
        	    GameObject obj2=Main.world.getObjTemplate(randomElement).createNewItem(1,false);
    	        if(player.addObjet(obj2,true))
    	          World.addGameObject(obj2,true);
    	        SocketManager.GAME_SEND_Ow_PACKET(player);
    	        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+randomElement);  
        	  break;
			}
          case 47: {// Mystery Box : Bouclier Skin
        	  List<Integer> givenList = Arrays.asList(24471,24472,24473,24474,24475,24476,24477,24479,
        			  10167,10161,10163,10162,10161,10159,10158,10073,11472,10164,10160);
        	    Random rand = new Random();
        	    int randomElement = givenList.get(rand.nextInt(givenList.size()));
        	    GameObject obj2=Main.world.getObjTemplate(randomElement).createNewItem(1,false);
    	        if(player.addObjet(obj2,true))
    	          World.addGameObject(obj2,true);
    	        SocketManager.GAME_SEND_Ow_PACKET(player);
    	        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+randomElement);  
        	  break;
			}
          case 48: {// Mystery Box : Coiffe Skin 2.X
        	  List<Integer> givenList = Arrays.asList(101531,101532,101533,101534,101535,101536,101537,101538,101539,
        			  101540,101541,101542,101543,101544,101545,101546,101547,101548,101549,
        			  101550,101551,101552,101553,101554,101555,101556,101557,101558,101559,101560,101561,101562,
        			  101563,101564,101565,101566,101567,101568,101569,101570,101571,101572,101573,101574,101575,
        			  101576,101577,101578,101579,101580,101581,101582,101583,101584,101585,101586,101587,101588,
        			  101589,101590,101591,101592,101593,101594,101595,101596,101597,101598,101599,101600,101601,
        			  101602,101603,101604,101605);
        	    Random rand = new Random();
        	    int randomElement = givenList.get(rand.nextInt(givenList.size()));
        	    GameObject obj2=Main.world.getObjTemplate(randomElement).createNewItem(1,false);
    	        if(player.addObjet(obj2,true))
    	          World.addGameObject(obj2,true);
    	        SocketManager.GAME_SEND_Ow_PACKET(player);
    	        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+randomElement);  
        	  break;
			}
          case 49: {// Mystery Box : Cape Skin 2.X
        	  List<Integer> givenList = Arrays.asList(101606,101462,101463,101464,101465,101466,101467,101468,
        			  101469,101470,101471,101472,101473,101474,101475,101476,101477,101478,101479,101480,
        			  101481,101482,101483,101484,101485,101486,101487,101488,101489,101490,101491,101492,
        			  101493,101494,101495,101496,101497,101498,101499,101500,101501,101502,101503,101504,
        			  101505,101506,101507,101508,101509,101510,101511,101512,101513,101514,101515,101516,
        			  101517,101518,101519,101520,101521,101522,101523,101524,101525,101526,101527,101528,101529,101530);
        	    Random rand = new Random();
        	    int randomElement = givenList.get(rand.nextInt(givenList.size()));
        	    GameObject obj2=Main.world.getObjTemplate(randomElement).createNewItem(1,false);
    	        if(player.addObjet(obj2,true))
    	          World.addGameObject(obj2,true);
    	        SocketManager.GAME_SEND_Ow_PACKET(player);
    	        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+randomElement);  
        	  break;
			}
          case 50: {// Mystery Box : Familier 2.x
        	  List<Integer> givenList = Arrays.asList(101612,101613,101614,101615,101616,101617,101618,101619,
        			  101621,101622,101623,101624,101625,101626,101627,101628,101629,101630,101631);
        	    Random rand = new Random();
        	    int randomElement = givenList.get(rand.nextInt(givenList.size()));
        	    GameObject obj2=Main.world.getObjTemplate(randomElement).createNewItem(1,false);
    	        if(player.addObjet(obj2,true))
    	          World.addGameObject(obj2,true);
    	        SocketManager.GAME_SEND_Ow_PACKET(player);
    	        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+randomElement);  
        	  break;
			}
          case 51: {// Mystery Box : Bouclier 2.x
        	  List<Integer> givenList = Arrays.asList(100256,100261,100264,100265,100279,100281,101645,101646,101647,
        			  101648,101649,101650,101651,101652,101653,101654,101655,101656);
        	    Random rand = new Random();
        	    int randomElement = givenList.get(rand.nextInt(givenList.size()));
        	    GameObject obj2=Main.world.getObjTemplate(randomElement).createNewItem(1,false);
    	        if(player.addObjet(obj2,true))
    	          World.addGameObject(obj2,true);
    	        SocketManager.GAME_SEND_Ow_PACKET(player);
    	        SocketManager.GAME_SEND_Im_PACKET(player,"021;"+1+"~"+randomElement);  
        	  break;
			}
          case 52: {// renom guilde
        	  if(player.get_guild() == null)
        	  {
        		  player.sendMessage("Vous ne posséde pas de guilde"); 
        		return;  
        	  }
        	  if(player.get_guild().getMember(player.getId()) == null)
        		return;
        	  if(player.get_guild().getMember(player.getId()).getRank() != 1) {
        		  player.sendMessage("Vous devez etre le mineur de votre guilde");
          		return;
        	  }
        	  player.send("gn");
        	  return;
			}
          case 53: {// panel Size
        	  player.send("wO");
        	  return;
			}
        }
        turn++;
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    boolean effect=this.haveEffect(World.getGameObject(objet).getTemplate().getId(),World.getGameObject(objet),player);
    if(effect)
      isOk=true;
    if(isOk)
      effect=true;
    if(this.type.split(";").length>1)
      isOk=true;
    if(objet!=-1)
    {
      if(send)
        SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+World.getGameObject(objet).getTemplate().getId());
      if(sureIsOk||(isOk&&effect&&World.getGameObject(objet).getTemplate().getId()!=7799))
      {
        if(World.getGameObject(objet)!=null)
        {
          player0.removeItem(objet,1,true,true);
        }
      }
    }
  }


  public String getArgs(){
      return this.args;
  }

private boolean haveEffect(int id, GameObject gameObject, Player player)
  {
    if(player.getFight()!=null)
      return true;
    switch(id)
    {
      case 8378://Fragment magique.
        for(Pair<Integer, Integer> couple : ((Fragment)gameObject).getRunes())
        {
          ObjectTemplate objectTemplate=Main.world.getObjTemplate(couple.getLeft());

          if(objectTemplate==null)
            continue;

          GameObject newGameObject=objectTemplate.createNewItem(couple.getRight(),true);

          if(newGameObject==null)
            continue;

          if(!player.addObjetSimiler(newGameObject,true,-1))
          {
            World.addGameObject(newGameObject,true);
            player.addObjet(newGameObject);
          }
        }
        send=true;
        return true;
      case 7799://Le Saut Sifflard
        player.toogleOnMount();
        send=false;
        return false;

      case 10832://Craqueloroche
    	  if(!Capture.isInArenaMap(player.getCurMap().getId()))
              return false;
        player.getCurMap().spawnNewGroup(true,player.getCurCell().getId(),"483,1,1000","MiS="+player.getId(), player.getId(),player.getAccount().getCurrentIp());
        return true;

      case 10664://Abragland
    	  if(!Capture.isInArenaMap(player.getCurMap().getId()))
              return false;
        player.getCurMap().spawnNewGroup(true,player.getCurCell().getId(),"47,1,1000","MiS="+player.getId(),player.getId(),player.getAccount().getCurrentIp());
        return true;

      case 10665://Coffre de Jorbak
        player.setCandy(10688);
        return true;

      case 10670://Parchemin de persimol
        player.setBenediction(10682);
        return true;

      case 8435://Ballon Rouge Magique
        SocketManager.sendPacketToMap(player.getCurMap(),"GA;208;"+player.getId()+";"+player.getCurCell().getId()+",2906,11,8,1");
        return true;

      case 8624://Ballon Bleu Magique
        SocketManager.sendPacketToMap(player.getCurMap(),"GA;208;"+player.getId()+";"+player.getCurCell().getId()+",2907,11,8,1");
        return true;

      case 8625://Ballon Vert Magique
        SocketManager.sendPacketToMap(player.getCurMap(),"GA;208;"+player.getId()+";"+player.getCurCell().getId()+",2908,11,8,1");
        return true;

      case 8430://Ballon Jaune Magique
        SocketManager.sendPacketToMap(player.getCurMap(),"GA;208;"+player.getId()+";"+player.getCurCell().getId()+",2909,11,8,1");
        return true;

      case 8621://Cawotte Maudite
        player.setGfxId(1109);
        SocketManager.GAME_SEND_ALTER_GM_PACKET(player.getCurMap(),player);
        return true;

      case 8626://Nisitik Miditik
        player.setGfxId(1046);
        SocketManager.GAME_SEND_ALTER_GM_PACKET(player.getCurMap(),player);
        return true;

      case 10833://Chapain
        player.setGfxId(9001);
        SocketManager.GAME_SEND_ALTER_GM_PACKET(player.getCurMap(),player);
        return true;

      case 10839://Monstre Pain
    	  if(!Capture.isInArenaMap(player.getCurMap().getId()))
              return false;
        player.getCurMap().spawnNewGroup(true,player.getCurCell().getId(),"2787,1,1000","MiS="+player.getId(), player.getId(),player.getAccount().getCurrentIp());
        return true;

      case 8335://Cadeau 1
        Noel.getRandomObjectOne(player);
        return true;
      case 8336://Cadeau 2
        Noel.getRandomObjectTwo(player);
        return true;
      case 8337://Cadeau 3
        Noel.getRandomObjectTree(player);
        return true;
      case 8339://Cadeau 4
        Noel.getRandomObjectFour(player);
        return true;
      case 8340://Cadeau 5
        Noel.getRandomObjectFive(player);
        return true;
      case 10912://Cadeau nowel 1
        return false;
      case 10913://Cadeau nowel 2
        return false;
      case 10914://Cadeau nowel 3
        return false;
    }
    return false;
  }

    public int getType(){
        return Integer.parseInt(type);
    }
}
