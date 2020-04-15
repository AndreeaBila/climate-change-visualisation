package aib.environment;

import aib.Constants;
import aib.Main;
import aib.Renderer;
import aib.life.Animal;
import javafx.scene.paint.Color;

import java.util.*;

import static aib.Main.userInterface;

/**
 * The static World class handles
 */
public class World {
    /** The map of pixels */
    public static Pixel[][] pixels = new Pixel[Constants.MAP_SIZE_X][Constants.MAP_SIZE_Y];
    /** The list of animals in the world */
    private static List<Animal> animals = new ArrayList<>();

    /** The variable that tracks the year the world is in, necessary when the user changes to a new year
     * so we know how much time has passed and thus how big the changes in the world should be */
    public static double prevYear;
    /** The sea level after all the ice melts */
    public static float ICE_MELTED_SEA_LEVEL;
    /** The current sea level in the world */
    public static float SEA_LEVEL;
    /** The lowest point in the current world */
    public static float lowestPoint;
    /** The highest point in the current world */
    public static float highestPoint;
    /** The average temperature for the current world */
    private static float averageWorldTemperature;
    /** The highest temperature on the map */
    private static float maxTemp;
    /** The lowest temperature on the map */
    private static float minTemp;
    /** The total map temperature (all pixels temperatures added) */
    private static float totalMapTemperature;
    /** The number of pixels of water and ice */
    private static int waterPixels, icePixels;
    /** The initial average temperature on the map */
    private static float initialAverageTemperature;

    /**
     * Initialise world members
     */
    public static void initWorld() {
        prevYear = 2000;
        SEA_LEVEL = Constants.BASE_SEA_LEVEL;
        // Initialise the terrain sections map with the set SEA LEVEL
        TerrainSections.initSectionsMap();
//        ICE_MELTED_SEA_LEVEL = (7-7*Constants.BASE_SEA_LEVEL)/250f + Constants.BASE_SEA_LEVEL;
        ICE_MELTED_SEA_LEVEL = 0.7f;
        lowestPoint = Integer.MAX_VALUE;
        highestPoint = Integer.MIN_VALUE;
        averageWorldTemperature = 0;
        maxTemp = -100f;
        minTemp = 100f;
        totalMapTemperature = 0f;
        waterPixels = 0;
        icePixels = 0;
    }

    /**
     * Get the list of animals in the world
     * @return The list of animals
     */
    public static List<Animal> getAnimals() {
        return animals;
    }

    /**
     * Set the list of animals in the world
     * @param animals The list of animals
     */
    public static void setAnimals(List<Animal> animals) {
        World.animals = animals;
    }

