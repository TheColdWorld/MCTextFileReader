package cn.thecoldworld.textfilereader.exceptions;

import net.minecraft.text.Text;

public class TranslatableException extends Throwable {
    public final String TranslateKey;
    public final Object[] Args;

    public TranslatableException(String translateKey, Object... args) {

        this.TranslateKey = translateKey;
        Args = args;
    }

    public String GetMessage() {
        return Text.translatable(TranslateKey, Args).getString();
    }
}
