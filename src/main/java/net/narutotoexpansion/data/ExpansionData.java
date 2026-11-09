package net.narutotoexpansion.data;

import net.minecraft.nbt.CompoundTag;

/**
 * Per-player state of the add-on (kept apart from Narutoto's own PlayerVariables on purpose:
 * we never inject fields into Narutoto and never write beasts 1-8 into its 'jinchuriki' variable,
 * which Narutoto only uses for Kurama == 9).
 */
public class ExpansionData {
    /** 0 = none, otherwise BeastType id (never 9). */
    public int hostBeast;
    /** Mastery per beast id (index 0 unused). Survives death so progression is kept. */
    public final int[] mastery = new int[10];
    /** Active form level, 0 = not transformed. */
    public int form;
    /** Preferred form level (0 = highest unlocked). */
    public int selectedForm;
    public ChakraMode mode = ChakraMode.SUPPRESSED;
    /** Beast chakra pool. */
    public double pool;
    public boolean charging;
    public int chargeTicks;
    public int bombCooldown;

    public boolean isHost() {
        return hostBeast != 0;
    }

    public int masteryOfHost() {
        return hostBeast >= 1 && hostBeast <= 9 ? mastery[hostBeast] : 0;
    }

    /** Persistent part. */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("host", hostBeast);
        tag.putIntArray("mastery", mastery);
        tag.putInt("selectedForm", selectedForm);
        tag.putInt("mode", mode.ordinal());
        tag.putDouble("pool", pool);
        return tag;
    }

    public void load(CompoundTag tag) {
        hostBeast = tag.getInt("host");
        int[] m = tag.getIntArray("mastery");
        for (int i = 0; i < mastery.length; i++) {
            mastery[i] = i < m.length ? m[i] : 0;
        }
        selectedForm = tag.getInt("selectedForm");
        mode = ChakraMode.byOrdinal(tag.getInt("mode"));
        pool = tag.getDouble("pool");
        // transient state is never persisted
        form = 0;
        charging = false;
        chargeTicks = 0;
        bombCooldown = 0;
    }

    /** Full state for client synchronisation (HUD). */
    public CompoundTag saveForSync() {
        CompoundTag tag = save();
        tag.putInt("form", form);
        tag.putBoolean("charging", charging);
        tag.putInt("chargeTicks", chargeTicks);
        tag.putInt("bombCooldown", bombCooldown);
        return tag;
    }

    public void loadFromSync(CompoundTag tag) {
        load(tag);
        form = tag.getInt("form");
        charging = tag.getBoolean("charging");
        chargeTicks = tag.getInt("chargeTicks");
        bombCooldown = tag.getInt("bombCooldown");
    }

    /** Used by PlayerEvent.Clone. */
    public void copyFrom(ExpansionData other, boolean keepHost) {
        System.arraycopy(other.mastery, 0, mastery, 0, mastery.length);
        selectedForm = other.selectedForm;
        if (keepHost) {
            hostBeast = other.hostBeast;
            mode = other.mode;
            pool = other.pool;
        }
    }
}
