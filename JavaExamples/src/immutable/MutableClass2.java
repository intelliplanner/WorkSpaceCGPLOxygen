package immutable;

public class MutableClass2 implements Cloneable {

	public Object clone()throws CloneNotSupportedException{  
		return super.clone();  
		}  
		  
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
