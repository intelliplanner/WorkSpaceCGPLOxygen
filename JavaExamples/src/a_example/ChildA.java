package a_example;

public class ChildA extends ParentA {

//	B() {
//		this();
//		super(); // error constructor always be a first;
//	}

	int i = 20;

	void print(String s) {
		System.out.println(s);
	}

	void print(Object o) {
		System.out.println(o);
	}

	void test() {
		System.out.println("B B1");
	}

	void m1() {
		System.out.println("B m1");
	}

	public void privateMethod() {
		System.out.println("B privateMethod");
	}

	public static void main(String[] args) {
		 ParentA a = new ChildA();
		 System.out.println(a.i);
		 
		// a.m1();
		 a.test();
		//
		// a.privateMethod();
		//
		// A ob = new A();
		// ob.privateMethod();
		//
//		 B ob1 = new B();
//		 ob1.privateMethod();

		// A b=null;
		// A.print();

		// a.m2();

		// B b = new B();
		// b.print(null);
	}

}
