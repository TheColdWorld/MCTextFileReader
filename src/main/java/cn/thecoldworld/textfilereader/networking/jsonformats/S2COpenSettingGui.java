package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.Settings;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;

public class S2COpenSettingGui implements NetworkPackageContent {
    public final Settings Setting;

    public S2COpenSettingGui(Settings settings) {
        this.Setting = settings;
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("Setting");
        } catch (Throwable e) {
            return false;
        }
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, S2COpenSettingGui.class);
    }

    public JsonObject ToJsonObject() {
        return variables.defaultGson.fromJson(this.ToJson(), JsonObject.class);
    }
}
