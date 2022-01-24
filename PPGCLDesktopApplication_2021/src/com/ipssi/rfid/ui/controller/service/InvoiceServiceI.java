package com.ipssi.rfid.ui.controller.service;

import java.math.BigDecimal;

import functions.rfc.sap.document.sap_com.BAPIRET1;

public interface InvoiceServiceI {

	BAPIRET1 getSapResp(String vehicleNo,String tprId, String _salesOrder, String _shipTo, String _transporter,
			String _itmNumber, String _inTime, String _outTime, String _tareWt, String _grossWt, BigDecimal _netWt,
			String hsnNO, String lrNo, String LrDate) throws Exception;
	

	// <xsd:element name="SALESORDER" type="tns:char10"/>
	// <xsd:element name="ITMNUMBER" type="tns:numeric6"/>
	// <xsd:element name="NET_WEIGHT" type="tns:quantum13.3"/>
	// <xsd:element name="TRANSPORTER" type="tns:char255"/>
	// <xsd:element name="VEHICLE_NO" type="tns:char255"/>
	// <xsd:element name="SHIPTO" type="tns:char255"/>
	// <xsd:element name="HSN_NO" type="tns:char255"/>
	// <xsd:element name="EMPTY_WT" type="tns:char255"/>
	// <xsd:element name="GROSS_WT" type="tns:char255"/>
	// <xsd:element name="IN_TIME" type="tns:char255"/>
	// <xsd:element name="OUT_TIME" type="tns:char255"/>
	// <xsd:element name="LR_NO" type="tns:char255"/>
	// <xsd:element name="LR_DATE" type="tns:char255"/>


}
