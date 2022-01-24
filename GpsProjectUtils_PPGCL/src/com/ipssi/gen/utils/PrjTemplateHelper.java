// Copyright (c) 2000 IntelliPlanner Software Systems, Inc.
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

import java.text.*;
public  class PrjTemplateHelper extends Object {
//NEED to redesign ..... separate out frontpage stuff with detail stuff
//                 ..... make the detail stuff update general across different types of objects

//public vars
//globals ..
//need to clean these two vars
   
   public static HashMap g_frontPageList = new HashMap(); //of FrontPageInfo
   

   public static ArrayList readRowColInfo(Element topElem, boolean readParamMissingMeansRead) { //Elements are ArrayList of DimConfigInfo
       ArrayList result = null;
       for (Node ch = topElem.getFirstChild();ch != null; ch = ch.getNextSibling()) { //row
           if (ch.getNodeType() != Node.ELEMENT_NODE)
                continue;
           ArrayList perRowInfo = null;
           Element elem = (Element) ch;

           for (Node level2Ch = elem.getFirstChild(); level2Ch != null; level2Ch = level2Ch.getNextSibling()) { //col
               if (level2Ch.getNodeType() != Node.ELEMENT_NODE)
                  continue;
               Element level2Elem = (Element) level2Ch;
               DimConfigInfo cfInfo = DimConfigInfo.getDimConfigInfo(level2Elem,readParamMissingMeansRead);
               if (cfInfo == null)
                  continue;
               if (perRowInfo == null)
                  perRowInfo = new ArrayList();
               perRowInfo.add(cfInfo);
           }//each level2 ch (col)
           if (perRowInfo == null)
               continue;
           if (result == null)
               result = new ArrayList();
           result.add(perRowInfo);
       }//each lelve1 ch (row)
       return result;
   }


   public static FrontPageInfo getFrontPage(String fileName, boolean doReload, Connection dbConn, Cache cache) throws Exception {
	   FrontPageInfo retval = (FrontPageInfo) g_frontPageList.get(fileName);
	   if (retval == null || doReload) {
		   retval = createFrontPageEntry(fileName, dbConn, cache);
		   synchronized (g_frontPageList){
			   g_frontPageList.put(fileName, retval);
		   }		   
	   }
	   return retval;
   }
   
   
   
//instance funcs
   private static void helpReadFrontHeader(Element elem, FrontPageInfo entry, Connection dbConn, Cache cache) throws Exception {
   //<header><filter><row><col/></row></filter><block><row><col/></row></block></header>
       try {
           //filter
           //row, col ..
           for (Node n = elem.getFirstChild(); n != null ; n=n.getNextSibling()) {
              if (n.getNodeType() != 1)
                 continue;
              Element e = (Element) n;
              String tagName = e.getTagName();
              if ("filter".equals(tagName)) {
                 entry.m_headerFilterList = readRowColInfo(e, true);
              }
              else if ("block".equals(tagName)) {
                 entry.m_headerInfo = readRowColInfo(e, true);
              }
           }
           if (entry.m_headerInfo != null) {
              entry.m_headerValGetter = new FrontGetValHelper(entry.m_headerInfo, true);
              StringBuilder selClause = new StringBuilder();
              StringBuilder fromClause = new StringBuilder();
              StringBuilder joinClause = new StringBuilder();
              


              StringBuilder first = new StringBuilder();
              first.append("select ").append(selClause);
//              first.append(" from ").append(fromClause); TO_PORT_FORWARD
              StringBuilder second = new StringBuilder();
              if (joinClause.length() > 0 || !entry.m_headerTimeReq.m_attribSelAreNotPrjDep)
                 second.append(" where ");
              if (joinClause.length() > 0)
                 second.append(joinClause);
              else
                 second.append(" 1 = 1 ");
              if (!entry.m_headerTimeReq.m_attribSelAreNotPrjDep) { 
                 second.append(" and (pj_map_items.map_type in (1)) and (alternatives.is_primary = 1) and (alt_map_items.map_type in (1)) "); //122607 .. added alt_map_items.map_type in and changed 1,4 to 1
              }
              entry.m_headerQuerySelPart = first.toString(); //TO_PORT_FORWARD
              entry.m_headerQueryFromPart = fromClause.toString(); //first.toString();//TO_PORT_FORWARD
              entry.m_headerQueryAfterWhere = second.toString();              
           }
       }
       catch (Exception e) {
           e.printStackTrace();
           throw e;
       }
   }

