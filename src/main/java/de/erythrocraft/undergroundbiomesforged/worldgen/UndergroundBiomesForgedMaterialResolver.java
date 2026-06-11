package de.erythrocraft.undergroundbiomesforged.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import de.erythrocraft.undergroundbiomesforged.init.UndergroundBiomesForgedModBlocks;

public class UndergroundBiomesForgedMaterialResolver {

    /**
     * Behebt den SonarQube-Fehler java:S1118.
     * Ein privater Konstruktor verhindert, dass diese Utility-Klasse instanziiert
     * wird.
     */
    private UndergroundBiomesForgedMaterialResolver() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse und darf nicht instanziiert werden.");
    }

    /**
     * Prüft, ob der übergebene Block ein UBF-Platzhalter ist, und löst ihn
     * in den finalen, echten Block aus der Config auf.
     * 
     * @param currentState
     *            Der aktuelle BlockState in der Welt
     * @param pos
     *            Die exakte Position (wichtig für die Y-Höhe und das Rauschen)
     * @param blendNoise
     *            Ein vorberechneter 3D-Noise-Wert für natürliche Block-Muster
     * @return Der transformierte, echte Minecraft- oder Mod-Block
     */
    public static BlockState resolvePlaceholder(BlockState currentState, BlockPos pos, double blendNoise) {
        Block currentBlock = currentState.getBlock();

        // Hole die Instanzen deiner drei registrierten Platzhalter
        Block ubfFloor = UndergroundBiomesForgedModBlocks.UBF_FLOOR.get();
        Block ubfWall = UndergroundBiomesForgedModBlocks.UBF_WALL.get();
        Block ubfCeiling = UndergroundBiomesForgedModBlocks.UBF_CEILING.get();

        // Schneller Performance-Check: Wenn es kein UBF-Platzhalter ist, sofort
        // abbrechen!
        if (currentBlock != ubfFloor && currentBlock != ubfWall && currentBlock != ubfCeiling) {
            return currentState;
        }

        // Bestimme den Typ des Platzhalters mithilfe der SonarQube-sicheren Konstanten
        String type = UbfBiomeConfig.TYPE_WALL;
        if (currentBlock == ubfFloor) {
            type = UbfBiomeConfig.TYPE_FLOOR;
        } else if (currentBlock == ubfCeiling) {
            type = UbfBiomeConfig.TYPE_CEILING;
        }

        // Hole die definierten echten Blöcke aus der Config
        BlockState primary = UbfBiomeConfig.getPrimaryReplacement(type, pos);
        BlockState secondary = UbfBiomeConfig.getSecondaryReplacement(type, pos);
        double threshold = UbfBiomeConfig.getBlendThreshold(type);

        // Nutze das übergebene Rauschen, um harte Kacheleffekte zu verhindern
        return (blendNoise > threshold) ? primary : secondary;
    }
}
