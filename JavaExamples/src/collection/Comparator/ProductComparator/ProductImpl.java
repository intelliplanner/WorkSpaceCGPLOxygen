package collection.Comparator.ProductComparator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ProductImpl {
	public static void main(String[] args) {
		List<Product> prodList=new ArrayList<Product>();
		prodList.add(new Product(1, "Samsung", 20));
		prodList.add(new Product(2, "Apple", 10));
		prodList.add(new Product(3, "One Plus", 30));
		prodList.add(new Product(3, "One Plus", 30));
		
		prodList.forEach(prod -> System.out.println(prod.id+", "+prod.name+" ,"+prod.price));
		
		System.out.println("============================");
		
		java.util.Collections.sort(prodList,new ProductPriceComparator());
		System.out.println("==============After Price sorting ==============");
		
		prodList.forEach(prod -> System.out.println(prod.id+", "+prod.name+" ,"+prod.price));
		
		System.out.println("==============Name Sorting==============");
		java.util.Collections.sort(prodList,new ProductNameComparator());
		prodList.forEach(prod -> System.out.println(prod.id+", "+prod.name+" ,"+prod.price));
		
		
		System.out.println("==============Remove Dublicate==============");
		
//		---------- Remove Dublicate----------------
		Set<Product> pro1 = new HashSet<>(prodList);
		prodList.clear();
		prodList.addAll(pro1);
		
		prodList.forEach(prod -> System.out.println(prod.id+", "+prod.name+" ,"+prod.price));
	} 
	
}
