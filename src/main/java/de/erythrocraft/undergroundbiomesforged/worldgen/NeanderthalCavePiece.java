package de.erythrocraft.undergroundbiomesforged.worldgen;

import de.erythrocraft.undergroundbiomesforged.init.ModStructurePieces;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class NeanderthalCavePiece extends StructurePiece {

    // Konstruktor für die eigentliche Platzierung im Spiel
    public NeanderthalCavePiece(BlockPos wallPos) {
        super(ModStructurePieces.NEANDERTHAL_CAVE.get(), 0,
                new BoundingBox(
                        wallPos.getX() - 3, wallPos.getY() - 3, wallPos.getZ() - 3,
                        wallPos.getX() + 3, wallPos.getY() + 3, wallPos.getZ() + 3));
    }

    // Dieser Konstruktor wird von Minecraft beim Laden der Welt aufgerufen
    public NeanderthalCavePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructurePieces.NEANDERTHAL_CAVE.get(), tag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        // Falls du Variablen im Tag speichern willst, kommt das hier hin
    }

    @Override
    public void postProcess(WorldGenLevel level, net.minecraft.world.level.StructureManager structureManager,
            net.minecraft.world.level.chunk.ChunkGenerator chunkGenerator, RandomSource random,
            BoundingBox box, net.minecraft.server.level.ChunkPos chunkPos, BlockPos spawnPos) {

        int radius = 3;
        int centerX = 3;
        int centerY = 3;
        int centerZ = 3;

        // 1. Höhleneinkerbung schneiden
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                for (int z = 0; z < 7; z++) {
                    double distance = Math
                            .sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) + Math.pow(z - centerZ, 2));
                    if (distance <= radius) {
                        if (!this.getBlock(level, x, y, z, box).isAir()) {
                            this.placeBlock(level, Blocks.AIR.defaultBlockState(), x, y, z, box);
                        }
                    }
                }
            }
        }

        // 2. Erloschenes Lagerfeuer
        BlockPos firePos = new BlockPos(this.getWorldX(centerX, centerZ), this.getWorldY(1),
                this.getWorldZ(centerX, centerZ));
        if (box.isInside(firePos)) {
            level.setBlock(firePos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false), 2);
        }

        // 3. Knochenreste
        if (random.nextBoolean())
            this.placeBlock(level, Blocks.BONE_BLOCK.defaultBlockState(), centerX - 1, 1, centerZ, box);
        if (random.nextBoolean())
            this.placeBlock(level, Blocks.BONE_BLOCK.defaultBlockState(), centerX + 1, 1, centerZ + 1, box);
    }
}
