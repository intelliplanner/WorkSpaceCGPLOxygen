package com.ipssi.rfid.ui.dao;

import java.math.BigDecimal;
import java.util.logging.Logger;

import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.controller.service.InvoiceServiceI;

import functions.rfc.sap.document.sap_com.BAPIRET1;
import functions.rfc.sap.document.sap_com.ZST_INV_DETAILS_PPGCL_VTS;
import functions.rfc.sap.document.sap_com.ZVTS_SERV_BINDINGStub;
import functions.rfc.sap.document.sap_com.ZWS_INV_PPGCL_VTS_ServiceLocator;
import functions.rfc.sap.document.sap_com.holders.BAPIRET1_TABHolder;

public class InvoiceGeneration implements InvoiceServiceI {
	private static final Logger log = Logger.getLogger(InvoiceGeneration.class.getName());

	@Override
	public BAPIRET1 getSapResp(String vehicleNo, String tprId, String _salesOrder, String _shipTo, String _transporter,
			String _itmNumber, String _inTime, String _outTime, String _tareWt, String _grossWt,
			java.math.BigDecimal _netWt, String hsnNO, String lrNo, String LrDate) throws Exception {
		String address = "http://tpcped.tpc.co.in:8000/sap/bc/srt/rfc/sap/zws_inv_ppgcl_vts/400/zws_inv_ppgcl_vts/zvts_serv_binding"; // Dev Enviroment
//		String address = "http://tpcorasst.tpc.co.in:8000/sap/bc/srt/wsdl/flv_10002A111AD1/bndg_url/sap/bc/srt/rfc/sap/zws_inv_ppgcl_vts/400/zws_inv_ppgcl_vts/zvts_serv_binding?sap-client=400"; // QA Enviroment
		
		address = TokenManager.SAP_INVOICE_CREATION_URL;
		ZST_INV_DETAILS_PPGCL_VTS imInvoiceDetails = new ZST_INV_DETAILS_PPGCL_VTS(_salesOrder, _itmNumber, _netWt,
				_transporter, vehicleNo, _shipTo, hsnNO, _tareWt, _grossWt, _inTime, _outTime, lrNo, LrDate);
		ZVTS_SERV_BINDINGStub stub = new ZVTS_SERV_BINDINGStub();
		ZWS_INV_PPGCL_VTS_ServiceLocator locator = new ZWS_INV_PPGCL_VTS_ServiceLocator();
		locator.setZVTS_SERV_BINDINGEndpointAddress(address);
		System.out.println("[ ENDPOINT_ADDRESS_PROPERTY : HTTP_Port_address= " + locator.getZVTS_SERV_BINDINGAddress() + "  ]");
		stub._setProperty(stub.ENDPOINT_ADDRESS_PROPERTY, locator.getZVTS_SERV_BINDINGAddress());
		stub._setProperty(stub.USERNAME_PROPERTY, TokenManager.SAP_USERNAME);
		stub._setProperty(stub.PASSWORD_PROPERTY, TokenManager.SAP_PASSWORD);
		System.out
				.println("SAP_USERNAME: " + TokenManager.SAP_USERNAME + " SAP_PASSWORD: " + TokenManager.SAP_PASSWORD);

		System.out.println("Address: " + address);
		BAPIRET1_TABHolder bapiretObj = new BAPIRET1_TABHolder();
		javax.xml.rpc.holders.StringHolder ExInvoice = new javax.xml.rpc.holders.StringHolder();
		stub.ZFM_CREATE_INV_PPGCL_VTS(imInvoiceDetails, tprId, bapiretObj, ExInvoice);
		String resp = "";
		BAPIRET1 bapObj = null;
		BAPIRET1[] obj = bapiretObj.value;
		if (obj != null && obj.length > 0) {
			for (int i = 0; i < obj.length; i++) {
				bapObj = obj[i];
				System.out.println("BAPIRET1:-  TYPE: " + bapObj.getTYPE() + "ID: " + bapObj.getID() + " NUMBER: "
						+ bapObj.getNUMBER() + "   MESSAGE:" + bapObj.getMESSAGE() + " LOG_NO: " + bapObj.getLOG_NO()
						+ "  LOG_MSG_NO: " + bapObj.getLOG_MSG_NO() + " MESSAGE_V1: " + bapObj.getMESSAGE_V1()
						+ "  MESSAGE_V2: " + bapObj.getMESSAGE_V2() + " MESSAGE_V3: " + bapObj.getMESSAGE_V3()
						+ "  MESSAGE_V4: " + bapObj.getMESSAGE_V4());
				resp = "BAPIRET1:-  TYPE: " + bapObj.getTYPE() + "ID: " + bapObj.getID() + " NUMBER: "
						+ bapObj.getNUMBER() + " MESSAGE: " + bapObj.getMESSAGE() + " LOG_NO: " + bapObj.getLOG_NO()
						+ " LOG_MSG_NO: " + bapObj.getLOG_MSG_NO() + " MESSAGE_V1: " + bapObj.getMESSAGE_V1()
						+ " MESSAGE_V2: " + bapObj.getMESSAGE_V2() + " MESSAGE_V3: " + bapObj.getMESSAGE_V3()
						+ " MESSAGE_V4: " + bapObj.getMESSAGE_V4();
				if (ExInvoice != null) {
					System.out.println("ExInvoice: " + ExInvoice.value);
					bapObj.setMESSAGE_V1(ExInvoice.value);
				}
			}
		
		}
	

		return bapObj;
	}

	public BAPIRET1 getSapRespTest(String vehicleName, String string, String _salesOrder, String _shipTo,
			String _transporter, String _itmNumber, String _inTime, String _outTime, String printableDouble,
			String printableDouble2, BigDecimal _netWt, String hSN_NO, String lrNo, String lrDate) {
		BAPIRET1 bapObj = new BAPIRET1();
		bapObj.setID("1");
		bapObj.setLOG_NO("890");
		bapObj.setLOG_MSG_NO("12345");
		bapObj.setMESSAGE("Create Invoice");
		bapObj.setMESSAGE_V1("invoiceNum1");
		bapObj.setNUMBER("123");
		bapObj.setTYPE("S");
		return bapObj;
	}


	

}
