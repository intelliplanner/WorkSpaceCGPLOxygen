package Java8.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class Product {
	int id;
	String name;
	float price;

	public Product(int id, String name, float price) {
		this.id = id;
		this.name = name;
		this.price = price;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

}

public class JavaStreamExample {
	public static void main(String[] args) {
		List<Product> productsList = new ArrayList<Product>();
		// Adding Products
		productsList.add(new Product(1, "HP Laptop", 25000f));
		productsList.add(new Product(2, "Dell Laptop", 30000f));
		productsList.add(new Product(3, "Lenevo Laptop", 28000f));
		productsList.add(new Product(4, "Sony Laptop", 28000f));
		productsList.add(new Product(5, "Apple Laptop", 90000f));

		// filterWithStream(productsList);
//		convertListBeforeJava8(productsList);
//		convertListToMap();
		convertListAfterJava8(productsList);

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

	public static void filterWithStream(List<Product> productsList) {
		// List<Float> productList2 = productsList.stream().filter(p -> p.price >
		// 30000).map(p -> p.price)
		// .collect(Collectors.toList());
		List<Float> productList2 = productsList.stream().filter(p -> p.price > 30000)// filtering data
				.map(p -> p.price) // fetching price
				.collect(Collectors.toList()); // collecting as list
		List<String> productName = productsList.stream().filter(p -> p.name.contains("Sony")).map(p -> p.name)
				.collect(Collectors.toList());
		System.out.println(productList2);
		System.out.println(productName);
	}

	public static void convertListBeforeJava8(List<Product> list) {
		Map<Integer, Product> map = new HashMap<>();
		for (Product product : list) {
			map.put(product.getId(), product);
		}
		System.out.println(map);
	}

	public static void convertListAfterJava8(List<Product> list) {
		Map<Integer, Product> map = list.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
		System.out.println(map);
		Map<Integer, String> map2 = list.stream().collect(Collectors.toMap(Product::getId, Product::getName));
		System.out.println(map2);
	}
}
