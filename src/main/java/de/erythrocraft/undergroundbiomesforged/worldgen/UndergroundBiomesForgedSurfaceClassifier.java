package de.erythrocraft.undergroundbiomesforged.worldgen;

import de.erythrocraft.undergroundbiomesforged.UndergroundBiomesForgedMod;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class UndergroundBiomesForgedSurfaceClassifier {

    /**
     * Behebt den SonarQube-Fehler java:S1118.
     * Ein privater Konstruktor verhindert, dass diese Utility-Klasse instanziiert
     * wird.
     */
    private UndergroundBiomesForgedSurfaceClassifier() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse und darf nicht instanziiert werden.");
    }

    /**
     * Berechnet den Normalenvektor basierend auf dem Dichte-Gradienten der Höhle.
     * Ein positiver Y-Wert bedeutet, die Luft ist OBEN (also ist der Block der
     * BODEN).
     * Ein negativer Y-Wert bedeutet, die Luft ist UNTEN (also ist der Block die
     * DECKE).
     * 
     * @param densityNorth
     *            Dichtewert des Blocks im Norden (z - 1)
     * @param densitySouth
     *            Dichtewert des Blocks im Süden (z + 1)
     * @param densityUp
     *            Dichtewert des Blocks darüber (y + 1)
     * @param densityDown
     *            Dichtewert des Blocks darunter (y - 1)
     * @param densityWest
     *            Dichtewert des Blocks im Westen (x - 1)
     * @param densityEast
     *            Dichtewert des Blocks im Osten (x + 1)
     * @return Der passende UBF-Platzhalter-BlockState
     */
    public static BlockState calculateSurfaceBlock(
            double densityNorth, double densitySouth,
            double densityUp, double densityDown,
            double densityWest, double densityEast) {

        // Gradienten-Vektor berechnen (Änderungsrate der Höhlendichte)
        // Luft in der Höhle hat eine hohe Dichte (carve), Stein hat eine niedrige
        // Dichte
        double gradX = densityEast - densityWest;
        double gradY = densityUp - densityDown;
        double gradZ = densitySouth - densityNorth;

        // Vektor normalisieren, um die reine Richtung zu erhalten
        double length = Math.sqrt(gradX * gradX + gradY * gradY + gradZ * gradZ);

        if (length < 0.001) {
            // Sicherheits-Fallback: Falls keine klare Richtung erkennbar ist, ist es eine
            // Wand
            return UndergroundBiomesForgedMod.classifySurfaceType(Direction.NORTH);
        }

        double normalizedY = gradY / length;

        // Klassifizierung anhand des Y-Schwellenwerts (Thresholds aus deinem Konzept)
        if (normalizedY > 0.6) {
            // Der Vektor zeigt steil nach oben -> Der Block befindet sich UNTER der Luft ->
            // BODEN
            return UndergroundBiomesForgedMod.classifySurfaceType(Direction.UP);
        } else if (normalizedY < -0.6) {
            // Der Vektor zeigt steil nach unten -> Der Block befindet sich ÜBER der Luft ->
            // DECKE
            return UndergroundBiomesForgedMod.classifySurfaceType(Direction.DOWN);
        } else {
            // Der Vektor ist flach/horizontal -> WAND
            return UndergroundBiomesForgedMod.classifySurfaceType(Direction.NORTH);
        }
    }
}
