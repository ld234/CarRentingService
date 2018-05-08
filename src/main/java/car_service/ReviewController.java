package car_service;

import static spark.Spark.*;

import java.sql.SQLException;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

import spark.Request;

public class ReviewController {
	private JDBCConnector jc;
	private UserController uc;
	
	public ReviewController(JDBCConnector jc, UserController uc) {
		this.uc = uc;
		this.jc = jc;
		
		// Get all ratings and reviews of a car listing
		get("/review/:listingNum" , (request,response) ->{
			StandardResponse res = getReviews(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			if (res.getStatusCode() == 200)
				return JsonUtil.toJson2(res.getData());
			return JsonUtil.toJson(res);
		});
		
		// Add ratings and review for a car listing
		post("/review/:listingNum" , (request,response) ->{
			StandardResponse res = leaveReview(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			if (res.getStatusCode() == 200)
				return JsonUtil.toJson2(res.getData());
			return JsonUtil.toJson(res);
		});
	}
	
	private StandardResponse getReviews(Request request) {
		Long listingNum = Long.parseLong(request.params(":listingNum"));
		ArrayList<Review> reviewList = new ArrayList<Review>();
		try {
			reviewList = jc.getReviewsByListing(listingNum);
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(400, "Cannot get reviews for listing " + listingNum);
		}
		return new StandardResponse(200,reviewList,true);
	}
	
	private StandardResponse leaveReview (Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String username = null;
		Review newReview = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			username = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		JSONObject jsObj = new JSONObject(request.body());

		if (!fieldsRequiredExist(jsObj)) {
			return new StandardResponse(400,"Cannot save the review");
		}
		else {
			String rM = jsObj.getString("reviewMessage");
			int rating = jsObj.getInt("rating");
			Long listingNum = Long.parseLong(request.params(":listingNum"));
			newReview = new Review(listingNum,username,rating,rM);
			try {
				jc.insertReview(newReview);
				jc.insertNotification(new Notification("newReview", username + " left a review on car listing " + listingNum,jc.getListingOwner(listingNum)));
			}
			catch (SQLException e) {
				e.printStackTrace();
				return new StandardResponse(400,"Invalid message");
			}
		}
		return new StandardResponse(200,newReview,true);
	}
	
	private boolean fieldsRequiredExist(JSONObject jsonObj) {
		try {
			jsonObj.getString("reviewMessage");
			jsonObj.getInt("rating");
			//jsonObj.getLong("listingNumber");	
		}
		catch(JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
