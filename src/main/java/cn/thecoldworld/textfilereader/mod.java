package cn.thecoldworld.textfilereader;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class mod implements ModInitializer {
    @Override
    public void onInitialize() {
        variables.Log.info("Start Initialize TextFileReader mod");
        FileIO.Rootdir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        if ( !FileIO.Rootdir.isAbsolute() ) throw new RuntimeException("Cannot get Data path");
        FileIO.GlobalTextPath = Paths.get(FileIO.Rootdir.toString(), "Texts");
        FileIO.ConfigPath = Paths.get(FileIO.Rootdir.toString(), "config", "TextFileReader.json");
        if ( !FileIO.GlobalTextPath.toFile().exists() || !FileIO.GlobalTextPath.toFile().isDirectory() ) {
            try {
                variables.Log.info("Missing global text data directory,starting crate");
                Files.createDirectory(FileIO.GlobalTextPath);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create global text data directory ,exception message:" + e.getMessage());
            }
        }
        variables.ModSettings =Settings.GetSettings();
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> filereader.Init(dispatcher)));
        try {
            funcitons.CreateFile(Paths.get(FileIO.GlobalTextPath.toString(), "permissions.json").toFile(),"{\"Files\":[]}");
        } catch (Exception e) {
            if(!e.getMessage().equals("File exist")) throw new RuntimeException(e);
        }
        FilePermissions.GlobalTextPermission = FilePermissions.InitPermission(Paths.get(FileIO.GlobalTextPath.toString(), "permissions.json"));
        variables.threadPool=new ThreadPool();
        ServerWorldEvents.LOAD.register(((server, world) -> {
            Path PermissionPath = server.getSavePath(WorldSavePath.ROOT).toAbsolutePath().resolve("Texts").resolve("permissions.json").normalize();
            if ( !PermissionPath.getParent().normalize().toFile().exists() || !PermissionPath.getParent().normalize().toFile().isDirectory() ) {
                try {
                    funcitons.CreateDir(PermissionPath.getParent().normalize());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    if(!e.getMessage().equals("Dir exist")) throw new RuntimeException(e);
                }
            }
            if ( !PermissionPath.toFile().exists() || !PermissionPath.toFile().isFile() ) {
                variables.Log.info("Missing world text data permission file,starting crate");
                try {
                    funcitons.CreateFile(PermissionPath.toFile(),"{\"Files\":[]}");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    if(!e.getMessage().equals("File exist")) throw new RuntimeException(e);
                }
            }
            FilePermissions.WorldTextPermission = FilePermissions.InitPermission(PermissionPath);
            variables.IsWorldLoaded=true;
        }));
        variables.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(variables.threadPool);
        variables.scheduledExecutorService.scheduleAtFixedRate(() -> {
            if(!variables.IsWorldLoaded)return;
            try {
                if ( variables.TickEvent.isEmpty() ) {
                    FilePermissions.GlobalTextPermission.UpdateFile();
                    FilePermissions.WorldTextPermission.UpdateFile();
                    FilePermissions.GlobalTextPermission.UpToFile();
                    FilePermissions.WorldTextPermission.UpToFile();
                    variables.ModSettings.UptoFile();
                    return;
                }
                variables.TickEvent.take().run();
            } catch (Exception e)
            {
                variables.Log.error("",e);
            }
        },0,500, TimeUnit.MICROSECONDS);
        ServerWorldEvents.UNLOAD.register(((server, world) -> variables.IsWorldLoaded=false));
        variables.Log.info("Initialize TextFileReader mod done");
    }
}
