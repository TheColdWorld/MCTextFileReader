package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.Settings;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;

public class C2SChangeSettings implements NetworkPackageContent {
    public final String CommitTime;
    public final Settings settings;

    public C2SChangeSettings(String commitTime, Settings settings) {
        CommitTime = commitTime;
        this.settings = settings;
    }

    static public C2SChangeSettings GetInstance(String Json) {
        return variables.defaultGson.fromJson(Json, C2SChangeSettings.class);
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("Setting");
        } catch (Throwable e) {
            return false;
        }
    }

    static public boolean IsInstance(JsonObject Json) {
        try {
            return Json.has("CommitTime") && Json.has("settings");
        } catch (Throwable e) {
            return false;
        }
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, C2SChangeSettings.class);
    }

    public JsonObject ToJsonObject() {
        return variables.defaultGson.fromJson(this.ToJson(), JsonObject.class);
    }
}
