package InterviewQuestions.Paytm.systemdesign.lift_call;

public class TestElevator2 {
	public static void main(String args[]) {

		Elevator elevator = new Elevator();

		/**
		 * Thread for starting the elevator
		 */
		ProcessJobWorker processJobWorker = new ProcessJobWorker(elevator);
		Thread t2 = new Thread(processJobWorker);
		t2.start();

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ExternalRequest er = new ExternalRequest(Direction.DOWN, 5);

		InternalRequest ir = new InternalRequest(0);

		Request request1 = new Request(ir, er);

	}
}
