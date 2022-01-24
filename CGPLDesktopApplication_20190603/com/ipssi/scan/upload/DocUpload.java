package com.ipssi.scan.upload;


import static com.ipssi.tracker.common.util.ApplicationConstants.SEARCH;
import static com.ipssi.tracker.common.util.ApplicationConstants.VIEW;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Iterator;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ipssi.cbse.reports.CBSEReportDao;
import com.ipssi.gen.utils.InitHelper;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.tracker.common.exception.GenericException;
import com.ipssi.tracker.common.util.Common;
import com.ipssi.tracker.web.ActionI;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;


public class DocUpload implements ActionI{
	private static final long serialVersionUID = 1L;
	private boolean isMultipart;
	private String filePath;
	private int maxFileSize = 500 * 1024 * 1024 ;
//	private int maxMemSize = 4 * 1024 *1024; 
	private SessionManager m_session = null;
	//private File file ;
	
	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException, Exception {
		System.out.println("uploadDoc.processRequest()   $##################  ");
		boolean success = false;
		Connection conn = null;
		m_session = InitHelper.helpGetSession(request);
		conn = m_session.getConnection();
		String actionForward = "";
		//try{
				System.out.println("Processing Request");
				String tripId = request.getParameter("id");
				System.out.println("TripId"+tripId);
				isMultipart = ServletFileUpload.isMultipartContent(request);
				if( isMultipart ){
					System.out.println("Multipart Request");
					DiskFileItemFactory factory = new DiskFileItemFactory();
					//factory.setSizeThreshold(maxMemSize);
					//factory.setRepository(new File("D:/upload/"));
					ServletFileUpload upload = new ServletFileUpload(factory);
					upload.setSizeMax( maxFileSize );
					
				      List fileItems = upload.parseRequest(request);
				      Iterator i = fileItems.iterator();
				      while ( i.hasNext () ) 
				      {
						 System.out.println("Fetch File");
				         FileItem fi = (FileItem)i.next();
				         if ( !fi.isFormField () )	
				         {
				     		ScanDocumentDao scanDao = new ScanDocumentDao();
				     		scanDao.insertData(conn, tripId, fi);
				         }
				         else
				         {
				        	 System.out.println("Error in File Upload");
				         }
				      }
				}
				else
				{
		        	 System.out.println("Error in File Multipart");
				}
				//actionForward = updateTripInfo(request, response);
		/*}
		catch (GenericException e) {
			System.out.println("4");
			System.out.println("actionList "+e.getMessage());
			e.printStackTrace();
		}*/
		success = true;
		actionForward = sendResponse(actionForward, success, request);
		return actionForward;
	}

	public String sendResponse(String action, boolean success, HttpServletRequest request) {
		System.out.println("Send Response");
		//String actionForward = "/trip_summary.jsp";
		String actionForward = "/blank.jsp";
		//String actionForward = "";
		if (success)
		{
			//actionForward = "Operation Successfull";
		}
		else
		{
			//actionForward = "Error Occured Please contact Administrator";

		}
		return actionForward;
	}
	
}
