package a_example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TestExample2 {
	public static void main(String args[]) {
		// 1st
//		List<String> lst = new ArrayList<String>(Arrays.asList("a", "b", "c"));
//		for (String value : lst) {
//			if (value.equals("a")) {
//				lst.remove(value);
//			}
//		}
//		System.out.println(lst);

		// 2nd
		Map<String,Integer> mp = new HashMap<String, Integer>();
		mp.put("test1", 1);
		mp.put("test2", 2);
		mp.put("test3", 3);
		mp.put("test1", 4);
		
		System.out.println(mp +", "+ mp.size());
		
	}

}
