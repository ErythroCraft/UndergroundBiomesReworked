package de.erythrocraft.undergroundbiomesforged.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

@SuppressWarnings("null")
public class UbfCeilingBlock extends Block {
	public UbfCeilingBlock() {
		super(BlockBehaviour.Properties.copy(Blocks.STONE)
				.sound(SoundType.STONE)
				.strength(-1.0F, 3600000.0F)
				.lightLevel(state -> 0)
				.noOcclusion()
				.isRedstoneConductor((bs, br, bp) -> false)
				.replaceable());
	}
}
