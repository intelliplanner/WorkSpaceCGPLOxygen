package com.ipssi.reporting.trip;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.w3c.dom.Document;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.DimConfigInfo;
import com.ipssi.gen.utils.DimInfo;
import com.ipssi.gen.utils.FrontPageInfo;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;
import com.ipssi.gen.utils.Pair;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.input.InputTemplate;

public class TextGenerator {
	private char rowMarker = '-';
	private boolean doHeaderRowMarker = true;
	private boolean doIntermediareRowMarker = false;
	private boolean doGroupingRowMarker = true;
	private ArrayList<Integer> charWidth = new ArrayList<Integer>();
	private ArrayList<Integer> doTruncation = new ArrayList<Integer>();
	private ArrayList<Integer> allAlignment = new ArrayList<Integer>();
	private String inBetweenColSeparator = " ";
	private String rowStarter = "";
	private String rowEnder = "";
	private int numCharsInRow = 0; 
	private int numCols = 0;
	private int maxRowsPerPage = 88;
	private int blankHeaderLine = 4;
	private int blankFooterLine = 3;
	private String firstPageHeaderFile = null;
	private String lastFooterFile = null;
	private String innerPageHeaderFile = null;
	private String refInnerPageHeader = "SECL %project_name% Project";
	private String innerPageFooterFile = null;
	private String firstPageHeaderText = null;
	private int firstPageHeaderLineCount = 0;
	private String innerPageHeaderText = null;
	private int innerPageHeaderLineCount = 0;
	private String innerPageFooterText = null;
	private int innerPageFooterLineCount = 0;
	private String lastFooterText = null;
	private int lastFooterLineCount = 0;
	private String escCode = null;
	
	private StringBuilder addnlHeaderLines = null;
	private boolean lastRowHadRowSeparator = false;
	private boolean isFirstHeaderRow = true;
	private int numLinesOnCurrPage = 0;
	private StringBuilder headerRows = new StringBuilder();
	private int headerRowsCount = 0;
	private int pageNumber = 0;
	private SessionManager session = null; //this keeps projectName which is populated from WB ... but gets populated at first data Row
	private boolean tempInitedProjectNameFromSession = false;//incase attempt to initiate innerHeader from projectName in session has been done
	public TextGenerator(StringBuilder addnlHeaderLines) {
		this.addnlHeaderLines = addnlHeaderLines;
	}
	
	public Pair<String, Integer> readTemplate(String file, int width) {
	 	   FileReader  inp = null;
	 	  String retval = null;
	 	  int numRows = 0;
		   try {
			   if (file != null) {
				   inp = new FileReader(Cache.serverConfigPath+System.getProperty("file.separator")+"web_look"+System.getProperty("file.separator")+"project"+System.getProperty("file.separator")+file);
				   char data[] = new char[1024];
				   StringBuilder sb = new StringBuilder();
				   int count = 0;
				   while ((count = inp.read(data, 0, 1024)) != -1) {
					   sb.append(data,0, count);
				   }
				   ArrayList<String> rows = this.getRowsForStr(sb.toString(), width);
				   StringBuilder outp = new StringBuilder();
				   this.helperPrintRowsForStr(outp, rows, 0, true, width);
				   numRows = rows.size();
				   retval = outp.toString();
			   }
	                      
		   }
		   catch (Exception e2) {
			   
			//   return null;
		   }
		   finally {
			   if (inp != null) {
				   try {
				   inp.close();
				   }
				   catch (Exception e3) {
					   
				   }
			   }
			   inp = null;
		   }
		   return new Pair<String, Integer>(retval, numRows);
	}
	
