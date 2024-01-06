package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.ServerFileSource;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public final class C2SGetFileList implements NetworkPackageContent {

    public final String ListFileSource;

    public C2SGetFileList(ServerFileSource FileSource) {
        this.ListFileSource = switch (FileSource) {
            case global -> "Global";
            case save -> "Save";
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

    public ServerFileSource GetFileSource() {
        return switch (ListFileSource.toLowerCase()) {
            case "global" -> ServerFileSource.global;
            case "save" -> ServerFileSource.save;
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
