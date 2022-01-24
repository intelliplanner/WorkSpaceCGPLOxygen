package Java8.collector;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectorExample {

	public static void averagingDouble() {
		Stream<String> s = Stream.of("3", "4", "5");
		// using Collectors averagingDouble(ToDoubleFunction mapper)
		// method to find arithmetic mean of inputs given
		double ans = s.collect(Collectors.averagingDouble(num -> Double.parseDouble(num)));
		System.out.println(ans);
	}

	public static void toSet() {
		// creating a Stream of strings
		Stream<String> s = Stream.of("1", "2", "3", "4");
		// using Collectors toSet() function
		Set<String> mySet = s.collect(Collectors.toSet());
		// printing the elements
		System.out.println(mySet);
	}

	public static void allMatch() {
		List<Integer> list = Arrays.asList(3, 4, 6, 12, 20);

		// Check if all elements of stream
		// are divisible by 3 or not using
		// Stream allMatch(Predicate predicate)
		boolean answer = list.stream().allMatch(n -> n % 3 == 0);
		
		Stream<String> stream = Stream.of("Geeks", "for", "GeeksQuiz", "GeeksforGeeks");

		// Check if all elements of stream 
		// have length greater than 2 using 
		// Stream allMatch(Predicate predicate) 
		boolean answer1 = stream.allMatch(str -> str.length() > 2);

// Displaying the result 
		System.out.println(answer1);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		-- averagingDouble();
		toSet();
	}

}
