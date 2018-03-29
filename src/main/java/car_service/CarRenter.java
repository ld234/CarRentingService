package car_service;

public class CarRenter extends User{
	private String driverLicense;
	CreditCard creditCard;
	
	public CarRenter(String username, String password, String fn, String ln, String driverLicense,String dob) {
		super(username,password,fn,ln,dob);
		this.driverLicense = driverLicense;
	}
	
	public String getDriverLicense() {
		return driverLicense;
	}
	
	public String getCardNumber() {
		return creditCard.getCardNumber();
	}
	
	public String getExpiryDate() {
		return creditCard.getDate();
	}
}
