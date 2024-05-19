/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Generic;

/**
 *
 * @author IPSSI
 */
public class GenericMethodTest {
    public static <E> void printArray(E[] inputArray){
    	int i = 1;
        for(E e:inputArray){
            System.out.printf("%S", e);
            if( i < inputArray.length)
            	System.out.printf(",");
            i++;
        }
    }
    public static void main(String[] args) {
      Integer[] intArray = { 1, 2, 3, 4, 5 };
      Double[] doubleArray = { 1.1, 2.2, 3.3, 4.4 };
      Character[] charArray = { 'H', 'E', 'L', 'L', 'o' };

      System.out.println("Array integerArray contains:");
      printArray(intArray);   // pass an Integer array

      System.out.println("\nArray doubleArray contains:");
      printArray(doubleArray);   // pass a Double array

      System.out.println("\nArray characterArray contains:");
      printArray(charArray); 
        
    }
}
