package car_service;

import static spark.Spark.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

import spark.Request;

public class BookingController {
	private JDBCConnector jc;
	private UserController uc;
	
	public BookingController(JDBCConnector jc, UserController uc) {
		this.uc = uc;
		this.jc = jc;
		post("/request", (request,response)->{
			StandardResponse res = requestCarListing(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			return JsonUtil.toJson(res);
		});
	}
	
	private StandardResponse requestCarListing(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String renter = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			renter = new JSONObject(verifyRes.getData()).getString("subject");
		}
		JSONObject jsObj = new JSONObject(request.body());
		String from = jsObj.getString("from");
		String to = jsObj.getString("to");
		Long listingNum = jsObj.getLong("listingNumber");
		BookingRequest br = new BookingRequest(LocalDate.parse(from,DateTimeFormatter.ofPattern("dd-MM-yyyy")),
				LocalDate.parse(to,DateTimeFormatter.ofPattern("dd-MM-yyyy")),renter,listingNum.longValue());
		// WRITE TO DATABASE
		try {
			Notification n = new Notification("newBooking",renter + " has requested to rent car listing " + listingNum, jc.getListingOwner(listingNum));
			jc.insertBookingRequest(br,n);
			jc.insertNotification(n);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//NOTIF?
		
		return new StandardResponse(200);
	}
}
