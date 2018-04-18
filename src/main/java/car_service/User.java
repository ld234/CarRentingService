package car_service;

import java.text.*;
import java.util.*;

public class User {
	public static final String DATE_FORMAT = "dd-MM-yyyy";
	protected String username;
	protected String password;
	protected String firstname;
	protected String lastname;
	protected Date dob;
	
	public User(String username, String password, String fn, String ln, String dob) {
		this.username = username;
		this.password = password;
		firstname = fn;
		lastname = ln;
		try {
			this.dob = new SimpleDateFormat(DATE_FORMAT).parse(dob);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getFullName() {
		return firstname +" "+ lastname;
	}
	
	public String getFirstName() {
		return firstname ;
	}
	
	public String getLastName() {
		return lastname;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getDOB() {
		return new SimpleDateFormat(DATE_FORMAT).format(dob);
	}
	
	public boolean setPassword (String newpw) {
		if (newpw.length() < 8 || !containsDigit(newpw)) {
			return false;
		}
		password = newpw;
		return true;
	}
	
	public static boolean containsDigit(String s) {
		for (int i = 0 ; i < s.length(); i++) {
			if (s.charAt(i) >= 48 || s.charAt(i) <= 57) {
				return true;
			}
		}
		return false;
	}
	
	public boolean equals(User other) {
		return this.getUsername().equals(other.getUsername());
	}
	
	public int hashCode(){
	    return username.hashCode();
	}
}