package de.erythrocraft.undergroundbiomesforged.worldgen;

import de.erythrocraft.undergroundbiomesforged.UndergroundBiomesForgedMod;
import de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.server.ServerLifecycleHooks;

@SuppressWarnings("null")
public class UndergroundBiomesForgedOreInjector {

    private UndergroundBiomesForgedOreInjector() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse.");
    }

    public static void resolveAndInjectChunk(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        boolean isDeepWorld = chunk.getMinBuildHeight() < 0;
        int minY = isDeepWorld ? -64 : 0;
        int maxY = isDeepWorld ? 60 : 127;

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

                    if (currentBlock == ubfFloor || currentBlock == ubfWall || currentBlock == ubfCeiling) {
                        double blendNoise = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(worldX + 500,
                                worldY * 2, worldZ - 300);
                        BlockState resolvedState = UndergroundBiomesForgedMaterialResolver
                                .resolvePlaceholder(chunk, currentState, pos, blendNoise);
                        chunk.setBlockState(pos, resolvedState, false);
                    }
                }
            }
        }

        // --- NEANDERTHALER HÖHLEN-GENERIERUNG AUF CHUNK-EBENE (LAG-FREI) ---
        // Ein stabiler Seed basierend auf den Chunk-Koordinaten
        long chunkSeed = ((long) chunk.getPos().x << 32) != 0 ? ((long) chunk.getPos().x << 32)
                : (long) chunk.getPos().z;
        java.util.Random rand = new java.util.Random(chunkSeed);

        // 15% Chance, dass in diesem Chunk überhaupt eine Höhle existiert (Anpassbar!)
        if (rand.nextDouble() < 0.15) {
            // Wähle eine zufällige Koordinate innerhalb dieses Chunks
            int randomX = minX + rand.nextInt(16);
            int randomZ = minZ + rand.nextInt(16);
            // Höhenbereich einschränken (z.B. Y = -30 bis 30 für Oberwelt, 20 bis 90 für
            // Nether)
            int randomY = isDeepWorld ? (-30 + rand.nextInt(60)) : (20 + rand.nextInt(70));

            final BlockPos spawnPos = new BlockPos(randomX, randomY, randomZ);
            final ResourceKey<Level> currentDimension = isDeepWorld ? Level.OVERWORLD : Level.NETHER;

            // Nur ein einziger Queue-Eintrag pro erfolgreichem Chunk!
            UndergroundBiomesForgedMod.queueServerWork(1, () -> {
                ServerLevel serverLevel = ServerLifecycleHooks.getCurrentServer().getLevel(currentDimension);
                if (serverLevel != null && serverLevel.getBlockState(spawnPos).isAir()) {

                    de.erythrocraft.undergroundbiomesforged.worldgen.NeanderthalCavePiece cave = new de.erythrocraft.undergroundbiomesforged.worldgen.NeanderthalCavePiece(
                            spawnPos);

                    net.minecraft.world.level.levelgen.structure.BoundingBox totalBox = new net.minecraft.world.level.levelgen.structure.BoundingBox(
                            spawnPos.getX() - 5, spawnPos.getY() - 5, spawnPos.getZ() - 5,
                            spawnPos.getX() + 5, spawnPos.getY() + 5, spawnPos.getZ() + 5);

                    cave.postProcess(
                            serverLevel,
                            serverLevel.structureManager(),
                            serverLevel.getChunkSource().getGenerator(),
                            serverLevel.getRandom(),
                            totalBox,
                            new net.minecraft.world.level.ChunkPos(spawnPos),
                            spawnPos);
                }
            });
        }
    }
}
