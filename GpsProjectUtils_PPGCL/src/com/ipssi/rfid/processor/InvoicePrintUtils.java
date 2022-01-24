package com.ipssi.rfid.processor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.Misc;


public class InvoicePrintUtils {
	
	public static final int getCharPerLine1 = 96; //80 CPL at 10 CPI and 96 CPL at 12 CPI and 120 cpl at 15cpi
	public static final int docColonWith = 3;
	public static final int docAmountWidth = 8;
	public static final int docDateWidth = 10;
	public static final int docTimeWidth = 8;
	public static int getCharPerLine(int cpi){
		switch(cpi){
			case 10 : return 80;
			case 15 : return 120;
			default : return 96;
		}
	}
	public static int getLeftHalfWidth(int cpi){
		return (getCharPerLine(cpi)-3)/2;
	}
	public static int getRightHalfWidth(int cpi){
		return getCharPerLine(cpi)-getLeftHalfWidth(cpi)-3;
	}
	public static enum Alignment{
		Left,
		Top,
		Bottom,
		Right,
		Top_center,
		Bottom_center,
		Center,
	}
	public static enum Position{
		Relative,
		Absolute,
	}
	public static class PrintingDocument{
		private int documentWidth = 80;//no of character in line;
		private Position position;
		private int left;
		private int top;
		private Alignment align;
		private int height;
		private int width;
		private double percentheight;
		private double percentWidth;
		private Style style;
		
		private ArrayList<char[]> printLayout = null;
		private int currentRow = -1;
		private int currentColumn = -1;
		private int currentCoumnWidth = 0;
		private int currentColumnStart = 0;
		private int currentCharIndex = -1;
		
