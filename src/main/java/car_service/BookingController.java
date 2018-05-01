package car_service;

import static spark.Spark.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

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
		
		post("/approve/:requester/:listingNum",(request,response) -> {
			response.type("application/json");
			StandardResponse res = approveListing(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res.getData());
		});
		
		delete("/reject/:requester/:listingNum",(request,response) -> {
			response.type("application/json");
			StandardResponse res = rejectListing(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res);
		});
	}
	
	private StandardResponse approveListing(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String owner = null;
		String requester = request.params(":requester");
		Long listingNum = Long.parseLong(request.params(":listingNum"));
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			owner = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		if (uc.getSession(owner) instanceof OwnerSession && uc.getSession(owner) != null) {
			if(!((CarOwner)((OwnerSession)uc.getSession(owner)).getUser()).getCarListingList().containsKey(listingNum))
				return new StandardResponse(401,"Not authorised");
		}  
		else {
			return new StandardResponse(401,"Not authorised");
		}
		try {
			// add new booking to database and to session
			jc.addBooking(requester,listingNum);
			// delete booking request
			jc.deleteBookingRequest(requester, listingNum);
			// add transaction history
//			jc.insertTransaction();
			
			// add to unavailable dates of listing and to session
			
			// deduct bank account and add to bank account of owner
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(500);
		}
		BookingRequest brResult;
		try {
			brResult = jc.getBookingRequest(requester, listingNum);
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(500);
		}
		return new StandardResponse(200,brResult,true);
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
		if (uc.getSession(owner) instanceof OwnerSession && uc.getSession(owner) != null) {
			if(!((CarOwner)((OwnerSession)uc.getSession(owner)).getUser()).getCarListingList().containsKey(listingNum))
				return new StandardResponse(401,"Not authorised");
//			uc.getSession(owner).getUser();
//			Collection <BookingRequest> arr = ((CarOwner)((OwnerSession)uc.getSession(owner)).getUser()).getBookingRequestList();
//			for (BookingRequest p:arr) {
//				System.out.println(p.getListingNumber() + " "+ p.getRenter());
//			}
		}  
		else {
			return new StandardResponse(401,"Not authorised");
		}
		try {
			jc.deleteBookingRequest(requester,listingNum);
			//Notification
			jc.insertNotification(new Notification("rejectedBooking",owner + " has rejected your booking request on car listing " + listingNum,requester));
			OwnerSession os = (OwnerSession) uc.getSession(owner);
			((CarOwner) os.getUser()).rejectRequest(listingNum, requester);
			//UserController.connectedUsers.put(owner, os);
			
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(500);
		}
		
		
		return new StandardResponse(200);
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
