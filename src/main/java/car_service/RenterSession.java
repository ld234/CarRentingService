package car_service;

public class RenterSession extends Session {

	public RenterSession(String username) {
		super(username);
	}
	
	public RenterSession(CarRenter u) {
		super (u);
	}
}
