/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package collection.set;

/**
 *
 * @author Vicky
 */
import java.util.*;

class Employee {
	int id;
	String name;

	public Employee(int id, String name) {
		this.id = id;
		this.name = name;
	}

}

class HashSetExample {

	public static void main(String args[]) {

		HashSet<String> al = new HashSet<String>();
		al.add("Ravi");
		al.add("Vijay");
		al.add("Ravi");
		al.add("Ajay");
		al.add("Ajay");

//		Iterator<String> itr = al.iterator();
//		while (itr.hasNext()) {
//			System.out.println(itr.next());
//		}

		// =============

		HashSet hashSet = new HashSet();
		hashSet.add(1);
		hashSet.add("test");
		for(Object obj:hashSet) {
			System.out.println(obj);
		}
	}
}
