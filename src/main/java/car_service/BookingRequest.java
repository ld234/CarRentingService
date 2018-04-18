package car_service;

import java.time.LocalDate;

public class BookingRequest {
	private LocalDate from;
	private LocalDate to;
	private String renter;
	private long listingNum;
	public BookingRequest(LocalDate from, LocalDate to, String username, long listingNum) {
		this.from = from;
		this.to = to;
		this.renter = username;
		this.listingNum = listingNum;
	}
	
	public LocalDate getFrom() {
		return from;
	}
	
	public LocalDate getTo() {
		return to;
	}
	
	public String getRenter() {
		return renter;
	}
	
	public long getListingNumber() {
		return listingNum;
	}
	
	public void approve() {
	}
	
	public void reject() {
	}
}
