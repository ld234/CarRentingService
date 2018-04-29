package car_service;

import spark.Request;
import spark.utils.IOUtils;

import static spark.Spark.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.GsonBuilder;
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
		
		put("/list/:listingNum", (request,response) -> {
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
			return JsonUtil.toJson(res.getData());
		});
		
		// Search car listing
		get("/search",(request,response) -> {
			response.type("application/json");
			StandardResponse res = search(request);
			request.queryParams("");
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res.getData());
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
	
	private StandardResponse createListing (Request request) {
		//System.out.println(request.body());   
		String json="";
		JDBCConnector.listingCount += 1;
		long listingNum = JDBCConnector.listingCount;

		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(System.getProperty("user.dir")+File.separator+"listingImg"));
		json = request.raw().getParameter("data");
		if (request.raw().getParameter("data") == null) {
			System.out.println("Cannot get image");
		}
		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(json);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		String owner ="";
		StandardResponse verifyRes = uc.verify(request);
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			owner = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		jsonObj.put("listingNumber", listingNum);
		try {
			String imgPath = handleUpload(request, listingNum);
			jsonObj.put("img", imgPath);
		} catch (IOException | ServletException e1) {
			e1.printStackTrace();
		}
		if (!fieldsRequiredExist(jsonObj)) {
			return new StandardResponse(400, "Missing fields");
		}
		else if(!verifyListing(jsonObj.getString("rego"),owner)) {
			return new StandardResponse(400, "Invalid car.");
		}
		try {
			CarListing cl = new GsonBuilder().create().fromJson(jsonObj.toString(), CarListing.class);
			jc.addListing(owner,cl);
			((OwnerSession) uc.getSession(owner)).addCarListingToSession(cl);
		} catch (JsonSyntaxException | SQLException e) {
			System.out.println(e.getMessage());		
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
	private String handleUpload(Request request, long listingNum) throws IOException, ServletException {
		
		System.out.println(System.getProperty("user.dir")+File.separator+"listingImg");
		request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(System.getProperty("user.dir")+File.separator+"listingImg"));
        Part filePart = request.raw().getPart("listing_img");
        try (InputStream inputStream = filePart.getInputStream()) {
            OutputStream outputStream = new FileOutputStream(System.getProperty("user.dir")+File.separator+"listingImg" +File.separator+ listingNum +"."+filePart.getSubmittedFileName().split("\\.")[1]);
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();
        }
        System.out.println( "<h1>You uploaded this image:<h1><img src=");
        return listingNum+"."+filePart.getContentType().split("\\/")[1];
	}

	private boolean valuesInList(JSONObject jsObj) {
		String [] valueList = new String[] {"model","colour","odometer","location","transmission","capacity"};
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
