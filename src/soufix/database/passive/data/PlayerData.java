package soufix.database.passive.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.client.Account;
import soufix.client.Player;
import soufix.command.administration.Group;
import soufix.database.Database;
import soufix.database.passive.AbstractDAO;
import soufix.game.World;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class PlayerData extends AbstractDAO<Player>
{

  public PlayerData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  public int getNextId()
  {
    Result result=null;
    int guid=0;
    try
    {
      result=getData("SELECT id FROM players ORDER BY id DESC LIMIT 1");
      ResultSet RS=result.resultSet;

      if(!RS.first())
        guid=1;
      else
        guid=RS.getInt("id")+1;
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData getNextId",e);
    } finally
    {
      close(result);
    }
    return guid;
  }

  public void load()
  {
    Result result=null;
    try
    {
    	result=getData("SELECT * FROM players where server ="+Config.getInstance().serverId);
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        HashMap<Integer, Integer> stats=new HashMap<Integer, Integer>();
        stats.put(Constant.STATS_ADD_VITA,RS.getInt("vitalite"));
        stats.put(Constant.STATS_ADD_FORC,RS.getInt("force"));
        stats.put(Constant.STATS_ADD_SAGE,RS.getInt("sagesse"));
        stats.put(Constant.STATS_ADD_INTE,RS.getInt("intelligence"));
        stats.put(Constant.STATS_ADD_CHAN,RS.getInt("chance"));
        stats.put(Constant.STATS_ADD_AGIL,RS.getInt("agilite"));
        Player perso=new Player(RS.getInt("id"),RS.getString("name"),RS.getInt("groupe"),RS.getInt("sexe"),RS.getInt("class"),RS.getInt("color1"),RS.getInt("color2"),RS.getInt("color3"),RS.getLong("kamas"),RS.getInt("spellboost"),RS.getInt("capital"),RS.getInt("energy"),RS.getInt("level"),RS.getLong("xp"),RS.getInt("size"),RS.getInt("gfx"),RS.getByte("alignement"),RS.getInt("account"),stats,RS.getByte("seeFriend"),RS.getByte("seeAlign"),RS.getByte("seeSeller"),RS.getString("canaux"),RS.getShort("map"),RS.getInt("cell"),RS.getString("objets"),RS.getString("storeObjets"),RS.getString("pdvper"),RS.getString("spells"),RS.getString("savepos"),RS.getString("jobs"),RS.getInt("mountxpgive"),RS.getInt("mount"),RS.getInt("honor"),RS.getInt("deshonor"),RS.getInt("alvl"),RS.getString("zaaps"),RS.getInt("title"),RS.getInt("wife"),RS.getString("morphMode"),RS.getString("allTitle"),RS.getString("emotes"),RS.getLong("prison"),false,RS.getString("parcho"),RS.getLong("timeDeblo"),RS.getBoolean("noall"),RS.getString("deadInformation"),RS.getByte("deathCount"),RS.getLong("totalKills"),RS.getInt("tokens"),RS.getInt("apExo"),RS.getInt("mpExo"),RS.getInt("raExo"),RS.getString("rapid"),RS.getInt("song"),false,RS.getInt("ornement"),RS.getString("ornementsList"));
        Main.world.kamas_total += RS.getLong("kamas");
        perso.VerifAndChangeItemPlace();
        Main.world.addPlayer(perso);
        if(perso.isShowSeller())
          Main.world.addSeller(perso);
      }
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData load",e);
      Main.stop("unknown");
    } finally
    {
      close(result);
    }
  }

  public Player load(int obj)
  {
    Result result=null;
    Player player=null;
    try
    {
      result=getData("SELECT * FROM players WHERE id = '"+obj+"'");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        if(RS.getInt("server")!=Config.getInstance().serverId)
          continue;

        HashMap<Integer, Integer> stats=new HashMap<Integer, Integer>();
        stats.put(Constant.STATS_ADD_VITA,RS.getInt("vitalite"));
        stats.put(Constant.STATS_ADD_FORC,RS.getInt("force"));
        stats.put(Constant.STATS_ADD_SAGE,RS.getInt("sagesse"));
        stats.put(Constant.STATS_ADD_INTE,RS.getInt("intelligence"));
        stats.put(Constant.STATS_ADD_CHAN,RS.getInt("chance"));
        stats.put(Constant.STATS_ADD_AGIL,RS.getInt("agilite"));
        Main.world.kamas_total += RS.getLong("kamas");
        Player oldPlayer=Main.world.getPlayer((int)obj);
        player=new Player(RS.getInt("id"),RS.getString("name"),RS.getInt("groupe"),RS.getInt("sexe"),RS.getInt("class"),RS.getInt("color1"),RS.getInt("color2"),RS.getInt("color3"),RS.getLong("kamas"),RS.getInt("spellboost"),RS.getInt("capital"),RS.getInt("energy"),RS.getInt("level"),RS.getLong("xp"),RS.getInt("size"),RS.getInt("gfx"),RS.getByte("alignement"),RS.getInt("account"),stats,RS.getByte("seeFriend"),RS.getByte("seeAlign"),RS.getByte("seeSeller"),RS.getString("canaux"),RS.getShort("map"),RS.getInt("cell"),RS.getString("objets"),RS.getString("storeObjets"),RS.getString("pdvper"),RS.getString("spells"),RS.getString("savepos"),RS.getString("jobs"),RS.getInt("mountxpgive"),RS.getInt("mount"),RS.getInt("honor"),RS.getInt("deshonor"),RS.getInt("alvl"),RS.getString("zaaps"),RS.getInt("title"),RS.getInt("wife"),RS.getString("morphMode"),RS.getString("allTitle"),RS.getString("emotes"),RS.getLong("prison"),false,RS.getString("parcho"),RS.getLong("timeDeblo"),RS.getBoolean("noall"),RS.getString("deadInformation"),RS.getByte("deathCount"),RS.getLong("totalKills"),RS.getInt("tokens"),RS.getInt("apExo"),RS.getInt("mpExo"),RS.getInt("raExo"),RS.getString("rapid"),RS.getInt("song"),true,RS.getInt("ornement"),RS.getString("ornementsList"));
        if(oldPlayer!=null)
          player.setNeededEndFight(oldPlayer.needEndFight(),oldPlayer.hasMobGroup());

        player.VerifAndChangeItemPlace();
        Main.world.addPlayer(player);
        int guild=Database.getDynamics().getGuildMemberData().isPersoInGuild(RS.getInt("id"));

        if(guild>=0) {
          player.setGuildMember(Main.world.getGuild(guild).getMember(RS.getInt("id")));
        }

      }
    }
    catch(Exception e)
    {
      super.sendError("PlayerData load id",e);
    } finally
    {
      close(result);
    }
    return player;
  }
  public Player loadby_name(String obj)
  {
    Result result=null;
    Player player=null;
    try
    {
      result=getData("SELECT * FROM players WHERE name = '"+obj+"'");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        if(RS.getInt("server")!=Config.getInstance().serverId)
          continue;

        HashMap<Integer, Integer> stats=new HashMap<Integer, Integer>();
        stats.put(Constant.STATS_ADD_VITA,RS.getInt("vitalite"));
        stats.put(Constant.STATS_ADD_FORC,RS.getInt("force"));
        stats.put(Constant.STATS_ADD_SAGE,RS.getInt("sagesse"));
        stats.put(Constant.STATS_ADD_INTE,RS.getInt("intelligence"));
        stats.put(Constant.STATS_ADD_CHAN,RS.getInt("chance"));
        stats.put(Constant.STATS_ADD_AGIL,RS.getInt("agilite"));
        Main.world.kamas_total += RS.getLong("kamas");
        Player oldPlayer=Main.world.getPlayer(RS.getInt("id"));
        player=new Player(RS.getInt("id"),RS.getString("name"),RS.getInt("groupe"),RS.getInt("sexe"),RS.getInt("class"),RS.getInt("color1"),RS.getInt("color2"),RS.getInt("color3"),RS.getLong("kamas"),RS.getInt("spellboost"),RS.getInt("capital"),RS.getInt("energy"),RS.getInt("level"),RS.getLong("xp"),RS.getInt("size"),RS.getInt("gfx"),RS.getByte("alignement"),RS.getInt("account"),stats,RS.getByte("seeFriend"),RS.getByte("seeAlign"),RS.getByte("seeSeller"),RS.getString("canaux"),RS.getShort("map"),RS.getInt("cell"),RS.getString("objets"),RS.getString("storeObjets"),RS.getString("pdvper"),RS.getString("spells"),RS.getString("savepos"),RS.getString("jobs"),RS.getInt("mountxpgive"),RS.getInt("mount"),RS.getInt("honor"),RS.getInt("deshonor"),RS.getInt("alvl"),RS.getString("zaaps"),RS.getInt("title"),RS.getInt("wife"),RS.getString("morphMode"),RS.getString("allTitle"),RS.getString("emotes"),RS.getLong("prison"),false,RS.getString("parcho"),RS.getLong("timeDeblo"),RS.getBoolean("noall"),RS.getString("deadInformation"),RS.getByte("deathCount"),RS.getLong("totalKills"),RS.getInt("tokens"),RS.getInt("apExo"),RS.getInt("mpExo"),RS.getInt("raExo"),RS.getString("rapid"),RS.getInt("song"),true,RS.getInt("ornement"),RS.getString("ornementsList"));
        if(oldPlayer!=null)
          player.setNeededEndFight(oldPlayer.needEndFight(),oldPlayer.hasMobGroup());

        player.VerifAndChangeItemPlace();
        Main.world.addPlayer(player);
        int guild=Database.getDynamics().getGuildMemberData().isPersoInGuild(RS.getInt("id"));

        if(guild>=0) {
          player.setGuildMember(Main.world.getGuild(guild).getMember(RS.getInt("id")));
        }

      }
    }
    catch(Exception e)
    {
      super.sendError("PlayerData load id",e);
    } finally
    {
      close(result);
    }
    return player;
  }
 

  public void loadByAccountId(int id)
  {
    try
    {
      Account account=Main.world.getAccount(id);
      if(account!=null)
        if(account.getPlayers()!=null)
          account.getPlayers().values().stream().filter(p -> p!=null).forEach(Main.world::verifyClone);
    }
    catch(Exception e)
    {
      super.sendError("PlayerData loadByAccountId clone",e);
    }

    Result result=null;
    try
    {
      result=getData("SELECT * FROM players WHERE account = '"+id+"'");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        if(RS.getInt("server")!=Config.getInstance().serverId)
          continue;

        Player p=Main.world.getPlayer(RS.getInt("id"));
        if(p!=null)
        {
          if(p.getFight()!=null)
          {
            continue;
          }
        }
        HashMap<Integer, Integer> stats=new HashMap<Integer, Integer>();

        stats.put(Constant.STATS_ADD_VITA,RS.getInt("vitalite"));
        stats.put(Constant.STATS_ADD_FORC,RS.getInt("force"));
        stats.put(Constant.STATS_ADD_SAGE,RS.getInt("sagesse"));
        stats.put(Constant.STATS_ADD_INTE,RS.getInt("intelligence"));
        stats.put(Constant.STATS_ADD_CHAN,RS.getInt("chance"));
        stats.put(Constant.STATS_ADD_AGIL,RS.getInt("agilite"));
        Player player=new Player(RS.getInt("id"),RS.getString("name"),RS.getInt("groupe"),RS.getInt("sexe"),RS.getInt("class"),RS.getInt("color1"),RS.getInt("color2"),RS.getInt("color3"),RS.getLong("kamas"),RS.getInt("spellboost"),RS.getInt("capital"),RS.getInt("energy"),RS.getInt("level"),RS.getLong("xp"),RS.getInt("size"),RS.getInt("gfx"),RS.getByte("alignement"),RS.getInt("account"),stats,RS.getByte("seeFriend"),RS.getByte("seeAlign"),RS.getByte("seeSeller"),RS.getString("canaux"),RS.getShort("map"),RS.getInt("cell"),RS.getString("objets"),RS.getString("storeObjets"),RS.getString("pdvper"),RS.getString("spells"),RS.getString("savepos"),RS.getString("jobs"),RS.getInt("mountxpgive"),RS.getInt("mount"),RS.getInt("honor"),RS.getInt("deshonor"),RS.getInt("alvl"),RS.getString("zaaps"),RS.getInt("title"),RS.getInt("wife"),RS.getString("morphMode"),RS.getString("allTitle"),RS.getString("emotes"),RS.getLong("prison"),false,RS.getString("parcho"),RS.getLong("timeDeblo"),RS.getBoolean("noall"),RS.getString("deadInformation"),RS.getByte("deathCount"),RS.getLong("totalKills"),RS.getInt("tokens"),RS.getInt("apExo"),RS.getInt("mpExo"),RS.getInt("raExo"),RS.getString("rapid"),RS.getInt("song"),true,RS.getInt("ornement"),RS.getString("ornementsList"));
        Main.world.kamas_total += RS.getLong("kamas");
        if(p!=null)
          player.setNeededEndFight(p.needEndFight(),p.hasMobGroup());
        player.VerifAndChangeItemPlace();
        Main.world.addPlayer(player);
        int guild=Database.getDynamics().getGuildMemberData().isPersoInGuild(RS.getInt("id"));
        if(guild>=0)
          player.setGuildMember(Main.world.getGuild(guild).getMember(RS.getInt("id")));
      }
    }
    catch(Exception e)
    {
      super.sendError("PlayerData loadByAccountId",e);
    } finally
    {
      close(result);
    }
  }

  public String loadTitles(int guid)
  {
    Result result=null;
    String title="";
    try
    {
      result=getData("SELECT * FROM players WHERE id = '"+guid+"';");
      ResultSet RS=result.resultSet;
      if(RS.next())
      {
        title=RS.getString("allTitle");
      }
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData loadTitles",e);
    } finally
    {
      close(result);
    }
    return title;
  }

  public boolean add(Player perso)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("INSERT INTO players(`id`, `name`, `sexe`, `class`, `color1`, `color2`, `color3`, `kamas`, `spellboost`, `capital`, `energy`, `level`, `xp`, `size`, `gfx`, `account`, `cell`, `map`, `spells`, `objets`, `storeObjets`, `morphMode`, `server`,`rapid`, `ornement`,`ornementsList`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'','','0',?,'',?,?)");
      p.setInt(1,perso.getId());
      p.setString(2,perso.getName());
      p.setInt(3,perso.getSexe());
      p.setInt(4,perso.getClasse());
      p.setInt(5,perso.getColor1());
      p.setInt(6,perso.getColor2());
      p.setInt(7,perso.getColor3());
      p.setLong(8,perso.getKamas());
      p.setInt(9,perso.get_spellPts());
      p.setInt(10,perso.get_capital());
      p.setInt(11,perso.getEnergy());
      p.setInt(12,perso.getLevel());
      p.setLong(13,perso.getExp());
      p.setInt(14,perso.get_size());
      p.setInt(15,perso.getGfxId());
      p.setInt(16,perso.getAccID());
      p.setInt(17,perso.getCurCell().getId());
      p.setInt(18,perso.getCurMap().getId());
      p.setString(19,perso.parseSpellToDB());
      p.setInt(20,Config.getInstance().serverId);
	  p.setInt(21,perso.getOrnement());
	  p.setString(22,perso.parseOrnementsToDB());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData add",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public boolean delete(Player perso)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("DELETE FROM players WHERE id = ?");
      p.setInt(1,perso.getId());
      execute(p);

      if(!perso.getItemsIDSplitByChar(",").equals(""))
        for(String id : perso.getItemsIDSplitByChar(",").split(","))
          Database.getDynamics().getObjectData().delete(Integer.parseInt(id));
      if(!perso.getStoreItemsIDSplitByChar(",").equals(""))
        for(String id : perso.getStoreItemsIDSplitByChar(",").split(","))
          Database.getDynamics().getObjectData().delete(Integer.parseInt(id));
      if(perso.getMount()!=null)
        Database.getDynamics().getMountData().update(perso.getMount());
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData delete",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  public boolean update(Player player)
  {
    if(player==null)
    {
      super.sendError("PlayerData update",new Exception("perso is null"));
      return false;
    }

    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `players` SET `kamas`= ?, `spellboost`= ?, `capital`= ?, `energy`= ?, `level`= ?, `xp`= ?, `size` = ?, `gfx`= ?, `alignement`= ?, `honor`= ?, `deshonor`= ?, `alvl`= ?, `vitalite`= ?, `force`= ?, `sagesse`= ?, `intelligence`= ?, `chance`= ?, `agilite`= ?, `seeFriend`= ?, `seeAlign`= ?, `seeSeller`= ?, `canaux`= ?, `map`= ?, `cell`= ?, `pdvper`= ?, `spells`= ?, `objets`= ?, `storeObjets`= ?, `savepos`= ?, `zaaps`= ?, `jobs`= ?, `mountxpgive`= ?, `mount`= ?, `title`= ?, `wife`= ?, `morphMode`= ?, `allTitle` = ?, `emotes` = ?, `prison` = ?, `parcho` = ?, `timeDeblo` = ?, `noall` = ?, `deadInformation` = ?, `deathCount` = ?, `totalKills` = ?, `rapid` = ? , `song` = ? , `ornement` = ?,  `ornementsList` = ? WHERE `players`.`id` = ? LIMIT 1");
      p.setLong(1,player.getKamas());
      p.setInt(2,player.get_spellPts());
      p.setInt(3,player.get_capital());
      p.setInt(4,player.getEnergy());
      p.setInt(5,player.getLevel());
      p.setLong(6,player.getExp());
      p.setInt(7,player.get_size());
      p.setInt(8,player.getGfxId());
      p.setInt(9,player.get_align());
      p.setInt(10,player.get_honor());
      p.setInt(11,player.getDeshonor());
      p.setInt(12,player.getALvl());
      p.setInt(13,player.stats.getEffect(Constant.STATS_ADD_VITA));
      p.setInt(14,player.stats.getEffect(Constant.STATS_ADD_FORC));
      p.setInt(15,player.stats.getEffect(Constant.STATS_ADD_SAGE));
      p.setInt(16,player.stats.getEffect(Constant.STATS_ADD_INTE));
      p.setInt(17,player.stats.getEffect(Constant.STATS_ADD_CHAN));
      p.setInt(18,player.stats.getEffect(Constant.STATS_ADD_AGIL));
      p.setInt(19,(player.is_showFriendConnection() ? 1 : 0));
      p.setInt(20,(player.is_showWings() ? 1 : 0));
      p.setInt(21,(player.isShowSeller() ? 1 : 0));
      p.setString(22,player.get_canaux());
      if(player.getCurMap()!=null)
        p.setInt(23,player.getCurMap().getId());
      else
        p.setInt(23,7411);
      if(player.getCurCell()!=null)
        p.setInt(24,player.getCurCell().getId());
      else
        p.setInt(24,311);
      p.setString(25, player.get_pdvper()+";"+(System.currentTimeMillis()/1000));
      p.setString(26,player.parseSpellToDB());
      p.setString(27,player.parseObjetsToDB());
      p.setString(28,player.parseStoreItemstoBD());
      p.setString(29,player.getSavePosition());
      p.setString(30,player.parseZaaps());
      p.setString(31,player.parseJobData());
      p.setInt(32,player.getMountXpGive());
      p.setInt(33,(player.getMount()!=null ? player.getMount().getId() : -1));
      p.setInt(34,(player.get_title()));
      p.setInt(35,player.getWife());
      p.setString(36,(player.getMorphMode() ? 1 : 0)+";"+player.getMorphId());
      p.setString(37,player.getAllTitle());
      p.setString(38,player.parseEmoteToDB());
      p.setLong(39,(player.isInEnnemyFaction ? player.enteredOnEnnemyFaction : 0));
      p.setString(40,player.parseStatsParcho());
      p.setLong(41,player.getTimeTaverne());
      p.setBoolean(42,player.noall);
      p.setString(43,player.getDeathInformation());
      p.setByte(44,player.getDeathCount());
      p.setLong(45,player.getTotalKills());
      p.setString(46, player.getSetsRapidos());
      p.setInt(47,player.Song);
      p.setInt(48,player.getOrnement());
      p.setString(49,player.parseOrnementsToDB());
      p.setInt(50,player.getId());
      execute(p);
      if(player.getGuildMember()!=null)
        Database.getDynamics().getGuildMemberData().update(player);
      if(player.getMount()!=null)
        Database.getDynamics().getMountData().update(player.getMount());
      if(player.getQuestPerso()!=null&&!player.getQuestPerso().isEmpty())
          player.getQuestPerso().values().stream().filter(QP -> QP!=null).forEach(QP -> Database.getDynamics().getQuestPlayerData().update(QP,player));
      Database.getStatics().getSuccesData().update_succes(player.getId(), World.get_Succes(player.getId()).getCombat(),
    		  World.get_Succes(player.getId()).getQuete(), World.get_Succes(player.getId()).getDonjon(), 
    		  World.get_Succes(player.getId()).getArchi(), World.get_Succes(player.getId()).getPassed(),
    		  World.get_Succes(player.getId()).getPvp(),
    		  World.get_Succes(player.getId()).getBoutique(),World.get_Succes(player.getId()).getPoints(),
    		  World.get_Succes(player.getId()).Ravens, World.get_Succes(player.getId()).getKoli_lose(), 
    		  World.get_Succes(player.getId()).getKoli_wine(), World.get_Succes(player.getId()).getRecolte()
    		  , World.get_Succes(player.getId()).getCraft(), World.get_Succes(player.getId()).getFm()
    		  , World.get_Succes(player.getId()).getBrisage(), World.get_Succes(player.getId()).getChall()
    		  , World.get_Succes(player.getId()).getMsg() , World.get_Succes(player.getId()).getPvp_lose());
    }
    catch(Exception e)
    {
      super.sendError("PlayerData update",e);
      Database.launchDatabase();
    } finally
    {
      close(p);
    }
    return true;
  }
  

  public void updateInfos(Player perso)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `players` SET `name` = ?, `sexe`=?, `class`= ?, `spells`= ? WHERE `id`= ?");
      p.setString(1,perso.getName());
      p.setInt(2,perso.getSexe());
      p.setInt(3,perso.getClasse());
      p.setString(4,perso.parseSpellToDB());
      p.setInt(5,perso.getId());
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData updateInfos",e);
    } finally
    {
      close(p);
    }
  }

  public void updateGroupe(int group, String name)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `players` SET `groupe` = ? WHERE `name` = ?;");

      p.setInt(1,group);
      p.setString(2,name);
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData updateGroupe",e);
    } finally
    {
      close(p);
    }
  }

  public void updateGroupe(Player perso)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `players` SET `groupe` = ? WHERE `id`= ?");
      int id=(perso.getGroupe()!=null) ? perso.getGroupe().getId() : -1;
      p.setInt(1,id);
      p.setInt(2,perso.getId());
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData updateGroupe",e);
    } finally
    {
      close(p);
    }
  }

  public void updateTimeTaverne(Player player)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE players SET `timeDeblo` = ? WHERE `id` = ?");
      p.setLong(1,player.getTimeTaverne());
      p.setInt(2,player.getId());
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData updateTimeDeblo",e);
    } finally
    {
      close(p);
    }
  }

  public void updateTitles(int guid, String title)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE players SET `allTitle` = ? WHERE `id` = ?");
      p.setString(1,title);
      p.setInt(2,guid);
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData updateTitles",e);
    } finally
    {
      close(p);
    }
  }

  public void updateLogged(int guid, int logged)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE players SET `logged` = ? WHERE `id` = ?");
      p.setInt(1,logged);
      p.setInt(2,guid);
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData updateLogged",e);
    } finally
    {
      close(p);
    }
  }

  public void updateAllLogged(int guid, int logged)
  {
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE `players` SET `logged` = ? WHERE `account` = ?");
      p.setInt(1,logged);
      p.setInt(2,guid);
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData updateAllLogged",e);
    } finally
    {
      close(p);
    }
  }

  public boolean exist(String name)
  {
    Result result=null;
    boolean exist=false;
    try
    {
      result=getData("SELECT COUNT(*) AS exist FROM players WHERE name LIKE '"+name+"' AND server = '"+Config.getInstance().serverId+"';");
      ResultSet RS=result.resultSet;
      if(RS.next())
      {
        if(RS.getInt("exist")>0)
          exist=true;
      }
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData exist",e);
    } finally
    {
      close(result);
    }
    return exist;
  }

  //v2.7 - Replaced String += with StringBuilder
  public String haveOtherPlayer(int account)
  {
    Result result=null;
    StringBuilder servers=new StringBuilder();
    try
    {
      result=getData("SELECT server FROM players WHERE account = '"+account+"' AND NOT server = '"+Config.getInstance().serverId+"'");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        servers.append(servers.toString().isEmpty() ? RS.getInt("server") : ","+RS.getInt("server"));
      }
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData haveOtherPlayer",e);
    } finally
    {
      close(result);
    }
    return servers.toString();
  }

  public void reloadGroup(Player p)
  {
    Result result=null;
    try
    {
      result=getData("SELECT groupe FROM players WHERE id = '"+p.getId()+"'");
      ResultSet RS=result.resultSet;
      if(RS.next())
      {
        int group=RS.getInt("groupe");
        Group g=Group.getGroupeById(group);
        p.setGroupe(g,false);
      }
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData reloadGroup",e);
    } finally
    {
      close(result);
    }
  }

  public byte canRevive(Player player)
  {
    Result result=null;
    byte revive=0;
    try
    {
      result=getData("SELECT id, revive FROM players WHERE `id` = '"+player.getId()+"';");
      ResultSet RS=result.resultSet;
      while(RS.next())
        revive=RS.getByte("revive");
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData canRevive",e);
    } finally
    {
      close(result);
    }
    return revive;
  }

  public void setRevive(Player player)
  {
	  PreparedStatement p = null;
    try
    {
      p=getPreparedStatement("UPDATE players SET `revive` = 0 WHERE `id` = '"+player.getId()+"';");
      execute(p);
    }
    catch(SQLException e)
    {
      super.sendError("PlayerData setRevive",e);
    }
    finally
    {
      close(p);
    }
  }

  //v0.01 - Token Shop
  public void updateTokens(String user, int tokens)
  {
    PreparedStatement p=null;
    try
    {
      try
      {
        p=this.getPreparedStatement("UPDATE players SET `tokens` = ? WHERE `name` = ?");
        p.setInt(1,tokens);
        p.setString(2,user);
        this.execute(p);
      }
      catch(SQLException e)
      {
        e.printStackTrace();
      }
    } finally
    {
      this.close(p);
    }
  }

  //v0.01 - Token Shop
  public int loadTokens(String user)
  {
    int tokens=0;
    Result result=null;
    try
    {
      try
      {
        result=super.getData("SELECT * from players WHERE `name` = '"+user+"'");
        ResultSet RS=result.resultSet;
        if(RS.next())
          tokens=RS.getInt("tokens");
      }
      catch(SQLException e)
      {
        e.printStackTrace();
      }
    } finally
    {
      this.close(result);
    }
    return tokens;
  }



  
  public boolean shop_item(String name , int id_compte ,int prix ,int iditem) {
      PreparedStatement p = null;
      try {
          p = getPreparedStatement("INSERT INTO logs_buy_items(`name`, `id_compte`, `prix`, `iditem`) VALUES (?, ?, ?, ?)");
          p.setString(1, name);
          p.setInt(2, id_compte);
          p.setInt(3, prix);
          p.setInt(4, iditem);
          execute(p);
          return true;
      } catch (SQLException e) {
          super.sendError("Items_shop add", e);
      } finally {
          close(p);
      }
      return false;
  }
