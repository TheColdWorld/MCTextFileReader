package cn.thecoldworld.textfilereader.networking.jsonformats;

import cn.thecoldworld.textfilereader.ServerFileSource;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;

public final class C2SGetFileContent implements NetworkPackageContent {
    public final String FileName;
    public final String fileSource;

    public C2SGetFileContent(String fileName, ServerFileSource serverFileSource) {
        FileName = fileName;
        this.fileSource = switch (serverFileSource) {
            case global -> "Global";
            case save -> "Save";
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

    public ServerFileSource GetFileSource() {
        return switch (fileSource.toLowerCase()) {
            case "global" -> ServerFileSource.global;
            case "save" -> ServerFileSource.save;
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
