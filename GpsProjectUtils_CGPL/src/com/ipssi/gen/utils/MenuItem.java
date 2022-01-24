//
package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;
//import oracle.xml.parser.v2.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.xml.sax.*;
import org.w3c.dom.*;
public class MenuItem {
   public static class Param {
      public String m_name;
      public String m_value;
      public int m_foundAt;
      public boolean m_isFixed;
      public String toString() {
    	  return m_name;
      }
       public Param(String name, String value, int foundAt, boolean isFixed) {
         m_name = name;
         m_value = value;
         m_foundAt = foundAt;
         if (!isFixed && 
             (
             "page_name".equals(name) 
           || "dev_page".equals(name) 
           || "section_id".equals(name)
           || "user_param_0".equals(name)
           || "user_param_1".equals(name)           
           || "_no_lhs".equals(name)
             )
         )
            isFixed = true;
         m_isFixed = isFixed;
      }
      public Param(Element elem) {
         m_name = elem.getAttribute("name");
         m_value = elem.getAttribute("value");
         m_foundAt = Misc.getParamAsInt(elem.getAttribute("foundat"));
         boolean isFixed = "1".equals(elem.getAttribute("fixed"));
         if (!isFixed && 
             (
             "page_name".equals(m_name) 
           || "dev_page".equals(m_name) 
           || "section_id".equals(m_name)
           || "user_param_0".equals(m_name)
           || "user_param_1".equals(m_name)           
           || "_no_lhs".equals(m_name)
             )
         )
            isFixed = true;
         m_isFixed = isFixed;
      }
      public Param copy() {
         Param ret = new Param(m_name, m_value, m_foundAt, m_isFixed);
         return ret;
      }
   }//end of  param class
   
   public static class Button {
      public int m_id;
      public String m_name;
      public String m_url;
      public String m_script;
      public ArrayList m_params = null;
      public static HashMap g_buttonInfo = new HashMap(15,0.75f);
      public void addParam(Param param) {
         if (m_params == null)
           m_params = new ArrayList();
         m_params.add(param);
      }
      public Button(Element elem) {
         m_id = Misc.getParamAsInt(elem.getAttribute("id"));
         m_name = elem.getAttribute("name");
         m_url = elem.getAttribute("page");
         m_script = elem.getAttribute("script");
         m_params = null;
         
         for (Node n=elem.getFirstChild();n != null; n = n.getNextSibling()) {
             if (n.getNodeType() != 1)
                continue;
             Element e = (Element)n;
             if (e.getTagName().equals("_p_")) {
                Param param = new Param(e);
                addParam(param);
                
             }
         }
         g_buttonInfo.put(new Integer(m_id), this);         
      }
   }//end of button class
   public int m_id; //not sure if needed
   public String m_tag;
   public String m_url;
   public MenuItem m_parent = null;
   public ArrayList m_children = null;
   public String m_script;
   public String m_name;
   public String m_helpTag;
   public String m_accCheck;
   public String m_sn;   
   public String m_idParam = null;
   public String m_altPage = null;
   public boolean m_isGenerated = false;
   public boolean m_needsCheck = false;
   public boolean m_disableIfNoPrj = false;
   public String m_nameIfShownInPopup = null;
   
   public String toString() {
 	  return m_tag;
   }
   
   public ArrayList m_params = null;
   public ArrayList m_buttons = null;
   public ArrayList m_visibilityRules = null;//ArrayList of ArrayList WkspStepNew.CondPair. If no condition or one of the condition matches then show
   public ArrayList m_actionRules = null; //ArrayList of WkspStepNew.MenuNext oring achieved by having multiple entries for a nextState
   
  
   public static HashMap g_menuByTag = new HashMap(300, 0.75f);
   
   public static int g_currMenuItem = 0;
   public MenuItem(String tag, String menuName, String menu_url) {
	      m_id = g_currMenuItem++;
	      m_tag = tag.replaceAll("\\s", "");
	      m_url = menu_url;
	      m_script = null;
	      m_name = menuName;
	      m_helpTag = null;
	      m_accCheck = null;
	      m_sn = tag;
	      g_menuByTag.put(m_tag, this);
	   }
   public MenuItem(String tag) {
      m_id = g_currMenuItem++;
      m_tag = tag;
      m_url = null;
      m_script = null;
      m_name = tag;
      m_helpTag = null;
      m_accCheck = null;
      m_sn = tag;
      g_menuByTag.put(m_tag, this);
   }
   public MenuItem(Element elem) {//just reads the element
      m_id = g_currMenuItem++;
      m_tag = elem.getTagName();
      m_url = Misc.getParamAsString(elem.getAttribute("page"),null);
      m_script = Misc.getParamAsString(elem.getAttribute("script"),null);
      m_name = Misc.getParamAsString(elem.getAttribute("name"),null);
      m_nameIfShownInPopup = Misc.getParamAsString(elem.getAttribute("name_pop"),null);
      if (m_nameIfShownInPopup == null)
          m_nameIfShownInPopup = "Back to "+m_name;//m_name+" List";
      m_helpTag = Misc.getParamAsString(elem.getAttribute("help_tag"),null);
      m_accCheck = Misc.getParamAsString(elem.getAttribute("_acc_check"),null);
      m_sn = Misc.getParamAsString(elem.getAttribute("next_name"),null);
      if (m_sn == null)
         m_sn = m_name;
      m_isGenerated = "generated".equals(elem.getAttribute("type"));
      m_idParam = Misc.getParamAsString(elem.getAttribute("id"),null);
      m_needsCheck = "1".equals(elem.getAttribute("needs_check"));
      m_disableIfNoPrj = "1".equals(elem.getAttribute("disable_if_no_prj"));
      m_altPage = Misc.getParamAsString(elem.getAttribute("alt_page"),null);
      g_menuByTag.put(m_tag, this);
   }
   