public boolean shop_parcho(String name , int id_compte ,int iditem ,int points) {
      PreparedStatement p = null;
      try {
          p = getPreparedStatement("INSERT INTO logs_use_parcho(`name`, `id_compte`, `iditem`, `points`) VALUES (?, ?, ?, ?)");
          p.setString(1, name);
          p.setInt(2, id_compte);
          p.setInt(3, iditem);
          p.setInt(4, points);
          execute(p);
          return true;
      } catch (SQLException e) {

          super.sendError("Items_shop_parcho add", e);
      } finally {
          close(p);
      }
      return false;
  }
public boolean logs_event(String staff , String player,int points) {
    PreparedStatement p = null;
    try {
        p = getPreparedStatement("INSERT INTO logs_event(`staff`, `player`, `points`) VALUES (?, ?, ?)");
        p.setString(1, staff);
        p.setString(2, player);
        p.setInt(3, points);
        execute(p);
        return true;
    } catch (SQLException e) {
        super.sendError("Items_shop_parcho add", e);
    } finally {
        close(p);
    }
    return false;
}
public boolean logs_agro(String name , String target) {
      PreparedStatement p = null;
      try {
          p = getPreparedStatement("INSERT INTO logs_agro(`perso`,`target`) VALUES (?, ?)");
          p.setString(1, name);
          p.setString(2, target);
          execute(p);
          return true;
      } catch (SQLException e) {
          super.sendError("agro_logs add", e);
      } finally {
          close(p);
      }
      return false;
  }
