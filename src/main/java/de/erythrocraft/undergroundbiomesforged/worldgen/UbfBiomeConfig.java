package de.erythrocraft.undergroundbiomesforged.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class UbfBiomeConfig {

    public static final String TYPE_FLOOR = "floor";
    public static final String TYPE_CEILING = "ceiling";
    public static final String TYPE_WALL = "wall";

    private UbfBiomeConfig() {
        throw new UnsupportedOperationException("Utility-Klasse");
    }

    /**
     * Ermittelt den aktuellen Untergrund-Gesteinstyp (Biom) basierend auf
     * großflächigem Rauschen.
     * Erzeugt fließende Übergänge wie an der Oberfläche.
     */
    public static int getUndergroundBiomeId(BlockPos pos) {
        // Wir runden die doubles mathematisch korrekt ab, um sie als int zu übergeben
        int noiseX = (int) Math.floor(pos.getX() / 300.0);
        int noiseZ = (int) Math.floor(pos.getZ() / 300.0);

        // Wir rufen die Methode mit den passenden int-Argumenten auf
        double biomeNoise = UndergroundBiomesForgedNoiseGenerator.sampleTunnelDensity(
                noiseX,
                0,
                noiseZ);

        // Mappt das Rauschen (-1.0 bis 1.0) auf drei Gesteinszonen (0, 1, 2)
        if (biomeNoise < -0.3)
            return 0; // Biom 0: Sedimentgesteine
        else if (biomeNoise < 0.3)
            return 1; // Biom 1: Metamorphe Gesteine
        else
            return 2; // Biom 2: Magmatische Gesteine
    }

    public static BlockState getPrimaryReplacement(String placeholderType, BlockPos pos) {
        int y = pos.getY();

        // 1. NETHER-BIOME (Bleiben unberührt)
        if (y >= 0 && y <= 128 && placeholderType.equals(TYPE_WALL) && (pos.getX() + pos.getZ()) % 7 == 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> Blocks.NETHERRACK.defaultBlockState();
                case TYPE_CEILING -> Blocks.BASALT.defaultBlockState();
                case TYPE_WALL -> Blocks.BLACKSTONE.defaultBlockState();
                default -> Blocks.NETHERRACK.defaultBlockState();
            };
        }

        // 2. DYNAMISCHE OBERWELT-BIOME JE NACH GEGEND
        int biomeId = getUndergroundBiomeId(pos);

        if (y < 0) {
            // DEEP LAYER VARIATIONEN JE NACH UNDERGROUND-BIOM
            return switch (biomeId) {
                case 0 -> switch (placeholderType) { // Sediment-Höhle tief unten
                    case TYPE_FLOOR -> Blocks.CALCITE.defaultBlockState();
                    default -> Blocks.TUFF.defaultBlockState();
                };
                case 1 -> switch (placeholderType) { // Metamorphe Höhle tief unten
                    case TYPE_CEILING -> Blocks.SMOOTH_BASALT.defaultBlockState();
                    default -> Blocks.DEEPSLATE.defaultBlockState();
                };
                default -> switch (placeholderType) { // Magmatische Höhle tief unten
                    case TYPE_CEILING -> Blocks.BUDDING_AMETHYST.defaultBlockState();
                    default -> Blocks.DEEPSLATE.defaultBlockState();
                };
            };
        } else {
            // UPPER LAYER VARIATIONEN JE NACH UNDERGROUND-BIOM
            return switch (biomeId) {
                case 0 -> switch (placeholderType) { // Sediment-Höhle oben
                    case TYPE_FLOOR -> Blocks.MUD.defaultBlockState();
                    case TYPE_WALL -> Blocks.DRIPSTONE_BLOCK.defaultBlockState();
                    default -> Blocks.STONE.defaultBlockState();
                };
                case 1 -> switch (placeholderType) { // Metamorphe Höhle oben
                    case TYPE_FLOOR -> Blocks.MOSS_BLOCK.defaultBlockState();
                    case TYPE_WALL -> Blocks.ANDESITE.defaultBlockState();
                    default -> Blocks.STONE.defaultBlockState();
                };
                default -> switch (placeholderType) { // Magmatische Höhle oben
                    case TYPE_FLOOR -> Blocks.MOSS_BLOCK.defaultBlockState();
                    case TYPE_WALL -> Blocks.DIORITE.defaultBlockState();
                    default -> Blocks.STONE.defaultBlockState();
                };
            };
        }
    }

    public static BlockState getSecondaryReplacement(String placeholderType, BlockPos pos) {
        int y = pos.getY();
        long posHash = pos.asLong();
        java.util.Random rand = new java.util.Random(posHash);
        double chance = rand.nextDouble();

        // 1. NETHER BLENDING (Bleibt unberührt)
        if (y >= 0 && y <= 128 && (pos.getX() * pos.getZ()) % 5 == 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> chance < 0.70 ? Blocks.SOUL_SOIL.defaultBlockState()
                        : Blocks.SOUL_SAND.defaultBlockState();
                case TYPE_CEILING -> chance < 0.80 ? Blocks.SMOOTH_BASALT.defaultBlockState()
                        : Blocks.GLOWSTONE.defaultBlockState();
                case TYPE_WALL -> {
                    if (chance < 0.60)
                        yield Blocks.GILDED_BLACKSTONE.defaultBlockState();
                    else if (chance < 0.90)
                        yield Blocks.MAGMA_BLOCK.defaultBlockState();
                    else
                        yield Blocks.CRYING_OBSIDIAN.defaultBlockState();
                }
                default -> Blocks.NETHERRACK.defaultBlockState();
            };
        }

        // 2. OBERWELT BLENDING BASIEREND AUF UNDERGROUND-BIOM
        int biomeId = getUndergroundBiomeId(pos);

        if (y < 0) {
            return switch (placeholderType) {
                case TYPE_FLOOR -> chance < 0.70 ? Blocks.AMETHYST_BLOCK.defaultBlockState()
                        : Blocks.BUDDING_AMETHYST.defaultBlockState();
                case TYPE_CEILING -> chance < 0.80 ? Blocks.SMOOTH_BASALT.defaultBlockState()
                        : Blocks.BASALT.defaultBlockState();
                case TYPE_WALL -> {
                    // Je nach Biom mischen wir andere Steine in die tiefen Wände ein!
                    if (biomeId == 0)
                        yield chance < 0.60 ? Blocks.TUFF.defaultBlockState() : Blocks.CALCITE.defaultBlockState();
                    else
                        yield chance < 0.70 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.TUFF.defaultBlockState();
                }
                default -> Blocks.TUFF.defaultBlockState();
            };
        } else {
            return switch (placeholderType) {
                case TYPE_FLOOR -> chance < 0.80 ? Blocks.MUD.defaultBlockState()
                        : Blocks.MUDDY_MANGROVE_ROOTS.defaultBlockState();
                case TYPE_CEILING -> chance < 0.70 ? Blocks.POINTED_DRIPSTONE.defaultBlockState()
                        : Blocks.DRIPSTONE_BLOCK.defaultBlockState();
                case TYPE_WALL -> {
                    // Lokale feine Blockmischungen innerhalb des Bioms (Kacheleffekt verhindern)
                    if (biomeId == 0) { // Sediment-Gegend: Hauptsächlich Kalkstein/Dripstone-Mischung
                        if (chance < 0.70)
                            yield Blocks.STONE.defaultBlockState();
                        else
                            yield Blocks.DRIPSTONE_BLOCK.defaultBlockState();
                    } else if (biomeId == 1) { // Metamorph-Gegend: Andesit & Schiefer
                        if (chance < 0.70)
                            yield Blocks.ANDESITE.defaultBlockState();
                        else
                            yield Blocks.GRAVEL.defaultBlockState();
                    } else { // Magmatisch-Gegend: Diorit & Granit
                        if (chance < 0.50)
                            yield Blocks.DIORITE.defaultBlockState();
                        else if (chance < 0.90)
                            yield Blocks.GRANITE.defaultBlockState();
                        else
                            yield Blocks.STONE.defaultBlockState();
                    }
                }
                default -> Blocks.ANDESITE.defaultBlockState();
            };
        }
    }

    public static double getBlendThreshold(String placeholderType) {
        return switch (placeholderType) {
            case TYPE_FLOOR -> 0.35;
            case TYPE_WALL -> 0.45;
            case TYPE_CEILING -> 0.25;
            default -> 0.50;
        };
    }
}
