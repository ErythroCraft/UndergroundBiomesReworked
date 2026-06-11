package de.erythrocraft.undergroundbiomesforged.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("null")
public class UndergroundBiomesForgedCarver {

    private static final double CARVE_THRESHOLD = 0.72;

    private UndergroundBiomesForgedCarver() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse.");
    }

    /**
     * Schnitzt die endlos verbundenen Tunnel in den übergebenen Chunk.
     * Platziert die material-agnostischen Platzhalter-Blöcke an den Tunnelwänden,
     * -decken und -böden.
     * 
     * @param chunk
     *            Der Minecraft-Chunk, der gerade generiert wird
     */
    public static void carveTunnelChunk(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        // Grenzen des Dual-Layer-Systems (Von y=60 bis y=-64)
        int minY = -64;
        int maxY = 60;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;

                for (int worldY = minY; worldY <= maxY; worldY++) {
                    pos.set(worldX, worldY, worldZ);

                    double centerDensity = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX, worldY,
                            worldZ);

                    // Wenn die Dichte über dem Schwellenwert liegt, schneiden wir Luft aus
                    // (Höhlen-Inneres)
                    if (centerDensity > CARVE_THRESHOLD) {
                        chunk.setBlockState(pos, Blocks.CAVE_AIR.defaultBlockState(), false);
                    }
                    // Wenn wir uns knapp außerhalb des Tunnels befinden, prüfen wir auf
                    // Oberflächen-Haut
                    else if (centerDensity > (CARVE_THRESHOLD - 0.05)) {
                        handleSurfacePlacement(chunk, pos, worldX, worldY, worldZ);
                    }
                }
            }
        }
    }

    /**
     * Hilfsmethode zur Bestimmung und Platzierung des Oberflächen-Platzhalters.
     */
    private static void handleSurfacePlacement(ChunkAccess chunk, BlockPos.MutableBlockPos pos, int worldX, int worldY,
            int worldZ) {
        // Berechne die Rausch-Dichten der sechs Nachbarblöcke für den Gradienten
        double densityNorth = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX, worldY, worldZ - 1);
        double densitySouth = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX, worldY, worldZ + 1);
        double densityUp = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX, worldY + 1, worldZ);
        double densityDown = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX, worldY - 1, worldZ);
        double densityWest = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX - 1, worldY, worldZ);
        double densityEast = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX + 1, worldY, worldZ);

        // Klassifiziere die Wandneigung
        BlockState surfaceBlock = UndergroundBiomesForgedSurfaceClassifier.calculateSurfaceBlock(
                densityNorth, densitySouth,
                densityUp, densityDown,
                densityWest, densityEast);

        // Platziere den material-agnostischen UBF-Block im Chunk
        chunk.setBlockState(pos, surfaceBlock, false);
    }
}
