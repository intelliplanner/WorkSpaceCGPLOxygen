package Java8.Stream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamExample {
	public static void main(String[] args) {
		 getArrayListNthValue();
//		EveryNth();
	}

	private static void getArrayListNthValue() {
		int nth = 2;
		List<Integer> arr = Arrays.asList(10, 21, 32, 43, 54, 64, 74);

		// IntStream.range(0, arr.size()-1).filter(i -> (nth-1)-i == 0).map(m->
		// arr.get(m)).forEach(System.out::println);

		IntStream.range(0, arr.size() - 1).filter(i -> (nth - 1) - i == 0).mapToObj(m -> arr.toArray()[m])
				.forEach(System.out::println);

		String[] names = { "", "", "", "", "" };
		List<String> evenIndexedNames = IntStream.range(0, names.length).filter(i -> i % 2 == 0).mapToObj(i -> names[i])
				.collect(Collectors.toList());

		System.out.println(evenIndexedNames);
	}

	private static void EveryNth() {
		int nth = 2;
		List<Integer> lists = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
		IntStream.range(0, nth).forEach(i -> System.out.println(i));
	}

	// @Test
	// public void whenCalled_thenReturnListOfEvenIndexedStrings() {
	// String[] names
	// = {"Afrim", "Bashkim", "Besim", "Lulzim", "Durim", "Shpetim"};
	// List<String> expectedResult
	// = Arrays.asList("Afrim", "Besim", "Durim");
	// List<String> actualResult
	// = StreamIndices.getEvenIndexedStrings(names);
	//
	// assertEquals(expectedResult, actualResult);
	// }
}
