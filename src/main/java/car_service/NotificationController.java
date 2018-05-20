package car_service;

import org.json.JSONObject;
import static spark.Spark.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import spark.Request;

public class NotificationController {
	private JDBCConnector jc;
	private UserController uc;
	
	public NotificationController(JDBCConnector jc, UserController uc) {
		this.jc = jc;
		this.uc= uc;
		
		get("/notification", (request,response) -> {
			StandardResponse res = getNotifications(request);
			response.type("application/json");
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res.getData());
		});
		
		put("/seen/:notifNumber", (request,response) ->{
			StandardResponse res = seenNotification(request);
			response.type("application/json");
			response.status(res.getStatusCode());
			return JsonUtil.toJson(res);
		});
	}
	
	public StandardResponse seenNotification(Request request) {
		Long notifNumber = Long.parseLong(request.params(":notifNumber"));
		try {
			jc.setSeen(notifNumber, null, null);
		} catch (SQLException e) {
			e.printStackTrace();
			new StandardResponse (500, "Failed to set seen");
		}
		return new StandardResponse (200, "Notif seen");
	}
	
	public StandardResponse getNotifications (Request request) {
		JsonObject obj = new JsonObject();
		StandardResponse verifyRes = uc.verify(request);
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		String username = new JSONObject((String)verifyRes.getData()).getString("subject");
		UserSession s = UserController.connectedUsers.get(username);
		
		if (s == null) {
			UserController.connectedUsers.put(username, uc.createSession(username));
			s = UserController.connectedUsers.get(username);
		}
		else {
			
			try {
				UserController.connectedUsers.get(username).user = jc.getUser(username);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		User thisUser = s.getUser();
		ArrayList<Notification> res = thisUser.getNotifList();
		Collections.sort(res, new Comparator<Notification>() {
	        @Override
	        public int compare(Notification n1, Notification n2) {
	            return Boolean.compare(n1.getSeen(),n2.getSeen());
	        }
	    });
		obj.add("notifications",new Gson().toJsonTree(thisUser.getNotifList()));
		
		return new StandardResponse(200,obj,true);
	}
}
