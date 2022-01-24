package com.ipssi.tracker.common.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;

public class AutoCompleteData {
	private String label;
	private String value;
	private ArrayList<String> dataList;
	public AutoCompleteData(){
		
	}
	public AutoCompleteData(String label,String value){
		this.value = value;
		this.label = label;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public ArrayList<String> getDataList() {
		return dataList;
	}
	public void setDataList(ArrayList<String> dataList) {
		this.dataList = dataList;
	}
	public void addExtraValue(String val){
		if(dataList == null)
			dataList = new ArrayList<String>();
		dataList.add(val);
	}
	public String toString(){
		StringBuilder retval =  new StringBuilder();
		retval.append("{");
		retval.append("\"label\" : \"").append(label).append("\",");
		retval.append("\"value\" : \"").append(value).append("\"");
		if(dataList != null && dataList.size() > 0){
			int size = dataList.size();
			for(int i=0; i<size; i++)
				retval.append(",field_"+i).append(" : '").append(dataList.get(i)).append("'");
		}
		retval.append("}");
	    return retval.toString();
	}
	public static StringBuilder getAutoCompleteData(Connection conn,int portNodeId,int dimId,String label, int addnlDimId, int addnlVal){
		StringBuilder sb = new StringBuilder();
		ArrayList<AutoCompleteData> dataList = getAutoCompleteResult(conn, portNodeId, dimId, label, addnlDimId, addnlVal);
		AutoCompleteData data = null;
		sb.append("[");
		for(int i=0,is=dataList == null ? 0:dataList.size();i<is;i++){
			data = dataList.get(i);
			if (sb.length()>1) {
				sb.append(",");
			}
			sb.append(data.toString());
		}
		sb.append("]");
		return sb;
	}
	public static int getAutoCompleteValue(Connection conn,int portNodeId,int dimId,String label, int addnlDimId, int addnlVal){
		int retval = Misc.getUndefInt();
		ArrayList<AutoCompleteData> dataList = getAutoCompleteResult(conn, portNodeId, dimId, label, addnlDimId, addnlVal);
		AutoCompleteData data = null;
		for(int i=0,is=dataList == null ? 0:dataList.size();i<is;i++){
			data = dataList.get(i);
			retval = Misc.getParamAsInt(data.value);
			if(!Misc.isUndef(retval))
				break;
		}
		return retval;
	}
	public static ArrayList<AutoCompleteData> getAutoCompleteResult(Connection conn,int portNodeId,int dimId,String label, int addnlDimId, int addnlVal){
		StringBuilder sb = null;
		DimInfo dimInfo = null;
		ArrayList<AutoCompleteData> retval = null;
		AutoCompleteData data = null;
		try{
			dimInfo =DimInfo.getDimInfo(dimId);
			String baseTable = null;
			String nameField = null;
			String idField = null;
			if(dimInfo != null && dimInfo.m_colMap != null){
				baseTable =  dimInfo.m_colMap.base_table;
				idField =  dimInfo.m_colMap.idField;
				nameField =  dimInfo.m_colMap.nameField;
			}
			if(baseTable == null || baseTable.length() <= 0 || idField == null || idField.length() <= 0 || nameField == null || nameField.length() <= 0 )
				return null;
			if ("do_rr_details".equals(baseTable))
				baseTable += "_apprvd";
			StringBuilder query = new StringBuilder();
			String adjNameField = nameField;
			if (nameField.contains(baseTable))
				adjNameField = nameField;
			else
				adjNameField = baseTable+"."+nameField;
			
			query.append(" SELECT DISTINCT ")
			.append(baseTable).append(".").append(idField)
			.append(",").append(adjNameField)
			.append(" from port_nodes sel JOIN port_nodes anc on") 
			.append(" (anc.lhs_number <= sel.lhs_number and anc.rhs_number >= sel.rhs_number) join port_nodes leaf ON") 
			.append(" (sel.lhs_number <= leaf.lhs_number and sel.rhs_number >= leaf.rhs_number) join ")
			.append(baseTable).append(" on ") 
			.append(" (").append(baseTable).append("vehicle".equalsIgnoreCase(baseTable) ? ".customer_id" : ".port_node_id").append(" = anc.id ").append(" or ").append(baseTable).append("vehicle".equalsIgnoreCase(baseTable) ? ".customer_id" : ".port_node_id").append("= leaf.id)")  
			.append(" where ");
			if(!Misc.isUndef(portNodeId))
				query.append("sel.id=").append(portNodeId).append(" and ");
			query.append(baseTable).append(".status=1");
			//String query = "select "+idField+", "+nameField+" from "+baseTable+" ";
			if(label != null && label.trim().length() > 0 )
				query.append(" and ").append(adjNameField).append(" like '%").append(label).append("%' ");
			
			if (addnlDimId >= 0 && addnlVal >= 0) {
				DimInfo adim = DimInfo.getDimInfo(addnlDimId);
				if (adim != null) {//HACK ... later figure out how to do FK join
					if ("do_rr_details".equals(baseTable) || "do_rr_details_apprvd".equals(baseTable)) {
						query.append(" and do_rr_details_apprvd.mines_id in (").append(addnlVal).append(")");
					}
				}
			}
			if(dimInfo.m_subsetOf==80179)//mines
				query.append(" and mines_details.type in (").append("0,6").append(")");
			if(dimInfo.m_subsetOf==80180)//sub area
				query.append(" and mines_details.type in (").append(3).append(")");
			if(dimInfo.m_subsetOf==80181)//area
				query.append(" and mines_details.type in (").append(4).append(")");
			if(dimInfo.m_subsetOf==93458)//siding
				query.append(" and mines_details.type in (").append("1,6").append(")");
			if(dimInfo.m_subsetOf==93376)//washery
				query.append(" and mines_details.type in (").append(2).append(")");
			PreparedStatement ps = conn.prepareStatement(query.toString());
			System.out.println(query.toString());
			ResultSet rs = ps.executeQuery();
			sb = new StringBuilder();
			//sb.append("[");
			while (rs.next()) {
				
				data = new AutoCompleteData(rs.getString(2),rs.getString(1));
				if(retval == null)
					retval = new ArrayList<AutoCompleteData>();
				retval.add(data);
				/*if (sb.length()>1) {
					sb.append(",");
				}
				sb.append(data.toString());*/
			}
			//sb.append("]");
			Misc.closeRS(rs);
			Misc.closePS(ps);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}
	
}
