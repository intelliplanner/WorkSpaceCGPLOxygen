package inteviewQuestion;

import java.util.Arrays;

public class StringQuestion {
	// Remove space without string methods

	public static void main(String[] args) {
		String str = "Lets Go for Search";
		//removeSpace(str);
		//findLastIndexOfString();
		Findrepeter();

	}

	private static void findLastIndexOfString() {
		// String myStr = "Hello planet earth, you are a great planet.";
		// System.out.println(myStr.lastIndexOf("planet"));

		String myStr1 = "Hello";
		System.out.println(myStr1.lastIndexOf("ll"));

		String str = "This is index of example";
		int index = str.lastIndexOf('s', 7);
		System.out.println(index);
	
		
		
	}

	
	static void Findrepeter(){
	    String s="mmababctamantlslmag";
	    int distinct = 0 ;

	    for (int i = 0; i < s.length(); i++) {

	        for (int j = 0; j < s.length(); j++) {

	            if(s.charAt(i)==s.charAt(j))
	            {
	                distinct++;

	            }
	        }   
	        System.out.println(s.charAt(i)+"--"+distinct);
	        String d=String.valueOf(s.charAt(i)).trim();
	        s=s.replaceAll(d,"");
	        distinct = 0;

	    }

	}
	private static void removeSpace(String str) {

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) != ' ') {

			}
		}
	}
}
