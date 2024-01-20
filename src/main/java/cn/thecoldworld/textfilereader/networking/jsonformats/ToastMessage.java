package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import net.minecraft.text.Text;

import java.util.Objects;

public class ToastMessage implements NetworkPackageContent {
    public final String Title;
    private final String Message;

    public ToastMessage(String title, Text message) {
        Message = Text.Serialization.toJsonString(message);
        Title = title;
    }

    static public ToastMessage GetInstance(String Json) {
        return variables.defaultGson.fromJson(Json, ToastMessage.class);
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("Message");
        } catch (Throwable e) {
            return false;
        }
    }

    static public boolean IsInstance(JsonObject object) {
        try {
            return object.has("Message") && object.has("Title");
        } catch (Throwable e) {
            return false;
        }
    }

    public String GetString() {
        return Objects.requireNonNullElse(Text.Serialization.fromJson(Message), Text.EMPTY).getString();
    }

    public Text GetMCText() {
        return Objects.requireNonNullElse(Text.Serialization.fromJson(Message), Text.empty());
    }

    public String GetStringOrThrow() throws NullPointerException {
        return Objects.requireNonNull(Text.Serialization.fromJson(Message)).getString();
    }

    public Text GetMCTextOrThrow() throws NullPointerException {
        return Objects.requireNonNull(Text.Serialization.fromJson(Message));
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, ToastMessage.class);
    }

    public JsonObject ToJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("Message", Message);
        object.addProperty("Title", Title);
        return object;
    }
}
