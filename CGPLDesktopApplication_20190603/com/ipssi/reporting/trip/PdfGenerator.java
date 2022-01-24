package com.ipssi.reporting.trip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jxl.write.WriteException;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfGenerator {
	private BaseColor headingColor = new BaseColor(102);
	private BaseColor cellHeaderColor = new BaseColor(15090709);
	private BaseColor cellDataColor = new BaseColor(15525606);
	private Font headingFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD, headingColor);
	private Font subHeadingFont = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.BOLD, headingColor);
	private Font cellHeaderFont = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL, BaseColor.WHITE);
	private Font cellDataFont = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL, BaseColor.BLACK);
	private PdfPTable pdfTable = null;
	private Document document = null;
	//private String logoFile = "g://report_logo.png";
	private String logoFile = "/home/jboss/static/images/report_logo.png";
	//private String aavvikLogo = "/home/jboss/static/images/aavvik.jpg";
	public  void printPdf(OutputStream out,String reportName,Table table,SessionManager session,int reportId) {
		try {
			logoFile = CssClassDefinition.getOrgLogo(reportId, session);
			int tableSize = table.getTableWidth();
			if(tableSize > 0){
				document = new Document(PageSize.A3.rotate());
				PdfWriter.getInstance(document, out);
				document.open();
				addMetaData(document);
				pdfTable = createTableHeader(table, pdfTable);
				pdfTable = insertTableData(table, pdfTable);
				addHeader(document,reportName);
				generateReport();
			}
		} catch (Exception e) {
			if(document != null)
				document.close();
			e.printStackTrace();
		}
	}
	private  void addMetaData(Document document) {
		document.addTitle("Intelliplanner Auto Emailing System");
		document.addSubject("Report");
		document.addAuthor("IPSSI");
		document.addCreator("IPSSI");
	}
	private void addHeader(Document document,String reportName) throws DocumentException, MalformedURLException, IOException {
		SimpleDateFormat indepDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Paragraph preface = new Paragraph();
		addEmptyLine(preface, 2);
		Image logo = null;
		logo = Image.getInstance(logoFile);
		logo.scalePercent(85, 70);
		logo.setAlignment(Image.ALIGN_CENTER);
		document.add(preface);
		document.add(logo);
		addEmptyLine(preface, 1);
		preface.add(new Paragraph(reportName, headingFont));
		preface.add(new Paragraph("Report generated On: "+ indepDateFormat.format(new Date()),subHeadingFont));
		addEmptyLine(preface, 1);
		document.add(preface);    
	}
	private PdfPTable createTableHeader(Table table, PdfPTable pdfTable)
			throws DocumentException, WriteException { 
		int tableSize = table.getTableWidth(); 
		boolean hasRowSpan = tableSize > 0 && table.getHeader() != null ? tableSize > table.getHeader().get(0).getRowData().size() : false;
		PdfPCell cell = null;
		if (tableSize > 0){
			ArrayList<TR> header = table.getHeader();
			if (pdfTable == null){
				pdfTable = new PdfPTable(tableSize);	   
				pdfTable.setWidthPercentage(100);
			}
			if (header != null){
				CSSPdf css = null;
				int align = Misc.getUndefInt();
				for (TR row : header){
					for(TD col : row.getRowData()){
						if(col.getDoIgnore() || col.getHidden())
							continue;
						css = !Misc.isUndef(col.getClassId()) ? CssClassDefinition.getPdfCssClass(col.getClassId()) : CssClassDefinition.getPdfCssClass(row.getClassId());
						align = Misc.isUndef(col.getAlignment()) ? css.getTextAlign() : col.getAlignment();
						cell = new PdfPCell(new Phrase(col.getContent(),css != null ? css.getFont(): cellHeaderFont));
						cell.setRowspan(col.getRowSpan());
						cell.setColspan(col.getColSpan());
						cell.setHorizontalAlignment(Misc.isUndef(align)? Element.ALIGN_CENTER :  align == -1 ? Element.ALIGN_LEFT : align == 0 ? Element.ALIGN_CENTER : Element.ALIGN_RIGHT);
						cell.setBackgroundColor(css.getBgColor() != null ? css.getBgColor():cellHeaderColor);
						cell.setBorderColor(css.getBorderColor() != null ? css.getBorderColor(): BaseColor.WHITE);
						cell.setBorderWidth(css.getBorderWidth());
						pdfTable.addCell(cell);
					}
				}

				if(hasRowSpan)
					pdfTable.setHeaderRows(2);
				else
					pdfTable.setHeaderRows(1);
			}
		}
		return pdfTable;
	}
	private PdfPTable insertTableData(Table table, PdfPTable pdfTable)
			throws DocumentException, WriteException { 
		PdfPCell cell = null;  
		int tableSize = table.getTableWidth();
		if (tableSize > 0){
			ArrayList<TR> body = table.getBody();
			if (pdfTable == null){
				pdfTable = new PdfPTable(tableSize);	   
				pdfTable.setWidthPercentage(100);
			}
			if (body != null){
				CSSPdf css = null;
				int align = Misc.getUndefInt();
				for (TR row : body){
					for(TD col : row.getRowData()){
						if(col.getDoIgnore() || col.getHidden())
							continue;
						css = !Misc.isUndef(col.getClassId()) ? CssClassDefinition.getPdfCssClass(col.getClassId()) : CssClassDefinition.getPdfCssClass(row.getClassId());
						align = Misc.isUndef(col.getAlignment()) ? css.getTextAlign() : col.getAlignment();						
						if (col.getContent().equals(Misc.nbspString))
							col.setContent("");
						if(col.getContent().contains("power_on.png"))
							col.setContent("ON");
						if (col.getContent().contains("power_off.png"))
							col.setContent("OFF");
						if (col.getContent().contains("battery_charging.png"))
							col.setContent("ON");
						if (col.getContent().contains("battery_discharging.png"))
							col.setContent("OFF");
						cell = new PdfPCell(new Phrase(col.getContent(),css != null ? css.getFont(): cellDataFont));
						cell.setRowspan(col.getRowSpan());
						cell.setColspan(col.getColSpan());
						cell.setHorizontalAlignment(Misc.isUndef(align)? Element.ALIGN_CENTER :  align == -1 ? Element.ALIGN_LEFT : align == 0 ? Element.ALIGN_CENTER : Element.ALIGN_RIGHT);
						cell.setBackgroundColor(css.getBgColor() != null ? css.getBgColor():cellHeaderColor);
						cell.setBorderColor(css.getBorderColor() != null ? css.getBorderColor(): BaseColor.WHITE);
						cell.setBorderWidth(css.getBorderWidth());
						pdfTable.addCell(cell);
					}
				}
			}
		}
		return pdfTable;
	}
	private  void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}
	private void generateReport(){
		try {
			if(pdfTable != null)
				document.add(pdfTable);
			if(document != null)
				document.close();

		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	private  void main(String arg[]) throws DocumentException, FileNotFoundException{
		/*CreatePdf cp = new CreatePdf();
		ArrayList<CellContent> dataHeader = new ArrayList<CellContent>();
		ArrayList<CellContent> cellList = null;
		CellContent cell = null;
		ArrayList<ArrayList<CellContent>> dataCell = new ArrayList<ArrayList<CellContent>>();
		cellList = new ArrayList<CellContent>();	
		for(int i=0;i<4;i++)
		{
			cell = new CellContent();
			cell.setContent("header"+i);
			cell.setColSpan(1);
			cell.setRowSpan(1);
			dataHeader.add(cell);
		}
		for(int j=0;j<10;j++)
		{   cellList = new ArrayList<CellContent>();
		for(int k=0;k<10;k++)
		{
			cell = new CellContent();
			cell.setContent(j+k+1+"");
			cell.setColSpan(1);
			cell.setRowSpan(1);
			cellList.add(cell);
		}
		dataCell.add(cellList);
		}
		String FILE = "g://dataX.pdf";
		FileOutputStream out = new FileOutputStream(FILE);
		//ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		cp.createPdf(out, "test",new Table());
		cp.createTableHeader(dataHeader, false, 4);
		cp.insertTableData(dataCell);
		cp.generateReport();
		System.out.print("report generated");*/
	}
} 