	public void prepare(FrontPageInfo fpi, SessionManager session) {
		this.doGroupingRowMarker = fpi.textPrinterDoGroupingRowMarker;
		this.doHeaderRowMarker = fpi.textPrinterDoHeaderRowMarker;
		this.doIntermediareRowMarker = fpi.textPrinterDoIntermediareRowMarker;
		this.inBetweenColSeparator = fpi.textPrinterInBetweenColSeparator;
		this.rowEnder = fpi.textPrinterRowEnder;
		this.rowMarker = fpi.textPrinterRowMarker;
		this.rowStarter = fpi.textPrinterRowStarter;
		this.maxRowsPerPage = fpi.textMaxRowsPerPage;
		this.blankHeaderLine = fpi.textBlankHeaderLine;
		this.blankFooterLine = fpi.textBlankFooterLine;
		this.firstPageHeaderFile = fpi.textFirstPageHeaderFile;
		this.lastFooterFile = fpi.textLastPageFooterFile;
		this.innerPageHeaderFile = fpi.textInnerPageHeaderFile;
		this.refInnerPageHeader = fpi.refTextInnerPageHeader;
		this.innerPageFooterFile = fpi.textInnerPageFooterFile;
		this.session = session;
		ArrayList<DimConfigInfo> dciList = fpi.m_frontInfoList;
		this.numCharsInRow = this.rowEnder.length() + this.rowStarter.length();
		
		for (int i=0,is=dciList == null ? 0 : dciList.size();i<is;i++) {
			DimConfigInfo dci = dciList.get(i);
			if (!dci.m_hidden) {
				charWidth.add(dci.m_textPrintCharWidth);
				doTruncation.add(dci.m_textPrintTruncateToWidth);
				this.numCharsInRow += dci.m_textPrintCharWidth;
				this.numCols++;
				if (charWidth.size() > 1)
					this.numCharsInRow += this.inBetweenColSeparator.length();
				DimInfo dimInfo = dci.m_dimCalc != null ? dci.m_dimCalc.m_dimInfo : null;
				int algn = -1;
				if (dimInfo != null) {
					if (dimInfo.m_type == Cache.NUMBER_TYPE || dimInfo.m_type == Cache.INTEGER_TYPE)
						algn = 1;
				}
				this.allAlignment.add(algn);
			}
		}
		this.numCharsInRow += this.rowStarter.length() + this.rowEnder.length();
		
		this.firstPageHeaderFile = fpi.textFirstPageHeaderFile;
		this.lastFooterFile = fpi.textLastPageFooterFile;
		this.innerPageHeaderFile = fpi.textInnerPageHeaderFile;
		this.innerPageFooterFile = fpi.textInnerPageFooterFile;
		this.escCode = fpi.textEscCode;
		
		Pair<String, Integer> templateInfo = null;
		if (templateInfo == null || templateInfo.first == null) {
			templateInfo = readTemplate(this.firstPageHeaderFile, this.numCharsInRow);
		}
		this.firstPageHeaderText = templateInfo.first;
		this.firstPageHeaderLineCount = templateInfo.second;

		templateInfo = readTemplate(this.lastFooterFile, this.numCharsInRow);
		this.lastFooterText = templateInfo.first;
		this.lastFooterLineCount = templateInfo.second;

		templateInfo = readTemplate(this.innerPageHeaderFile, this.numCharsInRow);
		this.innerPageHeaderText = templateInfo.first;
		this.innerPageHeaderLineCount = templateInfo.second;

		
		templateInfo = readTemplate(this.innerPageFooterFile, this.numCharsInRow);
		this.innerPageFooterText = templateInfo.first;
		this.innerPageFooterLineCount = templateInfo.second;
		
	}
	private void printEscCode(StringBuilder sb) {
		printEscCode(sb, this.escCode);
	}
	
	private static void printEscCode(StringBuilder sb, String escCode) {
		if (escCode == null)
			escCode = "ESC0ESCg";
		String parts[] = escCode.split("ESC");
		for (int i=0,is=parts == null ? 0 : parts.length; i<is; i++) {
			String part = parts[i];
			if (part == null || part.length() == 0) 
				continue;
			sb.append((char)27);
			sb.append(part);
		}
	}

