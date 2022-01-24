package com.ipssi.reporting.trip;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.servlet.jsp.JspWriter;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
//currently for HTML - eventually make an interface or abstract class
public class HTMLStreamingGenerator {
	protected int reportType = Misc.HTML;
	protected SessionManager _session;
	protected StringBuilder sb = new StringBuilder();
	protected JspWriter out = null;
	protected boolean inHeader = false;
	protected boolean tableOpen = false;
	protected boolean headerOrBodyOpen = false;
	protected boolean trOpen = false;
	protected boolean tdOpen = false;
	protected Table table = null;
	protected boolean doPlainTable = false;
	protected Writer genOut = null;
	public StringBuilder addnlHeader = null;
	
	public HTMLStreamingGenerator(int reportType, SessionManager _session, JspWriter out, Table table, boolean doPlainTable, Writer genOut, StringBuilder addnlHeader) {
		this.doPlainTable = doPlainTable;
		this.reportType = reportType;
		this._session = _session;
		this.out = out;
		this.table = table;
		this.genOut = genOut;
		this.addnlHeader = addnlHeader;
	}
	public JspWriter getWriter() {
		return out;
	}
	public int getReportType() {
		return reportType;
	}
	public void callOnTableCreate() throws Exception {
		HtmlGenerator.printTableStart(sb, _session, doPlainTable ? 
				HtmlGenerator.styleWithoutAdornment
				:
					HtmlGenerator.styleWithAdornment
					);
		tableOpen = true;
		out.println(sb);
		sb.setLength(0);
	}
	public void callOnTHeadCreate() throws Exception {
		callOnTRCreate();//will be vacous
		out.println("<THEAD>");
		headerOrBodyOpen = true;
		inHeader = true;
	}
	public void callOnTBodyCreate() throws Exception {
		callOnTRCreate();
		if (headerOrBodyOpen && inHeader) {			
			out.println("</THEAD>");
			headerOrBodyOpen = false;
		}
		inHeader = false;
		out.println("<TBODY>");
		headerOrBodyOpen = true;
	}
	public void callOnTRCreate() throws IOException {
		ArrayList<TR> list = inHeader ? table.getHeader() : table.getBody();
		TR lastRow = list == null || list.size() == 0 ? null : list.get(list.size()-1);
		if (lastRow != null) {
			if (inHeader)
				HtmlGenerator.printHeaderRow(lastRow, sb, _session);
			else
				HtmlGenerator.printBodyRow(lastRow,sb,_session);
			out.println(sb);
			sb.setLength(0);
			list.remove(list.size()-1);
		}
	}
	public void callOnTDCreate() {
		//do nothing
	}
	public void callOnNestedTableCreate() {
		//TODO
	}
	public void callOnClose() throws Exception {
		callOnTRCreate();
		if (tdOpen) {
			out.println("</td>");
			tdOpen = false;
		}
		if (trOpen) {
			out.println("</tr>");
			trOpen = false;
		}
		if (headerOrBodyOpen) {
			out.println(inHeader ? "</THEAD>" : "</TBODY>");
			headerOrBodyOpen = false;
		}
		out.println("</TABLE>");
		tableOpen = false;
	}
	public Writer getGenOut() {
		return genOut;
	}
	public void setGenOut(Writer genOut) {
		this.genOut = genOut;
	}
	
}