public boolean logs_gm(String name , String target , String ip , String compte ,int map) {
    PreparedStatement p = null;
    target = MysqlRealScapeString(target); 
    try {
        p = getPreparedStatement("INSERT INTO logs_gm(`perso`,`commande` ,`ip` ,`compte`,`map`) VALUES (?, ?, ?, ?, ?)");
        p.setString(1, name);
        p.setString(2, target);
        p.setString(3, ip);
        p.setString(4, compte);
        p.setInt(5, map);
        execute(p);
        return true;
    } catch (SQLException e) {
        super.sendError("logs gm add", e);
    } finally {
        close(p);
    }
    return false;
}
public boolean logs_craft(String name , String target) {
    PreparedStatement p = null;
    target = MysqlRealScapeString(target); 
    try {
        p = getPreparedStatement("INSERT INTO logs_craft(`perso`,`craft`) VALUES (?, ?)");
        p.setString(1, name);
        p.setString(2, target);
        execute(p);
        return true;
    } catch (SQLException e) {
        super.sendError("logs craft add", e);
    } finally {
        close(p);
    }
    return false;
}
public boolean logs_ramase(String name , String target) {
    PreparedStatement p = null;
    target = MysqlRealScapeString(target); 
    try {
        p = getPreparedStatement("INSERT INTO logs_ramase(`perso`,`objet`) VALUES (?, ?)");
        p.setString(1, name);
        p.setString(2, target);
        execute(p);
        return true;
    } catch (SQLException e) {
        super.sendError("logs ramase add", e);
    } finally {
        close(p);
    }
    return false;
}
public boolean logs_echange(String name , String target , String items) {
	items = MysqlRealScapeString(items); 
      PreparedStatement p = null;
      try {
          p = getPreparedStatement("INSERT INTO logs_echange(`perso`,`target`,`items`) VALUES (?, ?, ?)");
          p.setString(1, name);
          p.setString(2, target);
          p.setString(3, items);
          execute(p);
          return true;
      } catch (SQLException e) {
          super.sendError("echange_logs add", e);
      } finally {
          close(p);
      }
      return false;
  }
