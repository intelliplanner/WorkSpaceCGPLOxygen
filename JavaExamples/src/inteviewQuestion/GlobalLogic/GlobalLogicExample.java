package inteviewQuestion.GlobalLogic;

//Vararg methods Override/Overload confusion

class Parent {
	int i = 10;
	public void test(int m) {
		System.out.println("Parent test() m: " +m);
		System.out.println("Parent test() i: " +i);
	}
}

public class GlobalLogicExample extends Parent {
	int i = 20;
//	public void test(int m) {
//		System.out.println("child test() m: " +m);
//		System.out.println("child test() i: " +i);
//	}
//	
	public void test(int... m) {
		System.out.println("child varags test() m: " +m);
		System.out.println("child varags test() i: " +i);
	}

	public static void main(String[] args) {
		GlobalLogicExample o1 = new GlobalLogicExample();
		o1.test(30,20);
	
		Parent o2 = new GlobalLogicExample();
		o2.test(40);
		
		System.out.println(o2.i);
	}
}
