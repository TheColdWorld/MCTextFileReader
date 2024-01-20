package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;

public class CommonMessage implements NetworkPackageContent {
    public final String message;

    public CommonMessage(String message) {
        this.message = message;
    }

    static public CommonMessage GetInstance(String Json) {
        return variables.defaultGson.fromJson(Json, CommonMessage.class);
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("message");
        } catch (Throwable e) {
            return false;
        }
    }

    static public boolean IsInstance(JsonObject object) {
        try {
            return object.has("message");
        } catch (Throwable e) {
            return false;
        }
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, CommonMessage.class);
    }

    public JsonObject ToJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("message", message);
        return object;
    }
}
