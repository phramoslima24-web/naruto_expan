package net.narutotoexpansion.data;

import net.narutotoexpansion.config.ExpansionConfig;

/**
 * Transformation rules. Four form levels:
 * 1 Chakra Cloak, 2 Empowered Cloak, 3 Beast Cloak, 4 Beast Avatar (placeholder visuals, full stat effects).
 * Level n unlocks at mastery BASE[n] * beast difficulty (own formula, independent of other mods).
 */
public final class FormRules {
    public static final int MAX_FORM = 4;
    private static final int[] BASE_THRESHOLD = {0, 0, 120, 320, 650};

    public static int threshold(BeastType type, int form) {
        if (form < 1 || form > MAX_FORM) return Integer.MAX_VALUE;
        return Math.round(BASE_THRESHOLD[form] * type.difficulty);
    }

    /** Highest form level unlocked with this mastery (always at least 1). */
    public static int unlocked(BeastType type, int mastery) {
        int result = 1;
        for (int f = 2; f <= MAX_FORM; f++) {
            if (mastery >= threshold(type, f)) result = f;
        }
        return result;
    }

    public static double poolMax(int mastery) {
        return 100.0 + mastery * 0.4;
    }

    public static int clampMastery(int mastery) {
        return Math.max(0, Math.min(ExpansionConfig.MAX_MASTERY.get(), mastery));
    }

    private FormRules() {}
}
