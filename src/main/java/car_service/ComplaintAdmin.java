package car_service;

import java.util.ArrayList;

public class ComplaintAdmin extends User {
	
	public ComplaintAdmin(String username, String password, String fn, String ln,String dob,ArrayList<Notification> a) {
		super(username,password,fn,ln, dob,a);
	}
}
