package collection.Comparable;

import lombok.Data;


public class Employee implements Comparable<Employee> {
	int age;
	int salary;
	String name;
	public Employee(int age,int salary,String name){
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

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getSalary() {
		return salary;
	}

	public void setSalary(int salary) {
		this.salary = salary;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
