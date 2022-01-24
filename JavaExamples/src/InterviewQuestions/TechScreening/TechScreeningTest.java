package InterviewQuestions.TechScreening;

import java.util.*;  
import java.util.stream.Collectors;  
class Product{  
    int id;  
    String name;  
    Integer price;  
    public Product(int id, String name, Integer price) {  
        this.id = id;  
        this.name = name;  
        this.price = price;  
    }  
}  
public class TechScreeningTest {  
    public static void main(String[] args) {  
        List<Product> productsList = new ArrayList<Product>();  
        //Adding Products  
        productsList.add(new Product(1,"HP Laptop",25000));  
        productsList.add(new Product(2,"Dell Laptop",30000));  
        productsList.add(new Product(3,"Lenevo Laptop",28000));  
        productsList.add(new Product(4,"Sony Laptop",28000));  
        productsList.add(new Product(5,"Apple Laptop",90000));  
        List<Integer> productPriceList2 =productsList.stream()  
                                     .filter(p -> p.price > 30000)// filtering data  
                                     .map(p->p.price)
                                     .collect(Collectors.toList()); // collecting as list  
      
        System.out.println(productPriceList2);
    }  
}  