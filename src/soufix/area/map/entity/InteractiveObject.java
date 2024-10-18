package soufix.area.map.entity;

import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.entity.monster.MobGroup;
import soufix.game.scheduler.Updatable;
import soufix.job.JobConstant;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.other.Dopeul;
import soufix.quest.Quest;
import soufix.quest.QuestPlayer;
import soufix.quest.QuestStep;

import java.util.ArrayList;

public class InteractiveObject
{

  public final static Updatable updatable=new Updatable(0)
  {
    private final ArrayList<InteractiveObject> queue=new ArrayList<>();

    @Override
    public void update()
    {
    	 try
         {
      if(this.queue.isEmpty())
        return;
      long time=System.currentTimeMillis();
      new ArrayList<>(this.queue).stream().filter(interactiveObject -> interactiveObject!=null&&interactiveObject.getTemplate()!=null&&time-interactiveObject.lastTime>interactiveObject.getTemplate().getRespawnTime()).forEach(interactiveObject -> {
        interactiveObject.active();
        this.queue.remove(interactiveObject);
      });
         }
         catch(Exception e)
         {
           e.printStackTrace();
         }
    }

    @Override
    public ArrayList<InteractiveObject> get()
    {
      return queue;
    }
  };

  private int id, state;
  private GameMap map;
  private GameCase cell;
  private boolean interactive=true, walkable;
  private long lastTime=0;
  private InteractiveObjectTemplate template;

  public InteractiveObject(int id, final GameMap iMap, GameCase iCell)
  {
    this.id=id;
    this.map=iMap;
    this.cell=iCell;
    this.state=JobConstant.IOBJECT_STATE_FULL;
    this.template=Main.world.getIOTemplate(this.id);
    this.walkable=this.getTemplate()!=null&&this.getTemplate().isWalkable()&&this.state==JobConstant.IOBJECT_STATE_FULL;
  }

