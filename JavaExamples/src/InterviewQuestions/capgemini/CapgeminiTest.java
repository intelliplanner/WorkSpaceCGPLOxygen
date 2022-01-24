package InterviewQuestions.capgemini;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
/* Find max occuring integer value from array */
public class CapgeminiTest { 
	public static void main(String[] args) {
		List<Integer> li = Arrays.asList(12, 67, 45, 12, 65, 65, 45, 78, 12, 65);
		HashMap<Integer, Integer> pair = new HashMap<Integer, Integer>();
		int count = 0;
		for (Integer i : li) {
			count = 0;
			if (pair.containsKey(i)) {
				count = pair.get(i);
				pair.put(i, ++count);
			} else {
				pair.put(i, ++count);
			}
		}
		System.out.println(pair);

		List<Integer> li2 = li.stream().distinct().collect(Collectors.toList());
		int maxCountvalue = 0;
		int value = 0;
		HashMap<Integer, Integer> pairNew = new HashMap<Integer, Integer>();
		for (Integer i : li2) {
			if (maxCountvalue <= pair.get(i)) {
				maxCountvalue = pair.get(i);
//				value=i;
				pairNew.put(i, pair.get(i));
			}
				
		}
//		System.out.println("MaxCountValue:" + maxCountvalue +" value "+ value);
		System.out.println(pairNew);
	}
}
