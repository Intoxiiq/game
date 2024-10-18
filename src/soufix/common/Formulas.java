package soufix.common;

import soufix.area.map.GameCase;
import soufix.area.map.GameMap;
import soufix.client.Player;
import soufix.entity.Collector;
import soufix.fight.Fight;
import soufix.fight.Fighter;
import soufix.fight.spells.SpellEffect;
import soufix.guild.GuildMember;
import soufix.job.fm.Rune;
import soufix.main.Config;
import soufix.main.Constant;
//import soufix.main.Logging;
//import soufix.main.Logging;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.utility.Pair;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

public class Formulas
{

  public static SecureRandom random=new SecureRandom();

  public static int countCell(int i)
  {
    if(i>64)
      i=64;
    return 2*(i)*(i+1);
  }

  public static int getRandomValue(int i1, int i2)
  {
    if(i2<i1)
      return 0;
    return (random.nextInt((i2-i1)+1))+i1;
  }

  public static int getMinJet(String jet)
  {
    int num=0;
    try
    {
      int des=Integer.parseInt(jet.split("d")[0]);
      int add=Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
      num=des+add;
      return num;
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
      return -1;
    }
  }

  public static int getMaxJet(String jet)
  {
    int num=0;
    try
    {
      int des=Integer.parseInt(jet.split("d")[0]);
      int faces=Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
      int add=Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
      for(int a=0;a<des;a++)
      {
        num+=faces;
      }
      num+=add;
      return num;
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
      return -1;
    }
  }

  public static int getRandomJet(String jet)//1d5+6
  {
    try
    {
      int num=0;
      int des=Integer.parseInt(jet.split("d")[0]);
      int faces=Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
      int add=Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
      if(faces==0&&add==0)
      {
        num=getRandomValue(0,des);
      }
      else
      {
        for(int a=0;a<des;a++)
        {
          num+=getRandomValue(1,faces);
        }
      }
      num+=add;
      return num;
    }
    catch(NumberFormatException e)
    {
      // e.printStackTrace();
     // Logging.getInstance().write("Error","bug ici jet "+jet);
      return -1;
    }
  }

  public static int getRandomJet(String jet, Fighter target, Fighter caster)//1d5+6
  {
    try
    {
      if(target!=null)
        if(target.hasBuff(782))
          return Formulas.getMaxJet(jet);
      if(caster!=null)
        if(caster.hasBuff(781))
          return Formulas.getMinJet(jet);
      int num=0,des,faces,add;

      des=Integer.parseInt(jet.split("d")[0]);
      faces=Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
      add=Integer.parseInt(jet.split("d")[1].split("\\+")[1]);

      if(faces==0&&add==0)
      {
        num=getRandomValue(0,des);
      }
      else
      {
        for(int a=0;a<des;a++)
        {
          num+=getRandomValue(1,faces);
        }
      }
      num+=add;
      return num;
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
      return -1;
    }
  }

  public static int getMiddleJet(String jet)//1d5+6
  {
    try
    {
      int num=0;
      int des=Integer.parseInt(jet.split("d")[0]);
      int faces=Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
      int add=Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
      num+=((1+faces)/2)*des;//on calcule moyenne
      num+=add;
      return num;
    }
    catch(NumberFormatException e)
    {
      e.printStackTrace();
      return 0;
    }
  }

  public static int getTacleChance(Fighter fight, Fighter fighter)
  {
    int agiTacleur=fight.getTotalStats().getEffect(Constant.STATS_ADD_AGIL);
    int agiEnemi=fighter.getTotalStats().getEffect(Constant.STATS_ADD_AGIL);
    int div=agiTacleur+agiEnemi+50;
    if(div==0)
      div=1;
    int esquive=300*(agiTacleur+25)/div-100;
    return esquive;
  }

  public static int calculFinalHeal(Fighter healer, int roll, boolean isCac)
  {
    int intel=healer.getTotalStats().getEffect(Constant.STATS_ADD_INTE);
    int heals=healer.getTotalStats().getEffect(Constant.STATS_ADD_SOIN);
    if(intel<0)
      intel=0;
    float a=1;

    //v2.1 - bonus without weapon attack fix
    if(isCac)
      if(healer.getPersonnage()!=null&&healer.getPersonnage().getObjetByPos(1)!=null)
      {
        float i=0; //Bonus maitrise
        float j=Constant.getWeaponClassModifier(healer.getPersonnage());
        int ArmeType=healer.getPersonnage().getObjetByPos(1).getTemplate().getType();
        if((healer.getSpellValueBool(392))&&ArmeType==2)//ARC
          i=healer.getMaitriseDmg(392);
        if((healer.getSpellValueBool(390))&&ArmeType==4)//BATON
          i=healer.getMaitriseDmg(390);
        if((healer.getSpellValueBool(391))&&ArmeType==6)//EPEE
          i=healer.getMaitriseDmg(391);
        if((healer.getSpellValueBool(393))&&ArmeType==7)//MARTEAUX
          i=healer.getMaitriseDmg(393);
        if((healer.getSpellValueBool(394))&&ArmeType==3)//BAGUETTE
          i=healer.getMaitriseDmg(394);
        if((healer.getSpellValueBool(395))&&ArmeType==5)//DAGUES
          i=healer.getMaitriseDmg(395);
        if((healer.getSpellValueBool(396))&&ArmeType==8)//PELLE
          i=healer.getMaitriseDmg(396);
        if((healer.getSpellValueBool(397))&&ArmeType==19)//HACHE
          i=healer.getMaitriseDmg(397);
        a=(((100+i)/100)*(j/100));
      }

    return (int)(a*(roll*((100.00+intel)/100)+heals));
  }

  public static int calculXpWinCraft(int lvl, int numCase)
  {
    if(lvl==100)
      return 0;
    switch(numCase)
    {
      case 1:
        if(lvl<40)
          return 1;
        return 0;
      case 2:
        if(lvl<60)
          return 10;
        return 0;
      case 3:
        if(lvl>9&&lvl<80)
          return 25;
        return 0;
      case 4:
        if(lvl>19)
          return 50;
        return 0;
      case 5:
        if(lvl>39)
          return 100;
        return 0;
      case 6:
        if(lvl>59)
          return 250;
        return 0;
      case 7:
        if(lvl>79)
          return 500;
        return 0;
      case 8:
        if(lvl>99)
          return 1000;
        return 0;
    }
    return 0;
  }

