package aib.environment;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * TerrainSections provides all the necessary associations to turn information about terrain types at each pixel
 * into a suggestive colour terrain map
 */
public class TerrainSections {

    /**
     * An ordered structure that maps a (maximum) temperature value to a map from a (maximum) terrain height to a terrain ID
     * to clarify: TreeMap<Temperature, TreeMap<Terrain Height, Terrain ID>>
     *     e.g. if we have the entries 15 -> (0.6, 5) and 15 -> (1, 7)
     *     it means that, on the current pixel,
     *     if the temperature is below 15 degrees
     *     and the terrain Perlin noise height is below 0.6 (noise is between 0 and 1)
     *     then, the pixel will be assigned the terrain with the ID 5, which is Deep Water.
     *     Otherwise, if the pixel noise height is above 0.6 and below 1
     *     then the pixel will be assigned the terrain with ID 7, which is Grass
     * The fact that the map is ordered (TreeMap) is essential to maintain the targeted ranges
     */
    public static final Map<Integer, Map<Float,Integer>> sections = new TreeMap<>();

    /** Predefined terrains, with ids, names and colours associated */
    public static final TerrainType IceTerrain            = new TerrainType("Ice",              Color.rgb(159,235,232), 1);
    public static final TerrainType SnowTerrain           = new TerrainType("Thick Snow",       Color.rgb(224,242,249), 2);
    public static final TerrainType SnowyTerrain          = new TerrainType("Snowy",            Color.rgb(218,236,243), 3);
    public static final TerrainType DeeperWaterTerrain    = new TerrainType("Deeper Water",     Color.rgb(53,44,121), 4);
    public static final TerrainType DeepWaterTerrain      = new TerrainType("Deep Water",       Color.rgb(61,56,143), 5);
    public static final TerrainType ShallowWaterTerrain   = new TerrainType("Shallow Water",    Color.rgb(77,87,170), 6);
    public static final TerrainType GrassTerrain          = new TerrainType("Grass",            Color.rgb(64,168,55), 7);
    public static final TerrainType DarkGrassTerrain      = new TerrainType("Dark Grass",       Color.rgb(58,145,50), 8);
    public static final TerrainType SandTerrain           = new TerrainType("Sand",             Color.rgb(212,209,137), 9);
    public static final TerrainType RedSandTerrain        = new TerrainType("Red Sand",         Color.rgb(205,165,105), 10);
    public static final TerrainType RockTerrain           = new TerrainType("Rock",             Color.rgb(95,75,75), 11);
    public static final TerrainType DarkRockTerrain       = new TerrainType("Dark Rock",        Color.rgb(70,55,55), 12);
    public static final ArrayList<TerrainType> terrs      = new ArrayList<>(Arrays.asList(
            IceTerrain, SnowTerrain, SnowyTerrain, DeeperWaterTerrain, DeepWaterTerrain, ShallowWaterTerrain,
            GrassTerrain, DarkGrassTerrain, SandTerrain, RedSandTerrain, RockTerrain, DarkRockTerrain));

    /**
     * Given an ID, return the terrain type with that ID
     * @param id The ID of the terrain type you want to retrieve
     * @return The terrain type with that ID
     */
    public static TerrainType getTerrainByID(int id) {
        // Find the terrain type with that ID, otherwise return null
        for(TerrainType t : terrs) {
            if(id == t.getId()) return t;
        }
        return null;
    }

