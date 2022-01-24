package com.ipssi.cgplSap;

public class RecordType {
	  public static class MessageType {
		    public static final int ANY = -1;
	        public static final int NO_RESPONSE = 0;
	        public static final int SUCCESS = 1;
	        public static final int FAILED  = 2;
	        public static final int CANCEL  = 9; // cancelled from web
	        

			public static String getStr(int id) {
				switch (id) {
				case ANY:
					return "Any";
				case NO_RESPONSE:
					return "No Response";
				case SUCCESS:
					return "Success";
				case FAILED:
					return "Failed";
				case CANCEL:
					return "Cancelled";
				default:
					return "NA";
				}
			}
	    }

}
