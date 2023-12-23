package cn.thecoldworld.textfilereader.client.screen.widgets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;

import java.util.List;

@Environment(EnvType.CLIENT)
public class FileListWidget extends AlwaysSelectedEntryListWidget<FileListWidget.Entry> {
    private final TextRenderer textRenderer;

    public FileListWidget(MinecraftClient client, int width, int height, int top, int itemHeight, TextRenderer textRenderer) {
        super(client, width, height, top, itemHeight);
        this.textRenderer = textRenderer;
        setRenderBackground(false);
    }

    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 20;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    public void AddChildren(String FileName) {
        this.children().add(new Entry(FileName));
    }

    public void AddChildren(List<String> FileNames) {
        FileNames.forEach(i -> this.children().add(new Entry(i)));
    }


    @Environment(EnvType.CLIENT)
    public class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
        public final String FileName;

        public Entry(String fileName) {
            FileName = fileName;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.onPressed();
            return true;
        }

        @Override
        public Text getNarration() {
            return Text.translatable("narrator.select", FileName);
        }

        void onPressed() {
            FileListWidget.this.setSelected(this);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawCenteredTextWithShadow(FileListWidget.this.textRenderer, FileName, FileListWidget.this.width / 2, y + 1, 16777215);
        }
    }
}