  public static void getActionIO(final Player player, GameCase cell, int id)
  {
    switch(id)
    {
      case 7041:
      case 7042:
      case 7043:
      case 7044:
      case 7045:
      case 1748:
        if(InteractiveDoor.tryActivate(player,cell))
          return;
        break;
    }
    switch(id)
    {
      case 1524:
      case 542://Statue Phoenix.
        if(player.isGhost())
        {
          player.setAlive();
          Quest q=Quest.getQuestById(190);
          if(q!=null)
          {
            QuestPlayer qp=player.getQuestPersoByQuest(q);
            if(qp!=null)
            {
              QuestStep qe=q.getCurrentQuestStep(qp);
              if(qe!=null)
                if(qe.getId()==783)
                  q.updateQuestData(player,true,qe.getValidationType());
            }
          }
        }
        break;

      //v2.7 - Replaced String += with StringBuilder
      case 684://Portillon donjon squelette.
        if(player.hasItemTemplate(10207,1))
        {
          String stats,statsReplace="";
          GameObject object=player.getItemTemplate(10207);
          stats=object.getTxtStat().get(Constant.STATS_NAME_DJ);
          try
          {
            for(String i : stats.split(","))
            {
              if(Dopeul.parseConditionTrousseau(i.replace(" ",""),-1,player.getCurMap().getId()))
              {
                player.teleport((short)2110,118);
                statsReplace=i;
              }
            }
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
          if(!statsReplace.isEmpty())
          {
        	  if(player.getAccount().getTime_dj() == 0L) {
            StringBuilder newStats=new StringBuilder();
            for(String i : stats.split(","))
              if(!i.equals(statsReplace))
                newStats.append((newStats.toString().isEmpty() ? i : ","+i));
            object.getTxtStat().remove(Constant.STATS_NAME_DJ);
            object.getTxtStat().put(Constant.STATS_NAME_DJ,newStats.toString());
            SocketManager.GAME_SEND_UPDATE_ITEM(player,player.getItemTemplate(10207));
        	  }
            break;
          }
        }

        if(!player.hasItemTemplate(1570,1))
        {
          SocketManager.GAME_SEND_MESSAGE(player,"You do not have the necessary key.","009900");
        }
        else
        {
          player.removeByTemplateID(1570,1);
          SocketManager.GAME_SEND_Im_PACKET(player,"022;"+1+"~"+1570);
          player.teleport((short)2110,118);
        }
        break;

      case 1330://Pierre de kwak
        player.getCurMap().startFightVersusProtectors(player,new MobGroup(player.getCurMap().nextObjectId,cell.getId(),getKwakere(player.getCurMap().getId())+","+40+","+40));
        break;

      case 1679:
        player.warpToSavePos();
        break;

      case 3000://Epée Crocoburio
        if(player.hasEquiped(1718)&&player.hasEquiped(1719)&&player.hasEquiped(1720)&&player.getStats().getEffect(Constant.STATS_ADD_VITA)==120&&player.getStats().getEffect(Constant.STATS_ADD_SAGE)==0&&player.getStats().getEffect(Constant.STATS_ADD_FORC)==60&&player.getStats().getEffect(Constant.STATS_ADD_INTE)==50&&player.getStats().getEffect(Constant.STATS_ADD_AGIL)==0&&player.getStats().getEffect(Constant.STATS_ADD_CHAN)==0)
        {
          SocketManager.GAME_SEND_ACTION_TO_DOOR(player.getCurMap(),237,true);
          SocketManager.GAME_SEND_MESSAGE(player,"La transformation crocoburio a été désactivée.");
          /*perso.getWaiter().addNext(new Runnable()
          {
          	public void run()
          	{
          		perso.setFullMorph(10, false, false);
          	}
          }, 3000);*/
        }
        else
        {
          SocketManager.GAME_SEND_Im_PACKET(player,"119");
        }

        break;

      case 7546://Foire au troll
      case 7547:
        SocketManager.send(player,"GDF|"+cell.getId()+";3");
        break;

      case 1324:// Plot Rouge des émotes
        switch(player.getCurMap().getId())
        {
          case 2196:
            if(player.isAway())
              return;
            if(player.get_guild()!=null||player.getGuildMember()!=null&&player.hasItemTemplate(1575,1))
            {
              player.removeByTemplateID(1575,1);
              SocketManager.GAME_SEND_gC_PACKET(player,"Ea");
              SocketManager.GAME_SEND_Im_PACKET(player,"14");
              return;
            }
            SocketManager.GAME_SEND_gn_PACKET(player);
            break;
          case 2037://Emote Faire signe
            player.addStaticEmote(2);
            break;
          case 2025://Emote Applaudir
            player.addStaticEmote(3);
            break;
          case 2039://Emote Se mettre en Colére
            player.addStaticEmote(4);
            break;
          case 2047://Emote Peur
            player.addStaticEmote(5);
            break;
          case 8254://Emote Montrer son Arme
            player.addStaticEmote(6);
            break;
          case 2099://Emote Saluer
            player.addStaticEmote(9);
            break;
          case 8539://Emote Croiser les bras
            player.addStaticEmote(14);
            break;
        }
        break;
      case 1694://Village brigandin tire éolienne
        SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"","2",player.getId()+"","4");
        player.teleport((short)6848,390);
        break;
      case 1695://Village brigandin tire éolienne
        SocketManager.GAME_SEND_GA_PACKET(player.getGameClient(),"","2",player.getId()+"","3");
        player.teleport((short)6844,268);
        break;
      default:
        break;
    }
  }

