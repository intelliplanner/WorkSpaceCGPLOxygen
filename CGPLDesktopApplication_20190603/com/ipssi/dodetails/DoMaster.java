package com.ipssi.dodetails;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.csvreader.CsvReader;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.TimePeriodHelper;
import com.ipssi.gen.utils.Triple;
import com.ipssi.rfid.constant.Type;
import com.ipssi.tracker.common.util.TripProcessorGateway;
import com.ipssi.trip.challan.ChallanInformation;
import com.ipssi.trip.challan.ChallanUtils;

public class DoMaster {
	public static SessionManager m_session = null;

	
	private static HashMap<String, Integer> seclDOReleaseTypeMap = new HashMap<String, Integer>();
    private static HashMap<String, Integer> seclDOPriorityTypeMap = new HashMap<String, Integer>();
    private static HashMap<String, String> seclGradeCodeMasterMap = new HashMap<String, String>();
    public  static HashMap<Integer, String> seclMinesDetailsMap = new HashMap<Integer, String>();
    
    private static final String ISSUE_NO_EXIST= "Select id from mines_do_details where ";
    private static final String DO_NO_EXIST= "Select id from mines_do_details where do_number = ?";
    private static final String FETCH_SAP_ID = "Select id from customer_details where sn = ?";
    private static final String FETCH_SOURCE_CODE = "Select sn from mines_details where unit_code = ?";
    private static final String FETCH_GRADE_SN = "Select sn from grade_details where ";
    private static final String FETCH_COAL_SIZE = "Select sn from coal_size where name like ?";
    
