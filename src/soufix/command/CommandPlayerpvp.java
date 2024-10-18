package soufix.command;

import soufix.client.Player;
import soufix.client.other.Stats;
import soufix.common.SocketManager;
import soufix.fight.spells.Spell.SortStats;
import soufix.game.GameClient;
import soufix.game.World;
import soufix.game.action.ExchangeAction;
import soufix.main.Config;
import soufix.main.Constant;
import soufix.main.Main;
import soufix.object.GameObject;

public class CommandPlayerpvp {
	private static String canal;

	static {
		CommandPlayerpvp.canal = "Casper";
	}

	public static boolean analyse(final Player perso, final String msg) {
		if (msg.charAt(0) != '.' || msg.charAt(1) == '.') {
			return false;
		}
		if(msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("points")) {
			perso.sendMessage("Vous avez <b>" + perso.getAccount().getPoints() + "</b> points boutique");
			return true;
		} 
		if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("all") || msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("monde")) {
			if (perso.noall) {
				SocketManager.GAME_SEND_MESSAGE(perso,
						"Votre canal " + CommandPlayerpvp.canal + " est d\u00e9sactiv\u00e9.", "C35617");
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
		} else {
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("noall")) {
				if (perso.noall) {
					perso.noall = false;
					SocketManager.GAME_SEND_MESSAGE(perso,
							"Vous avez activ\u00e9 le canal " + CommandPlayerpvp.canal + ".", "C35617");
				} else {
					perso.noall = true;
					SocketManager.GAME_SEND_MESSAGE(perso,
							"Vous avez d\u00e9sactiv\u00e9 le canal " + CommandPlayerpvp.canal + ".", "C35617");
				}
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
			if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("vip")) {
				SocketManager.PACKET_POPUP_DEPART(perso, 
						 "\n- Vos points acquis par vote sur Serveur-Prive passent é 20 PB par vote."
						+ "\n+ <b>10.000</b> pods de plus."
						+ "\n<b>.aura</b> - Aura exlusive multicolore"
						+ "\n Vous obtenez un bonus de 15% point d'honneur  é chaque combat pvp");
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
			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("level")) {
				if (perso.getFight() != null) {
					return true;
				}
				   int count = 0;
					try {
					String name = msg.substring(7, msg.length() - 1);
					int count2 = Integer.parseInt(name);
					count = count2;
					}
					catch (Exception e) {
						SocketManager.GAME_SEND_MESSAGE(perso,"Level innexistant");
					return true; 
					}
						if(count > 201){
							SocketManager.GAME_SEND_MESSAGE(perso,"Level Maximum 200");
						return true;
						}
						if(perso.getLevel() > count){
							SocketManager.GAME_SEND_MESSAGE(perso,"Level bas de votre niveau");
						return true;
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
				        String mess="Vous avez fixe le niveau de "+perso.getName()+" a "+count+".";
				        SocketManager.GAME_SEND_MESSAGE(perso,mess);
	
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
			if (msg.length() > 8 && msg.substring(1, 9).equalsIgnoreCase("boutique")) {
				GameClient.leaveExchange(perso);
				soufix.main.Boutique.open(perso);
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

			if (msg.length() > 5 && msg.substring(1, 6).equalsIgnoreCase("start") || msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("shop")) {
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
				perso.teleport((short) 10114, 282);
				return true;
			}
			if (msg.length() > 4 && msg.substring(1, 5).equalsIgnoreCase("pvp2")) {
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
				perso.teleport((short) 7423, 266);
				return true;
			}
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
			if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("enclos")) {
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
				perso.teleport((short) 8746, 189);
				return true;
			}
			if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("pvm")) {
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
				perso.teleport((short) 10134, 297);
				return true;
			}
			if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("guilde")) {
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
				perso.teleport((short) 2196, 313);
				return true;
			}
			if (msg.length() > 8 && msg.substring(1, 9).equalsIgnoreCase("spellmax")) {

				if (perso.getFight() != null) {
					return true;
				}
				if (System.currentTimeMillis() <  perso.getGameClient().timeLasttpcommande) {
	                perso.sendMessage("Tu dois attendre encore "+(System.currentTimeMillis() -  perso.getGameClient().timeLasttpcommande) / 1000+" seconde(s)");
	                return true;
	            }
				for (SortStats sort : perso.getSorts().values()) {
					for (int i=0; i<6;i++) {
						perso.boostSpellpvp(sort.getSpellID());
					}
					
					
				}
				
					 perso.sendMessage("Vous avez boosté vos sorts au niveaux");
					 SocketManager.GAME_SEND_ASK(perso.getGameClient(),perso);
				      SocketManager.GAME_SEND_SPELL_LIST(perso);
				
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
			else
				if (msg.length() > 6 && msg.substring(1, 7).equalsIgnoreCase("ration")) {
					 SocketManager.GAME_SEND_MESSAGE(perso,""
					     		+ "Victoires PvM :<b> "+World.get_Succes(perso.getId()).getCombat() +
					    		 "</b>. Victoires PvP : <b>"+World.get_Succes(perso.getId()).getPvp()+ ""
					    		 +"</b>. Défaites PvP : <b>"+World.get_Succes(perso.getId()).getPvp_lose()+ ""
					    		 +"</b>. Victoires Kolizéum : <b>"+World.get_Succes(perso.getId()).getKoli_wine()+"</b>"
					    		 +"</b>. Défaites Kolizéum : <b>"+World.get_Succes(perso.getId()).getKoli_lose()+"</b>"
					    		 +"</b>. Donjons effectués : <b>"+World.get_Succes(perso.getId()).getDonjon()
					    		 +"</b>. Points boutique dépenser : <b>"+World.get_Succes(perso.getId()).getBoutique()
					    		 +"</b>. Challenges remporter : <b>"+World.get_Succes(perso.getId()).getChall()+"</b>"
					    		 +"</b>. Messages envoyer : <b>"+World.get_Succes(perso.getId()).getMsg()+"</b>","008000");
					return true;
				}
				else 
				if (msg.length() > 3 && msg.substring(1, 4).equalsIgnoreCase("vie")) {
					if (perso.getFight() != null && perso.getFight().getState() != Constant.FIGHT_STATE_PLACE) {
						return true;
					}
					int val = 100000;
					if(perso.getCurPdv()+val>perso.getMaxPdv())
	                    val=perso.getMaxPdv()-perso.getCurPdv();
					perso.setPdv(perso.getCurPdv()+val);
	                  if(perso.getFight()!=null)
	                	  perso.getFight().getFighterByPerso(perso).setPdv(perso.getCurPdv());
	                  SocketManager.GAME_SEND_STATS_PACKET(perso);
	                  SocketManager.GAME_SEND_Im_PACKET(perso,"01;"+val);

			        perso.sendMessage("Vous avez fixe le pourcentage de vitalite de "+perso.getName()+" a 100%.");
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
						 if(Config.getInstance().serverId == 1)
				        	fake = 100;
						 if(Config.getInstance().serverId == 22)
					        	fake = 20;
					 }
					final int nbPlayer = Main.world.getOnlinePlayers().size()+fake;
					//final int nbPlayerIp = Main.gameServer.getPlayersNumberByIp();
					final int maxPlayer = Main.Max_players+fake;
					String mess = "<b>Finishim PvP</b>\n" + "Uptime : " + jour + "j " + hour + "h "
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
				SocketManager.GAME_SEND_MESSAGE(perso,
						"Les commandes disponibles sont  :\n<b>.infos</b> - Permet d'obtenir des informations sur le serveur."
						+ "\n<b>.start</b> - Permet de se téléporter au map start."
						+ "\n<b>.pvp</b> - Permet de se téléporter a la map pvp."
						+ "\n<b>.pvp2</b> - Permet de se téléporter a la map pvp2."
						+ "\n<b>.pvm</b> - Permet de se téléporter a la map pvm."
						+ "\n<b>.event</b> - Permet de se téléporter a la map event."
						+ "\n<b>.enclos</b> - Permet de se téléporter a la map enclos."
						+ "\n<b>.guilde</b> - Permet de se téléporter a la map creation de guilde."
						+ "\n<b>.staff</b> - Permet de voir les membres du staff connect\u00e9s."
						+ "\n<b>.boutique</b> - Permet d'accéder é la boutique."
						+ "\n<b>.points</b> - Affiche ses points boutique."
						+ "\n<b>.all</b> - <b>.noall</b> - Permet d'envoyer un message \u00e0 tous les joueurs."
						+ "\n<b>.banque</b> - Ouvrir la banque néimporte oé."
						+ "\n<b>.fmcac</b> - Permet de (Fm) son arme"
						+ "\n<b>.exopa</b> - Permet de (exo)"
						+ "\n<b>.exopm</b> - Permet de (exo)"
						+ "\n<b>.vie</b> - Permet de mettre votre vie au maximum"
						+ "\n<b>.spellmax</b> - permet de monter level 6 tous les sorts"
						+ "\n<b>.level</b> - Level - Ajoute des levels."
						+ "\n<b>.ration</b> - Affiche son Ration PvP."
						+ "\n<b>.vip</b> - Affiche les priviléges VIP."
						);
				return true;
			}
		}
	}
}
