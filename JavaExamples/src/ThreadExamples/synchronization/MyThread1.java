package ThreadExamples.synchronization;

public class MyThread1 extends Thread{
	Table t;
	public MyThread1(Table t) {
		this.t=t;
	}
	public void run(){  
		t.printable(10);  
	}  
}
