//  Copyright (c) 2000 IntelliPlanner Software Systems, Inc.
package com.ipssi.gen.utils;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ipssi.SingleSession;
import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DimInfo.ValInfo;

public class Cache {

  private Document lovXML; //temporary will be reset to null after creation .... will be set to NULL at end of Cache creation
  private Document internalXML; //temporary will be reset to null after creation .... will be set to NULL at end of Cache creation
  
  private Document m_dashBoard = null; //TODO ... make it local
  private Document getDashBoardXML() { return m_dashBoard; } //TODO ... make it local
  private Document m_prjDashBoard = null; //TODO ... make it local though no longer used
  private Document getPrjDashBoardXML() { return m_prjDashBoard;} //TODO ... make it local though no longer used
  
  private Document m_privDoc; //privlieges XML file - but has privileges for the dyanamic graphs and menus
  private HashMap m_portVersion = null;  
  
  
  
  private Document getPrivilegeDoc() throws Exception {

   try {
     if (m_privDoc == null) {
        System.err.println("Privilege Document reloaded when it should not be ...");
        helpLoadPrivDoc();
     }
     return m_privDoc;
   }
   catch (Exception e) {
      e.printStackTrace();
      throw e;
   }
  }


  private static boolean m_globalIsSynced = false;
  public void syncGlobal(Connection dbConn) throws Exception {
     if (!m_globalIsSynced) {
     //CAPEX_REMOVED   Misc.syncGlobal(dbConn);
        m_globalIsSynced = true;
     }
  }
  public void markGlobalSyncDirty() { m_globalIsSynced = false;}
  
  private HashMap g_tagPrivLookup = null;//new HashMap(); //key = tagName val = PrivInfo.TagInfo, loaded in getPrivilegeDoc
  private HashMap g_privDetailsLookup = null; //new HashMap(); //key = privid, points to PrivInfo.PrivDetails (to what applies to & getPrivilegeDoc)

  public PrivInfo.TagInfo getPrivId(String tag) throws Exception
  {
	  if (false)
		  return null;
	  if (g_tagPrivLookup == null)
	  {
		  getPrivilegeDoc();
	  }
	  return (PrivInfo.TagInfo)g_tagPrivLookup.get(tag);
  }
  public PrivInfo.PrivDetails getPrivDetails(int id) throws Exception
  {
	  if (g_privDetailsLookup == null)
	  {
		  getPrivilegeDoc();
	  }
	  return (PrivInfo.PrivDetails)g_privDetailsLookup.get(new Integer(id));
  }

  
  
  private String m_portNodeRestrictor = null;
  
  public ArrayList m_workflowCrit = new ArrayList(); //ArrayList of ArrayList of DimConfigInfo //0 => org stuff, 1 => next state map searcher
  public ArrayList m_envelopeCrit = new ArrayList(); //ArrayList of integer
  public FrontGetValHelper m_workflowValGetter = null;
  //////
  //CAPEX_REMOVE public MiscInner.TimeReq m_wktimeReq = null;//getEnhPrjSelAttrib(m_workflowValGetter.getToAskDimListNew(), wkselClause, wkfromClause, wkjoinClause, false,false, dbConn, this, false);
  //CAPEX_REMOVE public StringBuilder m_wkpjInfo = new StringBuilder();
  
  static public final int LOV_TYPE = 1;
  static public final int STRING_TYPE = 2;
  static public final int NUMBER_TYPE = 3;
  static public final int LOV_NO_VAL_TYPE = 4;
  static public final int INTEGER_TYPE = 5;
  static public final int DATE_TYPE = 6;
  // sameer 04112006
  static public final int FILE_TYPE = 7;
  static public final int IMAGE_TYPE = 8;
  static public final int GROUP_TOTAL = 9;
  // end sameer 04112006

  static private final int LOV_STANDARD = 1;
  static private final int LOV_CUSTOM = 2;
  static public final int INTERNAL_CONST = 3;
  static public final int INTERNAL_LOV = 4;
  static public final int INTERNAL_DIMLIST = 5;

  
  private UserMap userMap = new UserMap();
  
  public static String serverConfigPath = Misc.getServerConfigPath();
  public static String webConfigPath = Misc.getWebConfigPath();
  private String internalName = "internal.xml";
  private String lovName = "lov.xml";
  
  
  public ColumnMappingHelper getColumnMapping(int id) {
     DimInfo dimInfo = DimInfo.getDimInfo(id);
     if (dimInfo != null)
         return dimInfo.m_colMap;
     return null;
  }

  public ColumnMappingHelper getColumnMapping(Integer id) {
     if (id != null)
        return getColumnMapping(id.intValue());
     else
        return null;
  }
  
  public int getDefaultInt(int dimId) {
      DimInfo dimInfo = DimInfo.getDimInfo(dimId);
      return dimInfo.getDefaultInt();
  }
  
  private MiscInner.PairElemInt getAttribCatNodeNotFromDimList(String attribCat, Element refDimensionsNodeInInternal) { //IS Fine only used in creation
    int retType = LOV_NO_VAL_TYPE; //the initialization is important basically if non lov in dimensions that overrides type found elsewhere
                                     //but if lov then just take from whereever it was found .. but being lazy so not checking if is
                                     //found only in dimensions and there  for LOV_NO_TYPE

    Element retNode = null;
    NodeList nl;
    Element node;
    NodeList attribNl;
    int definedWhere = 0;
    boolean found = false;
    if  (refDimensionsNodeInInternal  == null) {// this really needs to be in the last so that variables that are not
         nl = internalXML.getElementsByTagName("dimensions");
         node = (Element) nl.item(0);
         attribNl = node.getElementsByTagName(attribCat);
         if (attribNl.getLength() > 0) { // found it
             refDimensionsNodeInInternal = (Element) attribNl.item(0);
         }
      }

      if (refDimensionsNodeInInternal != null) {
          String typeDefined = refDimensionsNodeInInternal.getAttribute("type");
          if (!"lov".equals(typeDefined)) {
        	  // dev 09 Aug 2010
        	  return new MiscInner.PairElemInt(refDimensionsNodeInInternal, "string".equals(typeDefined) ? STRING_TYPE : "date".equals(typeDefined) ? DATE_TYPE: "file".equals(typeDefined) ? FILE_TYPE : "img".equals(typeDefined) ? IMAGE_TYPE : "integer".equals(typeDefined) ? INTEGER_TYPE : NUMBER_TYPE);
             
        	  // sameer 04112006
//              return new MiscInner.PairElemInt(refDimensionsNodeInInternal, "string".equals(typeDefined) ? STRING_TYPE : "date".equals(typeDefined) ? DATE_TYPE: "file".equals(typeDefined) ? FILE_TYPE : "img".equals(typeDefined) ? IMAGE_TYPE : NUMBER_TYPE);              
              // end sameer 04112006
          }
      }

      nl = lovXML.getElementsByTagName("standard");
      node = (Element)nl.item(0);
      attribNl = node.getElementsByTagName(attribCat);
      if (attribNl.getLength() > 0) { // found it
          retNode = (Element) attribNl.item(0);
          retType = LOV_TYPE;
          definedWhere = LOV_STANDARD;
          found = true;
      }
      if (retNode == null) {
         nl = lovXML.getElementsByTagName("custom");
         node = (Element) nl.item(0);
         nl = node.getElementsByTagName("lov");
         node = (Element) nl.item(0);
         attribNl = node.getElementsByTagName(attribCat);
         if (attribNl.getLength() > 0) { // found it
             retNode = (Element) attribNl.item(0);
             retType = LOV_TYPE;
             definedWhere = LOV_CUSTOM;
             found = true;
         }
      }
      if (retNode == null) {
         nl = lovXML.getElementsByTagName("custom");
         node = (Element) nl.item(0);
         nl = node.getElementsByTagName("other_custom_fields");
         node = (Element) nl.item(0);
         attribNl = node.getElementsByTagName(attribCat);
         if (attribNl.getLength() > 0) { // found it
              retNode = (Element) attribNl.item(0);
              String nodeTypeString = retNode.getAttribute("type");
              if (nodeTypeString.equalsIgnoreCase("number"))
                  retType = NUMBER_TYPE;
              else if (nodeTypeString.equalsIgnoreCase("string"))
                  retType = STRING_TYPE;
              else if (nodeTypeString.equalsIgnoreCase("date"))
                  retType = DATE_TYPE;
              // sameer 04112006
              else if (nodeTypeString.equalsIgnoreCase("file"))
                  retType = FILE_TYPE;
              else if (nodeTypeString.equalsIgnoreCase("img"))
                  retType = IMAGE_TYPE;
              // end sameer 04112006
              else
                  retType = LOV_NO_VAL_TYPE;
              definedWhere = LOV_CUSTOM;
              found = true;
         }
      }
      if (retNode == null) {
         nl = internalXML.getElementsByTagName("constants");
         node = (Element) nl.item(0);
         attribNl = node.getElementsByTagName(attribCat);
         if (attribNl.getLength() > 0) { // found it
              retNode = (Element) attribNl.item(0);
              retType = LOV_TYPE;
              definedWhere = INTERNAL_CONST;
              found = true;
         }
      }
      if (retNode == null) {
         nl = internalXML.getElementsByTagName("lov");
         node = (Element) nl.item(0);
         attribNl = node.getElementsByTagName(attribCat);
         if (attribNl.getLength() > 0) { // found it
             retNode = (Element) attribNl.item(0);
             retType = LOV_TYPE;
             definedWhere = INTERNAL_LOV;
             found = true;
         }
      }

      if (refDimensionsNodeInInternal != null) {
         String typeAttrib = refDimensionsNodeInInternal.getAttribute("type");
         boolean isNumber = typeAttrib.equals("number");
         if (isNumber)
            retType =  NUMBER_TYPE;
         else {
            boolean isString = typeAttrib.equals("string");
            if (isString)
              retType = STRING_TYPE;
            if ("date".equals(typeAttrib))
              retType = DATE_TYPE;
			// sameer 04112006
			      if ("file".equals(typeAttrib))
              retType = FILE_TYPE;
            if ("img".equals(typeAttrib))
              retType = IMAGE_TYPE;
            // sameer 04112006
         }
         if (typeAttrib.equals("lov") && !found) { //check if  is a subset_of
            String subSetOf = refDimensionsNodeInInternal.getAttribute("subset_of");
            if (subSetOf != null && subSetOf.length() != 0) {
               retType = LOV_TYPE;
               retNode = refDimensionsNodeInInternal;
               definedWhere = LOV_STANDARD;
            }
            // to populate lov from data base - dyn_query
            String dynQuery = refDimensionsNodeInInternal.getAttribute("dyn_query");
            if (dynQuery != null && dynQuery.length() != 0) {
               retType = LOV_TYPE;
               retNode = refDimensionsNodeInInternal;
               definedWhere = LOV_STANDARD;
            }
         }


      }
      if (retNode == null) {
         retNode = refDimensionsNodeInInternal;// this really needs to be in the last so that variables that are not
         definedWhere = INTERNAL_DIMLIST;
         //the initialization is important basically if non lov in dimensions that overrides type found elsewhere
         //but if lov then just take from whereever it was found .. but being lazy so not checking if is
         //found only in dimensions and there  for LOV_NO_TYPE
      }

      if (retNode != null)
         return new MiscInner.PairElemInt(retNode, retType);         
      else
         return null;
  }
   public static String getAttribShortName(int dimId, int valId) {
      DimInfo dim = DimInfo.getDimInfo(dimId);
      if (dim != null) {
          DimInfo.ValInfo valInfo = dim.getValInfo(valId);
          if (valInfo != null)
             return valInfo.m_sn;
      }
      return Integer.toString(valId);
   }
   
   

  

  
  public static int QTY_TYPE_CURRENCY = 0;
  public static int QTY_TYPE_NUMBER = 1;
  public static int QTY_TYPE_PERCENT = 2;
  public static int QTY_TYPE_QTY = 3;
  public static int QTY_TYPE_COUNT = 4;

  //currency=0, 1 = number, 2 = Tonnes, 3 = count, 4 = Years,

  private  void createPartialMetaAndUpdateId(Connection dbConn) throws Exception {
  //name is misnomer coming ... from CapEx
  
       //Add in from the dimension list
       Element dims = MyXMLHelper.getChildElementByTagName(internalXML.getDocumentElement(),"dimensions");
       NodeList childNodes = dims.getChildNodes();
       int chIndex, chCount;
       for (chIndex=0,chCount = childNodes.getLength();chIndex < chCount;chIndex++) {
           Node tempNode = childNodes.item(chIndex);
           if (tempNode.getNodeType() != Node.ELEMENT_NODE)
              continue;
           Element chElem = (Element) tempNode;
//           if (chElem.getAttribute("dimlist").equals("1")) {
           {

               //create the id - note that dimlist only contains standard dimensions
               //custom dimensions id's will be incremented (with custom_dim_start) later on.
               
               MiscInner.PairElemInt attribInfo = getAttribCatNodeNotFromDimList(chElem.getTagName(), chElem);
               if (attribInfo != null){
                   Element attribNode = attribInfo.first;
                   String attribNodeIdStr = attribNode.getAttribute("id");
                   String chElemNodeIdStr = chElem.getAttribute("id");
                   if (attribNodeIdStr == null || attribNodeIdStr.length() == 0) {                   
                       attribNode.setAttribute("id", chElemNodeIdStr);                      
                       attribNode.setAttribute("sn", chElem.getAttribute("sn"));
                   }
               }
            
               String dimsn = chElem.getAttribute("sn");
               String dimname = chElem.getAttribute("name");
               
               String chElemType = chElem.getAttribute("type");
               // sameer 04112006
               //int valType = chElemType.equals("number")?NUMBER_TYPE :
               //                             chElemType.equals("string")?STRING_TYPE:chElemType.equals("date") ? DATE_TYPE: LOV_TYPE;
               //String typeStringForMeta = valType == NUMBER_TYPE?"n":valType == STRING_TYPE?"s": valType == DATE_TYPE ? "d" : "i";
               int valType = chElemType.equals("number")? NUMBER_TYPE :
			                 chElemType.equals("string")? STRING_TYPE :
			                 chElemType.equals("date") ? DATE_TYPE :
			                 chElemType.equals("file") ? FILE_TYPE : chElemType.equals("img") ? IMAGE_TYPE : LOV_TYPE;
               String typeStringForMeta = valType == NUMBER_TYPE?"n":valType == STRING_TYPE?"s": valType == DATE_TYPE ? "d" : valType == FILE_TYPE ? "f" : valType == IMAGE_TYPE ? "p"  : "i";
               // end sameer 04112006
            
            
               String subsetOf = chElem.getAttribute("subset_of");
               if (subsetOf != null && subsetOf.length() != 0) {          
                   Element attribNode = attribInfo.first;
                   attribNode.setAttribute("subset_of", subsetOf);
               }
               String notLoad = chElem.getAttribute("no_load");               
               String precalc = chElem.getAttribute("pre_calc");
               String propOf = chElem.getAttribute("property_of");               
               String showInUI = chElem.getAttribute("show_in_ui");
               String exprString = chElem.getAttribute("expr_string");               
               //Add is_exp and ref_exp_raw="123" info ... 080605
               String isExp = chElem.getAttribute("is_exp");
               String refExpRaw =  chElem.getAttribute("ref_exp_raw");
               String preDef = chElem.getAttribute("pre_def");
               String desc = chElem.getAttribute("desc");
               String colClause = chElem.getAttribute(Misc.G_DO_ORACLE ? "column_orcl" : "column");
               if (Misc.G_DO_ORACLE && (colClause == null || colClause.length() == 0))
                  colClause = chElem.getAttribute("column");
               String joinClause = chElem.getAttribute(Misc.G_DO_ORACLE ? "join_clause_orcl" : "join_clause");
               if (Misc.G_DO_ORACLE && (joinClause == null || joinClause.length() == 0))
                  joinClause = chElem.getAttribute("join_clause");
               String andClause = chElem.getAttribute(Misc.G_DO_ORACLE ? "and_clause_orcl" : "and_clause");
               if (Misc.G_DO_ORACLE && (andClause == null || andClause.length() == 0))
                  andClause = chElem.getAttribute("and_clause");
               ColumnMappingHelper colMap = new ColumnMappingHelper(chElem.getAttribute("table"),
                               colClause,
                               valType,
                               andClause,
                               Misc.getParamAsString(chElem.getAttribute("base_table"), ""),
                               joinClause,
                               "1".equals(chElem.getAttribute("use_col_for_name")),
                               "1".equals(chElem.getAttribute("do_having_filter")),
                               chElem.getAttribute("pur_link_hint"), ////order_to_ccbs, order_to_prj, supp_to_ccbs_awarded, supp_to_ccbs_assoc, supp_to_order_assoc, supp_to_order_awarded
                               "1".equals(chElem.getAttribute("colname_has_agg")),
                               Misc.getParamAsString(chElem.getAttribute("id_field"), ""),
                               Misc.getParamAsString(chElem.getAttribute("name_field"), "")
                               );
               
               int qtyType = Misc.getParamAsInt(chElem.getAttribute("qty_type"), 0);
               int dimId = Misc.getParamAsInt(chElem.getAttribute("id"));
               int refDescDimId = Misc.getParamAsInt(chElem.getAttribute("ref_desc_dim_id"));
               
               String refOrgLevel = chElem.getAttribute("ref_org_level_cat");
               if ("".equals(refOrgLevel))
                   refOrgLevel = null;
               
               String refOrgDescTill =  chElem.getAttribute("ref_org_desc_till"); //if empty then all
               if ("".equals(refOrgDescTill))
                   refOrgDescTill = null;
               
               String refOrgAncTill = chElem.getAttribute("ref_org_anc_till"); //if empty then all
               if ("".equals(refOrgAncTill))
                   refOrgAncTill = null;
                   
               boolean useRepCurrencyByDefault = !"0".equals(chElem.getAttribute("use_rep_currency"));
               

               int refMeasureId = Misc.getParamAsInt(chElem.getAttribute("ref_measure_id"));
               int ignorePastFuture = Misc.getParamAsInt(chElem.getAttribute("ref_ignore_past_or_future"), 0);
               int editPastMon = Misc.getParamAsInt(chElem.getAttribute("edit_past"),0);
               int editFutureMon = Misc.getParamAsInt(chElem.getAttribute("edit_future"),24);
               int lookWhereBaseline = Misc.getParamAsInt(chElem.getAttribute("look_where_baseline"), 0);
               boolean notime = "1".equals(chElem.getAttribute("notime"));
               if (Misc.isUndef(refDescDimId))
                  refDescDimId = dimId;
               if (Misc.isUndef(refMeasureId))
                  refMeasureId = dimId;
               double scale = Misc.getParamAsDouble(chElem.getAttribute("ref_scale"), 1);
               String unitString = chElem.getAttribute("ref_unit");
               int currencyRateList = Misc.getParamAsInt(chElem.getAttribute("currency_rate_list"),1);
               String isGlobalProp = chElem.getAttribute("is_global");
               DimInfo dimInfo = DimInfo.addDimInfo(dbConn, chElem.getTagName(), dimId, colMap, attribInfo.first, attribInfo.second, chElem.getAttribute("default"), qtyType, refDescDimId,refOrgLevel, refMeasureId, ignorePastFuture, editPastMon, editFutureMon, scale, unitString, useRepCurrencyByDefault, notime, dimname, dimsn, Misc.getParamAsInt(subsetOf), chElem, refOrgDescTill, refOrgAncTill, chElem.getAttribute("sub_type"));
               dimInfo.m_lookInThisBaseline = lookWhereBaseline;
               dimInfo.m_useKendo= "1".equals(chElem.getAttribute("use_kendo"));
               
               

           } //if dimlist == 1
       } //for loop ends

       //add dims to DimInfo that can only be found by CatNode
       for (int art = 0;art<3;art++) {
           Element lovNode = null;
           if (art == 0) {
              lovNode = MyXMLHelper.getChildElementByTagName((Element)lovXML.getDocumentElement().getElementsByTagName("standard").item(0), "lov");              
           }
           else if (art == 1){
              lovNode = MyXMLHelper.getChildElementByTagName(internalXML.getDocumentElement(), "lov");              
           }
           else {
              lovNode = MyXMLHelper.getChildElementByTagName(internalXML.getDocumentElement(), "constants");              
           }
           for (Node n=lovNode.getFirstChild();n!=null;n=n.getNextSibling()) {
              if (n.getNodeType() != 1)
                 continue;
              Element e = (Element) n;
              String idStr = e.getAttribute("id");
              if (idStr == null || idStr.length() == 0) {
                  DimInfo.addDimInfo(dbConn, e.getTagName(), Misc.getUndefInt(),null, e, LOV_TYPE, null, 4, Misc.getUndefInt(), null, Misc.getUndefInt(), 0, 0, 0, 1.0f, null, true, true, null, null, Misc.getUndefInt(), null, null,null,null);
              }
           }           
       }
       

       // ValidVal
       
       NodeList nl = lovXML.getElementsByTagName("validval");
       childNodes = nl.item(0).getChildNodes(); // the pair nodes
       for (chIndex=0,chCount = childNodes.getLength(); chIndex < chCount; chIndex++) {
          Node tempNode = childNodes.item(chIndex);
          if (tempNode.getNodeType() != Node.ELEMENT_NODE)
             continue;
        
          Element chNode = (Element) tempNode;
          DimInfo.loadValidVal(chNode);          
       }// end of for for pairs             
  }

  public static String getMenuNextFriendlyName(String tag) { //a generic
     MenuItem menuInfo = MenuItem.getMenuInfo(tag);
     if (menuInfo != null)
        return menuInfo.m_sn;     
     return "";
  }
  public String getMenuUserFriendlyName(String pgContext) {
     MenuItem menuInfo = MenuItem.getMenuInfo(pgContext);
     if (menuInfo != null)
        return menuInfo.m_name;     
     return "";
  }

  public ArrayList getPrivListAvForMenuTag(String menuTagName) {
      return (ArrayList) m_menuTagPrivReq.get(menuTagName);
  }

  

  private HashMap m_menuTagPrivReq = null; //key is menutagname, values ArrayLists of privid - for a given leaf level tagName the priv that require that leaf to be available
  
