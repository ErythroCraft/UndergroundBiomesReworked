package de.erythrocraft.undergroundbiomesforged.init;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;

import de.erythrocraft.undergroundbiomesforged.UndergroundBiomesForgedMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = UndergroundBiomesForgedMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@SuppressWarnings("null")
public class UndergroundBiomesForgedModBiomes {

	public static final ResourceKey<Biome> BIOME_TOP = ResourceKey.create(Registries.BIOME,
			ResourceLocation.fromNamespaceAndPath(UndergroundBiomesForgedMod.MODID, "underground_top"));
	public static final ResourceKey<Biome> BIOME_DOWN = ResourceKey.create(Registries.BIOME,
			ResourceLocation.fromNamespaceAndPath(UndergroundBiomesForgedMod.MODID, "underground_down"));
	public static final ResourceKey<Biome> BIOME_SIDE = ResourceKey.create(Registries.BIOME,
			ResourceLocation.fromNamespaceAndPath(UndergroundBiomesForgedMod.MODID, "underground_side"));

	private UndergroundBiomesForgedModBiomes() {
		throw new UnsupportedOperationException("Dies ist eine Utility-Klasse.");
	}

	@SubscribeEvent
	public static void onRegister(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.BIOME)) {
			event.register(Registries.BIOME, BIOME_TOP.location(), () -> createUndergroundBiome(0.7f, 0.8f, 0x888888));
			event.register(Registries.BIOME, BIOME_DOWN.location(), () -> createUndergroundBiome(0.2f, 0.9f, 0x444455));
		}
	}

	private static Biome createUndergroundBiome(float temperature, float downfall, int fogColor) {
		MobSpawnSettings.Builder spawnSettings = new MobSpawnSettings.Builder();
		HolderGetter<PlacedFeature> featureLookup = new EmptyHolderGetter<>();
		HolderGetter<ConfiguredWorldCarver<?>> carverLookup = new EmptyHolderGetter<>();

		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(featureLookup, carverLookup);
		BiomeGenerationSettings genSettings = builder.build();

		return new Biome.BiomeBuilder()
				.hasPrecipitation(false)
				.temperature(temperature)
				.downfall(downfall)
				.specialEffects(new BiomeSpecialEffects.Builder()
						.waterColor(0x3f76e4)
						.waterFogColor(0x050533)
						.fogColor(fogColor)
						.skyColor(0x000000)
						.build())
				.mobSpawnSettings(spawnSettings.build())
				.generationSettings(genSettings)
				.build();
	}

	@Mod.EventBusSubscriber(modid = UndergroundBiomesForgedMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ForgeForgeLifecycleEvents {

		private ForgeForgeLifecycleEvents() {
			throw new UnsupportedOperationException("Innere Utility-Klasse.");
		}

		@SubscribeEvent
		public static void onServerAboutToStart(ServerAboutToStartEvent event) {
			MinecraftServer server = event.getServer();
			Registry<LevelStem> levelStemTypeRegistry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
			Registry<Biome> biomeRegistry = server.registryAccess().registryOrThrow(Registries.BIOME);

			for (LevelStem levelStem : levelStemTypeRegistry.stream().toList()) {
				Holder<DimensionType> dimensionType = levelStem.type();
				ChunkGenerator chunkGenerator = levelStem.generator();

				// --- DYNAMISCHE INJEKTION IN OVERWORLD & NETHER ---
				if (dimensionType.is(BuiltinDimensionTypes.OVERWORLD)
						|| dimensionType.is(BuiltinDimensionTypes.NETHER)) {

					if (chunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource noiseSource) {
						List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameters = new ArrayList<>(
								noiseSource.parameters().values());

						// Wir fügen deine Untergrund-Biome zu den Klima-Parametern hinzu
						parameters.add(new Pair<>(new Climate.ParameterPoint(
								Climate.Parameter.span(0f, 2f), Climate.Parameter.span(0f, 2f),
								Climate.Parameter.span(0f, 2f), Climate.Parameter.span(0f, 2f),
								Climate.Parameter.span(0.2f, 0.9f), Climate.Parameter.span(-1.5f, 1.5f), 0),
								biomeRegistry.getHolderOrThrow(BIOME_TOP)));

						parameters.add(new Pair<>(new Climate.ParameterPoint(
								Climate.Parameter.span(0f, 2f), Climate.Parameter.span(0f, 2f),
								Climate.Parameter.span(0f, 2f), Climate.Parameter.span(0f, 2f),
								Climate.Parameter.span(0.2f, 0.9f), Climate.Parameter.span(-1.5f, 1.5f), 0),
								biomeRegistry.getHolderOrThrow(BIOME_DOWN)));

						chunkGenerator.biomeSource = MultiNoiseBiomeSource
								.createFromList(new Climate.ParameterList<>(parameters));
						chunkGenerator.featuresPerStep = Suppliers.memoize(() -> FeatureSorter.buildFeaturesPerStep(
								List.copyOf(chunkGenerator.biomeSource.possibleBiomes()),
								biome -> chunkGenerator.generationSettingsGetter.apply(biome).features(), true));
					}

					// Übergibt den Dimensionstyp an deine Einstellungsklasse für das Mixin
					if (chunkGenerator instanceof NoiseBasedChunkGenerator noiseGenerator) {
						((UndergroundBiomesForgedModNoiseGeneratorSettings) (Object) noiseGenerator.settings.value())
								.undergroundBiomesForgedDimensionTypeReference(dimensionType);
					}
				}
			}
		}
	}

	/**
	 * Prüft die Dimension und wendet die entsprechenden Oberflächen-Regeln an.
	 */
	public static SurfaceRules.RuleSource adaptSurfaceRule(SurfaceRules.RuleSource currentRuleSource,
			Holder<DimensionType> dimensionType) {
		// Regeln greifen nun sowohl in der Oberwelt als auch im Nether!
		if (dimensionType.is(BuiltinDimensionTypes.OVERWORLD) || dimensionType.is(BuiltinDimensionTypes.NETHER)) {
			return injectUbfSurfaceRules(currentRuleSource);
		}
		return currentRuleSource;
	}

	/**
	 * Injiziert die UBF-Platzhalterregeln in die bestehende Dimensions-Pipeline.
	 * Umbenannt von injectOverworldSurfaceRules zu injectUbfSurfaceRules (da
	 * universell).
	 */
	private static SurfaceRules.RuleSource injectUbfSurfaceRules(SurfaceRules.RuleSource currentRuleSource) {
		List<SurfaceRules.RuleSource> customSurfaceRules = new ArrayList<>();

		BlockState floorState = UndergroundBiomesForgedModBlocks.UBF_FLOOR.get().defaultBlockState();
		BlockState wallState = UndergroundBiomesForgedModBlocks.UBF_WALL.get().defaultBlockState();
		BlockState ceilingState = UndergroundBiomesForgedModBlocks.UBF_CEILING.get().defaultBlockState();

		// BIOME_TOP Regeln hinzufügen
		customSurfaceRules.add(SurfaceRules.ifTrue(
				SurfaceRules.isBiome(BIOME_TOP),
				SurfaceRules.sequence(
						SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.CEILING),
								SurfaceRules.state(ceilingState)),
						SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
								SurfaceRules.state(floorState)),
						SurfaceRules.state(wallState))));

		// BIOME_DOWN Regeln hinzufügen
		customSurfaceRules.add(SurfaceRules.ifTrue(
				SurfaceRules.isBiome(BIOME_DOWN),
				SurfaceRules.sequence(
						SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.CEILING),
								SurfaceRules.state(ceilingState)),
						SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
								SurfaceRules.state(floorState)),
						SurfaceRules.state(wallState))));

		// Bestehende Regeln der Dimension hinten anhängen
		if (currentRuleSource instanceof SurfaceRules.SequenceRuleSource sequenceRuleSource) {
			customSurfaceRules.addAll(sequenceRuleSource.sequence());
		} else {
			customSurfaceRules.add(currentRuleSource);
		}

		return SurfaceRules.sequence(customSurfaceRules.toArray(SurfaceRules.RuleSource[]::new));
	}

	public interface UndergroundBiomesForgedModNoiseGeneratorSettings {
		void undergroundBiomesForgedDimensionTypeReference(Holder<DimensionType> dimensionType);
	}

	private static class EmptyHolderGetter<T> implements HolderGetter<T> {
		@Override
		public java.util.Optional<net.minecraft.core.Holder.Reference<T>> get(
				net.minecraft.resources.ResourceKey<T> key) {
			return java.util.Optional.empty();
		}

		@Override
		public java.util.Optional<net.minecraft.core.HolderSet.Named<T>> get(net.minecraft.tags.TagKey<T> tag) {
			return java.util.Optional.empty();
		}
	}
}
