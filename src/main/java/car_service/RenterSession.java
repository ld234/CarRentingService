package car_service;

public class RenterSession extends UserSession {

	public RenterSession(String username) {
		super(username);
	}
	
	public RenterSession(CarRenter u) {
		super (u);
	}
}
