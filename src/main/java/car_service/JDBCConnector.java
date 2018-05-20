package car_service;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class JDBCConnector {
	public static Long listingCount; 
	private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/car_service?verifyServerCertificate=false&useSSL=true";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "root";
	private Connection dbConnection = null;
	private static HashMap<String,Double> carPrices;
	
	public JDBCConnector() {
		carPrices = new HashMap<String,Double>();
		try {
			listingCount = new Long(getListingCount());
			loadCarPrice();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static double priceLookup (String brand) {
		return carPrices.get(brand);
	}

	private void loadCarPrice() throws SQLException{
		Statement statement = null;
		String loadCarPriceSQL = "SELECT * FROM CARPRICE;";
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(loadCarPriceSQL);
			while (rs.next()) {
				carPrices.put(rs.getString("BRAND"), rs.getDouble("RATE"));
			}
			rs.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	// Query for existence of a username
	public boolean usernameExists (String username) throws SQLException {
		Statement statement = null;
		String findUserSQL = "SELECT USERNAME FROM USER WHERE USERNAME = \'" + username + "\';";
		boolean exists = false;
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			ResultSet rs = statement.executeQuery(findUserSQL);
			if (rs.next()) {
				exists = true;
			}
			rs.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return exists;
	}

	// Insert a new user into the database
	public void insertUser(CarRenter user) throws SQLException {
		Statement statement = null;

		String insertUserSQL = "INSERT INTO USER VALUES (\'"
				+ user.getUsername() + "\', \'"
				+ user.getPassword() + "\', \'"
				+ user.getFirstName() + "\', \'"
				+ user.getLastName() + "\', "
				+ "STR_TO_DATE(\'" + user.getDOB().toString() + "\', '%d-%m-%Y'),"
				+ "\'RENTER\'"
				+ ");";
		String insertUserSQL2 = "INSERT INTO CARRENTER VALUES (\'"
				+ user.getUsername() + "\', \'"
				+ user.getDriverLicense() + "\', \'"
				+ user.getCardNumber() + "\'," 
				+ "NULL);";

		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			System.out.println(insertUserSQL2);
			System.out.println(insertUserSQL);
			statement.execute(insertUserSQL);
			statement.execute(insertUserSQL2);
			System.out.println("New user created");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	// Query for login - whether username exists
	public String findUsernameAndPassword(String username, String password) throws SQLException{
		Statement statement = null;
		String findUserSQL = "SELECT TYPE FROM USER WHERE USERNAME = \'"
							+ username + "\' AND PASSWORD = \'"+ password + "\';";
		String type = null;
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			ResultSet rs = statement.executeQuery(findUserSQL);
			if (rs.next()) {
				type = rs.getString("TYPE");
			}
			rs.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return type;
	}
	
	// Verify license
	public boolean findLicense(String license,String fn, String ln, String dob) throws SQLException{
		Statement statement = null;
		String findLicenseSQL = "SELECT LICENSENUM FROM DRIVERLICENSE " 
							+ "WHERE LICENSENUM = \'"
							+ license + "\' AND "
							+ "FIRSTNAME = \'"
							+ fn + "\' AND "
							+ "LASTNAME = \'"
							+ ln + "\' AND "
							+ "DATE_FORMAT(DOB,\'%d-%m-%Y\') = \'"
							+ dob+  "\';";
		String findLicenseExist = "SELECT LICENSENUM FROM CARRENTER " 
				+ "WHERE LICENSENUM = \'"
				+ license +  "\';";
		System.out.println("findLicenseSQL");
		
		boolean exists = false;
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			boolean valid = statement.executeQuery(findLicenseSQL).next();
			boolean duplicate = statement.executeQuery(findLicenseExist).next();
			if (valid && !duplicate) {
				exists = true;
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return exists;
	}
	
	public boolean findCarOwner(String username) throws SQLException{
		Statement statement = null;
		String findCarOwnerSQL = "SELECT USERNAME FROM CAROWNER WHERE USERNAME = \'"
							+ username +  "\';";
		boolean exists = false;
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			ResultSet rs = statement.executeQuery(findCarOwnerSQL);
			
			if (rs.next()) {
				exists = true;
				System.out.println(findCarOwnerSQL);
			}
			rs.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return exists;
	}
	
	// Query for verification of credit card with third party
	public boolean findCreditCard(String cardNum, String cardholder, String expiryDate, int cvv) throws SQLException{
		Statement statement = null;
		String findLicenseSQL = "SELECT CARDNUMBER FROM CREDITCARD " 
							+ "WHERE CARDNUMBER = \'"
							+ cardNum + "\' AND "
							+ "CARDHOLDER = \'"
							+ cardholder + "\' AND "
							+ "DATE_FORMAT(EXPIRYDATE,\'%d-%m-%Y\') = \'"
							+ expiryDate + "\' AND " 
							+ "CVV = " + cvv+ ";";
		boolean exists = false;
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			ResultSet rs = statement.executeQuery(findLicenseSQL);
			if (rs.next()) {
				exists = true;
			}
			rs.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return exists;
	}
	
	// Update user details
	public void updateUser(JSONObject jsObj, String username ) throws SQLException{
		Statement statement = null;
		int sz = jsObj.keySet().size();
		String updateUserSQL = "UPDATE USER SET ";
		String [] keys = new String[sz];
		keys = jsObj.keySet().toArray(keys);
		System.out.println(keys[0]);
		ArrayList<String> crFields = new ArrayList<String>();
		ArrayList<String> coFields = new ArrayList<String>();
		for (int i = 0; i < sz; i++) {
			if (!keys[i].equals("cardNumber") && !keys[i].equals("accountNumber") && !keys[i].equals("accountNumber")) {
				updateUserSQL+= keys[i]	+  " = \'" + jsObj.get(keys[i]) + "\'";
				if (i < sz-1) {
					updateUserSQL+= ", ";
				}
			}
			else if (keys[i].equals("cardNumber")){
				crFields.add(keys[i]);
			}
			else {
				coFields.add(keys[i]);
			}
				
		}
		String updateUserSQL2= "UPDATE CARRENTER SET ";
		for (int i = 0; i < crFields.size(); i++) {
			updateUserSQL2+= crFields.get(i)	+  " = \'" + jsObj.get(crFields.get(i)) + "\'";
			if (i < crFields.size()-1) {
				updateUserSQL2+= ", ";
			}				
		}
		updateUserSQL2 += " WHERE USERNAME = \'"+ username + "\';";
		
		String updateUserSQL3= "UPDATE CAROWNER SET ";
		for (int i = 0; i < coFields.size(); i++) {
			updateUserSQL3 += coFields.get(i)	+  " = \'" + jsObj.get(coFields.get(i)) + "\'";
			if (i < coFields.size()-1) {
				updateUserSQL3 += ", ";
			}				
		}
		updateUserSQL3 += " WHERE USERNAME = \'"+ username + "\';";

		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			if (jsObj.has("password")) {
				statement.execute(updateUserSQL);
				System.out.println(updateUserSQL);
			}
			if (jsObj.has("cardNumber")) {
				System.out.println(updateUserSQL2);
	            // execute the SQL statement
				statement.execute(updateUserSQL2);
			}

			if (jsObj.has("accountNumber") && jsObj.has("bsb")) {
				System.out.println(updateUserSQL3);
				statement.execute(updateUserSQL3);
			}
			System.out.println("User is updated");

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}	
	
	// Get user details
	public User getUser(String username) throws SQLException {
		Statement statement = null;
		Statement statement2 = null;
		String findUserSQL = "SELECT * FROM USER U JOIN CARRENTER C ON U.USERNAME = C.USERNAME WHERE U.USERNAME = \'"
							+ username + "\';";
		String findNotifSQL = "SELECT * FROM NOTIFICATION WHERE RECEIVER = \'" + username +"\';";
		
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			ArrayList<Notification> notifList = new ArrayList<Notification>();
			ResultSet rs2 = statement.executeQuery(findNotifSQL);
			if (rs2.next()){
				notifList.add(new Notification(rs2.getString("NOTIFTYPE"),rs2.getString("MESSAGE"),rs2.getString("RECEIVER")));
			}
			rs2.close();
			statement2 = dbConnection.createStatement();
			ResultSet rs = statement2.executeQuery(findUserSQL);
			rs.next();
			
			CreditCard cc = getCreditCard(rs.getString("CARDNUMBER"));
			
			CarRenter cr = new CarRenter(rs.getString(1),rs.getString("PASSWORD"),rs.getString("FIRSTNAME"),
					rs.getString("LASTNAME"),rs.getString("LICENSENUM"), 
					new SimpleDateFormat(User.DATE_FORMAT).format(new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("DOB"))),cc,notifList,
					rs.getString("SOCIALMEDIALINK"));
			
			if (!findCarOwner(username)) {
				System.out.println("Is car renter");
				return cr;
			}
			else {
				String selectOwnerSQL = "SELECT * FROM CAROWNER WHERE USERNAME = \'" +username + "\';";
				rs = statement.executeQuery(selectOwnerSQL);
				System.out.println("Is car owner");
				rs.next();
				return new CarOwner(cr,getCarListings(cr.getUsername()),getBookingRequestsByOwner(cr.getUsername()),rs.getString("BSB"),rs.getString("ACCOUNTNUMBER"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} catch(Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}finally {
			if (statement != null) {
				statement.close();
			}
			if (statement2 != null) {
				statement2.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return null;
	}
	
	// Get credit card details
	public CreditCard getCreditCard(String cardNum) throws SQLException{
		Statement statement = null;
		String findUserSQL = "SELECT * FROM CREDITCARD WHERE CARDNUMBER = \'"
							+ cardNum + "\';";
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			ResultSet rs = statement.executeQuery(findUserSQL);
			rs.next();
			return new CreditCard(rs.getString("CARDNUMBER"),rs.getString("CARDHOLDER"),new SimpleDateFormat("yyyy-mm-dd").parse(rs.getString("EXPIRYDATE")),
					rs.getInt("CVV"), rs.getInt("BALANCE"));
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return null;
	}
	
	// Get the number of listings
	public long getListingCount() throws SQLException{
		Statement statement = null;
		String getListingCountSQL = "SELECT MAX(LISTINGNUM) FROM LISTING;";
		long count = 0;
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			ResultSet rs = statement.executeQuery(getListingCountSQL);
			rs.next();
			count = rs.getLong(1);
			rs.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return count;
	}
	
	// Get a map of listings
	public HashMap<Long,CarListing> getCarListings(String carOwner) throws SQLException{
		Statement statement = null;
		Statement statement2 = null;
		String getListingsSQL = "SELECT * FROM LISTING L JOIN CAR C ON L.REGO = C.REGO "
				+ "WHERE C.OWNER = \'"+ carOwner +"\';";
//		String getUnvailability = "SELECT * FROM LISTING WHERE LISTINGNUM = (SELECT LISTINGNUM FROM LISTING WHERE OWNER = \'"+ carOwner +"\');";
//		String getAvailability = "SELECT * FROM AVAILABILITY WHERE LISTINGNUM = ";
//		String getListingsByOwner = "(SELECT LISTINGNUM FROM LISTING WHERE OWNER = \'"+ carOwner +"\');";
		HashMap<Long,CarListing> cls = new HashMap<Long,CarListing>();
		String getAvailByListing= "SELECT AVAILDATE FROM AVAILABILITY WHERE LISTINGNUM = ";
		
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			
			ResultSet rs = statement.executeQuery(getListingsSQL);
			
			
			while(rs.next()) {
				cls.put(rs.getLong("LISTINGNUM"), new CarListing(rs.getLong("LISTINGNUM"),
																 rs.getString("REGO"),
						                                         rs.getString("BRAND"),
						                                         rs.getString("MODEL"),
						                                         rs.getString("LOCATION"),
						                                         rs.getString("COLOUR"),
						                                         rs.getString("TRANSMISSION"),
						                                         rs.getInt("YEAR"),
						                                         rs.getInt("CAPACITY"),
						                                         rs.getDouble("ODOMETER"),rs.getString("IMAGEPATH"),
						                                         rs.getString("OWNER")
						                                         ));
			}
			rs.close();
			statement2 = dbConnection.createStatement();
			for (Long l : cls.keySet()) {
				rs = statement2.executeQuery(getAvailByListing  + l +";");
				HashSet<LocalDate> avail = new HashSet<LocalDate>();
				while(rs.next()) {
					avail.add(Instant.ofEpochMilli(rs.getDate("AVAILDATE").getTime())
							.atZone(ZoneId.systemDefault()).toLocalDate());
	//				cls.get(rs.getLong("LISTINGNUM")).bookCarListing(rs.getString("REQUESTER"), 
	//						Instant.ofEpochMilli(rs.getDate("FROMDATE").getTime())
	//						.atZone(ZoneId.systemDefault()).toLocalDate(),
	//						Instant.ofEpochMilli(rs.getDate("TODATE").getTime())
	//						.atZone(ZoneId.systemDefault()).toLocalDate());
				}
				System.out.println(avail.size());
				cls.get(l).setAvailable(avail);
				rs.close();	
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (statement2 != null) {
				statement2.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return cls;
	}
	
	// Find if rego exists
	public boolean findRego(String rego, String fn, String ln) throws SQLException{
		Statement statement = null;
		String findRegoSQL = "SELECT REGO FROM REGO " 
							+ "WHERE REGO = \'"
							+ rego + "\' AND "
							+ "FIRSTNAME = \'"
							+ fn + "\' AND "
							+ "LASTNAME = \'"
							+ ln +  "\';";
		String findRegoExist = "SELECT REGO FROM CAR " 
				+ "WHERE REGO = \'"
				+ rego +  "\';";
		boolean exists = false;
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			boolean valid = statement.executeQuery(findRegoSQL).next();
			boolean duplicate = statement.executeQuery(findRegoExist).next();
			if (valid && !duplicate) {
				exists = true;
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return exists;
	}
	
	// Add a new listing
	public void addCar(String owner, Car cl) throws SQLException {
		Statement statement = null;

		String insertListingSQL = "INSERT INTO CAR VALUES (\'"
				+ cl.getRego()+ "\', \'"
				+ cl.getBrand() + "\', \'"
				+ cl.getModel() + "\', \'"
				+ cl.getLocation() + "\', \'"
				+ cl.getColour() + "\', \'"
				+ cl.getTransmission() + "\', "
				+ cl.getYear() + ", "
				+ cl.getCapacity() + ", "
				+ cl.getOdometer() + ", \'"
				+ owner + "\', \'"
				+ cl.getImgPath() 
				+ "\');";

		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			statement.execute(insertListingSQL);
			System.out.println("New car created");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public void addListing(String owner, CarListing cl) throws SQLException {
		Statement statement = null;

		String insertListingSQL = "INSERT INTO LISTING VALUES ("
				+ cl.getListingNumber() +  ", \'"
				+ cl.getRego()+ "\', \'"
				+ owner + "\');";
		
		String insertListingSQL2 = "INSERT INTO AVAILABILITY VALUES ("
				+ cl.getListingNumber() +  ",";

		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			statement.execute(insertListingSQL);
			System.out.println("New listing created");
			for (LocalDate d : cl.getAvailableDates()) {
				System.out.println(insertListingSQL2 + "STR_TO_DATE(\'" + d.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "\', \'%d-%m-%Y\'));");
				statement.execute(insertListingSQL2 + "STR_TO_DATE(\'" + d.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "\', \'%d-%m-%Y\'));");
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	// Check if a car is owner's listing
	public boolean isOwnerListing(String owner, long listingNum) throws SQLException {
		Statement statement = null;
		String findOwnerSQL = "SELECT OWNER FROM LISTING " 
							+ "WHERE OWNER = \'"
							+ owner + "\' AND "
							+ "LISTINGNUM = "
							+ listingNum + ";";

		boolean exists = false;
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			exists = statement.executeQuery(findOwnerSQL).next();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return exists;
	}
	
	public Car getCar(String rego) throws SQLException	{
		Statement statement = null;
		String getCarSQL = "SELECT * FROM CAR WHERE REGO = \'" + rego +"\';";
		Car c = null;
		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(getCarSQL);
                        // execute the SQL statement
			ResultSet rs = statement.executeQuery(getCarSQL);
			if (rs.next()) {
				c = new Car( rs.getString("REGO"),
                        rs.getString("BRAND"),
                        rs.getString("MODEL"),
                        rs.getString("LOCATION"),
                        rs.getString("COLOUR"),
                        rs.getString("TRANSMISSION"),
                        rs.getInt("YEAR"),
                        rs.getInt("CAPACITY"),
                        rs.getDouble("ODOMETER"),rs.getString("IMAGEPATH"),
                        rs.getString("OWNER"));
			}
			rs.close();
			System.out.println("Get car");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return c;
	}
	
	// Update car listing
	public void updateListing(JSONObject jsObj, long listingNum) throws SQLException{
		Statement statement = null;
		Statement statement2 = null;
		Statement statement3 = null;
		
		System.out.println("In update jdbc2");
		String updateListingSQL = "UPDATE CAR SET ";
		JSONArray x = jsObj.getJSONArray("availableDates");
		jsObj.remove("availableDates");
		jsObj.remove("rego");
		int sz = jsObj.keySet().size();
		System.out.println(sz);
		System.out.println(jsObj.toString());
		String [] keys = new String[sz];
		keys = jsObj.keySet().toArray(keys);
		System.out.println("In update jdbc3");
		for (int i = 0; i < sz; i++) {
			if (keys[i].equals("odometer") || keys[i].equals("capacity"))
				updateListingSQL += keys[i] + " = " +jsObj.get(keys[i]);
			else 
				updateListingSQL+= keys[i]	+  " = \'" + jsObj.get(keys[i]) + "\'";
			if (i < sz-1) {
				updateListingSQL+= ", ";
			}
		}
		updateListingSQL += " WHERE REGO = ( SELECT REGO FROM LISTING WHERE LISTINGNUM ="+  listingNum + ");";
		
		String updateListingSQL2 = "DELETE FROM  AVAILABILITY WHERE LISTINGNUM = "+listingNum+";";
		String updateListingSQL3 = "INSERT INTO AVAILABILITY VALUES (" +listingNum +", ";
		System.out.println("In update jdbc");
		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(updateListingSQL);
			if (sz > 0)
                        // execute the SQL statement
				statement.execute(updateListingSQL);
			
			if (x != null) {
				statement2 = dbConnection.createStatement();
				statement2.execute(updateListingSQL2);
				statement3 = dbConnection.createStatement();
				for (Object d : x) {
					System.out.println(updateListingSQL3 + "STR_TO_DATE(\'" + d.toString() + "\', \'%d-%m-%Y\'));");
					statement3.execute(updateListingSQL3 + "STR_TO_DATE(\'" + d.toString() + "\', \'%d-%m-%Y\'));");
				}
			}
			System.out.println("Listing updated");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (statement2 != null) {
				statement.close();
			}
			if (statement3 != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public void deleteListing(long listingNum) throws SQLException{
		Statement statement = null;
		String deleteListingSQL1 = "DELETE FROM AVAILABILITY "
				+ " WHERE LISTINGNUM = "+ listingNum + ";";
		String deleteListingSQL2 = "DELETE FROM BOOKINGREQUEST "
				+ " WHERE LISTINGNUM = "+ listingNum + ";";
//		String deleteListingSQL3 = "DELETE FROM BOOKING "
//				+ " WHERE LISTINGNUM = "+ listingNum + ";";
		String deleteListingSQL4 = "DELETE FROM COMPLAINT "
				+ " WHERE LISTINGNUM = "+ listingNum + ";";
		String deleteListingSQL5 = "DELETE FROM REVIEW "
				+ " WHERE LISTINGNUM = "+ listingNum + ";";
		String deleteListingSQL = "DELETE FROM LISTING "
		+ " WHERE LISTINGNUM = "+ listingNum + ";";

		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(deleteListingSQL);
                        // execute the SQL statement
			statement.execute(deleteListingSQL5);
			statement.execute(deleteListingSQL4);
//			statement.execute(deleteListingSQL3);
			statement.execute(deleteListingSQL2);
			statement.execute(deleteListingSQL1);
			statement.execute(deleteListingSQL);
			System.out.println("Listing DELETED");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public void insertBookingRequest(BookingRequest br, Notification n) throws SQLException {
		Statement statement = null;
		long listingNum = br.getListingNumber();
		String insertBookingRequestSQL = "INSERT INTO BOOKINGREQUEST VALUES ( " +
									listingNum + ", " +
									"STR_TO_DATE(\'" + br.getFrom().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))+ "\', '%d-%m-%Y'), "+
									"STR_TO_DATE(\'" + br.getTo().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "\', '%d-%m-%Y'), \'"+
									br.getRenter()+"\'," + br.getPrice() +");";

		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(insertBookingRequestSQL);
                        // execute the SQL statement
			statement.execute(insertBookingRequestSQL);
			System.out.println("Booking MADE");
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public int getNotifCount () throws SQLException {
		Statement statement = null;
		String getNotifCountSQL = "SELECT MAX(NOTIFNUMBER) FROM NOTIFICATION;";
		int count = -1;
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs= statement.executeQuery(getNotifCountSQL);
			
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return count;
	}
	
	public String getListingOwner (Long listingNumber) throws SQLException {
		Statement statement = null;
		String getListingOwnerSQL = "SELECT OWNER FROM LISTING WHERE LISTINGNUM = " + listingNumber.longValue() + ";";
		String owner = "";
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs= statement.executeQuery(getListingOwnerSQL);
			
			if (rs.next()) {
				owner = rs.getString(1);
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return owner;
	}
	
	public void insertNotification(Notification notif) throws SQLException {
		Statement statement = null;
		int notifNum = this.getNotifCount() +1;
		String insertNotiSQL = "INSERT INTO NOTIFICATION VALUES ( " +
									notifNum+", \'" +
									notif.getMessage() + "\', \'"+
									notif.getType()+ "\',\'" +
									notif.getReceiver()+"\') ;";


		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(insertNotiSQL);
                        // execute the SQL statement
			statement.execute(insertNotiSQL);
			System.out.println("Notification MADE");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public CarListing getListing (Long listingNumber) throws SQLException {
		Statement statement = null;
		String getListingSQL = "SELECT * FROM CAR C JOIN LISTING L ON C.REGO = L.REGO WHERE LISTINGNUM = " + listingNumber.longValue() + ";";
		
		HashSet<LocalDate> avail = this.getAvailableDates(listingNumber);
		CarListing listing  = null;
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
	
			ResultSet rs= statement.executeQuery(getListingSQL);
			
			if (rs.next()) {
				listing   = new CarListing(rs.getLong("LISTINGNUM"),
						 rs.getString("REGO"),
                         rs.getString("BRAND"),
                         rs.getString("MODEL"),
                         rs.getString("LOCATION"),
                         rs.getString("COLOUR"),
                         rs.getString("TRANSMISSION"),
                         rs.getInt("YEAR"),
                         rs.getInt("CAPACITY"),
                         rs.getDouble("ODOMETER"),rs.getString("IMAGEPATH"),
                         rs.getString("OWNER"),
                         avail);
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return listing;
	}
	
	public ArrayList<CarListing> searchCarListing(HashMap<String,String> criteria) throws SQLException, ArrayIndexOutOfBoundsException{
		ArrayList<CarListing> result = new ArrayList<CarListing>();
		Statement statement = null;
		String from = criteria.remove("from");
		String to  = criteria.remove("to");
		List<LocalDate> datesBtween = CarListing.getDatesBetween(LocalDate.parse(from,DateTimeFormatter.ofPattern("dd-MM-yyyy")), 
				LocalDate.parse(to,DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		int sz = criteria.size();
		System.out.println(sz);
		String searchCarListingSQL = "SELECT L.LISTINGNUM,L.REGO, BRAND, MODEL,LOCATION,COLOUR,TRANSMISSION,"
									+ "YEAR, CAPACITY, ODOMETER,L.OWNER,IMAGEPATH "
									+ "FROM AVAILABILITY A JOIN LISTING L ON A.LISTINGNUM = L.LISTINGNUM "
									+ "JOIN CAR C ON C.REGO = L.REGO "
									+ "WHERE ";
		
		
		String [] keys = new String[sz];
		keys = criteria.keySet().toArray(keys);
		for (int i = 0; i < sz; i++) {
			if (keys[i].toLowerCase().equals("capacity"))
				searchCarListingSQL += keys[i] + " >= " + criteria.get(keys[i]);
			else if (keys[i].toLowerCase().equals("transmission") || keys[i].toLowerCase().equals("brand") || keys[i].toLowerCase().equals("model"))
				searchCarListingSQL += keys[i]	+  " = \'" + criteria.get(keys[i]) + "\'";
			else if (keys[i].toLowerCase().equals("location") )
				searchCarListingSQL += keys[i]	+  " LIKE \'%" + criteria.get(keys[i]) + "%\'";
			else if (keys[i].toLowerCase().equals("year") || keys[i].toLowerCase().equals("odometer") )
				searchCarListingSQL += keys[i]	+  " = " + criteria.get(keys[i]);
			else
				searchCarListingSQL += keys[i]	+  " = \'" + criteria.get(keys[i]) + "\'";
//			else if (keys[i].toLowerCase().equals("to") )
			searchCarListingSQL += " AND ";
		}
		searchCarListingSQL += "AVAILDATE IN (";
		for (int i= 0; i <  datesBtween.size(); i++ ) {
			searchCarListingSQL += "STR_TO_DATE(\'" 
								+ datesBtween.get(i).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
								+ "\',\'%d-%m-%Y\')";
			if(i<datesBtween.size()-1)
				searchCarListingSQL+=",";
		}
		searchCarListingSQL += ")";
		searchCarListingSQL += " GROUP BY LISTINGNUM HAVING COUNT(*) = " + datesBtween.size() + ";";

		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(searchCarListingSQL);
                        // execute the SQL statementS
			ResultSet rs2 = statement.executeQuery(searchCarListingSQL);
			while (rs2.next()) {
				HashSet<LocalDate> avail = this.getAvailableDates(rs2.getLong("LISTINGNUM"));
				result.add(new CarListing(rs2.getLong("LISTINGNUM"),
						 rs2.getString("REGO"),
                         rs2.getString("BRAND"),
                         rs2.getString("MODEL"),
                         rs2.getString("LOCATION"),
                         rs2.getString("COLOUR"),
                         rs2.getString("TRANSMISSION"),
                         rs2.getInt("YEAR"),
                         rs2.getInt("CAPACITY"),
                         rs2.getDouble("ODOMETER"),rs2.getString("IMAGEPATH"),
                         rs2.getString("OWNER"),
                         avail));
				System.out.println(rs2.getString("REGO") + " " + avail.size());
			}
			System.out.println("Listing searched");
			rs2.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		System.out.println("Res size: " + result.size());
		return result;
	}
	
	public void deleteAvailability(Long listingNum, List<LocalDate> datesDeleted) throws SQLException {
		Statement statement = null;
		String deleteAvailSQL = "DELETE FROM AVAILABILITY WHERE AVAILDATE IN (";
		for (int i = 0; i < datesDeleted.size(); i++ ) {
			deleteAvailSQL += "STR_TO_DATE(\'"+datesDeleted.get(i).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) +"\',\'"
					+ "%d-%m-%Y\')";
			if (i < datesDeleted.size()-1) {
				deleteAvailSQL+=",";
			}
		}
		deleteAvailSQL += ");";
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(deleteAvailSQL);
			System.out.println(deleteAvailSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public void deleteBookingRequest(String requester, Long listingNum) throws SQLException {
		Statement statement = null;
		String getListingOwnerSQL = "DELETE FROM BOOKINGREQUEST WHERE LISTINGNUM = " + listingNum.longValue() + " AND REQUESTER = \'" + requester +"\';";
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(getListingOwnerSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public HashSet<LocalDate> getAvailableDates(long listingNumber) throws SQLException{
		Statement statement = null;
		String getDatesAvail = "SELECT AVAILDATE FROM AVAILABILITY WHERE LISTINGNUM = " +listingNumber +";";
		HashSet<LocalDate> avail = new HashSet<LocalDate>();

		System.out.println(getDatesAvail);
		ResultSet rs = null;
		try {
			connect();
			statement = dbConnection.createStatement();
			rs = statement.executeQuery(getDatesAvail);
			System.out.println(getDatesAvail);
			while(rs.next()) {
				avail.add(LocalDate.parse(rs.getString("AVAILDATE"), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			}
			
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (rs != null)
				rs.close();
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return avail;
	}
	
	public HashMap<Pair<Long,String>,BookingRequest> getBookingRequestsByOwner(String owner) throws SQLException{
		Statement statement = null;
		HashMap<Pair<Long,String>,BookingRequest> hm = new HashMap<Pair<Long,String>,BookingRequest>();
		String getBookingRequestsByOwnerSQL = "SELECT B.LISTINGNUM,B.FROMDATE,B.TODATE,B.REQUESTER, B.PRICE FROM BOOKINGREQUEST B JOIN LISTING L ON B.LISTINGNUM = L.LISTINGNUM "
				+ " AND OWNER = \'" + owner +"\';";
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs= statement.executeQuery(getBookingRequestsByOwnerSQL);
			while(rs.next()) {
				hm.put(new Pair<Long, String>(rs.getLong("LISTINGNUM"),rs.getString("REQUESTER")),
						new BookingRequest(LocalDate.parse(rs.getString("FROMDATE"), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
								LocalDate.parse(rs.getString("TODATE"), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
								rs.getString("REQUESTER"),rs.getLong("LISTINGNUM"),rs.getDouble("PRICE")));
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return hm;
	}
	
	public BookingRequest getBookingRequest (String requester, Long listingNum) throws SQLException {
		Statement statement = null;
		BookingRequest br = null;
		String getBookingRequestSQL = "SELECT * FROM BOOKINGREQUEST WHERE LISTINGNUM = " + listingNum + " AND REQUESTER = \'" + requester + "\';";	
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs= statement.executeQuery(getBookingRequestSQL);
			if(rs.next()) {
				br = new BookingRequest(LocalDate.parse(rs.getString("FROMDATE"), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
						LocalDate.parse(rs.getString("TODATE"), DateTimeFormatter.ofPattern("yyyy-MM-dd")),
						rs.getString("REQUESTER"),rs.getLong("LISTINGNUM"),rs.getDouble("PRICE"));
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return br;
	}
	
/*	public BookingRequest addBooking (String requester, Long listingNum) throws SQLException{
		Statement statement = null;
		
		try {
			BookingRequest br = this.getBookingRequest(requester, listingNum);
			String addBookingSQL = "INSERT INTO BOOKING (SELECT * FROM BOOKINGREQUEST WHERE REQUESTER = \'"
					+ requester + "\' AND LISTINGNUM = " + listingNum + ");";
			connect();
			statement = dbConnection.createStatement();
			statement.execute(addBookingSQL);

			return br;
		} catch (SQLException | NullPointerException e) {
			e.getStackTrace();
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		
	}
*/	
	public BookingRequest insertTransaction (String requester, Long listingNum) throws SQLException{
		Statement statement = null;
		
		try {
			BookingRequest br = this.getBookingRequest(requester, listingNum);
			String insertTransactionSQL = "INSERT INTO TRANSACTION VALUES (" 
									+ br.getListingNumber() + ", STR_TO_DATE(\'"
									+ br.getFrom().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "\','%d-%m-%Y'), STR_TO_DATE(\'"
									+ br.getTo().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "\','%d-%m-%Y'), \'"
									+ br.getRenter() + "\', \'"
									+ this.getListingOwner(listingNum.longValue()) + "\',"
									+ br.getPrice()+");";
			connect();
			statement = dbConnection.createStatement();
			System.out.println(insertTransactionSQL);
			statement.execute(insertTransactionSQL);
			return br;
		} catch (SQLException | NullPointerException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public void conductTransaction(String requester, Long listingNum) throws SQLException{
		Statement statement = null;
		
		try {
			BookingRequest br = this.getBookingRequest(requester, listingNum);
			double amount = br.getPrice();
			CarOwner co = (CarOwner) this.getUser(this.getListingOwner(listingNum));
			CarRenter cr = (CarRenter) this.getUser(requester);
			String deductCreditCardBalanceSQL = "UPDATE CREDITCARD SET BALANCE = BALANCE - " + amount 
												+ " WHERE CARDNUMBER = \'" + cr.getCardNumber() + "\';";
			String increaseAccBalanceSQL = "UPDATE BANKACCOUNT SET BALANCE = BALANCE + " + amount 
											+ " WHERE BSB = \'"+ co.getBSB()+ "\' AND ACCOUNTNUMBER = \'" + co.getAccountNumber() + "\';" ;
			connect();
			statement = dbConnection.createStatement();
			statement.execute(deductCreditCardBalanceSQL);
			System.out.println(deductCreditCardBalanceSQL);
			statement.execute(increaseAccBalanceSQL);
			System.out.println(increaseAccBalanceSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public ArrayList<Message> getMessageSent(String username, String otherUser) throws SQLException{
		Statement statement = null;
		ArrayList<Message> result = new ArrayList<Message>();
		String getMessageSentSQL = "SELECT * FROM MESSAGE WHERE SENDER = \'" + username +"\' AND RECEIVER = \'" + otherUser + "\';";
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(getMessageSentSQL);
			System.out.println(getMessageSentSQL);
			
			while (rs.next()) {
				result.add(new Message(rs.getString("SENDER"),rs.getString("RECEIVER"),rs.getString("MESSAGE"),rs.getLong("TSTAMP")));
			}
			rs.close();
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return result;
	}
	
	public ArrayList<Message> getMessageReceived(String username, String otherUser) throws SQLException{
		Statement statement = null;
		ArrayList<Message> result = new ArrayList<Message>();
		String getMessageReceivedSQL = "SELECT * FROM MESSAGE WHERE RECEIVER = \'" + username + "\' AND SENDER = \'" + otherUser +"\';";
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(getMessageReceivedSQL);
			System.out.println(getMessageReceivedSQL);
			
			while (rs.next()) {
				result.add(new Message(rs.getString("SENDER"),rs.getString("RECEIVER"),rs.getString("MESSAGE"),rs.getLong("TSTAMP")));
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return result;
	}
	
	public void insertMessage(Message m) throws SQLException{
		Statement statement = null;
		String insertMessageSQL = "INSERT INTO MESSAGE VALUES( \'" + m.getSender() + "\', \'" 
									+ m.getReceiver() +"\', \'" 
									+ escapeMetaCharacters(m.getMessage()) +"\', " 
									+ m.getTimestamp().getTime() +");";
		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(insertMessageSQL);
			statement.execute(insertMessageSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public ArrayList<Transaction> getTransactions(String username) throws SQLException{
		Statement statement = null;
		ArrayList<Transaction> result = new ArrayList<Transaction>();
		String getTransactionSQL = "SELECT * FROM TRANSACTION WHERE SENDER = \'" + username + "\' OR RECEIVER = \'" +username +"\';";
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(getTransactionSQL);
			System.out.println(getTransactionSQL);
			
			while(rs.next()) {
				result.add(new Transaction(rs.getString("SENDER"),rs.getString("RECEIVER"),
						Instant.ofEpochMilli(rs.getDate("FROMDATE").getTime()).atZone(ZoneId.systemDefault()).toLocalDate(),
						Instant.ofEpochMilli(rs.getDate("TODATE").getTime()).atZone(ZoneId.systemDefault()).toLocalDate(),
						rs.getLong("LISTINGNUM"),rs.getDouble("AMOUNT")));
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return result;
	}
	
	public void upgradeAccount(String username, String accountNumber, String bsb) throws SQLException{
		Statement statement = null;
		String upgradeAccountSQL = "INSERT INTO CAROWNER VALUES( \'" + username +"\',\'" + bsb+ "\',\'"+accountNumber+"\');";
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(upgradeAccountSQL);
			System.out.println(upgradeAccountSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		
	}
	
	public void setSeen(long notiNumber) throws SQLException {
		Statement statement = null;
		String insertMessageSQL = "UPDATE NOTIFICATION SET SEEN = TRUE WHERE NOTIFNUMBER = ( " + notiNumber +");";
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(insertMessageSQL);
			System.out.println(insertMessageSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public long getCarCountByOwner(String owner) throws SQLException {
		Statement statement = null;
		String getCarCountByOwnerSQL = "SELECT COUNT(*) FROM CAR WHERE OWNER = \'" + owner +"\';";
		long count = 0;
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs =statement.executeQuery(getCarCountByOwnerSQL);
			if (rs.next()) {
				count = rs.getLong(1);
			}
			rs.close();
			System.out.println(getCarCountByOwnerSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return count;
	}
	
	public ArrayList<Review> getReviewsByListing(Long listingNum) throws SQLException {
		Statement statement = null;
		String getReviewsByListingSQL = "SELECT * FROM REVIEW WHERE LISTINGNUM = " + listingNum +";";
		ArrayList<Review> result = new ArrayList<Review>();
		
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs =statement.executeQuery(getReviewsByListingSQL);
			while (rs.next()) {
				result.add(new Review(rs.getLong("LISTINGNUM"),rs.getString("REVIEWER"),rs.getInt("RATING"),rs.getString("REVIEWMESSAGE"),rs.getLong("TSTAMP")));
			}
			rs.close();
			System.out.println(getReviewsByListingSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return result;
	}
	
	public void insertReview(Review r) throws SQLException{
		Statement statement = null;
		String insertMessageSQL = "INSERT INTO REVIEW VALUES(" +r.getListingNumber()+ ", \'" 
									+ r.getReviewer() +"\', \'" 
									+ escapeMetaCharacters(r.getReviewMessage()) +"\', " 
									+ r.getRating() +","
									+ r.getTimestamp().getTime() +");";
		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(insertMessageSQL);
			statement.execute(insertMessageSQL);
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public ArrayList<Car> getCarByOwner(String owner) throws SQLException {
		Statement statement = null;
		String getCarByOwnerSQL = "SELECT * FROM CAR WHERE OWNER = \'" + owner +"\';";
		ArrayList<Car> carList = new ArrayList<Car>();
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs =statement.executeQuery(getCarByOwnerSQL);
			while (rs.next()) {
				carList.add(new Car(rs.getString("REGO"),
						    rs.getString("BRAND"),
						    rs.getString("MODEL"),
						    rs.getString("LOCATION"),
						    rs.getString("COLOUR"),
						    rs.getString("TRANSMISSION"),
						    rs.getInt("YEAR"),
						    rs.getInt("CAPACITY"),
						    rs.getDouble("ODOMETER"),rs.getString("IMAGEPATH"),
						    rs.getString("OWNER")
						    ));
			}
			rs.close();
			System.out.println(getCarByOwnerSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return carList;
	}
	
	public double calcRating(Long listingNum) throws SQLException{
		Statement statement = null;
		String calcRatingSQL = "SELECT AVG(RATING) FROM REVIEW WHERE LISTINGNUM = "+listingNum+";";
		double r = 0;
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(calcRatingSQL);
			if(rs.next()) {
				r= rs.getDouble(1);
			}
			rs.close();
			System.out.println(calcRatingSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return r;
	}
	
	public void addComplaint(Complaint c) throws SQLException{
		Statement statement = null;
		String addComplaintSQL = "INSERT INTO COMPLAINT VALUES( "
				+ c.getCID() +", \'" 
				+ c.getComplainant() +"\'," 
				+ c.getListingNumber() + ", \'"
				+ c.getDescription() +"\');";
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(addComplaintSQL);
			System.out.println(addComplaintSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public long getComplaintID() throws SQLException{
		Statement statement = null;
		String getComplaintIDSQL = "SELECT MAX(CID) FROM COMPLAINT;";
		long r = 0;
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(getComplaintIDSQL);
			if(rs.next()) {
				r= rs.getLong(1);
			}
			rs.close();
			System.out.println(getComplaintIDSQL);
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return r+1;
	}
	
	public ArrayList<String> getAdmins() throws SQLException{
		Statement statement = null;
		String getAdminsSQL = "SELECT USERNAME FROM USER WHERE TYPE = 'CADMIN' OR TYPE = 'FADMIN';";
		ArrayList <String> r = new ArrayList<String>();
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(getAdminsSQL);
			while(rs.next()) {
				r.add(rs.getString("USERNAME"));
			}
			rs.close();
			System.out.println(getAdminsSQL);
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return r;
	}
	
	public boolean isCAdmin(String u) throws SQLException{
		Statement statement = null;
		String isCAdminSQL = "SELECT USERNAME FROM USER WHERE USERNAME = \'"+ u + "\' AND TYPE = 'CADMIN';";
		boolean r =false;
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(isCAdminSQL);
			if(rs.next()) {
				r = true;
			}
			rs.close();
			System.out.println(isCAdminSQL);
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return r;
	}	
			
	public boolean isFAdmin(String u) throws SQLException{
		Statement statement = null;
		String isFAdminSQL = "SELECT USERNAME FROM USER WHERE USERNAME = \'"+ u + "\' AND TYPE = 'FADMIN';";
		boolean r = false;
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(isFAdminSQL);
			while(rs.next()) {
				r = true;
			}
			rs.close();
			System.out.println(isFAdminSQL);
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return r;
	}		

	public ArrayList<Complaint> getApprovedComplaints() throws SQLException{
		Statement statement = null;
		String isFAdminSQL = "SELECT * FROM COMPLAINT WHERE APPROVED = TRUE;";
		ArrayList<Complaint> r = new ArrayList<Complaint>();
		//cid, String c, long lNum, String desc
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(isFAdminSQL);
			while(rs.next()) {
				Complaint c = new Complaint(rs.getLong("CID"),rs.getString("COMPLAINANT"),rs.getLong("LISTINGNUM"),rs.getString("DESCRIPTION"));
				c.approve();
				r.add(c);
			}
			rs.close();
			System.out.println(isFAdminSQL);
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return r;
	}
	
	public ArrayList<Complaint> getUnapprovedComplaints() throws SQLException{
		Statement statement = null;
		String isFAdminSQL = "SELECT * FROM COMPLAINT WHERE APPROVED = FALSE;";
		ArrayList<Complaint> r = new ArrayList<Complaint>();
		//cid, String c, long lNum, String desc
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(isFAdminSQL);
			while(rs.next()) {
				Complaint c = new Complaint(rs.getLong("CID"),rs.getString("COMPLAINANT"),rs.getLong("LISTINGNUM"),rs.getString("DESCRIPTION"));
				r.add(c);
			}
			rs.close();
			System.out.println(isFAdminSQL);
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return r;
	}
	
	public void approveComplaint(Long id) throws SQLException{
		Statement statement = null;
		String approvedComplaintSQL = "UPDATE COMPLAINT SET APPROVED = TRUE WHERE CID = " + id +";";

		//cid, String c, long lNum, String desc
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(approvedComplaintSQL);
			System.out.println(approvedComplaintSQL);
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public void rejectComplaint(Long id) throws SQLException{
		Statement statement = null;
		String rejectComplaintSQL = "DELETE FROM COMPLAINT WHERE CID = " + id +";";
		//cid, String c, long lNum, String desc
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(rejectComplaintSQL);
			System.out.println(rejectComplaintSQL);
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public Complaint getComplaintById(Long id) throws SQLException{
		Statement statement = null;
		String rejectComplaintSQL = "SELECT * FROM COMPLAINT WHERE CID = " + id +";";
		//cid, String c, long lNum, String desc
		Complaint c = null;
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(rejectComplaintSQL);
			if (rs.next()) {
				c = new Complaint(rs.getLong("CID"),rs.getString("COMPLAINANT"),rs.getLong("LISTINGNUM"),rs.getString("DESCRIPTION"));
			}
			rs.close();
			System.out.println(rejectComplaintSQL);
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return c;
	}
	
	public boolean verifyBankAccount(String accNum, String bsb) throws SQLException{
		Statement statement = null;
		String verifyBankAccountSQL = "SELECT * FROM BANKACCOUNT WHERE ACCOUNTNUMBER = \'" + accNum + "\' AND BSB = \'" + bsb + "\'"+";";
		//cid, String c, long lNum, String desc
		boolean exists = false;
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(verifyBankAccountSQL);
			if (rs.next()) {
				exists = true;
			}
			rs.close();
			System.out.println(verifyBankAccountSQL);
		} catch (SQLException e) {
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return exists;
	}
	
	public boolean booked(JSONArray availDates, Long listingNumber) throws SQLException{
		Statement statement = null;
		Statement statement2 = null;
		String from = availDates.getString(0);
		System.out.println("In booked jdbc");
		String to = availDates.getString(availDates.length()-1);
		String bookedSQL = "SELECT * FROM TRANSACTION WHERE LISTINGNUM = " + listingNumber + " AND (TODATE < STR_TO_DATE(\'"
								+ from + "\',\'%d-%m-%Y\') OR FROMDATE > STR_TO_DATE(\'"+ to + "\', \'%d-%m-%Y\') ) GROUP BY LISTINGNUM "
										+ "HAVING COUNT(*) = (SELECT COUNT(*) FROM TRANSACTION WHERE LISTINGNUM = "+listingNumber +");";
		String ifEmptysQL = "SELECT * FROM TRANSACTION WHERE LISTINGNUM = " + listingNumber + ";";
		//cid, String c, long lNum, String desc
		boolean booked = true;
		try {
			System.out.println(bookedSQL);
			connect();
			statement = dbConnection.createStatement();
			statement2 = dbConnection.createStatement();
			System.out.println("In booked jdbc 2");
			ResultSet rs = statement.executeQuery(bookedSQL);
			ResultSet rs2 = statement2.executeQuery(ifEmptysQL);
			System.out.println("In booked jdbc 3");
			if (rs.next() || !rs2.next()) {
				booked = false;
			}
			rs.close();
			System.out.println("In update jdbc");
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (statement2 != null) {
				statement2.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return booked;
	}
	
	public ArrayList<String> getConnectedUsers (String username) throws SQLException{
		Statement statement = null;
		ArrayList<String> usernames = new ArrayList<String>();
		String getConnectedUsersSQL = "SELECT SENDER FROM MESSAGE WHERE RECEIVER = \'" + username + "\' UNION " 
				+ "SELECT RECEIVER FROM MESSAGE WHERE SENDER = \'" + username +"\';";
		//cid, String c, long lNum, String desc
		boolean booked = true;
		try {
			System.out.println(getConnectedUsersSQL);
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(getConnectedUsersSQL);
			while (rs.next()) {
				usernames.add(rs.getString(1));
			}
			rs.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		return usernames;
	}
	private void connect() {
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}

		try {

			dbConnection = DriverManager.getConnection(	DB_CONNECTION, DB_USER, DB_PASSWORD);

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}
	public Connection getDBConnection() {
		return dbConnection;
	}
	
	
	public String escapeMetaCharacters(String inputString){
	    final String[] metaCharacters = {"\\","'"};
	    String outputString="";
	    boolean containsMeta = false;
	    for (int i = 0 ; i < metaCharacters.length ; i++){
	        if(inputString.contains(metaCharacters[i])){
	            outputString = inputString.replace(metaCharacters[i],"\\"+metaCharacters[i]);
	            inputString = outputString;
	            containsMeta = true;
	        }
	    }
	    if (!containsMeta) {
	    	outputString = inputString;
	    }
	    return outputString;
	}
}
