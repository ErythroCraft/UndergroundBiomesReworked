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
     * EBENE 1 & 2: Bestimmt das HAUPT-MATERIAL (Primary) basierend auf der Y-Höhe
     * und der Art des Platzhalters. Java 17 konform.
     */
    public static BlockState getPrimaryReplacement(String placeholderType, BlockPos pos) {
        int y = pos.getY();

        // LAYER 2: DEEP LAYER (y < 0)
        if (y < 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> Blocks.CALCITE.defaultBlockState();
                case TYPE_CEILING -> Blocks.BUDDING_AMETHYST.defaultBlockState();
                case TYPE_WALL -> Blocks.DEEPSLATE.defaultBlockState();
                default -> Blocks.DEEPSLATE.defaultBlockState();
            };
        }

        // LAYER 1: UPPER LAYER (y >= 0)
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
     * EBENE 1 & 2: Bestimmt das SEKUNDÄR-MATERIAL für das Rausch-Blending.
     */
    public static BlockState getSecondaryReplacement(String placeholderType, BlockPos pos) {
        int y = pos.getY();

        // Blending für den Deep Layer (y < 0)
        if (y < 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> Blocks.AMETHYST_BLOCK.defaultBlockState();
                case TYPE_CEILING -> Blocks.SMOOTH_BASALT.defaultBlockState();
                case TYPE_WALL -> Blocks.TUFF.defaultBlockState();
                default -> Blocks.TUFF.defaultBlockState();
            };
        }

        // Blending für den Upper Layer (y >= 0)
        else {
            return switch (placeholderType) {
                case TYPE_FLOOR -> Blocks.MUD.defaultBlockState();
                case TYPE_CEILING -> Blocks.POINTED_DRIPSTONE.defaultBlockState();
                case TYPE_WALL -> Blocks.ANDESITE.defaultBlockState();
                default -> Blocks.ANDESITE.defaultBlockState();
            };
        }
    }

    /**
     * Gibt den exakten Schwellenwert (Threshold) für das Blending zurück.
     */
    public static double getBlendThreshold(String placeholderType) {
        return switch (placeholderType) {
            case TYPE_FLOOR -> 0.35; // Häufiger Wechsel für organische Böden
            case TYPE_WALL -> 0.45; // Größere Gesteinsadern an den Wänden
            case TYPE_CEILING -> 0.25; // Gezielte Akzente an den Decken
            default -> 0.50;
        };
    }
}
