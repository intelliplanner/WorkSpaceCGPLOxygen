package ThreadExamples.synchronization;

public class Table {
	synchronized public void printable(int i) {
		for (int j = 1; j < i; j++) {
			System.out.println(j);
			try {
				Thread.sleep(400);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
}
