package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.LinkedList;

public final class FailedContent implements NetworkPackageContent {
    public final String Reason;

    public FailedContent(String TranslateKey) {
        Reason = TranslateKey;
    }

    public FailedContent(String TranslateKey, Object... args) {
        StringBuilder sb = new StringBuilder(TranslateKey);
        for (Object arg : args) {
            sb.append(arg.toString()).append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        Reason = sb.toString();
    }

    public FailedContent(String TranslateKey, String... args) {
        StringBuilder sb = new StringBuilder(TranslateKey);
        for (String arg : args) {
            sb.append(arg).append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        Reason = sb.toString();
    }

    public FailedContent(JsonObject object) {
        Reason = object.has("Reason") ? object.get("Reason").getAsString() : "";
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("Reason");
        } catch (Throwable e) {
            return false;
        }
    }

    static public boolean IsInstance(JsonObject obj) {
        try {
            return obj.has("Reason");
        } catch (Throwable e) {
            return false;
        }
    }

    public Text AsMCText() {
        LinkedList<String> Keys = new LinkedList<>(Arrays.asList(this.Reason.split("\n")));
        String Key = Keys.get(0);
        Keys.remove(0);
        return Text.translatable(Key, Keys.toArray());
    }

    public String AsString() {
        LinkedList<String> Keys = new LinkedList<>(Arrays.asList(this.Reason.split("\n")));
        String Key = Keys.get(0);
        Keys.remove(0);
        return Text.translatable(Key, Keys.toArray()).getString();
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
