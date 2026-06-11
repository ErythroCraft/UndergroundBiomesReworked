package de.erythrocraft.undergroundbiomesforged.init;

import de.erythrocraft.undergroundbiomesforged.UndergroundBiomesForgedMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings({ "null" })
public class UndergroundBiomesForgedModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS,
			UndergroundBiomesForgedMod.MODID);

	public static final RegistryObject<Block> UBF_FLOOR = REGISTRY.register("ubf_floor",
			() -> new Block(createPlaceholderProperties()));

	public static final RegistryObject<Block> UBF_WALL = REGISTRY.register("ubf_wall",
			() -> new Block(createPlaceholderProperties()));

	public static final RegistryObject<Block> UBF_CEILING = REGISTRY.register("ubf_ceiling",
			() -> new Block(createPlaceholderProperties()));

	private UndergroundBiomesForgedModBlocks() {
		throw new UnsupportedOperationException("Dies ist eine Utility-Klasse und darf nicht instanziiert werden.");
	}

	private static BlockBehaviour.Properties createPlaceholderProperties() {
		return BlockBehaviour.Properties.copy(Blocks.STONE)
				.mapColor(MapColor.STONE)
				.strength(-1.0F, 3600000.0F)
				.sound(SoundType.STONE)
				.noLootTable();
	}
}
