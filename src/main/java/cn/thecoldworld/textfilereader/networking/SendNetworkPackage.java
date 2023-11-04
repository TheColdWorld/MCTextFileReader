package cn.thecoldworld.textfilereader.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SendNetworkPackage extends NetWorkPackage {

    public final boolean NeedResponse;
    public transient final Gson internalGson;

    public SendNetworkPackage() {
        super(new JsonObject());
        NeedResponse = false;
        internalGson = new GsonBuilder().create();
    }

    public SendNetworkPackage(Gson gson) {
        super(new JsonObject());
        NeedResponse = false;
        internalGson = gson;
    }

    public SendNetworkPackage(JsonObject body, boolean needResponse) {
        super(body);
        NeedResponse = needResponse;
        internalGson = new GsonBuilder().create();
    }

    public static @NotNull SendNetworkPackage GetPackage(@NotNull String Json) {
        return new GsonBuilder().create().fromJson(Json, SendNetworkPackage.class);
    }

    public static @NotNull SendNetworkPackage GetPackage(@NotNull PacketByteBuf bytes, @Nullable Charset Charset) {
        Charset charset;
        if ( Charset == null ) charset = StandardCharsets.UTF_8;
        else charset = Charset;
        return new GsonBuilder().create().fromJson(new String(bytes.array(), charset).trim(), SendNetworkPackage.class);
    }

    public String ToJson() {
        return internalGson.toJson(this, this.getClass());
    }
}
