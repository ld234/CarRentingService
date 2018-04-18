package car_service;
import static spark.Spark.*;

import com.google.common.hash.Hashing;
import com.google.gson.GsonBuilder;

import org.json.*;
import io.jsonwebtoken.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeUnit;

import javax.crypto.spec.SecretKeySpec;

import spark.*;

public class UserController {
	private JDBCConnector jc;
	private Key key;
	private HashMap<String,Session> connectedUsers;
	private static final long SESSION_DURATION = 600000; // 10 mins
	
	public UserController(JDBCConnector jc) {
		this.jc = jc;
		//key = MacProvider.generateKey();
		byte[] bytes = new String("ServerSecret").getBytes();
		key = new SecretKeySpec(bytes,"HS512");
		connectedUsers = new HashMap<String,Session>();
		// Route for registration
		post("/account", (request, response) -> {
			//TimeUnit.SECONDS.sleep(30);
			response.type("application/json");
            StandardResponse send = register(request);
            response.status(send.getStatusCode());
            return JsonUtil.toJson(send);
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
		
		// Route for update user password
		put("/account", (request, response) -> {
			response.type("application/json");
			StandardResponse res = update(request);
			return JsonUtil.toJson(res);
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
				// Session expires after 10 mins
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
		try {
			CarRenter cr = null;
			JSONObject jsonObj = new JSONObject(request.body());
			
			if (!fieldsRequiredExist(jsonObj)) {
				return new StandardResponse(400, "Missing fields");
			}
			String driverLicense = jsonObj.getString("driverLicense");
			String fn = jsonObj.getString("firstname");
			String ln = jsonObj.getString("lastname");
			String dob = jsonObj.getString("dob");
			JSONObject cc = jsonObj.getJSONObject("creditCard");
			String cardholder = cc.getString("cardholder");
			String exp = cc.getString("expiryDate");
			String cardNum = cc.getString("cardNumber");
			if (jc.usernameExists(jsonObj.getString("username"))) {
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
			else if(!verifyCreditCard(cardNum,cardholder,exp)) {
				return new StandardResponse(400, "Cannot verify credit card");
			}
			else {
				jsonObj.put("password", hashPassword(jsonObj.getString("password")));
				cr = new GsonBuilder().setDateFormat("dd-MM-yyyy").create().fromJson(jsonObj.toString(), CarRenter.class);
				jc.insertUser(cr);
			}
		}
		catch(SQLException e){
			return new StandardResponse(500, e.getMessage());
		}
		return new StandardResponse(200);
	}
	
	private StandardResponse update(Request request) {
		JSONObject jsObj =  new JSONObject(request.body());
		String subject ="";
		StandardResponse verifyRes = verify(request);
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			subject = new JSONObject(verifyRes.getData()).getString("subject");
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
				Session s = connectedUsers.get(subject);
				if (s == null) {
					System.out.println("Add new session");
					connectedUsers.put(subject, createSession(subject));
					s = connectedUsers.get(subject);
					//return new StandardResponse(400, "Cannot update.");
				}
				s.getUser().setPassword(newpw);
				connectedUsers.put(subject, s);
				//System.out.println(connectedUsers.get(subject).getUser().getFirstName() + " " + connectedUsers.get(subject).getUser().getPassword());
				jc.updateUser(jsObj,subject);
			} catch (SQLException e) {
				return new StandardResponse(400, "Cannot update.");
			} catch (JSONException e) {
				e.printStackTrace();
			} 
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
					cc.getString("cardNumber").equals(""))
				return false;
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
	
	private boolean verifyCreditCard (String cardNum,String cardholder, String expiryDate) {
		try {
			return jc.findCreditCard( cardNum, cardholder,expiryDate);
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
		    System.out.println("Time now: " + new Date().getTime());
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
	
	private Session createSession(String username) {
		try {
			if (jc.findCarOwner(username))
				return new OwnerSession(username);
			else if(jc.usernameExists(username))
				return new RenterSession(username);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		removeSession(username, SESSION_DURATION);
		return new AdminSession(username);
	}
	
	public Session getSession(String username) {
		Session s = connectedUsers.get(username);
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
}
	
