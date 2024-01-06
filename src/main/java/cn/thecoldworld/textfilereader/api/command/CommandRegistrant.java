package cn.thecoldworld.textfilereader.api.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @param <Src> The command source<br>Minecraft Server : ServerCommandSource <br> Minecraft Client : ClientCommandManager
 */
public class CommandRegistrant<Src> {
    public final String CommandNode;
    public final CommandRegisterEvent<Src> action;
    public final CommandRegistrantSrcType CommandRegistrantSrcType;

    /**
     * @param commandNode              in "action" parameter will Be used "ArgumentBuilder"<br>use spaces to separate each command node
     * @param action                   the invoked action whenI it is registered
     * @param commandRegistrantSrcType the type of source
     */
    public CommandRegistrant(String commandNode, CommandRegisterEvent<Src> action, cn.thecoldworld.textfilereader.api.command.CommandRegistrantSrcType commandRegistrantSrcType) {
        CommandNode = commandNode;
        this.action = action;
        CommandRegistrantSrcType = commandRegistrantSrcType;
        CommandRegistrants.Register(this);
    }


    /**
     * @param commandNode              in "action" parameter will Be used "ArgumentBuilder"<br>use spaces to separate each command node
     * @param action                   the invoked action whenI it is registered
     * @param commandRegistrantSrcType the type of source
     */
    @Contract("_, _,_ -> new")
    public static <Src> @NotNull CommandRegistrant<Src> Register(String commandNode, CommandRegisterEvent<Src> action, cn.thecoldworld.textfilereader.api.command.CommandRegistrantSrcType commandRegistrantSrcType) {
        return new CommandRegistrant<>(commandNode, action, commandRegistrantSrcType);
    }


    public void run(ArgumentBuilder<Src, ?> argumentBuilder) {
        action.Register(argumentBuilder);
    }

    @FunctionalInterface
    public interface CommandRegisterEvent<Src> {
        void Register(ArgumentBuilder<Src, ?> arg);
    }
}
