package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public final class S2CGetFileContent implements NetworkPackageContent {
    public final String Value;

    public S2CGetFileContent(String value) {
        Value = value;
    }

    public S2CGetFileContent(@NotNull JsonObject jsonObject) {
        Value = jsonObject.has("Value") ? jsonObject.get("Value").getAsString() : "";
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("Value");
        } catch (Throwable e) {
            return false;
        }
    }

    static public boolean IsInstance(JsonObject object) {
        try {
            return object.has("Value");
        } catch (Throwable e) {
            return false;
        }
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, S2CGetFileContent.class);
    }

    public JsonObject ToJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("Value", Value);
        return object;
    }
}
