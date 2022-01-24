package com.ipssi.reporting.trip;

import java.io.ByteArrayOutputStream;
import com.ipssi.gen.utils.SessionManager;

public class ExcelGenerator_poi {
	private String logoFile =  "";//"G://Working//EclipseWorkspace//static//images//report_logo.png";
	private String templateFileUrl = "";//"C:\\Users\\ipssi4\\Desktop\\staging\\lafarge_template1.xlsm";
	public ExcelGenerator_poi(){

	}
	public void printExcel(ByteArrayOutputStream out,String reportName,Table table,SessionManager session,int reportId) {
		int tableSize = table.getTableWidth();
		boolean templateBased = false;
		try {
			logoFile = CssClassDefinition.getOrgLogo(reportId, session);
			templateFileUrl = CssClassDefinition.getTemplateFile(reportId, session);
			if(tableSize > 0){
				templateBased = templateFileUrl != null && templateFileUrl.length() > 0;
				if(templateBased){
					Excel2007 printer = new Excel2007(logoFile, templateFileUrl);
					printer.printExcel(out, reportName, table, session, reportId);
				}
				else{
					Excel2003 printer = new Excel2003(logoFile);
					printer.printExcel(out, reportName, table, session, reportId);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}