		public int getDocumentWidth() {
			return documentWidth;
		}
		public void setDocumentWidth(int documentWidth) {
			this.documentWidth = documentWidth;
		}
		public Position getPosition() {
			return position;
		}
		public void setPosition(Position position) {
			this.position = position;
		}
		public int getLeft() {
			return left;
		}
		public void setLeft(int left) {
			this.left = left;
		}
		public int getTop() {
			return top;
		}
		public void setTop(int top) {
			this.top = top;
		}
		public Alignment getAlign() {
			return align;
		}
		public void setAlign(Alignment align) {
			this.align = align;
		}
		public int getHeight() {
			return height;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public double getPercentheight() {
			return percentheight;
		}
		public void setPercentheight(double percentheight) {
			this.percentheight = percentheight;
		}
		public double getPercentWidth() {
			return percentWidth;
		}
		public void setPercentWidth(double percentWidth) {
			this.percentWidth = percentWidth;
		}
		public Style getStyle() {
			return style;
		}
		public void setStyle(Style style) {
			this.style = style;
		}
		public ArrayList<char[]> getPrintLayout() {
			return printLayout;
		}
		public void setPrintLayout(ArrayList<char[]> printLayout) {
			this.printLayout = printLayout;
		}
		public int getCurrentRow() {
			return currentRow;
		}
		public void setCurrentRow(int currentRow) {
			this.currentRow = currentRow;
		}
		public int getCurrentColumn() {
			return currentColumn;
		}
		public void setCurrentColumn(int currentColumn) {
			this.currentColumn = currentColumn;
		}
		public int getCurrentCoumnWidth() {
			return currentCoumnWidth;
		}
		public void setCurrentCoumnWidth(int currentCoumnWidth) {
			this.currentCoumnWidth = currentCoumnWidth;
		}
		public int getCurrentColumnStart() {
			return currentColumnStart;
		}
		public void setCurrentColumnStart(int currentColumnStart) {
			this.currentColumnStart = currentColumnStart;
		}
		public int getCurrentCharIndex() {
			return currentCharIndex;
		}
		public void setCurrentCharIndex(int currentCharIndex) {
			this.currentCharIndex = currentCharIndex;
		}
		public PrintingDocument(int cpi){
			this.documentWidth = getCharPerLine(cpi);
			this.printLayout = new ArrayList<char[]>();
		}
		public void addRow(){
			this.printLayout.add(new char[documentWidth]);
			currentRow++;
			currentColumn=-1;
			currentCharIndex=-1;
			currentColumnStart=0;
			currentCoumnWidth = 0;
		}
		public void addColumn(int columnWidth){
			currentColumn++;
			currentColumnStart += currentCoumnWidth;
			currentCharIndex = currentColumnStart;
			this.currentCoumnWidth = columnWidth;
		}
		
		public void addContent(String content,int paddingLeft, int paddingRight){
			if(content == null)
				return;
			if(paddingLeft < 0)
				paddingLeft = 0;
			if(paddingRight < 0)
				paddingRight = 0;
			int textIndex = 0;
			int paddingLength = paddingLeft + paddingRight;
			int contentFullLength = content.length() + paddingLength;
			if(contentFullLength <= (currentCoumnWidth-currentCharIndex)){
				char[] currentRowArray = this.printLayout.get(currentRow);
				for (int i=0; currentCharIndex < contentFullLength; i++,currentCharIndex++) {
					currentRowArray[currentCharIndex] = content.charAt(i);
				}
			}else{
				char[] currentRowArray = this.printLayout.get(currentRow);
				/*int remainingSpace = (currentCoumnWidth+currentColumnStart)-currentCharIndex;
				
				for (; currentCharIndex < (currentCoumnWidth+currentColumnStart) && textIndex < content.length(); textIndex++,currentCharIndex++) {
					currentRowArray[currentCharIndex] = content.charAt(textIndex);
				}
				
				int remainingTextLength = content.length() - remainingSpace;*/
				int remainingTextLength = content.length();
				while(remainingTextLength > 0){
					
				}
				int noOfRowReq = (int)Math.ceil((double)remainingTextLength/currentCoumnWidth);
				for (int i = 1; i <= noOfRowReq; i++) {
					currentCharIndex = currentColumnStart;
					if((currentRow+i) >= this.printLayout.size()){
						this.printLayout.add(new char[documentWidth]);
					}
					currentRowArray = this.printLayout.get(currentRow+i);
					for (; currentCharIndex < (currentCoumnWidth+currentColumnStart) && textIndex < content.length(); textIndex++,currentCharIndex++) {
						currentRowArray[currentCharIndex] = content.charAt(textIndex);
					}
				}
			}
		}
		public int addContent(char[] row,String content,int start, int length,int paddingLeft, int paddingRight){
			int contentFullLength = length+paddingLeft+paddingRight;
			if(row == null || contentFullLength > row.length)
				return length;
			int textIndex = start;
			int remainingLength = length;
			for (int i=0; currentCharIndex < (currentCoumnWidth+currentColumnStart) && i < length; textIndex++,currentCharIndex++) {
				row[currentCharIndex] = content.charAt(textIndex++);
				remainingLength--;
			}
			return remainingLength;
		}
		public void print() {
			if(this.printLayout != null){
				for (int i = 0; i < this.printLayout.size(); i++) {
					System.out.println(new String(printLayout.get(i)));
				}
			}
		}
	}
	public static class PrintTable extends PrintingDocument{
		ArrayList<PrintRow> header;
		ArrayList<PrintRow> body;
		public PrintTable(int cpi){
			super(cpi);
		}
	}
	public static class PrintRow extends PrintingDocument{
		ArrayList<PrintColumn> row;
		public PrintRow(int cpi){
			super(cpi);
		}
	}
	public static class PrintColumn extends PrintingDocument{
		private int rowSpan;
		private int colSpan;
		public PrintColumn(int cpi){
			super(cpi);
		}
		public int getRowSpan() {
			return rowSpan;
		}
		public void setRowSpan(int rowSpan) {
			this.rowSpan = rowSpan;
		}
		public int getColSpan() {
			return colSpan;
		}
		public void setColSpan(int colSpan) {
			this.colSpan = colSpan;
		}
		
	}
	public static class Style{
		private Box margin;
		private Box padding;
		private Alignment textAlign;
		private String fontFamily;
		private String fontSize;
		private String fontWeight;
		public Box getMargin() {
			return margin;
		}
		public void setMargin(Box margin) {
			this.margin = margin;
		}
		public Box getPadding() {
			return padding;
		}
		public void setPadding(Box padding) {
			this.padding = padding;
		}
		public Alignment getTextAlign() {
			return textAlign;
		}
		public void setTextAlign(Alignment textAlign) {
			this.textAlign = textAlign;
		}
		public String getFontFamily() {
			return fontFamily;
		}
		public void setFontFamily(String fontFamily) {
			this.fontFamily = fontFamily;
		}
		public String getFontSize() {
			return fontSize;
		}
		public void setFontSize(String fontSize) {
			this.fontSize = fontSize;
		}
		public String getFontWeight() {
			return fontWeight;
		}
		public void setFontWeight(String fontWeight) {
			this.fontWeight = fontWeight;
		}
		
	}

