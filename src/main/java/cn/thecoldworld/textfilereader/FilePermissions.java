package cn.thecoldworld.textfilereader;

import cn.thecoldworld.textfilereader.api.funcitons;
import com.google.gson.JsonSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public abstract class FilePermissions {
    public static Files GlobalTextPermission = null;
    public static Files WorldTextPermission = null;

    public static @NotNull Files InitPermission(Path FilePath) throws JsonSyntaxException {
        if (!FilePath.toFile().exists() || !FilePath.toFile().isFile()) {
            try {
                funcitons.CreateFile(FilePath.toFile(), "");
            } catch (Exception e) {
                if (!e.getMessage().equals("File exist")) variables.Log.error("", e);
            }
        }
        Files FP;
        try {
            FP = Optional.ofNullable(variables.defaultGson.fromJson(String.join("", java.nio.file.Files.readAllLines(FilePath)), Files.class)).orElseGet(Files::new);
            FP.FilePath = FilePath.toFile();
            FP.UpdateFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return FP;
    }

    public static boolean HavePermission(ServerPlayerEntity player, String FileName, ServerFileSource serverFileSource) {
        boolean Online_Mode = Optional.ofNullable(player.getServer()).map(MinecraftServer::isOnlineMode).orElse(false);
        try {
            return switch (serverFileSource) {
                case global -> FilePermissions.GlobalTextPermission.HavePermission(player, FileName, Online_Mode);
                case save -> FilePermissions.WorldTextPermission.HavePermission(player, FileName, Online_Mode);
            };
        } catch (Exception ignored) {
            return false;
        }
    }

    public static Scanner GetFileScanner(String FileName, ServerFileSource fileSource) throws IOException {
        return new Scanner(switch (fileSource) {
            case global ->
                    Paths.get(FilePermissions.GlobalTextPermission.FilePath.getParentFile().getAbsoluteFile().toString(), FileName);
            case save ->
                    Paths.get(FilePermissions.WorldTextPermission.FilePath.getParentFile().getAbsoluteFile().toString(), FileName);
        });
    }

    public static class Files {

        public final List<File> Files;
        public transient java.io.File FilePath;
        public boolean NeedUpdate;

        public Files() {
            Files = new ArrayList<>();
            NeedUpdate = true;
        }

        public void UpdateFile() throws IOException {
            try (Stream<Path> stream = java.nio.file.Files.walk(FilePath.getParentFile().toPath(), Integer.MAX_VALUE)) {
                stream.filter(java.nio.file.Files::isRegularFile).forEach(i -> {
                    if (i.getFileName().toString().equals("permissions.json")) return;
                    if (funcitons.GetFilePrefix(i.toFile()).equals("exe")) return;
                    if (Files.stream().anyMatch(m -> m.Name.equals(i.toFile().getName()))) return;
                    File fs = new File();
                    fs.Name = i.toFile().getName();
                    fs.Permissions = new ArrayList<>();
                    variables.TickEvent.add(() -> {
                        Files.add(fs);
                        NeedUpdate = true;
                    });
                });
            }
            if (variables.ModSettings.isRemoveInvalidFile()) {
                Files.forEach(i -> {
                    if (!Paths.get(FilePath.getParent(), i.Name).toFile().exists() || !Paths.get(FilePath.getParent(), i.Name).toFile().isFile()) {
                        if (variables.ModSettings.isRemoveInvalidFile()) {
                            variables.TickEvent.add(() -> {
                                Files.remove(i);
                                NeedUpdate = true;
                            });
                        }
                    }
                });
            }
        }

        public void UpToFile() throws IOException {
            if (!NeedUpdate) return;
            FileWriter fp = new FileWriter(FilePath, StandardCharsets.UTF_8, false);
            fp.write(variables.defaultGson.toJson(this));
            fp.flush();
            fp.close();
            NeedUpdate = false;
        }

        public boolean RemovePermission(Entity ent, String FileName, boolean Online_Mode) {
            return Files.stream().filter(i -> i.Name.equals(FileName)).allMatch(file -> variables.TickEvent.add(() -> file.RemovePermission(ent, Online_Mode, new SoftReference<>(this))));
        }

        public boolean GivePermission(@NotNull Entity ent, String FileName) throws Exception {
            if (!ent.isPlayer()) throw new Exception("Is not a Player");
            return Files.stream().filter(i -> i.Name.equals(FileName)).findFirst().map(file -> file.GivePermission(ent, new SoftReference<>(this))).orElse(false);
        }

        public boolean HavePermission(@NotNull Entity entity, String FileName, boolean Online_Mode) throws Exception {
            if (!entity.isPlayer()) throw new Exception("Is not a Player");
            if (Files.stream().filter(i -> i.Name.equals(FileName)).map(i -> i.HavePermission(entity, Online_Mode)).findAny().isEmpty())
                return false;
            return Files.stream().filter(i -> i.Name.equals(FileName)).allMatch(i -> i.HavePermission(entity, Online_Mode));
        }
    }

    public static class File {

        public String Name;

        public List<Permissions> Permissions;

        public boolean RemovePermission(Entity ent, boolean Online_Mode, @NotNull Reference<Files> father) {
            if (Online_Mode) {
                return Permissions.stream().filter(i -> i.UUID.equals(ent.getUuidAsString())).allMatch(i -> variables.TickEvent.add(() -> {
                    Permissions.remove(i);
                    Objects.requireNonNull(father.get()).NeedUpdate = true;
                }));
            }
            return Permissions.stream().filter(i -> i.Name.equals(ent.getName().getString())).allMatch(i -> variables.TickEvent.add(() -> {
                Permissions.remove(i);
                Objects.requireNonNull(father.get()).NeedUpdate = true;
            }));
        }

        public boolean GivePermission(@NotNull Entity ent, @NotNull Reference<Files> father) {
            FilePermissions.Permissions p = new Permissions();
            p.Name = ent.getName().getString();
            p.UUID = ent.getUuidAsString();
            variables.TickEvent.add(() -> {
                Permissions.add(p);
                Objects.requireNonNull(father.get()).NeedUpdate = true;
            });
            return true;
        }

        public boolean HavePermission(Entity entity, boolean Online_Mode) {
            if (Online_Mode) {
                return Permissions.stream().map(i -> i.UUID.equals(entity.getUuidAsString())).findAny().isPresent();
            }
            return Permissions.stream().map(i -> i.Name.equals(entity.getName().getString())).findAny().isPresent();
        }
    }

    public static class Permissions {

        public String Name;

        public String UUID;
    }
}
