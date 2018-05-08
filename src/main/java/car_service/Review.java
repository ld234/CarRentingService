package car_service;

import java.sql.Timestamp;

public class Review {
	private String reviewMessage;
	private int rating;
	private String reviewer;
	private long listingNum;
	private Timestamp tstamp;
	
	public Review(long listingNum, String reviewer, int r, String rM) {
		this.reviewer = reviewer;
		rating = r;
		reviewMessage = rM;
		this.listingNum = listingNum;
		tstamp = new Timestamp(System.currentTimeMillis());
	}
	
	public Review(long listingNum, String reviewer, int r, String rM, long ts) {
		this.reviewer = reviewer;
		rating = r;
		reviewMessage = rM;
		this.listingNum = listingNum;
		tstamp = new Timestamp(ts);
	}
	
	public Timestamp getTimestamp() {
		return tstamp;
	}
	
	public long getListingNumber() {
		return listingNum ;
	}
	
	public String getReviewer() {
		return reviewer;
	}
	
	public String getReviewMessage() {
		return reviewMessage;
	}
	
	public int getRating() {
		return rating;
	}
	
	public void setMessage(String m) {
		reviewMessage = m;
	}
	
	public void setReviewer(String r) {
		reviewer = r;
	}
	
	public void setRating(int r) {
		rating = r;
	}
}