  public static void getSignIO(Player perso, int cell, int id)
  {
    switch(perso.getCurMap().getId())
    {
      case 7460:
        String[][] q=Constant.HUNTING_QUESTS;
        for(int v=0;v<q.length;v++)
        {
          if(Integer.parseInt(q[v][1])==cell&&Integer.parseInt(q[v][0])==id)
          {
            SocketManager.send(perso,"dCK"+q[v][2]);
            break;
          }
        }
        break;

      case 7411:
        if(id==1531&&cell==230)
          SocketManager.send(perso,"dCK139_0612131303");
        break;

      case 7543:
        if(id==1528&&cell==262)
          SocketManager.send(perso,"dCK75_0603101710");
        if(id==1533&&cell==169)
          SocketManager.send(perso,"dCK74_0603101709");
        if(id==1528&&cell==169)
          SocketManager.send(perso,"dCK73_0706211414");
        break;

      case 7314:
        if(id==1531&&cell==93)
          SocketManager.send(perso,"dCK78_0706221019");
        if(id==1532&&cell==256)
          SocketManager.send(perso,"dCK76_0603091219");
        if(id==1533&&cell==415)
          SocketManager.send(perso,"dCK77_0603091218");
        break;

      case 7417:
        if(id==1532&&cell==264)
          SocketManager.send(perso,"dCK79_0603101711");
        if(id==1528&&cell==211)
          SocketManager.send(perso,"dCK80_0510251009");
        if(id==1532&&cell==212)
          SocketManager.send(perso,"dCK77_0603091218");
        if(id==1529&&cell==212)
          SocketManager.send(perso,"dCK81_0510251010");
        break;

      case 2698:
        if(id==1531&&cell==93)
          SocketManager.send(perso,"dCK51_0706211150");
        if(id==1528&&cell==109)
          SocketManager.send(perso,"dCK41_0706221516");
        break;

      case 2814:
        if(id==1533&&cell==415)
          SocketManager.send(perso,"dCK43_0706201719");
        if(id==1532&&cell==326)
          SocketManager.send(perso,"dCK50_0706211149");
        if(id==1529&&cell==325)
          SocketManager.send(perso,"dCK41_0706221516");
        break;

      case 3087:
        if(id==1529&&cell==89)
          SocketManager.send(perso,"dCK41_0706221516");
        break;

      case 3018:
        if(id==1530&&cell==354)
          SocketManager.send(perso,"dCK52_0706211152");
        if(id==1532&&cell==256)
          SocketManager.send(perso,"dCK50_0706211149");
        if(id==1528&&cell==255)
          SocketManager.send(perso,"dCK41_0706221516");
        break;

      case 3433:
        if(id==1533&&cell==282)
          SocketManager.send(perso,"dCK53_0706211407");
        if(id==1531&&cell==179)
          SocketManager.send(perso,"dCK50_0706211149");
        if(id==1529&&cell==178)
          SocketManager.send(perso,"dCK41_0706221516");
        break;

      case 4493:
        if(id==1533&&cell==415)
          SocketManager.send(perso,"dCK43_0706201719");
        if(id==1532&&cell==326)
          SocketManager.send(perso,"dCK50_0706211149");
        if(id==1529&&cell==325)
          SocketManager.send(perso,"dCK41_0706221516");
        break;

      case 4876:
        if(id==1532&&cell==316)
          SocketManager.send(perso,"dCK54_0706211408");
        if(id==1531&&cell==283)
          SocketManager.send(perso,"dCK51_0706211150");
        if(id==1530&&cell==282)
          SocketManager.send(perso,"dCK52_0706211152");
        break;
    }
  }

  private static int getKwakere(int i)
  {
    switch(i)
    {
      case 2072:
        return 270;
      case 2071:
        return 269;
      case 2067:
        return 272;
      case 2068:
        return 271;
    }
    return 269;
  }

  public int getId()
  {
    return this.id;
  }

  public int getState()
  {
    return this.state;
  }

  public void setState(int state)
  {
    this.state=state;
  }

  public boolean isInteractive()
  {
    return this.interactive;
  }

  public void setInteractive(boolean interactive)
  {
    this.interactive=interactive;
  }

  public int getUseDuration()
  {
    int duration=1500;
    if(this.getTemplate()!=null)
      duration=this.getTemplate().getDuration();
    return duration;
  }

  public int getUnknowValue()
  {
    int unk=4;
    if(this.getTemplate()!=null)
      unk=this.getTemplate().getUnk();
    return unk;
  }

  public boolean isWalkable()
  {
    return this.walkable;
  }

  public InteractiveObjectTemplate getTemplate()
  {
    return template;
  }

  public void setTemplate(InteractiveObjectTemplate template)
  {
    this.template=template;
  }

  public void active()
  {
    this.state=JobConstant.IOBJECT_STATE_FULLING;
    this.interactive=true;
    SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(this.map,this.cell);
    this.state=JobConstant.IOBJECT_STATE_FULL;
  }

  public void desactive()
  {
    this.lastTime=System.currentTimeMillis();
    @SuppressWarnings("unchecked")
    ArrayList<InteractiveObject> array=(ArrayList<InteractiveObject>)InteractiveObject.updatable.get();
    array.add(this);
  }

  public static class InteractiveObjectTemplate
  {

    private int id;
    private int respawnTime;
    private int duration;
    private int unk;
    private boolean walkable;

    public InteractiveObjectTemplate(int id, int respawnTime, int duration, int unk, boolean walkable)
    {
     	
      this.id=id;
      this.respawnTime=respawnTime;
      this.duration=duration;
      this.unk=unk;
      this.walkable=walkable;
    }

    public int getId()
    {
      return id;
    }

    public boolean isWalkable()
    {
      return walkable;
    }

    public int getRespawnTime()
    {
      return respawnTime;
    }

    public int getDuration()
    {
      return duration;
    }

    public int getUnk()
    {
      return unk;
    }
  }

}