package cn.thecoldworld.textfilereader.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Environment (EnvType.CLIENT)
public class MainGUI extends Screen {
    @NotNull
    private final Screen prevScreen;

    public MainGUI(@NotNull Screen prevScreen) {
        super(Text.literal("Test"));
        this.prevScreen = prevScreen;
    }

    @Override
    protected void init() {
        ButtonWidget button1 = ButtonWidget.builder(Text.literal("按钮 1"), button -> {
                })
                .dimensions(width / 2 - 205, 20, 200, 20)
                .tooltip(Tooltip.of(Text.literal("按钮 1 的提示")))
                .build();
        addDrawableChild(button1);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        //MultilineText.create(textRenderer, Text.literal("这个文本很长 ".repeat(20)), width - 20).drawWithShadow(context, 10, height / 2, 16, 0xffffff);
    }

    @Override
    public void close() {
        client.setScreen(prevScreen);
    }
}
