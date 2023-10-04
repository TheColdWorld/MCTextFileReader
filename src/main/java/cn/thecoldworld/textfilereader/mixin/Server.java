package cn.thecoldworld.textfilereader.mixin;

import cn.thecoldworld.textfilereader.FilePermissions;
import cn.thecoldworld.textfilereader.mod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(MinecraftServer.class)
public abstract class Server {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        Path PermissionPath = ((MinecraftServer) (Object) this).getSavePath(WorldSavePath.ROOT).toAbsolutePath().resolve("Texts").resolve("permissions.json").normalize();
        if ( !PermissionPath.getParent().normalize().toFile().exists() || !PermissionPath.getParent().normalize().toFile().isDirectory() ) {
            try {
                Files.createDirectory(PermissionPath.getParent().normalize());
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
        if ( FilePermissions.WorldTextPermission == null ) {
            try {
                FilePermissions.WorldTextPermission = FilePermissions.InitPermission(PermissionPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
