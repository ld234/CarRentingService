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
		
		post("/approve/:reqNum",(request,response) -> {
			response.type("application/json");
			StandardResponse res = approveListing(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res.getData());
		});
		
		delete("/reject/:requester/:listingNum",(request,response) -> {
			response.type("application/json");
			StandardResponse res = rejectListing(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res.getData());
		});
	}
	
	private StandardResponse approveListing(Request request) {
		
		return null;
	}
	
	private StandardResponse rejectListing(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String owner = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			owner = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		String requester = request.params(":requester");
		Long listingNum = Long.parseLong(request.params(":listingNum"));
		System.out.println(requester + listingNum); 
		try {
			jc.deleteBookingRequest(requester,listingNum);
			//Notification
			jc.insertNotification(new Notification("rejectBooking",owner + " has rejected your booking request on car listing " + listingNum,requester));
			OwnerSession os = (OwnerSession) UserController.connectedUsers.get(owner);
			((CarOwner) os.getUser()).rejectRequest(listingNum, requester);
			//UserController.connectedUsers.put(owner, os);
			
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(500);
		}
		
		
		return null;
	}
	
	private StandardResponse requestCarListing(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String renter = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			renter = new JSONObject((String)verifyRes.getData()).getString("subject");
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
