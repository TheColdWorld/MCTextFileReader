package cn.thecoldworld.textfilereader.networking.jsonformats;

import com.google.gson.JsonObject;

public interface ToJsonAble {
    String ToJson();

    JsonObject ToJsonObject();
}
