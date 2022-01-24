/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package profilemanagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JTable;

/**
 *
 * @author rajeev
 */
public class ExcelExporter {
     
    public void exportTable(JTable jTable1,File file) throws IOException{
      PdfExporter pdfobj = new PdfExporter();
      ArrayList<HoldingRegistrationData>  dataList = pdfobj.fetchJtableData(jTable1);
      FileWriter out=new FileWriter(file);
      BufferedWriter bw=new BufferedWriter(out);
          bw.write("Name"+"\t");
          bw.write("Father Name"+"\t");
          bw.write("Phone_No."+"\t");
          bw.write("Mobile_No"+"\t");
          bw.write("Email_Id"+"\t");
          bw.write("DateOfBirth"+"\t");
          bw.write("Address"+"\t");
          bw.write("City"+"\t");
          bw.write("State"+"\t");
          bw.write("PinCode"+"\t");
          bw.write("Notes");
          bw.write("\n");
      for (int i=0;i<dataList.size();i++){
          HoldingRegistrationData holdingRegistrationData = dataList.get(i);
          bw.write(holdingRegistrationData.getFname()+"\t");
          bw.write(holdingRegistrationData.getFather_Name()+"\t");
          bw.write(holdingRegistrationData.getPhone_No()+"\t");
          bw.write(holdingRegistrationData.getMobile_No()+"\t");
          bw.write(holdingRegistrationData.getEmail_ID()+"\t");
          bw.write(holdingRegistrationData.getDateBirth()+"\t");
          bw.write(holdingRegistrationData.getAddress()+"\t");
          bw.write(holdingRegistrationData.getCity()+"\t");
          bw.write(holdingRegistrationData.getState()+"\t");
          bw.write(holdingRegistrationData.getPinCode()+"\t");
          bw.write(holdingRegistrationData.getNotes());
          bw.write("\n");
        }
          bw.close();
          out.close();
      }
  
}