package car_service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;


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
	HashMap< Pair<LocalDate,LocalDate> ,String > unavailable;
	HashMap<Date,String > Brands;
	String owner;
	
	public CarListing(long carListingNum, String rego, String brand, String model, String location, String colour, String transType, int year, int capacity, double odometer,String imgPath, String owner) {
		unavailable = new HashMap< Pair<LocalDate,LocalDate>, String>();
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
		Pair<LocalDate, LocalDate> [] rangeList = null;
		rangeList = unavailable.keySet().toArray(rangeList);
		
		
		for(Pair <LocalDate, LocalDate> fromTo : rangeList )
		{
			boolean valid = (fromTo.getKey().isAfter(from) && fromTo.getKey().isAfter(to)) || (fromTo.getValue().isBefore(from) && fromTo.getValue().isBefore(to));
			if (!valid) {
				return false;
			}
		}
		//books carlisting on the given dates
		unavailable.put(new Pair<LocalDate,LocalDate>(from,to),username);
		/*
		//find how many days for booking
		long diff = Math.abs(toDate.getTime() - fromDate.getTime());
		long diffDays = diff / (24 * 60 * 60 * 1000);
		

		// set the from date to the calender
		Calendar cal = Calendar.getInstance();
		cal.setTime(fromDate);
		
		//Checks if  from and to date is already booked
		if(unavailable.containsKey(fromDate) && unavailable.containsKey(toDate)){
			return false;
		}
		else {
			
			//iterate through all the values in between to and from date
			for (int i = 1; i< diffDays; i++) {
				// increment from date by one
				cal.add(Calendar.DAY_OF_YEAR, 1);
				String dateString = cal.get(Calendar.YEAR)+"-"+((cal.get(Calendar.MONTH)+1)%12)+"-"+cal.get(Calendar.DAY_OF_MONTH);
				Date CheckDate = parseDate(dateString);
				System.out.println("check date:"+CheckDate);
				//Checks if date is booked
				if(unavailable.containsKey(CheckDate))
				{
					return false;
				}
			}
			//Sets the fromDate again to add to the booking list
			cal.setTime(fromDate);
			
			for (int i = 0; i<= diffDays; i++) {
				String keyDateStr = cal.get(Calendar.YEAR)+"-"+((cal.get(Calendar.MONTH)+1)%12)+"-"+cal.get(Calendar.DAY_OF_MONTH);
				Date keyDate = parseDate(keyDateStr);
				System.out.println("adding:"+keyDate);
				unavailable.put(keyDate,username);
				
				cal.add(Calendar.DAY_OF_YEAR, 1);
				System.out.println("successfully booked on"+cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH));
			}
			
		}
		
		*/
		return returnVal;
	}
	
	public String getTransmission() {
		if (transType == transmission.auto)
			return "auto";
		else
			return "manual";
	}
	
	
	private void setPrice(double price) {
		this.price = price;
	}
	
	//for testing*************************************************************
	public void showBooking() {
		Iterator<HashMap.Entry< Pair<LocalDate,LocalDate>, String>> it = unavailable.entrySet().iterator();
	    while (it.hasNext()) {
	    	HashMap.Entry< Pair<LocalDate,LocalDate>, String> pair = it.next();
	        System.out.println(pair.getKey() + " = " + pair.getValue());
	    }
	}
}