  public static int calculXpWinFm(final int lvl, final float power)
  {
    if(lvl<=1)
    {
      if(power<=10)
      {
        return 10;
      }
      if(power<=50)
      {
        return 25;
      }
      return 50;
    }
    else if(lvl<=25)
    {
      if(power<=10)
      {
        return 10;
      }
      return 50;
    }
    else if(lvl<=50)
    {
      if(power<=1)
      {
        return 10;
      }
      if(power<=10)
      {
        return 25;
      }
      if(power<=50)
      {
        return 50;
      }
      return 100;
    }
    else if(lvl<=75)
    {
      if(power<=3)
      {
        return 25;
      }
      if(power<=10)
      {
        return 50;
      }
      if(power<=50)
      {
        return 100;
      }
      return 250;
    }
    else if(lvl<=100)
    {
      if(power<=3)
      {
        return 50;
      }
      if(power<=10)
      {
        return 100;
      }
      if(power<=50)
      {
        return 250;
      }
      return 500;
    }
    else if(lvl<=125)
    {
      if(power<=3)
      {
        return 100;
      }
      if(power<=10)
      {
        return 250;
      }
      if(power<=50)
      {
        return 500;
      }
      return 1000;
    }
    else if(lvl<=150)
    {
      if(power<=10)
      {
        return 250;
      }
      return 1000;
    }
    else if(lvl<=175)
    {
      if(power<=1)
      {
        return 250;
      }
      if(power<=10)
      {
        return 500;
      }
      return 1000;
    }
    else
    {
      if(power<=1)
      {
        return 500;
      }
      return 1000;
    }
  }

  public static int calculXpLooseCraft(int lvl, int numCase)
  {
    if(lvl==100)
      return 0;
    switch(numCase)
    {
      case 1:
        if(lvl<40)
          return 1;
        return 0;
      case 2:
        if(lvl<60)
          return 5;
        return 0;
      case 3:
        if(lvl>9&&lvl<80)
          return 12;
        return 0;
      case 4:
        if(lvl>19)
          return 25;
        return 0;
      case 5:
        if(lvl>39)
          return 50;
        return 0;
      case 6:
        if(lvl>59)
          return 125;
        return 0;
      case 7:
        if(lvl>79)
          return 250;
        return 0;
      case 8:
        if(lvl>99)
          return 500;
        return 0;
    }
    return 0;
  }

  public static int calculHonorWin(ArrayList<Fighter> winner, ArrayList<Fighter> looser, Fighter F)
  {
    float totalGradeWin=0;
    float totalLevelWin=0;
    float totalGradeLoose=0;
    float totalLevelLoose=0;
    boolean Prisme=false;
    int fighters=0;
    for(Fighter f : winner)
    {
      if(f.getPersonnage()==null&&f.getPrism()==null)
        continue;
      if(f.getPersonnage()!=null)
      {
        totalLevelWin+=f.getLvl();
        totalGradeWin+=f.getPersonnage().getGrade();
      }
      else
      {
        Prisme=true;
        totalLevelWin+=(f.getPrism().getLevel()*15+80);
        totalGradeWin+=f.getPrism().getLevel();
      }
    }
    for(Fighter f : looser)
    {
      if(f.getPersonnage()==null&&f.getPrism()==null)
        continue;
      if(f.getPersonnage()!=null)
      {
        totalLevelLoose+=f.getLvl();
        totalGradeLoose+=f.getPersonnage().getGrade();
        fighters++;
      }
      else
      {
        Prisme=true;
        totalLevelLoose+=(f.getPrism().getLevel()*15+80);
        totalGradeLoose+=f.getPrism().getLevel();
      }
    }
    if(!Prisme)
      if(totalLevelWin-totalLevelLoose>15*fighters)
        return 0;
    int base=(int)(100*((totalGradeLoose*totalLevelLoose)/(totalGradeWin*totalLevelWin)))/winner.size();
    if(Prisme&&base<=0)
      return 100;
    if(looser.contains(F))
      base=-base;
    if(Config.singleton.serverId == 6) {
    return (int) ((base*Config.getInstance().rateHonor)+((base*Config.getInstance().rateHonor)*0.15));	
    }
    return base*Config.getInstance().rateHonor;
  }

