package de.erythrocraft.undergroundbiomesforged.init;

import de.erythrocraft.undergroundbiomesforged.UndergroundBiomesForgedMod;
import java.util.ArrayList;
import java.util.List;
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

	// 1. DIE FUNKTIONALEN PLATZHALTER (Bleiben für Carver & OreInjector erhalten)
	public static final RegistryObject<Block> UBF_FLOOR = REGISTRY.register("ubf_floor",
			() -> new Block(createPlaceholderProperties()));

	public static final RegistryObject<Block> UBF_WALL = REGISTRY.register("ubf_wall",
			() -> new Block(createPlaceholderProperties()));

	public static final RegistryObject<Block> UBF_CEILING = REGISTRY.register("ubf_ceiling",
			() -> new Block(createPlaceholderProperties()));

	// 2. DIE GLOBALE BLOCKLISTE FÜR DEINE ECHTEN GESTEINSARTEN (Jetzt sicher
	// deklariert)
	private static final List<RegistryObject<Block>> UBF_STONE_LIST = new ArrayList<>();

	/**
	 * SonarQube-konformer Getter (Behebt java:S2386).
	 * Gibt eine unmodifizierbare Ansicht der Liste zurück.
	 */
	public static List<RegistryObject<Block>> getStoneList() {
		return java.util.Collections.unmodifiableList(UBF_STONE_LIST);
	}

	// Statischer Initialisierungsblock: Hier definierst du alle deine Mod-Steine!
	static {
		// Magmatische Gesteine (Igneous)
		registerStone("granite_ubf", MapColor.DIRT);
		registerStone("rhyolite_ubf", MapColor.SAND);
		registerStone("andesite_ubf", MapColor.STONE);
		registerStone("gabbro_ubf", MapColor.COLOR_BLACK);
		registerStone("basalt_ubf", MapColor.COLOR_BLACK);
		registerStone("komatiite_ubf", MapColor.PODZOL);
		registerStone("dacite_ubf", MapColor.STONE);
		registerStone("pegmatite_ubf", MapColor.QUARTZ);

		// Metamorphe Gesteine (Metamorphic)
		registerStone("gneiss_ubf", MapColor.DIRT);
		registerStone("eclogite_ubf", MapColor.COLOR_GREEN);
		registerStone("marble_ubf", MapColor.QUARTZ);
		registerStone("quartzite_ubf", MapColor.QUARTZ);
		registerStone("schist_ubf", MapColor.STONE);
		registerStone("amphibolite_ubf", MapColor.COLOR_BLACK);
		registerStone("slate_ubf", MapColor.COLOR_GRAY);
		registerStone("soapstone_ubf", MapColor.STONE);

		// Sedimentgesteine (Sedimentary)
		registerStone("limestone_ubf", MapColor.SAND);
		registerStone("chalk_ubf", MapColor.SNOW);
		registerStone("shale_ubf", MapColor.COLOR_GRAY);
		registerStone("siltstone_ubf", MapColor.SAND);
		registerStone("sandstone_ubf", MapColor.SAND);
		registerStone("lignite_ubf", MapColor.COLOR_BLACK);
		registerStone("dolomite_ubf", MapColor.QUARTZ);
		registerStone("flint_ubf", MapColor.COLOR_BLACK);
	}

	private UndergroundBiomesForgedModBlocks() {
		throw new UnsupportedOperationException("Dies ist eine Utility-Klasse und darf nicht instanziiert werden.");
	}

	/**
	 * Hilfsmethode, um einen echten, abbaubaren Stein im System anzumelden
	 * und ihn automatisch in die globale Blockliste einzufügen.
	 */
	private static void registerStone(String name, MapColor color) {
		RegistryObject<Block> registeredBlock = REGISTRY.register(name,
				() -> new Block(createRealStoneProperties(color)));
		UBF_STONE_LIST.add(registeredBlock);
	}

	/**
	 * Eigenschaften für die unzerstörbaren Platzhalter
	 */
	private static BlockBehaviour.Properties createPlaceholderProperties() {
		return BlockBehaviour.Properties.copy(Blocks.STONE)
				.mapColor(MapColor.STONE)
				.strength(-1.0F, 3600000.0F)
				.sound(SoundType.STONE)
				.noLootTable();
	}

	/**
	 * Eigenschaften für deine echten, spielbaren Gesteinsblöcke.
	 * Sie verhalten sich wie normaler Stein, lassen sich abbauen und brauchen eine
	 * Spitzhacke.
	 */
	private static BlockBehaviour.Properties createRealStoneProperties(MapColor color) {
		return BlockBehaviour.Properties.copy(Blocks.STONE)
				.mapColor(color)
				.strength(1.5F, 6.0F) // Gleiche Härte wie normaler Minecraft-Stein!
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops(); // Setzt voraus, dass man eine Picke nutzt
	}
}
