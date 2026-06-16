package de.erythrocraft.undergroundbiomesforged.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class UbfModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // --- SEKTION: HÖHLEN (NEANDERTHALER) ---
    public static final ForgeConfigSpec.DoubleValue NEANDERTHAL_CAVE_CHANCE;
    public static final ForgeConfigSpec.IntValue NEANDERTHAL_CAVE_RADIUS;

    // --- SEKTION: BIOME ---
    public static final ForgeConfigSpec.DoubleValue BIOME_SIZE;

    // --- SEKTION: ERZE (CHANCEN) ---
    public static final ForgeConfigSpec.DoubleValue ORE_CHANCE_UPPER;
    public static final ForgeConfigSpec.DoubleValue ORE_CHANCE_DEEP;
    public static final ForgeConfigSpec.DoubleValue ORE_CHANCE_NETHER;

    static {
        BUILDER.comment("Underground Biomes Forged - Konfigurationsdatei").push("general");

        BUILDER.comment("Einstellungen fur die prähistorischen Neandertaler-Wohnhöhlen").push("neanderthal_caves");
        NEANDERTHAL_CAVE_CHANCE = BUILDER
                .comment("Chance pro Chunk, dass eine Neandertaler-Hohle generiert wird (0.15 = 15%)")
                .defineInRange("spawnChance", 0.15, 0.0, 1.0);
        NEANDERTHAL_CAVE_RADIUS = BUILDER
                .comment("Der Radius der ausgehohlten Kugel (Standard: 5)")
                .defineInRange("caveRadius", 5, 2, 10);
        BUILDER.pop();

        BUILDER.comment("Einstellungen fur die Gesteins-Biome").push("underground_biomes");
        BIOME_SIZE = BUILDER
                .comment(
                        "Große der Untergrund-Gesteinsbiome. Hohere Werte bedeuten riesige, langere Gesteinszonen (Standard: 300.0)")
                .defineInRange("biomeSize", 300.0, 50.0, 2000.0);
        BUILDER.pop();

        BUILDER.comment("Erzgenerierung (Zusatz-Injektionen in den Tunnelwanden)").push("ore_generation");
        ORE_CHANCE_UPPER = BUILDER
                .comment("Multiplikator fur Erze in der oberen Oberwelt (y >= 0). 1.0 ist Standard, hoher = mehr Erze.")
                .defineInRange("upperOreMultiplier", 1.0, 0.0, 10.0);
        ORE_CHANCE_DEEP = BUILDER
                .comment("Multiplikator fur Tiefenschiefer-Erze (y < 0). 1.0 ist Standard.")
                .defineInRange("deepOreMultiplier", 1.0, 0.0, 10.0);
        ORE_CHANCE_NETHER = BUILDER
                .comment("Multiplikator fur Nether-Erze. 1.0 ist Standard.")
                .defineInRange("netherOreMultiplier", 1.0, 0.0, 10.0);
        BUILDER.pop();

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
