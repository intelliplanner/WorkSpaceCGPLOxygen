package com.ipssi.reporting.reportForMenu;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.MenuItem;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.reporting.common.util.Common;
import com.ipssi.reporting.customize.CustomizeDao;
import com.ipssi.reporting.customize.MenuBean;
import com.ipssi.reporting.customize.UIColumnBean;
import com.ipssi.reporting.customize.UIParameterBean;
import com.ipssi.tracker.common.db.DBQueries;

public class MenuReportDao {

	private SessionManager m_session;

	public MenuReportDao(SessionManager m_session) {
		this.m_session = m_session;
	}
	
	public ArrayList<MenuReportBean> getSearchData(int reportType,String optionalReportName, String reportName, int status, int menuPlaceHolderId,int pv123){
		ArrayList<MenuReportBean> list = new ArrayList<MenuReportBean>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		MenuReportBean bean = null;
		String query = reportType == 0 ? DBQueries.MENUREPORT.GET_MENU_REPORT_LISTS : DBQueries.MENUREPORT.GET_ONLINE_REPORT_LIST;
		//StringBuffer fromClause = new StringBuffer(" from report_definitions");
		//StringBuffer selectClause = new StringBuffer("select id,optional_menu_name,name,status,help ");
		StringBuffer whereClause = new StringBuffer("");
		if (!Misc.isUndef(menuPlaceHolderId)) { 
			//selectClause.append(",menu_placeholder_id ");
			//fromClause.append(" join menu_report_definition on (report_definitions.id = menu_report_definition.report_definition_id) ");
			whereClause.append("and menu_report_definition.menu_placeholder_id ="+menuPlaceHolderId +" ");
		}
		if (!"".equalsIgnoreCase(optionalReportName.trim())) {
			whereClause.append("and report_definitions.optional_menu_name like '%"+optionalReportName +"%'  ");
		}
		if (!"".equalsIgnoreCase(reportName.trim())) {
			whereClause.append("and report_definitions.name like '%"+reportName+"%' ");
			
		}
		if (!Misc.isUndef(status)) {
			whereClause.append("and report_definitions.status = "+status+" ");
		}
		if (!Misc.isUndef(reportType)) {
//			whereClause.append("type = "+reportType);
		}
		try {
			//query = selectClause .append( fromClause) .append( whereClause).toString();
			query += whereClause.toString();
			conn = m_session.getConnection();
			ps = conn.prepareStatement(query);
			ps.setInt(1, pv123);
			ps.setInt(2, reportType);
		    rs = ps.executeQuery();
			while(rs.next()){
				int count = 1;
		    	bean = new MenuReportBean();
		    	bean.setReportId(rs.getLong(count++));
		    	if (reportType == 0) {
		    		bean.setMenuPlaceHolderId(rs.getInt(count++));	
				}
		    	bean.setMenuTitle(rs.getString(count++));
		    	bean.setStatus(rs.getInt(count++));
		    	bean.setHelp(rs.getString(count++));
		    	bean.setOptionMenuTagName(rs.getString(count++));
		    	bean.setMenuTagName(rs.getString(count++));
		    	bean.setConfigFile(rs.getString(count++));
		    	bean.setOrgId(Misc.getRsetInt(rs, count++));
		    	bean.setMenuMasterId(Misc.getRsetInt(rs, count++));
		    	list.add(bean);
				/*bean = new MenuReportBean();
				if (!Misc.isUndef(menuPlaceHolderId)) {
					bean.setMenuPlaceHolderId(rs.getInt("menu_placeholder_id"));	
				}
				bean.setOptionMenuTagName(rs.getString("optional_menu_name"));
				bean.setMenuTagName(rs.getString("name"));
				bean.setReportId(rs.getInt("id"));
				bean.setHelp(rs.getString("help"));
				bean.setStatus(rs.getInt("status"));
				list.add(bean);	*/
			}
			return list;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	public MenuReportBean getAutoEmailDetailList(int autoEmailId){
		MenuReportBean bean = new MenuReportBean();
		MenuReportBean.ReprtGroupEmailAndFrequency subBean = new MenuReportBean.ReprtGroupEmailAndFrequency();
		PreparedStatement ps = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
			//select name,report_format,status from email_report_groups where id = 2;
		//	select granularity,hours,minutes from email_report_frequencies where email_report_group_id=3;
		//	select email from email_report_users where email_report_group_id = 3;
			conn = m_session.getConnection();
			ps = conn.prepareStatement("select name,report_format,status from email_report_groups where id = ?");
			ps.setInt(1, autoEmailId);
			rs = ps.executeQuery();
			while (rs.next()) {
            subBean.setEmailReportName(rs.getString(1));
            subBean.setEmail_report_format(rs.getInt(2));
            subBean.setStatus(rs.getInt(3));
			}
			bean.setEmailReportGroupInformation(subBean);
			
			ArrayList frequencyList = new ArrayList();
			ps = conn.prepareStatement("select granularity,hours,minutes from email_report_frequencies where email_report_group_id = ?");
			ps.setInt(1, autoEmailId);
			rs = ps.executeQuery();
			while (rs.next()) {
			subBean = new MenuReportBean.ReprtGroupEmailAndFrequency();
            subBean.setGranularity(rs.getInt(1));
            subBean.setShiftHours(rs.getInt(2));
            subBean.setShiftMinutes(rs.getInt(3));
            frequencyList.add(subBean);
			}
			bean.setReportFrequencyList(frequencyList);
			ArrayList userList = new ArrayList();
			ps = conn.prepareStatement("select email from email_report_users where email_report_group_id = ?");
			ps.setInt(1, autoEmailId);
			rs = ps.executeQuery();
			while (rs.next()) {
				subBean = new MenuReportBean.ReprtGroupEmailAndFrequency();
			    subBean.setEmailReportName(rs.getString(1));
			    userList.add(subBean);	
			}
			bean.setReportUserEmailList(userList);
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
					
					e.printStackTrace();
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	public void saveNewAutoEmailGroup(String emailGroupName , int emailFormat, int emailStatus , String emailFrequencyData, String emailUserData, int emailGroupEditId){
		int emailReportId = Misc.getUndefInt();
		PreparedStatement ps = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
            conn = m_session.getConnection();
            ps = Misc.isUndef(emailGroupEditId) ? conn.prepareStatement(DBQueries.MENUREPORT.INSERT_EMAIL_REPORT_GROUPS,Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(DBQueries.MENUREPORT.UPDATE_EMAIL_REPORT_GROUPS);
            //name,report_format,status,report_definition_id
            //name = ? ,report_format = ? ,status = ?,report_definition_id = ? where id = ?
            ps.setString(1, emailGroupName);
            ps.setInt(2, emailFormat);
            ps.setInt(3, emailStatus);
            ps.setNull(4, java.sql.Types.INTEGER);
            
            if (Misc.isUndef(emailGroupEditId)) {
            	int affectedRow = ps.executeUpdate();
    			if (affectedRow == 0) {
    				throw new SQLException("Insertion failed, no rows affected.");
    			}
    			rs = ps.getGeneratedKeys();
    			if (rs.next()) {
    				emailReportId = rs.getInt(1);
    				emailGroupEditId = emailReportId;
    			} else {
    				throw new SQLException("Insertion failed, no rows affected.");
    		}	
			}else{
				ps.setInt(5, emailGroupEditId);
				ps.executeUpdate();
			}
			
            if (!Misc.isUndef(emailGroupEditId)) {
				ps = conn.prepareStatement(DBQueries.MENUREPORT.DELETE_EMAIL_REPORT_USERS);
				ps.setInt(1, emailGroupEditId);
				ps.execute();
			}
			if (!Misc.isUndef(emailGroupEditId)) {
				ps = conn.prepareStatement(DBQueries.MENUREPORT.DELETE_EMAIL_REPORT_FREQUENCIES);
				ps.setInt(1, emailGroupEditId);
				ps.execute();
			}
            
			if (!"".equalsIgnoreCase(emailUserData)) {
				insertUserEmailList(conn, ps, emailUserData, emailGroupEditId);
				
			}
			
			if (!"".equalsIgnoreCase(emailFrequencyData)) {
				
				insetFrequencyData(emailFrequencyData, conn, ps, emailGroupEditId);
			}	
		} catch (Exception e) {
           e.printStackTrace();
		}
		
	}
	public ArrayList getAllAutoEmailList(){
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
       ArrayList list = new ArrayList();
       MenuReportBean bean = null;
		try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.MENUREPORT.GET_ALL_EMAIL_REPORT_GROUP);
			rs = ps.executeQuery();
			while (rs.next()) {
			bean = new MenuReportBean();
			bean.setMenuPlaceHolderId(rs.getInt(1));
			bean.setMenuTagName(rs.getString(2));
			bean.setStatus(rs.getInt(3));
			list.add(bean);
			}
		} catch (Exception e) {
		     e.printStackTrace();
		}finally{
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	return list;	
	}
	public MenuReportBean getMenuReportInformation(int reportId){
		MenuReportBean bean = new MenuReportBean();
		PreparedStatement ps = null;
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.MENUREPORT.GET_MENU_REPORT_INFORMATION);
			ps.setInt(1, reportId);
		    rs = ps.executeQuery();
		    while (rs.next()) {
				bean.setOptionMenuTagName(rs.getString(1));
				bean.setMenuTagName(rs.getString(2));
				bean.setMenuTitle(rs.getString(3));
				bean.setOrgId(rs.getInt(4));
				bean.setUserId(rs.getInt(5));
				bean.setMenuPlaceHolderId(rs.getInt(6));
				bean.setConfigFile(rs.getString(7));
				bean.setUrl(rs.getString(8));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return bean;
	}
	
	public MenuReportBean parseMenuReportField(String menuField, MenuReportBean bean, int reportTye){
		
		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(menuField);
		org.w3c.dom.NodeList nList = xmlDoc.getElementsByTagName("MainTag");
		int size = nList.getLength();
		
		int status = Misc.getUndefInt();
		for (int i = 0; i < size; i++) {
			org.w3c.dom.Node n = nList.item(i);
			org.w3c.dom.Element e = (org.w3c.dom.Element) n;
			int menuplaceHolderId =  Misc.getParamAsInt(e.getAttribute("menuplaceholder"));
			bean.setMenuPlaceHolderId(menuplaceHolderId);
			 
			String optionalMenuGroupTagName = Misc.getParamAsString(e.getAttribute("optionalmenugroup"));
			if ("".equalsIgnoreCase(optionalMenuGroupTagName)) {
				optionalMenuGroupTagName = null;
			}
			bean.setOptionMenuTagName(optionalMenuGroupTagName);
			String menuTagName = Misc.getParamAsString(e.getAttribute("menuname")); 
			bean.setMenuTagName(menuTagName);
			String menuTitleName = Misc.getParamAsString(e.getAttribute("reporttitle"));
			if ("".equalsIgnoreCase(menuTitleName)) {
				menuTitleName = null;
			}
			bean.setMenuTitle(menuTitleName);
            String help = Misc.getParamAsString( e.getAttribute("help"));
            bean.setHelp(help);
            int noData = Misc.getParamAsString( e.getAttribute("no_data")).equalsIgnoreCase("true") ? 1 : 0;
            bean.setNodata(noData);
            int senderProfile = Misc.getParamAsInt(e.getAttribute("sender_profile"));
            bean.setSenderProfile(senderProfile);
            bean.setType(reportTye);
			status = Misc.getParamAsInt(e.getAttribute("reportstatus"));
			bean.setStatus(status);
			
			int orgId = Misc.getParamAsInt(e.getAttribute("fororg"));
			
			bean.setOrgId(orgId);
			int userId = Misc.getParamAsInt(e.getAttribute("foruser"));
			bean.setUserId(userId);
		}
		return bean;
	}
	
	public void getMenuURLConfigFile(String oldMenuTag,MenuReportBean bean){
		MenuItem oldMenuItemInformation = MenuItem.getMenuInfo(oldMenuTag);
        String m_url = null;
        String configFile = null;
        if (oldMenuItemInformation != null) {
		synchronized (oldMenuItemInformation) {
		m_url = oldMenuItemInformation.m_url;    
	    ArrayList oldMenuParamItem = oldMenuItemInformation.m_params;
	    int paramSize = oldMenuParamItem == null ? 0 : oldMenuParamItem.size();
	    if(oldMenuParamItem != null && paramSize > 0 ){
	    	for(int i = 0 ; i < paramSize ; i++){
	    		MenuItem.Param param = (MenuItem.Param)oldMenuParamItem.get(i);
	    		if(param.m_name.equalsIgnoreCase("front_page")){
	    			configFile = param.m_value;
	    			break;
	    		}
	    	}
	    }
	}
     bean.setUrl(m_url);
     bean.setConfigFile(configFile);
   }
}
	public void updateMenuTagMenuMaster(String menuTag, int menuMasterId, int orgId, int userId){
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.MENUREPORT.UPDATE_MENU_MASTER_MENU_TAG);
			ps.setString(1, menuTag);
			if (Misc.isUndef(orgId)) {
				ps.setNull(2, java.sql.Types.INTEGER);
			}else{
			ps.setInt(2, orgId);
			}
			if (Misc.isUndef(userId)) {
				ps.setNull(3, java.sql.Types.INTEGER);
			}else{
			ps.setInt(3, userId);
			}
			ps.setInt(4, menuMasterId);
			ps.executeUpdate();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	public MenuReportBean saveMenuReport(String menuField, String existGroup, String newGroupData, String newGroupEmailList,String newGroupFrequencyList,MenuReportBean bean, String oldMenuTag, String columnXML, String paramXML,int reportType) throws GenericException{
		PreparedStatement ps = null;
		ResultSet rSet = null;
		long reportId = Misc.getUndefInt();
		Connection conn = null;		
		Cache cache = null;
		MenuBean mbean = new MenuBean();
		try{
			cache = m_session.getCache();
			
			conn = m_session.getConnection();
			ps = bean.getReportId() <=0 ? conn.prepareStatement(DBQueries.MENUREPORT.INSERT_REPORT_DEFINITION,Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(DBQueries.MENUREPORT.UPDATE_REPORT_DEFINITION);
			bean = parseMenuReportField(menuField, bean,reportType);
			getMenuURLConfigFile(oldMenuTag, bean);
			org.w3c.dom.Document xmlDoc = null;
			org.w3c.dom.NodeList nList = null;
			int size = Misc.getUndefInt();
			int status = Misc.getUndefInt();
				 if (bean.getReportId() > 0) {
					
					    ps.setString(1, bean.getOptionMenuTagName());
					    ps.setString(2, bean.getMenuTagName());
						ps.setString(3, bean.getMenuTitle());
						ps.setString(4, bean.getHelp());
						ps.setInt(5, bean.getType());
						ps.setInt(6, bean.getStatus());
						ps.setInt(7, bean.getOrgId());
						ps.setInt(8, bean.getUserId());
						ps.setInt(9, bean.getNodata());
						ps.setInt(10, bean.getSenderProfile());
						ps.setInt(11, (int)bean.getReportId());
						MenuReportBean bean1 = new MenuReportBean();
						bean1 = getMenuReportInformation((int)bean.getReportId());
						cache = m_session.getCache();
						//set menuUrl and page_context from bean1 to bean.......
						bean.setConfigFile(bean1.getConfigFile());
						bean.setUrl(bean1.getUrl());
						if (reportType == 0) {
							cache.updateMenuTreeFromPage(m_session,bean1.getOptionMenuTagName(), bean1.getMenuTagName(), bean1.getMenuTitle(),bean1.getOrgId(), bean1.getUserId(),bean.getReportId(),bean.getOptionMenuTagName(),bean.getMenuPlaceHolderId());	
						}
						
						if (bean1.getOptionMenuTagName() != null  && !bean1.getOptionMenuTagName().equalsIgnoreCase(bean.getOptionMenuTagName())) {
							updateOptionalMenuTagInDataBases(bean1.getOptionMenuTagName(),bean.getOptionMenuTagName());	
						}
						
						
						reportId = bean.getReportId();
						ps.executeUpdate();
						//now update only menu_tag in menu_master ...because it might be change ...rest field remain same ..
						  int artificialPrivId = cache.getArtificialPrivId((int)reportId, bean.getUserId(), bean.getOrgId());
						    String menuTag = "tr_"+ bean.getMenuPlaceHolderId()+"tr_"+ bean.getOptionMenuTagName() +"tr_"+ bean.getMenuTagName() + "tr_" + artificialPrivId;
						    updateMenuTagMenuMaster(menuTag.replaceAll("\\s", ""), bean.getMenuMasterId(),bean.getOrgId(),bean.getUserId());
				 }
					if (bean.getReportId() <= 0) {
						ps.setString(1, bean.getUrl());
						ps.setString(2, bean.getOptionMenuTagName());
						ps.setString(3, bean.getMenuTagName());
						ps.setString(4, bean.getMenuTitle());
						ps.setString(5, bean.getHelp());
						ps.setInt(6, bean.getType());
						ps.setInt(7, bean.getStatus());
						ps.setInt(8, bean.getOrgId());
						ps.setInt(9, bean.getUserId());
						ps.setInt(10, bean.getNodata());
						ps.setInt(11, bean.getSenderProfile());
						int affectedRow = ps.executeUpdate();
						if (affectedRow == 0) {
							throw new SQLException("Insertion failed, no rows affected.");
						}
						rSet = ps.getGeneratedKeys();
						if (rSet.next()) {
						 reportId = rSet.getLong(1);
						} else {
							throw new SQLException("Insertion failed, no rows affected.");
					}
		        //insert into menu_master table as well as ui_column & ui_param table....
			    int artificialPrivId = cache.getArtificialPrivId((int)reportId, bean.getUserId(), bean.getOrgId());
			    String menuTag = "tr_"+ bean.getMenuPlaceHolderId()+"tr_"+ bean.getOptionMenuTagName() +"tr_"+ bean.getMenuTagName() + "tr_" + artificialPrivId;
			    mbean = saveCustomize(conn, menuTag.replaceAll("\\s", ""), bean.getConfigFile(), bean.getUserId(), bean.getOrgId(), columnXML, paramXML);
			// now insert into menu_master_report_definiton table..................
				    ps = conn.prepareStatement(DBQueries.MENUREPORT.UPDATE_REPORT_DEFINITION_MENU_ID);
					ps.setInt(1, mbean.getId());
					ps.setInt(2, (int)reportId);
					ps.executeUpdate();
			}
			
			
			if (reportType == 0) {
			ps = bean.getReportId() <=0 ? conn.prepareStatement(DBQueries.MENUREPORT.INSERT_REPORT_MENUPLACEHOLDER) : conn.prepareStatement(DBQueries.MENUREPORT.UPDATE_REPORT_MENUPLACEHOLDER);
			ps.setInt(1, bean.getMenuPlaceHolderId());
			ps.setLong(2, reportId);
			ps.executeUpdate();
			}
			//first need to delete all user email list corresponding to that email group ..........Edit then insert new records
			if (bean.getReportId() > 0) {
				
				
			/*	if (bean.getEmailReportGroupInformation().getEmailReportId() > 0) {
					ps = conn.prepareStatement(DBQueries.MENUREPORT.DELETE_EMAIL_REPORT_USERS);
					ps.setInt(1, bean.getEmailReportGroupInformation().getEmailReportId());
					ps.execute();
				}
				if (bean.getEmailReportGroupInformation().getEmailReportId() > 0) {
					ps = conn.prepareStatement(DBQueries.MENUREPORT.DELETE_EMAIL_REPORT_FREQUENCIES);
					ps.setInt(1, bean.getEmailReportGroupInformation().getEmailReportId());
					ps.execute();
				}*/
				if (bean.getReportId() > 0) {
					ps  = conn.prepareStatement(DBQueries.MENUREPORT.DELETE_EMAIL_REPORT_DEFINITION);
					ps.setInt(1, (int)bean.getReportId());
					ps.execute();
				}
				
		}
			int emailReportId = Misc.getUndefInt();
            boolean newGroupIsCreated = false;       
			if (!"".equalsIgnoreCase(newGroupData) && bean.getReportId() <= 0 ) {
                newGroupIsCreated = true;
				ps = bean.getEmailReportGroupInformation().getEmailReportId() <=0 ? conn.prepareStatement(DBQueries.MENUREPORT.INSERT_EMAIL_REPORT_GROUPS,Statement.RETURN_GENERATED_KEYS) : conn.prepareStatement(DBQueries.MENUREPORT.UPDATE_EMAIL_REPORT_GROUPS);
				xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(newGroupData);
				nList = xmlDoc.getElementsByTagName("MainTag");
				size = nList.getLength();
				int reportFormat = Misc.getUndefInt();
				boolean newEmailGroupCreated = false;
				String reportGroupName = null;
				for (int i = 0; i < size; i++) {
					org.w3c.dom.Node n = nList.item(i);
					org.w3c.dom.Element e = (org.w3c.dom.Element) n;
					reportFormat =  Misc.getParamAsInt(e.getAttribute("reportformat"));
					reportGroupName = Misc.getParamAsString(e.getAttribute("groupname")); 
				   if (!"".equalsIgnoreCase(reportGroupName)) {
					ps.setString(1, reportGroupName);
				    ps.setInt(2, reportFormat);
				    ps.setInt(3, bean.getStatus());
				    ps.setInt(4, (int)reportId);
				    newEmailGroupCreated = true;
				   }
				}
				if (newEmailGroupCreated && bean.getEmailReportGroupInformation().getEmailReportId() > 0) {
					ps.setInt(5, bean.getEmailReportGroupInformation().getEmailReportId());
					ps.executeUpdate();
					emailReportId = bean.getEmailReportGroupInformation().getEmailReportId();
				}else if(newEmailGroupCreated){
				int affectedRow = ps.executeUpdate();
				if (affectedRow == 0) {
					throw new SQLException("Insertion failed, no rows affected.");
				}
				rSet = ps.getGeneratedKeys();
				if (rSet.next()) {
					emailReportId = rSet.getInt(1);
				} else {
					throw new SQLException("Insertion failed, no rows affected.");
				}
				}
				
				if (!"".equalsIgnoreCase(newGroupEmailList)) {
					insertUserEmailList(conn, ps, newGroupEmailList, emailReportId);
					
				}
				
				if (!"".equalsIgnoreCase(newGroupFrequencyList)) {
					
					insetFrequencyData(newGroupFrequencyList, conn, ps, emailReportId);
				}
			}
			
			//first delete all record corresponding to reportid then insert new records ....Edit 
			
			if (!"".equalsIgnoreCase(existGroup)) {
				
				boolean execute = false;
				ps = conn.prepareStatement(DBQueries.MENUREPORT.INSERT_EMAIL_REPORT_DEFINITION);
				Document dataDoc = MyXMLHelper.loadFromString(existGroup);
				for (Node n=dataDoc == null || dataDoc.getDocumentElement() == null ? null : dataDoc.getDocumentElement().getFirstChild(); n != null; n=n.getNextSibling()) {
					if (n.getNodeType() != 1)
						continue;
					Element e = (Element) n;
					int existGroupId = Misc.getParamAsInt(e.getAttribute("Existing_Report"));
                    if(!Misc.isUndef(existGroupId)) {
					ps.setLong(1, reportId); 
                    ps.setInt(2, existGroupId);
                    ps.addBatch();
                    execute = true;
                    }
				}
				if (!Misc.isUndef(emailReportId)) {
					ps.setLong(1, reportId); 
                    ps.setInt(2, emailReportId);
                    ps.addBatch();
                    execute = true;
				}
				if (execute) {
					ps.executeBatch();	
				}
				
			}
			
			bean.setReportId(reportId);
			
			
		}catch (Exception e) {
			e.printStackTrace();
			throw new GenericException();
		}finally {
			try {
				if (ps != null) {
					ps.close();

				}
				if (rSet != null) {
					rSet.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new GenericException(e);
			}
		}
		
				
		return bean;
	}
	
	public void getEmailReportData(int reportId, MenuReportBean reportBean) throws Exception{
		MenuReportBean.ReprtGroupEmailAndFrequency bean = new MenuReportBean.ReprtGroupEmailAndFrequency();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
		conn = m_session.getConnection();
		ps = conn.prepareStatement(DBQueries.MENUREPORT.GET_EMAIL_REPORT_DATA);
		ps.setInt(1, reportId);
	    rs = ps.executeQuery();
	    while (rs.next()) {
	    	bean.setEmailReportId(rs.getInt(1));
	    	bean.setEmailReportName(rs.getString(2));
	    	bean.setEmail_report_format(rs.getInt(3));
	    	bean.setShiftId(rs.getInt(4));// work as status
		}
	    reportBean.setEmailReportGroupInformation(bean);
		}catch(Exception e){
			e.printStackTrace();
		
		}finally{
			if (ps != null) {
				ps.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		
	}
	
	public ArrayList<MenuReportBean> getMenuReportList(int reportType) throws Exception{
		ArrayList<MenuReportBean> menuListData = new ArrayList<MenuReportBean>();
		MenuReportBean bean = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
		conn = m_session.getConnection();
		ps = reportType == 0 ? conn.prepareStatement(DBQueries.MENUREPORT.GET_MENU_REPORT_LISTS) : conn.prepareStatement(DBQueries.MENUREPORT.GET_ONLINE_REPORT_LIST);
		ps.setInt(1, reportType);
	    rs = ps.executeQuery();
	    
	    while (rs.next()) {
	    	int count = 1;
	    	bean = new MenuReportBean();
	    	bean.setReportId(rs.getLong(count++));
	    	if (reportType == 0) {
	    		bean.setMenuPlaceHolderId(rs.getInt(count++));	
			}
	    	bean.setMenuTitle(rs.getString(count++));
	    	bean.setStatus(rs.getInt(count++));
	    	bean.setHelp(rs.getString(count++));
	    	bean.setOptionMenuTagName(rs.getString(count++));
	    	bean.setMenuTagName(rs.getString(count++));
	    	bean.setConfigFile(rs.getString(count++));
	    	bean.setOrgId(Misc.getRsetInt(rs, count++));
	    	bean.setMenuMasterId(Misc.getRsetInt(rs, count++));
	    	menuListData.add(bean);
		}
		}catch(Exception e){
			e.printStackTrace();
		
		}finally{
			if (ps != null) {
				ps.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return menuListData;
	}
	
	public MenuReportBean getMenuReportData(int reportId, int reportType) throws Exception{
		MenuReportBean bean = new MenuReportBean();
	    Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	    try {
			conn = m_session.getConnection();
			ps = reportType == 0 ? conn.prepareStatement(DBQueries.MENUREPORT.GET_MENU_REPORT_DATA) : conn.prepareStatement(DBQueries.MENUREPORT.GET_ONLINE_REPORT_DATA);
			ps.setInt(1, reportType);
			ps.setInt(2, reportId);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				int count = 1;
				if (reportType == 0) {
					bean.setMenuPlaceHolderId(rs.getInt(count++));		
				}
			
				bean.setOptionMenuTagName(rs.getString(count++));
				bean.setMenuTagName(rs.getString(count++));
				bean.setMenuTitle(rs.getString(count++));
				bean.setStatus(rs.getInt(count++));
				bean.setHelp(rs.getString(count++));
				bean.setUserId(rs.getInt(count++));
				bean.setOrgId(rs.getInt(count++));
				bean.setMenuMasterId(rs.getInt(count++));
				bean.setNodata(rs.getInt(count++));
				bean.setSenderProfile(rs.getInt(count++));
				bean.setReportId(reportId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (ps != null) {
				ps.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return bean;
		
	}

	public void getEmailReportGroupData(int reportId,MenuReportBean bean, int reportType) throws Exception{
        MenuReportBean.ReprtGroupEmailAndFrequency dataList = null;
        ArrayList list  = new ArrayList();
		Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	    try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.MENUREPORT.GET_EMAIL_REPORT_GROUP);
			ps.setInt(1, reportId);
			ps.setInt(2, reportType);
			rs = ps.executeQuery();
			while (rs.next()) {
			dataList =	new MenuReportBean.ReprtGroupEmailAndFrequency();
				dataList.setEmailReportId(rs.getInt(1));
				dataList.setEmailReportName(rs.getString(2));
				dataList.setEmail_report_format(rs.getInt(3));
			    list.add(dataList);
			}
			bean.setExistingEmailGroupList(list);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (ps != null) {
				ps.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		
	}
	public void getEmailReportEmailData(int reportId,MenuReportBean bean) throws Exception{
        MenuReportBean.ReprtGroupEmailAndFrequency dataList = null;
        ArrayList list = new ArrayList();
		Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	    try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.MENUREPORT.GET_EMAIL_REPORT_EMAIL);
			ps.setInt(1, reportId);
			rs = ps.executeQuery();
			while (rs.next()) {
			dataList =	new MenuReportBean.ReprtGroupEmailAndFrequency();
				dataList.setEmailReportId(rs.getInt(1));
				dataList.setEmailReportName(rs.getString(2));
			    list.add(dataList);
			}
			bean.setReportUserEmailList(list);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (ps != null) {
				ps.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		
	}

	public void getEmailReportFrequencyData(int reportId,MenuReportBean bean, int reportType) throws Exception{
        MenuReportBean.ReprtGroupEmailAndFrequency dataList = null;
        ArrayList list = new ArrayList();
		Connection conn = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	    try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.MENUREPORT.GET_EMAIL_REPORT_FREQUENCY);
			ps.setInt(1, reportType);
			ps.setInt(2, reportId);
			rs = ps.executeQuery();
			while (rs.next()) {
			dataList =	new MenuReportBean.ReprtGroupEmailAndFrequency();
				dataList.setEmailReportId(rs.getInt(1));
				dataList.setShiftId(rs.getInt(2));
				dataList.setShiftHours(rs.getInt(3));
                dataList.setShiftMinutes(rs.getInt(4));				
			    list.add(dataList);
			}
			bean.setReportFrequencyList(list);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (ps != null) {
				ps.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		
	}

public void	updateOptionalMenuTagInDataBases(String oldOptionalMenuTagName,String newOptionalMenuGroupTagName){
		PreparedStatement ps = null;
		Connection conn = null;
		try{
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.MENUREPORT.UPDATE_OPTIONAL_MENU_NAME);
			ps.setString(1, newOptionalMenuGroupTagName);
			ps.setString(2, oldOptionalMenuTagName);
			ps.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
			if (ps != null) {
				ps.close();
			}
			}catch (Exception e) {
			e.printStackTrace();
			}
		}
	}

public ArrayList<MenuReportBean> getAllEmailReportGroups(){
	PreparedStatement ps = null;
	ResultSet rs = null;
	Connection conn = null;
	MenuReportBean bean = null;
	ArrayList<MenuReportBean> list = new ArrayList<MenuReportBean>();
	try {
		
		conn = m_session.getConnection();
		ps = conn.prepareStatement(DBQueries.MENUREPORT.GET_ALL_ACTIVE_EMAIL_REPORT_GROUP);
		rs = ps.executeQuery();
		
		while (rs.next()) {
			bean = new MenuReportBean();
			bean.setMenuPlaceHolderId(rs.getInt(1));
			bean.setOptionMenuTagName(rs.getString(2));
			list.add(bean);
		}
		ps.close();
		rs.close();
	} catch (Exception e) {
		e.printStackTrace();
	}
	return list;
	
}
public ArrayList<Pair<Integer,String>> getReportSenderProfile(){
	PreparedStatement ps = null;
	ResultSet rs = null;
	Connection conn = null;
	ArrayList<Pair<Integer, String>> retval  = new ArrayList<Pair<Integer,String>>();
	try {
		conn = m_session.getConnection();
		ps = conn.prepareStatement(DBQueries.MENUREPORT.GET_REPORT_SENDER_PROFILES);
		rs = ps.executeQuery();
		while (rs.next()) {
			retval.add(new Pair<Integer, String>(Misc.getRsetInt(rs, "id"), rs.getString("short_code")));
		}
		ps.close();
		rs.close();
	} catch (Exception e) {
		e.printStackTrace();
	}
	return retval;
}

public MenuBean saveCustomize(Connection conn, String menuTag, String configFile , int userId, int portNodeId, String columnXML, String paramXML)
throws ServletException, IOException, GenericException, Exception {
		MenuBean menuBean = new MenuBean();
		
		menuBean.setUserId(userId);
		menuBean.setRowId(0);
		menuBean.setColId(0);
		menuBean.setPortNodeId(portNodeId);
//		if(request.getParameter("portNodeId") != null && !"".equals(request.getParameter("portNodeId")))
//			menuBean.setPortNodeId(Integer.parseInt(request.getParameter("portNodeId")));
		menuBean.setMenuTag(menuTag);
		menuBean.setComponentFile(configFile);
			
		String xml = Common.getParamAsString(columnXML);
		System.out.println("CustomizeServlet.saveCustomize()  :  XML_DATA_COLUMN : "  +  xml);
		if (!"".equalsIgnoreCase(xml) && !xml.contains("&amp;")) {
			xml = xml.replaceAll("&", "&amp;");
		}
		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
		
	    org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("COLUMN");
	    List<UIColumnBean> uiColumnBeanList = menuBean.getUiColumnBean();
	    int size = nList.getLength();
	    UIColumnBean uiColumnBean = null;	    
	    if (size != 0) {//ensure last is proper
	    	org.w3c.dom.Node node =  nList.item(size-1);		        
	        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
	        if (element.getAttribute("colName") == null || "".equals(element.getAttribute("colName")) 
	        		|| element.getAttribute("colTitle") == null && "".equals(element.getAttribute("colTitle"))) {
	        	size--;
	        }
	    }
	    for ( int i=0; i<size ; i++){
	    	
	        org.w3c.dom.Node node =  nList.item(i);
	        
	        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
	        if(element.getAttribute("colName") != null && !"".equals(element.getAttribute("colName")) 
	        		&& element.getAttribute("colTitle") != null && !"".equals(element.getAttribute("colTitle"))){
		        uiColumnBean = new UIColumnBean();
		        uiColumnBean.setColumnName(element.getAttribute("colName"));
		        uiColumnBean.setAttrName(element.getAttribute("colTitle"));
		        uiColumnBean.setAttrValue(element.getAttribute("colTitle"));
		        uiColumnBean.setAttrValue(element.getAttribute("colTitle"));
		        uiColumnBean.setRollup(Misc.getParamAsInt(element.getAttribute("status"),0));
   
				uiColumnBeanList.add(uiColumnBean);
	        }
	    }
	    menuBean.setUiColumnBean((ArrayList<UIColumnBean>)uiColumnBeanList);
	   
	    xml = Common.getParamAsString(paramXML);
	    System.out.println("CustomizeServlet.saveCustomize() ==  XML_DATA_PARAM  ===> "+xml);
		if (!"".equalsIgnoreCase(xml) && !xml.contains("&amp;")) {
			xml = xml.replaceAll("&", "&amp;");
		}
		xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xml);
		
	    nList= xmlDoc.getElementsByTagName("PARAM");
	    List<UIParameterBean> uiParameterBeanList = menuBean.getUiParameterBean();
	    size = nList.getLength();
	    UIParameterBean uiParameterBean = null;
	    for ( int i=0; i<size ; i++){
	        org.w3c.dom.Node node =  nList.item(i);
	        
	        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
	        if(element.getAttribute("propertyName") != null && !"".equals(element.getAttribute("propertyName")) 
	        		&& element.getAttribute("propertyValue") != null && !"".equals(element.getAttribute("propertyValue"))){
	        	uiParameterBean = new UIParameterBean(element.getAttribute("propertyName"), element.getAttribute("propertyValue"), element.getAttribute("operator"), element.getAttribute("right_operand"));
	        	uiParameterBeanList.add(uiParameterBean);
	        }
	    }
	    menuBean.setUiParameterBean((ArrayList<UIParameterBean>)uiParameterBeanList);
	    
	    
	    CustomizeDao customizeDao = new CustomizeDao();
	    
	    
	    	customizeDao.insertMenu(conn, menuBean);
	    	System.out.println("hello dear");
		
	return menuBean; //editCustomize(request, response);
}

public void insetFrequencyData(String newGroupFrequencyList,Connection conn,PreparedStatement ps, int emailReportId) throws Exception{
	org.w3c.dom.Document xmlDoc = null;
	org.w3c.dom.NodeList nList = null;

	ps = conn.prepareStatement(DBQueries.MENUREPORT.INSERT_EMAIL_REPORT_FREQUENCIES);
	xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(newGroupFrequencyList);
	nList = xmlDoc.getElementsByTagName("d");
	int size = nList.getLength();
	int granularity = Misc.getUndefInt();
	int shiftHours = Misc.getUndefInt();
	int shiftMinutes = Misc.getUndefInt();
	
	for (int i = 0; i < size; i++) {
		org.w3c.dom.Node n = nList.item(i);
		org.w3c.dom.Element e = (org.w3c.dom.Element) n;
		granularity =  Misc.getParamAsInt(e.getAttribute("time_granularity"));
		shiftHours = Misc.getParamAsInt(e.getAttribute("frequency_hours")); 
		shiftMinutes = Misc.getParamAsInt(e.getAttribute("frequency_minutes")); 
	 ps.setInt(1, emailReportId);
	 ps.setInt(2, granularity);
	 ps.setInt(3, shiftHours);
	 ps.setInt(4, shiftMinutes);
	 ps.addBatch();
	}	
	ps.executeBatch();
}

public void insertUserEmailList(Connection conn , PreparedStatement ps, String newGroupEmailList,int emailReportId) throws Exception{
	
	ps = conn.prepareStatement(DBQueries.MENUREPORT.INSERT_EMAIL_REPORT_USERS);
	Document dataDoc = MyXMLHelper.loadFromString(newGroupEmailList);
	for (Node n=dataDoc == null || dataDoc.getDocumentElement() == null ? null : dataDoc.getDocumentElement().getFirstChild(); n != null; n=n.getNextSibling()) {
		if (n.getNodeType() != 1)
			continue;
		Element e = (Element) n;
		String emailTo = Misc.getParamAsString(e.getAttribute("toemail"));
		if (!"".equalsIgnoreCase(emailTo)) {
			ps.setInt(1, emailReportId); 
            ps.setString(2, emailTo);
            ps.addBatch();	
		}
        
	}
	ps.executeBatch();


}
}


