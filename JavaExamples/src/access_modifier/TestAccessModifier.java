package access_modifier;

class TestAccessModifier {
	 int i=10;
}
class Test extends TestAccessModifier{
	public static void main(String args[]) {
		TestAccessModifier t=new TestAccessModifier();
		System.out.println(t.i);
	}
}