package ThreadExamples.example2;

public class ThreadSynchronization implements Runnable {
	 int totalcount = 10;
	 int noOfThreads = 3;
	static int number = 1;
	static Object lock = new Object();
	int rem = 0;

	ThreadSynchronization(int rem) {
		this.rem = rem;
	}

	@Override
	public void run() {
		synchronized (lock) {
			while (number < totalcount-1) {
				while (number % noOfThreads != rem) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				
				System.out.println(Thread.currentThread().getName() + "  " + number);
				number++;
				lock.notifyAll();
			}
		}
	}

	public static void main(String[] args) {
		ThreadSynchronization r1 = new ThreadSynchronization(1);
		ThreadSynchronization r2 = new ThreadSynchronization(2);
		ThreadSynchronization r3 = new ThreadSynchronization(0);
		Thread t1 = new Thread(r1, "Thread-1");
		Thread t2 = new Thread(r2, "Thread-2");
		Thread t3 = new Thread(r3, "Thread-3");
		t1.start();
		t2.start();
		t3.start();
	}

}