   public static MenuItem getMenuInfo(String tag) {
      return (MenuItem) g_menuByTag.get(tag);
   }
   public static void remove(String tag){
	   g_menuByTag.remove(tag);
   }
   void addButton(Button button) {
      if (button == null)
         return;
      if (m_buttons == null)
         m_buttons = new ArrayList();
      m_buttons.add(button);
   }
   void addParam(Param param) {
      if (param == null)
         return;
      if (m_params == null)
         m_params = new ArrayList();
      m_params.add(param);
   }
   void appendChild(MenuItem item) {
      if (item == null)
         return;
      item.m_parent = this;
      if (m_children == null)
         m_children = new ArrayList();
      m_children.add(item);
   }
   void insertBefore(MenuItem item, MenuItem beforeThis) {
      if (item == null)
         return;
      if (beforeThis == null)
         appendChild(item);
      item.m_parent = this;
      
      if (m_children == null)
         m_children = new ArrayList();
      int pos = m_children.size();
      for (int i=0,is = pos;i<is;i++) {
          if (beforeThis == (MenuItem) m_children.get(i)) {
             pos = i;
             break;
          }
      }
      m_children.add(null);
      for (int i=m_children.size()-2;i>=pos;i--)
         m_children.set(i+1, m_children.get(i));
      m_children.set(pos, item);
   }
   
   void insertAfter(MenuItem item, MenuItem afterThis) {
	      if (item == null)
	         return;
	      if (afterThis == null)
	         appendChild(item);
	      item.m_parent = this;
	      
	      if (m_children == null)
	         m_children = new ArrayList();
	      int pos = m_children.size();
	      for (int i=0,is = pos;i<is;i++) {
	          if (afterThis == (MenuItem) m_children.get(i)) {
	             pos = i;
	             break;
	          }
	      }
	      m_children.add(null);
	      int newSize = m_children.size();
	      for (int i= newSize-1; i > pos+1;i--)
	         m_children.set(i, m_children.get(i-1));
	      m_children.set(pos+1, item);
	   }
   
   static public MenuItem helpProcessMenuDoc(Element menuElem, boolean inMain) {
     if (menuElem == null)
        return null;
     if (!inMain) { //currently ignoring top
        String tagName = menuElem.getTagName();
        if (tagName.equals("top"))
           return null;
        else if (tagName.equals("top_buttons")) {
           for (Node n=menuElem.getFirstChild();n != null;n=n.getNextSibling()) {
              if (n.getNodeType() != 1)
                 continue;
              Element e = (Element) n;
              Button button = new Button(e);
           }
           return null;
        }
        else if (tagName.equals("main")) {
           inMain = true;           
        }
        else {
           MenuItem retval = null;
           for (Node n=menuElem.getFirstChild();n!=null;n=n.getNextSibling()) {
              if (n.getNodeType() != 1)
                 continue;
              Element e = (Element) n;
              MenuItem t = helpProcessMenuDoc(e, false);
              if (t != null)
                 retval = t;
           }
           return retval;
        }
     }
     inMain = true;
     MenuItem menuItem = new MenuItem(menuElem);
     for (Node n=menuElem.getFirstChild(), next = null; n != null; n=next) {
        if (n.getNodeType() != 1) {
           next = n.getNextSibling();
           continue;
        }

        Element e = (Element) n;
        next = n.getNextSibling();
        
        String tagName = e.getTagName();
        if (tagName.equals("_p_")) {
            menuItem.addParam(new Param(e));            
            continue;
        }
        if (tagName.equals("_b_")) {            
            int bid = Misc.getParamAsInt(e.getAttribute("bid"));
            Button button = (Button) Button.g_buttonInfo.get(new Integer(bid));
            menuItem.addButton(button);            
            continue;
        }
        if ("_visibility_rules".equals(tagName)) { //can repeat to achieve or conditionality
           if (menuItem.m_visibilityRules == null)
              menuItem.m_visibilityRules = new ArrayList();
           
           ArrayList vecOfConditions = menuItem.m_visibilityRules;           
           ArrayList conditions = new ArrayList();
           vecOfConditions.add(conditions);
           WkspStepNew.readCondition(conditions, e); //CAPEX_REMOVE was WkspStepMgr 
           continue;
        }
        if ("_next_rules".equals(tagName)) { //can repeat to achieve or conditionality
           if (menuItem.m_actionRules == null)
              menuItem.m_actionRules = new ArrayList();
           ArrayList vecOfNexts = menuItem.m_actionRules;
           
           String nextTag = e.getAttribute("for");
           if (nextTag == null || nextTag.length() ==0)
              continue;
           if (menuElem.getOwnerDocument().getElementsByTagName(nextTag).getLength() == 0)
              continue;
           WkspStepNew.MenuNext menuNext = new WkspStepNew.MenuNext();
           menuNext.m_tag = nextTag;

           vecOfNexts.add(menuNext);
           WkspStepNew.readCondition(menuNext.m_conditions, e); //was WkspStepMgr
           e.getParentNode().removeChild(e);
           continue;
        }
        
        MenuItem chMenuItem = helpProcessMenuDoc(e, inMain);
        menuItem.appendChild(chMenuItem);        
     }//for each child
     if (menuItem.m_params != null)
     menuItem.m_params.trimToSize();
     if (menuItem.m_buttons != null)
     menuItem.m_buttons.trimToSize();
     if (menuItem.m_children != null)
     menuItem.m_children.trimToSize();
     return menuItem;
  }//end of func
   
   
   
}//end of class