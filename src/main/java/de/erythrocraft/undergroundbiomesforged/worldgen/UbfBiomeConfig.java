package de.erythrocraft.undergroundbiomesforged.worldgen;

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
     * Bestimmt das HAUPT-MATERIAL (Primary) basierend auf Dimension und Höhe.
     */
    public static BlockState getPrimaryReplacement(String placeholderType, BlockPos pos) {
        int y = pos.getY();

        // 1. WEICHE FÜR DEN NETHER
        if (y >= 0 && y <= 128 && placeholderType.equals(TYPE_WALL) && (pos.getX() + pos.getZ()) % 7 == 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> Blocks.NETHERRACK.defaultBlockState();
                case TYPE_CEILING -> Blocks.BASALT.defaultBlockState();
                case TYPE_WALL -> Blocks.BLACKSTONE.defaultBlockState();
                default -> Blocks.NETHERRACK.defaultBlockState();
            };
        }

        // 2. OBERWELT: LAYER 2 - DEEP LAYER (y < 0)
        if (y < 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> Blocks.CALCITE.defaultBlockState();
                case TYPE_CEILING -> Blocks.BUDDING_AMETHYST.defaultBlockState();
                case TYPE_WALL -> Blocks.DEEPSLATE.defaultBlockState();
                default -> Blocks.DEEPSLATE.defaultBlockState();
            };
        }

        // 3. OBERWELT: LAYER 1 - UPPER LAYER (y >= 0)
        else {
            return switch (placeholderType) {
                case TYPE_FLOOR -> Blocks.MOSS_BLOCK.defaultBlockState();
                case TYPE_CEILING -> Blocks.DRIPSTONE_BLOCK.defaultBlockState();
                case TYPE_WALL -> Blocks.STONE.defaultBlockState();
                default -> Blocks.STONE.defaultBlockState();
            };
        }
    }

    /**
     * Bestimmt das SEKUNDÄR-MATERIAL mit organischen Block-Mischungen.
     */
    public static BlockState getSecondaryReplacement(String placeholderType, BlockPos pos) {
        int y = pos.getY();

        long posHash = pos.asLong();
        java.util.Random rand = new java.util.Random(posHash);
        double chance = rand.nextDouble();

        // 1. NETHER BLENDING
        if (y >= 0 && y <= 128 && (pos.getX() * pos.getZ()) % 5 == 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> chance < 0.70 ? Blocks.SOUL_SOIL.defaultBlockState()
                        : Blocks.SOUL_SAND.defaultBlockState();
                case TYPE_CEILING -> chance < 0.80 ? Blocks.SMOOTH_BASALT.defaultBlockState()
                        : Blocks.GLOWSTONE.defaultBlockState();
                case TYPE_WALL -> {
                    // KORREKTUR: "yield" statt "return" innerhalb der Switch-Expression!
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

        // 2. OBERWELT: DEEP LAYER (y < 0)
        if (y < 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> chance < 0.70 ? Blocks.AMETHYST_BLOCK.defaultBlockState()
                        : Blocks.BUDDING_AMETHYST.defaultBlockState();
                case TYPE_CEILING -> chance < 0.80 ? Blocks.SMOOTH_BASALT.defaultBlockState()
                        : Blocks.BASALT.defaultBlockState();
                case TYPE_WALL -> {
                    // KORREKTUR: "yield" statt "return" innerhalb der Switch-Expression!
                    if (chance < 0.60)
                        yield Blocks.TUFF.defaultBlockState();
                    else if (chance < 0.90)
                        yield Blocks.DEEPSLATE.defaultBlockState();
                    else
                        yield Blocks.CALCITE.defaultBlockState();
                }
                default -> Blocks.TUFF.defaultBlockState();
            };
        }

        // 3. OBERWELT: UPPER LAYER (y >= 0)
        else {
            return switch (placeholderType) {
                case TYPE_FLOOR -> chance < 0.80 ? Blocks.MUD.defaultBlockState()
                        : Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState();
                case TYPE_CEILING -> chance < 0.70 ? Blocks.POINTED_DRIPSTONE.defaultBlockState()
                        : Blocks.DRIPSTONE_BLOCK.defaultBlockState();
                case TYPE_WALL -> {
                    // KORREKTUR: "yield" statt "return" innerhalb der Switch-Expression!
                    if (chance < 0.65)
                        yield Blocks.ANDESITE.defaultBlockState();
                    else if (chance < 0.85)
                        yield Blocks.STONE.defaultBlockState();
                    else if (chance < 0.95)
                        yield Blocks.DIORITE.defaultBlockState();
                    else
                        yield Blocks.GRAVEL.defaultBlockState();
                }
                default -> Blocks.ANDESITE.defaultBlockState();
            };
        }
    }

    /**
     * Gibt den exakten Schwellenwert (Threshold) für das Blending zurück.
     */
    public static double getBlendThreshold(String placeholderType) {
        return switch (placeholderType) {
            case TYPE_FLOOR -> 0.35;
            case TYPE_WALL -> 0.45;
            case TYPE_CEILING -> 0.25;
            default -> 0.50;
        };
    }
}
