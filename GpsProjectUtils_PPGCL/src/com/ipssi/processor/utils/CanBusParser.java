package com.ipssi.processor.utils;

import java.util.ArrayList;

import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MiscInner;
import com.ipssi.gen.utils.Pair;

public class CanBusParser {
	public static boolean isCanbusBased(int dimId) {
		return dimId >= 5000 && dimId < 6000;
	}
	
	public static int bitsAtPosInByteLSB(long iv, int zeroBasedIndexFromLSB, int cnt) {
		iv = iv >> (1*zeroBasedIndexFromLSB);
		int mask[] = {0x1,0x3,0x7,0xF,0x1F,0x3F,0x7F,0xFF};
		return (int) (iv & mask[cnt-1]);
	}
	
	public static int byteAtPosLSB(long lv, int zeroBasedIndexFromLSB, int cnt) {
		lv = lv >> 8 * zeroBasedIndexFromLSB;
		if (cnt == 1)
			return (int) (lv & 0xFF);
		else if (cnt == 2)
			return (int) (lv & 0xFFFF);
		else if (cnt == 3)
			return (int)(lv & 0xFFFFFF);
		else
			return (int) lv;
	}
	
	
	public static long byteToUnsignedLong(String str) {
		int iv = Misc.getParamAsInt(str);
		long lv = 0xFF;
		lv = lv & iv;
		return lv;
	}
	public static long intToUnsignedLong(String str) {
		int iv = Misc.getParamAsInt(str);
		long lv = 0xFFFFFFFFL;
		lv = lv & iv;
		return lv;
	}
	public static void interpret(int dimId, String str, ArrayList<Pair<Integer, Double>> result) {
		result.clear();
		switch (dimId) {
			case 5001: //CAN ODO
			{
				long vl = intToUnsignedLong(str);
				if (vl != 0xFFFFFFFFL) {
					double v= vl*5.0/1000.0; 
					result.add(new Pair<Integer, Double>(dimId, v));
				}
				break;
			}
			case 5002: //CAN FUEL Consumed //low res
			{
				long vl = intToUnsignedLong(str);
				if (vl != 0xFFFFFFFFL) {
					double v= vl*0.5; 
					result.add(new Pair<Integer, Double>(dimId, v));
				}
				break;
			}
			case 5003: //CAN Engine Hours
			{
				long vl = intToUnsignedLong(str);
				if (vl != 0xFFFFFFFFL) {
					double v= vl*0.05; 
					result.add(new Pair<Integer, Double>(dimId, v));
				}
				break;
			}
			case 5004: //CAN Coolant temp
			{
				long vl = byteToUnsignedLong(str);
				if (vl != 0xFFL) {
					double v= vl*1-40; 
					result.add(new Pair<Integer, Double>(dimId, v));
				}
				break;
			}
			case 5005: //CAN Service Dur Km
			{
				long vl = intToUnsignedLong(str);
				if (vl != 0xFFFFL) {
					double v= vl*5 - 160635; 
					result.add(new Pair<Integer, Double>(dimId, v));
				}
				break;
			}
			case 5006: //CAN Wt Comb
			{
				long vl = intToUnsignedLong(str);
				if (vl != 0xFFFFL) {
					double v= vl*10/1000; 
					result.add(new Pair<Integer, Double>(dimId, v));
				}
				break;
			}
			case 5007: //Fuel level percent
			{
				long vl = byteToUnsignedLong(str);
				if (vl != 0xFFL) {
					double v= vl*0.4; 
					result.add(new Pair<Integer, Double>(dimId, v));
				}
				break;
			}
			case 5008: //PTO Indep
			{
				long vl = byteToUnsignedLong(str);
				vl = CanBusParser.bitsAtPosInByteLSB(vl, 0, 2);
				if (vl <= 1) {
					double v= vl; 
					result.add(new Pair<Integer, Double>(dimId, v));
				}
				break;
			}
			case 5009: //CCVS+PTO
			{
				long lv = intToUnsignedLong(str);
				int byte23 = byteAtPosLSB(lv, 0, 2); //byt2 anf bt3 but we are ignoring 0
				int byte4 = byteAtPosLSB(lv, 2, 1);
				int byte7 = byteAtPosLSB(lv, 3,1);
				if (byte23 != 0xFFFFL) {
					double wheelBasedSpeed  = (double)byte23/256.0;
					result.add(new Pair<Integer, Double>(5010, wheelBasedSpeed));
				}

				int clutchSwitch = bitsAtPosInByteLSB(byte4, 6, 2);
				if (clutchSwitch != 3)
					result.add(new Pair<Integer, Double>(5011, (double) clutchSwitch));
				int brakeSwitch = bitsAtPosInByteLSB(byte4,4,2);
				if (brakeSwitch != 3)
					result.add(new Pair<Integer, Double>(5012, (double) brakeSwitch));
				int cruiseSwitch = bitsAtPosInByteLSB(byte4,0,2);
				if (cruiseSwitch != 3)
					result.add(new Pair<Integer, Double>(5013, (double) cruiseSwitch));
				byte7 = bitsAtPosInByteLSB(byte7, 0, 5);
				if (byte7 != 0x1FL)
					result.add(new Pair<Integer, Double>(5014, (double) byte7));
				
				break;
			}
			case 5015: //Torque+RPM
			{
				long lv = intToUnsignedLong(str);
				int byte3 = byteAtPosLSB(lv, 0, 1); //byt2 anf bt3 but we are ignoring 0
				int byte45 = byteAtPosLSB(lv, 1, 2);
				if (byte3 != 0xFFL)
					result.add(new Pair<Integer, Double>(5016, byte3*1.0-125.0));
				if (byte45 != 0xFFFFL)
					result.add(new Pair<Integer, Double>(5017, byte45*0.125));
				break;
			}
			case 5018: //Combo Fuel Economy
			{
				long lv = intToUnsignedLong(str);
				int byte12 = byteAtPosLSB(lv, 0, 2); //byt2 anf bt3 but we are ignoring 0
				int byte34 = byteAtPosLSB(lv, 2, 2);
				if (byte12 != 0xFFFFL)
					result.add(new Pair<Integer, Double>(5019, byte12*0.05));
				if (byte34 != 0xFFFFL)
					result.add(new Pair<Integer, Double>(5020, byte34/512.0));
				break;
			}
			case 5021: //Accl+EngineLoad
			{
				long lv = intToUnsignedLong(str);
				int byte2 = byteAtPosLSB(lv, 0, 1); //byt2 anf bt3 but we are ignoring 0
				int byte3 = byteAtPosLSB(lv, 1, 1);
				if (byte2 != 0xFFL)
					result.add(new Pair<Integer, Double>(5022, byte2*0.4));
				if (byte3 != 0xFFL)
					result.add(new Pair<Integer, Double>(5023, byte3*1.0));
				break;
			}
			default: { //for tell tale
				break;
			}
		}
	}
}
