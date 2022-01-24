package com.ipssi.cgpl.sap;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.db.RFIDMasterDao;
import com.ipssi.rfid.processor.TPRInformation;


public class SapIntegration {
	
	
	public static RecordsetResp getRespData(String salesOrder, java.math.BigDecimal netWeight, String transporterName,
			String vehicleName, String shipTo, String hsnNo, String tareWt, String grossWt, String inTime,
			String outTime, String itmNumber, String tprId) {
		System.out.println("###   SAP Integration Start   ###");
		RecordsetResp recordsetResp = null;
		try {
			
		
			RecordsetIM_INVOICE_DETAILS rsInvoiceDetails = new RecordsetIM_INVOICE_DETAILS(salesOrder, netWeight,
					transporterName, vehicleName, shipTo, hsnNo, tareWt, grossWt, inTime, outTime, itmNumber);
			
			Recordset recordsetRequest = new Recordset(rsInvoiceDetails, tprId);

			FlyAshInvoice_OutServiceLocator fls = new FlyAshInvoice_OutServiceLocator();

			FlyAshInvoice_OutBindingStub stub = new FlyAshInvoice_OutBindingStub();
			stub._setProperty(stub.ENDPOINT_ADDRESS_PROPERTY, fls.getHTTP_PortAddress());
			stub._setProperty(stub.USERNAME_PROPERTY,"RFIDUSER");
			stub._setProperty(stub.PASSWORD_PROPERTY,"Ipssi@123");
			
			System.out.println("Fetching Record Start");
			recordsetResp = stub.flyAshInvoice_Out(recordsetRequest);
			System.out.println("Fetching Record Complete");
			
//			recordsetResp  = fls.getHTTP_Port().flyAshInvoice_Out(recordsetRequest);
			
			
			RecordsetRespIM_RETURN  recordsetRespImReturn = recordsetResp.getIM_RETURN();
			
			System.out.println("RecordSetResponseOutput: EX_INVOICE: " +recordsetRespImReturn.getEX_INVOICE()  + ", MESSAGE: "+  recordsetRespImReturn.getMESSAGE() + ", TYPE: "+ recordsetRespImReturn.getTYPE() );
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println(e);
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
		System.out.println("###   SAP Integration End   ###");
		return recordsetResp;
	}

	
	

	public static void main(String args[]) {

		try {
		System.out.println("SapIntegration.getRespData() Start");
		int tprId = 1; 
		BigDecimal _netWt = new BigDecimal(10.20);//(TEXT_NET_WEIGHT.getText() != null && TEXT_NET_WEIGHT.getText().length() > 0) ?  new BigDecimal(TEXT_NET_WEIGHT.getText()) : new BigDecimal(0);
		_netWt = _netWt.setScale(2,  BigDecimal.ROUND_DOWN);
		RecordsetResp recordsetResp = SapIntegration.getRespData("0000003360",_netWt,"BKB","TEST2132","A.P. Power Co-Ordination Committee", "26219000", "14.30", "24.30", "17:36", "17:37","10","w-5");
		if(recordsetResp != null && recordsetResp.getIM_RETURN() != null) {
			System.out.println("Data: "+ recordsetResp);
			System.out.println("Type: "+recordsetResp.getIM_RETURN().getTYPE() + ", Message: "+ recordsetResp.getIM_RETURN().getMESSAGE() + ", ExInvoice: "  + recordsetResp.getIM_RETURN().getEX_INVOICE() );
		}
		System.out.println("SapIntegration.getRespData() End");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	
		
	}	
	
	public static StringBuilder generateInvoice(Connection conn, String tprId) {
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat requireFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		
		try {
			TPRecord tpRecord = getTpr(conn, Integer.parseInt(tprId));
			
			if(tpRecord!=null){
				
				String inTime = requireFormat.format(tpRecord.getComboStart());
				String outTime =  requireFormat.format(tpRecord.getLatestLoadWbInExit());
				BigDecimal netWt = getNetWeight(tpRecord.getLoadGross(),tpRecord.getLoadTare());
				if(netWt !=null){	
					String tpr_id = "w-" + tprId;
					System.out.println("Data: "+tpRecord.getProductCode()+", "+netWt +", "+tpRecord.getTransporterCode()+", "+	tpRecord.getVehicleName()+", "+ tpRecord.getConsigneeName()+", "+ tpRecord.getWasheryCode()+", "+   Misc.getPrintableDouble(tpRecord.getLoadTare())+", "+ Misc.getPrintableDouble(tpRecord.getLoadGross())+", "+ inTime+", "+outTime+", "+ tpRecord.getDoNumber()+", "+ tpr_id);
					
					RecordsetResp recordsetResp= getRespData(tpRecord.getProductCode(),netWt ,tpRecord.getTransporterCode(),
							tpRecord.getVehicleName(), tpRecord.getConsigneeName(), tpRecord.getWasheryCode(),   Misc.getPrintableDouble(tpRecord.getLoadTare()), Misc.getPrintableDouble(tpRecord.getLoadGross()), inTime,outTime, tpRecord.getDoNumber(), tpr_id);
					if(recordsetResp!=null && recordsetResp.getIM_RETURN()!=null){
				    	String sapType = recordsetResp.getIM_RETURN().getTYPE();
				    	int sapStatus= sapType.equalsIgnoreCase("S") ? RecordType.MessageType.SUCCESS : RecordType.MessageType.FAILED;
				    	tpRecord.setReportingStatus(sapStatus);
						tpRecord.setMessage(recordsetResp.getIM_RETURN().getMESSAGE());
						tpRecord.setExInvoice(recordsetResp.getIM_RETURN().getEX_INVOICE());
						
						System.out.println("[Response- SAP_Type:"+ sapType + "EX_INVOICE: " + recordsetResp.getIM_RETURN().getEX_INVOICE()+ "MESSAGE: "+recordsetResp.getIM_RETURN().getMESSAGE());
						TPRInformation.insertUpdateTpr(conn, tpRecord);
						sb.append("Success");
					}else{
						sb.append("Invoice not generated");
						System.out.println("Invoice not generated");
					}
						
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			sb.append(ex);
		}
		return sb;
	}	
	



	private static BigDecimal getNetWeight(double loadGross, double loadTare) {
		if(Misc.isUndef(loadGross) && Misc.isUndef(loadTare)){
			return null;
		}
		double Wb_Net_Wt =  loadGross - loadGross;
		BigDecimal _netWt = !Misc.isUndef(Wb_Net_Wt)  ?  new BigDecimal(Wb_Net_Wt) : new BigDecimal(0);
		_netWt = _netWt.setScale(2,  BigDecimal.ROUND_DOWN);
		return _netWt;
	}



	public static StringBuilder cancelInvoice(Connection conn, String tprId){
		return null;
	}
	
	public static TPRecord getTpr(Connection conn, int tprId) throws Exception {

		System.out.println(" ######## Start Get Date From selectTpr(Connection conn, int tprId) ######");

		TPRecord tprBean = null;
		ArrayList<Object> list = null;
		try {
			tprBean = new TPRecord();
			tprBean.setTprId(tprId);
			list = RFIDMasterDao.select(conn, tprBean);
			if(list==null)
				return null;
			for (int i = 0; i < list.size(); i++) {
				tprBean = (TPRecord) list.get(i);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		System.out.println(" ######## End Get Date From selectTPR(Connection conn, int tprId) ######");
		return tprBean;
	}

}
