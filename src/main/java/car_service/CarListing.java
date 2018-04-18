package car_service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


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
	HashMap<LocalDate,String > unavailable;
	HashMap<Date,String > Brands;
	
	public CarListing(long carListingNum, String rego, String brand, String model, String location, String colour, String transType, int year, int capacity, double odometer,String imgPath) {
		unavailable = new HashMap<LocalDate, String>();
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
			System.out.println("Error: wrong type of transmission");
		}
	}
	
	public boolean bookCarListing( String username,List<LocalDate> dates) {
		
		boolean returnVal = true;
		
		//Checks if booking is valid
		for(int i = 0; i< dates.size(); i++)
		{
			if(unavailable.containsKey(dates.get(i))) {
				return false;
			}
		}
		//books carlisting on the given dates
		for(int i = 0; i< dates.size(); i++)
		{
			unavailable.put(dates.get(i),username);
		}
		
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
		Iterator<HashMap.Entry<LocalDate, String>> it = unavailable.entrySet().iterator();
	    while (it.hasNext()) {
	        HashMap.Entry<LocalDate, String> pair = it.next();
	        System.out.println(pair.getKey() + " = " + pair.getValue());
	    }
	}
}
