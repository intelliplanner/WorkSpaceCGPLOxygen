package com.ipssi.secldata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;

public class LoadXML {


    private static boolean xmlRead = false;
    private static HashMap<Integer, Pair<Pair<String,String>,ArrayList>> dataList = new HashMap<Integer,Pair<Pair<String,String>, ArrayList>>();
public static void main(String[] args) {
	readXML();
}
    public static void readXML()
    {

        try
        {
        	File fXmlFile = new File(Misc.getServerConfigPath()+File.separator+"table_config.xml");
        	//File fXmlFile = new File("C:\\Users\\ipssi2\\Desktop\\table_config.xml");
        	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        	Document doc = dBuilder.parse(fXmlFile);
        	NodeList nList = doc.getElementsByTagName("table");
        	for (int i = 0; i < nList.getLength(); i++) {
    			Node newNode = nList.item(i);
    			Element e = (Element) newNode;
    			String id = Misc.getParamAsString(e.getAttribute("id"));
    			String name = Misc.getParamAsString(e.getAttribute("name"));
                String primaryKey = Misc.getParamAsString(e.getAttribute("pk"));
                ArrayList list = new ArrayList();
                dataList.put(Misc.getParamAsInt(id), new Pair<Pair<String,String>, ArrayList>(new Pair<String,String>(name,primaryKey), list));

    			NodeList childNodeList = e.getElementsByTagName("field");
    			for (int j = 0; j < childNodeList.getLength(); j++) {
    				Node childNode = childNodeList.item(j);
    				Element childElement = (Element) childNode;
    				String columnName = Misc.getParamAsString(childElement.getAttribute("name"));
    				String columType = Misc.getParamAsString(childElement.getAttribute("type"));
    				System.out.println(columnName);
    		           System.out.println(columType);
    		                    Pair<String, String> pair = new Pair<String, String>(columnName, columType);
    		                    list.add(pair);
    		          
    			}
    		}
            xmlRead = true;
        }
        catch (Exception e)
        {
           e.printStackTrace();
        }
    }
    public static Pair<Pair<String,String>, ArrayList> getTableFieldInfo(int id)
    {
        Pair<Pair<String,String>,ArrayList> tableInfo = null;
        try {
            if (!xmlRead && dataList.size() <= 0)
            {
            	readXML();  	
            }                
             tableInfo = dataList.get(id);


        }catch(Exception ex)
        {
         ex.printStackTrace();
        }
        return tableInfo;
    }


}
