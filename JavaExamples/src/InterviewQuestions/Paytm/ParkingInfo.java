package InterviewQuestions.Paytm;

public class ParkingInfo {
	int parkingSeq;
	String alotForVehicleType;
//	int entryPoint;
	int entryPointNo;
	int exitPoint;
	boolean isBusy;
	
	public ParkingInfo(int parkingSeq, String alotForVehicleType, int entryPointNo, int exitPoint, boolean isBusy) {
		super();
		this.parkingSeq = parkingSeq;
		this.alotForVehicleType = alotForVehicleType;
		this.entryPointNo = entryPointNo;
		this.exitPoint = exitPoint;
		this.isBusy = isBusy;
	}
	public int getParkingSeq() {
		return parkingSeq;
	}
	public void setParkingSeq(int parkingSeq) {
		this.parkingSeq = parkingSeq;
	}
	public String getAlotForVehicleType() {
		return alotForVehicleType;
	}
	public void setAlotForVehicleType(String alotForVehicleType) {
		this.alotForVehicleType = alotForVehicleType;
	}
	public int getEntryPointNo() {
		return entryPointNo;
	}
	public void setEntryPointNo(int entryPointNo) {
		this.entryPointNo = entryPointNo;
	}
	public int getExitPoint() {
		return exitPoint;
	}
	public void setExitPoint(int exitPoint) {
		this.exitPoint = exitPoint;
	}
	public boolean isBusy() {
		return isBusy;
	}
	public void setBusy(boolean isBusy) {
		this.isBusy = isBusy;
	}
	
}
