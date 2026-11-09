package net.narutotoexpansion.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.narutotoexpansion.NarutotoExpansion;
import net.narutotoexpansion.content.entity.ModEntities;
import net.narutotoexpansion.data.BeastType;
import net.narutotoexpansion.network.ExpansionNetwork;

/**
 * Client wiring: renderers, model layers, key mappings, HUD and key handling.
 * Default keys (G, H, B) were chosen to avoid Narutoto's own C / X / Z; all can be rebound in Controls.
 */
public final class ClientSetup {
    public static final KeyMapping KEY_TRANSFORM = new KeyMapping("key.narutoto_expansion.transform",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_H, "key.categories.narutoto_expansion");
    public static final KeyMapping KEY_MODE = new KeyMapping("key.narutoto_expansion.mode",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_G, "key.categories.narutoto_expansion");
    public static final KeyMapping KEY_BOMB = new KeyMapping("key.narutoto_expansion.bijuudama",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_B, "key.categories.narutoto_expansion");

    @Mod.EventBusSubscriber(modid = NarutotoExpansion.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(KEY_TRANSFORM);
            event.register(KEY_MODE);
            event.register(KEY_BOMB);
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(BeastModel.LAYER, BeastModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            for (BeastType type : BeastType.implemented()) {
                event.registerEntityRenderer(ModEntities.BEASTS.get(type).get(), BeastRenderer::new);
            }
            event.registerEntityRenderer(ModEntities.BIJUUDAMA.get(), BijuudamaRenderer::new);
        }

        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("bijuu_hud", (gui, graphics, partialTick, width, height) -> HudOverlay.render(graphics, width, height));
        }
    }

    @Mod.EventBusSubscriber(modid = NarutotoExpansion.MODID, value = Dist.CLIENT)
    public static final class ForgeBus {
        private static boolean bombHeld;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) {
                bombHeld = false;
                return;
            }
            while (KEY_TRANSFORM.consumeClick()) {
                ExpansionNetwork.sendToServer(mc.player.isShiftKeyDown()
                        ? ExpansionNetwork.Action.SELECT_FORM : ExpansionNetwork.Action.TOGGLE_FORM);
            }
            while (KEY_MODE.consumeClick()) {
                ExpansionNetwork.sendToServer(ExpansionNetwork.Action.CYCLE_MODE);
            }
            boolean down = KEY_BOMB.isDown();
            if (down && !bombHeld) {
                ExpansionNetwork.sendToServer(ExpansionNetwork.Action.BOMB_START);
            } else if (!down && bombHeld) {
                ExpansionNetwork.sendToServer(ExpansionNetwork.Action.BOMB_RELEASE);
            }
            bombHeld = down;
        }
    }

    private ClientSetup() {}
}
