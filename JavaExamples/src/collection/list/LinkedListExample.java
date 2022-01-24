package collection.list;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/*
 * LinkedList uses doubly linked list to store data elements 
 * It provides linkedList Data Structure
 LinkedList class inherit AbstractSequencialList class and List implements List and Deque interface	    
 */
public class LinkedListExample {
	public static void main(String a[]) {
		LinkedList<String> lst1 = new	LinkedList<String>();
		lst1.add("a");
		lst1.add("w");
		lst1.add("r");
		lst1.add("y");
		lst1.add("e");
		lst1.add("t");
		System.out.println("List1: "+lst1);
		
		LinkedList<String> lst2 = new	LinkedList<String>();

		lst2.add("A");
		lst2.add("C");
		System.out.println("List2: " + lst2);
		
		LinkedList<String> lst3 = new	LinkedList<String>();
		lst3.add("G");
		lst3.add("S");
		lst3.add("T");
		
		
		System.out.println("List3: " + lst3);
		
		System.out.println("After Update position");
		
		lst1.add(1, "20");
		System.out.println("List1: " + lst1);
		
		System.out.println("After Merge List");
		lst1.addAll(lst2);
		
		System.out.println("List1: "+lst1);
		
		lst1.addAll(1,lst3);
		
		System.out.println("List1: "+lst1);
		
		
		
		System.out.println("First: "+lst1.getFirst());
		
		System.out.println("Last: "+lst1.getLast());
		lst1.removeFirstOccurrence("a");
		System.out.println("Remove: "+lst1);
		
		
		for(String s :lst1){
			System.out.print(s);
		}
		System.out.print("\n"+ lst1.size() +"\n");
		
		Iterator<String> s = lst1.iterator();
		while(s.hasNext())
			System.out.print(s.next());
	}
}
