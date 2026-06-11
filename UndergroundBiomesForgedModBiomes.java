package de.erythrocraft.undergroundbiomesforged.init;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class UndergroundBiomesForgedModBiomes {
	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		MinecraftServer server = event.getServer();
		Registry<LevelStem> levelStemTypeRegistry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
		Registry<Biome> biomeRegistry = server.registryAccess().registryOrThrow(Registries.BIOME);
		for (LevelStem levelStem : levelStemTypeRegistry.stream().toList()) {
			Holder<DimensionType> dimensionType = levelStem.type();
			if (dimensionType.is(BuiltinDimensionTypes.OVERWORLD)) {
				ChunkGenerator chunkGenerator = levelStem.generator();
				// Inject biomes to biome source
				if (chunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource noiseSource) {
					List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameters = new ArrayList<>(noiseSource.parameters().values());
					addParameterPoint(parameters, new Pair<>(new Climate.ParameterPoint(
							Climate.Parameter.span(0f, 2f),
							Climate.Parameter.span(0f, 2f),
							Climate.Parameter.span(0f, 2f),
							Climate.Parameter.span(0f, 2f),
							Climate.Parameter.span(0.2f, 0.9f),
							Climate.Parameter.span(-1.5f, 1.5f), 0),
							biomeRegistry.getHolderOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("underground_biomes_forged", "underground_top")))));

					chunkGenerator.biomeSource = MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(parameters));
					chunkGenerator.featuresPerStep = Suppliers.memoize(() -> FeatureSorter.buildFeaturesPerStep(
							List.copyOf(chunkGenerator.biomeSource.possibleBiomes()), biome -> chunkGenerator.generationSettingsGetter.apply(biome).features(), true));
				}
							
				if (chunkGenerator instanceof NoiseBasedChunkGenerator noiseGenerator) {
					((UndergroundBiomesForgedModNoiseGeneratorSettings) 
									(Object) noiseGenerator.settings.value()).underground_biomes_forgedDimensionTypeReference(dimensionType);
				}
			}
		}
							
	}

	public static SurfaceRules.RuleSource adaptSurfaceRule(SurfaceRules.RuleSource currentRuleSource,
			Holder<DimensionType> dimensionType) {
		if (dimensionType.is(BuiltinDimensionTypes.OVERWORLD))
			return injectOverworldSurfaceRules(currentRuleSource);
		return currentRuleSource;

	}

	private static SurfaceRules.RuleSource injectOverworldSurfaceRules(SurfaceRules.RuleSource currentRuleSource) {
		List<SurfaceRules.RuleSource> customSurfaceRules = new ArrayList<>();
		customSurfaceRules.add(anySurfaceRule(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("underground_biomes_forged", "underground_top")),
				UndergroundBiomesForgedModBlocks.TEST_MARKER_TOP.get().defaultBlockState(),
				UndergroundBiomesForgedModBlocks.TEST_MARKER_TOP.get().defaultBlockState(),
				UndergroundBiomesForgedModBlocks.TEST_MARKER_TOP.get().defaultBlockState()));

		customSurfaceRules.add(
				SurfaceRules.ifTrue(
						SurfaceRules.isBiome(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("underground_biomes_forged", "underground_top"))),
						SurfaceRules.ifTrue(
								// CaveSurface.CEILING ist der Schlüssel! Prüft auf festen Block über Luft.
								SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.CEILING),
								SurfaceRules.state(UndergroundBiomesForgedModBlocks.TEST_MARKER_TOP.get().defaultBlockState())
						)
				)
		);

		// 1. Spezifische Regel für DEIN top-Biom (Decke)
		customSurfaceRules.add(SurfaceRules.ifTrue(
				SurfaceRules.isBiome(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("underground_biomes_forged", "underground_top"))),
				SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.CEILING),
						SurfaceRules.state(UndergroundBiomesForgedModBlocks.TEST_MARKER_TOP.get().defaultBlockState()))
		));
		// 2. Spezifische Regel für DEIN down-Biom (Boden)
		customSurfaceRules.add(SurfaceRules.ifTrue(
				SurfaceRules.isBiome(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("underground_biomes_forged", "underground_down"))),
				SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
						SurfaceRules.state(UndergroundBiomesForgedModBlocks.TEST_MARKER_DOWN.get().defaultBlockState()))
		));

		// 3. DIE GLOBALEN SCHLÄUCHE (Für alle anderen Biome/Mods)
		// Diese Regel hat KEINE isBiome-Abfrage und gilt daher überall dort,
		// wo die obigen Regeln nicht greifen.
		customSurfaceRules.add(SurfaceRules.sequence(
				// Globale Decke für alle Höhlen
				SurfaceRules.ifTrue(
						SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.CEILING),
						SurfaceRules.state(UndergroundBiomesForgedModBlocks.TEST_MARKER_TOP.get().defaultBlockState()) // Beispiel: Alle Decken werden Basalt
				),
				// Globaler Boden für alle Höhlen
				SurfaceRules.ifTrue(
						SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
						SurfaceRules.state(UndergroundBiomesForgedModBlocks.TEST_MARKER_DOWN.get().defaultBlockState()) // Beispiel: Alle Böden werden Tuff
				)
		));



		customSurfaceRules.add(anySurfaceRule(ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("underground_biomes_forged", "underground_down")),
				UndergroundBiomesForgedModBlocks.TEST_MARKER_DOWN.get().defaultBlockState(),
				UndergroundBiomesForgedModBlocks.TEST_MARKER_DOWN.get().defaultBlockState(),
				UndergroundBiomesForgedModBlocks.TEST_MARKER_DOWN.get().defaultBlockState()));
				
		if (currentRuleSource instanceof SurfaceRules.SequenceRuleSource sequenceRuleSource) {
			customSurfaceRules.addAll(sequenceRuleSource.sequence());
        } else {
			customSurfaceRules.add(currentRuleSource);
        }
        return SurfaceRules.sequence(customSurfaceRules.toArray(SurfaceRules.RuleSource[]::new));
    }

	private static SurfaceRules.RuleSource anySurfaceRule(ResourceKey<Biome> biomeKey, BlockState groundBlock, BlockState undergroundBlock, BlockState underwaterBlock)
	{
		return SurfaceRules.ifTrue(SurfaceRules.isBiome(biomeKey), SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.aboveBottom(5), 0),
						SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.belowTop(5), 0)),
								SurfaceRules.sequence(SurfaceRules.ifTrue(

										SurfaceRules.stoneDepthCheck(0, false, 0, CaveSurface.FLOOR),
												SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.waterBlockCheck(-1, 0), SurfaceRules.state(groundBlock)), SurfaceRules.state(underwaterBlock))),

										SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, true, 0, CaveSurface.FLOOR), SurfaceRules.state(undergroundBlock))))));
	}

	private static void addParameterPoint(List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameters, Pair<Climate.ParameterPoint, Holder<Biome>> point) {
		if (!parameters.contains(point))
			parameters.add(point);
	}

	public interface UndergroundBiomesForgedModNoiseGeneratorSettings {
		void underground_biomes_forgedDimensionTypeReference(Holder<DimensionType> dimensionType);
	}
}