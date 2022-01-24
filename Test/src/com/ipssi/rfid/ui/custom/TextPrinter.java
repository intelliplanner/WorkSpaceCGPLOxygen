package com.ipssi.rfid.ui.custom;

import java.io.FileWriter;
import java.io.IOException;

public class TextPrinter {

	public static void print(){
		try (FileWriter out = new FileWriter("LPT1:")) {
			out.write("String1\nString2\nString3\n");
			out.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

