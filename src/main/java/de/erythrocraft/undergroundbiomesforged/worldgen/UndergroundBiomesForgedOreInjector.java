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

    public static void resolveAndInjectChunk(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        boolean isDeepWorld = chunk.getMinBuildHeight() < 3;
        int minY = isDeepWorld ? -62 : 0;
        int maxY = isDeepWorld ? 1 : 62;

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

                        // 1. STABILER ZUFALLSWERT FÜR JEDEN BLOCK
                        long posHash = pos.asLong();
                        java.util.Random blockRand = new java.util.Random(posHash);
                        double oreChance = blockRand.nextDouble();

                        BlockState finalState;

                        // 2. ERZ-INJEKTION (Getrennt nach Oberwelt, Tiefen & Nether)
                        if (isDeepWorld) {
                            if (worldY < 0) {
                                // --- DEEP LAYER ERZE (Y < 0) ---
                                finalState = getDeepOreState(oreChance);
                            } else {
                                // --- UPPER LAYER ERZE (Y >= 0) ---
                                finalState = getUpperOreState(oreChance);
                            }
                        } else {
                            // --- NETHER ERZE ---
                            finalState = getNetherOreState(oreChance);
                        }

                        // Wenn kein Erz gewürfelt wurde (finalState ist null), nutzen wir das normale
                        // Gesteins-Blending
                        if (finalState == null) {
                            double blendNoise = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(
                                    (int) (worldX * 2.5),
                                    (int) (worldY * 3.0),
                                    (int) (worldZ * 2.5));
                            finalState = UndergroundBiomesForgedMaterialResolver
                                    .resolvePlaceholder(chunk, currentState, pos, blendNoise);
                        }

                        chunk.setBlockState(pos, finalState, false);
                    }
                }
            }
        }

        // --- NEANDERTHALER HÖHLEN-GENERIERUNG AUF CHUNK-EBENE (BLEIBT UNVERÄNDERT) ---
        long chunkSeed = ((long) chunk.getPos().x << 32) != 0 ? ((long) chunk.getPos().x << 32)
                : (long) chunk.getPos().z;
        java.util.Random rand = new java.util.Random(chunkSeed);

        if (rand.nextDouble() < 0.15) {
            int randomX = minX + rand.nextInt(16);
            int randomZ = minZ + rand.nextInt(16);
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

    /**
     * Bestimmt, ob ein Erz in der oberen Schicht der Oberwelt spawnt.
     * Erhöhte Chancen (z.B. 2% Kohle, 1.5% Eisen etc.)
     */
    private static BlockState getUpperOreState(double chance) {
        if (chance < 0.020)
            return Blocks.COAL_ORE.defaultBlockState();
        if (chance < 0.035)
            return Blocks.IRON_ORE.defaultBlockState();
        if (chance < 0.042)
            return Blocks.COPPER_ORE.defaultBlockState();
        if (chance < 0.045)
            return Blocks.GOLD_ORE.defaultBlockState();
        return null; // Kein Erz -> Generiere normalen UBF-Stein
    }

    /**
     * Bestimmt, ob ein Erz in den Tiefen (Deepslate-Ebene) spawnt.
     * Nutzt die Tiefenschiefer-Varianten (Deepslate Ores).
     */
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
            return Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState(); // Erhöhte Diamanten-Chance!
        return null;
    }

    /**
     * Bestimmt, ob ein Erz im Nether spawnt.
     */
    private static BlockState getNetherOreState(double chance) {
        if (chance < 0.030)
            return Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
        if (chance < 0.050)
            return Blocks.NETHER_GOLD_ORE.defaultBlockState();
        if (chance < 0.053)
            return Blocks.ANCIENT_DEBRIS.defaultBlockState(); // Seltenes antiker Schrott!
        return null;
    }
}
