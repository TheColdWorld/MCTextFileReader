package cn.thecoldworld.textfilereader.api.command;

import com.mojang.brigadier.builder.ArgumentBuilder;

import java.util.LinkedList;

public final class CommandRegistrants {
    private static final LinkedList<Runnable> registerFunctions = new LinkedList<>();
    private static final LinkedList<CommandRegistrant<?>> registrants = new LinkedList<>();

    public static void RegisterRegistrantFunction(Runnable action) {
        registerFunctions.add(action);
    }

    public static void ReRegisterCommandRegistrants() {
        registrants.clear();
        for (Runnable action : registerFunctions) {
            action.run();
        }
    }

    public static void Register(CommandRegistrant<?> commandRegistrant) {
        registrants.add(commandRegistrant);
    }

    public static void InvokeCommandRegistrants(String commandNode, InvokeCommandRegistrantArg arg) {
        registrants.stream().filter(commandRegistrant -> commandRegistrant.CommandNode.equals(commandNode)
                && commandRegistrant.CommandRegistrantSrcType.equals(arg.commandRegistrantSrcType)).forEach(i -> i.action.Register(arg.arg));
    }

    public static <S> ArgumentBuilder<S, ?> InvokeCommandRegistrants(String commandNode, CommandRegistrantSrcType commandRegistrantSrcType, ArgumentBuilder<S, ?> argumentBuilder) {
        registrants.stream().filter(commandRegistrant -> commandRegistrant.CommandNode.equals(commandNode)
                && commandRegistrant.CommandRegistrantSrcType.equals(commandRegistrantSrcType)).forEach(i -> i.action.Register((ArgumentBuilder) argumentBuilder));
        return argumentBuilder;
    }
}
