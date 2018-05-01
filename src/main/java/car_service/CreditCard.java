package car_service;

import java.text.*;
import java.util.*;

public class CreditCard {
	private String cardNumber;
	private String cardholder;
	private Date expiryDate;
	private int cvv;
	private double balance;
	
	public CreditCard(String cardNumber, String cardholder, Date date, int cvv, double balance){
		this.cardNumber = cardNumber;
		this.cardholder = cardholder;
		this.expiryDate = date;
		this.cvv = cvv;
		this.balance = balance;
	}
	
	public String getCardNumber() {
		return cardNumber;
	}
	
	public String getCardholder() {
		return cardholder;
	}
	
	public String getDate() {
		return new SimpleDateFormat(User.DATE_FORMAT).format(expiryDate);
	}
	
	public int getCvv() {
		return cvv;
	}
	
	public double getBalance() {
		return balance;
	}
}
