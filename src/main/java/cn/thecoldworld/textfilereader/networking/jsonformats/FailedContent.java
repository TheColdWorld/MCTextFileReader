package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;

public class FailedContent implements ToJsonAble {
    public final String Reason;

    public FailedContent(String reason) {
        Reason = reason;
    }

    public FailedContent(JsonObject object) {
        Reason = object.has("Reason") ? "" : object.get("Reason").getAsString();
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("Reason");
        } catch (Throwable e) {
            return false;
        }
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, FailedContent.class);
    }

    public JsonObject ToJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("Reason", Reason);
        return object;
    }
}