	public static class Box{
		private int left;
		private int top;
		private int bottom;
		private int right;
		public int getLeft() {
			return left;
		}
		public void setLeft(int left) {
			this.left = left;
		}
		public int getTop() {
			return top;
		}
		public void setTop(int top) {
			this.top = top;
		}
		public int getBottom() {
			return bottom;
		}
		public void setBottom(int bottom) {
			this.bottom = bottom;
		}
		public int getRight() {
			return right;
		}
		public void setRight(int right) {
			this.right = right;
		}
		
	}
	public static void print() {

	}
	public static String getAlignStr(String text, Alignment align){
		return getAlignStr(text, align, text != null ? text.length() : 0);
	}
	public static String getAlignStr(String text, Alignment align, int maxCharInline){
		if(text == null)
			text = "";
		text = text.trim();
		int textLength = text.length();
		int padStart = 0;
		int padEnd = 0;
		if(textLength > maxCharInline){
			//padEnd = textLength % maxCharInline;
		}else{
			if(align == Alignment.Left){
				padEnd = maxCharInline - textLength;
			}else if(align == Alignment.Right){
				padStart = maxCharInline - textLength;
			}else{
				padStart = (maxCharInline - textLength)/2;
				padEnd = maxCharInline -textLength- padStart;
			}
		}
		//System.out.println(align);
		//System.out.println(textLength);
		//System.out.println(padStart);
		//System.out.println(padEnd);
		for (int i = 0; i < padStart; i++) {
			text = " "+text;
		}
		for (int i = 0; i < padEnd; i++) {
			text = text + " ";
		}
		return text;
		//return textLength > 0 ?  text.substring(0, text.length() < (maxCharInline) ? text.length() : maxCharInline) : text;
	}
	public static String getline(int cpi){
		return getRepeatStr('-',getCharPerLine(cpi))+"\n";
	}
	public static String getRepeatStr(char c,int length){
		if(length <= 0)
			return "";
		char[] arr = new char[length];
		for (int i = 0; i < length; i++) {
			arr[i] = c;
		}
		return new String(arr);
	}
	public static void main(String[] arg){
		try{
			System.out.println(Math.round(1.5));
			System.out.println(Math.round(2.5));
			System.out.println(Math.round(1.4999));
			System.out.println(Math.round(2.4999));
			int cpi = 15;
			PrintingDocument p = new PrintingDocument(cpi);
			p.addRow();
			p.addColumn(18);
			p.addContent("Regd. office: Seepat Road Bilaspur (CG) Pin-495006",-1,-1);
			p.addColumn(18);
			p.addContent("Regd. office: Seepat Road Bilaspur (CG) Pin-495006",-1,-1);
			p.print();
			//FileWriter out =  null;
			{/*
				//try(
				FileWriter out = null;//new FileWriter("////192.168.5.100//TVSMSP240STAR");
				//){
				String doNumber = "";
				String doDate = "";
				String doExpiary = "";
				String deliveryPoint = "";
				String doQty = "";
				String balanceQty = "";
				String destination = "";
				String material = "";
				String coalSize = "";
				String grade = "";
				String typeOfConsumer = "";
				String grossWeight = "";
				String grossDateStr = "";
				String grossTimeStr = "";
				String tareWeight = "";
				String tareDateStr = "";
				String tareTimeStr = "";
				String netWeight = "";
				String coalRate = "";
				String coalValue = "";
				String sizingChargesRate = "";
				String sizingCharges = "";
				String SiloChargesRate = "";
				String SiloCharges = "";
				String stcChargesRate = "";
				String stcCharges = "";
				String dumpingChargesRate = "";
				String dumpingCharges = "";
				String royaltyChargesRate = "";
				String royaltyCharges = "";
				String dmfChargesRate = "";
				String dmfCharges = "";
				String nmetChargesRate = "";
				String nmetCharges = "";
				String stowingChargesRate = "";
				String stowingCharges = "";
				String terminalChargesRate = "";
				String terminalCharges = "";
				String forestCessRate = "";
				String forestCess = "";
				String otherTaxRate = "";
				String otherTax = "";
				String assessableAmountBlockA = "";
				String exciseRate = "";
				String exciseOnBlockA = "";
				String educationCessRate = "";
				String educationCessOnBlocakA = "";
				String sheduCessRate = "";
				String sheduCessOnBlockA = "";
				String totalExciseAndCessOnBlockA = "";
				String assessableAmountBlockB = "";
				String exciseOnBlockB = "";
				String educationCessOnBlocakB = "";
				String sheduCessOnBlockB = "";
				String totalExciseAndCessOnBlockB = "";
				String totalExciseAndCessPaybleOnBlockA = "";
				String totalExcisePaybleUnderProtest = "";
				String assessableAmountBlockAWord = "";
				String assessableAmountBlockBWord = "";
				String totalExciseAndCessOnBlockBWord = "";
				String totalExciseAndCessPaybleOnBlockAWord = "";
				String totalExcisePaybleAmount = "";
				String cinNumber = "";
				String challanNo = "";
				String workStationCode = "";
				String truckNo="";
				String projectName=""; 


				String gatePassNo = "";
				String minesRegNo = "";
				String consigneeName1 = "";
				String gatePassDate = "";
				String minesTinNo = "";
				String consigneeName2 = "";
				String minesName = "";
				String minesProduct1 = "";
				String timeOfRemoval = "";
				String authAddressRange = "";
				String minesProduct2 = "";
				String consigneeNameAdd1 = "";
				String transportationMode = "";
				String consigneeNameAdd2 = "";
				String minesProduct3 = "";
				String authAddressDivisionName = "";
				String consigneeNameAdd3 = "";
				String authAddressDivisionAdd1 = "";
				String consigneeNameAdd4 = "";
				String authAddressDivisionAdd2 = "";
				String authAddressDivisionAdd3 = "";
				String customerCode = "";
				String qtyLifted = "";
				long st = System.currentTimeMillis();
				String printableStr = getSampleData();getPrintableContent( truckNo, projectName, doNumber, doDate, doExpiary, deliveryPoint, 
						doQty, balanceQty, destination, material, coalSize, 
						grade, typeOfConsumer, grossWeight, grossDateStr, 
						grossTimeStr, tareWeight, tareDateStr, tareTimeStr, 
						netWeight, coalRate, coalValue, sizingChargesRate, 
						sizingCharges, SiloChargesRate, SiloCharges, stcChargesRate, 
						stcCharges, dumpingChargesRate, dumpingCharges, royaltyChargesRate, 
						royaltyCharges, dmfChargesRate, dmfCharges, nmetChargesRate, 
						nmetCharges, stowingChargesRate, stowingCharges, terminalChargesRate, 
						terminalCharges, forestCessRate, forestCess, otherTaxRate, otherTax, 
						assessableAmountBlockA, exciseRate, exciseOnBlockA, 
						educationCessRate, educationCessOnBlocakA, sheduCessRate, 
						sheduCessOnBlockA, totalExciseAndCessOnBlockA, assessableAmountBlockB, 
						exciseOnBlockB, educationCessOnBlocakB, sheduCessOnBlockB, 
						totalExciseAndCessOnBlockB, totalExciseAndCessPaybleOnBlockA, 
						totalExcisePaybleUnderProtest, assessableAmountBlockAWord, 
						assessableAmountBlockBWord, totalExciseAndCessOnBlockBWord, 
						totalExciseAndCessPaybleOnBlockAWord, totalExcisePaybleAmount,
						cinNumber, challanNo, workStationCode,gatePassNo,minesRegNo,consigneeName1,gatePassDate,
						minesTinNo,consigneeName2,minesName,minesProduct1,timeOfRemoval,authAddressRange,minesProduct2, minesProduct3,
						consigneeNameAdd1,transportationMode,consigneeNameAdd2,authAddressDivisionName,consigneeNameAdd3,authAddressDivisionAdd1,consigneeNameAdd4,authAddressDivisionAdd2,authAddressDivisionAdd3
						,customerCode,qtyLifted);
				//System.out.println(printableStr);
//				System.out.println(getSampleData());
				out.write(printableStr);
				out.flush();
				out.close();
				System.out.println("["+Thread.currentThread().toString()+"]Time Taken in printing :"+ ((System.currentTimeMillis()-st)));
			*/}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static final String DATE_FORMAT_DDMMYYYY_HHMM = "dd/MM/yyyy HH:mm";
	public static final String DATE_FORMAT_DDMMYYYY_HHMMSS = DATE_FORMAT_DDMMYYYY_HHMM+":ss";
	public static final String MYSQL_FORMAT_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT_DDMMYY = "dd-MM-yyyy";
	public static final String TIME_FORMAT_HHMMSS = "HH:mm:ss";
	private static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DDMMYY);
	private static SimpleDateFormat stf = new SimpleDateFormat(TIME_FORMAT_HHMMSS);
	
	public static String getDateStr(Date date){
		if(date == null)
			return "";
		return sdf.format(date);
	}
	public static String getTimeStr(Date date){
		if(date == null)
			return "";
		return stf.format(date);
	}
	public static String printDouble(double val){
		return printDouble(val, Misc.getUndefDouble());
	}
	public static String printDouble(double val,double undef){
		if((Misc.isUndef(val) && Misc.isUndef(undef)) )
			return "";
		return String.format("%.2f", val);
	}
	public static String printDoubleOneDecimal(double val,double undef){
		if((Misc.isUndef(val) && Misc.isUndef(undef)) )
			return "";
		return String.format("%.1f", val);
	}
	public static String printAmount(double val){
		if(Misc.isUndef(val) || val < 0.0)
			return "";
		return NumberToWordsConverter.Convert((int)val);
	}
	public static String[] splitStringByLength(String val,int length){
		if(Utils.isNull(val) || length <= 0 )
			return null;
		int size = val.length();
		int splitSize = (int)Math.ceil((double)size/length);
		String[] retval = new String[splitSize];
		int index = 0;
		for (int i = 0; i < splitSize; i++) {
			int st = index;
			int en = index + length;
			if(en > val.length())
				en = val.length();
			if(st >= en)
				break;
			retval[i] = val.substring(st, en);
			index = en; 
		}
		return retval;
	}
	public static String getStringAt(String[] val,int index){
		if(index < 0 || val == null || val.length <= index )
			return null;
		return val[index];
	}
	public static String print(int val){
		if(Misc.isUndef(val))
			return "";
		return val + "";
	}
	public static String print(double val){
		return printDouble(val);
	}
	public static String print(String val){
		if(Utils.isNull(val))
			return "";
		return val.trim();
	}
	
}
