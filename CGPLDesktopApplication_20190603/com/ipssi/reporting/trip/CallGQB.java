package com.ipssi.reporting.trip;

import java.sql.Connection;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.PageHeader;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.User;
import com.ipssi.gen.utils.MiscInner.SearchBoxHelper;
import com.ipssi.reporting.cache.CacheManager;

public class CallGQB {
	public static Table  getTable(SessionManager _session, String pgContext, String frontPageName, int portNodeId, SearchBoxHelper searchBoxHelper) {
		  try {
			  _session.rememberSessionVars();
			  Cache _cache = _session.getCache();
			  Connection _dbConnection = _session.getConnection();
			  User _user = _session.getUser();
			  
			 
			  FrontPageInfo fPageInfo = CacheManager.getFrontPageConfig(_dbConnection , _user.getUserId(), portNodeId, pgContext, frontPageName, 0, 0);
			  SearchBoxHelper searchBoxHelperNested = PageHeader.processSearchBox(_session, searchBoxHelper.m_privIdForOrg, pgContext, fPageInfo.m_frontSearchCriteria, null);			  
			  GeneralizedQueryBuilder.setWorkflowTypeInSession(_session, searchBoxHelper); //HACK to handle object_type and from there getting applicable worjflow types
			  GeneralizedQueryBuilder.setPassedObjectInSession(_session);
			  GeneralizedQueryBuilder qb = new GeneralizedQueryBuilder();
			  Table nestedTable = qb.corePrintPage(_dbConnection, null, Misc.XML, null, frontPageName, fPageInfo, _session, searchBoxHelperNested, null, null, null);
			  return nestedTable;
		  }
		  catch (Exception e) {
			  e.printStackTrace();
			  //eat it
		  }
		  finally {
			  _session.setToRememberedVars();//for re-entrancy
		  }
		  return null;
	}
}
