package car_service;

import java.util.HashMap;

public class CarOwner extends CarRenter{
	HashMap <String,CarListing> carListings;
	
	public CarOwner(CarRenter cr, HashMap <String,CarListing> carListings) {
		super(cr.getUsername(),cr.getPassword(),cr.getFirstName(),cr.getLastName(),cr.getDriverLicense(),cr.getDOB(),cr.getCard());
		this.carListings = carListings;
	}
}
