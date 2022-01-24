/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package profilemanagement;

/**
 *
 * @author virendra Gupta
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
 
/**
 * A utility class that reads/saves SMTP settings from/to a properties file.
 * @author www.codejava.net
 *
 */
public class ConfigUtility {
 
   private File configFile = new File("configFile.properties");
    private Properties configProps;
    public Properties loadProperlies() throws FileNotFoundException,IOException {
   
        Properties defaultProps = new Properties();
        // sets default properties
        defaultProps.setProperty("mail.smtp.host", "smtp.gmail.com");
        defaultProps.setProperty("mail.smtp.port", "587");
        defaultProps.setProperty("mail.user", "ABC@gmail.com");
        defaultProps.setProperty("mail.password", "ABCDE");
        defaultProps.setProperty("mail.smtp.starttls.enable", "true");
        defaultProps.setProperty("mail.smtp.auth", "true");
         
         configProps = new Properties(defaultProps);
         
        // loads properties from file
        if (configFile.exists()) {
            InputStream inputStream = new FileInputStream(configFile);
            configProps.load(inputStream);
            inputStream.close();
        }
         
        return configProps;
      
    }
     
    public void saveProperties(String Host1, String Port1, String User1, char[] Pwd1) throws FileNotFoundException,IOException {
        configProps = new Properties();
        FileOutputStream cfos = new FileOutputStream("configFile.properties");
       configProps.setProperty("mail.smtp.host", Host1);
        configProps.setProperty("mail.smtp.port", Port1);
        configProps.setProperty("mail.user", User1);
        configProps.setProperty("mail.password", new String(Pwd1));
        configProps.setProperty("mail.smtp.starttls.enable", "true");
        configProps.setProperty("mail.smtp.auth", "true");
        configProps.store(cfos, "Host Setting Properties file generated from Java program");
                cfos.close();
    }  
}