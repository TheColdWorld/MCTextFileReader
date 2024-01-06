package cn.thecoldworld.textfilereader;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class FileIO {
    public static Path RootDir = null;
    public static Path GlobalTextPath = null;
    public static Path ConfigPath = null;

    public static int PrintFile(Entity entity, World world, String Filename, ServerFileSource serverFileSource, boolean IsSelf, CommandContext<ServerCommandSource> context) throws IOException {
        try (Scanner fp = switch (serverFileSource) {
            case save ->
                    new Scanner(Paths.get(Objects.requireNonNull(world.getServer()).getSavePath(WorldSavePath.ROOT).getParent().toString(), "Texts", Filename), StandardCharsets.UTF_8);
            case global -> new Scanner(Paths.get(GlobalTextPath.toString(), Filename), StandardCharsets.UTF_8);
        }) {
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
            return 0;
        }
    }

    public static String GetFileContent(String FileName, ServerFileSource serverFileSource, MinecraftServer server) throws IOException {
        StringBuilder sb;
        try (Scanner fp = switch (serverFileSource) {
            case save ->
                    new Scanner(Paths.get(server.getSavePath(WorldSavePath.ROOT).getParent().toString(), "Texts", FileName), StandardCharsets.UTF_8);
            case global -> new Scanner(Paths.get(GlobalTextPath.toString(), FileName), StandardCharsets.UTF_8);
        }) {
            sb = new StringBuilder();
            while (fp.hasNextLine()) {
                sb.append(fp.nextLine()).append("\n");
            }
        }
        return sb.toString();
    }

    public static List<String> GetFileList(String FileName, ServerFileSource serverFileSource, MinecraftServer server) throws IOException {
        LinkedList<String> sl;
        try (Scanner fp = switch (serverFileSource) {
            case save ->
                    new Scanner(Paths.get(server.getSavePath(WorldSavePath.ROOT).getParent().toString(), "Texts", FileName), StandardCharsets.UTF_8);
            case global -> new Scanner(Paths.get(GlobalTextPath.toString(), FileName), StandardCharsets.UTF_8);
        }) {
            sl = new LinkedList<>();
            while (fp.hasNextLine()) {
                sl.add(fp.nextLine());
            }
        }
        return sl;
    }

    public static String GetFileSingleLine(String FileName, ServerFileSource serverFileSource, MinecraftServer server, int line) throws IOException {
        try (Scanner fp = switch (serverFileSource) {
            case save ->
                    new Scanner(Paths.get(server.getSavePath(WorldSavePath.ROOT).getParent().toString(), "Texts", FileName), StandardCharsets.UTF_8);
            case global -> new Scanner(Paths.get(GlobalTextPath.toString(), FileName), StandardCharsets.UTF_8);
        }) {
            for (int i = 0; fp.hasNextLine(); i++) {
                if (i == line) {
                    return fp.nextLine();
                }
                fp.nextLine();
            }
            return "null";
        }
    }

    public static void Command_GetFileContent(String FileName, ServerFileSource serverFileSource, ServerPlayerEntity player, MinecraftServer server, @Nullable String Sender) throws CommandSyntaxException {
        try (Scanner fp = switch (serverFileSource) {
            case save ->
                    new Scanner(Paths.get(server.getSavePath(WorldSavePath.ROOT).getParent().toString(), "Texts", FileName), StandardCharsets.UTF_8);
            case global -> new Scanner(Paths.get(GlobalTextPath.toString(), FileName), StandardCharsets.UTF_8);
        }) {
            if (variables.ModSettings.isSegmentedOutput()) {
                player.sendMessage(Text.translatable("text.filereader.printfile", FileName, fp.nextLine()), false);
                while (fp.hasNextLine()) {
                    player.sendMessage(Text.literal(fp.nextLine()), false);
                }
                if (Sender != null && !Sender.equals(player.getName().getString()))
                    player.sendMessage(Text.translatable("text.filereader.printfile.others", Sender), false);
            } else {
                StringBuilder sb = new StringBuilder();
                while (fp.hasNextLine()) {
                    sb.append(fp.nextLine()).append("\n");
                }
                if (Sender == null || Sender.equals(player.getName().getString()))
                    player.sendMessage(Text.translatable("text.filereader.printfile", FileName, sb), false);
                else player.sendMessage(Text.translatable("text.filereader.printfile", FileName, sb)
                        .append(Text.translatable("text.filereader.printfile.others", Sender)), false);

            }
        } catch (Exception exception) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception", exception.getClass().getCanonicalName(), exception.getMessage())).create();
        }
    }

    public static int PrintFileLines(Entity entity, World world, String Filename, ServerFileSource serverFileSource, boolean IsSelf, CommandContext<ServerCommandSource> context, int begin, int end) throws IOException {
        if (world.isClient) return -1;
        try (Scanner fp = switch (serverFileSource) {
            case save ->
                    new Scanner(Paths.get(Objects.requireNonNull(world.getServer()).getSavePath(WorldSavePath.ROOT).getParent().toString(), "Texts", Filename), StandardCharsets.UTF_8);
            case global -> new Scanner(Paths.get(GlobalTextPath.toString(), Filename), StandardCharsets.UTF_8);
        }) {
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
        }
        return 0;
    }
}
