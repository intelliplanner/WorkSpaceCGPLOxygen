/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.dao;

import com.ipssi.mpl.dhama_gudiya.services.HttpClientServicesImpl;
import com.ipssi.mpl.dhama_gudiya.services.XmlParser;
import com.ipssi.rfid.beans.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javax.swing.JOptionPane;

/**
 *
 * @author IPSSI
 */
public class UserLoginClient {

    public String BASE_URL = "localhost:8080/LocTracker/insertRFIDMaithon.jsp";

    public static User Login(String username, String password) throws Exception {
        User user = null;
        try {
            if (username != null && password != null) {
                String userData = HttpClientServicesImpl.loginRequest(username, password);
                
                user = XmlParser.userDataParser(userData);
                if(user != null)
                	user.setSupperUser(isSuperUser(user.getId()));
                
                if (user != null && !isSuperUser(user.getId())) {
                    String privilegeData = HttpClientServicesImpl.getPrivlegeList(user.getId());
                    ArrayList<Integer> privilegeList = XmlParser.userPrivilegeParser(privilegeData);
                    user.setPrivList(privilegeList);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(UserLoginClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        }

        return user;
    }

    public static boolean isSuperUser(int userId) {
        boolean retval = userId == 1;
        return retval;
    }

    public static void main(String[] args) {
        try {
            //char[] ps = {'3'};
            UserLoginClient.Login("3", "3");
        } catch (Exception ex) {
            Logger.getLogger(UserLoginClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
