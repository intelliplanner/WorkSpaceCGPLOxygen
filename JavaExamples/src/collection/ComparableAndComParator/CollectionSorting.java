/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collection.ComparableAndComParator;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author IPSSI
 */
public class CollectionSorting {
    static void sortArrayList1(){
        ArrayList<String> al = new ArrayList<String>(); 
        al.add("Geeks For Geeks"); 
        al.add("Friends"); 
        al.add("Dear"); 
        al.add("Is"); 
        al.add("Superb"); 
  
        /* Collections.sort method is sorting the 
        elements of ArrayList in ascending order. */
        System.out.println("List Before the use of" + 
                           " Collection.sort() :\n" + al); 
        Collections.sort(al); 
              // Create a list of strings 
  
        // Let us print the sorted list 
        System.out.println("List after the use of" + 
                           " Collection.sort() :\n" + al); 
    }
        static void sortArrayList2(){
        ArrayList<String> al = new ArrayList<String>(); 
        al.add("Geeks For Geeks"); 
        al.add("Friends"); 
        al.add("Dear"); 
        al.add("Is"); 
        al.add("Superb"); 
  
        /* Collections.sort method is sorting the 
        elements of ArrayList in ascending order. */
        System.out.println("List Before the use of" + 
                           " Collection.reverseOrder() :\n" + al); 
        Collections.sort(al, Collections.reverseOrder()); 
              // Create a list of strings 
       
  
        // Let us print the sorted list 
        System.out.println("List after the use of" + 
                           " Collection.sort() :\n" + al); 
    }
    
    
    
    public static void main(String[] args) 
    { 
       sortArrayList1();
       sortArrayList2();
    }   
}
