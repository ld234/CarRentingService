package car_service;
import static spark.Spark.*;

public class Server {

	public static void main(String[] argv) {
		port(8080);
		JDBCConnector jc = new JDBCConnector();
		new UserController(jc);
	}
}
 