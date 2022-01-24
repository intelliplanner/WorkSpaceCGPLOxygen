// Copyright (c) 2000 IntelliPlanner Software Systems, Inc.
package com.ipssi.gen.utils;
import java.io.FileInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.reporting.customize.ReportDetailVO;

public class FrontPageInfo implements Serializable {
	/**
	 * 
	 */
    //ESC M for 12 cpi, ESC P for 10 cpi ESC g for 15 cpi
	//Esc x 1 for NLQ Esc x 0 for Draft
	//Esc k 0 roman, Esc k 1 sans serif, Esc k 2 courier

	private static final long serialVersionUID = 1L;
	public ChartInfo chartInfo=null;
	public char textPrinterRowMarker = '-';
	public boolean textPrinterDoHeaderRowMarker = true;
	public boolean textPrinterDoIntermediareRowMarker = false;
	public boolean textPrinterDoGroupingRowMarker = true;
	public String textPrinterInBetweenColSeparator = "  ";
	public String textPrinterRowStarter = "";
	public String textPrinterRowEnder = "";
	public int textMaxRowsPerPage = 88;
	public int textBlankHeaderLine = 4;
	public int textBlankFooterLine = 3;

	public String textEscCode = "ESC@ESC0ESCM";
	public String textFirstPageHeaderFile = null;
	public String textLastPageFooterFile = null;
	public String textInnerPageHeaderFile = null;
	public String refTextInnerPageHeader = "SECL %project_name% Project";
	public String textInnerPageFooterFile = null;
	public String m_driverObjectLocTracker = "vehicle";
	public int m_orgTimingBased = 2; //0 dont do regardless of global parameter, 1 do regardless of global parameter 2 only if global parameter says yes
	public ArrayList<Integer> subQueryDrivers = null;
	public ArrayList<Integer> subQueryTimeGran = null;
	public ArrayList<Integer> subQueryRelativeTimeDelta = null;
	public String projectNameLookupFieldName = null;
	public int projectNameLookupFieldIndex = -1;
	public int m_doPivotMeasureFirstRow = 1; //0 => measure 2nd row, 2 (not implemented) - dont print measure if not multiple
	public int m_queryTimeOut = 0; // Set Query Time Out
	public String m_createURLLocTracker = null; //merge with CapEx
	public String m_createButtonNameLocTracker = "Create"; //merge with CapEx
	public String m_manageButtonNameLocTracker = "View Details"; //merge with CapEx
	public String m_createButtonAccessControl = null; //merge with CapEx
	public String m_addnlSearchButton = null;
	public String m_addnlSearchButtonRetainDim = null;
	public String m_commonLinkParamTemplate = null;
	public String m_limitByColNames = null;
	public String m_limitAcrossColNames = null;
	public String m_limitOrderByColNames = null;
	public String m_orderByAttribId = null;
	public ArrayList<Integer> m_orderIds = null;//it is colIndex of valid (Order by + 1) - if negative then desc, if possitive then asc +1 to handle idex = 0
	public ArrayList<Integer> m_limitColIndices = null;
	public ArrayList<Integer> m_limitAcrossColIndices = null;
	public ArrayList<Integer> m_limitOrderByColIndices = null; //asc will be positive, desc will be negative as follows: -10000+col index  = value that will be stored here
	public DimConfigInfo.LinkHelper m_commonLinkHelper = null;//created in postProcess of FrontPageInfo
	//for headers
	public ArrayList m_headerFilterList = null; //of ArrayList of DimConfigInfo .. simply to help with getting filters etc.
	public ArrayList m_headerInfo = null; //of ArrayList of DimConfigInfo
	public FrontGetValHelper m_headerValGetter = null;
	public MiscInner.TimeReq m_headerTimeReq = null;
	/* ... remove this after changeover
	public StringBuilder m_headerQuery = null;
	*/
	public String m_headerQuerySelPart; //TO_PORT_FORWARD
	public String m_headerQueryFromPart;//TO_PORT_FORWARD
	public String m_headerQueryAfterWhere;
	//public String getHeaderQuery();
	//for others ..
	public String m_label = "Your Current Projects";
	public String m_help = null;
	public boolean m_doinplaceEdit = false;
	public boolean m_showSearchFirstTime = false;
	public ArrayList m_frontInfoList = new ArrayList();
	public HashMap<String, Integer> m_colIndexLookup = new HashMap();
	public ArrayList<Integer> m_colIndexUsingExpr = null; //initialized in postProcess
	public ArrayList m_frontSearchCriteria = new ArrayList();//ArrayList<ArrayList<DimConfigInfo>>
	
	// DashBoard info 
	public ArrayList m_frontDashboardList = new ArrayList();
	public ArrayList m_frontButtonList = new ArrayList();
	public ArrayList m_frontExtMultiCriteria = new ArrayList(); //arrayList of MiscInner.PurSearchItemPart //the cond Part is to be filled separately

	public ArrayList m_multiAttribQueryParts = null; //will be populated through getEnhM ..
	public boolean m_hasMultiple = false;
	/* ... remove this after 
	public String m_frontSelFirstPart; //will have select xxxx 
	public String m_frontSelSecondPart;//will not have from, else will have whole of from clause excluding sel for prj_portfolio_map
	public String m_frontSelThirdPart;//will not have order by but anything after order by
	public String m_frontJustPrjIdSelForFilter; //it will noe just have the select xxxx others need to be picked from second part, third part etc.
	*/
	public String m_frontSelPart; //will have select xxxx 
	public String m_frontFromPart;//will not have from, else will have whole of from clause excluding sel for prj_portfolio_map
	public String m_frontWherePart;//will not have order by but anything after order by
	public String m_frontOrderPart;//
	public String m_frontPrjIdSelOnly; //will have select prj_id ...
	public String m_frontPrjIdOnlyFromPart;//currently fixed based upon objects being asked for .. but should be examined dynamically

	public String m_frontJustPrjIdSelForFilter; //it will noe just have the select xxxx others need to be picked from second part, third part etc.
	public String m_frontCustomPrjIdFilterTable = null; //the must have form of select .. pid ... the query creator will put in other join etc.
													  //if any parameters are needed assume that they are the 1st set of parameters needed after setting up of uid       
	public boolean m_createProject = true;
	public boolean m_doZeroRow = false;
	public String m_createCheckTag = null;//rajeev 040808
	public boolean m_multiSelect = false;
	public boolean m_hideSearchGlobally = false;
	public int m_isForApprovalSpecial = 0;
	//      public boolean m_showMergeOption = false;
	//      public boolean m_showGroupOption = false;
	//replaced by m_colIndexLookup ..public HashMap m_colMapper = new HashMap(); //maps columnName to the pos in the frontInfoList ... to make life easier for custom print of columns
   

	public ArrayList m_manageOptions = new ArrayList(); //of OptionInfo
	public String m_targetPage = "project_detail.jsp";
	public String m_targetContext = "prj_basic";
	public String m_customField1 = null;
	public String m_customField2 = null;
	public String m_customField3 = null;
	public String m_createLabel = "Create Project";
	public String m_manageLabel = "Manage Projects";
	public ArrayList m_checkBoxFilter = null; //ArrayList of DimValList, dim must be in the col selected. 
	public ArrayList m_checkBoxFilterColIndexLookup = null; // of DimConfigInfo.Pair, indexed by index in m_checkBoxFilter and tells posn in FrontInfoList
	public boolean m_hasPlanDateLikeStuff = false;
	int m_id;
	String m_fileName = "front_page.xml";
	public boolean m_doRollupAtJava = true;//20160420 - rollup at Java preferred
	private ArrayList m_getDimList = new ArrayList(); //for hacking of actuals+forecast, if status = xxx, inv request else cash need
	public FrontGetValHelper m_valGetter = null;
	public FrontGetValHelper m_searchGetter = null;
	//      public ArrayList m_paramCodeDate = null;//param date code 0=>beg of CY, 1=>endOfCYEx, 3=>currDate, 4=>currDate+1
	public MiscInner.TimeReq m_queryTimeReq = null;

	public String m_templateId = null;
	public String m_page_target_detail = null; //the _page_detail to use in creating link,
	public String m_topMenu = null;
	public String m_createPrjContext = null;
	public int m_interleavedTemplateId = Misc.getUndefInt();
	public String m_interleavedInstructText = null;
	public String m_objectIdParamLabel = "vehicle_id";
	public String m_objectIdColName = "d20274";
	public MiscInner.Pair m_checkBoxLinkPos = null;//will be created in creation of FrontPage       
	public String m_hackAddnlFilterForValidationEtc = null;

	public boolean m_doingSupplier = false;
	public boolean m_doingOrder = false;
	public boolean m_preventGrouping =false;
	public String[] m_privTagToCheck = null;
	//public boolean m_hackDoGetPrjListFromOrderToo = false; //not used .. currently used ... may be changed to be smarter to look from privTagToCheck
	public HashMap m_accessCheckControlInfo = null;
	public ArrayList m_topLevelObjIdPortIdIndices = null; //0425 populated using ..helpGetObjIdOrgIndexWoNest(dimConfigList)
    public boolean m_hackTrackDriveTimeTableJoinLoggedData = false;
    public static DimConfigInfo getEntryForInSearch(ArrayList<ArrayList<DimConfigInfo>> searchCriteria, int dimId) {
    	for (int i=0,is=searchCriteria == null ? 0 : searchCriteria.size(); i<is;i++) {
    		ArrayList<DimConfigInfo> row = searchCriteria.get(i);
    		for (int j=0,js=row == null ? 0 : row.size(); j<js; j++) {
    			DimConfigInfo dci = row.get(j);
    			if (dci != null && dci.m_dimCalc != null && dci.m_dimCalc.m_dimInfo != null && dci.m_dimCalc.m_dimInfo.m_id == dimId) {
    				return dci;
    			}
    		}
    	}
    	return null;
    }
    public static boolean isSearchDimForSubQ(ArrayList<ArrayList<DimConfigInfo>> searchCriteria, int dimId, int subQIndex) {
    	DimConfigInfo dci = getEntryForInSearch(searchCriteria, dimId);
    	if (dci != null) {
    		if (dci.includeFilterFromSubQ != null && dci.includeFilterFromSubQ.size() > 0) {
	    		for (int i=0,is=dci.includeFilterFromSubQ.size();i<is;i++)
	    			if (dci.includeFilterFromSubQ.get(i) == subQIndex)
	    				return true;
	    		return false;
    		}
    		else if (dci.excludeFilterFromSubQ != null && dci.excludeFilterFromSubQ.size() > 0) {
	    		for (int i=0,is=dci.excludeFilterFromSubQ.size();i<is;i++)
	    			if (dci.excludeFilterFromSubQ.get(i) == subQIndex)
	    				return false;
	    		return true;
    		}
    		else {
    			return true;
    		}
    	}
    	return false;
    }
	public void getFilterForValidationEtc() {
		StringBuilder temp = new StringBuilder();
		for (int i=0,is = m_frontInfoList.size();i<is;i++) {
			DimConfigInfo dimConfig = (DimConfigInfo) m_frontInfoList.get(i);
			if (dimConfig != null && dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_colMap != null) {
				ColumnMappingHelper colMap = dimConfig.m_dimCalc.m_dimInfo.m_colMap;
				String tab = colMap.table.toLowerCase();

				if (tab.startsWith("v_prjv_")) {
					if (temp.length() > 0) {
						temp.append(" or ");
					}
					else {
						temp.append("(");
					}
					temp.append(tab).append(".prj_id is not null ");
				}                
			}
		}
		if (temp.length() != 0) {
			temp.append(")");
			m_hackAddnlFilterForValidationEtc = temp.toString();
		}          
	}