  public static int calculFinalDommage(Fight fight, Fighter caster, Fighter target, int statID, int jet, boolean isHeal, boolean isCaC, int spellid, GameCase castCell, GameCase targetCell, boolean isAoe, boolean isTrap)
  {
    float a=1; //Calcul
    float num=0;
    float statC=0,domC=0,perdomC=0,resfT=0,respT=0,mulT=1;
    if(!isHeal)
    {
      domC=caster.getTotalStats().getEffect(Constant.STATS_ADD_DOMA);
      perdomC=caster.getTotalStats().getEffect(Constant.STATS_ADD_PERDOM);
      if(perdomC<0)
        perdomC=0;
      if(caster.hasBuff(114)&&spellid!=0)
        mulT=caster.getBuffValue(114);
    }
    else
      domC=caster.getTotalStats().getEffect(Constant.STATS_ADD_SOIN);

    //v2.1 - punch fix
    if(caster.getPersonnage()!=null&&spellid==0&&caster.getPersonnage().getObjetByPos(1)==null)
    {
      jet=Formulas.getRandomJet("1d4+1");
      statID=0;
      isHeal=false;
      isCaC=false;
    }


    switch(statID)
    {
      case Constant.ELEMENT_NULL://Fixe
        statC=0;
        resfT=0;
        respT=0;
        respT=0;
        mulT=1;
        break;
      case Constant.ELEMENT_NEUTRE://neutre
    	  if(spellid != 446) {
        statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_FORC);
        resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_NEU);
        respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_NEU);
        if(caster.getPersonnage()!=null)//Si c'est un joueur
        {
          respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_NEU);
          resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_NEU);
        }
        //on ajoute les dom Physique
        domC+=caster.getTotalStats().getEffect(Constant.STATS_ADD_PDOM);
        //Ajout de la resist Physique
        resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_P);
    	  }
        break;
      case Constant.ELEMENT_TERRE://force
        statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_FORC);
        resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_TER);
        respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_TER);
        if(caster.getPersonnage()!=null)//Si c'est un joueur
        {
          respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_TER);
          resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_TER);
        }
        //on ajout les dom Physique
        domC+=caster.getTotalStats().getEffect(Constant.STATS_ADD_PDOM);
        //Ajout de la resist Physique
        resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_P);
        break;
      case Constant.ELEMENT_EAU://chance
        statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_CHAN);
        resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_EAU);
        respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_EAU);
        if(caster.getPersonnage()!=null)//Si c'est un joueur
        {
          respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_EAU);
          resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_EAU);
        }
        //Ajout de la resist Magique
        resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_M);
        break;
      case Constant.ELEMENT_FEU://intell
        statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_INTE);
        resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_FEU);
        respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_FEU);
        if(caster.getPersonnage()!=null)//Si c'est un joueur
        {
          respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_FEU);
          resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_FEU);
        }
        //Ajout de la resist Magique
        resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_M);
        break;
      case Constant.ELEMENT_AIR://agilitÃ¯Â¿Â½
        statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_AGIL);
        resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_AIR);
        respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_AIR);
        if(caster.getPersonnage()!=null)//Si c'est un joueur
        {
          respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_AIR);
          resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_AIR);
        }
        //Ajout de la resist Magique
        resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_M);
        break;
    }
    //Poisons
    if(spellid!=-1)
    {
     // int red=0;
      switch(spellid)
      {
        case 196: //Poisoned Wind
        {
        	statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_FORC);
            resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_NEU);
            respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_NEU);
            if(caster.getPersonnage()!=null)//Si c'est un joueur
            {
              respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_NEU);
              resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_NEU);
            }
            //on ajout les dom Physique
            domC+=caster.getTotalStats().getEffect(Constant.STATS_ADD_PDOM);
            //Ajout de la resist Physique
            resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_P);
            break;
        }
        case 219: //Poisoning
        {
        	 statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_FORC);
             resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_TER);
             respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_TER);
             if(caster.getPersonnage()!=null)//Si c'est un joueur
             {
               respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_TER);
               resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_TER);
             }
             //on ajout les dom Physique
             domC+=caster.getTotalStats().getEffect(Constant.STATS_ADD_PDOM);
             //Ajout de la resist Physique
             resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_P);
             break;
        }
        case 181: //Earthquake
        {
        	 statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_INTE);
             resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_FEU);
             respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_FEU);
             if(caster.getPersonnage()!=null)//Si c'est un joueur
             {
               respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_FEU);
               resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_FEU);
             }
             //Ajout de la resist Magique
             resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_M);
             break;
        }
        case 200: //Paralyzing Poison
        {
        	 statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_INTE);
             resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_FEU);
             respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_FEU);
             if(caster.getPersonnage()!=null)//Si c'est un joueur
             {
               respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_FEU);
               resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_FEU);
             }
             //Ajout de la resist Magique
             resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_M);
             break;
        }
        case 66: //Insidious Poison
        {
        	  statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_AGIL);
              resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_AIR);
              respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_AIR);
              if(caster.getPersonnage()!=null)//Si c'est un joueur
              {
                respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_AIR);
                resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_AIR);
              }
              //Ajout de la resist Magique
              resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_M);
              break;
        }
        case 71: //Poisoned Trap
        {
        	statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_FORC);
            resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_TER);
            respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_TER);
            if(caster.getPersonnage()!=null)//Si c'est un joueur
            {
              respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_TER);
              resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_TER);
            }
            //on ajout les dom Physique
            domC+=caster.getTotalStats().getEffect(Constant.STATS_ADD_PDOM);
            //Ajout de la resist Physique
            resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_P);
            break;
        }
        case 164: //Poisoned Arrow
        {
        	 statC=caster.getTotalStats().getEffect(Constant.STATS_ADD_FORC);
             resfT=target.getTotalStats().getEffect(Constant.STATS_ADD_R_NEU);
             respT=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_NEU);
             if(caster.getPersonnage()!=null)//Si c'est un joueur
             {
               respT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_NEU);
               resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_NEU);
             }
             //on ajout les dom Physique
             domC+=caster.getTotalStats().getEffect(Constant.STATS_ADD_PDOM);
             //Ajout de la resist Physique
             resfT+=target.getTotalStats().getEffect(Constant.STATS_ADD_RES_P);
             break;
        }
      }
    }
    //On bride la resistance a 50% si c'est un joueur
    if(target.getMob()==null&&respT>50)
      respT=50;

    if(statC<0)
      statC=0;

    //v2.1 - bonus without weapon attack fix
    if(caster.getPersonnage()!=null&&spellid==0&&caster.getPersonnage().getObjetByPos(1)!=null)
    {
      float i=0; //Bonus maitrise
      float j=Constant.getWeaponClassModifier(caster.getPersonnage());
      int ArmeType=caster.getPersonnage().getObjetByPos(1).getTemplate().getType();
      if((caster.getSpellValueBool(392))&&ArmeType==2)//ARC
        i=caster.getMaitriseDmg(392);
      if((caster.getSpellValueBool(390))&&ArmeType==4)//BATON
        i=caster.getMaitriseDmg(390);
      if((caster.getSpellValueBool(391))&&ArmeType==6)//EPEE
        i=caster.getMaitriseDmg(391);
      if((caster.getSpellValueBool(393))&&ArmeType==7)//MARTEAUX
        i=caster.getMaitriseDmg(393);
      if((caster.getSpellValueBool(394))&&ArmeType==3)//BAGUETTE
        i=caster.getMaitriseDmg(394);
      if((caster.getSpellValueBool(395))&&ArmeType==5)//DAGUES
        i=caster.getMaitriseDmg(395);
      if((caster.getSpellValueBool(396))&&ArmeType==8)//PELLE
        i=caster.getMaitriseDmg(396);
      if((caster.getSpellValueBool(397))&&ArmeType==19)//HACHE
        i=caster.getMaitriseDmg(397);
      a=(((100+i)/100)*(j/100));
    }

    float aoeMultiplier=1f;
    if(isAoe)
    {
      aoeMultiplier+=0.1f;
      //FIXME
      // mate9ch f had code
      if(caster.getFight().getMap() != null) {
    	  
      Pair<Integer, ArrayList<GameCase>> distanceList=PathFinding.getPath(caster.getFight().getMap(),(short)castCell.getId(),(short)targetCell.getId(),-1);
      if(distanceList!=null)
        aoeMultiplier-=0.1f*distanceList.getRight().size();
      
      }
      // end
      aoeMultiplier=(float)Math.round(aoeMultiplier*10)/10; //rounds to one decimal
      if(aoeMultiplier<0.7f)
        aoeMultiplier=0.7f;
    }

    float trapMultiplier=1f;
    if(isTrap)
      for(SpellEffect SS : target.getBuffsByEffectID(1029))
        trapMultiplier+=(float)SS.getValue()/100;

    num=a*mulT*aoeMultiplier*trapMultiplier*(jet*((100+statC+perdomC)/100))+domC; //dÃ¯Â¿Â½gats bruts

    //Non-buff reflect
    if(caster.getId()!=target.getId())
    {
      int renvoie=target.getTotalStatsLessBuff().getEffect(Constant.STATS_RETDOM);
      if(renvoie>0&&!isHeal)
      {
    	  if(caster.hasBuff(765))//sacrifice
          {
            if(caster.getBuff(765)!=null&&!caster.getBuff(765).getCaster().isDead())
            {
              SpellEffect.applyEffect_765B(fight,caster);
              caster=caster.getBuff(765).getCaster();
            }
          }
        if(renvoie>num)
          renvoie=(int)num;
        num-=renvoie;
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,107,"-1",target.getId()+","+renvoie);
        if(renvoie>caster.getPdv())
          renvoie=caster.getPdv();
        if(num<1)
          num=0;
        renvoie-=Formulas.getArmorResist(caster,-1);
        if(caster.hasBuff(149))
            if(caster.getBuff(149).getSpell() == 197)
            	renvoie = 0;
        if(renvoie<0)
        	renvoie=0;
        if(caster.getPdv()<=renvoie)
        {
          caster.removePdv(caster,renvoie);
          caster.removePdvMax((int)Math.floor(renvoie*(Config.getInstance().erosion+target.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-target.getTotalStats().getEffect(Constant.STATS_REM_ERO)-caster.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+caster.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
          fight.onFighterDie(caster,caster);
        }
        else
        {
          caster.removePdv(caster,renvoie);
          caster.removePdvMax((int)Math.floor(renvoie*(Config.getInstance().erosion+target.getTotalStats().getEffect(Constant.STATS_ADD_ERO)-target.getTotalStats().getEffect(Constant.STATS_REM_ERO)-caster.getTotalStats().getEffect(Constant.STATS_ADD_R_ERO)+caster.getTotalStats().getEffect(Constant.STATS_REM_R_ERO)))/100);
        }
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,100,caster.getId()+"",caster.getId()+",-"+renvoie);
      }
    }

    int reduc=(int)((num/(float)100)*respT);//Reduc %resis
    if(!isHeal)
      num-=reduc;
    int armor=getArmorResist(target,statID);

    if(!isHeal)
      num-=armor;
    if(!isHeal)
      if(armor>0)
        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight,7,105,caster.getId()+"",target.getId()+","+armor);
    if(!isHeal)
      num-=resfT; //resis fixe
    //dÃ¯Â¿Â½gats finaux
    if(num<1)
      num=0;

    //DÃ¯Â¿Â½but Formule pour les MOBs
    if(caster.getPersonnage()==null&&!caster.isCollector())
    {
      if(caster.getMob().getTemplate().getId()==116) //SacrifiÃ¯Â¿Â½ Dommage = PDV*2
      {
        return (int)((num/25)*caster.getPdvMax());
      }
      else
      {
        int niveauMob=caster.getLvl();
        double CalculCoef=((niveauMob*0.5)/100);
        int Multiplicateur=(int)Math.ceil(CalculCoef);
        if(Multiplicateur>3)
          Multiplicateur=3;
        return (int)num*Multiplicateur;
      }
    }
    return (int)num;
  }

  public static int calculZaapCost(GameMap map1, GameMap map2)
  {
    return 10*(Math.abs(map2.getX()-map1.getX())+Math.abs(map2.getY()-map1.getY())-1);
  }

  //v2.4 - Non-elemental shield change
  public static int getArmorResist(final Fighter target, final int statID)
  {
    int armor=0;
    for(final SpellEffect SE : target.getBuffsByEffectID(265))
    {
      Fighter fighter=null;
      switch(SE.getSpell())
      {
        case 1: //Glowing Armour
        {
          if(statID!=3)
            continue;
          fighter=SE.getCaster();
          break;
        }
        case 6: //Earth Armour
        {
          if(statID!=1&&statID!=0)
            continue;
          fighter=SE.getCaster();
          break;
        }
        case 14: //Wind Armour
        {
          if(statID!=4)
            continue;
          fighter=SE.getCaster();
          break;
        }
        case 18: //Aqueous Armour
        {
          if(statID!=2)
            continue;
          fighter=SE.getCaster();
          break;
        }
        default:
        {
          fighter=SE.getCaster();
          break;
        }
      }
      int damRed=0;
      int carac=0;
      int[] stats= { SE.getCaster().getTotalStats().getEffect(Constant.STATS_ADD_FORC), SE.getCaster().getTotalStats().getEffect(Constant.STATS_ADD_INTE), SE.getCaster().getTotalStats().getEffect(Constant.STATS_ADD_CHAN), SE.getCaster().getTotalStats().getEffect(Constant.STATS_ADD_AGIL) };
      int highest=0;
      for(int stat : stats)
        if(stat>highest)
          highest=stat;
      final int value=SE.getValue();
      switch(statID)
      {
        case 4:
        {
          carac=fighter.getTotalStats().getEffect(Constant.STATS_ADD_AGIL);
          break;
        }
        case 3:
        {
          carac=fighter.getTotalStats().getEffect(Constant.STATS_ADD_INTE);
          break;
        }
        case 2:
        {
          carac=fighter.getTotalStats().getEffect(Constant.STATS_ADD_CHAN);
          break;
        }
        case 0:
        case 1:
        {
          carac=fighter.getTotalStats().getEffect(Constant.STATS_ADD_FORC);
          break;
        }
      }
      damRed=value*(100+highest/2+carac/2)/100;
      armor+=damRed;
    }

    for(final SpellEffect SE : target.getBuffsByEffectID(105))
    {
      int[] stats= { SE.getCaster().getTotalStats().getEffect(Constant.STATS_ADD_FORC), SE.getCaster().getTotalStats().getEffect(Constant.STATS_ADD_INTE), SE.getCaster().getTotalStats().getEffect(Constant.STATS_ADD_CHAN), SE.getCaster().getTotalStats().getEffect(Constant.STATS_ADD_AGIL) };
      int highest=0;
      for(int stat : stats)
        if(stat>highest)
          highest=stat;

      final int value=SE.getValue();
      armor+=value*(100+highest)/100;
    }

    return armor>0 ? armor : 0;
  }

  public static int getPointsLost(char z, int value, Fighter caster, Fighter target)
  {
    float esquiveC=z=='a' ? caster.getTotalStats().getEffect(Constant.STATS_ADD_AFLEE) : caster.getTotalStats().getEffect(Constant.STATS_ADD_MFLEE);
    float esquiveT=z=='a' ? target.getTotalStats().getEffect(Constant.STATS_ADD_AFLEE) : target.getTotalStats().getEffect(Constant.STATS_ADD_MFLEE);
    float ptsMax=z=='a' ? target.getTotalStatsLessBuff().getEffect(Constant.STATS_ADD_PA) : target.getTotalStatsLessBuff().getEffect(Constant.STATS_ADD_PM);

    int retrait=0;

    for(int i=0;i<value;i++)
    {
      if(ptsMax==0&&target.getMob()!=null)
      {
        ptsMax=z=='a' ? target.getMob().getPa() : target.getMob().getPm();
      }

      float pts=z=='a' ? target.getPa() : target.getPm();
      float ptsAct=pts-retrait;

      if(esquiveT<=0)
        esquiveT=1;
      if(esquiveC<=0)
        esquiveC=1;

      float a=esquiveC/esquiveT;
      float b=(ptsAct/ptsMax);

      float pourcentage=a*b*50;
      int chance=(int)Math.ceil(pourcentage);

      if(chance<0)
        chance=0;
      if(chance>100)
        chance=100;

      int jet=getRandomValue(0,99);
      if(jet<chance)
      {
        retrait++;
      }
    }
    return retrait;
  }

  public static long getGuildXpWin(Fighter perso, AtomicReference<Long> xpWin)
  {
    if(perso.getPersonnage()==null)
      return 0;
    if(perso.getPersonnage().getGuildMember()==null)
      return 0;

    GuildMember gm=perso.getPersonnage().getGuildMember();

    double xp=(double)xpWin.get(),Lvl=perso.getLvl(),LvlGuild=perso.getPersonnage().get_guild().getLvl(),pXpGive=(double)gm.getPXpGive()/100;

    double maxP=xp*pXpGive*0.10; //Le maximum donnÃ¯Â¿Â½ Ã¯Â¿Â½ la guilde est 10% du montant prÃ¯Â¿Â½levÃ¯Â¿Â½ sur l'xp du combat
    double diff=Math.abs(Lvl-LvlGuild); //Calcul l'Ã¯Â¿Â½cart entre le niveau du personnage et le niveau de la guilde
    double toGuild;
    if(diff>=70)
    {
      toGuild=maxP*0.10; //Si l'Ã¯Â¿Â½cart entre les deux level est de 70 ou plus, l'experience donnÃ¯Â¿Â½e a la guilde est de 10% la valeur maximum de don
    }
    else if(diff>=31&&diff<=69)
    {
      toGuild=maxP-((maxP*0.10)*(Math.floor((diff+30)/10)));
    }
    else if(diff>=10&&diff<=30)
    {
      toGuild=maxP-((maxP*0.20)*(Math.floor(diff/10)));
    }
    else
    //Si la diffÃ¯Â¿Â½rence est [0,9]
    {
      toGuild=maxP;
    }
    xpWin.set((long)(xp-xp*pXpGive));
    return Math.round(toGuild);
  }

  public static long getMountXpWin(Fighter perso, AtomicReference<Long> xpWin)
  {
    if(perso.getPersonnage()==null)
      return 0;
    if(perso.getPersonnage().getMount()==null)
      return 0;

    int diff=Math.abs(perso.getLvl()-perso.getPersonnage().getMount().getLevel());

    double coeff=0;
    double xp=(double)xpWin.get();
    double pToMount=(double)perso.getPersonnage().getMountXpGive()/100+0.2;

    if(diff>=0&&diff<=9)
      coeff=0.1;
    else if(diff>=10&&diff<=19)
      coeff=0.08;
    else if(diff>=20&&diff<=29)
      coeff=0.06;
    else if(diff>=30&&diff<=39)
      coeff=0.04;
    else if(diff>=40&&diff<=49)
      coeff=0.03;
    else if(diff>=50&&diff<=59)
      coeff=0.02;
    else if(diff>=60&&diff<=69)
      coeff=0.015;
    else
      coeff=0.01;

    if(pToMount>0.2)
      xpWin.set((long)(xp-(xp*(pToMount-0.2))));

    return Math.round(xp*pToMount*coeff);
  }

  public static int getKamasWin(Fighter i, ArrayList<Fighter> winners, int maxk, int mink)
  {
    maxk++;
    int rkamas=(int)(Math.random()*(maxk-mink))+mink;
    return rkamas*Config.getInstance().rateKamas;
  }

  public static int getKamasWinPerco(int maxk, int mink)
  {
    maxk++;
    int rkamas=(int)(Math.random()*(maxk-mink))+mink;
    return rkamas*Config.getInstance().rateKamas;
  }

  public static Pair<Integer, Integer> decompPierreAme(GameObject toDecomp)
  {
    Pair<Integer, Integer> toReturn;
    String[] stats=toDecomp.parseStatsString().split("#");
    int lvlMax=Integer.parseInt(stats[3],16);
    int chance=Integer.parseInt(stats[1],16);
    toReturn=new Pair<Integer, Integer>(chance,lvlMax);

    return toReturn;
  }

  public static int totalCaptChance(int pierreChance, Player p)
  {
    int sortChance=0;

    switch(p.getSortStatBySortIfHas(413).getLevel())
    {
      case 1:
        sortChance=1;
        break;
      case 2:
        sortChance=3;
        break;
      case 3:
        sortChance=6;
        break;
      case 4:
        sortChance=10;
        break;
      case 5:
        sortChance=15;
        break;
      case 6:
        sortChance=25;
        break;
    }
    return sortChance+pierreChance;
  }

  public static int spellCost(int nb)
  {
    int total=0;
    for(int i=1;i<nb;i++)
    {
      total+=i;
    }

    return total;
  }

  public static int getLoosEnergy(int lvl, boolean isAgression, boolean isPerco)
  {

    int returned=5*lvl;
    if(isAgression)
      returned*=(7/4);
    if(isPerco)
      returned*=(3/2);
    return returned;
  }

  public static int totalAppriChance(boolean Amande, boolean Rousse, boolean Doree, Player p)
  {
    int sortChance=0;
    int ddChance=0;
    switch(p.getSortStatBySortIfHas(414).getLevel())
    {
      case 1:
        sortChance=15;
        break;
      case 2:
        sortChance=20;
        break;
      case 3:
        sortChance=25;
        break;
      case 4:
        sortChance=30;
        break;
      case 5:
        sortChance=35;
        break;
      case 6:
        sortChance=45;
        break;
    }
    if(Amande||Rousse)
      ddChance=50;
    if(Doree)
      ddChance=40;
    return sortChance+ddChance;
  }

  public static int getCouleur(boolean Amande, boolean Rousse, boolean Doree)
  {
    int Couleur=0;
    if(Amande&&!Rousse&&!Doree)
      return 20;
    if(Rousse&&!Amande&&!Doree)
      return 10;
    if(Doree&&!Amande&&!Rousse)
      return 18;

    if(Amande&&Rousse&&!Doree)
    {
      int Chance=Formulas.getRandomValue(1,2);
      if(Chance==1)
        return 20;
      if(Chance==2)
        return 10;
    }
    if(Amande&&!Rousse&&Doree)
    {
      int Chance=Formulas.getRandomValue(1,2);
      if(Chance==1)
        return 20;
      if(Chance==2)
        return 18;
    }
    if(!Amande&&Rousse&&Doree)
    {
      int Chance=Formulas.getRandomValue(1,2);
      if(Chance==1)
        return 18;
      if(Chance==2)
        return 10;
    }
    if(Amande&&Rousse&&Doree)
    {
      int Chance=Formulas.getRandomValue(1,3);
      if(Chance==1)
        return 20;
      if(Chance==2)
        return 10;
      if(Chance==3)
        return 18;
    }
    return Couleur;
  }

  public static int calculEnergieLooseForToogleMount(int pts)
  {
    if(pts<=170)
      return 4;
    if(pts>=171&&pts<180)
      return 5;
    if(pts>=180&&pts<200)
      return 6;
    if(pts>=200&&pts<210)
      return 7;
    if(pts>=210&&pts<220)
      return 8;
    if(pts>=220&&pts<230)
      return 10;
    if(pts>=230&&pts<=240)
      return 12;
    return 10;
  }

  public static int getLvlDopeuls(int lvl)
  {
    if(lvl<20)
      return 20;
    if(lvl<40)
      return 40;
    if(lvl<60)
      return 60;
    if(lvl<80)
      return 80;
    if(lvl<100)
      return 100;
    if(lvl<120)
      return 120;
    if(lvl<140)
      return 140;
    if(lvl<160)
      return 160;
    if(lvl<180)
      return 180;
    return 200;
  }

  public static int calculChanceByElement(int lvlJob, int lvlObject, int lvlPotion)
  {
    int K=1;
    if(lvlPotion==1)
      K=100;
    else if(lvlPotion==25)
      K=175;
    else if(lvlPotion==50)
      K=350;
    return lvlJob*100/(K+lvlObject);
  }

  public static float runeOnItemFormula(float z, float x)
  {
    return (float)((-z*x/Math.pow(1.082705,-z*x))+99)/100; //z limits range, x is the power input. If z=1, cutoff = 20, if z=0.5, cutoff = 40, if z=2, cutoff = 10
  }

  public static float chanceForRuneFormula(float runePower)
  {
    return (float)((1000/runePower)-9)/100;
  }

  public static float itemPowerFormula(float averageItemPower, float itemPower, float runePower, boolean negative)
  {
    if(negative)
      return (itemPower+runePower)/averageItemPower; //return reciprocal to make you not gain more chance when getting closer to 0
    return averageItemPower/(itemPower+runePower);
  }

  public static float statPowerFormula(float averageStatPower, float statPower, boolean negative)
  {
    if(negative)
    {
      if(averageStatPower==0)
        averageStatPower=0.0001f;
      return statPower/averageStatPower; //return reciprocal to make you not gain more chance when getting closer to 0
    }
    else if(statPower==0)
      statPower=0.0001f;
    return averageStatPower/statPower;
  }

  //v2.8 - Magus chance redone
  public static ArrayList<Integer> chanceFM(final float maxItemPower, final float minItemPower, final float itemPower, final float itemStatPower, final float runePower, final int maxStat, final int minStat, final int runeStat, final double coef, final boolean negative, final Rune rune)
  {
    final ArrayList<Integer> chances=new ArrayList<Integer>();

    final float averageItemPower=(float)Math.round(((float)(maxItemPower+minItemPower)/2)*100)/100; //rounded to two decimals to avoid small errors
    final float statPower=runePower/runeStat;

    float twentyPowerRule=(float)((itemPower/averageItemPower)/coef); //0.5 if half of average item power, 2 if double of average item power, when exo limit / 0.5 = * 2
    if(twentyPowerRule<0.5f)
      twentyPowerRule=0.5f;
    else if(twentyPowerRule>1f)
      twentyPowerRule=1f;
    final float currentStat=itemStatPower/statPower; //runePower/runeStat = power per stat
    float runesOnItem=currentStat/runeStat;
    if(runesOnItem<0f)
      runesOnItem=0f;
    else if(runesOnItem>20f)
      runesOnItem=20f;

    float allowedRunes=runeOnItemFormula(twentyPowerRule,runesOnItem); //modifier that makes small runes stop working when stat amount gets big, 0.99 at 0, 0.5 at 15, 0.01 at 20
    if(allowedRunes<0.01f)
      allowedRunes=0.01f;
    else if(allowedRunes>1f)
      allowedRunes=1f;

    float chanceForRune=chanceForRuneFormula(statPower); //modifier that reduces chance of working for stats with high base power, prevents ap/mp/range runes from landing easily
    if(chanceForRune<0.01f)
      chanceForRune=0.01f;
    else if(chanceForRune>1f)
      chanceForRune=1f;

    float itemPowerModifier=itemPowerFormula(averageItemPower,itemPower,runePower,negative); //modifier that reduces the chance of working for above-average items and increases it for below-average items
    if(itemPowerModifier<0.01f)
      itemPowerModifier=0.01f;
    else if(itemPowerModifier>1.5f)
      itemPowerModifier=1.5f;

    float averageStat=(float)(minStat+maxStat)/2;
    float averageStatPower=averageStat*statPower;

    if(negative)
      averageStatPower=averageStat*(-rune.getnPower()/rune.getStatsAdd());

    float statPowerModifier=statPowerFormula(averageStatPower,itemStatPower,negative);
    if(statPowerModifier<0.17f)
      statPowerModifier=0.17f;
    else if(statPower>=4.5f)
    {
      if(statPowerModifier>runePower/2)
        statPowerModifier=runePower/2;
    }
    else if(statPowerModifier>=1f)
      statPowerModifier=1f;

    float chance=itemPowerModifier*allowedRunes*chanceForRune*statPowerModifier*(float)coef*Config.getInstance().rateFm;

    int critSuccess=(int)(Math.floor(chance*100));

    if(critSuccess<1)
      critSuccess=1;
    else if(critSuccess>99)
      critSuccess=99;

    int normSuccess=0;
    if(critSuccess!=1&&critSuccess!=100)
    {
      normSuccess=(int)Math.floor(Math.pow(critSuccess,1.33f)-critSuccess);
      if(normSuccess<0)
        normSuccess=0;
      if(normSuccess+critSuccess>99)
        normSuccess=99-critSuccess;
    }
    chances.add(0,critSuccess);
    chances.add(1,normSuccess);
    return chances;
  }

  public static String convertToDate(long time)
  {
    String hexDate="#";
    DateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date=formatter.format(time);

    String[] split=date.split("\\s");

    String[] split0=split[0].split("-");
    hexDate+=Integer.toHexString(Integer.parseInt(split0[0]))+"#";
    int mois=Integer.parseInt(split0[1])-1;
    int jour=Integer.parseInt(split0[2]);
    hexDate+=Integer.toHexString(Integer.parseInt((mois<10 ? "0"+mois : mois)+""+(jour<10 ? "0"+jour : jour)))+"#";

    String[] split1=split[1].split(":");
    String heure=split1[0]+split1[1];
    hexDate+=Integer.toHexString(Integer.parseInt(heure));
    return hexDate;
  }

  public static int getXpStalk(int lvl)
  {
	  if(Config.singleton.serverId == 6)
		  return 0;
    switch(lvl)
    {
      case 50:
      case 51:
      case 52:
      case 53:
      case 54:
      case 55:
      case 56:
      case 57:
      case 58:
      case 59:
        return 65000;
      case 60:
      case 61:
      case 62:
      case 63:
      case 64:
      case 65:
      case 66:
      case 67:
      case 68:
      case 69:
        return 90000;
      case 70:
      case 71:
      case 72:
      case 73:
      case 74:
      case 75:
      case 76:
      case 77:
      case 78:
      case 79:
        return 120000;
      case 80:
      case 81:
      case 82:
      case 83:
      case 84:
      case 85:
      case 86:
      case 87:
      case 88:
      case 89:
        return 160000;
      case 90:
      case 91:
      case 92:
      case 93:
      case 94:
      case 95:
      case 96:
      case 97:
      case 98:
      case 99:
        return 210000;
      case 100:
      case 101:
      case 102:
      case 103:
      case 104:
      case 105:
      case 106:
      case 107:
      case 108:
      case 109:
        return 270000;
      case 110:
      case 111:
      case 112:
      case 113:
      case 114:
      case 115:
      case 116:
      case 117:
      case 118:
      case 119:
        return 350000;
      case 120:
      case 121:
      case 122:
      case 123:
      case 124:
      case 125:
      case 126:
      case 127:
      case 128:
      case 129:
        return 440000;
      case 130:
      case 131:
      case 132:
      case 133:
      case 134:
      case 135:
      case 136:
      case 137:
      case 138:
      case 139:
        return 540000;
      case 140:
      case 141:
      case 142:
      case 143:
      case 144:
      case 145:
      case 146:
      case 147:
      case 148:
      case 149:
        return 650000;
      case 150:
      case 151:
      case 152:
      case 153:
      case 154:
        return 760000;
      case 155:
      case 156:
      case 157:
      case 158:
      case 159:
        return 880000;
      case 160:
      case 161:
      case 162:
      case 163:
      case 164:
        return 1000000;
      case 165:
      case 166:
      case 167:
      case 168:
      case 169:
        return 1130000;
      case 170:
      case 171:
      case 172:
      case 173:
      case 174:
        return 1300000;
      case 175:
      case 176:
      case 177:
      case 178:
      case 179:
        return 1500000;
      case 180:
      case 181:
      case 182:
      case 183:
      case 184:
        return 1700000;
      case 185:
      case 186:
      case 187:
      case 188:
      case 189:
        return 2000000;
      case 190:
      case 191:
      case 192:
      case 193:
      case 194:
        return 2500000;
      case 195:
      case 196:
      case 197:
      case 198:
      case 199:
      case 200:
        return 3000000;

    }
    return 65000;
  }

  public static String translateMsg(String msg)
  {
    String alpha="a b c d e f g h i j k l n o p q r s t u v w x y z ÃƒÂ© ÃƒÂ¨ ÃƒÂ  ÃƒÂ§ & ÃƒÂ» ÃƒÂ¢ ÃƒÂª ÃƒÂ´ ÃƒÂ® ÃƒÂ¤ ÃƒÂ« ÃƒÂ¼ ÃƒÂ¯ ÃƒÂ¶";
    for(String i : alpha.split(" "))
      msg=msg.replace(i,"m");
    alpha="A B C D E F G H I J K L M N O P Q R S T U V W X Y Z Ãƒâ€¹ ÃƒÅ“ Ãƒâ€ž Ãƒï¿½ Ãƒâ€“ Ãƒâ€š ÃƒÅ  Ãƒâ€º ÃƒÅ½ Ãƒâ€é";
    for(String i : alpha.split(" "))
      msg=msg.replace(i,"H");
    return msg;
  }

  //v2.8 - droprate cleaned
  public static float dropChance(float dropPercent, float starBonus, float challDrop, float conquestBonus, float charPP, float soloBonus)
  {
    return (dropPercent+(dropPercent*((starBonus/100)+(challDrop/100)+(conquestBonus/100))))*(charPP/100)*soloBonus*Config.getInstance().rateDrop;
  }

  //v2.5 - new stats
  public static int pushDamage(int pushedCells, int lvlPusher, int pushDam, int negPushDam, int pushRes, int negPushRes)
  {
    return (8+Formulas.getRandomJet("1d8+0")*lvlPusher/50)*pushedCells+pushDam-negPushDam-pushRes+negPushRes;
  }

  //v2.0 - Redid xp formula
  public static long getXp(Object object, ArrayList<Fighter> winners, long groupXp, byte nbonus, int star, int challenge, int lvlMax, int lvlMin, int lvlLoosers, int lvlWinners, double conquestBonus)
  {
    if(lvlMin<=0)
      return 0;
    if(object instanceof Fighter)
    {
      Fighter fighter=(Fighter)object;
      if(winners.contains(fighter))
      {
        if(lvlWinners<=0)
          return 0;

        double sagesse=fighter.getLvl()*0.5+fighter.getPersonnage().getTotalStats().getEffect(Constant.STATS_ADD_SAGE),nvGrpMonster=((double)lvlMax/(double)lvlMin),bonus=1.0,
            rapport=((double)lvlLoosers/(double)lvlWinners);

        if(winners.size()==1)
          rapport=0.6;
        else if(rapport==0)
          return 0;
        else if(rapport<=1.1&&rapport>=0.9)
          rapport=1;
        else
        {
          if(rapport>1)
            rapport=1/rapport;
          if(rapport<0.01)
            rapport=0.01;
        }

        int sizeGroupe=0;
        for(Fighter f : winners)
        {
          if(f.getPersonnage()!=null&&!f.isInvocation()&&!f.isMob()&&!f.isCollector()&&!f.isDouble())
            sizeGroupe++;
        }
        if(sizeGroupe<1)
          return 0;
        if(sizeGroupe>8)
          sizeGroupe=8;

        if(nbonus>8)
          nbonus=8;
        switch(nbonus)
        {
          case 0:
            bonus=0.5;
            break;
          case 1:
            bonus=0.5;
            break;
          case 2:
            bonus=2.1;
            break;
          case 3:
            bonus=3.2;
            break;
          case 4:
            bonus=4.3;
            break;
          case 5:
            bonus=5.4;
            break;
          case 6:
            bonus=6.5;
            break;
          case 7:
            bonus=7.8;
            break;
          case 8:
            bonus=9;
            break;
        }
        if(nvGrpMonster==0)
          return 0;
        else if(nvGrpMonster<3.0)
          nvGrpMonster=1;
        else
          nvGrpMonster=1/nvGrpMonster;

        if(nvGrpMonster<0)
          nvGrpMonster=0;
        else if(nvGrpMonster>1)
          nvGrpMonster=1;
        
        float taux_xp = Config.getInstance().rateXp;
        
      
         
        long xpResult=(long)(((1+((sagesse+challenge+star+conquestBonus)/100))*(bonus+rapport)*(nvGrpMonster)*(groupXp/sizeGroupe))*taux_xp);
        if(challenge >= 100)
        {
       xpResult = xpResult + (xpResult * 1);	
        }
        if(challenge >= 200)
        {
       xpResult = xpResult + (xpResult * 2);	
        }
    
        
        return xpResult;
      }
    }
    else if(object instanceof Collector)
    {
      Collector collector=(Collector)object;

      if(Main.world.getGuild(collector.getGuildId())==null)
        return 0;

      if(lvlWinners<=0)
        return 0;

      double sagesse=Main.world.getGuild(collector.getGuildId()).getLvl()*0.5+Main.world.getGuild(collector.getGuildId()).getStats(Constant.STATS_ADD_SAGE),
          nvGrpMonster=((double)lvlMax/(double)lvlMin),bonus=1.0,rapport=((double)lvlLoosers/(double)lvlWinners);

      if(winners.size()==1)
        rapport=0.6;
      else if(rapport==0)
        return 0;
      else if(rapport<=1.1&&rapport>=0.9)
        rapport=1;
      else
      {
        if(rapport>1)
          rapport=1/rapport;
        if(rapport<0.01)
          rapport=0.01;
      }

      int sizeGroupe=0;
      for(Fighter f : winners)
      {
        if(f.getPersonnage()!=null&&!f.isInvocation()&&!f.isMob()&&!f.isCollector()&&!f.isDouble())
          sizeGroupe++;
      }
      if(sizeGroupe<1)
        return 0;
      if(sizeGroupe>8)
        sizeGroupe=8;

      if(nbonus>8)
        nbonus=8;
      switch(nbonus)
      {
        case 0:
          bonus=0.5;
          break;
        case 1:
          bonus=0.5;
          break;
        case 2:
          bonus=2.1;
          break;
        case 3:
          bonus=3.2;
          break;
        case 4:
          bonus=4.3;
          break;
        case 5:
          bonus=5.4;
          break;
        case 6:
          bonus=6.5;
          break;
        case 7:
          bonus=7.8;
          break;
        case 8:
          bonus=9;
          break;
      }
      if(nvGrpMonster==0)
        return 0;
      else if(nvGrpMonster<3.0)
        nvGrpMonster=1;
      else
        nvGrpMonster=1/nvGrpMonster;

      if(nvGrpMonster<0)
        nvGrpMonster=0;
      else if(nvGrpMonster>1)
        nvGrpMonster=1;

      long xpResult=(long)(((1+((sagesse+star+challenge+conquestBonus)/100))*(bonus+rapport)*(nvGrpMonster)*(groupXp/sizeGroupe))*Config.getInstance().rateXp);
      if(xpResult<0)
        xpResult=0;
      return xpResult;
    }
    return 0;
  }

  //v2.8 - Critical fail system
  public static boolean isCriticalFail(int spellRate, Fighter fighter)
  {
    if(spellRate==0)
      return false;
    int buffRate=fighter.getBuffValue(Constant.STATS_ADD_EC);
    // pas de echec
    int CFRate=(spellRate+200)-buffRate;
    if(CFRate<2)
      CFRate=2;
    return Formulas.getRandomValue(1,CFRate)==CFRate;
  }

  //v2.8 - Critical hit system
  public static boolean isCriticalHit(int spellCrit, int critHits, int critFails, int agi)
  {
    spellCrit=(int)Math.floor(((spellCrit-critHits+critFails)*2.9901)/Math.log(agi+12));
    if(spellCrit<2)
      spellCrit=2;
    return Formulas.getRandomValue(1,spellCrit)==spellCrit;
  }

  //v2.8 - drop bonus for less unique IPs in fight
  public static float getSoloBonus(ArrayList<Fighter> fighters)
  {
    int sameIpFighters=0;
    ArrayList<String> IPs=new ArrayList<String>();
    ArrayList<Player> players=new ArrayList<Player>();
    for(Fighter fighter : fighters)
      if(fighter.getPersonnage()!=null)
        players.add(fighter.getPersonnage());
    for(Player player : players)
    {
      boolean found=false;
      if(player.getAccount()!=null&&player.getAccount().getCurrentIp()!=null)
      {
        for(String Ip : IPs)
          if(player.getAccount().getCurrentIp().compareTo(Ip)==0)
          {
            sameIpFighters++;
            found=true;
            break;
          }
        if(!found)
          IPs.add(player.getAccount().getCurrentIp());
      }
      else //assume worst case scenario
        sameIpFighters++;
    }
    float val=1.5f-(0.166666f*sameIpFighters);
    if(val<1f)
      val=1f;
    if(val>1.5f)
      val=1.5f;
    return val;
  }

  public static boolean getRandomBoolean()
  {
    return random.nextBoolean();
  }
  public static long calcule_xp(final Player perso,final long groupXp) {  
		final double sagesse = perso.getLevel() * 0.5 + perso.getTotalStats().getEffect(124);
		double bonus = 1.0;
		return (long) ((long) ((1.0 + (sagesse) / 100.0) * (bonus + 0.5) * 0.5
				* (groupXp / 0.5)) * Config.getInstance().rateXp);
}
  public static long xp_2(Player perso,int size, long groupXp, int i, int star, int challenge, int lvlMax, int lvlMin, int lvlLoosers, int lvlWinners, double conquestBonus)
  {
    if(lvlMin<=0)
      return 0;
        if(lvlWinners<=0)
          return 0;

        double sagesse=perso.getLevel()*0.5+perso.getTotalStats().getEffect(Constant.STATS_ADD_SAGE),nvGrpMonster=((double)lvlMax/(double)lvlMin),bonus=1.0,
            rapport=((double)lvlLoosers/(double)lvlWinners);

        if(size==1)
          rapport=0.6;
        else if(rapport==0)
          return 0;
        else if(rapport<=1.1&&rapport>=0.9)
          rapport=1;
        else
        {
          if(rapport>1)
            rapport=1/rapport;
          if(rapport<0.01)
            rapport=0.01;
        }
        if(size<1)
          return 0;
        if(size>8)
        	size=8;

        if(i>8)
          i=8;
        switch(i)
        {
          case 0:
            bonus=0.5;
            break;
          case 1:
            bonus=0.5;
            break;
          case 2:
            bonus=2.1;
            break;
          case 3:
            bonus=3.2;
            break;
          case 4:
            bonus=4.3;
            break;
          case 5:
            bonus=5.4;
            break;
          case 6:
            bonus=6.5;
            break;
          case 7:
            bonus=7.8;
            break;
          case 8:
            bonus=9;
            break;
        }
        if(nvGrpMonster==0)
          return 0;
        else if(nvGrpMonster<3.0)
          nvGrpMonster=1;
        else
          nvGrpMonster=1/nvGrpMonster;

        if(nvGrpMonster<0)
          nvGrpMonster=0;
        else if(nvGrpMonster>1)
          nvGrpMonster=1;

        long xpResult=(long)(((1+((sagesse+star+challenge+conquestBonus)/100))*(bonus+rapport)*(nvGrpMonster)*(groupXp/size))*Config.getInstance().rateXp);
        if(xpResult<0)
          xpResult=0;
        return xpResult;
  }

	private final static short[] order = new short[] {

			Constant.STATS_ADD_PA, Constant.STATS_ADD_PM, Constant.STATS_ADD_PO, Constant.STATS_ADD_VITA,
			Constant.STATS_ADD_AGIL, Constant.STATS_ADD_CHAN, Constant.STATS_ADD_FORC, Constant.STATS_ADD_INTE,
			Constant.STATS_ADD_SAGE, Constant.STATS_ADD_CC, Constant.STATS_ADD_DOMA, Constant.STATS_ADD_PERDOM,
			Constant.STATS_MULTIPLY_DOMMAGE, Constant.STATS_ADD_INIT,

	};
  
public static String sortStatsByOrder(final String strStats) {

		if (strStats.isEmpty())
			return strStats;

		final Map<Byte, String> orderStats = new TreeMap<>();
		final Map<Integer, String> unknowStats = new TreeMap<>();

		for (final String stat : strStats.split(",")) {
			final int id = Integer.parseInt(stat.split("#")[0], 16);

			boolean ok = false;

			for (byte i = 0; i < order.length; ++i) {
				if (order[i] != id)
					continue;
				orderStats.put(i, stat);
				ok = true;
				break;
			}

			if (ok)
				continue;

			unknowStats.put(id, stat);

		}

		final StringBuilder theReturn = new StringBuilder();
		boolean isFirst = true;

		for (final byte number : orderStats.keySet()) {

			if (!isFirst)
				theReturn.append(",");
			isFirst = false;

			theReturn.append(orderStats.get(number));

		}

		for (final String stat : unknowStats.values()) {

			if (!isFirst)
				theReturn.append(",");
			isFirst = false;

			theReturn.append(stat);
		}

		return theReturn.toString();

	}
}
