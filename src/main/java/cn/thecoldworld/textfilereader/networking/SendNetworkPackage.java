package cn.thecoldworld.textfilereader.networking;

import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class SendNetworkPackage extends NetWorkPackage {

    public final boolean NeedResponse;
    public final String ResponseType;


    public SendNetworkPackage(JsonObject body, String responseType, String identifier, boolean needResponse) {
        super(body, identifier);
        NeedResponse = needResponse;
        ResponseType = responseType;
    }

    public SendNetworkPackage(JsonObject body, String identifier, boolean needResponse) {
        super(body, identifier);
        NeedResponse = needResponse;
        ResponseType = "Json";
    }

    public static @NotNull SendNetworkPackage GetPackage(@NotNull String Json) {
        return variables.defaultGson.fromJson(Json, SendNetworkPackage.class);
    }

    public static @NotNull SendNetworkPackage GetPackage(@NotNull ByteBuf bytes, @Nullable Charset Charset) {
        Charset charset = Charset == null ? StandardCharsets.UTF_8 : Charset;
        return variables.defaultGson.fromJson(new String(bytes.array(), charset).trim(), SendNetworkPackage.class);
    }

    public static boolean IsSendPackage(String Json) {
        return variables.defaultGson.fromJson(Json, SendNetworkPackage.class).ResponseType != null;
    }

    public static boolean IsSendPackage(@NotNull ByteBuf bytes, @Nullable Charset Charset) {
        Charset charset;
        if (Charset == null) charset = StandardCharsets.UTF_8;
        else charset = Charset;
        return variables.defaultGson.fromJson(new String(bytes.array(), charset).trim(), SendNetworkPackage.class).ResponseType != null;
    }

    public String ToJson() {
        return variables.defaultGson.toJson(this, this.getClass());
    }
}
