package car_service;

public class OwnerSession extends UserSession{
	
	public OwnerSession(String username) {
		super(username);
	}

	public void addCarListingToSession(CarListing cl) {
		((CarOwner) this.user).addCarListing(cl);
	}
	
	public void updateCarListingInSession(CarListing cl) {
		((CarOwner) this.user).updateCarListing(cl);
	}

	public void deleteCarListingInSession(long listingNum) {
		((CarOwner) this.user).deleteCarListing(listingNum);
	}
}
