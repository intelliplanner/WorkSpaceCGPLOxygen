package collection.ConsumerProducerProb;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProducerConsumerService {

	public static void main(String[] args) {
		BlockingQueue<Message> queue = new ArrayBlockingQueue<>(5);
		
		Producer p = new Producer(queue);
		Consumer c = new Consumer(queue);
		new Thread(p).start();
		new Thread(c).start();
		System.out.println("Producer and Consumer has been started");
	}

}
