package immutable;

public class ImmutbleClass2{
	private final MutableClass2 mc2;

	public Object clone()throws CloneNotSupportedException{  
		return super.clone();  
		}  
		  
	public ImmutbleClass2(MutableClass2 mc2) {
		this.mc2 = mc2;
	}
	
	public MutableClass2 getMc2() throws CloneNotSupportedException {
		return (MutableClass2) mc2.clone();
	}

	@Override
	public String toString() {
		return "ImmutbleClass2 [m2 name=" + mc2.getName() + "]";
	}
	
}
