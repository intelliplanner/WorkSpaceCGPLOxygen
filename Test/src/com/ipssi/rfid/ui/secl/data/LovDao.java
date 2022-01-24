package com.ipssi.rfid.ui.secl.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.ComboItem;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.secl.controller.MainWindow;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Created by ipssi11 on 16-Oct-16.
 */
public class LovDao {
	private static final String seperatorDel = "@";
    public static enum LovItemType {
    	ALLOWED_AREA,
    	VEHICLE,
    	AREA,
    	SUB_AREA,
    	MINES,
        SIDING,
        SIDING_AND_STOCK,
        DO,
        DO_ALL,
        TRANSPORTER,
        MATERIAL_GRADE,
        CUSTOMER,
        WASHERY,
        ROAD_DEST,
        RFID_TYPE,
        RFID_ISSUING_PURPOSE
    }
    public static Pair<String,String> getCodeNamePair(String text) {
    	String[] splitText = !Utils.isNull(text) ? text.split(seperatorDel) : null;
    	String first = splitText !=null && splitText.length > 0 ? splitText[0] : "";
    	String second = splitText !=null && splitText.length > 1 ? splitText[1] : "";
    	return new Pair<String, String>(first, second);
    }
    public static String getAutoCompleteValue(Node t){
    	String text = t != null ? ((t instanceof TextField) ? ((TextField)t).getText() : ((t instanceof Label) ? ((Label)t).getText() : null)) : null;
    	String[] textSplit = t == null || Utils.isNull(text) ? null : text.split(seperatorDel);
    	if(textSplit != null && textSplit.length > 0)
    		return textSplit[0];
    	return null;
    }
    
