package InterviewQuestions.hcl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Employee implements Comparable<Employee> {
	int age;
	String name;
	int departmentId;
	int salary;

	public Employee(int age, String name, int departmentId, int salary) {
		super();
		this.age = age;
		this.name = name;
		this.departmentId = departmentId;
		this.salary = salary;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public int getSalary() {
		return salary;
	}

	public void setSalary(int salary) {
		this.salary = salary;
	}

	@Override
	public int compareTo(Employee o) {
		if (this.salary > o.salary) {
			return 1;
		} else if (this.salary == o.salary) {
			return 0;
		} else {
			return -1;
		}

	}

}

public class HCL_Example {

	public static void main(String[] args) {
		List<Employee> empList = new ArrayList<Employee>();
		empList.add(new Employee(25, "vir", 1, 10000));
		empList.add(new Employee(22, "oiu", 4, 30000));
		empList.add(new Employee(23, "ghd", 5, 20000));
		empList.add(new Employee(21, "qwert", 6, 60000));

		empList.forEach(a->System.out.println(a.getSalary()));
		
//		Collections.sort(empList);
		
		System.out.println("-------------------------------------------------------");
		empList.forEach(a->System.out.println(a.getSalary()));
		System.out.println("-------------------------------------------------------");
		  Employee max = Collections.max(empList);
		  System.out.println("ArrayList max value : " + max.getSalary());
		
		// empList.stream().filter(s -> s.getSalary() >
		// 10000).map(a->a.getName()).collect(Collectors.toList()).forEach(System.out::println);

		// empList.stream().reduce((s1,s2) -> s1.getSalary() >
		// s2.getSalary()).map(a->a.getName()).collect(Collectors.toList()).forEach(System.out::println);

		// empList.stream().filter(m -> m.getDepartmentId() ==
		// 6).map(a->a.getName()).collect(Collectors.toList()).forEach(System.out::println);

		// empList.stream().filter(m -> m.getDepartmentId() ==
		// 6).map(a->a.getName()).collect(Collectors.toList()).forEach(System.out.println(a))

	}

}
