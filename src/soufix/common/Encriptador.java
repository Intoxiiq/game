package soufix.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Encriptador {
	private static final char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',// 15
	'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',// 38
	'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',// 61
	'-', '_'};// q = 16, N = 40, - = 63 _ = 64
	private static char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	public static final String ABC_MIN = "abcdefghijklmnopqrstuvwxyz";
	public static final String ABC_MAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String VOCALES = "aeiouAEIOU";
	public static final String CONSONANTES = "bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ";
	public static final String NUMEROS = "0123456789";
	public static final String ESPACIO = " ";
	public static final String GUIONES = "_-";
	
	public static String crearKey(final int limite) {
		try {
		final StringBuilder nombre = new StringBuilder();
		while (nombre.length() < limite) {
			nombre.append(HASH[Formulas.getRandomValue(0, HASH.length - 1)]);
		}
		final StringBuilder key = new StringBuilder();
		for (char c : nombre.toString().toCharArray()) {
			key.append(Integer.toHexString(c));
		}
		return key.toString();
	}
	   catch(Exception e)
	    {
	      e.printStackTrace();
	    }
		return null;
	}

	
	

	public static String unprepareData(final String s,final int currentKey,final String[] aKeys) {
		try {
			if (currentKey < 1) {
				return s;
			}
			final String _loc3 = aKeys[Integer.parseInt(s.substring(0, 1), 16)];
			if (_loc3 == null) {
				return s;
			}
			final String _loc4 = s.substring(1, 2).toUpperCase();
			final String _loc5 = decypherData(s.substring(2), _loc3, Integer.parseInt(_loc4, 16) * 2);
			if (checksum(_loc5) != (_loc4.charAt(0))) {
				return (s);
			}
			return (_loc5);
		} catch (Exception e) {
			return s;
		}
	}
	
	public static String prepareData(String s, int currentKey, String[] aKeys) {
		if (currentKey < 1) {
			return s;
		}
		if (aKeys[currentKey] == null) {
			return s;
		}
		char _loc3 = HEX_CHARS[currentKey];
		char _loc4 = checksum(s);
		String listo = (_loc3 + "" + _loc4 + "" + cypherData(s, aKeys[currentKey], Integer.parseInt(_loc4 + "", 16) * 2));
		return listo;
	}
	
	private static String cypherData(String d, String k, int c) {
		StringBuilder _loc5 = new StringBuilder();
		int _loc6 = k.length();
		d = preEscape(d);
		for (int _loc7 = 0; _loc7 < d.length(); _loc7++) {
			_loc5.append(d2h((int) (d.charAt(_loc7)) ^ (int) (k.charAt((_loc7 + c) % _loc6))));
		}
		return _loc5.toString();
	}
	
	private static String decypherData(final String d,final String k,final int c) {
		 String _loc5 = "";
		final int _loc6 = k.length();
		 int _loc7 = 0;
		 int _loc9 = 0;
		for (; _loc9 < d.length(); _loc9 = _loc9 + 2) {
			_loc5 += (char) (Integer.parseInt(d.substring(_loc9, _loc9 + 2), 16) ^ k.codePointAt((_loc7 + c) % _loc6));
			_loc7++;
		}
		_loc5 = unescape(_loc5);
		return (_loc5);
	}
	
	private static String unescape(String s) {
		try {
			s = URLDecoder.decode(s, "UTF-8");
		} catch (Exception e) {}
		return s;
	}
	
	// oscila del 32 al 127, todos los contenidos de k 95
	private static String escape(String s) {
		try {
			s = URLEncoder.encode(s, "UTF-8");
		} catch (Exception e) {}
		return s;
	}
	
	private static String preEscape(String s) {
		StringBuilder _loc3 = new StringBuilder();
		for (int _loc4 = 0; _loc4 < s.length(); _loc4++) {
			char _loc5 = s.charAt(_loc4);
			int _loc6 = _loc5;
			if (_loc6 < 32 || (_loc6 > 127 || (_loc5 == '%' || _loc5 == '+'))) {
				_loc3.append(escape(_loc5 + ""));
				continue;
			}
			_loc3.append(_loc5);
		}
		return _loc3.toString();
	}
	
	private static String d2h(int d) {
		if (d > 255) {
			d = 255;
		}
		return (HEX_CHARS[(int) Math.floor(d / 16)] + "" + HEX_CHARS[d % 16]);
	}
	
	public static String prepareKey(String d) {
		try {
		String _loc3 = new String();
		int _loc4 = 0;
		for (; _loc4 < d.length(); _loc4 = _loc4 + 2) {
			_loc3 = _loc3 + (char) (Integer.parseInt(d.substring(_loc4, _loc4 + 2), 16));
		}
		_loc3 = unescape(_loc3);
		return (_loc3);
	}
	   catch(Exception e)
	    {
	      e.printStackTrace();
	    }
		return d;
	}
	
	private static char checksum(final String s) {
		int _loc3 = 0;
		int _loc4 = 0;
		for (; _loc4 < s.length(); _loc4++) {
			_loc3 = _loc3 + s.codePointAt(_loc4) % 16;
		}
		return HEX_CHARS[_loc3 % 16];
	}
	
	public static void consultaWeb(String url) throws Exception {
		URL obj = new URL(url);
		URLConnection con = obj.openConnection();
		con.setRequestProperty("Content-type", "charset=Unicode");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		while ((in.readLine()) != null) {
			Thread.sleep(5);
		}
		in.close();
	}
	
	public static String aUTF(final String entrada) {
		String out = "";
		try {
			out = new String(entrada.getBytes("UTF8"));
		} catch (final Exception e) {
		}
		return out;
	}
	
	public static String aUnicode(final String entrada) {
		String out = "";
		try {
			out = new String(entrada.getBytes(), "UTF8");
		} catch (final Exception e) {
			System.out.println("Conversion en UNICODE fallida! : " + e.toString());
		}
		return out;
	}
}
