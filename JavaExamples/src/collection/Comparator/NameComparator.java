package collection.Comparator;

import java.util.Comparator;

public class NameComparator implements Comparator<Student> {

	public int compare(Student o1, Student o2) {
		return o1.name.compareTo(o2.name);
	}
}
