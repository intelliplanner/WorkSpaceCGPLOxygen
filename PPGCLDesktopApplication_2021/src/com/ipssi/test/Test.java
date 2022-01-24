package com.ipssi.test;

public class Test {
	public static void main(String[] args) {
//		String str = "    740 kg    G 000000 A";
//		System.out.println(str);
//		System.out.println(str.substring(0,str.indexOf("kg")).trim());
		
		reverseStringNew("test");
	}
	
	private static void reverseStringNew(String string) {
        String str="Aman";
//        char[] ch = str.toCharArray();
        StringBuilder  newStr = new StringBuilder();
//        for(int i=str.length()-1;i>=0;i--){
//            newStr.append(ch[i]);
//        }
        for(int i=str.length()-1;i>=0;i--){
        	newStr.append(str.charAt(i));
        }
        
        System.out.println(newStr.toString());
        
        
}

}