  private static ConcurrentHashMap<String,Triple<Integer,Integer,Integer>> m_menuTagPrivArtificial = new ConcurrentHashMap<String,Triple<Integer,Integer,Integer>>(500,0.75f); //key is menutagname, values ArrayLists of privid - for a given leaf level tagName the priv that require that leaf to be available
  
  public Triple<Integer,Integer,Integer> getPrivListAvForMenuTagArtificial(String menuTagName){
	  return m_menuTagPrivArtificial.get(menuTagName);
  }
/*  private static ConcurrentHashMap<Integer, Integer> reportForOrg = new ConcurrentHashMap<Integer, Integer>();
  public int getOrgIdBasedOnReportId(int reportId){
	  return reportForOrg.get(reportId);
  }*/
  private void helpLoadPrivDoc() throws Exception {       //TODO - need to add privileges corresponding to graph elements
    if (m_privDoc != null)
       return;
    try {
       FileInputStream inp = new FileInputStream(serverConfigPath+System.getProperty("file.separator")+"privelege.xml");

       MyXMLHelper test = new MyXMLHelper(inp, null);
       m_privDoc =  test.load();
       inp.close();

       helpProcessPrivDocStatus(false); //privilege
//       helpProcessPrivDocStatus(true); // for prj's
	   understandPrivDoc(m_privDoc);
    }
    catch (Exception e) {
       e.printStackTrace();
       throw e;
    }
  }

  private void helpProcessPrivDocStatus(boolean doPrj) {
     //Currently only sets up menu element shown in status page and project status pages

     Element topLevelPrivDocElem = m_privDoc.getDocumentElement();
     Document xmlDoc = doPrj ? m_prjDashBoard : m_dashBoard;
     String prefix = doPrj ? "prj_status_" : "status_";

     for (int i=0;i<2;i++) {
        String elemTag = i == 0? "graph" : "table";
        NodeList viewList = xmlDoc.getElementsByTagName(elemTag);
        for (int j=0, size = viewList.getLength(); j<size;j++) {
           Element view = (Element) viewList.item(j);
           int id = Misc.getParamAsInt(view.getAttribute("id"));
           String name = view.getAttribute("name");
           //get parent dashboard elem
           Element db=null;
           for (db=(Element) view.getParentNode();db != null && !db.getTagName().equals("dash_board");db = (Element) db.getParentNode()) {
           }
           if (db == null)
              continue;
           String dbName = db.getAttribute("name");
           int fullId = id+300000;
           Element priv = m_privDoc.createElement("priv");
           topLevelPrivDocElem.appendChild(priv);

           priv.setAttribute("id", Integer.toString(fullId));
           priv.setAttribute("all_scope", "1");
           priv.setAttribute("portfolio_scope","1");
           StringBuilder privName = new StringBuilder("View dashboard item: ");
           privName.append(name);
           priv.setAttribute("n",privName.toString());
           privName.append(" Under DashBoard: ").append(dbName);
           priv.setAttribute("desc", privName.toString());
           String menuTagName = prefix;
           menuTagName += db.getAttribute("page");
           priv.setAttribute("menu", menuTagName);
        }
     }
  }

  private void helpProcessMenuDocStatus(boolean doPrj) {
  //gotten rid of doPrj parameter
	  	Document xmlDoc = m_dashBoard;
	  	String prefix = "status_";
	  	String linkPage = Misc.G_APP_1_BASE+"status.jsp";

      NodeList tempNL = xmlDoc.getElementsByTagName("meta");
      Element metaElem = tempNL.getLength() == 0 ? null : (Element) tempNL.item(0);
      tempNL = xmlDoc.getElementsByTagName("data");
      Element data = tempNL.getLength() == 0 ? null: (Element) tempNL.item(0);
      if (data == null)
         return;
      MenuItem defaultMenuElem = MenuItem.getMenuInfo("status");      

      for (Node n = data.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() != 1)
            continue;
         Element upDash = (Element) n;
         String tagName = upDash.getTagName();
         if (!"dash_board".equals(tagName) && !"upper_dashboard".equals(tagName))
            continue;
         String upDashName = upDash.getAttribute("name");
         if (upDashName != null && upDashName.startsWith("$_"))
            continue;

         NodeList dashBoardList = null;
         Element firstDBElem = null;

         if ("upper_dashboard".equals(tagName)) {
            dashBoardList = upDash.getElementsByTagName("dash_board");
            if (dashBoardList.getLength() == 0)
               continue;
            firstDBElem = (Element) dashBoardList.item(0);
         }
         else {
            firstDBElem = upDash;
         }
         //get the menuElem below which we have to add and the menuElem whom we have to copy (for parameters etc.)
         MenuItem addBelowElem = null; //after being done with optional upper_dashboard, this will point to upper_dashboardElem menu node just created
         MenuItem copyParamFromAndAddBeforeElem = null; //after being done with optional upper_dashboard, this will point to null
         


         String locId = firstDBElem.getAttribute("loc");
         if (locId == null || locId.length() == 0)
            locId = "0";
         String refLocId = locId;
         if (metaElem != null) {
             Element locElem = MyXMLHelper.getElementById(metaElem, "loc", "id", locId);
             if (locElem != null) {
                 addBelowElem = MenuItem.getMenuInfo(locElem.getAttribute("menu_tag")); 
                 copyParamFromAndAddBeforeElem = MenuItem.getMenuInfo(locElem.getAttribute("ref_tag"));
             }
         }
         if (addBelowElem == null) {
            addBelowElem = defaultMenuElem;
         }
         if (addBelowElem == null)
            continue;
         //create the list of param nodes that each graph being added must have ..
         
         String tagToUseInAccessibility = copyParamFromAndAddBeforeElem != null ? copyParamFromAndAddBeforeElem.m_tag : addBelowElem.m_accCheck;
         if (tagToUseInAccessibility == null)
            tagToUseInAccessibility = addBelowElem.m_tag;
         //paramElemListForMenu
         MenuItem copyParamFromActually = copyParamFromAndAddBeforeElem;

         if ("upper_dashboard".equals(tagName)) {

            String upperLinkElemName = prefix;
            upperLinkElemName += upDash.getAttribute("page");
            MenuItem upperLinkElem = new MenuItem(upperLinkElemName);
            upperLinkElem.m_name = upDashName;
            
            if (copyParamFromAndAddBeforeElem == null)
                addBelowElem.appendChild(upperLinkElem);
            else
                addBelowElem.insertBefore(upperLinkElem,copyParamFromAndAddBeforeElem);

            copyParamFromAndAddBeforeElem = null;
            addBelowElem = upperLinkElem;
         }
         for (int i = 0, is = dashBoardList == null || dashBoardList.getLength() == 0 ? 1 : dashBoardList.getLength(); i < is; i++) {
            Element dashBoard = i == 0 ? firstDBElem : (Element) dashBoardList.item(i);
            String dashName = dashBoard.getAttribute("name");
            if (dashName != null && dashName.startsWith("$_"))
               continue;
            String linkElemName = prefix;
            linkElemName += dashBoard.getAttribute("page");
            MenuItem linkElement = new MenuItem(linkElemName);
            if (tagToUseInAccessibility != null)
               linkElement.m_accCheck = tagToUseInAccessibility;
            if (copyParamFromAndAddBeforeElem == null)
                addBelowElem.appendChild(linkElement);
            else
                addBelowElem.insertBefore(linkElement,copyParamFromAndAddBeforeElem);
            linkElement.m_name = dashName;
            linkElement.m_url = linkPage;
            MenuItem.Param pageParam = new MenuItem.Param("page",dashBoard.getAttribute("page"), 0, true);
            //
            String elemLocId = dashBoard.getAttribute("loc");
            if (elemLocId != null && elemLocId != refLocId) {
             if (metaElem != null) {
                Element locElem = MyXMLHelper.getElementById(metaElem, "loc", "id", elemLocId);
                if (locElem != null) { 
                    MenuItem ti = MenuItem.getMenuInfo(locElem.getAttribute("ref_tag"));
                    if (ti != null)
                       copyParamFromActually = ti;                    
                    refLocId = elemLocId;
                }//valid loc
             }//meta exists
            } //different locid from refLocId
            
            for (int p=0,ps = copyParamFromActually == null || copyParamFromActually.m_params.size() == 0 ? 0 : copyParamFromActually.m_params.size();p<ps;p++) {
               MenuItem.Param param = (MenuItem.Param) copyParamFromActually.m_params.get(p);
               if ("page".equals(param.m_name))
                  continue;
               linkElement.addParam(param.copy());
               
            }
            linkElement.addParam(pageParam);
			if (copyParamFromActually != null)
				linkElement.m_script = copyParamFromActually.m_script;
         } //end of going thru all dashboard elem
      } //end of going thru all upper_dashboard elem
  	}//end of function

	//this is the function that will eventually replace all the info kept otherwise about privilege
	private void understandPrivDoc(Document doc) throws Exception {
		//1. get the read/writes for the tag
		//2. understand the priv info (to what does it apply to) 
		g_tagPrivLookup = new HashMap(500,0.75f);
		g_privDetailsLookup = new HashMap(2000,0.75f);
		Document privDoc = doc;
    
    
    NodeList privList = null;
     //privList = privDoc.getElementsByTagName("priv");
     NodeList privilegeNodeList = privDoc.getElementsByTagName("privelege");
     if (privilegeNodeList != null && privilegeNodeList.getLength() > 0) {
        Element te = (Element)privilegeNodeList.item(0);
        privList = te.getElementsByTagName("priv");
     }


		for (int i = 0, is = privList == null ? 0 : privList.getLength(); i < is; i++)
		{
			Node n = (Node)privList.item(i);
			if (n.getNodeType() != 1)
				continue;
			Element e = (Element)n;
			int id = Misc.getParamAsInt(e.getAttribute("id"));
			String tag = e.getAttribute("action_name");
			if (tag == null || tag.length() == 0)
				tag = e.getAttribute("menu");
			int type = Misc.getParamAsInt(e.getAttribute("read_write"), 3); //0 - read, 1 write. 2 read/write, 3 special

			PrivInfo.TagInfo tagSeen = (PrivInfo.TagInfo)g_tagPrivLookup.get(tag);
			if (tagSeen == null)
			{
				tagSeen = new PrivInfo.TagInfo(tag, id, id, id); //assume 1 id for read/write
				g_tagPrivLookup.put(tag, tagSeen);
			}
			else
			{
				if (type == 0 || type == 2)
					tagSeen.m_read = id;
				if (type == 1 || type == 2)
					tagSeen.m_write = id;
			}
			//THIS MAY NOT BE NEEDED
			int privAppliesTo = Misc.getParamAsInt(e.getAttribute("applies_to"), -1);// means dont know ... do all the checks necessary to get priv applicability
			PrivInfo.PrivDetails privDetails = new PrivInfo.PrivDetails(privAppliesTo, e);
			g_privDetailsLookup.put(new Integer(id), privDetails);
		}//for each node           
    DimInfo.populateAccCheckInfo(this);
	}

  	
	private void helpLoadMenuTagReqForPriv() throws Exception { //with new approach for privilege, menu ... this may not be needed
		//load the menuDocument
    try {
       helpLoadPrivDoc();
       m_menuTagPrivReq = new HashMap(500,0.75f);
       
       
       NodeList privList = null;
       
       //privList = m_privDoc.getElementsByTagName("priv");
       NodeList privilegeNodeList = m_privDoc.getElementsByTagName("privelege");
       if (privilegeNodeList != null && privilegeNodeList.getLength() > 0) {
          Element te = (Element)privilegeNodeList.item(0);
          privList = te.getElementsByTagName("priv");
       }
       
       for (int i=0,size = privList == null ? 0 : privList.getLength();i<size;i++) {
          Element priv = (Element) privList.item(i);
          String menuTagName = priv.getAttribute("menu");
          if (menuTagName != null && menuTagName.length() != 0) {
              ArrayList privAv = (ArrayList) m_menuTagPrivReq.get(menuTagName);
              if (privAv == null) {
                 privAv = new ArrayList();
                 m_menuTagPrivReq.put(menuTagName, privAv);
              }

              privAv.add(new Integer(Misc.getParamAsInt(priv.getAttribute("id"))));
          }
       }
    }
    catch (Exception e) {
       e.printStackTrace();
       throw e;
    }
  }



  
  public static Cache g_cache = null;
  private static void initCacheGlobals() {
     m_globalIsSynced = false;
  }

  public synchronized static Cache getCacheInstance(Connection dbConn) throws Exception {
     if (g_cache == null) {
        initCacheGlobals(); 
        Cache tcache = new Cache(dbConn);
        g_cache = tcache;
        tcache = null;
       // System.gc();
     }
     g_cache.syncGlobal(dbConn);
     return g_cache;
  }
  public synchronized static Cache getCacheInstance() throws GenericException
  {
	  if (g_cache == null) {
	        initCacheGlobals();
	        Cache tcache = null;
	        Connection dbConn = DBConnectionPool.getConnectionFromPoolNonWeb();
			try {
				tcache = new Cache(dbConn);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				DBConnectionPool.returnConnectionToPoolNonWeb(dbConn);
			}
	        g_cache = tcache;
	        tcache = null;
	       // System.gc();
	     }
	     return g_cache;
  }

  public synchronized static void makeCacheDirty(ServletContext application) throws Exception {

     //_log.log("removing attribute _cache");
     application.removeAttribute("_cache");
//     System.out.println(application.getAttribute("_cache"));
     g_cache = null;
  }

  public synchronized static void makeDashboardDirty(ServletContext application) throws Exception {
     makeCacheDirty(application);
  }
  
//added by balwant for inventory Management system
  public synchronized void loadMenuetc(Connection dbConn, String serverName) throws Exception {
      //load the menuDocument
	  FileInputStream inp = null;
      MyXMLHelper test = null;
    
	  try {
		  if (MenuItem.g_menuByTag != null)
			  MenuItem.g_menuByTag.clear();
		  MenuItem.g_currMenuItem = 0;
		  this.invalidateUserMap(-1);
		  
	      inp = new FileInputStream(serverConfigPath+System.getProperty("file.separator")+"menu.xml");
	      test = new MyXMLHelper(inp, null);
	      Document menuXML = test.load();
	      inp.close();
	      inp=null;
	      test = null;
	      MenuItem.helpProcessMenuDoc(menuXML.getDocumentElement(),false);
	      this.helpProcessMenuDocStatus(false); //for portfolio dashboard
	      helpLoadMenuTagReqForPriv();
	      //add by balwant to load Menu Information from databasse...................................
	     if (serverName == null)
	    	 serverName = Misc.getServerName();
     	
	      if (!"node1".equalsIgnoreCase(serverName)) {
	      	updateMenuTreeFromDatabaseIfAny(dbConn);	
		 }
	      
	  }
	  catch (Exception e) {
		  e.printStackTrace();
		  //eat it
	  }
	  finally {
		  try {
			  if (inp != null)
				  inp.close();
		  }
		  catch (Exception e2) {
			  
		  }
	  }
        

  }
  private Cache(Connection dbConn) throws Exception {
//       System.out.println("$$$$$$$ Creating a new Cache");
        try {
            String serverName = Misc.getServerName();
        	System.out.print("@@@@@########@@@@@SWhich Instance Is Running ="+ serverName);
            FileInputStream inp = new FileInputStream(serverConfigPath+System.getProperty("file.separator")+internalName);
            MyXMLHelper test = new MyXMLHelper(inp, null);
            internalXML = test.load();
            Element elem = internalXML.getDocumentElement();
            inp.close();
            inp = null;
            test = null;
            inp = new FileInputStream(serverConfigPath+System.getProperty("file.separator")+lovName);
            test = new MyXMLHelper(inp,null);
            lovXML = test.load();
            inp.close();
            test=null;
            inp=null;
            createPartialMetaAndUpdateId(dbConn);
            m_dashBoard = Misc.getFileFromDB(dbConn, serverConfigPath+System.getProperty("file.separator")+"dashboard.xml", true, false, null);
            inp = new FileInputStream(serverConfigPath+System.getProperty("file.separator")+"prj_dashboard.xml");
            test = new MyXMLHelper(inp,null);
            m_prjDashBoard = test.load();
            inp.close();
            test = null;
            inp = null;

            //load the menuDocument
            loadMenuetc(dbConn, serverName);
              
           
           //added by balwant for inventoryManagement system
        	        	
          
            
            m_privDoc = null;

//            prjDimList = new DimMapList(lovXML, internalXML, partialMetaXML, DimMapInfo.FOR_PROJECT);
//            altDimList = new DimMapList(lovXML, internalXML, partialMetaXML, DimMapInfo.FOR_ALTERNATIVE);
//            System.out.println("Ended Cache");
             NodeList port_node_restrict = internalXML.getElementsByTagName("port_node_restrict"); //TO_PORT_FORWARD ... moved from elsewhere
             if (port_node_restrict != null && port_node_restrict.getLength() > 0) {
                Element se = (Element) port_node_restrict.item(0);
                m_portNodeRestrictor = se.getAttribute("and_or");
             }
             getPortTree(dbConn);
              //CAPEX_REMOVE_TEMP getPortTree(dbConn); //033008 TO_PORT_FORWARD
              Misc.getQueryHint(this);
              
              

              Element classifyElem = (Element) internalXML.getElementsByTagName("classifications").item(0);
              DimInfo.readAllClassifications(classifyElem);

             //CAPEX_REMOVE PrjTemplateHelper.loadTemplateInfo(dbConn, this);
              

         NodeList wrcrit = internalXML.getElementsByTagName("workflow_rule_crit");
         if (wrcrit != null && wrcrit.getLength() > 0) {
            m_workflowCrit.add(new ArrayList());
            ArrayList addInThis = (ArrayList) m_workflowCrit.get(m_workflowCrit.size()-1);
            for (Node n = ((Element)(wrcrit.item(0))).getFirstChild(); n != null ;n = n.getNextSibling()) {
               if (n.getNodeType() != 1)
                  continue;
               Element e = (Element) n;
               int tid = Misc.getParamAsInt(e.getAttribute("id"));
               if (!Misc.isUndef(tid)) {
                  DimConfigInfo dimConfigInfo = DimConfigInfo.getDimConfigInfo(e, true);//new DimConfigInfo();
//                  dimConfigInfo.m_id = tid;
//                  dimConfigInfo.m_dimInfo = DimInfo.getDimInfo(tid);
                  addInThis.add(dimConfigInfo);
               }
            }
         }
         wrcrit = internalXML.getElementsByTagName("workflow_eval_crit");
         if (wrcrit != null && wrcrit.getLength() > 0) {
            m_workflowCrit.add(new ArrayList());
            ArrayList addInThis = (ArrayList) m_workflowCrit.get(m_workflowCrit.size()-1);
            for (Node n = ((Element)(wrcrit.item(0))).getFirstChild(); n != null ;n = n.getNextSibling()) {
               if (n.getNodeType() != 1)
                  continue;
               Element e = (Element) n;
               int tid = Misc.getParamAsInt(e.getAttribute("id"));
               if (!Misc.isUndef(tid)) {
                  DimConfigInfo dimConfigInfo = DimConfigInfo.getDimConfigInfo(e, true);//new DimConfigInfo();
//                  dimConfigInfo.m_id = tid;
//                  dimConfigInfo.m_dimInfo = DimInfo.getDimInfo(tid);
                  addInThis.add(dimConfigInfo);
               }
            }
         }
         m_workflowValGetter = new FrontGetValHelper(m_workflowCrit, true);
         
          //CAPEX_REMOVE StringBuilder wkselClause = new StringBuilder();
          //CAPEX_REMOVE StringBuilder wkfromClause = new StringBuilder();
          //CAPEX_REMOVE StringBuilder wkjoinClause = new StringBuilder();
          //CAPEX_REMOVE wkjoinClause.append(" 1=1 and ");
          //CAPEX_REMOVE ArrayList wkparamCodeDate = null;
          //CAPEX_REMOVE MiscInner.TimeReq wktimeReq = Misc.getEnhPrjSelAttrib(m_workflowValGetter.getToAskDimListNew(), wkselClause, wkfromClause, wkjoinClause, false,false, dbConn, this, false,false,false, false, false); //NOT USING PRJ_PORTFOLIO_MAP JOIN
          //CAPEX_REMOVE StringBuilder wkpjInfo = new StringBuilder();
          //TO_PORT_FORWARD
          //122607 .. see Misc.getEnh* wkpjInfo.append("select ").append(wkselClause).append(" from projects ").append(wkfromClause).append(" where projects.id = ? and workspaces.id = ? and alternatives.id = ? and ").append(wkjoinClause);
          //CAPEX_REMOVE wkpjInfo.append("select ").append(wkselClause).append(" from projects ").append(wkfromClause).append(" where projects.id = ? and pj_map_items.wspace_id = ? and alternatives.id = ? and ").append(wkjoinClause);
          
          //CAPEX_REMOVE m_wktimeReq = wktimeReq;
          //CAPEX_REMOVE m_wkpjInfo = wkpjInfo;
         NodeList envcrit = internalXML.getElementsByTagName("envelope_criteria");

         if (envcrit != null && envcrit.getLength() > 0) {
            for (Node n = ((Element)(envcrit.item(0))).getFirstChild(); n != null ;n = n.getNextSibling()) {
               if (n.getNodeType() != 1)
                  continue;
               Element e = (Element) n;
               int tid = Misc.getParamAsInt(e.getAttribute("id"));
               if (!Misc.isUndef(tid)) {
                  m_envelopeCrit.add(new Integer(tid));
               }
            }
         }

         NodeList serverParam = internalXML.getElementsByTagName("other_server_param");
         if (serverParam != null && serverParam.getLength() > 0) {
            Element se = (Element) serverParam.item(0);
            Misc.G_NEXTBESTBUTTON_ONLY = !"0".equals(se.getAttribute("next_button_only"));
            Misc.G_WIZARD_AT_BOTTOM = !"0".equals(se.getAttribute("wiz_at_bottom"));
            Misc.G_TAKE_PRJ_LOCK = 1 == Misc.getParamAsInt(se.getAttribute("guard_per_project"), Misc.G_TAKE_PRJ_LOCK ? 1 : 0);
         }




        if (true) { //for debug
          processCurrCountryEtcNode(); 
          {
              NodeList tl = internalXML.getElementsByTagName("qty_info");
              if (tl.getLength() != 0) {
                 loadQtyUnitInfo((Element)tl.item(0));
              }
          }
        }
        
        loadUserPrefFields(); //042908
        // Formatting related cache objects
        loadUnitProfileDef();
        loadScaleProfileDef();
        loadFormatProfileDef();
        internalXML = null;
        lovXML = null;
//        getPortTree(dbConn); moved to begininning
        }//end of try
        catch (Exception e) {
        e.printStackTrace();
		   throw e;
           //e.printStackTrace();
        }
  }

