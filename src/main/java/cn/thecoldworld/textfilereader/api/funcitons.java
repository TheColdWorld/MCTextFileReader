package cn.thecoldworld.textfilereader.api;


import cn.thecoldworld.textfilereader.FilePermissions;
import cn.thecoldworld.textfilereader.ServerFileSource;
import cn.thecoldworld.textfilereader.exceptions.TranslatableException;
import cn.thecoldworld.textfilereader.networking.NetworkingFunctions;
import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public abstract class funcitons {
    public static List<String> GetFileList(MinecraftServer server, ServerPlayerEntity player, ServerFileSource serverFileSource) throws TranslatableException {
        try {
            FilePermissions.Files files = switch (serverFileSource) {
                case global -> FilePermissions.GlobalTextPermission;
                case save -> FilePermissions.WorldTextPermission;
            };
            LinkedList<String> FileList = new LinkedList<>();
            files.Files.forEach(file -> {
                if (file.HavePermission(player, server.isOnlineMode())) FileList.add(file.Name);
            });
            return FileList;
        } catch (Exception e) {
            throw new TranslatableException("text.filereader.exception", e.getClass().getCanonicalName(), e.getMessage());
        }
    }

    public static void RegisterServerNetworkReceivers(Identifier... Identifiers) {
        for (Identifier i : Identifiers) {
            ServerPlayNetworking.registerGlobalReceiver(i, ((server, player, handler, buf, responseSender) -> NetworkingFunctions.GetNetPackageCallback(server, player, handler, buf, responseSender, i)));
        }
    }

    public static boolean CreateFile(File file, String DefaultInput) throws Exception {
        if (file.exists() && file.isFile()) throw new Exception("File exist");
        try {
            if (!file.createNewFile()) return false;
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
        if (_path.toFile().exists() && _path.toFile().isDirectory()) throw new Exception("Dir exist");
        try {
            if (!_path.getParent().toFile().exists() || !_path.getParent().toFile().isDirectory())
                CreateDir(_path.getParent());
            Files.createDirectory(path);
            return true;
        } catch (IOException e) {
            variables.Log.error("", e);
            return false;
        }
    }

    public static String GetFilePrefix(@NotNull File fp) {
        if (fp.exists() && fp.isFile()) return fp.getName().substring(fp.getName().lastIndexOf(".") + 1);
        return "";
    }

    public static void OnWorldLoading(MinecraftServer server, ServerWorld world) {
        Path PermissionPath = server.getSavePath(WorldSavePath.ROOT).toAbsolutePath().resolve("Texts").resolve("permissions.json").normalize();
        if (!PermissionPath.getParent().normalize().toFile().exists() || !PermissionPath.getParent().normalize().toFile().isDirectory()) {
            try {
                funcitons.CreateDir(PermissionPath.getParent().normalize());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                if (!e.getMessage().equals("Dir exist")) throw new RuntimeException(e);
            }
        }
        if (!PermissionPath.toFile().exists() || !PermissionPath.toFile().isFile()) {
            variables.Log.info("Missing world text data permission file,starting crate");
            try {
                funcitons.CreateFile(PermissionPath.toFile(), "{\"Files\":[]}");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                if (!e.getMessage().equals("File exist")) throw new RuntimeException(e);
            }
        }
        FilePermissions.WorldTextPermission = FilePermissions.InitPermission(PermissionPath);
        variables.IsWorldLoaded = true;
    }

    public static void OnUpdateThread() {
        if (variables.ModSettings != null) {
            try {
                variables.ModSettings.UptoFile();
            } catch (IOException e) {
                variables.Log.error("", e);
            }
        }
        if (!variables.IsWorldLoaded) return;
        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            variables.Log.error("", e);
        }
        try {
            if (variables.TickEvent.isEmpty()) {
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

    public static <T> List<T> AutoRemoveGetItemFromStream(List<T> src, Function<T, Boolean> filter) {
        List<T> ts = new LinkedList<>();
        for (T i : src) {
            if (filter.apply(i)) {
                ts.add(i);
            }
        }
        src.removeAll(ts);
        return ts;
    }

    public static int DivisibleUpwards(int x, int y) {
        return (x + y - 1) / y;
    }

    public static String[] GetPages(int LinesPerPage, String... Lines) {
        int Pages = funcitons.DivisibleUpwards(Lines.length, LinesPerPage);
        String[] pages = new String[Pages];
        int EndRow = 0;
        for (int k = 0; k < Pages; k++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < LinesPerPage && EndRow < Lines.length; j++, EndRow++) {
                sb.append(Lines[EndRow].replace("\t", "    ")).append('\n');
            }
            pages[k] = sb.toString();
        }
        return pages;
    }

    public static String[] GetPages(int LinesPerPage, List<String> Lines) {
        int Pages = funcitons.DivisibleUpwards(Lines.size(), LinesPerPage);
        String[] pages = new String[Pages];
        int EndRow = 0;
        for (int k = 0; k < Pages; k++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < LinesPerPage && EndRow < Lines.size(); j++, EndRow++) {
                sb.append(Lines.get(EndRow).replace("\t", "    ")).append('\n');
            }
            pages[k] = sb.toString();
        }
        return pages;
    }
}
