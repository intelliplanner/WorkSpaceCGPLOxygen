package DefaultMethods;

class Operationa {
	int data = 50;

	void change(int data) {
		data = data + 100;// changes will be in the local variable only
		
//		this.data = data + 100;
	}

	
	
	public static void main(String args[]) {
		Operationa op = new Operationa();

		System.out.println("before change " + op.data);
		op.change(500);
		System.out.println("after change " + op.data);

	}
}
