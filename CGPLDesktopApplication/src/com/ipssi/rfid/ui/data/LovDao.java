package com.ipssi.rfid.ui.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.processor.Utils;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Created by ipssi11 on 16-Oct-16.
 */
public class LovDao {
	private static final String seperatorDel = "@";

	public static enum LovItemType {
		VEHICLE, PO_SALES_ORDER,SALES_ORDER, TRANSPORTER, CUSTOMER, PO_LINE_ITEM, DATA_ENTRY_TYPE, DL_NUMBER,DRIVER_NAME,AUTO_COMPLETE_VEHICLE,PROCESSING_STATUS,OPEN_CLOSE,HOUR,MINUTE,INVOICE_CANCEL,INVOICE_STATUS,PRINTER
	}

	public static Pair<String, String> getCodeNamePair(String text) {
		String[] splitText = !Utils.isNull(text) ? text.split(seperatorDel) : null;
		String first = splitText != null && splitText.length > 0 ? splitText[0] : "";
		String second = splitText != null && splitText.length > 1 ? splitText[1] : "";
		return new Pair<String, String>(first, second);
	}

	public static String getAutoCompleteValue(Node t) {
		String text = t != null
				? ((t instanceof TextField) ? ((TextField) t).getText()
						: ((t instanceof Label) ? ((Label) t).getText() : null))
				: null;
		String[] textSplit = t == null || Utils.isNull(text) ? null : text.split(seperatorDel);
		if (textSplit != null && textSplit.length > 0)
			return textSplit[0];
		return null;
	}

	public static String getAutoCompleteValue(String text) {
		if (Utils.isNull(text))
			return text;
		String[] textSplit = Utils.isNull(text) ? null : text.split(seperatorDel);
		if (textSplit != null && textSplit.length > 0)
			return textSplit[0];
		return null;
	}

	public static Pair<String, String> getAutocompletePrintablePair(Connection conn, int portNodeId, String text,
			LovItemType suggestionType) {
		Pair<Boolean, String> retval = getSuggestionPairByCode(conn, portNodeId, text, suggestionType);
		String[] splitText = !Utils.isNull(retval.second) ? retval.second.split(seperatorDel) : null;
		String first = splitText != null && splitText.length > 0 ? splitText[0] : "";
		String second = splitText != null && splitText.length > 1 ? splitText[1] : "";
		return new Pair<String, String>(first, second);
	}

	public static String getAutocompletePrintable(Connection conn, int portNodeId, String text,
			LovItemType suggestionType) {
		Pair<Boolean, String> retval = getSuggestionPairByCode(conn, portNodeId, text, suggestionType);
		return Utils.isNull(retval.second) ? "" : retval.second;
	}

	public static boolean isAutocompleteNull(Connection conn, int portNodeId, String text, LovItemType suggestionType) {
		return getSuggestionPairByCode(conn, portNodeId, text, suggestionType).first;
	}

