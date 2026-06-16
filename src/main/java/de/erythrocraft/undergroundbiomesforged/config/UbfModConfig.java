package de.erythrocraft.undergroundbiomesforged.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class UbfModConfig {
        private UbfModConfig() {
                /* This utility class should not be instantiated */
        }

        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.DoubleValue NEANDERTHAL_CAVE_CHANCE;
        public static final ForgeConfigSpec.IntValue NEANDERTHAL_CAVE_RADIUS;
        public static final ForgeConfigSpec.DoubleValue BIOME_SIZE;
        public static final ForgeConfigSpec.DoubleValue ORE_CHANCE_UPPER;
        public static final ForgeConfigSpec.DoubleValue ORE_CHANCE_DEEP;
        public static final ForgeConfigSpec.DoubleValue ORE_CHANCE_NETHER;

        static {
                BUILDER.comment("Underground Biomes Forged Configuration").push("general");

                BUILDER.push("neanderthal_caves");
                NEANDERTHAL_CAVE_CHANCE = BUILDER
                                .comment("Chance per chunk to generate a cave (0.15 = 15%)")
                                .defineInRange("spawnChance", 0.15, 0.0, 1.0);
                NEANDERTHAL_CAVE_RADIUS = BUILDER
                                .comment("Radius of the cave sphere (Default: 5)")
                                .defineInRange("caveRadius", 5, 2, 10);
                BUILDER.pop();

                BUILDER.push("underground_biomes");
                BIOME_SIZE = BUILDER
                                .comment("Size of underground biomes (Default: 300.0)")
                                .defineInRange("biomeSize", 300.0, 50.0, 2000.0);
                BUILDER.pop();

                BUILDER.push("ore_generation");
                ORE_CHANCE_UPPER = BUILDER
                                .comment("Multiplier for upper ores. 1.0 is default.")
                                .defineInRange("upperOreMultiplier", 1.0, 0.0, 10.0);
                ORE_CHANCE_DEEP = BUILDER
                                .comment("Multiplier for deepslate ores. 1.0 is default.")
                                .defineInRange("deepOreMultiplier", 1.0, 0.0, 10.0);
                ORE_CHANCE_NETHER = BUILDER
                                .comment("Multiplier for nether ores. 1.0 is default.")
                                .defineInRange("netherOreMultiplier", 1.0, 0.0, 10.0);
                BUILDER.pop();

                // KORREKTUR: Nur noch EIN pop(), um die "general"-Kategorie sauber zu
                // schließen!
                BUILDER.pop();
                SPEC = BUILDER.build();
        }
}
