package Java8.ForEach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StreamFilter {
	public static void main(String args[]) {
		convertListToMap();
		// ListFilterUsinfStream();

	}

	private static void ListFilterUsinfStream() {
		List<Integer> intList = new ArrayList<Integer>();
		intList.add(21);
		intList.add(52);
		intList.add(31);
		intList.add(10);
		intList.forEach(System.out::println);
		System.out.println("First way to find even no.");

		List<Integer> newList = intList.stream().filter(p -> p % 2 == 0).collect(Collectors.toList());
		newList.forEach(System.out::println);

		System.out.println("Second way to find odd no.");

		intList.removeIf(p -> p % 2 == 0);
		intList.forEach(System.out::println);

		System.out.println("3rd way to find odd no.");
		List<Integer> testList = new ArrayList<Integer>();
		testList.add(21);
		testList.add(52);
		testList.add(31);
		testList.add(10);
		testList.stream().filter(p -> p % 2 == 0).collect(Collectors.toList()).forEach(System.out::println);
	}

	private static void convertListToMap() {
		List<Integer> intList = new ArrayList<Integer>();
		intList.add(21);
		intList.add(52);
		intList.add(31);
		intList.add(10);
		Map<Integer, Integer> map = intList.stream().collect(Collectors.toMap(Integer::intValue, Integer::intValue));
		System.out.println(map);

		List<String> testList = new ArrayList<String>();
		testList.add("ABC");
		testList.add("BCD");
		testList.add("DEF");
		Map<String, String> mapList = testList.stream().collect(Collectors.toMap(String::toString, String::toString));
		System.out.println(mapList);

		List<String> srt = Arrays.asList("Asd", "SED", "FES");
		srt.add("ss");// Runtime Exception java.lang.UnsupportedOperationException
		srt.stream().collect(Collectors.toList()).forEach(System.out::println);

	}
}
