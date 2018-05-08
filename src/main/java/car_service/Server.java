package car_service;
import static spark.Spark.*;

import java.io.File;
import java.util.concurrent.*;

public class Server {

	public static void main(String[] argv) throws InterruptedException, ExecutionException {
		port(8080);
		File theDir = new File("listingImg");
		theDir.mkdir();
		staticFiles.externalLocation(System.getProperty("user.dir")+File.separator+"listingImg");
		JDBCConnector jc = new JDBCConnector();
		UserController uc = new UserController(jc);
		new ListingController(jc, uc);
		new BookingController(jc, uc);
		new ReviewController(jc, uc);
	}
	
	
}
 
