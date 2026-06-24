package de.erythrocraft.ubreworked.worldgen;

public class UbNoiseGenerator {

    private UbNoiseGenerator() {
        throw new UnsupportedOperationException("Dies ist eine Utility-Klasse.");
    }

    /**
     * Berechnet die Tunnel-Dichte an einer exakten Position (X, Y, Z).
     * Kombiniert Flow-Noise, Worm-Noise und Detail-Noise nach der UBF-Hauptformel.
     */
    public static double sampleTunnelDensity(int x, int y, int z) {
        double flow = sampleSimplex3D(x * 0.004, y * 0.003, z * 0.004);
        double worm = sampleSimplex3D(x * 0.02, y * 0.01, z * 0.02);
        double detail = sampleSimplex3D(x * 0.08, y * 0.08, z * 0.08);

        return (flow * 0.65) + (worm * 0.25) + (detail * 0.10);
    }

    /**
     * Hilfsklasse zur Kapselung der Simplex-Ecken, um die kognitive Komplexität zu
     * senken.
     */
    private static class SimplexIndices {
        final int i1;
        final int j1;
        final int k1;
        final int i2;
        final int j2;
        final int k2;

        SimplexIndices(int i1, int j1, int k1, int i2, int j2, int k2) {
            this.i1 = i1;
            this.j1 = j1;
            this.k1 = k1;
            this.i2 = i2;
            this.j2 = j2;
            this.k2 = k2;
        }
    }

    /**
     * Bestimmt die Indices des Simplex-Subraums.
     */
    private static SimplexIndices determineIndices(double x0, double y0, double z0) {
        if (x0 >= y0) {
            if (y0 >= z0)
                return new SimplexIndices(1, 0, 0, 1, 1, 0);
            if (x0 >= z0)
                return new SimplexIndices(1, 0, 0, 1, 0, 1);
            return new SimplexIndices(0, 0, 1, 1, 0, 1);
        } else {
            if (y0 < z0)
                return new SimplexIndices(0, 0, 1, 0, 1, 1);
            if (x0 < z0)
                return new SimplexIndices(0, 1, 0, 0, 1, 1);
            return new SimplexIndices(0, 1, 0, 1, 1, 0);
        }
    }

    /**
     * Ein kompakpter, performanter 3D-Simplex-Rausch-Algorithmus.
     */
    private static double sampleSimplex3D(double x, double y, double z) {
        double s = (x + y + z) * 0.333333333;
        int i = fastFloor(x + s);
        int j = fastFloor(y + s);
        int k = fastFloor(z + s);

        double t = (i + j + k) * 0.166666667;
        double x0 = x - (i - t);
        double y0 = y - (j - t);
        double z0 = z - (k - t);

        SimplexIndices idx = determineIndices(x0, y0, z0);

        double x1 = x0 - idx.i1 + 0.166666667;
        double y1 = y0 - idx.j1 + 0.166666667;
        double z1 = z0 - idx.k1 + 0.166666667;
        double x2 = x0 - idx.i2 + 0.333333333;
        double y2 = y0 - idx.j2 + 0.333333333;
        double z2 = z0 - idx.k2 + 0.333333333;
        double x3 = x0 - 1.0 + 0.5;
        double y3 = y0 - 1.0 + 0.5;
        double z3 = z0 - 1.0 + 0.5;

        double n0 = computeCornerContribution(x0, y0, z0, i, j, k);
        double n1 = computeCornerContribution(x1, y1, z1, i + idx.i1, j + idx.j1, k + idx.k1);
        double n2 = computeCornerContribution(x2, y2, z2, i + idx.i2, j + idx.j2, k + idx.k2);
        double n3 = computeCornerContribution(x3, y3, z3, i + 1, j + 1, k + 1);

        return 32.0 * (n0 + n1 + n2 + n3);
    }

    /**
     * Berechnet den Dichte-Beitrag einer einzelnen Simplex-Ecke.
     */
    private static double computeCornerContribution(double dx, double dy, double dz, int hashX, int hashY, int hashZ) {
        double t = 0.6 - dx * dx - dy * dy - dz * dz;
        if (t < 0)
            return 0.0;
        t *= t;
        return t * t * grad(hashX, hashY, hashZ, dx, dy, dz);
    }

    private static int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    /**
     * Berechnet den Gradienten-Skalarwert für die Rauschecke.
     * Aufgelöst für java:S3358 (Keine verschachtelten ternären Operatoren mehr).
     */
    private static double grad(int hashX, int hashY, int hashZ, double x, double y, double z) {
        int h = (hashX * 127 + hashY * 311 + hashZ) & 15;

        // Erster ternärer Operator (flach, unverschachtelt)
        double u = h < 8 ? x : y;

        // Verschachtelung durch klares if-else ersetzt
        double v;
        if (h < 4) {
            v = y;
        } else if (h == 12 || h == 14) {
            v = x;
        } else {
            v = z;
        }

        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
