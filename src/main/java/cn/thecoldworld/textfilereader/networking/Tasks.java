package cn.thecoldworld.textfilereader.networking;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.function.Consumer;

public final class Tasks {
    public static LinkedList<Task> TaskPool_Server;

    public static LinkedList<Task> TaskPool_Client;


    public static void GetNetPackageCallback(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier textFileIdentifier) {
        ResponseNetworkPackage responseNetworkPackage = ResponseNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8);
        if ( responseNetworkPackage.getNetWorkPackage() == null || responseNetworkPackage.getResponseID() == null )
            return;
        TaskPool_Server.stream()
                .filter(i -> i.envType == EnvType.SERVER)
                .filter(i -> !i.Returned)
                .filter(i -> i.PackageID.equals(responseNetworkPackage.getResponseID()))
                .forEach(i -> i.Return(new Task.Arguments(server, player, responseNetworkPackage.getNetWorkPackage().Body)));
        Events.C2SPackageEvent.InvokeAsync(new C2SEventArgs(server, player, handler, buf, responseSender, textFileIdentifier));
    }


    public static void GetNetPackageCallback(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier textFileIdentifier) {
        ResponseNetworkPackage responseNetworkPackage = ResponseNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8);
        if ( responseNetworkPackage.getNetWorkPackage() == null || responseNetworkPackage.getResponseID() == null )
            return;
        TaskPool_Client.stream()
                .filter(i -> i.envType == EnvType.CLIENT)
                .filter(i -> !i.Returned)
                .filter(i -> i.PackageID.equals(responseNetworkPackage.getResponseID()))
                .forEach(i -> i.Return(new Task.Arguments(client, responseNetworkPackage.getNetWorkPackage().Body)));
        Events.S2CPackageEvent.InvokeAsync(new S2CEventArgs(client, handler, buf, responseSender, textFileIdentifier));
    }

    public static final class Task {
        public final boolean NeedResponse;
        public final String sendnetWorkPackage;
        public final String PackageID;
        public final Identifier identifier;
        public final EnvType envType;
        private final Consumer<Arguments> callback;
        private boolean Returned;

        public Task(JsonObject SendPackageInformation, Identifier identifier, EnvType envType, Consumer<Arguments> callback) {
            SendNetworkPackage p = new SendNetworkPackage(SendPackageInformation, true);
            sendnetWorkPackage = p.ToJson();
            this.identifier = identifier;
            this.callback = callback;
            this.envType = envType;
            PackageID = p.ID;
            NeedResponse = p.NeedResponse;
        }

        public Task(JsonObject SendPackageInformation, Identifier identifier, EnvType envType) {
            SendNetworkPackage p = new SendNetworkPackage(SendPackageInformation, true);
            sendnetWorkPackage = p.ToJson();
            this.identifier = identifier;
            this.callback = arguments -> {
            };
            this.envType = envType;
            PackageID = p.ID;
            NeedResponse = p.NeedResponse;
        }

        public Task(JsonObject ResponsePackageInformation, String ResponseID, Identifier identifier, EnvType envType, Consumer<Arguments> callback) {
            ResponseNetworkPackage p = new ResponseNetworkPackage(ResponsePackageInformation, ResponseID);
            sendnetWorkPackage = p.ToJson();
            this.identifier = identifier;
            this.callback = callback;
            this.envType = envType;
            PackageID = p.getNetWorkPackage().ID;
            NeedResponse = false;
        }

        public Task(JsonObject ResponsePackageInformation, String ResponseID, Identifier identifier, EnvType envType) {
            ResponseNetworkPackage p = new ResponseNetworkPackage(ResponsePackageInformation, ResponseID);
            sendnetWorkPackage = p.ToJson();
            this.identifier = identifier;
            this.callback = arguments -> {
            };
            this.envType = envType;
            PackageID = p.getNetWorkPackage().ID;
            NeedResponse = false;
        }

        public static void Run(ServerPlayerEntity sendto, JsonObject SendPackageInformation, Identifier identifier, Consumer<Arguments> callback) {
            (new Task(SendPackageInformation, identifier, EnvType.SERVER, callback)).Send(sendto);
        }

        public static void Run(ServerPlayerEntity sendto, JsonObject SendPackageInformation, Identifier identifier) {
            (new Task(SendPackageInformation, identifier, EnvType.SERVER)).Send(sendto);
        }

        public static void Run(ServerPlayerEntity sendto, JsonObject SendPackageInformation, String responseID, Identifier identifier) {
            (new Task(SendPackageInformation, responseID, identifier, EnvType.SERVER)).Send(sendto);
        }

        public static void Run(JsonObject SendPackageInformation, String responseID, Identifier identifier, Consumer<Arguments> callback) {
            (new Task(SendPackageInformation, responseID, identifier, EnvType.CLIENT, callback)).Send();
        }

        public static void Run(JsonObject SendPackageInformation, Identifier identifier, Consumer<Arguments> callback) {
            (new Task(SendPackageInformation, identifier, EnvType.CLIENT, callback)).Send();
        }

        public static void Run(JsonObject SendPackageInformation, Identifier identifier) {
            (new Task(SendPackageInformation, identifier, EnvType.CLIENT)).Send();
        }

        public boolean isReturned() {
            return Returned;
        }

        public void Send() {
            if ( envType == EnvType.CLIENT ) {
                var packagebyte = PacketByteBufs.create();
                packagebyte.writeBytes(sendnetWorkPackage.getBytes(StandardCharsets.UTF_8));
                if ( NeedResponse ) TaskPool_Client.add(this);
                ClientPlayNetworking.send(identifier, packagebyte);
            }

        }

        public void Send(ServerPlayerEntity sendto) {
            if ( envType == EnvType.SERVER ) {
                var packagebyte = PacketByteBufs.create();
                packagebyte.writeBytes(sendnetWorkPackage.getBytes(StandardCharsets.UTF_8));
                if ( NeedResponse ) TaskPool_Server.add(this);
                ServerPlayNetworking.send(sendto, identifier, packagebyte);
            }
        }

        public void Return(Arguments arguments) {
            if ( Returned ) {
                if ( envType == EnvType.CLIENT ) {
                    TaskPool_Client.remove(this);
                }
                if ( envType == EnvType.SERVER ) {
                    TaskPool_Server.remove(this);
                }
            }
            Returned = true;
            callback.accept(arguments);
        }

        public static final class Arguments {
            public final EnvType envType;
            public final JsonObject value;
            private final MinecraftClient client;
            private final MinecraftServer server;
            private final ServerPlayerEntity c2sSender;

            public Arguments(MinecraftClient client, JsonObject value) {
                this.client = client;
                this.value = value;
                envType = EnvType.CLIENT;
                server = null;
                c2sSender = null;
            }

            public Arguments(MinecraftServer server, ServerPlayerEntity sender, JsonObject value) {
                this.value = value;
                client = null;
                this.server = server;
                c2sSender = sender;
                envType = EnvType.SERVER;
            }


            public MinecraftClient getClient() {
                return client;
            }


            public MinecraftServer getServer() {
                return server;
            }


            public ServerPlayerEntity getC2sSender() {
                return c2sSender;
            }
        }

    }
}
