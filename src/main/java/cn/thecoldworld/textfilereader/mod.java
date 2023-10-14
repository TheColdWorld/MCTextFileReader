package cn.thecoldworld.textfilereader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

public class mod implements ModInitializer {
    public static Logger Log = LoggerFactory.getLogger("TextFileReader");
    public static BlockingDeque<Runnable> TickEvent = new LinkedBlockingDeque<>();
    public static ScheduledExecutorService scheduledExecutorService=null;
    public static ThreadPool threadPool=null;
    public  static boolean IsWorldLoaded=false;

    public static JsonElement GetConfig(String Name) {
        Gson gson = new com.google.gson.Gson();
        try {
            JsonObject configjson = gson.fromJson(String.join("", Files.readAllLines(FileIO.ConfigPath)), JsonObject.class);
            return configjson.get(Name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onInitialize() {
        Log.info("Start Initialize TextFileReader mod");
        FileIO.Rootdir = Paths.get(System.getProperty("user.dir"));
        if ( !FileIO.Rootdir.isAbsolute() ) throw new RuntimeException("Cannot get Data path");
        FileIO.GlobalTextPath = Paths.get(FileIO.Rootdir.toString(), "Texts");
        FileIO.ConfigPath = Paths.get(FileIO.Rootdir.toString(), "config", "TextFileReader.json");
        if ( !FileIO.GlobalTextPath.toFile().exists() || !FileIO.GlobalTextPath.toFile().isDirectory() ) {
            try {
                Log.info("Missing global text data directory,starting crate");
                Files.createDirectory(FileIO.GlobalTextPath);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create global text data directory ,exception message:" + e.getMessage());
            }
        }
        if ( !FileIO.ConfigPath.toFile().exists() || !FileIO.ConfigPath.toFile().isFile() ) {
            try {
                Log.info("Missing config file,starting crate");
                Files.createFile(FileIO.ConfigPath);
                FileWriter fr = new FileWriter(FileIO.ConfigPath.toFile());
                fr.write("{\"Segmentedoutput\":false}");
                fr.flush();
                fr.close();
            } catch (Exception e) {
                throw new RuntimeException("Cannot create global text data directory ,exception message:" + e.getMessage());
            }
        }

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> filereader.Init(dispatcher)));
        try {
            FileWriter fr = new FileWriter(Paths.get(FileIO.GlobalTextPath.toString(), "permissions.json").toFile());
            fr.write("{\"Files\":[]}");
            fr.flush();
            fr.close();
            FilePermissions.GlobalTextPermission = FilePermissions.InitPermission(Paths.get(FileIO.GlobalTextPath.toString(), "permissions.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        threadPool=new ThreadPool();
        ServerWorldEvents.LOAD.register(((server, world) -> {
            Path PermissionPath = server.getSavePath(WorldSavePath.ROOT).toAbsolutePath().resolve("Texts").resolve("permissions.json").normalize();
            if ( !PermissionPath.getParent().normalize().toFile().exists() || !PermissionPath.getParent().normalize().toFile().isDirectory() ) {
                try {
                    Files.createDirectory(PermissionPath.getParent().normalize());
                    mod.Log.info("Missing world text data permission file,starting crate");
                    Files.createFile(PermissionPath);
                    FileWriter fr = new FileWriter(PermissionPath.toFile());
                    fr.write("{\"Files\":[]}");
                    fr.flush();
                    fr.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if ( !PermissionPath.toFile().exists() || !PermissionPath.toFile().isFile() ) {
                mod.Log.info("Missing world text data permission file,starting crate");
                try {
                    Files.createFile(PermissionPath);
                    FileWriter fr = new FileWriter(PermissionPath.toFile());
                    fr.write("{\"Files\":[]}");
                    fr.flush();
                    fr.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                FilePermissions.WorldTextPermission = FilePermissions.InitPermission(PermissionPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mod.IsWorldLoaded=true;
        }));
        mod.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(mod.threadPool);
        mod.scheduledExecutorService.scheduleAtFixedRate(() -> {
            if(!mod.IsWorldLoaded)return;
            try {
                if ( mod.TickEvent.isEmpty() ) {
                    FilePermissions.GlobalTextPermission.UpdateFile();
                    FilePermissions.WorldTextPermission.UpdateFile();
                    FilePermissions.GlobalTextPermission.UpToFile();
                    FilePermissions.WorldTextPermission.UpToFile();
                    return;
                }
                mod.TickEvent.take().run();
            } catch (Exception e)
            {
                mod.Log.error("",e);
            }
        },0,500, TimeUnit.MICROSECONDS);
        ServerWorldEvents.UNLOAD.register(((server, world) -> mod.IsWorldLoaded=false));

        Log.info("Initialize TextFileReader mod done");
    }
}
