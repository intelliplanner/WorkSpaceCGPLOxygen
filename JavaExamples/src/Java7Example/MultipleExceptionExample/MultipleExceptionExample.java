/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Java7Example.MultipleExceptionExample;

/**
 *
 * @author IPSSI
 */
public class MultipleExceptionExample {
    public static void testException(){
        try{
            int arr[]= new int[10];
            arr[11]=20/0;
        }    catch( ArithmeticException | ArrayIndexOutOfBoundsException e){   
            System.out.println(e.getMessage());  
        }   
    }
    
    public static void main(String s[]){
    testException();
    }
}
