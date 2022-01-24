package collection.Comparable;

public class Employee implements Comparable<Employee> {
	int age;
	int salary;
	String name;
	Employee(int age,int salary,String name){
		this.age=age;
		this.salary=salary;
		this.name=name;
	}
		
	@Override
	public int compareTo(Employee emp) {
		if(age==emp.age) 
			return 0;
		else if(age > emp.age) 
			return 1;
		else 
			return -1;
	}

}
