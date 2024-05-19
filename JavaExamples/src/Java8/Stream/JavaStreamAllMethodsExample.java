package Java8.Stream;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.poi.util.SystemOutLogger;

public class JavaStreamAllMethodsExample {
	public static void main(String[] args) {
		allMatchMethod();
		anyMatchMethod();
		// concatMethod();
		// optionalFindAnyMethod();
		// averagingDouble();
		// listFilterExampleMphasis();
		// listFilterExampleMphasis2();
		// findChar();

		// anyMatchSTringDataTypeMethod();

		// findFirstCharAtUpperCase();
	}

	private static void listFilterExampleMphasis() {
		List<Integer> arr1 = Arrays.asList(43, 54, 46, 56);
		List<Integer> arr2 = Arrays.asList(43, 54, 55, 46);

		// prime No
		// arr1.stream().filter(str->str % 2 == 0).collect(Collectors.toList());
		// arr1.stream().filter(str->str % 2 ==
		// 0).map(st->st).forEach(System.out::println);
		System.out.println("listFilterExampleMphasis: ");
		Stream.concat(arr1.stream(), arr2.stream()).distinct().forEach(System.out::println);
	}

	private static void listFilterExampleMphasis2() {
		Stream<Integer> arr1 = Stream.of(43, 54, 46, 56);
		Stream<Integer> arr2 = Stream.of(43, 54, 55, 46);

		// prime No
		// arr1.filter(str->str % 2 == 0).collect(Collectors.toList());
		// arr1.filter(str->str % 2 == 0).map(st->st).forEach(System.out::println);

		System.out.println("listFilterExampleMphasis2: ");
		Stream.concat(arr1, arr2).distinct().forEach(System.out::println);
	}

	private static void averagingDouble() {
		// averagingDouble

		Double data = null;
		// 1st way
		Stream<Double> dob = Stream.of(12.0, 14.0, 16.0, 10.0);
		data = dob.collect(Collectors.averagingDouble(s -> s));
		System.out.println(data);
		// // 2nd way
		Stream<String> str = Stream.of("12", "12", "15", "10");
		data = str.collect(Collectors.averagingDouble(s -> Double.parseDouble(s)));
		System.out.println(data);

		// 3rd way
		Stream<String> str1 = Stream.of("12", "12", "15", "10");
		data = str1.collect(Collectors.averagingInt(s -> Integer.parseInt(s)));
		System.out.println(data);
	}

	private static void optionalFindAnyMethod() {
		// 1st
		Stream<String> st1 = Stream.of("STRING", "IS", "AN", "OBJECT");
		Optional<String> str = st1.findAny();
		if (str.isPresent()) {
			System.out.println(str.get());
		} else {
			System.out.println("No Value");
		}

		// 2nd
		IntStream stream = IntStream.of(4, 5, 8, 10, 12, 16).parallel();

		stream = stream.filter(i -> i % 4 == 0);
		OptionalInt answer = stream.findAny();
		if (answer.isPresent()) {
			System.out.println(answer.getAsInt());
		}
	}

	private static void concatMethod() {
		// 1st
		Stream<String> st1 = Stream.of("STRING", "IS", "AN", "OBJECT");
		Stream<String> st2 = Stream.of("STRING", "IS", "AN", "OBJECT");
		// Stream<String> st2 = Stream.of("STRING IS AN OBJECT");
		Stream.concat(st1, st2).distinct().forEach(s -> System.out.println("1st: " + s));

		System.out.println("\n");

		// 2nd
		Stream<String> st3 = Stream.of("A", "B");
		Stream<String> st4 = Stream.of("C", "D");
		Stream<String> st5 = Stream.of("E", "F");
		Stream<String> st6 = Stream.of("G", "H");

		Stream.concat(Stream.concat(st3, st4), Stream.concat(st5, st6)).distinct()
				.forEach(s -> System.out.println("2nd: " + s));

		System.out.println("\n");
		// 3rd
		DoubleStream Stream1 = DoubleStream.of(1520, 1620);
		DoubleStream Stream2 = DoubleStream.of(1720, 1820, 1620);
		// DoubleStream.concat(Stream1, Stream2).forEach(element ->
		// System.out.println(element));
		DoubleStream.concat(Stream1, Stream2).distinct().forEach(element -> System.out.println("3rd: " + element));
		System.out.println("\n");
		// 4th
		IntStream intStream1 = IntStream.of(1520, 1620);
		IntStream intStream2 = IntStream.of(1720, 1820, 1620);
		IntStream.concat(intStream1, intStream2).distinct().forEach(s -> System.out.println("4th: " + s));
	}

	private static void anyMatchMethod() {
		boolean status = false;
		// 1st
		List<Integer> al = Arrays.asList(3, 5, 9);
		status = al.stream().anyMatch(st -> st % 2 == 0);
		System.out.println("1st: " + status);
	}

	private static void anyMatchSTringDataTypeMethod() {
		boolean status = false;
		// 1st
		List<Integer> al = Arrays.asList(3, 5, 9);
		status = al.stream().anyMatch(st -> st instanceof Integer);
		System.out.println("1st: " + status);
	}

	private static void allMatchMethod() {
		boolean status = false;
		// 1st
		List<Integer> al = Arrays.asList(2, 4, 6, 8, 10);
		status = al.stream().allMatch(st -> st % 2 == 0);
		System.out.println("1st: " + status);

		// 2nd

		Stream<String> str = Stream.of("fg", "dfghj");
		status = str.allMatch(s -> s.length() > 2);
		System.out.println("2nd: " + status);

		// 3rd
		Stream<Integer> strInt = Stream.of(2, 4, 6, 8, 9);
		status = strInt.allMatch(st -> st % 2 == 0);
		System.out.println("3rd: " + status);

		// 4th check; 1st letter in uppercase
		Stream<String> strUp = Stream.of("Fg", "Dfghj");
		status = strUp.allMatch(s -> Character.isUpperCase(s.charAt(0)));
		System.out.println("4th: " + status);

		// 5TH
		String sample = "Om Sarve Bhavantu Sukhinah";
		IntStream intStrate = sample.chars();
		status = intStrate.allMatch(s -> s > 100);
		System.out.println("5th: " + status);
	}

	private static void findChar() {
		// String[] str = ["1","1","0","0","1","0","0","1","1"];

		Stream<String> str = Stream.of("1", "1", "1", "0", "1");
		// Stream<String> str2 = Stream.of("1", "1", "1", "0", "1");
		List<String> list1 = str.filter(s -> Integer.parseInt(s) == 1).map(m -> m).collect(Collectors.toList());
		System.out.println(list1);
		List<String> list2 = str.filter(s -> Integer.parseInt(s) == 0).map(m -> m).collect(Collectors.toList());
		System.out.println(list2);
	}

	private static void findFirstCharAtUpperCase() {
		Stream<String> st = Stream.of("Abc", "Boy", "qwerty");
		st.filter(s -> Character.isUpperCase(s.charAt(0))).collect(Collectors.toList()).forEach(System.out::println);
	}
}
