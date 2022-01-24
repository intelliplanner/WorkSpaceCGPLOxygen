package com.ipssi.reporting.trip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;

public class HtmlGenerator {
	public static String styleWithAdornment = " style='margin-left:15px' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'";
	public static String styleWithoutAdornment = " style='page-break-before:always' border='0' cellspacing='2' cellpadding='3' ";
	
	private static final String hidden = " style='display:none' ";	
	public static void printTableStart(StringBuilder sb, SessionManager _session) {
		printTableStart(sb, _session, styleWithAdornment);
	}
	
	public static void printTableStart(StringBuilder sb, SessionManager _session, String style) {
		printTableStart(sb, _session, style, "DATA_TABLE", false, false);
	}

	public static void printTableStart(StringBuilder sb, SessionManager _session, String style, String id, boolean asJS, boolean noId) {
		printTableStart(sb, _session, style, id, asJS, noId, false);
	}
	public static void printTableStart(StringBuilder sb, SessionManager _session, String style, String id, boolean asJS, boolean noId, boolean doCheckMandatory) {
		if (noId) {
			sb.append("<table ").append(style == null ? "" : style);
			if (doCheckMandatory) {
				sb.append(" _mand=\"1\" ");
			}
			sb.append(">");
		}
		else {
			if (id == null || id.length() == 0)
				id = "DATA_TABLE";
			sb.append("<table ").append(asJS ? "_js" : "ID").append("='").append(id).append("' ").append(style == null ? "" : style);
			if (doCheckMandatory) {
				sb.append(" _mand=\"1\" ");
			}
			sb.append(">");
		}
	}
	public static void printHtmlTable(Table table,StringBuilder sb, SessionManager _session){
		printHtmlTable(table,sb, _session, false);
	}
	public static void printHtmlTable(Table table,StringBuilder sb, SessionManager _session, boolean doPlainTable){
		printTableStart(sb, _session, doPlainTable ? styleWithoutAdornment : styleWithAdornment);
		printHeader(table.getHeader(),sb,_session);
		printBody(table.getBody(),sb, _session);
		sb.append("</table>");
	}
	public static void printHeaderRow(TR row, StringBuilder sb, SessionManager _session) {
		try {
			sb.append(row.getId() != null? "<tr id='"+row.getId()+"' >": "<tr>");
			for(TD col : row.getRowData()){
				sb.append("<td");
				if (col.getRowSpan() > 1) {
					sb.append(" rowspan='")
					.append(col.getRowSpan())
					.append("'");
				}
				if (col.getColSpan() > 1) {
					sb.append(" colspan='")
					.append(col.getColSpan())
					.append("'");
				}
				sb.append(" dt_type='").append(getDataType(col.getContentType(),_session)).append("' ")
				.append(col.getHidden()? hidden : "")
				.append(" class='").append(!Misc.isUndef(col.getClassId()) ? CssClassDefinition.getHtmlCssClass(col.getClassId()) : CssClassDefinition.getHtmlCssClass(row.getClassId()))
				.append(Misc.isUndef(col.getAlignment()) ? "" : col.getAlignment() == -1 ? " align='left'" : col.getAlignment() == 0 ?" align='center'" : " align='right'")
				.append("'>")
				;
				if (col.getDisplay() != null && col.getDisplay().length() != 0) {
					sb.append(col.getDisplay());
				}
				else if (col.getLinkAPart() != null && col.getLinkAPart().length() != 0) {
					sb.append(col.getLinkAPart()).append(col.getContent() == null ? "&nbsp;" : col.getContent()).append("</a>");
				}
				else {
					sb.append(col.getContent());
				}
				
				sb.append("</td>")
				;
			}
			sb.append("</tr>");	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void printHeader(ArrayList<TR> header,StringBuilder sb,SessionManager _session){
		if (header != null){
			try{
				sb.append("<thead>");
				for (TR row : header){
					printHeaderRow(row, sb, _session);
				}
				sb.append("</thead>");
			}catch(Exception e){
				e.printStackTrace();		
			}
		}
	}
	public static void printBodyRow(TR row, StringBuilder sb, SessionManager _session) {
		printBodyRow(row, sb, _session, true);
	}
	private static void printBodyRow(TR row, StringBuilder sb, SessionManager _session, boolean printClosing) {
		try {
			boolean pageBreakRow = row.getClassId() == 16;
			if (pageBreakRow) {
				//sb.append("<!-- pagebreak -->");
				sb.append("</TABLE>");
				printTableStart(sb, _session, true ? styleWithoutAdornment : styleWithAdornment);//HACK for row break
				//HACK todo print header
				return;
			}
			sb.append(row.getId() != null? "<tr id='"+row.getId()+"' " : "<tr ");
			
			if (row.getClassId() > 0)
				sb.append("class='"+CssClassDefinition.getHtmlCssClass(row.getClassId())+"' ");
			sb.append(" >");
			for(TD col : row.getRowData()){
				sb.append("<td valign='top' ");
				if (col.getRowSpan() > 1) {
					sb.append(" rowspan='")
					.append(col.getRowSpan())
					.append("'");
				}
				if (col.getColSpan() > 1) {
					sb.append(" colspan='")
					.append(col.getColSpan())
					.append("'");
				}
				if (!Misc.isUndef(col.getClassId())) {
				sb.append(" class='")
				.append(CssClassDefinition.getHtmlCssClass(col.getClassId()));
				sb.append("'");
				}
				
				sb.append(col.getHidden()? hidden : "")
				.append(Misc.isUndef(col.getAlignment()) ? "" : col.getAlignment() == -1 ? "style='text-align:left'" : col.getAlignment() == 0 ?" style='text-align:center'" : " style='text-align:right'")
				.append(">")
				;
				if (col.getNestedTable() != null) {
					HtmlGenerator.printHtmlTable(col.getNestedTable(), sb, _session);
				}
				else {
					if (col.getDisplay() != null && col.getDisplay().length() != 0) {
						sb.append(col.getDisplay());
					}
					else if (col.getLinkAPart() != null && col.getLinkAPart().length() != 0) {
						sb.append(col.getLinkAPart()).append(col.getContent() == null ? "&nbsp;" : col.getContent()).append("</a>");
					}
					else {
						sb.append(col.getContent());
					}
				}
				if (printClosing)
					sb.append("</td>")
				;
			}
			sb.append("</tr>");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void printBody(ArrayList<TR> header,StringBuilder sb, SessionManager _session){
		if (header != null){
			try{
				for (TR row : header){
					printBodyRow(row, sb, _session);
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
	public static void printTransposedHTMLTable(StringBuilder sb,Table table) {
		ArrayList<TR> header = table.getHeader();
		ArrayList<TR> body = table.getBody();
		int headerRowSize = header.size();
		int skipColumnCount = 0;
		int startRow = 0;
		int[] index = new int[headerRowSize];
		for(int i=0;i<headerRowSize;i++)
			index[i] = 0;
		boolean rowspan = false;
		TD col = null;
		TR row = null;
		if (header != null){
			try{
				for(TR tr : body){
					header.add(tr);
				}
				sb.append("<table ID='DATA_TABLE' border='1' cellspacing='0' cellpadding='3' bordercolor='#003366'>");
				for(int i=0 ;i<header.get(headerRowSize).getRowData().size();i++){
					sb.append("<tr>");
					for(int j=startRow; j<header.size();j++)//j change inside loop due to rowspan
					{   
						row = header.get(j);
						if (i >= row.getRowData().size() || j<headerRowSize){
							if (skipColumnCount > 0){
								col = row.getRowData().get(index[j]++);
								skipColumnCount--;
							}
							else if (j==0){
								col = row.getRowData().get(index[j]++);
							}
							else{
								continue;
							}
						}
						else
							col = row.getRowData().get(i);
						if(col.getColSpan() > 1){
							skipColumnCount = col.getColSpan();
							rowspan = true;
							startRow = j+1;
						}
						sb.append("<td")
						.append(" rowspan='")
						.append(col.getColSpan())
						.append("' colspan='")
						.append(col.getRowSpan())
						.append("'")
						.append(col.getHidden()? hidden : "")
						.append(" class='").append(!Misc.isUndef(col.getClassId()) ? CssClassDefinition.getHtmlCssClass(col.getClassId()) : CssClassDefinition.getHtmlCssClass(row.getClassId()))
						.append(Misc.isUndef(col.getAlignment()) ? "" : col.getAlignment() == -1 ? " align='left'" : col.getAlignment() == 0 ?" align='center'" : " align='right'")
						.append("'>")
						;
						if (col.getDisplay() != null && col.getDisplay().length() != 0) {
							sb.append(col.getDisplay());
						}
						else if (col.getLinkAPart() != null && col.getLinkAPart().length() != 0) {
							sb.append(col.getLinkAPart()).append(col.getContent() == null ? "&nbsp;" : col.getContent()).append("</a>");
						}
						else {
							sb.append(col.getContent());
						}
						sb.append("</td>")
						;
						j = j+col.getRowSpan()-1;
					}
					if(skipColumnCount == 0)
						startRow = 0;
					sb.append("</tr>");
				}
				sb.append("</table>");
			}catch(Exception e){
				e.printStackTrace();		
			}
		}
	}
	public static void postProcessTransit(Table table,SessionManager _session){
		//vehicle - 9002
		//event_start - 20146
		//event_stop - 20147
		//attribute_id - 20571
		//attribute- 20572
		//event_dur - 20574
		//state - 20573
		//start location -20155
		//start lat - 20143
		//start lon - 20142
		//end location - 20156
		//end lat - 20145
		//end lon - 20144
        ArrayList<Integer> startColLookUp = new ArrayList<Integer>(Arrays.asList(20146,20155,20143,20142));
        ArrayList<Integer> endColLookUp = new ArrayList<Integer>(Arrays.asList(20147,20156,20145,20144));
        ArrayList<TR> body = table.getBody();
		ArrayList<TR> tempBody = new ArrayList<TR>();
		TR currentRow = null;
		TR previousRow = null;
		TR tempRow = null;
		TD tempCol = null;
		Date currentStart = null;
		Date currentEnd = null;
		Date previousStart = null;
		Date previousEnd = null;
		double previousAttr = 0;
		double currentAttr = 0;
		boolean do_merge = false;
		long dur = 0;
		double printDist = 0.0;
		int preVehicleId = Misc.getUndefInt();
		int currVehicleId = Misc.getUndefInt();
		SimpleDateFormat sdf = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT_HHMM);
		if (body != null){
			try{
				previousRow = body.get(0);
				//tempBody.add(previousRow);
				tempRow = new TR();
				for(int j=0;j<previousRow.getRowData().size();j++){
					tempCol = new TD();
					tempCol.setId(previousRow.getRowData().get(j).getId());
					tempCol.setClassId(previousRow.getRowData().get(j).getClassId());
					tempCol.setColSpan(previousRow.getRowData().get(j).getColSpan());
					tempCol.setRowSpan(previousRow.getRowData().get(j).getRowSpan());
					tempCol.setHidden(previousRow.getRowData().get(j).getHidden());
					tempCol.setAlignment(previousRow.getRowData().get(j).getAlignment());
					tempCol.setContentType(previousRow.getRowData().get(j).getContentType());
					tempCol.setContent(previousRow.getRowData().get(j).getContent());
					tempCol.setDisplay(previousRow.getRowData().get(j).getDisplay());
					tempCol.setLinkAPart(previousRow.getRowData().get(j).getLinkAPart());
					tempRow.setRowData(tempCol);
				}
				if(!Misc.isUndef(table.getColumnIndexById(20574)))
					tempRow.get(table.getColumnIndexById(20574)).setContent(getTimeStrFromMin(Misc.getParamAsLong(tempRow.get(table.getColumnIndexById(20574)).getContent(),0)));
				tempBody.add(tempRow);
				for (int i=0; i<body.size(); i++){
					if (i > 0 && !do_merge)
						previousRow = body.get(i-1);
					if(i == 0)
						continue;
					currentRow = body.get(i);
					if(!Misc.isUndef(table.getColumnIndexById(20146)) && !Misc.isUndef(table.getColumnIndexById(20147))){
					currentStart = !currentRow.get(table.getColumnIndexById(20146)).getContent().equalsIgnoreCase("&nbsp;") ? sdf.parse(currentRow.get(table.getColumnIndexById(20146)).getContent()) : new Date(System.currentTimeMillis());
					currentEnd = !currentRow.get(table.getColumnIndexById(20147)).getContent().equalsIgnoreCase("&nbsp;") ? sdf.parse(currentRow.get(table.getColumnIndexById(20147)).getContent()) : new Date(System.currentTimeMillis());
					previousStart = !previousRow.get(table.getColumnIndexById(20146)).getContent().equalsIgnoreCase("&nbsp;") ? sdf.parse(previousRow.get(table.getColumnIndexById(20146)).getContent()) : new Date(System.currentTimeMillis());
					previousEnd = !previousRow.get(table.getColumnIndexById(20147)).getContent().equalsIgnoreCase("&nbsp;") ? sdf.parse(previousRow.get(table.getColumnIndexById(20147)).getContent()) : new Date(System.currentTimeMillis());
					if(!Misc.isUndef(table.getColumnIndexById(20572))){
						currentAttr = Misc.getParamAsDouble(currentRow.get(table.getColumnIndexById(20572)).getContent());
						previousAttr = Misc.getParamAsDouble(previousRow.get(table.getColumnIndexById(20572)).getContent());
					 }
					if(!Misc.isUndef(table.getColumnIndexById(20274))){
						preVehicleId = Misc.getParamAsInt(currentRow.get(table.getColumnIndexById(20274)).getContent());
						currVehicleId = Misc.getParamAsInt(previousRow.get(table.getColumnIndexById(20274)).getContent());
					}
					}
					else{
						break;
					}
					if(preVehicleId != currVehicleId){
						if(!Misc.isUndef(table.getColumnIndexById(20572)))
							previousRow.get(table.getColumnIndexById(20572)).setContent("0.00");
						do_merge = false;
						tempRow = new TR();
						for(int j=0;j<currentRow.getRowData().size();j++){
							tempCol = new TD();
							tempCol.setId(currentRow.getRowData().get(j).getId());
							tempCol.setClassId(currentRow.getRowData().get(j).getClassId());
							tempCol.setColSpan(currentRow.getRowData().get(j).getColSpan());
							tempCol.setRowSpan(currentRow.getRowData().get(j).getRowSpan());
							tempCol.setHidden(currentRow.getRowData().get(j).getHidden());
							tempCol.setAlignment(currentRow.getRowData().get(j).getAlignment());
							tempCol.setContentType(currentRow.getRowData().get(j).getContentType());
							tempCol.setContent(currentRow.getRowData().get(j).getContent());
							tempCol.setDisplay(currentRow.getRowData().get(j).getDisplay());
							tempCol.setLinkAPart(currentRow.getRowData().get(j).getLinkAPart());
							tempRow.setRowData(tempCol);
						}
						if(!Misc.isUndef(table.getColumnIndexById(20574)))
							tempRow.get(table.getColumnIndexById(20574)).setContent(getTimeStrFromMin(Misc.getParamAsLong(tempRow.get(table.getColumnIndexById(20574)).getContent(),0)));
						tempRow.get(table.getColumnIndexById(20572)).setContent("0.00");
						tempBody.add(tempRow);
						continue;
					}
					else if(currentStart.getTime() == previousEnd.getTime()){
						if(!Misc.isUndef(table.getColumnIndexById(20574))){
						dur = getMinFromTimeStr(currentRow.get(table.getColumnIndexById(20574)).getContent()) + getMinFromTimeStr(previousRow.get(table.getColumnIndexById(20574)).getContent());
						previousRow.get(table.getColumnIndexById(20574)).setContent(getTimeStrFromMin(dur));
						}
						//previousRow.get(table.getColumnIndexById(20146)).setContent(sdf.format(previousStart));
						//previousRow.get(table.getColumnIndexById(20147)).setContent(sdf.format(currentEnd));
						for(Integer index : endColLookUp){
							if(!Misc.isUndef(table.getColumnIndexById(index))){
								previousRow.get(table.getColumnIndexById(index)).setContent(currentRow.get(table.getColumnIndexById(index)).getContent());
							}
						}
						do_merge = true;
						//merge
					}
					else if(currentStart.getTime() > previousStart.getTime() && currentStart.getTime() < previousEnd.getTime()){
						do_merge = true;
						continue;
					}
					else if(currentStart.getTime() >= previousStart.getTime() && currentStart.getTime() <= previousEnd.getTime()){
						long start = !previousRow.get(table.getColumnIndexById(20146)).getContent().equalsIgnoreCase("&nbsp;") ? sdf.parse(previousRow.get(table.getColumnIndexById(20146)).getContent()).getTime() : System.currentTimeMillis();
						//previousRow.get(table.getColumnIndexById(20574)).setContent(getTimeStrFromMin(dur));						
						//previousRow.get(table.getColumnIndexById(20146)).setContent(sdf.format(previousStart));
						//previousRow.get(table.getColumnIndexById(20147)).setContent(sdf.format(new Date(Math.max(previousEnd.getTime(), currentEnd.getTime()))));
						if(previousEnd.getTime() <= currentEnd.getTime()){
							for(Integer index : endColLookUp){
								if(!Misc.isUndef(table.getColumnIndexById(index))){
									previousRow.get(table.getColumnIndexById(index)).setContent(currentRow.get(table.getColumnIndexById(index)).getContent());
								}
							}
						}
						long end = !previousRow.get(table.getColumnIndexById(20147)).getContent().equalsIgnoreCase("&nbsp;") ? sdf.parse(previousRow.get(table.getColumnIndexById(20147)).getContent()).getTime() : System.currentTimeMillis();
						if(!Misc.isUndef(table.getColumnIndexById(20574))){
							dur = Math.max(getMinFromTimeStr(currentRow.get(table.getColumnIndexById(20574)).getContent()) , getMinFromTimeStr(previousRow.get(table.getColumnIndexById(20574)).getContent()));
							dur = (end - start)/(1000*60);
							previousRow.get(table.getColumnIndexById(20574)).setContent(getTimeStrFromMin(dur));
						}
						do_merge = true;
						//merge
					}
					else {
						do_merge = false;
						tempRow = new TR();
						for(int j=0;j<currentRow.getRowData().size();j++){
							tempCol = new TD();
							tempCol.setId(currentRow.getRowData().get(j).getId());
							tempCol.setClassId(currentRow.getRowData().get(j).getClassId());
							tempCol.setColSpan(currentRow.getRowData().get(j).getColSpan());
							tempCol.setRowSpan(currentRow.getRowData().get(j).getRowSpan());
							tempCol.setHidden(currentRow.getRowData().get(j).getHidden());
							tempCol.setAlignment(currentRow.getRowData().get(j).getAlignment());
							tempCol.setContentType(currentRow.getRowData().get(j).getContentType());
							tempCol.setContent(currentRow.getRowData().get(j).getContent());
							tempCol.setDisplay(currentRow.getRowData().get(j).getDisplay());
							tempCol.setLinkAPart(currentRow.getRowData().get(j).getLinkAPart());
							tempRow.setRowData(tempCol);
						}
						// start = previousrow.end and end = currentrow.start
				        for(Integer index : startColLookUp){
								if(!Misc.isUndef(table.getColumnIndexById(index))){
									if(index == 20146 && !Misc.isUndef(table.getColumnIndexById(20147)))
										tempRow.get(table.getColumnIndexById(index)).setContent(previousRow.get(table.getColumnIndexById(20147)).getContent());
									else if(index == 20155 && !Misc.isUndef(table.getColumnIndexById(20156)))
										tempRow.get(table.getColumnIndexById(index)).setContent(previousRow.get(table.getColumnIndexById(20156)).getContent());
									else if(index == 20143 && !Misc.isUndef(table.getColumnIndexById(20145)))
										tempRow.get(table.getColumnIndexById(index)).setContent(previousRow.get(table.getColumnIndexById(20145)).getContent());
									else if(index == 20142 && !Misc.isUndef(table.getColumnIndexById(20144)))
										tempRow.get(table.getColumnIndexById(index)).setContent(previousRow.get(table.getColumnIndexById(20144)).getContent());
								}
						}
				        for(Integer index : endColLookUp){
							if(!Misc.isUndef(table.getColumnIndexById(index))){
								if(index == 20147 && !Misc.isUndef(table.getColumnIndexById(20146)))
									tempRow.get(table.getColumnIndexById(index)).setContent(currentRow.get(table.getColumnIndexById(20146)).getContent());
								else if(index == 20156 && !Misc.isUndef(table.getColumnIndexById(20155)))
									tempRow.get(table.getColumnIndexById(index)).setContent(currentRow.get(table.getColumnIndexById(20155)).getContent());
								else if(index == 20145 && !Misc.isUndef(table.getColumnIndexById(20143)))
									tempRow.get(table.getColumnIndexById(index)).setContent(currentRow.get(table.getColumnIndexById(20143)).getContent());
								else if(index == 20144 && !Misc.isUndef(table.getColumnIndexById(20142)))
									tempRow.get(table.getColumnIndexById(index)).setContent(currentRow.get(table.getColumnIndexById(20142)).getContent());
							}
				        }
						//Date start = sdf.parse(previousRow.get(table.getColumnIndexById(20147)).getContent());
						//Date end = sdf.parse(currentRow.get(table.getColumnIndexById(20146)).getContent());
				        if(!Misc.isUndef(table.getColumnIndexById(20574))){
							dur = (currentStart.getTime()-previousEnd.getTime())/(1000*60);
							tempRow.get(table.getColumnIndexById(20574)).setContent(getTimeStrFromMin(dur));
						}
					    double dist = currentAttr - previousAttr;
					    //dist = dist > 0 ? dist : 0;
						dist = (dur > 0) && (dist/dur*60) <= 100 && dist > 0? dist : 0;
						/*tempRow.get(table.getColumnIndexById(20146)).setContent(sdf.format(start));
						tempRow.get(table.getColumnIndexById(20147)).setContent(sdf.format(end));*/
						if(!Misc.isUndef(table.getColumnIndexById(20572))){
							printDist += dist;
							previousRow.get(table.getColumnIndexById(20572)).setContent("0.00");
							tempRow.get(table.getColumnIndexById(20572)).setContent(String.format("%1$,.2f", dist));
						}
						if(!Misc.isUndef(table.getColumnIndexById(20574)))
							tempRow.get(table.getColumnIndexById(20574)).setContent(getTimeStrFromMin(dur));
						if(!Misc.isUndef(table.getColumnIndexById(20573)))
							tempRow.get(table.getColumnIndexById(20573)).setContent("Transit");
						tempBody.add(tempRow);
						if(i>0 ){
							if(!Misc.isUndef(table.getColumnIndexById(20574)))
								currentRow.get(table.getColumnIndexById(20574)).setContent(getTimeStrFromMin(Misc.getParamAsLong(currentRow.get(table.getColumnIndexById(20574)).getContent(),0)));
						    tempBody.add(currentRow);
						}
						if(i == (body.size()-1) && !Misc.isUndef(table.getColumnIndexById(20572)))
							currentRow.get(table.getColumnIndexById(20572)).setContent("0.00");
					}
			}
			}catch(Exception e){
				e.printStackTrace();		
			}
			if(tempBody.size() > 0){
				if(!Misc.isUndef(table.getColumnIndexById(20572)))
					tempBody.get(tempBody.size()-1).get(table.getColumnIndexById(20572)).setContent("0.00");
				table.setBody(tempBody);
				System.out.println(printDist);
			}
		}
	}
	public static String getTimeStrFromMin(long interval){
		int hr = (int) (interval/60);
		int min = (int) (interval - hr*60);
		int days = hr/24;
		String intervalStr = "";
		hr = hr - days*24;
		if (hr == 0 && days == 0)
			intervalStr = Integer.toString(min)+"m";
		else if (days == 0) {
			intervalStr = Integer.toString(hr)+"h:"+Integer.toString(min)+"m";
		}
		else {
			intervalStr = Integer.toString(days)+"d:"+Integer.toString(hr)+"h:"+Integer.toString(min)+"m";
		}
		return intervalStr; 
	}
	public static long getMinFromTimeStr(String intervalStr){
		String dayStr = intervalStr.indexOf("d") > -1 ? intervalStr.split("d")[0] : "0";
		String hrStr = intervalStr.indexOf("h") > -1   ?  (intervalStr.indexOf("d") > -1 ? intervalStr.split("d")[1].split("h")[0] : intervalStr.split("h")[0] ): "0";
		String minStr = intervalStr.indexOf("m") > -1   ?  (intervalStr.indexOf("h") > -1 ? (intervalStr.indexOf("d") > -1 ? intervalStr.split("d")[1].split("h")[1].split("m")[0] : intervalStr.split("h")[1].split("m")[0] ) : intervalStr.split("m")[0]): intervalStr;
		long hr = Misc.getParamAsLong(hrStr,0);
		long min = Misc.getParamAsLong(minStr,0);
		long day = Misc.getParamAsLong(dayStr,0);
		return (day*24*60 + hr*60 + min); 
	}
}
