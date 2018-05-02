package car_service;

import static spark.Spark.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import spark.Request;

public class ChatController {
	private JDBCConnector jc; 
	private UserController uc; 
	
	public ChatController(JDBCConnector jc, UserController uc) {
		this.uc = uc;
		this.jc = jc;
		
		get("/chat", (request,response) -> {
			StandardResponse res = getMessages(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			return JsonUtil.toJson(res);
		});
		
		post("/chat",(request, response) -> {
			StandardResponse res = sendMessage(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			return JsonUtil.toJson(res);
		});
	}
	
	// Get all messages by a user
	private StandardResponse getMessages(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String username = null;
		ArrayList<Message> sent = null;
		ArrayList<Message> received = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			username = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		HashMap<String, ArrayList<Message> > messageMap = new HashMap<String, ArrayList<Message> >();
		try {
			sent = jc.getMessageSent(username);
			received = jc.getMessageReceived(username);
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(500);
		}
		messageMap.put("sentMessages", sent);
		messageMap.put("receivedMessages",received);
		return new StandardResponse(200,messageMap,true);
	}
	
	private StandardResponse sendMessage(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String username = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			username = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		JSONObject jsObj = new JSONObject(request.body());
		Message message = null;
		if (!fieldsRequiredExist(jsObj)) {
			return new StandardResponse(400,"Message did not go through");
		}
		else {
			message = new Message(username,jsObj.getString("receiver"), jsObj.getString("message"));
			try {
				jc.insertMessage(message);
			}
			catch (SQLException e) {
				e.printStackTrace();
				return new StandardResponse(400,"Invalid message");
			}
		}
		return new StandardResponse(200,message,true);
	}
	
	private boolean fieldsRequiredExist(JSONObject jsonObj) {
		try {
			JSONObject cc = jsonObj.getJSONObject("creditCard");
			if (jsonObj.getString("receiver").equals("") || jsonObj.getString("message").equals("")) {
				return false;
			}
		}
		catch(JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
