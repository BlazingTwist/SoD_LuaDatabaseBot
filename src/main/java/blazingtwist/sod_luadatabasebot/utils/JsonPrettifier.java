package blazingtwist.sod_luadatabasebot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonPrettifier {

	private static Gson gsonInstance;

	private static void ensureGson() {
		if (gsonInstance == null) {
			gsonInstance = new GsonBuilder().setPrettyPrinting().create();
		}
	}

	public static String prettify(String json) {
		ensureGson();
		JsonElement jsonElement = JsonParser.parseString(json);
		return gsonInstance.toJson(jsonElement);
	}

}
