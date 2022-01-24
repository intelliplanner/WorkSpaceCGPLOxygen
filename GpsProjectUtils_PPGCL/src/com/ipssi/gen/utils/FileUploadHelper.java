//Title:        Your Product Name
//Version:
//Copyright:   Copyright (c) 1999
//Author:      Your Name
//Company:     Your Company
//Description:
package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.xpath.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;

//import com.csvreader.*;

public class FileUploadHelper
{
  	private HttpServletRequest m_request = null;
  	private ServletContext m_context = null;
  	private SessionManager m_session = null;
  	private User m_user = null;
  	private Cache m_cache = null;
  	private Logger m_log = null;
  	private Connection dbConn = null;

  	public FileUploadHelper(HttpServletRequest request, ServletContext context)
  	{
     	m_request = request;
     	m_context = context;
     	m_cache = (Cache) context.getAttribute("_cache");
     	m_session = (SessionManager) request.getAttribute("_session");
     	m_user = (User) request.getAttribute("_user");
     	m_log = (Logger) request.getAttribute("_log");
     	dbConn = (Connection) request.getAttribute("_dbConnection");
  	}

  	public MiscInner.PairStr saveUploadedFile(String fPath) throws Exception { //1st of pair is the original filename, 2nd is the name with which saved
		String retStr = "";
    String fileStoredAs = "";
		try {
			boolean isMultipart = FileUpload.isMultipartContent(m_request);
			if (isMultipart) {
				DiskFileItemFactory fileFactory = new DiskFileItemFactory();
				ServletFileUpload fileUpload = new ServletFileUpload(fileFactory);
				List fileItems = fileUpload.parseRequest(m_request);

				//Get an iterator
				Iterator iter = fileItems.iterator();
				while (iter.hasNext()) {
					FileItem item = (FileItem) iter.next();
					if (!item.isFormField()) {
						// get the size, we might want to restrict the size of file being uploaded
						int size = (int) item.getSize();
						// TODO - get the max size allowed from some static variable or from some parameter in xml
						if (size <= Misc.G_UPLOAD_FILE_LIMIT) {
							String name = item.getName();
							//Will require file extension if creating the new file name dynamically
							String fileExtn = getFileExtn(name);
							String fileName = getFileName(name);
							System.out.println("FileUploadHelper#### file name is " + fileName);
							String newName = "xxyyzz" + fileExtn;
							//long fileId = com.ipssi.gen.utils.Misc.getNextId(_dbConnection, com.ipssi.gen.utils.Sequence.FILES);
   							//int bufSize = 1024*32;
   							//FileOutputStream fout = new FileOutputStream(com.ipssi.gen.utils.Misc.getUserFilesSavePath()+System.getProperty("file.separator")+"mpp_model"+System.getProperty("file.separator")+Long.toString(fileId)+".mpp");
   							String filePath = com.ipssi.gen.utils.Misc.getUserFilesSavePath() +
   											System.getProperty("file.separator") + "business_plans" +
   											System.getProperty("file.separator");
   						//	filePath = application.getRealPath("/") + "user_files/business_plans/";
              filePath = fPath; //rajeev 021608
							File uploadedFile = new File(filePath + fileName);
							item.write(uploadedFile);
							System.out.println("FileUploadHelper#### file path at server is " + filePath + fileName);
							//retStr = filePath + fileName; rajeev to_make consistent with definition used in other function;
              //         also to accomodate new signature
              retStr = fileName;
              fileStoredAs = fileName;
							//retStr = "/intelliplanner/data/user_files/business_plans/" + fileName;
						}
						else {
							System.out.println("FileUploadHelper#### " + "File size is too large");
							retStr = "";
						}
					}
				}
			}
			else {
				System.out.println("FileUploadHelper#### " + "Request is not of multipart format");
				retStr = "";
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return new MiscInner.PairStr(retStr, fileStoredAs);
	}

	public MiscInner.PairStr saveUploadedFile(String fPath, long fileId) throws Exception {//1st of pair is the original filename, 2nd is the name with which saved
		String retStr = "";
    String fileStoredAs = "";
		try {
			boolean isMultipart = FileUpload.isMultipartContent(m_request);
			if (isMultipart) {
				DiskFileItemFactory fileFactory = new DiskFileItemFactory();
				ServletFileUpload fileUpload = new ServletFileUpload(fileFactory);
				List fileItems = fileUpload.parseRequest(m_request);

				//Get an iterator
				Iterator iter = fileItems.iterator();
				while (iter.hasNext()) {
					FileItem item = (FileItem) iter.next();
					if (!item.isFormField()) {
						// get the size, we might want to restrict the size of file being uploaded
						int size = (int) item.getSize();
						// TODO - get the max size allowed from some static variable or from some parameter in xml
						if (size <= Misc.G_UPLOAD_FILE_LIMIT) {
							String name = item.getName();
							//Will require file extension if creating the new file name dynamically
							String fileExtn = getFileExtn(name);
							String fileName = getFileName(name);
							String newName = Long.toString(fileId) + fileExtn;

							// Store the file reference in the db
							CallableStatement cStmt = dbConn.prepareCall(com.ipssi.gen.utils.Queries.CREATE_FILE_NAME_MOD);
							cStmt.setLong(1, fileId);
							cStmt.setString(2, null); //TODO - set the one for MS-Excel
							cStmt.setString(3, newName);
							cStmt.setString(4, fileExtn);
							cStmt.setString(5, fileName);
							cStmt.execute();
							cStmt.close();

							//long fileId = com.ipssi.gen.utils.Misc.getNextId(_dbConnection, com.ipssi.gen.utils.Sequence.FILES);
							//int bufSize = 1024*32;
							//FileOutputStream fout = new FileOutputStream(com.ipssi.gen.utils.Misc.getUserFilesSavePath()+System.getProperty("file.separator")+"mpp_model"+System.getProperty("file.separator")+Long.toString(fileId)+".mpp");
							String filePath = com.ipssi.gen.utils.Misc.getUserFilesSavePath() +
											System.getProperty("file.separator") + "business_plans" +
											System.getProperty("file.separator");
							filePath = fPath;
							File uploadedFile = new File(filePath + newName);
							item.write(uploadedFile);
							retStr = fileName;
              fileStoredAs = newName;
						}
						else {
							System.out.println("FileUploadHelper:: " + "File size is too large");
							retStr = "";
						}
					}
				}
			}
			else {
				System.out.println("FileUploadHelper:: " + "Request is not of multipart format");
				retStr = "";
			}
		  return new MiscInner.PairStr(retStr,fileStoredAs);      
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
  //MERGED from sameer 112707 ... to check where is this function being called
  public MiscInner.PairStr saveUploadedFile(FileItem fItem, String fPath, long fileId, int fileSizeLimit) throws Exception {//1st of pair is the original filename, 2nd is the name with which saved
		String retStr = "";
    String fileStoredAs = "";
		try {
			// get the size, we might want to restrict the size of file being uploaded
			int size = (int) fItem.getSize();
			// TODO - get the max size allowed from some static variable or from some parameter in xml
			if (size <= fileSizeLimit) {
				String name = fItem.getName();
				//Will require file extension if creating the new file name dynamically
				String fileExtn = getFileExtn(name);
				String fileName = getFileName(name);
				String newName = Long.toString(fileId) + fileExtn;

				// Store the file reference in the db
				CallableStatement cStmt = dbConn.prepareCall(com.ipssi.gen.utils.Queries.CREATE_FILE_NAME_MOD);
				cStmt.setLong(1, fileId);
				cStmt.setString(2, null); //TODO - set the one for MS-Excel
				cStmt.setString(3, newName);
				cStmt.setString(4, fileExtn);
				cStmt.setString(5, fileName);
				cStmt.execute();
				cStmt.close();

				//long fileId = com.ipssi.gen.utils.Misc.getNextId(_dbConnection, com.ipssi.gen.utils.Sequence.FILES);
				//int bufSize = 1024*32;
				//FileOutputStream fout = new FileOutputStream(com.ipssi.gen.utils.Misc.getUserFilesSavePath()+System.getProperty("file.separator")+"mpp_model"+System.getProperty("file.separator")+Long.toString(fileId)+".mpp");
				String filePath = com.ipssi.gen.utils.Misc.getUserFilesSavePath() +
								System.getProperty("file.separator") + "business_plans" +
								System.getProperty("file.separator");
				filePath = fPath;
				File uploadedFile = new File(filePath + newName);
				fItem.write(uploadedFile);
				retStr = fileName;
        fileStoredAs = newName;
        
			}
			else {
				System.out.println("FileUploadHelper:: " + "File size is too large");
				retStr = "";
			}
      return new MiscInner.PairStr(retStr, fileStoredAs);
					
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw ex;
		}		
	}
	// Method to get the file extension
	public String getFileExtn(String filePath) {
		int dotIndex = 0;
		dotIndex = filePath.lastIndexOf('.');

		if (dotIndex == -1) {
			return "";
		}

		int slashIndex = 0;
		slashIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

		if (slashIndex == -1) {
			return filePath.substring(dotIndex);
		}

		if (dotIndex < slashIndex) {
			return "";
		}
		return filePath.substring(dotIndex);
	}

	public String getFileName(String filePath) {
		int slashIndex = 0;
		slashIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
		if (slashIndex == -1) {
			return "";
		}
		return filePath.substring(slashIndex + 1);
	}

	// Parse and load an uploaded file, the parameter dataType is an indicator of the type
	// of data being uploaded. 1 = project data, 2 = organization data, 3 = currency data
	// currencyType will only be used for currency data upload, mapping is as follows,
	// budget currency = 0, current currency = 1 authorized currency = 2
	// startDate and endDate will only be present for project data
	

	

	
}
