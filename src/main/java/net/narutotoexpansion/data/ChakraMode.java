package net.narutotoexpansion.data;

/**
 * Chakra modes of a host. They are independent of the form level (the "transformation").
 * <ul>
 *   <li>SUPPRESSED - default; the beast's chakra is held back and its pool slowly refills.</li>
 *   <li>SYNC - the host draws on the beast: the pool is converted into Narutoto chakra and mastery grows slowly (training).</li>
 *   <li>RAGE - needs an active form; more damage, less armour, double upkeep and double mastery gain.</li>
 * </ul>
 */
public enum ChakraMode {
    SUPPRESSED,
    SYNC,
    RAGE;

    public ChakraMode next() {
        ChakraMode[] v = values();
        return v[(ordinal() + 1) % v.length];
    }

    public String translationKey() {
        return "mode.narutoto_expansion." + name().toLowerCase();
    }

    public static ChakraMode byOrdinal(int i) {
        ChakraMode[] v = values();
        return i >= 0 && i < v.length ? v[i] : SUPPRESSED;
    }
}
