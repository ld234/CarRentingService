package car_service;

import org.json.*;

public class StandardResponse {
	  
    private int statusCode;
    private String message;
    private String data;
     
    public StandardResponse(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public StandardResponse(int statusCode, String message) {
    	this.statusCode = statusCode;
    	this.message = message;
    }
    
    public StandardResponse(int statusCode, String data, boolean dataExist) {
    	this.statusCode = statusCode;
    	this.data = data;
    }
     
    public int getStatusCode() {
    	return statusCode;
    }
    
    public String getData() {
    	return data;
    }
}
