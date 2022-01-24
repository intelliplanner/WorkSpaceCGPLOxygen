// Copyright (c) 2000 IntelliPlanner Software Systems, Inc.
package com.ipssi.gen.utils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;



   public class DimConfigInfo implements Cloneable,Serializable {
      /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class Pair implements Cloneable, Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
         public int m_rowIndex=-1;
         public int m_colIndex=-1;
         public Pair (int row, int col) { m_rowIndex = row; m_colIndex = col;}
         public Object clone() throws CloneNotSupportedException {
            return super.clone();
         }
      }
      public static class FixedHelper implements Cloneable, Serializable {
          /**
  		 * 
  		 */
  		private static final long serialVersionUID = 1L;
         public int m_idThenIndex = Misc.getUndefInt();
         public ArrayList m_val = new ArrayList(); //of integer
         public Object clone() throws CloneNotSupportedException {
             return super.clone();
         }
      }
      public static class InnerMenuInfo implements Cloneable, Serializable {
          /**
  		 * 
  		 */
  		private static final long serialVersionUID = 1L;
  		 public int m_id = 0; //0 => as obtained from menu page
         public String m_menuTag = null;
         public String m_targetTag = null;
         public String m_label = null;
         public String m_page = null;
         public String m_paramTemplate = null;
         public boolean m_ofSameLevel = false;
         public boolean m_doPopup = false;
         public boolean m_suspendTimer = false;
         public String m_javascriptOptional = null;
         public Object clone() throws CloneNotSupportedException {
            return super.clone();
         }
         public static InnerMenuInfo read(Element e) {
             if (e == null)
                 return null;
             InnerMenuInfo retval = new InnerMenuInfo();
             retval.m_id = Misc.getParamAsInt(e.getAttribute("id"),true ? Misc.getUndefInt() : 0);//for LocTracker want it to be undef, while capex had 0 ... not sure why
             retval.m_menuTag = Misc.getParamAsString(e.getAttribute("tag"),null);
             retval.m_targetTag = Misc.getParamAsString(e.getAttribute("target_tag"), retval.m_menuTag);
             retval.m_label = Misc.getParamAsString(e.getAttribute("label"), null);
             retval.m_page = Misc.getParamAsString(e.getAttribute("page"), null);
             retval.m_paramTemplate = Misc.getParamAsString(e.getAttribute("param_template"), null);
             retval.m_ofSameLevel = "1".equals(e.getAttribute("same_level"));
             retval.m_doPopup = "1".equals(e.getAttribute("do_popup"));
             retval.m_suspendTimer = "1".equals(e.getAttribute("suspend_timer"));
             retval.m_javascriptOptional = Misc.getParamAsString(e.getAttribute("pre_handler"), null);
             return retval;
         }
		
      }
      public String m_lookHelp1 = null;
      public String m_lookHelp2 = null;
      
      public String m_helpTag = null;
      public String m_name;
      public String m_accessPriv = null;
      public String m_writePriv = null;
      //public int m_id;
      public DimCalc m_dimCalc = null;
      public ArrayList m_addnlDimInfoNew = null; //want to change to ArrayList of DimCalc rather than dimInfo
//      public DimInfo m_dimInfo;
      
      public int m_textPrintCharWidth = 10;
      public int m_textPrintTruncateToWidth = 0;
      public int m_width;
      public int m_labelWidth = Misc.getUndefInt();
      public int m_height;
      public String m_disp;
      public String m_default;
      public String m_tip = null;
      public int m_multiCols = 1;
      public int m_dataSpan = 1;
      public int m_rowSpan = 1;
      public String m_customField1 = null;
      public String m_customField2 = null;
      public String m_customField3 = null;
      public boolean m_forceCheckRadio = false;
      public boolean m_addRow = false;
      public boolean m_readOnly = false;
      public boolean m_nowrap = true;
      public boolean m_labelNowrap = false;
      public boolean m_hidden = false;
      public boolean m_doEdit = false;
      public boolean m_initZero = false; //if true and editable then will be b;ank
      public boolean m_forceGetValueDefaultValueFromDB = false;
      public boolean m_canTot = false;
      public boolean m_doautoCompleteAsClass = true;
      public boolean m_doExactSearch = false;
      public String m_columnName = null; //the name by which we will refer to in m_calcBy
      public String m_calcBy = null;
      public boolean m_orderBy = false;
      public boolean m_skip_groupby = false;
      public boolean m_orderByTag = false; //asec/desc
      public boolean m_canEndBeyondCurrent = false;
      public int m_helperType = 0; // 0 => regular, 1 => hidden and to be included if somebody needs it for color coding purposes
      public int m_calcOp = 0; // 0 = sum ... then only one supported
      public ArrayList m_calcByMap = null; //value = DimConfiInfo.Pair
      public boolean m_isSelect = false; //dual use: the column is check box, the id is project_id
                                         //when doing project compare, will show the property as match box
      public ArrayList m_fixedForSel = null; //ArrayList of FixedHelper
      public boolean m_multiSelect = true;
      public boolean m_isMandatory = false; //for PrjTemplateHelpers tells if col is must, for frontPageInfo,: in frontSearchCritieria list, is set to true  when - front page info first read, any thing in search
      																  //criteria that is not on front info list or marked as must or related to 123/20023 or 20034 or 20035 or 20036
      public int m_mandatoryGroup = Misc.getUndefInt();
      public String m_oneOfAllGrouping = null; //all entries in one of the group needs to be filled in .. csv of list of groups of which part
      public int m_uniqueGroup = Misc.getUndefInt();
      public boolean m_isCached = false;
