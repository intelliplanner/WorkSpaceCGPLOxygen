package ThreadInterview;

public class ThreadCountinuous implements Runnable {

	// int noOfThreads = 3;
	int no = 1;
	int PRINT_NUMBERS_UPTO = 3;
	static Object obj = new Object();

	@Override
	public void run() {
		synchronized (obj) {
			while (no <= PRINT_NUMBERS_UPTO) {
				System.out.print(no + " ");
				no++;
			}
			System.out.println("Thread Finish");
		}
	}

	public static void main(String args[]) {
//		PrintSequence r1 = new PrintSequence(1);
//		PrintSequence r2 = new PrintSequence(2);
//		PrintSequence r3 = new PrintSequence(3);
//		PrintSequence r4 = new PrintSequence(4);
//		PrintSequence r5 = new PrintSequence(0);
//
//		Thread t1 = new Thread(r1, "Thread-1");
//		Thread t2 = new Thread(r2, "Thread-2");
//		Thread t3 = new Thread(r3, "Thread-3");
//		Thread t4 = new Thread(r4, "Thread-4");
//		Thread t5 = new Thread(r5, "Thread-5");
//		t1.start();
//		t2.start();
//		t3.start();
//		t4.start();
//		t5.start();

		 ThreadCountinuous r6 = new ThreadCountinuous();
		 ThreadCountinuous r7 = new ThreadCountinuous();
		 ThreadCountinuous r8 = new ThreadCountinuous();
		
		 Thread t1 = new Thread(r6, "Thread-1");
		 Thread t2 = new Thread(r7, "Thread-2");
		 Thread t3 = new Thread(r8, "Thread-3");
		 t1.start();
		 t2.start();
		 t3.start();

	}
}