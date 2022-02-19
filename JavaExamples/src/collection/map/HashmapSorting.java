package collection.map;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.xssf.model.MapInfo;

public class HashmapSorting {
	public static void main(String args[]) {
		// sortedComparingByKey();
		// sortedInReverseComparingByKey();// Java Map Example: comparingByKey() in
		// Descending Order
		// sortedComparingByValue();
		// sortedInReverseComparingByValue();

//		HashMapOrderTest();
		HashMapOrderTest2();
	}

	private static void HashMapOrderTest2() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("Jayant", 80);
		map.put("Abhishek", 90);
		map.put("Anushka", 80);
		map.put("Amit", 75);
		map.put("Danish", 40);
		System.out.println(map);
		
//		map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(System.out::println);
//		map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(System.out::println);
//		map.entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.reverseOrder())).forEach(System.out::println);
//		map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEach(System.out::println);
		
		
//		TreeMap<String, Integer> treemap = new TreeMap<String, Integer>(map);  // maintain acending order
//		System.out.println(treemap);
		
		
		List<Map.Entry<String, Integer>> lists = new LinkedList(map.entrySet());
		
		Collections.sort(lists, (i1,i2) -> i1.getKey().compareTo(i2.getKey() ));
		System.out.println(lists);
		
	}

	private static void HashMapOrderTest() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("Jayant", 80);
		map.put("Abhishek", 90);
		map.put("Anushka", 80);
		map.put("Amit", 75);
		map.put("Danish", 40);
		System.out.println(map);
		TreeMap<String, Integer> treemap = new TreeMap<String, Integer>(map);
		System.out.println(treemap);
		
		List<Map.Entry<String, Integer>>  listHasmap = new LinkedList(map.entrySet());
		
		Collections.sort(
				listHasmap,
	            (i1, i2) -> i1.getKey().compareTo(i2.getKey()));
		Collections.sort(
				listHasmap,
	            (i1, i2) -> i1.getValue() > i2.getValue() ? 1 :  i1.getValue() == i2.getValue() ? 0 : -1  );
		
		System.out.println(listHasmap);
		
		Collections.sort(
				listHasmap,
	            (i1, i2) -> i1.getKey().compareTo(i2.getKey()));
		
		
		System.out.println(listHasmap);
		
	}

	private static void sortedInReverseComparingByValue() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(100, "Amit");
		map.put(101, "Vijay");
		map.put(102, "Rahul");
		System.out.println(map);
		map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.forEach(System.out::println);
	}

	private static void sortedComparingByValue() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(100, "Amit");
		map.put(101, "Vijay");
		map.put(102, "Rahul");
		System.out.println(map);
		map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(System.out::println);
	}

	private static void sortedInReverseComparingByKey() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(100, "Amit");
		map.put(101, "Vijay");
		map.put(102, "Rahul");
		// Returns a Set view of the mappings contained in this map
		map.entrySet()
				// Returns a sequential Stream with this collection as its source
				.stream()
				// Sorted according to the provided Comparator
				.sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
				// Performs an action for each element of this stream
				.forEach(System.out::println);
	}

	private static void sortedComparingByKey() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(100, "Amit");
		map.put(102, "Rahul");
		map.put(101, "Vijay");
		System.out.println(map);
		// Returns a Set view of the mappings contained in this map
		map.entrySet()
				// Returns a sequential Stream with this collection as its source
				.stream()
				// Sorted according to the provided Comparator
				.sorted(Map.Entry.comparingByKey())
				// Performs an action for each element of this stream
				.forEach(System.out::println);

	}
}
