package car_service;

public class Complaint {
	private Long cid;
	private String complainant;
	private Long listingNum;
	private String description;
	private boolean approved;
	
	public Complaint(long cid, String c, long lNum, String desc) {
		this.cid = cid;
		complainant = c;
		listingNum = lNum;
		description = desc;
		approved = false;
	}
	
	public Complaint(String c, long lNum, String desc) {
		this.cid = (long) 0;
		complainant = c;
		listingNum = lNum;
		description = desc;
	}
	
	public Complaint(long cid, String c, long lNum, String desc, boolean x) {
		this.cid = cid;
		complainant = c;
		listingNum = lNum;
		description = desc;
		approved = x;
	}
	
	public Long getCID() {
		return cid;
	}
	
	public String getComplainant() {
		return complainant;
	}
	
	public Long getListingNumber() {
		return listingNum;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean getApproved() {
		return approved;
	}
	
	public void approve() {
		approved = true;
	}
}
