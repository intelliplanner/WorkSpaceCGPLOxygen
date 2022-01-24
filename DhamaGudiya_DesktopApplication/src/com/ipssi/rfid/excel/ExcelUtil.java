package com.ipssi.rfid.excel;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public final class ExcelUtil {

//	protected static SXSSFWorkbook wb;
//    protected Sheet sh;
//    protected static final String EMPTY_VALUE = " ";
    
	private ExcelUtil() {
	}

	// capitalize the first letter of the field name for retrieving value of the
	// field later
	public static String capitalizeInitialLetter(String s) {
		if (s.length() == 0)
			return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	
	 protected static CellStyle getHeaderStyle(SXSSFWorkbook wb) {
	        CellStyle style = wb.createCellStyle();
	        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
//	        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
//	        style.setBorderBottom(CellStyle.BORDER_THIN);
	        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
//	        style.setBorderLeft(CellStyle.BORDER_THIN);
	        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
//	        style.setBorderRight(CellStyle.BORDER_THIN);
	        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
//	        style.setBorderTop(CellStyle.BORDER_THIN);
	        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
//	        style.setAlignment(CellStyle.ALIGN_CENTER);
	        return style;
	    }
	    /**
	     * 
	     * This method will return style for Normal Cell
	     * 
	     * @return
	     */
	    protected static CellStyle getNormalStyle(SXSSFWorkbook wb) {
	        CellStyle style = wb.createCellStyle();
//	        style.setBorderBottom(CellStyle.BORDER_THIN);
	        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
//	        style.setBorderLeft(CellStyle.BORDER_THIN);
	        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
//	        style.setBorderRight(CellStyle.BORDER_THIN);
	        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
//	        style.setBorderTop(CellStyle.BORDER_THIN);
	        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
//	        style.setAlignment(CellStyle.ALIGN_CENTER);
	        return style;
	    }

	
}


