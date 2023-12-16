package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.FileSource;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class C2SGetFileList implements NetworkPackageContent {

    public final String ListFileSource;

    public C2SGetFileList(cn.thecoldworld.textfilereader.FileSource fileSource) {
        this.ListFileSource = switch (fileSource) {
            case global -> "Global";
            case save -> "Save";
            case local -> "";
        };
    }

    public C2SGetFileList(String Json) {
        C2SGetFileContent i = variables.defaultGson.fromJson(Json, C2SGetFileContent.class);
        if (i.FileName == null || i.fileSource == null) {
            ListFileSource = "";
        } else {
            ListFileSource = i.fileSource;
        }
    }

    public C2SGetFileList(@NotNull JsonObject jsonObject) {
        ListFileSource = jsonObject.has("ListFileSource") ? jsonObject.get("ListFileSource").getAsString() : "";
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("ListFileSource");
        } catch (Throwable e) {
            return false;
        }
    }

    public FileSource GetFileSource() {
        return switch (ListFileSource.toLowerCase()) {
            case "global" -> FileSource.global;
            case "save" -> FileSource.save;
            default -> throw new IllegalStateException("Unexpected value: " + ListFileSource);
        };
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, C2SGetFileList.class);
    }

    public JsonObject ToJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("ListFileSource", ListFileSource);
        return object;
    }
}