public void updateMenuTagReqForPriv(String menuTagName,int artificialPrivId, int userId, int portNodeId ,int menuReportId){
	//m_menuTagPrivArtificial
	// privid for user --start with 1000000, and for portnode --- start with 3000000
	Triple<Integer,Integer,Integer> val = new Triple<Integer,Integer,Integer>(artificialPrivId,userId,portNodeId);
	m_menuTagPrivArtificial.put(menuTagName, val);
	
}
public MenuItem createOptionalMenuItem(String tagName, String menuName, String menu_url){
	MenuItem item = MenuItem.getMenuInfo(tagName);
	if (item == null) {
		item = new MenuItem(tagName,menuName,menu_url);
	}
	return item;
}
public void plugLeafToParentNode(MenuItem childItem, MenuItem parentItem, int afterBefore){
	if (afterBefore == 0 ) {
		parentItem.m_parent.insertAfter(childItem, parentItem);	
	}else if (afterBefore == 1) {
		parentItem.m_parent.insertBefore(childItem, parentItem);
	}
	
}
public void plugInToMenuTree(String afterBeforeTagName, String tagName, String menuName, int afterBeforeIndex,String menu_url,String componentFile){
	
	MenuItem parentMenuItem = MenuItem.getMenuInfo(afterBeforeTagName);
	synchronized (parentMenuItem) {
		MenuItem leafMenuItem = createOptionalMenuItem(tagName, menuName,menu_url);
        addParameters(leafMenuItem, componentFile, tagName,menuName);
		plugLeafToParentNode(leafMenuItem, parentMenuItem, afterBeforeIndex);	
	}
	
	
}
public int getArtificialPrivId(int reportId, int userId, int orgId){

	int artificialPrivId = Misc.getUndefInt();
	if (userId != Misc.getUndefInt() && orgId == Misc.getUndefInt()) {
		artificialPrivId = 1000000 + reportId;
	}else if(userId == Misc.getUndefInt() && orgId != Misc.getUndefInt()) {
		artificialPrivId = 3000000 + reportId;
	}
	return artificialPrivId;
}
private void addParameters(MenuItem leafMenuItem,String component_file,String menuTag, String menuName){
	MenuItem.Param param1 = new MenuItem.Param("front_page",component_file,0,true);
	 MenuItem.Param param2 = new MenuItem.Param("page_context",menuTag.replaceAll("\\s",""),0,true);
	 MenuItem.Param param3 = new MenuItem.Param("dyn","dyn",0,true);
	 MenuItem.Param param4 = new MenuItem.Param("page_name",menuName,0,true);
	 leafMenuItem.addParam(param1);
	 leafMenuItem.addParam(param2);
	 leafMenuItem.addParam(param3);
	 leafMenuItem.addParam(param4);
}

public void insertInMenuTree(SessionManager session,String optionalMenuTag, String afterTag, String beforeTag, String menuTagName ,int userId, int orgId , int reportId , int menuPlaceHolderId,String menu_tag, String component_file, int menumasterId,String menu_url){
	String menuTag = null;
	int artificialPrivId = Misc.getUndefInt();
	artificialPrivId = getArtificialPrivId(reportId, userId, orgId);
    /*if (Misc.isUndef(userId)  && !Misc.isUndef(orgId)) {
    	reportForOrg.put(reportId, orgId);
	}*/
	String removeSpaceOptionalMenuTag = null;
	if (optionalMenuTag != null) {
		removeSpaceOptionalMenuTag = optionalMenuTag.replaceAll("\\s", "");
	}
	if (menu_tag != null) {
	menuTag = menu_tag;	
	}else{
	menuTag = "tr_"+ menuPlaceHolderId+"tr_"+ removeSpaceOptionalMenuTag +"tr_"+ menuTagName.replaceAll("\\s", "") + "tr_" + artificialPrivId;
	}
	String optionalTag = "tr_" + menuPlaceHolderId + "tr_" + removeSpaceOptionalMenuTag ;
	//set menuItem tag and corresponding priv_id in m_menuTagPrivReq .........
	if (menuTagName != null && menuTagName.length() != 0) {
        ArrayList privAv = (ArrayList) m_menuTagPrivReq.get(menuTag);
        if (privAv == null) {
           privAv = new ArrayList();
           m_menuTagPrivReq.put(menuTag, privAv);
        }
        privAv.add(new Integer(artificialPrivId));
    }
	PrivInfo.TagInfo tagSeen = (PrivInfo.TagInfo)g_tagPrivLookup.get(menuTag);
	if (tagSeen == null)
	{
		tagSeen = new PrivInfo.TagInfo(menuTag, artificialPrivId, artificialPrivId, artificialPrivId); //assume 1 id for read/write
		g_tagPrivLookup.put(menuTag, tagSeen);
	}
	//This Is From UI Layer................
	if (session != null) {
		PrivInfo privObj = null;
		HashMap userPriv_OrgMap = session.getUser().getPrivList();
		
		 Integer privIdObj1 = new Integer(artificialPrivId);
         privObj = (PrivInfo) userPriv_OrgMap.get(privIdObj1);
         if (privObj == null) {
             privObj = new PrivInfo();
             userPriv_OrgMap.put(privIdObj1, privObj);
         }
         int userDefaulPort = Misc.getUndefInt();
    	 int userTrackControlOrg = Misc.getUndefInt();
         if (!Misc.isUndef(userId)) {
        	   userDefaulPort = Misc.getParamAsInt((String)session.getUser().getUserPreference(session.getConnection(), false).get("pv123"));
        	   userTrackControlOrg = Misc.getParamAsInt((String)session.getUser().getUserPreference(session.getConnection(), false).get("pv9016"));
		}
         int portId = !Misc.isUndef(orgId) ? orgId : !Misc.isUndef(userDefaulPort) ? userDefaulPort : userTrackControlOrg ; 
         privObj.m_portList.add(new Integer(portId));	
	}
	MenuItem leafMenuItem =  null;
	MenuItem parentMenuItem = null;
	MenuItem optionalMenuItem = null;
	String afterBeforeTagName = null;
	int afterBeforeIndex = Misc.getUndefInt();
	
    //once need to verify with sir about tagName ...........
	//afterTag - 0 , beforeTag - 1
	if ("".equalsIgnoreCase(beforeTag) && afterTag != null) {
		  afterBeforeTagName = afterTag;
		  afterBeforeIndex = 0;
	}else{
		  afterBeforeTagName = beforeTag;
		  afterBeforeIndex = 1;
	}
	
	if (optionalMenuTag == null  || "".equalsIgnoreCase(optionalMenuTag)) {	
		plugInToMenuTree(afterBeforeTagName,menuTag,menuTagName,afterBeforeIndex,menu_url,component_file);
	}else{
		 parentMenuItem = MenuItem.getMenuInfo(optionalTag);
		 if (parentMenuItem != null) {
			 synchronized (parentMenuItem) {
				 leafMenuItem = createOptionalMenuItem(menuTag, menuTagName,menu_url);
				 addParameters(leafMenuItem,component_file,menuTag,menuTagName);
				 parentMenuItem.appendChild(leafMenuItem);
			}
			 
		}else{
			plugInToMenuTree(afterBeforeTagName,optionalTag,optionalMenuTag,afterBeforeIndex,null,null);
			optionalMenuItem = MenuItem.getMenuInfo(optionalTag);
           synchronized (optionalMenuItem) {
        	   leafMenuItem = createOptionalMenuItem(menuTag, menuTagName,menu_url);
        	   addParameters(leafMenuItem, component_file, menuTag,menuTagName);
        	   optionalMenuItem.appendChild(leafMenuItem);	
			}
		}
	}
	/*
	  if (optionalMenuTag == null  || "".equalsIgnoreCase(optionalMenuTag)) {
		  block1 = true;
		  
		  
	}else{
		System.out.println("Hello World");
		 item3  = MenuItem.getMenuInfo(optionalMenuTag);
		 if (item3 != null) {
			 synchronized (item3) {
				  item4 = new MenuItem(menuTagName+artificialPrivId);
				  item4.m_name = menuTagName;
				  item3.appendChild(item4);	
			}
			 
		}else{
		    block1 = true;
			block2 = true;
		  
		}
		
	}
	  if (block1) {
		  String tag = menuTagName;
			 if (block2) {
				tag = optionalMenuTag;
			}
			 if ("".equalsIgnoreCase(beforeTag) && afterTag != null) {
				  MenuItem item1 = MenuItem.getMenuInfo(afterTag);
				  synchronized (item1) {
					  MenuItem item2 = new MenuItem(tag);
					  item2.m_tag = tag +artificialPrivId;
					  item1.m_parent.insertAfter(item2, item1);	
				}
				 
				  
			}else{
				  MenuItem item1 = MenuItem.getMenuInfo(beforeTag);
				  synchronized (item1) {
					  MenuItem item2 = new MenuItem(tag);
					  item2.m_tag = tag + artificialPrivId;
					  item1.m_parent.insertBefore(item2, item1);	
				}
				 
			}
		}
		 if (block2) {
			  item3  = MenuItem.getMenuInfo(optionalMenuTag);
			  item3.m_tag = optionalMenuTag + artificialPrivId;
			  synchronized (item3) {
				  item4 = new MenuItem(menuTagName);
				  item4.m_tag = menuTagName + artificialPrivId;
				  item3.appendChild(item4);	
				}
			  
		}*/
		 //if child is visible then parent will also visible ...... thats why make artificialPriv only for child (menuTagName) not for (optionalMenuTag) ....
		 //verify ????
	updateMenuTagReqForPriv(menuTag,artificialPrivId,userId,orgId,reportId);  

	
	
	
}
public void remove(MenuItem item , String menuName,String menuTag){
	int pos = Misc.getUndefInt();
	int size = item == null ? 0 : item.m_children.size();
	for (int i = 0; i < size; i++) {
        MenuItem item1 = (MenuItem)item.m_children.get(i);
        synchronized (item1) {
		if (item1.m_name.equalsIgnoreCase(menuName)) {
			pos = i;
			break;
		}
       }
	}
	if (pos != Misc.getUndefInt()) {
		synchronized (item) {
			item.m_children.remove(pos);	
			MenuItem.remove(menuTag);
			m_menuTagPrivReq.remove(menuTag);
			g_tagPrivLookup.remove(menuTag);
		}
	}
}
public void updateMenuTreeFromPage(SessionManager session, String optionalMenuTag, String menuTagName, String menuTitleName,int orgId, int userId,long reportId, String newOptionalMenuTagName, int menuPlaceHolderId){
	MenuItem item = null;
	int artificialPrivId = getArtificialPrivId((int)reportId, userId, orgId);
	/*if (Misc.isUndef(userId) && !Misc.isUndef(orgId)) {
		reportForOrg.remove(reportId);	
	}*/
	String menuTag = "tr_"+ menuPlaceHolderId+"tr_"+ optionalMenuTag  +"tr_"+ menuTagName + "tr_" + artificialPrivId;
	String optionalTag = "tr_" + menuPlaceHolderId + "tr_" + optionalMenuTag ;
	String newOptionalTag = "tr_" + menuPlaceHolderId + "tr_" + newOptionalMenuTagName ;
	if (session != null) {
		HashMap privOrgMap = session.getUser().getPrivList();
		privOrgMap.remove(artificialPrivId);
	}
	if (optionalMenuTag != null &&  optionalMenuTag.length() > 0) {
		//System.out.println("hello");
		item = MenuItem.getMenuInfo(optionalTag);
		remove(item,menuTagName,menuTag);
		//if new optional tag is null then we move all its children to as child of its parents
		if (newOptionalMenuTagName == null) {
			ArrayList<MenuItem> childList = item.m_children;
			int childListSize  = childList.size();
			MenuItem itemParent = item.m_parent;
			synchronized (itemParent) {
				for (int i = 0; i < childListSize ; i++) {
					itemParent.m_children.add(childList.get(i));
				}
			}		
			remove(itemParent, optionalMenuTag, optionalTag);
		}else{
			if (item.m_children.size() == 0) {
			remove(item.m_parent, optionalMenuTag, optionalTag);	
			}else{
		    MenuItem.remove(optionalTag);
			m_menuTagPrivReq.remove(optionalTag);
			g_tagPrivLookup.remove(optionalTag);
			/*synchronized (item) {
				item.m_name = newOptionalMenuTagName;//if newoptional tag is not null
				MenuItem.g_menuByTag.put(newOptionalTag, item);
				m_menuTagPrivReq.put(newOptionalTag, artificialPrivId);
				PrivInfo.TagInfo tagSeen = (PrivInfo.TagInfo)g_tagPrivLookup.get(menuTag);
				if (tagSeen == null)
				{
					tagSeen = new PrivInfo.TagInfo(menuTag, artificialPrivId, 1, 1); //assume 1 id for read/write
					g_tagPrivLookup.put(menuTag, tagSeen);
				}
				
			}*/   
		  }
		}
        
	}else{
		 item = MenuItem.getMenuInfo(menuTag);
		 MenuItem item2 = (MenuItem) item.m_parent;
		 remove(item2,menuTagName, menuTag);
	}
	    
	    m_menuTagPrivArtificial.remove(menuTag);
	
}

