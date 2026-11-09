package net.narutotoexpansion.data;

import javax.annotation.Nullable;

/**
 * The nine tailed beasts. Ids 1-9 follow the number of tails.
 * <p>
 * Kurama (9) is {@link #isExternal() external}: Narutoto already implements its jinchuriki, forms and bomb, so this
 * add-on never hosts it and only recognises its host (see NarutotoBridge#isNineTailsHost) to avoid duplicated systems.
 */
public enum BeastType {
    SHUKAKU(1, "shukaku", 0xC9A35B, 0x7A5C2E, 0.8f, 0.9f, 3.2f, 3.0f, false),
    MATATABI(2, "matatabi", 0x4C7FC9, 0x1D2F6B, 0.9f, 0.95f, 2.4f, 2.2f, true),
    ISOBU(3, "isobu", 0x6C8E7E, 0x2C4A3E, 1.0f, 1.0f, 3.0f, 2.6f, false),
    SON_GOKU(4, "son_goku", 0xC4452F, 0x6B1D12, 1.05f, 1.05f, 3.0f, 3.4f, true),
    KOKUO(5, "kokuo", 0xE8E4DA, 0x8A857A, 1.1f, 1.1f, 2.8f, 2.8f, false),
    SAIKEN(6, "saiken", 0x8FB7D9, 0x3F6E96, 1.15f, 1.1f, 2.8f, 2.4f, false),
    CHOMEI(7, "chomei", 0x6FAE4F, 0x2F5B1C, 1.2f, 1.15f, 2.6f, 2.4f, false),
    GYUKI(8, "gyuki", 0xB9783A, 0x5E3512, 1.3f, 1.25f, 3.4f, 3.2f, false),
    KURAMA(9, "kurama", 0xE2872B, 0x8F3E0B, 1.4f, 1.4f, 3.6f, 3.4f, true);

    public final int id;
    public final String key;
    public final int color;
    public final int accentColor;
    /** Multiplies the mastery thresholds of each form (bigger = harder to master). */
    public final float difficulty;
    /** Multiplies attribute bonuses, bomb radius and wild-beast stats. */
    public final float power;
    public final float width;
    public final float height;
    public final boolean fireImmune;

    BeastType(int id, String key, int color, int accent, float difficulty, float power,
              float width, float height, boolean fireImmune) {
        this.id = id;
        this.key = key;
        this.color = color;
        this.accentColor = accent;
        this.difficulty = difficulty;
        this.power = power;
        this.width = width;
        this.height = height;
        this.fireImmune = fireImmune;
    }

    /** True for Kurama: handled by Narutoto itself. */
    public boolean isExternal() {
        return this == KURAMA;
    }

    public int tails() {
        return id;
    }

    public String translationKey() {
        return "beast.narutoto_expansion." + key;
    }

    @Nullable
    public static BeastType byId(int id) {
        for (BeastType t : values()) {
            if (t.id == id) return t;
        }
        return null;
    }

    @Nullable
    public static BeastType byKey(String key) {
        for (BeastType t : values()) {
            if (t.key.equalsIgnoreCase(key)) return t;
        }
        return null;
    }

    /** Beasts this add-on implements (everything except the external Kurama). */
    public static BeastType[] implemented() {
        BeastType[] out = new BeastType[values().length - 1];
        int i = 0;
        for (BeastType t : values()) {
            if (!t.isExternal()) out[i++] = t;
        }
        return out;
    }
}
