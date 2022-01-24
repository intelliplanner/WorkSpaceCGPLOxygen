package com.ipssi.reporting.trip;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.servlet.jsp.JspWriter;

import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;

public class TextStreamingGenerator extends HTMLStreamingGenerator {
	private TextGenerator textPrinter = null;
	public TextStreamingGenerator(int reportType, SessionManager _session, JspWriter out, Table table, FrontPageInfo fpi, Writer genOut, StringBuilder addnlHeaderLines) {
		super(reportType, _session, out, table, true, genOut,addnlHeaderLines);
		textPrinter = new TextGenerator(addnlHeaderLines);
		textPrinter.prepare(fpi, _session);
	}
	public void callOnTableCreate() throws Exception {
		//
		tableOpen = true;
		//out.println(sb);
		//sb.setLength(0);
	}
	public void callOnTHeadCreate() throws Exception {
		callOnTRCreate();//will be vacous
		//out.println("<THEAD>");
		headerOrBodyOpen = true;
		inHeader = true;
	}
	public void callOnTBodyCreate() throws Exception {
		callOnTRCreate();
		if (headerOrBodyOpen && inHeader) {			
			//out.println("</THEAD>");
			headerOrBodyOpen = false;
			textPrinter.printHeader(this.table.getHeader(), sb, _session);
			if (genOut != null)
				genOut.write(sb.toString());
			else
				out.print(sb);
			sb.setLength(0);
		}
		inHeader = false;
		//out.println("<TBODY>");
		headerOrBodyOpen = true;
	}
	public void callOnTRCreate() throws IOException {
		ArrayList<TR> list = inHeader ? table.getHeader() : table.getBody();
		TR lastRow = list == null || list.size() == 0 ? null : list.get(list.size()-1);
		if (lastRow != null) {
			if (inHeader) {
			//	textPrinter.printHeaderRow(lastRow, sb, _session);
			}
			else {
				textPrinter.printBodyRow(lastRow,sb,_session, true);
				if (genOut != null)
					genOut.write(sb.toString());
				else
					out.print(sb);
				sb.setLength(0);
				list.remove(list.size()-1);
			}
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
		textPrinter.printEndOfPage(sb, true);
		System.out.println("Thread:"+Thread.currentThread().getId()+" [END of Report:]"+textPrinter.getPageNumber());
		if (genOut != null)
			genOut.write(sb.toString());
		else
			out.write(sb.toString());
		sb.setLength(0);
		if (tdOpen) {
			//out.println("</td>");
			tdOpen = false;
		}
		if (trOpen) {
			//out.println("</tr>");
			trOpen = false;
		}
		if (headerOrBodyOpen) {
			//out.println(inHeader ? "</THEAD>" : "</TBODY>");
			headerOrBodyOpen = false;
		}
		//out.println("</TABLE>");
		tableOpen = false;
	}

}
