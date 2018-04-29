package car_service;

import java.text.*;
import java.util.*;

public class CreditCard {
	String cardNumber;
	String cardholder;
	Date expiryDate;
	
	public CreditCard(String cardNumber, String cardholder, Date date){
		this.cardNumber = cardNumber;
		this.cardholder = cardholder;
		this.expiryDate = date;
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
}
