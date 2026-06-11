package de.erythrocraft.undergroundbiomesforged;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModBlocks;
import de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModItems;
import de.erythrocraft.undergroundbiomesforged.worldgen.UbfBiomeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod("underground_biomes_forged")
@Mod.EventBusSubscriber(modid = UndergroundBiomesForgedMod.MODID)
@SuppressWarnings("java:S1118")
public class UndergroundBiomesForgedMod {
	public static final Logger LOGGER = LogManager.getLogger(UndergroundBiomesForgedMod.class);
	public static final String MODID = "underground_biomes_forged";

	public UndergroundBiomesForgedMod(FMLJavaModLoadingContext context) {
		LOGGER.info("Underground Biomes Forged (UBF) wird initialisiert...");

		IEventBus bus = context.getModEventBus();

		UndergroundBiomesForgedModBlocks.REGISTRY.register(bus);
		UndergroundBiomesForgedModItems.REGISTRY.register(bus);
	}

	public static BlockState classifySurfaceType(Direction normal) {
		Block floor = UndergroundBiomesForgedModBlocks.UBF_FLOOR.get();
		Block wall = UndergroundBiomesForgedModBlocks.UBF_WALL.get();
		Block ceiling = UndergroundBiomesForgedModBlocks.UBF_CEILING.get();

		float yNormal = normal.getStepY();

		if (yNormal > 0.6f) {
			return floor.defaultBlockState();
		} else if (yNormal < -0.6f) {
			return ceiling.defaultBlockState();
		} else {
			return wall.defaultBlockState();
		}
	}

	public static BlockState resolvePlaceholder(BlockState currentState, BlockPos pos, double blendNoise) {
		Block currentBlock = currentState.getBlock();

		Block ubfFloor = UndergroundBiomesForgedModBlocks.UBF_FLOOR.get();
		Block ubfWall = UndergroundBiomesForgedModBlocks.UBF_WALL.get();
		Block ubfCeiling = UndergroundBiomesForgedModBlocks.UBF_CEILING.get();

		if (currentBlock != ubfFloor && currentBlock != ubfWall && currentBlock != ubfCeiling) {
			return currentState;
		}

		String type = UbfBiomeConfig.TYPE_WALL;
		if (currentBlock == ubfFloor) {
			type = UbfBiomeConfig.TYPE_FLOOR;
		} else if (currentBlock == ubfCeiling) {
			type = UbfBiomeConfig.TYPE_CEILING;
		}

		BlockState primaryState = UbfBiomeConfig.getPrimaryReplacement(type, pos);
		BlockState secondaryState = UbfBiomeConfig.getSecondaryReplacement(type, pos);
		double threshold = UbfBiomeConfig.getBlendThreshold(type);

		return (blendNoise > threshold) ? primaryState : secondaryState;
	}

	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
			ResourceLocation.fromNamespaceAndPath(MODID, MODID),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals);
	private static int messageID = 0;

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder,
			Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++;
	}

	private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
	}

	@SubscribeEvent
	public static void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
			workQueue.forEach(work -> {
				work.setValue(work.getValue() - 1);
				if (work.getValue() == 0)
					actions.add(work);
			});
			actions.forEach(e -> e.getKey().run());
			workQueue.removeAll(actions);
		}
	}
}
