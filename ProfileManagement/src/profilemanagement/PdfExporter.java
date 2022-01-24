/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package profilemanagement;

import aspose.pdf.PageSize;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font.FontFamily;
import javax.swing.JTable;
import java.awt.Graphics2D;
import java.io.FileOutputStream;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Shape;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.table.TableModel;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
 
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 *
 * @author Vi$ky
 */
public class PdfExporter {
    private String logoFile = "C:/Users/Vi$ky/Documents/NetBeansProjects/ProfileManagement/src/profilemanagement/report_logo.png";
     BaseColor headingColor = new BaseColor(102);
    BaseColor cellDataColor = new BaseColor(15525606);
    private Font subHeadingFont = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.BOLD, headingColor);
    Font headingFont = new Font(Font.FontFamily.TIMES_ROMAN, 7, Font.BOLD, headingColor);
    Font cellDataFont = new Font(Font.FontFamily.TIMES_ROMAN, 7, Font.NORMAL, BaseColor.BLACK);
    public void print(File file,JTable table ){ 

 Document document = null;
PdfWriter docWriter = null;
DecimalFormat df = new DecimalFormat("0.00");

try {
    document = new Document();
   
   // Font bfBold12 = new Font(FontFamily.TIMES_ROMAN, 9, Font.BOLD, new BaseColor(0, 0, 0)); 
   // Font bf12 = new Font(FontFamily.TIMES_ROMAN, 9); 
   
    String path = file.getAbsolutePath();
    String fileName = file.getName();
    docWriter = PdfWriter.getInstance(document,  new FileOutputStream(path));
    document.open();
   //specify column widths
   float[] columnWidths = {.2f,.5f,.6f, .6f,.6f,.5f,.6f,.5f,.5f,.5f,.4f,.5f};
   //create PDF table with the given widths
   PdfPTable pdfTable = new PdfPTable(columnWidths);
   // set table width a percentage of the page width
   pdfTable.setWidthPercentage(95f);
   insertCell(pdfTable,"S.N", Element.ALIGN_CENTER, 1, headingFont);
   insertCell(pdfTable,"Name", Element.ALIGN_CENTER, 1, headingFont);
   insertCell(pdfTable, "FatherName", Element.ALIGN_CENTER, 1, headingFont);
   insertCell(pdfTable, "PhoneNo", Element.ALIGN_CENTER, 1, headingFont);
   insertCell(pdfTable, "MobileNo", Element.ALIGN_CENTER, 1, headingFont);
   insertCell(pdfTable, "Email Id", Element.ALIGN_CENTER, 1, headingFont);
   insertCell(pdfTable, "DateOfBirth", Element.ALIGN_CENTER, 1, headingFont);
   insertCell(pdfTable, "Address", Element.ALIGN_CENTER, 1, headingFont);
   insertCell(pdfTable, "City", Element.ALIGN_CENTER, 1, headingFont);
   insertCell(pdfTable, "State", Element.ALIGN_CENTER, 1, headingFont);
   insertCell(pdfTable, "PinCode", Element.ALIGN_CENTER, 1, headingFont);
   insertCell(pdfTable,"Remarks", Element.ALIGN_CENTER, 1, headingFont);
  
     ArrayList<HoldingRegistrationData>  dataList = fetchJtableData(table);
    
     for (int i = 0; i < dataList.size(); i++) {
        HoldingRegistrationData holdingRegistrationData = dataList.get(i);
          insertCell(pdfTable,""+(i+1) , Element.ALIGN_RIGHT, 1, cellDataFont);
          insertCell(pdfTable, holdingRegistrationData.getFname(), Element.ALIGN_RIGHT, 1, cellDataFont);
          insertCell(pdfTable, holdingRegistrationData.getFather_Name(), Element.ALIGN_RIGHT, 1, cellDataFont);
          insertCell(pdfTable, holdingRegistrationData.getPhone_No(), Element.ALIGN_CENTER, 1, cellDataFont);
          insertCell(pdfTable, holdingRegistrationData.getMobile_No(), Element.ALIGN_CENTER, 1, cellDataFont);
          insertCell(pdfTable, holdingRegistrationData.getEmail_ID(), Element.ALIGN_RIGHT, 1, cellDataFont);
          insertCell(pdfTable, holdingRegistrationData.getDateBirth(), Element.ALIGN_CENTER, 1, cellDataFont);
          insertCell(pdfTable, holdingRegistrationData.Address, Element.ALIGN_RIGHT, 1, cellDataFont);
          insertCell(pdfTable, holdingRegistrationData.getCity(), Element.ALIGN_RIGHT, 1, cellDataFont);
          insertCell(pdfTable, holdingRegistrationData.getState(), Element.ALIGN_RIGHT, 1, cellDataFont);
          insertCell(pdfTable, holdingRegistrationData.getPinCode(), Element.ALIGN_CENTER, 1, cellDataFont);
          insertCell(pdfTable, holdingRegistrationData.getNotes(), Element.ALIGN_RIGHT, 1, cellDataFont);
    }
   
    
   // add the paragraph to the document
   Image logo = null;
   String reportName = "Employee Record";
   SimpleDateFormat indepDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
   Paragraph preface = new Paragraph(); 
   Paragraph preface1 = new Paragraph(); 
   logo = Image.getInstance(logoFile);
   logo.scalePercent(85,70);
   logo.setAlignment(Image.ALIGN_CENTER);
   document.add(logo);
   addEmptyLine(preface, 1);
   preface.add(new Paragraph("     Report generated On:"+ indepDateFormat.format(new Date())+"                                  "+reportName,subHeadingFont));
 //  preface.add(new Paragraph(reportName, headingFont));
   addEmptyLine(preface, 1);
   document.add(preface);
   document.add(pdfTable);
 
}  catch (DocumentException dex)
  {
   dex.printStackTrace();
  }
  catch (Exception ex)
  {
   ex.printStackTrace();
  }
  finally
  {
   if (document != null){
    //close the document
    document .close();
   }
   if (docWriter != null){
    //close the writer
    docWriter.close();
   }
  }
    }
    public ArrayList<HoldingRegistrationData> fetchJtableData(JTable table) {
       ArrayList<HoldingRegistrationData> dataList = new ArrayList<HoldingRegistrationData>();
       HoldingRegistrationData bean ;
      TableModel model=table.getModel();
      for (int i=0;i<model.getRowCount();i++){
           bean = new HoldingRegistrationData();
        for (int j=0;j<model.getColumnCount()-2;j++){
            if(j == 0)
                  bean.setFname(model.getValueAt(i,j).toString());
            if(j == 1)
                  bean.setFather_Name(model.getValueAt(i,j).toString());
            if(j == 2)
                   bean.setPhone_No(model.getValueAt(i,j).toString());
            if(j == 3)
                   bean.setMobile_No(model.getValueAt(i,j).toString());
            if(j == 4)
                   bean.setEmail_ID(model.getValueAt(i,j).toString());
            if(j == 5)
                   bean.setDateBirth(model.getValueAt(i,j).toString());
            if(j == 6)
                   bean.setAddress(model.getValueAt(i,j).toString());
            if(j == 7)
                   bean.setCity(model.getValueAt(i,j).toString());
            if(j == 8)
                   bean.setState(model.getValueAt(i,j).toString());
            if(j == 9)
                   bean.setPinCode(model.getValueAt(i,j).toString());
            if(j == 10)
                   bean.setNotes(model.getValueAt(i,j).toString());
        }
        dataList.add(bean);
      }
       return dataList;
    }
     private void insertCell(PdfPTable table, String text, int align, int colspan, Font font){
   
  //create a new cell with the specified Text and Font
  PdfPCell cell = new PdfPCell(new Phrase(text.trim(), font));
  //set the cell alignment
  cell.setHorizontalAlignment(align);
  //set the cell column span in case you want to merge two or more cells
  cell.setColspan(colspan);
  //in case there is no text and you wan to create an empty row
  if(text.trim().equalsIgnoreCase("")){
   cell.setMinimumHeight(10f);
  }
  //add the call to the table
  table.addCell(cell);
   
 }
  private  void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}
}
