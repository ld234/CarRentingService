   package car_service;

import spark.Request;
import spark.utils.IOUtils;

import static spark.Spark.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;

public class ListingController {
	private JDBCConnector jc;
	private UserController uc;
	
	public ListingController(JDBCConnector jc, UserController uc) {
		this.jc = jc;
		this.uc= uc;
		
		// Post a new car listing
		post("/list", (request,response) -> {
			response.type("application/json");
			StandardResponse res = createListing(request);
			response.status(res.getStatusCode());
			if (res.getStatusCode() == 200) {
				JsonObject obj = new JsonObject();
				obj.addProperty("statusCode", 200);
				obj.addProperty("listingNumber", (Long)res.getData());
				return JsonUtil.toJson(obj);
			}
			return JsonUtil.toJson(res);
		});
		
		// Add a new car to account
		post("/car", (request,response) -> {
			response.type("application/json");
			StandardResponse res = addNewCar(request);
			response.status(res.getStatusCode());
			if (res.getStatusCode() == 200) 
				return res.getData().toString();
			return JsonUtil.toJson(res);
		});
		
		// Edit car listing
		put("/list/:listingNum", (request,response) -> {
			response.type("application/json");
			StandardResponse res = editListing(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res);
		});
		
		// Add more avail dates - Not Done
		put("/avail/:listingNum",(request,response) -> {
			response.type("application/json");
			StandardResponse res = editListing(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res);
		});
		
		// Remove avail dates - Not Done
		delete("/avail/:listingNum",(request,response) -> {
			response.type("application/json");
			StandardResponse res = editListing(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res);
		});
		
		// Delete listing with listingNum
		delete("/list/:listingNum", (request,response) -> {
			response.type("application/json");
			StandardResponse res = deleteListing(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res);
		});
		
		// Get info of a listing by listing number
		get("/list/:listingNum",(request,response) -> {
			response.type("application/json");
			StandardResponse res = getListing(request);
			response.status(res.getStatusCode());
			if (res.getStatusCode() == 200)
				return JsonUtil.toJson(res.getData());
			return JsonUtil.toJson(res);
		});
		
		// Get listings by owner
		get("/list",(request,response) -> {
			response.type("application/json");
			StandardResponse res = getListingsByOwner(request);
			response.status(res.getStatusCode());
			if (res.getStatusCode() == 200)
				return JsonUtil.toJson(res.getData());
			return JsonUtil.toJson(res);
		});
		
		// Get car info
		get("/car/:rego",(request,response) -> {
			response.type("application/json");
			StandardResponse res = getCar(request);
			response.status(res.getStatusCode());
			if (res.getStatusCode() == 200)
				return JsonUtil.toJson(res.getData());
			else
				return JsonUtil.toJson(res);
		});
		
		// Get cars by car owner
		get("/car",(request,response) -> {
			response.type("application/json");
			StandardResponse res = getCarByOwner(request);
			response.status(res.getStatusCode());
			if (res.getStatusCode() == 200)
				return JsonUtil.toJson(res.getData());
			else
				return JsonUtil.toJson(res);
		});
		
		// Search car listing
		get("/search",(request,response) -> {
			response.type("application/json");
			StandardResponse res = search(request);
			response.status(res.getStatusCode());
			if (res.getStatusCode() == 200)
				return JsonUtil.toJson(res.getData());
			return JsonUtil.toJson(res);
		});
		
		// Become a car owner
		put("/upgrade",(request,response)->{
			response.type("application/json");
			StandardResponse res = upgrade(request);
			response.status(res.getStatusCode());
			if (res.getStatusCode() == 200)
				return res.getData().toString();
			return JsonUtil.toJson(res);
		});
		
		
		/*post("/carimg", (request,response) -> {
			this.handleUpload(request);
			response.type("application/json");
			try {
				listingNum = this.handleUpload(request).split(".")[0];
			} catch (IOException | ServletException e1) {
				return JsonUtil.toJson(new StandardResponse(400,"Cannot upload image."));
			}
			return JsonUtil.toJson(new StandardResponse(200,listingNum));
		});*/
	}
	
	private StandardResponse getCarByOwner (Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String owner;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			owner = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		ArrayList<Car> result = new ArrayList<Car>();
		try {
			result = jc.getCarByOwner(owner);
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(400,"Cannot get listings by owner");
		}
		return new StandardResponse(200,result,true);
	}
	
