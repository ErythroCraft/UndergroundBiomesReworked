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

        // --- DYNAMISCHE HÖHLEN GENERIERUNG (OVERWORLD & NETHER) ---
        // --- UNIVERSELLE HÖHLEN GENERIERUNG (ALLE DIMENSIONEN) ---
        if (currentBlock == ubfWall) {
            long posHash = pos.asLong();
            java.util.Random rand = new java.util.Random(posHash);

            if (rand.nextDouble() < 0.0005) {
                final BlockPos spawnPos = pos.immutable();

                // DER TRICK: Wir holen uns den exakten Registrierungs-Schlüssel der aktuellen
                // Welt!
                // Egal ob Vanilla (Overworld, Nether, End) oder Mod-Dimensionen (z.B. Twilight
                // Forest)
                final ResourceKey<Level> currentDimension = chunk instanceof net.minecraft.world.level.chunk.LevelChunk levelChunk
                        ? levelChunk.getLevel().dimension()
                        : Level.OVERWORLD; // Sicherheits-Fallback

                UndergroundBiomesForgedMod.queueServerWork(1, () -> {
                    ServerLevel serverLevel = ServerLifecycleHooks.getCurrentServer().getLevel(currentDimension);

                    if (serverLevel != null) {
                        // Sicherheits-Check für die Nether-Höhen (gilt nur, wenn wir auch wirklich im
                        // Nether sind)
                        if (currentDimension == Level.NETHER && (spawnPos.getY() < 10 || spawnPos.getY() > 115)) {
                            return;
                        }

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

        // --- ENDE DER ERWEITERUNG ---

        BlockState primary = UbfBiomeConfig.getPrimaryReplacement(type, pos);
        BlockState secondary = UbfBiomeConfig.getSecondaryReplacement(type, pos);
        double threshold = UbfBiomeConfig.getBlendThreshold(type);

        return (blendNoise > threshold) ? primary : secondary;
    }
}
