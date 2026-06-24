package de.erythrocraft.ubreworked.init;

import de.erythrocraft.ubreworked.UndergroundBiomesReworkedMod;
import de.erythrocraft.ubreworked.worldgen.NeanderthalCavePiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class UbModStructurePieces {
        private UbModStructurePieces() {
                /* This utility class should not be instantiated */
        }

        public static final DeferredRegister<StructurePieceType> REGISTRY = DeferredRegister
                        .create(Registries.STRUCTURE_PIECE, UndergroundBiomesReworkedMod.MODID);

        // Registriert das eigentliche Stück Java-Code
        public static final RegistryObject<StructurePieceType> NEANDERTHAL_CAVE = REGISTRY.register("neanderthal_cave",
                        () -> NeanderthalCavePiece::new);
}
