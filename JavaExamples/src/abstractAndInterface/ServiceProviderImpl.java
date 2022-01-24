package abstractAndInterface;

public class ServiceProviderImpl extends AbstractServiceProvider {

	@Override
	void test1() {
		System.out.println("call test1");
	}

	public static void main(String s[]) {
		System.out.println("call mains");
		ServiceProviderImpl obj = new ServiceProviderImpl();
		obj.test1();
		obj.test2();
	}

	@Override
	public void b() {
	}

	@Override
	public void c() {
	}

	@Override
	public void d() {
		// TODO Auto-generated method stub
		
	}


}
