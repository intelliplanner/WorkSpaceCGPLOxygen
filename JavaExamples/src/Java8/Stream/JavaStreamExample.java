package Java8.Stream;

import java.util.ArrayList;
import java.util.List;
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

		filterWithStream(productsList);
	}

	public static void filterWithStream(List<Product> productsList) {
//		List<Float> productList2 = productsList.stream().filter(p -> p.price > 30000).map(p -> p.price)
//				.collect(Collectors.toList());
		List<Float> productList2 =	productsList.stream()
									.filter(p -> p.price > 30000)// filtering data  
									.map(p->p.price)        // fetching price  
									.collect(Collectors.toList()); // collecting as list  
		List<String> productName = productsList.stream().filter(p->p.name.contains("Sony")).map(p->p.name).collect(Collectors.toList());
		System.out.println(productList2);
		System.out.println(productName);
	}

	public static void filterWithStream() {

	}

}
