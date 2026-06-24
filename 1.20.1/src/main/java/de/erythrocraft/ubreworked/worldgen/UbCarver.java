package de.erythrocraft.ubreworked.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

@SuppressWarnings("null")
public class UbCarver {

    private static final double CARVE_THRESHOLD = 0.72;

    private UbCarver() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse.");
    }

    public static void carveTunnelChunk(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        boolean isDeepWorld = chunk.getMinBuildHeight() < 0;

        int minY = isDeepWorld ? -64 : 0;
        int maxY = isDeepWorld ? 60 : 127;

        // 1. SCHRITT: Höhlengänge komplett in CAVE_AIR verwandeln (Nur innerhalb dieses
        // Chunks!)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;

                for (int worldY = minY; worldY <= maxY; worldY++) {
                    double centerDensity = UbNoiseGenerator.sampleTunnelDensity(worldX, worldY,
                            worldZ);

                    if (centerDensity > CARVE_THRESHOLD) {
                        pos.set(worldX, worldY, worldZ);
                        chunk.setBlockState(pos, Blocks.CAVE_AIR.defaultBlockState(), false);
                    }
                }
            }
        }

        // 2. SCHRITT: Die Wände/Oberflächen dekorieren (Sicherer, lokaler Check!)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;

                for (int worldY = minY; worldY <= maxY; worldY++) {
                    pos.set(worldX, worldY, worldZ);

                    // Wenn der aktuelle Block KEINE Luft ist, schauen wir, ob er an ein
                    // Höhlen-Aushub grenzt
                    if (!chunk.getBlockState(pos).isAir()) {
                        double centerDensity = UbNoiseGenerator.sampleTunnelDensity(worldX, worldY,
                                worldZ);

                        // Liegt im Schwellenwert-Bereich für die Wand-Dekoration
                        if (centerDensity > (CARVE_THRESHOLD - 0.05)) {
                            handleLocalSurfacePlacement(chunk, pos, worldX, worldY, worldZ, minX, minZ, minY, maxY);
                        }
                    }
                }
            }
        }
    }

    /**
     * Ermittelt die Oberflächen-Blöcke threadsicher, ohne Nachbar-Chunks durch
     * Noise-Abfragen aufzuwecken.
     */
    private static void handleLocalSurfacePlacement(ChunkAccess chunk, BlockPos.MutableBlockPos pos,
            int worldX, int worldY, int worldZ,
            int minX, int minZ, int minY, int maxY) {

        // Wir holen uns die Dichten. Wenn wir am Chunk-Rand sind, spiegeln wir den
        // eigenen Wert,
        // um den Generator nicht in fremde Chunks springen zu lassen.
        double currentDensity = UbNoiseGenerator.sampleTunnelDensity(worldX, worldY, worldZ);

        double densityNorth = (worldZ - 1 >= minZ)
                ? UbNoiseGenerator.sampleTunnelDensity(worldX, worldY, worldZ - 1)
                : currentDensity;
        double densitySouth = (worldZ + 1 < minZ + 16)
                ? UbNoiseGenerator.sampleTunnelDensity(worldX, worldY, worldZ + 1)
                : currentDensity;
        double densityWest = (worldX - 1 >= minX)
                ? UbNoiseGenerator.sampleTunnelDensity(worldX - 1, worldY, worldZ)
                : currentDensity;
        double densityEast = (worldX + 1 < minX + 16)
                ? UbNoiseGenerator.sampleTunnelDensity(worldX + 1, worldY, worldZ)
                : currentDensity;

        // Vertikale Achsen sind innerhalb des Chunks immer sicher
        double densityUp = (worldY + 1 <= maxY)
                ? UbNoiseGenerator.sampleTunnelDensity(worldX, worldY + 1, worldZ)
                : currentDensity;
        double densityDown = (worldY - 1 >= minY)
                ? UbNoiseGenerator.sampleTunnelDensity(worldX, worldY - 1, worldZ)
                : currentDensity;

        BlockState surfaceBlock = UbSurfaceClassifier.calculateSurfaceBlock(
                densityNorth, densitySouth,
                densityUp, densityDown,
                densityWest, densityEast);

        chunk.setBlockState(pos, surfaceBlock, false);
    }
}