	private  void printSeparatorRow(StringBuilder sb) {
		for(int i=0;i<numCharsInRow;i++)
			sb.append(this.rowMarker);
		sb.append("\n");
	}
	
	
	
	private ArrayList<ArrayList<Triple<Integer, Integer, Integer>>> getWidthForMultiRowHeader(ArrayList<TR> rows) {
		//1st = for each header row
		//2nd for each entry in row, first = preWidth, second = width, third alignment; (alignment is centered if colspan > 1 else same as data type
		ArrayList<ArrayList<Pair<Integer, Integer>>> mappingOfCols = new ArrayList<ArrayList<Pair<Integer, Integer>>>();
		//rows correspond to cols in tbody and each entry tells what row,col of rows is it going to filled from
		for (int i=0,is=rows.size();i<is;i++) {
			ArrayList<Pair<Integer, Integer>> temp = new ArrayList<Pair<Integer, Integer>>();
			mappingOfCols.add(temp);
			for (int t1=0;t1<numCols;t1++)
				temp.add(new Pair<Integer, Integer>(-1,-1));			
		}
		for (int i=0,is=rows.size();i<is;i++) {
			TR row = rows.get(i);
			int colIndex = 0;
			int j=0;
			for (TD td : row.getRowData()) {
				if (td.getHidden())
					continue;
				
				ArrayList<Pair<Integer, Integer>> mappingOfCol = mappingOfCols.get(i);
				for (int cis=mappingOfCol.size(); colIndex<cis;colIndex++) {
					if (mappingOfCol.get(colIndex).first < 0)
						break;
				}
				
				int rowspan = td.getRowSpan();
				int colspan = td.getColSpan();
				if (colspan <= 0)
					colspan = 1;
				if (rowspan <= 0)
					rowspan = 1;
				
				int numNonHidden = 0;
				for (int t=0;t<colspan;t++) {
					if (colIndex+t >= this.charWidth.size())
						break;
					if (this.charWidth.get(t+colIndex) != 0)
						numNonHidden++;
				}
				
				for (int t=0;t<numNonHidden;t++) {
					for (int t2=0;t2<rowspan;t2++) {
						mappingOfCols.get(i+t2).set(t+colIndex, new Pair<Integer, Integer>(i,j));
					}
				}
				colIndex += numNonHidden;
				j++;
			}
		}
		ArrayList<ArrayList<Triple<Integer, Integer, Integer>>> retval = new ArrayList<ArrayList<Triple<Integer, Integer, Integer>>>();// first is the number of rows
		                                                                                                                                                                //, 1st of pair tells how much chars gap in between, 2nd tells the width, 3rd alignment
		for (int i=0,is=rows.size(); i<is; i++) {
			TR row = rows.get(i);
			ArrayList<Triple<Integer, Integer, Integer>> entry = new ArrayList<Triple<Integer, Integer, Integer>>();
			retval.add(entry);
			for (int j=0,js=row.getRowData().size(); j<js; j++) {
				if (!row.getRowData().get(j).getHidden())
					entry.add(new Triple<Integer, Integer, Integer>(0,0,-1));
			}				
		}
		
		for (int i=0,is=mappingOfCols.size(); i<is; i++) {			
			ArrayList<Pair<Integer, Integer>> mappingOfCol = mappingOfCols.get(i);
			int preWidth = 0;
			int colIndexInTableRow = 0;
			for (int j=0,js=mappingOfCol.size();j<js;) {
				if (mappingOfCol.get(j).first != i) {
					if (preWidth != 0)
						preWidth += this.inBetweenColSeparator.length();
					preWidth += this.charWidth.get(j);		
					j++;
					continue;
				}
				
				int width = 0;
				int cntSpn = 0;
				for (int t=j; t<js; t++) {
					if (mappingOfCol.get(t).equals(mappingOfCol.get(j))) {
						if (width != 0)
							width += this.inBetweenColSeparator.length();
						width += this.getCharWidth().get(t);
						cntSpn++;
					}
					else {
						break;
					}
				}
				
				int align = -1;
				if (cntSpn > 1)
					align = 0;
				else 
					align = this.allAlignment.get(j);
				retval.get(i).set(colIndexInTableRow, new Triple<Integer, Integer, Integer>(preWidth, width, align));
				j += cntSpn;
				colIndexInTableRow++;
				preWidth = 0;
			}
		}
		return retval;
	}
	
