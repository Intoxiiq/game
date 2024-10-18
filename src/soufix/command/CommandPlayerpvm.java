package soufix.command;

import java.util.ArrayList;
import soufix.Hdv.Hdv;
import soufix.client.Player;
import soufix.client.other.Party;
import soufix.client.other.Stats;
import soufix.common.SocketManager;
import soufix.database.Database;
//import soufix.database.Database;
import soufix.game.GameClient;
import soufix.game.World;
import soufix.game.action.ExchangeAction;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;
import soufix.client.other.Ornements;

public class CommandPlayerpvm {
	private static String canal;

	static {
		CommandPlayerpvm.canal = "Casper";
	}
	
	private static final String[] emplacements = new String[] {

			// Pour jetMax et exo
			"coiffe", "cape", "ceinture", "bottes", "amulette", "anneauG", "anneauD", "cac",

			// Pour jetMax
			"familier", "bouclier", "dofus", "all" };
	
	private static final byte[] emplacementsID = new byte[] {

			// Pour jetMax et exo
			Constant.ITEM_POS_COIFFE, Constant.ITEM_POS_CAPE, Constant.ITEM_POS_CEINTURE, Constant.ITEM_POS_BOTTES,
			Constant.ITEM_POS_AMULETTE, Constant.ITEM_POS_ANNEAU1, Constant.ITEM_POS_ANNEAU2, Constant.ITEM_POS_ARME,

			// Pour jetMax
			Constant.ITEM_POS_FAMILIER, Constant.ITEM_POS_BOUCLIER, -2, -3 };

	// Pour jetMax
	private static final byte[] dofusEmplacements = new byte[] { Constant.ITEM_POS_DOFUS1, Constant.ITEM_POS_DOFUS2,
			Constant.ITEM_POS_DOFUS3, Constant.ITEM_POS_DOFUS4, Constant.ITEM_POS_DOFUS5, Constant.ITEM_POS_DOFUS6 };

	private static boolean jetMaxAItem(final Player player, final String emplacementName, final byte emplacmentID,
			final boolean sendMessage) {
		final GameObject obj = player.getObjetByPos(emplacmentID);
		if (obj == null) {
			if (sendMessage)
				player.sendMessage("Action impossible : vous ne portez pas de " + emplacementName + ".");
			return false;
		}
		obj.setStats(obj.generateNewStatsFromTemplate(obj.getTemplate().getStrTemplate(), true));
		SocketManager.GAME_SEND_UPDATE_ITEM(player, obj);
		SocketManager.GAME_SEND_STATS_PACKET(player);

		if (sendMessage)
			player.sendMessage("Votre item : <b>" + obj.getTemplate().getName()
					+ "</b> a été modifié avec les caractéristiques maximales !");

		return true;
	}

