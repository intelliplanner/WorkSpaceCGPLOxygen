package arrayExample;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class ArrayExample2 {
	public static void main(String[] args) {
		  ArrayList<String> al=new ArrayList<String>();//creating arraylist    
		  al.add("A");//adding object in arraylist    
		  al.add("B");    
		  al.add("C");
		  al.add("D");    
		  al.add("E");
		  al.add("F");
		  al.add("G");
		  al.add("H");
		  al.add("I");
		  al.add("J");
		  al.add("K");
		  al.add("L");
		  al.add("M");
		  al.add("N");
		  al.add("O");
		  al.add("P");
		  al.add("Q");
		  al.add("R");
		  al.add("S");
		  al.add("T");
		  al.add("U");
		  al.add("V");
		  al.add("W");
		  al.add("X");
		  al.add("Y");
		  al.add("Z");
		  int count=1;
		
		  for(int i=0;i<al.size();i++) {
				  System.out.println("INSERT INTO mst_section_master (mst_section_id,mst_section_name,created_date_time,created_by,is_active) VALUES ("+ count++ +", '" +al.get(i) +"', now(), 1, 1"+");");
		  }
		  
		  for(int i=0;i<3;i++) {
			  for(int j=0;j<al.size();j++) {
				  
//				  System.out.println(count++ + ": "+ al.get(i) +   al.get(j));
				  System.out.println("INSERT INTO mst_section_master (mst_section_id,mst_section_name,created_date_time,created_by,is_active) VALUES ("+ count++ +", '" +al.get(i) + al.get(j)+"', now(), 1, 1"+");");
			}
		  }
	}
	
	
}
