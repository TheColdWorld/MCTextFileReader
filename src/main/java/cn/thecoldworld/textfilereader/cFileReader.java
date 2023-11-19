package cn.thecoldworld.textfilereader;


import cn.thecoldworld.textfilereader.networking.Tasks;
import cn.thecoldworld.textfilereader.networking.jsonformats.C2SGetContent;
import cn.thecoldworld.textfilereader.networking.jsonformats.FailedContent;
import cn.thecoldworld.textfilereader.networking.jsonformats.S2CGetContent;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.EnvType;
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

public class cFileReader {
    public static void init(@NotNull CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("cFileLoader")
                .then(ClientCommandManager.literal("Get")
                        .then(ClientCommandManager.argument("FileName", StringArgumentType.string())
                                .then(ClientCommandManager.literal("Save")
                                        .executes(i -> GetFileFromServer(i, FileSource.save)))
                                .then(ClientCommandManager.literal("Global")
                                        .executes(i -> GetFileFromServer(i, FileSource.global))))
                )
                .then(ClientCommandManager.literal("Debug")
                        .executes(i -> {
                            Tasks.Task.Run(new JsonObject(), variables.Identifiers.DebugFileIdentifier,
                                    (arguments -> i.getSource().sendFeedback(Text.literal(arguments.value.get("tmp").getAsString()))));
                            return 0;
                        }))
                .then(ClientCommandManager.literal("List")
                        .executes(cFileReader::CPrintFileList))
                .then(ClientCommandManager.literal("Read")
                        .then(ClientCommandManager.argument("FileName", StringArgumentType.string()).executes(cFileReader::CGetFileContext))
                )
                .executes(i -> {
                    i.getSource().sendFeedback(Text.literal("TextFileReader"));
                    i.getSource().sendFeedback(Text.translatable("text.filereader.description"));
                    return 0;
                })
        );
    }

    public static int GetFileFromServer(CommandContext<FabricClientCommandSource> context, FileSource fileSource) {
        {
            new Tasks.Task(new C2SGetContent(context.getArgument("FileName", String.class), fileSource).ToJsonObject(), variables.Identifiers.TextFileNetworkingIdentifier, EnvType.CLIENT, "Json",
                    arguments -> {
                        try {
                            if ( FailedContent.IsInstance(arguments.value.toString()) ) {
                                context.getSource().sendError(Text.literal(arguments.value.get("Reason").getAsString()));
                            }
                            if ( !S2CGetContent.IsInstance(arguments.value.toString()) || arguments.envType != EnvType.CLIENT )
                                return;
                            context.getSource().sendFeedback(Text.literal(arguments.value.get("Value").getAsString()));
                        } catch (Exception e) {
                            context.getSource().sendError(Text.translatable("text.filereader.exception", e.getClass().getCanonicalName(), e.getMessage()));
                        }
                    }).Send();
            return 0;
        }
    }

    public static int CGetFileContext(@NotNull CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try {
            String Fileaddress = context.getArgument("FileName", String.class);
            Scanner fp = new Scanner(Paths.get(FileIO.GlobalTextPath.toString(), Fileaddress), StandardCharsets.UTF_8);
            if ( variables.ModSettings.isSegmentedOutput() ) {
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
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.filenotfound", fe.getMessage())).create();
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
            if ( variables.ModSettings.isSegmentedOutput() ) {
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
