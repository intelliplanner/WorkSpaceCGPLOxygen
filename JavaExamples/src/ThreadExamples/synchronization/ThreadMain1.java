package ThreadExamples.synchronization;

public class ThreadMain1 {

	public static void main(String[] args) {
		Table t = new Table();
		MyThread1 m1 = new MyThread1(t);
		MyThread2 m2 = new MyThread2(t);
		m1.start();
		m2.start();
	}

}
