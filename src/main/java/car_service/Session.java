package car_service;

//import java.util.HashMap;

public class Session {
	private User user;
	//private ArrayList<Message> messages;
	//private HashMap<String,CarListing> carListing;
	
	public Session(String username) {
		
	}
	
	public Session(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
}
