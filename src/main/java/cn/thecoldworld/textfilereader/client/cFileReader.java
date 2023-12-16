package cn.thecoldworld.textfilereader.client;


import cn.thecoldworld.textfilereader.FileIO;
import cn.thecoldworld.textfilereader.FileSource;
import cn.thecoldworld.textfilereader.networking.ClientNetWorkingTask;
import cn.thecoldworld.textfilereader.variables;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class cFileReader {
    public static void init(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("cFileLoader")
                .then(ClientCommandManager.literal("Get")
                        .then(ClientCommandManager.argument("FileName", StringArgumentType.string())
                                .then(ClientCommandManager.literal("Save")
                                        .executes(i -> cFunctions.GetFileFromServer(i, FileSource.save)))
                                .then(ClientCommandManager.literal("Global")
                                        .executes(i -> cFunctions.GetFileFromServer(i, FileSource.global))))
                )
                .then(ClientCommandManager.literal("List")
                        .executes(cFileReader::CPrintFileList))
                .then(ClientCommandManager.literal("Read")
                        .then(ClientCommandManager.argument("FileName", StringArgumentType.string()).executes(cFunctions::CGetFileContext))
                )
                .executes(i -> {
                    i.getSource().sendFeedback(Text.literal("TextFileReader"));
                    i.getSource().sendFeedback(Text.translatable("text.filereader.description"));
                    return 0;
                })
        );
    }

    @Environment(EnvType.CLIENT)
    public static int CPrintFileList(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try {
            Path Path = FileIO.GlobalTextPath;
            if (!Path.toFile().exists() || !Path.toFile().isDirectory()) {
                variables.Log.info("Missing client text data directory,starting crate");
                Files.createDirectory(Path);
            }
            if (variables.ModSettings.isSegmentedOutput()) {
                FabricClientCommandSource src = context.getSource();
                CompletableFuture.supplyAsync(() -> {
                    src.sendFeedback(Text.translatable("text.filereader.printcurrentpath", ""));
                    try {
                        Files.walk(Path, Integer.MAX_VALUE).filter(Files::isRegularFile).forEach(i -> {
                            if (i.getFileName().toString().equals("permissions.json")) return;
                            src.sendFeedback(Text.literal(i.getFileName().toString()));
                        });
                    } catch (IOException e) {
                        variables.Log.error("", e);
                        src.sendError(Text.translatable("text.filereader.exception", e.getClass().getCanonicalName(), e.getMessage()));
                    }
                    return 0;
                });
            } else {
                StringBuilder sb = new StringBuilder();
                Files.walk(Path, Integer.MAX_VALUE).filter(Files::isRegularFile).forEach(i -> {
                    if (i.getFileName().toString().equals("permissions.json")) return;
                    sb.append(i.getFileName()).append("\n");
                });
                context.getSource().sendFeedback(Text.translatable("text.filereader.printcurrentpath", "\n" + sb));
            }
            return 0;
        } catch (Exception ex) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception", ex.getClass().getCanonicalName(), ex.getMessage())).create();
        }
    }
}
