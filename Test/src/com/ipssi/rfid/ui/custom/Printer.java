package com.ipssi.rfid.ui.custom;




import java.io.FileWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.Pair;
import com.ipssi.rfid.beans.CustomerDetails;
import com.ipssi.rfid.beans.DoDetails;
import com.ipssi.rfid.beans.DoDetails.LatestDOInfo;
import com.ipssi.rfid.beans.Mines;
import com.ipssi.rfid.beans.SECLWorkstationDetails;
import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.constant.Type;
import com.ipssi.rfid.processor.TokenManager;
import com.ipssi.rfid.processor.Utils;
import com.ipssi.rfid.ui.secl.controller.PropertyManager;
import com.ipssi.rfid.ui.secl.controller.PropertyManager.PropertyType;
import com.ipssi.rfid.ui.secl.controller.ScreenConstant;
import com.ipssi.rfid.ui.secl.data.LovDao;
import com.ipssi.rfid.ui.secl.data.LovDao.LovItemType;


public class Printer {	
	public static final int charPerLine = 96; //80 CPL at 10 CPI and 96 CPL at 12 CPI
	public static final int docColonWith = 3;
	public static final int docAmountWidth = 8;
	public static final int docDateWidth = 10;
	public static final int docTimeWidth = 8;
	public static final int leftHalfWidth = (charPerLine-3)/2;
	public static final int rightHalfWidth = charPerLine-leftHalfWidth-3;
	private static final String timeOfRemoval = null;
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
		
