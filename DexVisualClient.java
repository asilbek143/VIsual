package uz.dexvisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;
import uz.dexvisual.mixin.SimpleOptionAccessor;

public class DexVisualClient implements ClientModInitializer {
    public static KeyBinding menuKey;
    public static KeyBinding zoomKey;

    private static boolean gammaApplied = false;
    private static double savedGamma = 1.0;
    private static boolean zooming = false;
    private static int savedFov = 70;
    private static boolean lastHitboxes = false;

    private static Entity lastTarget;
    private static long lastTargetTime;

    private static final int ZOOM_FOV = 25;
    private static final double FULLBRIGHT_GAMMA = 16.0;

    @Override
    public void onInitializeClient() {
        Config.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dexvisual.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.dexvisual"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dexvisual.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.dexvisual"));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && entity instanceof LivingEntity) {
                lastTarget = entity;
                lastTargetTime = System.currentTimeMillis();
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(DexVisualClient::tick);
        HudRenderCallback.EVENT.register(Hud::render);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            restoreOptions(client);
            Config.save();
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> void force(SimpleOption<T> option, T value) {
        ((SimpleOptionAccessor<T>) (Object) option).dex$setValue(value);
    }

    private static void tick(MinecraftClient mc) {
        while (menuKey.wasPressed()) {
            if (mc.currentScreen == null) mc.setScreen(new DexScreen());
        }
        if (mc.player == null || mc.world == null) return;

        // Fullbright
        if (Modules.FULLBRIGHT.enabled) {
            if (!gammaApplied) {
                savedGamma = mc.options.getGamma().getValue();
                gammaApplied = true;
            }
            force(mc.options.getGamma(), FULLBRIGHT_GAMMA);
        } else if (gammaApplied) {
            force(mc.options.getGamma(), savedGamma);
            gammaApplied = false;
        }

        // Zoom
        boolean zoomHeld = Modules.ZOOM.enabled && mc.currentScreen == null && zoomKey.isPressed();
        if (zoomHeld) {
            if (!zooming) {
                savedFov = mc.options.getFov().getValue();
                zooming = true;
            }
            force(mc.options.getFov(), ZOOM_FOV);
        } else if (zooming) {
            force(mc.options.getFov(), savedFov);
            zooming = false;
        }

        // Hitboxes
        if (Modules.HITBOXES.enabled != lastHitboxes) {
            mc.getEntityRenderDispatcher().setRenderHitboxes(Modules.HITBOXES.enabled);
            lastHitboxes = Modules.HITBOXES.enabled;
        }
    }

    private static void restoreOptions(MinecraftClient mc) {
        if (mc.options == null) return;
        if (gammaApplied) {
            force(mc.options.getGamma(), savedGamma);
            gammaApplied = false;
        }
        if (zooming) {
            force(mc.options.getFov(), savedFov);
            zooming = false;
        }
    }

    // ---- ESP (glow outline, visible through walls) ----

    /** Entity you last hit (4s) or are looking at. */
    public static LivingEntity currentTarget() {
        if (!Modules.TARGET_ESP.enabled) return null;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (lastTarget instanceof LivingEntity le && le != mc.player && le.isAlive() && !le.isRemoved()
                && System.currentTimeMillis() - lastTargetTime < 4000) {
            return le;
        }
        if (mc.targetedEntity instanceof LivingEntity le2 && le2 != mc.player) return le2;
        return null;
    }

    public static boolean espGlow(Entity e) {
        if (e == null || !e.getWorld().isClient) return false;
        if (e == MinecraftClient.getInstance().player) return false;
        if (e == currentTarget()) return true;
        if (e instanceof PlayerEntity) return Modules.ESP_PLAYERS.enabled;
        if (e instanceof Monster) return Modules.ESP_HOSTILE.enabled;
        if (e instanceof AnimalEntity) return Modules.ESP_ANIMALS.enabled;
        if (e instanceof ItemEntity) return Modules.ESP_ITEMS.enabled;
        return false;
    }

    /** RGB color for the outline, or -1 for default. */
    public static int espColor(Entity e) {
        if (!espGlow(e)) return -1;
        if (e == currentTarget() && e instanceof LivingEntity le) {
            float r = Math.max(0f, Math.min(1f, le.getHealth() / Math.max(1f, le.getMaxHealth())));
            return ((int) (255 * (1 - r)) << 16) | ((int) (255 * r) << 8);
        }
        if (e instanceof PlayerEntity) return 0xFF5555;
        if (e instanceof Monster) return 0xFFAA00;
        if (e instanceof AnimalEntity) return 0x55FF55;
        if (e instanceof ItemEntity) return 0x55FFFF;
        return -1;
    }
}
