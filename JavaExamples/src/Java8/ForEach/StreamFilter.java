package Java8.ForEach;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StreamFilter {
	public static void main(String args[]) {
		List<Integer> intList = new ArrayList<Integer>();
		intList.add(21);
		intList.add(52);
		intList.add(31);
		intList.add(10);
		intList.forEach(System.out:: println);
		System.out.println("First way to find even no.");

		List<Integer> newList = intList.stream().filter(p->p%2==0).collect(Collectors.toList());
		newList.forEach(System.out:: println);
		
		System.out.println("Second way to find odd no.");

		intList.removeIf(p -> p % 2 == 0);
		intList.forEach(System.out::println);

		System.out.println("3rd way to find odd no.");
		List<Integer> testList = new ArrayList<Integer>();
		testList.add(21);
		testList.add(52);
		testList.add(31);
		testList.add(10);
		testList.stream().filter(p->p%2==0).collect(Collectors.toList()).forEach(System.out:: println);
		

	}
}
