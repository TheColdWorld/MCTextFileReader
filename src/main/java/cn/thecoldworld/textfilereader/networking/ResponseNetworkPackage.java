package cn.thecoldworld.textfilereader.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ResponseNetworkPackage {

    private final @Nullable NetWorkPackage netWorkPackage;
    private final @Nullable String ResponseID;

    public ResponseNetworkPackage(@NotNull String Json) {
        Gson internalGson = new GsonBuilder().create();
        JsonObject jsonObject = internalGson.fromJson(Json, JsonObject.class);
        netWorkPackage = internalGson.fromJson(jsonObject.getAsJsonObject("netWorkPackage").toString(), NetWorkPackage.class);
        ResponseID = jsonObject.get("ResponseID").getAsString();
    }

    public ResponseNetworkPackage(@NotNull JsonObject ResponsePackageInformation, @NotNull String ResponseID) {
        this.ResponseID = ResponseID;
        this.netWorkPackage = new SendNetworkPackage(ResponsePackageInformation, false);
    }

    public static @NotNull ResponseNetworkPackage GetPackage(@NotNull String Json) {
        return new ResponseNetworkPackage(Json);
    }

    public static @NotNull ResponseNetworkPackage GetPackage(@NotNull PacketByteBuf bytes, @Nullable Charset Charset) {
        Charset charset;
        if ( Charset == null ) charset = StandardCharsets.UTF_8;
        else charset = Charset;
        return new ResponseNetworkPackage(new String(bytes.array(), charset).trim());
    }

    public String ToJson() {
        return new GsonBuilder().create().toJson(this, ResponseNetworkPackage.class);
    }

    public @Nullable NetWorkPackage getNetWorkPackage() {
        return netWorkPackage;
    }

    public @Nullable String getResponseID() {
        return ResponseID;
    }
}
