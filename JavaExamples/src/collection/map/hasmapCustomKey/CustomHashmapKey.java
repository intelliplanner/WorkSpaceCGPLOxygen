package collection.map.hasmapCustomKey;

public class CustomHashmapKey {
	int rollno;
	String name;

	public CustomHashmapKey(int rollno, String name) {
		this.rollno = rollno;
		this.name = name;
	}

	public String toString() {
		return rollno +", " + name;
	}
	
	@Override
	public int hashCode() {
		return this.rollno;
	} 
	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;

		CustomHashmapKey other = (CustomHashmapKey) o; 
		
		if(this.name == other.name)
			return true;
		
		if(this.rollno == other.rollno)
			return true;
		
		return false;
	}
	
}
