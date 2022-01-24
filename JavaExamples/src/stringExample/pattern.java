/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stringExample;

/**
 *
 * @author Vicky
 */
public class pattern {
    
    private static void pritnTriangle(int size){
        for (int i=0;i<size;i++) {
        for (int j=0;j<i;j++) {
                System.out.print("*");
            }
            System.out.println("");
        }
    }
    private static void pritnTriangleUp(int size){
        for (int i=1;i<size;i++) {
        for (int j=size;j>i;j--) {
                System.out.print("*");
            }
            System.out.println("");
        }
    }
     private static void pritnTriangledown(int size){
        for (int i=0;i<size;i++) {
        for (int j=0;j<size-i;j++) {
                System.out.print("*");
            }
            System.out.println("");
        }
    }
    public static void main(String arg[]){
//        pritnTriangle(10);
//        pritnTriangledown(10);
        pritnTriangleUp(7);
    }    
}
