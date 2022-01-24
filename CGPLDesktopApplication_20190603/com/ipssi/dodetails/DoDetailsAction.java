package com.ipssi.dodetails;

import static com.ipssi.tracker.common.util.ApplicationConstants.ACTION;
import static com.ipssi.tracker.common.util.Common.getParamAsString;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
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
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.tracker.web.ActionI;
import com.ipssi.trip.challan.ChallanMaster;



public class DoDetailsAction implements ActionI {
	private SessionManager m_session = null;
	private final static String UPLOAD_DO_DETAILS = "/uploadDoDetails.jsp";
	private final static String INSERT_DO_DETAILS = "/uploadDoDetails.jsp";
	private final static String UPDATE_DO_DETAILS = "/uploadDoDetails.jsp";
	@Override
	public String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			Exception {

		String actionForward = "";
		boolean success = false;
		String contentType = request.getContentType();
		Connection conn = null;
		m_session = InitHelper.helpGetSession(request);
		conn = m_session.getConnection();
		StringBuilder str = new StringBuilder();
		Triple<Integer, Integer, StringBuilder> result = null;
  
		String action = getParamAsString(request.getParameter(ACTION));
		String file = null;
		CsvReader dataReader = null;
		DoDefinitionBean doDef = null;
		HashMap<Integer, DoParamBean> doParamList = null;
		if ("UPLOAD".equalsIgnoreCase(action)  && (contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
			SessionManager session = InitHelper.helpGetSession(request);
			int portNodeId = Misc.getParamAsInt(session.getParameter("org_id"));
			if (Misc.isUndef(portNodeId)) {
				portNodeId = Misc.getParamAsInt(session.getParameter("pv123"));
			}
			System.out.println("[CGRA]:incomming DO Mapping file");
			doDef =  DoInformation.getDODefinitionByPortNodeId(conn,portNodeId, true);
			boolean doUpdateDo = (request.getParameter("doUpdate")).equalsIgnoreCase("true") ? true : false;
			
			if(doDef != null){
				DoInformation.loadMinesDoDetails(conn,false);
				doParamList = doDef != null ? doDef.getDoParamList() : null;
				ArrayList<DimInfo.ValInfo> valList = DimInfo.getDimInfo("do_mapping_params") != null ? (ArrayList<DimInfo.ValInfo>)DimInfo.getDimInfo("do_mapping_params").getValList() : null;
				boolean isMultipart = FileUpload.isMultipartContent(request);
				String ext = null;
				InputStream input = null;
				int sz = Misc.getUndefInt();
				if (isMultipart) {
					DiskFileItemFactory fileFactory = new DiskFileItemFactory();
					ServletFileUpload fileUpload = new ServletFileUpload(fileFactory);
					List fileItems = fileUpload.parseRequest(request);

					//Get an iterator
					Iterator iter = fileItems.iterator();

					while (iter.hasNext()) {
						FileItem item = (FileItem) iter.next();
						if (!item.isFormField()) {
							input = item.getInputStream();
							sz = (int) item.getSize();
							String fname = item.getName();
							if (fname != null) {
								int dotIndex = 0;
								dotIndex = fname.lastIndexOf('.');

								if (dotIndex != -1) {
									ext = fname.substring(dotIndex+1);
								}
							}
							break;
						}
					}
				}

				boolean doingXLS2007 = "xlsx".equals(ext == null ? doDef.getFileName() : ext);
				boolean doingXLS2003 = "xls".equals(ext == null ? doDef.getFileName() : ext);
				if (input == null)
					input = request.getInputStream();
				if (sz < 0)
					sz = request.getContentLength();
				if(input != null){
					if (!doingXLS2003 && !doingXLS2007 && sz > 0) {
						DataInputStream in = new DataInputStream(input);

						int formDataLength = sz;
						byte dataBytes[] = new byte[formDataLength];
						int byteRead = 0;
						int totalBytesRead = 0;
						while (totalBytesRead < formDataLength) {
							byteRead = in.read(dataBytes, totalBytesRead, formDataLength);
							totalBytesRead += byteRead;
						}
						if(doDef.getCharset() != null && doDef.getCharset().length() > 0)
							file = new String(dataBytes,doDef.getCharset());
						else
							file = new String(dataBytes);
						if(file != null)
							dataReader = CsvReader.parse(file);
					//	if(dataReader != null)
						//	result = ChallanMaster.processContentCSV(conn, dataReader, portNodeId, doDef, doParamList, valList);
					}
					else {
						DataInputStream in = new DataInputStream(input);
						int userId = (int)m_session.getUserId();
						if (doingXLS2003)
							result = DoMaster.processContentXLS2003(conn, in, portNodeId, doDef, doParamList, valList,userId);
						else if (doingXLS2007)
							result = DoMaster.processContentXLS2007(conn, in, portNodeId, doDef, doParamList, valList,userId,doUpdateDo);
					}
				}
			}
		}else{
			
		}
				
		request.setAttribute("errorString", result != null && result.third != null ?  ((StringBuilder)result.third).toString(): "");
		request.setAttribute("resultStatus", result);
		success = true;
		actionForward = sendResponse(action, success, request);
		return actionForward;
	
	}

	@Override
	public String sendResponse(String action, boolean success,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		if ("UPLOAD".equalsIgnoreCase(action)) {
			return  UPLOAD_DO_DETAILS;
		} 
//		else if("INSERT".equalsIgnoreCase(action)) {
//			return SEARCH_PAGE;
//		}
//		else if("SEARCH_UPDATE".equalsIgnoreCase(action)){
//			return INSERT_PAGE;
//		}
//		else if("UPDATE".equalsIgnoreCase(action)){
//			return SEARCH_PAGE;
//		}
//		else if("SEARCH".equalsIgnoreCase(action)){
//			return SEARCH_PAGE;
//		}
		else
			return UPLOAD_DO_DETAILS;
	}

}