    static{
          seclGradeCodeMasterMap.put("G-1", "G1");
          seclGradeCodeMasterMap.put("G-2", "G2");
          seclGradeCodeMasterMap.put("G-3", "G3");
          seclGradeCodeMasterMap.put("G-4", "G4");
          seclGradeCodeMasterMap.put("G-5", "G5");
          seclGradeCodeMasterMap.put("G-6", "G6");
          seclGradeCodeMasterMap.put("G-7", "G7");
          seclGradeCodeMasterMap.put("G-8", "G8");
          seclGradeCodeMasterMap.put("G-9", "G9");
          seclGradeCodeMasterMap.put("G-10", "G10");
          seclGradeCodeMasterMap.put("G-11", "G11");
          seclGradeCodeMasterMap.put("G-12", "G12");
          seclGradeCodeMasterMap.put("G-13", "G13");
          seclGradeCodeMasterMap.put("G-14", "G14");
          seclGradeCodeMasterMap.put("G-15", "G15");
          seclGradeCodeMasterMap.put("G1", "G1");
          seclGradeCodeMasterMap.put("G2", "G2");
          seclGradeCodeMasterMap.put("G3", "G3");
          seclGradeCodeMasterMap.put("G4", "G4");
          seclGradeCodeMasterMap.put("G5", "G5");
          seclGradeCodeMasterMap.put("G6", "G6");
          seclGradeCodeMasterMap.put("G7", "G7");
          seclGradeCodeMasterMap.put("G8", "G8");
          seclGradeCodeMasterMap.put("G9", "G9");
          seclGradeCodeMasterMap.put("G10", "G10");
          seclGradeCodeMasterMap.put("G11", "G11");
          seclGradeCodeMasterMap.put("G12", "G12");
          seclGradeCodeMasterMap.put("G13", "G13");
          seclGradeCodeMasterMap.put("G14", "G14");
          seclGradeCodeMasterMap.put("G15", "G15");
          
          seclGradeCodeMasterMap.put("ROM", "ROM");
          
          seclDOPriorityTypeMap.put("POWER", 1);
          seclDOPriorityTypeMap.put("CPP (CAPTIVE POWER PLANT)", 2);
          seclDOPriorityTypeMap.put("CAPTIVE POWER PLANT", 2);
          seclDOPriorityTypeMap.put("RRM (RE ROLLING MILLS)", 3);
          seclDOPriorityTypeMap.put("RE ROLLING MILLS", 3);
          seclDOPriorityTypeMap.put("RE-ROLLING MILLS", 3);
          seclDOPriorityTypeMap.put("CEMENT", 4);
          seclDOPriorityTypeMap.put("SPONGE", 5);
          seclDOPriorityTypeMap.put("PAPER", 6);
          seclDOPriorityTypeMap.put("BRICK", 7);
          seclDOPriorityTypeMap.put("STATE AGENCY", 8);
          seclDOPriorityTypeMap.put("ALUMINIUM", 9);
          seclDOPriorityTypeMap.put("AS IS WHERE IS BASIS", 10);
          seclDOPriorityTypeMap.put("LINKAGE AUCTION", 11);
          seclDOPriorityTypeMap.put("E-AUCTION", 12);
          seclDOPriorityTypeMap.put("EAUCTION", 12);
          seclDOPriorityTypeMap.put("E- AUCTION", 12);
          seclDOPriorityTypeMap.put("F EAUCTION", 13);
          seclDOPriorityTypeMap.put("FEAUCTION", 13);
          seclDOPriorityTypeMap.put("F E-AUCTION", 13);
          seclDOPriorityTypeMap.put("SF- AUCTION", 14);
          seclDOPriorityTypeMap.put("S F-AUCTION", 14);
          seclDOPriorityTypeMap.put("SFAUCTION", 14);
          seclDOPriorityTypeMap.put("SF-AUCTION", 14);
          seclDOPriorityTypeMap.put("X- AUCTION", 15);
          seclDOPriorityTypeMap.put("X-AUCTION", 15);
          seclDOPriorityTypeMap.put("XAUCTION", 15);
          seclDOPriorityTypeMap.put("EXAUCTION", 16);
          seclDOPriorityTypeMap.put("EX-AUCTION", 16);
          seclDOPriorityTypeMap.put("EX AUCTION", 16);
          
          seclDOPriorityTypeMap.put("OTHERS", 17);
          seclDOPriorityTypeMap.put("OTHER", 17);
          
          seclDOReleaseTypeMap.put("POWER", 1);
          seclDOReleaseTypeMap.put("NON POWER", 2);
          seclDOReleaseTypeMap.put("FUEL SUPPLY AGREEMENT", 2);
          seclDOReleaseTypeMap.put("AIWI", 3);
          seclDOReleaseTypeMap.put("LINKAGE AUCTION", 4);
          seclDOReleaseTypeMap.put("FSA THROUGH LINKAGE AUCTION", 4);
          seclDOReleaseTypeMap.put("EAUCTION", 5);
          seclDOReleaseTypeMap.put("E- AUCTION", 5);
          seclDOReleaseTypeMap.put("FEAUCTION", 6);
          seclDOReleaseTypeMap.put("F EAUCTION", 6);
          seclDOReleaseTypeMap.put("FE-AUCTION", 6);
          seclDOReleaseTypeMap.put("F E-AUCTION", 6);
          seclDOReleaseTypeMap.put("S F-AUCTION", 7);
          seclDOReleaseTypeMap.put("SFAUCTION", 7);
          seclDOReleaseTypeMap.put("SF-AUCTION", 7);
          seclDOReleaseTypeMap.put("X- AUCTION", 8);
          seclDOReleaseTypeMap.put("X-AUCTION", 8);
          seclDOReleaseTypeMap.put("X AUCTION", 8);
          seclDOReleaseTypeMap.put("XAUCTION", 8);
    }

	
	public DoMaster(SessionManager m_session) {
		this.m_session = m_session;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	public static Triple<Integer, Integer, StringBuilder>  processContentCSV(Connection conn, CsvReader data, int portNodeId, DoDefinitionBean challanDef, HashMap<Integer, DoParamBean> challanParamList, ArrayList<DimInfo.ValInfo> valList) throws Exception {
		PreparedStatement psInsert = null;
		PreparedStatement psUpdate = null;
		PreparedStatement psSelect = null;
		PreparedStatement psUpdPrior = null;
		String query = null;
		int counter = 0;
		int rowCount = 0;
		StringBuilder error = new StringBuilder();
		int processCount = 0;
		int errorCount = 0;
		try {
			if(data != null && challanDef.getCsvDelemeter() != null && challanDef.getCsvDelemeter().length() > 0)
				data.setDelimiter(challanDef.getCsvDelemeter().charAt(0));
			int pos = Misc.getUndefInt();
			if(valList != null )//&& challanParamList != null)
				query = DoInformation.getDynamicInsertQuery(valList);	
			System.out.println("[Challan Master]-INSERT-"+query);
			psInsert = conn.prepareStatement(query);
			String updateQuery = DoInformation.getDynamicUpdateQuery(valList, challanParamList);
			System.out.println("[Challan Master]-UPDATE-"+updateQuery);
			psUpdate = conn.prepareStatement(updateQuery);
			psSelect = conn.prepareStatement("select id from challan_details where vehicle_id=? and challan_date=?");
			psUpdPrior = conn.prepareStatement("update challan_details set trip_status=2 where vehicle_id=? and challan_date < ? and (trip_status is null or trip_status=1)");
			ArrayList<String> row = new ArrayList<String>();
			HashMap<Integer, Integer> vehicleUpdated = new HashMap<Integer, Integer>();
			while (readRowFromCSV(data, row)) {
				try {
					if (isEmptyRow(row))
						continue;
					rowCount++;
					if (rowCount <= challanDef.getStartRow())
						continue;
					if (data.getColumnCount() < 2)
						continue;
//					int vehicleId = processSingleRow(conn, row, rowCount, challanDef, challanParamList, valList, portNodeId, psInsert, psUpdate, psSelect, psUpdPrior, error); 
//					if (!Misc.isUndef(vehicleId)) {
//						vehicleUpdated.put(vehicleId, vehicleId);
//						processCount++;
//					}
//					else { 
//						errorCount++;
//					}
				}
				catch (Exception e) {
					e.printStackTrace();
					//eat it
				}
			}
			if (processCount >0) {
				psInsert.executeBatch();
				psUpdate.executeBatch();
				psUpdPrior.executeBatch();
			}
			
			/*if (!conn.getAutoCommit())*/
				conn.commit();
			sendUpdMsg(vehicleUpdated);
		} 
		catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} 
		finally {
			try {
				if (psInsert != null) {
					psInsert.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (psUpdate != null) {
					psUpdate.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (psSelect != null) {
					psSelect.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (psUpdPrior != null)
					psUpdPrior.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		error.append("<br/>").append(processCount).append(" Rows successfully added<br/>").append(errorCount).append(" Rows had error");
		return new Triple<Integer, Integer, StringBuilder>(processCount, errorCount, error) ;
	}
	
	public static Triple<Integer, Integer, StringBuilder> processContentXLS2003(Connection conn, InputStream data, int portNodeId, DoDefinitionBean doDef, HashMap<Integer, DoParamBean> doParamList, ArrayList<DimInfo.ValInfo> valList,int userId) throws Exception {
		PreparedStatement psInsert = null;
		//	PreparedStatement psInsertApprvd = null;
//			PreparedStatement psUpdateApprvd = null;	
			//String queryApprvd = null;
			//String queryUpdateApprvdByReleaseNo = null;
//			String queryCustomerDetailsApprvd = null;
			//String queryUpdateApprvdByDoNumber = null;
			PreparedStatement psUpdateDoNumber = null;
			PreparedStatement psUpdateReleaseNo = null;
			String query = null;
			String queryUpdateByReleaseNo = null;
			String queryUpdateByDoNumber = null; 
			String queryCustomerDetails = null;
			String queryUpdateCustomerDetails = null;
			StringBuilder error = new StringBuilder();
			int rowCount = 0;
			int processCount = 0;
			int errorCount = 0;
			int updateCount = 0;
			int insertCount = 0;

		try {
			if(valList != null && doParamList != null)
				query = ChallanInformation.getDynamicInsertQuery(valList);	

			query =  DoInformation.getDynamicInsertQuery(valList,"mines_do_details",portNodeId);
			//queryApprvd = DoInformation.getDynamicInsertQuery(valList,"mines_do_details_apprvd",portNodeId);
			
			queryUpdateByDoNumber =  DoInformation.getDynamicUpdateQuery(valList,"mines_do_details","do_number");
			//queryUpdateApprvdByDoNumber =  DoInformation.getDynamicUpdateQuery(valList,"mines_do_details_apprvd","do_number");
			

			queryUpdateByReleaseNo =  DoInformation.getDynamicUpdateQueryReleaseType(valList,"mines_do_details","do_release_no");
			//queryUpdateApprvdByReleaseNo =  DoInformation.getDynamicUpdateQuery(valList,"mines_do_details_apprvd","do_release_no");
			
			queryCustomerDetails = DoInformation.getDynamicInsertQueryForCustomerDetails(valList,"customer_details",portNodeId);
			//queryCustomerDetailsApprvd = DoInformation.getDynamicInsertQueryForCustomerDetails(valList,"customer_details_apprvd",portNodeId);
			queryUpdateCustomerDetails  = DoInformation.getDynamicUpdateQueryForCustomer(valList,"customer_details","sn");
			System.out.println("[Query: "+query+"]");
			//System.out.println("[Query: "+queryApprvd+"]");
			
			System.out.println("[Query: "+queryUpdateByReleaseNo+"]");
			//System.out.println("[Query: "+queryUpdateApprvdByReleaseNo+"]");
			
			System.out.println("[Query: "+queryUpdateByDoNumber+"]");
			//System.out.println("[Query: "+queryUpdateApprvdByDoNumber+"]");
			
			psInsert = conn.prepareStatement(query);
			//psInsertApprvd = conn.prepareStatement(queryApprvd);
			psUpdateDoNumber = conn.prepareStatement(queryUpdateByDoNumber);
			psUpdateReleaseNo = conn.prepareStatement(queryUpdateByReleaseNo);
			
	//		XSSFWorkbook wb1 = new XSSFWorkbook(data);
		//	XSSFSheet sheet1 = wb1.getSheetAt(0);
			
			HSSFWorkbook wb = new HSSFWorkbook(data);
			HSSFSheet sheet = wb.getSheetAt(0);
			int excelRows = sheet.getPhysicalNumberOfRows();
			ArrayList<String> row = new ArrayList<String>();
			HashMap<Integer, Integer> vehicleUpdated = new HashMap<Integer, Integer>();
			for(int i = 0 ; i <= excelRows; i++) {
				try {

					int columnCount = 1;
					HSSFRow excelRow = sheet.getRow(i);
					if (excelRow == null)
						continue;
					row.clear();
					boolean seenText = false;
					for (int j = 0, js = doDef.getTotalColumn(); j < js; j++) {
						HSSFCell cell = excelRow.getCell(j);
						if(cell != null)
							cell.setCellType(Cell.CELL_TYPE_STRING);
						String cellStr = cell == null ? null : cell.getStringCellValue();
						
						if (cellStr != null)
							cellStr = cellStr.replace("\r<br/>", "").trim();

						if (seenText || cell != null) {
							/*if(cell.getCellType() == cell.CELL_TYPE_NUMERIC)
								cellStr = cell.getNumericCellValue() +"";*/
							row.add(cellStr);
							seenText = true;
						}
					}
					if (isEmptyRow(row))
						continue;
					rowCount++;
					if (rowCount <= doDef.getStartRow())
						continue;
					rowCount++;
					if (rowCount <= doDef.getStartRow())
						continue;
				
					 processCount = processSingleRow(conn, row, i+1, doDef, doParamList, valList, portNodeId, psInsert,psUpdateDoNumber,psUpdateReleaseNo,queryCustomerDetails,queryUpdateCustomerDetails, error,userId,false);
					if(processCount  == 1)
						insertCount++;
					else if(processCount  == 2)
						updateCount++;
					 
				}
				catch (Exception e) {
					e.printStackTrace();
					errorCount++;
					//error.append("<br/> At Row").append(row);
					//eat it
				}
			}//each row
			System.out.println("[Query: "+ psInsert.toString()+"]");
			
		if (!conn.getAutoCommit())
			conn.commit();
	} 
	catch (SQLException e) {
		e.printStackTrace();
		throw e;
	} 
	finally {
		try {
			if (psInsert != null) {
				psInsert.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (psUpdateDoNumber != null) {
				psUpdateDoNumber.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (psUpdateReleaseNo != null) {
				psUpdateReleaseNo.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	error.append("<br/>").append(insertCount).append(" Rows successfully added, ").append(updateCount).append(" Rows successfully updated, ").append(errorCount).append(" Rows had error");
	
	return new Triple<Integer, Integer, StringBuilder>(processCount, errorCount, error) ;
	
		}

	public static Triple<Integer, Integer, StringBuilder> processContentXLS2007(Connection conn, InputStream data, int portNodeId, DoDefinitionBean doDef, HashMap<Integer, DoParamBean> doParamList, ArrayList<DimInfo.ValInfo> valList, int userId,boolean doUpdateDo) throws Exception {
		PreparedStatement psInsert = null;
	//	PreparedStatement psInsertApprvd = null;
//		PreparedStatement psUpdateApprvd = null;	
		//String queryApprvd = null;
		//String queryUpdateApprvdByReleaseNo = null;
//		String queryCustomerDetailsApprvd = null;
		//String queryUpdateApprvdByDoNumber = null;
		PreparedStatement psUpdateDoNumber = null;
		PreparedStatement psUpdateReleaseNo = null;
		String query = null;
		String queryUpdateByReleaseNo = null;
		String queryUpdateByDoNumber = null; 
		String queryCustomerDetails = null;
		String queryUpdateCustomerDetails =null;
		StringBuilder error = new StringBuilder();
		int rowCount = 0;
		int processCount = 0;
		int errorCount = 0;
		int updateCount = 0;
		int existCount = 0;
		
		int insertCount = 0;
		try {
			if(valList == null || doParamList == null)
					return null;
			
			query =  DoInformation.getDynamicInsertQuery(valList,"mines_do_details",portNodeId);
			queryUpdateByDoNumber =  DoInformation.getDynamicUpdateQuery(valList,"mines_do_details","do_number");
			queryUpdateByReleaseNo =  DoInformation.getDynamicUpdateQueryReleaseType(valList,"mines_do_details","do_release_no");
			queryCustomerDetails = DoInformation.getDynamicInsertQueryForCustomerDetails(valList,"customer_details",portNodeId);
			queryUpdateCustomerDetails  = DoInformation.getDynamicUpdateQueryForCustomer(valList,"customer_details","sn");
			System.out.println("[Query: "+query+"]");
			System.out.println("[Query: "+queryUpdateByReleaseNo+"]");
			System.out.println("[Query: "+queryUpdateByDoNumber+"]");
			
			psInsert = conn.prepareStatement(query);
			psUpdateDoNumber = conn.prepareStatement(queryUpdateByDoNumber);
			psUpdateReleaseNo = conn.prepareStatement(queryUpdateByReleaseNo);
			
			
			XSSFWorkbook wb = new XSSFWorkbook(data);
			XSSFSheet sheet = wb.getSheetAt(0);
			int excelRows = sheet.getPhysicalNumberOfRows();
			ArrayList<String> row = new ArrayList<String>();
//			HashMap<Integer, Integer> vehicleUpdated = new HashMap<Integer, Integer>();
			for(int i = 0 ; i <= excelRows; i++) {
				try {
//					int columnCount = 1;
					XSSFRow excelRow = sheet.getRow(i);
					if (excelRow == null)
						continue;
					row.clear();
					boolean seenText = false;
					for (int j = 0, js = doDef.getTotalColumn(); j <= js; j++) {
						XSSFCell cell = excelRow.getCell(j);
						if(cell != null)
							cell.setCellType(Cell.CELL_TYPE_STRING);
						String cellStr = cell == null ? null : cell.getStringCellValue();
						if (cellStr != null)
							cellStr = cellStr.replace("\r<br/>", "").trim();
						if (seenText || cell != null) {
							/*if(cell.getCellType() == cell.CELL_TYPE_NUMERIC)
								cellStr = cell.getNumericCellValue() + "";*/
							row.add(cellStr);
							seenText = true;
						}
					}
					if (isEmptyRow(row))
						continue;
					rowCount++;
					if (rowCount <= doDef.getStartRow())
						continue;
				
					processCount = processSingleRow(conn, row, i+1, doDef, doParamList, valList, portNodeId, psInsert,psUpdateDoNumber,psUpdateReleaseNo,queryCustomerDetails, queryUpdateCustomerDetails, error,userId, doUpdateDo);
					if(processCount  == 1)
						insertCount++;
					else if(processCount  == 2)
						updateCount++;
					else if(processCount  == 3)
						 existCount++;
					else
						errorCount++;
				}
				catch (Exception e) {
					e.printStackTrace();
					errorCount++;
					//eat it
				}
			}//each row
			//				System.out.println("[Query: "+ psInsert.toString()+"]");
			//				totalRowInsert = psInsert.executeBatch();
			//				totalRowUpdatedByRelease= psUpdateReleaseNo.executeBatch();
			//				totalRowUpdatedByDo =  psUpdateDoNumber.executeBatch();
			//totalRow = psInsertApprvd.executeBatch();

			if (!conn.getAutoCommit())
				conn.commit();
		} 
		catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} 
		finally {
			try {
				if (psInsert != null) {
					psInsert.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (psUpdateDoNumber != null) {
					psUpdateDoNumber.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (psUpdateReleaseNo != null) {
					psUpdateReleaseNo.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//error.append("<br/>").append(insertCount).append(" Rows successfully added<br/>");//.append(errorCount).append(" Rows had error");
		error.append("<br/>").append(insertCount).append(" Rows successfully added, ").append(updateCount).append(" Rows updated, ").append(errorCount).append(" Rows had error");
		
		return new Triple<Integer, Integer, StringBuilder>(processCount, errorCount, error) ;
	}

	private static int processSingleRow(Connection conn, ArrayList<String> data, int rowCount, DoDefinitionBean doDef, HashMap<Integer, DoParamBean> doParamList, ArrayList<DimInfo.ValInfo> valList, int portNodeId,PreparedStatement psInsert,PreparedStatement psUpdateDoNumber,PreparedStatement psUpdateReleaseNo,String queryCustomerDetails,String queryUpdateCustomerDetails,StringBuilder error,int userId,boolean doUpdate) {
		int count = 0;
		int rowInsert = 1;
		int rowUpdate= 2;
		int rowExist= 3;
		int pos = 1;
//		String sapCode = "";
		String sourceCode = "";
		//int priorityCode = Misc.getUndefInt();
//		String mines_source_code = "";
	//	int posInUpd = 1;
	
		//int DOPriorityTypeLovId = 11;
	//	int doReleaseTypeId = 14;
//		int doGradeType = 13;
		//int cgstLovId = 36;
		//int sgstLovId  = 37;
		//int igstLovId = 38;
		//int deliveryPointLovId = 44;
		//int typeOfConsumer = 49;
		//int validityDateLovId = 54;
		//int stowEdLovId = 55;
//		int sourceCodeLovId = 56;
		
		int doDateLovId = 3;
		
		Date validityDate = null; 
		
		int posInUpd = 1;
		int doId  = Misc.getUndefInt();
		boolean isIssueNumberExist = false;
		boolean isDoExist  = false;
		String fetchIssueNo;
		int lessRow = doDef.getStartRow();
		try {
			if (rowCount < doDef.getStartRow())
				return Misc.getUndefInt();
			if (data.size() < 2)
				return Misc.getUndefInt();

			String str[] = data.get(3).split("/"); // DO No
			String unitCode = str.length == 3 ? str[1] : "";

			fetchIssueNo = ISSUE_NO_EXIST + "do_release_no = '" +data.get(1)+ "' or do_release_no = '_"+data.get(1)+"' or do_release_no = '"+ data.get(1)+"_'";
			doId =  DoDefinitionDao.isExist(conn, data.get(1), fetchIssueNo,false);
			isIssueNumberExist = Misc.isUndef(doId) ? false  : true;

			doId = DoDefinitionDao.isExist(conn, data.get(3), DO_NO_EXIST,true);
			isDoExist = Misc.isUndef(doId) ? false  : true;
			//if(isIssueNoExit || isDoExit)
			//	return Misc.getUndefInt();

			if((isIssueNumberExist || isDoExist)  && !doUpdate){
				error.append("<br/>Row ").append(rowCount-lessRow).append(" already exist");
				return rowExist;
			}

			sourceCode = DoDefinitionDao.getSapCode(conn, unitCode, FETCH_SOURCE_CODE);
			boolean isSourceCodeExit = sourceCode != null && sourceCode.length() > 0 ? true : false;
			//sourceCode = isSourceCodeExit ?  : "" ;
			if(!isSourceCodeExit){
				//error.append("<br/>SourceCode does Not Exist "+data.get(5)).append(rowCount);
				error.append("<br/>Error at row ").append(rowCount-lessRow).append(" Unit Code does Not exist ").append(unitCode);
				return Misc.getUndefInt();
			}

			boolean isGradeExist = seclGradeCodeMasterMap.containsKey(data.get(16).toUpperCase()); // Grade Code
			if(!isGradeExist){
				//error.append("<br/>GradeCode "+data.get(18)).append(rowCount).append(" does Not Exist");
				error.append("<br/>Error at row ").append(rowCount-lessRow).append(" GradeCode does Not exist ").append(data.get(16));
				return Misc.getUndefInt();
			}

			boolean isDOPriorityTypeMapExist = seclDOPriorityTypeMap.containsKey(data.get(10).toUpperCase());// Priority Desc
			if(!isDOPriorityTypeMapExist){
				error.append("<br/>Error at row ").append(rowCount-lessRow).append("  DOPriorityType does Not exist ").append(data.get(10));
				return Misc.getUndefInt();
			}
			boolean isDOReleaseTypeMapExist = seclDOReleaseTypeMap.containsKey(data.get(13).toUpperCase());// Release Type
			if(!isDOReleaseTypeMapExist){
				error.append("<br/>Error at row ").append(rowCount-lessRow).append(" DOReleaseType does Not exist ").append(data.get(13));
				return Misc.getUndefInt();
			}
			// ??confirm with Rahul wheather party_code mapped to sn or sap_code in mines_details

			int sapCodeId =  DoDefinitionDao.getSapId(conn, data.get(6), FETCH_SAP_ID);//DoDefinitionDao.isExist(conn, data.get(6), FETCH_SAP_CODE); // Customer code
			if(!Misc.isUndef(sapCodeId)){  
				DoDefinitionDao.insertUpdateSapCode(conn,data,portNodeId,queryUpdateCustomerDetails,true);// true for update and false for insert
			}else{
				sapCodeId = DoDefinitionDao.insertUpdateSapCode(conn,data,portNodeId,queryCustomerDetails,false);
			}
			//sapCodeId  = Misc.isUndef(sapCodeId) ? : sapCodeId  ;

			for(DimInfo.ValInfo val : valList){
				String fieldVal = null;
				int contentType = Cache.STRING_TYPE;
				String secValStr = null;
				int secColDatatype = Cache.STRING_TYPE;
//				DoParamBean paramBean = doParamList.get(val.m_id);
				if(!val.getOtherProperty("base_table").equalsIgnoreCase("mines_do_details") || val.m_name == "") 
					continue;
				contentType = Misc.getParamAsInt(val.getOtherProperty("col_data_type"));

				// secValStr = data.get(val.m_id -1 );
				if(val.m_id <= doDef.getTotalColumn())
					fieldVal = data.get(val.m_id -1 );
				/*if(paramBean != null && !Misc.isUndef(paramBean.getSecColDataType()) && !Misc.isUndef(paramBean.getSecColDataType())){
					fieldVal = data.get(paramBean.getParamPos()-1);
					secValStr = data.get(paramBean.getSecColPos()-1);
					secColDatatype = paramBean.getSecColDataType();
				}
				secColDatatype = Misc.getParamAsInt(val.getOtherProperty("xls_data_type"));*/

				/*if(paramBean == null){
					fieldVal = null;
					contentType = Misc.getParamAsInt(val.m_sn,Cache.STRING_TYPE); 
				}
				else{
					fieldVal = data.get(paramBean.getParamPos()-1);
					contentType = Misc.getParamAsInt(val.getOtherProperty("col_data_type")); // 
					contentType = Misc.isUndef(contentType) ? paramBean.getDataType() : contentType;

					if(!Misc.isUndef(paramBean.getSecColDataType()) && !Misc.isUndef(paramBean.getSecColDataType())){
						secValStr = data.get(paramBean.getSecColPos()-1);
						secColDatatype = paramBean.getSecColDataType();
					}
				}*/
				//hack 
				Object retval = null;
				if( !Misc.isUndef(sapCodeId) && val.getOtherProperty("base_table").equalsIgnoreCase("mines_do_details") && val.m_name != "" ){
					if(val.m_name.equalsIgnoreCase("release_priority")){//do 
						//	int dataVal =  == seclDOPriorityTypeMap.get(data.get(10)).toUpperCase()) ;
						fieldVal = seclDOPriorityTypeMap.get(data.get(10).toUpperCase()) == null ? "" : Integer.toString(seclDOPriorityTypeMap.get(data.get(10).toUpperCase()));
					}
					else if(val.m_name.equalsIgnoreCase("type_of_release")){ // doReleaseType
						fieldVal = seclDOReleaseTypeMap.get(data.get(13).toUpperCase()) == null ?  ""  : Integer.toString(seclDOReleaseTypeMap.get(data.get(13).toUpperCase()));
					}
					else if(val.m_name.equalsIgnoreCase("do_date")){ // do_date
						long doDate = data.get(val.m_id - 1) != null ?  Math.min(Long.parseLong(data.get(val.m_id - 1)) , System.currentTimeMillis()) : System.currentTimeMillis() ;
						fieldVal = Long.toString(doDate);
					} 
						
					else if(val.m_name.equalsIgnoreCase("validity_date") && validityDate != null){  // validity_date
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						fieldVal = sdf.format(validityDate);
					}

					else if(val.m_name.equalsIgnoreCase("cgst_rate")){
						fieldVal =  Misc.getParamAsDouble(data.get(val.m_id-1)) > 0 ?  "2.5" : "0" ;
						//contentType = 5;
					}
					else if(val.m_name.equalsIgnoreCase("sgst_rate")){
						fieldVal = Misc.getParamAsDouble(data.get(val.m_id-1))  > 0   ?  "2.5" : "0" ;
						//contentType = 5;
					}
					else if(val.m_name.equalsIgnoreCase("igst_rate")){
						fieldVal = Misc.getParamAsDouble(data.get(val.m_id-1))  > 0 ?  "5.0" :  "0" ;
						//contentType = 5;
					}
					else if(val.m_name.equalsIgnoreCase("source_code") || val.m_name.equalsIgnoreCase("delivery_point")){
						fieldVal = sourceCode;
					}
					else if(val.m_name.equalsIgnoreCase("stow_ed") ){
						fieldVal = "0";
					}
					else if(val.m_name.equalsIgnoreCase("grade_code")){
						//String fetchGrade = FETCH_GRADE_SN + "name like '" +data.get(16)+ "'" + " or sn like '" +data.get(16)+ "'";
						//fieldVal  = DoDefinitionDao.getSapCode(conn, data.get(16),FETCH_GRADE_SN);
						fieldVal  = DoDefinitionDao.getGradeSapCode(conn, data.get(16),FETCH_GRADE_SN);
					}
					else if(val.m_name.equalsIgnoreCase("coal_size")){// need to check why ? while @line 
						String coalSize = "";
						if(data.get(15) !=null || (data.get(15).trim()).length() > 0 ){
							coalSize =  (data.get(15).toUpperCase().replace("SIZED","")).trim() ;
						}
						fieldVal  = DoDefinitionDao.getSapCode(conn, coalSize,FETCH_COAL_SIZE); 
					}
					else if(val.m_name.equalsIgnoreCase("typeOfConsumer")){ // type of consumer
						String releaseTypeStr =  data.get(10) +","+ data.get(13);
						if(releaseTypeStr.contains("POWER"))
							fieldVal  = Integer.toString(Type.MinesDoDetails.TypeOfConsumer.power);
						else if(releaseTypeStr.contains("AUCTION")  && !releaseTypeStr.contains("LINKAGE"))
							fieldVal =  Integer.toString(Type.MinesDoDetails.TypeOfConsumer.eAuction);
						else
							fieldVal = Integer.toString(Type.MinesDoDetails.TypeOfConsumer.nonPower);
					}
					else if(val.m_name.equalsIgnoreCase("other_charges1_post_tax") || val.m_name.equalsIgnoreCase("other_charges2_post_tax") ||
							val.m_name.equalsIgnoreCase("sizing_charge") || val.m_name.equalsIgnoreCase("dump_charge")
							|| val.m_name.equalsIgnoreCase("other_charges") || val.m_name.equalsIgnoreCase("stc_charge") 
							|| val.m_name.equalsIgnoreCase("terminal_charge") || val.m_name.equalsIgnoreCase("sadak_tax")
							|| val.m_name.equalsIgnoreCase("forest_cess") || val.m_name.equalsIgnoreCase("silo_charge")  
							|| val.m_name.equalsIgnoreCase("transport_charge") ){

						if(fieldVal == null  || fieldVal.length() == 0)
							fieldVal = Integer.toString(0);
						
					}
					else if(val.m_name.equalsIgnoreCase("src_record_time")){
						SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						fieldVal = sdf.format(new Date());
					}else if(val.m_name.equalsIgnoreCase("nmet")){
						Double nmetVal = Misc.getParamAsDouble(data.get(val.m_id-1)) / Misc.getParamAsDouble(data.get(21));
						fieldVal = Double.toString(Math.round((nmetVal) * 100)) ;         // 21 lov id for royality
					}
					else if(val.m_name.equalsIgnoreCase("dmf")){
						Double dmfVal = Misc.getParamAsDouble(data.get(val.m_id-1)) / Misc.getParamAsDouble(data.get(21));
						fieldVal = Double.toString(Math.round((dmfVal) * 100)) ; 
					}
					
					

					if(isIssueNumberExist || isDoExist ){						
						if(isIssueNumberExist && val.m_name.equalsIgnoreCase("do_number"))
							continue;

						retval = addParameter(posInUpd, fieldVal, contentType,isIssueNumberExist ? psUpdateReleaseNo : psUpdateDoNumber,secValStr,secColDatatype);
						if(val.m_name.equalsIgnoreCase("source_code")){  // if sourcecodeLovId Get then add update Where Clause value
							if(isIssueNumberExist)	
								psUpdateReleaseNo.setString(posInUpd+1, data.get(1));
							else 
								psUpdateDoNumber.setString(posInUpd+1, data.get(3));
						}
						posInUpd++;
					}else{
						retval = addParameter(pos, fieldVal, contentType, psInsert,secValStr,secColDatatype);
						pos++;
					}

					if(val.m_name.equalsIgnoreCase("do_date")){
						validityDate = (Date)retval;
						TimePeriodHelper.addScopedDur(validityDate, Misc.SCOPE_DAY, 45);
						validityDate = new Date(validityDate.getTime()-1000);
						System.out.println("validityDate "+validityDate );
					}

				}else{
					error.append("<br/>Error at row ").append(rowCount-lessRow);
					continue;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			error.append("<br/>Error at row ").append(rowCount-lessRow).append(" " +e);
			return Misc.getUndefInt();
		} 		
		
		if(isIssueNumberExist){
		    	System.out.println(psUpdateReleaseNo.toString());
				try {
					psUpdateReleaseNo.execute();
					//error.append("<br/> Row ").append(rowCount).append("  successfully updated on basis of Issue Number ").append(data.get(1));
					error.append("<br/>Row ").append(rowCount-lessRow).append(" updated");
					count = rowUpdate; 
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(Misc.isUndef(doId))
						doId =  DoDefinitionDao.isExist(conn, data.get(1), fetchIssueNo,false);
		}else if(isDoExist){
			System.out.println(psUpdateDoNumber.toString());
			try {
				psUpdateDoNumber.execute();
				error.append("<br/>Row ").append(rowCount-lessRow).append(" updated");	
				count = rowUpdate;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				 /*try {
						if (!conn.getAutoCommit())
								conn.commit();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
				}	
				if(Misc.isUndef(doId))
						doId = DoDefinitionDao.isExist(conn, data.get(3), DO_NO_EXIST,true);
		}else{
			System.out.println(psInsert.toString());
			try {
				psInsert.executeUpdate();
				error.append("<br/>Inserted Row ").append(rowCount-lessRow);
				ResultSet rs = psInsert.getGeneratedKeys();
				if (rs.next()){
					doId = rs.getInt(1);
					count = rowInsert;
				}
				Misc.closeRS(rs);

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				 /*try {
					if (!conn.getAutoCommit())
							conn.commit();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}	
		}
		
        // handle workflow 
        // We DO NOT create entry in apprvd
        // Instead we create an entry in workflows if there does not exist – something as follows:
        handleWorkFlow(conn,   doId , data.get(3), userId, portNodeId);
        
        try {
			if (!conn.getAutoCommit())
					conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return  count;
	}

	
	private static void handleWorkFlow(Connection conn, int  doId ,String doNumber,int userId,int portNodeId){
	    PreparedStatement psWorkflowsIns = null;
        PreparedStatement psWorkflowsHistIns = null;
        ResultSet rs = null;
        int workflowId = Misc.getUndefInt();
        String insertWkflows = "insert into workflows(object_id, workflow_type_id, status, created_on, updated_on,created_by,updated_by" +
        ",pending_approval_level,pending_approval_of,port_node_id, object_name,change_notes) (select "+doId+", 51,1,now(),now()," +
        userId+","+userId+",0, approval_role_user.user_id,"+portNodeId+",'"+doNumber+"','Created thru upload'   from approval_role_user " +
        "where port_node_id=3 and approval_role_id=1 and workflow_type_id=51      " +
        "and not exists(select 1 from workflows where workflow_type_id in (51) and status=1 and object_id="+doId+") order by id desc limit 1)";
       
        //get the id generated above and if a row was inserted above then next step needed
        try {
        	psWorkflowsIns = conn.prepareStatement(insertWkflows);
            psWorkflowsIns.executeUpdate();
            rs = psWorkflowsIns.getGeneratedKeys();
            if (rs.next()){
                  workflowId = rs.getInt(1);
            }
            Misc.closeRS(rs);
            String insertWkflowsHist = "insert into workflow_hist(workflow_id, pending_since, pending_cause_by, action_status,submitted_to_approval_role" +
            ",submitted_to,submitted_to_approval_level) (select "+workflowId+", now(), "+userId+",1,1, approval_role_user.user_id,0 " +
            "from approval_role_user where port_node_id=3 and approval_role_id=1 and workflow_type_id=51     )";
            
            if(workflowId > 0){
                  psWorkflowsHistIns = conn.prepareStatement(insertWkflowsHist);
                  psWorkflowsHistIns.executeUpdate();
            }
                        
            Misc.closePS(psWorkflowsIns);
            Misc.closePS(psWorkflowsHistIns);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
	}
	
	
	private static Object addParameter(int pos,String val,int contentType,PreparedStatement ps,String secValStr,int secColDatatype){
		try{
			Object retval = null;

			if(contentType == Cache.INTEGER_TYPE) {
				retval = new Integer((int) Math.round(Misc.getParamAsDouble(val, 0.0)));
				//retval = mergeColValue(retval, secValStr, secColDatatype);
				Misc.setParamInt(ps, ((Integer)retval).intValue(), pos);
			}
			if(contentType == Cache.NUMBER_TYPE) {
				retval = new Double(Misc.getParamAsDouble(val));
				//retval = mergeColValue(retval, secValStr, secColDatatype);
				Misc.setParamDouble(ps, ((Double)retval).doubleValue(), pos);
			}
			if(contentType == Cache.DATE_TYPE || contentType == 7) {//if date type
				retval = ChallanUtils.getDateFromStrDo(val);
				
				//retval = mergeColValue(retval, secValStr, secColDatatype);
				ps.setTimestamp(pos, Misc.utilToSqlDate((java.util.Date) retval));
			}
			if(contentType == Cache.STRING_TYPE) {
				retval = val;
				//retval = mergeColValue(retval, secValStr, secColDatatype);
				ps.setString(pos, val);
			}
			return retval;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	private static Object mergeColValue(Object colValue,String secValStr,int secColDatatype){
		if(colValue == null || secValStr == null || secValStr.length() <= 0)
			return colValue;
		if(secColDatatype == 8){// 8-time
			return new java.util.Date(((java.util.Date) colValue).getTime() + ChallanUtils.getTimeFromStr(secValStr, 0));
		}
		else if(secColDatatype == Cache.STRING_TYPE){
			return colValue + "," + secValStr;
		}
		else if(secColDatatype == Cache.INTEGER_TYPE ){
			return ((Double)colValue).intValue() + Misc.getParamAsInt(secValStr);
		}
		else if(secColDatatype == Cache.NUMBER_TYPE ){
			return ((Double)colValue).doubleValue() + Misc.getParamAsDouble(secValStr);
		}
		return colValue;
	}
	private static void sendUpdMsg(HashMap<Integer, Integer> vehicleUpdated) {
		Set<Integer> keys = vehicleUpdated.keySet();
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (Integer key:keys) {
			list.add(key);
		}
		if (list.size() > 0)
			TripProcessorGateway.updateChallan(list, Misc.getServerName(), false);
	}
	private static boolean isEmptyRow(ArrayList<String> row) {
		for (int i=0,is=row.size();i<is;i++) {
			if (row.get(i) != null && row.get(i).length() > 0)
				return false;
		}
		return true;
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
				if (str.length() == 0)
					str = null;
				if (seenText || str != null) {
					row.add(data.get(i));
					seenText = true;
				}
			}
		}
		return hasNext;
	}


	
}
