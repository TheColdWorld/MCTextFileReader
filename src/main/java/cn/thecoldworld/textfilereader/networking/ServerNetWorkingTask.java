package cn.thecoldworld.textfilereader.networking;

import cn.thecoldworld.textfilereader.api.tasks.OutReturnTask;
import cn.thecoldworld.textfilereader.networking.jsonformats.NetworkPackageContent;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public final class ServerNetWorkingTask extends OutReturnTask<C2SArguments> {
    public static final LinkedList<ServerNetWorkingTask> TaskPool = new LinkedList<>();
    public final String PackageID;
    public final String PackageBody;

    public ServerNetWorkingTask(NetworkPackageContent SendPackageInformation, ServerPlayerEntity sendto, Identifier identifier, List<Consumer<C2SArguments>> callbacks) {
        super(callbacks);
        SendNetworkPackage p = new SendNetworkPackage(SendPackageInformation, "Json", true);
        PackageID = p.ID;
        PackageBody = p.ToJson();
        super.SetAction(() ->
        {
            var PackageByte = PacketByteBufs.create();
            PackageByte.writeBytes(PackageBody.getBytes(StandardCharsets.UTF_8));
            TaskPool.add(this);
            ServerPlayNetworking.send(sendto, identifier, PackageByte);
        });
    }

    public ServerNetWorkingTask(NetworkPackageContent SendPackageInformation, ServerPlayerEntity sendto, Identifier identifier) {
        super();
        SendNetworkPackage p = new SendNetworkPackage(SendPackageInformation, "Json", false);
        PackageID = p.ID;
        PackageBody = p.ToJson();
        super.SetAction(() ->
        {
            var PackageByte = PacketByteBufs.create();
            PackageByte.writeBytes(PackageBody.getBytes(StandardCharsets.UTF_8));
            ServerPlayNetworking.send(sendto, identifier, PackageByte);
        });
    }

    public ServerNetWorkingTask(NetworkPackageContent ResponsePackageInformation, String ResponseID, ServerPlayerEntity sendto, Identifier identifier) {
        super();
        ResponseNetworkPackage p = new ResponseNetworkPackage(ResponsePackageInformation, ResponseID);
        PackageID = p.ID;
        PackageBody = p.ToJson();
        super.SetAction(() ->
        {
            var PackageByte = PacketByteBufs.create();
            PackageByte.writeBytes(PackageBody.getBytes(StandardCharsets.UTF_8));
            ServerPlayNetworking.send(sendto, identifier, PackageByte);
        });
    }

    public static ServerNetWorkingTask Run(NetworkPackageContent SendPackageInformation, ServerPlayerEntity sendto, Identifier identifier, List<Consumer<C2SArguments>> callbacks) {
        ServerNetWorkingTask i = new ServerNetWorkingTask(SendPackageInformation, sendto, identifier, callbacks);
        i.Start();
        return i;
    }

    public static ServerNetWorkingTask Run(NetworkPackageContent SendPackageInformation, ServerPlayerEntity sendto, Identifier identifier) {
        ServerNetWorkingTask i = new ServerNetWorkingTask(SendPackageInformation, sendto, identifier);
        i.Start();
        return i;
    }

    public static ServerNetWorkingTask Run(NetworkPackageContent ResponsePackageInformation, String ResponseId, ServerPlayerEntity sendto, Identifier identifier) {
        ServerNetWorkingTask i = new ServerNetWorkingTask(ResponsePackageInformation, ResponseId, sendto, identifier);
        i.Start();
        return i;
    }
}
