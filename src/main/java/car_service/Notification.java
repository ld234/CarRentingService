package car_service;

public class Notification {
	private enum NotifType{
		newBooking,
		acceptedBooking,
		rejectedBooking
	};
	private NotifType notif;
	private String message;
	private String receiver;
	
	public Notification(String type, String msg, String receiver) {
		setNotifType(type);
		message = msg;
		this.receiver = receiver;
	}
	
	public void setReceiver(String r ) {
		receiver = r;
	}
	
	public String getReceiver( ) {
		return receiver;
	}
	
	public void setNotifType(String notifType)
	{
		if (notifType.equals("newBooking"))
		{
			this.notif = NotifType.newBooking;
		}
		else if(notifType.equals("acceptedBooking")) {
			this.notif = NotifType.acceptedBooking;
		}
		else if (notifType.equals("rejectedBooking")) {
			this.notif = NotifType.rejectedBooking;
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
		return null;
	}
	
}
