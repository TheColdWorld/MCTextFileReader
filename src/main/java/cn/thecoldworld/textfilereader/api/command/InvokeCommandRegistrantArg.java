package cn.thecoldworld.textfilereader.api.command;

import com.mojang.brigadier.builder.ArgumentBuilder;

public class InvokeCommandRegistrantArg<S> {
    public final ArgumentBuilder<S, ?> arg;
    public final CommandRegistrantSrcType commandRegistrantSrcType;

    public InvokeCommandRegistrantArg(ArgumentBuilder<S, ?> arg, CommandRegistrantSrcType commandRegistrantSrcType) {
        this.arg = arg;
        this.commandRegistrantSrcType = commandRegistrantSrcType;
    }
}
