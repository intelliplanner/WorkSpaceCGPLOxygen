package com.ipssi.reporting.trip;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;

public class CssClassDefinition {
	
	private static HashMap<Integer,CSSPdf> styleSheetPdf = null;
	private static HashMap<String, Integer> reverseStyleSheetHtml = null;
	private static HashMap<Integer,String> styleSheetHtml = null;

	public static String getHtmlCssClass(int cssClassId) {
		if (styleSheetHtml == null)
			initHtml();
		return styleSheetHtml.get(cssClassId);		
	}
	public static CSSPdf getPdfCssClass(int cssClassId){
		if(styleSheetPdf == null) {
			if (styleSheetHtml == null)
				initHtml(); //so that reverse gets initialized 
			initPdf();
		}
		return styleSheetPdf.get(cssClassId);		
	}
	public static String getOrgLogo(int reportId,SessionManager session){
		String retval = "";//session.getAttribute("homeName") != null && session.getAttribute("homeName").equalsIgnoreCase("home_aavvik") ? "/home/jboss/static/images/aavvik.png" : "/home/jboss/static/images/report_logo.png";
		String query = "select mail_logo from report_definitions join org_mailing_params on (report_definitions.org_mailing_id = org_mailing_params.id) where report_definitions.id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = session.getConnection();
		try{
			ps = conn.prepareStatement(query);
			ps.setInt(1,reportId);
			rs = ps.executeQuery();
			while(rs.next()){
				retval = rs.getString("mail_logo");
			}
			if(new File(retval).exists()){
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();
			}	
			/*else{				
				retval = "G://Working//EclipseWorkspace//static//images//report_logo.png";
			}*/
		}catch(Exception e){
			e.printStackTrace();
		}
		return retval;	
	}
	public static String getTemplateFile(int reportId,SessionManager session){
		String retval = "";
		String query = "select excel_template.url from report_definitions join excel_template on (report_definitions.template_id = excel_template.id) where report_definitions.id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = session.getConnection();
		try{
			ps = conn.prepareStatement(query);
			ps.setInt(1,reportId);
			rs = ps.executeQuery();
			while(rs.next()){
				retval = rs.getString("url");
			}
			if(new File(retval).exists()){
				if(rs != null)
					rs.close();
				if(ps != null)
					ps.close();
			}	
		}catch(Exception e){
			e.printStackTrace();
		}
		return retval;	
	}
	/*
	 *id-class 
	 *0 -tshb
	 *1 -tshc
	 *2 -cn
	 *3 -nn
	 *4 -nnGreen
	 *5 -nnYellow
	 *6 -nnRed
	 *7-sh
	 *8-tn
	 *9-sh1
	 */	
	public static void initHtml() { 
		styleSheetHtml = new HashMap<Integer, String>();
		reverseStyleSheetHtml = new HashMap<String, Integer>();
		//Html
		styleSheetHtml.put(0, "tshb");
		reverseStyleSheetHtml.put("tshb",0);
		styleSheetHtml.put(1, "tshc");
		reverseStyleSheetHtml.put("tshc",1);
		styleSheetHtml.put(2, "cn");
		reverseStyleSheetHtml.put("cn",2);
		styleSheetHtml.put(3, "nn");
		reverseStyleSheetHtml.put("nn",3);
		styleSheetHtml.put(4, "nnGreen");
		reverseStyleSheetHtml.put("nnGreen",4);
		styleSheetHtml.put(5, "nnYellow");
		reverseStyleSheetHtml.put("nnYellow",5);
		styleSheetHtml.put(6, "nnRed");
		reverseStyleSheetHtml.put("nnRed",6);
		styleSheetHtml.put(7, "sh");
		reverseStyleSheetHtml.put("sh",7);
		styleSheetHtml.put(8, "tn");
		reverseStyleSheetHtml.put("tn",8);
		styleSheetHtml.put(9,"sh1");
		reverseStyleSheetHtml.put("sh1",9);
		styleSheetHtml.put(10,"cnb");
		reverseStyleSheetHtml.put("cnb",10);
		styleSheetHtml.put(11,"nnb");
		reverseStyleSheetHtml.put("nnb",11);
		styleSheetHtml.put(12,"nnGreen");
		reverseStyleSheetHtml.put("nnGreenb",12);
		styleSheetHtml.put(13,"nnYellow");
		reverseStyleSheetHtml.put("nnYellowb",13);
		styleSheetHtml.put(14,"nnRed");
		reverseStyleSheetHtml.put("nnRedb",14);
		styleSheetHtml.put(15,"extHR");
		reverseStyleSheetHtml.put("extHR",15);
		styleSheetHtml.put(16,"extPageBreak");
		reverseStyleSheetHtml.put("extPageBreak",16);
		styleSheetHtml.put(17,"dash");
		reverseStyleSheetHtml.put("dash",17);
		styleSheetHtml.put(18,"sh3");
		reverseStyleSheetHtml.put("sh3",18);
		styleSheetHtml.put(19,"dash_small");
		reverseStyleSheetHtml.put("dash_small",19);
	}
	public static int getClassIdByClassName(String  css, boolean doingInTable) {
		int id = getClassIdByClassName(css);
		if (Misc.isUndef(id)) {
			id = getClassIdByClassName(doingInTable ? "cn" : "tn"); 
		}
		return id;
	}
	public static int getClassIdByClassName(String css) {
		if (styleSheetHtml == null) 
			initHtml();
		Integer iv = reverseStyleSheetHtml.get(css);
		return iv == null ? Misc.getUndefInt() : iv.intValue();
	}
	public static void initPdf() {
		styleSheetPdf = new HashMap<Integer, CSSPdf>();
		//pdf
		//styleSheetPdf.put(key, new CSSPdf(textColorId, bgColorId, borderColorId, borderWidth, textAlign, bold, textSize))
		styleSheetPdf.put(0, new CSSPdf(16777215, 15090709, 16088368, 1, 0, false,9));
		styleSheetPdf.put(1, new CSSPdf(16777215, 15090709, 15090709, 1, 0, false,9));
		styleSheetPdf.put(2, new CSSPdf(13158, 15525606, 16777215, 1, -1, false,9));//cn
		styleSheetPdf.put(3, new CSSPdf(102, 15525606, 16777215, 1, 1, false,9));//nn
		styleSheetPdf.put(4, new CSSPdf(102, 65280, 16777215, 1, 1, false,9));//nnGreen
		styleSheetPdf.put(5, new CSSPdf(16777215, 16777011, 16777215, 1, 1, false,9));//nnYellow
		styleSheetPdf.put(6, new CSSPdf(16777215, 16711680, 16777215, 1, 1, false,9));//nnRed
		styleSheetPdf.put(7, new CSSPdf(16777215, 16711680, 16777215, 1, 1, false,9));//TODO
		styleSheetPdf.put(8, new CSSPdf(16777215, 16711680, 16777215, 1, 1, false,9));//TODO
		styleSheetPdf.put(9, new CSSPdf(16777215, 16711680, 16777215, 1, 1, false,9));//TODO
		styleSheetPdf.put(10, new CSSPdf(13158, 15525606, 16777215, 1, -1, true,9));//cnb
		styleSheetPdf.put(11, new CSSPdf(102, 15525606, 16777215, 1, 1, true,9));//nnb
		styleSheetPdf.put(12, new CSSPdf(102, 65280, 16777215, 1, 1, true,9));//nnGreenb
		styleSheetPdf.put(13, new CSSPdf(16777215, 16777011, 16777215, 1, 1, true,9));//nnYellowb
		styleSheetPdf.put(14, new CSSPdf(16777215, 16711680, 16777215, 1, 1, true,9));//nnRedb
		styleSheetPdf.put(15, new CSSPdf(13158, 15525606, 16777215, 1, -1, false,9));//cn
		styleSheetPdf.put(16, new CSSPdf(13158, 15525606, 16777215, 1, -1, false,9));//cn
	}

}
class CSSPdf{

