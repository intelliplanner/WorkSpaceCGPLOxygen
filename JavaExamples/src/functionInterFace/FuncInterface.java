/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package functionInterFace;

/**
 *
 * @author IPSSI
 */

@FunctionalInterface
interface Test {
    public void abs(int i);
}

public class FuncInterface {

    public static void main(String s[]) {
        Test t = (a) -> {
            System.out.println("test");
        };
    }

}
