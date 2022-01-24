package ThreadExamples.synchronization;

public class MyThread2 extends Thread{
	Table t;
	public MyThread2(Table t) {
		this.t=t;
	}
	public void run(){  
		t.printable(10);  
	}  
}