	private ArrayList<ArrayList<String>> helperGetRowsAndColsForRow(TR row, ArrayList<Triple<Integer, Integer, Integer>> optionalPreWidthWidthAlign, boolean noTruncate) {//retval: 1 is rows of string per col, 2 is alignment
		int colIndex = -1;
		ArrayList<ArrayList<String>> retval = new ArrayList<ArrayList<String>>();
		//ArrayList<Integer> alignment = new ArrayList<Integer>();
		for(TD col : row.getRowData()) {
			if (col.getHidden())
				continue;
			colIndex++;
			//int align = col.getAlignment();
			//if (Misc.isUndef(align)) {
			//	align = -1;
			//	int styleClassId = col.getClassId();
			//	if (Misc.isUndef(styleClassId))
			//		styleClassId = row.getClassId();
			//	if (styleClassId == 0 || styleClassId == 1)
			//		align = 0;
			//	else if (styleClassId == 3 || styleClassId == 4 || styleClassId == 5 || styleClassId == 6 )
			//		align = 1;
			//}
			//alignment.add(align);
			String toPrint = col.getContent();
			if (toPrint == null || toPrint.length() == 0) {
				toPrint = col.getDisplay();
			}
			
			if (toPrint == null)
				toPrint = Misc.emptyString;
			else
				toPrint = toPrint.replaceAll("&nbsp;", " ");
			int width =charWidth.get(colIndex);
			if (optionalPreWidthWidthAlign != null && optionalPreWidthWidthAlign.size() > colIndex)
				width = optionalPreWidthWidthAlign.get(colIndex).second;
			int numRowsRequired = !noTruncate && this.doTruncation.get(colIndex) != 0 ? 1 : (int) Math.ceil((double)toPrint.length()/(double)width);
			if (!noTruncate && this.doTruncation.get(colIndex) == -1 && toPrint.length() > width) {
				toPrint = toPrint.substring(toPrint.length()-width, toPrint.length());
			}
			int lolt = 0;
			int hilt = 0;
			for (int j=0;j<numRowsRequired;j++) {
				for (int k=retval.size(); k<=j;k++)
					retval.add(new ArrayList<String>());
				ArrayList<String> addInThisRow = retval.get(j);
				for (int k=addInThisRow.size();k<=colIndex;k++)
					addInThisRow.add(null);
				//int lolt = j*width;
				//int hilt = (j+1)*width;
				int gap = width;
				//hack .. break at natural position like space, . Doing for last but one row
				if (j == numRowsRequired-2) {
					if (toPrint.length() < 2*width+lolt) {
						int p1 = toPrint.lastIndexOf('.', lolt+width);
						if (p1 >= 0 && toPrint.length()-p1 <= width) {
							gap = p1-lolt;
						}
						else {
							p1 = toPrint.lastIndexOf(' ', lolt+width);
							if (p1 >= 0 && toPrint.length()-p1 <= width) {
								gap = p1-lolt;
							}
							else {
								p1 = toPrint.lastIndexOf(',', lolt+width);
								if (p1 >= 0 && toPrint.length()-p1 <= width) {
									gap = p1-lolt;
								}	
							}
						}
					}
				}
				hilt = lolt+gap;
				if (lolt >= toPrint.length())
					lolt = toPrint.length();
				if (hilt >= toPrint.length())
					hilt = toPrint.length();
				String toAdd = toPrint.substring(lolt, hilt);
				addInThisRow.set(colIndex, toAdd);
				lolt = hilt;
			}
		}
		return retval;//new Pair<ArrayList<ArrayList<String>>, ArrayList<Integer>>(retval, alignment);
	}
	
