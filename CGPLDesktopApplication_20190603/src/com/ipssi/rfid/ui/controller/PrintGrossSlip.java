/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.controller;

import com.ipssi.gen.utils.Misc;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JTable;

/**
 *
 * @author IPSSI
 */
public class PrintGrossSlip {

	private String logoFile = "C:" + File.separator + "ExpenceManagement" + File.separator + "report_logo.png";
	// private String logoFile = "com"+ File.separator +"ipssi"+ File.separator
	// +"expence"+ File.separator +"report_logo.png"; // for build
	// static File f = new File(logoFile);
	// static String logopath = f.getAbsolutePath();
	static BaseColor headingColor = new BaseColor(102);
	static BaseColor cellDataColor = new BaseColor(15525606);
	static Font subHeadingFont = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.BOLD, headingColor);
	static Font headingFont = new Font(Font.FontFamily.TIMES_ROMAN, 7, Font.BOLD, headingColor);
	static Font cellDataFont = new Font(Font.FontFamily.TIMES_ROMAN, 7, Font.NORMAL, BaseColor.BLACK);

	public static void print(File file, JTable table) {

		Document document = null;
		PdfWriter docWriter = null;
		DecimalFormat df = new DecimalFormat("0.00");

		try {
			document = new Document();

			// Font bfBold12 = new Font(FontFamily.TIMES_ROMAN, 9, Font.BOLD, new
			// BaseColor(0, 0, 0));
			// Font bf12 = new Font(FontFamily.TIMES_ROMAN, 9);

			String path = file.getAbsolutePath();
			String fileName = file.getName();
			docWriter = PdfWriter.getInstance(document, new FileOutputStream(path));
			document.open();
			// specify column widths
			float[] columnWidths = { .1f, .3f, .4f, .4f, .9f, .4f };
			// create PDF table with the given widths
			PdfPTable pdfTable = new PdfPTable(columnWidths);
			// set table width a percentage of the page width
			pdfTable.setWidthPercentage(95f);
			insertCell(pdfTable, "S.No", Element.ALIGN_CENTER, 1, headingFont);
			insertCell(pdfTable, "Date", Element.ALIGN_CENTER, 1, headingFont);
			insertCell(pdfTable, "From", Element.ALIGN_CENTER, 1, headingFont);
			insertCell(pdfTable, "To", Element.ALIGN_CENTER, 1, headingFont);
			insertCell(pdfTable, "Details", Element.ALIGN_CENTER, 1, headingFont);
			insertCell(pdfTable, "Amount", Element.ALIGN_CENTER, 1, headingFont);

			// ArrayList<ExpencesBean> dataList = ExpenceDao.fetchJtableData(table);

			// for (int i = 0; i < dataList.size(); i++) {
			// ExpencesBean ExpencesBean = dataList.get(i);
			insertCell(pdfTable, "1", Element.ALIGN_CENTER, 1, cellDataFont);
			insertCell(pdfTable, "Date", Element.ALIGN_CENTER, 1, cellDataFont);
			insertCell(pdfTable, "qq", Element.ALIGN_CENTER, 1, cellDataFont);
			insertCell(pdfTable, "ww", Element.ALIGN_CENTER, 1, cellDataFont);
			insertCell(pdfTable, "2", Element.ALIGN_CENTER, 1, cellDataFont);
			insertCell(pdfTable, "20", Element.ALIGN_CENTER, 1, cellDataFont);
			// }
			insertCell(pdfTable, "", Element.ALIGN_CENTER, 1, cellDataFont);
			insertCell(pdfTable, "", Element.ALIGN_CENTER, 1, cellDataFont);
			insertCell(pdfTable, "", Element.ALIGN_CENTER, 1, cellDataFont);
			insertCell(pdfTable, "", Element.ALIGN_CENTER, 1, cellDataFont);
			insertCell(pdfTable, "Total", Element.ALIGN_CENTER, 1, subHeadingFont);
			insertCell(pdfTable, "10000", Element.ALIGN_CENTER, 2, subHeadingFont);

			// add the paragraph to the document
			Image logo = null;
			String reportName = "Expence Detail";
			SimpleDateFormat indepDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			Paragraph preface = new Paragraph();
			Paragraph preface1 = new Paragraph();
			Paragraph preface2 = new Paragraph();
			// logo = Image.getInstance(logopath);
			logo.scalePercent(85, 70);
			logo.setAlignment(Image.ALIGN_CENTER);
			document.add(logo);

			addEmptyLine(preface, 1);
			preface.add(new Paragraph("   Report generated On: " + indepDateFormat.format(new Date())
					+ "                                  " + reportName, subHeadingFont));
			// preface1.add(new Paragraph(" Rupees: " + " Checked By: " , subHeadingFont));
			// preface1.add(new Paragraph(" Approved: " + " Received: " , subHeadingFont));

			// String labelRupees =Misc.getLabelString("Rupees: ",18 );
			// String labelApproved =Misc.getLabelString("Approved: ",18 );
			// String labelReceived =Misc.getLabelString("Received: ",18 );
			// String labelCheckedBy =Misc.getLabelString("Checked By: ",18 );
			//

			preface1.add(new Paragraph("HEllo1", subHeadingFont));
			preface1.add(new Paragraph("bsjk", subHeadingFont));

			// preface1.add(new Paragraph(,subHeadingFont));
			// preface1.add(new Paragraph(Misc.getLabelString("Approved: ",18
			// ),subHeadingFont));
			// preface1.add(new Paragraph(Misc.getLabelString("Rupees: ",18 )+
			// Misc.getString(EnglishNumberToWords.convert((new
			// Double(MainPage.total_ammount)).longValue()).toUpperCase(),72
			// )+Misc.getLabelString("Checked By: ",18 )+
			// Misc.getString(MainPage.checkedBy.getText(),33 ),subHeadingFont));
			// preface1.add(new Paragraph(Misc.getLabelString("Approved: ",18 )+
			// Misc.getString(MainPage.approved.getText(),72
			// )+Misc.getLabelString("Received: ",18 )+
			// Misc.getString(MainPage.received.getText(),33 ),subHeadingFont));
			addEmptyLine(preface, 1);
			document.add(preface);
			document.add(pdfTable);
			addEmptyLine(preface1, 2);
			document.add(preface1);
			addEmptyLine(preface2, 2);
			document.add(preface2);

		} catch (DocumentException ex) {
			// LoggerNew.Write(ex);
		} catch (Exception ex) {
			// LoggerNew.Write(ex);
		} finally {
			if (document != null) {
				// close the document

				document.close();
			}
			if (docWriter != null) {
				// close the writer
				docWriter.close();
			}
		}
	}

	private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	private static void insertCell(PdfPTable table, String text, int align, int colspan, Font font) {

		// create a new cell with the specified Text and Font
		PdfPCell cell = new PdfPCell(new Phrase(text.trim(), font));
		// set the cell alignment
		cell.setHorizontalAlignment(align);
		// set the cell column span in case you want to merge two or more cells
		cell.setColspan(colspan);
		// in case there is no text and you wan to create an empty row
		if (text.trim().equalsIgnoreCase("")) {
			cell.setMinimumHeight(10f);
		}
		// add the call to the table
		table.addCell(cell);

	}

}
