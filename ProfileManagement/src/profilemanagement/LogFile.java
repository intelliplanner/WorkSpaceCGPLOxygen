/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package profilemanagement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vi$ky
 */
public class LogFile {
    private static String exception = "Test code";
  
    public static void main(String s[]){
        try {
            Log_File.write(exception);
        } catch (IOException ex) {
            Logger.getLogger(LogFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
