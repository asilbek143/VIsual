package uz.dexvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DexScreen extends Screen {
    public DexScreen() {
        super(Text.literal("DEX VISUAL"));
    }

    private static Text label(Module m) {
        return Text.literal(m.name + ": " + (m.enabled ? "\u00a7aON" : "\u00a7cOFF"));
    }

    @Override
    protected void init() {
        int bw = 130, bh = 20, gap = 6;
        int totalW = bw * 2 + gap;
        int startX = (width - totalW) / 2;
        int startY = 34;
        int i = 0;
        for (Module m : Modules.ALL) {
            int col = i % 2;
            int row = i / 2;
            int x = startX + col * (bw + gap);
            int y = startY + row * (bh + gap);
            ButtonWidget btn = ButtonWidget.builder(label(m), b -> {
                m.toggle();
                b.setMessage(label(m));
            }).dimensions(x, y, bw, bh).build();
            addDrawableChild(btn);
            i++;
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "DEX VISUAL  |  1.21.4", width / 2, 14, 0xFF55FFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        Config.save();
    }
}
