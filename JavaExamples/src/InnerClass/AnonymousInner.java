package InnerClass;

interface Test{
	void display();
}
public class AnonymousInner {
	
	public static void main(String... args) {
		Test t = new Test() {
			
			@Override
			public void display() {
				System.out.println("Annonymous Call");
			}
		};
		t.display();
	}
}
