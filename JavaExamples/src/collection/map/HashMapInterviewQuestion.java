package collection.map;

import java.util.*;
import java.util.Map.Entry;

public class HashMapInterviewQuestion {

	/*
	 * HashMap implements map interface and contains key value pair,
	 * HashMap maintain no Order,
	 *  Not contain duplicate key, 
	 * and can contain single null key or multiple null values
	 */

	public static void main(String[] args) {
		
		String a1="ABC";
		String a2="ABC";
		String a3=new String("ABC");  
		HashMap hm = new HashMap<String,Integer>();
		hm.put(a1,2);
		hm.put(a2,1);
		hm.put(a3,3);
		System.out.println(hm.size());

		Scanner sc = new Scanner(System.in);
		String str = sc.nextLine();
		System.out.println(str);
		print(str);
	
	}

	private static void print(String str) {
		HashMap<Integer, Integer> hm = new HashMap<>();
		String [] str1 = str.split(" ");
		int count = 0;
		for(int i=0;i<str1.length;i++) {
			count = 0;
			int val = Integer.valueOf(str1[i]);
			if(hm.size()>0 &&  hm.containsKey(val)) {
				count = hm.get(val);
				hm.put(val, ++count);
			}else {
				hm.put(val, ++count);
			}
		}
		
		for(Map.Entry<Integer, Integer> m:hm.entrySet()) {
			System.out.println(m.getKey()+":"+m.getValue());
		}
	}
	
	

}
