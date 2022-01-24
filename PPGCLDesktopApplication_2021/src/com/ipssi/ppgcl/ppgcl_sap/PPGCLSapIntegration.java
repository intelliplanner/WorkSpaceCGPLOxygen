package com.ipssi.ppgcl.ppgcl_sap;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import com.ipssi.cgplSap.FlyAshInvoice_OutBindingStub;
import com.ipssi.cgplSap.FlyAshInvoice_OutServiceLocator;
import com.ipssi.cgplSap.Recordset;
import com.ipssi.cgplSap.RecordsetIM_INVOICE_DETAILS;
import com.ipssi.cgplSap.RecordsetResp;
import com.ipssi.cgplSap.RecordsetRespIM_RETURN;
import com.ipssi.rfid.processor.TokenManager;

public class PPGCLSapIntegration {
	private static final Logger log = Logger.getLogger(PPGCLSapIntegration.class.getName());
	public static RecordsetResp getRespData(String salesOrder, java.math.BigDecimal netWeight, String transporterName,
			String vehicleName, String shipTo, String hsnNo, String tareWt, String grossWt, String inTime,
			String outTime, String itmNumber, int tprId) {
		System.out.println("###   SAP Integration Start   ###");
		RecordsetResp recordsetResp = null;
		try {
			
		
			RecordsetIM_INVOICE_DETAILS rsInvoiceDetails = new RecordsetIM_INVOICE_DETAILS(salesOrder, netWeight,
					transporterName, vehicleName, shipTo, hsnNo, tareWt, grossWt, inTime, outTime, itmNumber);
			
			Recordset recordsetRequest = new Recordset(rsInvoiceDetails, Integer.toString(tprId));

			FlyAshInvoice_OutServiceLocator fls = new FlyAshInvoice_OutServiceLocator();

			FlyAshInvoice_OutBindingStub stub = new FlyAshInvoice_OutBindingStub();
			System.out.println("[ ENDPOINT_ADDRESS_PROPERTY : HTTP_Port_address= "+ fls.getHTTP_PortAddress()+ "  ]");
			stub._setProperty(stub.ENDPOINT_ADDRESS_PROPERTY, fls.getHTTP_PortAddress());
			stub._setProperty(stub.USERNAME_PROPERTY,TokenManager.SAP_USERNAME);
			stub._setProperty(stub.PASSWORD_PROPERTY,TokenManager.SAP_PASSWORD);
			
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
}
