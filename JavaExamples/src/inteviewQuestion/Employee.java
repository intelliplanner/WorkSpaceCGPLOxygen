package inteviewQuestion;

public class Employee {
	String name;
	
	@Override
	public int hashCode() {
		return 1;
	}
	
	public boolean equals(Object o) {
		if(o == null) 
			return false;
		else if(!(o instanceof Employee))
			return false;
		else if(o==this)
			return true;
		
		return (this.getName() == ((Employee)o).getName()) ? true : false; 
	}
	public Employee(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
