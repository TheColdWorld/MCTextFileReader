package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class S2CGetContent implements ToJsonAble {
    public final String Value;

    public S2CGetContent(String value) {
        Value = value;
    }

    public S2CGetContent(@NotNull JsonObject jsonObject) {
        Value = jsonObject.has("Value") ? "" : jsonObject.get("Value").getAsString();
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("Value");
        } catch (Throwable e) {
            return false;
        }
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, S2CGetContent.class);
    }

    public JsonObject ToJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("Value", Value);
        return object;
    }
}
