package de.erythrocraft.ubreworked.worldgen;

import de.erythrocraft.ubreworked.init.UbModBlocks;
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
     * Ermittelt das großflächige Untergrund-Biom stabil und threadsicher.
     */
    public static int getUndergroundBiomeId(BlockPos pos) {
        double size = de.erythrocraft.ubreworked.config.UbModConfig.BIOME_SIZE.get();

        // Verhindert Division durch Null, falls die Config fehlerhaft geladen wurde
        if (size <= 0)
            size = 512.0;

        // WICHTIG: Wir übergeben dem Noise-Generator echte double-Koordinaten.
        // Das Integrieren per Math.floor() direkt in die Abfrage hinein verhindert,
        // dass der Noise-Sampler bei Werten nahe 0 hängenbleibt oder springt.
        double noiseX = pos.getX() / size;
        double noiseZ = pos.getZ() / size;

        // Nutzt Y=0 als feste Schicht für die Biom-Verteilung
        double biomeNoise = UbNoiseGenerator.sampleTunnelDensity((int) Math.floor(noiseX * 100), 0,
                (int) Math.floor(noiseZ * 100));

        if (biomeNoise < -0.3)
            return 0; // Sediment
        else if (biomeNoise < 0.3)
            return 1; // Metamorph
        else
            return 2; // Magmatisch
    }

    /**
     * Bestimmt das HAUPT-MATERIAL (Primary) basierend auf deiner statischen
     * Mod-Blockliste!
     */
    public static BlockState getPrimaryReplacement(String placeholderType, BlockPos pos) {
        int y = pos.getY();

        // 1. NETHER WEICHE (Threadsicher ohne Modulo-Grenzfehler bei negativen
        // Koordinaten)
        // Verwende Math.abs, da negative Weltkoordinaten sonst ein negatives Ergebnis
        // bei % liefern!
        if (y >= 0 && y <= 128 && placeholderType.equals(TYPE_WALL) && Math.abs(pos.getX() + pos.getZ()) % 7 == 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> Blocks.NETHERRACK.defaultBlockState();
                case TYPE_CEILING -> Blocks.BASALT.defaultBlockState();
                case TYPE_WALL -> Blocks.BLACKSTONE.defaultBlockState();
                default -> Blocks.NETHERRACK.defaultBlockState();
            };
        }

        int biomeId = getUndergroundBiomeId(pos);

        // Sicherheitsprüfung, um IndexOutOfBoundsException in der getStoneList() zu
        // blockieren
        if (UbModBlocks.getStoneList().size() < 21) {
            return Blocks.STONE.defaultBlockState();
        }

        // Indizes deiner Liste: 0-7 Magmatisch, 8-15 Metamorph, 16-23 Sediment
        return switch (biomeId) {
            case 0 -> switch (placeholderType) {
                case TYPE_WALL -> UbModBlocks.getStoneList().get(16).get().defaultBlockState();
                case TYPE_FLOOR -> Blocks.MOSS_BLOCK.defaultBlockState();
                default -> Blocks.DRIPSTONE_BLOCK.defaultBlockState();
            };
            case 1 -> switch (placeholderType) {
                case TYPE_WALL -> UbModBlocks.getStoneList().get(10).get().defaultBlockState();
                default -> Blocks.STONE.defaultBlockState();
            };
            default -> switch (placeholderType) {
                case TYPE_WALL -> UbModBlocks.getStoneList().get(0).get().defaultBlockState();
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

        // Schneller, nicht blockierender LCG (Linear Congruential Generator) Hash für
        // die Position.
        // java.util.Random() bei jedem Block zu instanziieren ist ein massiver
        // Performance-Fresser!
        long seed = pos.asLong();
        seed = seed * 6364136223846793005L + 1442695040888963407L;
        double chance = (double) (seed & 0xFFFFFFFFL) / 4294967296.0;
        if (chance < 0)
            chance = -chance;

        // 1. NETHER BLENDING
        if (y >= 0 && y <= 128 && Math.abs(pos.getX() * pos.getZ()) % 5 == 0) {
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

        int biomeId = getUndergroundBiomeId(pos);

        // Sicherheitsprüfung für die Mod-Blockliste
        if (UbModBlocks.getStoneList().size() < 21) {
            return Blocks.STONE.defaultBlockState();
        }

        return switch (placeholderType) {
            case TYPE_FLOOR -> chance < 0.80 ? Blocks.MUD.defaultBlockState()
                    : Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState();
            case TYPE_CEILING -> chance < 0.70 ? Blocks.POINTED_DRIPSTONE.defaultBlockState()
                    : Blocks.DRIPSTONE_BLOCK.defaultBlockState();
            case TYPE_WALL -> {
                if (biomeId == 0) {
                    if (chance < 0.50)
                        yield UbModBlocks.getStoneList().get(17).get().defaultBlockState();
                    else if (chance < 0.85)
                        yield UbModBlocks.getStoneList().get(18).get().defaultBlockState();
                    else
                        yield UbModBlocks.getStoneList().get(20).get().defaultBlockState();
                } else if (biomeId == 1) {
                    if (chance < 0.40)
                        yield UbModBlocks.getStoneList().get(8).get().defaultBlockState();
                    else if (chance < 0.80)
                        yield UbModBlocks.getStoneList().get(11).get().defaultBlockState();
                    else
                        yield UbModBlocks.getStoneList().get(14).get().defaultBlockState();
                } else {
                    if (chance < 0.40)
                        yield UbModBlocks.getStoneList().get(1).get().defaultBlockState();
                    else if (chance < 0.80)
                        yield UbModBlocks.getStoneList().get(2).get().defaultBlockState();
                    else
                        yield UbModBlocks.getStoneList().get(4).get().defaultBlockState();
                }
            }
            default -> Blocks.STONE.defaultBlockState();
        };
    }

    public static double getBlendThreshold(String placeholderType) {
        return 1.00;
    }
}
