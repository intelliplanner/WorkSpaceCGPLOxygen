/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Java7Example.UnderscoreInNumericLiteral;

/**
 *
 * @author IPSSI
 */
public class UnderscoreInNumericLiteral {
    public static void main(String[] args) {  
        // Underscore in integral literal  
        int a = 10_00000;  
        System.out.println("a = "+a);  
        // Underscore in floating literal  
        float b = 10.5_000f;  
        System.out.println("b = "+b);  
        // 	Underscore in binary literal  
        int c = 0B10_10;  
        System.out.println("c = "+c);  
        // Underscore in hexadecimal literal  
        int d = 0x1_1;  
        System.out.println("d = "+d);  
        // Underscore in octal literal  
        int e = 01_1;
        System.out.println("e = "+e);  
    }  
}