public void insertMenuTreeFromPage(SessionManager session,int menuPlaceHolderId,String optionalMenuTag, String menuTagName, String menuTitleName,int orgId, int userId,long reportId,int status, String menu_url, String component_file){
	String beforeTag ;
	String afterTag;
	String menu_tag = null;
	int menumasterId = Misc.getUndefInt();
	DimInfo dimInfo = DimInfo.getDimInfo("menu_placeholder");
	ValInfo dimInfo2 = dimInfo.getValInfo(menuPlaceHolderId);
	afterTag = dimInfo2.getOtherProperty("after_tag");
	beforeTag = dimInfo2.getOtherProperty("before_tag");
	insertInMenuTree(session,optionalMenuTag,  afterTag, beforeTag,  menuTagName , userId,  orgId ,  (int)reportId, menuPlaceHolderId,menu_tag,component_file,menumasterId,menu_url);
	
	
}

  public void updateMenuTreeFromDatabaseIfAny(Connection dbConn) throws SQLException{
	  SessionManager session = null;
	
	  PreparedStatement ps  = dbConn.prepareStatement(Queries.GET_MENU_REPORT_INFORMATION);
	  ResultSet rs = ps.executeQuery();
	  try{
	  String beforeTag ;
	  String afterTag;
	  String optionalMenuTag;
	  int menuReportId = Misc.getUndefInt();
	  int userId = Misc.getUndefInt();
	  int portNodeId = Misc.getUndefInt();
	  int menuPlaceHolderId = Misc.getUndefInt();
	  String menuTagName;
	  //menu_tag,component_file,menu_master.id as menumasterId
	  String menu_tag;
	  String component_file;
	  int menumasterId;
	  String menu_url = null;
	  DimInfo dimInfo = DimInfo.getDimInfo("menu_placeholder");
	  while (rs.next()) {
		    menuPlaceHolderId = Misc.getParamAsInt(rs.getString("menu_placeholder_id"));
			menuReportId = Misc.getParamAsInt(rs.getString("id"));
			userId = Misc.getParamAsInt(rs.getString("for_user_id"));
			portNodeId = Misc.getParamAsInt(rs.getString("for_port_node_id"));
			optionalMenuTag = rs.getString("optional_Menu_Name");
			ValInfo dimInfo2 = dimInfo.getValInfo(menuPlaceHolderId);
			afterTag = dimInfo2.getOtherProperty("after_tag");
			beforeTag = dimInfo2.getOtherProperty("before_tag");
		    menuTagName = rs.getString("name");
		    menu_tag = rs.getString("menu_tag");
		    component_file = rs.getString("component_file");
		    menumasterId = rs.getInt("menumasterId");
		    menu_url = rs.getString("page_context");
		    insertInMenuTree(session, optionalMenuTag,  afterTag, beforeTag,  menuTagName , userId,  portNodeId ,  (int)menuReportId,menuPlaceHolderId,menu_tag,component_file,menumasterId,menu_url);   
	}
	  rs.close();
	  rs = null;
	  ps.close();
	  ps = null;
   }catch (Exception e) {
	e.printStackTrace();
}finally{
	try{
if (ps != null) {
	ps.close();
 }
if (rs != null) {
	rs.close();
}
	}catch (Exception e) {
		e.printStackTrace();
	}
}
  }
  public int getAttribVal(String attribCat, String attribName) {
      // returns value for an LOV attribCat, whose name is provided. The Name to be provided
      // is the code name - in the event an attribVal doesn't have code name, then
      // display name field will be used for comparison purposes
      DimInfo dim = DimInfo.getDimInfo(attribCat);
      if (dim != null) {
        DimInfo.ValInfo valInfo = dim.getValInfo(attribName);
        if (valInfo != null)
           return valInfo.m_id;
      }
      return Misc.getUndefInt();
  }

  public int getAttribVal(DimInfo dimInfo, String name) {
     DimInfo.ValInfo valInfo = dimInfo.getValInfo(name);
     if (valInfo == null)
        return Misc.getUndefInt();
     return valInfo.m_id;
        
  }
  public String getAttribCodeName(String attribCat, int attribVal) {
      // returns the code Name for an LOV attrib, whose val is provided. If the code name is
      // not provided, then returns the Display Name (for e.g. attributes in LOV.XML
      DimInfo dim = DimInfo.getDimInfo(attribCat);
      if (dim != null) {
        DimInfo.ValInfo valInfo = dim.getValInfo(attribVal);
        if (valInfo != null)
           return valInfo.m_sn;
      }
      return Integer.toString(attribVal);
  }
  
  public String getAttribDisplayNameFull(Connection dbConn, DimInfo dim, int attribVal) throws Exception {
	  return getAttribDisplayNameFull(null, dbConn, dim, attribVal);
  }
  
  public String getAttribDisplayNameFull(SessionManager session, Connection dbConn, DimInfo dim, int attribVal) throws Exception {
    // returns the display Name for an LOV - if the element doesn;t have a display name, then returns the code name 
     if (dim != null && dim.m_descDataDimId == 123) {
         //HACK ... we do not have dbConn ...
         //do not have a database connection ....
         MiscInner.PortInfo av = getPortNode(dbConn, attribVal);         
         if (av != null)
            return av.m_name;
         return "N/A";
         //return Integer.toString(attribVal);
     }
     else if (dim != null && (dim.m_descDataDimId == 8070 || dim.m_subsetOf == 8070)) {
         return "N/A";//return getFullCCBSName(dbConn, attribVal, null);
         
     }
     DimInfo.ValInfo valInfo = dim.getValInfo(attribVal, dbConn, session);
     if (valInfo != null)
        return valInfo.m_name;
     else
        return Misc.isUndef(attribVal) ?  "" : Integer.toString(attribVal);     
  }
  
  public String getAttribDisplayName(DimInfo dim, int attribVal) throws Exception {
     return getAttribDisplayNameFull(null, dim, attribVal);
  }
  
  public String getAttribDisplayName(String attribCat, int attribVal) throws Exception {     
     DimInfo dim = DimInfo.getDimInfo(attribCat);
     return getAttribDisplayNameFull(null, dim, attribVal);     
  }
  public String getAttribDisplayName(int dimId, int attribVal) throws Exception {
     DimInfo dim = DimInfo.getDimInfo(dimId);
     return getAttribDisplayName(dim, attribVal);
     
  }
  

  

 public int getPortRsetId(Connection _dbConnection, int maj, int min) throws Exception { //remove this from Tracking
      HashMap portVersion = m_portVersion;
      if (portVersion != null) {
         Integer retvalInt = (Integer) portVersion.get(new PortVersionHelper(maj,min));   
         return retvalInt == null ? Misc.getUndefInt() : retvalInt.intValue();
      }
      synchronized (this) {
          m_portVersion = new HashMap(60, 0.75f);
          
          PreparedStatement oStmt = null;
          ResultSet rset = null;
    
          try {      
               oStmt = _dbConnection.prepareStatement(com.ipssi.gen.utils.Queries.GET_PORT_VERSION);
    //             oStmt.setInt(1,HISTORICAL_VERSION_TYPE);
               rset = oStmt.executeQuery();
               
               int countOfNonspecialPortRset = 0;
               while (rset.next()) {
                  //result sets are ordered such that defaults are lower than non-defaults
                  int map_type = rset.getInt(1);
                  int isDefault = rset.getInt(2); //will always be 1
                  int port_wksp_id = rset.getInt(3);
                  String port_wksp_name = rset.getString(4);
                  int port_rset_id = rset.getInt(5);
                  String port_rset_name = rset.getString(6);
                  boolean isRecommended = !Misc.isUndef(Misc.getRsetLong(rset,7));
                  boolean isAuto = Misc.getRsetInt(rset,8) == 1;
                  m_portVersion.put(new PortVersionHelper(port_wksp_id,port_rset_id), new Integer(port_rset_id));
                  //CapEx if (PortHelper.isPredefinedMapType(map_type) || map_type == Misc.MAP_PORT_BASELINE) {
                      m_portVersion.put(new PortVersionHelper(1, -1*map_type), new Integer(port_rset_id));                  
                  //CapEx }
              } // end of while rstmt.next()
              rset.close();
              oStmt.close();
              Integer retvalInt = (Integer) m_portVersion.get(new PortVersionHelper(maj,min));   
              return retvalInt == null ? Misc.getUndefInt() : retvalInt.intValue();
          }
          catch (Exception e) {
             e.printStackTrace();         
             throw e;
          }
      }
  }


  public int getValCount(String dimName) {
     DimInfo dim = DimInfo.getDimInfo(dimName);
     return dim == null ? 0 : dim.getValCount();   
  }
  
  public int getValidatedDimVal(int dimId, int val) {
    if (dimId == 123)
       return val;
     DimInfo dimInfo = DimInfo.getDimInfo(dimId);
     if (dimInfo.m_descDataDimId == 123)
        return val;
     if (dimInfo.m_subsetOf == 99 || dimInfo.m_subsetOf == 203 || dimInfo.m_subsetOf == 8070 || dimInfo.m_subsetOf == 413 || dimInfo.m_subsetOf == 414)
       return val;
     int attribType = dimInfo.getAttribType();
     if (attribType == Cache.LOV_NO_VAL_TYPE || attribType == Cache.INTEGER_TYPE || attribType == Cache.NUMBER_TYPE || attribType == Cache.FILE_TYPE || attribType == Cache.IMAGE_TYPE)
        return val;
     DimInfo.ValInfo v = dimInfo.getValInfo(val);
     
     if (v == null)
        val = dimInfo.getDefaultInt();
     return val;
  }
  public int getDefaultDimVal(int dimId){
       DimInfo dimInfo = DimInfo.getDimInfo(dimId);
       return dimInfo.getDefaultInt();
  }
  public int getDefaultDimVal(String dimName) {
       DimInfo dimInfo = DimInfo.getDimInfo(dimName);
       if (dimInfo != null)
          return dimInfo.getDefaultInt();
       else
          return Misc.getUndefInt();
  }
  
  public int printDimVals(String dimName, int selectedId, JspWriter out) throws IOException {
     return printDimVals(dimName, selectedId, out, false);     
 }

  public int printDimVals(String dimName, int selectedId, JspWriter out, boolean globalUseAltName) throws IOException {
     DimInfo dimInfo = DimInfo.getDimInfo(dimName);
     return printDimVals(dimInfo, selectedId, out, globalUseAltName);     
  }

  public int printDimVals(DimInfo dimInfo, int selectedId, JspWriter out, boolean globalUseAltName) throws IOException {
	  return printDimVals(dimInfo, selectedId, out, globalUseAltName, false, null);
  }
  public int printDimVals(DimInfo dimInfo, int selectedId, JspWriter out, boolean globalUseAltName, boolean doAny, String anyInstruct) throws IOException {
	    
	     boolean isFirst = true;
	     int retval = selectedId;
	     boolean doFirst = false;
	     if (Misc.isUndef(selectedId)) {
	         doFirst = true;
	     }
	     if (doAny) {
	    	 if (anyInstruct == null || anyInstruct.length() == 0)
	    		 anyInstruct = "&lt; Select &gt;";
	    	
	    	 out.println("<option "+(doFirst?"selected":"")+">"+anyInstruct+"</option>");
	    	 isFirst = false;
	     }
	     if(dimInfo == null)
	    	 return retval;
	     ArrayList valList = dimInfo.getValList();
	     for (int i=0,count=valList == null ? 0 : valList.size();i<count;i++) {
	        
	        DimInfo.ValInfo valInfo = (DimInfo.ValInfo) valList.get(i);
	        int id = valInfo.m_id;
	        String nameStr = null;
	        if (globalUseAltName) {
	            nameStr = valInfo.getOtherProperty("alt_name");
	            if (nameStr == null || nameStr.length() == 0)
	               nameStr = valInfo.m_name;
	        }
	        else {
	            nameStr = valInfo.m_name;
	        }

	          if (isFirst) {
	             retval = id;

	          }
	          boolean doSelected = id == selectedId;
	          if (doSelected)
	             retval = id;
	          if (isFirst && !doSelected && doFirst)
	             doSelected = true;
	          isFirst = false;
	        if (valInfo.m_isSp)
	           continue;
	        out.println("<option value=\""+id+"\" "+(doSelected?"selected":"")+">"+nameStr+"</option>");
	     }
	     
	     return retval;  
	  }
  
  public int printDimVals(SessionManager session, Connection dbConn, DimInfo dimInfo, int selectedId, JspWriter out, boolean globalUseAltName, boolean doAny, String anyInstruct) throws Exception {
    
     boolean isFirst = true;
     int retval = selectedId;
     boolean doFirst = false;
     if (Misc.isUndef(selectedId)) {
         doFirst = true;
     }
     if (doAny) {
    	 if (anyInstruct == null || anyInstruct.length() == 0)
    		 anyInstruct = "&lt; Select &gt;";
    	
    	 out.println("<option "+(doFirst?"selected":"")+">"+anyInstruct+"</option>");
    	 isFirst = false;
     }
     
     ArrayList valList = dimInfo.getValList(dbConn, session);
     for (int i=0,count=valList == null ? 0 : valList.size();i<count;i++) {
        
        DimInfo.ValInfo valInfo = (DimInfo.ValInfo) valList.get(i);
        int id = valInfo.m_id;
        boolean toNotSkip = id == selectedId || valInfo.hasPriv(session);
        if (!toNotSkip)
        	continue;
        String nameStr = null;
        if (globalUseAltName) {
            nameStr = valInfo.getOtherProperty("alt_name");
            if (nameStr == null || nameStr.length() == 0)
               nameStr = valInfo.m_name;
        }
        else {
            nameStr = valInfo.m_name;
        }

          if (isFirst) {
             retval = id;

          }
          boolean doSelected = id == selectedId;
          if (doSelected)
             retval = id;
          if (isFirst && !doSelected && doFirst)
             doSelected = true;
          isFirst = false;
        if (valInfo.m_isSp)
           continue;
        out.println("<option value=\""+id+"\" "+(doSelected?"selected":"")+">"+nameStr+"</option>");
     }
     
     return retval;  
  }
  public static MiscInner.PairStr getLocationDisplayInfo(Connection dbConn, int projectId) throws Exception {
	  try {
	         String name = "";
	         String code = "";
	         PreparedStatement ps = dbConn.prepareStatement("select landmarks.id, landmarks.name from landmarks where landmarks.id = ?");
	         ps.setInt(1, projectId);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next()) {
	             code = rs.getString(1);
	             name = rs.getString(2);
	         }
	         rs.close();
	         ps.close();
	         return new MiscInner.PairStr(name, code);
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	     }	  
  }
  public static MiscInner.PairStr getRegionDisplayInfo(Connection dbConn, int projectId) throws Exception {
	  try {
	         String name = "";
	         String code = "";
	         PreparedStatement ps = dbConn.prepareStatement("select regions.id, regions.short_code from regions where regions.id = ?");
	         ps.setInt(1, projectId);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next()) {
	             code = rs.getString(1);
	             name = rs.getString(2);
	         }
	         rs.close();
	         ps.close();
	         return new MiscInner.PairStr(name, code);
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	     }
  }
  public static MiscInner.PairStr getUserDisplayInfo(Connection dbConn, int projectId) throws Exception {
     try {
         String name = "";
         String code = "";
         PreparedStatement ps = dbConn.prepareStatement("select users.id, users.name from users where users.id = ?");
         ps.setInt(1, projectId);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             code = rs.getString(1);
             name = rs.getString(2);
         }
         rs.close();
         ps.close();
         return new MiscInner.PairStr(name, code);
     }
     catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
  }
  
  public static MiscInner.PairStr getVehicleDisplayInfo(Connection dbConn, int projectId) throws Exception {
	     try {
	         String name = "";
	         String code = "";
	         PreparedStatement ps = dbConn.prepareStatement("select vehicle.id, vehicle.name from vehicle where vehicle.id = ?");
	         ps.setInt(1, projectId);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next()) {
	             code = rs.getString(1);
	             name = rs.getString(2);
	         }
	         rs.close();
	         ps.close();
	         return new MiscInner.PairStr(name, code);
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	     }
	  }
	  
  public static MiscInner.PairStr getDriverDisplayInfo(Connection dbConn, int projectId) throws Exception {
	     try {
	         String name = "";
	         String code = "";
	         PreparedStatement ps = dbConn.prepareStatement("select driver_details.id, driver_details.driver_name from driver_details where driver_details.id = ?");
	         ps.setInt(1, projectId);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next()) {
	             code = rs.getString(1);
	             name = rs.getString(2);
	         }
	         rs.close();
	         ps.close();
	         return new MiscInner.PairStr(name, code);
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	     }
	  }

  public static MiscInner.PairStr getDORRDisplayInfo(Connection dbConn, int projectId) throws Exception {
	     try {
	         String name = "";
	         String code = "";
	         PreparedStatement ps = dbConn.prepareStatement("select do_rr_details.id, do_rr_details.do_rr_number from do_rr_details where do_rr_details.id = ?");
	         ps.setInt(1, projectId);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next()) {
	             code = rs.getString(1);
	             name = rs.getString(2);
	         }
	         rs.close();
	         ps.close();
	         return new MiscInner.PairStr(name, code);
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	     }
  }
  public static MiscInner.PairStr getPOLineDisplayInfo(Connection dbConn, int projectId) throws Exception {
	     try {
	         String name = "";
	         String code = "";
	         PreparedStatement ps = dbConn.prepareStatement("select po_line_item.id, concat(po_no,'/Line:',po_line) from po_line_item where id = ?");
	         ps.setInt(1, projectId);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next()) {
	             code = rs.getString(1);
	             name = rs.getString(2);
	         }
	         rs.close();
	         ps.close();
	         return new MiscInner.PairStr(name, code);
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	     }
	  }
	 	 
  public static MiscInner.PairStr getMultiAttribDisplayInfo(Connection dbConn, int itemId, int selectedId, DimInfo dimInfo) throws Exception {
  //query must be of the form of "
     try {
         String name = "";
         String code = "";
         String baseTable = dimInfo == null || dimInfo.m_colMap == null ? "supplier_multi_attrib" : dimInfo.m_colMap.table;
         boolean doingPrj = "prj_multi_attrib".equals(baseTable);
         boolean doingOrder = "order_multi_attrib".equals(baseTable);
         boolean doingSupp = "supplier_multi_attrib".equals(baseTable);
         String fkey = doingPrj ? "prj_id" : doingOrder ? "order_id" : "supplier_id";
         String queryToLookupFlexMultiAttrib = "select row_num, str_val from "+baseTable+" where "+fkey+" = ? and attrib_id = ? and row_num = ? ";
         PreparedStatement ps = dbConn.prepareStatement(queryToLookupFlexMultiAttrib);
         ps.setInt(1, itemId);
         ps.setInt(2, dimInfo.m_id);
         ps.setInt(3, selectedId);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             code = rs.getString(1);
             name = rs.getString(2);
         }
         rs.close();
         ps.close();
         return new MiscInner.PairStr(name, code);
     }
     catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
  }
  public static MiscInner.PairStr getProjectDisplayInfo(Connection dbConn, int projectId) throws Exception {
     try {
         String name = "";
         String code = "";
         PreparedStatement ps = dbConn.prepareStatement(Queries.GET_PRJ_TOP_LINE_INFO);
         ps.setInt(1, projectId);
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
             code = rs.getString(2);
             name = rs.getString(3);
         }
         rs.close();
         ps.close();
         return new MiscInner.PairStr(name, code);
     }
     catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
  }
 
  public static MiscInner.PairStr getGeneralParamDisplayInfo(Connection dbConn, int selectedId, int parameterId) throws Exception {
     try {
          
        		PreparedStatement ps = dbConn.prepareStatement(com.ipssi.gen.utils.Queries.GET_GENERAL_PARAM_NAME);
        		ps.setInt(1, parameterId);
            ps.setInt(2, selectedId);
            
        		ResultSet rs = ps.executeQuery();
            String name = "";
            String id = "";
        		if (rs.next()) {
        			name = Misc.getRsetString(rs, 2, "");
              id = Misc.getRsetString(rs,1,"");        			
        		}

        		rs.close();
        		ps.close();        		
        	
          return new MiscInner.PairStr(name, id);
     }
     catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
  }
  public static MiscInner.PairStr getFileDisplayInfo(Connection dbConn, int fileId) throws Exception {
     try {
        	String fileName = "";
        	String originalFileName = "";
        	String fileLink = "";
        	if (! Misc.isUndef(fileId)) {
        		PreparedStatement fileStmt = dbConn.prepareStatement(com.ipssi.gen.utils.Queries.GET_FILE_NAME_FROM_ID);
        		fileStmt.setInt(1, fileId);
        		ResultSet fileRset = fileStmt.executeQuery();

        		if (fileRset.next()) {
        			fileName = Misc.getRsetString(fileRset, 1, "");
					fileLink = (fileName.equals("")) ? "" : Misc.getUserUploadFileURL(fileId);//"user_files/business_plans/" + fileName;
        			originalFileName = Misc.getRsetString(fileRset, 2, "");
        		}

        		fileRset.close();
        		fileStmt.close();
        		fileRset = null;
        		fileStmt = null;
        	}
          return new MiscInner.PairStr(originalFileName, fileLink);
     }
     catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
  }
  public static MiscInner.PairStr getDisplayInfo(Connection dbConn,int itemId, DimInfo dimInfo) throws Exception {
	  //query must be of the form of "
	     try {
	         String name = "";
	         String code = "";
	         String baseTable = null;
	         String nameField = null;
	         String idField = null;
	         if(dimInfo != null && dimInfo.m_colMap != null){
	        	 baseTable =  dimInfo.m_colMap.base_table;
	        	 idField =  dimInfo.m_colMap.idField;
	        	 nameField =  dimInfo.m_colMap.nameField;
	         }
	         if(Misc.isUndef(itemId) || baseTable == null || baseTable.length() <= 0 || idField == null || idField.length() <= 0 || nameField == null || nameField.length() <= 0 )
	        	 return null;
	         String query = "select "+idField+", "+nameField+" from "+baseTable+" where "+idField+" = ? ";
	         PreparedStatement ps = dbConn.prepareStatement(query);
	         ps.setInt(1, itemId);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next()) {
	             code = rs.getString(1);
	             name = rs.getString(2);
	         }
	         rs.close();
	         ps.close();
	         return new MiscInner.PairStr(name, code);
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	     }
	  }
  public static MiscInner.PairStr getDisplayInfoByCode(Connection dbConn,String selectedCode, DimInfo dimInfo) throws Exception {
	  //query must be of the form of "
	     try {
	         String name = "";
	         String code = "";
	         String baseTable = null;
	         String nameField = null;
	         String idField = null;
	         if(dimInfo != null && dimInfo.m_colMap != null){
	        	 baseTable =  dimInfo.m_colMap.base_table;
	        	 idField =  dimInfo.m_colMap.idField;
	        	 nameField =  dimInfo.m_colMap.nameField;
	         }
	         if(selectedCode == null || selectedCode.length() <= 0 || baseTable == null || baseTable.length() <= 0 || idField == null || idField.length() <= 0 || nameField == null || nameField.length() <= 0 )
	        	 return new MiscInner.PairStr(name, code);
	         String query = "select "+idField+", "+nameField+" from "+baseTable+" where "+idField+" = ? ";
	         PreparedStatement ps = dbConn.prepareStatement(query);
	         ps.setString(1, selectedCode);
	         ResultSet rs = ps.executeQuery();
	         if (rs.next()) {
	             code = rs.getString(1);
	             name = rs.getString(2);
	         }
	         rs.close();
	         ps.close();
	         return new MiscInner.PairStr(name, code);
	     }
	     catch (Exception e) {
	         e.printStackTrace();
	         throw e;
	     }
	  }
  public int printDimVals(Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, JspWriter out, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize, boolean globalUseAltName, String addnlSearchPortParam, MiscInner.ContextInfo contextInfo) throws Exception {
     StringBuilder op = new StringBuilder();
     int retval = printDimVals(dbConn, user, dimInfo, selectedId, otherSels, op, varName, toPrintAny, printAnyValLabel, printMultiSelect, helperId, vsize, hsize, globalUseAltName, addnlSearchPortParam, contextInfo);
     out.println(op);
     return retval;
  }
  
  public int printDimVals(Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize, boolean globalUseAltName, String addnlSearchPortParam, MiscInner.ContextInfo contextInfo) throws Exception {
     return printDimVals(dbConn, user, dimInfo, selectedId, otherSels, buf, varName, toPrintAny, printAnyValLabel, printMultiSelect, helperId, vsize, hsize, globalUseAltName, addnlSearchPortParam, false, false, false, null, null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo);
  }
  
  public int printDimVals(SessionManager session, Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize, boolean globalUseAltName, String addnlSearchPortParam, MiscInner.ContextInfo contextInfo) throws Exception {
	     return printDimVals(session, dbConn, user, dimInfo, selectedId, otherSels, buf, varName, toPrintAny, printAnyValLabel, printMultiSelect, helperId, vsize, hsize, globalUseAltName, addnlSearchPortParam, false, false, false, null, null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo);
	  }
  public int printDimVals(SessionManager session, Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize, boolean globalUseAltName, String addnlSearchPortParam, MiscInner.ContextInfo contextInfo, int printNullNotNullLOV) throws Exception {
	     return printDimVals(session, dbConn, user, dimInfo, selectedId, otherSels, buf, varName, toPrintAny, printAnyValLabel, printMultiSelect, helperId, vsize, hsize, globalUseAltName, addnlSearchPortParam, false, false, false, null, null, Misc.getUndefInt(), Misc.getUndefInt(), contextInfo,printNullNotNullLOV);
	  }

  public int printDimVals(Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize
          , boolean globalUseAltName, String addnlSearchPortParam, boolean printUseInstruct, boolean doingInTableForMultiVal, boolean doReadOnly, String onChangeHandler, String addnlParamForPortCollectorLike, int addnlParamForLookupFlexMultiAttrib, int valsToPrintCustomHint
          , MiscInner.ContextInfo contextInfo
          ) throws Exception {
	  return printDimVals(null, dbConn,  user,  dimInfo,  selectedId,  otherSels,  buf,  varName,  toPrintAny,   printAnyValLabel,  printMultiSelect,  helperId,  vsize,  hsize
              ,  globalUseAltName,  addnlSearchPortParam,  printUseInstruct,  doingInTableForMultiVal,  doReadOnly,  onChangeHandler,  addnlParamForPortCollectorLike,  addnlParamForLookupFlexMultiAttrib,  valsToPrintCustomHint
              ,  contextInfo
              );
  }
  public int printDimVals(Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize
          , boolean globalUseAltName, String addnlSearchPortParam, boolean printUseInstruct, boolean doingInTableForMultiVal, boolean doReadOnly, String onChangeHandler, String addnlParamForPortCollectorLike, int addnlParamForLookupFlexMultiAttrib, int valsToPrintCustomHint
          , MiscInner.ContextInfo contextInfo, int printNullIsNotNullLOV
          ) throws Exception {
	  return printDimVals(null, dbConn,  user,  dimInfo,  selectedId,  otherSels,  buf,  varName,  toPrintAny,   printAnyValLabel,  printMultiSelect,  helperId,  vsize,  hsize
              ,  globalUseAltName,  addnlSearchPortParam,  printUseInstruct,  doingInTableForMultiVal,  doReadOnly,  onChangeHandler,  addnlParamForPortCollectorLike,  addnlParamForLookupFlexMultiAttrib,  valsToPrintCustomHint
              ,  contextInfo, printNullIsNotNullLOV
              );
  }
  public int printDimVals(SessionManager session, Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize
          , boolean globalUseAltName, String addnlSearchPortParam, boolean printUseInstruct, boolean doingInTableForMultiVal, boolean doReadOnly, String onChangeHandler, String addnlParamForPortCollectorLike, int addnlParamForLookupFlexMultiAttrib, int valsToPrintCustomHint
          , MiscInner.ContextInfo contextInfo
          ) throws Exception {
	  return printDimVals(session, dbConn, user, dimInfo, selectedId, otherSels, buf, varName, toPrintAny,  printAnyValLabel, printMultiSelect, helperId, vsize, hsize
	          , globalUseAltName, addnlSearchPortParam, printUseInstruct, doingInTableForMultiVal, doReadOnly, onChangeHandler, addnlParamForPortCollectorLike, addnlParamForLookupFlexMultiAttrib, valsToPrintCustomHint
	          , contextInfo, null);
  }
  public int printDimVals(SessionManager session, Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize
          , boolean globalUseAltName, String addnlSearchPortParam, boolean printUseInstruct, boolean doingInTableForMultiVal, boolean doReadOnly, String onChangeHandler, String addnlParamForPortCollectorLike, int addnlParamForLookupFlexMultiAttrib, int valsToPrintCustomHint
          , MiscInner.ContextInfo contextInfo, int printIsNullNotNullLOV
          ) throws Exception {
	  return printDimVals(session, dbConn, user, dimInfo, selectedId, otherSels, buf, varName, toPrintAny,  printAnyValLabel, printMultiSelect, helperId, vsize, hsize
	          , globalUseAltName, addnlSearchPortParam, printUseInstruct, doingInTableForMultiVal, doReadOnly, onChangeHandler, addnlParamForPortCollectorLike, addnlParamForLookupFlexMultiAttrib, valsToPrintCustomHint
	          , contextInfo, null, printIsNullNotNullLOV);
  }
  public int printDimVals(SessionManager session, Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize
          , boolean globalUseAltName, String addnlSearchPortParam, boolean printUseInstruct, boolean doingInTableForMultiVal, boolean doReadOnly, String onChangeHandler, String addnlParamForPortCollectorLike, int addnlParamForLookupFlexMultiAttrib, int valsToPrintCustomHint
          , MiscInner.ContextInfo contextInfo, String nonLovFormattedVal
          ) throws Exception{
	  return printDimVals(session, dbConn, user, dimInfo, selectedId, otherSels, buf, varName, toPrintAny,  printAnyValLabel, printMultiSelect, helperId, vsize, hsize
	          , globalUseAltName, addnlSearchPortParam, printUseInstruct, doingInTableForMultiVal, doReadOnly, onChangeHandler, addnlParamForPortCollectorLike, addnlParamForLookupFlexMultiAttrib, valsToPrintCustomHint
	          , contextInfo, nonLovFormattedVal,false, 0, null);
  }
  public int printDimVals(SessionManager session, Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize
          , boolean globalUseAltName, String addnlSearchPortParam, boolean printUseInstruct, boolean doingInTableForMultiVal, boolean doReadOnly, String onChangeHandler, String addnlParamForPortCollectorLike, int addnlParamForLookupFlexMultiAttrib, int valsToPrintCustomHint
          , MiscInner.ContextInfo contextInfo, String nonLovFormattedVal, int printIsNullIsNotNullLOV
          ) throws Exception{
	  return printDimVals(session, dbConn, user, dimInfo, selectedId, otherSels, buf, varName, toPrintAny,  printAnyValLabel, printMultiSelect, helperId, vsize, hsize
	          , globalUseAltName, addnlSearchPortParam, printUseInstruct, doingInTableForMultiVal, doReadOnly, onChangeHandler, addnlParamForPortCollectorLike, addnlParamForLookupFlexMultiAttrib, valsToPrintCustomHint
	          , contextInfo, nonLovFormattedVal,false, 0, null, printIsNullIsNotNullLOV);
  }
  public int printDimVals(SessionManager session, Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize
          , boolean globalUseAltName, String addnlSearchPortParam, boolean printUseInstruct, boolean doingInTableForMultiVal, boolean doReadOnly, String onChangeHandler, String addnlParamForPortCollectorLike, int addnlParamForLookupFlexMultiAttrib, int valsToPrintCustomHint
          , MiscInner.ContextInfo contextInfo, String nonLovFormattedVal
          ,boolean isRadio) throws Exception {
	  return printDimVals(session, dbConn, user, dimInfo, selectedId, otherSels, buf, varName, toPrintAny,  printAnyValLabel, printMultiSelect, helperId, vsize, hsize
              , globalUseAltName, addnlSearchPortParam, printUseInstruct, doingInTableForMultiVal, doReadOnly, onChangeHandler, addnlParamForPortCollectorLike, addnlParamForLookupFlexMultiAttrib, valsToPrintCustomHint
              , contextInfo, nonLovFormattedVal
              ,isRadio, 0, null);
  }
  public int printDimVals(SessionManager session, Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize
          , boolean globalUseAltName, String addnlSearchPortParam, boolean printUseInstruct, boolean doingInTableForMultiVal, boolean doReadOnly, String onChangeHandler, String addnlParamForPortCollectorLike, int addnlParamForLookupFlexMultiAttrib, int valsToPrintCustomHint
          , MiscInner.ContextInfo contextInfo, String nonLovFormattedVal
          ,boolean isRadio, int printValidation, DimConfigInfo dci) throws Exception {
	  return printDimVals(session, dbConn, user, dimInfo, selectedId, otherSels, buf, varName, toPrintAny,  printAnyValLabel, printMultiSelect, helperId, vsize, hsize
              , globalUseAltName, addnlSearchPortParam, printUseInstruct, doingInTableForMultiVal, doReadOnly, onChangeHandler, addnlParamForPortCollectorLike, addnlParamForLookupFlexMultiAttrib, valsToPrintCustomHint
              , contextInfo, nonLovFormattedVal
              ,isRadio, printValidation, dci,0);  
  }
  public int printDimVals(SessionManager session, Connection dbConn, User user, DimInfo dimInfo, int selectedId, ArrayList otherSels, StringBuilder buf, String varName, boolean toPrintAny,  String printAnyValLabel, boolean printMultiSelect, int helperId, int vsize, int hsize
                            , boolean globalUseAltName, String addnlSearchPortParam, boolean printUseInstruct, boolean doingInTableForMultiVal, boolean doReadOnly, String onChangeHandler, String addnlParamForPortCollectorLike, int addnlParamForLookupFlexMultiAttrib, int valsToPrintCustomHint
                  , MiscInner.ContextInfo contextInfo, String nonLovFormattedVal
                  ,boolean isRadio, int printValidation, DimConfigInfo dci, int printIsNullIsNotNullLOV) throws Exception {
     //valsToPrintCustomHint if not undef will be passed to CustomImpl.checkIfValToPrint() and the val will be printed only if that returns tru
     // the above is applicable only for dims that are going to be shown in sel form
     // the above was intended for printing the project status and order status, but is no longer being used 
     //will be smart to print the org selector or the dimval list in an appropriate manner
     //SPECIAL CASES -> portfolio ....
     //              -> project selector
     //              -> file upload/download
     //              -> User Selector
     //              -> make the general_param_based list more configurable
	  //             -> vehicle selector
	  //             -> driver id selector
	  
     //varName == null && doReadOnly => only the display portion is printed
	  StringBuilder validationHandler = null;
	  String mandPart = null;
	  if (printValidation == 1 && dci != null) {
		  if (dci.m_isMandatory) {
			  mandPart = " _mand=\"1\" ";
		  }
		  else if (dci.m_mandatoryGroup >= 0) {
			  mandPart = " _mand_group=\""+dci.m_mandatoryGroup+"\" ";
		  }
		  if (dci.m_uniqueGroup >= 0) {
			  mandPart = " _unique_group=\""+dci.m_uniqueGroup+"\" ";
		  }
		  if (dci.m_oneOfAllGrouping != null && dci.m_oneOfAllGrouping.length() > 0) {
			  mandPart = " _one_of=\""+dci.m_oneOfAllGrouping+"\" ";
		  }
	  }
	  if (printValidation == 1 || printValidation == 2) {
		  int validMin = dci == null ? Misc.getUndefInt() : dci.getMinVal();
		  int validMax = dci == null ? Misc.getUndefInt() : dci.getMaxVal();
		  String validPattern = dci == null ? null : dci.getAllowedPattern();
		  if (dimInfo != null && Misc.isUndef(validMin)) {
			  validMin = dimInfo.getMinVal();
		  }
		  if (dimInfo != null && Misc.isUndef(validMax)) {
			  validMax = dimInfo.getMaxVal();
		  }
		  if (dimInfo != null && validPattern == null) {
			  validPattern = dimInfo.getAllowedPattern();
		  }
		  if (!Misc.isUndef(validMin) || !Misc.isUndef(validMax) || validPattern != null || (dimInfo.m_type != Cache.STRING_TYPE && dimInfo.m_type != Cache.DATE_TYPE)) {
			  validationHandler = new StringBuilder();
			  validationHandler.append("_validateInput(event,").append(validMin).append(",").append(validMax).append(",\"").append(validPattern).append("\",").append(dimInfo == null ? Misc.getUndefInt() : dimInfo.m_type).append(")");
		  }
	  }
	  if (onChangeHandler != null && onChangeHandler.length() == 0)
		  onChangeHandler = null;
	  if (Misc.isUndef(selectedId) && otherSels != null && otherSels.size() > 0)
		  selectedId = (Integer)otherSels.get(0);
     boolean isGeneralParam = dimInfo.m_id == 8086;//dimInfo.m_id == 126; //actually 126 is wrong ... will lead to backward comptissue
     boolean isMultiAttribSearchParam = dimInfo.m_id == 8085; //todo somehow make it dynamic
     int otherSelSz = otherSels == null ? 0 : otherSels.size();
     if (Misc.isUndef(selectedId) &&  otherSelSz > 1)
        selectedId = ((Integer)otherSels.get(0)).intValue();
     boolean hasAny = toPrintAny && this.hasAnyVal(selectedId, otherSels, dimInfo);
    
     if (hasAny) {
           if (otherSels != null) {
              otherSels.clear();
              otherSels.add(new Integer(Misc.G_HACKANYVAL));
			  otherSelSz = otherSels.size();
           }
           selectedId = Misc.G_HACKANYVAL;
     }
     if (printMultiSelect) {
        boolean doingSpecial = isMultiAttribSearchParam || isGeneralParam || dimInfo.m_descDataDimId == 123 || dimInfo.m_subsetOf == 8070 || dimInfo.m_descDataDimId == 8070 || dimInfo.getAttribType() == FILE_TYPE || dimInfo.getAttribType() == IMAGE_TYPE
                          ||dimInfo.m_subsetOf == 99 || dimInfo.m_subsetOf == 203 || dimInfo.m_subsetOf == 20274 || dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM || dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM
                          || dimInfo.m_subsetOf == 413 //location picker
                          || dimInfo.m_subsetOf == 414
                          ;
        if (doingSpecial) {
           boolean printSeparateAddRow = dimInfo.m_descDataDimId != 123 && !(dimInfo.m_subsetOf == 8070 || dimInfo.m_descDataDimId == 8070);
           int otherSelSize = otherSels == null ? 0 : otherSels.size();
           int rowcnt = otherSelSize == 0 ? (Misc.isUndef(selectedId) ? 0 : 1) : otherSelSize;
           if (printSeparateAddRow || rowcnt == 0)
              rowcnt++;
           buf.append("<table cellspacing='0' cellpadding='0' border='0'>");
           int retval = selectedId;
           for (int l=0;l<rowcnt;l++) {
              
              int v = l < otherSelSize ? ((Integer)otherSels.get(l)).intValue() : !Misc.isUndef(selectedId) ? selectedId : Misc.getUndefInt();
              if (Misc.isUndef(retval))
                 retval = v;
              buf.append("<tr><td class='tn'>");
              printDimVals(dbConn, user, dimInfo, v, null, buf, varName, toPrintAny, printAnyValLabel, false, helperId, vsize, hsize
                  , globalUseAltName, addnlSearchPortParam, printUseInstruct, doingInTableForMultiVal, doReadOnly, onChangeHandler, addnlParamForPortCollectorLike, addnlParamForLookupFlexMultiAttrib, valsToPrintCustomHint, contextInfo, printIsNullIsNotNullLOV);
              buf.append("</td>");
              buf.append("<td class='tn'>");
              if (l == rowcnt-1) {
                  if (dimInfo.getAttribType() == Cache.FILE_TYPE || dimInfo.getAttribType() == Cache.IMAGE_TYPE)
                      buf.append("<img title='Add another row' src='"+Misc.G_IMAGES_BASE+"green_check.gif'  onClick='callOpenFileUploadPopup(\"").append("v").append(dimInfo.m_id).append("\",true)'>");
                  else {
                      buf.append("<img title='Add another row' src='"+Misc.G_IMAGES_BASE+"green_check.gif'  onClick='")                        
                     .append("addRowMulti(event.srcElement)")
                     .append("'>");                        
                  }
              }
              else {
                  buf.append("<img title='Remove row' src='"+Misc.G_IMAGES_BASE+"cancel.gif'  onClick='removeRowHelper(event.srcElement)'>");
              }
              buf.append("</td>");
           }//for each row
           buf.append("</table>");
           return selectedId;
        }//if doingSpecial
     }//if doingMultiSelect
     
     if(isRadio){
    	 if (varName != null) {
    		 if(dimInfo.m_type == LOV_TYPE ) {
    			 ValInfo tmpv = dimInfo.getValInfo(selectedId,dbConn,session);
    			 nonLovFormattedVal =  tmpv != null ? tmpv.m_name : "";
    		 }
    		 buf.append("<input type=\"hidden\" audit=\"0\" name=\""+varName+"\" id=\""+varName+"\" value=\""+nonLovFormattedVal+"\">");
    		 buf.append(nonLovFormattedVal);
    		 buf.append("<br>");
    		 buf.append("<input type=\"checkbox\" onClick=\"handleRadio(this,'"+varName+"',0)\" name=\""+varName+"_audit\" value=\"0\" checked> No");
    		 buf.append("<input type=\"checkbox\" onClick=\"handleRadio(this,'"+varName+"',1)\" name=\""+varName+"_audit\" value=\"1\" > Yes");
    	 }
    	 return selectedId;
    }
     else if (dimInfo.m_descDataDimId == 123) { //get the org level descendant .... //figure out a way to make this on-demand download
        String portName = getFullPortName(dbConn, selectedId, null);//getPortName(dbConn, selectedId);
        if (varName != null) {
            buf.append("<input type=\"hidden\" name=\""+varName+"\" id=\""+varName+"\" value=\""+Integer.toString(selectedId)+"\"");
            if (onChangeHandler != null && onChangeHandler.length() != 0) {
                buf.append(" onChange='").append(onChangeHandler).append("' ");
            }
            buf.append(">");   
        }
        if (doReadOnly) {            
            buf.append(portName);
        }
        else {
        
           // buf.append("<script language=\"jscript\" src=\"scripts/mtmcode.js\">");
           // buf.append("</script>");
           // buf.append("<script language=\"jscript\" src=\"scripts/port_collector.js\">");
           // buf.append("</script>");
           // buf.append("<script language=\"jscript\">");
           // buf.append("var g_portURL = null;");
           // buf.append("</script>");
            
            StringBuilder javaPortURL = new StringBuilder();
            javaPortURL.append("\"").append(Misc.G_APP_1_BASE).append("getXMLportfolio.jsp?user_id=" + user.getUserId());
            if (!Misc.isUndef(helperId))
              javaPortURL.append("&priv_id=" + Integer.toString(helperId));
            javaPortURL.append("&diminfo=" + Integer.toString(dimInfo.m_id));
            if (addnlSearchPortParam != null)
               javaPortURL.append("&").append(addnlSearchPortParam);
            javaPortURL.append("\"");
//from here ..            
           if (false) {
                buf.append("<script language=\"jscript\">");
                buf.append("g_portURL = ").append(javaPortURL);
                buf.append(";");
                buf.append("</script>");               
                
                buf.append("<script language=\"jscript\">");
                buf.append("portCollectorInit(true, ").append(javaPortURL).append(", 350,\"Select Organization\", true);");
                buf.append("</script>");
           }
//till here needs to be just done once and outside of here ..            
            MiscInner.PortInfo portInfo = getPortInfo(selectedId, dbConn);
            String customStr = null;//from CapEx ... CustomImpl.getAddnlCustomPortInfo(portInfo, this);
            if (customStr == null)
               customStr = "";
		   //082808 .. following line to_ignor added
		   buf.append("<input to_ignore=\"1\" readonly=\"readonly\" size=\"" + Integer.toString(hsize) + "\" class=\"tn\" type=\"text\" name=\"port_node_name\"  value=\"" + portName + "\" onClick='callPopUpPortCollector(this, \"" + varName + "\", \"port_node_name\", \"port_custom\", null, null, \"Select Organization\", ").append(javaPortURL).append(", null"); //the last null is that we don't care about sel specified here
            if (addnlParamForPortCollectorLike != null && addnlParamForPortCollectorLike.length() != 0)
               buf.append(",\"").append(addnlParamForPortCollectorLike).append("\")'>");    
            else
              buf.append(")'>");
            //buf.append("<input size=\""+Integer.toString(hsize)+"\" class=\"tn\" type=\"text\" name=\"port_node_name\"  value=\""+portName+"\" onClick='popUpPortCollector(this, document.forms[0]."+varName+", document.forms[0].port_node_name, document.forms[0].port_custom, null, null, \"Select Organization\", ").append(javaPortURL).append(", null)'>");    
            buf.append("<input type=\"hidden\" name=\"port_custom\" value=\""+customStr+"\">");
        }
        return selectedId;
     }
     else if (dimInfo.m_descDataDimId == 8070 || dimInfo.m_subsetOf == 8070) { //get the org level descendant .... //figure out a way to make this on-demand download
        String portName = "CCBSNAMEHACK_FOR_TRACK";//getFullCCBSName(dbConn, selectedId, null);//getPortName(dbConn, selectedId);
        if (varName != null) {
            buf.append("<input type=\"hidden\" name=\""+varName+"\" id=\""+varName+"\" value=\""+Integer.toString(selectedId)+"\"");
            if (onChangeHandler != null && onChangeHandler.length() != 0) {
                buf.append(" onChange='").append(onChangeHandler).append("' ");
            }
            buf.append(">");   
        }
        if (doReadOnly) {            
            buf.append(portName);
        }
        else {
        
           // buf.append("<script language=\"jscript\" src=\"scripts/mtmcode.js\">");
           // buf.append("</script>");
           // buf.append("<script language=\"jscript\" src=\"scripts/port_collector.js\">");
           // buf.append("</script>");
           // buf.append("<script language=\"jscript\">");
           // buf.append("var g_portURL = null;");
           // buf.append("</script>");
            
            StringBuilder javaPortURL = new StringBuilder();
            javaPortURL.append("\"").append(Misc.G_APP_1_BASE).append("getCCBSxml.jsp?user_id=" + user.getUserId());
            if (!Misc.isUndef(helperId))
              javaPortURL.append("&priv_id=" + Integer.toString(helperId));
            javaPortURL.append("&diminfo=" + Integer.toString(dimInfo.m_id));
            if (addnlSearchPortParam != null)
               javaPortURL.append("&").append(addnlSearchPortParam);
            javaPortURL.append("\"");
            
            //javaPortURL.setLength(0);
            //javaPortURL.append("\"zwbs.xml\"");
            if (false) {            
                buf.append("<script language=\"jscript\">");
                buf.append("portCollectorInit(false, ").append(javaPortURL).append(", 350,\"Select Equipment\", false);");
                buf.append("</script>");                        
            }
			//082808 ..following lin to_ignore=1 added
			buf.append("<input to_ignore=\"1\" readonly=\"readonly\" size=\"" + Integer.toString(hsize) + "\" class=\"tn\" type=\"text\" name=\"port_node_name\"  value=\"" + portName + "\" onClick='callPopUpPortCollector(this, \"" + varName + "\", \"port_node_name\", null, null, null, \"Select Equipment\", ").append(javaPortURL).append(", false");
            if (addnlParamForPortCollectorLike != null && addnlParamForPortCollectorLike.length() != 0)
               buf.append(",\"").append(addnlParamForPortCollectorLike).append("\")'>");    
            else
              buf.append(")'>");
            
        }
        return selectedId;
     }
     else if (dimInfo.getAttribType() == FILE_TYPE || dimInfo.getAttribType() == IMAGE_TYPE) {
        // tempVal would  be the file id
        MiscInner.PairStr fileNameInfo = this.getFileDisplayInfo(dbConn, selectedId);
        
        String originalFileName = "";
        String fileLink = "";
        originalFileName = fileNameInfo.first;
        fileLink = fileNameInfo.second;
        if (doReadOnly) {    
          
          if (varName != null)
             buf.append("<input type=\"hidden\" name=\""+varName+"\" id=\""+varName+"\" value=\""+Integer.toString(selectedId)+"\">");   
          if (dimInfo.getAttribType() == FILE_TYPE) {
            buf.append("<a href=\"").append(fileLink).append("\" target=\"_blank\" class=\"tn\" name=\"uploaded_file_link\">").append(originalFileName).append("</a>&nbsp;&nbsp;");        
          }
          else {
             if (Misc.isUndef(selectedId)) {
                fileLink = ""+Misc.G_IMAGES_BASE+"transparent_sp.gif";
             }
            buf.append("<IMG ");
            if (!Misc.isUndef(hsize) && hsize < 500)
               buf.append(" width=\"").append(hsize).append("\" ");               
            buf.append("src=\"").append(fileLink).append("\" name=\"uploaded_file_link\">").append("</IMG>&nbsp;&nbsp;");
          }
        }
        else {
            if (!doingInTableForMultiVal)    {
              buf.append("<input type=\"hidden\" name=\"").append(varName).append("\" value=\"").append(selectedId).append("\">");
              if (dimInfo.getAttribType() == FILE_TYPE) {
                 buf.append("<a href=\"").append(fileLink).append("\" target=\"_blank\" class=\"tn\" name=\"uploaded_file_link\">").append(originalFileName).append("</a>&nbsp;&nbsp;");
              }
              else {
                if (Misc.isUndef(selectedId)) {
                   fileLink = ""+Misc.G_IMAGES_BASE+"transparent_sp.gif";
                }
                 buf.append("<IMG ");
                  if (!Misc.isUndef(hsize) && hsize < 500)
                     buf.append(" width=\"").append(hsize).append("\" ");               
                  buf.append("src=\"").append(fileLink).append("\" name=\"uploaded_file_link\">").append("</IMG>&nbsp;&nbsp;");
              }
              buf.append("<input type=\"button\" name=\"Upload\" value=\"Upload\" class=\"input_buttons\" onClick=\"openFileUploadPopup('").append(varName).append("');\">");
              buf.append("&nbsp;&nbsp;");              
              //if (!Misc.isUndef(selectedId))
              buf.append("<IMG name='remove_img' ").append(Misc.isUndef(selectedId) ? "style='display:none'" : "").append("title='Remove File Uploaded' src='"+Misc.G_IMAGES_BASE+"cancel.gif'  onClick=\"removeFile('").append(varName).append("')\">");
            }
            else {
                boolean doingNew = false;
                if (originalFileName == null || originalFileName.length() == 0) {
                    originalFileName = "&lt;Click button to load&gt;";
                    fileLink = "#";
                    doingNew = true;
                }
                    
                buf.append("<input type='hidden' name='").append(varName).append("' value='").append(selectedId).append("'>");
                if (dimInfo.m_type == FILE_TYPE) {
                   buf.append("<a ").append(doingNew ? "style='display:none' ":"").append("href='").append(fileLink).append("' target='_blank' class='tn' name='uploaded_file_link'>").append(originalFileName).append("</a>");
                }
                else {
                   if (Misc.isUndef(selectedId)) {
                      fileLink = ""+Misc.G_IMAGES_BASE+"transparent_sp.gif";
                   }
                   buf.append("<IMG ");
                    if (!Misc.isUndef(hsize) && hsize < 500)
                       buf.append(" width=\"").append(hsize).append("\" ");               
                    buf.append("src=\"").append(doingNew ? "style='display:none' ":"").append(fileLink).append("\" name=\"uploaded_file_link\">").append("</IMG>&nbsp;&nbsp;");
                }
                 if (doingNew) {
                    buf.append("<SPAN class='tn'>&lt;Click On Button to Upload&gt;</SPAN>");
                 }
                 else
                    buf.append("");                                                        
            }
        }
        return selectedId;
     }
     /*else if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM || dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM) {
         MiscInner.PairStr projectInfo = dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM ? this.getVehicleDisplayInfo(dbConn, selectedId) : this.getDriverDisplayInfo(dbConn, selectedId);
         if (doReadOnly) {
             if (varName != null)
                buf.append("<input type=\"hidden\" name=\""+varName+"\" id=\""+varName+"\" value=\""+Integer.toString(selectedId)+"\">");
             buf.append(projectInfo.first);
         }
         else {
             String userName = varName+"_name";
             
             buf.append("<input value=\"").append(projectInfo.first).append("\" name=\"").append(userName).append("\" type=\"text\" size=\"").append(hsize).append("\" class=\"tn\"");
 			buf.append(" readonly=\"readonly\" onclick=\"pickElement(event.srcElement, document.all.").append(userName).append(", document.all.").append(varName).append(", '").append(Misc.G_APP_1_BASE).append("project.jsp', 'v123', null, 'special_mode=1");
             if (addnlSearchPortParam != null)
                buf.append("&").append(addnlSearchPortParam);
             buf.append("')\">");
             buf.append("<input value=\"").append(selectedId).append("\" type=\"hidden\" name=\"").append(varName).append("\"/>");
     //                    <img  title="Find/Re-assign User" src=""+Misc.G_IMAGES_BASE+"reassign_user.gif" height=16 name="pick_user" onclick="pickElement(clickElem, userNameElem, userIdElem, uri, uriParamLabel1, uriParamLabel2, otherQueryParam)(event.srcElement)"/>
         }
         return selectedId;
      }*/
     else if (dimInfo.m_subsetOf == 99) {
         MiscInner.PairStr projectInfo = this.getProjectDisplayInfo(dbConn, selectedId);
         if (doReadOnly) {
             if (varName != null)
                buf.append("<input type=\"hidden\" name=\""+varName+"\" id=\""+varName+"\" value=\""+Integer.toString(selectedId)+"\">");
             buf.append(projectInfo.first);
         }
         else {
             String userName = varName+"_name";
             
             buf.append("<input value=\"").append(projectInfo.first).append("\" name=\"").append(userName).append("\" type=\"text\" size=\"").append(hsize).append("\" class=\"tn\"");
 			buf.append(" readonly=\"readonly\" onclick=\"pickElement(event.srcElement, document.all.").append(userName).append(", document.all.").append(varName).append(", '").append(Misc.G_APP_1_BASE).append("project.jsp', 'v123', null, 'special_mode=1");
             if (addnlSearchPortParam != null)
                buf.append("&").append(addnlSearchPortParam);
             buf.append("')\">");
             buf.append("<input value=\"").append(selectedId).append("\" type=\"hidden\" name=\"").append(varName).append("\"/>");
     //                    <img  title="Find/Re-assign User" src=""+Misc.G_IMAGES_BASE+"reassign_user.gif" height=16 name="pick_user" onclick="pickElement(clickElem, userNameElem, userIdElem, uri, uriParamLabel1, uriParamLabel2, otherQueryParam)(event.srcElement)"/>
         }
         return selectedId;
      }
     /*else if (dimInfo.m_subsetOf == 99) {
        MiscInner.PairStr projectInfo = this.getProjectDisplayInfo(dbConn, selectedId);
        if (doReadOnly) {
            if (varName != null)
               buf.append("<input type=\"hidden\" name=\""+varName+"\" id=\""+varName+"\" value=\""+Integer.toString(selectedId)+"\">");
            buf.append(projectInfo.first);
        }
        else {
            String userName = varName+"_name";
            
            buf.append("<input value=\"").append(projectInfo.first).append("\" name=\"").append(userName).append("\" type=\"text\" size=\"").append(hsize).append("\" class=\"tn\"");
			buf.append(" readonly=\"readonly\" onclick=\"pickElement(event.srcElement, document.all.").append(userName).append(", document.all.").append(varName).append(", '").append(Misc.G_APP_1_BASE).append("project.jsp', 'v123', null, 'special_mode=1");
            if (addnlSearchPortParam != null)
               buf.append("&").append(addnlSearchPortParam);
            buf.append("')\">");
            buf.append("<input value=\"").append(selectedId).append("\" type=\"hidden\" name=\"").append(varName).append("\"/>");
    //                    <img  title="Find/Re-assign User" src=""+Misc.G_IMAGES_BASE+"reassign_user.gif" height=16 name="pick_user" onclick="pickElement(clickElem, userNameElem, userIdElem, uri, uriParamLabel1, uriParamLabel2, otherQueryParam)(event.srcElement)"/>
        }
        return selectedId;
     }*/     
     else if (isMultiAttribSearchParam) { //program ... from general_param_lov ... 126 is for test ... make it 200 else backward compatability issue

        String baseTable = dimInfo == null || dimInfo.m_colMap == null ? "supplier_multi_attrib" : dimInfo.m_colMap.table;
        boolean doingPrj = "prj_multi_attrib".equals(baseTable);
        boolean doingOrder = "order_multi_attrib".equals(baseTable);
        boolean doingSupp = "supplier_multi_attrib".equals(baseTable);        
        DimInfo useThisForLookup = dimInfo;
        if (!doingPrj && !doingOrder && !doingSupp) {
            if (!Misc.isUndef(dimInfo.m_subsetOf) && dimInfo.m_subsetOf != dimInfo.m_id)
               useThisForLookup = DimInfo.getDimInfo(dimInfo.m_subsetOf);               
        }
        MiscInner.PairStr projectInfo = this.getMultiAttribDisplayInfo(dbConn, addnlParamForLookupFlexMultiAttrib, selectedId, useThisForLookup);
        
        String objectIdName = doingPrj ? "project_id" : doingOrder ? "order_id" : "supplier_id";
        if (doReadOnly) {
            if (varName != null)
               buf.append("<input type=\"hidden\" name=\""+varName+"\" id=\""+varName+"\" value=\""+Integer.toString(selectedId)+"\">");
            buf.append(projectInfo.first);
        }
        else {
            String userName = varName+"_name";

			buf.append("<input readonly=\"readonly\" value=\"").append(projectInfo.first).append("\" name=\"").append(userName).append("\" type=\"text\" size=\"").append(hsize).append("\" class=\"tn\"");
            if (doingInTableForMultiVal)
               buf.append(" onclick=\"callPickElement(event.srcElement, '").append(userName).append("', '").append(varName).append("', '").append(Misc.G_APP_1_BASE).append("pick_multi_attrib.jsp', null, null, 'parameter_id=").append(useThisForLookup.m_id).append("");
            else
              buf.append(" onclick=\"pickElement(event.srcElement, document.all.").append(userName).append(", document.all.").append(varName).append(", '").append(Misc.G_APP_1_BASE).append("pick_multi_attrib.jsp', null, null, 'parameter_id=").append(useThisForLookup.m_id);
            
            buf.append("&").append(objectIdName).append("=");
            if (!Misc.isUndef(addnlParamForLookupFlexMultiAttrib)) {
                buf.append(addnlParamForLookupFlexMultiAttrib);
                buf.append("')\">");
            }
            else { //sort of hack ... look in the current row using objectIdParam ... designed for framework_agreement in orders page
               buf.append("'+readInputFromElem(getParentRow(event.srcElement),'").append(objectIdName).append("')");   
               buf.append(")\">");
            }
                
            
            buf.append("<input value=\"").append(selectedId).append("\" type=\"hidden\" name=\"").append(varName).append("\"/>");
    //                    <img  title="Find/Re-assign User" src=""+Misc.G_IMAGES_BASE+"reassign_user.gif" height=16 name="pick_user" onclick="pickElement(clickElem, userNameElem, userIdElem, uri, uriParamLabel1, uriParamLabel2, otherQueryParam)(event.srcElement)"/>
        }
        return selectedId;
     }
     else if (isGeneralParam) { //program ... from general_param_lov ... 126 is for test ... make it 200 else backward compatability issue
        MiscInner.PairStr projectInfo = this.getGeneralParamDisplayInfo(dbConn, selectedId, dimInfo.m_id);
        if (doReadOnly) {
            if (varName != null)
                buf.append("<input type=\"hidden\" name=\""+varName+"\" id=\""+varName+"\" value=\""+Integer.toString(selectedId)+"\">");
            buf.append(projectInfo.first);
        }
        else {
            String userName = varName+"_name";

			buf.append("<input readonly=\"readonly\" value=\"").append(projectInfo.first).append("\" name=\"").append(userName).append("\" type=\"text\" size=\"").append(hsize).append("\" class=\"tn\"");
            if (doingInTableForMultiVal)
               buf.append(" onclick=\"callPickElement(event.srcElement, '").append(userName).append("', '").append(varName).append("', '").append(Misc.G_APP_1_BASE).append("pick_general_param.jsp', null, null, 'parameter_id=").append(dimInfo.m_id).append("'");
            else
              buf.append(" onclick=\"pickElement(event.srcElement, document.all.").append(userName).append(", document.all.").append(varName).append(", '").append(Misc.G_APP_1_BASE).append("pick_general_param.jsp', null, null, 'parameter_id=").append(dimInfo.m_id).append("'");
            buf.append(")\">");
            buf.append("<input value=\"").append(selectedId).append("\" type=\"hidden\" name=\"").append(varName).append("\"/>");
    //                    <img  title="Find/Re-assign User" src=""+Misc.G_IMAGES_BASE+"reassign_user.gif" height=16 name="pick_user" onclick="pickElement(clickElem, userNameElem, userIdElem, uri, uriParamLabel1, uriParamLabel2, otherQueryParam)(event.srcElement)"/>
        }
        return selectedId;
     }
     else if (dimInfo.m_subsetOf == 203 || dimInfo.m_subsetOf == 413 || dimInfo.m_subsetOf == 414) { //user ,landmark, region
        MiscInner.PairStr projectInfo = dimInfo.m_subsetOf == 203 ? this.getUserDisplayInfo(dbConn, selectedId)
        		: dimInfo.m_subsetOf == 413 ? this.getLocationDisplayInfo(dbConn,selectedId)
        		: this.getRegionDisplayInfo(dbConn,selectedId)
        		;
        if (doReadOnly) {
            if (varName != null)
               buf.append("<input type=\"hidden\" name=\""+varName+"\" id=\""+varName+"\" value=\""+Integer.toString(selectedId)+"\">");
            buf.append(projectInfo.first);
        }
        else {
            String userName = varName+"_name";
            String callJsp = dimInfo.m_subsetOf == 203 ? "pick_user.jsp" : dimInfo.m_subsetOf == 413 ? "pick_landmark.jsp?lm_only=1" : "pick_region.jsp"; 
			buf.append("<input readonly=\"readonly\" value=\"").append(projectInfo.first).append("\" name=\"").append(userName).append("\" type=\"text\" size=\"").append(hsize).append("\" class=\"tn\"");
            if (doingInTableForMultiVal)
               buf.append(" onclick=\"callPickElement(event.srcElement, '").append(userName).append("', '").append(varName).append("', '").append(Misc.G_APP_1_BASE).append(callJsp).append("', null, null, null");
            else
              buf.append(" onclick=\"pickElement(event.srcElement, document.all.").append(userName).append(", document.all.").append(varName).append(", '").append(Misc.G_APP_1_BASE).append(callJsp).append("', null, null, null");
    //        if (addnlSearchPortParam != null)
    //           buf.append("&").append(addnlSearchPortParam);
            buf.append(")\">");
            buf.append("<input value=\"").append(selectedId).append("\" type=\"hidden\" name=\"").append(varName).append("\"/>");
    //                    <img  title="Find/Re-assign User" src=""+Misc.G_IMAGES_BASE+"reassign_user.gif" height=16 name="pick_user" onclick="pickElement(clickElem, userNameElem, userIdElem, uri, uriParamLabel1, uriParamLabel2, otherQueryParam)(event.srcElement)"/>
        }
        return selectedId;
     }
     else if (dimInfo.m_subsetOf == Misc.VEHICLE_ID_DIM) { //vehicle
    	 MiscInner.PairStr projectInfo = this.getVehicleDisplayInfo(dbConn, selectedId);
         if (doReadOnly) {
             if (varName != null)
                buf.append("<input type=\"hidden\" name=\""+varName+"\"  value=\""+Integer.toString(selectedId)+"\">");
             buf.append(projectInfo.first);
         }
         else {
             String userName = varName+"_name";
             buf.append("<input ").append(dci != null && mandPart != null && mandPart.length() > 0 ? mandPart : "").append(" dim_id='").append(dimInfo.m_id).append("' name=\"").append(userName).append("\" ");
             
             if (dci != null && dci.m_doautoCompleteAsClass)
            	 buf.append(" class='tn autocomplete' ");
             else {
            	 buf.append(" class='tn' onclick='setupAutoComplete($this)' ");
             }
             buf.append(" value = \"").append(projectInfo.first == null ? "" : projectInfo.first).append("\"")
             //.append("  onFocus=\"populateElemSpecific(this, '").append(varName).append("',  jg_vehicleList, 10 )\"")
             .append(" size=\"20\">")
     	            .append("<input type='hidden' name=\"").append(varName).append("\" value='").append(selectedId).append("'/>");
         }
         return selectedId;
     }
     else if (dimInfo.m_subsetOf == Misc.DRIVER_ID_DIM) { //user
         MiscInner.PairStr projectInfo = this.getDriverDisplayInfo(dbConn, selectedId);
         if (doReadOnly) {
             if (varName != null)
                buf.append("<input type=\"hidden\" name=\""+varName+"\"  value=\""+Integer.toString(selectedId)+"\">");
             buf.append(projectInfo.first);
         }
         else {
             String userName = varName+"_name";
             buf.append("<input ").append(dci != null && mandPart != null && mandPart.length() > 0 ? mandPart : "").append(" dim_id='").append(dimInfo.m_id).append("' name=\"").append(userName).append("\"");
             if (dci != null && dci.m_doautoCompleteAsClass)
            	 buf.append(" class='tn autocomplete' ");
             else {
            	 buf.append(" class='tn' onclick='setupAutoComplete($this)' ");
             }
             buf.append(" value = \"").append(projectInfo.first == null ? "" : projectInfo.first).append("\"")
             
             //.append("  onFocus=\"populateElemSpecific(this, '").append(varName).append("',  jg_driverList, 10 )\"")
             .append(" size=\"20\">")
     	            .append("<input type='hidden' name=\"").append(varName).append("\" value='").append(selectedId).append("'/>");
         }
         return selectedId;
      }
     else if (dimInfo.m_subsetOf == 70201) { //DOR
         MiscInner.PairStr projectInfo = this.getDORRDisplayInfo(dbConn, selectedId);
         if (doReadOnly) {
             if (varName != null)
                buf.append("<input type=\"hidden\" name=\""+varName+"\"  value=\""+Integer.toString(selectedId)+"\">");
             buf.append(projectInfo.first);
         }
         else {
             String userName = varName+"_name";
       //      buf.append("<input addnl_dim='60230' ").append(dci != null && dci.m_isMandatory ?" _mand=\"1\" " : "").append(" dim_id='").append(dimInfo.m_id).append("' name=\"").append(userName).append("\" class='tn autocomplete' value = \"").append(projectInfo.first == null ? "" : projectInfo.first).append("\"")
             buf.append("<input addnl_dim='60230' ");
             if (dci != null && dci.m_doautoCompleteAsClass)
            	 buf.append(" class='tn autocomplete' ");
             else {
            	 buf.append(" class='tn' onclick='setupAutoComplete($this)' ");
             }
             buf.append(dci != null && mandPart != null && mandPart.length() > 0 ? mandPart : "").append(" dim_id='").append(dimInfo.m_id).append("' name=\"").append(userName).append("\"  value = \"").append(projectInfo.first == null ? "" : projectInfo.first).append("\" min_sugg_len=\"").append(dci != null && !Misc.isUndef(dci.m_min_sugg_length) ? dci.m_min_sugg_length : 4).append("\"")
             ;
             //.append("  onFocus=\"populateElemSpecific(this, '").append(varName).append("',  jg_driverList, 10 )\"")
             if (onChangeHandler != null) {
            	 buf.append(" onchange='").append(onChangeHandler).append("' ");
             }
             buf.append(" size=\"20\">")
     	            .append("<input type='hidden' name=\"").append(varName).append("\" value='").append(selectedId).append("'/>");
         }
         return selectedId;
      }else if(dci != null && dci.m_isAutocomplete){//dimInfo.m_subsetOf == 80179){
    	  MiscInner.PairStr projectInfo = this.getDisplayInfoByCode(dbConn, nonLovFormattedVal, dimInfo);
          if (doReadOnly) {
              if (varName != null)
                 buf.append("<input type=\"hidden\" name=\""+varName+"\"  value=\""+projectInfo.first+"\">");
              buf.append(projectInfo.first);
          }
          else {
              String userName = varName+"_name";
              
              buf.append("<input addnl_dim='60230' ");
              if (dci != null && dci.m_doautoCompleteAsClass)
              	 buf.append(" class='tn autocomplete' ");
               else {
              	 buf.append(" class='tn' onclick='setupAutoComplete($this)' ");
               }

              buf.append(dci != null && mandPart != null && mandPart.length() > 0 ? mandPart : "").append(" dim_id='").append(dimInfo.m_id).append("' name=\"").append(userName).append("\" value = \"").append(projectInfo.second == null ? "" : projectInfo.first).append("\" min_sugg_len=\"").append(dci != null && !Misc.isUndef(dci.m_min_sugg_length) ? dci.m_min_sugg_length : 4).append("\"")
              ;
              if (onChangeHandler != null) {
             	 buf.append(" onchange='").append(onChangeHandler).append("' ");
              }
              buf.append(" size=\"20\">")
      	            .append("<input type='hidden' name=\"").append(varName).append("\" value='").append(projectInfo.second).append("'/>");
          }
          return selectedId;
      }
     else if (dimInfo.m_subsetOf == 90630) { //PO Line
         MiscInner.PairStr projectInfo = this.getPOLineDisplayInfo(dbConn, selectedId);
         if (doReadOnly) {
             if (varName != null)
                buf.append("<input type=\"hidden\" name=\""+varName+"\"  value=\""+Integer.toString(selectedId)+"\">");
             buf.append(projectInfo.first);
         }
         else {
             String userName = varName+"_name";
             buf.append("<input readonly='1' ").append(dci != null && dci.m_isMandatory ?" _mand=\"1\" " : "").append(" dim_id='").append(dimInfo.m_id).append("' name=\"").append(userName).append("\" class='tn' value = \"").append(projectInfo.first == null ? "" : projectInfo.first).append("\"")
             .append("  onClick=\"selectPO(this, '").append(varName).append("')\"")
             .append(" size=\"20\">")
     	            .append("<input type='hidden' name=\"").append(varName).append("\" value='").append(selectedId).append("'/>");
         }
         return selectedId;
      }
     else if (dimInfo.m_id == 60263 || dimInfo.m_subsetOf == 60263 ) { //autocomplete
         
         if (doReadOnly) {
             if (varName != null)
                buf.append("<input type=\"hidden\" name=\""+varName+"\"  value=\""+Integer.toString(selectedId)+"\">");
             buf.append(nonLovFormattedVal);
         }
         else {
             String userName = varName+"_name";
             

             buf.append("<input ").append(dci != null && dci.m_isMandatory ?" _mand=\"1\" " : "").append(" dim_id=\""+dimInfo.m_id+"\"  name=\"").append(userName).append("\" ");
             if (dci != null && dci.m_doautoCompleteAsClass)
             	 buf.append(" class='tn autocomplete' ");
              else {
             	 buf.append(" class='tn' onclick='setupAutoComplete($this)' ");
              }
             buf.append(" value = \"").append(nonLovFormattedVal).append("\" size=\"20\">")
     	            .append("<input type='hidden' name=\"").append(varName).append("\" value='").append(selectedId).append("'/>");
         }
         return selectedId;
      }
     else { //regular stuff
        
        //print multi, single etc.... no need to worry about the selected
        ArrayList valList = dimInfo.getValList(dbConn, session);
        if (nonLovFormattedVal != null) {
        	if (doReadOnly) {
 		       buf.append("<input type='hidden' class=tn name='"+varName+"' size=\""+Integer.toString(hsize)+"\" value=\"").append(nonLovFormattedVal).append("\"/>");
 		       buf.append(nonLovFormattedVal);
        	}
        	else {
        		if (dimInfo.getAttribType() == Cache.DATE_TYPE) {
        			boolean kendo = dimInfo.m_useKendo;
        			if (!kendo) {
        				buf.append("<input title='Example: 19/04/14 21:39' placeholder='dd/MM/yy").append("20506".equals(dimInfo.m_subtype) ? " HH:mm" : "").append("'  class='tn'  type='text' size='").append(hsize <= 0 ? 9 : hsize).append("' name='").append(varName).append("' value='").append(nonLovFormattedVal).append("'");
    	  			 	//buf.append(" onclick='callCalendar(this,this, \"").append(varName).append("\", \"").append(Misc.G_DEFAULT_DATE_FORMAT).append("\", ").append("20506".equals(dimInfo.m_subtype) ?"true": "false").append(")'");
        			}
        			else {
        				buf.append("<input  class='datetimepicker'  type='text' size='").append(hsize <= 0 ? 9 : hsize).append("' name='").append(varName).append("' value='").append(nonLovFormattedVal).append("'");
        			}
        			if (!kendo) {
    	  			    //buf.append(" onclick='callCalendar(this,this, \"").append(varName).append("\", \"").append(Misc.G_DEFAULT_DATE_FORMAT).append("\", ").append("20506".equals(dimInfo.m_subtype) ?"true": "false").append(")'");
        			}
    	  			 if (validationHandler != null || onChangeHandler != null) {
    	  				 buf.append(" onchange='");
    	  				 if (onChangeHandler != null)
    	  					 buf.append(onChangeHandler);
    	  				 
    	  				 if (validationHandler != null) {
    	  					 if (onChangeHandler != null)
    	  						 buf.append(";");
    	  					 buf.append(validationHandler);
    	  				 }
    	  				 buf.append("' ");
    	  			 }
    	  			 if (mandPart != null)
    	  				 buf.append(mandPart);
    	  			 buf.append("/>");
    	  			//buf.append("<img width='16' height='16' border='0' alt='Clear Date' src='").append(com.ipssi.gen.utils.Misc.G_IMAGES_BASE).append("undo_orange.gif' onclick='callClearCalendar(this, \"").append(varName).append("\")' />");
    	  		}
        		else {
        			if (vsize <= 1) {
        				buf.append("<input type='text' class=tn name='"+varName+"' size=\""+Integer.toString(hsize)+"\" value=\"").append(nonLovFormattedVal).append("\"");
        				if (mandPart != null)
        					buf.append(mandPart);
        				
	        	  		 if (validationHandler != null || onChangeHandler != null) {
        	  				 buf.append(" onchange='");
        	  				 if (onChangeHandler != null)
        	  					 buf.append(onChangeHandler);
        	  				 
        	  				 if (validationHandler != null) {
        	  					 if (onChangeHandler != null)
        	  						 buf.append(";");
        	  					 buf.append(validationHandler);
        	  				 }
        	  				 buf.append("' ");
        	  			 }
        				buf.append(" />");
        			}
        			else {
        				 buf.append("<textarea class='tn' name='").append(varName).append("' rows='").append(vsize).append("' cols='").append(hsize).append("'");
        				 if (mandPart != null)
         					buf.append(mandPart);
    					 if (validationHandler != null || onChangeHandler != null) {
        	  				 buf.append(" onchange='");
        	  				 if (onChangeHandler != null)
        	  					 buf.append(onChangeHandler);
        	  				 
        	  				 if (validationHandler != null) {
        	  					 if (onChangeHandler != null)
        	  						 buf.append(";");
        	  					 buf.append(validationHandler);
        	  				 }
        	  				 buf.append("' ");
        	  			 }
        				buf.append(" >");
        				buf.append(nonLovFormattedVal).append("</textarea>");
        			}
        		}
        	}
        	return Misc.getUndefInt();
        }
        else if (valList == null || valList.size() == 0) {//print as text box ..
           if (doReadOnly) {
               if (varName != null) {
                   buf.append("<input type='hidden' class=tn name='"+varName+"' size=\""+Integer.toString(hsize)+"\" value=\"");
                   if (otherSelSz == 0) {
				      if (!Misc.isUndef(selectedId))					   
                         buf.append(selectedId);
				   }
                   else
                      Misc.convertInListToStr(otherSels, buf);
                   buf.append("\"");
                   buf.append(">");    
               }
               if (otherSelSz == 0) {
			      if (!Misc.isUndef(selectedId))
                     buf.append(selectedId);
				  else
				     buf.append("&nbsp;");
			   }
               else
                  Misc.convertInListToStr(otherSels, buf);
               
           }
           else {
               buf.append("<input class=tn name='"+varName+"' size=\""+Integer.toString(hsize)+"\" value=\"");
               if (otherSelSz == 0) {
			      if (!Misc.isUndef(selectedId))
                     buf.append(selectedId);
			   }
               else
                  Misc.convertInListToStr(otherSels, buf);
			   buf.append("\"");
		        if (mandPart != null)
             	   buf.append(mandPart);
        
               buf.append(" >");
           }
           return selectedId;
        }
        boolean budgetYearHack = dimInfo.m_id == 5079;        
        budgetYearHack = false; //i.e. hack no longer required
        
        //onmouseout="this.size=1; return true;" onmouseover="this.size=5"
        //buf.append("<select class=tn name="+varName+" size="+Integer.toString(printMultiSelect ? vsize : 1)+(printMultiSelect? " multiple=multiple> " : " >"));
        if (doReadOnly) {
            if (varName != null) {
                buf.append("<input type='hidden' class=tn name='"+varName+"' size=\""+Integer.toString(hsize)+"\" value=\"");
                if (otherSelSz == 0)
                    buf.append(selectedId);
                else
                    Misc.convertInListToStr(otherSels, buf);
                buf.append("\">");
            }
            if (otherSels == null || otherSels.size() == 0) {
                DimInfo.ValInfo vinfo = dimInfo.getValInfo(selectedId);
                if (vinfo != null)
                   buf.append(vinfo.m_name);                
            }
            else {
                for (int l1=0,l1s=otherSels.size();l1<l1s;l1++) {
                   DimInfo.ValInfo vinfo = dimInfo.getValInfo(((Integer)otherSels.get(l1)).intValue());
                   if (vinfo != null)
                      buf.append(vinfo.m_name);                
                }
            }
            return selectedId;
        }
        else {
            buf.append("<select class=\"tn\" name=\""+varName+"\" ");
            if (onChangeHandler != null && onChangeHandler.length() != 0) {
                buf.append(" onChange=\"").append(onChangeHandler).append("\" ");
            }
            if (mandPart != null)
         	   buf.append(mandPart);
            if (onChangeHandler != null && onChangeHandler.startsWith("warnOnStatusChange")) {
            	buf.append(" old_val='").append(selectedId).append("' ");
            }
            boolean oldMultiApproach = false;
            if (!printMultiSelect)
            	oldMultiApproach = true;
            boolean toShowDownImg = false;
            ArrayList<Integer> selOnTopIndex = null;
            int begOfNonSelIndex = 0;
            
            boolean isFirst = true;
            int retval = selectedId;
            boolean doFirst = false;
    
            if (Misc.isUndef(selectedId)) {
               if (toPrintAny) {
                  hasAny = true;
                  selectedId = Misc.G_HACKANYVAL;
                  doFirst = false;
               }
               else if (!printUseInstruct || otherSelSz == 0)
                  doFirst = true;
            }
            boolean inNewApproachPrintAnyAtTop = oldMultiApproach || hasAny;
            boolean inNewApproachPrintUseInstruct = oldMultiApproach || (otherSelSz == 0 && Misc.isUndef(selectedId));

            if (printMultiSelect) {
            	if (oldMultiApproach) {
	                buf.append("size='1' onmouseout='this.size=1; return true;' onmouseover='this.size=Math.min(12,this.options.length)' ");
	                buf.append(" multiple=multiple> ");
            	}
            	else {		            
		            selOnTopIndex = new ArrayList<Integer>();
            		for (int i=0,count=valList.size();i<count;i++) {          
                        DimInfo.ValInfo valInfo = (DimInfo.ValInfo) valList.get(i);
                        
                        
                        int id = valInfo.m_id;
                        if (budgetYearHack && id != selectedId && id > 0 && ((Misc.G_BUDGET_ON && id < Misc.G_BUDGET_YEAR) || (!Misc.G_BUDGET_ON && id <= Misc.G_BUDGET_YEAR)))
                           continue;
                        boolean doSelected = id == selectedId;
                        if (!doSelected && otherSelSz > 1) {
                           for (int l=0;l<otherSelSz;l++) {
                              if (id == ((Integer)otherSels.get(l)).intValue()) {
                                 doSelected = true;
                                 break;
                              }
                           }
                        }
                    //    boolean toNotSkip = doSelected || valInfo.hasPriv(session);
                    //    if (!toNotSkip)
                    //    	continue;
                        if (doSelected) {
                          selOnTopIndex.add(begOfNonSelIndex++, i);
                        }
                        else {
                        	selOnTopIndex.add(i);
                        }
                     }//end of for
            		int selSz = begOfNonSelIndex+(inNewApproachPrintAnyAtTop ? 1 : 0)+(inNewApproachPrintUseInstruct ? 1 : 0);
            		int maxselToShow = 1;
            		int relSzToPrint = selSz > maxselToShow? maxselToShow : selSz;
            		if (selSz > maxselToShow)
            			toShowDownImg = true;
            		buf.append(" size='").append(relSzToPrint).append("' onmouseout='handleSelChangeMultiSel(); return true;' onmouseover='this.size=Math.min(12,this.options.length)' ");
		            buf.append(" multiple=multiple> ");
		            //now get the order in which vals must be printed
            	}//if new approach
            }
            else {
               buf.append(" >");
            }
            

            
            if (toPrintAny && inNewApproachPrintAnyAtTop) {
                buf.append("<option value=\""+Integer.toString(Misc.G_HACKANYVAL)+"\" "+(hasAny?"selected":"")+">"+printAnyValLabel+"</option>");
             }
             if (printUseInstruct && inNewApproachPrintUseInstruct) {
                boolean isSel = otherSelSz == 0 && Misc.isUndef(selectedId);
                buf.append("<option value=\""+Integer.toString(Misc.getUndefInt())+"\" "+(isSel?"selected":"")+">&lt; Select &gt;</option>");
                
             }
             boolean doCustomCheck = !Misc.isUndef(valsToPrintCustomHint) && !Misc.isUndef(selectedId);
             
             for (int i=0,count=valList.size();i<count;i++) {
             	if (!oldMultiApproach && i >= begOfNonSelIndex) {//going to print non sel part ... if any needs to be printed and was not selected 
             		                                                                                      //or use instructions needs to be printed and not selected then it needs to be printed here 
             		if (toPrintAny && !inNewApproachPrintAnyAtTop) {
             			buf.append("<option value=\""+Integer.toString(Misc.G_HACKANYVAL)+"\" "+(hasAny?"selected":"")+">"+printAnyValLabel+"</option>");
             			inNewApproachPrintAnyAtTop = true; //reusing inNewApproachPrintAnyAtTop ... basically print any only once
             		}
             		if (printUseInstruct && !inNewApproachPrintUseInstruct) {
                         boolean isSel = otherSelSz == 0 && Misc.isUndef(selectedId);
                         buf.append("<option value=\""+Integer.toString(Misc.getUndefInt())+"\" "+(isSel?"selected":"")+">&lt; Select &gt;</option>");
                         inNewApproachPrintUseInstruct = true;  //print instruction only once ..
                      }
             	}
                DimInfo.ValInfo valInfo = (DimInfo.ValInfo) valList.get(oldMultiApproach ? i : selOnTopIndex.get(i));
                
                
                
                int id = valInfo.m_id;
                if (budgetYearHack && id != selectedId && id > 0 && ((Misc.G_BUDGET_ON && id < Misc.G_BUDGET_YEAR) || (!Misc.G_BUDGET_ON && id <= Misc.G_BUDGET_YEAR)))
                   continue;
                boolean doSelected = false;
                if (!oldMultiApproach) {
             	   doSelected = i < begOfNonSelIndex;
                }
                else {
 	               doSelected = id == selectedId;
 	               if (!doSelected && otherSelSz > 1) {
 	                  for (int l=0;l<otherSelSz;l++) {
 	                     if (id == ((Integer)otherSels.get(l)).intValue()) {
 	                        doSelected = true;
 	                        break;
 	                     }
 	                  }
 	               }
                }
                if (!doSelected && !valInfo.hasPriv(session))
                	continue;
                String nameStr = null;
                if (globalUseAltName) {
                    nameStr = valInfo.getOtherProperty("alt_name");
                    if (nameStr == null || nameStr.length() == 0)
                       nameStr = valInfo.m_name;
                }
                else {
                    nameStr = valInfo.m_name;
                }
     
     
     
                if (isFirst) {
                    retval = id;
                }
                
     
                if (doSelected)
                   retval = id;
                if (isFirst && !doSelected && doFirst)
                   doSelected = true;
                isFirst = false;
                if (valInfo.m_isSp)        
                   continue;
                //if (doCustomCheck && !CustomImpl.doDimValInfoPrint(dimInfo, valInfo, selectedId))
                //   continue;
                buf.append("<option value=\""+id+"\" "+(doSelected?"selected":"")+">"+nameStr+"</option>");
             }//end of for
            if (printIsNullIsNotNullLOV == 1) {
            	DimInfo specialDim = DimInfo.getDimInfo("special_null_filter");
            	if (specialDim != null) {
            		ArrayList<ValInfo> spv = specialDim.getValList();
            		for (ValInfo v1:spv) {
            			buf.append("<option value=\""+v1.m_id+"\" "+(v1.m_id == selectedId ?"selected":"")+">"+v1.m_name+"</option>");
            		}
            	}
            }
             buf.append("</select>");
             if (!oldMultiApproach) {
             	buf.append("<IMG src='"+Misc.G_IMAGES_BASE+"ask_rework.gif' name='down_img' style='display:").append(toShowDownImg ?"INLINE":"NONE").append("'/>");
             }
             return retval;

        }
        
     }//end of else doing non 123

  }
  
  public void getAllParentValsExcl123Dims(DimInfo parentDimInfo, int valId, ArrayList result) throws Exception {
      result.clear();
      if (parentDimInfo.m_descDataDimId == parentDimInfo.m_id) {
          result.add(new Integer(valId));
          return;
      }      
      else {
         DimInfo chDimInfo = DimInfo.getDimInfo(parentDimInfo.m_descDataDimId);
         chDimInfo.getAllParentVals(parentDimInfo.m_id,valId, result);
      }
  }
  
  public int getParentDimValId(Connection dbConn, DimInfo parentDimInfo, int valId) throws Exception {
      if (parentDimInfo.m_descDataDimId == parentDimInfo.m_id) {
          return valId;
      }
      else if (parentDimInfo.m_descDataDimId == 123) {
         MiscInner.PortInfo pn = getParentPortNode(dbConn, valId, parentDimInfo);
         if (pn != null)
            return (pn.m_id);
         //return Misc.getUndefInt();
      }
      else {
         DimInfo chDimInfo = DimInfo.getDimInfo(parentDimInfo.m_descDataDimId);
         if (chDimInfo != null)
            return chDimInfo.getAParentVal(parentDimInfo.m_id, valId);
      }
      return valId;//Misc.getUndefInt();      
  }


  public static boolean hasAnyVal(int valId, ArrayList selList, DimInfo dimInfo) {

      int sz = selList == null ? 0 : selList.size();
      if (sz == 0) {
         return valId == Misc.G_HACKANYVAL || (dimInfo != null && dimInfo.m_descDataDimId == 123 && Misc.isUndef(valId));
      }
      for (int i=0; i<sz;i++) {
         int v = ((Integer)selList.get(i)).intValue();
         if (v == Misc.G_HACKANYVAL || (dimInfo != null && dimInfo.m_descDataDimId == 123 && Misc.isUndef(v)))
            return true;
      }
      return false;
  }

  public void helperPopulateDesc(MiscInner.PortInfo elem, ArrayList retval) {
     if (elem != null && (elem.m_children == null || elem.m_children.size() == 0)) {
        retval.add(new Integer(elem.m_id));
     }
     else {
        ArrayList chList = elem.m_children;
        
        for (int n1=0,n1s=chList == null ? 0 : chList.size();n1<n1s;n1++) {
            MiscInner.PortInfo e = (MiscInner.PortInfo) chList.get(n1);
            if (e.m_isExtended)
               continue;
            helperPopulateDesc(e, retval);
        }
     }
  }

  public ArrayList getInList(Connection dbConn, DimInfo parentDimInfo, int valId, ArrayList selList) throws Exception {//may return selList itself so be careful about using retval
      return getInList(dbConn, parentDimInfo, valId, selList, true);
  }

  public ArrayList getInList(Connection dbConn, DimInfo parentDimInfo, int valId, ArrayList selList, boolean expandDesc) throws Exception {//may return selList itself so be careful about using retval
      boolean hasAny = hasAnyVal(valId, selList, parentDimInfo);
      if (hasAny && parentDimInfo.m_descDataDimId != 123) {
          DimInfo chDimInfo = parentDimInfo.m_descDataDimId == parentDimInfo.m_id ? parentDimInfo : DimInfo.getDimInfo(parentDimInfo.m_descDataDimId);
          
          ArrayList retval = new ArrayList();
          ArrayList valList = chDimInfo.getValList();
          for (int i=0,is = valList == null ? 0 : valList.size(); i<is; i++) {
              DimInfo.ValInfo v = (DimInfo.ValInfo) valList.get(i);              
              retval.add(new Integer(v.m_id));
          }
          return retval;
      }
      if (parentDimInfo.m_id == parentDimInfo.m_descDataDimId || !expandDesc) {

         if (selList == null || selList.size() == 0) {
            ArrayList retval = new ArrayList();
            retval.add(new Integer(valId));
            return retval;
         }
         return selList;
      }
      else {

         int pos = 0;
         int sz = selList == null ? 0 : selList.size();
         int valToCheck = sz > 0 ? ((Integer)selList.get(0)).intValue() : valId;
         boolean done = pos >= sz;
         ArrayList retval = new ArrayList();
         DimInfo chDimInfo = DimInfo.getDimInfo(parentDimInfo.m_descDataDimId);
         if (parentDimInfo.m_descDataDimId == 123) {
             do {
                //do stuff
                MiscInner.PortInfo portNode = getPortNode(dbConn, valToCheck);
                helperPopulateDesc(portNode, retval);                
                pos++;
                done = pos >= sz;
                if (!done)
                   valToCheck = ((Integer)selList.get(pos)).intValue();
             } while (!done);
         }
         else {             
             do {
                ArrayList rhsVals = parentDimInfo.getRHSVals(chDimInfo.m_id, valToCheck);
                for (int n1=0,n1s = rhsVals.size();n1<n1s;n1++) {
                   retval.add(rhsVals.get(n1));
                }                
                pos++;
                done = pos >= sz;
                if (!done)
                   valToCheck = ((Integer)selList.get(pos)).intValue();
             } while (!done);
         }
         return retval;
      }
  }

  
  public void printDimValAsTable(String dimName, JspWriter out, int numCols, int selValList[]) throws IOException {
     printDimValAsTable(dimName, out, numCols, selValList, false);
  }
  public void printDimValAsTable(String dimName, JspWriter out, int numCols, int selValList[], boolean doAsRadio) throws IOException {
     printDimValAsTable(dimName, out, numCols, selValList, doAsRadio, false, null);
  }

  public void printDimValAsTable(String dimName, JspWriter out, int numCols, int selValList[], boolean doAsRadio, boolean printClassEtc, String inpName) throws IOException {
     //doesn't print the beginning <table>, </table>
     DimInfo dimInfo = DimInfo.getDimInfo(dimName);
     ArrayList valList = dimInfo.getValList();
     int colPrinted = numCols;
     boolean firstRowPrinted = false;
     if (inpName == null)
        inpName = dimName;
     for (int i=0,count=valList == null ? 0 : valList.size();i<count;i++) {
        DimInfo.ValInfo valInfo = (DimInfo.ValInfo)valList.get(i);
        if (valInfo.m_isSp)
            continue;
        int id = valInfo.m_id;
        String idStr = Integer.toString(id);
        String nameStr = valInfo.m_name;
        boolean foundInSelValList = (selValList != null && selValList.length > 0 && Arrays.binarySearch(selValList, id) >= 0);
        
        if (colPrinted == numCols) {
           if (firstRowPrinted) //close row
              out.println("</tr>");
           out.println("<tr>");
           colPrinted = 0;
        }
        firstRowPrinted = true;
        colPrinted++;
        if (printClassEtc) {
           out.println("<td class=\"cn\"><input class=\"tn\" type=\""+(doAsRadio?"radio":"checkbox")+"\" name=\""+inpName+"\" value=\""+idStr+"\""+(foundInSelValList?"checked":"")+"></td>");
           out.println("<td class=\"cn\">"+nameStr+"</td>");
        }
        else {
           out.println("<td class=\"tn\" width=7><input class=\"tn\" type=\""+(doAsRadio?"radio":"checkbox")+"\" name=\""+inpName+"\" value=\""+idStr+"\""+(foundInSelValList?"checked":"")+"></td>");
           out.println("<td class=\"tn\">"+nameStr+"</td>");
           out.println("<td class=\"tn\" width=7>&nbsp;</td>");
        }
     }
     if (firstRowPrinted) {
        for (int i=colPrinted;i<numCols;i++)
           if (printClassEtc) {
               out.println("<td class='cn'>&nbsp;</td><td class='cn'>&nbsp;</td>");
           }
           else {
               out.println("<td class='tn'>&nbsp;</td><td class='tn'>&nbsp;</td><td class='tn'>&nbsp;</td>");
           }
        out.println("</tr>");
     }
  }
  
  ///  UNIT
  private static ArrayList g_qtyUnitInfo = new ArrayList(); //ArrayList of ArrayList of MiscInner.UnitInfo //dont reinit
  public static MiscInner.UnitInfo getUnitInfo(int qtyType, int unitCode) {
     ArrayList unitList = getQtyUnitList(qtyType);
     if (unitList == null || unitList.size() <= unitCode || unitCode < 0)
        return MiscInner.UnitInfo.g_defaultUnit;
     MiscInner.UnitInfo retval =  (MiscInner.UnitInfo) unitList.get(unitCode);      
     if (retval == null)
        return MiscInner.UnitInfo.g_defaultUnit;
     return retval;
  }
  public static void printUnitList(StringBuilder outp, int qtyType, int currSel) {
     ArrayList unitList = getQtyUnitList(qtyType);
     if (unitList != null && unitList.size() > 0) {
        //outp.append("<select name='").append(varName).append("' class='tn'>");
        for (int i=0,is = unitList.size();i<is;i++) {
           MiscInner.UnitInfo unitInfo = (MiscInner.UnitInfo) unitList.get(i);
           if (unitInfo == null)
              continue;
           outp.append("<option ").append(currSel == unitInfo.m_id ? " selected " : "").append(" value='").append(unitInfo.m_id).append("'>").append(unitInfo.m_displayName).append("</option>");
        }
  //      outp.append("</select>");
     }
     return;     
  }  
  private static ArrayList getQtyUnitList(int qtyType) {
     if (qtyType >= g_qtyUnitInfo.size() || qtyType < 0)
        return null;
        
    return (ArrayList) g_qtyUnitInfo.get(qtyType);
     
  }
  private static void loadQtyUnitInfo(Element elem) {
     for (Node n1 = elem == null ? null : elem.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
         if (n1.getNodeType() != 1)
            continue;
         Element qtyNode = (Element) n1;
         int qtyType = Misc.getParamAsInt(qtyNode.getAttribute("id"));
         if (Misc.isUndef(qtyType))
            continue;
         ArrayList unitList = new ArrayList();
         
         if (g_qtyUnitInfo.size() <= qtyType) {
           for (int t1=g_qtyUnitInfo.size(),t1s=qtyType+1;t1<t1s;t1++)
              g_qtyUnitInfo.add(null);
         }            
         g_qtyUnitInfo.set(qtyType, unitList);
         
         for (Node n = qtyNode.getFirstChild();n!= null; n = n.getNextSibling()) {
             if (n.getNodeType() != 1)
                continue;
             Element e = (Element) n;
             //public UnitInfo(int id, int qtyType, double unit, int decimal, String prefix, String suffix, String displayName) {
             int id = Misc.getParamAsInt(e.getAttribute("id"));
             String name = Misc.getParamAsString(e.getAttribute("name"));
             double unit = Misc.getParamAsDouble(e.getAttribute("unit"),1);
             int decimal = Misc.getParamAsInt(e.getAttribute("decimal"),0);
             String prefix = Misc.getParamAsString(e.getAttribute("prefix"));
             String suffix = Misc.getParamAsString(e.getAttribute("suffix"));
             int minDecimal = Misc.getParamAsInt(e.getAttribute("min_decimal"),0);
             MiscInner.UnitInfo unitInfo = new MiscInner.UnitInfo(id, qtyType, unit, decimal, prefix, suffix, name, minDecimal);
             if (unitList.size() <= id) {
                for (int t1=unitList.size(),t1s=id+1;t1<t1s;t1++)
                    unitList.add(null);
             }
             unitList.set(id, unitInfo);
         }
     }
  }
  
  
  // **** CURRENCY
  private static ArrayList g_currencyInfo = new ArrayList(); //indexed by countrCode val MiscInner.CurrencyInfo; //dont reinit
  public static MiscInner.CurrencyInfo getCurrencyInfo(int id) {
      if (g_currencyInfo.size() <= id || id < 0)
        return null;
     return (MiscInner.CurrencyInfo) g_currencyInfo.get(id);
  }
  
  // ***** COUNTRY
  private static ArrayList g_countryInfo = new ArrayList(); //dont reinit
  public static MiscInner.CountryInfo getCountryInfo(int id) {
     if (g_countryInfo.size() <= id || id < 0)
        return null;
     return (MiscInner.CountryInfo) g_countryInfo.get(id);
  }
  
  private static HashMap g_localeById = new HashMap(6,0.75f);
  private static HashMap g_localesDefined = new HashMap(6,0.75f); //key is locale string, val is Locale definition ... so as to avoid creating 100s of duplicate locales
  public static Locale getLocale(String langCode, String countryCode) {
     String lookup = langCode+"_"+countryCode;
     Locale retval = (Locale) g_localesDefined.get(lookup);
     if (retval == null) {
         retval = new Locale(langCode, countryCode); 
         g_localesDefined.put(lookup, retval);
         DimInfo locDim = DimInfo.getDimInfo(257);
         ArrayList locValList = locDim.getValList();
         
         for (int i=0,is = locValList == null ? 0 : locValList.size(); i<is; i++) {
            DimInfo.ValInfo valInfo = (DimInfo.ValInfo) locValList.get(i);
            if (langCode.equals(valInfo.getOtherProperty("lang")) && countryCode.equals(valInfo.getOtherProperty("country"))) {
               g_localeById.put(new Integer(valInfo.m_id), retval);
               break;
            }
         }
     }
     return retval;       
  }
  public static Locale getLocale(int localeId) {
     Integer lookup = new Integer(localeId);
     Locale retval = (Locale) g_localeById.get(lookup);
     if (retval == null) {
         DimInfo locDim = DimInfo.getDimInfo(257);
         DimInfo.ValInfo valInfo = locDim.getValInfo(localeId);
         
         if (valInfo != null) {
            String langCode = valInfo.getOtherProperty("lang");
            String countryCode = valInfo.getOtherProperty("country");
            retval = new Locale(langCode, countryCode);
            g_localeById.put(lookup, retval);
            g_localesDefined.put(langCode+"_"+countryCode, retval);            
         }                  
     }   
     return retval;       
  }
  
  
  
  void processCurrCountryEtcNode() { //called after end of 
  //   public CurrencyInfo(int id, String iso3digitCode, String twoDigitCode, String userFriendlyName) {
  //   public CountryInfo(int id, String iso2digitCode, String threeDigitCode, String userFriendlyName, String localeLangCode, Locale locale) {
      DimInfo countryInfo = DimInfo.getDimInfo("country_code");
      if (countryInfo != null) {
         ArrayList valList = countryInfo.getValList();
         
         for (int i=0,is = valList == null ? 0 : valList.size();i<is;i++) {
             DimInfo.ValInfo valInfo = (DimInfo.ValInfo) valList.get(i);
             int id = valInfo.m_id;
             if (id < 0)
                continue;
             String name = valInfo.m_name;
             String iso2digitCode = valInfo.m_sn;
             String threeDigitCode = valInfo.getOtherProperty("_3digitCode");
             if (threeDigitCode == null)
                threeDigitCode = iso2digitCode; 
             
             MiscInner.CountryInfo country = new MiscInner.CountryInfo(id, iso2digitCode, threeDigitCode, name);
             if (g_countryInfo.size() <= id) {
                for (int t1=g_countryInfo.size(),t1s=id+1;t1<t1s;t1++)
                   g_countryInfo.add(null);                
             }
             g_countryInfo.set(id, country);                          
         }
      }
      DimInfo currencyInfo = DimInfo.getDimInfo("currency_list");
      if (currencyInfo != null) {
         
         DimInfo dataCurrSpec = DimInfo.getDimInfo("data_curr_spec");         
         ArrayList currencyValList = currencyInfo.getValList();
         
         for (int i=0,is = currencyValList == null ? 0 : currencyValList.size();i<is;i++) {
             DimInfo.ValInfo currVal = (DimInfo.ValInfo) currencyValList.get(i);
             int id = currVal.m_id;
             if (id < 0)
                continue;
             String name = currVal.m_name;
             String iso3digitCode = currVal.m_sn;
             String twoDigitCode = currVal.getOtherProperty("_2digitCode");
             if (twoDigitCode == null)
                twoDigitCode = iso3digitCode;
             int unitCode = Misc.getParamAsInt(currVal.getOtherProperty("def_unit_code"), MiscInner.UnitInfo.g_defaultUnitCode);
             MiscInner.CurrencyInfo currency = new MiscInner.CurrencyInfo(id, iso3digitCode, twoDigitCode, name, unitCode);
             if (g_currencyInfo.size() <= id) {
                for (int t1=g_currencyInfo.size(),t1s=id+1;t1<t1s;t1++)
                   g_currencyInfo.add(null);
             }
             g_currencyInfo.set(id, currency);
             if (dataCurrSpec != null) {
                dataCurrSpec.copyValInfo(currVal);                
             }
         }
      }
  }
  
  
  HashMap m_portInfo = null; //key integer val MiscInner.PortInfo
  
  //kind of pretty messy -
  //   getPortTree - only the port Tree
  //   printOutputDocTree - also has the project nodes attached
  //   internally it reuses the getPortTree to init the portTree and portNodeMap
  //   Finally you have the getPortNodeName - that gets the name that also uses the getPortTree to init  
  
  public synchronized void makePortTreeDirty() {  
     m_portInfo = null;
  }
  public boolean isPortTreeUseful(Connection dbConn) throws Exception { //checks to see if the port tree has All and unassigned only ..
     HashMap portMap = getPortTree(dbConn);
     return (portMap.size() > 2);     
  }

  public MiscInner.PortInfo getPortInfo(int portId, Connection dbConn) throws Exception {
     HashMap portMap = getPortTree(dbConn);     
     return (MiscInner.PortInfo) portMap.get(new Integer(portId));
  }  
  
  public HashMap getPortTree(Connection dbConn) throws Exception {     
     HashMap retval = m_portInfo;
     if (retval != null)
        return retval;
     synchronized (this) {     
       if (dbConn == null)
          return null;
       
       if (retval == null) {
          
          retval =  new HashMap(1500,0.75f);       
         
         try {       
            StringBuilder query  = new StringBuilder();
            query.append("select ");
            if (m_portNodeRestrictor != null && m_portNodeRestrictor.length() != 0) {
               query.append(" (case when( ").append(m_portNodeRestrictor).append(") then 1 else 0 end),");
            }
            else {
                query.append(" 0,");
            }
            query.append(Queries.GET_PORT_TREE);
            PreparedStatement pStmt = dbConn.prepareStatement(query.toString());          
            ResultSet rset = pStmt.executeQuery();
            MiscInner.PortInfo rootElement = null;
            while (rset.next()) {
               int portId = rset.getInt(2);
               Integer portIdInt = new Integer(portId);
               MiscInner.PortInfo portNode = (MiscInner.PortInfo) retval.get(portIdInt);
               if (portNode == null) {
                   portNode = new MiscInner.PortInfo(portId);
                   retval.put(portIdInt, portNode);
               }
               portNode.read(rset);
               int parentId = portNode.m_parentId;
               if (!Misc.isUndef(parentId)) {
                   Integer parentIdInt = new Integer(parentId);
                   MiscInner.PortInfo parentPort = (MiscInner.PortInfo) retval.get(parentIdInt);
                   if (parentPort == null) {
                       parentPort = new MiscInner.PortInfo(parentId);
                       retval.put(parentIdInt, parentPort);
                   }
                   portNode.addAsChildTo(parentPort);
               }
               else {
                   rootElement = portNode;
               }
            }
            rset.close();
            pStmt.close();
            MiscInner.PortInfo.loadGeneralParams(retval, dbConn);//for tracking            
            m_portInfo = retval;
         }
         catch (Exception e) {
                 throw e;
                 //e.printStackTrace();
  
         }
       }     
     }
     return retval;
  }
  
  public int updLHSRHSPortTree(Connection dbConn) throws Exception { //set updateNumberInDBStmt = null if the db need to be updated
     try {
         MiscInner.PortInfo top = getPortInfo(Misc.G_TOP_LEVEL_PORT, dbConn);
         PreparedStatement updLHSRHS = dbConn.prepareStatement(Queries.UPDATE_PORT_LHS_RHS);
         int retval = markLHSRHSAndUpdPortTree(top, 1, updLHSRHS);
         updLHSRHS.close();
         return retval;
     }
     catch (Exception e) {
         e.printStackTrace();
         throw e;
     }
  }
  
  public int markLHSRHSAndUpdPortTree(MiscInner.PortInfo n, int nextNumberToUse, PreparedStatement updateNumberInDBStmt) throws Exception { //set updateNumberInDBStmt = null if the db need to be updated
  
      //returns next number to use for the next markup
      try {
           if (n == null)
              return nextNumberToUse;
           n.m_lhsNumber = nextNumberToUse++;           
           for (int i=0,is = n.m_children == null ? 0 : n.m_children.size();i<is;i++) {
              MiscInner.PortInfo ch = (MiscInner.PortInfo)n.m_children.get(i);          
              nextNumberToUse = markLHSRHSAndUpdPortTree(ch, nextNumberToUse, updateNumberInDBStmt);
           }
           n.m_rhsNumber = nextNumberToUse++;            
           if (updateNumberInDBStmt != null) {
              updateNumberInDBStmt.setInt(1, n.m_lhsNumber);
              updateNumberInDBStmt.setInt(2, n.m_rhsNumber);
              updateNumberInDBStmt.setInt(3, n.m_id);
              updateNumberInDBStmt.execute();
           }
           
           return nextNumberToUse;
      }
      catch (Exception e) {
           e.printStackTrace();
           throw e;
      }
  }
  
  
  
  public MiscInner.PortInfo getParentPortNode(Connection dbConn, int portId, DimInfo dimInfo) throws Exception {
     HashMap portMap = getPortTree(dbConn);
     MiscInner.PortInfo portNode = getPortInfo(portId, dbConn);     
     if (dimInfo.m_refOrgLevel == null || dimInfo.m_refOrgLevel.size() == 0)
        return portNode;
     for (MiscInner.PortInfo n = portNode; n != null; n = n.m_parent) {
         
         int mot = n.m_orgType;
         if (dimInfo.inRefOrg(mot))
            return n;
     }
     return null;
  }

  public MiscInner.PortInfo getPortNode(Connection dbConn, int portId) throws Exception {
     return getPortInfo(portId, dbConn);     
  }
  
  public MiscInner.PortInfo getPortNodeExt(Connection dbConn, int portId) throws Exception {
     return getPortInfo(portId, dbConn);     
  }

  public boolean isAncestor(Connection dbConn, int descId, int ancId) throws Exception
  {
	  MiscInner.PortInfo desc = this.getPortNode(dbConn, descId);
	  
	  for (MiscInner.PortInfo n = desc; n != null; n = n.m_parent) {
      if (n.m_id == ancId)
         return true;
	  }
	  return false;
  }

  public boolean isAncestorOrg(Connection dbConn, int descId, int ancId) throws Exception
  {
    return isAncestor(dbConn, descId, ancId);	  
  }

  public ArrayList getAncestorPath(Connection dbConn, int descId) throws Exception
  {
	  return getAncestorPath(dbConn, descId, 1);
  }

  public ArrayList getAncestorPath(Connection dbConn, int descId, int ancId) throws Exception
  {
	  ArrayList retval = new ArrayList();
	  MiscInner.PortInfo desc = this.getPortNode(dbConn, descId);
	  
	  boolean found = false;
	  for (MiscInner.PortInfo n = desc; n != null; n = n.m_parent) {
		  
		  retval.add(new Integer(n.m_id));
		  if (n.m_id == ancId)
		  {
			  found = true;
			  break;
		  }
	  }
	  if (found)
		  return retval;
	  return null;
  }

  public String getFullPortName(Connection dbConn, int portId, String linkPage) throws Exception {
     MiscInner.PortInfo current = getPortInfo(portId, dbConn);     
     if (current == null)
        return "";
     StringBuilder temp = new StringBuilder();
     temp.setLength(0);
     if (linkPage != null) {
        temp.append("<a href=\"").append(linkPage).append("portfolio_id=").append(current.m_id).append("\">").append(current.m_name).append("</a>");
     }
     else {
        temp.append(current.m_name);        
     }
     String currName = temp.toString();
     
     if (1 != current.m_id) {
        for (MiscInner.PortInfo parent =  current.m_parent; parent != null ; parent =  parent.m_parent) {
          temp.setLength(0);
          portId = parent.m_id;
          if (linkPage == null && ((Misc.G_MITTAL_DOING && 2 == portId) || 1 == portId)) //dont want Group Name too
             break;
          if (linkPage != null) {
              temp.append("<a href=\"").append(linkPage).append("portfolio_id=").append(portId).append("\">").append(parent.m_name).append("</a>");
          }
          else {
              temp.append(parent.m_name);
          }
           currName = temp.toString() + " -> " + currName;
           if (1 == parent.m_id)
              break;
        }
     }
     return currName;
  }

  public String getPortName(Connection dbConn, int portId) throws Exception {
     MiscInner.PortInfo current = getPortInfo(portId, dbConn);
     
     if (current == null)
        return "";
     else
        return current.m_name;
  }

  public String getParentPortName(Connection dbConn, int portId, DimInfo dimInfo) throws Exception {
     MiscInner.PortInfo p = getParentPortNode(dbConn, portId, dimInfo);
     if (p == null)
        return "";
     return p.m_name;
  }


  
  public synchronized void cacheClearPortInfo() {
     //go thru all users and clearPortInfo() on the user     
     m_portVersion = null;
     
  }
  
  

  public UserMap getUserMap() {
      return userMap;
  }
  
  
  public  void returnUser(User user) {
     user.resetInUse();
  }
  
  public synchronized User loadUserInfo(int uid, String userName, HttpServletRequest request, ServletContext context) throws Exception {
     User user = null;
     Integer uLong = new Integer(uid);
     if ((user = (com.ipssi.gen.utils.User) getUserMap().get(uid)) == null) {
        user = new com.ipssi.gen.utils.User(request, context, uid);
        user.setUserName(userName); 
        user.setCache(this);
        getUserMap().addUser(user);
     }
     else { //set the context for this

        //user.setRequestAndContext(request, context);
     }
     request.setAttribute("_user", user);
     
     //user.request = request;
     //user.context = context;
     return user;
  }

  synchronized public void invalidateUserMap(int u) {
     getUserMap().invalidateUserMap(u);

  }



   /**
   * main
   * @param args
   */
  /*public static void main(String[] args) {
    try {


//       FileWriter fout = new FileWriter("c:\\test\\meta.xml");
//       PrintWriter outw = new PrintWriter(fout, true);
       MyXMLHelper test = new MyXMLHelper(null, null);

//       test.save(cache.partialMetaXML);
//       outw.close();

       DBConnectionPool _dbConnPool = new com.ipssi.gen.utils.DBConnectionPool();
       Connection _dbConnection = _dbConnPool.getConnection();
       Cache cache = new Cache(_dbConnection);       
       Document meta = null;
       Document meta2 = null;
//       meta = cache.getMetaDataTree(_dbConnection, -2, 1, 1);
//       meta2 = cache.getMetaDataTree(_dbConnection,1,1,1); // project 1
 //      meta2 = cache.getMetaDataTree(_dbConnection,2,1,6); // project 2
//       meta2 = cache.getMetaDataTree(_dbConnection,3,1,7); // project 3
//      meta2 = cache.getMetaDataTree(_dbConnection,4,1,10); // project 4
       _dbConnPool.returnConnection(_dbConnection);
//       test.save(meta); // for portfolio
       meta2 = null;
       test.save(meta2); // for project
//       outw.close();
    }
    catch (Exception e) {
//	   throw e;
       e.printStackTrace();
    }
//    System.out.println("Done Cache ..");

  }*/

    //from sameer ... 033006
   	
    public String getDimValidValsAsString(int dimId, int left_dim_val) throws Exception {
         DimInfo dimInfo = DimInfo.getDimInfo(dimId);         
         return getDimValidValsAsString(dimInfo, left_dim_val);
    }

    public String getDimValidValsAsString(int dimId) throws Exception {         
         return getDimValidValsAsString(dimId, Misc.getUndefInt());
    }

    
    
    
    
    public String getDimValidValsAsString(String left_dim_name, int left_dim_val) throws Exception {
       DimInfo leftDim = DimInfo.getDimInfo(left_dim_name);
       return getDimValidValsAsString(leftDim, left_dim_val);
    }
    
    public String getDimValidValsAsString(DimInfo leftDim, int leftDimVal) throws Exception {
       return leftDim.getBackwardCompatValidVals(leftDimVal);       
	  }

	// sameer 05082006


	public void printMultipleDimVals(String dimName, String[] selectedIds, JspWriter out) throws IOException {
		printMultipleDimVals(dimName, selectedIds, out, false);
	}

	public void printMultipleDimVals(String dimName, String[] selectedIds, JspWriter out, boolean globalUseAltName) throws IOException {
		DimInfo dimInfo = DimInfo.getDimInfo(dimName);
		ArrayList valList = dimInfo.getValList();
    
		for (int i = 0,count = valList == null ? 0 : valList.size(); i < count; i++) {
			DimInfo.ValInfo valInfo = (DimInfo.ValInfo)valList.get(i);
			int id = valInfo.m_id;
			String nameStr = null;
			if (globalUseAltName) {
				nameStr = valInfo.getOtherProperty("alt_name");
				if (nameStr == null || nameStr == "")
					nameStr = valInfo.m_name;
			}
			else {
				nameStr = valInfo.m_name;
			}
			
			String selectStr = "";
			if (selectedIds != null && selectedIds.length > 0) {
				for (int idCnt = 0; idCnt < selectedIds.length; idCnt++) {
					if ((Integer.toString(id)).equals(selectedIds[idCnt])) {
						selectStr = "selected";
					}
				}
			}
			out.println("<option value=\"" + id + "\" " + selectStr + ">" + nameStr + "</option>");
		}
	}
	// end sameer 05082006


	// sameer 06062006

	public String getDimValidValsAsString(String left_dim_name) throws Exception {
          return getDimValidValsAsString(left_dim_name, Misc.getUndefInt());
	}


  public ArrayList getValList(int dimId) {
     DimInfo dim = DimInfo.getDimInfo(dimId);
     return dim == null ? null : dim.getValList();
  }
  public ArrayList getValList(String catName) {
     DimInfo dim = DimInfo.getDimInfo(catName);
     return dim == null ? null : dim.getValList();
  }
	// end sameer 06062006

    public ArrayList m_userPrefFields = null; //DimConfigInfo
    public void loadUserPrefFields() {
       NodeList nl = internalXML.getElementsByTagName("user_pref");
       if (nl != null && nl.getLength() != 0) {
          Element m = (Element) nl.item(0);
          m_userPrefFields = DimConfigInfo.readRowColInfo(m, false);          
       }
    }
    
    // For unit profiler
    public ArrayList<ArrayList<DimConfigInfo>> m_unitProfileDef = null; //DimConfigInfo
    public void loadUnitProfileDef() {
       NodeList nl = internalXML.getElementsByTagName("unit_profile_def");
       if (nl != null && nl.getLength() != 0) {
          Element m = (Element) nl.item(0);
          m_unitProfileDef = DimConfigInfo.readRowColInfo(m, false);          
       }
    }

    // For unit profiler
    public ArrayList<ArrayList<DimConfigInfo>> m_scaleProfileDef = null; //DimConfigInfo
    public void loadScaleProfileDef() {
       NodeList nl = internalXML.getElementsByTagName("scale_profile_def");
       if (nl != null && nl.getLength() != 0) {
          Element m = (Element) nl.item(0);
          m_scaleProfileDef = DimConfigInfo.readRowColInfo(m, false);          
       }
    }
    // For unit profiler
    public ArrayList<ArrayList<DimConfigInfo>> m_formatProfileDef = null; //DimConfigInfo
    public void loadFormatProfileDef() {
       NodeList nl = internalXML.getElementsByTagName("format_profile_def");
       if (nl != null && nl.getLength() != 0) {
          Element m = (Element) nl.item(0);
          m_formatProfileDef = DimConfigInfo.readRowColInfo(m, false);          
       }
    }
    public ArrayList getUnitProfileDef(){
    	return m_unitProfileDef;
    }
    public ArrayList getScaleProfileDef(){
    	return m_scaleProfileDef;
    }
    public ArrayList getFormatProfileDef(){
    	return m_formatProfileDef;
    }
    
    private static int getParentOrgId(Connection conn, int portNodeId) throws Exception {
		PreparedStatement ps = null;
		int retVal = portNodeId;
		try {
			String query = "select port_node_id from port_nodes where id = ?";
			ps = conn.prepareStatement(query);
			ps.setInt(1, portNodeId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				retVal = rs.getInt("port_node_id");
			} else {
				retVal = Misc.G_TOP_LEVEL_PORT;
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return retVal;
	}

	public static int getParentOrganization(Connection conn, int portNodeId) {

		int retVal = portNodeId;
		try {
			if (portNodeId != Misc.G_TOP_LEVEL_PORT) {
				int parent = getParentOrgId(conn, portNodeId);
				if (parent == Misc.G_TOP_LEVEL_PORT) {
					retVal = portNodeId;
				} else if (parent == portNodeId) {
					return portNodeId;
				} else {
					return getParentOrganization(conn, parent);
				}
			}
		} catch (Exception e) {
			return Misc.G_TOP_LEVEL_PORT;
		}
		return retVal;
	}

	public static int getParentOrganization(int portNodeId)  {
		Connection conn = null;
		int retVal = portNodeId;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			retVal = getParentOrganization(conn, portNodeId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception e) {
				System.out.println("CacheManager.getParentOrganization() : Cannot Return Connection");
				e.printStackTrace();
			}
		}
		return retVal;
	}
}




