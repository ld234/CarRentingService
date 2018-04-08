package car_service;

import java.util.HashMap;

public class CarOwner extends CarRenter{
	HashMap <Long,CarListing> carListings;
	
	public CarOwner(CarRenter cr, HashMap <Long,CarListing> carListings) {
		super(cr.getUsername(),cr.getPassword(),cr.getFirstName(),cr.getLastName(),cr.getDriverLicense(),cr.getDOB(),cr.getCard());
		this.carListings = carListings;
	}
	
	public void addCarListing(CarListing cl) {
		carListings.put(cl.getListingNumber(), cl);
	}
	
	public void updateCarListing(CarListing cl) {
		carListings.put(cl.getListingNumber(), cl);
	}

	public void deleteCarListing(long listingNum) {
		carListings.remove(new Long(listingNum));
		System.out.println(carListings.size());
	}
}
