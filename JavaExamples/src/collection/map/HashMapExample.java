package collection.map;

import java.util.*;
import java.util.Map.Entry;

public class HashMapExample {

	/*
	 * HashMap implements map interface and contains key value pair,
	 * HashMap maintain ascending ordered list,
	 * Not contain duplicate key, 
	 * and can contain single null key or multiple null values
	 */

	public static void main(String[] args) {
		HashMap<Integer, Book> hm = new HashMap<Integer, Book>();
		hm.put(2, new Book(102, "book2"));
		hm.put(3, new Book(103, "book3"));
		hm.put(4, new Book(104, "book4"));
		hm.put(5, new Book(105, "book5"));
		hm.put(1, new Book(101, "book1"));
		hm.put(null, new Book(106, "book6"));

		
		
		for (Map.Entry<Integer, Book> m : hm.entrySet()) {
			Book b = m.getValue();
			System.out.println(b.bookId + ", " + b.bookAuth);
		}
		System.out.println("\n\n\n");
		for (Book s : hm.values()) {
			System.out.println(s.bookId + ", " + s.bookAuth);
		}
		for (Integer i : hm.keySet()) {
			System.out.println(i);
		}
		
		
	Iterator<Entry<Integer, Book>> i = hm.entrySet().iterator();
		while(i.hasNext()) {
			Entry<Integer, Book> e=i.next();
			System.out.println(e.getKey() + ", " + e.getValue());
		}
		System.out.println("-========--\n\n");
		
		HashMap<Integer, Book> hmNew = new HashMap<Integer, Book>();
		hmNew.put(7, new Book(107, "book7"));
		hmNew.put(8, new Book(108, "book8"));
		
		hmNew.putAll(hm);
		
		for(Entry<Integer,Book> m:hmNew.entrySet()) {
			Book k = m.getValue();
			System.out.println("New: "+k.bookId+ ", "+k.bookAuth);
		}
		
		hmNew.remove(null);
		System.out.println("-====removed null====--\n\n");
		for(Entry<Integer,Book> m:hmNew.entrySet()) {
			Book k = m.getValue();
			System.out.println("New: "+k.bookId+ ", "+k.bookAuth);
		}
		
		
	}
	
	

}
