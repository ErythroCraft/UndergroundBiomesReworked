package de.erythrocraft.undergroundbiomesforged.worldgen;

import de.erythrocraft.undergroundbiomesforged.config.UbfModConfig;
import de.erythrocraft.undergroundbiomesforged.init.ModStructurePieces; // WICHTIGER IMPORT
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class NeanderthalCavePiece extends StructurePiece {

    // Der Konstruktor bleibt starr auf 5 fixiert – Das sichert das Laden/Speichern
    // der Regionen ab!
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

        // Wir lesen den Radius sicher HIER aus, wenn die Generierung aktiv läuft!
        int configRadius = UbfModConfig.NEANDERTHAL_CAVE_RADIUS.get();

        // Begrenzung, damit es nicht die BoundingBox des Konstruktors sprengt (Maximal
        // 5)
        int radius = Math.min(configRadius, 5);

        // Das mathematische Zentrum liegt immer synchron zum Radius
        int centerX = radius;
        int centerY = radius;
        int centerZ = radius;

        // 1. Höhleneinkerbung schneiden (Übergibt den dynamischen Radius)
        this.carveCaveSphere(level, box, centerX, centerY, centerZ, radius);

        // 2. Erloschenes Lagerfeuer auf dem Höhlenboden platziert
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

    /**
     * Hilfsmethode: Errechnet die Kugelform dynamisch basierend auf dem übergebenen
     * Radius.
     */
    private void carveCaveSphere(
            @javax.annotation.Nonnull WorldGenLevel level,
            @javax.annotation.Nonnull BoundingBox box,
            int centerX,
            int centerY,
            int centerZ,
            int radius) {

        int maxBound = (radius * 2) + 1; // 11 bei Radius 5
        BlockState airState = java.util.Objects.requireNonNull(Blocks.AIR.defaultBlockState());

        for (int x = 0; x < maxBound; x++) {
            for (int y = 0; y < maxBound; y++) {
                for (int z = 0; z < maxBound; z++) {
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
