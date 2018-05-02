package car_service;

import java.time.LocalDate;

public class Transaction {
	private String sender;
	private String receiver;
	private LocalDate from;
	private LocalDate to;
	private Long listingNum;
	private double amount;
	
	public Transaction (String sender, String receiver, BookingRequest br, double amount) {
		this.sender = sender;
		this.receiver = receiver;
		from = br.getFrom();
		to = br.getTo();
		listingNum = br.getListingNumber();
		this.amount = amount;
	}
	
	public Transaction (String sender, String receiver, LocalDate from , LocalDate to, long listingNum, double amount) {
		this.sender = sender;
		this.receiver = receiver;
		this.from = from;
		this.to = to;
		this.listingNum = listingNum;
		this.amount = amount;
	}
}
