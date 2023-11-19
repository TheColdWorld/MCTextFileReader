package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.FileSource;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;

public class C2SGetContent implements ToJsonAble {
    public final String FileName;
    public final String fileSource;

    public C2SGetContent(String fileName, FileSource fileSource) {
        FileName = fileName;
        this.fileSource = switch (fileSource) {
            case global -> "Global";
            case save -> "Save";
        };
    }

    public C2SGetContent(String Json) {
        C2SGetContent i = variables.defaultGson.fromJson(Json, C2SGetContent.class);
        if ( i.FileName == null || i.fileSource == null ) {
            FileName = "";
            fileSource = "";
        } else {
            FileName = i.FileName;
            fileSource = i.fileSource;
        }
    }

    public C2SGetContent(JsonObject Json) {
        if ( !Json.has("FileName") || !Json.has("fileSource") ) {
            FileName = "";
            fileSource = "";
        } else {
            FileName = Json.get("FileName").getAsString();
            fileSource = Json.get("fileSource").getAsString();
        }
    }

    static public boolean IsInstance(String Json) {
        try {
            return variables.defaultGson.fromJson(Json, JsonObject.class).has("FileName") &&
                    variables.defaultGson.fromJson(Json, JsonObject.class).has("fileSource");
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
        return variables.defaultGson.toJson(this, C2SGetContent.class);
    }

    public JsonObject ToJsonObject() {
        var i = new JsonObject();
        i.addProperty("FileName", FileName);
        i.addProperty("fileSource", fileSource);
        return i;
    }
}
