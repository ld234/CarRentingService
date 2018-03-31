package car_service;

public class CarRenter extends User{
	private String driverLicense;
	CreditCard creditCard;
	
	public CarRenter(String username, String password, String fn, String ln, String driverLicense,String dob,CreditCard creditCard) {
		super(username,password,fn,ln,dob);
		this.driverLicense = driverLicense;
		this.creditCard = creditCard;
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
}
