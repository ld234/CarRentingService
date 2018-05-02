package car_service;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

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
			if (rs.next()) {
				carPrices.put(rs.getString("BRAND"), rs.getDouble("RATE"));
			}
			rs.close();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
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

		} catch (SQLException e) {
			System.out.println(e.getMessage());
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

		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
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

		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
		for (int i = 0; i < sz; i++) {
				updateUserSQL+= keys[i]	+  " = \'" + jsObj.get(keys[i]) + "\'";
				if (i < sz-1) {
					updateUserSQL+= ", ";
				}
		}
		updateUserSQL += " WHERE USERNAME = \'"+ username + "\';";

		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(updateUserSQL);
                        // execute the SQL statement
			statement.execute(updateUserSQL);
			System.out.println("Password updated");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
			ResultSet rs = statement.executeQuery(findUserSQL);
			rs.next();
			
			CreditCard cc = getCreditCard(rs.getString("CARDNUMBER"));
			
			CarRenter cr = new CarRenter(rs.getString(1),rs.getString("PASSWORD"),rs.getString("FIRSTNAME"),
					rs.getString("LASTNAME"),rs.getString("LICENSENUM"), 
					new SimpleDateFormat(User.DATE_FORMAT).format(new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("DOB"))),cc,notifList,
					rs.getString("SOCIALMEDIALINK"));
			rs.close();
			if (!findCarOwner(username)) {
				return cr;
			}
			else {
				String selectOwnerSQL = "SELECT * FROM CAROWNER WHERE USERNAME = \'" +username + "\';";
				rs = statement.executeQuery(selectOwnerSQL);
				rs.next();
				return new CarOwner(cr,getCarListings(cr.getUsername()),getBookingRequestsByOwner(cr.getUsername()),rs.getString("BSB"),rs.getString("ACCOUNTNUMBER"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}finally {
			if (statement != null) {
				statement.close();
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
		}

	}
	
	// Get the number of listings
	public long getListingCount() throws SQLException{
		Statement statement = null;
		String getListingCountSQL = "SELECT COUNT(*) FROM LISTING;";
		long count = 0;
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			ResultSet rs = statement.executeQuery(getListingCountSQL);
			rs.next();
			count = rs.getLong(1);

		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
		String getListingsSQL = "SELECT * FROM LISTING WHERE OWNER = \'"+ carOwner +"\';";
		HashMap<Long,CarListing> cls = new HashMap<Long,CarListing>();
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
						                                         rs.getString("OWNER")));
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
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
		String findRegoExist = "SELECT REGO FROM LISTING " 
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
	public void addListing(String owner, CarListing cl) throws SQLException {
		Statement statement = null;

		String insertListingSQL = "INSERT INTO LISTING VALUES ("
				+ cl.getListingNumber() +  ", \'"
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
			System.out.println("New listing created");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
	
	// Update car listing
	public void updateListing(JSONObject jsObj, long listingNum) throws SQLException{
		Statement statement = null;
		int sz = jsObj.keySet().size();
		String updateListingSQL = "UPDATE LISTING SET ";
		String [] keys = new String[sz];
		keys = jsObj.keySet().toArray(keys);
		System.out.println(keys[0]);
		for (int i = 0; i < sz; i++) {
			if (keys[i].equals("odometer") || keys[i].equals("capacity"))
				updateListingSQL += keys[i] + " = " +jsObj.get(keys[i]);
			else 
				updateListingSQL+= keys[i]	+  " = \'" + jsObj.get(keys[i]) + "\'";
			if (i < sz-1) {
				updateListingSQL+= ", ";
			}
		}
		updateListingSQL += " WHERE LISTINGNUM = "+ listingNum + ";";

		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(updateListingSQL);
                        // execute the SQL statement
			statement.execute(updateListingSQL);
			System.out.println("Listing updated");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public void deleteListing(long listingNum) throws SQLException{
		Statement statement = null;
		String deleteListingSQL = "DELETE FROM LISTING "
		+ " WHERE LISTINGNUM = "+ listingNum + ";";

		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(deleteListingSQL);
                        // execute the SQL statement
			statement.execute(deleteListingSQL);
			System.out.println("Listing DELETED");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
		String getNotifCountSQL = "SELECT COUNT(*) FROM NOTIFICATION;";
		int count = -1;
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs= statement.executeQuery(getNotifCountSQL);
			
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
		String getListingOwnerSQL = "SELECT * FROM LISTING WHERE LISTINGNUM = " + listingNumber.longValue() + ";";
		CarListing listing = null;
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs= statement.executeQuery(getListingOwnerSQL);
			
			if (rs.next()) {
				listing = new CarListing(rs.getLong("LISTINGNUM"),
						 rs.getString("REGO"),
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
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
	
	public ArrayList<CarListing> searchCarListing(HashMap<String,String> criteria) throws SQLException{
		ArrayList<CarListing> result = new ArrayList<CarListing>();
		Statement statement = null;
		String from = criteria.remove("from");
		String to  = criteria.remove("to");
		int sz = criteria.size();
		String searchCarListingSQL = "SELECT L.LISTINGNUM,REGO, BRAND, MODEL,LOCATION,COLOUR,TRANSMISSION,YEAR, CAPACITY, ODOMETER,OWNER,IMAGEPATH"
									+ " FROM BOOKING B RIGHT OUTER JOIN LISTING L ON B.LISTINGNUM = L.LISTINGNUM WHERE "+
									"( FROMDATE > STR_TO_DATE(\'" + to + "\', '%d-%m-%Y') OR "+
									"TODATE < STR_TO_DATE(\'" + from + "\', '%d-%m-%Y')) AND ";
		
		String [] keys = new String[sz];
		keys = criteria.keySet().toArray(keys);
		System.out.println(keys[0]);
		for (int i = 0; i < sz; i++) {
			if (keys[i].toLowerCase().equals("capacity"))
				searchCarListingSQL += keys[i] + " = " + criteria.get(keys[i]);
			else if (keys[i].toLowerCase().equals("transmission") || keys[i].toLowerCase().equals("brand") )
				searchCarListingSQL += keys[i]	+  " = \'" + criteria.get(keys[i]) + "\'";
			else if (keys[i].toLowerCase().equals("location") )
				searchCarListingSQL += keys[i]	+  " LIKE \'%" + criteria.get(keys[i]) + "%\'";
			else if (keys[i].toLowerCase().equals("from") )
				searchCarListingSQL += keys[i]	+  " " + criteria.get(keys[i]) + "%\'";
			else if (keys[i].toLowerCase().equals("to") )
			if (i < sz-1) {
				searchCarListingSQL += " AND ";
			}
		}
		searchCarListingSQL += " UNION SELECT * FROM LISTING WHERE LISTINGNUM NOT IN (SELECT LISTINGNUM FROM BOOKING) ";
		searchCarListingSQL +=  ";";
		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(searchCarListingSQL);
                        // execute the SQL statementS
			ResultSet rs = statement.executeQuery(searchCarListingSQL);
			while (rs.next()) {
				result.add(new CarListing(rs.getLong("LISTINGNUM"),
						 rs.getString("REGO"),
                         rs.getString("BRAND"),
                         rs.getString("MODEL"),
                         rs.getString("LOCATION"),
                         rs.getString("COLOUR"),
                         rs.getString("TRANSMISSION"),
                         rs.getInt("YEAR"),
                         rs.getInt("CAPACITY"),
                         rs.getDouble("ODOMETER"),rs.getString("IMAGEPATH"),
                         rs.getString("OWNER")));
			}
			System.out.println("Listing searched");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
	
	public void deleteBookingRequest(String requester, Long listingNum) throws SQLException {
		Statement statement = null;
		String getListingOwnerSQL = "DELETE FROM BOOKINGREQUEST WHERE LISTINGNUM = " + listingNum.longValue() + " AND REQUESTER = \'" + requester +"\';";
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(getListingOwnerSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
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
	
	public BookingRequest addBooking (String requester, Long listingNum) throws SQLException{
		Statement statement = null;
		BookingRequest br = this.getBookingRequest(requester, listingNum);
		String addBookingSQL = "INSERT INTO BOOKING (SELECT * FROM BOOKINGREQUEST WHERE REQUESTER = \'"
				+ requester + "\' AND LISTINGNUM = " + listingNum + ");";
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(addBookingSQL);
		} catch (SQLException e) {
			e.getStackTrace();
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
	
	
	public BookingRequest insertTransaction (String requester, Long listingNum) throws SQLException{
		Statement statement = null;
		BookingRequest br = this.getBookingRequest(requester, listingNum);
		String insertTransactionSQL = "INSERT INTO TRANSACTION VALUES (" 
								+ br.getListingNumber() + ", STR_TO_DATE(\'"
								+ br.getFrom().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "\','%d-%m-%Y'), STR_TO_DATE(\'"
								+ br.getTo().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "\','%d-%m-%Y'), \'"
								+ br.getRenter() + "\', \'"
								+ this.getListingOwner(listingNum.longValue()) + "\',"
								+ br.getPrice()+");";
		System.out.println(insertTransactionSQL);
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(insertTransactionSQL);
			System.out.println(insertTransactionSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
	
	public void conductTransaction(String requester, Long listingNum) throws SQLException{
		Statement statement = null;
		BookingRequest br = this.getBookingRequest(requester, listingNum);
		double amount = br.getPrice();
		CarOwner co = (CarOwner) this.getUser(this.getListingOwner(listingNum));
		CarRenter cr = (CarRenter) this.getUser(requester);
		String deductCreditCardBalanceSQL = "UPDATE CREDITCARD SET BALANCE = BALANCE - " + amount 
											+ " WHERE CARDNUMBER = \'" + cr.getCardNumber() + "\';";
		String increaseAccBalanceSQL = "UPDATE BANKACCOUNT SET BALANCE = BALANCE + " + amount 
										+ " WHERE BSB = \'"+ co.getBSB()+ "\' AND ACCOUNTNUMBER = \'" + co.getAccountNumber() + "\';" ;
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(deductCreditCardBalanceSQL);
			System.out.println(deductCreditCardBalanceSQL);
			statement.execute(increaseAccBalanceSQL);
			System.out.println(increaseAccBalanceSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public ArrayList<Message> getMessageSent(String username) throws SQLException{
		Statement statement = null;
		ArrayList<Message> result = new ArrayList<Message>();
		String getMessageSentSQL = "SELECT * FROM MESSAGE WHERE SENDER = \'" + username +"\';";
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(getMessageSentSQL);
			System.out.println(getMessageSentSQL);
			
			if (rs.next()) {
				result.add(new Message(rs.getString("SENDER"),rs.getString("RECEIVER"),rs.getString("MESSAGE"),rs.getLong("TSTAMP")));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
	
	public ArrayList<Message> getMessageReceived(String username) throws SQLException{
		Statement statement = null;
		ArrayList<Message> result = new ArrayList<Message>();
		String getMessageReceivedSQL = "SELECT * FROM MESSAGE WHERE RECEIVER = \'" + username +"\';";
		try {
			connect();
			statement = dbConnection.createStatement();
			ResultSet rs = statement.executeQuery(getMessageReceivedSQL);
			System.out.println(getMessageReceivedSQL);
			
			if (rs.next()) {
				result.add(new Message(rs.getString("SENDER"),rs.getString("RECEIVER"),rs.getString("MESSAGE"),rs.getLong("TSTAMP")));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
									+ m.getMessage() +"\', " 
									+ m.getTimestamp().getTime() +");";
		try {
			connect();
			statement = dbConnection.createStatement();
			statement.execute(insertMessageSQL);
			System.out.println(insertMessageSQL);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
	}
	
	public Connection getDBConnection() {
		return dbConnection;
	}

}
