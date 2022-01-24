package com.ipssi.map.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;


public class TripDefDataInsertion {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {
			HashMap<String, Integer> hMap = new HashMap<String, Integer>();
			Connection con = getConnectionFromPool();
			PreparedStatement ps = null;
			ResultSet rs = null;
			String query = "select id,short_code from regions ";
			ps = con.prepareStatement(query);
			rs = ps.executeQuery();
			while(rs.next()){
				hMap.put(rs.getString("short_code"), rs.getInt("id"));
			}
			rs.close();
			ps.close();
			int id = -1;
			FileReader fr = new FileReader("resources\\trip.xml"); 
			BufferedReader br = new BufferedReader(fr); 
			String s = new String();
			String t = null;
			while((t = br.readLine()) != null) { 
				s =  s + t;
			}
			System.out.println(s); 
			Document dom = load(s);
			NodeList  nList= dom.getElementsByTagName("trip_def");
	    	int size = nList.getLength();
	    	ArrayList<String> strList = new ArrayList<String>();
	    	strList.add("load_gen");//gate_area
	    	strList.add("unload_gen");
	    	strList.add("load_points");//load_area
	    	strList.add("unload_points");
	    	strList.add("load_confirm_gen");
	    	strList.add("load_wait");//wait_area
	    	strList.add("unload_wait");
	    	ArrayList<String> valList = new ArrayList<String>();
	    	
		    for ( int i=0; i<size ; i++){
		        org.w3c.dom.Node node =  nList.item(i);
		        org.w3c.dom.Element element = (org.w3c.dom.Element) node;
		        for (int k = 0; k < strList.size(); k++) {
					String string = strList.get(k);
					NodeList cList =  element.getElementsByTagName(string);
					for (int j = 0; j < cList.getLength(); j++) {
						org.w3c.dom.Node cNode =  cList.item(j);
						org.w3c.dom.Element element_ = (org.w3c.dom.Element) cNode;
				        //System.out.println("element.getAttribute  :  "  +  element_.getAttribute("reg"));
				        if(element_.getAttribute("reg") != null){
				        	valList.add(k, element_.getAttribute("reg"));
				        }
					}
		        }
		       try{
					int did = -1;
					query = " insert into op_station(wait_reg_id,gate_reg_id) values("
						+hMap.get(valList.get(5))+","+hMap.get(valList.get(0))+")";
					ps = con.prepareStatement(query);
					ps.executeUpdate();
					rs = ps.getGeneratedKeys();
					if (rs.next()){
						did = rs.getInt(1);
					}
					rs.close();
					ps.close();
					query = " insert into opstations_opareas(op_station_id,region_id) values("
						+did+","+hMap.get(valList.get(2))+")";
					ps = con.prepareStatement(query);
					ps.executeUpdate();
					ps.close();
					query = " insert into opstation_mapping(op_station_id,port_node_id,type) values("
						+did+",2,1)";
					ps = con.prepareStatement(query);
					ps.executeUpdate();
					ps.close();
					query = " insert into op_station(wait_reg_id,gate_reg_id) values("
						+hMap.get(valList.get(6))+","+hMap.get(valList.get(1))+")";
					ps = con.prepareStatement(query);
					ps.executeUpdate();
					rs = ps.getGeneratedKeys();
					if (rs.next()){
						did = rs.getInt(1);
					}
					rs.close();
					ps.close();
					query = " insert into opstations_opareas(op_station_id,region_id) values("
						+did+","+hMap.get(valList.get(3))+")";
					ps = con.prepareStatement(query);
					ps.executeUpdate();
					ps.close();
					query = " insert into opstation_mapping(op_station_id,port_node_id,type) values("
						+did+",2,2)";
					ps = con.prepareStatement(query);
					ps.executeUpdate();
					ps.close();
//					con.commit();
		       }catch(Exception e){
		    	   e.printStackTrace();
		       }
		    }
		
	}
	public static Connection getConnectionFromPool() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Connection conn = null;
		String userName = "root";
		String password = "ebwebw";
		String url = "jdbc:mysql://localhost/ipssi";

		Class.forName("com.mysql.jdbc.Driver").newInstance();
		conn = DriverManager.getConnection(url, userName, password);
		//System.out.println("Gave Connection.. ..");
		return conn;
	}
	public static Document load(String xmlString) {
	    
		  ByteArrayInputStream xmlDataStream = null;
			try {
				xmlDataStream = new ByteArrayInputStream(xmlString.getBytes("ISO-8859-1"));
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		///** ------------- Comment begin for Old parser use
		      try {

		        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		        dbf.setNamespaceAware(false);

		        // Set the validation mode to either: no validation, DTD
		        // validation, or XSD validation
		        dbf.setValidating(false);

		        // Optional: set various configuration options
		        dbf.setIgnoringComments(true);
		        dbf.setIgnoringElementContentWhitespace(true);
		        dbf.setCoalescing(false);
		        // The opposite of creating entity ref nodes is expanding them inline
		        dbf.setExpandEntityReferences(true);

		        // Step 2: create a DocumentBuilder that satisfies the constraints
		        // specified by the DocumentBuilderFactory
				DocumentBuilder db = dbf.newDocumentBuilder();
		        // Set an ErrorHandler before parsing
		        /*OutputStreamWriter errorWriter =
		            new OutputStreamWriter(System.err, oututEncoding);
		        db.setErrorHandler(
		            new MyErrorHandler(new PrintWriter(errorWriter, true)));*/

		        // Step 3: parse the input file
		        Document doc = db.parse(xmlDataStream);
		        return doc;
		      }
		      catch(SAXParseException spe){
		    	   
		          StringBuilder sb = new StringBuilder( spe.toString() );   
		          sb.append("\n  Line number: " + spe.getLineNumber());   
		          sb.append("\nColumn number: " + spe.getColumnNumber() );   
		          sb.append("\n Public ID: " + spe.getPublicId() );   
		          sb.append("\n System ID: " + spe.getSystemId() + "\n");   
		          System.out.println( sb.toString() );  
		          spe.printStackTrace();
		      }
		      catch (Exception e) {
		        e.printStackTrace();
		        
		      }
		      return null;
		//----      ****/
		    }
}
