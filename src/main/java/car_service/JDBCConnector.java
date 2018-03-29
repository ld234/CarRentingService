package car_service;
import java.sql.*;

public class JDBCConnector {

	private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/car_service";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "root";
	private Connection dbConnection = null;
	
	public JDBCConnector() {}
	
/*	public void createDbUserTable() throws SQLException {
		
		Statement statement = null;
		String createTableSQL = "CREATE TABLE USER("
				+ "USERNAME VARCHAR(20) NOT NULL, "
				+ "PASSWORD VARCHAR(100) NOT NULL, "
				+ "FNAME VARCHAR(20) NOT NULL, " 
				+ "LNAME VARCHAR(20) NOT NULL,"
				+ "CONSTRAINT USER_PK PRIMARY KEY (USERNAME) "
				+ ")";

		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(createTableSQL);
                        // execute the SQL statement
			statement.execute(createTableSQL);
			System.out.println("Table \"user\" is created!");

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
	}*/
	
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

	public void insertUser(User user) throws SQLException {
		Statement statement = null;

		String insertUserSQL = "INSERT INTO USER VALUES (\""
				+ user.getUsername() + "\", \""
				+ user.getPassword() + "\", \""
				+ user.getFirstName() + "\", \""
				+ user.getLastName()
				+ "\")";

		try {
			connect();
			statement = dbConnection.createStatement();
			System.out.println(insertUserSQL);
                        // execute the SQL statement
			statement.execute(insertUserSQL);
			System.out.println("Table \"user\" is created!");

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
