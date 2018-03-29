package car_service;

import java.text.*;
import java.util.*;

public class CreditCard {
	String cardNumber;
	String cardholder;
	Date expiryDate;
	
	public CreditCard(String cardNumber, String cardholder, String date){
		this.cardNumber = cardNumber;
		this.cardholder = cardholder;
		try {
			this.expiryDate = new SimpleDateFormat().parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
