package ThreadInterview;

class PrintSequence implements Runnable {
	public int PRINT_NUMBERS_UPTO = 10;
	static int number = 1;
	int remainder;
	static Object lock = new Object();

	public PrintSequence(int remainder) {
		this.remainder = remainder;
	}

	@Override
	public void run() {
		while (number < PRINT_NUMBERS_UPTO) {
			synchronized (lock) {
				while (number % 5 != remainder) { // wait for numbers other than remainder
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println(Thread.currentThread().getName() + " " + number);
				number++;
				lock.notifyAll();
			}
		}
	}
}

public class ThreadInterview1 {
	public static void main(String args[]) {
		 PrintSequence r1 = new PrintSequence(1);
		 PrintSequence r2 = new PrintSequence(2);
		 PrintSequence r3 = new PrintSequence(3);
		 PrintSequence r4 = new PrintSequence(4);
		 PrintSequence r5 = new PrintSequence(0);

		 Thread t1 = new Thread(r1, "Thread-1");
		 Thread t2 = new Thread(r2, "Thread-2");
		 Thread t3 = new Thread(r3, "Thread-3");
		 Thread t4 = new Thread(r4, "Thread-4");
		 Thread t5 = new Thread(r5, "Thread-5");
		 
		 t1.start();
		 t2.start();
		 t3.start();
		 t4.start();
		 t5.start();

//		ThreadCountinuous r6 = new ThreadCountinuous();
//		ThreadCountinuous r7 = new ThreadCountinuous();
//		ThreadCountinuous r8 = new ThreadCountinuous();
//
//		Thread t1 = new Thread(r6, "Thread-1");
//		Thread t2 = new Thread(r7, "Thread-2");
//		Thread t3 = new Thread(r8, "Thread-3");
//		t1.start();
//		t2.start();
//		t3.start();

	}

}
