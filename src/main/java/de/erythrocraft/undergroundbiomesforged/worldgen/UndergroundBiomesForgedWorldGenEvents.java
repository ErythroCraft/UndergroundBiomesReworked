package de.erythrocraft.undergroundbiomesforged.worldgen;

import de.erythrocraft.undergroundbiomesforged.UndergroundBiomesForgedMod;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UndergroundBiomesForgedMod.MODID)
@SuppressWarnings("null")
public class UndergroundBiomesForgedWorldGenEvents {

    private UndergroundBiomesForgedWorldGenEvents() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse und darf nicht instanziiert werden.");
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor level = event.getLevel();
        if (level != null && !level.isClientSide()) {
            ChunkAccess chunk = event.getChunk();
            if (!event.isNewChunk()) {
                UndergroundBiomesForgedCarver.carveTunnelChunk(chunk);
            }
            UndergroundBiomesForgedOreInjector.resolveAndInjectChunk(chunk);
        }
    }
}
