package car_service;

public class Car {
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
//	private double price;
	private String img;
	private String owner;
	
	public Car (String rego, String brand, String model, String location, String colour, String transType, int year, int capacity, double odometer,String imgPath, String owner) {
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
	
	public String getLocation() {
		return location;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public double getOdometer() {
		return odometer;
	}
	
	public String getTransmission() {
		if (transType == transmission.auto)
			return "auto";
		else
			return "manual";
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
}

