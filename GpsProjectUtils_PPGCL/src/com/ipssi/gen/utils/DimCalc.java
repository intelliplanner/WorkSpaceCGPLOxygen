package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;
//import oracle.xml.parser.v2.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.xml.sax.*;
import org.w3c.dom.*;


  public class DimCalc implements Cloneable,Serializable {
     /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String toString() {
		return m_dimInfo == null ? null : this.m_dimInfo.toString();
	}
	public DimInfo m_dimInfo = null;
     public ArrayList m_filterBy = null; // of DimInfo.DimValList
     public ArrayList m_groupBy = null; //of DimInfo.DimValList
     public String m_tag = null;
     
     public Object clone() throws CloneNotSupportedException {
        DimCalc retval = (DimCalc) super.clone();
        if (m_filterBy != null)
            retval.m_filterBy = (ArrayList) m_filterBy.clone();
        if (m_groupBy != null)
            retval.m_groupBy = (ArrayList) m_groupBy.clone();
        return retval;
     }
     public DimCalc(DimInfo dimInfo, ArrayList fiterBy, ArrayList groupBy) {
        m_dimInfo = dimInfo;
        m_filterBy = fiterBy;
        m_groupBy = groupBy;
     
     }
     public DimCalc(Element node) {
        m_dimInfo = DimInfo.getDimInfo(Misc.getParamAsInt(node.getAttribute("id")));
        m_tag = Misc.getParamAsString(node.getAttribute("tag"));
        for (Node n = node.getFirstChild();n!=null;n=n.getNextSibling()) {
           if (n.getNodeType() != 1)
              continue;
           Element e = (Element) n;
           ArrayList addTo = null;
           String tagName = e.getTagName();
           if (tagName.equals("filter")) {
              if (m_filterBy == null)
                 m_filterBy = new ArrayList();
              addTo = m_filterBy;
           }
           else if (tagName.equals("group")) {
              if (m_groupBy == null)
                 m_groupBy = new ArrayList();
              addTo = m_groupBy;
           }
           if (addTo != null) {
              for (Node n1 = e.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
                 if (n1.getNodeType() != 1)
                    continue;
                 Element e1 = (Element) n1;
                 DimInfo.DimValList dimValList = DimInfo.DimValList.readDimValList(e1);
                 if (dimValList != null)
                    addTo.add(dimValList);
              }
           }
        }
     }
  }