package soufix.other;

import java.util.Map.Entry;

import soufix.client.Player;
import soufix.common.SocketManager;
import soufix.game.World;
import soufix.job.JobStat;
import soufix.main.Main;
import soufix.object.GameObject;

public class Succes {

    private int ID;
	private int Combat;
	private int Pvp;
	private int Pvp_lose;
	private int Quete;
	private int Donjon;
	private int Archi;
	private int Boutique;
	private int Points;
	private String Passed;
	public String Ravens;
	private int Koli_lose;
	private int Koli_wine;
	private int Recolte;
	private int Craft;
	private int Fm;
	private int Brisage;
	private int chall;
	private int msg;
	public Succes(int id, int combat,int pvp,  int quete,
    		int donjon,  int archi, int boutique,int points, String passed ,String ravens ,
    		int koli_lose , int koli_wine ,int  recolte,int  craft, int fm , int brisage , int chall , int msg, int pvp_lose) {
        this.ID = id;
        this.Combat = combat;
        this.Pvp = pvp;
        this.Quete = quete;
        this.Donjon = donjon;
        this.Archi = archi;
        this.Passed = passed;
        if(this.Passed == null)
        	this.Passed = "";
        this.Boutique = boutique;
        this.Points = points;
        this.Ravens = ravens;
        if(this.Ravens == null)
        	this.Ravens = "";
        this.Koli_lose = koli_lose;
        this.Koli_wine = koli_wine;
        this.Recolte = recolte;
        this.Craft =craft;
        this.Fm = fm;
        this.Brisage = brisage;
        this.chall = chall;
        this.msg = msg;
        this.Pvp_lose = pvp_lose;
    }
    public int getFm() {
		return Fm;
	}
	public int getBrisage() {
		return Brisage;
	}
	public int getKoli_lose() {
		return Koli_lose;
	}
    public int getChall() {
		return chall;
	}
	public int getMsg() {
		return msg;
	}
	public int getPvp_lose() {
		return Pvp_lose;
	}
	public int getKoli_wine() {
		return Koli_wine;
	}
	public int getRecolte() {
		return Recolte;
	}
	public int getID() {
		return ID;
	}
	public int getCraft() {
		return Craft;
	}
    public int getCombat() {
		return Combat;
	}
    public int getPvp() {
		return Pvp;
	}
    public int getArchi() {
		return Archi;
	}
	public String getPassed() {
		return Passed;
	}
    public int getDonjon() {
		return Donjon;
	}
    public int getQuete() {
		return Quete;
	}
    public int getBoutique() {
		return Boutique;
	}
    public int getPoints() {
		return Points;
	}
    public void fm_add (Player perso) {
    	Fm += 1;
    	Check(perso,13);
	}
    public void brisage_add (Player perso , int qua) {
    	Brisage += qua;
    	Check(perso,14);
	}
    public void craft_add (Player perso) {
    	Craft += 1;
    	Check(perso,12);
	}
    public void msg_add (Player perso) {
    	msg += 1;
    	Check(perso,17);
	}
    public void koli_lose_add (Player perso) {
    	Check(perso,15);
    	Koli_lose += 1;
	}
    public void koli_lose_set (Player perso,int numbre) {
    	Koli_lose = numbre;
	}
    public void koli_wine_add (Player perso) {
    	Koli_wine += 1;
	}
    public void koli_wine_set (Player perso,int numbre) {
    	Koli_wine = numbre;
	}
    public void recolte_add (Player perso , int qua) {
    	Recolte += qua;
    	Check(perso,11);
	}
    public void combat_add (Player perso) {
    	Combat += 1;
    	Check(perso,1);
	}
    public void donjon_add (Player perso) {
    	Donjon += 1;
    	Check(perso,3);
	}
    public void archi_add (Player perso) {
    	Archi += 1;
    	Check(perso,4);
	}
    public void pvp_add (Player perso) {
    	Pvp += 1;
    	Check(perso,8);
	}
    public void pvp_add_set (Player perso,int numbre) {
    	Pvp = numbre;
	}
    public void pvp_lose_add (Player perso) {
    	Pvp_lose += 1;
	}
    public void pvp_lose_set (Player perso,int numbre) {
    	Pvp_lose = numbre;
	}
    public void quete_add (Player perso) {
    	Quete += 1;
    	Check(perso,5);
	}
    public void boutique_add (int boutique , Player perso) {
    	Boutique += boutique;
    	Check(perso,2);
	}
    public void chall_add (Player perso, int chall) {
    	this.chall += chall;
    	Check(perso,16);
	}
    public void level (Player perso) {
    	Check(perso,9);
	}
    public void Grade (Player perso) {
    	Check(perso,7);
	}
    public void points_add (int points , Player perso) {
    	Points += points;
	}
    private void give (Player perso,String args , int type) {
    	switch(type)
        {
          case 1: // item
        	  String[] split=args.split(",");
             int template= Integer.parseInt(split[0]);
             int qua = Integer.parseInt(split[1]);
        GameObject obj=Main.world.getObjTemplate(template).createNewItem(qua,false);
        if(perso.addObjet(obj,true))
          World.addGameObject(obj,true);
        SocketManager.GAME_SEND_Ow_PACKET(perso);
        SocketManager.GAME_SEND_Im_PACKET(perso,"021;"+qua+"~"+template);
        	break; 
        	
          case 2: // kamas
                perso.addKamas(Integer.parseInt(args),false);
                Main.world.kamas_total += Integer.parseInt(args);
                SocketManager.GAME_SEND_Im_PACKET(perso,"045;"+Integer.parseInt(args));
                  SocketManager.GAME_SEND_STATS_PACKET(perso);
          	break; 
          	
          	
          case 3: // xp
        	  perso.addXp(Integer.parseInt(args));
              SocketManager.GAME_SEND_STATS_PACKET(perso);
              SocketManager.GAME_SEND_Im_PACKET(perso,"08;"+Integer.parseInt(args));
          	break; 
        	
        }
    }
    
