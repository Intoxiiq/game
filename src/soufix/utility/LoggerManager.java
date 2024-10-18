package soufix.utility;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import soufix.main.Main;

public class LoggerManager {
    
    private static Map<String, Logger> Loggers = Collections.synchronizedMap(new HashMap<String, Logger>());
    
    public static Logger getLoggerByIp(String ip){
    	try{
        if(Loggers.containsKey(ip))
            return Loggers.get(ip);
        else{
            Logger l = new Logger("Logs/Ip_logs/"+Main.FolderLogName+"/"+ip+".txt",0);
            Loggers.put(ip, l);
            return l;
        } 
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
		return null;
    }
    
    public static void checkFolder(String name){
        if(!new File(name).exists()){
            new File(name).mkdir();
        }
    }
    
    public static String getDate(){
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"-"+(Calendar.getInstance().get(Calendar.MONTH)+1)+"-"+Calendar.getInstance().get(Calendar.YEAR);
    }
    
}