   private  static FrontPageInfo createFrontPageEntry(String fileName, Connection dbConn, Cache cache) throws Exception {//synced at Cache
          //now load the front page
	   int templateId = Misc.getUndefInt();
	   String name = null; 
	   String desc = null;
	   String label = null;
	   boolean inplaceEdit = false;
	   boolean showSearchFirstTime=true;
	   boolean createProject=false;
	   boolean multiSelect=true;
	   String targetPage= null;
	   String targetContext=null;
	   
       try {
           FileInputStream inp = new FileInputStream(Cache.serverConfigPath+System.getProperty("file.separator")+"web_look"+System.getProperty("file.separator")+"project"+System.getProperty("file.separator")+fileName);
           MyXMLHelper test = new MyXMLHelper(inp, null);
           Document frontXML =  test.load();
           inp.close();
           if (frontXML == null && frontXML.getDocumentElement() == null) {
               return null;
           }
           FrontPageInfo entry = new FrontPageInfo();
           entry.m_label = label;
           entry.m_help = frontXML.getDocumentElement().getAttribute("help");
           entry.m_doinplaceEdit = inplaceEdit;
           entry.m_showSearchFirstTime = showSearchFirstTime;
           entry.m_id = templateId;
           entry.m_fileName = fileName;
           entry.m_createProject = createProject;           
           entry.m_multiSelect = multiSelect;
           entry.m_targetPage = targetPage;
           entry.m_targetContext = targetContext;
           Element topElem = frontXML.getDocumentElement();

           entry.m_templateId = topElem.getAttribute("create_template_id");
           entry.m_page_target_detail = topElem.getAttribute("_page_detail");
           entry.m_topMenu = topElem.getAttribute("_top_menu");
           entry.m_createPrjContext = topElem.getAttribute("page_create");
           entry.m_isForApprovalSpecial = Misc.getParamAsInt(topElem.getAttribute("is_for_approval_special"),0);
           entry.m_createCheckTag = Misc.getParamAsString(topElem.getAttribute("create_check"), null);//rajeev 040808
           if (entry.m_templateId != null && entry.m_templateId.length() == 0)
              entry.m_templateId = null;
           if (entry.m_page_target_detail != null && entry.m_page_target_detail.length() == 0)
              entry.m_page_target_detail = null;
           if (entry.m_topMenu != null && entry.m_topMenu.length() == 0)
              entry.m_topMenu = null;
           entry.m_createLabel = Misc.getParamAsString(topElem.getAttribute("create_label"), entry.m_createLabel);
           entry.m_manageLabel = Misc.getParamAsString(topElem.getAttribute("manage_label"), entry.m_manageLabel);
           entry.m_interleavedTemplateId = Misc.getParamAsInt(topElem.getAttribute("interleaved_template"));
           entry.m_objectIdParamLabel = Misc.getParamAsString(topElem.getAttribute("object_param"), entry.m_objectIdParamLabel);
           entry.m_objectIdColName = Misc.getParamAsString(topElem.getAttribute("object_col_name"), null);
           entry.m_interleavedInstructText = Misc.getParamAsString(topElem.getAttribute("interleaved_text"), "&nbsp;");
           entry.m_doingSupplier = "1".equals(topElem.getAttribute("for_supplier"));
           entry.m_doingOrder = "1".equals(topElem.getAttribute("for_order"));
           //entry.m_hackDoGetPrjListFromOrderToo = "1".equals(topElem.getAttribute("get_prj_list_from_order_access"));
           
           String temp = Misc.getParamAsString(topElem.getAttribute("priv_to_check"), null);
           if (temp != null)
              entry.m_privTagToCheck = Misc.convertValToArray(temp);

           

           Element searchNode = null;
           Element optionNode = null;


//old           StringBuilder selClause = new StringBuilder();
           for (Node n = frontXML.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
              if (n.getNodeType() != 1)
                 continue;
              Element elem = (Element) n;
              if (elem.getTagName().equals("search")) {
                 searchNode = elem;
                 continue;
              }
              else if (elem.getTagName().equals("option")) {
                 optionNode = elem;
                 continue;
              }
              else if (elem.getTagName().equals("header")) {
                 helpReadFrontHeader(elem, entry, dbConn, cache);
                 continue;
              }
              else if (elem.getTagName().equals("check_box_filter")) {
                  entry.m_checkBoxFilter = new ArrayList();
                  ArrayList addTo = entry.m_checkBoxFilter;
                  for (Node n1 = elem.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
                     if (n1.getNodeType() != 1)
                        continue;
                     Element e1 = (Element) n1;
                     DimInfo.DimValList dimValList = DimInfo.DimValList.readDimValList(e1);
                     if (dimValList != null)
                        addTo.add(dimValList);
                  }                 
              }
              else if (!elem.getTagName().equals("col")) {
                 continue;
              }
              
              DimConfigInfo dimConf = DimConfigInfo.getDimConfigInfo(elem, true);
              entry.m_frontInfoList.add(dimConf);
              int id = dimConf.m_dimCalc.m_dimInfo == null ? Misc.getUndefInt() : dimConf.m_dimCalc.m_dimInfo.m_id;
              if (!Misc.isUndef(id)) {
                  ColumnMappingHelper colMap = cache.getColumnMapping(id);
                  if (colMap != null && !colMap.table.equalsIgnoreCase("dummy")) {
                      if (colMap.table.equalsIgnoreCase("prj_plan_date_helper")) {
                         entry.m_hasPlanDateLikeStuff = true;
                      }
                  }
              }
              
           }
           if (searchNode != null) {
               entry.m_frontSearchCriteria = readRowColInfo(searchNode, false);
               entry.m_hideSearchGlobally = "1".equals(searchNode.getAttribute("hidden"));
               //TODO - right now if date search is required then it should also be present in the view list
               for (int l=0,ls=entry.m_frontSearchCriteria.size();l<ls;l++) {
                   ArrayList r = (ArrayList) entry.m_frontSearchCriteria.get(l);
                   for (int k=0,ks = r.size();k<ks;k++) {
                      DimConfigInfo dc = (DimConfigInfo) r.get(k);
                      DimInfo di = dc.m_dimCalc.m_dimInfo;
                      ColumnMappingHelper colMap = di == null ? null : di.m_colMap;
                      if (colMap.table.endsWith("_multi_attrib") || colMap.table.equals("orders_suppliers")) {
                         MiscInner.PurSearchItemPart toAdd = new MiscInner.PurSearchItemPart();
                         toAdd.m_dimInfo = di;
                         toAdd.m_purConds = new ArrayList();
                         entry.m_frontExtMultiCriteria.add(toAdd);
                         MiscInner.PurSearchCondPart condPart = new MiscInner.PurSearchCondPart();
                         toAdd.m_purConds.add(condPart);
                         if (di.m_type == Cache.LOV_TYPE) 
                            condPart.m_condType = 1;//oneOf                         
                         else if (di.m_type == Cache.STRING_TYPE)
                            condPart.m_condType = 9;//like
                         else
                            condPart.m_condType = 5;//greater or equal
                         
                      }
                   }
               }
           }
           if (optionNode != null) {

               if (true) {
                  for(Node on = optionNode.getFirstChild();on!=null;on=on.getNextSibling()) {
                     if (on.getNodeType() != 1)
                        continue;
                     Element oe = (Element) on;
                     int id = Misc.getParamAsInt(oe.getAttribute("id"));
                     String code = oe.getAttribute("code");
                     String targetTag = Misc.getParamAsString(oe.getAttribute("target_tag"), code);
                     String olabel = oe.getAttribute("label");
                     String directPage = oe.getAttribute("page");
                     String paramTemplate = oe.getAttribute("param_template");
                     boolean doPopup = "1".equals(oe.getAttribute("do_popup"));
                     boolean suspendTimer = "1".equals(oe.getAttribute("suspend_timer"));
                     String javascriptOptional = Misc.getParamAsString(oe.getAttribute("pre_handler"), null);
                     
                     if (Misc.isUndef(id) || code == null || code.length() == 0 || olabel == null || olabel.length() == 0)
                        continue;
                     OptionInfo option = new OptionInfo(id, code,olabel, directPage, paramTemplate, doPopup, suspendTimer, targetTag, javascriptOptional);
                     entry.m_manageOptions.add(option);
                  }
               }
           }
           if (entry.m_manageOptions.size() == 0)
              entry.m_multiSelect = false;
           entry.m_hasMultiple = entry.needsMulti();
           entry.fillCheckBoxIndexLookup();
           entry.fillColMapper();
           entry.getIndexInSumOfForDim();
           entry.m_valGetter = new FrontGetValHelper(entry.m_frontInfoList, false);
           entry.m_searchGetter = new FrontGetValHelper(entry.m_frontSearchCriteria, true);
           
           entry.m_checkBoxLinkPos = entry.getCheckBoxLinkIndex();
           entry.getFilterForValidationEtc();
           entry.m_accessCheckControlInfo = FrontPageInfo.getAccessControlInfo(entry.m_frontInfoList);
           entry.m_topLevelObjIdPortIdIndices = FrontPageInfo.helpGetObjIdOrgIndexWoNest(entry.m_frontInfoList);//0425
           return entry;



//old           createPrjInfoFrontPageQueryOld(selClause, entry);
       }
       catch (Exception e) {
           e.printStackTrace();
           throw e;
       }
   }

 }
