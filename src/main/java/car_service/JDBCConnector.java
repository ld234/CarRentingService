package car_service;
import java.sql.*;
import java.util.HashMap;

public class JDBCConnector {

	private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/car_service";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "root";
	private Connection dbConnection = null;
	
	public JDBCConnector() {}
	
	// Query for existence of a username
	public boolean usernameExists (String username) throws SQLException {
		Statement statement = null;
		String findUserSQL = "SELECT USERNAME FROM USER WHERE USERNAME = \'" + username + "\';";
		boolean exists = false;
		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(findUserSQL);
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
				+ "STR_TO_DATE(\'" + user.getDOB().toString() + "\', '%d-%m-%Y')"
				+ ");";
		String insertUserSQL2 = "INSERT INTO CARRENTER VALUES (\""
				+ user.getUsername() + "\", \""
				+ user.getDriverLicense() + "\", \""
				+ user.getCardNumber()
				+ "\");";

		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(insertUserSQL);
			System.out.println(insertUserSQL2);
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
	
	// Query for login
	public boolean findUsernameAndPassword(String username, String password) throws SQLException{
		Statement statement = null;
		String findUserSQL = "SELECT USERNAME,PASSWORD FROM USER WHERE USERNAME = \'"
							+ username + "\' AND PASSWORD = \'"+ password + "\';";
		boolean exists = false;
		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(findUserSQL);
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
		boolean exists = false;
		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(findLicenseSQL);
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
			System.out.println(findLicenseSQL);
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
	
	public void updateUser(HashMap<String,String> hm, String username ) throws SQLException{
		Statement statement = null;
		int sz = hm.keySet().size();
		String updateUserSQL = "UPDATE USER SET ";
		String [] keys = new String[sz];
		keys = hm.keySet().toArray(keys);
		for (int i = 0; i <  sz; i++) {
				updateUserSQL+= keys[i]	+  " = \'" + hm.get(keys[i]) + "\'";
				if (i < sz) {
					updateUserSQL+= " AND ";
				}
				
		}
	updateUserSQL += "WHERE USERNAME = \'"+ username + "\';";

		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(updateUserSQL);
                        // execute the SQL statement
			statement.execute(updateUserSQL);
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
	
	public Connection getDBConnection() {
		return dbConnection;
	}

}
