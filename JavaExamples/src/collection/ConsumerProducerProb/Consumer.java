package collection.ConsumerProducerProb;

import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable{
	BlockingQueue<Message> queue;
	Consumer(BlockingQueue<Message> queue){
		this.queue=queue;
	}
	@Override
	public void run() {
		try {
			Message msg;
			while(((msg = queue.take()).getMsg() != "exit")) {
				System.out.println("Consumed Msg-"+ msg.getMsg());
				Thread.sleep(3000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
