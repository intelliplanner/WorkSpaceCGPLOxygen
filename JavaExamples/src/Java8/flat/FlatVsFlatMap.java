package Java8.flat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlatVsFlatMap {
	public static void main(String[] args) {
		map1();
		map2();
		map3();
		flatMap();
		flatMap2();
		flatMap3();
	}

	private static void map3() {
		System.out.println("The stream after applying " + "the function is : ");

		// Creating a list of Integers
		List<Integer> list = Arrays.asList(3, 6, 9, 12, 15);

		// Using Stream map(Function mapper) and
		// displaying the corresponding new stream
		list.stream().map(number -> number * 3).forEach(System.out::println);
	}

	private static void map2() {
		System.out.println("The stream after applying " + "the function is : ");

		// Creating a list of Integers
		List<String> list = Arrays.asList("geeks", "gfg", "g", "e", "e", "k", "s");

		// Using Stream map(Function mapper) to
		// convert the Strings in stream to
		// UpperCase form
		List<String> answer = list.stream().map(String::toUpperCase).collect(Collectors.toList());

		// displaying the new stream of UpperCase Strings
		System.out.println(answer);
	}

	private static void map1() {
		System.out.println("The stream after applying " + "the function is : ");

		// Creating a list of Strings
		List<String> list = Arrays.asList("Geeks", "FOR", "GEEKSQUIZ", "Computer", "Science", "gfg");

		// Using Stream map(Function mapper) and
		// displaying the length of each String
		list.stream().map(str -> str.length()).forEach(System.out::println);
	}

	private static void flatMap3() {

		List<Integer> PrimeNumbers = Arrays.asList(5, 7, 11, 13);

		// Creating a list of Odd Numbers
		List<Integer> OddNumbers = Arrays.asList(1, 3, 5);

		// Creating a list of Even Numbers
		List<Integer> EvenNumbers = Arrays.asList(2, 4, 6, 8);

		List<List<Integer>> listOfListofInts = Arrays.asList(PrimeNumbers, OddNumbers, EvenNumbers);

		System.out.println("The Structure before flattening is : " + listOfListofInts);

		// Using flatMap for transformating and flattening.
		List<Integer> listofInts = listOfListofInts.stream().flatMap(list -> list.stream())
				.collect(Collectors.toList());

		System.out.println("The Structure after flattening is : " + listofInts);
	}

	private static void flatMap() {
		List<String> list = Arrays.asList("5.6", "7.4", "4", "1", "2.3");

		// Using Stream flatMap(Function mapper)
		list.stream().flatMap(num -> Stream.of(num)).forEach(System.out::println);
	}

	private static void flatMap2() {

		// Creating a List of Strings

		// Creating a List of Strings
		List<String> list = Arrays.asList("Geeks", "GFG", "GeeksforGeeks", "gfg");

		// Using Stream flatMap(Function mapper)
		list.stream().flatMap(str -> Stream.of(str.charAt(2))).forEach(System.out::println);

	}

}