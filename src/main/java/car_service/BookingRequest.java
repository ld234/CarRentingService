package car_service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BookingRequest {
	private LocalDate from;
	private LocalDate to;
	private String renter;
	private long listingNum;
	private double totalPrice;
	
	public BookingRequest(LocalDate from, LocalDate to, String username, long listingNum) {
		this.from = from;
		this.to = to;
		this.renter = username;
		this.listingNum = listingNum;
		try {
			totalPrice = calcPrice();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public BookingRequest(LocalDate from, LocalDate to, String username, long listingNum,double price) {
		this.from = from;
		this.to = to;
		this.renter = username;
		this.listingNum = listingNum;
		totalPrice = price;
	}
	
	private double calcPrice() throws SQLException {
		System.out.println("Dates between: "+ChronoUnit.DAYS.between(from,to));
		return JDBCConnector.priceLookup(new JDBCConnector().getListing(listingNum).getBrand()) * (ChronoUnit.DAYS.between(from,to) + 1);
	}
	
	public double getPrice(){
		return totalPrice;
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
}