	private StandardResponse getListingsByOwner(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String owner;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			owner = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		ArrayList<CarListing> result = new ArrayList<CarListing>();
		HashMap<Long, CarListing> carListings = null;
		try {
			carListings = jc.getCarListings(owner);
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(400,"Cannot get listings by owner");
		}
		for (Long l : carListings.keySet()) {
			result.add(carListings.get(l));
		}
		return new StandardResponse(200,result,true);
	}
	
	
	private StandardResponse getCar(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String rego = request.params(":rego");
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}

		Car c = null;
		
		try {
			c = jc.getCar(rego);
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(400, "Failed to get car info");
		}
		return new StandardResponse(200,c,true);
	}
	
	private StandardResponse upgrade(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String renter = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			renter = new JSONObject((String)verifyRes.getData()).getString("subject");
		}	
		
		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(System.getProperty("user.dir")+File.separator+"listingImg"));
		String req = request.raw().getParameter("data");
		if (req == null){
			return new StandardResponse(400,"Cannot upgrade account");
		}
		JSONObject jsObj = new JSONObject(req);
		
		try {
			jc.upgradeAccount(renter,jsObj.getString("accountNumber"), jsObj.getString("bsb"));
			
		} catch (JSONException e) {
			e.printStackTrace();
			return new StandardResponse(400,"Invalid fields");
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(400,"Cannot upgrade account");
		} catch (NullPointerException e) {
			e.printStackTrace();
			return new StandardResponse(400,"Null pointer exception");
		}
		// NULL
		
		StandardResponse addCarResponse = this.addNewCar(request);
		if (addCarResponse.getStatusCode() != 200) {
			return addCarResponse;
		}
		JSONObject jsonObj = (JSONObject) addCarResponse.getData();
		if (jsonObj == null) {
			System.out.println("NULL jsonObject");
		}
		
		
		return new StandardResponse(200,jsonObj,true);
	}
	
	private StandardResponse addNewCar(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String json="";
		String owner ="";
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			owner = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(System.getProperty("user.dir")+File.separator+"listingImg"));
		json = request.raw().getParameter("data");
		if (json == null) {
			System.out.println("Cannot get data 2");
			return new StandardResponse(400, "Insufficient details");
		}
		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(json);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("ERROR in add Car");
			return new StandardResponse(400);
		}
		if (!fieldsRequiredExist(jsonObj)) {
			System.out.println("Missing fields");
			return new StandardResponse(400, "Missing fields");
		}
		else if(!verifyListing(jsonObj.getString("rego"),owner)) {
			System.out.println("Invalid car.");
			return new StandardResponse(400, "Invalid car.");
		}
		try {
			String imgPath = handleUpload(request,owner);
			jsonObj.put("img", imgPath);
			jc.addCar(owner, new Car(jsonObj.getString("rego"),jsonObj.getString("brand"),jsonObj.getString("model"),
					jsonObj.getString("location"),jsonObj.getString("colour"),jsonObj.getString("transmission"),jsonObj.getInt("year"),
					jsonObj.getInt("capacity"),jsonObj.getInt("odometer"),jsonObj.getString("img"),owner));
		} catch (IOException | ServletException | JSONException | NullPointerException| SQLException e1) {
			e1.printStackTrace();
			System.out.println("SUCKS");
			return new StandardResponse(400, "Cannot create new car");
		}
		return new StandardResponse(200,jsonObj,true);
	}
	
	private StandardResponse search (Request request) {
		HashMap<String,String> criteria = new HashMap<String,String>();
		for (String key : request.queryParams()) {
			criteria.put(key, request.queryParams(key));
		}
		ArrayList<CarListing> data;
		try {
			data = jc.searchCarListing(criteria);
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(500);
		} catch (ArrayIndexOutOfBoundsException e) {
			return new StandardResponse(400, "Insufficient search criteria.");
		}
		return new StandardResponse(200,data,true);
		
	}
	
	private StandardResponse getListing(Request request) {
		Long listingNum = Long.parseLong(request.params(":listingNum"));
		CarListing cl = null;
		try {
			cl = jc.getListing(listingNum);
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(500);
		}
		return new StandardResponse(200,cl,true);
	}
	
	StandardResponse createListing (Request request) {
		String owner ="";
		StandardResponse verifyRes = uc.verify(request);
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			owner = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		
		JSONObject jsonObj = new JSONObject(request.body());
		JDBCConnector.listingCount += 1;
		long listingNum = JDBCConnector.listingCount;		              
		jsonObj.put("listingNumber", listingNum);
		System.out.println(listingNum);
		try {
			String rego = jsonObj.getString("rego");
			HashSet <LocalDate> availDates = new HashSet<LocalDate>();
			JSONArray arr = jsonObj.getJSONArray("availableDates");
			if (arr != null) {
				for (Object s: arr) {
					availDates.add(LocalDate.parse((String)s, DateTimeFormatter.ofPattern("dd-MM-yyyy")));
				}
			}
			else {
				availDates.add(null);
			}
			CarListing cl = new CarListing(listingNum, rego, availDates);
			jc.addListing(owner,cl);
			((OwnerSession) uc.getSession(owner)).addCarListingToSession(cl);
		} catch (JsonSyntaxException | SQLException e) {
			System.out.println(e.getMessage());	
			return new StandardResponse(400,"Cannot create new listing");
		} catch(JSONException e) {
			return new StandardResponse(400,"Missing fields");
		}
		return new StandardResponse(200,jsonObj.getLong("listingNumber"),true);
	}
	
	private StandardResponse editListing(Request request) {
		long listingNum = Long.parseLong(request.params(":listingNum"));
		JSONObject jsonObj = new JSONObject(request.body());
		String owner ="";
		StandardResponse verifyRes = uc.verify(request);
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			owner = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		if (!valuesInList(jsonObj)) {
			return new StandardResponse(400,"Cannot update listing");
		}
		try {
			if (!jc.isOwnerListing(owner,listingNum)) {
				return new StandardResponse(400,"Cannot update listing");
			}
			jc.updateListing(jsonObj, listingNum);
			((OwnerSession) uc.getSession(owner)).updateCarListingInSession(jc.getCarListings(owner).get(listingNum));
		} catch (SQLException e) {
			return new StandardResponse(500,"Cannot update listing");
		}
		return new StandardResponse(200);
	}
	
	private StandardResponse deleteListing (Request request) {
		long listingNum = Long.parseLong(request.params(":listingNum"));
		String owner ="";
		StandardResponse verifyRes = uc.verify(request);
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			owner = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		try {
			if (!jc.isOwnerListing(owner, listingNum)) {
				return new StandardResponse(400,"Cannot delete listing");
			}
			jc.deleteListing(listingNum);
			((OwnerSession) uc.getSession(owner)).deleteCarListingInSession(listingNum);
			JDBCConnector.listingCount= jc.getListingCount();
		} catch (SQLException e) {
			return new StandardResponse(500,"Cannot delete listing");
		}
		
		
		return new StandardResponse(200);
	}
	
	private boolean verifyListing(String rego,String username) {
		try {
			if (uc.getSession(username) == null) {
				System.out.println("Cannot get session");
			}
			return jc.findRego(rego,uc.getSession(username).getUser().getFirstName(),uc.getSession(username).getUser().getLastName());
		} catch (SQLException e) {
			return false;
		}
	}
	
	private boolean fieldsRequiredExist(JSONObject jsonObj) {
		try {
			if (jsonObj.getString("rego").equals("") || jsonObj.getString("brand").equals("") || 
					jsonObj.getString("model").equals("") || 
					jsonObj.getString("location").equals("") || 
					jsonObj.getString("colour").equals("") ||
					jsonObj.getString("transmission").equals("")  ) {
				jsonObj.getString("capacity");
				jsonObj.getString("year");
				jsonObj.getString("odometer");
				return false;
			}
		}
		catch(JSONException e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	// Upload car listing image and returns image path
	private String handleUpload(Request request,String username) throws IOException, ServletException, SQLException, NullPointerException {
		
		System.out.println(System.getProperty("user.dir")+File.separator+"listingImg");
		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(System.getProperty("user.dir")+File.separator+"listingImg"));
        Part filePart = request.raw().getPart("listing_img");
        try (InputStream inputStream = filePart.getInputStream()) {
            OutputStream outputStream = new FileOutputStream(System.getProperty("user.dir")+File.separator+"listingImg" 
            		+File.separator+ username + "-" +jc.getCarCountByOwner(username) 
            		+"."+filePart.getSubmittedFileName().split("\\.")[filePart.getSubmittedFileName().split("\\.").length-1]);
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();
        }
        System.out.println( "You uploaded this image:<h1><img src=");
        return username + "-"+jc.getCarCountByOwner(username)+"."+filePart.getSubmittedFileName().split("\\.")[filePart.getSubmittedFileName().split("\\.").length-1];
	}

	private boolean valuesInList(JSONObject jsObj) {
		String [] valueList = new String[] {"model","colour","odometer","location","transmission","capacity","accountNumber","bsb"};
		int sz = jsObj.keySet().size();
		String [] keys = new String[sz];
		keys = jsObj.keySet().toArray(keys);
		for (String fields : keys) {
			if (!Arrays.asList(valueList).contains(fields))
				return false;
		}
		return true;
	}
}
