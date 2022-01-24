package Java8.Example;

interface Test1 {
	void add(int a, int b);
}

public class LambdaExpression2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Test1 t = (int a,int b)->{
			System.out.println(a+b);
		};
		t.add(10, 20);
	}

}
