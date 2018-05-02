package car_service;

import java.sql.SQLException;

//import java.util.HashMap;

public class UserSession {
	protected User user;
	//private ArrayList<Message> messages;
	//private HashMap<String,CarListing> carListing;
	
	public UserSession(String username) {
		try {
			user = new JDBCConnector().getUser(username);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public UserSession(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
}
