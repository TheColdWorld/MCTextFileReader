package cn.thecoldworld.textfilereader;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class filereader {
    public static void Init(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(CommandManager.literal("FileReader")
                .then(CommandManager.literal("Permission")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.literal("Remove")
                                .then(CommandManager.argument("FileName", StringArgumentType.string())
                                        .then(CommandManager.literal("Save")
                                                .executes(i -> RemovePermissionMyself(i, FileSource.save)))
                                        .then(CommandManager.literal("Global")
                                                .executes(i -> RemovePermissionMyself(i, FileSource.global)))
                                        .then(CommandManager.argument("Player", EntityArgumentType.players())
                                                .then(CommandManager.literal("Save")
                                                        .executes(i -> RemovePermission(i, FileSource.save)))
                                                .then(CommandManager.literal("Global")
                                                        .executes(i -> RemovePermission(i, FileSource.global))))))
                        .then(CommandManager.literal("Give")
                                .then(CommandManager.argument("FileName", StringArgumentType.string())
                                        .then(CommandManager.literal("Save")
                                                .executes(i -> GivePermissionMyself(i, FileSource.save)))
                                        .then(CommandManager.literal("Global")
                                                .executes(i -> GivePermissionMyself(i, FileSource.global)))
                                        .then(CommandManager.argument("Player", EntityArgumentType.players())
                                                .then(CommandManager.literal("Save")
                                                        .executes(i -> GivePermission(i, FileSource.save)))
                                                .then(CommandManager.literal("Global")
                                                        .executes(i -> GivePermission(i, FileSource.global)))))))
                .then(CommandManager.literal("File")
                        .then(CommandManager.literal("Read")
                                .then(CommandManager.argument("FileName", StringArgumentType.string())
                                        .then(CommandManager.literal("Global")
                                                .then(CommandManager.argument("Player", EntityArgumentType.player())
                                                        .executes(i -> GetFileContext(i, FileSource.global, EntityArgumentType.getEntity(i, "Player"))))
                                                .executes(i -> GetFileContext(i, FileSource.global, null)))
                                        .then(CommandManager.literal("Save")
                                                .then(CommandManager.argument("Player", EntityArgumentType.player())
                                                        .executes(i -> GetFileContext(i, FileSource.global, EntityArgumentType.getEntity(i, "Player"))))
                                                .executes(i -> GetFileContext(i, FileSource.save, null)))))
                        .then(CommandManager.literal("List")
                                .requires(src -> src.hasPermissionLevel(2))
                                .then(CommandManager.literal("Global")
                                        .executes(i -> PrintFileList(i, FileSource.global)))
                                .then(CommandManager.literal("Save")
                                        .executes(i -> PrintFileList(i, FileSource.save)))))
                .executes(i -> {
                    i.getSource().sendMessage(Text.literal("TextFileReader"));
                    i.getSource().sendMessage(Text.translatable("text.filereader.description"));
                    return 0;
                })
        );
    }
    public  static int PrintFileList(CommandContext<ServerCommandSource> context,FileSource fileSource) throws CommandSyntaxException
    {
        try {
            boolean Segmentedoutput= mod.GetConfig("Segmentedoutput").getAsBoolean();
            Path Path;
            if(fileSource == FileSource.save) Path= Paths.get(context.getSource().getWorld().getServer().getSavePath(WorldSavePath.ROOT).getParent().toString(),"Texts");
            else Path= FileIO.GlobalTextPath;
            if(!Path.toFile().exists() || !Path.toFile().isDirectory())
            {
                mod.Log.info("Missing server text data directory,starting crate");
                Files.createDirectory(Path);
            }
            if(Segmentedoutput)
            {
                ServerCommandSource src=context.getSource();
                CompletableFuture.supplyAsync(()->{
                    src.sendMessage(Text.translatable("text.filereader.printcurrentpath",""));
                    try {
                        Files.walk(Path, Integer.MAX_VALUE).filter(Files::isRegularFile).forEach(i -> {
                            if (i.getFileName().toString().equals("permissions.json")) return;
                            src.sendMessage(Text.literal(i.getFileName().toString()));
                        });
                    } catch (IOException e) {
                        mod.Log.error("",e);
                        src.sendMessage(Text.translatable("text.filereader.exception",e.getClass().getCanonicalName(),e.getMessage()));
                    }
                    return 0;
                });
            }
            else
            {
                StringBuilder sb=new StringBuilder();
                Files.walk(Path, Integer.MAX_VALUE).filter(Files::isRegularFile).forEach(i -> {
                    if (i.getFileName().toString().equals("permissions.json")) return;
                    sb.append(i.getFileName()).append("\n");
                });
                context.getSource().sendMessage(Text.translatable("text.filereader.printcurrentpath","\n"+ sb));
            }
            return  0;
        }
        catch (Exception ex) {throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception",ex.getClass().getCanonicalName(),ex.getMessage())).create();}
    }
    public  static  int GetFileContext(CommandContext<ServerCommandSource> context, FileSource fileSource,@Nullable Entity ent) throws CommandSyntaxException
    {
        try {
            sender sender= GetSender(context);
            ServerCommandSource source = context.getSource();
            String FileAddress = context.getArgument("FileName", String.class);
            if(sender == cn.thecoldworld.textfilereader.sender.console) {mod.Log.info("Command FileReader File Get from server console");return PrintConsole(context,FileAddress,fileSource);}
            Entity entity;
            if(ent == null)  entity= source.getEntityOrThrow();
            else entity=ent;
           if(!switch (fileSource)
            {
                case global ->FilePermissions.GlobalTextPermission.HavePermission(entity,FileAddress,source.getServer().isOnlineMode());
                case save -> FilePermissions.WorldTextPermission.HavePermission(entity,FileAddress,source.getServer().isOnlineMode());
            }) {throw new SimpleCommandExceptionType(Text.translatable("text.filereader.printfile.nopermission",FileAddress)).create();}
            CompletableFuture.supplyAsync(()-> {
                try {
                    return FileIO.PrintFile(entity,source.getWorld(),FileAddress,fileSource);
                } catch (IOException e) {
                    mod.Log.error("",e);
                    context.getSource().sendMessage(Text.translatable("text.filereader.exception",e.getClass().getCanonicalName(),e.getMessage()));
                    return -1;
                }
            });
            mod.Log.info("Command FileReader File Get from player "+entity.getEntityName());
            return 0;
        }
        catch (CommandSyntaxException syntaxException){ throw syntaxException;}
        catch (java.lang.Exception ex){
            context.getSource().sendMessage(Text.translatable("text.filereader.exception",ex.getClass().getCanonicalName(),ex.getMessage()));
            throw new SimpleCommandExceptionType(Text.literal(ex.getMessage())).create();
        }
    }

    public static int PrintConsole(CommandContext<ServerCommandSource> context,String FileName,FileSource fileSource) throws CommandSyntaxException
    {
        try
        {
            boolean Segmentedoutput= mod.GetConfig("Segmentedoutput").getAsBoolean();
            Scanner fp;
            switch (fileSource)
            {
                case save -> {
                    fp=new Scanner(Paths.get(context.getSource().getWorld().getServer().getSavePath(WorldSavePath.ROOT).getParent().toString(),FileName),StandardCharsets.UTF_8);
                }
                case global -> {
                    fp=new Scanner(Paths.get(FileIO.GlobalTextPath.toString(),FileName), StandardCharsets.UTF_8);
                }
                default -> throw new SimpleCommandExceptionType(Text.literal("Internal error")).create();
            }
            if(Segmentedoutput)
            {
                mod.Log.info(Text.translatable("text.filereader.printfile",FileName,"").asTruncatedString(100));
                while (fp.hasNext())
                {
                    mod.Log.info(fp.nextLine());
                }
            }
            else
            {
                StringBuilder sb= new StringBuilder();
                while (fp.hasNext())
                {
                    sb.append(fp.nextLine()).append('\n');
                }
                mod.Log.info(Text.translatable("text.filereader.printfile",FileName,"\n"+sb).asTruncatedString(sb.length()+100));
            }
            fp.close();
            return 0;
        }
        catch (Exception e)
        {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception",e.getClass().getCanonicalName(),e.getMessage())).create();
        }
    }
    public static int GivePermissionMyself(CommandContext<ServerCommandSource> context,FileSource fs) throws CommandSyntaxException
    {
        GetSender(context);
        try
        {
            if(fs == FileSource.save)
            {
                if(!Files.exists(Paths.get(context.getSource().getServer().getSavePath(WorldSavePath.ROOT).getParent().toAbsolutePath().normalize().toString(),"Texts",context.getArgument("FileName", String.class)).toAbsolutePath().normalize()) ||
                        !Paths.get(context.getSource().getServer().getSavePath(WorldSavePath.ROOT).getParent().toAbsolutePath().normalize().toString(),"Texts",context.getArgument("FileName", String.class)).toAbsolutePath().normalize().toFile().isFile())
                    throw new SimpleCommandExceptionType(Text.translatable("text.filereader.permission.fail.invaidfile",context.getArgument("FileName", String.class))).create();
                FilePermissions.WorldTextPermission.GivePermission(context.getSource().getEntityOrThrow(), context.getArgument("FileName",String.class));
            }
            else if(fs == FileSource.global)
            {
                if(!Files.exists(Paths.get(FileIO.GlobalTextPath.toAbsolutePath().normalize().toString(),context.getArgument("FileName", String.class)).toAbsolutePath().normalize()) ||
                        !Paths.get(FileIO.GlobalTextPath.toAbsolutePath().normalize().toString(),context.getArgument("FileName", String.class)).toAbsolutePath().normalize().toFile().isFile())
                    throw new SimpleCommandExceptionType(Text.translatable("text.filereader.permission.fail.invaidfile",context.getArgument("FileName", String.class))).create();
                FilePermissions.GlobalTextPermission.GivePermission(context.getSource().getEntityOrThrow(),context.getArgument("FileName",String.class));
            }
        }
        catch(CommandSyntaxException ex)
        {
            throw ex;
        }
        catch (Exception e) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception",e.getClass().getCanonicalName(),e.getMessage())).create();
        }
        context.getSource().sendMessage(Text.translatable("text.filereader.permission.success.give",context.getArgument("FileName",String.class),context.getSource().getEntityOrThrow().getEntityName()));
        return 0;
    }
    public static int GivePermission(CommandContext<ServerCommandSource> context,FileSource fs) throws CommandSyntaxException
    {
        GetSender(context);
        try {
        if(fs == FileSource.save)
        {
            if(!Files.exists(Paths.get(context.getSource().getServer().getSavePath(WorldSavePath.ROOT).getParent().toAbsolutePath().normalize().toString(),"Texts",context.getArgument("FileName", String.class)).toAbsolutePath().normalize()) ||
                    !Paths.get(context.getSource().getServer().getSavePath(WorldSavePath.ROOT).getParent().toAbsolutePath().normalize().toString(),"Texts",context.getArgument("FileName", String.class)).toAbsolutePath().normalize().toFile().isFile())
                throw new SimpleCommandExceptionType(Text.translatable("text.filereader.permission.fail.invaidfile",context.getArgument("FileName", String.class))).create();
            EntityArgumentType.getEntities(context,"Player").forEach(i->{
                try {
                    FilePermissions.WorldTextPermission.GivePermission(i, context.getArgument("FileName",String.class));
                } catch (Exception e) {
                    mod.Log.error("",e);
                }
            });
        }
        else if(fs == FileSource.global)
        {
            if(!Files.exists(Paths.get(FileIO.GlobalTextPath.toAbsolutePath().normalize().toString(),context.getArgument("FileName", String.class)).toAbsolutePath().normalize()) ||
                    !Paths.get(FileIO.GlobalTextPath.toAbsolutePath().normalize().toString(),context.getArgument("FileName", String.class)).toAbsolutePath().normalize().toFile().isFile())
                throw new SimpleCommandExceptionType(Text.translatable("text.filereader.permission.fail.invaidfile",context.getArgument("FileName", String.class))).create();
            EntityArgumentType.getEntities(context,"Player").forEach(i->{
                try {
                    FilePermissions.GlobalTextPermission.GivePermission(i,context.getArgument("FileName",String.class));
                } catch (Exception e) {
                    mod.Log.error("",e);
                }
            });
        }
        }
        catch(CommandSyntaxException ex)
        {
            throw ex;
        }
        catch (Exception e) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception",e.getClass().getCanonicalName(),e.getMessage())).create();
        }
        StringBuilder sb=new StringBuilder();
        EntityArgumentType.getEntities(context,"Player").forEach(i ->{sb.append(i.getEntityName()).append(',');});
        context.getSource().sendMessage(Text.translatable("text.filereader.permission.success.give",context.getArgument("FileName",String.class),sb.toString()));
        return 0;
    }
    public static int RemovePermissionMyself(CommandContext<ServerCommandSource> context,FileSource fs) throws CommandSyntaxException
    {
        GetSender(context);
        try {
            if(fs == FileSource.save)
            {
                if(!Files.exists(Paths.get(context.getSource().getServer().getSavePath(WorldSavePath.ROOT).getParent().toAbsolutePath().normalize().toString(),"Texts",context.getArgument("FileName", String.class)).toAbsolutePath().normalize()) ||
                        !Paths.get(context.getSource().getServer().getSavePath(WorldSavePath.ROOT).getParent().toAbsolutePath().normalize().toString(),"Texts",context.getArgument("FileName", String.class)).toAbsolutePath().normalize().toFile().isFile())
                    throw new SimpleCommandExceptionType(Text.translatable("text.filereader.permission.fail.invaidfile",context.getArgument("FileName", String.class))).create();
                FilePermissions.WorldTextPermission.RemovePermission(context.getSource().getEntityOrThrow(), context.getArgument("FileName",String.class),context.getSource().getServer().isOnlineMode());
            }
            if(fs == FileSource.global)
            {
                FilePermissions.GlobalTextPermission.RemovePermission(context.getSource().getEntityOrThrow(),context.getArgument("FileName",String.class),context.getSource().getServer().isOnlineMode());
            }
        }
        catch(CommandSyntaxException ex)
        {
            throw ex;
        }
        catch (Exception e) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception",e.getClass().getCanonicalName(),e.getMessage())).create();
        }
        context.getSource().sendMessage(Text.translatable("text.filereader.permission.success.remove",context.getArgument("FileName",String.class),context.getSource().getEntityOrThrow().getEntityName()));
        return 0;
    }
    public static int RemovePermission(CommandContext<ServerCommandSource> context,FileSource fs) throws CommandSyntaxException
    {
        GetSender(context);
        try {
            if(fs == FileSource.save)
            {
                if(!Files.exists(Paths.get(context.getSource().getServer().getSavePath(WorldSavePath.ROOT).getParent().toAbsolutePath().normalize().toString(),"Texts",context.getArgument("FileName", String.class)).toAbsolutePath().normalize()) ||
                        !Paths.get(context.getSource().getServer().getSavePath(WorldSavePath.ROOT).getParent().toAbsolutePath().normalize().toString(),"Texts",context.getArgument("FileName", String.class)).toAbsolutePath().normalize().toFile().isFile())
                    throw new SimpleCommandExceptionType(Text.translatable("text.filereader.permission.fail.invaidfile",context.getArgument("FileName", String.class))).create();
                EntityArgumentType.getEntities(context,"Player").forEach(i->{
                    try {
                        FilePermissions.WorldTextPermission.RemovePermission(i, context.getArgument("FileName",String.class),context.getSource().getServer().isOnlineMode());
                    } catch (Exception e) {
                        mod.Log.error("",e);
                    }
                });
            }
            if(fs == FileSource.global)
            {
                if(!Files.exists(Paths.get(FileIO.GlobalTextPath.toAbsolutePath().normalize().toString(),context.getArgument("FileName", String.class)).toAbsolutePath().normalize()) ||
                        !Paths.get(FileIO.GlobalTextPath.toAbsolutePath().normalize().toString(),context.getArgument("FileName", String.class)).toAbsolutePath().normalize().toFile().isFile())
                    throw new SimpleCommandExceptionType(Text.translatable("text.filereader.permission.fail.invaidfile",context.getArgument("FileName", String.class))).create();
                EntityArgumentType.getEntities(context,"Player").forEach(i->{
                    try {
                        FilePermissions.GlobalTextPermission.RemovePermission(i,context.getArgument("FileName",String.class),context.getSource().getServer().isOnlineMode());
                    } catch (Exception e) {
                        mod.Log.error("",e);
                    }
                });
            }
        }
        catch(CommandSyntaxException ex)
        {
            throw ex;
        }
        catch (Exception e) {
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.exception",e.getClass().getCanonicalName(),e.getMessage())).create();
        }
        StringBuilder sb=new StringBuilder();
        EntityArgumentType.getEntities(context,"Player").forEach(i ->{sb.append(i.getEntityName()).append(',');});
        context.getSource().sendMessage(Text.translatable("text.filereader.permission.success.remove",context.getArgument("FileName",String.class),sb.toString()));
        return 0;
    }
    public  static  sender GetSender(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
    {
        ServerCommandSource source = context.getSource();
        Entity sender = source.getEntity();
        if(sender == null)
        {
            if(source.getName() .equals("Server") ) return cn.thecoldworld.textfilereader.sender.console;
            throw new SimpleCommandExceptionType(Text.translatable("text.filereader.invaidsender")).create();
        }
        source.getEntityOrThrow();
        return cn.thecoldworld.textfilereader.sender.player;
    }
}
