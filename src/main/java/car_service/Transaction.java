package car_service;

import java.time.LocalDate;

public class Transaction {
	private CarRenter sender;
	private CarOwner receiver;
	private LocalDate from;
	private LocalDate to;
	private Long listingNum;
	private double amount;
	
	public Transaction (CarRenter sender, CarOwner receiver, BookingRequest br, double amount) {
		this.sender = sender;
		this.receiver = receiver;
		from = br.getFrom();
		to = br.getTo();
		listingNum = br.getListingNumber();
		this.amount = amount;
	}
}
