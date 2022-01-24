package inteviewQuestion.GlobalLogic;

class ParentB {
	public void test(int m) {
		System.out.println("Parent test() ");
		System.out.println("Parent test() ");
	}

	
}

class ChildB extends ParentB {

	public void test(int... m) {
		System.out.println("Child test() ");
		System.out.println("Child test() ");
	}
	public void test(double... m) {
		System.out.println("Child test() ");
		System.out.println("Child test() ");
	}
}

public class GlobalLogicExample2 {
	public static void main(String args[]) {

		ChildB ob1 = new ChildB();
		ob1.test(10,20);
//		ParentB ob2 = new ChildB();
//		ob2.test(20,30);
		
//		ParentB ob4 = new ChildB();
//		ob4.test(20,30);
		
//		ParentB ob3 = new ParentB();
//		ob3.test(30);

	}
}
