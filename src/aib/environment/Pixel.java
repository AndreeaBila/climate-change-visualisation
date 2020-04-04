package aib.environment;

import javafx.scene.paint.Color;

/**
 * Each pixel on the map holds relevant information. This class deals with storing that information.
 */
public class Pixel {
    /** The Perlin noise height value of the pixel */
    private float noiseHeight;
    /** The pixel terrain height converted to meters */
    private float terrainHeight;
    /** The latitude this pixel lies on */
    private float latitude;
    /** The temperature at this latitude */
    private float latitudeTemperature;
    /** The base temperature at this pixel, determined by its latitude and height */
    private float heightTemperature;
    /** The level of greenhouse gas on this pixel */
    private float greenhouseGasFactor;
    /** The initial greenhouse gas level of the point */
    private float greenhouseHeight;
    /** Object with the final temperature information of this pixel, containing the temperature value,
     * determined by the greenhouse gas level and the base temperature (heightTemperature) */
    private TemperatureSettings.Temperature temperature;
    /** The terrain type of this pixel, determined by its height and temperature */
    private TerrainType terrainType;
    /** The colour of the pixel, determined by its terrain type */
    private Color colour;


    /**
     * Constructor allowing to create a new pixel, given the Perlin noise value
     * @param noiseHeight The Perlin noise value for this pixel
     */
    public Pixel(float noiseHeight) {
        this.noiseHeight = noiseHeight;
        this.colour = null;
        this.terrainType = null;
        this.temperature = null;
    }

    /**
     * Get the Perlin noise height value for the pixel
     * @return The Perlin noise height value for the pixel
     */
    public float getNoiseHeight() {
        return noiseHeight;
    }

    /**
     * Set the Perlin noise height value for the pixel
     * @param noiseHeight The Perlin noise height value for the pixel
     */
    public void setNoiseHeight(float noiseHeight) {
        this.noiseHeight = noiseHeight;
    }

    /**
     * Get the terrain height in meters for this pixel
     * @return The terrain height in meters
     */
    public float getTerrainHeight() {
        return terrainHeight;
    }

    /**
     * Set the terrain height in meters for this pixel
     * @param terrainHeight The terrain height in meters for this pixel
     */
    public void setTerrainHeight(float terrainHeight) {
        this.terrainHeight = terrainHeight;
    }

    /**
     * Get the latitude of the pixel
     * @return The latitude of the pixel
     */
    public float getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude of the pixel
     * @param latitude The latitude the pixel needs to have
     */
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    /**
     * Get the temperature for the latitude of this pixel
     * @return The temperature for the latitude of this pixel
     */
    public float getLatitudeTemperature() {
        return latitudeTemperature;
    }

    /**
     * Set the temperature for the latitude of this pixel
     * @param latitudeTemperature The temperature for the latitude of this pixel
     */
    public void setLatitudeTemperature(float latitudeTemperature) {
        this.latitudeTemperature = latitudeTemperature;
    }

    /**
     * Get the base temperature at this pixel, determined by its latitude and height
     * @return The base temperature at this pixel, determined by its latitude and height
     */
    public float getHeightTemperature() {
        return heightTemperature;
    }

    /**
     * Set the base temperature at this pixel, determined by its latitude and height
     * @param heightTemperature The base temperature at this pixel, determined by its latitude and height
     */
    public void setHeightTemperature(float heightTemperature) {
        this.heightTemperature = heightTemperature;
    }

    /**
     * Get the level of greenhouse gas on this pixel
     * @return The level of greenhouse gas on this pixel
     */
    public float getGreenhouseGasFactor() {
        return greenhouseGasFactor;
    }

    /**
     * Set the level of greenhouse gas on this pixel
     * @param greenhouseGasFactor The level of greenhouse gas on this pixel
     */
    public void setGreenhouseGasFactor(float greenhouseGasFactor) {
        this.greenhouseGasFactor = greenhouseGasFactor;
    }

    /**
     * Get the initial greenhouse gas level of the point, obtained through a second layer of Perlin noise
     * @return The initial greenhouse gas level of the point
     */
    public float getGreenhouseHeight() {
        return greenhouseHeight;
    }

    /**
     * Set the initial greenhouse gas level of the point
     * @param greenhouseHeight The initial greenhouse gas level of the point
     */
    public void setGreenhouseHeight(float greenhouseHeight) {
        this.greenhouseHeight = greenhouseHeight;
    }

    /**
     * Get the object with the final temperature information of this pixel
     * @return The object with the final temperature information of this pixel
     */
    public TemperatureSettings.Temperature getTemperature() {
        return temperature;
    }

    /**
     * Set the object with the final temperature information of this pixel
     * @param temperature The object with the final temperature information of this pixel
     */
    public void setTemperature(TemperatureSettings.Temperature temperature) {
        this.temperature = temperature;
    }

    /**
     * Get the terrain type of this pixel, determined by its height and temperature
     * @return The terrain type of this pixel
     */
    public TerrainType getTerrainType() {
        return terrainType;
    }

    /**
     * Set the terrain type of this pixel
     * @param terrainType The terrain type of this pixel
     */
    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    /**
     * Get the colour of the pixel, determined by its terrain type
     * @return The colour of the pixel, determined by its terrain type
     */
    public Color getColour() {
        return colour;
    }

    /**
     * Set the colour of the pixel, determined by its terrain type
     * @param colour The colour of the pixel, determined by its terrain type
     */
    public void setColour(Color colour) {
        this.colour = colour;
    }
}
