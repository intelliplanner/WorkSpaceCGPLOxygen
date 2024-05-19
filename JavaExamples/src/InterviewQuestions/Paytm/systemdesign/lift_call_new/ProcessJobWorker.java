package InterviewQuestions.Paytm.systemdesign.lift_call_new;

public class ProcessJobWorker implements Runnable {

	private ElevatorNew elevator;

	ProcessJobWorker(ElevatorNew elevator) {
		this.elevator = elevator;
	}

	@Override
	public void run() {
		/**
		 * start the elevator
		 */
		elevator.startElevator();
	}

}
