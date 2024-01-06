package cn.thecoldworld.textfilereader.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public abstract class BasicGui extends Screen {
    public final Screen PrevScreen;

    protected BasicGui(MinecraftClient client, Text title, @Nullable Screen prevScreen) {
        super(title);
        PrevScreen = prevScreen;
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public void close() {
        this.client.setScreen(PrevScreen);
    }

    @Override
    public abstract boolean shouldPause();
}
