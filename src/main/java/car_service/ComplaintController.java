package car_service;

import static spark.Spark.*;

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
		
		// Get unresolved complaint
		get("/complaint", (request,response) ->{
			StandardResponse res = fileComplaint(request);
			response.status(res.getStatusCode());
			if(res.getStatusCode() == 200 )
				return res.getData();
			return JsonUtil.toJson(res);
		});
		
		get("/complaint/:id", (request,response) ->{
			StandardResponse res = fileComplaint(request);
			response.status(res.getStatusCode());
			if(res.getStatusCode() == 200 )
				return res.getData();
			return JsonUtil.toJson(res);
		});
		
		delete("/complaint/:id", (request,response) ->{
			StandardResponse res = fileComplaint(request);
			response.status(res.getStatusCode());
			if(res.getStatusCode() == 200 )
				return res.getData();
			return JsonUtil.toJson(res);
		});
	}
	
	public StandardResponse fileComplaint(Request request) {
		return null;
	}
}
