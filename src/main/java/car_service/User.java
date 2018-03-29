package car_service;
public class User {
	String username;
	String password;
	String firstname;
	String lastname;
	
	public User(String username, String password, String fn, String ln) {
		this.username = username;
		this.password = password;
		firstname = fn;
		lastname = ln;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getFullName() {
		return firstname + lastname;
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
	
	public boolean setPassword (String newpw) {
		if (newpw.length() < 8 || !containsDigit(newpw)) {
			return false;
		}
		password = newpw;
		return true;
	}
	
	public static boolean containsDigit(String s) {
		for (int i = 0 ; i < s.length(); i++) {
			if (s.charAt(i) <48 || s.charAt(i) > 57) {
				return false;
			}
		}
		return true;
	}
}