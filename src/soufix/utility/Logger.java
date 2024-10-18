package soufix.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import soufix.main.Main;

/**
 * Class de gestion d'écriture de logs dans un fichier
 *
 * @author Neo-Craft
 *
 */
public class Logger {

    private BufferedWriter out;
    private int bufferSize;

    /**
     *
     * @param filePath Chemin d'accés relatif ou absolue du fichier oé écrire
     * les logs.
     */
    public Logger(String filePath, int bufferSize) {
        File fichier = new File(filePath);

        try {
            FileWriter tmpWriter = new FileWriter(fichier, true);
            out = new BufferedWriter(tmpWriter);
        } catch (IOException e) {
        	LoggerManager.checkFolder("Logs/Ip_logs/" + Main.FolderLogName);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        //setBufferSize(bufferSize);
    }

    /**
     * Ajoute une String dans le buffer. Elle seras écrite lorsque le buffer
     * seras plein ou é l'appel de la fonction "write()".
     *
     * @param toAdd Chaine de caractére é placer dans le buffer en vue d'une
     * écriture.
     */
    public void addToLog(String toAdd) {
        if (out == null) {
            return;
        }

        String date = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(+Calendar.MINUTE) + ":" + Calendar.getInstance().get(Calendar.SECOND);
        //toWrite.add(date + ": " + toAdd);

        //if(toWrite.size() >= bufferSize)
        write(date + ": " + toAdd);
        toAdd = null;
    }

    /**
     * Vide le buffer en écrivant tout son contenue dans le fichier de sortie.
     */
    public void write(String m) {
        if (out == null) {
            return;
        }
        try {
            out.write(m);
            out.newLine();
            out.flush();
        } catch (IOException e) {
        	LoggerManager.checkFolder("Logs/Ip_logs/" + Main.FolderLogName);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * écrit le contenue du buffer par un appel é la fonction "write()" et ferme
     * le flux de sortie par la suite.
     */
    public void close() {
        try {
            //write();
            if (out != null) {
                out.close();
            }
            out = null;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Place une chaine de retour é la ligne dans le buffer.
     *
     */
    public void newLine() {
        if (out == null) {
            return;
        }
        write("\r\n");
    }

    /**
     * Définit la taille du buffer. Elle influence le temps entre deux phase
     * d'écriture dans le fichier de sortie. Une taille plus petite résulte
     * d'une écriture fréquente mais plus rapide. Une taille plus grande résulte
     * d'une écriture plus rare mais plus longue.
     *
     * @param newSize La nouvelle taille du buffer. Si c'est une valeur insensé
     * (<= 0), la valeur par défaut (20) seras appliqué.
     */
    public void setBufferSize(int newSize) {
        if (bufferSize <= 0) {
            bufferSize = 20;
        }

        this.bufferSize = newSize;
    }
}