package car_service;
import static spark.Spark.*;

public class Server {

	public static void main(String[] argv) {
		port(8080);
		new UserController();
	}
}
