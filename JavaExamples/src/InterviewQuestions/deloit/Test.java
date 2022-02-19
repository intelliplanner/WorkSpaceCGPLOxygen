package InterviewQuestions.deloit;

import java.util.HashMap;
import java.util.Map;

public class Test {

	public static void main(String[] args) {
		String str = "Great great responsibility";
		HashMap<String,Integer> map = new HashMap<>();
		int i=0;
		for(i=0; i < str.length()-1 ;i++){
			int count = 0;
			if(map.containsKey(str.charAt(i)+"")) {
				count = map.get(str.charAt(i)+"");
				map.put(str.charAt(i) +"", ++count);
			}else {
				map.put(str.charAt(i)+"", ++count);
			}
		}
		
		for(Map.Entry<String,Integer> entry:map.entrySet()) {
			if(entry.getValue()>1) {
				System.out.println(entry.getKey());
			}
		}
		
		
	}

}