	public boolean matchesCheckBoxCrit(Connection dbConn, ResultSet rset, Cache cache) throws Exception {
		if (m_checkBoxFilter == null || m_checkBoxFilter.size() == 0)
			return true;
		boolean retval = true;
		for (int i=0,is = m_checkBoxFilter.size();i<is;i++) {
			DimConfigInfo.Pair pos = (DimConfigInfo.Pair) m_checkBoxFilterColIndexLookup.get(i);
			DimInfo.DimValList dv = (DimInfo.DimValList) m_checkBoxFilter.get(i);
			if (pos == null || dv.getValList() == null || dv.getValList().size() == 0)
				continue;
			int val = m_valGetter.getValInt(dbConn, rset, pos.m_rowIndex, pos.m_colIndex, cache);
			boolean found = false;
			for (int j=0,js = dv.getValList().size();j<js;j++) {
				int v = ((Integer)dv.getValList().get(j)).intValue();
				if (v == val) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;         
	}
      
	public void fillCheckBoxIndexLookup() {
		if (m_checkBoxFilter == null)
			return;
		m_checkBoxFilterColIndexLookup = new ArrayList();
		for (int i=0,is=m_checkBoxFilter.size();i<is;i++) {
			DimInfo.DimValList dv = (DimInfo.DimValList) m_checkBoxFilter.get(i);
			DimConfigInfo.Pair pos = null;
			if (dv.m_dimInfo != null) {
				int dimId = dv.m_dimInfo.m_id;
				for (int k=0, ks = m_frontInfoList.size();k<ks;k++) {
					DimConfigInfo dc1 = (DimConfigInfo)m_frontInfoList.get(k);
					int dc1Id = dc1.m_dimCalc.m_dimInfo != null ? dc1.m_dimCalc.m_dimInfo.m_id : Misc.getUndefInt();
					if (dc1Id == dimId) {
						pos = new DimConfigInfo.Pair(0,k);
						break;
					}//found match
				}//for each col
			}//if valid dim ref in filter clause
			m_checkBoxFilterColIndexLookup.add(pos);
		}//for each dim ref in filter clause
	}//end of func
      
	public void fillColMapper() {
		for (int i=0, is = m_frontInfoList.size();i<is;i++) {
			DimConfigInfo dimConfig = (DimConfigInfo)m_frontInfoList.get(i);
			if (dimConfig != null && dimConfig.m_columnName != null && dimConfig.m_columnName.length() != 0) {
				if (m_colIndexLookup == null)
					m_colIndexLookup = new HashMap();
				m_colIndexLookup.put(dimConfig.m_columnName, new Integer(i));
			}            
		}//for each row
	}

	public int getColIndexByName(String name) {
		Integer retval = (Integer)m_colIndexLookup.get(name);
		if (retval == null)
			return -1;
		return retval.intValue();
	}

	public void getIndexInSumOfForDim() {//puts in the rowIndex/ColIndex and if necessary will create hidden cols at the end of the row
		for (int i=0, is = m_frontInfoList.size();i<is;i++) {
			DimConfigInfo dimConfig = (DimConfigInfo)m_frontInfoList.get(i);
			//if (dimConfig != null && dimConfig.m_dimCalc.m_dimInfo != null) {
			//	m_colMapper.put(new Integer(i), new Integer(dimConfig.m_dimCalc.m_dimInfo.m_id));
			//}
			for (int l1=0,l1s = dimConfig.m_fixedForSel == null ? 0 : dimConfig.m_fixedForSel.size();l1<l1s;l1++) {
				DimConfigInfo.FixedHelper fix = (DimConfigInfo.FixedHelper) dimConfig.m_fixedForSel.get(l1);
				int dimId = fix.m_idThenIndex;
				for (int k=0, ks = m_frontInfoList.size();k<ks;k++) {
					DimConfigInfo dc1 = (DimConfigInfo)m_frontInfoList.get(k);
					int dc1Id = dc1.m_dimCalc.m_dimInfo != null ? dc1.m_dimCalc.m_dimInfo.m_id : Misc.getUndefInt();
					if (dc1Id == dimId) {
						fix.m_idThenIndex = k;
						break;
					}
				} //for
			}//if there was a fixed
			if (dimConfig.m_calcBy != null && dimConfig.m_calcBy.length() != 0) {
				StringTokenizer strTok = new StringTokenizer(dimConfig.m_calcBy, ",", false);
				boolean first = true;
				while (strTok.hasMoreTokens()) {
					String tok = strTok.nextToken();
					int dimId = com.ipssi.gen.utils.Misc.getParamAsInt(tok);
					DimConfigInfo.Pair pos = null;
					if (!com.ipssi.gen.utils.Misc.isUndef(dimId)) {
						//check which of the rows/cols does this dimId belong to
						for (int k=0, ks = m_frontInfoList.size();k<ks;k++) {
							DimConfigInfo dc1 = (DimConfigInfo)m_frontInfoList.get(k);
							int dc1Id = dc1.m_dimCalc.m_dimInfo != null ? dc1.m_dimCalc.m_dimInfo.m_id : Misc.getUndefInt();
							if (dc1Id == dimId) {
								pos = new DimConfigInfo.Pair(0,k);
								break;
							}
						}
					}//if there is a mactch
					if (pos != null) {
						if (dimConfig.m_calcByMap == null)
							dimConfig.m_calcByMap = new ArrayList();
						dimConfig.m_calcByMap.add(pos);
					}
				}//strtok while
			}//if interpretation is required
		}//for each row
	} //end of func

	public static ArrayList getPerUserOptions(SessionManager session, ArrayList origList, User user) throws Exception {
		try {
			ArrayList retval = null;
			for (int i=0,is=origList.size();i<is;i++) {
				OptionInfo option = (OptionInfo) origList.get(i);
				if (user.isPrivAvailable(session, option.m_code, true, Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt())) {//TODO_PRIV_CHECK
					if (retval == null)
						retval = new ArrayList();
					retval.add(option);
				}
			}
			return retval;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
    public static void getAllListMenuDiv(SessionManager session, DimConfigInfo dimConfig, StringBuilder retval, boolean doFirstURLOnly, String linkMenuName) throws Exception {
       // will generate the div to show the menu in project list page
          try {
        
              if (dimConfig == null || dimConfig.m_innerMenuList == null || dimConfig.m_innerMenuList.size() == 0)
                 return;
              if (linkMenuName == null || linkMenuName.length() == 0)
            	  linkMenuName = "page_link_menu";
              int len = retval.length();
              User user = session.getUser();
              if (!doFirstURLOnly)
            	  retval.append("<DIV ID='").append(linkMenuName).append("' STYLE='display:none;width:200px' ><div class='menuholder'>");
              for (int i=0,is = dimConfig.m_innerMenuList.size();i<is;i++) {
                 DimConfigInfo.InnerMenuInfo innerMenu = (DimConfigInfo.InnerMenuInfo) (dimConfig.m_innerMenuList.get(i));
                 if (innerMenu == null) {
                    continue;
                 }
                 boolean doPopup = innerMenu.m_doPopup;
                 if (!user.isPrivAvailable(session, innerMenu.m_menuTag))
                     continue;
                 if (innerMenu.m_id == 0) {
                     String pgContext = innerMenu.m_targetTag; //innerMenu.m_menuTag;
                     if (pgContext == null)
                        pgContext = session.getParameter("page_context");
                     if (pgContext == null || pgContext.length() == 0)
                        continue;
                     UserGen.Menu elem = UserGen.menuGetElem(session.getUser(), session, session.getMenu(), pgContext, null, null, true);                     
                     
                     if (elem == null || elem.m_menu.m_url.endsWith("home.jsp")) {
                        MenuItem elemItem = MenuItem.getMenuInfo(pgContext);
                        if (elemItem != null) {
                           UserGen.getListPageMenuDivByMenuItem(session, elemItem, retval, innerMenu.m_ofSameLevel, doFirstURLOnly, doPopup);
                           if (doFirstURLOnly && len != retval.length()) {
                        	   return;
                           }
                        }
                     }
                     else {                        
                        UserGen.getListPageMenuDiv(session, elem, retval, innerMenu.m_ofSameLevel, doFirstURLOnly, doPopup);
                        if (doFirstURLOnly && len != retval.length()) {
                        	return;
                        }
                     }
                 }
                 else {
                     String name = innerMenu.m_label;
                     String url = removeDynParams(innerMenu.m_page);
                     //if (!user.isPrivAvailable(session, innerMenu.m_menuTag)) //moved up
                     //   continue;
                     if (doFirstURLOnly) {
                    	 retval.append(url);
                    	 if (url.indexOf("page_context") == -1) {
	                    	 if (url.indexOf('?') == -1)
	                    		 retval.append("?");
	                    	 else
	                    		 retval.append("&");
	                    	 retval.append("page_context=").append(innerMenu.m_targetTag);
                    	 }
                    	 return;
                     }
                     else {
                    	 retval.append("<DIV onmouseover='highlightMenu(this)' onMouseOut='unhighlightMenu(this)' STYLE='font-family:verdana; font-size:70%; color:#000066; font-weight:normal; height:20px; background:#DBDBE2; border:1px solid #000066; padding-left:10px;  cursor:hand; filter:; padding-right:3px; padding-top:2px; padding-bottom:2px'>")
                           .append("<SPAN ONCLICK=\"").append(innerMenu.m_javascriptOptional != null ? innerMenu.m_javascriptOptional+";" : "").append("gotoPageLink('")
                           
                           .append(url);
                    	 if (url.indexOf("page_context") == -1) {
	                    	 if (url.indexOf('?') == -1)
	                    		 retval.append("?");
	                    	 else
	                    		 retval.append("&");
	                    	 retval.append("page_context=").append(innerMenu.m_targetTag);
                    	 }
                         retval.append("',").append(doPopup)
                         .append(",").append(innerMenu.m_suspendTimer)
                           .append(")\">")
                           .append(name)
                           .append("</SPAN></DIV>");
                     }
                 }                 
              }
              if (!doFirstURLOnly) {
	              retval.append("</div>");
	              retval.append("</DIV>");
              }
          }
          catch (Exception e) {
              e.printStackTrace();
              throw e;
          }
      }

      //out.println("<THEAD><TR>");
      public static PreparedStatement getQueryWithParamSet(FrontPageInfo frontPage, SessionManager _session, String pgContext, MiscInner.ContextInfo contextInfo, boolean doingInterleaved, String addnlAndClause, ArrayList paramForAddnlAndClause, ArrayList searchCriteria, FmtI.Date dtfmt) throws Exception {
        
         try {
            java.sql.PreparedStatement pStmt = null;
            Connection _dbConnection = _session.getConnection();
            Logger _log = _session.getLogger();
            User _user = _session.getUser();
            Cache _cache = _session.getCache();
            boolean doTiming = _log.getLoggingLevel() >= 15;
            StringBuilder timeMsg = doTiming ? new StringBuilder() : null;
            
            
            ArrayList paramsFromFilter = new ArrayList();
            
            String projStatusSel = "1,2,3,4,7";
            //$TRACK
//            if (_user.isPrivAvailable(_session,com.ipssi.gen.utils.Misc.PROJ_TEMPLATE_PRIV_ID))
//               projStatusSel += ","+Integer.toString(com.ipssi.gen.utils.Misc.PROJ_STATUS_TEMPLATE);
            String order_by = _session.getParameter("orderby") != null ? _session.getParameter("orderby") : Integer.toString(com.ipssi.gen.utils.Misc.getUndefInt());
            StringBuilder addnlAndClauseBuf = null;
            if (addnlAndClause != null) {
               addnlAndClauseBuf = new StringBuilder();
               addnlAndClauseBuf.append(addnlAndClause);
            }
            for (int t1=0,t1s= paramForAddnlAndClause == null ? 0 : paramForAddnlAndClause.size();t1<t1s;t1++)
                paramsFromFilter.add(paramForAddnlAndClause.get(t1));

            ArrayList paramForPort = new ArrayList();//TO_PORT_FORWARD
            //want to pass searchcriteria instead of other frontPage.m_frontSearchCriteria
            //String queryString = com.ipssi.gen.utils.PrjTemplateHelper.getFrontSelQuery(projStatusSel, order_by, frontPage.m_frontSearchCriteria, _session,  frontPage, _cache, _dbConnection, pgContext, addnlAndClauseBuf, paramsFromFilter, false, doingInterleaved, paramForPort, dtfmt); //041508
//            String queryString = com.ipssi.gen.utils.PrjTemplateHelper.getFrontSelQuery(projStatusSel, order_by, searchCriteria, _session,  frontPage, _cache, _dbConnection, pgContext, addnlAndClauseBuf, paramsFromFilter, false, doingInterleaved, paramForPort, dtfmt); //041508
           // $Track
            String queryString = null;
            _log.log(queryString,15);

            pStmt = _dbConnection.prepareStatement(queryString);
            //for priveleges
            int psIndex = 1;
            //populate any date param
            int askedYear = com.ipssi.gen.utils.Misc.getParamAsInt(_session.getParameter("ask_year"));
            int budTimeId = com.ipssi.gen.utils.Misc.getBudgetYearTimeId(_dbConnection);
            int currTimeId = com.ipssi.gen.utils.Misc.getCurrentYearTimeId();
            java.sql.Date currDate = com.ipssi.gen.utils.Misc.getCurrentDate();
            int undefInt = com.ipssi.gen.utils.Misc.getUndefInt();
         // $TRACK
//            psIndex = com.ipssi.gen.utils.Misc.putParams(frontPage.m_queryTimeReq.m_paramRequiredSel, pStmt, psIndex, undefInt, undefInt, undefInt, currTimeId, budTimeId, currDate,1, contextInfo.m_currencyInfo.m_id, contextInfo.m_unitInfo.m_id, _dbConnection, _cache, timeMsg);
            if (paramForPort != null && paramForPort.size() > 0) {
                pStmt.setInt(psIndex++, ((Integer)paramForPort.get(0)).intValue());
                if (timeMsg != null)
                   timeMsg.append(((Integer)paramForPort.get(0)).intValue());
            }
            // $TRACK
//            psIndex = com.ipssi.gen.utils.Misc.putParams(frontPage.m_queryTimeReq.m_paramRequired, pStmt, psIndex, undefInt, undefInt, undefInt, currTimeId, budTimeId, currDate,1, contextInfo.m_currencyInfo.m_id, contextInfo.m_unitInfo.m_id, _dbConnection, _cache, timeMsg);

//COPY_AND_PASTE TO LoadMultiAttrib ... for parameters
            long uid = _user.getUserId();
            if (!frontPage.m_doingSupplier) {
               for (int t2=0,t2s= frontPage.m_queryTimeReq.m_numUserIdReqForPriv; t2<t2s;t2++)
                    pStmt.setLong(psIndex++, uid);            
            }
            if (timeMsg != null)
               timeMsg.append(",").append(uid).append(",").append(uid).append(",").append(uid);
            for (int t22=0,t22s = paramsFromFilter == null ? 0 : paramsFromFilter.size();t22<t22s;t22++) {
                pStmt.setInt(psIndex++, ((Integer)paramsFromFilter.get(t22)).intValue());
                if (timeMsg != null)
                   timeMsg.append(",").append(((Integer)paramsFromFilter.get(t22)).intValue());
            }
            if (frontPage.m_isForApprovalSpecial == 1) {
                pStmt.setLong(psIndex++, uid);
                pStmt.setLong(psIndex++, uid);
                if (timeMsg != null)
                   timeMsg.append(",").append(uid).append(",").append(uid);
            }
            if (doTiming)
               _log.log(timeMsg,15);
            return pStmt;
         }
         catch (Exception e) {
            e.printStackTrace();
            throw e;
         }
      }

     //042508 ... start
      public static ArrayList getTargetPrivIfRowLevelCheck(FrontPageInfo frontPage, MiscInner.ApplyFilterResult filterResult, SessionManager session, User user) throws Exception {//042508         
          ArrayList targetPriv = null;
          if (user.isSuperUser() || filterResult == null)
             return null;
          boolean toCheck = true;
          targetPriv = new ArrayList();
          ArrayList orgIdToCheck = filterResult.m_v123;
          boolean toCheckAt1 = orgIdToCheck == null || orgIdToCheck.size() == 0;
          Cache cache = session.getCache();
          for (int i1=0,i1s = frontPage.m_privTagToCheck == null ? 0 : frontPage.m_privTagToCheck.length; i1<i1s;i1++) {          
              PrivInfo.TagInfo privInfo = cache.getPrivId(frontPage.m_privTagToCheck[i1]);
              if (privInfo != null) {
                  int priv = privInfo.m_read;
                  if (priv <= 0)
                     priv = privInfo.m_write;
                  if (priv <= 0)
                     continue;
                  if (toCheckAt1) {
                      if (user.isPrivAvailable(session, priv, Misc.getUndefInt(), Misc.getUndefInt(), 1, false,Misc.getUndefInt(),Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), false, null))
                         return null;
                  }
                  else {
                      for (int i=0,is=orgIdToCheck == null ? 0 : orgIdToCheck.size();i<is;i++) {
                          if (user.isPrivAvailable(session, priv, Misc.getUndefInt(), Misc.getUndefInt(), ((Integer)orgIdToCheck.get(i)).intValue(), false, Misc.getUndefInt(),Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), false, null))
                             return null;   
                      }
                  }
                  targetPriv.add(new Integer(priv));                
              }   
          }
          if (targetPriv.size() == 0) {
              PrivInfo.TagInfo privInfo = cache.getPrivId("prj_basic");
              if (privInfo != null) {
                int priv = privInfo.m_read;
                if (priv <= 0)
                   priv = privInfo.m_write;
                if (toCheckAt1) {
                      if (user.isPrivAvailable(session, priv, Misc.getUndefInt(), Misc.getUndefInt(), 1, false,Misc.getUndefInt(),Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), false, null))
                         return null;
                  }
                  else {
                      for (int i=0,is=orgIdToCheck == null ? 0 : orgIdToCheck.size();i<is;i++) {
                          if (user.isPrivAvailable(session, priv, Misc.getUndefInt(), Misc.getUndefInt(), ((Integer)orgIdToCheck.get(i)).intValue(), false,Misc.getUndefInt(),Misc.getUndefInt(), Misc.getUndefInt(), Misc.getUndefInt(), false, null))
                             return null;   
                      }
                  }
                  targetPriv.add(new Integer(priv));                
              }
          }
          return targetPriv;
      }
      
      public static MiscInner.FrontPagePerRowPrivCheckColIndex getFrontPerRowPrivCheckIndex(FrontPageInfo frontPage, int objType) { //0425
          MiscInner.Pair prjInfo = null; //1st is obj id, 2nd is port for the obj
          MiscInner.Pair objInfo = null; //1st is obj id, 2nd is port for the obj
          ArrayList topLevelObjInfo = frontPage.m_topLevelObjIdPortIdIndices;
          
          
          
          if (objInfo == null && topLevelObjInfo != null && topLevelObjInfo.size() > objType) {
             objInfo = (MiscInner.Pair) topLevelObjInfo.get(objType);         
          }
        //$TRACK
//          if (prjInfo == null && topLevelObjInfo != null && topLevelObjInfo.size() > PurchaseSearch.G_FOR_PROJECT) {
//             prjInfo = (MiscInner.Pair) topLevelObjInfo.get(PurchaseSearch.G_FOR_PROJECT);         
//          }
          int objRow = 0;
          int objCol = -1;
          int objOrgRow = 0;
          int objOrgCol = -1;
          int prjRow = 0;
          int prjCol = -1;
          int prjOrgRow = 0;
          int prjOrgCol = -1;          
          
          if (objInfo != null || prjInfo != null) {          
              if (objInfo != null) {
                objCol = objInfo.first;
                objOrgCol = objInfo.second;
              }
              if (prjInfo != null) {
                prjCol = prjInfo.first;
                prjOrgCol = prjInfo.second;
              }             
          }
        //$TRACK
//          if (objType == PurchaseSearch.G_FOR_PROJECT) {
//              prjRow = objRow;
//              prjCol = objCol;
//          }
          return new MiscInner.FrontPagePerRowPrivCheckColIndex(prjRow, prjCol, prjOrgRow, prjOrgCol, objRow, objCol, objOrgRow, objOrgCol);
      }
      
      public static boolean isRowLevelVisible(FrontPageInfo frontPage, SessionManager session, User user, Cache cache, ResultSet rset, Connection dbConn, ArrayList targetPriv, FrontGetValHelper valGetter, int objType, MiscInner.FrontPagePerRowPrivCheckColIndex frontPagePerRowPrivCheckColIndex) throws Exception {//042508          
          if (rset == null || frontPagePerRowPrivCheckColIndex == null)
             return true;
          int projectId = Misc.getUndefInt();
          int orgIdOfPrj = Misc.getUndefInt();
          int objectId = Misc.getUndefInt();
          int orgIdOfObj = Misc.getUndefInt();
          
          int objRow = frontPagePerRowPrivCheckColIndex.m_objRow;
          int objCol = frontPagePerRowPrivCheckColIndex.m_objCol;
          int objOrgRow = frontPagePerRowPrivCheckColIndex.m_objOrgRow;
          int objOrgCol = frontPagePerRowPrivCheckColIndex.m_objOrgCol;
          int prjRow = frontPagePerRowPrivCheckColIndex.m_prjRow;
          int prjCol = frontPagePerRowPrivCheckColIndex.m_prjCol;
          int prjOrgRow = frontPagePerRowPrivCheckColIndex.m_prjOrgRow;
          int prjOrgCol = frontPagePerRowPrivCheckColIndex.m_prjOrgCol;
          
          if (rset != null) {
             if (objRow >= 0 && objCol >= 0) {
                 objectId = valGetter.getValInt(dbConn, rset, objRow, objCol, cache);             
             }
             if (objOrgRow >= 0 && objOrgCol >= 0) {
                 orgIdOfObj = valGetter.getValInt(dbConn, rset, objOrgRow, objOrgCol, cache);
             }
             if (prjRow >= 0 && prjCol >= 0) {
                 projectId = valGetter.getValInt(dbConn, rset, prjRow, prjCol, cache);             
             }
             if (prjOrgRow >= 0 && prjOrgCol >= 0) {
                 orgIdOfPrj = valGetter.getValInt(dbConn, rset, prjOrgRow, prjOrgCol, cache);
             }
          }               
          if (projectId == 69804 || objectId == 50004 || objectId == 50005) {
             int dbg = 1;
          }
          for (int i1=0,i1s = targetPriv == null ? 0 : targetPriv.size(); i1<i1s;i1++) {          
              int privId = ((Integer)targetPriv.get(i1)).intValue();              
              //isPrivAvailable(SessionManager session, int privId, int projectId, int workspaceId, int portfolioId, boolean checkFromMenuPointOfView, int projectPortfolioId, int objType, int objId, int objPortfolioId, boolean checkExplicitGrant)
              if (user.isPrivAvailable(session, privId, projectId, Misc.getUndefInt(), orgIdOfPrj, false, orgIdOfPrj, objType, objectId, orgIdOfObj, false, null))
                 return true;              
          }
          return false;          
      }
      //042508
      
      public static void printMainTable(FrontPageInfo frontPage, JspWriter out, SessionManager _session, boolean doingMultiSelect, MiscInner.ContextInfo contextInfo, String pgContext, StringBuilder origPrjList, StringBuilder origInnerPrjList, FrontPageInfo interleavedPageInfo, String addnlAndClause, ArrayList paramForAddnlAndClause, MiscInner.ContextInfo currencyEtcContext) throws Exception {         
         try {     
             Cache _cache = _session.getCache();
             Logger log = _session.getLogger();
             Connection dbConn = _session.getConnection();
      //       FrontPageInfo interleavedPageInfo = PrjTemplateHelper.getFrontPageInfo(frontPage.m_interleavedTemplateId);
             out.println("<table ID='DATA_TABLE' width='100%' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'>");//<thead><tr>");
             int colCount = frontPage.m_frontInfoList.size();
             int adjColCount = frontPage.m_valGetter.getRsetColCount();
              //ArrayList formatters = new ArrayList();
             com.ipssi.gen.utils.FmtI.Date dtfmt = contextInfo.getDateFormatter();
             com.ipssi.gen.utils.FmtI.Number numberfmt = contextInfo.getNumberFormatter();
             FmtI.Number unscaledFmt = contextInfo.getUnscaledFormatter();

             

             PreparedStatement frontStmt = getQueryWithParamSet(frontPage, _session, pgContext, contextInfo, false, addnlAndClause, paramForAddnlAndClause, frontPage.m_frontSearchCriteria, dtfmt);
             ArrayList mainMultiAttrib = frontPage.loadMultiAttribsForList(dbConn, _session, dtfmt, numberfmt, unscaledFmt, pgContext, false, addnlAndClause, paramForAddnlAndClause, frontPage.m_frontSearchCriteria, currencyEtcContext);
             
             //042508 ... begin
             MiscInner.ApplyFilterResult filterResult = (MiscInner.ApplyFilterResult) _session.getAttributeObj("__filter");
             User user = _session.getUser();
             ArrayList topLevelPrivList = getTargetPrivIfRowLevelCheck(frontPage, filterResult, _session, user);
           //$TRACK
             int topObjType = 0;
             boolean doTopLevelPerRowCheck = false;
//             int topObjType = frontPage.m_queryTimeReq.m_hasSuppliersTable ? PurchaseSearch.G_FOR_SUPPLIER : frontPage.m_queryTimeReq.m_hasOrdersTable ? PurchaseSearch.G_FOR_ORDER : PurchaseSearch.G_FOR_PROJECT;
//             boolean doTopLevelPerRowCheck = topObjType != PurchaseSearch.G_FOR_SUPPLIER && topLevelPrivList  != null && topLevelPrivList.size() != 0;
             MiscInner.FrontPagePerRowPrivCheckColIndex topPrivCheckIndex = !doTopLevelPerRowCheck ? null : getFrontPerRowPrivCheckIndex(frontPage, topObjType);
             
             ArrayList interleavdPrivList = interleavedPageInfo == null ? null : getTargetPrivIfRowLevelCheck(interleavedPageInfo, filterResult, _session, user);
             int interleavedObjType = 0;
             boolean doInterleavedPerRowCheck = false;
//             int interleavedObjType = interleavedPageInfo == null ? PurchaseSearch.G_FOR_PROJECT : interleavedPageInfo.m_queryTimeReq.m_hasSuppliersTable ? PurchaseSearch.G_FOR_SUPPLIER : interleavedPageInfo.m_queryTimeReq.m_hasOrdersTable ? PurchaseSearch.G_FOR_ORDER :  PurchaseSearch.G_FOR_PROJECT;
//             boolean doInterleavedPerRowCheck = interleavedObjType != PurchaseSearch.G_FOR_SUPPLIER && interleavdPrivList  != null && interleavdPrivList.size() != 0;
             MiscInner.FrontPagePerRowPrivCheckColIndex interleavedPrivCheckIndex = !doInterleavedPerRowCheck ? null : getFrontPerRowPrivCheckIndex(interleavedPageInfo, interleavedObjType);
             //... end 042508
             
             //frontStmt.setFetchSize(1000);
             boolean doTiming = log.getLoggingLevel() >= 15;
             StringBuilder timeMsg = doTiming ? new StringBuilder() : null;
             long currTimeMilli = 0;
             
             if (doTiming) {
                currTimeMilli = System.currentTimeMillis();
                timeMsg.append("[PROJECT],").append("In,").append(currTimeMilli);
             }
             
             ResultSet frontRset = frontStmt.executeQuery();
      //       frontRset.setFetchSize(1000); //check if this has any effect
             if (doTiming) {
                long oldTime = currTimeMilli;
                currTimeMilli = System.currentTimeMillis();
                timeMsg.append(",").append("Out_Delta_Main,").append(currTimeMilli-oldTime);
             }
             
             PreparedStatement interleavedStmt = interleavedPageInfo == null ? null : getQueryWithParamSet(interleavedPageInfo, _session, pgContext, contextInfo, true, addnlAndClause, paramForAddnlAndClause, frontPage.m_frontSearchCriteria, dtfmt);
             ArrayList interleavedMultiAttrib = interleavedPageInfo == null ? null : interleavedPageInfo.loadMultiAttribsForList(dbConn, _session, dtfmt, numberfmt, unscaledFmt, pgContext, true, addnlAndClause, paramForAddnlAndClause, frontPage.m_frontSearchCriteria, currencyEtcContext);
             ResultSet interleavedRset = interleavedStmt == null ? null : interleavedStmt.executeQuery();
             if (doTiming) {
                long oldTime = currTimeMilli;
                currTimeMilli = System.currentTimeMillis();
                timeMsg.append(",").append("Out_Delta_Interleave,").append(currTimeMilli-oldTime);
             }
             
             
             
             out.println("<thead>");
             //out.println("<tr>"); done in printColHead
             printColHead(frontPage.m_frontInfoList, out, _cache, doingMultiSelect, true, null, false, frontPage.m_hasMultiple);
             //out.println("</tr>"); done in printColHead
             out.println("</thead>");
             out.println("<tbody>");
             String objectIdParam = frontPage.m_objectIdParamLabel;//"project_id";
             String objectIndexCol = frontPage.m_objectIdColName;
             int objectIdIndex = -1;
             if (objectIndexCol != null && objectIndexCol.length() > 0) {
                 objectIdIndex = frontPage.getColIndexByName(objectIndexCol);
             }
             if (objectIdIndex < 0)
                objectIdIndex = adjColCount+1;
             else
                objectIdIndex = frontPage.m_valGetter.getIndexInRset(0, objectIdIndex)+1;
                 
             
             int interleavedColCount = 0;
             int interleavedAdjColCount = 0;
             String interleavedObjectIdParam = null;             
             int interleavedObjectIdIndex = 0;
             
             if (interleavedPageInfo != null) {
                 interleavedColCount = interleavedPageInfo.m_frontInfoList.size();
                 interleavedAdjColCount = interleavedPageInfo.m_valGetter.getRsetColCount();
                 interleavedObjectIdParam = interleavedPageInfo.m_objectIdParamLabel;
                 String interleavedObjectIndexCol = interleavedPageInfo.m_objectIdColName;
                 if (interleavedObjectIndexCol == null) {
                    interleavedObjectIdIndex = interleavedAdjColCount+1;
                 }
                 else {
                    interleavedObjectIdIndex = interleavedPageInfo.getColIndexByName(interleavedObjectIndexCol);
                    if (interleavedObjectIdIndex < 0)
                        interleavedObjectIdIndex = interleavedAdjColCount+1;
                    else
                        interleavedObjectIdIndex = interleavedPageInfo.m_valGetter.getIndexInRset(0, interleavedObjectIdIndex)+1;
                 }
             }
             int[] colTimeList = null;
             int[] colTimeList2 = null;
             if (doTiming) {
                colTimeList = new int[colCount+1];
                for (int i=0,is=colTimeList.length;i<is;i++)
                   colTimeList[i] = 0;
                if (interleavedColCount > 0) {
                   colTimeList2 = new int[interleavedColCount+1];
                   for (int i=0,is=colTimeList2.length;i<is;i++)
                      colTimeList2[i] = 0;
                }
             }
             boolean interleavedRsetUseful = interleavedRset != null ? interleavedRset.next() : false;
             long rowFetchTime = 0;
             long rowProcessTime = 0;
             long rowPrintSingleRowTime = 0;
             long currRowTime = doTiming ? System.currentTimeMillis() : 0;

			 // Variable to track if any rows are returned
			 boolean dataPresent = false;
             while (frontRset.next()) {
				 dataPresent = true;
                 if (doTiming) {
                    long oldTime = currRowTime;
                    currRowTime = System.currentTimeMillis();
                    rowFetchTime += (currRowTime - oldTime);
                 }
                 if (doTopLevelPerRowCheck) {//042508
                    if (!isRowLevelVisible(frontPage, _session, user, _cache, frontRset, dbConn, topLevelPrivList, frontPage.m_valGetter, topObjType, topPrivCheckIndex)) {
                       continue;
                    }
                 }//end
             
                 out.println("<tr>");
                 frontPage.fpPrintSingleRow(out, _cache, _session, frontRset, colCount, adjColCount, dtfmt, numberfmt, unscaledFmt, origPrjList, doingMultiSelect, objectIdParam, objectIdIndex, false, colTimeList, mainMultiAttrib);
                 if (doTiming) {
                    long oldTime = currRowTime;
                    currRowTime = System.currentTimeMillis();
                    rowPrintSingleRowTime += (currRowTime - oldTime);
                 }
                 out.println("</tr>");
                 if (interleavedRset != null) {
                    int refObjId = frontRset.getInt(adjColCount+1);
                    boolean printedHeader = false;
                    while (interleavedRsetUseful) {
                       //int interleavedObjId = interleavedRset.getInt(interleavedObjectIdIndex);
                       int interleavedParentObjId = interleavedRset.getInt(interleavedAdjColCount+1);
                       if (interleavedParentObjId > refObjId) { //orders for found projects that should not otherwise have been found ... need to understand why
                           interleavedRsetUseful = interleavedRset.next();
                           continue;                          
                       }
                       if (interleavedParentObjId != refObjId)
                          break;
                       if (doInterleavedPerRowCheck) {//042508
                          if (!isRowLevelVisible(interleavedPageInfo, _session, user, _cache, interleavedRset, dbConn, interleavdPrivList, interleavedPageInfo.m_valGetter, interleavedObjType,  interleavedPrivCheckIndex)) {
                             interleavedRsetUseful = interleavedRset.next();
                             continue;
                          }
                       }//end
                       if (!printedHeader) {
                           out.println("<tr>");
                           out.println("<td class='cn'>");
                           out.println(frontPage.m_interleavedInstructText);
                           out.println("<td class='cn' colspan='"+Integer.toString(colCount-1)+"'>");
                           out.println("<table ID='INNER_DATA_TABLE' width='100%' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'>");
                           out.println("<thead>");
//                           out.println("<tr>");
                           printColHead(interleavedPageInfo.m_frontInfoList, out, _cache, doingMultiSelect, true, "tmSelectedSubfolder", true, interleavedPageInfo.m_hasMultiple);
//                           out.println("</tr>");              
                           out.println("</thead>");
                           out.println("<tbody>");
                           printedHeader = true;
                       }
                       out.println("<tr>");
                       interleavedPageInfo.fpPrintSingleRow(out, _cache, _session, interleavedRset, interleavedColCount, interleavedAdjColCount, dtfmt, numberfmt, unscaledFmt, origInnerPrjList, doingMultiSelect, interleavedObjectIdParam, interleavedObjectIdIndex, true, colTimeList2, interleavedMultiAttrib);
                       out.println("</tr>");
                       interleavedRsetUseful = interleavedRset.next();
                    }
                    if (printedHeader) {
                       out.println("</tbody></table>");
                       out.println("</td></tr>");
                    }
                 }
                 if (doTiming) {
                    long oldTime = currRowTime;
                    currRowTime = System.currentTimeMillis();
                    rowProcessTime += (currRowTime - oldTime);
                 }
             }
             frontRset.close();
             frontStmt.close();
             if (interleavedPageInfo != null) {
                 interleavedRset.close();
                 interleavedStmt.close();
             }

			 // Print an empty row if no data present
			 if (!dataPresent) {
				 out.println("<tr bordercolor=\"#FFFFFF\"> <td class=\"cn\" colspan=\"100\">No data available</td></tr>");
			 }
             out.println("</tbody>");
             out.println("</table>");             
             if (doTiming) {
                long oldTime = currTimeMilli;
                currTimeMilli = System.currentTimeMillis();
                timeMsg.append(",Print_Delta,").append(currTimeMilli-oldTime);
                timeMsg.append(",FetchTime,").append(rowFetchTime).append(",rowPrintSingleRowTime").append(rowPrintSingleRowTime).append(",otherRowProcess").append(rowProcessTime);
                log.log(timeMsg, 15);
//                System.out.println(timeMsg);
                
                if (colTimeList != null) {
                   timeMsg.setLength(0);
                   timeMsg.append("Col Times");
                   for (int i=0,is=colTimeList.length;i<is;i++)
                      timeMsg.append(",").append(colTimeList[i]);
                   log.log(timeMsg,15);                      
//                   System.out.println(timeMsg);
                }
                if (colTimeList2 != null) {
                   timeMsg.setLength(0);
                   timeMsg.append("Interleaved Col Times");
                   for (int i=0,is=colTimeList2.length;i<is;i++)
                      timeMsg.append(",").append(colTimeList2[i]);
                   log.log(timeMsg,15);        
//                   System.out.println(timeMsg);
                }
                
             }
         }
         
         catch (Exception e) {
             e.printStackTrace();
             throw e;
         }         
      }
       
      public static void printColHead(ArrayList dimConfigList, JspWriter out, Cache _cache, boolean doingMultiSelect, boolean doSortLink, String headerClassName, boolean doingInner, boolean hasMultiple) throws Exception {         
         //int colCount = frontPage.m_frontInfoList.size();
         int colCount = dimConfigList.size();
         
         if (headerClassName == null) {
            if (hasMultiple)
               headerClassName = "tshb";
            else
               headerClassName = "tshc";
         }
         String selectCheckBoxVarName = doingInner ? "inner_project_list" : "project_list";                  
         out.println("<tr  id='scrollmenu'>");         
         //print the single elem row first .. then if necessary print the second row
         boolean secondRowNeeded = false;
         String topRowSpan = hasMultiple ? "rowspan='2'" : "";
         for (int i=0;i<colCount; i++) {
             com.ipssi.gen.utils.DimConfigInfo dimConf = (com.ipssi.gen.utils.DimConfigInfo) dimConfigList.get(i);//frontPage.m_frontInfoList.get(i);
             if (dimConf.m_hidden)
                continue;
            
             DimCalc dimCalc = dimConf.m_dimCalc;
             com.ipssi.gen.utils.DimInfo dimInfo = dimCalc.m_dimInfo;
			 MiscInner.PairIntBool intBoolPair = getSubColCount(dimConf);
             int multiColCount = intBoolPair.first;
             boolean isMultiColCell = intBoolPair.second;
             String rowSpan = !isMultiColCell ? topRowSpan : "";
             if (isMultiColCell) 
                 secondRowNeeded = true;
                 
             int attribType = dimInfo != null ? dimInfo.getAttribType() : _cache.STRING_TYPE;
             boolean doDate = attribType == _cache.DATE_TYPE;
             boolean doNumber = attribType == _cache.NUMBER_TYPE;
                     
             int labelWidth = dimConf.m_labelWidth;
             String widthStr = labelWidth > 0 ? " width='"+labelWidth+"' " : "";
             String colSpan = isMultiColCell ? " colspan="+multiColCount : "";
             if (dimConf.m_isSelect) {
                out.println("<td "+rowSpan+" "+colSpan+" dt_type='"+ (doDate ? "date" : doNumber ? "num" : "text" )+"' width='20' class='"+headerClassName+"' >"+ (dimConf.m_name != null && dimConf.m_name.length() != 0 ? dimConf.m_name+"<br>" : ""));
                if (doingMultiSelect) {
                   out.println("<input class='tn' type='checkbox' name='select_"+selectCheckBoxVarName+"' value='1' onclick='setSelectAll(event.srcElement)'/>");
                }
                out.println("</td>");			 
                continue;
             }
             
             String displayText = dimConf.m_hidden ? "style='display:none'" : "";            
             out.println("<td  "+rowSpan+" "+colSpan+" "+ widthStr +" dt_type='"+( doDate ? "date" : doNumber ? "num" : "text" )+"' "+displayText+" class='"+headerClassName+"' >");
             if (dimConf != null && dimConf.m_name != null && dimConf.m_name.length() != 0) {
                if (doSortLink && !isMultiColCell)
                    out.println("<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>");
                out.println(dimConf.m_name);
                if (doSortLink && !isMultiColCell)
                    out.println("</a>");
            }
            else {
               out.println("&nbsp;");
            }
            out.println("</td>");
         }//end of for for each dimConfig
         out.println("</tr>");
         
         if (secondRowNeeded) {
             out.println("<tr>");
             
             for (int i=0;i<colCount; i++) {
                 com.ipssi.gen.utils.DimConfigInfo dimConf = (com.ipssi.gen.utils.DimConfigInfo) dimConfigList.get(i);//frontPage.m_frontInfoList.get(i);
                 MiscInner.PairIntBool intBoolPair = getSubColCount(dimConf);
				 int multiColCount = intBoolPair.first;
                 boolean isMultiColCell = intBoolPair.second;                 
                 if (!isMultiColCell)
                    continue;
                
                 DimCalc dimCalc = dimConf.m_dimCalc;
                 com.ipssi.gen.utils.DimInfo dimInfo = dimCalc.m_dimInfo;
                 if (dimCalc.m_groupBy != null && dimCalc.m_groupBy.size() > 0) {
                    DimInfo.DimValList dv = (DimInfo.DimValList) dimCalc.m_groupBy.get(0);
                    ArrayList dvlist = dv.getValList();
                    DimInfo groupDim = dv.m_dimInfo;
                    ArrayList valList = dv.getValListOfValInfo(); //groupDim.m_valList;
                    if (valList == null)
                       valList = groupDim.getValList();
                    
                    
                    int attribType = dimInfo != null ? dimInfo.getAttribType() : _cache.STRING_TYPE;
                    boolean doDate = attribType == _cache.DATE_TYPE;
                    boolean doNumber = attribType == _cache.NUMBER_TYPE;
                         
                    int labelWidth = dimConf.m_labelWidth;
                    String widthStr = labelWidth > 0 ? " width='"+labelWidth+"' " : "";
                    for (int j=0,js=valList == null || valList.size() == 0 ? (dvlist == null ? 0 : dvlist.size()) : valList.size();j<js;j++) {                    
                        DimInfo.ValInfo valInfo = (DimInfo.ValInfo) valList.get(j);
                        String cellText = null;    
                        if (valInfo != null)
                           cellText = valInfo.m_name;
                        else {
                           int attribId = ((Integer)dvlist.get(j)).intValue();
//                           if (groupDim.m_subsetOf == 8070) { 
//                              MiscInner.CCBSInfo ccbs = _cache.getCCBSInfo(attribId, null);
//                              cellText = ccbs.m_name;
//                           }
//                           else {
//                              cellText = _cache.getAttribDisplayName(groupDim, attribId);
//                           }
                           cellText = _cache.getAttribDisplayName(groupDim, attribId);
                        }
                        if (dimConf.m_isSelect) {
                            out.println("<td dt_type='"+ (doDate ? "date" : doNumber ? "num" : "text" )+"' width='20' class='"+headerClassName+"' >"+ (cellText != null && cellText.length() != 0 ? cellText+"<br>" : ""));
                            if (doingMultiSelect) {
                              out.println("<input class='tn' type='checkbox' name='select_"+selectCheckBoxVarName+"' value='1' onclick='setSelectAll(event.srcElement)'/>");
                            }
                            out.println("</td>");			 
                            continue;
                        }
                     
                        String displayText = dimConf.m_hidden ? "style='display:none'" : "";            
                        out.println("<td  "+ widthStr +" dt_type='"+( doDate ? "date" : doNumber ? "num" : "text" )+"' "+displayText+" class='"+headerClassName+"' >");
                        if (cellText != null && cellText.length() != 0) {
                            if (doSortLink)
                                out.println("<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>");
                            out.println(cellText);
                            if (doSortLink)
                                out.println("</a>");
                        }
                        else {
                            out.println("&nbsp;");
                        }
                        out.println("</td>");
                    }//for each val in the group by criteria                    
                 }//if doing grouping
                 else { //doing because of nested table
                    ArrayList subColList = dimConf.m_multiRowSubColList;
                    int subcolSz = subColList == null ? 0 : subColList.size();
                    for (int j=0,js=subcolSz == 0 ? 1 : subcolSz;j<js;j++) {
                        DimConfigInfo subDimConf = subcolSz == 0 ? dimConf : (DimConfigInfo) subColList.get(j);
                        DimInfo subDimInfo = subDimConf.m_dimCalc == null ? null : subDimConf.m_dimCalc.m_dimInfo;
                        if (subDimInfo == null)
                           subDimInfo = subDimConf.m_ifClassifyIsTypeOfDim;
                        
                        int attribType = subDimInfo != null ? subDimInfo.getAttribType() : "created_on".equals(subDimConf.m_columnName) ? _cache.DATE_TYPE : _cache.STRING_TYPE;
                        boolean doDate = attribType == _cache.DATE_TYPE;
                        boolean doNumber = attribType == _cache.NUMBER_TYPE;
                             
                        int labelWidth = subDimConf.m_labelWidth;
                        String widthStr = labelWidth > 0 ? " width='"+labelWidth+"' " : "";
                                              
                        String cellText = subDimConf.m_name;
                        if (dimConf.m_isSelect) {
                            out.println("<td dt_type='"+ (doDate ? "date" : doNumber ? "num" : "text" )+"' width='20' class='"+headerClassName+"' >"+ (cellText != null && cellText.length() != 0 ? cellText+"<br>" : ""));
                            if (doingMultiSelect) {
                              out.println("<input class='tn' type='checkbox' name='select_"+selectCheckBoxVarName+"' value='1' onclick='setSelectAll(event.srcElement)'/>");
                            }
                            out.println("</td>");			 
                            continue;
                        }
                     
                        String displayText = subDimConf.m_hidden ? "style='display:none'" : "";            
                        out.println("<td  "+ widthStr +" dt_type='"+( doDate ? "date" : doNumber ? "num" : "text" )+"' "+displayText+" class='"+headerClassName+"' >");
                        if (cellText != null && cellText.length() != 0) {
                            if (doSortLink)
                                out.println("<a class='tsl' href='#' onclick='sortTable(event.srcElement)'>");
                            out.println(cellText);
                            if (doSortLink)
                                out.println("</a>");
                        }
                        else {
                            out.println("&nbsp;");
                        }
                        out.println("</td>");

                    }//for each inner col
                 }// else if doing nested table
             }//end of for for each dimConfig
             out.println("</tr>");
         }//articificial for inner cols
      }
      
      public StringBuilder getFPLink(int prjId, String templateId, String objectIdParam) {
         FrontPageInfo frontPage = this; //code moved from somewhere else
         StringBuilder linkStr = new StringBuilder();
         if (frontPage.m_topMenu == null) {
             linkStr.append("<a href=\"").append(frontPage.m_targetPage).append("?").append(objectIdParam).append("=")
             .append(prjId).append("&template_id=").append(templateId)
             .append("&page_context=").append(frontPage.m_targetContext).append("\">");
         }
         else {
             linkStr.append("<a href=\"").append(Misc.G_APP_1_BASE).append("project_detail.jsp?").append(objectIdParam).append("=")
             .append(prjId).append("&template_id=").append(templateId)
             .append("&_top_menu=").append(frontPage.m_topMenu);
             if (frontPage.m_page_target_detail != null)
                 linkStr.append("&_page_detail=").append(frontPage.m_page_target_detail);
             linkStr.append("&page_context=").append(frontPage.m_createPrjContext).append("\">");
         }
         return linkStr;
      }
      
      public MiscInner.Pair getCheckBoxLinkIndex() {//checks if there is a checkBox and LinkStr - 
         int foundCount = 0;
         MiscInner.Pair pair = new MiscInner.Pair(-1,-1);
         for (int i=0,is=m_frontInfoList.size();i<is;i++) {
             DimConfigInfo dimConf = (DimConfigInfo) m_frontInfoList.get(i);
             if ("link".equals(dimConf.m_disp)) {
                foundCount++;
                pair.first = i;
             }
             else if (dimConf.m_isSelect) {
                foundCount++;
                pair.second = i;
             }
             if (foundCount == 2)
                break;
         }
         return pair;
      }
      
      public void fpPrintSingleRow(JspWriter out, Cache _cache, SessionManager _session, ResultSet rset, int colCount, int adjColCount, FmtI.Date dtfmt, FmtI.Number numberfmt, FmtI.Number unscaledFmt,  StringBuilder origPrjList, boolean doingMultiSelect, String objectIdParam, int objectIdIndex, boolean doingInner, int[] colTimeList, ArrayList multiAttribData) throws Exception {
          //fp for frontPage 
          Connection _dbConnection = _session.getConnection();
          boolean matchesCheckBoxCriteria = (m_checkBoxLinkPos.second >= 0) ? matchesCheckBoxCrit(_dbConnection, rset, _cache) : true;
         
          long iPrjId = Misc.getUndefInt();
          if (Misc.isUndef(objectIdIndex))
              objectIdIndex = adjColCount+1;
          int prjId = rset.getInt(objectIdIndex); 
          if (objectIdParam == null)
               objectIdParam = "project_id";
          
          int templateId = rset.getInt(adjColCount+7);
          String templateIdAsStr = Integer.toString(templateId);
          StringBuilder linkStr = getFPLink(prjId, m_templateId == null || !m_createProject ? templateIdAsStr : m_templateId, objectIdParam);//getFPLink(prjId, templateId, objectIdParam);
          printSingleRow(m_frontInfoList, m_valGetter, out, _cache, _session, rset, colCount, adjColCount, dtfmt, numberfmt, unscaledFmt,  origPrjList, doingMultiSelect, objectIdParam, objectIdIndex, doingInner, colTimeList, matchesCheckBoxCriteria, linkStr, multiAttribData,null, m_accessCheckControlInfo);
      }
      
      public static void printSingleRow(ArrayList dimConfigList, FrontGetValHelper valGetter, JspWriter out, Cache _cache, SessionManager _session, ResultSet rset, int colCount, int adjColCount, FmtI.Date dtfmt, FmtI.Number numberfmt, FmtI.Number unscaledFmt,  StringBuilder origPrjList, boolean doingMultiSelect, String objectIdParam, int objectIdIndex, boolean doingInner, int[] colTimeList
      , boolean matchesCheckBoxCrit, StringBuilder linkStr, ArrayList multiAttribData, String targetForLink, HashMap accessCheckControlInfo) throws Exception {
        
        //adjColCount = the 'actual' index in rset afterwhich you get the id prjId or the objectId index if not provided will 1+adjColCount
         try {            
            //out.println("<tr >");
            StringBuilder tempBuf = new StringBuilder();
            long currTimeMillis = colTimeList == null ? 0 : System.currentTimeMillis();
            Connection _dbConnection = _session.getConnection();
            long iPrjId = Misc.getUndefInt();
            if (Misc.isUndef(objectIdIndex))
               objectIdIndex = adjColCount+1;
            iPrjId = rset.getLong(objectIdIndex); 
            if (objectIdParam == null)
               objectIdParam = "project_id";
            String prjId  = Long.toString(iPrjId);
     //       long templateId = rset.getLong(adjColCount+7);
            long templateId = Misc.getUndefInt();//rset.getLong(adjColCount+7);
            String selectCheckBoxVarName = doingInner ? "inner_project_list" : "project_list";
			      String tdClass = doingInner ? "Ce_sf" : "Ce";
            
            if (colTimeList != null) {
                long oldTime = currTimeMillis;                        
                currTimeMillis = System.currentTimeMillis();
                colTimeList[0] += (int) (currTimeMillis-oldTime);                        
            }
            int linkMenuIndex = 0;
        		for (int i=0;i<colCount; i++) {                 		        	   
                 com.ipssi.gen.utils.DimConfigInfo dimConf = (com.ipssi.gen.utils.DimConfigInfo) dimConfigList.get(i);
                 if (dimConf.m_hidden)
                    continue;
                 DimCalc dimCalc = dimConf.m_dimCalc;
                 com.ipssi.gen.utils.DimInfo dimInfo = dimCalc.m_dimInfo;
                 HashMap allMultiAttribVals = multiAttribData == null ? null : (HashMap) multiAttribData.get(i);
                 boolean isMultiValCol = allMultiAttribVals != null;// && !allMultiAttribVals.isEmpty();
                 if (isMultiValCol) {                    
                    if (dimCalc.m_groupBy != null && dimCalc.m_groupBy.size() > 0) {
                        boolean isAccessible = true;
                        if (dimInfo != null && dimInfo.m_accCheck != null && accessCheckControlInfo != null)
                            isAccessible = helperHasValAccess(dimInfo, accessCheckControlInfo, _session.getUser(), i, 0, valGetter, null, _cache,rset, _dbConnection, _session);
                        DimInfo.DimValList dv = (DimInfo.DimValList) dimCalc.m_groupBy.get(0);
                        DimInfo groupDim = dv.m_dimInfo;
                        ArrayList dvlist = dv.getValList();
                        ArrayList valList = dv.getValListOfValInfo();
                        if (valList == null || valList.size() == 0)
                           valList = groupDim.getValList();
                        
                        ArrayList attribVals = (ArrayList) allMultiAttribVals.get(new Integer((int)iPrjId));//this is arraylist of arraylist of string
                        int attribType = dimInfo != null ? dimInfo.getAttribType() : _cache.STRING_TYPE;                        
                        boolean doNumber = attribType == _cache.INTEGER_TYPE || attribType == _cache.LOV_NO_VAL_TYPE || attribType == _cache.NUMBER_TYPE;
                        String alignText = doNumber ? "  " : "";
                        for (int j=0,js=valList == null || valList.size() == 0 ? (dvlist == null ? 0 : dvlist.size()) : valList.size();j<js;j++) {                    
                            DimInfo.ValInfo vInfo = (DimInfo.ValInfo) valList.get(j);
                            int valId = vInfo != null ? vInfo.m_id : ((Integer)dvlist.get(j)).intValue();
                            String valIdStr = Integer.toString(valId);
                            ArrayList valSpecificRow = null;
                            for (int k=0,ks=attribVals == null ? 0 : attribVals.size();k<ks;k++) {
                               ArrayList row = (ArrayList) attribVals.get(k);
                               String groupVal = (String) row.get(1);
                               if (groupVal.equals(valIdStr)) {
                                  valSpecificRow = row;
                                  break;
                               }
                            }                            
                            String cellText = valSpecificRow == null ? null : (String) valSpecificRow.get(0);
                            if (!isAccessible)
                               cellText = Misc.NO_FIELD_ACCESS_MSG;
                            String displayText = dimConf.m_hidden ? "style='display:none'" : "";
                            out.println("<td "+alignText+displayText+" class='"+tdClass+"'>");
                            if (cellText == null) {
                               out.println("&nbsp;");
                            }
                            else {
                               out.println(cellText);
                            }
                            out.println("</td>");
                        }//for each val in the group by criteria                    
                     }//if doing grouping
                     else { //doing because of nested table
                        ArrayList subColList = dimConf.m_multiRowSubColList;
                        int subcolSz = subColList == null ? 0 : subColList.size();
						String nowrapLabel = subcolSz <= 1 ? "" : " nowrap ";
                        for (int j=0,js=subcolSz == 0 ? 1 : subcolSz;j<js;j++) {
                            DimConfigInfo subDimConf = (subcolSz == 0) ? dimConf : (DimConfigInfo) subColList.get(j);
                            DimInfo subDimInfo = subDimConf.m_dimCalc == null ? null : subDimConf.m_dimCalc.m_dimInfo;
                            if (subDimInfo == null)
                               subDimInfo = subDimConf.m_ifClassifyIsTypeOfDim;
                            int attribType = subDimInfo != null ? subDimInfo.getAttribType() : "created_on".equals(subDimConf.m_columnName) ? _cache.DATE_TYPE : _cache.STRING_TYPE;                            
                            boolean doNumber = attribType == _cache.NUMBER_TYPE;
                            String displayText = subDimConf.m_hidden ? "style='display:none'" : "";
                            String alignText = doNumber ? "  " : "";
                            boolean doLink = "link".equals(subDimConf.m_disp);                            
                            
                            int labelWidth = subDimConf.m_labelWidth;
                            String widthStr = labelWidth > 0 ? " width='"+labelWidth+"' " : "";                                                  
							out.println("<td " + nowrapLabel + alignText + displayText + " class='" + tdClass + "'>");
			                            
                            ArrayList attribVals = (ArrayList) allMultiAttribVals.get(new Integer((int)iPrjId));//this is arraylist of arraylist of string
                            StringBuilder cellPrint = new StringBuilder(); 
                            for (int k=0,ks=attribVals == null ? 0 : attribVals.size();k<ks;k++) {
                                if (cellPrint.length() != 0)
                                    cellPrint.append("<br>");
                                ArrayList row = (ArrayList) attribVals.get(k);
                                String cellVal = (String) row.get(j);
                                boolean isAccessible = true;
                                if (subDimInfo != null && subDimInfo.m_accCheck != null && accessCheckControlInfo != null)
                                    isAccessible = helperHasValAccess(subDimInfo, accessCheckControlInfo, _session.getUser(), i, j, valGetter, row, _cache,rset, _dbConnection, _session);
                                if (!isAccessible)
                                    cellVal = Misc.NO_FIELD_ACCESS_MSG;
                                if (doLink) {
                                    cellPrint.append("<a ").append(targetForLink != null ? targetForLink : "").append(" href='").append(subDimConf.m_targetPage).append("?").append(subDimConf.m_objectIdParam).append("=");
                                    String prevCellVal = j != 0 ? (String)row.get(j-1):"";
                                    cellPrint.append(prevCellVal);
                                    cellPrint.append("'>");
                                    cellPrint.append(cellVal);
                                    cellPrint.append("</a>");
                                }
                                else {
                                    cellPrint.append(cellVal == null ? "&nbsp;":cellVal);
                                }                                                                
                            }//for each row
                            if (cellPrint ==null || cellPrint.length() == 0)
                                out.println("&nbsp;");
                            else
                                out.println(cellPrint);                            
                            out.println("</td>");                            
  
                        }//for each inner col
                     }// else if doing nested table
                 }//end of doing multiValCol
                 else {                                          
                     int attribType = dimInfo != null ? dimInfo.getAttribType() : _cache.STRING_TYPE;
                     boolean doDate = dimInfo != null && attribType == com.ipssi.gen.utils.Cache.DATE_TYPE;
                     boolean doNumber = attribType == _cache.INTEGER_TYPE || attribType == _cache.LOV_NO_VAL_TYPE || attribType == _cache.NUMBER_TYPE;
                     
                     boolean doCurr =  dimInfo != null && attribType == _cache.NUMBER_TYPE && dimInfo.m_qtyType == 0; 
                     boolean doDouble = dimInfo != null && !doCurr && attribType == _cache.NUMBER_TYPE;
                     String val = null;
                     int tempValInt = Misc.getUndefInt();
                     
                     if (!doDate && !doCurr && !doDouble)                           
                         val = valGetter.getVal(_dbConnection, rset,0,i,_cache);
                     else {
                        if (doDate) {
                           val = dtfmt.format(valGetter.getValDate(_dbConnection, rset, 0,i,_cache));                                 
                        }
                        else if (doCurr) {
                           val = numberfmt.format(valGetter.getValDouble(_dbConnection, rset, 0,i,_cache));
                        }
                        else if (doDouble) {
                           double doubleVal = valGetter.getValDouble(_dbConnection, rset, 0,i,_cache);
                           //scale this if necessary by using ref_scale provided at measure level                                 
                           doubleVal /= dimInfo.m_scale;
                           val = unscaledFmt.format(doubleVal);
                        }
                     }
                     if (dimInfo != null && (dimInfo.m_id == 8015 || dimInfo.m_id == 8041)) {
                        tempValInt = valGetter.getValInt(_dbConnection, rset, 0, i, _cache);
                     }
                     if (dimConf.m_dimCalc.m_dimInfo != null && dimConf.m_dimCalc.m_dimInfo.m_id == 51 && (val == null  || val.length() == 0))
                         val = prjId;
                     boolean doLink = "link".equals(dimConf.m_disp);
                     boolean isAccessible = true;
                     if (dimInfo != null && dimInfo.m_accCheck != null && accessCheckControlInfo != null)
                        isAccessible = helperHasValAccess(dimInfo, accessCheckControlInfo, _session.getUser(), i, 0, valGetter, null, _cache,rset, _dbConnection, _session);
                     if (!isAccessible)
                        val = Misc.NO_FIELD_ACCESS_MSG;
                     if (attribType == Cache.LOV_NO_VAL_TYPE ||  attribType == Cache.FILE_TYPE || attribType == Cache.IMAGE_TYPE || (dimInfo != null && dimInfo.m_subsetOf == 203)) {
                       tempValInt = valGetter.getValInt(_dbConnection, rset, 0, i, _cache);
                       StringBuilder t1 = new StringBuilder();
                       _cache.printDimVals(_dbConnection, _session.getUser(), dimInfo, tempValInt, null, t1, null, false, null, false, Misc.getUndefInt(), 1, dimConf.m_width
                                    , false, null, false, false, true, null, null, Misc.getUndefInt(), Misc.getUndefInt(), null);
                       val = t1.toString();
                     }
                     
      
                     if (dimConf.m_innerMenuList != null && dimConf.m_innerMenuList.size() != 0) {
                    	 String linkMenuName = "page_link_menu"+(linkMenuIndex==0?"":Integer.toString(linkMenuIndex));
                    	 linkMenuIndex++;
                         out.println("<td class='" + tdClass + "'><img src='"+Misc.G_IMAGES_BASE+"prj_action.gif'  onMouseOut=\"closeDropDown('"+linkMenuName+"')\"  onMouseOver=\"showPageLinkMenu("+ prjId +",null,null,'"+linkMenuName+"')\"></td>");
                         if (colTimeList != null) {
                            long oldTime = currTimeMillis;                        
                            currTimeMillis = System.currentTimeMillis();
                            colTimeList[i+1] += (int) (currTimeMillis-oldTime);                        
                         }
                         continue;
                     }
                     if (dimConf.m_isSelect) {
                        int valInt = valGetter.getValInt(_dbConnection, rset,0,i,_cache);
                        if (dimConf.m_dimCalc.m_dimInfo == null) {
                           valInt = "1".equals(dimConf.m_default) ? 1 : 0;
                        }
                        boolean toShow = true;
                        for (int a1=0, a1s = dimConf.m_fixedForSel == null ? 0 : dimConf.m_fixedForSel.size(); a1<a1s;a1++) {
                             com.ipssi.gen.utils.DimConfigInfo.FixedHelper fix = (com.ipssi.gen.utils.DimConfigInfo.FixedHelper) dimConf.m_fixedForSel.get(a1);
                             int fixIndex = fix.m_idThenIndex;
                             if (fixIndex >= 0) {
                                int fixVal = valGetter.getValInt(_dbConnection, rset, 0, fixIndex,_cache);
                                if (!com.ipssi.gen.utils.Misc.isUndef(fixVal)) {
                                   for (int a2=0,a2s=fix.m_val == null ? 0 : fix.m_val.size();a2<a2s;a2++) {
                                      if (((Integer)fix.m_val.get(a2)).intValue() == fixVal) {
                                         toShow = false;
                                         break;
                                      }//if val matches
                                   }//for each vals for fixed rule
                                }//if good fix val
                             }//if good spec of fix index
                             if (!toShow)
                                break;
                        }//for each rule of fixed
                        if (toShow) {//make sure that it is not otherwise filtered off ...
                             toShow = matchesCheckBoxCrit;
                        }
                        String displayText = dimConf.m_hidden ? "style='display:none'" : "";
                        out.println("<td "+displayText+" class='" + tdClass + "'>");
                        if (toShow) {
                           if (origPrjList != null) {
                               if (origPrjList.length() > 0) 
                                  origPrjList.append(",");
                               origPrjList.append(prjId);
                           }                       
                           out.print("<input class='tn' type='"+( doingMultiSelect ? "checkbox" : "radio" )+"' name='"+selectCheckBoxVarName+"' value='"+ prjId+ ( valInt != 0 ? "' checked" : "' " ));
                          // if (!doingMultiSelect)
                          //    out.print(" onclick='autoSelect()' ");
                           out.println("/>");
                        }
                        else 
                           out.println("&nbsp;");
                       
                        out.println("</td>");                   
                        if (colTimeList != null) {
                            long oldTime = currTimeMillis;                        
                            currTimeMillis = System.currentTimeMillis();
                            colTimeList[i+1] += (int) (currTimeMillis-oldTime);                        
                         }
                        continue;
                     }//if doing checkbox
                     String alignText = doNumber ? "" : "";
                     String displayText = dimConf.m_hidden ? "style='display:none'" : "";
                     out.println("<td "+alignText+" "+displayText+" class='" + tdClass + "'>");
                     if (doLink) {
                        
                        if (dimConf.m_targetPage != null && dimConf.m_targetPage.length() != 0 && dimConf.m_objectIdParam != null && dimConf.m_objectIdParam.length() != 0){                           
                           tempBuf.setLength(0);
                           tempBuf.append("<a ").append(targetForLink != null ? targetForLink : "").append(" href='").append(dimConf.m_targetPage).append("?").append(dimConf.m_objectIdParam).append("=");
                           String prevCellVal = i != 0 ? valGetter.getVal(_dbConnection, rset, 0, i-1, _cache) :"";
                           tempBuf.append(prevCellVal);
                           tempBuf.append("'>");
                           out.println(tempBuf);
                        }
                        else if (linkStr != null && linkStr.length() != 0 && !"#".equals(linkStr))
                           out.println(linkStr.toString());
                        else {
                           out.println("<a href='#'>");
                        }                        
                     }
                    // hack for showing purchasing issue flag sameer 12192007
                     if (dimInfo != null && (dimInfo.m_id == 8015 || dimInfo.m_id == 8041)) {
                      String imageUrl = ""+Misc.G_IMAGES_BASE+"";
                      
                      if (tempValInt == 1)
                        imageUrl += "flag_yellow.gif";
                      else if (tempValInt == 2)
                        imageUrl += "flag_red.gif";
                      else                      
                        imageUrl += "flag_green.gif";
                      out.println("<img height=\"16\" width=\"16\" src=\"" + imageUrl + "\"/>");
                     }
                     else {
                        out.println(val == null || val.length() == 0 ? "&nbsp;" :val);
                     }
    
                     if (doLink)
                        out.println("</a>");                  
                     if (dimConf.m_dimCalc.m_dimInfo != null && dimConf.m_dimCalc.m_dimInfo.m_id == 11) { //print name in hidden text box so that project picker can pick that up                  
                        out.println("<input type='hidden' name='name' value='"+val+"'>");              
                     }
                     out.println("</td>");
                     if (colTimeList != null) {
                            long oldTime = currTimeMillis;                        
                            currTimeMillis = System.currentTimeMillis();
                            colTimeList[i+1] += (int) (currTimeMillis-oldTime);                        
                     }
                 }//!isMultiValCol
    				 } //all cols printed except for wksp versions             
         }
         catch (Exception e) {
             e.printStackTrace();
             throw e;
         }
      }//end of print single row
  public boolean needsMulti() {
      return needsMulti(m_frontInfoList);
  }
  public static boolean needsMulti(ArrayList dimConfigList) {
      for (int i=0,is = dimConfigList.size();i<is;i++) {
         //if (getSubColCount((DimConfigInfo) dimConfigList.get(i)) > 1)
		  MiscInner.PairIntBool intBoolPair = getSubColCount((DimConfigInfo)dimConfigList.get(i));
		  if (intBoolPair.second)
			return true;
        // if (isMultiColCell((DimConfigInfo) dimConfigList.get(i))) //consider prj multi attrib dims as multicol
        //    return true;         
      }
      return false;
  }
  public ArrayList loadMultiAttribsForList(Connection dbConn, SessionManager session, FmtI.Date dtfmt, FmtI.Number numberfmt, FmtI.Number unscaledFmt, String pgContext, boolean doingInterleaved, String addnlAndClause, ArrayList paramForAddnlAndClause, ArrayList searchCriteriaConfig, MiscInner.ContextInfo currencyEtcContext) throws Exception {//TODO
  //TODO - update getEnh to remember of multiAttribParse needs to be called .. being done in needsMulti
  //     - printTable .. loadMultiAttribsForList
  //     - printRow,Col get the smarts to print link, to print multiple rows, nested tables etc.
	  return null;
  /*
      ArrayList dimConfigList = m_frontInfoList;
      ArrayList multiAttribQueryParts = m_multiAttribQueryParts;      
      if (multiAttribQueryParts == null)
          return null;
      int searchType = m_queryTimeReq.m_hasSuppliersTable ? PurchaseSearch.G_FOR_SUPPLIER : m_queryTimeReq.m_hasOrdersTable ? PurchaseSearch.G_FOR_ORDER : PurchaseSearch.G_FOR_PROJECT;
      String itemSubSelQuery = null;
      ArrayList params = new ArrayList();
      //calculate the above params from frontPage itself
      
      StringBuilder addnlAndClauseBuf = null;
      if (addnlAndClause != null) {
         addnlAndClauseBuf = new StringBuilder();
         addnlAndClauseBuf.append(addnlAndClause);
      }
      for (int t1=0,t1s= paramForAddnlAndClause == null ? 0 : paramForAddnlAndClause.size();t1<t1s;t1++)
          params.add(paramForAddnlAndClause.get(t1));
      ArrayList paramForPort = new ArrayList();//TO_PORT_FORWARD
      //041508 ..... instead of m_frontSearchCriteria we want to pass searchCriteriaConfig
      //itemSubSelQuery = PrjTemplateHelper.getFrontSelQuery(null, null, m_frontSearchCriteria, session,  this, session.getCache(), dbConn, pgContext, addnlAndClauseBuf, params, true, doingInterleaved, paramForPort, dtfmt);
      itemSubSelQuery = PrjTemplateHelper.getFrontSelQuery(null, null, searchCriteriaConfig, session,  this, session.getCache(), dbConn, pgContext, addnlAndClauseBuf, params, true, doingInterleaved, paramForPort, dtfmt);
      
      if (!m_doingSupplier) {
          User _user = session.getUser();
          int uid = (int)_user.getUserId();
          Integer uidInteger = new Integer(uid);
          for (int t2=0,t2s= m_queryTimeReq.m_numUserIdReqForPriv; t2<t2s;t2++)
            params.add(0, uidInteger);
          
          
          if (m_isForApprovalSpecial == 1) {
             params.add(uidInteger);
             params.add(uidInteger);
          }      
      }
      if (paramForPort != null && paramForPort.size() > 0)//TO_PORT_FORWARD
          params.add(0, paramForPort.get(0));

      return PrjTemplateHelper.loadMultiAttribsForList(dimConfigList, multiAttribQueryParts, itemSubSelQuery, dbConn, session, searchType, params, dtfmt, numberfmt, unscaledFmt, currencyEtcContext);
      */
  }      
  
  
  public static boolean isMultiColCell(DimConfigInfo dc) { //, boolean dontConsiderMultiAttrib) {
  
      DimCalc dcalc = dc.m_dimCalc;
      if ((dc.m_multiRowSubColList != null && dc.m_multiRowSubColList.size() > 0) ||
         (dcalc.m_groupBy != null && dcalc.m_groupBy.size() > 0)
         )
         return true;
      DimInfo dimInfo = dcalc.m_dimInfo;
      ColumnMappingHelper colMap = dimInfo == null ? null : dimInfo.m_colMap;
      if (colMap == null)
             return false;
      String colTable = colMap.table;
      
      //if (!dontConsiderMultiAttrib && ("prj_multi_attrib".equals(colTable) || "order_multi_attrib".equals(colTable) || "supplier_multi_attrib".equals(colTable) || "pur_cost_parameters".equals(colTable)))
      if (("prj_multi_attrib".equals(colTable) || "order_multi_attrib".equals(colTable) || "supplier_multi_attrib".equals(colTable) || "pur_cost_parameters".equals(colTable)))
            return true;
      return false;
  }

	public static MiscInner.PairIntBool getSubColCount(DimConfigInfo dc) { 
		// boolean part returns true if there are sub cols and int part return num of cols
		// int part returns 0 if no subCol!!
		boolean subColPresent = false;
		int colCount = 0;

		DimCalc dcalc = dc.m_dimCalc;
		if ((dcalc.m_groupBy != null && dcalc.m_groupBy.size() > 0)) {
			subColPresent = true;
			DimInfo.DimValList dv = (DimInfo.DimValList)dcalc.m_groupBy.get(0);
			DimInfo groupDim = dv.m_dimInfo;
			ArrayList valList = dv.getValListOfValInfo();
			if (valList == null || valList.size() == 0)
				valList = groupDim.getValList();
			colCount = valList.size();
		}
		else if ((dc.m_multiRowSubColList != null && dc.m_multiRowSubColList.size() > 0)) {
			for (int i = 0, is = dc.m_multiRowSubColList.size(); i < is; i++) {
				DimConfigInfo sdc = (DimConfigInfo)dc.m_multiRowSubColList.get(i);
				if (!sdc.m_hidden)
					colCount++;
			}
			if (colCount > 1)
				subColPresent = true;
		}

		if (colCount == 0)
			colCount = 1;

		return new MiscInner.PairIntBool(colCount, subColPresent);
	}

  public static int getSubColCount_old(DimConfigInfo dc) { //returns 0 if no subCol!!
      DimCalc dcalc = dc.m_dimCalc;
      if ((dcalc.m_groupBy != null && dcalc.m_groupBy.size() > 0)) {
          DimInfo.DimValList dv = (DimInfo.DimValList) dcalc.m_groupBy.get(0);
          DimInfo groupDim = dv.m_dimInfo;
          ArrayList valList = dv.getValListOfValInfo();
          if (valList == null || valList.size() == 0)
              valList = groupDim.getValList();
          return valList.size();
      }
      else if ((dc.m_multiRowSubColList != null && dc.m_multiRowSubColList.size() > 0)) {
         int count = 0;
         for (int i=0,is=dc.m_multiRowSubColList.size();i<is;i++) {
            DimConfigInfo sdc = (DimConfigInfo) dc.m_multiRowSubColList.get(i);
            if (!sdc.m_hidden)
               count++;
         }
         return count;
      }
      return 0;
  }
  
  
  public static MiscInner.PairIntBool helpIsIdOrPortIdOfObjType(DimConfigInfo config) {
	  /* $TRACK
     if (config != null) {
         DimCalc dc = (DimCalc) config.m_dimCalc;
         if (dc == null || dc.m_dimInfo == null || dc.m_dimInfo.m_colMap == null)
             return null;
         DimInfo di = dc.m_dimInfo;
         ColumnMappingHelper colMap = di.m_colMap;
         int objectType = -1;
         boolean isObjId = false;
         boolean isPortId = false;
         
         if ("projects".equals(colMap.table) && "id".equals(colMap.column)) {
            objectType = PurchaseSearch.G_FOR_PROJECT;
            isObjId = true;
         }
         else if ("orders".equals(colMap.table) && "id".equals(colMap.column)) {
            objectType = PurchaseSearch.G_FOR_ORDER;
            isObjId = true;
         }
         else if ("suppliers".equals(colMap.table) && "id".equals(colMap.column)) {
            objectType = PurchaseSearch.G_FOR_SUPPLIER;
            isObjId = true;
         }
         else if ("pur_cost_items".equals(colMap.table) && "id".equals(colMap.column)) {
            objectType = PurchaseSearch.G_FOR_CCBS;
            isObjId = true;
         }
         else if (colMap.column.equals("prj_id") || colMap.column.endsWith(".prj_id")) {
        	objectType = PurchaseSearch.G_FOR_PROJECT;
            isObjId = true;
         }
         else if (colMap.column.equals("order_id") || colMap.column.endsWith(".order_id")) {
            objectType = PurchaseSearch.G_FOR_ORDER;
            isObjId = true;
         }
         else if (colMap.column.equals("supplier_id") || colMap.column.endsWith(".supplier_id")) {
            objectType = PurchaseSearch.G_FOR_SUPPLIER;
            isObjId = true;
         }
         else if (colMap.column.equals("cost_item_id") || (colMap.column.endsWith(".cost_item_id") && colMap.table.startsWith("pur_"))) {
            objectType = PurchaseSearch.G_FOR_CCBS;
            isObjId = true;
         }
         else if (colMap.table.equals("projects") && colMap.column.equals("port_node_id")) {
            objectType = PurchaseSearch.G_FOR_PROJECT;
            isPortId = true;
         }
         else if (colMap.table.equals("order_details") && colMap.column.equals("port_node_id")) {
            objectType = PurchaseSearch.G_FOR_ORDER;
            isPortId = true;
         }             
         if (objectType >= 0)
            return new MiscInner.PairIntBool(objectType, isObjId ? true : false);
            
     }//if config
     */
     return null;
  }
  
  public static ArrayList helpGetObjIdOrgIndexWoNest(ArrayList dimConfigList) {
     //ArrayList is indexed by object type and will contain Pair of id index, org id index relevant for that obj type
     ArrayList retval = new ArrayList();
     for (int i=0,is = dimConfigList == null ? 0 : dimConfigList.size();i<is;i++) {
         DimConfigInfo config = (DimConfigInfo) dimConfigList.get(i);
         MiscInner.PairIntBool configInterpret = helpIsIdOrPortIdOfObjType(config);
         int objectType = -1;
         int objIndex = -1;
         int portIndex = -1;
         if (configInterpret != null && configInterpret.first >= 0) {
             objectType = configInterpret.first;
             if (configInterpret.second)
                 objIndex = i;
             else
                 portIndex = i;
         }
         
         if (objectType >= 0) {             
             if (retval.size() <= objectType) {
                 for (int t1=retval.size();t1<=objectType;t1++)
                    retval.add(null);
             }
             MiscInner.Pair info = (MiscInner.Pair) retval.get(objectType);
             if (info == null) {
                info = new MiscInner.Pair(-1,-1);
                retval.set(objectType, info);
             }
             if (objIndex >= 0)
                info.first = objIndex;
             if (portIndex >= 0)
                info.second = portIndex;
         }
     }//for each
     return retval;
  }//end of function
  
  public static HashMap getAccessControlInfo(ArrayList dimConfigList) { //If some dim configured for acc check then the way to determine the access check. key: Pair of topPos, innerPos value Value = MiscInner.ItemAccControlInfo
     ArrayList topLevelObjInfo = null;
     HashMap retval = null;
     for (int i=0,is = dimConfigList == null ? 0 : dimConfigList.size();i<is;i++) {
         DimConfigInfo config = (DimConfigInfo) dimConfigList.get(i);
         if (config != null) {
             DimCalc dc = (DimCalc) config.m_dimCalc;
             if (dc != null && dc.m_dimInfo != null) {
                 DimInfo di = dc.m_dimInfo;
                 
                 ColumnMappingHelper colMap = di.m_colMap;
                 if (di.m_accCheck != null) {
                    if (topLevelObjInfo == null)
                       topLevelObjInfo =  helpGetObjIdOrgIndexWoNest(dimConfigList);
                    retval = helperAddAccessControl(di, topLevelObjInfo, null, retval, i, 0);
                    
                 }//if accCheck specified
             }//dc != null && m_dimInfo != null
             if (config.m_multiRowSubColList != null && config.m_multiRowSubColList.size() != 0) {
                 ArrayList innerObjInfo = null;
                 for (int j=0,js = config.m_multiRowSubColList.size();j<js;j++) {
                     DimConfigInfo innerConfig = (DimConfigInfo) config.m_multiRowSubColList.get(j);
                     if (innerConfig != null) {
                         DimCalc innerDc = (DimCalc) innerConfig.m_dimCalc;
                         if (innerDc != null && innerDc.m_dimInfo != null) {
                             DimInfo innerDim = innerDc.m_dimInfo;
                             if (innerDim.m_accCheck != null) { //inner Dc empty
                                if (topLevelObjInfo == null)
                                   topLevelObjInfo =  helpGetObjIdOrgIndexWoNest(dimConfigList);            
                                if (innerObjInfo == null)
                                   innerObjInfo = helpGetObjIdOrgIndexWoNest(config.m_multiRowSubColList);
                                retval = helperAddAccessControl(innerDim, topLevelObjInfo, innerObjInfo, retval, i, j);
                             }
                         }//innDc != null
                     }//if innerConfig != null
                 }//for each inner col                 
             }//if there are innerCol
         }//if config is null
     }//for each entry in dimConfigList          
     return retval;
  }//end of function
  
  public static HashMap helperAddAccessControl(DimInfo dim, ArrayList topLevelObjInfo, ArrayList innerObjInfo, HashMap retval, int mainIndex, int innerIndex) {//creates retval if null, else returns retval populated with control info for how to determine if the project is visible
      int objType = dim.m_accCheckonObj;
      MiscInner.Pair prjInfo = null; //1st is obj id, 2nd is port for the obj
      MiscInner.Pair objInfo = null; //1st is obj id, 2nd is port for the obj
      boolean objInOuter = true;
      boolean prjInOuter = true;
      if (innerObjInfo != null && innerObjInfo.size() > objType) {
         objInfo = (MiscInner.Pair) innerObjInfo.get(objType);         
         objInOuter = false;
      }
      //$Trackif (innerObjInfo != null && innerObjInfo.size() > PurchaseSearch.G_FOR_PROJECT) {
      //   prjInfo = (MiscInner.Pair) innerObjInfo.get(PurchaseSearch.G_FOR_PROJECT);         
      //   prjInOuter = false;
      //}
      if (objInfo == null && topLevelObjInfo != null && topLevelObjInfo.size() > objType) {
         objInfo = (MiscInner.Pair) topLevelObjInfo.get(objType);         
      }
      //$Track if (prjInfo == null && topLevelObjInfo != null && topLevelObjInfo.size() > PurchaseSearch.G_FOR_PROJECT) {
      //   prjInfo = (MiscInner.Pair) topLevelObjInfo.get(PurchaseSearch.G_FOR_PROJECT);         
      //}
      if (objInfo != null || prjInfo != null) {
          if (retval == null)
             retval = new HashMap();
          MiscInner.ItemAccControlInfo accInfo = new MiscInner.ItemAccControlInfo();
          if (objInfo != null) {
             accInfo.m_objIndex = new MiscInner.Pair(0,objInfo.first);
             accInfo.m_orgIdIndex = new MiscInner.Pair(0,objInfo.second);
          }
          if (prjInfo != null) {
             accInfo.m_prjIdIndex = new MiscInner.Pair(0,prjInfo.first);
             accInfo.m_orgIdOfPrj = new MiscInner.Pair(0,prjInfo.second);
          }
          accInfo.m_objType = objType;
          accInfo.m_objInOuter = objInOuter;
          accInfo.m_prjInOuter = prjInOuter;
          retval.put(new MiscInner.Pair(mainIndex, innerIndex), accInfo);
             
      }
      return retval;
  }//end of func
  public static boolean helperHasValAccess(DimInfo dimInfo, HashMap accControlInfo, User user, int mainIndex, int innerIndex, FrontGetValHelper valGetter, ArrayList rowOfMultiCol, Cache cache, ResultSet rset, Connection dbConn, SessionManager session) throws Exception {
      MiscInner.ItemAccControlInfo itemAcc = accControlInfo == null ? null : (MiscInner.ItemAccControlInfo) accControlInfo.get(new MiscInner.Pair(mainIndex, innerIndex));
      if (itemAcc == null) 
         return true;
      return helperHasValAccess(dimInfo, itemAcc, user, valGetter, rowOfMultiCol, cache, rset, dbConn, session, false);
  }
  public static boolean helperHasValAccess(DimInfo dimInfo, MiscInner.ItemAccControlInfo itemAcc, User user, FrontGetValHelper valGetter, ArrayList rowOfMultiCol, Cache cache, ResultSet rset, Connection dbConn, SessionManager session, boolean lookPrjOrdFromSession) throws Exception {      
      if (itemAcc == null)
         return true;
         
      int projectId = Misc.getUndefInt();
      int orgIdOfPrj = Misc.getUndefInt();
      int objectId = Misc.getUndefInt();
      int orgIdOfObj = Misc.getUndefInt();
      
      int objRow = itemAcc.m_objIndex == null ? -1 : itemAcc.m_objIndex.first;
      int objCol = itemAcc.m_objIndex == null ? -1 : itemAcc.m_objIndex.second;
      int objOrgRow = itemAcc.m_orgIdIndex == null ? -1 : itemAcc.m_orgIdIndex.first;
      int objOrgCol = itemAcc.m_orgIdIndex == null ? -1 : itemAcc.m_orgIdIndex.second;
      int prjRow = itemAcc.m_prjIdIndex == null ? -1 : itemAcc.m_prjIdIndex.first;
      int prjCol = itemAcc.m_prjIdIndex == null ? -1 : itemAcc.m_prjIdIndex.second;
      int prjOrgRow = itemAcc.m_orgIdOfPrj == null ? -1 : itemAcc.m_orgIdOfPrj.first;
      int prjOrgCol = itemAcc.m_orgIdOfPrj == null ? -1 : itemAcc.m_orgIdOfPrj.second;
      
      if (itemAcc.m_objInOuter) {
        if (rset != null) {
           if (objRow >= 0 && objCol >= 0) {
               objectId = valGetter.getValInt(dbConn, rset, objRow, objCol, cache);             
           }
           if (objOrgRow >= 0 && objOrgCol >= 0) {
               orgIdOfObj = valGetter.getValInt(dbConn, rset, objOrgRow, objOrgCol, cache);
           }
        }
      }
      else {
         if (objRow >= 0 && objCol >= 0) {
             String v = rowOfMultiCol == null || rowOfMultiCol.size() <= objCol ? null : (String) rowOfMultiCol.get(objCol);
             objectId = Misc.getParamAsInt(v);
         }
         if (objOrgRow >= 0 && objOrgCol >= 0) {
             String v = rowOfMultiCol == null || rowOfMultiCol.size() <= objOrgCol ? null : (String) rowOfMultiCol.get(objOrgCol);
             orgIdOfObj = Misc.getParamAsInt(v);
         }
      }
      if (itemAcc.m_prjInOuter) {
         if (rset != null) {
             if (prjRow >= 0 && prjCol >= 0) {
                 projectId = valGetter.getValInt(dbConn, rset, prjRow, prjCol, cache);             
             }
             if (prjOrgRow >= 0 && prjOrgCol >= 0) {
                 orgIdOfPrj = valGetter.getValInt(dbConn, rset, prjOrgRow, prjOrgCol, cache);
             }
         }
      }
      else {
         if (prjRow >= 0 && prjCol >= 0) {
             String v = rowOfMultiCol == null || rowOfMultiCol.size() <= prjCol ? null : (String) rowOfMultiCol.get(prjCol);
             projectId = Misc.getParamAsInt(v);
         }
         if (prjOrgRow >= 0 && prjOrgCol >= 0) {
             String v = rowOfMultiCol == null || rowOfMultiCol.size() <= prjOrgCol ? null : (String) rowOfMultiCol.get(prjOrgCol);
             orgIdOfPrj = Misc.getParamAsInt(v);
         }
      }
      PrivInfo.TagInfo tagInfo = dimInfo.m_accCheck;
      int objectType = dimInfo.m_accCheckonObj;
      if (lookPrjOrdFromSession && Misc.isUndef(projectId))
         projectId = (int) session.getProjectId();
      //$Track if (objectType == PurchaseSearch.G_FOR_ORDER && lookPrjOrdFromSession && Misc.isUndef(objectId))
      //   objectId = Misc.getParamAsInt(session.getParameter("order_id"));
      boolean retval = user.isPrivAvailable(session, tagInfo.m_read, projectId, Misc.getUndefInt(), Misc.getUndefInt(),false,orgIdOfPrj, objectType, objectId, orgIdOfObj, null);
      if (!retval)
         retval = user.isPrivAvailable(session, tagInfo.m_write, projectId, Misc.getUndefInt(), Misc.getUndefInt(),false, orgIdOfPrj, objectType, objectId, orgIdOfObj, null);
      return retval;      
      
  }
  
  public static int getFrontTemplateId(SessionManager session, String pgContext, User user, int defaultTemplateId) throws Exception {     
      String retval = UserGen.getParamForPrivSeq(session, pgContext, "front_template_id", session.getCache(), Misc.isUndef(defaultTemplateId)?null : Integer.toString(defaultTemplateId));
      return Misc.getParamAsInt(retval);
  }

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
   
   public static FrontPageInfo getFrontPage(ReportDetailVO reportDetailVO, boolean doReload, Connection dbConn, Cache cache) throws Exception {
	   FrontPageInfo retval = (FrontPageInfo) g_frontPageList.get(reportDetailVO.getFileName());
	   if (retval == null || doReload) {
		   retval = createFrontPageEntry(reportDetailVO, dbConn, cache);
		   synchronized (g_frontPageList){
			   g_frontPageList.put(reportDetailVO.getFileName(), retval);
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
           if (false && entry.m_headerInfo != null) {
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

   private  static FrontPageInfo createFrontPageEntry(ReportDetailVO reportDetailVO, Connection dbConn, Cache cache) throws Exception {//synced at Cache
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
 	   FileInputStream inp = null;
 	   Document frontXML = null;
    
 	   try {
 		  frontXML = com.ipssi.gen.utils.MyXMLHelper.loadFromString(reportDetailVO.getXmlData());	
//	      inp = new FileInputStream(Cache.serverConfigPath+System.getProperty("file.separator")+"web_look"+System.getProperty("file.separator")+"project"+System.getProperty("file.separator")+fileName);
//	      MyXMLHelper test = new MyXMLHelper(inp, null);
//	      frontXML =  test.load();           
 	   }
 	   catch (Exception e2) {
 		   throw e2;
 		//   return null;
 	   }
 	   finally {
 		   if (inp != null)
 			   inp.close();
 		   inp = null;
 	   }
        if (frontXML == null && frontXML.getDocumentElement() == null) {
            return null;
        }
        FrontPageInfo entry = new FrontPageInfo();
        
        entry.m_label = label;
        entry.m_help = frontXML.getDocumentElement().getAttribute("help");
        entry.m_doinplaceEdit = inplaceEdit;
        entry.m_showSearchFirstTime = showSearchFirstTime;
        entry.m_id = templateId;
        entry.m_fileName = reportDetailVO.getFileName();
        entry.m_createProject = createProject;           
        entry.m_multiSelect = multiSelect;
        entry.m_targetPage = targetPage;
        entry.m_targetContext = targetContext;
        Element topElem = frontXML.getDocumentElement();
        entry.m_customField1 = Misc.getParamAsString(topElem.getAttribute("custom_field1"), null);
        entry.m_customField2 = Misc.getParamAsString(topElem.getAttribute("custom_field2"), null);
        entry.m_customField3 = Misc.getParamAsString(topElem.getAttribute("custom_field3"), null);
        entry.m_doZeroRow = "1".equals(topElem.getAttribute("do_zero_row"));
        entry.m_doRollupAtJava = Misc.g_doRollupAtJava ? !"0".equals(topElem.getAttribute("rollup_java")) //rollup at java preferred - explicitly downgrade
                : "1".equals(topElem.getAttribute("rollup_java"));
        entry.m_templateId = topElem.getAttribute("create_template_id");
        entry.m_hackTrackDriveTimeTableJoinLoggedData = "1".equals(topElem.getAttribute("hack_track_driver_timetable_join_logged_data"));
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
        entry.m_objectIdColName = Misc.getParamAsString(topElem.getAttribute("object_col_name"), entry.m_objectIdColName);
        entry.m_interleavedInstructText = Misc.getParamAsString(topElem.getAttribute("interleaved_text"), "&nbsp;");
        entry.m_doingSupplier = "1".equals(topElem.getAttribute("for_supplier"));
        entry.m_doingOrder = "1".equals(topElem.getAttribute("for_order"));
        //entry.m_hackDoGetPrjListFromOrderToo = "1".equals(topElem.getAttribute("get_prj_list_from_order_access"));
        
        String temp = Misc.getParamAsString(topElem.getAttribute("priv_to_check"), null);
        if (temp != null)
           entry.m_privTagToCheck = Misc.convertValToArray(temp);

        

        Element searchNode = null;
        Element optionNode = null;
        Element dashboardNode = null;// to load dashboard related info


//old           StringBuilder selClause = new StringBuilder();
        for (Node n = frontXML.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
           if (n.getNodeType() != 1)
              continue;
           Element elem = (Element) n;
           if (elem.getTagName().equals("dashboard")) {
         	  dashboardNode = elem;
               continue;
            }
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
        if (dashboardNode != null) {
            entry.m_frontDashboardList = readRowColInfo(dashboardNode, false);
//            entry.m_hideSearchGlobally = "1".equals(searchNode.getAttribute("hidden"));
            //TODO - right now if date search is required then it should also be present in the view list
//            for (int l=0,ls=entry.m_frontDashboardList.size();l<ls;l++) {
//                ArrayList r = (ArrayList) entry.m_frontDashboardList.get(l);
//                for (int k=0,ks = r.size();k<ks;k++) {
//                   DimConfigInfo dc = (DimConfigInfo) r.get(k);
//                   DimInfo di = dc.m_dimCalc.m_dimInfo;
//                   ColumnMappingHelper colMap = di == null ? null : di.m_colMap;
//                   if (colMap.table.endsWith("_multi_attrib") || colMap.table.equals("orders_suppliers")) {
//                      MiscInner.PurSearchItemPart toAdd = new MiscInner.PurSearchItemPart();
//                      toAdd.m_dimInfo = di;
//                      toAdd.m_purConds = new ArrayList();
//                      entry.m_frontExtMultiCriteria.add(toAdd);
//                      MiscInner.PurSearchCondPart condPart = new MiscInner.PurSearchCondPart();
//                      toAdd.m_purConds.add(condPart);
//                      if (di.m_type == Cache.LOV_TYPE) 
//                         condPart.m_condType = 1;//oneOf                         
//                      else if (di.m_type == Cache.STRING_TYPE)
//                         condPart.m_condType = 9;//like
//                      else
//                         condPart.m_condType = 5;//greater or equal
//                      
//                   }
//                }
//            }
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
                  OptionInfo option = new OptionInfo(id, code,olabel, directPage, paramTemplate, doPopup,suspendTimer, targetTag, javascriptOptional);
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
        entry.postProcess(frontXML.getDocumentElement(), false); //currently does LinkHelper fill for all dimConfig
        return entry;



//old           createPrjInfoFrontPageQueryOld(selClause, entry);
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
    	   FileInputStream inp = null;
    	   Document frontXML = null;
       
    	   try {
    		   String loadFileName=Cache.serverConfigPath+System.getProperty("file.separator")+"web_look"+System.getProperty("file.separator")+"project"+System.getProperty("file.separator")+fileName;
    		   System.out.println("Load File="+loadFileName);
	           inp = new FileInputStream(loadFileName);
	           MyXMLHelper test = new MyXMLHelper(inp, null);
	           frontXML =  test.load();           
    	   }
    	   catch (Exception e2) {
    		   throw e2;
    		//   return null;
    	   }
    	   finally {
    		   if (inp != null)
    			   inp.close();
    		   inp = null;
    	   }
           if (frontXML == null || frontXML.getDocumentElement() == null) {
               return null;
           }
           FrontPageInfo entry = new FrontPageInfo();
       		Element topElem = frontXML.getDocumentElement();
       		FrontPageInfo.readFrontPageTopLevel(topElem, entry);
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


           Element searchNode = null;
           Element optionNode = null;
           Element dashboardNode = null;// to load dashboard related info
           Element buttonNode = null;
           Element chartNode=null;

//old           StringBuilder selClause = new StringBuilder();
           for (Node n = frontXML.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
              if (n.getNodeType() != 1)
                 continue;
              Element elem = (Element) n;
              if (elem.getTagName().equals("button")) {
            	  buttonNode = elem;
                  continue;
               }
              if (elem.getTagName().equals("dashboard")) {
            	  dashboardNode = elem;
                  continue;
               }
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
              else if (elem.getTagName().equals("chart")) {
                  chartNode = elem;
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
           if(buttonNode != null){
        	   entry.m_frontButtonList = readRowColInfo(buttonNode, false);
           }
           if (dashboardNode != null) {
               entry.m_frontDashboardList = readRowColInfo(dashboardNode, false);
//               entry.m_hideSearchGlobally = "1".equals(searchNode.getAttribute("hidden"));
               //TODO - right now if date search is required then it should also be present in the view list
//               for (int l=0,ls=entry.m_frontDashboardList.size();l<ls;l++) {
//                   ArrayList r = (ArrayList) entry.m_frontDashboardList.get(l);
//                   for (int k=0,ks = r.size();k<ks;k++) {
//                      DimConfigInfo dc = (DimConfigInfo) r.get(k);
//                      DimInfo di = dc.m_dimCalc.m_dimInfo;
//                      ColumnMappingHelper colMap = di == null ? null : di.m_colMap;
//                      if (colMap.table.endsWith("_multi_attrib") || colMap.table.equals("orders_suppliers")) {
//                         MiscInner.PurSearchItemPart toAdd = new MiscInner.PurSearchItemPart();
//                         toAdd.m_dimInfo = di;
//                         toAdd.m_purConds = new ArrayList();
//                         entry.m_frontExtMultiCriteria.add(toAdd);
//                         MiscInner.PurSearchCondPart condPart = new MiscInner.PurSearchCondPart();
//                         toAdd.m_purConds.add(condPart);
//                         if (di.m_type == Cache.LOV_TYPE) 
//                            condPart.m_condType = 1;//oneOf                         
//                         else if (di.m_type == Cache.STRING_TYPE)
//                            condPart.m_condType = 9;//like
//                         else
//                            condPart.m_condType = 5;//greater or equal
//                         
//                      }
//                   }
//               }
           }
           
           if (chartNode != null) {
        	  entry.chartInfo=new ChartInfo(chartNode);
        	   
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
                      if (colMap != null && (colMap.table.endsWith("_multi_attrib") || colMap.table.equals("orders_suppliers"))) {
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
           entry.postProcess(frontXML.getDocumentElement(), false); //currently does LinkHelper fill for all dimConfig
           return entry;



//old           createPrjInfoFrontPageQueryOld(selClause, entry);
       }
       catch (Exception e) {
           e.printStackTrace();
           throw e;
       }
   }

   public static void readFrontPageTopLevel(Element topElem, FrontPageInfo entry) {
  		entry.projectNameLookupFieldName = Misc.getParamAsString(topElem.getAttribute("project_name_wb_col"), "d90200");
  		
  		String ts = Misc.getParamAsString(topElem.getAttribute("text_row_marker"), "-");
  		if (ts.length() > 0)
  			entry.textPrinterRowMarker = ts.charAt(0);
  		entry.textPrinterDoHeaderRowMarker = !"0".equals(topElem.getAttribute("text_do_header_row_marker"));
  		entry.textPrinterDoIntermediareRowMarker = "1".equals(topElem.getAttribute("text_do_intermediate_row_marker"));
  		entry.textPrinterDoGroupingRowMarker = !"0".equals(topElem.getAttribute("text_do_grouping_row_marker"));
  		entry.textPrinterInBetweenColSeparator = Misc.getParamAsString(topElem.getAttribute("text_col_spearator"),entry.textPrinterInBetweenColSeparator, false);
  		entry.textPrinterRowStarter = Misc.getParamAsString(topElem.getAttribute("text_row_starter"),entry.textPrinterRowStarter);
  		entry.textPrinterRowEnder = Misc.getParamAsString(topElem.getAttribute("text_row_ender"),entry.textPrinterRowEnder);
  		
  		entry.textMaxRowsPerPage = Misc.getParamAsInt(topElem.getAttribute("text_rows_page"), entry.textMaxRowsPerPage);
  		entry.textBlankHeaderLine = Misc.getParamAsInt(topElem.getAttribute("text_blankheader_line"), entry.textBlankHeaderLine);
  		entry.textBlankFooterLine = Misc.getParamAsInt(topElem.getAttribute("text_blankFooter_line"), entry.textBlankFooterLine);
  		
  		entry.textFirstPageHeaderFile = Misc.getParamAsString(topElem.getAttribute("text_header_first"), entry.textFirstPageHeaderFile);
  		entry.textEscCode = Misc.getParamAsString(topElem.getAttribute("text_esc_code"), entry.textEscCode);
  		entry.textLastPageFooterFile = Misc.getParamAsString(topElem.getAttribute("text_footer_last"), entry.textLastPageFooterFile);
  		entry.textInnerPageHeaderFile = Misc.getParamAsString(topElem.getAttribute("text_header_inner"), entry.textInnerPageHeaderFile);
  		entry.refTextInnerPageHeader = Misc.getParamAsString(topElem.getAttribute("ref_text_inner"), entry.refTextInnerPageHeader);
  		entry.textInnerPageFooterFile = Misc.getParamAsString(topElem.getAttribute("text_footer_inner"), entry.textInnerPageFooterFile);

  		entry.m_driverObjectLocTracker = Misc.getParamAsString(topElem.getAttribute("driver_object"), entry.m_driverObjectLocTracker);
  		
  		//0 dont do regardless of global parameter, 1 do regardless of global parameter 2 only if global parameter says yes
  		entry.m_orgTimingBased = Misc.getParamAsInt(topElem.getAttribute("do_org_time_based"), entry.m_orgTimingBased); 

  		entry.m_doPivotMeasureFirstRow = Misc.getParamAsInt(topElem.getAttribute("pivot_measure_show"), entry.m_doPivotMeasureFirstRow);
  		for (int i=0;true;i++) {
     	  String sbq = topElem.getAttribute("driver_for_subq_"+i);
     	  int sbqn = Misc.getParamAsInt(sbq);
     	  if (Misc.isUndef(sbqn)) {
     		  break;
     	  }
     	  if (entry.subQueryDrivers == null) 
     		  entry.subQueryDrivers = new ArrayList<Integer>();
     	  entry.subQueryDrivers.add(sbqn);
       }
  		
  		for (int i=0;true;i++) {
       	  String sbq = topElem.getAttribute("granularity_for_subq_"+i);
       	  int sbqn = Misc.getParamAsInt(sbq);
       	  if (Misc.isUndef(sbqn)) {
       		  break;
       	  }
       	  if (entry.subQueryTimeGran == null) 
       		  entry.subQueryTimeGran = new ArrayList<Integer>();
       	  entry.subQueryTimeGran.add(sbqn);
       }
  		for (int i=entry.subQueryTimeGran == null ? 0 : entry.subQueryTimeGran.size(), is = entry.subQueryDrivers == null ? 0 : entry.subQueryDrivers.size();i<is;i++) {
  		  if (entry.subQueryTimeGran == null) 
     		  entry.subQueryTimeGran = new ArrayList<Integer>();
  		  entry.subQueryTimeGran.add(-1);
  		}
  		
  		
  		for (int i=0;true;i++) {
     	  String sbq = topElem.getAttribute("delta_for_subq_"+i);
     	  int sbqn = Misc.getParamAsInt(sbq);
     	  if (Misc.isUndef(sbqn)) {
     		  break;
     	  }
     	  if (entry.subQueryRelativeTimeDelta == null) 
     		  entry.subQueryRelativeTimeDelta = new ArrayList<Integer>();
     	  entry.subQueryRelativeTimeDelta.add(sbqn);
  		}
		for (int i=entry.subQueryRelativeTimeDelta == null ? 0 : entry.subQueryRelativeTimeDelta.size(), is = entry.subQueryDrivers == null ? 0 : entry.subQueryDrivers.size();i<is;i++) {
		  if (entry.subQueryRelativeTimeDelta == null) 
   		  entry.subQueryRelativeTimeDelta = new ArrayList<Integer>();
		  entry.subQueryRelativeTimeDelta.add(0);
		}
  		entry.m_createURLLocTracker = Misc.getParamAsString(topElem.getAttribute("create_page"), entry.m_createURLLocTracker);
  		entry.m_createButtonNameLocTracker = Misc.getParamAsString(topElem.getAttribute("create_button_name"), entry.m_createButtonNameLocTracker);
  		entry.m_manageButtonNameLocTracker = Misc.getParamAsString(topElem.getAttribute("manage_button_name"), entry.m_manageButtonNameLocTracker);
  		/* Tanuj-  Query TimeOut */
  		entry.m_queryTimeOut = Misc.getParamAsInt(topElem.getAttribute("set_query_timeout"), entry.m_queryTimeOut);
  		/*  Query TimeOut */
  		entry.m_createButtonAccessControl = Misc.getParamAsString(topElem.getAttribute("create_access_control"), entry.m_createButtonAccessControl);
  		entry.m_addnlSearchButton = Misc.getParamAsString(topElem.getAttribute("addnl_search"));
  		entry.m_addnlSearchButtonRetainDim = Misc.getParamAsString(topElem.getAttribute("addnl_search_retain_dim"));

      entry.m_commonLinkParamTemplate = Misc.getParamAsString(topElem.getAttribute("common_link_template"));
      entry.m_limitByColNames = Misc.getParamAsString(topElem.getAttribute("limit_by"));
      entry.m_limitAcrossColNames = Misc.getParamAsString(topElem.getAttribute("limit_across"));
      entry.m_orderByAttribId = Misc.getParamAsString(topElem.getAttribute("order_by_attrib"));
      entry.m_limitOrderByColNames = Misc.getParamAsString(topElem.getAttribute("limit_order"));
      entry.m_templateId = topElem.getAttribute("create_template_id");
      entry.m_customField1 = Misc.getParamAsString(topElem.getAttribute("custom_field1"), null);
      entry.m_customField2 = Misc.getParamAsString(topElem.getAttribute("custom_field2"), null);
      entry.m_customField3 = Misc.getParamAsString(topElem.getAttribute("custom_field3"), null);
      entry.m_doZeroRow = "1".equals(topElem.getAttribute("do_zero_row"));
      entry.m_hackTrackDriveTimeTableJoinLoggedData = "1".equals(topElem.getAttribute("hack_track_driver_timetable_join_logged_data"));
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
      entry.m_preventGrouping = "1".equals(topElem.getAttribute("prevent_group"));
      
      //entry.m_hackDoGetPrjListFromOrderToo = "1".equals(topElem.getAttribute("get_prj_list_from_order_access"));
      
      String temp = Misc.getParamAsString(topElem.getAttribute("priv_to_check"), null);
      if (temp != null)
         entry.m_privTagToCheck = Misc.convertValToArray(temp);


   }
   public void postProcess(Element topElem, boolean hackNoProcessOnSearchCriteria) {//this function must be reentrant - must be called whenever FrontPageInfoList changes
      HashMap<String, Integer> namesToIndexLookup = new HashMap<String, Integer>();
      m_colIndexLookup = namesToIndexLookup;
      if (m_colIndexUsingExpr != null)
    	  m_colIndexUsingExpr.clear();
      for (int i=0,is= hackNoProcessOnSearchCriteria || m_frontSearchCriteria == null ?0 : m_frontSearchCriteria.size(); i<is;i++) {
    	  ArrayList dimConfList = (ArrayList)m_frontSearchCriteria.get(i);
    	  for (int j=0,js=dimConfList.size();j<js;j++) {
    		  DimConfigInfo searchItem = (DimConfigInfo) dimConfList.get(j);
    		  boolean toBeMand = searchItem.m_isMandatory;
    		  DimInfo searchDimInfo = searchItem.m_dimCalc != null && searchItem.m_dimCalc.m_dimInfo != null ? searchItem.m_dimCalc.m_dimInfo : null;
    		  if (searchDimInfo == null)
    			  continue;
    		  if (!toBeMand) {
    			  toBeMand = (searchDimInfo.m_colMap != null && "trick".equals(searchDimInfo.m_colMap.table)) || searchDimInfo.m_id == 20053 || searchDimInfo.m_id == 20051 || searchDimInfo.m_subsetOf == 20052 ||  searchDimInfo.m_id == 20023 || searchDimInfo.m_id == 20034 || searchDimInfo.m_descDataDimId == 123 ||  searchDimInfo.m_id == 20035 ||  searchDimInfo.m_id == 20036; //these if asked are mand   
    		  }
    		  if (!toBeMand) {
    			  boolean found = false;
	    		  for (int k=0,ks=m_frontInfoList == null ? 0 : m_frontInfoList.size();k<ks;k++) {
	    			  DimConfigInfo frontItem = (DimConfigInfo)dimConfList.get(k);
	    			  DimInfo frontDimInfo = frontItem.m_dimCalc == null ? null : frontItem.m_dimCalc.m_dimInfo;
	    			  if (frontDimInfo != null && frontDimInfo.m_descDataDimId == searchDimInfo.m_descDataDimId) {
	    				  found = true;
	    				  break;
	    			  }
	    		  }
	    		  if (!found)
	    			  toBeMand = true;	    		  
    		  }
    		  searchItem.m_isMandatory = toBeMand;
    	  }//each col of search
      }//each row of search
      for (int i=0,is=m_frontInfoList == null ? 0:m_frontInfoList.size();i<is;i++) {
    	  DimConfigInfo dimConfig = (DimConfigInfo) m_frontInfoList.get(i);
    	  if (dimConfig == null)
    		  continue;
    	  
    	  if (dimConfig.m_internalName != null && dimConfig.m_internalName.length() != 0)
    		  namesToIndexLookup.put(dimConfig.m_internalName, i);
    	  if (dimConfig.m_columnName != null && dimConfig.m_columnName.length() != 0)
    		  namesToIndexLookup.put(dimConfig.m_columnName, i);
    	  
    	  String dimBasedName = dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null ? "d"+Integer.toString(dimConfig.m_dimCalc.m_dimInfo.m_id) : null;
    	  if (dimBasedName != null) {
             if (!namesToIndexLookup.containsKey(dimBasedName)) {
            	 namesToIndexLookup.put(dimBasedName, i);
             }
             if (dimConfig.m_internalName == null || dimConfig.m_internalName.length() == 0)
            	 dimConfig.m_internalName = dimBasedName;
             if (dimConfig.m_columnName == null || dimConfig.m_columnName.length() == 0)
            	 dimConfig.m_columnName = dimBasedName;
    	  }
    	  
      }
      if (this.projectNameLookupFieldName != null) {
    	  Integer tv = this.m_colIndexLookup.get(this.projectNameLookupFieldName);
    	  this.projectNameLookupFieldIndex = tv == null ? -1 : tv.intValue();
      }
      for (int i=0,is=m_frontInfoList == null ? 0:m_frontInfoList.size();i<is;i++) {
    	  DimConfigInfo dimConfig = (DimConfigInfo) m_frontInfoList.get(i);
    	  if (dimConfig != null)
    		  dimConfig.m_color_code_by_index = -1;
    	  if (dimConfig == null || dimConfig.m_color_code_by == null || dimConfig.m_color_code_by.length() == 0)
    		  continue;
  	  	  String s = dimConfig.m_color_code_by; 
		  Integer origColorIndex  = this.m_colIndexLookup.get(s);
		  if (origColorIndex == null) {
				//due to a naming bug somewhere uiColumnName may be dimId
			  int tempId = Misc.getParamAsInt(s);
			  if (!Misc.isUndef(tempId)) {
				 origColorIndex = this.m_colIndexLookup.get("d"+tempId);
			  }
		  }
		  if (origColorIndex != null) {
			dimConfig.m_color_code_by_index = origColorIndex.intValue();
		  }
      }
      DimConfigInfo lastDimConfWithColSpanLabel = null;
      for (int i=0,is=m_frontInfoList == null ? 0:m_frontInfoList.size();i<is;i++) {
    	  DimConfigInfo dimConfig = (DimConfigInfo) m_frontInfoList.get(i);
    	  if (dimConfig == null) 
    		  continue;
    	  //identify the span needed for colSpanLabelling
    	  if (dimConfig.m_frontPageColSpanLabel != null && dimConfig.m_frontPageColSpanLabel.length() != 0) {
    		  if (lastDimConfWithColSpanLabel != null && !dimConfig.m_frontPageColSpanLabel.equals(lastDimConfWithColSpanLabel.m_frontPageColSpanLabel))
    			  lastDimConfWithColSpanLabel = null;
    		  if (lastDimConfWithColSpanLabel == null) {
    			  lastDimConfWithColSpanLabel = dimConfig;
    			  lastDimConfWithColSpanLabel.m_dataSpan = 1;
    		  }
    		  else {
    			  lastDimConfWithColSpanLabel.m_dataSpan++;
    		  }
    	  }
    	  else {
    		  lastDimConfWithColSpanLabel = null;
    	  }
    	  //identify the expr
    	  if (dimConfig.m_calcExprStr != null && dimConfig.m_calcExprStr.length() != 0) {
    		  int firstLeftParanthesis = dimConfig.m_calcExprStr.indexOf('(');
    		  
    		  String func = firstLeftParanthesis >= 0 ? dimConfig.m_calcExprStr.substring(0, firstLeftParanthesis) : dimConfig.m_calcExprStr;
    		  func = func.trim();
    		  DimConfigInfo.ExprHelper.CalcFunctionEnum funcEnum = DimConfigInfo.ExprHelper.CalcFunctionEnum.getFuncCode(func);
    		  String columnCsvStr = firstLeftParanthesis >= 0 ? dimConfig.m_calcExprStr.substring(firstLeftParanthesis+1, dimConfig.m_calcExprStr.length()-1):null;
    		  String[] column = Misc.convertValToArray(columnCsvStr);
    		  int cummOn = column != null && column.length != 0 ? namesToIndexLookup.get(column[0]) : -1;
    		  ArrayList columnIndex = null;
    		  if (cummOn >= 0) {
    			  columnIndex = new ArrayList<Integer>();
    			  for (int j=1,js=column.length;j<js;j++) {
    				  int index = namesToIndexLookup.get(column[j]);
    				  if (index >= 0)
    					  columnIndex.add(index);
    			  }
    			 dimConfig.m_expr = new DimConfigInfo.ExprHelper();
    			 dimConfig.m_expr.m_calcFunction = funcEnum;
    			 dimConfig.m_expr.m_cummColIndex = cummOn;
    			 dimConfig.m_expr.m_resetColIndex = columnIndex;
    			 if (m_colIndexUsingExpr == null) {
    				 m_colIndexUsingExpr = new ArrayList<Integer>();
    			 }
    			 m_colIndexUsingExpr.add(i);
    		  }
    	  }
    	  //identify the colspanning for grouped cols
    	  if (dimConfig.m_linkControlString != null && dimConfig.m_linkControlString.length() != 0) {
	    	 String fromTopLevel = topElem == null ? null : topElem.getAttribute(dimConfig.m_linkControlString);
	    	 if (fromTopLevel != null && fromTopLevel.length() != 0)
	    		 dimConfig.m_linkControlString = fromTopLevel;
	    	  dimConfig.m_linkHelper = getLinkInfo(dimConfig.m_linkControlString, namesToIndexLookup, false);
    	  }
      }//end of for each frontPageInfoList
      if (m_commonLinkParamTemplate != null && m_commonLinkParamTemplate.length() != 0) {
    	  m_commonLinkHelper = getLinkInfo(m_commonLinkParamTemplate, namesToIndexLookup, true);
      }
      if (this.m_limitByColNames != null && this.m_limitByColNames.length() != 0) {
    	  ArrayList<String> temp = new ArrayList<String>();
    	  Misc.convertValToStrVector(this.m_limitByColNames, temp);
    	  this.m_limitColIndices = new ArrayList<Integer>();
    	  for (String str : temp) {
    		  Integer idx = namesToIndexLookup.get(str);
    		  if (idx != null) {
    			  m_limitColIndices.add(idx.intValue());
    		  }
    	  }
      }
      if (this.m_limitAcrossColNames != null && this.m_limitAcrossColNames.length() != 0) {
    	  ArrayList<String> temp = new ArrayList<String>();
    	  Misc.convertValToStrVector(this.m_limitAcrossColNames, temp);
    	  this.m_limitAcrossColIndices = new ArrayList<Integer>();
    	  for (String str : temp) {
    		  Integer idx = namesToIndexLookup.get(str);
    		  if (idx != null) {
    			  m_limitAcrossColIndices.add(idx.intValue());
    		  }
    	  }
      }
      if (this.m_limitOrderByColNames != null && this.m_limitOrderByColNames.length() != 0) {
    	  ArrayList<String> temp = new ArrayList<String>();
    	  Misc.convertValToStrVector(this.m_limitOrderByColNames, temp);
    	  this.m_limitOrderByColIndices = new ArrayList<Integer>();
    	  for (String str : temp) {
    		  boolean doDesc = str.startsWith("-");
    		  if (doDesc)
    			  str = str.substring(1);
    		  Integer idx = namesToIndexLookup.get(str);
    		  if (idx != null) {
    			  int v = idx.intValue();
    			  if (doDesc)
    				  v = -1000+v;
    			  m_limitOrderByColIndices.add(v);
    		  }
    	  }
      }
      if (this.m_orderByAttribId != null && this.m_orderByAttribId.length() != 0) {
    	  ArrayList<Integer> orderList = new ArrayList<Integer>();
    	  Misc.convertValToVector(this.m_orderByAttribId, orderList);
    	  for (int i=0,is=orderList.size();i<is;i++) {
    		  int dimid = orderList.get(i);
    		  int check = dimid;
    		  if (dimid < 0) {
    			  check = -1*dimid;
    		  }
    		  int idx = this.getColIndexByName("d"+check);
    		  if (idx < 0) {
    			  orderList.remove(i);
    			  is--;
    			  i--;
    		  }
    		  else {
    			  idx++;
    			  if (dimid < 0)
    				  idx *= -1;
    			  orderList.set(i, idx);
    		  }
    	  }
    	  if (orderList != null && orderList.size() != 0)
    		  this.m_orderIds = orderList;
    	  else
    		  this.m_orderIds = null;
      }
      else {
    	  this.m_orderIds = null;
      }
   }//end of func
      
   public static DimConfigInfo.LinkHelper getLinkInfo(String linkControlString, HashMap<String, Integer> namesToIndexLookup, boolean onlyParamPart) {
	   if (linkControlString == null || linkControlString.length() == 0)
		   return null;
	  DimConfigInfo.LinkHelper linkHelper = new DimConfigInfo.LinkHelper();
 	  int qmarkPart = onlyParamPart ? -1 : linkControlString.indexOf('?');
 	  if (qmarkPart <= -1 && !onlyParamPart)
 		  linkHelper.m_pagePart = linkControlString;
 	  else {
 		  if (!onlyParamPart)
 			  linkHelper.m_pagePart = linkControlString.substring(0,qmarkPart);
 		  String paramPart = linkControlString.substring(qmarkPart+1);    		  
 		  StringTokenizer strtok = new StringTokenizer(paramPart,"&=",false);
           while(strtok.hasMoreTokens()) {
                try {
                	  boolean dynamicVal = false;
	                  String firstToken = strtok.nextToken();
	                  if (strtok.hasMoreTokens()) {
	                     String val = strtok.nextToken();
	                     if (val == null || val.length() == 0 || firstToken == null || firstToken.length() == 0)
	                    	 continue;
	                     else {
	                    	 String paramName = firstToken;
	                    	 boolean doReplaceWithTop = false;
	                    	 if (firstToken.startsWith("@")) {
	                    		 paramName = firstToken.substring(1);
	                    		 doReplaceWithTop = true;
	                    	 }
	                    	 int indexInFrontInfoList = -1;	   	                    	 
	                    	 String colName = val;
	                    	 if (val.startsWith("$") || val.startsWith("#")) {
	                    		 colName = val.substring(1, val.length()-(val.startsWith("#") ? 1 : 0));
	                    		 Integer indexInteger = namesToIndexLookup.get(colName);
	                    		 if (indexInteger != null) {
	                    			 indexInFrontInfoList = indexInteger.intValue();
//	                    			 continue;
	                    		 }
	                    		 dynamicVal = true;
	                    	 }
	                    	 if (indexInFrontInfoList < 0 && !doReplaceWithTop ) { //goes to fixedPart
	                    		 if(dynamicVal)
		                    		 continue;
	                    		 if (linkHelper.m_fixedParamPart == null) {
	                    			 linkHelper.m_fixedParamPart = "";
	                    		 }
	                    		 else {
	                    			 linkHelper.m_fixedParamPart += "&";
	                    		 }
	                    		 linkHelper.m_fixedParamPart += paramName+"="+colName;       	                    		 
	                    	 }
	                    	 else {//goes to dynPart
	                    		 linkHelper.m_paramName.add(new MiscInner.PairStrBool(paramName, doReplaceWithTop));
	                    		 linkHelper.m_paramValue.add(new MiscInner.PairIntStr(indexInFrontInfoList, colName));	                    		 
	                    	 }
	                     }//end of valid val for paramName, paramVal
	                  }//had more tokens
                }//end of try
                catch (Exception e) {
             	   //just ignore ... some formatting error
                }
            }//end of reading thru token
 	   }//need to worry about param=param value part rather than fixed
 	  return linkHelper;
   }
   
   public String getDefaultSearchCriteria(int dimId) {
	   for (int i=0,is=m_frontSearchCriteria.size();i<is;i++) {
		   ArrayList colInfoList = (ArrayList) m_frontSearchCriteria.get(i);
		   for (int j=0,js=colInfoList.size();j<js;j++) {
			   DimConfigInfo dimConfig = (DimConfigInfo) colInfoList.get(j);
			   if (dimConfig != null && dimConfig.m_dimCalc != null && dimConfig.m_dimCalc.m_dimInfo != null && dimConfig.m_dimCalc.m_dimInfo.m_id == dimId)
				   return dimConfig.m_default;
		   }
	   }
	   return null;
	
   }
   public static String removeDynParams(String pageURL) {
		String retval = pageURL; 
		if (pageURL == null || !(pageURL.length() > 0))
            return retval;
		  String page = pageURL;
		  String fixedStr = "";
		  String url = "";
	 	  int qmarkPart = page.indexOf('?');
	 	  if (qmarkPart > -1){
	 		  url = page.substring(0,qmarkPart);
	 		  String paramPart = page.substring(qmarkPart+1);    		  
	 		  StringTokenizer strtok = new StringTokenizer(paramPart,"&=",false);
	           while(strtok.hasMoreTokens()) {
	                try {
	                	  boolean dynamicVal = false;
		                  String firstToken = strtok.nextToken();
		                  if (strtok.hasMoreTokens()) {
		                     String val = strtok.nextToken();
		                     if (val == null || val.length() == 0 || firstToken == null || firstToken.length() == 0)
		                    	 continue;
		                     else {
		                    	 String paramName = firstToken;
		                    	 if (firstToken.startsWith("@")) {
		                    		 paramName = firstToken.substring(1);
		                    	 }
		                    	 String colName = val;
		                    	 if (val.startsWith("$") || val.startsWith("#")) {
		                    		continue;
		                    	 }
		                    		 if (fixedStr == null || fixedStr == "") {
		                    			 fixedStr = "";
		                    		 }
		                    		 else {
		                    			 fixedStr += "&";
		                    		 }
		                    		 fixedStr += paramName+"="+colName;       	                    		 
		                    	 }
		                     }
		                  }
	                catch (Exception e) {
	             	  e.printStackTrace();
	                }
	            }
	 	   }
	 	  retval = url + "?" + ((fixedStr != null && fixedStr.length() > 0) ?  fixedStr : "");
	return retval; 	
	}
}
