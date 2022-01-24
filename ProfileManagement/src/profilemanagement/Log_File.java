/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package profilemanagement;

/**
 *
 * @author rajeev
 */
import java.io.*;
 import java.text.*;
 import java.util.*;

     public class Log_File {
     protected static String defaultLogFile = "FingerPrinlog.txt";
      private static File configFile = new File(defaultLogFile);
         public static void write(String s) throws IOException {
              if(!configFile.exists()){
                FileOutputStream cfos = new FileOutputStream(defaultLogFile);
           }
         write(defaultLogFile, s);
     }
    
         public static void write(String f, String s) throws IOException {
            
         TimeZone tz = TimeZone.getTimeZone("EST"); // or PST, MID, etc ...
         Date now = new Date();
         DateFormat df = new SimpleDateFormat ("dd.MM.yyyy hh:mm:ss ");
         df.setTimeZone(tz);
         String currentTime = df.format(now);
         FileWriter aWriter = new FileWriter(f, true);
         aWriter.write("  "+"\n");
         aWriter.write(currentTime + " " + s + "\n");
         aWriter.flush();
         aWriter.close();
     }
 }