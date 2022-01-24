package com.ipssi.reporting.trip;


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;



public class Excel2007 {
	private static HashMap<Integer,XSSFCellStyle> styleSheetExcel = null;
	private int currentRowNumber = 0;
	private String logoFile =  "";//"G://Working//EclipseWorkspace//static//images//report_logo.png";
	private String templateFileUrl = "";//"C:\\Users\\ipssi4\\Desktop\\staging\\lafarge_template1.xlsm";
	private FileInputStream fin = null;
	public Excel2007(String logoFile, String templateFileUrl){
		this.logoFile = logoFile;
		this.templateFileUrl = templateFileUrl;
	}
	public void printExcel(ByteArrayOutputStream out,String reportName,Table table,SessionManager session,int reportId) {
		XSSFWorkbook workbook = null;
		XSSFSheet workSheet =  null;
		int tableSize = table.getTableWidth();
		boolean templateBased = false;
		try {
			if(tableSize > 0){
				currentRowNumber = 0;
				templateBased = templateFileUrl != null && templateFileUrl.length() > 0;
				if(templateBased){
					fin = new FileInputStream(templateFileUrl);
					workbook = new XSSFWorkbook(fin);
					workSheet = workbook.getSheet("DETAIL_DATA");
					table.setOneDimentionHeader();
				}
				else{
					workbook = new XSSFWorkbook();
					workSheet = workbook.createSheet(reportName);
				}
				initExcel(workbook);
				if(logoFile != null && logoFile.length() > 0)
					addReportHeader(reportName, table,workSheet,workbook);
				createTableHeader(table, workSheet, workbook);
				createTableBody(table, workSheet, workbook);
				if(table.getTableWidth() > 0 ){
					workbook.unLockRevision();
					for(int i=0;i<table.getTableWidth();i++)					
						workSheet.autoSizeColumn(i);
				}
				if(out != null){
					workbook.write(out);
					out.flush();
					out.close();
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	private XSSFCellStyle getHeadingFormat(XSSFWorkbook workbook) {
		XSSFFont headingFont = workbook.createFont();
		headingFont.setFontName(XSSFFont.DEFAULT_FONT_NAME);
		headingFont.setFontHeightInPoints((short)12);
		headingFont.setColor(HSSFColor.WHITE.index);
		headingFont.setBoldweight((short) 1);
		XSSFCellStyle headingFormat = workbook.createCellStyle();
		headingFormat.setFont(headingFont);
		headingFormat.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		headingFormat.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		headingFormat.setFillForegroundColor(HSSFColor.BLUE_GREY.index);
		headingFormat.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		headingFormat.setBottomBorderColor(HSSFColor.BLACK.index);
		headingFormat.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		headingFormat.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		headingFormat.setBorderTop(XSSFCellStyle.BORDER_THIN);
		headingFormat.setBorderRight(XSSFCellStyle.BORDER_THIN);
		return 	headingFormat;
	}
	private void addReportHeader(String reportName, Table table, XSSFSheet workSheet, XSSFWorkbook workbook)
			throws Exception {
		int tableSize = table.getTableWidth();
		SimpleDateFormat indepDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		XSSFCellStyle headingFormat = getHeadingFormat(workbook);
		try{
			currentRowNumber += 3;
			int imageStart = tableSize <= 10 ? 0 :((tableSize-10)/2);
			CreationHelper helper = workbook.getCreationHelper();
			int picIndex = getPicIndex(workbook, logoFile);
			Drawing drawing = workSheet.createDrawingPatriarch();
			ClientAnchor anchor = helper.createClientAnchor();
		    anchor.setAnchorType(0);
			anchor.setCol1((short)1);
		    anchor.setRow1((short)0);
		    Picture pic = drawing.createPicture(anchor, picIndex);
		    pic.resize(0.7);
		    workSheet.addMergedRegion(new CellRangeAddress(0, currentRowNumber, 0,(tableSize-1)));
		    currentRowNumber++;
		    XSSFRow row1 = workSheet.createRow((short) currentRowNumber);
			XSSFCell cellA1 = row1.createCell((short) 0);
			cellA1.setCellValue(reportName.toUpperCase()+"                                                                 REPORT GENERATED ON: "+ indepDateFormat.format(new Date()));
			cellA1.setCellStyle(headingFormat);
			for(int i=1;i<=(tableSize-1);i++){
				cellA1 = row1.createCell(i);
				cellA1.setCellStyle(headingFormat);
			}
			workSheet.addMergedRegion(new CellRangeAddress(currentRowNumber,currentRowNumber,0, (tableSize-1)));
			currentRowNumber++;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	private void createTableHeader(Table table, XSSFSheet workSheet, XSSFWorkbook workbook)
			throws IOException {
		insertTableData(table, workSheet, true, workbook);
	}
	private void createTableBody(Table table, XSSFSheet workSheet, XSSFWorkbook workbook)
			throws IOException {
		insertTableData(table, workSheet, false, workbook);
	}

	private void insertTableData(Table table, XSSFSheet workSheet ,boolean isHeader, XSSFWorkbook workbook)
			throws IOException {
		int tableSize = table.getTableWidth();
		try {
			if (tableSize > 0){
				ArrayList<TR> header = isHeader ? table.getHeader() : table.getBody();
				int cellNumber = 0;
				int rowNumber = 0;
				int minX[] = new int[20];
				int maxX[] =  new int[20];
				ArrayList<Integer> tempRowSpan = new ArrayList<Integer>();
				boolean doRowSpan = false;
				int j = 0;
				XSSFCellStyle css = null;
				int index = 0;
				rowNumber = currentRowNumber;
				XSSFRow row1 = null;
				XSSFCell tempCell = null;
				XSSFCellStyle tempCss = null;
				if (header != null){
					boolean doColspan = true;
					for (TR row : header){
						if(!isHeader)
							currentRowNumber = rowNumber;
						cellNumber = 0;
						row1 = workSheet.createRow(rowNumber);
						if(doRowSpan){
							for(Integer i : tempRowSpan){
								tempCell = row1.createCell(i);
								tempCell.setCellStyle(tempCss);
								CellRangeAddress region = new CellRangeAddress((rowNumber-1),rowNumber,i,i);
								workSheet.addMergedRegion(region);
							}
							tempRowSpan.clear();
							doRowSpan = false;
						}
						XSSFCell cell1 = null;
						for(TD col : row.getRowData()){
							boolean doNumber = false;
							if(col.getDoIgnore() || col.getHidden())
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
							doNumber = col.getContentType() == 5;
							css = !Misc.isUndef(col.getClassId()) ? styleSheetExcel.get(col.getClassId()) : styleSheetExcel.get(row.getClassId());
							if (rowNumber > currentRowNumber && doColspan)
							{   
								for(int k=index;k<maxX.length;k++)
									if ((minX[k] < maxX[k]) && (minX[k] < tableSize) && (maxX[k] < tableSize))
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
								cell1 = row1.createCell(cellNumber);
								if(doNumber && !isHeader)
									cell1.setCellValue(Misc.getParamAsInt(col.getContent(),0));
								else
									cell1.setCellValue(col.getContent());
								cell1.setCellStyle(css);
								for(int i=cellNumber+1;i<=cellNumber+col.getColSpan();i++){
									cell1 = row1.createCell(i);
									cell1.setCellStyle(css);
								}
								CellRangeAddress region = new CellRangeAddress(rowNumber,rowNumber,cellNumber, (cellNumber+col.getColSpan()-1));
								workSheet.addMergedRegion(region);
								cellNumber += col.getColSpan() ;
							}
							if (col.getRowSpan() > 1){
								cell1 = row1.createCell(cellNumber);
								if(doNumber && !isHeader)
									cell1.setCellValue(Misc.getParamAsInt(col.getContent(),0));
								else
									cell1.setCellValue(col.getContent());
								cell1.setCellStyle(css);
								tempRowSpan.add(cellNumber);
								tempCss = css;
								doRowSpan = true;
								cellNumber++;
							}
							if (col.getColSpan() <= 1 && col.getRowSpan() <= 1)
							{ 
								cell1 = row1.createCell(cellNumber);
								if(doNumber && !isHeader)
									cell1.setCellValue(Misc.getParamAsInt(col.getContent(),0));
								else
									cell1.setCellValue(col.getContent());
								cell1.setCellStyle(css);
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

	public static void initExcel(XSSFWorkbook workbook)  {
		XSSFFont font1 = workbook.createFont();
		font1.setFontName(HSSFFont.FONT_ARIAL);
		font1.setFontHeightInPoints((short)10);
		font1.setColor(HSSFColor.WHITE.index);
		font1.setBoldweight((short) 1);
		XSSFFont font2 = workbook.createFont();
		font2.setFontName(HSSFFont.FONT_ARIAL);
		font2.setFontHeightInPoints((short)10);
		font2.setColor(HSSFColor.WHITE.index);
		font2.setBoldweight((short) 0);
		XSSFFont font3 = workbook.createFont();
		font3.setFontName(HSSFFont.FONT_ARIAL);
		font3.setFontHeightInPoints((short)10);
		font3.setColor(HSSFColor.BLACK.index);
		font3.setBoldweight((short) 0);
		
		XSSFFont font2b = workbook.createFont();
		font2b.setFontName(HSSFFont.FONT_ARIAL);
		font2b.setFontHeightInPoints((short)10);
		font2b.setColor(HSSFColor.WHITE.index);
		font2b.setBoldweight((short) 1);
		XSSFFont font3b = workbook.createFont();
		font3b.setFontName(HSSFFont.FONT_ARIAL);
		font3b.setFontHeightInPoints((short)10);
		font3b.setColor(HSSFColor.BLACK.index);
		font3b.setBoldweight((short) 1);
		
		styleSheetExcel = new HashMap<Integer, XSSFCellStyle>();
		{
			XSSFCellStyle format0 = workbook.createCellStyle();
			format0.setFont(font1);
			format0.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			format0.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format0.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
			format0.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format0.setBottomBorderColor(HSSFColor.WHITE.index);
			format0.setBorderBottom((short) 1);
			format0.setBorderLeft((short) 1);
			format0.setBorderTop((short) 1);
			format0.setBorderRight((short) 1);
			styleSheetExcel.put(0, format0);
		}
		{
			XSSFCellStyle format1 = workbook.createCellStyle();
			format1.setFont(font1);
			format1.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			format1.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format1.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
			format1.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format1.setBottomBorderColor(HSSFColor.WHITE.index);
			format1.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			format1.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			format1.setBorderTop(XSSFCellStyle.BORDER_THIN);
			format1.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleSheetExcel.put(1, format1);
		}
		{
			XSSFCellStyle format2 = workbook.createCellStyle();
			format2.setFont(font3);
			format2.setAlignment(XSSFCellStyle.ALIGN_LEFT);
			format2.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format2.setFillForegroundColor(HSSFColor.WHITE.index);
			format2.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format2.setBottomBorderColor(HSSFColor.BLACK.index);
			format2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			format2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			format2.setBorderTop(XSSFCellStyle.BORDER_THIN);
			format2.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleSheetExcel.put(2, format2);
		}
		{
			XSSFCellStyle format3 = workbook.createCellStyle();
			format3.setFont(font3);
			format3.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
			format3.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format3.setFillForegroundColor(HSSFColor.WHITE.index);
			format3.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format3.setBottomBorderColor(HSSFColor.BLACK.index);
			format3.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			format3.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			format3.setBorderTop(XSSFCellStyle.BORDER_THIN);
			format3.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleSheetExcel.put(3, format3);
		}
		{
			XSSFCellStyle format4 = workbook.createCellStyle();
			format4.setFont(font2);
			format4.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
			format4.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format4.setFillForegroundColor(HSSFColor.GREEN.index);
			format4.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format4.setBottomBorderColor(HSSFColor.WHITE.index);
			format4.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			format4.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			format4.setBorderTop(XSSFCellStyle.BORDER_THIN);
			format4.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleSheetExcel.put(4, format4);
		}
		{
			XSSFCellStyle format5 = workbook.createCellStyle();
			format5.setFont(font3);
			format5.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
			format5.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format5.setFillForegroundColor(HSSFColor.YELLOW.index);
			format5.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format5.setBottomBorderColor(HSSFColor.WHITE.index);
			format5.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			format5.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			format5.setBorderTop(XSSFCellStyle.BORDER_THIN);
			format5.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleSheetExcel.put(5, format5);
		}
		{
			XSSFCellStyle format6 = workbook.createCellStyle();
			format6.setFont(font2);
			format6.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
			format6.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format6.setFillForegroundColor(HSSFColor.RED.index);
			format6.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format6.setBottomBorderColor(HSSFColor.WHITE.index);
			format6.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			format6.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			format6.setBorderTop(XSSFCellStyle.BORDER_THIN);
			format6.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleSheetExcel.put(6, format6);
		}
//b
		{
			XSSFCellStyle format2 = workbook.createCellStyle();
			format2.setFont(font3b);
			format2.setAlignment(XSSFCellStyle.ALIGN_LEFT);
			format2.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format2.setFillForegroundColor(HSSFColor.WHITE.index);
			format2.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format2.setBottomBorderColor(HSSFColor.BLACK.index);
			format2.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			format2.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			format2.setBorderTop(XSSFCellStyle.BORDER_THIN);
			format2.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleSheetExcel.put(10, format2);
		}
		{
			XSSFCellStyle format3 = workbook.createCellStyle();
			format3.setFont(font3b);
			format3.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
			format3.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format3.setFillForegroundColor(HSSFColor.WHITE.index);
			format3.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format3.setBottomBorderColor(HSSFColor.BLACK.index);
			format3.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			format3.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			format3.setBorderTop(XSSFCellStyle.BORDER_THIN);
			format3.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleSheetExcel.put(11, format3);
		}
		{
			XSSFCellStyle format4 = workbook.createCellStyle();
			format4.setFont(font2b);
			format4.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
			format4.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format4.setFillForegroundColor(HSSFColor.GREEN.index);
			format4.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format4.setBottomBorderColor(HSSFColor.WHITE.index);
			format4.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			format4.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			format4.setBorderTop(XSSFCellStyle.BORDER_THIN);
			format4.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleSheetExcel.put(12, format4);
		}
		{
			XSSFCellStyle format5 = workbook.createCellStyle();
			format5.setFont(font3b);
			format5.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
			format5.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format5.setFillForegroundColor(HSSFColor.YELLOW.index);
			format5.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format5.setBottomBorderColor(HSSFColor.WHITE.index);
			format5.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			format5.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			format5.setBorderTop(XSSFCellStyle.BORDER_THIN);
			format5.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleSheetExcel.put(13, format5);
		}
		{
			XSSFCellStyle format6 = workbook.createCellStyle();
			format6.setFont(font2b);
			format6.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
			format6.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
			format6.setFillForegroundColor(HSSFColor.RED.index);
			format6.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
			format6.setBottomBorderColor(HSSFColor.WHITE.index);
			format6.setBorderBottom(XSSFCellStyle.BORDER_THIN);
			format6.setBorderLeft(XSSFCellStyle.BORDER_THIN);
			format6.setBorderTop(XSSFCellStyle.BORDER_THIN);
			format6.setBorderRight(XSSFCellStyle.BORDER_THIN);
			styleSheetExcel.put(14, format6);
		}
	}
	public static int getPicIndex(XSSFWorkbook workbook,String fileURL){
		int index = -1;
		try {
			InputStream is = new FileInputStream(fileURL);
		    byte[] bytes = IOUtils.toByteArray(is);
			index = workbook.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_PNG);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}  catch (Exception e) {
			e.printStackTrace();
		} 
		return index;
	}
	public XSSFWorkbook mergeExcelFiles(XSSFWorkbook book, ArrayList<FileInputStream> inList) throws IOException {

	    for (FileInputStream fin : inList) {
	        XSSFWorkbook b = new XSSFWorkbook(fin);
	        for (int i = 0; i < b.getNumberOfSheets(); i++) {
	            // not entering sheet name, because of duplicated names
	            copySheets(book.createSheet(),b.getSheetAt(i));
	        }
	    }
	    return book;
	}
	/** 
	 * @param newSheet the sheet to create from the copy. 
	 * @param sheet the sheet to copy. 
	 */  
	public static void copySheets(XSSFSheet newSheet, XSSFSheet sheet){     
	    copySheets(newSheet, sheet, true);     
	}     

	/** 
	 * @param newSheet the sheet to create from the copy. 
	 * @param sheet the sheet to copy. 
	 * @param copyStyle true copy the style. 
	 */  
	public static void copySheets(XSSFSheet newSheet, XSSFSheet sheet, boolean copyStyle){     
	    int maxColumnNum = 0;     
	    Map<Integer, XSSFCellStyle> styleMap = (copyStyle) ? new HashMap<Integer, XSSFCellStyle>() : null;     
	    for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {     
	        XSSFRow srcRow = sheet.getRow(i);     
	        XSSFRow destRow = newSheet.createRow(i);     
	        if (srcRow != null) {     
	            copyRow(sheet, newSheet, srcRow, destRow, styleMap);     
	            if (srcRow.getLastCellNum() > maxColumnNum) {     
	                maxColumnNum = srcRow.getLastCellNum();     
	            }     
	        }     
	    }     
	    for (int i = 0; i <= maxColumnNum; i++) {     
	        newSheet.setColumnWidth(i, sheet.getColumnWidth(i));     
	    }     
	}     

	/** 
	 * @param srcSheet the sheet to copy. 
	 * @param destSheet the sheet to create. 
	 * @param srcRow the row to copy. 
	 * @param destRow the row to create. 
	 * @param styleMap - 
	 */  
	public static void copyRow(XSSFSheet srcSheet, XSSFSheet destSheet, XSSFRow srcRow, XSSFRow destRow, Map<Integer, XSSFCellStyle> styleMap) {     
	    // manage a list of merged zone in order to not insert two times a merged zone  
	  Set<CellRangeAddressWrapper> mergedRegions = new TreeSet<CellRangeAddressWrapper>();     
	    destRow.setHeight(srcRow.getHeight());     
	    // reckoning delta rows  
	    int deltaRows = destRow.getRowNum()-srcRow.getRowNum();  
	    // pour chaque row  
	    for (int j = srcRow.getFirstCellNum(); j <= srcRow.getLastCellNum(); j++) {     
	        XSSFCell oldCell = srcRow.getCell(j);   // ancienne cell  
	        XSSFCell newCell = destRow.getCell(j);  // new cell   
	        if (oldCell != null) {     
	            if (newCell == null) {     
	                newCell = destRow.createCell(j);     
	            }     
	            // copy chaque cell  
	            copyCell(oldCell, newCell, styleMap);     
	            // copy les informations de fusion entre les cellules  
	            //System.out.println("row num: " + srcRow.getRowNum() + " , col: " + (short)oldCell.getColumnIndex());  
	            CellRangeAddress mergedRegion = getMergedRegion(srcSheet, srcRow.getRowNum(), (short)oldCell.getColumnIndex());     

	            if (mergedRegion != null) {   
	              //System.out.println("Selected merged region: " + mergedRegion.toString());  
	              CellRangeAddress newMergedRegion = new CellRangeAddress(mergedRegion.getFirstRow()+deltaRows, mergedRegion.getLastRow()+deltaRows, mergedRegion.getFirstColumn(),  mergedRegion.getLastColumn());  
	                //System.out.println("New merged region: " + newMergedRegion.toString());  
	                CellRangeAddressWrapper wrapper = new CellRangeAddressWrapper(newMergedRegion);  
	                if (isNewMergedRegion(wrapper, mergedRegions)) {  
	                    mergedRegions.add(wrapper);  
	                    destSheet.addMergedRegion(wrapper.range);     
	                }     
	            }     
	        }     
	    }                
	}    

	/** 
	 * @param oldCell 
	 * @param newCell 
	 * @param styleMap 
	 */  
	public static void copyCell(XSSFCell oldCell, XSSFCell newCell, Map<Integer, XSSFCellStyle> styleMap) {     
	    if(styleMap != null) {     
	        if(oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()){     
	            newCell.setCellStyle(oldCell.getCellStyle());     
	        } else{     
	            int stHashCode = oldCell.getCellStyle().hashCode();     
	            XSSFCellStyle newCellStyle = styleMap.get(stHashCode);     
	            if(newCellStyle == null){     
	                newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();     
	                newCellStyle.cloneStyleFrom(oldCell.getCellStyle());     
	                styleMap.put(stHashCode, newCellStyle);     
	            }     
	            newCell.setCellStyle(newCellStyle);     
	        }     
	    }     
	    switch(oldCell.getCellType()) {     
	        case XSSFCell.CELL_TYPE_STRING:     
	            newCell.setCellValue(oldCell.getStringCellValue());     
	            break;     
	      case XSSFCell.CELL_TYPE_NUMERIC:     
	            newCell.setCellValue(oldCell.getNumericCellValue());     
	            break;     
	        case XSSFCell.CELL_TYPE_BLANK:     
	            newCell.setCellType(XSSFCell.CELL_TYPE_BLANK);     
	            break;     
	        case XSSFCell.CELL_TYPE_BOOLEAN:     
	            newCell.setCellValue(oldCell.getBooleanCellValue());     
	            break;     
	        case XSSFCell.CELL_TYPE_ERROR:     
	            newCell.setCellErrorValue(oldCell.getErrorCellValue());     
	            break;     
	        case XSSFCell.CELL_TYPE_FORMULA:     
	            newCell.setCellFormula(oldCell.getCellFormula());     
	            break;     
	        default:     
	            break;     
	    }     

	}     

	/** 
	 * Récupère les informations de fusion des cellules dans la sheet source pour les appliquer 
	 * à la sheet destination... 
	 * Récupère toutes les zones merged dans la sheet source et regarde pour chacune d'elle si 
	 * elle se trouve dans la current row que nous traitons. 
	 * Si oui, retourne l'objet CellRangeAddress. 
	 *  
	 * @param sheet the sheet containing the data. 
	 * @param rowNum the num of the row to copy. 
	 * @param cellNum the num of the cell to copy. 
	 * @return the CellRangeAddress created. 
	 */  
	public static CellRangeAddress getMergedRegion(XSSFSheet sheet, int rowNum, short cellNum) {     
	    for (int i = 0; i < sheet.getNumMergedRegions(); i++) {   
	        CellRangeAddress merged = sheet.getMergedRegion(i);     
	        if (merged.isInRange(rowNum, cellNum)) {     
	            return merged;     
	        }     
	    }     
	    return null;     
	}     

	/** 
	 * Check that the merged region has been created in the destination sheet. 
	 * @param newMergedRegion the merged region to copy or not in the destination sheet. 
	 * @param mergedRegions the list containing all the merged region. 
	 * @return true if the merged region is already in the list or not. 
	 */  
	private static boolean isNewMergedRegion(CellRangeAddressWrapper newMergedRegion, Set<CellRangeAddressWrapper> mergedRegions) {  
	  return !mergedRegions.contains(newMergedRegion);     
	}     

	}
	class CellRangeAddressWrapper implements Comparable<CellRangeAddressWrapper> {  

	public CellRangeAddress range;  

	/** 
	 * @param theRange the CellRangeAddress object to wrap. 
	 */  
	public CellRangeAddressWrapper(CellRangeAddress theRange) {  
	      this.range = theRange;  
	}  

	/** 
	 * @param o the object to compare. 
	 * @return -1 the current instance is prior to the object in parameter, 0: equal, 1: after... 
	 */  
	public int compareTo(CellRangeAddressWrapper o) {  

	            if (range.getFirstColumn() < o.range.getFirstColumn()  
	                        && range.getFirstRow() < o.range.getFirstRow()) {  
	                  return -1;  
	            } else if (range.getFirstColumn() == o.range.getFirstColumn()  
	                        && range.getFirstRow() == o.range.getFirstRow()) {  
	                  return 0;  
	            } else {  
	                  return 1;  
	            }  

	}  

	public static void main(String[] args){
	}
	
}