package de.erythrocraft.ubreworked.init;

import de.erythrocraft.ubreworked.UndergroundBiomesReworkedMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings({ "null" })
public class UbModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS,
			UndergroundBiomesReworkedMod.MODID);
	public static final RegistryObject<Item> UBF_CEILING = block(UbModBlocks.UBF_CEILING);
	public static final RegistryObject<Item> UBF_WALL = block(UbModBlocks.UBF_WALL);
	public static final RegistryObject<Item> UBF_FLOOR = block(UbModBlocks.UBF_FLOOR);

	private UbModItems() {
		throw new UnsupportedOperationException("Dies ist eine Utility-Klasse und darf nicht instanziiert werden.");
	}

	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return block(block, new Item.Properties());
	}

	private static RegistryObject<Item> block(RegistryObject<Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}
