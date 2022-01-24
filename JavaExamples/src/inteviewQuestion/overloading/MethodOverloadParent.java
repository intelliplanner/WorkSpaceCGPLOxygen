package inteviewQuestion.overloading;

public class MethodOverloadParent {
	public void test() {
		System.out.println("Hello MethodOverloadParent test()");
	}	
	public static void main(String[] args) {
		MethodOverloadParent ob = new MethodOverloadParent();
		String[] str =new String[3];
		str[0]= "1";
		ob.test();
	}	
}
