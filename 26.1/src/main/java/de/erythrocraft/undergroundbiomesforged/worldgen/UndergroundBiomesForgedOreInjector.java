package de.erythrocraft.undergroundbiomesforged.worldgen;

import de.erythrocraft.undergroundbiomesforged.config.UbfModConfig;
import de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

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

        double neanderthalChance = UbfModConfig.NEANDERTHAL_CAVE_CHANCE.get();
        int neanderthalRadius = Math.min(UbfModConfig.NEANDERTHAL_CAVE_RADIUS.get(), 5); // Schutzgrenze
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

        // --- NEANDERTHALER HÖHLEN-GENERIERUNG (REIN LOKAL & THREADSICHER) ---
        if (blockRand.nextDouble() < neanderthalChance) {
            // Wir erzwingen, dass das Zentrum der Höhle so liegt, dass sie nicht aus dem
            // Chunk ragt!
            // Ein Radius von max 5 benötigt links/rechts 5 Blöcke Platz. Also Range 5 bis
            // 10 innerhalb des Chunks (0-15).
            int localCenterX = 5 + blockRand.nextInt(6);
            int localCenterZ = 5 + blockRand.nextInt(6);
            int worldCenterY = isDeepWorld ? (-30 + blockRand.nextInt(60)) : (20 + blockRand.nextInt(70));

            BlockState airState = Blocks.AIR.defaultBlockState();

            // 1. Höhlenschale rein lokal aushöhlen
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int worldY = worldCenterY - neanderthalRadius; worldY <= worldCenterY
                            + neanderthalRadius; worldY++) {

                        int sIdx = chunk.getSectionIndex(worldY);
                        if (sIdx < 0 || sIdx >= sections.length)
                            continue;
                        LevelChunkSection sec = sections[sIdx];
                        if (sec == null)
                            continue;

                        double distance = Math.sqrt(
                                Math.pow(x - localCenterX, 2) +
                                        Math.pow(worldY - worldCenterY, 2) +
                                        Math.pow(z - localCenterZ, 2));

                        if (distance <= neanderthalRadius) {
                            BlockState state = sec.getBlockState(x, worldY & 15, z);
                            if (!state.isAir() && !state.is(Blocks.BEDROCK)) {
                                sec.setBlockState(x, worldY & 15, z, airState, false);
                            }
                        }
                    }
                }
            }

            // 2. Dekoration (Lagerfeuer & Knochen) auf dem lokalen Höhlenboden platzieren
            int localFloorY = worldCenterY - neanderthalRadius + 1;
            int floorSectionIdx = chunk.getSectionIndex(localFloorY);

            if (floorSectionIdx >= 0 && floorSectionIdx < sections.length) {
                LevelChunkSection floorSection = sections[floorSectionIdx];
                if (floorSection != null) {
                    int secY = localFloorY & 15;

                    // Erloschenes Lagerfeuer
                    BlockState campfireState = java.util.Objects.requireNonNull(
                            Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
                    floorSection.setBlockState(localCenterX, secY, localCenterZ, campfireState, false);

                    // Knochenreste
                    BlockState boneState = java.util.Objects.requireNonNull(Blocks.BONE_BLOCK.defaultBlockState());
                    if (blockRand.nextBoolean() && localCenterX - 1 >= 0) {
                        floorSection.setBlockState(localCenterX - 1, secY, localCenterZ, boneState, false);
                    }
                    if (blockRand.nextBoolean() && localCenterX + 1 < 16 && localCenterZ + 1 < 16) {
                        floorSection.setBlockState(localCenterX + 1, secY, localCenterZ + 1, boneState, false);
                    }
                }
            }
        }
    }

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
