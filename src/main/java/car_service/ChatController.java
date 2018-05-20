package car_service;

import static spark.Spark.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONException;
import org.json.JSONObject;

import spark.Request;

public class ChatController {
	private JDBCConnector jc; 
	private UserController uc; 
	
	public ChatController(JDBCConnector jc, UserController uc) {
		this.uc = uc;
		this.jc = jc;
		
		get("/chat/users", (request,response) -> {
			StandardResponse res = getConnectedUsers(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			if (res.getStatusCode() == 200)
				return JsonUtil.toJson(res.getData());
			return JsonUtil.toJson(res);
		});
		
		get("/chat/:otherUser", (request,response) -> {
			StandardResponse res = getMessages(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			return JsonUtil.toJson3(res.getData());
		});
		
		post("/chat/:receiver",(request, response) -> {
			StandardResponse res = sendMessage(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			if (res.getStatusCode()==200)
				return JsonUtil.toJson3(res);
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
		String otherUser = request.params(":otherUser");
		try {
			sent = jc.getMessageSent(username,otherUser);
			received = jc.getMessageReceived(username,otherUser);
			sent.addAll(received);
			Collections.sort(sent);
		} catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(500);
		}
		return new StandardResponse(200,sent,true);
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
		String receiver = request.params(":receiver");
		if (!fieldsRequiredExist(jsObj)) {
			return new StandardResponse(400,"Message did not go through");
		}
		else {
			message = new Message(username,receiver, jsObj.getString("message"));
			try {
				jc.insertMessage(message);
				jc.insertNotification(new Notification("newMessage",username + " messaged you.",receiver));
			}
			catch (SQLException e) {
				e.printStackTrace();
				return new StandardResponse(400,"Invalid message");
			}
		}
		return new StandardResponse(200,message,true);
	}
	
	private StandardResponse getConnectedUsers(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String username = null;
		ArrayList<String> userList = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			username = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		try {
			userList = jc.getConnectedUsers(username);
		}
		catch (SQLException e) {
			e.printStackTrace();
			return new StandardResponse(400,"Cannot fetch username list");
		}
		return new StandardResponse(200,userList,true);
	}
	
	
	private boolean fieldsRequiredExist(JSONObject jsonObj) {
		try {
			if (jsonObj.getString("message").equals("")) {
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
