package car_service;
import static spark.Spark.*;
import com.google.gson.GsonBuilder;

import org.json.*;
import io.jsonwebtoken.*;
import java.security.Key;
import java.sql.SQLException;
import java.util.*;

import javax.crypto.spec.SecretKeySpec;

import spark.*;

public class UserController {
	private JDBCConnector jc;
	private Key key;
	public UserController() {
		jc = new JDBCConnector();
		//key = MacProvider.generateKey();
		byte[] bytes = new String("ServerSecret").getBytes();
		key = new SecretKeySpec(bytes,"HS512");
		//System.out.println(new SecretKeySpec(bytes,"HS512").getEncoded());
		post("/account", (request, response) -> {
			response.type("application/json");
            StandardResponse send = register(request);
            response.status(send.getStatusCode());
            return JsonUtil.toJson(send);
        });
        
		/*put("/account", (request, response) -> {
            //...
		});*/
		
		post("/auth", (request, response) -> {
			response.type("application/json");
			StandardResponse res = login(request);
			response.status(res.getStatusCode());
			String resp = "{\"statusCode\" :" + res.getStatusCode() + ", " + res.getData() + "}";
			return resp;
		});
		
		post("/verify", (request, response) -> {
			response.type("application/json");
			StandardResponse res = verify(request);
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res);
		});
	}
	
	// Login
	public StandardResponse login (Request request) {
		// Check username and password
		JSONObject jsonObj = new JSONObject(request.body());
		String username = jsonObj.getString("username");
		String password = jsonObj.getString("password");
		try {
			if (jc.findUsernameAndPassword(username,password)) {
				Date newDate = new Date();
				// Session expires after 30 mins
				newDate.setTime(newDate.getTime() + 900000);
				String data = "\"token\": \"" + sign(username, newDate)+ "\""; 
				System.out.println(data);
				return new StandardResponse(200, data,true);
			}
		}
		catch (SQLException e) {
			return new StandardResponse(500, e.getMessage());
		}
		return new StandardResponse(400, "Incorrect username or password");
	}
	
	public StandardResponse register(Request request) {
		try {
			CarRenter cr = null;
			JSONObject jsonObj = new JSONObject(request.body());
			String driverLicense = jsonObj.getString("driverLicense");
			String fn = jsonObj.getString("firstname");
			String ln = jsonObj.getString("lastname");
			String dob = jsonObj.getString("dob");
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
			else {
				cr = new GsonBuilder().setDateFormat("dd-MM-yyyy").create().fromJson(request.body(), CarRenter.class);
				jc.insertUser(cr);
			}
		}
		catch(SQLException e){
			return new StandardResponse(500, e.getMessage());
		}
		return new StandardResponse(200);
	}
	
	private boolean fieldsRequiredExist(JSONObject jsonObj) {
		try {
			JSONObject cc = jsonObj.getJSONObject("creditCard");
			if (jsonObj.getString("username").equals("") || jsonObj.getString("password").equals("") || 
					jsonObj.getString("firstname").equals("") || 
					jsonObj.getString("lastname").equals("") || 
					jsonObj.getString("driverLicense").equals("") ||
					jsonObj.getString("dob").equals("") || 
					cc.getString("cardholder").equals("") ||
					cc.getString("expirydate").equals("") ||
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
	
	public String sign(String username, Date exp) {
		String compactJws = Jwts.builder()
		  .setExpiration(exp)
		  .setSubject(username)
		  .signWith(SignatureAlgorithm.HS512, key)
		  .compact();
		return compactJws;
	}
	
	public StandardResponse verify (Request request ){
		String compactJws = request.headers("x-access-token");
		try {
		    String sub = Jwts.parser().setSigningKey(key).parseClaimsJws(compactJws).getBody().getSubject();
		    System.out.println(sub);
		} 
		catch (SignatureException e) {
		   return new StandardResponse(401, "Fail to authenticate.");
		}
		catch (ExpiredJwtException e) {
			return new StandardResponse(400,"Session has expired");
		}
		return new StandardResponse(200,"Verified");
	}
	
}
	
