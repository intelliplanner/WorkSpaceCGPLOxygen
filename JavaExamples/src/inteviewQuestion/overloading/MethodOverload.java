package inteviewQuestion.overloading;

public class MethodOverload extends MethodOverloadParent {
	public void test() {
		System.out.println("Hello MethodOverload test()");
	}	
	public static void main(String[] args) {
		MethodOverloadParent ob = new MethodOverload();
		ob.test();
	}
}
