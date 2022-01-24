package com.ipssi.rfid.uiconstant;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ipssi.rfid.constant.UIConstant;

public class test {
	public static void main(String s[]) {
//		System.out.println(UIConstant.requireFormat.format(new Date()));
		int decimalPlaces = 2;
	    BigDecimal bd = new BigDecimal(0.28);
	     
	    bd =  bd.setScale(2, BigDecimal.ROUND_DOWN);
//	    String string = bd.toString();
	    System.out.println(bd);
	}
}
