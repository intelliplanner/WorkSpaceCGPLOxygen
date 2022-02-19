package Java8.reduce;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class ReduceStream {
	public static void main(String[] args) {
		test1();
		test2();
		test4();
		test5();
		test6();

	}

	private static void test6() {
		// TODO Auto-generated method stub

	}

	private static void test5() {
		int product = IntStream.range(2, 8).reduce((num1, num2) -> num1 * num2).orElse(-1);

		// Displaying the product
		System.out.println("The product is : " + product);
	}

	private static void test4() {
		List<Integer> array = Arrays.asList(-2, 0, 4, 6, 8);

		// Finding sum of all elements
		int sum = array.stream().reduce(0, (element1, element2) -> element1 + element2);

		// Displaying sum of all elements
		System.out.println("The sum of all elements is " + sum);
	}

	private static void test1() {
		String[] array = { "Geeks", "for", "Geeks" };
		Optional<String> String_combine = Arrays.stream(array).reduce((str1, str2) -> str1 + "-" + str2);
		if (String_combine.isPresent()) {
			System.out.println(String_combine.get());
		}
	}

	private static void test2() {
		List<String> words = Arrays.asList("GFG", "Geeks", "for", "GeeksQuiz", "GeeksforGeeks");

		Optional<String> longestString = words.stream().reduce((word1, word2) -> word1.length() > word2.length() ? word1 : word2);

		longestString.ifPresent(System.out::println);
	}

}
