package car_service;

public class Transaction {
	private CarRenter sender;
	private CarOwner receiver;
	private BookingRequest booking;
	private double amount;
	
	public Transaction (CarRenter sender, CarOwner receiver, BookingRequest br, double amount) {
		this.sender = sender;
		this.receiver = receiver;
		booking = br;
		this.amount = amount;
	}
}
