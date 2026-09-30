package uz.dexvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Hud {
    private static final int ACCENT = 0xFF55FFFF;

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        TextRenderer tr = mc.textRenderer;
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        int y = 4;
        if (Modules.WATERMARK.enabled) {
            ctx.drawTextWithShadow(tr, "DEX VISUAL", 4, y, ACCENT);
            y += 12;
        }

        if (Modules.INFO_HUD.enabled) {
            List<String> lines = new ArrayList<>();
            lines.add("FPS: " + mc.getCurrentFps());
            lines.add("XYZ: " + mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ());
            lines.add("Facing: " + mc.player.getHorizontalFacing().asString());
            if (mc.getNetworkHandler() != null) {
                PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (entry != null) lines.add("Ping: " + entry.getLatency() + "ms");
            }
            for (String s : lines) {
                ctx.drawTextWithShadow(tr, s, 4, y, 0xFFFFFFFF);
                y += 10;
            }
        }

        if (Modules.MODULE_LIST.enabled) {
            List<String> names = new ArrayList<>();
            for (Module m : Modules.ALL) if (m.enabled) names.add(m.name);
            names.sort(Comparator.comparingInt((String s) -> tr.getWidth(s)).reversed());
            int ry = 4;
            for (String n : names) {
                ctx.drawTextWithShadow(tr, n, sw - tr.getWidth(n) - 4, ry, ACCENT);
                ry += 10;
            }
        }

        if (Modules.KEYSTROKES.enabled) {
            int x = 6, ky = sh - 90;
            key(ctx, tr, x + 22, ky, 20, "W", mc.options.forwardKey.isPressed());
            key(ctx, tr, x, ky + 22, 20, "A", mc.options.leftKey.isPressed());
            key(ctx, tr, x + 22, ky + 22, 20, "S", mc.options.backKey.isPressed());
            key(ctx, tr, x + 44, ky + 22, 20, "D", mc.options.rightKey.isPressed());
            key(ctx, tr, x, ky + 44, 64, "SPACE", mc.options.jumpKey.isPressed());
        }

        LivingEntity target = DexVisualClient.currentTarget();
        if (target != null) {
            int bx = sw / 2 - 50, by = sh / 2 + 30;
            float hp = target.getHealth(), max = Math.max(1f, target.getMaxHealth());
            float r = Math.max(0f, Math.min(1f, hp / max));
            int color = 0xFF000000 | ((int) (255 * (1 - r)) << 16) | ((int) (255 * r) << 8);
            ctx.fill(bx - 4, by - 4, bx + 104, by + 26, 0x88000000);
            ctx.drawTextWithShadow(tr, target.getName().getString(), bx, by, 0xFFFFFFFF);
            ctx.fill(bx, by + 12, bx + 100, by + 18, 0xFF333333);
            ctx.fill(bx, by + 12, bx + (int) (100 * r), by + 18, color);
            ctx.drawTextWithShadow(tr, String.format("%.1f / %.1f", hp, max), bx, by + 19, 0xFFAAAAAA);
        }

        if (Modules.ARMOR_HUD.enabled) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            int ax = sw - 22;
            int ay = sh / 2 - 36;
            for (EquipmentSlot slot : slots) {
                ItemStack stack = mc.player.getEquippedStack(slot);
                if (!stack.isEmpty()) {
                    ctx.drawItem(stack, ax, ay);
                    ctx.drawStackOverlay(tr, stack, ax, ay);
                }
                ay += 18;
            }
        }
    }

    private static void key(DrawContext ctx, TextRenderer tr, int x, int y, int w, String label, boolean down) {
        ctx.fill(x, y, x + w, y + 20, down ? 0xAA55FFFF : 0x88000000);
        ctx.drawCenteredTextWithShadow(tr, label, x + w / 2, y + 6, 0xFFFFFFFF);
    }

    private Hud() {}
}