    public static Pair<String,String> getAutocompletePrintablePair(int portNodeId,String text,LovItemType suggestionType) {
    	Pair<Boolean,String> retval = getSuggestionPairByCode(portNodeId, text, suggestionType);
    	String[] splitText = !Utils.isNull(retval.second) ?  retval.second.split(seperatorDel) : null;
    	String first = splitText !=null && splitText.length > 0 ? splitText[0] : "";
    	String second = splitText !=null && splitText.length > 1 ? splitText[1] : "";
    	return new Pair<String, String>(first, second);
    }
    public static String getAutocompletePrintable(int portNodeId,String text,LovItemType suggestionType) {
    	Pair<Boolean,String> retval = getSuggestionPairByCode(portNodeId, text, suggestionType);
    	return Utils.isNull(retval.second) ? "" : retval.second;
    }
    public static boolean isAutocompleteNull(int portNodeId,String text,LovItemType suggestionType) {
    	return getSuggestionPairByCode(portNodeId, text, suggestionType).first;
    }
    public static Pair<Boolean,String> getSuggestionPairByCode(int portNodeId,String text,LovItemType suggestionType) {
    	if(text == null || text.length() == 0)
            return new Pair<Boolean, String>(false, null);
        Connection conn =null ;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = null;
        switch (suggestionType){
        	case VEHICLE : 
        		query = " select vehicle.id,vehicle.std_name from vehicle join " +
                    " (select distinct(vehicle.id) vehicle_id from vehicle " +
                    " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
                    " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) " +
                    " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) " +
                    " join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
                    " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
                    " where status in (1) and vehicle.std_name like '"+text+"'";
        		break;
        	case ALLOWED_AREA :
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',(case when type=4 then concat('Area :',name) when type=3 then concat('Sub Area :',name) when type=2 then concat('Washery :',name)when type=1 then concat('Siding :',name) else concat('Mines :',name) end)) name FROM mines_details where status=1 and port_node_id=? and sn like '"+text+"'";
        		break;
        	case AREA :
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM mines_details where type=4 and status=1 and port_node_id=? and sn like '"+text+"'";
        		break;
        	case SUB_AREA :
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM mines_details where type=3 and status=1 and port_node_id=? and sn like '"+text+"'";
        		break;
        	case MINES :
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM mines_details where type=0 and status=1 and port_node_id=? and sn like '"+text+"'";
        		break;
        	case DO:
        		query = "SELECT id,do_number FROM mines_do_details where status=1 and (prefered_wb_1='"+MainWindow.getWorkStationCode()+"' or prefered_wb_2='"+MainWindow.getWorkStationCode()+"' or prefered_wb_3='"+MainWindow.getWorkStationCode()+"' or prefered_wb_4='"+MainWindow.getWorkStationCode()+"') and port_node_id=? and do_number like '"+text+"'";
        		break;
        	case DO_ALL:
        		query = "SELECT id,do_number FROM mines_do_details where status=1 and port_node_id=? and do_number like '"+text+"'";
        		break;
        	case TRANSPORTER:
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM transporter_details where status=1 and port_node_id=? and sn like '"+text+"'";
        		break;
        	case MATERIAL_GRADE:
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM grade_details where status=1 and port_node_id=? and sn like '%"+text+"%'";
        		break;
        	case CUSTOMER:
        		query = "select id, concat(sn,'"+seperatorDel+"',name) from customer_details where status=1 and port_node_id=? and sn like '"+text+"'";
        		break;
        	case WASHERY:
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM mines_details where type=2 and status=1 and port_node_id=? and sn like '"+text+"'";
        		break;
        	case SIDING:
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM mines_details where type=1 and status=1 and port_node_id=? and sn like '"+text+"'";
        		break;
        	case SIDING_AND_STOCK:
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',(case when type=6 then concat('Stock: ',name)  else concat('Siding :',name) end)) FROM mines_details where type in (1,6) and status=1 and port_node_id=? and sn like '"+text+"'";
        		break;
        		
        	case ROAD_DEST:
        		query = "select id, concat(sn,'"+seperatorDel+"',name) from destination_details where status=1 and port_node_id=? and sn like '"+text+"'";
        		break;
        	default:break;
        }
        if(query == null)
        	return new Pair<Boolean, String>(false, null);
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            ps = conn.prepareStatement(query);
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
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn);
            }catch (Exception ex){
                    ex.printStackTrace();
            }
        }
        return new Pair<Boolean, String>(false, null);
    }
    public static ArrayList<String> getFieldSuggestion(int portNodeId,String text,LovItemType suggestionType) {
        if(text == null || text.length() == 0)
            return null;
        Connection conn =null ;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = null;
        switch (suggestionType){
        	case VEHICLE : 
        		query = " select vehicle.id,vehicle.std_name from vehicle join " +
                    " (select distinct(vehicle.id) vehicle_id from vehicle " +
                    " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
                    " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) " +
                    " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) " +
                    " join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
                    " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
                    " where status in (1) and vehicle.std_name like '%"+text+"%'";
        		break;
        	case ALLOWED_AREA :
        		query = "SELECT id,sn  FROM mines_details where status=1 and port_node_id=? and concat(sn,'-',(case when type=4 then concat('Area :',name) when type=3 then concat('Sub Area :',name) when type=2 then concat('Washery :',name)when type=1 then concat('Siding :',name) else concat('Mines :',name) end)) like '%"+text+"%'";
        		break;
        	case AREA :
        		query = "SELECT id,sn FROM mines_details where type=4 and status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case SUB_AREA :
        		query = "SELECT id,sn FROM mines_details where type=3 and status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case MINES :
        		query = "SELECT id, sn FROM mines_details where type=0 and status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case DO:
        		query = "SELECT id,do_number FROM mines_do_details where status=1 and (prefered_wb_1='"+MainWindow.getWorkStationCode()+"' or prefered_wb_2='"+MainWindow.getWorkStationCode()+"' or prefered_wb_3='"+MainWindow.getWorkStationCode()+"' or prefered_wb_4='"+MainWindow.getWorkStationCode()+"') and port_node_id=? and do_number like '%"+text+"%'";
        		break;
        	case DO_ALL:
        		query = "SELECT id,do_number FROM mines_do_details where status=1 and port_node_id=? and do_number like '%"+text+"%'";
        		break;
        	case TRANSPORTER:
        		query = "SELECT id, sn FROM transporter_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case MATERIAL_GRADE:
        		query = "SELECT id , sn FROM grade_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case CUSTOMER:
        		query = "select id, sn from customer_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case WASHERY:
        		query = "SELECT id, sn FROM mines_details where type=2 and status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case SIDING:
        		query = "SELECT id, sn FROM mines_details where type=1 and status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case SIDING_AND_STOCK:
        		query = "SELECT id, sn FROM mines_details where type in (1,6) and status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case ROAD_DEST:
        		query = "select id, sn from destination_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	default:break;
        }
        if(query == null)
        	return null;
        ArrayList<String> suggestionList = new ArrayList<>();
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
            }catch (Exception ex){
                    ex.printStackTrace();
            }
        }
        return suggestionList;
    }
    public static ArrayList<String> getFieldSuggestionMerged(int portNodeId,String text,LovItemType suggestionType) {
        if(text == null || text.length() == 0)
            return null;
        Connection conn =null ;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = null;
        switch (suggestionType){
        	case VEHICLE : 
        		query = " select vehicle.id,vehicle.std_name from vehicle join " +
                    " (select distinct(vehicle.id) vehicle_id from vehicle " +
                    " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
                    " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) " +
                    " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) " +
                    " join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
                    " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
                    " where status in (1) and vehicle.std_name like '%"+text+"%'";
        		break;
        	case ALLOWED_AREA :
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',(case when type=4 then concat('Area :',name) when type=3 then concat('Sub Area :',name) when type=2 then concat('Washery :',name)when type=1 then concat('Siding :',name) else concat('Mines :',name) end)) name FROM mines_details where status=1 and port_node_id=? and concat(sn,'-',(case when type=4 then concat('Area :',name) when type=3 then concat('Sub Area :',name) when type=2 then concat('Washery :',name)when type=1 then concat('Siding :',name) else concat('Mines :',name) end)) like '%"+text+"%'";
        		break;
        	case AREA :
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM mines_details where type=4 and status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case SUB_AREA :
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM mines_details where type=3 and status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case MINES :
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM mines_details where type=0 and status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case DO:
        		query = "SELECT id,do_number FROM mines_do_details where status=1 and (prefered_wb_1='"+MainWindow.getWorkStationCode()+"' or prefered_wb_2='"+MainWindow.getWorkStationCode()+"' or prefered_wb_3='"+MainWindow.getWorkStationCode()+"' or prefered_wb_4='"+MainWindow.getWorkStationCode()+"') and port_node_id=? and do_number like '%"+text+"%'";
        		break;
        	case DO_ALL:
        		query = "SELECT id,do_number FROM mines_do_details where status=1 and port_node_id=? and do_number like '%"+text+"%'";
        		break;
        	case TRANSPORTER:
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM transporter_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case MATERIAL_GRADE:
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM grade_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case CUSTOMER:
        		query = "select id, concat(sn,'"+seperatorDel+"',name) from customer_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case WASHERY:
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM mines_details where type=2 and status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case SIDING:
        		query = "SELECT id,concat(sn,'"+seperatorDel+"',name) FROM mines_details where type=1 and status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	case ROAD_DEST:
        		query = "select id, concat(sn,'"+seperatorDel+"',name) from destination_details where status=1 and port_node_id=? and concat(sn,'-',name) like '%"+text+"%'";
        		break;
        	default:break;
        }
        if(query == null)
        	return null;
        ArrayList<String> suggestionList = new ArrayList<>();
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
            }catch (Exception ex){
                    ex.printStackTrace();
            }
        }
        return suggestionList;
    }
    
    public static Pair<String,String> getFieldText(int portNodeId,int id,LovItemType suggestionType,String undef) {
    	Pair<String,String> retval = new Pair<String, String>(undef, undef);
    	if(Misc.isUndef(id))
            return retval;
        Connection conn =null ;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = null;
        switch (suggestionType){
        	case VEHICLE : 
        		query = " select vehicle.id,vehicle.std_name,vehicle.std_name from vehicle join " +
                    " (select distinct(vehicle.id) vehicle_id from vehicle " +
                    " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
                    " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) " +
                    " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) " +
                    " join port_nodes anc  on (anc.id in (?) and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
                    " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
                    " where status in (1) and vehicle.id=?";
        		break;
        	case ALLOWED_AREA :
        		query = "SELECT id,(case when type=4 then concat('Area :',name) when type=3 then concat('Sub Area :',name) when type=2 then concat('Washery :',name)when type=1 then concat('Siding :',name) else concat('Mines :',name) end) name,sn FROM mines_details where status=1 and port_node_id=? and id=?";
        		break;
        	case AREA :
        		query = "SELECT id,name,sn FROM mines_details where type=4 and status=1 and port_node_id=? and id=?";
        		break;
        	case SUB_AREA :
        		query = "SELECT id,name,sn FROM mines_details where type=3 and status=1 and port_node_id=? and id=?";
        		break;
        	case MINES :
        		query = "SELECT id,name,sn FROM mines_details where type=0 and status=1 and port_node_id=? and id=?";
        		break;
        	case DO:
        		query = "SELECT id,do_number,sn FROM mines_do_details where status=1 and (prefered_wb_1='"+MainWindow.getWorkStationCode()+"' or prefered_wb_2='"+MainWindow.getWorkStationCode()+"' or prefered_wb_3='"+MainWindow.getWorkStationCode()+"' or prefered_wb_4='"+MainWindow.getWorkStationCode()+"') and port_node_id=? and id=?";
        		break;
        	case DO_ALL:
        		query = "SELECT id,do_number,sn FROM mines_do_details where status=1 and port_node_id=? and id=?";
        		break;
        	case TRANSPORTER:
        		query = "SELECT id,name,sn FROM transporter_details where status=1 and port_node_id=? and id=?";
        		break;
        	case MATERIAL_GRADE:
        		query = "SELECT id,name,sn FROM grade_details where status=1 and port_node_id=? and id=?";
        		break;
        	case CUSTOMER:
        		query = "select id, name,sn from customer_details where status=1 and port_node_id=? and id=?";
        		break;
        	case WASHERY:
        		query = "SELECT id,name,sn FROM mines_details where type=2 and status=1 and port_node_id=? and id=?";
        		break;
        	case SIDING:
        		query = "SELECT id,name,sn FROM mines_details where type=1 and status=1 and port_node_id=? and id=?";
        		break;
        	case ROAD_DEST:
        		query = "select id, name,sn from destination_details where status=1 and port_node_id=? and id=?";
        		break;
        	default:break;
        }
        if(query == null)
        	return retval;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            ps = conn.prepareStatement(query);
            Misc.setParamInt(ps, portNodeId, 1);
            Misc.setParamInt(ps, id, 2);
            rs = ps.executeQuery();
            if (rs.next()) {
                retval = new Pair<String, String>(rs.getString(2),rs.getString(3));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Misc.closeRS(rs);
            Misc.closePS(ps);
            try {
            	if(conn != null)
                DBConnectionPool.returnConnectionToPoolNonWeb(conn);
            }catch (Exception ex){
                    ex.printStackTrace();
            }
        }
        return retval;
    }
    public static ArrayList<String> getVehicleSuggestion(int portNodeId,String text) {
        if(text == null || text.length() == 0)
            return null;
        Connection conn =null ;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String> suggestionList = new ArrayList<>();
        String query = " select vehicle.id,vehicle.std_name from vehicle join " +
                " (select distinct(vehicle.id) vehicle_id from vehicle " +
                " left outer join port_nodes custleaf on (custleaf.id = vehicle.customer_id) " +
                " left outer join vehicle_access_groups on (vehicle_access_groups.vehicle_id = vehicle.id) " +
                " left outer join port_nodes leaf on (leaf.id = vehicle_access_groups.port_node_id) " +
                " join port_nodes anc  on (anc.id in ("+ portNodeId+") and ((anc.lhs_number <= leaf.lhs_number and anc.rhs_number >= leaf.rhs_number) " +
                " or  (anc.lhs_number <= custleaf.lhs_number and anc.rhs_number >= custleaf.rhs_number))) ) vi on vi.vehicle_id = vehicle.id " +
                " where status in (1) and vehicle.std_name like '%"+text+"%'";
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
            }catch (Exception ex){
                    ex.printStackTrace();
            }
        }
        return suggestionList;
    }
    public static ArrayList<String> getDoSuggestion(int portNodeId,String text) {
        if(text == null || text.length() == 0)
            return null;
        Connection conn =null ;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String> suggestionList = new ArrayList<>();
        String query = " select id,do_number from do_rr_details where port_node_id=?";
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            ps = conn.prepareStatement(query);
            Misc.setParamInt(ps,portNodeId,1);
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
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return suggestionList;
    }
    public static Pair<Integer,ArrayList<ComboItem>> getLovItemPair(Connection conn,int portNodeId,LovItemType lovItemType, int selectedValue) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<ComboItem> retval = new ArrayList<>();
        int selectedIndex = Misc.getUndefInt();
        String query = null;
        switch (lovItemType){
        	case ALLOWED_AREA :
    			query = "SELECT id,(case when type=4 then concat('Area :',name) when type=3 then concat('Sub Area :',name) when type=2 then concat('Washery :',name)when type=1 then concat('Siding :',name) else concat('Mines :',name) end) name FROM mines_details where status=1 and port_node_id=? ";
    		break;
        	case AREA :
        		query = "SELECT id,name FROM mines_details where type=4 and status=1 and port_node_id=?";
        		break;
        	case SUB_AREA :
                query = "SELECT id,name FROM mines_details where type=3 and status=1 and port_node_id=?";
                break;
            case MINES :
                query = "SELECT id,name FROM mines_details where type=0 and status=1 and port_node_id=?";
                break;
            case DO:
                query = "SELECT id,do_number FROM mines_do_details where status=1 and (prefered_wb_1='"+MainWindow.getWorkStationCode()+"' or prefered_wb_2='"+MainWindow.getWorkStationCode()+"' or prefered_wb_3='"+MainWindow.getWorkStationCode()+"' or prefered_wb_4='"+MainWindow.getWorkStationCode()+"') and port_node_id=?";
                break;
            case DO_ALL:
                query = "SELECT id,do_number FROM mines_do_details where status=1 and port_node_id=?";
                break;
            case TRANSPORTER:
                query = "SELECT id,name FROM transporter_details where status=1 and port_node_id=?";
                break;
            case MATERIAL_GRADE:
                query = "SELECT id,name FROM grade_details where status=1 and port_node_id=?";
                break;
            case CUSTOMER:
            	query = "select id, name from customer_details where status=1 and port_node_id=?";
                break;
            case WASHERY:
                query = "SELECT id,name FROM mines_details where type=2 and status=1 and port_node_id=?";
                break;
            case SIDING:
                query = "SELECT id,name FROM mines_details where type=1 and status=1 and port_node_id=?";
                break;
            case ROAD_DEST:
            	query = "select id, name from destination_details where status=1 and port_node_id=?";
            	break;
            default:break;
        }
        if(query == null)
            return null;
        try {
            conn = DBConnectionPool.getConnectionFromPoolNonWeb();
            ps = conn.prepareStatement(query);
            Misc.setParamInt(ps,portNodeId,1);
            rs = ps.executeQuery();
            while (rs.next()) {
                ComboItem c = new ComboItem(Misc.getRsetInt(rs,1),rs.getString(2));
                retval.add(c);
                if(c.getValue() == selectedValue)
                    selectedIndex = retval.size()-1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Misc.closeRS(rs);
            Misc.closePS(ps);
            try {
                DBConnectionPool.returnConnectionToPoolNonWeb(conn);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return new Pair<>(selectedIndex,retval);
    }
    public static void main(String[] arg) {
        System.out.println("LovDao");
    }
}
