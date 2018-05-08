package car_service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.ArrayList;

public class CarListing {
	private Car car;
	HashSet<LocalDate> unavailable;
	HashSet<LocalDate> available;
	private double price;
	private double rating;
	private long carListingNumber ;
	private ArrayList<Review> reviewList;
	
	public CarListing(long carListingNum, String rego, String brand, String model, String location, String colour, String transType, int year, int capacity, double odometer,String imgPath, String owner, HashSet<LocalDate> avail) {
		carListingNumber = carListingNum;
		car = new Car(rego,brand,model,location,colour,transType,year,capacity,odometer,imgPath,owner);
		unavailable = new HashSet<LocalDate>();
		setPrice(JDBCConnector.priceLookup(brand));
		available = avail;
		setRating();
	}
	
	public CarListing(long carListingNum, String rego, String brand, String model, String location, String colour, String transType, int year, int capacity, double odometer,String imgPath, String owner) {
		carListingNumber = carListingNum;
		car = new Car(rego,brand,model,location,colour,transType,year,capacity,odometer,imgPath,owner);
		unavailable = new HashSet<LocalDate>();
		setPrice(JDBCConnector.priceLookup(brand));
		available = new HashSet<LocalDate>();
		setRating();
	}
	
	public CarListing(long carListingNum, String rego, HashSet<LocalDate> avail) {
		carListingNumber = carListingNum;
		try {
			car = new JDBCConnector().getCar(rego);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		unavailable = new HashSet<LocalDate>();
		setPrice(JDBCConnector.priceLookup(car.getBrand()));
		available = avail;
		setRating();
	}
	
	public CarListing(long carListingNum, String rego) {
		carListingNumber = carListingNum;
		try {
			car = new JDBCConnector().getCar(rego);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		unavailable = new HashSet<LocalDate>();
		setPrice(JDBCConnector.priceLookup(car.getBrand()));
		available = new HashSet<LocalDate>();
		setRating();
	}
	
	public void setRating() {
		try {
			rating = new JDBCConnector().calcRating(carListingNumber);
		} catch (SQLException e) {
			e.printStackTrace();
			rating = 0;
		}
	}
	
	public String getOwner() {
		return car.getOwner();
	}
	
	public String getImgPath() {
		return car.getImgPath();
	}
	
	public long getListingNumber() {
		return carListingNumber;
	}
	
	public String getRego() {
		return car.getRego();
	}
	
	public String getBrand() {
		return car.getBrand();
	}
	
	public int getYear() {
		return car.getYear();
	}
	
	public String getModel() {
		return car.getModel();
	}
	
	public String getColour() {
		return car.getColour();
	}
	
	public double getPrice() {
		return price;
	}
	
	public String getLocation() {
		return car.getLocation();
	}
	
	public int getCapacity() {
		return car.getCapacity();
	}
	
	public double getOdometer() {
		return car.getOdometer();
	}
	
	//set the transmission to either manual or auto
	public void setTransmission(String transmissionType){
		car.setTransmission(transmissionType);
	}
	
	public boolean bookCarListing( String username,LocalDate from, LocalDate to) {
		//Checks if booking is valid, the from is not within any range, the to is not within any range
		if (!isAvailable(from,to))
			return false;
		//books carlisting on the given dates
		for (LocalDate d : getDatesBetween(from,to))
			unavailable.add(d);
		return true;
	}
	
	public boolean isAvailable(LocalDate from, LocalDate to) {
//		LocalDate [] rangeList = new LocalDate[unavailable.size()];
//		rangeList = unavailable.toArray(rangeList);
		List<LocalDate> dates = getDatesBetween(from,to);
		if (available.contains(null)) {
			for(LocalDate d : dates) {
				if (unavailable.contains(d)) {
					return false;
				}
			}
		}
		else {
			System.out.println("available is not infinite");
			printAvail();
			for (LocalDate d : dates) {
				if (!available.contains(d)) {
					System.out.println(d.toString());
					return false;
				}
			}
			
		}
		return true;
	}
	
	public void printAvail() {
		System.out.println(available.size());
		for (LocalDate d : available) {
			System.out.println(d.toString());
		}
	}
	
	public String getTransmission() {
		return car.getTransmission();
	}
	
	
	private void setPrice(double p) {
		if (car.getYear() > 2000 && car.getYear() <=2010) {
			this.price = p*0.75;
		}
		else if (car.getYear() <= 1999) {
			this.price = p*0.5;
		}
		else {
			this.price = p;
		}
	}
	
	public void addAvailableDate(LocalDate d) {
		available.add(d);
	}
	
	public double getRating()	{
		return rating;
	}
	
	public void setAvailable(HashSet<LocalDate> a)	{
		available = a;
	}
	
	public static List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) { 
	  
	    long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate.plusDays(1)); 
	    return IntStream.iterate(0, i -> i + 1)
	      .limit(numOfDaysBetween)
	      .mapToObj(i -> startDate.plusDays(i))
	      .collect(Collectors.toList()); 
	}
	
	public HashSet<LocalDate> getAvailableDates(){
		return available;
		
	}
	
	public Car getCar() {
		return car;
	}
}
