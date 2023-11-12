package cn.thecoldworld.textfilereader;


import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class funcitons {
    public static boolean CreateFile(File file, String DefaultInput) throws Exception {
        if ( file.exists() && file.isFile() ) throw new Exception("File exist");
        try {
            if ( !file.createNewFile() ) return false;
            FileWriter fr = new FileWriter(file);
            fr.write(DefaultInput);
            fr.flush();
            fr.close();
            return true;
        } catch (IOException e) {
            variables.Log.error("", e);
            return false;
        }
    }

    public static boolean CreateDir(Path path) throws Exception {
        Path _path = path.toAbsolutePath().normalize();
        if ( _path.toFile().exists() && _path.toFile().isDirectory() ) throw new Exception("Dir exist");
        try {
            if ( !_path.getParent().toFile().exists() || !_path.getParent().toFile().isDirectory() )
                CreateDir(_path.getParent());
            Files.createDirectory(path);
            return true;
        } catch (IOException e) {
            variables.Log.error("", e);
            return false;
        }
    }

    public static String GetFilePrefix(@NotNull File fp) {
        if ( fp.exists() && fp.isFile() ) return fp.getName().substring(fp.getName().lastIndexOf(".") + 1);
        return "";
    }

    public static void OnWorldLoading(MinecraftServer server, ServerWorld world) {
        Path PermissionPath = server.getSavePath(WorldSavePath.ROOT).toAbsolutePath().resolve("Texts").resolve("permissions.json").normalize();
        if ( !PermissionPath.getParent().normalize().toFile().exists() || !PermissionPath.getParent().normalize().toFile().isDirectory() ) {
            try {
                funcitons.CreateDir(PermissionPath.getParent().normalize());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                if ( !e.getMessage().equals("Dir exist") ) throw new RuntimeException(e);
            }
        }
        if ( !PermissionPath.toFile().exists() || !PermissionPath.toFile().isFile() ) {
            variables.Log.info("Missing world text data permission file,starting crate");
            try {
                funcitons.CreateFile(PermissionPath.toFile(), "{\"Files\":[]}");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                if ( !e.getMessage().equals("File exist") ) throw new RuntimeException(e);
            }
        }
        FilePermissions.WorldTextPermission = FilePermissions.InitPermission(PermissionPath);
        variables.IsWorldLoaded = true;
    }

    public static void OnUpdateThread() {
        if ( variables.ModSettings != null ) {
            try {
                variables.ModSettings.UptoFile();
            } catch (IOException e) {
                variables.Log.error("", e);
            }
        }
        if ( !variables.IsWorldLoaded ) return;
        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            variables.Log.error("", e);
        }
        try {
            if ( variables.TickEvent.isEmpty() ) {
                FilePermissions.GlobalTextPermission.UpdateFile();
                FilePermissions.WorldTextPermission.UpdateFile();
                FilePermissions.GlobalTextPermission.UpToFile();
                FilePermissions.WorldTextPermission.UpToFile();
                return;
            }
            variables.TickEvent.take().run();
        } catch (Exception e) {
            variables.Log.error("", e);
        }
    }
}