    public void Check(Player perso , int type) {
    	for (Entry<Integer, Succes_data> Liste : World.Succes_data.entrySet()) {
                if(Liste.getValue().getType() != type || this.Passed.contains(","+Liste.getValue().getID()+","))
                	continue;
                switch(type)
                {
                  case 1: // pvm
                	  if(this.Passed.contains(","+Liste.getValue().getID()+","))
                		  continue;
                   if(this.Combat >= Liste.getValue().getArgs()) {
                	 if(Liste.getValue().getRecompense() != 0)
                	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                	  this.Points += Liste.getValue().getPoints();
                	  if(this.Passed.equals("") || this.Passed == null) {
                	  this.Passed += ","+Liste.getValue().getID()+",";
                	  }else {
                	this.Passed += Liste.getValue().getID()+",";
                	  }
                	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                   }
                	  continue;

                  case 2: // boutique
                	  if(this.Passed.contains(","+Liste.getValue().getID()+","))
                		  continue;
                	  if(this.Boutique >= Liste.getValue().getArgs()) {
                     	 if(Liste.getValue().getRecompense() != 0)
                     	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                     	  this.Points += Liste.getValue().getPoints();
                     	  if(this.Passed.equals("") || this.Passed == null) {
                     	  this.Passed += ","+Liste.getValue().getID()+",";
                     	  }else {
                     	this.Passed += Liste.getValue().getID()+",";
                     	  }
                     	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                        }
                	  continue;
                    
                  case 3: // donjon
                	  if(this.Passed.contains(","+Liste.getValue().getID()+","))
                		  continue;
                	  if(this.Donjon >= Liste.getValue().getArgs()) {
                     	 if(Liste.getValue().getRecompense() != 0)
                     	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                     	  this.Points += Liste.getValue().getPoints();
                     	  if(this.Passed.equals("") || this.Passed == null) {
                     	  this.Passed += ","+Liste.getValue().getID()+",";
                     	  }else {
                     	this.Passed += Liste.getValue().getID()+",";
                     	  }
                     	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                        }
                	  continue;
                      
                  case 4: // archi
                	  if(this.Passed.contains(","+Liste.getValue().getID()+","))
                		  continue;
                	  if(this.Archi >= Liste.getValue().getArgs()) {
                     	 if(Liste.getValue().getRecompense() != 0)
                     	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                     	  this.Points += Liste.getValue().getPoints();
                     	  if(this.Passed.equals("") || this.Passed == null) {
                     	  this.Passed += ","+Liste.getValue().getID()+",";
                     	  }else {
                     	this.Passed += Liste.getValue().getID()+",";
                     	  }
                     	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                        }
                	  continue;

                  case 5: // quete
                	  if(this.Passed.contains(","+Liste.getValue().getID()+","))
                		  continue;
                	  if(this.Quete >= Liste.getValue().getArgs()) {
                     	 if(Liste.getValue().getRecompense() != 0)
                     	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                     	  this.Points += Liste.getValue().getPoints();
                     	  if(this.Passed.equals("") || this.Passed == null) {
                     	  this.Passed += ","+Liste.getValue().getID()+",";
                     	  }else {
                     	this.Passed += Liste.getValue().getID()+",";
                     	  }
                     	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                        }
                	  continue;
                	  
                  case 7: // grade
                	  if(this.Passed.contains(","+Liste.getValue().getID()+","))
                		  continue;
               	   if(perso.get_honor() >= Liste.getValue().getArgs()) {
                     	 if(Liste.getValue().getRecompense() != 0)
                     	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                     	  this.Points += Liste.getValue().getPoints();
                     	  if(this.Passed.equals("") || this.Passed == null) {
                     	  this.Passed += ","+Liste.getValue().getID()+",";
                     	  }else {
                     	this.Passed += Liste.getValue().getID()+",";
                     	  }
                     	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                        }
               	   continue;
                   case 8: // pvp
                	   if(this.Passed.contains(","+Liste.getValue().getID()+","))
                 		  continue;
                	   if(this.Pvp >= Liste.getValue().getArgs()) {
                      	 if(Liste.getValue().getRecompense() != 0)
                      	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                      	  this.Points += Liste.getValue().getPoints();
                      	  if(this.Passed.equals("") || this.Passed == null) {
                      	  this.Passed += ","+Liste.getValue().getID()+",";
                      	  }else {
                      	this.Passed += Liste.getValue().getID()+",";
                      	  }
                      	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                         }
                	   continue;
                   case 9: // level
                	   if(this.Passed.contains(","+Liste.getValue().getID()+","))
                 		  continue;
                	   if(perso.getLevel() >= Liste.getValue().getArgs()) {
                      	 if(Liste.getValue().getRecompense() != 0)
                      	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                      	  this.Points += Liste.getValue().getPoints();
                      	  if(this.Passed.equals("") || this.Passed == null) {
                      	  this.Passed += ","+Liste.getValue().getID()+",";
                      	  }else {
                      	this.Passed += Liste.getValue().getID()+",";
                      	  }
                      	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                         }
                	   continue;
                	   
                   case 10: // metier
                	   if(this.Passed.contains(","+Liste.getValue().getID()+","))
                 		  continue;
                	   int metier = 0;
                	   for(JobStat SM : perso.getMetiers().values())
                	    {
                		   if(SM == null)
                			   continue;
                	      if(SM.get_lvl() == 100)
                	    	  metier++;
                	    }
                	   if(metier >= Liste.getValue().getArgs()) {
                      	 if(Liste.getValue().getRecompense() != 0)
                      	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                      	  this.Points += Liste.getValue().getPoints();
                      	  if(this.Passed.equals("") || this.Passed == null) {
                      	  this.Passed += ","+Liste.getValue().getID()+",";
                      	  }else {
                      	this.Passed += Liste.getValue().getID()+",";
                      	  }
                      	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                         }
                	   continue;
                	   
                   case 11: // recolte
                	   if(this.Passed.contains(","+Liste.getValue().getID()+","))
                 		  continue;
                	   if(this.Recolte >= Liste.getValue().getArgs()) {
                      	 if(Liste.getValue().getRecompense() != 0)
                      	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                      	  this.Points += Liste.getValue().getPoints();
                      	  if(this.Passed.equals("") || this.Passed == null) {
                      	  this.Passed += ","+Liste.getValue().getID()+",";
                      	  }else {
                      	this.Passed += Liste.getValue().getID()+",";
                      	  }
                      	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                         }
                	   continue;
                	   
                   case 12: // craft
                	   if(this.Passed.contains(","+Liste.getValue().getID()+","))
                 		  continue;
                	   if(this.Craft >= Liste.getValue().getArgs()) {
                      	 if(Liste.getValue().getRecompense() != 0)
                      	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                      	  this.Points += Liste.getValue().getPoints();
                      	  if(this.Passed.equals("") || this.Passed == null) {
                      	  this.Passed += ","+Liste.getValue().getID()+",";
                      	  }else {
                      	this.Passed += Liste.getValue().getID()+",";
                      	  }
                      	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                         }
                	   continue;
                   case 13: // Fm rune
                	   if(this.Passed.contains(","+Liste.getValue().getID()+","))
                 		  continue;
                	   if(this.Fm >= Liste.getValue().getArgs()) {
                      	 if(Liste.getValue().getRecompense() != 0)
                      	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                      	  this.Points += Liste.getValue().getPoints();
                      	  if(this.Passed.equals("") || this.Passed == null) {
                      	  this.Passed += ","+Liste.getValue().getID()+",";
                      	  }else {
                      	this.Passed += Liste.getValue().getID()+",";
                      	  }
                      	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                         }
                	   continue;
                   case 14: // Brisage objet
                	   if(this.Passed.contains(","+Liste.getValue().getID()+","))
                 		  continue;
                	   if(this.Brisage >= Liste.getValue().getArgs()) {
                      	 if(Liste.getValue().getRecompense() != 0)
                      	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                      	  this.Points += Liste.getValue().getPoints();
                      	  if(this.Passed.equals("") || this.Passed == null) {
                      	  this.Passed += ","+Liste.getValue().getID()+",";
                      	  }else {
                      	this.Passed += Liste.getValue().getID()+",";
                      	  }
                      	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                         }
                	   continue;
                   case 15: // koli
                	   if(this.Passed.contains(","+Liste.getValue().getID()+","))
                 		  continue;
                	   if(this.Koli_wine >= Liste.getValue().getArgs()) {
                      	 if(Liste.getValue().getRecompense() != 0)
                      	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                      	  this.Points += Liste.getValue().getPoints();
                      	  if(this.Passed.equals("") || this.Passed == null) {
                      	  this.Passed += ","+Liste.getValue().getID()+",";
                      	  }else {
                      	this.Passed += Liste.getValue().getID()+",";
                      	  }
                      	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                         }
                	   continue;
                   case 16: // chall
                	   if(this.Passed.contains(","+Liste.getValue().getID()+","))
                 		  continue;
                	   if(this.chall >= Liste.getValue().getArgs()) {
                      	 if(Liste.getValue().getRecompense() != 0)
                      	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                      	  this.Points += Liste.getValue().getPoints();
                      	  if(this.Passed.equals("") || this.Passed == null) {
                      	  this.Passed += ","+Liste.getValue().getID()+",";
                      	  }else {
                      	this.Passed += Liste.getValue().getID()+",";
                      	  }
                      	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                         }
                	   continue;
                   case 17: // msg
                	   if(this.Passed.contains(","+Liste.getValue().getID()+","))
                 		  continue;
                	   if(this.msg >= Liste.getValue().getArgs()) {
                      	 if(Liste.getValue().getRecompense() != 0)
                      	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                      	  this.Points += Liste.getValue().getPoints();
                      	  if(this.Passed.equals("") || this.Passed == null) {
                      	  this.Passed += ","+Liste.getValue().getID()+",";
                      	  }else {
                      	this.Passed += Liste.getValue().getID()+",";
                      	  }
                      	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                         }
                	   continue;
                   case 18: // songe
                	   if(this.Passed.contains(","+Liste.getValue().getID()+","))
                 		  continue;
                	   if(perso.Song >= Liste.getValue().getArgs()) {
                      	 if(Liste.getValue().getRecompense() != 0)
                      	   give(perso,Liste.getValue().getRecompense_args(),Liste.getValue().getRecompense());	 
                      	  this.Points += Liste.getValue().getPoints();
                      	  if(this.Passed.equals("") || this.Passed == null) {
                      	  this.Passed += ","+Liste.getValue().getID()+",";
                      	  }else {
                      	this.Passed += Liste.getValue().getID()+",";
                      	  }
                      	  SocketManager.GAME_SEND_MESSAGE(perso,"Succés déverrouillé ["+Liste.getValue().getName()+"]", "008000");
                         }
                	   continue;
                }
    	}	
    }
}


