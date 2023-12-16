package cn.thecoldworld.textfilereader.client.screen;

import cn.thecoldworld.textfilereader.client.screen.widgets.TextPagesWidget;
import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TextGUI extends Screen {
    public final String FileName;
    private final Screen prevScreen;
    public Text MainText;
    public List<Text> TextPages;
    private int page;

    public TextGUI(@NotNull Screen prevScreen, MinecraftClient client, String FileName) {
        super(Text.translatable("gui.filereader.read.title", FileName));
        this.prevScreen = prevScreen;
        this.client = client;
        MainText = Text.empty();
        this.FileName = FileName;
    }

    public void SetPage(int page) {
        if (TextPages == null || page < 0 || page >= TextPages.size()) return;
        this.page = page;
        this.SetText(TextPages.get(page));
    }

    public TextGUI SetText(Text text) {
        MainText = text;
        client.execute(this::clearAndInit);
        return this;
    }

    public TextGUI SetText(String text) {
        MainText = Text.literal(text);
        client.execute(this::clearAndInit);
        return this;
    }

    public TextGUI SetTextPage(List<Text> texts) {
        TextPages = texts;
        if (!TextPages.isEmpty()) this.SetPage(0);
        return this;
    }

    public TextGUI SetTextPage(String... texts) {
        TextPages = new LinkedList<>();
        for (String i : texts) {
            TextPages.add(Text.literal(i));
        }
        if (!TextPages.isEmpty()) this.SetPage(0);
        return this;
    }

    @Override
    public boolean shouldPause() {
        return variables.ClientModSettings.isPauseGame();
    }

    @Override
    public void init() {
        TextPagesWidget scrollableTextWidget = new TextPagesWidget(0, 0, width, height - 25, MainText.copy(), textRenderer);
        addDrawableChild(scrollableTextWidget);
        ButtonWidget NextPageButton = ButtonWidget.builder(Text.translatable("createWorld.customize.custom.next"), button -> {
            try {
                if (TextPages == null || page == TextPages.size()) return;
                this.SetPage(page + 1);
            } catch (Exception e) {
                variables.Log.error("", e);
            }
        }).dimensions(width / 2, height - 25, 200, 20).build();
        ButtonWidget LastPageButton = ButtonWidget.builder(Text.translatable("createWorld.customize.custom.prev"), button ->
                {
                    try {
                        if (TextPages == null || page == 0) return;
                        this.SetPage(page - 1);
                    } catch (Exception e) {
                        variables.Log.error("", e);
                    }
                })
                .dimensions(width / 2 - 205, height - 25, 200, 20)
                .build();
        addDrawableChild(NextPageButton);
        addDrawableChild(LastPageButton);
    }

    @Override
    public void clearAndInit() {
        super.clearAndInit();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderBackgroundTexture(context);
    }

    @Override
    public void close() {
        client.setScreen(prevScreen);
    }

    public int getPage() {
        return page;
    }
}
