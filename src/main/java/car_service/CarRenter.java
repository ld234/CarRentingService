package car_service;

import java.util.ArrayList;

public class CarRenter extends User{
	private String driverLicense;
	private CreditCard creditCard;
	private String socialMediaLink;
	
	public CarRenter(String username, String password, String fn, String ln, String driverLicense,String dob,CreditCard creditCard,ArrayList<Notification> notifList,String link) {
		super(username,password,fn,ln,dob,notifList);
		this.driverLicense = driverLicense;
		this.creditCard = creditCard;
		socialMediaLink = link;
	}
	
	public String getDriverLicense() {
		return driverLicense;
	}
	
	public String getCardNumber() {
		return creditCard.getCardNumber();
	}
	
	public CreditCard getCard() {
		return creditCard;
	}
	
	public String getExpiryDate() {
		return creditCard.getDate();
	}
	
	public String getSocialMediaLink() {
		return socialMediaLink;
	}
}
