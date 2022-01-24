package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;
//import oracle.xml.parser.v2.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;


public  class PrivInfo extends Object  implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
     public ArrayList m_prjList;
     public ArrayList m_workspaceList;
     public ArrayList m_portList;
     public int    m_miscRefId; //refers to GraphId (or later to measureId)
     public boolean isGlobal;
     private ArrayList m_genPrivList; //indexed by PurchaseSearch.ObjectType, curr size 4
     private Integer g_dummy = new Integer(1);
     public PrivInfo() {
         isGlobal = false;
         m_miscRefId = Misc.getUndefInt();
         m_prjList = new ArrayList();
         m_workspaceList = new ArrayList();
         m_portList = new ArrayList();
         m_genPrivList = new ArrayList();
         m_genPrivList.add(new HashMap(20,0.75f));
         m_genPrivList.add(new HashMap(20,0.75f));
         m_genPrivList.add(new HashMap(20,0.75f));
         m_genPrivList.add(new HashMap(20,0.75f));
         
     }
     
     public void addGenObj(int objectType, int objectId) {
        if (objectType < 0 || objectType >= m_genPrivList.size())
           return;
        HashMap addInto = (HashMap) m_genPrivList.get(objectType);
        addInto.put(new Integer(objectId), g_dummy);
     }
     
     public boolean isGenPrivAv(int objectType, int objectId) {
        if (objectType < 0 || objectType >= m_genPrivList.size())
           return false;
        HashMap addInto = (HashMap) m_genPrivList.get(objectType);
        return addInto.get(new Integer(objectId)) != null ;
     }
     
     public boolean hasSomeObjInGen() {//returns true if there are some object in the gen hashmap
        for (int i=0,is=m_genPrivList == null ? 0 : m_genPrivList.size();i<is;i++) {
           HashMap hm = (HashMap) m_genPrivList.get(i);
           if (hm != null && !hm.isEmpty())
              return true;
        }
        return false;
     }

	 public static class TagInfo  implements Serializable {
	        /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		 public String m_tag = null; //m_tag = tag name in menu or predefined tag name
		 int m_id = Misc.getUndefInt();
		 public int m_read = 0; //read priv id
		 public int m_write = 0; //write priv id
		 public TagInfo(String tag, int id, int readPrivId, int writePrivId) {
			 m_tag = tag;
			 m_id = id;
			 m_read = readPrivId;
			 m_write = writePrivId;
		 }
	 }

	 public static class PrivAvail  implements Serializable {
	        /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		 public boolean m_read = false;
		 public boolean m_write = false;
	 }

	 public static class PrivDetails  implements Serializable {
	        /**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		 public int m_appliesTo = 0;// 0 = prj, 1 = port, 2 = global
		 public Element m_privElem = null;
		 public PrivDetails(int appliesTo, Element privElem) {
			 m_appliesTo = appliesTo;
			 m_privElem = privElem;
		 }
	 }
}
