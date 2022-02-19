package Java8.reduce;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReduceStreamExample {

	public static void main(String[] args) {
		reduce1();
		reduce2();
		reduce3();
		findDuplicate1();
		findDuplicate2();
	}

	private static void findDuplicate1() {
		List<String> list = Arrays.asList("Geeks","GFG", "Geeks", "GeeksQuiz","for", "GeeksQuiz", "GeeksforGeeks");
		HashSet<String> set = new HashSet<>();
		Set<String> s =  list.stream().filter(n-> !set.add(n)).collect(Collectors.toSet());
		// Set.add() returns false if the element was already in the set.
		System.out.println(s); 
		
		
	}

	private static void findDuplicate2() {
		List<String> list = Arrays.asList("Geeks","GFG", "Geeks", "GeeksQuiz","for", "GeeksQuiz", "GeeksforGeeks");
		Set<String> s = list.stream().filter(i -> Collections.frequency(list, i) > 1).collect(Collectors.toSet());
		System.out.println(s);
		
		
	}

	private static void reduce3() {
		// TODO Auto-generated method stub

	}

	private static void reduce2() {
		// TODO Auto-generated method stub

	}

	private static void reduce1() {
		// TODO Auto-generated method stub

	}

}
