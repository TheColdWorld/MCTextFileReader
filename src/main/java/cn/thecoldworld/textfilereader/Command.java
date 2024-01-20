package cn.thecoldworld.textfilereader;

import cn.thecoldworld.textfilereader.api.command.CommandRegistrant;
import cn.thecoldworld.textfilereader.api.command.CommandRegistrantSrcType;
import cn.thecoldworld.textfilereader.api.command.CommandRegistrants;
import cn.thecoldworld.textfilereader.networking.ServerNetWorkingTask;
import cn.thecoldworld.textfilereader.networking.jsonformats.CommonMessage;
import cn.thecoldworld.textfilereader.networking.jsonformats.S2COpenSettingGui;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.LinkedList;
import java.util.Optional;

public abstract class Command {
    public static void Register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        CommandRegistrants.ReRegisterCommandRegistrants();
        LiteralArgumentBuilder<ServerCommandSource> commandRoot = CommandManager.literal("TextFileReader");
        CommandRegistrants.InvokeCommandRegistrants("TextFileReader",
                CommandRegistrantSrcType.server, commandRoot);
        dispatcher.register(commandRoot);
    }

    public static <S> void RegisterCommandNodes(CommandRegistrantSrcType srcType, String NodeName, ArgumentBuilder<S, ?> argumentBuilder, String... commandNodes) {
        for (String sNode : commandNodes) {
            CommandRegistrants.Register(CommandRegistrant.<S>Register(sNode,
                    dispatcher -> dispatcher.then(CommandRegistrants.InvokeCommandRegistrants(sNode + " " + NodeName, srcType, argumentBuilder))
                    , srcType));
        }
    }


    public static void RegisterOthers() {
        //TextFileReader ReadFile
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Line",
                CommandManager.argument("Line", IntegerArgumentType.integer(1)).executes(
                        i -> Command_ReadFileSingleLine(i, ServerFileSource.global, true)),
                "TextFileReader ReadFile FileName Global Player Line Single");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Line",
                CommandManager.argument("Line", IntegerArgumentType.integer(1)).executes(
                        i -> Command_ReadFileSingleLine(i, ServerFileSource.global, false)),
                "TextFileReader ReadFile FileName Global Line Single");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Line",
                CommandManager.argument("Line", IntegerArgumentType.integer(1))
                        .executes(i -> Command_ReadFileSingleLine(i, ServerFileSource.save, true)),
                "TextFileReader ReadFile FileName World Player Line Single");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Line",
                CommandManager.argument("Line", IntegerArgumentType.integer(1))
                        .executes(i -> Command_ReadFileSingleLine(i, ServerFileSource.save, false)),
                "TextFileReader ReadFile FileName World Line Single");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Single",
                CommandManager.literal("Single"),
                "TextFileReader ReadFile FileName Global Player Line", "TextFileReader ReadFile FileName World Player Line", "TextFileReader ReadFile FileName World Line", "TextFileReader ReadFile FileName Global Line");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Line",
                CommandManager.literal("Line"),
                "TextFileReader ReadFile FileName Global Player", "TextFileReader ReadFile FileName World Player", "TextFileReader ReadFile FileName World", "TextFileReader ReadFile FileName Global");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "CurrentPage",
                CommandManager.argument("CurrentPage", IntegerArgumentType.integer(1)),
                "TextFileReader ReadFile FileName Global Player");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "CurrentPage",
                CommandManager.argument("CurrentPage", IntegerArgumentType.integer(1)),
                "TextFileReader ReadFile FileName World Player");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "CurrentPage",
                CommandManager.argument("CurrentPage", IntegerArgumentType.integer(1)),
                "TextFileReader ReadFile FileName World");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "CurrentPage",
                CommandManager.argument("CurrentPage", IntegerArgumentType.integer(1)),
                "TextFileReader ReadFile FileName Global");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Page",
                CommandManager.literal("Page"),
                "TextFileReader ReadFile FileName Global Player", "TextFileReader ReadFile FileName World Player", "TextFileReader ReadFile FileName World", "TextFileReader ReadFile FileName Global");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Player",
                CommandManager.argument("Player", EntityArgumentType.players())
                        .executes(i -> Command_ReadFileAllContentPlayer(i, ServerFileSource.save))
                , "TextFileReader ReadFile FileName World");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Player",
                CommandManager.argument("Player", EntityArgumentType.players())
                        .executes(i -> Command_ReadFileAllContentPlayer(i, ServerFileSource.global)),
                "TextFileReader ReadFile FileName Global");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "World",
                CommandManager.literal("World")
                        .executes(i -> Command_ReadFileAllContent(i, ServerFileSource.global)),
                "TextFileReader ReadFile FileName");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Global",
                CommandManager.literal("Global")
                        .executes(i -> Command_ReadFileAllContent(i, ServerFileSource.global)),
                "TextFileReader ReadFile FileName");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "FileName",
                CommandManager.argument("FileName", StringArgumentType.string()),
                "TextFileReader ReadFile");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "ReadFile",
                CommandManager.literal("ReadFile"),
                "TextFileReader");


        // TextFileReader GUI
        RegisterCommandNodes(CommandRegistrantSrcType.server, "ReadText",
                CommandManager.literal("ReadText")
                        .executes(context -> {
                            ServerNetWorkingTask.Run(new CommonMessage("Gui_Open::MainGUI"),
                                    context.getSource().getPlayerOrThrow(), variables.Identifiers.ControlingIdentifier);
                            return 1;
                        }),
                "TextFileReader GUI");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Settings",
                CommandManager.literal("Settings")
                        .requires(player -> player.hasPermissionLevel(3))
                        .executes(context -> {
                            ServerNetWorkingTask.Run(new S2COpenSettingGui(variables.ModSettings), context.getSource().getPlayerOrThrow(), variables.Identifiers.ControlingIdentifier);
                            return 1;
                        }),
                "TextFileReader GUI");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "GUI",
                CommandManager.literal("GUI"),
                "TextFileReader");


        //TextFileReader Permission
        RegisterCommandNodes(CommandRegistrantSrcType.server, "value",
                CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> Command_SetPlayerFilePermission(context, ServerFileSource.save)),
                "TextFileReader Permission FileName World set Player");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "value",
                CommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> Command_SetPlayerFilePermission(context, ServerFileSource.global)),
                "TextFileReader Permission FileName Global set Player");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Player",
                CommandManager.argument("Player", EntityArgumentType.player()),
                "TextFileReader Permission FileName World set", "TextFileReader Permission FileName Global set");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Player",
                CommandManager.argument("Player", EntityArgumentType.player())
                        .executes(context -> Command_GetPlayerFilePermission(context, ServerFileSource.save)),
                "TextFileReader Permission FileName World get");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Player",
                CommandManager.argument("Player", EntityArgumentType.player())
                        .executes(context -> Command_GetPlayerFilePermission(context, ServerFileSource.global)),
                "TextFileReader Permission FileName Global get");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "get",
                CommandManager.literal("get"),
                "TextFileReader Permission FileName Global", "TextFileReader Permission FileName World");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "set",
                CommandManager.literal("set"),
                "TextFileReader Permission FileName Global", "TextFileReader Permission FileName World");
        RegisterCommandNodes(CommandRegistrantSrcType.server,"Clear",
                CommandManager.literal("clear").executes(context -> Command_ClearFilePermission(context,ServerFileSource.global))
                ,"TextFileReader Permission FileName Global");
        RegisterCommandNodes(CommandRegistrantSrcType.server,"Clear",
                CommandManager.literal("clear").executes(context -> Command_ClearFilePermission(context,ServerFileSource.save))
                ,"TextFileReader Permission FileName World");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "World",
                CommandManager.literal("World"),
                "TextFileReader Permission FileName");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Global",
                CommandManager.literal("Global"),
                "TextFileReader Permission FileName");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "FileName",
                CommandManager.argument("FileName", StringArgumentType.string()),
                "TextFileReader Permission");
        RegisterCommandNodes(CommandRegistrantSrcType.server, "Permission",
                CommandManager.literal("Permission")
                        .requires(player -> player.hasPermissionLevel(3)),
                "TextFileReader");
    }

    public static int Command_ReadFileAllContent(CommandContext<ServerCommandSource> context, ServerFileSource fileSource) throws CommandSyntaxException {
        try {
            String fileName = context.getArgument("FileName", String.class);
            if (FilePermissions.HavePermission(context.getSource().getPlayerOrThrow(), fileName, fileSource)) {
                FileIO.Command_GetFileContent(fileName, fileSource, context.getSource().getPlayerOrThrow(), context.getSource().getServer(), context.getSource().getName());
            } else
                throw new SimpleCommandExceptionType(Text.translatable("text.filereader.printfile.nopermission", fileName)).create();
        } catch (CommandSyntaxException commandSyntaxException) {
            throw commandSyntaxException;
        } catch (Exception exception) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception", exception.getClass().getCanonicalName(), exception.getMessage())).create();
        }
        return 1;
    }

    public static int Command_ReadFileAllContentPlayer(CommandContext<ServerCommandSource> context, ServerFileSource fileSource) throws CommandSyntaxException {
        try {
            LinkedList<String> FailedPlayers = new LinkedList<>();
            String fileName = context.getArgument("FileName", String.class);
            for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "Player")) {
                if (FilePermissions.HavePermission(player, fileName, fileSource)) {
                    FileIO.Command_GetFileContent(fileName, fileSource, player, player.getServer(), context.getSource().getName());
                } else FailedPlayers.add(player.getName().getString());
            }
            if (!FailedPlayers.isEmpty()) {
                throw new SimpleCommandExceptionType(Text.translatable("text.filereader.permission.get.no", String.join(",", FailedPlayers), fileName)).create();
            }
        } catch (CommandSyntaxException commandSyntaxException) {
            throw commandSyntaxException;
        } catch (Exception exception) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception", exception.getClass().getCanonicalName(), exception.getMessage())).create();
        }
        return 1;
    }

    public static int Command_ReadFileSingleLine(CommandContext<ServerCommandSource> context, ServerFileSource fileSource, boolean Is_not_Self) throws CommandSyntaxException {
        try {
            String fileName = context.getArgument("FileName", String.class);
            int Line = context.getArgument("Line", int.class);
            if (Is_not_Self || context.getSource().getName()
                    .equals(Optional.ofNullable(context.getSource().getPlayer())
                            .map(i -> i.getName().getString())
                            .orElse(""))) {
                LinkedList<String> FailedPlayers = new LinkedList<>();
                for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "Player")) {
                    if (FilePermissions.HavePermission(player, fileName, fileSource)) {
                        player.sendMessage(Text.translatable("text.filereader.printfile.line", fileName, Line, FileIO.GetFileSingleLine(fileName, fileSource, context.getSource().getServer(), Line)));
                        player.sendMessage(Text.translatable("text.filereader.printfile.line.others", context.getSource().getName()));
                    } else FailedPlayers.add(player.getName().getString());
                }
                if (!FailedPlayers.isEmpty()) {
                    throw new SimpleCommandExceptionType(Text.translatable("text.filereader.permission.get.no", String.join(",", FailedPlayers), fileName)).create();
                }
            } else {
                if (!FilePermissions.HavePermission(context.getSource().getPlayerOrThrow(), fileName, fileSource))
                    throw new SimpleCommandExceptionType(Text.translatable("text.filereader.printfile.nopermission", fileName)).create();
                context.getSource().sendMessage(Text.translatable("text.filereader.printfile.line", fileName, Line, FileIO.GetFileSingleLine(fileName, fileSource, context.getSource().getServer(), Line)));
            }
        } catch (CommandSyntaxException commandSyntaxException) {
            throw commandSyntaxException;
        } catch (Exception exception) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception", exception.getClass().getCanonicalName(), exception.getMessage())).create();
        }
        return 1;
    }

    public static int Command_GetPlayerFilePermission(CommandContext<ServerCommandSource> context, ServerFileSource fileSource) throws CommandSyntaxException {
        {
            final String FileName = context.getArgument("FileName", String.class);
            final ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "Player");
            context.getSource().sendMessage(Text.translatable(FilePermissions.HavePermission(player, FileName, fileSource)
                            ? "text.filereader.permission.get.yes" : "text.filereader.permission.get.no"
                    , player.getName().getString(), FileName));
            return 1;
        }
    }

    public static int Command_SetPlayerFilePermission(CommandContext<ServerCommandSource> context, ServerFileSource fileSource) throws CommandSyntaxException {
        try {
            final String FileName = context.getArgument("FileName", String.class);
            final ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "Player");
            FilePermissions.Files permission = switch (fileSource) {
                case global -> FilePermissions.GlobalTextPermission;
                case save -> FilePermissions.WorldTextPermission;
            };
            Text OpMessage;
            if (context.getArgument("value", boolean.class)) {
                permission.GivePermission(player, FileName);
                context.getSource().sendMessage(Text.translatable("text.filereader.permission.success.give", FileName, player.getName().getString()));
                OpMessage = Text.translatable("text.filereader.permission.success.give.ops", context.getSource().getName(), player.getName().getString(), FileName);
            } else {
                permission.RemovePermission(player, FileName, context.getSource().getServer().isOnlineMode());
                context.getSource().sendMessage(Text.translatable("text.filereader.permission.success.remove", FileName, player.getName().getString()));
                OpMessage = Text.translatable("text.filereader.permission.success.remove.ops", context.getSource().getName(), player.getName().getString(), FileName);
            }
            cn.thecoldworld.textfilereader.api.funcitons.SendOPMessage(context.getSource().getServer(), context.getSource().getPlayer(), OpMessage);
            variables.Log.info(OpMessage.getString());
        } catch (Exception e) {
            throw new SimpleCommandExceptionType(Text.translatable("argument.player.entities")).create();
        }
        return 1;
    }
    public static int Command_ClearFilePermission(CommandContext<ServerCommandSource> context,ServerFileSource fileSource) throws CommandSyntaxException {
        final String FileName = context.getArgument("FileName", String.class);
        FilePermissions.Files files= switch (fileSource) {
            case global -> FilePermissions.GlobalTextPermission;
            case save -> FilePermissions.WorldTextPermission;
        };
        Optional<Boolean> result= files.Files.stream().filter(f->f.Name.equals(FileName))
                .map(f->variables.TickEvent.add(()->{f.Permissions.clear();files.NeedUpdate=true;})).findAny();
        if(result.isEmpty() || !result.get())
        {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.clearpermission.fail",FileName)).create();
        } else {
            context.getSource().sendMessage(Text.translatable("text.filereader.clearpermission.success",FileName));
            cn.thecoldworld.textfilereader.api.funcitons.SendOPMessage(context.getSource().getServer()
                    ,context.getSource().getPlayer()
                    ,Text.translatable("text.filereader.clearpermission.success.ops",context.getSource().getName(),FileName));
        }
        return 1;
    }

    public static int Command_GetFilePage(CommandContext<ServerCommandSource> context, ServerFileSource fileSource, boolean Is_not_Self, boolean UseScreen) {
        String fileName = context.getArgument("FileName", String.class);
        int Line = context.getArgument("CurrentPage", int.class);
        if (Is_not_Self) {

        } else {

        }
        return 1;
    }
}
