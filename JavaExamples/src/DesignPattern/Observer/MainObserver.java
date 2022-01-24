package DesignPattern.Observer;

public class MainObserver {

	public static void main(String[] args) {
		GrabInformation obj = new GrabInformation();
		InformationObserver info = new InformationObserver(obj);
		obj.setAvailable(true);
		obj.setTvPrice(200000.00);
		
		GrabInformation obj1 = new GrabInformation();
		InformationObserver info1 = new InformationObserver(obj1);
		obj1.setAvailable(true);
		obj1.setTvPrice(200000.00);
		
	}

}
