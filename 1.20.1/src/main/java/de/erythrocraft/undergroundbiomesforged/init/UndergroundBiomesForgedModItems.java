package de.erythrocraft.undergroundbiomesforged.init;

import de.erythrocraft.undergroundbiomesforged.UndergroundBiomesForgedMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings({ "null" })
public class UndergroundBiomesForgedModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS,
			UndergroundBiomesForgedMod.MODID);
	public static final RegistryObject<Item> UBF_CEILING = block(UndergroundBiomesForgedModBlocks.UBF_CEILING);
	public static final RegistryObject<Item> UBF_WALL = block(UndergroundBiomesForgedModBlocks.UBF_WALL);
	public static final RegistryObject<Item> UBF_FLOOR = block(UndergroundBiomesForgedModBlocks.UBF_FLOOR);

	private UndergroundBiomesForgedModItems() {
		throw new UnsupportedOperationException("Dies ist eine Utility-Klasse und darf nicht instanziiert werden.");
	}

	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return block(block, new Item.Properties());
	}

	private static RegistryObject<Item> block(RegistryObject<Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}
