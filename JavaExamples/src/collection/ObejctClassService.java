package collection;

import java.util.Iterator;
import java.util.TreeSet;

public class ObejctClassService {
	public static void main(String[] args) {
		TreeSet<String> al = new TreeSet<String>();
		al.add("Ravi");
		al.add("Vijay");
		al.add("Ravi");
		al.add("Ajay");
		// Traversing elements
		Iterator<String> itr = al.iterator();
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}
		// creating the Objects of Geek class.
		ObjectClass g1 = new ObjectClass("aa", 1);
		ObjectClass g2 = new ObjectClass("aa", 2);
		// comparing above created Objects.
		if (g1.hashCode() == g2.hashCode()) {

			if (g1.equals(g2))
				System.out.println("Both Objects are equal. ");
			else
				System.out.println("Both Objects are not equal. ");

		} else
			System.out.println("Both Objects are not equal. ");
	}

}
