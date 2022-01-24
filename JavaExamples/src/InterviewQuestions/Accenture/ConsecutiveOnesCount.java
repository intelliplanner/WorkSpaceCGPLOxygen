package InterviewQuestions.Accenture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConsecutiveOnesCount {
	/*
	 * Long num = 1100111011 count consecutive one's Output = 3
	 */
	public static void main(String[] args) {
		
//		List<String> str = Arrays.asList("ABC", "EFG", "QWERT", null);
//		str.stream().filter(i -> i != null).collect(Collectors.toList()).forEach(System.out::println);
		// exmple();
		ConsecutiveOnesCountTest();
	}

	private static void ConsecutiveOnesCountTest() {
		
	}

	private static void exmple() {
		Long num = 1100111011l;
		String str = Long.toString(num); // O(1)
		char[] ch = str.toCharArray(); // O(2)

		ArrayList<String> li = new ArrayList<>();
		String st = "";
		for (int i = 0; i < ch.length - 1; i++) {
			if ("1".equalsIgnoreCase(ch[i] + "")) {
				st += ch[i]; // st =11 // st =111 // st=11
			} else {
				if (st.length() > 0) { // 11 00 111 ==> 0 111. ; 1 ,1
					li.add(st); // li = {"11","111"}
					st = "";
				}
			}

			if (i == ch.length - 1 && st.contains("1")) {
				li.add(st);
			}
		}

		li.forEach(System.out::println);

		System.out.println("consicutive one's: " + li.size()); // ? size =

	}
}
