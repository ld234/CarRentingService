package car_service;

import static spark.Spark.*;

import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import spark.Request;

public class ComplaintController {
	private JDBCConnector jc;
	private UserController uc;
	
	public ComplaintController(JDBCConnector jc, UserController uc) {
		this.uc = uc;
		this.jc = jc;
		
		// File a complaint
		post("/complaint", (request,response) ->{
			StandardResponse res = fileComplaint(request);
			response.status(res.getStatusCode());
			if(res.getStatusCode() == 200 )
				return res.getData();
			return JsonUtil.toJson(res);
		});
		
		// Get resolved complaint
		get("/complaint/approved", (request,response) ->{
			StandardResponse res = getApprovedComplaints(request);
			response.status(res.getStatusCode());
			if(res.getStatusCode() == 200 )
				return res.getData();
			return JsonUtil.toJson(res);
		});
		
		// Get unresolved complaint
		get("/complaint/unapproved", (request,response) ->{
			StandardResponse res = getUnapprovedComplaints(request);
			response.status(res.getStatusCode());
			if(res.getStatusCode() == 200 )
				return res.getData();
			return JsonUtil.toJson(res);
		});
		
		// Get complaint 
		get("/complaint/:id", (request,response) ->{
			StandardResponse res = fileComplaint(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			if(res.getStatusCode() == 200 )
				return res.getData();
			return JsonUtil.toJson(res);
		});
		
		// Approve
		put("/complaint/:id", (request,response) ->{
			StandardResponse res = approveComplaint(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			return JsonUtil.toJson(res);
		});
		
		// Reject
		delete("/complaint/:id", (request,response) ->{
			StandardResponse res = rejectComplaint(request);
			response.status(res.getStatusCode());
			response.type("application/json");
			return JsonUtil.toJson(res);
		});
	}
	
	public StandardResponse fileComplaint(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String complainant = null;
		Complaint c = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			complainant = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		JSONObject jsObj = new JSONObject(request.body());
		try {
			c = new Complaint(jc.getComplaintID(),complainant,jsObj.getLong("listingNumber"),jsObj.getString("description"));
			//String type, String msg, String receiver
			for (String admin: jc.getAdmins()) {
				Notification notif = new Notification("newComplaint", complainant + " has submitted a claim.",admin);
				jc.insertNotification(notif);
			}
			jc.addComplaint(c);
		} catch(JSONException e) {
			return new StandardResponse(400,"Insufficient details");
		} catch(SQLException e) {
			return new StandardResponse(400,"Cannot save complaint.");
		}
		return new StandardResponse(200,c,true);
	}
	
	public StandardResponse getApprovedComplaints(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String admin = null;
		ArrayList<Complaint> c = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			admin = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		try {
			if (!jc.isFAdmin(admin) && !jc.isCAdmin(admin)) {
				return new StandardResponse(401,"Unauthorised");
			}
			c = jc.getApprovedComplaints();
		} catch (SQLException e) {
			return new StandardResponse(500,"Cannot get approved complaints");
		}
		
		return new StandardResponse(200,c,true);
	}
	
	public StandardResponse getUnapprovedComplaints(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String admin = null;
		ArrayList<Complaint> c = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			admin = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		try {
			if (!jc.isFAdmin(admin) && !jc.isCAdmin(admin)) {
				return new StandardResponse(401,"Unauthorised");
			}
			c = jc.getUnapprovedComplaints();
		} catch (SQLException e) {
			return new StandardResponse(500,"Cannot approve complaint");
		}
		
		return new StandardResponse(200,c,true);
	}
	
	public StandardResponse approveComplaint(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String admin = null;
		Long cid = Long.parseLong(request.params(":id"));
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			admin = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		try {
			if (!jc.isFAdmin(admin) && !jc.isCAdmin(admin)) {
				return new StandardResponse(401,"Unauthorised");
			}
			jc.approveComplaint(cid);
		} catch (SQLException e) {
			return new StandardResponse(500,"Cannot reject complaint");
		}
		return new StandardResponse(200,"Complaint rejected");
	}
	
	public StandardResponse rejectComplaint(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String admin = null;
		Long cid = Long.parseLong(request.params(":id"));
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			admin = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		try {
			if (!jc.isFAdmin(admin) && !jc.isCAdmin(admin)) {
				return new StandardResponse(401,"Unauthorised");
			}
			jc.rejectComplaint(cid);
		} catch (SQLException e) {
			return new StandardResponse(500,"Cannot reject complaint");
		}
		
		return new StandardResponse(200,"Complaint rejected");
	}
	
	public StandardResponse getComplaintById(Request request) {
		StandardResponse verifyRes = uc.verify(request);
		String admin = null;
		Long cid = Long.parseLong(request.params(":id"));
		Complaint c = null;
		if (verifyRes.getStatusCode() != 200) {
			return verifyRes;
		}
		else {
			admin = new JSONObject((String)verifyRes.getData()).getString("subject");
		}
		try {
			if (!jc.isFAdmin(admin) && !jc.isCAdmin(admin)) {
				return new StandardResponse(401,"Unauthorised");
			}
			c = jc.getComplaintById(cid);
		} catch (SQLException e) {
			return new StandardResponse(500,"Cannot approve complaint");
		}
		
		return new StandardResponse(200,c,true);
	}
}