	public static boolean analyse(final Player perso, final String msg) {
		if (msg.charAt(0) != '.' || msg.charAt(1) == '.') {
			return false;
		}
		if(perso.getGameClient() == null)
		return true;	
		if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("points")) {
			perso.sendMessage("Vous avez <b>" + perso.getAccount().getPoints() + "</b> points boutique");
			return true;
		} 
		if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("all") || msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("monde")) {
			if (perso.noall) {
				SocketManager.GAME_SEND_MESSAGE(perso,
						"Votre canal " + CommandPlayerpvm.canal + " est d\u00e9sactiv\u00e9.", "C35617");
				return true;
			}
            if (perso.getGroupe() == null && System.currentTimeMillis() < perso.getGameClient().getTimeLastTaverne()) {
                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() - perso.getGameClient().getTimeLastTaverne()) / 1000+" seconde(s)");
                return true;
            }
            if(msg.substring(5).compareTo("") == 0) {
            	 perso.sendMessage("Message vide");
            	return true;
            }
            World.get_Succes(perso.getId()).msg_add(perso);
			perso.getGameClient().setTimeLastTaverne(System.currentTimeMillis()+5000);
			SocketManager.GAME_SEND_cMK_PACKET_TO_ALL_commande_all(perso,"~",perso.getId(),perso.getName(),msg.substring(5));
			
			return true;
		} 
		if (msg.length() > 5
				&& msg.substring(1, 6).equalsIgnoreCase("exopa"))
		{
   
		String choix = null;
		GameObject items = null;
		
		if(perso.getFight() != null)
		{
			SocketManager.GAME_SEND_MESSAGE(perso,"Commande inutilisable en combat.","222222");
			return true;
		}

			
		try 
		{
			choix = msg.substring(7, msg.length() - 1);
		} 
		catch (Exception e) 
		{
			SocketManager.GAME_SEND_MESSAGE(perso,"<b>Liste (Faites .exopa + l'item (é porter sur le personnage) que vous voulez exo +1PA) : <br /></b> Coiffe, Cape, AnneauDroite, AnneauGauche, Amulette, Ceinture, Bottes , Cac", "222222");
		    return true;
		}					    
	      
		if (choix.equalsIgnoreCase("Coiffe"))
	    {
			if (perso.getObjetByPos(Constant.ITEM_POS_COIFFE) != null)
				items = perso.getObjetByPos(Constant.ITEM_POS_COIFFE);
			else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
		else
		if (choix.equalsIgnoreCase("Cac"))
	    {
			if (perso.getObjetByPos(Constant.ITEM_POS_ARME) != null)
				items = perso.getObjetByPos(Constant.ITEM_POS_ARME);
			else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
	    else if (choix.equalsIgnoreCase("Cape"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_CAPE) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_CAPE);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
	    else if (choix.equalsIgnoreCase("AnneauDroite"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_ANNEAU2) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_ANNEAU2);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
	    else if (choix.equalsIgnoreCase("AnneauGauche"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_ANNEAU1) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_ANNEAU1);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
	    else if (choix.equalsIgnoreCase("Ceinture"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_CEINTURE) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_CEINTURE);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
	    else if (choix.equalsIgnoreCase("Bottes"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_BOTTES) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_BOTTES);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }   
	    else if (choix.equalsIgnoreCase("Amulette"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_AMULETTE) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_AMULETTE);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
	    else
	    {
	    	SocketManager.GAME_SEND_MESSAGE(perso,"<b>Liste (Faites .exopa + l'item (é porter sur le personnage) que vous voulez exo +1PA) : <br /></b> Coiffe, Cape, AnneauDroite, AnneauGauche, Amulette, Ceinture, Bottes", "222222");
			return true;
		}
		
		Stats stats = items.getStats();
		if(items.getPosition() != Constant.ITEM_POS_BOTTES)
		if(stats.getEffect(128) > 0)
		{
			SocketManager.GAME_SEND_MESSAGE(perso,"Ton item te donne déjé 1 PM.","222222");
			return true;
		}			
		if(stats.getEffect(111) > 0)
		{
			SocketManager.GAME_SEND_MESSAGE(perso,"Ton item te donne déjé 1 PA.", "222222");
			return true;
		}
		
		else 
		{
		        items.getStats().addOneStat(111, 1);
				SocketManager.GAME_SEND_STATS_PACKET(perso);
				SocketManager.GAME_SEND_ASK(perso.getGameClient(), perso);
				SocketManager.GAME_SEND_SPELL_LIST(perso);
				SocketManager.GAME_SEND_MESSAGE(perso,"<b>Succés !</b> Votre "+ ((GameObject) items).getTemplate().getName()+ " donne désormais +1PA en plus de ses jets habituels ! ","#01D758");
				
		}
		return true;
		}
		
		else if (msg.length() > 5
				&& msg.substring(1, 6).equalsIgnoreCase("exopm")) 
		{
		String choix = null;
		GameObject items = null;
		
		if(perso.getFight() != null)
		{
			perso.send("Commande inutilisable en combat.");
			return true;
		}
		try 
		{
			choix = msg.substring(7, msg.length() - 1);
		} 
		catch (Exception e) 
		{
			SocketManager.GAME_SEND_MESSAGE(perso,"<b>Liste (Faites .exopm + l'item (é porter sur le personnage) que vous voulez exo +1PM) : <br /></b> Coiffe, Cape, AnneauDroite, AnneauGauche, Amulette, Ceinture, Bottes , Cac", "222222");
		    return true;
		}					    
	      
		if (choix.equalsIgnoreCase("Coiffe"))
	    {
			if (perso.getObjetByPos(Constant.ITEM_POS_COIFFE) != null)
				items = perso.getObjetByPos(Constant.ITEM_POS_COIFFE);
			else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
		else
			if (choix.equalsIgnoreCase("Cac"))
		    {
				if (perso.getObjetByPos(Constant.ITEM_POS_ARME) != null)
					items = perso.getObjetByPos(Constant.ITEM_POS_ARME);
				else {
		    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
		    		return true;
		    	}
		    }
	    else if (choix.equalsIgnoreCase("Cape"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_CAPE) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_CAPE);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
	    else if (choix.equalsIgnoreCase("AnneauDroite"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_ANNEAU2) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_ANNEAU2);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
	    else if (choix.equalsIgnoreCase("AnneauGauche"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_ANNEAU1) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_ANNEAU1);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
	    else if (choix.equalsIgnoreCase("Ceinture"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_CEINTURE) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_CEINTURE);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
	    else if (choix.equalsIgnoreCase("Bottes"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_BOTTES) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_BOTTES);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }   
	    else if (choix.equalsIgnoreCase("Amulette"))
	    {
	    	if (perso.getObjetByPos(Constant.ITEM_POS_AMULETTE) != null)
	    		items = perso.getObjetByPos(Constant.ITEM_POS_AMULETTE);
	    	else {
	    		SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne portez pas l'item neccéssaire.","222222");
	    		return true;
	    	}
	    }
	    else
	    {
	    	SocketManager.GAME_SEND_MESSAGE(perso,"<b>Liste (Faites .exopa + l'item (é porter sur le personnage) que vous voulez exo +1PA) : <br /></b> Coiffe, Cape, AnneauDroite, AnneauGauche, Amulette, Ceinture, Bottes", "222222");
			return true;
		}
		
		Stats stats = items.getStats();
						
		if(items.getPosition() != Constant.ITEM_POS_AMULETTE)
		if(stats.getEffect(111) > 0)
		{
			SocketManager.GAME_SEND_MESSAGE(perso,"Ton item te donne déjé 1 PA.", "222222");
			return true;
		}
		if(stats.getEffect(128) > 0)
		{
			SocketManager.GAME_SEND_MESSAGE(perso,"Ton item te donne déjé 1 PM.","222222");
			return true;
		}
		
		else 
		{
			    items.getStats().addOneStat(128, 1);
				SocketManager.GAME_SEND_STATS_PACKET(perso);
				SocketManager.GAME_SEND_ASK(perso.getGameClient(), perso);
				SocketManager.GAME_SEND_SPELL_LIST(perso);
				SocketManager.GAME_SEND_MESSAGE(perso,"<b>Succés !</b> Votre "+ ((GameObject) items).getTemplate().getName()+ " donne désormais +1PM en plus de ses jets habituels !","#01D758");
				
		}
		return true;
		}
		//------------------------------
		else
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("fmcac")) {
				if (perso.getFight() != null) {
					return true;
				}
				GameObject obj = perso.getObjetByPos(Constant.ITEM_POS_ARME);
				if (obj == null) {
					 perso.sendMessage("Action impossible : vous ne portez pas d'arme");
					return true;
				}
				String answer;
				try {
					answer = msg.substring(7, msg.length() - 1);
				}
				catch (Exception e) {
					 perso.sendMessage("Action impossible : vous n'avez pas spécifié l'élément (neutre, air, feu, terre, eau) qui remplacera les dégats/vols de vies neutres");
					return true;
				}
				if (!answer.equalsIgnoreCase("air") && !answer.equalsIgnoreCase("terre")
						&& !answer.equalsIgnoreCase("feu") && !answer.equalsIgnoreCase("eau") && !answer.equalsIgnoreCase("neutre")) {
					 perso.sendMessage("Action impossible : l'élément " + answer
							+ " n'existe pas ! (dispo : air, feu, terre, eau)");
					return true;
				}
				for (int i = 0; i < obj.getEffects().size(); i++) {
					if (obj.getEffects().get(i).getEffectID() == 100) {
						if (answer.equalsIgnoreCase("air")) {
							obj.getEffects().get(i).setEffectID(98);
						}
						if (answer.equalsIgnoreCase("feu")) {
							obj.getEffects().get(i).setEffectID(99);
						}
						if (answer.equalsIgnoreCase("terre")) {
							obj.getEffects().get(i).setEffectID(97);
						}
						if (answer.equalsIgnoreCase("eau")) {
							obj.getEffects().get(i).setEffectID(96);
						}
					}
					
					
					if (obj.getEffects().get(i).getEffectID() == 95) {
						if (answer.equalsIgnoreCase("air")) {
							obj.getEffects().get(i).setEffectID(93);
						}
						if (answer.equalsIgnoreCase("feu")) {
							obj.getEffects().get(i).setEffectID(94);
						}
						if (answer.equalsIgnoreCase("terre")) {
							obj.getEffects().get(i).setEffectID(92);
						}
						if (answer.equalsIgnoreCase("eau")) {
							obj.getEffects().get(i).setEffectID(91);
						}
					}
					
				
				}
				 SocketManager.GAME_SEND_STATS_PACKET(perso);
				 SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(perso, obj.getGuid());
				 SocketManager.GAME_SEND_OAKO_PACKET(perso, obj);
				 SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(perso, obj);
				 SocketManager.GAME_SEND_OBJET_MOVE_PACKET(perso, obj);
				 perso.sendMessage("Votre objet : " + obj.getTemplate().getName() + " a été FM avec succés en " + answer);
				return true;
		}
		else {
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("noall")) {
				if (perso.noall) {
					perso.noall = false;
					SocketManager.GAME_SEND_MESSAGE(perso,
							"Vous avez activ\u00e9 le canal " + CommandPlayerpvm.canal + ".", "C35617");
				} else {
					perso.noall = true;
					SocketManager.GAME_SEND_MESSAGE(perso,
							"Vous avez d\u00e9sactiv\u00e9 le canal " + CommandPlayerpvm.canal + ".", "C35617");
				}
				return true;
			}
			if (msg.length() > 9
					&& msg.substring(1, 10).equalsIgnoreCase("celldeblo"))
			{//180min
				if (perso.isInPrison())
					return true;
				if (perso.cantTP())
					return true;
				if (perso.getFight() != null)
					return true;
				boolean autorised = true;
				switch (perso.getCurMap().getId())
				{
				case 8911:
				case 8916:
				case 8917:
				case 9827:
				case 8930:
				case 8932:
				case 8933:
				case 8934:
				case 8935:
				case 8936:
				case 8938:
				case 8939:
				case 9230:
				case 9646:
				case 9589:
				case 9590:
					autorised = false;
					break;
				}
				if (!autorised)
					return true;
				if (System.currentTimeMillis() < perso.getGameClient().getTimeLastTaverne()) {
	                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() - perso.getGameClient().getTimeLastTaverne()) / 1000+" seconde(s)");
	                return true;
	            }
				perso.getGameClient().setTimeLastTaverne(System.currentTimeMillis()+15000);
				perso.teleport(perso.getCurMap(), perso.getCurMap().getRandomFreeCellId());
				return true;
			}
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("staff")) {
				String message = "Liste des membres du staff connect\u00e9s :";
				boolean vide = true;
				 for(Player player : Main.world.getOnlinePlayers())
				 {
					if (player == null) {
						continue;
					}
					if (player.getGroupe() == null) {
						continue;
					}
					if (player.isInvisible()) {
						continue;
					}
					message = String.valueOf(message) + "\n- <b><a href='asfunction:onHref,ShowPlayerPopupMenu,"
							+ player.getName() + "'>[" + player.getGroupe().getName() + "] " + player.getName()
							+ "</a></b>";
					vide = false;
				}
				if (vide) {
					message = "Il n'y a aucun membre du staff connect\u00e9. Vous pouvez tout de m\u00eame allez voir sur notre Discord.";
				}
				SocketManager.GAME_SEND_MESSAGE(perso, message);
				return true;
			}
			if (msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("pass")) {
				if(perso.getAutoSkip()== true){
					perso.setAutoSkip(false);
				SocketManager.GAME_SEND_MESSAGE(perso,"Auto pass Off,","008000");
				}else{
					perso.setAutoSkip(true);
					SocketManager.GAME_SEND_MESSAGE(perso,"Auto pass On", "008000");
				}
				return true;
			}
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("krala")) {
				 SocketManager.GAME_SEND_MESSAGE(perso,"La relique du jour est <b>["+Main.relique_donjon+"]</b> pour <b>Antre du kralamour </b>.","257C38");
				return true;
			}
			if (msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("aura")) {
				if(perso.getAccount().getSubscribeRemaining() == 0L){
	               	 SocketManager.GAME_SEND_MESSAGE(perso,"Réservé au V.I.P.","008000");	 
	                return true;	 
	                }
				if(perso.couleur== true){
					perso.couleur = false;
				SocketManager.GAME_SEND_MESSAGE(perso,"Mode aura Off","008000");
				SocketManager.GAME_SEND_ALTER_GM_PACKET(perso.getCurMap(),perso);
				}else{
					perso.couleur = true;
					SocketManager.GAME_SEND_MESSAGE(perso,"Mode aura On", "008000");
					SocketManager.GAME_SEND_ALTER_GM_PACKET(perso.getCurMap(),perso);
				}
				return true;
			}
			if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("vip")) {
				if(Config.singleton.serverId == 1) {
				SocketManager.PACKET_POPUP_DEPART(perso, 
						 "\n- Vos points acquis par vote sur Serveur-Prive passent é 45 PB par vote."
						+ "\n- Avoir accés é la <b>banque</b>. gratuitement pour les VIP."
						//+ "\n- L'accélération <b>*3</b> de votre temps de craft."
						+ "\n+ <b>10.000</b> pods de plus."
						+ "\n<b>.ipdrop</b> - Permets de récupérer le drop de vos mules."
						+ "\n<b>.ipkamas</b> - Permets de récupérer les kamas de vos mules."
						+ "\n<b>.nodropitem</b> - Vous empéche de recevoir des items de monstres."
						+ "\n<b>.nodropressources</b> - Vous empéche de recevoir des ressources de monstres."
						+ "\n Vous obtenez un bonus de 25% d'expérience é chaque combat pour toute la team"
						+ "\n Vous obtenez un bonus de 25% d'expérience métier "
						+ "\n Vous obtenez un bonus de 25% de drop é chaque combat pour toute la team"
						+ "\n<b>.aura</b> - Aura exlusive multicolore"
						+ "\nAugmente la chance de reussite d'un <b>Exo</b> 1/4 vip 1/10 non vip.");
				}else {
					SocketManager.PACKET_POPUP_DEPART(perso, 
							 "\n- Vos points acquis par vote sur Serveur-Prive passent é 45 PB par vote."
							+ "\n- Avoir accés é la <b>banque</b>. gratuitement pour les VIP."
							//+ "\n- L'accélération <b>*3</b> de votre temps de craft."
							+ "\n+ <b>10.000</b> pods de plus."
							+ "\n<b>.ipdrop</b> - Permets de récupérer le drop de vos mules."
							+ "\n<b>.ipkamas</b> - Permets de récupérer les kamas de vos mules."
							+ "\n<b>.nodropitem</b> - Vous empéche de recevoir des items de monstres."
							+ "\n<b>.nodropressources</b> - Vous empéche de recevoir des ressources de monstres."
							+ "\n Vous obtenez un bonus de 25% d'expérience é chaque combat pour toute la team"
							+ "\n Vous obtenez un bonus de 25% d'expérience métier "
							+ "\n Vous obtenez un bonus de 25% de drop é chaque combat pour toute la team"
							+ "\n<b>.aura</b> - Aura exlusive multicolore"
							+ "\nAugmente la chance de reussite d'un <b>Exo</b> 1/10 vip 1/20 non vip.");	
				}
				return true;
			}
			if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("maitre")) {
			
				if (System.currentTimeMillis() < perso.getGameClient().timeLasttpcommande) {
	                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				 perso.getGameClient().timeLasttpcommande =(System.currentTimeMillis()+700);
				    if(perso.getParty()!=null)
				    {
				    	if(perso.getParty().getMaster() != null)
				    	if(perso.getParty().getMaster().getId() == perso.getId()) {
				    		 perso.getParty().setMaster(null);
				    		 perso.getParty().clear_groupe();
				    		 perso.setOne_windows(false);
				    		 perso.sendMessage("Mode Maitre off.");
				    		 return true;
				    	}
				      perso.sendMessage("Vos etes déjé dans un groupe.");
				      return true;
				    }
				    int nbr = 0;
				    for (final Player z : perso.getCurMap().getPlayers()) {
				    	if(z == null)
				    		continue;
    					if (z.getGameClient() == null) {
    						continue;
    					}
    					if(z.getAccount().getId_web() != perso.getAccount().getId_web()) 
    					if(!z.getAccount().getCurrentIp().equals(perso.getAccount().getCurrentIp()))
    					continue;
    					if(perso.getId() == z.getId())continue;
    					if(z.getParty() != null)
        					continue;
    					if (perso.getParty() != null && nbr == 8)
    						continue;
    					nbr++;
    					perso.getGameClient().inviteParty("zz"+z.getName(),true);
    					z.getGameClient().acceptInvitation();
    					SocketManager.GAME_SEND_PR_PACKET(z);
    					SocketManager.GAME_SEND_MESSAGE(z,"Vous suivez maintenant "+perso.getName()+"");
    				}
				    if(nbr == 0 || perso.getParty() == null) {
				    	SocketManager.GAME_SEND_MESSAGE(perso,"Aucune mule n'est sur la map");	
				    	return true;
				    }
				    final Party party=perso.getParty();
				    party.setMaster(perso);
				    party.moveAllPlayersToMaster(null);
				    SocketManager.GAME_SEND_MESSAGE(perso,"Vous étes désormais le maitre de votre groupe");
				    return true;
			}
			if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("banque")) {
				if (perso.getFight() != null) {
					return true;
				}
				if(perso.getGameClient().show_cell_BANK) {
					GameClient.leaveExchange(perso);
					perso.getGameClient().show_cell_BANK = false;
				}
				else {
				GameClient.leaveExchange(perso);
				final int cost = perso.getBankCost();
				if(perso.getAccount().getSubscribeRemaining() == 0L)
				if (cost > 0) {
					final long playerKamas = perso.getKamas();
					final long kamasRemaining = playerKamas - cost;
					final long bankKamas = perso.getAccount().getBankKamas();
					long totalKamas = bankKamas + playerKamas;
					if (kamasRemaining < 0L) {
						if (bankKamas >= cost) {
							Main.world.kamas_total -= cost;
							perso.setBankKamas(bankKamas - cost);
						} else {
							if (totalKamas < cost) {
								SocketManager.GAME_SEND_MESSAGE_SERVER(perso, "10|" + cost);
								return true;
							}
							Main.world.kamas_total -= cost;
							perso.setKamas(0L);
							perso.setBankKamas(totalKamas - cost);
							SocketManager.GAME_SEND_STATS_PACKET(perso);
							SocketManager.GAME_SEND_Im_PACKET(perso, "020;" + playerKamas);
						}
					} else {
						Main.world.kamas_total -= cost;
						perso.setKamas(kamasRemaining);
						SocketManager.GAME_SEND_STATS_PACKET(perso);
						SocketManager.GAME_SEND_Im_PACKET(perso, "020;" + cost);
					}
				}
				SocketManager.GAME_SEND_ECK_PACKET(perso.getGameClient(), 5, "");
				SocketManager.GAME_SEND_EL_BANK_PACKET(perso);
				perso.setAway(true);
				perso.setExchangeAction(new ExchangeAction<>(ExchangeAction.IN_BANK,0));
				perso.getGameClient().show_cell_BANK = true;
				}
				return true;
				
			}
			if (msg.length() > 8 && msg.substring(1, 9).equalsIgnoreCase("ornement")) {StringBuilder ornements = new StringBuilder();
			for (Ornements o : World.getOrnements().values()) {

				if(perso.getOrnementsList().contains(o.getId())) {
					if (ornements.length() > 0) {
						ornements.append(";");
					}
					ornements.append(o.getId() + ","+ o.getName() +",T");

				} else if (!o.isCanbuy()) {
					if (ornements.length() > 0) {
						ornements.append(";");
					}
					ornements.append(o.getId() + ","+o.getName()+"," + o.getPrice());
				}
			}


			perso.send("wl"+ornements.toString());
			return true;
		}}
			if (msg.length() > 8 && msg.substring(1, 9).equalsIgnoreCase("boutique")) {
				GameClient.leaveExchange(perso);
				soufix.main.Boutique.open(perso);
				return true;
			}
			if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("window")) {
				if(perso.getFight()!=null) {
					SocketManager.GAME_SEND_MESSAGE(perso,"Error Combat", "008000");
					return true;
				}
				if(perso.getParty() == null || perso.getParty().getMaster() == null ||
						perso.getParty().getMaster().getId() != perso.getId())
				{
				SocketManager.GAME_SEND_MESSAGE(perso,"Mets toi Maétre avant");	
				return true;
				}
				if(perso.isOne_windows())
				{
					 SocketManager.GAME_SEND_MESSAGE(perso,"One Window Off");
				 perso.setOne_windows(false);
				}else
				{
					 SocketManager.GAME_SEND_MESSAGE(perso,"One Window On");
				perso.setOne_windows(true);	
				}
				return true;
			} 
			if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("hdv")) {
				if(perso.getFight()!=null)
				SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne pouvez pas ouvrir le marché pendant le combat.");
		        else
		        {
		        	GameClient.leaveExchange(perso);
		          Hdv hdv=Main.world.getWorldMarket();
		          if(hdv!=null)
		          {
		            String info="1,10,100;"+hdv.getStrCategory()+";"+hdv.parseTaxe()+";"+hdv.getLvlMax()+";"+hdv.getMaxAccountItem()+";-1;"+hdv.getSellTime();
		            SocketManager.GAME_SEND_ECK_PACKET(perso,11,info);
		            ExchangeAction<Integer> exchangeAction=new ExchangeAction<>(ExchangeAction.AUCTION_HOUSE_BUYING,-perso.getCurMap().getId()); //Récupére l'ID de la map et rend cette valeur négative
		            perso.setExchangeAction(exchangeAction);
		            perso.setWorldMarket(true);
		          }
		        }
				return true;
			} 
			if (msg.length() > 8 && msg.substring(1, 9).equalsIgnoreCase("movemobs")) {
				perso.getCurMap().onMapMonsterDeplacement();
			      perso.sendMessage("Vous avez deplace un groupe de monstres.");
				return true;
			} 
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("debug")) {
				GameClient.leaveExchange(perso);
				if(perso.getFight() != null) {
					perso.sendMessage("Combat !!!!.");
				return true;	
				}
				if (System.currentTimeMillis() <  perso.getAccount().timeLastdebug) {
					 perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getAccount().timeLastdebug) / 1000+" seconde(s)");
	                return true;
	            }
				perso.getAccount().timeLastdebug =(System.currentTimeMillis()+60000);
				
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
			    //  Main.world.ReassignAccountToChar(perso.getAccount().getId());;
			      if(perso.get_guild() != null)
				      perso.get_guild().getMember(perso.getId()).setPlayer(Main.world.getPlayer(perso.getId()));
				return true;
			}
			/*if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("debug")) {
				if (perso.getFight() != null){
					SocketManager.GAME_SEND_MESSAGE(perso,"Error Combat", "008000");
					return true;
				}
				perso.getCurCell().removePlayer(perso);
			      if(perso.getGameClient()!=null)
			        perso.getGameClient().disconnect();
			      perso.setOnline(false);
			      perso.resetVars();
			      Database.getStatics().getPlayerData().update(perso);
			      SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(),perso.getId());
			      Main.world.unloadPerso(perso);
			      Database.getStatics().getPlayerData().load(perso.getId());
			      Main.world.ReassignAccountToChar(perso.getAccount().getId());
				return true;
			} */
			if (msg.length() > 8 && msg.substring(1, 9).equalsIgnoreCase("tpgroupe")) {
				if(perso.getFight() != null)return true;
				if (System.currentTimeMillis() <  perso.getGameClient().timeLasttpcommande) {
					 perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				 perso.getGameClient().timeLasttpcommande =(System.currentTimeMillis()+1000);
				for (final String s : Main.world.maps_dj) {
					if (Integer.parseInt(s) == perso.getCurMap().getId()) {
						SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone DJ", "008000");	
						return true;
					}
				}
				if (perso.isInPrison()) {
					SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Prison", "008000");
					return true;
				}
				if (perso.cantTP()) {
					SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone DJ", "008000");
					return true;
				}
				if (perso.getFight() != null){
					SocketManager.GAME_SEND_MESSAGE(perso,"Error Combat", "008000");
					return true;
				}
				if(perso.getCurMap().getId() == 9877 ){
					SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone DJ", "008000");	
					return true;
				}
				if (perso.getCurMap().getSong() == 1)
				{
					SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone Songe", "008000");	
					return true;
				}
				boolean autorised = true;
				switch (perso.getCurMap().getId())
				{
				case 9646:
				case 8905:
				case 8911:
				case 8916:
				case 8917:
				case 9827:
				case 8930:
				case 8932:
				case 8933:
				case 8934:
				case 8935:
				case 8936:
				case 8938:
				case 8939:
				case 9230:
				case 9589:
				case 9590:
					autorised = false;
					break;
				}
				if (!autorised)
				{
					SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone DJ", "008000");	
					return true;
				}
				if(perso.getParty() != null){
					for (final Player z :perso.getParty().getPlayers()) {
						if (z.getGameClient() == null) {
							continue;
						}
						if(z.getFight() != null)continue;
						if(z.getId() == perso.getId())
	    					continue;
						z.teleport(perso.getCurMap(), perso.getCurCell().getId());
					}
				}else{
					SocketManager.GAME_SEND_MESSAGE(perso,"Mets toi en groupe avant", "008000");	
				}
				return true;
			}
			if (msg.length() > 2 && msg.substring(1, 3).equalsIgnoreCase("tp")) {
				if(perso.getFight() != null)return true;
				if (System.currentTimeMillis() <  perso.getGameClient().timeLasttpcommande) {
					 perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				 perso.getGameClient().timeLasttpcommande =(System.currentTimeMillis()+1000);
				for (final String s : Main.world.maps_dj) {
					if (Integer.parseInt(s) == perso.getCurMap().getId()) {
						SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone DJ", "008000");	
						return true;
					}
				}
				if (perso.isInPrison()) {
					SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Prison", "008000");
					return true;
				}
				if (perso.cantTP()) {
					SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone DJ", "008000");
					return true;
				}
				if (perso.getFight() != null){
					SocketManager.GAME_SEND_MESSAGE(perso,"Error Combat", "008000");
					return true;
				}
				if(perso.getCurMap().getId() == 9877 ){
					SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone DJ", "008000");	
					return true;
				}
				if (perso.getCurMap().getSong() == 1)
				{
					SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone Songe", "008000");	
					return true;
				}
				boolean autorised = true;
				switch (perso.getCurMap().getId())
				{
				case 9646:
				case 8911:
				case 8916:
				case 8917:
				case 9827:
				case 8930:
				case 8932:
				case 8933:
				case 8934:
				case 8935:
				case 8936:
				case 8938:
				case 8939:
				case 9230:
				case 9589:
				case 9590:
					autorised = false;
					break;
				}
				if (!autorised){
					SocketManager.GAME_SEND_MESSAGE(perso,"Error TP Zone DJ", "008000");	
					return true;
				}
				if(perso.getParty() != null && perso.getParty().isChief(perso.getId())){
					for (final Player z :perso.getParty().getPlayers()) {
						if (z.getGameClient() == null) {
							continue;
						}
						if(z.getFight() != null)continue;
						if(z.getAccount().getId_web() != perso.getAccount().getId_web()) 
	    					if(!z.getAccount().getCurrentIp().equals(perso.getAccount().getCurrentIp()))
	    					continue;
						if(z.getId() == perso.getId())
	    					continue;
						z.teleport(perso.getCurMap(), perso.getCurCell().getId());
					}
				}else{
					SocketManager.GAME_SEND_MESSAGE(perso,"Mets toi Maétre avant", "008000");	
				}
				return true;
			}
			if (msg.length() > 16 && msg.substring(1, 17).equalsIgnoreCase("nodropressources")) {
				if(perso.getAccount().getSubscribeRemaining() == 0L){
	               	 SocketManager.GAME_SEND_MESSAGE(perso,"Réservé au V.I.P.","008000");	 
	                return true;	 
	                }
				if(perso.isCanDrop_ressources())
			    {
					perso.setCanDrop(true);
				      perso.setCanDrop_items(true);
				      perso.setCanDrop_ressources(false);
			      SocketManager.GAME_SEND_MESSAGE(perso,"No drop ressources on.");
			    }
			    else
			    {
			    	perso.setCanDrop(true);
				      perso.setCanDrop_items(true);
				      perso.setCanDrop_ressources(true);
			      SocketManager.GAME_SEND_MESSAGE(perso,"No drop ressources off.");
			    }
			    return true;
				
			}
			if (msg.length() > 10 && msg.substring(1, 11).equalsIgnoreCase("nodropitem")) {
				if(perso.getAccount().getSubscribeRemaining() == 0L){
	               	 SocketManager.GAME_SEND_MESSAGE(perso,"Réservé au V.I.P.","008000");	 
	                return true;	 
	                }
				if(perso.isCanDrop_items())
			    {
					perso.setCanDrop(true);
				      perso.setCanDrop_items(false);
				      perso.setCanDrop_ressources(true);
			      SocketManager.GAME_SEND_MESSAGE(perso,"No drop items on.");
			    }
			    else
			    {
			    	perso.setCanDrop(true);
				      perso.setCanDrop_items(true);
				      perso.setCanDrop_ressources(true);
			      SocketManager.GAME_SEND_MESSAGE(perso,"No drop items off.");
			    }
			    return true;
				
			}
			if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("nodrop")) {
				if(perso.getCanDrop())
			    {
			      perso.setCanDrop(false);
			      perso.setCanDrop_items(true);
			      perso.setCanDrop_ressources(true);
			      SocketManager.GAME_SEND_MESSAGE(perso,"No drop on.");
			    }
			    else
			    {
			      perso.setCanDrop(true);
			      perso.setCanDrop_items(true);
			      perso.setCanDrop_ressources(true);
			      SocketManager.GAME_SEND_MESSAGE(perso,"No drop off.");
			    }
			    return true;
				
			}
			else
			if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("jetmax")) {
				if (perso.getFight() != null) {
					perso.sendMessage("Action impossible : vous ne devez pas être en combat.");
					return false;
				}

				final String[] split = msg.substring(0, msg.length() - 1).trim().split(" ");

				if (split.length < 2) {
					perso.sendMessage(
							"La commande doit être : .[commandeName] [coiffe, cape, ceinture, bottes, amulette, anneauG, anneauD, cac, familier, dofus, bouclier, all]");
					return false;
				}

				byte emplacementID = -1;

				for (byte i = 0; i < emplacements.length; ++i) {
					if (!emplacements[i].equalsIgnoreCase(split[1]))
						continue;
					emplacementID = emplacementsID[i];
					break;
				}

				if (emplacementID == -1) {
					perso.sendMessage(
							"Seul ces objets peuvent être indiqué : [coiffe, cape, ceinture, bottes, amulette, anneauG, anneauD, cac, familier, dofus, bouclier, all]");
					return false;
				}

				if (emplacementID > 0)
					return jetMaxAItem(perso, split[1], emplacementID, true);

				for (byte i = 0; i < dofusEmplacements.length; ++i)
					jetMaxAItem(perso, split[1], dofusEmplacements[i], false);

				if (emplacementID != -3) {
					perso.sendMessage("Toutes les dofus équipé ont été modifié avec les caractéristiques maximales !");
					return true;
				}

				for (byte i = 0; i < emplacementsID.length - 2; ++i)
					jetMaxAItem(perso, split[1], emplacementsID[i], false);

				perso.sendMessage("Tout les items équipé ont été modifié avec les caractéristiques maximales !");
				return true;
			}

			if (msg.length() > 7 && msg.substring(1, 8).equalsIgnoreCase("ipkamas")) {
				if(perso.getParty() == null || perso.getParty().getMaster() == null ||
						perso.getParty().getMaster().getId() != perso.getId())
				{
				SocketManager.GAME_SEND_MESSAGE(perso,"Mets toi Maétre avant");	
				return true;
				}
				if(perso.getAccount().getSubscribeRemaining() == 0L){
	               	 SocketManager.GAME_SEND_MESSAGE(perso,"Réservé au V.I.P.","008000");	 
	                return true;	 
	                }
				 if(perso.ipKamas)
				    {
				      perso.ipKamas=false;
				      SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne gagnerez plus tous les kamas de cette IP.");
				    }
				    else
				    {
				    	
				      perso.ipKamas=true;
				      SocketManager.GAME_SEND_MESSAGE(perso,"Vous allez maintenant gagner tous les kamas de cette IP.");
				      for(Player z : Main.world.getOnlinePlayers())
				      {
				        if(z==null)
				          continue;
				        if(z.getAccount().getCurrentIp().equals(perso.getAccount().getCurrentIp()))
				        {
				          if(z.ipKamas && z.getId() != perso.getId()) {
				        	  z.ipKamas=false;
						      SocketManager.GAME_SEND_MESSAGE(z,"Vous ne gagnerez plus tous les kamas de cette IP.");  
				          }
				        }
				      }
				    }
				    return true;
			} 
			if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("ipdrop")) {
				if(perso.getParty() == null || perso.getParty().getMaster() == null ||
						perso.getParty().getMaster().getId() != perso.getId())
				{
				SocketManager.GAME_SEND_MESSAGE(perso,"Mets toi Maétre avant");	
				return true;
				}
				if(perso.getAccount().getSubscribeRemaining() == 0L){
	               	 SocketManager.GAME_SEND_MESSAGE(perso,"Réservé au V.I.P.","008000");	 
	                return true;	 
	                }
				 if(perso.ipDrop)
				    {
				      perso.ipDrop=false;
				      SocketManager.GAME_SEND_MESSAGE(perso,"Vous ne gagnerez plus tous les drops de cette IP.");
				    }
				    else
				    {
				    	
				      perso.ipDrop=true;
				      SocketManager.GAME_SEND_MESSAGE(perso,"Vous allez maintenant gagner tous les drops de cette IP.");
				      for(Player z : Main.world.getOnlinePlayers())
				      {
				        if(z==null)
				          continue;
				        if(z.getAccount().getCurrentIp().equals(perso.getAccount().getCurrentIp()))
				        {
				          if(z.ipDrop && z.getId() != perso.getId()) {
				        	  z.ipDrop=false;
						      SocketManager.GAME_SEND_MESSAGE(z,"Vous ne gagnerez plus tous les drops de cette IP.");  
				          }
				        }
				      }
				    }
				    return true;
			}
			
			if(Config.singleton.serverId == 1)
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("start") || msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("astrub")) {
				if (perso.isInPrison()) {
					return true;
				}
				if (perso.getFight() != null) {
					return true;
				}
				if (System.currentTimeMillis() <  perso.getGameClient().timeLasttpcommande) {
	                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				 perso.getGameClient().timeLasttpcommande =(System.currentTimeMillis()+1000);
				perso.teleport((short) 6954, 283);
				return true;
			}
			
			if(Config.singleton.serverId == 1)
			if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("pvp")) {
				if (perso.isInPrison()) {
					return true;
				}
				if (perso.getFight() != null) {
					return true;
				}
				if (System.currentTimeMillis() <  perso.getGameClient().timeLasttpcommande) {
	                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				 perso.getGameClient().timeLasttpcommande =(System.currentTimeMillis()+1000);
				perso.teleport((short) 952, 297);
				return true;
			}
			if (msg.length() > 8 && msg.substring(1, 9).equalsIgnoreCase("marchand")) {
				if (perso.isInPrison()) {
					return true;
				}
				if (perso.getFight() != null) {
					return true;
				}
				if (System.currentTimeMillis() <  perso.getGameClient().timeLasttpcommande) {
	                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				 perso.getGameClient().timeLasttpcommande =(System.currentTimeMillis()+1000);
				perso.teleport((short) 33, 283);
				return true;
			}
			if (msg.length() > 9 && msg.substring(1, 10).equalsIgnoreCase("joindelay")) {
                try {
                	int join = Integer.parseInt(msg.substring(11).replace("|", ""));
                	if(join < 1)
                	join = 1;
                	if(join > 10)
                    	join = 10;
                	perso.setJoindelay(join);
                	SocketManager.GAME_SEND_MESSAGE(perso,"joindelay "+perso.getJoindelay()+" seconde(s)"); 
                }catch(Exception e)
                {
                	SocketManager.GAME_SEND_MESSAGE(perso,".joindelay 1 é 10 en seconde");   
                  }
				return true;
			}
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("event")) {
				if (perso.isInPrison()) {
					return true;
				}
				if (perso.getFight() != null) {
					return true;
				}
				if (System.currentTimeMillis() <  perso.getGameClient().timeLasttpcommande) {
	                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				 perso.getGameClient().timeLasttpcommande =(System.currentTimeMillis()+1000);
				perso.teleport((short) 3336, 183);
				return true;
			}
			else {
				if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("infos")) {
					long uptime = System.currentTimeMillis() - Config.getInstance().startTime;
					final int jour = (int) (uptime / 86400000L);
					uptime %= 86400000L;
					final int hour = (int) (uptime / 3600000L);
					uptime %= 3600000L;
					final int min = (int) (uptime / 60000L);
					uptime %= 60000L;
					final int sec = (int) (uptime / 1000L);
					int fake = 0;
					 long calcul=System.currentTimeMillis()-Config.getInstance().startTime;
					 if(calcul>1200000) {
						 if(Config.getInstance().serverId == 1 )
				        	fake = 100;
						 if(Config.getInstance().serverId == 22|| Config.getInstance().serverId == 7)
					        	fake = 20;
					 }
					final int nbPlayer = Main.world.getOnlinePlayers().size()+fake;
					//final int nbPlayerIp = Main.gameServer.getPlayersNumberByIp();
					final int maxPlayer = Main.Max_players+fake;
					String mess = "<b>" + Config.getInstance().name + "</b>\n" + "Uptime : " + jour + "j " + hour + "h "
							+ min + "m " + sec + "s.";
					if (nbPlayer > 0) {
						mess = String.valueOf(mess) + "\nJoueurs en ligne : " + nbPlayer;
					}
				//	if (nbPlayerIp > 0)  mess = String.valueOf(mess) + "\nJoueurs uniques en ligne : " + nbPlayerIp;
					
					if (maxPlayer > 0) {
						mess = String.valueOf(mess) + "\nRecord de connexion : " + maxPlayer;
					}
					SocketManager.GAME_SEND_MESSAGE(perso, mess);
					return true;
				}
				if(Config.singleton.serverId == 1) {
				SocketManager.GAME_SEND_MESSAGE(perso,
						"Les commandes disponibles sont  :\n<b>.infos</b> - Permet d'obtenir des informations sur le serveur."
						+ "\n<b>.start</b> - Permet de se téléporter au zaap."
						+ "\n<b>.marchand</b> - Permets de se téléporter é la map marchande."
						+ "\n<b>.staff</b> - Permet de voir les membres du staff connect\u00e9s."
						+ "\n<b>.boutique</b> - Permet d'accéder é la boutique."
						+ "\n<b>.points</b> - Affiche ses points boutique."
						+ "\n<b>.all</b> - <b>.noall</b> - Permet d'envoyer un message \u00e0 tous les joueurs."
						+ "\n<b>.celldeblo</b> - Permet de téléporter é une cellule libre si vous étes bloqués."
						+ "\n<b>.movemobs</b> - Permet de deplace un groupe de monstres."
						+ "\n<b>.banque</b> - Ouvrir la banque néimporte oé."
						+ "\n<b>.maitre</b> - Permet de créer une escouade et d'inviter toutes tes mules dans ton groupe."
						+ "\n<b>.window</b> - Permet de gérer toutes vos mules en combat via la fenétre du maitre."
						+ "\n<b>.tp</b> - Permet de téléporter tes personnages sur ta map actuelle."
						+ "\n<b>.tpgroupe</b> - Permets de téléporter ton groupe sur ta map actuelle."
						+ "\n<b>.pass</b> - Permet au joueur de passer automatiquement ses tours."
						+ "\n<b>.nodrop</b> - Vous empéche de recevoir des items de monstres."
						+ "\n<b>.joindelay</b> - Permet de définir le delai d'attente avant de rejoindre le combat pour vos mules en groupe."
						+ "\n<b>.debug</b> - permet de debug le personnage lors d'un freeze."
						+ "\n<b>.hdv</b> - Permet d'accéder au HDV."
						+ "\n<b>.exopa</b> - Permet d' exo pa un item."
						+ "\n<b>.exopm</b> - Permet de exo pm un item."
						+ "\n<b>.fmcac</b> - Permet de fm votre cac."
						+ "\n<b>.jetmax</b> - Permet de passer les items equiper jet max."
						+ "\n<b>.vip</b> - Affiche les priviléges VIP."
						);
				}else
				{
					SocketManager.GAME_SEND_MESSAGE(perso,
							"Les commandes disponibles sont  :\n<b>.infos</b> - Permet d'obtenir des informations sur le serveur."
							//+ "\n<b>.start</b> - Permet de se téléporter au zaap."
							+ "\n<b>.marchand</b> - Permets de se téléporter é la map marchande."
							+ "\n<b>.staff</b> - Permet de voir les membres du staff connect\u00e9s."
							+ "\n<b>.boutique</b> - Permet d'accéder é la boutique."
							+ "\n<b>.points</b> - Affiche ses points boutique."
							+ "\n<b>.all</b> - <b>.noall</b> - Permet d'envoyer un message \u00e0 tous les joueurs."
							+ "\n<b>.celldeblo</b> - Permet de téléporter é une cellule libre si vous étes bloqués."
							+ "\n<b>.movemobs</b> - Permet de deplace un groupe de monstres."
							+ "\n<b>.banque</b> - Ouvrir la banque néimporte oé."
							+ "\n<b>.maitre</b> - Permet de créer une escouade et d'inviter toutes tes mules dans ton groupe."
							+ "\n<b>.window</b> - Permet de gérer toutes vos mules en combat via la fenétre du maitre."
							+ "\n<b>.tp</b> - Permet de téléporter tes personnages sur ta map actuelle."
							+ "\n<b>.tpgroupe</b> - Permets de téléporter ton groupe sur ta map actuelle."
							+ "\n<b>.pass</b> - Permet au joueur de passer automatiquement ses tours."
							+ "\n<b>.nodrop</b> - Vous empéche de recevoir des items de monstres."
							+ "\n<b>.debug</b> - permet de debug le personnage lors d'un freeze."
							+ "\n<b>.hdv</b> - Permet d'accéder au HDV."
							+ "\n<b>.exopa</b> - Permet d' exo pa un item."
							+ "\n<b>.exopm</b> - Permet de exo pm un item."
							+ "\n<b>.fmcac</b> - Permet de fm votre cac."
							+ "\n<b>.jetmax</b> - Permet de passer les items equiper jet max."
							+ "\n<b>.vip</b> - Affiche les priviléges VIP."
							);	
				}
				return true;
			}
		}
	}

