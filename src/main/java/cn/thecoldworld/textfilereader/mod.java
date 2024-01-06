package cn.thecoldworld.textfilereader;

import cn.thecoldworld.textfilereader.api.command.CommandRegistrants;
import cn.thecoldworld.textfilereader.api.event.Events;
import cn.thecoldworld.textfilereader.api.funcitons;
import cn.thecoldworld.textfilereader.networking.NetworkingFunctions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class mod implements ModInitializer {
    @Override
    public final void onInitialize() {
        try {
            FileIO.RootDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
            if (!FileIO.RootDir.isAbsolute()) throw new RuntimeException("Cannot get Data path");
            FileIO.GlobalTextPath = Paths.get(FileIO.RootDir.toString(), "Texts");
            FileIO.ConfigPath = Paths.get(FileIO.RootDir.toString(), "config", "TextFileReader.json");
            if (!FileIO.GlobalTextPath.toFile().exists() || !FileIO.GlobalTextPath.toFile().isDirectory()) {
                try {
                    variables.Log.info("Missing global text data directory,starting crate");
                    Files.createDirectory(FileIO.GlobalTextPath);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot create global text data directory ", e);
                }
            }
            variables.ModSettings = Settings.GetSettings();
            try {
                funcitons.CreateFile(Paths.get(FileIO.GlobalTextPath.toString(), "permissions.json").toFile(), "{\"Files\":[]}");
            } catch (Exception e) {
                if (!e.getMessage().equals("File exist")) throw new RuntimeException(e);
            }
            FilePermissions.GlobalTextPermission = FilePermissions.InitPermission(Paths.get(FileIO.GlobalTextPath.toString(), "permissions.json"));
            ServerWorldEvents.LOAD.register((funcitons::OnWorldLoading));
            variables.scheduledExecutorService = Executors.newScheduledThreadPool(variables.ModSettings.getThreads(), variables.threadPool);
            variables.scheduledExecutorService.scheduleAtFixedRate(funcitons::OnUpdateThread, 0, 500, TimeUnit.MICROSECONDS);
            ServerWorldEvents.UNLOAD.register(((server, world) -> variables.IsWorldLoaded = false));
            Events.C2SSendPacketEvent.EVENT.Register((server, player, handler, sendNetworkPackage, responseSender, identifier) -> NetworkingFunctions.OnReceiveSedPackage(server, player, identifier, sendNetworkPackage),
                    variables.Identifiers.TextFileNetworkingIdentifier, variables.Identifiers.TextFileListNetworkingIdentifier, variables.Identifiers.ControlingIdentifier);
            CommandRegistrants.RegisterRegistrantFunction(Command::RegisterOthers);
            funcitons.RegisterServerNetworkReceivers(
                    variables.Identifiers.TextFileListNetworkingIdentifier,
                    variables.Identifiers.TextFileNetworkingIdentifier,
                    variables.Identifiers.ControlingIdentifier
            );
            CommandRegistrationCallback.EVENT.register(Command::Register);
        } catch (RuntimeException e) {
            throw new CrashException(new CrashReport("Failed initialize TextFileReader mod!", e));
        }
    }
}
