/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Java7Example.TryWithResources;

import java.io.File;
import java.io.FileOutputStream;

/**
 *
 * @author IPSSI
 */
public class TryWithResources {

    public static void testException() {
        File fs = new File("");

        try (FileOutputStream fileOutputStream = new FileOutputStream("abc.txt")) {
            String msg = "Welcome to javaTpoint!";
            byte byteArray[] = msg.getBytes(); //converting string into byte array      
            fileOutputStream.write(byteArray);
            System.out.println("Message written to file successfuly!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String s[]) {
        testException();
    }
}
