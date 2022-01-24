package Java8.reduce;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ReduceStream {
	public static void main(String[] args) {
		test1();
		test2();

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

		Optional<String> longestString = words.stream()
				.reduce((word1, word2) -> word1.length() > word2.length() ? word1 : word2);

		longestString.ifPresent(System.out::println);
	}
}
