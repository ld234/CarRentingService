package car_service;

import java.util.Collection;
import java.util.HashMap;

public class CarOwner extends CarRenter{
	private HashMap <Long,CarListing> carListings;
	private HashMap < Pair<Long,String>,BookingRequest > bookingRequests;
	private String bsb;
	private String accountNumber;
	
	/*public CarOwner(CarRenter cr, HashMap <Long,CarListing> carListings) {
		super(cr.getUsername(),cr.getPassword(),cr.getFirstName(),cr.getLastName(),cr.getDriverLicense(),cr.getDOB(),cr.getCard(),cr.getNotifList());
		this.carListings = carListings;
		bookingRequests = new HashMap< Pair<Long,String>,BookingRequest>();
	}*/
	
	public CarOwner(CarRenter cr, HashMap <Long,CarListing> carListings,HashMap< Pair<Long,String>,BookingRequest>br, String bsb, String accNum) {
		super(cr.getUsername(),cr.getPassword(),cr.getFirstName(),cr.getLastName(),cr.getDriverLicense(),cr.getDOB(),cr.getCard(),cr.getNotifList(),cr.getSocialMediaLink());
		this.carListings = carListings;
		bookingRequests = br;
		this.bsb = bsb;
		accountNumber = accNum;
	}
	
	public String getBSB() {
		return bsb;
	}
	
	public String getAccountNumber() {
		return accountNumber;
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
	
	public void approveRequest(long listingNum, String renter) {
		BookingRequest br = bookingRequests.get(new Pair<Long,String>(listingNum,renter));
		carListings.get(listingNum).bookCarListing(renter, br.getFrom(),br.getTo());
	}
	
	public void rejectRequest(long listingNum, String renter) {
		bookingRequests.remove(new Pair<Long,String>(listingNum,renter));
	}
	
	public Collection <BookingRequest> getBookingRequestList() {
		Collection <BookingRequest> result = bookingRequests.values();
		return result; 
	}
	
	public void addBookingRequest(BookingRequest br) {
		bookingRequests.put(new Pair<Long,String>(br.getListingNumber(),br.getRenter()),br);
	}
	
	public BookingRequest getBookingRequest(Long l, String requester) {
		return bookingRequests.get(new Pair<Long,String>(l,requester));
	}
	
	public HashMap<Long,CarListing> getCarListingList(){
		return carListings;
	}
}