		public PrintingDocument(){
			this.documentWidth = charPerLine;
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
		private int addContent(char[] row,String content,int start, int length,int paddingLeft, int paddingRight){
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
			// TODO Auto-generated method stub
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
		public PrintTable(){
			super();
		}
	}
	public static class PrintRow extends PrintingDocument{
		ArrayList<PrintColumn> row;
		public PrintRow(){
			super();
		}
	}
	public static class PrintColumn extends PrintingDocument{
		private int rowSpan;
		private int colSpan;
		public PrintColumn(){
			super();
		}
	}
	public static class Style{
		private Box margin;
		private Box padding;
		private Alignment textAlign;
		private String fontFamily;
		private String fontSize;
		private String fontWeight;
	}

	public static class Box{
		private int left;
		private int top;
		private int bottom;
		private int right;
	}
	public static void print() {

	}
	public static String getAlignStr(String text, Alignment align){
		return getAlignStr(text, align, text != null ? text.length() : 0);
	}
	public static String getAlignStr(String text, Alignment align, int maxCharInline){
		if(text == null)
			return text;
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
	public static String getline(){
		return getRepeatStr('-',charPerLine)+"\n";
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
			
			/*PrintingDocument p = new PrintingDocument();
			p.addRow();
			p.addColumn(18);
			p.addContent("Regd. office: Seepat Road Bilaspur (CG) Pin-495006", null,-1,-1);
			p.addColumn(18);
			p.addContent("Regd. office: Seepat Road Bilaspur (CG) Pin-495006", null,-1,-1);
			p.print();*/
			
			//FileWriter out =  null;
			{
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
				String printableStr = getSampleData();/*getPrintableContent( truckNo, projectName, doNumber, doDate, doExpiary, deliveryPoint, 
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
						,customerCode,qtyLifted)*/;
				//System.out.println(printableStr);
//				System.out.println(getSampleData());
				out.write(printableStr);
				out.flush();
				out.close();
				System.out.println("Time Taken in printing :"+ ((System.currentTimeMillis()-st)));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	private static SimpleDateFormat sdf = new SimpleDateFormat(ScreenConstant.DATE_FORMAT_DDMMYY);
	private static SimpleDateFormat stf = new SimpleDateFormat(ScreenConstant.TIME_FORMAT_HHMMSS);
	
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
		if((Misc.isUndef(val) && Misc.isUndef(undef)) || (val < 0.0 && undef < 0.0))
			return "";
		return String.format("%.2f", val);
	}
	public static String printAmount(double val){
		if(Misc.isUndef(val) || val < 0.0)
			return "";
		return NumberToWordsConverter.Convert((int)val);
	}
	private static String[] splitStringByLength(String val,int length){
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
				en = val.length() - 1;
			if(st >= en)
				break;
			retval[i] = val.substring(st, en);
			index = en; 
		}
		return retval;
	}
	private static String getStringAt(String[] val,int index){
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
		return val;
	}
	private static String getExcisePrintData(Connection conn, int workstationType,SECLWorkstationDetails workStation,TPRecord tpr) throws Exception{
		int portNodeId = workStation.getPortNodeId();
		Date date = new Date(System.currentTimeMillis());
		String dateStr = getDateStr(date);
		String timeStr = getTimeStr(date);
		String vehicleName = Misc.getParamAsString(tpr.getVehicleName(),"");
		String challanNo = Misc.getParamAsString(tpr.getLrNo(),"");
		String challanDate = getDateStr(tpr.getChallanDate());
		String challanTime = getTimeStr(tpr.getChallanDate());
		String slipSerialNo = Misc.getParamAsString(tpr.getChallanNo(),"");//Misc.isUndef(tpr.getTprId()) ? "" :tpr.getTprId() +"";
		Mines mines = Mines.getMines(conn, tpr.getMinesCode(), Misc.getUndefInt());
		Pair<String,String> sourcePair = LovDao.getAutocompletePrintablePair(portNodeId, tpr.getMinesCode(), LovItemType.MINES);
		Pair<String,String> areaPair = mines != null ? LovDao.getAutocompletePrintablePair(portNodeId, mines.getParentAreaCode(), LovItemType.AREA) : null;
		Pair<String,String> subAreaPair = mines != null ? LovDao.getAutocompletePrintablePair(portNodeId, mines.getParentSubAreaCode(), LovItemType.SUB_AREA) : null;
		Pair<String,String> transporterPair = LovDao.getAutocompletePrintablePair(portNodeId, tpr.getTransporterCode(), LovItemType.TRANSPORTER);
		Pair<String,String> gradePair = LovDao.getAutocompletePrintablePair(portNodeId, tpr.getGradeCode(), LovItemType.MATERIAL_GRADE);
		Pair<String,String> destinationPair = null;
		CustomerDetails customer = null;
		String minesName = mines.getName();
		String areaName = areaPair == null ? "" : areaPair.second;
		String subAreaName = subAreaPair == null ? "" : subAreaPair.second;
		String slipName = "";
		String cinNumber = workStation.getCoorporateIdentityNumber();
		String workStationCode = workStation.getCode();
		String projectName= mines.getProjectName();
		String gatePassDate = challanDate;//to be think
		String minesTinNo = mines.getTinNumber();
		String minesRegNo = mines.getCentralExciseRegNo();
		String gatePassNo = "";
		String timeOfRemoval = "";
		String authAddressRange = mines.getAddressRange();
		String[] minesProduct = splitStringByLength(mines.getCentralExciseGoods(), charPerLine/6-1);
		String authAddressDivisionName = mines.getAddressDivision();
		String commissionerate = mines.getCommissionerate();

		String doNumber = "";
		String doDate = "";
		String doExpiary = "";
		String deliveryPoint = "";
		String customerCode = "";
		String[] consigneeName = null;
		String[] consigneeAddress = null;
		int transportationMode = Misc.getUndefInt();
		double doQty = Misc.getUndefDouble();
		double balanceQty = Misc.getUndefDouble();
		double qtyLifted = Misc.getUndefDouble();

		String material = "";
		String coalSize = "";
		String grade = "";
		int typeOfConsumer = Misc.getUndefInt();

		double coalRate = Misc.getUndefDouble();
		double coalValue = Misc.getUndefDouble();
		double tare = Misc.getUndefDouble();
		double gross = Misc.getUndefDouble();
		double net = Misc.getUndefDouble();
		String grossDateStr = "";
		String grossTimeStr = "";
		String tareDateStr = "";
		String tareTimeStr = "";
		double sizingChargesRate = Misc.getUndefDouble();
		double sizingCharges = Misc.getUndefDouble();
		double SiloChargesRate = Misc.getUndefDouble();
		double SiloCharges = Misc.getUndefDouble();
		double stcChargesRate = Misc.getUndefDouble();
		double stcCharges = Misc.getUndefDouble();
		double dumpingChargesRate = Misc.getUndefDouble();
		double dumpingCharges = Misc.getUndefDouble();
		double royaltyChargesRate = Misc.getUndefDouble();
		double royaltyCharges = Misc.getUndefDouble();
		double dmfChargesRate = Misc.getUndefDouble();
		double dmfCharges = Misc.getUndefDouble();
		double nmetChargesRate = Misc.getUndefDouble();
		double nmetCharges = Misc.getUndefDouble();
		double stowingChargesRate = Misc.getUndefDouble();
		double stowingCharges = Misc.getUndefDouble();
		double terminalChargesRate = Misc.getUndefDouble();
		double terminalCharges = Misc.getUndefDouble();
		double forestCessRate = Misc.getUndefDouble();
		double forestCess = Misc.getUndefDouble();
		double otherTaxRate = Misc.getUndefDouble();
		double otherTax = Misc.getUndefDouble();
		double assessableAmountBlockA = Misc.getUndefDouble();
		double exciseRate = Misc.getUndefDouble();
		double exciseOnBlockA = Misc.getUndefDouble();
		double educationCessRate = Misc.getUndefDouble();
		double educationCessOnBlocakA = Misc.getUndefDouble();
		double sheduCessRate = Misc.getUndefDouble();
		double sheduCessOnBlockA = Misc.getUndefDouble();
		double totalExciseAndCessOnBlockA = Misc.getUndefDouble();
		double assessableAmountBlockB = Misc.getUndefDouble();
		double exciseOnBlockB = Misc.getUndefDouble();
		double educationCessOnBlocakB = Misc.getUndefDouble();
		double sheduCessOnBlockB = Misc.getUndefDouble();
		double totalExciseAndCessOnBlockB = Misc.getUndefDouble();
		double totalExcisePaybleUnderProtest = Misc.getUndefDouble();
		double totalExcisePaybleAmount = Misc.getUndefDouble();
		StringBuilder sb = new StringBuilder();

		tare = Misc.isUndef(tpr.getLoadTare()) ? 0.0 : tpr.getLoadTare();
		gross = Misc.isUndef(tpr.getLoadGross()) ? 0.0 : tpr.getLoadGross();
		net =  Misc.isUndef(tpr.getLoadGross()) ||  Misc.isUndef(tpr.getLoadTare()) ? 0.0 : tpr.getLoadGross() - tpr.getLoadTare();
		grossDateStr = getDateStr(tpr.getLatestLoadWbOutExit());
		grossTimeStr = getTimeStr(tpr.getLatestLoadWbOutExit());
		tareDateStr = getDateStr(tpr.getLatestLoadWbInExit());
		tareTimeStr = getTimeStr(tpr.getLatestLoadWbInExit());

		slipName = "Road Weighment Slip";
		doNumber = tpr.getDoNumber();
		DoDetails doDetails = DoDetails.getDODetails(conn, doNumber, Misc.getUndefInt());
		if(doDetails != null){
			customer = CustomerDetails.getCustomer(conn, doDetails.getCustomerCode(), Misc.getUndefInt());
			destinationPair = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getDestinationCode(), LovItemType.ROAD_DEST);
			gradePair = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getGradeCode(), LovItemType.MATERIAL_GRADE);
			customerCode = customer != null ? customer.getSapCode() : null;
			consigneeName = splitStringByLength(Misc.getParamAsString(customer == null ? null : customer.getName(),""), charPerLine/6-1);
			consigneeAddress = splitStringByLength(Misc.getParamAsString(customer == null ? null : customer.getAddress(),""), charPerLine/6-1);
			transportationMode = doDetails.getTransportMode();
			doDate = getDateStr(doDetails.getDoDate());
			doExpiary = getDateStr(doDetails.getDoReleaseDate());
			deliveryPoint = Misc.getParamAsString(doDetails.getDeliveryPoint(),"");
			doQty = Misc.isUndef(doDetails.getQtyAlloc(null)) ? 0.0 : doDetails.getQtyAlloc(null);
			LatestDOInfo latestDoInfo = DoDetails.getLatestDOInfo(conn,  doNumber, workStation.getCode());
			double qtyAllocated = doDetails.getQtyAlloc(null);
			double liftedQty = latestDoInfo != null ? latestDoInfo.getLiftedQty() : Misc.getUndefDouble();
			balanceQty = doDetails.getTotQtyRemaining()+(Misc.isUndef(doDetails.getQtyAlreadyLifted()) ? 0.0 : doDetails.getQtyAlreadyLifted()); //Misc.isUndef(qtyAllocated) ? Misc.getUndefDouble() : qtyAllocated - ((Misc.isUndef(liftedQty) ? 0.0 : liftedQty) + (Misc.isUndef(doDetails.getQtyAlreadyLifted()) ? 0.0 : doDetails.getQtyAlreadyLifted()) );
			qtyLifted = Misc.isUndef(doQty) || Misc.isUndef(balanceQty) ? Misc.getUndefDouble() : doQty - balanceQty;
			material = Misc.getParamAsString(doDetails.getMaterial(),"");
			coalSize = Misc.getParamAsString(doDetails.getCoalSize(),"");
			grade = gradePair.second;
			typeOfConsumer = doDetails.getTypeOfConsumer();
			coalRate = Misc.isUndef(doDetails.getRate()) ? 0.0 : doDetails.getRate();
			coalValue = Misc.isUndef(net) ? 0.0 : (net)*doDetails.getRate();
			sizingChargesRate = Misc.isUndef(doDetails.getSizingCharge()) ? 0.0 : doDetails.getSizingCharge();
			sizingCharges = Misc.isUndef(net) ? 0.0 : (net)*doDetails.getSizingCharge();
			SiloChargesRate = Misc.isUndef(doDetails.getSiloCharge()) ? 0.0 : doDetails.getSiloCharge();
			SiloCharges = Misc.isUndef(net) ? 0.0 : (net)*doDetails.getSiloCharge();
			stcChargesRate = Misc.isUndef(doDetails.getStcCharge()) ? 0.0 : doDetails.getStcCharge();
			stcCharges = Misc.isUndef(net) ? 0.0 : (net)*doDetails.getStcCharge();
			dumpingChargesRate = Misc.isUndef(doDetails.getDumpingCharge()) ? 0.0 : doDetails.getDumpingCharge();
			dumpingCharges = Misc.isUndef(net) ? 0.0 : (net)*doDetails.getDumpingCharge();
			royaltyChargesRate = Misc.isUndef(doDetails.getRoyaltyCharge()) ? 0.0 : doDetails.getRoyaltyCharge();
			royaltyCharges = Misc.isUndef(net) ? 0.0 : (net)*doDetails.getRoyaltyCharge();
			dmfChargesRate = Misc.isUndef(mines.getDmfRate()) ? 0.0 : mines.getDmfRate();
			dmfCharges = Misc.isUndef(royaltyCharges) ? 0.0 : royaltyCharges* (mines.getDmfRate()/100);
			nmetChargesRate = Misc.isUndef(mines.getNmetRate()) ? 0.0 :  mines.getNmetRate();
			nmetCharges = Misc.isUndef(royaltyCharges) ? 0.0 : royaltyCharges* (mines.getNmetRate()/100);
			stowingChargesRate = Misc.isUndef(doDetails.getStowingEd()) ? 0.0 : doDetails.getStowingEd();
			stowingCharges = Misc.isUndef(net) ? 0.0 : (net)*doDetails.getStowingEd();
			terminalChargesRate = Misc.isUndef(doDetails.getTerminalCharge()) ? 0.0 : doDetails.getTerminalCharge();
			terminalCharges = Misc.isUndef(net) ? 0.0 : (net)*doDetails.getTerminalCharge();
			forestCessRate = Misc.isUndef(doDetails.getForestCess()) ? 0.0 : doDetails.getForestCess();
			forestCess = Misc.isUndef(net) ? 0.0 : (net)*doDetails.getForestCess();
			otherTaxRate = Misc.isUndef(doDetails.getaVap()) ? 0.0 : doDetails.getaVap();
			otherTax = Misc.isUndef(net) ? 0.0 : (net)*doDetails.getaVap();
			assessableAmountBlockA = coalValue + sizingCharges + SiloCharges + stcCharges + dumpingCharges;
			exciseRate = Misc.isUndef(mines.getExciseDutyRate()) ? 0.0 : mines.getExciseDutyRate();
			exciseOnBlockA = Misc.isUndef(assessableAmountBlockA) ? 0.0 : (exciseRate/100) * assessableAmountBlockA;
			educationCessRate = Misc.isUndef(mines.getEducationCessRate()) ? 0.0 : mines.getEducationCessRate();
			educationCessOnBlocakA = Misc.isUndef(assessableAmountBlockA) ? 0.0 : (educationCessRate/100) * assessableAmountBlockA;
			sheduCessRate = Misc.isUndef(mines.getHigherEducationCessRate()) ? 0.0 : mines.getHigherEducationCessRate();
			sheduCessOnBlockA = Misc.isUndef(assessableAmountBlockA) ? 0.0 : (sheduCessRate/100) * assessableAmountBlockA;
			totalExciseAndCessOnBlockA = exciseOnBlockA+educationCessOnBlocakA+sheduCessOnBlockA;
			assessableAmountBlockB = assessableAmountBlockA + royaltyCharges+dmfCharges+nmetCharges+stowingCharges+terminalCharges+forestCess+otherTax;
			exciseOnBlockB = Misc.isUndef(assessableAmountBlockB) ? 0.0 : (exciseRate/100) * assessableAmountBlockB;
			educationCessOnBlocakB = Misc.isUndef(assessableAmountBlockB) ? 0.0 : (educationCessRate/100) * assessableAmountBlockB;
			sheduCessOnBlockB = Misc.isUndef(assessableAmountBlockB) ? 0.0 : (sheduCessRate/100) * assessableAmountBlockB;
			totalExciseAndCessOnBlockB = exciseOnBlockB+educationCessOnBlocakB+sheduCessOnBlockB;
			totalExcisePaybleUnderProtest = totalExciseAndCessOnBlockB - totalExciseAndCessOnBlockA;
			totalExcisePaybleAmount = totalExciseAndCessOnBlockA+totalExciseAndCessOnBlockB+totalExcisePaybleUnderProtest;

		}
		//to do
		sb.append("\n");
		sb.append(getAlignStr("SOUTH EASTERN COALFIELD LIMITED", Alignment.Center,charPerLine)+"\n");
		sb.append(getAlignStr("CIN:"+print(cinNumber), Alignment.Center,charPerLine)+"\n");
		sb.append(getAlignStr("Invoice - Cum - Challan", Alignment.Center,charPerLine)+"\n");
		sb.append(getAlignStr("Issued Under Rule 11 of Excise 2002 For Removal of Excisable Goods", Alignment.Center,charPerLine)+"\n");
		sb.append("\n");
		sb.append(getAlignStr("Weighment Serial No. : ", Alignment.Left));
		sb.append(getAlignStr(print(tpr.getTprId()), Alignment.Left,charPerLine-(23+15+3+5)));
		sb.append(getAlignStr("Weighbridge No.", Alignment.Right,15));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(workStation.getCode()), Alignment.Left,5)+"\n");

		sb.append(getline());

		sb.append(getAlignStr("Regd. Office:", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Work :", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Central Excise", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Name & Add", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Mining Pass", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Authenticated", Alignment.Left,charPerLine/6)+"\n");

		sb.append(getAlignStr("Seepat Road", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Name & Add Of", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Registation No", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Of Consignee:", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("No.:", Alignment.Left));
		sb.append(getAlignStr(print(challanNo)+"", Alignment.Left,(charPerLine/6)-4));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6)+"\n");

		sb.append(getAlignStr("Bilaspur (CG)", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Source Mine &", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(mines.getCentralExciseRegNo()), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(getStringAt(consigneeName, 0)), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Date:", Alignment.Left));
		sb.append(getAlignStr(getDateStr(tpr.getLatestLoadGateInExit()), Alignment.Left,(charPerLine/6)-5));
		sb.append(getAlignStr("Auth. Signatory", Alignment.Left,charPerLine/6)+"\n");

		sb.append(getAlignStr("Pin-495006", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Tin.", Alignment.Left));
		sb.append(getAlignStr(print(mines.getTinNumber()), Alignment.Left,(charPerLine/6)-4));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(getStringAt(consigneeName, 1), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Time Of Removal", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Address Range", Alignment.Left,charPerLine/6)+"\n");

		sb.append(getAlignStr("Phone No:", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(mines.getName()), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(getStringAt(minesProduct,0)), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(timeOfRemoval), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(mines.getAddressRange()), Alignment.Left,charPerLine/6)+"\n");

		sb.append(getAlignStr("Fax No:", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(getStringAt(minesProduct,1)), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(getStringAt(consigneeAddress,0)), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Mode : ", Alignment.Left));
		sb.append(getAlignStr(print(Type.MinesDoDetails.TransPortMode.getString(transportationMode)), Alignment.Left,(charPerLine/6)-7));
		sb.append(getAlignStr("Address Division", Alignment.Left,charPerLine/6)+"\n");

		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(getStringAt(minesProduct,2)), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(getStringAt(consigneeAddress,1)), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(authAddressDivisionName), Alignment.Left,charPerLine/6)+"\n");

		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(getStringAt(consigneeAddress,2)), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("Commissionerate", Alignment.Left,charPerLine/6)+"\n");

		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(getStringAt(consigneeAddress,3)), Alignment.Left,charPerLine/6));
		sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
		sb.append(getAlignStr(print(commissionerate), Alignment.Left,charPerLine/6)+"\n");
		sb.append(getline());

		sb.append(getAlignStr("DO No. & Date", Alignment.Center,charPerLine/6));
		sb.append(getAlignStr("DO Valid", Alignment.Center,charPerLine/6));
		sb.append(getAlignStr("Delivery", Alignment.Center,charPerLine/6));
		sb.append(getAlignStr("Do Qty", Alignment.Center,charPerLine/6));
		sb.append(getAlignStr("Balance Qty", Alignment.Center,charPerLine/6));
		sb.append(getAlignStr("Customer Code", Alignment.Center,charPerLine/6)+"\n");

		sb.append(getAlignStr(print(doNumber), Alignment.Center,charPerLine/6));
		sb.append(getAlignStr("Upto", Alignment.Center,charPerLine/6));
		sb.append(getAlignStr("Point", Alignment.Center,charPerLine/6));
		sb.append(getAlignStr(print(doQty), Alignment.Center,charPerLine/6));
		sb.append(getAlignStr("to be lifted", Alignment.Center,charPerLine/6));
		sb.append(getAlignStr(print(customerCode), Alignment.Center,charPerLine/6)+"\n");

		sb.append(getAlignStr(print(doDate), Alignment.Center,charPerLine/6));
		sb.append(getAlignStr(print(doExpiary), Alignment.Center,charPerLine/6));
		sb.append(getAlignStr(deliveryPoint, Alignment.Center,charPerLine/6));
		sb.append(getAlignStr("", Alignment.Center,charPerLine/6));
		sb.append(getAlignStr(print(balanceQty), Alignment.Center,charPerLine/6));
		sb.append(getAlignStr("Prog.", Alignment.Left));
		sb.append(getAlignStr(print(qtyLifted), Alignment.Right,charPerLine/6-(5+2)));
		sb.append(getAlignStr("MT", Alignment.Left)+"\n");

		sb.append(getline());

		sb.append(getAlignStr("Destination", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(destinationPair.second), Alignment.Left,rightHalfWidth)+"\n");

		sb.append(getAlignStr("Sepecification of Goods", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(material), Alignment.Left,rightHalfWidth)+"\n");

		sb.append(getAlignStr("Size", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(coalSize), Alignment.Left,rightHalfWidth)+"\n");

		sb.append(getAlignStr("Grade", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(grade), Alignment.Left,rightHalfWidth)+"\n");

		sb.append(getAlignStr("Type Of Consumer", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(Type.MinesDoDetails.TypeOfConsumer.getString(typeOfConsumer)), Alignment.Left,rightHalfWidth)+"\n");

		sb.append(getAlignStr("Gross Weight", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(gross), Alignment.Left,docAmountWidth));
		sb.append(getAlignStr(" Date", Alignment.Left));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(grossDateStr), Alignment.Left,docDateWidth));
		sb.append(getAlignStr(" Time", Alignment.Left));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(grossTimeStr), Alignment.Left,docTimeWidth)+"\n");

		sb.append(getAlignStr("Tare Weight", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(tare), Alignment.Left,docAmountWidth));
		sb.append(getAlignStr(" Date", Alignment.Left));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(tareDateStr), Alignment.Left,docDateWidth));
		sb.append(getAlignStr(" Time", Alignment.Left));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(tareTimeStr), Alignment.Left,docTimeWidth)+"\n");

		sb.append(getAlignStr("Net Weight", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(net), Alignment.Left,rightHalfWidth)+"\n");

		sb.append(getAlignStr("Rate", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(coalRate), Alignment.Right,docAmountWidth));
		sb.append(getAlignStr(" / MT", Alignment.Left)+"\n");

		sb.append(getAlignStr(" 1. Coal Value", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(coalValue), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" 2. Sizing Charges", Alignment.Left,leftHalfWidth-12));
		sb.append(getAlignStr(print(sizingChargesRate), Alignment.Left,7));
		sb.append(getAlignStr(" / MT", Alignment.Left));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(sizingCharges), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" 3. SILO Charges", Alignment.Left,leftHalfWidth-12));
		sb.append(getAlignStr(print(SiloChargesRate), Alignment.Left,7));
		sb.append(getAlignStr(" / MT", Alignment.Left,5));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(SiloCharges), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" 4. STC Charge", Alignment.Left,leftHalfWidth-12));
		sb.append(getAlignStr(print(stcChargesRate), Alignment.Left,7));
		sb.append(getAlignStr(" / MT", Alignment.Left,5));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(stcCharges), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" 5. Dumping Charge", Alignment.Left,leftHalfWidth-12));
		sb.append(getAlignStr(print(dumpingChargesRate), Alignment.Left,7));
		sb.append(getAlignStr(" / MT", Alignment.Left,5));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(dumpingCharges), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" 6. Royalty", Alignment.Left,leftHalfWidth-12));
		sb.append(getAlignStr(print(royaltyChargesRate), Alignment.Left,7));
		sb.append(getAlignStr(" / MT", Alignment.Left,5));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(royaltyCharges), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" 7. DMF", Alignment.Left,leftHalfWidth-(2+5+16)));
		sb.append(getAlignStr("@ ", Alignment.Left,2));
		sb.append(getAlignStr(print(dmfChargesRate), Alignment.Left,5));
		sb.append(getAlignStr(" % of 6 above", Alignment.Left,16));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(dmfCharges), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" 8. NMET", Alignment.Left,leftHalfWidth-(2+5+16)));
		sb.append(getAlignStr("@ ", Alignment.Left,2));
		sb.append(getAlignStr(print(nmetChargesRate), Alignment.Left,5));
		sb.append(getAlignStr(" % of 6 above", Alignment.Left,16));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(nmetCharges), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" 9. Stowing Excise Duty", Alignment.Left,leftHalfWidth-(5+5+2)));
		sb.append(getAlignStr(print(stowingChargesRate), Alignment.Left,7));
		sb.append(getAlignStr(" / MT", Alignment.Left,5));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(stowingCharges), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr("10. Terminal Tax", Alignment.Left,leftHalfWidth-(5+5+2)));
		sb.append(getAlignStr(print(terminalChargesRate), Alignment.Left,7));
		sb.append(getAlignStr(" / MT", Alignment.Left,5));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(terminalCharges), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr("11. Forest Cess", Alignment.Left,leftHalfWidth-(5+5+2)));
		sb.append(getAlignStr(print(forestCessRate), Alignment.Left,7));
		sb.append(getAlignStr(" / MT", Alignment.Left,5));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(forestCess), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr("12. MP Sadak Tax / CG", Alignment.Left,80)+"\n");
		sb.append(getAlignStr("    Paryavaran & Vikas Upkar", Alignment.Left,leftHalfWidth-(5+5+2)));
		sb.append(getAlignStr(print(otherTaxRate), Alignment.Left,7));
		sb.append(getAlignStr(" / MT", Alignment.Left,5));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(otherTax), Alignment.Right,docAmountWidth)+"\n");

		sb.append("\n");

		sb.append(getAlignStr("       Assessable Amount(A) [1 to 5]", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(assessableAmountBlockA), Alignment.Right,docAmountWidth));
		sb.append(getAlignStr("("+print(NumberToWordsConverter.Convert((int)assessableAmountBlockA))+")", Alignment.Left,rightHalfWidth-docAmountWidth)+"\n");

		sb.append("\n");

		sb.append(getAlignStr(" Excise Duty", Alignment.Left,leftHalfWidth-(4+18)));
		sb.append(getAlignStr(print(exciseRate), Alignment.Right,4));
		sb.append(getAlignStr(" % on (A)", Alignment.Left,18));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(exciseOnBlockA), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" Education Cess", Alignment.Left,leftHalfWidth-(4+18)));
		sb.append(getAlignStr(print(educationCessRate), Alignment.Right,4));
		sb.append(getAlignStr(" %", Alignment.Left,18));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(educationCessOnBlocakA), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" SHEDU Cess", Alignment.Left,leftHalfWidth-(4+18)));
		sb.append(getAlignStr(print(sheduCessRate), Alignment.Right,4));
		sb.append(getAlignStr(" %", Alignment.Left,18));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(sheduCessOnBlockA), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" Excise Duty & Cess on (Total)(A)", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(totalExciseAndCessOnBlockA), Alignment.Right,docAmountWidth)+"\n");

		sb.append("\n");

		sb.append(getAlignStr("   New Assessable Amount(B) [1 to 12]", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(assessableAmountBlockB), Alignment.Right,docAmountWidth));
		sb.append(getAlignStr("("+print(NumberToWordsConverter.Convert((int)assessableAmountBlockB))+")", Alignment.Left,rightHalfWidth-docAmountWidth)+"\n");

		sb.append("\n");

		sb.append(getAlignStr(" Excise Duty", Alignment.Left,leftHalfWidth-(4+18)));
		sb.append(getAlignStr(print(exciseRate), Alignment.Right,4));
		sb.append(getAlignStr(" % on (B)", Alignment.Left,18));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(exciseOnBlockB), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" Education Cess", Alignment.Left,leftHalfWidth-(4+18)));
		sb.append(getAlignStr(print(educationCessRate), Alignment.Right,4));
		sb.append(getAlignStr(" %", Alignment.Left,18));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(educationCessOnBlocakB), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" SHEDU Cess", Alignment.Left,leftHalfWidth-(4+18)));
		sb.append(getAlignStr(print(educationCessRate), Alignment.Right,4));
		sb.append(getAlignStr(" %", Alignment.Left,18));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(sheduCessOnBlockB), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getAlignStr(" Excise Duty & Cess on (Total)(B)", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(totalExciseAndCessOnBlockB), Alignment.Right,docAmountWidth));
		sb.append(getAlignStr("("+print(NumberToWordsConverter.Convert((int)totalExciseAndCessOnBlockB))+")", Alignment.Left,rightHalfWidth-docAmountWidth)+"\n");

		sb.append(getAlignStr(" Excise Duty & Cess on Payble(A)", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(totalExciseAndCessOnBlockA), Alignment.Right,docAmountWidth));
		sb.append(getAlignStr("("+print(NumberToWordsConverter.Convert((int)totalExciseAndCessOnBlockA))+")", Alignment.Left,rightHalfWidth-docAmountWidth)+"\n");

		sb.append(getAlignStr(" Excise Duty Payble UNDER PROTEST(B-A)", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr(":", Alignment.Center,docColonWith));
		sb.append(getAlignStr(print(totalExcisePaybleUnderProtest), Alignment.Right,docAmountWidth));
		sb.append(getAlignStr("(incl. cess) against Assessable value of", Alignment.Left)+"\n");

		sb.append(getAlignStr("", Alignment.Left,leftHalfWidth));
		sb.append(getAlignStr("Rs.", Alignment.Center));
		sb.append(getAlignStr(print(totalExcisePaybleAmount), Alignment.Right,docAmountWidth)+"\n");

		sb.append(getline());
		sb.append(getAlignStr("**Certified that particulars given above are true and correct. ", Alignment.Left,charPerLine - (16+16)));
		sb.append(getAlignStr("Truck No.", Alignment.Center,16));
		sb.append(getAlignStr("For "+projectName, Alignment.Center,16)+"\n");
		sb.append(getAlignStr(" ", Alignment.Left,charPerLine - (16+16)));
		sb.append(getAlignStr(print(vehicleName), Alignment.Center,16));
		sb.append(getAlignStr("project", Alignment.Right,16)+"\n");
		sb.append("\n");
		sb.append("\n");
		sb.append(getAlignStr("(Authorised Signatory)", Alignment.Right,charPerLine)+"\n");
		sb.append(getline());
		return sb.toString();
	}
	public static boolean print(int workstationType,SECLWorkstationDetails workStation,TPRecord tpr){
		Connection conn = null;
		boolean destroyIt = false;
		try{
			conn = DBConnectionPool.getConnectionFromPoolNonWeb();
			int portNodeId = workStation.getPortNodeId();
			Date date = new Date(System.currentTimeMillis());
			String dateStr = getDateStr(date);
			String timeStr = getTimeStr(date);
			String vehicleName = Misc.getParamAsString(tpr.getVehicleName(),"");
			String challanNo = Misc.getParamAsString(tpr.getChallanNo(),"");
			String challanDate = getDateStr(tpr.getChallanDate());
			String challanTime = getTimeStr(tpr.getChallanDate());
			String slipSerialNo = Misc.isUndef(tpr.getTprId()) ? "" :tpr.getTprId() +"";
			Mines mines = Mines.getMines(conn, tpr.getMinesCode(), Misc.getUndefInt());
			Pair<String,String> sourcePair = LovDao.getAutocompletePrintablePair(portNodeId, tpr.getMinesCode(), LovItemType.MINES);
			Pair<String,String> areaPair = mines != null ? LovDao.getAutocompletePrintablePair(portNodeId, mines.getParentAreaCode(), LovItemType.AREA) : null;
			Pair<String,String> subAreaPair = mines != null ? LovDao.getAutocompletePrintablePair(portNodeId, mines.getParentSubAreaCode(), LovItemType.SUB_AREA) : null;
			Pair<String,String> transporterPair = LovDao.getAutocompletePrintablePair(portNodeId, tpr.getTransporterCode(), LovItemType.TRANSPORTER);
			Pair<String,String> gradePair = LovDao.getAutocompletePrintablePair(portNodeId, tpr.getGradeCode(), LovItemType.MATERIAL_GRADE);
			Pair<String,String> destinationPair = null;
			Pair<String,String> customerPair = null;
			String minesName = mines.getName();
			String areaName = areaPair == null ? "" : areaPair.second;
			String subAreaName = subAreaPair == null ? "" : subAreaPair.second;
			String slipName = "";
			String workStationCode = workStation.getCode();
			double tare = Misc.getUndefDouble();
			double gross = Misc.getUndefDouble();
			double net = Misc.getUndefDouble();
			String grossDateStr = "";
			String grossTimeStr = "";
			String tareDateStr = "";
			String tareTimeStr = "";
			
			String doNumber = tpr.getDoNumber();
			String doDate = "";
			String doExpiary = "";
			String deliveryPoint = "";
			double doQty = Misc.getUndefDouble();
			double balanceQty = Misc.getUndefDouble();
			double qtyLifted = Misc.getUndefDouble();

			StringBuilder sb = new StringBuilder();
			if(workstationType == Type.WorkStationType.SECL_LOAD_ROAD_WB_GROSS){
				sb.append(getExcisePrintData(conn, workstationType, workStation, tpr));
			}else{
				if(workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_GROSS 
						|| workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_TARE
						|| workstationType == Type.WorkStationType.SECL_LOAD_ROAD_WB_TARE
						|| workstationType == Type.WorkStationType.SECL_LOAD_WASHERY_TARE
						|| workstationType == Type.WorkStationType.SECL_LOAD_WASHERY_GROSS
						){
					tare = Misc.isUndef(tpr.getLoadTare()) ? 0.0 : tpr.getLoadTare();
					gross = Misc.isUndef(tpr.getLoadGross()) ? 0.0 : tpr.getLoadGross();
					net =  Misc.isUndef(tpr.getLoadGross()) ||  Misc.isUndef(tpr.getLoadTare()) ? 0.0 : tpr.getLoadGross() - tpr.getLoadTare();
					grossDateStr = getDateStr(tpr.getLatestLoadWbOutExit());
					grossTimeStr = getTimeStr(tpr.getLatestLoadWbOutExit());
					tareDateStr = getDateStr(tpr.getLatestLoadWbInExit());
					tareTimeStr = getTimeStr(tpr.getLatestLoadWbInExit());
					if(workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_TARE || workstationType == Type.WorkStationType.SECL_LOAD_INT_WB_GROSS){
						slipName = "Internal Shiting Weighment Slip";
						destinationPair = LovDao.getAutocompletePrintablePair(portNodeId, tpr.getDestinationCode(), LovItemType.SIDING);
					}else {
						slipName = "Road Weigment Slip";
						DoDetails doDetails = DoDetails.getDODetails(conn, doNumber, Misc.getUndefInt());
						if(doDetails != null){
							customerPair = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getCustomerCode(), LovItemType.CUSTOMER);
							destinationPair = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getDestinationCode(), LovItemType.ROAD_DEST);
							gradePair = LovDao.getAutocompletePrintablePair(portNodeId, doDetails.getGradeCode(), LovItemType.MATERIAL_GRADE);
							doDate = getDateStr(doDetails.getDoDate());
							doExpiary = getDateStr(doDetails.getDoReleaseDate());
							deliveryPoint = Misc.getParamAsString(doDetails.getDeliveryPoint(),"");
							doQty = Misc.isUndef(doDetails.getQtyAlloc(null)) ? 0.0 : doDetails.getQtyAlloc(null);
							LatestDOInfo latestDoInfo = DoDetails.getLatestDOInfo(conn,  doNumber, workStation.getCode());
							double qtyAllocated = doDetails.getQtyAlloc(null);
							double liftedQty = latestDoInfo != null ? latestDoInfo.getLiftedQty() : Misc.getUndefDouble();
							balanceQty = doDetails.getTotQtyRemaining()+(Misc.isUndef(doDetails.getQtyAlreadyLifted()) ? 0.0 : doDetails.getQtyAlreadyLifted());
							//balanceQty = Misc.isUndef(qtyAllocated) ? Misc.getUndefDouble() : qtyAllocated - ((Misc.isUndef(liftedQty) ? 0.0 : liftedQty) + (Misc.isUndef(doDetails.getQtyAlreadyLifted()) ? 0.0 : doDetails.getQtyAlreadyLifted()) );
							qtyLifted = Misc.isUndef(doQty) || Misc.isUndef(balanceQty) ? Misc.getUndefDouble() : doQty - balanceQty;
						}
					}
				}else if(workstationType == Type.WorkStationType.SECL_UNLOAD_INT_WB_GROSS || workstationType == Type.WorkStationType.SECL_UNLOAD_INT_WB_TARE){
					slipName = "Siding Weighment Slip";
					destinationPair = LovDao.getAutocompletePrintablePair(portNodeId, tpr.getDestinationCode(), LovItemType.SIDING);
					tare = Misc.isUndef(tpr.getUnloadTare()) ? 0.0 : tpr.getUnloadTare();
					gross = Misc.isUndef(tpr.getUnloadGross()) ? 0.0 : tpr.getUnloadGross();
					net =  Misc.isUndef(tpr.getUnloadGross()) ||  Misc.isUndef(tpr.getUnloadTare()) ? 0.0 : tpr.getUnloadGross() - tpr.getUnloadTare();
					grossDateStr = getDateStr(tpr.getLatestUnloadWbInExit());
					grossTimeStr = getTimeStr(tpr.getLatestUnloadWbInExit());
					tareDateStr = getDateStr(tpr.getLatestUnloadWbOutExit());
					tareTimeStr = getTimeStr(tpr.getLatestUnloadWbOutExit());
				}
				
				//to do 
				sb.append(getAlignStr("South Eastern CoaldField Ltd.", Alignment.Center,charPerLine)+"\n");
				sb.append(getAlignStr(minesName, Alignment.Center,charPerLine)+"\n");
				sb.append(getAlignStr(workStationCode, Alignment.Center,charPerLine)+"\n");
				sb.append(getline());
				sb.append(getAlignStr("Date", Alignment.Right));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(dateStr, Alignment.Left));
				sb.append(getAlignStr(slipName, Alignment.Center,charPerLine-(4+4+docDateWidth+docTimeWidth+2*docColonWith)));
				sb.append(getAlignStr("Time", Alignment.Right));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(timeStr, Alignment.Left));
				sb.append(getline());
				if(!Utils.isNull(doNumber)){
					sb.append(getAlignStr("DO No.", Alignment.Right));
					sb.append(getAlignStr(":", Alignment.Center,docColonWith));
					sb.append(getAlignStr(print(doNumber), Alignment.Left,(charPerLine/3)-(6+docColonWith)));
					sb.append(getAlignStr("DO Date.", Alignment.Right));
					sb.append(getAlignStr(":", Alignment.Center,docColonWith));
					sb.append(getAlignStr(print(doDate), Alignment.Left,(charPerLine/3)-(8+docColonWith)));
					sb.append(getAlignStr("Validity Date.", Alignment.Right));
					sb.append(getAlignStr(":", Alignment.Center,docColonWith));
					sb.append(getAlignStr(print(doExpiary), Alignment.Left));
					sb.append("\n");
				}
				sb.append(getAlignStr("Truck No", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(vehicleName), Alignment.Left,charPerLine/6));
				sb.append(getAlignStr("",Alignment.Right,charPerLine/3));
				sb.append(getAlignStr("slip No", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(slipSerialNo), Alignment.Left));
				sb.append("\n");
				if(customerPair != null){
					sb.append(getAlignStr("Customer Code", Alignment.Right,charPerLine/6-docColonWith));
					sb.append(getAlignStr(":", Alignment.Center,docColonWith));
					sb.append(getAlignStr(print(customerPair.first), Alignment.Left,charPerLine/6));
					sb.append(getAlignStr("Customer Name", Alignment.Right,charPerLine/6-docColonWith));
					sb.append(getAlignStr(":", Alignment.Center,docColonWith));
					sb.append(getAlignStr(print(customerPair.second),Alignment.Left));
					sb.append("\n");
				}
				sb.append(getAlignStr("Trans. Code", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(transporterPair == null ? "" : transporterPair.first), Alignment.Left,charPerLine/6));
				sb.append(getAlignStr("Trans. Name", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(transporterPair == null ? "" : transporterPair.second),Alignment.Left));
				sb.append("\n");
				sb.append(getAlignStr("Grade Code", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(gradePair == null ? "" : gradePair.first), Alignment.Left,charPerLine/6));
				sb.append(getAlignStr("Grade Name", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(gradePair == null ? "" : gradePair.second),Alignment.Left));
				sb.append("\n");
				sb.append(getAlignStr("From Code", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(sourcePair == null ? "" : sourcePair.first), Alignment.Left,charPerLine/6));
				sb.append(getAlignStr("From Name", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(sourcePair == null ? "" : sourcePair.second),Alignment.Left));
				sb.append("\n");
				sb.append(getAlignStr("To Code", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(destinationPair == null ? "" : destinationPair.first), Alignment.Left,charPerLine/6));
				sb.append(getAlignStr("To Name", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(destinationPair == null ? "" : destinationPair.second),Alignment.Left));
				sb.append("\n");
				
				sb.append(getAlignStr("Tare", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(tare), Alignment.Left,charPerLine/6));
				sb.append(getAlignStr("Date", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(tareDateStr), Alignment.Left,charPerLine/6));
				sb.append(getAlignStr("Time", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(tareTimeStr), Alignment.Left,charPerLine/6));
				sb.append("\n");
				sb.append(getAlignStr("Gross", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(gross), Alignment.Left,charPerLine/6));
				sb.append(getAlignStr("Date", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(grossDateStr), Alignment.Left,charPerLine/6));
				sb.append(getAlignStr("Time", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(grossTimeStr), Alignment.Left,charPerLine/6));
				sb.append("\n");
				sb.append(getAlignStr("Net", Alignment.Right,charPerLine/6-docColonWith));
				sb.append(getAlignStr(":", Alignment.Center,docColonWith));
				sb.append(getAlignStr(print(net), Alignment.Left,charPerLine/6));
				sb.append("\n");
				sb.append(getline());
				sb.append(getAlignStr("Signed By", Alignment.Left,leftHalfWidth));
				sb.append(getAlignStr("Checked By", Alignment.Right,rightHalfWidth+docColonWith));
				sb.append("\n");
			}
			if(TokenManager.isDebug)
				System.out.println(sb.toString());
			String printerStr = PropertyManager.getPropertyVal(PropertyType.System, "PRINTER_ADDR");
			if(!Utils.isNull(printerStr)){
				FileWriter out = new FileWriter(printerStr);//new FileWriter("////192.168.5.100//TVSMSP240STA");
				out.write(sb.toString());
				out.flush();
				out.close();
			}
			return true;
		}catch(Exception ex){
			destroyIt = true;
			ex.printStackTrace();
		}finally{
			try{
				DBConnectionPool.returnConnectionToPoolNonWeb(conn,destroyIt);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public static String getPrintableContent(String truckNo,String projectName, String doNumber, String doDate, String doExpiary, String deliveryPoint, 
									String doQty, String balanceQty, String destination, String material, String coalSize, 
									String grade, String typeOfConsumer, String grossWeight, String grossDateStr, 
									String grossTimeStr, String tareWeight, String tareDateStr, String tareTimeStr, 
									String netWeight, String coalRate, String coalValue, String sizingChargesRate, 
									String sizingCharges, String SiloChargesRate, String SiloCharges, String stcChargesRate, 
									String stcCharges, String dumpingChargesRate, String dumpingCharges, String royaltyChargesRate, 
									String royaltyCharges, String dmfChargesRate, String dmfCharges, String nmetChargesRate, 
									String nmetCharges, String stowingChargesRate, String stowingCharges, String terminalChargesRate, 
									String terminalCharges, String forestCessRate, String forestCess, String otherTaxRate, String otherTax, 
									String assessableAmountBlockA, String exciseRate, String exciseOnBlockA, 
									String educationCessRate, String educationCessOnBlocakA, String sheduCessRate, 
									String sheduCessOnBlockA, String totalExciseAndCessOnBlockA, String assessableAmountBlockB, 
									String exciseOnBlockB, String educationCessOnBlocakB, String sheduCessOnBlockB, 
									String totalExciseAndCessOnBlockB, String totalExciseAndCessPaybleOnBlockA, 
									String totalExcisePaybleUnderProtest, String assessableAmountBlockAWord, 
									String assessableAmountBlockBWord, String totalExciseAndCessOnBlockBWord, 
									String totalExciseAndCessPaybleOnBlockAWord, String totalExcisePaybleAmount,
									String cinNumber,String challanNo, String workStationCode,
									String gatePassNo,String minesRegNo,String consigneeName1,
									String gatePassDate,String minesTinNo,String consigneeName2,
									String minesName,String minesProduct1,String timeOfRemoval,
									String authAddressRange,String minesProduct2, String minesProduct3,String consigneeNameAdd1,
									String transportationMode,String consigneeNameAdd2,String authAddressDivisionName,
									String consigneeNameAdd3,String authAddressDivisionAdd1,String consigneeNameAdd4,
									String authAddressDivisionAdd2,String authAddressDivisionAdd3,String customerCode, String qtyLifted){
		StringBuilder sb = new StringBuilder();
			sb.append("\n");
			sb.append(getAlignStr("SOUTH EASTERN COALFIELD LIMITED", Alignment.Center,charPerLine)+"\n");
			sb.append(getAlignStr("CIN:"+cinNumber, Alignment.Center,charPerLine)+"\n");
			sb.append(getAlignStr("Invoice - Cum - Challan", Alignment.Center,charPerLine)+"\n");
			sb.append(getAlignStr("Issued Under Rule 11 of Excise 2002 For Removal of Excisable Goods", Alignment.Center,charPerLine)+"\n");
			sb.append("\n");
			sb.append(getAlignStr("Weighment Serial No. : ", Alignment.Left));
			sb.append(getAlignStr(challanNo, Alignment.Left,charPerLine-(23+15+3+5)));
			sb.append(getAlignStr("Weighbridge No.", Alignment.Right,15));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(workStationCode, Alignment.Left,5)+"\n");
			
			sb.append(getline());
			
			sb.append(getAlignStr("Regd. Office:", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Work :", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Central Excise", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Name & Add", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Mining Pass", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Authenticated", Alignment.Left,charPerLine/6)+"\n");
			
			sb.append(getAlignStr("Seepat Road", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Name & Add Of", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Registation No", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Of Consignee:", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("No.:", Alignment.Left));
			sb.append(getAlignStr(gatePassNo, Alignment.Left,(charPerLine/6)-4));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6)+"\n");
			
			sb.append(getAlignStr("Bilaspur (CG)", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Source Mine &", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(minesRegNo, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(consigneeName1, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Date:", Alignment.Left));
			sb.append(getAlignStr(gatePassDate, Alignment.Left,(charPerLine/6)-5));
			sb.append(getAlignStr("Auth. Signatory", Alignment.Left,charPerLine/6)+"\n");
			
			sb.append(getAlignStr("Pin-495006", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Tin.", Alignment.Left));
			sb.append(getAlignStr(minesTinNo, Alignment.Left,(charPerLine/6)-4));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(consigneeName2, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Time Of Removal", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Address Range", Alignment.Left,charPerLine/6)+"\n");
			
			sb.append(getAlignStr("Phone No:", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(minesName, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(minesProduct1, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(timeOfRemoval, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(authAddressRange, Alignment.Left,charPerLine/6)+"\n");
			
			sb.append(getAlignStr("Fax No:", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(minesProduct2, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(consigneeNameAdd1, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("Mode : ", Alignment.Left));
			sb.append(getAlignStr(transportationMode, Alignment.Left,(charPerLine/6)-7));
			sb.append(getAlignStr("Address Division", Alignment.Left,charPerLine/6)+"\n");
			
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(minesProduct3, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(consigneeNameAdd2, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(authAddressDivisionName, Alignment.Left,charPerLine/6)+"\n");
			
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(consigneeNameAdd3, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(authAddressDivisionAdd1, Alignment.Left,charPerLine/6)+"\n");
			
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(consigneeNameAdd4, Alignment.Left,charPerLine/6));
			sb.append(getAlignStr("", Alignment.Left,charPerLine/6));
			sb.append(getAlignStr(authAddressDivisionAdd2, Alignment.Left,charPerLine/6)+"\n");
		    sb.append(getline());
			
			sb.append(getAlignStr("DO No. & Date", Alignment.Center,charPerLine/6));
			sb.append(getAlignStr("DO Valid", Alignment.Center,charPerLine/6));
			sb.append(getAlignStr("Delivery", Alignment.Center,charPerLine/6));
			sb.append(getAlignStr("Do Qty", Alignment.Center,charPerLine/6));
			sb.append(getAlignStr("Balance Qty", Alignment.Center,charPerLine/6));
			sb.append(getAlignStr("Customer Code", Alignment.Center,charPerLine/6)+"\n");
			
			sb.append(getAlignStr(doNumber, Alignment.Center,charPerLine/6));
			sb.append(getAlignStr("Upto", Alignment.Center,charPerLine/6));
			sb.append(getAlignStr("Point", Alignment.Center,charPerLine/6));
			sb.append(getAlignStr(doQty, Alignment.Center,charPerLine/6));
			sb.append(getAlignStr("to be lifted", Alignment.Center,charPerLine/6));
			sb.append(getAlignStr(customerCode, Alignment.Center,charPerLine/6)+"\n");
			
			sb.append(getAlignStr(doDate, Alignment.Center,charPerLine/6));
			sb.append(getAlignStr(doExpiary, Alignment.Center,charPerLine/6));
			sb.append(getAlignStr(deliveryPoint, Alignment.Center,charPerLine/6));
			sb.append(getAlignStr("", Alignment.Center,charPerLine/6));
			sb.append(getAlignStr(balanceQty, Alignment.Center,charPerLine/6));
			sb.append(getAlignStr("Prog.", Alignment.Left));
			sb.append(getAlignStr(qtyLifted, Alignment.Right,charPerLine/6-(5+2)));
			sb.append(getAlignStr("MT", Alignment.Left)+"\n");
			
			sb.append(getline());
			
			sb.append(getAlignStr("Destination", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(destination, Alignment.Left,rightHalfWidth)+"\n");
			
			sb.append(getAlignStr("Sepecification of Goods", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(material, Alignment.Left,rightHalfWidth)+"\n");
			
			sb.append(getAlignStr("Size", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(coalSize, Alignment.Left,rightHalfWidth)+"\n");
			
			sb.append(getAlignStr("Grade", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(grade, Alignment.Left,rightHalfWidth)+"\n");
			
			sb.append(getAlignStr("Type Of Consumer", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(typeOfConsumer, Alignment.Left,rightHalfWidth)+"\n");
			
			sb.append(getAlignStr("Gross Weight", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(grossWeight, Alignment.Left,docAmountWidth));
			sb.append(getAlignStr(" Date", Alignment.Left));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(grossDateStr, Alignment.Left,docDateWidth));
			sb.append(getAlignStr(" Time", Alignment.Left));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(grossTimeStr, Alignment.Left,docTimeWidth)+"\n");
			
			sb.append(getAlignStr("Tare Weight", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(tareWeight, Alignment.Left,docAmountWidth));
			sb.append(getAlignStr(" Date", Alignment.Left));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(tareDateStr, Alignment.Left,docDateWidth));
			sb.append(getAlignStr(" Time", Alignment.Left));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(tareTimeStr, Alignment.Left,docTimeWidth)+"\n");
			
			sb.append(getAlignStr("Net Weight", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(netWeight, Alignment.Left,rightHalfWidth)+"\n");
			
			sb.append(getAlignStr("Rate", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(coalRate, Alignment.Right,docAmountWidth));
			sb.append(getAlignStr(" / MT", Alignment.Left)+"\n");
			
			sb.append(getAlignStr(" 1. Coal Value", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(coalValue, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" 2. Sizing Charges", Alignment.Left,leftHalfWidth-10));
			sb.append(getAlignStr(sizingChargesRate, Alignment.Left,5));
			sb.append(getAlignStr(" / MT", Alignment.Left));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(sizingCharges, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" 3. SILO Charges", Alignment.Left,leftHalfWidth-10));
			sb.append(getAlignStr(SiloChargesRate, Alignment.Left,5));
			sb.append(getAlignStr(" / MT", Alignment.Left,5));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(SiloCharges, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" 4. STC Charge", Alignment.Left,leftHalfWidth-10));
			sb.append(getAlignStr(stcChargesRate, Alignment.Left,5));
			sb.append(getAlignStr(" / MT", Alignment.Left,5));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(stcCharges, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" 5. Dumping Charge", Alignment.Left,leftHalfWidth-10));
			sb.append(getAlignStr(dumpingChargesRate, Alignment.Left,5));
			sb.append(getAlignStr(" / MT", Alignment.Left,5));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(dumpingCharges, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" 6. Royalty", Alignment.Left,leftHalfWidth-10));
			sb.append(getAlignStr(royaltyChargesRate, Alignment.Left,5));
			sb.append(getAlignStr(" / MT", Alignment.Left,5));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(royaltyCharges, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" 7. DMF", Alignment.Left,leftHalfWidth-(2+5+16)));
			sb.append(getAlignStr("@ ", Alignment.Left,2));
			sb.append(getAlignStr(dmfChargesRate, Alignment.Left,5));
			sb.append(getAlignStr(" % of 6 above", Alignment.Left,16));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(dmfCharges, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" 8. NMET", Alignment.Left,leftHalfWidth-(2+5+16)));
			sb.append(getAlignStr("@ ", Alignment.Left,2));
			sb.append(getAlignStr(nmetChargesRate, Alignment.Left,5));
			sb.append(getAlignStr(" % of 6 above", Alignment.Left,16));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(nmetCharges, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" 9. Stowing Excise Duty", Alignment.Left,leftHalfWidth-(5+5)));
			sb.append(getAlignStr(stowingChargesRate, Alignment.Left,5));
			sb.append(getAlignStr(" / MT", Alignment.Left,5));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(stowingCharges, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr("10. Terminal Tax", Alignment.Left,leftHalfWidth-(5+5)));
			sb.append(getAlignStr(terminalChargesRate, Alignment.Left,5));
			sb.append(getAlignStr(" / MT", Alignment.Left,5));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(terminalCharges, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr("11. Forest Cess", Alignment.Left,leftHalfWidth-(5+5)));
			sb.append(getAlignStr(forestCessRate, Alignment.Left,5));
			sb.append(getAlignStr(" / MT", Alignment.Left,5));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(forestCess, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr("12. MP Sadak Tax / CG", Alignment.Left,80)+"\n");
			sb.append(getAlignStr("    Paryavaran & Vikas Upkar", Alignment.Left,leftHalfWidth-(5+5)));
			sb.append(getAlignStr(otherTaxRate, Alignment.Left,5));
			sb.append(getAlignStr(" / MT", Alignment.Left,5));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(otherTax, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append("\n");
			
			sb.append(getAlignStr("       Assessable Amount(A) [1 to 5]", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(assessableAmountBlockA, Alignment.Right,docAmountWidth));
			sb.append(getAlignStr(assessableAmountBlockAWord, Alignment.Left,rightHalfWidth-docAmountWidth)+"\n");
			
			sb.append("\n");
			
			sb.append(getAlignStr(" Excise Duty", Alignment.Left,leftHalfWidth-(2+18)));
			sb.append(getAlignStr(exciseRate, Alignment.Right,2));
			sb.append(getAlignStr("% on (A)", Alignment.Left,18));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(exciseOnBlockA, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" Education Cess", Alignment.Left,leftHalfWidth-(2+18)));
			sb.append(getAlignStr(educationCessRate, Alignment.Right,2));
			sb.append(getAlignStr("%", Alignment.Left,18));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(educationCessOnBlocakA, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" SHEDU Cess", Alignment.Left,leftHalfWidth-(2+18)));
			sb.append(getAlignStr(sheduCessRate, Alignment.Right,2));
			sb.append(getAlignStr("%", Alignment.Left,18));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(sheduCessOnBlockA, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" Excise Duty & Cess on (Total)(A)", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(totalExciseAndCessOnBlockA, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append("\n");
			
			sb.append(getAlignStr("   New Assessable Amount(B) [1 to 12]", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(assessableAmountBlockB, Alignment.Right,docAmountWidth));
			sb.append(getAlignStr(assessableAmountBlockBWord, Alignment.Left,rightHalfWidth-docAmountWidth)+"\n");
			
			sb.append("\n");
			
			sb.append(getAlignStr(" Excise Duty", Alignment.Left,leftHalfWidth-(2+18)));
			sb.append(getAlignStr(exciseRate, Alignment.Right,2));
			sb.append(getAlignStr("% on (B)", Alignment.Left,18));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(exciseOnBlockB, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" Education Cess", Alignment.Left,leftHalfWidth-(2+18)));
			sb.append(getAlignStr(educationCessRate, Alignment.Right,2));
			sb.append(getAlignStr("%", Alignment.Left,18));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(educationCessOnBlocakB, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" SHEDU Cess", Alignment.Left,leftHalfWidth-(2+18)));
			sb.append(getAlignStr(educationCessRate, Alignment.Right,2));
			sb.append(getAlignStr("%", Alignment.Left,18));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(sheduCessOnBlockB, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" Excise Duty & Cess on (Total)(B)", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(totalExciseAndCessOnBlockB, Alignment.Right,docAmountWidth));
			sb.append(getAlignStr(totalExciseAndCessOnBlockBWord, Alignment.Left,rightHalfWidth-docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" Excise Duty & Cess on Payble(A)", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(totalExciseAndCessPaybleOnBlockA, Alignment.Right,docAmountWidth));
			sb.append(getAlignStr(totalExciseAndCessPaybleOnBlockAWord, Alignment.Left,rightHalfWidth-docAmountWidth)+"\n");
			
			sb.append(getAlignStr(" Excise Duty Payble UNDER PROTEST(B-A)", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr(":", Alignment.Center,docColonWith));
			sb.append(getAlignStr(totalExcisePaybleUnderProtest, Alignment.Right,docAmountWidth));
			sb.append(getAlignStr("(incl. cess) against Assessable value of", Alignment.Left)+"\n");
			
			sb.append(getAlignStr("", Alignment.Left,leftHalfWidth));
			sb.append(getAlignStr("Rs.", Alignment.Center));
			sb.append(getAlignStr(totalExcisePaybleAmount, Alignment.Right,docAmountWidth)+"\n");
			
			sb.append(getline());
			sb.append(getAlignStr("**Certified that particulars given above are true and correct. ", Alignment.Left,charPerLine - (16+16)));
			sb.append(getAlignStr("Truck No.", Alignment.Center,16));
			sb.append(getAlignStr(projectName, Alignment.Center,16)+"\n");
			sb.append(getAlignStr(" ", Alignment.Left,charPerLine - (16+16)));
			sb.append(getAlignStr(truckNo, Alignment.Center,16));
			sb.append(getAlignStr("project", Alignment.Right,16)+"\n");
			sb.append("\n");
			sb.append("\n");
			sb.append(getAlignStr("(Authorised Signatory)", Alignment.Right,charPerLine)+"\n");
			sb.append(getline());
			return sb.toString();
	}
	public static void printableContent(FileWriter out, String doNumber, String doDate, String doExpiary, String deliveryPoint, 
									String doQty, String balanceQty, String destination, String material, String coalSize, 
									String grade, String typeOfConsumer, String grossWeight, String grossDateStr, 
									String grossTimeStr, String tareWeight, String tareDateStr, String tareTimeStr, 
									String netWeight, String coalRate, String coalValue, String sizingChargesRate, 
									String sizingCharges, String SiloChargesRate, String SiloCharges, String stcChargesRate, 
									String stcCharges, String dumpingChargesRate, String dumpingCharges, String royaltyChargesRate, 
									String royaltyCharges, String dmfChargesRate, String dmfCharges, String nmetChargesRate, 
									String nmetCharges, String stowingChargesRate, String stowingCharges, String terminalChargesRate, 
									String terminalCharges, String forestCessRate, String forestCess, String otherTax, 
									String assessableAmountBlockA, String exciseRate, String exciseOnBlockA, 
									String educationCessRate, String educationCessOnBlocakA, String sheduCessRate, 
									String sheduCessOnBlockA, String totalExciseAndCessOnBlockA, String assessableAmountBlockB, 
									String exciseOnBlockB, String educationCessOnBlocakB, String sheduCessOnBlockB, 
									String totalExciseAndCessOnBlockB, String totalExciseAndCessPaybleOnBlockA, 
									String totalExcisePaybleUnderProtest, String assessableAmountBlockAWord, 
									String assessableAmountBlockBWord, String totalExciseAndCessOnBlockBWord, 
									String totalExciseAndCessPaybleOnBlockAWord, String totalExcisePaybleUnderProtestword) throws Exception{
		out.write("\n");
			out.write(getAlignStr("SOUTH EASTERN COALFIELD LIMITED", Alignment.Center)+"\n");
			out.write(getAlignStr("Invoice-Cum-Challan", Alignment.Center)+"\n");
			out.write(getAlignStr("Issued Under Rule 11 of Excise 2002 For Removal of Excisable Goods", Alignment.Center)+"\n");
			out.write(getAlignStr("Weighment Serial No. : 16-11-017298", Alignment.Left)+"\n");
			out.write("\n");
			out.write(getline());
			
		    out.write(getline());
			
			out.write(getAlignStr("DO No. & Date", Alignment.Center,14));
			out.write(getAlignStr("DO Valid", Alignment.Center,13));
			out.write(getAlignStr("Delivery", Alignment.Center,13));
			out.write(getAlignStr("Do Qty", Alignment.Center,13));
			out.write(getAlignStr("Balance Qty", Alignment.Center,14));
			out.write(getAlignStr("Destination", Alignment.Center,13)+"\n");
			
			out.write(getAlignStr(doNumber, Alignment.Center,14));
			out.write(getAlignStr("Upto", Alignment.Center,13));
			out.write(getAlignStr("Point", Alignment.Center,13));
			out.write(getAlignStr(doQty, Alignment.Center,13));
			out.write(getAlignStr("to be lifted", Alignment.Center,14));
			out.write(getAlignStr("", Alignment.Center,13)+"\n");
			
			out.write(getAlignStr(doDate, Alignment.Center,14));
			out.write(getAlignStr(doExpiary, Alignment.Center,13));
			out.write(getAlignStr(deliveryPoint, Alignment.Center,13));
			out.write(getAlignStr("", Alignment.Center,13));
			out.write(getAlignStr(balanceQty, Alignment.Center,14));
			out.write(getAlignStr("", Alignment.Center,13)+"\n");
			
			out.write(getline());
			
			out.write(getAlignStr("Destination", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(destination, Alignment.Left,38)+"\n");
			
			out.write(getAlignStr("Sepecification of Goods", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(material, Alignment.Left,38)+"\n");
			
			out.write(getAlignStr("Size", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(coalSize, Alignment.Left,38)+"\n");
			
			out.write(getAlignStr("Grade", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(grade, Alignment.Left,38)+"\n");
			
			out.write(getAlignStr("Type Of Consumer", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(typeOfConsumer, Alignment.Left,38)+"\n");
			
			out.write(getAlignStr("Gross Weight", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(grossWeight, Alignment.Left,8));
			out.write(getAlignStr(" Date", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(grossDateStr, Alignment.Left,10));
			out.write(getAlignStr(" Time", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(grossTimeStr, Alignment.Left,5)+"\n");
			
			out.write(getAlignStr("Tare Weight", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(tareWeight, Alignment.Left,8));
			out.write(getAlignStr(" Date", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(tareDateStr, Alignment.Left,10));
			out.write(getAlignStr(" Time", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(tareTimeStr, Alignment.Left,5)+"\n");
			
			out.write(getAlignStr("Net Weight", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(netWeight, Alignment.Left,38)+"\n");
			
			out.write(getAlignStr("Rate", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(coalRate, Alignment.Right,8));
			out.write(getAlignStr(" / MT", Alignment.Right,30)+"\n");
			
			out.write(getAlignStr("1. Coal Value", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(coalValue, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr("2. Sizing Charges", Alignment.Left,29));
			out.write(getAlignStr(sizingChargesRate, Alignment.Left,5));
			out.write(getAlignStr(" / MT", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(sizingCharges, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr("3. SILO Charges", Alignment.Left,29));
			out.write(getAlignStr(SiloChargesRate, Alignment.Left,5));
			out.write(getAlignStr(" / MT", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(SiloCharges, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr("4. STC Charge", Alignment.Left,29));
			out.write(getAlignStr(stcChargesRate, Alignment.Left,5));
			out.write(getAlignStr(" / MT", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(stcCharges, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr("5. Dumping Charge", Alignment.Left,29));
			out.write(getAlignStr(dumpingChargesRate, Alignment.Left,5));
			out.write(getAlignStr(" / MT", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(dumpingCharges, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr("6. Royalty", Alignment.Left,29));
			out.write(getAlignStr(royaltyChargesRate, Alignment.Left,5));
			out.write(getAlignStr(" / MT", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(royaltyCharges, Alignment.Left,8)+"\n");
			
			out.write(getAlignStr("7. DMF", Alignment.Left,16));
			out.write(getAlignStr("@ ", Alignment.Left,2));
			out.write(getAlignStr(dmfChargesRate, Alignment.Left,5));
			out.write(getAlignStr(" % of 6 above", Alignment.Left,16));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(dmfCharges, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr("8. NMET", Alignment.Left,16));
			out.write(getAlignStr("@ ", Alignment.Left,2));
			out.write(getAlignStr(nmetChargesRate, Alignment.Left,5));
			out.write(getAlignStr(" % of 6 above", Alignment.Left,16));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(nmetCharges, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr("9. Stowing Excise Duty", Alignment.Left,29));
			out.write(getAlignStr(stowingChargesRate, Alignment.Left,5));
			out.write(getAlignStr(" / MT", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(stowingCharges, Alignment.Left,8)+"\n");
			
			out.write(getAlignStr("10. Terminal Tax", Alignment.Left,29));
			out.write(getAlignStr(terminalChargesRate, Alignment.Left,5));
			out.write(getAlignStr(" / MT", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(terminalCharges, Alignment.Left,8)+"\n");
			
			out.write(getAlignStr("11. Forest Cess", Alignment.Left,29));
			out.write(getAlignStr(forestCessRate, Alignment.Left,5));
			out.write(getAlignStr(" / MT", Alignment.Left,5));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(forestCess, Alignment.Left,8)+"\n");
			
			out.write(getAlignStr("12. MP Sadak Tax / CG", Alignment.Left,80)+"\n");
		
			out.write(getAlignStr("    Paryavaran & Vikas Upkar", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(otherTax, Alignment.Right,8)+"\n");
			
			out.write("\n");
			
			out.write(getAlignStr("       Assessable Amount(A) [1 to 5]", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(assessableAmountBlockAWord, Alignment.Right,8));
			out.write(getAlignStr(assessableAmountBlockA, Alignment.Left,30)+"\n");
			
			out.write("\n");
			
			out.write(getAlignStr(" Excise Duty", Alignment.Left,19));
			out.write(getAlignStr(exciseRate, Alignment.Right,2));
			out.write(getAlignStr("% on (A)", Alignment.Left,18));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(exciseOnBlockA, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr(" Education Cess", Alignment.Left,19));
			out.write(getAlignStr(educationCessRate, Alignment.Right,2));
			out.write(getAlignStr("%", Alignment.Left,18));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(educationCessOnBlocakA, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr(" SHEDU Cess", Alignment.Left,19));
			out.write(getAlignStr(sheduCessRate, Alignment.Right,2));
			out.write(getAlignStr("%", Alignment.Left,18));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(sheduCessOnBlockA, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr(" Excise Duty & Cess on (Total)(A)", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(totalExciseAndCessOnBlockA, Alignment.Left,8)+"\n");
			
			out.write("\n");
			
			out.write(getAlignStr("   New Assessable Amount(B) [1 to 12]", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(assessableAmountBlockBWord, Alignment.Right,8));
			out.write(getAlignStr(assessableAmountBlockB, Alignment.Left,30)+"\n");
			
			out.write("\n");
			
			out.write(getAlignStr(" Excise Duty", Alignment.Left,19));
			out.write(getAlignStr(exciseRate, Alignment.Right,2));
			out.write(getAlignStr("% on (B)", Alignment.Left,18));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(exciseOnBlockB, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr(" Education Cess", Alignment.Left,19));
			out.write(getAlignStr(educationCessRate, Alignment.Right,2));
			out.write(getAlignStr("%", Alignment.Left,18));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(educationCessOnBlocakB, Alignment.Left,38)+"\n");
			
			out.write(getAlignStr(" SHEDU Cess", Alignment.Left,19));
			out.write(getAlignStr(educationCessRate, Alignment.Right,2));
			out.write(getAlignStr("%", Alignment.Left,18));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(sheduCessOnBlockB, Alignment.Right,8)+"\n");
			
			out.write(getAlignStr(" Excise Duty & Cess on (Total)(B)", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(totalExciseAndCessOnBlockBWord, Alignment.Right,8));
			out.write(getAlignStr(totalExciseAndCessOnBlockB, Alignment.Left,38)+"\n");
			
			out.write(getAlignStr(" Excise Duty & Cess on Payble(A)", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(totalExciseAndCessPaybleOnBlockAWord, Alignment.Right,8));
			out.write(getAlignStr(totalExciseAndCessPaybleOnBlockA, Alignment.Left,30)+"\n");
			
			out.write(getAlignStr(" Excise Duty Payble UNDER PROTEST(B-A)", Alignment.Left,39));
			out.write(getAlignStr(":", Alignment.Center,3));
			out.write(getAlignStr(totalExcisePaybleUnderProtestword, Alignment.Right,8));
			out.write(getAlignStr(totalExcisePaybleUnderProtest, Alignment.Left,30)+"\n");
			
			out.write(getline());
			out.write(getAlignStr("**Certified that particulars given are true and correct. ", Alignment.Left,80)+"\n");
			out.flush();
		}
	private static String getSampleData(){
		String doNumber = "06637";
		String doDate = "28-11-2016";
		String doExpiary = "11-01-17";
		String deliveryPoint = "EA-100MM STOCK";
		String doQty = "25000.00 MT";
		String balanceQty = "24777.31MT";
		String destination = "MARUTI COAL RATIJA C.G.";
		String material = "COAL";
		String coalSize = "SIZED ROM (-100mm)";
		String grade = "BITUMINUS COAL GCV BAND 4000-4300 (KCAL/KG)";
		String typeOfConsumer = "E-AUCTION";
		String grossWeight = "39.88 MT ";
		String grossDateStr = "06-12-2016";
		String grossTimeStr = "17:05:26";
		String tareWeight = "14.59 MT ";
		String tareDateStr = "06-12-2016";
		String tareTimeStr = "14:57:22";
		String netWeight = "25.29 MT ";
		String coalRate = "1454.00";
		String coalValue = "36771.66";
		String sizingChargesRate = "79.00";
		String sizingCharges = "1997.91";
		String SiloChargesRate = "0.00";
		String SiloCharges = "0.00";
		String stcChargesRate = "0.00";
		String stcCharges = "0.00";
		String dumpingChargesRate = "0.00";
		String dumpingCharges = "0.00";
		String royaltyChargesRate = "203.56";
		String royaltyCharges = "5148.03";
		String dmfChargesRate = "30.00";
		String dmfCharges = "1544.41";
		String nmetChargesRate = "2.00";
		String nmetCharges = "102.96";
		String stowingChargesRate = "10.00";
		String stowingCharges = "252.90";
		String terminalChargesRate = "1.53";
		String terminalCharges = "38.69";
		String forestCessRate = "0.00";
		String forestCess = "0.00";
		String otherTaxRate = "15.00";
		String otherTax = "379.35";
		String assessableAmountBlockA = "38769.57";
		String assessableAmountBlockAWord = "("+NumberToWordsConverter.Convert(38769)+" Only)";
		String exciseRate = "6";
		String exciseOnBlockA = "2326.17";
		String educationCessRate = "0";
		String educationCessOnBlocakA = "0.00";
		String sheduCessRate = "0";
		String sheduCessOnBlockA = "0.00";
		String totalExciseAndCessOnBlockA = "2326.00";
		String assessableAmountBlockB = "46235.93";
		String assessableAmountBlockBWord = "("+NumberToWordsConverter.Convert(46235)+" Only)";
		
		String exciseOnBlockB = "2774.15";
		String educationCessOnBlocakB = "0.00";
		String sheduCessOnBlockB = "0.00";
		String totalExciseAndCessOnBlockB = "2774.00";
		String totalExciseAndCessOnBlockBWord = "("+NumberToWordsConverter.Convert(2774)+" Only)";
		String totalExciseAndCessPaybleOnBlockA = "2326.00";
		String totalExciseAndCessPaybleOnBlockAWord = "("+NumberToWordsConverter.Convert(2326)+" Only)";
		String totalExcisePaybleUnderProtest = "448.00";
		String totalExcisePaybleAmount = "7466.35";

		String cinNumber = "U10102CT1985G01003161";
		String challanNo = "16-12-000260";
		String workStationCode = "WB07";
		String truckNo="CG12S0392";
		String projectName="For. SECL GAVERA"; 


		String gatePassNo = "06R602T282";
		String minesRegNo = "AADCS2066EEM032";
		String consigneeName1 = "GODAWARI POWER";
		String gatePassDate = "06-12-16";
		String minesTinNo = "22924603096";
		String consigneeName2 = "AND ISPAT LTD.";
		String minesName = "GAVERA OCM";
		String minesProduct1 = "Chap Sub Hd No";
		String timeOfRemoval = "17:05:26";
		String authAddressRange = "RANGE V";
		String minesProduct2 = "27011200 Bitu-";
		String consigneeNameAdd1 = "PLOT NO 428/2 P";
		String transportationMode = "by Road";
		String consigneeNameAdd2 = "HASE 1 INDUSTRI";
		String minesProduct3 = "minus Coal";
		String authAddressDivisionName = "BILASPUR - 1";
		String consigneeNameAdd3 = "AL AREA SILTARA";
		String authAddressDivisionAdd1 = "Commisionerate";
		String consigneeNameAdd4 = "";
		String authAddressDivisionAdd2 = "BILASPUR";
		String authAddressDivisionAdd3 = "";
		String customerCode = "209880";
		String qtyLifted = "222.69";
		long st = System.currentTimeMillis();
		String printableStr = getPrintableContent( truckNo, projectName, doNumber, doDate, doExpiary, deliveryPoint, 
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
				cinNumber, challanNo, workStationCode,gatePassNo,minesRegNo,
				consigneeName1,gatePassDate,minesTinNo,consigneeName2,minesName,minesProduct1,
				timeOfRemoval,authAddressRange,minesProduct2, minesProduct3,consigneeNameAdd1,transportationMode,consigneeNameAdd2,authAddressDivisionName,consigneeNameAdd3,authAddressDivisionAdd1,consigneeNameAdd4,authAddressDivisionAdd2,authAddressDivisionAdd3
				,customerCode,qtyLifted);
		System.out.println(printableStr);
		return printableStr;
	}
	
}