package de.erythrocraft.ubreworked.worldgen;

import de.erythrocraft.ubreworked.UndergroundBiomesReworkedMod;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UndergroundBiomesReworkedMod.MODID)
@SuppressWarnings("null")
public class UbWorldGenEvents {

    private UbWorldGenEvents() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse und darf nicht instanziiert werden.");
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor level = event.getLevel();

        if (level != null && !level.isClientSide()) {
            ChunkAccess chunk = event.getChunk();

            if (chunk != null && chunk.getStatus().isOrAfter(ChunkStatus.FULL) && event.isNewChunk()) {

                UbCarver.carveTunnelChunk(chunk);
                UbOreInjector.resolveAndInjectChunk(chunk);
            }
        }
    }
}
