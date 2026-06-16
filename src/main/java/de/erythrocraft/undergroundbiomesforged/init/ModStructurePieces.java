package de.erythrocraft.undergroundbiomesforged.init;

import de.erythrocraft.undergroundbiomesforged.UndergroundBiomesForgedMod;
import de.erythrocraft.undergroundbiomesforged.worldgen.NeanderthalCavePiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructurePieces {
    public static final DeferredRegister<StructurePieceType> REGISTRY = DeferredRegister
            .create(Registries.STRUCTURE_PIECE, UndergroundBiomesForgedMod.MODID);

    // Registriert das eigentliche Stück Java-Code
    public static final RegistryObject<StructurePieceType> NEANDERTHAL_CAVE = REGISTRY.register("neanderthal_cave",
            () -> NeanderthalCavePiece::new);
}