	private BaseColor textColor = null;
	private BaseColor bgColor= null;
	private BaseColor borderColor = null;
	private int borderWidth = 1;
	private int textAlign = -1; //-1 left   0 center   1 right
	private Font font = null;
	private int textSize = 11;
	public CSSPdf(int textColorId,int bgColorId,int borderColorId,int borderWidth,int textAlign,boolean bold,int textSize){
		textColor = new BaseColor(textColorId);
		bgColor = new BaseColor(bgColorId);
		borderColor = new BaseColor(borderColorId);
		this.borderWidth = borderWidth;
		this.textAlign = textAlign;
		this.textSize = textSize;
		if (bold)
			font = new Font(Font.FontFamily.TIMES_ROMAN, this.textSize, Font.BOLD, textColor);
		else
			font = new Font(Font.FontFamily.HELVETICA,this.textSize,Font.NORMAL, textColor);
	}
	public BaseColor getTextColor(){
		return this.textColor;
	}
	public BaseColor getBgColor(){
		return this.bgColor;
	}
	public BaseColor getBorderColor(){
		return this.borderColor;
	}
	public int getBorderWidth(){
		return this.borderWidth;
	}
	public int getTextAlign(){
		return this.textAlign;
	}
	public int getTextSize(){
		return this.textSize;
	}
	public Font getFont(){
		return this.font;
	}
}
