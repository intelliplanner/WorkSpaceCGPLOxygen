package com.ipssi.input;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.csvreader.CsvReader;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.DimInfo.ValInfo;
import com.ipssi.mapping.common.db.DBQueries.PortNode;
import com.ipssi.tracker.common.util.AutoCompleteData;
import com.ipssi.tracker.web.ActionI;

public class UploadInputHelper implements ActionI{
	private SessionManager m_session = null;
	private static String RESPONSE_PAGE = "/uploadInputResponse.jsp";
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, Exception {
		String contentType = request.getContentType();
		Connection conn = InitHelper.helpGetDBConn(request);
		m_session = InitHelper.helpGetSession(request);
		conn = m_session.getConnection();
		StringBuilder error = new StringBuilder();
		StringBuilder result = new StringBuilder();
		String file = null;
		CsvReader dataReader = null;

		int portNodeId = Misc.getParamAsInt(m_session.getParameter("org_id"));
		if (Misc.isUndef(portNodeId)) {
			portNodeId = Misc.getParamAsInt(m_session.getParameter("pv123"));
		}
		if(Misc.isUndef(portNodeId)){
			portNodeId = Misc.getUserTrackControlOrg(m_session);
		}


		if ((contentType != null) ){
				//&& (contentType.indexOf("multipart/form-data") >= 0)) {
			boolean isMultipart = FileUpload.isMultipartContent(request);
			InputStream input = null;
			int sz = Misc.getUndefInt();
			HashMap<String, String> paramMap = new HashMap<String, String>();
			if (isMultipart) {
				DiskFileItemFactory fileFactory = new DiskFileItemFactory();
				ServletFileUpload fileUpload = new ServletFileUpload(fileFactory);
				List fileItems = fileUpload.parseRequest(request);
				Iterator iter = fileItems.iterator();
				while (iter.hasNext()) {
					FileItem item = (FileItem) iter.next();
					if (item.isFormField()) {
						paramMap.put(item.getFieldName(), item.getString());
					}else{
						input = item.getInputStream();
					}

				}
			}
			
			//Get an iterator
			
			/*String pgContext = paramMap.get("page_context");
			String templateName = paramMap.get("template_name");*/
			String pgContext = request.getParameter("page_context");
			String templateName = request.getParameter("template_name");
			if(templateName == null || templateName.length() == 0)
				return RESPONSE_PAGE;
			InputTemplate inputTemplate = InputTemplate.getTemplate(m_session.getCache(), conn, pgContext, portNodeId, templateName, m_session);
			if(inputTemplate == null || inputTemplate.getRows() == null || inputTemplate.getRows().size() == 0)
				return RESPONSE_PAGE;
			HashMap<String,DimConfigInfo> frontPageCellMap = new HashMap<String, DimConfigInfo>();
			ArrayList<ArrayList<DimConfigInfo>> rows = inputTemplate.getRows();

			for(int i=0,is=rows == null ? 0 : rows.size();i<is;i++){
				for(int j=0,js=rows.get(i) == null ? 0 : rows.get(i).size();j<js;j++){
					DimConfigInfo dimConf = (DimConfigInfo)rows.get(i).get(j);
					if(dimConf == null || dimConf.m_dimCalc == null || dimConf.m_dimCalc.m_dimInfo == null || dimConf.m_dimCalc.m_dimInfo.m_sn == null || dimConf.m_dimCalc.m_dimInfo.m_sn.length() == 0)
						continue;
					String cellCode = dimConf.m_dimCalc.m_dimInfo.m_sn;
					frontPageCellMap.put(cellCode,dimConf);
				}
			}
			
			if (input == null)
				input = request.getInputStream();
			if (sz < 0)
				sz = input.available();
			if(input != null){
				if (sz > 0) {
					DataInputStream in = new DataInputStream(input);

					int formDataLength = sz;
					byte dataBytes[] = new byte[formDataLength];
					int byteRead = 0;
					int totalBytesRead = 0;
					while (totalBytesRead < formDataLength) {
						byteRead = in.read(dataBytes, totalBytesRead, formDataLength);
						if(byteRead < 0)
							break;
						totalBytesRead += byteRead;
					}
					file = new String(dataBytes);
					if(file != null)
						dataReader = CsvReader.parse(file);
					if(dataReader != null){
						result = UploadInputHelper.processTemplateData(conn,m_session, dataReader, frontPageCellMap, error,portNodeId);
						request.setAttribute("response", result == null ? "-1":result.toString());
					}
				}
			}
		}
		return RESPONSE_PAGE;
	}
	public static StringBuilder  processTemplateData(Connection conn,SessionManager session, CsvReader data, HashMap<String,DimConfigInfo> frontPageCellMap,StringBuilder error,int portNodeId) throws Exception {
		StringBuilder xmlData = null; 
		int dataCount = 0;
		int errorCount = 0;
		int totalRows = 0;
		StringBuilder response = new StringBuilder();
		try {
			ArrayList<String> row = new ArrayList<String>();
			boolean isFirst = false;
			ArrayList<String> header = new ArrayList<String>();
			while (readRowFromCSV(data, row)) {
				totalRows++;
				try {
					if (isEmptyRow(row))
						continue;
					StringBuilder xmlRow = null;
					if(!isFirst){//create header {assumption first is header row}
						getXMLFromSingleRow(conn, session, row, frontPageCellMap, error,!isFirst,header,dataCount+1,portNodeId);
						isFirst = true;
						continue;
					}else{
						xmlRow = getXMLFromSingleRow(conn, session,row, frontPageCellMap, error,!isFirst,header,dataCount+1,portNodeId);
					}
					if(xmlRow == null){
						errorCount++;
						continue;
					}
					if(xmlData == null){
						xmlData = new StringBuilder();
						xmlData.append("<data>");
					}
					xmlData.append(xmlRow.toString());
					dataCount++;
				}
				catch (Exception e) {
					e.printStackTrace();
					//eat it
				}
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} 
		finally {

		}
		if(xmlData != null){
			xmlData.append("</data>");
		}
		response.append("{\"data\":\""+(xmlData == null ? "" : xmlData.toString())+"\",\"error\":\""+error.toString()+"\"}");
		System.out.println("[UITH]:("+totalRows+","+dataCount+","+errorCount+")");
		return response;
	}

	private static StringBuilder getXMLFromSingleRow(Connection conn,SessionManager session, ArrayList<String> data, HashMap<String,DimConfigInfo> frontPageCellMap,StringBuilder error, boolean isFirst, ArrayList<String> header,int rowNumber,int portNodeId) throws Exception {
		StringBuilder sb = null;
		StringBuilder rowErr = null;
		try {
			if(isFirst){
				for (int i = 0,is=data==null?0:data.size(); i < is; i++) {
					header.add(data.get(i));
				}
			}else{
				for(int i=0,is=header == null ? 0 : header.size();i<is;i++){
					String cellCode = header.get(i);
					String fieldVal = data.get(i);
					DimConfigInfo dimConf = frontPageCellMap.get(cellCode);
					if(dimConf == null)
						continue;
					if(sb == null){
						sb = new StringBuilder();
						sb.append("<r");
					}
					sb.append(" v"+dimConf.m_dimCalc.m_dimInfo.m_id).append("='");
					if(dimConf.m_dimCalc != null && dimConf.m_dimCalc.m_dimInfo != null && dimConf.m_dimCalc.m_dimInfo.m_type == Cache.LOV_TYPE){
						int lov = getLovValueFromCode(conn, session, dimConf, fieldVal, portNodeId);
						if(fieldVal != null && fieldVal.length() > 0 && Misc.isUndef(lov)){
							if(rowErr == null){
								rowErr = new StringBuilder();
								rowErr.append("<div>Row("+rowNumber+") ");
							}else{
								rowErr.append(", ");
							}
							rowErr.append(" Invalid code("+header.get(i)+")-"+fieldVal);
						}
						sb.append(lov);
					}else{
						sb.append(fieldVal);
					}
					sb.append("' ");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.append(e.getMessage());
		} finally {
		}
		if(sb != null)
			sb.append("/>");
		if(rowErr != null){
			error.append(rowErr.toString()).append("</div>");
		}
		return sb;
	}
	private static boolean readRowFromCSV(CsvReader data, ArrayList<String> row) throws Exception {
		boolean hasNext = data.readRecord();
		row.clear();
		if (hasNext) {
			boolean seenText = false;
			for (int i=0,is=data.getColumnCount(); i<is; i++) {
				String str = data.get(i);
				if (str != null)
					str = str.trim();
//				if (seenText ) {
					row.add(data.get(i));
//					seenText = true;
//				}
			}
		}
		return hasNext;
	}
	private static boolean isEmptyRow(ArrayList<String> row) {
		for (int i=0,is=row.size();i<is;i++) {
			if (row.get(i) != null && row.get(i).length() > 0)
				return false;
		}
		return true;
	}
	@Override
	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return RESPONSE_PAGE;
	}
	private static int getLovValueFromCode(Connection conn,SessionManager session,DimConfigInfo dimConfig,String code,int portNodeId){
		int retval = Misc.getUndefInt();
		if(conn == null || code == null || code.length() == 0 || dimConfig == null || dimConfig.m_dimCalc == null || dimConfig.m_dimCalc.m_dimInfo == null )
			return retval;
		try{
			DimInfo dimInfo = dimConfig.m_dimCalc.m_dimInfo;
			if(dimInfo.m_subsetOf == 70201){
				retval = AutoCompleteData.getAutoCompleteValue(conn, portNodeId, dimInfo.m_id, code, Misc.getUndefInt(), Misc.getUndefInt());
			}else{
				ArrayList<ValInfo> vals = dimInfo.getValList(conn, session);
				for (int i = 0,is= vals == null ? 0 : vals.size(); i < is; i++) {
					ValInfo val = vals.get(i);
					if(val == null)
						continue;
					String sn  = val.m_sn;
					if(code.equalsIgnoreCase(sn) || code.equalsIgnoreCase(val.m_name)){
						retval = val.m_id;
						break;
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return retval;
	}

}