	private void helperPrintFinalRows(StringBuilder sb, ArrayList<ArrayList<String>> detailsToPrint, boolean toBold, ArrayList<Triple<Integer, Integer, Integer>> optionalPreWidthWidthAlign) {
		//dont know how to bold ... so do it later
		if (toBold) {
			sb.append((char)27).append('E');
		}
		ArrayList<ArrayList<String>> rowsToPrint = detailsToPrint;
		for (int i=0,is=rowsToPrint.size();i<is;i++) {
			ArrayList<String> rowToPrint = rowsToPrint.get(i);
			for (int j=0,js=rowToPrint.size(); j<js; j++) {
				
				String data = rowToPrint.get(j);
				if (data == null)
					data = Misc.emptyString;
				data = data.trim();
				if (Misc.nbspString.equals(data))
					data = "";
				int len = data.length();
				int align = optionalPreWidthWidthAlign == null ? this.allAlignment.get(j) : optionalPreWidthWidthAlign.get(j).third;
				if (Misc.isUndef(align))
					align = -1;
				int width =charWidth.get(j);
				int preWidth = 0;
				if (optionalPreWidthWidthAlign != null && optionalPreWidthWidthAlign.size() > j) {
					width = optionalPreWidthWidthAlign.get(j).second;
					preWidth  = optionalPreWidthWidthAlign.get(j).first;
				}
				if (j == 0) {
					sb.append(this.rowStarter);
				}
				else {
					sb.append(this.inBetweenColSeparator);
				}
				if (preWidth > 0) {
					for (int t2=0;t2<preWidth;t2++) {
						sb.append(" ");
					}
					sb.append(this.inBetweenColSeparator);
				}
				
				
				int rhsPadding = width - len;
				if (rhsPadding < 0)
					rhsPadding = 0;
				int lhsPadding = 0;
				if (align == 0) {
					lhsPadding = rhsPadding/2;
					rhsPadding = rhsPadding-lhsPadding;
				}
				if (align > 0) {
					lhsPadding = rhsPadding;
					rhsPadding = 0;
				}
				for (int t=0;t<lhsPadding;t++)
						sb.append(" ");
				sb.append(data);
				for (int t=0;t<rhsPadding;t++)
					sb.append(" ");
				if (j == rowToPrint.size()-1) {
					sb.append(this.rowEnder);
					sb.append("\n");
				}
			}//for each col
		}//for each constiuent row
		if (toBold) {
		   sb.append((char)27).append('F');
		}
	}
	
	private ArrayList<String> getRowsForStr(String toPrintCombo, int width) {//retval: 1 is rows of string per col, 2 is alignment
		ArrayList<String> retval = new ArrayList<String>();
		int colIndex = -1;
		String s[] = toPrintCombo.split("\n");
		for (int t0=0, t0s = s == null? 0 : s.length;t0<t0s; t0++) {
			String toPrint = s[t0];
			if (toPrint != null && toPrint.endsWith("\r"))
				toPrint.substring(toPrint.length()-1);
			if (toPrint != null && toPrint.startsWith("\r"))
				toPrint.substring(1, toPrint.length());
			if (toPrint == null || toPrint.length() == 0)
				toPrint = " ";//Misc.emptyString;
			int numRowsRequired = (int) Math.ceil((double)toPrint.length()/(double)width);
			for (int j=0;j<numRowsRequired;j++) {
				int lolt = j*width;
				int hilt = (j+1)*width;
				if (lolt >= toPrint.length())
					lolt = toPrint.length();
				if (hilt >= toPrint.length())
					hilt = toPrint.length();
				String toAdd = toPrint.substring(lolt, hilt);
				retval.add(toAdd);
			}
		}
		return retval;
	}
	
