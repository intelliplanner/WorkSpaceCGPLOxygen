package com.ipssi.rfid.excel;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JTable;

import jxl.SheetSettings;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


public class ExcelExamples {
	static String firstHeading =  "Intelliplanner Software System India Pvt. Ltd.";
	static String secondHeading = "D-83, 2nd  Floor Seoctor-6, Noida-201301 (U.P.)";
	WritableFont  normalFont = new WritableFont(WritableFont.ARIAL, 12,WritableFont.NO_BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK );
	WritableFont FirstheadingFont = new WritableFont(WritableFont.ARIAL, 16,WritableFont.BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK ); 
	WritableFont secondheadingFont = new WritableFont(WritableFont.ARIAL,16,WritableFont.NO_BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK );
	
	
	private void initialize(){
	}
	
	private static WritableCellFormat getFirstHeadingFormat() throws WriteException {
		WritableFont headingFont = new WritableFont(WritableFont.ARIAL, 20,WritableFont.BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
		WritableCellFormat headingFormat = new WritableCellFormat(headingFont);
		headingFormat.setWrap(false);
		headingFormat.setAlignment(jxl.format.Alignment.CENTRE);
		headingFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
		headingFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);
		headingFormat.setBackground(Colour.BLUE_GREY);  
		return 	headingFormat;
	}
	private static WritableCellFormat getNormalFormat() throws WriteException {
		WritableFont headingFont = new WritableFont(WritableFont.ARIAL, 12,WritableFont.BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
		WritableCellFormat headingFormat = new WritableCellFormat(headingFont);
		headingFormat.setWrap(false);
		headingFormat.setAlignment(jxl.format.Alignment.CENTRE);
		headingFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
		headingFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);
		headingFormat.setBackground(Colour.BLUE_GREY);  
		return 	headingFormat;
	}
	private static WritableCellFormat getSecondHeadingFormat() throws WriteException {
		WritableFont headingFont = new WritableFont(WritableFont.ARIAL, 16,WritableFont.NO_BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
		WritableCellFormat headingFormat = new WritableCellFormat(headingFont);
		headingFormat.setWrap(false);
		headingFormat.setAlignment(jxl.format.Alignment.CENTRE);
		headingFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
		headingFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);
		headingFormat.setBackground(Colour.BLUE_GREY);  
		return 	headingFormat;
	}
	
	public static void exportExcelTable(JTable jTable1,File file) throws IOException{
		WorkbookSettings ws = new WorkbookSettings();
		try{
		WritableWorkbook workbook = Workbook.createWorkbook(file, ws);
		//create work sheet
		WritableSheet workSheet = null;
		workSheet = workbook.createSheet("Expence Report" ,0);
		SheetSettings sh = workSheet.getSettings();
		
		workSheet.mergeCells(0, 0, 12, 0);
		workSheet.mergeCells(0, 1, 12, 0);
		
		workSheet.mergeCells(0, 4, 5, 0);
		
		
//		workSheet.addCell(new jxl.write.Label(0,0,firstHeading,getFirstHeadingFormat()));
//		workSheet.addCell(new jxl.write.Label(0,1,secondHeading,getSecondHeadingFormat()));
//		
//		workSheet.addCell(new jxl.write.Label(3,4,"S.No",getNormalFormat()));
//		workSheet.addCell(new jxl.write.Label(4,4,"Date",getNormalFormat()));
//		workSheet.addCell(new jxl.write.Label(5,4,"From",getNormalFormat()));
//		workSheet.addCell(new jxl.write.Label(6,4,"To",getNormalFormat()));
//		workSheet.addCell(new jxl.write.Label(7,4,"Details",getNormalFormat()));
//		workSheet.addCell(new jxl.write.Label(8,4,"Ammount",getNormalFormat()));
		
		
		
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void main(String s[]) {
		//create WorkbookSettings object
		WorkbookSettings ws = new WorkbookSettings();

		try{
			File f = new File("d:/TestReport9.xls");
			//create work book
			WritableWorkbook workbook = Workbook.createWorkbook(f, ws);

			//create work sheet
			WritableSheet workSheet = null;
			workSheet = workbook.createSheet("Expence Report" ,0);
			SheetSettings sh = workSheet.getSettings();

			
//			workSheet.mergeCells(0, 0, 0,1);
			workSheet.mergeCells(0, 0, 12, 0);
			workSheet.mergeCells(0, 1, 12, 0);
			
			workSheet.mergeCells(0, 4, 5, 7);//0-start column, 4-row number, 5-number of columns merge, (5 to 8)- row merge
			
			//write to datasheet 
//			for(int i=0;i<10;i++)
//			
				workSheet.addCell(new jxl.write.Label(0,0,firstHeading,getFirstHeadingFormat()));
				workSheet.addCell(new jxl.write.Label(0,1,secondHeading,getSecondHeadingFormat()));
				workSheet.addCell(new jxl.write.Label(3,4,"S.No",getNormalFormat()));
				workSheet.addCell(new jxl.write.Label(4,4,"Date",getNormalFormat()));
				workSheet.addCell(new jxl.write.Label(5,4,"From",getNormalFormat()));
				workSheet.addCell(new jxl.write.Label(6,4,"To",getNormalFormat()));
				workSheet.addCell(new jxl.write.Label(7,4,"Details",getNormalFormat()));
				workSheet.addCell(new jxl.write.Label(8,4,"Ammount",getNormalFormat()));
				
			//write to the excel sheet
			workbook.write();

			//close the workbook
			workbook.close();
			Desktop.getDesktop().open(f);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
