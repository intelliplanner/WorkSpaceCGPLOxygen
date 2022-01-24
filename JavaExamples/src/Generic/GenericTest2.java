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
public class GenericTest2 {
    public static <E> E[] add(E[] inputArray){
        for(E e:inputArray){
            System.out.printf("%s",e);
        }
        System.out.println();
        return inputArray;
    }
    public static void main(String[] args) {
         Integer[] intArray = { 1, 2, 3, 4, 5 };
      Double[] doubleArray = { 1.1, 2.2, 3.3, 4.4 };
      Character[] charArray = { 'H', 'E', 'L', 'L', 'O' };

      System.out.println("Array integerArray contains:");
//   E[] a=   printArray(intArray);   // pass an Integer array

      System.out.println("\nArray doubleArray contains:");
//      printArray(doubleArray);   // pass a Double array

      System.out.println("\nArray characterArray contains:");
//      printArray(charArray); 
        
    }
}
