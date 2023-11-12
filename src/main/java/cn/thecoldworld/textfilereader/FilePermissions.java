package cn.thecoldworld.textfilereader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilePermissions {
    public static Files GlobalTextPermission = null;
    public static Files WorldTextPermission = null;

    public static @NotNull Files InitPermission(Path FilePath) throws JsonSyntaxException {
        if ( !FilePath.toFile().exists() || !FilePath.toFile().isFile() ) {
            try {
                funcitons.CreateFile(FilePath.toFile(), "");
            } catch (Exception e) {
                if ( !e.getMessage().equals("File exist") ) variables.Log.error("", e);
            }
        }
        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        Files FP;
        try {
            FP = gson.fromJson(String.join("", java.nio.file.Files.readAllLines(FilePath)), Files.class);
            if ( FP == null ) FP = new Files();
            FP.FilePath = FilePath.toFile();
            FP.UpdateFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return FP;
    }

    public static class Files {
        @Expose
        public final List<File> Files;
        public transient java.io.File FilePath;
        public boolean NeedUpdate;

        public Files() {
            Files = new ArrayList<>();
            NeedUpdate = true;
        }

        public void UpdateFile() throws IOException {
            java.nio.file.Files.walk(FilePath.getParentFile().toPath(), Integer.MAX_VALUE).filter(java.nio.file.Files::isRegularFile).forEach(i -> {
                if ( i.getFileName().toString().equals("permissions.json") ) return;
                if ( funcitons.GetFilePrefix(i.toFile()).equals("exe") ) return;
                if ( Files.stream().anyMatch(m -> m.Name.equals(i.toFile().getName())) ) return;
                File fs = new File();
                fs.Name = i.toFile().getName();
                fs.Permissions = new ArrayList<>();
                variables.TickEvent.add(() -> {
                    Files.add(fs);
                    NeedUpdate = true;
                });
            });
            Files.forEach(i -> {
                if ( !Paths.get(FilePath.getParent(), i.Name).toFile().exists() || !Paths.get(FilePath.getParent(), i.Name).toFile().isFile() ) {
                    if ( variables.ModSettings.isRemoveInvalidFile() ) {
                        variables.TickEvent.add(() -> {
                            Files.remove(i);
                            NeedUpdate = true;
                        });
                    }
                }
            });
        }

        public void UpToFile() throws IOException {
            if ( !NeedUpdate ) return;
            Gson gson = new GsonBuilder()
                    .enableComplexMapKeySerialization()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
            FileWriter fp = new FileWriter(FilePath, StandardCharsets.UTF_8, false);
            fp.write(gson.toJson(this));
            fp.flush();
            fp.close();
            NeedUpdate = false;
        }

        public boolean RemovePermission(Entity ent, String FileName, boolean Online_Mode) {
            return Files.stream().filter(i -> i.Name.equals(FileName)).allMatch(file -> variables.TickEvent.add(() -> file.RemovePermission(ent, Online_Mode, new SoftReference<>(this))));
        }

        public boolean GivePermission(@NotNull Entity ent, String FileName) throws Exception {
            if ( !ent.isPlayer() ) throw new Exception("Is not a Player");
            return Files.stream().filter(i -> i.Name.equals(FileName)).findFirst().map(file -> file.GivePermission(ent, new SoftReference<>(this))).orElse(false);
        }

        public boolean HavePermission(@NotNull Entity entity, String FileName, boolean Online_Mode) throws Exception {
            if ( !entity.isPlayer() ) throw new Exception("Is not a Player");
            if ( Files.stream().filter(i -> i.Name.equals(FileName)).map(i -> i.HavePermission(entity, Online_Mode)).findAny().isEmpty() )
                return false;
            return Files.stream().filter(i -> i.Name.equals(FileName)).allMatch(i -> i.HavePermission(entity, Online_Mode));
        }
    }

    public static class File {
        @Expose
        public String Name;
        @Expose
        public List<Permissions> Permissions;

        public boolean RemovePermission(Entity ent, boolean Online_Mode, @NotNull Reference<Files> father) {
            if ( Online_Mode ) {
                return Permissions.stream().filter(i -> i.UUID.equals(ent.getUuidAsString())).allMatch(i -> variables.TickEvent.add(() -> {
                    Permissions.remove(i);
                    Objects.requireNonNull(father.get()).NeedUpdate = true;
                }));
            }
            return Permissions.stream().filter(i -> i.Name.equals(ent.getEntityName())).allMatch(i -> variables.TickEvent.add(() -> {
                Permissions.remove(i);
                Objects.requireNonNull(father.get()).NeedUpdate = true;
            }));
        }

        public boolean GivePermission(@NotNull Entity ent, @NotNull Reference<Files> father) {
            FilePermissions.Permissions p = new Permissions();
            p.Name = ent.getEntityName();
            p.UUID = ent.getUuidAsString();
            variables.TickEvent.add(() -> {
                Permissions.add(p);
                Objects.requireNonNull(father.get()).NeedUpdate = true;
            });
            return true;
        }

        public boolean HavePermission(Entity entity, boolean Online_Mode) {
            if ( Online_Mode ) {
                return Permissions.stream().map(i -> i.UUID.equals(entity.getUuidAsString())).findAny().isPresent();
            }
            return Permissions.stream().map(i -> i.Name.equals(entity.getEntityName())).findAny().isPresent();
        }
    }

    public static class Permissions {
        @Expose
        public String Name;
        @Expose
        public String UUID;
    }
}
