package soufix.exchanger;

import java.net.*;

import soufix.common.CryptManager;
import soufix.main.Config;
import soufix.main.Main;

import java.io.*;

 
public class ExchangeClient implements Runnable {
 
	private Socket _s;
	private BufferedReader _in;
	private PrintWriter _out;
	private Thread _t;
	public static int com_Try = 0;
 
        
        public ExchangeClient() {
        	try {
    			_s = new Socket(Config.getInstance().exchangeIp, Config.getInstance().exchangePort);
    			_in = new BufferedReader(new InputStreamReader(_s.getInputStream()));
    			_out = new PrintWriter(_s.getOutputStream());
    			_t = new Thread(this);
    			_t.setDaemon(true);
    			_t.start();
    		} catch (Exception e) {
        		System.out.println("ComServer : Connection au Realm impossible");
        		Main.com_Running = false;
        		try_ComServer();
    		}
        }
    	public static void try_ComServer()
    	{
    		if(com_Try == 0 && Main.isRunning)
    		{
    			try {
    				System.out.print("Creation d'une nouvelle connexion avec le Realm (ComServer) ... ");
    				com_Try = 1;
    				while(Main.com_Running == false && Main.isRunning)
    				{
    					Main.exchangeClient = new ExchangeClient();
    					Thread.sleep(2000);
    				}
    				System.out.println("ComServer de nouveau operationnel !");
    				com_Try = 0;
    			} catch (InterruptedException e) {
    				try_ComServer();
    			}
    		}
    	}
        
        public void run() {
        	try{
        		StringBuilder sPacket = new StringBuilder();
        		char[] charCur = new char[1];
        		Main.com_Running = true;
        		while (_in.read(charCur, 0, 1) != -1 && Main.isRunning) {
    				if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r') {
    					sPacket.append(charCur[0]);
    				} else if (sPacket.length() > 0) {
    					String packet = sPacket.toString();
    					//System.out.println(packet);
    					ExchangePacketHandler.parser(packet);
    					// Ancestra.printDebug(packet);
    					packet = null;
    					sPacket = new StringBuilder();
    				}
    			}
        	}catch(IOException e)
        	{
        		System.out.println("\nComServer : Serveur d'echange inlancable");
        		Main.com_Running = false;
        		try_ComServer();
        	}
        }
        public void send(String str)
    	{
        	try {
        		Thread.sleep(200);
        	//System.out.println("out >> "+str);
        	str = CryptManager.toUtf(str);
			_out.print((str)+(char)0x00);
			_out.flush();
        	}
			catch(Exception e)
      	    {
				try_ComServer();
      	    }
    	}

}