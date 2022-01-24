package JavaAnnotations;

import java.util.*;

class TestAnnotation2 {
	
	 public static void main(String args[]){
		ArrayList<String> list = new ArrayList<String>();
		list.add("sonoo");
		list.add("vimal");
		list.add("ratan");
		System.out.println(list);
		for (Object obj : list) {
			System.out.println(obj);
		}

	}
}