public boolean logs_buy(Player name , String buy) {
    PreparedStatement p = null;
    try {
        p = getPreparedStatement("INSERT INTO logs_boutique(`nom`,`buy`,`idcompte`) VALUES (?, ?, ?)");
        p.setString(1, name.getName());
        p.setString(2, buy);
        p.setInt(3, name.getAccID());
        execute(p);
        return true;
    } catch (SQLException e) {
        super.sendError("echange_logs add", e);
    } finally {
        close(p);
    }
    return false;
}
public void updateName(int id, String name)
{
	name = MysqlRealScapeString(name); 
  PreparedStatement p=null;
  try
  {
    try
    {
      p=this.getPreparedStatement("UPDATE players SET `name` = ? WHERE `id` = ?");
      p.setString(1,name);
      p.setInt(2,id);
      this.execute(p);
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
  } finally
  {
    this.close(p);
  }
}
public void updateColor(int id, int color1, int color2, int color3)
{
  PreparedStatement p=null;
  try
  {
    try
    {
      p=this.getPreparedStatement("UPDATE players SET `color1` = ? ,`color2` = ? ,`color3` = ? WHERE `id` = ?");
      p.setInt(1,color1);
      p.setInt(2,color2);
      p.setInt(3,color3);
      p.setInt(4,id);
      this.execute(p);
    }
    catch(SQLException e)
    {
      e.printStackTrace();
    }
  } finally
  {
    this.close(p);
  }
}
public String MysqlRealScapeString(String str){
	  String data = null;
	  if (str != null && str.length() > 0) {
	    str = str.replace("'", " ");
	    str = str.replace("\"", " ");
	    str = str.replace("`", " ");
	    data = str;
	  }
	  return data;
	}

 public void updateOrnements(Player perso) {
    PreparedStatement p = null;
    try {
        p = getPreparedStatement("UPDATE `players` SET `ornement` = ?, `ornementsList` = ? WHERE `id` = ?");
        int ornement = perso.getOrnement();
        p.setInt(1, ornement);
        p.setString(2, perso.parseOrnementsToDB());
        p.setInt(3, perso.getId());
        execute(p);
    } catch (SQLException e) {
        super.sendError("PlayerData updateOrnements", e);
    } finally {
        close(p);
    }
}
}
