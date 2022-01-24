package com.ipssi.angular;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.reporting.trip.TR;
import com.ipssi.reporting.trip.Table;

public class ReportManager {
	private static final Gson gson = new Gson();

	public static String getFPJson(Connection conn, SessionManager session, FrontPageInfo frontPageInfo) {
		return gson.toJson(ReportConfig.getReport(conn, session, frontPageInfo));
	}

	public static class ReportConfig {
		ArrayList<Column> uicolumn = null;
		ArrayList<ArrayList<Column>> uiParameter = null;

		public static ReportConfig getReport(Connection conn, SessionManager session, FrontPageInfo frontPageInfo) {
			if (frontPageInfo == null)
				return null;
			ReportConfig retval = new ReportConfig();
			for (int i = 0, is = frontPageInfo.m_frontInfoList == null ? 0
					: frontPageInfo.m_frontInfoList.size(); i < is; i++) {
				DimConfigInfo dimConfigInfo = (DimConfigInfo) frontPageInfo.m_frontInfoList.get(i);
				DimInfo dimInfo = dimConfigInfo == null || dimConfigInfo.m_dimCalc == null ? null
						: dimConfigInfo.m_dimCalc.m_dimInfo;
				if (dimInfo != null) {
					Column col = Column.getColum(conn, session, dimConfigInfo, false);
					if(col != null){
						if (retval.uicolumn == null)
							retval.uicolumn = new ArrayList<Column>();
						retval.uicolumn.add(col);
					}
				}
			}
			for (int i = 0, is = frontPageInfo.m_frontSearchCriteria == null ? 0
					: frontPageInfo.m_frontSearchCriteria.size(); i < is; i++) {
				ArrayList rows = (ArrayList) frontPageInfo.m_frontSearchCriteria.get(i);
				ArrayList<Column> row = null;
				for (int j = 0, js = rows == null ? 0 : rows.size(); j < js; j++) {
					DimConfigInfo dimConfigInfo = (DimConfigInfo) rows.get(j);
					DimInfo dimInfo = dimConfigInfo == null || dimConfigInfo.m_dimCalc == null ? null
							: dimConfigInfo.m_dimCalc.m_dimInfo;
					if (dimInfo != null) {
						Column col = Column.getColum(conn, session, dimConfigInfo, true);
						if(col != null){
							if (row == null)
								row = new ArrayList<Column>();
							row.add(col);
						}
					}
				}
				if (row != null) {
					if (retval.uiParameter == null)
						retval.uiParameter = new ArrayList<ArrayList<Column>>();
					retval.uiParameter.add(row);
				}

			}
			return retval;
		}
	}

	public static class Column {
		private int key;
		private String label;
		private String placeholder;
		private int type;
		private String subType;
		private boolean hidden;
		private boolean mandatory;
		private String defaultVal;
		private boolean multiple;
		private boolean autocomple;
		private ArrayList<BasicSelectionModel> basicSelectionModel;
		private ArrayList<TreeSelectionModel> treeSelectionModel;

		public Column(DimConfigInfo dimConfigInfo, DimInfo dimInfo) {
			this.key = dimInfo.m_id;
			this.label = dimInfo.m_catName;
			this.placeholder = dimConfigInfo.m_internalName;
			this.type = dimInfo.m_type;
			this.subType = dimInfo.m_subtype;
			this.hidden = dimConfigInfo.m_hidden;
			this.mandatory = dimConfigInfo.m_isMandatory;
			this.defaultVal = dimConfigInfo.m_default;
			this.multiple = dimConfigInfo.m_multiSelect;
			this.autocomple = dimConfigInfo.m_isAutocomplete;
		}
		public static Column getColum(Connection conn, SessionManager session, DimConfigInfo dimConfigInfo, boolean isSearchParam){
			DimInfo dimInfo = dimConfigInfo == null || dimConfigInfo.m_dimCalc == null ? null
					: dimConfigInfo.m_dimCalc.m_dimInfo;
			Column retval = null;
			if (dimInfo != null) {
				retval = new Column(dimConfigInfo, dimInfo);
				if(isSearchParam){
					if(retval.type == Cache.LOV_TYPE){ //lovModel
						if(dimInfo.m_descDataDimId == 123){//org selection model
							retval.type = 0;
						}else{
						try {
							ArrayList valList = dimInfo.getValList(conn, session);
							ArrayList<BasicSelectionModel> basicSelectionModel = null; 
							for (int i=0,is=valList == null ? 0 :valList.size();i<is;i++) {          
		                        DimInfo.ValInfo valInfo = (DimInfo.ValInfo) valList.get(i);
		                        BasicSelectionModel model = new BasicSelectionModel();
		                        model.id = valInfo.m_id;
		                        model.value = valInfo.m_name;
		                        model.sn = valInfo.m_sn;
		                        model.v1 = valInfo.m_str_field1;
		                        model.v2 = valInfo.m_str_field2;
		                        model.v3 = valInfo.m_str_field3;
		                        model.v4 = valInfo.m_str_field4;
		                        if(basicSelectionModel == null)
		                        	basicSelectionModel = new ArrayList<ReportManager.BasicSelectionModel>();
		                        basicSelectionModel.add(model);
							}
							if(basicSelectionModel != null)
								retval.basicSelectionModel = basicSelectionModel;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						}
					}
				}
			}
			return retval;
		}

	}

