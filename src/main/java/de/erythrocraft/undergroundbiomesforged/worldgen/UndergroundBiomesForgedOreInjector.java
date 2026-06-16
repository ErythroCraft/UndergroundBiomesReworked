package de.erythrocraft.undergroundbiomesforged.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModBlocks;
import de.erythrocraft.undergroundbiomesforged.UndergroundBiomesForgedMod;

@SuppressWarnings("null")
public class UndergroundBiomesForgedOreInjector {

    private UndergroundBiomesForgedOreInjector() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse.");
    }

    /**
     * Führt die Ersetzung der Platzhalter-Blöcke durch echte Gesteine und Erze aus.
     * Scanniert jetzt die VOLLE Bauhöhe des Chunks, um unzerstörbare Reste zu
     * verhindern.
     */
    public static void resolveAndInjectChunk(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        // --- KORREKTUR: SCANNEN DER VOLLE BAUHÖHE ---
        // Anstatt starr bei Y=60 aufzuhören, fragen wir den Chunk nach seiner echten
        // Deckenhöhe!
        // In der Oberwelt scannt er nun von -64 bis weit über 60+ (je nach Berg-Biom).
        boolean isDeepWorld = chunk.getMinBuildHeight() < 0;
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        Block ubfFloor = UndergroundBiomesForgedModBlocks.UBF_FLOOR.get();
        Block ubfWall = UndergroundBiomesForgedModBlocks.UBF_WALL.get();
        Block ubfCeiling = UndergroundBiomesForgedModBlocks.UBF_CEILING.get();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minX + x;
                int worldZ = minZ + z;

                for (int worldY = minY; worldY < maxY; worldY++) { // Wichtig: < maxY, da max außerhalb liegt
                    pos.set(worldX, worldY, worldZ);
                    BlockState currentState = chunk.getBlockState(pos);
                    Block currentBlock = currentState.getBlock();

                    if (currentBlock == ubfFloor || currentBlock == ubfWall || currentBlock == ubfCeiling) {

                        long posHash = pos.asLong();
                        java.util.Random blockRand = new java.util.Random(posHash);
                        double oreChance = blockRand.nextDouble();

                        BlockState finalState;

                        // Erz-Injektion
                        if (isDeepWorld) {
                            if (worldY < 0) {
                                finalState = getDeepOreState(oreChance);
                            } else {
                                finalState = getUpperOreState(oreChance);
                            }
                        } else {
                            finalState = getNetherOreState(oreChance);
                        }

                        // Gesteins-Blending ausführen, wenn kein Erz gewürfelt wurde
                        if (finalState == null) {
                            double blendNoise = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(
                                    (int) (worldX * 2.5),
                                    (int) (worldY * 3.0),
                                    (int) (worldZ * 2.5));
                            finalState = UndergroundBiomesForgedMaterialResolver
                                    .resolvePlaceholder(chunk, currentState, pos, blendNoise);
                        }

                        // Der eigentliche Austausch der Blöcke
                        chunk.setBlockState(pos, finalState, false);
                    }
                }
            }
        }

        // --- NEANDERTHALER HÖHLEN-GENERIERUNG ---
        long chunkSeed = ((long) chunk.getPos().x << 32) != 0 ? ((long) chunk.getPos().x << 32)
                : (long) chunk.getPos().z;
        java.util.Random rand = new java.util.Random(chunkSeed);

        if (rand.nextDouble() < de.erythrocraft.undergroundbiomesforged.config.UbfModConfig.NEANDERTHAL_CAVE_CHANCE
                .get()) {
            int randomX = minX + rand.nextInt(16);
            int randomZ = minZ + rand.nextInt(16);
            // Sichere Höhengrenzen für das Platzieren der Neandertaler-Höhle
            int randomY = isDeepWorld ? (-30 + rand.nextInt(60)) : (20 + rand.nextInt(70));

            final BlockPos spawnPos = new BlockPos(randomX, randomY, randomZ);
            final ResourceKey<Level> currentDimension = isDeepWorld ? Level.OVERWORLD : Level.NETHER;

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

    private static BlockState getUpperOreState(double chance) {
        if (chance < 0.020)
            return Blocks.COAL_ORE.defaultBlockState();
        if (chance < 0.035)
            return Blocks.IRON_ORE.defaultBlockState();
        if (chance < 0.042)
            return Blocks.COPPER_ORE.defaultBlockState();
        if (chance < 0.045)
            return Blocks.GOLD_ORE.defaultBlockState();
        return null;
    }

    private static BlockState getDeepOreState(double chance) {
        if (chance < 0.015)
            return Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState();
        if (chance < 0.028)
            return Blocks.DEEPSLATE_IRON_ORE.defaultBlockState();
        if (chance < 0.038)
            return Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState();
        if (chance < 0.045)
            return Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState();
        if (chance < 0.049)
            return Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState();
        return null;
    }

    private static BlockState getNetherOreState(double chance) {
        if (chance < 0.030)
            return Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
        if (chance < 0.050)
            return Blocks.NETHER_GOLD_ORE.defaultBlockState();
        if (chance < 0.053)
            return Blocks.ANCIENT_DEBRIS.defaultBlockState();
        return null;
    }
}
