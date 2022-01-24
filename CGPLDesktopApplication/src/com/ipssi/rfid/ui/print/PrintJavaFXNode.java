package com.ipssi.rfid.ui.print;

import javafx.print.PrinterJob;

public class PrintJavaFXNode {
//	private void print(Node node) 
	private void print(String node) 
    {
        // Define the Job Status Message
//        jobStatus.textProperty().unbind();
//        jobStatus.setText("Creating a printer job...");
//         
        // Create a printer job for the default printer
        PrinterJob job = PrinterJob.createPrinterJob();
         
        if (job != null) 
        {
            // Show the printer job status
//            jobStatus.textProperty().bind(job.jobStatusProperty().asString());
             
            // Print the node
            boolean printed = false;//  job.printPage(node);
 
            if (printed) 
            {
                // End the printer job
                job.endJob();
            } 
            else
            {
                // Write Error Message
//                jobStatus.textProperty().unbind();
//                jobStatus.setText("Printing failed.");
            }
        } 
        else
        {
            // Write Error Message
//            jobStatus.setText("Could not create a printer job.");
        }
    }   
}
