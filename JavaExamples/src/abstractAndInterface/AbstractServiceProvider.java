package abstractAndInterface;

public abstract class AbstractServiceProvider implements ServiceProvider {
	abstract void test1();

	void test2() {
		System.out.println("call test2()");
	}

	@Override
	public void a() {
		System.out.println("call a()");
	}
}
