package car_service;
import static spark.Spark.*;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.*;
import io.jsonwebtoken.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.crypto.spec.SecretKeySpec;

import spark.*;

public class UserController {
	private JDBCConnector jc;
	private Key key;
	static HashMap<String,UserSession> connectedUsers;
	private static final long SESSION_DURATION = 10800000; // 60 mins
	
	public UserController(JDBCConnector jc) {
		this.jc = jc;
		//key = MacProvider.generateKey();
		byte[] bytes = new String("ServerSecret").getBytes();
		key = new SecretKeySpec(bytes,"HS512");
		connectedUsers = new HashMap<String,UserSession>();
		
		// Route for registration
		post("/account", (request, response) -> {
			response.type("application/json");
            StandardResponse send = register(request);
            response.status(send.getStatusCode());
			// return JsonUtil.toJson(send);
			if (send.getStatusCode() != 200) {
				return JsonUtil.toJson(send);
			}
			return "{\"statusCode\" :" + send.getStatusCode() + ", " + send.getData() + "}";
        });
		
		// Route for login
		post("/auth", (request, response) -> {
			response.type("application/json");
			StandardResponse res = login(request);
			response.status(res.getStatusCode());
			String resp = "";
			if (res.getStatusCode() == 200)
				resp = "{\"statusCode\" :" + res.getStatusCode() + ", " + res.getData() + "}";
			else {
				resp = JsonUtil.toJson(res);
			}
			System.out.println(resp);
			return resp;
		});
		
		// Route for update user password and credit card, billing details
		put("/account", (request, response) -> {
			response.type("application/json");
			StandardResponse res = update(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res);
		});

		get("/account", (req, res) -> {
			res.type("application/json");
			StandardResponse verifyRes = verify(req);
//			System.out.println("Get data verify " + verifyRes.getData());
			res.status(verifyRes.getStatusCode());
			if (verifyRes.getStatusCode() != 200) {
				return JsonUtil.toJson(verifyRes);
			}
			String username = new JSONObject((String)verifyRes.getData()).getString("subject");
			UserSession s = connectedUsers.get(username);
			if (s == null) {
				connectedUsers.put(username, createSession(username));
				s = connectedUsers.get(username);
			}
			else {
				connectedUsers.get(username).user = jc.getUser(username);
			}
			User thisUser = s.getUser();	
			String type = "";
			String className = thisUser.getClass().getSimpleName();
			if (className.equals("CarOwner")) {
				type = "carOwner";
			}
			else if (className.equals("CarRenter")) {
				type = "carRenter";
			}
			else {
				type = "admin";
			}			
			//Map<String, String> additionalProps = new HashMap<String, String>();
			//additionalProps.put("type", type);
			//additionalProps.put("username", username);
			System.out.println(JsonUtil.toJson(thisUser.getNotifList()));
			///additionalProps.put("notifications", JsonUtil.toJson(thisUser.getNotifList()));
			//String resp = JsonUtil.toJson(thisUser, new UserExclusionStrategy(), additionalProps);
			JsonObject obj = new JsonObject();
			obj.addProperty("type", type);
			obj.addProperty("username", thisUser.getUsername());
			obj.addProperty("DOB", thisUser.getDOB());
			obj.addProperty("fullname", thisUser.getFullName());
			if (type.equals("carRenter")) {
				obj.addProperty("creditCard", ((CarRenter) thisUser).getCardNumber().substring(13, 16));
			}
			else if (type.equals("carOwner")) {
				obj.addProperty("creditCard", ((CarRenter) thisUser).getCardNumber().substring(13, 16));
				obj.add("bookingRequests", new GsonBuilder().create().toJsonTree(((CarOwner) thisUser).getBookingRequestList()));
				obj.addProperty("accountNumber", ((CarOwner)thisUser).getAccountNumber());
				obj.addProperty("bsb", ((CarOwner)thisUser).getBSB());
			}
			obj.add("notifications",new Gson().toJsonTree(thisUser.getNotifList()));
			return obj.toString();
		});
		
		
		/*post("/verify", (request, response) -> {
			response.type("application/json");
			StandardResponse res = verify(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res);
		}); */
	}
	
	
	