//      public String m_helpTag = null;
      public String m_prefixBeforeEntry = null;
      public String m_suffixAfterEntry = null;
      //public boolean m_makeReadOnlyPost = false; //to be deprecated
      public String m_calcExprStr = null;  
      public boolean m_isRuleType = false;
      //to handle onchange events
      public String m_onChange_handler = null; //function name in java script
      public String m_onChange_url = null;  // url to update select element
      public String m_onChange_add_params = null; // veriable used in onchange function
      public int m_splitByLovDim = Misc.getUndefInt();
      public boolean m_splitTot = true;
      public boolean m_orBlock = false;
      
      public boolean m_isDisplay = false;
      public boolean m_isAutocomplete = false;
      public boolean m_isRadio = false;
      public boolean m_searchExact = false;
      //for button in input template
      public String m_buttonAction=null;
      public String m_buttonAddnlParams= null;
      public String m_buttonOnCreate = null;
      
      //Usage of below 4-5 fields
      //doPivot - values for these will be show in table header rather than cols. idea is to have all cross join .. but currently impleted for 1 pivot only
      //doPivotMeasure - Under the columns for each value of doPivot nested row of measures having this flag when pivoting
      //ytdScopePlus1 - when getting multiple ytd (e.g mtd, ytd, today ... we will replace query by case when xx between start and end) so that we dont end up doing multiple pass thru same tables
      //   note here query is executed is db
      //doJavaCumm - may not be needed ... idea is that if data is obtained over multiple periods but we want to show cumm then we cumm in java
      //cummDontResetOver tells effectively 'the time period' col on which we want to cummulate and then reset.
      //   it is possible to do doJavaCumm by judiciously using period_table and ytdScopePlus1
      //re endScopeForYtdPlus1 : consider we want to show ytd for a column but only till beg of current month (or only till beg of current day)
      //  if above-1 == -1 then till latest, else beg of period of this scope for the time passed     
      private byte doPivot = 0; //0 = no, 1 => yes with whatever values, 2 => expand as much as possible - if lov - all vals, if date then min to max date
      private byte ytdScopePlus1 = 0; //need to do ytd like stuff for scope = m_ytdScopePlus1 -1 //since 0 is default for deserializedNewField so this hack
      private byte endScopeForYtdPlus1 = 0;//since 0 is default in deserialization ... hence plus 1
      private boolean doPivotMeasure = false;
      public boolean doTotOnlyInPivotForMeasure = false;
      private boolean cummDontResetOver = false;
      private boolean doJavaCumm = false; //may not be needed ... see notes above
      private boolean doTotalAcrossColInPivot = false;
      private boolean doTotalAcrossRow = false;
      public boolean innerMandatory = false;
      public boolean useTopLevelInInpTemplate = false;
      public boolean putValAsHiddenTooInInpTemplate = false;
      public String outerQueryAggOp = null;
      public String outerCalcExpr = null;
      public ArrayList<Integer> subQueryGroup = null;
      public ArrayList<Integer> excludeFilterFromSubQ = null;
      public ArrayList<Integer> includeFilterFromSubQ = null;
      public ArrayList<String> doPivotByIfMulti = null;//NOT YET IMPLEMENTED
      public ArrayList<com.ipssi.gen.utils.Pair<Integer, String>> subQueryJoinColName = null;
      public int minVal = Misc.getUndefInt();
      public int maxVal = Misc.getUndefInt();
      public String allowedPattern = null;
      public int m_min_sugg_length = Misc.getUndefInt();
      public int getDoPivot() {
			return doPivot;
		}
		public void setDoPivot(int doPivot) {
			this.doPivot = (byte) doPivot;
		}
		public int getYtdScope() {
			return ytdScopePlus1-1;
		}
		public int getEndScopeForYtd() {
			return endScopeForYtdPlus1-1;
		}
		public void setEndScopeForYtdPlus1(int endScopeForYtdPlus1) {
			this.endScopeForYtdPlus1 = (byte) endScopeForYtdPlus1;
		}
		public void setYtdScopePlus1(int ytdScopePlus1) {
			this.ytdScopePlus1 = (byte) ytdScopePlus1;
		}
      public static class ExprHelper  implements Cloneable, Serializable {
    	  private static final long serialVersionUID = 1L;
    	  public enum CalcFunctionEnum   implements Cloneable, Serializable {        	  
    		  CUMM;
    		  public static CalcFunctionEnum getFuncCode(String str) {
    			  return CUMM;
    		  }
    		  private static final long serialVersionUID = 1L;
    	  }	  
    	  public  CalcFunctionEnum m_calcFunction = CalcFunctionEnum.CUMM;
    	  public int m_cummColIndex = -1;
    	  public ArrayList<Integer> m_resetColIndex = null;
      }
      public ExprHelper m_expr = null;
      public String m_paramName = null; 
      public String m_frontPageColSpanLabel = null; //if a given col is beginning of nested col,  then set dataSpan to amt of nest included col and set this label to label of text 
      public String m_linkControlString = null;//will be of form of xyz.jsp?param=$column_name$&@param=$column_name
                                                                       //when printing link @ to be replaced with topPageContext, $column_name to be replaced with value of that column in that row (if that is not null)
      public static class LinkHelper implements Cloneable, Serializable {
    	  private static final long serialVersionUID = 1L;
    	  public String m_pagePart = null;
    	  public String m_fixedParamPart = null;
    	  public ArrayList<MiscInner.PairStrBool> m_paramName = new ArrayList<MiscInner.PairStrBool> (); //Str= paramName, bool= if paramNames needs to be prepended by topPageContext
    	  public ArrayList<MiscInner.PairIntStr> m_paramValue = new ArrayList<MiscInner.PairIntStr>();//int=colIndex in frontInfoList, str = value of param 
      }
      public String toString() {
    	  return this.m_name+","+this.m_columnName+","+this.m_dimCalc;
      }
      public LinkHelper m_linkHelper = null; //this will be populated after FrontPageInfo has been constructed
      public String m_orderByInFrontPage = null; //if non-null will be added to order by (or group by if doing mySql and the latter has with rollup
      public boolean m_doRollupTotal = false;
      public boolean m_setAsPermInSearchBox = false; //overloaded ... also used in InputTemplate nested cols to find the column that is unique for the parent object      
      public boolean m_rememberUserSel = false; //dont know what is this for ... may be for CapEx
      
      // added to generalize Granularity & Aggregate function  07102009
      public boolean m_granularity = false;
      public boolean m_aggregate = false;
      public String m_subtype = null;
      public double m_scale;
      public int m_decimalPrecision;
      public int m_minDecimal;
      public int m_p_val;
      // dashboard url
      public String m_url = null;
      public String m_xml = null;
      public boolean m_show_modal_dialog = false;
      public String m_dialog_width = null;
      public String m_dialog_height = null;
      public boolean m_do_submit = true;
      // Numeric filter related
      public boolean m_numeric_filter = false;
      //Image related
      public boolean m_use_image = false;
      public String m_image_file = null;
      // color coding related
      public boolean m_color_code = false;
      public String m_color_code_by = null;
      public int m_color_code_by_index = -1;
      public int m_param1;
      public int m_param2;
      public int m_param3;
      public int m_param1_color;
      public int m_param2_color;
      public int m_param3_color;
      public int m_color_gran;
      public int m_on_change_id;
      public String m_customBlock = null;
     
      public static final int G_READHIDE_NEVER = 0;
      public static final int G_READHIDE_PRECREATE = 1;
      public static final int G_READHIDE_POSTCREATE = 2;      
      public static final int G_READHIDE_ALWAYS = 3;
      public static final int G_READHIDE_POSTCREATE_BUT_NOT_IN_OVERRIDE = 4;
      public static final int G_READ_NODELETE = 5;
      
      public int m_hiddenSpecialControl = G_READHIDE_NEVER;
      public int m_readSpecialControl = G_READHIDE_NEVER;
      public boolean m_multiRowOldReadOnly = false; 
      public ArrayList m_multiRowSubColList = null; //of DimConfigInfo
      public DimInfo m_ifClassifyIsTypeOfDim = null; //hack ... in multiple sub cols, currently cols are classify1 etc. 
                                                 //where if these are LOV then can't be identified
      public ArrayList m_innerMenuList = null; //of InnerMenuInfo ... if non-null then null
      public boolean m_forInterleavedSearchOnly = false;      
      public String m_targetPage = null;//"project_detail.jsp";
      public String m_objectIdParam = "project_id";
      public String m_multiColAddRowScript = null;
      public String m_multiColValidateScript = null; //not used ... instead will change validation script
      public int m_refUnit = Misc.getUndefInt(); //DO NOT USE ... currently while printing the code supports but when reading and saving this is not supported. See populateCallStmt 
      public String m_refMasterBlockInPFM = null;
      public String m_refBlockInPFM = null;

	    public int m_dataWidth;
      public String m_labelStyleClass = null;
      public String m_valStyleClass = null;
      public boolean m_forDateApplyGreater = false;   
      public String m_defaultOperator = null;
      public String m_rightOperand = null;
      public boolean m_showOperator = true;
      public String m_customPropEditHint = null;
      public String m_internalName = null;
      public boolean m_askUser = false;
      public boolean m_skip_link_params;
      public String m_set_val = null;
      public ArrayList<ArrayList<DimConfigInfo>> m_nestedCols = null; //although 2 dim only 1 entry in the first (i.e single row)
      public Object clone() throws CloneNotSupportedException { //only 
         DimConfigInfo retval = (DimConfigInfo) super.clone();
         retval.m_dimCalc = (DimCalc) m_dimCalc.clone();
         if (m_addnlDimInfoNew != null) {
            retval.m_addnlDimInfoNew = (ArrayList) m_addnlDimInfoNew.clone();
            for (int i=0,is=retval.m_addnlDimInfoNew.size();i<is;i++) {
               DimCalc currVal = (DimCalc) retval.m_addnlDimInfoNew.get(i);
               if (currVal != null)
                 retval.m_addnlDimInfoNew.set(i, currVal.clone());
            }
         }
         if (m_calcByMap != null) {
            retval.m_calcByMap = (ArrayList) m_calcByMap.clone();
            for (int i=0,is=retval.m_calcByMap.size();i<is;i++) {
               DimConfigInfo.Pair currVal = (DimConfigInfo.Pair) retval.m_calcByMap.get(i);
               if (currVal != null)
                 retval.m_calcByMap.set(i, currVal.clone());
            }
         }
         if (m_fixedForSel != null) {
            retval.m_fixedForSel = (ArrayList) m_fixedForSel.clone();
            for (int i=0,is=retval.m_fixedForSel.size();i<is;i++) {
               FixedHelper currVal = (FixedHelper) retval.m_fixedForSel.get(i);
               if (currVal != null)
                 retval.m_fixedForSel.set(i, currVal.clone());
            }
         }
         if (m_nestedCols != null) {
        	 retval.m_nestedCols = new ArrayList<ArrayList<DimConfigInfo>>();
        	 for (int i=0,is = m_nestedCols.size(); i<is;i++) {
        		 ArrayList<DimConfigInfo> lnlist = new ArrayList<DimConfigInfo>();
        		 ArrayList<DimConfigInfo> rnlist = m_nestedCols.get(i);
        		 retval.m_nestedCols.add(lnlist);
        		 for (int j=0,js=rnlist.size(); j<js; j++) {
        			 lnlist.add((DimConfigInfo)(rnlist.get(j).clone()));
        		 }
        	 }
         }
         if (m_multiRowSubColList != null) {
            retval.m_multiRowSubColList = (ArrayList) m_multiRowSubColList.clone();
            for (int i=0,is=retval.m_multiRowSubColList.size();i<is;i++) {
               DimConfigInfo currVal = (DimConfigInfo) retval.m_multiRowSubColList.get(i);
               if (currVal != null)
                 retval.m_multiRowSubColList.set(i, currVal.clone());
            }
         }
         if (m_innerMenuList != null) {
            retval.m_innerMenuList = (ArrayList) m_innerMenuList.clone();
            for (int i=0,is=retval.m_innerMenuList.size();i<is;i++) {
               InnerMenuInfo currVal = (InnerMenuInfo) retval.m_innerMenuList.get(i);
               if (currVal != null)
                 retval.m_innerMenuList.set(i, currVal.clone());
            }
         }
         return retval;
      }
      public DimConfigInfo() {
         //m_id = Misc.getUndefInt();
         //m_dimInfo = null;
         m_width = 20;
         m_labelWidth = Misc.getUndefInt();
         m_height = 1;
         m_disp = null;
         ArrayList m_addnlDimInfo = null;
         m_default = null;
		 m_dataWidth = 0;
      }
      
      public DimConfigInfo(String columnName) {
         //m_id = Misc.getUndefInt();
         //m_dimInfo = null;
         m_width = 20;
         m_labelWidth = Misc.getUndefInt();
         m_height = 1;
         m_disp = null;
         ArrayList m_addnlDimInfo = null;
         m_default = null;
         m_columnName = columnName;
		 m_dataWidth = 0;
      }
      public static DimConfigInfo getDimConfigInfo(Element elem, boolean readParamMissingMeansRead) {
          DimConfigInfo dimConf = new DimConfigInfo();
          String idStr = elem.getAttribute("id");

          StringTokenizer strTok = new StringTokenizer(idStr, ",", false);

          boolean first = true;
          int dimId = Misc.getUndefInt();
          while (strTok.hasMoreTokens()) {
             String tok = strTok.nextToken();
             dimId = com.ipssi.gen.utils.Misc.getParamAsInt(tok);

             if (!com.ipssi.gen.utils.Misc.isUndef(dimId)) {

                com.ipssi.gen.utils.DimInfo dimInfo = com.ipssi.gen.utils.DimInfo.getDimInfo(dimId);
                if (dimInfo == null)
                   continue;
                DimCalc dimCalc = null;

                if (first) {
                   first = false;
                   dimCalc = new DimCalc(elem);
                   dimConf.m_dimCalc = dimCalc;
                   dimConf.m_dimCalc.m_dimInfo = dimInfo;
                   //dimConf.m_id = dimId;
                   continue;
                }
                else {
                   dimCalc = new DimCalc(dimInfo, null, null);
                }
                if (dimConf.m_addnlDimInfoNew == null)
                    dimConf.m_addnlDimInfoNew = new ArrayList();
                dimConf.m_addnlDimInfoNew.add(dimCalc);
             }
          } //valid token
          int maxSubQ = -1;
          
          for (int i=0;true;i++) {
        	  String sbq = elem.getAttribute("sub_query_"+i);
        	  int sbqn = Misc.getParamAsInt(sbq);
        	  if (sbqn < 0) {
        		  break;
        	  }
        	  if (dimConf.subQueryGroup == null) 
        		  dimConf.subQueryGroup = new ArrayList<Integer>();
        	  dimConf.subQueryGroup.add(sbqn);
        	  if (maxSubQ < sbqn)
        		  maxSubQ = sbqn;
          }
          //public ArrayList<Integer> excludeFilterFromSubQ = null;
          for (int i=0;true;i++) {
        	  String sbq = elem.getAttribute("exclude_filter_"+i);
        	  int sbqn = Misc.getParamAsInt(sbq);
        	  if (sbqn < 0) {
        		  break;
        	  }
        	  if (dimConf.excludeFilterFromSubQ == null) 
        		  dimConf.excludeFilterFromSubQ = new ArrayList<Integer>();
        	  dimConf.excludeFilterFromSubQ.add(sbqn);
          }
          //public ArrayList<integer> includeFilterFromSubQ = null;
          for (int i=0;true;i++) {
        	  String sbq = elem.getAttribute("include_filter_"+i);
        	  int sbqn = Misc.getParamAsInt(sbq);
        	  if (sbqn < 0) {
        		  break;
        	  }
        	  if (dimConf.includeFilterFromSubQ == null) 
        		  dimConf.includeFilterFromSubQ = new ArrayList<Integer>();
        	  dimConf.includeFilterFromSubQ.add(sbqn);
          }

          for (int i=0;i<=maxSubQ;i++) {
        	  String sbq = elem.getAttribute("join_forsubq_"+i);
        	  if (sbq == null || sbq.length() == 0)
        		  continue;
        	  if (dimConf.subQueryJoinColName == null)
        		  dimConf.subQueryJoinColName = new ArrayList<com.ipssi.gen.utils.Pair<Integer, String>>();
        	  dimConf.subQueryJoinColName.add(new com.ipssi.gen.utils.Pair<Integer, String>(i,sbq));
          }
          dimConf.m_name = MyXMLHelper.getAttribAsString(elem, "label");
          dimConf.m_accessPriv = Misc.getParamAsString(elem.getAttribute("priv_tag"));
          dimConf.m_writePriv = Misc.getParamAsString(elem.getAttribute("write_priv"));
          dimConf.m_helpTag = MyXMLHelper.getAttribAsString(elem, "help_tag");
          dimConf.m_tip = MyXMLHelper.getAttribAsString(elem, "tip");
          dimConf.setMinVal(Misc.getParamAsInt(elem.getAttribute("min_val")));
          dimConf.setMaxVal(Misc.getParamAsInt(elem.getAttribute("max_val")));
          dimConf.setAllowedPattern(Misc.getParamAsString(elem.getAttribute("allowed_pattern"), null));
          if (first) { //no id was found
             dimConf.m_dimCalc = new DimCalc(null, null,null);
          }
//          dimConf.m_dimInfo = DimInfo.getDimInfo(dimConf.m_id);
          dimConf.m_height = Misc.getParamAsInt(elem.getAttribute("height"), 1);
          dimConf.m_width = Misc.getParamAsInt(elem.getAttribute("width"), 20);
          dimConf.m_textPrintCharWidth = Misc.getParamAsInt(elem.getAttribute("text_char_width"), 10);
          dimConf.m_textPrintTruncateToWidth = Misc.getParamAsInt(elem.getAttribute("text_truncate"),1);
          dimConf.m_labelWidth = Misc.getParamAsInt(elem.getAttribute("label_width"), dimConf.m_labelWidth);          
          dimConf.m_disp = MyXMLHelper.getAttribAsString(elem, "disp");
          dimConf.m_default = MyXMLHelper.getAttribAsString(elem, "default");
          dimConf.m_multiCols = Misc.getParamAsInt(MyXMLHelper.getAttribAsString(elem, "num_cols"),1);
          dimConf.m_dataSpan = Misc.getParamAsInt(elem.getAttribute("data_span"),1);
          dimConf.m_rowSpan = Misc.getParamAsInt(elem.getAttribute("row_span"),1);
          dimConf.m_customField1 = Misc.getParamAsString(elem.getAttribute("custom_field1"),null);
          dimConf.m_customField2 = Misc.getParamAsString(elem.getAttribute("custom_field2"),null);
          dimConf.m_customField3 = Misc.getParamAsString(elem.getAttribute("custom_field3"),null);
          dimConf.m_addRow = 1 == MyXMLHelper.getAttribAsInt(elem, "add_row");
          //public boolean m_multiRowOldOnlyReadOnly = false;
          //public ArrayList m_multiRowSubColList = null; //of DimConfigInfo
          dimConf.m_multiRowOldReadOnly = "1".equals(elem.getAttribute("multi_old_read_only"));

		  dimConf.m_dataWidth = Misc.getParamAsInt(elem.getAttribute("data_width"), 0);
          
          dimConf.m_forceCheckRadio = 1 == MyXMLHelper.getAttribAsInt(elem, "check_radio");
          dimConf.m_nowrap = !"0".equals(elem.getAttribute("nowrap"));
          dimConf.m_labelNowrap = "1".equals(elem.getAttribute("label_nowrap"));
          dimConf.m_internalName = MyXMLHelper.getAttribAsString(elem, "internal_name");
          dimConf.m_orBlock = "1".equals(elem.getAttribute("or_block")); 
          // to set column value 
          dimConf.m_set_val = MyXMLHelper.getAttribAsString(elem, "set_val");
          	
          if (dimConf.m_internalName == null)
        	  dimConf.m_internalName = dimConf.m_name;
          dimConf.m_askUser = "1".equals(elem.getAttribute("ask_user"));
          if (dimConf.m_addRow && !dimConf.m_forceCheckRadio)
             dimConf.m_forceCheckRadio = true;
          String readParam = MyXMLHelper.getAttribAsString(elem, "read");
          if (readParam == null || readParam.length() == 0) {
             if (readParamMissingMeansRead)
                dimConf.m_readOnly = true;
             else
                dimConf.m_readOnly = false;
          }
          else
             dimConf.m_readOnly = "1".equals(readParam);
          if (dimConf.m_dimCalc != null && dimConf.m_dimCalc.m_dimInfo != null ) {
             int typ = dimConf.m_dimCalc.m_dimInfo.getAttribType();
             if (typ == Cache.LOV_NO_VAL_TYPE || typ == Cache.LOV_TYPE) {
                dimConf.m_rememberUserSel = true;
             }
          }
          
          dimConf.m_forceGetValueDefaultValueFromDB = "1".equals(elem.getAttribute("forceDefaultFromDB"));
          dimConf.m_hidden = "1".equals(elem.getAttribute("hidden"));
          dimConf.m_doEdit = "1".equals(elem.getAttribute("do_edit"));
          dimConf.m_initZero = "1".equals(elem.getAttribute("init_zero"));
          dimConf.m_doExactSearch = "1".equals(elem.getAttribute("do_exact_search"));
          dimConf.m_canTot = "1".equals(elem.getAttribute("tot"));
          dimConf.m_doautoCompleteAsClass = !"0".equals(elem.getAttribute("auto_complete_class"));
          dimConf.m_splitByLovDim = Misc.getParamAsInt(elem.getAttribute("splt_by_dim"));
          dimConf.m_splitTot = !"0".equals(elem.getAttribute("split_tot"));

          dimConf.m_orderBy = "1".equals(elem.getAttribute("order_by"));
          dimConf.m_skip_groupby = "1".equals(elem.getAttribute("skip_groupby"));
          dimConf.m_skip_link_params = "1".equals(elem.getAttribute("skip_link_params"));
          dimConf.m_orderByTag = !"0".equals(elem.getAttribute("asec"));
          dimConf.m_canEndBeyondCurrent = "1".equals(elem.getAttribute("after_current"));
          dimConf.m_helperType = Misc.getParamAsInt(elem.getAttribute("helper_type"), dimConf.m_helperType);
          
          dimConf.m_columnName = MyXMLHelper.getAttribAsString(elem, "column_name");
          dimConf.m_ifClassifyIsTypeOfDim = DimInfo.getDimInfo(Misc.getParamAsInt(elem.getAttribute("classify_type")));
          dimConf.m_calcBy = MyXMLHelper.getAttribAsString(elem, "calc_by");
          dimConf.m_calcOp = Misc.getParamAsInt(elem.getAttribute("calc_op"), dimConf.m_calcOp);
          dimConf.m_isSelect = "1".equals(elem.getAttribute("select"));
          dimConf.m_multiSelect = !"0".equals(elem.getAttribute("multi_select"));
          dimConf.m_isMandatory = "1".equals(elem.getAttribute("mandatory"));
          dimConf.m_mandatoryGroup = Misc.getParamAsInt(elem.getAttribute("mandatory_group"));
          dimConf.m_oneOfAllGrouping = elem.getAttribute("one_of_mand");
          dimConf.m_uniqueGroup = Misc.getParamAsInt(elem.getAttribute("unique_group"));
          dimConf.innerMandatory = "1".equals(elem.getAttribute("inner_mandatory"));
          dimConf.useTopLevelInInpTemplate = "1".equals(elem.getAttribute("use_top_level_in_template"));
          dimConf.putValAsHiddenTooInInpTemplate = "1".equals(elem.getAttribute("put_hidden_forwrite_template"));
          dimConf.outerQueryAggOp = elem.getAttribute("outer_agg_op");
          if (dimConf.outerQueryAggOp != null && dimConf.outerQueryAggOp.length() == 0)
        	  dimConf.outerQueryAggOp = null;
          dimConf.outerCalcExpr = elem.getAttribute("outer_calc_expr");
          if (dimConf.outerCalcExpr != null && dimConf.outerCalcExpr.length() == 0)
        	  dimConf.outerCalcExpr = null;
          dimConf.m_isCached = "1".equals(elem.getAttribute("cached"));
//          dimConf.m_helpTag = elem.getAttribute("help_tag");
          dimConf.m_prefixBeforeEntry = MyXMLHelper.getAttribAsString(elem, "prefix_entry");
          dimConf.m_suffixAfterEntry = MyXMLHelper.getAttribAsString(elem, "suffix_entry");
          //dimConf.m_makeReadOnlyPost = "1".equals(elem.getAttribute("read_only_after_edit")); //to be deprecated
          dimConf.m_paramName = MyXMLHelper.getAttribAsString(elem, "param_inp_name");
          dimConf.m_setAsPermInSearchBox = "1".equals(elem.getAttribute("is_perm")); //overloaded ... also used in InputTemplate nested cols to find the column that is unique for the parent object
          // TODO check for 0 or null && 20501, 20530 etc
          if((elem.getAttribute("is_perm") == null || "0".equals(elem.getAttribute("is_perm"))) && (dimId == 20501 || dimId == 20530 || dimId == 20560)){
        	  dimConf.m_setAsPermInSearchBox = true;
          }
          dimConf.m_frontPageColSpanLabel = MyXMLHelper.getAttribAsString(elem, "col_span_label");
          dimConf.m_calcExprStr = MyXMLHelper.getAttribAsString(elem, "calc_expr");
          dimConf.m_linkControlString = MyXMLHelper.getAttribAsString(elem, "link");//will be of form of xyz.jsp?param=$column_name$&@param=$column_name
                                                                                                           //when printing link @ to be replaced with topPageContext, $column_name to be replaced with value of that column in that row (if that is not null)
          dimConf.m_orderByInFrontPage = MyXMLHelper.getAttribAsString(elem, "order"); //if non-null will be added to order by (or group by if doing mySql and the latter has with rollup
          dimConf.m_doRollupTotal = "1".equals(elem.getAttribute("do_rollup"));
          dimConf.m_forInterleavedSearchOnly = "1".equals(elem.getAttribute("for_interleaved_search_only"));

          dimConf.m_hiddenSpecialControl = Misc.getParamAsInt(elem.getAttribute("hidden_special_flag"), G_READHIDE_NEVER);
          dimConf.m_readSpecialControl = Misc.getParamAsInt(elem.getAttribute("read_special_flag"), G_READHIDE_NEVER);
          dimConf.m_targetPage = Misc.getParamAsString(elem.getAttribute("target_page"), dimConf.m_targetPage);
          dimConf.m_objectIdParam = Misc.getParamAsString(elem.getAttribute("object_id_param"), dimConf.m_objectIdParam);
          dimConf.m_multiColAddRowScript = Misc.getParamAsString(elem.getAttribute("add_script"), null);
          dimConf.m_multiColValidateScript = Misc.getParamAsString(elem.getAttribute("validate_script"), null); //not used ... will instead change stuff     
          dimConf.m_labelStyleClass = Misc.getParamAsString(elem.getAttribute("label_style"), null);
          dimConf.m_valStyleClass = Misc.getParamAsString(elem.getAttribute("val_style"), null);
          dimConf.m_isRuleType = "1".equals(elem.getAttribute("is_rule_type"));
          dimConf.m_customPropEditHint = MyXMLHelper.getAttribAsString(elem, "custom_prop_edit_hint");
          if (dimConf.m_customPropEditHint != null && dimConf.m_customPropEditHint.length() != 0) {
              int dbg=1;
          }
            
          if (dimConf.m_customPropEditHint != null && dimConf.m_customPropEditHint.length() == 0)
              dimConf.m_customPropEditHint = null;
          if ("1".equals(elem.getAttribute("read_only_after_edit")))
             dimConf.m_readSpecialControl = G_READHIDE_POSTCREATE;
          dimConf.m_refUnit = Misc.getParamAsInt(elem.getAttribute("ref_unit"));
          if ("Investment Request".equals(dimConf.m_name)) {
             String dbg1 = elem.getAttribute("master_data");
             String dbg2 = null;
          }
          dimConf.m_refMasterBlockInPFM = Misc.getParamAsString(elem.getAttribute("master_data"), null);
          dimConf.m_refBlockInPFM = Misc.getParamAsString(elem.getAttribute("block"), null);
          // 07102009
          dimConf.m_granularity = "1".equals(elem.getAttribute("use_granularity"));
          dimConf.m_aggregate = "1".equals(elem.getAttribute("use_aggregate"));
          dimConf.m_subtype = Misc.getParamAsString(elem.getAttribute("sub_type"), null);
          dimConf.m_scale = Misc.getParamAsDouble(elem.getAttribute("scale"));
          dimConf.m_decimalPrecision = Misc.getParamAsInt(elem.getAttribute("decimal_precision"));
          dimConf.m_minDecimal = Misc.getParamAsInt(elem.getAttribute("min_decimal"),0);
          dimConf.m_p_val = Misc.getParamAsInt(elem.getAttribute("p_val"));
          dimConf.m_url = Misc.getParamAsString(elem.getAttribute("url"), null);
          dimConf.m_xml = Misc.getParamAsString(elem.getAttribute("xml"), null);
          dimConf.m_show_modal_dialog = "1".equals(elem.getAttribute("show_modal_dialog"));
          dimConf.m_dialog_width = Misc.getParamAsString(elem.getAttribute("dialog_width"), null);
          dimConf.m_dialog_height = Misc.getParamAsString(elem.getAttribute("dialog_height"), null);
          dimConf.m_do_submit = "1".equals(elem.getAttribute("do_submit"));
          dimConf.m_forDateApplyGreater = "1".equals(elem.getAttribute("date_filter_greater"));
          dimConf.m_defaultOperator = Misc.getParamAsString(elem.getAttribute("operator"));
          dimConf.m_showOperator = !"0".equals(elem.getAttribute("show_operator"));
          dimConf.m_rightOperand = Misc.getParamAsString(elem.getAttribute("operand_second"));
          dimConf.m_numeric_filter = "1".equals(elem.getAttribute("numeric_filter"));
          dimConf.m_image_file = Misc.getParamAsString(elem.getAttribute("image_file"), null);
          dimConf.m_use_image = "1".equals(elem.getAttribute("use_image"));
          dimConf.m_color_code = "1".equals(elem.getAttribute("color_code"));
          dimConf.m_color_code_by = elem.getAttribute("color_code_by");
          
          dimConf.m_param1 = Misc.getParamAsInt(elem.getAttribute("param1"));
          dimConf.m_param2 = Misc.getParamAsInt(elem.getAttribute("param2"));
          dimConf.m_param3 = Misc.getParamAsInt(elem.getAttribute("param3"));
          dimConf.m_param1_color = Misc.getParamAsInt(elem.getAttribute("param1_color"));
          dimConf.m_param2_color = Misc.getParamAsInt(elem.getAttribute("param2_color"));
          dimConf.m_param3_color = Misc.getParamAsInt(elem.getAttribute("param3_color"));
          dimConf.m_color_gran = Misc.getParamAsInt(elem.getAttribute("m_color_gran"));
          
          dimConf.m_on_change_id = Misc.getParamAsInt(elem.getAttribute("on_change_id"));
          dimConf.m_lookHelp1 = elem.getAttribute("look1");
          dimConf.m_lookHelp2 = elem.getAttribute("look2");
          //to handle onchange events in search box
          dimConf.m_onChange_handler = Misc.getParamAsString(elem.getAttribute("onchange_handler"), null);
          dimConf.m_onChange_url = Misc.getParamAsString(elem.getAttribute("onchange_url"), null);
          dimConf.m_onChange_add_params = Misc.getParamAsString(elem.getAttribute("onchange_add_params"), null);
          dimConf.m_customBlock= Misc.getParamAsString(elem.getAttribute("custom_block"), null);
          dimConf.m_isDisplay = "1".equals(elem.getAttribute("is_display"));
          dimConf.m_isAutocomplete = "1".equals(elem.getAttribute("is_autocomplete"));
          dimConf.m_min_sugg_length = Misc.getParamAsInt(elem.getAttribute("min_sugg_len"));
          dimConf.m_isRadio = "1".equals(elem.getAttribute("radio"));
          dimConf.m_searchExact = "1".equals(elem.getAttribute("exact_search"));
          dimConf.m_buttonAction = elem.getAttribute("button_name");
          
          //@#@#@#
          dimConf.doPivot = (byte) (Misc.getParamAsInt(elem.getAttribute("pivot"),0));
          dimConf.doPivotMeasure = "1".equals(elem.getAttribute("pivot_measure"));
          dimConf.doTotOnlyInPivotForMeasure = "1".equals(elem.getAttribute("pivot_tot_only"));
          String pivotBy = elem.getAttribute("do_pivot_by");
          if (pivotBy != null && pivotBy.length() > 0) {
        	  ArrayList<String> multiPivot = new ArrayList<String>();
        	  Misc.convertValToStrVector(pivotBy, multiPivot);
        	  dimConf.doPivotByIfMulti = multiPivot;
          }
          dimConf.ytdScopePlus1 = (byte)Misc.getParamAsInt(elem.getAttribute("ytd_scope_plus1"),0);
          dimConf.endScopeForYtdPlus1 = (byte)Misc.getParamAsInt(elem.getAttribute("end_scope_for_ytd_plus1"),0);
          dimConf.cummDontResetOver = "1".equals(elem.getAttribute("cumm_dont_reset"));
          dimConf.doJavaCumm = "1".equals(elem.getAttribute("do_java_cumm"));
          dimConf.doTotalAcrossColInPivot = "1".equals(elem.getAttribute("tot_across_col"));
          dimConf.doTotalAcrossRow = "1".equals(elem.getAttribute("tot_across_row"));
          
          if (dimConf.m_isSelect) {
              for (Node n = elem.getFirstChild(); n != null ; n = n.getNextSibling()) {
                 if (n.getNodeType() != 1)
                    continue;
                 Element e = (Element) n;
                 int id = Misc.getParamAsInt(e.getAttribute("id"));
                 String val = e.getAttribute("val");
                 if (!Misc.isUndef(id) && val != null && val.length() != 0) {
                    FixedHelper fix = new FixedHelper();
                    fix.m_idThenIndex = id;
                    Misc.convertValToVector(val, fix.m_val);
                    if (dimConf.m_fixedForSel == null)
                       dimConf.m_fixedForSel = new ArrayList();
                    dimConf.m_fixedForSel.add(fix);
                 }
              }
          }

          //for (Node n = elem.getFirstChild(); n != null ; n = n.getNextSibling()) {
          //    if (n.getNodeType() != 1)
          //       continue;
          //    Element e = (Element) n;
          //    int id = Misc.getParamAsInt(e.getAttribute("id"));
          //    if (Misc.isUndef(id))
          //       continue;
          //    if (dimConf.m_subDimId == null)
          //       dimConf.m_subDimId = new ArrayList();
          //    dimConf.m_subDimId.add(new Integer(id));
          //}

//          if (Misc.isUndef(dimConf.m_id))
//             return null;
         for (Node n = elem.getFirstChild(); n != null; n = n.getNextSibling()) {
             if (n.getNodeType() != 1) 
                continue;
             Element e = (Element) n;
             String tagName = e.getTagName();
             boolean isCol = "col".equals(tagName);
             boolean isMenu = "menu".equals(tagName);
             if (isCol) {//DIFFERENT FROM CAPEX
            	 
                DimConfigInfo subCol = getDimConfigInfo(e, readParamMissingMeansRead);
                if (subCol != null) {
                	if (dimConf.m_nestedCols == null) {
               		 	dimConf.m_nestedCols = new ArrayList<ArrayList<DimConfigInfo>>();
               		 	dimConf.m_nestedCols.add(new ArrayList<DimConfigInfo>());
               	 	}
                	dimConf.m_nestedCols.get(0).add(subCol);
                }
             }
             else if (isMenu) {
                InnerMenuInfo menu = InnerMenuInfo.read(e);
                if (menu != null) {
                    if (dimConf.m_innerMenuList == null)
                       dimConf.m_innerMenuList = new ArrayList();       
                    dimConf.m_innerMenuList.add(menu);
                }
             }
         }
         if (dimConf.m_dimCalc != null && dimConf.m_dimCalc.m_dimInfo != null && (dimConf.m_columnName == null || dimConf.m_columnName.length() == 0)) 
        	 dimConf.m_columnName = "d"+dimConf.m_dimCalc.m_dimInfo.m_id;
        
         if (dimConf.m_name != null && dimConf.m_name.length() == 0 && (dimConf.m_internalName == null || dimConf.m_internalName.length() == 0))
        	 dimConf.m_internalName = dimConf.m_name;
         
         if (dimConf.m_columnName == null || dimConf.m_columnName.length() == 0) {
             dimConf.m_columnName = dimConf.m_internalName; 
         }
         if (dimConf.m_columnName == null || dimConf.m_columnName.length() == 0) {
        	 //uggh .. figure out a junk name somehow
         } 
         
          return dimConf;
      }
      
   public static ArrayList readRowColInfo(Element topElem, boolean readParamMissingMeansRead) { //Elements are ArrayList of DimConfigInfo
       //copied from PrjTemplateHelper this is the final place
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
   // to get profiles
   public static ArrayList<DimConfigInfo> getProfileList(ArrayList profileList, int id){
	   for (int i = 0; i < profileList.size(); i++) {
		   if(id == i) 
			   return (ArrayList<DimConfigInfo>)profileList.get(i);
	   }
	   return (ArrayList)profileList.get(0);
   }
   public static DimConfigInfo getProfile(ArrayList<DimConfigInfo> profileList, int id){
	   for (int i = 0; i < profileList.size(); i++) {
		   if(((DimConfigInfo)profileList.get(i)).m_dimCalc.m_dimInfo.m_id == id) 
			   return (DimConfigInfo)profileList.get(i);
	   }
	   return (DimConfigInfo)profileList.get(0);
   }
public boolean isDoPivotMeasure() {
	return doPivotMeasure;
}
public void setDoPivotMeasure(boolean doPivotMeasure) {
	this.doPivotMeasure = doPivotMeasure;
}
public boolean isCummDontResetOver() {
	return cummDontResetOver;
}
public void setCummDontResetOver(boolean cummDontResetOver) {
	this.cummDontResetOver = cummDontResetOver;
}
public boolean isDoJavaCumm() {
	return doJavaCumm;
}
public void setDoJavaCumm(boolean doJavaCumm) {
	this.doJavaCumm = doJavaCumm;
}
public boolean isDoTotalAcrossColInPivot() {
	return doTotalAcrossColInPivot;
}
public void setDoTotalAcrossColInPivot(boolean doTotalAcrossColInPivot) {
	this.doTotalAcrossColInPivot = doTotalAcrossColInPivot;
}
public boolean isDoTotalAcrossRow() {
	return doTotalAcrossRow;
}
public void setDoTotalAcrossRow(boolean doTotalAcrossRow) {
	this.doTotalAcrossRow = doTotalAcrossRow;
}
public int getMaxVal() {
	return maxVal;
}
public void setMaxVal(int maxVal) {
	this.maxVal = maxVal;
}
public String getAllowedPattern() {
	return allowedPattern;
}
public void setAllowedPattern(String allowedPattern) {
	this.allowedPattern = allowedPattern;
}
public int getMinVal() {
	return minVal;
}
public void setMinVal(int minVal) {
	this.minVal = minVal;
}
   }
