package DesignPattern.Observer;

import java.util.ArrayList;
import java.util.List;

public class GrabInformation implements Flipkart {

	
	List<Observer> observerList = null;
	double tvPrice = 0;
	boolean isAvailable=false;
	
	GrabInformation(){
		observerList=new ArrayList<>();
	}
	@Override
	public void addObserver(Observer addObserver) {
		observerList.add(addObserver);
		System.out.println("Add New Observer");
	}

	
	public void setTvPrice(double tvPrice) {
		this.tvPrice = tvPrice;
		 notifyObserver();
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
		 notifyObserver();
	}
	@Override
	public void removeObserver(Observer removeObserver) {
		int index = observerList.indexOf(removeObserver);
		System.out.println("Remove Observer"+index+1);
		observerList.remove(index);
	}

	@Override
	public void notifyObserver() {
		for(Observer observer: observerList)
			observer.updateInformation(isAvailable, tvPrice);
		
	}
	
}
