package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.FileSource;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;

public class C2SGetFileContent implements NetworkPackageContent {
    public final String FileName;
    public final String fileSource;

    public C2SGetFileContent(String fileName, FileSource fileSource) throws Exception {
        FileName = fileName;
        this.fileSource = switch (fileSource) {
            case global -> "Global";
            case save -> "Save";
            case local -> throw new Exception("Cannot use FileSource.local");
        };
    }

    public C2SGetFileContent(String Json) {
        C2SGetFileContent i = variables.defaultGson.fromJson(Json, C2SGetFileContent.class);
        if (i.FileName == null || i.fileSource == null) {
            FileName = "";
            fileSource = "";
        } else {
            FileName = i.FileName;
            fileSource = i.fileSource;
        }
    }

    public C2SGetFileContent(JsonObject Json) {
        if (!Json.has("FileName") || !Json.has("ListFileSource")) {
            FileName = "";
            fileSource = "";
        } else {
            FileName = Json.get("FileName").getAsString();
            fileSource = Json.get("ListFileSource").getAsString();
        }
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("FileName") &&
                    variables.defaultGson.fromJson(Json, JsonObject.class).has("ListFileSource");
        } catch (Throwable e) {
            return false;
        }
    }

    public FileSource GetFileSource() {
        return switch (fileSource.toLowerCase()) {
            case "global" -> FileSource.global;
            case "save" -> FileSource.save;
            default -> throw new IllegalStateException("Unexpected value: " + fileSource);
        };
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, C2SGetFileContent.class);
    }

    public JsonObject ToJsonObject() {
        var i = new JsonObject();
        i.addProperty("FileName", FileName);
        i.addProperty("ListFileSource", fileSource);
        return i;
    }
}
