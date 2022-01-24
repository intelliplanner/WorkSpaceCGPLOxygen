package com.ipssi.dispatch.helper;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ipssi.gen.utils.CacheTrack;
import com.ipssi.orient.jason.reader.OrientVehicleDataDTO;
import com.ipssi.orient.jason.reader.ShahTransDataDTO;
import com.ipssi.orient.jason.reader.ShahTransWrapperDTO;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebXmlFormatter {

	    public WebXmlFormatter() {
	    }

	    public String format(String unformattedXml) {
	        try {
	            final Document document = parseXmlFile(unformattedXml);

	            OutputFormat format = new OutputFormat(document);
	            format.setLineWidth(65);
	            format.setIndenting(true);
	            format.setIndent(2);
	            Writer out = new StringWriter();
	            XMLSerializer serializer = new XMLSerializer(out, format);
	            serializer.serialize(document);

	            return out.toString();
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	    }

	    private Document parseXmlFile(String in) {
	        try {
	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            DocumentBuilder db = dbf.newDocumentBuilder();
	            InputSource is = new InputSource(new StringReader(in));
	            return db.parse(is);
	        } catch (ParserConfigurationException e) {
	            throw new RuntimeException(e);
	        } catch (SAXException e) {
	            throw new RuntimeException(e);
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	    }

	    public static void main(String[] args) {
	       /* String unformattedXml =
	                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><QueryMessage xmlns=\"http://www.SDMX.org/resources/SDMXML/schemas/v2_0/message\"\n" +
	                        " xmlns:query=\"http://www.SDMX.org/resources/SDMXML/schemas/v2_0/query\">\n" +
	                        "<Query><query:CategorySchemeWhere><query:AgencyID>ECB</query:AgencyID></query:CategorySchemeWhere>\n" +
	                        "    </Query></QueryMessage>";

	        System.out.println(new WebXmlFormatter().format(unformattedXml));
*/	 /**
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		//initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		String xmlString = result.getWriter().toString();
		System.out.println(xmlString);
		*/
	    	
	    	
	    	/* DecimalFormat onedf = new DecimalFormat("0.0");
	    	 DecimalFormat twodf = new DecimalFormat();
	    	 System.out.println(onedf.format(14567.887654));
	    	 System.out.println(twodf.format(1234567.78765));*/
	    	
String ur="http://locate.trackinggenie.com/trackingapi/vstatus.php?apitoken=C5naTAYtm3v*B8+F";
try {
//	sendPOST(ur);
	 call_me();
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
//try{
	// second URL to get orient data
//	URL url = new URL(ur);
	/*in = url.openStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
	Gson gson = new GsonBuilder().create();
	Type type = new TypeToken<Map<String,List<OrientVehicleDataDTO>>>() {}.getType();
	Map<String,List<OrientVehicleDataDTO>> json=gson.fromJson(reader,type);
	List<OrientVehicleDataDTO> dataList=json.get("detail_data");
	for (OrientVehicleDataDTO data : dataList) {
		cachedDataList.add(data);
		data.setVehicle_no(CacheTrack.standardizeName(data.getVehicle_no()));
	}
	System.out.println("Got Girish {"+dataList.size()+"}Vehicle(s) data ");
	g_now = new Date();
	in.close();
}catch(Exception e){
	e.printStackTrace();
}
	}*/ catch (Exception e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
	    }
	    
	    
	    private static void sendPOST(String url) throws IOException {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");

			// For POST only - START
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write("username=STRANS&password=Naminath@21".getBytes());
			os.flush();
			os.close();
			// For POST only - END

			int responseCode = con.getResponseCode();
			System.out.println("POST Response Code :: " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) { //success
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// print result
				System.out.println(response.toString());
			} else {
				System.out.println("POST request not worked");
			}
		}
	    
	    
	    
	    public static void call_me() throws Exception {
		    URL url = new URL("http://locate.trackinggenie.com/trackingapi/vstatus.php");
		    Map<String,String> params = new LinkedHashMap<String,String>();
		    params.put("username", "STRANS");
		    params.put("password", "Naminath@21");
		    params.put("apitoken", "C5naTAYtm3v*B8+F");
		    StringBuilder postData = new StringBuilder();
		    for (Map.Entry<String,String> param : params.entrySet()) {
		        if (postData.length() != 0) postData.append('&');
		        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
		        postData.append('=');
		        postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		    }
		    byte[] postDataBytes = postData.toString().getBytes("UTF-8");
		    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		    conn.setRequestMethod("POST");
		    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		    conn.setDoOutput(true);
		    conn.getOutputStream().write(postDataBytes);
		    Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		    StringBuilder sb = new StringBuilder();
		    for (int c; (c = in.read()) >= 0;)
		        sb.append((char)c);
		    String response = sb.toString();
		    System.out.println(response);
		    Gson gson = new GsonBuilder().create();
			Type type = new TypeToken<ShahTransWrapperDTO>() {}.getType();
			ShahTransWrapperDTO json=gson.fromJson(response,type);//List<ShahTransDataDTO>
			ArrayList<ShahTransDataDTO> dataList=json.getData();
		for (int i = 0; i < dataList.size(); i++) {
			System.out.println(""+((ShahTransDataDTO)dataList.get(i)).toJson()+"");
		} 
		}
	    
	    
}
