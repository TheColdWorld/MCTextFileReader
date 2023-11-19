package cn.thecoldworld.textfilereader.screen;

import cn.thecoldworld.textfilereader.screen.widgets.ListWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Environment (EnvType.CLIENT)
public class MainGUI extends Screen {
    @NotNull
    private final Screen prevScreen;
    private MultilineTextWidget textWidget;
    private Text MainText;

    public MainGUI(@NotNull Screen prevScreen) {
        super(Text.literal("Test"));
        this.prevScreen = prevScreen;
        MainText = Text.empty();
    }

    @Override
    protected void init() {
        textWidget = new MultilineTextWidget(0, height / 3, Text.empty(), textRenderer);
        ButtonWidget button1 = ButtonWidget.builder(Text.literal("按钮 1"), button -> {
                    setText(Text.translatable("text.filereader.description")
                    );

                })
                .dimensions(width / 2 - 205, 20, 200, 20)
                .tooltip(Tooltip.of(Text.literal("按钮 1 的提示")))
                .build();
        addDrawableChild(button1);
        textWidget.active = true;
        textWidget.visible = true;
        textWidget.setMessage(MainText);
        textWidget.setFocused(false);
        textWidget.setMaxWidth(width);
        textWidget.setMaxRows(Integer.MAX_VALUE);
        textWidget.setCentered(false);
        ListWidget widget1 = new ListWidget(client, width, height, height / 3, height, 50);
        widget1.children().add(new ListWidget.Entry(textWidget));
        addDrawable(widget1);
    }

    public Text getText() {
        return this.MainText;
    }

    public void setText(Text text) {
        MainText = text;
        textWidget.setMessage(MainText);
    }

    public void setText(String text) {
        MainText = Text.literal(text);
        textWidget.setMessage(MainText);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.setScreen(prevScreen);
    }
}