	// Login
	private StandardResponse login (Request request) {
		// Check username and password
		JSONObject jsonObj = new JSONObject(request.body());
		String username = jsonObj.getString("username");
		jsonObj.put("password", hashPassword(jsonObj.getString("password")));
		String password = jsonObj.getString("password");
		try {
			String userType = jc.findUsernameAndPassword(username,password);
			if (userType != null) {
				System.out.println("Found username and password");
				Date newDate = new Date();
				// Session expires after 30 mins
				newDate.setTime(newDate.getTime() + SESSION_DURATION);
				connectedUsers.put(username,createSession(username));
				String data = "\"token\": \"" + sign(username, newDate)+ "\"," + "\"type\":\""+ userType+"\", \"fullname\":\""+ connectedUsers.get(username).getUser().getFullName()+ "\"";
				return new StandardResponse(200, data, true);
			}
			else {
				return new StandardResponse(400, "Incorrect username of password");
			}
		}
		catch (SQLException e) {
			return new StandardResponse(500, e.getMessage());
		}
	}
	
	private StandardResponse register(Request request) {
		String data = "";
		try {
			CarRenter cr = null;
			JSONObject jsonObj = new JSONObject(request.body());
			
			if (!fieldsRequiredExist(jsonObj)) {
				return new StandardResponse(400, "Missing fields");
			}
			String username = jsonObj.getString("username");
			String driverLicense = jsonObj.getString("driverLicense");
			String fn = jsonObj.getString("firstname");
			String ln = jsonObj.getString("lastname");
			String dob = jsonObj.getString("dob");
			JSONObject cc = jsonObj.getJSONObject("creditCard");
			String cardholder = cc.getString("cardholder");
			String exp = cc.getString("expiryDate");
			String cardNum = cc.getString("cardNumber");
			int cvv = cc.getInt("cardCvv");
			if (jc.usernameExists(username)) {
				return new StandardResponse(400, "Username exists");
			}
			else if (jsonObj.getString("password").length() < 8 ) {
				return new StandardResponse(400, "Password too short");
			}
			else if (!User.containsDigit(jsonObj.getString("password") )) {
				return new StandardResponse(400, "Password must contains digits");
			}
			else if(!verifyLicense(driverLicense,fn,ln,dob)) {
				return new StandardResponse(400, "Cannot verify license");
			}
			else if(!verifyCreditCard(cardNum,cardholder,exp,cvv)) {
				return new StandardResponse(400, "Cannot verify credit card");
			}
			else {
				jsonObj.put("password", hashPassword(jsonObj.getString("password")));
				cr = new GsonBuilder().setDateFormat("dd-MM-yyyy").create().fromJson(jsonObj.toString(), CarRenter.class);
				jc.insertUser(cr);
				Date newDate = new Date();
				// Session expires after 5 mins
				newDate.setTime(newDate.getTime() + SESSION_DURATION);
				data = "\"token\": \"" + sign(username, newDate) + "\"";
				// data = sign(username, newDate);
				connectedUsers.put(username, createSession(username));
			}
		}
		catch(SQLException e){
			return new StandardResponse(500, e.getMessage());
		}
		return new StandardResponse(200, data, true);
	}
	
