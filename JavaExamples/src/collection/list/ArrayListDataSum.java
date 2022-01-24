package collection.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ArrayListDataSum {
	public static void main(String[] args) {
		test1();
		test2();
	}

	private static void test2() {
		List<Product> productsList = new ArrayList<Product>();
		// Adding Products
		productsList.add(new Product(1, "HP Laptop", 25000f));
		productsList.add(new Product(2, "Dell Laptop", 30000f));
		productsList.add(new Product(3, "Lenevo Laptop", 28000f));
		productsList.add(new Product(4, "Sony Laptop", 28000f));
		productsList.add(new Product(5, "Apple Laptop", 90000f));
		// Using Collectors's method to sum the prices.
		double totalPrice3 = productsList.stream().collect(Collectors.summingDouble(product -> product.price));
		System.out.println(totalPrice3);

	}

	private static void test1() {
		List<Integer> productsList = Arrays.asList(10, 20, 30, 40);// new ArrayList<Integer>() ;
		Integer i = productsList.stream().collect(Collectors.summingInt(p -> p));
		System.out.println(i);
	}
}
