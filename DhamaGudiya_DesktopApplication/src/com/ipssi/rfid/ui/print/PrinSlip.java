/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipssi.rfid.ui.print;

import com.ipssi.rfid.beans.TPRecord;
import com.ipssi.rfid.constant.ScreenConstant;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author IPSSI
 */
public class PrinSlip {

	static BaseColor headingColor = new BaseColor(102);
	static BaseColor cellDataColor = new BaseColor(15525606);
	private static Font subHeadingFont = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.BOLD, headingColor);
	private static Font headingFont = new Font(Font.FontFamily.TIMES_ROMAN, 7, Font.BOLD, headingColor);
	private static Font cellDataFont = new Font(Font.FontFamily.TIMES_ROMAN, 7, Font.NORMAL, BaseColor.BLACK);

	static Document document = null;
	static PdfWriter docWriter = null;
	TPRecord tpRecord = null;
	final static String Base = "";
	static String path = Base + "test.pdf";

	private void printSlip(TPRecord tpRecord) {
		this.tpRecord = tpRecord;
	}

	private static void printTableData() {
		String[] tableColsRowFirst = { "Sr. No", "Description of Goods", "HSN", "Qty", "Unit", "Rate(Per Item)",
				"Total", "Discount", "Taxable Value", "CGST", "SGST", "IGST" };
		String[] tableColsRowSecond = { "", "Rate(%)", "Amt (INR)", "Rate(%)", "Amt (INR)", "Rate(%)", "Amt (INR)" };
		float[] columnWidthsFirst = { .2f, .72f, .5f, .2f, .3f, .4f, .3f, .3f, .3f, .27f, .2f, .27f, .2f, .27f, .2f };
		document = new Document();
		// DecimalFormat df = new DecimalFormat("0.00");
		PdfPTable pdfTable = new PdfPTable(columnWidthsFirst);
		try {
			docWriter = PdfWriter.getInstance(document, new FileOutputStream(path));
			document.open();
			pdfTable.setWidthPercentage(100f);// set table width a percentage of the page width
			// First row
			insertCell(pdfTable, tableColsRowFirst[0], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowFirst[1], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowFirst[2], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowFirst[3], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowFirst[4], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowFirst[5], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowFirst[6], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowFirst[7], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowFirst[8], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowFirst[9], Element.ALIGN_LEFT, 2, headingFont);
			insertCell(pdfTable, tableColsRowFirst[10], Element.ALIGN_LEFT, 2, headingFont);
			insertCell(pdfTable, tableColsRowFirst[11], Element.ALIGN_LEFT, 2, headingFont);
			// 2nd row
			insertCell(pdfTable, tableColsRowSecond[0], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[0], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[0], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[0], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[0], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[0], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[0], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[0], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[0], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[1], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[2], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[3], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[4], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[5], Element.ALIGN_LEFT, 1, headingFont);
			insertCell(pdfTable, tableColsRowSecond[6], Element.ALIGN_LEFT, 1, headingFont);

			//
			// insertCell(pdfTable, tableColsRowFirst[0], Element.ALIGN_LEFT, 1,
			// headingFont);
			// insertCell(pdfTable,tableColsRowFirst[1], Element.ALIGN_LEFT, 1,
			// headingFont);
			// insertCell(pdfTable,tableColsRowFirst[2], Element.ALIGN_LEFT, 1,
			// headingFont);
			// insertCell(pdfTable,tableColsRowFirst[3], Element.ALIGN_LEFT, 1,
			// headingFont);
			// insertCell(pdfTable,tableColsRowFirst[4], Element.ALIGN_LEFT, 1,
			// headingFont);
			// insertCell(pdfTable,tableColsRowFirst[5], Element.ALIGN_LEFT, 1,
			// headingFont);
			// insertCell(pdfTable,tableColsRowFirst[6], Element.ALIGN_LEFT, 1,
			// headingFont);
			// insertCell(pdfTable,tableColsRowFirst[7], Element.ALIGN_LEFT, 1,
			// headingFont);
			// insertCell(pdfTable,tableColsRowFirst[8], Element.ALIGN_LEFT, 1,
			// headingFont);
			// insertCell(pdfTable,tableColsRowFirst[9], Element.ALIGN_LEFT, 2,
			// headingFont);
			// insertCell(pdfTable,tableColsRowFirst[10], Element.ALIGN_LEFT, 2,
			// headingFont);
			// insertCell(pdfTable,tableColsRowFirst[11], Element.ALIGN_LEFT, 2,
			// headingFont);
			//
			document.add(pdfTable);
			document.open();
		} catch (Exception e) {
			System.out.println(e);
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

	public static void main(String[] args) {
		printTableData();
	}
}
