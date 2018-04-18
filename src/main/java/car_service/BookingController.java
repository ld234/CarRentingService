package car_service;

import static spark.Spark.*;

import spark.Request;

public class BookingController {
	
	public BookingController() {
		post("/request", (request,response)->{
			StandardResponse res = requestCarListing(request);
			return JsonUtil.toJson(res);
		});
	}
	
	private StandardResponse requestCarListing(Request request) {
		return null;
	}
}
