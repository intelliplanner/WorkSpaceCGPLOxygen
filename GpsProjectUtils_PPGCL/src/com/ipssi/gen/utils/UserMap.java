package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;
//import oracle.xml.parser.v2.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
/*
Menu processing:
cache -> copy of menu
user makes a copy of menu then in loadAndProcessOrigMenu it will make attributes that are available to
the user by either m or c (m = global why?)

*/




public class UserMap {
    public HashMap m_userMap = new HashMap(100,0.75f); //index userId, values => User
    public User get(int userId) { //will get things that are available
       return getUser(userId);
    }
    public synchronized User getUser(int userId) { //will get things that are available
       User user = (User) m_userMap.get(new Integer(userId));       
       return user;
    }
    
    public synchronized void addUser(User user) {
       int uid = user.getUserId();
       
       Integer uidInt = new Integer(uid);
       m_userMap.put(uidInt, user);       
    }
   
    public synchronized void helpCacheClearPortInfo() {
     //go thru all users and clearPortInfo() on the user
     return;
     /*
     Iterator iter = m_userMap.values().iterator();
//     System.out.println("###### UserMapSize in clear:"+Integer.toString(getUserMap().size()));
     while (iter.hasNext()) {
        ArrayList v  = (ArrayList) iter.next();
        for (int i=0,is= v == null ? 0 : v.size();i<is;i++) {
           User u = (User) v.get(i);
//           u.clearPortInfo();   TODO GET RID OF THIS
        }
     }
     */
   }
   synchronized public void invalidateUserMap(int u) {
     if (u < 0) {
        Integer i0 = new Integer(0);
        Integer i1 = new Integer(1);
        User vu0 = (User) m_userMap.get(i0);
        User vu1 = (User) m_userMap.get(i1);
        
        m_userMap.clear();
        if (vu0 != null)
           m_userMap.put(i0, vu0);
        if (vu1 != null)
           m_userMap.put(i1, vu1);
     }
     else if (u > 1) {
        m_userMap.remove(new Integer(u));
     }
  }
  
  synchronized void remove(int uid) {
     m_userMap.remove(new Integer(uid));
  }
  
}