package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.LinkedList;
import java.util.List;

public class S2CGetFileList implements NetworkPackageContent {
    final List<String> files;

    public S2CGetFileList(List<String> files) {
        this.files = files;
    }

    public S2CGetFileList(JsonObject object) {
        LinkedList<String> i = new LinkedList<>();
        object.get("files").getAsJsonArray().asList().forEach(j -> i.add(j.getAsString()));
        files = i;
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("files");
        } catch (Throwable e) {
            return false;
        }
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, S2CGetFileList.class);
    }

    public JsonObject ToJsonObject() {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray(files.size());
        files.forEach(array::add);
        object.add("files", array);
        return object;
    }
}
