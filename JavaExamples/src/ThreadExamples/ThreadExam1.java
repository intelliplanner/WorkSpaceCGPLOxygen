/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ThreadExamples;

/**
 *
 * @author Vicky
 */
public class ThreadExam1{
    TrheadService th = null;
    void test(){
        if(th == null){
            th = new TrheadService();
            th.setHandler(new ClassInterface() {

                @Override
                public void print(int val) {
                    System.out.println("Print Method "+val);
                }

                @Override
                public void changeText(String str) {
                    System.out.println("changeText Method " +str );
                }
            });
            th.start();
        }
    }
    
    public static void main(String[] args) {
        ThreadExam1 t1 = new ThreadExam1();
        t1.test();
    }
}
