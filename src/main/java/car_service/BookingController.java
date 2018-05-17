package car_service;

import static spark.Spark.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.json.JSONObject;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import spark.Request;

public class BookingController {
	private JDBCConnector jc;
	private UserController uc;
	
	public BookingController(JDBCConnector jc, UserController uc) {
		this.uc = uc;
		this.jc = jc;
		
		// Get booking requests by car owner
		get("/request" , (request,response)->{
			StandardResponse res = getBookingRequests(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			return JsonUtil.toJson(res.getData());
		});
		
		// Request to rent a car
		post("/request", (request,response)->{
			StandardResponse res = requestCarListing(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			return JsonUtil.toJson(res);
		});
		
		// Approve a booking request
		post("/approve/:requester/:listingNum",(request,response) -> {
			response.type("application/json");
			StandardResponse res = approveBooking(request);
			response.status(res.getStatusCode());
			if (res.getStatusCode() == 200)
				return JsonUtil.toJson(res.getData());
			else {
				return JsonUtil.toJson(res);
			}
		});
		
		// Reject a booking request
		delete("/reject/:requester/:listingNum",(request,response) -> {
			response.type("application/json");
			StandardResponse res = rejectBooking(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res);
		});
		
//		get("/transactions", (request,response) -> {
//			response.type("application/json");
//			StandardResponse res = getTransactions(request);
//			response.status(res.getStatusCode());
//			return JsonUtil.toJson(res.getData());
//		});
		
		// View booking transactions
		get("/transactions", (request,response) -> {
			response.type("application/json");
			StandardResponse res = getTransactions(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res.getData());
		});
	}
	
	private StandardResponse getBookingRequests(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String owner = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			owner = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		JsonObject obj = new JsonObject();
		UserSession s = UserController.connectedUsers.get(owner);
		if (s == null) {
			UserController.connectedUsers.put(owner, uc.createSession(owner));
			s = UserController.connectedUsers.get(owner);
		}
		else {
			try {
				UserController.connectedUsers.get(owner).user = jc.getUser(owner);
			} catch (SQLException e) {
				e.printStackTrace();
				return new StandardResponse(500,"Cannot get booking requests");
			}
		}
		User thisUser = s.getUser();	
		obj.add("bookingRequests", new GsonBuilder().create().toJsonTree(((CarOwner) thisUser).getBookingRequestList()));
		return new StandardResponse(200,obj,true);
	}
	
	// When listing is approved
	private StandardResponse approveBooking(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String owner = null;
		BookingRequest brResult = null;
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
			LocalDate f = jc.getBookingRequest(requester, listingNum).getFrom();
			LocalDate t = jc.getBookingRequest(requester, listingNum).getTo();
			// add new booking to database and to session
			// jc.addBooking(requester,listingNum);
			//Notification
			jc.insertNotification(new Notification("acceptedBooking",owner + " has approved your booking request on car listing " + listingNum,requester));
			// add transaction history
			jc.insertTransaction(requester, listingNum);
			// add to unavailable dates of listing and to session
			((CarOwner)((OwnerSession)uc.getSession(owner)).getUser()).getCarListingList().get(listingNum)
								.bookCarListing(requester, f, t);
			// deduct bank account and add to bank account of owner, maybe some error checking LATER
			jc.conductTransaction(requester, listingNum);
			brResult = jc.getBookingRequest(requester, listingNum);
			// delete booking request
			jc.deleteBookingRequest(requester, listingNum);
			jc.deleteAvailability(listingNum,CarListing.getDatesBetween(f, t));
			
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(500);
		}
		return new StandardResponse(200,brResult,true);
	}
	
	private StandardResponse rejectBooking(Request request) {
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
		Long listingNumber = jsObj.getLong("listingNumber");
		BookingRequest br = new BookingRequest(LocalDate.parse(from,DateTimeFormatter.ofPattern("dd-MM-yyyy")),
				LocalDate.parse(to,DateTimeFormatter.ofPattern("dd-MM-yyyy")),renter,listingNumber.longValue());
		// WRITE TO DATABASE
		LocalDate f = LocalDate.parse(from, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		LocalDate t = LocalDate.parse(to, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		
		try {
			if(!((CarOwner)jc.getUser(jc.getListingOwner(listingNumber))).getCarListingList().get(listingNumber).isAvailable(f, t)) {
				System.out.println(JsonUtil.toJson(((CarOwner)jc.getUser(jc.getListingOwner(listingNumber))).getCarListingList().get(listingNumber)));
				return new StandardResponse(400,"Listing " + listingNumber + " is not available from "+  from  +" to " + to);
			}
			Notification n = new Notification("newBooking",renter + " has requested to rent car listing " + listingNumber, jc.getListingOwner(listingNumber));
			jc.insertBookingRequest(br,n);
			jc.insertNotification(n);
			
		} catch (SQLException e) {
//			e.printStackTrace();
			return new StandardResponse(400,"Cannot request to book this listing.");
		}
		
		return new StandardResponse(200);
	}
	
	public StandardResponse getTransactions(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String username = null;
		
		ArrayList<Transaction> transList = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			username = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		try {
			transList = jc.getTransactions(username);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new StandardResponse(200,transList,true);
	}
}
