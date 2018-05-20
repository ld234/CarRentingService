package car_service;

public class Notification {
	private enum NotifType{
		newBooking,
		acceptedBooking,
		rejectedBooking,
		newComplaint,
		newReview,
		newMessage
	};
	private long notifNum;
	private NotifType notif;
	private String message;
	private String receiver;
	private boolean seen;
	
	public Notification(long notifNum, String type, String msg, String receiver) {
		this.notifNum = notifNum;
		setNotifType(type);
		message = msg;
		this.receiver = receiver;
		seen = false;
	}
	
	public Notification(String type, String msg, String receiver) {
		notifNum = -1;
		setNotifType(type);
		message = msg;
		this.receiver = receiver;
		seen = false;
	}
	
	public Notification(long notifNum,String type, String msg, String receiver, boolean s) {
		this.notifNum = notifNum;
		setNotifType(type);
		message = msg;
		this.receiver = receiver;
		seen = s;
	}
	
	public long getNotifNumber() {
		return notifNum;
	}
	
	public void seen () {
		seen = true;
	}
	
	public boolean getSeen () {
		return seen;
	}
	
	public void setReceiver(String r ) {
		receiver = r;
	}
	
	public String getReceiver( ) {
		return receiver;
	}
	
	public void setNotifType(String notifType)
	{
		if (notifType.equals("newBooking")){
			this.notif = NotifType.newBooking;
		}
		else if(notifType.equals("acceptedBooking")) {
			this.notif = NotifType.acceptedBooking;
		}
		else if (notifType.equals("rejectedBooking")) {
			this.notif = NotifType.rejectedBooking;
		}
		else if (notifType.equals("newComplaint")) {
			this.notif = NotifType.newComplaint;
		}
		else if (notifType.equals("newReview")) {
			this.notif = NotifType.newReview;
		}
		else if (notifType.equals("newMessage")) {
			this.notif = NotifType.newMessage;
		}
		else {
			System.out.println("Error: wrong type of booking.");
		}
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getType() {
		if (this.notif == NotifType.acceptedBooking)
			return "acceptedBooking";
		if (this.notif == NotifType.newBooking)
			return "newBooking";
		if (this.notif == NotifType.rejectedBooking)
			return "rejectedBooking";
		if (this.notif == NotifType.newComplaint)
			return "newComplaint";
		if (this.notif == NotifType.newReview)
			return "newReview";
		if (this.notif == NotifType.newMessage)
			return "newMessage";
		return null;
	}
	
}
