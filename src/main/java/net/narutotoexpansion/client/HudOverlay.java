package net.narutotoexpansion.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.narutotoexpansion.config.ExpansionConfig;
import net.narutotoexpansion.data.BeastType;
import net.narutotoexpansion.data.ExpansionCapability;
import net.narutotoexpansion.data.ExpansionData;
import net.narutotoexpansion.data.FormRules;

/** Simple HUD for hosts (bottom-left): beast, form, mode, beast chakra pool, mastery and Bijuudama charge. */
public final class HudOverlay {
    private static final int BAR_W = 110;
    private static final int BAR_H = 6;

    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;
        ExpansionData data = ExpansionCapability.get(player);
        BeastType type = BeastType.byId(data.hostBeast);
        if (type == null) return;

        int x = 8;
        int y = screenHeight - 62;
        int mastery = data.mastery[type.id];
        int unlocked = FormRules.unlocked(type, mastery);

        Component title = Component.translatable(type.translationKey())
                .append(Component.literal("  "))
                .append(Component.translatable("hud.narutoto_expansion.form", data.form, unlocked));
        g.drawString(mc.font, title, x, y, 0xFF000000 | type.color, true);
        g.drawString(mc.font, Component.translatable(data.mode.translationKey()), x, y + 10, 0xFFE0E0E0, true);

        // beast chakra pool
        double max = FormRules.poolMax(mastery);
        bar(g, x, y + 22, (float) (data.pool / max), 0xFF000000 | type.color);
        g.drawString(mc.font, String.format("%.0f/%.0f", data.pool, max), x + BAR_W + 4, y + 21, 0xFFFFFFFF, true);

        // mastery toward the next form (or maximum)
        int cap = ExpansionConfig.MAX_MASTERY.get();
        int next = unlocked < FormRules.MAX_FORM ? FormRules.threshold(type, unlocked + 1) : cap;
        bar(g, x, y + 32, Math.min(1.0f, mastery / (float) Math.max(1, next)), 0xFFB0A040);
        g.drawString(mc.font, mastery + "/" + next, x + BAR_W + 4, y + 31, 0xFFFFFFFF, true);

        // Bijuudama charge or cooldown
        if (data.charging) {
            bar(g, x, y + 42, data.chargeTicks / (float) ExpansionConfig.BOMB_CHARGE_TICKS.get(), 0xFFFFFFFF);
        } else if (data.bombCooldown > 0) {
            bar(g, x, y + 42, 1.0f - data.bombCooldown / (float) Math.max(1, ExpansionConfig.BOMB_COOLDOWN_TICKS.get()), 0xFF707070);
        }
    }

    private static void bar(GuiGraphics g, int x, int y, float fill, int color) {
        fill = Math.max(0.0f, Math.min(1.0f, fill));
        g.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, 0xCC000000);
        g.fill(x, y, x + (int) (BAR_W * fill), y + BAR_H, color);
    }

    private HudOverlay() {}
}
