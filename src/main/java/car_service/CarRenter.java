package car_service;

public class CarRenter extends User{
	private String driverLicense;
	CreditCard creditCard;
	
	public CarRenter(String username, String password, String fn, String ln, String driverLicense) {
		super(username,password,fn,ln);
		this.driverLicense = driverLicense;
	}
	
	public String getDriverLicense() {
		return driverLicense;
	}
}
