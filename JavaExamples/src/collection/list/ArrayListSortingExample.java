package collection.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Product{  
    int id;  
    String name;  
    float price;  
    public Product(int id, String name, float price) {  
        super();  
        this.id = id;  
        this.name = name;  
        this.price = price;  
    }  
}  
public class ArrayListSortingExample{  
    public static void main(String[] args) {  
        List<Product> list=new ArrayList<Product>();  
          
        //Adding Products  
        list.add(new Product(1,"HP Laptop",25000f));  
        list.add(new Product(2,"Dell Mouse",150f));  
        list.add(new Product(3,"Keyboard",300f));    
       
  
        System.out.println("--------------------Before Sorting-------------------");
        for(Product p:list){  
            System.out.println(p.id+" "+p.name+" "+p.price);  
        }  
  
        System.out.println("--------------------Sorting on the basis of name...--------------------");  
//         implementing lambda expression  
        Collections.sort(list,(p1,p2)->{  
        return p1.name.compareTo(p2.name);  
        }); 
        
        for(Product p:list){  
            System.out.println(p.id+" "+p.name+" "+p.price);  
        }  
  
        
        System.out.println("--------------------Sorting on the basis of price...-------------------- ");  
        Collections.sort(list,(p1,p2)->{  
            return p1.price > p2.price ? 1 : p1.price == p2.price ? 0 : -1;  
            }); 
        
        for(Product p:list){  
            System.out.println(p.id+" "+p.name+" "+p.price);  
        }  
  
    }  
}  