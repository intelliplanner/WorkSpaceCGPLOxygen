package ThreadExamples.synchronization;

public class ThreadMain2 {

	public static void main(String[] args) {
		final Table obj=new Table();
		Thread t1= new Thread() {
			public void run() {
				obj.printable(10);
			}
		};
		Thread t2= new Thread() {
			public void run() {
				obj.printable(10);
			}
		};
		
		t1.start();
		t2.start();
	}

}
