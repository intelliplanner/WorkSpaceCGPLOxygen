package InterviewQuestions.Paytm.systemdesign.lift_call_new;

import InterviewQuestions.Paytm.systemdesign.lift_call.Direction;
import InterviewQuestions.Paytm.systemdesign.lift_call.ExternalRequest;
import InterviewQuestions.Paytm.systemdesign.lift_call.InternalRequest;
import InterviewQuestions.Paytm.systemdesign.lift_call.Request;

public class TestElevator {
	public static void main(String args[]) {

		ElevatorNew elevator = new ElevatorNew();

		/**
		 * Thread for starting the elevator
		 */
		ProcessJobWorker processJobWorker = new ProcessJobWorker(elevator);
		Thread t2 = new Thread(processJobWorker);
		t2.start();

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ExternalRequest er = new ExternalRequest(Direction.UP, 0);

		InternalRequest ir = new InternalRequest(5);

		Request request1 = new Request(ir, er);

		/**
		 * Pass job to the elevator
		 */
		new Thread(new AddJobWorker(elevator, request1)).start();

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
