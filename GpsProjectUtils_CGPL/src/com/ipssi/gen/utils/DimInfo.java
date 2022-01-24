package com.ipssi.gen.utils;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


  public class DimInfo implements Serializable {
     /**
	 * 
	 */
	  public String toString() {
		  return Integer.toString(m_id);
	  }
	private static final long serialVersionUID = 1L;
	public static HashMap g_dimList = new HashMap(10000,0.75f); //keyed by integer
     public static HashMap g_dimListByCatName = new HashMap(10000,0.75f); //keyed by Category Node
     public static ArrayList g_dimInfoWithCurUsingPrjCurrency = new ArrayList();
     private static ArrayList g_tempDimInfoListRequiringAccessCheck = new ArrayList(); //while reading internal/lov will populate acc check tag. then when privilege list read then using this list will populate all
     private String m_tempAccTag = null;
     public PrivInfo.TagInfo m_accCheck = null;
     public String m_readTagNew = null;
     public String m_writeTagNew = null;
     public int m_accCheckonObj = -1; //one of PurchaseSearch. object     
     public String m_getQuery = null; //if not null, then the values need to be loaded dynamically query must be of form of select id,name .. see next field
     public boolean m_lovListRequiresDynamicOrg = false; //if true then the query above will be select id, name etc. etc where port_node_id = ?. It will get pv123 value and fill in
     private boolean m_lovPopulatedFromDB = false; 
     private int m_lovPopulatedLastFromPortId = Misc.getUndefInt();
     private int m_dynQueryHasTag = -1;//not found, 0 => no, 1 => 2
     private int minVal = Misc.getUndefInt();
     private int maxVal = Misc.getUndefInt();
     private String allowedPattern = null;
     public byte m_loadFromDB = 0; // 0=> dont, 1 => from DB only, -1 => never from DB
     public void makeDirty() {
    	 m_lovPopulatedLastFromPortId = Misc.getUndefInt();
    	 if (this.m_lovPopulatedFromDB || this.m_lovListRequiresDynamicOrg)
    		 m_valList = null;
     }
     public int getValCount() {
    	 ArrayList t = getValList();
    	 return t == null ? 0 : t.size();
     }
     private ArrayList<ValInfo> loadFromDB(int pv123) throws Exception {
    	 boolean destroyIt = false;
    	 Connection conn = null;
    	 try {
    		 conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		 ValInfo parValInfo = null;
    		 PreparedStatement ps = conn.prepareStatement(m_getQuery);
    		 if (m_lovListRequiresDynamicOrg)
    			 ps.setInt(1,pv123);
    		 ResultSet rs = ps.executeQuery();
    		 ResultSetMetaData metaData = rs.getMetaData();
    		 int columnCount = metaData.getColumnCount();
             ArrayList valList = new ArrayList();    		 
    		 while (rs.next()) {
                    ValInfo
                    valInfo = new ValInfo();
                    int id = rs.getInt(1);
                    if (rs.wasNull())
                    	continue;
                    String name = rs.getString(2);
                    String code = null;
                    if(columnCount > 2)
                    	code = rs.getString(3);//m_sn
                    
                    if (name == null) {
                    	if (!Misc.isUndef(this.m_subsetOf) && this.m_id != this.m_subsetOf) {
                    		DimInfo parDim = DimInfo.getDimInfo(this.m_subsetOf);
                    		parValInfo = parDim.getValInfo(id);
                    		name = parValInfo == null ? Integer.toString(id) : parValInfo.m_name;
                    	}
                    	if (name == null)
                    		continue;
                    }
                    valInfo.m_id = id;
                    valInfo.m_name = name;
                    valInfo.m_sn = code;
                    String tag = null;
                    if (this.m_dynQueryHasTag != 0) {
					try {
						tag =  rs.getString(3);//previous DIMs has not used aliasing
						valInfo.m_str_field1 = getStringFromRset(rs,"str_field1"); 
						valInfo.m_str_field2 = getStringFromRset(rs,"str_field2");
						valInfo.m_str_field3 = getStringFromRset(rs,"str_field3");
						valInfo.m_str_field4 = getStringFromRset(rs,"str_field4");
					} catch (Exception e2) {
						// eat it ...
						m_dynQueryHasTag = 0; // indicate that priv tag is not
												// defined in the query
					}
				}
                    
                    if (tag != null) {
                    	valInfo.privTag = new ArrayList<String>();
                    	Misc.convertValToStrVector(tag, valInfo.privTag);
                    }
                    else {
                    	if (!Misc.isUndef(this.m_subsetOf) && this.m_id != this.m_subsetOf && (parValInfo == null || parValInfo.m_id != id)) {
                    		DimInfo parDim = DimInfo.getDimInfo(this.m_subsetOf);
                    		parValInfo = parDim.getValInfo(id);
                    		
                    	}
                    	if (parValInfo != null && parValInfo.privTag != null)
                    		valInfo.privTag = (ArrayList<String>) parValInfo.privTag.clone();
                    }
                    valList.add(valInfo);
    		 }
    		 rs.close();
    		 ps.close();
    		if (!m_lovListRequiresDynamicOrg && valList.size() == 0) //no data was found just use existing
    			valList = null;
    		synchronized (this) {
    			m_valList = valList;
    			m_lovPopulatedLastFromPortId = pv123;
    			m_lovPopulatedFromDB = true;
    		}
    		return valList;
    	 }
    	 catch (Exception e) {
    		 e.printStackTrace();
    		 destroyIt = true;
    		 throw e;
    	 } finally {
				try {
					if (conn != null)
						DBConnectionPool.returnConnectionToPoolNonWeb(conn, destroyIt);
				}
				catch (Exception e) {
					e.printStackTrace();
					//eat it
				}
			}
     }
     
     private String getStringFromRset(ResultSet rs, String colName){
     		try {
				return rs.getString(colName);
			} catch (SQLException e) {
			}
			return null;
	}
	public ArrayList checkAndPopulateLovListFromDB(Connection conn1, SessionManager session) throws Exception { //returns if it did so successfully
    	 if (m_getQuery == null)
    		 return getValList();
    	 int pv123 = Misc.getUndefInt();
    	 if (m_lovListRequiresDynamicOrg) 
    		 pv123 = Misc.getParamAsInt(session == null ? null : session.getParameter("pv123"), Misc.G_TOP_LEVEL_PORT);
    	 if (m_valList == null || m_valList.size() == 0 || (m_lovListRequiresDynamicOrg && pv123 !=m_lovPopulatedLastFromPortId))
    		 return loadFromDB(pv123);
    	 return getValList();
    	 
   	 
     }         
     
     public static void populateAccCheckInfo(Cache cache) throws Exception {
        for (int i=0,is = g_tempDimInfoListRequiringAccessCheck == null ? 0 : g_tempDimInfoListRequiringAccessCheck.size(); i<is;i++) {
           DimInfo dimInfo = (DimInfo) g_tempDimInfoListRequiringAccessCheck.get(i);
           if (dimInfo.m_tempAccTag != null)
              dimInfo.m_accCheck = cache.getPrivId(dimInfo.m_tempAccTag);
        }
     }
     public void clearValList() {
		  if (m_valList != null)
			  m_valList.clear();
	  }

      public static class CapSpecialInfo implements Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int m_skillId = 0;
        public ArrayList m_common = null; //of int
        public ArrayList m_special = null;//of int        
        void addCommon(int id) {
           if (m_common == null)
              m_common = new ArrayList();
           m_common.add(new Integer(id));
        }
        void addSpecial(int id) {
           if (m_special == null) {
              m_special = new ArrayList();
           }
           m_special.add(new Integer(id));
        }
     }
     public ArrayList m_capSpecialInfo = null;//of CapSpecialInfo
     
     public static class ValInfo  implements Serializable {
         /**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
        public int m_id = Misc.getUndefInt();
        public String m_name = null;
        public String m_sn = null;
        public boolean m_isSp = false;
        public String m_str_field1 = null;
        public String m_str_field2 = null;
        public String m_str_field3 = null;
        public String m_str_field4 = null;
        
        public ArrayList m_otherProp = null; //of MiscInner.PairStr
//        public String m_subtype = null;
        public ArrayList<String> privTag = null;
        public ValInfo() {
        }
        public boolean hasPriv(SessionManager session) throws Exception {
        	if (session == null || session.getUser() == null || privTag == null || privTag.size() == 0)
        		return true;
        	User user = session.getUser();
        	for (String s: privTag) {
        		if (user.isPrivAvailable(session, s))
        			return true;
        	}
        	return false;
        }
        
        public ValInfo(Element lovItem) {
           org.w3c.dom.NamedNodeMap attribList = lovItem.getAttributes();
           for (int t22=0,t22s = attribList.getLength();t22<t22s;t22++) {
               org.w3c.dom.Node attribNode = attribList.item(t22);
               String attribName = attribNode.getNodeName();
               String attribValue = attribNode.getNodeValue();
               if ("id".equals(attribName) || "val".equals(attribName)) {
                   m_id = Misc.getParamAsInt(attribValue);
               }
               else if ("name".equals(attribName)) {
                   m_name = attribValue;
               }
               else if ("is_sp".equals(attribName)) {
                   m_isSp = "1".equals(attribValue);
               }
               else if ("sn".equals(attribName)) {
                   m_sn = attribValue;
               }
//               else if ("subtype".equals(attribName)) {
//                   m_subtype = attribValue;
//               }
               else if ("sec_tag".equals(attribName)) {
            	   if (attribValue != null && attribValue.length() != 0) {
            		   privTag = new ArrayList<String>();
            		   Misc.convertValToStrVector(attribValue, privTag);
            	   }
               }
               else {
                  if (m_otherProp == null)
                     m_otherProp = new ArrayList();
                  m_otherProp.add(new MiscInner.PairStr(attribName, attribValue));
               }
           }
           if (m_sn == null) {
              String itemTag = lovItem.getTagName();
              if (!itemTag.equals("val"))
                 m_sn = itemTag;              
           }
           if (m_name == null)
              m_name = m_sn;
           if (m_name == null)
              m_name = "d"+Integer.toString(m_id);
           if (m_sn == null)
              m_sn = m_name;
        }
        public String getOtherProperty(String name) {
           for (int i=0,is=m_otherProp == null ? 0 : m_otherProp.size();i<is;i++) {
              MiscInner.PairStr pst = (MiscInner.PairStr) m_otherProp.get(i);
              if (pst.first.equals(name))
                 return pst.second;
           }
           //may be check for "id", "name", "sn", "is_sp"
           return null;
           
        }
        public static ArrayList readLovNode(Element lovNode) {
           ArrayList retval = null;           
           for (Node n = lovNode.getFirstChild(); n != null; n = n.getNextSibling()) {
               if (n.getNodeType() != 1)
                  continue;
               Element e = (Element) n;
               ValInfo valInfo = new ValInfo(e);
               if (retval == null)
                  retval = new ArrayList(5);
               retval.add(valInfo);
           }
           return retval;
        }//end of func
     }//end of class
     
     public static class RHSVals  implements Serializable {
         /**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
        int m_leftVal; //the lhsValInfo;
        public ArrayList m_rhsVals = new ArrayList(); //of integer
        public RHSVals(int leftVal) {
           m_leftVal = leftVal;
        }
     }
     public static class RHSValInfo  implements Serializable {
         /**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
        public int m_rhsDimId; // the rhs dim
        public ArrayList m_rhsVals = new ArrayList(); //of RHSVals
        public RHSValInfo(int rhsDimId) {
          m_rhsDimId = rhsDimId;
        }
     }
     public static class LHSValInfo  implements Serializable {
         /**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
        public int m_lhsDim;
        public ArrayList m_lhsVals = new ArrayList(); //of MiscInner.Pair (of int) 1st is rhsval, 2nd is parent valinfo
        public LHSValInfo(int lhsDim) {
           m_lhsDim = lhsDim;
        }
     }
     
     private ArrayList m_rhsDescInfo = null; //of RHSValInfo
     private ArrayList m_lhsParentInfo = null; //of LHSValInfo;
     public ArrayList getRHSValidValsForDim(int childDim) {
        for (int i=0,is = m_rhsDescInfo == null ? 0 : m_rhsDescInfo.size();i<is;i++) {
           RHSValInfo rhsValInfo = (RHSValInfo) m_rhsDescInfo.get(i);
           if (rhsValInfo.m_rhsDimId == childDim) {
              return rhsValInfo.m_rhsVals;
           }
        }
        return null;           
     }
     public String getBackwardCompatValidVals(int leftVal) { //for the 1st rhsdim found
        StringBuilder retval = new StringBuilder();
        boolean allVals = Misc.isUndef(leftVal);
        for (int i=0,is = m_rhsDescInfo == null ? 0 : m_rhsDescInfo.size();i<is;i++) {
           RHSValInfo rhsValInfo = (RHSValInfo) m_rhsDescInfo.get(i);
           
            ArrayList rhsVals = rhsValInfo.m_rhsVals;
            for (int j=0,js = rhsVals == null ? 0 : rhsVals.size();j<js;j++) {
               RHSVals rhsVal = (RHSVals) rhsVals.get(j);
               if (rhsVal.m_leftVal == leftVal || allVals) {
                  Misc.convertInListToStr(rhsVal.m_rhsVals, retval);
               }                  
            }           
        }
        return retval.toString();
     }
     public ArrayList getRHSVals(int childDim, int leftVal) {
        for (int i=0,is = m_rhsDescInfo == null ? 0 : m_rhsDescInfo.size();i<is;i++) {
           RHSValInfo rhsValInfo = (RHSValInfo) m_rhsDescInfo.get(i);
           if (rhsValInfo.m_rhsDimId == childDim) {
              ArrayList rhsVals = rhsValInfo.m_rhsVals;
              for (int j=0,js = rhsVals == null ? 0 : rhsVals.size();j<js;j++) {
                 RHSVals rhsVal = (RHSVals) rhsVals.get(j);
                 if (rhsVal.m_leftVal == leftVal)
                    return rhsVal.m_rhsVals;
              }
           }
        }
        return null;
     }
     
     public void getAllParentVals(int parentDim, int rhsVal, ArrayList result) {       
       for (int i=0,is = m_lhsParentInfo == null ? 0 : m_lhsParentInfo.size();i<is;i++) {
           LHSValInfo lhsList = (LHSValInfo) m_lhsParentInfo.get(i);
           if (lhsList.m_lhsDim == parentDim) {
              for (int j=0,js = lhsList.m_lhsVals == null ? 0:lhsList.m_lhsVals.size();j<js;j++) {
                  MiscInner.Pair ancInfo = (MiscInner.Pair) lhsList.m_lhsVals.get(j);
                  if (ancInfo.first == rhsVal) {
                     result.add(new Integer(ancInfo.second));              
                  }
              }
              break;
           }
       }
     }
     public int getAParentVal(int parentDim, int rhsVal) {       
       for (int i=0,is = m_lhsParentInfo == null ? 0 : m_lhsParentInfo.size();i<is;i++) {
           LHSValInfo lhsList = (LHSValInfo) m_lhsParentInfo.get(i);
           if (lhsList.m_lhsDim == parentDim) {
              for (int j=0,js = lhsList.m_lhsVals == null ? 0:lhsList.m_lhsVals.size();j<js;j++) {
                  MiscInner.Pair ancInfo = (MiscInner.Pair) lhsList.m_lhsVals.get(j);
                  if (ancInfo.first == rhsVal) {
                     return ancInfo.second;
                  }
              }
           }
       }
       return Misc.getUndefInt();
     }
     
     public static void loadValidVal(Element validVal) {
        DimInfo ldim = DimInfo.getDimInfo(validVal.getAttribute("left_dim"));
        DimInfo rdim = DimInfo.getDimInfo(validVal.getAttribute("right_dim"));
        if (ldim == null || rdim == null)
           return;
        if (ldim.m_rhsDescInfo == null)
           ldim.m_rhsDescInfo = new ArrayList();
        RHSValInfo rhsAdd = new RHSValInfo(rdim.m_id);
        ldim.m_rhsDescInfo.add(rhsAdd);
        
        if (rdim.m_lhsParentInfo == null)
           rdim.m_lhsParentInfo = new ArrayList();
        LHSValInfo lhsAdd = new LHSValInfo(ldim.m_id);
        rdim.m_lhsParentInfo.add(lhsAdd);
        
        for (Node n=validVal.getFirstChild();n!=null;n=n.getNextSibling()) {
           if (n.getNodeType() != 1)
              continue;
           Element e = (Element) n;
           int lval = Misc.getParamAsInt(e.getAttribute("id"));
           RHSVals rhsVals = new RHSVals(lval);
           rhsAdd.m_rhsVals.add(rhsVals);
           for (Node cn=e.getFirstChild();cn != null; cn = cn.getNextSibling()) {
               if (cn.getNodeType() != 1)
                  continue;
               Element ce = (Element) cn;
               int rval = Misc.getParamAsInt(ce.getAttribute("id"));
               rhsVals.m_rhsVals.add(new Integer(rval));
               lhsAdd.m_lhsVals.add(new MiscInner.Pair(rval, lval));
           }//each possible r vals
        }//each possible lvals
     }//end of fucn
          
     public static final int QTY_CURRENCY = 0;
     public static final int QTY_NUMBER = 1;     
     public static final int QTY_TONNES = 2;
     public static final int QTY_INTEGER = 3;
     public static final int QTY_YEARS = 4;
     //public static final int QTY_PERCENT = 5;
     //public static final int QTY_FTE = 6;     
     
     
     public int m_id;
     public int m_subsetOf;
     private ArrayList m_valList = null;//of DimInfo.ValInfo
     public int m_type = Cache.STRING_TYPE;
     public int getAttribType() { return m_type;}
     public int m_descDataDimId;
     public ArrayList m_refOrgLevel = null;
     public ArrayList m_refOrgAncTill = null;
     public ArrayList m_refOrgDescTill = null;
     public String m_default;
     public ColumnMappingHelper m_colMap;
     
     public String m_catName;
     public int m_qtyType = 0;
     public int m_refMeasureId = Misc.getUndefInt();
     public int m_ignorePastFuture = 0; //-1 => ignore past, 1=> future. while saving data it will incorporate these
     public int m_editPastEditMon = 0;
     public int m_editFutureEditMon = 0;
     public double m_scale = 1;
     public String m_unitString = "";
     public int getQtyType() { return m_qtyType; }
     public ArrayList m_classifyDimListDimInfo = new ArrayList(); //ArrayList of DimInfo //excludes project level filters
     public ArrayList m_classifyDimListInteger = new ArrayList(); //ArrayList of Integer //excludes project level filter
     public ArrayList m_classifyDimListAsSpecified = new ArrayList(); //ArrayList of MiscInner.Pair (dim, type)
     
     public boolean m_useRepCurrencyByDefault = true;     
     public boolean m_noTime = false;
     public boolean m_isCCBSPerformance = false;
     public int m_lookInThisBaseline = 0; //0 => current, 1=>current baseline, 2=>original baseline
     public String m_sn = null;
     public String m_name = null;
     public int m_currencyList = 1; //0 budget, 1 spot, 2 auth
     public String m_subtype = null;
     public boolean m_useKendo = false;
     public int m_nestedDimIdBehaviour = 0;//0 - none, 1 - auto create, 2 - user provided
     
     public ArrayList getClassifyList() {
        return m_classifyDimListDimInfo;
     }
     public static void readAllClassifications(Element classifyList) {
        for (Node n=classifyList.getFirstChild();n!=null;n=n.getNextSibling()) {
            if (n.getNodeType() != 1)
               continue;
            Element e = (Element) n;
            DimInfo forDim = DimInfo.getDimInfo(Misc.getParamAsInt(e.getAttribute("id")));
            if (forDim != null && forDim.m_id == 8503) {
               int dbg=1;
            }
            if (forDim != null)
               forDim.readClassifyList(e);
        }
     }
     public void readClassifyList(Element classifyDef) {
        if (classifyDef != null){
           for (org.w3c.dom.Node cln = classifyDef.getFirstChild(); cln != null; cln = cln.getNextSibling()) {
              if (cln.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                 continue;
              }
              org.w3c.dom.Element cle = (org.w3c.dom.Element) cln;
              int clDim = com.ipssi.gen.utils.Misc.getParamAsInt(cle.getAttribute("id"));
              int type = com.ipssi.gen.utils.Misc.getParamAsInt(cle.getAttribute("type"));
              m_classifyDimListAsSpecified.add(new MiscInner.Pair(clDim, type));
              if (type == 1) {//not interested in project level stuff                 
                 continue;
              }
              com.ipssi.gen.utils.DimInfo dimInfo = com.ipssi.gen.utils.DimInfo.getDimInfo(clDim);
              m_classifyDimListDimInfo.add(dimInfo);
              m_classifyDimListInteger.add(new Integer(clDim));
           }
        }        
     }

     public static class DimValList  implements Serializable {
         /**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
        public DimInfo m_dimInfo;
        public ArrayList<ValInfo> useThisForGps = null; //may have null entry to correspond to summation //if speced = -1000 then need to pop with fullValListInfos
        private ArrayList m_valList = new ArrayList();
        private ArrayList m_valListOfValInfo = null;
        public void setValList(ArrayList valList) {
           m_valList = valList;
           m_valListOfValInfo = null;
        }
        public void addValId(int valId) {
           m_valList.add(new Integer(valId));
           m_valListOfValInfo = null;
           m_valListOfValInfo = getValListOfValInfo();
        }
        public ArrayList getValList() {
           return m_valList;
        }
        public ArrayList getValListOfValInfo() {
           if (m_valListOfValInfo != null)
              return m_valListOfValInfo;
             
           if (m_valList == null || m_valList.size() == 0)
              return null;
           ArrayList retval = new ArrayList();
           m_valListOfValInfo = retval;
           for (int i=0,is = m_valList.size();i<is;i++)
              retval.add(m_dimInfo.getValInfo(((Integer)m_valList.get(i)).intValue()));
           return retval;
        }
        static public DimValList readDimValList(Element elem) {
           DimInfo dimInfo = DimInfo.getDimInfo(Misc.getParamAsInt(elem.getAttribute("id")));
           if (dimInfo == null)
              return null;
           DimValList retval = new DimValList();
           retval.m_dimInfo = dimInfo;
           for (Node n = elem.getFirstChild(); n != null; n=n.getNextSibling()) {
              if (n.getNodeType() != 1)
                 continue;
              Element e = (Element) n;
              int v = Misc.getParamAsInt(e.getAttribute("id"));
              if (dimInfo.m_id == 14 || dimInfo.m_id == 95 || dimInfo.m_id == 96 || dimInfo.m_descDataDimId == 123) {
                 retval.m_valList.add(new Integer(v));
              }
              else if (!Misc.isUndef(v)) {
            	  try {
	                 ValInfo valInfo = dimInfo.getValInfo(v);
	                 if (valInfo != null) {
	                    retval.m_valList.add(new Integer(v));
	                 }
            	  }
            	  catch (Exception e2) {
            		  e2.printStackTrace();
            		  //eat it
            	  }
              }
           }
         //  if (retval.m_valList.size() == 0)
         //     return retval;
           return retval;
        }
     }
     public static String getTextQtyType(int qtyType) {
        switch (qtyType) {
           case 0: return "USD";
           case 1: return "";
           case 2: return "Tonnes";
           case 3: return "Count";
           case 4: return "Years";
           default: return "";
        }
     }

     public int getDefaultInt() {
         if (m_id == 5055)
            return Misc.G_BUDGET_ON ? 1 : 0;
         if (m_id == 5079) {
            return Misc.G_BUDGET_YEAR;
         }
         //do for some other constants too ..
         return Misc.getParamAsInt(m_default);
     }
     public double getDefaultDouble() {
         return Misc.getParamAsDouble(m_default, Misc.getUndefDouble());
     }
     public String getDefaultString() {
         if (m_id == 5055)
            return Misc.G_BUDGET_ON ? "1" : "0";
         if (m_id == 5079) {
            return Integer.toString(Misc.G_BUDGET_YEAR);
         }

         return (m_default);
     }
     public java.sql.Date getDefaultDate() {
        if ("today".equals(m_default))
            return (Misc.getTodayDate());
        else
            return null;
     }
    
     public  void initDimInfo() {
        m_rhsDescInfo = null;
        m_lhsParentInfo = null;
        m_valList = null;
        m_refOrgLevel = null;
        m_classifyDimListDimInfo = new ArrayList();
        m_classifyDimListInteger = new ArrayList();
        m_classifyDimListAsSpecified = new ArrayList();
        this.m_nestedDimIdBehaviour = 0;
     }
     public ValInfo copyValInfo(ValInfo copyFrom) {
        
         ValInfo retval = new ValInfo();
         if (copyFrom.m_otherProp != null)
            retval.m_otherProp = (ArrayList) copyFrom.m_otherProp.clone();
         retval.m_id = copyFrom.m_id;
         retval.m_name = copyFrom.m_name;
         retval.m_sn = copyFrom.m_sn;
         retval.m_isSp = copyFrom.m_isSp;
         addValInfo(retval);
         
         return retval;
     }
     public void addValInfo(ValInfo val) {
        if (m_valList == null)
           m_valList = new ArrayList(5);
        m_valList.add(val);
     }
     public static DimInfo addDimInfo(Connection dbConn, String catName, int id, ColumnMappingHelper colMap, Element xmlInfo, int attribType, String defaultVal, int qtyType, int descDataDimId, String refOrgLevel, int refMeasureId, int ignorePastFuture, int editPastMon, int editFutureMon, double scale, String unitString, boolean useRepCurrencyByDefault, boolean noTime, String name, String sn, int subsetOf, Element refInInternal, String refOrgDescTill, String refOrgAncTill, String subType) throws Exception {
         
         if (ignorePastFuture == 1) { //ignore future
            if (editPastMon == 0)
               editPastMon = 1;
            if (editFutureMon > 0)
               editFutureMon = 0;
         }
         else if (ignorePastFuture == -1) {
            if (editFutureMon == 0)
               editFutureMon = 1;
            if (editPastMon > 0)
               editPastMon = 0;
         }
         DimInfo dimInfo = DimInfo.getDimInfo(id);
         boolean isNew = dimInfo == null;
         if (isNew)
            dimInfo = new DimInfo();
         else {
            dimInfo.initDimInfo();
         }
         dimInfo.m_id = id;
         dimInfo.m_name = name;
         dimInfo.m_sn = sn;
         if (dimInfo.m_name == null)
            dimInfo.m_name = dimInfo.m_catName;
         if (dimInfo.m_sn == null)
            dimInfo.m_sn = dimInfo.m_name;
         dimInfo.m_subsetOf = subsetOf;
         if (Misc.isUndef(dimInfo.m_subsetOf))
            dimInfo.m_subsetOf = dimInfo.m_id;
         
         dimInfo.m_colMap = colMap;
         dimInfo.m_type = attribType;
         Element attribNode = xmlInfo;
         if (attribNode != null) {
            dimInfo.m_valList = ValInfo.readLovNode(attribNode);            
            dimInfo.m_subsetOf = Misc.getParamAsInt(attribNode.getAttribute("subset_of"), dimInfo.m_id);
         }
         if (subType == null || subType.length() == 0)
             subType = "20508"; //normal data
         dimInfo.m_subtype = subType;
         dimInfo.m_catName = catName;
         if (!Misc.isUndef(id))
            g_dimList.put(new Integer(id), dimInfo);
         if (catName != null && catName.length() != 0)
         g_dimListByCatName.put(catName, dimInfo);
         if (defaultVal == null || defaultVal.length() == 0) {
            defaultVal = dimInfo.m_valList == null || dimInfo.m_valList.size() == 0 ? null : Integer.toString(((ValInfo)dimInfo.m_valList.get(0)).m_id);
         }
         
         dimInfo.m_default = defaultVal;
         dimInfo.m_qtyType = qtyType;
         dimInfo.m_descDataDimId = descDataDimId;
         if (refOrgLevel != null && refOrgLevel.length() != 0) {
            dimInfo.m_refOrgLevel = new ArrayList();
            Misc.convertValToVector(refOrgLevel, dimInfo.m_refOrgLevel);
         }
         if (refOrgDescTill != null) {
            dimInfo.m_refOrgDescTill = new ArrayList();
            Misc.convertValToVector(refOrgDescTill, dimInfo.m_refOrgDescTill);
         }
         if (refOrgAncTill != null) {
            dimInfo.m_refOrgAncTill = new ArrayList();
            Misc.convertValToVector(refOrgAncTill, dimInfo.m_refOrgAncTill);
         }
         if (Misc.isUndef(refMeasureId))
            refMeasureId = id;
         dimInfo.m_refMeasureId = refMeasureId;
         dimInfo.m_ignorePastFuture = ignorePastFuture;
         dimInfo.m_editPastEditMon = editPastMon;
         dimInfo.m_editFutureEditMon = editFutureMon;
         if (Misc.isUndef(scale) || scale < 0)
            scale = 1;
         dimInfo.m_scale = scale;
         if (unitString == null)
            unitString = "";
         dimInfo.m_unitString = unitString;
         dimInfo.m_useRepCurrencyByDefault = useRepCurrencyByDefault;
         if (dimInfo.m_colMap != null && "npv".equals(dimInfo.m_colMap.table) || noTime)
            dimInfo.m_noTime = true;
         dimInfo.m_nestedDimIdBehaviour = refInInternal != null ? Misc.getParamAsInt(refInInternal.getAttribute("nested_dim_behaviour"), 0) : 0;
         if (refInInternal != null) {
            dimInfo.m_currencyList = Misc.getParamAsInt(refInInternal.getAttribute("currency_rate_list"),1);
         
        	 dimInfo.m_loadFromDB = (byte)Misc.getParamAsInt(refInInternal.getAttribute("load_from_db"),0);
        	 dimInfo.setMinVal(Misc.getParamAsInt(refInInternal.getAttribute("min_val")));
        	 dimInfo.setMaxVal(Misc.getParamAsInt(refInInternal.getAttribute("max_val")));
        	 dimInfo.setAllowedPattern(Misc.getParamAsString(refInInternal.getAttribute("allowed_pattern"), null));
            String tag = refInInternal.getAttribute("acc_check");
            if (tag != null && tag.length() != 0) {
               g_tempDimInfoListRequiringAccessCheck.add(dimInfo);
               dimInfo.m_tempAccTag = tag;
               dimInfo.m_accCheckonObj = Misc.getParamAsInt(refInInternal.getAttribute("acc_check_obj_type"), Misc.G_FOR_ORDER);
            }
            String tt = refInInternal.getAttribute("read_check");
            if (tt != null && tt.length() != 0)
            	dimInfo.m_readTagNew = tt;
            tt = refInInternal.getAttribute("write_check");
            if (tt != null && tt.length() != 0)
            	dimInfo.m_writeTagNew = tt;
            
            dimInfo.m_getQuery = Misc.getParamAsString(refInInternal.getAttribute("dyn_query"),null);
            dimInfo.m_lovListRequiresDynamicOrg = "1".equals(refInInternal.getAttribute("dyn_query_org_dependent"));
         }
                  
    	 if (dimInfo.m_getQuery != null && !dimInfo.m_lovListRequiresDynamicOrg)
    		 dimInfo.checkAndPopulateLovListFromDB(dbConn, null);
         
         return dimInfo;

     }

     public boolean inRefOrg(int orgType) {
         return Misc.isInList(m_refOrgLevel, orgType);
     }
     
     public boolean inRefAncTill(int orgType) {
        return m_refOrgAncTill == null || Misc.isInList(m_refOrgAncTill, orgType);
     }
     
     public boolean inRefDescTill(int orgType) {
        return m_refOrgDescTill != null && Misc.isInList(m_refOrgDescTill, orgType);
     }

     public static DimInfo getDimInfo(int id) {
        return (DimInfo) g_dimList.get(new Integer(id));
     }

     public static DimInfo getDimInfo(String catName) {
        return (DimInfo) g_dimListByCatName.get(catName);
     }
     public ArrayList getValList() {
        if (m_valList != null)
           return m_valList;
        if (m_id != m_subsetOf && !Misc.isUndef(m_subsetOf) && (m_type == Cache.LOV_NO_VAL_TYPE || m_type == Cache.LOV_TYPE)) {           
           DimInfo d = DimInfo.getDimInfo(m_subsetOf);
           return d.getValList();
        }
        return null;
     }
     public ArrayList getValList(Connection conn, SessionManager session) throws Exception {
    	 ArrayList  retval = checkAndPopulateLovListFromDB(conn,session);
    	 if ((retval == null || retval.size() == 0) && (!Misc.isUndef(this.m_subsetOf)) && this.m_subsetOf != this.m_id && (this.m_getQuery == null)) {
    		 DimInfo d = DimInfo.getDimInfo(this.m_subsetOf);
    		 retval = d == null ? null : d.getValList(conn, session);
    	 }
    	 return retval;    	 
     }
     
     public ValInfo getValInfo(int id, Connection conn, SessionManager session) throws Exception {
    	 ArrayList valList = getValList(conn, session);
    	 for (int i=0,is = valList == null ? 0:valList.size();i<is;i++) {
             ValInfo valInfo = (ValInfo) valList.get(i);
             if (valInfo.m_id == id)
                return valInfo;
        }
    	 return null;
     }
     
     public ValInfo getValInfo(int id) {
        ArrayList valList = getValList();
        for (int i=0,is = valList == null ? 0:valList.size();i<is;i++) {
             ValInfo valInfo = (ValInfo) valList.get(i);
             if (valInfo.m_id == id)
                return valInfo;
        }
        return null;
     }
     
     public ValInfo getValInfo(String name) {
        if (name == null)
           return null;
        ArrayList valList = getValList();
        for (int i=0,is = valList == null ? 0:valList.size();i<is;i++) {
             ValInfo valInfo = (ValInfo) valList.get(i);
             if (name.equalsIgnoreCase(valInfo.m_name))
                return valInfo;
        }        
        return null;
     }
	public int getMinVal() {
		return minVal;
	}
	public void setMinVal(int minVal) {
		this.minVal = minVal;
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

     
  }
