package car_service;
import static spark.Spark.*;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.File;
import java.util.concurrent.*;

public class Server {

	public static void main(String[] argv) throws InterruptedException, ExecutionException {
		//getAllMethods();
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
	
	public static void getAllMethods() {
		Method [] methods = JDBCConnector.class.getDeclaredMethods();
		for (Method m : methods) {
			System.out.println(m);
		}
	}
}


 
