package ThreadExamples.example1;

public class Counter {
	public volatile int count = 0;

	public int getCount() {
		return count;	
	}

	public synchronized void increment() {
		count++;
	}
}
