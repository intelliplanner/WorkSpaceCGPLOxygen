package a_example;

import java.util.HashMap;

public class StringCharCount {

	public static void main(String[] args) {
			String str = "Virendraa";
			HashMap<String, Integer> map = new HashMap<>();
			int count = 0;
			for (int i = 0; i < str.length(); i++) {
				count = 0;
				String chars = str.charAt(i)+""; 
				if(map.containsKey(chars)) {
					count =  map.get(chars);
					map.put(chars, ++count);
				}
				else {
					map.put(chars, ++count);
				}
			} 
			
			System.out.println(map);
	}

}
