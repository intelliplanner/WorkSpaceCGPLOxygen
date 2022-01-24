package abstractAndInterface.test_hierarchy;

interface Test1 {
	void a();

	void b();

	void c();

	void d();
}

abstract class A implements Test1 {
	
	abstract void test();
	
	@Override
	public void a() {
		System.out.println("class A call a()");
	}
}

class Test2 extends A {

	@Override
	public void b() {
		System.out.println("class Test2 call b()");
	}

	@Override
	public void c() {
		System.out.println("class Test2 call c()");
	}

	@Override
	public void d() {
		System.out.println("class Test2 call d()");
	}

	@Override
	void test() {
		// TODO Auto-generated method stub
		
	}



}



public class Test5 {
	public static void main(String args[]) {
		Test2 t = new Test2();
		t.a();
		t.b();
		t.c();
		t.d();
	}
}
