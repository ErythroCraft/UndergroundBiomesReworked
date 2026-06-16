package de.erythrocraft.undergroundbiomesforged.worldgen;

import de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class UbfBiomeConfig {

    public static final String TYPE_FLOOR = "floor";
    public static final String TYPE_CEILING = "ceiling";
    public static final String TYPE_WALL = "wall";

    private UbfBiomeConfig() {
        throw new UnsupportedOperationException("Utility-Klasse");
    }

    /**
     * Ermittelt das großflächige Untergrund-Biom.
     */
    public static int getUndergroundBiomeId(BlockPos pos) {
        // Zuerst lokal sichern, damit Java nicht pro Block in die Config-Datei greifen
        // muss!
        double size = de.erythrocraft.undergroundbiomesforged.config.UbfModConfig.BIOME_SIZE.get();

        int noiseX = (int) Math.floor(pos.getX() / size);
        int noiseZ = (int) Math.floor(pos.getZ() / size);

        double biomeNoise = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(noiseX, 0, noiseZ);

        if (biomeNoise < -0.3)
            return 0;
        else if (biomeNoise < 0.3)
            return 1;
        else
            return 2;
    }

    /**
     * Bestimmt das HAUPT-MATERIAL (Primary) basierend auf deiner statischen
     * Mod-Blockliste!
     */
    public static BlockState getPrimaryReplacement(String placeholderType, BlockPos pos) {
        int y = pos.getY();

        // 1. NETHER WEICHE (Bleibt für die Nether-Atmosphäre)
        if (y >= 0 && y <= 128 && placeholderType.equals(TYPE_WALL) && (pos.getX() + pos.getZ()) % 7 == 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> Blocks.NETHERRACK.defaultBlockState();
                case TYPE_CEILING -> Blocks.BASALT.defaultBlockState();
                case TYPE_WALL -> Blocks.BLACKSTONE.defaultBlockState();
                default -> Blocks.NETHERRACK.defaultBlockState();
            };
        }

        // 2. OBERWELT DYNAMIK: Wir holen uns deine echten Mod-Steine über den sicheren
        // getStoneList() Getter!
        int biomeId = getUndergroundBiomeId(pos);

        // Indizes deiner Liste: 0-7 Magmatisch, 8-15 Metamorph, 16-23 Sediment
        return switch (biomeId) {
            case 0 -> switch (placeholderType) {
                // Sediment-Biom: Hauptstein ist Kalkstein (Index 16)
                case TYPE_WALL -> UndergroundBiomesForgedModBlocks.getStoneList().get(16).get().defaultBlockState();
                case TYPE_FLOOR -> Blocks.MOSS_BLOCK.defaultBlockState();
                default -> Blocks.DRIPSTONE_BLOCK.defaultBlockState();
            };
            case 1 -> switch (placeholderType) {
                // Metamorphic-Biom: Hauptstein ist Marmor (Index 10)
                case TYPE_WALL -> UndergroundBiomesForgedModBlocks.getStoneList().get(10).get().defaultBlockState();
                default -> Blocks.STONE.defaultBlockState();
            };
            default -> switch (placeholderType) {
                // Magmatisch-Biom: Hauptstein ist dein UBF-Granit (Index 0)
                case TYPE_WALL -> UndergroundBiomesForgedModBlocks.getStoneList().get(0).get().defaultBlockState();
                default -> Blocks.STONE.defaultBlockState();
            };
        };
    }

    /**
     * Bestimmt das SEKUNDÄR-MATERIAL (Blending) mit feinen Mischungen deiner
     * UBF-Steine!
     */
    public static BlockState getSecondaryReplacement(String placeholderType, BlockPos pos) {
        int y = pos.getY();
        long posHash = pos.asLong();
        java.util.Random rand = new java.util.Random(posHash);
        double chance = rand.nextDouble();

        // 1. NETHER BLENDING (Bleibt unberührt)
        if (y >= 0 && y <= 128 && (pos.getX() * pos.getZ()) % 5 == 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> chance < 0.70 ? Blocks.SOUL_SOIL.defaultBlockState()
                        : Blocks.SOUL_SAND.defaultBlockState();
                case TYPE_CEILING -> chance < 0.80 ? Blocks.SMOOTH_BASALT.defaultBlockState()
                        : Blocks.GLOWSTONE.defaultBlockState();
                case TYPE_WALL -> {
                    if (chance < 0.60)
                        yield Blocks.GILDED_BLACKSTONE.defaultBlockState();
                    else if (chance < 0.90)
                        yield Blocks.MAGMA_BLOCK.defaultBlockState();
                    else
                        yield Blocks.CRYING_OBSIDIAN.defaultBlockState();
                }
                default -> Blocks.NETHERRACK.defaultBlockState();
            };
        }

        // 2. OBERWELT BLENDING: Mischung deiner eigenen Mod-Steine je nach Region
        int biomeId = getUndergroundBiomeId(pos);

        return switch (placeholderType) {
            case TYPE_FLOOR -> chance < 0.80 ? Blocks.MUD.defaultBlockState()
                    : Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState();
            case TYPE_CEILING -> chance < 0.70 ? Blocks.POINTED_DRIPSTONE.defaultBlockState()
                    : Blocks.DRIPSTONE_BLOCK.defaultBlockState();
            case TYPE_WALL -> {
                if (biomeId == 0) {
                    // Sediment-Biom: Mische Kreide (17), Schiefer (18) und Sandstein (20) ein
                    if (chance < 0.50)
                        yield UndergroundBiomesForgedModBlocks.getStoneList().get(17).get().defaultBlockState();
                    else if (chance < 0.85)
                        yield UndergroundBiomesForgedModBlocks.getStoneList().get(18).get().defaultBlockState();
                    else
                        yield UndergroundBiomesForgedModBlocks.getStoneList().get(20).get().defaultBlockState();
                } else if (biomeId == 1) {
                    // Metamorph-Biom: Mische Gneis (8), Quarzit (11) und Schiefer (14) ein
                    if (chance < 0.40)
                        yield UndergroundBiomesForgedModBlocks.getStoneList().get(8).get().defaultBlockState();
                    else if (chance < 0.80)
                        yield UndergroundBiomesForgedModBlocks.getStoneList().get(11).get().defaultBlockState();
                    else
                        yield UndergroundBiomesForgedModBlocks.getStoneList().get(14).get().defaultBlockState();
                } else {
                    // Magmatisch-Biom: Mische Rhyolith (1), Andesit (2) und Basalt (4) ein
                    if (chance < 0.40)
                        yield UndergroundBiomesForgedModBlocks.getStoneList().get(1).get().defaultBlockState();
                    else if (chance < 0.80)
                        yield UndergroundBiomesForgedModBlocks.getStoneList().get(2).get().defaultBlockState();
                    else
                        yield UndergroundBiomesForgedModBlocks.getStoneList().get(4).get().defaultBlockState();
                }
            }
            default -> Blocks.STONE.defaultBlockState();
        };
    }

    public static double getBlendThreshold(String placeholderType) {
        return switch (placeholderType) {
            case TYPE_FLOOR -> 1.00;
            case TYPE_WALL -> 1.00;
            case TYPE_CEILING -> 1.00;
            default -> 1.00;
        };
    }
}
