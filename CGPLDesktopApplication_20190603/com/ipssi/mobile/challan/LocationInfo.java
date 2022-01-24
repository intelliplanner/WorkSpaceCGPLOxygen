package com.ipssi.mobile.challan;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;

/**
 * Created by ipssi11 on 9/17/2015.
 */
public class LocationInfo {
	int id = Misc.getUndefInt();
    String name;
    double lat = Misc.getUndefDouble();
    double lon = Misc.getUndefDouble();
    
    public LocationInfo() {
    }
    
    public LocationInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public LocationInfo(int id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public StringBuilder toXMl(){
        StringBuilder retval = new StringBuilder();
        try{
            retval.append("<location");
            retval.append(" id=\""+id+"\" ")
            .append(" name=\""+name+"\" ")
            .append(" lat=\""+lat+"\" ")
            .append(" lon=\"" + lon + "\" ")
            .append("/>\n");
        }catch(Exception ex){
        	ex.printStackTrace();
        }
        return retval;
    }
    public static ArrayList<LocationInfo> getLocationInfoList(String xmlStr){
    	ArrayList<LocationInfo> retval = null;
    	LocationInfo location = null;
    	try {
            org.w3c.dom.Document xmlDoc = MyXMLHelper.loadFromString(xmlStr);
            org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("location");
            int length = nList.getLength();
            for ( int i=0; i<length ; i++){
                org.w3c.dom.Node node =  nList.item(i);
                org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                if(element != null){
                	if(retval == null)
                		retval = new ArrayList<LocationInfo>();
                	location = new LocationInfo();
                	load(element,location);
                	retval.add(location);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    	return retval;
    }
    public static StringBuilder getLocationInfoListXML(ArrayList<LocationInfo> locationInfoList){
    	StringBuilder retval = null;
    	try {
            if(locationInfoList != null  && locationInfoList.size() > 0){
            	for(LocationInfo loc : locationInfoList){
            		if(retval == null){
            			retval = new StringBuilder();
            			retval.append("<root>");
            		}
            		retval.append(loc.toXMl().toString());
            		
            	}
            	retval.append("</root>");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    	return retval;
    }
    public LocationInfo(String xmlStr) {
        try {
            org.w3c.dom.Document xmlDoc = MyXMLHelper.loadFromString(xmlStr);
            org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("location");
            int length = nList.getLength();
            for ( int i=0; i<length ; i++){
                org.w3c.dom.Node node =  nList.item(i);
                org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                if(element != null){
                	load(element,this);
                	break;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void load(org.w3c.dom.Element element,LocationInfo locationInfo) {
        try {
        	if(element != null){
        		locationInfo.id = Misc.getParamAsInt(element.getAttribute("id"));
        		locationInfo.name = Misc.getParamAsString(element.getAttribute("name"));
        		locationInfo.lon = Misc.getParamAsDouble(element.getAttribute("lon"));
        		locationInfo.lat =  Misc.getParamAsDouble(element.getAttribute("lat"));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
