package cn.thecoldworld.textfilereader.networking;

import cn.thecoldworld.textfilereader.networking.jsonformats.NetworkPackageContent;
import cn.thecoldworld.textfilereader.tasks.Task;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ClientNetWorkingTask extends Task<S2CArguments> {
    @Environment(EnvType.CLIENT)
    public static LinkedList<ClientNetWorkingTask> TaskPool = new LinkedList<>();
    public final String PackageID;
    public final String PackageBody;

    public ClientNetWorkingTask(JsonObject SendPackageInformation, Identifier identifier, List<Consumer<S2CArguments>> callbacks) {
        super(callbacks);
        SendNetworkPackage p = new SendNetworkPackage(SendPackageInformation, "Json", true);
        PackageID = p.ID;
        PackageBody = p.ToJson();
        super.SetAction(() ->
        {
            var PackageByte = PacketByteBufs.create();
            PackageByte.writeBytes(PackageBody.getBytes(StandardCharsets.UTF_8));
            TaskPool.add(this);
            ClientPlayNetworking.send(identifier, PackageByte);
        });
    }

    public ClientNetWorkingTask(NetworkPackageContent SendPackageInformation, Identifier identifier, boolean needResponse) {
        super();
        SendNetworkPackage p = new SendNetworkPackage(SendPackageInformation.ToJsonObject(), "Json", needResponse);
        PackageID = p.ID;
        PackageBody = p.ToJson();
        super.SetAction(() ->
        {
            var PackageByte = PacketByteBufs.create();
            PackageByte.writeBytes(PackageBody.getBytes(StandardCharsets.UTF_8));
            if (needResponse) TaskPool.add(this);
            ClientPlayNetworking.send(identifier, PackageByte);
        });
    }

    public ClientNetWorkingTask(NetworkPackageContent ResponsePackageInformation, String ResponseID, Identifier identifier) {
        super();
        ResponseNetworkPackage p = new ResponseNetworkPackage(ResponsePackageInformation.ToJsonObject(), ResponseID);
        PackageID = p.ID;
        PackageBody = p.ToJson();
        super.SetAction(() ->
        {
            var PackageByte = PacketByteBufs.create();
            PackageByte.writeBytes(PackageBody.getBytes(StandardCharsets.UTF_8));
            ClientPlayNetworking.send(identifier, PackageByte);
        });
    }

    public static ClientNetWorkingTask Run(NetworkPackageContent SendPackageInformation, Identifier identifier, List<Consumer<S2CArguments>> callbacks) {
        ClientNetWorkingTask i = new ClientNetWorkingTask(SendPackageInformation.ToJsonObject(), identifier, callbacks);
        i.Start();
        return i;
    }

    public static ClientNetWorkingTask Run(NetworkPackageContent SendPackageInformation, Identifier identifier, boolean needResponse) {
        ClientNetWorkingTask i = new ClientNetWorkingTask(SendPackageInformation, identifier, needResponse);
        i.Start();
        return i;
    }

    public static ClientNetWorkingTask Run(NetworkPackageContent ResponsePackageInformation, String ResponseId, Identifier identifier) {
        ClientNetWorkingTask i = new ClientNetWorkingTask(ResponsePackageInformation, ResponseId, identifier);
        i.Start();
        return i;
    }
}
