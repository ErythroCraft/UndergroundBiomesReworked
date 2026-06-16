package de.erythrocraft.undergroundbiomesforged.worldgen;

import de.erythrocraft.undergroundbiomesforged.config.UbfModConfig;
import de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.BoundingBox; // Import der Config

@SuppressWarnings("null")
public class UndergroundBiomesForgedOreInjector {

    private UndergroundBiomesForgedOreInjector() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse.");
    }

    public static void resolveAndInjectChunk(ChunkAccess chunk) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        boolean isDeepWorld = chunk.getMinBuildHeight() < 0;
        int minY = chunk.getMinBuildHeight();
        int maxY = isDeepWorld ? 64 : 127;

        // ==========================================
        // PERFORMANCE-RETTUNG: CONFIG-WERTE CACHEN!
        // Wir lesen die Config nur EINMAL pro Chunk aus, statt Millionen Mal!
        // ==========================================
        double neanderthalChance = UbfModConfig.NEANDERTHAL_CAVE_CHANCE.get();
        int neanderthalRadius = UbfModConfig.NEANDERTHAL_CAVE_RADIUS.get();
        double oreMultUpper = UbfModConfig.ORE_CHANCE_UPPER.get();
        double oreMultDeep = UbfModConfig.ORE_CHANCE_DEEP.get();
        double oreMultNether = UbfModConfig.ORE_CHANCE_NETHER.get();

        Block ubfFloor = UndergroundBiomesForgedModBlocks.UBF_FLOOR.get();
        Block ubfWall = UndergroundBiomesForgedModBlocks.UBF_WALL.get();
        Block ubfCeiling = UndergroundBiomesForgedModBlocks.UBF_CEILING.get();

        long chunkSeed = ((long) chunk.getPos().x << 32) | (chunk.getPos().z & 0xFFFFFFFFL);
        java.util.Random blockRand = new java.util.Random(chunkSeed);

        LevelChunkSection[] sections = chunk.getSections();
        for (int sectionIdx = 0; sectionIdx < sections.length; sectionIdx++) {
            LevelChunkSection section = sections[sectionIdx];

            if (section == null || section.hasOnlyAir()) {
                continue;
            }

            int sectionMinY = chunk.getSectionYFromSectionIndex(sectionIdx) << 4;
            if (sectionMinY < minY || sectionMinY > maxY) {
                continue;
            }

            for (int y = 0; y < 16; y++) {
                int worldY = sectionMinY + y;
                if (worldY < minY || worldY > maxY)
                    continue;

                for (int x = 0; x < 16; x++) {
                    int worldX = minX + x;

                    for (int z = 0; z < 16; z++) {
                        int worldZ = minZ + z;

                        BlockState currentState = section.getBlockState(x, y, z);
                        Block currentBlock = currentState.getBlock();

                        if (currentBlock == ubfFloor || currentBlock == ubfWall || currentBlock == ubfCeiling) {

                            double oreChance = blockRand.nextDouble();
                            BlockState finalState;

                            // Erz-Injektion (nutzt jetzt die schnellen lokalen Cache-Variablen!)
                            if (isDeepWorld) {
                                finalState = (worldY < 0) ? getDeepOreState(oreChance, oreMultDeep)
                                        : getUpperOreState(oreChance, oreMultUpper);
                            } else {
                                finalState = getNetherOreState(oreChance, oreMultNether);
                            }

                            if (finalState == null) {
                                double blendNoise = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(
                                        (int) (worldX * 2.5),
                                        (int) (worldY * 3.0),
                                        (int) (worldZ * 2.5));
                                BlockPos pos = new BlockPos(worldX, worldY, worldZ);
                                finalState = UndergroundBiomesForgedMaterialResolver
                                        .resolvePlaceholder(chunk, currentState, pos, blendNoise);
                            }

                            section.setBlockState(x, y, z, finalState, false);
                        }
                    }
                }
            }
        }

        // --- NEANDERTHALER HÖHLEN-GENERIERUNG ---
        if (blockRand.nextDouble() < neanderthalChance) {
            int randomX = minX + blockRand.nextInt(16);
            int randomZ = minZ + blockRand.nextInt(16);
            int randomY = isDeepWorld ? (-30 + blockRand.nextInt(60)) : (20 + blockRand.nextInt(70));

            BlockPos spawnPos = new BlockPos(randomX, randomY, randomZ);

            if (chunk instanceof WorldGenLevel worldGenLevel && worldGenLevel.getBlockState(spawnPos).isAir()) {

                de.erythrocraft.undergroundbiomesforged.worldgen.NeanderthalCavePiece cave = new de.erythrocraft.undergroundbiomesforged.worldgen.NeanderthalCavePiece(
                        spawnPos);

                // Box dynamisch an den gecachten Radius anpassen!
                BoundingBox totalBox = new BoundingBox(
                        spawnPos.getX() - neanderthalRadius, spawnPos.getY() - neanderthalRadius,
                        spawnPos.getZ() - neanderthalRadius,
                        spawnPos.getX() + neanderthalRadius, spawnPos.getY() + neanderthalRadius,
                        spawnPos.getZ() + neanderthalRadius);

                cave.postProcess(
                        worldGenLevel,
                        null,
                        null,
                        worldGenLevel.getRandom(),
                        totalBox,
                        new net.minecraft.world.level.ChunkPos(spawnPos),
                        spawnPos);
            }
        }
    }

    // Methoden angepasst, um die Multiplikatoren direkt als schnellen Parameter zu
    // empfangen
    private static BlockState getUpperOreState(double chance, double mult) {
        if (chance < (0.020 * mult))
            return Blocks.COAL_ORE.defaultBlockState();
        if (chance < (0.035 * mult))
            return Blocks.IRON_ORE.defaultBlockState();
        if (chance < (0.042 * mult))
            return Blocks.COPPER_ORE.defaultBlockState();
        if (chance < (0.045 * mult))
            return Blocks.GOLD_ORE.defaultBlockState();
        return null;
    }

    private static BlockState getDeepOreState(double chance, double mult) {
        if (chance < (0.015 * mult))
            return Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState();
        if (chance < (0.028 * mult))
            return Blocks.DEEPSLATE_IRON_ORE.defaultBlockState();
        if (chance < (0.038 * mult))
            return Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState();
        if (chance < (0.045 * mult))
            return Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState();
        if (chance < (0.049 * mult))
            return Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState();
        return null;
    }

    private static BlockState getNetherOreState(double chance, double mult) {
        if (chance < (0.030 * mult))
            return Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
        if (chance < (0.050 * mult))
            return Blocks.NETHER_GOLD_ORE.defaultBlockState();
        if (chance < (0.053 * mult))
            return Blocks.ANCIENT_DEBRIS.defaultBlockState();
        return null;
    }
}
