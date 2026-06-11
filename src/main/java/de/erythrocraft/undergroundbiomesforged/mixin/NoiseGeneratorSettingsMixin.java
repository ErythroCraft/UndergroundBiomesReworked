package de.erythrocraft.undergroundbiomesforged.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.core.Holder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;

@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsMixin implements
		de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModBiomes.UndergroundBiomesForgedModNoiseGeneratorSettings {

	@Unique
	private Holder<DimensionType> undergroundBiomesForgedDimensionTypeReference;

	@WrapMethod(method = "surfaceRule")
	public SurfaceRules.RuleSource surfaceRule(Operation<SurfaceRules.RuleSource> original) {
		SurfaceRules.RuleSource retval = original.call();

		if (this.undergroundBiomesForgedDimensionTypeReference != null) {
			retval = de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModBiomes
					.adaptSurfaceRule(retval, this.undergroundBiomesForgedDimensionTypeReference);
		}

		return retval;
	}

	@Override
	public void undergroundBiomesForgedDimensionTypeReference(Holder<DimensionType> dimensionType) {
		this.undergroundBiomesForgedDimensionTypeReference = dimensionType;
	}
}