	public static Pair<Boolean, String> getSuggestionPairByCode(Connection conn, int portNodeId, String text,
			LovItemType suggestionType) {
		if (text == null || text.length() == 0)
			return new Pair<Boolean, String>(false, null);
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = null;
		switch (suggestionType) {
		case TRANSPORTER:
			query = "SELECT id,concat(sn,'" + seperatorDel
					+ "',name) FROM transporter_details where status=1 and port_node_id=? and sn like '" + text + "'";
			break;

		case CUSTOMER:
			query = "select id, concat(sn,'" + seperatorDel
					+ "',name) from customer_details where status=1 and port_node_id=? and sn like '" + text + "'";
			break;
		case PO_LINE_ITEM:
			query = "select id, concat(sn,'" + seperatorDel
					+ "',name) from customer_details where status=1 and port_node_id=? and sn like '" + text + "'";
			break;

		default:
			break;
		}
		if (query == null)
			return new Pair<Boolean, String>(false, null);
		try {
			ps = conn.prepareStatement(query);
			if (ps.getParameterMetaData().getParameterCount() > 0)
				Misc.setParamInt(ps, portNodeId, 1);
			rs = ps.executeQuery();
			if (rs.next()) {
				return new Pair<Boolean, String>(true, rs.getString(2));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}
		return new Pair<Boolean, String>(false, null);
	}

	public static ArrayList<String> getFieldSuggestion(int portNodeId, String text, LovItemType suggestionType) {
		if (text == null || text.length() == 0)
			return null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = null;
		switch (suggestionType) {
		case VEHICLE:
			query = " select vehicle.id,vehicle.std_name from vehicle join "
					+ " (select distinct(vehicle.id) vehicle_id from vehicle "
					+ " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) "
					+ " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) "
					+ " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) "
					+ " join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "
					+ " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id "
					+ " where status in (1) and vehicle.std_name like '%" + text + "%'";
			break;

		case TRANSPORTER:
			query = "SELECT id, sn FROM transporter_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"
					+ text + "%'";
			break;

		case CUSTOMER:
			query = "select id, sn from customer_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"
					+ text + "%'";
			break;

		default:
			break;
		}
		if (query == null)
			return null;
		ArrayList<String> suggestionList = new ArrayList<String>();
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			if (ps.getParameterMetaData().getParameterCount() > 0)
				Misc.setParamInt(ps, portNodeId, 1);
			rs = ps.executeQuery();
			while (rs.next()) {
				suggestionList.add(rs.getString(2));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return suggestionList;
	}

	public static ArrayList<String> getFieldSuggestionMerged(int portNodeId, String text, LovItemType suggestionType) {
		if (text == null || text.length() == 0)
			return null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = null;
		switch (suggestionType) {
		case VEHICLE:
			query = " select vehicle.id,vehicle.std_name from vehicle join "
					+ " (select distinct(vehicle.id) vehicle_id from vehicle "
					+ " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) "
					+ " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) "
					+ " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) "
					+ " join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "
					+ " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id "
					+ " where status in (1) and vehicle.std_name like '%" + text + "%'";
			break;

		case TRANSPORTER:
			query = "SELECT id,concat(sn,'" + seperatorDel
					+ "',name) FROM transporter_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"
					+ text + "%'";
			break;

		case CUSTOMER:
			query = "select id, concat(sn,'" + seperatorDel
					+ "',name) from customer_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"
					+ text + "%'";
			break;
		default:
			break;
		}
		if (query == null)
			return null;
		ArrayList<String> suggestionList = new ArrayList<String>();
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			if (ps.getParameterMetaData().getParameterCount() > 0)
				Misc.setParamInt(ps, portNodeId, 1);
			rs = ps.executeQuery();
			while (rs.next()) {
				suggestionList.add(rs.getString(2));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return suggestionList;
	}

	public static Pair<String, String> getFieldText(int portNodeId, int id, LovItemType suggestionType, String undef) {
		Pair<String, String> retval = new Pair<String, String>(undef, undef);
		if (Misc.isUndef(id))
			return retval;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = null;
		switch (suggestionType) {
		case VEHICLE:
			query = " select vehicle.id,vehicle.std_name,vehicle.std_name from vehicle join "
					+ " (select distinct(vehicle.id) vehicle_id from vehicle "
					+ " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) "
					+ " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) "
					+ " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) "
					+ " join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "
					+ " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id "
					+ " where status in (1) and vehicle.id=?";
			break;
		case TRANSPORTER:
			query = "SELECT id,name,sn FROM transporter_details where status=1 and port_node_id=? and id=?";
			break;

		case CUSTOMER:
//			query = "select id, name,sn from customer_details where status=1 and port_node_id=? and id=?";
			query = "Select id,sap_customer_sap_code,sap_customer_name from CGPL_SALES_ORDER WHERE  STATUS=1 AND CGPL_SALES_ORDER.sap_customer_sap_code = ?";
			
			break;
		default:
			break;
		}
		if (query == null)
			return retval;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			int count = 1;
			if (ps.getParameterMetaData().getParameterCount() > 0)
				Misc.setParamInt(ps, portNodeId, count++);
			if (ps.getParameterMetaData().getParameterCount() > 1)
				Misc.setParamInt(ps, id, count++);
			rs = ps.executeQuery();
			if (rs.next()) {
				retval = new Pair<String, String>(rs.getString(2), rs.getString(3));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				if (conn != null)
					DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return retval;
	}

	public static ArrayList<String> getVehicleSuggestion(int portNodeId, String text) {
		if (text == null || text.length() == 0)
			return null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<String> suggestionList = new ArrayList<String>();
		String query = " select vehicle.id,vehicle.std_name from vehicle join "
				+ " (select distinct(vehicle.id) vehicle_id from vehicle "
				+ " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) "
				+ " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) "
				+ " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) "
				+ " join port_nodes anc  on (anc.id in (" + portNodeId
				+ ") and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) "
				+ " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id "
				+ " where status in (1) and vehicle.std_name like '%" + text + "%'";
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				suggestionList.add(rs.getString(2));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return suggestionList;
	}

	public static ArrayList<String> getDoSuggestion(int portNodeId, String text) {
		if (text == null || text.length() == 0)
			return null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<String> suggestionList = new ArrayList<String>();
		String query = " select id,do_number from do_rr_details where port_node_id=?";
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, portNodeId, 1);
			rs = ps.executeQuery();
			while (rs.next()) {
				suggestionList.add(rs.getString(2));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return suggestionList;
	}

	public static Pair<Integer, ArrayList<ComboItem>> getLovItemPair(Connection conn, int portNodeId,
			LovItemType lovItemType, int selectedValue, int otherParamVal) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ComboItem> retval = new ArrayList<ComboItem>();
		
		int selectedIndex = Misc.getUndefInt();
		String query = null;
		switch (lovItemType) {
		case TRANSPORTER:
			query = "SELECT transporter_details.id,transporter_details.name  FROM transporter_details where status=1 and port_node_id=? ";
			break;
		case PO_SALES_ORDER:
			query = "SELECT cgpl_sales_order.id,cgpl_sales_order.sap_sales_order FROM cgpl_sales_order where cgpl_sales_order.status =1 and cgpl_sales_order.port_node_id=? ";
			if(!Misc.isUndef(selectedValue))
				query += " and sap_customer_sap_code  = ? ";	
			query +=	"group by cgpl_sales_order.sap_sales_order";
			break;
		case CUSTOMER:
			query = "Select customer_details.id,customer_details.name from customer_details  where customer_details.status=1 and customer_details.port_node_id=?";
//			query = "Select sap_customer_sap_code,sap_customer_name from CGPL_SALES_ORDER WHERE  STATUS=1 AND cgpl_sales_order.port_node_id=? AND CGPL_SALES_ORDER.sap_sales_order=?";
//			query = "Select sap_customer_sap_code,sap_customer_name from CGPL_SALES_ORDER WHERE  STATUS=1 AND cgpl_sales_order.port_node_id=?";
			break;
		case PO_LINE_ITEM:
			query = "SELECT id,sap_line_item FROM cgpl_sales_order where cgpl_sales_order.status=1 and cgpl_sales_order.port_node_id="+portNodeId+ " and cgpl_sales_order.id="+otherParamVal;
			break;
		case SALES_ORDER:
			query = "SELECT cgpl_sales_order.id,cgpl_sales_order.sap_sales_order FROM cgpl_sales_order where cgpl_sales_order.status =1 and cgpl_sales_order.port_node_id=? ";
			query +=	"group by cgpl_sales_order.sap_sales_order";
			
			break;	
		default:
			break;
		}
		if (query == null)
			return null;
		try {
			System.out.println("query:"+ query + " ,PortNadeId"+ portNodeId + " ,SelectValue:"+selectedValue);
			int insertPos = 1;
			retval.add(new ComboItem(Misc.getUndefInt(),"select"));
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			Misc.setParamInt(ps, portNodeId, insertPos++);
//			if (!Misc.isUndef(selectedValue))
//				Misc.setParamInt(ps, selectedValue, insertPos++);
//			if (!Misc.isUndef(otherParamVal))
//				Misc.setParamInt(ps, otherParamVal, insertPos++);
			rs = ps.executeQuery();
			while (rs.next()) {
			ComboItem c = new ComboItem(Misc.getRsetInt(rs, 1), rs.getString(2));
				retval.add(c);
				if (c.getValue() == selectedValue)
					selectedIndex = retval.size() - 1;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return new Pair<Integer, ArrayList<ComboItem>>(selectedIndex, retval);
	}

	public static Pair<Integer, ArrayList<ComboItem>> getLovItemPairNew(Connection conn, int portNodeId,
			LovItemType lovItemType, int selectedValue, int otherParamVal, String selectedText) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<ComboItem> retval = new ArrayList<ComboItem>();
		
		int selectedIndex = Misc.getUndefInt();
		String query = null;
		
		switch (lovItemType) {
//		case TRANSPORTER:
//			query = "SELECT transporter_details.id,transporter_details.name  FROM transporter_details where status=1 and port_node_id= "+portNodeId;
//			break;
		case TRANSPORTER:
			query = "SELECT transporter_details.id,transporter_details.name  FROM transporter_details where status=1 and port_node_id= "+portNodeId;
//			query = "SELECT DISTINCT td.id,td.name FROM transporter_details td JOIN cgpl_customer_transporter_rel ct ON (td.id=ct.transporter_detail_id) JOIN customer_details cd ON (ct.customer_detail_id=cd.id) "
//					+ " WHERE td.status=1 AND td.port_node_id="+portNodeId +" AND ct.customer_detail_id="+otherParamVal;
			break;
		case PO_SALES_ORDER:
			query = "SELECT cgpl_sales_order.id,cgpl_sales_order.sap_sales_order FROM cgpl_sales_order where cgpl_sales_order.status=1 and cgpl_sales_order.port_node_id="+portNodeId +" and cgpl_sales_order.customer_id="+ otherParamVal + " group by cgpl_sales_order.sap_sales_order";
			break;
		case CUSTOMER:
			query = "Select sap_code,name from customer_details where customer_details.status=1 and customer_details.port_node_id="+portNodeId;
//			query = "Select sap_customer_sap_code,sap_customer_name from CGPL_SALES_ORDER WHERE  STATUS=1 AND cgpl_sales_order.port_node_id="+ portNodeId +" AND cgpl_sales_order.sap_sales_order="+selectedValue;
			break;
		case PO_LINE_ITEM:
			if(Utils.isNull(selectedText) || selectedText.equalsIgnoreCase("select"))
			 selectedText="-1111111";
			 query = "SELECT id,sap_line_item FROM cgpl_sales_order where cgpl_sales_order.status=1 and cgpl_sales_order.port_node_id="+portNodeId +" and cgpl_sales_order.sap_sales_order='"+selectedText+"'";
			break;
		default:
			break;
		}
		if (query == null)
			return null;
		
		try {
			int insertPos = 1;
			retval.add(new ComboItem(Misc.getUndefInt(),"select"));
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			
//			if (!Misc.isUndef(otherParamVal))
//				Misc.setParamInt(ps, otherParamVal, insertPos++);
//			if (selectedText != null && selectedText.length() > 0)
//				ps.setString(insertPos++, selectedText);
			rs = ps.executeQuery();
			while (rs.next()) {
				ComboItem c = new ComboItem(Misc.getRsetInt(rs, 1), rs.getString(2));
				retval.add(c);
				if (!Misc.isUndef(selectedValue)  && c.getValue() == selectedValue)
					selectedIndex = retval.size() - 1;
				else if ((selectedText != null && selectedText.length() > 0)  && c.getLabel().equalsIgnoreCase(selectedText))
					selectedIndex = retval.size() - 1;
			}
			if(retval.size() > 0 && Misc.isUndef(selectedIndex))
				selectedIndex=0;	
			
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return new Pair<Integer, ArrayList<ComboItem>>(selectedIndex, retval);
	}

	public static void main1(String[] arg) {
		System.out.println("LovDao");
	}
	
	public static String getText(int portNodeId,  LovItemType suggestionType,int selectedParam,int otherParam) {
		
		if (Misc.isUndef(selectedParam))
			return null;
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = null;
		String _name = "";
		switch (suggestionType) {
		case CUSTOMER:
			query = "Select customer_details.name from customer_details where customer_details.status=1 and customer_details.port_node_id=? and customer_details.id=? ";
//			query = "Select sap_customer_name from CGPL_SALES_ORDER WHERE  STATUS=1 AND cgpl_sales_order.port_node_id=? AND CGPL_SALES_ORDER.sap_customer_sap_code=? ";
			break;

		case TRANSPORTER:
			query = "SELECT transporter_details.name  FROM transporter_details where transporter_details.status=1 and transporter_details.port_node_id=? and transporter_details.id=?";
			break;

		case PO_SALES_ORDER:
			query = "SELECT cgpl_sales_order.sap_sales_order FROM cgpl_sales_order where cgpl_sales_order.status =1 and cgpl_sales_order.port_node_id=? and  cgpl_sales_order.id=? ";
			break;
		case PO_LINE_ITEM:
			query = "SELECT cgpl_sales_order.sap_line_item FROM cgpl_sales_order where cgpl_sales_order.status =1 and cgpl_sales_order.port_node_id=? and  cgpl_sales_order.id=? ";
			break;	
		default:
			break;
		}
		if (query == null)
			return null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			int insertParam = 1;
			if (!Misc.isUndef(portNodeId))
				Misc.setParamInt(ps, portNodeId, insertParam++);
			if (!Misc.isUndef(selectedParam))
				Misc.setParamInt(ps, selectedParam, insertParam++);
			if (!Misc.isUndef(otherParam))
				Misc.setParamInt(ps, otherParam, insertParam++);
			rs = ps.executeQuery();
			System.out.println(ps.toString());
			while (rs.next()) {
				_name = rs.getString(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return _name;
	}
	

public static String getText(int portNodeId,  LovItemType suggestionType,String textStr) {
	   String _name = "";
		if (Utils.isNull(textStr))
			return _name;
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = null;
		
		switch (suggestionType) {
		case CUSTOMER:
//			query = "Select customer_details.name from customer_details where customer_details.status=1 and customer_details.port_node_id=? and customer_details.id=? ";
			query = "Select sap_customer_name from CGPL_SALES_ORDER WHERE  STATUS=1 AND cgpl_sales_order.port_node_id=? AND CGPL_SALES_ORDER.sap_customer_sap_code=? ";
			break;

		case TRANSPORTER:
			query = "SELECT transporter_details.name  FROM transporter_details where transporter_details.status=1 and transporter_details.port_node_id=? and transporter_details.id=?";
			break;

		case DRIVER_NAME:
			query = "Select driver_name from driver_details where status=1 and driver_dl_number like '%" + textStr+ "%'";
			break;
		
		default:
			break;
		}
		if (query == null)
			return null;
		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			System.out.println(ps.toString());
			while (rs.next()) {
				_name = rs.getString(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return _name;
	}

	
	
	public static ArrayList<String> getVehicleSuggestion(int portNodeId, LovItemType suggestionType ) {
		ArrayList<String> retvalList = new ArrayList<String>();
		String query = null;
		switch (suggestionType) {
		case VEHICLE:
			query = " select vehicle.id,vehicle.std_name from vehicle"
				+ " where status in (1) and customer_id in (" + portNodeId+ ")" ;
			break;
		case TRANSPORTER:
			query = "SELECT id,name,sn FROM transporter_details where status=1 and port_node_id in (" + portNodeId+ ")" ;
			break;

		case CUSTOMER:
			query = "select id, name,sn from customer_details where status=1 and port_node_id in (" + portNodeId+ ")" ;
			break;
		case DL_NUMBER:
			query = "Select id,driver_dl_number from driver_details where status=1"; // and port_node_id in (" + portNodeId+ ")" ;
			break;	
			
		default:
			break;
		}
		if (query == null)
			return retvalList;
                
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			System.out.println(ps.toString());
			while (rs.next()) {
				retvalList.add(rs.getString(2));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Misc.closeRS(rs);
			Misc.closePS(ps);
			try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return retvalList;
	}
        
        
}
