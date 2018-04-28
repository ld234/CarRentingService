package car_service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.util.*;

public class CarOwner extends CarRenter{
	HashMap <Long,CarListing> carListings;
	@SuppressWarnings("restriction")
	HashMap < Pair<Long,String>,BookingRequest > bookingRequests;
	
	public CarOwner(CarRenter cr, HashMap <Long,CarListing> carListings) {
		super(cr.getUsername(),cr.getPassword(),cr.getFirstName(),cr.getLastName(),cr.getDriverLicense(),cr.getDOB(),cr.getCard(),cr.getNotifList());
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
	
	@SuppressWarnings("restriction")
	public void approveRequest(long listingNum, String renter) {
		BookingRequest br = bookingRequests.get(new Pair<Long,String>(listingNum,renter));
		br.approve();
		carListings.get(listingNum).bookCarListing(renter, br.getFrom(),br.getTo());
	}
	
	@SuppressWarnings("restriction")
	public void rejectRequest(long listingNum, String renter) {
		bookingRequests.get(new Pair<Long,String>(listingNum,renter)).reject();
	}
	
	public static List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) { 
		long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate); 
		return IntStream.iterate(0, i -> i + 1)
			      .limit(numOfDaysBetween)
			      .mapToObj(i -> startDate.plusDays(i))
			      .collect(Collectors.toList()); 
	}
}
