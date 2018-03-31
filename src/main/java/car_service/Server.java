package car_service;
import static spark.Spark.*;

import java.util.concurrent.*;

public class Server {

	public static void main(String[] argv) throws InterruptedException, ExecutionException {
		
		port(8080);
		
		JDBCConnector jc = new JDBCConnector();
		new UserController(jc);
		
		
	}
	
	
}
 
