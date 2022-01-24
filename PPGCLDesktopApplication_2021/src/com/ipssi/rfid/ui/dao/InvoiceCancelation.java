package com.ipssi.rfid.ui.dao;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import org.apache.axis.AxisFault;

import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.ui.controller.service.InvoiceCancelationServiceI;

import functions.rfc.sap.document.sap_com.cancel_invoce.ZVTS_SERV_BINDINGStub;
import functions.rfc.sap.document.sap_com.cancel_invoce.ZWS_INV_CANCEL_PPGCL_VTS_ServiceLocator;


public class InvoiceCancelation implements InvoiceCancelationServiceI {
	private static final Logger log = Logger.getLogger(InvoiceCancelation.class.getName());
	String address = "http://tpcped.tpc.co.in:8000/sap/bc/srt/rfc/sap/zws_inv_cancel_ppgcl_vts/400/zws_inv_cancel_ppgcl_vts/zvts_serv_binding"; // DEV
//	String address = "http://tpcped.tpc.co.in:8000/sap/bc/srt/rfc/sap/zws_inv_cancel_ppgcl_vts/400/zws_inv_cancel_ppgcl_vts/zvts_serv_binding"; // QA
	@Override
	public String cancelInvoice(String IM_INVOICE) {
		StringBuilder resp =  new StringBuilder();
		try {
			address = TokenManager.SAP_INVOICE_CANCELLATION_URL;
			ZVTS_SERV_BINDINGStub stub = new ZVTS_SERV_BINDINGStub();
			ZWS_INV_CANCEL_PPGCL_VTS_ServiceLocator locator = new ZWS_INV_CANCEL_PPGCL_VTS_ServiceLocator();
			locator.setZVTS_SERV_BINDINGEndpointAddress(address);
			System.out.println("[ ENDPOINT_ADDRESS_PROPERTY : HTTP_Port_address= " + locator.getZVTS_SERV_BINDINGAddress() + "  ]");
			stub._setProperty(stub.ENDPOINT_ADDRESS_PROPERTY, locator.getZVTS_SERV_BINDINGAddress());
			stub._setProperty(stub.USERNAME_PROPERTY, TokenManager.SAP_USERNAME);
			stub._setProperty(stub.PASSWORD_PROPERTY, TokenManager.SAP_PASSWORD);
			System.out.println("SAP_USERNAME: " + TokenManager.SAP_USERNAME + " SAP_PASSWORD: " + TokenManager.SAP_PASSWORD);
			System.out.println("Address: " + address);
			javax.xml.rpc.holders.StringHolder ExCancelInvoice = new javax.xml.rpc.holders.StringHolder();
			javax.xml.rpc.holders.StringHolder ExMESSAGE = new javax.xml.rpc.holders.StringHolder();
			stub.ZFM_CANCEL_INV_PPGCL_VTS(IM_INVOICE, ExCancelInvoice, ExMESSAGE);
			
			if(ExMESSAGE!=null) {
				resp.append("EX_MESSAGE: ").append(ExMESSAGE.value);
			} if(ExCancelInvoice!=null) {
				resp.append(", ExCancelInvoice: ").append(ExCancelInvoice.value);
			}
			
//			Response:
//			0095116394
//			Two times Request
//
//			<env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
//			   <env:Header/>
//			   <env:Body>
//			      <n0:ZFM_CANCEL_INV_PPGCL_VTSResponse xmlns:n0="urn:sap-com:document:sap:rfc:functions">
//			         <EX_CANCEL_INVOICE/>
//			         <EX_MESSAGE>Billing document is already cancelled</EX_MESSAGE>
//			      </n0:ZFM_CANCEL_INV_PPGCL_VTSResponse>
//			   </env:Body>
//			</env:Envelope>
//
//			0095116433
//			<env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
//			   <env:Header/>
//			   <env:Body>
//			      <n0:ZFM_CANCEL_INV_PPGCL_VTSResponse xmlns:n0="urn:sap-com:document:sap:rfc:functions">
//			         <EX_CANCEL_INVOICE>0090000058</EX_CANCEL_INVOICE>
//			         <EX_MESSAGE>Invoice0095116433cancelled with document0090000058</EX_MESSAGE>
//			      </n0:ZFM_CANCEL_INV_PPGCL_VTSResponse>
//			   </env:Body>
//			</env:Envelope>
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resp.toString();
	}

	

}
