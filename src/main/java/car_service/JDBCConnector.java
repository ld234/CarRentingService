package car_service;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.json.JSONObject;

public class JDBCConnector {
	public static Long listingCount; 
	private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/car_service?verifyServerCertificate=false&useSSL=true";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "root";
	private Connection dbConnection = null;
	
	public JDBCConnector() {
		try {
			listingCount = new Long(getListingCount());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		String insertUserSQL2 = "INSERT INTO CARRENTER VALUES (\""
				+ user.getUsername() + "\", \""
				+ user.getDriverLicense() + "\", \""
				+ user.getCardNumber()
				+ "\");";

		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
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
		String findCarOwnerSQL = "SELECT USERNAME FROM USER WHERE USERNAME = \'"
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
	public boolean findCreditCard(String cardNum, String cardholder, String expiryDate) throws SQLException{
		Statement statement = null;
		String findLicenseSQL = "SELECT CARDNUMBER FROM CREDITCARD " 
							+ "WHERE CARDNUMBER = \'"
							+ cardNum + "\' AND "
							+ "CARDHOLDER = \'"
							+ cardholder + "\' AND "
							+ "DATE_FORMAT(EXPIRYDATE,\'%d-%m-%Y\') = \'"
							+ expiryDate +  "\';";
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
		
		try {
			connect();
			statement = dbConnection.createStatement();
                        // execute the SQL statement
			ResultSet rs = statement.executeQuery(findUserSQL);
			rs.next();
			CreditCard cc = getCreditCard(rs.getString("CARDNUMBER"));
			CarRenter cr = new CarRenter(rs.getString(1),rs.getString("PASSWORD"),rs.getString("FIRSTNAME"),rs.getString("LASTNAME"),rs.getString("LICENSENUM"), rs.getString("DOB"),cc);
			if (!findCarOwner(username)) {
				return cr;
			}
			else {
				return new CarOwner(cr,getCarListings(cr.getUsername()));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
			return new CreditCard(rs.getString("CARDNUMBER"),rs.getString("CARDHOLDER"),new SimpleDateFormat("yyyy-mm-dd").parse(rs.getString("EXPIRYDATE")));
		} catch (SQLException e) {
			return null;
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
						                                         rs.getDouble("ODOMETER"),rs.getString("IMAGEPATH")));
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
	public Connection getDBConnection() {
		return dbConnection;
	}

}
