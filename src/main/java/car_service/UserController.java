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

import javax.crypto.spec.SecretKeySpec;

import spark.*;

public class UserController {
	private JDBCConnector jc;
	private Key key;
	
	public UserController(JDBCConnector jc) {
		this.jc = jc;
		//key = MacProvider.generateKey();
		byte[] bytes = new String("ServerSecret").getBytes();
		key = new SecretKeySpec(bytes,"HS512");
		
		// Route for registration
		post("/account", (request, response) -> {
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
			if (jc.findUsernameAndPassword(username,password)) {
				System.out.println("Found username and password");
				Date newDate = new Date();
				// Session expires after 30 mins
				newDate.setTime(newDate.getTime() + 900000);
				String data = "\"token\": \"" + sign(username, newDate)+ "\""; 
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
			String driverLicense = jsonObj.getString("driverLicense");
			String fn = jsonObj.getString("firstname");
			String ln = jsonObj.getString("lastname");
			String dob = jsonObj.getString("dob");
			JSONObject cc = jsonObj.getJSONObject("creditCard");
			String cardholder = cc.getString("cardholder");
			String exp = cc.getString("expiryDate");
			String cardNum = cc.getString("cardNumber");
			if (!fieldsRequiredExist(jsonObj)) {
				return new StandardResponse(400, "Missing fields");
			}
			else if (jc.usernameExists(jsonObj.getString("username"))) {
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
		//CarRenter cr;
		if (verify(request).getStatusCode() != 200) {
			return verify(request);
		}
		else {
			subject = new JSONObject(verify(request).getData()).getString("subject");
			/*try {
				cr = (CarRenter) jc.getUser(subject);
			} catch (SQLException e) {
				return new StandardResponse(500);
			}*/
		}
		if (!User.containsDigit(jsObj.getString("password"))) {
			return new StandardResponse(400,"Password must contain digits");
		}
		else if(jsObj.getString("password").length() < 8) {
			return new StandardResponse(400,"Password too short");
		}
		else {
			try {
				jsObj.put("password", hashPassword(jsObj.getString("password")));
				//cr.setPassword(jsObj.getString("password"));
				jc.updateUser(jsObj,subject);
			} catch (SQLException e) {
				return new StandardResponse(400, "Cannot update.");
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
	private StandardResponse verify (Request request ){
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
		return new StandardResponse(200,"{\"subject\" : \"" + sub + "\"}",true);
	}
	
	// Hash user password - without salt!!!
	private String hashPassword(String password) {
		String sha256hex = Hashing.sha256()
				  .hashString(password, StandardCharsets.UTF_8)
				  .toString();
		return sha256hex;
	}
}
	
