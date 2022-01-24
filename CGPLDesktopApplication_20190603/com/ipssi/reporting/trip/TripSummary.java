package com.ipssi.reporting.trip;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;

//import com.ipssi.gen.utils.*;


public class TripSummary {
    public static void main(String[] args) 
    {    	
        try {
        	
            DOMParser parser = new DOMParser();
            parser.parse("C:\\Working\\EclipseWorkspace\\LocReporting\\config_server\\internal.xml");
            Document doc = parser.getDocument();

            NodeList nodes = doc.getElementsByTagName("dimensions");
            System.out.println("There are " + nodes.getLength() + "  elements.");
            Node node = nodes.item(0);
            System.out.println(node.getNodeValue());
            getElementValue(node);

        } catch (Exception ex) {
            System.out.println(ex);
        }
     
    }
    
    public final static void getElementValue( Node elem ) {
    	String select = " select ";
    	String from = " from ";
    	String where = " where ";
    	Node kid;
    	NamedNodeMap nm = null;
    	int tab = 0;
    	if( elem != null){
    		if (elem.hasChildNodes()){
    			for( kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling() ){
    	            System.out.println("==>" + kid.getNodeName());
    	            System.out.println("................." + kid.getNodeValue());
//    	            System.out.println("................." + kid.getTextContent());
    	            System.out.println("................." + kid.getNodeType());
    	            System.out.println("................." + kid.getPrefix());
    	            System.out.println("................." + kid.toString());
    	            System.out.println("................." + kid.hasAttributes());
    	            if(kid.hasAttributes()){
        	            nm =  kid.getAttributes();
	    	            for(int i=0; i<nm.getLength(); i++){
	        	            System.out.println("........................................." + nm.item(i).getNodeName()+" = " + nm.item(i).getNodeValue());
	        	            if(nm.item(i).getNodeName().equals("column")){
	        	            	if(!select.contains(nm.item(i).getNodeValue()))
	        	            		select = select + nm.item(i).getNodeValue() + ", ";
	        	            }
	        	            if(nm.item(i).getNodeName().equals("table")){
	        	            	if(!from.contains(nm.item(i).getNodeValue())){
	        	            		from = from + nm.item(i).getNodeValue() + ", ";
	        	            		tab ++;
	        	            	}
	        	            }
	    	            }
    	            }
    			}
	            select = select.substring(0, select.length()-2);
	            from = from.substring(0, from.length()-2);
	            String query = tab > 1 ? select + from + where : select + from;
	            System.out.println("query = "+ query);
    		}
    	}
    }

}