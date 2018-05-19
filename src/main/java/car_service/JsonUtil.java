package car_service;

import java.sql.Timestamp;
import java.util.*;
import com.google.gson.*;

public class JsonUtil {
	public static String toJson(Object object) {
		return new GsonBuilder().setDateFormat("dd-MM-yyyy").create().toJson(object);
	}
	
	public static String toJson2(Object object) {
		return new GsonBuilder().setDateFormat("dd-MM-yyyy hh:mm:ss.SS").create().toJson(object);
	}
	
	public static String toJson3(Object object) {
		Gson gson = new GsonBuilder()
		        .registerTypeAdapter(Timestamp.class, (JsonDeserializer<Timestamp>) (json, typeOfT, context) -> new Timestamp(json.getAsJsonPrimitive().getAsLong()))
		        .registerTypeAdapter(Timestamp.class, (JsonSerializer<Timestamp>) (date, type, jsonSerializationContext) -> new JsonPrimitive(date.getTime()))
		        .create();
		return gson.toJson(object);
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