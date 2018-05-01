package car_service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


public class CarListing {
	private long listingNumber;
	private String rego;
	private String brand;
	private String model;
	private String location;
	private String colour;
	public static enum transmission{
		auto,
		manual
	};
	private transmission transType;
	private int year;
	private int capacity;
	private double odometer;
	private double price;
	private double totalPrice;
	private int rating;
	private String img;
	HashMap<LocalDate, Pair<LocalDate, String> >unavailable;
	String owner;
	
	public CarListing(long carListingNum, String rego, String brand, String model, String location, String colour, String transType, int year, int capacity, double odometer,String imgPath, String owner) {
		unavailable = new HashMap< LocalDate,Pair<LocalDate, String> >();
		listingNumber = carListingNum;
		this.rego= rego;
		this.brand= brand;
		this.model= model;
		this.location= location;
		this.colour= colour;
		this.year= year;
		this.capacity= capacity;
		this.odometer= odometer;
		setTransmission(transType);
		img = imgPath;
		this.owner = owner;
		setPrice(JDBCConnector.priceLookup(brand));
	}
	
	public String getOwner() {
		return owner;
	}
	
	public String getImgPath() {
		return img;
	}
	
	public long getListingNumber() {
		return listingNumber;
	}
	
	public String getRego() {
		return rego;
	}
	
	public String getBrand() {
		return brand;
	}
	
	public int getYear() {
		return year;
	}
	
	public String getModel() {
		return model;
	}
	
	public String getColour() {
		return colour;
	}
	
	public double getPrice() {
		return price;
	}
	
	public String getLocation() {
		return location;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public double getOdometer() {
		return odometer;
	}
	
	//set the transmission to either manual or auto
	public void setTransmission(String transmissionType)
	{
		if (transmissionType.toLowerCase().equals("auto"))
		{
			this.transType = transmission.auto;
		}
		else if(transmissionType.toLowerCase().equals("manual")) {
			this.transType = transmission.manual;
		}
		else {
			System.err.println("Error: wrong type of transmission");
		}
	}
	
	public boolean bookCarListing( String username,LocalDate from, LocalDate to) {
		
		boolean returnVal = true;
		
		//Checks if booking is valid, the from is not within any range, the to is not within any range
		LocalDate [] rangeList = new LocalDate[unavailable.size()];
		rangeList = unavailable.keySet().toArray(rangeList);
		
		if (rangeList != null) {
			for(LocalDate f : rangeList )
			{
				boolean valid = (f.isAfter(to) || (unavailable.get(f).getKey().isBefore(from)));
				if (!valid) {
					return false;
				}
			}
		}
		//books carlisting on the given dates
		unavailable.put(from,new Pair<LocalDate,String>(to,username));
		return returnVal;
	}
	
	public String getTransmission() {
		if (transType == transmission.auto)
			return "auto";
		else
			return "manual";
	}
	
	
	private void setPrice(double p) {
		if (year > 2000 && year <=2010) {
			this.price = p*0.75;
		}
		else if (year <= 1999) {
			this.price = p*0.5;
		}
		else {
			this.price = p;
		}
	}
	
	//for testing*************************************************************
	public void showBooking() {
		Iterator<Entry<LocalDate, Pair<LocalDate, String>>> it = unavailable.entrySet().iterator();
	    while (it.hasNext()) {
	    	Entry<LocalDate, Pair<LocalDate, String>> pair = it.next();
	        System.out.println(pair.getKey() + " = " + pair.getValue());
	    }
	}
}
