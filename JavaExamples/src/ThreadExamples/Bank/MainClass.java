/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ThreadExamples.Bank;

/**
 *
 * @author IPSSI
 */
public class MainClass {

    public static void main(String s[]) {
        final UIBank c = new UIBank();
        new Thread() {
            @Override
            public void run() {
                c.withDrawal(15000);
            }
        }.start();
        new Thread() {
            public void run() {
                c.deposit(10000);
            }
        }.start();
    }
}
