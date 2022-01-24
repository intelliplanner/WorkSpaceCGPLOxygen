package collection.map;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyCounts {

	public static void main(String args[]){
		List<String> list = Arrays.asList("a", "a", "a", "b", "b", "c", "d", "e", "a","d");
		HashMap<String,Integer> pairVal = new HashMap<>();

		for(String val :list){
			int count=0;
			if(pairVal.containsKey(val)){
				count = pairVal.get(val);
				pairVal.put(val,++count);
			}else{
				pairVal.put(val,++count);
			}
		}

		for(Map.Entry m : pairVal.entrySet()){
			System.out.println("Key: "+ m.getKey() + " , Value: "+ m.getValue());
		}
}
}
