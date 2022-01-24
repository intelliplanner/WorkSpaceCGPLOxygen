package DesignPattern.Observer;

public class InformationObserver implements Observer{

	static int infoObserverId = 0;
	double tvPrice = 0;
	boolean isAvailable=false;
	
	private Flipkart grabInfo;
	
	public InformationObserver(Flipkart grabInfo) {
		this.grabInfo=grabInfo;
		this.infoObserverId = ++infoObserverId;
		System.out.println("New Observer Id:"+this.infoObserverId);
		this.grabInfo.addObserver(this);
	}

	public void printInfo() {
		System.out.println("Information Observer [tvPrice=" + this.tvPrice + ", isAvailable=" + this.isAvailable + "]");
	}

	@Override
	public void updateInformation(boolean isAvailableTv, double tvPrice) {
		this.isAvailable=isAvailableTv;
		this.tvPrice=tvPrice;
		printInfo();
	}

	

}
