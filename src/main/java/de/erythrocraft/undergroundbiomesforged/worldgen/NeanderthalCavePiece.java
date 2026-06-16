package de.erythrocraft.undergroundbiomesforged.worldgen;

import de.erythrocraft.undergroundbiomesforged.init.ModStructurePieces;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class NeanderthalCavePiece extends StructurePiece {

    public NeanderthalCavePiece(@javax.annotation.Nonnull BlockPos wallPos) {
        super(ModStructurePieces.NEANDERTHAL_CAVE.get(), 0,
                new BoundingBox(
                        wallPos.getX() - 5, wallPos.getY() - 5, wallPos.getZ() - 5,
                        wallPos.getX() + 5, wallPos.getY() + 5, wallPos.getZ() + 5));
    }

    public NeanderthalCavePiece(
            @javax.annotation.Nonnull StructurePieceSerializationContext context,
            @javax.annotation.Nonnull CompoundTag tag) {
        super(ModStructurePieces.NEANDERTHAL_CAVE.get(), tag);
    }

    @Override
    protected void addAdditionalSaveData(
            @javax.annotation.Nonnull StructurePieceSerializationContext context,
            @javax.annotation.Nonnull CompoundTag tag) {
    }

    @Override
    public void postProcess(
            @javax.annotation.Nonnull WorldGenLevel level,
            @javax.annotation.Nonnull StructureManager structureManager,
            @javax.annotation.Nonnull net.minecraft.world.level.chunk.ChunkGenerator chunkGenerator,
            @javax.annotation.Nonnull RandomSource random,
            @javax.annotation.Nonnull BoundingBox box,
            @javax.annotation.Nonnull ChunkPos chunkPos,
            @javax.annotation.Nonnull BlockPos spawnPos) {

        int centerX = 5;
        int centerY = 5;
        int centerZ = 5;

        // 1. Höhleneinkerbung schneiden
        this.carveCaveSphere(level, box, centerX, centerY, centerZ);

        // 2. Erloschenes Lagerfeuer
        BlockPos firePos = new BlockPos(this.getWorldX(centerX, centerZ), this.getWorldY(1),
                this.getWorldZ(centerX, centerZ));
        if (box.isInside(firePos)) {
            BlockState campfireState = java.util.Objects
                    .requireNonNull(Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
            level.setBlock(firePos, campfireState, 2);
        }

        // 3. Knochenreste
        BlockState boneState = java.util.Objects.requireNonNull(Blocks.BONE_BLOCK.defaultBlockState());

        if (random.nextBoolean())
            this.placeBlock(level, boneState, centerX - 1, 1, centerZ, box);
        if (random.nextBoolean())
            this.placeBlock(level, boneState, centerX + 1, 1, centerZ + 1, box);
    }

    private void carveCaveSphere(
            @javax.annotation.Nonnull WorldGenLevel level,
            @javax.annotation.Nonnull BoundingBox box,
            int centerX,
            int centerY,
            int centerZ) {
        int radius = 5;
        BlockState airState = java.util.Objects.requireNonNull(Blocks.AIR.defaultBlockState());

        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                for (int z = 0; z < 11; z++) {
                    double distance = Math
                            .sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) + Math.pow(z - centerZ, 2));

                    if (distance <= radius && !this.getBlock(level, x, y, z, box).isAir()) {
                        this.placeBlock(level, airState, x, y, z, box);
                    }
                }
            }
        }
    }
}