	public static class BasicSelectionModel{
		public int id;
		public String sn;
		public String value;
		public String v1;
		public String v2;
		public String v3;
		public String v4;
	}
	public static class TreeSelectionModel{
		public String label;
		public int id;
		public int parent;
		public int lhs;
		public int rhs;
		public String v1;
		public String v2;
	}
	public static class ReportData{
		private String title;
		private DataTable table;
		private int status;
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public DataTable getTable() {
			return table;
		}
		public void setTable(DataTable table) {
			this.table = table;
		}
		
		public ReportData() {
			super();
		}
		
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public ReportData(String title, DataTable table) {
			super();
			this.title = title;
			this.table = table;
			
		}
		public ReportData(int status) {
			super();
			this.status = status;
		}
		 
	}
	public static class DataTable{
		private JSONObject headerColumns;
		private ArrayList<JSONObject> rows;
		private ArrayList<Integer> columns;
		public JSONObject getHeaderColumns() {
			return headerColumns;
		}
		public void setHeaderColumns(JSONObject headerColumns) {
			this.headerColumns = headerColumns;
		}
		public ArrayList<JSONObject> getRows() {
			return rows;
		}
		public void setRows(ArrayList<JSONObject> rows) {
			this.rows = rows;
		}
		public ArrayList<Integer> getColumns() {
			return columns;
		}
		public void setColumns(ArrayList<Integer> columns) {
			this.columns = columns;
		}
		public DataTable() {
			super();
		}
		public DataTable(ArrayList<Integer> columns, ArrayList<JSONObject> rows, JSONObject headerColumns) {
			super();
			this.columns = columns;
			this.rows = rows;
			this.headerColumns = headerColumns;
		}	
	}
	public static String convertToJsonString(Connection conn, SessionManager _session, Table table, String reportName) throws JSONException{
		JSONObject retval = new JSONObject();
		
		if(table == null || table.getBody() == null || table.getBody().size() == 0){
			retval.putOpt("status",-1);
			return retval.toString();
		}
		retval.putOpt("status",1);
		retval.putOpt("title",reportName);
		ArrayList<TR> header = table.getHeader();
		ArrayList<TR> body = table.getBody();
		JSONObject headerColumns = null;
		JSONArray columns = null;
		//ArrayList<String> keys = null;
		int blankIndex = 0;
		int index = 0;
		for (int i = 0, is=header == null ? 0 : header.size(); i < is; i++) {
			for (int j = 0, js=header.get(i).getRowData() == null ? 0 : header.get(i).getRowData().size(); j < js; j++) {
				int id = header.get(i).getRowData().get(j).getId();
				if(id <= 0)
					continue;
				if(header.get(i).getRowData().get(j).getHidden())
					continue;
				if(i == 0){
					if(header.get(i).getRowData().get(j).getColSpan() > 1){
						//blankIndex = columns == null ? 0 : columns.length();
						index += header.get(i).getRowData().get(j).getColSpan();
						continue;
					}
				}else{
					for (int k = 0, ks = columns == null ? 0 : columns.length(); k < ks; k++) {
						if("#".equalsIgnoreCase(columns.getString(k))){
							blankIndex = k;
							break;
						}
					}
				}
				String label = header.get(i).getRowData().get(j).getContent();
				String key="d"+id+"_"+label.replaceAll(" ", "_").toLowerCase();
				if(columns == null)
					columns = new JSONArray();
				if(blankIndex > 0 && blankIndex < columns.length())
					columns.put(blankIndex, key);
				else{
					if(index >= columns.length()){
						int size = (index-columns.length());
					for (int k = 0; k < size; k++) {
						columns.put("#");
					}
					}
					columns.put(key);
					index++;
					
					
				}
				JSONObject col = new JSONObject();
				//col.putOpt("id", id+"");
				col.putOpt("label", label);
				if(headerColumns == null)
					headerColumns = new JSONObject();
				headerColumns.putOpt(key, col);
			}
		}
		JSONArray dataColumns = null;
		for (int i = 0, is=body == null ? 0 : body.size(); i < is; i++) {
			JSONObject row = null;
			int count = 0;
			for (int j = 0, js=body.get(i).getRowData() == null ? 0 : body.get(i).getRowData().size(); j < js; j++) {
				int id = body.get(i).getRowData().get(j).getId();
				if(body.get(i).getRowData().get(j).getColSpan() > 1 || body.get(i).getRowData().get(j).getHidden())
					continue;
				if(id <= 0)
					continue;
				if(row == null)
					row = new JSONObject();
				String content = body.get(i).getRowData().get(j).getContent();
				if(content != null)
					content = content.replaceAll("&nbsp;", "");
				JSONObject col = new JSONObject();
				//col.putOpt("id", "d"+id);
				col.putOpt("content", content);
				col.putOpt("type", body.get(i).getRowData().get(j).getContentType());
				String key=columns.getString(count++);
				row.putOpt(key, col);
			}
			if(dataColumns == null)
				dataColumns = new JSONArray();
			dataColumns.put(row);
		}
		JSONObject tableObj = new JSONObject();
		tableObj.putOpt("rows", dataColumns);
		tableObj.putOpt("columns", columns);
		tableObj.putOpt("headerColumns", headerColumns);
		retval.putOpt("table",tableObj);
		return retval.toString();
	}
	public static class Col{
		private int id;

		public Col(int id) {
			super();
			this.id = id;
		}
	}
	public static class Reo{
		private ArrayList<Col> s;

		public Reo(ArrayList<Col> s) {
			super();
			this.s = s;
		}
		
	}
	public static void main(String[] arg){
		
		ArrayList<Col> s = new ArrayList<Col>(Arrays.asList(new Col(1),new Col(2),new Col(3),new Col(4)));
		System.out.println(gson.toJson(new Reo(s)));
	}
}
