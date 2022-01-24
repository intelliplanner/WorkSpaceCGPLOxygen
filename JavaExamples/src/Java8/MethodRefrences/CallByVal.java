package Java8.MethodRefrences;
public class CallByVal {

	int u = 20;

	private void CallByValMtd(int i) {
		u= u+i;
	}
	
    public static void main(String[] args) { 
		CallByVal obj = new CallByVal();
		System.out.println(obj.u);
		obj.CallByValMtd(10);
		System.out.println(obj.u);
	}


}
