package soufix.utility;

import java.util.Random;

public class RandomNam {
	
	static String[] dictionary = {"ae", "au", "ao", "ap", "ka", "ha", "ah", "na", "hi", "he", 
								  "eh", "an", "ma", "wa", "we", "wh", "sk", "sa", "se", "ne", 
								  "ra", "re", "ru", "ri", "ro", "za", "zu", "ta", "te", "ty", 
								  "tu", "ti", "to", "pa", "pe", "py", "pu", "pi", "po", "da", 
								  "de", "du", "di", "do", "fa", "fe", "fu", "fi", "fo", "ga", 
								  "gu", "ja", "je", "ju", "ji", "jo", "la", "le", "lu", "ma", 
								  "me", "mu", "mo", "cra", "kill", "-", "bana", "ron", "fou", "bl"
								  , "rox", "cap", "dou", "mech", "gra", "evil", "kani", "bou", "mig", "nig", "hel", "tro", "big", "how"
								  , "ki", "no", "raven", "snes", "ou", "hil", "pol", "z"};
	public static String get() {
		StringBuilder name = new StringBuilder();
		boolean b = true;
		
		do {
			int numberOfChaine = random(0, 2);
			
			for(int i = 0 ; i <= numberOfChaine ; i++)
				name
				.append(dictionary[random(0, dictionary.length - 1)]);
			
			if(name.toString().length() < 5) continue;
			else b = false;
		} while(b);
		
		char c = name.toString().charAt(0);
		return (""+c).toUpperCase() + name.toString().substring(1);
	}
	
	public static int random(int min, int max) {
		Random rand = new Random();
		return (rand.nextInt((max-min)+1))+min;
	}
}
