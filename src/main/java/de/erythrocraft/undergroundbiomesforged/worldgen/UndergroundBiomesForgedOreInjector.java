package de.erythrocraft.undergroundbiomesforged.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModBlocks;

@SuppressWarnings("null")
public class UndergroundBiomesForgedOreInjector {

    private UndergroundBiomesForgedOreInjector() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse.");
    }

    /**
     * Führt die Phase 5 (Auflösung) und Phase 6 (Erz-Vorbereitung) auf dem Chunk
     * aus.
     * Geht jeden Block durch, berechnet das Blending-Rauschen und ersetzt die
     * UBF-Platzhalter durch die finalen Blöcke der Biome.
     * 
     * @param chunk
     *            Der Chunk, der finalisiert wird
     */
    public static void resolveAndInjectChunk(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        int minY = -64;
        int maxY = 60;

        Block ubfFloor = UndergroundBiomesForgedModBlocks.UBF_FLOOR.get();
        Block ubfWall = UndergroundBiomesForgedModBlocks.UBF_WALL.get();
        Block ubfCeiling = UndergroundBiomesForgedModBlocks.UBF_CEILING.get();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;

                for (int worldY = minY; worldY <= maxY; worldY++) {
                    pos.set(worldX, worldY, worldZ);
                    BlockState currentState = chunk.getBlockState(pos);
                    Block currentBlock = currentState.getBlock();

                    // Performance-Check: Nur verarbeiten, wenn es ein UBF-Platzhalter ist
                    if (currentBlock == ubfFloor || currentBlock == ubfWall || currentBlock == ubfCeiling) {

                        // Generiere ein lokales 3D-Rauschen für das Material-Blending an dieser
                        // Position
                        double blendNoise = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX + 500,
                                worldY * 2, worldZ - 300);

                        // Schicke den Platzhalter durch die Transformations-Maschine
                        BlockState resolvedState = UndergroundBiomesForgedMaterialResolver
                                .resolvePlaceholder(currentState, pos, blendNoise);

                        // Schreibe den echten Block (z.B. Amethyst, Moos, Stone oder Deepslate) in den
                        // Chunk
                        chunk.setBlockState(pos, resolvedState, false);
                    }
                }
            }
        }
    }
}
