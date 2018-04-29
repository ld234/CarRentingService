package car_service;

public class StandardResponse {
	  
    private int statusCode;
    private String message;
    private Object data;
     
    public StandardResponse(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public StandardResponse(int statusCode, String message) {
    	this.statusCode = statusCode;
    	this.message = message;
    }
    
    public StandardResponse(int statusCode, Object data, boolean dataExist) {
    	this.statusCode = statusCode;
    	this.data = data;
    }
     
    public int getStatusCode() {
    	return statusCode;
    }
    
    public String getMessage() {
    	return message;
    }
    
    public Object getData() {
    	return data;
    }
}
