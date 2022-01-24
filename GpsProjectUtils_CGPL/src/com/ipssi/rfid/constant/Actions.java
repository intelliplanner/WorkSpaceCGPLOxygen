package com.ipssi.rfid.constant;

public class Actions {

	public static class Token{
		public static final int PENDING = 0;
		public static final int ALLOW_NEXT_STEP = 1;
		public static final int BLOCK_STEP = 2;
		public static final int BLOCK_NEXT_STEP = 3;
		public static final int BLOCK_NEXT_TRIP_TILL = 4;
		public static final int BLOCK_NEXT_TRIP_PAPER = 5;
		public static final int BLOCK_PERMANENT = 6;
	}
}