	private StandardResponse update(Request request) {
		System.out.println("xxxxxxx");
		JSONObject jsObj =  new JSONObject(request.body());
		String subject ="";
		StandardResponse verifyRes = verify(request);
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			subject = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		if (!valuesInList(jsObj))
			return new StandardResponse(400,"Values sent not accepted.");
		if (jsObj.has("password")){
			if (!jsObj.has("oldPassword")) {
				return new StandardResponse(400,"Missing old password");
			}
			else {
				String oldPwHash = hashPassword(jsObj.getString("oldPassword"));
				try {
					if (jc.findUsernameAndPassword(subject, oldPwHash) == null) {
						return new StandardResponse(401,"Wrong old password");
					}
				} catch (SQLException e) {
					e.printStackTrace();
					return new StandardResponse(500);
				}
			}
			if (!User.containsDigit(jsObj.getString("password"))) {
				return new StandardResponse(400,"Password must contain digits");
			}
			else if(jsObj.getString("password").length() < 8) {
				return new StandardResponse(400,"Password too short");
			}
			else {
				try {
					String newpw = hashPassword(jsObj.getString("password"));
					jsObj.put("password", newpw);
					jsObj.remove("oldPassword");
					UserSession s = connectedUsers.get(subject);
					if (s == null) {
						System.out.println("Add new session");
						connectedUsers.put(subject, createSession(subject));
						s = connectedUsers.get(subject);
						//return new StandardResponse(400, "Cannot update.");
					}
					s.getUser().setPassword(newpw);
					connectedUsers.put(subject, s);
					//System.out.println(connectedUsers.get(subject).getUser().getFirstName() + " " + connectedUsers.get(subject).getUser().getPassword());
					//jc.updateUser(jsObj,subject);
				} catch (JSONException e) {
					e.printStackTrace();
					return new StandardResponse(400);
				} 
			}
		}
		if (jsObj.has("creditCard")) {
			JSONObject cc = jsObj.getJSONObject("creditCard");
			if (!verifyCreditCard(cc.getString("cardNumber"),cc.getString("cardholder"),cc.getString("expiryDate"),cc.getInt("cardCvv"))) {
				return new StandardResponse(400,"Incorrect credit card details");
			}
			jsObj.remove("creditCard");
			jsObj.put("cardNumber", cc.getString("cardNumber"));
		}
		
		if (jsObj.has("bsb") || jsObj.has("accountNumber") ) {
			System.out.println("BSB and acc num");
			try {
				if (!(jsObj.has("bsb") && jsObj.has("accountNumber") && jc.verifyBankAccount(jsObj.getString("accountNumber"), jsObj.getString("bsb")))) {
					return new StandardResponse(400, "Invalid bank account details.");
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return new StandardResponse(400,"Insufficient bank account details." );
			} catch (SQLException e) {
				e.printStackTrace();
				return new StandardResponse(400, "Cannot verify bank account details.");
			}
		}
		
		try {
			jc.updateUser(jsObj, subject);
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(400, "Cannot update.");
		}
		return new StandardResponse(200);
	}
	
	// Check register process - all fields are required
	private boolean fieldsRequiredExist(JSONObject jsonObj) {
		try {
			JSONObject cc = jsonObj.getJSONObject("creditCard");
			if (jsonObj.getString("username").equals("") || jsonObj.getString("password").equals("") || 
					jsonObj.getString("firstname").equals("") || 
					jsonObj.getString("lastname").equals("") || 
					jsonObj.getString("driverLicense").equals("") ||
					jsonObj.getString("dob").equals("") || 
					cc.getString("cardholder").equals("") ||
					cc.getString("expiryDate").equals("") ||
					cc.getString("cardNumber").equals("")) {
				
				return false;
			}
			cc.getInt("cardCvv");
		}
		catch(JSONException e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	private boolean verifyLicense(String license,String fn, String ln, String dob) {
		try {
			return jc.findLicense( license, fn,  ln,  dob);
		} catch (SQLException e) {
			return false;
		}
	}
	
	private boolean verifyCreditCard (String cardNum,String cardholder, String expiryDate, int cvv) {
		try {
			return jc.findCreditCard( cardNum, cardholder,expiryDate,cvv);
		} catch (SQLException e) {
			return false;
		}
	}
	
	// Create token
	private String sign(String username, Date exp) {
		String compactJws = Jwts.builder()
		  .setExpiration(exp)
		  .setSubject(username)
		  .signWith(SignatureAlgorithm.HS512, key)
		  .compact();
		return compactJws;
	}
	
	// Verify token
	public StandardResponse verify (Request request ){
		String sub = "";
		String compactJws = request.headers("x-access-token");
		try {
		    sub = Jwts.parser().setSigningKey(key).parseClaimsJws(compactJws).getBody().getSubject();
		    System.out.println("Subject" + sub);
		} 
		catch (SignatureException e) {
		   return new StandardResponse(401, "Fail to authenticate");
		}
		catch (ExpiredJwtException e) {
			return new StandardResponse(400,"Session has expired");
		}
		catch (MalformedJwtException e) {
			return new StandardResponse(401,"Not authorised");
		}
		catch (IllegalArgumentException e) {
			return new StandardResponse(401,"Not authorised");
		}
		return new StandardResponse(200,"{\"subject\" : \"" + sub +"\"}",true);
	}
	
	// Hash user password - without salt!!!
	private String hashPassword(String password) {
		String sha256hex = Hashing.sha256()
				  .hashString(password, StandardCharsets.UTF_8)
				  .toString();
		return sha256hex;
	}
	
	public UserSession createSession(String username) {
		try {
			if (jc.findCarOwner(username)) {
				removeSession(username, SESSION_DURATION);
				return new OwnerSession(username);
			}
			else if(jc.usernameExists(username)) {
				removeSession(username, SESSION_DURATION);
				return new UserSession(username);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public UserSession getSession(String username) {
		UserSession s = connectedUsers.get(username);
		if(s != null) return s;
		return createSession(username);
	}
	
	private void removeSession(String username, long amount) {
		new ScheduledThreadPoolExecutor(20).schedule(new RemovalService(username), amount, TimeUnit.MILLISECONDS);
	}
	
	private class RemovalService implements Runnable{
		private String username; 
		public RemovalService(String username) {
			this.username = username;
		}
		
		public void run() {
			System.out.println("removing "+ username);
			connectedUsers.remove(username);
		}
	}
	
	private boolean valuesInList(JSONObject jsObj) {
		String [] valueList = new String[] {"password","creditCard","oldPassword","accountNumber","bsb"};
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
	
