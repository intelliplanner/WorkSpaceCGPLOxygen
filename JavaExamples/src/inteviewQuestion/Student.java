package inteviewQuestion;

public class Student implements Comparable<String>{

	String name;
	public Student(String name) {
		this.name=name;
	}
	@Override
	public int compareTo(String n) {
		if(name.equals(n))
			return 0;
		else
			return -1;
	}
	

	
}
