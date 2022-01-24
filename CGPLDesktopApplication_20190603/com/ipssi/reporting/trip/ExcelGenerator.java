package com.ipssi.reporting.trip;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.itextpdf.text.DocumentException;

public class ExcelGenerator {
	private static HashMap<Integer,WritableCellFormat> styleSheetExcel = null;
	private int currentRowNumber = 0;
//	private String logoFile = "G://Working//EclipseWorkspace//static//images//report_logo.png";
	private String logoFile = "";
//	private String aavvikLogo = "/home/jboss/static/images/aavvik.jpg";
	private String groupParamVal = null;
	private int dataRowsCount = 0;
	public ExcelGenerator(){
    	
    }
	public void printExcel(ByteArrayOutputStream out,String reportName,Table table,SessionManager session,int reportId) {
		printExcel(out, reportName, table, session, reportId, null);
	}
	
	public void printExcel(ByteArrayOutputStream out,String reportName,Table table,SessionManager session,int reportId,String groupParamVal) {
		WorkbookSettings workSetting = null;
		WritableWorkbook workbook = null;
		WritableSheet workSheet = null;
		this.groupParamVal = groupParamVal;
		
		//table.format();
		int tableSize = table.getTableWidth();
		try {
			logoFile = CssClassDefinition.getOrgLogo(reportId, session);
			if(tableSize > 0){
				currentRowNumber = 0;
				initExcel();
				workSetting = new WorkbookSettings();
				workSetting.setLocale(new Locale("en", "EN"));
				workbook = Workbook.createWorkbook(out, workSetting);
				workbook.createSheet(reportName ,0);
				workSheet = workbook.getSheet(0);
				if(logoFile != null && logoFile.length() > 0)
					addReportHeader(reportName, table,workSheet);
				createTableHeader(table, workSheet);
				createTableBody(table, workSheet);
				if(workbook != null){
					workbook.write();
					workbook.close();
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			try {
				workbook.close();
				workbook = null;
			} catch (WriteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	private WritableCellFormat getHeadingFormat() throws WriteException {
		WritableFont headingFont = new WritableFont(WritableFont.ARIAL, 12,WritableFont.BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.WHITE);
		WritableCellFormat headingFormat = new WritableCellFormat(headingFont);
		headingFormat.setWrap(false);
		headingFormat.setAlignment(jxl.format.Alignment.CENTRE);
		headingFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
		headingFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);
		headingFormat.setBackground(Colour.BLUE_GREY);  
		return 	headingFormat;
	}
	private void addReportHeader(String reportName, Table table, WritableSheet workSheet)
			throws Exception {
		int tableSize = table.getTableWidth();
		SimpleDateFormat indepDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		WritableCellFormat headingFormat = getHeadingFormat();
		try{
			
			currentRowNumber += 3;
			int imageStart = tableSize <= 10 ? 0 :((tableSize-10)/2);
			int imageWidth = tableSize >= 10 ? 10 : tableSize;
			workSheet.mergeCells(0, 0, (tableSize-1),currentRowNumber);
			workSheet.addImage(new WritableImage(imageStart,0,imageWidth,4,new File(logoFile)));
			currentRowNumber++;
			workSheet.mergeCells(0,currentRowNumber, (tableSize-1),currentRowNumber);
			workSheet.addCell(new jxl.write.Label(0,currentRowNumber,reportName.toUpperCase()+"                        REPORT GENERATED ON: "+ indepDateFormat.format(new Date()),headingFormat));
			currentRowNumber++;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	private void createTableHeader(Table table, WritableSheet workSheet )
			throws DocumentException, WriteException, IOException {
		insertTableData(table, workSheet, true);
	}
	private void createTableBody(Table table, WritableSheet workSheet )
			throws DocumentException, WriteException, IOException {
		insertTableData(table, workSheet, false);
	}

	private void insertTableData(Table table, WritableSheet workSheet ,boolean isHeader)
			throws DocumentException, WriteException, IOException {
		int tableSize = table.getTableWidth();
		try {
			if (tableSize > 0){
				ArrayList<TR> header = isHeader ? table.getHeader() : table.getBody();
				int cellNumber = 0;
				int rowNumber = 0;
				int minX[] = new int[20];
				int maxX[] =  new int[20];
				int j = 0;
				WritableCellFormat css = null;
				int index = 0;
				rowNumber = currentRowNumber;
				if (header != null){
					boolean doColspan = true;
					for (TR row : header){
						if(!isHeader){
							if(table.getMailClusteringIndex() > 0 && groupParamVal != null && groupParamVal.length() > 0 && row.getRowData() != null &&  row.getRowData().size() > table.getMailClusteringIndex()  && row.getRowData().get(table.getMailClusteringIndex()) != null && !groupParamVal.equalsIgnoreCase(row.getRowData().get(table.getMailClusteringIndex()).getContent()))
								continue;
							currentRowNumber = rowNumber;
							dataRowsCount++;
						}
						cellNumber = 0;
						for(TD col : row.getRowData()){
							if(col.getDoIgnore() || col.getHidden() )
								continue;
							if(!isHeader){
								if (col.getContent().equals(Misc.nbspString))
									col.setContent("");
								if (col.getContent().contains("power_on.png"))
									col.setContent("ON");
								if (col.getContent().contains("power_off.png"))
									col.setContent("OFF");
								if (col.getContent().contains("battery_charging.png"))
									col.setContent("ON");
								if (col.getContent().contains("battery_discharging.png"))
									col.setContent("OFF");
							}
							css = !Misc.isUndef(col.getClassId()) ? styleSheetExcel.get(col.getClassId()) : styleSheetExcel.get(row.getClassId());
							if (rowNumber > currentRowNumber && doColspan)
							{   
								for(int k=index;k<maxX.length;k++)
									if ((minX[k] < maxX[k]) && (minX[k] <= tableSize) && (maxX[k] <= tableSize))
									{   
										index = k;
										cellNumber = minX[k];
										doColspan = false;
										break;
									}
							}
							if (col.getColSpan() > 1){
								minX[j] = cellNumber;
								maxX [j++]= cellNumber+col.getColSpan();
								workSheet.mergeCells(cellNumber, rowNumber,(cellNumber+col.getColSpan()-1),rowNumber);
								addCellData(workSheet, col, cellNumber, rowNumber, css,isHeader);
								//workSheet.addCell(new jxl.write.Label(cellNumber,rowNumber,col.getContent(),css));
								cellNumber += col.getColSpan() ;
							}
							if (col.getRowSpan() > 1){
								workSheet.mergeCells(cellNumber, rowNumber,cellNumber,(rowNumber+col.getRowSpan()-1));
								addCellData(workSheet, col, cellNumber, rowNumber, css,isHeader);
								//workSheet.addCell(new jxl.write.Label(cellNumber,rowNumber,col.getContent(),css));
								cellNumber++;
							}
							if (col.getColSpan() <= 1 && col.getRowSpan() <= 1)
							{
								addCellData(workSheet, col, cellNumber, rowNumber, css,isHeader);
								//workSheet.addCell(new jxl.write.Label(cellNumber,rowNumber,col.getContent(),css));
								cellNumber++;
							}
							if(cellNumber == maxX[index] && !doColspan){
								index++;
								doColspan = true;
							}
						}	
						rowNumber++;
					}
				}
				currentRowNumber = rowNumber;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	private void addCellData(WritableSheet workSheet,TD td,int cellNumber ,int rowNumber,WritableCellFormat css,boolean isHeader) throws RowsExceededException, WriteException{
		if(td == null)
			return;
		if(!isHeader && (td.getContentType() == Cache.NUMBER_TYPE || td.getContentType() == Cache.INTEGER_TYPE) && !Misc.isUndef(Misc.getParamAsDouble(td.getContent()))){
			workSheet.addCell(new jxl.write.Number(cellNumber,rowNumber,Misc.getParamAsDouble(td.getContent()),css));
		}else{
			workSheet.addCell(new jxl.write.Label(cellNumber,rowNumber,td.getContent(),css));
		}
	}

	/*private void insertTableData(Table table, WritableSheet workSheet)
			throws DocumentException, WriteException, IOException { 
		try {
			int tableSize = table.getTableWidth();
			if (tableSize > 0){
				WritableCellFormat css = null;
				ArrayList<TR> body = table.getBody();
				int cellNumber = 0;
				int rowNumber = currentRowNumber;
				for (TR row : body){
					cellNumber = 0;
					for(TD col : row.getRowData()){
						if(col.getDoIgnore())
							continue;
						css = !Misc.isUndef(col.getClassId()) ? styleSheetExcel.get(col.getClassId()) : styleSheetExcel.get(row.getClassId());
						if (col.getContent().equals(Misc.nbspString))
							col.setContent("");
						if (col.getContent().contains("power_on.png"))
							col.setContent("ON");
						if (col.getContent().contains("power_off.png"))
							col.setContent("OFF");
						if (col.getContent().contains("battery_charging.png"))
							col.setContent("ON");
						if (col.getContent().contains("battery_discharging.png"))
							col.setContent("OFF");
						workSheet.addCell(new jxl.write.Label(cellNumber,rowNumber,col.getContent(),css));
						cellNumber++;				
					}
					rowNumber++;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}*/
	public static void initExcel() throws WriteException {

		WritableFont font1 = new WritableFont(WritableFont.ARIAL, 10,WritableFont.BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.WHITE);
		WritableFont font2 = new WritableFont(WritableFont.ARIAL, 10,WritableFont.NO_BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.WHITE);
		WritableFont font3 = new WritableFont(WritableFont.ARIAL, 10,WritableFont.NO_BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
		
		WritableFont font2b = new WritableFont(WritableFont.ARIAL, 10,WritableFont.BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.WHITE);
		WritableFont font3b = new WritableFont(WritableFont.ARIAL, 10,WritableFont.BOLD,  false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
	
		styleSheetExcel = new HashMap<Integer, WritableCellFormat>();
		{
			WritableCellFormat format0 = new WritableCellFormat(font1);
			format0.setWrap(false);
			format0.setAlignment(jxl.format.Alignment.CENTRE);
			format0.setVerticalAlignment(VerticalAlignment.CENTRE);
			format0.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.WHITE);
			format0.setBackground(Colour.GRAY_50); 
			styleSheetExcel.put(0, format0);
		}
		{
			WritableCellFormat format1 = new WritableCellFormat(font1);
			format1.setWrap(false);
			format1.setAlignment(jxl.format.Alignment.CENTRE);
			format1.setVerticalAlignment(VerticalAlignment.CENTRE);
			format1.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.WHITE);
			format1.setBackground(Colour.GRAY_50); 
			styleSheetExcel.put(1, format1);
		}
		{
			WritableCellFormat format2 = new WritableCellFormat(font3);
			format2.setWrap(false);
			format2.setAlignment(jxl.format.Alignment.LEFT);
			format2.setVerticalAlignment(VerticalAlignment.CENTRE);
			format2.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);
			format2.setBackground(Colour.WHITE); 
			styleSheetExcel.put(2, format2);
		}
		{
			WritableCellFormat format3 = new WritableCellFormat(font3);
			format3.setWrap(false);
			format3.setAlignment(jxl.format.Alignment.RIGHT);
			format3.setVerticalAlignment(VerticalAlignment.CENTRE);
			format3.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);
			format3.setBackground(Colour.WHITE); 
			styleSheetExcel.put(3, format3);
		}
		{
			WritableCellFormat format4 = new WritableCellFormat(font2);
			format4.setWrap(false);
			format4.setAlignment(jxl.format.Alignment.RIGHT);
			format4.setVerticalAlignment(VerticalAlignment.CENTRE);
			format4.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.WHITE);
			format4.setBackground(Colour.GREEN); 
			styleSheetExcel.put(4, format4);
		}
		{
			WritableCellFormat format5 = new WritableCellFormat(font3);
			format5.setWrap(false);
			format5.setAlignment(jxl.format.Alignment.RIGHT);
			format5.setVerticalAlignment(VerticalAlignment.CENTRE);
			format5.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.WHITE);
			format5.setBackground(Colour.YELLOW); 
			styleSheetExcel.put(5, format5);
		}
		{
			WritableCellFormat format6 = new WritableCellFormat(font2);
			format6.setWrap(false);
			format6.setAlignment(jxl.format.Alignment.RIGHT);
			format6.setVerticalAlignment(VerticalAlignment.CENTRE);
			format6.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.WHITE);
			format6.setBackground(Colour.RED); 
			styleSheetExcel.put(6, format6);
		}
		//b
		{
			WritableCellFormat format2 = new WritableCellFormat(font3b);
			format2.setWrap(false);
			format2.setAlignment(jxl.format.Alignment.LEFT);
			format2.setVerticalAlignment(VerticalAlignment.CENTRE);
			format2.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);
			format2.setBackground(Colour.WHITE); 
			styleSheetExcel.put(10, format2);
		}
		{
			WritableCellFormat format3 = new WritableCellFormat(font3b);
			format3.setWrap(false);
			format3.setAlignment(jxl.format.Alignment.RIGHT);
			format3.setVerticalAlignment(VerticalAlignment.CENTRE);
			format3.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);
			format3.setBackground(Colour.WHITE); 
			styleSheetExcel.put(11, format3);
		}
		{
			WritableCellFormat format4 = new WritableCellFormat(font2b);
			format4.setWrap(false);
			format4.setAlignment(jxl.format.Alignment.RIGHT);
			format4.setVerticalAlignment(VerticalAlignment.CENTRE);
			format4.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.WHITE);
			format4.setBackground(Colour.GREEN); 
			styleSheetExcel.put(12, format4);
		}
		{
			WritableCellFormat format5 = new WritableCellFormat(font3b);
			format5.setWrap(false);
			format5.setAlignment(jxl.format.Alignment.RIGHT);
			format5.setVerticalAlignment(VerticalAlignment.CENTRE);
			format5.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.WHITE);
			format5.setBackground(Colour.YELLOW); 
			styleSheetExcel.put(13, format5);
		}
		{
			WritableCellFormat format6 = new WritableCellFormat(font2b);
			format6.setWrap(false);
			format6.setAlignment(jxl.format.Alignment.RIGHT);
			format6.setVerticalAlignment(VerticalAlignment.CENTRE);
			format6.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.WHITE);
			format6.setBackground(Colour.RED); 
			styleSheetExcel.put(14, format6);
		}
	}
	public static void main(String[] args) throws WriteException, IOException, DocumentException {/*
		ExcelGenerator ce = new ExcelGenerator();
		FileOutputStream file = new FileOutputStream("G://test.xls");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ce.init(out, "test");
		CellContent cd = null;
		ArrayList<CellContent> data = new ArrayList<CellContent>();
		cd = new CellContent();
		cd.setContent("heading 1");
		cd.setColSpan(1);
		cd.setRowSpan(2);
		data.add(cd);
		cd = new CellContent();
		cd.setContent("heading 2");
		cd.setColSpan(4);
		cd.setRowSpan(1);
		data.add(cd);
		cd = new CellContent();
		cd.setContent("heading 3");
		cd.setColSpan(1);
		cd.setRowSpan(2);
		data.add(cd);
		cd = new CellContent();
		cd.setContent("heading 1");
		cd.setColSpan(1);
		cd.setRowSpan(1);
		data.add(cd);
		cd = new CellContent();
		cd.setContent("heading 3");
		cd.setColSpan(1);
		cd.setRowSpan(1);
		data.add(cd);

		ce.createTableHeader(data, "test", 6);
		ce.generateReport();
		file.write(out.toByteArray());
		file.close();
		//create WorkbookSettings object
		WorkbookSettings ws = new WorkbookSettings();

		try{
			//create work book
			WritableWorkbook workbook = Workbook.createWorkbook(new File("g:/TestReport.xls"), ws);

			//create work sheet
			WritableSheet workSheet = null;
			workSheet = workbook.createSheet("Test Report" ,0);
			SheetSettings sh = workSheet.getSettings();

			//Creating Writable font to be used in the report  
			WritableFont normalFont = new WritableFont(WritableFont.TIMES,
					12,
					WritableFont.BOLD,  false,
					UnderlineStyle.NO_UNDERLINE,
					Colour.WHITE
					);

			//creating plain format to write data in excel sheet
			WritableCellFormat normalFormat = new WritableCellFormat(normalFont);
			normalFormat.setWrap(true);
			normalFormat.setAlignment(jxl.format.Alignment.CENTRE);
			normalFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
			normalFormat.setWrap(true);
			normalFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN, jxl.format.Colour.WHITE);
			normalFormat.setBackground(Colour.ORANGE);
			workSheet.mergeCells(0, 0, 0,1);
			//write to datasheet 
			for(int i=0;i<10;i++)
				workSheet.addCell(new jxl.write.Label(i,0,"User Name"+i,normalFormat));

			//write to the excel sheet
			workbook.write();

			//close the workbook
			workbook.close();

		}catch(Exception e){
			e.printStackTrace();
		}
	 */}
	public int getDataRowsCount() {
		return dataRowsCount;
	}
	
}