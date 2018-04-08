package car_service;

import java.sql.SQLException;

//import java.util.HashMap;

public class Session {
	protected User user;
	//private ArrayList<Message> messages;
	//private HashMap<String,CarListing> carListing;
	
	public Session(String username) {
		try {
			user = new JDBCConnector().getUser(username);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public Session(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
}
