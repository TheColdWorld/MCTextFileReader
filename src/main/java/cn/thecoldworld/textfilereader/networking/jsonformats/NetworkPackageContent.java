package cn.thecoldworld.textfilereader.networking.jsonformats;

import com.google.gson.JsonObject;

public interface NetworkPackageContent {
    String ToJson();

    JsonObject ToJsonObject();
}
