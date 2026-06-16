package de.erythrocraft.undergroundbiomesforged.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

@SuppressWarnings("null")
public class UndergroundBiomesForgedCarver {

    private static final double CARVE_THRESHOLD = 0.72;

    private UndergroundBiomesForgedCarver() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse.");
    }

    public static void carveTunnelChunk(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        boolean isDeepWorld = chunk.getMinBuildHeight() < 0;

        int minY = isDeepWorld ? -64 : 0;
        int maxY = isDeepWorld ? 60 : 127;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;

                for (int worldY = minY; worldY <= maxY; worldY++) {
                    pos.set(worldX, worldY, worldZ);

                    double centerDensity = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX, worldY,
                            worldZ);

                    if (centerDensity > CARVE_THRESHOLD) {
                        chunk.setBlockState(pos, Blocks.CAVE_AIR.defaultBlockState(), false);
                    } else if (centerDensity > (CARVE_THRESHOLD - 0.05)) {
                        handleSurfacePlacement(chunk, pos, worldX, worldY, worldZ);
                    }
                }
            }
        }
    }

    private static void handleSurfacePlacement(ChunkAccess chunk, BlockPos.MutableBlockPos pos, int worldX, int worldY,
            int worldZ) {
        double densityNorth = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX, worldY, worldZ - 1);
        double densitySouth = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX, worldY, worldZ + 1);
        double densityUp = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX, worldY + 1, worldZ);
        double densityDown = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX, worldY - 1, worldZ);
        double densityWest = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX - 1, worldY, worldZ);
        double densityEast = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX + 1, worldY, worldZ);

        BlockState surfaceBlock = UndergroundBiomesForgedSurfaceClassifier.calculateSurfaceBlock(
                densityNorth, densitySouth,
                densityUp, densityDown,
                densityWest, densityEast);

        chunk.setBlockState(pos, surfaceBlock, false);
    }
}
