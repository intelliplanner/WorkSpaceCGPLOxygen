package com.ipssi.reporting.trip;

import java.util.ArrayList;

import javax.servlet.ServletOutputStream;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;

public class XmlGenerator {
	public static void printXML(Table table,StringBuilder sb, SessionManager _session, ServletOutputStream servletStream){
		org.w3c.dom.Document doc = null;
		org.w3c.dom.Element root = null;
		Pair<org.w3c.dom.Document,org.w3c.dom.Element> xml = null;
		xml = MyXMLHelper.getDocument("TABLE_DATA");
		if(xml != null){
			doc = xml.first;
			root = xml.second;
		}
		printHeader(table.getHeader(), doc, root, _session);
		printBody(table.getBody(), doc, root, _session);
		if(doc != null)
			MyXMLHelper.getStreamXMLData(doc, servletStream);
		sb.append(doc.toString());
	}
	private static void printHeader(ArrayList<TR> header,org.w3c.dom.Document doc, org.w3c.dom.Element root,SessionManager _session){
		org.w3c.dom.Element headerEle = null;
		org.w3c.dom.Element rowEle = null;
		org.w3c.dom.Element colEle = null;
		if (header != null){
			try{
				headerEle = MyXMLHelper.addElement(doc, root, "TABLE_HEADER");
				for (TR row : header){
					rowEle = MyXMLHelper.addElement(doc, headerEle, "ROW");
					if (row.getId() != null)
						MyXMLHelper.addAttribute(rowEle, "id",row.getId()+"");
					for(TD col : row.getRowData()){
						colEle = MyXMLHelper.addElement(doc, rowEle, "COL");
						MyXMLHelper.addAttribute(colEle, "id", col.getId()+"");
						MyXMLHelper.addAttribute(colEle, "rowspan", col.getRowSpan()+"");
						MyXMLHelper.addAttribute(colEle, "colspan", col.getColSpan()+"");
						MyXMLHelper.addAttribute(colEle, "dt_type", getDataType(col.getContentType(),_session));
						MyXMLHelper.addAttribute(colEle, "class", !Misc.isUndef(col.getClassId()) ? CssClassDefinition.getHtmlCssClass(col.getClassId()) : CssClassDefinition.getHtmlCssClass(row.getClassId()));
						MyXMLHelper.addAttribute(colEle, "content", col.getContent() == null ? "" : col.getContent());
						MyXMLHelper.addAttribute(colEle, "hidden", col.getHidden()? "1": "0");
					}
				}
			}catch(Exception e){
				e.printStackTrace();		
			}
		}
	}
	private static void printBody(ArrayList<TR> body,org.w3c.dom.Document doc, org.w3c.dom.Element root,SessionManager _session){
		org.w3c.dom.Element bodyEle = null;
		org.w3c.dom.Element rowEle = null;
		org.w3c.dom.Element colEle = null;
		if (body != null){
			try{
				bodyEle = MyXMLHelper.addElement(doc, root, "TABLE_BODY");
				for (TR row : body){
					rowEle = MyXMLHelper.addElement(doc, bodyEle, "ROW");
					if (row.getId() != null)
						MyXMLHelper.addAttribute(rowEle, "id",row.getId()+"");
					for(TD col : row.getRowData()){
						colEle = MyXMLHelper.addElement(doc, rowEle, "COL");
						MyXMLHelper.addAttribute(colEle, "id", col.getId()+"");
						MyXMLHelper.addAttribute(colEle, "rowspan", col.getRowSpan()+"");
						MyXMLHelper.addAttribute(colEle, "colspan", col.getColSpan()+"");
						MyXMLHelper.addAttribute(colEle, "class", !Misc.isUndef(col.getClassId()) ? CssClassDefinition.getHtmlCssClass(col.getClassId()) : CssClassDefinition.getHtmlCssClass(row.getClassId()));
						MyXMLHelper.addAttribute(colEle, "content", col.getContent() == null ? "" : col.getContent());
					}
				}
			}catch(Exception e){
				e.printStackTrace();		
			}
		}
	}
	private static String getDataType(int contentType, SessionManager _session){
		Cache cache = _session.getCache();
		boolean doDate = contentType == cache.DATE_TYPE;
		boolean doNumber = contentType == cache.NUMBER_TYPE;
		boolean doInterval = contentType == 20510;
		return doDate ? "date" : doInterval ? "interval" : doNumber ? "num" : "text" ;
	}
}
