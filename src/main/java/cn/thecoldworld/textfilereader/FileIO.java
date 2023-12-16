package cn.thecoldworld.textfilereader;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

public class FileIO {
    public static Path RootDir = null;
    public static Path GlobalTextPath = null;
    public static Path ConfigPath = null;
    @Environment(EnvType.CLIENT)
    public static Path ClientConfigPath = null;

    public static int PrintFile(Entity entity, World world, String Filename, FileSource fileSource, boolean IsSelf, CommandContext<ServerCommandSource> context) throws IOException {
        if (world.isClient) return -1;
        Scanner fp = switch (fileSource) {
            case save ->
                    new Scanner(Paths.get(Objects.requireNonNull(world.getServer()).getSavePath(WorldSavePath.ROOT).getParent().toString(), "Texts", Filename), StandardCharsets.UTF_8);
            case global -> new Scanner(Paths.get(GlobalTextPath.toString(), Filename), StandardCharsets.UTF_8);
            case local -> throw new IOException("Cannot use filesource.local");
        };
        if (variables.ModSettings.isSegmentedOutput()) {
            entity.sendMessage(Text.translatable("text.filereader.printfile", Filename, ""));
            while (fp.hasNext()) {
                entity.sendMessage(Text.literal(fp.nextLine()));
            }
        } else {
            StringBuilder sb = new StringBuilder();
            while (fp.hasNext()) {
                sb.append(fp.nextLine()).append('\n');
            }
            entity.sendMessage(Text.translatable("text.filereader.printfile", Filename, "\n" + sb));
        }
        if (!IsSelf)
            entity.sendMessage(Text.translatable("text.filereader.printfile.others", context.getSource().getName()));
        fp.close();
        return 0;
    }

    public static String GetFileContent(String FileName, FileSource fileSource, MinecraftServer server) throws IOException {
        Scanner fp = switch (fileSource) {
            case save ->
                    new Scanner(Paths.get(server.getSavePath(WorldSavePath.ROOT).getParent().toString(), "Texts", FileName), StandardCharsets.UTF_8);
            case global -> new Scanner(Paths.get(GlobalTextPath.toString(), FileName), StandardCharsets.UTF_8);
            case local -> throw new IOException("Cannot use filesource.local");
        };
        StringBuilder sb = new StringBuilder();
        while (fp.hasNextLine()) {
            sb.append(fp.nextLine()).append("\n");
        }
        return sb.toString();
    }

    public static int PrintFileLines(Entity entity, World world, String Filename, FileSource fileSource, boolean IsSelf, CommandContext<ServerCommandSource> context, int begin, int end) throws IOException {
        if (world.isClient) return -1;
        Scanner fp;
        switch (fileSource) {
            case save ->
                    fp = new Scanner(Paths.get(Objects.requireNonNull(world.getServer()).getSavePath(WorldSavePath.ROOT).getParent().toString(), "Texts", Filename), StandardCharsets.UTF_8);
            case global -> fp = new Scanner(Paths.get(GlobalTextPath.toString(), Filename), StandardCharsets.UTF_8);
            default -> throw new IOException("Internal error");
        }
        if (variables.ModSettings.isSegmentedOutput()) {
            entity.sendMessage(Text.translatable("text.filereader.printfile", Filename, ""));
            for (int i = 0; i <= end && fp.hasNextLine(); i++) {
                if (i >= begin) {
                    entity.sendMessage(Text.literal(fp.nextLine()));
                } else {
                    fp.nextLine();
                }
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= end && fp.hasNextLine(); i++) {
                if (i >= begin) {
                    sb.append(fp.nextLine()).append('\n');
                } else {
                    fp.nextLine();
                }
            }
            entity.sendMessage(Text.translatable("text.filereader.printfile", Filename, "\n" + sb));
            if (!IsSelf)
                entity.sendMessage(Text.translatable("text.filereader.printfile.others", context.getSource().getName()));

        }
        fp.close();
        return 0;
    }
}