	private void helperPrintRowsForStr(StringBuilder sb, ArrayList<String> rowToPrint, int align, boolean toBold, int width) {
		//dont know how to bold ... so do it later
		if (toBold) {
			sb.append((char)27).append('E');
		}
		for (int j=0,js=rowToPrint.size(); j<js; j++) {
			
			String data = rowToPrint.get(j);
			if (data == null)
				data = Misc.emptyString;
			data = data.trim();
			if (Misc.nbspString.equals(data))
				data = " ";
			int len = data.length();
			if (Misc.isUndef(align))
				align = -1;
			sb.append(this.rowStarter);
			int rhsPadding = width - len;
			if (rhsPadding < 0)
				rhsPadding = 0;
			int lhsPadding = 0;
			if (align == 0) {
				lhsPadding = rhsPadding/2;
				rhsPadding = rhsPadding-lhsPadding;
			}
			if (align > 0) {
				lhsPadding = rhsPadding;
				rhsPadding = 0;
			}
			for (int t=0;t<lhsPadding;t++)
					sb.append(" ");
			sb.append(data);
			for (int t=0;t<rhsPadding;t++)
				sb.append(" ");
			sb.append(this.rowEnder);
			sb.append("\n");
		}//for each col
		if (toBold) {
			sb.append((char)27).append('F');
		}
	}
	public  void printTableStart(StringBuilder sb) {
	}
	
	
	public void printHeaderRow(TR row, StringBuilder sb, SessionManager _session, ArrayList<Triple<Integer, Integer, Integer>> preWidthWidthAlign) {
		try {
			int cnt = 0;
			int stMarker = sb.length();
			if (isFirstHeaderRow) {
				printEscCode(sb);
				stMarker = sb.length();
				for (int t1=0;t1<this.blankHeaderLine;t1++) {
					sb.append("\n");
					cnt++;
				}
				int hwwidth = this.numCharsInRow-this.rowStarter.length()-this.rowEnder.length();
				//1st print 1st PageHeader
				//TODO - for separate title page header
				if (!tempInitedProjectNameFromSession) {
					String projName = session == null ? null : session.getAttribute("_project_name");
					if (projName != null && projName.length() > 0) {
						String refText = this.refInnerPageHeader;
						if (refText == null || refText.length() == 0)
							refText = "SECL %project_name% Project";
						refText = refText.replaceAll("%project_name%", projName);
						 ArrayList<String> temprows = this.getRowsForStr(refText, this.numCharsInRow);
						StringBuilder tempsb = new StringBuilder();
						this.helperPrintRowsForStr(tempsb, temprows, 0, true, this.numCharsInRow);
						this.innerPageHeaderText = tempsb.toString();
						this.innerPageHeaderLineCount = temprows.size();
						tempInitedProjectNameFromSession = true;
					}
				}
				
			
				if (this.innerPageHeaderLineCount > 0) {
					sb.append(this.innerPageHeaderText);
					sb.append("\n");
					cnt += this.innerPageHeaderLineCount;
				}
					
				ArrayList<String> headerComp = this.getRowsForStr(this.addnlHeaderLines.toString(), hwwidth );
				this.helperPrintRowsForStr(sb, headerComp, 0, true, hwwidth);
				cnt += headerComp.size();
				isFirstHeaderRow = false;
			}
			if (!this.lastRowHadRowSeparator && this.doHeaderRowMarker) {
				this.printSeparatorRow(sb);
				cnt++;
			}
			ArrayList<ArrayList<String>> rowsToPrintInfo = helperGetRowsAndColsForRow(row, preWidthWidthAlign, true);
			cnt += rowsToPrintInfo.size();
			this.helperPrintFinalRows(sb, rowsToPrintInfo, true, preWidthWidthAlign);

			if (this.doHeaderRowMarker) {
				this.printSeparatorRow(sb);
				cnt++;
				this.lastRowHadRowSeparator = true;
			}
			else {
				this.lastRowHadRowSeparator = false;
			}
			int enMarker = sb.length();
			this.headerRows.append(sb.substring(stMarker, enMarker));
			this.headerRowsCount += cnt;
			this.numLinesOnCurrPage += cnt;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public  void printHeader(ArrayList<TR> header,StringBuilder sb,SessionManager _session){
		ArrayList<ArrayList<Triple<Integer, Integer, Integer>>> preWidthWidthAlign = this.getWidthForMultiRowHeader(header);
		
		if (header != null){
			try{
				int i=0;
				for (TR row : header){
					printHeaderRow(row, sb, _session, preWidthWidthAlign.get(i++));
				}
			}catch(Exception e){
				e.printStackTrace();		
			}
			int dbg=1;
		}
	}
	public void printEndOfPage(StringBuilder sb) {
		printEndOfPage(sb, false);
			
	}
	public void printEndOfPage(StringBuilder sb, boolean endOfFile) {
		sb.append("\n\n");
		String endOfRep = "End of Report:  ";
		for (int i1=0;i1<this.numCharsInRow-(endOfFile ? 20+endOfRep.length():20);i1++) {
			sb.append(" ");
		}
		sb.append(endOfFile ? endOfRep : "").append("Page ").append(++this.pageNumber).append("\n");
		if (!endOfFile) {
			if (this.innerPageFooterLineCount > 0) {
				sb.append("\n");
				sb.append(this.innerPageFooterText);
				sb.append("\n");
			}
		}
		else {
			if (this.lastFooterLineCount > 0) {
				sb.append("\n");
				sb.append(this.lastFooterText);
				sb.append("\n");
			}
		}
		sb.append("\f");	
	}
	public  void printBodyRow(TR row, StringBuilder sb, SessionManager _session, boolean isLastRow) {
		try {
			boolean pageBreakRow = row.getClassId() == 16;
			if (pageBreakRow) {
				//HACK todo print header
				return;
			}
			
			boolean hasGrouping = false;
			for(TD col : row.getRowData()){
				if (col.getDoGroup()) {
					hasGrouping = true;
					break;
				}
			}
			int cntNeededToPrint = 0;
			if (hasGrouping && this.doGroupingRowMarker) {
				if (!this.lastRowHadRowSeparator) 
					cntNeededToPrint++;
				cntNeededToPrint++;
			}
			if (!hasGrouping && this.doIntermediareRowMarker) {
				if (!this.lastRowHadRowSeparator)
					cntNeededToPrint++;
				cntNeededToPrint++;
			}
			ArrayList<ArrayList<String>> rowsToPrintInfo = helperGetRowsAndColsForRow(row, null, false);
			cntNeededToPrint += rowsToPrintInfo.size();
			//check if we need to break
			int numFooterLines = this.blankFooterLine-3 /*for \n\n page number*/+3 + this.innerPageFooterLineCount;
			if (isLastRow) {
				numFooterLines = this.blankFooterLine-3 /*for \n\n page number*/+3 + this.lastFooterLineCount;
			}
			if ((this.numLinesOnCurrPage+cntNeededToPrint+numFooterLines) > this.maxRowsPerPage && (this.numLinesOnCurrPage > (this.headerRowsCount))) {
				//for (int t1=0,t1s=this.maxRowsPerPage-this.numLinesOnCurrPage+this.blankFooterLine; t1<t1s;t1++) {
				//	sb.append("\n");					
				//}
				printEndOfPage(sb);
				this.numLinesOnCurrPage = 0;
				this.lastRowHadRowSeparator = false;
				sb.append(this.headerRows);
				this.numLinesOnCurrPage += this.headerRowsCount;				
			}

			if (hasGrouping && this.doGroupingRowMarker) {
				if (!this.lastRowHadRowSeparator) { 
					this.printSeparatorRow(sb);
					this.numLinesOnCurrPage++;
				}
			}
			if (!hasGrouping && this.doIntermediareRowMarker) {
				if (!this.lastRowHadRowSeparator) { 
					this.printSeparatorRow(sb);
					this.numLinesOnCurrPage++;
				}
			}
			this.helperPrintFinalRows(sb, rowsToPrintInfo, hasGrouping, null);
			this.numLinesOnCurrPage += rowsToPrintInfo.size();
			if (hasGrouping) {
				if (this.doGroupingRowMarker) {
					this.printSeparatorRow(sb);
					this.numLinesOnCurrPage++;
					this.lastRowHadRowSeparator = true;
				}
				else {
					this.lastRowHadRowSeparator = false;
				}	
			}
			else {
				if (this.doIntermediareRowMarker) {
					this.printSeparatorRow(sb);
					this.numLinesOnCurrPage++;
					this.lastRowHadRowSeparator = true;
				}
				else {
					this.lastRowHadRowSeparator = false;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private  void printBody(ArrayList<TR> header,StringBuilder sb, SessionManager _session){
		if (header != null){
			try{
				int i=0;
				for (TR row : header){
					
					printBodyRow(row, sb, _session, i == header.size()-1);
					i++;
				}
				this.printEndOfPage(sb,true);
			}catch(Exception e){
				e.printStackTrace();		
			}
		}
	}
	
	
	

	public char getRowMarker() {
		return rowMarker;
	}



	public void setRowMarker(char rowMarker) {
		this.rowMarker = rowMarker;
	}



	public boolean isDoHeaderRowMarker() {
		return doHeaderRowMarker;
	}



	public void setDoHeaderRowMarker(boolean doHeaderRowMarker) {
		this.doHeaderRowMarker = doHeaderRowMarker;
	}



	public boolean isDoIntermediareRowMarker() {
		return doIntermediareRowMarker;
	}



	public void setDoIntermediareRowMarker(boolean doIntermediareRowMarker) {
		this.doIntermediareRowMarker = doIntermediareRowMarker;
	}



	public boolean isDoGroupingRowMarker() {
		return doGroupingRowMarker;
	}



	public void setDoGroupingRowMarker(boolean doGroupingRowMarker) {
		this.doGroupingRowMarker = doGroupingRowMarker;
	}



	public ArrayList<Integer> getCharWidth() {
		return charWidth;
	}



	public void setCharWidth(ArrayList<Integer> charWidth) {
		this.charWidth = charWidth;
	}



	public String getInBetweenColSeparator() {
		return inBetweenColSeparator;
	}



	public void setInBetweenColSeparator(String inBetweenColSeparator) {
		this.inBetweenColSeparator = inBetweenColSeparator;
	}



	public String getRowStarter() {
		return rowStarter;
	}



	public void setRowStarter(String rowStarter) {
		this.rowStarter = rowStarter;
	}



	public String getRowEnder() {
		return rowEnder;
	}



	public void setRowEnder(String rowEnder) {
		this.rowEnder = rowEnder;
	}

	public int getMaxRowsPerPage() {
		return maxRowsPerPage;
	}

	public void setMaxRowsPerPage(int maxRowsPerPage) {
		this.maxRowsPerPage = maxRowsPerPage;
	}

	public int getBlankHeaderLine() {
		return blankHeaderLine;
	}

	public void setBlankHeaderLine(int blankHeaderLine) {
		this.blankHeaderLine = blankHeaderLine;
	}

	public int getBlankFooterLine() {
		return blankFooterLine;
	}

	public void setBlankFooterLine(int blankFooterLine) {
		this.blankFooterLine = blankFooterLine;
	}

	public StringBuilder getAddnlHeaderLines() {
		return addnlHeaderLines;
	}

	public void setAddnlHeaderLines(StringBuilder addnlHeaderLines) {
		this.addnlHeaderLines = addnlHeaderLines;
	}

	public void printHtmlTable(Table table,StringBuilder sb, SessionManager _session){
		printHtmlTable(table,sb, _session, false);
	}
	public void printHtmlTable(Table table,StringBuilder sb, SessionManager _session, boolean doPlainTable){
		printHeader(table.getHeader(),sb,_session);
		printBody(table.getBody(),sb, _session);
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
}
