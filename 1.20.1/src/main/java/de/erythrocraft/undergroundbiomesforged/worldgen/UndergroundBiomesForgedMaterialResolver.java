package de.erythrocraft.undergroundbiomesforged.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModBlocks;

public class UndergroundBiomesForgedMaterialResolver {

    private UndergroundBiomesForgedMaterialResolver() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse und darf nicht instanziiert werden.");
    }

    public static BlockState resolvePlaceholder(ChunkAccess chunk, BlockState currentState, BlockPos pos,
            double blendNoise) {
        Block currentBlock = currentState.getBlock();

        Block ubfFloor = UndergroundBiomesForgedModBlocks.UBF_FLOOR.get();
        Block ubfWall = UndergroundBiomesForgedModBlocks.UBF_WALL.get();
        Block ubfCeiling = UndergroundBiomesForgedModBlocks.UBF_CEILING.get();

        if (currentBlock != ubfFloor && currentBlock != ubfWall && currentBlock != ubfCeiling) {
            return currentState;
        }

        String type = UbfBiomeConfig.TYPE_WALL;
        if (currentBlock == ubfFloor) {
            type = UbfBiomeConfig.TYPE_FLOOR;
        } else if (currentBlock == ubfCeiling) {
            type = UbfBiomeConfig.TYPE_CEILING;
        }

        BlockState primary = UbfBiomeConfig.getPrimaryReplacement(type, pos);
        BlockState secondary = UbfBiomeConfig.getSecondaryReplacement(type, pos);
        double threshold = UbfBiomeConfig.getBlendThreshold(type);

        return (blendNoise > threshold) ? primary : secondary;
    }
}
