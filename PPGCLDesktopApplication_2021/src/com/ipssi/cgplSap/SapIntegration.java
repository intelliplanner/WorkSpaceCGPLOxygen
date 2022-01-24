package com.ipssi.cgplSap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.Base64;
import java.util.logging.Logger;

import com.ipssi.rfid.processor.TokenManager;


public class SapIntegration {
	
	private static final Logger log = Logger.getLogger(SapIntegration.class.getName());
	
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

	
	
	public static void sendToNicerGlobes() {
        
        try {
             
        //Code to make a webservice HTTP request
        	String userName="RFIDUSER";
        	String pass="Tata@123";
        String responseString = "";
        String outputString = "";
//      String wsURL = "http://www.deeptraining.com/webservices/weather.asmx";
//      String wsURL = "http://nicerglobe.org/nicerglobeparser/gpsdataservice";
        String wsURL = "http://tpcpid.tpc.co.in:50000/dir/wsdl?p=ic/dd77e0f1eec030dea6b6dc970607cd40";
        
        URL url = new URL(wsURL);
        URLConnection connection = url.openConnection();
        
        HttpURLConnection httpConn = (HttpURLConnection)connection;
        String encoded = Base64.getEncoder().encodeToString((userName+":"+pass).getBytes(StandardCharsets.UTF_8));  //Java 8
        connection.setRequestProperty("Authorization", "Basic "+encoded);
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
//        String xmlInput =
//              "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><gpsDataElement><DATAELEMENTS><DATAELEMENTS>" +
//              "<LATITUDE>"+gpsData.getLatitude()+"</LATITUDE>" +
//              "<LONGITUDE>"+gpsData.getLongitude()+"</LONGITUDE>" +
//              "<SPEED>"+gpsData.getSpeed()+"</SPEED>" +
//              "<HEADING></HEADING>" +
//              "<DATETIME>"+dateStr+"</DATETIME>" +
//              "<IGNSTATUS>"+ingStatus+"</IGNSTATUS>" +
//              "<LOCATION></LOCATION>" +
//              "</DATAELEMENTS></DATAELEMENTS><VEHICLENO>"+vehName+"</VEHICLENO></gpsDataElement>";
        
        String xmlInput = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:vts=\"http://vts.com:SD:VTS:FlyAshInvoice\">" + 
        		"   <soapenv:Header/>" + 
        		"   <soapenv:Body> " + 
        		"      <vts:RecordsetRequest>" + 
        		"         <IM_INVOICE_DETAILS>" + 
        		"            <SALESORDER>0000003363</SALESORDER> " + 
        		"			<NET_WEIGHT>0.00</NET_WEIGHT>" + 
        		"			<TRANSPORTER>BKB</TRANSPORTER>" + 
        		"			<VEHICLE_NO>TEST2132</VEHICLE_NO>" + 
        		"			<SHIPTO>A.P. Power Co-Ordination Committee</SHIPTO>			" + 
        		"			<HSN_NO>26219000</HSN_NO>" + 
        		"			<EMPTY_WT>14.30</EMPTY_WT>" + 
        		"			<GROSS_WT>24.30</GROSS_WT>" + 
        		"			<IN_TIME>2019/05/03 02:55:23</IN_TIME>" + 
        		"			<OUT_TIME>2019/05/03 03:55:23</OUT_TIME>" + 
        		"			<ITM_NUMBER>000010</ITM_NUMBER>" + 
        		"		  </IM_INVOICE_DETAILS>" + 
        		"		  <IM_TPRID>12</IM_TPRID>" + 
        		"      </vts:RecordsetRequest>" + 
        		"   </soapenv:Body>" + 
        		"</soapenv:Envelope>";
        
        
        
        System.out.println("EilParser.sendToNicerGlobe() xmlData : "+xmlInput); 
        byte[] buffer = new byte[xmlInput.length()];
        buffer = xmlInput.getBytes();
        bout.write(buffer);
        byte[] b = bout.toByteArray();
//      String SOAPAction = "http://nicerglobe.org/nicerglobeparser/gpsdataservice";
        String SOAPAction = "http://tpcpid.tpc.co.in:50000/dir/wsdl?p=ic/dd77e0f1eec030dea6b6dc970607cd40";//redirectUrl;
//      "http://litwinconsulting.com/webservices/GetWeather";
        // Set the appropriate HTTP parameters.
        httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
        httpConn.setRequestProperty("Content-Type", "application/xml; charset=utf-8");
        httpConn.setRequestProperty("SOAPAction", SOAPAction);
        httpConn.setRequestMethod("POST");
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
//         httpConn.getOutputStream();
        //Write the content of the request to the outputstream of the HTTP Connection.
//        out.write(b);
        
        //Ready with sending the request.
        
        //Read the response.
        InputStreamReader isr =
        new InputStreamReader(httpConn.getInputStream());
        BufferedReader in = new BufferedReader(isr);
        
        //Write the SOAP message response to a String.
        while ((responseString = in.readLine()) != null) {
              outputString = outputString + responseString;
              System.out.println("EilParser.sendToNicerGlobe() xmlResp : "+outputString);
        }
        //Parse the String output to a org.w3c.dom.Document and be able to reach every node with the org.w3c.dom API.
        /*Document document = parseXmlFile(outputString);
        NodeList nodeLst = document.getElementsByTagName("GetWeatherResult");
        String weatherResult = nodeLst.item(0).getTextContent();
        System.out.println("Weather: " + weatherResult);
        
        //Write the SOAP message formatted to the console.
        String formattedSOAPResponse = formatXML(outputString);
        System.out.println(formattedSOAPResponse);
        return weatherResult;*/
//        out.close();
        in.close();
        } catch (MalformedURLException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
        } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
        }
  }

	public static void main(String args[]) {
		
	}	
	public static void test(){
		try {
		System.out.println("SapIntegration.getRespData() Start");
		int tprId = 1; 
		BigDecimal _netWt = new BigDecimal(10.20);//(TEXT_NET_WEIGHT.getText() != null && TEXT_NET_WEIGHT.getText().length() > 0) ?  new BigDecimal(TEXT_NET_WEIGHT.getText()) : new BigDecimal(0);
		_netWt = _netWt.setScale(2,  BigDecimal.ROUND_DOWN);
		RecordsetResp recordsetResp = SapIntegration.getRespData("0000003360",_netWt,"BKB","TEST2132","A.P. Power Co-Ordination Committee", "26219000", "14.30", "24.30", "17:36", "17:37","10",5);
		if(recordsetResp != null && recordsetResp.getIM_RETURN() != null) {
			System.out.println("Data: "+ recordsetResp);
			System.out.println("Type: "+recordsetResp.getIM_RETURN().getTYPE() + ", Message: "+ recordsetResp.getIM_RETURN().getMESSAGE() + ", ExInvoice: "  + recordsetResp.getIM_RETURN().getEX_INVOICE() );
		}
		System.out.println("SapIntegration.getRespData() End");
//		System.out.println("SapIntegration.sendToNicerGlobe() Start");
//		SapIntegration.sendToNicerGlobe();
//		System.out.println("SapIntegration.sendToNicerGlobe()  End");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
//	public static void sendToNicerGlobe(CacheTrack.VehicleSetup vehSetup, GpsData gpsData, int vehicleId, String redirectUrl) {
//        
//        try {
//              if(gpsData == null)
//                    return;
//        //Code to make a webservice HTTP request
//        String responseString = "";
//        String outputString = "";
////      String wsURL = "http://www.deeptraining.com/webservices/weather.asmx";
////      String wsURL = "http://nicerglobe.org/nicerglobeparser/gpsdataservice";
//        String wsURL = redirectUrl;
//        URL url = new URL(wsURL);
//        URLConnection connection = url.openConnection();
//        HttpURLConnection httpConn = (HttpURLConnection)connection;
//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//
//        String vehName = vehSetup.m_name;
//
//        String ingStatus = "0";
//        if(gpsData.getSpeed() > 0.1)
//              ingStatus = "1";
//        
//        String xmlInput =
//              "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><gpsDataElement><DATAELEMENTS><DATAELEMENTS>" +
//              "<LATITUDE>"+gpsData.getLatitude()+"</LATITUDE>" +
//              "<LONGITUDE>"+gpsData.getLongitude()+"</LONGITUDE>" +
//              "<SPEED>"+gpsData.getSpeed()+"</SPEED>" +
//              "<HEADING></HEADING>" +
//              "<DATETIME>"+dateStr+"</DATETIME>" +
//              "<IGNSTATUS>"+ingStatus+"</IGNSTATUS>" +
//              "<LOCATION></LOCATION>" +
//              "</DATAELEMENTS></DATAELEMENTS><VEHICLENO>"+vehName+"</VEHICLENO></gpsDataElement>";
//        System.out.println("EilParser.sendToNicerGlobe() xmlData : "+xmlInput); 
//        byte[] buffer = new byte[xmlInput.length()];
//        buffer = xmlInput.getBytes();
//        bout.write(buffer);
//        byte[] b = bout.toByteArray();
////      String SOAPAction = "http://nicerglobe.org/nicerglobeparser/gpsdataservice";
//        String SOAPAction = redirectUrl;
////      "http://litwinconsulting.com/webservices/GetWeather";
//        // Set the appropriate HTTP parameters.
//        httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
//        httpConn.setRequestProperty("Content-Type", "application/xml; charset=utf-8");
//        httpConn.setRequestProperty("SOAPAction", SOAPAction);
//        httpConn.setRequestMethod("POST");
//        httpConn.setDoOutput(true);
//        httpConn.setDoInput(true);
//        OutputStream out = httpConn.getOutputStream();
//        //Write the content of the request to the outputstream of the HTTP Connection.
//        out.write(b);
//        
//        //Ready with sending the request.
//        
//        //Read the response.
//        InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
//        BufferedReader in = new BufferedReader(isr);
//        
//        //Write the SOAP message response to a String.
//        while ((responseString = in.readLine()) != null) {
//              outputString = outputString + responseString;
//              System.out.println("EilParser.sendToNicerGlobe() xmlResp : "+outputString);
//        }
//        
//        out.close();
//        in.close();
//        } catch (MalformedURLException e) {
//              // TODO Auto-generated catch block
//              e.printStackTrace();
//        } catch (IOException e) {
//              // TODO Auto-generated catch block
//              e.printStackTrace();
//        }
//  }

	
	
	
}
