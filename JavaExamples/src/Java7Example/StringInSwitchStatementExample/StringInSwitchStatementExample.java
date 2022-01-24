/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Java7Example.StringInSwitchStatementExample;

/**
 *
 * @author IPSSI
 */
public class StringInSwitchStatementExample {  
    public static void firstTest(){
    String game = null;  //Exception in thread "main" java.lang.NullPointerException
        switch(game){  
        case "Hockey":  
            System.out.println("Let's play Hockey");  
            break;  
        case "Cricket":  
            System.out.println("Let's play Cricket");  
            break;
        case "Football":  
            System.out.println("Let's play Football");  
        }  
    }
    public static void secondTest(){
    String game = "Hockey";  
        switch(game){  
        case "Hockey" : case "Cricket": case "Football":   
            System.out.println("Let's play Outdoor Games");  
            break;
        case "Carrom":  case "Ludo":  
            System.out.println("Let's play Indoor Game");  
            break;  
        default:
            System.out.println("What game it is?");  
        }  
    }
    public static void main(String[] args) {  
        secondTest();
    	//firstTest();
    }  
}  
