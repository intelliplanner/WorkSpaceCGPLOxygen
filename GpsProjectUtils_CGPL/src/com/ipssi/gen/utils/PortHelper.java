/******* NOT BEING USED ANYMORE *************/
// Copyright (c) 2000 IntelliPlanner Software Systems, Inc.
package com.ipssi.gen.utils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import com.ipssi.gen.exception.ExceptionMessages;



public  class PortHelper extends Object {
	private HttpServletRequest request = null;
	private ServletContext context = null;
	private SessionManager session = null;
	private User user = null;
	private Cache cache = null;
	private Logger log = null;
	private Connection dbConn = null;
	
	org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PortHelper.class);

	//Here is how it works ... see revision_port_wksp.txt
	//
	///// End of routines to do current and baseline comparisons

  public void deleteOrg(int portfolioId) throws Exception {
     try {
        PreparedStatement cs = dbConn.prepareStatement(Queries.UPDATE_PRJ_PORT_MAP);
        /*cs.setInt(1, (int)Misc.UNASSIGNED_PORTFOLIO_ID);
        cs.setInt(2, 2);
        cs.setInt(3, portfolioId);
        cs.execute();
        cs.close();*/
        cs = dbConn.prepareStatement(Queries.DEL_PORT_NODE);
        cs.setInt(1, portfolioId);
        cs.execute();
        cs.close();
        /*cs = dbConn.prepareStatement(Queries.DEL_ALLOWED_CURRENCIES);
        cs.setInt(1, portfolioId);
        cs.execute();
        cs.close();*/
        cache.makePortTreeDirty();
        CacheTrack.VehicleSetup.makeDistCalcControlDirtyForAll();
        cache.getPortTree(dbConn); //so that next one doesn't get hit
        cache.updLHSRHSPortTree(dbConn);
     }
     catch (Exception e) {
        e.printStackTrace();
        throw e;
     }
  }

  public int addOrg(int parentPortfolioId, String sn, String fn, String desc, int defCurrency, ArrayList otherCurrencies, String dateFormat, int repCurrency, int currencyUnit, int orgType, String externalCode, double threshold, int level, int countryCode, int localeId, double defaultTh1, double defaultTh2, double defaultTh3, double defaultTh4, int classify1, int classify2, int classify3, int classify4, int classify5, String strField1, String strField2, String strField3, String strField4, String strField5) throws Exception {
     try {
        //level
        if (level < 0)
           level = 0;
	   int param = 1;
    PreparedStatement cs = Misc.G_DO_ORACLE ? dbConn.prepareStatement(Queries.INSERT_PORT_NODES):dbConn.prepareStatement(Queries.INSERT_PORT_NODES,Statement.RETURN_GENERATED_KEYS);
		int portfolioId = Misc.getUndefInt();
		if (Misc.G_DO_ORACLE) {
			portfolioId = (int)Misc.getNextId(dbConn, Sequence.SEQ_PORT_NODES);
			cs.setInt(param++, portfolioId);
		}

        if (fn == null || fn.length() == 0)
           fn = sn;
        if (sn == null || sn.length() == 0)
           sn = fn;
        if (sn == null || sn.length() == 0)
          sn ="<New Org>";
        if (fn == null || fn.length() == 0)
          fn = "<New Org>";
        if (desc == null)
          desc = "";
        if (dateFormat == null || dateFormat.length() == 0)
           dateFormat = Misc.G_DEFAULT_DATE_FORMAT;
        if (Misc.isUndef(orgType))
           orgType = 0;
        if (Misc.isUndef(currencyUnit))
           currencyUnit = 1;

	   cs.setString(param++, sn);
        cs.setString(param++, fn);
		cs.setString(param++, desc);
		Misc.setParamInt(cs, parentPortfolioId, param++);
		Misc.setParamInt(cs, defCurrency, param++);
		cs.setString(param++, dateFormat);
		Misc.setParamInt(cs, repCurrency, param++);
		cs.setInt(param++, orgType);
		Misc.setParamDouble(cs, threshold, param++);
		cs.setString(param++, externalCode);
		cs.setInt(param++, currencyUnit);
		cs.setInt(param++, level + 1);
		Misc.setParamInt(cs, countryCode, param++);
		Misc.setParamInt(cs, localeId, param++);
		Misc.setParamDouble(cs, defaultTh1, param++);
		Misc.setParamDouble(cs, defaultTh2, param++);
		Misc.setParamDouble(cs, defaultTh3, param++);
		Misc.setParamDouble(cs, defaultTh4, param++);
		Misc.setParamInt(cs, classify1, param++);
		Misc.setParamInt(cs, classify2, param++);
		Misc.setParamInt(cs, classify3, param++);
		Misc.setParamInt(cs, classify4, param++);
		Misc.setParamInt(cs, classify5, param++);
		cs.setString(param++, strField1);
		cs.setString(param++, strField2);
		cs.setString(param++, strField3);
		cs.setString(param++, strField4);
		cs.setString(param++, strField5);
		if (Misc.G_DO_ORACLE)
			Misc.executeGetId(cs, false, null);
		else
			portfolioId = Misc.executeGetId(cs, true, Queries.INSERT_PORT_NODES);
		Misc.closeStatement(cs);

        cs = dbConn.prepareStatement(Queries.INSERT_ALLOWED_CURRENCIES);
        cs.setInt(1, portfolioId);
        for (int i=0,is = otherCurrencies == null ? 0 : otherCurrencies.size();i<is;i++) {
            cs.setInt(2, ((Integer)otherCurrencies.get(i)).intValue());
            cs.execute();
        }
        cs.close();
        cache.makePortTreeDirty();
        CacheTrack.VehicleSetup.makeDistCalcControlDirtyForAll();
//      updatePrjDefaultCurrencyCalc(dbConn); rewrite for MYSQL

		cache.getPortTree(dbConn); //so that next one doesn't get hit
    cache.updLHSRHSPortTree(dbConn);
        return portfolioId;
     }
     catch (Exception e) {
        e.printStackTrace();
        throw e;
     }
  }

  public void updateOrg(int portfolioId, String sn, String fn, String desc, int defCurrency, ArrayList otherCurrencies, String dateFormat, int repCurrency, int currencyUnit, int orgType, String externalCode, double threshold, int level, int countryCode, int localeId, double defaultTh1, double defaultTh2, double defaultTh3, double defaultTh4, int classify1, int classify2, int classify3, int classify4, int classify5, String strField1, String strField2, String strField3, String strField4, String strField5) throws Exception {
     try {
        if (level < 0)
           level = 0;
        PreparedStatement cs = null;
        cs = dbConn.prepareStatement(Queries.DEL_ALLOWED_CURRENCIES);
        cs.setInt(1, portfolioId);
        cs.execute();
        cs.close();
        cs = dbConn.prepareStatement(Queries.UPDATE_PORT_NODES);
        if (fn == null || fn.length() == 0)
           fn = sn;
        if (sn == null || sn.length() == 0)
           sn = fn;
        if (sn == null)
          sn ="<New Org>";
        if (fn == null)
          fn = "<New Org>";
        if (desc == null)
          desc = "";
        if (dateFormat == null || dateFormat.length() == 0)
          dateFormat = Misc.G_DEFAULT_DATE_FORMAT;
        
        if (Misc.isUndef(orgType))
           orgType = 0;
        if (Misc.isUndef(currencyUnit))
           currencyUnit = 1;

        cs.setString(1, sn);
        cs.setString(2, fn);
        cs.setString(3, desc);
        Misc.setParamInt(cs, defCurrency, 4);
        cs.setString(5, dateFormat);
        Misc.setParamInt(cs, repCurrency, 6);
        cs.setInt(7, orgType);
        Misc.setParamDouble(cs, threshold, 8);
        cs.setString(9, externalCode);
        cs.setInt(10, currencyUnit);
        cs.setInt(11, level);
        Misc.setParamInt(cs, countryCode, 12);
        Misc.setParamInt(cs, localeId, 13);
        Misc.setParamDouble(cs, defaultTh1, 14);
        Misc.setParamDouble(cs, defaultTh2, 15);
        Misc.setParamDouble(cs, defaultTh3, 16);
        Misc.setParamDouble(cs, defaultTh4, 17);
        Misc.setParamInt(cs, classify1, 18);
        Misc.setParamInt(cs, classify2, 19);
        Misc.setParamInt(cs, classify3, 20);
        Misc.setParamInt(cs, classify4, 21);
        Misc.setParamInt(cs, classify5, 22);
        cs.setString(23, strField1);
        cs.setString(24, strField2);
        cs.setString(25, strField3);
        cs.setString(26, strField4);
        cs.setString(27, strField5);
        cs.setInt(28, portfolioId);
        

        cs.execute();
        cs.close();
        
        cs = dbConn.prepareStatement(Queries.INSERT_ALLOWED_CURRENCIES);
        cs.setInt(1, portfolioId);
        for (int i=0,is = otherCurrencies == null ? 0 : otherCurrencies.size();i<is;i++) {
            cs.setInt(2, ((Integer)otherCurrencies.get(i)).intValue());
            cs.execute();
        }
        cs.close();
		cache.makePortTreeDirty();
		CacheTrack.VehicleSetup.makeDistCalcControlDirtyForAll();
//		updatePrjDefaultCurrencyCalc(dbConn);
//		updatePrjPortfolioMap(dbConn);
		cache.getPortTree(dbConn); //so that next one doesn't get hit
    //no need to to update LHS/RHS in this case
     }
     catch (Exception e) {
        e.printStackTrace();
        throw e;
     }
  }

  public ArrayList helperGetCurrencies() {
     ArrayList retval = new ArrayList();
     java.lang.String[] vals = request.getParameterValues("currency_sel");
     for (int i=0,is = vals.length;i<is;i++) {
        int v = Misc.getParamAsInt(vals[i]);
        if (v >= 0)
           retval.add(new Integer(v));
     }
     return retval;
  }

 
	

     public static void getCurrChangeList(Connection dbConn, Cache cache, Logger log, JspWriter out) throws Exception {
          try {
             PreparedStatement pStmt = dbConn.prepareStatement(Queries.GET_CURR_CHANGE_LIST);
             ResultSet rset = pStmt.executeQuery();
             out.println("<data>");
             while (rset.next()) {
                int id = rset.getInt(1);
                out.println("<p i=\""+Integer.toString(id)+"\"/>");
             }
             out.println("</data>");
             rset.close();
             pStmt.close();
             if (false) {
                 PreparedStatement cStmt = dbConn.prepareStatement(Queries.DEL_CURR_CHANGE_LIST);
                 cStmt.execute();
                 cStmt.close();
             }
          }
          catch (Exception e) {
             e.printStackTrace();
             throw e;
          }
     }

	
	public PortHelper(HttpServletRequest request, ServletContext context) {
		this.request = request;
		this.context = context;
		this.session = InitHelper.helpGetSession(request);
		
		this.cache = session.getCache();
		this.user = session.getUser();
		this.log = (Logger) request.getAttribute("_log");
		this.dbConn = session.getConnection();
	}

	
	public static void updatePrjDefaultCurrencyCalc(Connection dbConn) throws Exception {
		try {
			PreparedStatement cs = dbConn.prepareStatement(Queries.ORG_DEF_SETUP_TOP);
			cs.execute();
			cs.close();
      
			cs = dbConn.prepareStatement(Queries.ORG_DEF_CURR_SETUP);
			cs.execute();
			cs.close();
      

			PreparedStatement ps = null;
			cs = dbConn.prepareStatement(Queries.ORG_DEF_UPDATE_BUD_CURRENCY);
			ps = dbConn.prepareStatement(Queries.ORG_DEF_CHECK_BUD_CURRENCY);
      int maxLoop = 10;
      int loopCount = 0;
			while (Misc.execIfExistStmt(ps)) {
				cs.execute();
        
        loopCount++;
        if (loopCount > maxLoop)
           break;
			}
			cs.close();
			ps.close();

			cs = dbConn.prepareStatement(Queries.ORG_DEF_UPDATE_REP_CURRENCY);
			ps = dbConn.prepareStatement(Queries.ORG_DEF_CHECK_REP_CURRENCY);
      loopCount = 0;
			while (Misc.execIfExistStmt(ps)) {
				cs.execute();
        
        loopCount++;
        if (loopCount > maxLoop)
           break;
			}
			cs.close();
			ps.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public static void updatePrjPortfolioMap(Connection dbConn) throws Exception {//misnomer will just update the legalentity_etc_update
		try {
			PreparedStatement cs = dbConn.prepareStatement(Queries.MAP_DELETE_ALL);
			cs.execute();
			cs.close();
			cs = dbConn.prepareStatement(Queries.MAP_INITIALIZE_MULTI);
			cs.execute();
			cs.close();
			PreparedStatement ps = dbConn.prepareStatement(Queries.MAP_CHECK_IF_MORE_UPDATES);
			PreparedStatement ins = dbConn.prepareStatement(Queries.MAP_INSERT_PARENT);
			PreparedStatement updf = dbConn.prepareStatement(Queries.MAP_UPDATE_FLAG);
      
			while (Misc.execIfExistStmt(ps)) {
				ins.execute();
        
				updf.execute();
        
			}
			ps.close();
			ins.close();
			updf.close();
      PreparedStatement dupli = null;
      dupli = dbConn.prepareStatement(Queries.UPDATE_PRJ_PORTFOLIO_MAP_FOR_DUPLI_STEP1);
      dupli.execute();
      dupli.close();
      dupli = dbConn.prepareStatement(Queries.UPDATE_PRJ_PORTFOLIO_MAP_FOR_DUPLI_STEP2);
      dupli.execute();
      dupli.close();
      dupli = dbConn.prepareStatement(Queries.UPDATE_PRJ_PORTFOLIO_MAP_FOR_DUPLI_STEP3);
      dupli.execute();
      dupli.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
