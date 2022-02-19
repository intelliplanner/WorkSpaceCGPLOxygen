package a_example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Employee {
	int id;
	String name;
	int age;
	int salary;

	public Employee(int id, String name, int age, int salary) {
		super();
		this.id = id;
		this.name = name;
		this.age = age;
		this.salary = salary;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

}

public class Virtusa {
	public static void main(String[] args) {
		List<Employee> empList = new ArrayList<Employee>();
		empList.add(new Employee(1,"vicky",20,200000));
		empList.add(new Employee(2,"vicky1",20,10000));
		empList.add(new Employee(3,"vicky2",20,50000));
		empList.add(new Employee(4,"vicky3",20,60000));
		empList.add(new Employee(5,"vicky4",20,5000));
		Collections.sort(empList,
				(s1, s2) -> s1.getSalary() < s2.getSalary() ? 1 : s1.getSalary() == s2.getSalary() ? 0 : -1);
		for (Employee e : empList) {
			System.out.println(e.getId() + " ," + e.getAge() + " ," + e.getName() + " ," + e.getSalary());
		}
	}

}