    /**
     * Create a new world map with the input parameters
     * @param seed The map seed
     * @param scale The scale for the noise
     * @param octaves The octaves for the noise
     * @param persistence The persistence for the noise
     * @param lacunarity The lacunarity for the noise
     * @param offsetX The horizontal offset for the noise
     * @param offsetY The vertical offset for the noise
     */
    public static void calculateMap(int seed, float scale, int octaves, float persistence, float lacunarity, float offsetX, float offsetY) {

        // Clear the list of animals
        World.getAnimals().clear();
        initWorld();

        // Create the Perlin noise height map
        float[][] noiseMap = Noise.generateNoiseMap(Constants.MAP_SIZE_X, Constants.MAP_SIZE_Y,
                seed,scale,octaves,persistence,lacunarity,offsetX,offsetY);

        // Create a second layer of Perlin noise, for the initial greenhouse gas levels
        Random r = new Random(Integer.parseInt(Main.userInterface.seedField.getText()));
        float[][] greenhouseMap = Noise.generateNoiseMap(Constants.MAP_SIZE_X, Constants.MAP_SIZE_Y,
                r.nextInt(),2.5f,5,0.4f,2.7f,0,0);

        // Create each pixel of the map
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                // Set the pixel noise height
                World.pixels[x][y] = new Pixel(noiseMap[x][y]);
                // Set the pixel greenhouse gas level
                World.pixels[x][y].setGreenhouseHeight(greenhouseMap[x][y]);
                World.pixels[x][y].setGreenhouseGasFactor(greenhouseMap[x][y]);

                // Set the pixel temperature
                setTemperature(x,y);

                // Set the pixel terrain and terrain colour
                Color terrainColour = getTerrain(x, y);
                pixels[x][y].setColour(terrainColour);

                // Count the number of water pixels
                if(Constants.WATER_TERRAINS.contains(pixels[x][y].getTerrainType().getId())) waterPixels++;
                // And the number of ice pixels
                else if(Constants.ICE_TERRAINS.contains(pixels[x][y].getTerrainType().getId())) icePixels++;
            }
        }

        // Calculate the average world temperature
        averageWorldTemperature = totalMapTemperature / (pixels.length * pixels[0].length);
        initialAverageTemperature = averageWorldTemperature;
        // Display the temperature to the user
        Main.userInterface.printToUserTextBox("Average world temperature: " + String.format("%.2f",averageWorldTemperature) + "\u00B0" + "C");
        // Display the percentage of water and ice on the map to the user
        Main.userInterface.printToUserTextBox("Water percentage: " +  String.format("%.2f",(waterPixels *100.0f)/(Constants.MAP_SIZE_Y * Constants.MAP_SIZE_X)) + "%");
        Main.userInterface.printToUserTextBox("Ice percentage: " +  String.format("%.2f",(icePixels *100.0f)/(Constants.MAP_SIZE_Y * Constants.MAP_SIZE_X)) + "%");
    }

    /**
     * Set the temperature for a given pixel
     * @param x The x coordinate of the pixel
     * @param y The y coordinate of the pixel
     */
    public static void setTemperature(int x, int y) {
        // Get the pixel at these coordinates
        Pixel pixel = pixels[x][y];
        // Calculate the pixel latitude
        int latitude = -(y - Constants.MAP_SIZE_Y/2);
        // Set the point latitude
        pixel.setLatitude(latitude/(Constants.MAP_SIZE_Y/2/90f));

        // First step of temperature calculation:
        // Calculate sea level (0 meters above water) temperature
        float latitudeTemperature = 7.5f*(float)Math.cos((float)latitude/160) +12.5f;
        pixel.setLatitudeTemperature(latitudeTemperature);

        // Second step of temperature calculation:
        // Calculate the temperature at any height, given the temperature at sea level
        calculateHeightTemperature(x,y);

        // Third step of temperature calculation:
        // Calculate the final temperature by deviating the height temperature by the greenhouse gas level
        float finalTemperature = calculateFinalTemperature(x,y);

        // Update minimum and maximum temperature if it is the case
        if(finalTemperature < minTemp) minTemp = finalTemperature;
        if(finalTemperature > maxTemp) maxTemp = finalTemperature;
        // Update the global total temperature
        totalMapTemperature += finalTemperature;
    }

    /**
     * Calculate the height temperature, that depends on the latitude and height of the point
     * @param x The x coordinate of the point
     * @param y The y coordinate of the point
     * @return The height temperature of the point
     */
    public static float calculateHeightTemperature(int x, int y) {
        Pixel pixel = pixels[x][y];

        // Calculate the pixel height in meters (above or below sea level)
        float terrHeight;
        if(pixel.getNoiseHeight() >= SEA_LEVEL) {
            float rmax = 1f;
            if (SEA_LEVEL >= rmax) rmax = SEA_LEVEL + 0.01f;
            terrHeight = Renderer.scaleToRange(SEA_LEVEL, rmax, 0f, Constants.MAX_TERRAIN_HEIGHT, pixel.getNoiseHeight());
        } else
            terrHeight = Renderer.scaleToRange(0,SEA_LEVEL, Constants.MIN_TERRAIN_HEIGHT,0f,pixel.getNoiseHeight());

        pixel.setTerrainHeight(terrHeight);

        // Calculate the height temperature at the current height, given the temperature at sea level (height = 0)
        float heightTemperature = pixel.getLatitudeTemperature() - (0.00649f * Math.abs(terrHeight));
        pixel.setHeightTemperature(heightTemperature);

        return heightTemperature;
    }

    /**
     * Calculate the final point temperature, that depends on the latitude, height and greenhouse gas
     * @param x The x coordinate of the pixel
     * @param y The y coordinate of the pixel
     * @return The final temperature of the pixel
     */
    public static float calculateFinalTemperature(int x, int y) {
        // Deviate the temperature by the greenhouse gas factor
        float finalTemperature = pixels[x][y].getHeightTemperature() + pixels[x][y].getGreenhouseGasFactor();
        findPixelTemperatureColour(x,y,finalTemperature);
        return finalTemperature;
    }

    /**
     * Set the pixel temperature colour for the temperature map
     * @param x The x coordinate of the pixel
     * @param y The y coordinate of the pixel
     * @param finalTemperature The pixel final temperature
     */
    public static void findPixelTemperatureColour(int x, int y, float finalTemperature) {
        Color temperatureColour;
        TemperatureSettings.Temperature foundTemp = null, prevTemp = null;
        /* The list TemperatureSettings.temperatures is a collection of Temperature objects.
         * A Temperature object has a value (degrees) and a colour (for the temperature colour map) associated with it
         * Neighbouring entries in the list constitute ranges
         * i.e. if we have the list [ {-15, Blue}, {0, Green}, {15, Red} ],
         * we have 2 temperature ranges, (-15,0) and (0,15)
         * For our temperature, we find the range it lies in (from this predefined set of temperature ranges)
         * So we can use the range bounds colours to calculate the temperature colour for our current pixel
         * It should be a mix between the colour bounds it lies between, and more similar to the one that it is closer to */
        for (TemperatureSettings.Temperature temperature : TemperatureSettings.temperatures) {
            // if we find the upper bound (a temperature that is higher than our current one)
            if(finalTemperature < temperature.value) {
                // store the upper bound and end the search
                foundTemp = temperature;
                break;
            }
            // the lower bound will be the one immediately before the found upper bound
            prevTemp = temperature;
        }

        // The temperature should never be out of known bounds (-60 degrees and 60 degrees)
        if(prevTemp == null) System.out.println("Temperature is lower than -60 degrees;" +
                                                " current temperature: " + finalTemperature);
        if(foundTemp == null) System.out.println("Temperature is higher than 60 degrees;" +
                                                 " current temperature: " + finalTemperature +
                                                 " noise height: " + pixels[x][y].getNoiseHeight());

        // Compute the intensity, which informs which bound our temperature is closer to
        // 0 < intensity < 1
        float temperatureIntensity = (finalTemperature - prevTemp.value) / (foundTemp.value - prevTemp.value);

        // Using this intensity, create a colour that is a mix between the two bounds
        // if intensity is closer to 0, the colour will be more like the lower bound colour
        // if the intensity is closer to 1, the colour will be more like the upper bound colour
        temperatureColour = Renderer.generateColor(prevTemp.colour, foundTemp.colour, temperatureIntensity, 0,2);
        pixels[x][y].setTemperature(new TemperatureSettings.Temperature(finalTemperature,temperatureColour));
    }

    /**
     * Set the type of terrain and colour on a pixel
     * @param x The x coordinate of the pixel
     * @param y The y coordinate of the pixel
     * @return The colour for the terrain on the pixel
     */
    public static Color getTerrain(int x, int y){
        // Get the pixel at these coordinates
        Pixel pixel = pixels[x][y];

        float foundHeight = -1f, prevHeight = -1f;
        int prevID = -1, foundID = -1;
        // Name the outer loop so we can break out of it from a nested loop
        outerloop:
        for(Map.Entry<Integer,Map<Float,Integer>> tempSec : TerrainSections.sections.entrySet()) {
            /* To find the terrain we take a similar approach as with the temperature colour
             * TerrainSections.section is a map of terrain types, sorted by temperature and height
             * i.e. the terrain type is predetermined by the temperature and height
             * one temperature and height pair only has one terrain type associated with it
             * we must find that terrain type for our current temperature and height pair
             * Sp, we first find the right temperature range
             */
            if(pixel.getTemperature().value < tempSec.getKey()) {
                /* And, inside that temperature range, we must find the height range */
                for (Map.Entry<Float,Integer> heightSec : tempSec.getValue().entrySet()) {
                    if(pixel.getNoiseHeight() < heightSec.getKey()) {
                        // By finding both of those, we determine the terrain our pixel needs to have
                        foundHeight = heightSec.getKey();
                        foundID = heightSec.getValue();
                        // When we find the terrain that fits the criteria, stop looking
                        break outerloop;
                    }
                    // The previous terrain will be the one immediately preceding the current one we found
                    prevHeight = heightSec.getKey();
                    prevID = heightSec.getValue();
                }
            }
        }

        // Compute the intensity, which informs which of the two terrains ours is closer to
        // 0 < intensity < 1
        float intensity = (pixel.getNoiseHeight() - foundHeight) / (prevHeight - foundHeight);

        // By default we want to have a smooth gradient between the terrain colours
        int function = 0;
        // Except when the terrain type is ice. When we have ice, the transition should be crisp,
        // because ice does not blend into terrain
        if(foundID == 1)
            function = 1;

        // Get the terrains by the ids
        TerrainType found, prev;
        found = TerrainSections.getTerrainByID(foundID);
        prev = TerrainSections.getTerrainByID(prevID);

        // Set the terrain type on the pixel
        pixel.setTerrainType(found);

        // The terrain should never be out of known bounds (noise height below 0 or above 1)
        if(found == null) System.out.println("Noise height is above 1; noise height: " + pixel.getNoiseHeight() +
                                            "; temperature: " + pixel.getTemperature().value);
        if(prev == null) System.out.println("Noise height is below 0; noise height: " + pixel.getNoiseHeight() +
                                            "; temperature: " + pixel.getTemperature().value + " found: " + found.getName() );

        // Using this intensity, create a colour that is a mix between the previous and found terrain colours
        // if intensity is closer to 0, the colour will be more like the found colour
        // if the intensity is closer to 1, the colour will be more like the previous colour
        return Renderer.generateColor(found.getColour(), prev.getColour(), intensity, function,5);
    }

    /**
     * Change the sea level by a given amount
     * @param amount The amount (in noise height) to change the sea level by
     */
    public static void updateSeaLevel(float amount) {
        // If the sea level should rise
        if(amount > 0) {
            // If the sea level reached its highest limit - it can't rise any higher
            if (SEA_LEVEL == ICE_MELTED_SEA_LEVEL){
                // don't change anything
            // Otherwise, increase by the given amount
            } else if ((SEA_LEVEL + amount) <= ICE_MELTED_SEA_LEVEL) {
                SEA_LEVEL += amount;
                // and reinitialise the sorted sections map with the new sea level
                TerrainSections.initSectionsMap();
            // But make sure not to rise the sea level above its highest limit
            } else {
                SEA_LEVEL = ICE_MELTED_SEA_LEVEL;
                TerrainSections.initSectionsMap();
            }
        // If the sea level should decrease
        } else {
            // If the sea level is already at its lowest limit, it can't decrease anymore
            if (SEA_LEVEL == Constants.BASE_SEA_LEVEL){
                // don't change anything
            // Otherwise, decrease it by the given amount, making sure not to decrease it past its lowest limit
            } else if ((SEA_LEVEL + amount) >= Constants.BASE_SEA_LEVEL) {
                SEA_LEVEL += amount;
                // Reinitialise the sorted sections map with the new sea level
                TerrainSections.initSectionsMap();
            } else {
                SEA_LEVEL = Constants.BASE_SEA_LEVEL;
                TerrainSections.initSectionsMap();
            }
        }
    }

    /**
      Start a thread that does the calculations to update the map
     * @param decades The number of decades the time changed by
     * @param increase If the map temperature should increase or decrease
     */
    public static void updateMap(int decades, boolean increase) {
        (new Thread(new TemperatureThread(decades, increase))).start();
    }

    /**
     * Deal with all calculation necessary to update the map after the year changed
     * @param decades How many decades the year changed by
     * @param increase If the map temperature should increase or decrease
     */
    public static void updateMapCalculation(int decades, boolean increase) {
        // Refresh global variables that track temperature and water and ice percentages
        totalMapTemperature = 0;
        waterPixels = 0;
        icePixels = 0;

        // If temperature needs to increase, increase the sea level
        // otherwise, decrease the sea level
        float sl = (increase) ? 0.0012f*decades : -0.0012f*decades;
        updateSeaLevel(sl);

        // For each pixel, recalculate its final temperature according to the new greenhouse gas value
        for(int x = 0; x < pixels.length; x++) {
            for(int y = 0; y < pixels[x].length; y++) {
                float t = calculateHeightTemperature(x,y);
                float baseGH = pixels[x][y].getGreenhouseHeight();
                float currentGH = pixels[x][y].getGreenhouseGasFactor();
                // Either increase or decrease the greenhouse gas level depending on what we need
                float gh = (increase) ? currentGH + baseGH*1.2f*decades : currentGH - baseGH*1.2f*decades;
                pixels[x][y].setGreenhouseGasFactor(gh);

                // Ensure we don't increase or decrease the temperature too much
                boolean condition = (increase) ? t < 39 : t > -15;
                if(condition) {
                    // Calculate the new final temperature given the changed greenhouse gas level
                    t = calculateFinalTemperature(x,y);

                    // And update everything else accordingly
                    // Find the new temperature colour
                    findPixelTemperatureColour(x,y,t);
                    // Recalculate the type of terrain according to the new temperature
                    Color terrainColour = getTerrain(x, y);
                    pixels[x][y].setColour(terrainColour);

                    // Count the water and ice pixels
                    if(Constants.WATER_TERRAINS.contains(pixels[x][y].getTerrainType().getId())) waterPixels++;
                    else if(Constants.ICE_TERRAINS.contains(pixels[x][y].getTerrainType().getId())) icePixels++;
                    // And increase the total map temperature
                    totalMapTemperature += t;
                }
            }
        }



        // And update the animals according to the changes
        updateAnimals();

        // Prepare temperature information for user
        averageWorldTemperature = totalMapTemperature / (pixels.length * pixels[0].length);
        float tempChange;
        boolean tempCond = averageWorldTemperature > initialAverageTemperature;
        if(tempCond) tempChange = averageWorldTemperature - initialAverageTemperature;
        else tempChange = initialAverageTemperature - averageWorldTemperature;

        // Add update to dialog text
        userInterface.dialogText += "New average world temperature: " + String.format("%.2f",averageWorldTemperature) + "\u00B0" + "C\n";
        // Print updates to the user information box
        Main.userInterface.printToUserTextBox("New average world temperature: " + String.format("%.2f",averageWorldTemperature) + "\u00B0" + "C");
        if(userInterface.timelineSlider.getValue()!=2000.0) {
            Main.userInterface.printToUserTextBox("The world temperature increased by " + String.format("%.2f", tempChange) + "\u00B0" + "C since 2000");
            userInterface.dialogText += "The temperature increased by " + String.format("%.2f", tempChange) + "\u00B0" + "C since 2000\n";
        }

        Main.userInterface.printToUserTextBox("Water percentage: " +  String.format("%.2f",(waterPixels *100.0f)/(Constants.MAP_SIZE_Y * Constants.MAP_SIZE_X)) + "%");
        Main.userInterface.printToUserTextBox("Ice percentage: " +  String.format("%.2f",(icePixels *100.0f)/(Constants.MAP_SIZE_Y * Constants.MAP_SIZE_X)) + "%");
    }

    /**
     * Update the animals after temperature changed
     */
    public static void updateAnimals() {
        // Lists to track changes that occurred
        Map<String,Integer> deaths = new HashMap<>();
        Map<String,Integer> reverts = new HashMap<>();

        // For each animal in the world
        for (Animal animal : animals) {
            // Get the terrain it is currently located on
            int terrainID = pixels[animal.getX()][animal.getY()].getTerrainType().getId();
            // If the animal is alive, check if the terrain is still inhabitable by this animal
            if (animal.isAlive() && !animal.getCompatibleTerrainsIDs().contains(terrainID)) {
                // If it is not, the animal dies
                animal.setAlive(false);
                // Keep a record the death
                if(deaths.containsKey(animal.getName()))
                    deaths.put(animal.getName(), deaths.get(animal.getName())+1);
                else deaths.put(animal.getName(), 1);
            // If the animal was dead, check if the terrain it lies on is now inhabitable by this animal
            } else if (!animal.isAlive() && animal.getCompatibleTerrainsIDs().contains(terrainID)) {
                // If it is, set its state back to alive
                animal.setAlive(true);
                // Keep a record of the change
                if(reverts.containsKey(animal.getName()))
                    reverts.put(animal.getName(), reverts.get(animal.getName())+1);
                else reverts.put(animal.getName(), 1);
            }
        }

        // Print how many of each species died
        for(Map.Entry<String,Integer> entry : deaths.entrySet()) {
            Main.userInterface.printToUserTextBox(entry.getValue() + " of the " + entry.getKey() + "s died.");
            userInterface.dialogText += entry.getValue() + " of the " + entry.getKey() + "s died." + "\n";
        }

        // Print how many of each species came back to life
        for(Map.Entry<String,Integer> entry : reverts.entrySet()) {
            Main.userInterface.printToUserTextBox(entry.getValue() + " of the " + entry.getKey() + "s are back.");
            userInterface.dialogText += entry.getValue() + " of the " + entry.getKey() + "s are back." + "\n";
        }

        deaths.clear();
        reverts.clear();
        // Print the percentage of animals lost in total
        lifePercentages();
    }


    /**
     * Print the percentage of animals lost of each species in the user information box
     */
    public static void lifePercentages() {
        // A map with all the species and their number of dead individuals
        Map<String,Integer> dead = new HashMap<>();
        // A map with all the species and their total number of individuals
        Map<String,Integer> total = new HashMap<>();

        for(Animal a : animals) {
            // Count all animals (alive or not)
            if(total.containsKey(a.getName()))
                total.put(a.getName(), total.get(a.getName())+1);
            else
                total.put(a.getName(), 1);
            // Count all animals no longer alive
            if(!a.isAlive()) {
                if (dead.containsKey(a.getName()))
                    dead.put(a.getName(), dead.get(a.getName()) + 1);
                else dead.put(a.getName(), 1);
            }
        }

        // Print the percentage of animals lost of each species
        for(Map.Entry<String,Integer> totalEntry : total.entrySet()) {
            if (dead.get(totalEntry.getKey()) == null) {
                Main.userInterface.printToUserTextBox("The " + totalEntry.getKey() + " population is still 100% alive");
                userInterface.dialogText += "The " + totalEntry.getKey() + " population is still 100% alive\n";
            } else {
                float res = (dead.get(totalEntry.getKey()) * 100f)/totalEntry.getValue();
                Main.userInterface.printToUserTextBox("In total, we lost " +  String.format("%.2f",res) + "% of the " + totalEntry.getKey() + " population");
                userInterface.dialogText += "In total, we lost " +  String.format("%.2f",res) + "% of the " + totalEntry.getKey() + " population\n";
            }
        }
        total.clear();
        dead.clear();
    }


    /**
     * Set the temperature on a pixel
     * @param x The pixel x coordinate
     * @param y The pixel y coordinate
     */
    public static void oldSetTemperature(int x, int y) {
        // The result (temperature map) looks right, but it is not backed by research
        // Just the idea that temperature can be a function of latitude and height

        // temperature should be between -35 at y = 0 or y = mapHeight and 45 at y = mapHeight/2 (i.e. equator)
        int latitude = y < Constants.MAP_SIZE_Y / 2 ? y : Constants.MAP_SIZE_Y - y;
        latitude += 1;
        // temperature should pe highest around sea level, which is 0.6 usually
        // and decrease symmetrically as height increases/decreases
        float terrainHeight = pixels[x][y].getNoiseHeight() < 0.6f ? pixels[x][y].getNoiseHeight() : 1.2f - pixels[x][y].getNoiseHeight();

        // Empirical function (verifiable by observation rather than theory)
        float temp = (float) Math.sqrt(terrainHeight) * (float) Math.cbrt(latitude);

        // Track minimum and maximum temperature
        if (temp > maxTemp) maxTemp = temp;
        if (temp < minTemp) minTemp = temp;

        // Scale the result so it fits our expected temperature range
        float saveTemp = Renderer.scaleToRange(0f, 5.83f, -30f, 45f, temp);

        // Find the temperature colour
        findPixelTemperatureColour(x, y, saveTemp);
        totalMapTemperature += saveTemp;
    }
}
