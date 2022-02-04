package a_example;

import java.util.ArrayList;
import java.util.List;

public class Ana {

	public static void main(String[] args) {
		String str1 = "TRIGNALE";
		String str2 = "TRIANGLE";
		List<String> arr = new ArrayList<>();
		for (int i = 0; i < str1.length(); i++) {
			for (int j = 0; j < str2.length(); j++) {
				if(str1.charAt(i) == str2.charAt(j) && !arr.contains(str2.charAt(j))) {
					arr.add(str1.charAt(i)+"");
					break;
				}
			}
		}
	}

}
