package car_service;

import java.util.*;
import com.google.gson.*;

public class JsonUtil {
	public static String toJson(Object object) {
		return new GsonBuilder().setDateFormat("dd-MM-yyyy").create().toJson(object);
	}
	
	public static String toJson2(Object object) {
		return new GsonBuilder().setDateFormat("dd-MM-yyyy hh:mm:ss.SS").create().toJson(object);
	}

	public static String toJson(Object object, ExclusionStrategy MyExclusionStrategy) {
		return new GsonBuilder().setDateFormat("dd-MM-yyyy").setExclusionStrategies(MyExclusionStrategy).create().toJson(object);
	}

	public static String toJson(Object object, ExclusionStrategy MyExclusionStrategy, Map<String, String> additionalProperties) {
		Gson gs = new GsonBuilder().setDateFormat("dd-MM-yyyy").setExclusionStrategies(MyExclusionStrategy).create();
		JsonElement jsonElement = gs.toJsonTree(object);
		if (!additionalProperties.isEmpty()) {
			 for (Map.Entry<String, String> prop : additionalProperties.entrySet()) {
				 jsonElement.getAsJsonObject().addProperty(prop.getKey(), prop.getValue());
			 }
		}
		return gs.toJson(jsonElement);
	}
}