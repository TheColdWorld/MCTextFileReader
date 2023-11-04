package cn.thecoldworld.textfilereader;

import cn.thecoldworld.textfilereader.networking.Events;
import cn.thecoldworld.textfilereader.networking.Identifiers;
import cn.thecoldworld.textfilereader.networking.NetWorkPackage;
import cn.thecoldworld.textfilereader.networking.Tasks;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class mod implements ModInitializer {
    @Override
    public void onInitialize() {
        FileIO.Rootdir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        if ( !FileIO.Rootdir.isAbsolute() ) throw new RuntimeException("Cannot get Data path");
        FileIO.GlobalTextPath = Paths.get(FileIO.Rootdir.toString(), "Texts");
        FileIO.ConfigPath = Paths.get(FileIO.Rootdir.toString(), "config", "TextFileReader.json");
        if ( !FileIO.GlobalTextPath.toFile().exists() || !FileIO.GlobalTextPath.toFile().isDirectory() ) {
            try {
                variables.Log.info("Missing global text data directory,starting crate");
                Files.createDirectory(FileIO.GlobalTextPath);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create global text data directory ", e);
            }
        }
        variables.ModSettings = Settings.GetSettings();
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> filereader.Init(dispatcher)));
        try {
            funcitons.CreateFile(Paths.get(FileIO.GlobalTextPath.toString(), "permissions.json").toFile(), "{\"Files\":[]}");
        } catch (Exception e) {
            if ( !e.getMessage().equals("File exist") ) throw new RuntimeException(e);
        }
        FilePermissions.GlobalTextPermission = FilePermissions.InitPermission(Paths.get(FileIO.GlobalTextPath.toString(), "permissions.json"));
        variables.threadPool = new ThreadPool();
        ServerWorldEvents.LOAD.register((funcitons::OnWorldLoading));
        variables.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(variables.threadPool);
        variables.scheduledExecutorService.scheduleAtFixedRate(funcitons::OnUpdateThread, 0, 500, TimeUnit.MICROSECONDS);
        ServerWorldEvents.UNLOAD.register(((server, world) -> variables.IsWorldLoaded = false));
        Identifiers.TextFileIdentifier = new Identifier("textfilereader", "transfer/textfile");
        Identifiers.DebugFileIdentifier = new Identifier("textfilereader", "debug/test");
        Tasks.TaskPool_Server = new LinkedList<>();
        ServerPlayNetworking.registerGlobalReceiver(Identifiers.TextFileIdentifier,
                (server, player, handler, buf, responseSender) -> Tasks.GetNetPackageCallback(server, player, handler, buf, responseSender, Identifiers.TextFileIdentifier));
        ServerPlayNetworking.registerGlobalReceiver(Identifiers.DebugFileIdentifier,
                (server, player, handler, buf, responseSender) -> {
                    NetWorkPackage netWorkPackage = new GsonBuilder().create().fromJson(new String(buf.array(), StandardCharsets.UTF_8).trim(), NetWorkPackage.class);
                    JsonObject object = new JsonObject();
                    object.addProperty("tmp", "114514");
                    Tasks.Task.Run(player, object, netWorkPackage.ID, Identifiers.DebugFileIdentifier);
                });
        Events.S2CPackageEvent = new Events.Event<>();
        Events.C2SPackageEvent = new Events.Event<>();
    }
}