    /**
     * Initialise the terrain sections ordered map, according to the SEA LEVEL value
     * i.e. if sea level rises the other terrain type should adapt to remain above it
     * this way the terrain types are assigned by percentages rather than fixed values.
     */
    public static void initSectionsMap() {
        // Clear the map to begin
        sections.clear();

        // An important factor to keep in mind is that
        // for all temperature ranges, all heights (0 to 1) should be covered
        // to ensure all possible cases are considered and no exceptions will rise.

        // For all temperatures up to -8 degrees, the terrain should be ice irrespective of height
        sections.put(-8, new TreeMap<Float,Integer>() {{
            // lower threshold
            // A lower threshold boundary is always necessary for the implementation,
            // because, when setting the terrain colour for a pixel, we need to calculate the colour
            // by determining where it lies between the previous terrain type and the actual terrain type.
            // So, while there will never be noise heights below 0f, it is necessary for the next entry
            put(0f,1);
            // For all heights, set terrain to ice (id = 1 is ice)
            put(1.1f,1);
        }});

        // For temperatures above -8 degrees and below -5 degrees,
        // the terrain should be thick snow irrespective of height
        sections.put(-5, new TreeMap<Float,Integer>() {{
            // lower threshold
            put(0f,2);
            // For all heights, set terrain to thick snow (id = 2 is thick snow)
            put(1.1f,2);
        }});

        // For temperatures between -5 and 0 degrees, the terrain should be snowy irrespective of height
        sections.put(0, new TreeMap<Float,Integer>() {{
            // lower threshold
            put(0f,3);
            // For all heights, set terrain to snowy (id = 3 is snowy)
            put(1.1f,3);
        }});

        // For temperatures between 0 and 2 degrees:
        sections.put(2, new TreeMap<Float,Integer>() {{
            // lower threshold
            put(0f,4);
            // SEA_LEVEL is the current height that water goes up to
            // Deeper water should be one third of all water (the deepest third) (id = 4 is deeper water)
            put(2*World.SEA_LEVEL/6,4);
            // Deep water should be above deeper water, the next third and a half of all the water (id = 5 is deep water)
            put(5*World.SEA_LEVEL/6,5);
            // Shallow water should be above the deep water, the remainder of all the water (one sixth) (id = 6 is shallow water)
            put(World.SEA_LEVEL,6);
            // Rock and dark rock should be a small height above the water (id = 11 is rock, id = 12 is dark rock)
            put(6.5f*World.SEA_LEVEL/6,11);
            put(7f*World.SEA_LEVEL/6,12);
            // Anything above that should be snowy (id = 3 is snowy)
            put(1.1f,3);
        }});

        // For temperatures between 2 and 6 degrees:
        sections.put(6, new TreeMap<Float,Integer>() {{
            // lower threshold
            put(0f,4);
            // SEA_LEVEL is the current height that water goes up to
            // Deeper water should be one third of all water (the deepest third) (id = 4 is deeper water)
            put(2*World.SEA_LEVEL/6,4);
            // Deep water should be above deeper water, the next third and a half of all the water (id = 5 is deep water)
            put(5*World.SEA_LEVEL/6,5);
            // Shallow water should be above the deep water, the remainder of all the water (one sixth) (id = 6 is shallow water)
            put(World.SEA_LEVEL,6);
            // Rock should be a small height above the water (id = 11 is rock)
            put(7.5f*World.SEA_LEVEL/6,11);
            // Anything above that should be dark rock (id = 12 is dark rock)
            put(1.1f,12);
        }});

        // For temperatures between 6 and 19 degrees:
        sections.put(19, new TreeMap<Float,Integer>() {{
            // lower threshold
            put(0f,4);
            // SEA_LEVEL is the current height that water goes up to
            // Deeper water should be one third of all water (the deepest third) (id = 4 is deeper water)
            put(2*World.SEA_LEVEL/6,4);
            // Deep water should be above deeper water, the next third and a half of all the water (id = 5 is deep water)
            put(5*World.SEA_LEVEL/6,5);
            // Shallow water should be above the deep water, the remainder of all the water (one sixth) (id = 6 is shallow water)
            put(World.SEA_LEVEL,6);
            // Grass and dark grass should be above the water and about the same amount as one third of the water
            // (id = 7 is grass, id = 8 is dark grass)
            put(7*World.SEA_LEVEL/6,7);
            put(8*World.SEA_LEVEL/6,8);
            // Rock should be a small height above the grass, and one sixth of the water percentage (id = 11 is rock)
            put(9*World.SEA_LEVEL/6,11);
            // Anything above that should be dark rock (id = 12 is dark rock)
            put(1.1f,12);
        }});

        // For temperatures between 19 and 40 degrees:
        sections.put(40, new TreeMap<Float,Integer>() {{
            // lower threshold
            put(0f,4);
            // SEA_LEVEL is the current height that water goes up to
            // Deeper water should be one third of all water (the deepest third) (id = 4 is deeper water)
            put(2*World.SEA_LEVEL/6,4);
            // Deep water should be above deeper water, the next third of all the water (id = 5 is deep water)
            put(4*World.SEA_LEVEL/6,5);
            // Shallow water should be above the deep water, the remainder of all the water (one third) (id = 6 is shallow water)
            put(World.SEA_LEVEL,6);
            // Sand should be above the water and about the same amount as one sixth of the water
            put(6.5f*World.SEA_LEVEL/6,9);
            put(7*World.SEA_LEVEL/6,9);
            // Anything above that should be red sand (id = 10 is red sand)
            put(1.1f,10);
        }});
    }

    /**
     * Print the map of terrain sections
     * useful to see map values after sea level changes
     */
    public static void printSectionsMap() {
        for (Map.Entry<Integer,Map<Float,Integer>> entry: sections.entrySet()) {
            System.out.println("& temperatures below: " + entry.getKey());
            for(Map.Entry<Float,Integer> innerMapEntry : entry.getValue().entrySet()) {
                System.out.println(innerMapEntry.getKey() + " " + getTerrainByID(innerMapEntry.getValue()).getName());
            }
            System.out.println("-------------------------");
            System.out.println("For temperatures above: " + entry.getKey());
        }
    }

}
