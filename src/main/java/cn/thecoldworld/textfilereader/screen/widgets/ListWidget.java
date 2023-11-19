package cn.thecoldworld.textfilereader.screen.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ListWidget extends EntryListWidget<ListWidget.Entry> {
    private static final Identifier scroller_texture = new Identifier("widget/scroller");

    public ListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawGuiTexture(scroller_texture, width - 11, top, 11, height);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, Text.literal("List"));
    }


    public static class Entry extends EntryListWidget.Entry<Entry> {
        MultilineTextWidget widget;

        public Entry(MultilineTextWidget textWidget) {
            widget = textWidget;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            widget.render(context, mouseX, mouseY, tickDelta);
        }
    }
}
