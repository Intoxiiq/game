package soufix.database.active.data;

import com.zaxxer.hikari.HikariDataSource;

import soufix.database.active.AbstractDAO;
import soufix.entity.npc.NpcTemplate;
import soufix.main.Main;
import soufix.object.ObjectTemplate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NpcTemplateData extends AbstractDAO<NpcTemplate>
{
  public NpcTemplateData(HikariDataSource dataSource)
  {
    super(dataSource);
  }

  @Override
  public void load(Object obj)
  {
  }

  @Override
  //v2.7 - replaced String += with StringBuilder
  public boolean update(NpcTemplate npc)
  {
    StringBuilder i=new StringBuilder();
    boolean first=true;
    for(ObjectTemplate obj : npc.getAllItem())
    {
      if(first)
        i.append(obj.getId());
      else
        i.append(","+obj.getId());
      first=false;
    }
    PreparedStatement p=null;
    try
    {
      p=getPreparedStatement("UPDATE npc_template SET `ventes` = ? WHERE `id` = ?");
      p.setString(1,i.toString());
      p.setInt(2,npc.getId());
      execute(p);
      return true;
    }
    catch(SQLException e)
    {
      super.sendError("Npc_templateData update",e);
    } finally
    {
      close(p);
    }
    return false;
  }

  public void load()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM npc_template");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        int id=RS.getInt("id");
        int bonusValue=RS.getInt("bonusValue");
        int gfxID=RS.getInt("gfxID");
        int scaleX=RS.getInt("scaleX");
        int scaleY=RS.getInt("scaleY");
        int sex=RS.getInt("sex");
        int color1=RS.getInt("color1");
        int color2=RS.getInt("color2");
        int color3=RS.getInt("color3");
        String access=RS.getString("accessories");
        int extraClip=RS.getInt("extraClip");
        int customArtWork=RS.getInt("customArtWork");
        String initQId=RS.getString("initQuestion");
        String ventes=RS.getString("ventes");
        String exchanges=RS.getString("exchanges");
        Main.world.addNpcTemplate(new NpcTemplate(id,bonusValue,gfxID,scaleX,scaleY,sex,color1,color2,color3,access,extraClip,customArtWork,initQId,ventes,exchanges,RS.getString("path"),RS.getByte("informations")));
      }
    }
    catch(SQLException e)
    {
      super.sendError("Npc_templateData load",e);
      Main.stop("unknown");
    } finally
    {
      close(result);
    }
  }

  public void reload()
  {
    Result result=null;
    try
    {
      result=getData("SELECT * FROM npc_template");
      ResultSet RS=result.resultSet;
      while(RS.next())
      {
        if(Main.world.getNPCTemplate(RS.getInt("id"))==null)
        {
          int id=RS.getInt("id");
          int bonusValue=RS.getInt("bonusValue");
          int gfxID=RS.getInt("gfxID");
          int scaleX=RS.getInt("scaleX");
          int scaleY=RS.getInt("scaleY");
          int sex=RS.getInt("sex");
          int color1=RS.getInt("color1");
          int color2=RS.getInt("color2");
          int color3=RS.getInt("color3");
          String access=RS.getString("accessories");
          int extraClip=RS.getInt("extraClip");
          int customArtWork=RS.getInt("customArtWork");
          String initQId=RS.getString("initQuestion");
          String ventes=RS.getString("ventes");
          String exchanges=RS.getString("exchanges");
          Main.world.addNpcTemplate(new NpcTemplate(id,bonusValue,gfxID,scaleX,scaleY,sex,color1,color2,color3,access,extraClip,customArtWork,initQId,ventes,exchanges,RS.getString("path"),RS.getByte("informations")));
        }
        else
        {
          int id=RS.getInt("id");
          int bonusValue=RS.getInt("bonusValue");
          int gfxID=RS.getInt("gfxID");
          int scaleX=RS.getInt("scaleX");
          int scaleY=RS.getInt("scaleY");
          int sex=RS.getInt("sex");
          int color1=RS.getInt("color1");
          int color2=RS.getInt("color2");
          int color3=RS.getInt("color3");
          String access=RS.getString("accessories");
          int extraClip=RS.getInt("extraClip");
          int customArtWork=RS.getInt("customArtWork");
          String initQId=RS.getString("initQuestion");
          String ventes=RS.getString("ventes");
          String exchanges=RS.getString("exchanges");
          Main.world.getNPCTemplate(RS.getInt("id")).setInfos(id,bonusValue,gfxID,scaleX,scaleY,sex,color1,color2,color3,access,extraClip,customArtWork,initQId,ventes,exchanges,RS.getString("path"),RS.getByte("informations"));
        }

      }
    }
    catch(SQLException e)
    {
      super.sendError("Npc_templateData reload",e);
    } finally
    {
      close(result);
    }
  }
}