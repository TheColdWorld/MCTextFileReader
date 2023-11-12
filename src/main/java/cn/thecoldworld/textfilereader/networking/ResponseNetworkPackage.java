package cn.thecoldworld.textfilereader.networking;

import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ResponseNetworkPackage extends NetWorkPackage {

    public final String ResponseID;

    public ResponseNetworkPackage(JsonObject body, String requestID) {
        super(body, "TextFileReader::Response");
        this.ResponseID = requestID;
    }

    public static ResponseNetworkPackage GetPackage(String Json) {
        return variables.defaultGson.fromJson(Json, ResponseNetworkPackage.class);
    }

    public static ResponseNetworkPackage GetPackage(ByteBuf byteBuf, Charset charset) {
        return variables.defaultGson.fromJson(new String(byteBuf.array(), charset).trim(), ResponseNetworkPackage.class);
    }

    public static boolean IsResponse(String Json) {
        return variables.defaultGson.fromJson(Json, ResponseNetworkPackage.class).ResponseID == null;
    }

    public static boolean IsResponse(ByteBuf byteBuf, @Nullable Charset Charset) {
        Charset charset;
        if ( Charset == null ) charset = StandardCharsets.UTF_8;
        else charset = Charset;
        return variables.defaultGson.fromJson(new String(byteBuf.array(), charset).trim(), ResponseNetworkPackage.class).ResponseID != null;
    }

    public static boolean IsResponse(JsonObject Json) {
        return Json.get("ResponseID") == null;
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, ResponseNetworkPackage.class);
    }
}
