package InterviewQuestions.Paytm.systemdesign.lift_call_new;

import InterviewQuestions.Paytm.systemdesign.lift_call.Request;

public class AddJobWorker implements Runnable {

	private ElevatorNew elevator;
	private Request request;

	AddJobWorker(ElevatorNew elevator, Request request) {
		this.elevator = elevator;
		this.request = request;
	}

	@Override
	public void run() {

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		elevator.addJob(request);
	}

}