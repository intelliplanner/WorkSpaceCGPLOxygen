package DesignPattern.Observer;

public interface Flipkart {// Subject that contains or manage observerlist
	void addObserver(Observer observer);
	void removeObserver(Observer observer);
	void notifyObserver();
	
}
