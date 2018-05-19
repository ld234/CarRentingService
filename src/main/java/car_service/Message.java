package car_service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Message implements Comparable<Message> {
	public static final String TIMESTAMP_FORMAT = "dd-MM-yyyy HH.mm.ss";
	private String sender;
	private String receiver;
	private String message ;
	private Timestamp tstamp;
	
	public Message(String s, String r, String m) {
		sender = s;
		receiver = r;
		message = m;
		tstamp = new Timestamp(System.currentTimeMillis());
	}
	
	public Message(String s, String r, String m, long ts) {
		sender = s;
		receiver = r;
		message = m;
		tstamp = new Timestamp(ts);
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public String getFormattedTimestamp() {
		return new SimpleDateFormat(Message.TIMESTAMP_FORMAT).format(tstamp);
	}
	
	public Timestamp getTimestamp() {
		return tstamp;
	}

	public int compareTo(Message o) {
		if (tstamp.before(o.getTimestamp()))
			return -1;
		if (tstamp.equals(o.getTimestamp()))
			return 0;
		return 1;
	}


}
