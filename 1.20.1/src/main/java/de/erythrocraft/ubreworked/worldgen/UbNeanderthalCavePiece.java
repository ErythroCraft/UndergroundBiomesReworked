package de.erythrocraft.ubreworked.worldgen;

import de.erythrocraft.ubreworked.config.UbModConfig;
import de.erythrocraft.ubreworked.init.UbModStructurePieces;
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

public class UbNeanderthalCavePiece extends StructurePiece {

    // Der Konstruktor bleibt starr auf 5 fixiert – Das sichert das Laden/Speichern
    // der Regionen ab!
    // BoundingBox reicht lokal von 0 bis 10 in alle Achsen (Zentrum ist 5)
    public UbNeanderthalCavePiece(@javax.annotation.Nonnull BlockPos wallPos) {
        super(UbModStructurePieces.NEANDERTHAL_CAVE.get(), 0,
                new BoundingBox(
                        wallPos.getX() - 5, wallPos.getY() - 5, wallPos.getZ() - 5,
                        wallPos.getX() + 5, wallPos.getY() + 5, wallPos.getZ() + 5));
    }

    public UbNeanderthalCavePiece(
            @javax.annotation.Nonnull StructurePieceSerializationContext context,
            @javax.annotation.Nonnull CompoundTag tag) {
        super(UbModStructurePieces.NEANDERTHAL_CAVE.get(), tag);
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

        int configRadius = UbModConfig.NEANDERTHAL_CAVE_RADIUS.get();

        // Begrenzung, damit es nicht die BoundingBox des Konstruktors sprengt (Maximal
        // 5)
        int radius = Math.min(configRadius, 5);

        // Das mathematische und physische Zentrum der Struktur-Box ist IMMER 5 (Mitte
        // von 0-10)
        int centerX = 5;
        int centerY = 5;
        int centerZ = 5;

        // 1. Höhleneinkerbung schneiden
        this.carveCaveSphere(level, box, centerX, centerY, centerZ, radius);

        // Der Höhlenboden wandert dynamisch mit dem Radius nach oben (z.B. Y=1 bei
        // Radius 5, Y=3 bei Radius 3)
        int floorY = centerY - radius + 1;

        // 2. Erloschenes Lagerfeuer auf dem Höhlenboden platziert
        // Null-Safety für die IDE durch requireNonNull um das gesamte Statement gelöst
        BlockState campfireState = java.util.Objects.requireNonNull(
                Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));

        // Nutzt jetzt die sichere placeBlock-Methode mit relativen Koordinaten
        // (Verhindert 94%-Freeze!)
        this.placeBlock(level, campfireState, centerX, floorY, centerZ, box);

        // 3. Knochenreste
        BlockState boneState = java.util.Objects.requireNonNull(Blocks.BONE_BLOCK.defaultBlockState());

        if (random.nextBoolean()) {
            this.placeBlock(level, boneState, centerX - 1, floorY, centerZ, box);
        }
        if (random.nextBoolean()) {
            this.placeBlock(level, boneState, centerX + 1, floorY, centerZ + 1, box);
        }
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

        BlockState airState = java.util.Objects.requireNonNull(Blocks.AIR.defaultBlockState());

        // Wir loopen immer starr durch die gesamte BoundingBox (0 bis 10),
        // die Kugel darin wird über die Distanzberechnung zum Zentrum (5,5,5) skaliert.
        for (int x = 0; x <= 10; x++) {
            for (int y = 0; y <= 10; y++) {
                for (int z = 0; z <= 10; z++) {

                    double distance = Math.sqrt(
                            Math.pow(x - centerX, 2) +
                                    Math.pow(y - centerY, 2) +
                                    Math.pow(z - centerZ, 2));

                    // Nur aushöhlen, wenn wir im Radius liegen und kein Bedrock/Luft im Weg ist
                    if (distance <= radius) {
                        BlockState currentState = this.getBlock(level, x, y, z, box);
                        if (!currentState.isAir() && !currentState.is(Blocks.BEDROCK)) {
                            this.placeBlock(level, airState, x, y, z, box);
                        }
                    }
                }
            }
        }
    }
}
