package aib;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Useful constants
 */
public class Constants {
    /** The window width */
    public static final double WINDOW_WIDTH = 1400.0;
    /** The window minimum width */
    public static final double WINDOW_MIN_WIDTH = 800.0;
    /** The map minimum width */
    public static final double MAP_MIN_WIDTH = 200.0;
    /** The window height */
    public static final double WINDOW_HEIGHT = 1000.0;
    /** The window minimum height */
    public static final double WINDOW_MINIMUM_HEIGHT = 400.0;
    /** The map width (in pixels) */
    public static final int MAP_SIZE_X = 1300;
    /** The map height (in pixels) */
    public static final int MAP_SIZE_Y = 1000;

    /** The initial sea level in the world,
     * i.e. the height (perlin noise height) up to which the world should be covered in water */
    public static final float BASE_SEA_LEVEL = 0.6f;
    /** The maximum height in the world in meters */
    public static final float MAX_TERRAIN_HEIGHT = 2500;
    /** The minimum height in the world in meters */
    public static final float MIN_TERRAIN_HEIGHT = -2500;

    /** A list of all the ids of water type terrains */
    public static final ArrayList<Integer> WATER_TERRAINS = new ArrayList<>(Arrays.asList(4,5,6));
    /** A list of all the ids of ice/snow type terrains  */
    public static final ArrayList<Integer> ICE_TERRAINS = new ArrayList<>(Arrays.asList(1,2,3));
}
