package cn.thecoldworld.textfilereader;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class cfileloader {
    public static void init(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("cFileLoader")
                .then(ClientCommandManager.literal("List")
                        .executes(cfileloader::CPrintFileList))
                .then(ClientCommandManager.literal("Read")
                        .then(ClientCommandManager.argument("FileName", StringArgumentType.string()).executes(cfileloader::CGetFileContext))
                )
                .executes(i -> {
                    i.getSource().sendFeedback(Text.literal("TextFileReader"));
                    i.getSource().sendFeedback(Text.translatable("text.filereader.description"));
                    return 0;
                })
        );
    }

    public static int CGetFileContext(@NotNull CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try {
            String Fileaddress = context.getArgument("FileName", String.class);
            Scanner fp = new Scanner(Paths.get(FileIO.GlobalTextPath.toString(), Fileaddress), StandardCharsets.UTF_8);
            if ( variables.ModSettings.Segmentedoutput ) {
                context.getSource().sendFeedback(Text.translatable("text.filereader.printfile", Fileaddress, ""));
                while (fp.hasNext()) {
                    context.getSource().sendFeedback(Text.literal(fp.nextLine()));
                }
            } else {
                StringBuilder sb = new StringBuilder();
                while (fp.hasNext()) {
                    sb.append(fp.nextLine()).append('\n');
                }
                context.getSource().sendFeedback(Text.translatable("text.filereader.printfile", Fileaddress, "\n" + sb));
            }
            fp.close();
        } catch (NoSuchFileException fe) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader,filenotfound", fe.getMessage())).create();
        } catch (Exception ex) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception", ex.getClass().getCanonicalName(), ex.getMessage())).create();
        }
        return 0;
    }

    public static int CPrintFileList(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try {
            Path Path = FileIO.GlobalTextPath;
            if ( !Path.toFile().exists() || !Path.toFile().isDirectory() ) {
                variables.Log.info("Missing client text data directory,starting crate");
                Files.createDirectory(Path);
            }
            if ( variables.ModSettings.Segmentedoutput ) {
                FabricClientCommandSource src = context.getSource();
                CompletableFuture.supplyAsync(() -> {
                    src.sendFeedback(Text.translatable("text.filereader.printcurrentpath", ""));
                    try {
                        Files.walk(Path, Integer.MAX_VALUE).filter(Files::isRegularFile).forEach(i -> {
                            if ( i.getFileName().toString().equals("permissions.json") ) return;
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
                    if ( i.getFileName().toString().equals("permissions.json") ) return;
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
