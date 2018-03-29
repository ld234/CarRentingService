package car_service;

import com.google.gson.*;

public class JsonUtil {
	public static String toJson(Object object) {
		return new GsonBuilder().setDateFormat("dd-MM-yyyy").create().toJson(object);
	}
}