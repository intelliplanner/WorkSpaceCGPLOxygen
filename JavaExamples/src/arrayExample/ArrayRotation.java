/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arrayExample;

import java.util.ArrayList;

/**
 *
 * @author IPSSI
 */
public class ArrayRotation {
    public static void main(String []args){
          int val=1;
          int [][] arr = new int[4][4];
          
          for(int i = 0; i<4; i++ ){
              for(int j=0;j<4;j++){
                  arr [i][j] = val;
                   val++;
              }
             
          }
System.out.println("====================================================== \n\n");

          for(int i = 0; i<4; i++ ){
              for(int j=0;j<4;j++){
                    System.out.print(" " +arr[i][j]);
              }
                   System.out.println(" ");
          }   
        System.out.println("====================================================== \n\n");
          rotateArray("LEFT",arr);
          rotateArray("RIGHT",arr);
          
    }

    private static void rotateArray(String rotationDirection, int[][] arr) {
        if(rotationDirection.equalsIgnoreCase("LEFT")){

          for(int i = 3; i>=0; i-- ){
              for(int j=0;j<4;j++){
                    System.out.print(arr [j][i] +" ");
              }
                   System.out.println(" ");
          }   
          
        } else if(rotationDirection.equalsIgnoreCase("RIGHT")){
System.out.println("====================================================== \n\n");
          for(int i = 0; i<4; i++ ){
              for(int j=0;j<4;j++){
                    System.out.print(arr [j][i] +" ");
              }
                   System.out.println(" ");
          }   
          
        }
    }
}
