package InterviewQuestions.Paytm.systemdesign.lift_call;

public class InternalRequest {
	private int destinationFloor;

	public InternalRequest(int destinationFloor) {
		this.destinationFloor = destinationFloor;
	}

	public int getDestinationFloor() {
		return destinationFloor;
	}

	public void setDestinationFloor(int destinationFloor) {
		this.destinationFloor = destinationFloor;
	}
}